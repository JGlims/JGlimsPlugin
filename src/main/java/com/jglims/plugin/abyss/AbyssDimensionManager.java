package com.jglims.plugin.abyss;

import com.jglims.plugin.JGlimsPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.util.*;
import java.util.logging.Logger;

/**
 * AbyssDimensionManager v6.0
 * 
 * Manages the Abyss custom dimension (world_abyss).
 * Player spawns at Z~+200 (south), walks north to cathedral, then to arena at origin.
 * Dragon naturally orbits (0,128,0) which is directly above our arena.
 */
public class AbyssDimensionManager implements Listener {

    private static final String WORLD_NAME = "world_abyss";
    private static final int SPAWN_Z = 200;  // Must match builder
    private static final int ARENA_CENTER_Z = 0;  // Arena at origin now

    private final JGlimsPlugin plugin;
    private final Logger log;
    private World abyssWorld;
    private final Set<Location> activePortalBlocks = new HashSet<>();
    private final Map<UUID, Long> portalCooldowns = new HashMap<>();

    public AbyssDimensionManager(JGlimsPlugin plugin) {
        this.plugin = plugin;
        this.log = plugin.getLogger();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        initializeWorld();
    }

    private void initializeWorld() {
        abyssWorld = Bukkit.getWorld(WORLD_NAME);
        if (abyssWorld == null) {
            log.info("[Abyss] Creating custom dimension: " + WORLD_NAME);
            WorldCreator creator = new WorldCreator(WORLD_NAME);
            creator.environment(World.Environment.THE_END);
            creator.type(WorldType.FLAT);
            creator.generatorSettings("{\"layers\":[],\"biome\":\"the_end\"}");
            creator.generateStructures(false);
            abyssWorld = creator.createWorld();
        }

        if (abyssWorld != null) {
            // World rules
            abyssWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
            abyssWorld.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
            abyssWorld.setGameRule(GameRule.DO_MOB_SPAWNING, false);  // We control spawning
            abyssWorld.setGameRule(GameRule.KEEP_INVENTORY, true);
            abyssWorld.setGameRule(GameRule.MOB_GRIEFING, false);
            abyssWorld.setGameRule(GameRule.DO_FIRE_TICK, false);
            abyssWorld.setDifficulty(Difficulty.HARD);
            abyssWorld.setTime(18000); // Night
            abyssWorld.setStorm(false);

            // Check if citadel needs building
            Block check = abyssWorld.getBlockAt(0, 50, 0); // Arena center
            if (check.getType() == Material.AIR) {
                log.info("[Abyss] Building massive Abyssal Citadel on fresh world...");
                AbyssCitadelBuilder builder = new AbyssCitadelBuilder(abyssWorld, log);
                builder.build();
            } else {
                log.info("[Abyss] Citadel already built (found " + check.getType() + " at arena center).");
            }

            // Start ambient mob spawner (reduced enderman rate)
            startAmbientSpawner();
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  TELEPORT TO ABYSS
    // ════════════════════════════════════════════════════════════════
    public void teleportToAbyss(Player player) {
        if (abyssWorld == null) {
            player.sendMessage(Component.text("Abyss dimension not available!", NamedTextColor.RED));
            return;
        }

        int safeY = findSafeY(0, SPAWN_Z);
        Location dest = new Location(abyssWorld, 0.5, safeY, SPAWN_Z + 0.5, 0, 0); // Face north (toward cathedral)

        player.teleport(dest);
        player.showTitle(Title.title(
            Component.text("The Abyss", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD),
            Component.text("Face what lies within...", NamedTextColor.LIGHT_PURPLE),
            Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofSeconds(1))
        ));
        player.playSound(player.getLocation(), Sound.AMBIENT_CAVE, 1.0f, 0.5f);
    }

    // ════════════════════════════════════════════════════════════════
    //  ABYSSAL KEY & PORTAL
    // ════════════════════════════════════════════════════════════════
    public ItemStack createAbyssalKey() {
        ItemStack key = new ItemStack(Material.ECHO_SHARD);
        ItemMeta meta = key.getItemMeta();
        meta.displayName(Component.text("Abyssal Key", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Use on a Purpur frame to", NamedTextColor.GRAY));
        lore.add(Component.text("open the Abyss portal.", NamedTextColor.GRAY));
        lore.add(Component.empty());
        lore.add(Component.text("WARNING: Point of no return!", NamedTextColor.RED, TextDecoration.ITALIC));
        meta.lore(lore);
        meta.setCustomModelData(9001);
        NamespacedKey nsk = new NamespacedKey(plugin, "abyssal_key");
        meta.getPersistentDataContainer().set(nsk, PersistentDataType.BYTE, (byte) 1);
        key.setItemMeta(meta);
        return key;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        ItemStack hand = player.getInventory().getItemInMainHand();
        Block clicked = event.getClickedBlock();

        if (clicked == null || hand.getType() != Material.ECHO_SHARD) return;
        ItemMeta meta = hand.getItemMeta();
        if (meta == null || !meta.hasCustomModelData() || meta.getCustomModelData() != 9001) return;

        // Check for purpur frame
        if (clicked.getType() != Material.PURPUR_BLOCK) return;

        // Cooldown check
        long now = System.currentTimeMillis();
        Long last = portalCooldowns.get(player.getUniqueId());
        if (last != null && now - last < 10000) {
            player.sendMessage(Component.text("Portal cooling down...", NamedTextColor.GRAY));
            return;
        }
        portalCooldowns.put(player.getUniqueId(), now);

        // Consume key
        hand.setAmount(hand.getAmount() - 1);

        // Create portal effect
        Location portalLoc = clicked.getLocation().add(0, 1, 0);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                Block portalBlock = portalLoc.getWorld().getBlockAt(
                    portalLoc.getBlockX() + dx, portalLoc.getBlockY(), portalLoc.getBlockZ() + dz);
                portalBlock.setType(Material.END_GATEWAY);
                activePortalBlocks.add(portalBlock.getLocation());
            }
        }

        // Particles and sound
        portalLoc.getWorld().spawnParticle(Particle.PORTAL, portalLoc.getX(), portalLoc.getY() + 1, portalLoc.getZ(),
            100, 1, 1, 1, 0.5);
        portalLoc.getWorld().playSound(portalLoc, Sound.BLOCK_END_PORTAL_SPAWN, 1.0f, 0.5f);

        player.showTitle(Title.title(
            Component.text("The Abyss Opens", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD),
            Component.text("Step through before it closes...", NamedTextColor.LIGHT_PURPLE),
            Title.Times.times(Duration.ofMillis(300), Duration.ofSeconds(2), Duration.ofMillis(300))
        ));

        // Close portal after 30 seconds
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Location loc : new ArrayList<>(activePortalBlocks)) {
                loc.getBlock().setType(Material.AIR);
            }
            activePortalBlocks.clear();
        }, 600L);

        // Teleport player after 2 seconds
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                teleportToAbyss(player);
            }
        }, 40L);
    }

    // ════════════════════════════════════════════════════════════════
    //  AMBIENT MOB SPAWNER (reduced rate, no endermen near arena)
    // ════════════════════════════════════════════════════════════════
    private void startAmbientSpawner() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (abyssWorld == null) return;
                List<Player> players = abyssWorld.getPlayers();
                if (players.isEmpty()) return;

                // Count current ambient mobs
                int ambientCount = 0;
                for (Entity e : abyssWorld.getEntities()) {
                    if (e.getScoreboardTags().contains("abyss_ambient")) ambientCount++;
                }
                if (ambientCount >= 8) return;  // Reduced cap for performance

                for (Player player : players) {
                    if (player.getGameMode() == GameMode.SPECTATOR || player.getGameMode() == GameMode.CREATIVE) continue;

                    Location pLoc = player.getLocation();

                    // Don't spawn near arena (radius 50 from origin)
                    double distFromArena = Math.sqrt(pLoc.getX() * pLoc.getX() + pLoc.getZ() * pLoc.getZ());
                    if (distFromArena < 55) continue;

                    // Spawn 25-45 blocks away
                    double angle = Math.random() * Math.PI * 2;
                    double dist = 25 + Math.random() * 20;
                    int sx = pLoc.getBlockX() + (int)(Math.cos(angle) * dist);
                    int sz = pLoc.getBlockZ() + (int)(Math.sin(angle) * dist);
                    int sy = findSafeY(sx, sz);

                    if (sy < 0) continue;

                    Location spawnLoc = new Location(abyssWorld, sx + 0.5, sy, sz + 0.5);
                    EntityType type = Math.random() < 0.5 ? EntityType.ENDERMAN : EntityType.WITHER_SKELETON;

                    LivingEntity mob = (LivingEntity) abyssWorld.spawnEntity(spawnLoc, type);
                    mob.addScoreboardTag("abyss_ambient");
                    mob.setPersistent(false);
                    if (type == EntityType.ENDERMAN) {
                        mob.setMaxHealth(50);
                        mob.setHealth(50);
                    } else {
                        mob.setMaxHealth(40);
                        mob.setHealth(40);
                        ((WitherSkeleton) mob).getEquipment().setItemInMainHand(new ItemStack(Material.STONE_SWORD));
                    }
                }
            }
        }.runTaskTimer(plugin, 400L, 400L + (long)(Math.random() * 200)); // Every 20-30 seconds
    }

    // ════════════════════════════════════════════════════════════════
    //  HELPERS
    // ════════════════════════════════════════════════════════════════
    private int findSafeY(int x, int z) {
        if (abyssWorld == null) return 51;
        for (int y = 120; y >= 0; y--) {
            if (abyssWorld.getBlockAt(x, y, z).getType() != Material.AIR
                && abyssWorld.getBlockAt(x, y + 1, z).getType() == Material.AIR
                && abyssWorld.getBlockAt(x, y + 2, z).getType() == Material.AIR) {
                return y + 1;
            }
        }
        return 51; // default above baseY
    }

    public World getAbyssWorld() { return abyssWorld; }
}
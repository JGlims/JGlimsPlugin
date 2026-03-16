package com.jglims.plugin.abyss;

import com.jglims.plugin.JGlimsPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.util.*;

/**
 * AbyssDimensionManager v4.0 — Matched to AbyssCitadelBuilder v4.0 Gothic Cathedral
 *
 * Teleport destination: south approach at (0.5, safeY, 115.5) facing north (180f yaw)
 * The cathedral entrance faces +Z (south), so players land outside looking at the facade.
 *
 * Arena is at z=-120 (behind the cathedral). The ambient mob exclusion zone
 * avoids a 50-block radius around (0, y, -120).
 */
public class AbyssDimensionManager implements Listener {

    private static final String ABYSS_WORLD_NAME = "world_abyss";
    private static final int ARENA_CENTER_Z = -120; // must match AbyssDragonBoss
    private final JGlimsPlugin plugin;
    private World abyssWorld;
    private boolean citadelBuilt = false;
    private final Map<UUID, Set<String>> activePortalBlocks = new HashMap<>();
    private final Set<UUID> teleportCooldown = new HashSet<>();

    public AbyssDimensionManager(JGlimsPlugin plugin) {
        this.plugin = plugin;
    }

    // ═══════════════════════════════════════════════════════════
    //  INITIALIZATION
    // ═══════════════════════════════════════════════════════════
    public void initialize() {
        abyssWorld = Bukkit.getWorld(ABYSS_WORLD_NAME);
        boolean freshWorld = (abyssWorld == null);

        if (freshWorld) {
            plugin.getLogger().info("[Abyss] Creating dimension: " + ABYSS_WORLD_NAME);
            WorldCreator creator = new WorldCreator(ABYSS_WORLD_NAME);
            creator.environment(World.Environment.NORMAL);
            creator.generator(new AbyssChunkGenerator());
            abyssWorld = creator.createWorld();
        } else {
            plugin.getLogger().info("[Abyss] Loaded existing dimension: " + ABYSS_WORLD_NAME);
        }

        if (abyssWorld != null) {
            abyssWorld.setDifficulty(Difficulty.HARD);
            abyssWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
            abyssWorld.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
            abyssWorld.setGameRule(GameRule.DO_MOB_SPAWNING, false);
            abyssWorld.setGameRule(GameRule.KEEP_INVENTORY, true);
            abyssWorld.setGameRule(GameRule.MOB_GRIEFING, false);
            abyssWorld.setGameRule(GameRule.DO_FIRE_TICK, false);
            abyssWorld.setTime(18000);
            abyssWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);

            // Enforce permanent midnight and dark atmosphere
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (abyssWorld == null) return;
                    abyssWorld.setTime(18000);
                    abyssWorld.setStorm(false);
                    abyssWorld.setThundering(false);
                }
            }.runTaskTimer(plugin, 200L, 6000L);

            // No vanilla dragon cleanup needed — NORMAL environment

            startAmbientSpawner();

            if (freshWorld) {
                plugin.getLogger().info("[Abyss] Building massive Abyssal Citadel on fresh world...");
                new AbyssCitadelBuilder(plugin, abyssWorld).build();
                citadelBuilt = true;
                plugin.getLogger().info("[Abyss] Citadel construction complete.");
            } else {
                citadelBuilt = true;
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  AMBIENT MOB SPAWNER
    // ═══════════════════════════════════════════════════════════
    private void startAmbientSpawner() {
        new BukkitRunnable() {
            final Random rng = new Random();
            @Override
            public void run() {
                if (abyssWorld == null) return;
                for (Player p : abyssWorld.getPlayers()) {
                    Location pLoc = p.getLocation();
                    long ambientCount = abyssWorld.getEntities().stream()
                        .filter(e -> e.getScoreboardTags().contains("abyss_ambient"))
                        .count();
                    if (ambientCount >= 12) continue;

                    double angle = rng.nextDouble() * Math.PI * 2;
                    double dist = 25 + rng.nextDouble() * 20;
                    double sx = pLoc.getX() + Math.cos(angle) * dist;
                    double sz = pLoc.getZ() + Math.sin(angle) * dist;

                    // Don't spawn inside arena zone (50-block radius around arena center)
                    double arenaDistSq = sx * sx + (sz - ARENA_CENTER_Z) * (sz - ARENA_CENTER_Z);
                    if (arenaDistSq < 50 * 50) continue;

                    int sy = findSafeY(abyssWorld, (int) sx, (int) sz);
                    Location spawnLoc = new Location(abyssWorld, sx, sy, sz);

                    if (rng.nextDouble() < 0.6) {
                        abyssWorld.spawn(spawnLoc, Enderman.class, e -> {
                            e.customName(Component.text("Void Wanderer", NamedTextColor.DARK_PURPLE));
                            e.setCustomNameVisible(true);
                            e.addScoreboardTag("abyss_ambient");
                            Objects.requireNonNull(e.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(50);
                            e.setHealth(50);
                        });
                    } else {
                        abyssWorld.spawn(spawnLoc, WitherSkeleton.class, ws -> {
                            ws.customName(Component.text("Abyssal Sentinel", NamedTextColor.DARK_RED));
                            ws.setCustomNameVisible(true);
                            ws.addScoreboardTag("abyss_ambient");
                            Objects.requireNonNull(ws.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(40);
                            ws.setHealth(40);
                            ws.getEquipment().setItemInMainHand(new ItemStack(Material.STONE_SWORD));
                        });
                    }
                }
            }
        }.runTaskTimer(plugin, 400L, 400L + new Random().nextInt(200));
    }

    // ═══════════════════════════════════════════════════════════
    //  ABYSSAL KEY
    // ═══════════════════════════════════════════════════════════
    public ItemStack createAbyssalKey() {
        ItemStack key = new ItemStack(Material.ECHO_SHARD, 1);
        ItemMeta meta = key.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Abyssal Key", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("A key forged in the void.", NamedTextColor.GRAY));
            lore.add(Component.text("Use on a Purpur portal frame", NamedTextColor.GRAY));
            lore.add(Component.text("to open a gateway to the Abyss.", NamedTextColor.DARK_GRAY));
            lore.add(Component.empty());
            lore.add(Component.text("Abyssal Key", NamedTextColor.DARK_PURPLE));
            meta.lore(lore);
            meta.setCustomModelData(9001);
            key.setItemMeta(meta);
        }
        return key;
    }

    private boolean isAbyssalKey(ItemStack item) {
        if (item == null || item.getType() != Material.ECHO_SHARD) return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.hasCustomModelData() && meta.getCustomModelData() == 9001;
    }

    // ═══════════════════════════════════════════════════════════
    //  PORTAL ACTIVATION
    // ═══════════════════════════════════════════════════════════
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block clicked = event.getClickedBlock();
        if (clicked == null || clicked.getType() != Material.PURPUR_BLOCK) return;
        Player player = event.getPlayer();
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (!isAbyssalKey(hand)) return;

        Location frameLoc = findPortalFrame(clicked.getLocation());
        if (frameLoc == null) {
            player.sendMessage(Component.text("The key resonates... but no valid portal frame is found.", NamedTextColor.DARK_PURPLE));
            return;
        }

        event.setCancelled(true);
        if (hand.getAmount() > 1) hand.setAmount(hand.getAmount() - 1);
        else player.getInventory().setItemInMainHand(null);
        activatePortal(frameLoc, player);
    }

    private Location findPortalFrame(Location loc) {
        World w = loc.getWorld();
        int cx = loc.getBlockX(), cy = loc.getBlockY(), cz = loc.getBlockZ();
        for (int ox = -4; ox <= 1; ox++) {
            for (int oy = -5; oy <= 0; oy++) {
                Location b = new Location(w, cx + ox, cy + oy, cz);
                if (checkFrame(b, true)) return b;
            }
        }
        for (int oz = -4; oz <= 1; oz++) {
            for (int oy = -5; oy <= 0; oy++) {
                Location b = new Location(w, cx, cy + oy, cz + oz);
                if (checkFrame(b, false)) return b;
            }
        }
        return null;
    }

    private boolean checkFrame(Location base, boolean xAxis) {
        World w = base.getWorld();
        int bx = base.getBlockX(), by = base.getBlockY(), bz = base.getBlockZ();
        if (xAxis) {
            for (int i = 0; i < 4; i++) {
                if (w.getBlockAt(bx + i, by, bz).getType() != Material.PURPUR_BLOCK) return false;
                if (w.getBlockAt(bx + i, by + 4, bz).getType() != Material.PURPUR_BLOCK) return false;
            }
            for (int i = 0; i < 5; i++) {
                if (w.getBlockAt(bx, by + i, bz).getType() != Material.PURPUR_BLOCK) return false;
                if (w.getBlockAt(bx + 3, by + i, bz).getType() != Material.PURPUR_BLOCK) return false;
            }
            for (int x = 1; x <= 2; x++) for (int y = 1; y <= 3; y++) {
                if (!w.getBlockAt(bx + x, by + y, bz).getType().isAir()) return false;
            }
        } else {
            for (int i = 0; i < 4; i++) {
                if (w.getBlockAt(bx, by, bz + i).getType() != Material.PURPUR_BLOCK) return false;
                if (w.getBlockAt(bx, by + 4, bz + i).getType() != Material.PURPUR_BLOCK) return false;
            }
            for (int i = 0; i < 5; i++) {
                if (w.getBlockAt(bx, by + i, bz).getType() != Material.PURPUR_BLOCK) return false;
                if (w.getBlockAt(bx, by + i, bz + 3).getType() != Material.PURPUR_BLOCK) return false;
            }
            for (int z = 1; z <= 2; z++) for (int y = 1; y <= 3; y++) {
                if (!w.getBlockAt(bx, by + y, bz + z).getType().isAir()) return false;
            }
        }
        return true;
    }

    private void activatePortal(Location frameLoc, Player player) {
        World w = frameLoc.getWorld();
        int bx = frameLoc.getBlockX(), by = frameLoc.getBlockY(), bz = frameLoc.getBlockZ();
        boolean xAxis = checkFrame(frameLoc, true);

        Set<String> blockKeys = new HashSet<>();
        if (xAxis) {
            for (int x = 1; x <= 2; x++) for (int y = 1; y <= 3; y++) {
                Block b = w.getBlockAt(bx + x, by + y, bz);
                b.setType(Material.END_GATEWAY, false);
                blockKeys.add((bx + x) + "," + (by + y) + "," + bz);
            }
        } else {
            for (int z = 1; z <= 2; z++) for (int y = 1; y <= 3; y++) {
                Block b = w.getBlockAt(bx, by + y, bz + z);
                b.setType(Material.END_GATEWAY, false);
                blockKeys.add(bx + "," + (by + y) + "," + (bz + z));
            }
        }

        UUID wid = w.getUID();
        activePortalBlocks.put(wid, blockKeys);

        Location center = frameLoc.clone().add(xAxis ? 1.5 : 0.5, 2.5, xAxis ? 0.5 : 1.5);
        w.playSound(center, Sound.BLOCK_END_PORTAL_SPAWN, 1.5f, 0.5f);
        w.spawnParticle(Particle.PORTAL, center, 200, 1, 2, 1, 0.5);
        w.spawnParticle(Particle.REVERSE_PORTAL, center, 100, 1, 2, 1, 0.3);
        player.sendMessage(Component.text("The Abyssal Gateway opens...", NamedTextColor.DARK_PURPLE, TextDecoration.ITALIC));

        final Set<String> keys = blockKeys;
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                ticks++;
                if (ticks > 600) {
                    for (String k : keys) {
                        String[] parts = k.split(",");
                        Block b = w.getBlockAt(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
                        if (b.getType() == Material.END_GATEWAY) b.setType(Material.AIR);
                    }
                    activePortalBlocks.remove(wid);
                    w.playSound(center, Sound.BLOCK_PORTAL_TRAVEL, 0.8f, 1.5f);
                    cancel();
                    return;
                }
                if (ticks % 5 == 0) {
                    w.spawnParticle(Particle.PORTAL, center, 30, 0.5, 1.5, 0.5, 0.3);
                    w.spawnParticle(Particle.REVERSE_PORTAL, center, 15, 0.5, 1.5, 0.5, 0.2);
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    // ═══════════════════════════════════════════════════════════
    //  PORTAL TELEPORT
    // ═══════════════════════════════════════════════════════════
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getTo() == null) return;
        Player player = event.getPlayer();
        if (player.getWorld().getName().equals(ABYSS_WORLD_NAME)) return;
        if (teleportCooldown.contains(player.getUniqueId())) return;

        Location to = event.getTo();
        String key = to.getBlockX() + "," + to.getBlockY() + "," + to.getBlockZ();
        UUID wid = player.getWorld().getUID();
        Set<String> portals = activePortalBlocks.get(wid);
        if (portals == null || !portals.contains(key)) return;

        teleportCooldown.add(player.getUniqueId());
        teleportToAbyss(player);
        new BukkitRunnable() {
            @Override public void run() { teleportCooldown.remove(player.getUniqueId()); }
        }.runTaskLater(plugin, 100L);
    }

    // ═══════════════════════════════════════════════════════════
    //  TELEPORT TO ABYSS
    // ═══════════════════════════════════════════════════════════
    public void teleportToAbyss(Player player) {
        if (abyssWorld == null) {
            player.sendMessage(Component.text("The Abyss is not yet ready.", NamedTextColor.RED));
            return;
        }
        // Land on approach path outside south gate: z=115, facing north (yaw=180)
        int safeY = findSafeY(abyssWorld, 0, 115);
        Location dest = new Location(abyssWorld, 0.5, safeY, 115.5, 180f, 0f);
        player.teleport(dest);

        Title.Times times = Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(1000));
        player.showTitle(Title.title(
            Component.text("THE ABYSS", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD),
            Component.text("Abandon all hope...", NamedTextColor.GRAY, TextDecoration.ITALIC),
            times
        ));
        player.playSound(dest, Sound.AMBIENT_CAVE, 1.0f, 0.5f);
        player.playSound(dest, Sound.ENTITY_ENDER_DRAGON_AMBIENT, 0.5f, 0.5f);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (abyssWorld == null) return;
                for (EnderDragon d : abyssWorld.getEntitiesByClass(EnderDragon.class)) {
                    if (d.getCustomName() == null) d.remove();
                }
            }
        }.runTaskLater(plugin, 40L);
    }

    // ═══════════════════════════════════════════════════════════
    //  UTILITIES
    // ═══════════════════════════════════════════════════════════
    public int findSafeY(World world, int x, int z) {
        for (int y = 120; y > 1; y--) {
            Block b = world.getBlockAt(x, y, z);
            Block a1 = world.getBlockAt(x, y + 1, z);
            Block a2 = world.getBlockAt(x, y + 2, z);
            if (b.getType().isSolid() && !a1.getType().isSolid() && !a2.getType().isSolid()) return y + 1;
        }
        return 60;
    }

    public World getAbyssWorld() { return abyssWorld; }
    public JGlimsPlugin getPlugin() { return plugin; }
    public boolean isCitadelBuilt() { return citadelBuilt; }
}
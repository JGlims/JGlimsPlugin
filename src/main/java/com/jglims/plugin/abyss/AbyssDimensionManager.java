package com.jglims.plugin.abyss;

import com.jglims.plugin.JGlimsPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.boss.DragonBattle;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.util.*;

/**
 * AbyssDimensionManager v9.0 — Complete rewrite
 * Fixes: phantom Ender Dragon boss bar, world environment,
 *        portal detection, NPC leak, and arena coordinates.
 */
public class AbyssDimensionManager implements Listener {

    private static final String WORLD_NAME = "world_abyss";
    public static final int ARENA_CENTER_Z = -120;

    private final JGlimsPlugin plugin;
    private World abyssWorld;
    private boolean citadelBuilt = false;

    // Active portal gateway blocks
    private final Set<Location> activePortalBlocks = new HashSet<>();
    private final Map<UUID, Long> teleportCooldowns = new HashMap<>();

    public AbyssDimensionManager(JGlimsPlugin plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        plugin.getLogger().info("[Abyss] Initializing Abyss dimension...");

        // === Create world_abyss paper-world.yml BEFORE world loads ===
        // This disables the vanilla dragon fight at the config level
        java.io.File worldFolder = new java.io.File(
            Bukkit.getWorldContainer(), WORLD_NAME);
        if (!worldFolder.exists()) {
            worldFolder.mkdirs();
        }
        java.io.File paperWorldYml = new java.io.File(worldFolder, "paper-world.yml");
        if (!paperWorldYml.exists()) {
            try {
                String yaml = "_version: 31\nentities:\n  spawning:\n    scan-for-legacy-ender-dragon: false\n";
                java.nio.file.Files.writeString(paperWorldYml.toPath(), yaml);
                plugin.getLogger().info("[Abyss] Created paper-world.yml with dragon scan disabled");
            } catch (Exception e) {
                plugin.getLogger().warning("[Abyss] Could not create paper-world.yml: " + e.getMessage());
            }
        }

        // === Create the world with THE_END environment ===
        WorldCreator creator = new WorldCreator(WORLD_NAME);
        creator.environment(World.Environment.THE_END);
        creator.generator(new AbyssChunkGenerator());
        creator.generateStructures(false);
        abyssWorld = Bukkit.createWorld(creator);

        if (abyssWorld == null) {
            plugin.getLogger().severe("[Abyss] FAILED to create world_abyss!");
            return;
        }

        // === Disable game rules ===
        abyssWorld.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        abyssWorld.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        abyssWorld.setGameRule(GameRule.DO_FIRE_TICK, false);
        abyssWorld.setGameRule(GameRule.MOB_GRIEFING, false);
        abyssWorld.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        abyssWorld.setGameRule(GameRule.SHOW_DEATH_MESSAGES, false);
        abyssWorld.setDifficulty(Difficulty.HARD);
        abyssWorld.setTime(18000); // midnight
        abyssWorld.setStorm(false);
        abyssWorld.setThundering(false);

        // === CRITICAL: Disable the vanilla DragonBattle ===
        disableVanillaDragonBattle();

        // === Scrub vanilla End features (obsidian altar towers, end crystals,
        // exit portal, gateway) from the main island area. Vanilla End gen
        // builds these regardless of the custom chunk generator, so we nuke
        // them after world load on a short delay. ===
        Bukkit.getScheduler().runTaskLater(plugin, () -> scrubVanillaEndFeatures(abyssWorld), 40L);

        // === Build citadel if fresh world ===
        if (!citadelBuilt) {
            boolean hasBlocks = false;
            // Quick check: is there anything built at the citadel location?
            Block check = abyssWorld.getBlockAt(0, 65, 0);
            if (check.getType() != Material.AIR && check.getType() != Material.END_STONE) {
                hasBlocks = true;
            }
            if (!hasBlocks) {
                plugin.getLogger().info("[Abyss] Building Abyssal Citadel...");
                new AbyssCitadelBuilder(plugin, abyssWorld).build();
                citadelBuilt = true;
            } else {
                citadelBuilt = true;
                plugin.getLogger().info("[Abyss] Citadel already exists, skipping build.");
            }
        }

        // === Repeating task: aggressively remove stray EnderDragons, boss bars,
        //     and end crystals. Runs every 3 seconds (60 ticks) — the vanilla
        //     dragon fight re-spawns the dragon if the fight state isn't fully
        //     dismantled, so we need to be relentless here. ===
        new BukkitRunnable() {
            @Override
            public void run() {
                if (abyssWorld == null) { cancel(); return; }
                disableVanillaDragonBattle();
                abyssWorld.getEntitiesByClass(EnderDragon.class).forEach(dragon -> {
                    dragon.remove();
                });
                // Also remove vanilla end crystals that might be on top of
                // obsidian pillars (some survive the block scrub because
                // they're entities, not blocks).
                abyssWorld.getEntitiesByClass(org.bukkit.entity.EnderCrystal.class).forEach(c -> {
                    if (Math.abs(c.getLocation().getX()) < 80 && Math.abs(c.getLocation().getZ()) < 80) {
                        c.remove();
                    }
                });
            }
        }.runTaskTimer(plugin, 20L, 60L); // first run 1s after init, repeat every 3s

        // === Set the Abyss world spawn to a safe approach position ===
        // The citadel footprint (including porch + pylons) extends from
        // roughly z=-80 to z=~65 with towers up to y=200+. We place spawn at
        // (0, 67, 90) — 25 blocks past the front pylons, at the Abyss's flat
        // ground level. This puts players on open ground looking north at
        // the full cathedral facade — the intended first impression.
        abyssWorld.setSpawnLocation(0, 67, 90);
        plugin.getLogger().info("[Abyss] Spawn set to approach ground (0, 67, 90).");

        // === Start ambient mob spawner ===
        startAmbientSpawner();

        plugin.getLogger().info("[Abyss] Abyss dimension initialized successfully!");
    }

    /**
     * Disables the vanilla DragonBattle and removes its boss bar.
     * This is the fix for the phantom "Ender Dragon" health bar.
     */
    private void disableVanillaDragonBattle() {
        if (abyssWorld == null) return;
        try {
            DragonBattle battle = abyssWorld.getEnderDragonBattle();
            if (battle != null) {
                // Get the boss bar from the battle and remove all players
                org.bukkit.boss.BossBar bar = battle.getBossBar();
                if (bar != null) {
                    bar.setVisible(false);
                    bar.removeAll();
                }
                // Remove the dragon entity if one exists
                EnderDragon dragon = battle.getEnderDragon();
                if (dragon != null) {
                    dragon.remove();
                }
            }
        } catch (Exception e) {
            // Some versions may not support all these methods
            plugin.getLogger().fine("[Abyss] DragonBattle cleanup note: " + e.getMessage());
        }
    }

    /**
     * Hard block on vanilla EnderDragon spawns in the Abyss dimension. The
     * vanilla End respawn mechanic can schedule a dragon to spawn even after
     * the DragonBattle is "disabled", and the 3-second cleanup task lets the
     * dragon exist briefly. This listener intercepts the spawn itself and
     * cancels it before the dragon is added to the world.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onCreatureSpawn(org.bukkit.event.entity.CreatureSpawnEvent event) {
        if (event.getEntityType() != org.bukkit.entity.EntityType.ENDER_DRAGON) return;
        if (event.getLocation().getWorld() == null) return;
        if (!WORLD_NAME.equals(event.getLocation().getWorld().getName())) return;
        event.setCancelled(true);
        plugin.getLogger().info("[Abyss] Cancelled vanilla EnderDragon spawn attempt.");
    }

    /**
     * Removes all vanilla End features from the main-island area of the
     * Abyss: obsidian altar towers, end crystals (cages + crystals), the
     * exit portal block, and any stray end-gateway blocks. The Abyss uses
     * {@code World.Environment.THE_END}, so vanilla generation places
     * these regardless of the custom chunk generator.
     *
     * <p>Scan radius: 80 blocks from the main island spawn, Y 30-120.
     * That is large enough to cover all 10 vanilla obsidian pillars
     * without clipping into the Abyssal Citadel which lives further out.
     */
    private void scrubVanillaEndFeatures(World abyss) {
        if (abyss == null) return;
        plugin.getLogger().info("[Abyss] Scrubbing vanilla End altar/crystals/gateways...");
        int removed = 0;
        // Remove block-level End altar features
        for (int x = -80; x <= 80; x++) {
            for (int z = -80; z <= 80; z++) {
                // Skip blocks under the citadel footprint — the citadel may have
                // legitimate obsidian in its construction at z=~0 or higher.
                // The vanilla altar towers center around (0, ~76, 0) with a
                // 43-block ring. We clear only the tower footprints.
                double dist = Math.sqrt(x * x + z * z);
                if (dist > 50) continue;
                for (int y = 30; y <= 120; y++) {
                    Block b = abyss.getBlockAt(x, y, z);
                    Material mat = b.getType();
                    if (mat == Material.OBSIDIAN ||
                        mat == Material.BEDROCK ||
                        mat == Material.END_STONE ||
                        mat == Material.IRON_BARS ||
                        mat == Material.END_GATEWAY ||
                        mat == Material.END_PORTAL ||
                        mat == Material.END_CRYSTAL) {
                        // Only remove if NOT inside the citadel footprint
                        // (citadel occupies a roughly 200x250 box between
                        // z=-150..70, x=-70..70 at sY~70). The vanilla altar
                        // is a small feature right around world spawn.
                        // We use a tight radius-30 sphere to be safe.
                        double d = Math.sqrt(x * x + z * z);
                        if (d <= 30) {
                            b.setType(Material.AIR, false);
                            removed++;
                        }
                    }
                }
            }
        }
        // Remove End Crystal entities within the scrub region
        int crystalCount = 0;
        for (org.bukkit.entity.EnderCrystal c :
                abyss.getEntitiesByClass(org.bukkit.entity.EnderCrystal.class)) {
            Location loc = c.getLocation();
            if (Math.abs(loc.getX()) < 80 && Math.abs(loc.getZ()) < 80) {
                c.remove();
                crystalCount++;
            }
        }
        plugin.getLogger().info("[Abyss] Scrubbed " + removed
                + " vanilla blocks and " + crystalCount + " end crystals.");
    }

    private void startAmbientSpawner() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (abyssWorld == null) { cancel(); return; }
                for (Player p : abyssWorld.getPlayers()) {
                    // Don't spawn ambient mobs near the arena
                    if (Math.abs(p.getLocation().getZ() - ARENA_CENTER_Z) < 50) continue;

                    int count = (int) abyssWorld.getEntities().stream()
                        .filter(e -> e.getType() == EntityType.ENDERMAN ||
                                     e.getType() == EntityType.WITHER_SKELETON)
                        .count();
                    if (count >= 12) continue;

                    Location spawn = p.getLocation().add(
                        (Math.random() - 0.5) * 60,
                        0,
                        (Math.random() - 0.5) * 60
                    );
                    spawn.setY(findSafeY(spawn));

                    if (Math.random() < 0.6) {
                        var mob = abyssWorld.spawnEntity(spawn, EntityType.ENDERMAN);
                        mob.customName(Component.text("Void Wanderer", NamedTextColor.DARK_PURPLE));
                    } else {
                        var mob = abyssWorld.spawnEntity(spawn, EntityType.WITHER_SKELETON);
                        mob.customName(Component.text("Abyssal Sentinel", NamedTextColor.DARK_RED));
                    }
                }
            }
        }.runTaskTimer(plugin, 400L, 400L + (long)(Math.random() * 200));
    }

    // ==================== ABYSSAL KEY ====================

    public ItemStack createAbyssalKey() {
        ItemStack key = new ItemStack(Material.ECHO_SHARD);
        ItemMeta meta = key.getItemMeta();
        meta.displayName(Component.text("Abyssal Key", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD));
        meta.lore(List.of(
            Component.text("A key forged in the void", NamedTextColor.GRAY),
            Component.text("Right-click a Purpur frame to open", NamedTextColor.DARK_GRAY),
            Component.text("the gateway to the Abyss", NamedTextColor.DARK_GRAY)
        ));
        meta.setCustomModelData(9001);
        meta.getPersistentDataContainer().set(
            new NamespacedKey(plugin, "abyssal_key"),
            PersistentDataType.BYTE, (byte) 1
        );
        key.setItemMeta(meta);
        return key;
    }

    public boolean isAbyssalKey(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(
            new NamespacedKey(plugin, "abyssal_key"), PersistentDataType.BYTE
        );
    }

    // ==================== PORTAL INTERACTION ====================

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;
        if (event.getClickedBlock().getType() != Material.PURPUR_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (!isAbyssalKey(hand)) return;

        // Don't open portals inside the Abyss
        if (player.getWorld().getName().equals(WORLD_NAME)) return;

        Block clicked = event.getClickedBlock();
        plugin.getLogger().info("[Abyss] Player " + player.getName() +
            " used Abyssal Key on purpur at " + clicked.getLocation());

        // Try to find a 4-wide x 5-tall portal frame
        Location frameLoc = findPortalFrame(clicked);
        if (frameLoc != null) {
            // Consume key
            hand.setAmount(hand.getAmount() - 1);
            activatePortal(frameLoc, player);
            event.setCancelled(true);
        } else {
            player.sendMessage(Component.text(
                "The key resonates, but no valid portal frame is nearby...",
                NamedTextColor.DARK_PURPLE));
        }
    }

    /**
     * Searches for a valid 4-wide x 5-tall purpur portal frame
     * around the clicked block. The frame is:
     *   - 4 blocks wide (X or Z axis)
     *   - 5 blocks tall (Y axis)
     *   - Inner opening is 2 wide x 3 tall
     */
    private Location findPortalFrame(Block clicked) {
        World world = clicked.getWorld();
        int cx = clicked.getX(), cy = clicked.getY(), cz = clicked.getZ();

        // Search in a small radius for potential bottom-left corners
        for (int dx = -4; dx <= 4; dx++) {
            for (int dy = -5; dy <= 0; dy++) {
                for (int dz = -4; dz <= 4; dz++) {
                    int bx = cx + dx, by = cy + dy, bz = cz + dz;

                    // Try X-axis frame (portal faces along Z)
                    if (isValidFrame(world, bx, by, bz, true)) {
                        return new Location(world, bx, by, bz);
                    }
                    // Try Z-axis frame (portal faces along X)
                    if (isValidFrame(world, bx, by, bz, false)) {
                        return new Location(world, bx, by, bz);
                    }
                }
            }
        }
        return null;
    }

    private boolean isValidFrame(World world, int bx, int by, int bz, boolean xAxis) {
        // Frame shape (4 wide x 5 tall, inner 2x3):
        //  P P P P    (top row, y+4)
        //  P . . P    (y+3)
        //  P . . P    (y+2)
        //  P . . P    (y+1)
        //  P P P P    (bottom row, y+0)
        for (int w = 0; w < 4; w++) {
            for (int h = 0; h < 5; h++) {
                int x = bx + (xAxis ? w : 0);
                int z = bz + (xAxis ? 0 : w);
                int y = by + h;

                boolean isEdge = (w == 0 || w == 3 || h == 0 || h == 4);
                Block b = world.getBlockAt(x, y, z);

                if (isEdge) {
                    if (b.getType() != Material.PURPUR_BLOCK &&
                        b.getType() != Material.PURPUR_PILLAR) {
                        return false;
                    }
                }
                // Inner blocks should be air (or will be replaced)
            }
        }
        return true;
    }

    private void activatePortal(Location frameLoc, Player player) {
        World world = frameLoc.getWorld();
        int bx = (int) frameLoc.getX();
        int by = (int) frameLoc.getY();
        int bz = (int) frameLoc.getZ();

        // Determine axis by checking which direction the frame extends
        boolean xAxis = (world.getBlockAt(bx + 1, by, bz).getType() == Material.PURPUR_BLOCK ||
                          world.getBlockAt(bx + 1, by, bz).getType() == Material.PURPUR_PILLAR);

        // Fill inner 2x3 with END_GATEWAY
        Set<Location> newPortals = new HashSet<>();
        for (int w = 1; w <= 2; w++) {
            for (int h = 1; h <= 3; h++) {
                int x = bx + (xAxis ? w : 0);
                int z = bz + (xAxis ? 0 : w);
                int y = by + h;

                Block b = world.getBlockAt(x, y, z);
                b.setType(Material.END_GATEWAY);
                Location loc = b.getLocation();
                newPortals.add(loc);
                activePortalBlocks.add(loc);
            }
        }

        // Effects
        Location center = new Location(world,
            bx + (xAxis ? 2 : 0.5), by + 2.5, bz + (xAxis ? 0.5 : 2));
        world.playSound(center, Sound.BLOCK_END_PORTAL_SPAWN, 1.0f, 0.5f);
        world.spawnParticle(Particle.PORTAL, center, 100, 1, 1, 1, 0.5);
        world.spawnParticle(Particle.REVERSE_PORTAL, center, 50, 1, 1, 1, 0.1);

        player.sendMessage(Component.text(
            "The Abyss Gateway opens... step through if you dare.",
            NamedTextColor.DARK_PURPLE, TextDecoration.ITALIC));

        // Remove gateway after 30 seconds
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                ticks += 20;
                if (ticks >= 600) { // 30 seconds
                    for (Location loc : newPortals) {
                        if (loc.getBlock().getType() == Material.END_GATEWAY) {
                            loc.getBlock().setType(Material.AIR);
                        }
                        activePortalBlocks.remove(loc);
                    }
                    cancel();
                } else {
                    // Periodic particles
                    world.spawnParticle(Particle.PORTAL, center, 20, 0.5, 0.5, 0.5, 0.3);
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    // ==================== PORTAL TELEPORTATION ====================

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockY() == event.getTo().getBlockY() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;

        Player player = event.getPlayer();
        if (player.getWorld().getName().equals(WORLD_NAME)) return;

        Location to = event.getTo();
        Location blockLoc = new Location(to.getWorld(),
            to.getBlockX(), to.getBlockY(), to.getBlockZ());

        if (!activePortalBlocks.contains(blockLoc)) return;

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        if (teleportCooldowns.containsKey(uuid) &&
            now - teleportCooldowns.get(uuid) < 5000) return;
        teleportCooldowns.put(uuid, now);

        teleportToAbyss(player);
    }

    public void teleportToAbyss(Player player) {
        if (abyssWorld == null) {
            player.sendMessage(Component.text("The Abyss is not ready.", NamedTextColor.RED));
            return;
        }

        // Teleport to south of citadel, facing north toward arena
        double safeY = findSafeY(new Location(abyssWorld, 0.5, 80, 115.5));
        Location dest = new Location(abyssWorld, 0.5, safeY, 115.5, 180f, 0f);

        player.teleport(dest);

        // Title
        player.showTitle(Title.title(
            Component.text("THE ABYSS", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD),
            Component.text("Enter at your own peril", NamedTextColor.GRAY),
            Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(1000))
        ));

        // Sounds
        player.playSound(dest, Sound.AMBIENT_CAVE, 1.0f, 0.5f);
        player.playSound(dest, Sound.ENTITY_ENDER_DRAGON_AMBIENT, 0.3f, 0.5f);

        // Clean up any stray vanilla dragons after a short delay
        new BukkitRunnable() {
            @Override
            public void run() {
                disableVanillaDragonBattle();
                abyssWorld.getEntitiesByClass(EnderDragon.class).forEach(EnderDragon::remove);
            }
        }.runTaskLater(plugin, 40L);

        plugin.getLogger().info("[Abyss] " + player.getName() + " teleported to the Abyss");
    }

    public double findSafeY(Location loc) {
        World w = loc.getWorld();
        if (w == null) return 80;
        for (int y = 120; y > 30; y--) {
            Block b = w.getBlockAt(loc.getBlockX(), y, loc.getBlockZ());
            Block above1 = w.getBlockAt(loc.getBlockX(), y + 1, loc.getBlockZ());
            Block above2 = w.getBlockAt(loc.getBlockX(), y + 2, loc.getBlockZ());
            if (b.getType().isSolid() &&
                above1.getType().isAir() && above2.getType().isAir()) {
                return y + 1.0;
            }
        }
        return 80;
    }

    // ==================== GETTERS ====================

    public World getAbyssWorld() { return abyssWorld; }
    public JGlimsPlugin getPlugin() { return plugin; }
    public boolean isCitadelBuilt() { return citadelBuilt; }

    /** Check if a world is the Abyss — use this from other managers to filter */
    public static boolean isAbyssWorld(World world) {
        return world != null && WORLD_NAME.equals(world.getName());
    }
}
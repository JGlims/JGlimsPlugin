package com.jglims.plugin.abyss;

import com.jglims.plugin.JGlimsPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Orientable;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class AbyssDimensionManager implements Listener {

    private static final String ABYSS_WORLD_NAME = "world_abyss";
    private static final String KEY_ABYSSAL_KEY = "abyssal_key";

    private final JGlimsPlugin plugin;
    private World abyssWorld;
    private boolean citadelBuilt = false;

    // Portal tracking: world UUID -> set of portal block locations
    private final Map<UUID, Set<Location>> activePortals = new HashMap<>();

    public AbyssDimensionManager(JGlimsPlugin plugin) {
        this.plugin = plugin;
    }

    // ── World creation / loading ──────────────────────────────

    public void initialize() {
        abyssWorld = Bukkit.getWorld(ABYSS_WORLD_NAME);
        boolean freshWorld = (abyssWorld == null);

        if (freshWorld) {
            plugin.getLogger().info("[Abyss] Creating dimension: " + ABYSS_WORLD_NAME);
            WorldCreator creator = new WorldCreator(ABYSS_WORLD_NAME);
            creator.environment(World.Environment.THE_END);
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

            // Remove vanilla ender dragons every 5 seconds
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (abyssWorld == null) return;
                    for (EnderDragon dragon : abyssWorld.getEntitiesByClass(EnderDragon.class)) {
                        if (dragon.getCustomName() == null) {
                            dragon.remove();
                        }
                    }
                }
            }.runTaskTimer(plugin, 100L, 100L);

            if (freshWorld) {
                plugin.getLogger().info("[Abyss] Building Abyssal Citadel on fresh world...");
                new AbyssCitadelBuilder(plugin, abyssWorld).build();
                citadelBuilt = true;
                plugin.getLogger().info("[Abyss] Citadel construction complete.");
            } else {
                citadelBuilt = true;
            }
        }
    }

    // ── Abyssal Key item ──────────────────────────────────────

    public ItemStack createAbyssalKey() {
        ItemStack key = new ItemStack(Material.TRIPWIRE_HOOK, 1);
        ItemMeta meta = key.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Abyssal Key", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("A key forged in the void.", NamedTextColor.GRAY));
            lore.add(Component.text("Use on a Purpur portal frame", NamedTextColor.GRAY));
            lore.add(Component.text("to open a gateway to the Abyss.", NamedTextColor.DARK_GRAY));
            lore.add(Component.empty());
            lore.add(Component.text("\u2737 Abyssal Key", NamedTextColor.DARK_PURPLE));
            meta.setCustomModelData(9001);
            key.setItemMeta(meta);
        }
        return key;
    }

    private boolean isAbyssalKey(ItemStack item) {
        if (item == null || item.getType() != Material.TRIPWIRE_HOOK) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasCustomModelData()) return false;
        return meta.getCustomModelData() == 9001;
    }

    // ── Portal creation via right-click ───────────────────────

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

        // Consume one key
        if (hand.getAmount() > 1) {
            hand.setAmount(hand.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }

        activatePortal(frameLoc, player);
    }

    private Location findPortalFrame(Location clickedLoc) {
        World w = clickedLoc.getWorld();
        int cx = clickedLoc.getBlockX();
        int cy = clickedLoc.getBlockY();
        int cz = clickedLoc.getBlockZ();

        for (int ox = -4; ox <= 1; ox++) {
            for (int oy = -5; oy <= 0; oy++) {
                Location base = new Location(w, cx + ox, cy + oy, cz);
                if (isCompleteFrameX(base)) return base;
            }
        }
        for (int oz = -4; oz <= 1; oz++) {
            for (int oy = -5; oy <= 0; oy++) {
                Location base = new Location(w, cx, cy + oy, cz + oz);
                if (isCompleteFrameZ(base)) return base;
            }
        }
        return null;
    }

    private boolean isCompleteFrameX(Location base) {
        World w = base.getWorld();
        int bx = base.getBlockX(), by = base.getBlockY(), bz = base.getBlockZ();
        for (int x = 0; x < 4; x++) {
            if (w.getBlockAt(bx + x, by, bz).getType() != Material.PURPUR_BLOCK) return false;
            if (w.getBlockAt(bx + x, by + 4, bz).getType() != Material.PURPUR_BLOCK) return false;
        }
        for (int y = 0; y < 5; y++) {
            if (w.getBlockAt(bx, by + y, bz).getType() != Material.PURPUR_BLOCK) return false;
            if (w.getBlockAt(bx + 3, by + y, bz).getType() != Material.PURPUR_BLOCK) return false;
        }
        for (int x = 1; x <= 2; x++) {
            for (int y = 1; y <= 3; y++) {
                Material m = w.getBlockAt(bx + x, by + y, bz).getType();
                if (m != Material.AIR && m != Material.CAVE_AIR && m != Material.NETHER_PORTAL) return false;
            }
        }
        return true;
    }

    private boolean isCompleteFrameZ(Location base) {
        World w = base.getWorld();
        int bx = base.getBlockX(), by = base.getBlockY(), bz = base.getBlockZ();
        for (int z = 0; z < 4; z++) {
            if (w.getBlockAt(bx, by, bz + z).getType() != Material.PURPUR_BLOCK) return false;
            if (w.getBlockAt(bx, by + 4, bz + z).getType() != Material.PURPUR_BLOCK) return false;
        }
        for (int y = 0; y < 5; y++) {
            if (w.getBlockAt(bx, by + y, bz).getType() != Material.PURPUR_BLOCK) return false;
            if (w.getBlockAt(bx, by + y, bz + 3).getType() != Material.PURPUR_BLOCK) return false;
        }
        for (int z = 1; z <= 2; z++) {
            for (int y = 1; y <= 3; y++) {
                Material m = w.getBlockAt(bx, by + y, bz + z).getType();
                if (m != Material.AIR && m != Material.CAVE_AIR && m != Material.NETHER_PORTAL) return false;
            }
        }
        return true;
    }

    private void activatePortal(Location frameLoc, Player player) {
        World w = frameLoc.getWorld();
        int bx = frameLoc.getBlockX(), by = frameLoc.getBlockY(), bz = frameLoc.getBlockZ();
        boolean xAligned = isCompleteFrameX(frameLoc);

        Set<Location> portalBlocks = new HashSet<>();

        if (xAligned) {
            for (int x = 1; x <= 2; x++) {
                for (int y = 1; y <= 3; y++) {
                    Block b = w.getBlockAt(bx + x, by + y, bz);
                    b.setType(Material.NETHER_PORTAL, false);
                    org.bukkit.block.data.BlockData data = b.getBlockData();
                    if (data instanceof Orientable) {
                        Orientable orientable = (Orientable) data;
                        orientable.setAxis(Axis.X);
                        b.setBlockData(orientable, false);
                    }
                    portalBlocks.add(b.getLocation().toBlockLocation());
                }
            }
        } else {
            for (int z = 1; z <= 2; z++) {
                for (int y = 1; y <= 3; y++) {
                    Block b = w.getBlockAt(bx, by + y, bz + z);
                    b.setType(Material.NETHER_PORTAL, false);
                    org.bukkit.block.data.BlockData data = b.getBlockData();
                    if (data instanceof Orientable) {
                        Orientable orientable = (Orientable) data;
                        orientable.setAxis(Axis.Z);
                        b.setBlockData(orientable, false);
                    }
                    portalBlocks.add(b.getLocation().toBlockLocation());
                }
            }
        }

        UUID worldId = w.getUID();
        activePortals.computeIfAbsent(worldId, k -> new HashSet<>()).addAll(portalBlocks);

        Location center = frameLoc.clone().add(xAligned ? 1.5 : 0.5, 2.5, xAligned ? 0.5 : 1.5);
        w.playSound(center, Sound.BLOCK_PORTAL_TRIGGER, 1.5f, 0.5f);
        w.spawnParticle(Particle.PORTAL, center, 200, 1.0, 2.0, 1.0, 0.5);
        w.spawnParticle(Particle.REVERSE_PORTAL, center, 100, 1.0, 2.0, 1.0, 0.3);

        player.sendMessage(Component.text("The Abyssal Gateway opens...", NamedTextColor.DARK_PURPLE, TextDecoration.ITALIC));

        // Auto-close after 30 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Location loc : portalBlocks) {
                    Block b = loc.getBlock();
                    if (b.getType() == Material.NETHER_PORTAL) {
                        b.setType(Material.AIR);
                    }
                }
                Set<Location> set = activePortals.get(worldId);
                if (set != null) {
                    set.removeAll(portalBlocks);
                }
                w.playSound(center, Sound.BLOCK_PORTAL_TRAVEL, 0.8f, 1.5f);
            }
        }.runTaskLater(plugin, 600L);
    }

    // ── Portal walk-in teleport ───────────────────────────────

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getTo() == null) return;
        Player player = event.getPlayer();
        Block block = event.getTo().getBlock();

        if (block.getType() != Material.NETHER_PORTAL) return;
        Location blockLoc = block.getLocation().toBlockLocation();

        UUID worldId = player.getWorld().getUID();
        Set<Location> portals = activePortals.get(worldId);
        if (portals == null || !portals.contains(blockLoc)) return;

        if (player.getWorld().getName().equals(ABYSS_WORLD_NAME)) return;

        teleportToAbyss(player);
    }

    public void teleportToAbyss(Player player) {
        if (abyssWorld == null) {
            player.sendMessage(Component.text("The Abyss is not yet ready.", NamedTextColor.RED));
            return;
        }

        int safeY = findSafeY(abyssWorld, 0, 85);
        Location destination = new Location(abyssWorld, 0.5, safeY, 85.5, 0f, 0f);
        player.teleport(destination);

        Title.Times times = Title.Times.times(
                Duration.ofMillis(500),
                Duration.ofSeconds(3),
                Duration.ofMillis(1000)
        );
        Title title = Title.title(
                Component.text("THE ABYSS", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD),
                Component.text("Abandon all hope...", NamedTextColor.GRAY, TextDecoration.ITALIC),
                times
        );
        player.showTitle(title);

        player.playSound(player.getLocation(), Sound.AMBIENT_CAVE, 1.0f, 0.5f);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_AMBIENT, 0.5f, 0.5f);

        // Remove vanilla dragons after teleport
        new BukkitRunnable() {
            @Override
            public void run() {
                if (abyssWorld == null) return;
                for (EnderDragon dragon : abyssWorld.getEntitiesByClass(EnderDragon.class)) {
                    if (dragon.getCustomName() == null) {
                        dragon.remove();
                    }
                }
            }
        }.runTaskLater(plugin, 40L);
    }

    // ── Helpers ───────────────────────────────────────────────

    public int findSafeY(World world, int x, int z) {
        for (int y = 120; y > 1; y--) {
            Block block = world.getBlockAt(x, y, z);
            Block above = world.getBlockAt(x, y + 1, z);
            Block above2 = world.getBlockAt(x, y + 2, z);
            if (block.getType().isSolid()
                    && !above.getType().isSolid()
                    && !above2.getType().isSolid()) {
                return y + 1;
            }
        }
        return 60;
    }

    public World getAbyssWorld() { return abyssWorld; }
    public JGlimsPlugin getPlugin() { return plugin; }
    public boolean isCitadelBuilt() { return citadelBuilt; }
}
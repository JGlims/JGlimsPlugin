package com.jglims.plugin.abyss;

import com.jglims.plugin.JGlimsPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the Abyss custom dimension with the Abyssal Citadel.
 * 
 * FIXES in Phase 3:
 * - Portal now fills interior with END_GATEWAY blocks (visible swirl)
 * - Teleport lands on the gate PATH outside the castle (z=85), not on towers
 * - Vanilla Ender Dragon suppressed with repeating task + GameRule
 * - Safe Y scan from top down instead of getHighestBlockYAt
 */
public class AbyssDimensionManager implements Listener {

    private final JGlimsPlugin plugin;
    private final NamespacedKey KEY_ABYSSAL_KEY;
    private World abyssWorld;
    private boolean citadelBuilt = false;

    public static final String ABYSS_WORLD_NAME = "world_abyss";
    private static final boolean DEBUG = true;

    public AbyssDimensionManager(JGlimsPlugin plugin) {
        this.plugin = plugin;
        this.KEY_ABYSSAL_KEY = new NamespacedKey(plugin, "abyssal_key");
    }

    public void initialize() {
        abyssWorld = plugin.getServer().getWorld(ABYSS_WORLD_NAME);
        boolean freshWorld = false;

        if (abyssWorld == null) {
            plugin.getLogger().info("[Abyss] Creating Abyss dimension...");
            WorldCreator creator = new WorldCreator(ABYSS_WORLD_NAME);
            creator.environment(World.Environment.THE_END);
            creator.generator(new AbyssChunkGenerator());
            creator.generateStructures(false);
            abyssWorld = creator.createWorld();
            freshWorld = true;
        }

        if (abyssWorld == null) {
            plugin.getLogger().severe("[Abyss] FAILED to create/load Abyss world!");
            return;
        }

        // Configure world
        abyssWorld.setDifficulty(Difficulty.HARD);
        abyssWorld.setSpawnLocation(0, 66, 0);
        abyssWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        abyssWorld.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        abyssWorld.setGameRule(GameRule.DO_MOB_SPAWNING, true);
        abyssWorld.setGameRule(GameRule.KEEP_INVENTORY, false);
        abyssWorld.setTime(18000);

        // Remove vanilla dragons immediately
        removeVanillaDragons();

        // Repeating task: kill any vanilla dragon every 5 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                if (abyssWorld == null) { cancel(); return; }
                for (EnderDragon dragon : abyssWorld.getEntitiesByClass(EnderDragon.class)) {
                    if (!dragon.getPersistentDataContainer().has(
                            new NamespacedKey(plugin, "abyss_dragon"), PersistentDataType.BYTE)) {
                        dragon.remove();
                    }
                }
            }
        }.runTaskTimer(plugin, 100L, 100L); // every 5 seconds

        // Build citadel on fresh worlds
        if (freshWorld) {
            plugin.getLogger().info("[Abyss] Building Abyssal Citadel (Phase 3 mega build)...");
            try {
                new AbyssCitadelBuilder(plugin).buildCitadel(abyssWorld);
                citadelBuilt = true;
                plugin.getLogger().info("[Abyss] Citadel construction complete!");
            } catch (Exception e) {
                plugin.getLogger().severe("[Abyss] Citadel build FAILED: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            citadelBuilt = true;
            plugin.getLogger().info("[Abyss] Abyss dimension loaded (citadel exists).");
        }

        plugin.getLogger().info("[Abyss] Abyss dimension ready: " + ABYSS_WORLD_NAME);
    }

    private void removeVanillaDragons() {
        if (abyssWorld == null) return;
        int removed = 0;
        for (EnderDragon dragon : abyssWorld.getEntitiesByClass(EnderDragon.class)) {
            if (!dragon.getPersistentDataContainer().has(
                    new NamespacedKey(plugin, "abyss_dragon"), PersistentDataType.BYTE)) {
                dragon.remove();
                removed++;
            }
        }
        if (removed > 0) plugin.getLogger().info("[Abyss] Removed " + removed + " vanilla Ender Dragon(s).");
    }

    /** Find a safe Y at the given XZ by scanning downward from y=120. */
    private int findSafeY(int x, int z) {
        if (abyssWorld == null) return 60;
        for (int y = 120; y > 30; y--) {
            Material below = abyssWorld.getBlockAt(x, y - 1, z).getType();
            Material at = abyssWorld.getBlockAt(x, y, z).getType();
            Material above = abyssWorld.getBlockAt(x, y + 1, z).getType();
            if (below.isSolid() && !at.isSolid() && !above.isSolid()) {
                return y;
            }
        }
        return 60;
    }

    // ── Abyssal Key ──
    public ItemStack createAbyssalKey() {
        ItemStack key = new ItemStack(Material.ECHO_SHARD, 1);
        ItemMeta meta = key.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Abyssal Key", TextColor.color(170, 0, 0))
                    .decorate(TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("A key forged from the Dragon's essence", NamedTextColor.DARK_GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());
            lore.add(Component.text("Right-click any Purpur Block in a", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("4x5 portal frame to open the Abyss.", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());
            lore.add(Component.text("Portal: 4 wide x 5 tall Purpur frame", NamedTextColor.DARK_PURPLE)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("(same shape as a Nether portal)", NamedTextColor.DARK_PURPLE)
                    .decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
            meta.getPersistentDataContainer().set(KEY_ABYSSAL_KEY, PersistentDataType.BYTE, (byte) 1);
            key.setItemMeta(meta);
        }
        return key;
    }

    public boolean isAbyssalKey(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer()
                .getOrDefault(KEY_ABYSSAL_KEY, PersistentDataType.BYTE, (byte) 0) == 1;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (!isAbyssalKey(hand)) return;

        Block clicked = event.getClickedBlock();
        if (clicked == null || clicked.getType() != Material.PURPUR_BLOCK) return;

        if (DEBUG) plugin.getLogger().info("[Abyss] " + player.getName()
                + " right-clicked purpur at " + clicked.getX() + "," + clicked.getY() + "," + clicked.getZ());

        // Try to find and activate a portal frame
        int[] frameOrigin = findPortalFrame(clicked);
        if (frameOrigin != null) {
            event.setCancelled(true);
            if (DEBUG) plugin.getLogger().info("[Abyss] Valid portal frame found at origin ("
                    + frameOrigin[0] + "," + frameOrigin[1] + "," + frameOrigin[2] + ") orient=" + frameOrigin[3]);
            activateAbyssPortal(player, clicked, frameOrigin);
        } else {
            if (DEBUG) plugin.getLogger().info("[Abyss] No valid portal frame around clicked block.");
            player.sendMessage(Component.text("No valid Purpur portal frame detected.", NamedTextColor.RED));
            player.sendMessage(Component.text("Build a 4 wide x 5 tall frame (Nether portal shape).", NamedTextColor.GRAY));
        }
    }

    /**
     * Scans around the clicked block to find a valid 4x5 purpur frame.
     * Returns {ox, oy, oz, orientation} or null. orientation: 0=X-aligned, 1=Z-aligned.
     */
    private int[] findPortalFrame(Block block) {
        int bx = block.getX(), by = block.getY(), bz = block.getZ();
        World world = block.getWorld();

        for (int dy = -4; dy <= 0; dy++) {
            int oy = by + dy;
            // X-aligned frame (portal faces Z)
            for (int dx = -3; dx <= 0; dx++) {
                if (isCompleteFrameX(world, bx + dx, oy, bz)) {
                    return new int[]{bx + dx, oy, bz, 0};
                }
            }
            // Z-aligned frame (portal faces X)
            for (int dz = -3; dz <= 0; dz++) {
                if (isCompleteFrameZ(world, bx, oy, bz + dz)) {
                    return new int[]{bx, oy, bz + dz, 1};
                }
            }
        }
        return null;
    }

    private boolean isCompleteFrameX(World world, int ox, int oy, int oz) {
        // Bottom and top bars (4 blocks)
        for (int x = 0; x < 4; x++) {
            if (world.getBlockAt(ox + x, oy, oz).getType() != Material.PURPUR_BLOCK) return false;
            if (world.getBlockAt(ox + x, oy + 4, oz).getType() != Material.PURPUR_BLOCK) return false;
        }
        // Side pillars (3 blocks each)
        for (int y = 1; y <= 3; y++) {
            if (world.getBlockAt(ox, oy + y, oz).getType() != Material.PURPUR_BLOCK) return false;
            if (world.getBlockAt(ox + 3, oy + y, oz).getType() != Material.PURPUR_BLOCK) return false;
        }
        // Interior must be air (2 wide x 3 tall)
        for (int x = 1; x <= 2; x++) {
            for (int y = 1; y <= 3; y++) {
                Material mat = world.getBlockAt(ox + x, oy + y, oz).getType();
                if (mat != Material.AIR && mat != Material.CAVE_AIR && mat != Material.VOID_AIR
                        && mat != Material.END_GATEWAY) return false;
            }
        }
        return true;
    }

    private boolean isCompleteFrameZ(World world, int ox, int oy, int oz) {
        for (int z = 0; z < 4; z++) {
            if (world.getBlockAt(ox, oy, oz + z).getType() != Material.PURPUR_BLOCK) return false;
            if (world.getBlockAt(ox, oy + 4, oz + z).getType() != Material.PURPUR_BLOCK) return false;
        }
        for (int y = 1; y <= 3; y++) {
            if (world.getBlockAt(ox, oy + y, oz).getType() != Material.PURPUR_BLOCK) return false;
            if (world.getBlockAt(ox, oy + y, oz + 3).getType() != Material.PURPUR_BLOCK) return false;
        }
        for (int z = 1; z <= 2; z++) {
            for (int y = 1; y <= 3; y++) {
                Material mat = world.getBlockAt(ox, oy + y, oz + z).getType();
                if (mat != Material.AIR && mat != Material.CAVE_AIR && mat != Material.VOID_AIR
                        && mat != Material.END_GATEWAY) return false;
            }
        }
        return true;
    }

    private void activateAbyssPortal(Player player, Block clicked, int[] frame) {
        if (abyssWorld == null) {
            player.sendMessage(Component.text("The Abyss dimension is not available!", NamedTextColor.RED));
            return;
        }

        // Consume one key
        ItemStack hand = player.getInventory().getItemInMainHand();
        hand.setAmount(hand.getAmount() - 1);

        int ox = frame[0], oy = frame[1], oz = frame[2], orient = frame[3];
        World world = clicked.getWorld();

        // FILL INTERIOR WITH END_GATEWAY BLOCKS (visible swirling portal!)
        for (int y = 1; y <= 3; y++) {
            if (orient == 0) {
                // X-aligned: fill x+1, x+2 at fixed z
                world.getBlockAt(ox + 1, oy + y, oz).setType(Material.END_GATEWAY);
                world.getBlockAt(ox + 2, oy + y, oz).setType(Material.END_GATEWAY);
            } else {
                // Z-aligned: fill z+1, z+2 at fixed x
                world.getBlockAt(ox, oy + y, oz + 1).setType(Material.END_GATEWAY);
                world.getBlockAt(ox, oy + y, oz + 2).setType(Material.END_GATEWAY);
            }
        }

        // VFX at the portal center
        Location portalCenter = clicked.getLocation().add(0.5, 1.5, 0.5);
        world.spawnParticle(Particle.REVERSE_PORTAL, portalCenter, 80, 1.5, 2.5, 1.5, 0.5);
        world.spawnParticle(Particle.DRAGON_BREATH, portalCenter, 50, 1.5, 2.0, 1.5, 0.1);
        world.spawnParticle(Particle.WITCH, portalCenter, 40, 1.5, 2.5, 1.5, 0.3);
        world.spawnParticle(Particle.END_ROD, portalCenter, 30, 1.0, 2.0, 1.0, 0.2);
        world.playSound(portalCenter, Sound.BLOCK_END_PORTAL_SPAWN, 1.5f, 0.5f);
        world.playSound(portalCenter, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.3f);

        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("  \u2726 ", TextColor.color(170, 0, 0))
                .append(Component.text("THE ABYSS AWAITS...", TextColor.color(170, 0, 0)).decorate(TextDecoration.BOLD)));
        player.sendMessage(Component.empty());

        player.showTitle(net.kyori.adventure.title.Title.title(
                Component.text("\u2620 ENTERING THE ABYSS \u2620", TextColor.color(170, 0, 0)).decorate(TextDecoration.BOLD),
                Component.text("Prepare yourself...", NamedTextColor.DARK_GRAY),
                net.kyori.adventure.title.Title.Times.times(
                        java.time.Duration.ofMillis(500), java.time.Duration.ofSeconds(2), java.time.Duration.ofMillis(1000))));

        // Delayed teleport — land on the path OUTSIDE the south gate
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            // Path is at z=85 (outer wall gate at z=70, path extends to z=85)
            int safeY = findSafeY(0, 85);
            Location dest = new Location(abyssWorld, 0.5, safeY, 85.5);
            dest.setYaw(0); // face north (toward the gate)

            if (DEBUG) plugin.getLogger().info("[Abyss] Teleporting " + player.getName()
                    + " to gate path: 0, " + safeY + ", 85");

            player.teleport(dest);
            player.playSound(player.getLocation(), Sound.BLOCK_END_PORTAL_SPAWN, 0.8f, 0.7f);
            player.sendMessage(Component.text("  You have entered the Abyss.", TextColor.color(170, 0, 0)));

            // Remove vanilla dragons
            removeVanillaDragons();
        }, 40L);

        // Auto-close portal after 30 seconds
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            for (int y = 1; y <= 3; y++) {
                if (orient == 0) {
                    world.getBlockAt(ox + 1, oy + y, oz).setType(Material.AIR);
                    world.getBlockAt(ox + 2, oy + y, oz).setType(Material.AIR);
                } else {
                    world.getBlockAt(ox, oy + y, oz + 1).setType(Material.AIR);
                    world.getBlockAt(ox, oy + y, oz + 2).setType(Material.AIR);
                }
            }
            if (DEBUG) plugin.getLogger().info("[Abyss] Portal closed after 30s.");
        }, 600L);
    }

    // Getters
    public World getAbyssWorld() { return abyssWorld; }
    public NamespacedKey getKeyAbyssalKey() { return KEY_ABYSSAL_KEY; }
    public JGlimsPlugin getPlugin() { return plugin; }
    public boolean isCitadelBuilt() { return citadelBuilt; }
}
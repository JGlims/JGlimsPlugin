package com.jglims.plugin.abyss;

import com.jglims.plugin.JGlimsPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.block.Block;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the Abyss custom dimension with the Abyssal Citadel.
 *
 * Portal: 4 wide x 5 tall PURPUR_BLOCK frame (Nether-portal shape).
 * Activation: Right-click any purpur block in the frame with an Abyssal Key.
 * Teleport: Player lands at the citadel gate entrance (0, surface, 35).
 */
public class AbyssDimensionManager implements Listener {

    private final JGlimsPlugin plugin;
    private final NamespacedKey KEY_ABYSSAL_KEY;
    private World abyssWorld;
    private boolean citadelBuilt = false;

    public static final String ABYSS_WORLD_NAME = "world_abyss";
    private static final boolean DEBUG = true; // enable for portal/teleport logging

    public AbyssDimensionManager(JGlimsPlugin plugin) {
        this.plugin = plugin;
        this.KEY_ABYSSAL_KEY = new NamespacedKey(plugin, "abyssal_key");
    }

    /** Create or load the Abyss world. Called from onEnable(). */
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

        // Configure world settings
        abyssWorld.setDifficulty(Difficulty.HARD);
        abyssWorld.setSpawnLocation(0, 66, 0);
        abyssWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        abyssWorld.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        abyssWorld.setGameRule(GameRule.DO_MOB_SPAWNING, true);
        abyssWorld.setGameRule(GameRule.KEEP_INVENTORY, false);
        abyssWorld.setTime(18000);

        // Remove any vanilla Ender Dragon (THE_END environment auto-spawns one)
        int removed = 0;
        for (EnderDragon dragon : abyssWorld.getEntitiesByClass(EnderDragon.class)) {
            dragon.remove();
            removed++;
        }
        if (removed > 0) plugin.getLogger().info("[Abyss] Removed " + removed + " vanilla Ender Dragon(s).");

        // Build citadel on fresh worlds
        if (freshWorld) {
            plugin.getLogger().info("[Abyss] Building Abyssal Citadel (first-time generation)...");
            try {
                new AbyssCitadelBuilder(plugin).buildCitadel(abyssWorld);
                citadelBuilt = true;
                plugin.getLogger().info("[Abyss] Citadel construction complete!");
            } catch (Exception e) {
                plugin.getLogger().severe("[Abyss] Citadel build FAILED: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            citadelBuilt = true; // assume it was built previously
            plugin.getLogger().info("[Abyss] Abyss dimension loaded (citadel should already exist).");
        }

        plugin.getLogger().info("[Abyss] Abyss dimension ready: " + ABYSS_WORLD_NAME);
    }

    /** Create an Abyssal Key item. */
    public ItemStack createAbyssalKey() {
        ItemStack key = new ItemStack(Material.ECHO_SHARD, 1);
        ItemMeta meta = key.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Abyssal Key", TextColor.color(170, 0, 0))
                    .decorate(TextDecoration.BOLD)
                    .decoration(TextDecoration.ITALIC, false));
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
        if (clicked == null) return;
        if (clicked.getType() != Material.PURPUR_BLOCK) return;

        if (DEBUG) plugin.getLogger().info("[Abyss] " + player.getName()
                + " right-clicked purpur at " + clicked.getX() + "," + clicked.getY() + "," + clicked.getZ());

        if (findPortalFrame(clicked)) {
            event.setCancelled(true);
            if (DEBUG) plugin.getLogger().info("[Abyss] Valid portal frame found! Activating...");
            activateAbyssPortal(player, clicked);
        } else {
            if (DEBUG) plugin.getLogger().info("[Abyss] No valid portal frame around clicked block.");
            player.sendMessage(Component.text("No valid Purpur portal frame detected.", NamedTextColor.RED));
            player.sendMessage(Component.text("Build a 4 wide x 5 tall frame from Purpur Blocks.", NamedTextColor.GRAY));
        }
    }

    private boolean findPortalFrame(Block block) {
        int bx = block.getX(), by = block.getY(), bz = block.getZ();
        World world = block.getWorld();

        for (int dy = -4; dy <= 0; dy++) {
            int oy = by + dy;
            for (int dx = -3; dx <= 0; dx++) {
                if (isCompletePortalFrameX(world, bx + dx, oy, bz)) return true;
            }
            for (int dz = -3; dz <= 0; dz++) {
                if (isCompletePortalFrameZ(world, bx, oy, bz + dz)) return true;
            }
        }
        return false;
    }

    private boolean isCompletePortalFrameX(World world, int ox, int oy, int oz) {
        for (int x = 0; x < 4; x++) {
            if (world.getBlockAt(ox + x, oy, oz).getType() != Material.PURPUR_BLOCK) return false;
            if (world.getBlockAt(ox + x, oy + 4, oz).getType() != Material.PURPUR_BLOCK) return false;
        }
        for (int y = 1; y <= 3; y++) {
            if (world.getBlockAt(ox, oy + y, oz).getType() != Material.PURPUR_BLOCK) return false;
            if (world.getBlockAt(ox + 3, oy + y, oz).getType() != Material.PURPUR_BLOCK) return false;
        }
        for (int x = 1; x <= 2; x++) {
            for (int y = 1; y <= 3; y++) {
                Material mat = world.getBlockAt(ox + x, oy + y, oz).getType();
                if (mat != Material.AIR && mat != Material.CAVE_AIR && mat != Material.VOID_AIR) return false;
            }
        }
        return true;
    }

    private boolean isCompletePortalFrameZ(World world, int ox, int oy, int oz) {
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
                if (mat != Material.AIR && mat != Material.CAVE_AIR && mat != Material.VOID_AIR) return false;
            }
        }
        return true;
    }

    private void activateAbyssPortal(Player player, Block clicked) {
        if (abyssWorld == null) {
            player.sendMessage(Component.text("The Abyss dimension is not available!", NamedTextColor.RED));
            return;
        }

        // Consume one key
        ItemStack hand = player.getInventory().getItemInMainHand();
        hand.setAmount(hand.getAmount() - 1);

        // VFX at the portal
        Location portalLoc = clicked.getLocation().add(0.5, 1.5, 0.5);
        World world = clicked.getWorld();
        world.spawnParticle(Particle.REVERSE_PORTAL, portalLoc, 50, 1.0, 2.0, 1.0, 0.5);
        world.spawnParticle(Particle.DRAGON_BREATH, portalLoc, 30, 1.0, 1.5, 1.0, 0.1);
        world.spawnParticle(Particle.WITCH, portalLoc, 30, 1.0, 2.0, 1.0, 0.3);
        world.playSound(portalLoc, Sound.BLOCK_END_PORTAL_SPAWN, 1.0f, 0.5f);
        world.playSound(portalLoc, Sound.ENTITY_ENDER_DRAGON_GROWL, 0.8f, 0.3f);

        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("  \u2726 ", TextColor.color(170, 0, 0))
                .append(Component.text("THE ABYSS AWAITS...", TextColor.color(170, 0, 0)).decorate(TextDecoration.BOLD)));
        player.sendMessage(Component.empty());

        player.showTitle(net.kyori.adventure.title.Title.title(
                Component.text("\u2620 ENTERING THE ABYSS \u2620", TextColor.color(170, 0, 0))
                        .decorate(TextDecoration.BOLD),
                Component.text("Prepare yourself...", NamedTextColor.DARK_GRAY),
                net.kyori.adventure.title.Title.Times.times(
                        java.time.Duration.ofMillis(500),
                        java.time.Duration.ofSeconds(2),
                        java.time.Duration.ofMillis(1000))));

        // Delayed teleport — land at the citadel gate entrance
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            // Gate is at z=35 (just outside the south wall gate at z=30)
            Location dest = new Location(abyssWorld, 0.5, 0, 75.5);
            int safeY = abyssWorld.getHighestBlockYAt(0, 75) + 1;
            if (safeY < 30) safeY = 60; // citadel fallback
            dest.setY(safeY);

            if (DEBUG) plugin.getLogger().info("[Abyss] Teleporting " + player.getName()
                    + " to gate: 0, " + safeY + ", 35");

            player.teleport(dest);
            player.playSound(player.getLocation(), Sound.BLOCK_END_PORTAL_SPAWN, 0.8f, 0.7f);
            player.sendMessage(Component.text("  You have entered the Abyss.", TextColor.color(170, 0, 0)));

            // Remove any vanilla dragon that might have spawned
            for (EnderDragon dragon : abyssWorld.getEntitiesByClass(EnderDragon.class)) {
                dragon.remove();
            }
        }, 40L);
    }

    // Getters
    public World getAbyssWorld() { return abyssWorld; }
    public NamespacedKey getKeyAbyssalKey() { return KEY_ABYSSAL_KEY; }
    public JGlimsPlugin getPlugin() { return plugin; }
    public boolean isCitadelBuilt() { return citadelBuilt; }
}
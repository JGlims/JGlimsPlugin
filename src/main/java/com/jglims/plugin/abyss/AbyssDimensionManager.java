package com.jglims.plugin.abyss;

import com.jglims.plugin.JGlimsPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
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
 * Manages the Abyss custom dimension.
 *
 * Portal: A nether-portal-shaped frame (4 wide x 5 tall) built from PURPUR_BLOCK.
 * Activation: Right-click ANY purpur block in the frame with an Abyssal Key.
 * Teleportation: Player is teleported to the Abyss world at Y=65 above the central island.
 * Return: Players can build another purpur portal in the Abyss, or use /spawn.
 */
public class AbyssDimensionManager implements Listener {

    private final JGlimsPlugin plugin;
    private final NamespacedKey KEY_ABYSSAL_KEY;
    private World abyssWorld;

    public static final String ABYSS_WORLD_NAME = "world_abyss";
    private static final boolean DEBUG = false; // set true for portal debug logging

    public AbyssDimensionManager(JGlimsPlugin plugin) {
        this.plugin = plugin;
        this.KEY_ABYSSAL_KEY = new NamespacedKey(plugin, "abyssal_key");
    }

    /** Create or load the Abyss world. Called from onEnable(). */
    public void initialize() {
        abyssWorld = plugin.getServer().getWorld(ABYSS_WORLD_NAME);
        if (abyssWorld == null) {
            plugin.getLogger().info("Creating Abyss dimension...");
            WorldCreator creator = new WorldCreator(ABYSS_WORLD_NAME);
            creator.environment(World.Environment.THE_END);
            creator.generator(new AbyssChunkGenerator());
            creator.generateStructures(false);
            abyssWorld = creator.createWorld();
            if (abyssWorld != null) {
                abyssWorld.setDifficulty(Difficulty.HARD);
                abyssWorld.setSpawnLocation(0, 66, 0);
                abyssWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
                abyssWorld.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
                abyssWorld.setGameRule(GameRule.DO_MOB_SPAWNING, true);
                abyssWorld.setGameRule(GameRule.KEEP_INVENTORY, false);
                abyssWorld.setTime(18000);
                plugin.getLogger().info("Abyss dimension created: " + ABYSS_WORLD_NAME);
            }
        } else {
            plugin.getLogger().info("Abyss dimension loaded: " + ABYSS_WORLD_NAME);
        }
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

    /**
     * Handle right-clicking a purpur block in a portal frame with an Abyssal Key.
     *
     * FIX (v7.0): Only check RIGHT_CLICK_BLOCK on PURPUR_BLOCK.
     * Search for the portal frame origin by scanning around the clicked block
     * in ALL directions (negative AND positive Y offsets).
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Only accept right-clicking a block
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (!isAbyssalKey(hand)) return;

        Block clicked = event.getClickedBlock();
        if (clicked == null) return;

        // Only accept clicking directly on a PURPUR_BLOCK
        if (clicked.getType() != Material.PURPUR_BLOCK) return;

        if (DEBUG) plugin.getLogger().info("[Abyss] Player " + player.getName()
                + " right-clicked purpur at " + clicked.getX() + "," + clicked.getY() + "," + clicked.getZ());

        // Search around the clicked purpur block for a valid portal frame
        if (findPortalFrame(clicked)) {
            event.setCancelled(true);
            if (DEBUG) plugin.getLogger().info("[Abyss] Valid portal frame found! Activating...");
            activateAbyssPortal(player, clicked);
        } else {
            if (DEBUG) plugin.getLogger().info("[Abyss] No valid portal frame found around clicked block.");
            player.sendMessage(Component.text("No valid Purpur portal frame detected.", NamedTextColor.RED));
            player.sendMessage(Component.text("Build a 4 wide x 5 tall frame from Purpur Blocks.", NamedTextColor.GRAY));
        }
    }

    /**
     * Search around a clicked purpur block for a valid 4x5 portal frame.
     * The clicked block could be any of the 14 frame blocks, so we need to
     * search for the bottom-left origin in a range that covers the full frame.
     *
     * Frame is 4 wide x 5 tall, so from any frame block:
     *   X/Z offset: -3 to 0 (the clicked block could be the rightmost pillar)
     *   Y offset:   -4 to 0 (the clicked block could be the top row)
     */
    private boolean findPortalFrame(Block block) {
        int bx = block.getX();
        int by = block.getY();
        int bz = block.getZ();
        World world = block.getWorld();

        // Try every possible origin for both orientations
        for (int dy = -4; dy <= 0; dy++) {
            int oy = by + dy;

            // X-oriented frame: origin scans along X axis
            for (int dx = -3; dx <= 0; dx++) {
                if (isCompletePortalFrameX(world, bx + dx, oy, bz)) {
                    if (DEBUG) plugin.getLogger().info("[Abyss] Found X-oriented frame at origin "
                            + (bx + dx) + "," + oy + "," + bz);
                    return true;
                }
            }

            // Z-oriented frame: origin scans along Z axis
            for (int dz = -3; dz <= 0; dz++) {
                if (isCompletePortalFrameZ(world, bx, oy, bz + dz)) {
                    if (DEBUG) plugin.getLogger().info("[Abyss] Found Z-oriented frame at origin "
                            + bx + "," + oy + "," + (bz + dz));
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check for a complete purpur portal frame oriented along the X axis.
     * Origin (ox, oy, oz) is the bottom-left corner of the frame.
     *
     * Frame shape (4w x 5h, viewed from south/+Z):
     *   PPPP      <- oy+4 (top row)
     *   P  P      <- oy+3
     *   P  P      <- oy+2
     *   P  P      <- oy+1
     *   PPPP      <- oy   (bottom row)
     *   ^  ^
     *   ox ox+3
     */
    private boolean isCompletePortalFrameX(World world, int ox, int oy, int oz) {
        // Bottom row: 4 purpur blocks along X
        for (int x = 0; x < 4; x++) {
            if (world.getBlockAt(ox + x, oy, oz).getType() != Material.PURPUR_BLOCK) return false;
        }
        // Top row: 4 purpur blocks along X
        for (int x = 0; x < 4; x++) {
            if (world.getBlockAt(ox + x, oy + 4, oz).getType() != Material.PURPUR_BLOCK) return false;
        }
        // Left pillar (x=ox) and right pillar (x=ox+3): 3 blocks each (y+1 to y+3)
        for (int y = 1; y <= 3; y++) {
            if (world.getBlockAt(ox, oy + y, oz).getType() != Material.PURPUR_BLOCK) return false;
            if (world.getBlockAt(ox + 3, oy + y, oz).getType() != Material.PURPUR_BLOCK) return false;
        }
        // Interior must be air (2 wide x 3 tall)
        for (int x = 1; x <= 2; x++) {
            for (int y = 1; y <= 3; y++) {
                Material mat = world.getBlockAt(ox + x, oy + y, oz).getType();
                if (mat != Material.AIR && mat != Material.CAVE_AIR && mat != Material.VOID_AIR) return false;
            }
        }
        return true;
    }

    /** Same as X but oriented along the Z axis. */
    private boolean isCompletePortalFrameZ(World world, int ox, int oy, int oz) {
        for (int z = 0; z < 4; z++) {
            if (world.getBlockAt(ox, oy, oz + z).getType() != Material.PURPUR_BLOCK) return false;
        }
        for (int z = 0; z < 4; z++) {
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

        // Delayed teleport (2 seconds)
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            Location dest = abyssWorld.getSpawnLocation().clone().add(0.5, 0, 0.5);
            int safeY = abyssWorld.getHighestBlockYAt(dest.getBlockX(), dest.getBlockZ()) + 1;
            if (safeY < 10) safeY = 66;
            dest.setY(safeY);
            player.teleport(dest);
            player.playSound(player.getLocation(), Sound.BLOCK_END_PORTAL_SPAWN, 0.8f, 0.7f);
            player.sendMessage(Component.text("  You have entered the Abyss.", TextColor.color(170, 0, 0)));
        }, 40L);
    }

    // Getters
    public World getAbyssWorld() { return abyssWorld; }
    public NamespacedKey getKeyAbyssalKey() { return KEY_ABYSSAL_KEY; }
    public JGlimsPlugin getPlugin() { return plugin; }
}
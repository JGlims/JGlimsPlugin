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
 * Activation: Right-click the inside of the frame with an Abyssal Key.
 * Teleportation: Player is teleported to the Abyss world at Y=65 above the central island.
 * Return: Players can build another purpur portal in the Abyss, or use /spawn.
 */
public class AbyssDimensionManager implements Listener {

    private final JGlimsPlugin plugin;
    private final NamespacedKey KEY_ABYSSAL_KEY;
    private final NamespacedKey KEY_ABYSS_PORTAL;
    private World abyssWorld;

    public static final String ABYSS_WORLD_NAME = "world_abyss";

    public AbyssDimensionManager(JGlimsPlugin plugin) {
        this.plugin = plugin;
        this.KEY_ABYSSAL_KEY = new NamespacedKey(plugin, "abyssal_key");
        this.KEY_ABYSS_PORTAL = new NamespacedKey(plugin, "abyss_portal_block");
    }

    /**
     * Create or load the Abyss world. Called from onEnable().
     */
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
                abyssWorld.setTime(18000); // permanent night
                plugin.getLogger().info("Abyss dimension created: " + ABYSS_WORLD_NAME);
            }
        } else {
            plugin.getLogger().info("Abyss dimension loaded: " + ABYSS_WORLD_NAME);
        }
    }

    /**
     * Create an Abyssal Key item.
     */
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
            lore.add(Component.text("Use on a Purpur Portal frame to", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("open a gateway to the Abyss.", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());
            lore.add(Component.text("Portal: 4x5 Purpur Block frame", NamedTextColor.DARK_PURPLE)
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
     * Handle right-clicking inside a purpur portal frame with an Abyssal Key.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        ItemStack hand = player.getInventory().getItemInMainHand();

        if (!isAbyssalKey(hand)) return;
        Block clicked = event.getClickedBlock();
        if (clicked == null) return;

        // Check if the clicked block is inside a valid purpur portal frame
        // or is a purpur block that forms part of a portal frame
        if (clicked.getType() == Material.PURPUR_BLOCK || clicked.getType() == Material.AIR) {
            if (isValidPurpurPortalFrame(clicked)) {
                event.setCancelled(true);
                activateAbyssPortal(player, clicked);
            }
        }
    }

    /**
     * Checks if the block is inside a valid 4x5 purpur portal frame (nether portal shape).
     * Frame: 4 wide x 5 tall (2x3 interior opening).
     *
     * Checks both X-axis and Z-axis orientations.
     */
    private boolean isValidPurpurPortalFrame(Block block) {
        // Try both orientations
        return checkPortalFrame(block, BlockFace.NORTH) || checkPortalFrame(block, BlockFace.EAST);
    }

    private boolean checkPortalFrame(Block block, BlockFace facing) {
        // Portal interior is 2 wide x 3 tall
        // Frame is 4 wide x 5 tall
        // We need to find the portal by checking if this block is inside or part of the frame

        Location loc = block.getLocation();
        World world = block.getWorld();

        // Search in a small area around the clicked block for the portal frame
        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = -4; dy <= 0; dy++) {
                for (int dz = -3; dz <= 3; dz++) {
                    Location origin = loc.clone().add(dx, dy, dz);
                    if (facing == BlockFace.NORTH) {
                        if (isCompletePortalFrameX(world, origin.getBlockX(), origin.getBlockY(), origin.getBlockZ())) {
                            return true;
                        }
                    } else {
                        if (isCompletePortalFrameZ(world, origin.getBlockX(), origin.getBlockY(), origin.getBlockZ())) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Check for a complete purpur portal frame oriented along the X axis.
     * Origin is the bottom-left corner of the frame.
     * Frame shape (4w x 5h):
     *   PPPP
     *   P  P
     *   P  P
     *   P  P
     *   PPPP
     */
    private boolean isCompletePortalFrameX(World world, int ox, int oy, int oz) {
        // Bottom row (4 blocks along X)
        for (int x = 0; x < 4; x++) {
            if (world.getBlockAt(ox + x, oy, oz).getType() != Material.PURPUR_BLOCK) return false;
        }
        // Top row
        for (int x = 0; x < 4; x++) {
            if (world.getBlockAt(ox + x, oy + 4, oz).getType() != Material.PURPUR_BLOCK) return false;
        }
        // Left and right pillars (3 blocks each, y+1 to y+3)
        for (int y = 1; y <= 3; y++) {
            if (world.getBlockAt(ox, oy + y, oz).getType() != Material.PURPUR_BLOCK) return false;
            if (world.getBlockAt(ox + 3, oy + y, oz).getType() != Material.PURPUR_BLOCK) return false;
        }
        // Interior must be air or non-solid (2x3)
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

        // Consume the key
        ItemStack hand = player.getInventory().getItemInMainHand();
        hand.setAmount(hand.getAmount() - 1);

        // VFX at portal
        Location portalLoc = clicked.getLocation().add(0.5, 0.5, 0.5);
        World world = clicked.getWorld();
        world.spawnParticle(Particle.REVERSE_PORTAL, portalLoc, 30, 1, 2, 1, 0.5);
        world.spawnParticle(Particle.DRAGON_BREATH, portalLoc, 25, 1, 1.5, 1, 0.1);
        world.spawnParticle(Particle.WITCH, portalLoc, 25, 1, 2, 1, 0.3);
        world.playSound(portalLoc, Sound.BLOCK_END_PORTAL_SPAWN, 1.0f, 0.5f);
        world.playSound(portalLoc, Sound.ENTITY_ENDER_DRAGON_GROWL, 0.8f, 0.3f);

        // Fill portal interior with end gateway blocks briefly (visual)
        // Then teleport player after 1 second
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

        // Delayed teleport
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            Location dest = abyssWorld.getSpawnLocation().clone().add(0.5, 0, 0.5);
            // Find safe Y
            int safeY = abyssWorld.getHighestBlockYAt(dest.getBlockX(), dest.getBlockZ()) + 1;
            if (safeY < 10) safeY = 66; // fallback to spawn height
            dest.setY(safeY);
            player.teleport(dest);
            player.playSound(player.getLocation(), Sound.BLOCK_END_PORTAL_SPAWN, 0.8f, 0.7f);
            player.sendMessage(Component.text("  You have entered the Abyss.", TextColor.color(170, 0, 0)));
        }, 40L); // 2 seconds
    }

    // Getters
    public World getAbyssWorld() { return abyssWorld; }
    public NamespacedKey getKeyAbyssalKey() { return KEY_ABYSSAL_KEY; }
    public JGlimsPlugin getPlugin() { return plugin; }
}
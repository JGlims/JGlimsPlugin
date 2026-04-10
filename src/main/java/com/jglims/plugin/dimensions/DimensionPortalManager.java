package com.jglims.plugin.dimensions;

import com.jglims.plugin.JGlimsPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.util.*;

/**
 * Reusable portal framework for all custom dimensions.
 * <p>
 * Each dimension registers a {@link PortalDefinition} specifying the frame material,
 * activation item, visual fill material, destination world, and spawn point.
 * The portal pattern is always a 4-wide x 5-tall rectangle frame.
 */
public class DimensionPortalManager implements Listener {

    /**
     * Defines a dimension portal type.
     *
     * @param id             unique identifier
     * @param frameMaterial  block type for the portal frame
     * @param activationItem material of the item used to activate (right-click)
     * @param fillMaterial   block placed inside the frame as portal visual
     * @param worldName      name of the destination world
     * @param portalColor    color for title/messages
     * @param displayName    human-readable name of the dimension
     */
    public record PortalDefinition(
            String id,
            Material frameMaterial,
            Material activationItem,
            Material fillMaterial,
            String worldName,
            TextColor portalColor,
            String displayName
    ) {}

    private final JGlimsPlugin plugin;
    private final Map<String, PortalDefinition> portals = new HashMap<>();

    /** Tracks active portal blocks for teleportation on move-through. */
    private final Set<Location> activePortalBlocks = new HashSet<>();

    /** Maps portal block → destination world name. */
    private final Map<Location, String> portalBlockDest = new HashMap<>();

    /** Teleport cooldowns per player. */
    private final Map<UUID, Long> teleportCooldowns = new HashMap<>();

    private static final long COOLDOWN_MS = 5000;

    public DimensionPortalManager(JGlimsPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Registers a portal definition. Call during initialization.
     *
     * @param def the portal definition
     */
    public void registerPortal(PortalDefinition def) {
        portals.put(def.id(), def);
        plugin.getLogger().info("[Portal] Registered portal: " + def.displayName()
                + " (" + def.frameMaterial() + " + " + def.activationItem() + ")");
    }

    // ── Portal Activation ──────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getClickedBlock() == null) return;

        Block clicked = event.getClickedBlock();
        Player player = event.getPlayer();
        ItemStack heldItem = player.getInventory().getItemInMainHand();

        // Check each portal definition
        for (PortalDefinition def : portals.values()) {
            if (clicked.getType() != def.frameMaterial()) continue;
            if (heldItem.getType() != def.activationItem()) continue;

            event.setCancelled(true);

            // Search for a valid 4x5 frame
            Location frameLoc = findFrame(clicked.getLocation(), def.frameMaterial());
            if (frameLoc == null) {
                player.sendMessage(Component.text("No valid portal frame detected...",
                        NamedTextColor.GRAY));
                return;
            }

            // Fill frame interior with portal blocks
            fillFrame(frameLoc, def);

            // Consume activation item (unless it's a bucket-type that shouldn't be consumed)
            if (def.activationItem() != Material.WATER_BUCKET) {
                heldItem.setAmount(heldItem.getAmount() - 1);
            } else {
                // Water bucket → empty bucket
                player.getInventory().setItemInMainHand(new ItemStack(Material.BUCKET));
            }

            // Effects
            player.getWorld().playSound(clicked.getLocation(),
                    Sound.BLOCK_END_PORTAL_SPAWN, 1.0f, 1.2f);
            player.sendMessage(Component.text(def.displayName() + " portal activated!",
                    def.portalColor()).decorate(TextDecoration.BOLD));
            return;
        }
    }

    // ── Portal Teleportation ───────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;

        Player player = event.getPlayer();
        Location playerBlock = player.getLocation().getBlock().getLocation();

        // Check if standing in a portal block
        String destWorld = portalBlockDest.get(playerBlock);
        if (destWorld == null) return;

        // Cooldown check
        UUID uuid = player.getUniqueId();
        Long lastTp = teleportCooldowns.get(uuid);
        if (lastTp != null && System.currentTimeMillis() - lastTp < COOLDOWN_MS) return;

        // Find destination world
        World dest = Bukkit.getWorld(destWorld);
        if (dest == null) {
            player.sendMessage(Component.text("Destination world not loaded!", NamedTextColor.RED));
            return;
        }

        teleportCooldowns.put(uuid, System.currentTimeMillis());

        // Find portal definition for this world
        PortalDefinition def = null;
        for (PortalDefinition d : portals.values()) {
            if (d.worldName().equals(destWorld)) {
                def = d;
                break;
            }
        }

        // Teleport with effects
        Location spawnLoc = findSafeSpawn(dest);
        showTeleportEffects(player, def);

        new BukkitRunnable() {
            @Override
            public void run() {
                player.teleport(spawnLoc);
                player.playSound(spawnLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.8f);
            }
        }.runTaskLater(plugin, 20L); // 1 second delay for effects
    }

    // ── Frame Detection ────────────────────────────────────────────────

    /**
     * Searches for a valid 4-wide x 5-tall portal frame near the clicked block.
     * Returns the bottom-left corner of the frame, or null if not found.
     */
    private Location findFrame(Location clicked, Material frameMaterial) {
        World world = clicked.getWorld();
        int cx = clicked.getBlockX();
        int cy = clicked.getBlockY();
        int cz = clicked.getBlockZ();

        // Search in a 6-block radius for valid frames
        for (int dx = -5; dx <= 1; dx++) {
            for (int dy = -4; dy <= 0; dy++) {
                // Try X-axis aligned frame
                if (validateFrame(world, cx + dx, cy + dy, cz, frameMaterial, true)) {
                    return new Location(world, cx + dx, cy + dy, cz);
                }
                // Try Z-axis aligned frame
                if (validateFrame(world, cx, cy + dy, cz + dx, frameMaterial, false)) {
                    return new Location(world, cx, cy + dy, cz + dx);
                }
            }
        }
        return null;
    }

    /**
     * Validates a 4-wide x 5-tall portal frame.
     *
     * @param world    the world
     * @param bx       bottom-left X
     * @param by       bottom-left Y
     * @param bz       bottom-left Z
     * @param material frame material
     * @param xAxis    true if frame extends along X axis, false for Z axis
     * @return true if valid frame
     */
    private boolean validateFrame(World world, int bx, int by, int bz,
                                  Material material, boolean xAxis) {
        // Frame shape: 4 wide, 5 tall rectangle
        // Bottom row: 4 frame blocks
        // Sides: frame blocks at edges
        // Top row: 4 frame blocks
        // Interior (2x3): must be air or portal block
        for (int w = 0; w < 4; w++) {
            for (int h = 0; h < 5; h++) {
                int x = xAxis ? bx + w : bx;
                int y = by + h;
                int z = xAxis ? bz : bz + w;
                Block block = world.getBlockAt(x, y, z);

                boolean isEdge = (w == 0 || w == 3 || h == 0 || h == 4);
                if (isEdge) {
                    if (block.getType() != material) return false;
                } else {
                    if (block.getType().isSolid() && block.getType() != Material.END_GATEWAY) return false;
                }
            }
        }
        return true;
    }

    /**
     * Fills the interior of a portal frame with the portal visual material.
     */
    private void fillFrame(Location frameLoc, PortalDefinition def) {
        World world = frameLoc.getWorld();
        int bx = frameLoc.getBlockX();
        int by = frameLoc.getBlockY();
        int bz = frameLoc.getBlockZ();

        // Detect axis (check if frame extends along X)
        boolean xAxis = world.getBlockAt(bx + 1, by, bz).getType() == def.frameMaterial();

        // Fill interior (columns 1-2, rows 1-3)
        for (int w = 1; w <= 2; w++) {
            for (int h = 1; h <= 3; h++) {
                int x = xAxis ? bx + w : bx;
                int y = by + h;
                int z = xAxis ? bz : bz + w;
                Block block = world.getBlockAt(x, y, z);
                block.setType(def.fillMaterial());

                // Track as active portal block
                Location loc = block.getLocation();
                activePortalBlocks.add(loc);
                portalBlockDest.put(loc, def.worldName());
            }
        }
    }

    // ── Teleportation Helpers ──────────────────────────────────────────

    /**
     * Finds a safe spawn location in the destination world.
     */
    private Location findSafeSpawn(World world) {
        Location spawn = world.getSpawnLocation();
        // Search for a safe spot near spawn
        for (int y = spawn.getBlockY(); y < world.getMaxHeight() - 5; y++) {
            Block feet = world.getBlockAt(spawn.getBlockX(), y, spawn.getBlockZ());
            Block head = world.getBlockAt(spawn.getBlockX(), y + 1, spawn.getBlockZ());
            Block below = world.getBlockAt(spawn.getBlockX(), y - 1, spawn.getBlockZ());
            if (below.getType().isSolid() && !feet.getType().isSolid() && !head.getType().isSolid()) {
                return new Location(world, spawn.getX(), y, spawn.getZ(), spawn.getYaw(), spawn.getPitch());
            }
        }
        return spawn.add(0, 1, 0);
    }

    /**
     * Shows teleport visual effects.
     */
    private void showTeleportEffects(Player player, PortalDefinition def) {
        TextColor color = def != null ? def.portalColor() : TextColor.color(170, 0, 170);
        String name = def != null ? def.displayName() : "Unknown";

        player.showTitle(Title.title(
                Component.text(name, color).decorate(TextDecoration.BOLD),
                Component.text("Entering dimension...", NamedTextColor.GRAY),
                Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(1), Duration.ofMillis(500))
        ));
        player.getWorld().spawnParticle(Particle.PORTAL,
                player.getLocation().add(0, 1, 0), 50, 0.5, 1, 0.5, 0);
    }
}

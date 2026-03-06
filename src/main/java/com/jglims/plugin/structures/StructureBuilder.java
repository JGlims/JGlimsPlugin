package com.jglims.plugin.structures;

import com.jglims.plugin.JGlimsPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Utility class for programmatic structure generation.
 * Provides methods to place blocks, create rooms, walls, pillars, and other building primitives.
 */
public class StructureBuilder {

    private final JGlimsPlugin plugin;
    private final World world;
    private final Location origin;
    private final Random random;
    private final List<Location> chestLocations = new ArrayList<>();
    private Location bossSpawnLocation;

    public StructureBuilder(JGlimsPlugin plugin, World world, Location origin) {
        this.plugin = plugin;
        this.world = world;
        this.origin = origin.clone();
        this.random = new Random();
    }

    // ── Basic block placement ──

    public void setBlock(int rx, int ry, int rz, Material material) {
        Block block = world.getBlockAt(origin.getBlockX() + rx, origin.getBlockY() + ry, origin.getBlockZ() + rz);
        block.setType(material, false);
    }

    public void setBlockIfAir(int rx, int ry, int rz, Material material) {
        Block block = world.getBlockAt(origin.getBlockX() + rx, origin.getBlockY() + ry, origin.getBlockZ() + rz);
        if (block.getType().isAir()) block.setType(material, false);
    }

    public Material getBlock(int rx, int ry, int rz) {
        return world.getBlockAt(origin.getBlockX() + rx, origin.getBlockY() + ry, origin.getBlockZ() + rz).getType();
    }

    // ── Shape primitives ──

    /** Fill a rectangular region with a material. */
    public void fillBox(int x1, int y1, int z1, int x2, int y2, int z2, Material material) {
        int minX = Math.min(x1, x2), maxX = Math.max(x1, x2);
        int minY = Math.min(y1, y2), maxY = Math.max(y1, y2);
        int minZ = Math.min(z1, z2), maxZ = Math.max(z1, z2);
        for (int x = minX; x <= maxX; x++)
            for (int y = minY; y <= maxY; y++)
                for (int z = minZ; z <= maxZ; z++)
                    setBlock(x, y, z, material);
    }

    /** Fill only the walls (shell) of a box, interior is hollow. */
    public void fillWalls(int x1, int y1, int z1, int x2, int y2, int z2, Material material) {
        int minX = Math.min(x1, x2), maxX = Math.max(x1, x2);
        int minY = Math.min(y1, y2), maxY = Math.max(y1, y2);
        int minZ = Math.min(z1, z2), maxZ = Math.max(z1, z2);
        for (int x = minX; x <= maxX; x++)
            for (int y = minY; y <= maxY; y++)
                for (int z = minZ; z <= maxZ; z++) {
                    if (x == minX || x == maxX || y == minY || y == maxY || z == minZ || z == maxZ)
                        setBlock(x, y, z, material);
                }
    }

    /** Hollow out the interior of a box (set to AIR). */
    public void hollowBox(int x1, int y1, int z1, int x2, int y2, int z2) {
        int minX = Math.min(x1, x2) + 1, maxX = Math.max(x1, x2) - 1;
        int minY = Math.min(y1, y2) + 1, maxY = Math.max(y1, y2) - 1;
        int minZ = Math.min(z1, z2) + 1, maxZ = Math.max(z1, z2) - 1;
        for (int x = minX; x <= maxX; x++)
            for (int y = minY; y <= maxY; y++)
                for (int z = minZ; z <= maxZ; z++)
                    setBlock(x, y, z, Material.AIR);
    }

    /** Create a floor at a specific Y level. */
    public void fillFloor(int x1, int z1, int x2, int z2, int y, Material material) {
        fillBox(x1, y, z1, x2, y, z2, material);
    }

    /** Create a pillar from y1 to y2 at (x, z). */
    public void pillar(int x, int y1, int y2, int z, Material material) {
        for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++)
            setBlock(x, y, z, material);
    }

    /** Create a circle outline at a specific Y level. */
    public void circle(int cx, int y, int cz, int radius, Material material) {
        for (int angle = 0; angle < 360; angle++) {
            int x = cx + (int) Math.round(radius * Math.cos(Math.toRadians(angle)));
            int z = cz + (int) Math.round(radius * Math.sin(Math.toRadians(angle)));
            setBlock(x, y, z, material);
        }
    }

    /** Fill a circle (disc) at a specific Y level. */
    public void filledCircle(int cx, int y, int cz, int radius, Material material) {
        for (int x = cx - radius; x <= cx + radius; x++)
            for (int z = cz - radius; z <= cz + radius; z++) {
                if ((x - cx) * (x - cx) + (z - cz) * (z - cz) <= radius * radius)
                    setBlock(x, y, z, material);
            }
    }

    // ── Decoration helpers ──

    /** Randomly replace some blocks in a region with another material (for ruined effects). */
    public void scatter(int x1, int y1, int z1, int x2, int y2, int z2,
                        Material target, Material replacement, double chance) {
        int minX = Math.min(x1, x2), maxX = Math.max(x1, x2);
        int minY = Math.min(y1, y2), maxY = Math.max(y1, y2);
        int minZ = Math.min(z1, z2), maxZ = Math.max(z1, z2);
        for (int x = minX; x <= maxX; x++)
            for (int y = minY; y <= maxY; y++)
                for (int z = minZ; z <= maxZ; z++) {
                    if (getBlock(x, y, z) == target && random.nextDouble() < chance)
                        setBlock(x, y, z, replacement);
                }
    }

    /** Remove random blocks in a region (for ruins/decay). */
    public void decay(int x1, int y1, int z1, int x2, int y2, int z2, double chance) {
        int minX = Math.min(x1, x2), maxX = Math.max(x1, x2);
        int minY = Math.min(y1, y2), maxY = Math.max(y1, y2);
        int minZ = Math.min(z1, z2), maxZ = Math.max(z1, z2);
        for (int x = minX; x <= maxX; x++)
            for (int y = minY; y <= maxY; y++)
                for (int z = minZ; z <= maxZ; z++) {
                    if (!getBlock(x, y, z).isAir() && random.nextDouble() < chance)
                        setBlock(x, y, z, Material.AIR);
                }
    }

    /** Place a random material from an array. */
    public void setRandomBlock(int rx, int ry, int rz, Material... options) {
        setBlock(rx, ry, rz, options[random.nextInt(options.length)]);
    }

    // ── Chest and boss spawn ──

    /** Place a chest and register its location for loot population. */
    public Location placeChest(int rx, int ry, int rz) {
        setBlock(rx, ry, rz, Material.CHEST);
        Location loc = new Location(world, origin.getBlockX() + rx, origin.getBlockY() + ry, origin.getBlockZ() + rz);
        chestLocations.add(loc);
        return loc;
    }

    /** Set where the boss should spawn. */
    public void setBossSpawn(int rx, int ry, int rz) {
        bossSpawnLocation = new Location(world, origin.getBlockX() + rx + 0.5, origin.getBlockY() + ry, origin.getBlockZ() + rz + 0.5);
    }

    // ── Surface finding ──

    /** Find the highest solid block Y at (x, z) relative to origin. */
    public int getSurfaceY(int rx, int rz) {
        int worldX = origin.getBlockX() + rx;
        int worldZ = origin.getBlockZ() + rz;
        return world.getHighestBlockYAt(worldX, worldZ);
    }

    /** Check if the terrain is relatively flat in the given area. Max height difference allowed. */
    public boolean isTerrainFlat(int x1, int z1, int x2, int z2, int maxDiff) {
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x += 2)
            for (int z = Math.min(z1, z2); z <= Math.max(z1, z2); z += 2) {
                int y = getSurfaceY(x, z);
                if (y < minY) minY = y;
                if (y > maxY) maxY = y;
            }
        return (maxY - minY) <= maxDiff;
    }

    /** Check if the area is not underwater. */
    public boolean isAboveWater(int rx, int rz) {
        int y = getSurfaceY(rx, rz);
        Material surface = world.getBlockAt(origin.getBlockX() + rx, y, origin.getBlockZ() + rz).getType();
        return surface != Material.WATER && surface != Material.LAVA;
    }

    // ── Getters ──

    public Location getOrigin() { return origin.clone(); }
    public List<Location> getChestLocations() { return chestLocations; }
    public Location getBossSpawnLocation() { return bossSpawnLocation; }
    public World getWorld() { return world; }
    public Random getRandom() { return random; }
    public JGlimsPlugin getPlugin() { return plugin; }
}
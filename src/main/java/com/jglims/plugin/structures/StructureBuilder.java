package com.jglims.plugin.structures;

import com.jglims.plugin.JGlimsPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Advanced utility class for programmatic structure generation.
 * Provides primitives from basic fills to complex architectural elements:
 * gothic arches, domes, gabled roofs, spiral staircases, windows, pillars,
 * interior furnishing, material gradients, and decay effects.
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

    // ══════════════════════════════════════════════════════════════════
    // BASIC BLOCK PLACEMENT
    // ══════════════════════════════════════════════════════════════════

    public void setBlock(int rx, int ry, int rz, Material material) {
        world.getBlockAt(origin.getBlockX() + rx, origin.getBlockY() + ry, origin.getBlockZ() + rz)
                .setType(material, false);
    }

    public void setBlockIfAir(int rx, int ry, int rz, Material material) {
        Block block = world.getBlockAt(origin.getBlockX() + rx, origin.getBlockY() + ry, origin.getBlockZ() + rz);
        if (block.getType().isAir()) block.setType(material, false);
    }

    public void setBlockIfSolid(int rx, int ry, int rz, Material material) {
        Block block = world.getBlockAt(origin.getBlockX() + rx, origin.getBlockY() + ry, origin.getBlockZ() + rz);
        if (block.getType().isSolid()) block.setType(material, false);
    }

    public Material getBlock(int rx, int ry, int rz) {
        return world.getBlockAt(origin.getBlockX() + rx, origin.getBlockY() + ry, origin.getBlockZ() + rz).getType();
    }

    public void setRandomBlock(int rx, int ry, int rz, Material... options) {
        setBlock(rx, ry, rz, options[random.nextInt(options.length)]);
    }

    /** Place a block with directional data (stairs, logs, etc). */
    public void setDirectional(int rx, int ry, int rz, Material material, BlockFace facing) {
        Block block = world.getBlockAt(origin.getBlockX() + rx, origin.getBlockY() + ry, origin.getBlockZ() + rz);
        block.setType(material, false);
        BlockData data = block.getBlockData();
        if (data instanceof Directional dir) {
            dir.setFacing(facing);
            block.setBlockData(dir, false);
        }
    }

    /** Place a slab (top or bottom half). */
    public void setSlab(int rx, int ry, int rz, Material slabMaterial, boolean top) {
        Block block = world.getBlockAt(origin.getBlockX() + rx, origin.getBlockY() + ry, origin.getBlockZ() + rz);
        block.setType(slabMaterial, false);
        BlockData data = block.getBlockData();
        if (data instanceof Slab slab) {
            slab.setType(top ? Slab.Type.TOP : Slab.Type.BOTTOM);
            block.setBlockData(slab, false);
        }
    }

    /** Place stairs with facing direction and optionally upside-down. */
    public void setStairs(int rx, int ry, int rz, Material stairMaterial, BlockFace facing, boolean upsideDown) {
        Block block = world.getBlockAt(origin.getBlockX() + rx, origin.getBlockY() + ry, origin.getBlockZ() + rz);
        block.setType(stairMaterial, false);
        BlockData data = block.getBlockData();
        if (data instanceof Stairs stairs) {
            stairs.setFacing(facing);
            stairs.setHalf(upsideDown ? org.bukkit.block.data.Bisected.Half.TOP : org.bukkit.block.data.Bisected.Half.BOTTOM);
            block.setBlockData(stairs, false);
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // FILLS AND SHAPES
    // ══════════════════════════════════════════════════════════════════

    public void fillBox(int x1, int y1, int z1, int x2, int y2, int z2, Material material) {
        for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++)
            for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++)
                for (int z = Math.min(z1, z2); z <= Math.max(z1, z2); z++)
                    setBlock(x, y, z, material);
    }

    public void fillWalls(int x1, int y1, int z1, int x2, int y2, int z2, Material material) {
        for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++)
            for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++)
                for (int z = Math.min(z1, z2); z <= Math.max(z1, z2); z++) {
                    if (x == Math.min(x1, x2) || x == Math.max(x1, x2) ||
                            y == Math.min(y1, y2) || y == Math.max(y1, y2) ||
                            z == Math.min(z1, z2) || z == Math.max(z1, z2))
                        setBlock(x, y, z, material);
                }
    }

    public void hollowBox(int x1, int y1, int z1, int x2, int y2, int z2) {
        for (int x = Math.min(x1, x2) + 1; x <= Math.max(x1, x2) - 1; x++)
            for (int y = Math.min(y1, y2) + 1; y <= Math.max(y1, y2) - 1; y++)
                for (int z = Math.min(z1, z2) + 1; z <= Math.max(z1, z2) - 1; z++)
                    setBlock(x, y, z, Material.AIR);
    }

    public void fillFloor(int x1, int z1, int x2, int z2, int y, Material material) {
        fillBox(x1, y, z1, x2, y, z2, material);
    }

    public void pillar(int x, int y1, int y2, int z, Material material) {
        for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++) setBlock(x, y, z, material);
    }

    public void circle(int cx, int y, int cz, int radius, Material material) {
        for (int a = 0; a < 360; a++) {
            int x = cx + (int) Math.round(radius * Math.cos(Math.toRadians(a)));
            int z = cz + (int) Math.round(radius * Math.sin(Math.toRadians(a)));
            setBlock(x, y, z, material);
        }
    }

    public void filledCircle(int cx, int y, int cz, int radius, Material material) {
        for (int x = cx - radius; x <= cx + radius; x++)
            for (int z = cz - radius; z <= cz + radius; z++)
                if ((x - cx) * (x - cx) + (z - cz) * (z - cz) <= radius * radius)
                    setBlock(x, y, z, material);
    }

    /** Filled ellipse at Y level. */
    public void filledEllipse(int cx, int y, int cz, int radiusX, int radiusZ, Material material) {
        for (int x = cx - radiusX; x <= cx + radiusX; x++)
            for (int z = cz - radiusZ; z <= cz + radiusZ; z++) {
                double dx = (double) (x - cx) / radiusX;
                double dz = (double) (z - cz) / radiusZ;
                if (dx * dx + dz * dz <= 1.0) setBlock(x, y, z, material);
            }
    }

    // ══════════════════════════════════════════════════════════════════
    // ARCHITECTURAL ELEMENTS
    // ══════════════════════════════════════════════════════════════════

    /**
     * Builds a gothic pointed arch along the X axis (opening faces Z).
     * @param cx center X, baseY base, cz Z position, width full width, height arch height
     */
    public void gothicArch(int cx, int baseY, int cz, int width, int height, Material material) {
        int halfW = width / 2;
        // Vertical sides
        for (int y = 0; y < height - halfW; y++) {
            setBlock(cx - halfW, baseY + y, cz, material);
            setBlock(cx + halfW, baseY + y, cz, material);
        }
        // Pointed arch curve
        for (int a = 0; a <= 180; a += 2) {
            double rad = Math.toRadians(a);
            int x = (int) Math.round(halfW * Math.cos(rad));
            int y = (int) Math.round(halfW * Math.sin(rad));
            setBlock(cx + x, baseY + height - halfW + y, cz, material);
        }
    }

    /**
     * Builds a full arch doorway (gothic arch with interior cleared).
     */
    public void archDoorway(int cx, int baseY, int cz, int width, int height, Material material) {
        gothicArch(cx, baseY, cz, width, height, material);
        int halfW = width / 2;
        // Clear interior
        for (int x = cx - halfW + 1; x <= cx + halfW - 1; x++)
            for (int y = baseY; y < baseY + height - 1; y++)
                setBlock(x, y, cz, Material.AIR);
    }

    /**
     * Builds a hemispherical dome.
     * @param cx, baseY, cz center of dome base; radius dome radius
     */
    public void dome(int cx, int baseY, int cz, int radius, Material material) {
        for (int y = 0; y <= radius; y++) {
            double r = Math.sqrt(radius * radius - y * y);
            for (int x = (int) -Math.ceil(r); x <= (int) Math.ceil(r); x++)
                for (int z = (int) -Math.ceil(r); z <= (int) Math.ceil(r); z++) {
                    double dist = Math.sqrt(x * x + y * y + z * z);
                    if (dist >= radius - 1 && dist <= radius + 0.5)
                        setBlock(cx + x, baseY + y, cz + z, material);
                }
        }
    }

    /**
     * Builds a solid dome (filled hemisphere).
     */
    public void filledDome(int cx, int baseY, int cz, int radius, Material material) {
        for (int y = 0; y <= radius; y++) {
            double r = Math.sqrt(radius * radius - y * y);
            filledCircle(cx, baseY + y, cz, (int) r, material);
        }
    }

    /**
     * Builds a gabled (triangular) roof along the Z axis.
     * @param x1, z1, x2, z2 the footprint; baseY where roof starts; material roof block
     */
    public void gabledRoof(int x1, int z1, int x2, int z2, int baseY, Material material, Material slabMat) {
        int minX = Math.min(x1, x2), maxX = Math.max(x1, x2);
        int minZ = Math.min(z1, z2), maxZ = Math.max(z1, z2);
        int width = maxX - minX;
        int peak = width / 2;
        int midX = (minX + maxX) / 2;

        for (int layer = 0; layer <= peak; layer++) {
            for (int z = minZ; z <= maxZ; z++) {
                // Stair blocks on each side
                if (layer < peak) {
                    setStairs(minX + layer, baseY + layer, z, material, BlockFace.EAST, false);
                    setStairs(maxX - layer, baseY + layer, z, material, BlockFace.WEST, false);
                } else {
                    // Peak slab
                    setSlab(midX, baseY + layer, z, slabMat, false);
                }
            }
        }
    }

    /**
     * Builds a pyramid/hip roof.
     */
    public void pyramidRoof(int x1, int z1, int x2, int z2, int baseY, Material material) {
        int minX = Math.min(x1, x2), maxX = Math.max(x1, x2);
        int minZ = Math.min(z1, z2), maxZ = Math.max(z1, z2);
        int layer = 0;
        while (minX + layer <= maxX - layer && minZ + layer <= maxZ - layer) {
            fillFloor(minX + layer, minZ + layer, maxX - layer, maxZ - layer, baseY + layer, material);
            layer++;
        }
    }

    /**
     * Builds a cone/spire (circular tapering tower top).
     */
    public void spire(int cx, int baseY, int cz, int baseRadius, int height, Material material) {
        for (int y = 0; y < height; y++) {
            double r = baseRadius * (1.0 - (double) y / height);
            if (r < 0.5) { setBlock(cx, baseY + y, cz, material); continue; }
            circle(cx, baseY + y, cz, (int) Math.ceil(r), material);
        }
    }

    /**
     * Builds a spiral staircase.
     * @param cx, cz center; y1 to y2 height range; radius spiral radius
     */
    public void spiralStaircase(int cx, int y1, int y2, int cz, int radius, Material stairMat, Material pillarMat) {
        int minY = Math.min(y1, y2), maxY = Math.max(y1, y2);
        // Central pillar
        pillar(cx, minY, maxY, cz, pillarMat);
        // Spiral steps
        for (int y = minY; y <= maxY; y++) {
            double angle = Math.toRadians((y - minY) * 30.0); // 30 degrees per step
            int sx = cx + (int) Math.round(radius * Math.cos(angle));
            int sz = cz + (int) Math.round(radius * Math.sin(angle));
            setBlock(sx, y, sz, stairMat);
            // Landing pad (wider step)
            int sx2 = cx + (int) Math.round((radius - 1) * Math.cos(angle));
            int sz2 = cz + (int) Math.round((radius - 1) * Math.sin(angle));
            setBlockIfAir(sx2, y, sz2, stairMat);
        }
    }

    /**
     * Creates a gothic window (tall narrow opening with pointed top).
     * @param facing NORTH/SOUTH/EAST/WEST determines which axis the window is on
     */
    public void gothicWindow(int cx, int baseY, int cz, int height, Material glassMat, BlockFace facing) {
        boolean zAxis = (facing == BlockFace.NORTH || facing == BlockFace.SOUTH);
        for (int y = 0; y < height; y++) {
            if (y < height - 1) {
                setBlock(cx, baseY + y, cz, glassMat);
            } else {
                // Pointed top using stairs
                setBlock(cx, baseY + y, cz, glassMat);
            }
        }
    }

    /**
     * Creates a rose window (circular stained glass pattern).
     */
    public void roseWindow(int cx, int cy, int cz, int radius, Material glass, BlockFace facing) {
        boolean zAxis = (facing == BlockFace.NORTH || facing == BlockFace.SOUTH);
        for (int a = -radius; a <= radius; a++)
            for (int b = -radius; b <= radius; b++) {
                if (a * a + b * b <= radius * radius) {
                    if (zAxis) setBlock(cx + a, cy + b, cz, glass);
                    else setBlock(cx, cy + b, cz + a, glass);
                }
            }
    }

    /**
     * Creates a flying buttress from (x1,y1,z1) down to (x2,y2,z2).
     */
    public void flyingButtress(int x1, int y1, int z1, int x2, int y2, int z2, Material material) {
        int steps = Math.max(Math.abs(x2 - x1), Math.max(Math.abs(y2 - y1), Math.abs(z2 - z1)));
        if (steps == 0) return;
        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            int x = x1 + (int) Math.round((x2 - x1) * t);
            int y = y1 + (int) Math.round((y2 - y1) * t);
            int z = z1 + (int) Math.round((z2 - z1) * t);
            setBlock(x, y, z, material);
        }
    }

    /**
     * Creates a crenellated battlement on top of a wall.
     */
    public void battlements(int x1, int y, int z1, int x2, int z2, Material material) {
        boolean xWall = (x1 == x2); // Wall runs along Z
        if (xWall) {
            for (int z = Math.min(z1, z2); z <= Math.max(z1, z2); z++) {
                if (z % 2 == 0) setBlock(x1, y, z, material);
            }
        } else {
            for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++) {
                if (x % 2 == 0) setBlock(x, y, z1, material);
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // INTERIOR FURNISHING
    // ══════════════════════════════════════════════════════════════════

    /** Place a furnished table (fence + pressure plate). */
    public void table(int x, int y, int z) {
        setBlock(x, y, z, Material.OAK_FENCE);
        setBlock(x, y + 1, z, Material.OAK_PRESSURE_PLATE);
    }

    /** Place a chair (stairs facing a direction). */
    public void chair(int x, int y, int z, Material stairMat, BlockFace facing) {
        setStairs(x, y, z, stairMat, facing, false);
    }

    /** Place a chandelier (chain + lantern hanging from ceiling). */
    public void chandelier(int x, int ceilingY, int z, int chainLength) {
        for (int i = 0; i < chainLength; i++) {
            setBlock(x, ceilingY - i, z, Material.IRON_BARS);
        }
        setBlock(x, ceilingY - chainLength, z, Material.LANTERN);
    }

    /** Place a banner. */
    public void banner(int x, int y, int z, Material bannerMat) {
        setBlock(x, y, z, bannerMat);
    }

    /** Place a row of bookshelves. */
    public void bookshelfWall(int x1, int y1, int z, int x2, int y2) {
        for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++)
            for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++)
                setBlock(x, y, z, Material.BOOKSHELF);
    }

    // ══════════════════════════════════════════════════════════════════
    // DECORATION AND WEATHERING
    // ══════════════════════════════════════════════════════════════════

    /** Randomly scatter-replace blocks in a region. */
    public void scatter(int x1, int y1, int z1, int x2, int y2, int z2,
                        Material target, Material replacement, double chance) {
        for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++)
            for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++)
                for (int z = Math.min(z1, z2); z <= Math.max(z1, z2); z++)
                    if (getBlock(x, y, z) == target && random.nextDouble() < chance)
                        setBlock(x, y, z, replacement);
    }

    /** Remove random blocks for ruins/decay effect. */
    public void decay(int x1, int y1, int z1, int x2, int y2, int z2, double chance) {
        for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++)
            for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++)
                for (int z = Math.min(z1, z2); z <= Math.max(z1, z2); z++)
                    if (!getBlock(x, y, z).isAir() && random.nextDouble() < chance)
                        setBlock(x, y, z, Material.AIR);
    }

    /** Add vines hanging from solid blocks on walls. */
    public void addVines(int x1, int y1, int z1, int x2, int y2, int z2, double chance) {
        for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++)
            for (int y = Math.max(y1, y2); y >= Math.min(y1, y2); y--)
                for (int z = Math.min(z1, z2); z <= Math.max(z1, z2); z++)
                    if (getBlock(x, y, z).isAir() && random.nextDouble() < chance) {
                        // Check if there's a solid block adjacent
                        if (getBlock(x, y + 1, z).isSolid() ||
                                getBlock(x + 1, y, z).isSolid() || getBlock(x - 1, y, z).isSolid() ||
                                getBlock(x, y, z + 1).isSolid() || getBlock(x, y, z - 1).isSolid()) {
                            setBlock(x, y, z, Material.VINE);
                        }
                    }
    }

    /** Place torches/lanterns at regular intervals along a wall. */
    public void wallLighting(int x1, int y, int z1, int x2, int z2, int spacing, Material lightSource) {
        boolean xWall = (z1 == z2);
        if (xWall) {
            for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x += spacing)
                setBlock(x, y, z1, lightSource);
        } else {
            for (int z = Math.min(z1, z2); z <= Math.max(z1, z2); z += spacing)
                setBlock(x1, y, z, lightSource);
        }
    }

    /**
     * Material gradient — blends from one material to another over a height range.
     * Useful for towers, walls that transition from stone to brick, etc.
     */
    public void gradientPillar(int x, int y1, int y2, int z, Material bottom, Material top) {
        int minY = Math.min(y1, y2), maxY = Math.max(y1, y2);
        int height = maxY - minY;
        for (int y = minY; y <= maxY; y++) {
            double t = (double) (y - minY) / Math.max(1, height);
            setBlock(x, y, z, t < 0.5 ? bottom : top);
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // COMPOUND STRUCTURES
    // ══════════════════════════════════════════════════════════════════

    /**
     * Builds a complete round tower with walls, windows, interior, and conical roof.
     */
    public void roundTower(int cx, int baseY, int cz, int radius, int height,
                           Material wallMat, Material roofMat, Material glassMat) {
        // Walls
        for (int y = 0; y < height; y++) {
            circle(cx, baseY + y, cz, radius, wallMat);
        }
        // Interior clear
        for (int y = 1; y < height; y++) {
            filledCircle(cx, baseY + y, cz, radius - 1, Material.AIR);
        }
        // Floor
        filledCircle(cx, baseY, cz, radius - 1, wallMat);
        // Windows every 3 blocks
        for (int y = 2; y < height - 2; y += 3) {
            setBlock(cx + radius, baseY + y, cz, glassMat);
            setBlock(cx - radius, baseY + y, cz, glassMat);
            setBlock(cx, baseY + y, cz + radius, glassMat);
            setBlock(cx, baseY + y, cz - radius, glassMat);
        }
        // Conical roof
        spire(cx, baseY + height, cz, radius + 1, radius + 2, roofMat);
        // Spiral staircase
        spiralStaircase(cx, baseY + 1, baseY + height - 1, cz, radius - 1,
                Material.STONE_BRICK_STAIRS, wallMat);
    }

    /**
     * Builds a rectangular room with walls, floor, ceiling, door, and lighting.
     */
    public void furnishedRoom(int x1, int y, int z1, int x2, int z2, int height,
                              Material wallMat, Material floorMat, Material ceilMat) {
        // Floor
        fillFloor(x1, z1, x2, z2, y, floorMat);
        // Walls
        for (int h = 1; h <= height; h++) {
            fillBox(x1, y + h, z1, x2, y + h, z1, wallMat); // North
            fillBox(x1, y + h, z2, x2, y + h, z2, wallMat); // South
            fillBox(x1, y + h, z1, x1, y + h, z2, wallMat); // West
            fillBox(x2, y + h, z1, x2, y + h, z2, wallMat); // East
        }
        // Ceiling
        fillFloor(x1, z1, x2, z2, y + height + 1, ceilMat);
        // Interior clear
        for (int h = 1; h <= height; h++)
            fillBox(x1 + 1, y + h, z1 + 1, x2 - 1, y + h, z2 - 1, Material.AIR);
        // Door (north wall center)
        int midX = (x1 + x2) / 2;
        setBlock(midX, y + 1, z1, Material.AIR);
        setBlock(midX, y + 2, z1, Material.AIR);
        // Chandelier
        int midZ = (z1 + z2) / 2;
        chandelier(midX, y + height + 1, midZ, 2);
    }

    // ══════════════════════════════════════════════════════════════════
    // CHEST, BOSS SPAWN, SURFACE
    // ══════════════════════════════════════════════════════════════════

    public Location placeChest(int rx, int ry, int rz) {
        setBlock(rx, ry, rz, Material.CHEST);
        Location loc = new Location(world, origin.getBlockX() + rx, origin.getBlockY() + ry, origin.getBlockZ() + rz);
        chestLocations.add(loc);
        return loc;
    }

    public void setBossSpawn(int rx, int ry, int rz) {
        bossSpawnLocation = new Location(world, origin.getBlockX() + rx + 0.5,
                origin.getBlockY() + ry, origin.getBlockZ() + rz + 0.5);
    }

    public int getSurfaceY(int rx, int rz) {
        return world.getHighestBlockYAt(origin.getBlockX() + rx, origin.getBlockZ() + rz);
    }

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

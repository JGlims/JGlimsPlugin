package com.jglims.plugin.abyss;

import com.jglims.plugin.JGlimsPlugin;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

/**
 * Builds the Abyssal Citadel — the endgame mega-structure at the center of the Abyss.
 *
 * Layout (centered at 0, surfaceY, 0):
 * - Main Keep: 41x41 base, 40 blocks tall, 3 floors
 * - 4 Corner Towers: 9x9 base, 55 blocks tall
 * - Entrance Gate with grand staircase
 * - Arena on the roof (25x25) for the Dragon fight
 * - Interior: hallways, rooms with loot chests, spawner rooms
 * - Outer walls connecting the towers (perimeter 61x61)
 *
 * Total footprint: ~61x61 blocks, height ~60 blocks above ground
 * Materials: Deepslate Bricks, Polished Deepslate, Purpur, Obsidian, End Stone Bricks
 */
public class AbyssCitadelBuilder {

    private final JGlimsPlugin plugin;
    private final Random random = new Random();

    // Palette
    private static final Material WALL = Material.DEEPSLATE_BRICKS;
    private static final Material WALL2 = Material.POLISHED_DEEPSLATE;
    private static final Material FLOOR = Material.DEEPSLATE_TILES;
    private static final Material PILLAR = Material.POLISHED_BLACKSTONE_BRICKS;
    private static final Material ACCENT = Material.PURPUR_BLOCK;
    private static final Material ACCENT2 = Material.PURPUR_PILLAR;
    private static final Material DARK = Material.OBSIDIAN;
    private static final Material LIGHT = Material.END_ROD;
    private static final Material GLASS = Material.MAGENTA_STAINED_GLASS;
    private static final Material BRICK = Material.END_STONE_BRICKS;
    private static final Material SLAB_TOP = Material.DEEPSLATE_BRICK_SLAB;
    private static final Material STAIR = Material.DEEPSLATE_BRICK_STAIRS;
    private static final Material FENCE = Material.NETHER_BRICK_FENCE;
    private static final Material CRYING = Material.CRYING_OBSIDIAN;
    private static final Material BEACON_BLOCK = Material.BEACON;

    public AbyssCitadelBuilder(JGlimsPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Build the entire citadel. Called once after world creation.
     * Runs synchronously in chunks to avoid lag (scheduled over multiple ticks).
     */
    public void buildCitadel(World world) {
        // Find surface Y at center
        int baseY = world.getHighestBlockYAt(0, 0);
        if (baseY < 30) baseY = 55; // fallback
        baseY += 1; // build on top of terrain

        plugin.getLogger().info("Building Abyssal Citadel at Y=" + baseY + "...");
        final int y = baseY;

        // Build everything synchronously (world is fresh, no players yet)
        buildFoundation(world, y);
        buildOuterWalls(world, y);
        buildCornerTowers(world, y);
        buildMainKeep(world, y);
        buildArena(world, y);
        buildInteriorRooms(world, y);
        buildGateEntrance(world, y);
        buildDecorations(world, y);

        plugin.getLogger().info("Abyssal Citadel complete! Base Y=" + y
                + ", footprint 61x61, height ~60 blocks");
    }

    // ── Foundation: clear and flatten the center area ──
    private void buildFoundation(World world, int baseY) {
        for (int x = -32; x <= 32; x++) {
            for (int z = -32; z <= 32; z++) {
                // Clear above
                for (int y = baseY; y < baseY + 65; y++) {
                    world.getBlockAt(x, y, z).setType(Material.AIR);
                }
                // Foundation slab
                world.getBlockAt(x, baseY - 1, z).setType(FLOOR);
                world.getBlockAt(x, baseY - 2, z).setType(WALL);
            }
        }
    }

    // ── Outer walls: 61x61 perimeter, 12 blocks tall ──
    private void buildOuterWalls(World world, int baseY) {
        int half = 30; // -30 to +30
        int wallH = 12;

        for (int y = baseY; y < baseY + wallH; y++) {
            for (int i = -half; i <= half; i++) {
                // North wall (z = -half)
                setWallBlock(world, i, y, -half, baseY, wallH);
                // South wall (z = +half)
                setWallBlock(world, i, y, half, baseY, wallH);
                // West wall (x = -half)
                setWallBlock(world, -half, y, i, baseY, wallH);
                // East wall (x = +half)
                setWallBlock(world, half, y, i, baseY, wallH);
            }
        }

        // Battlements on top
        for (int i = -half; i <= half; i += 2) {
            world.getBlockAt(i, baseY + wallH, -half).setType(WALL);
            world.getBlockAt(i, baseY + wallH, half).setType(WALL);
            world.getBlockAt(-half, baseY + wallH, i).setType(WALL);
            world.getBlockAt(half, baseY + wallH, i).setType(WALL);
        }

        // Torches on battlements
        for (int i = -half; i <= half; i += 6) {
            world.getBlockAt(i, baseY + wallH + 1, -half).setType(LIGHT);
            world.getBlockAt(i, baseY + wallH + 1, half).setType(LIGHT);
            world.getBlockAt(-half, baseY + wallH + 1, i).setType(LIGHT);
            world.getBlockAt(half, baseY + wallH + 1, i).setType(LIGHT);
        }
    }

    private void setWallBlock(World world, int x, int y, int z, int baseY, int wallH) {
        int relY = y - baseY;
        Material mat;
        if (relY == 0 || relY == wallH - 1) mat = WALL2; // base and top trim
        else if (relY % 4 == 0) mat = ACCENT; // purple stripe every 4 blocks
        else mat = WALL;
        world.getBlockAt(x, y, z).setType(mat);
    }

    // ── 4 Corner Towers: 9x9 base, 55 blocks tall ──
    private void buildCornerTowers(World world, int baseY) {
        int[][] corners = {{-26, -26}, {26, -26}, {-26, 26}, {26, 26}};
        int towerR = 4; // radius from center of tower
        int towerH = 55;

        for (int[] corner : corners) {
            int tcx = corner[0];
            int tcz = corner[1];

            for (int y = baseY; y < baseY + towerH; y++) {
                int relY = y - baseY;
                for (int dx = -towerR; dx <= towerR; dx++) {
                    for (int dz = -towerR; dz <= towerR; dz++) {
                        double dist = Math.sqrt(dx * dx + dz * dz);
                        // Circular tower
                        if (dist <= towerR + 0.5) {
                            if (dist >= towerR - 0.5) {
                                // Wall
                                Material mat = relY % 6 == 0 ? ACCENT : WALL;
                                world.getBlockAt(tcx + dx, y, tcz + dz).setType(mat);
                            } else if (relY % 15 == 0) {
                                // Floor every 15 blocks
                                world.getBlockAt(tcx + dx, y, tcz + dz).setType(FLOOR);
                            }
                            // Windows every 5 blocks on walls
                            if (dist >= towerR - 0.5 && relY % 5 == 3 && Math.abs(dx) + Math.abs(dz) == towerR) {
                                world.getBlockAt(tcx + dx, y, tcz + dz).setType(GLASS);
                            }
                        }
                    }
                }
            }

            // Pointed roof (cone)
            for (int h = 0; h < 10; h++) {
                double r = towerR * (1.0 - h / 10.0);
                for (int dx = (int) -r; dx <= (int) r; dx++) {
                    for (int dz = (int) -r; dz <= (int) r; dz++) {
                        if (Math.sqrt(dx * dx + dz * dz) <= r) {
                            world.getBlockAt(tcx + dx, baseY + towerH + h, tcz + dz).setType(ACCENT);
                        }
                    }
                }
            }
            // Spire on top
            for (int h = 0; h < 5; h++) {
                world.getBlockAt(tcx, baseY + towerH + 10 + h, tcz).setType(ACCENT2);
            }
            world.getBlockAt(tcx, baseY + towerH + 15, tcz).setType(LIGHT);

            // Loot chest in each tower at ground floor
            Block chestBlock = world.getBlockAt(tcx, baseY + 1, tcz);
            chestBlock.setType(Material.CHEST);
            if (chestBlock.getState() instanceof Chest chest) {
                fillLootChest(chest.getInventory(), 2);
            }
        }
    }

    // ── Main Keep: 41x41, 40 tall, 3 floors ──
    private void buildMainKeep(World world, int baseY) {
        int half = 20; // -20 to +20
        int keepH = 40;
        int floorH = 13; // floor every 13 blocks

        // Outer walls of the keep
        for (int y = baseY; y < baseY + keepH; y++) {
            int relY = y - baseY;
            for (int i = -half; i <= half; i++) {
                Material mat = relY % 5 == 0 ? ACCENT : WALL;
                world.getBlockAt(i, y, -half).setType(mat);
                world.getBlockAt(i, y, half).setType(mat);
                world.getBlockAt(-half, y, i).setType(mat);
                world.getBlockAt(half, y, i).setType(mat);
            }

            // Floors
            if (relY > 0 && relY % floorH == 0) {
                for (int x = -half + 1; x < half; x++) {
                    for (int z = -half + 1; z < half; z++) {
                        world.getBlockAt(x, y, z).setType(FLOOR);
                    }
                }
            }
        }

        // Windows (3-tall windows on each face)
        for (int face = 0; face < 4; face++) {
            for (int w = -half + 4; w <= half - 4; w += 6) {
                for (int wy = 0; wy < 3; wy++) {
                    for (int floor = 0; floor < 3; floor++) {
                        int fy = baseY + 3 + floor * floorH + wy;
                        switch (face) {
                            case 0 -> world.getBlockAt(w, fy, -half).setType(GLASS);
                            case 1 -> world.getBlockAt(w, fy, half).setType(GLASS);
                            case 2 -> world.getBlockAt(-half, fy, w).setType(GLASS);
                            case 3 -> world.getBlockAt(half, fy, w).setType(GLASS);
                        }
                    }
                }
            }
        }

        // Central grand pillar (4x4, full height)
        for (int y = baseY; y < baseY + keepH; y++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    world.getBlockAt(dx, y, dz).setType(PILLAR);
                }
            }
        }

        // Interior lighting
        for (int floor = 0; floor < 3; floor++) {
            int fy = baseY + 1 + floor * floorH;
            for (int x = -half + 3; x < half; x += 5) {
                for (int z = -half + 3; z < half; z += 5) {
                    world.getBlockAt(x, fy + floorH - 2, z).setType(LIGHT);
                }
            }
        }

        // Stairs between floors (spiral in corner)
        for (int floor = 0; floor < 2; floor++) {
            int startY = baseY + 1 + floor * floorH;
            int sx = -half + 2;
            int sz = -half + 2;
            for (int step = 0; step < floorH; step++) {
                int stairX = sx + (step % 4 < 2 ? step % 4 : 1);
                int stairZ = sz + (step % 4 >= 2 ? step % 4 - 2 : 0);
                world.getBlockAt(sx + step % 3, startY + step, sz + step / 3 % 3).setType(STAIR);
                // Clear above for headroom
                world.getBlockAt(sx + step % 3, startY + step + 1, sz + step / 3 % 3).setType(Material.AIR);
                world.getBlockAt(sx + step % 3, startY + step + 2, sz + step / 3 % 3).setType(Material.AIR);
            }
        }
    }

    // ── Arena on the roof: 25x25 fighting area ──
    private void buildArena(World world, int baseY) {
        int arenaY = baseY + 40; // top of keep
        int arenaR = 12;

        // Arena floor
        for (int x = -arenaR; x <= arenaR; x++) {
            for (int z = -arenaR; z <= arenaR; z++) {
                double dist = Math.sqrt(x * x + z * z);
                if (dist <= arenaR) {
                    Material mat = dist < 4 ? CRYING : (dist < 8 ? DARK : FLOOR);
                    world.getBlockAt(x, arenaY, z).setType(mat);
                }
            }
        }

        // Arena walls (3 blocks high, open top)
        for (int x = -arenaR; x <= arenaR; x++) {
            for (int z = -arenaR; z <= arenaR; z++) {
                double dist = Math.sqrt(x * x + z * z);
                if (dist >= arenaR - 0.5 && dist <= arenaR + 0.5) {
                    for (int dy = 1; dy <= 3; dy++) {
                        world.getBlockAt(x, arenaY + dy, z).setType(WALL);
                    }
                    // Battlements
                    if ((x + z) % 2 == 0) {
                        world.getBlockAt(x, arenaY + 4, z).setType(WALL);
                    }
                }
            }
        }

        // 4 beacons at cardinal points of the arena
        int[][] beaconPos = {{0, -arenaR + 2}, {0, arenaR - 2}, {-arenaR + 2, 0}, {arenaR - 2, 0}};
        for (int[] bp : beaconPos) {
            world.getBlockAt(bp[0], arenaY + 1, bp[1]).setType(LIGHT);
            world.getBlockAt(bp[0], arenaY + 4, bp[1]).setType(LIGHT);
        }

        // Dragon perch (obsidian pillar at center)
        for (int dy = 1; dy <= 8; dy++) {
            world.getBlockAt(0, arenaY + dy, 0).setType(DARK);
        }
        world.getBlockAt(0, arenaY + 9, 0).setType(CRYING);
    }

    // ── Interior Rooms with loot ──
    private void buildInteriorRooms(World world, int baseY) {
        // Floor 1 rooms (y = baseY+1 to baseY+12)
        buildRoom(world, -18, baseY + 1, -18, 8, 6, 8, "Armory", 3);
        buildRoom(world, 10, baseY + 1, -18, 8, 6, 8, "Vault", 3);
        buildRoom(world, -18, baseY + 1, 10, 8, 6, 8, "Library", 2);
        buildRoom(world, 10, baseY + 1, 10, 8, 6, 8, "Treasury", 3);

        // Floor 2 rooms (y = baseY+14 to baseY+25)
        buildRoom(world, -18, baseY + 14, -18, 8, 6, 8, "Spawner Hall", 4);
        buildRoom(world, 10, baseY + 14, -18, 8, 6, 8, "Alchemy Lab", 2);
        buildRoom(world, -18, baseY + 14, 10, 8, 6, 8, "Throne Room", 3);
        buildRoom(world, 10, baseY + 14, 10, 8, 6, 8, "War Room", 2);

        // Floor 3 rooms (y = baseY+27 to baseY+38)
        buildRoom(world, -18, baseY + 27, -18, 8, 6, 8, "Observatory", 2);
        buildRoom(world, 10, baseY + 27, -18, 8, 6, 8, "Dragon Hoard", 4);
        buildRoom(world, -15, baseY + 27, -5, 30, 10, 10, "Grand Hall", 3);
    }

    private void buildRoom(World world, int ox, int oy, int oz, int w, int h, int d, String name, int lootTier) {
        // Floor and ceiling
        for (int x = ox; x < ox + w; x++) {
            for (int z = oz; z < oz + d; z++) {
                world.getBlockAt(x, oy, z).setType(FLOOR);
                world.getBlockAt(x, oy + h, z).setType(FLOOR);
                // Clear interior
                for (int y = oy + 1; y < oy + h; y++) {
                    world.getBlockAt(x, y, z).setType(Material.AIR);
                }
            }
        }

        // Walls (only if not already a keep wall)
        for (int y = oy; y <= oy + h; y++) {
            for (int x = ox; x < ox + w; x++) {
                world.getBlockAt(x, y, oz).setType(WALL);
                world.getBlockAt(x, y, oz + d - 1).setType(WALL);
            }
            for (int z = oz; z < oz + d; z++) {
                world.getBlockAt(ox, y, z).setType(WALL);
                world.getBlockAt(ox + w - 1, y, z).setType(WALL);
            }
        }

        // Door (clear 2x1 in one wall)
        world.getBlockAt(ox + w / 2, oy + 1, oz).setType(Material.AIR);
        world.getBlockAt(ox + w / 2, oy + 2, oz).setType(Material.AIR);

        // Lighting
        world.getBlockAt(ox + 1, oy + h - 1, oz + 1).setType(LIGHT);
        world.getBlockAt(ox + w - 2, oy + h - 1, oz + d - 2).setType(LIGHT);

        // Loot chest
        Block chestBlock = world.getBlockAt(ox + w / 2, oy + 1, oz + d - 2);
        chestBlock.setType(Material.CHEST);
        if (chestBlock.getState() instanceof Chest chest) {
            fillLootChest(chest.getInventory(), lootTier);
        }

        // Spawner in spawner rooms
        if (name.contains("Spawner")) {
            Block spawnerBlock = world.getBlockAt(ox + w / 2, oy + 1, oz + d / 2);
            spawnerBlock.setType(Material.SPAWNER);
            if (spawnerBlock.getState() instanceof CreatureSpawner spawner) {
                spawner.setSpawnedType(EntityType.ENDERMAN);
                spawner.setDelay(200);
                spawner.setMaxNearbyEntities(4);
                spawner.setSpawnRange(4);
                spawner.update();
            }
        }
    }

    // ── Gate Entrance on south face ──
    private void buildGateEntrance(World world, int baseY) {
        // Grand entrance at z=30 (south outer wall), 5 wide x 7 tall
        for (int x = -2; x <= 2; x++) {
            for (int y = baseY; y < baseY + 7; y++) {
                world.getBlockAt(x, y, 30).setType(Material.AIR);
            }
        }
        // Archway
        for (int x = -3; x <= 3; x++) {
            world.getBlockAt(x, baseY + 7, 30).setType(ACCENT);
        }
        world.getBlockAt(-3, baseY + 7, 30).setType(ACCENT2);
        world.getBlockAt(3, baseY + 7, 30).setType(ACCENT2);

        // Flanking pillars
        for (int y = baseY; y < baseY + 10; y++) {
            world.getBlockAt(-3, y, 31).setType(PILLAR);
            world.getBlockAt(3, y, 31).setType(PILLAR);
        }
        world.getBlockAt(-3, baseY + 10, 31).setType(LIGHT);
        world.getBlockAt(3, baseY + 10, 31).setType(LIGHT);

        // Path leading to gate
        for (int z = 31; z <= 40; z++) {
            for (int x = -2; x <= 2; x++) {
                world.getBlockAt(x, baseY - 1, z).setType(FLOOR);
            }
        }

        // Keep entrance at z=20
        for (int x = -2; x <= 2; x++) {
            for (int y = baseY; y < baseY + 6; y++) {
                world.getBlockAt(x, y, 20).setType(Material.AIR);
            }
        }
        world.getBlockAt(-3, baseY + 6, 20).setType(ACCENT);
        world.getBlockAt(3, baseY + 6, 20).setType(ACCENT);
        for (int x = -2; x <= 2; x++) {
            world.getBlockAt(x, baseY + 6, 20).setType(ACCENT);
        }
    }

    // ── Decorations: banners, ambient blocks ──
    private void buildDecorations(World world, int baseY) {
        // Crying obsidian accents on outer wall corners
        int half = 30;
        for (int y = baseY; y < baseY + 12; y += 3) {
            world.getBlockAt(-half, y, -half).setType(CRYING);
            world.getBlockAt(half, y, -half).setType(CRYING);
            world.getBlockAt(-half, y, half).setType(CRYING);
            world.getBlockAt(half, y, half).setType(CRYING);
        }

        // Fence posts along walkway
        for (int z = 31; z <= 40; z += 2) {
            world.getBlockAt(-3, baseY, z).setType(FENCE);
            world.getBlockAt(3, baseY, z).setType(FENCE);
        }
    }

    // ── Loot generation ──
    private void fillLootChest(Inventory inv, int tier) {
        // Tier 1 = basic, 4 = amazing
        int slots = 3 + tier * 2;
        for (int i = 0; i < slots; i++) {
            int slot = random.nextInt(27);
            ItemStack item = generateLootItem(tier);
            if (item != null) inv.setItem(slot, item);
        }
    }

    private ItemStack generateLootItem(int tier) {
        return switch (tier) {
            case 1 -> switch (random.nextInt(5)) {
                case 0 -> new ItemStack(Material.IRON_INGOT, 4 + random.nextInt(8));
                case 1 -> new ItemStack(Material.GOLD_INGOT, 3 + random.nextInt(6));
                case 2 -> new ItemStack(Material.DIAMOND, 1 + random.nextInt(3));
                case 3 -> new ItemStack(Material.ENDER_PEARL, 2 + random.nextInt(4));
                default -> new ItemStack(Material.EXPERIENCE_BOTTLE, 4 + random.nextInt(8));
            };
            case 2 -> switch (random.nextInt(5)) {
                case 0 -> new ItemStack(Material.DIAMOND, 3 + random.nextInt(6));
                case 1 -> new ItemStack(Material.EMERALD, 5 + random.nextInt(10));
                case 2 -> new ItemStack(Material.GOLDEN_APPLE, 2 + random.nextInt(3));
                case 3 -> new ItemStack(Material.NETHERITE_SCRAP, 1 + random.nextInt(2));
                default -> new ItemStack(Material.EXPERIENCE_BOTTLE, 8 + random.nextInt(16));
            };
            case 3 -> switch (random.nextInt(5)) {
                case 0 -> new ItemStack(Material.DIAMOND, 6 + random.nextInt(10));
                case 1 -> new ItemStack(Material.NETHERITE_INGOT, 1 + random.nextInt(2));
                case 2 -> new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 1);
                case 3 -> new ItemStack(Material.NETHER_STAR, 1);
                default -> new ItemStack(Material.TOTEM_OF_UNDYING, 1);
            };
            case 4 -> switch (random.nextInt(5)) {
                case 0 -> new ItemStack(Material.NETHERITE_INGOT, 2 + random.nextInt(3));
                case 1 -> new ItemStack(Material.NETHER_STAR, 1 + random.nextInt(2));
                case 2 -> new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 1 + random.nextInt(2));
                case 3 -> new ItemStack(Material.DIAMOND_BLOCK, 2 + random.nextInt(4));
                default -> new ItemStack(Material.TOTEM_OF_UNDYING, 1);
            };
            default -> new ItemStack(Material.DIAMOND, 1);
        };
    }
}
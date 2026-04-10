package com.jglims.plugin.structures;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;

/**
 * Static utility class containing all building methods for dimension structures:
 * Nether, End, Abyss, Aether, Lunar, and Jurassic.
 * Each method receives a {@link StructureBuilder} positioned at the structure origin.
 */
public final class DimensionStructureBuilders {

    private DimensionStructureBuilders() {}

    // ══════════════════════════════════════════════════════════════════════
    //  NETHER STRUCTURES (9)
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Crimson Citadel — Nether brick fortress with crimson wood accents,
     * lava moat, drawbridge, throne room, prison cells, guard posts.
     */
    public static void buildCrimsonCitadel(StructureBuilder b) {
        // Lava moat (ring around the fortress)
        for (int x = -19; x <= 19; x++)
            for (int z = -19; z <= 19; z++) {
                double dist = Math.sqrt(x * x + z * z);
                if (dist >= 16 && dist <= 19) b.setBlock(x, 0, z, Material.LAVA);
            }

        // Foundation platform
        b.fillBox(-15, 0, -15, 15, 0, 15, Material.POLISHED_BLACKSTONE_BRICKS);

        // Outer walls — nether brick with crimson accents
        b.fillWalls(-15, 1, -15, 15, 12, 15, Material.NETHER_BRICKS);
        b.hollowBox(-15, 1, -15, 15, 12, 15);

        // Crimson trim bands at y=4 and y=8
        for (int y : new int[]{4, 8}) {
            b.fillBox(-15, y, -15, 15, y, -15, Material.CRIMSON_PLANKS);
            b.fillBox(-15, y, 15, 15, y, 15, Material.CRIMSON_PLANKS);
            b.fillBox(-15, y, -15, -15, y, 15, Material.CRIMSON_PLANKS);
            b.fillBox(15, y, -15, 15, y, 15, Material.CRIMSON_PLANKS);
        }

        // Battlements on top
        b.battlements(-15, 13, -15, 15, -15, Material.NETHER_BRICKS);
        b.battlements(-15, 13, 15, 15, 15, Material.NETHER_BRICKS);
        b.battlements(-15, 13, -15, -15, 15, Material.NETHER_BRICKS);
        b.battlements(15, 13, -15, 15, 15, Material.NETHER_BRICKS);

        // Floor — crimson planks with blackstone border
        b.fillFloor(-14, -14, 14, 14, 0, Material.CRIMSON_PLANKS);
        b.fillBox(-14, 0, -14, -12, 0, 14, Material.POLISHED_BLACKSTONE);
        b.fillBox(12, 0, -14, 14, 0, 14, Material.POLISHED_BLACKSTONE);

        // Corner towers (4) with soul lanterns
        int[][] corners = {{-15, -15}, {15, -15}, {-15, 15}, {15, 15}};
        for (int[] c : corners) {
            b.fillBox(c[0] - 2, 0, c[1] - 2, c[0] + 2, 15, c[1] + 2, Material.RED_NETHER_BRICKS);
            b.fillBox(c[0] - 1, 1, c[1] - 1, c[0] + 1, 14, c[1] + 1, Material.AIR);
            b.setBlock(c[0], 14, c[1], Material.SOUL_LANTERN);
            b.spiralStaircase(c[0], 1, 14, c[1], 1, Material.NETHER_BRICK_STAIRS, Material.NETHER_BRICKS);
        }

        // Drawbridge (south entrance across moat)
        b.fillBox(-2, 0, -19, 2, 0, -15, Material.CRIMSON_PLANKS);
        b.fillBox(-2, 1, -15, 2, 4, -15, Material.AIR); // gate opening
        b.archDoorway(0, 1, -15, 4, 5, Material.NETHER_BRICKS);

        // Interior pillars — polished blackstone
        for (int x = -10; x <= 10; x += 5)
            for (int z = -10; z <= 10; z += 10) {
                b.pillar(x, 1, 11, z, Material.POLISHED_BLACKSTONE_BRICKS);
                b.setBlock(x, 11, z, Material.SOUL_LANTERN);
            }

        // Throne room (north section)
        b.fillBox(-6, 0, 8, 6, 0, 14, Material.RED_NETHER_BRICKS);
        b.fillBox(-1, 1, 12, 1, 1, 14, Material.CRIMSON_PLANKS);
        b.setBlock(0, 2, 13, Material.GOLD_BLOCK);
        b.setBlock(0, 3, 13, Material.GOLD_BLOCK);
        b.setBlock(-1, 3, 13, Material.CRIMSON_FENCE);
        b.setBlock(1, 3, 13, Material.CRIMSON_FENCE);
        b.setBlock(0, 1, 14, Material.CRIMSON_STAIRS);
        // Throne banners
        b.setBlock(-3, 5, 14, Material.RED_BANNER);
        b.setBlock(3, 5, 14, Material.RED_BANNER);

        // Prison cells (east wing)
        for (int cell = 0; cell < 3; cell++) {
            int cz = -10 + cell * 5;
            b.fillBox(8, 0, cz, 13, 4, cz + 3, Material.NETHER_BRICKS);
            b.fillBox(9, 1, cz + 1, 12, 3, cz + 2, Material.AIR);
            b.setBlock(8, 1, cz + 1, Material.IRON_BARS);
            b.setBlock(8, 2, cz + 1, Material.IRON_BARS);
            b.setBlock(8, 3, cz + 1, Material.IRON_BARS);
            b.setBlock(8, 1, cz + 2, Material.IRON_BARS);
            b.setBlock(8, 2, cz + 2, Material.IRON_BARS);
            b.setBlock(8, 3, cz + 2, Material.IRON_BARS);
        }

        // Wither skeleton guard posts (raised platforms)
        b.fillBox(-13, 1, -13, -11, 2, -11, Material.POLISHED_BLACKSTONE);
        b.setBlock(-12, 3, -12, Material.SOUL_TORCH);
        b.fillBox(11, 1, -13, 13, 2, -11, Material.POLISHED_BLACKSTONE);
        b.setBlock(12, 3, -12, Material.SOUL_TORCH);

        // Wall lighting
        b.wallLighting(-14, 6, -15, 14, -15, 4, Material.SOUL_LANTERN);
        b.wallLighting(-14, 6, 15, 14, 15, 4, Material.SOUL_LANTERN);

        // Ceiling detail
        b.fillFloor(-14, -14, 14, 14, 12, Material.NETHER_BRICKS);
        b.chandelier(0, 12, 0, 3);
        b.chandelier(-8, 12, -8, 2);
        b.chandelier(8, 12, -8, 2);

        b.placeChest(0, 1, 0);
        b.placeChest(11, 1, -12);
        b.placeChest(-11, 1, 12);
        b.placeChest(5, 1, 13);
        b.setBossSpawn(0, 1, 8);
    }

    /**
     * Soul Sanctum — Soul sand temple with ghostly blue glass, meditation rooms,
     * sealed vault with soul fire perimeter.
     */
    public static void buildSoulSanctum(StructureBuilder b) {
        // Foundation
        b.fillBox(-14, 0, -14, 14, 0, 14, Material.SOUL_SOIL);

        // Outer walls — soul sand with deepslate frame
        b.fillWalls(-14, 1, -14, 14, 10, 14, Material.SOUL_SAND);
        b.hollowBox(-14, 1, -14, 14, 10, 14);

        // Deepslate edge pillars
        for (int[] c : new int[][]{{-14, -14}, {14, -14}, {-14, 14}, {14, 14}})
            b.pillar(c[0], 1, 10, c[1], Material.POLISHED_DEEPSLATE);

        // Floor pattern — alternating soul soil and blackstone
        for (int x = -13; x <= 13; x++)
            for (int z = -13; z <= 13; z++)
                b.setBlock(x, 0, z, (x + z) % 2 == 0 ? Material.SOUL_SOIL : Material.BLACKSTONE);

        // Blue stained glass windows every 4 blocks
        for (int i = -10; i <= 10; i += 4) {
            for (int y = 4; y <= 7; y++) {
                b.setBlock(i, y, -14, Material.LIGHT_BLUE_STAINED_GLASS);
                b.setBlock(i, y, 14, Material.LIGHT_BLUE_STAINED_GLASS);
                b.setBlock(-14, y, i, Material.LIGHT_BLUE_STAINED_GLASS);
                b.setBlock(14, y, i, Material.LIGHT_BLUE_STAINED_GLASS);
            }
        }

        // Soul fire perimeter (ring inside)
        b.circle(0, 1, 0, 11, Material.SOUL_CAMPFIRE);

        // Central altar — raised obsidian platform with soul fire
        b.fillBox(-3, 0, -3, 3, 2, 3, Material.POLISHED_BLACKSTONE);
        b.fillBox(-2, 2, -2, 2, 2, 2, Material.CRYING_OBSIDIAN);
        b.setBlock(0, 3, 0, Material.SOUL_CAMPFIRE);
        b.setBlock(-2, 3, -2, Material.SOUL_LANTERN);
        b.setBlock(2, 3, -2, Material.SOUL_LANTERN);
        b.setBlock(-2, 3, 2, Material.SOUL_LANTERN);
        b.setBlock(2, 3, 2, Material.SOUL_LANTERN);

        // Meditation rooms (4 alcoves in corners)
        int[][] alcoves = {{-12, -12}, {12, -12}, {-12, 12}, {12, 12}};
        for (int[] a : alcoves) {
            b.fillBox(a[0] - 2, 0, a[1] - 2, a[0] + 2, 5, a[1] + 2, Material.DEEPSLATE_BRICKS);
            b.fillBox(a[0] - 1, 1, a[1] - 1, a[0] + 1, 4, a[1] + 1, Material.AIR);
            b.setBlock(a[0], 0, a[1], Material.SOUL_SOIL);
            b.setBlock(a[0], 1, a[1], Material.SOUL_CAMPFIRE);
            b.setBlock(a[0], 4, a[1], Material.SOUL_LANTERN);
        }

        // Sealed vault (underground chamber)
        b.fillBox(-8, -6, -8, 8, -1, 8, Material.DEEPSLATE_BRICKS);
        b.fillBox(-7, -5, -7, 7, -1, 7, Material.AIR);
        b.fillFloor(-7, -7, 7, 7, -6, Material.SOUL_SOIL);
        b.circle(0, -5, 0, 5, Material.SOUL_CAMPFIRE);
        // Vault entrance (stairs down)
        for (int s = 0; s < 6; s++) b.setBlock(0, -s, -13 + s, Material.DEEPSLATE_BRICK_STAIRS);

        // Ceiling — dome shape using soul sand and lanterns
        b.dome(0, 10, 0, 10, Material.SOUL_SAND);
        b.setBlock(0, 19, 0, Material.SOUL_LANTERN);

        // Ghostly atmosphere — scattered soul torches
        b.wallLighting(-13, 5, -14, 13, -14, 5, Material.SOUL_TORCH);
        b.wallLighting(-13, 5, 14, 13, 14, 5, Material.SOUL_TORCH);

        b.placeChest(0, -5, 0);
        b.placeChest(5, 1, 5);
        b.placeChest(-5, 1, -5);
        b.setBossSpawn(0, 3, 5);
    }

    /**
     * Basalt Spire — Massive 50-block basalt tower with interior rooms,
     * lava waterfalls, polished basalt trim, open observation top.
     */
    public static void buildBasaltSpire(StructureBuilder b) {
        // Build the main tower — tapering from radius 9 at base to 4 at top
        for (int y = 0; y <= 50; y++) {
            int r = Math.max(4, 9 - (y * 5 / 50));
            b.circle(0, y, 0, r, Material.BASALT);
            // Polished basalt trim every 10 blocks
            if (y % 10 == 0) b.circle(0, y, 0, r, Material.POLISHED_BASALT);
        }

        // Clear interior (spiral rooms)
        for (int y = 1; y <= 48; y++) {
            int r = Math.max(3, 8 - (y * 5 / 50));
            b.filledCircle(0, y, 0, r - 1, Material.AIR);
        }

        // Floors at different heights (rooms)
        int[] roomFloors = {0, 10, 20, 30, 40};
        for (int floor : roomFloors) {
            int r = Math.max(3, 8 - (floor * 5 / 50));
            b.filledCircle(0, floor, 0, r - 1, Material.POLISHED_BASALT);
        }

        // Spiral staircase connecting floors
        b.spiralStaircase(0, 1, 48, 0, 3, Material.POLISHED_BLACKSTONE_BRICK_STAIRS, Material.BASALT);

        // Lava waterfalls on exterior (4 sides)
        for (int y = 45; y >= 5; y--) {
            b.setBlock(0, y, 9 - (y * 5 / 50), Material.LAVA);
            b.setBlock(0, y, -(9 - (y * 5 / 50)), Material.LAVA);
            b.setBlock(9 - (y * 5 / 50), y, 0, Material.LAVA);
            b.setBlock(-(9 - (y * 5 / 50)), y, 0, Material.LAVA);
        }

        // Room furnishing — ground floor: entry hall
        b.fillBox(-2, 1, -9, 2, 4, -9, Material.AIR); // entrance
        b.archDoorway(0, 1, -9, 4, 5, Material.POLISHED_BASALT);
        b.setBlock(-3, 3, -3, Material.SOUL_LANTERN);
        b.setBlock(3, 3, -3, Material.SOUL_LANTERN);

        // Room at y=10: armory
        b.setBlock(-2, 11, 0, Material.SMITHING_TABLE);
        b.setBlock(2, 11, 0, Material.ANVIL);
        b.setBlock(0, 11, -2, Material.GRINDSTONE);
        b.wallLighting(-3, 14, 0, 3, 0, 3, Material.SOUL_LANTERN);

        // Room at y=20: library
        b.bookshelfWall(-3, 21, -3, 3, 23);
        b.setBlock(0, 21, 2, Material.LECTERN);
        b.chandelier(0, 29, 0, 2);

        // Room at y=30: treasury
        b.setBlock(-1, 31, -1, Material.GOLD_BLOCK);
        b.setBlock(1, 31, -1, Material.GOLD_BLOCK);
        b.setBlock(0, 31, 0, Material.MAGMA_BLOCK);

        // Room at y=40: war room
        b.table(0, 41, 0);
        b.chair(-1, 41, 0, Material.POLISHED_BLACKSTONE_BRICK_STAIRS, BlockFace.EAST);
        b.chair(1, 41, 0, Material.POLISHED_BLACKSTONE_BRICK_STAIRS, BlockFace.WEST);

        // Open top observation platform
        b.filledCircle(0, 50, 0, 5, Material.POLISHED_BASALT);
        b.circle(0, 51, 0, 5, Material.BASALT);
        b.battlements(0, 52, 0, 5, 0, Material.BASALT); // simplified ring battlement

        // Magma accents scattered on exterior
        b.scatter(-9, 0, -9, 9, 50, 9, Material.BASALT, Material.MAGMA_BLOCK, 0.03);

        // Deepslate base reinforcement
        b.filledCircle(0, -1, 0, 10, Material.DEEPSLATE_BRICKS);

        b.placeChest(0, 31, 1);
        b.placeChest(0, 11, 2);
        b.placeChest(0, 51, 0);
        b.setBossSpawn(0, 51, 2);
    }

    /**
     * Nether Dungeon — Underground cell blocks, torture devices, warden room,
     * treasure vault, lava traps.
     */
    public static void buildNetherDungeon(StructureBuilder b) {
        // Underground shell
        b.fillBox(-14, -2, -10, 14, 8, 10, Material.NETHER_BRICKS);
        b.fillBox(-13, -1, -9, 13, 7, 9, Material.AIR);
        b.fillFloor(-13, -9, 13, 9, -2, Material.POLISHED_BLACKSTONE);

        // Surface entrance — trapdoor style
        b.fillBox(-2, 8, -2, 2, 10, 2, Material.NETHER_BRICKS);
        b.fillBox(-1, 8, -1, 1, 10, 1, Material.AIR);
        b.setBlock(0, 8, 0, Material.NETHER_BRICK_STAIRS);

        // Spiral staircase down
        b.spiralStaircase(0, -1, 8, 0, 2, Material.NETHER_BRICK_STAIRS, Material.NETHER_BRICKS);

        // Central corridor
        b.fillBox(-1, -1, -8, 1, 2, 8, Material.AIR);
        b.fillBox(-1, -2, -8, 1, -2, 8, Material.DEEPSLATE_BRICKS);

        // Cell blocks (left side)
        for (int cell = 0; cell < 4; cell++) {
            int cz = -7 + cell * 4;
            b.fillBox(-8, -2, cz, -3, 3, cz + 2, Material.NETHER_BRICKS);
            b.fillBox(-7, -1, cz, -4, 2, cz + 2, Material.AIR);
            b.fillFloor(-7, cz, -4, cz + 2, -2, Material.SOUL_SAND);
            // Iron bars as cell door
            for (int y = -1; y <= 2; y++) {
                b.setBlock(-3, y, cz, Material.IRON_BARS);
                b.setBlock(-3, y, cz + 1, Material.IRON_BARS);
                b.setBlock(-3, y, cz + 2, Material.IRON_BARS);
            }
            b.setBlock(-6, 2, cz + 1, Material.SOUL_LANTERN);
        }

        // Cell blocks (right side)
        for (int cell = 0; cell < 4; cell++) {
            int cz = -7 + cell * 4;
            b.fillBox(3, -2, cz, 8, 3, cz + 2, Material.NETHER_BRICKS);
            b.fillBox(4, -1, cz, 7, 2, cz + 2, Material.AIR);
            b.fillFloor(4, cz, 7, cz + 2, -2, Material.SOUL_SAND);
            for (int y = -1; y <= 2; y++) {
                b.setBlock(3, y, cz, Material.IRON_BARS);
                b.setBlock(3, y, cz + 1, Material.IRON_BARS);
                b.setBlock(3, y, cz + 2, Material.IRON_BARS);
            }
        }

        // Torture room (far east corner)
        b.fillBox(9, -2, -8, 13, 4, -4, Material.RED_NETHER_BRICKS);
        b.fillBox(10, -1, -7, 12, 3, -5, Material.AIR);
        b.setBlock(10, -1, -7, Material.ANVIL);      // torture device
        b.setBlock(12, -1, -7, Material.CAULDRON);
        b.setBlock(11, -1, -5, Material.IRON_BARS);
        b.setBlock(11, 0, -5, Material.IRON_BARS);
        b.setBlock(11, 3, -6, Material.SOUL_LANTERN);

        // Warden room (far north)
        b.fillBox(-8, -2, 5, -3, 4, 9, Material.POLISHED_BLACKSTONE_BRICKS);
        b.fillBox(-7, -1, 6, -4, 3, 8, Material.AIR);
        b.table(-6, -1, 7);
        b.chair(-5, -1, 7, Material.POLISHED_BLACKSTONE_BRICK_STAIRS, BlockFace.WEST);
        b.setBlock(-7, -1, 8, Material.RED_BED);
        b.setBlock(-4, 3, 7, Material.SOUL_LANTERN);

        // Lava traps — channels in the floor
        b.fillBox(-1, -2, -6, -1, -2, 6, Material.MAGMA_BLOCK);
        b.fillBox(1, -2, -6, 1, -2, 6, Material.MAGMA_BLOCK);

        // Treasure vault (far south behind locked bars)
        b.fillBox(-5, -2, -9, 5, 3, -9, Material.IRON_BARS);
        b.fillBox(-12, -2, -10, -3, 4, -10, Material.AIR);
        b.fillBox(-13, -2, -10, 13, -2, -10, Material.GOLD_BLOCK);

        b.placeChest(0, -1, -9);
        b.placeChest(-11, -1, -9);
        b.placeChest(11, -1, -9);
        b.placeChest(-6, -1, 6);
        b.setBossSpawn(0, -1, 0);
    }

    /**
     * Piglin Palace — Gold-accented blackstone palace, throne room, treasure hoard,
     * barracks, banquet hall, golden pillars.
     */
    public static void buildPiglinPalace(StructureBuilder b) {
        // Foundation
        b.fillBox(-24, 0, -24, 24, 0, 24, Material.POLISHED_BLACKSTONE);

        // Outer walls — gilded blackstone
        b.fillWalls(-24, 1, -24, 24, 12, 24, Material.GILDED_BLACKSTONE);
        b.hollowBox(-24, 1, -24, 24, 12, 24);

        // Blackstone base course and top course
        b.fillBox(-24, 1, -24, 24, 2, -24, Material.POLISHED_BLACKSTONE_BRICKS);
        b.fillBox(-24, 1, 24, 24, 2, 24, Material.POLISHED_BLACKSTONE_BRICKS);
        b.fillBox(-24, 1, -24, -24, 2, 24, Material.POLISHED_BLACKSTONE_BRICKS);
        b.fillBox(24, 1, -24, 24, 2, 24, Material.POLISHED_BLACKSTONE_BRICKS);
        b.fillBox(-24, 11, -24, 24, 12, -24, Material.POLISHED_BLACKSTONE_BRICKS);
        b.fillBox(-24, 11, 24, 24, 12, 24, Material.POLISHED_BLACKSTONE_BRICKS);
        b.fillBox(-24, 11, -24, -24, 12, 24, Material.POLISHED_BLACKSTONE_BRICKS);
        b.fillBox(24, 11, -24, 24, 12, 24, Material.POLISHED_BLACKSTONE_BRICKS);

        // Floor — gold block inlay pattern
        b.fillFloor(-23, -23, 23, 23, 0, Material.BLACKSTONE);
        for (int x = -20; x <= 20; x += 4)
            for (int z = -20; z <= 20; z += 4)
                b.setBlock(x, 0, z, Material.GOLD_BLOCK);

        // Golden pillars (ring of 12)
        for (int a = 0; a < 360; a += 30) {
            int px = (int) (18 * Math.cos(Math.toRadians(a)));
            int pz = (int) (18 * Math.sin(Math.toRadians(a)));
            b.pillar(px, 1, 11, pz, Material.GOLD_BLOCK);
            b.setBlock(px, 11, pz, Material.GLOWSTONE);
        }

        // Grand entrance (south wall)
        b.fillBox(-3, 1, -24, 3, 8, -24, Material.AIR);
        b.gothicArch(0, 1, -24, 6, 9, Material.GOLD_BLOCK);

        // Throne room (north)
        b.fillBox(-8, 0, 14, 8, 0, 23, Material.GOLD_BLOCK);
        b.fillBox(-2, 1, 20, 2, 1, 22, Material.GOLD_BLOCK);
        b.setBlock(0, 2, 21, Material.GOLD_BLOCK);
        b.setBlock(0, 3, 21, Material.GOLD_BLOCK);
        b.setBlock(-1, 3, 21, Material.GOLD_BLOCK);
        b.setBlock(1, 3, 21, Material.GOLD_BLOCK);
        b.setBlock(0, 4, 21, Material.GOLD_BLOCK);
        // Throne carpet
        for (int z = 14; z <= 20; z++) b.setBlock(0, 1, z, Material.RED_CARPET);

        // Treasure hoard room (east wing)
        b.fillBox(10, 0, -10, 23, 8, 10, Material.POLISHED_BLACKSTONE_BRICKS);
        b.fillBox(11, 1, -9, 22, 7, 9, Material.AIR);
        b.fillFloor(11, -9, 22, 9, 0, Material.GOLD_BLOCK);
        // Piles of gold
        b.fillBox(14, 1, -5, 18, 3, -2, Material.GOLD_BLOCK);
        b.fillBox(15, 1, 3, 19, 2, 6, Material.GOLD_BLOCK);
        b.setBlock(16, 4, -3, Material.GOLD_BLOCK);
        b.setBlock(17, 3, -4, Material.GOLD_BLOCK);

        // Barracks (west wing)
        b.fillBox(-23, 0, -10, -10, 8, 10, Material.POLISHED_BLACKSTONE_BRICKS);
        b.fillBox(-22, 1, -9, -11, 7, 9, Material.AIR);
        b.fillFloor(-22, -9, -11, 9, 0, Material.BLACKSTONE);
        for (int z = -7; z <= 7; z += 4) {
            b.setBlock(-20, 1, z, Material.RED_BED);
            b.setBlock(-13, 1, z, Material.RED_BED);
        }
        b.wallLighting(-22, 5, -9, -11, -9, 4, Material.SOUL_LANTERN);

        // Banquet hall (center-south)
        b.fillBox(-6, 0, -12, 6, 0, -2, Material.POLISHED_BLACKSTONE);
        // Long table
        for (int z = -10; z <= -4; z++) {
            b.table(0, 1, z);
        }
        b.chair(-1, 1, -10, Material.POLISHED_BLACKSTONE_BRICK_STAIRS, BlockFace.EAST);
        b.chair(1, 1, -10, Material.POLISHED_BLACKSTONE_BRICK_STAIRS, BlockFace.WEST);
        b.chair(-1, 1, -4, Material.POLISHED_BLACKSTONE_BRICK_STAIRS, BlockFace.EAST);
        b.chair(1, 1, -4, Material.POLISHED_BLACKSTONE_BRICK_STAIRS, BlockFace.WEST);

        // Ceiling
        b.fillFloor(-23, -23, 23, 23, 12, Material.POLISHED_BLACKSTONE_BRICKS);
        b.chandelier(0, 12, 0, 3);
        b.chandelier(0, 12, 18, 2);
        b.chandelier(0, 12, -10, 2);

        // Battlements
        b.battlements(-24, 13, -24, 24, -24, Material.POLISHED_BLACKSTONE);
        b.battlements(-24, 13, 24, 24, 24, Material.POLISHED_BLACKSTONE);

        b.placeChest(16, 1, 5);
        b.placeChest(18, 1, -6);
        b.placeChest(0, 1, 22);
        b.placeChest(-18, 1, 0);
        b.setBossSpawn(0, 1, 18);
    }

    /**
     * Wither Sanctum — Soul sand valley temple, wither skull decorations,
     * dark altar, soul fire torches, ominous ambiance.
     */
    public static void buildWitherSanctum(StructureBuilder b) {
        // Stepped platform base
        for (int step = 0; step < 4; step++) {
            int r = 18 - step * 2;
            b.fillBox(-r, step, -r, r, step, r, Material.SOUL_SAND);
        }

        // Main temple walls
        b.fillWalls(-15, 4, -15, 15, 18, 15, Material.NETHER_BRICKS);
        b.hollowBox(-15, 5, -15, 15, 18, 15);
        b.fillFloor(-14, -14, 14, 14, 4, Material.SOUL_SOIL);

        // Netherrack pillar supports (8 interior pillars)
        int[][] pillars = {{-10, -10}, {10, -10}, {-10, 10}, {10, 10}, {-10, 0}, {10, 0}, {0, -10}, {0, 10}};
        for (int[] p : pillars) {
            b.pillar(p[0], 5, 17, p[1], Material.NETHERRACK);
            b.setBlock(p[0], 17, p[1], Material.SOUL_TORCH);
        }

        // Wither skull decorations on walls
        for (int i = -12; i <= 12; i += 6) {
            b.setBlock(i, 12, -15, Material.WITHER_SKELETON_SKULL);
            b.setBlock(i, 12, 15, Material.WITHER_SKELETON_SKULL);
            b.setBlock(-15, 12, i, Material.WITHER_SKELETON_SKULL);
            b.setBlock(15, 12, i, Material.WITHER_SKELETON_SKULL);
        }

        // Dark altar (center)
        b.fillBox(-3, 4, -3, 3, 6, 3, Material.POLISHED_BLACKSTONE);
        b.fillBox(-2, 6, -2, 2, 6, 2, Material.CRYING_OBSIDIAN);
        b.setBlock(0, 7, 0, Material.SOUL_CAMPFIRE);
        // Wither pattern on altar (3 skulls + 4 soul sand)
        b.setBlock(-1, 7, 0, Material.WITHER_SKELETON_SKULL);
        b.setBlock(1, 7, 0, Material.WITHER_SKELETON_SKULL);
        b.setBlock(0, 7, -1, Material.WITHER_SKELETON_SKULL);
        b.setBlock(-1, 6, 0, Material.SOUL_SAND);
        b.setBlock(1, 6, 0, Material.SOUL_SAND);
        b.setBlock(0, 6, -1, Material.SOUL_SAND);
        b.setBlock(0, 6, 1, Material.SOUL_SAND);

        // Side chambers (wing rooms)
        // West wing — bone shrine
        b.fillBox(-15, 4, -6, -18, 12, 6, Material.AIR);
        b.fillWalls(-18, 4, -6, -15, 12, 6, Material.NETHER_BRICKS);
        b.fillFloor(-17, -5, -16, 5, 4, Material.BONE_BLOCK);
        b.setBlock(-17, 5, 0, Material.WITHER_SKELETON_SKULL);

        // East wing — offering room
        b.fillBox(15, 4, -6, 18, 12, 6, Material.AIR);
        b.fillWalls(15, 4, -6, 18, 12, 6, Material.NETHER_BRICKS);
        b.fillFloor(16, -5, 17, 5, 4, Material.SOUL_SOIL);

        // Entrance archway (south)
        b.archDoorway(0, 4, -15, 6, 8, Material.RED_NETHER_BRICKS);

        // Soul fire braziers (torches at intervals)
        b.wallLighting(-14, 8, -15, 14, -15, 4, Material.SOUL_TORCH);
        b.wallLighting(-14, 8, 15, 14, 15, 4, Material.SOUL_TORCH);
        b.wallLighting(-15, 8, -14, -15, 14, 4, Material.SOUL_TORCH);
        b.wallLighting(15, 8, -14, 15, 14, 4, Material.SOUL_TORCH);

        // Ceiling with soul lanterns
        b.fillFloor(-14, -14, 14, 14, 18, Material.NETHER_BRICKS);
        b.chandelier(0, 18, 0, 4);
        b.chandelier(-8, 18, -8, 2);
        b.chandelier(8, 18, 8, 2);

        // Scatter soul sand variation on floor
        b.scatter(-14, 4, -14, 14, 4, 14, Material.SOUL_SOIL, Material.SOUL_SAND, 0.2);

        b.placeChest(-17, 5, 3);
        b.placeChest(16, 5, 0);
        b.placeChest(0, 7, 2);
        b.setBossSpawn(0, 7, 5);
    }

    /**
     * Blaze Colosseum — Nether brick arena with tiered magma seating,
     * fire pit center, blaze spawner frames, spectator boxes, trophy room.
     */
    public static void buildBlazeColosseum(StructureBuilder b) {
        // Arena floor
        b.filledCircle(0, 0, 0, 22, Material.NETHER_BRICKS);
        b.filledCircle(0, 0, 0, 10, Material.BLACKSTONE);

        // Central fire pit
        b.filledCircle(0, 0, 0, 4, Material.MAGMA_BLOCK);
        b.setBlock(0, 1, 0, Material.FIRE);
        b.setBlock(-2, 1, 0, Material.FIRE);
        b.setBlock(2, 1, 0, Material.FIRE);
        b.setBlock(0, 1, -2, Material.FIRE);
        b.setBlock(0, 1, 2, Material.FIRE);

        // Tiered seating (4 tiers)
        for (int tier = 0; tier < 4; tier++) {
            int r = 14 + tier * 2;
            int y = tier + 1;
            b.circle(0, y, 0, r, Material.NETHER_BRICKS);
            // Magma block seats every other position
            for (int a = 0; a < 360; a += 8) {
                int sx = (int) (r * Math.cos(Math.toRadians(a)));
                int sz = (int) (r * Math.sin(Math.toRadians(a)));
                if (a % 16 == 0) b.setBlock(sx, y, sz, Material.MAGMA_BLOCK);
            }
        }

        // Outer wall
        for (int y = 0; y <= 8; y++) b.circle(0, y, 0, 22, Material.RED_NETHER_BRICKS);
        b.battlements(0, 9, 0, 22, 0, Material.NETHER_BRICKS); // ring simplified

        // Blaze spawner frames (6 pillars around arena)
        for (int a = 0; a < 360; a += 60) {
            int px = (int) (12 * Math.cos(Math.toRadians(a)));
            int pz = (int) (12 * Math.sin(Math.toRadians(a)));
            b.pillar(px, 1, 8, pz, Material.NETHER_BRICK_FENCE);
            b.setBlock(px, 9, pz, Material.LAVA);
            b.setBlock(px, 8, pz, Material.MAGMA_BLOCK);
            // Iron bars frame
            b.setBlock(px + 1, 5, pz, Material.IRON_BARS);
            b.setBlock(px - 1, 5, pz, Material.IRON_BARS);
            b.setBlock(px, 5, pz + 1, Material.IRON_BARS);
            b.setBlock(px, 5, pz - 1, Material.IRON_BARS);
        }

        // Spectator boxes (4 elevated platforms)
        int[][] specPos = {{-20, 0}, {20, 0}, {0, -20}, {0, 20}};
        for (int[] sp : specPos) {
            b.fillBox(sp[0] - 3, 5, sp[1] - 3, sp[0] + 3, 5, sp[1] + 3, Material.NETHER_BRICKS);
            b.fillBox(sp[0] - 2, 6, sp[1] - 2, sp[0] + 2, 6, sp[1] + 2, Material.RED_CARPET);
            b.setBlock(sp[0], 8, sp[1], Material.SOUL_LANTERN);
            // Railing
            b.fillBox(sp[0] - 3, 6, sp[1] - 3, sp[0] + 3, 7, sp[1] - 3, Material.NETHER_BRICK_FENCE);
        }

        // Trophy room (underground)
        b.fillBox(-8, -6, -8, 8, -1, 8, Material.RED_NETHER_BRICKS);
        b.fillBox(-7, -5, -7, 7, -1, 7, Material.AIR);
        b.fillFloor(-7, -7, 7, 7, -6, Material.POLISHED_BLACKSTONE);
        // Trophies — armor stands simulated with fences + skulls
        for (int x = -5; x <= 5; x += 5) {
            b.pillar(x, -5, -3, 0, Material.NETHER_BRICK_FENCE);
            b.setBlock(x, -2, 0, Material.WITHER_SKELETON_SKULL);
        }
        b.setBlock(0, -5, -5, Material.SOUL_LANTERN);
        b.setBlock(0, -5, 5, Material.SOUL_LANTERN);

        // Entrance tunnel
        b.fillBox(-2, 0, -22, 2, 4, -14, Material.AIR);
        b.fillWalls(-3, 0, -22, 3, 5, -14, Material.NETHER_BRICKS);

        b.placeChest(0, -5, 0);
        b.placeChest(5, 1, 5);
        b.placeChest(-5, 1, -5);
        b.setBossSpawn(0, 1, 0);
    }

    /**
     * Nether King's Castle — MASSIVE 250x250 blackstone fortress with outer walls,
     * towers, inner courtyard, massive throne room arena (40-radius with lava,
     * pillars, raised throne), treasure vaults, grand entrance.
     */
    public static void buildNetherKingsCastle(StructureBuilder b) {
        // === OUTER WALLS (perimeter at ~100 blocks from center) ===
        int ow = 100; // outer wall half-size
        b.fillWalls(-ow, 0, -ow, ow, 18, ow, Material.POLISHED_BLACKSTONE_BRICKS);
        // Clear interior of outer walls
        b.fillBox(-ow + 1, 1, -ow + 1, ow - 1, 17, ow - 1, Material.AIR);
        b.fillFloor(-ow, -ow, ow, ow, 0, Material.BLACKSTONE);

        // Outer wall battlements
        b.battlements(-ow, 19, -ow, ow, -ow, Material.POLISHED_BLACKSTONE);
        b.battlements(-ow, 19, ow, ow, ow, Material.POLISHED_BLACKSTONE);
        b.battlements(-ow, 19, -ow, -ow, ow, Material.POLISHED_BLACKSTONE);
        b.battlements(ow, 19, -ow, ow, ow, Material.POLISHED_BLACKSTONE);

        // Outer corner towers (4)
        int[][] outerCorners = {{-ow, -ow}, {ow, -ow}, {-ow, ow}, {ow, ow}};
        for (int[] c : outerCorners) {
            b.roundTower(c[0], 0, c[1], 6, 25, Material.POLISHED_BLACKSTONE_BRICKS,
                    Material.DEEPSLATE_TILES, Material.RED_STAINED_GLASS);
        }

        // Grand entrance gate (south)
        b.fillBox(-6, 1, -ow, 6, 14, -ow, Material.AIR);
        b.gothicArch(0, 1, -ow, 12, 15, Material.GILDED_BLACKSTONE);
        // Flanking towers at gate
        b.roundTower(-10, 0, -ow, 5, 22, Material.NETHER_BRICKS, Material.RED_NETHER_BRICKS,
                Material.ORANGE_STAINED_GLASS);
        b.roundTower(10, 0, -ow, 5, 22, Material.NETHER_BRICKS, Material.RED_NETHER_BRICKS,
                Material.ORANGE_STAINED_GLASS);

        // === INNER KEEP (40x40) ===
        int iw = 40;
        b.fillWalls(-iw, 0, -iw, iw, 25, iw, Material.NETHER_BRICKS);
        b.hollowBox(-iw, 1, -iw, iw, 25, iw);
        b.fillFloor(-iw + 1, -iw + 1, iw - 1, iw - 1, 0, Material.POLISHED_BLACKSTONE);

        // Inner keep towers (4 corners)
        int[][] innerCorners = {{-iw, -iw}, {iw, -iw}, {-iw, iw}, {iw, iw}};
        for (int[] c : innerCorners) {
            b.fillBox(c[0] - 4, 0, c[1] - 4, c[0] + 4, 30, c[1] + 4, Material.POLISHED_BLACKSTONE_BRICKS);
            b.fillBox(c[0] - 3, 1, c[1] - 3, c[0] + 3, 29, c[1] + 3, Material.AIR);
            b.setBlock(c[0], 31, c[1], Material.SOUL_LANTERN);
        }

        // Inner keep entrance
        b.fillBox(-4, 1, -iw, 4, 10, -iw, Material.AIR);
        b.gothicArch(0, 1, -iw, 8, 11, Material.GOLD_BLOCK);

        // === THRONE ROOM (40-radius circular arena) ===
        b.filledCircle(0, 0, 0, 35, Material.DEEPSLATE_TILES);
        b.filledCircle(0, 0, 0, 30, Material.POLISHED_BLACKSTONE);

        // Lava channels (cross pattern)
        for (int i = -25; i <= 25; i++) {
            b.setBlock(i, 0, 0, Material.LAVA);
            b.setBlock(0, 0, i, Material.LAVA);
        }
        // Safe walkways over lava
        for (int i = -25; i <= 25; i += 3) {
            b.setBlock(i, 0, 0, Material.POLISHED_BLACKSTONE);
            b.setBlock(0, 0, i, Material.POLISHED_BLACKSTONE);
        }

        // Arena pillars (8 in a ring)
        for (int a = 0; a < 360; a += 45) {
            int px = (int) (20 * Math.cos(Math.toRadians(a)));
            int pz = (int) (20 * Math.sin(Math.toRadians(a)));
            b.pillar(px, 1, 20, pz, Material.GILDED_BLACKSTONE);
            b.setBlock(px, 20, pz, Material.SOUL_LANTERN);
            b.setBlock(px, 10, pz, Material.SOUL_LANTERN);
        }

        // Raised throne platform (north of center)
        b.fillBox(-5, 0, 20, 5, 4, 30, Material.GOLD_BLOCK);
        b.fillBox(-4, 4, 21, 4, 4, 29, Material.RED_CARPET);
        // Throne
        b.setBlock(0, 5, 27, Material.GOLD_BLOCK);
        b.setBlock(0, 6, 27, Material.GOLD_BLOCK);
        b.setBlock(-1, 6, 27, Material.GOLD_BLOCK);
        b.setBlock(1, 6, 27, Material.GOLD_BLOCK);
        b.setBlock(0, 7, 27, Material.GOLD_BLOCK);
        // Steps
        for (int s = 0; s < 4; s++) b.fillBox(-3, s, 20 + s, 3, s, 20 + s, Material.POLISHED_BLACKSTONE_BRICK_STAIRS);

        // === TREASURE VAULTS (east & west wings) ===
        // East vault
        b.fillBox(iw + 2, 0, -15, iw + 20, 12, 15, Material.POLISHED_BLACKSTONE_BRICKS);
        b.fillBox(iw + 3, 1, -14, iw + 19, 11, 14, Material.AIR);
        b.fillFloor(iw + 3, -14, iw + 19, 14, 0, Material.GOLD_BLOCK);
        b.fillBox(iw + 10, 1, -5, iw + 15, 4, 5, Material.GOLD_BLOCK);

        // West vault
        b.fillBox(-iw - 20, 0, -15, -iw - 2, 12, 15, Material.POLISHED_BLACKSTONE_BRICKS);
        b.fillBox(-iw - 19, 1, -14, -iw - 3, 11, 14, Material.AIR);
        b.fillFloor(-iw - 19, -14, -iw - 3, 14, 0, Material.GOLD_BLOCK);

        // Courtyard features — lava pools in outer courtyard
        b.filledCircle(-60, 0, -60, 8, Material.LAVA);
        b.filledCircle(60, 0, -60, 8, Material.LAVA);
        b.filledCircle(-60, 0, 60, 8, Material.LAVA);
        b.filledCircle(60, 0, 60, 8, Material.LAVA);

        // Magma block paths connecting features
        for (int i = -ow + 5; i <= -iw - 5; i++) {
            b.setBlock(i, 0, 0, Material.MAGMA_BLOCK);
            b.setBlock(-i, 0, 0, Material.MAGMA_BLOCK);
        }

        // Ceiling for throne room
        b.dome(0, 25, 0, 30, Material.NETHER_BRICKS);
        b.chandelier(0, 25, 0, 5);
        b.chandelier(-15, 25, -15, 3);
        b.chandelier(15, 25, -15, 3);
        b.chandelier(-15, 25, 15, 3);
        b.chandelier(15, 25, 15, 3);

        // Wall lighting throughout
        b.wallLighting(-iw + 1, 10, -iw, iw - 1, -iw, 6, Material.SOUL_LANTERN);
        b.wallLighting(-iw + 1, 10, iw, iw - 1, iw, 6, Material.SOUL_LANTERN);

        b.placeChest(iw + 12, 1, 0);
        b.placeChest(-iw - 12, 1, 0);
        b.placeChest(0, 5, 25);
        b.placeChest(25, 1, 25);
        b.placeChest(-25, 1, -25);
        b.setBossSpawn(0, 1, 10);
    }

    /**
     * Demon Fortress — Blackstone/nether brick hybrid, demonic symbols (nether gold ore patterns),
     * prison cells, forge room, altar, soul fire braziers.
     */
    public static void buildDemonFortress(StructureBuilder b) {
        // Foundation
        b.fillBox(-18, 0, -18, 18, 0, 18, Material.BLACKSTONE);

        // Hybrid walls — alternating blackstone and nether brick layers
        for (int y = 1; y <= 14; y++) {
            Material wallMat = y % 3 == 0 ? Material.NETHER_BRICKS : Material.POLISHED_BLACKSTONE_BRICKS;
            b.fillBox(-18, y, -18, 18, y, -18, wallMat);
            b.fillBox(-18, y, 18, 18, y, 18, wallMat);
            b.fillBox(-18, y, -18, -18, y, 18, wallMat);
            b.fillBox(18, y, -18, 18, y, 18, wallMat);
        }
        b.hollowBox(-18, 1, -18, 18, 14, 18);

        // Floor with nether gold ore demonic symbol (pentagram-ish)
        b.fillFloor(-17, -17, 17, 17, 0, Material.BLACKSTONE);
        // Star pattern in nether gold ore
        for (int a = 0; a < 360; a += 72) {
            double rad1 = Math.toRadians(a);
            double rad2 = Math.toRadians(a + 144);
            int x1 = (int) (12 * Math.cos(rad1));
            int z1 = (int) (12 * Math.sin(rad1));
            int x2 = (int) (12 * Math.cos(rad2));
            int z2 = (int) (12 * Math.sin(rad2));
            b.flyingButtress(x1, 0, z1, x2, 0, z2, Material.NETHER_GOLD_ORE);
        }
        b.filledCircle(0, 0, 0, 3, Material.NETHER_GOLD_ORE);

        // Soul fire braziers (6 around perimeter)
        for (int a = 0; a < 360; a += 60) {
            int px = (int) (14 * Math.cos(Math.toRadians(a)));
            int pz = (int) (14 * Math.sin(Math.toRadians(a)));
            b.fillBox(px - 1, 0, pz - 1, px + 1, 2, pz + 1, Material.POLISHED_BLACKSTONE);
            b.setBlock(px, 3, pz, Material.SOUL_CAMPFIRE);
        }

        // Central altar — dark ritual platform
        b.fillBox(-3, 0, -3, 3, 3, 3, Material.CRYING_OBSIDIAN);
        b.fillBox(-2, 3, -2, 2, 3, 2, Material.OBSIDIAN);
        b.setBlock(0, 4, 0, Material.SOUL_CAMPFIRE);
        b.setBlock(-2, 4, -2, Material.CANDLE);
        b.setBlock(2, 4, -2, Material.CANDLE);
        b.setBlock(-2, 4, 2, Material.CANDLE);
        b.setBlock(2, 4, 2, Material.CANDLE);

        // Prison cells (NE corner)
        for (int cell = 0; cell < 3; cell++) {
            int cx = 10 + cell * 3;
            b.fillBox(cx, 0, -16, cx + 2, 5, -12, Material.NETHER_BRICKS);
            b.fillBox(cx, 1, -15, cx + 2, 4, -13, Material.AIR);
            for (int y = 1; y <= 4; y++) b.setBlock(cx, y, -12, Material.IRON_BARS);
            b.setBlock(cx + 1, 4, -14, Material.SOUL_LANTERN);
        }

        // Forge room (SW corner)
        b.fillBox(-17, 0, 10, -10, 8, 17, Material.NETHER_BRICKS);
        b.fillBox(-16, 1, 11, -11, 7, 16, Material.AIR);
        b.fillFloor(-16, 11, -11, 16, 0, Material.BLACKSTONE);
        b.setBlock(-14, 1, 14, Material.BLAST_FURNACE);
        b.setBlock(-13, 1, 14, Material.SMITHING_TABLE);
        b.setBlock(-12, 1, 14, Material.ANVIL);
        b.setBlock(-15, 1, 12, Material.LAVA);
        b.setBlock(-15, 1, 13, Material.LAVA);
        b.setBlock(-14, 7, 13, Material.SOUL_LANTERN);

        // Entrance
        b.archDoorway(0, 1, -18, 6, 8, Material.RED_NETHER_BRICKS);

        // Ceiling
        b.pyramidRoof(-18, -18, 18, 18, 14, Material.NETHER_BRICKS);

        // Interior pillars
        for (int x = -12; x <= 12; x += 8)
            for (int z = -12; z <= 12; z += 8) {
                if (Math.abs(x) < 5 && Math.abs(z) < 5) continue;
                b.pillar(x, 1, 13, z, Material.POLISHED_BLACKSTONE_BRICKS);
                b.setBlock(x, 13, z, Material.SOUL_LANTERN);
            }

        b.placeChest(-14, 1, 12);
        b.placeChest(14, 1, -14);
        b.placeChest(0, 4, 2);
        b.setBossSpawn(0, 4, 5);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  END STRUCTURES (8)
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Void Shrine — Purpur/end stone shrine, floating platforms connected by bridges,
     * end rod lighting, obsidian altar, chorus flower garden.
     */
    public static void buildVoidShrine(StructureBuilder b) {
        // Central platform
        b.filledCircle(0, 0, 0, 12, Material.END_STONE_BRICKS);
        b.filledCircle(0, 1, 0, 10, Material.PURPUR_BLOCK);

        // Outer floating platforms (4) connected by bridges
        int[][] platforms = {{-20, 0}, {20, 0}, {0, -20}, {0, 20}};
        for (int[] p : platforms) {
            b.filledCircle(p[0], 0, p[1], 6, Material.END_STONE_BRICKS);
            b.filledCircle(p[0], 1, p[1], 4, Material.PURPUR_BLOCK);
            // End rod lights on each platform
            b.setBlock(p[0], 3, p[1], Material.END_ROD);
            b.setBlock(p[0] - 3, 2, p[1], Material.END_ROD);
            b.setBlock(p[0] + 3, 2, p[1], Material.END_ROD);
        }

        // Bridges connecting to center
        for (int[] p : platforms) {
            int dx = Integer.signum(p[0]);
            int dz = Integer.signum(p[1]);
            for (int i = 7; i < 14; i++) {
                int bx = dx * i;
                int bz = dz * i;
                b.setBlock(bx, 0, bz, Material.PURPUR_BLOCK);
                b.setBlock(bx + (dz != 0 ? 1 : 0), 0, bz + (dx != 0 ? 1 : 0), Material.PURPUR_BLOCK);
                b.setBlock(bx - (dz != 0 ? 1 : 0), 0, bz - (dx != 0 ? 1 : 0), Material.PURPUR_BLOCK);
            }
        }

        // Central obsidian altar
        b.fillBox(-2, 1, -2, 2, 3, 2, Material.OBSIDIAN);
        b.fillBox(-1, 3, -1, 1, 3, 1, Material.CRYING_OBSIDIAN);
        b.setBlock(0, 4, 0, Material.END_ROD);
        b.setBlock(0, 5, 0, Material.END_ROD);

        // Pillar ring around altar
        for (int a = 0; a < 360; a += 45) {
            int px = (int) (8 * Math.cos(Math.toRadians(a)));
            int pz = (int) (8 * Math.sin(Math.toRadians(a)));
            b.pillar(px, 1, 10, pz, Material.PURPUR_PILLAR);
            b.setBlock(px, 11, pz, Material.END_ROD);
        }

        // Dome over central area
        b.dome(0, 10, 0, 10, Material.PURPUR_BLOCK);

        // Chorus flower garden (on east platform)
        for (int x = 18; x <= 22; x += 2)
            for (int z = -2; z <= 2; z += 2) {
                b.setBlock(x, 2, z, Material.END_STONE);
                b.setBlock(x, 3, z, Material.CHORUS_PLANT);
                b.setBlock(x, 4, z, Material.CHORUS_FLOWER);
            }

        // Ender chest at altar
        b.setBlock(0, 4, 1, Material.ENDER_CHEST);

        b.placeChest(0, 2, 0);
        b.placeChest(-20, 2, 0);
        b.placeChest(0, 2, 20);
        b.setBossSpawn(0, 2, 5);
    }

    /**
     * Ender Monastery — Peaceful end stone building with library, meditation hall,
     * training yard, monk cells, zen garden.
     */
    public static void buildEnderMonastery(StructureBuilder b) {
        // Main building
        b.fillBox(-18, 0, -12, 18, 0, 12, Material.END_STONE_BRICKS);
        b.fillWalls(-18, 1, -12, 18, 12, 12, Material.END_STONE_BRICKS);
        b.hollowBox(-18, 1, -12, 18, 12, 12);
        b.fillFloor(-17, -11, 17, 11, 0, Material.PURPUR_BLOCK);

        // Gabled roof
        b.gabledRoof(-18, -12, 18, 12, 12, Material.PURPUR_STAIRS, Material.PURPUR_SLAB);

        // Central corridor
        b.fillBox(-1, 1, -11, 1, 8, 11, Material.AIR);
        b.fillBox(-1, 0, -11, 1, 0, 11, Material.END_STONE_BRICKS);

        // Library wing (west: -18 to -3)
        b.bookshelfWall(-16, 1, -11, -4, 5);
        b.bookshelfWall(-16, 1, 11, -4, 5);
        b.setBlock(-10, 1, 0, Material.LECTERN);
        b.setBlock(-12, 1, -5, Material.ENCHANTING_TABLE);
        b.chandelier(-10, 12, 0, 3);

        // Meditation hall (east: 3 to 18)
        b.fillFloor(3, -10, 17, 10, 0, Material.PURPUR_BLOCK);
        // Meditation mats (carpets)
        for (int x = 5; x <= 15; x += 5)
            for (int z = -8; z <= 8; z += 4)
                b.setBlock(x, 1, z, Material.PURPLE_CARPET);
        // Central meditation pillar
        b.pillar(10, 1, 8, 0, Material.PURPUR_PILLAR);
        b.setBlock(10, 9, 0, Material.END_ROD);
        b.chandelier(10, 12, 0, 2);

        // Training yard (courtyard outside south)
        b.fillBox(-10, 0, 13, 10, 0, 22, Material.END_STONE);
        b.fillBox(-10, 1, 13, -10, 3, 22, Material.END_STONE_BRICKS); // wall
        b.fillBox(10, 1, 13, 10, 3, 22, Material.END_STONE_BRICKS);
        // Training dummies (fence + skull)
        for (int x = -6; x <= 6; x += 4) {
            b.pillar(x, 1, 2, 18, Material.OAK_FENCE);
            b.setBlock(x, 3, 18, Material.SKELETON_SKULL);
        }

        // Monk cells (along north wall interior)
        for (int cell = 0; cell < 4; cell++) {
            int cx = -14 + cell * 8;
            b.fillBox(cx, 0, -11, cx + 4, 5, -8, Material.END_STONE_BRICKS);
            b.fillBox(cx + 1, 1, -10, cx + 3, 4, -9, Material.AIR);
            b.setBlock(cx + 2, 1, -10, Material.WHITE_BED);
            b.setBlock(cx + 1, 4, -10, Material.END_ROD);
        }

        // Zen garden (dead bushes + end stone, east yard)
        b.fillBox(12, 0, 13, 18, 0, 20, Material.END_STONE);
        for (int x = 13; x <= 17; x += 2)
            for (int z = 14; z <= 19; z += 2) {
                b.setBlock(x, 1, z, Material.DEAD_BUSH);
            }
        b.setBlock(15, 1, 17, Material.END_ROD); // garden light

        // Entrance (south wall center)
        b.archDoorway(0, 1, -12, 4, 6, Material.PURPUR_BLOCK);

        // Interior pillars
        for (int x = -14; x <= 14; x += 7) {
            b.pillar(x, 1, 10, -4, Material.PURPUR_PILLAR);
            b.pillar(x, 1, 10, 4, Material.PURPUR_PILLAR);
        }

        // Lighting
        b.wallLighting(-17, 6, -12, 17, -12, 5, Material.END_ROD);
        b.wallLighting(-17, 6, 12, 17, 12, 5, Material.END_ROD);

        b.placeChest(-12, 1, -3);
        b.placeChest(14, 1, 5);
        b.placeChest(-14, 1, 8);
        b.setBossSpawn(10, 1, 0);
    }

    /**
     * Dragon's Hoard — Cave of treasures: gold/diamond piles, chests everywhere,
     * dragon egg pedestal, skull decorations.
     */
    public static void buildDragonsHoard(StructureBuilder b) {
        // Cave shell
        b.filledDome(0, 0, 0, 12, Material.END_STONE);
        b.filledDome(0, 0, 0, 10, Material.AIR);
        b.fillFloor(-10, -10, 10, 10, 0, Material.END_STONE_BRICKS);

        // Gold piles (irregular mounds)
        b.filledCircle(-4, 0, -4, 4, Material.GOLD_BLOCK);
        b.filledCircle(-4, 1, -4, 3, Material.GOLD_BLOCK);
        b.filledCircle(-4, 2, -4, 2, Material.GOLD_BLOCK);
        b.setBlock(-4, 3, -4, Material.GOLD_BLOCK);

        b.filledCircle(5, 0, 3, 3, Material.GOLD_BLOCK);
        b.filledCircle(5, 1, 3, 2, Material.GOLD_BLOCK);
        b.setBlock(5, 2, 3, Material.GOLD_BLOCK);

        // Diamond pile
        b.filledCircle(-3, 0, 5, 2, Material.DIAMOND_BLOCK);
        b.setBlock(-3, 1, 5, Material.DIAMOND_BLOCK);

        // Emerald accents
        b.setBlock(7, 1, -2, Material.EMERALD_BLOCK);
        b.setBlock(6, 1, -3, Material.EMERALD_BLOCK);

        // Dragon egg pedestal
        b.fillBox(-1, 0, -1, 1, 3, 1, Material.OBSIDIAN);
        b.setBlock(0, 3, 0, Material.CRYING_OBSIDIAN);
        b.setBlock(0, 4, 0, Material.DRAGON_EGG);

        // Skull decorations
        b.setBlock(-8, 1, 0, Material.SKELETON_SKULL);
        b.setBlock(8, 1, 0, Material.SKELETON_SKULL);
        b.setBlock(0, 1, -8, Material.WITHER_SKELETON_SKULL);
        b.setBlock(0, 1, 8, Material.SKELETON_SKULL);

        // Scattered chests
        b.placeChest(3, 1, -6);
        b.placeChest(-6, 1, 2);
        b.placeChest(7, 1, 5);
        b.placeChest(-2, 2, -5);
        b.placeChest(4, 0, -3);

        // End rod lighting
        b.setBlock(0, 10, 0, Material.END_ROD);
        b.setBlock(-5, 8, -5, Material.END_ROD);
        b.setBlock(5, 8, -5, Material.END_ROD);
        b.setBlock(-5, 8, 5, Material.END_ROD);
        b.setBlock(5, 8, 5, Material.END_ROD);

        // Narrow cave entrance
        b.fillBox(-2, 1, -12, 2, 4, -10, Material.AIR);
    }

    /**
     * End Rift Arena — 50-radius open arena with obsidian pillars, end crystal
     * positions, entry portal, spectator platforms, barrier edges.
     */
    public static void buildEndRiftArena(StructureBuilder b) {
        // Arena floor
        b.filledCircle(0, 0, 0, 50, Material.END_STONE_BRICKS);
        b.filledCircle(0, 0, 0, 45, Material.END_STONE);
        b.filledCircle(0, 0, 0, 5, Material.OBSIDIAN);

        // Barrier ring (edge wall)
        for (int y = 1; y <= 4; y++) b.circle(0, y, 0, 50, Material.OBSIDIAN);

        // Obsidian pillars (12 in a ring for cover)
        for (int a = 0; a < 360; a += 30) {
            int px = (int) (30 * Math.cos(Math.toRadians(a)));
            int pz = (int) (30 * Math.sin(Math.toRadians(a)));
            b.fillBox(px - 1, 0, pz - 1, px + 1, 12, pz + 1, Material.OBSIDIAN);
            // End rod atop
            b.setBlock(px, 13, pz, Material.END_ROD);
        }

        // End crystal positions (4 cardinal pillars, taller)
        int[][] crystalPillars = {{0, -35}, {0, 35}, {-35, 0}, {35, 0}};
        for (int[] cp : crystalPillars) {
            b.fillBox(cp[0] - 2, 0, cp[1] - 2, cp[0] + 2, 18, cp[1] + 2, Material.OBSIDIAN);
            b.fillBox(cp[0] - 1, 18, cp[1] - 1, cp[0] + 1, 18, cp[1] + 1, Material.IRON_BARS);
            b.setBlock(cp[0], 19, cp[1], Material.END_ROD);
        }

        // Entry portal (south)
        b.fillBox(-3, 0, -50, 3, 8, -46, Material.PURPUR_BLOCK);
        b.fillBox(-2, 1, -50, 2, 6, -46, Material.AIR);
        b.gothicArch(0, 1, -50, 4, 7, Material.PURPUR_PILLAR);
        b.setBlock(0, 1, -48, Material.END_GATEWAY);

        // Spectator platforms (4 elevated, outside the ring)
        int[][] specPlats = {{-45, -45}, {45, -45}, {-45, 45}, {45, 45}};
        for (int[] sp : specPlats) {
            b.fillBox(sp[0] - 5, 8, sp[1] - 5, sp[0] + 5, 8, sp[1] + 5, Material.PURPUR_BLOCK);
            b.fillBox(sp[0] - 5, 9, sp[1] - 5, sp[0] + 5, 10, sp[1] - 5, Material.PURPUR_BLOCK);
            b.setBlock(sp[0], 10, sp[1], Material.END_ROD);
        }

        // Decorative rings on floor
        b.circle(0, 0, 0, 15, Material.CRYING_OBSIDIAN);
        b.circle(0, 0, 0, 25, Material.CRYING_OBSIDIAN);

        // Center pedestal
        b.setBlock(0, 1, 0, Material.OBSIDIAN);
        b.setBlock(0, 2, 0, Material.END_ROD);

        b.placeChest(5, 1, 5);
        b.placeChest(-5, 1, -5);
        b.setBossSpawn(0, 1, 0);
    }

    /**
     * Dragon Death Chest — Small obsidian monument with guaranteed loot chest,
     * dragon head decoration, end rod torches.
     */
    public static void buildDragonDeathChest(StructureBuilder b) {
        // Base platform
        b.filledCircle(0, 0, 0, 5, Material.END_STONE_BRICKS);
        b.filledCircle(0, 1, 0, 3, Material.OBSIDIAN);

        // Monument pillar
        b.fillBox(-1, 1, -1, 1, 5, 1, Material.OBSIDIAN);
        b.fillBox(-2, 2, -2, 2, 2, 2, Material.CRYING_OBSIDIAN);

        // Dragon head atop
        b.setBlock(0, 6, 0, Material.DRAGON_HEAD);

        // End rod torches (4 corners)
        b.setBlock(-3, 2, -3, Material.END_ROD);
        b.setBlock(3, 2, -3, Material.END_ROD);
        b.setBlock(-3, 2, 3, Material.END_ROD);
        b.setBlock(3, 2, 3, Material.END_ROD);

        // Secondary end rods on pillars
        b.setBlock(-2, 3, 0, Material.END_ROD);
        b.setBlock(2, 3, 0, Material.END_ROD);
        b.setBlock(0, 3, -2, Material.END_ROD);
        b.setBlock(0, 3, 2, Material.END_ROD);

        // Purpur accent border
        b.circle(0, 0, 0, 5, Material.PURPUR_BLOCK);

        // Loot chest
        b.placeChest(0, 3, 0);
    }

    /**
     * Gleeok Arena — Multi-level purpur/end stone arena, destructible pillars,
     * elevated platforms, grand entrance arch, 3 head-themed pillars.
     */
    public static void buildGleeokArena(StructureBuilder b) {
        // Ground level arena floor
        b.filledCircle(0, 0, 0, 38, Material.END_STONE_BRICKS);
        b.filledCircle(0, 0, 0, 35, Material.END_STONE);
        b.filledCircle(0, 0, 0, 8, Material.PURPUR_BLOCK);

        // Outer wall ring
        for (int y = 1; y <= 15; y++) b.circle(0, y, 0, 38, Material.END_STONE_BRICKS);

        // Three head-themed pillars (at 120-degree intervals, representing 3 heads)
        Material[] headColors = {Material.RED_CONCRETE, Material.BLUE_CONCRETE, Material.GREEN_CONCRETE};
        for (int i = 0; i < 3; i++) {
            int a = i * 120;
            int px = (int) (25 * Math.cos(Math.toRadians(a)));
            int pz = (int) (25 * Math.sin(Math.toRadians(a)));
            b.fillBox(px - 2, 0, pz - 2, px + 2, 18, pz + 2, Material.PURPUR_BLOCK);
            // Head decoration on top
            b.fillBox(px - 2, 18, pz - 2, px + 2, 20, pz + 2, headColors[i]);
            b.setBlock(px, 21, pz, Material.END_ROD);
            // Eyes
            b.setBlock(px - 1, 19, pz - 2, Material.SEA_LANTERN);
            b.setBlock(px + 1, 19, pz - 2, Material.SEA_LANTERN);
        }

        // Destructible pillars for cover (8 smaller)
        for (int a = 22; a < 360; a += 45) {
            int px = (int) (15 * Math.cos(Math.toRadians(a)));
            int pz = (int) (15 * Math.sin(Math.toRadians(a)));
            b.pillar(px, 1, 8, pz, Material.END_STONE);
        }

        // Elevated platforms (second level at y=10)
        int[][] elevPlats = {{-30, -30}, {30, -30}, {-30, 30}, {30, 30}};
        for (int[] ep : elevPlats) {
            b.fillBox(ep[0] - 4, 10, ep[1] - 4, ep[0] + 4, 10, ep[1] + 4, Material.PURPUR_BLOCK);
            // Stairs up
            for (int s = 0; s < 10; s++) {
                int sx = ep[0] + (ep[0] > 0 ? -4 - s : 4 + s);
                b.setBlock(sx, s, ep[1], Material.PURPUR_STAIRS);
            }
            b.setBlock(ep[0], 12, ep[1], Material.END_ROD);
        }

        // Grand entrance arch (south)
        b.fillBox(-5, 0, -38, 5, 0, -34, Material.PURPUR_BLOCK);
        b.fillBox(-5, 1, -38, 5, 12, -38, Material.PURPUR_BLOCK);
        b.fillBox(-4, 1, -38, 4, 10, -38, Material.AIR);
        b.gothicArch(0, 1, -38, 8, 11, Material.PURPUR_PILLAR);

        // Floor pattern — rings of obsidian
        b.circle(0, 0, 0, 12, Material.OBSIDIAN);
        b.circle(0, 0, 0, 20, Material.OBSIDIAN);
        b.circle(0, 0, 0, 30, Material.CRYING_OBSIDIAN);

        // Central arena lighting
        b.chandelier(0, 15, 0, 4);

        b.placeChest(0, 1, 0);
        b.placeChest(25, 1, 0);
        b.placeChest(-25, 1, 0);
        b.setBossSpawn(0, 1, 5);
    }

    /**
     * Illidan's Prison — Obsidian prison complex with cell, broken bars,
     * observation room, warden quarters, rift tear, trophy room.
     */
    public static void buildIllidanPrison(StructureBuilder b) {
        // Main prison building
        b.fillBox(-18, 0, -14, 18, 14, 14, Material.OBSIDIAN);
        b.fillBox(-17, 1, -13, 17, 13, 13, Material.AIR);
        b.fillFloor(-17, -13, 17, 13, 0, Material.CRYING_OBSIDIAN);

        // The main cell (center, heavily fortified)
        b.fillBox(-6, 0, -6, 6, 10, 6, Material.OBSIDIAN);
        b.fillBox(-5, 1, -5, 5, 9, 5, Material.AIR);
        b.fillFloor(-5, -5, 5, 5, 0, Material.CRYING_OBSIDIAN);
        // Broken iron bars (gaps representing Illidan's escape)
        for (int y = 1; y <= 6; y++) {
            b.setBlock(-6, y, -2, Material.IRON_BARS);
            b.setBlock(-6, y, 2, Material.IRON_BARS);
            // Broken sections (air)
            b.setBlock(-6, y, -1, Material.AIR);
            b.setBlock(-6, y, 0, Material.AIR);
            b.setBlock(-6, y, 1, Material.AIR);
        }
        // Remnants of chains
        b.setBlock(0, 6, 0, Material.IRON_CHAIN);
        b.setBlock(0, 5, 0, Material.IRON_CHAIN);
        b.setBlock(-3, 5, -3, Material.IRON_CHAIN);
        b.setBlock(3, 5, -3, Material.IRON_CHAIN);

        // Observation room (above cell)
        b.fillBox(-8, 10, -8, 8, 14, 8, Material.OBSIDIAN);
        b.fillBox(-7, 11, -7, 7, 13, 7, Material.AIR);
        // Glass floor to look down
        b.fillFloor(-5, -5, 5, 5, 10, Material.PURPLE_STAINED_GLASS);

        // Warden quarters (east wing)
        b.furnishedRoom(10, 0, -12, 17, -6, 5, Material.OBSIDIAN,
                Material.CRYING_OBSIDIAN, Material.OBSIDIAN);
        b.setBlock(14, 1, -10, Material.RED_BED);

        // Rift tear (end gateway blocks in west wing)
        b.fillBox(-17, 0, -6, -10, 10, 6, Material.OBSIDIAN);
        b.fillBox(-16, 1, -5, -11, 9, 5, Material.AIR);
        b.setBlock(-14, 3, 0, Material.END_GATEWAY);
        b.setBlock(-13, 4, 0, Material.END_GATEWAY);
        b.setBlock(-14, 5, 0, Material.END_GATEWAY);
        // Crying obsidian frame around rift
        b.fillBox(-15, 2, -1, -12, 6, 1, Material.CRYING_OBSIDIAN);
        b.fillBox(-14, 3, 0, -13, 5, 0, Material.AIR);
        b.setBlock(-14, 3, 0, Material.END_GATEWAY);
        b.setBlock(-13, 4, 0, Material.END_GATEWAY);

        // Trophy room (far east)
        b.fillBox(10, 0, 4, 17, 8, 12, Material.OBSIDIAN);
        b.fillBox(11, 1, 5, 16, 7, 11, Material.AIR);
        // Display cases (glass on pedestals)
        for (int x = 12; x <= 15; x += 3) {
            b.setBlock(x, 1, 8, Material.PURPUR_PILLAR);
            b.setBlock(x, 2, 8, Material.GLASS);
        }
        b.setBlock(12, 7, 8, Material.END_ROD);

        // Entrance
        b.archDoorway(0, 1, -14, 4, 6, Material.CRYING_OBSIDIAN);

        // Lighting — sparse, ominous
        b.setBlock(-15, 8, -12, Material.END_ROD);
        b.setBlock(15, 8, -12, Material.END_ROD);
        b.setBlock(-15, 8, 12, Material.END_ROD);
        b.setBlock(15, 8, 12, Material.END_ROD);
        b.chandelier(0, 9, 0, 2);

        b.placeChest(14, 1, 8);
        b.placeChest(-14, 1, 4);
        b.placeChest(0, 1, 0);
        b.setBossSpawn(0, 1, -3);
    }

    /**
     * Skeleton Dragon Lair — Bone block cave, skull decorations, nest area,
     * treasure pile, narrow entrance opening to large cavern.
     */
    public static void buildSkeletonDragonLair(StructureBuilder b) {
        // Large cavern (irregular dome)
        b.filledDome(0, 0, 0, 16, Material.BONE_BLOCK);
        b.filledDome(0, 0, 0, 14, Material.AIR);
        b.fillFloor(-14, -14, 14, 14, 0, Material.BONE_BLOCK);

        // Scatter to make walls look organic
        b.scatter(-16, 0, -16, 16, 16, 16, Material.BONE_BLOCK, Material.CALCITE, 0.15);
        b.scatter(-16, 0, -16, 16, 16, 16, Material.BONE_BLOCK, Material.DRIPSTONE_BLOCK, 0.05);

        // Skull decorations lining the walls
        for (int a = 0; a < 360; a += 20) {
            int sx = (int) (13 * Math.cos(Math.toRadians(a)));
            int sz = (int) (13 * Math.sin(Math.toRadians(a)));
            b.setBlock(sx, 2, sz, Material.SKELETON_SKULL);
        }

        // Nest area (center — hay bales simulating a nest)
        b.filledCircle(0, 0, 0, 5, Material.HAY_BLOCK);
        b.filledCircle(0, 1, 0, 3, Material.HAY_BLOCK);
        b.setBlock(0, 2, 0, Material.TURTLE_EGG);
        b.setBlock(-1, 2, 1, Material.TURTLE_EGG);
        b.setBlock(1, 2, -1, Material.TURTLE_EGG);

        // Treasure pile (southeast)
        b.filledCircle(8, 0, 8, 4, Material.GOLD_BLOCK);
        b.filledCircle(8, 1, 8, 3, Material.GOLD_BLOCK);
        b.setBlock(8, 2, 8, Material.DIAMOND_BLOCK);
        b.setBlock(9, 2, 7, Material.GOLD_BLOCK);

        // Bone pillars (ribcage effect)
        for (int z = -10; z <= 10; z += 5) {
            b.pillar(-10, 1, 8, z, Material.BONE_BLOCK);
            b.pillar(10, 1, 8, z, Material.BONE_BLOCK);
            // Curved top connectors
            b.setBlock(-9, 9, z, Material.BONE_BLOCK);
            b.setBlock(-8, 10, z, Material.BONE_BLOCK);
            b.setBlock(9, 9, z, Material.BONE_BLOCK);
            b.setBlock(8, 10, z, Material.BONE_BLOCK);
        }

        // Narrow entrance tunnel (south)
        b.fillBox(-2, 0, -16, 2, 4, -14, Material.BONE_BLOCK);
        b.fillBox(-1, 1, -16, 1, 3, -14, Material.AIR);

        // Lighting — sparse torches
        b.setBlock(-6, 4, -6, Material.SOUL_LANTERN);
        b.setBlock(6, 4, -6, Material.SOUL_LANTERN);
        b.setBlock(-6, 4, 6, Material.SOUL_LANTERN);
        b.setBlock(6, 4, 6, Material.SOUL_LANTERN);

        // Wither skull on a pedestal
        b.setBlock(0, 1, -10, Material.POLISHED_BLACKSTONE);
        b.setBlock(0, 2, -10, Material.WITHER_SKELETON_SKULL);

        b.placeChest(8, 2, 9);
        b.placeChest(-8, 1, -8);
        b.placeChest(0, 1, 8);
        b.setBossSpawn(0, 2, 3);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  ABYSS STRUCTURES (3)
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Abyssal Castle — Grand gothic castle, 120x80 footprint, 4 corner mega-towers,
     * central keep with dome, flying buttresses, stained glass, nave, side chapels,
     * crypt below. The crown jewel of the Abyss dimension.
     */
    public static void buildAbyssalCastle(StructureBuilder b) {
        // === OUTER WALLS (120x80 footprint: -60 to 60 x, -40 to 40 z) ===
        b.fillBox(-60, 0, -40, 60, 0, 40, Material.DEEPSLATE_BRICKS);
        b.fillWalls(-60, 1, -40, 60, 20, 40, Material.DEEPSLATE_BRICKS);
        b.hollowBox(-60, 1, -40, 60, 20, 40);
        b.fillFloor(-59, -39, 59, 39, 0, Material.DEEPSLATE_TILES);

        // Blackstone base course
        b.fillBox(-60, 1, -40, 60, 2, -40, Material.POLISHED_BLACKSTONE);
        b.fillBox(-60, 1, 40, 60, 2, 40, Material.POLISHED_BLACKSTONE);
        b.fillBox(-60, 1, -40, -60, 2, 40, Material.POLISHED_BLACKSTONE);
        b.fillBox(60, 1, -40, 60, 2, 40, Material.POLISHED_BLACKSTONE);

        // === 4 CORNER MEGA-TOWERS (30 blocks tall) ===
        int[][] towerCorners = {{-60, -40}, {60, -40}, {-60, 40}, {60, 40}};
        for (int[] tc : towerCorners) {
            // Square tower base
            b.fillBox(tc[0] - 6, 0, tc[1] - 6, tc[0] + 6, 30, tc[1] + 6, Material.DEEPSLATE_BRICKS);
            b.fillBox(tc[0] - 5, 1, tc[1] - 5, tc[0] + 5, 29, tc[1] + 5, Material.AIR);
            // Spiral staircase
            b.spiralStaircase(tc[0], 1, 29, tc[1], 3, Material.DEEPSLATE_BRICK_STAIRS, Material.DEEPSLATE_BRICKS);
            // Conical roof
            b.spire(tc[0], 30, tc[1], 7, 10, Material.DEEPSLATE_TILES);
            // Top lantern
            b.setBlock(tc[0], 40, tc[1], Material.SOUL_LANTERN);
            // Battlements
            b.battlements(tc[0] - 6, 31, tc[1] - 6, tc[0] + 6, tc[1] - 6, Material.DEEPSLATE_BRICKS);
            b.battlements(tc[0] - 6, 31, tc[1] + 6, tc[0] + 6, tc[1] + 6, Material.DEEPSLATE_BRICKS);
            // Windows
            for (int y = 5; y <= 25; y += 5) {
                b.setBlock(tc[0], y, tc[1] - 6, Material.PURPLE_STAINED_GLASS);
                b.setBlock(tc[0], y, tc[1] + 6, Material.PURPLE_STAINED_GLASS);
                b.setBlock(tc[0] - 6, y, tc[1], Material.PURPLE_STAINED_GLASS);
                b.setBlock(tc[0] + 6, y, tc[1], Material.PURPLE_STAINED_GLASS);
            }
        }

        // === CENTRAL KEEP (30x30 with dome) ===
        b.fillWalls(-15, 0, -15, 15, 30, 15, Material.POLISHED_BLACKSTONE_BRICKS);
        b.hollowBox(-15, 1, -15, 15, 30, 15);
        b.fillFloor(-14, -14, 14, 14, 0, Material.CRYING_OBSIDIAN);

        // Dome atop central keep
        b.dome(0, 30, 0, 15, Material.DEEPSLATE_TILES);
        b.setBlock(0, 45, 0, Material.SOUL_LANTERN);

        // Rose windows (4 sides of keep)
        b.roseWindow(0, 20, -15, 5, Material.PURPLE_STAINED_GLASS, BlockFace.NORTH);
        b.roseWindow(0, 20, 15, 5, Material.PURPLE_STAINED_GLASS, BlockFace.SOUTH);
        b.roseWindow(-15, 20, 0, 5, Material.PURPLE_STAINED_GLASS, BlockFace.WEST);
        b.roseWindow(15, 20, 0, 5, Material.PURPLE_STAINED_GLASS, BlockFace.EAST);

        // === FLYING BUTTRESSES (connecting keep to outer walls) ===
        // North side
        b.flyingButtress(-15, 18, -15, -40, 8, -40, Material.DEEPSLATE_BRICKS);
        b.flyingButtress(15, 18, -15, 40, 8, -40, Material.DEEPSLATE_BRICKS);
        // South side
        b.flyingButtress(-15, 18, 15, -40, 8, 40, Material.DEEPSLATE_BRICKS);
        b.flyingButtress(15, 18, 15, 40, 8, 40, Material.DEEPSLATE_BRICKS);

        // === NAVE (central corridor from entrance to keep) ===
        b.fillBox(-6, 0, -40, 6, 18, -16, Material.DEEPSLATE_BRICKS);
        b.fillBox(-5, 1, -39, 5, 16, -16, Material.AIR);
        b.fillFloor(-5, -39, 5, -16, 0, Material.DEEPSLATE_TILES);
        // Nave pillars
        for (int z = -37; z <= -18; z += 4) {
            b.pillar(-5, 1, 15, z, Material.POLISHED_BLACKSTONE_BRICKS);
            b.pillar(5, 1, 15, z, Material.POLISHED_BLACKSTONE_BRICKS);
            b.setBlock(-5, 15, z, Material.SOUL_LANTERN);
            b.setBlock(5, 15, z, Material.SOUL_LANTERN);
        }
        // Gothic arches in nave ceiling
        for (int z = -36; z <= -20; z += 8)
            b.gothicArch(0, 12, z, 10, 6, Material.DEEPSLATE_BRICKS);

        // Grand entrance
        b.archDoorway(0, 1, -40, 8, 12, Material.POLISHED_BLACKSTONE);

        // === SIDE CHAPELS (east and west) ===
        // East chapel
        b.fillBox(16, 0, -10, 35, 12, 10, Material.DEEPSLATE_BRICKS);
        b.fillBox(17, 1, -9, 34, 11, 9, Material.AIR);
        b.fillFloor(17, -9, 34, 9, 0, Material.OBSIDIAN);
        // Chapel altar
        b.fillBox(30, 1, -2, 33, 3, 2, Material.CRYING_OBSIDIAN);
        b.setBlock(32, 4, 0, Material.SOUL_LANTERN);
        b.setBlock(31, 1, 0, Material.AMETHYST_BLOCK);
        // Pews
        for (int z = -7; z <= 7; z += 3)
            b.chair(20, 1, z, Material.POLISHED_BLACKSTONE_BRICK_STAIRS, BlockFace.EAST);

        // West chapel
        b.fillBox(-35, 0, -10, -16, 12, 10, Material.DEEPSLATE_BRICKS);
        b.fillBox(-34, 1, -9, -17, 11, 9, Material.AIR);
        b.fillFloor(-34, -9, -17, 9, 0, Material.OBSIDIAN);
        b.fillBox(-33, 1, -2, -30, 3, 2, Material.CRYING_OBSIDIAN);
        b.setBlock(-32, 4, 0, Material.SOUL_LANTERN);
        b.setBlock(-31, 1, 0, Material.AMETHYST_BLOCK);

        // === THRONE ROOM (inside keep) ===
        // Throne platform (north of keep center)
        b.fillBox(-4, 0, 8, 4, 3, 14, Material.OBSIDIAN);
        b.fillBox(-3, 3, 9, 3, 3, 13, Material.CRYING_OBSIDIAN);
        b.setBlock(0, 4, 12, Material.GOLD_BLOCK);
        b.setBlock(0, 5, 12, Material.GOLD_BLOCK);
        b.setBlock(-1, 5, 12, Material.AMETHYST_BLOCK);
        b.setBlock(1, 5, 12, Material.AMETHYST_BLOCK);
        b.setBlock(0, 6, 12, Material.AMETHYST_BLOCK);

        // Interior pillars in keep
        for (int x = -12; x <= 12; x += 6)
            for (int z = -12; z <= 12; z += 6) {
                if (Math.abs(x) < 5 && Math.abs(z) < 5) continue;
                b.pillar(x, 1, 28, z, Material.POLISHED_BLACKSTONE_BRICKS);
                b.setBlock(x, 28, z, Material.SOUL_LANTERN);
            }

        // Chandeliers in keep
        b.chandelier(0, 30, 0, 5);
        b.chandelier(-8, 30, -8, 3);
        b.chandelier(8, 30, -8, 3);
        b.chandelier(-8, 30, 8, 3);
        b.chandelier(8, 30, 8, 3);

        // === CRYPT (below the keep) ===
        b.fillBox(-20, -8, -20, 20, -1, 20, Material.DEEPSLATE_BRICKS);
        b.fillBox(-19, -7, -19, 19, -1, 19, Material.AIR);
        b.fillFloor(-19, -19, 19, 19, -8, Material.OBSIDIAN);

        // Crypt pillars
        for (int x = -15; x <= 15; x += 10)
            for (int z = -15; z <= 15; z += 10) {
                b.pillar(x, -7, -1, z, Material.POLISHED_BLACKSTONE_BRICKS);
            }

        // Sarcophagi (obsidian blocks)
        for (int i = -12; i <= 12; i += 8) {
            b.fillBox(i - 1, -7, -2, i + 1, -6, 2, Material.CRYING_OBSIDIAN);
        }

        // Crypt entrance (stairs down from keep)
        for (int s = 0; s < 8; s++) b.setBlock(0, -s, -14 + s, Material.DEEPSLATE_BRICK_STAIRS);

        // Amethyst crystal decorations
        b.setBlock(-14, 4, 0, Material.AMETHYST_CLUSTER);
        b.setBlock(14, 4, 0, Material.AMETHYST_CLUSTER);
        b.setBlock(0, 4, 14, Material.AMETHYST_CLUSTER);
        b.setBlock(0, 4, -14, Material.AMETHYST_CLUSTER);

        // Outer wall battlements
        b.battlements(-60, 21, -40, 60, -40, Material.DEEPSLATE_BRICKS);
        b.battlements(-60, 21, 40, 60, 40, Material.DEEPSLATE_BRICKS);
        b.battlements(-60, 21, -40, -60, 40, Material.DEEPSLATE_BRICKS);
        b.battlements(60, 21, -40, 60, 40, Material.DEEPSLATE_BRICKS);

        // Wall lighting for outer walls
        b.wallLighting(-58, 10, -40, 58, -40, 8, Material.SOUL_LANTERN);
        b.wallLighting(-58, 10, 40, 58, 40, 8, Material.SOUL_LANTERN);

        b.placeChest(0, 1, 0);
        b.placeChest(32, 1, 5);
        b.placeChest(-32, 1, -5);
        b.placeChest(0, -7, 10);
        b.placeChest(0, -7, -10);
        b.placeChest(0, 4, 10);
        b.setBossSpawn(0, -7, 0);
    }

    /**
     * Void Nexus — Floating obsidian platforms connected by thin bridges,
     * central nexus chamber with end gateway, amethyst crystals, soul fire,
     * 4 surrounding pylons.
     */
    public static void buildVoidNexus(StructureBuilder b) {
        // Central platform (main nexus)
        b.filledCircle(0, 0, 0, 14, Material.OBSIDIAN);
        b.filledCircle(0, 1, 0, 12, Material.END_STONE);
        b.filledCircle(0, 2, 0, 10, Material.CRYING_OBSIDIAN);

        // Central nexus chamber (cylinder)
        for (int y = 3; y <= 20; y++) b.circle(0, y, 0, 6, Material.OBSIDIAN);
        // Clear interior
        for (int y = 3; y <= 19; y++) b.filledCircle(0, y, 0, 5, Material.AIR);
        // Dome top
        b.dome(0, 20, 0, 6, Material.OBSIDIAN);

        // End gateway portal at center
        b.setBlock(0, 5, 0, Material.END_GATEWAY);
        b.setBlock(0, 6, 0, Material.END_GATEWAY);
        b.setBlock(0, 4, 0, Material.END_GATEWAY);
        // Obsidian frame
        b.fillBox(-1, 3, -1, 1, 3, 1, Material.CRYING_OBSIDIAN);
        b.fillBox(-1, 7, -1, 1, 7, 1, Material.CRYING_OBSIDIAN);

        // Amethyst crystal formations around nexus interior
        for (int a = 0; a < 360; a += 45) {
            int cx = (int) (4 * Math.cos(Math.toRadians(a)));
            int cz = (int) (4 * Math.sin(Math.toRadians(a)));
            b.setBlock(cx, 3, cz, Material.AMETHYST_BLOCK);
            b.setBlock(cx, 4, cz, Material.AMETHYST_CLUSTER);
            if (a % 90 == 0) {
                b.setBlock(cx, 5, cz, Material.AMETHYST_BLOCK);
                b.setBlock(cx, 6, cz, Material.AMETHYST_CLUSTER);
            }
        }

        // Void energy (soul fire ring)
        b.circle(0, 3, 0, 3, Material.SOUL_CAMPFIRE);

        // Nexus entrance
        b.fillBox(-2, 3, -6, 2, 7, -6, Material.AIR);

        // 4 surrounding pylons (at cardinal directions, connected by bridges)
        int[][] pylonPos = {{0, -22}, {0, 22}, {-22, 0}, {22, 0}};
        for (int[] pp : pylonPos) {
            // Pylon platform
            b.filledCircle(pp[0], 0, pp[1], 6, Material.OBSIDIAN);
            b.filledCircle(pp[0], 1, pp[1], 4, Material.END_STONE);

            // Pylon tower
            b.pillar(pp[0], 2, 18, pp[1], Material.OBSIDIAN);
            b.setBlock(pp[0], 19, pp[1], Material.END_ROD);
            b.setBlock(pp[0], 20, pp[1], Material.END_ROD);

            // Amethyst decorations at base
            b.setBlock(pp[0] - 2, 2, pp[1], Material.AMETHYST_BLOCK);
            b.setBlock(pp[0] + 2, 2, pp[1], Material.AMETHYST_BLOCK);
            b.setBlock(pp[0], 2, pp[1] - 2, Material.AMETHYST_BLOCK);
            b.setBlock(pp[0], 2, pp[1] + 2, Material.AMETHYST_BLOCK);
            b.setBlock(pp[0] - 2, 3, pp[1], Material.AMETHYST_CLUSTER);
            b.setBlock(pp[0] + 2, 3, pp[1], Material.AMETHYST_CLUSTER);

            // Thin bridge to center (2 wide)
            int dx = Integer.signum(pp[0]);
            int dz = Integer.signum(pp[1]);
            for (int i = 7; i <= 15; i++) {
                int bx = dx * i;
                int bz = dz * i;
                b.setBlock(bx, 1, bz, Material.OBSIDIAN);
                b.setBlock(bx + (dz != 0 ? 1 : 0), 1, bz + (dx != 0 ? 1 : 0), Material.OBSIDIAN);
            }
        }

        // Lighting
        b.setBlock(0, 19, 0, Material.SOUL_LANTERN);
        for (int y = 8; y <= 16; y += 4) {
            b.setBlock(5, y, 0, Material.SOUL_LANTERN);
            b.setBlock(-5, y, 0, Material.SOUL_LANTERN);
        }

        b.placeChest(3, 3, 3);
        b.placeChest(-3, 3, -3);
        b.placeChest(0, 2, 20);
        b.placeChest(0, 2, -20);
        b.setBossSpawn(0, 3, 5);
    }

    /**
     * Shattered Cathedral — Broken gothic cathedral, partially collapsed but grand.
     * Rose window, ribbed vault ceiling, altar, crypts, overgrown with chorus plants.
     */
    public static void buildShatteredCathedral(StructureBuilder b) {
        // Main nave shell (elongated)
        b.fillBox(-10, 0, -24, 10, 22, 24, Material.END_STONE_BRICKS);
        b.fillBox(-9, 1, -23, 9, 21, 23, Material.AIR);
        b.fillFloor(-9, -23, 9, 23, 0, Material.DEEPSLATE_TILES);

        // Ribbed vault ceiling (where intact, south half)
        for (int z = -10; z <= 23; z += 2) {
            b.setBlock(-6, 18, z, Material.DEEPSLATE_BRICKS);
            b.setBlock(6, 18, z, Material.DEEPSLATE_BRICKS);
            b.setBlock(-4, 20, z, Material.DEEPSLATE_BRICKS);
            b.setBlock(4, 20, z, Material.DEEPSLATE_BRICKS);
            b.setBlock(-2, 21, z, Material.DEEPSLATE_BRICKS);
            b.setBlock(2, 21, z, Material.DEEPSLATE_BRICKS);
            b.setBlock(0, 22, z, Material.DEEPSLATE_BRICKS);
        }

        // Rose window (north wall, facing outward)
        b.roseWindow(0, 14, -24, 6, Material.PURPLE_STAINED_GLASS, BlockFace.NORTH);

        // Collapse the north portion (decay)
        b.decay(-10, 12, -24, 10, 22, -14, 0.35);
        b.decay(-10, 1, -24, -6, 22, -18, 0.25);
        b.decay(6, 1, -24, 10, 22, -18, 0.25);

        // Fallen pillar debris on floor (north section)
        b.fillBox(-3, 1, -20, -1, 1, -16, Material.DEEPSLATE_BRICKS);
        b.fillBox(2, 1, -18, 4, 1, -14, Material.DEEPSLATE_BRICKS);

        // Altar (still standing, south end)
        b.fillBox(-3, 0, 18, 3, 2, 22, Material.CRYING_OBSIDIAN);
        b.fillBox(-2, 2, 19, 2, 2, 21, Material.OBSIDIAN);
        b.setBlock(0, 3, 20, Material.SOUL_CAMPFIRE);
        b.setBlock(0, 4, 20, Material.END_ROD);
        b.setBlock(-2, 3, 20, Material.CANDLE);
        b.setBlock(2, 3, 20, Material.CANDLE);

        // Standing pillars (alternating, some broken)
        for (int z = -15; z <= 18; z += 6) {
            b.pillar(-7, 1, 16, z, Material.POLISHED_BLACKSTONE_BRICKS);
            if (z > -10) b.pillar(7, 1, 16, z, Material.POLISHED_BLACKSTONE_BRICKS);
            else b.pillar(7, 1, 6, z, Material.POLISHED_BLACKSTONE_BRICKS); // broken
        }

        // Stained glass windows (where walls are intact)
        for (int z = -8; z <= 20; z += 4) {
            for (int y = 6; y <= 14; y++) {
                b.setBlock(-10, y, z, Material.PURPLE_STAINED_GLASS);
                b.setBlock(10, y, z, Material.PURPLE_STAINED_GLASS);
            }
        }

        // Overgrown with chorus plants (scattered)
        for (int x = -8; x <= 8; x += 4)
            for (int z = -20; z <= -10; z += 3) {
                b.setBlock(x, 1, z, Material.END_STONE);
                b.setBlock(x, 2, z, Material.CHORUS_PLANT);
                b.setBlock(x, 3, z, Material.CHORUS_FLOWER);
            }

        // Bell tower (front, partially collapsed)
        b.fillBox(-4, 0, -28, 4, 28, -24, Material.END_STONE_BRICKS);
        b.fillBox(-3, 1, -27, 3, 27, -25, Material.AIR);
        b.setBlock(0, 22, -26, Material.BELL);
        b.decay(-4, 18, -28, 4, 28, -24, 0.3);

        // Crypts (below nave)
        b.fillBox(-8, -6, -15, 8, -1, 15, Material.DEEPSLATE_BRICKS);
        b.fillBox(-7, -5, -14, 7, -1, 14, Material.AIR);
        b.fillFloor(-7, -14, 7, 14, -6, Material.OBSIDIAN);
        // Sarcophagi
        for (int z = -10; z <= 10; z += 5) {
            b.fillBox(-2, -5, z, 2, -4, z + 2, Material.CRYING_OBSIDIAN);
        }
        // Crypt entrance (stairs from nave)
        for (int s = 0; s < 6; s++) b.setBlock(0, -s, 16 + s, Material.DEEPSLATE_BRICK_STAIRS);

        // Scattered crying obsidian on ceiling
        b.scatter(-9, 19, -10, 9, 22, 23, Material.DEEPSLATE_BRICKS, Material.CRYING_OBSIDIAN, 0.08);

        // Entrance archway
        b.archDoorway(0, 1, -24, 6, 10, Material.DEEPSLATE_BRICKS);

        // Lighting (sparse, atmospheric)
        b.chandelier(0, 22, 5, 4);
        b.chandelier(0, 22, 15, 3);
        b.setBlock(-7, 8, 10, Material.SOUL_LANTERN);
        b.setBlock(7, 8, 10, Material.SOUL_LANTERN);
        b.setBlock(-7, 8, 0, Material.SOUL_LANTERN);
        b.setBlock(7, 8, 0, Material.SOUL_LANTERN);

        b.placeChest(0, 3, 19);
        b.placeChest(-6, 1, -10);
        b.placeChest(6, 1, 12);
        b.placeChest(0, -5, 0);
        b.setBossSpawn(0, 1, 10);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  AETHER STRUCTURES (7)
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Aether Village — Cloud village: 6-8 small quartz/white wool houses,
     * golden accents, flower gardens, central fountain, glass bridges.
     */
    public static void buildAetherVillage(StructureBuilder b) {
        // Central island platform
        b.filledCircle(0, 0, 0, 30, Material.WHITE_WOOL);
        b.filledCircle(0, 0, 0, 28, Material.QUARTZ_BLOCK);

        // Central fountain
        b.filledCircle(0, 0, 0, 4, Material.QUARTZ_BRICKS);
        b.circle(0, 1, 0, 4, Material.QUARTZ_BRICKS);
        b.circle(0, 2, 0, 3, Material.QUARTZ_BRICKS);
        b.filledCircle(0, 0, 0, 3, Material.WATER);
        b.setBlock(0, 1, 0, Material.WATER);
        b.setBlock(0, 2, 0, Material.WATER);
        b.setBlock(0, 3, 0, Material.GOLD_BLOCK);

        // Houses (6 around the perimeter)
        int[][] housePositions = {{-18, -12}, {18, -12}, {-20, 8}, {20, 8}, {-8, -22}, {8, 22}};
        for (int idx = 0; idx < 6; idx++) {
            int hx = housePositions[idx][0];
            int hz = housePositions[idx][1];
            // Foundation
            b.fillBox(hx - 4, 0, hz - 4, hx + 4, 0, hz + 4, Material.QUARTZ_BLOCK);
            // Walls
            b.fillWalls(hx - 4, 1, hz - 4, hx + 4, 4, hz + 4, Material.WHITE_WOOL);
            b.hollowBox(hx - 4, 1, hz - 4, hx + 4, 4, hz + 4);
            // Gold trim at base
            b.fillBox(hx - 4, 1, hz - 4, hx + 4, 1, hz - 4, Material.GOLD_BLOCK);
            b.fillBox(hx - 4, 1, hz + 4, hx + 4, 1, hz + 4, Material.GOLD_BLOCK);
            // Roof
            b.pyramidRoof(hx - 4, hz - 4, hx + 4, hz + 4, 4, Material.QUARTZ_SLAB);
            // Door
            b.setBlock(hx, 1, hz - 4, Material.AIR);
            b.setBlock(hx, 2, hz - 4, Material.AIR);
            // Window
            b.setBlock(hx + 4, 2, hz, Material.GLASS_PANE);
            b.setBlock(hx - 4, 2, hz, Material.GLASS_PANE);
            // Interior
            b.setBlock(hx - 2, 1, hz + 2, Material.WHITE_BED);
            b.chandelier(hx, 4, hz, 1);
        }

        // Flower gardens between houses
        int[][] gardenSpots = {{-10, 0}, {10, 0}, {0, -12}, {0, 12}};
        for (int[] g : gardenSpots) {
            for (int dx = -2; dx <= 2; dx++)
                for (int dz = -2; dz <= 2; dz++)
                    b.setRandomBlock(g[0] + dx, 1, g[1] + dz,
                            Material.DANDELION, Material.POPPY, Material.AZURE_BLUET,
                            Material.OXEYE_DAISY, Material.CORNFLOWER);
        }

        // Glass bridges to outer islands (2 satellite platforms)
        int[][] satellites = {{-38, 0}, {38, 0}};
        for (int[] sat : satellites) {
            // Bridge
            int dir = Integer.signum(sat[0]);
            for (int i = 29; i <= 33; i++) {
                b.setBlock(dir * i, 0, -1, Material.GLASS);
                b.setBlock(dir * i, 0, 0, Material.GLASS);
                b.setBlock(dir * i, 0, 1, Material.GLASS);
            }
            // Island
            b.filledCircle(sat[0], 0, sat[1], 8, Material.WHITE_WOOL);
            b.filledCircle(sat[0], 0, sat[1], 6, Material.QUARTZ_BLOCK);
        }

        // NPC spawn markers (pressure plates)
        for (int[] h : housePositions) b.setBlock(h[0], 1, h[1], Material.LIGHT_WEIGHTED_PRESSURE_PLATE);

        b.placeChest(0, 1, 5);
        b.placeChest(-18, 1, -10);
        b.placeChest(18, 1, 10);
    }

    /**
     * Crystal Cavern — Underground amethyst geode, crystal pillars, glowing pools,
     * winding tunnels, central crystal chamber.
     */
    public static void buildCrystalCavern(StructureBuilder b) {
        // Outer geode shell
        b.filledDome(0, 0, 0, 18, Material.CALCITE);
        b.filledDome(0, 0, 0, 16, Material.AMETHYST_BLOCK);
        b.filledDome(0, 0, 0, 14, Material.AIR);
        b.fillFloor(-14, -14, 14, 14, 0, Material.AMETHYST_BLOCK);

        // Crystal pillars (amethyst, irregular heights)
        int[][] crystalPos = {{-8, -8}, {8, -8}, {-8, 8}, {8, 8}, {0, -10}, {0, 10}, {-10, 0}, {10, 0}};
        for (int[] cp : crystalPos) {
            int height = 5 + (Math.abs(cp[0] + cp[1]) % 4);
            b.pillar(cp[0], 1, height, cp[1], Material.AMETHYST_BLOCK);
            b.setBlock(cp[0], height + 1, cp[1], Material.AMETHYST_CLUSTER);
        }

        // Glowing pools (sea lanterns under glass)
        int[][] poolPos = {{-5, -5}, {5, 5}, {-6, 4}, {6, -4}};
        for (int[] pp : poolPos) {
            b.filledCircle(pp[0], -1, pp[1], 2, Material.SEA_LANTERN);
            b.filledCircle(pp[0], 0, pp[1], 2, Material.LIGHT_BLUE_STAINED_GLASS);
        }

        // Central crystal chamber (raised platform)
        b.filledCircle(0, 0, 0, 5, Material.AMETHYST_BLOCK);
        b.filledCircle(0, 1, 0, 3, Material.AMETHYST_BLOCK);
        b.setBlock(0, 2, 0, Material.AMETHYST_BLOCK);
        b.setBlock(0, 3, 0, Material.AMETHYST_CLUSTER);
        // Large crystal (budding amethyst core)
        b.setBlock(-1, 2, 0, Material.BUDDING_AMETHYST);
        b.setBlock(1, 2, 0, Material.BUDDING_AMETHYST);
        b.setBlock(0, 2, -1, Material.BUDDING_AMETHYST);
        b.setBlock(0, 2, 1, Material.BUDDING_AMETHYST);

        // Winding tunnel entrance (south)
        b.fillBox(-2, 0, -18, 2, 4, -14, Material.AIR);
        // Bend in tunnel
        b.fillBox(-4, 0, -22, 0, 4, -18, Material.AIR);
        b.fillBox(-4, 0, -22, 0, 0, -18, Material.AMETHYST_BLOCK);
        b.fillWalls(-5, 0, -23, 1, 5, -17, Material.CALCITE);

        // Second entrance (east)
        b.fillBox(14, 0, -2, 18, 4, 2, Material.AIR);

        // Amethyst clusters on walls (scattered)
        b.scatter(-14, 1, -14, 14, 14, 14, Material.AMETHYST_BLOCK, Material.BUDDING_AMETHYST, 0.1);

        // Ambient lighting from crystals
        b.setBlock(0, 12, 0, Material.SEA_LANTERN);
        b.setBlock(-8, 8, -8, Material.SEA_LANTERN);
        b.setBlock(8, 8, -8, Material.SEA_LANTERN);
        b.setBlock(-8, 8, 8, Material.SEA_LANTERN);
        b.setBlock(8, 8, 8, Material.SEA_LANTERN);

        b.placeChest(0, 1, 0);
        b.placeChest(-10, 1, 5);
        b.placeChest(10, 1, -5);
        b.setBossSpawn(0, 2, 4);
    }

    /**
     * Storm Peak Tower — Tall prismarine/quartz tower on mountain peak,
     * lightning rod, observatory, cloud floor, wind-catcher arms.
     */
    public static void buildStormPeakTower(StructureBuilder b) {
        // Tower base (wider)
        for (int y = 0; y <= 5; y++) b.filledCircle(0, y, 0, 8 - y / 2, Material.PRISMARINE_BRICKS);
        for (int y = 1; y <= 5; y++) b.filledCircle(0, y, 0, 6 - y / 2, Material.AIR);
        b.filledCircle(0, 0, 0, 7, Material.QUARTZ_BLOCK);

        // Main tower shaft
        for (int y = 6; y <= 40; y++) {
            b.circle(0, y, 0, 5, Material.PRISMARINE_BRICKS);
            // Quartz trim every 8 blocks
            if (y % 8 == 0) b.circle(0, y, 0, 5, Material.QUARTZ_BLOCK);
        }
        // Clear interior
        for (int y = 6; y <= 39; y++) b.filledCircle(0, y, 0, 4, Material.AIR);

        // Floors at intervals
        int[] floors = {0, 10, 20, 30};
        for (int fy : floors) b.filledCircle(0, fy, 0, 4, Material.QUARTZ_BLOCK);

        // Spiral staircase
        b.spiralStaircase(0, 1, 39, 0, 3, Material.QUARTZ_STAIRS, Material.PRISMARINE_BRICKS);

        // Observatory (top level y=38-45)
        b.filledCircle(0, 40, 0, 7, Material.PRISMARINE_BRICKS);
        b.filledCircle(0, 40, 0, 5, Material.QUARTZ_BLOCK);
        for (int y = 41; y <= 45; y++) {
            b.circle(0, y, 0, 7, Material.PRISMARINE_BRICKS);
            b.filledCircle(0, y, 0, 6, Material.AIR);
        }
        // Glass dome over observatory
        b.dome(0, 45, 0, 7, Material.GLASS);
        // Telescope (end rods)
        b.setBlock(0, 41, -4, Material.END_ROD);
        b.setBlock(0, 42, -4, Material.END_ROD);
        b.setBlock(0, 43, -5, Material.END_ROD);

        // Lightning rod at very top
        b.setBlock(0, 52, 0, Material.LIGHTNING_ROD);
        b.setBlock(0, 51, 0, Material.IRON_BLOCK);
        b.setBlock(0, 50, 0, Material.IRON_BLOCK);

        // Cloud floor at y=20 (white wool)
        b.filledCircle(0, 20, 0, 4, Material.WHITE_WOOL);

        // 4 wind-catcher arms (cross pattern extending from tower)
        int[][] arms = {{-12, 0}, {12, 0}, {0, -12}, {0, 12}};
        for (int[] arm : arms) {
            int dx = Integer.signum(arm[0]);
            int dz = Integer.signum(arm[1]);
            for (int i = 5; i <= 12; i++) {
                b.setBlock(dx * i, 35, dz * i, Material.PRISMARINE_BRICKS);
                b.setBlock(dx * i, 36, dz * i, Material.IRON_BARS);
            }
            // Fan blades (end rods)
            b.setBlock(arm[0], 37, arm[1], Material.END_ROD);
            b.setBlock(arm[0], 34, arm[1], Material.END_ROD);
        }

        // Storm imagery — prismarine accents on exterior
        b.scatter(0, 6, 0, 5, 40, 5, Material.PRISMARINE_BRICKS, Material.DARK_PRISMARINE, 0.1);

        // Windows
        for (int y = 8; y <= 38; y += 4) {
            b.setBlock(5, y, 0, Material.GLASS_PANE);
            b.setBlock(-5, y, 0, Material.GLASS_PANE);
            b.setBlock(0, y, 5, Material.GLASS_PANE);
            b.setBlock(0, y, -5, Material.GLASS_PANE);
        }

        // Entrance
        b.fillBox(-2, 1, -8, 2, 4, -5, Material.AIR);
        b.archDoorway(0, 1, -8, 4, 5, Material.QUARTZ_BLOCK);

        // Interior furnishing
        b.setBlock(-2, 11, -2, Material.ENCHANTING_TABLE);
        b.bookshelfWall(-3, 11, 3, 3, 14);
        b.chandelier(0, 40, 0, 2);

        b.placeChest(0, 41, 3);
        b.placeChest(0, 11, 0);
        b.setBossSpawn(0, 41, 0);
    }

    /**
     * Pitlord's Lair — Dark cave beneath Aether, blackstone/deepslate intrusion,
     * lava pit, demonic altar, prison for angels, soul fire braziers.
     */
    public static void buildPitlordLair(StructureBuilder b) {
        // Cave shell (dark intrusion beneath bright Aether)
        b.filledDome(0, 0, 0, 18, Material.DEEPSLATE);
        b.filledDome(0, 0, 0, 16, Material.AIR);
        b.fillFloor(-16, -16, 16, 16, 0, Material.BLACKSTONE);

        // Scatter variation on walls
        b.scatter(-18, 0, -18, 18, 18, 18, Material.DEEPSLATE, Material.DEEPSLATE_BRICKS, 0.2);

        // Central lava pit
        b.filledCircle(0, -1, 0, 6, Material.LAVA);
        b.filledCircle(0, 0, 0, 6, Material.LAVA);
        b.circle(0, 0, 0, 7, Material.BLACKSTONE);

        // Demonic altar (south of pit)
        b.fillBox(-3, 0, 8, 3, 3, 12, Material.POLISHED_BLACKSTONE);
        b.fillBox(-2, 3, 9, 2, 3, 11, Material.CRYING_OBSIDIAN);
        b.setBlock(0, 4, 10, Material.SOUL_CAMPFIRE);
        b.setBlock(-2, 4, 10, Material.CANDLE);
        b.setBlock(2, 4, 10, Material.CANDLE);
        // Nether gold ore demonic symbols
        b.setBlock(-1, 2, 9, Material.NETHER_GOLD_ORE);
        b.setBlock(1, 2, 9, Material.NETHER_GOLD_ORE);
        b.setBlock(0, 2, 11, Material.NETHER_GOLD_ORE);

        // Prison for Aether angels (cages)
        int[][] cages = {{-12, -8}, {12, -8}, {-12, 8}};
        for (int[] c : cages) {
            b.fillBox(c[0] - 2, 0, c[1] - 2, c[0] + 2, 5, c[1] + 2, Material.IRON_BARS);
            b.fillBox(c[0] - 1, 1, c[1] - 1, c[0] + 1, 4, c[1] + 1, Material.AIR);
            b.fillFloor(c[0] - 1, c[1] - 1, c[0] + 1, c[1] + 1, 0, Material.BLACKSTONE);
            // Chain from ceiling
            b.setBlock(c[0], 6, c[1], Material.IRON_CHAIN);
            b.setBlock(c[0], 7, c[1], Material.IRON_CHAIN);
        }

        // Soul fire braziers (6)
        for (int a = 0; a < 360; a += 60) {
            int bx = (int) (13 * Math.cos(Math.toRadians(a)));
            int bz = (int) (13 * Math.sin(Math.toRadians(a)));
            b.fillBox(bx - 1, 0, bz - 1, bx + 1, 1, bz + 1, Material.POLISHED_BLACKSTONE);
            b.setBlock(bx, 2, bz, Material.SOUL_CAMPFIRE);
        }

        // Magma veins in floor
        b.scatter(-14, 0, -14, 14, 0, 14, Material.BLACKSTONE, Material.MAGMA_BLOCK, 0.08);

        // Entrance tunnel (narrow, from above)
        b.fillBox(-2, 14, -18, 2, 18, -16, Material.AIR);
        b.fillBox(-2, 0, -16, 2, 18, -14, Material.AIR);
        b.spiralStaircase(0, 1, 18, -15, 2, Material.DEEPSLATE_BRICK_STAIRS, Material.DEEPSLATE);

        b.placeChest(0, 4, 9);
        b.placeChest(-12, 1, -10);
        b.placeChest(12, 1, 10);
        b.setBossSpawn(0, 1, 5);
    }

    /**
     * Warrior's Sky Fortress — MASSIVE floating fortress: 100x50, quartz walls,
     * golden trim, grand entrance, throne room, sky bridges, open-air arena,
     * banner-adorned walls.
     */
    public static void buildWarriorSkyFortress(StructureBuilder b) {
        // Main platform
        b.fillBox(-50, 0, -25, 50, 0, 25, Material.QUARTZ_BLOCK);
        b.fillBox(-48, 0, -23, 48, 0, 23, Material.QUARTZ_BRICKS);

        // Outer walls
        b.fillWalls(-50, 1, -25, 50, 16, 25, Material.QUARTZ_BLOCK);
        b.hollowBox(-50, 1, -25, 50, 16, 25);

        // Gold trim bands
        for (int y : new int[]{1, 8, 16}) {
            b.fillBox(-50, y, -25, 50, y, -25, Material.GOLD_BLOCK);
            b.fillBox(-50, y, 25, 50, y, 25, Material.GOLD_BLOCK);
            b.fillBox(-50, y, -25, -50, y, 25, Material.GOLD_BLOCK);
            b.fillBox(50, y, -25, 50, y, 25, Material.GOLD_BLOCK);
        }

        // Grand entrance with twin towers (south)
        b.fillBox(-5, 1, -25, 5, 12, -25, Material.AIR);
        b.gothicArch(0, 1, -25, 10, 13, Material.GOLD_BLOCK);

        // Twin entrance towers
        b.roundTower(-12, 0, -25, 5, 22, Material.QUARTZ_BLOCK,
                Material.GOLD_BLOCK, Material.YELLOW_STAINED_GLASS);
        b.roundTower(12, 0, -25, 5, 22, Material.QUARTZ_BLOCK,
                Material.GOLD_BLOCK, Material.YELLOW_STAINED_GLASS);

        // Battlements on all walls
        b.battlements(-50, 17, -25, 50, -25, Material.QUARTZ_BLOCK);
        b.battlements(-50, 17, 25, 50, 25, Material.QUARTZ_BLOCK);
        b.battlements(-50, 17, -25, -50, 25, Material.QUARTZ_BLOCK);
        b.battlements(50, 17, -25, 50, 25, Material.QUARTZ_BLOCK);

        // Banners on walls
        for (int x = -44; x <= 44; x += 8) {
            b.setBlock(x, 12, -25, Material.WHITE_BANNER);
            b.setBlock(x, 12, 25, Material.WHITE_BANNER);
        }

        // Throne room (center-north)
        b.fillBox(-12, 0, 8, 12, 20, 24, Material.QUARTZ_BRICKS);
        b.fillBox(-11, 1, 9, 11, 19, 23, Material.AIR);
        b.fillFloor(-11, 9, 11, 23, 0, Material.GOLD_BLOCK);
        // Throne
        b.fillBox(-2, 1, 20, 2, 3, 22, Material.GOLD_BLOCK);
        b.setBlock(0, 4, 21, Material.GOLD_BLOCK);
        b.setBlock(0, 5, 21, Material.GOLD_BLOCK);
        b.setBlock(-1, 5, 21, Material.GOLD_BLOCK);
        b.setBlock(1, 5, 21, Material.GOLD_BLOCK);
        // Carpet runner
        for (int z = 9; z <= 20; z++) b.setBlock(0, 1, z, Material.RED_CARPET);
        // Pillars
        for (int x = -8; x <= 8; x += 4) {
            b.pillar(x, 1, 18, 12, Material.QUARTZ_PILLAR);
        }
        b.chandelier(0, 20, 16, 4);

        // Open-air arena at top (rooftop)
        b.fillBox(-30, 16, -20, -10, 16, 0, Material.QUARTZ_BLOCK);
        b.filledCircle(-20, 16, -10, 10, Material.QUARTZ_BRICKS);
        // Arena pillars
        for (int a = 0; a < 360; a += 45) {
            int px = -20 + (int) (8 * Math.cos(Math.toRadians(a)));
            int pz = -10 + (int) (8 * Math.sin(Math.toRadians(a)));
            b.pillar(px, 17, 22, pz, Material.QUARTZ_PILLAR);
            b.setBlock(px, 22, pz, Material.END_ROD);
        }

        // Sky bridges (connecting sections)
        b.fillBox(-1, 10, -5, 1, 10, 8, Material.QUARTZ_BLOCK);
        b.fillBox(-1, 11, -5, 1, 11, 8, Material.AIR);

        // Side rooms (barracks east, armory west)
        // Barracks
        b.furnishedRoom(20, 0, -18, 45, -5, 4,
                Material.QUARTZ_BLOCK, Material.QUARTZ_BRICKS, Material.QUARTZ_BLOCK);
        for (int z = -16; z <= -8; z += 4) {
            b.setBlock(25, 1, z, Material.WHITE_BED);
            b.setBlock(35, 1, z, Material.WHITE_BED);
        }

        // Armory
        b.furnishedRoom(-45, 0, -18, -20, -5, 4,
                Material.QUARTZ_BLOCK, Material.QUARTZ_BRICKS, Material.QUARTZ_BLOCK);
        b.setBlock(-35, 1, -15, Material.SMITHING_TABLE);
        b.setBlock(-33, 1, -15, Material.ANVIL);
        b.setBlock(-31, 1, -15, Material.GRINDSTONE);

        // Floor pattern
        for (int x = -45; x <= 45; x += 4)
            for (int z = -20; z <= 20; z += 4)
                b.setBlock(x, 0, z, Material.GOLD_BLOCK);

        // Windows
        for (int x = -44; x <= 44; x += 6) {
            for (int y = 5; y <= 12; y++) {
                b.setBlock(x, y, -25, Material.YELLOW_STAINED_GLASS);
                b.setBlock(x, y, 25, Material.YELLOW_STAINED_GLASS);
            }
        }

        b.placeChest(0, 1, 20);
        b.placeChest(-35, 1, -12);
        b.placeChest(35, 1, -12);
        b.placeChest(-20, 17, -10);
        b.setBossSpawn(0, 1, 15);
    }

    /**
     * Dragonkin Temple — Ancient martial arts temple, stone bricks/prismarine,
     * training courtyard, inner sanctum, meditation pool, weapon displays,
     * dragon carvings (gold blocks in patterns).
     */
    public static void buildDragonkinTemple(StructureBuilder b) {
        // Temple platform (stepped)
        for (int step = 0; step < 3; step++) {
            int r = 22 - step * 3;
            b.fillBox(-r, step, -r, r, step, r, Material.STONE_BRICKS);
        }

        // Main temple building
        b.fillWalls(-16, 3, -16, 16, 16, 16, Material.STONE_BRICKS);
        b.hollowBox(-16, 4, -16, 16, 16, 16);
        b.fillFloor(-15, -15, 15, 15, 3, Material.DARK_PRISMARINE);

        // Prismarine pillar columns (interior)
        for (int x = -12; x <= 12; x += 6)
            for (int z = -12; z <= 12; z += 6) {
                if (Math.abs(x) <= 3 && Math.abs(z) <= 3) continue;
                b.pillar(x, 4, 15, z, Material.PRISMARINE_BRICKS);
            }

        // Training courtyard (south exterior)
        b.fillBox(-12, 2, -22, 12, 2, -17, Material.STONE_BRICKS);
        b.fillBox(-10, 3, -21, 10, 3, -18, Material.SAND); // sand training floor
        // Training dummies
        for (int x = -8; x <= 8; x += 4) {
            b.pillar(x, 3, 5, -20, Material.OAK_FENCE);
            b.setBlock(x, 6, -20, Material.CARVED_PUMPKIN);
        }

        // Inner sanctum (raised platform center)
        b.fillBox(-5, 3, -5, 5, 5, 5, Material.PRISMARINE_BRICKS);
        b.fillBox(-4, 5, -4, 4, 5, 4, Material.DARK_PRISMARINE);
        b.setBlock(0, 6, 0, Material.BEACON);
        b.setBlock(0, 7, 0, Material.END_ROD);

        // Meditation pool (east wing)
        b.fillBox(8, 3, -6, 15, 3, 6, Material.PRISMARINE_BRICKS);
        b.fillBox(9, 3, -5, 14, 3, 5, Material.WATER);
        b.setBlock(12, 4, 0, Material.LILY_PAD);

        // Weapon displays (west wing)
        b.fillBox(-15, 3, -6, -8, 8, 6, Material.STONE_BRICKS);
        b.fillBox(-14, 4, -5, -9, 7, 5, Material.AIR);
        // Display cases (item frames simulated with fences)
        for (int z = -4; z <= 4; z += 2) {
            b.setBlock(-14, 4, z, Material.OAK_FENCE);
            b.setBlock(-14, 5, z, Material.OAK_FENCE);
        }
        b.wallLighting(-14, 6, -5, -9, -5, 3, Material.LANTERN);

        // Dragon carvings (gold blocks in dragon-scale pattern on walls)
        for (int y = 8; y <= 14; y += 2) {
            b.setBlock(-16, y, 0, Material.GOLD_BLOCK);
            b.setBlock(16, y, 0, Material.GOLD_BLOCK);
            b.setBlock(-16, y + 1, -1, Material.GOLD_BLOCK);
            b.setBlock(-16, y + 1, 1, Material.GOLD_BLOCK);
            b.setBlock(16, y + 1, -1, Material.GOLD_BLOCK);
            b.setBlock(16, y + 1, 1, Material.GOLD_BLOCK);
        }

        // Roof
        b.pyramidRoof(-16, -16, 16, 16, 16, Material.DARK_PRISMARINE);

        // Entrance (south)
        b.archDoorway(0, 3, -16, 6, 8, Material.PRISMARINE_BRICKS);

        // Lighting
        b.chandelier(0, 16, 0, 3);
        b.wallLighting(-15, 8, -16, 15, -16, 5, Material.LANTERN);
        b.wallLighting(-15, 8, 16, 15, 16, 5, Material.LANTERN);

        b.placeChest(0, 6, 3);
        b.placeChest(-12, 4, 0);
        b.placeChest(12, 4, 4);
        b.setBossSpawn(0, 6, -3);
    }

    /**
     * Aether Ancient Ruins — Crumbling quartz ruins, broken columns, overgrown
     * with flowers, hidden chambers, treasure buried in rubble.
     */
    public static void buildAetherAncientRuins(StructureBuilder b) {
        // Foundation remnants
        b.fillBox(-14, 0, -14, 14, 0, 14, Material.QUARTZ_BLOCK);
        b.scatter(-14, 0, -14, 14, 0, 14, Material.QUARTZ_BLOCK, Material.CRACKED_STONE_BRICKS, 0.2);

        // Broken columns (varying heights, some fallen)
        int[][] colPos = {{-10, -10}, {10, -10}, {-10, 10}, {10, 10},
                {-10, 0}, {10, 0}, {0, -10}, {0, 10}};
        for (int i = 0; i < colPos.length; i++) {
            int height = (i < 4) ? 3 + (i % 3) : 6 + (i % 2); // varying
            b.pillar(colPos[i][0], 1, height, colPos[i][1], Material.QUARTZ_PILLAR);
        }
        // Fallen column on ground
        b.fillBox(-5, 1, -8, -5, 1, -3, Material.QUARTZ_PILLAR);
        b.fillBox(3, 1, 5, 8, 1, 5, Material.QUARTZ_PILLAR);

        // Partial walls (ruins of rooms)
        b.fillBox(-12, 1, -12, -8, 4, -12, Material.QUARTZ_BLOCK);
        b.fillBox(-12, 1, -12, -12, 3, -8, Material.QUARTZ_BLOCK);
        b.decay(-12, 1, -12, -8, 4, -8, 0.3);

        b.fillBox(8, 1, 8, 12, 5, 12, Material.QUARTZ_BLOCK);
        b.fillBox(9, 1, 9, 11, 4, 11, Material.AIR);
        b.decay(8, 3, 8, 12, 5, 12, 0.25);

        // Overgrown (flowers everywhere)
        for (int x = -12; x <= 12; x += 2)
            for (int z = -12; z <= 12; z += 2)
                if (b.getBlock(x, 1, z) == Material.AIR)
                    b.setRandomBlock(x, 1, z, Material.DANDELION, Material.POPPY,
                            Material.AZURE_BLUET, Material.CORNFLOWER, Material.OXEYE_DAISY,
                            Material.GRASS_BLOCK);

        // Vine/moss coverage
        b.addVines(-12, 1, -12, 12, 6, 12, 0.15);
        b.scatter(-14, 0, -14, 14, 0, 14, Material.QUARTZ_BLOCK, Material.MOSS_BLOCK, 0.1);

        // Hidden chamber (underground)
        b.fillBox(-5, -5, -5, 5, -1, 5, Material.QUARTZ_BLOCK);
        b.fillBox(-4, -4, -4, 4, -1, 4, Material.AIR);
        b.fillFloor(-4, -4, 4, 4, -5, Material.QUARTZ_BRICKS);
        // Entrance (broken floor section)
        b.setBlock(0, 0, 0, Material.AIR);
        b.setBlock(0, -1, 0, Material.AIR);

        // Treasure in rubble
        b.placeChest(-10, 0, -10);
        b.placeChest(10, 1, 10);
        b.placeChest(0, -4, 0);
        b.setBossSpawn(0, 1, 3);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  LUNAR STRUCTURES (6)
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Invaderling Outpost — Small military base: gray concrete walls, reinforced glass,
     * antenna, barracks, command room, supply storage.
     */
    public static void buildInvaderlingOutpost(StructureBuilder b) {
        // Foundation
        b.fillBox(-9, 0, -9, 9, 0, 9, Material.GRAY_CONCRETE);

        // Walls
        b.fillWalls(-9, 1, -9, 9, 6, 9, Material.GRAY_CONCRETE);
        b.hollowBox(-9, 1, -9, 9, 6, 9);

        // Reinforced glass windows
        for (int x = -6; x <= 6; x += 4) {
            b.setBlock(x, 3, -9, Material.GRAY_STAINED_GLASS);
            b.setBlock(x, 4, -9, Material.GRAY_STAINED_GLASS);
            b.setBlock(x, 3, 9, Material.GRAY_STAINED_GLASS);
            b.setBlock(x, 4, 9, Material.GRAY_STAINED_GLASS);
        }

        // Floor
        b.fillFloor(-8, -8, 8, 8, 0, Material.LIGHT_GRAY_CONCRETE);

        // Antenna (corner, iron bars + end rod)
        b.pillar(8, 7, 12, 8, Material.IRON_BARS);
        b.setBlock(8, 13, 8, Material.END_ROD);
        b.setBlock(8, 14, 8, Material.LIGHTNING_ROD);

        // Barracks (south half)
        b.fillBox(-8, 0, 2, -2, 5, 8, Material.GRAY_CONCRETE);
        b.fillBox(-7, 1, 3, -3, 4, 7, Material.AIR);
        b.setBlock(-6, 1, 5, Material.WHITE_BED);
        b.setBlock(-4, 1, 5, Material.WHITE_BED);
        b.setBlock(-5, 4, 5, Material.LANTERN);

        // Command room (north)
        b.fillBox(-8, 0, -8, 0, 5, -2, Material.GRAY_CONCRETE);
        b.fillBox(-7, 1, -7, -1, 4, -3, Material.AIR);
        b.table(-4, 1, -5);
        b.chair(-3, 1, -5, Material.POLISHED_ANDESITE_STAIRS, BlockFace.WEST);
        b.setBlock(-6, 1, -7, Material.REDSTONE_LAMP);
        b.setBlock(-2, 1, -7, Material.LEVER);

        // Supply storage (east)
        b.fillBox(3, 0, -8, 8, 5, 0, Material.GRAY_CONCRETE);
        b.fillBox(4, 1, -7, 7, 4, -1, Material.AIR);
        b.setBlock(5, 1, -5, Material.BARREL);
        b.setBlock(6, 1, -5, Material.BARREL);
        b.setBlock(7, 1, -5, Material.BARREL);

        // Ceiling
        b.fillFloor(-8, -8, 8, 8, 6, Material.GRAY_CONCRETE);

        // Entrance
        b.setBlock(0, 1, -9, Material.AIR);
        b.setBlock(0, 2, -9, Material.AIR);
        b.setBlock(1, 1, -9, Material.AIR);
        b.setBlock(1, 2, -9, Material.AIR);

        // Lighting
        b.chandelier(0, 6, 0, 1);

        b.placeChest(6, 1, -3);
        b.placeChest(-6, 1, 3);
        b.setBossSpawn(0, 1, 0);
    }

    /**
     * Underground Hive — Alien cave system: sculk/deepslate walls, egg pods,
     * interconnected chambers, queen's chamber, tunnels.
     */
    public static void buildUndergroundHive(StructureBuilder b) {
        // Main queen's chamber (large)
        b.filledDome(0, 0, 0, 16, Material.SCULK);
        b.filledDome(0, 0, 0, 14, Material.AIR);
        b.fillFloor(-14, -14, 14, 14, 0, Material.SCULK);

        // Organic wall variation
        b.scatter(-16, 0, -16, 16, 16, 16, Material.SCULK, Material.DEEPSLATE, 0.15);
        b.scatter(-14, 0, -14, 14, 0, 14, Material.SCULK, Material.SCULK_VEIN, 0.1);

        // Egg pods (soul lanterns in sculk niches)
        for (int a = 0; a < 360; a += 30) {
            int ex = (int) (10 * Math.cos(Math.toRadians(a)));
            int ez = (int) (10 * Math.sin(Math.toRadians(a)));
            b.setBlock(ex, 1, ez, Material.SCULK);
            b.setBlock(ex, 2, ez, Material.SOUL_LANTERN);
            b.setBlock(ex, 3, ez, Material.SCULK);
        }

        // Central nest platform for queen
        b.filledCircle(0, 0, 0, 5, Material.SCULK_CATALYST);
        b.filledCircle(0, 1, 0, 3, Material.SCULK);
        b.setBlock(0, 2, 0, Material.SCULK_SHRIEKER);

        // Interconnected sub-chambers (tunnels branching out)
        int[][] subChambers = {{-20, -15}, {20, -15}, {-18, 18}, {18, 18}};
        for (int[] sc : subChambers) {
            b.filledDome(sc[0], 0, sc[1], 8, Material.SCULK);
            b.filledDome(sc[0], 0, sc[1], 6, Material.AIR);
            b.fillFloor(sc[0] - 6, sc[1] - 6, sc[0] + 6, sc[1] + 6, 0, Material.DEEPSLATE);
            // Egg cluster in each sub-chamber
            b.setBlock(sc[0], 1, sc[1], Material.SOUL_LANTERN);
            b.setBlock(sc[0] - 1, 1, sc[1], Material.SOUL_LANTERN);
            b.setBlock(sc[0] + 1, 1, sc[1], Material.SOUL_LANTERN);

            // Tunnel connecting to main chamber
            int dx = Integer.signum(-sc[0]);
            int dz = Integer.signum(-sc[1]);
            for (int i = 0; i < 8; i++) {
                int tx = sc[0] + dx * (7 + i);
                int tz = sc[1] + dz * (7 + i);
                b.fillBox(tx - 1, 0, tz - 1, tx + 1, 3, tz + 1, Material.AIR);
                // Tunnel walls
                b.setBlock(tx - 2, 0, tz, Material.SCULK);
                b.setBlock(tx + 2, 0, tz, Material.SCULK);
                b.setBlock(tx, 4, tz, Material.SCULK);
            }
        }

        // Sculk sensor proximity detectors
        b.setBlock(-6, 1, 0, Material.SCULK_SENSOR);
        b.setBlock(6, 1, 0, Material.SCULK_SENSOR);
        b.setBlock(0, 1, -6, Material.SCULK_SENSOR);
        b.setBlock(0, 1, 6, Material.SCULK_SENSOR);

        b.placeChest(0, 1, 5);
        b.placeChest(-18, 1, -15);
        b.placeChest(18, 1, 18);
        b.setBossSpawn(0, 2, 0);
    }

    /**
     * Alien Citadel — LARGE alien fortress: gray concrete/iron/glass, 80x40,
     * command center, hangar bay, prison block, research lab, power core, defenses.
     */
    public static void buildAlienCitadel(StructureBuilder b) {
        // Main platform
        b.fillBox(-40, 0, -20, 40, 0, 20, Material.GRAY_CONCRETE);

        // Outer walls
        b.fillWalls(-40, 1, -20, 40, 16, 20, Material.GRAY_CONCRETE);
        b.hollowBox(-40, 1, -20, 40, 16, 20);

        // Iron reinforcement bands
        for (int y : new int[]{4, 8, 12}) {
            b.fillBox(-40, y, -20, 40, y, -20, Material.IRON_BLOCK);
            b.fillBox(-40, y, 20, 40, y, 20, Material.IRON_BLOCK);
            b.fillBox(-40, y, -20, -40, y, 20, Material.IRON_BLOCK);
            b.fillBox(40, y, -20, 40, y, 20, Material.IRON_BLOCK);
        }

        // Floor
        b.fillFloor(-39, -19, 39, 19, 0, Material.LIGHT_GRAY_CONCRETE);

        // Command center (center-north, elevated)
        b.fillBox(-10, 0, 8, 10, 20, 19, Material.IRON_BLOCK);
        b.fillBox(-9, 1, 9, 9, 19, 18, Material.AIR);
        b.fillFloor(-9, 9, 9, 18, 0, Material.GRAY_CONCRETE);
        // Control panels (redstone)
        b.fillBox(-8, 1, 17, 8, 3, 17, Material.REDSTONE_LAMP);
        b.fillBox(-8, 1, 16, 8, 1, 16, Material.SMOOTH_STONE);
        b.setBlock(0, 2, 16, Material.LEVER);
        // Windows
        for (int x = -7; x <= 7; x += 2) b.setBlock(x, 10, 19, Material.GRAY_STAINED_GLASS_PANE);

        // Hangar bay (west wing)
        b.fillBox(-39, 0, -15, -20, 12, 5, Material.GRAY_CONCRETE);
        b.fillBox(-38, 1, -14, -21, 11, 4, Material.AIR);
        b.fillFloor(-38, -14, -21, 4, 0, Material.SMOOTH_STONE);
        // Hangar doors (open bay on south)
        b.fillBox(-38, 1, -15, -21, 8, -15, Material.AIR);

        // Prison block (east wing)
        b.fillBox(20, 0, -15, 39, 10, 5, Material.GRAY_CONCRETE);
        b.fillBox(21, 1, -14, 38, 9, 4, Material.AIR);
        for (int cell = 0; cell < 4; cell++) {
            int cz = -12 + cell * 4;
            b.fillBox(25, 0, cz, 30, 6, cz + 2, Material.IRON_BLOCK);
            b.fillBox(26, 1, cz, 29, 5, cz + 2, Material.AIR);
            for (int y = 1; y <= 5; y++) b.setBlock(25, y, cz + 1, Material.IRON_BARS);
        }

        // Research lab (southeast)
        b.fillBox(20, 0, 8, 38, 10, 19, Material.GRAY_CONCRETE);
        b.fillBox(21, 1, 9, 37, 9, 18, Material.AIR);
        b.fillFloor(21, 9, 37, 18, 0, Material.WHITE_CONCRETE);
        b.setBlock(25, 1, 14, Material.BREWING_STAND);
        b.setBlock(27, 1, 14, Material.CAULDRON);
        b.setBlock(29, 1, 14, Material.ENCHANTING_TABLE);

        // Power core room (underground center)
        b.fillBox(-8, -8, -8, 8, -1, 8, Material.IRON_BLOCK);
        b.fillBox(-7, -7, -7, 7, -1, 7, Material.AIR);
        b.fillFloor(-7, -7, 7, 7, -8, Material.GRAY_CONCRETE);
        // Power core (sea lanterns surrounded by glass)
        b.fillBox(-2, -7, -2, 2, -3, 2, Material.SEA_LANTERN);
        b.fillBox(-3, -7, -3, 3, -2, 3, Material.LIGHT_BLUE_STAINED_GLASS);
        b.fillBox(-2, -6, -2, 2, -3, 2, Material.SEA_LANTERN);

        // Automated defenses (dispensers on walls)
        for (int x = -36; x <= 36; x += 12) {
            b.setDirectional(x, 8, -20, Material.DISPENSER, BlockFace.SOUTH);
            b.setDirectional(x, 8, 20, Material.DISPENSER, BlockFace.NORTH);
        }

        // Ceiling
        b.fillFloor(-39, -19, 39, 19, 16, Material.GRAY_CONCRETE);

        // Entrance (south, large door)
        b.fillBox(-4, 1, -20, 4, 10, -20, Material.AIR);

        // Glass windows throughout
        for (int x = -35; x <= 35; x += 5) {
            for (int y = 6; y <= 12; y++) {
                b.setBlock(x, y, -20, Material.GRAY_STAINED_GLASS_PANE);
                b.setBlock(x, y, 20, Material.GRAY_STAINED_GLASS_PANE);
            }
        }

        b.placeChest(0, 1, 15);
        b.placeChest(-30, 1, 0);
        b.placeChest(30, 1, -10);
        b.placeChest(0, -7, 0);
        b.setBossSpawn(0, 1, 12);
    }

    /**
     * Meteor Crash Site — Impact crater, meteor rock at center, radiating cracks,
     * scattered debris, rare ores exposed.
     */
    public static void buildMeteorCrashSite(StructureBuilder b) {
        // Impact crater (depression, radius 30)
        for (int x = -30; x <= 30; x++)
            for (int z = -30; z <= 30; z++) {
                double dist = Math.sqrt(x * x + z * z);
                if (dist <= 30) {
                    int depth = (int) (8 * (1 - dist / 30));
                    for (int y = 0; y >= -depth; y--) b.setBlock(x, y, z, Material.AIR);
                    b.setBlock(x, -depth, z, Material.GRAY_CONCRETE);
                }
            }

        // Meteor rock in center (blackstone + crying obsidian)
        b.filledCircle(0, -8, 0, 6, Material.BLACKSTONE);
        b.filledCircle(0, -7, 0, 5, Material.BLACKSTONE);
        b.filledCircle(0, -6, 0, 4, Material.CRYING_OBSIDIAN);
        b.filledCircle(0, -5, 0, 3, Material.CRYING_OBSIDIAN);
        b.filledCircle(0, -4, 0, 2, Material.BLACKSTONE);
        b.setBlock(0, -3, 0, Material.CRYING_OBSIDIAN);

        // Scatter obsidian on meteor
        b.scatter(-6, -8, -6, 6, -3, 6, Material.BLACKSTONE, Material.OBSIDIAN, 0.2);

        // Radiating crack lines (magma blocks, 8 directions)
        for (int a = 0; a < 360; a += 45) {
            double rad = Math.toRadians(a);
            for (int d = 7; d <= 25; d++) {
                int cx = (int) (d * Math.cos(rad));
                int cz = (int) (d * Math.sin(rad));
                int depth = (int) (6 * (1 - (double) d / 30));
                b.setBlock(cx, -depth, cz, Material.MAGMA_BLOCK);
            }
        }

        // Scattered debris (fragments)
        for (int i = 0; i < 12; i++) {
            int dx = -20 + (i * 37 % 40);
            int dz = -15 + (i * 23 % 30);
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist > 8 && dist < 28) {
                b.setBlock(dx, 0, dz, Material.BLACKSTONE);
                b.setBlock(dx + 1, 0, dz, Material.CRYING_OBSIDIAN);
            }
        }

        // Exposed rare ores in crater walls
        b.setBlock(-15, -3, -15, Material.DIAMOND_ORE);
        b.setBlock(12, -2, -18, Material.GOLD_ORE);
        b.setBlock(-20, -2, 5, Material.EMERALD_ORE);
        b.setBlock(18, -3, 10, Material.LAPIS_ORE);
        b.setBlock(0, -6, -12, Material.ANCIENT_DEBRIS);
        b.setBlock(8, -5, 8, Material.ANCIENT_DEBRIS);

        // Small fires near impact
        b.setBlock(-3, -7, 2, Material.FIRE);
        b.setBlock(2, -7, -3, Material.FIRE);
        b.setBlock(4, -6, 4, Material.FIRE);

        b.placeChest(0, -3, 2);
        b.placeChest(-10, -2, 0);
    }

    /**
     * Observation Tower — Tall thin tower (15x40): iron/glass, multiple observation
     * floors, telescope, antenna array, ladder access, radar dish.
     */
    public static void buildObservationTower(StructureBuilder b) {
        // Base platform
        b.fillBox(-7, 0, -7, 7, 0, 7, Material.IRON_BLOCK);

        // Tower shaft (thin, 6x6)
        for (int y = 1; y <= 36; y++) {
            b.fillBox(-3, y, -3, 3, y, -3, Material.IRON_BLOCK);
            b.fillBox(-3, y, 3, 3, y, 3, Material.IRON_BLOCK);
            b.fillBox(-3, y, -3, -3, y, 3, Material.IRON_BLOCK);
            b.fillBox(3, y, -3, 3, y, 3, Material.IRON_BLOCK);
            // Interior clear
            b.fillBox(-2, y, -2, 2, y, 2, Material.AIR);
        }

        // Glass windows on each floor
        for (int y = 4; y <= 36; y += 4) {
            b.setBlock(0, y, -3, Material.GLASS_PANE);
            b.setBlock(0, y, 3, Material.GLASS_PANE);
            b.setBlock(-3, y, 0, Material.GLASS_PANE);
            b.setBlock(3, y, 0, Material.GLASS_PANE);
            b.setBlock(0, y + 1, -3, Material.GLASS_PANE);
            b.setBlock(0, y + 1, 3, Material.GLASS_PANE);
        }

        // Observation floors
        for (int fy = 8; fy <= 32; fy += 8) {
            b.fillBox(-2, fy, -2, 2, fy, 2, Material.IRON_BLOCK);
        }

        // Ladder access (inside, along one wall)
        for (int y = 1; y <= 36; y++) b.setBlock(2, y, 0, Material.LADDER);

        // Top observation deck (wider)
        b.fillBox(-5, 36, -5, 5, 36, 5, Material.IRON_BLOCK);
        b.fillBox(-5, 37, -5, 5, 40, -5, Material.IRON_BLOCK);
        b.fillBox(-5, 37, 5, 5, 40, 5, Material.IRON_BLOCK);
        b.fillBox(-5, 37, -5, -5, 40, 5, Material.IRON_BLOCK);
        b.fillBox(5, 37, -5, 5, 40, 5, Material.IRON_BLOCK);
        b.fillBox(-4, 37, -4, 4, 39, 4, Material.AIR);
        // Glass observation windows
        for (int x = -3; x <= 3; x++) {
            b.setBlock(x, 38, -5, Material.GLASS_PANE);
            b.setBlock(x, 38, 5, Material.GLASS_PANE);
            b.setBlock(x, 39, -5, Material.GLASS_PANE);
            b.setBlock(x, 39, 5, Material.GLASS_PANE);
        }

        // Telescope (end rods pointing up)
        b.setBlock(0, 37, -3, Material.END_ROD);
        b.setBlock(0, 38, -3, Material.END_ROD);
        b.setBlock(0, 39, -4, Material.END_ROD);

        // Antenna array on top
        b.pillar(0, 40, 44, 0, Material.IRON_BARS);
        b.setBlock(0, 45, 0, Material.END_ROD);
        b.setBlock(-2, 42, 0, Material.IRON_BARS);
        b.setBlock(2, 42, 0, Material.IRON_BARS);

        // Radar dish (daylight sensor + slab)
        b.setBlock(0, 43, 2, Material.DAYLIGHT_DETECTOR);
        b.setSlab(0, 43, 1, Material.IRON_BLOCK, false);

        // Entrance
        b.setBlock(0, 1, -3, Material.AIR);
        b.setBlock(0, 2, -3, Material.AIR);

        b.placeChest(0, 37, 0);
        b.placeChest(-1, 9, -1);
    }

    /**
     * Lunar Base — Abandoned base: gray/white concrete rooms, airlock, hydroponics,
     * crew quarters, control room, oxygen tanks.
     */
    public static void buildLunarBase(StructureBuilder b) {
        // Main building shell
        b.fillBox(-18, 0, -18, 18, 8, 18, Material.WHITE_CONCRETE);
        b.fillBox(-17, 1, -17, 17, 7, 17, Material.AIR);
        b.fillFloor(-17, -17, 17, 17, 0, Material.LIGHT_GRAY_CONCRETE);

        // Airlock (south entrance, double iron doors)
        b.fillBox(-3, 0, -18, 3, 6, -14, Material.GRAY_CONCRETE);
        b.fillBox(-2, 1, -17, 2, 5, -15, Material.AIR);
        b.setBlock(-1, 1, -18, Material.IRON_DOOR);
        b.setBlock(1, 1, -18, Material.IRON_DOOR);
        b.setBlock(-1, 1, -14, Material.IRON_DOOR);
        b.setBlock(1, 1, -14, Material.IRON_DOOR);

        // Corridor (central)
        b.fillBox(-2, 1, -14, 2, 5, 14, Material.AIR);
        b.fillBox(-2, 0, -14, 2, 0, 14, Material.LIGHT_GRAY_CONCRETE);

        // Hydroponics bay (NE)
        b.fillBox(4, 0, -16, 16, 7, -4, Material.WHITE_CONCRETE);
        b.fillBox(5, 1, -15, 15, 6, -5, Material.AIR);
        b.fillFloor(5, -15, 15, -5, 0, Material.LIGHT_GRAY_CONCRETE);
        // Farmland plots (dead crops)
        for (int x = 6; x <= 14; x += 2)
            for (int z = -14; z <= -6; z += 2) {
                b.setBlock(x, 0, z, Material.FARMLAND);
                b.setBlock(x, 1, z, Material.DEAD_BUSH);
            }
        // Grow lights
        b.wallLighting(5, 5, -15, 15, -15, 3, Material.SEA_LANTERN);

        // Crew quarters (NW)
        b.fillBox(-16, 0, -16, -4, 7, -4, Material.WHITE_CONCRETE);
        b.fillBox(-15, 1, -15, -5, 6, -5, Material.AIR);
        b.fillFloor(-15, -15, -5, -5, 0, Material.LIGHT_GRAY_CONCRETE);
        for (int z = -14; z <= -6; z += 4) {
            b.setBlock(-12, 1, z, Material.WHITE_BED);
            b.setBlock(-8, 1, z, Material.WHITE_BED);
        }
        b.chandelier(-10, 7, -10, 1);

        // Control room (SE)
        b.fillBox(4, 0, 4, 16, 7, 16, Material.WHITE_CONCRETE);
        b.fillBox(5, 1, 5, 15, 6, 15, Material.AIR);
        b.fillFloor(5, 5, 15, 15, 0, Material.GRAY_CONCRETE);
        // Redstone control panels
        b.fillBox(5, 1, 14, 15, 3, 14, Material.REDSTONE_LAMP);
        b.fillBox(5, 1, 13, 15, 1, 13, Material.SMOOTH_STONE_SLAB);
        b.setBlock(10, 2, 13, Material.LEVER);
        b.setBlock(8, 2, 13, Material.STONE_BUTTON);
        // Map display
        b.setBlock(10, 4, 14, Material.SEA_LANTERN);

        // Oxygen tanks (SW corner — blue glass cylinders)
        b.fillBox(-16, 0, 4, -4, 7, 16, Material.WHITE_CONCRETE);
        b.fillBox(-15, 1, 5, -5, 6, 15, Material.AIR);
        // Tanks
        for (int x = -14; x <= -6; x += 4) {
            b.pillar(x, 1, 5, 10, Material.LIGHT_BLUE_STAINED_GLASS);
            b.setBlock(x, 1, 10, Material.IRON_BLOCK);
            b.setBlock(x, 6, 10, Material.IRON_BLOCK);
        }

        // Ceiling
        b.fillFloor(-17, -17, 17, 17, 8, Material.WHITE_CONCRETE);

        // Abandoned details — scattered debris
        b.scatter(-16, 1, -16, 16, 1, 16, Material.AIR, Material.COBWEB, 0.03);

        b.placeChest(10, 1, 10);
        b.placeChest(-12, 1, -12);
        b.placeChest(10, 1, -10);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  JURASSIC STRUCTURES (7)
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Bone Arena — Massive bone block arena, ribcage-shaped arches, skull gate,
     * sand/coarse dirt floor, tiered bone seating, torches in eye sockets.
     */
    public static void buildBoneArena(StructureBuilder b) {
        // Arena floor
        b.filledCircle(0, 0, 0, 28, Material.SAND);
        b.filledCircle(0, 0, 0, 25, Material.COARSE_DIRT);

        // Outer bone wall ring
        for (int y = 1; y <= 10; y++) b.circle(0, y, 0, 28, Material.BONE_BLOCK);

        // Tiered bone seating (4 tiers)
        for (int tier = 0; tier < 4; tier++) {
            int r = 24 - tier * 2;
            b.circle(0, tier + 1, 0, r, Material.BONE_BLOCK);
        }

        // Ribcage arches (8 massive ribs arching over the arena)
        for (int a = 0; a < 360; a += 45) {
            int bx = (int) (25 * Math.cos(Math.toRadians(a)));
            int bz = (int) (25 * Math.sin(Math.toRadians(a)));
            // Rib going from base to peak
            for (int y = 5; y <= 18; y++) {
                double t = (double) (y - 5) / 13;
                int rx = (int) (bx * (1 - t * 0.7));
                int rz = (int) (bz * (1 - t * 0.7));
                b.setBlock(rx, y, rz, Material.BONE_BLOCK);
            }
        }

        // Skull gate entrance (south)
        b.fillBox(-5, 0, -28, 5, 10, -25, Material.BONE_BLOCK);
        b.fillBox(-3, 1, -28, 3, 8, -25, Material.AIR);
        // Skull decoration over gate
        b.fillBox(-4, 8, -28, 4, 13, -25, Material.BONE_BLOCK);
        b.fillBox(-3, 9, -28, 3, 12, -25, Material.AIR);
        // Eyes (torches in sockets)
        b.setBlock(-2, 11, -28, Material.TORCH);
        b.setBlock(2, 11, -28, Material.TORCH);
        // Nose hole
        b.setBlock(0, 9, -28, Material.AIR);
        // Mouth
        b.fillBox(-2, 8, -28, 2, 8, -28, Material.AIR);

        // Interior combat pillars (bone)
        for (int a = 0; a < 360; a += 60) {
            int px = (int) (12 * Math.cos(Math.toRadians(a)));
            int pz = (int) (12 * Math.sin(Math.toRadians(a)));
            b.pillar(px, 1, 6, pz, Material.BONE_BLOCK);
            b.setBlock(px, 7, pz, Material.SKELETON_SKULL);
        }

        // Torches around the arena
        b.wallLighting(0, 8, -28, 0, 28, 6, Material.TORCH);

        // Trophy skulls on the wall
        for (int a = 0; a < 360; a += 30)
            b.setBlock((int) (27 * Math.cos(Math.toRadians(a))), 6,
                    (int) (27 * Math.sin(Math.toRadians(a))), Material.SKELETON_SKULL);

        b.placeChest(0, 1, 0);
        b.placeChest(10, 1, 10);
        b.placeChest(-10, 1, -10);
        b.setBossSpawn(0, 1, 5);
    }

    /**
     * Dinobot Arena — Mechanical arena: iron/redstone blocks, lightning rod pylons,
     * electrified floor, observation deck, launch pad.
     */
    public static void buildDinobotArena(StructureBuilder b) {
        // Arena floor
        b.filledCircle(0, 0, 0, 24, Material.IRON_BLOCK);
        b.filledCircle(0, 0, 0, 22, Material.SMOOTH_STONE);

        // Electrified floor sections (redstone lamps in patterns)
        b.circle(0, 0, 0, 8, Material.REDSTONE_LAMP);
        b.circle(0, 0, 0, 16, Material.REDSTONE_LAMP);

        // Outer wall
        for (int y = 1; y <= 8; y++) b.circle(0, y, 0, 24, Material.IRON_BLOCK);

        // Lightning rod pylons (6 around perimeter)
        for (int a = 0; a < 360; a += 60) {
            int px = (int) (20 * Math.cos(Math.toRadians(a)));
            int pz = (int) (20 * Math.sin(Math.toRadians(a)));
            b.pillar(px, 1, 14, pz, Material.IRON_BLOCK);
            b.setBlock(px, 15, pz, Material.LIGHTNING_ROD);
            b.setBlock(px, 14, pz, Material.REDSTONE_BLOCK);
            // Connecting redstone lines
            b.setBlock(px, 1, pz, Material.REDSTONE_BLOCK);
        }

        // Central arena platform
        b.filledCircle(0, 0, 0, 4, Material.REDSTONE_BLOCK);
        b.filledCircle(0, 1, 0, 2, Material.IRON_BLOCK);

        // Observation deck (elevated, south)
        b.fillBox(-10, 10, -24, 10, 10, -20, Material.IRON_BLOCK);
        b.fillBox(-10, 11, -24, 10, 13, -24, Material.IRON_BLOCK);
        b.fillBox(-8, 11, -24, 8, 12, -24, Material.GLASS_PANE);
        // Stairs up to deck
        for (int s = 0; s < 10; s++) b.setBlock(0, s + 1, -24 + s, Material.IRON_BLOCK);

        // Launch pad (north, open platform)
        b.fillBox(-8, 0, 20, 8, 0, 28, Material.IRON_BLOCK);
        b.fillBox(-6, 0, 22, 6, 0, 26, Material.YELLOW_CONCRETE);
        // Warning stripes
        b.fillBox(-8, 0, 20, -8, 0, 28, Material.BLACK_CONCRETE);
        b.fillBox(8, 0, 20, 8, 0, 28, Material.BLACK_CONCRETE);

        // Redstone circuitry patterns on floor
        b.scatter(-20, 0, -20, 20, 0, 20, Material.SMOOTH_STONE, Material.REDSTONE_BLOCK, 0.05);

        // Interior combat obstacles
        for (int[] pos : new int[][]{{-10, -10}, {10, -10}, {-10, 10}, {10, 10}}) {
            b.fillBox(pos[0] - 2, 1, pos[1] - 2, pos[0] + 2, 4, pos[1] + 2, Material.IRON_BLOCK);
            b.setBlock(pos[0], 5, pos[1], Material.REDSTONE_LAMP);
        }

        b.placeChest(0, 1, 0);
        b.placeChest(-8, 11, -22);
        b.setBossSpawn(0, 2, 3);
    }

    /**
     * Raptor Nest — Large stick nest, egg cluster, feather carpets,
     * scratch marks, small cave entrance.
     */
    public static void buildRaptorNest(StructureBuilder b) {
        // Nest ring (dark oak fences as sticks)
        b.filledCircle(0, 0, 0, 7, Material.COARSE_DIRT);
        b.circle(0, 1, 0, 7, Material.DARK_OAK_FENCE);
        b.circle(0, 1, 0, 6, Material.DARK_OAK_FENCE);
        b.circle(0, 2, 0, 7, Material.DARK_OAK_FENCE);

        // Nest interior (soft)
        b.filledCircle(0, 0, 0, 5, Material.HAY_BLOCK);
        b.filledCircle(0, 1, 0, 3, Material.HAY_BLOCK);

        // Egg cluster
        b.setBlock(0, 2, 0, Material.TURTLE_EGG);
        b.setBlock(-1, 2, 0, Material.TURTLE_EGG);
        b.setBlock(0, 2, -1, Material.TURTLE_EGG);
        b.setBlock(1, 2, 1, Material.TURTLE_EGG);

        // Feather carpets (white/light gray scattered)
        for (int x = -4; x <= 4; x += 2)
            for (int z = -4; z <= 4; z += 2) {
                double dist = Math.sqrt(x * x + z * z);
                if (dist < 5 && dist > 2)
                    b.setRandomBlock(x, 1, z, Material.WHITE_CARPET, Material.LIGHT_GRAY_CARPET);
            }

        // Scratch marks on surrounding terrain (coarse dirt paths)
        for (int a = 0; a < 360; a += 45) {
            for (int d = 7; d <= 10; d++) {
                int sx = (int) (d * Math.cos(Math.toRadians(a)));
                int sz = (int) (d * Math.sin(Math.toRadians(a)));
                b.setBlock(sx, 0, sz, Material.COARSE_DIRT);
            }
        }

        // Small cave entrance (south)
        b.fillBox(-2, 0, -10, 2, 3, -7, Material.STONE);
        b.fillBox(-1, 1, -10, 1, 2, -8, Material.AIR);

        // Scattered bones nearby
        b.setBlock(5, 1, 3, Material.BONE_BLOCK);
        b.setBlock(-4, 1, -5, Material.BONE_BLOCK);
        b.setBlock(6, 1, -2, Material.SKELETON_SKULL);

        b.placeChest(0, 2, 2);
    }

    /**
     * Watering Hole — Natural pond with muddy banks, palm trees,
     * animal trails, drinking area, peaceful meadow.
     */
    public static void buildWateringHole(StructureBuilder b) {
        // Meadow base
        b.filledCircle(0, 0, 0, 14, Material.GRASS_BLOCK);

        // Muddy banks (mud blocks ring)
        b.filledCircle(0, 0, 0, 8, Material.MUD);

        // Water pond
        b.filledCircle(0, 0, 0, 5, Material.WATER);
        b.filledCircle(0, -1, 0, 4, Material.CLAY);

        // Palm trees (jungle wood + leaves)
        int[][] treePos = {{-10, -8}, {10, -8}, {-8, 10}, {8, 10}};
        for (int[] tp : treePos) {
            b.pillar(tp[0], 1, 8, tp[1], Material.JUNGLE_LOG);
            for (int dx = -2; dx <= 2; dx++)
                for (int dz = -2; dz <= 2; dz++)
                    if (Math.abs(dx) + Math.abs(dz) <= 3)
                        b.setBlockIfAir(tp[0] + dx, 9, tp[1] + dz, Material.JUNGLE_LEAVES);
        }

        // Animal trails (coarse dirt paths)
        for (int i = -14; i <= -6; i++) b.setBlock(i, 0, 0, Material.COARSE_DIRT);
        for (int i = 6; i <= 14; i++) b.setBlock(i, 0, 0, Material.COARSE_DIRT);
        for (int i = -14; i <= -6; i++) b.setBlock(0, 0, i, Material.COARSE_DIRT);
        for (int i = 6; i <= 14; i++) b.setBlock(0, 0, i, Material.COARSE_DIRT);

        // Drinking area (flat stones near water)
        b.setBlock(-6, 0, -1, Material.SMOOTH_STONE_SLAB);
        b.setBlock(-6, 0, 0, Material.SMOOTH_STONE_SLAB);
        b.setBlock(-6, 0, 1, Material.SMOOTH_STONE_SLAB);

        // Flowers and grass for meadow
        for (int x = -13; x <= 13; x += 2)
            for (int z = -13; z <= 13; z += 2) {
                double dist = Math.sqrt(x * x + z * z);
                if (dist > 9 && dist < 14)
                    b.setRandomBlock(x, 1, z, Material.GRASS_BLOCK, Material.DANDELION,
                            Material.POPPY, Material.FERN);
            }

        // Lily pads on water
        b.setBlock(-2, 1, 0, Material.LILY_PAD);
        b.setBlock(1, 1, 2, Material.LILY_PAD);
        b.setBlock(0, 1, -3, Material.LILY_PAD);

        b.placeChest(8, 1, 8);
    }

    /**
     * Volcanic Forge — Active volcanic forge: lava channels, basalt/deepslate,
     * multiple anvils, blast furnace, coal storage, chimney.
     */
    public static void buildVolcanicForge(StructureBuilder b) {
        // Building shell
        b.fillBox(-12, 0, -12, 12, 0, 12, Material.DEEPSLATE_BRICKS);
        b.fillWalls(-12, 1, -12, 12, 10, 12, Material.BASALT);
        b.hollowBox(-12, 1, -12, 12, 10, 12);
        b.fillFloor(-11, -11, 11, 11, 0, Material.DEEPSLATE);

        // Lava channels (cross pattern feeding into forge)
        b.fillBox(-11, 0, -1, -4, 0, 1, Material.LAVA);
        b.fillBox(4, 0, -1, 11, 0, 1, Material.LAVA);
        b.fillBox(-1, 0, -11, 1, 0, -4, Material.LAVA);

        // Central forge pit
        b.fillBox(-3, 0, -3, 3, 0, 3, Material.LAVA);
        b.circle(0, 0, 0, 4, Material.DEEPSLATE_BRICKS);

        // Smithing area (south)
        b.fillBox(-8, 0, 4, 8, 0, 10, Material.POLISHED_BASALT);
        b.setBlock(-4, 1, 8, Material.ANVIL);
        b.setBlock(-2, 1, 8, Material.ANVIL);
        b.setBlock(0, 1, 8, Material.SMITHING_TABLE);
        b.setBlock(2, 1, 8, Material.BLAST_FURNACE);
        b.setBlock(4, 1, 8, Material.FURNACE);

        // Coal storage (east)
        b.fillBox(6, 0, -8, 10, 4, -4, Material.DEEPSLATE_BRICKS);
        b.fillBox(7, 1, -7, 9, 3, -5, Material.COAL_BLOCK);

        // Chimney (northeast corner, going through roof)
        b.fillBox(8, 0, -12, 12, 14, -8, Material.BASALT);
        b.fillBox(9, 1, -11, 11, 14, -9, Material.AIR);
        // Smoke effect (cobwebs)
        b.setBlock(10, 13, -10, Material.COBWEB);
        b.setBlock(10, 14, -10, Material.COBWEB);

        // Basalt decorative pillars
        for (int[] p : new int[][]{{-10, -10}, {10, -10}, {-10, 10}}) {
            b.pillar(p[0], 1, 9, p[1], Material.POLISHED_BASALT);
            b.setBlock(p[0], 9, p[1], Material.LANTERN);
        }

        // Magma block accents
        b.scatter(-11, 0, -11, 11, 0, 11, Material.DEEPSLATE, Material.MAGMA_BLOCK, 0.08);

        // Water quench trough
        b.fillBox(-8, 0, 6, -5, 1, 7, Material.DEEPSLATE_BRICKS);
        b.fillBox(-7, 1, 6, -6, 1, 7, Material.WATER);

        // Entrance
        b.fillBox(-2, 1, -12, 2, 5, -12, Material.AIR);

        // Roof with gap for chimney
        b.fillFloor(-11, -11, 7, 11, 10, Material.BASALT);

        // Lighting
        b.chandelier(0, 10, 7, 2);

        b.placeChest(-6, 1, 5);
        b.placeChest(6, 1, 5);
        b.placeChest(8, 1, -6);
        b.setBossSpawn(0, 1, 5);
    }

    /**
     * Tar Pit — Depression filled with soul soil, trapped fossil, warning signs,
     * fencing, excavation equipment (scaffolding).
     */
    public static void buildTarPit(StructureBuilder b) {
        // Surrounding terrain
        b.filledCircle(0, 0, 0, 10, Material.COARSE_DIRT);

        // Tar pit depression
        for (int x = -7; x <= 7; x++)
            for (int z = -7; z <= 7; z++) {
                double dist = Math.sqrt(x * x + z * z);
                if (dist <= 7) {
                    int depth = (int) (3 * (1 - dist / 7));
                    for (int y = 0; y >= -depth; y--) b.setBlock(x, y, z, Material.SOUL_SOIL);
                    b.setBlock(x, -depth - 1, z, Material.SOUL_SAND);
                }
            }

        // Surface layer (soul sand on top of soul soil for "tar" look)
        b.filledCircle(0, 0, 0, 6, Material.SOUL_SAND);

        // Trapped fossil (bone blocks partially buried)
        b.setBlock(-2, 0, -1, Material.BONE_BLOCK);
        b.setBlock(-1, 0, -1, Material.BONE_BLOCK);
        b.setBlock(0, 0, 0, Material.BONE_BLOCK);
        b.setBlock(1, 0, 0, Material.BONE_BLOCK);
        b.setBlock(2, 0, 1, Material.BONE_BLOCK);
        b.setBlock(3, 0, 1, Material.BONE_BLOCK);
        // Ribcage sticking out
        b.setBlock(-1, 1, -1, Material.BONE_BLOCK);
        b.setBlock(0, 1, 0, Material.BONE_BLOCK);
        b.setBlock(1, 1, 0, Material.BONE_BLOCK);
        // Skull
        b.setBlock(-3, 1, -2, Material.SKELETON_SKULL);

        // Fencing around the pit
        b.circle(0, 1, 0, 9, Material.OAK_FENCE);

        // Warning signs (banners)
        b.setBlock(0, 2, -9, Material.ORANGE_BANNER);
        b.setBlock(0, 2, 9, Material.ORANGE_BANNER);
        b.setBlock(-9, 2, 0, Material.ORANGE_BANNER);
        b.setBlock(9, 2, 0, Material.ORANGE_BANNER);

        // Excavation equipment (scaffolding + tools)
        b.pillar(7, 1, 5, 7, Material.SCAFFOLDING);
        b.pillar(8, 1, 4, 7, Material.SCAFFOLDING);
        b.setBlock(6, 1, 6, Material.BARREL);
        b.setBlock(5, 1, 7, Material.CRAFTING_TABLE);

        // Research tent (small canopy)
        b.fillBox(-9, 0, -10, -6, 0, -7, Material.COARSE_DIRT);
        b.pillar(-9, 1, 3, -10, Material.OAK_FENCE);
        b.pillar(-6, 1, 3, -10, Material.OAK_FENCE);
        b.pillar(-9, 1, 3, -7, Material.OAK_FENCE);
        b.pillar(-6, 1, 3, -7, Material.OAK_FENCE);
        b.fillBox(-9, 3, -10, -6, 3, -7, Material.WHITE_WOOL);

        b.placeChest(-8, 1, -9);
        b.placeChest(6, 1, 7);
    }

    /**
     * Nesting Ground — Large flat area with multiple nests, egg clusters,
     * protective walls, feeding troughs.
     */
    public static void buildNestingGround(StructureBuilder b) {
        // Ground base
        b.fillBox(-18, 0, -18, 18, 0, 18, Material.COARSE_DIRT);
        b.scatter(-18, 0, -18, 18, 0, 18, Material.COARSE_DIRT, Material.GRASS_BLOCK, 0.3);

        // Protective bone walls (perimeter)
        for (int x = -18; x <= 18; x++) {
            b.setBlock(x, 1, -18, Material.BONE_BLOCK);
            b.setBlock(x, 2, -18, Material.BONE_BLOCK);
            b.setBlock(x, 1, 18, Material.BONE_BLOCK);
            b.setBlock(x, 2, 18, Material.BONE_BLOCK);
        }
        for (int z = -18; z <= 18; z++) {
            b.setBlock(-18, 1, z, Material.BONE_BLOCK);
            b.setBlock(-18, 2, z, Material.BONE_BLOCK);
            b.setBlock(18, 1, z, Material.BONE_BLOCK);
            b.setBlock(18, 2, z, Material.BONE_BLOCK);
        }
        // Fence on top of walls
        for (int x = -18; x <= 18; x++) {
            b.setBlock(x, 3, -18, Material.OAK_FENCE);
            b.setBlock(x, 3, 18, Material.OAK_FENCE);
        }
        for (int z = -18; z <= 18; z++) {
            b.setBlock(-18, 3, z, Material.OAK_FENCE);
            b.setBlock(18, 3, z, Material.OAK_FENCE);
        }

        // Multiple nests (6, scattered)
        int[][] nestPos = {{-10, -10}, {10, -10}, {-10, 10}, {10, 10}, {0, -5}, {0, 8}};
        for (int[] np : nestPos) {
            // Nest ring
            b.filledCircle(np[0], 0, np[1], 3, Material.HAY_BLOCK);
            b.circle(np[0], 1, np[1], 3, Material.DARK_OAK_FENCE);
            // Egg clusters
            b.setBlock(np[0], 1, np[1], Material.TURTLE_EGG);
            b.setBlock(np[0] - 1, 1, np[1], Material.TURTLE_EGG);
            b.setBlock(np[0] + 1, 1, np[1], Material.TURTLE_EGG);
        }

        // Feeding troughs (water-filled cauldrons along paths)
        for (int x = -12; x <= 12; x += 6) {
            b.setBlock(x, 1, 0, Material.CAULDRON);
        }

        // Patrol path (coarse dirt ring inside walls)
        b.circle(0, 0, 0, 15, Material.COARSE_DIRT);

        // Gate entrance (south)
        b.setBlock(0, 1, -18, Material.AIR);
        b.setBlock(0, 2, -18, Material.AIR);
        b.setBlock(1, 1, -18, Material.AIR);
        b.setBlock(1, 2, -18, Material.AIR);

        // Scattered feathers (carpets)
        b.scatter(-16, 0, -16, 16, 0, 16, Material.COARSE_DIRT, Material.WHITE_CARPET, 0.03);

        b.placeChest(0, 1, -14);
        b.placeChest(-14, 1, 0);
        b.setBossSpawn(0, 1, 5);
    }
}

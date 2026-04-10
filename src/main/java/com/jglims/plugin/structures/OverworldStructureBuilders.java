package com.jglims.plugin.structures;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;

/**
 * Static utility class containing all Overworld structure building methods.
 * Each method constructs a fully detailed structure using the {@link StructureBuilder} API.
 */
public final class OverworldStructureBuilders {

    private OverworldStructureBuilders() {}

    // ═══════════════════════════════════════════════════════════════
    //  1. CAMPING SMALL
    // ═══════════════════════════════════════════════════════════════

    public static void buildCampingSmall(StructureBuilder b) {
        // Ground pad
        b.filledCircle(0, 0, 0, 5, Material.GRASS_BLOCK);
        b.scatter(-5, 0, -5, 5, 0, 5, Material.GRASS_BLOCK, Material.COARSE_DIRT, 0.3);

        // Central campfire with ring of cobblestone
        b.circle(0, 0, 0, 1, Material.COBBLESTONE);
        b.setBlock(0, 1, 0, Material.CAMPFIRE);

        // Tent 1 (NW) — white wool on fence frame
        b.setBlock(-3, 1, -3, Material.OAK_FENCE);
        b.setBlock(-3, 1, -1, Material.OAK_FENCE);
        b.setBlock(-1, 1, -3, Material.OAK_FENCE);
        b.setBlock(-1, 1, -1, Material.OAK_FENCE);
        b.setBlock(-3, 2, -3, Material.OAK_FENCE);
        b.setBlock(-1, 2, -1, Material.OAK_FENCE);
        b.fillBox(-3, 2, -3, -1, 2, -1, Material.WHITE_WOOL);
        b.setBlock(-2, 3, -2, Material.WHITE_WOOL);
        b.setBlock(-2, 1, -2, Material.BROWN_CARPET);

        // Tent 2 (SE) — red wool
        b.setBlock(1, 1, 1, Material.OAK_FENCE);
        b.setBlock(1, 1, 3, Material.OAK_FENCE);
        b.setBlock(3, 1, 1, Material.OAK_FENCE);
        b.setBlock(3, 1, 3, Material.OAK_FENCE);
        b.setBlock(1, 2, 1, Material.OAK_FENCE);
        b.setBlock(3, 2, 3, Material.OAK_FENCE);
        b.fillBox(1, 2, 1, 3, 2, 3, Material.RED_WOOL);
        b.setBlock(2, 3, 2, Material.RED_WOOL);
        b.setBlock(2, 1, 2, Material.GREEN_CARPET);

        // Log seats around fire
        b.setBlock(2, 1, -1, Material.STRIPPED_OAK_LOG);
        b.setBlock(-2, 1, 1, Material.STRIPPED_OAK_LOG);
        b.setBlock(0, 1, 2, Material.STRIPPED_OAK_LOG);

        // Supply chest
        b.placeChest(4, 1, 0);

        // Lantern on a fence
        b.setBlock(-4, 1, 2, Material.OAK_FENCE);
        b.setBlock(-4, 2, 2, Material.LANTERN);

        // Flower pots for ambiance
        b.setBlock(4, 1, -3, Material.POTTED_POPPY);
        b.setBlock(-4, 1, -2, Material.POTTED_DANDELION);
    }

    // ═══════════════════════════════════════════════════════════════
    //  2. CAMPING LARGE
    // ═══════════════════════════════════════════════════════════════

    public static void buildCampingLarge(StructureBuilder b) {
        // Ground pad
        b.filledCircle(0, 0, 0, 10, Material.GRASS_BLOCK);
        b.scatter(-10, 0, -10, 10, 0, 10, Material.GRASS_BLOCK, Material.COARSE_DIRT, 0.2);

        // Palisade wall (stripped logs)
        b.circle(0, 0, 0, 9, Material.STRIPPED_OAK_LOG);
        for (int a = 0; a < 360; a += 8) {
            int wx = (int) Math.round(9 * Math.cos(Math.toRadians(a)));
            int wz = (int) Math.round(9 * Math.sin(Math.toRadians(a)));
            b.pillar(wx, 1, 3, wz, Material.STRIPPED_OAK_LOG);
        }
        // Gate opening
        b.setBlock(0, 1, -9, Material.AIR); b.setBlock(0, 2, -9, Material.AIR); b.setBlock(0, 3, -9, Material.AIR);
        b.setBlock(1, 1, -9, Material.AIR); b.setBlock(1, 2, -9, Material.AIR); b.setBlock(1, 3, -9, Material.AIR);
        b.setBlock(-1, 4, -9, Material.STRIPPED_OAK_LOG); b.setBlock(0, 4, -9, Material.STRIPPED_OAK_LOG);
        b.setBlock(1, 4, -9, Material.STRIPPED_OAK_LOG); b.setBlock(2, 4, -9, Material.STRIPPED_OAK_LOG);

        // Central firepit
        b.circle(0, 0, 0, 2, Material.COBBLESTONE);
        b.setBlock(0, 1, 0, Material.CAMPFIRE);
        b.setBlock(1, 1, 0, Material.CAMPFIRE);
        b.setBlock(0, 1, 1, Material.CAMPFIRE);

        // 4 tents around the firepit
        int[][] tentPositions = {{-5, -5}, {5, -5}, {-5, 5}, {5, 5}};
        Material[] tentColors = {Material.WHITE_WOOL, Material.RED_WOOL, Material.BLUE_WOOL, Material.GREEN_WOOL};
        for (int i = 0; i < 4; i++) {
            int tx = tentPositions[i][0]; int tz = tentPositions[i][1];
            b.setBlock(tx - 1, 1, tz - 1, Material.OAK_FENCE);
            b.setBlock(tx + 1, 1, tz - 1, Material.OAK_FENCE);
            b.setBlock(tx - 1, 1, tz + 1, Material.OAK_FENCE);
            b.setBlock(tx + 1, 1, tz + 1, Material.OAK_FENCE);
            b.fillBox(tx - 1, 2, tz - 1, tx + 1, 2, tz + 1, tentColors[i]);
            b.setBlock(tx, 3, tz, tentColors[i]);
            b.setBlock(tx, 1, tz, Material.BROWN_CARPET);
        }

        // Lookout tower (NE corner)
        b.fillBox(6, 0, -8, 8, 0, -6, Material.COBBLESTONE);
        b.pillar(6, 1, 7, -8, Material.STRIPPED_OAK_LOG);
        b.pillar(8, 1, 7, -8, Material.STRIPPED_OAK_LOG);
        b.pillar(6, 1, 7, -6, Material.STRIPPED_OAK_LOG);
        b.pillar(8, 1, 7, -6, Material.STRIPPED_OAK_LOG);
        b.fillBox(5, 7, -9, 9, 7, -5, Material.OAK_PLANKS);
        b.circle(7, 8, -7, 2, Material.OAK_FENCE);
        for (int y = 1; y <= 7; y++) b.setBlock(7, y, -6, Material.LADDER);
        b.setBlock(7, 8, -7, Material.LANTERN);

        // Weapon rack area
        b.setBlock(-7, 1, 0, Material.LIGHTNING_ROD);
        b.setBlock(-7, 1, 1, Material.GRINDSTONE);
        b.setBlock(-7, 1, -1, Material.SMITHING_TABLE);

        // Chests
        b.placeChest(3, 1, -3);
        b.placeChest(-3, 1, 3);
        b.setBossSpawn(0, 1, 0);
    }

    // ═══════════════════════════════════════════════════════════════
    //  3. ABANDONED HOUSE
    // ═══════════════════════════════════════════════════════════════

    public static void buildAbandonedHouse(StructureBuilder b) {
        // Foundation
        b.fillBox(-6, 0, -6, 6, 0, 6, Material.COBBLESTONE);
        b.scatter(-6, 0, -6, 6, 0, 6, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE, 0.4);

        // First floor walls
        b.fillWalls(-6, 1, -6, 6, 5, 6, Material.OAK_PLANKS);
        b.hollowBox(-6, 1, -6, 6, 5, 6);

        // Mossy and rotting wood patches
        b.scatter(-6, 1, -6, 6, 5, 6, Material.OAK_PLANKS, Material.MOSSY_COBBLESTONE, 0.15);
        b.scatter(-6, 1, -6, 6, 5, 6, Material.OAK_PLANKS, Material.SPRUCE_PLANKS, 0.1);

        // Second floor
        b.fillBox(-5, 5, -5, 5, 5, 5, Material.OAK_PLANKS);
        b.fillWalls(-5, 6, -5, 5, 9, 5, Material.OAK_PLANKS);
        b.hollowBox(-5, 6, -5, 5, 9, 5);

        // Gabled roof
        b.gabledRoof(-6, -6, 6, 6, 10, Material.SPRUCE_STAIRS, Material.SPRUCE_SLAB);

        // Collapsed roof section (NE corner)
        b.decay(2, 10, -6, 6, 13, 0, 0.7);
        b.decay(3, 6, -5, 5, 9, -1, 0.4);

        // Broken windows (air gaps with some glass pane remnants)
        b.setBlock(-6, 3, 0, Material.AIR); b.setBlock(-6, 4, 0, Material.AIR);
        b.setBlock(6, 3, -3, Material.AIR); b.setBlock(6, 4, -3, Material.GLASS_PANE);
        b.setBlock(0, 3, -6, Material.GLASS_PANE); b.setBlock(0, 4, -6, Material.AIR);
        b.setBlock(0, 7, 6, Material.AIR); b.setBlock(0, 8, 6, Material.GLASS_PANE);
        b.setBlock(-5, 7, 0, Material.AIR);

        // Door (broken — just open)
        b.setBlock(0, 1, -6, Material.AIR); b.setBlock(0, 2, -6, Material.AIR);

        // Interior first floor: rotting furniture
        b.setBlock(-4, 1, -4, Material.CRAFTING_TABLE);
        b.setBlock(-3, 1, -4, Material.OAK_FENCE); b.setBlock(-3, 2, -4, Material.OAK_PRESSURE_PLATE);
        b.chair(-2, 1, -4, Material.OAK_STAIRS, BlockFace.NORTH);
        b.setBlock(4, 1, -4, Material.FURNACE);
        b.setBlock(3, 1, 4, Material.CAULDRON);

        // Cobwebs
        b.scatter(-5, 1, -5, 5, 5, 5, Material.AIR, Material.COBWEB, 0.08);
        b.scatter(-4, 6, -4, 4, 9, 4, Material.AIR, Material.COBWEB, 0.06);

        // Ladder to second floor
        for (int y = 1; y <= 5; y++) b.setBlock(4, y, 4, Material.LADDER);

        // Second floor: broken bed
        b.setBlock(-3, 6, 3, Material.RED_BED);
        b.setBlock(-2, 6, 2, Material.BOOKSHELF);
        b.setBlock(-2, 7, 2, Material.BOOKSHELF);

        // Hidden basement: dig down from trapdoor
        b.setBlock(0, 0, 0, Material.OAK_TRAPDOOR);
        b.fillBox(-3, -3, -3, 3, -1, 3, Material.COBBLESTONE);
        b.fillBox(-2, -2, -2, 2, -1, 2, Material.AIR);
        b.fillFloor(-2, -2, 2, 2, -3, Material.MOSSY_COBBLESTONE);
        b.setBlock(-2, -2, -2, Material.SOUL_LANTERN);
        b.placeChest(0, -2, 0);

        // Vines on exterior
        b.addVines(-7, 1, -7, 7, 10, 7, 0.15);

        b.setBossSpawn(0, 1, 2);
    }

    // ═══════════════════════════════════════════════════════════════
    //  4. WITCH HOUSE FOREST
    // ═══════════════════════════════════════════════════════════════

    public static void buildWitchHouseForest(StructureBuilder b) {
        // Stilts (dark oak logs)
        b.pillar(-4, -2, 0, -4, Material.DARK_OAK_LOG);
        b.pillar(4, -2, 0, -4, Material.DARK_OAK_LOG);
        b.pillar(-4, -2, 0, 4, Material.DARK_OAK_LOG);
        b.pillar(4, -2, 0, 4, Material.DARK_OAK_LOG);

        // Platform and floor
        b.fillBox(-5, 0, -5, 5, 0, 5, Material.SPRUCE_PLANKS);

        // Walls (spruce planks with dark oak trim)
        b.fillWalls(-5, 1, -5, 5, 5, 5, Material.SPRUCE_PLANKS);
        b.hollowBox(-5, 1, -5, 5, 5, 5);
        // Corner trim
        b.pillar(-5, 1, 5, -5, Material.DARK_OAK_LOG);
        b.pillar(5, 1, 5, -5, Material.DARK_OAK_LOG);
        b.pillar(-5, 1, 5, 5, Material.DARK_OAK_LOG);
        b.pillar(5, 1, 5, 5, Material.DARK_OAK_LOG);

        // Pyramid roof with spruce stairs
        b.pyramidRoof(-6, -6, 6, 6, 6, Material.SPRUCE_PLANKS);

        // Purple stained glass windows
        b.setBlock(-5, 3, 0, Material.PURPLE_STAINED_GLASS);
        b.setBlock(5, 3, 0, Material.PURPLE_STAINED_GLASS);
        b.setBlock(0, 3, 5, Material.PURPLE_STAINED_GLASS);
        b.setBlock(-5, 4, -2, Material.PURPLE_STAINED_GLASS);
        b.setBlock(5, 4, 2, Material.PURPLE_STAINED_GLASS);

        // Door
        b.setBlock(0, 1, -5, Material.AIR); b.setBlock(0, 2, -5, Material.AIR);

        // Interior: cauldron, brewing stands, witch supplies
        b.setBlock(-3, 1, -3, Material.CAULDRON);
        b.setBlock(-3, 1, 3, Material.BREWING_STAND);
        b.setBlock(3, 1, 3, Material.BREWING_STAND);
        b.setBlock(3, 1, -3, Material.ENCHANTING_TABLE);

        // Potion shelves
        b.bookshelfWall(-4, 1, 4, -2, 3);

        // Mushroom garden below the house
        for (int x = -3; x <= 3; x += 2) {
            b.setBlock(x, -1, -6, b.getRandom().nextBoolean() ? Material.RED_MUSHROOM : Material.BROWN_MUSHROOM);
        }
        b.setBlock(-5, -1, 0, Material.RED_MUSHROOM);
        b.setBlock(5, -1, 0, Material.BROWN_MUSHROOM);

        // Hanging lanterns under eaves
        b.setBlock(-5, 0, -5, Material.IRON_CHAIN); b.setBlock(-5, -1, -5, Material.LANTERN);
        b.setBlock(5, 0, -5, Material.IRON_CHAIN); b.setBlock(5, -1, -5, Material.LANTERN);
        b.setBlock(5, 0, 5, Material.IRON_CHAIN); b.setBlock(5, -1, 5, Material.LANTERN);

        // Jack o'lantern for eerie light
        b.setBlock(0, 1, 3, Material.JACK_O_LANTERN);

        // Cobwebs in corners
        b.setBlock(-4, 4, -4, Material.COBWEB);
        b.setBlock(4, 4, 4, Material.COBWEB);

        // Flower pots with dead bushes
        b.setBlock(2, 1, -4, Material.POTTED_DEAD_BUSH);
        b.setBlock(-2, 1, 4, Material.POTTED_DEAD_BUSH);

        b.placeChest(2, 1, -2);
        b.setBossSpawn(0, 1, 0);
    }

    // ═══════════════════════════════════════════════════════════════
    //  5. WITCH HOUSE SWAMP
    // ═══════════════════════════════════════════════════════════════

    public static void buildWitchHouseSwamp(StructureBuilder b) {
        // Stilts over water (mangrove roots)
        for (int[] p : new int[][]{{-5, -5}, {5, -5}, {-5, 5}, {5, 5}, {0, -5}, {0, 5}}) {
            b.pillar(p[0], -4, 0, p[1], Material.MANGROVE_LOG);
        }

        // Lily pad path leading to the house
        for (int z = -10; z <= -6; z++) {
            b.setBlock(0, -3, z, Material.LILY_PAD);
            if (z % 2 == 0) b.setBlock(1, -3, z, Material.LILY_PAD);
        }

        // Floor
        b.fillBox(-5, 0, -5, 5, 0, 5, Material.MANGROVE_PLANKS);

        // Walls with mangrove planks
        b.fillWalls(-5, 1, -5, 5, 5, 5, Material.MANGROVE_PLANKS);
        b.hollowBox(-5, 1, -5, 5, 5, 5);
        // Mangrove log trim
        b.pillar(-5, 1, 5, -5, Material.MANGROVE_LOG);
        b.pillar(5, 1, 5, -5, Material.MANGROVE_LOG);
        b.pillar(-5, 1, 5, 5, Material.MANGROVE_LOG);
        b.pillar(5, 1, 5, 5, Material.MANGROVE_LOG);

        // Sloped roof
        b.pyramidRoof(-6, -6, 6, 6, 6, Material.MANGROVE_PLANKS);
        b.setBlock(0, 10, 0, Material.MANGROVE_LOG);

        // Windows
        b.setBlock(-5, 3, 0, Material.PURPLE_STAINED_GLASS_PANE);
        b.setBlock(5, 3, 0, Material.PURPLE_STAINED_GLASS_PANE);
        b.setBlock(0, 3, 5, Material.PURPLE_STAINED_GLASS_PANE);

        // Door
        b.setBlock(0, 1, -5, Material.AIR); b.setBlock(0, 2, -5, Material.AIR);

        // Interior: bubbling cauldron
        b.setBlock(0, 1, 0, Material.CAULDRON);
        b.setBlock(0, 0, 0, Material.CAMPFIRE);  // fire under cauldron

        // Potion shelves
        b.setBlock(-4, 1, 4, Material.BOOKSHELF);
        b.setBlock(-3, 1, 4, Material.BOOKSHELF);
        b.setBlock(-4, 2, 4, Material.BOOKSHELF);
        b.setBlock(-3, 2, 4, Material.BOOKSHELF);

        // Brewing stands
        b.setBlock(3, 1, 3, Material.BREWING_STAND);
        b.setBlock(4, 1, 3, Material.BREWING_STAND);

        // Moss carpet floor accents
        b.setBlock(-2, 1, -2, Material.MOSS_CARPET);
        b.setBlock(2, 1, 2, Material.MOSS_CARPET);
        b.setBlock(-3, 1, 1, Material.MOSS_CARPET);

        // Hanging plants
        b.setBlock(-4, 4, -4, Material.COBWEB);
        b.setBlock(4, 4, 4, Material.COBWEB);

        // Lantern lighting
        b.setBlock(-3, 4, 0, Material.LANTERN);
        b.setBlock(3, 4, 0, Material.LANTERN);

        // Potted mushrooms
        b.setBlock(4, 1, -3, Material.POTTED_RED_MUSHROOM);
        b.setBlock(-4, 1, -3, Material.POTTED_BROWN_MUSHROOM);

        // Exterior vines
        b.addVines(-6, -2, -6, 6, 6, 6, 0.2);

        b.placeChest(-3, 1, -3);
        b.setBossSpawn(0, 1, 2);
    }

    // ═══════════════════════════════════════════════════════════════
    //  6. HOUSE TREE
    // ═══════════════════════════════════════════════════════════════

    public static void buildHouseTree(StructureBuilder b) {
        // Giant trunk (5-block wide base, tapers)
        for (int y = 0; y <= 20; y++) {
            int r = y < 5 ? 3 : (y < 12 ? 2 : 1);
            b.filledCircle(0, y, 0, r, Material.OAK_LOG);
        }

        // Massive canopy
        for (int y = 14; y <= 22; y++) {
            int r = y < 17 ? 7 : (y < 20 ? 5 : (y < 22 ? 3 : 1));
            b.filledCircle(0, y, 0, r, Material.OAK_LEAVES);
        }
        // Extra branch canopies
        b.filledCircle(5, 13, 3, 3, Material.OAK_LEAVES);
        b.filledCircle(-4, 14, -4, 3, Material.OAK_LEAVES);
        // Branch supports
        b.flyingButtress(2, 10, 0, 5, 13, 3, Material.OAK_LOG);
        b.flyingButtress(-2, 11, 0, -4, 14, -4, Material.OAK_LOG);

        // Level 1 room (y=5-8): living room
        b.fillBox(-4, 5, -4, 4, 5, 4, Material.OAK_PLANKS);
        b.fillWalls(-4, 6, -4, 4, 8, 4, Material.OAK_PLANKS);
        b.hollowBox(-4, 6, -4, 4, 8, 4);
        b.setBlock(-4, 7, 0, Material.GLASS_PANE);
        b.setBlock(4, 7, 0, Material.GLASS_PANE);
        b.table(0, 6, 0);
        b.chair(-1, 6, 0, Material.OAK_STAIRS, BlockFace.EAST);
        b.chair(1, 6, 0, Material.OAK_STAIRS, BlockFace.WEST);
        b.setBlock(-3, 7, 3, Material.LANTERN);

        // Level 2 room (y=9-12): bedroom
        b.fillBox(-3, 9, -3, 3, 9, 3, Material.OAK_PLANKS);
        b.fillWalls(-3, 10, -3, 3, 12, 3, Material.OAK_PLANKS);
        b.hollowBox(-3, 10, -3, 3, 12, 3);
        b.setBlock(-3, 11, 0, Material.GLASS_PANE);
        b.setBlock(3, 11, 0, Material.GLASS_PANE);
        b.setBlock(-2, 10, 2, Material.RED_BED);
        b.setBlock(2, 10, -2, Material.BOOKSHELF);
        b.setBlock(2, 11, -2, Material.BOOKSHELF);
        b.setBlock(0, 11, 2, Material.LANTERN);

        // Level 3 lookout platform (y=14)
        b.fillBox(-2, 14, -2, 2, 14, 2, Material.OAK_PLANKS);
        b.circle(0, 15, 0, 2, Material.OAK_FENCE);
        b.setBlock(0, 15, 0, Material.LANTERN);

        // Ladders connecting levels
        for (int y = 1; y <= 5; y++) b.setBlock(3, y, -2, Material.LADDER);
        for (int y = 6; y <= 9; y++) b.setBlock(2, y, -2, Material.LADDER);
        for (int y = 10; y <= 14; y++) b.setBlock(1, y, -1, Material.LADDER);

        // Rope bridge from branch canopy to main platform
        for (int x = 2; x <= 5; x++) {
            b.setBlock(x, 14, 3, Material.OAK_PLANKS);
            b.setBlock(x, 15, 4, Material.OAK_FENCE);
            b.setBlock(x, 15, 2, Material.OAK_FENCE);
        }

        // Roots at base
        b.setBlock(-3, 0, -3, Material.OAK_LOG);
        b.setBlock(3, 0, 3, Material.OAK_LOG);
        b.setBlock(-3, 0, 2, Material.OAK_LOG);
        b.setBlock(3, 0, -2, Material.OAK_LOG);

        // Flower pots on balcony
        b.setBlock(-4, 6, -4, Material.POTTED_FERN);
        b.setBlock(4, 6, 4, Material.POTTED_AZURE_BLUET);

        b.placeChest(-2, 6, -2);
    }

    // ═══════════════════════════════════════════════════════════════
    //  7. DRUIDS GROVE
    // ═══════════════════════════════════════════════════════════════

    public static void buildDruidsGrove(StructureBuilder b) {
        // Mossy clearing
        b.filledCircle(0, 0, 0, 14, Material.MOSS_BLOCK);
        b.scatter(-14, 0, -14, 14, 0, 14, Material.MOSS_BLOCK, Material.GRASS_BLOCK, 0.3);

        // Stone circle (8 standing stones, 4 blocks tall)
        int[][] stones = {{10, 0}, {-10, 0}, {0, 10}, {0, -10}, {7, 7}, {-7, 7}, {7, -7}, {-7, -7}};
        for (int[] s : stones) {
            b.pillar(s[0], 1, 4, s[1], Material.MOSSY_STONE_BRICKS);
            b.setBlock(s[0], 5, s[1], Material.STONE_BRICK_SLAB);
        }

        // Central altar
        b.fillBox(-2, 1, -2, 2, 1, 2, Material.MOSSY_STONE_BRICKS);
        b.fillBox(-1, 2, -1, 1, 2, 1, Material.SMOOTH_STONE);
        b.setBlock(0, 3, 0, Material.GLOWSTONE);
        b.setBlock(-1, 3, -1, Material.CANDLE);
        b.setBlock(1, 3, -1, Material.CANDLE);
        b.setBlock(-1, 3, 1, Material.CANDLE);
        b.setBlock(1, 3, 1, Material.CANDLE);

        // Sacred oak tree behind altar
        b.fillBox(-1, 0, 5, 1, 18, 7, Material.OAK_LOG);
        for (int y = 12; y <= 20; y++) {
            int r = y < 16 ? 6 : (y < 19 ? 4 : 2);
            b.filledCircle(0, y, 6, r, Material.OAK_LEAVES);
        }
        // Glowstone hidden in leaves
        b.setBlock(2, 14, 6, Material.GLOWSTONE);
        b.setBlock(-2, 15, 8, Material.GLOWSTONE);
        b.setBlock(0, 17, 6, Material.GLOWSTONE);

        // Surrounding oaks (smaller)
        int[][] treePos = {{-10, -8}, {10, -6}, {-8, 10}, {12, 8}};
        for (int[] tp : treePos) {
            b.pillar(tp[0], 1, 8, tp[1], Material.OAK_LOG);
            for (int y = 6; y <= 10; y++) b.filledCircle(tp[0], y, tp[1], 3, Material.OAK_LEAVES);
        }

        // Flower patches
        for (int a = 0; a < 360; a += 25) {
            int fx = (int) (12 * Math.cos(Math.toRadians(a)));
            int fz = (int) (12 * Math.sin(Math.toRadians(a)));
            Material flower = switch (a % 100) {
                case 0 -> Material.POPPY;
                case 25 -> Material.DANDELION;
                case 50 -> Material.CORNFLOWER;
                default -> Material.OXEYE_DAISY;
            };
            b.setBlockIfAir(fx, 1, fz, flower);
        }

        // Glow lichen on standing stones
        b.setBlock(-10, 3, 0, Material.GLOW_LICHEN);
        b.setBlock(10, 3, 0, Material.GLOW_LICHEN);
        b.setBlock(0, 3, 10, Material.GLOW_LICHEN);

        b.placeChest(0, 3, -2);
        b.setBossSpawn(0, 1, 5);
    }

    // ═══════════════════════════════════════════════════════════════
    //  8. ALLAY SANCTUARY
    // ═══════════════════════════════════════════════════════════════

    public static void buildAllaySanctuary(StructureBuilder b) {
        // Meadow base
        b.filledCircle(0, 0, 0, 12, Material.GRASS_BLOCK);
        b.scatter(-12, 0, -12, 12, 0, 12, Material.GRASS_BLOCK, Material.FLOWERING_AZALEA_LEAVES, 0.05);

        // Glass dome structure
        b.dome(0, 1, 0, 8, Material.LIGHT_BLUE_STAINED_GLASS);

        // Calcite floor with amethyst ring
        b.filledCircle(0, 0, 0, 8, Material.CALCITE);
        b.circle(0, 0, 0, 6, Material.AMETHYST_BLOCK);

        // Interior flowering garden
        b.setBlock(-2, 1, -2, Material.FLOWERING_AZALEA);
        b.setBlock(2, 1, 2, Material.FLOWERING_AZALEA);
        b.setBlock(-3, 1, 3, Material.AZALEA);
        b.setBlock(3, 1, -3, Material.AZALEA);

        // Copper pillars
        for (int[] p : new int[][]{{-5, 0}, {5, 0}, {0, -5}, {0, 5}}) {
            b.pillar(p[0], 1, 6, p[1], Material.OXIDIZED_COPPER);
            b.setBlock(p[0], 7, p[1], Material.END_ROD);
        }

        // Note blocks and amethyst
        b.setBlock(-4, 1, -4, Material.NOTE_BLOCK);
        b.setBlock(4, 1, 4, Material.NOTE_BLOCK);
        b.setBlock(4, 1, -4, Material.NOTE_BLOCK);
        b.setBlock(-4, 1, 4, Material.NOTE_BLOCK);

        // Central waterfall feature
        b.pillar(0, 1, 5, 0, Material.COPPER_BLOCK);
        b.setBlock(0, 6, 0, Material.WATER);

        // Amethyst decorations
        b.setBlock(-3, 1, 0, Material.AMETHYST_CLUSTER);
        b.setBlock(3, 1, 0, Material.AMETHYST_CLUSTER);
        b.setBlock(0, 1, -3, Material.BUDDING_AMETHYST);
        b.setBlock(0, 1, 3, Material.BUDDING_AMETHYST);

        // Candles
        b.setBlock(-2, 1, 0, Material.CYAN_CANDLE);
        b.setBlock(2, 1, 0, Material.CYAN_CANDLE);
        b.setBlock(0, 1, -2, Material.LIGHT_BLUE_CANDLE);
        b.setBlock(0, 1, 2, Material.LIGHT_BLUE_CANDLE);

        // Flower pots around exterior
        for (int a = 0; a < 360; a += 45) {
            int fx = (int) (10 * Math.cos(Math.toRadians(a)));
            int fz = (int) (10 * Math.sin(Math.toRadians(a)));
            b.setBlock(fx, 1, fz, Material.POTTED_ALLIUM);
        }

        b.placeChest(0, 1, 0);
    }

    // ═══════════════════════════════════════════════════════════════
    //  9. MAGE TOWER
    // ═══════════════════════════════════════════════════════════════

    public static void buildMageTower(StructureBuilder b) {
        int radius = 6;

        // Foundation
        b.filledCircle(0, 0, 0, radius + 1, Material.STONE_BRICKS);

        // 8 stories, each 5 blocks high
        for (int story = 0; story < 8; story++) {
            int baseY = 1 + story * 5;
            int r = radius - (story / 3);

            // Floor
            b.filledCircle(0, baseY, 0, r, Material.STONE_BRICKS);

            // Walls
            for (int y = baseY + 1; y <= baseY + 4; y++) {
                b.circle(0, y, 0, r, Material.STONE_BRICKS);
            }

            // Clear interior
            for (int y = baseY + 1; y <= baseY + 4; y++) {
                b.filledCircle(0, y, 0, r - 1, Material.AIR);
            }

            // Purple stained glass windows on each floor
            b.setBlock(r, baseY + 2, 0, Material.PURPLE_STAINED_GLASS);
            b.setBlock(-r, baseY + 2, 0, Material.PURPLE_STAINED_GLASS);
            b.setBlock(0, baseY + 2, r, Material.PURPLE_STAINED_GLASS);
            b.setBlock(0, baseY + 2, -r, Material.PURPLE_STAINED_GLASS);
            b.setBlock(r, baseY + 3, 0, Material.PURPLE_STAINED_GLASS);
            b.setBlock(-r, baseY + 3, 0, Material.PURPLE_STAINED_GLASS);

            // Floor lighting
            b.setBlock(0, baseY + 4, 0, Material.LANTERN);
        }

        // Floor 1 (y=1): Entrance hall
        b.setBlock(radius, 2, 0, Material.AIR);
        b.setBlock(radius, 3, 0, Material.AIR);  // Door
        b.table(0, 2, 0);
        b.chair(-1, 2, 0, Material.OAK_STAIRS, BlockFace.EAST);

        // Floor 2 (y=6): Library / Enchanting
        b.setBlock(0, 7, 0, Material.ENCHANTING_TABLE);
        // Ring of bookshelves (15 for max enchanting)
        for (int a = 0; a < 360; a += 24) {
            int bx = (int) Math.round(3 * Math.cos(Math.toRadians(a)));
            int bz = (int) Math.round(3 * Math.sin(Math.toRadians(a)));
            b.setBlock(bx, 7, bz, Material.BOOKSHELF);
            b.setBlock(bx, 8, bz, Material.BOOKSHELF);
        }

        // Floor 3 (y=11): Potion room
        b.setBlock(-2, 12, 0, Material.BREWING_STAND);
        b.setBlock(2, 12, 0, Material.BREWING_STAND);
        b.setBlock(0, 12, -2, Material.CAULDRON);

        // Floor 4 (y=16): Alchemy lab
        b.setBlock(0, 17, 0, Material.CRAFTING_TABLE);
        b.setBlock(-2, 17, 2, Material.BARREL);
        b.setBlock(2, 17, -2, Material.BARREL);

        // Floor 5 (y=21): Storage
        b.placeChest(0, 22, 0);
        b.placeChest(-2, 22, 2);

        // Floor 6 (y=26): Study
        b.setBlock(-2, 27, -2, Material.BOOKSHELF);
        b.setBlock(-2, 28, -2, Material.BOOKSHELF);
        b.setBlock(2, 27, 2, Material.LECTERN);

        // Floor 7 (y=31): Bedroom
        b.setBlock(-2, 32, 2, Material.RED_BED);
        b.setBlock(2, 32, -2, Material.CRAFTING_TABLE);

        // Floor 8 (y=36): Observatory
        b.setBlock(0, 37, 0, Material.END_ROD);
        b.setBlock(0, 38, 0, Material.END_ROD);
        b.setBlock(0, 39, 0, Material.END_ROD);
        b.setBlock(1, 37, 0, Material.END_ROD);
        b.setBlock(-1, 37, 0, Material.END_ROD);
        b.setBlock(0, 37, 1, Material.END_ROD);
        b.setBlock(0, 37, -1, Material.END_ROD);

        // Purple glass dome roof
        b.dome(0, 41, 0, 5, Material.PURPLE_STAINED_GLASS);

        // Spiral staircase (central pillar + steps)
        b.spiralStaircase(0, 1, 40, 0, 3, Material.STONE_BRICK_STAIRS, Material.STONE_BRICKS);

        // Exterior buttresses
        for (int a = 0; a < 360; a += 90) {
            int bx = (int) Math.round(radius * Math.cos(Math.toRadians(a)));
            int bz = (int) Math.round(radius * Math.sin(Math.toRadians(a)));
            b.flyingButtress(bx * 2, 0, bz * 2, bx, 10, bz, Material.STONE_BRICKS);
        }

        b.placeChest(2, 2, 2);
        b.setBossSpawn(0, 37, 0);
    }

    // ═══════════════════════════════════════════════════════════════
    //  10. RUINED COLOSSEUM
    // ═══════════════════════════════════════════════════════════════

    public static void buildRuinedColosseum(StructureBuilder b) {
        // Sand arena floor
        b.filledEllipse(0, 0, 0, 18, 12, Material.SAND);
        b.filledEllipse(0, 0, 0, 14, 9, Material.SMOOTH_SANDSTONE);

        // Tiered seating (4 tiers)
        for (int tier = 0; tier < 4; tier++) {
            int rx = 16 + tier * 2;
            int rz = 10 + tier * 2;
            int y = tier + 1;
            // Outer ring
            for (int a = 0; a < 360; a += 3) {
                int x = (int) Math.round(rx * Math.cos(Math.toRadians(a)));
                int z = (int) Math.round(rz * Math.sin(Math.toRadians(a)));
                b.setStairs(x, y, z, Material.STONE_BRICK_STAIRS, BlockFace.SOUTH, false);
            }
        }

        // Pillars around the arena (some broken/toppled)
        for (int a = 0; a < 360; a += 30) {
            int px = (int) (16 * Math.cos(Math.toRadians(a)));
            int pz = (int) (10 * Math.sin(Math.toRadians(a)));
            int pillarHeight = 8 + b.getRandom().nextInt(5);
            if (b.getRandom().nextDouble() < 0.3) {
                // Broken pillar (shorter)
                pillarHeight = 3 + b.getRandom().nextInt(3);
            }
            b.pillar(px, 1, pillarHeight, pz, Material.STONE_BRICK_WALL);
            if (pillarHeight > 6) {
                b.setBlock(px, pillarHeight + 1, pz, Material.STONE_BRICK_SLAB);
            }
        }

        // Connecting arches between tall pillars
        for (int a = 0; a < 360; a += 60) {
            int px = (int) (16 * Math.cos(Math.toRadians(a)));
            int pz = (int) (10 * Math.sin(Math.toRadians(a)));
            b.setBlock(px, 9, pz, Material.CHISELED_STONE_BRICKS);
        }

        // Gladiator cells underneath (south side)
        for (int cell = -2; cell <= 2; cell++) {
            int cx = cell * 5;
            b.fillBox(cx - 2, -3, 8, cx + 2, -1, 12, Material.STONE_BRICKS);
            b.fillBox(cx - 1, -2, 9, cx + 1, -1, 11, Material.AIR);
            b.setBlock(cx, -2, 8, Material.IRON_BARS);
            b.setBlock(cx, -1, 8, Material.IRON_BARS);
        }

        // Champion's trophy room
        b.fillBox(-4, -3, -16, 4, 0, -10, Material.STONE_BRICKS);
        b.fillBox(-3, -2, -15, 3, -1, -11, Material.AIR);
        b.fillFloor(-3, -15, 3, -11, -3, Material.SMOOTH_SANDSTONE);
        b.setBlock(0, -2, -13, Material.GOLD_BLOCK);
        b.setBlock(-2, -2, -14, Material.LIGHTNING_ROD);
        b.setBlock(2, -2, -14, Material.LIGHTNING_ROD);
        b.setBlock(0, -1, -13, Material.LANTERN);

        // Decay and moss
        b.scatter(-22, 0, -16, 22, 12, 16, Material.STONE_BRICKS, Material.CRACKED_STONE_BRICKS, 0.25);
        b.scatter(-22, 0, -16, 22, 12, 16, Material.STONE_BRICKS, Material.MOSSY_STONE_BRICKS, 0.15);
        b.decay(-22, 5, -16, 22, 12, 16, 0.18);
        b.addVines(-22, 1, -16, 22, 12, 16, 0.08);

        b.placeChest(0, -2, -12);
        b.placeChest(0, 1, 0);
        b.setBossSpawn(0, 1, 5);
    }

    // ═══════════════════════════════════════════════════════════════
    //  11. SHREK HOUSE
    // ═══════════════════════════════════════════════════════════════

    public static void buildShrekHouse(StructureBuilder b) {
        // Swamp ground
        b.filledCircle(0, 0, 0, 10, Material.DIRT);
        b.scatter(-10, 0, -10, 10, 0, 10, Material.DIRT, Material.MUD, 0.3);

        // Round-ish log cabin (octagonal walls)
        b.fillWalls(-5, 1, -4, 5, 5, 4, Material.OAK_LOG);
        b.fillWalls(-4, 1, -5, 4, 5, 5, Material.OAK_LOG);
        b.hollowBox(-5, 1, -4, 5, 5, 4);
        b.hollowBox(-4, 1, -5, 4, 5, 5);
        // Corner fills for roundness
        b.pillar(-5, 1, 5, -4, Material.OAK_LOG);
        b.pillar(5, 1, 5, -4, Material.OAK_LOG);
        b.pillar(-5, 1, 5, 4, Material.OAK_LOG);
        b.pillar(5, 1, 5, 4, Material.OAK_LOG);

        // Floor
        b.fillBox(-4, 0, -4, 4, 0, 4, Material.OAK_PLANKS);

        // Layered conical roof
        b.fillBox(-6, 6, -5, 6, 6, 5, Material.OAK_PLANKS);
        b.fillBox(-5, 6, -6, 5, 6, 6, Material.OAK_PLANKS);
        b.fillBox(-5, 7, -4, 5, 7, 4, Material.OAK_PLANKS);
        b.fillBox(-4, 7, -5, 4, 7, 5, Material.OAK_PLANKS);
        b.fillBox(-4, 8, -3, 4, 8, 3, Material.OAK_PLANKS);
        b.fillBox(-3, 8, -4, 3, 8, 4, Material.OAK_PLANKS);
        b.fillBox(-3, 9, -2, 3, 9, 2, Material.OAK_PLANKS);
        b.fillBox(-2, 10, -1, 2, 10, 1, Material.OAK_PLANKS);
        b.setBlock(0, 11, 0, Material.OAK_PLANKS);

        // Chimney
        b.pillar(3, 6, 12, 3, Material.COBBLESTONE);
        b.setBlock(3, 12, 3, Material.COBBLESTONE_WALL);
        b.setBlock(3, 11, 3, Material.CAMPFIRE);

        // Door
        b.setBlock(0, 1, -5, Material.AIR); b.setBlock(0, 2, -5, Material.AIR);

        // Windows
        b.setBlock(-5, 3, 0, Material.GLASS_PANE);
        b.setBlock(5, 3, 0, Material.GLASS_PANE);
        b.setBlock(0, 3, 4, Material.GLASS_PANE);

        // Interior furnishing
        b.setBlock(-3, 1, 2, Material.CRAFTING_TABLE);
        b.setBlock(-3, 1, 3, Material.FURNACE);
        b.setBlock(3, 1, 3, Material.CAULDRON);
        b.table(0, 1, 0);
        b.chair(-1, 1, 0, Material.OAK_STAIRS, BlockFace.EAST);
        b.chair(1, 1, 0, Material.OAK_STAIRS, BlockFace.WEST);
        b.setBlock(-3, 1, -3, Material.RED_BED);
        b.setBlock(2, 1, -3, Material.BARREL);

        // "Keep Out" sign
        b.setBlock(0, 2, -6, Material.OAK_SIGN);

        // Outhouse (small structure to the side)
        b.fillBox(8, 0, -2, 10, 0, 2, Material.OAK_PLANKS);
        b.fillWalls(8, 1, -2, 10, 4, 2, Material.OAK_PLANKS);
        b.hollowBox(8, 1, -2, 10, 4, 2);
        b.setBlock(9, 1, -2, Material.AIR); b.setBlock(9, 2, -2, Material.AIR);
        b.setBlock(9, 4, 0, Material.OAK_PLANKS);

        // Onion garden (using carved pumpkins/potatoes)
        b.fillBox(-7, 0, -7, -4, 0, -4, Material.FARMLAND);
        b.setBlock(-6, 1, -6, Material.POTATOES);
        b.setBlock(-5, 1, -6, Material.POTATOES);
        b.setBlock(-6, 1, -5, Material.CARROTS);
        b.setBlock(-5, 1, -5, Material.CARVED_PUMPKIN);

        // Mud path
        b.setBlock(0, 0, -7, Material.MUD);
        b.setBlock(0, 0, -8, Material.MUD);
        b.setBlock(1, 0, -9, Material.MUD);
        b.setBlock(0, 0, -9, Material.MUD);

        // Swamp tree nearby
        b.pillar(-8, 1, 8, 6, Material.OAK_LOG);
        for (int y = 5; y <= 9; y++) b.filledCircle(-8, y, 6, 3, Material.OAK_LEAVES);
        b.addVines(-11, 3, 3, -5, 9, 9, 0.3);

        // Mushrooms
        b.setBlock(-8, 1, -1, Material.RED_MUSHROOM);
        b.setBlock(-9, 1, 2, Material.BROWN_MUSHROOM);
        b.setBlock(7, 1, 4, Material.RED_MUSHROOM);

        b.placeChest(0, 1, 2);
        b.setBossSpawn(0, 1, -7);
    }

    // ═══════════════════════════════════════════════════════════════
    //  12. ANCIENT TEMPLE
    // ═══════════════════════════════════════════════════════════════

    public static void buildAncientTemple(StructureBuilder b) {
        // Stepped pyramid (10 layers)
        for (int layer = 0; layer < 10; layer++) {
            int r = 16 - layer;
            b.fillBox(-r, layer, -r, r, layer, r, Material.SANDSTONE);
        }

        // Interior hollow
        b.fillBox(-12, 1, -12, 12, 7, 12, Material.AIR);

        // Entrance with carved pillars (4 sides)
        for (int face = 0; face < 4; face++) {
            int dx = face == 0 ? 0 : (face == 2 ? 0 : (face == 1 ? 1 : -1));
            int dz = face == 0 ? -1 : (face == 2 ? 1 : 0);
            int ex = dx * 14; int ez = dz * 14;
            // Entrance opening
            b.fillBox(ex - 2, 1, ez - 2 * Math.abs(dx), ex + 2, 4, ez + 2 * Math.abs(dx), Material.AIR);
        }
        // Main entrance (south)
        b.fillBox(-2, 1, -16, 2, 4, -12, Material.AIR);
        // Pillars flanking entrance
        b.pillar(-3, 1, 7, -14, Material.SANDSTONE_WALL);
        b.pillar(3, 1, 7, -14, Material.SANDSTONE_WALL);
        b.pillar(-3, 1, 7, -12, Material.SANDSTONE_WALL);
        b.pillar(3, 1, 7, -12, Material.SANDSTONE_WALL);

        // Central treasure chamber
        b.fillBox(-4, 1, -4, 4, 1, 4, Material.GOLD_BLOCK);
        b.setBlock(0, 2, 0, Material.DIAMOND_BLOCK);

        // Hieroglyph walls (glazed terracotta)
        Material[] terracotta = {Material.ORANGE_GLAZED_TERRACOTTA, Material.YELLOW_GLAZED_TERRACOTTA,
                Material.CYAN_GLAZED_TERRACOTTA, Material.BLUE_GLAZED_TERRACOTTA};
        for (int y = 1; y <= 5; y++) {
            for (int x = -10; x <= 10; x += 3) {
                b.setBlock(x, y, 12, terracotta[(x + y) & 3]);
                b.setBlock(x, y, -12, terracotta[(x + y + 1) & 3]);
            }
            for (int z = -10; z <= 10; z += 3) {
                b.setBlock(12, y, z, terracotta[(z + y) & 3]);
                b.setBlock(-12, y, z, terracotta[(z + y + 2) & 3]);
            }
        }

        // Trapped corridors (tripwire)
        for (int z = -10; z <= -4; z += 3) {
            b.setBlock(-1, 1, z, Material.TRIPWIRE);
            b.setBlock(0, 1, z, Material.TRIPWIRE);
            b.setBlock(1, 1, z, Material.TRIPWIRE);
        }

        // Hidden room behind waterfall (east side)
        b.fillBox(12, 1, -3, 14, 4, 3, Material.AIR);
        b.pillar(12, 1, 4, 0, Material.WATER);
        b.placeChest(13, 1, 0);

        // Interior pillars
        for (int[] p : new int[][]{{-8, -8}, {8, -8}, {-8, 8}, {8, 8}}) {
            b.pillar(p[0], 1, 7, p[1], Material.SANDSTONE_WALL);
            b.setBlock(p[0], 8, p[1], Material.CUT_SANDSTONE);
        }

        // Torches
        b.wallLighting(-11, 3, -12, 11, -12, 4, Material.TORCH);
        b.wallLighting(-11, 3, 12, 11, 12, 4, Material.TORCH);

        // Prismarine accents
        b.setBlock(0, 7, 0, Material.PRISMARINE);
        b.setBlock(-5, 1, 0, Material.PRISMARINE_BRICKS);
        b.setBlock(5, 1, 0, Material.PRISMARINE_BRICKS);

        b.placeChest(0, 1, 5);
        b.placeChest(0, 1, -5);
        b.setBossSpawn(0, 1, 0);
    }

    // ═══════════════════════════════════════════════════════════════
    //  13. VOLCANO
    // ═══════════════════════════════════════════════════════════════

    public static void buildVolcano(StructureBuilder b) {
        // Cone shape (40 blocks high)
        for (int y = 0; y < 40; y++) {
            int r = 20 - (y * 20 / 40);
            if (r < 2) r = 2;
            // Outer shell: basalt
            b.filledCircle(0, y, 0, r, Material.BASALT);
            // Inner core: blackstone
            if (r > 4) b.filledCircle(0, y, 0, r - 3, Material.BLACKSTONE);
        }

        // Lava core
        for (int y = 1; y < 38; y++) {
            int r = Math.max(1, 5 - (y / 8));
            b.filledCircle(0, y, 0, r, Material.LAVA);
        }

        // Lava pool at top (crater)
        b.filledCircle(0, 38, 0, 3, Material.LAVA);
        b.filledCircle(0, 39, 0, 2, Material.LAVA);

        // Magma block veins running down exterior
        b.scatter(-20, 0, -20, 20, 40, 20, Material.BASALT, Material.MAGMA_BLOCK, 0.12);

        // Obsidian chamber at base (treasure room)
        b.fillBox(-6, 3, -6, 6, 8, 6, Material.AIR);
        b.fillBox(-6, 2, -6, 6, 2, 6, Material.OBSIDIAN);
        b.fillWalls(-6, 3, -6, 6, 8, 6, Material.OBSIDIAN);
        // Don't fill interior with obsidian
        b.fillBox(-5, 3, -5, 5, 7, 5, Material.AIR);

        // Lava channels inside chamber
        b.setBlock(-5, 3, 0, Material.LAVA);
        b.setBlock(5, 3, 0, Material.LAVA);
        b.setBlock(0, 3, -5, Material.LAVA);
        b.setBlock(0, 3, 5, Material.LAVA);

        // Treasure in chamber
        b.setBlock(0, 3, 0, Material.GOLD_BLOCK);
        b.setBlock(-2, 3, -2, Material.DIAMOND_BLOCK);

        // Lava tube entrance
        b.fillBox(-2, 3, -8, 2, 5, -6, Material.AIR);

        // Smoke area at top (soul campfires)
        b.setBlock(-1, 40, 0, Material.SOUL_CAMPFIRE);
        b.setBlock(1, 40, 0, Material.SOUL_CAMPFIRE);
        b.setBlock(0, 40, -1, Material.SOUL_CAMPFIRE);

        // Decorative obsidian outcrops
        b.setBlock(-12, 8, -12, Material.CRYING_OBSIDIAN);
        b.setBlock(10, 5, 10, Material.CRYING_OBSIDIAN);
        b.setBlock(-8, 12, 8, Material.CRYING_OBSIDIAN);

        // Lanterns in treasure cave
        b.setBlock(-4, 6, -4, Material.LANTERN);
        b.setBlock(4, 6, 4, Material.LANTERN);

        b.placeChest(0, 3, 2);
        b.placeChest(-3, 3, -3);
        b.setBossSpawn(0, 3, 4);
    }

    // ═══════════════════════════════════════════════════════════════
    //  14. FORTRESS
    // ═══════════════════════════════════════════════════════════════

    public static void buildFortress(StructureBuilder b) {
        // Outer wall (star fort shape simulated with rectangle + bastions)
        b.fillWalls(-24, 0, -24, 24, 9, 24, Material.DEEPSLATE_BRICKS);
        b.hollowBox(-24, 1, -24, 24, 9, 24);
        b.fillFloor(-23, -23, 23, 23, 0, Material.STONE_BRICKS);

        // Battlements
        for (int x = -24; x <= 24; x += 2) {
            b.setBlock(x, 10, -24, Material.DEEPSLATE_BRICK_WALL);
            b.setBlock(x, 10, 24, Material.DEEPSLATE_BRICK_WALL);
        }
        for (int z = -24; z <= 24; z += 2) {
            b.setBlock(-24, 10, z, Material.DEEPSLATE_BRICK_WALL);
            b.setBlock(24, 10, z, Material.DEEPSLATE_BRICK_WALL);
        }

        // 4 Bastions (triangular protrusions at corners)
        for (int[] c : new int[][]{{-24, -24}, {24, -24}, {-24, 24}, {24, 24}}) {
            b.fillBox(c[0] - 3, 0, c[1] - 3, c[0] + 3, 14, c[1] + 3, Material.DEEPSLATE_BRICKS);
            b.fillBox(c[0] - 2, 1, c[1] - 2, c[0] + 2, 13, c[1] + 2, Material.AIR);
            b.spire(c[0], 14, c[1], 3, 5, Material.DEEPSLATE_TILE_SLAB);
            for (int y = 1; y <= 13; y++) b.setBlock(c[0] + 1, y, c[1] - 2, Material.LADDER);
            b.setBlock(c[0], 13, c[1], Material.LANTERN);
        }

        // Moat (water ring)
        for (int x = -27; x <= 27; x++) {
            for (int z = -27; z <= 27; z++) {
                if (Math.abs(x) >= 25 || Math.abs(z) >= 25) {
                    if (Math.abs(x) <= 27 && Math.abs(z) <= 27) {
                        b.setBlock(x, -1, z, Material.WATER);
                        b.setBlock(x, 0, z, Material.WATER);
                    }
                }
            }
        }

        // Drawbridge (south)
        b.fillBox(-2, 1, -24, 2, 5, -24, Material.AIR);  // Gate opening
        b.fillBox(-2, 0, -27, 2, 0, -24, Material.OAK_PLANKS);  // Bridge
        b.pillar(-3, 1, 8, -24, Material.IRON_BARS);
        b.pillar(3, 1, 8, -24, Material.IRON_BARS);

        // Central keep
        b.fillWalls(-8, 0, -8, 8, 18, 8, Material.POLISHED_DEEPSLATE);
        b.hollowBox(-8, 1, -8, 8, 18, 8);
        b.fillFloor(-7, -7, 7, 7, 0, Material.STONE_BRICKS);
        b.spire(0, 18, 0, 5, 8, Material.DEEPSLATE_TILE_SLAB);
        b.setBlock(0, 26, 0, Material.END_ROD);

        // Keep entrance
        b.setBlock(0, 1, -8, Material.AIR); b.setBlock(0, 2, -8, Material.AIR); b.setBlock(0, 3, -8, Material.AIR);

        // Keep interior: throne
        b.setBlock(0, 1, 5, Material.GOLD_BLOCK);
        b.setStairs(-1, 1, 5, Material.STONE_BRICK_STAIRS, BlockFace.EAST, false);
        b.setStairs(1, 1, 5, Material.STONE_BRICK_STAIRS, BlockFace.WEST, false);
        b.setBlock(0, 2, 6, Material.GOLD_BLOCK);

        // Barracks room (NW)
        b.fillBox(-22, 0, -22, -14, 0, -14, Material.STONE_BRICKS);
        b.fillWalls(-22, 1, -22, -14, 5, -14, Material.STONE_BRICKS);
        b.hollowBox(-22, 1, -22, -14, 5, -14);
        b.fillBox(-22, 6, -22, -14, 6, -14, Material.OAK_PLANKS);
        b.setBlock(-18, 1, -22, Material.AIR); b.setBlock(-18, 2, -22, Material.AIR);
        for (int bx = -21; bx <= -15; bx += 3) b.setBlock(bx, 1, -20, Material.RED_BED);

        // Armory (NE)
        b.fillBox(14, 0, -22, 22, 0, -14, Material.STONE_BRICKS);
        b.fillWalls(14, 1, -22, 22, 5, -14, Material.COBBLESTONE);
        b.hollowBox(14, 1, -22, 22, 5, -14);
        b.fillBox(14, 6, -22, 22, 6, -14, Material.OAK_PLANKS);
        b.setBlock(18, 1, -22, Material.AIR); b.setBlock(18, 2, -22, Material.AIR);
        b.setBlock(15, 1, -20, Material.ANVIL);
        b.setBlock(16, 1, -20, Material.SMITHING_TABLE);
        b.setBlock(21, 1, -15, Material.GRINDSTONE);

        // Dungeon below
        b.fillBox(-12, -6, -12, 12, -1, 12, Material.DEEPSLATE_BRICKS);
        b.fillBox(-11, -5, -11, 11, -1, 11, Material.AIR);
        b.fillFloor(-11, -11, 11, 11, -6, Material.DEEPSLATE_TILES);
        for (int z = -8; z <= 8; z += 4) {
            b.setBlock(-10, -5, z, Material.IRON_BARS);
            b.setBlock(-10, -4, z, Material.IRON_BARS);
            b.setBlock(10, -5, z, Material.IRON_BARS);
            b.setBlock(10, -4, z, Material.IRON_BARS);
        }
        b.setBlock(0, -5, 0, Material.SOUL_LANTERN);

        // Wall lighting
        b.wallLighting(-23, 5, -24, 23, -24, 5, Material.LANTERN);
        b.wallLighting(-23, 5, 24, 23, 24, 5, Material.LANTERN);

        b.placeChest(-20, 1, -18);
        b.placeChest(18, 1, -18);
        b.placeChest(0, -5, 5);
        b.setBossSpawn(0, 1, 0);
    }

    // ═══════════════════════════════════════════════════════════════
    //  15. GIGANTIC CASTLE
    // ═══════════════════════════════════════════════════════════════

    public static void buildGiganticCastle(StructureBuilder b) {
        // Curtain wall
        b.fillWalls(-38, 0, -38, 38, 14, 38, Material.STONE_BRICKS);
        b.hollowBox(-38, 1, -38, 38, 14, 38);
        b.fillFloor(-37, -37, 37, 37, 0, Material.COBBLESTONE);
        // Scattered grass in courtyard
        b.scatter(-30, 0, -30, 30, 0, 30, Material.COBBLESTONE, Material.GRASS_BLOCK, 0.2);

        // Battlements on curtain wall
        for (int x = -38; x <= 38; x += 2) {
            b.setBlock(x, 15, -38, Material.STONE_BRICK_WALL);
            b.setBlock(x, 15, 38, Material.STONE_BRICK_WALL);
        }
        for (int z = -38; z <= 38; z += 2) {
            b.setBlock(-38, 15, z, Material.STONE_BRICK_WALL);
            b.setBlock(38, 15, z, Material.STONE_BRICK_WALL);
        }

        // 8 Towers along the walls
        int[][] towerPos = {{-38, -38}, {0, -38}, {38, -38}, {-38, 0}, {38, 0}, {-38, 38}, {0, 38}, {38, 38}};
        for (int[] t : towerPos) {
            b.fillBox(t[0] - 3, 0, t[1] - 3, t[0] + 3, 22, t[1] + 3, Material.STONE_BRICKS);
            b.fillBox(t[0] - 2, 1, t[1] - 2, t[0] + 2, 21, t[1] + 2, Material.AIR);
            b.spire(t[0], 22, t[1], 4, 6, Material.DARK_OAK_PLANKS);
            for (int y = 1; y <= 21; y++) b.setBlock(t[0] + 1, y, t[1] - 2, Material.LADDER);
            b.setBlock(t[0], 20, t[1], Material.LANTERN);
        }

        // Gatehouse with portcullis (south)
        b.fillBox(-5, 0, -38, 5, 16, -32, Material.STONE_BRICKS);
        b.fillBox(-3, 1, -38, 3, 10, -32, Material.AIR);
        // Portcullis (iron bars)
        for (int x = -3; x <= 3; x++) {
            for (int y = 4; y <= 10; y++) {
                b.setBlock(x, y, -38, Material.IRON_BARS);
            }
        }
        b.fillBox(-3, 1, -38, 3, 3, -38, Material.AIR);  // Opening below portcullis

        // Great hall (interior 40x20)
        b.fillBox(-20, 0, -18, 20, 0, 2, Material.STONE_BRICKS);
        b.fillWalls(-20, 1, -18, 20, 12, 2, Material.STONE_BRICKS);
        b.hollowBox(-20, 1, -18, 20, 12, 2);
        b.fillBox(-20, 13, -18, 20, 13, 2, Material.OAK_PLANKS);
        b.fillFloor(-19, -17, 19, 1, 0, Material.POLISHED_ANDESITE);
        // Great hall entrance
        b.setBlock(0, 1, -18, Material.AIR); b.setBlock(0, 2, -18, Material.AIR); b.setBlock(0, 3, -18, Material.AIR);
        // Great hall pillars
        for (int x = -16; x <= 16; x += 8) {
            b.pillar(x, 1, 12, -15, Material.STONE_BRICK_WALL);
            b.pillar(x, 1, 12, 0, Material.STONE_BRICK_WALL);
        }
        // Chandeliers
        b.chandelier(-8, 13, -8, 3);
        b.chandelier(8, 13, -8, 3);
        b.chandelier(0, 13, -4, 4);
        // Dining tables
        for (int x = -12; x <= 12; x += 4) {
            b.table(x, 1, -8);
            b.table(x, 1, -4);
        }
        // Banners
        for (int x = -18; x <= 18; x += 6) {
            b.banner(x, 8, -18, Material.RED_BANNER);
            b.banner(x, 8, 2, Material.RED_BANNER);
        }

        // Throne room
        b.fillBox(-8, 0, 5, 8, 0, 18, Material.STONE_BRICKS);
        b.fillWalls(-8, 1, 5, 8, 10, 18, Material.STONE_BRICKS);
        b.hollowBox(-8, 1, 5, 8, 10, 18);
        b.fillFloor(-7, 6, 7, 17, 0, Material.RED_CARPET);
        b.setBlock(0, 1, 5, Material.AIR); b.setBlock(0, 2, 5, Material.AIR);
        // Throne
        b.setBlock(0, 1, 15, Material.GOLD_BLOCK);
        b.setBlock(0, 2, 15, Material.GOLD_BLOCK);
        b.setBlock(-1, 2, 16, Material.GOLD_BLOCK);
        b.setBlock(1, 2, 16, Material.GOLD_BLOCK);
        b.setBlock(0, 3, 16, Material.GOLD_BLOCK);
        b.setBlock(0, 1, 14, Material.RED_CARPET);

        // Kitchen (east wing)
        b.furnishedRoom(22, 0, -15, 35, -5, 5, Material.STONE_BRICKS, Material.STONE_BRICKS, Material.OAK_PLANKS);
        b.setBlock(25, 1, -12, Material.FURNACE);
        b.setBlock(26, 1, -12, Material.FURNACE);
        b.setBlock(27, 1, -12, Material.SMOKER);
        b.setBlock(30, 1, -10, Material.CAULDRON);
        b.setBlock(33, 1, -8, Material.BARREL);

        // Bedchambers (NE)
        b.furnishedRoom(22, 0, 10, 35, 25, 5, Material.STONE_BRICKS, Material.OAK_PLANKS, Material.OAK_PLANKS);
        b.setBlock(25, 1, 15, Material.RED_BED);
        b.setBlock(28, 1, 15, Material.RED_BED);
        b.setBlock(31, 1, 15, Material.RED_BED);

        // Chapel (west wing)
        b.furnishedRoom(-35, 0, -15, -22, -5, 5, Material.STONE_BRICKS, Material.STONE_BRICKS, Material.STONE_BRICKS);
        b.setBlock(-28, 1, -8, Material.GOLD_BLOCK); // Altar
        b.setBlock(-28, 2, -8, Material.CANDLE);
        b.bookshelfWall(-34, 1, -5, -30, 3);
        b.roseWindow(-28, 4, -15, 2, Material.YELLOW_STAINED_GLASS, BlockFace.NORTH);

        // Dungeon below
        b.fillBox(-10, -7, -10, 10, -1, 10, Material.DEEPSLATE_BRICKS);
        b.fillBox(-9, -6, -9, 9, -1, 9, Material.AIR);
        b.fillFloor(-9, -9, 9, 9, -7, Material.DEEPSLATE_TILES);
        for (int z = -8; z <= 8; z += 4) {
            b.setBlock(-8, -6, z, Material.IRON_BARS); b.setBlock(-8, -5, z, Material.IRON_BARS);
            b.setBlock(8, -6, z, Material.IRON_BARS); b.setBlock(8, -5, z, Material.IRON_BARS);
        }
        b.setBlock(0, -6, 0, Material.SOUL_LANTERN);

        // Courtyard well
        b.filledCircle(0, 0, -28, 2, Material.COBBLESTONE);
        b.setBlock(0, 0, -28, Material.WATER);
        b.pillar(-2, 1, 3, -28, Material.COBBLESTONE_WALL);
        b.pillar(2, 1, 3, -28, Material.COBBLESTONE_WALL);
        b.fillBox(-2, 4, -29, 2, 4, -27, Material.OAK_PLANKS);

        b.placeChest(0, 1, 0);
        b.placeChest(5, -6, 5);
        b.placeChest(-25, 1, -10);
        b.placeChest(28, 1, 18);
        b.setBossSpawn(0, 1, 10);
    }

    // ═══════════════════════════════════════════════════════════════
    //  16. ULTRA VILLAGE
    // ═══════════════════════════════════════════════════════════════

    public static void buildUltraVillage(StructureBuilder b) {
        // Village wall
        b.fillWalls(-42, 0, -42, 42, 5, 42, Material.STONE_BRICKS);
        b.hollowBox(-42, 1, -42, 42, 5, 42);

        // Battlements
        for (int x = -42; x <= 42; x += 2) {
            b.setBlock(x, 6, -42, Material.STONE_BRICK_WALL);
            b.setBlock(x, 6, 42, Material.STONE_BRICK_WALL);
        }
        for (int z = -42; z <= 42; z += 2) {
            b.setBlock(-42, 6, z, Material.STONE_BRICK_WALL);
            b.setBlock(42, 6, z, Material.STONE_BRICK_WALL);
        }

        // Gate
        b.fillBox(-3, 1, -42, 3, 4, -42, Material.AIR);
        b.setBlock(-4, 5, -42, Material.STONE_BRICK_WALL);
        b.setBlock(4, 5, -42, Material.STONE_BRICK_WALL);

        // Main road (gravel)
        b.fillBox(-2, 0, -42, 2, 0, 42, Material.GRAVEL);
        b.fillBox(-42, 0, -2, 42, 0, 2, Material.GRAVEL);

        // Market square (center)
        b.filledCircle(0, 0, 0, 6, Material.COBBLESTONE);
        b.filledCircle(0, 0, 0, 2, Material.WATER);
        b.pillar(-2, 1, 3, 2, Material.COBBLESTONE_WALL);
        b.pillar(2, 1, 3, 2, Material.COBBLESTONE_WALL);
        b.fillBox(-2, 4, -2, 2, 4, 2, Material.OAK_PLANKS);

        // Streetlamps along main roads
        for (int x = -38; x <= 38; x += 8) {
            b.pillar(x, 1, 3, 4, Material.COBBLESTONE_WALL);
            b.setBlock(x, 4, 4, Material.LANTERN);
        }
        for (int z = -38; z <= 38; z += 8) {
            b.pillar(4, 1, 3, z, Material.COBBLESTONE_WALL);
            b.setBlock(4, 4, z, Material.LANTERN);
        }

        // 10 houses in a grid
        int[][] housePositions = {
            {-30, -30}, {-15, -30}, {15, -30}, {30, -30},
            {-30, 10}, {-15, 10}, {15, 10}, {30, 10},
            {-30, 25}, {30, 25}
        };
        Material[] roofColors = {Material.OAK_PLANKS, Material.SPRUCE_PLANKS, Material.BIRCH_PLANKS,
                Material.OAK_PLANKS, Material.SPRUCE_PLANKS, Material.BIRCH_PLANKS,
                Material.OAK_PLANKS, Material.SPRUCE_PLANKS, Material.OAK_PLANKS, Material.BIRCH_PLANKS};
        for (int i = 0; i < housePositions.length; i++) {
            int hx = housePositions[i][0]; int hz = housePositions[i][1];
            b.fillBox(hx, 0, hz, hx + 8, 0, hz + 8, Material.COBBLESTONE);
            b.fillWalls(hx, 1, hz, hx + 8, 4, hz + 8, Material.OAK_PLANKS);
            b.hollowBox(hx, 1, hz, hx + 8, 4, hz + 8);
            b.fillBox(hx, 5, hz, hx + 8, 5, hz + 8, roofColors[i]);
            b.pyramidRoof(hx, hz, hx + 8, hz + 8, 5, roofColors[i]);
            // Door and window
            b.setBlock(hx + 4, 1, hz, Material.AIR); b.setBlock(hx + 4, 2, hz, Material.AIR);
            b.setBlock(hx, 3, hz + 4, Material.GLASS_PANE);
            b.setBlock(hx + 8, 3, hz + 4, Material.GLASS_PANE);
            // Interior: bed, table
            b.setBlock(hx + 2, 1, hz + 6, Material.RED_BED);
            b.table(hx + 5, 1, hz + 3);
            b.setBlock(hx + 4, 4, hz + 4, Material.LANTERN);
        }

        // Church (NE area)
        b.fillBox(10, 0, -25, 25, 0, -10, Material.STONE_BRICKS);
        b.fillWalls(10, 1, -25, 25, 10, -10, Material.STONE_BRICKS);
        b.hollowBox(10, 1, -25, 25, 10, -10);
        b.gabledRoof(10, -25, 25, -10, 11, Material.STONE_BRICK_STAIRS, Material.STONE_BRICK_SLAB);
        b.setBlock(17, 1, -25, Material.AIR); b.setBlock(17, 2, -25, Material.AIR);
        b.setBlock(17, 1, -12, Material.GOLD_BLOCK); // Altar
        b.setBlock(17, 2, -12, Material.CANDLE);
        b.roseWindow(17, 7, -25, 2, Material.YELLOW_STAINED_GLASS, BlockFace.NORTH);
        // Bell tower
        b.pillar(12, 11, 18, -23, Material.STONE_BRICKS);
        b.setBlock(12, 18, -23, Material.BELL);

        // Blacksmith
        b.fillBox(-25, 0, -25, -15, 0, -15, Material.COBBLESTONE);
        b.fillWalls(-25, 1, -25, -15, 5, -15, Material.COBBLESTONE);
        b.hollowBox(-25, 1, -25, -15, 5, -15);
        b.fillBox(-25, 6, -25, -15, 6, -15, Material.DARK_OAK_PLANKS);
        b.setBlock(-20, 1, -25, Material.AIR); b.setBlock(-20, 2, -25, Material.AIR);
        b.setBlock(-23, 1, -17, Material.ANVIL);
        b.setBlock(-22, 1, -17, Material.SMITHING_TABLE);
        b.setBlock(-17, 1, -23, Material.BLAST_FURNACE);
        b.setBlock(-24, 1, -23, Material.LAVA);

        // Inn
        b.fillBox(-25, 0, 15, -10, 0, 35, Material.COBBLESTONE);
        b.fillWalls(-25, 1, 15, -10, 6, 35, Material.OAK_PLANKS);
        b.hollowBox(-25, 1, 15, -10, 6, 35);
        b.fillBox(-25, 7, 15, -10, 7, 35, Material.SPRUCE_PLANKS);
        b.setBlock(-17, 1, 15, Material.AIR); b.setBlock(-17, 2, 15, Material.AIR);
        // Bar counter
        for (int x = -23; x <= -12; x++) b.setStairs(x, 1, 25, Material.SPRUCE_STAIRS, BlockFace.SOUTH, false);
        b.setBlock(-20, 1, 26, Material.BARREL);
        b.setBlock(-15, 1, 26, Material.BARREL);
        // Beds upstairs
        b.fillBox(-24, 4, 16, -11, 4, 34, Material.OAK_PLANKS);
        for (int x = -22; x <= -13; x += 3) b.setBlock(x, 5, 20, Material.RED_BED);

        // Farm plots
        for (int fz = 28; fz <= 38; fz += 5) {
            b.fillBox(10, 0, fz, 22, 0, fz + 3, Material.FARMLAND);
            for (int fx = 10; fx <= 22; fx++) b.setBlock(fx, 1, fz + 1, Material.WHEAT);
        }

        b.placeChest(-22, 1, -20);
        b.placeChest(18, 1, -18);
        b.placeChest(-20, 1, 28);
    }

    // ═══════════════════════════════════════════════════════════════
    //  17. DUNGEON DEEP
    // ═══════════════════════════════════════════════════════════════

    public static void buildDungeonDeep(StructureBuilder b) {
        // Level 1: Main hall
        b.fillWalls(-14, 0, -14, 14, 6, 14, Material.DEEPSLATE_BRICKS);
        b.hollowBox(-14, 1, -14, 14, 6, 14);
        b.fillFloor(-13, -13, 13, 13, 0, Material.DEEPSLATE_TILES);
        b.scatter(-13, 0, -13, 13, 0, 13, Material.DEEPSLATE_TILES, Material.SCULK, 0.08);

        // Cell blocks along walls
        for (int z = -12; z <= 12; z += 4) {
            // West cells
            b.setBlock(-13, 1, z, Material.IRON_BARS); b.setBlock(-13, 2, z, Material.IRON_BARS);
            b.setBlock(-13, 3, z, Material.IRON_BARS);
            // East cells
            b.setBlock(13, 1, z, Material.IRON_BARS); b.setBlock(13, 2, z, Material.IRON_BARS);
            b.setBlock(13, 3, z, Material.IRON_BARS);
        }

        // Torture room (NE)
        b.fillBox(6, 1, -13, 13, 5, -6, Material.DEEPSLATE_BRICKS);
        b.fillBox(7, 1, -12, 12, 4, -7, Material.AIR);
        b.setBlock(8, 1, -10, Material.ANVIL);
        b.setBlock(10, 1, -10, Material.CAULDRON);
        b.setBlock(9, 1, -8, Material.IRON_CHAIN);
        b.setBlock(9, 2, -8, Material.IRON_CHAIN);
        b.setBlock(11, 1, -8, Material.SOUL_LANTERN);

        // Warden's office (NW)
        b.fillBox(-13, 1, -13, -6, 5, -6, Material.DEEPSLATE_BRICKS);
        b.fillBox(-12, 1, -12, -7, 4, -7, Material.AIR);
        b.setBlock(-7, 1, -9, Material.AIR); b.setBlock(-7, 2, -9, Material.AIR); // Door
        b.table(-10, 1, -10);
        b.chair(-9, 1, -10, Material.OAK_STAIRS, BlockFace.WEST);
        b.setBlock(-11, 1, -11, Material.BOOKSHELF);
        b.setBlock(-11, 2, -11, Material.BOOKSHELF);
        b.setBlock(-10, 3, -10, Material.LANTERN);

        // Level 2: Corridors (below)
        b.fillBox(-14, -8, -14, 14, -1, 14, Material.DEEPSLATE_BRICKS);
        b.fillBox(-2, -7, -13, 2, -2, 13, Material.AIR);  // Main corridor
        b.fillBox(-13, -7, -2, 13, -2, 2, Material.AIR);  // Cross corridor
        b.fillFloor(-13, -13, 13, 13, -8, Material.DEEPSLATE_TILES);

        // Stairway between levels
        for (int s = 0; s < 7; s++) {
            b.setBlock(5 + s, -s, 0, Material.DEEPSLATE_BRICK_STAIRS);
        }

        // Spawner rooms on level 2
        b.fillBox(-12, -7, -12, -4, -3, -4, Material.AIR);
        b.setBlock(-8, -7, -8, Material.MOSSY_COBBLESTONE);
        b.fillBox(4, -7, 4, 12, -3, 12, Material.AIR);
        b.setBlock(8, -7, 8, Material.MOSSY_COBBLESTONE);

        // Flooded section (SW)
        b.fillBox(-12, -7, 4, -4, -5, 12, Material.AIR);
        b.fillBox(-12, -7, 4, -4, -7, 12, Material.WATER);

        // Level 3: Treasure vault (deepest)
        b.fillBox(-8, -14, -8, 8, -9, 8, Material.DEEPSLATE_BRICKS);
        b.fillBox(-7, -13, -7, 7, -9, 7, Material.AIR);
        b.fillFloor(-7, -7, 7, 7, -14, Material.OBSIDIAN);
        b.setBlock(0, -13, 0, Material.GOLD_BLOCK);
        b.setBlock(-4, -13, -4, Material.SOUL_LANTERN);
        b.setBlock(4, -13, 4, Material.SOUL_LANTERN);

        // Cave-in decoration
        b.scatter(-13, -7, -13, 13, -2, 13, Material.AIR, Material.COBBLESTONE, 0.04);

        // Lighting
        b.wallLighting(-13, 4, -14, 13, -14, 5, Material.SOUL_LANTERN);
        b.wallLighting(-13, 4, 14, 13, 14, 5, Material.SOUL_LANTERN);

        b.placeChest(-10, 1, -10);
        b.placeChest(0, -13, 4);
        b.placeChest(10, -7, 0);
        b.setBossSpawn(0, 1, 0);
    }

    // ═══════════════════════════════════════════════════════════════
    //  18. PILLAGER FORTRESS
    // ═══════════════════════════════════════════════════════════════

    public static void buildPillagerFortress(StructureBuilder b) {
        // Outer palisade walls
        b.fillWalls(-28, 0, -28, 28, 10, 28, Material.COBBLESTONE);
        b.hollowBox(-28, 1, -28, 28, 10, 28);
        b.fillFloor(-27, -27, 27, 27, 0, Material.STONE_BRICKS);

        // Battlements
        for (int x = -28; x <= 28; x += 2) {
            b.setBlock(x, 11, -28, Material.COBBLESTONE_WALL);
            b.setBlock(x, 11, 28, Material.COBBLESTONE_WALL);
        }
        for (int z = -28; z <= 28; z += 2) {
            b.setBlock(-28, 11, z, Material.COBBLESTONE_WALL);
            b.setBlock(28, 11, z, Material.COBBLESTONE_WALL);
        }

        // Corner watchtowers (4)
        for (int[] c : new int[][]{{-28, -28}, {28, -28}, {-28, 28}, {28, 28}}) {
            b.fillBox(c[0] - 3, 0, c[1] - 3, c[0] + 3, 16, c[1] + 3, Material.STONE_BRICKS);
            b.fillBox(c[0] - 2, 1, c[1] - 2, c[0] + 2, 15, c[1] + 2, Material.AIR);
            b.fillBox(c[0] - 4, 14, c[1] - 4, c[0] + 4, 14, c[1] + 4, Material.DARK_OAK_PLANKS);
            b.setBlock(c[0], 15, c[1], Material.LANTERN);
            for (int y = 1; y <= 14; y++) b.setBlock(c[0] + 1, y, c[1] - 2, Material.LADDER);
        }

        // Main gate
        b.fillBox(-3, 1, -28, 3, 7, -28, Material.AIR);
        b.setBlock(-4, 8, -28, Material.DARK_OAK_LOG); b.setBlock(4, 8, -28, Material.DARK_OAK_LOG);
        b.fillBox(-3, 7, -28, 3, 7, -28, Material.DARK_OAK_PLANKS);

        // Pillager banners
        for (int x = -2; x <= 2; x += 2) b.setBlock(x, 8, -27, Material.WHITE_BANNER);

        // Central courtyard
        b.filledCircle(0, 0, 0, 8, Material.SMOOTH_STONE);
        b.setBlock(0, 1, 0, Material.CAMPFIRE);
        b.pillar(-6, 1, 4, 0, Material.DARK_OAK_FENCE); b.pillar(6, 1, 4, 0, Material.DARK_OAK_FENCE);

        // Barracks (left side)
        b.fillBox(-25, 0, -15, -15, 0, -5, Material.STONE_BRICKS);
        b.fillWalls(-25, 1, -15, -15, 5, -5, Material.DARK_OAK_PLANKS);
        b.hollowBox(-25, 1, -15, -15, 5, -5);
        b.fillBox(-25, 6, -15, -15, 6, -5, Material.DARK_OAK_PLANKS);
        b.setBlock(-20, 1, -15, Material.AIR); b.setBlock(-20, 2, -15, Material.AIR);
        for (int bx = -24; bx <= -16; bx += 4) {
            b.setBlock(bx, 1, -12, Material.RED_BED);
            b.setBlock(bx, 1, -8, Material.RED_BED);
        }

        // Armory (right side)
        b.fillBox(15, 0, -15, 25, 0, -5, Material.STONE_BRICKS);
        b.fillWalls(15, 1, -15, 25, 5, -5, Material.COBBLESTONE);
        b.hollowBox(15, 1, -15, 25, 5, -5);
        b.fillBox(15, 6, -15, 25, 6, -5, Material.DARK_OAK_PLANKS);
        b.setBlock(20, 1, -15, Material.AIR); b.setBlock(20, 2, -15, Material.AIR);
        b.setBlock(16, 1, -13, Material.ANVIL); b.setBlock(17, 1, -13, Material.GRINDSTONE);
        b.setBlock(24, 1, -6, Material.BARREL); b.setBlock(23, 1, -6, Material.BARREL);

        // Command tent (rear)
        b.fillBox(-8, 0, 10, 8, 0, 22, Material.STONE_BRICKS);
        b.fillWalls(-8, 1, 10, 8, 6, 22, Material.DARK_OAK_PLANKS);
        b.hollowBox(-8, 1, 10, 8, 6, 22);
        b.fillBox(-8, 7, 10, 8, 7, 22, Material.DARK_OAK_PLANKS);
        b.setBlock(0, 1, 10, Material.AIR); b.setBlock(0, 2, 10, Material.AIR);
        b.setBlock(0, 1, 16, Material.CRAFTING_TABLE);
        b.setBlock(1, 1, 16, Material.CARTOGRAPHY_TABLE);
        b.banner(-7, 4, 10, Material.WHITE_BANNER);
        b.banner(7, 4, 10, Material.WHITE_BANNER);

        // Prison below
        b.fillBox(-10, -6, -10, 10, -1, 10, Material.STONE_BRICKS);
        b.fillBox(-9, -5, -9, 9, -1, 9, Material.AIR);
        for (int z = -8; z <= 8; z += 4) {
            b.setBlock(-8, -5, z, Material.IRON_BARS); b.setBlock(-8, -4, z, Material.IRON_BARS);
            b.setBlock(8, -5, z, Material.IRON_BARS); b.setBlock(8, -4, z, Material.IRON_BARS);
        }
        b.setBlock(0, -5, 0, Material.SOUL_LANTERN);

        // Banners on walls
        for (int x = -26; x <= 26; x += 8) {
            b.banner(x, 8, -27, Material.WHITE_BANNER);
            b.banner(x, 8, 27, Material.WHITE_BANNER);
        }

        b.placeChest(-22, 1, -10);
        b.placeChest(22, 1, -10);
        b.placeChest(0, 1, 18);
        b.placeChest(0, -5, 0);
        b.setBossSpawn(0, 1, 5);
    }

    // ═══════════════════════════════════════════════════════════════
    //  19. PILLAGER AIRSHIP
    // ═══════════════════════════════════════════════════════════════

    public static void buildPillagerAirship(StructureBuilder b) {
        int baseY = 15;

        // Hull (boat shape)
        for (int z = -12; z <= 12; z++) {
            int halfWidth = z < -8 ? (z + 12) : (z > 8 ? (12 - z) : 4);
            if (halfWidth < 1) halfWidth = 1;
            b.fillBox(-halfWidth, baseY, z, halfWidth, baseY, z, Material.DARK_OAK_PLANKS);
            b.setBlock(-halfWidth, baseY + 1, z, Material.DARK_OAK_PLANKS);
            b.setBlock(halfWidth, baseY + 1, z, Material.DARK_OAK_PLANKS);
            b.setBlock(-halfWidth, baseY + 2, z, Material.DARK_OAK_FENCE);
            b.setBlock(halfWidth, baseY + 2, z, Material.DARK_OAK_FENCE);
        }

        // Deck interior
        b.fillFloor(-3, -10, 3, 10, baseY, Material.OAK_PLANKS);

        // Below-deck cargo hold
        b.fillBox(-3, baseY - 3, -6, 3, baseY - 1, 6, Material.DARK_OAK_PLANKS);
        b.fillBox(-2, baseY - 2, -5, 2, baseY - 1, 5, Material.AIR);
        b.setBlock(0, baseY - 2, 0, Material.BARREL);
        b.setBlock(1, baseY - 2, 2, Material.BARREL);
        b.setBlock(-1, baseY - 2, -3, Material.BARREL);
        b.setBlock(0, baseY - 2, 4, Material.TNT);

        // Captain's cabin (stern)
        b.fillBox(-3, baseY + 1, 6, 3, baseY + 4, 10, Material.DARK_OAK_PLANKS);
        b.fillBox(-2, baseY + 1, 7, 2, baseY + 3, 9, Material.AIR);
        b.setBlock(0, baseY + 1, 6, Material.AIR); b.setBlock(0, baseY + 2, 6, Material.AIR);
        b.setBlock(0, baseY + 1, 8, Material.CRAFTING_TABLE);
        b.setBlock(1, baseY + 1, 9, Material.RED_BED);
        b.setBlock(1, baseY + 3, 8, Material.LANTERN);
        b.setBlock(-2, baseY + 2, 10, Material.GLASS_PANE);
        b.setBlock(2, baseY + 2, 10, Material.GLASS_PANE);

        // Balloon (gray wool envelope)
        int balloonY = baseY + 6;
        for (int y = 0; y < 8; y++) {
            int r = y < 2 ? (2 + y) : (y > 5 ? (9 - y) : 4);
            b.filledCircle(0, balloonY + y, 0, r, Material.GRAY_WOOL);
        }

        // Ropes connecting balloon to hull
        for (int[] rope : new int[][]{{-2, -4}, {2, -4}, {-2, 4}, {2, 4}}) {
            for (int ry = baseY + 3; ry <= balloonY; ry++) {
                b.setBlock(rope[0], ry, rope[1], Material.IRON_BARS);
            }
        }

        // Mast and crow's nest
        b.pillar(0, baseY + 1, 10, -10, Material.OAK_LOG);
        b.fillBox(-2, baseY + 11, -12, 2, baseY + 11, -8, Material.OAK_PLANKS);
        b.circle(0, baseY + 12, -10, 2, Material.OAK_FENCE);

        // Cannons (dispensers)
        b.setDirectional(-3, baseY + 1, -6, Material.DISPENSER, BlockFace.WEST);
        b.setDirectional(3, baseY + 1, -6, Material.DISPENSER, BlockFace.EAST);
        b.setDirectional(-3, baseY + 1, 0, Material.DISPENSER, BlockFace.WEST);
        b.setDirectional(3, baseY + 1, 0, Material.DISPENSER, BlockFace.EAST);

        // Crossbow mounts on deck
        b.setBlock(-3, baseY + 1, -4, Material.DARK_OAK_FENCE);
        b.setBlock(-3, baseY + 2, -4, Material.TRIPWIRE_HOOK);
        b.setBlock(3, baseY + 1, 2, Material.DARK_OAK_FENCE);
        b.setBlock(3, baseY + 2, 2, Material.TRIPWIRE_HOOK);

        // Banners
        b.setBlock(0, baseY + 12, -10, Material.WHITE_BANNER);
        b.setBlock(-4, baseY + 2, 0, Material.WHITE_BANNER);
        b.setBlock(4, baseY + 2, 0, Material.WHITE_BANNER);

        // Rigging (ladders as rope)
        for (int y = baseY + 2; y <= baseY + 10; y++) {
            b.setBlock(0, y, -10, Material.LADDER);
        }

        b.placeChest(0, baseY + 1, 9);
        b.placeChest(0, baseY - 2, 3);
        b.placeChest(1, baseY + 1, -4);
        b.setBossSpawn(0, baseY + 1, 0);
    }

    // ═══════════════════════════════════════════════════════════════
    //  20. THANOS TEMPLE
    // ═══════════════════════════════════════════════════════════════

    public static void buildThanosTemple(StructureBuilder b) {
        // Stepped pyramid base (5 steps)
        for (int step = 0; step < 5; step++) {
            int r = 22 - step * 4;
            int y = step * 4;
            b.fillBox(-r, y, -r, r, y, r, Material.OBSIDIAN);
            b.fillWalls(-r, y + 1, -r, r, y + 3, r, Material.PURPUR_BLOCK);
            b.hollowBox(-r, y + 1, -r, r, y + 3, r);
        }

        // Top platform
        b.fillBox(-6, 20, -6, 6, 20, 6, Material.PURPUR_BLOCK);
        b.fillBox(-5, 21, -5, 5, 21, 5, Material.OBSIDIAN);

        // Purple glass dome
        b.dome(0, 22, 0, 6, Material.PURPLE_STAINED_GLASS);

        // Throne
        b.fillBox(-1, 21, 2, 1, 21, 4, Material.GOLD_BLOCK);
        b.setBlock(0, 22, 3, Material.GOLD_BLOCK); b.setBlock(0, 23, 3, Material.GOLD_BLOCK);
        b.setBlock(-1, 23, 3, Material.GOLD_BLOCK); b.setBlock(1, 23, 3, Material.GOLD_BLOCK);
        b.setBlock(0, 24, 3, Material.GOLD_BLOCK);

        // 6 Infinity Stone pedestals
        Material[] stoneMats = {Material.RED_CONCRETE, Material.BLUE_CONCRETE, Material.YELLOW_CONCRETE,
                Material.ORANGE_CONCRETE, Material.GREEN_CONCRETE, Material.PURPLE_CONCRETE};
        int[][] pedestals = {{-4, 0}, {4, 0}, {0, -4}, {0, 4}, {-3, -3}, {3, 3}};
        for (int i = 0; i < 6; i++) {
            b.setBlock(pedestals[i][0], 21, pedestals[i][1], Material.QUARTZ_PILLAR);
            b.setBlock(pedestals[i][0], 22, pedestals[i][1], Material.QUARTZ_PILLAR);
            b.setBlock(pedestals[i][0], 23, pedestals[i][1], stoneMats[i]);
        }

        // Entrance
        b.fillBox(-3, 1, -22, 3, 6, -22, Material.AIR);
        b.setBlock(-4, 7, -22, Material.PURPUR_PILLAR); b.setBlock(4, 7, -22, Material.PURPUR_PILLAR);

        // Grand staircase
        for (int s = 0; s < 20; s++) {
            b.fillBox(-2, s, -20 + s, 2, s, -20 + s, Material.PURPUR_STAIRS);
        }

        // Imposing columns along the staircase
        for (int s = 0; s < 20; s += 5) {
            b.pillar(-4, s, s + 5, -20 + s, Material.PURPUR_PILLAR);
            b.pillar(4, s, s + 5, -20 + s, Material.PURPUR_PILLAR);
        }

        // Soul lantern lighting
        for (int a = 0; a < 360; a += 45) {
            int lx = (int) (5 * Math.cos(Math.toRadians(a)));
            int lz = (int) (5 * Math.sin(Math.toRadians(a)));
            b.setBlock(lx, 22, lz, Material.SOUL_LANTERN);
        }

        // Underground vault
        b.fillBox(-15, -8, -15, 15, -1, 15, Material.OBSIDIAN);
        b.fillBox(-14, -7, -14, 14, -1, 14, Material.AIR);
        b.fillFloor(-14, -14, 14, 14, -8, Material.CRYING_OBSIDIAN);
        for (int[] c : new int[][]{{-10, -10}, {10, -10}, {-10, 10}, {10, 10}}) {
            b.pillar(c[0], -7, -1, c[1], Material.PURPUR_PILLAR);
            b.setBlock(c[0], -1, c[1], Material.END_ROD);
        }
        b.setBlock(0, -7, 0, Material.BEACON);

        // Decay
        b.decay(-22, 8, -22, 22, 20, 22, 0.08);
        b.scatter(-22, 0, -22, 22, 20, 22, Material.PURPUR_BLOCK, Material.CRACKED_STONE_BRICKS, 0.06);

        b.placeChest(0, 21, 0);
        b.placeChest(-12, -7, 0);
        b.placeChest(12, -7, 0);
        b.setBossSpawn(0, 21, -2);
    }

    // ═══════════════════════════════════════════════════════════════
    //  21. FROST DUNGEON
    // ═══════════════════════════════════════════════════════════════

    public static void buildFrostDungeon(StructureBuilder b) {
        // Main hall
        b.fillWalls(-15, 0, -15, 15, 8, 15, Material.PACKED_ICE);
        b.hollowBox(-15, 1, -15, 15, 8, 15);
        b.fillFloor(-14, -14, 14, 14, 0, Material.BLUE_ICE);

        // Ice pillars
        for (int a = 0; a < 360; a += 45) {
            int px = (int) (10 * Math.cos(Math.toRadians(a)));
            int pz = (int) (10 * Math.sin(Math.toRadians(a)));
            b.pillar(px, 1, 7, pz, Material.PACKED_ICE);
            b.setBlock(px, 8, pz, Material.SEA_LANTERN);
        }

        // Frozen throne room (north alcove)
        b.fillBox(-5, 1, 8, 5, 5, 14, Material.AIR);
        b.fillBox(-5, 0, 8, 5, 0, 14, Material.BLUE_ICE);
        b.fillBox(-1, 1, 12, 1, 3, 12, Material.PACKED_ICE);
        b.setBlock(0, 4, 12, Material.SEA_LANTERN);
        // Throne
        b.setStairs(-1, 1, 13, Material.QUARTZ_STAIRS, BlockFace.EAST, false);
        b.setStairs(1, 1, 13, Material.QUARTZ_STAIRS, BlockFace.WEST, false);
        b.setBlock(0, 1, 13, Material.QUARTZ_BLOCK);
        b.setBlock(0, 2, 14, Material.QUARTZ_BLOCK);

        // Frost crystal formations
        for (int[] crystal : new int[][]{{-8, 5}, {8, -7}, {-6, -10}, {10, 3}}) {
            b.pillar(crystal[0], 1, 3, crystal[1], Material.ICE);
            b.setBlock(crystal[0], 4, crystal[1], Material.BLUE_ICE);
        }

        // Side corridors
        b.fillBox(-15, 1, -2, -20, 4, 2, Material.AIR);
        b.fillWalls(-20, 0, -5, -15, 5, 5, Material.PACKED_ICE);
        b.fillBox(15, 1, -2, 20, 4, 2, Material.AIR);
        b.fillWalls(15, 0, -5, 20, 5, 5, Material.PACKED_ICE);

        // Ice cell blocks in corridors
        b.setBlock(-18, 1, -4, Material.IRON_BARS); b.setBlock(-18, 2, -4, Material.IRON_BARS);
        b.setBlock(-18, 1, 4, Material.IRON_BARS); b.setBlock(-18, 2, 4, Material.IRON_BARS);
        b.setBlock(18, 1, -4, Material.IRON_BARS); b.setBlock(18, 2, -4, Material.IRON_BARS);
        b.setBlock(18, 1, 4, Material.IRON_BARS); b.setBlock(18, 2, 4, Material.IRON_BARS);

        // Powder snow traps
        b.scatter(-14, 0, -14, 14, 0, 14, Material.BLUE_ICE, Material.POWDER_SNOW, 0.08);

        // Blue ice accent details
        b.setBlock(-14, 4, -14, Material.BLUE_ICE);
        b.setBlock(14, 4, 14, Material.BLUE_ICE);
        b.setBlock(-14, 4, 14, Material.BLUE_ICE);
        b.setBlock(14, 4, -14, Material.BLUE_ICE);

        b.placeChest(0, 1, 10);
        b.placeChest(-18, 1, 0);
        b.placeChest(18, 1, 0);
        b.setBossSpawn(0, 1, 11);
    }

    // ═══════════════════════════════════════════════════════════════
    //  22. BANDIT HIDEOUT
    // ═══════════════════════════════════════════════════════════════

    public static void buildBanditHideout(StructureBuilder b) {
        // Canyon walls
        b.fillWalls(-12, 0, -10, 12, 8, 10, Material.SANDSTONE);
        b.hollowBox(-12, 1, -10, 12, 8, 10);
        b.fillFloor(-11, -9, 11, 9, 0, Material.SAND);

        // Cave entrance (north)
        b.fillBox(-3, 1, -12, 3, 5, -10, Material.AIR);

        // Wooden platforms inside canyon walls (multiple levels)
        // Platform 1 (west, low)
        b.fillBox(-11, 3, -5, -8, 3, -2, Material.OAK_PLANKS);
        b.circle(-10, 4, -3, 1, Material.OAK_FENCE);
        b.setBlock(-10, 4, -4, Material.LANTERN);

        // Platform 2 (east, mid)
        b.fillBox(8, 5, 1, 11, 5, 4, Material.OAK_PLANKS);
        b.circle(9, 6, 2, 1, Material.OAK_FENCE);
        b.setBlock(9, 6, 3, Material.LANTERN);

        // Platform 3 (west, high)
        b.fillBox(-11, 6, 3, -8, 6, 6, Material.OAK_PLANKS);
        b.setBlock(-10, 7, 5, Material.OAK_FENCE);

        // Rope bridges (ladders between platforms)
        for (int y = 1; y <= 3; y++) b.setBlock(-10, y, -4, Material.LADDER);
        for (int y = 4; y <= 5; y++) b.setBlock(8, y, 2, Material.LADDER);
        for (int y = 4; y <= 6; y++) b.setBlock(-9, y, 4, Material.LADDER);

        // Rope bridge (string/planks) between platforms
        for (int x = -7; x <= 7; x++) {
            b.setBlock(x, 5, 2, Material.OAK_PLANKS);
            b.setBlock(x, 6, 3, Material.OAK_FENCE);
            b.setBlock(x, 6, 1, Material.OAK_FENCE);
        }

        // Stolen loot piles
        b.fillBox(-8, 1, -7, -6, 2, -5, Material.HAY_BLOCK);
        b.setBlock(-7, 3, -6, Material.GOLD_BLOCK);
        b.fillBox(6, 1, 5, 8, 1, 7, Material.BARREL);

        // Central campfire
        b.setBlock(0, 1, 0, Material.CAMPFIRE);
        b.setBlock(-1, 1, 1, Material.STRIPPED_OAK_LOG);
        b.setBlock(1, 1, -1, Material.STRIPPED_OAK_LOG);

        // Lookout post (on top of east wall)
        b.fillBox(10, 8, -2, 12, 8, 2, Material.OAK_PLANKS);
        b.circle(11, 9, 0, 1, Material.OAK_FENCE);
        b.setBlock(11, 9, 0, Material.LANTERN);
        for (int y = 1; y <= 8; y++) b.setBlock(11, y, -9, Material.LADDER);

        // Escape tunnel (south)
        b.fillBox(-1, 1, 10, 1, 3, 14, Material.AIR);
        b.fillWalls(-2, 0, 10, 2, 3, 14, Material.CUT_SANDSTONE);

        // Weapon rack area
        b.setBlock(-5, 1, 0, Material.GRINDSTONE);
        b.setBlock(-5, 1, 1, Material.SMITHING_TABLE);

        b.placeChest(-7, 1, -5);
        b.placeChest(7, 1, 6);
        b.placeChest(0, 1, 12);
        b.setBossSpawn(0, 1, 3);
    }

    // ═══════════════════════════════════════════════════════════════
    //  23. SUNKEN RUINS
    // ═══════════════════════════════════════════════════════════════

    public static void buildSunkenRuins(StructureBuilder b) {
        // Base platform
        b.filledCircle(0, 0, 0, 14, Material.PRISMARINE_BRICKS);

        // Rising walls (multiple tiers, partially decayed)
        b.filledCircle(0, 1, 0, 12, Material.PRISMARINE);
        b.circle(0, 2, 0, 12, Material.PRISMARINE);
        b.circle(0, 3, 0, 12, Material.PRISMARINE);
        b.circle(0, 4, 0, 10, Material.PRISMARINE_BRICKS);
        b.circle(0, 5, 0, 10, Material.PRISMARINE_BRICKS);

        // Pillars
        for (int a = 0; a < 360; a += 40) {
            int px = (int) (10 * Math.cos(Math.toRadians(a)));
            int pz = (int) (10 * Math.sin(Math.toRadians(a)));
            int h = 4 + b.getRandom().nextInt(5);
            b.pillar(px, 1, h, pz, Material.PRISMARINE_WALL);
        }

        // Central altar
        b.fillBox(-2, 1, -2, 2, 1, 2, Material.DARK_PRISMARINE);
        b.setBlock(0, 2, 0, Material.SEA_LANTERN);
        b.setBlock(-2, 2, -2, Material.SEA_LANTERN);
        b.setBlock(2, 2, 2, Material.SEA_LANTERN);

        // Coral decorations around perimeter
        Material[] corals = {Material.BRAIN_CORAL_BLOCK, Material.TUBE_CORAL_BLOCK,
                Material.FIRE_CORAL_BLOCK, Material.HORN_CORAL_BLOCK};
        for (int a = 0; a < 360; a += 20) {
            int cx = (int) (13 * Math.cos(Math.toRadians(a)));
            int cz = (int) (13 * Math.sin(Math.toRadians(a)));
            b.setBlockIfAir(cx, 1, cz, corals[a / 20 % 4]);
        }

        // Air pocket rooms (underwater chambers)
        b.fillBox(-6, -3, -6, -2, 0, -2, Material.PRISMARINE_BRICKS);
        b.fillBox(-5, -2, -5, -3, -1, -3, Material.AIR);
        b.fillBox(2, -3, 2, 6, 0, 6, Material.PRISMARINE_BRICKS);
        b.fillBox(3, -2, 3, 5, -1, 5, Material.AIR);

        // Sea lantern lighting throughout
        b.setBlock(-4, -1, -4, Material.SEA_LANTERN);
        b.setBlock(4, -1, 4, Material.SEA_LANTERN);

        // Decay and ruin effects
        b.scatter(-14, 0, -14, 14, 6, 14, Material.PRISMARINE_BRICKS, Material.PRISMARINE, 0.15);
        b.decay(-14, 3, -14, 14, 8, 14, 0.2);

        // Kelp and seagrass ambiance
        b.scatter(-14, 1, -14, 14, 2, 14, Material.AIR, Material.SEAGRASS, 0.05);

        b.placeChest(0, 1, 5);
        b.placeChest(-4, -2, -4);
        b.placeChest(4, -2, 4);
        b.setBossSpawn(0, 1, 0);
    }

    // ═══════════════════════════════════════════════════════════════
    //  24. CURSED GRAVEYARD
    // ═══════════════════════════════════════════════════════════════

    public static void buildCursedGraveyard(StructureBuilder b) {
        // Ground
        b.filledCircle(0, 0, 0, 12, Material.PODZOL);
        b.scatter(-12, 0, -12, 12, 0, 12, Material.PODZOL, Material.SOUL_SOIL, 0.3);
        b.scatter(-12, 0, -12, 12, 0, 12, Material.PODZOL, Material.COARSE_DIRT, 0.15);

        // Iron fence perimeter
        b.circle(0, 1, 0, 12, Material.IRON_BARS);
        // Gate (south)
        b.setBlock(0, 1, -12, Material.AIR); b.setBlock(1, 1, -12, Material.AIR);
        b.pillar(-1, 1, 3, -12, Material.STONE_BRICK_WALL);
        b.pillar(2, 1, 3, -12, Material.STONE_BRICK_WALL);

        // 20+ tombstones (stone buttons on stone)
        int[][] gravePositions = {
            {-8, 3}, {-5, 5}, {-2, 7}, {1, 5}, {4, 3}, {7, 1},
            {-7, -1}, {-4, -3}, {-1, -6}, {2, -4}, {5, -2}, {8, 0},
            {-6, 8}, {-3, 9}, {0, 10}, {3, 8}, {6, 6},
            {-9, -4}, {-6, -7}, {3, -8}, {6, -5}
        };
        for (int[] g : gravePositions) {
            b.setBlock(g[0], 1, g[1], Material.STONE_BRICK_WALL);
            b.setBlock(g[0], 2, g[1], Material.STONE_BRICK_WALL);
            b.setBlock(g[0], 3, g[1], Material.STONE_BUTTON);
        }

        // Central mausoleum
        b.fillBox(-3, 0, -3, 3, 0, 3, Material.DEEPSLATE_BRICKS);
        b.fillWalls(-3, 1, -3, 3, 5, 3, Material.DEEPSLATE_BRICKS);
        b.hollowBox(-3, 1, -3, 3, 5, 3);
        b.fillBox(-3, 6, -3, 3, 6, 3, Material.DEEPSLATE_BRICK_SLAB);
        b.pyramidRoof(-4, -4, 4, 4, 6, Material.DEEPSLATE_TILES);
        // Door
        b.setBlock(0, 1, -3, Material.AIR); b.setBlock(0, 2, -3, Material.AIR);
        // Coffins inside
        b.setBlock(-1, 1, 1, Material.DARK_OAK_PLANKS);
        b.setBlock(-1, 1, 0, Material.DARK_OAK_PLANKS);
        b.setBlock(1, 1, 1, Material.DARK_OAK_PLANKS);
        b.setBlock(1, 1, 0, Material.DARK_OAK_PLANKS);
        b.setBlock(0, 4, 0, Material.SOUL_LANTERN);

        // Dead trees
        b.pillar(-9, 1, 6, -8, Material.DARK_OAK_LOG);
        b.setBlock(-9, 7, -8, Material.DARK_OAK_LOG);
        b.setBlock(-10, 6, -8, Material.DARK_OAK_LOG);
        b.setBlock(-8, 6, -8, Material.DARK_OAK_LOG);
        b.pillar(8, 1, 5, 7, Material.DARK_OAK_LOG);
        b.setBlock(9, 6, 7, Material.DARK_OAK_LOG);
        b.setBlock(7, 5, 7, Material.DARK_OAK_LOG);

        // Soul lanterns throughout
        b.setBlock(-5, 1, 0, Material.SOUL_LANTERN);
        b.setBlock(5, 1, 0, Material.SOUL_LANTERN);
        b.setBlock(0, 1, 6, Material.SOUL_LANTERN);
        b.setBlock(0, 1, -6, Material.SOUL_LANTERN);

        // Fog (soul fire)
        b.setBlock(-3, 0, -8, Material.SOUL_CAMPFIRE);
        b.setBlock(4, 0, 6, Material.SOUL_CAMPFIRE);
        b.setBlock(-7, 0, 5, Material.SOUL_CAMPFIRE);

        // Open grave (empty hole)
        b.fillBox(6, -2, -8, 8, 0, -6, Material.AIR);
        b.fillBox(5, -2, -9, 5, 0, -5, Material.COARSE_DIRT);
        b.fillBox(9, -2, -9, 9, 0, -5, Material.COARSE_DIRT);

        // Cobwebs
        b.scatter(-12, 1, -12, 12, 3, 12, Material.AIR, Material.COBWEB, 0.04);

        b.placeChest(0, 1, 0);
        b.placeChest(-5, 0, 4);
        b.setBossSpawn(0, 1, -5);
    }

    // ═══════════════════════════════════════════════════════════════
    //  25. SKY ALTAR
    // ═══════════════════════════════════════════════════════════════

    public static void buildSkyAltar(StructureBuilder b) {
        // Main platform (quartz)
        b.filledCircle(0, 0, 0, 9, Material.QUARTZ_BLOCK);
        b.filledCircle(0, -1, 0, 7, Material.QUARTZ_BLOCK);
        b.filledCircle(0, -2, 0, 5, Material.QUARTZ_BLOCK);
        b.filledCircle(0, -3, 0, 3, Material.QUARTZ_BLOCK);

        // Golden pillar supports (4 cardinal)
        for (int[] p : new int[][]{{-8, 0}, {8, 0}, {0, -8}, {0, 8}}) {
            b.pillar(p[0], 1, 10, p[1], Material.QUARTZ_PILLAR);
            b.setBlock(p[0], 11, p[1], Material.END_ROD);
        }

        // Connecting arches between pillars
        b.fillBox(-1, 9, -8, 1, 9, 8, Material.QUARTZ_BLOCK);
        b.fillBox(-8, 9, -1, 8, 9, 1, Material.QUARTZ_BLOCK);

        // Central altar
        b.fillBox(-1, 1, -1, 1, 1, 1, Material.GOLD_BLOCK);
        b.setBlock(0, 2, 0, Material.BEACON);
        b.setBlock(0, 3, 0, Material.END_ROD);

        // Enchanting circle (end rod frame)
        for (int a = 0; a < 360; a += 30) {
            int ex = (int) Math.round(4 * Math.cos(Math.toRadians(a)));
            int ez = (int) Math.round(4 * Math.sin(Math.toRadians(a)));
            b.setBlock(ex, 1, ez, Material.END_ROD);
        }

        // Stained glass floor sections
        b.setBlock(-5, 0, -5, Material.YELLOW_STAINED_GLASS);
        b.setBlock(5, 0, -5, Material.LIGHT_BLUE_STAINED_GLASS);
        b.setBlock(-5, 0, 5, Material.MAGENTA_STAINED_GLASS);
        b.setBlock(5, 0, 5, Material.ORANGE_STAINED_GLASS);
        b.setBlock(-3, 0, 0, Material.CYAN_STAINED_GLASS);
        b.setBlock(3, 0, 0, Material.PINK_STAINED_GLASS);
        b.setBlock(0, 0, -3, Material.LIME_STAINED_GLASS);
        b.setBlock(0, 0, 3, Material.PURPLE_STAINED_GLASS);

        // Amethyst cluster decorations
        b.setBlock(-6, 2, -6, Material.AMETHYST_CLUSTER);
        b.setBlock(6, 2, 6, Material.AMETHYST_CLUSTER);
        b.setBlock(-6, 2, 6, Material.AMETHYST_CLUSTER);
        b.setBlock(6, 2, -6, Material.AMETHYST_CLUSTER);

        // Candles for ambient light
        b.setBlock(-2, 2, -2, Material.WHITE_CANDLE);
        b.setBlock(2, 2, -2, Material.WHITE_CANDLE);
        b.setBlock(-2, 2, 2, Material.WHITE_CANDLE);
        b.setBlock(2, 2, 2, Material.WHITE_CANDLE);

        b.placeChest(3, 1, 3);
        b.placeChest(-3, 1, -3);
        b.setBossSpawn(0, 1, 5);
    }

    // ═══════════════════════════════════════════════════════════════
    //  26. FORGE (Dwarven Mountain Forge)
    // ═══════════════════════════════════════════════════════════════

    public static void buildForge(StructureBuilder b) {
        // Mountain exterior (carved into hillside)
        for (int y = 0; y < 20; y++) {
            int r = 25 - (y * 25 / 20);
            if (r < 3) r = 3;
            b.filledCircle(0, y, 0, r, Material.STONE);
        }
        b.scatter(-25, 0, -25, 25, 20, 25, Material.STONE, Material.COBBLESTONE, 0.15);
        b.scatter(-25, 0, -25, 25, 20, 25, Material.STONE, Material.ANDESITE, 0.1);

        // Grand entrance (carved archway)
        b.fillBox(-4, 1, -26, 4, 8, -20, Material.AIR);
        b.gothicArch(0, 1, -25, 8, 9, Material.DEEPSLATE_BRICKS);
        // Entrance pillars
        b.pillar(-5, 1, 8, -25, Material.POLISHED_DEEPSLATE);
        b.pillar(5, 1, 8, -25, Material.POLISHED_DEEPSLATE);

        // Main forge hall (hollowed interior)
        b.fillBox(-18, 1, -20, 18, 14, 15, Material.AIR);
        b.fillFloor(-18, -20, 18, 15, 0, Material.DEEPSLATE_TILES);

        // Huge anvil room (center)
        b.fillBox(-3, 1, -5, 3, 1, 5, Material.IRON_BLOCK);
        b.setBlock(-1, 2, 0, Material.ANVIL);
        b.setBlock(0, 2, 0, Material.ANVIL);
        b.setBlock(1, 2, 0, Material.ANVIL);
        b.setBlock(0, 2, -2, Material.ANVIL);
        b.setBlock(0, 2, 2, Material.ANVIL);
        // Decorative iron block structure
        b.pillar(-3, 2, 4, 0, Material.IRON_BLOCK);
        b.pillar(3, 2, 4, 0, Material.IRON_BLOCK);

        // Lava channels (running along the sides)
        for (int z = -15; z <= 10; z++) {
            b.setBlock(-15, 0, z, Material.LAVA);
            b.setBlock(15, 0, z, Material.LAVA);
        }
        // Lava falls
        b.setBlock(-15, 8, -10, Material.LAVA);
        b.setBlock(15, 8, 5, Material.LAVA);

        // Bellows (pistons)
        b.setDirectional(-6, 1, 0, Material.PISTON, BlockFace.EAST);
        b.setDirectional(6, 1, 0, Material.PISTON, BlockFace.WEST);
        b.setDirectional(-6, 2, 0, Material.PISTON, BlockFace.EAST);
        b.setDirectional(6, 2, 0, Material.PISTON, BlockFace.WEST);

        // Smithing tables and workstations
        b.setBlock(-10, 1, -8, Material.SMITHING_TABLE);
        b.setBlock(-9, 1, -8, Material.SMITHING_TABLE);
        b.setBlock(-10, 1, -6, Material.GRINDSTONE);
        b.setBlock(-9, 1, -6, Material.BLAST_FURNACE);
        b.setBlock(10, 1, -8, Material.SMITHING_TABLE);
        b.setBlock(9, 1, -8, Material.CRAFTING_TABLE);
        b.setBlock(10, 1, -6, Material.CARTOGRAPHY_TABLE);

        // Weapon racks (armor stands)
        for (int z = 5; z <= 12; z += 2) {
            b.setBlock(-16, 1, z, Material.LIGHTNING_ROD);
            b.setBlock(16, 1, z, Material.LIGHTNING_ROD);
        }

        // Support pillars
        for (int[] p : new int[][]{{-12, -12}, {12, -12}, {-12, 8}, {12, 8}}) {
            b.pillar(p[0], 1, 14, p[1], Material.DEEPSLATE_BRICK_WALL);
            b.setBlock(p[0], 14, p[1], Material.LANTERN);
        }

        // Chimney (vertical shaft to surface)
        b.fillBox(-2, 14, -2, 2, 25, 2, Material.AIR);
        b.fillWalls(-3, 14, -3, 3, 25, 3, Material.DEEPSLATE_BRICKS);
        b.setBlock(0, 25, 0, Material.CAMPFIRE);

        // Chandeliers (chains + lanterns)
        b.chandelier(-8, 14, -5, 4);
        b.chandelier(8, 14, -5, 4);
        b.chandelier(0, 14, 5, 5);

        // Storage area (barrels and chests)
        for (int x = -16; x <= -12; x++) {
            b.setBlock(x, 1, -16, Material.BARREL);
            b.setBlock(x, 2, -16, Material.BARREL);
        }

        // Magma block floor accents
        b.scatter(-18, 0, -20, 18, 0, 15, Material.DEEPSLATE_TILES, Material.MAGMA_BLOCK, 0.05);

        b.placeChest(0, 1, -10);
        b.placeChest(-14, 1, -14);
        b.placeChest(14, 1, 10);
        b.setBossSpawn(0, 2, 3);
    }

    // ═══════════════════════════════════════════════════════════════
    //  27. OGRIN HUT
    // ═══════════════════════════════════════════════════════════════

    public static void buildOgrinHut(StructureBuilder b) {
        // Swamp ground
        b.filledCircle(0, 0, 0, 10, Material.MUD);
        b.scatter(-10, 0, -10, 10, 0, 10, Material.MUD, Material.DIRT, 0.2);

        // Oversized hut (big for an ogre!)
        b.fillBox(-6, 0, -5, 6, 0, 5, Material.OAK_PLANKS);
        b.fillWalls(-6, 1, -5, 6, 7, 5, Material.SPRUCE_LOG);
        b.hollowBox(-6, 1, -5, 6, 7, 5);

        // Mud floor accents
        b.scatter(-5, 0, -4, 5, 0, 4, Material.OAK_PLANKS, Material.MUD, 0.3);

        // Oversized door (3 wide, 4 tall)
        b.fillBox(-1, 1, -5, 1, 4, -5, Material.AIR);

        // Messy conical roof
        b.pyramidRoof(-7, -6, 7, 6, 8, Material.SPRUCE_PLANKS);
        b.scatter(-7, 8, -6, 7, 12, 6, Material.SPRUCE_PLANKS, Material.MOSS_BLOCK, 0.15);

        // Windows (large, with shutters)
        b.setBlock(-6, 4, 0, Material.GLASS_PANE);
        b.setBlock(-6, 5, 0, Material.GLASS_PANE);
        b.setBlock(6, 4, 0, Material.GLASS_PANE);
        b.setBlock(6, 5, 0, Material.GLASS_PANE);

        // Interior: cauldron "soup"
        b.setBlock(0, 1, 2, Material.CAULDRON);
        b.setBlock(0, 0, 2, Material.CAMPFIRE);
        b.setBlock(1, 1, 2, Material.BARREL);  // Ingredients

        // Oversized bed (wool on planks)
        b.fillBox(-4, 1, 2, -2, 1, 4, Material.OAK_PLANKS);
        b.fillBox(-4, 2, 2, -2, 2, 4, Material.GREEN_WOOL);

        // Table with food
        b.setBlock(3, 1, 0, Material.OAK_FENCE);
        b.setBlock(3, 2, 0, Material.OAK_PRESSURE_PLATE);
        b.setBlock(4, 1, 0, Material.OAK_FENCE);
        b.setBlock(4, 2, 0, Material.OAK_PRESSURE_PLATE);
        b.setBlock(3, 1, -3, Material.BARREL);

        // "Onion" garden (potatoes and carrots)
        b.fillBox(8, 0, -5, 12, 0, -1, Material.FARMLAND);
        b.setBlock(9, 1, -4, Material.POTATOES);
        b.setBlock(10, 1, -4, Material.POTATOES);
        b.setBlock(11, 1, -4, Material.POTATOES);
        b.setBlock(9, 1, -2, Material.CARROTS);
        b.setBlock(10, 1, -2, Material.CARROTS);
        b.setBlock(11, 1, -2, Material.CARVED_PUMPKIN);

        // Fishing dock
        b.fillBox(-8, 0, 6, -4, 0, 10, Material.OAK_PLANKS);
        b.pillar(-8, -2, 0, 6, Material.OAK_LOG);
        b.pillar(-4, -2, 0, 6, Material.OAK_LOG);
        b.pillar(-8, -2, 0, 10, Material.OAK_LOG);
        b.pillar(-4, -2, 0, 10, Material.OAK_LOG);
        b.setBlock(-6, 1, 10, Material.OAK_FENCE);
        b.setBlock(-6, 2, 10, Material.OAK_FENCE);  // "Fishing rod holder"

        // Funny details: pile of bones (the ogre's meals)
        b.setBlock(4, 1, 3, Material.BONE_BLOCK);
        b.setBlock(5, 1, 3, Material.BONE_BLOCK);
        b.setBlock(4, 2, 3, Material.BONE_BLOCK);

        // Lanterns
        b.setBlock(0, 6, 0, Material.LANTERN);
        b.setBlock(-5, 1, -4, Material.LANTERN);

        // Mushrooms around the hut
        b.setBlock(7, 1, 3, Material.RED_MUSHROOM);
        b.setBlock(-7, 1, -3, Material.BROWN_MUSHROOM);
        b.setBlock(8, 1, 5, Material.RED_MUSHROOM);

        b.placeChest(4, 1, -2);
        b.setBossSpawn(0, 1, -7);
    }

    // ═══════════════════════════════════════════════════════════════
    //  28. FAIRY GLADE
    // ═══════════════════════════════════════════════════════════════

    public static void buildFairyGlade(StructureBuilder b) {
        // Cherry blossom clearing
        b.filledCircle(0, 0, 0, 14, Material.GRASS_BLOCK);
        b.scatter(-14, 0, -14, 14, 0, 14, Material.GRASS_BLOCK, Material.MOSS_BLOCK, 0.2);

        // Cherry blossom trees (4 around the glade)
        int[][] treePos = {{-8, -8}, {8, -8}, {-8, 8}, {8, 8}};
        for (int[] tp : treePos) {
            b.pillar(tp[0], 1, 7, tp[1], Material.CHERRY_LOG);
            for (int y = 5; y <= 9; y++) {
                int r = y < 7 ? 4 : (y < 9 ? 3 : 1);
                b.filledCircle(tp[0], y, tp[1], r, Material.CHERRY_LEAVES);
            }
        }

        // Enchanted spring (center)
        b.filledCircle(0, -1, 0, 3, Material.STONE);
        b.filledCircle(0, 0, 0, 2, Material.WATER);
        b.setBlock(0, 0, 0, Material.SOUL_LANTERN);  // Underwater glow
        b.circle(0, 0, 0, 3, Material.MOSSY_STONE_BRICKS);

        // Flower arches (2 crossing paths)
        for (int x = -6; x <= 6; x++) {
            b.setBlock(x, 3, -1, Material.CHERRY_LEAVES);
            b.setBlock(x, 3, 1, Material.CHERRY_LEAVES);
            b.setBlock(x, 4, 0, Material.CHERRY_LEAVES);
        }
        for (int z = -6; z <= 6; z++) {
            b.setBlock(-1, 3, z, Material.CHERRY_LEAVES);
            b.setBlock(1, 3, z, Material.CHERRY_LEAVES);
            b.setBlock(0, 4, z, Material.CHERRY_LEAVES);
        }

        // Crystal formations (amethyst)
        b.setBlock(-5, 1, 0, Material.AMETHYST_BLOCK);
        b.setBlock(-5, 2, 0, Material.AMETHYST_CLUSTER);
        b.setBlock(5, 1, 0, Material.AMETHYST_BLOCK);
        b.setBlock(5, 2, 0, Material.AMETHYST_CLUSTER);
        b.setBlock(0, 1, -5, Material.BUDDING_AMETHYST);
        b.setBlock(0, 2, -5, Material.AMETHYST_CLUSTER);

        // Unicorn stable area (small fenced area with hay)
        b.fillBox(5, 0, 5, 10, 0, 10, Material.GRASS_BLOCK);
        b.fillBox(5, 1, 5, 5, 2, 10, Material.OAK_FENCE);
        b.fillBox(10, 1, 5, 10, 2, 10, Material.OAK_FENCE);
        b.fillBox(5, 1, 10, 10, 2, 10, Material.OAK_FENCE);
        b.fillBox(5, 1, 5, 10, 2, 5, Material.OAK_FENCE);
        b.setBlock(7, 1, 5, Material.AIR); b.setBlock(7, 2, 5, Material.AIR);  // Gate
        b.setBlock(7, 1, 7, Material.HAY_BLOCK);
        b.setBlock(8, 1, 8, Material.WATER);

        // Butterflies (end rods as particle emitters)
        b.setBlock(-3, 3, -3, Material.END_ROD);
        b.setBlock(3, 3, 3, Material.END_ROD);
        b.setBlock(-4, 4, 4, Material.END_ROD);
        b.setBlock(4, 4, -4, Material.END_ROD);

        // Flowers everywhere
        Material[] flowers = {Material.PINK_TULIP, Material.ALLIUM, Material.LILY_OF_THE_VALLEY,
                Material.CORNFLOWER, Material.AZURE_BLUET, Material.OXEYE_DAISY};
        for (int a = 0; a < 360; a += 15) {
            int fx = (int) (11 * Math.cos(Math.toRadians(a)));
            int fz = (int) (11 * Math.sin(Math.toRadians(a)));
            b.setBlockIfAir(fx, 1, fz, flowers[a / 15 % flowers.length]);
        }

        // Lanterns along paths
        for (int i = -5; i <= 5; i += 5) {
            b.setBlock(i, 1, 0, Material.OAK_FENCE);
            b.setBlock(i, 2, 0, Material.LANTERN);
            b.setBlock(0, 1, i, Material.OAK_FENCE);
            b.setBlock(0, 2, i, Material.LANTERN);
        }

        // Path to spring
        for (int z = -12; z <= -4; z++) {
            b.setBlock(0, 0, z, Material.GRAVEL);
            b.setBlock(1, 0, z, Material.GRAVEL);
        }

        b.placeChest(0, 1, 3);
        b.setBossSpawn(0, 1, -8);
    }

    // ═══════════════════════════════════════════════════════════════
    //  29. JAPANESE TEMPLE
    // ═══════════════════════════════════════════════════════════════

    public static void buildJapaneseTemple(StructureBuilder b) {
        // Stone foundation platform
        b.fillBox(-12, 0, -12, 12, 0, 12, Material.SMOOTH_STONE);
        b.fillBox(-13, -1, -13, 13, -1, 13, Material.STONE);

        // Torii gate (entrance, red nether brick)
        b.pillar(-3, 1, 6, -14, Material.RED_NETHER_BRICKS);
        b.pillar(3, 1, 6, -14, Material.RED_NETHER_BRICKS);
        b.fillBox(-4, 7, -14, 4, 7, -14, Material.RED_NETHER_BRICKS);
        b.fillBox(-5, 8, -14, 5, 8, -14, Material.RED_NETHER_BRICK_SLAB);
        // Second crossbar
        b.fillBox(-3, 5, -14, 3, 5, -14, Material.RED_NETHER_BRICKS);

        // Path from torii to temple
        for (int z = -13; z <= -4; z++) {
            b.setBlock(0, 0, z, Material.GRAVEL);
            b.setBlock(-1, 0, z, Material.GRAVEL);
            b.setBlock(1, 0, z, Material.GRAVEL);
        }

        // Main hall
        b.fillBox(-10, 0, -3, 10, 0, 10, Material.OAK_PLANKS);
        b.fillWalls(-10, 1, -3, 10, 6, 10, Material.OAK_PLANKS);
        b.hollowBox(-10, 1, -3, 10, 6, 10);

        // Paper walls (white wool replacing some planks)
        for (int x = -9; x <= 9; x += 2) {
            b.setBlock(x, 2, -3, Material.WHITE_WOOL);
            b.setBlock(x, 3, -3, Material.WHITE_WOOL);
            b.setBlock(x, 4, -3, Material.WHITE_WOOL);
        }
        for (int z = -2; z <= 9; z += 2) {
            b.setBlock(-10, 3, z, Material.WHITE_STAINED_GLASS_PANE);
            b.setBlock(10, 3, z, Material.WHITE_STAINED_GLASS_PANE);
        }

        // Curved roofline (stairs on each side)
        for (int layer = 0; layer < 4; layer++) {
            int y = 7 + layer;
            for (int z = -4 + layer; z <= 11 - layer; z++) {
                b.setStairs(-11 + layer, y, z, Material.DARK_OAK_STAIRS, BlockFace.EAST, false);
                b.setStairs(11 - layer, y, z, Material.DARK_OAK_STAIRS, BlockFace.WEST, false);
            }
        }
        // Ridge
        for (int z = -2; z <= 9; z++) {
            b.setSlab(0, 11, z, Material.DARK_OAK_SLAB, false);
        }
        // Upturned eaves
        b.setStairs(-12, 7, -4, Material.DARK_OAK_STAIRS, BlockFace.SOUTH, true);
        b.setStairs(12, 7, -4, Material.DARK_OAK_STAIRS, BlockFace.SOUTH, true);
        b.setStairs(-12, 7, 11, Material.DARK_OAK_STAIRS, BlockFace.NORTH, true);
        b.setStairs(12, 7, 11, Material.DARK_OAK_STAIRS, BlockFace.NORTH, true);

        // Entrance (wide sliding door)
        b.fillBox(-2, 1, -3, 2, 4, -3, Material.AIR);

        // Interior: altar
        b.setBlock(0, 1, 8, Material.GOLD_BLOCK);
        b.setBlock(0, 2, 8, Material.CANDLE);
        b.setBlock(-2, 1, 8, Material.FLOWER_POT);
        b.setBlock(2, 1, 8, Material.FLOWER_POT);

        // Tatami floor (smooth sandstone as tatami)
        b.fillFloor(-9, -2, 9, 9, 0, Material.SMOOTH_SANDSTONE);

        // Lanterns (paper lantern style)
        b.setBlock(-8, 4, 0, Material.LANTERN);
        b.setBlock(8, 4, 0, Material.LANTERN);
        b.setBlock(-8, 4, 6, Material.LANTERN);
        b.setBlock(8, 4, 6, Material.LANTERN);
        b.setBlock(0, 5, 3, Material.LANTERN);

        // Koi pond (east side)
        b.fillBox(6, -1, -10, 11, -1, -5, Material.STONE);
        b.fillBox(7, 0, -9, 10, 0, -6, Material.WATER);
        b.setBlock(8, 0, -8, Material.LILY_PAD);
        b.setBlock(9, 0, -7, Material.LILY_PAD);

        // Zen garden (west side) — gravel + dead bushes
        b.fillBox(-11, 0, -10, -6, 0, -5, Material.GRAVEL);
        b.setBlock(-9, 1, -8, Material.DEAD_BUSH);
        b.setBlock(-8, 1, -6, Material.DEAD_BUSH);
        b.setBlock(-7, 0, -7, Material.SMOOTH_STONE);  // Stone in zen garden

        // Cherry tree next to temple
        b.pillar(-8, 1, 6, 8, Material.CHERRY_LOG);
        for (int y = 4; y <= 8; y++) b.filledCircle(-8, y, 8, 3, Material.CHERRY_LEAVES);

        // Exterior lanterns on posts
        b.pillar(-4, 1, 3, -12, Material.OAK_FENCE);
        b.setBlock(-4, 3, -12, Material.LANTERN);
        b.pillar(4, 1, 3, -12, Material.OAK_FENCE);
        b.setBlock(4, 3, -12, Material.LANTERN);

        b.placeChest(0, 1, 5);
    }

    // ═══════════════════════════════════════════════════════════════
    //  30. MONSTER ISLAND
    // ═══════════════════════════════════════════════════════════════

    public static void buildMonsterIsland(StructureBuilder b) {
        // Main island (200x200 base, irregularly shaped)
        for (int y = -5; y <= 0; y++) {
            int r = 80 + y * 4;
            b.filledCircle(0, y, 0, r, Material.STONE);
        }
        b.filledCircle(0, 0, 0, 85, Material.GRASS_BLOCK);
        b.scatter(-85, 0, -85, 85, 0, 85, Material.GRASS_BLOCK, Material.SAND, 0.05);

        // Beach ring
        for (int a = 0; a < 360; a += 2) {
            int bx = (int) (82 * Math.cos(Math.toRadians(a)));
            int bz = (int) (82 * Math.sin(Math.toRadians(a)));
            b.setBlock(bx, 0, bz, Material.SAND);
            b.setBlockIfAir(bx, 1, bz, Material.SAND);
        }

        // Central arena (50 radius, large open space)
        b.filledCircle(0, 0, 0, 50, Material.STONE_BRICKS);
        b.filledCircle(0, 0, 0, 48, Material.SMOOTH_STONE);

        // Arena pillars (for cover during boss fight)
        for (int a = 0; a < 360; a += 30) {
            int px = (int) (35 * Math.cos(Math.toRadians(a)));
            int pz = (int) (35 * Math.sin(Math.toRadians(a)));
            b.pillar(px, 1, 8, pz, Material.STONE_BRICK_WALL);
            b.setBlock(px, 9, pz, Material.STONE_BRICK_SLAB);
        }
        // Inner ring of pillars
        for (int a = 15; a < 360; a += 30) {
            int px = (int) (20 * Math.cos(Math.toRadians(a)));
            int pz = (int) (20 * Math.sin(Math.toRadians(a)));
            b.pillar(px, 1, 5, pz, Material.COBBLESTONE_WALL);
        }

        // Ruined city buildings (4 around the arena)
        int[][] buildingPos = {{-65, -65}, {65, -65}, {-65, 65}, {65, 65}};
        for (int[] bp : buildingPos) {
            // Ruined building shell
            b.fillBox(bp[0] - 6, 0, bp[1] - 6, bp[0] + 6, 0, bp[1] + 6, Material.STONE_BRICKS);
            b.fillWalls(bp[0] - 6, 1, bp[1] - 6, bp[0] + 6, 8, bp[1] + 6, Material.STONE_BRICKS);
            b.hollowBox(bp[0] - 6, 1, bp[1] - 6, bp[0] + 6, 8, bp[1] + 6);
            b.decay(bp[0] - 6, 4, bp[1] - 6, bp[0] + 6, 8, bp[1] + 6, 0.4);
            b.scatter(bp[0] - 6, 1, bp[1] - 6, bp[0] + 6, 8, bp[1] + 6,
                    Material.STONE_BRICKS, Material.CRACKED_STONE_BRICKS, 0.3);
            b.scatter(bp[0] - 6, 1, bp[1] - 6, bp[0] + 6, 8, bp[1] + 6,
                    Material.STONE_BRICKS, Material.MOSSY_STONE_BRICKS, 0.2);
            b.addVines(bp[0] - 7, 1, bp[1] - 7, bp[0] + 7, 8, bp[1] + 7, 0.15);
        }

        // Crashed ships (2 on opposite shores)
        // Ship 1 (south shore)
        for (int z = 70; z <= 82; z++) {
            int hw = Math.max(1, 3 - Math.abs(z - 76) / 3);
            b.fillBox(-hw, 0, z, hw, 0, z, Material.DARK_OAK_PLANKS);
            b.setBlock(-hw, 1, z, Material.DARK_OAK_PLANKS);
            b.setBlock(hw, 1, z, Material.DARK_OAK_PLANKS);
        }
        b.pillar(0, 1, 5, 76, Material.OAK_LOG);  // Broken mast
        b.decay(-3, 0, 70, 3, 3, 82, 0.3);

        // Ship 2 (north shore, tilted)
        for (int z = -82; z <= -70; z++) {
            int hw = Math.max(1, 3 - Math.abs(z + 76) / 3);
            b.fillBox(-hw, 1, z, hw, 1, z, Material.OAK_PLANKS);
        }
        b.decay(-3, 0, -82, 3, 3, -70, 0.35);

        // Overgrown vegetation
        int[][] treePositions = {{-50, -30}, {50, 30}, {-40, 50}, {40, -50},
                {-70, 0}, {70, 0}, {0, -70}, {0, 70}};
        for (int[] tp : treePositions) {
            b.pillar(tp[0], 1, 8, tp[1], Material.OAK_LOG);
            for (int y = 6; y <= 10; y++) b.filledCircle(tp[0], y, tp[1], 3, Material.OAK_LEAVES);
        }

        // Atmospheric details: soul lanterns, cobwebs
        for (int a = 0; a < 360; a += 45) {
            int lx = (int) (45 * Math.cos(Math.toRadians(a)));
            int lz = (int) (45 * Math.sin(Math.toRadians(a)));
            b.setBlock(lx, 1, lz, Material.SOUL_LANTERN);
        }

        b.placeChest(0, 1, 0);
        b.placeChest(-60, 1, -60);
        b.placeChest(60, 1, 60);
        b.setBossSpawn(0, 1, 10);
    }

    // ═══════════════════════════════════════════════════════════════
    //  31. WEREWOLF DEN
    // ═══════════════════════════════════════════════════════════════

    public static void buildWerewolfDen(StructureBuilder b) {
        // Moonlit clearing
        b.filledCircle(0, 0, 0, 10, Material.PODZOL);
        b.scatter(-10, 0, -10, 10, 0, 10, Material.PODZOL, Material.COARSE_DIRT, 0.3);

        // Cave entrance (south side, into a hill)
        b.fillBox(-3, 0, 4, 3, 0, 12, Material.STONE);
        b.fillBox(-4, 1, 4, 4, 5, 12, Material.STONE);
        b.fillBox(-5, 2, 4, 5, 6, 12, Material.STONE);
        b.fillBox(-3, 1, 4, 3, 4, 12, Material.AIR);  // Cave interior
        b.fillBox(-2, 1, 3, 2, 3, 4, Material.AIR);  // Entrance opening

        // Bone-littered floor
        b.scatter(-2, 0, 5, 2, 0, 11, Material.STONE, Material.BONE_BLOCK, 0.2);
        b.setBlock(-1, 1, 8, Material.BONE_BLOCK);
        b.setBlock(1, 1, 6, Material.BONE_BLOCK);
        b.setBlock(0, 1, 10, Material.BONE_BLOCK);
        b.setBlock(2, 1, 9, Material.BONE_BLOCK);

        // Claw marks (stripped logs on walls)
        b.setBlock(-3, 2, 7, Material.STRIPPED_SPRUCE_LOG);
        b.setBlock(-3, 3, 7, Material.STRIPPED_SPRUCE_LOG);
        b.setBlock(-3, 2, 8, Material.STRIPPED_SPRUCE_LOG);
        b.setBlock(3, 2, 9, Material.STRIPPED_SPRUCE_LOG);
        b.setBlock(3, 3, 9, Material.STRIPPED_SPRUCE_LOG);
        b.setBlock(3, 3, 10, Material.STRIPPED_SPRUCE_LOG);

        // Wolf pelts (carpets)
        b.setBlock(-1, 1, 7, Material.GRAY_CARPET);
        b.setBlock(0, 1, 9, Material.BROWN_CARPET);
        b.setBlock(1, 1, 11, Material.LIGHT_GRAY_CARPET);

        // Ritual circle in the clearing
        b.circle(0, 0, 0, 5, Material.SOUL_SOIL);
        for (int a = 0; a < 360; a += 60) {
            int rx = (int) (5 * Math.cos(Math.toRadians(a)));
            int rz = (int) (5 * Math.sin(Math.toRadians(a)));
            b.setBlock(rx, 1, rz, Material.SOUL_CAMPFIRE);
        }
        b.setBlock(0, 1, 0, Material.LODESTONE);

        // Dead trees around clearing
        b.pillar(-8, 1, 6, -5, Material.SPRUCE_LOG);
        b.setBlock(-9, 5, -5, Material.SPRUCE_LOG);
        b.setBlock(-7, 6, -5, Material.SPRUCE_LOG);
        b.pillar(7, 1, 5, -7, Material.SPRUCE_LOG);
        b.setBlock(8, 5, -7, Material.SPRUCE_LOG);
        b.pillar(-6, 1, 7, 8, Material.SPRUCE_LOG);
        b.setBlock(-5, 7, 8, Material.SPRUCE_LOG);

        // Dense dark forest surround
        int[][] darkTrees = {{-9, 2}, {9, -3}, {-7, -8}, {8, 6}};
        for (int[] dt : darkTrees) {
            b.pillar(dt[0], 1, 10, dt[1], Material.DARK_OAK_LOG);
            for (int y = 7; y <= 11; y++) b.filledCircle(dt[0], y, dt[1], 3, Material.DARK_OAK_LEAVES);
        }

        // Cave back: den area with sleeping spot
        b.setBlock(-2, 1, 11, Material.BROWN_CARPET);
        b.setBlock(-1, 1, 11, Material.BROWN_CARPET);
        b.setBlock(0, 1, 11, Material.BROWN_CARPET);

        // Eerie lighting
        b.setBlock(0, 4, 7, Material.SOUL_LANTERN);

        // Cobwebs in cave
        b.setBlock(-2, 3, 10, Material.COBWEB);
        b.setBlock(2, 3, 6, Material.COBWEB);

        b.placeChest(0, 1, 5);
        b.setBossSpawn(0, 1, -3);
    }

    // ═══════════════════════════════════════════════════════════════
    //  32. NECROMANCER DUNGEON
    // ═══════════════════════════════════════════════════════════════

    public static void buildNecromancerDungeon(StructureBuilder b) {
        // Underground crypt — main chamber
        b.fillWalls(-12, -1, -12, 12, 8, 12, Material.DEEPSLATE_BRICKS);
        b.hollowBox(-12, 0, -12, 12, 8, 12);
        b.fillFloor(-11, -11, 11, 11, -1, Material.SOUL_SAND);

        // Entrance stairway (descending from surface)
        for (int s = 0; s < 8; s++) {
            b.fillBox(-2, 8 - s, -14 - s, 2, 8 - s, -14 - s, Material.STONE_BRICK_STAIRS);
            b.fillBox(-2, 9 - s, -14 - s, 2, 12, -14 - s, Material.AIR);
        }
        b.fillBox(-2, 0, -12, 2, 5, -12, Material.AIR);  // Entrance door

        // Skull decorations (wither skeleton skulls on walls)
        for (int x = -10; x <= 10; x += 4) {
            b.setBlock(x, 5, -11, Material.WITHER_SKELETON_SKULL);
            b.setBlock(x, 5, 11, Material.WITHER_SKELETON_SKULL);
        }
        for (int z = -10; z <= 10; z += 4) {
            b.setBlock(-11, 5, z, Material.WITHER_SKELETON_SKULL);
            b.setBlock(11, 5, z, Material.WITHER_SKELETON_SKULL);
        }

        // Dark library (west wing)
        b.fillBox(-12, 0, -8, -6, 6, -2, Material.DEEPSLATE_BRICKS);
        b.fillBox(-11, 0, -7, -7, 5, -3, Material.AIR);
        b.setBlock(-6, 1, -5, Material.AIR); b.setBlock(-6, 2, -5, Material.AIR); // Door
        b.bookshelfWall(-11, 1, -7, -7, 3);
        b.bookshelfWall(-11, 1, -3, -7, 3);
        b.setBlock(-9, 1, -5, Material.LECTERN);
        b.setBlock(-8, 4, -5, Material.SOUL_LANTERN);

        // Summoning chamber (east wing)
        b.fillBox(6, 0, -8, 12, 6, -2, Material.DEEPSLATE_BRICKS);
        b.fillBox(7, 0, -7, 11, 5, -3, Material.AIR);
        b.setBlock(6, 1, -5, Material.AIR); b.setBlock(6, 2, -5, Material.AIR); // Door
        b.fillFloor(7, -7, 11, -3, 0, Material.SOUL_SAND);
        // Summoning circle
        b.circle(9, 0, -5, 2, Material.OBSIDIAN);
        b.setBlock(9, 1, -5, Material.SOUL_CAMPFIRE);
        b.setBlock(7, 1, -7, Material.GREEN_CANDLE);
        b.setBlock(11, 1, -7, Material.GREEN_CANDLE);
        b.setBlock(7, 1, -3, Material.GREEN_CANDLE);
        b.setBlock(11, 1, -3, Material.GREEN_CANDLE);

        // Central altar/throne area
        b.fillBox(-3, 0, 5, 3, 0, 10, Material.OBSIDIAN);
        b.setBlock(0, 1, 8, Material.OBSIDIAN);
        b.setBlock(0, 2, 8, Material.OBSIDIAN);
        b.setStairs(-1, 1, 8, Material.DARK_OAK_STAIRS, BlockFace.EAST, false);
        b.setStairs(1, 1, 8, Material.DARK_OAK_STAIRS, BlockFace.WEST, false);
        b.setBlock(0, 3, 9, Material.WITHER_SKELETON_SKULL);

        // Green candles throughout
        b.setBlock(-8, 1, 0, Material.GREEN_CANDLE);
        b.setBlock(8, 1, 0, Material.GREEN_CANDLE);
        b.setBlock(0, 1, 4, Material.GREEN_CANDLE);
        b.setBlock(-6, 1, 6, Material.GREEN_CANDLE);
        b.setBlock(6, 1, 6, Material.GREEN_CANDLE);

        // Pillars
        for (int[] p : new int[][]{{-8, -8}, {8, -8}, {-8, 8}, {8, 8}}) {
            b.pillar(p[0], 0, 7, p[1], Material.POLISHED_BLACKSTONE_BRICKS);
            b.setBlock(p[0], 7, p[1], Material.SOUL_LANTERN);
        }

        // Soul sand floor decorations
        b.scatter(-11, -1, -11, 11, -1, 11, Material.SOUL_SAND, Material.SOUL_SOIL, 0.15);

        // Coffins (dark oak planks)
        b.fillBox(-10, 0, 3, -8, 0, 5, Material.DARK_OAK_PLANKS);
        b.setBlock(-9, 1, 4, Material.DARK_OAK_SLAB);
        b.fillBox(8, 0, 3, 10, 0, 5, Material.DARK_OAK_PLANKS);
        b.setBlock(9, 1, 4, Material.DARK_OAK_SLAB);

        // Cobwebs
        b.scatter(-11, 5, -11, 11, 7, 11, Material.AIR, Material.COBWEB, 0.06);

        // Chains from ceiling
        b.setBlock(-4, 7, -4, Material.IRON_CHAIN); b.setBlock(-4, 6, -4, Material.IRON_CHAIN);
        b.setBlock(4, 7, 4, Material.IRON_CHAIN); b.setBlock(4, 6, 4, Material.IRON_CHAIN);

        b.placeChest(-10, 1, -5);
        b.placeChest(0, 1, 6);
        b.placeChest(9, 1, -5);
        b.setBossSpawn(0, 1, 2);
    }
}

package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

/**
 * UltraVillageBuilder — a walled village with 10 houses, streetlamp-lit roads, a
 * market square well, church with bell tower, blacksmith, inn and wheat farm plots.
 *
 * <p>Extracted verbatim from the original {@code OverworldStructureBuilders.buildUltraVillage}
 * method to give the structure its own file per the one-class-per-structure
 * architecture.
 */
public final class UltraVillageBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
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
}

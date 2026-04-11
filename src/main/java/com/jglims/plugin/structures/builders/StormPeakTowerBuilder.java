package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;

/**
 * StormPeakTowerBuilder — Tall prismarine/quartz tower on mountain peak,
 * lightning rod, observatory, cloud floor, wind-catcher arms.
 *
 * <p>Extracted verbatim from the original {@code DimensionStructureBuilders.buildStormPeakTower}
 * method to give the structure its own file per the one-class-per-structure
 * architecture.
 */
public final class StormPeakTowerBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
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
}

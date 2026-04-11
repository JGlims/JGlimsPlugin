package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;

/**
 * WitherSanctumBuilder — Soul sand valley temple, wither skull decorations,
 * dark altar, soul fire torches, ominous ambiance.
 *
 * <p>Extracted verbatim from the original {@code DimensionStructureBuilders.buildWitherSanctum}
 * method to give the structure its own file per the one-class-per-structure
 * architecture.
 */
public final class WitherSanctumBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
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
}

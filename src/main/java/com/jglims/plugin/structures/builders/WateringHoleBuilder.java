package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;

/**
 * WateringHoleBuilder — natural pond with muddy banks, palm trees, animal trails, and stone idol shrine.
 *
 * <p>Extracted verbatim from the original monolithic builder method.
 */
public final class WateringHoleBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
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

        // ─── Underwater plant life: kelp strands + sea lanterns at pool bottom ───
        b.setBlock(-3, 0, -2, Material.KELP_PLANT);
        b.setBlock(2, 0, 2, Material.KELP_PLANT);
        b.setBlock(-1, -1, 3, Material.SEA_LANTERN);
        b.setBlock(3, -1, -1, Material.SEA_LANTERN);

        // ─── Stone idol at the watering hole's edge (prehistoric shrine) ───
        b.pillar(0, 1, 4, -8, Material.MOSSY_COBBLESTONE);
        b.setBlock(0, 5, -8, Material.CARVED_PUMPKIN);
        b.setBlock(-1, 3, -8, Material.MOSSY_COBBLESTONE_WALL);
        b.setBlock(1, 3, -8, Material.MOSSY_COBBLESTONE_WALL);
        b.setBlock(0, 2, -9, Material.CAMPFIRE);

        // ─── Drinking footprints (different animal prints) ───
        for (int i = 0; i < 10; i++) {
            int fx = -4 - i;
            b.setBlock(fx, 0, 0 + (i % 2 == 0 ? 1 : -1), Material.DIRT_PATH);
        }

        // ─── Ancient dinosaur bones lying around (atmosphere) ───
        b.fillBox(-12, 1, -12, -10, 1, -10, Material.BONE_BLOCK);
        b.setBlock(-11, 2, -11, Material.BONE_BLOCK);
        b.setBlock(12, 1, 11, Material.BONE_BLOCK);
        b.setBlock(11, 1, 12, Material.BONE_BLOCK);

        // ─── Berry bushes for passive mob food ───
        b.setBlockIfAir(-10, 1, 5, Material.SWEET_BERRY_BUSH);
        b.setBlockIfAir(10, 1, -5, Material.SWEET_BERRY_BUSH);

        // ─── Fallen log across the trail ───
        b.fillBox(3, 1, 6, 7, 1, 6, Material.JUNGLE_LOG);
        b.setBlock(5, 2, 6, Material.MOSS_CARPET);
        b.setBlock(6, 2, 6, Material.MOSS_CARPET);

        // ─── Small hidden cache under a rock ───
        b.setBlock(-9, 0, 9, Material.COBBLESTONE);
        b.fillBox(-9, -2, 9, -9, -1, 9, Material.AIR);

        b.placeChest(8, 1, 8);
        b.placeChest(-9, -1, 9);   // hidden cache
        b.placeChest(0, 2, -8);    // idol offering chest
    }
}

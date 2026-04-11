package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;

/**
 * RaptorNestBuilder — coarse dirt nest with hay bedding, egg clusters, and extended cave system.
 *
 * <p>Extracted verbatim from the original monolithic builder method.
 */
public final class RaptorNestBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
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

        // ─── Extended cave system behind the nest (where the raptors sleep) ───
        b.fillBox(-4, 0, -16, 4, 4, -10, Material.STONE);
        b.fillBox(-3, 1, -15, 3, 3, -11, Material.AIR);
        b.fillFloor(-3, -15, 3, -11, 0, Material.COARSE_DIRT);
        // Sleeping nests inside the cave
        b.filledCircle(-2, 1, -13, 1, Material.HAY_BLOCK);
        b.filledCircle(2, 1, -13, 1, Material.HAY_BLOCK);
        b.setBlock(-2, 2, -13, Material.TURTLE_EGG);
        b.setBlock(2, 2, -13, Material.TURTLE_EGG);

        // ─── Bone pile trophy display ───
        b.fillBox(-6, 1, -6, -4, 2, -4, Material.BONE_BLOCK);
        b.setBlock(-5, 3, -5, Material.SKELETON_SKULL);
        b.setBlock(-5, 2, -4, Material.LANTERN);
        b.fillBox(4, 1, 4, 6, 2, 6, Material.BONE_BLOCK);
        b.setBlock(5, 3, 5, Material.WITHER_SKELETON_SKULL);

        // ─── Kill site: dead prey with feather carpets (raptor pack's lunch) ───
        b.setBlock(8, 1, -3, Material.RED_WOOL);  // meat pile
        b.setBlock(8, 1, -4, Material.BONE_BLOCK);
        b.setBlock(7, 1, -3, Material.LIGHT_GRAY_CARPET);
        b.setBlock(7, 1, -4, Material.WHITE_CARPET);
        b.setBlock(9, 1, -3, Material.BONE_BLOCK);

        // ─── Claw marks on trees around the nest ───
        for (int[] tree : new int[][]{{-10, 2}, {10, -2}, {-6, -10}, {10, 8}}) {
            b.pillar(tree[0], 1, 6, tree[1], Material.DARK_OAK_LOG);
            for (int y = 4; y <= 7; y++) b.filledCircle(tree[0], y, tree[1], 2, Material.DARK_OAK_LEAVES);
            b.setBlock(tree[0] + 1, 3, tree[1], Material.STRIPPED_DARK_OAK_LOG);  // claw mark
        }

        // ─── Hidden egg cache under the main nest ───
        b.fillBox(-2, -3, -2, 2, -1, 2, Material.STONE);
        b.fillBox(-1, -2, -1, 1, -2, 1, Material.AIR);
        b.setBlock(0, -2, 0, Material.TURTLE_EGG);

        b.placeChest(0, 2, 2);
        b.placeChest(0, 2, -13);   // cave chest
        b.placeChest(0, -2, 0);    // hidden egg cache chest
    }
}

package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;

/**
 * FairyGladeBuilder — cherry blossom clearing with enchanted spring, flower arches, and unicorn stable.
 *
 * <p>Extracted verbatim from the original monolithic builder method.
 */
public final class FairyGladeBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
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
}

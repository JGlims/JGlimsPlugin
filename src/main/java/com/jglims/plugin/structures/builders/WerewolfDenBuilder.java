package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;

/**
 * WerewolfDenBuilder — moonlit clearing with stone cave, ritual circle, and dark oak surround.
 *
 * <p>Extracted verbatim from the original monolithic builder method.
 */
public final class WerewolfDenBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
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
}

package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

/**
 * PiglinPalaceBuilder — Gold-accented blackstone palace, throne room, treasure hoard,
 * barracks, banquet hall, golden pillars.
 *
 * <p>Extracted verbatim from the original {@code DimensionStructureBuilders.buildPiglinPalace}
 * method to give the structure its own file per the one-class-per-structure
 * architecture.
 */
public final class PiglinPalaceBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
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
}

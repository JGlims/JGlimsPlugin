package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

/**
 * BasaltSpireBuilder — Massive 50-block basalt tower with interior rooms,
 * lava waterfalls, polished basalt trim, open observation top.
 *
 * <p>Extracted verbatim from the original {@code DimensionStructureBuilders.buildBasaltSpire}
 * method to give the structure its own file per the one-class-per-structure
 * architecture.
 */
public final class BasaltSpireBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
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
}

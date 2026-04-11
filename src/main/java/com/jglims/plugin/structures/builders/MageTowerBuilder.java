package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

/**
 * MageTowerBuilder — an 8-story circular wizard's tower topped with a purple-glass
 * observatory dome, containing an enchanting library, alchemy labs, and spiral staircase.
 *
 * <p>Extracted verbatim from the original {@code OverworldStructureBuilders.buildMageTower}
 * method to give the structure its own file per the one-class-per-structure
 * architecture.
 */
public final class MageTowerBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
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
}

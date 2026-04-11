package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

/**
 * FortressBuilder — a star-fort style deepslate fortress with bastions, moat,
 * drawbridge, central keep with spire, barracks, armory and an underground dungeon.
 *
 * <p>Extracted verbatim from the original {@code OverworldStructureBuilders.buildFortress}
 * method to give the structure its own file per the one-class-per-structure
 * architecture.
 */
public final class FortressBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // Outer wall (star fort shape simulated with rectangle + bastions)
        b.fillWalls(-24, 0, -24, 24, 9, 24, Material.DEEPSLATE_BRICKS);
        b.hollowBox(-24, 1, -24, 24, 9, 24);
        b.fillFloor(-23, -23, 23, 23, 0, Material.STONE_BRICKS);

        // Battlements
        for (int x = -24; x <= 24; x += 2) {
            b.setBlock(x, 10, -24, Material.DEEPSLATE_BRICK_WALL);
            b.setBlock(x, 10, 24, Material.DEEPSLATE_BRICK_WALL);
        }
        for (int z = -24; z <= 24; z += 2) {
            b.setBlock(-24, 10, z, Material.DEEPSLATE_BRICK_WALL);
            b.setBlock(24, 10, z, Material.DEEPSLATE_BRICK_WALL);
        }

        // 4 Bastions (triangular protrusions at corners)
        for (int[] c : new int[][]{{-24, -24}, {24, -24}, {-24, 24}, {24, 24}}) {
            b.fillBox(c[0] - 3, 0, c[1] - 3, c[0] + 3, 14, c[1] + 3, Material.DEEPSLATE_BRICKS);
            b.fillBox(c[0] - 2, 1, c[1] - 2, c[0] + 2, 13, c[1] + 2, Material.AIR);
            b.spire(c[0], 14, c[1], 3, 5, Material.DEEPSLATE_TILE_SLAB);
            for (int y = 1; y <= 13; y++) b.setBlock(c[0] + 1, y, c[1] - 2, Material.LADDER);
            b.setBlock(c[0], 13, c[1], Material.LANTERN);
        }

        // Moat (water ring)
        for (int x = -27; x <= 27; x++) {
            for (int z = -27; z <= 27; z++) {
                if (Math.abs(x) >= 25 || Math.abs(z) >= 25) {
                    if (Math.abs(x) <= 27 && Math.abs(z) <= 27) {
                        b.setBlock(x, -1, z, Material.WATER);
                        b.setBlock(x, 0, z, Material.WATER);
                    }
                }
            }
        }

        // Drawbridge (south)
        b.fillBox(-2, 1, -24, 2, 5, -24, Material.AIR);  // Gate opening
        b.fillBox(-2, 0, -27, 2, 0, -24, Material.OAK_PLANKS);  // Bridge
        b.pillar(-3, 1, 8, -24, Material.IRON_BARS);
        b.pillar(3, 1, 8, -24, Material.IRON_BARS);

        // Central keep
        b.fillWalls(-8, 0, -8, 8, 18, 8, Material.POLISHED_DEEPSLATE);
        b.hollowBox(-8, 1, -8, 8, 18, 8);
        b.fillFloor(-7, -7, 7, 7, 0, Material.STONE_BRICKS);
        b.spire(0, 18, 0, 5, 8, Material.DEEPSLATE_TILE_SLAB);
        b.setBlock(0, 26, 0, Material.END_ROD);

        // Keep entrance
        b.setBlock(0, 1, -8, Material.AIR); b.setBlock(0, 2, -8, Material.AIR); b.setBlock(0, 3, -8, Material.AIR);

        // Keep interior: throne
        b.setBlock(0, 1, 5, Material.GOLD_BLOCK);
        b.setStairs(-1, 1, 5, Material.STONE_BRICK_STAIRS, BlockFace.EAST, false);
        b.setStairs(1, 1, 5, Material.STONE_BRICK_STAIRS, BlockFace.WEST, false);
        b.setBlock(0, 2, 6, Material.GOLD_BLOCK);

        // Barracks room (NW)
        b.fillBox(-22, 0, -22, -14, 0, -14, Material.STONE_BRICKS);
        b.fillWalls(-22, 1, -22, -14, 5, -14, Material.STONE_BRICKS);
        b.hollowBox(-22, 1, -22, -14, 5, -14);
        b.fillBox(-22, 6, -22, -14, 6, -14, Material.OAK_PLANKS);
        b.setBlock(-18, 1, -22, Material.AIR); b.setBlock(-18, 2, -22, Material.AIR);
        for (int bx = -21; bx <= -15; bx += 3) b.setBlock(bx, 1, -20, Material.RED_BED);

        // Armory (NE)
        b.fillBox(14, 0, -22, 22, 0, -14, Material.STONE_BRICKS);
        b.fillWalls(14, 1, -22, 22, 5, -14, Material.COBBLESTONE);
        b.hollowBox(14, 1, -22, 22, 5, -14);
        b.fillBox(14, 6, -22, 22, 6, -14, Material.OAK_PLANKS);
        b.setBlock(18, 1, -22, Material.AIR); b.setBlock(18, 2, -22, Material.AIR);
        b.setBlock(15, 1, -20, Material.ANVIL);
        b.setBlock(16, 1, -20, Material.SMITHING_TABLE);
        b.setBlock(21, 1, -15, Material.GRINDSTONE);

        // Dungeon below
        b.fillBox(-12, -6, -12, 12, -1, 12, Material.DEEPSLATE_BRICKS);
        b.fillBox(-11, -5, -11, 11, -1, 11, Material.AIR);
        b.fillFloor(-11, -11, 11, 11, -6, Material.DEEPSLATE_TILES);
        for (int z = -8; z <= 8; z += 4) {
            b.setBlock(-10, -5, z, Material.IRON_BARS);
            b.setBlock(-10, -4, z, Material.IRON_BARS);
            b.setBlock(10, -5, z, Material.IRON_BARS);
            b.setBlock(10, -4, z, Material.IRON_BARS);
        }
        b.setBlock(0, -5, 0, Material.SOUL_LANTERN);

        // Wall lighting
        b.wallLighting(-23, 5, -24, 23, -24, 5, Material.LANTERN);
        b.wallLighting(-23, 5, 24, 23, 24, 5, Material.LANTERN);

        b.placeChest(-20, 1, -18);
        b.placeChest(18, 1, -18);
        b.placeChest(0, -5, 5);
        b.setBossSpawn(0, 1, 0);
    }
}

package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

/**
 * JapaneseTempleBuilder — a traditional Japanese-style temple with a red torii gate,
 * curved dark-oak pagoda roof, paper walls, interior altar, koi pond, zen garden and
 * a cherry blossom tree.
 *
 * <p>Extracted verbatim from the original {@code OverworldStructureBuilders.buildJapaneseTemple}
 * method to give the structure its own file per the one-class-per-structure
 * architecture.
 */
public final class JapaneseTempleBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // Stone foundation platform
        b.fillBox(-12, 0, -12, 12, 0, 12, Material.SMOOTH_STONE);
        b.fillBox(-13, -1, -13, 13, -1, 13, Material.STONE);

        // Torii gate (entrance, red nether brick)
        b.pillar(-3, 1, 6, -14, Material.RED_NETHER_BRICKS);
        b.pillar(3, 1, 6, -14, Material.RED_NETHER_BRICKS);
        b.fillBox(-4, 7, -14, 4, 7, -14, Material.RED_NETHER_BRICKS);
        b.fillBox(-5, 8, -14, 5, 8, -14, Material.RED_NETHER_BRICK_SLAB);
        // Second crossbar
        b.fillBox(-3, 5, -14, 3, 5, -14, Material.RED_NETHER_BRICKS);

        // Path from torii to temple
        for (int z = -13; z <= -4; z++) {
            b.setBlock(0, 0, z, Material.GRAVEL);
            b.setBlock(-1, 0, z, Material.GRAVEL);
            b.setBlock(1, 0, z, Material.GRAVEL);
        }

        // Main hall
        b.fillBox(-10, 0, -3, 10, 0, 10, Material.OAK_PLANKS);
        b.fillWalls(-10, 1, -3, 10, 6, 10, Material.OAK_PLANKS);
        b.hollowBox(-10, 1, -3, 10, 6, 10);

        // Paper walls (white wool replacing some planks)
        for (int x = -9; x <= 9; x += 2) {
            b.setBlock(x, 2, -3, Material.WHITE_WOOL);
            b.setBlock(x, 3, -3, Material.WHITE_WOOL);
            b.setBlock(x, 4, -3, Material.WHITE_WOOL);
        }
        for (int z = -2; z <= 9; z += 2) {
            b.setBlock(-10, 3, z, Material.WHITE_STAINED_GLASS_PANE);
            b.setBlock(10, 3, z, Material.WHITE_STAINED_GLASS_PANE);
        }

        // Curved roofline (stairs on each side)
        for (int layer = 0; layer < 4; layer++) {
            int y = 7 + layer;
            for (int z = -4 + layer; z <= 11 - layer; z++) {
                b.setStairs(-11 + layer, y, z, Material.DARK_OAK_STAIRS, BlockFace.EAST, false);
                b.setStairs(11 - layer, y, z, Material.DARK_OAK_STAIRS, BlockFace.WEST, false);
            }
        }
        // Ridge
        for (int z = -2; z <= 9; z++) {
            b.setSlab(0, 11, z, Material.DARK_OAK_SLAB, false);
        }
        // Upturned eaves
        b.setStairs(-12, 7, -4, Material.DARK_OAK_STAIRS, BlockFace.SOUTH, true);
        b.setStairs(12, 7, -4, Material.DARK_OAK_STAIRS, BlockFace.SOUTH, true);
        b.setStairs(-12, 7, 11, Material.DARK_OAK_STAIRS, BlockFace.NORTH, true);
        b.setStairs(12, 7, 11, Material.DARK_OAK_STAIRS, BlockFace.NORTH, true);

        // Entrance (wide sliding door)
        b.fillBox(-2, 1, -3, 2, 4, -3, Material.AIR);

        // Interior: altar
        b.setBlock(0, 1, 8, Material.GOLD_BLOCK);
        b.setBlock(0, 2, 8, Material.CANDLE);
        b.setBlock(-2, 1, 8, Material.FLOWER_POT);
        b.setBlock(2, 1, 8, Material.FLOWER_POT);

        // Tatami floor (smooth sandstone as tatami)
        b.fillFloor(-9, -2, 9, 9, 0, Material.SMOOTH_SANDSTONE);

        // Lanterns (paper lantern style)
        b.setBlock(-8, 4, 0, Material.LANTERN);
        b.setBlock(8, 4, 0, Material.LANTERN);
        b.setBlock(-8, 4, 6, Material.LANTERN);
        b.setBlock(8, 4, 6, Material.LANTERN);
        b.setBlock(0, 5, 3, Material.LANTERN);

        // Koi pond (east side)
        b.fillBox(6, -1, -10, 11, -1, -5, Material.STONE);
        b.fillBox(7, 0, -9, 10, 0, -6, Material.WATER);
        b.setBlock(8, 0, -8, Material.LILY_PAD);
        b.setBlock(9, 0, -7, Material.LILY_PAD);

        // Zen garden (west side) — gravel + dead bushes
        b.fillBox(-11, 0, -10, -6, 0, -5, Material.GRAVEL);
        b.setBlock(-9, 1, -8, Material.DEAD_BUSH);
        b.setBlock(-8, 1, -6, Material.DEAD_BUSH);
        b.setBlock(-7, 0, -7, Material.SMOOTH_STONE);  // Stone in zen garden

        // Cherry tree next to temple
        b.pillar(-8, 1, 6, 8, Material.CHERRY_LOG);
        for (int y = 4; y <= 8; y++) b.filledCircle(-8, y, 8, 3, Material.CHERRY_LEAVES);

        // Exterior lanterns on posts
        b.pillar(-4, 1, 3, -12, Material.OAK_FENCE);
        b.setBlock(-4, 3, -12, Material.LANTERN);
        b.pillar(4, 1, 3, -12, Material.OAK_FENCE);
        b.setBlock(4, 3, -12, Material.LANTERN);

        b.placeChest(0, 1, 5);
    }
}

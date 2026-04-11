package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

/**
 * HouseTreeBuilder — giant oak with multi-level treehouse, branch canopies, and rope bridge.
 *
 * <p>Extracted verbatim from the original {@code OverworldStructureBuilders.buildHouseTree}
 * method to give the structure its own file per the one-class-per-structure
 * architecture.
 */
public final class HouseTreeBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // Giant trunk (5-block wide base, tapers)
        for (int y = 0; y <= 20; y++) {
            int r = y < 5 ? 3 : (y < 12 ? 2 : 1);
            b.filledCircle(0, y, 0, r, Material.OAK_LOG);
        }

        // Massive canopy
        for (int y = 14; y <= 22; y++) {
            int r = y < 17 ? 7 : (y < 20 ? 5 : (y < 22 ? 3 : 1));
            b.filledCircle(0, y, 0, r, Material.OAK_LEAVES);
        }
        // Extra branch canopies
        b.filledCircle(5, 13, 3, 3, Material.OAK_LEAVES);
        b.filledCircle(-4, 14, -4, 3, Material.OAK_LEAVES);
        // Branch supports
        b.flyingButtress(2, 10, 0, 5, 13, 3, Material.OAK_LOG);
        b.flyingButtress(-2, 11, 0, -4, 14, -4, Material.OAK_LOG);

        // Level 1 room (y=5-8): living room
        b.fillBox(-4, 5, -4, 4, 5, 4, Material.OAK_PLANKS);
        b.fillWalls(-4, 6, -4, 4, 8, 4, Material.OAK_PLANKS);
        b.hollowBox(-4, 6, -4, 4, 8, 4);
        b.setBlock(-4, 7, 0, Material.GLASS_PANE);
        b.setBlock(4, 7, 0, Material.GLASS_PANE);
        b.table(0, 6, 0);
        b.chair(-1, 6, 0, Material.OAK_STAIRS, BlockFace.EAST);
        b.chair(1, 6, 0, Material.OAK_STAIRS, BlockFace.WEST);
        b.setBlock(-3, 7, 3, Material.LANTERN);

        // Level 2 room (y=9-12): bedroom
        b.fillBox(-3, 9, -3, 3, 9, 3, Material.OAK_PLANKS);
        b.fillWalls(-3, 10, -3, 3, 12, 3, Material.OAK_PLANKS);
        b.hollowBox(-3, 10, -3, 3, 12, 3);
        b.setBlock(-3, 11, 0, Material.GLASS_PANE);
        b.setBlock(3, 11, 0, Material.GLASS_PANE);
        b.setBlock(-2, 10, 2, Material.RED_BED);
        b.setBlock(2, 10, -2, Material.BOOKSHELF);
        b.setBlock(2, 11, -2, Material.BOOKSHELF);
        b.setBlock(0, 11, 2, Material.LANTERN);

        // Level 3 lookout platform (y=14)
        b.fillBox(-2, 14, -2, 2, 14, 2, Material.OAK_PLANKS);
        b.circle(0, 15, 0, 2, Material.OAK_FENCE);
        b.setBlock(0, 15, 0, Material.LANTERN);

        // Ladders connecting levels
        for (int y = 1; y <= 5; y++) b.setBlock(3, y, -2, Material.LADDER);
        for (int y = 6; y <= 9; y++) b.setBlock(2, y, -2, Material.LADDER);
        for (int y = 10; y <= 14; y++) b.setBlock(1, y, -1, Material.LADDER);

        // Rope bridge from branch canopy to main platform
        for (int x = 2; x <= 5; x++) {
            b.setBlock(x, 14, 3, Material.OAK_PLANKS);
            b.setBlock(x, 15, 4, Material.OAK_FENCE);
            b.setBlock(x, 15, 2, Material.OAK_FENCE);
        }

        // Roots at base
        b.setBlock(-3, 0, -3, Material.OAK_LOG);
        b.setBlock(3, 0, 3, Material.OAK_LOG);
        b.setBlock(-3, 0, 2, Material.OAK_LOG);
        b.setBlock(3, 0, -2, Material.OAK_LOG);

        // Flower pots on balcony
        b.setBlock(-4, 6, -4, Material.POTTED_FERN);
        b.setBlock(4, 6, 4, Material.POTTED_AZURE_BLUET);

        b.placeChest(-2, 6, -2);
    }
}

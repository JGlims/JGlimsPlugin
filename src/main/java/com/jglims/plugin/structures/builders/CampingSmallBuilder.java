package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;

/**
 * CampingSmallBuilder — small two-tent campsite with campfire, seats, and supply chest.
 *
 * <p>Extracted verbatim from the original {@code OverworldStructureBuilders.buildCampingSmall}
 * method to give the structure its own file per the one-class-per-structure
 * architecture.
 */
public final class CampingSmallBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // Ground pad
        b.filledCircle(0, 0, 0, 5, Material.GRASS_BLOCK);
        b.scatter(-5, 0, -5, 5, 0, 5, Material.GRASS_BLOCK, Material.COARSE_DIRT, 0.3);

        // Central campfire with ring of cobblestone
        b.circle(0, 0, 0, 1, Material.COBBLESTONE);
        b.setBlock(0, 1, 0, Material.CAMPFIRE);

        // Tent 1 (NW) — white wool on fence frame
        b.setBlock(-3, 1, -3, Material.OAK_FENCE);
        b.setBlock(-3, 1, -1, Material.OAK_FENCE);
        b.setBlock(-1, 1, -3, Material.OAK_FENCE);
        b.setBlock(-1, 1, -1, Material.OAK_FENCE);
        b.setBlock(-3, 2, -3, Material.OAK_FENCE);
        b.setBlock(-1, 2, -1, Material.OAK_FENCE);
        b.fillBox(-3, 2, -3, -1, 2, -1, Material.WHITE_WOOL);
        b.setBlock(-2, 3, -2, Material.WHITE_WOOL);
        b.setBlock(-2, 1, -2, Material.BROWN_CARPET);

        // Tent 2 (SE) — red wool
        b.setBlock(1, 1, 1, Material.OAK_FENCE);
        b.setBlock(1, 1, 3, Material.OAK_FENCE);
        b.setBlock(3, 1, 1, Material.OAK_FENCE);
        b.setBlock(3, 1, 3, Material.OAK_FENCE);
        b.setBlock(1, 2, 1, Material.OAK_FENCE);
        b.setBlock(3, 2, 3, Material.OAK_FENCE);
        b.fillBox(1, 2, 1, 3, 2, 3, Material.RED_WOOL);
        b.setBlock(2, 3, 2, Material.RED_WOOL);
        b.setBlock(2, 1, 2, Material.GREEN_CARPET);

        // Log seats around fire
        b.setBlock(2, 1, -1, Material.STRIPPED_OAK_LOG);
        b.setBlock(-2, 1, 1, Material.STRIPPED_OAK_LOG);
        b.setBlock(0, 1, 2, Material.STRIPPED_OAK_LOG);

        // Supply chest
        b.placeChest(4, 1, 0);

        // Lantern on a fence
        b.setBlock(-4, 1, 2, Material.OAK_FENCE);
        b.setBlock(-4, 2, 2, Material.LANTERN);

        // Flower pots for ambiance
        b.setBlock(4, 1, -3, Material.POTTED_POPPY);
        b.setBlock(-4, 1, -2, Material.POTTED_DANDELION);
    }
}

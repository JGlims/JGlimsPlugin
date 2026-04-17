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
        // Ground: natural clearing with dirt path and scattered podzol
        b.filledCircle(0, 0, 0, 6, Material.GRASS_BLOCK);
        b.scatter(-6, 0, -6, 6, 0, 6, Material.GRASS_BLOCK, Material.COARSE_DIRT, 0.25);
        b.scatter(-6, 0, -6, 6, 0, 6, Material.GRASS_BLOCK, Material.PODZOL, 0.08);
        // Foot-worn dirt path from south entrance to campfire
        for (int z = -7; z <= 0; z++) { b.setBlock(0, 0, z, Material.DIRT_PATH); b.setBlock(1, 0, z, Material.DIRT_PATH); }

        // ── Central firepit with stone ring and cooking spit ──
        b.circle(0, 0, 0, 2, Material.COBBLESTONE);
        b.setBlock(1, 0, 0, Material.COBBLESTONE_SLAB);
        b.setBlock(-1, 0, 0, Material.COBBLESTONE_SLAB);
        b.setBlock(0, 1, 0, Material.CAMPFIRE);
        // Cooking spit: chain frame over fire
        b.setBlock(-2, 1, 0, Material.OAK_FENCE); b.setBlock(-2, 2, 0, Material.OAK_FENCE);
        b.setBlock(2, 1, 0, Material.OAK_FENCE); b.setBlock(2, 2, 0, Material.OAK_FENCE);
        b.setBlock(-1, 2, 0, Material.IRON_CHAIN); b.setBlock(0, 2, 0, Material.IRON_CHAIN); b.setBlock(1, 2, 0, Material.IRON_CHAIN);

        // ── Tent 1 (NW) — canvas A-frame with bedroll ──
        // Log ridge pole
        b.setBlock(-4, 1, -4, Material.STRIPPED_OAK_LOG); b.setBlock(-4, 1, -2, Material.STRIPPED_OAK_LOG);
        b.setBlock(-2, 1, -4, Material.STRIPPED_OAK_LOG); b.setBlock(-2, 1, -2, Material.STRIPPED_OAK_LOG);
        b.setBlock(-3, 2, -4, Material.STRIPPED_OAK_LOG); b.setBlock(-3, 2, -2, Material.STRIPPED_OAK_LOG);
        b.setBlock(-3, 3, -3, Material.STRIPPED_OAK_LOG);
        // Canvas walls (wool + carpet overhang)
        b.fillBox(-4, 2, -4, -2, 2, -2, Material.WHITE_WOOL);
        b.setBlock(-3, 3, -3, Material.WHITE_WOOL);
        b.setBlock(-4, 2, -3, Material.WHITE_CARPET); b.setBlock(-2, 2, -3, Material.WHITE_CARPET);
        // Bedroll inside
        b.setBlock(-3, 1, -3, Material.BROWN_CARPET); b.setBlock(-3, 1, -2, Material.BROWN_CARPET);
        b.setBlock(-4, 1, -3, Material.LIGHT_GRAY_CARPET);

        // ── Tent 2 (SE) — red canvas with sleeping bag ──
        b.setBlock(2, 1, 2, Material.STRIPPED_SPRUCE_LOG); b.setBlock(2, 1, 4, Material.STRIPPED_SPRUCE_LOG);
        b.setBlock(4, 1, 2, Material.STRIPPED_SPRUCE_LOG); b.setBlock(4, 1, 4, Material.STRIPPED_SPRUCE_LOG);
        b.setBlock(3, 2, 2, Material.STRIPPED_SPRUCE_LOG); b.setBlock(3, 2, 4, Material.STRIPPED_SPRUCE_LOG);
        b.setBlock(3, 3, 3, Material.STRIPPED_SPRUCE_LOG);
        b.fillBox(2, 2, 2, 4, 2, 4, Material.RED_WOOL);
        b.setBlock(3, 3, 3, Material.RED_WOOL);
        b.setBlock(2, 2, 3, Material.RED_CARPET); b.setBlock(4, 2, 3, Material.RED_CARPET);
        b.setBlock(3, 1, 3, Material.GREEN_CARPET); b.setBlock(3, 1, 4, Material.GREEN_CARPET);

        // ── Seating: log stumps and stripped logs around fire ──
        b.setBlock(2, 1, -1, Material.STRIPPED_OAK_LOG);
        b.setBlock(-2, 1, 1, Material.STRIPPED_OAK_LOG);
        b.setBlock(0, 1, 2, Material.STRIPPED_SPRUCE_LOG);
        b.setBlock(1, 1, -2, Material.OAK_LOG); // Tree stump seat

        // ── Supply area (NE): chest, barrels, crafting ──
        b.placeChest(4, 1, -2);
        b.setBlock(5, 1, -2, Material.BARREL);
        b.setBlock(5, 1, -3, Material.BARREL);
        b.setBlock(4, 1, -3, Material.CRAFTING_TABLE);
        // Stacked crates (barrels)
        b.setBlock(5, 2, -2, Material.BARREL);

        // ── Lanterns on fence posts (perimeter lighting) ──
        b.setBlock(-5, 1, 3, Material.OAK_FENCE); b.setBlock(-5, 2, 3, Material.LANTERN);
        b.setBlock(5, 1, 1, Material.OAK_FENCE); b.setBlock(5, 2, 1, Material.LANTERN);
        b.setBlock(-1, 1, -6, Material.OAK_FENCE); b.setBlock(-1, 2, -6, Material.LANTERN);

        // ── Drying rack (clothesline between two fence posts) ──
        b.setBlock(-5, 1, -1, Material.OAK_FENCE); b.setBlock(-5, 2, -1, Material.OAK_FENCE);
        b.setBlock(-5, 1, -4, Material.OAK_FENCE); b.setBlock(-5, 2, -4, Material.OAK_FENCE);
        b.setBlock(-5, 3, -1, Material.OAK_FENCE); b.setBlock(-5, 3, -4, Material.OAK_FENCE);
        b.setBlock(-5, 3, -2, Material.TRIPWIRE); b.setBlock(-5, 3, -3, Material.TRIPWIRE);

        // ── Scattered flora for natural look ──
        b.setBlock(5, 1, 4, Material.POTTED_POPPY);
        b.setBlock(-6, 1, -5, Material.SHORT_GRASS);
        b.setBlock(6, 1, -1, Material.SHORT_GRASS);
        b.setBlock(-6, 1, 1, Material.FERN);
        b.setBlock(4, 1, 5, Material.DANDELION);
        b.setBlock(-3, 1, 5, Material.CORNFLOWER);

        // ── Woodpile ──
        b.setBlock(4, 1, 4, Material.OAK_LOG);
        b.setBlock(5, 1, 4, Material.OAK_LOG);
        b.setBlock(4, 2, 4, Material.OAK_LOG);
    }
}

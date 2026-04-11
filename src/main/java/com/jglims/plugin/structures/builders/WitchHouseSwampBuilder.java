package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;

/**
 * WitchHouseSwampBuilder — mangrove swamp witch hut on stilts over water with lily pad path and brewing area.
 *
 * <p>Extracted verbatim from the original {@code OverworldStructureBuilders.buildWitchHouseSwamp}
 * method to give the structure its own file per the one-class-per-structure
 * architecture.
 */
public final class WitchHouseSwampBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // Stilts over water (mangrove roots)
        for (int[] p : new int[][]{{-5, -5}, {5, -5}, {-5, 5}, {5, 5}, {0, -5}, {0, 5}}) {
            b.pillar(p[0], -4, 0, p[1], Material.MANGROVE_LOG);
        }

        // Lily pad path leading to the house
        for (int z = -10; z <= -6; z++) {
            b.setBlock(0, -3, z, Material.LILY_PAD);
            if (z % 2 == 0) b.setBlock(1, -3, z, Material.LILY_PAD);
        }

        // Floor
        b.fillBox(-5, 0, -5, 5, 0, 5, Material.MANGROVE_PLANKS);

        // Walls with mangrove planks
        b.fillWalls(-5, 1, -5, 5, 5, 5, Material.MANGROVE_PLANKS);
        b.hollowBox(-5, 1, -5, 5, 5, 5);
        // Mangrove log trim
        b.pillar(-5, 1, 5, -5, Material.MANGROVE_LOG);
        b.pillar(5, 1, 5, -5, Material.MANGROVE_LOG);
        b.pillar(-5, 1, 5, 5, Material.MANGROVE_LOG);
        b.pillar(5, 1, 5, 5, Material.MANGROVE_LOG);

        // Sloped roof
        b.pyramidRoof(-6, -6, 6, 6, 6, Material.MANGROVE_PLANKS);
        b.setBlock(0, 10, 0, Material.MANGROVE_LOG);

        // Windows
        b.setBlock(-5, 3, 0, Material.PURPLE_STAINED_GLASS_PANE);
        b.setBlock(5, 3, 0, Material.PURPLE_STAINED_GLASS_PANE);
        b.setBlock(0, 3, 5, Material.PURPLE_STAINED_GLASS_PANE);

        // Door
        b.setBlock(0, 1, -5, Material.AIR); b.setBlock(0, 2, -5, Material.AIR);

        // Interior: bubbling cauldron
        b.setBlock(0, 1, 0, Material.CAULDRON);
        b.setBlock(0, 0, 0, Material.CAMPFIRE);  // fire under cauldron

        // Potion shelves
        b.setBlock(-4, 1, 4, Material.BOOKSHELF);
        b.setBlock(-3, 1, 4, Material.BOOKSHELF);
        b.setBlock(-4, 2, 4, Material.BOOKSHELF);
        b.setBlock(-3, 2, 4, Material.BOOKSHELF);

        // Brewing stands
        b.setBlock(3, 1, 3, Material.BREWING_STAND);
        b.setBlock(4, 1, 3, Material.BREWING_STAND);

        // Moss carpet floor accents
        b.setBlock(-2, 1, -2, Material.MOSS_CARPET);
        b.setBlock(2, 1, 2, Material.MOSS_CARPET);
        b.setBlock(-3, 1, 1, Material.MOSS_CARPET);

        // Hanging plants
        b.setBlock(-4, 4, -4, Material.COBWEB);
        b.setBlock(4, 4, 4, Material.COBWEB);

        // Lantern lighting
        b.setBlock(-3, 4, 0, Material.LANTERN);
        b.setBlock(3, 4, 0, Material.LANTERN);

        // Potted mushrooms
        b.setBlock(4, 1, -3, Material.POTTED_RED_MUSHROOM);
        b.setBlock(-4, 1, -3, Material.POTTED_BROWN_MUSHROOM);

        // Exterior vines
        b.addVines(-6, -2, -6, 6, 6, 6, 0.2);

        b.placeChest(-3, 1, -3);
        b.setBossSpawn(0, 1, 2);
    }
}

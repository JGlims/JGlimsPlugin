package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;

/**
 * WitchHouseForestBuilder — stilted dark forest witch cottage with cauldrons, brewing stands, and mushroom garden.
 *
 * <p>Extracted verbatim from the original {@code OverworldStructureBuilders.buildWitchHouseForest}
 * method to give the structure its own file per the one-class-per-structure
 * architecture.
 */
public final class WitchHouseForestBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // Stilts (dark oak logs)
        b.pillar(-4, -2, 0, -4, Material.DARK_OAK_LOG);
        b.pillar(4, -2, 0, -4, Material.DARK_OAK_LOG);
        b.pillar(-4, -2, 0, 4, Material.DARK_OAK_LOG);
        b.pillar(4, -2, 0, 4, Material.DARK_OAK_LOG);

        // Platform and floor
        b.fillBox(-5, 0, -5, 5, 0, 5, Material.SPRUCE_PLANKS);

        // Walls (spruce planks with dark oak trim)
        b.fillWalls(-5, 1, -5, 5, 5, 5, Material.SPRUCE_PLANKS);
        b.hollowBox(-5, 1, -5, 5, 5, 5);
        // Corner trim
        b.pillar(-5, 1, 5, -5, Material.DARK_OAK_LOG);
        b.pillar(5, 1, 5, -5, Material.DARK_OAK_LOG);
        b.pillar(-5, 1, 5, 5, Material.DARK_OAK_LOG);
        b.pillar(5, 1, 5, 5, Material.DARK_OAK_LOG);

        // Pyramid roof with spruce stairs
        b.pyramidRoof(-6, -6, 6, 6, 6, Material.SPRUCE_PLANKS);

        // Purple stained glass windows
        b.setBlock(-5, 3, 0, Material.PURPLE_STAINED_GLASS);
        b.setBlock(5, 3, 0, Material.PURPLE_STAINED_GLASS);
        b.setBlock(0, 3, 5, Material.PURPLE_STAINED_GLASS);
        b.setBlock(-5, 4, -2, Material.PURPLE_STAINED_GLASS);
        b.setBlock(5, 4, 2, Material.PURPLE_STAINED_GLASS);

        // Door
        b.setBlock(0, 1, -5, Material.AIR); b.setBlock(0, 2, -5, Material.AIR);

        // Interior: cauldron, brewing stands, witch supplies
        b.setBlock(-3, 1, -3, Material.CAULDRON);
        b.setBlock(-3, 1, 3, Material.BREWING_STAND);
        b.setBlock(3, 1, 3, Material.BREWING_STAND);
        b.setBlock(3, 1, -3, Material.ENCHANTING_TABLE);

        // Potion shelves
        b.bookshelfWall(-4, 1, 4, -2, 3);

        // Mushroom garden below the house
        for (int x = -3; x <= 3; x += 2) {
            b.setBlock(x, -1, -6, b.getRandom().nextBoolean() ? Material.RED_MUSHROOM : Material.BROWN_MUSHROOM);
        }
        b.setBlock(-5, -1, 0, Material.RED_MUSHROOM);
        b.setBlock(5, -1, 0, Material.BROWN_MUSHROOM);

        // Hanging lanterns under eaves
        b.setBlock(-5, 0, -5, Material.IRON_CHAIN); b.setBlock(-5, -1, -5, Material.LANTERN);
        b.setBlock(5, 0, -5, Material.IRON_CHAIN); b.setBlock(5, -1, -5, Material.LANTERN);
        b.setBlock(5, 0, 5, Material.IRON_CHAIN); b.setBlock(5, -1, 5, Material.LANTERN);

        // Jack o'lantern for eerie light
        b.setBlock(0, 1, 3, Material.JACK_O_LANTERN);

        // Cobwebs in corners
        b.setBlock(-4, 4, -4, Material.COBWEB);
        b.setBlock(4, 4, 4, Material.COBWEB);

        // Flower pots with dead bushes
        b.setBlock(2, 1, -4, Material.POTTED_DEAD_BUSH);
        b.setBlock(-2, 1, 4, Material.POTTED_DEAD_BUSH);

        b.placeChest(2, 1, -2);
        b.setBossSpawn(0, 1, 0);
    }
}

package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;

/**
 * OgrinHutBuilder — oversized swampland ogre hut with cauldron, pumpkin patch, and fishing dock.
 *
 * <p>Extracted verbatim from the original monolithic builder method.
 */
public final class OgrinHutBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // Swamp ground
        b.filledCircle(0, 0, 0, 10, Material.MUD);
        b.scatter(-10, 0, -10, 10, 0, 10, Material.MUD, Material.DIRT, 0.2);

        // Oversized hut (big for an ogre!)
        b.fillBox(-6, 0, -5, 6, 0, 5, Material.OAK_PLANKS);
        b.fillWalls(-6, 1, -5, 6, 7, 5, Material.SPRUCE_LOG);
        b.hollowBox(-6, 1, -5, 6, 7, 5);

        // Mud floor accents
        b.scatter(-5, 0, -4, 5, 0, 4, Material.OAK_PLANKS, Material.MUD, 0.3);

        // Oversized door (3 wide, 4 tall)
        b.fillBox(-1, 1, -5, 1, 4, -5, Material.AIR);

        // Messy conical roof
        b.pyramidRoof(-7, -6, 7, 6, 8, Material.SPRUCE_PLANKS);
        b.scatter(-7, 8, -6, 7, 12, 6, Material.SPRUCE_PLANKS, Material.MOSS_BLOCK, 0.15);

        // Windows (large, with shutters)
        b.setBlock(-6, 4, 0, Material.GLASS_PANE);
        b.setBlock(-6, 5, 0, Material.GLASS_PANE);
        b.setBlock(6, 4, 0, Material.GLASS_PANE);
        b.setBlock(6, 5, 0, Material.GLASS_PANE);

        // Interior: cauldron "soup"
        b.setBlock(0, 1, 2, Material.CAULDRON);
        b.setBlock(0, 0, 2, Material.CAMPFIRE);
        b.setBlock(1, 1, 2, Material.BARREL);  // Ingredients

        // Oversized bed (wool on planks)
        b.fillBox(-4, 1, 2, -2, 1, 4, Material.OAK_PLANKS);
        b.fillBox(-4, 2, 2, -2, 2, 4, Material.GREEN_WOOL);

        // Table with food
        b.setBlock(3, 1, 0, Material.OAK_FENCE);
        b.setBlock(3, 2, 0, Material.OAK_PRESSURE_PLATE);
        b.setBlock(4, 1, 0, Material.OAK_FENCE);
        b.setBlock(4, 2, 0, Material.OAK_PRESSURE_PLATE);
        b.setBlock(3, 1, -3, Material.BARREL);

        // "Onion" garden (potatoes and carrots)
        b.fillBox(8, 0, -5, 12, 0, -1, Material.FARMLAND);
        b.setBlock(9, 1, -4, Material.POTATOES);
        b.setBlock(10, 1, -4, Material.POTATOES);
        b.setBlock(11, 1, -4, Material.POTATOES);
        b.setBlock(9, 1, -2, Material.CARROTS);
        b.setBlock(10, 1, -2, Material.CARROTS);
        b.setBlock(11, 1, -2, Material.CARVED_PUMPKIN);

        // Fishing dock
        b.fillBox(-8, 0, 6, -4, 0, 10, Material.OAK_PLANKS);
        b.pillar(-8, -2, 0, 6, Material.OAK_LOG);
        b.pillar(-4, -2, 0, 6, Material.OAK_LOG);
        b.pillar(-8, -2, 0, 10, Material.OAK_LOG);
        b.pillar(-4, -2, 0, 10, Material.OAK_LOG);
        b.setBlock(-6, 1, 10, Material.OAK_FENCE);
        b.setBlock(-6, 2, 10, Material.OAK_FENCE);  // "Fishing rod holder"

        // Funny details: pile of bones (the ogre's meals)
        b.setBlock(4, 1, 3, Material.BONE_BLOCK);
        b.setBlock(5, 1, 3, Material.BONE_BLOCK);
        b.setBlock(4, 2, 3, Material.BONE_BLOCK);

        // Lanterns
        b.setBlock(0, 6, 0, Material.LANTERN);
        b.setBlock(-5, 1, -4, Material.LANTERN);

        // Mushrooms around the hut
        b.setBlock(7, 1, 3, Material.RED_MUSHROOM);
        b.setBlock(-7, 1, -3, Material.BROWN_MUSHROOM);
        b.setBlock(8, 1, 5, Material.RED_MUSHROOM);

        b.placeChest(4, 1, -2);
        b.setBossSpawn(0, 1, -7);
    }
}

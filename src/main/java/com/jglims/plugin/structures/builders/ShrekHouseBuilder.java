package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

/**
 * ShrekHouseBuilder — a swampy round oak log cabin with conical roof, outhouse,
 * vegetable garden, mud path, and a nearby swamp tree. Inspired by Shrek's home.
 *
 * <p>Extracted verbatim from the original {@code OverworldStructureBuilders.buildShrekHouse}
 * method to give the structure its own file per the one-class-per-structure
 * architecture.
 */
public final class ShrekHouseBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // Swamp ground
        b.filledCircle(0, 0, 0, 10, Material.DIRT);
        b.scatter(-10, 0, -10, 10, 0, 10, Material.DIRT, Material.MUD, 0.3);

        // Round-ish log cabin (octagonal walls)
        b.fillWalls(-5, 1, -4, 5, 5, 4, Material.OAK_LOG);
        b.fillWalls(-4, 1, -5, 4, 5, 5, Material.OAK_LOG);
        b.hollowBox(-5, 1, -4, 5, 5, 4);
        b.hollowBox(-4, 1, -5, 4, 5, 5);
        // Corner fills for roundness
        b.pillar(-5, 1, 5, -4, Material.OAK_LOG);
        b.pillar(5, 1, 5, -4, Material.OAK_LOG);
        b.pillar(-5, 1, 5, 4, Material.OAK_LOG);
        b.pillar(5, 1, 5, 4, Material.OAK_LOG);

        // Floor
        b.fillBox(-4, 0, -4, 4, 0, 4, Material.OAK_PLANKS);

        // Layered conical roof
        b.fillBox(-6, 6, -5, 6, 6, 5, Material.OAK_PLANKS);
        b.fillBox(-5, 6, -6, 5, 6, 6, Material.OAK_PLANKS);
        b.fillBox(-5, 7, -4, 5, 7, 4, Material.OAK_PLANKS);
        b.fillBox(-4, 7, -5, 4, 7, 5, Material.OAK_PLANKS);
        b.fillBox(-4, 8, -3, 4, 8, 3, Material.OAK_PLANKS);
        b.fillBox(-3, 8, -4, 3, 8, 4, Material.OAK_PLANKS);
        b.fillBox(-3, 9, -2, 3, 9, 2, Material.OAK_PLANKS);
        b.fillBox(-2, 10, -1, 2, 10, 1, Material.OAK_PLANKS);
        b.setBlock(0, 11, 0, Material.OAK_PLANKS);

        // Chimney
        b.pillar(3, 6, 12, 3, Material.COBBLESTONE);
        b.setBlock(3, 12, 3, Material.COBBLESTONE_WALL);
        b.setBlock(3, 11, 3, Material.CAMPFIRE);

        // Door
        b.setBlock(0, 1, -5, Material.AIR); b.setBlock(0, 2, -5, Material.AIR);

        // Windows
        b.setBlock(-5, 3, 0, Material.GLASS_PANE);
        b.setBlock(5, 3, 0, Material.GLASS_PANE);
        b.setBlock(0, 3, 4, Material.GLASS_PANE);

        // Interior furnishing
        b.setBlock(-3, 1, 2, Material.CRAFTING_TABLE);
        b.setBlock(-3, 1, 3, Material.FURNACE);
        b.setBlock(3, 1, 3, Material.CAULDRON);
        b.table(0, 1, 0);
        b.chair(-1, 1, 0, Material.OAK_STAIRS, BlockFace.EAST);
        b.chair(1, 1, 0, Material.OAK_STAIRS, BlockFace.WEST);
        b.setBlock(-3, 1, -3, Material.RED_BED);
        b.setBlock(2, 1, -3, Material.BARREL);

        // "Keep Out" sign
        b.setBlock(0, 2, -6, Material.OAK_SIGN);

        // Outhouse (small structure to the side)
        b.fillBox(8, 0, -2, 10, 0, 2, Material.OAK_PLANKS);
        b.fillWalls(8, 1, -2, 10, 4, 2, Material.OAK_PLANKS);
        b.hollowBox(8, 1, -2, 10, 4, 2);
        b.setBlock(9, 1, -2, Material.AIR); b.setBlock(9, 2, -2, Material.AIR);
        b.setBlock(9, 4, 0, Material.OAK_PLANKS);

        // Onion garden (using carved pumpkins/potatoes)
        b.fillBox(-7, 0, -7, -4, 0, -4, Material.FARMLAND);
        b.setBlock(-6, 1, -6, Material.POTATOES);
        b.setBlock(-5, 1, -6, Material.POTATOES);
        b.setBlock(-6, 1, -5, Material.CARROTS);
        b.setBlock(-5, 1, -5, Material.CARVED_PUMPKIN);

        // Mud path
        b.setBlock(0, 0, -7, Material.MUD);
        b.setBlock(0, 0, -8, Material.MUD);
        b.setBlock(1, 0, -9, Material.MUD);
        b.setBlock(0, 0, -9, Material.MUD);

        // Swamp tree nearby
        b.pillar(-8, 1, 8, 6, Material.OAK_LOG);
        for (int y = 5; y <= 9; y++) b.filledCircle(-8, y, 6, 3, Material.OAK_LEAVES);
        b.addVines(-11, 3, 3, -5, 9, 9, 0.3);

        // Mushrooms
        b.setBlock(-8, 1, -1, Material.RED_MUSHROOM);
        b.setBlock(-9, 1, 2, Material.BROWN_MUSHROOM);
        b.setBlock(7, 1, 4, Material.RED_MUSHROOM);

        b.placeChest(0, 1, 2);
        b.setBossSpawn(0, 1, -7);
    }
}

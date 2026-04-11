package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

/**
 * AbandonedHouseBuilder — derelict two-story cottage with collapsed roof, cobwebs, and hidden basement.
 *
 * <p>Extracted verbatim from the original {@code OverworldStructureBuilders.buildAbandonedHouse}
 * method to give the structure its own file per the one-class-per-structure
 * architecture.
 */
public final class AbandonedHouseBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // Foundation
        b.fillBox(-6, 0, -6, 6, 0, 6, Material.COBBLESTONE);
        b.scatter(-6, 0, -6, 6, 0, 6, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE, 0.4);

        // First floor walls
        b.fillWalls(-6, 1, -6, 6, 5, 6, Material.OAK_PLANKS);
        b.hollowBox(-6, 1, -6, 6, 5, 6);

        // Mossy and rotting wood patches
        b.scatter(-6, 1, -6, 6, 5, 6, Material.OAK_PLANKS, Material.MOSSY_COBBLESTONE, 0.15);
        b.scatter(-6, 1, -6, 6, 5, 6, Material.OAK_PLANKS, Material.SPRUCE_PLANKS, 0.1);

        // Second floor
        b.fillBox(-5, 5, -5, 5, 5, 5, Material.OAK_PLANKS);
        b.fillWalls(-5, 6, -5, 5, 9, 5, Material.OAK_PLANKS);
        b.hollowBox(-5, 6, -5, 5, 9, 5);

        // Gabled roof
        b.gabledRoof(-6, -6, 6, 6, 10, Material.SPRUCE_STAIRS, Material.SPRUCE_SLAB);

        // Collapsed roof section (NE corner)
        b.decay(2, 10, -6, 6, 13, 0, 0.7);
        b.decay(3, 6, -5, 5, 9, -1, 0.4);

        // Broken windows (air gaps with some glass pane remnants)
        b.setBlock(-6, 3, 0, Material.AIR); b.setBlock(-6, 4, 0, Material.AIR);
        b.setBlock(6, 3, -3, Material.AIR); b.setBlock(6, 4, -3, Material.GLASS_PANE);
        b.setBlock(0, 3, -6, Material.GLASS_PANE); b.setBlock(0, 4, -6, Material.AIR);
        b.setBlock(0, 7, 6, Material.AIR); b.setBlock(0, 8, 6, Material.GLASS_PANE);
        b.setBlock(-5, 7, 0, Material.AIR);

        // Door (broken — just open)
        b.setBlock(0, 1, -6, Material.AIR); b.setBlock(0, 2, -6, Material.AIR);

        // Interior first floor: rotting furniture
        b.setBlock(-4, 1, -4, Material.CRAFTING_TABLE);
        b.setBlock(-3, 1, -4, Material.OAK_FENCE); b.setBlock(-3, 2, -4, Material.OAK_PRESSURE_PLATE);
        b.chair(-2, 1, -4, Material.OAK_STAIRS, BlockFace.NORTH);
        b.setBlock(4, 1, -4, Material.FURNACE);
        b.setBlock(3, 1, 4, Material.CAULDRON);

        // Cobwebs
        b.scatter(-5, 1, -5, 5, 5, 5, Material.AIR, Material.COBWEB, 0.08);
        b.scatter(-4, 6, -4, 4, 9, 4, Material.AIR, Material.COBWEB, 0.06);

        // Ladder to second floor
        for (int y = 1; y <= 5; y++) b.setBlock(4, y, 4, Material.LADDER);

        // Second floor: broken bed
        b.setBlock(-3, 6, 3, Material.RED_BED);
        b.setBlock(-2, 6, 2, Material.BOOKSHELF);
        b.setBlock(-2, 7, 2, Material.BOOKSHELF);

        // Hidden basement: dig down from trapdoor
        b.setBlock(0, 0, 0, Material.OAK_TRAPDOOR);
        b.fillBox(-3, -3, -3, 3, -1, 3, Material.COBBLESTONE);
        b.fillBox(-2, -2, -2, 2, -1, 2, Material.AIR);
        b.fillFloor(-2, -2, 2, 2, -3, Material.MOSSY_COBBLESTONE);
        b.setBlock(-2, -2, -2, Material.SOUL_LANTERN);
        b.placeChest(0, -2, 0);

        // Vines on exterior
        b.addVines(-7, 1, -7, 7, 10, 7, 0.15);

        b.setBossSpawn(0, 1, 2);
    }
}

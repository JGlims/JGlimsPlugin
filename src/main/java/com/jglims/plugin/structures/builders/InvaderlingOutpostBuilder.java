package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

/**
 * InvaderlingOutpostBuilder — small gray concrete military base with barracks, command room, and antenna.
 *
 * <p>Extracted verbatim from the original monolithic builder method.
 */
public final class InvaderlingOutpostBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // Foundation
        b.fillBox(-9, 0, -9, 9, 0, 9, Material.GRAY_CONCRETE);

        // Walls
        b.fillWalls(-9, 1, -9, 9, 6, 9, Material.GRAY_CONCRETE);
        b.hollowBox(-9, 1, -9, 9, 6, 9);

        // Reinforced glass windows
        for (int x = -6; x <= 6; x += 4) {
            b.setBlock(x, 3, -9, Material.GRAY_STAINED_GLASS);
            b.setBlock(x, 4, -9, Material.GRAY_STAINED_GLASS);
            b.setBlock(x, 3, 9, Material.GRAY_STAINED_GLASS);
            b.setBlock(x, 4, 9, Material.GRAY_STAINED_GLASS);
        }

        // Floor
        b.fillFloor(-8, -8, 8, 8, 0, Material.LIGHT_GRAY_CONCRETE);

        // Antenna (corner, iron bars + end rod)
        b.pillar(8, 7, 12, 8, Material.IRON_BARS);
        b.setBlock(8, 13, 8, Material.END_ROD);
        b.setBlock(8, 14, 8, Material.LIGHTNING_ROD);

        // Barracks (south half)
        b.fillBox(-8, 0, 2, -2, 5, 8, Material.GRAY_CONCRETE);
        b.fillBox(-7, 1, 3, -3, 4, 7, Material.AIR);
        b.setBlock(-6, 1, 5, Material.WHITE_BED);
        b.setBlock(-4, 1, 5, Material.WHITE_BED);
        b.setBlock(-5, 4, 5, Material.LANTERN);

        // Command room (north)
        b.fillBox(-8, 0, -8, 0, 5, -2, Material.GRAY_CONCRETE);
        b.fillBox(-7, 1, -7, -1, 4, -3, Material.AIR);
        b.table(-4, 1, -5);
        b.chair(-3, 1, -5, Material.POLISHED_ANDESITE_STAIRS, BlockFace.WEST);
        b.setBlock(-6, 1, -7, Material.REDSTONE_LAMP);
        b.setBlock(-2, 1, -7, Material.LEVER);

        // Supply storage (east)
        b.fillBox(3, 0, -8, 8, 5, 0, Material.GRAY_CONCRETE);
        b.fillBox(4, 1, -7, 7, 4, -1, Material.AIR);
        b.setBlock(5, 1, -5, Material.BARREL);
        b.setBlock(6, 1, -5, Material.BARREL);
        b.setBlock(7, 1, -5, Material.BARREL);

        // Ceiling
        b.fillFloor(-8, -8, 8, 8, 6, Material.GRAY_CONCRETE);

        // Entrance
        b.setBlock(0, 1, -9, Material.AIR);
        b.setBlock(0, 2, -9, Material.AIR);
        b.setBlock(1, 1, -9, Material.AIR);
        b.setBlock(1, 2, -9, Material.AIR);

        // Lighting
        b.chandelier(0, 6, 0, 1);

        b.placeChest(6, 1, -3);
        b.placeChest(-6, 1, 3);
        b.setBossSpawn(0, 1, 0);
    }
}

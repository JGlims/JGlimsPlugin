package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;

/**
 * LunarBaseBuilder — abandoned white concrete moon base with airlock, hydroponics, and oxygen tanks.
 *
 * <p>Extracted verbatim from the original monolithic builder method.
 */
public final class LunarBaseBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // Main building shell
        b.fillBox(-18, 0, -18, 18, 8, 18, Material.WHITE_CONCRETE);
        b.fillBox(-17, 1, -17, 17, 7, 17, Material.AIR);
        b.fillFloor(-17, -17, 17, 17, 0, Material.LIGHT_GRAY_CONCRETE);

        // Airlock (south entrance, double iron doors)
        b.fillBox(-3, 0, -18, 3, 6, -14, Material.GRAY_CONCRETE);
        b.fillBox(-2, 1, -17, 2, 5, -15, Material.AIR);
        b.setBlock(-1, 1, -18, Material.IRON_DOOR);
        b.setBlock(1, 1, -18, Material.IRON_DOOR);
        b.setBlock(-1, 1, -14, Material.IRON_DOOR);
        b.setBlock(1, 1, -14, Material.IRON_DOOR);

        // Corridor (central)
        b.fillBox(-2, 1, -14, 2, 5, 14, Material.AIR);
        b.fillBox(-2, 0, -14, 2, 0, 14, Material.LIGHT_GRAY_CONCRETE);

        // Hydroponics bay (NE)
        b.fillBox(4, 0, -16, 16, 7, -4, Material.WHITE_CONCRETE);
        b.fillBox(5, 1, -15, 15, 6, -5, Material.AIR);
        b.fillFloor(5, -15, 15, -5, 0, Material.LIGHT_GRAY_CONCRETE);
        // Farmland plots (dead crops)
        for (int x = 6; x <= 14; x += 2)
            for (int z = -14; z <= -6; z += 2) {
                b.setBlock(x, 0, z, Material.FARMLAND);
                b.setBlock(x, 1, z, Material.DEAD_BUSH);
            }
        // Grow lights
        b.wallLighting(5, 5, -15, 15, -15, 3, Material.SEA_LANTERN);

        // Crew quarters (NW)
        b.fillBox(-16, 0, -16, -4, 7, -4, Material.WHITE_CONCRETE);
        b.fillBox(-15, 1, -15, -5, 6, -5, Material.AIR);
        b.fillFloor(-15, -15, -5, -5, 0, Material.LIGHT_GRAY_CONCRETE);
        for (int z = -14; z <= -6; z += 4) {
            b.setBlock(-12, 1, z, Material.WHITE_BED);
            b.setBlock(-8, 1, z, Material.WHITE_BED);
        }
        b.chandelier(-10, 7, -10, 1);

        // Control room (SE)
        b.fillBox(4, 0, 4, 16, 7, 16, Material.WHITE_CONCRETE);
        b.fillBox(5, 1, 5, 15, 6, 15, Material.AIR);
        b.fillFloor(5, 5, 15, 15, 0, Material.GRAY_CONCRETE);
        // Redstone control panels
        b.fillBox(5, 1, 14, 15, 3, 14, Material.REDSTONE_LAMP);
        b.fillBox(5, 1, 13, 15, 1, 13, Material.SMOOTH_STONE_SLAB);
        b.setBlock(10, 2, 13, Material.LEVER);
        b.setBlock(8, 2, 13, Material.STONE_BUTTON);
        // Map display
        b.setBlock(10, 4, 14, Material.SEA_LANTERN);

        // Oxygen tanks (SW corner — blue glass cylinders)
        b.fillBox(-16, 0, 4, -4, 7, 16, Material.WHITE_CONCRETE);
        b.fillBox(-15, 1, 5, -5, 6, 15, Material.AIR);
        // Tanks
        for (int x = -14; x <= -6; x += 4) {
            b.pillar(x, 1, 5, 10, Material.LIGHT_BLUE_STAINED_GLASS);
            b.setBlock(x, 1, 10, Material.IRON_BLOCK);
            b.setBlock(x, 6, 10, Material.IRON_BLOCK);
        }

        // Ceiling
        b.fillFloor(-17, -17, 17, 17, 8, Material.WHITE_CONCRETE);

        // Abandoned details — scattered debris
        b.scatter(-16, 1, -16, 16, 1, 16, Material.AIR, Material.COBWEB, 0.03);

        b.placeChest(10, 1, 10);
        b.placeChest(-12, 1, -12);
        b.placeChest(10, 1, -10);
    }
}

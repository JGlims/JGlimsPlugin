package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

/**
 * AlienCitadelBuilder — large alien fortress with command center, hangar, prison block, lab, and power core.
 *
 * <p>Extracted verbatim from the original monolithic builder method.
 */
public final class AlienCitadelBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // Main platform
        b.fillBox(-40, 0, -20, 40, 0, 20, Material.GRAY_CONCRETE);

        // Outer walls
        b.fillWalls(-40, 1, -20, 40, 16, 20, Material.GRAY_CONCRETE);
        b.hollowBox(-40, 1, -20, 40, 16, 20);

        // Iron reinforcement bands
        for (int y : new int[]{4, 8, 12}) {
            b.fillBox(-40, y, -20, 40, y, -20, Material.IRON_BLOCK);
            b.fillBox(-40, y, 20, 40, y, 20, Material.IRON_BLOCK);
            b.fillBox(-40, y, -20, -40, y, 20, Material.IRON_BLOCK);
            b.fillBox(40, y, -20, 40, y, 20, Material.IRON_BLOCK);
        }

        // Floor
        b.fillFloor(-39, -19, 39, 19, 0, Material.LIGHT_GRAY_CONCRETE);

        // Command center (center-north, elevated)
        b.fillBox(-10, 0, 8, 10, 20, 19, Material.IRON_BLOCK);
        b.fillBox(-9, 1, 9, 9, 19, 18, Material.AIR);
        b.fillFloor(-9, 9, 9, 18, 0, Material.GRAY_CONCRETE);
        // Control panels (redstone)
        b.fillBox(-8, 1, 17, 8, 3, 17, Material.REDSTONE_LAMP);
        b.fillBox(-8, 1, 16, 8, 1, 16, Material.SMOOTH_STONE);
        b.setBlock(0, 2, 16, Material.LEVER);
        // Windows
        for (int x = -7; x <= 7; x += 2) b.setBlock(x, 10, 19, Material.GRAY_STAINED_GLASS_PANE);

        // Hangar bay (west wing)
        b.fillBox(-39, 0, -15, -20, 12, 5, Material.GRAY_CONCRETE);
        b.fillBox(-38, 1, -14, -21, 11, 4, Material.AIR);
        b.fillFloor(-38, -14, -21, 4, 0, Material.SMOOTH_STONE);
        // Hangar doors (open bay on south)
        b.fillBox(-38, 1, -15, -21, 8, -15, Material.AIR);

        // Prison block (east wing)
        b.fillBox(20, 0, -15, 39, 10, 5, Material.GRAY_CONCRETE);
        b.fillBox(21, 1, -14, 38, 9, 4, Material.AIR);
        for (int cell = 0; cell < 4; cell++) {
            int cz = -12 + cell * 4;
            b.fillBox(25, 0, cz, 30, 6, cz + 2, Material.IRON_BLOCK);
            b.fillBox(26, 1, cz, 29, 5, cz + 2, Material.AIR);
            for (int y = 1; y <= 5; y++) b.setBlock(25, y, cz + 1, Material.IRON_BARS);
        }

        // Research lab (southeast)
        b.fillBox(20, 0, 8, 38, 10, 19, Material.GRAY_CONCRETE);
        b.fillBox(21, 1, 9, 37, 9, 18, Material.AIR);
        b.fillFloor(21, 9, 37, 18, 0, Material.WHITE_CONCRETE);
        b.setBlock(25, 1, 14, Material.BREWING_STAND);
        b.setBlock(27, 1, 14, Material.CAULDRON);
        b.setBlock(29, 1, 14, Material.ENCHANTING_TABLE);

        // Power core room (underground center)
        b.fillBox(-8, -8, -8, 8, -1, 8, Material.IRON_BLOCK);
        b.fillBox(-7, -7, -7, 7, -1, 7, Material.AIR);
        b.fillFloor(-7, -7, 7, 7, -8, Material.GRAY_CONCRETE);
        // Power core (sea lanterns surrounded by glass)
        b.fillBox(-2, -7, -2, 2, -3, 2, Material.SEA_LANTERN);
        b.fillBox(-3, -7, -3, 3, -2, 3, Material.LIGHT_BLUE_STAINED_GLASS);
        b.fillBox(-2, -6, -2, 2, -3, 2, Material.SEA_LANTERN);

        // Automated defenses (dispensers on walls)
        for (int x = -36; x <= 36; x += 12) {
            b.setDirectional(x, 8, -20, Material.DISPENSER, BlockFace.SOUTH);
            b.setDirectional(x, 8, 20, Material.DISPENSER, BlockFace.NORTH);
        }

        // Ceiling
        b.fillFloor(-39, -19, 39, 19, 16, Material.GRAY_CONCRETE);

        // Entrance (south, large door)
        b.fillBox(-4, 1, -20, 4, 10, -20, Material.AIR);

        // Glass windows throughout
        for (int x = -35; x <= 35; x += 5) {
            for (int y = 6; y <= 12; y++) {
                b.setBlock(x, y, -20, Material.GRAY_STAINED_GLASS_PANE);
                b.setBlock(x, y, 20, Material.GRAY_STAINED_GLASS_PANE);
            }
        }

        b.placeChest(0, 1, 15);
        b.placeChest(-30, 1, 0);
        b.placeChest(30, 1, -10);
        b.placeChest(0, -7, 0);
        b.setBossSpawn(0, 1, 12);
    }
}

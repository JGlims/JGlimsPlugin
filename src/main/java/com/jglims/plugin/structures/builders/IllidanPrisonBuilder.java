package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

/**
 * IllidanPrisonBuilder — obsidian prison complex with a reinforced central cell
 * showing broken bars (Illidan's escape), observation room above, warden
 * quarters, trophy hall, and a rift tear of end-gateway blocks in the west wing.
 *
 * <p>Extracted verbatim from the original {@code DimensionStructureBuilders.buildIllidanPrison}
 * method to give the structure its own file per the one-class-per-structure
 * architecture.
 */
public final class IllidanPrisonBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // Main prison building
        b.fillBox(-18, 0, -14, 18, 14, 14, Material.OBSIDIAN);
        b.fillBox(-17, 1, -13, 17, 13, 13, Material.AIR);
        b.fillFloor(-17, -13, 17, 13, 0, Material.CRYING_OBSIDIAN);

        // The main cell (center, heavily fortified)
        b.fillBox(-6, 0, -6, 6, 10, 6, Material.OBSIDIAN);
        b.fillBox(-5, 1, -5, 5, 9, 5, Material.AIR);
        b.fillFloor(-5, -5, 5, 5, 0, Material.CRYING_OBSIDIAN);
        // Broken iron bars (gaps representing Illidan's escape)
        for (int y = 1; y <= 6; y++) {
            b.setBlock(-6, y, -2, Material.IRON_BARS);
            b.setBlock(-6, y, 2, Material.IRON_BARS);
            // Broken sections (air)
            b.setBlock(-6, y, -1, Material.AIR);
            b.setBlock(-6, y, 0, Material.AIR);
            b.setBlock(-6, y, 1, Material.AIR);
        }
        // Remnants of chains
        b.setBlock(0, 6, 0, Material.IRON_CHAIN);
        b.setBlock(0, 5, 0, Material.IRON_CHAIN);
        b.setBlock(-3, 5, -3, Material.IRON_CHAIN);
        b.setBlock(3, 5, -3, Material.IRON_CHAIN);

        // Observation room (above cell)
        b.fillBox(-8, 10, -8, 8, 14, 8, Material.OBSIDIAN);
        b.fillBox(-7, 11, -7, 7, 13, 7, Material.AIR);
        // Glass floor to look down
        b.fillFloor(-5, -5, 5, 5, 10, Material.PURPLE_STAINED_GLASS);

        // Warden quarters (east wing)
        b.furnishedRoom(10, 0, -12, 17, -6, 5, Material.OBSIDIAN,
                Material.CRYING_OBSIDIAN, Material.OBSIDIAN);
        b.setBlock(14, 1, -10, Material.RED_BED);

        // Rift tear (end gateway blocks in west wing)
        b.fillBox(-17, 0, -6, -10, 10, 6, Material.OBSIDIAN);
        b.fillBox(-16, 1, -5, -11, 9, 5, Material.AIR);
        b.setBlock(-14, 3, 0, Material.END_GATEWAY);
        b.setBlock(-13, 4, 0, Material.END_GATEWAY);
        b.setBlock(-14, 5, 0, Material.END_GATEWAY);
        // Crying obsidian frame around rift
        b.fillBox(-15, 2, -1, -12, 6, 1, Material.CRYING_OBSIDIAN);
        b.fillBox(-14, 3, 0, -13, 5, 0, Material.AIR);
        b.setBlock(-14, 3, 0, Material.END_GATEWAY);
        b.setBlock(-13, 4, 0, Material.END_GATEWAY);

        // Trophy room (far east)
        b.fillBox(10, 0, 4, 17, 8, 12, Material.OBSIDIAN);
        b.fillBox(11, 1, 5, 16, 7, 11, Material.AIR);
        // Display cases (glass on pedestals)
        for (int x = 12; x <= 15; x += 3) {
            b.setBlock(x, 1, 8, Material.PURPUR_PILLAR);
            b.setBlock(x, 2, 8, Material.GLASS);
        }
        b.setBlock(12, 7, 8, Material.END_ROD);

        // Entrance
        b.archDoorway(0, 1, -14, 4, 6, Material.CRYING_OBSIDIAN);

        // Lighting — sparse, ominous
        b.setBlock(-15, 8, -12, Material.END_ROD);
        b.setBlock(15, 8, -12, Material.END_ROD);
        b.setBlock(-15, 8, 12, Material.END_ROD);
        b.setBlock(15, 8, 12, Material.END_ROD);
        b.chandelier(0, 9, 0, 2);

        b.placeChest(14, 1, 8);
        b.placeChest(-14, 1, 4);
        b.placeChest(0, 1, 0);
        b.setBossSpawn(0, 1, -3);
    }
}

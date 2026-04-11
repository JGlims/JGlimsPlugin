package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;

/**
 * ObservationTowerBuilder — tall iron and glass tower with multiple floors, telescope, and radar dish.
 *
 * <p>Extracted verbatim from the original monolithic builder method.
 */
public final class ObservationTowerBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // Base platform
        b.fillBox(-7, 0, -7, 7, 0, 7, Material.IRON_BLOCK);

        // Tower shaft (thin, 6x6)
        for (int y = 1; y <= 36; y++) {
            b.fillBox(-3, y, -3, 3, y, -3, Material.IRON_BLOCK);
            b.fillBox(-3, y, 3, 3, y, 3, Material.IRON_BLOCK);
            b.fillBox(-3, y, -3, -3, y, 3, Material.IRON_BLOCK);
            b.fillBox(3, y, -3, 3, y, 3, Material.IRON_BLOCK);
            // Interior clear
            b.fillBox(-2, y, -2, 2, y, 2, Material.AIR);
        }

        // Glass windows on each floor
        for (int y = 4; y <= 36; y += 4) {
            b.setBlock(0, y, -3, Material.GLASS_PANE);
            b.setBlock(0, y, 3, Material.GLASS_PANE);
            b.setBlock(-3, y, 0, Material.GLASS_PANE);
            b.setBlock(3, y, 0, Material.GLASS_PANE);
            b.setBlock(0, y + 1, -3, Material.GLASS_PANE);
            b.setBlock(0, y + 1, 3, Material.GLASS_PANE);
        }

        // Observation floors
        for (int fy = 8; fy <= 32; fy += 8) {
            b.fillBox(-2, fy, -2, 2, fy, 2, Material.IRON_BLOCK);
        }

        // Ladder access (inside, along one wall)
        for (int y = 1; y <= 36; y++) b.setBlock(2, y, 0, Material.LADDER);

        // Top observation deck (wider)
        b.fillBox(-5, 36, -5, 5, 36, 5, Material.IRON_BLOCK);
        b.fillBox(-5, 37, -5, 5, 40, -5, Material.IRON_BLOCK);
        b.fillBox(-5, 37, 5, 5, 40, 5, Material.IRON_BLOCK);
        b.fillBox(-5, 37, -5, -5, 40, 5, Material.IRON_BLOCK);
        b.fillBox(5, 37, -5, 5, 40, 5, Material.IRON_BLOCK);
        b.fillBox(-4, 37, -4, 4, 39, 4, Material.AIR);
        // Glass observation windows
        for (int x = -3; x <= 3; x++) {
            b.setBlock(x, 38, -5, Material.GLASS_PANE);
            b.setBlock(x, 38, 5, Material.GLASS_PANE);
            b.setBlock(x, 39, -5, Material.GLASS_PANE);
            b.setBlock(x, 39, 5, Material.GLASS_PANE);
        }

        // Telescope (end rods pointing up)
        b.setBlock(0, 37, -3, Material.END_ROD);
        b.setBlock(0, 38, -3, Material.END_ROD);
        b.setBlock(0, 39, -4, Material.END_ROD);

        // Antenna array on top
        b.pillar(0, 40, 44, 0, Material.IRON_BARS);
        b.setBlock(0, 45, 0, Material.END_ROD);
        b.setBlock(-2, 42, 0, Material.IRON_BARS);
        b.setBlock(2, 42, 0, Material.IRON_BARS);

        // Radar dish (daylight sensor + slab)
        b.setBlock(0, 43, 2, Material.DAYLIGHT_DETECTOR);
        b.setSlab(0, 43, 1, Material.IRON_BLOCK, false);

        // Entrance
        b.setBlock(0, 1, -3, Material.AIR);
        b.setBlock(0, 2, -3, Material.AIR);

        b.placeChest(0, 37, 0);
        b.placeChest(-1, 9, -1);
    }
}

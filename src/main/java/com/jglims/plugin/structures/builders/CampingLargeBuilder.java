package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;

/**
 * CampingLargeBuilder — fortified multi-tent camp with palisade walls, lookout tower, and weapon rack.
 *
 * <p>Extracted verbatim from the original {@code OverworldStructureBuilders.buildCampingLarge}
 * method to give the structure its own file per the one-class-per-structure
 * architecture.
 */
public final class CampingLargeBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // Ground pad
        b.filledCircle(0, 0, 0, 10, Material.GRASS_BLOCK);
        b.scatter(-10, 0, -10, 10, 0, 10, Material.GRASS_BLOCK, Material.COARSE_DIRT, 0.2);

        // Palisade wall (stripped logs)
        b.circle(0, 0, 0, 9, Material.STRIPPED_OAK_LOG);
        for (int a = 0; a < 360; a += 8) {
            int wx = (int) Math.round(9 * Math.cos(Math.toRadians(a)));
            int wz = (int) Math.round(9 * Math.sin(Math.toRadians(a)));
            b.pillar(wx, 1, 3, wz, Material.STRIPPED_OAK_LOG);
        }
        // Gate opening
        b.setBlock(0, 1, -9, Material.AIR); b.setBlock(0, 2, -9, Material.AIR); b.setBlock(0, 3, -9, Material.AIR);
        b.setBlock(1, 1, -9, Material.AIR); b.setBlock(1, 2, -9, Material.AIR); b.setBlock(1, 3, -9, Material.AIR);
        b.setBlock(-1, 4, -9, Material.STRIPPED_OAK_LOG); b.setBlock(0, 4, -9, Material.STRIPPED_OAK_LOG);
        b.setBlock(1, 4, -9, Material.STRIPPED_OAK_LOG); b.setBlock(2, 4, -9, Material.STRIPPED_OAK_LOG);

        // Central firepit
        b.circle(0, 0, 0, 2, Material.COBBLESTONE);
        b.setBlock(0, 1, 0, Material.CAMPFIRE);
        b.setBlock(1, 1, 0, Material.CAMPFIRE);
        b.setBlock(0, 1, 1, Material.CAMPFIRE);

        // 4 tents around the firepit
        int[][] tentPositions = {{-5, -5}, {5, -5}, {-5, 5}, {5, 5}};
        Material[] tentColors = {Material.WHITE_WOOL, Material.RED_WOOL, Material.BLUE_WOOL, Material.GREEN_WOOL};
        for (int i = 0; i < 4; i++) {
            int tx = tentPositions[i][0]; int tz = tentPositions[i][1];
            b.setBlock(tx - 1, 1, tz - 1, Material.OAK_FENCE);
            b.setBlock(tx + 1, 1, tz - 1, Material.OAK_FENCE);
            b.setBlock(tx - 1, 1, tz + 1, Material.OAK_FENCE);
            b.setBlock(tx + 1, 1, tz + 1, Material.OAK_FENCE);
            b.fillBox(tx - 1, 2, tz - 1, tx + 1, 2, tz + 1, tentColors[i]);
            b.setBlock(tx, 3, tz, tentColors[i]);
            b.setBlock(tx, 1, tz, Material.BROWN_CARPET);
        }

        // Lookout tower (NE corner)
        b.fillBox(6, 0, -8, 8, 0, -6, Material.COBBLESTONE);
        b.pillar(6, 1, 7, -8, Material.STRIPPED_OAK_LOG);
        b.pillar(8, 1, 7, -8, Material.STRIPPED_OAK_LOG);
        b.pillar(6, 1, 7, -6, Material.STRIPPED_OAK_LOG);
        b.pillar(8, 1, 7, -6, Material.STRIPPED_OAK_LOG);
        b.fillBox(5, 7, -9, 9, 7, -5, Material.OAK_PLANKS);
        b.circle(7, 8, -7, 2, Material.OAK_FENCE);
        for (int y = 1; y <= 7; y++) b.setBlock(7, y, -6, Material.LADDER);
        b.setBlock(7, 8, -7, Material.LANTERN);

        // Weapon rack area
        b.setBlock(-7, 1, 0, Material.LIGHTNING_ROD);
        b.setBlock(-7, 1, 1, Material.GRINDSTONE);
        b.setBlock(-7, 1, -1, Material.SMITHING_TABLE);

        // Chests
        b.placeChest(3, 1, -3);
        b.placeChest(-3, 1, 3);
        b.setBossSpawn(0, 1, 0);
    }
}

package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;

/**
 * UndergroundHiveBuilder — sculk queen's dome with branching tunnels, egg pods, and larvae pits.
 *
 * <p>Extracted verbatim from the original monolithic builder method.
 */
public final class UndergroundHiveBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // Main queen's chamber (large)
        b.filledDome(0, 0, 0, 16, Material.SCULK);
        b.filledDome(0, 0, 0, 14, Material.AIR);
        b.fillFloor(-14, -14, 14, 14, 0, Material.SCULK);

        // Organic wall variation
        b.scatter(-16, 0, -16, 16, 16, 16, Material.SCULK, Material.DEEPSLATE, 0.15);
        b.scatter(-14, 0, -14, 14, 0, 14, Material.SCULK, Material.SCULK_VEIN, 0.1);

        // Egg pods (soul lanterns in sculk niches)
        for (int a = 0; a < 360; a += 30) {
            int ex = (int) (10 * Math.cos(Math.toRadians(a)));
            int ez = (int) (10 * Math.sin(Math.toRadians(a)));
            b.setBlock(ex, 1, ez, Material.SCULK);
            b.setBlock(ex, 2, ez, Material.SOUL_LANTERN);
            b.setBlock(ex, 3, ez, Material.SCULK);
        }

        // Central nest platform for queen
        b.filledCircle(0, 0, 0, 5, Material.SCULK_CATALYST);
        b.filledCircle(0, 1, 0, 3, Material.SCULK);
        b.setBlock(0, 2, 0, Material.SCULK_SHRIEKER);

        // Interconnected sub-chambers (tunnels branching out)
        int[][] subChambers = {{-20, -15}, {20, -15}, {-18, 18}, {18, 18}};
        for (int[] sc : subChambers) {
            b.filledDome(sc[0], 0, sc[1], 8, Material.SCULK);
            b.filledDome(sc[0], 0, sc[1], 6, Material.AIR);
            b.fillFloor(sc[0] - 6, sc[1] - 6, sc[0] + 6, sc[1] + 6, 0, Material.DEEPSLATE);
            // Egg cluster in each sub-chamber
            b.setBlock(sc[0], 1, sc[1], Material.SOUL_LANTERN);
            b.setBlock(sc[0] - 1, 1, sc[1], Material.SOUL_LANTERN);
            b.setBlock(sc[0] + 1, 1, sc[1], Material.SOUL_LANTERN);

            // Tunnel connecting to main chamber
            int dx = Integer.signum(-sc[0]);
            int dz = Integer.signum(-sc[1]);
            for (int i = 0; i < 8; i++) {
                int tx = sc[0] + dx * (7 + i);
                int tz = sc[1] + dz * (7 + i);
                b.fillBox(tx - 1, 0, tz - 1, tx + 1, 3, tz + 1, Material.AIR);
                // Tunnel walls
                b.setBlock(tx - 2, 0, tz, Material.SCULK);
                b.setBlock(tx + 2, 0, tz, Material.SCULK);
                b.setBlock(tx, 4, tz, Material.SCULK);
            }
        }

        // Sculk sensor proximity detectors
        b.setBlock(-6, 1, 0, Material.SCULK_SENSOR);
        b.setBlock(6, 1, 0, Material.SCULK_SENSOR);
        b.setBlock(0, 1, -6, Material.SCULK_SENSOR);
        b.setBlock(0, 1, 6, Material.SCULK_SENSOR);

        // ─── Hive Queen's throne of sculk on the nest platform ───
        b.setStairs(-1, 2, 0, Material.DEEPSLATE_BRICK_STAIRS, org.bukkit.block.BlockFace.EAST, false);
        b.setStairs(1, 2, 0, Material.DEEPSLATE_BRICK_STAIRS, org.bukkit.block.BlockFace.WEST, false);
        b.setBlock(0, 3, 1, Material.SCULK_CATALYST);

        // ─── Hanging egg sacs (sculk veins + shroomlight cores suspended) ───
        for (int i = 0; i < 12; i++) {
            int a = i * 30;
            int ex = (int) Math.round(8 * Math.cos(Math.toRadians(a)));
            int ez = (int) Math.round(8 * Math.sin(Math.toRadians(a)));
            b.setBlock(ex, 10, ez, Material.SCULK);
            b.setBlock(ex, 9, ez, Material.SHROOMLIGHT);
            b.setBlock(ex, 8, ez, Material.SCULK);
            b.setBlock(ex, 7, ez, Material.SCULK_VEIN);
        }

        // ─── Larvae pits (4 small pits around the main chamber) ───
        for (int[] pit : new int[][]{{-10, -10}, {10, -10}, {-10, 10}, {10, 10}}) {
            b.fillBox(pit[0] - 2, -2, pit[1] - 2, pit[0] + 2, -1, pit[1] + 2, Material.SCULK);
            b.fillBox(pit[0] - 1, -1, pit[1] - 1, pit[0] + 1, -1, pit[1] + 1, Material.AIR);
            b.setBlock(pit[0], -1, pit[1], Material.SCULK_CATALYST);
        }

        // ─── Spore emitters (wind charges + soul fire) marking danger zones ───
        b.setBlock(-4, 1, -4, Material.SOUL_FIRE);
        b.setBlock(4, 1, 4, Material.SOUL_FIRE);
        b.setBlock(-4, 1, 4, Material.SOUL_FIRE);
        b.setBlock(4, 1, -4, Material.SOUL_FIRE);

        // ─── Pulsating hive core (suspended above the queen) ───
        b.setBlock(0, 8, 0, Material.SCULK_CATALYST);
        b.setBlock(0, 9, 0, Material.SHROOMLIGHT);
        b.setBlock(0, 7, 0, Material.SCULK_SHRIEKER);

        // ─── Deepslate armor plating on the ceiling ───
        b.scatter(-12, 12, -12, 12, 14, 12, Material.SCULK, Material.DEEPSLATE_BRICKS, 0.3);

        b.placeChest(0, 1, 5);
        b.placeChest(-18, 1, -15);
        b.placeChest(18, 1, 18);
        b.placeChest(-10, -1, -10);  // larvae pit loot
        b.placeChest(10, -1, 10);
        b.setBossSpawn(0, 2, 0);
    }
}

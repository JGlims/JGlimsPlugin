package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

/**
 * UndergroundHiveBuilder — massive sculk queen's dome with 4 branching sub-chambers,
 * connecting tunnels, egg pods, larvae pits, spore emitters, throne of sculk, hanging
 * egg sacs, sensor-guarded perimeter, deepslate armor plating, pulsating hive core.
 */
public final class UndergroundHiveBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // ── Main queen's chamber (large dome) ───────────────────────────
        b.filledDome(0, 0, 0, 18, Material.SCULK);
        b.filledDome(0, 0, 0, 16, Material.AIR);
        b.fillFloor(-16, -16, 16, 16, 0, Material.SCULK);
        b.scatter(-16, 0, -16, 16, 0, 16, Material.SCULK, Material.SCULK_CATALYST, 0.08);

        // ── Organic wall variation ──────────────────────────────────────
        b.scatter(-18, 0, -18, 18, 18, 18, Material.SCULK, Material.DEEPSLATE, 0.15);
        b.scatter(-16, 0, -16, 16, 0, 16, Material.SCULK, Material.SCULK_VEIN, 0.15);

        // ── Deepslate armor plating on ceiling + walls ──────────────────
        b.scatter(-14, 12, -14, 14, 16, 14, Material.SCULK, Material.DEEPSLATE_BRICKS, 0.35);
        b.scatter(-16, 10, -16, 16, 16, 16, Material.SCULK, Material.REINFORCED_DEEPSLATE, 0.05);

        // ── Egg pods (soul lanterns in sculk niches) around perimeter ───
        for (int a = 0; a < 360; a += 20) {
            int ex = (int) (12 * Math.cos(Math.toRadians(a)));
            int ez = (int) (12 * Math.sin(Math.toRadians(a)));
            b.setBlock(ex, 1, ez, Material.SCULK);
            b.setBlock(ex, 2, ez, Material.SOUL_LANTERN);
            b.setBlock(ex, 3, ez, Material.SCULK);
        }

        // ── Central queen's nest platform ───────────────────────────────
        b.filledCircle(0, 0, 0, 6, Material.SCULK_CATALYST);
        b.filledCircle(0, 1, 0, 4, Material.SCULK);
        b.setBlock(0, 2, 0, Material.SCULK_SHRIEKER);

        // Queen's throne (behind platform)
        b.setStairs(-1, 2, 0, Material.DEEPSLATE_BRICK_STAIRS, BlockFace.EAST, false);
        b.setStairs(1, 2, 0, Material.DEEPSLATE_BRICK_STAIRS, BlockFace.WEST, false);
        b.setBlock(0, 3, 1, Material.SCULK_CATALYST);
        b.pillar(-2, 2, 2, 2, Material.DEEPSLATE_BRICKS);
        b.pillar(2, 2, 2, 2, Material.DEEPSLATE_BRICKS);
        b.setBlock(-2, 5, 2, Material.WITHER_SKELETON_SKULL);
        b.setBlock(2, 5, 2, Material.WITHER_SKELETON_SKULL);

        // ── Pulsating hive core (suspended above the queen) ─────────────
        b.setBlock(0, 8, 0, Material.SCULK_CATALYST);
        b.setBlock(0, 9, 0, Material.SHROOMLIGHT);
        b.setBlock(0, 10, 0, Material.SCULK_SHRIEKER);
        b.setBlock(0, 7, 0, Material.SCULK_SHRIEKER);
        // Core chains
        for (int y = 4; y <= 7; y++) {
            b.setBlock(0, y, 0, Material.IRON_CHAIN);
        }

        // ── 4 sub-chambers with connecting tunnels ──────────────────────
        int[][] subChambers = {{-22, -16}, {22, -16}, {-22, 18}, {22, 18}};
        for (int[] sc : subChambers) {
            b.filledDome(sc[0], 0, sc[1], 9, Material.SCULK);
            b.filledDome(sc[0], 0, sc[1], 7, Material.AIR);
            b.fillFloor(sc[0] - 7, sc[1] - 7, sc[0] + 7, sc[1] + 7, 0, Material.DEEPSLATE);
            b.scatter(sc[0] - 7, 0, sc[1] - 7, sc[0] + 7, 0, sc[1] + 7, Material.DEEPSLATE, Material.SCULK_VEIN, 0.2);
            // Egg cluster in center of sub-chamber
            b.filledCircle(sc[0], 0, sc[1], 2, Material.SCULK);
            b.setBlock(sc[0], 1, sc[1], Material.SOUL_LANTERN);
            b.setBlock(sc[0] - 1, 1, sc[1], Material.SOUL_LANTERN);
            b.setBlock(sc[0] + 1, 1, sc[1], Material.SOUL_LANTERN);
            b.setBlock(sc[0], 1, sc[1] - 1, Material.SCULK_CATALYST);
            b.setBlock(sc[0], 1, sc[1] + 1, Material.SCULK_CATALYST);
            // Sub-chamber throne (smaller)
            b.setBlock(sc[0], 2, sc[1], Material.SCULK_SHRIEKER);
            // Hanging spore sac
            b.setBlock(sc[0], 6, sc[1], Material.SCULK_VEIN);
            b.setBlock(sc[0], 5, sc[1], Material.SHROOMLIGHT);

            // Connecting tunnel to main chamber
            int dx = Integer.signum(-sc[0]);
            int dz = Integer.signum(-sc[1]);
            for (int i = 0; i < 8; i++) {
                int tx = sc[0] + dx * (8 + i);
                int tz = sc[1] + dz * (8 + i);
                b.fillBox(tx - 1, 0, tz - 1, tx + 1, 3, tz + 1, Material.AIR);
                // Tunnel walls
                b.setBlock(tx - 2, 0, tz, Material.SCULK);
                b.setBlock(tx + 2, 0, tz, Material.SCULK);
                b.setBlock(tx, 4, tz, Material.SCULK);
                if (i % 3 == 0) {
                    b.setBlock(tx, 3, tz, Material.SCULK_VEIN);
                    b.setBlock(tx, 1, tz, Material.SCULK_SENSOR);
                }
            }
        }

        // ── Sculk sensor proximity detectors ────────────────────────────
        for (int a = 0; a < 360; a += 45) {
            int sx = (int) Math.round(8 * Math.cos(Math.toRadians(a)));
            int sz = (int) Math.round(8 * Math.sin(Math.toRadians(a)));
            b.setBlock(sx, 1, sz, Material.SCULK_SENSOR);
        }

        // ── Hanging egg sacs (sculk + shroomlight cores) ────────────────
        for (int i = 0; i < 12; i++) {
            int a = i * 30;
            int ex = (int) Math.round(10 * Math.cos(Math.toRadians(a)));
            int ez = (int) Math.round(10 * Math.sin(Math.toRadians(a)));
            b.setBlock(ex, 12, ez, Material.SCULK);
            b.setBlock(ex, 11, ez, Material.SHROOMLIGHT);
            b.setBlock(ex, 10, ez, Material.SCULK);
            b.setBlock(ex, 9, ez, Material.SCULK_VEIN);
        }

        // ── Larvae pits (4 around the main chamber, with catalyst bait) ─
        for (int[] pit : new int[][]{{-11, -11}, {11, -11}, {-11, 11}, {11, 11}}) {
            b.fillBox(pit[0] - 2, -3, pit[1] - 2, pit[0] + 2, -1, pit[1] + 2, Material.SCULK);
            b.fillBox(pit[0] - 1, -2, pit[1] - 1, pit[0] + 1, -1, pit[1] + 1, Material.AIR);
            b.setBlock(pit[0], -2, pit[1], Material.SCULK_CATALYST);
            b.setBlock(pit[0] - 1, -2, pit[1], Material.SOUL_FIRE);
            b.setBlock(pit[0] + 1, -2, pit[1], Material.SOUL_FIRE);
            b.setBlock(pit[0], -2, pit[1] + 1, Material.SCULK_VEIN);
        }

        // ── Spore emitters (soul fire marking danger zones) ─────────────
        for (int[] sf : new int[][]{{-4, -4}, {4, 4}, {-4, 4}, {4, -4}, {-8, 0}, {8, 0}, {0, -8}, {0, 8}}) {
            b.setBlock(sf[0], 1, sf[1], Material.SOUL_FIRE);
        }

        // ── Wall warts (sculk veins climbing inside) ────────────────────
        for (int y = 2; y <= 10; y += 2) {
            for (int a = 0; a < 360; a += 40) {
                int wx = (int) (14 * Math.cos(Math.toRadians(a)));
                int wz = (int) (14 * Math.sin(Math.toRadians(a)));
                b.setBlock(wx, y, wz, Material.SCULK_VEIN);
            }
        }

        // ── Sub-chamber entrance markers (totems) ───────────────────────
        for (int[] sc : subChambers) {
            int dx = Integer.signum(-sc[0]);
            int dz = Integer.signum(-sc[1]);
            int tx = sc[0] + dx * 7;
            int tz = sc[1] + dz * 7;
            b.pillar(tx, 0, 4, tz, Material.DEEPSLATE_BRICKS);
            b.setBlock(tx, 4, tz, Material.SKELETON_SKULL);
        }

        // ── Loot ────────────────────────────────────────────────────────
        b.placeChest(0, 1, 5);         // main chamber
        b.placeChest(-22, 1, -16);     // SW sub-chamber
        b.placeChest(22, 1, -16);      // SE sub-chamber
        b.placeChest(-22, 1, 18);      // NW sub-chamber
        b.placeChest(22, 1, 18);       // NE sub-chamber
        b.placeChest(-11, -1, -11);    // SW larvae pit
        b.placeChest(11, -1, 11);      // NE larvae pit
        b.setBossSpawn(0, 2, 0);
    }
}

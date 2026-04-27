package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;

/**
 * AllaySanctuaryBuilder — glass-domed amethyst sanctuary with copper pillars, central
 * waterfall, floating amethyst chains, mosaic floor, archive nook, 4 display cages,
 * beehive garden, candle circle, floral entrance path, chime hangings, crystal pool.
 */
public final class AllaySanctuaryBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // ── Meadow base ─────────────────────────────────────────────────
        b.filledCircle(0, 0, 0, 14, Material.GRASS_BLOCK);
        b.scatter(-14, 0, -14, 14, 0, 14, Material.GRASS_BLOCK, Material.MOSS_BLOCK, 0.2);
        b.scatter(-14, 0, -14, 14, 0, 14, Material.GRASS_BLOCK, Material.FLOWERING_AZALEA_LEAVES, 0.06);

        // ── Glass dome + foundation ─────────────────────────────────────
        b.dome(0, 1, 0, 9, Material.LIGHT_BLUE_STAINED_GLASS);
        b.filledCircle(0, 0, 0, 9, Material.CALCITE);
        b.circle(0, 0, 0, 7, Material.AMETHYST_BLOCK);
        b.circle(0, 0, 0, 6, Material.POLISHED_DIORITE);

        // ── Mosaic floor pattern (diamond inlay) ────────────────────────
        for (int x = -6; x <= 6; x += 3) {
            for (int z = -6; z <= 6; z += 3) {
                b.setBlockIfAir(x, 0, z, Material.LIGHT_BLUE_STAINED_GLASS);
                b.setBlockIfAir(x + 1, 0, z, Material.PINK_STAINED_GLASS);
                b.setBlockIfAir(x - 1, 0, z, Material.WHITE_STAINED_GLASS);
                b.setBlockIfAir(x, 0, z + 1, Material.WHITE_STAINED_GLASS);
                b.setBlockIfAir(x, 0, z - 1, Material.WHITE_STAINED_GLASS);
            }
        }
        // Center tile
        b.setBlock(0, 0, 0, Material.MAGENTA_STAINED_GLASS);
        b.setBlock(-1, 0, -1, Material.LIGHT_BLUE_STAINED_GLASS);
        b.setBlock(1, 0, -1, Material.PINK_STAINED_GLASS);
        b.setBlock(-1, 0, 1, Material.PINK_STAINED_GLASS);
        b.setBlock(1, 0, 1, Material.LIGHT_BLUE_STAINED_GLASS);

        // ── Interior flowering garden ───────────────────────────────────
        b.setBlock(-2, 1, -2, Material.FLOWERING_AZALEA);
        b.setBlock(2, 1, 2, Material.FLOWERING_AZALEA);
        b.setBlock(-3, 1, 3, Material.AZALEA);
        b.setBlock(3, 1, -3, Material.AZALEA);
        b.setBlock(-2, 1, 2, Material.PINK_TULIP);
        b.setBlock(2, 1, -2, Material.PINK_TULIP);

        // ── Copper pillars (8 around the rim) ───────────────────────────
        for (int a = 0; a < 360; a += 45) {
            int px = (int) Math.round(6 * Math.cos(Math.toRadians(a)));
            int pz = (int) Math.round(6 * Math.sin(Math.toRadians(a)));
            b.pillar(px, 1, 7, pz, Material.OXIDIZED_COPPER);
            b.setBlock(px, 8, pz, Material.END_ROD);
            b.setBlock(px, 7, pz, Material.COPPER_BLOCK);  // capital
        }

        // ── Central waterfall feature ───────────────────────────────────
        b.pillar(0, 1, 6, 0, Material.COPPER_BLOCK);
        b.setBlock(0, 7, 0, Material.WATER);
        b.setBlock(0, 6, 0, Material.WATER);
        // Pool at base
        b.fillBox(-2, 0, -2, 2, 0, 2, Material.DEEPSLATE_BRICKS);
        b.fillBox(-1, 1, -1, 1, 1, 1, Material.WATER);
        b.setBlock(0, 1, 0, Material.SEA_LANTERN);

        // ── Floating amethyst chains ────────────────────────────────────
        for (int i = 0; i < 8; i++) {
            int a = i * 45;
            int cx = (int) Math.round(4 * Math.cos(Math.toRadians(a)));
            int cz = (int) Math.round(4 * Math.sin(Math.toRadians(a)));
            b.setBlock(cx, 7, cz, Material.IRON_CHAIN);
            b.setBlock(cx, 6, cz, Material.IRON_CHAIN);
            b.setBlock(cx, 5, cz, Material.IRON_CHAIN);
            b.setBlock(cx, 4, cz, Material.AMETHYST_CLUSTER);
        }

        // ── Note blocks + chime circle ──────────────────────────────────
        b.setBlock(-5, 1, -5, Material.NOTE_BLOCK);
        b.setBlock(5, 1, 5, Material.NOTE_BLOCK);
        b.setBlock(5, 1, -5, Material.NOTE_BLOCK);
        b.setBlock(-5, 1, 5, Material.NOTE_BLOCK);
        b.setBlock(0, 1, -5, Material.NOTE_BLOCK);
        b.setBlock(0, 1, 5, Material.NOTE_BLOCK);
        b.setBlock(-5, 1, 0, Material.NOTE_BLOCK);
        b.setBlock(5, 1, 0, Material.NOTE_BLOCK);

        // ── Amethyst decorations ────────────────────────────────────────
        b.setBlock(-3, 1, 0, Material.AMETHYST_CLUSTER);
        b.setBlock(3, 1, 0, Material.AMETHYST_CLUSTER);
        b.setBlock(0, 1, -3, Material.BUDDING_AMETHYST);
        b.setBlock(0, 1, 3, Material.BUDDING_AMETHYST);
        b.setBlock(-3, 1, -3, Material.SMALL_AMETHYST_BUD);
        b.setBlock(3, 1, 3, Material.SMALL_AMETHYST_BUD);

        // ── Candle circle ───────────────────────────────────────────────
        Material[] candles = {Material.CYAN_CANDLE, Material.LIGHT_BLUE_CANDLE,
                Material.PINK_CANDLE, Material.PURPLE_CANDLE, Material.MAGENTA_CANDLE};
        for (int i = 0; i < 12; i++) {
            int a = i * 30;
            int cx = (int) Math.round(3 * Math.cos(Math.toRadians(a)));
            int cz = (int) Math.round(3 * Math.sin(Math.toRadians(a)));
            b.setBlockIfAir(cx, 1, cz, candles[i % candles.length]);
        }

        // ── Flower pots around exterior perimeter ───────────────────────
        for (int a = 0; a < 360; a += 30) {
            int fx = (int) (11 * Math.cos(Math.toRadians(a)));
            int fz = (int) (11 * Math.sin(Math.toRadians(a)));
            b.setBlock(fx, 1, fz, Material.POTTED_ALLIUM);
        }

        // ── Bookshelf archive nook (NW) ─────────────────────────────────
        b.fillBox(-8, 1, -8, -5, 4, -5, Material.OAK_PLANKS);
        b.fillBox(-7, 1, -7, -5, 3, -5, Material.AIR);
        b.setBlock(-7, 1, -7, Material.BOOKSHELF);
        b.setBlock(-7, 2, -7, Material.BOOKSHELF);
        b.setBlock(-6, 1, -7, Material.LECTERN);
        b.setBlock(-7, 1, -6, Material.ENCHANTING_TABLE);
        b.setBlock(-6, 2, -6, Material.LANTERN);
        b.pyramidRoof(-8, -8, -5, -5, 4, Material.OAK_STAIRS);

        // ── Beehive garden (exterior, 6 hives with flowering azaleas) ───
        for (int[] hive : new int[][]{{10, 0}, {-10, 0}, {0, 10}, {0, -10}, {8, 7}, {-8, -7}}) {
            b.setBlock(hive[0], 1, hive[1], Material.BEEHIVE);
            b.setBlockIfAir(hive[0], 2, hive[1], Material.FLOWERING_AZALEA);
            b.setBlockIfAir(hive[0] + 1, 1, hive[1], Material.POPPY);
            b.setBlockIfAir(hive[0] - 1, 1, hive[1], Material.DANDELION);
        }

        // ── Allay display cages ─────────────────────────────────────────
        for (int[] cage : new int[][]{{-5, -5}, {5, 5}, {5, -5}, {-5, 5}}) {
            b.pillar(cage[0], 1, 2, cage[1], Material.IRON_BARS);
            b.setBlock(cage[0], 3, cage[1], Material.SEA_LANTERN);
            b.setBlock(cage[0], 4, cage[1], Material.IRON_BARS);
        }

        // ── Floral entrance path (south approach) ───────────────────────
        for (int z = -13; z <= -9; z++) {
            b.setBlock(0, 0, z, Material.PACKED_MUD);
            b.setBlockIfAir(-1, 1, z, Material.PINK_TULIP);
            b.setBlockIfAir(1, 1, z, Material.AZURE_BLUET);
        }
        // Entrance arch
        b.setBlock(0, 3, -9, Material.CHERRY_LEAVES);
        b.setBlock(-1, 2, -9, Material.CHERRY_LEAVES);
        b.setBlock(1, 2, -9, Material.CHERRY_LEAVES);

        // ── Loot ────────────────────────────────────────────────────────
        b.placeChest(0, 1, 0);       // center
        b.placeChest(-7, 1, -6);     // archive nook
        b.placeChest(5, 1, 5);       // display cage corner
        b.placeChest(-5, 1, -5);     // opposite cage
        b.placeChest(10, 1, 0);      // east beehive
        b.setBossSpawn(0, 1, -3);
    }
}

package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;

/**
 * AllaySanctuaryBuilder — glass-domed amethyst sanctuary with copper pillars, waterfall, archive nook, and beehives.
 *
 * <p>Extracted verbatim from the original {@code OverworldStructureBuilders.buildAllaySanctuary}
 * method to give the structure its own file per the one-class-per-structure
 * architecture.
 */
public final class AllaySanctuaryBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // Meadow base
        b.filledCircle(0, 0, 0, 12, Material.GRASS_BLOCK);
        b.scatter(-12, 0, -12, 12, 0, 12, Material.GRASS_BLOCK, Material.FLOWERING_AZALEA_LEAVES, 0.05);

        // Glass dome structure
        b.dome(0, 1, 0, 8, Material.LIGHT_BLUE_STAINED_GLASS);

        // Calcite floor with amethyst ring
        b.filledCircle(0, 0, 0, 8, Material.CALCITE);
        b.circle(0, 0, 0, 6, Material.AMETHYST_BLOCK);

        // Interior flowering garden
        b.setBlock(-2, 1, -2, Material.FLOWERING_AZALEA);
        b.setBlock(2, 1, 2, Material.FLOWERING_AZALEA);
        b.setBlock(-3, 1, 3, Material.AZALEA);
        b.setBlock(3, 1, -3, Material.AZALEA);

        // Copper pillars
        for (int[] p : new int[][]{{-5, 0}, {5, 0}, {0, -5}, {0, 5}}) {
            b.pillar(p[0], 1, 6, p[1], Material.OXIDIZED_COPPER);
            b.setBlock(p[0], 7, p[1], Material.END_ROD);
        }

        // Note blocks and amethyst
        b.setBlock(-4, 1, -4, Material.NOTE_BLOCK);
        b.setBlock(4, 1, 4, Material.NOTE_BLOCK);
        b.setBlock(4, 1, -4, Material.NOTE_BLOCK);
        b.setBlock(-4, 1, 4, Material.NOTE_BLOCK);

        // Central waterfall feature
        b.pillar(0, 1, 5, 0, Material.COPPER_BLOCK);
        b.setBlock(0, 6, 0, Material.WATER);

        // Amethyst decorations
        b.setBlock(-3, 1, 0, Material.AMETHYST_CLUSTER);
        b.setBlock(3, 1, 0, Material.AMETHYST_CLUSTER);
        b.setBlock(0, 1, -3, Material.BUDDING_AMETHYST);
        b.setBlock(0, 1, 3, Material.BUDDING_AMETHYST);

        // Candles
        b.setBlock(-2, 1, 0, Material.CYAN_CANDLE);
        b.setBlock(2, 1, 0, Material.CYAN_CANDLE);
        b.setBlock(0, 1, -2, Material.LIGHT_BLUE_CANDLE);
        b.setBlock(0, 1, 2, Material.LIGHT_BLUE_CANDLE);

        // Flower pots around exterior
        for (int a = 0; a < 360; a += 45) {
            int fx = (int) (10 * Math.cos(Math.toRadians(a)));
            int fz = (int) (10 * Math.sin(Math.toRadians(a)));
            b.setBlock(fx, 1, fz, Material.POTTED_ALLIUM);
        }

        // ─── Floating amethyst cluster arrangement suspended by chains ───
        for (int i = 0; i < 6; i++) {
            int a = i * 60;
            int cx = (int) Math.round(4 * Math.cos(Math.toRadians(a)));
            int cz = (int) Math.round(4 * Math.sin(Math.toRadians(a)));
            b.setBlock(cx, 5, cz, Material.IRON_CHAIN);
            b.setBlock(cx, 4, cz, Material.IRON_CHAIN);
            b.setBlock(cx, 3, cz, Material.AMETHYST_CLUSTER);
        }

        // ─── Bookshelves hidden under the dome (archive nook) ───
        b.fillBox(-7, 1, -7, -5, 3, -5, Material.OAK_PLANKS);
        b.fillBox(-6, 1, -6, -5, 2, -5, Material.AIR);
        b.setBlock(-6, 1, -6, Material.BOOKSHELF);
        b.setBlock(-6, 2, -6, Material.BOOKSHELF);
        b.setBlock(-5, 1, -6, Material.LECTERN);
        b.setBlock(-6, 1, -5, Material.ENCHANTING_TABLE);

        // ─── Beehives outside the sanctuary ───
        for (int[] hive : new int[][]{{9, 0}, {-9, 0}, {0, 9}, {0, -9}}) {
            b.setBlock(hive[0], 1, hive[1], Material.BEEHIVE);
            b.setBlockIfAir(hive[0], 2, hive[1], Material.FLOWERING_AZALEA);
        }

        // ─── Stained glass mosaic floor pattern ───
        b.setBlock(-1, 0, -1, Material.LIGHT_BLUE_STAINED_GLASS);
        b.setBlock(1, 0, -1, Material.PINK_STAINED_GLASS);
        b.setBlock(-1, 0, 1, Material.PINK_STAINED_GLASS);
        b.setBlock(1, 0, 1, Material.LIGHT_BLUE_STAINED_GLASS);
        b.setBlock(0, 0, 0, Material.WHITE_STAINED_GLASS);

        // ─── Allay display cages (iron bars with sea lanterns) ───
        for (int[] cage : new int[][]{{-5, -5}, {5, 5}, {5, -5}, {-5, 5}}) {
            b.setBlock(cage[0], 1, cage[1], Material.IRON_BARS);
            b.setBlock(cage[0], 2, cage[1], Material.IRON_BARS);
            b.setBlock(cage[0], 3, cage[1], Material.SEA_LANTERN);
        }

        b.placeChest(0, 1, 0);
        b.placeChest(-5, 1, -5);  // archive chest
        b.placeChest(5, 1, 5);
    }
}

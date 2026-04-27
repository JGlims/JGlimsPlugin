package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;

/**
 * FairyGladeBuilder — enchanted cherry-blossom clearing with scrying spring, flower arches,
 * amethyst formations, mushroom ring, unicorn stable, fairy ring, glowing moss paths,
 * hanging flower pots, fairy lanterns, wish well, bird perches, stone altar.
 */
public final class FairyGladeBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // ── Cherry-blossom clearing ─────────────────────────────────────
        b.filledCircle(0, 0, 0, 16, Material.GRASS_BLOCK);
        b.scatter(-16, 0, -16, 16, 0, 16, Material.GRASS_BLOCK, Material.MOSS_BLOCK, 0.25);
        b.scatter(-16, 0, -16, 16, 0, 16, Material.GRASS_BLOCK, Material.FLOWERING_AZALEA_LEAVES, 0.08);
        b.scatter(-16, 0, -16, 16, 0, 16, Material.GRASS_BLOCK, Material.MYCELIUM, 0.04);

        // ── Cherry-blossom trees (6 around the glade, different sizes) ──
        int[][] treePos = {{-10, -10}, {10, -10}, {-10, 10}, {10, 10}, {-14, 0}, {14, 0}};
        for (int i = 0; i < treePos.length; i++) {
            int[] tp = treePos[i];
            int height = 7 + i % 3;
            b.pillar(tp[0], 1, height, tp[1], Material.CHERRY_LOG);
            int crownBase = height - 2;
            for (int y = crownBase; y <= crownBase + 4; y++) {
                int r = y < crownBase + 2 ? 4 : (y < crownBase + 4 ? 3 : 1);
                b.filledCircle(tp[0], y, tp[1], r, Material.CHERRY_LEAVES);
            }
            // Pink flower petals on ground under tree
            b.scatter(tp[0] - 3, 1, tp[1] - 3, tp[0] + 3, 1, tp[1] + 3, Material.AIR, Material.PINK_PETALS, 0.4);
            // Hanging lantern from tree
            b.setBlock(tp[0], height - 1, tp[1], Material.IRON_CHAIN);
            b.setBlock(tp[0], height - 2, tp[1], Material.SOUL_LANTERN);
        }

        // ── Enchanted scrying spring (center) ───────────────────────────
        b.filledCircle(0, -1, 0, 3, Material.COBBLESTONE);
        b.filledCircle(0, 0, 0, 2, Material.WATER);
        b.setBlock(0, 0, 0, Material.SOUL_LANTERN);  // underwater glow
        b.circle(0, 0, 0, 3, Material.MOSSY_STONE_BRICKS);
        // Rim decorations (candles + flowers)
        for (int a = 0; a < 360; a += 45) {
            int cx = (int) Math.round(3 * Math.cos(Math.toRadians(a)));
            int cz = (int) Math.round(3 * Math.sin(Math.toRadians(a)));
            b.setBlock(cx, 1, cz, a % 90 == 0 ? Material.PINK_CANDLE : Material.FLOWERING_AZALEA);
        }
        // Wish coins at bottom
        b.setBlock(0, -1, 0, Material.GOLD_BLOCK);

        // ── Flower arches (2 crossing paths, cherry-leaf canopies) ──────
        for (int x = -8; x <= 8; x++) {
            b.setBlock(x, 3, -1, Material.CHERRY_LEAVES);
            b.setBlock(x, 3, 1, Material.CHERRY_LEAVES);
            b.setBlock(x, 4, 0, Material.CHERRY_LEAVES);
            if (Math.abs(x) % 2 == 0) b.setBlock(x, 5, 0, Material.PINK_PETALS);
        }
        for (int z = -8; z <= 8; z++) {
            b.setBlockIfAir(-1, 3, z, Material.CHERRY_LEAVES);
            b.setBlockIfAir(1, 3, z, Material.CHERRY_LEAVES);
            b.setBlockIfAir(0, 4, z, Material.CHERRY_LEAVES);
        }
        // Path under arch
        for (int x = -8; x <= 8; x++) b.setBlock(x, 0, 0, Material.PACKED_MUD);
        for (int z = -8; z <= 8; z++) b.setBlockIfAir(0, 0, z, Material.PACKED_MUD);

        // ── Amethyst crystal formations ─────────────────────────────────
        b.setBlock(-6, 1, 0, Material.AMETHYST_BLOCK);
        b.setBlock(-6, 2, 0, Material.AMETHYST_CLUSTER);
        b.setBlock(-6, 1, 1, Material.SMALL_AMETHYST_BUD);
        b.setBlock(6, 1, 0, Material.AMETHYST_BLOCK);
        b.setBlock(6, 2, 0, Material.AMETHYST_CLUSTER);
        b.setBlock(6, 1, -1, Material.SMALL_AMETHYST_BUD);
        b.setBlock(0, 1, -6, Material.BUDDING_AMETHYST);
        b.setBlock(0, 2, -6, Material.AMETHYST_CLUSTER);
        b.setBlock(0, 1, 6, Material.BUDDING_AMETHYST);
        b.setBlock(0, 2, 6, Material.AMETHYST_CLUSTER);

        // ── Mushroom fairy ring (NW corner) ─────────────────────────────
        int[][] ring = {{-9, -6}, {-8, -7}, {-7, -8}, {-6, -7}, {-7, -6}, {-8, -5}};
        for (int[] r : ring) {
            b.setBlock(r[0], 1, r[1], Material.RED_MUSHROOM);
        }
        b.setBlock(-8, 1, -7, Material.RED_MUSHROOM_BLOCK);
        b.setBlock(-7, 1, -7, Material.MUSHROOM_STEM);

        // ── Unicorn stable (SE) ─────────────────────────────────────────
        b.fillBox(6, 0, 6, 12, 0, 12, Material.GRASS_BLOCK);
        b.fillBox(5, 1, 5, 13, 3, 5, Material.AIR);
        // Fenced perimeter
        for (int x = 5; x <= 13; x++) {
            b.setBlock(x, 1, 5, Material.CHERRY_FENCE);
            b.setBlock(x, 2, 5, Material.CHERRY_FENCE);
            b.setBlock(x, 1, 13, Material.CHERRY_FENCE);
        }
        for (int z = 5; z <= 13; z++) {
            b.setBlock(5, 1, z, Material.CHERRY_FENCE);
            b.setBlock(13, 1, z, Material.CHERRY_FENCE);
        }
        // Gate
        b.setBlock(9, 1, 5, Material.CHERRY_FENCE_GATE);
        b.setBlock(9, 2, 5, Material.AIR);
        // Feed + water
        b.setBlock(7, 1, 7, Material.HAY_BLOCK);
        b.setBlock(8, 1, 7, Material.HAY_BLOCK);
        b.setBlock(11, 1, 11, Material.WATER_CAULDRON);
        // Stable roof pavilion
        b.fillBox(6, 5, 6, 12, 5, 12, Material.CHERRY_PLANKS);
        b.pyramidRoof(6, 6, 12, 12, 5, Material.CHERRY_STAIRS);
        for (int[] p : new int[][]{{6, 6}, {12, 6}, {6, 12}, {12, 12}}) {
            b.pillar(p[0], 1, 4, p[1], Material.CHERRY_LOG);
        }
        b.setBlock(9, 4, 9, Material.LANTERN);

        // ── Wish well (SW corner) ───────────────────────────────────────
        b.fillBox(-12, 0, 6, -10, 0, 8, Material.MOSSY_COBBLESTONE);
        b.fillBox(-12, 1, 6, -10, 3, 8, Material.MOSSY_COBBLESTONE);
        b.fillBox(-11, 1, 7, -11, 2, 7, Material.WATER);
        // Well roof
        b.pillar(-12, 4, 4, 6, Material.OAK_LOG);
        b.pillar(-10, 4, 4, 6, Material.OAK_LOG);
        b.pillar(-12, 4, 4, 8, Material.OAK_LOG);
        b.pillar(-10, 4, 4, 8, Material.OAK_LOG);
        b.fillBox(-12, 5, 6, -10, 5, 8, Material.OAK_PLANKS);
        b.pyramidRoof(-12, 6, -10, 8, 5, Material.OAK_STAIRS);
        b.setBlock(-11, 5, 7, Material.LANTERN);
        // Well bucket
        b.setBlock(-11, 3, 7, Material.IRON_CHAIN);

        // ── Stone altar (northeast grove, offering platform) ────────────
        b.fillBox(-5, 1, -11, -3, 1, -9, Material.POLISHED_DIORITE);
        b.setBlock(-4, 2, -10, Material.CHISELED_QUARTZ_BLOCK);
        b.setBlock(-4, 3, -10, Material.DECORATED_POT);
        b.setBlock(-5, 2, -10, Material.CANDLE);
        b.setBlock(-3, 2, -10, Material.CANDLE);
        b.setBlock(-4, 2, -11, Material.FLOWERING_AZALEA);
        b.setBlock(-4, 2, -9, Material.FLOWERING_AZALEA);

        // ── Bird perches on the east side ───────────────────────────────
        for (int[] perch : new int[][]{{12, -5}, {13, -7}, {11, -8}}) {
            b.pillar(perch[0], 1, 3, perch[1], Material.BIRCH_LOG);
            b.setBlock(perch[0], 4, perch[1], Material.BIRCH_FENCE);
        }

        // ── Butterfly motes (end rods as particle emitters) ─────────────
        for (int[] er : new int[][]{{-3, -3}, {3, 3}, {-4, 4}, {4, -4}, {0, 7}, {7, 0}, {-7, 0}, {0, -7}}) {
            b.setBlock(er[0], 3, er[1], Material.END_ROD);
        }

        // ── Flowers scattered around perimeter ──────────────────────────
        Material[] flowers = {Material.PINK_TULIP, Material.ALLIUM, Material.LILY_OF_THE_VALLEY,
                Material.CORNFLOWER, Material.AZURE_BLUET, Material.OXEYE_DAISY,
                Material.BLUE_ORCHID, Material.RED_TULIP, Material.WHITE_TULIP};
        for (int a = 0; a < 360; a += 12) {
            int fx = (int) (13 * Math.cos(Math.toRadians(a)));
            int fz = (int) (13 * Math.sin(Math.toRadians(a)));
            b.setBlockIfAir(fx, 1, fz, flowers[a / 12 % flowers.length]);
        }
        // Inner flower ring
        for (int a = 0; a < 360; a += 20) {
            int fx = (int) (5 * Math.cos(Math.toRadians(a)));
            int fz = (int) (5 * Math.sin(Math.toRadians(a)));
            b.setBlockIfAir(fx, 1, fz, flowers[a / 20 % flowers.length]);
        }

        // ── Fairy lanterns along paths ──────────────────────────────────
        for (int i = -5; i <= 5; i += 5) {
            if (i == 0) continue;
            b.setBlock(i, 1, 0, Material.CHERRY_FENCE);
            b.setBlock(i, 2, 0, Material.SOUL_LANTERN);
            b.setBlock(0, 1, i, Material.CHERRY_FENCE);
            b.setBlock(0, 2, i, Material.SOUL_LANTERN);
        }

        // ── Glowing moss paths to spring ────────────────────────────────
        for (int z = -14; z <= -4; z++) {
            b.setBlock(0, 0, z, Material.MOSS_BLOCK);
            b.scatter(0, 1, z, 0, 1, z, Material.AIR, Material.GLOW_LICHEN, 0.3);
        }

        // ── Loot ────────────────────────────────────────────────────────
        b.placeChest(0, 1, 3);      // spring-side trove
        b.placeChest(-4, 2, -10);   // altar offering
        b.placeChest(9, 4, 9);      // unicorn stable
        b.placeChest(-11, 1, 7);    // wish well
        b.setBossSpawn(0, 1, -8);
    }
}

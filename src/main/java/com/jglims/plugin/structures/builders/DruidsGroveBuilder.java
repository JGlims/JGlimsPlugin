package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;

/**
 * DruidsGroveBuilder — sacred mossy clearing with stone circle, central altar, massive sacred
 * oak, satellite oak ring, rune-path arcs, forest-spirit statue, meditation cushions,
 * hanging lantern canopy, hidden brewing alcove, mushroom garden, animal shrines.
 */
public final class DruidsGroveBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // ── Mossy clearing ──────────────────────────────────────────────
        b.filledCircle(0, 0, 0, 16, Material.MOSS_BLOCK);
        b.scatter(-16, 0, -16, 16, 0, 16, Material.MOSS_BLOCK, Material.GRASS_BLOCK, 0.25);
        b.scatter(-16, 0, -16, 16, 0, 16, Material.MOSS_BLOCK, Material.PODZOL, 0.08);
        b.scatter(-16, 0, -16, 16, 0, 16, Material.MOSS_BLOCK, Material.ROOTED_DIRT, 0.04);

        // ── Stone circle (8 standing stones, 5 tall) ────────────────────
        int[][] stones = {{11, 0}, {-11, 0}, {0, 11}, {0, -11}, {8, 8}, {-8, 8}, {8, -8}, {-8, -8}};
        for (int[] s : stones) {
            b.pillar(s[0], 1, 5, s[1], Material.MOSSY_STONE_BRICKS);
            b.setBlock(s[0], 6, s[1], Material.STONE_BRICK_SLAB);
            // Moss base
            for (int[] off : new int[][]{{0, 1}, {0, -1}, {1, 0}, {-1, 0}}) {
                b.setBlockIfAir(s[0] + off[0], 1, s[1] + off[1], Material.MOSS_CARPET);
            }
            // Glow lichen
            b.setBlock(s[0], 3, s[1], Material.GLOW_LICHEN);
        }

        // ── Central altar ───────────────────────────────────────────────
        b.fillBox(-2, 1, -2, 2, 1, 2, Material.MOSSY_STONE_BRICKS);
        b.fillBox(-1, 2, -1, 1, 2, 1, Material.SMOOTH_STONE);
        b.setBlock(0, 3, 0, Material.GLOWSTONE);
        b.setBlock(-1, 3, -1, Material.CANDLE);
        b.setBlock(1, 3, -1, Material.CANDLE);
        b.setBlock(-1, 3, 1, Material.CANDLE);
        b.setBlock(1, 3, 1, Material.CANDLE);
        // Ring of flowering azaleas around altar
        for (int[] rz : new int[][]{{-3, 0}, {3, 0}, {0, -3}, {0, 3}}) {
            b.setBlock(rz[0], 1, rz[1], Material.FLOWERING_AZALEA);
        }

        // ── Sacred oak behind altar (massive, 20 blocks tall) ───────────
        b.fillBox(-1, 0, 5, 1, 18, 7, Material.OAK_LOG);
        // Larger base
        b.setBlock(-2, 0, 6, Material.OAK_LOG);
        b.setBlock(2, 0, 6, Material.OAK_LOG);
        b.setBlock(0, 0, 4, Material.OAK_LOG);
        b.setBlock(0, 0, 8, Material.OAK_LOG);
        // Canopy layers
        for (int y = 12; y <= 22; y++) {
            int r = y < 16 ? 7 : (y < 19 ? 5 : (y < 21 ? 3 : 1));
            b.filledCircle(0, y, 6, r, Material.OAK_LEAVES);
        }
        // Flowering accent (azalea)
        b.filledCircle(3, 14, 6, 1, Material.FLOWERING_AZALEA_LEAVES);
        b.filledCircle(-3, 16, 6, 1, Material.FLOWERING_AZALEA_LEAVES);
        // Glowstone hidden in leaves
        b.setBlock(2, 14, 6, Material.GLOWSTONE);
        b.setBlock(-2, 15, 8, Material.GLOWSTONE);
        b.setBlock(0, 17, 6, Material.GLOWSTONE);
        b.setBlock(3, 18, 5, Material.GLOWSTONE);

        // ── Surrounding satellite oaks (8 smaller) ──────────────────────
        int[][] treePos = {{-12, -9}, {12, -7}, {-10, 12}, {13, 9}, {-14, 3}, {14, -3}, {0, -14}, {4, 14}};
        for (int i = 0; i < treePos.length; i++) {
            int[] tp = treePos[i];
            int h = 7 + i % 3;
            b.pillar(tp[0], 1, h, tp[1], Material.OAK_LOG);
            int crownBase = h - 2;
            for (int y = crownBase; y <= crownBase + 3; y++) {
                int r = y < crownBase + 2 ? 3 : 2;
                b.filledCircle(tp[0], y, tp[1], r, Material.OAK_LEAVES);
            }
            // Hanging lantern
            b.setBlock(tp[0], h - 1, tp[1] + 1, Material.IRON_CHAIN);
            b.setBlock(tp[0], h - 2, tp[1] + 1, Material.LANTERN);
        }

        // ── Flower patches ──────────────────────────────────────────────
        Material[] flowers = {Material.POPPY, Material.DANDELION, Material.CORNFLOWER,
                Material.OXEYE_DAISY, Material.AZURE_BLUET, Material.LILY_OF_THE_VALLEY};
        for (int a = 0; a < 360; a += 15) {
            int fx = (int) (13 * Math.cos(Math.toRadians(a)));
            int fz = (int) (13 * Math.sin(Math.toRadians(a)));
            b.setBlockIfAir(fx, 1, fz, flowers[a / 15 % flowers.length]);
        }

        // ── Druid rune arcs between stones ──────────────────────────────
        for (int i = 0; i < 8; i++) {
            int a1 = i * 45;
            int a2 = ((i + 1) % 8) * 45;
            int steps = 6;
            for (int t = 1; t < steps; t++) {
                double f = (double) t / steps;
                double angle = Math.toRadians(a1 + (a2 - a1) * f);
                int rx = (int) Math.round(10 * Math.cos(angle));
                int rz = (int) Math.round(10 * Math.sin(angle));
                b.setBlock(rx, 0, rz, Material.MOSS_CARPET);
                b.setBlock(rx, 1, rz, Material.PINK_PETALS);
            }
        }

        // ── Forest spirit statue behind altar ───────────────────────────
        b.pillar(0, 1, 5, 3, Material.STRIPPED_OAK_LOG);
        b.setBlock(-1, 3, 3, Material.OAK_LEAVES);
        b.setBlock(1, 3, 3, Material.OAK_LEAVES);
        b.setBlock(0, 5, 3, Material.CARVED_PUMPKIN);
        b.setBlock(-1, 2, 3, Material.STRIPPED_OAK_LOG);
        b.setBlock(1, 2, 3, Material.STRIPPED_OAK_LOG);
        b.setBlock(0, 4, 3, Material.FLOWERING_AZALEA);

        // ── Meditation mats around altar ────────────────────────────────
        for (int[] mat : new int[][]{{3, 0}, {-3, 0}, {0, 3}, {0, -3}, {2, 2}, {-2, -2}, {2, -2}, {-2, 2}}) {
            b.setBlockIfAir(mat[0], 1, mat[1], Material.MOSS_CARPET);
        }

        // ── Hanging lanterns in sacred oak ──────────────────────────────
        for (int[] hl : new int[][]{{0, 6}, {3, 6}, {-3, 6}, {0, 8}, {2, 10}, {-2, 10}}) {
            b.setBlock(hl[0], 11, hl[1], Material.IRON_CHAIN);
            b.setBlock(hl[0], 10, hl[1], Material.LANTERN);
        }

        // ── Sweet berry bushes at clearing edge ─────────────────────────
        for (int[] berry : new int[][]{{-15, 0}, {15, 0}, {0, -15}, {11, 11}, {-11, 11}, {11, -11}, {-11, -11}}) {
            b.setBlockIfAir(berry[0], 1, berry[1], Material.SWEET_BERRY_BUSH);
        }

        // ── Mushroom garden (NE corner) ─────────────────────────────────
        for (int[] ms : new int[][]{{10, -13}, {11, -12}, {12, -14}, {10, -14}, {13, -12}}) {
            b.setBlock(ms[0], 1, ms[1], ms[0] % 2 == 0 ? Material.RED_MUSHROOM : Material.BROWN_MUSHROOM);
        }
        b.setBlock(11, 1, -13, Material.RED_MUSHROOM_BLOCK);

        // ── Animal shrines (stag skull + wolf statue) ───────────────────
        // Stag shrine (NW)
        b.pillar(-13, 1, 2, -5, Material.STRIPPED_OAK_LOG);
        b.setBlock(-13, 3, -5, Material.SKELETON_SKULL);
        b.setBlock(-12, 1, -5, Material.CANDLE);
        b.setBlock(-14, 1, -5, Material.CANDLE);
        // Wolf shrine (SE)
        b.pillar(13, 1, 2, 5, Material.STRIPPED_DARK_OAK_LOG);
        b.setBlock(13, 3, 5, Material.SKELETON_SKULL);
        b.setBlock(12, 1, 5, Material.CANDLE);
        b.setBlock(14, 1, 5, Material.CANDLE);

        // ── Glow-lichen trail to sacred oak ─────────────────────────────
        for (int z = 1; z <= 4; z++) {
            b.setBlock(0, 0, z, Material.GRAVEL);
            b.setBlock(-1, 0, z, Material.GRAVEL);
            b.setBlock(1, 0, z, Material.GRAVEL);
        }

        // ── Hidden brewing alcove under the altar ───────────────────────
        b.fillBox(-2, -4, -2, 2, -1, 2, Material.MOSSY_STONE_BRICKS);
        b.fillBox(-1, -3, -1, 1, -2, 1, Material.AIR);
        b.setBlock(0, -3, 0, Material.BREWING_STAND);
        b.setBlock(-1, -3, 0, Material.CAULDRON);
        b.setBlock(1, -3, 0, Material.LANTERN);
        b.setBlock(0, -3, 1, Material.ENCHANTING_TABLE);
        b.setBlock(0, -3, -1, Material.BOOKSHELF);
        // Ladder down from altar
        for (int y = -2; y <= 0; y++) b.setBlock(0, y, 2, Material.LADDER);

        // ── Loot ────────────────────────────────────────────────────────
        b.placeChest(0, 3, -2);    // altar-side
        b.placeChest(0, -3, -1);   // hidden alcove
        b.placeChest(-13, 1, -5);  // stag shrine
        b.placeChest(13, 1, 5);    // wolf shrine
        b.placeChest(0, 1, 6);     // at base of sacred oak
        b.setBossSpawn(0, 1, 5);
    }
}

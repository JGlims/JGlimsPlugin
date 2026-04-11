package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;

/**
 * DruidsGroveBuilder — sacred mossy clearing with stone circle, central altar, sacred oak, and hidden brewing alcove.
 *
 * <p>Extracted verbatim from the original {@code OverworldStructureBuilders.buildDruidsGrove}
 * method to give the structure its own file per the one-class-per-structure
 * architecture.
 */
public final class DruidsGroveBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // Mossy clearing
        b.filledCircle(0, 0, 0, 14, Material.MOSS_BLOCK);
        b.scatter(-14, 0, -14, 14, 0, 14, Material.MOSS_BLOCK, Material.GRASS_BLOCK, 0.3);

        // Stone circle (8 standing stones, 4 blocks tall)
        int[][] stones = {{10, 0}, {-10, 0}, {0, 10}, {0, -10}, {7, 7}, {-7, 7}, {7, -7}, {-7, -7}};
        for (int[] s : stones) {
            b.pillar(s[0], 1, 4, s[1], Material.MOSSY_STONE_BRICKS);
            b.setBlock(s[0], 5, s[1], Material.STONE_BRICK_SLAB);
        }

        // Central altar
        b.fillBox(-2, 1, -2, 2, 1, 2, Material.MOSSY_STONE_BRICKS);
        b.fillBox(-1, 2, -1, 1, 2, 1, Material.SMOOTH_STONE);
        b.setBlock(0, 3, 0, Material.GLOWSTONE);
        b.setBlock(-1, 3, -1, Material.CANDLE);
        b.setBlock(1, 3, -1, Material.CANDLE);
        b.setBlock(-1, 3, 1, Material.CANDLE);
        b.setBlock(1, 3, 1, Material.CANDLE);

        // Sacred oak tree behind altar
        b.fillBox(-1, 0, 5, 1, 18, 7, Material.OAK_LOG);
        for (int y = 12; y <= 20; y++) {
            int r = y < 16 ? 6 : (y < 19 ? 4 : 2);
            b.filledCircle(0, y, 6, r, Material.OAK_LEAVES);
        }
        // Glowstone hidden in leaves
        b.setBlock(2, 14, 6, Material.GLOWSTONE);
        b.setBlock(-2, 15, 8, Material.GLOWSTONE);
        b.setBlock(0, 17, 6, Material.GLOWSTONE);

        // Surrounding oaks (smaller)
        int[][] treePos = {{-10, -8}, {10, -6}, {-8, 10}, {12, 8}};
        for (int[] tp : treePos) {
            b.pillar(tp[0], 1, 8, tp[1], Material.OAK_LOG);
            for (int y = 6; y <= 10; y++) b.filledCircle(tp[0], y, tp[1], 3, Material.OAK_LEAVES);
        }

        // Flower patches
        for (int a = 0; a < 360; a += 25) {
            int fx = (int) (12 * Math.cos(Math.toRadians(a)));
            int fz = (int) (12 * Math.sin(Math.toRadians(a)));
            Material flower = switch (a % 100) {
                case 0 -> Material.POPPY;
                case 25 -> Material.DANDELION;
                case 50 -> Material.CORNFLOWER;
                default -> Material.OXEYE_DAISY;
            };
            b.setBlockIfAir(fx, 1, fz, flower);
        }

        // Glow lichen on standing stones
        b.setBlock(-10, 3, 0, Material.GLOW_LICHEN);
        b.setBlock(10, 3, 0, Material.GLOW_LICHEN);
        b.setBlock(0, 3, 10, Material.GLOW_LICHEN);

        // ─── Druid circle runes (moss-inlaid arcs between stones) ───
        for (int i = 0; i < 8; i++) {
            int a1 = i * 45;
            int a2 = ((i + 1) % 8) * 45;
            int steps = 6;
            for (int t = 1; t < steps; t++) {
                double f = (double) t / steps;
                double angle = Math.toRadians(a1 + (a2 - a1) * f);
                int rx = (int) Math.round(9 * Math.cos(angle));
                int rz = (int) Math.round(9 * Math.sin(angle));
                b.setBlock(rx, 0, rz, Material.MOSS_CARPET);
            }
        }

        // ─── Forest spirit statue behind the altar (oak log + leaves) ───
        b.pillar(0, 1, 4, 3, Material.STRIPPED_OAK_LOG);
        b.setBlock(-1, 3, 3, Material.OAK_LEAVES);
        b.setBlock(1, 3, 3, Material.OAK_LEAVES);
        b.setBlock(0, 4, 3, Material.CARVED_PUMPKIN);  // spirit head
        b.setBlock(-1, 2, 3, Material.STRIPPED_OAK_LOG);
        b.setBlock(1, 2, 3, Material.STRIPPED_OAK_LOG);

        // ─── Meditation mats (moss carpet cushions around altar) ───
        for (int[] mat : new int[][]{{3, 0}, {-3, 0}, {0, 3}, {0, -3}}) {
            b.setBlock(mat[0], 1, mat[1], Material.MOSS_CARPET);
        }

        // ─── Hanging lanterns in the sacred oak ───
        b.setBlock(0, 11, 6, Material.LANTERN);
        b.setBlock(3, 14, 6, Material.LANTERN);
        b.setBlock(-3, 14, 6, Material.LANTERN);
        b.setBlock(0, 16, 6, Material.LANTERN);

        // ─── Sweet berry bushes at the clearing edge ───
        for (int[] berry : new int[][]{{-13, 0}, {13, 0}, {0, -13}, {9, 9}}) {
            b.setBlockIfAir(berry[0], 1, berry[1], Material.SWEET_BERRY_BUSH);
        }

        // ─── Brewing alcove (hidden under the altar) ───
        b.fillBox(-1, -3, -1, 1, -1, 1, Material.MOSSY_STONE_BRICKS);
        b.fillBox(-1, -2, -1, 1, -2, 1, Material.AIR);
        b.setBlock(0, -2, 0, Material.BREWING_STAND);
        b.setBlock(-1, -2, 0, Material.CAULDRON);
        b.setBlock(1, -2, 0, Material.LANTERN);

        b.placeChest(0, 3, -2);
        b.placeChest(0, -2, -1);  // hidden alcove chest
        b.setBossSpawn(0, 1, 5);
    }
}

package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;

/**
 * CursedGraveyardBuilder — iron-fenced cemetery with central mausoleum, 25+ tombstones,
 * open graves, crypt basement, dead-tree grove, soul-fire ambient, wrought-iron gate,
 * gargoyle statues, candle vigils, ossuary wall, coffin lines.
 */
public final class CursedGraveyardBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // ── Ground (podzol + soul soil) ─────────────────────────────────
        b.filledCircle(0, 0, 0, 14, Material.PODZOL);
        b.scatter(-14, 0, -14, 14, 0, 14, Material.PODZOL, Material.SOUL_SOIL, 0.30);
        b.scatter(-14, 0, -14, 14, 0, 14, Material.PODZOL, Material.COARSE_DIRT, 0.15);
        b.scatter(-14, 0, -14, 14, 0, 14, Material.PODZOL, Material.ROOTED_DIRT, 0.06);

        // ── Iron fence perimeter ────────────────────────────────────────
        b.circle(0, 1, 0, 13, Material.IRON_BARS);
        b.circle(0, 2, 0, 13, Material.IRON_BARS);

        // ── Wrought-iron gate (south) with stone pillars ────────────────
        b.setBlock(0, 1, -13, Material.AIR);
        b.setBlock(1, 1, -13, Material.AIR);
        b.setBlock(0, 2, -13, Material.AIR);
        b.setBlock(1, 2, -13, Material.AIR);
        b.pillar(-1, 1, 4, -13, Material.DEEPSLATE_BRICKS);
        b.pillar(2, 1, 4, -13, Material.DEEPSLATE_BRICKS);
        b.setBlock(-1, 5, -13, Material.SKELETON_SKULL);
        b.setBlock(2, 5, -13, Material.SKELETON_SKULL);
        // Gate lintel
        b.setBlock(0, 5, -13, Material.DEEPSLATE_BRICK_STAIRS);
        b.setBlock(1, 5, -13, Material.DEEPSLATE_BRICK_STAIRS);
        // Gate approach path
        for (int z = -12; z <= -4; z++) {
            b.setBlock(0, 0, z, Material.GRAVEL);
            b.setBlock(1, 0, z, Material.GRAVEL);
        }

        // ── 25 tombstones ───────────────────────────────────────────────
        int[][] gravePositions = {
                {-8, 3}, {-5, 5}, {-2, 7}, {1, 5}, {4, 3}, {7, 1},
                {-7, -1}, {-4, -3}, {-1, -6}, {2, -4}, {5, -2}, {8, 0},
                {-6, 8}, {-3, 9}, {0, 10}, {3, 8}, {6, 6},
                {-9, -4}, {-6, -7}, {3, -8}, {6, -5},
                {-11, 2}, {11, -2}, {-11, -6}, {11, 5}
        };
        for (int[] g : gravePositions) {
            b.setBlock(g[0], 1, g[1], Material.STONE_BRICK_WALL);
            b.setBlock(g[0], 2, g[1], Material.STONE_BRICK_WALL);
            b.setBlock(g[0], 3, g[1], Material.STONE_BUTTON);
            // Grave mound
            b.setBlockIfSolid(g[0] - 1, 0, g[1], Material.COARSE_DIRT);
            b.setBlockIfSolid(g[0] + 1, 0, g[1], Material.COARSE_DIRT);
            // Candle vigil on some
            if ((g[0] + g[1]) % 3 == 0) {
                b.setBlockIfAir(g[0] - 1, 1, g[1], Material.CANDLE);
            }
        }

        // ── Central mausoleum ───────────────────────────────────────────
        b.fillBox(-4, 0, -4, 4, 0, 4, Material.DEEPSLATE_BRICKS);
        b.fillWalls(-4, 1, -4, 4, 6, 4, Material.DEEPSLATE_BRICKS);
        b.hollowBox(-4, 1, -4, 4, 6, 4);
        b.fillBox(-4, 7, -4, 4, 7, 4, Material.DEEPSLATE_BRICK_SLAB);
        b.pyramidRoof(-5, -5, 5, 5, 7, Material.DEEPSLATE_TILES);
        // Mausoleum weathering
        b.scatter(-4, 1, -4, 4, 6, 4, Material.DEEPSLATE_BRICKS, Material.CRACKED_DEEPSLATE_BRICKS, 0.15);
        // Door
        b.setBlock(0, 1, -4, Material.AIR);
        b.setBlock(0, 2, -4, Material.AIR);
        // Corner columns
        for (int[] c : new int[][]{{-4, -4}, {4, -4}, {-4, 4}, {4, 4}}) {
            b.pillar(c[0], 1, 7, c[1], Material.POLISHED_DEEPSLATE);
        }
        // Coffins inside
        for (int[] cof : new int[][]{{-2, 2}, {-2, 0}, {-2, -2}, {2, 2}, {2, 0}, {2, -2}}) {
            b.setBlock(cof[0], 1, cof[1], Material.DARK_OAK_PLANKS);
            b.setBlock(cof[0], 1, cof[1] + (cof[1] < 0 ? 1 : -1), Material.DARK_OAK_PLANKS);
            // Lid
            b.setBlock(cof[0], 2, cof[1], Material.DARK_OAK_TRAPDOOR);
        }
        // Altar inside mausoleum
        b.setBlock(0, 1, 3, Material.CHISELED_POLISHED_BLACKSTONE);
        b.setBlock(0, 2, 3, Material.SKELETON_SKULL);
        b.setBlock(-1, 2, 3, Material.CANDLE);
        b.setBlock(1, 2, 3, Material.CANDLE);
        b.setBlock(0, 5, 0, Material.SOUL_LANTERN);
        b.setBlock(0, 4, -3, Material.SOUL_LANTERN);

        // ── Crypt basement (under mausoleum) ────────────────────────────
        b.fillBox(-3, -5, -3, 3, -1, 3, Material.DEEPSLATE_BRICKS);
        b.fillBox(-2, -4, -2, 2, -2, 2, Material.AIR);
        b.fillFloor(-2, -2, 2, 2, -5, Material.POLISHED_DEEPSLATE);
        // Ossuary wall (bones embedded)
        for (int x = -2; x <= 2; x++) {
            b.setBlock(x, -4, 2, Material.BONE_BLOCK);
            b.setBlock(x, -3, 2, Material.BONE_BLOCK);
        }
        b.setBlock(0, -4, 2, Material.WITHER_SKELETON_SKULL);
        b.setBlock(-2, -4, 2, Material.SKELETON_SKULL);
        b.setBlock(2, -4, 2, Material.SKELETON_SKULL);
        // Coffin row on other wall
        for (int x = -2; x <= 2; x++) {
            b.setBlock(x, -4, -2, Material.DARK_OAK_PLANKS);
        }
        // Ladder down
        for (int y = -4; y <= 0; y++) b.setBlock(0, y, 0, Material.LADDER);
        b.setBlock(0, 0, 0, Material.OAK_TRAPDOOR);  // hatch
        b.setBlock(-2, -4, 0, Material.SOUL_LANTERN);
        b.setBlock(2, -4, 0, Material.SOUL_LANTERN);

        // ── Dead trees scattered ────────────────────────────────────────
        for (int[] tp : new int[][]{{-9, 6}, {9, 5}, {-10, -5}, {9, -7}, {-3, 11}, {3, -11}}) {
            b.pillar(tp[0], 1, 5 + (tp[0] % 2 == 0 ? 0 : 2), tp[1], Material.DARK_OAK_LOG);
            // Broken branches
            b.setBlock(tp[0] + 1, 5, tp[1], Material.DARK_OAK_LOG);
            b.setBlock(tp[0], 6, tp[1] + 1, Material.DARK_OAK_LOG);
        }

        // ── Soul lanterns and candles ───────────────────────────────────
        for (int[] sl : new int[][]{{-6, 0}, {6, 0}, {0, 7}, {0, -7}, {-10, 7}, {10, -7}}) {
            b.setBlock(sl[0], 1, sl[1], Material.SOUL_LANTERN);
        }

        // ── Fog (soul fire scattered) ───────────────────────────────────
        for (int[] sf : new int[][]{{-3, -8}, {4, 6}, {-7, 5}, {8, -3}, {-8, 10}, {10, 9}}) {
            b.setBlock(sf[0], 0, sf[1], Material.SOUL_CAMPFIRE);
        }

        // ── Open graves (empty holes with broken headstones) ────────────
        b.fillBox(6, -2, -8, 8, 0, -6, Material.AIR);
        b.fillBox(5, -2, -9, 5, 0, -5, Material.COARSE_DIRT);
        b.fillBox(9, -2, -9, 9, 0, -5, Material.COARSE_DIRT);
        b.fillFloor(6, -8, 8, -6, -2, Material.COARSE_DIRT);
        b.setBlock(6, 0, -9, Material.STONE_BRICK_WALL);
        b.setBlock(6, 1, -9, Material.STONE_BUTTON);
        // Claw marks in the dirt
        b.setBlock(7, -1, -7, Material.STRIPPED_OAK_LOG);

        // Second open grave (NW)
        b.fillBox(-8, -2, 5, -6, 0, 7, Material.AIR);
        b.fillBox(-9, -2, 5, -9, 0, 7, Material.COARSE_DIRT);
        b.fillBox(-5, -2, 5, -5, 0, 7, Material.COARSE_DIRT);
        b.setBlock(-8, 0, 8, Material.STONE_BRICK_WALL);
        b.setBlock(-8, 1, 8, Material.STONE_BUTTON);

        // ── Gargoyle statues on mausoleum corners ───────────────────────
        for (int[] gg : new int[][]{{-4, -4}, {4, -4}}) {
            b.setBlock(gg[0], 8, gg[1], Material.POLISHED_BLACKSTONE);
            b.setBlock(gg[0], 9, gg[1], Material.WITHER_SKELETON_SKULL);
        }

        // ── Cobwebs ─────────────────────────────────────────────────────
        b.scatter(-13, 1, -13, 13, 5, 13, Material.AIR, Material.COBWEB, 0.06);
        for (int[] w : new int[][]{{-3, 3}, {3, -3}, {-3, -3}, {3, 3}}) {
            b.setBlock(w[0], 5, w[1], Material.COBWEB);
        }

        // ── Loot ────────────────────────────────────────────────────────
        b.placeChest(0, 1, 0);      // mausoleum center
        b.placeChest(0, -3, 0);     // crypt
        b.placeChest(-5, 0, 4);     // pit near NW
        b.placeChest(6, -1, -7);    // open grave loot
        b.placeChest(-8, -1, 6);    // second open grave
        b.setBossSpawn(0, 1, -5);
    }
}

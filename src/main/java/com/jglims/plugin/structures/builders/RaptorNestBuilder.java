package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;

/**
 * RaptorNestBuilder — coarse-dirt central nest with hay bedding, egg clutches, extended
 * cave sleeping hall, bone pile trophies, carcass kill site, claw-marked trees,
 * tracking prints, watch-perch rock, raptor-sign totems, cached egg chamber.
 */
public final class RaptorNestBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // ── Outer territory ring (disturbed earth) ──────────────────────
        b.filledCircle(0, 0, 0, 14, Material.COARSE_DIRT);
        b.scatter(-14, 0, -14, 14, 0, 14, Material.COARSE_DIRT, Material.PODZOL, 0.15);
        b.scatter(-14, 0, -14, 14, 0, 14, Material.COARSE_DIRT, Material.ROOTED_DIRT, 0.10);
        b.scatter(-14, 0, -14, 14, 0, 14, Material.COARSE_DIRT, Material.DIRT_PATH, 0.08);

        // ── Main nest (stick ring + hay interior) ───────────────────────
        b.filledCircle(0, 0, 0, 7, Material.COARSE_DIRT);
        b.circle(0, 1, 0, 7, Material.DARK_OAK_FENCE);
        b.circle(0, 1, 0, 6, Material.DARK_OAK_FENCE);
        b.circle(0, 2, 0, 7, Material.DARK_OAK_FENCE);
        b.circle(0, 3, 0, 7, Material.DARK_OAK_FENCE);
        // Nest interior (soft hay bedding)
        b.filledCircle(0, 0, 0, 5, Material.HAY_BLOCK);
        b.filledCircle(0, 1, 0, 3, Material.HAY_BLOCK);

        // ── Egg cluster (center + peripheral nests) ─────────────────────
        b.setBlock(0, 2, 0, Material.TURTLE_EGG);
        b.setBlock(-1, 2, 0, Material.TURTLE_EGG);
        b.setBlock(0, 2, -1, Material.TURTLE_EGG);
        b.setBlock(1, 2, 1, Material.TURTLE_EGG);
        b.setBlock(1, 2, -1, Material.TURTLE_EGG);
        b.setBlock(-1, 2, 1, Material.TURTLE_EGG);

        // ── Feather carpet scattering ───────────────────────────────────
        for (int x = -4; x <= 4; x += 2) {
            for (int z = -4; z <= 4; z += 2) {
                double dist = Math.sqrt(x * x + z * z);
                if (dist < 5 && dist > 2) {
                    b.setRandomBlock(x, 1, z, Material.WHITE_CARPET, Material.LIGHT_GRAY_CARPET,
                            Material.BROWN_CARPET);
                }
            }
        }

        // ── Scratch-mark trails radiating outward ───────────────────────
        for (int a = 0; a < 360; a += 30) {
            for (int d = 7; d <= 12; d++) {
                int sx = (int) (d * Math.cos(Math.toRadians(a)));
                int sz = (int) (d * Math.sin(Math.toRadians(a)));
                b.setBlock(sx, 0, sz, Material.DIRT_PATH);
            }
        }

        // ── Watch-perch rock (NW) ───────────────────────────────────────
        b.fillBox(-10, 1, -10, -8, 2, -8, Material.COBBLESTONE);
        b.setBlock(-9, 3, -9, Material.MOSSY_COBBLESTONE);
        b.setBlock(-9, 3, -9, Material.MOSSY_COBBLESTONE);
        b.setBlock(-9, 4, -9, Material.COBBLESTONE);
        b.setBlock(-8, 3, -10, Material.COBBLESTONE_SLAB);
        b.scatter(-10, 1, -10, -8, 4, -8, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE, 0.3);

        // ── Main cave entrance (south) ──────────────────────────────────
        b.fillBox(-3, 0, -12, 3, 4, -8, Material.STONE);
        b.fillBox(-2, 1, -12, 2, 3, -9, Material.AIR);
        b.scatter(-3, 0, -12, 3, 4, -8, Material.STONE, Material.COBBLESTONE, 0.2);
        // Entrance frame of bone
        b.setBlock(-2, 1, -12, Material.BONE_BLOCK);
        b.setBlock(2, 1, -12, Material.BONE_BLOCK);
        b.setBlock(-2, 2, -12, Material.BONE_BLOCK);
        b.setBlock(2, 2, -12, Material.BONE_BLOCK);
        b.setBlock(0, 3, -12, Material.SKELETON_SKULL);

        // ── Extended cave sleeping hall ─────────────────────────────────
        b.fillBox(-5, 0, -18, 5, 5, -11, Material.STONE);
        b.fillBox(-4, 1, -17, 4, 4, -12, Material.AIR);
        b.fillFloor(-4, -17, 4, -12, 0, Material.COARSE_DIRT);
        b.scatter(-4, 0, -17, 4, 0, -12, Material.COARSE_DIRT, Material.PODZOL, 0.3);
        // Sleeping nest pockets
        b.filledCircle(-3, 1, -14, 1, Material.HAY_BLOCK);
        b.filledCircle(3, 1, -14, 1, Material.HAY_BLOCK);
        b.filledCircle(0, 1, -16, 1, Material.HAY_BLOCK);
        b.setBlock(-3, 2, -14, Material.TURTLE_EGG);
        b.setBlock(3, 2, -14, Material.TURTLE_EGG);
        b.setBlock(0, 2, -16, Material.TURTLE_EGG);
        // Wall torch sockets (orange light from magma)
        b.setBlock(-4, 3, -14, Material.MAGMA_BLOCK);
        b.setBlock(4, 3, -14, Material.MAGMA_BLOCK);
        b.setBlock(0, 4, -16, Material.MAGMA_BLOCK);
        // Stalactite / stalagmite pairs
        for (int[] sl : new int[][]{{-2, -13}, {2, -15}, {-1, -17}, {1, -12}}) {
            b.setBlock(sl[0], 4, sl[1], Material.POINTED_DRIPSTONE);
            b.setBlock(sl[0], 1, sl[1], Material.POINTED_DRIPSTONE);
        }

        // ── Bone pile trophy display (W + E) ────────────────────────────
        b.fillBox(-9, 1, -6, -7, 2, -4, Material.BONE_BLOCK);
        b.setBlock(-8, 3, -5, Material.SKELETON_SKULL);
        b.setBlock(-8, 2, -4, Material.WITHER_SKELETON_SKULL);
        b.setBlock(-8, 2, -6, Material.BONE_BLOCK);
        b.fillBox(7, 1, 4, 9, 2, 6, Material.BONE_BLOCK);
        b.setBlock(8, 3, 5, Material.WITHER_SKELETON_SKULL);
        b.setBlock(8, 2, 4, Material.SKELETON_SKULL);

        // ── Kill site with carcass ──────────────────────────────────────
        b.setBlock(8, 1, -3, Material.RED_WOOL);
        b.setBlock(8, 1, -4, Material.BONE_BLOCK);
        b.setBlock(7, 1, -3, Material.LIGHT_GRAY_CARPET);
        b.setBlock(7, 1, -4, Material.WHITE_CARPET);
        b.setBlock(9, 1, -3, Material.BONE_BLOCK);
        b.setBlock(9, 1, -4, Material.RED_WOOL);
        b.setBlock(8, 2, -4, Material.SKELETON_SKULL);
        // Blood spatter (red terracotta)
        for (int[] sp : new int[][]{{6, -2}, {7, -5}, {10, -3}, {8, -6}}) {
            b.setBlock(sp[0], 0, sp[1], Material.RED_TERRACOTTA);
        }

        // ── Claw-marked trees around the nest ───────────────────────────
        int[][] trees = {{-10, 2}, {10, -2}, {-6, -10}, {10, 8}, {-12, -4}, {12, 6}, {-4, 12}};
        for (int[] tree : trees) {
            b.pillar(tree[0], 1, 6, tree[1], Material.DARK_OAK_LOG);
            for (int y = 4; y <= 7; y++) {
                b.filledCircle(tree[0], y, tree[1], 2, Material.DARK_OAK_LEAVES);
            }
            // Claw marks
            b.setBlock(tree[0] + 1, 3, tree[1], Material.STRIPPED_DARK_OAK_LOG);
            b.setBlock(tree[0], 3, tree[1] + 1, Material.STRIPPED_DARK_OAK_LOG);
            // Hanging bone totem
            if ((tree[0] + tree[1]) % 2 == 0) {
                b.setBlock(tree[0], 5, tree[1] + 1, Material.IRON_CHAIN);
                b.setBlock(tree[0], 4, tree[1] + 1, Material.BONE_BLOCK);
            }
        }

        // ── Raptor-sign totems (stacked bones with warning) ─────────────
        for (int[] tot : new int[][]{{-13, 0}, {13, 0}, {0, -13}, {0, 13}}) {
            b.pillar(tot[0], 1, 3, tot[1], Material.BONE_BLOCK);
            b.setBlock(tot[0], 4, tot[1], Material.SKELETON_SKULL);
        }

        // ── Hidden egg cache under the main nest ────────────────────────
        b.fillBox(-3, -4, -3, 3, -1, 3, Material.STONE);
        b.fillBox(-2, -3, -2, 2, -2, 2, Material.AIR);
        b.fillFloor(-2, -2, 2, 2, -4, Material.COARSE_DIRT);
        b.setBlock(0, -3, 0, Material.TURTLE_EGG);
        b.setBlock(-1, -3, 0, Material.TURTLE_EGG);
        b.setBlock(1, -3, 0, Material.TURTLE_EGG);
        b.setBlock(0, -3, -1, Material.TURTLE_EGG);
        b.setBlock(0, -3, 1, Material.TURTLE_EGG);
        b.setBlock(-2, -3, 0, Material.SOUL_LANTERN);
        b.setBlock(2, -3, 0, Material.SOUL_LANTERN);
        // Tunnel down
        for (int y = -3; y <= 0; y++) b.setBlock(2, y, 2, Material.LADDER);

        // ── Scattered small bones ───────────────────────────────────────
        for (int[] bn : new int[][]{{5, 3}, {-4, -5}, {6, -2}, {-5, 6}, {7, 9}, {-9, -2}, {11, -7}}) {
            b.setBlockIfSolid(bn[0], 1, bn[1], Material.BONE_BLOCK);
        }
        b.setBlock(6, 1, -2, Material.SKELETON_SKULL);
        b.setBlock(-5, 1, 6, Material.SKELETON_SKULL);

        // ── Loot ────────────────────────────────────────────────────────
        b.placeChest(0, 2, 2);       // nest interior
        b.placeChest(0, 2, -15);     // sleeping hall
        b.placeChest(0, -3, 0);      // hidden egg cache
        b.placeChest(-8, 2, -5);     // W bone pile
        b.placeChest(8, 2, 5);       // E bone pile
        b.setBossSpawn(0, 1, 0);
    }
}

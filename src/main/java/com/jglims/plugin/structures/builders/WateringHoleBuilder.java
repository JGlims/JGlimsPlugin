package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;

/**
 * WateringHoleBuilder — natural pond with stepped muddy banks, lily pads, palm fringe,
 * animal trails, prehistoric stone idol, dinosaur-bone fossil dig, egg clutch,
 * ranger's lookout, bamboo grove, drying mud prints, and hidden supply cache.
 */
public final class WateringHoleBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // ── Meadow base ─────────────────────────────────────────────────
        b.filledCircle(0, 0, 0, 16, Material.GRASS_BLOCK);
        b.scatter(-16, 0, -16, 16, 0, 16, Material.GRASS_BLOCK, Material.MOSS_BLOCK, 0.12);
        b.scatter(-16, 0, -16, 16, 0, 16, Material.GRASS_BLOCK, Material.PODZOL, 0.07);

        // ── Muddy banks (stepped) ───────────────────────────────────────
        b.filledCircle(0, 0, 0, 10, Material.MUD);
        b.filledCircle(0, 0, 0, 8, Material.MUDDY_MANGROVE_ROOTS);

        // ── Water pond (layered depth) ──────────────────────────────────
        b.filledCircle(0, 0, 0, 6, Material.WATER);
        b.filledCircle(0, -1, 0, 5, Material.WATER);
        b.filledCircle(0, -2, 0, 3, Material.WATER);
        b.filledCircle(0, -1, 0, 5, Material.CLAY);
        b.filledCircle(0, -2, 0, 3, Material.CLAY);

        // ── Palm trees (jungle + cherry mix for exotic look) ────────────
        int[][] treePos = {{-12, -9}, {12, -9}, {-10, 11}, {10, 11}, {-14, 3}, {14, -3}};
        for (int i = 0; i < treePos.length; i++) {
            int[] tp = treePos[i];
            b.pillar(tp[0], 1, 8 + (i % 3), tp[1], Material.JUNGLE_LOG);
            int crownY = 9 + (i % 3);
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (Math.abs(dx) + Math.abs(dz) <= 3) {
                        b.setBlockIfAir(tp[0] + dx, crownY, tp[1] + dz, Material.JUNGLE_LEAVES);
                    }
                }
            }
            // Coconut dangling
            b.setBlock(tp[0], crownY - 1, tp[1], Material.COCOA);
        }

        // ── Bamboo grove cluster (SW corner) ────────────────────────────
        for (int[] bm : new int[][]{{-14, 7}, {-13, 9}, {-15, 6}, {-12, 8}, {-14, 10}, {-15, 8}}) {
            b.pillar(bm[0], 1, 4 + bm[0] % 3, bm[1], Material.BAMBOO_BLOCK);
        }

        // ── Animal trails (coarse dirt cross-roads) ─────────────────────
        for (int i = -15; i <= -6; i++) b.setBlock(i, 0, 0, Material.COARSE_DIRT);
        for (int i = 6; i <= 15; i++) b.setBlock(i, 0, 0, Material.COARSE_DIRT);
        for (int i = -15; i <= -6; i++) b.setBlock(0, 0, i, Material.COARSE_DIRT);
        for (int i = 6; i <= 15; i++) b.setBlock(0, 0, i, Material.COARSE_DIRT);
        // Trail-edge foliage
        for (int i = -14; i <= 14; i += 3) {
            b.setBlockIfAir(i, 1, -1, Material.TALL_GRASS);
            b.setBlockIfAir(-1, 1, i, Material.TALL_GRASS);
        }

        // ── Drinking area (flat stones near water) ──────────────────────
        for (int z = -2; z <= 2; z++) {
            b.setBlock(-7, 0, z, Material.SMOOTH_STONE_SLAB);
            b.setBlock(7, 0, z, Material.SMOOTH_STONE_SLAB);
        }

        // ── Flowers + grass scatter in meadow ───────────────────────────
        for (int x = -15; x <= 15; x += 2) {
            for (int z = -15; z <= 15; z += 2) {
                double dist = Math.sqrt(x * x + z * z);
                if (dist > 10 && dist < 16) {
                    b.setRandomBlock(x, 1, z, Material.GRASS_BLOCK, Material.DANDELION,
                            Material.POPPY, Material.FERN, Material.SHORT_GRASS, Material.BLUE_ORCHID);
                }
            }
        }

        // ── Lily pads on water ──────────────────────────────────────────
        b.setBlock(-2, 1, 0, Material.LILY_PAD);
        b.setBlock(1, 1, 2, Material.LILY_PAD);
        b.setBlock(0, 1, -3, Material.LILY_PAD);
        b.setBlock(3, 1, 1, Material.LILY_PAD);
        b.setBlock(-3, 1, -2, Material.LILY_PAD);

        // ── Underwater plant life ───────────────────────────────────────
        b.setBlock(-3, 0, -2, Material.KELP_PLANT);
        b.setBlock(2, 0, 2, Material.KELP_PLANT);
        b.setBlock(-2, 0, 3, Material.KELP_PLANT);
        b.setBlock(-1, -1, 3, Material.SEA_LANTERN);
        b.setBlock(3, -1, -1, Material.SEA_LANTERN);
        b.setBlock(0, -2, 0, Material.SEA_LANTERN);

        // ── Prehistoric stone idol (north of pond) ──────────────────────
        b.pillar(0, 1, 5, -10, Material.MOSSY_COBBLESTONE);
        b.setBlock(0, 6, -10, Material.CARVED_PUMPKIN);
        b.setBlock(-1, 3, -10, Material.MOSSY_COBBLESTONE_WALL);
        b.setBlock(1, 3, -10, Material.MOSSY_COBBLESTONE_WALL);
        b.setBlock(0, 2, -11, Material.CAMPFIRE);
        b.setBlock(-1, 1, -10, Material.MOSSY_COBBLESTONE);
        b.setBlock(1, 1, -10, Material.MOSSY_COBBLESTONE);
        // Idol offerings
        b.setBlock(0, 1, -9, Material.DECORATED_POT);
        b.setBlock(-2, 1, -10, Material.DECORATED_POT);
        b.setBlock(2, 1, -10, Material.DECORATED_POT);

        // ── Animal footprints (dirt path) ───────────────────────────────
        for (int i = 0; i < 12; i++) {
            int fx = -4 - i;
            b.setBlock(fx, 0, 0 + (i % 2 == 0 ? 1 : -1), Material.DIRT_PATH);
        }
        // Parallel trail from the east
        for (int i = 0; i < 10; i++) {
            int fx = 4 + i;
            b.setBlock(fx, 0, 0 + (i % 2 == 0 ? 2 : -2), Material.DIRT_PATH);
        }

        // ── Dinosaur bone fossil dig (exposed skeleton SE) ──────────────
        b.fillBox(-12, 1, -12, -10, 1, -10, Material.BONE_BLOCK);
        b.setBlock(-11, 2, -11, Material.BONE_BLOCK);
        b.setBlock(-12, 2, -12, Material.BONE_BLOCK);
        b.setBlock(-10, 2, -10, Material.BONE_BLOCK);
        b.setBlock(-11, 1, -9, Material.BONE_BLOCK);
        b.setBlock(-9, 1, -11, Material.BONE_BLOCK);
        // Skull on top
        b.setBlock(-11, 3, -11, Material.SKELETON_SKULL);
        // Dig kit
        b.setBlock(-9, 1, -12, Material.CRAFTING_TABLE);
        b.setBlock(-10, 1, -13, Material.BARREL);
        b.setBlock(-9, 2, -12, Material.LANTERN);

        // ── Second bone pile (NE) ───────────────────────────────────────
        b.fillBox(12, 1, 10, 13, 1, 11, Material.BONE_BLOCK);
        b.setBlock(11, 1, 12, Material.BONE_BLOCK);

        // ── Egg clutch (hidden nest under palm grove) ───────────────────
        b.setBlock(-10, 1, 11, Material.TURTLE_EGG);
        b.setBlock(-11, 1, 11, Material.TURTLE_EGG);
        b.setBlock(-10, 1, 12, Material.TURTLE_EGG);
        b.setBlock(-9, 1, 11, Material.TURTLE_EGG);
        b.setBlock(-10, 1, 10, Material.PODZOL);

        // ── Ranger's lookout (elevated platform, west) ──────────────────
        for (int[] p : new int[][]{{-14, -4}, {-14, -2}, {-12, -4}, {-12, -2}}) {
            b.pillar(p[0], 1, 6, p[1], Material.OAK_LOG);
        }
        b.fillBox(-14, 6, -4, -12, 6, -2, Material.OAK_PLANKS);
        for (int x = -14; x <= -12; x++) {
            b.setBlock(x, 7, -4, Material.OAK_FENCE);
            b.setBlock(x, 7, -2, Material.OAK_FENCE);
        }
        b.setBlock(-14, 7, -3, Material.OAK_FENCE);
        b.setBlock(-12, 7, -3, Material.OAK_FENCE);
        b.pyramidRoof(-14, -4, -12, -2, 8, Material.SPRUCE_STAIRS);
        b.setBlock(-13, 7, -3, Material.LANTERN);
        b.setBlock(-13, 6, -3, Material.BARREL);
        for (int y = 1; y <= 6; y++) b.setBlock(-13, y, -1, Material.LADDER);

        // ── Berry bushes ────────────────────────────────────────────────
        for (int[] bb : new int[][]{{-10, 5}, {10, -5}, {-5, -14}, {5, 14}, {-13, 13}, {13, -13}}) {
            b.setBlockIfAir(bb[0], 1, bb[1], Material.SWEET_BERRY_BUSH);
        }

        // ── Fallen log across a trail ───────────────────────────────────
        b.fillBox(3, 1, 6, 8, 1, 6, Material.JUNGLE_LOG);
        b.setBlock(5, 2, 6, Material.MOSS_CARPET);
        b.setBlock(6, 2, 6, Material.MOSS_CARPET);
        b.setBlock(7, 2, 6, Material.MOSS_CARPET);
        b.setBlock(4, 2, 6, Material.BROWN_MUSHROOM);

        // ── Small hidden cache under a rock (NW) ────────────────────────
        b.setBlock(-9, 0, 9, Material.COBBLESTONE);
        b.fillBox(-9, -2, 9, -9, -1, 9, Material.AIR);
        b.setBlock(-9, -2, 10, Material.LANTERN);

        // ── Prehistoric rock-art panel (west cliff) ─────────────────────
        b.fillBox(-16, 1, -1, -16, 3, 1, Material.COBBLESTONE);
        b.setBlock(-16, 2, 0, Material.BROWN_CONCRETE);
        b.setBlock(-16, 2, -1, Material.ORANGE_CONCRETE);
        b.setBlock(-16, 2, 1, Material.ORANGE_CONCRETE);

        // ── Tiki torches around the meadow ──────────────────────────────
        for (int a = 0; a < 360; a += 60) {
            int tx = (int) (14 * Math.cos(Math.toRadians(a)));
            int tz = (int) (14 * Math.sin(Math.toRadians(a)));
            b.pillar(tx, 1, 3, tz, Material.STRIPPED_JUNGLE_LOG);
            b.setBlock(tx, 4, tz, Material.CAMPFIRE);
        }

        // ── Loot ────────────────────────────────────────────────────────
        b.placeChest(8, 1, 8);     // near fallen log
        b.placeChest(-9, -1, 9);   // hidden cache
        b.placeChest(0, 2, -8);    // idol offering
        b.placeChest(-10, 1, -13); // dig-kit supply
        b.placeChest(-13, 6, -3);  // ranger lookout
    }
}

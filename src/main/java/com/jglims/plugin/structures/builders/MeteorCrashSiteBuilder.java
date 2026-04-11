package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;

/**
 * MeteorCrashSiteBuilder — impact crater with blackstone meteor core, radiating cracks, and scorched debris.
 *
 * <p>Extracted verbatim from the original monolithic builder method.
 */
public final class MeteorCrashSiteBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // Impact crater (depression, radius 30)
        for (int x = -30; x <= 30; x++)
            for (int z = -30; z <= 30; z++) {
                double dist = Math.sqrt(x * x + z * z);
                if (dist <= 30) {
                    int depth = (int) (8 * (1 - dist / 30));
                    for (int y = 0; y >= -depth; y--) b.setBlock(x, y, z, Material.AIR);
                    b.setBlock(x, -depth, z, Material.GRAY_CONCRETE);
                }
            }

        // Meteor rock in center (blackstone + crying obsidian)
        b.filledCircle(0, -8, 0, 6, Material.BLACKSTONE);
        b.filledCircle(0, -7, 0, 5, Material.BLACKSTONE);
        b.filledCircle(0, -6, 0, 4, Material.CRYING_OBSIDIAN);
        b.filledCircle(0, -5, 0, 3, Material.CRYING_OBSIDIAN);
        b.filledCircle(0, -4, 0, 2, Material.BLACKSTONE);
        b.setBlock(0, -3, 0, Material.CRYING_OBSIDIAN);

        // Scatter obsidian on meteor
        b.scatter(-6, -8, -6, 6, -3, 6, Material.BLACKSTONE, Material.OBSIDIAN, 0.2);

        // Radiating crack lines (magma blocks, 8 directions)
        for (int a = 0; a < 360; a += 45) {
            double rad = Math.toRadians(a);
            for (int d = 7; d <= 25; d++) {
                int cx = (int) (d * Math.cos(rad));
                int cz = (int) (d * Math.sin(rad));
                int depth = (int) (6 * (1 - (double) d / 30));
                b.setBlock(cx, -depth, cz, Material.MAGMA_BLOCK);
            }
        }

        // Scattered debris (fragments)
        for (int i = 0; i < 12; i++) {
            int dx = -20 + (i * 37 % 40);
            int dz = -15 + (i * 23 % 30);
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist > 8 && dist < 28) {
                b.setBlock(dx, 0, dz, Material.BLACKSTONE);
                b.setBlock(dx + 1, 0, dz, Material.CRYING_OBSIDIAN);
            }
        }

        // Exposed rare ores in crater walls
        b.setBlock(-15, -3, -15, Material.DIAMOND_ORE);
        b.setBlock(12, -2, -18, Material.GOLD_ORE);
        b.setBlock(-20, -2, 5, Material.EMERALD_ORE);
        b.setBlock(18, -3, 10, Material.LAPIS_ORE);
        b.setBlock(0, -6, -12, Material.ANCIENT_DEBRIS);
        b.setBlock(8, -5, 8, Material.ANCIENT_DEBRIS);

        // Small fires near impact
        b.setBlock(-3, -7, 2, Material.FIRE);
        b.setBlock(2, -7, -3, Material.FIRE);
        b.setBlock(4, -6, 4, Material.FIRE);

        // ─── Meteor shards radiating from impact (8 spikes of black/magma) ───
        for (int i = 0; i < 8; i++) {
            double rad = Math.toRadians(i * 45);
            int sx = (int) Math.round(10 * Math.cos(rad));
            int sz = (int) Math.round(10 * Math.sin(rad));
            b.pillar(sx, -5, 3, sz, Material.BLACKSTONE);
            b.setBlock(sx, -2, sz, Material.MAGMA_BLOCK);
            b.setBlock(sx, -1, sz, Material.BLACKSTONE);
        }

        // ─── Crash-landing alien debris (iron bars + redstone from a craft) ───
        for (int[] wreck : new int[][]{{-12, -5}, {15, -3}, {-8, 12}, {10, 14}}) {
            b.setBlock(wreck[0], -2, wreck[1], Material.IRON_BLOCK);
            b.setBlock(wreck[0] + 1, -2, wreck[1], Material.IRON_BARS);
            b.setBlock(wreck[0], -2, wreck[1] + 1, Material.REDSTONE_LAMP);
            b.decay(wreck[0] - 1, -3, wreck[1] - 1, wreck[0] + 2, -1, wreck[1] + 2, 0.4);
        }

        // ─── Soul fire column at the center (dramatic landing mark) ───
        b.setBlock(0, -2, 0, Material.SOUL_FIRE);
        b.setBlock(-1, -2, 0, Material.SOUL_FIRE);
        b.setBlock(1, -2, 0, Material.SOUL_FIRE);
        b.setBlock(0, -2, -1, Material.SOUL_FIRE);
        b.setBlock(0, -2, 1, Material.SOUL_FIRE);

        // ─── Meteor core: hidden chamber inside the meteor with ancient tech ───
        b.fillBox(-2, -10, -2, 2, -7, 2, Material.BLACKSTONE);
        b.fillBox(-1, -9, -1, 1, -8, 1, Material.AIR);
        b.setBlock(0, -9, 0, Material.RESPAWN_ANCHOR);
        b.setBlock(0, -8, 0, Material.BEACON);

        // ─── Scorched earth around the crater rim ───
        for (int a = 0; a < 360; a += 10) {
            double rad = Math.toRadians(a);
            int rx = (int) Math.round(28 * Math.cos(rad));
            int rz = (int) Math.round(28 * Math.sin(rad));
            if (Math.random() < 0.4) b.setBlockIfSolid(rx, 0, rz, Material.COARSE_DIRT);
        }

        b.placeChest(0, -3, 2);
        b.placeChest(-10, -2, 0);
        b.placeChest(0, -8, 0);   // hidden core chest
    }
}

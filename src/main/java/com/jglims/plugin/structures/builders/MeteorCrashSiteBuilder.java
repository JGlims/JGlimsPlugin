package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;

/**
 * MeteorCrashSiteBuilder — fresh impact crater with blackstone meteor core, radiating
 * magma cracks, scientist research tent, warning perimeter, sample containment,
 * ancient-debris veins, crashed alien pod fragments, soul-fire center, hidden core chamber.
 */
public final class MeteorCrashSiteBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // ── Impact crater (bowl, radius 30) ─────────────────────────────
        for (int x = -30; x <= 30; x++) {
            for (int z = -30; z <= 30; z++) {
                double dist = Math.sqrt(x * x + z * z);
                if (dist <= 30) {
                    int depth = (int) (8 * (1 - dist / 30));
                    for (int y = 0; y >= -depth; y--) b.setBlock(x, y, z, Material.AIR);
                    b.setBlock(x, -depth, z, Material.GRAY_CONCRETE);
                }
            }
        }
        // Crater lining weathering
        b.scatter(-30, -8, -30, 30, 0, 30, Material.GRAY_CONCRETE, Material.COARSE_DIRT, 0.15);
        b.scatter(-30, -8, -30, 30, 0, 30, Material.GRAY_CONCRETE, Material.GRAVEL, 0.20);

        // ── Meteor core (layered blackstone + crying obsidian) ──────────
        b.filledCircle(0, -8, 0, 6, Material.BLACKSTONE);
        b.filledCircle(0, -7, 0, 5, Material.BLACKSTONE);
        b.filledCircle(0, -6, 0, 4, Material.CRYING_OBSIDIAN);
        b.filledCircle(0, -5, 0, 3, Material.CRYING_OBSIDIAN);
        b.filledCircle(0, -4, 0, 2, Material.BLACKSTONE);
        b.setBlock(0, -3, 0, Material.CRYING_OBSIDIAN);
        b.scatter(-6, -8, -6, 6, -3, 6, Material.BLACKSTONE, Material.OBSIDIAN, 0.20);
        b.scatter(-6, -8, -6, 6, -3, 6, Material.BLACKSTONE, Material.BASALT, 0.15);

        // ── Radiating magma cracks (8 directions) ───────────────────────
        for (int a = 0; a < 360; a += 45) {
            double rad = Math.toRadians(a);
            for (int d = 7; d <= 25; d++) {
                int cx = (int) (d * Math.cos(rad));
                int cz = (int) (d * Math.sin(rad));
                int depth = (int) (6 * (1 - (double) d / 30));
                b.setBlock(cx, -depth, cz, Material.MAGMA_BLOCK);
            }
        }

        // ── 8 meteor shard spikes ───────────────────────────────────────
        for (int i = 0; i < 8; i++) {
            double rad = Math.toRadians(i * 45);
            int sx = (int) Math.round(10 * Math.cos(rad));
            int sz = (int) Math.round(10 * Math.sin(rad));
            b.pillar(sx, -5, 4, sz, Material.BLACKSTONE);
            b.setBlock(sx, -1, sz, Material.MAGMA_BLOCK);
            b.setBlock(sx, 0, sz, Material.BLACKSTONE);
        }

        // ── Scattered debris (larger chunks) ────────────────────────────
        for (int i = 0; i < 20; i++) {
            int dx = -20 + (i * 37 % 40);
            int dz = -15 + (i * 23 % 30);
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist > 8 && dist < 28) {
                b.setBlock(dx, 0, dz, Material.BLACKSTONE);
                b.setBlock(dx + 1, 0, dz, Material.CRYING_OBSIDIAN);
                if (i % 3 == 0) b.setBlock(dx, 1, dz, Material.BLACKSTONE);
            }
        }

        // ── Exposed rare ore veins in crater walls ──────────────────────
        b.setBlock(-15, -3, -15, Material.DIAMOND_ORE);
        b.setBlock(-14, -3, -15, Material.DIAMOND_ORE);
        b.setBlock(12, -2, -18, Material.GOLD_ORE);
        b.setBlock(13, -2, -18, Material.GOLD_ORE);
        b.setBlock(-20, -2, 5, Material.EMERALD_ORE);
        b.setBlock(18, -3, 10, Material.LAPIS_ORE);
        b.setBlock(19, -3, 10, Material.LAPIS_ORE);
        b.setBlock(0, -6, -12, Material.ANCIENT_DEBRIS);
        b.setBlock(8, -5, 8, Material.ANCIENT_DEBRIS);
        b.setBlock(-8, -5, 8, Material.ANCIENT_DEBRIS);

        // ── Small fires near impact ─────────────────────────────────────
        b.setBlock(-3, -7, 2, Material.FIRE);
        b.setBlock(2, -7, -3, Material.FIRE);
        b.setBlock(4, -6, 4, Material.FIRE);
        b.setBlock(-4, -6, -4, Material.FIRE);

        // ── Soul-fire column at the center (dramatic landing mark) ──────
        for (int[] sf : new int[][]{{0, 0}, {-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, -1}, {1, 1}, {-1, 1}, {1, -1}}) {
            b.setBlock(sf[0], -2, sf[1], Material.SOUL_FIRE);
        }

        // ── Hidden meteor-core chamber ──────────────────────────────────
        b.fillBox(-3, -11, -3, 3, -8, 3, Material.BLACKSTONE);
        b.fillBox(-2, -10, -2, 2, -9, 2, Material.AIR);
        b.fillFloor(-2, -2, 2, 2, -10, Material.CRYING_OBSIDIAN);
        b.setBlock(0, -9, 0, Material.RESPAWN_ANCHOR);
        b.setBlock(0, -8, 0, Material.BEACON);
        b.setBlock(-2, -9, 0, Material.SOUL_LANTERN);
        b.setBlock(2, -9, 0, Material.SOUL_LANTERN);
        b.setBlock(0, -9, -2, Material.SOUL_LANTERN);
        b.setBlock(0, -9, 2, Material.SOUL_LANTERN);
        // Corner gilded markers
        for (int[] c : new int[][]{{-2, -2}, {2, -2}, {-2, 2}, {2, 2}}) {
            b.setBlock(c[0], -9, c[1], Material.GILDED_BLACKSTONE);
        }

        // ── Research tent (north rim) ───────────────────────────────────
        b.fillBox(-4, 1, -32, 4, 0, -26, Material.COBBLESTONE);
        b.fillWalls(-4, 1, -32, 4, 3, -26, Material.OAK_PLANKS);
        b.hollowBox(-4, 1, -32, 4, 3, -26);
        b.fillBox(-3, 4, -32, 3, 4, -26, Material.YELLOW_WOOL);  // tent canvas roof
        b.setBlock(0, 1, -26, Material.AIR);  // doorway
        b.setBlock(0, 2, -26, Material.AIR);
        // Interior
        b.setBlock(-3, 1, -31, Material.CRAFTING_TABLE);
        b.setBlock(-2, 1, -31, Material.BREWING_STAND);
        b.setBlock(-1, 1, -31, Material.CAULDRON);
        b.setBlock(0, 1, -31, Material.ENCHANTING_TABLE);
        b.setBlock(1, 1, -31, Material.SMITHING_TABLE);
        b.setBlock(2, 1, -31, Material.GRINDSTONE);
        b.setBlock(3, 1, -31, Material.LECTERN);
        // Bedroll
        b.setBlock(-2, 1, -28, Material.WHITE_BED);
        // Light
        b.setBlock(-3, 3, -29, Material.LANTERN);
        b.setBlock(3, 3, -29, Material.LANTERN);
        // Radio table
        b.table(0, 1, -28);
        b.setBlock(0, 2, -28, Material.JUKEBOX);
        b.setBlock(-1, 2, -28, Material.REDSTONE_LAMP);

        // ── Crashed alien-pod wreckage scattered around rim ─────────────
        for (int[] wreck : new int[][]{{-12, -5}, {15, -3}, {-8, 12}, {10, 14}, {-15, 8}, {16, -14}}) {
            b.setBlock(wreck[0], -2, wreck[1], Material.IRON_BLOCK);
            b.setBlock(wreck[0] + 1, -2, wreck[1], Material.IRON_BARS);
            b.setBlock(wreck[0], -1, wreck[1], Material.IRON_BARS);
            b.setBlock(wreck[0], -2, wreck[1] + 1, Material.REDSTONE_LAMP);
            b.decay(wreck[0] - 1, -3, wreck[1] - 1, wreck[0] + 2, -1, wreck[1] + 2, 0.4);
        }

        // ── Warning perimeter posts (containment line) ──────────────────
        for (int a = 0; a < 360; a += 30) {
            double rad = Math.toRadians(a);
            int px = (int) Math.round(20 * Math.cos(rad));
            int pz = (int) Math.round(20 * Math.sin(rad));
            b.setBlock(px, 1, pz, Material.POLISHED_BLACKSTONE);
            b.setBlock(px, 2, pz, Material.YELLOW_TERRACOTTA);
            b.setBlock(px, 3, pz, Material.REDSTONE_LAMP);
        }
        // Caution-tape rings (yellow concrete at ground level)
        for (int a = 0; a < 360; a += 10) {
            double rad = Math.toRadians(a);
            int rx = (int) Math.round(19 * Math.cos(rad));
            int rz = (int) Math.round(19 * Math.sin(rad));
            if (Math.random() < 0.5) b.setBlock(rx, 1, rz, Material.YELLOW_CONCRETE);
            else b.setBlock(rx, 1, rz, Material.BLACK_CONCRETE);
        }

        // ── Scorched earth around the crater rim ────────────────────────
        for (int a = 0; a < 360; a += 8) {
            double rad = Math.toRadians(a);
            int rx = (int) Math.round(28 * Math.cos(rad));
            int rz = (int) Math.round(28 * Math.sin(rad));
            if (Math.random() < 0.5) b.setBlockIfSolid(rx, 0, rz, Material.COARSE_DIRT);
            if (Math.random() < 0.3) b.setBlockIfSolid(rx, 1, rz, Material.DEAD_BUSH);
        }

        // ── Sample barrels (north rim near tent) ────────────────────────
        for (int[] smp : new int[][]{{-6, -27}, {6, -27}, {-6, -28}, {6, -28}}) {
            b.setBlock(smp[0], 1, smp[1], Material.BARREL);
            b.setBlock(smp[0], 2, smp[1], Material.BARREL);
        }

        // ── Loot ────────────────────────────────────────────────────────
        b.placeChest(0, -3, 2);     // meteor-rim chest
        b.placeChest(-10, -2, 0);   // magma-crack trove
        b.placeChest(0, -8, 0);     // hidden core chest
        b.placeChest(0, 1, -30);    // research-tent supplies
        b.placeChest(-6, 1, -27);   // sample barrels
    }
}

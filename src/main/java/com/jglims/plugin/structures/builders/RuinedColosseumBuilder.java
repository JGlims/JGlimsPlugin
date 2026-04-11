package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

/**
 * RuinedColosseumBuilder — a Roman-style elliptical arena with broken columns, tiered
 * stone-brick seating, arches, an underground hypogeum of gladiator cells, and a
 * champion's trophy chamber.
 *
 * <p>Extracted verbatim from the original {@code OverworldStructureBuilders.buildRuinedColosseum}
 * method to give the structure its own file per the one-class-per-structure
 * architecture.
 */
public final class RuinedColosseumBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // ─── Roman colosseum: ellipse 36x24, 5 tiers, broken arches, moss & vines ───

        // Foundation slab
        for (int yy = -2; yy <= 0; yy++) b.filledEllipse(0, yy, 0, 19, 13, Material.SMOOTH_SANDSTONE);
        // Sand arena floor (slightly recessed)
        b.filledEllipse(0, 0, 0, 14, 9, Material.SAND);
        b.filledEllipse(0, 0, 0, 12, 7, Material.SMOOTH_SANDSTONE);
        // Subtle blood-stained patches
        b.scatter(-12, 0, -7, 12, 0, 7, Material.SMOOTH_SANDSTONE, Material.RED_TERRACOTTA, 0.04);

        // ─── 5 stepped seating tiers (each with stair lip facing inward) ───
        for (int tier = 0; tier < 5; tier++) {
            int rx = 14 + tier * 2;
            int rz = 9 + tier * 2;
            int y = tier * 2 + 1;
            // Tier wall ring
            for (int a = 0; a < 360; a += 1) {
                int x = (int) Math.round(rx * Math.cos(Math.toRadians(a)));
                int z = (int) Math.round(rz * Math.sin(Math.toRadians(a)));
                b.setBlock(x, y, z, Material.STONE_BRICKS);
                b.setBlock(x, y + 1, z, Material.STONE_BRICKS);
            }
            // Stair seating lip facing inward toward arena
            for (int a = 0; a < 360; a += 6) {
                int x = (int) Math.round((rx - 1) * Math.cos(Math.toRadians(a)));
                int z = (int) Math.round((rz - 1) * Math.sin(Math.toRadians(a)));
                org.bukkit.block.BlockFace facing = (Math.abs(x) > Math.abs(z) * (rx / (double) rz))
                        ? (x > 0 ? org.bukkit.block.BlockFace.WEST : org.bukkit.block.BlockFace.EAST)
                        : (z > 0 ? org.bukkit.block.BlockFace.NORTH : org.bukkit.block.BlockFace.SOUTH);
                b.setStairs(x, y + 1, z, Material.STONE_BRICK_STAIRS, facing, false);
            }
        }

        // ─── 12 outer columns (some intact, some broken) with arches between ───
        int[] colHeights = new int[12];
        for (int i = 0; i < 12; i++) {
            int a = i * 30;
            int px = (int) Math.round(24 * Math.cos(Math.toRadians(a)));
            int pz = (int) Math.round(15 * Math.sin(Math.toRadians(a)));
            // Random broken / standing
            int h = 14 + b.getRandom().nextInt(5);
            if (b.getRandom().nextDouble() < 0.33) h = 4 + b.getRandom().nextInt(4);
            colHeights[i] = h;
            // Fluted column (chiseled stone brick + stone brick wall capital)
            b.pillar(px, 1, h, pz, Material.STONE_BRICKS);
            if (h > 6) {
                // Capital
                b.setBlock(px - 1, h, pz, Material.STONE_BRICK_SLAB);
                b.setBlock(px + 1, h, pz, Material.STONE_BRICK_SLAB);
                b.setBlock(px, h, pz - 1, Material.STONE_BRICK_SLAB);
                b.setBlock(px, h, pz + 1, Material.STONE_BRICK_SLAB);
                b.setBlock(px, h + 1, pz, Material.CHISELED_STONE_BRICKS);
            } else {
                // Broken — add rubble at base
                b.setBlock(px + 1, 1, pz, Material.STONE_BRICK_WALL);
                b.setBlock(px - 1, 1, pz, Material.STONE_BRICK_WALL);
                b.setBlock(px, 1, pz + 1, Material.MOSSY_COBBLESTONE);
            }
        }

        // ─── Arches connecting adjacent intact columns ───
        for (int i = 0; i < 12; i++) {
            int a1 = i * 30;
            int a2 = ((i + 1) % 12) * 30;
            int h1 = colHeights[i];
            int h2 = colHeights[(i + 1) % 12];
            if (h1 < 10 || h2 < 10) continue;  // skip if either is broken
            int x1 = (int) Math.round(24 * Math.cos(Math.toRadians(a1)));
            int z1 = (int) Math.round(15 * Math.sin(Math.toRadians(a1)));
            int x2 = (int) Math.round(24 * Math.cos(Math.toRadians(a2)));
            int z2 = (int) Math.round(15 * Math.sin(Math.toRadians(a2)));
            int peakY = Math.min(h1, h2) + 2;
            // Trace an arch between the two columns
            int steps = 8;
            for (int t = 0; t <= steps; t++) {
                double f = (double) t / steps;
                int ax = (int) Math.round(x1 + (x2 - x1) * f);
                int az = (int) Math.round(z1 + (z2 - z1) * f);
                int ay = h1 + (int) Math.round(Math.sin(f * Math.PI) * 2);
                if (t > 0 && t < steps) ay = peakY - (int) Math.round(Math.abs(0.5 - f) * 4);
                b.setBlock(ax, ay, az, Material.STONE_BRICKS);
                b.setBlock(ax, ay - 1, az, Material.STONE_BRICK_WALL);
            }
        }

        // ─── Underground hypogeum: gladiator holding cells beneath the arena ───
        // South cell row
        for (int cell = -3; cell <= 3; cell++) {
            int cx = cell * 4;
            b.fillBox(cx - 1, -5, 8, cx + 1, -2, 12, Material.STONE_BRICKS);
            b.fillBox(cx - 1, -5, 9, cx + 1, -3, 11, Material.AIR);
            b.fillFloor(cx - 1, 9, cx + 1, 11, -5, Material.SMOOTH_SANDSTONE);
            // Iron bar door
            b.setBlock(cx, -4, 8, Material.IRON_BARS);
            b.setBlock(cx, -3, 8, Material.IRON_BARS);
            // Hay bedding
            b.setBlock(cx, -4, 11, Material.HAY_BLOCK);
            // Wall torch
            b.setBlock(cx, -3, 12, Material.SOUL_TORCH);
        }
        // North cell row (mirrored)
        for (int cell = -3; cell <= 3; cell++) {
            int cx = cell * 4;
            b.fillBox(cx - 1, -5, -12, cx + 1, -2, -8, Material.STONE_BRICKS);
            b.fillBox(cx - 1, -5, -11, cx + 1, -3, -9, Material.AIR);
            b.fillFloor(cx - 1, -11, cx + 1, -9, -5, Material.SMOOTH_SANDSTONE);
            b.setBlock(cx, -4, -8, Material.IRON_BARS);
            b.setBlock(cx, -3, -8, Material.IRON_BARS);
            b.setBlock(cx, -4, -11, Material.HAY_BLOCK);
            b.setBlock(cx, -3, -12, Material.SOUL_TORCH);
        }
        // Connecting underground corridor
        b.fillBox(-14, -5, -2, 14, -3, 2, Material.AIR);
        b.fillFloor(-14, -2, 14, 2, -5, Material.SMOOTH_SANDSTONE);
        for (int x = -13; x <= 13; x++) {
            b.setBlock(x, -3, -3, Material.STONE_BRICKS);
            b.setBlock(x, -3, 3, Material.STONE_BRICKS);
            if (x % 4 == 0) {
                b.setBlock(x, -2, -3, Material.SOUL_LANTERN);
                b.setBlock(x, -2, 3, Material.SOUL_LANTERN);
            }
        }
        // Trapdoor up to arena floor (center)
        b.fillBox(-1, -5, -1, 1, 0, 1, Material.AIR);
        for (int s = -4; s <= -1; s++) b.setBlock(0, s, s + 4, Material.SMOOTH_SANDSTONE_STAIRS);

        // ─── Champion's trophy chamber (north hypogeum end) ───
        b.fillBox(-5, -5, -18, 5, 0, -12, Material.STONE_BRICKS);
        b.fillBox(-4, -4, -17, 4, -1, -13, Material.AIR);
        b.fillFloor(-4, -17, 4, -13, -5, Material.POLISHED_BLACKSTONE);
        // Throne
        b.setStairs(0, -4, -16, Material.STONE_BRICK_STAIRS, BlockFace.SOUTH, false);
        b.setBlock(-1, -4, -16, Material.GOLD_BLOCK);
        b.setBlock(1, -4, -16, Material.GOLD_BLOCK);
        // Trophy weapons on the wall
        b.setBlock(-3, -3, -17, Material.LIGHTNING_ROD);
        b.setBlock(3, -3, -17, Material.LIGHTNING_ROD);
        b.setBlock(0, -3, -17, Material.GOLDEN_SWORD);
        // Brazier
        b.setBlock(-3, -4, -14, Material.CAMPFIRE);
        b.setBlock(3, -4, -14, Material.CAMPFIRE);
        // Hanging banners (red wool)
        b.setBlock(-2, -2, -17, Material.RED_WOOL);
        b.setBlock(2, -2, -17, Material.RED_WOOL);

        // ─── Two grand entrance gates (east and west of the arena) ───
        b.gothicArch(-19, 1, 0, 6, 8, Material.STONE_BRICKS);
        b.gothicArch(19, 1, 0, 6, 8, Material.STONE_BRICKS);
        b.fillBox(-19, 1, -2, -19, 8, 2, Material.AIR);  // arch interior west
        b.fillBox(19, 1, -2, 19, 8, 2, Material.AIR);    // arch interior east

        // ─── Imperial standards on tall poles around the arena ───
        for (int i = 0; i < 4; i++) {
            int a = i * 90 + 45;
            int sx = (int) Math.round(28 * Math.cos(Math.toRadians(a)));
            int sz = (int) Math.round(18 * Math.sin(Math.toRadians(a)));
            b.pillar(sx, 1, 12, sz, Material.STRIPPED_DARK_OAK_LOG);
            b.setBlock(sx, 13, sz, Material.GOLD_BLOCK);
            // Banner
            for (int by = 9; by <= 12; by++) b.setBlock(sx + 1, by, sz, Material.RED_WOOL);
        }

        // ─── Decay, moss, vines, broken seating ───
        b.scatter(-26, 0, -18, 26, 12, 18, Material.STONE_BRICKS, Material.CRACKED_STONE_BRICKS, 0.30);
        b.scatter(-26, 0, -18, 26, 12, 18, Material.STONE_BRICKS, Material.MOSSY_STONE_BRICKS, 0.20);
        b.scatter(-26, 0, -18, 26, 12, 18, Material.CRACKED_STONE_BRICKS, Material.MOSSY_COBBLESTONE, 0.15);
        b.decay(-26, 6, -18, 26, 12, 18, 0.20);
        b.addVines(-26, 1, -18, 26, 12, 18, 0.10);

        // Loot
        b.placeChest(0, -4, -15);   // trophy room
        b.placeChest(0, 1, 0);      // arena center
        b.placeChest(-12, -4, 10);  // hypogeum south
        b.placeChest(12, -4, -10);  // hypogeum north

        b.setBossSpawn(0, 1, 0);
    }
}

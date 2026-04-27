package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

/**
 * ThanosTempleBuilder — gothic obsidian pyramid with grand throne, six Infinity Stone
 * pedestals, kneeling guard statues, chained altar, and layered underground vault.
 */
public final class ThanosTempleBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // ── Stepped pyramid base (5 tiers, material layers) ─────────────
        for (int step = 0; step < 5; step++) {
            int r = 22 - step * 4;
            int y = step * 4;
            b.fillBox(-r, y, -r, r, y, r, Material.OBSIDIAN);
            b.fillWalls(-r, y + 1, -r, r, y + 3, r, Material.PURPUR_BLOCK);
            b.hollowBox(-r, y + 1, -r, r, y + 3, r);
            // Tier cornice (polished blackstone rim)
            for (int a : new int[]{-r, r}) {
                b.fillBox(a, y + 3, -r, a, y + 3, r, Material.POLISHED_BLACKSTONE);
                b.fillBox(-r, y + 3, a, r, y + 3, a, Material.POLISHED_BLACKSTONE);
            }
            // Tier crying-obsidian accents on corners
            for (int[] c : new int[][]{{-r, -r}, {r, -r}, {-r, r}, {r, r}}) {
                b.pillar(c[0], y + 1, y + 3, c[1], Material.CRYING_OBSIDIAN);
            }
        }

        // ── Top platform + inner sanctum floor ───────────────────────────
        b.fillBox(-6, 20, -6, 6, 20, 6, Material.PURPUR_BLOCK);
        b.fillBox(-5, 21, -5, 5, 21, 5, Material.POLISHED_BLACKSTONE);
        // Gold inlay spoked around the throne center
        b.setBlock(0, 21, 0, Material.GOLD_BLOCK);
        for (int d : new int[]{-2, 2}) {
            b.setBlock(d, 21, 0, Material.GILDED_BLACKSTONE);
            b.setBlock(0, 21, d, Material.GILDED_BLACKSTONE);
            b.setBlock(d, 21, d, Material.GILDED_BLACKSTONE);
            b.setBlock(-d, 21, d, Material.GILDED_BLACKSTONE);
        }

        // ── Purple glass dome with cross-beam supports ───────────────────
        b.dome(0, 22, 0, 6, Material.PURPLE_STAINED_GLASS);
        for (int a = 0; a < 360; a += 90) {
            double rad = Math.toRadians(a);
            int cx = (int) (5 * Math.cos(rad));
            int cz = (int) (5 * Math.sin(rad));
            b.pillar(cx, 21, 26, cz, Material.PURPUR_PILLAR);
        }

        // ── Grand throne ────────────────────────────────────────────────
        b.fillBox(-2, 21, 2, 2, 21, 4, Material.GOLD_BLOCK);    // dais
        b.fillBox(-1, 22, 2, 1, 22, 2, Material.GOLD_BLOCK);    // seat
        b.fillBox(-2, 23, 4, 2, 26, 4, Material.GOLD_BLOCK);    // high back
        b.setBlock(-2, 22, 2, Material.GILDED_BLACKSTONE);      // armrests
        b.setBlock(2, 22, 2, Material.GILDED_BLACKSTONE);
        b.setBlock(-2, 23, 2, Material.GILDED_BLACKSTONE);
        b.setBlock(2, 23, 2, Material.GILDED_BLACKSTONE);
        // Crown of spikes along the top of the back
        for (int x = -2; x <= 2; x++) b.setBlock(x, 27, 4, Material.END_ROD);
        // Purple carpet approach
        for (int z = -1; z <= 1; z++) b.setBlock(0, 21, z, Material.PURPLE_WOOL);
        for (int z = -5; z <= -2; z++) b.setBlock(0, 21, z, Material.PURPLE_WOOL);

        // ── 6 Infinity Stone pedestals (Mind, Soul, Reality, Time, Space, Power) ──
        Material[] stoneMats = {
                Material.YELLOW_CONCRETE,  // Mind
                Material.ORANGE_CONCRETE,  // Soul
                Material.RED_CONCRETE,     // Reality
                Material.GREEN_CONCRETE,   // Time
                Material.BLUE_CONCRETE,    // Space
                Material.PURPLE_CONCRETE   // Power
        };
        int[][] pedestals = {{-4, 0}, {4, 0}, {0, -4}, {-3, -3}, {3, -3}, {0, -5}};
        for (int i = 0; i < 6; i++) {
            int px = pedestals[i][0], pz = pedestals[i][1];
            b.setBlock(px, 21, pz, Material.POLISHED_BLACKSTONE);
            b.setBlock(px, 22, pz, Material.QUARTZ_PILLAR);
            b.setBlock(px, 23, pz, Material.QUARTZ_PILLAR);
            b.setBlock(px, 24, pz, stoneMats[i]);
        }

        // ── Front entrance + grand staircase ────────────────────────────
        b.fillBox(-3, 1, -22, 3, 6, -22, Material.AIR);
        b.setBlock(-4, 7, -22, Material.PURPUR_PILLAR);
        b.setBlock(4, 7, -22, Material.PURPUR_PILLAR);
        // Archway lintel
        for (int x = -4; x <= 4; x++) b.setBlock(x, 8, -22, Material.POLISHED_BLACKSTONE);
        b.setBlock(0, 9, -22, Material.GILDED_BLACKSTONE);

        for (int s = 0; s < 20; s++) {
            b.fillBox(-2, s, -20 + s, 2, s, -20 + s, Material.PURPUR_STAIRS);
        }

        // ── Guardian braziers along staircase ───────────────────────────
        for (int s = 0; s < 20; s += 4) {
            b.pillar(-4, s, s + 5, -20 + s, Material.PURPUR_PILLAR);
            b.pillar(4, s, s + 5, -20 + s, Material.PURPUR_PILLAR);
            // Top-of-column braziers
            if (s % 8 == 0) {
                b.setBlock(-4, s + 6, -20 + s, Material.CAMPFIRE);
                b.setBlock(4, s + 6, -20 + s, Material.CAMPFIRE);
            }
        }

        // ── Kneeling guard statues (armor stand silhouettes from blocks) ──
        for (int[] gp : new int[][]{{-6, -15}, {6, -15}, {-6, -8}, {6, -8}}) {
            b.setBlock(gp[0], 0, gp[1], Material.POLISHED_BLACKSTONE);           // base
            b.setBlock(gp[0], 1, gp[1], Material.NETHERITE_BLOCK);               // torso
            b.setBlock(gp[0], 2, gp[1], Material.CARVED_PUMPKIN);                // head
            b.setBlock(gp[0], 1, gp[1] + (gp[1] < 0 ? 1 : -1), Material.IRON_BARS); // spear shaft
        }

        // ── Chained altar in front of the throne ────────────────────────
        b.setBlock(0, 21, -1, Material.GILDED_BLACKSTONE);
        b.setBlock(0, 22, -1, Material.CHISELED_POLISHED_BLACKSTONE);
        for (int h = 22; h <= 25; h++) {
            b.setBlock(-1, h, -1, Material.IRON_CHAIN);
            b.setBlock(1, h, -1, Material.IRON_CHAIN);
        }

        // ── Soul-flame lighting ring around the dome base ───────────────
        for (int a = 0; a < 360; a += 30) {
            int lx = (int) (5 * Math.cos(Math.toRadians(a)));
            int lz = (int) (5 * Math.sin(Math.toRadians(a)));
            b.setBlock(lx, 22, lz, Material.SOUL_LANTERN);
        }

        // ── Underground vault ───────────────────────────────────────────
        b.fillBox(-15, -8, -15, 15, -1, 15, Material.OBSIDIAN);
        b.fillBox(-14, -7, -14, 14, -1, 14, Material.AIR);
        b.fillFloor(-14, -14, 14, 14, -8, Material.CRYING_OBSIDIAN);
        // Vault ribbed ceiling
        for (int x = -14; x <= 14; x += 4) {
            b.fillBox(x, -1, -14, x, -1, 14, Material.POLISHED_BLACKSTONE);
        }
        for (int z = -14; z <= 14; z += 4) {
            b.fillBox(-14, -1, z, 14, -1, z, Material.POLISHED_BLACKSTONE);
        }

        // Four corner columns with end rod finials
        for (int[] c : new int[][]{{-10, -10}, {10, -10}, {-10, 10}, {10, 10}}) {
            b.pillar(c[0], -7, -1, c[1], Material.PURPUR_PILLAR);
            b.setBlock(c[0], -1, c[1], Material.END_ROD);
            b.setBlock(c[0], -2, c[1], Material.GILDED_BLACKSTONE);
        }

        // Central beacon pedestal
        b.fillBox(-2, -7, -2, 2, -7, 2, Material.IRON_BLOCK);
        b.setBlock(0, -6, 0, Material.BEACON);
        // Obsidian rim around beacon
        for (int[] r : new int[][]{{-3, -3}, {3, 3}, {-3, 3}, {3, -3}}) {
            b.setBlock(r[0], -7, r[1], Material.CRYING_OBSIDIAN);
        }

        // Vault side vaults with chests (infinity trophies)
        for (int[] off : new int[][]{{-12, 0}, {12, 0}, {0, -12}, {0, 12}}) {
            b.setBlock(off[0], -6, off[1], Material.CHISELED_POLISHED_BLACKSTONE);
            b.setBlock(off[0], -5, off[1], Material.GILDED_BLACKSTONE);
            b.setBlock(off[0], -4, off[1], Material.CHISELED_POLISHED_BLACKSTONE);
            b.setBlock(off[0], -7, off[1] + (off[1] == 0 ? 1 : 0), Material.SOUL_LANTERN);
        }

        // Spiral staircase from vault to throne room
        b.spiralStaircase(-10, -7, 20, 0, 2, Material.BLACKSTONE_STAIRS, Material.POLISHED_BLACKSTONE);

        // ── Cosmetic decay + purple-stained scatter ─────────────────────
        b.decay(-22, 8, -22, 22, 20, 22, 0.10);
        b.scatter(-22, 0, -22, 22, 20, 22, Material.PURPUR_BLOCK, Material.CRACKED_STONE_BRICKS, 0.06);
        b.scatter(-22, 0, -22, 22, 8, 22, Material.OBSIDIAN, Material.CRYING_OBSIDIAN, 0.05);
        b.addVines(-22, 5, -22, 22, 20, 22, 0.08);

        // Loot
        b.placeChest(0, 21, 0);    // throne footstool
        b.placeChest(-12, -7, 0);  // west vault
        b.placeChest(12, -7, 0);   // east vault
        b.placeChest(0, -7, 12);   // north vault
        b.placeChest(0, -7, -12);  // south vault

        b.setBossSpawn(0, 21, -2);
    }
}

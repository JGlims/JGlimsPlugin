package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;

/**
 * ObservationTowerBuilder — tall steel-and-glass research tower with 6 decorated floors,
 * spiral staircase, radar dish, telescope array, research labs, and beacon crown.
 */
public final class ObservationTowerBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // ── Base foundation platform ────────────────────────────────────
        b.fillBox(-9, 0, -9, 9, 0, 9, Material.POLISHED_ANDESITE);
        b.fillWalls(-9, 0, -9, 9, 1, 9, Material.DEEPSLATE_BRICKS);
        // Inlaid pattern
        for (int x = -8; x <= 8; x += 4)
            for (int z = -8; z <= 8; z += 4)
                b.setBlock(x, 0, z, Material.POLISHED_DEEPSLATE);
        // Stair approach on north
        for (int s = 0; s < 3; s++) {
            b.fillBox(-3, -s, -9 - s, 3, -s, -9 - s, Material.POLISHED_ANDESITE_SLAB);
        }

        // ── Tower shaft (layered materials, 36 levels) ──────────────────
        for (int y = 1; y <= 36; y++) {
            // Primary frame
            b.fillBox(-3, y, -3, 3, y, -3, Material.IRON_BLOCK);
            b.fillBox(-3, y, 3, 3, y, 3, Material.IRON_BLOCK);
            b.fillBox(-3, y, -3, -3, y, 3, Material.IRON_BLOCK);
            b.fillBox(3, y, -3, 3, y, 3, Material.IRON_BLOCK);
            // Interior clear
            b.fillBox(-2, y, -2, 2, y, 2, Material.AIR);
            // Every 4th layer: deepslate brick band for architectural rhythm
            if (y % 4 == 0) {
                b.fillWalls(-3, y, -3, 3, y, 3, Material.DEEPSLATE_BRICKS);
            }
            // Every 8th layer: gold inlay stripe
            if (y % 8 == 0) {
                b.fillBox(-3, y, -3, -3, y, 3, Material.GOLD_BLOCK);
                b.fillBox(3, y, -3, 3, y, 3, Material.GOLD_BLOCK);
            }
        }

        // ── Reinforced corner columns (chains cosmetic "wiring") ────────
        for (int[] c : new int[][]{{-3, -3}, {3, -3}, {-3, 3}, {3, 3}}) {
            b.pillar(c[0], 1, 36, c[1], Material.CHISELED_POLISHED_BLACKSTONE);
        }

        // ── Windows (double-height per floor, sea lanterns as "search lights") ──
        for (int y = 4; y <= 36; y += 4) {
            b.setBlock(0, y, -3, Material.TINTED_GLASS);
            b.setBlock(0, y, 3, Material.TINTED_GLASS);
            b.setBlock(-3, y, 0, Material.TINTED_GLASS);
            b.setBlock(3, y, 0, Material.TINTED_GLASS);
            b.setBlock(0, y + 1, -3, Material.TINTED_GLASS);
            b.setBlock(0, y + 1, 3, Material.TINTED_GLASS);
            b.setBlock(-3, y + 1, 0, Material.TINTED_GLASS);
            b.setBlock(3, y + 1, 0, Material.TINTED_GLASS);
        }

        // ── Observation floors (every 8 levels) with differentiated themes ──
        // Floor 1 (y=8): Archives
        b.fillBox(-2, 8, -2, 2, 8, 2, Material.POLISHED_DEEPSLATE);
        b.setBlock(-2, 9, -2, Material.BOOKSHELF);
        b.setBlock(-2, 9, 0, Material.BOOKSHELF);
        b.setBlock(-2, 9, 2, Material.BOOKSHELF);
        b.setBlock(2, 9, -2, Material.BOOKSHELF);
        b.setBlock(2, 9, 2, Material.LECTERN);
        b.setBlock(0, 10, 0, Material.LANTERN);

        // Floor 2 (y=16): Laboratory
        b.fillBox(-2, 16, -2, 2, 16, 2, Material.POLISHED_ANDESITE);
        b.setBlock(-2, 17, -2, Material.CRAFTING_TABLE);
        b.setBlock(0, 17, -2, Material.BREWING_STAND);
        b.setBlock(2, 17, -2, Material.CAULDRON);
        b.setBlock(-2, 17, 2, Material.SMOKER);
        b.setBlock(2, 17, 2, Material.BLAST_FURNACE);
        b.setBlock(0, 18, 0, Material.REDSTONE_LAMP);

        // Floor 3 (y=24): Command deck
        b.fillBox(-2, 24, -2, 2, 24, 2, Material.IRON_BLOCK);
        b.setBlock(-2, 25, -2, Material.REPEATER);
        b.setBlock(2, 25, 2, Material.COMPARATOR);
        b.setBlock(0, 25, 0, Material.LODESTONE);
        b.setBlock(0, 26, 0, Material.SEA_LANTERN);

        // Floor 4 (y=32): Signal room
        b.fillBox(-2, 32, -2, 2, 32, 2, Material.SMOOTH_STONE);
        b.setBlock(0, 33, -2, Material.JUKEBOX);
        b.setBlock(-2, 33, 0, Material.NOTE_BLOCK);
        b.setBlock(2, 33, 0, Material.NOTE_BLOCK);
        b.setBlock(0, 33, 2, Material.NOTE_BLOCK);
        b.setBlock(0, 34, 0, Material.END_ROD);

        // ── Ladder access along east wall ───────────────────────────────
        for (int y = 1; y <= 36; y++) b.setBlock(2, y, -2, Material.LADDER);

        // ── Top observation deck (larger, 11×11 footprint) ──────────────
        b.fillBox(-5, 36, -5, 5, 36, 5, Material.IRON_BLOCK);
        b.fillWalls(-5, 37, -5, 5, 40, 5, Material.IRON_BLOCK);
        b.hollowBox(-5, 37, -5, 5, 40, 5);
        b.fillBox(-4, 37, -4, 4, 39, 4, Material.AIR);

        // Glass observation windows (all four sides, double height)
        for (int x = -3; x <= 3; x++) {
            b.setBlock(x, 38, -5, Material.GLASS_PANE);
            b.setBlock(x, 38, 5, Material.GLASS_PANE);
            b.setBlock(x, 39, -5, Material.GLASS_PANE);
            b.setBlock(x, 39, 5, Material.GLASS_PANE);
        }
        for (int z = -3; z <= 3; z++) {
            b.setBlock(-5, 38, z, Material.GLASS_PANE);
            b.setBlock(5, 38, z, Material.GLASS_PANE);
            b.setBlock(-5, 39, z, Material.GLASS_PANE);
            b.setBlock(5, 39, z, Material.GLASS_PANE);
        }

        // Observation deck interior: control panels
        b.setBlock(-4, 37, -4, Material.COMMAND_BLOCK);
        b.setBlock(4, 37, -4, Material.COMMAND_BLOCK);
        b.setBlock(-4, 37, 4, Material.LODESTONE);
        b.setBlock(4, 37, 4, Material.LODESTONE);
        b.setBlock(0, 37, 0, Material.ENCHANTING_TABLE);
        // Ceiling lighting
        b.setBlock(-3, 40, -3, Material.SEA_LANTERN);
        b.setBlock(3, 40, -3, Material.SEA_LANTERN);
        b.setBlock(-3, 40, 3, Material.SEA_LANTERN);
        b.setBlock(3, 40, 3, Material.SEA_LANTERN);
        b.setBlock(0, 40, 0, Material.GLOWSTONE);

        // ── Telescope array on observation deck ─────────────────────────
        b.setBlock(0, 38, -3, Material.END_ROD);
        b.setBlock(0, 38, -4, Material.END_ROD);
        b.setBlock(0, 37, -3, Material.IRON_BLOCK);
        // Telescope eyepiece
        b.setBlock(0, 38, -2, Material.DAYLIGHT_DETECTOR);

        // ── Antenna mast ────────────────────────────────────────────────
        b.pillar(0, 40, 50, 0, Material.IRON_BARS);
        b.setBlock(0, 51, 0, Material.END_ROD);
        b.setBlock(0, 52, 0, Material.BEACON);
        // Mast cross-struts
        for (int h : new int[]{44, 47}) {
            b.setBlock(-2, h, 0, Material.IRON_BARS);
            b.setBlock(2, h, 0, Material.IRON_BARS);
            b.setBlock(0, h, -2, Material.IRON_BARS);
            b.setBlock(0, h, 2, Material.IRON_BARS);
        }

        // ── Radar dish on deck roof ─────────────────────────────────────
        b.setBlock(0, 41, 2, Material.DAYLIGHT_DETECTOR);
        b.setSlab(0, 41, 1, Material.IRON_BLOCK, false);
        b.setBlock(-1, 41, 2, Material.IRON_TRAPDOOR);
        b.setBlock(1, 41, 2, Material.IRON_TRAPDOOR);
        b.setBlock(0, 41, 3, Material.IRON_TRAPDOOR);
        b.setBlock(0, 42, 2, Material.END_ROD);

        // ── Entrance doors (double, east/north) ─────────────────────────
        b.setBlock(0, 1, -3, Material.AIR);
        b.setBlock(0, 2, -3, Material.AIR);
        b.setBlock(-1, 3, -3, Material.IRON_BLOCK);
        b.setBlock(0, 3, -3, Material.IRON_BLOCK);
        b.setBlock(1, 3, -3, Material.IRON_BLOCK);
        // Exterior entrance canopy
        b.setBlock(-1, 4, -4, Material.IRON_BARS);
        b.setBlock(0, 4, -4, Material.IRON_BARS);
        b.setBlock(1, 4, -4, Material.IRON_BARS);

        // ── Exterior ground-level details ───────────────────────────────
        // Flagpole
        b.pillar(-7, 1, 5, -7, Material.IRON_BARS);
        b.setBlock(-7, 6, -7, Material.WHITE_WOOL);
        b.setBlock(-6, 6, -7, Material.LIGHT_BLUE_WOOL);
        b.setBlock(-7, 5, -6, Material.LIGHT_BLUE_WOOL);
        // Satellite dish on ground (decorative)
        b.setBlock(7, 1, -7, Material.IRON_TRAPDOOR);
        b.setBlock(7, 2, -7, Material.IRON_TRAPDOOR);
        b.setBlock(7, 2, -6, Material.IRON_TRAPDOOR);
        b.setBlock(7, 1, -6, Material.DAYLIGHT_DETECTOR);
        // Lamp posts on corners
        for (int[] c : new int[][]{{-9, -9}, {9, -9}, {-9, 9}, {9, 9}}) {
            b.pillar(c[0], 1, 4, c[1], Material.IRON_BARS);
            b.setBlock(c[0], 5, c[1], Material.LANTERN);
        }

        // ── Weathering ──────────────────────────────────────────────────
        b.scatter(-5, 1, -5, 5, 36, 5, Material.IRON_BLOCK, Material.CRACKED_DEEPSLATE_BRICKS, 0.04);

        // ── Loot ────────────────────────────────────────────────────────
        b.placeChest(0, 37, 2);    // observation deck
        b.placeChest(-1, 9, -1);   // archives
        b.placeChest(-1, 17, -1);  // lab
        b.placeChest(-1, 25, -1);  // command
        b.placeChest(-1, 33, -1);  // signal
        b.setBossSpawn(0, 37, 0);
    }
}

package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;

/**
 * SunkenRuinsBuilder — sprawling prismarine temple with 4 underwater chambers, connecting
 * tunnels, coral gardens, sunken ship wreck, treasure hoard, central altar, drowned skeleton
 * circle, trident racks, column grove, moss-veined floor, and kelp forest perimeter.
 */
public final class SunkenRuinsBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // ── Base platform ───────────────────────────────────────────────
        b.filledCircle(0, 0, 0, 16, Material.PRISMARINE_BRICKS);
        b.scatter(-16, 0, -16, 16, 0, 16, Material.PRISMARINE_BRICKS, Material.DARK_PRISMARINE, 0.2);
        b.scatter(-16, 0, -16, 16, 0, 16, Material.PRISMARINE_BRICKS, Material.MOSSY_COBBLESTONE, 0.1);

        // ── Rising walls (tiered, partially decayed) ────────────────────
        b.filledCircle(0, 1, 0, 14, Material.PRISMARINE);
        b.circle(0, 2, 0, 14, Material.PRISMARINE);
        b.circle(0, 3, 0, 14, Material.PRISMARINE);
        b.circle(0, 4, 0, 12, Material.PRISMARINE_BRICKS);
        b.circle(0, 5, 0, 12, Material.PRISMARINE_BRICKS);
        b.circle(0, 6, 0, 10, Material.DARK_PRISMARINE);

        // ── Broken column grove ─────────────────────────────────────────
        for (int a = 0; a < 360; a += 30) {
            int px = (int) (11 * Math.cos(Math.toRadians(a)));
            int pz = (int) (11 * Math.sin(Math.toRadians(a)));
            int h = 4 + b.getRandom().nextInt(5);
            b.pillar(px, 1, h, pz, Material.PRISMARINE_WALL);
            b.setBlock(px, h + 1, pz, Material.SEA_LANTERN);
            // Base
            for (int[] off : new int[][]{{0, 1}, {0, -1}, {1, 0}, {-1, 0}}) {
                b.setBlockIfAir(px + off[0], 1, pz + off[1], Material.PRISMARINE);
            }
        }

        // ── Central altar ───────────────────────────────────────────────
        b.fillBox(-3, 1, -3, 3, 1, 3, Material.DARK_PRISMARINE);
        b.fillBox(-2, 2, -2, 2, 2, 2, Material.PRISMARINE_BRICKS);
        b.setBlock(0, 3, 0, Material.SEA_LANTERN);
        b.setBlock(-1, 3, 0, Material.PRISMARINE_SLAB);
        b.setBlock(1, 3, 0, Material.PRISMARINE_SLAB);
        b.setBlock(0, 3, -1, Material.PRISMARINE_SLAB);
        b.setBlock(0, 3, 1, Material.PRISMARINE_SLAB);
        // Corner sea lanterns
        b.setBlock(-3, 1, -3, Material.SEA_LANTERN);
        b.setBlock(3, 1, -3, Material.SEA_LANTERN);
        b.setBlock(-3, 1, 3, Material.SEA_LANTERN);
        b.setBlock(3, 1, 3, Material.SEA_LANTERN);

        // ── Coral garden around perimeter ───────────────────────────────
        Material[] corals = {Material.BRAIN_CORAL_BLOCK, Material.TUBE_CORAL_BLOCK,
                Material.FIRE_CORAL_BLOCK, Material.HORN_CORAL_BLOCK, Material.BUBBLE_CORAL_BLOCK};
        for (int a = 0; a < 360; a += 15) {
            int cx = (int) (14 * Math.cos(Math.toRadians(a)));
            int cz = (int) (14 * Math.sin(Math.toRadians(a)));
            b.setBlockIfAir(cx, 1, cz, corals[a / 15 % corals.length]);
            if (a % 30 == 0) {
                b.setBlockIfAir(cx, 2, cz, corals[a / 30 % corals.length]);
            }
        }

        // ── 4 air-pocket underwater chambers ────────────────────────────
        // NW chamber
        b.fillBox(-8, -4, -8, -4, 0, -4, Material.PRISMARINE_BRICKS);
        b.fillBox(-7, -3, -7, -5, -1, -5, Material.AIR);
        b.fillFloor(-7, -7, -5, -5, -4, Material.DARK_PRISMARINE);
        b.setBlock(-6, -3, -6, Material.SEA_LANTERN);
        // SE chamber
        b.fillBox(4, -4, 4, 8, 0, 8, Material.PRISMARINE_BRICKS);
        b.fillBox(5, -3, 5, 7, -1, 7, Material.AIR);
        b.fillFloor(5, 5, 7, 7, -4, Material.DARK_PRISMARINE);
        b.setBlock(6, -3, 6, Material.SEA_LANTERN);
        // NE chamber
        b.fillBox(4, -4, -8, 8, 0, -4, Material.PRISMARINE_BRICKS);
        b.fillBox(5, -3, -7, 7, -1, -5, Material.AIR);
        b.fillFloor(5, -7, 7, -5, -4, Material.DARK_PRISMARINE);
        b.setBlock(6, -3, -6, Material.SEA_LANTERN);
        // SW chamber
        b.fillBox(-8, -4, 4, -4, 0, 8, Material.PRISMARINE_BRICKS);
        b.fillBox(-7, -3, 5, -5, -1, 7, Material.AIR);
        b.fillFloor(-7, 5, -5, 7, -4, Material.DARK_PRISMARINE);
        b.setBlock(-6, -3, 6, Material.SEA_LANTERN);

        // ── Treasure hoard in each chamber ──────────────────────────────
        b.setBlock(-6, -3, -4, Material.GOLD_BLOCK);
        b.setBlock(-4, -3, -6, Material.GOLD_BLOCK);
        b.setBlock(6, -3, 4, Material.DIAMOND_BLOCK);
        b.setBlock(4, -3, 6, Material.DIAMOND_BLOCK);
        b.setBlock(6, -3, -4, Material.EMERALD_BLOCK);
        b.setBlock(4, -3, -6, Material.EMERALD_BLOCK);
        b.setBlock(-6, -3, 4, Material.LAPIS_BLOCK);
        b.setBlock(-4, -3, 6, Material.LAPIS_BLOCK);

        // ── Connecting tunnel (east chamber ↔ altar) ────────────────────
        for (int x = 4; x >= -1; x--) {
            b.setBlock(x, -2, 0, Material.AIR);
            b.setBlock(x, -3, 0, Material.PRISMARINE_BRICKS);
            b.setBlock(x, -1, 0, Material.PRISMARINE_BRICKS);
        }
        b.setBlock(-1, -2, 0, Material.SEA_LANTERN);

        // ── Drowned skeleton circle around the altar ────────────────────
        for (int i = 0; i < 12; i++) {
            int a = i * 30;
            int sx = (int) Math.round(7 * Math.cos(Math.toRadians(a)));
            int sz = (int) Math.round(7 * Math.sin(Math.toRadians(a)));
            b.setBlock(sx, 1, sz, Material.BONE_BLOCK);
            if (i % 2 == 0) b.setBlock(sx, 2, sz, Material.SKELETON_SKULL);
        }

        // ── Trident racks on interior walls ─────────────────────────────
        for (int[] rack : new int[][]{{-9, 0}, {9, 0}, {0, -9}, {0, 9}}) {
            b.pillar(rack[0], 1, 4, rack[1], Material.PRISMARINE_BRICKS);
            b.setBlock(rack[0], 5, rack[1], Material.SEA_LANTERN);
            b.setBlock(rack[0], 2, rack[1], Material.CONDUIT);
        }

        // ── Sunken ship wreckage (SW, angled) ───────────────────────────
        b.fillBox(-14, 0, -14, -9, 4, -9, Material.DARK_OAK_PLANKS);
        b.fillBox(-13, 1, -13, -10, 3, -10, Material.AIR);
        b.decay(-14, 0, -14, -9, 4, -9, 0.40);
        b.fillFloor(-13, -13, -10, -10, 0, Material.OAK_PLANKS);
        // Mast
        b.pillar(-11, 4, 10, -11, Material.DARK_OAK_LOG);
        for (int[] sail : new int[][]{{-12, -11}, {-10, -11}, {-11, -11}}) {
            b.setBlock(sail[0], 9, sail[1], Material.WHITE_WOOL);
            b.setBlock(sail[0], 8, sail[1], Material.WHITE_WOOL);
        }
        b.decay(-13, 5, -13, -9, 11, -9, 0.5);
        // Helm
        b.setBlock(-11, 2, -12, Material.SPRUCE_TRAPDOOR);
        // Captain's bones
        b.setBlock(-11, 1, -11, Material.SKELETON_SKULL);
        b.setBlock(-11, 1, -10, Material.BONE_BLOCK);

        // ── Decay and weathering ────────────────────────────────────────
        b.scatter(-16, 0, -16, 16, 6, 16, Material.PRISMARINE_BRICKS, Material.PRISMARINE, 0.15);
        b.decay(-16, 3, -16, 16, 8, 16, 0.25);

        // ── Kelp and seagrass forests ───────────────────────────────────
        b.scatter(-16, 1, -16, 16, 3, 16, Material.AIR, Material.SEAGRASS, 0.08);
        b.scatter(-16, 1, -16, 16, 4, 16, Material.AIR, Material.KELP_PLANT, 0.10);

        // ── Pufferfish-lantern ambient on seafloor ──────────────────────
        for (int[] pl : new int[][]{{-12, 6}, {12, -6}, {-6, 12}, {6, -12}}) {
            b.setBlock(pl[0], 0, pl[1], Material.SEA_LANTERN);
        }

        // ── Loot ────────────────────────────────────────────────────────
        b.placeChest(0, 1, 5);       // altar
        b.placeChest(-6, -2, -6);    // NW chamber
        b.placeChest(6, -2, 6);      // SE chamber
        b.placeChest(6, -2, -6);     // NE chamber
        b.placeChest(-11, 1, -11);   // shipwreck
        b.setBossSpawn(0, 1, 0);
    }
}

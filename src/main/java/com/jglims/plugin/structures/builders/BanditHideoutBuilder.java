package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

/**
 * BanditHideoutBuilder — sandstone canyon hideout with multi-level platforms, ziplines,
 * weapon cache, campfire camp, watchtower, stolen-loot piles, and escape tunnel.
 */
public final class BanditHideoutBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // ── Canyon walls (layered sandstone + smooth + cut) ─────────────
        b.fillWalls(-12, 0, -10, 12, 8, 10, Material.SANDSTONE);
        b.hollowBox(-12, 1, -10, 12, 8, 10);
        b.fillFloor(-11, -9, 11, 9, 0, Material.SAND);
        // Interior stripe of smooth sandstone at mid-height
        b.fillWalls(-12, 4, -10, 12, 4, 10, Material.SMOOTH_SANDSTONE);
        // Top crenellation
        b.fillWalls(-12, 8, -10, 12, 8, 10, Material.CUT_SANDSTONE);
        // Corner guard posts
        for (int[] c : new int[][]{{-12, -10}, {12, -10}, {-12, 10}, {12, 10}}) {
            b.pillar(c[0], 0, 10, c[1], Material.CUT_SANDSTONE);
            b.setBlock(c[0], 10, c[1], Material.CHISELED_SANDSTONE);
        }

        // Weathering & desert patina
        b.scatter(-12, 0, -10, 12, 8, 10, Material.SANDSTONE, Material.CHISELED_SANDSTONE, 0.08);
        b.scatter(-11, 0, -9, 11, 0, 9, Material.SAND, Material.RED_SAND, 0.15);
        b.scatter(-11, 0, -9, 11, 0, 9, Material.SAND, Material.COARSE_DIRT, 0.05);

        // ── Cave entrance (north) with archway ──────────────────────────
        b.fillBox(-3, 1, -12, 3, 5, -10, Material.AIR);
        b.archDoorway(0, 1, -10, 7, 5, Material.CHISELED_SANDSTONE);
        // Arch banner
        b.setBlock(0, 6, -10, Material.RED_WOOL);
        b.setBlock(-1, 6, -10, Material.BLACK_WOOL);
        b.setBlock(1, 6, -10, Material.BLACK_WOOL);

        // ── Multi-level wooden platforms ────────────────────────────────
        // Platform 1 — West low
        b.fillBox(-11, 3, -6, -7, 3, -2, Material.OAK_PLANKS);
        b.fillWalls(-11, 4, -6, -7, 4, -2, Material.OAK_FENCE);
        b.setBlock(-8, 4, -4, Material.LANTERN);
        b.setBlock(-10, 4, -3, Material.BARREL);
        b.setBlock(-9, 4, -5, Material.CRAFTING_TABLE);

        // Platform 2 — East mid
        b.fillBox(7, 5, 1, 11, 5, 5, Material.OAK_PLANKS);
        b.fillWalls(7, 6, 1, 11, 6, 5, Material.OAK_FENCE);
        b.setBlock(9, 6, 3, Material.LANTERN);
        b.setBlock(10, 6, 2, Material.BARREL);
        b.setBlock(8, 6, 4, Material.CHEST);  // secondary stash (not a loot chest, decor)

        // Platform 3 — West high (sniper nest)
        b.fillBox(-11, 6, 3, -8, 6, 7, Material.SPRUCE_PLANKS);
        b.fillWalls(-11, 7, 3, -8, 7, 7, Material.OAK_FENCE);
        b.setBlock(-10, 7, 5, Material.LANTERN);
        b.setBlock(-9, 7, 6, Material.OBSERVER);  // "scope"

        // ── Ladders between platforms ───────────────────────────────────
        for (int y = 1; y <= 3; y++) b.setBlock(-10, y, -2, Material.LADDER);
        for (int y = 4; y <= 5; y++) b.setBlock(8, y, 1, Material.LADDER);
        for (int y = 4; y <= 6; y++) b.setBlock(-9, y, 3, Material.LADDER);

        // ── Rope bridge across canyon ───────────────────────────────────
        for (int x = -7; x <= 7; x++) {
            b.setBlock(x, 5, 2, Material.OAK_PLANKS);
            b.setBlock(x, 6, 3, Material.OAK_FENCE);
            b.setBlock(x, 6, 1, Material.OAK_FENCE);
        }
        // Support lines (tripwire = rope visual)
        for (int x = -7; x <= 7; x += 2) {
            b.setBlock(x, 7, 3, Material.TRIPWIRE);
            b.setBlock(x, 7, 1, Material.TRIPWIRE);
        }

        // ── Stolen loot piles ───────────────────────────────────────────
        b.fillBox(-8, 1, -7, -6, 2, -5, Material.HAY_BLOCK);
        b.setBlock(-7, 3, -6, Material.GOLD_BLOCK);
        b.setBlock(-6, 3, -5, Material.EMERALD_BLOCK);
        b.fillBox(6, 1, 5, 8, 1, 7, Material.BARREL);
        b.setBlock(7, 2, 6, Material.GOLD_BLOCK);
        b.setBlock(6, 2, 5, Material.BARREL);
        // Scattered ingots / coins (gold blocks)
        b.scatter(-8, 1, -7, -6, 2, -5, Material.HAY_BLOCK, Material.GOLD_BLOCK, 0.10);

        // ── Central camp ────────────────────────────────────────────────
        b.setBlock(0, 1, 0, Material.CAMPFIRE);
        for (int[] log : new int[][]{{-1, 1}, {1, -1}, {1, 1}, {-1, -1}}) {
            b.setBlock(log[0], 1, log[1], Material.STRIPPED_OAK_LOG);
        }
        // Circular seating
        b.circle(0, 1, 0, 3, Material.SMOOTH_SANDSTONE_SLAB);
        b.setBlock(-2, 1, 2, Material.BARREL);

        // Cauldron-cookpot + tripod
        b.setBlock(3, 1, -2, Material.CAULDRON);
        b.setBlock(3, 2, -2, Material.TRIPWIRE_HOOK);

        // ── Watchtower on east wall ─────────────────────────────────────
        b.fillBox(10, 8, -2, 12, 8, 2, Material.OAK_PLANKS);
        b.fillBox(10, 9, -2, 12, 9, 2, Material.AIR);
        b.fillWalls(10, 9, -2, 12, 9, 2, Material.OAK_FENCE);
        b.pyramidRoof(10, -2, 12, 2, 10, Material.DARK_OAK_STAIRS);
        b.setBlock(11, 9, 0, Material.LANTERN);
        b.setBlock(11, 9, -1, Material.BARREL);
        for (int y = 1; y <= 8; y++) b.setBlock(11, y, -9, Material.LADDER);

        // ── Escape tunnel (south) ───────────────────────────────────────
        b.fillBox(-1, 1, 10, 1, 3, 18, Material.AIR);
        b.fillWalls(-2, 0, 10, 2, 3, 18, Material.CUT_SANDSTONE);
        b.fillFloor(-1, 10, 1, 18, 0, Material.COARSE_DIRT);
        // Tunnel supports
        for (int z = 11; z <= 17; z += 2) {
            b.setBlock(-2, 3, z, Material.OAK_LOG);
            b.setBlock(2, 3, z, Material.OAK_LOG);
            b.setBlock(-1, 4, z, Material.OAK_LOG);
            b.setBlock(0, 4, z, Material.OAK_LOG);
            b.setBlock(1, 4, z, Material.OAK_LOG);
        }
        // Tunnel-end cave room
        b.fillBox(-3, 1, 17, 3, 4, 21, Material.AIR);
        b.fillWalls(-4, 0, 16, 4, 4, 22, Material.CUT_SANDSTONE);
        b.fillFloor(-3, 17, 3, 21, 0, Material.COARSE_DIRT);
        b.setBlock(0, 1, 20, Material.CAMPFIRE);
        b.setBlock(-2, 1, 20, Material.BARREL);
        b.setBlock(2, 1, 20, Material.BARREL);
        b.setBlock(0, 3, 20, Material.LANTERN);

        // ── Weapon rack & smithy corner ─────────────────────────────────
        b.fillBox(-6, 1, 0, -5, 1, 2, Material.OAK_PLANKS);
        b.setBlock(-5, 1, 0, Material.GRINDSTONE);
        b.setBlock(-5, 1, 1, Material.SMITHING_TABLE);
        b.setBlock(-5, 1, 2, Material.ANVIL);
        b.setBlock(-6, 1, 0, Material.FURNACE);
        b.setBlock(-6, 1, 1, Material.BLAST_FURNACE);
        // Rack of weapons (armor stands represented as fence+trapdoor)
        for (int z = 0; z <= 2; z++) {
            b.setBlock(-4, 1, z, Material.OAK_FENCE);
            b.setBlock(-4, 2, z, Material.OAK_TRAPDOOR);
        }

        // ── Campfire pit lighting ───────────────────────────────────────
        b.setBlock(-6, 3, 0, Material.LANTERN);
        b.setBlock(6, 3, 0, Material.LANTERN);
        b.setBlock(0, 7, 6, Material.LANTERN);
        b.setBlock(0, 7, -6, Material.LANTERN);

        // ── Floor details: tracks and bloodstains ───────────────────────
        for (int[] p : new int[][]{{-3, 2}, {3, -2}, {-5, -7}, {5, 7}, {-8, 4}, {8, -4}}) {
            b.setBlock(p[0], 0, p[1], Material.RED_TERRACOTTA);
        }
        // Graffiti (banner-blocks)
        b.setBlock(-12, 4, 0, Material.BLACK_WOOL);
        b.setBlock(12, 4, 0, Material.RED_WOOL);

        // ── Animal pens / horse hitch ───────────────────────────────────
        b.fillBox(4, 1, -7, 8, 1, -5, Material.COARSE_DIRT);
        b.setBlock(4, 2, -7, Material.OAK_FENCE);
        b.setBlock(4, 2, -5, Material.OAK_FENCE);
        b.setBlock(8, 2, -7, Material.OAK_FENCE);
        b.setBlock(8, 2, -5, Material.OAK_FENCE);
        b.setBlock(6, 2, -7, Material.OAK_FENCE);
        b.setBlock(5, 1, -6, Material.HAY_BLOCK);
        b.setBlock(7, 1, -6, Material.WATER_CAULDRON);

        // ── Loot ────────────────────────────────────────────────────────
        b.placeChest(-7, 1, -5);  // campfire stash
        b.placeChest(7, 1, 6);    // east platform
        b.placeChest(0, 1, 20);   // escape tunnel end
        b.placeChest(-9, 7, 6);   // sniper nest
        b.placeChest(11, 9, -1);  // watchtower
        b.setBossSpawn(0, 1, 3);
    }
}

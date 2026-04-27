package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;

/**
 * WitchHouseSwampBuilder — mangrove stilted hut with brewing loft, cauldron room,
 * rune circle, drying herbs, hanging fishing dock, bat roost, potion shelves,
 * and swamp garden with glowing mushrooms and lily paths.
 */
public final class WitchHouseSwampBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // ── Stilts (mangrove roots into water) ──────────────────────────
        for (int[] p : new int[][]{{-5, -5}, {5, -5}, {-5, 5}, {5, 5}, {0, -5}, {0, 5}, {-5, 0}, {5, 0}}) {
            b.pillar(p[0], -4, 0, p[1], Material.MANGROVE_LOG);
        }
        // Exposed roots spreading outward
        for (int[] r : new int[][]{{-6, -5}, {-5, -6}, {6, -5}, {5, -6}, {-6, 5}, {-5, 6}, {6, 5}, {5, 6}}) {
            b.setBlock(r[0], -4, r[1], Material.MANGROVE_ROOTS);
            b.setBlock(r[0], -3, r[1], Material.MANGROVE_ROOTS);
            b.setBlock(r[0], -2, r[1], Material.MUD);
        }

        // ── Lily pad path leading in from the south ─────────────────────
        for (int z = -12; z <= -6; z++) {
            b.setBlock(0, -3, z, Material.LILY_PAD);
            if (z % 2 == 0) b.setBlock(1, -3, z, Material.LILY_PAD);
            if (z % 3 == 0) b.setBlock(-1, -3, z, Material.LILY_PAD);
        }
        // Path-side swamp reeds
        for (int z : new int[]{-11, -8}) {
            b.setBlock(-2, -3, z, Material.SEAGRASS);
            b.setBlock(2, -3, z, Material.SEAGRASS);
        }

        // ── Main floor + walls ──────────────────────────────────────────
        b.fillBox(-5, 0, -5, 5, 0, 5, Material.MANGROVE_PLANKS);
        b.fillWalls(-5, 1, -5, 5, 5, 5, Material.MANGROVE_PLANKS);
        b.hollowBox(-5, 1, -5, 5, 5, 5);
        // Mangrove log corner posts
        for (int[] c : new int[][]{{-5, -5}, {5, -5}, {-5, 5}, {5, 5}}) {
            b.pillar(c[0], 1, 6, c[1], Material.MANGROVE_LOG);
        }
        // Wall weathering
        b.scatter(-5, 1, -5, 5, 5, 5, Material.MANGROVE_PLANKS, Material.STRIPPED_MANGROVE_LOG, 0.15);
        b.scatter(-5, 1, -5, 5, 5, 5, Material.MANGROVE_PLANKS, Material.MUSHROOM_STEM, 0.08);

        // ── Sloped mangrove roof ────────────────────────────────────────
        b.pyramidRoof(-6, -6, 6, 6, 6, Material.MANGROVE_PLANKS);
        b.setBlock(0, 10, 0, Material.MANGROVE_LOG);
        // Roof moss
        b.scatter(-6, 6, -6, 6, 9, 6, Material.MANGROVE_PLANKS, Material.MOSS_BLOCK, 0.2);

        // ── Windows ─────────────────────────────────────────────────────
        b.setBlock(-5, 3, 0, Material.PURPLE_STAINED_GLASS_PANE);
        b.setBlock(-5, 4, 0, Material.MAGENTA_STAINED_GLASS_PANE);
        b.setBlock(5, 3, 0, Material.PURPLE_STAINED_GLASS_PANE);
        b.setBlock(5, 4, 0, Material.MAGENTA_STAINED_GLASS_PANE);
        b.setBlock(0, 3, 5, Material.PURPLE_STAINED_GLASS_PANE);
        b.setBlock(0, 4, 5, Material.MAGENTA_STAINED_GLASS_PANE);
        // Door (opening)
        b.setBlock(0, 1, -5, Material.AIR);
        b.setBlock(0, 2, -5, Material.AIR);

        // ── Cauldron cooking station (center) ───────────────────────────
        b.setBlock(0, 1, 0, Material.CAULDRON);
        b.setBlock(0, 0, 0, Material.CAMPFIRE);
        // Tripod
        b.setBlock(-1, 2, 0, Material.TRIPWIRE_HOOK);
        b.setBlock(1, 2, 0, Material.TRIPWIRE_HOOK);
        // Floor rune circle
        for (int[] rune : new int[][]{{-2, -2}, {-2, 2}, {2, -2}, {2, 2}, {0, -2}, {0, 2}, {-2, 0}, {2, 0}}) {
            b.setBlock(rune[0], 1, rune[1], Material.MOSS_CARPET);
        }
        b.setBlock(-3, 1, -3, Material.PURPLE_CANDLE);
        b.setBlock(3, 1, -3, Material.PURPLE_CANDLE);
        b.setBlock(-3, 1, 3, Material.PURPLE_CANDLE);
        b.setBlock(3, 1, 3, Material.PURPLE_CANDLE);

        // ── Potion shelves (west wall, double-stacked) ──────────────────
        for (int y = 1; y <= 3; y++)
            for (int z = 2; z <= 4; z++)
                b.setBlock(-4, y, z, Material.BOOKSHELF);
        b.setBlock(-3, 1, 4, Material.BREWING_STAND);
        b.setBlock(-3, 2, 4, Material.ENCHANTING_TABLE);

        // ── Brewing corner (east wall) ──────────────────────────────────
        b.setBlock(3, 1, 3, Material.BREWING_STAND);
        b.setBlock(4, 1, 3, Material.BREWING_STAND);
        b.setBlock(3, 1, 4, Material.COMPOSTER);
        b.setBlock(4, 1, 4, Material.BARREL);
        b.setBlock(4, 2, 3, Material.OAK_SIGN);

        // ── Bed nook (NW corner) ────────────────────────────────────────
        b.setBlock(-4, 1, -4, Material.PURPLE_BED);
        b.setBlock(-4, 2, -3, Material.BOOKSHELF);
        b.setBlock(-3, 2, -4, Material.LANTERN);

        // ── Witch's workbench (SE corner) ───────────────────────────────
        b.setBlock(3, 1, -4, Material.CRAFTING_TABLE);
        b.setBlock(4, 1, -4, Material.SMITHING_TABLE);
        b.setBlock(3, 2, -4, Material.ANVIL);
        b.setBlock(4, 2, -4, Material.LANTERN);

        // ── Hanging herbs from ceiling ──────────────────────────────────
        for (int[] h : new int[][]{{-2, 0}, {2, 0}, {0, -2}, {0, 2}, {-2, -2}, {2, 2}}) {
            b.setBlock(h[0], 5, h[1], Material.IRON_CHAIN);
            b.setBlock(h[0], 4, h[1], Material.BROWN_MUSHROOM);
        }

        // ── Cobweb corners + dust ───────────────────────────────────────
        b.setBlock(-4, 4, -4, Material.COBWEB);
        b.setBlock(4, 4, 4, Material.COBWEB);
        b.setBlock(-4, 4, 4, Material.COBWEB);
        b.setBlock(4, 4, -4, Material.COBWEB);

        // ── Lantern lighting (warm + soul mix) ──────────────────────────
        b.setBlock(-3, 4, 0, Material.LANTERN);
        b.setBlock(3, 4, 0, Material.LANTERN);
        b.setBlock(0, 4, -3, Material.SOUL_LANTERN);
        b.setBlock(0, 4, 3, Material.SOUL_LANTERN);
        b.setBlock(0, 5, 0, Material.SOUL_LANTERN);

        // ── Potted mushrooms + azalea ───────────────────────────────────
        b.setBlock(4, 1, -3, Material.POTTED_RED_MUSHROOM);
        b.setBlock(-4, 1, -3, Material.POTTED_BROWN_MUSHROOM);
        b.setBlock(4, 1, 1, Material.POTTED_AZALEA_BUSH);
        b.setBlock(-4, 1, 1, Material.POTTED_FERN);

        // ── Raised brewing loft above main floor ────────────────────────
        b.fillBox(-3, 5, -3, 3, 5, 3, Material.MANGROVE_PLANKS);
        // Cut an open hatch
        b.setBlock(-1, 5, 1, Material.AIR);
        b.setBlock(0, 5, 1, Material.AIR);
        b.setBlock(1, 5, 1, Material.AIR);
        // Ladder up
        for (int y = 1; y <= 5; y++) b.setBlock(3, y, 1, Material.LADDER);
        // Loft contents (cauldrons, bookshelves, lectern)
        b.setBlock(-2, 6, -2, Material.BREWING_STAND);
        b.setBlock(2, 6, -2, Material.CAULDRON);
        b.setBlock(-2, 6, 2, Material.BOOKSHELF);
        b.setBlock(2, 6, 2, Material.LECTERN);
        b.setBlock(0, 6, -2, Material.GLOW_LICHEN);
        b.setBlock(0, 7, 0, Material.SOUL_LANTERN);

        // ── Exterior fishing dock ───────────────────────────────────────
        b.fillBox(-7, 0, -7, -5, 0, -5, Material.MANGROVE_PLANKS);
        b.pillar(-7, -4, -1, -7, Material.MANGROVE_LOG);
        b.pillar(-5, -4, -1, -7, Material.MANGROVE_LOG);
        b.setBlock(-7, 1, -6, Material.OAK_FENCE);
        b.setBlock(-7, 2, -6, Material.OAK_FENCE);
        b.setBlock(-6, 1, -6, Material.BARREL);

        // ── Swamp garden around the house ───────────────────────────────
        for (int[] g : new int[][]{{-8, -3}, {-8, 3}, {8, -3}, {8, 3}, {-7, 7}, {7, -7}}) {
            b.setBlock(g[0], -3, g[1], Material.MUD);
            b.setBlock(g[0], -2, g[1], Material.FERN);
        }
        // Glow-mushroom patches
        for (int[] gm : new int[][]{{-7, 2}, {7, -2}, {-3, -7}, {3, 7}}) {
            b.setBlock(gm[0], -3, gm[1], Material.MYCELIUM);
            b.setBlock(gm[0], -2, gm[1], Material.RED_MUSHROOM);
        }

        // Exterior vines
        b.addVines(-6, -2, -6, 6, 6, 6, 0.25);
        b.addVines(-6, 6, -6, 6, 9, 6, 0.15);

        // ── Loot ────────────────────────────────────────────────────────
        b.placeChest(-3, 1, -3);   // bed-side trove
        b.placeChest(4, 1, -3);    // potted-mushroom stash
        b.placeChest(-2, 6, -2);   // brewing loft
        b.placeChest(-6, 1, -6);   // fishing dock
        b.setBossSpawn(0, 1, 2);
    }
}

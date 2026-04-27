package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

/**
 * PillagerAirshipBuilder — sky galleon with dark-oak hull, gray wool balloon envelope,
 * multi-tier deck, cannon row, captain's cabin with desk/bed, below-deck powder magazine,
 * crow's-nest lookout, crossbow mounts, banners, bridge wheel, brig cell, signal brazier.
 */
public final class PillagerAirshipBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        int baseY = 15;

        // ── Hull (boat profile, tapered bow + stern) ────────────────────
        for (int z = -12; z <= 12; z++) {
            int halfWidth = z < -8 ? (z + 12) : (z > 8 ? (12 - z) : 4);
            if (halfWidth < 1) halfWidth = 1;
            // Bottom keel
            b.fillBox(-halfWidth, baseY, z, halfWidth, baseY, z, Material.DARK_OAK_PLANKS);
            // Hull sides
            b.setBlock(-halfWidth, baseY + 1, z, Material.DARK_OAK_PLANKS);
            b.setBlock(halfWidth, baseY + 1, z, Material.DARK_OAK_PLANKS);
            // Railing
            b.setBlock(-halfWidth, baseY + 2, z, Material.DARK_OAK_FENCE);
            b.setBlock(halfWidth, baseY + 2, z, Material.DARK_OAK_FENCE);
            // Keel ribs underneath
            if (z % 3 == 0) {
                b.setBlock(0, baseY - 1, z, Material.STRIPPED_DARK_OAK_LOG);
                if (halfWidth >= 2) {
                    b.setBlock(-1, baseY - 1, z, Material.STRIPPED_DARK_OAK_LOG);
                    b.setBlock(1, baseY - 1, z, Material.STRIPPED_DARK_OAK_LOG);
                }
            }
        }
        // Bow spike
        b.setBlock(0, baseY + 1, -12, Material.STRIPPED_DARK_OAK_LOG);
        b.setBlock(0, baseY + 2, -12, Material.LIGHTNING_ROD);

        // ── Deck interior ───────────────────────────────────────────────
        b.fillFloor(-3, -10, 3, 10, baseY, Material.OAK_PLANKS);
        // Deck planks pattern
        b.scatter(-3, baseY, -10, 3, baseY, 10, Material.OAK_PLANKS, Material.STRIPPED_OAK_LOG, 0.15);

        // ── Below-deck cargo / powder magazine ──────────────────────────
        b.fillBox(-3, baseY - 3, -6, 3, baseY - 1, 6, Material.DARK_OAK_PLANKS);
        b.fillBox(-2, baseY - 2, -5, 2, baseY - 1, 5, Material.AIR);
        // Powder kegs
        b.setBlock(0, baseY - 2, 0, Material.BARREL);
        b.setBlock(1, baseY - 2, 2, Material.BARREL);
        b.setBlock(-1, baseY - 2, -3, Material.BARREL);
        b.setBlock(0, baseY - 2, 4, Material.TNT);
        b.setBlock(-1, baseY - 2, 4, Material.TNT);
        b.setBlock(1, baseY - 2, 4, Material.BLACK_CONCRETE);
        // Ladder down
        for (int y = baseY - 2; y <= baseY; y++) b.setBlock(2, y, -4, Material.LADDER);
        // Brig cell (iron bars at stern end)
        b.setBlock(-2, baseY - 2, -4, Material.IRON_BARS);
        b.setBlock(-1, baseY - 2, -4, Material.IRON_BARS);
        b.setBlock(0, baseY - 2, -4, Material.IRON_BARS);
        b.setBlock(-2, baseY - 2, -5, Material.SKELETON_SKULL);
        b.setBlock(-2, baseY - 1, -5, Material.IRON_CHAIN);

        // ── Captain's cabin (stern) ─────────────────────────────────────
        b.fillBox(-3, baseY + 1, 6, 3, baseY + 4, 10, Material.DARK_OAK_PLANKS);
        b.fillBox(-2, baseY + 1, 7, 2, baseY + 3, 9, Material.AIR);
        b.setBlock(0, baseY + 1, 6, Material.AIR);
        b.setBlock(0, baseY + 2, 6, Material.AIR);
        // Desk
        b.setBlock(0, baseY + 1, 8, Material.CRAFTING_TABLE);
        b.setBlock(-1, baseY + 1, 8, Material.OAK_SIGN);
        b.setBlock(1, baseY + 1, 8, Material.LECTERN);
        b.chair(0, baseY + 1, 7, Material.DARK_OAK_STAIRS, BlockFace.NORTH);
        // Bed
        b.setBlock(1, baseY + 1, 9, Material.RED_BED);
        b.setBlock(2, baseY + 1, 9, Material.BARREL);
        // Hanging lantern
        b.setBlock(0, baseY + 3, 8, Material.IRON_CHAIN);
        b.setBlock(0, baseY + 2, 8, Material.LANTERN);
        // Stern windows
        b.setBlock(-2, baseY + 2, 10, Material.GLASS_PANE);
        b.setBlock(2, baseY + 2, 10, Material.GLASS_PANE);
        b.setBlock(0, baseY + 2, 10, Material.GLASS_PANE);

        // ── Balloon envelope (gray wool dome, tapered) ──────────────────
        int balloonY = baseY + 6;
        for (int y = 0; y < 9; y++) {
            int r = y < 2 ? (2 + y) : (y > 6 ? (10 - y) : 4);
            b.filledCircle(0, balloonY + y, 0, r, Material.GRAY_WOOL);
        }
        // Balloon accent stripes (red + black)
        b.filledCircle(0, balloonY + 2, 0, 4, Material.RED_WOOL);
        b.filledCircle(0, balloonY + 4, 0, 4, Material.BLACK_WOOL);
        b.filledCircle(0, balloonY + 6, 0, 4, Material.RED_WOOL);

        // Ropes connecting balloon to hull
        for (int[] rope : new int[][]{{-2, -4}, {2, -4}, {-2, 4}, {2, 4}, {-3, 0}, {3, 0}}) {
            for (int ry = baseY + 3; ry <= balloonY; ry++) {
                b.setBlock(rope[0], ry, rope[1], Material.IRON_BARS);
            }
        }

        // ── Mast and crow's nest (forward) ──────────────────────────────
        b.pillar(0, baseY + 1, 14, -10, Material.OAK_LOG);
        b.fillBox(-2, baseY + 11, -12, 2, baseY + 11, -8, Material.OAK_PLANKS);
        b.fillWalls(-2, baseY + 12, -12, 2, baseY + 13, -8, Material.OAK_FENCE);
        b.setBlock(0, baseY + 12, -10, Material.LANTERN);
        b.setBlock(-1, baseY + 12, -10, Material.WHITE_BANNER);

        // ── Cannons (dispensers, 6 broadside) ───────────────────────────
        b.setDirectional(-3, baseY + 1, -6, Material.DISPENSER, BlockFace.WEST);
        b.setDirectional(3, baseY + 1, -6, Material.DISPENSER, BlockFace.EAST);
        b.setDirectional(-3, baseY + 1, -2, Material.DISPENSER, BlockFace.WEST);
        b.setDirectional(3, baseY + 1, -2, Material.DISPENSER, BlockFace.EAST);
        b.setDirectional(-3, baseY + 1, 2, Material.DISPENSER, BlockFace.WEST);
        b.setDirectional(3, baseY + 1, 2, Material.DISPENSER, BlockFace.EAST);
        // Cannon muzzles
        b.setBlock(-4, baseY + 1, -6, Material.OAK_FENCE);
        b.setBlock(4, baseY + 1, -6, Material.OAK_FENCE);
        b.setBlock(-4, baseY + 1, -2, Material.OAK_FENCE);
        b.setBlock(4, baseY + 1, -2, Material.OAK_FENCE);

        // ── Crossbow mounts on deck ─────────────────────────────────────
        b.setBlock(-3, baseY + 1, -4, Material.DARK_OAK_FENCE);
        b.setBlock(-3, baseY + 2, -4, Material.TRIPWIRE_HOOK);
        b.setBlock(3, baseY + 1, 2, Material.DARK_OAK_FENCE);
        b.setBlock(3, baseY + 2, 2, Material.TRIPWIRE_HOOK);

        // ── Banners (pillager iconography) ──────────────────────────────
        b.setBlock(0, baseY + 12, -10, Material.WHITE_BANNER);
        b.setBlock(-4, baseY + 2, 0, Material.WHITE_BANNER);
        b.setBlock(4, baseY + 2, 0, Material.WHITE_BANNER);
        b.setBlock(-4, baseY + 2, -4, Material.WHITE_BANNER);
        b.setBlock(4, baseY + 2, -4, Material.WHITE_BANNER);

        // ── Rigging (ladders as rope lines) ─────────────────────────────
        for (int y = baseY + 2; y <= baseY + 10; y++) {
            b.setBlock(0, y, -10, Material.LADDER);
            b.setBlock(-2, y, -10, Material.LADDER);
            b.setBlock(2, y, -10, Material.LADDER);
        }

        // ── Ship's wheel (center deck) ──────────────────────────────────
        b.setBlock(0, baseY + 1, 4, Material.SPRUCE_TRAPDOOR);
        b.setBlock(-1, baseY + 1, 4, Material.SPRUCE_TRAPDOOR);
        b.setBlock(1, baseY + 1, 4, Material.SPRUCE_TRAPDOOR);

        // ── Signal brazier aft deck ─────────────────────────────────────
        b.setBlock(0, baseY + 1, 5, Material.CAMPFIRE);
        b.setBlock(-1, baseY + 1, 5, Material.COBBLESTONE);
        b.setBlock(1, baseY + 1, 5, Material.COBBLESTONE);

        // ── Anchor chain (dangling over bow) ────────────────────────────
        for (int y = baseY - 1; y >= baseY - 4; y--) {
            b.setBlock(3, y, -10, Material.IRON_CHAIN);
        }
        b.setBlock(3, baseY - 5, -10, Material.IRON_BLOCK);

        // ── Loot ────────────────────────────────────────────────────────
        b.placeChest(0, baseY + 1, 9);    // captain's cabin
        b.placeChest(0, baseY - 2, 3);    // cargo hold
        b.placeChest(1, baseY + 1, -4);   // deck
        b.placeChest(0, baseY + 11, -10); // crow's nest
        b.placeChest(2, baseY - 2, 2);    // powder magazine
        b.setBossSpawn(0, baseY + 1, 0);
    }
}

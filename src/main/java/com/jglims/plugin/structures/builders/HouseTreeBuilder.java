package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

/**
 * HouseTreeBuilder — giant oak with 3-level treehouse (living / bedroom / lookout),
 * kitchen, rope bridges to branch platforms, hanging garden, swing, birdhouses,
 * branching canopies with leaves, roots, and dangling lanterns.
 */
public final class HouseTreeBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // ── Giant trunk (5-wide base, tapers) ───────────────────────────
        for (int y = 0; y <= 22; y++) {
            int r = y < 5 ? 3 : (y < 12 ? 2 : 1);
            b.filledCircle(0, y, 0, r, Material.OAK_LOG);
        }
        // Trunk weathering
        b.scatter(-3, 0, -3, 3, 22, 3, Material.OAK_LOG, Material.STRIPPED_OAK_LOG, 0.10);
        b.scatter(-3, 0, -3, 3, 12, 3, Material.OAK_LOG, Material.MOSSY_COBBLESTONE, 0.05);

        // ── Main canopy (layered leaves) ────────────────────────────────
        for (int y = 14; y <= 24; y++) {
            int r = y < 17 ? 8 : (y < 20 ? 6 : (y < 23 ? 4 : 2));
            b.filledCircle(0, y, 0, r, Material.OAK_LEAVES);
        }
        // Extra branch canopies
        b.filledCircle(6, 13, 3, 3, Material.OAK_LEAVES);
        b.filledCircle(-5, 14, -4, 3, Material.OAK_LEAVES);
        b.filledCircle(4, 12, -5, 3, Material.OAK_LEAVES);
        b.filledCircle(-4, 13, 5, 3, Material.OAK_LEAVES);

        // Branch supports (flying buttresses of oak log)
        b.flyingButtress(2, 10, 0, 6, 13, 3, Material.OAK_LOG);
        b.flyingButtress(-2, 11, 0, -5, 14, -4, Material.OAK_LOG);
        b.flyingButtress(0, 10, -2, 4, 12, -5, Material.OAK_LOG);
        b.flyingButtress(0, 11, 2, -4, 13, 5, Material.OAK_LOG);

        // Cherry-blossom accent clusters on canopy tips
        b.filledCircle(6, 14, 3, 1, Material.CHERRY_LEAVES);
        b.filledCircle(-5, 15, -4, 1, Material.CHERRY_LEAVES);

        // Dangling vines around canopy
        b.addVines(-8, 14, -8, 8, 20, 8, 0.25);

        // ── Level 1 room (y=5–8): living room ───────────────────────────
        b.fillBox(-5, 5, -5, 5, 5, 5, Material.OAK_PLANKS);
        b.fillWalls(-5, 6, -5, 5, 8, 5, Material.OAK_PLANKS);
        b.hollowBox(-5, 6, -5, 5, 8, 5);
        // Log corner trim
        for (int[] c : new int[][]{{-5, -5}, {5, -5}, {-5, 5}, {5, 5}}) {
            b.pillar(c[0], 5, 9, c[1], Material.OAK_LOG);
        }
        // Windows
        b.setBlock(-5, 7, -1, Material.GLASS_PANE);
        b.setBlock(-5, 7, 1, Material.GLASS_PANE);
        b.setBlock(5, 7, -1, Material.GLASS_PANE);
        b.setBlock(5, 7, 1, Material.GLASS_PANE);
        b.setBlock(0, 7, 5, Material.GLASS_PANE);
        // Door (to rope bridge)
        b.setBlock(5, 6, 0, Material.AIR);
        b.setBlock(5, 7, 0, Material.AIR);
        // Furniture: dining table + chairs
        b.table(0, 6, 0);
        b.chair(-1, 6, 0, Material.OAK_STAIRS, BlockFace.EAST);
        b.chair(1, 6, 0, Material.OAK_STAIRS, BlockFace.WEST);
        b.chair(0, 6, -1, Material.OAK_STAIRS, BlockFace.SOUTH);
        // Fireplace nook
        b.setBlock(-4, 6, -4, Material.CAMPFIRE);
        b.setBlock(-4, 7, -4, Material.COBBLESTONE);
        b.setBlock(-4, 8, -4, Material.COBBLESTONE);
        // Bookshelves
        b.setBlock(3, 6, -4, Material.BOOKSHELF);
        b.setBlock(3, 7, -4, Material.BOOKSHELF);
        b.setBlock(4, 7, -4, Material.LECTERN);
        // Lighting
        b.setBlock(-3, 8, 3, Material.LANTERN);
        b.setBlock(3, 8, -3, Material.LANTERN);

        // ── Kitchen nook (attached west on level 1) ─────────────────────
        b.fillBox(-7, 5, -2, -6, 5, 2, Material.OAK_PLANKS);
        b.fillWalls(-7, 6, -2, -6, 8, 2, Material.OAK_PLANKS);
        b.hollowBox(-7, 6, -2, -6, 8, 2);
        b.setBlock(-5, 6, 0, Material.AIR);  // doorway
        b.setBlock(-7, 6, -1, Material.FURNACE);
        b.setBlock(-7, 6, 0, Material.SMOKER);
        b.setBlock(-7, 6, 1, Material.BARREL);
        b.setBlock(-6, 6, 1, Material.COMPOSTER);
        b.setBlock(-6, 7, -1, Material.LANTERN);

        // ── Level 2 room (y=9–12): bedroom ──────────────────────────────
        b.fillBox(-3, 9, -3, 3, 9, 3, Material.OAK_PLANKS);
        b.fillWalls(-3, 10, -3, 3, 12, 3, Material.OAK_PLANKS);
        b.hollowBox(-3, 10, -3, 3, 12, 3);
        for (int[] c : new int[][]{{-3, -3}, {3, -3}, {-3, 3}, {3, 3}}) {
            b.pillar(c[0], 9, 12, c[1], Material.OAK_LOG);
        }
        // Windows
        b.setBlock(-3, 11, 0, Material.GLASS_PANE);
        b.setBlock(3, 11, 0, Material.GLASS_PANE);
        b.setBlock(0, 11, -3, Material.GLASS_PANE);
        // Bed + nightstand
        b.setBlock(-2, 10, 2, Material.RED_BED);
        b.setBlock(-2, 10, -2, Material.BARREL);
        b.setBlock(-2, 11, -2, Material.LANTERN);
        // Bookshelf wall
        b.setBlock(2, 10, -2, Material.BOOKSHELF);
        b.setBlock(2, 10, 0, Material.BOOKSHELF);
        b.setBlock(2, 11, -2, Material.BOOKSHELF);
        b.setBlock(2, 11, 0, Material.BOOKSHELF);
        b.setBlock(2, 10, 2, Material.ENCHANTING_TABLE);

        // ── Level 3 lookout platform (y=14) ─────────────────────────────
        b.fillBox(-3, 14, -3, 3, 14, 3, Material.OAK_PLANKS);
        b.fillWalls(-3, 15, -3, 3, 15, 3, Material.OAK_FENCE);
        b.setBlock(0, 15, 0, Material.LANTERN);
        b.setBlock(2, 15, 2, Material.BARREL);
        b.setBlock(-2, 15, -2, Material.LODESTONE);

        // ── Ladders between levels ──────────────────────────────────────
        for (int y = 1; y <= 5; y++) b.setBlock(3, y, -2, Material.LADDER);
        for (int y = 6; y <= 9; y++) b.setBlock(2, y, -2, Material.LADDER);
        for (int y = 10; y <= 14; y++) b.setBlock(1, y, -1, Material.LADDER);

        // ── Rope bridge to east branch canopy ───────────────────────────
        for (int x = 2; x <= 6; x++) {
            b.setBlock(x, 14, 3, Material.OAK_PLANKS);
            b.setBlock(x, 15, 4, Material.OAK_FENCE);
            b.setBlock(x, 15, 2, Material.OAK_FENCE);
        }
        // Rope strings
        for (int x = 2; x <= 6; x += 2) {
            b.setBlock(x, 16, 4, Material.TRIPWIRE);
            b.setBlock(x, 16, 2, Material.TRIPWIRE);
        }
        // Branch platform (tree-fort)
        b.fillBox(5, 14, 2, 7, 14, 4, Material.OAK_PLANKS);
        b.setBlock(6, 15, 3, Material.LANTERN);
        b.setBlock(7, 15, 3, Material.BARREL);

        // ── Swing hanging from south branch ─────────────────────────────
        b.setBlock(0, 13, -5, Material.IRON_CHAIN);
        b.setBlock(0, 12, -5, Material.IRON_CHAIN);
        b.setBlock(0, 11, -5, Material.OAK_SLAB);

        // ── Birdhouse on west branch ────────────────────────────────────
        b.pillar(-4, 1, 6, -4, Material.OAK_FENCE);
        b.setBlock(-4, 7, -4, Material.OAK_PLANKS);
        b.setBlock(-4, 8, -4, Material.DECORATED_POT);

        // ── Hanging garden pots on level 1 porch ────────────────────────
        b.setBlock(-5, 5, -5, Material.POTTED_FERN);
        b.setBlock(5, 5, 5, Material.POTTED_AZURE_BLUET);
        b.setBlock(-5, 5, 5, Material.POTTED_POPPY);
        b.setBlock(5, 5, -5, Material.POTTED_DANDELION);

        // ── Roots at base ───────────────────────────────────────────────
        for (int[] r : new int[][]{{-3, -3}, {3, 3}, {-3, 3}, {3, -3}, {-4, 0}, {4, 0}, {0, -4}, {0, 4}}) {
            b.setBlock(r[0], 0, r[1], Material.OAK_LOG);
            b.setBlock(r[0], -1, r[1], Material.MANGROVE_ROOTS);
        }

        // Ground moss + flowers
        b.filledCircle(0, 0, 0, 7, Material.GRASS_BLOCK);
        b.scatter(-7, 1, -7, 7, 1, 7, Material.AIR, Material.POPPY, 0.08);
        b.scatter(-7, 1, -7, 7, 1, 7, Material.AIR, Material.DANDELION, 0.08);

        // Hanging lanterns from canopy
        for (int[] hl : new int[][]{{-4, -4}, {4, 4}, {-4, 4}, {4, -4}}) {
            b.setBlock(hl[0], 15, hl[1], Material.IRON_CHAIN);
            b.setBlock(hl[0], 14, hl[1], Material.LANTERN);
        }

        // ── Loot ────────────────────────────────────────────────────────
        b.placeChest(-2, 6, -2);   // living room
        b.placeChest(2, 10, 2);    // bedroom
        b.placeChest(2, 14, 2);    // lookout
        b.placeChest(-7, 6, 1);    // kitchen
        b.placeChest(7, 14, 3);    // tree-fort branch
        b.setBossSpawn(0, 6, 0);
    }
}

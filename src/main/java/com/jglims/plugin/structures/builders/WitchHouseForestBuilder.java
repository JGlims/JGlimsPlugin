package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;

/**
 * WitchHouseForestBuilder — stilted dark-forest cottage with bubbling cauldron room,
 * brewing loft, ritual circle, mushroom garden, hanging bone wind chimes,
 * potion shelves, jack-o-lantern path, cat familiar perch, and soul-flame sconces.
 */
public final class WitchHouseForestBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // ── Stilts (dark oak logs deep into ground) ─────────────────────
        for (int[] p : new int[][]{{-4, -4}, {4, -4}, {-4, 4}, {4, 4}, {0, -4}, {0, 4}}) {
            b.pillar(p[0], -3, 0, p[1], Material.DARK_OAK_LOG);
        }

        // ── Platform + main floor ───────────────────────────────────────
        b.fillBox(-5, 0, -5, 5, 0, 5, Material.SPRUCE_PLANKS);
        b.scatter(-5, 0, -5, 5, 0, 5, Material.SPRUCE_PLANKS, Material.DARK_OAK_PLANKS, 0.3);

        // ── Walls (spruce with dark-oak trim) ───────────────────────────
        b.fillWalls(-5, 1, -5, 5, 5, 5, Material.SPRUCE_PLANKS);
        b.hollowBox(-5, 1, -5, 5, 5, 5);
        // Corner trim
        for (int[] c : new int[][]{{-5, -5}, {5, -5}, {-5, 5}, {5, 5}}) {
            b.pillar(c[0], 1, 6, c[1], Material.DARK_OAK_LOG);
        }
        b.scatter(-5, 1, -5, 5, 5, 5, Material.SPRUCE_PLANKS, Material.MOSSY_COBBLESTONE, 0.08);

        // ── Pyramid roof with spruce slabs ──────────────────────────────
        b.pyramidRoof(-6, -6, 6, 6, 6, Material.SPRUCE_PLANKS);
        b.setBlock(0, 10, 0, Material.DARK_OAK_LOG);
        // Crooked chimney
        b.pillar(-3, 6, 10, -3, Material.COBBLESTONE);
        b.setBlock(-3, 11, -3, Material.CAMPFIRE);

        // ── Purple stained-glass windows ────────────────────────────────
        b.setBlock(-5, 3, 0, Material.PURPLE_STAINED_GLASS);
        b.setBlock(5, 3, 0, Material.PURPLE_STAINED_GLASS);
        b.setBlock(0, 3, 5, Material.PURPLE_STAINED_GLASS);
        b.setBlock(-5, 4, -2, Material.MAGENTA_STAINED_GLASS);
        b.setBlock(5, 4, 2, Material.MAGENTA_STAINED_GLASS);
        b.setBlock(-5, 4, 2, Material.PINK_STAINED_GLASS);
        b.setBlock(5, 4, -2, Material.PINK_STAINED_GLASS);
        // Door
        b.setBlock(0, 1, -5, Material.AIR);
        b.setBlock(0, 2, -5, Material.AIR);

        // ── Cauldron cooking station (center) ───────────────────────────
        b.setBlock(0, 1, 0, Material.CAULDRON);
        b.setBlock(0, 0, 0, Material.CAMPFIRE);
        // Hanging cauldron chain
        b.setBlock(0, 5, 0, Material.IRON_CHAIN);
        b.setBlock(0, 4, 0, Material.IRON_CHAIN);
        b.setBlock(0, 3, 0, Material.IRON_CHAIN);
        // Rune circle around cauldron
        for (int[] rune : new int[][]{{-2, 0}, {2, 0}, {0, -2}, {0, 2}, {-2, -2}, {2, -2}, {-2, 2}, {2, 2}}) {
            b.setBlock(rune[0], 1, rune[1], Material.MOSS_CARPET);
        }

        // ── Brewing corner (NW) ─────────────────────────────────────────
        b.setBlock(-3, 1, -3, Material.BREWING_STAND);
        b.setBlock(-4, 1, -3, Material.BREWING_STAND);
        b.setBlock(-3, 1, -4, Material.BREWING_STAND);
        b.setBlock(-4, 1, -4, Material.ENCHANTING_TABLE);

        // ── Bookshelf wall (west) ───────────────────────────────────────
        b.bookshelfWall(-4, 1, 4, -2, 3);
        b.setBlock(-3, 1, 4, Material.LECTERN);

        // ── Potion shelves (east) ───────────────────────────────────────
        b.setBlock(3, 1, 3, Material.BARREL);
        b.setBlock(4, 1, 3, Material.BARREL);
        b.setBlock(3, 2, 3, Material.BARREL);
        b.setBlock(4, 2, 3, Material.BARREL);
        b.setBlock(4, 3, 3, Material.BARREL);

        // ── Bed nook (SE) ───────────────────────────────────────────────
        b.setBlock(3, 1, -4, Material.PURPLE_BED);
        b.setBlock(4, 1, -3, Material.BARREL);
        b.setBlock(4, 2, -3, Material.LANTERN);

        // ── Cat familiar perch (NE corner) ──────────────────────────────
        b.setBlock(4, 1, -4, Material.CHEST);  // decor only
        b.setBlock(4, 2, -4, Material.BLACK_CARPET);
        b.setBlock(4, 3, -4, Material.OAK_FENCE);

        // ── Ceiling hanging herbs + bones ───────────────────────────────
        for (int[] h : new int[][]{{-3, 0}, {3, 0}, {0, -3}, {0, 3}, {-2, -2}, {2, 2}, {2, -2}, {-2, 2}}) {
            b.setBlock(h[0], 5, h[1], Material.IRON_CHAIN);
            b.setBlock(h[0], 4, h[1], b.getRandom().nextBoolean() ? Material.BROWN_MUSHROOM : Material.RED_MUSHROOM);
        }

        // ── Bone wind chimes on the east porch ──────────────────────────
        for (int z : new int[]{-1, 0, 1}) {
            b.setBlock(6, 4, z, Material.IRON_CHAIN);
            b.setBlock(6, 3, z, Material.BONE_BLOCK);
        }

        // ── Cobwebs + dust ──────────────────────────────────────────────
        b.setBlock(-4, 4, -4, Material.COBWEB);
        b.setBlock(4, 4, 4, Material.COBWEB);
        b.setBlock(-4, 4, 4, Material.COBWEB);
        b.setBlock(4, 4, -4, Material.COBWEB);
        b.setBlock(0, 4, 0, Material.COBWEB);

        // ── Soul-fire sconces ───────────────────────────────────────────
        b.setBlock(-3, 4, 0, Material.SOUL_LANTERN);
        b.setBlock(3, 4, 0, Material.SOUL_LANTERN);
        b.setBlock(0, 4, -3, Material.SOUL_LANTERN);
        b.setBlock(0, 4, 3, Material.SOUL_LANTERN);

        // ── Jack-o-lantern under house (eerie underfloor glow) ──────────
        b.setBlock(0, -1, 0, Material.JACK_O_LANTERN);
        b.setBlock(-2, -1, 2, Material.JACK_O_LANTERN);
        b.setBlock(2, -1, -2, Material.JACK_O_LANTERN);

        // ── Flower pots with witch flora ────────────────────────────────
        b.setBlock(2, 1, -4, Material.POTTED_DEAD_BUSH);
        b.setBlock(-2, 1, 4, Material.POTTED_DEAD_BUSH);
        b.setBlock(2, 1, 4, Material.POTTED_RED_MUSHROOM);
        b.setBlock(-2, 1, -4, Material.POTTED_BROWN_MUSHROOM);
        b.setBlock(3, 1, 4, Material.POTTED_AZALEA_BUSH);

        // ── Hanging lanterns under eaves ────────────────────────────────
        b.setBlock(-5, 0, -5, Material.IRON_CHAIN);
        b.setBlock(-5, -1, -5, Material.LANTERN);
        b.setBlock(5, 0, -5, Material.IRON_CHAIN);
        b.setBlock(5, -1, -5, Material.LANTERN);
        b.setBlock(5, 0, 5, Material.IRON_CHAIN);
        b.setBlock(5, -1, 5, Material.LANTERN);
        b.setBlock(-5, 0, 5, Material.IRON_CHAIN);
        b.setBlock(-5, -1, 5, Material.SOUL_LANTERN);

        // ── Mushroom garden below the house ─────────────────────────────
        for (int x = -6; x <= 6; x += 2) {
            for (int z = -7; z <= 7; z += 3) {
                if (Math.abs(x) > 5 || Math.abs(z) > 5) {
                    b.setBlock(x, -1, z, Material.PODZOL);
                    b.setBlock(x, 0, z, b.getRandom().nextBoolean() ? Material.RED_MUSHROOM : Material.BROWN_MUSHROOM);
                }
            }
        }
        // Larger mushroom sprouts
        b.setBlock(-7, 0, 0, Material.RED_MUSHROOM_BLOCK);
        b.setBlock(7, 0, 0, Material.BROWN_MUSHROOM_BLOCK);
        b.setBlock(0, 0, -7, Material.MUSHROOM_STEM);

        // ── Ritual circle 10 blocks south (on ground level) ─────────────
        b.circle(0, -1, -10, 4, Material.SOUL_SOIL);
        for (int a = 0; a < 360; a += 72) {
            int rx = (int) Math.round(4 * Math.cos(Math.toRadians(a)));
            int rz = (int) Math.round(4 * Math.sin(Math.toRadians(a)));
            b.setBlock(rx, 0, -10 + rz, Material.SOUL_CAMPFIRE);
        }
        b.setBlock(0, 0, -10, Material.LODESTONE);

        // ── Jack-o-lantern path from door to ritual circle ──────────────
        for (int z = -6; z >= -9; z--) {
            if (z % 2 == 0) b.setBlock(0, -1, z, Material.JACK_O_LANTERN);
        }

        // ── Brewing loft above main floor ───────────────────────────────
        b.fillBox(-3, 5, -3, 3, 5, 3, Material.SPRUCE_PLANKS);
        b.setBlock(-1, 5, 1, Material.AIR);  // hatch
        b.setBlock(0, 5, 1, Material.AIR);
        b.setBlock(1, 5, 1, Material.AIR);
        for (int y = 1; y <= 5; y++) b.setBlock(3, y, 1, Material.LADDER);
        b.setBlock(-2, 6, -2, Material.CAULDRON);
        b.setBlock(2, 6, -2, Material.BREWING_STAND);
        b.setBlock(-2, 6, 2, Material.BOOKSHELF);
        b.setBlock(2, 6, 2, Material.LECTERN);
        b.setBlock(0, 7, 0, Material.SOUL_LANTERN);

        // ── Exterior vines + cobwebs ────────────────────────────────────
        b.addVines(-6, 1, -6, 6, 6, 6, 0.18);

        // ── Loot ────────────────────────────────────────────────────────
        b.placeChest(2, 1, -2);    // next to bed nook
        b.placeChest(-4, 1, -3);   // brewing corner
        b.placeChest(-2, 6, 2);    // brewing loft
        b.placeChest(0, 0, -10);   // ritual lodestone
        b.setBossSpawn(0, 1, 0);
    }
}

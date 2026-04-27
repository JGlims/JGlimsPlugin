package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;

/**
 * CampingLargeBuilder — fortified wilderness camp with palisade wall, 6 colored tents,
 * central fire, mess table, archery range, horse pen, lookout tower, smithy,
 * signal bonfire, latrine outhouse, supply tent, and hunter's trophy rack.
 */
public final class CampingLargeBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // ── Ground pad (grass with dirt patches) ────────────────────────
        b.filledCircle(0, 0, 0, 12, Material.GRASS_BLOCK);
        b.scatter(-12, 0, -12, 12, 0, 12, Material.GRASS_BLOCK, Material.COARSE_DIRT, 0.20);
        b.scatter(-12, 0, -12, 12, 0, 12, Material.GRASS_BLOCK, Material.DIRT_PATH, 0.10);
        b.scatter(-12, 0, -12, 12, 0, 12, Material.GRASS_BLOCK, Material.PODZOL, 0.05);

        // Central dirt-path crossroads
        for (int d = -9; d <= 9; d++) {
            b.setBlock(0, 0, d, Material.DIRT_PATH);
            b.setBlock(d, 0, 0, Material.DIRT_PATH);
        }

        // ── Palisade wall (stripped oak logs, ring) ─────────────────────
        b.circle(0, 0, 0, 10, Material.STRIPPED_OAK_LOG);
        for (int a = 0; a < 360; a += 6) {
            int wx = (int) Math.round(10 * Math.cos(Math.toRadians(a)));
            int wz = (int) Math.round(10 * Math.sin(Math.toRadians(a)));
            b.pillar(wx, 1, 4, wz, Material.STRIPPED_OAK_LOG);
            // Sharpened tips
            if (a % 12 == 0) b.setBlock(wx, 5, wz, Material.OAK_FENCE);
        }
        // Gate opening (south)
        for (int y = 1; y <= 4; y++) {
            b.setBlock(0, y, -10, Material.AIR);
            b.setBlock(1, y, -10, Material.AIR);
        }
        // Gate lintel
        for (int x = -1; x <= 2; x++) b.setBlock(x, 5, -10, Material.STRIPPED_OAK_LOG);
        // Gate torches
        b.setBlock(-1, 5, -11, Material.TORCH);
        b.setBlock(2, 5, -11, Material.TORCH);

        // ── Central firepit ─────────────────────────────────────────────
        b.circle(0, 0, 0, 2, Material.COBBLESTONE);
        b.setBlock(0, 1, 0, Material.CAMPFIRE);
        b.setBlock(1, 1, 0, Material.CAMPFIRE);
        b.setBlock(0, 1, 1, Material.CAMPFIRE);
        b.setBlock(-1, 1, 0, Material.CAMPFIRE);
        // Surrounding log seats
        for (int[] s : new int[][]{{-3, 0}, {3, 0}, {0, -3}, {0, 3}, {-2, -2}, {2, -2}, {-2, 2}, {2, 2}}) {
            b.setBlock(s[0], 1, s[1], Material.OAK_LOG);
        }
        // Cooking tripod
        b.setBlock(1, 1, 1, Material.CAULDRON);
        b.setBlock(-1, 1, 1, Material.SMOKER);

        // ── 6 tents around the firepit (ring) ───────────────────────────
        int[][] tentPositions = {{-6, -6}, {6, -6}, {-6, 6}, {6, 6}, {-8, 0}, {8, 0}};
        Material[] tentColors = {
                Material.WHITE_WOOL, Material.RED_WOOL, Material.BLUE_WOOL,
                Material.GREEN_WOOL, Material.YELLOW_WOOL, Material.ORANGE_WOOL
        };
        Material[] carpetColors = {
                Material.WHITE_CARPET, Material.RED_CARPET, Material.BLUE_CARPET,
                Material.GREEN_CARPET, Material.YELLOW_CARPET, Material.ORANGE_CARPET
        };
        for (int i = 0; i < tentPositions.length; i++) {
            int tx = tentPositions[i][0];
            int tz = tentPositions[i][1];
            // Corner posts
            b.setBlock(tx - 1, 1, tz - 1, Material.OAK_FENCE);
            b.setBlock(tx + 1, 1, tz - 1, Material.OAK_FENCE);
            b.setBlock(tx - 1, 1, tz + 1, Material.OAK_FENCE);
            b.setBlock(tx + 1, 1, tz + 1, Material.OAK_FENCE);
            // Canopy (wool roof, 2 layers for pitched feel)
            b.fillBox(tx - 1, 2, tz - 1, tx + 1, 2, tz + 1, tentColors[i]);
            b.setBlock(tx, 3, tz, tentColors[i]);
            // Floor carpet
            b.setBlock(tx, 1, tz, carpetColors[i]);
            // Sleeping gear
            b.setBlock(tx, 1, tz - 1, Material.WHITE_BED);
            // Backpack
            b.setBlock(tx + 1, 1, tz, Material.BARREL);
        }

        // ── Lookout tower (NE corner) ───────────────────────────────────
        b.fillBox(6, 0, -8, 8, 0, -6, Material.COBBLESTONE);
        b.pillar(6, 1, 8, -8, Material.STRIPPED_OAK_LOG);
        b.pillar(8, 1, 8, -8, Material.STRIPPED_OAK_LOG);
        b.pillar(6, 1, 8, -6, Material.STRIPPED_OAK_LOG);
        b.pillar(8, 1, 8, -6, Material.STRIPPED_OAK_LOG);
        b.fillBox(5, 8, -9, 9, 8, -5, Material.OAK_PLANKS);
        b.fillWalls(5, 9, -9, 9, 10, -5, Material.OAK_FENCE);
        b.pyramidRoof(5, -9, 9, -5, 11, Material.DARK_OAK_STAIRS);
        for (int y = 1; y <= 8; y++) b.setBlock(7, y, -6, Material.LADDER);
        b.setBlock(7, 9, -7, Material.LANTERN);
        b.setBlock(7, 9, -8, Material.CAMPFIRE);  // signal fire atop

        // ── Smithy tent (NW) ────────────────────────────────────────────
        b.fillBox(-7, 0, -7, -5, 0, -5, Material.COBBLESTONE);
        b.setBlock(-7, 1, -7, Material.OAK_FENCE);
        b.setBlock(-5, 1, -7, Material.OAK_FENCE);
        b.setBlock(-7, 1, -5, Material.OAK_FENCE);
        b.setBlock(-5, 1, -5, Material.OAK_FENCE);
        b.fillBox(-7, 2, -7, -5, 2, -5, Material.GRAY_WOOL);
        b.setBlock(-6, 1, -7, Material.FURNACE);
        b.setBlock(-6, 1, -6, Material.ANVIL);
        b.setBlock(-6, 1, -5, Material.GRINDSTONE);
        b.setBlock(-7, 1, -6, Material.SMITHING_TABLE);
        b.setBlock(-5, 1, -6, Material.BARREL);
        // Weapon rack
        b.setBlock(-6, 2, -6, Material.LANTERN);

        // ── Supply tent (SW) ────────────────────────────────────────────
        b.fillBox(-7, 0, 5, -5, 0, 7, Material.COBBLESTONE);
        b.setBlock(-7, 1, 5, Material.OAK_FENCE);
        b.setBlock(-5, 1, 5, Material.OAK_FENCE);
        b.setBlock(-7, 1, 7, Material.OAK_FENCE);
        b.setBlock(-5, 1, 7, Material.OAK_FENCE);
        b.fillBox(-7, 2, 5, -5, 2, 7, Material.BROWN_WOOL);
        b.setBlock(-6, 1, 6, Material.BARREL);
        b.setBlock(-7, 1, 6, Material.BARREL);
        b.setBlock(-5, 1, 6, Material.BARREL);
        b.setBlock(-6, 2, 5, Material.BARREL);
        b.setBlock(-6, 2, 7, Material.BARREL);

        // ── Horse pen (SE) ──────────────────────────────────────────────
        b.fillBox(4, 0, 5, 8, 0, 8, Material.COARSE_DIRT);
        for (int x = 4; x <= 8; x++) {
            b.setBlock(x, 1, 5, Material.OAK_FENCE);
            b.setBlock(x, 1, 8, Material.OAK_FENCE);
        }
        for (int z = 5; z <= 8; z++) {
            b.setBlock(8, 1, z, Material.OAK_FENCE);
        }
        // Gate
        b.setBlock(4, 1, 6, Material.OAK_FENCE_GATE);
        b.setBlock(4, 1, 7, Material.OAK_FENCE_GATE);
        // Feed trough
        b.setBlock(5, 1, 7, Material.HAY_BLOCK);
        b.setBlock(6, 1, 7, Material.HAY_BLOCK);
        b.setBlock(7, 1, 7, Material.WATER_CAULDRON);
        // Hitching post
        b.setBlock(5, 1, 6, Material.OAK_FENCE);
        b.setBlock(5, 2, 6, Material.TRIPWIRE);

        // ── Archery range (east alley) ──────────────────────────────────
        b.fillBox(4, 0, -3, 9, 0, 3, Material.COARSE_DIRT);
        // Firing line (hay bale markers)
        b.setBlock(5, 1, 0, Material.HAY_BLOCK);
        b.setBlock(5, 1, -1, Material.HAY_BLOCK);
        b.setBlock(5, 1, 1, Material.HAY_BLOCK);
        // Targets (concentric wool squares)
        b.setBlock(9, 1, 0, Material.HAY_BLOCK);
        b.setBlock(9, 2, 0, Material.RED_WOOL);
        b.setBlock(9, 2, -1, Material.WHITE_WOOL);
        b.setBlock(9, 2, 1, Material.WHITE_WOOL);
        b.setBlock(9, 3, 0, Material.WHITE_WOOL);

        // ── Latrine outhouse (far SE) ───────────────────────────────────
        b.fillBox(7, 0, -9, 8, 0, -9, Material.COBBLESTONE);
        b.fillWalls(7, 1, -9, 8, 3, -9, Material.OAK_PLANKS);
        b.hollowBox(7, 1, -10, 8, 3, -8);
        b.setBlock(7, 1, -10, Material.OAK_DOOR);
        b.setBlock(8, 4, -9, Material.OAK_STAIRS);
        b.setBlock(7, 4, -9, Material.OAK_STAIRS);

        // ── Hunter's trophy rack (near gate) ────────────────────────────
        b.pillar(-3, 1, 3, -9, Material.OAK_FENCE);
        b.pillar(3, 1, 3, -9, Material.OAK_FENCE);
        b.setBlock(-3, 3, -9, Material.OAK_FENCE);
        b.setBlock(-2, 3, -9, Material.BONE_BLOCK);
        b.setBlock(-1, 3, -9, Material.BONE_BLOCK);
        b.setBlock(0, 3, -9, Material.OAK_LOG);
        b.setBlock(1, 3, -9, Material.BONE_BLOCK);
        b.setBlock(2, 3, -9, Material.BONE_BLOCK);
        b.setBlock(3, 3, -9, Material.OAK_FENCE);

        // ── Signal fire on tower roof already placed ───────────────────
        // Perimeter torches on palisade
        for (int a = 0; a < 360; a += 45) {
            int tx = (int) Math.round(10 * Math.cos(Math.toRadians(a)));
            int tz = (int) Math.round(10 * Math.sin(Math.toRadians(a)));
            b.setBlock(tx, 5, tz, Material.TORCH);
        }

        // Decoration: scattered logs & stumps
        for (int[] stmp : new int[][]{{-4, 7}, {7, -4}, {-7, 0}, {0, 8}}) {
            b.setBlock(stmp[0], 1, stmp[1], Material.OAK_LOG);
        }

        // Weathering
        b.scatter(-10, 1, -10, 10, 3, 10, Material.STRIPPED_OAK_LOG, Material.OAK_LOG, 0.1);

        // ── Loot ────────────────────────────────────────────────────────
        b.placeChest(3, 1, -3);    // firepit quartermaster
        b.placeChest(-3, 1, 3);    // opposite firepit
        b.placeChest(-6, 1, -6);   // smithy
        b.placeChest(-6, 1, 6);    // supply tent
        b.placeChest(7, 9, -7);    // watchtower
        b.setBossSpawn(0, 1, 0);
    }
}

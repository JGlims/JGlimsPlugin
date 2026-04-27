package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

/**
 * AlienCitadelBuilder — sprawling fortress with command center, hangar bay, prison block,
 * research lab, underground power core, medbay, teleporter pad, observation deck, armory,
 * sentry turrets, reinforced corners, floodlight perimeter, hangar crane, data terminals.
 */
public final class AlienCitadelBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // ── Main platform ───────────────────────────────────────────────
        b.fillBox(-40, 0, -20, 40, 0, 20, Material.GRAY_CONCRETE);
        b.scatter(-40, 0, -20, 40, 0, 20, Material.GRAY_CONCRETE, Material.LIGHT_GRAY_CONCRETE, 0.15);
        // Hazard stripe on landing strip
        for (int x = -39; x <= 39; x += 3) {
            b.setBlock(x, 0, 0, Material.YELLOW_CONCRETE);
        }

        // ── Outer walls (layered materials) ─────────────────────────────
        b.fillWalls(-40, 1, -20, 40, 16, 20, Material.GRAY_CONCRETE);
        b.hollowBox(-40, 1, -20, 40, 16, 20);
        b.fillWalls(-40, 1, -20, 40, 2, 20, Material.DEEPSLATE_BRICKS);  // base course
        b.scatter(-40, 1, -20, 40, 16, 20, Material.GRAY_CONCRETE, Material.LIGHT_GRAY_CONCRETE, 0.12);

        // Iron reinforcement bands
        for (int y : new int[]{4, 8, 12}) {
            b.fillBox(-40, y, -20, 40, y, -20, Material.IRON_BLOCK);
            b.fillBox(-40, y, 20, 40, y, 20, Material.IRON_BLOCK);
            b.fillBox(-40, y, -20, -40, y, 20, Material.IRON_BLOCK);
            b.fillBox(40, y, -20, 40, y, 20, Material.IRON_BLOCK);
        }
        // Corner reinforcements
        for (int[] c : new int[][]{{-40, -20}, {40, -20}, {-40, 20}, {40, 20}}) {
            b.pillar(c[0], 0, 18, c[1], Material.POLISHED_DEEPSLATE);
            b.setBlock(c[0], 17, c[1], Material.LIGHTNING_ROD);
        }

        // ── Floor ───────────────────────────────────────────────────────
        b.fillFloor(-39, -19, 39, 19, 0, Material.LIGHT_GRAY_CONCRETE);

        // ── Command center (central, elevated dome) ─────────────────────
        b.fillBox(-10, 0, 8, 10, 20, 19, Material.IRON_BLOCK);
        b.fillBox(-9, 1, 9, 9, 19, 18, Material.AIR);
        b.fillFloor(-9, 9, 9, 18, 0, Material.GRAY_CONCRETE);
        // Control panels
        b.fillBox(-8, 1, 17, 8, 3, 17, Material.REDSTONE_LAMP);
        b.fillBox(-8, 1, 16, 8, 1, 16, Material.SMOOTH_STONE);
        b.setBlock(0, 2, 16, Material.LEVER);
        b.setBlock(-4, 2, 16, Material.STONE_BUTTON);
        b.setBlock(4, 2, 16, Material.STONE_BUTTON);
        b.setBlock(-7, 2, 16, Material.REPEATER);
        b.setBlock(7, 2, 16, Material.COMPARATOR);
        // Observation windows
        for (int x = -7; x <= 7; x += 2) b.setBlock(x, 10, 19, Material.GRAY_STAINED_GLASS_PANE);
        // Central holo-projector
        b.setBlock(0, 1, 12, Material.LODESTONE);
        b.setBlock(0, 2, 12, Material.END_ROD);
        b.setBlock(0, 5, 12, Material.BEACON);
        // Command chairs
        b.chair(-2, 1, 11, Material.POLISHED_ANDESITE_STAIRS, BlockFace.NORTH);
        b.chair(2, 1, 11, Material.POLISHED_ANDESITE_STAIRS, BlockFace.NORTH);
        // Observation-deck ladder
        for (int y = 1; y <= 17; y++) b.setBlock(9, y, 17, Material.LADDER);
        // Observation deck (upper level)
        b.fillBox(-8, 18, 10, 8, 18, 17, Material.IRON_BLOCK);
        b.setBlock(0, 19, 13, Material.BEACON);

        // ── Hangar bay (west wing) ──────────────────────────────────────
        b.fillBox(-39, 0, -15, -20, 12, 5, Material.GRAY_CONCRETE);
        b.fillBox(-38, 1, -14, -21, 11, 4, Material.AIR);
        b.fillFloor(-38, -14, -21, 4, 0, Material.SMOOTH_STONE);
        // Hangar door (open)
        b.fillBox(-38, 1, -15, -21, 8, -15, Material.AIR);
        // Crane rail
        b.fillBox(-37, 11, -5, -22, 11, -5, Material.IRON_BLOCK);
        b.setBlock(-30, 10, -5, Material.IRON_BARS);
        b.setBlock(-30, 9, -5, Material.IRON_BARS);
        b.setBlock(-30, 8, -5, Material.IRON_BLOCK);
        // Landing lights
        for (int x = -37; x <= -22; x += 3) {
            b.setBlock(x, 0, -14, Material.REDSTONE_LAMP);
            b.setBlock(x, 0, 3, Material.REDSTONE_LAMP);
        }
        // Fuel drums
        for (int[] d : new int[][]{{-36, 3}, {-34, 3}, {-32, 3}}) {
            b.pillar(d[0], 1, 2, d[1], Material.BLACK_CONCRETE);
            b.setBlock(d[0], 3, d[1], Material.IRON_BLOCK);
        }

        // ── Prison block (east wing) ────────────────────────────────────
        b.fillBox(20, 0, -15, 39, 10, 5, Material.GRAY_CONCRETE);
        b.fillBox(21, 1, -14, 38, 9, 4, Material.AIR);
        // 4 cells with bars
        for (int cell = 0; cell < 4; cell++) {
            int cz = -12 + cell * 4;
            b.fillBox(25, 0, cz, 30, 6, cz + 2, Material.IRON_BLOCK);
            b.fillBox(26, 1, cz, 29, 5, cz + 2, Material.AIR);
            for (int y = 1; y <= 5; y++) b.setBlock(25, y, cz + 1, Material.IRON_BARS);
            // Cot and bucket
            b.setBlock(28, 1, cz, Material.WHITE_BED);
            b.setBlock(27, 1, cz + 2, Material.BARREL);
            // Cell light
            b.setBlock(27, 5, cz + 1, Material.REDSTONE_LAMP);
        }
        // Guard desk
        b.table(33, 1, -10);
        b.chair(34, 1, -10, Material.POLISHED_ANDESITE_STAIRS, BlockFace.WEST);
        b.setBlock(33, 2, -10, Material.LODESTONE);
        b.setBlock(36, 1, -13, Material.JUKEBOX);
        b.setBlock(36, 1, 3, Material.BARREL);

        // ── Research lab (southeast) ────────────────────────────────────
        b.fillBox(20, 0, 8, 38, 10, 19, Material.GRAY_CONCRETE);
        b.fillBox(21, 1, 9, 37, 9, 18, Material.AIR);
        b.fillFloor(21, 9, 37, 18, 0, Material.WHITE_CONCRETE);
        // Lab stations
        b.setBlock(25, 1, 14, Material.BREWING_STAND);
        b.setBlock(27, 1, 14, Material.CAULDRON);
        b.setBlock(29, 1, 14, Material.ENCHANTING_TABLE);
        b.setBlock(31, 1, 14, Material.SMITHING_TABLE);
        b.setBlock(33, 1, 14, Material.GRINDSTONE);
        b.setBlock(35, 1, 14, Material.LECTERN);
        // Sample containment (glass cylinders)
        for (int x = 25; x <= 35; x += 5) {
            b.pillar(x, 1, 5, 17, Material.LIGHT_BLUE_STAINED_GLASS);
            b.setBlock(x, 1, 17, Material.IRON_BLOCK);
            b.setBlock(x, 6, 17, Material.IRON_BLOCK);
        }
        // Medbay cot
        b.setBlock(22, 1, 10, Material.WHITE_BED);
        b.setBlock(22, 1, 12, Material.WHITE_BED);
        // Lab ceiling lights
        b.wallLighting(21, 8, 9, 37, 9, 4, Material.SEA_LANTERN);

        // ── Armory (north-west connector) ───────────────────────────────
        b.fillBox(-18, 0, -18, -12, 7, -12, Material.GRAY_CONCRETE);
        b.fillBox(-17, 1, -17, -13, 6, -13, Material.AIR);
        // Weapon racks
        for (int z = -16; z <= -14; z++) {
            b.setBlock(-17, 1, z, Material.OAK_FENCE);
            b.setBlock(-17, 2, z, Material.OAK_TRAPDOOR);
        }
        for (int x = -16; x <= -14; x++) {
            b.setBlock(x, 1, -17, Material.OAK_FENCE);
            b.setBlock(x, 2, -17, Material.OAK_TRAPDOOR);
        }
        // TNT powder stash (decor)
        b.setBlock(-13, 1, -13, Material.TNT);
        b.setBlock(-13, 1, -14, Material.TNT);
        b.setBlock(-14, 1, -13, Material.TNT);
        b.setBlock(-15, 5, -15, Material.LANTERN);

        // ── Teleporter pad (NE connector) ───────────────────────────────
        b.fillBox(12, 0, -18, 18, 7, -12, Material.GRAY_CONCRETE);
        b.fillBox(13, 1, -17, 17, 6, -13, Material.AIR);
        b.setBlock(15, 1, -15, Material.RESPAWN_ANCHOR);
        for (int[] pad : new int[][]{{14, -14}, {16, -14}, {14, -16}, {16, -16}}) {
            b.setBlock(pad[0], 1, pad[1], Material.END_STONE_BRICKS);
            b.setBlock(pad[0], 2, pad[1], Material.END_ROD);
        }
        b.setBlock(15, 6, -15, Material.BEACON);

        // ── Power core room (underground center) ────────────────────────
        b.fillBox(-8, -10, -8, 8, -1, 8, Material.IRON_BLOCK);
        b.fillBox(-7, -9, -7, 7, -1, 7, Material.AIR);
        b.fillFloor(-7, -7, 7, 7, -10, Material.GRAY_CONCRETE);
        // Power core (sea lanterns surrounded by glass)
        b.fillBox(-2, -9, -2, 2, -3, 2, Material.SEA_LANTERN);
        b.fillBox(-3, -9, -3, 3, -2, 3, Material.LIGHT_BLUE_STAINED_GLASS);
        b.fillBox(-2, -8, -2, 2, -3, 2, Material.SEA_LANTERN);
        // Core supports
        for (int[] sup : new int[][]{{-5, -5}, {5, -5}, {-5, 5}, {5, 5}}) {
            b.pillar(sup[0], -9, 8, sup[1], Material.POLISHED_DEEPSLATE);
            b.setBlock(sup[0], -1, sup[1], Material.END_ROD);
        }
        // Ladder down from command
        for (int y = -9; y <= 0; y++) b.setBlock(0, y, 6, Material.LADDER);

        // ── Automated defenses (dispenser turrets) ──────────────────────
        for (int x = -36; x <= 36; x += 12) {
            b.setDirectional(x, 8, -20, Material.DISPENSER, BlockFace.SOUTH);
            b.setDirectional(x, 8, 20, Material.DISPENSER, BlockFace.NORTH);
            b.setBlock(x, 9, -20, Material.REDSTONE_LAMP);
            b.setBlock(x, 9, 20, Material.REDSTONE_LAMP);
        }

        // ── Ceiling ─────────────────────────────────────────────────────
        b.fillFloor(-39, -19, 39, 19, 16, Material.GRAY_CONCRETE);

        // ── Main entrance (south, large blast door) ─────────────────────
        b.fillBox(-4, 1, -20, 4, 10, -20, Material.AIR);
        b.setBlock(-5, 11, -20, Material.DEEPSLATE_BRICKS);
        b.setBlock(5, 11, -20, Material.DEEPSLATE_BRICKS);
        for (int x = -4; x <= 4; x++) b.setBlock(x, 11, -20, Material.IRON_BLOCK);

        // ── Exterior floodlights ────────────────────────────────────────
        for (int[] fl : new int[][]{{-40, -10}, {-40, 10}, {40, -10}, {40, 10}, {-20, -20}, {20, -20}, {-20, 20}, {20, 20}}) {
            b.setBlock(fl[0], 15, fl[1], Material.REDSTONE_LAMP);
        }

        // ── Glass windows throughout ────────────────────────────────────
        for (int x = -35; x <= 35; x += 5) {
            for (int y = 6; y <= 12; y++) {
                b.setBlock(x, y, -20, Material.GRAY_STAINED_GLASS_PANE);
                b.setBlock(x, y, 20, Material.GRAY_STAINED_GLASS_PANE);
            }
        }

        // ── Loot ────────────────────────────────────────────────────────
        b.placeChest(0, 1, 15);      // command
        b.placeChest(-30, 1, 0);     // hangar
        b.placeChest(30, 1, -10);    // prison
        b.placeChest(30, 1, 14);     // lab
        b.placeChest(-15, 1, -15);   // armory
        b.placeChest(15, 1, -15);    // teleporter
        b.placeChest(0, -7, 0);      // power core
        b.setBossSpawn(0, 1, 12);
    }
}

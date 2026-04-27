package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

/**
 * InvaderlingOutpostBuilder — hardened military base with concrete bunker, multi-room
 * layout (barracks, command deck, armory, comms, mess), guard towers, floodlights,
 * fuel drums, antenna array, perimeter blast walls, searchlight, vehicle bay.
 */
public final class InvaderlingOutpostBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // ── Perimeter blast wall ────────────────────────────────────────
        b.fillWalls(-13, 0, -13, 13, 2, 13, Material.GRAY_CONCRETE);
        b.hollowBox(-13, 0, -13, 13, 2, 13);
        // Razor-wire top
        for (int x = -13; x <= 13; x += 2) {
            b.setBlock(x, 3, -13, Material.IRON_BARS);
            b.setBlock(x, 3, 13, Material.IRON_BARS);
        }
        for (int z = -13; z <= 13; z += 2) {
            b.setBlock(-13, 3, z, Material.IRON_BARS);
            b.setBlock(13, 3, z, Material.IRON_BARS);
        }
        // Perimeter gate (south)
        b.fillBox(-1, 1, -13, 1, 2, -13, Material.AIR);
        b.setBlock(-2, 3, -13, Material.DEEPSLATE_BRICKS);
        b.setBlock(2, 3, -13, Material.DEEPSLATE_BRICKS);

        // ── Main bunker foundation ──────────────────────────────────────
        b.fillBox(-9, 0, -9, 9, 0, 9, Material.GRAY_CONCRETE);
        b.scatter(-9, 0, -9, 9, 0, 9, Material.GRAY_CONCRETE, Material.LIGHT_GRAY_CONCRETE, 0.2);

        // Bunker walls (thick concrete with deepslate base course)
        b.fillWalls(-9, 0, -9, 9, 1, 9, Material.DEEPSLATE_BRICKS);
        b.fillWalls(-9, 2, -9, 9, 6, 9, Material.GRAY_CONCRETE);
        b.hollowBox(-9, 1, -9, 9, 6, 9);
        // Corner reinforcements
        for (int[] c : new int[][]{{-9, -9}, {9, -9}, {-9, 9}, {9, 9}}) {
            b.pillar(c[0], 0, 7, c[1], Material.POLISHED_DEEPSLATE);
        }

        // Reinforced glass slit windows
        for (int x = -6; x <= 6; x += 4) {
            b.setBlock(x, 3, -9, Material.GRAY_STAINED_GLASS);
            b.setBlock(x, 4, -9, Material.GRAY_STAINED_GLASS);
            b.setBlock(x, 3, 9, Material.GRAY_STAINED_GLASS);
            b.setBlock(x, 4, 9, Material.GRAY_STAINED_GLASS);
            b.setBlock(-9, 3, x, Material.GRAY_STAINED_GLASS);
            b.setBlock(-9, 4, x, Material.GRAY_STAINED_GLASS);
            b.setBlock(9, 3, x, Material.GRAY_STAINED_GLASS);
            b.setBlock(9, 4, x, Material.GRAY_STAINED_GLASS);
        }

        // Interior floor with caution stripes
        b.fillFloor(-8, -8, 8, 8, 0, Material.LIGHT_GRAY_CONCRETE);
        for (int x = -8; x <= 8; x += 4) {
            b.setBlock(x, 0, 0, Material.YELLOW_CONCRETE);
            b.setBlock(x, 0, 1, Material.BLACK_CONCRETE);
        }

        // Ceiling
        b.fillFloor(-8, -8, 8, 8, 6, Material.GRAY_CONCRETE);
        // Ceiling lights
        for (int x = -6; x <= 6; x += 3)
            for (int z = -6; z <= 6; z += 3)
                b.setBlock(x, 6, z, Material.REDSTONE_LAMP);

        // ── Barracks (south half) ───────────────────────────────────────
        b.fillBox(-8, 0, 2, -2, 5, 8, Material.GRAY_CONCRETE);
        b.fillBox(-7, 1, 3, -3, 4, 7, Material.AIR);
        // 4 bunks (stacked)
        b.setBlock(-6, 1, 5, Material.WHITE_BED);
        b.setBlock(-4, 1, 5, Material.WHITE_BED);
        b.setBlock(-6, 3, 5, Material.WHITE_BED);
        b.setBlock(-4, 3, 5, Material.WHITE_BED);
        // Lockers (barrels)
        b.setBlock(-7, 1, 3, Material.BARREL);
        b.setBlock(-7, 2, 3, Material.BARREL);
        b.setBlock(-7, 1, 7, Material.BARREL);
        b.setBlock(-7, 2, 7, Material.BARREL);
        // Ceiling fixture
        b.setBlock(-5, 4, 5, Material.REDSTONE_LAMP);
        b.setBlock(-5, 4, 3, Material.LANTERN);

        // ── Command room (north-west) ───────────────────────────────────
        b.fillBox(-8, 0, -8, 0, 5, -2, Material.GRAY_CONCRETE);
        b.fillBox(-7, 1, -7, -1, 4, -3, Material.AIR);
        // Command table with map
        b.table(-4, 1, -5);
        b.setBlock(-4, 2, -5, Material.BLUE_TERRACOTTA);
        b.setBlock(-3, 2, -5, Material.BLUE_TERRACOTTA);
        b.setBlock(-5, 2, -5, Material.BLUE_TERRACOTTA);
        b.chair(-3, 1, -5, Material.POLISHED_ANDESITE_STAIRS, BlockFace.WEST);
        b.chair(-5, 1, -5, Material.POLISHED_ANDESITE_STAIRS, BlockFace.EAST);
        // Control panel
        for (int x = -6; x <= -2; x++) {
            b.setBlock(x, 2, -7, Material.REDSTONE_LAMP);
            b.setBlock(x, 3, -7, Material.LEVER);
        }
        // Lodestone (nav)
        b.setBlock(-7, 1, -7, Material.LODESTONE);

        // ── Comms station (north-east) ──────────────────────────────────
        b.fillBox(0, 0, -8, 8, 5, -2, Material.GRAY_CONCRETE);
        b.fillBox(1, 1, -7, 7, 4, -3, Material.AIR);
        b.setBlock(6, 1, -7, Material.JUKEBOX);
        b.setBlock(5, 1, -7, Material.NOTE_BLOCK);
        b.setBlock(4, 1, -7, Material.NOTE_BLOCK);
        b.setBlock(6, 2, -7, Material.COMPARATOR);
        // Radio rack
        for (int y = 1; y <= 3; y++) {
            b.setBlock(7, y, -5, Material.OBSERVER);
        }
        b.setBlock(3, 1, -5, Material.REPEATER);
        b.setBlock(4, 4, -5, Material.REDSTONE_LAMP);

        // ── Armory / supply storage (east) ──────────────────────────────
        b.fillBox(3, 0, -1, 8, 5, 0, Material.GRAY_CONCRETE);
        b.fillBox(3, 0, 2, 8, 5, 8, Material.GRAY_CONCRETE);
        b.fillBox(4, 1, 3, 7, 4, 7, Material.AIR);
        // Weapon rack
        for (int z = 3; z <= 7; z++) {
            b.setBlock(4, 1, z, Material.OAK_FENCE);
            b.setBlock(4, 2, z, Material.OAK_TRAPDOOR);
        }
        // Supply barrels
        b.setBlock(7, 1, 3, Material.BARREL);
        b.setBlock(7, 1, 5, Material.BARREL);
        b.setBlock(7, 1, 7, Material.BARREL);
        b.setBlock(7, 2, 4, Material.BARREL);
        b.setBlock(7, 2, 6, Material.BARREL);
        // Powder kegs (TNT — dormant, decorative)
        b.setBlock(6, 1, 6, Material.TNT);
        b.setBlock(6, 1, 4, Material.TNT);
        b.setBlock(5, 4, 5, Material.LANTERN);

        // Entrance corridor (south wall)
        b.setBlock(0, 1, -9, Material.AIR);
        b.setBlock(0, 2, -9, Material.AIR);
        b.setBlock(1, 1, -9, Material.AIR);
        b.setBlock(1, 2, -9, Material.AIR);
        // Blast door frame
        b.setBlock(-1, 3, -9, Material.DEEPSLATE_BRICKS);
        b.setBlock(2, 3, -9, Material.DEEPSLATE_BRICKS);
        b.setBlock(-1, 4, -9, Material.DEEPSLATE_BRICKS);
        b.setBlock(2, 4, -9, Material.DEEPSLATE_BRICKS);

        // ── Guard towers (NE & SW corners outside) ──────────────────────
        for (int[] tw : new int[][]{{11, -11}, {-11, 11}}) {
            b.pillar(tw[0], 1, 8, tw[1], Material.DEEPSLATE_BRICKS);
            b.fillBox(tw[0] - 1, 8, tw[1] - 1, tw[0] + 1, 8, tw[1] + 1, Material.GRAY_CONCRETE);
            b.fillWalls(tw[0] - 1, 9, tw[1] - 1, tw[0] + 1, 10, tw[1] + 1, Material.IRON_BARS);
            b.pyramidRoof(tw[0] - 1, tw[1] - 1, tw[0] + 1, tw[1] + 1, 11, Material.DEEPSLATE_TILES);
            b.setBlock(tw[0], 9, tw[1], Material.REDSTONE_LAMP);
            for (int y = 1; y <= 8; y++) b.setBlock(tw[0], y, tw[1] + (tw[1] < 0 ? 1 : -1), Material.LADDER);
        }

        // ── Antenna array on main roof ──────────────────────────────────
        b.pillar(8, 7, 14, 8, Material.IRON_BARS);
        b.setBlock(8, 15, 8, Material.LIGHTNING_ROD);
        b.setBlock(8, 14, 8, Material.END_ROD);
        // Secondary antenna
        b.pillar(-8, 7, 11, -8, Material.IRON_BARS);
        b.setBlock(-8, 12, -8, Material.LIGHTNING_ROD);
        // Cross-struts
        for (int h : new int[]{10, 12}) {
            b.setBlock(6, h, 8, Material.IRON_BARS);
            b.setBlock(10, h, 8, Material.IRON_BARS);
            b.setBlock(8, h, 6, Material.IRON_BARS);
            b.setBlock(8, h, 10, Material.IRON_BARS);
        }

        // Radar dish on the roof
        b.setBlock(0, 7, 0, Material.DAYLIGHT_DETECTOR);
        b.setBlock(0, 8, 0, Material.IRON_BLOCK);
        b.setBlock(1, 7, 0, Material.IRON_TRAPDOOR);
        b.setBlock(-1, 7, 0, Material.IRON_TRAPDOOR);
        b.setBlock(0, 7, 1, Material.IRON_TRAPDOOR);
        b.setBlock(0, 7, -1, Material.IRON_TRAPDOOR);

        // ── Exterior: fuel drums ────────────────────────────────────────
        for (int[] d : new int[][]{{-12, -5}, {-12, 5}, {12, -5}, {12, 5}}) {
            b.pillar(d[0], 1, 2, d[1], Material.BLACK_CONCRETE);
            b.setBlock(d[0], 3, d[1], Material.IRON_BLOCK);
        }

        // Floodlights on perimeter wall
        for (int[] fl : new int[][]{{-13, -6}, {-13, 6}, {13, -6}, {13, 6}, {-6, -13}, {6, -13}, {-6, 13}, {6, 13}}) {
            b.setBlock(fl[0], 3, fl[1], Material.REDSTONE_LAMP);
        }

        // Vehicle bay (open slab on north exterior)
        b.fillBox(-4, 0, 10, 4, 0, 12, Material.COBBLESTONE);
        b.fillBox(-4, 1, 10, 4, 1, 10, Material.IRON_BLOCK);  // ramp marker
        b.setBlock(-5, 1, 11, Material.IRON_BARS);
        b.setBlock(5, 1, 11, Material.IRON_BARS);

        // Sandbags along south approach
        for (int x = -4; x <= 4; x += 2) {
            b.setBlock(x, 1, -12, Material.MUD);
            b.setBlock(x, 1, -11, Material.MUD);
        }

        // ── Weathering ──────────────────────────────────────────────────
        b.scatter(-13, 0, -13, 13, 6, 13, Material.GRAY_CONCRETE, Material.CRACKED_DEEPSLATE_BRICKS, 0.04);

        // ── Loot ────────────────────────────────────────────────────────
        b.placeChest(6, 1, -3);   // comms
        b.placeChest(-6, 1, 3);   // barracks
        b.placeChest(-4, 1, -5);  // command
        b.placeChest(6, 1, 5);    // armory
        b.placeChest(-11, 8, 11); // tower
        b.setBossSpawn(0, 1, 0);
    }
}

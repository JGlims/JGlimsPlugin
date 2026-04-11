package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

/**
 * DinobotArenaBuilder — a mechanized factory amphitheater with iron bulwark
 * walls, eight Tesla-coil lightning pylons, redstone circuit rings, a hexagonal
 * command altar, four blackstone pillboxes, an observation deck, and a hazard
 * launch pad.
 *
 * <p>Extracted verbatim from the original {@code DimensionStructureBuilders.buildDinobotArena}
 * method to give the structure its own file per the one-class-per-structure
 * architecture.
 */
public final class DinobotArenaBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // ─── Dinobot Arena: a colossal mechanized factory amphitheater ───
        // Industrial palette: iron blocks, polished blackstone, redstone lamps,
        // copper accents, lightning conduits, hazard stripes.

        // Foundation slab (4-block deep iron+blackstone substrate)
        for (int y = -3; y <= 0; y++) b.filledCircle(0, y, 0, 28, Material.POLISHED_BLACKSTONE);
        b.filledCircle(0, 0, 0, 26, Material.IRON_BLOCK);
        b.filledCircle(0, 0, 0, 24, Material.SMOOTH_STONE);

        // Hexagonal hazard pattern across the arena floor
        for (int x = -22; x <= 22; x++)
            for (int z = -22; z <= 22; z++) {
                if (x * x + z * z > 484) continue;
                int hexId = ((x + 100) / 4 + (z + 100) / 4) % 3;
                if (hexId == 0 && (x + z) % 6 == 0) b.setBlock(x, 0, z, Material.YELLOW_TERRACOTTA);
                else if (hexId == 1 && (x + z) % 5 == 0) b.setBlock(x, 0, z, Material.BLACK_CONCRETE);
            }

        // ─── Electrified circuit rings (redstone trails along the floor) ───
        for (int a = 0; a < 360; a += 1) {
            int rx = (int) Math.round(8 * Math.cos(Math.toRadians(a)));
            int rz = (int) Math.round(8 * Math.sin(Math.toRadians(a)));
            b.setBlock(rx, 0, rz, Material.REDSTONE_LAMP);
            int rx2 = (int) Math.round(16 * Math.cos(Math.toRadians(a)));
            int rz2 = (int) Math.round(16 * Math.sin(Math.toRadians(a)));
            b.setBlock(rx2, 0, rz2, Material.REDSTONE_LAMP);
        }
        // Spoke circuits connecting the rings (8 radial conduit lines)
        for (int i = 0; i < 8; i++) {
            int a = i * 45;
            for (int d = 9; d <= 15; d++) {
                int rx = (int) Math.round(d * Math.cos(Math.toRadians(a)));
                int rz = (int) Math.round(d * Math.sin(Math.toRadians(a)));
                b.setBlock(rx, 0, rz, Material.WAXED_COPPER_BLOCK);
            }
        }

        // ─── Outer wall: stepped industrial bulwark (3 tiers, 14 high total) ───
        for (int yy = 1; yy <= 4; yy++) b.circle(0, yy, 0, 26, Material.IRON_BLOCK);
        for (int yy = 5; yy <= 9; yy++) b.circle(0, yy, 0, 25, Material.POLISHED_BLACKSTONE);
        for (int yy = 10; yy <= 14; yy++) b.circle(0, yy, 0, 24, Material.IRON_BARS);
        // Battlements on top
        for (int a = 0; a < 360; a += 8) {
            int wx = (int) Math.round(24 * Math.cos(Math.toRadians(a)));
            int wz = (int) Math.round(24 * Math.sin(Math.toRadians(a)));
            b.setBlock(wx, 15, wz, Material.IRON_BLOCK);
            if (a % 16 == 0) b.setBlock(wx, 16, wz, Material.LIGHTNING_ROD);
        }

        // ─── 8 Lightning Pylons (massive Tesla coils around perimeter) ───
        for (int i = 0; i < 8; i++) {
            int a = i * 45;
            int px = (int) Math.round(21 * Math.cos(Math.toRadians(a)));
            int pz = (int) Math.round(21 * Math.sin(Math.toRadians(a)));
            // Pylon base (3x3 with copper accents)
            b.fillBox(px - 1, 1, pz - 1, px + 1, 4, pz + 1, Material.IRON_BLOCK);
            b.setBlock(px - 1, 1, pz - 1, Material.WAXED_COPPER_BLOCK);
            b.setBlock(px + 1, 1, pz - 1, Material.WAXED_COPPER_BLOCK);
            b.setBlock(px - 1, 1, pz + 1, Material.WAXED_COPPER_BLOCK);
            b.setBlock(px + 1, 1, pz + 1, Material.WAXED_COPPER_BLOCK);
            // Tapering shaft
            b.pillar(px, 5, 14, pz, Material.IRON_BLOCK);
            b.pillar(px, 15, 18, pz, Material.LIGHTNING_ROD);
            // Coil rings (copper bulbs at intervals)
            b.setBlock(px - 1, 8, pz, Material.WAXED_COPPER_BLOCK);
            b.setBlock(px + 1, 8, pz, Material.WAXED_COPPER_BLOCK);
            b.setBlock(px, 8, pz - 1, Material.WAXED_COPPER_BLOCK);
            b.setBlock(px, 8, pz + 1, Material.WAXED_COPPER_BLOCK);
            b.setBlock(px - 1, 12, pz, Material.WAXED_COPPER_BLOCK);
            b.setBlock(px + 1, 12, pz, Material.WAXED_COPPER_BLOCK);
            // Power node at base (redstone block) with cable to ring
            b.setBlock(px, 0, pz, Material.REDSTONE_BLOCK);
            // Glowing crown
            b.setBlock(px, 19, pz, Material.SEA_LANTERN);
        }

        // ─── Central command altar: raised hexagonal platform with control console ───
        b.filledCircle(0, 1, 0, 5, Material.IRON_BLOCK);
        b.filledCircle(0, 2, 0, 4, Material.POLISHED_BLACKSTONE);
        b.filledCircle(0, 3, 0, 3, Material.WAXED_COPPER_BLOCK);
        // Central reactor core
        b.setBlock(0, 4, 0, Material.RESPAWN_ANCHOR);
        b.setBlock(-1, 4, 0, Material.SEA_LANTERN);
        b.setBlock(1, 4, 0, Material.SEA_LANTERN);
        b.setBlock(0, 4, -1, Material.SEA_LANTERN);
        b.setBlock(0, 4, 1, Material.SEA_LANTERN);
        // Console pillars
        for (int i = 0; i < 6; i++) {
            int a = i * 60;
            int cx = (int) Math.round(4 * Math.cos(Math.toRadians(a)));
            int cz = (int) Math.round(4 * Math.sin(Math.toRadians(a)));
            b.setBlock(cx, 4, cz, Material.LEVER);
        }

        // ─── 4 Combat obstacles: hardened iron pillboxes around the arena ───
        for (int[] pos : new int[][]{{-12, -12}, {12, -12}, {-12, 12}, {12, 12}}) {
            // Bunker base
            b.fillBox(pos[0] - 3, 1, pos[1] - 3, pos[0] + 3, 5, pos[1] + 3, Material.POLISHED_BLACKSTONE);
            b.fillBox(pos[0] - 2, 1, pos[1] - 2, pos[0] + 2, 4, pos[1] + 2, Material.AIR);
            // Slit windows
            b.setBlock(pos[0], 3, pos[1] - 3, Material.AIR);
            b.setBlock(pos[0], 3, pos[1] + 3, Material.AIR);
            b.setBlock(pos[0] - 3, 3, pos[1], Material.AIR);
            b.setBlock(pos[0] + 3, 3, pos[1], Material.AIR);
            // Top searchlight
            b.setBlock(pos[0], 6, pos[1], Material.LIGHTNING_ROD);
            b.setBlock(pos[0], 5, pos[1], Material.SEA_LANTERN);
            // Loot inside
            b.placeChest(pos[0], 1, pos[1]);
        }

        // ─── Observation deck: elevated command center on the south wall ───
        b.fillBox(-12, 10, -26, 12, 13, -23, Material.POLISHED_BLACKSTONE);
        b.fillBox(-11, 11, -25, 11, 12, -24, Material.AIR);
        // Glass viewport
        for (int x = -10; x <= 10; x++) b.setBlock(x, 12, -24, Material.GRAY_STAINED_GLASS_PANE);
        // Banks of monitors (note blocks as displays)
        for (int x = -8; x <= 8; x += 2) {
            b.setBlock(x, 11, -25, Material.NOTE_BLOCK);
            b.setBlock(x, 12, -25, Material.SEA_LANTERN);
        }
        // Stair access from arena floor
        for (int s = 0; s < 10; s++) {
            b.setBlock(0, s + 1, -22 - s, Material.POLISHED_BLACKSTONE_STAIRS);
            b.setBlock(-1, s + 1, -22 - s, Material.POLISHED_BLACKSTONE);
            b.setBlock(1, s + 1, -22 - s, Material.POLISHED_BLACKSTONE);
        }
        b.placeChest(-8, 11, -25);
        b.placeChest(8, 11, -25);

        // ─── Launch pad: heavy mechanical doors / hazard zone (north) ───
        b.fillBox(-10, 0, 22, 10, 0, 30, Material.POLISHED_BLACKSTONE);
        b.fillBox(-7, 0, 24, 7, 0, 28, Material.YELLOW_CONCRETE);
        // Warning chevrons
        for (int x = -8; x <= 8; x += 2) {
            b.setBlock(x, 0, 22, Material.BLACK_CONCRETE);
            b.setBlock(x, 0, 30, Material.BLACK_CONCRETE);
        }
        for (int z = 22; z <= 30; z += 2) {
            b.setBlock(-9, 0, z, Material.BLACK_CONCRETE);
            b.setBlock(9, 0, z, Material.BLACK_CONCRETE);
        }
        // Launch tower
        b.pillar(-9, 1, 16, 22, Material.IRON_BLOCK);
        b.pillar(9, 1, 16, 22, Material.IRON_BLOCK);
        b.setBlock(-9, 17, 22, Material.LIGHTNING_ROD);
        b.setBlock(9, 17, 22, Material.LIGHTNING_ROD);

        // Suspended ceiling lights (sea lanterns at high points)
        for (int i = 0; i < 12; i++) {
            int a = i * 30;
            int sx = (int) Math.round(18 * Math.cos(Math.toRadians(a)));
            int sz = (int) Math.round(18 * Math.sin(Math.toRadians(a)));
            b.setBlock(sx, 16, sz, Material.SEA_LANTERN);
        }
        b.chandelier(0, 18, 0, 5);

        // Central treasure
        b.placeChest(0, 1, 0);
        b.setBossSpawn(0, 4, 0);
    }
}

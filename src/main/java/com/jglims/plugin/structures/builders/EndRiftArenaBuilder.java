package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

/**
 * EndRiftArenaBuilder — a torn-reality obsidian plaza where the End Rift Dragon
 * emerges, ringed with crying obsidian rift veins, four colossal end-gateway
 * crystal pillars, spectator corner platforms, and a grand entry portal.
 *
 * <p>Extracted verbatim from the original {@code DimensionStructureBuilders.buildEndRiftArena}
 * method to give the structure its own file per the one-class-per-structure
 * architecture.
 */
public final class EndRiftArenaBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // ─── End Rift Arena: a torn-reality plaza where the End Rift Dragon emerges ───
        // Black obsidian arena ringed with crying obsidian "rift cracks", 4 colossal
        // crystal pillars holding end gateways, suspended spectator platforms.

        // Arena floor: layered stonework with rift veins
        b.filledCircle(0, 0, 0, 28, Material.OBSIDIAN);
        b.filledCircle(0, 0, 0, 26, Material.END_STONE_BRICKS);
        b.filledCircle(0, 0, 0, 24, Material.END_STONE);
        // Crying obsidian "rift veins" radiating from center
        for (int i = 0; i < 8; i++) {
            int a = i * 45;
            for (int d = 1; d <= 24; d++) {
                int rx = (int) Math.round(d * Math.cos(Math.toRadians(a)));
                int rz = (int) Math.round(d * Math.sin(Math.toRadians(a)));
                if (d % 2 == 0) b.setBlock(rx, 0, rz, Material.CRYING_OBSIDIAN);
                else b.setBlock(rx, 0, rz, Material.OBSIDIAN);
            }
        }
        // Concentric rune circles
        for (int a = 0; a < 360; a += 1) {
            int rx = (int) Math.round(8 * Math.cos(Math.toRadians(a)));
            int rz = (int) Math.round(8 * Math.sin(Math.toRadians(a)));
            b.setBlock(rx, 0, rz, Material.CRYING_OBSIDIAN);
            int rx2 = (int) Math.round(16 * Math.cos(Math.toRadians(a)));
            int rz2 = (int) Math.round(16 * Math.sin(Math.toRadians(a)));
            b.setBlock(rx2, 0, rz2, Material.CRYING_OBSIDIAN);
        }

        // ─── Outer barrier ring: massive obsidian wall with embedded gateways ───
        for (int yy = 1; yy <= 6; yy++) b.circle(0, yy, 0, 28, Material.OBSIDIAN);
        // Battlements with end rod beacons
        for (int a = 0; a < 360; a += 8) {
            int wx = (int) Math.round(28 * Math.cos(Math.toRadians(a)));
            int wz = (int) Math.round(28 * Math.sin(Math.toRadians(a)));
            b.setBlock(wx, 7, wz, Material.OBSIDIAN);
            if (a % 16 == 0) b.setBlock(wx, 8, wz, Material.END_ROD);
        }

        // ─── 4 Colossal end crystal pillars (cardinal directions, 24 high) ───
        int[][] crystalPillars = {{0, -22}, {0, 22}, {-22, 0}, {22, 0}};
        for (int[] cp : crystalPillars) {
            // Tapered base
            b.fillBox(cp[0] - 3, 0, cp[1] - 3, cp[0] + 3, 4, cp[1] + 3, Material.OBSIDIAN);
            // Main shaft
            b.fillBox(cp[0] - 2, 5, cp[1] - 2, cp[0] + 2, 18, cp[1] + 2, Material.OBSIDIAN);
            // Crying obsidian veins running up the shaft
            for (int yy = 5; yy <= 18; yy++) {
                if (yy % 3 == 0) {
                    b.setBlock(cp[0] - 2, yy, cp[1], Material.CRYING_OBSIDIAN);
                    b.setBlock(cp[0] + 2, yy, cp[1], Material.CRYING_OBSIDIAN);
                    b.setBlock(cp[0], yy, cp[1] - 2, Material.CRYING_OBSIDIAN);
                    b.setBlock(cp[0], yy, cp[1] + 2, Material.CRYING_OBSIDIAN);
                }
            }
            // End crystal cradle on top (iron bars cage)
            b.fillBox(cp[0] - 1, 19, cp[1] - 1, cp[0] + 1, 22, cp[1] + 1, Material.IRON_BARS);
            b.fillBox(cp[0], 20, cp[1], cp[0], 21, cp[1], Material.AIR);
            // End gateway block at the top — this is the "rift" where the dragon comes through
            b.setBlock(cp[0], 20, cp[1], Material.END_GATEWAY);
            // Glowing end rods around the cage
            b.setBlock(cp[0] - 2, 22, cp[1], Material.END_ROD);
            b.setBlock(cp[0] + 2, 22, cp[1], Material.END_ROD);
            b.setBlock(cp[0], 22, cp[1] - 2, Material.END_ROD);
            b.setBlock(cp[0], 22, cp[1] + 2, Material.END_ROD);
            b.setBlock(cp[0], 23, cp[1], Material.END_ROD);
        }

        // ─── 8 cover pillars at varying heights for tactical use ───
        for (int i = 0; i < 8; i++) {
            int a = i * 45 + 22;
            int px = (int) Math.round(13 * Math.cos(Math.toRadians(a)));
            int pz = (int) Math.round(13 * Math.sin(Math.toRadians(a)));
            int h = 3 + (i % 3) * 2;
            b.pillar(px, 1, h, pz, Material.OBSIDIAN);
            b.setBlock(px, h + 1, pz, Material.CRYING_OBSIDIAN);
        }

        // ─── Central altar: stepped obsidian dais where the rift opens ───
        b.filledCircle(0, 1, 0, 4, Material.OBSIDIAN);
        b.filledCircle(0, 2, 0, 3, Material.CRYING_OBSIDIAN);
        b.filledCircle(0, 3, 0, 2, Material.OBSIDIAN);
        b.setBlock(0, 4, 0, Material.END_GATEWAY);
        b.setBlock(0, 5, 0, Material.END_ROD);
        // Dragon head decorations on the altar corners
        b.setBlock(-3, 2, 0, Material.DRAGON_HEAD);
        b.setBlock(3, 2, 0, Material.DRAGON_HEAD);
        b.setBlock(0, 2, -3, Material.DRAGON_HEAD);
        b.setBlock(0, 2, 3, Material.DRAGON_HEAD);

        // ─── Grand entry portal (south) — gateway tunnel through the wall ───
        b.fillBox(-4, 1, -29, 4, 7, -27, Material.AIR);
        b.gothicArch(0, 1, -28, 8, 7, Material.PURPUR_PILLAR);
        // Approach causeway
        for (int z = -32; z <= -29; z++) {
            b.fillBox(-3, 0, z, 3, 0, z, Material.PURPUR_BLOCK);
            b.setBlock(-4, 0, z, Material.PURPUR_PILLAR);
            b.setBlock(4, 0, z, Material.PURPUR_PILLAR);
        }
        b.setBlock(-4, 1, -32, Material.END_ROD);
        b.setBlock(4, 1, -32, Material.END_ROD);

        // ─── 4 elevated spectator platforms (corners) ───
        int[][] specPlats = {{-23, -23}, {23, -23}, {-23, 23}, {23, 23}};
        for (int[] sp : specPlats) {
            // Support pillar
            b.pillar(sp[0], 1, 8, sp[1], Material.OBSIDIAN);
            // Platform top
            b.fillBox(sp[0] - 3, 9, sp[1] - 3, sp[0] + 3, 9, sp[1] + 3, Material.PURPUR_BLOCK);
            // Battlements around platform
            b.battlements(sp[0] - 3, 10, sp[1] - 3, sp[0] + 3, sp[1] - 3, Material.PURPUR_PILLAR);
            b.battlements(sp[0] - 3, 10, sp[1] + 3, sp[0] + 3, sp[1] + 3, Material.PURPUR_PILLAR);
            b.battlements(sp[0] - 3, 10, sp[1] - 3, sp[0] - 3, sp[1] + 3, Material.PURPUR_PILLAR);
            b.battlements(sp[0] + 3, 10, sp[1] - 3, sp[0] + 3, sp[1] + 3, Material.PURPUR_PILLAR);
            // Center end rod
            b.setBlock(sp[0], 11, sp[1], Material.END_ROD);
            // Loot chest
            b.placeChest(sp[0], 10, sp[1]);
        }

        // ─── Floating obsidian "rift fragments" suspended above the arena ───
        for (int i = 0; i < 6; i++) {
            int a = i * 60 + 30;
            int fx = (int) Math.round(10 * Math.cos(Math.toRadians(a)));
            int fz = (int) Math.round(10 * Math.sin(Math.toRadians(a)));
            int fy = 14 + i % 3;
            b.setBlock(fx, fy, fz, Material.CRYING_OBSIDIAN);
            b.setBlock(fx + 1, fy, fz, Material.OBSIDIAN);
            b.setBlock(fx, fy, fz + 1, Material.OBSIDIAN);
        }

        // Sky end-rod constellation overhead
        for (int i = 0; i < 12; i++) {
            int a = i * 30;
            int sx = (int) Math.round(20 * Math.cos(Math.toRadians(a)));
            int sz = (int) Math.round(20 * Math.sin(Math.toRadians(a)));
            b.setBlock(sx, 22, sz, Material.END_ROD);
        }

        // Central treasure
        b.placeChest(0, 1, 4);
        b.placeChest(0, 1, -4);
        b.placeChest(4, 1, 0);
        b.placeChest(-4, 1, 0);

        b.setBossSpawn(0, 1, 0);
    }
}

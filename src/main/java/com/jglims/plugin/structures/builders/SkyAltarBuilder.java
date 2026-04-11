package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

/**
 * SkyAltarBuilder — celestial mountaintop shrine built in white quartz, calcite
 * and gold, surrounded by floating auxiliary platforms connected by arched
 * bridges. Glows at night via beacons and end rods.
 *
 * <p>Architecture:
 * <ul>
 *   <li>Raised 18-radius circular main platform supported by 12 descending pillars</li>
 *   <li>Inlaid ritual sun-disk floor pattern in gold &amp; diamond</li>
 *   <li>8 tall fluted columns ring the platform, each crowned with an end rod</li>
 *   <li>Central altar with beacon cage and crown of end crystals</li>
 *   <li>4 floating satellite platforms at cardinals joined by gothic arch bridges</li>
 *   <li>Perimeter rose windows and feather-banner decor</li>
 * </ul>
 */
public final class SkyAltarBuilder implements IStructureBuilder {

    private static final Material QZ   = Material.QUARTZ_BLOCK;
    private static final Material QZP  = Material.QUARTZ_PILLAR;
    private static final Material QZB  = Material.QUARTZ_BRICKS;
    private static final Material SQZ  = Material.SMOOTH_QUARTZ;
    private static final Material CLC  = Material.CALCITE;
    private static final Material GLD  = Material.GOLD_BLOCK;
    private static final Material DIA  = Material.DIAMOND_BLOCK;
    private static final Material END  = Material.END_ROD;
    private static final Material LT   = Material.LANTERN;

    @Override
    public void build(StructureBuilder b) {
        // ─── 1. Main circular platform (radius 18, 3 blocks thick) ────
        for (int y = 0; y < 3; y++) {
            b.filledCircle(0, y, 0, 18, y == 0 ? CLC : SQZ);
        }
        // Outer ring rim
        b.circle(0, 3, 0, 18, QZB);
        // Inner calcite ring
        for (int y = 0; y < 3; y++) b.circle(0, y, 0, 18, QZB);

        // ─── 2. Inlaid sun-disk floor pattern ──────────────────────
        b.filledCircle(0, 2, 0, 10, GLD);
        b.filledCircle(0, 2, 0, 8, SQZ);
        b.filledCircle(0, 2, 0, 6, GLD);
        b.filledCircle(0, 2, 0, 4, DIA);
        b.filledCircle(0, 2, 0, 2, GLD);
        // Radial rays
        for (int a = 0; a < 360; a += 30) {
            int rx = (int) Math.round(14 * Math.cos(Math.toRadians(a)));
            int rz = (int) Math.round(14 * Math.sin(Math.toRadians(a)));
            b.setBlock(rx, 2, rz, GLD);
            int rx2 = (int) Math.round(16 * Math.cos(Math.toRadians(a)));
            int rz2 = (int) Math.round(16 * Math.sin(Math.toRadians(a)));
            b.setBlock(rx2, 2, rz2, DIA);
        }

        // ─── 3. 12 descending support pillars under the platform ──
        for (int a = 0; a < 360; a += 30) {
            int px = (int) Math.round(17 * Math.cos(Math.toRadians(a)));
            int pz = (int) Math.round(17 * Math.sin(Math.toRadians(a)));
            b.pillar(px, -25, -1, pz, QZP);
            // Capital accent
            b.setBlock(px, -25, pz, QZB);
        }

        // ─── 4. 8 fluted columns ringing the platform ─────────────
        int[] colAngles = {0, 45, 90, 135, 180, 225, 270, 315};
        for (int a : colAngles) {
            int cx = (int) Math.round(15 * Math.cos(Math.toRadians(a)));
            int cz = (int) Math.round(15 * Math.sin(Math.toRadians(a)));
            // Column shaft
            b.pillar(cx, 3, 16, cz, QZP);
            // Base
            b.setBlock(cx, 3, cz, QZB);
            // Capital
            b.setBlock(cx, 17, cz, QZB);
            b.setBlock(cx, 18, cz, SQZ);
            // End rod crown
            b.setBlock(cx, 19, cz, END);
            // Hanging lantern chain
            b.setBlock(cx, 16, cz, LT);
        }

        // ─── 5. Central altar — beacon cage ────────────────────────
        b.fillBox(-2, 3, -2, 2, 3, 2, GLD);
        b.fillBox(-1, 4, -1, 1, 5, 1, DIA);
        b.setBlock(0, 5, 0, Material.BEACON);
        // Cage of end crystals (simulated with end rods)
        b.setBlock(-2, 4, -2, END);
        b.setBlock( 2, 4, -2, END);
        b.setBlock(-2, 4,  2, END);
        b.setBlock( 2, 4,  2, END);
        // Crown ring above
        b.circle(0, 8, 0, 3, GLD);
        b.setBlock(0, 9, 0, DIA);

        // ─── 6. 4 floating satellite platforms at cardinals ──────
        satellite(b,  28,   0);
        satellite(b, -28,   0);
        satellite(b,  0,  28);
        satellite(b,  0, -28);

        // ─── 7. 4 gothic arch bridges connecting them ─────────────
        archBridge(b, 18, 0, 28, 0);
        archBridge(b, -18, 0, -28, 0);
        archBridgeZ(b, 0, 18, 0, 28);
        archBridgeZ(b, 0, -18, 0, -28);

        // ─── 8. Perimeter lantern ring ─────────────────────────────
        for (int a = 15; a < 360; a += 30) {
            int lx = (int) Math.round(17 * Math.cos(Math.toRadians(a)));
            int lz = (int) Math.round(17 * Math.sin(Math.toRadians(a)));
            b.setBlock(lx, 4, lz, LT);
        }

        // ─── 9. Feather-banner decor on 4 cardinal columns ───────
        b.setBlock(15, 19,  0, Material.WHITE_WOOL);
        b.setBlock(-15, 19, 0, Material.WHITE_WOOL);
        b.setBlock(0, 19,  15, Material.WHITE_WOOL);
        b.setBlock(0, 19, -15, Material.WHITE_WOOL);

        // ─── 10. Loot chests on satellite platforms ──────────────
        b.placeChest( 28, 1,  0);
        b.placeChest(-28, 1,  0);
        b.placeChest( 0, 1,  28);
        b.placeChest( 0, 1, -28);
        // Altar chest
        b.placeChest(0, 3, -3);

        b.setBossSpawn(0, 6, 0);
    }

    private void satellite(StructureBuilder b, int cx, int cz) {
        b.filledCircle(cx, 0, cz, 5, CLC);
        b.circle(cx, 0, cz, 5, QZB);
        b.setBlock(cx, 1, cz, GLD);
        b.setBlock(cx, 2, cz, END);
        b.setBlock(cx + 3, 1, cz, LT);
        b.setBlock(cx - 3, 1, cz, LT);
    }

    private void archBridge(StructureBuilder b, int x1, int z, int x2, int ignored) {
        int minX = Math.min(x1, x2), maxX = Math.max(x1, x2);
        for (int x = minX; x <= maxX; x++) {
            b.setBlock(x, 0, z, SQZ);
            b.setBlock(x, 0, z - 1, SQZ);
            b.setBlock(x, 0, z + 1, SQZ);
            // Arch curve over the span
            int mid = (minX + maxX) / 2;
            int distFromMid = Math.abs(x - mid);
            int rise = Math.max(0, 3 - (distFromMid * 3 / Math.max(1, (maxX - minX) / 2)));
            for (int r = 1; r <= rise; r++) {
                b.setBlock(x, r, z - 1, QZP);
                b.setBlock(x, r, z + 1, QZP);
            }
        }
    }

    private void archBridgeZ(StructureBuilder b, int x, int z1, int ignored, int z2) {
        int minZ = Math.min(z1, z2), maxZ = Math.max(z1, z2);
        for (int z = minZ; z <= maxZ; z++) {
            b.setBlock(x, 0, z, SQZ);
            b.setBlock(x - 1, 0, z, SQZ);
            b.setBlock(x + 1, 0, z, SQZ);
            int mid = (minZ + maxZ) / 2;
            int distFromMid = Math.abs(z - mid);
            int rise = Math.max(0, 3 - (distFromMid * 3 / Math.max(1, (maxZ - minZ) / 2)));
            for (int r = 1; r <= rise; r++) {
                b.setBlock(x - 1, r, z, QZP);
                b.setBlock(x + 1, r, z, QZP);
            }
        }
    }
}

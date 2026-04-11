package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;

/**
 * CrystalCavernBuilder — a gigantic hollowed amethyst geode with massive
 * stalactite and stalagmite clusters, a glowing crystal core at the
 * center, satellite geodes branching from the main chamber, and crystal
 * bridges that arch between them.
 *
 * <p>Light is provided by glow-lichen carpeting the walls and amethyst
 * cluster crystals that surge out from every surface.
 */
public final class CrystalCavernBuilder implements IStructureBuilder {

    private static final Material AMT = Material.AMETHYST_BLOCK;
    private static final Material CLC = Material.CALCITE;
    private static final Material SAM = Material.SMOOTH_BASALT;
    private static final Material AMC = Material.AMETHYST_CLUSTER;
    private static final Material BUD = Material.LARGE_AMETHYST_BUD;

    @Override
    public void build(StructureBuilder b) {
        // ─── 1. Main geode shell — 18 radius sphere ──────────────
        hollowSphere(b, 0, 15, 0, 18, SAM);
        // Amethyst inner shell
        hollowSphere(b, 0, 15, 0, 17, AMT);
        // Calcite intermediate layer
        hollowSphere(b, 0, 15, 0, 16, CLC);

        // Floor smoothed down
        b.filledCircle(0, 1, 0, 16, CLC);
        b.filledCircle(0, 0, 0, 17, CLC);
        // Ceiling carpeted with glow lichen (use SEA_LANTERN for light)
        for (int a = 0; a < 360; a += 30)
            for (int r = 0; r <= 14; r += 4) {
                int cx = (int) Math.round(r * Math.cos(Math.toRadians(a)));
                int cz = (int) Math.round(r * Math.sin(Math.toRadians(a)));
                b.setBlock(cx, 28, cz, Material.SEA_LANTERN);
            }

        // ─── 2. Central crystal core ─────────────────────────────
        b.fillBox(-3, 1, -3, 3, 1, 3, AMT);
        b.pillar(0, 2, 8, 0, AMT);
        for (int y = 2; y <= 8; y++) {
            if (y % 2 == 0) b.circle(0, y, 0, 2, BUD);
        }
        b.setBlock(0, 9, 0, AMC);
        b.setBlock(0, 10, 0, Material.BEACON);
        b.setBlock(0, 11, 0, AMT);

        // ─── 3. Massive stalactite cluster (hanging from ceiling) ─
        stalactite(b,  10,  25,  10);
        stalactite(b, -10,  25,  10);
        stalactite(b,  10,  25, -10);
        stalactite(b, -10,  25, -10);
        stalactite(b,   0,  26,  12);
        stalactite(b,   0,  26, -12);

        // ─── 4. Stalagmite cluster (rising from the floor) ──────
        stalagmite(b,  12, 1,  0);
        stalagmite(b, -12, 1,  0);
        stalagmite(b,   0, 1,  12);
        stalagmite(b,   0, 1, -12);
        stalagmite(b,   9, 1,   9);
        stalagmite(b,  -9, 1,  -9);
        stalagmite(b,   9, 1,  -9);
        stalagmite(b,  -9, 1,   9);

        // ─── 5. Satellite geodes at cardinal directions ─────────
        satelliteGeode(b,  28,  0);
        satelliteGeode(b, -28,  0);
        satelliteGeode(b,   0,  28);
        satelliteGeode(b,   0, -28);

        // ─── 6. Crystal bridges connecting main → satellites ──
        crystalBridgeX(b,  16,  28);
        crystalBridgeX(b, -16, -28);
        crystalBridgeZ(b,  16,  28);
        crystalBridgeZ(b, -16, -28);

        // ─── 7. Scattered amethyst clusters on the floor ────────
        b.scatter(-15, 1, -15, 15, 1, 15, CLC, AMC, 0.08);

        // ─── 8. Crystal shrines / chests around the core ────────
        shrineChest(b,  6, 0,  6);
        shrineChest(b, -6, 0,  6);
        shrineChest(b,  6, 0, -6);
        shrineChest(b, -6, 0, -6);

        b.setBossSpawn(0, 2, 0);
    }

    /** A hollow spherical shell of radius r centered at (cx, cy, cz). */
    private void hollowSphere(StructureBuilder b, int cx, int cy, int cz, int r, Material mat) {
        for (int x = -r; x <= r; x++)
            for (int y = -r; y <= r; y++)
                for (int z = -r; z <= r; z++) {
                    double d = Math.sqrt(x * x + y * y + z * z);
                    if (d >= r - 0.5 && d <= r + 0.5)
                        b.setBlock(cx + x, cy + y, cz + z, mat);
                }
    }

    private void stalactite(StructureBuilder b, int x, int y, int z) {
        b.setBlock(x, y, z, AMT);
        b.setBlock(x, y - 1, z, AMT);
        b.setBlock(x, y - 2, z, BUD);
        b.setBlock(x, y - 3, z, AMC);
        b.setBlock(x + 1, y - 1, z, BUD);
        b.setBlock(x - 1, y - 1, z, BUD);
    }

    private void stalagmite(StructureBuilder b, int x, int y, int z) {
        b.setBlock(x, y, z, AMT);
        b.setBlock(x, y + 1, z, AMT);
        b.setBlock(x, y + 2, z, BUD);
        b.setBlock(x, y + 3, z, AMC);
        b.setBlock(x + 1, y, z, CLC);
        b.setBlock(x - 1, y, z, CLC);
    }

    private void satelliteGeode(StructureBuilder b, int cx, int cz) {
        hollowSphere(b, cx, 8, cz, 5, AMT);
        // Hollow it
        for (int x = -3; x <= 3; x++)
            for (int y = -3; y <= 3; y++)
                for (int z = -3; z <= 3; z++)
                    if (x * x + y * y + z * z <= 9)
                        b.setBlock(cx + x, 8 + y, cz + z, Material.AIR);
        // Small core
        b.setBlock(cx, 8, cz, AMC);
        b.setBlock(cx, 9, cz, BUD);
        // Access chest
        b.placeChest(cx, 6, cz);
    }

    private void crystalBridgeX(StructureBuilder b, int x1, int x2) {
        int minX = Math.min(x1, x2), maxX = Math.max(x1, x2);
        for (int x = minX; x <= maxX; x++) {
            b.setBlock(x, 6, 0, CLC);
            b.setBlock(x, 6, -1, CLC);
            b.setBlock(x, 6,  1, CLC);
            if (x % 3 == 0) {
                b.setBlock(x, 7, -1, BUD);
                b.setBlock(x, 7,  1, BUD);
            }
        }
    }

    private void crystalBridgeZ(StructureBuilder b, int z1, int z2) {
        int minZ = Math.min(z1, z2), maxZ = Math.max(z1, z2);
        for (int z = minZ; z <= maxZ; z++) {
            b.setBlock(0, 6, z, CLC);
            b.setBlock(-1, 6, z, CLC);
            b.setBlock( 1, 6, z, CLC);
            if (z % 3 == 0) {
                b.setBlock(-1, 7, z, BUD);
                b.setBlock( 1, 7, z, BUD);
            }
        }
    }

    private void shrineChest(StructureBuilder b, int x, int y, int z) {
        b.setBlock(x, y + 1, z, AMT);
        b.setBlock(x, y + 2, z, BUD);
        b.placeChest(x + 1, y + 1, z);
    }
}

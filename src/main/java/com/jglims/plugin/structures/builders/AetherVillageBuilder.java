package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

/**
 * AetherVillageBuilder — a tiered, cloud-swept sky settlement of quartz and
 * purpur houses connected by gothic-arch bridges. Features a central market
 * plaza with a wind-chime obelisk, a sky watchtower, multiple residential
 * cottages, a shepherd's gate, and a floating pavilion of paper lanterns.
 */
public final class AetherVillageBuilder implements IStructureBuilder {

    private static final Material QZ  = Material.SMOOTH_QUARTZ;
    private static final Material QZP = Material.QUARTZ_PILLAR;
    private static final Material QZB = Material.QUARTZ_BRICKS;
    private static final Material PP  = Material.PURPUR_BLOCK;
    private static final Material PPP = Material.PURPUR_PILLAR;
    private static final Material END = Material.END_STONE_BRICKS;
    private static final Material LT  = Material.LANTERN;
    private static final Material CLC = Material.CALCITE;

    @Override
    public void build(StructureBuilder b) {
        // ─── 1. Main central plaza — 26 radius circular cloud base ─
        b.filledCircle(0, 0, 0, 26, QZ);
        b.circle(0, 0, 0, 26, QZB);
        b.filledCircle(0, 1, 0, 22, CLC);
        b.circle(0, 1, 0, 22, PP);

        // Inlaid compass pattern
        b.filledCircle(0, 2, 0, 5, END);
        b.setBlock(0, 2, 0, Material.BEACON);
        for (int a = 0; a < 360; a += 45) {
            int rx = (int) Math.round(10 * Math.cos(Math.toRadians(a)));
            int rz = (int) Math.round(10 * Math.sin(Math.toRadians(a)));
            b.setBlock(rx, 2, rz, PPP);
        }

        // ─── 2. Central wind-chime obelisk ───────────────────────
        b.pillar(0, 3, 10, 0, QZP);
        b.setBlock(0, 11, 0, Material.PURPUR_BLOCK);
        b.setBlock(0, 12, 0, Material.AMETHYST_BLOCK);
        // 4 hanging chimes
        for (int a = 0; a < 360; a += 90) {
            int cx = (int) Math.round(2 * Math.cos(Math.toRadians(a)));
            int cz = (int) Math.round(2 * Math.sin(Math.toRadians(a)));
            b.setBlock(cx, 10, cz, Material.IRON_BARS);
            b.setBlock(cx, 9,  cz, Material.IRON_BARS);
            b.setBlock(cx, 8,  cz, Material.BELL);
        }

        // ─── 3. 5 residential cottages at NE, NW, SE, SW, N ────
        cottage(b,  18,  14);
        cottage(b, -18,  14);
        cottage(b,  18, -14);
        cottage(b, -18, -14);
        cottage(b,   0,  22);

        // ─── 4. Sky watchtower on the south ─────────────────────
        watchtower(b, 0, -22);

        // ─── 5. Shepherd's gate on the east ─────────────────────
        shepherdGate(b, 25, 0);

        // ─── 6. Floating lantern pavilion west ──────────────────
        lanternPavilion(b, -25, 0);

        // ─── 7. Arch bridges connecting the plaza to each feature ─
        archBridgeX(b,  22,  0,  18,  0);
        archBridgeX(b, -22,  0, -18,  0);
        archBridgeZ(b,  0,  22,  0,  18);
        archBridgeZ(b,  0, -22,  0, -18);

        // ─── 8. Perimeter lantern ring ──────────────────────────
        for (int a = 0; a < 360; a += 20) {
            int lx = (int) Math.round(26 * Math.cos(Math.toRadians(a)));
            int lz = (int) Math.round(26 * Math.sin(Math.toRadians(a)));
            b.setBlock(lx, 2, lz, LT);
        }

        // ─── 9. Loot chests in each house and plaza ────────────
        b.placeChest( 0, 2,  4);
        b.placeChest( 0, 2, -4);

        // Note: no boss — this is a non-boss structure
    }

    /** A small 5×5 quartz/purpur cottage with a gabled roof and chest. */
    private void cottage(StructureBuilder b, int cx, int cz) {
        // Floor
        b.fillBox(cx - 3, 1, cz - 3, cx + 3, 1, cz + 3, QZ);
        // Walls
        b.fillWalls(cx - 3, 2, cz - 3, cx + 3, 4, cz + 3, QZB);
        b.hollowBox(cx - 3, 2, cz - 3, cx + 3, 4, cz + 3);
        // Gabled roof with stair slopes
        b.gabledRoof(cx - 3, cz - 3, cx + 3, cz + 3, 5,
                Material.PURPUR_STAIRS, Material.PURPUR_SLAB);
        // Door
        b.setBlock(cx, 2, cz - 3, Material.AIR);
        b.setBlock(cx, 3, cz - 3, Material.AIR);
        // Window
        b.setBlock(cx,  3, cz + 3, Material.LIGHT_BLUE_STAINED_GLASS);
        b.setBlock(cx - 3, 3, cz, Material.LIGHT_BLUE_STAINED_GLASS);
        b.setBlock(cx + 3, 3, cz, Material.LIGHT_BLUE_STAINED_GLASS);
        // Lantern inside
        b.setBlock(cx, 4, cz, LT);
        // Chair + table
        b.setStairs(cx - 1, 2, cz + 1, Material.PURPUR_STAIRS, BlockFace.SOUTH, false);
        b.setBlock(cx + 1, 2, cz + 1, Material.OAK_FENCE);
        b.setBlock(cx + 1, 3, cz + 1, Material.OAK_PRESSURE_PLATE);
        // Loot chest
        b.placeChest(cx - 1, 2, cz - 1);
    }

    /** Tall 14-block watchtower with a crenellated top. */
    private void watchtower(StructureBuilder b, int cx, int cz) {
        b.filledCircle(cx, 1, cz, 3, QZ);
        for (int y = 2; y <= 14; y++) {
            b.circle(cx, y, cz, 3, QZB);
        }
        // Windows
        b.setBlock(cx + 3, 7, cz, Material.LIGHT_BLUE_STAINED_GLASS);
        b.setBlock(cx - 3, 7, cz, Material.LIGHT_BLUE_STAINED_GLASS);
        b.setBlock(cx, 7, cz + 3, Material.LIGHT_BLUE_STAINED_GLASS);
        b.setBlock(cx, 7, cz - 3, Material.LIGHT_BLUE_STAINED_GLASS);
        // Top platform
        b.filledCircle(cx, 15, cz, 4, QZB);
        b.circle(cx, 16, cz, 4, PPP);
        // Battlements
        for (int a = 0; a < 360; a += 45) {
            int bx = cx + (int) Math.round(4 * Math.cos(Math.toRadians(a)));
            int bz = cz + (int) Math.round(4 * Math.sin(Math.toRadians(a)));
            b.setBlock(bx, 17, bz, PP);
        }
        // Watchfire
        b.setBlock(cx, 17, cz, Material.CAMPFIRE);
        // Ladder up
        for (int y = 2; y <= 14; y++) b.setBlock(cx, y, cz + 2, Material.LADDER);
        // Chest at the top
        b.placeChest(cx + 1, 16, cz);
    }

    /** An arched gate marking the village boundary. */
    private void shepherdGate(StructureBuilder b, int cx, int cz) {
        b.gothicArch(cx, 1, cz, 8, 8, QZB);
        b.pillar(cx - 4, 1, 9, cz, QZP);
        b.pillar(cx + 4, 1, 9, cz, QZP);
        b.setBlock(cx - 4, 9, cz, LT);
        b.setBlock(cx + 4, 9, cz, LT);
        // Flanking pedestals
        b.setBlock(cx - 6, 1, cz, QZB);
        b.setBlock(cx - 6, 2, cz, Material.WHITE_WOOL);
        b.setBlock(cx + 6, 1, cz, QZB);
        b.setBlock(cx + 6, 2, cz, Material.WHITE_WOOL);
    }

    /** An open-air pavilion strung with paper lanterns. */
    private void lanternPavilion(StructureBuilder b, int cx, int cz) {
        // Octagonal floor
        b.filledCircle(cx, 1, cz, 4, QZB);
        // 8 pillars
        for (int a = 0; a < 360; a += 45) {
            int px = cx + (int) Math.round(4 * Math.cos(Math.toRadians(a)));
            int pz = cz + (int) Math.round(4 * Math.sin(Math.toRadians(a)));
            b.pillar(px, 2, 6, pz, QZP);
        }
        // Roof rim
        b.circle(cx, 7, cz, 5, QZB);
        // Hanging lanterns from the rim
        for (int a = 0; a < 360; a += 45) {
            int lx = cx + (int) Math.round(4 * Math.cos(Math.toRadians(a)));
            int lz = cz + (int) Math.round(4 * Math.sin(Math.toRadians(a)));
            b.setBlock(lx, 6, lz, LT);
        }
        // Central tree-of-life prop
        b.setBlock(cx, 2, cz, Material.OAK_LOG);
        b.setBlock(cx, 3, cz, Material.OAK_LOG);
        b.setBlock(cx, 4, cz, Material.AZALEA_LEAVES);
        b.placeChest(cx + 2, 2, cz);
    }

    private void archBridgeX(StructureBuilder b, int x1, int y, int x2, int ignored) {
        int minX = Math.min(x1, x2), maxX = Math.max(x1, x2);
        for (int x = minX; x <= maxX; x++) {
            b.setBlock(x, 1, 0, QZ);
            b.setBlock(x, 1, -1, QZ);
            b.setBlock(x, 1,  1, QZ);
        }
    }

    private void archBridgeZ(StructureBuilder b, int x, int z1, int ignored, int z2) {
        int minZ = Math.min(z1, z2), maxZ = Math.max(z1, z2);
        for (int z = minZ; z <= maxZ; z++) {
            b.setBlock(x, 1, z, QZ);
            b.setBlock(x - 1, 1, z, QZ);
            b.setBlock(x + 1, 1, z, QZ);
        }
    }
}

package com.jglims.plugin.abyss;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.legendary.LegendaryWeapon;
import com.jglims.plugin.legendary.LegendaryWeaponManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class AbyssCitadelBuilder {

    private final JGlimsPlugin plugin;
    private final World world;
    private final Random rng = new Random(42);

    private static final Material W1 = Material.DEEPSLATE_BRICKS;
    private static final Material W2 = Material.DEEPSLATE_TILES;
    private static final Material WP = Material.POLISHED_DEEPSLATE;
    private static final Material BB = Material.POLISHED_BLACKSTONE_BRICKS;
    private static final Material BP = Material.POLISHED_BLACKSTONE;
    private static final Material BC = Material.CHISELED_POLISHED_BLACKSTONE;
    private static final Material OB = Material.OBSIDIAN;
    private static final Material CO = Material.CRYING_OBSIDIAN;
    private static final Material PP = Material.PURPUR_BLOCK;
    private static final Material PL = Material.PURPUR_PILLAR;
    private static final Material ES = Material.END_STONE_BRICKS;
    private static final Material AM = Material.AMETHYST_BLOCK;
    private static final Material BK = Material.BEDROCK;
    private static final Material BA = Material.BARRIER;
    private static final Material IB = Material.IRON_BARS;
    private static final Material SL = Material.SOUL_LANTERN;
    private static final Material SF = Material.SOUL_CAMPFIRE;
    private static final Material CH = Material.IRON_BARS;
    private static final Material ST = Material.DEEPSLATE_BRICK_STAIRS;
    private static final Material RC = Material.RED_CARPET;
    private static final Material PW = Material.PURPLE_WOOL;
    private static final Material ER = Material.END_ROD;
    private static final Material WR = Material.WITHER_ROSE;
    private static final Material SK = Material.SCULK;
    private static final Material CT = Material.CHEST;

    private static final int OR = 95;
    private static final int OH = 28;
    private static final int KR = 35;
    private static final int KF = 5;
    private static final int FH = 8;
    private static final int KH = KF * FH;
    private static final int TB = 13;
    private static final int TH = 60;
    private static final int STB = 9;
    private static final int STH = 45;
    private static final int AR = 40;
    private static final int AD = 25;
    private static final int SPH = 20;

    private int sY;
    private final int[][] weaponChestLocs = new int[4][3];

    public AbyssCitadelBuilder(JGlimsPlugin plugin, World world) {
        this.plugin = plugin;
        this.world = world;
    }

    public void build() {
        long start = System.currentTimeMillis();
        sY = findSurface();
        plugin.getLogger().info("[Citadel] Building mega-citadel at Y=" + sY);

        buildFoundation();
        buildOuterWalls();
        buildCornerTowers();
        buildSideTowers();
        buildGrandSouthGate();
        buildFlyingButtresses();
        buildCourtyard();
        buildInnerKeep();
        buildKeepFloors();
        buildGrandStaircase();
        buildKeepRoof();
        buildNorthParkourTower();
        buildEastPuzzleChamber();
        buildWestLibrary();
        buildSouthThroneRoom();
        buildArena();
        buildArenaStairs();
        buildDecorations();
        buildApproachPath();
        placeAbyssalChests();

        plugin.getLogger().info("[Citadel] Complete in " + (System.currentTimeMillis() - start) + "ms");
    }

    // ════════════════ FOUNDATION ════════════════
    private void buildFoundation() {
        plugin.getLogger().info("[Citadel] Foundation...");
        for (int x = -OR - 5; x <= OR + 5; x++) {
            for (int z = -OR - 5; z <= OR + 5; z++) {
                int ty = findTerrainY(x, z);
                for (int y = Math.max(1, ty - 3); y <= sY; y++)
                    s(x, y, z, y == sY ? W2 : W1);
                for (int y = sY + 1; y <= sY + KH + TH + SPH + 5; y++) {
                    Block b = world.getBlockAt(x, y, z);
                    if (b.getType().isSolid() && b.getType() != BK) b.setType(Material.AIR);
                }
            }
        }
    }

    // ════════════════ OUTER WALLS ════════════════
    private void buildOuterWalls() {
        plugin.getLogger().info("[Citadel] Outer walls...");
        int top = sY + OH;
        for (int y = sY; y <= top; y++) {
            Material m = wallMat(y - sY);
            for (int x = -OR; x <= OR; x++) {
                for (int t = 0; t < 3; t++) { s(x, y, -OR + t, m); s(x, y, OR - t, m); }
            }
            for (int z = -OR; z <= OR; z++) {
                for (int t = 0; t < 3; t++) { s(-OR + t, y, z, m); s(OR - t, y, z, m); }
            }
        }
        for (int x = -OR; x <= OR; x += 2) {
            s(x, top + 1, -OR, WP); s(x, top + 2, -OR, WP);
            s(x, top + 1, OR, WP); s(x, top + 2, OR, WP);
        }
        for (int z = -OR; z <= OR; z += 2) {
            s(-OR, top + 1, z, WP); s(-OR, top + 2, z, WP);
            s(OR, top + 1, z, WP); s(OR, top + 2, z, WP);
        }
        for (int i = -OR + 10; i <= OR - 10; i += 15) {
            buttress(i, sY, -OR, 0, -1);
            buttress(i, sY, OR, 0, 1);
            buttress(-OR, sY, i, -1, 0);
            buttress(OR, sY, i, 1, 0);
        }
        for (int i = -OR + 8; i <= OR - 8; i += 6) {
            for (int wy = sY + 5; wy <= top - 3; wy += 6) {
                s(i, wy, -OR, IB); s(i, wy + 1, -OR, IB);
                s(i, wy, OR, IB); s(i, wy + 1, OR, IB);
            }
        }
    }

    private void buttress(int bx, int by, int bz, int dx, int dz) {
        for (int y = 0; y < OH - 2; y++) {
            int d = Math.max(1, (OH - 2 - y) / 4);
            for (int i = 1; i <= d; i++) s(bx + dx * i, by + y, bz + dz * i, W1);
        }
    }

    // ════════════════ CORNER TOWERS ════════════════
    private void buildCornerTowers() {
        plugin.getLogger().info("[Citadel] Corner towers...");
        megaTower(-OR, sY, -OR, TB, TH);
        megaTower(OR - TB + 1, sY, -OR, TB, TH);
        megaTower(-OR, sY, OR - TB + 1, TB, TH);
        megaTower(OR - TB + 1, sY, OR - TB + 1, TB, TH);
    }

    private void megaTower(int tx, int ty, int tz, int base, int h) {
        for (int y = 0; y < h; y++) {
            Material m = wallMat(y);
            for (int x = 0; x < base; x++) for (int z = 0; z < base; z++) {
                boolean e = x == 0 || x == base - 1 || z == 0 || z == base - 1;
                if (e) s(tx + x, ty + y, tz + z, m);
                else if (y % FH == 0) s(tx + x, ty + y, tz + z, BP);
                else s(tx + x, ty + y, tz + z, Material.AIR);
            }
        }
        int cx = base / 2, cz = base / 2;
        for (int y = 1; y < h - 1; y++) {
            int p = y % 8;
            int sx = cx + (p < 2 ? -1 : p < 4 ? 1 : p < 6 ? 1 : -1);
            int sz = cz + (p < 2 ? -1 : p < 4 ? -1 : p < 6 ? 1 : 1);
            s(tx + sx, ty + y, tz + sz, ST);
            for (int dy = 1; dy <= 3; dy++) s(tx + sx, ty + y + dy, tz + sz, Material.AIR);
        }
        for (int r = base / 2; r >= 0; r--) {
            int y = ty + h + (base / 2 - r);
            for (int dx = -r; dx <= r; dx++) for (int dz = -r; dz <= r; dz++)
                if (dx * dx + dz * dz <= r * r + 1) s(tx + cx + dx, y, tz + cz + dz, WP);
        }
        int sb = ty + h + base / 2;
        for (int dy = 1; dy <= SPH; dy++)
            s(tx + cx, sb + dy, tz + cz, dy <= SPH - 3 ? OB : AM);
        s(tx + cx, sb + SPH + 1, tz + cz, ER);
        for (int y = 4; y < h - 3; y += FH) {
            int mid = base / 2;
            s(tx + mid, ty + y, tz, IB); s(tx + mid, ty + y + 1, tz, IB);
            s(tx + mid, ty + y, tz + base - 1, IB); s(tx + mid, ty + y + 1, tz + base - 1, IB);
            s(tx, ty + y, tz + mid, IB); s(tx, ty + y + 1, tz + mid, IB);
            s(tx + base - 1, ty + y, tz + mid, IB); s(tx + base - 1, ty + y + 1, tz + mid, IB);
        }
        for (int fl = 1; fl < h / FH; fl++) s(tx + cx, ty + fl * FH + FH - 1, tz + cz, SL);
    }

    // ════════════════ SIDE TOWERS ════════════════
    private void buildSideTowers() {
        plugin.getLogger().info("[Citadel] Side towers...");
        int m = STB / 2;
        megaTower(-m, sY, -OR - 2, STB, STH);
        megaTower(-m, sY, OR - STB + 3, STB, STH);
        megaTower(OR - STB + 3, sY, -m, STB, STH);
        megaTower(-OR - 2, sY, -m, STB, STH);
    }

    // ════════════════ GRAND SOUTH GATE ════════════════
    private void buildGrandSouthGate() {
        plugin.getLogger().info("[Citadel] Grand gate...");
        int gz = OR, gw = 7, gh = 18;
        for (int x = -gw + 1; x <= gw - 1; x++)
            for (int y = sY + 1; y <= sY + gh - 1; y++)
                for (int zt = 0; zt < 3; zt++) s(x, y, gz - zt, Material.AIR);
        for (int y = sY; y <= sY + gh + 5; y++) {
            Material mat = y % 3 == 0 ? OB : WP;
            for (int dx = 0; dx < 4; dx++) for (int dz = 0; dz < 3; dz++) {
                s(-gw - 1 + dx, y, gz - dz, mat); s(gw - 2 + dx, y, gz - dz, mat);
            }
        }
        for (int x = -gw + 1; x <= gw - 1; x++) {
            int archH = gh - (int)(Math.abs(x) * 0.5);
            for (int dz = 0; dz < 3; dz++) { s(x, sY + archH, gz - dz, WP); s(x, sY + archH + 1, gz - dz, WP); }
        }
        for (int x = -gw + 2; x <= gw - 2; x++)
            for (int y = sY + gh - 4; y >= sY + gh - 7; y--) s(x, y, gz, IB);
        for (int x = -gw + 3; x <= gw - 3; x += 3)
            for (int dy = 1; dy <= 4; dy++) s(x, sY + gh - 7 - dy, gz, CH);
        for (int dy = 0; dy < 6; dy++) { s(-gw - 2, sY + gh - dy, gz - 3, PW); s(gw + 1, sY + gh - dy, gz - 3, PW); }
        s(-gw - 2, sY + gh + 6, gz, SF); s(gw + 1, sY + gh + 6, gz, SF);
        for (int z = gz - 2; z >= gz - 15; z--)
            for (int x = -2; x <= 2; x++) s(x, sY + 1, z, RC);
    }

    // ════════════════ FLYING BUTTRESSES ════════════════
    private void buildFlyingButtresses() {
        plugin.getLogger().info("[Citadel] Buttresses...");
        int midY = sY + 15;
        for (int i = -KR + 10; i <= KR - 10; i += 20) {
            archButtress(i, midY, -KR - 1, i, midY, -OR + 4);
            archButtress(i, midY, KR + 1, i, midY, OR - 4);
            archButtress(KR + 1, midY, i, OR - 4, midY, i);
            archButtress(-KR - 1, midY, i, -OR + 4, midY, i);
        }
    }

    private void archButtress(int x1, int y1, int z1, int x2, int y2, int z2) {
        int steps = Math.max(Math.abs(x2 - x1), Math.abs(z2 - z1));
        if (steps == 0) return;
        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            int x = x1 + (int)((x2 - x1) * t);
            int z = z1 + (int)((z2 - z1) * t);
            int ay = y1 + (int)(6 * Math.sin(t * Math.PI));
            s(x, ay, z, W1); s(x, ay - 1, z, W1);
            if (i == 0 || i == steps)
                for (int dy = ay; dy >= sY; dy--) s(x, dy, z, W1);
        }
    }

    // ════════════════ COURTYARD ════════════════
    private void buildCourtyard() {
        plugin.getLogger().info("[Citadel] Courtyard...");
        for (int x = -OR + 4; x <= OR - 4; x++) for (int z = -OR + 4; z <= OR - 4; z++) {
            if (Math.abs(x) <= KR + 3 && Math.abs(z) <= KR + 3) continue;
            Material m; int d = Math.abs(x) + Math.abs(z);
            if (d % 9 == 0) m = CO; else if ((x + z) % 4 == 0) m = BP; else m = W2;
            s(x, sY, z, m);
            for (int y = sY + 1; y <= sY + 5; y++) {
                Block b = world.getBlockAt(x, y, z);
                if (b.getType() != Material.AIR && b.getType() != BK) b.setType(Material.AIR);
            }
        }
        for (int x = -OR + 12; x <= OR - 12; x += 18)
            for (int z = -OR + 12; z <= OR - 12; z += 18) {
                if (Math.abs(x) <= KR + 6 && Math.abs(z) <= KR + 6) continue;
                for (int dy = 0; dy <= 6; dy++) s(x, sY + dy, z, WP);
                s(x, sY + 7, z, SL);
            }
        int[][] gc = {{-OR+8,-OR+8},{OR-12,-OR+8},{-OR+8,OR-12},{OR-12,OR-12}};
        for (int[] c : gc)
            for (int dx = 0; dx < 5; dx++) for (int dz = 0; dz < 5; dz++)
                if (rng.nextDouble() < 0.4) { s(c[0]+dx, sY, c[1]+dz, Material.SOUL_SOIL); s(c[0]+dx, sY+1, c[1]+dz, WR); }
    }

    // ════════════════ INNER KEEP ════════════════
    private void buildInnerKeep() {
        plugin.getLogger().info("[Citadel] Inner keep...");
        int top = sY + KH;
        for (int y = sY; y <= top; y++) {
            Material m = wallMat(y - sY);
            for (int x = -KR; x <= KR; x++)
                for (int t = 0; t < 3; t++) { s(x, y, -KR + t, m); s(x, y, KR - t, m); }
            for (int z = -KR; z <= KR; z++)
                for (int t = 0; t < 3; t++) { s(-KR + t, y, z, m); s(KR - t, y, z, m); }
        }
        for (int x = -4; x <= 4; x++)
            for (int y = sY + 1; y <= sY + 10; y++)
                for (int zt = 0; zt < 3; zt++) s(x, y, KR - zt, Material.AIR);
        for (int x = -4; x <= 4; x++) { s(x, sY + 11, KR, WP); s(x, sY + 11, KR - 1, WP); }
        for (int x = -KR; x <= KR; x += 2) { s(x, top + 1, -KR, BC); s(x, top + 1, KR, BC); }
        for (int z = -KR; z <= KR; z += 2) { s(-KR, top + 1, z, BC); s(KR, top + 1, z, BC); }
    }

    // ════════════════ KEEP FLOORS ════════════════
    private void buildKeepFloors() {
        plugin.getLogger().info("[Citadel] Keep floors...");
        for (int fl = 0; fl < KF; fl++) {
            int fy = sY + (fl * FH);
            for (int x = -KR + 3; x <= KR - 3; x++) for (int z = -KR + 3; z <= KR - 3; z++) {
                if (Math.abs(x) <= 3 && Math.abs(z) <= 3 && fl > 0) continue;
                s(x, fy, z, ((x + z) % 2 == 0) ? W2 : BP);
                for (int y = fy + 1; y < fy + FH; y++) s(x, y, z, Material.AIR);
            }
            for (int x = -KR + 8; x <= KR - 8; x += 12) for (int z = -KR + 8; z <= KR - 8; z += 12) {
                if (Math.abs(x) <= 5 && Math.abs(z) <= 5) continue;
                for (int y = fy; y < fy + FH; y++) s(x, y, z, PL);
            }
            for (int x = -KR + 6; x <= KR - 6; x += 8) for (int z = -KR + 6; z <= KR - 6; z += 8) {
                if (Math.abs(x) <= 4 && Math.abs(z) <= 4) continue;
                s(x, fy + FH - 1, z, SL);
            }
            floorFeatures(fl, fy);
        }
    }

    private void floorFeatures(int fl, int fy) {
        switch (fl) {
            case 0 -> {
                for (int z = KR - 4; z >= 0; z--) for (int x = -1; x <= 1; x++) s(x, fy + 1, z, RC);
                room(-KR+5, fy, -KR+5, -KR+18, fy+FH-1, -10, true);
                room(KR-18, fy, -KR+5, KR-5, fy+FH-1, -10, true);
            }
            case 1 -> {
                room(-KR+5, fy, -KR+5, -8, fy+FH-1, KR-5, true);
                room(8, fy, -KR+5, KR-5, fy+FH-1, KR-5, true);
            }
            case 2 -> {
                for (int x = -KR+5; x <= KR-5; x += 2) {
                    if (Math.abs(x) <= 5) continue;
                    s(x, fy+1, -KR+5, Material.BOOKSHELF); s(x, fy+2, -KR+5, Material.BOOKSHELF);
                    s(x, fy+3, -KR+5, Material.BOOKSHELF);
                    s(x, fy+1, KR-5, Material.BOOKSHELF); s(x, fy+2, KR-5, Material.BOOKSHELF);
                }
                s(15, fy+1, 0, Material.ENCHANTING_TABLE);
            }
            case 3 -> {
                for (int x = -3; x <= 3; x++) for (int z = -3; z <= 3; z++)
                    if (Math.abs(x) <= 2 && Math.abs(z) <= 2 && !(Math.abs(x) <= 1 && Math.abs(z) <= 1))
                        s(x, fy+1, z, BB);
                s(0, fy+2, 0, Material.PURPLE_CANDLE);
            }
            case 4 -> {
                s(0, fy+1, 0, AM); s(0, fy+2, 0, AM); s(0, fy+3, 0, ER);
                for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++)
                    if (dx != 0 || dz != 0) s(dx, fy+1, dz, AM);
            }
        }
    }

    private void room(int x1, int y1, int z1, int x2, int y2, int z2, boolean loot) {
        for (int y = y1+1; y <= y2; y++) {
            for (int x = x1; x <= x2; x++) { s(x, y, z1, W2); s(x, y, z2, W2); }
            for (int z = z1; z <= z2; z++) { s(x1, y, z, W2); s(x2, y, z, W2); }
        }
        int mx = (x1+x2)/2, mz = (z1+z2)/2;
        for (int dy = 1; dy <= 3; dy++) { s(mx, y1+dy, z2, Material.AIR); if (mx+1 <= x2) s(mx+1, y1+dy, z2, Material.AIR); }
        s(mx, y2-1, mz, SL);
        if (loot) s(x1+2, y1+1, z1+2, CT);
    }

    // ════════════════ GRAND STAIRCASE ════════════════
    private void buildGrandStaircase() {
        plugin.getLogger().info("[Citadel] Grand staircase...");
        int sr = 4;
        for (int fl = 0; fl < KF; fl++) {
            int by = sY + (fl * FH);
            for (int x = -sr; x <= sr; x++) for (int z = -sr; z <= sr; z++)
                for (int y = by + 1; y < by + FH; y++) s(x, y, z, Material.AIR);
            for (int step = 0; step < FH; step++) {
                double ang = (step / (double) FH) * 2 * Math.PI;
                int sx = (int) Math.round(Math.cos(ang) * 3);
                int sz = (int) Math.round(Math.sin(ang) * 3);
                int y = by + step + 1;
                s(sx, y, sz, ST); s(sx + (sx >= 0 ? -1 : 1), y, sz, ST);
                for (int dy = 1; dy <= 3; dy++) {
                    s(sx, y+dy, sz, Material.AIR);
                    s(sx + (sx >= 0 ? -1 : 1), y+dy, sz, Material.AIR);
                }
            }
            for (int y = by; y < by + FH; y++) s(0, y, 0, PL);
            if (fl > 0) {
                for (int x = -sr; x <= sr; x++) { s(x, by+1, -sr, IB); s(x, by+1, sr, IB); }
                for (int z = -sr; z <= sr; z++) { s(-sr, by+1, z, IB); s(sr, by+1, z, IB); }
            }
        }
    }

    // ════════════════ KEEP ROOF ════════════════
    private void buildKeepRoof() {
        plugin.getLogger().info("[Citadel] Keep roof...");
        int ry = sY + KH;
        for (int x = -KR+3; x <= KR-3; x++) for (int z = -KR+3; z <= KR-3; z++) s(x, ry, z, W2);
        for (int y = 0; y < 25; y++) {
            int r = Math.max(1, 3 - y / 4);
            for (int dx = -r; dx <= r; dx++) for (int dz = -r; dz <= r; dz++)
                if (dx*dx + dz*dz <= r*r) s(dx, ry+y, dz, WP);
        }
        for (int dy = 25; dy <= 35; dy++) s(0, ry+dy, 0, OB);
        s(0, ry+36, 0, AM); s(0, ry+37, 0, ER);
        int[][] kc = {{-KR+3,-KR+3},{KR-3,-KR+3},{-KR+3,KR-3},{KR-3,KR-3}};
        for (int[] c : kc) { for (int dy = 0; dy < 8; dy++) s(c[0], ry+1+dy, c[1], WP); s(c[0], ry+9, c[1], ER); }
    }

    // ════════════════ NORTH: PARKOUR TOWER ════════════════
    private void buildNorthParkourTower() {
        plugin.getLogger().info("[Citadel] North parkour tower...");
        int cl = 15, ch = 6;
        for (int z = -KR-cl; z <= -KR; z++) {
            for (int y = sY; y <= sY+ch; y++) {
                s(-2, y, z, W1); s(2, y, z, W1);
                if (y == sY) for (int x = -1; x <= 1; x++) s(x, y, z, BP);
                else if (y == sY+ch) for (int x = -2; x <= 2; x++) s(x, y, z, W2);
                else for (int x = -1; x <= 1; x++) s(x, y, z, Material.AIR);
            }
        }
        for (int dy = 1; dy <= 4; dy++) for (int x = -1; x <= 1; x++)
            for (int zt = 0; zt < 3; zt++) s(x, sY+dy, -KR+zt, Material.AIR);

        int tB = 15, tH = 50;
        int tz = -KR - cl - tB, tx = -tB / 2;
        for (int y = 0; y < tH; y++) {
            Material m = wallMat(y);
            for (int xi = 0; xi < tB; xi++) for (int zi = 0; zi < tB; zi++) {
                boolean e = xi == 0 || xi == tB-1 || zi == 0 || zi == tB-1;
                if (e) s(tx+xi, sY+y, tz+zi, m);
                else if (y == 0) s(tx+xi, sY+y, tz+zi, BP);
                else s(tx+xi, sY+y, tz+zi, Material.AIR);
            }
        }
        for (int xi = 0; xi < tB; xi++) for (int zi = 0; zi < tB; zi++) s(tx+xi, sY+tH, tz+zi, W2);

        for (int lv = 0; lv < 12; lv++) {
            int py = sY + 3 + lv * 4;
            double ang = lv * Math.PI / 3;
            int px = tx + tB/2 + (int)(Math.cos(ang) * 4);
            int pz = tz + tB/2 + (int)(Math.sin(ang) * 4);
            for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) s(px+dx, py, pz+dz, BP);
            s(px, py-1, pz, SL);
        }

        int topY = sY + 3 + 11 * 4 + 3;
        int topX = tx + tB/2, topZ = tz + tB/2;
        for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++) s(topX+dx, topY, topZ+dz, AM);
        s(topX, topY+1, topZ, CT);
        weaponChestLocs[0] = new int[]{topX, topY+1, topZ};
    }

    // ════════════════ EAST: PUZZLE CHAMBER ════════════════
    private void buildEastPuzzleChamber() {
        plugin.getLogger().info("[Citadel] East puzzle chamber...");
        int cl = 15, ch = 6;
        for (int x = KR; x <= KR+cl; x++) {
            for (int y = sY; y <= sY+ch; y++) {
                s(x, y, -2, W1); s(x, y, 2, W1);
                if (y == sY) for (int z = -1; z <= 1; z++) s(x, y, z, BP);
                else if (y == sY+ch) for (int z = -2; z <= 2; z++) s(x, y, z, W2);
                else for (int z = -1; z <= 1; z++) s(x, y, z, Material.AIR);
            }
        }
        for (int dy = 1; dy <= 4; dy++) for (int z = -1; z <= 1; z++)
            for (int xt = 0; xt < 3; xt++) s(KR-xt, sY+dy, z, Material.AIR);

        int rx1 = KR+cl+1, rx2 = rx1+19, rz1 = -10, rz2 = 10, rh = 10;
        for (int y = sY; y <= sY+rh; y++) {
            Material m = wallMat(y-sY);
            for (int x = rx1; x <= rx2; x++) { s(x, y, rz1, m); s(x, y, rz2, m); }
            for (int z = rz1; z <= rz2; z++) { s(rx1, y, z, m); s(rx2, y, z, m); }
        }
        for (int x = rx1+1; x < rx2; x++) for (int z = rz1+1; z < rz2; z++) {
            s(x, sY, z, BP);
            for (int y = sY+1; y < sY+rh; y++) s(x, y, z, Material.AIR);
        }
        for (int x = rx1; x <= rx2; x++) for (int z = rz1; z <= rz2; z++) s(x, sY+rh, z, W2);
        for (int dy = 1; dy <= 4; dy++) for (int z = -1; z <= 1; z++) s(rx1, sY+dy, z, Material.AIR);
        for (int x = rx1+3; x < rx2-2; x += 3)
            for (int z = rz1+3; z < rz2-2; z += 4)
                for (int dy = 1; dy <= 5; dy++) s(x, sY+dy, z, OB);
        for (int x = rx1+2; x < rx2-1; x += 5) s(x, sY+rh-1, (rz1+rz2)/2, Material.REDSTONE_LAMP);
        s((rx1+rx2)/2, sY+rh-1, rz1+2, SL); s((rx1+rx2)/2, sY+rh-1, rz2-2, SL);
        s(rx2-3, sY+1, (rz1+rz2)/2, CT);
        weaponChestLocs[1] = new int[]{rx2-3, sY+1, (rz1+rz2)/2};
    }

    // ════════════════ WEST: LIBRARY ════════════════
    private void buildWestLibrary() {
        plugin.getLogger().info("[Citadel] West library...");
        int cl = 15, ch = 6;
        for (int x = -KR-cl; x <= -KR; x++) {
            for (int y = sY; y <= sY+ch; y++) {
                s(x, y, -2, W1); s(x, y, 2, W1);
                if (y == sY) for (int z = -1; z <= 1; z++) s(x, y, z, BP);
                else if (y == sY+ch) for (int z = -2; z <= 2; z++) s(x, y, z, W2);
                else for (int z = -1; z <= 1; z++) s(x, y, z, Material.AIR);
            }
        }
        for (int dy = 1; dy <= 4; dy++) for (int z = -1; z <= 1; z++)
            for (int xt = 0; xt < 3; xt++) s(-KR+xt, sY+dy, z, Material.AIR);

        int rx1 = -KR-cl-20, rx2 = -KR-cl-1, rz1 = -10, rz2 = 10, rh = 10;
        for (int y = sY; y <= sY+rh; y++) {
            Material m = wallMat(y-sY);
            for (int x = rx1; x <= rx2; x++) { s(x, y, rz1, m); s(x, y, rz2, m); }
            for (int z = rz1; z <= rz2; z++) { s(rx1, y, z, m); s(rx2, y, z, m); }
        }
        for (int x = rx1+1; x < rx2; x++) for (int z = rz1+1; z < rz2; z++) {
            s(x, sY, z, BP);
            for (int y = sY+1; y < sY+rh; y++) s(x, y, z, Material.AIR);
        }
        for (int x = rx1; x <= rx2; x++) for (int z = rz1; z <= rz2; z++) s(x, sY+rh, z, W2);
        for (int dy = 1; dy <= 4; dy++) for (int z = -1; z <= 1; z++) s(rx2, sY+dy, z, Material.AIR);
        for (int z = rz1+2; z < rz2-1; z += 3)
            for (int x = rx1+2; x < rx2-4; x++) {
                s(x, sY+1, z, Material.BOOKSHELF); s(x, sY+2, z, Material.BOOKSHELF); s(x, sY+3, z, Material.BOOKSHELF);
            }
        int vx1 = rx1+2, vx2 = rx1+8, vz1 = -3, vz2 = 3;
        for (int y = sY+1; y <= sY+6; y++) {
            for (int x = vx1; x <= vx2; x++) { s(x, y, vz1, OB); s(x, y, vz2, OB); }
            for (int z = vz1; z <= vz2; z++) { s(vx1, y, z, OB); s(vx2, y, z, OB); }
        }
        for (int x = vx1+1; x < vx2; x++) for (int z = vz1+1; z < vz2; z++)
            for (int y = sY+1; y <= sY+5; y++) s(x, y, z, Material.AIR);
        for (int dy = 1; dy <= 3; dy++) s(vx2, sY+dy, 0, Material.AIR);
        s(vx1+2, sY+1, 0, CT);
        weaponChestLocs[2] = new int[]{vx1+2, sY+1, 0};
        s((rx1+rx2)/2, sY+rh-1, 0, SL); s(vx1+3, sY+5, 0, SL);
    }

    // ════════════════ SOUTH: THRONE ROOM ════════════════
    private void buildSouthThroneRoom() {
        plugin.getLogger().info("[Citadel] South throne room...");
        int tz1 = KR+5, tz2 = OR-8, tx1 = -20, tx2 = 20, th = 14;
        for (int y = sY; y <= sY+th; y++) {
            Material m = wallMat(y-sY);
            for (int x = tx1; x <= tx2; x++) { s(x, y, tz1, m); s(x, y, tz2, m); }
            for (int z = tz1; z <= tz2; z++) { s(tx1, y, z, m); s(tx2, y, z, m); }
        }
        for (int x = tx1+1; x < tx2; x++) for (int z = tz1+1; z < tz2; z++) {
            s(x, sY, z, ((x+z)%2==0) ? W2 : BP);
            for (int y = sY+1; y < sY+th; y++) s(x, y, z, Material.AIR);
        }
        for (int x = tx1; x <= tx2; x++) for (int z = tz1; z <= tz2; z++) s(x, sY+th, z, W2);
        for (int dy = 1; dy <= 6; dy++) for (int x = -3; x <= 3; x++) s(x, sY+dy, tz1, Material.AIR);
        for (int z = tz1+1; z < tz2; z++) for (int x = -1; x <= 1; x++) s(x, sY+1, z, RC);
        for (int z = tz1+5; z < tz2-3; z += 6) for (int y = sY+1; y < sY+th; y++) {
            s(tx1+4, y, z, PL); s(tx2-4, y, z, PL);
        }
        int tZ = tz2 - 5;
        for (int x = -3; x <= 3; x++) for (int z = tZ-1; z <= tZ+2; z++) {
            s(x, sY+1, z, OB); s(x, sY+2, z, Material.AIR);
        }
        s(0, sY+2, tZ, Material.POLISHED_BLACKSTONE_BRICK_STAIRS);
        s(0, sY+3, tZ+1, BB); s(0, sY+4, tZ+1, BB); s(0, sY+5, tZ+1, AM);
        s(-1, sY+3, tZ+1, BB); s(1, sY+3, tZ+1, BB);
        for (int x = -3; x <= 3; x += 2) for (int dy = 3; dy <= 8; dy++) s(x, sY+dy, tz2-2, PW);
        s(0, sY+2, tZ+2, CT);
        weaponChestLocs[3] = new int[]{0, sY+2, tZ+2};
        for (int z = tz1+5; z < tz2-3; z += 6) s(0, sY+th-1, z, SL);
    }

    // ════════════════ DRAGON ARENA ════════════════
    private void buildArena() {
        plugin.getLogger().info("[Citadel] Dragon arena...");
        int ay = sY - AD;
        for (int x = -AR-1; x <= AR+1; x++) for (int z = -AR-1; z <= AR+1; z++)
            if (x*x + z*z <= (AR+1)*(AR+1)) s(x, ay, z, BK);
        for (int x = -AR; x <= AR; x++) for (int z = -AR; z <= AR; z++)
            if (x*x + z*z <= AR*AR) for (int y = ay+1; y <= sY+5; y++) s(x, y, z, Material.AIR);
        for (int y = ay; y <= sY+15; y++)
            for (int a = 0; a < 360; a++) {
                double r = Math.toRadians(a);
                for (int l = 0; l <= 2; l++) {
                    int rd = AR+1+l;
                    s((int)Math.round(rd*Math.cos(r)), y, (int)Math.round(rd*Math.sin(r)), BK);
                }
            }
        for (int x = -AR-2; x <= AR+2; x++) for (int z = -AR-2; z <= AR+2; z++)
            if (x*x + z*z <= (AR+2)*(AR+2)) { s(x, sY+15, z, BK); s(x, sY+16, z, BK); }
        for (int x = -AR-3; x <= AR+3; x++) for (int z = -AR-3; z <= AR+3; z++)
            if (x*x + z*z <= (AR+3)*(AR+3)) s(x, sY+17, z, BA);
        for (int x = -4; x <= 4; x++) for (int z = -4; z <= 4; z++)
            if (x*x + z*z <= 16) s(x, ay+1, z, OB);
        s(0, ay+2, 0, Material.LODESTONE);
        for (int x = -AR+3; x <= AR-3; x += 4) for (int z = -AR+3; z <= AR-3; z += 4)
            if (x*x + z*z < (AR-3)*(AR-3)) s(x, ay, z, CO);
        for (int a = 0; a < 360; a += 15) {
            double r = Math.toRadians(a);
            s((int)Math.round((AR-3)*Math.cos(r)), ay+1, (int)Math.round((AR-3)*Math.sin(r)), SF);
        }
        for (int a = 0; a < 360; a += 45) {
            double r = Math.toRadians(a);
            int px = (int)Math.round(20*Math.cos(r)), pz = (int)Math.round(20*Math.sin(r));
            for (int dy = 1; dy <= 5+rng.nextInt(6); dy++) s(px, ay+dy, pz, ES);
            s(px, ay+8, pz, SL);
        }
    }

    // ════════════════ ARENA STAIRS ════════════════
    private void buildArenaStairs() {
        plugin.getLogger().info("[Citadel] Arena stairs...");
        int ay = sY - AD, sw = 3;
        for (int x = -sw-1; x <= sw+1; x++) for (int z = -sw-1; z <= sw+1; z++)
            for (int y = ay+1; y <= sY; y++)
                if (world.getBlockAt(x, y, z).getType() != BK) s(x, y, z, Material.AIR);
        int total = sY - ay;
        for (int step = 0; step < total; step++) {
            int y = sY - step;
            int p = step % 12;
            int sx, sz;
            if (p < 3) { sx = -sw + p; sz = -sw; }
            else if (p < 6) { sx = sw; sz = -sw + (p-3); }
            else if (p < 9) { sx = sw - (p-6); sz = sw; }
            else { sx = -sw; sz = sw - (p-9); }
            s(sx, y, sz, ST); s(sx + (sx > 0 ? -1 : 1), y, sz, ST);
            for (int dy = 1; dy <= 4; dy++) {
                s(sx, y+dy, sz, Material.AIR);
                s(sx + (sx > 0 ? -1 : 1), y+dy, sz, Material.AIR);
            }
            if (step % 6 == 0) s(sx > 0 ? sx+1 : sx-1, y+2, sz, SL);
        }
        for (int dy = 1; dy <= 5; dy++) for (int x = -2; x <= 2; x++) {
            s(x, ay+dy, -AR-1, Material.AIR); s(x, ay+dy, -AR, Material.AIR); s(x, ay+dy, -AR+1, Material.AIR);
        }
        for (int x = -2; x <= 2; x++) {
            s(x, ay, -AR-1, BK); s(x, ay, -AR, BK);
            s(x, ay+6, -AR-1, BK); s(x, ay+6, -AR, BK);
        }
    }

    // ════════════════ DECORATIONS ════════════════
    private void buildDecorations() {
        plugin.getLogger().info("[Citadel] Decorations...");
        int[][] kc = {{-KR,-KR},{KR,-KR},{-KR,KR},{KR,KR}};
        for (int[] c : kc) for (int dy = 0; dy < 4; dy++) s(c[0], sY+KH+2+dy, c[1], ER);
        for (int i = 0; i < 30; i++) {
            int ax = -OR+6+rng.nextInt(OR*2-12), az = -OR+6+rng.nextInt(OR*2-12);
            if (Math.abs(ax) < KR+5 && Math.abs(az) < KR+5) continue;
            s(ax, sY+1, az, Material.AMETHYST_CLUSTER);
        }
        for (int x = -OR+10; x <= OR-10; x += 20) for (int dy = 0; dy < 5; dy++) {
            s(x, sY+OH-dy, -OR+1, PW); s(x, sY+OH-dy, OR-1, PW);
        }
        for (int z = -OR+10; z <= OR-10; z += 20) for (int dy = 0; dy < 5; dy++) {
            s(-OR+1, sY+OH-dy, z, PW); s(OR-1, sY+OH-dy, z, PW);
        }
        int[][] sa = {{-OR+5,-OR+5},{OR-10,-OR+5},{-OR+5,OR-10},{OR-10,OR-10}};
        for (int[] c : sa) for (int dx = 0; dx < 6; dx++) for (int dz = 0; dz < 6; dz++)
            if (rng.nextDouble() < 0.5) s(c[0]+dx, sY, c[1]+dz, SK);
    }

    // ════════════════ APPROACH PATH ════════════════
    private void buildApproachPath() {
        plugin.getLogger().info("[Citadel] Approach path...");
        for (int z = OR; z <= 120; z++) for (int x = -5; x <= 5; x++) {
            int ty = findTerrainY(x, z);
            for (int y = ty; y <= sY; y++) s(x, y, z, Math.abs(x) <= 2 ? BP : W2);
            if (Math.abs(x) == 5 && z % 10 == 0) {
                for (int dy = 1; dy <= 5; dy++) s(x, sY+dy, z, WP);
                s(x, sY+6, z, SL);
            }
        }
        for (int z = OR; z <= 120; z++) for (int x = -1; x <= 1; x++) s(x, sY+1, z, RC);
    }

    // ════════════════ PLACE ABYSSAL WEAPON CHESTS ════════════════
    private void placeAbyssalChests() {
        plugin.getLogger().info("[Citadel] Placing ABYSSAL weapon chests...");
        LegendaryWeapon[] weapons = {
            LegendaryWeapon.REQUIEM_AWAKENED,
            LegendaryWeapon.EXCALIBUR_AWAKENED,
            LegendaryWeapon.CREATION_SPLITTER_AWAKENED,
            LegendaryWeapon.WHISPERWIND_AWAKENED
        };
        List<LegendaryWeapon> shuffled = new ArrayList<>();
        Collections.addAll(shuffled, weapons);
        Collections.shuffle(shuffled, rng);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            LegendaryWeaponManager wm = plugin.getLegendaryWeaponManager();
            if (wm == null) { plugin.getLogger().warning("[Citadel] WeaponManager null!"); return; }
            for (int i = 0; i < 4; i++) {
                int[] loc = weaponChestLocs[i];
                Block b = world.getBlockAt(loc[0], loc[1], loc[2]);
                if (b.getType() != Material.CHEST) b.setType(Material.CHEST);
                if (b.getState() instanceof Chest chest) {
                    Inventory inv = chest.getInventory();
                    inv.clear();
                    ItemStack wp = wm.createWeapon(shuffled.get(i));
                    if (wp != null) inv.setItem(13, wp);
                    inv.setItem(0, new ItemStack(Material.NETHERITE_INGOT, 2+rng.nextInt(3)));
                    inv.setItem(2, new ItemStack(Material.DIAMOND, 8+rng.nextInt(10)));
                    inv.setItem(4, new ItemStack(Material.NETHER_STAR, 1+rng.nextInt(2)));
                    inv.setItem(6, new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 2+rng.nextInt(3)));
                    inv.setItem(8, new ItemStack(Material.TOTEM_OF_UNDYING, 1));
                    inv.setItem(18, new ItemStack(Material.EXPERIENCE_BOTTLE, 32+rng.nextInt(32)));
                    inv.setItem(22, new ItemStack(Material.ELYTRA, 1));
                    try {
                        if (plugin.getPowerUpManager() != null) {
                            inv.setItem(10, plugin.getPowerUpManager().createHeartCrystal());
                            inv.setItem(16, plugin.getPowerUpManager().createSoulFragment());
                        }
                    } catch (Exception ignored) {}
                    chest.update(true);
                    plugin.getLogger().info("[Citadel] Placed " + shuffled.get(i).getDisplayName() + " at " + loc[0] + "," + loc[1] + "," + loc[2]);
                }
            }
        }, 5L);
    }

    // ════════════════ UTILITY ════════════════
    private void s(int x, int y, int z, Material m) {
        if (y < -64 || y > 319) return;
        world.getBlockAt(x, y, z).setType(m);
    }

    private int findSurface() {
        for (int y = 120; y > 1; y--) if (world.getBlockAt(0, y, 0).getType().isSolid()) return y;
        return 50;
    }

    private int findTerrainY(int x, int z) {
        for (int y = 120; y > 1; y--) if (world.getBlockAt(x, y, z).getType().isSolid()) return y;
        return 45;
    }

    private Material wallMat(int h) {
        if (h % 7 == 0) return WP;
        if (h % 5 == 0) return BB;
        if (h % 3 == 0) return W2;
        return W1;
    }
}
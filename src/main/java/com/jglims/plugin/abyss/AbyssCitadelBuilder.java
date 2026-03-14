package com.jglims.plugin.abyss;

import com.jglims.plugin.JGlimsPlugin;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Random;

public class AbyssCitadelBuilder {

    private final JGlimsPlugin plugin;
    private final World world;
    private final Random random = new Random(42);

    // ── Material palette ──────────────────────────────────────
    private static final Material WALL_PRIMARY = Material.DEEPSLATE_BRICKS;
    private static final Material WALL_SECONDARY = Material.DEEPSLATE_TILES;
    private static final Material WALL_ACCENT = Material.POLISHED_DEEPSLATE;
    private static final Material FLOOR_PRIMARY = Material.DEEPSLATE_TILES;
    private static final Material FLOOR_ACCENT = Material.POLISHED_BLACKSTONE;
    private static final Material PILLAR = Material.POLISHED_DEEPSLATE;
    private static final Material STAIRS_MAT = Material.DEEPSLATE_BRICK_STAIRS;
    private static final Material DARK_ACCENT = Material.OBSIDIAN;
    private static final Material GLOW_BLOCK = Material.CRYING_OBSIDIAN;
    private static final Material CRYSTAL = Material.AMETHYST_BLOCK;
    private static final Material IRON_BARS_MAT = Material.IRON_BARS;
    private static final Material SOUL_LANTERN_MAT = Material.SOUL_LANTERN;
    private static final Material SOUL_FIRE_MAT = Material.SOUL_CAMPFIRE;
    private static final Material END_STONE_MAT = Material.END_STONE_BRICKS;
    private static final Material BEDROCK_MAT = Material.BEDROCK;
    private static final Material BARRIER_MAT = Material.BARRIER;
    private static final Material CHAIN_MAT;

    static {
        Material resolved;
        try {
            resolved = Material.valueOf("CHAIN");
        } catch (Exception e) {
            resolved = Material.IRON_BARS;
        }
        CHAIN_MAT = resolved;
    }

    // ── Dimensions ────────────────────────────────────────────
    private static final int OUTER_HALF = 70;
    private static final int OUTER_WALL_H = 25;
    private static final int KEEP_HALF = 30;
    private static final int KEEP_WALL_H = 35;
    private static final int KEEP_FLOORS = 4;
    private static final int FLOOR_HEIGHT = 7;
    private static final int TOWER_SIZE = 9;
    private static final int TOWER_HEIGHT = 45;
    private static final int ARENA_RADIUS = 30;
    private static final int ARENA_DEPTH = 20;
    private static final int GATE_WIDTH = 11;
    private static final int GATE_HEIGHT = 15;

    private int surfaceY;

    public AbyssCitadelBuilder(JGlimsPlugin plugin, World world) {
        this.plugin = plugin;
        this.world = world;
    }

    public void build() {
        long start = System.currentTimeMillis();
        surfaceY = findSurface();
        plugin.getLogger().info("[Citadel] Building at Y=" + surfaceY);

        buildFoundation();
        buildOuterWalls();
        buildCornerTowers();
        buildGrandSouthGate();
        buildCourtyard();
        buildInnerKeep();
        buildKeepFloors();
        buildGrandStaircase();
        buildArena();
        buildArenaStairwell();
        buildArenaBarriers();
        buildWingCorridors();
        buildWingRooms();
        buildDecorations();
        buildApproachPath();

        long elapsed = System.currentTimeMillis() - start;
        plugin.getLogger().info("[Citadel] Complete in " + elapsed + "ms");
    }

    // ── Foundation ────────────────────────────────────────────

    private void buildFoundation() {
        plugin.getLogger().info("[Citadel] Laying foundation...");
        for (int x = -OUTER_HALF - 2; x <= OUTER_HALF + 2; x++) {
            for (int z = -OUTER_HALF - 2; z <= OUTER_HALF + 2; z++) {
                int terrainY = findTerrainY(x, z);
                for (int y = terrainY; y <= surfaceY; y++) {
                    Material mat = (y == surfaceY) ? FLOOR_PRIMARY : WALL_PRIMARY;
                    set(x, y, z, mat);
                }
                for (int y = surfaceY + 1; y <= surfaceY + KEEP_WALL_H + 15; y++) {
                    Block b = world.getBlockAt(x, y, z);
                    if (b.getType().isSolid() && b.getType() != BEDROCK_MAT) {
                        b.setType(Material.AIR);
                    }
                }
            }
        }
    }

    // ── Outer walls ───────────────────────────────────────────

    private void buildOuterWalls() {
        plugin.getLogger().info("[Citadel] Building outer walls...");
        int base = surfaceY;
        int top = base + OUTER_WALL_H;

        for (int y = base; y <= top; y++) {
            Material wallMat = (y % 3 == 0) ? WALL_ACCENT : WALL_PRIMARY;
            for (int x = -OUTER_HALF; x <= OUTER_HALF; x++) {
                set(x, y, -OUTER_HALF, wallMat);
                set(x, y, OUTER_HALF, wallMat);
            }
            for (int z = -OUTER_HALF; z <= OUTER_HALF; z++) {
                set(-OUTER_HALF, y, z, wallMat);
                set(OUTER_HALF, y, z, wallMat);
            }
        }

        for (int x = -OUTER_HALF; x <= OUTER_HALF; x += 2) {
            set(x, top + 1, -OUTER_HALF, WALL_SECONDARY);
            set(x, top + 1, OUTER_HALF, WALL_SECONDARY);
        }
        for (int z = -OUTER_HALF; z <= OUTER_HALF; z += 2) {
            set(-OUTER_HALF, top + 1, z, WALL_SECONDARY);
            set(OUTER_HALF, top + 1, z, WALL_SECONDARY);
        }

        for (int i = -OUTER_HALF + 7; i <= OUTER_HALF - 7; i += 14) {
            buildButtress(i, base, -OUTER_HALF, 0, -1);
            buildButtress(i, base, OUTER_HALF, 0, 1);
            buildButtress(-OUTER_HALF, base, i, -1, 0);
            buildButtress(OUTER_HALF, base, i, 1, 0);
        }
    }

    private void buildButtress(int bx, int by, int bz, int dx, int dz) {
        for (int y = 0; y < OUTER_WALL_H - 2; y++) {
            int depth = Math.max(1, (OUTER_WALL_H - 2 - y) / 5);
            for (int d = 1; d <= depth; d++) {
                set(bx + dx * d, by + y, bz + dz * d, WALL_PRIMARY);
            }
        }
    }

    // ── Corner towers ─────────────────────────────────────────

    private void buildCornerTowers() {
        plugin.getLogger().info("[Citadel] Building corner towers...");
        buildTower(-OUTER_HALF, surfaceY, -OUTER_HALF);
        buildTower(OUTER_HALF - TOWER_SIZE + 1, surfaceY, -OUTER_HALF);
        buildTower(-OUTER_HALF, surfaceY, OUTER_HALF - TOWER_SIZE + 1);
        buildTower(OUTER_HALF - TOWER_SIZE + 1, surfaceY, OUTER_HALF - TOWER_SIZE + 1);
    }

    private void buildTower(int tx, int ty, int tz) {
        for (int y = 0; y < TOWER_HEIGHT; y++) {
            Material mat = (y % 4 == 0) ? WALL_ACCENT : WALL_PRIMARY;
            for (int x = 0; x < TOWER_SIZE; x++) {
                for (int z = 0; z < TOWER_SIZE; z++) {
                    boolean edge = (x == 0 || x == TOWER_SIZE - 1 || z == 0 || z == TOWER_SIZE - 1);
                    if (edge) {
                        set(tx + x, ty + y, tz + z, mat);
                    } else if (y % FLOOR_HEIGHT == 0) {
                        set(tx + x, ty + y, tz + z, FLOOR_PRIMARY);
                    }
                }
            }
        }
        for (int r = TOWER_SIZE / 2; r >= 0; r--) {
            int y = ty + TOWER_HEIGHT + (TOWER_SIZE / 2 - r);
            int cx = tx + TOWER_SIZE / 2;
            int cz = tz + TOWER_SIZE / 2;
            for (int x = -r; x <= r; x++) {
                for (int z = -r; z <= r; z++) {
                    set(cx + x, y, cz + z, WALL_SECONDARY);
                }
            }
        }
        set(tx + TOWER_SIZE / 2, ty + TOWER_HEIGHT + TOWER_SIZE / 2 + 1, tz + TOWER_SIZE / 2, SOUL_LANTERN_MAT);
    }

    // ── Grand south gate ──────────────────────────────────────

    private void buildGrandSouthGate() {
        plugin.getLogger().info("[Citadel] Building grand south gate...");
        int base = surfaceY;
        int gateHalfW = GATE_WIDTH / 2;
        int gz = OUTER_HALF;

        for (int x = -gateHalfW + 1; x <= gateHalfW - 1; x++) {
            for (int y = base + 1; y < base + GATE_HEIGHT; y++) {
                set(x, y, gz, Material.AIR);
            }
        }

        for (int y = base; y <= base + GATE_HEIGHT + 3; y++) {
            Material mat = (y % 3 == 0) ? DARK_ACCENT : PILLAR;
            for (int dx = 0; dx < 3; dx++) {
                set(-gateHalfW - 1 + dx, y, gz, mat);
                set(-gateHalfW - 1 + dx, y, gz + 1, mat);
                set(gateHalfW - 1 + dx, y, gz, mat);
                set(gateHalfW - 1 + dx, y, gz + 1, mat);
            }
        }

        for (int x = -gateHalfW + 1; x <= gateHalfW - 1; x++) {
            for (int dy = 0; dy < 3; dy++) {
                set(x, base + GATE_HEIGHT + dy, gz, WALL_ACCENT);
            }
        }

        for (int x = -gateHalfW + 1; x <= gateHalfW - 1; x++) {
            set(x, base + GATE_HEIGHT - 1, gz, IRON_BARS_MAT);
            set(x, base + GATE_HEIGHT - 2, gz, IRON_BARS_MAT);
        }

        set(-gateHalfW - 2, base + GATE_HEIGHT + 4, gz, SOUL_FIRE_MAT);
        set(gateHalfW + 2, base + GATE_HEIGHT + 4, gz, SOUL_FIRE_MAT);

        for (int x = -gateHalfW + 2; x <= gateHalfW - 2; x += 3) {
            for (int dy = 1; dy <= 3; dy++) {
                set(x, base + GATE_HEIGHT - 2 - dy, gz, CHAIN_MAT);
            }
        }

        for (int dy = 0; dy < 4; dy++) {
            set(-gateHalfW - 1, base + GATE_HEIGHT - dy, gz - 1, Material.PURPLE_WOOL);
            set(gateHalfW + 1, base + GATE_HEIGHT - dy, gz - 1, Material.PURPLE_WOOL);
        }

        for (int x = -gateHalfW; x <= gateHalfW; x++) {
            for (int dz = -2; dz <= 2; dz++) {
                set(x, base, gz + dz, FLOOR_ACCENT);
            }
        }
    }

    // ── Courtyard ─────────────────────────────────────────────

    private void buildCourtyard() {
        plugin.getLogger().info("[Citadel] Building courtyard...");
        int base = surfaceY;
        for (int x = -OUTER_HALF + 2; x <= OUTER_HALF - 2; x++) {
            for (int z = -OUTER_HALF + 2; z <= OUTER_HALF - 2; z++) {
                if (Math.abs(x) <= KEEP_HALF + 2 && Math.abs(z) <= KEEP_HALF + 2) continue;
                Material mat;
                int dist = Math.abs(x) + Math.abs(z);
                if (dist % 7 == 0) mat = GLOW_BLOCK;
                else if ((x + z) % 3 == 0) mat = FLOOR_ACCENT;
                else mat = FLOOR_PRIMARY;
                set(x, base, z, mat);
                for (int y = base + 1; y <= base + 4; y++) {
                    Block b = world.getBlockAt(x, y, z);
                    if (b.getType() != Material.AIR) b.setType(Material.AIR);
                }
            }
        }

        for (int x = -OUTER_HALF + 10; x <= OUTER_HALF - 10; x += 15) {
            for (int z = -OUTER_HALF + 10; z <= OUTER_HALF - 10; z += 15) {
                if (Math.abs(x) <= KEEP_HALF + 5 && Math.abs(z) <= KEEP_HALF + 5) continue;
                for (int y = 0; y <= 5; y++) {
                    set(x, base + y, z, PILLAR);
                }
                set(x, base + 6, z, SOUL_LANTERN_MAT);
            }
        }
    }

    // ── Inner keep ────────────────────────────────────────────

    private void buildInnerKeep() {
        plugin.getLogger().info("[Citadel] Building inner keep...");
        int base = surfaceY;
        int top = base + KEEP_WALL_H;

        for (int y = base; y <= top; y++) {
            Material wallMat = (y % 4 == 0) ? WALL_ACCENT : WALL_PRIMARY;
            for (int x = -KEEP_HALF; x <= KEEP_HALF; x++) {
                set(x, y, -KEEP_HALF, wallMat);
                set(x, y, KEEP_HALF, wallMat);
            }
            for (int z = -KEEP_HALF; z <= KEEP_HALF; z++) {
                set(-KEEP_HALF, y, z, wallMat);
                set(KEEP_HALF, y, z, wallMat);
            }
        }

        for (int x = -KEEP_HALF; x <= KEEP_HALF; x += 2) {
            set(x, top + 1, -KEEP_HALF, WALL_SECONDARY);
            set(x, top + 1, KEEP_HALF, WALL_SECONDARY);
        }
        for (int z = -KEEP_HALF; z <= KEEP_HALF; z += 2) {
            set(-KEEP_HALF, top + 1, z, WALL_SECONDARY);
            set(KEEP_HALF, top + 1, z, WALL_SECONDARY);
        }

        for (int x = -2; x <= 2; x++) {
            for (int y = base + 1; y <= base + 7; y++) {
                set(x, y, KEEP_HALF, Material.AIR);
            }
        }
        for (int x = -3; x <= 3; x++) {
            set(x, base + 8, KEEP_HALF, WALL_ACCENT);
        }
    }

    // ── Keep floors ───────────────────────────────────────────

    private void buildKeepFloors() {
        plugin.getLogger().info("[Citadel] Building keep floors...");
        int base = surfaceY;

        for (int floor = 0; floor < KEEP_FLOORS; floor++) {
            int floorY = base + (floor * FLOOR_HEIGHT);

            for (int x = -KEEP_HALF + 1; x <= KEEP_HALF - 1; x++) {
                for (int z = -KEEP_HALF + 1; z <= KEEP_HALF - 1; z++) {
                    Material mat = ((x + z) % 2 == 0) ? FLOOR_PRIMARY : FLOOR_ACCENT;
                    set(x, floorY, z, mat);
                }
            }

            for (int x = -KEEP_HALF + 1; x <= KEEP_HALF - 1; x++) {
                for (int z = -KEEP_HALF + 1; z <= KEEP_HALF - 1; z++) {
                    for (int y = floorY + 1; y < floorY + FLOOR_HEIGHT; y++) {
                        set(x, y, z, Material.AIR);
                    }
                }
            }

            for (int x = -KEEP_HALF + 5; x <= KEEP_HALF - 5; x += 10) {
                for (int z = -KEEP_HALF + 5; z <= KEEP_HALF - 5; z += 10) {
                    for (int y = floorY; y < floorY + FLOOR_HEIGHT; y++) {
                        set(x, y, z, PILLAR);
                    }
                }
            }

            if (floor < KEEP_FLOORS - 1) {
                int ceilY = floorY + FLOOR_HEIGHT - 1;
                for (int x = -KEEP_HALF + 1; x <= KEEP_HALF - 1; x += 5) {
                    for (int z = -KEEP_HALF + 1; z <= KEEP_HALF - 1; z += 5) {
                        set(x, ceilY, z, SOUL_LANTERN_MAT);
                    }
                }
            }

            buildFloorRooms(floor, floorY);
        }
    }

    private void buildFloorRooms(int floor, int floorY) {
        int topY = floorY + FLOOR_HEIGHT - 1;
        switch (floor) {
            case 0:
                buildRoom(-KEEP_HALF + 2, floorY, -KEEP_HALF + 2, KEEP_HALF - 2, topY, -2, true);
                buildRoom(-KEEP_HALF + 2, floorY, 3, -5, topY, KEEP_HALF - 2, true);
                buildRoom(5, floorY, 3, KEEP_HALF - 2, topY, KEEP_HALF - 2, true);
                break;
            case 1:
                buildRoom(-KEEP_HALF + 2, floorY, -KEEP_HALF + 2, -2, topY, KEEP_HALF - 2, true);
                buildRoom(2, floorY, -KEEP_HALF + 2, KEEP_HALF - 2, topY, KEEP_HALF - 2, true);
                break;
            case 2:
                buildRoom(-KEEP_HALF + 2, floorY, -KEEP_HALF + 2, KEEP_HALF - 2, topY, KEEP_HALF - 2, false);
                set(0, floorY + 1, -KEEP_HALF + 4, Material.POLISHED_BLACKSTONE_STAIRS);
                set(-1, floorY + 1, -KEEP_HALF + 4, DARK_ACCENT);
                set(1, floorY + 1, -KEEP_HALF + 4, DARK_ACCENT);
                for (int dy = 1; dy <= 4; dy++) {
                    set(-2, floorY + dy, -KEEP_HALF + 4, PILLAR);
                    set(2, floorY + dy, -KEEP_HALF + 4, PILLAR);
                }
                break;
            case 3:
                buildRoom(-KEEP_HALF + 2, floorY, -KEEP_HALF + 2, KEEP_HALF - 2, topY, KEEP_HALF - 2, false);
                set(0, floorY + 1, 0, CRYSTAL);
                set(0, floorY + 2, 0, CRYSTAL);
                set(0, floorY + 3, 0, Material.END_ROD);
                set(-1, floorY + 1, -1, CRYSTAL);
                set(1, floorY + 1, -1, CRYSTAL);
                set(-1, floorY + 1, 1, CRYSTAL);
                set(1, floorY + 1, 1, CRYSTAL);
                break;
        }
    }

    private void buildRoom(int x1, int y1, int z1, int x2, int y2, int z2, boolean addLoot) {
        for (int y = y1 + 1; y <= y2; y++) {
            for (int x = x1; x <= x2; x++) {
                set(x, y, z1, WALL_SECONDARY);
                set(x, y, z2, WALL_SECONDARY);
            }
            for (int z = z1; z <= z2; z++) {
                set(x1, y, z, WALL_SECONDARY);
                set(x2, y, z, WALL_SECONDARY);
            }
        }

        int midX = (x1 + x2) / 2;
        int midZ = (z1 + z2) / 2;
        for (int dy = 1; dy <= 3; dy++) {
            set(midX, y1 + dy, z2, Material.AIR);
            if (midX + 1 <= x2) set(midX + 1, y1 + dy, z2, Material.AIR);
            set(x1, y1 + dy, midZ, Material.AIR);
            set(x1, y1 + dy, midZ + 1, Material.AIR);
        }

        set(midX, y2 - 1, midZ, SOUL_LANTERN_MAT);

        if (addLoot) {
            set(x1 + 2, y1 + 1, z1 + 2, Material.CHEST);
            set(x2 - 2, y1 + 1, z2 - 2, Material.CHEST);
        }
    }

    // ── Grand staircase ───────────────────────────────────────

    private void buildGrandStaircase() {
        plugin.getLogger().info("[Citadel] Building grand staircase...");
        int base = surfaceY;
        int sx = KEEP_HALF - 6;
        int sz = 0;

        for (int floor = 0; floor < KEEP_FLOORS - 1; floor++) {
            int startY = base + (floor * FLOOR_HEIGHT);
            int endY = startY + FLOOR_HEIGHT;

            for (int step = 0; step < FLOOR_HEIGHT; step++) {
                int z = sz - 3 + step;
                set(sx, startY + step + 1, z, STAIRS_MAT);
                set(sx + 1, startY + step + 1, z, STAIRS_MAT);
                set(sx - 1, startY + step + 2, z, IRON_BARS_MAT);
                for (int dy = 1; dy <= 3; dy++) {
                    set(sx, startY + step + 1 + dy, z, Material.AIR);
                    set(sx + 1, startY + step + 1 + dy, z, Material.AIR);
                }
            }

            for (int x = sx - 1; x <= sx + 2; x++) {
                for (int z2 = sz - 4; z2 <= sz - 3; z2++) {
                    set(x, endY, z2, FLOOR_PRIMARY);
                }
            }
        }
    }

    // ── Arena ─────────────────────────────────────────────────

    private void buildArena() {
        plugin.getLogger().info("[Citadel] Building arena...");
        int arenaY = surfaceY - ARENA_DEPTH;

        for (int x = -ARENA_RADIUS; x <= ARENA_RADIUS; x++) {
            for (int z = -ARENA_RADIUS; z <= ARENA_RADIUS; z++) {
                if (x * x + z * z > ARENA_RADIUS * ARENA_RADIUS) continue;
                set(x, arenaY, z, BEDROCK_MAT);
                for (int y = arenaY + 1; y < surfaceY; y++) {
                    set(x, y, z, Material.AIR);
                }
            }
        }

        for (int y = arenaY; y <= arenaY + ARENA_DEPTH + 5; y++) {
            for (int angle = 0; angle < 360; angle++) {
                double rad = Math.toRadians(angle);
                int x = (int) Math.round(ARENA_RADIUS * Math.cos(rad));
                int z = (int) Math.round(ARENA_RADIUS * Math.sin(rad));
                set(x, y, z, BEDROCK_MAT);
            }
        }

        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                if (x * x + z * z <= 9) {
                    set(x, arenaY + 1, z, DARK_ACCENT);
                }
            }
        }
        set(0, arenaY + 2, 0, Material.LODESTONE);

        for (int angle = 0; angle < 360; angle += 20) {
            double rad = Math.toRadians(angle);
            int x = (int) Math.round((ARENA_RADIUS - 2) * Math.cos(rad));
            int z = (int) Math.round((ARENA_RADIUS - 2) * Math.sin(rad));
            set(x, arenaY + 1, z, SOUL_FIRE_MAT);
        }

        for (int x = -ARENA_RADIUS + 2; x <= ARENA_RADIUS - 2; x += 5) {
            for (int z = -ARENA_RADIUS + 2; z <= ARENA_RADIUS - 2; z += 5) {
                if (x * x + z * z < (ARENA_RADIUS - 2) * (ARENA_RADIUS - 2)) {
                    set(x, arenaY, z, GLOW_BLOCK);
                }
            }
        }
    }

    // ── Arena barriers ────────────────────────────────────────

    private void buildArenaBarriers() {
        plugin.getLogger().info("[Citadel] Building arena barriers...");
        int arenaY = surfaceY - ARENA_DEPTH;

        for (int y = arenaY + ARENA_DEPTH + 6; y <= arenaY + ARENA_DEPTH + 25; y++) {
            for (int angle = 0; angle < 360; angle++) {
                double rad = Math.toRadians(angle);
                int x = (int) Math.round((ARENA_RADIUS + 1) * Math.cos(rad));
                int z = (int) Math.round((ARENA_RADIUS + 1) * Math.sin(rad));
                set(x, y, z, BARRIER_MAT);
            }
        }

        for (int x = -ARENA_RADIUS - 1; x <= ARENA_RADIUS + 1; x++) {
            for (int z = -ARENA_RADIUS - 1; z <= ARENA_RADIUS + 1; z++) {
                if (x * x + z * z <= (ARENA_RADIUS + 1) * (ARENA_RADIUS + 1)) {
                    set(x, arenaY + ARENA_DEPTH + 25, z, BARRIER_MAT);
                }
            }
        }

        for (int angle = 0; angle < 360; angle += 15) {
            double rad = Math.toRadians(angle);
            int x = (int) Math.round((ARENA_RADIUS - 1) * Math.cos(rad));
            int z = (int) Math.round((ARENA_RADIUS - 1) * Math.sin(rad));
            int spikeH = 3 + random.nextInt(4);
            for (int dy = 1; dy <= spikeH; dy++) {
                set(x, arenaY + dy, z, END_STONE_MAT);
            }
            set(x, arenaY + spikeH + 1, z, Material.POINTED_DRIPSTONE);
        }
    }

    // ── Arena stairwell ───────────────────────────────────────

    private void buildArenaStairwell() {
        plugin.getLogger().info("[Citadel] Building arena stairwell...");
        int base = surfaceY;
        int arenaY = surfaceY - ARENA_DEPTH;
        int sx = -2;
        int ex = 2;
        int sz = -KEEP_HALF + 3;

        for (int x = sx; x <= ex; x++) {
            for (int z = sz; z <= sz + 3; z++) {
                set(x, base, z, Material.AIR);
            }
        }

        for (int y = base; y > arenaY; y--) {
            int step = base - y;
            int zOff = step % 8;
            boolean goingNorth = (step / 8) % 2 == 0;
            int targetZ = goingNorth ? (sz + zOff) : (sz + 7 - zOff);

            for (int x = sx; x <= ex; x++) {
                set(x, y, targetZ, STAIRS_MAT);
                set(sx - 1, y, targetZ, WALL_PRIMARY);
                set(ex + 1, y, targetZ, WALL_PRIMARY);
                for (int dy = 1; dy <= 3; dy++) {
                    set(x, y + dy, targetZ, Material.AIR);
                }
            }

            if (step % 4 == 0) {
                set(sx - 1, y + 2, targetZ, SOUL_LANTERN_MAT);
            }
        }

        for (int x = sx; x <= ex; x++) {
            for (int dy = 1; dy <= 4; dy++) {
                set(x, arenaY + dy, ARENA_RADIUS - 1, Material.AIR);
            }
        }
    }

    // ── Wing corridors ────────────────────────────────────────

    private void buildWingCorridors() {
        plugin.getLogger().info("[Citadel] Building wing corridors...");
        int base = surfaceY;
        int corridorH = 5;

        for (int z = -KEEP_HALF + 5; z <= KEEP_HALF - 5; z++) {
            for (int y = base; y <= base + corridorH; y++) {
                set(KEEP_HALF + 1, y, z, WALL_PRIMARY);
                set(KEEP_HALF + 5, y, z, WALL_PRIMARY);
                if (y == base) {
                    for (int x = KEEP_HALF + 2; x <= KEEP_HALF + 4; x++) set(x, y, z, FLOOR_PRIMARY);
                } else {
                    for (int x = KEEP_HALF + 2; x <= KEEP_HALF + 4; x++) set(x, y, z, Material.AIR);
                }
            }
            for (int x = KEEP_HALF + 1; x <= KEEP_HALF + 5; x++) set(x, base + corridorH + 1, z, WALL_SECONDARY);
        }

        for (int z = -KEEP_HALF + 5; z <= KEEP_HALF - 5; z++) {
            for (int y = base; y <= base + corridorH; y++) {
                set(-KEEP_HALF - 1, y, z, WALL_PRIMARY);
                set(-KEEP_HALF - 5, y, z, WALL_PRIMARY);
                if (y == base) {
                    for (int x = -KEEP_HALF - 4; x <= -KEEP_HALF - 2; x++) set(x, y, z, FLOOR_PRIMARY);
                } else {
                    for (int x = -KEEP_HALF - 4; x <= -KEEP_HALF - 2; x++) set(x, y, z, Material.AIR);
                }
            }
            for (int x = -KEEP_HALF - 5; x <= -KEEP_HALF - 1; x++) set(x, base + corridorH + 1, z, WALL_SECONDARY);
        }

        for (int dy = 1; dy <= 3; dy++) {
            set(KEEP_HALF, base + dy, 0, Material.AIR);
            set(-KEEP_HALF, base + dy, 0, Material.AIR);
        }

        for (int z = -KEEP_HALF + 7; z <= KEEP_HALF - 7; z += 5) {
            set(KEEP_HALF + 3, base + corridorH, z, SOUL_LANTERN_MAT);
            set(-KEEP_HALF - 3, base + corridorH, z, SOUL_LANTERN_MAT);
        }
    }

    // ── Wing rooms ────────────────────────────────────────────

    private void buildWingRooms() {
        plugin.getLogger().info("[Citadel] Building wing rooms...");
        int base = surfaceY;

        for (int i = 0; i < 4; i++) {
            int rz = -KEEP_HALF + 8 + (i * 12);
            int rx = KEEP_HALF + 6;
            buildWingRoom(rx, base, rz, rx + 10, base + 5, rz + 10);
            for (int dy = 1; dy <= 3; dy++) set(KEEP_HALF + 5, base + dy, rz + 5, Material.AIR);
        }

        for (int i = 0; i < 4; i++) {
            int rz = -KEEP_HALF + 8 + (i * 12);
            int rx = -KEEP_HALF - 16;
            buildWingRoom(rx, base, rz, rx + 10, base + 5, rz + 10);
            for (int dy = 1; dy <= 3; dy++) set(-KEEP_HALF - 5, base + dy, rz + 5, Material.AIR);
        }
    }

    private void buildWingRoom(int x1, int y1, int z1, int x2, int y2, int z2) {
        for (int y = y1; y <= y2; y++) {
            for (int x = x1; x <= x2; x++) {
                set(x, y, z1, WALL_PRIMARY);
                set(x, y, z2, WALL_PRIMARY);
            }
            for (int z = z1; z <= z2; z++) {
                set(x1, y, z, WALL_PRIMARY);
                set(x2, y, z, WALL_PRIMARY);
            }
        }
        for (int x = x1 + 1; x < x2; x++) {
            for (int z = z1 + 1; z < z2; z++) {
                set(x, y1, z, FLOOR_PRIMARY);
                for (int y = y1 + 1; y < y2; y++) set(x, y, z, Material.AIR);
            }
        }
        for (int x = x1; x <= x2; x++) {
            for (int z = z1; z <= z2; z++) set(x, y2, z, WALL_SECONDARY);
        }
        int mx = (x1 + x2) / 2;
        int mz = (z1 + z2) / 2;
        set(mx, y2 - 1, mz, SOUL_LANTERN_MAT);
        set(x1 + 2, y1 + 1, z1 + 2, Material.CHEST);
        set(x2 - 2, y1 + 1, z2 - 2, Material.CHEST);
    }

    // ── Decorations ───────────────────────────────────────────

    private void buildDecorations() {
        plugin.getLogger().info("[Citadel] Adding decorations...");
        int base = surfaceY;

        int[][] keepCorners = {{-KEEP_HALF, KEEP_HALF}, {KEEP_HALF, KEEP_HALF},
                {-KEEP_HALF, -KEEP_HALF}, {KEEP_HALF, -KEEP_HALF}};
        for (int[] pos : keepCorners) {
            for (int dy = 0; dy < 3; dy++) {
                set(pos[0], base + KEEP_WALL_H + 2 + dy, pos[1], Material.END_ROD);
            }
        }

        for (int i = 0; i < 20; i++) {
            int x = -OUTER_HALF + 5 + random.nextInt(OUTER_HALF * 2 - 10);
            int z = -OUTER_HALF + 5 + random.nextInt(OUTER_HALF * 2 - 10);
            if (Math.abs(x) < KEEP_HALF + 5 && Math.abs(z) < KEEP_HALF + 5) continue;
            set(x, base + 1, z, Material.AMETHYST_CLUSTER);
        }

        for (int x = -OUTER_HALF + 7; x <= OUTER_HALF - 7; x += 14) {
            for (int dy = 0; dy < 3; dy++) {
                set(x, base + OUTER_WALL_H - dy, -OUTER_HALF + 1, Material.PURPLE_WOOL);
                set(x, base + OUTER_WALL_H - dy, OUTER_HALF - 1, Material.PURPLE_WOOL);
            }
        }
    }

    // ── Approach path ─────────────────────────────────────────

    private void buildApproachPath() {
        plugin.getLogger().info("[Citadel] Building approach path...");
        int base = surfaceY;

        for (int z = OUTER_HALF; z <= 90; z++) {
            for (int x = -3; x <= 3; x++) {
                int terrainY = findTerrainY(x, z);
                for (int y = terrainY; y <= base; y++) {
                    set(x, y, z, (Math.abs(x) <= 1) ? FLOOR_ACCENT : FLOOR_PRIMARY);
                }
                if (Math.abs(x) == 3 && z % 8 == 0) {
                    for (int dy = 1; dy <= 4; dy++) set(x, base + dy, z, PILLAR);
                    set(x, base + 5, z, SOUL_LANTERN_MAT);
                }
            }
        }
    }

    // ── Helpers ────────────────────────────────────────────────

    private void set(int x, int y, int z, Material material) {
        world.getBlockAt(x, y, z).setType(material);
    }

    private int findSurface() {
        for (int y = 120; y > 1; y--) {
            if (world.getBlockAt(0, y, 0).getType().isSolid()) return y;
        }
        return 55;
    }

    private int findTerrainY(int x, int z) {
        for (int y = 120; y > 1; y--) {
            if (world.getBlockAt(x, y, z).getType().isSolid()) return y;
        }
        return 50;
    }
}
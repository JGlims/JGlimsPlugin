package com.jglims.plugin.abyss;

import com.jglims.plugin.JGlimsPlugin;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

/**
 * Builds the Abyssal Citadel — a massive, grounded dark fortress
 * with a grand entrance, interior rooms, staircases to the arena,
 * and a bedrock dragon-fighting arena with barrier spikes.
 *
 * Citadel center: (0, surfaceY, 0)
 * Grand entrance gate: (0, surfaceY, ~70) — south face
 * Arena: underground at (0, arenaY, 0)
 * Teleport landing: (0, safeY, 85) — just outside south gate
 */
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
    private static final Material ROOF = Material.DEEPSLATE_BRICK_SLAB;
    private static final Material STAIRS_MAT = Material.DEEPSLATE_BRICK_STAIRS;
    private static final Material DARK_ACCENT = Material.OBSIDIAN;
    private static final Material GLOW_BLOCK = Material.CRYING_OBSIDIAN;
    private static final Material CRYSTAL = Material.AMETHYST_BLOCK;
    private static final Material PURPUR = Material.PURPUR_BLOCK;
    private static final Material PURPUR_PILLAR = Material.PURPUR_PILLAR;
    private static final Material IRON_BARS = Material.IRON_BARS;
    private static final Material SOUL_LANTERN = Material.SOUL_LANTERN;
    private static final Material SOUL_FIRE = Material.SOUL_CAMPFIRE;
    private static final Material END_STONE = Material.END_STONE_BRICKS;
    private static final Material BEDROCK = Material.BEDROCK;
    private static final Material BARRIER = Material.BARRIER;
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
    // Outer walls: 141x141 footprint centered on (0,0)
    private static final int OUTER_HALF = 70;     // -70 to +70
    private static final int OUTER_WALL_H = 25;   // wall height
    // Inner keep: 61x61
    private static final int KEEP_HALF = 30;
    private static final int KEEP_WALL_H = 35;
    private static final int KEEP_FLOORS = 4;
    private static final int FLOOR_HEIGHT = 7;     // each keep floor
    // Towers
    private static final int TOWER_SIZE = 9;
    private static final int TOWER_HEIGHT = 45;
    // Arena
    private static final int ARENA_RADIUS = 30;
    private static final int ARENA_DEPTH = 20;     // below surface
    // Gate
    private static final int GATE_WIDTH = 11;
    private static final int GATE_HEIGHT = 15;

    private int surfaceY;

    public AbyssCitadelBuilder(JGlimsPlugin plugin, World world) {
        this.plugin = plugin;
        this.world = world;
    }

    // ── Main build entry ──────────────────────────────────────

    public void build() {
        long start = System.currentTimeMillis();
        surfaceY = findSurface();
        plugin.getLogger().info("[Citadel] Building at Y=" + surfaceY);

        buildFoundation();
        buildOuterWalls();
        buildCornerTowers();
        buildMidWallTowers();
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

    // ── Foundation: fill down to terrain ──────────────────────

    private void buildFoundation() {
        plugin.getLogger().info("[Citadel] Laying foundation...");
        for (int x = -OUTER_HALF - 2; x <= OUTER_HALF + 2; x++) {
            for (int z = -OUTER_HALF - 2; z <= OUTER_HALF + 2; z++) {
                // Find terrain at this column
                int terrainY = findTerrainY(x, z);
                // Fill from terrain up to surfaceY with solid blocks
                for (int y = terrainY; y <= surfaceY; y++) {
                    Material mat = (y == surfaceY) ? FLOOR_PRIMARY : WALL_PRIMARY;
                    setBlock(x, y, z, mat);
                }
                // Clear air above to surfaceY + KEEP_WALL_H + 10
                for (int y = surfaceY + 1; y <= surfaceY + KEEP_WALL_H + 15; y++) {
                    Block b = world.getBlockAt(x, y, z);
                    if (b.getType().isSolid() && b.getType() != BEDROCK) {
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

            // North and south walls
            for (int x = -OUTER_HALF; x <= OUTER_HALF; x++) {
                setBlock(x, y, -OUTER_HALF, wallMat);
                setBlock(x, y, OUTER_HALF, wallMat);
            }
            // East and west walls
            for (int z = -OUTER_HALF; z <= OUTER_HALF; z++) {
                setBlock(-OUTER_HALF, y, z, wallMat);
                setBlock(OUTER_HALF, y, z, wallMat);
            }
        }

        // Crenellations on top
        for (int x = -OUTER_HALF; x <= OUTER_HALF; x += 2) {
            setBlock(x, top + 1, -OUTER_HALF, WALL_SECONDARY);
            setBlock(x, top + 1, OUTER_HALF, WALL_SECONDARY);
        }
        for (int z = -OUTER_HALF; z <= OUTER_HALF; z += 2) {
            setBlock(-OUTER_HALF, top + 1, z, WALL_SECONDARY);
            setBlock(OUTER_HALF, top + 1, z, WALL_SECONDARY);
        }

        // Buttresses every 14 blocks on outer walls
        for (int i = -OUTER_HALF + 7; i <= OUTER_HALF - 7; i += 14) {
            buildButtress(i, base, -OUTER_HALF, BlockFace.NORTH);
            buildButtress(i, base, OUTER_HALF, BlockFace.SOUTH);
            buildButtress(-OUTER_HALF, base, i, BlockFace.WEST);
            buildButtress(OUTER_HALF, base, i, BlockFace.EAST);
        }
    }

    private void buildButtress(int bx, int by, int bz, BlockFace face) {
        int dx = 0, dz = 0;
        switch (face) {
            case NORTH -> dz = -1;
            case SOUTH -> dz = 1;
            case WEST -> dx = -1;
            case EAST -> dx = 1;
        }
        for (int y = 0; y < OUTER_WALL_H - 2; y++) {
            int depth = Math.max(1, (OUTER_WALL_H - 2 - y) / 5);
            for (int d = 1; d <= depth; d++) {
                setBlock(bx + dx * d, by + y, bz + dz * d, WALL_PRIMARY);
            }
            setBlock(bx, by + y, bz, WALL_PRIMARY); // reinforce wall line
        }
    }

    // ── Corner towers ─────────────────────────────────────────

    private void buildCornerTowers() {
        plugin.getLogger().info("[Citadel] Building corner towers...");
        int[][] corners = {
                {-OUTER_HALF, -OUTER_HALF},
                {OUTER_HALF - TOWER_SIZE + 1, -OUTER_HALF},
                {-OUTER_HALF, OUTER_HALF - TOWER_SIZE + 1},
                {OUTER_HALF - TOWER_SIZE + 1, OUTER_HALF - TOWER_SIZE + 1}
        };
        for (int[] c : corners) {
            buildTower(c[0], surfaceY, c[1], TOWER_SIZE, TOWER_HEIGHT);
        }
    }

    private void buildMidWallTowers() {
        plugin.getLogger().info("[Citadel] Building mid-wall towers...");
        int halfT = TOWER_SIZE / 2;
        // Mid-points of each outer wall
        buildTower(-halfT, surfaceY, -OUTER_HALF - 2, TOWER_SIZE, TOWER_HEIGHT - 5);
        buildTower(-halfT, surfaceY, OUTER_HALF - TOWER_SIZE + 3, TOWER_SIZE, TOWER_HEIGHT - 5);
        buildTower(-OUTER_HALF - 2, surfaceY, -halfT, TOWER_SIZE, TOWER_HEIGHT - 5);
        buildTower(OUTER_HALF - TOWER_SIZE + 3, surfaceY, -halfT, TOWER_SIZE, TOWER_HEIGHT - 5);
    }

    private void buildTower(int tx, int ty, int tz, int size, int height) {
        for (int y = 0; y < height; y++) {
            Material mat = (y % 4 == 0) ? WALL_ACCENT : WALL_PRIMARY;
            // Hollow square
            for (int x = 0; x < size; x++) {
                for (int z = 0; z < size; z++) {
                    boolean edge = (x == 0 || x == size - 1 || z == 0 || z == size - 1);
                    if (edge) {
                        setBlock(tx + x, ty + y, tz + z, mat);
                    } else if (y % FLOOR_HEIGHT == 0) {
                        // Floors
                        setBlock(tx + x, ty + y, tz + z, FLOOR_PRIMARY);
                    }
                }
            }
        }
        // Pointed roof / turret top
        for (int r = size / 2; r >= 0; r--) {
            int y = ty + height + (size / 2 - r);
            int cx = tx + size / 2;
            int cz = tz + size / 2;
            for (int x = -r; x <= r; x++) {
                for (int z = -r; z <= r; z++) {
                    setBlock(cx + x, y, cz + z, WALL_SECONDARY);
                }
            }
        }
        // Soul lantern at peak
        setBlock(tx + size / 2, ty + height + size / 2 + 1, tz + size / 2, SOUL_LANTERN);
        // Window slits
        for (int y = 3; y < height - 2; y += FLOOR_HEIGHT) {
            setBlock(tx + size / 2, ty + y, tz, Material.AIR);           // north slit
            setBlock(tx + size / 2, ty + y, tz + size - 1, Material.AIR); // south slit
            setBlock(tx, ty + y, tz + size / 2, Material.AIR);           // west slit
            setBlock(tx + size - 1, ty + y, tz + size / 2, Material.AIR); // east slit
        }
    }

    // ── Grand south gate ──────────────────────────────────────

    private void buildGrandSouthGate() {
        plugin.getLogger().info("[Citadel] Building grand south gate...");
        int base = surfaceY;
        int gateHalfW = GATE_WIDTH / 2;  // 5
        int gz = OUTER_HALF;

        // Clear gate opening in south wall
        for (int x = -gateHalfW + 1; x <= gateHalfW - 1; x++) {
            for (int y = base + 1; y < base + GATE_HEIGHT; y++) {
                setBlock(x, y, gz, Material.AIR);
            }
        }

        // Gate pillars (thicker, with accent bands)
        for (int y = base; y <= base + GATE_HEIGHT + 3; y++) {
            Material mat = (y % 3 == 0) ? DARK_ACCENT : PILLAR;
            // Left pillar (3 wide)
            for (int dx = 0; dx < 3; dx++) {
                setBlock(-gateHalfW - 1 + dx, y, gz, mat);
                setBlock(-gateHalfW - 1 + dx, y, gz + 1, mat);
            }
            // Right pillar
            for (int dx = 0; dx < 3; dx++) {
                setBlock(gateHalfW - 1 + dx, y, gz, mat);
                setBlock(gateHalfW - 1 + dx, y, gz + 1, mat);
            }
        }

        // Archway over gate
        for (int x = -gateHalfW + 1; x <= gateHalfW - 1; x++) {
            for (int dy = 0; dy < 3; dy++) {
                setBlock(x, base + GATE_HEIGHT + dy, gz, WALL_ACCENT);
            }
        }

        // Portcullis: iron bars in gate opening (decorative, not blocking)
        for (int x = -gateHalfW + 1; x <= gateHalfW - 1; x++) {
            setBlock(x, base + GATE_HEIGHT - 1, gz, IRON_BARS);
            setBlock(x, base + GATE_HEIGHT - 2, gz, IRON_BARS);
        }

        // Soul fire braziers flanking gate
        setBlock(-gateHalfW - 2, base + GATE_HEIGHT + 4, gz, SOUL_FIRE);
        setBlock(gateHalfW + 2, base + GATE_HEIGHT + 4, gz, SOUL_FIRE);

        // Chains hanging from archway
        for (int x = -gateHalfW + 2; x <= gateHalfW - 2; x += 3) {
            for (int dy = 1; dy <= 3; dy++) {
                setBlock(x, base + GATE_HEIGHT - 2 - dy, gz, CHAIN_MAT);
            }
        }

        // Banner spots (use purple wool as placeholder)
        for (int dy = 0; dy < 4; dy++) {
            setBlock(-gateHalfW - 1, base + GATE_HEIGHT - dy, gz - 1, Material.PURPLE_WOOL);
            setBlock(gateHalfW + 1, base + GATE_HEIGHT - dy, gz - 1, Material.PURPLE_WOOL);
        }

        // Gate floor: polished blackstone walkway
        for (int x = -gateHalfW; x <= gateHalfW; x++) {
            for (int dz = -2; dz <= 2; dz++) {
                setBlock(x, base, gz + dz, FLOOR_ACCENT);
            }
        }
    }

    // ── Courtyard ─────────────────────────────────────────────

    private void buildCourtyard() {
        plugin.getLogger().info("[Citadel] Building courtyard...");
        int base = surfaceY;

        // Floor: decorative pattern between outer walls and keep
        for (int x = -OUTER_HALF + 2; x <= OUTER_HALF - 2; x++) {
            for (int z = -OUTER_HALF + 2; z <= OUTER_HALF - 2; z++) {
                // Skip keep area
                if (Math.abs(x) <= KEEP_HALF + 2 && Math.abs(z) <= KEEP_HALF + 2) continue;

                Material mat;
                int dist = Math.abs(x) + Math.abs(z);
                if (dist % 7 == 0) mat = GLOW_BLOCK;
                else if ((x + z) % 3 == 0) mat = FLOOR_ACCENT;
                else mat = FLOOR_PRIMARY;
                setBlock(x, base, z, mat);

                // Clear above courtyard
                for (int y = base + 1; y <= base + 4; y++) {
                    Block b = world.getBlockAt(x, y, z);
                    if (b.getType() != Material.AIR) b.setType(Material.AIR);
                }
            }
        }

        // Courtyard pillars with soul lanterns
        for (int x = -OUTER_HALF + 10; x <= OUTER_HALF - 10; x += 15) {
            for (int z = -OUTER_HALF + 10; z <= OUTER_HALF - 10; z += 15) {
                if (Math.abs(x) <= KEEP_HALF + 5 && Math.abs(z) <= KEEP_HALF + 5) continue;
                for (int y = 0; y <= 5; y++) {
                    setBlock(x, base + y, z, PILLAR);
                }
                setBlock(x, base + 6, z, SOUL_LANTERN);
            }
        }
    }

    // ── Inner keep ────────────────────────────────────────────

    private void buildInnerKeep() {
        plugin.getLogger().info("[Citadel] Building inner keep...");
        int base = surfaceY;
        int top = base + KEEP_WALL_H;

        for (int y = base; y <= top; y++) {
            Material wallMat = (y % 4 == 0) ? WALL_ACCENT :
                    (y % 4 == 2) ? WALL_SECONDARY : WALL_PRIMARY;
            for (int x = -KEEP_HALF; x <= KEEP_HALF; x++) {
                setBlock(x, y, -KEEP_HALF, wallMat);
                setBlock(x, y, KEEP_HALF, wallMat);
            }
            for (int z = -KEEP_HALF; z <= KEEP_HALF; z++) {
                setBlock(-KEEP_HALF, y, z, wallMat);
                setBlock(KEEP_HALF, y, z, wallMat);
            }
        }

        // Crenellations
        for (int x = -KEEP_HALF; x <= KEEP_HALF; x += 2) {
            setBlock(x, top + 1, -KEEP_HALF, WALL_SECONDARY);
            setBlock(x, top + 1, KEEP_HALF, WALL_SECONDARY);
        }
        for (int z = -KEEP_HALF; z <= KEEP_HALF; z += 2) {
            setBlock(-KEEP_HALF, top + 1, z, WALL_SECONDARY);
            setBlock(KEEP_HALF, top + 1, z, WALL_SECONDARY);
        }

        // Keep entrance (south face, centered, 5 wide x 7 tall)
        for (int x = -2; x <= 2; x++) {
            for (int y = base + 1; y <= base + 7; y++) {
                setBlock(x, y, KEEP_HALF, Material.AIR);
            }
        }
        // Archway
        for (int x = -3; x <= 3; x++) {
            setBlock(x, base + 8, KEEP_HALF, WALL_ACCENT);
        }
    }

    // ── Keep floor levels ─────────────────────────────────────

    private void buildKeepFloors() {
        plugin.getLogger().info("[Citadel] Building keep floors...");
        int base = surfaceY;

        for (int floor = 0; floor < KEEP_FLOORS; floor++) {
            int floorY = base + (floor * FLOOR_HEIGHT);

            // Lay floor
            for (int x = -KEEP_HALF + 1; x <= KEEP_HALF - 1; x++) {
                for (int z = -KEEP_HALF + 1; z <= KEEP_HALF - 1; z++) {
                    Material mat = ((x + z) % 2 == 0) ? FLOOR_PRIMARY : FLOOR_ACCENT;
                    setBlock(x, floorY, z, mat);
                }
            }

            // Clear space above floor
            for (int x = -KEEP_HALF + 1; x <= KEEP_HALF - 1; x++) {
                for (int z = -KEEP_HALF + 1; z <= KEEP_HALF - 1; z++) {
                    for (int y = floorY + 1; y < floorY + FLOOR_HEIGHT; y++) {
                        setBlock(x, y, z, Material.AIR);
                    }
                }
            }

            // Pillars at regular intervals
            for (int x = -KEEP_HALF + 5; x <= KEEP_HALF - 5; x += 10) {
                for (int z = -KEEP_HALF + 5; z <= KEEP_HALF - 5; z += 10) {
                    for (int y = floorY; y < floorY + FLOOR_HEIGHT; y++) {
                        setBlock(x, y, z, PILLAR);
                    }
                }
            }

            // Ceiling detail (underside of next floor)
            if (floor < KEEP_FLOORS - 1) {
                int ceilY = floorY + FLOOR_HEIGHT - 1;
                for (int x = -KEEP_HALF + 1; x <= KEEP_HALF - 1; x += 5) {
                    for (int z = -KEEP_HALF + 1; z <= KEEP_HALF - 1; z += 5) {
                        setBlock(x, ceilY, z, SOUL_LANTERN);
                    }
                }
            }

            // Build rooms on this floor
            buildFloorRooms(floor, floorY);
        }
    }

    private void buildFloorRooms(int floor, int floorY) {
        switch (floor) {
            case 0 -> { // Ground floor: great hall + armory + vault
                buildRoom("Great Hall", -KEEP_HALF + 2, floorY, -KEEP_HALF + 2,
                        KEEP_HALF - 2, floorY + FLOOR_HEIGHT - 1, -2, false);
                buildRoom("Armory", -KEEP_HALF + 2, floorY, 3,
                        -5, floorY + FLOOR_HEIGHT - 1, KEEP_HALF - 2, true);
                buildRoom("Vault", 5, floorY, 3,
                        KEEP_HALF - 2, floorY + FLOOR_HEIGHT - 1, KEEP_HALF - 2, true);
            }
            case 1 -> { // Library + enchanting room
                buildRoom("Library", -KEEP_HALF + 2, floorY, -KEEP_HALF + 2,
                        -2, floorY + FLOOR_HEIGHT - 1, KEEP_HALF - 2, true);
                buildRoom("Enchanting Chamber", 2, floorY, -KEEP_HALF + 2,
                        KEEP_HALF - 2, floorY + FLOOR_HEIGHT - 1, KEEP_HALF - 2, true);
            }
            case 2 -> { // Throne room
                buildRoom("Throne Room", -KEEP_HALF + 2, floorY, -KEEP_HALF + 2,
                        KEEP_HALF - 2, floorY + FLOOR_HEIGHT - 1, KEEP_HALF - 2, false);
                // Throne
                setBlock(0, floorY + 1, -KEEP_HALF + 4, Material.POLISHED_BLACKSTONE_STAIRS);
                setBlock(0, floorY + 1, -KEEP_HALF + 3, Material.POLISHED_BLACKSTONE_SLAB);
                setBlock(-1, floorY + 1, -KEEP_HALF + 4, DARK_ACCENT);
                setBlock(1, floorY + 1, -KEEP_HALF + 4, DARK_ACCENT);
                for (int dy = 1; dy <= 4; dy++) {
                    setBlock(-2, floorY + dy, -KEEP_HALF + 4, PILLAR);
                    setBlock(2, floorY + dy, -KEEP_HALF + 4, PILLAR);
                }
            }
            case 3 -> { // Observatory / ritual room
                buildRoom("Observatory", -KEEP_HALF + 2, floorY, -KEEP_HALF + 2,
                        KEEP_HALF - 2, floorY + FLOOR_HEIGHT - 1, KEEP_HALF - 2, false);
                // Central crystal formation
                setBlock(0, floorY + 1, 0, CRYSTAL);
                setBlock(0, floorY + 2, 0, CRYSTAL);
                setBlock(0, floorY + 3, 0, Material.END_ROD);
                for (int dx = -1; dx <= 1; dx += 2) {
                    for (int dz = -1; dz <= 1; dz += 2) {
                        setBlock(dx, floorY + 1, dz, CRYSTAL);
                    }
                }
            }
        }
    }

    private void buildRoom(String name, int x1, int y1, int z1, int x2, int y2, int z2, boolean addLoot) {
        // Walls around room perimeter (only internal dividers)
        for (int y = y1 + 1; y <= y2; y++) {
            for (int x = x1; x <= x2; x++) {
                setBlock(x, y, z1, WALL_SECONDARY);
                setBlock(x, y, z2, WALL_SECONDARY);
            }
            for (int z = z1; z <= z2; z++) {
                setBlock(x1, y, z, WALL_SECONDARY);
                setBlock(x2, y, z, WALL_SECONDARY);
            }
        }

        // Doorway (centered on longest wall, 2 wide x 3 tall)
        int midX = (x1 + x2) / 2;
        int midZ = (z1 + z2) / 2;
        for (int dy = 1; dy <= 3; dy++) {
            setBlock(midX, y1 + dy, z2, Material.AIR);
            setBlock(midX + 1, y1 + dy, z2, Material.AIR);
            setBlock(x1, y1 + dy, midZ, Material.AIR);
            setBlock(x1, y1 + dy, midZ + 1, Material.AIR);
        }

        // Lighting
        setBlock(midX, y2 - 1, midZ, SOUL_LANTERN);

        // Loot chests if applicable
        if (addLoot) {
            setBlock(x1 + 2, y1 + 1, z1 + 2, Material.CHEST);
            setBlock(x2 - 2, y1 + 1, z2 - 2, Material.CHEST);
        }
    }

    // ── Grand staircase (keep entrance → upper floors) ────────

    private void buildGrandStaircase() {
        plugin.getLogger().info("[Citadel] Building grand staircase...");
        int base = surfaceY;

        // Spiral staircase in the keep's east side
        int sx = KEEP_HALF - 6;
        int sz = 0;
        int currentY = base;

        for (int floor = 0; floor < KEEP_FLOORS - 1; floor++) {
            int startY = base + (floor * FLOOR_HEIGHT);
            int endY = startY + FLOOR_HEIGHT;

            // Build a straight stair run along east wall
            for (int step = 0; step < FLOOR_HEIGHT; step++) {
                int z = sz - 3 + step;
                setBlock(sx, startY + step + 1, z, STAIRS_MAT);
                setBlock(sx + 1, startY + step + 1, z, STAIRS_MAT);
                // Railing
                setBlock(sx - 1, startY + step + 2, z, IRON_BARS);
                // Clear headroom
                for (int dy = 1; dy <= 3; dy++) {
                    setBlock(sx, startY + step + 1 + dy, z, Material.AIR);
                    setBlock(sx + 1, startY + step + 1 + dy, z, Material.AIR);
                }
            }

            // Landing
            for (int x = sx - 1; x <= sx + 2; x++) {
                for (int z2 = sz - 4; z2 <= sz - 3; z2++) {
                    setBlock(x, endY, z2, FLOOR_PRIMARY);
                }
            }

            // Opening in the floor above
            for (int x = sx; x <= sx + 1; x++) {
                for (int z2 = sz - 3; z2 <= sz + FLOOR_HEIGHT - 4; z2++) {
                    setBlock(x, endY, z2, Material.AIR);
                }
            }
        }
    }

    // ── Arena (underground bedrock arena) ─────────────────────

    private void buildArena() {
        plugin.getLogger().info("[Citadel] Building arena...");
        int arenaY = surfaceY - ARENA_DEPTH;

        // Clear arena space (cylindrical)
        for (int x = -ARENA_RADIUS; x <= ARENA_RADIUS; x++) {
            for (int z = -ARENA_RADIUS; z <= ARENA_RADIUS; z++) {
                if (x * x + z * z > ARENA_RADIUS * ARENA_RADIUS) continue;
                // Bedrock floor
                setBlock(x, arenaY, z, BEDROCK);
                // Clear space above
                for (int y = arenaY + 1; y < surfaceY; y++) {
                    setBlock(x, y, z, Material.AIR);
                }
            }
        }

        // Arena walls (cylinder)
        for (int y = arenaY; y <= arenaY + ARENA_DEPTH + 5; y++) {
            for (int angle = 0; angle < 360; angle++) {
                double rad = Math.toRadians(angle);
                int x = (int) Math.round(ARENA_RADIUS * Math.cos(rad));
                int z = (int) Math.round(ARENA_RADIUS * Math.sin(rad));
                setBlock(x, y, z, BEDROCK);
            }
        }

        // Central podium
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                if (x * x + z * z <= 9) {
                    setBlock(x, arenaY + 1, z, DARK_ACCENT);
                }
            }
        }
        setBlock(0, arenaY + 2, 0, Material.LODESTONE);

        // Soul fire torches around arena perimeter
        for (int angle = 0; angle < 360; angle += 20) {
            double rad = Math.toRadians(angle);
            int x = (int) Math.round((ARENA_RADIUS - 2) * Math.cos(rad));
            int z = (int) Math.round((ARENA_RADIUS - 2) * Math.sin(rad));
            setBlock(x, arenaY + 1, z, SOUL_FIRE);
        }

        // Glow blocks in floor pattern
        for (int x = -ARENA_RADIUS + 2; x <= ARENA_RADIUS - 2; x += 5) {
            for (int z = -ARENA_RADIUS + 2; z <= ARENA_RADIUS - 2; z += 5) {
                if (x * x + z * z < (ARENA_RADIUS - 2) * (ARENA_RADIUS - 2)) {
                    setBlock(x, arenaY, z, GLOW_BLOCK);
                }
            }
        }
    }

    // ── Arena barriers (invisible walls + spikes) ─────────────

    private void buildArenaBarriers() {
        plugin.getLogger().info("[Citadel] Building arena barriers...");
        int arenaY = surfaceY - ARENA_DEPTH;

        // Barrier blocks above arena walls to confine dragon
        for (int y = arenaY + ARENA_DEPTH + 6; y <= arenaY + ARENA_DEPTH + 25; y++) {
            for (int angle = 0; angle < 360; angle++) {
                double rad = Math.toRadians(angle);
                int x = (int) Math.round((ARENA_RADIUS + 1) * Math.cos(rad));
                int z = (int) Math.round((ARENA_RADIUS + 1) * Math.sin(rad));
                setBlock(x, y, z, BARRIER);
            }
        }

        // Barrier ceiling
        for (int x = -ARENA_RADIUS - 1; x <= ARENA_RADIUS + 1; x++) {
            for (int z = -ARENA_RADIUS - 1; z <= ARENA_RADIUS + 1; z++) {
                if (x * x + z * z <= (ARENA_RADIUS + 1) * (ARENA_RADIUS + 1)) {
                    setBlock(x, arenaY + ARENA_DEPTH + 25, z, BARRIER);
                }
            }
        }

        // Visible decorative spikes around inner perimeter
        for (int angle = 0; angle < 360; angle += 15) {
            double rad = Math.toRadians(angle);
            int x = (int) Math.round((ARENA_RADIUS - 1) * Math.cos(rad));
            int z = (int) Math.round((ARENA_RADIUS - 1) * Math.sin(rad));
            int spikeH = 3 + random.nextInt(4);
            for (int dy = 1; dy <= spikeH; dy++) {
                setBlock(x, arenaY + dy, z, END_STONE);
            }
            setBlock(x, arenaY + spikeH + 1, z, Material.POINTED_DRIPSTONE);
        }
    }

    // ── Stairwell from keep ground floor to arena ─────────────

    private void buildArenaStairwell() {
        plugin.getLogger().info("[Citadel] Building arena stairwell...");
        int base = surfaceY;
        int arenaY = surfaceY - ARENA_DEPTH;

        // Stairwell: 4 wide, spiraling down from (0, base, -KEEP_HALF+3)
        int sx = -2;
        int ex = 2;
        int sz = -KEEP_HALF + 3;

        // Cut opening in ground floor
        for (int x = sx; x <= ex; x++) {
            for (int z = sz; z <= sz + 3; z++) {
                setBlock(x, base, z, Material.AIR);
            }
        }

        // Build descending staircase
        int stairZ = sz;
        for (int y = base; y > arenaY; y--) {
            int step = (base - y);
            int zOff = step % 8;
            boolean goingNorth = (step / 8) % 2 == 0;

            if (goingNorth) {
                for (int x = sx; x <= ex; x++) {
                    setBlock(x, y, sz + zOff, STAIRS_MAT);
                    // Walls
                    setBlock(sx - 1, y, sz + zOff, WALL_PRIMARY);
                    setBlock(ex + 1, y, sz + zOff, WALL_PRIMARY);
                    // Clear headroom
                    for (int dy = 1; dy <= 3; dy++) {
                        setBlock(x, y + dy, sz + zOff, Material.AIR);
                    }
                }
            } else {
                for (int x = sx; x <= ex; x++) {
                    setBlock(x, y, sz + 7 - zOff, STAIRS_MAT);
                    setBlock(sx - 1, y, sz + 7 - zOff, WALL_PRIMARY);
                    setBlock(ex + 1, y, sz + 7 - zOff, WALL_PRIMARY);
                    for (int dy = 1; dy <= 3; dy++) {
                        setBlock(x, y + dy, sz + 7 - zOff, Material.AIR);
                    }
                }
            }

            // Lighting every 4 steps
            if (step % 4 == 0) {
                setBlock(sx - 1, y + 2, sz + (goingNorth ? zOff : 7 - zOff), SOUL_LANTERN);
            }
        }

        // Doorway from stairwell into arena
        for (int x = sx; x <= ex; x++) {
            for (int dy = 1; dy <= 4; dy++) {
                setBlock(x, arenaY + dy, ARENA_RADIUS - 1, Material.AIR);
            }
        }
    }

    // ── Wing corridors (east and west of keep) ────────────────

    private void buildWingCorridors() {
        plugin.getLogger().info("[Citadel] Building wing corridors...");
        int base = surfaceY;
        int corridorH = 5;

        // East wing corridor
        for (int z = -KEEP_HALF + 5; z <= KEEP_HALF - 5; z++) {
            for (int y = base; y <= base + corridorH; y++) {
                setBlock(KEEP_HALF + 1, y, z, WALL_PRIMARY);
                setBlock(KEEP_HALF + 5, y, z, WALL_PRIMARY);
                if (y == base) {
                    for (int x = KEEP_HALF + 2; x <= KEEP_HALF + 4; x++) {
                        setBlock(x, y, z, FLOOR_PRIMARY);
                    }
                } else {
                    for (int x = KEEP_HALF + 2; x <= KEEP_HALF + 4; x++) {
                        setBlock(x, y, z, Material.AIR);
                    }
                }
            }
            // Ceiling
            for (int x = KEEP_HALF + 1; x <= KEEP_HALF + 5; x++) {
                setBlock(x, base + corridorH + 1, z, WALL_SECONDARY);
            }
        }

        // West wing corridor (mirror)
        for (int z = -KEEP_HALF + 5; z <= KEEP_HALF - 5; z++) {
            for (int y = base; y <= base + corridorH; y++) {
                setBlock(-KEEP_HALF - 1, y, z, WALL_PRIMARY);
                setBlock(-KEEP_HALF - 5, y, z, WALL_PRIMARY);
                if (y == base) {
                    for (int x = -KEEP_HALF - 4; x <= -KEEP_HALF - 2; x++) {
                        setBlock(x, y, z, FLOOR_PRIMARY);
                    }
                } else {
                    for (int x = -KEEP_HALF - 4; x <= -KEEP_HALF - 2; x++) {
                        setBlock(x, y, z, Material.AIR);
                    }
                }
            }
            for (int x = -KEEP_HALF - 5; x <= -KEEP_HALF - 1; x++) {
                setBlock(x, base + corridorH + 1, z, WALL_SECONDARY);
            }
        }

        // Connect corridors to keep with doorways
        for (int dy = 1; dy <= 3; dy++) {
            setBlock(KEEP_HALF, base + dy, 0, Material.AIR);
            setBlock(-KEEP_HALF, base + dy, 0, Material.AIR);
        }

        // Corridor lighting
        for (int z = -KEEP_HALF + 7; z <= KEEP_HALF - 7; z += 5) {
            setBlock(KEEP_HALF + 3, base + corridorH, z, SOUL_LANTERN);
            setBlock(-KEEP_HALF - 3, base + corridorH, z, SOUL_LANTERN);
        }
    }

    // ── Wing rooms (off corridors) ────────────────────────────

    private void buildWingRooms() {
        plugin.getLogger().info("[Citadel] Building wing rooms...");
        int base = surfaceY;

        // East wing rooms
        String[] eastRooms = {"Barracks", "Alchemy Lab", "War Room", "Prison"};
        for (int i = 0; i < eastRooms.length; i++) {
            int rz = -KEEP_HALF + 8 + (i * 12);
            int rx = KEEP_HALF + 6;
            buildWingRoom(eastRooms[i], rx, base, rz, rx + 10, base + 5, rz + 10);
            // Doorway into corridor
            for (int dy = 1; dy <= 3; dy++) {
                setBlock(KEEP_HALF + 5, base + dy, rz + 5, Material.AIR);
            }
        }

        // West wing rooms
        String[] westRooms = {"Chapel", "Treasury", "Archive", "Dungeon"};
        for (int i = 0; i < westRooms.length; i++) {
            int rz = -KEEP_HALF + 8 + (i * 12);
            int rx = -KEEP_HALF - 16;
            buildWingRoom(westRooms[i], rx, base, rz, rx + 10, base + 5, rz + 10);
            // Doorway into corridor
            for (int dy = 1; dy <= 3; dy++) {
                setBlock(-KEEP_HALF - 5, base + dy, rz + 5, Material.AIR);
            }
        }
    }

    private void buildWingRoom(String name, int x1, int y1, int z1, int x2, int y2, int z2) {
        // Walls
        for (int y = y1; y <= y2; y++) {
            for (int x = x1; x <= x2; x++) {
                setBlock(x, y, z1, WALL_PRIMARY);
                setBlock(x, y, z2, WALL_PRIMARY);
            }
            for (int z = z1; z <= z2; z++) {
                setBlock(x1, y, z, WALL_PRIMARY);
                setBlock(x2, y, z, WALL_PRIMARY);
            }
        }
        // Floor
        for (int x = x1 + 1; x < x2; x++) {
            for (int z = z1 + 1; z < z2; z++) {
                setBlock(x, y1, z, FLOOR_PRIMARY);
                // Clear interior
                for (int y = y1 + 1; y < y2; y++) {
                    setBlock(x, y, z, Material.AIR);
                }
            }
        }
        // Ceiling
        for (int x = x1; x <= x2; x++) {
            for (int z = z1; z <= z2; z++) {
                setBlock(x, y2, z, WALL_SECONDARY);
            }
        }
        // Light
        int mx = (x1 + x2) / 2;
        int mz = (z1 + z2) / 2;
        setBlock(mx, y2 - 1, mz, SOUL_LANTERN);

        // Chests
        setBlock(x1 + 2, y1 + 1, z1 + 2, Material.CHEST);
        setBlock(x2 - 2, y1 + 1, z2 - 2, Material.CHEST);
    }

    // ── Decorations ───────────────────────────────────────────

    private void buildDecorations() {
        plugin.getLogger().info("[Citadel] Adding decorations...");
        int base = surfaceY;

        // End rods on tower tops and keep corners
        for (int[] pos : new int[][]{{-KEEP_HALF, KEEP_HALF}, {KEEP_HALF, KEEP_HALF},
                {-KEEP_HALF, -KEEP_HALF}, {KEEP_HALF, -KEEP_HALF}}) {
            for (int dy = 0; dy < 3; dy++) {
                setBlock(pos[0], base + KEEP_WALL_H + 2 + dy, pos[1], Material.END_ROD);
            }
        }

        // Amethyst clusters scattered in courtyard
        for (int i = 0; i < 20; i++) {
            int x = -OUTER_HALF + 5 + random.nextInt(OUTER_HALF * 2 - 10);
            int z = -OUTER_HALF + 5 + random.nextInt(OUTER_HALF * 2 - 10);
            if (Math.abs(x) < KEEP_HALF + 5 && Math.abs(z) < KEEP_HALF + 5) continue;
            setBlock(x, base + 1, z, Material.AMETHYST_CLUSTER);
        }

        // Banners along outer walls (purple wool as placeholders)
        for (int x = -OUTER_HALF + 7; x <= OUTER_HALF - 7; x += 14) {
            for (int dy = 0; dy < 3; dy++) {
                setBlock(x, base + OUTER_WALL_H - dy, -OUTER_HALF + 1, Material.PURPLE_WOOL);
                setBlock(x, base + OUTER_WALL_H - dy, OUTER_HALF - 1, Material.PURPLE_WOOL);
            }
        }
    }

    // ── Approach path (from tp point to gate) ─────────────────

    private void buildApproachPath() {
        plugin.getLogger().info("[Citadel] Building approach path...");
        int base = surfaceY;

        // Path from (0, base, 85) to the south gate (0, base, OUTER_HALF)
        for (int z = OUTER_HALF; z <= 90; z++) {
            for (int x = -3; x <= 3; x++) {
                // Ensure ground under path
                int terrainY = findTerrainY(x, z);
                for (int y = terrainY; y <= base; y++) {
                    setBlock(x, y, z, (Math.abs(x) <= 1) ? FLOOR_ACCENT : FLOOR_PRIMARY);
                }
                // Pillars flanking path
                if (Math.abs(x) == 3 && z % 8 == 0) {
                    for (int dy = 1; dy <= 4; dy++) {
                        setBlock(x, base + dy, z, PILLAR);
                    }
                    setBlock(x, base + 5, z, SOUL_LANTERN);
                }
            }
        }
    }

    // ── Block helpers ─────────────────────────────────────────

    private void setBlock(int x, int y, int z, Material material) {
        world.getBlockAt(x, y, z).setType(material);
    }

    private int findSurface() {
        // Find surface Y at (0, ?, 0)
        for (int y = 120; y > 1; y--) {
            if (world.getBlockAt(0, y, 0).getType().isSolid()) {
                return y;
            }
        }
        return 55;
    }

    private int findTerrainY(int x, int z) {
        for (int y = 120; y > 1; y--) {
            if (world.getBlockAt(x, y, z).getType().isSolid()) {
                return y;
            }
        }
        return 50;
    }
}

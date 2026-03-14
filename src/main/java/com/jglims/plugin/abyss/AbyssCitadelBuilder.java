package com.jglims.plugin.abyss;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.legendary.*;
import com.jglims.plugin.powerups.PowerUpManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

/**
 * Abyssal Citadel — Phase 2 Mega Build.
 * 
 * Footprint: ~141x141 blocks, height ~80 blocks.
 * Outer Walls: 140x140 perimeter, 15 blocks tall with walkways.
 * 8 Corner/Mid Towers: 7-block radius, 70 blocks tall with cone roofs.
 * Inner Keep: 61x61, 50 blocks tall, 4 floors of 12 blocks each.
 * 4 Wings connecting outer wall to keep (N/S/E/W), each 15 wide x 40 long, 2 floors.
 * Bedrock Arena on keep roof: 30-block radius circle.
 * 30+ rooms with endgame loot (MYTHIC/ABYSSAL weapons, power-ups, enchant books).
 * Wither Skeleton + Enderman spawners throughout.
 * Grand courtyard between outer wall and keep with gardens and statues.
 */
public class AbyssCitadelBuilder {

    private final JGlimsPlugin plugin;
    private final Random random = new Random();

    private static final Material WALL = Material.DEEPSLATE_BRICKS;
    private static final Material WALL2 = Material.POLISHED_DEEPSLATE;
    private static final Material FLOOR = Material.DEEPSLATE_TILES;
    private static final Material PILLAR = Material.POLISHED_BLACKSTONE_BRICKS;
    private static final Material ACCENT = Material.PURPUR_BLOCK;
    private static final Material ACCENT2 = Material.PURPUR_PILLAR;
    private static final Material DARK = Material.OBSIDIAN;
    private static final Material LIGHT = Material.END_ROD;
    private static final Material GLASS = Material.MAGENTA_STAINED_GLASS;
    private static final Material BRICK = Material.END_STONE_BRICKS;
    private static final Material FENCE = Material.NETHER_BRICK_FENCE;
    private static final Material CRYING = Material.CRYING_OBSIDIAN;
    private static final Material BEDROCK = Material.BEDROCK;

    public AbyssCitadelBuilder(JGlimsPlugin plugin) {
        this.plugin = plugin;
    }

    public void buildCitadel(World world) {
        int baseY = world.getHighestBlockYAt(0, 0);
        if (baseY < 30) baseY = 55;
        baseY += 1;
        plugin.getLogger().info("[Citadel] Building at Y=" + baseY + ", clearing 141x141 area...");
        final int y = baseY;

        clearArea(world, y);
        buildOuterWalls(world, y);
        buildTowers(world, y);
        buildInnerKeep(world, y);
        buildWings(world, y);
        buildArena(world, y);
        buildCourtyard(world, y);
        buildKeepRooms(world, y);
        buildWingRooms(world, y);
        buildGateEntrance(world, y);

        plugin.getLogger().info("[Citadel] Complete! 141x141, ~80 blocks tall, 30+ rooms");
    }

    private void clearArea(World world, int baseY) {
        for (int x = -75; x <= 75; x++) {
            for (int z = -75; z <= 75; z++) {
                for (int cy = baseY; cy < baseY + 85; cy++) world.getBlockAt(x, cy, z).setType(Material.AIR);
                world.getBlockAt(x, baseY - 1, z).setType(FLOOR);
                world.getBlockAt(x, baseY - 2, z).setType(WALL);
            }
        }
    }

    // ── Outer Walls: 140x140, 15 tall ──
    private void buildOuterWalls(World world, int y) {
        int h = 70; // half width
        int wallH = 15;
        for (int wy = y; wy < y + wallH; wy++) {
            int relY = wy - y;
            for (int i = -h; i <= h; i++) {
                Material mat = (relY == 0 || relY == wallH - 1) ? WALL2 : (relY % 4 == 0 ? ACCENT : WALL);
                world.getBlockAt(i, wy, -h).setType(mat);
                world.getBlockAt(i, wy, h).setType(mat);
                world.getBlockAt(-h, wy, i).setType(mat);
                world.getBlockAt(h, wy, i).setType(mat);
            }
        }
        // Battlements + lights
        for (int i = -h; i <= h; i += 2) {
            world.getBlockAt(i, y + wallH, -h).setType(WALL);
            world.getBlockAt(i, y + wallH, h).setType(WALL);
            world.getBlockAt(-h, y + wallH, i).setType(WALL);
            world.getBlockAt(h, y + wallH, i).setType(WALL);
        }
        for (int i = -h; i <= h; i += 8) {
            world.getBlockAt(i, y + wallH + 1, -h).setType(LIGHT);
            world.getBlockAt(i, y + wallH + 1, h).setType(LIGHT);
            world.getBlockAt(-h, y + wallH + 1, i).setType(LIGHT);
            world.getBlockAt(h, y + wallH + 1, i).setType(LIGHT);
        }
        // Walkway on top of outer wall
        for (int i = -h + 1; i < h; i++) {
            world.getBlockAt(i, y + wallH - 1, -h + 1).setType(FLOOR);
            world.getBlockAt(i, y + wallH - 1, h - 1).setType(FLOOR);
            world.getBlockAt(-h + 1, y + wallH - 1, i).setType(FLOOR);
            world.getBlockAt(h - 1, y + wallH - 1, i).setType(FLOOR);
        }
    }

    // ── 8 Towers: 4 corners + 4 midpoints ──
    private void buildTowers(World world, int y) {
        int h = 70;
        int[][] positions = {
            {-h, -h}, {h, -h}, {-h, h}, {h, h},  // corners
            {0, -h}, {0, h}, {-h, 0}, {h, 0}       // midpoints
        };
        for (int[] pos : positions) buildSingleTower(world, pos[0], y, pos[1], 7, 70);
    }

    private void buildSingleTower(World world, int cx, int baseY, int cz, int r, int height) {
        for (int wy = baseY; wy < baseY + height; wy++) {
            int relY = wy - baseY;
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    double dist = Math.sqrt(dx * dx + dz * dz);
                    if (dist <= r + 0.5) {
                        if (dist >= r - 0.5) {
                            Material mat = relY % 6 == 0 ? ACCENT : WALL;
                            world.getBlockAt(cx + dx, wy, cz + dz).setType(mat);
                        } else if (relY % 15 == 0) {
                            world.getBlockAt(cx + dx, wy, cz + dz).setType(FLOOR);
                        }
                        if (dist >= r - 0.5 && relY % 5 == 3 && (dx == 0 || dz == 0) && Math.abs(dx) + Math.abs(dz) >= r - 1)
                            world.getBlockAt(cx + dx, wy, cz + dz).setType(GLASS);
                    }
                }
            }
        }
        // Cone roof
        for (int rh = 0; rh < 12; rh++) {
            double cr = r * (1.0 - rh / 12.0);
            for (int dx = (int)-cr; dx <= (int)cr; dx++)
                for (int dz = (int)-cr; dz <= (int)cr; dz++)
                    if (Math.sqrt(dx * dx + dz * dz) <= cr)
                        world.getBlockAt(cx + dx, baseY + height + rh, cz + dz).setType(ACCENT);
        }
        for (int h = 0; h < 6; h++) world.getBlockAt(cx, baseY + height + 12 + h, cz).setType(ACCENT2);
        world.getBlockAt(cx, baseY + height + 18, cz).setType(LIGHT);

        // Loot + spawner per tower
        placeChest(world, cx, baseY + 1, cz, 4);
        placeSpawner(world, cx + 2, baseY + 1, cz, EntityType.WITHER_SKELETON);
    }

    // ── Inner Keep: 61x61, 50 tall, 4 floors ──
    private void buildInnerKeep(World world, int y) {
        int kh = 30; // half = 30, so 61x61
        int keepH = 50;
        int floorH = 12;

        for (int wy = y; wy < y + keepH; wy++) {
            int relY = wy - y;
            for (int i = -kh; i <= kh; i++) {
                Material mat = relY % 5 == 0 ? ACCENT : WALL;
                world.getBlockAt(i, wy, -kh).setType(mat);
                world.getBlockAt(i, wy, kh).setType(mat);
                world.getBlockAt(-kh, wy, i).setType(mat);
                world.getBlockAt(kh, wy, i).setType(mat);
            }
            if (relY > 0 && relY % floorH == 0) {
                for (int x = -kh + 1; x < kh; x++)
                    for (int z = -kh + 1; z < kh; z++)
                        world.getBlockAt(x, wy, z).setType(FLOOR);
            }
        }

        // Windows
        for (int face = 0; face < 4; face++) {
            for (int w = -kh + 5; w <= kh - 5; w += 6) {
                for (int wy = 0; wy < 3; wy++) {
                    for (int fl = 0; fl < 4; fl++) {
                        int fy = y + 3 + fl * floorH + wy;
                        switch (face) {
                            case 0 -> world.getBlockAt(w, fy, -kh).setType(GLASS);
                            case 1 -> world.getBlockAt(w, fy, kh).setType(GLASS);
                            case 2 -> world.getBlockAt(-kh, fy, w).setType(GLASS);
                            case 3 -> world.getBlockAt(kh, fy, w).setType(GLASS);
                        }
                    }
                }
            }
        }

        // Central grand pillar
        for (int wy = y; wy < y + keepH; wy++) {
            for (int dx = -2; dx <= 2; dx++)
                for (int dz = -2; dz <= 2; dz++)
                    if (Math.abs(dx) + Math.abs(dz) <= 3) world.getBlockAt(dx, wy, dz).setType(PILLAR);
        }

        // Lighting per floor
        for (int fl = 0; fl < 4; fl++) {
            int fy = y + 1 + fl * floorH;
            for (int x = -kh + 4; x < kh; x += 6)
                for (int z = -kh + 4; z < kh; z += 6)
                    world.getBlockAt(x, fy + floorH - 2, z).setType(LIGHT);
        }

        // Stairs in NW corner of keep, all floors
        for (int fl = 0; fl < 3; fl++) {
            int startY = y + 1 + fl * floorH;
            for (int step = 0; step < floorH; step++) {
                int sx = -kh + 3 + (step % 5);
                int sz = -kh + 3 + (step / 5);
                world.getBlockAt(sx, startY + step, sz).setType(Material.DEEPSLATE_BRICK_STAIRS);
                world.getBlockAt(sx, startY + step + 1, sz).setType(Material.AIR);
                world.getBlockAt(sx, startY + step + 2, sz).setType(Material.AIR);
            }
        }

        // Keep entrances (4 faces)
        for (int face = 0; face < 4; face++) {
            for (int dx = -3; dx <= 3; dx++) {
                for (int dy = 0; dy < 7; dy++) {
                    switch (face) {
                        case 0 -> world.getBlockAt(dx, y + dy, -kh).setType(Material.AIR);
                        case 1 -> world.getBlockAt(dx, y + dy, kh).setType(Material.AIR);
                        case 2 -> world.getBlockAt(-kh, y + dy, dx).setType(Material.AIR);
                        case 3 -> world.getBlockAt(kh, y + dy, dx).setType(Material.AIR);
                    }
                }
            }
        }
    }

    // ── 4 Wings connecting outer wall to keep ──
    private void buildWings(World world, int y) {
        int outerH = 70, keepH = 30;
        int wingW = 7; // half-width of wing corridor
        int wingH = 12;
        // North wing: z from -outerH to -keepH
        buildWing(world, y, -wingW, wingW, -outerH + 1, -keepH, wingH);
        // South wing: z from keepH to outerH
        buildWing(world, y, -wingW, wingW, keepH, outerH - 1, wingH);
        // West wing: x from -outerH to -keepH
        buildWingZ(world, y, -outerH + 1, -keepH, -wingW, wingW, wingH);
        // East wing: x from keepH to outerH
        buildWingZ(world, y, keepH, outerH - 1, -wingW, wingW, wingH);
    }

    private void buildWing(World world, int baseY, int x1, int x2, int z1, int z2, int h) {
        // Floor, ceiling, walls
        for (int x = x1; x <= x2; x++) {
            for (int z = z1; z <= z2; z++) {
                world.getBlockAt(x, baseY, z).setType(FLOOR);
                world.getBlockAt(x, baseY + h, z).setType(FLOOR);
                for (int wy = baseY + 1; wy < baseY + h; wy++) world.getBlockAt(x, wy, z).setType(Material.AIR);
            }
        }
        for (int z = z1; z <= z2; z++) {
            for (int wy = baseY; wy <= baseY + h; wy++) {
                world.getBlockAt(x1, wy, z).setType(WALL);
                world.getBlockAt(x2, wy, z).setType(WALL);
            }
        }
        // Second floor
        for (int x = x1 + 1; x < x2; x++)
            for (int z = z1; z <= z2; z++)
                world.getBlockAt(x, baseY + h / 2, z).setType(FLOOR);
        // Lighting
        for (int z = z1 + 2; z < z2; z += 4)
            world.getBlockAt(0, baseY + h - 2, z).setType(LIGHT);
        // Spawners along the wing
        for (int z = z1 + 5; z < z2 - 5; z += 12)
            placeSpawner(world, 0, baseY + 1, z, random.nextBoolean() ? EntityType.WITHER_SKELETON : EntityType.ENDERMAN);
    }

    private void buildWingZ(World world, int baseY, int x1, int x2, int z1, int z2, int h) {
        for (int x = x1; x <= x2; x++) {
            for (int z = z1; z <= z2; z++) {
                world.getBlockAt(x, baseY, z).setType(FLOOR);
                world.getBlockAt(x, baseY + h, z).setType(FLOOR);
                for (int wy = baseY + 1; wy < baseY + h; wy++) world.getBlockAt(x, wy, z).setType(Material.AIR);
            }
        }
        for (int x = x1; x <= x2; x++) {
            for (int wy = baseY; wy <= baseY + h; wy++) {
                world.getBlockAt(x, wy, z1).setType(WALL);
                world.getBlockAt(x, wy, z2).setType(WALL);
            }
        }
        for (int x = x1 + 1; x < x2; x++)
            for (int z = z1 + 1; z < z2; z++)
                world.getBlockAt(x, baseY + h / 2, z).setType(FLOOR);
        for (int x = x1 + 2; x < x2; x += 4)
            world.getBlockAt(x, baseY + h - 2, 0).setType(LIGHT);
        for (int x = x1 + 5; x < x2 - 5; x += 12)
            placeSpawner(world, x, baseY + 1, 0, random.nextBoolean() ? EntityType.WITHER_SKELETON : EntityType.ENDERMAN);
    }

    // ── BEDROCK Arena on keep roof ──
    private void buildArena(World world, int baseY) {
        int arenaY = baseY + 50;
        int arenaR = 30;
        for (int x = -arenaR; x <= arenaR; x++) {
            for (int z = -arenaR; z <= arenaR; z++) {
                double dist = Math.sqrt(x * x + z * z);
                if (dist <= arenaR) {
                    world.getBlockAt(x, arenaY, z).setType(BEDROCK);
                    // Clear 30 blocks above arena
                    for (int dy = 1; dy <= 30; dy++) world.getBlockAt(x, arenaY + dy, z).setType(Material.AIR);
                }
                // Arena wall ring (bedrock, 5 tall)
                if (dist >= arenaR - 0.5 && dist <= arenaR + 1.5) {
                    for (int dy = 1; dy <= 5; dy++) world.getBlockAt(x, arenaY + dy, z).setType(BEDROCK);
                }
            }
        }
        // 4 obsidian pillars at cardinal points (like End)
        int[][] pillars = {{0, -arenaR + 5}, {0, arenaR - 5}, {-arenaR + 5, 0}, {arenaR - 5, 0}};
        for (int[] p : pillars) {
            for (int dy = 1; dy <= 12; dy++) world.getBlockAt(p[0], arenaY + dy, p[1]).setType(DARK);
            world.getBlockAt(p[0], arenaY + 13, p[1]).setType(CRYING);
            world.getBlockAt(p[0], arenaY + 14, p[1]).setType(LIGHT);
        }
        // Central dragon perch
        for (int dy = 1; dy <= 15; dy++) world.getBlockAt(0, arenaY + dy, 0).setType(DARK);
        world.getBlockAt(0, arenaY + 16, 0).setType(CRYING);

        // Staircase from floor 4 to arena
        int stairStart = baseY + 37; // floor 4 start
        for (int step = 0; step < 14; step++) {
            int sx = -28 + step;
            world.getBlockAt(sx, stairStart + step, -28).setType(Material.DEEPSLATE_BRICK_STAIRS);
            world.getBlockAt(sx, stairStart + step + 1, -28).setType(Material.AIR);
            world.getBlockAt(sx, stairStart + step + 2, -28).setType(Material.AIR);
        }
    }

    // ── Courtyard features ──
    private void buildCourtyard(World world, int y) {
        // Obsidian statues at 4 points between keep and outer wall
        int[][] statuePos = {{-50, 0}, {50, 0}, {0, -50}, {0, 50}};
        for (int[] sp : statuePos) {
            for (int dy = 0; dy < 10; dy++) world.getBlockAt(sp[0], y + dy, sp[1]).setType(DARK);
            world.getBlockAt(sp[0], y + 10, sp[1]).setType(CRYING);
            world.getBlockAt(sp[0], y + 11, sp[1]).setType(LIGHT);
            // Base
            for (int dx = -1; dx <= 1; dx++)
                for (int dz = -1; dz <= 1; dz++)
                    world.getBlockAt(sp[0] + dx, y, sp[1] + dz).setType(PILLAR);
        }

        // Paths from outer gate to keep (cross pattern)
        for (int i = -70; i <= 70; i++) {
            for (int w = -1; w <= 1; w++) {
                world.getBlockAt(i, y - 1, w).setType(FLOOR);
                world.getBlockAt(w, y - 1, i).setType(FLOOR);
            }
        }

        // Ambient end rods scattered
        for (int i = 0; i < 40; i++) {
            int rx = -65 + random.nextInt(130), rz = -65 + random.nextInt(130);
            double dist = Math.sqrt(rx * rx + rz * rz);
            if (dist > 32 && dist < 68) world.getBlockAt(rx, y, rz).setType(LIGHT);
        }
    }

    // ── Keep Rooms (4 floors x multiple rooms) ──
    private void buildKeepRooms(World world, int y) {
        int kh = 30;
        int floorH = 12;
        // Floor 1
        buildRoom(world, -28, y + 1, -28, 12, 8, 12, "Armory", 5);
        buildRoom(world, 16, y + 1, -28, 12, 8, 12, "Vault", 5);
        buildRoom(world, -28, y + 1, 16, 12, 8, 12, "Library", 4);
        buildRoom(world, 16, y + 1, 16, 12, 8, 12, "Treasury", 5);
        buildRoom(world, -10, y + 1, -28, 20, 8, 10, "Great Hall", 4);
        buildRoom(world, -10, y + 1, 18, 20, 8, 10, "Throne Room", 5);
        // Floor 2
        buildRoom(world, -28, y + 13, -28, 12, 8, 12, "Spawner Crypt", 5);
        buildRoom(world, 16, y + 13, -28, 12, 8, 12, "Alchemy Lab", 4);
        buildRoom(world, -28, y + 13, 16, 12, 8, 12, "War Room", 4);
        buildRoom(world, 16, y + 13, 16, 12, 8, 12, "Infirmary", 3);
        buildRoom(world, -10, y + 13, -28, 20, 8, 10, "Banquet Hall", 4);
        buildRoom(world, -10, y + 13, 18, 20, 8, 10, "Chapel of Void", 5);
        // Floor 3
        buildRoom(world, -28, y + 25, -28, 12, 8, 12, "Observatory", 4);
        buildRoom(world, 16, y + 25, -28, 12, 8, 12, "Dragon Hoard", 5);
        buildRoom(world, -28, y + 25, 16, 12, 8, 12, "Prison", 3);
        buildRoom(world, 16, y + 25, 16, 12, 8, 12, "Enchanting Chamber", 5);
        buildRoom(world, -10, y + 25, -5, 20, 10, 10, "Grand Gallery", 5);
        // Floor 4
        buildRoom(world, -28, y + 37, -28, 12, 8, 12, "Dragon Egg Vault", 5);
        buildRoom(world, 16, y + 37, -28, 12, 8, 12, "Abyssal Sanctum", 5);
        buildRoom(world, -28, y + 37, 16, 12, 8, 12, "Ancient Archives", 5);
        buildRoom(world, 16, y + 37, 16, 12, 8, 12, "Crystal Chamber", 5);
    }

    // ── Wing rooms ──
    private void buildWingRooms(World world, int y) {
        // North wing rooms
        buildRoom(world, -5, y + 1, -65, 10, 6, 8, "North Guard Post", 3);
        buildRoom(world, -5, y + 1, -50, 10, 6, 8, "North Armory", 4);
        // South wing rooms
        buildRoom(world, -5, y + 1, 42, 10, 6, 8, "South Guard Post", 3);
        buildRoom(world, -5, y + 1, 57, 10, 6, 8, "South Vault", 4);
        // West wing rooms
        buildRoom(world, -65, y + 1, -5, 8, 6, 10, "West Barracks", 3);
        buildRoom(world, -50, y + 1, -5, 8, 6, 10, "West Shrine", 4);
        // East wing rooms
        buildRoom(world, 57, y + 1, -5, 8, 6, 10, "East Barracks", 3);
        buildRoom(world, 42, y + 1, -5, 8, 6, 10, "East Shrine", 4);
    }

    private void buildRoom(World world, int ox, int oy, int oz, int w, int h, int d, String name, int lootTier) {
        for (int x = ox; x < ox + w; x++) {
            for (int z = oz; z < oz + d; z++) {
                world.getBlockAt(x, oy, z).setType(FLOOR);
                world.getBlockAt(x, oy + h, z).setType(FLOOR);
                for (int wy = oy + 1; wy < oy + h; wy++) world.getBlockAt(x, wy, z).setType(Material.AIR);
            }
        }
        for (int wy = oy; wy <= oy + h; wy++) {
            for (int x = ox; x < ox + w; x++) { world.getBlockAt(x, wy, oz).setType(WALL); world.getBlockAt(x, wy, oz + d - 1).setType(WALL); }
            for (int z = oz; z < oz + d; z++) { world.getBlockAt(ox, wy, z).setType(WALL); world.getBlockAt(ox + w - 1, wy, z).setType(WALL); }
        }
        // Door
        world.getBlockAt(ox + w / 2, oy + 1, oz).setType(Material.AIR);
        world.getBlockAt(ox + w / 2, oy + 2, oz).setType(Material.AIR);
        world.getBlockAt(ox + w / 2 + 1, oy + 1, oz).setType(Material.AIR);
        world.getBlockAt(ox + w / 2 + 1, oy + 2, oz).setType(Material.AIR);
        // Lighting
        world.getBlockAt(ox + 1, oy + h - 1, oz + 1).setType(LIGHT);
        world.getBlockAt(ox + w - 2, oy + h - 1, oz + d - 2).setType(LIGHT);
        if (w > 10) world.getBlockAt(ox + w / 2, oy + h - 1, oz + d / 2).setType(LIGHT);

        // ENDGAME Loot chest
        placeChest(world, ox + w / 2, oy + 1, oz + d - 2, lootTier);

        // Spawner in certain rooms
        if (name.contains("Spawner") || name.contains("Crypt") || name.contains("Guard") || name.contains("Barracks")) {
            placeSpawner(world, ox + w / 2 + 2, oy + 1, oz + d / 2, EntityType.WITHER_SKELETON);
        }
        if (name.contains("Prison") || name.contains("Sanctum")) {
            placeSpawner(world, ox + w / 2 - 2, oy + 1, oz + d / 2, EntityType.ENDERMAN);
        }
    }

    private void buildGateEntrance(World world, int y) {
        int h = 70;
        // Grand south gate (7 wide x 10 tall)
        for (int x = -3; x <= 3; x++)
            for (int dy = 0; dy < 10; dy++)
                world.getBlockAt(x, y + dy, h).setType(Material.AIR);
        // Archway
        for (int x = -4; x <= 4; x++) world.getBlockAt(x, y + 10, h).setType(ACCENT);
        world.getBlockAt(-4, y + 10, h).setType(ACCENT2);
        world.getBlockAt(4, y + 10, h).setType(ACCENT2);
        // Flanking pillars
        for (int dy = 0; dy < 14; dy++) { world.getBlockAt(-5, y + dy, h + 1).setType(PILLAR); world.getBlockAt(5, y + dy, h + 1).setType(PILLAR); }
        world.getBlockAt(-5, y + 14, h + 1).setType(LIGHT); world.getBlockAt(5, y + 14, h + 1).setType(LIGHT);
        // Path
        for (int z = h + 1; z <= h + 15; z++)
            for (int x = -3; x <= 3; x++)
                world.getBlockAt(x, y - 1, z).setType(FLOOR);
    }

    // ── Endgame Loot System ──
    private void placeChest(World world, int x, int y, int z, int tier) {
        Block b = world.getBlockAt(x, y, z);
        b.setType(Material.CHEST);
        if (b.getState() instanceof Chest chest) fillEndgameLoot(chest.getInventory(), tier);
    }

    private void fillEndgameLoot(Inventory inv, int tier) {
        int items = 4 + tier * 2;
        for (int i = 0; i < items; i++) {
            int slot = random.nextInt(27);
            ItemStack item = generateEndgameLoot(tier);
            if (item != null) inv.setItem(slot, item);
        }
    }

    private ItemStack generateEndgameLoot(int tier) {
        LegendaryWeaponManager wm = plugin.getLegendaryWeaponManager();
        PowerUpManager pum = plugin.getPowerUpManager();

        if (tier >= 5) {
            // Top tier: ABYSSAL/MYTHIC weapons, best power-ups
            return switch (random.nextInt(12)) {
                case 0 -> { LegendaryWeapon[] pool = LegendaryWeapon.byTier(LegendaryTier.ABYSSAL); yield pool.length > 0 ? wm.createWeapon(pool[random.nextInt(pool.length)]) : null; }
                case 1, 2 -> { LegendaryWeapon[] pool = LegendaryWeapon.byTier(LegendaryTier.MYTHIC); yield pool.length > 0 ? wm.createWeapon(pool[random.nextInt(pool.length)]) : null; }
                case 3 -> pum.createHeartCrystal();
                case 4 -> pum.createPhoenixFeather();
                case 5 -> pum.createKeepInventorer();
                case 6 -> pum.createTitanResolve();
                case 7 -> pum.createBerserkerMark();
                case 8 -> new ItemStack(Material.NETHERITE_INGOT, 3 + random.nextInt(5));
                case 9 -> new ItemStack(Material.NETHER_STAR, 2 + random.nextInt(3));
                case 10 -> new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 2 + random.nextInt(3));
                default -> new ItemStack(Material.TOTEM_OF_UNDYING, 1);
            };
        } else if (tier >= 4) {
            return switch (random.nextInt(10)) {
                case 0 -> { LegendaryWeapon[] pool = LegendaryWeapon.byTier(LegendaryTier.MYTHIC); yield pool.length > 0 ? wm.createWeapon(pool[random.nextInt(pool.length)]) : null; }
                case 1 -> { LegendaryWeapon[] pool = LegendaryWeapon.byTier(LegendaryTier.EPIC); yield pool.length > 0 ? wm.createWeapon(pool[random.nextInt(pool.length)]) : null; }
                case 2 -> pum.createHeartCrystal();
                case 3 -> pum.createSoulFragment();
                case 4 -> pum.createVitalityShard();
                case 5 -> new ItemStack(Material.NETHERITE_INGOT, 2 + random.nextInt(3));
                case 6 -> new ItemStack(Material.NETHER_STAR, 1 + random.nextInt(2));
                case 7 -> new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 1 + random.nextInt(2));
                case 8 -> new ItemStack(Material.DIAMOND_BLOCK, 3 + random.nextInt(5));
                default -> new ItemStack(Material.TOTEM_OF_UNDYING, 1);
            };
        } else {
            return switch (random.nextInt(8)) {
                case 0 -> { LegendaryWeapon[] pool = LegendaryWeapon.byTier(LegendaryTier.EPIC); yield pool.length > 0 ? wm.createWeapon(pool[random.nextInt(pool.length)]) : null; }
                case 1 -> { LegendaryWeapon[] pool = LegendaryWeapon.byTier(LegendaryTier.RARE); yield pool.length > 0 ? wm.createWeapon(pool[random.nextInt(pool.length)]) : null; }
                case 2 -> pum.createSoulFragment();
                case 3 -> new ItemStack(Material.DIAMOND, 8 + random.nextInt(12));
                case 4 -> new ItemStack(Material.NETHERITE_SCRAP, 2 + random.nextInt(4));
                case 5 -> new ItemStack(Material.GOLDEN_APPLE, 3 + random.nextInt(5));
                case 6 -> new ItemStack(Material.EXPERIENCE_BOTTLE, 16 + random.nextInt(32));
                default -> new ItemStack(Material.EMERALD, 10 + random.nextInt(20));
            };
        }
    }

    private void placeSpawner(World world, int x, int y, int z, EntityType type) {
        Block b = world.getBlockAt(x, y, z);
        b.setType(Material.SPAWNER);
        if (b.getState() instanceof CreatureSpawner spawner) {
            spawner.setSpawnedType(type);
            spawner.setDelay(100);
            spawner.setMaxNearbyEntities(6);
            spawner.setSpawnRange(5);
            spawner.setRequiredPlayerRange(16);
            spawner.update();
        }
    }
}
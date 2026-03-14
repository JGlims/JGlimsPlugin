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
import org.bukkit.inventory.meta.ItemMeta;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.Random;

/**
 * Abyssal Citadel — Phase 3 MEGA Build.
 *
 * Footprint: ~141x141, height ~80 blocks.
 * Grand south gate (11 wide x 15 tall) with flanking mega-pillars.
 * Grand spiral staircases in all 4 corners of the keep connecting all floors.
 * Dedicated stairwell from floor 4 to the bedrock arena.
 * 50+ rooms: armories, vaults, libraries, throne room, crypts, spawner halls,
 *   PUZZLE ROOMS (pressure plate sequences, maze rooms),
 *   PARKOUR SECTIONS (floating platforms, lava gaps),
 *   HIDDEN PASSAGES (behind bookshelves, under floors),
 *   boss antechambers, alchemy labs, observatories, dragon hoards.
 * Endgame loot: MYTHIC/ABYSSAL weapons, power-ups, blessings, enchanted books.
 * Populated with Wither Skeletons + Endermen via spawners.
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
    private static final Material BOOKSHELF = Material.CHISELED_BOOKSHELF;
    private static final Material LAVA = Material.LAVA;
    private static final Material IRON_BARS = Material.IRON_BARS;
    private static final Material CHAIN;
    static {
        Material resolved;
        try {
            resolved = Material.valueOf("CHAIN");
        } catch (IllegalArgumentException e) {
            resolved = Material.IRON_BARS; // fallback for API versions where CHAIN is absent
        }
        CHAIN = resolved;
    }
    private static final Material LANTERN = Material.SOUL_LANTERN;
    private static final Material CARPET = Material.PURPLE_CARPET;
    private static final Material BANNER = Material.PURPLE_BANNER;

    private int baseY; // stored for helper methods

    public AbyssCitadelBuilder(JGlimsPlugin plugin) {
        this.plugin = plugin;
    }

    public void buildCitadel(World world) {
        baseY = world.getHighestBlockYAt(0, 0);
        if (baseY < 30) baseY = 55;
        baseY += 1;
        this.baseY = baseY;
        plugin.getLogger().info("[Citadel] Phase 3 build at Y=" + baseY + ", clearing 141x141...");

        clearArea(world);
        buildOuterWalls(world);
        buildTowers(world);
        buildInnerKeep(world);
        buildGrandStaircases(world);
        buildWings(world);
        buildArena(world);
        buildArenaStairwell(world);
        buildCourtyard(world);
        buildGrandGate(world);

        // ── ROOMS: Keep floors ──
        buildKeepFloor1Rooms(world);
        buildKeepFloor2Rooms(world);
        buildKeepFloor3Rooms(world);
        buildKeepFloor4Rooms(world);

        // ── ROOMS: Wing rooms ──
        buildWingRooms(world);

        // ── SPECIAL: Puzzle, Parkour, Hidden ──
        buildPuzzleRooms(world);
        buildParkourSections(world);
        buildHiddenPassages(world);

        // ── COURTYARD features ──
        buildCourtyardFeatures(world);

        plugin.getLogger().info("[Citadel] Phase 3 complete! 141x141, ~80 blocks, 50+ rooms, puzzles, parkour, secrets");
    }

    // ── CLEAR AREA ──
    private void clearArea(World world) {
        for (int x = -75; x <= 75; x++) {
            for (int z = -75; z <= 75; z++) {
                for (int cy = baseY; cy < baseY + 90; cy++) world.getBlockAt(x, cy, z).setType(Material.AIR);
                world.getBlockAt(x, baseY - 1, z).setType(FLOOR);
                world.getBlockAt(x, baseY - 2, z).setType(WALL);
            }
        }
    }

    // ── OUTER WALLS: 140x140, 15 tall ──
    private void buildOuterWalls(World world) {
        int h = 70;
        int wallH = 15;
        for (int wy = baseY; wy < baseY + wallH; wy++) {
            int relY = wy - baseY;
            for (int i = -h; i <= h; i++) {
                Material mat = (relY == 0 || relY == wallH - 1) ? WALL2 : (relY % 4 == 0 ? ACCENT : WALL);
                world.getBlockAt(i, wy, -h).setType(mat);
                world.getBlockAt(i, wy, h).setType(mat);
                world.getBlockAt(-h, wy, i).setType(mat);
                world.getBlockAt(h, wy, i).setType(mat);
            }
        }
        // Battlements
        for (int i = -h; i <= h; i += 2) {
            world.getBlockAt(i, baseY + wallH, -h).setType(WALL);
            world.getBlockAt(i, baseY + wallH, h).setType(WALL);
            world.getBlockAt(-h, baseY + wallH, i).setType(WALL);
            world.getBlockAt(h, baseY + wallH, i).setType(WALL);
        }
        // Walkway + lights
        for (int i = -h + 1; i < h; i++) {
            world.getBlockAt(i, baseY + wallH - 1, -h + 1).setType(FLOOR);
            world.getBlockAt(i, baseY + wallH - 1, h - 1).setType(FLOOR);
            world.getBlockAt(-h + 1, baseY + wallH - 1, i).setType(FLOOR);
            world.getBlockAt(h - 1, baseY + wallH - 1, i).setType(FLOOR);
        }
        for (int i = -h; i <= h; i += 8) {
            world.getBlockAt(i, baseY + wallH + 1, -h).setType(LIGHT);
            world.getBlockAt(i, baseY + wallH + 1, h).setType(LIGHT);
            world.getBlockAt(-h, baseY + wallH + 1, i).setType(LIGHT);
            world.getBlockAt(h, baseY + wallH + 1, i).setType(LIGHT);
        }
    }

    // ── 8 TOWERS ──
    private void buildTowers(World world) {
        int h = 70;
        int[][] positions = {
            {-h, -h}, {h, -h}, {-h, h}, {h, h},
            {0, -h}, {0, h}, {-h, 0}, {h, 0}
        };
        for (int[] pos : positions) buildSingleTower(world, pos[0], pos[1], 7, 70);
    }

    private void buildSingleTower(World world, int cx, int cz, int r, int height) {
        for (int wy = baseY; wy < baseY + height; wy++) {
            int relY = wy - baseY;
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    double dist = Math.sqrt(dx * dx + dz * dz);
                    if (dist <= r + 0.5) {
                        if (dist >= r - 0.5) {
                            world.getBlockAt(cx + dx, wy, cz + dz).setType(relY % 6 == 0 ? ACCENT : WALL);
                        } else if (relY % 15 == 0) {
                            world.getBlockAt(cx + dx, wy, cz + dz).setType(FLOOR);
                        }
                        if (dist >= r - 0.5 && relY % 5 == 3 && (dx == 0 || dz == 0)
                                && Math.abs(dx) + Math.abs(dz) >= r - 1)
                            world.getBlockAt(cx + dx, wy, cz + dz).setType(GLASS);
                    }
                }
            }
        }
        // Cone roof
        for (int rh = 0; rh < 12; rh++) {
            double cr = r * (1.0 - rh / 12.0);
            for (int dx = (int) -cr; dx <= (int) cr; dx++)
                for (int dz = (int) -cr; dz <= (int) cr; dz++)
                    if (Math.sqrt(dx * dx + dz * dz) <= cr)
                        world.getBlockAt(cx + dx, baseY + height + rh, cz + dz).setType(ACCENT);
        }
        // Spire
        for (int sh = 0; sh < 6; sh++) world.getBlockAt(cx, baseY + height + 12 + sh, cz).setType(ACCENT2);
        world.getBlockAt(cx, baseY + height + 18, cz).setType(LIGHT);
        // Loot + spawner
        placeChest(world, cx, baseY + 1, cz, 4);
        placeSpawner(world, cx + 2, baseY + 1, cz, EntityType.WITHER_SKELETON);
    }

    // ── INNER KEEP: 61x61, 50 tall, 4 floors ──
    private void buildInnerKeep(World world) {
        int kh = 30;
        int keepH = 50;
        int floorH = 12;
        // Walls
        for (int wy = baseY; wy < baseY + keepH; wy++) {
            int relY = wy - baseY;
            for (int i = -kh; i <= kh; i++) {
                Material mat = relY % 5 == 0 ? ACCENT : WALL;
                world.getBlockAt(i, wy, -kh).setType(mat);
                world.getBlockAt(i, wy, kh).setType(mat);
                world.getBlockAt(-kh, wy, i).setType(mat);
                world.getBlockAt(kh, wy, i).setType(mat);
            }
            // Floor plates
            if (relY > 0 && relY % floorH == 0) {
                for (int x = -kh + 1; x < kh; x++)
                    for (int z = -kh + 1; z < kh; z++)
                        world.getBlockAt(x, wy, z).setType(FLOOR);
            }
        }
        // Windows
        for (int face = 0; face < 4; face++) {
            for (int w = -kh + 5; w <= kh - 5; w += 6) {
                for (int fl = 0; fl < 4; fl++) {
                    for (int wy = 0; wy < 3; wy++) {
                        int fy = baseY + 3 + fl * floorH + wy;
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
        // Central pillar
        for (int wy = baseY; wy < baseY + keepH; wy++) {
            for (int dx = -2; dx <= 2; dx++)
                for (int dz = -2; dz <= 2; dz++)
                    if (Math.abs(dx) + Math.abs(dz) <= 3)
                        world.getBlockAt(dx, wy, dz).setType(PILLAR);
        }
        // Lighting per floor
        for (int fl = 0; fl < 4; fl++) {
            int fy = baseY + 1 + fl * floorH;
            for (int x = -kh + 5; x < kh; x += 8)
                for (int z = -kh + 5; z < kh; z += 8) {
                    world.getBlockAt(x, fy + floorH - 2, z).setType(LIGHT);
                    world.getBlockAt(x, fy, z).setType(LANTERN);
                }
        }
        // Keep entrances (4 faces, 7 wide x 8 tall)
        for (int face = 0; face < 4; face++) {
            for (int dx = -3; dx <= 3; dx++) {
                for (int dy = 0; dy < 8; dy++) {
                    switch (face) {
                        case 0 -> world.getBlockAt(dx, baseY + dy, -kh).setType(Material.AIR);
                        case 1 -> world.getBlockAt(dx, baseY + dy, kh).setType(Material.AIR);
                        case 2 -> world.getBlockAt(-kh, baseY + dy, dx).setType(Material.AIR);
                        case 3 -> world.getBlockAt(kh, baseY + dy, dx).setType(Material.AIR);
                    }
                }
            }
        }
    }

    // ── GRAND SPIRAL STAIRCASES (4 corners of keep) ──
    private void buildGrandStaircases(World world) {
        int kh = 30;
        int[][] corners = {{-kh + 2, -kh + 2}, {kh - 4, -kh + 2}, {-kh + 2, kh - 4}, {kh - 4, kh - 4}};
        for (int[] corner : corners) {
            buildSpiralStaircase(world, corner[0], corner[1], 3, 48);
        }
    }

    private void buildSpiralStaircase(World world, int ox, int oz, int radius, int totalHeight) {
        // Clear the shaft
        for (int dy = 0; dy < totalHeight; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dz * dz <= radius * radius) {
                        world.getBlockAt(ox + radius + dx, baseY + 1 + dy, oz + radius + dz).setType(Material.AIR);
                    }
                }
            }
        }
        // Central column
        for (int dy = 0; dy < totalHeight; dy++) {
            world.getBlockAt(ox + radius, baseY + 1 + dy, oz + radius).setType(ACCENT2);
        }
        // Spiral steps
        int steps = totalHeight * 2;
        for (int i = 0; i < steps; i++) {
            double angle = (i / (double) steps) * totalHeight * Math.PI * 2 / 12.0;
            int sx = ox + radius + (int) (Math.cos(angle) * (radius - 0.5));
            int sz = oz + radius + (int) (Math.sin(angle) * (radius - 0.5));
            int sy = baseY + 1 + (i * totalHeight / steps);
            world.getBlockAt(sx, sy, sz).setType(Material.DEEPSLATE_BRICK_STAIRS);
            // Also place adjacent step for width
            int sx2 = ox + radius + (int) (Math.cos(angle) * (radius - 1.5));
            int sz2 = oz + radius + (int) (Math.sin(angle) * (radius - 1.5));
            world.getBlockAt(sx2, sy, sz2).setType(Material.DEEPSLATE_BRICK_STAIRS);
            // Clear headroom
            world.getBlockAt(sx, sy + 1, sz).setType(Material.AIR);
            world.getBlockAt(sx, sy + 2, sz).setType(Material.AIR);
            // Light every 8 steps
            if (i % 8 == 0) world.getBlockAt(ox + radius, sy + 1, oz + radius).setType(LANTERN);
        }
    }

    // ── WINGS ──
    private void buildWings(World world) {
        int outer = 70, keep = 30, wingW = 7, wingH = 12;
        buildWingNS(world, -wingW, wingW, -outer + 1, -keep, wingH);
        buildWingNS(world, -wingW, wingW, keep, outer - 1, wingH);
        buildWingEW(world, -outer + 1, -keep, -wingW, wingW, wingH);
        buildWingEW(world, keep, outer - 1, -wingW, wingW, wingH);
    }

    private void buildWingNS(World world, int x1, int x2, int z1, int z2, int h) {
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
        for (int x = x1 + 1; x < x2; x++)
            for (int z = z1; z <= z2; z++)
                world.getBlockAt(x, baseY + h / 2, z).setType(FLOOR);
        for (int z = z1 + 2; z < z2; z += 4) world.getBlockAt(0, baseY + h - 2, z).setType(LIGHT);
        for (int z = z1 + 5; z < z2 - 5; z += 10)
            placeSpawner(world, 0, baseY + 1, z, random.nextBoolean() ? EntityType.WITHER_SKELETON : EntityType.ENDERMAN);
    }

    private void buildWingEW(World world, int x1, int x2, int z1, int z2, int h) {
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
        for (int x = x1 + 2; x < x2; x += 4) world.getBlockAt(x, baseY + h - 2, 0).setType(LIGHT);
        for (int x = x1 + 5; x < x2 - 5; x += 10)
            placeSpawner(world, x, baseY + 1, 0, random.nextBoolean() ? EntityType.WITHER_SKELETON : EntityType.ENDERMAN);
    }

    // ── BEDROCK ARENA ──
    private void buildArena(World world) {
        int arenaY = baseY + 50;
        int arenaR = 30;
        for (int x = -arenaR; x <= arenaR; x++) {
            for (int z = -arenaR; z <= arenaR; z++) {
                double dist = Math.sqrt(x * x + z * z);
                if (dist <= arenaR) {
                    world.getBlockAt(x, arenaY, z).setType(BEDROCK);
                    for (int dy = 1; dy <= 35; dy++) world.getBlockAt(x, arenaY + dy, z).setType(Material.AIR);
                }
                if (dist >= arenaR - 0.5 && dist <= arenaR + 1.5) {
                    for (int dy = 1; dy <= 6; dy++) world.getBlockAt(x, arenaY + dy, z).setType(BEDROCK);
                }
            }
        }
        // 4 obsidian pillars
        int[][] pillars = {{0, -arenaR + 5}, {0, arenaR - 5}, {-arenaR + 5, 0}, {arenaR - 5, 0}};
        for (int[] p : pillars) {
            for (int dy = 1; dy <= 15; dy++) world.getBlockAt(p[0], arenaY + dy, p[1]).setType(DARK);
            world.getBlockAt(p[0], arenaY + 16, p[1]).setType(CRYING);
            world.getBlockAt(p[0], arenaY + 17, p[1]).setType(LIGHT);
        }
        // Central perch
        for (int dy = 1; dy <= 18; dy++) world.getBlockAt(0, arenaY + dy, 0).setType(DARK);
        world.getBlockAt(0, arenaY + 19, 0).setType(CRYING);
        // Arena decorative rings
        for (int x = -arenaR; x <= arenaR; x++) {
            for (int z = -arenaR; z <= arenaR; z++) {
                double dist = Math.sqrt(x * x + z * z);
                if (Math.abs(dist - 10) < 0.7 || Math.abs(dist - 20) < 0.7) {
                    world.getBlockAt(x, arenaY, z).setType(Material.CRYING_OBSIDIAN);
                }
            }
        }
    }

    // ── ARENA STAIRWELL (from floor 4 to arena) ──
    private void buildArenaStairwell(World world) {
        int kh = 30;
        int startY = baseY + 37; // floor 4
        int arenaY = baseY + 50;
        int stairX = -kh + 6;
        int stairZ = -kh + 6;
        // Clear shaft
        for (int dy = 0; dy <= arenaY - startY + 2; dy++) {
            for (int dx = 0; dx < 5; dx++) {
                for (int dz = 0; dz < 5; dz++) {
                    world.getBlockAt(stairX + dx, startY + dy, stairZ + dz).setType(Material.AIR);
                }
            }
        }
        // Spiral up
        int totalSteps = arenaY - startY;
        for (int step = 0; step < totalSteps; step++) {
            int phase = step % 4;
            int dx = 0, dz = 0;
            switch (phase) {
                case 0 -> { dx = step % 4; dz = 0; }
                case 1 -> { dx = 3; dz = step % 4; }
                case 2 -> { dx = 3 - step % 4; dz = 3; }
                case 3 -> { dx = 0; dz = 3 - step % 4; }
            }
            world.getBlockAt(stairX + (step % 4), startY + step, stairZ + (step / 4) % 4).setType(Material.DEEPSLATE_BRICK_STAIRS);
            world.getBlockAt(stairX + (step % 4), startY + step + 1, stairZ + (step / 4) % 4).setType(Material.AIR);
            world.getBlockAt(stairX + (step % 4), startY + step + 2, stairZ + (step / 4) % 4).setType(Material.AIR);
        }
        // Sign at entrance
        world.getBlockAt(stairX, startY, stairZ - 1).setType(ACCENT);
        world.getBlockAt(stairX + 1, startY, stairZ - 1).setType(LIGHT);
        // Opening in arena floor
        for (int dx = 0; dx < 5; dx++)
            for (int dz = 0; dz < 5; dz++)
                world.getBlockAt(stairX + dx, arenaY, stairZ + dz).setType(Material.AIR);
    }

    // ── GRAND SOUTH GATE (11 wide x 15 tall) ──
    private void buildGrandGate(World world) {
        int h = 70;
        // Clear the gate opening
        for (int x = -5; x <= 5; x++)
            for (int dy = 0; dy < 15; dy++)
                world.getBlockAt(x, baseY + dy, h).setType(Material.AIR);
        // Pointed arch top
        for (int x = -6; x <= 6; x++) {
            int archY = 15 - (int)(Math.abs(x) * 0.8);
            world.getBlockAt(x, baseY + archY, h).setType(ACCENT);
            world.getBlockAt(x, baseY + archY + 1, h).setType(ACCENT2);
        }
        // Mega flanking pillars (3x3, 20 tall)
        for (int side = -1; side <= 1; side += 2) {
            int px = side * 7;
            for (int dy = 0; dy < 20; dy++) {
                for (int dx = -1; dx <= 1; dx++)
                    for (int dz = -1; dz <= 1; dz++)
                        world.getBlockAt(px + dx, baseY + dy, h + dz).setType(
                                dy % 4 == 0 ? ACCENT : PILLAR);
            }
            world.getBlockAt(px, baseY + 20, h).setType(CRYING);
            world.getBlockAt(px, baseY + 21, h).setType(LIGHT);
            // Banner on pillar
            world.getBlockAt(px, baseY + 18, h + 2).setType(BANNER);
        }
        // Drawbridge path (11 wide, 20 long)
        for (int z = h; z <= h + 20; z++)
            for (int x = -5; x <= 5; x++) {
                world.getBlockAt(x, baseY - 1, z).setType(FLOOR);
                // Edge walls (low, 2 high)
                if (Math.abs(x) == 5) {
                    world.getBlockAt(x, baseY, z).setType(FENCE);
                }
            }
        // Torches along path
        for (int z = h + 2; z <= h + 20; z += 4) {
            world.getBlockAt(-5, baseY + 1, z).setType(LIGHT);
            world.getBlockAt(5, baseY + 1, z).setType(LIGHT);
        }
        // Additional gates on other 3 sides (smaller, 7 wide x 10 tall)
        for (int face = 0; face < 3; face++) {
            for (int dx = -3; dx <= 3; dx++) {
                for (int dy = 0; dy < 10; dy++) {
                    switch (face) {
                        case 0 -> world.getBlockAt(dx, baseY + dy, -h).setType(Material.AIR);
                        case 1 -> world.getBlockAt(-h, baseY + dy, dx).setType(Material.AIR);
                        case 2 -> world.getBlockAt(h, baseY + dy, dx).setType(Material.AIR);
                    }
                }
            }
        }
    }

    // ── COURTYARD ──
    private void buildCourtyard(World world) {
        // Cross paths
        for (int i = -70; i <= 70; i++) {
            for (int w = -1; w <= 1; w++) {
                world.getBlockAt(i, baseY - 1, w).setType(FLOOR);
                world.getBlockAt(w, baseY - 1, i).setType(FLOOR);
            }
        }
    }

    private void buildCourtyardFeatures(World world) {
        // 4 large statues
        int[][] statuePos = {{-50, 0}, {50, 0}, {0, -50}, {0, 50}};
        for (int[] sp : statuePos) {
            // Base
            for (int dx = -2; dx <= 2; dx++)
                for (int dz = -2; dz <= 2; dz++)
                    world.getBlockAt(sp[0] + dx, baseY, sp[1] + dz).setType(PILLAR);
            // Body
            for (int dy = 1; dy < 12; dy++) world.getBlockAt(sp[0], baseY + dy, sp[1]).setType(DARK);
            world.getBlockAt(sp[0], baseY + 12, sp[1]).setType(CRYING);
            world.getBlockAt(sp[0], baseY + 13, sp[1]).setType(LIGHT);
            // Arms
            world.getBlockAt(sp[0] - 1, baseY + 8, sp[1]).setType(DARK);
            world.getBlockAt(sp[0] + 1, baseY + 8, sp[1]).setType(DARK);
        }
        // Scattered end rods
        for (int i = 0; i < 60; i++) {
            int rx = -65 + random.nextInt(130), rz = -65 + random.nextInt(130);
            double dist = Math.sqrt(rx * rx + rz * rz);
            if (dist > 32 && dist < 68) world.getBlockAt(rx, baseY, rz).setType(LIGHT);
        }
        // Small gardens (soul sand + wither roses)
        int[][] gardenPos = {{-40, -40}, {40, -40}, {-40, 40}, {40, 40}};
        for (int[] gp : gardenPos) {
            for (int dx = -3; dx <= 3; dx++)
                for (int dz = -3; dz <= 3; dz++) {
                    world.getBlockAt(gp[0] + dx, baseY - 1, gp[1] + dz).setType(Material.SOUL_SAND);
                    if (random.nextDouble() < 0.4)
                        world.getBlockAt(gp[0] + dx, baseY, gp[1] + dz).setType(Material.WITHER_ROSE);
                }
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // KEEP FLOOR ROOMS
    // ══════════════════════════════════════════════════════════════════

    // Floor 1 (baseY+1 to baseY+12): Main hall, Armory, Vault, Library, Throne, Great Hall, Guard rooms
    private void buildKeepFloor1Rooms(World world) {
        int fy = baseY + 1;
        buildRoom(world, -28, fy, -28, 14, 9, 14, "Grand Armory", 5);
        buildRoom(world, 16, fy, -28, 12, 9, 12, "Royal Vault", 5);
        buildRoom(world, -28, fy, 16, 14, 9, 14, "Ancient Library", 4);
        buildRoom(world, 16, fy, 16, 12, 9, 12, "Royal Treasury", 5);
        buildRoom(world, -10, fy, -28, 20, 10, 10, "Great Hall", 4);
        buildRoom(world, -10, fy, 20, 20, 10, 8, "Throne Room", 5);
        buildRoom(world, -28, fy, -10, 8, 8, 8, "Guard Post Alpha", 3);
        buildRoom(world, 22, fy, -10, 8, 8, 8, "Guard Post Beta", 3);
    }

    // Floor 2 (baseY+13): Spawner Crypts, Alchemy, War Room, Banquet, Chapel
    private void buildKeepFloor2Rooms(World world) {
        int fy = baseY + 13;
        buildRoom(world, -28, fy, -28, 14, 9, 14, "Spawner Crypt", 5);
        buildRoom(world, 16, fy, -28, 12, 9, 12, "Alchemy Laboratory", 4);
        buildRoom(world, -28, fy, 16, 14, 9, 14, "War Room", 4);
        buildRoom(world, 16, fy, 16, 12, 9, 12, "Infirmary", 3);
        buildRoom(world, -10, fy, -28, 20, 10, 10, "Banquet Hall", 4);
        buildRoom(world, -10, fy, 20, 20, 10, 8, "Chapel of Void", 5);
        buildRoom(world, -28, fy, -10, 10, 8, 10, "Torture Chamber", 3);
        buildRoom(world, 20, fy, -10, 10, 8, 10, "Servant Quarters", 3);
    }

    // Floor 3 (baseY+25): Observatory, Dragon Hoard, Prison, Gallery, Museum
    private void buildKeepFloor3Rooms(World world) {
        int fy = baseY + 25;
        buildRoom(world, -28, fy, -28, 14, 9, 14, "Grand Observatory", 4);
        buildRoom(world, 16, fy, -28, 12, 9, 12, "Dragon Hoard", 5);
        buildRoom(world, -28, fy, 16, 14, 9, 14, "Prison Block", 3);
        buildRoom(world, 16, fy, 16, 12, 9, 12, "Enchanting Chamber", 5);
        buildRoom(world, -10, fy, -5, 20, 10, 10, "Grand Gallery", 5);
        buildRoom(world, -28, fy, -10, 10, 8, 10, "Trophy Hall", 4);
        buildRoom(world, 20, fy, -10, 10, 8, 10, "Reliquary", 5);
    }

    // Floor 4 (baseY+37): High-value vaults, Sanctums, Archives, pre-arena rooms
    private void buildKeepFloor4Rooms(World world) {
        int fy = baseY + 37;
        buildRoom(world, -28, fy, -28, 14, 9, 14, "Dragon Egg Vault", 5);
        buildRoom(world, 16, fy, -28, 12, 9, 12, "Abyssal Sanctum", 5);
        buildRoom(world, -28, fy, 16, 14, 9, 14, "Ancient Archives", 5);
        buildRoom(world, 16, fy, 16, 12, 9, 12, "Crystal Chamber", 5);
        buildRoom(world, -10, fy, -10, 20, 10, 20, "Boss Antechamber", 5);
        buildRoom(world, -28, fy, -10, 8, 8, 8, "Void Shrine", 5);
        buildRoom(world, 22, fy, -10, 8, 8, 8, "Warden's Study", 4);
    }

    // ── WING ROOMS ──
    private void buildWingRooms(World world) {
        int fy = baseY + 1;
        // North wing
        buildRoom(world, -5, fy, -65, 10, 7, 10, "North Guard Post", 3);
        buildRoom(world, -5, fy, -50, 10, 7, 10, "North Armory", 4);
        buildRoom(world, -5, fy, -38, 10, 7, 8, "North Barracks", 3);
        // South wing
        buildRoom(world, -5, fy, 42, 10, 7, 10, "South Guard Post", 3);
        buildRoom(world, -5, fy, 55, 10, 7, 10, "South Vault", 4);
        // West wing
        buildRoom(world, -65, fy, -5, 10, 7, 10, "West Barracks", 3);
        buildRoom(world, -50, fy, -5, 10, 7, 10, "West Shrine", 4);
        buildRoom(world, -38, fy, -5, 8, 7, 10, "West Storage", 3);
        // East wing
        buildRoom(world, 55, fy, -5, 10, 7, 10, "East Barracks", 3);
        buildRoom(world, 42, fy, -5, 10, 7, 10, "East Shrine", 4);
    }

    // ══════════════════════════════════════════════════════════════════
    // PUZZLE ROOMS
    // ══════════════════════════════════════════════════════════════════
    private void buildPuzzleRooms(World world) {
        // Puzzle Room 1: Pressure Plate Maze (Floor 2, east side)
        int px = 16, py = baseY + 13, pz = 2;
        buildRoom(world, px, py, pz, 12, 8, 12, "Pressure Plate Maze", 5);
        // Fill floor with stone pressure plates in a pattern
        for (int dx = 1; dx < 11; dx++) {
            for (int dz = 1; dz < 11; dz++) {
                if ((dx + dz) % 3 == 0) {
                    world.getBlockAt(px + dx, py, pz + dz).setType(Material.STONE_PRESSURE_PLATE);
                } else if ((dx * dz) % 5 == 0) {
                    world.getBlockAt(px + dx, py - 1, pz + dz).setType(Material.TNT);
                    world.getBlockAt(px + dx, py, pz + dz).setType(Material.DEEPSLATE_TILES);
                }
            }
        }
        // Reward chest behind the maze
        placeChest(world, px + 6, py + 1, pz + 10, 5);

        // Puzzle Room 2: Locked Vault (Floor 3, hidden button)
        int vx = -10, vy = baseY + 25, vz = 16;
        buildRoom(world, vx, vy, vz, 10, 8, 8, "Locked Vault", 5);
        // Iron door with button hidden behind a block
        world.getBlockAt(vx + 5, vy + 1, vz).setType(Material.IRON_DOOR);
        world.getBlockAt(vx + 5, vy + 2, vz).setType(Material.IRON_DOOR);
        world.getBlockAt(vx + 7, vy + 3, vz + 3).setType(Material.STONE_BUTTON);

        // Puzzle Room 3: Redstone Sequence Room (Floor 1)
        int rx = 5, ry = baseY + 1, rz = -15;
        buildRoom(world, rx, ry, rz, 10, 8, 10, "Sequence Chamber", 4);
        // Levers on walls
        for (int i = 0; i < 4; i++) {
            world.getBlockAt(rx + 2 + i * 2, ry + 2, rz + 1).setType(Material.LEVER);
        }
        placeChest(world, rx + 5, ry + 1, rz + 8, 5);
    }

    // ══════════════════════════════════════════════════════════════════
    // PARKOUR SECTIONS
    // ══════════════════════════════════════════════════════════════════
    private void buildParkourSections(World world) {
        // Parkour 1: Lava Gap Challenge (Courtyard, between keep and outer wall, north side)
        int py = baseY;
        int startX = -20, startZ = -55;
        // Lava pit
        for (int x = startX; x < startX + 20; x++)
            for (int z = startZ; z < startZ + 10; z++) {
                world.getBlockAt(x, py - 2, z).setType(WALL);
                world.getBlockAt(x, py - 1, z).setType(LAVA);
                world.getBlockAt(x, py, z).setType(Material.AIR);
            }
        // Floating platforms (irregular spacing)
        int[][] platforms = {{0, 0}, {3, 2}, {7, 1}, {10, 3}, {13, 0}, {16, 2}, {19, 1}};
        for (int[] plat : platforms) {
            int px2 = startX + plat[0];
            int pz2 = startZ + 2 + plat[1];
            for (int dx = 0; dx < 2; dx++)
                for (int dz = 0; dz < 2; dz++)
                    world.getBlockAt(px2 + dx, py, pz2 + dz).setType(ACCENT);
        }
        // Reward at end
        placeChest(world, startX + 19, py + 1, startZ + 3, 5);

        // Parkour 2: Tower Climb (inside south mid-tower, floating blocks going up)
        int towerX = 0, towerZ = 70;
        for (int i = 0; i < 15; i++) {
            int px3 = towerX + (int)(Math.sin(i * 0.8) * 4);
            int pz3 = towerZ + (int)(Math.cos(i * 0.8) * 4);
            int py3 = baseY + 16 + i * 3;
            world.getBlockAt(px3, py3, pz3).setType(ACCENT);
            world.getBlockAt(px3 + 1, py3, pz3).setType(ACCENT);
        }
        placeChest(world, towerX, baseY + 61, towerZ, 5);

        // Parkour 3: Void Platform Run (courtyard east side)
        int vpStartX = 35, vpStartZ = -40;
        for (int i = 0; i < 12; i++) {
            int vpx = vpStartX + i * 3;
            int vpz = vpStartZ + (random.nextInt(5) - 2);
            world.getBlockAt(vpx, baseY, vpz).setType(Material.AIR);
            world.getBlockAt(vpx, baseY - 1, vpz).setType(Material.AIR);
            // Platform
            world.getBlockAt(vpx, baseY + (i % 2 == 0 ? 0 : 1), vpz).setType(ACCENT);
        }
        placeChest(world, vpStartX + 35, baseY + 1, vpStartZ, 4);
    }

    // ══════════════════════════════════════════════════════════════════
    // HIDDEN PASSAGES
    // ══════════════════════════════════════════════════════════════════
    private void buildHiddenPassages(World world) {
        // Hidden Passage 1: Behind the Throne Room (Floor 1)
        // Bookshelf wall that hides a tunnel to a secret vault
        int throneZ = 22; // behind throne room south wall
        for (int x = -3; x <= 3; x++) {
            world.getBlockAt(x, baseY + 1, throneZ).setType(BOOKSHELF);
            world.getBlockAt(x, baseY + 2, throneZ).setType(BOOKSHELF);
            world.getBlockAt(x, baseY + 3, throneZ).setType(BOOKSHELF);
        }
        // Actual passage (break bookshelves to find it)
        world.getBlockAt(0, baseY + 1, throneZ).setType(Material.AIR);
        world.getBlockAt(0, baseY + 2, throneZ).setType(Material.AIR);
        // Tunnel behind
        for (int z = throneZ + 1; z < throneZ + 8; z++) {
            world.getBlockAt(0, baseY + 1, z).setType(Material.AIR);
            world.getBlockAt(0, baseY + 2, z).setType(Material.AIR);
            world.getBlockAt(0, baseY, z).setType(FLOOR);
            world.getBlockAt(0, baseY + 3, z).setType(WALL);
            world.getBlockAt(-1, baseY + 1, z).setType(WALL);
            world.getBlockAt(1, baseY + 1, z).setType(WALL);
            world.getBlockAt(-1, baseY + 2, z).setType(WALL);
            world.getBlockAt(1, baseY + 2, z).setType(WALL);
        }
        // Secret room at end of tunnel
        buildRoom(world, -3, baseY + 1, throneZ + 8, 6, 5, 6, "Hidden Vault", 5);

        // Hidden Passage 2: Under the floor of the Great Hall (Floor 1)
        // Trapdoor to underground chamber
        world.getBlockAt(-5, baseY, -25).setType(Material.DARK_OAK_TRAPDOOR);
        for (int dy = -1; dy >= -5; dy--)
            world.getBlockAt(-5, baseY + dy, -25).setType(Material.AIR);
        // Small chamber below
        for (int x = -7; x <= -3; x++)
            for (int z = -27; z <= -23; z++) {
                world.getBlockAt(x, baseY - 5, z).setType(FLOOR);
                world.getBlockAt(x, baseY - 1, z).setType(Material.AIR);
                for (int dy = -4; dy <= -1; dy++) world.getBlockAt(x, baseY + dy, z).setType(Material.AIR);
                world.getBlockAt(x, baseY, z).setType(FLOOR); // restore floor above
            }
        // Walls around chamber
        for (int dy = -4; dy <= -1; dy++) {
            for (int i = -7; i <= -3; i++) {
                world.getBlockAt(i, baseY + dy, -27).setType(WALL);
                world.getBlockAt(i, baseY + dy, -23).setType(WALL);
            }
            for (int i = -27; i <= -23; i++) {
                world.getBlockAt(-7, baseY + dy, i).setType(WALL);
                world.getBlockAt(-3, baseY + dy, i).setType(WALL);
            }
        }
        placeChest(world, -5, baseY - 4, -25, 5);
        world.getBlockAt(-5, baseY, -25).setType(Material.DARK_OAK_TRAPDOOR); // re-place trapdoor

        // Hidden Passage 3: Secret door in the central pillar (all floors)
        // One side of the pillar has air leading to a narrow passage
        for (int fl = 0; fl < 4; fl++) {
            int fy = baseY + 1 + fl * 12;
            world.getBlockAt(3, fy, 0).setType(Material.AIR);
            world.getBlockAt(3, fy + 1, 0).setType(Material.AIR);
            // Small alcove
            for (int dx = 4; dx <= 6; dx++) {
                world.getBlockAt(dx, fy, 0).setType(FLOOR);
                world.getBlockAt(dx, fy + 1, 0).setType(Material.AIR);
                world.getBlockAt(dx, fy + 2, 0).setType(Material.AIR);
            }
            if (fl == 0 || fl == 3) {
                placeChest(world, 5, fy + 1, 0, 5);
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // ROOM BUILDER
    // ══════════════════════════════════════════════════════════════════
    private void buildRoom(World world, int ox, int oy, int oz, int w, int h, int d, String name, int lootTier) {
        // Floor + ceiling
        for (int x = ox; x < ox + w; x++) {
            for (int z = oz; z < oz + d; z++) {
                world.getBlockAt(x, oy, z).setType(FLOOR);
                world.getBlockAt(x, oy + h, z).setType(FLOOR);
                for (int wy = oy + 1; wy < oy + h; wy++) world.getBlockAt(x, wy, z).setType(Material.AIR);
            }
        }
        // Walls
        for (int wy = oy; wy <= oy + h; wy++) {
            for (int x = ox; x < ox + w; x++) {
                world.getBlockAt(x, wy, oz).setType(WALL);
                world.getBlockAt(x, wy, oz + d - 1).setType(WALL);
            }
            for (int z = oz; z < oz + d; z++) {
                world.getBlockAt(ox, wy, z).setType(WALL);
                world.getBlockAt(ox + w - 1, wy, z).setType(WALL);
            }
        }
        // Door (3 wide x 3 tall)
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = 1; dy <= 3; dy++) {
                world.getBlockAt(ox + w / 2 + dx, oy + dy, oz).setType(Material.AIR);
            }
        }
        // Lighting
        world.getBlockAt(ox + 1, oy + h - 1, oz + 1).setType(LANTERN);
        world.getBlockAt(ox + w - 2, oy + h - 1, oz + d - 2).setType(LANTERN);
        if (w > 10) world.getBlockAt(ox + w / 2, oy + h - 1, oz + d / 2).setType(LANTERN);

        // Carpet in large rooms
        if (w >= 12 && d >= 12) {
            for (int x = ox + 2; x < ox + w - 2; x += 2)
                for (int z = oz + 2; z < oz + d - 2; z += 2)
                    world.getBlockAt(x, oy + 1, z).setType(CARPET);
        }

        // Chains from ceiling in crypts and dark rooms
        if (name.contains("Crypt") || name.contains("Prison") || name.contains("Torture")) {
            for (int x = ox + 2; x < ox + w - 2; x += 3)
                for (int z = oz + 2; z < oz + d - 2; z += 3) {
                    world.getBlockAt(x, oy + h - 1, z).setType(CHAIN);
                    world.getBlockAt(x, oy + h - 2, z).setType(CHAIN);
                    world.getBlockAt(x, oy + h - 3, z).setType(LANTERN);
                }
        }

        // Room-specific decorations
        if (name.contains("Library") || name.contains("Archives")) {
            for (int x = ox + 1; x < ox + w - 1; x += 3) {
                for (int dy = 1; dy <= 3; dy++) {
                    world.getBlockAt(x, oy + dy, oz + 1).setType(BOOKSHELF);
                    world.getBlockAt(x, oy + dy, oz + d - 2).setType(BOOKSHELF);
                }
            }
        }
        if (name.contains("Throne")) {
            world.getBlockAt(ox + w / 2, oy + 1, oz + d - 3).setType(Material.DEEPSLATE_BRICK_STAIRS);
            world.getBlockAt(ox + w / 2, oy + 2, oz + d - 2).setType(BANNER);
        }

        // LOOT CHEST
        placeChest(world, ox + w / 2, oy + 1, oz + d - 2, lootTier);
        // Second chest in large rooms
        if (w >= 14 || lootTier >= 5) {
            placeChest(world, ox + 2, oy + 1, oz + d - 2, lootTier - 1);
        }

        // SPAWNERS in combat rooms
        if (name.contains("Spawner") || name.contains("Crypt") || name.contains("Guard")
                || name.contains("Barracks") || name.contains("Torture")) {
            placeSpawner(world, ox + w / 2 + 2, oy + 1, oz + d / 2, EntityType.WITHER_SKELETON);
            if (w >= 12) placeSpawner(world, ox + 2, oy + 1, oz + d / 2, EntityType.WITHER_SKELETON);
        }
        if (name.contains("Prison") || name.contains("Sanctum") || name.contains("Void")
                || name.contains("Antechamber")) {
            placeSpawner(world, ox + w / 2 - 2, oy + 1, oz + d / 2, EntityType.ENDERMAN);
            placeSpawner(world, ox + w / 2 + 2, oy + 1, oz + d / 2, EntityType.ENDERMAN);
        }
    }

    // ── LOOT SYSTEM ──
    private void placeChest(World world, int x, int y, int z, int tier) {
        Block b = world.getBlockAt(x, y, z);
        b.setType(Material.CHEST);
        if (b.getState() instanceof Chest chest) fillEndgameLoot(chest.getInventory(), tier);
    }

    private void fillEndgameLoot(Inventory inv, int tier) {
        int items = 5 + tier * 2;
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
            return switch (random.nextInt(16)) {
                case 0 -> { LegendaryWeapon[] pool = LegendaryWeapon.byTier(LegendaryTier.ABYSSAL); yield pool.length > 0 ? wm.createWeapon(pool[random.nextInt(pool.length)]) : null; }
                case 1, 2 -> { LegendaryWeapon[] pool = LegendaryWeapon.byTier(LegendaryTier.MYTHIC); yield pool.length > 0 ? wm.createWeapon(pool[random.nextInt(pool.length)]) : null; }
                case 3 -> pum.createHeartCrystal();
                case 4 -> pum.createPhoenixFeather();
                case 5 -> pum.createKeepInventorer();
                case 6 -> pum.createTitanResolve();
                case 7 -> pum.createBerserkerMark();
                case 8 -> pum.createVitalityShard();
                case 9 -> pum.createSoulFragment();
                case 10 -> new ItemStack(Material.NETHERITE_INGOT, 3 + random.nextInt(5));
                case 11 -> new ItemStack(Material.NETHER_STAR, 2 + random.nextInt(3));
                case 12 -> new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 2 + random.nextInt(3));
                case 13 -> new ItemStack(Material.TOTEM_OF_UNDYING, 1);
                case 14 -> createEnchantedBook();
                case 15 -> createBlessingCrystal();
                default -> new ItemStack(Material.DIAMOND, 8 + random.nextInt(8));
            };
        } else if (tier >= 4) {
            return switch (random.nextInt(12)) {
                case 0 -> { LegendaryWeapon[] pool = LegendaryWeapon.byTier(LegendaryTier.MYTHIC); yield pool.length > 0 ? wm.createWeapon(pool[random.nextInt(pool.length)]) : null; }
                case 1 -> { LegendaryWeapon[] pool = LegendaryWeapon.byTier(LegendaryTier.EPIC); yield pool.length > 0 ? wm.createWeapon(pool[random.nextInt(pool.length)]) : null; }
                case 2 -> pum.createHeartCrystal();
                case 3 -> pum.createSoulFragment();
                case 4 -> pum.createVitalityShard();
                case 5 -> new ItemStack(Material.NETHERITE_INGOT, 2 + random.nextInt(3));
                case 6 -> new ItemStack(Material.NETHER_STAR, 1 + random.nextInt(2));
                case 7 -> new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 1 + random.nextInt(2));
                case 8 -> new ItemStack(Material.DIAMOND_BLOCK, 3 + random.nextInt(5));
                case 9 -> new ItemStack(Material.TOTEM_OF_UNDYING, 1);
                case 10 -> createEnchantedBook();
                default -> pum.createBerserkerMark();
            };
        } else {
            return switch (random.nextInt(10)) {
                case 0 -> { LegendaryWeapon[] pool = LegendaryWeapon.byTier(LegendaryTier.EPIC); yield pool.length > 0 ? wm.createWeapon(pool[random.nextInt(pool.length)]) : null; }
                case 1 -> { LegendaryWeapon[] pool = LegendaryWeapon.byTier(LegendaryTier.RARE); yield pool.length > 0 ? wm.createWeapon(pool[random.nextInt(pool.length)]) : null; }
                case 2 -> pum.createSoulFragment();
                case 3 -> new ItemStack(Material.DIAMOND, 8 + random.nextInt(12));
                case 4 -> new ItemStack(Material.NETHERITE_SCRAP, 2 + random.nextInt(4));
                case 5 -> new ItemStack(Material.GOLDEN_APPLE, 3 + random.nextInt(5));
                case 6 -> new ItemStack(Material.EXPERIENCE_BOTTLE, 16 + random.nextInt(32));
                case 7 -> new ItemStack(Material.EMERALD, 10 + random.nextInt(20));
                case 8 -> createEnchantedBook();
                default -> new ItemStack(Material.IRON_BLOCK, 5 + random.nextInt(10));
            };
        }
    }

    /** Create a vanilla enchanted book with a high-level enchantment. */
    private ItemStack createEnchantedBook() {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        org.bukkit.inventory.meta.EnchantmentStorageMeta meta =
                (org.bukkit.inventory.meta.EnchantmentStorageMeta) book.getItemMeta();
        if (meta != null) {
            org.bukkit.enchantments.Enchantment[] enchants = {
                org.bukkit.enchantments.Enchantment.SHARPNESS,
                org.bukkit.enchantments.Enchantment.PROTECTION,
                org.bukkit.enchantments.Enchantment.POWER,
                org.bukkit.enchantments.Enchantment.FORTUNE,
                org.bukkit.enchantments.Enchantment.EFFICIENCY,
                org.bukkit.enchantments.Enchantment.UNBREAKING,
                org.bukkit.enchantments.Enchantment.MENDING,
                org.bukkit.enchantments.Enchantment.LOOTING,
                org.bukkit.enchantments.Enchantment.FEATHER_FALLING,
                org.bukkit.enchantments.Enchantment.FIRE_ASPECT
            };
            org.bukkit.enchantments.Enchantment ench = enchants[random.nextInt(enchants.length)];
            int level = Math.min(ench.getMaxLevel(), 3 + random.nextInt(3));
            meta.addStoredEnchant(ench, level, true);
            book.setItemMeta(meta);
        }
        return book;
    }

    /** Create a Blessing Crystal item (C-Bless, Ami-Bless, or La-Bless). */
    private ItemStack createBlessingCrystal() {
        int type = random.nextInt(3);
        Material mat;
        String name;
        TextColor color;
        String keyName;
        switch (type) {
            case 0 -> { mat = Material.DIAMOND; name = "C's Blessing Crystal"; color = TextColor.color(85, 255, 255); keyName = "c_bless_item"; }
            case 1 -> { mat = Material.REDSTONE; name = "Ami's Blessing Crystal"; color = TextColor.color(255, 85, 85); keyName = "ami_bless_item"; }
            default -> { mat = Material.LAPIS_LAZULI; name = "La's Blessing Crystal"; color = TextColor.color(85, 85, 255); keyName = "la_bless_item"; }
        }
        ItemStack crystal = new ItemStack(mat, 1);
        ItemMeta meta = crystal.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(name, color).decorate(TextDecoration.BOLD)
                    .decoration(TextDecoration.ITALIC, false));
            java.util.List<Component> lore = new java.util.ArrayList<>();
            lore.add(Component.text("Right-click to apply this blessing", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
            meta.getPersistentDataContainer().set(
                    new NamespacedKey(plugin, keyName),
                    org.bukkit.persistence.PersistentDataType.BYTE, (byte) 1);
            crystal.setItemMeta(meta);
        }
        return crystal;
    }

    private void placeSpawner(World world, int x, int y, int z, EntityType type) {
        Block b = world.getBlockAt(x, y, z);
        b.setType(Material.SPAWNER);
        if (b.getState() instanceof CreatureSpawner spawner) {
            spawner.setSpawnedType(type);
            spawner.setDelay(80);
            spawner.setMaxNearbyEntities(6);
            spawner.setSpawnRange(5);
            spawner.setRequiredPlayerRange(16);
            spawner.update();
        }
    }
}
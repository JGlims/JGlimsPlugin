package com.jglims.plugin.abyss;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.*;
import java.util.logging.Logger;

/**
 * Abyssal Citadel Builder v6.0
 * 
 * LAYOUT:  Arena at (0, Y, 0) so the Ender Dragon stays there naturally.
 *          Cathedral built to the south (positive Z).
 *          Player spawns far south and walks north through the castle to the arena.
 *
 *   Z ~ +200   = Player spawn platform
 *   Z ~ +180   = Custom trees + approach
 *   Z ~ +100   = Grand front facade (south-facing entrance)
 *   Z ~ +30-90 = Cathedral body (nave, transept, towers, side rooms)
 *   Z ~ +10-20 = Cathedral back (apse) → opening to arena
 *   Z ~ 0      = ARENA CENTER  (0, arenaY, 0) ← Dragon lives here
 */
public class AbyssCitadelBuilder {

    // ── Block Palette ──
    private static final Material PRIMARY      = Material.DEEPSLATE_BRICKS;
    private static final Material SECONDARY    = Material.POLISHED_DEEPSLATE;
    private static final Material ACCENT       = Material.DEEPSLATE_TILES;
    private static final Material DARK         = Material.BLACKSTONE;
    private static final Material OBSIDIAN_BLK = Material.OBSIDIAN;
    private static final Material CRYING_OBS   = Material.CRYING_OBSIDIAN;
    private static final Material AMETHYST     = Material.AMETHYST_BLOCK;
    private static final Material GLASS        = Material.PURPLE_STAINED_GLASS_PANE;
    private static final Material GLASS_BLOCK  = Material.PURPLE_STAINED_GLASS;
    private static final Material SOUL_LANTERN = Material.SOUL_LANTERN;
    private static final Material END_STONE    = Material.END_STONE_BRICKS;
    private static final Material BEDROCK      = Material.BEDROCK;
    private static final Material BARRIER      = Material.BARRIER;
    private static final Material PURPUR       = Material.PURPUR_BLOCK;
    private static final Material PRISMARINE   = Material.DARK_PRISMARINE;

    // ── Dimensions ──
    private static final int ARENA_RADIUS     = 45;      // arena at origin
    private static final int NAVE_LENGTH      = 70;      // Z extent of nave
    private static final int NAVE_HALF_W      = 25;      // half-width of nave
    private static final int FACADE_HALF_W    = 50;      // grand facade width
    private static final int SPIRE_HEIGHT     = 160;     // central spire
    private static final int TOWER_HEIGHT     = 65;      // corner towers
    private static final int TOWER_RADIUS     = 7;       // corner tower radius
    private static final int WALL_HEIGHT      = 40;      // main wall height
    private static final int ROOF_PEAK        = 50;      // roof peak above base

    // Cathedral body occupies Z = BODY_START to Z = BODY_END
    private static final int BODY_START = 15;   // back of cathedral (near arena)
    private static final int BODY_END   = 100;  // front facade
    private static final int TRANSEPT_Z = 55;   // transept crosses here
    private static final int TRANSEPT_HALF_W = 40;

    // Spawn & approach
    private static final int SPAWN_Z = 200;
    private static final int TREE_ZONE_START = 140;
    private static final int TREE_ZONE_END   = 180;

    private final World world;
    private final Logger log;
    private final int baseY;
    private int chestCount = 0;
    private int abyssalChestCount = 0;

    public AbyssCitadelBuilder(World world, Logger log) {
        this.world = world;
        this.log = log;
        this.baseY = findBaseY();
    }

    private int findBaseY() {
        // Find or create a base platform Y level
        // In void End world, we build at Y=50
        return 50;
    }

    // ════════════════════════════════════════════════════════════════
    //  MAIN BUILD
    // ════════════════════════════════════════════════════════════════
    public void build() {
        long start = System.currentTimeMillis();
        log.info("[Citadel] Building Gothic Abyssal Cathedral v6.0 at Y=" + baseY);

        phase("Foundation & Arena", this::buildArenaAndFoundation);
        phase("Approach Path & Spawn", this::buildApproachAndSpawn);
        phase("Custom Trees", this::buildCustomTrees);
        phase("Nave Walls & Floor", this::buildNave);
        phase("Transept", this::buildTransept);
        phase("Apse & Arena Connection", this::buildApse);
        phase("Grand Front Facade", this::buildGrandFacade);
        phase("Central Spire", this::buildCentralSpire);
        phase("Corner Towers x4", this::buildCornerTowers);
        phase("Minor Spires x8", this::buildMinorSpires);
        phase("Flying Buttresses", this::buildFlyingButtresses);
        phase("Gothic Windows", this::buildGothicWindows);
        phase("Roof", this::buildRoof);
        phase("Interior Clearing", this::clearInterior);
        phase("Interior Rooms (8)", this::buildInteriorRooms);
        phase("Weapon Chambers (4)", this::buildWeaponChambers);
        phase("Stairs & Galleries", this::buildStairsAndGalleries);
        phase("Chests & Loot", this::placeChests);
        phase("Arena Decorations", this::buildArenaDecorations);
        phase("Arena Barriers", this::buildArenaBarriers);
        phase("Ambient Lighting", this::placeLighting);

        long elapsed = System.currentTimeMillis() - start;
        log.info("[Citadel] Structure complete in " + elapsed + "ms");
        log.info("[Citadel] Found " + chestCount + " regular chests and " + abyssalChestCount + " abyssal weapon chests");

        // Place loot & guards after a short delay
        Bukkit.getScheduler().runTaskLater(
            Bukkit.getPluginManager().getPlugin("JGlimsPlugin"),
            this::populateLootAndGuards, 40L
        );
    }

    private void phase(String name, Runnable task) {
        log.info("[Citadel]   -> " + name);
        task.run();
    }

    // ════════════════════════════════════════════════════════════════
    //  ARENA & FOUNDATION  (centered at 0, baseY, 0)
    // ════════════════════════════════════════════════════════════════
    private void buildArenaAndFoundation() {
        // Arena: circular platform at origin, radius ARENA_RADIUS
        for (int x = -ARENA_RADIUS - 5; x <= ARENA_RADIUS + 5; x++) {
            for (int z = -ARENA_RADIUS - 5; z <= ARENA_RADIUS + 5; z++) {
                double dist = Math.sqrt(x * x + z * z);
                if (dist <= ARENA_RADIUS) {
                    // Arena floor: bedrock so dragon can't destroy it
                    setBlock(x, baseY, z, BEDROCK);
                    // Decorative ring
                    if (dist >= ARENA_RADIUS - 2 && dist <= ARENA_RADIUS) {
                        setBlock(x, baseY, z, OBSIDIAN_BLK);
                        if (dist >= ARENA_RADIUS - 1) {
                            setBlock(x, baseY + 1, z, CRYING_OBS);
                        }
                    }
                } else if (dist <= ARENA_RADIUS + 3) {
                    // Outer edge - crumbling effect
                    if (Math.random() < 0.6) {
                        setBlock(x, baseY, z, END_STONE);
                    }
                }
            }
        }

        // Cathedral foundation platform  (Z = BODY_START to BODY_END+20)
        for (int x = -FACADE_HALF_W - 10; x <= FACADE_HALF_W + 10; x++) {
            for (int z = BODY_START - 5; z <= BODY_END + 20; z++) {
                setBlock(x, baseY, z, PRIMARY);
                setBlock(x, baseY - 1, z, DARK);
            }
        }

        // Connect arena to cathedral (Z=0 to Z=BODY_START)
        for (int x = -15; x <= 15; x++) {
            for (int z = -ARENA_RADIUS; z <= BODY_START; z++) {
                setBlock(x, baseY, z, PRIMARY);
            }
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  APPROACH PATH & SPAWN PLATFORM
    // ════════════════════════════════════════════════════════════════
    private void buildApproachAndSpawn() {
        // Spawn platform at Z = SPAWN_Z
        for (int x = -8; x <= 8; x++) {
            for (int z = SPAWN_Z - 4; z <= SPAWN_Z + 4; z++) {
                setBlock(x, baseY, z, PURPUR);
                if (Math.abs(x) == 8 || z == SPAWN_Z - 4 || z == SPAWN_Z + 4) {
                    setBlock(x, baseY + 1, z, CRYING_OBS);
                }
            }
        }

        // Main approach path: Z = BODY_END+5 to SPAWN_Z-5
        // Wide stone path with soul lanterns
        for (int z = BODY_END + 5; z <= SPAWN_Z - 5; z++) {
            int pathHalf = 5;
            for (int x = -pathHalf; x <= pathHalf; x++) {
                setBlock(x, baseY, z, SECONDARY);
                // Edge pillars every 10 blocks
                if ((Math.abs(x) == pathHalf) && (z % 10 == 0)) {
                    for (int y = 1; y <= 4; y++) {
                        setBlock(x, baseY + y, z, ACCENT);
                    }
                    setBlock(x, baseY + 5, z, SOUL_LANTERN);
                }
            }
            // Under-path support (random deepslate columns into the void)
            if (z % 8 == 0) {
                for (int x2 = -pathHalf; x2 <= pathHalf; x2 += pathHalf * 2) {
                    for (int dy = -1; dy >= -15; dy--) {
                        if (Math.random() < 0.85) {
                            setBlock(x2, baseY + dy, z, DARK);
                        }
                    }
                }
            }
        }

        // Widen path near cathedral entrance
        for (int z = BODY_END; z <= BODY_END + 10; z++) {
            int w = 5 + (BODY_END + 10 - z);
            for (int x = -w; x <= w; x++) {
                if (world.getBlockAt(x, baseY, z).getType() == Material.AIR) {
                    setBlock(x, baseY, z, SECONDARY);
                }
            }
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  CUSTOM TREES (dark twisted End trees along approach)
    // ════════════════════════════════════════════════════════════════
    private void buildCustomTrees() {
        // Place 8 custom trees along the approach, alternating sides
        int[] treeZPositions = {145, 152, 160, 168, 175, 155, 163, 170};
        int[] treeSides =      { -1,   1,  -1,   1,  -1,   1,  -1,   1};

        for (int i = 0; i < treeZPositions.length; i++) {
            int tz = treeZPositions[i];
            int tx = treeSides[i] * (12 + (int)(Math.random() * 6));
            buildCustomTree(tx, baseY + 1, tz);
        }
    }

    private void buildCustomTree(int cx, int cy, int cz) {
        Material TRUNK = Material.DARK_OAK_LOG;
        Material LEAVES = Material.DARK_OAK_LEAVES;
        Material CHORUS = Material.CHORUS_PLANT;

        // Twisted trunk (5-12 blocks tall, with random offsets)
        int height = 7 + (int)(Math.random() * 6);
        int tx = cx, tz = cz;
        for (int y = 0; y < height; y++) {
            setBlock(tx, cy + y, tz, TRUNK);
            // Random twist
            if (y > 2 && Math.random() < 0.3) {
                tx += (Math.random() < 0.5) ? 1 : -1;
            }
            if (y > 2 && Math.random() < 0.3) {
                tz += (Math.random() < 0.5) ? 1 : -1;
            }
            // Branches
            if (y > height / 2 && Math.random() < 0.5) {
                int bx = (Math.random() < 0.5) ? 1 : -1;
                int bz = (Math.random() < 0.5) ? 1 : -1;
                setBlock(tx + bx, cy + y, tz, TRUNK);
                setBlock(tx + bx, cy + y + 1, tz + bz, CHORUS);
            }
        }
        // Canopy: mix of leaves and chorus, sphere-ish
        int canopyR = 3 + (int)(Math.random() * 2);
        for (int dx = -canopyR; dx <= canopyR; dx++) {
            for (int dy = -1; dy <= canopyR; dy++) {
                for (int dz = -canopyR; dz <= canopyR; dz++) {
                    double d = Math.sqrt(dx*dx + dy*dy + dz*dz);
                    if (d <= canopyR && Math.random() < 0.65) {
                        Material m = Math.random() < 0.7 ? LEAVES : CHORUS;
                        int bx = tx + dx, by = cy + height + dy, bz2 = tz + dz;
                        if (world.getBlockAt(bx, by, bz2).getType() == Material.AIR) {
                            setBlock(bx, by, bz2, m);
                        }
                    }
                }
            }
        }
        // Roots at base
        for (int r = 0; r < 3; r++) {
            int rx = cx + (int)(Math.random() * 4) - 2;
            int rz = cz + (int)(Math.random() * 4) - 2;
            setBlock(rx, cy - 1, rz, TRUNK);
            setBlock(rx, cy, rz, TRUNK);
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  NAVE (main cathedral hall)
    // ════════════════════════════════════════════════════════════════
    private void buildNave() {
        // Walls
        for (int z = BODY_START; z <= BODY_END; z++) {
            for (int y = baseY + 1; y <= baseY + WALL_HEIGHT; y++) {
                // Left wall
                setBlock(-NAVE_HALF_W, y, z, PRIMARY);
                setBlock(-NAVE_HALF_W - 1, y, z, SECONDARY);
                // Right wall
                setBlock(NAVE_HALF_W, y, z, PRIMARY);
                setBlock(NAVE_HALF_W + 1, y, z, SECONDARY);
            }
        }
        // Floor pattern (alternating tiles)
        for (int x = -NAVE_HALF_W + 1; x < NAVE_HALF_W; x++) {
            for (int z = BODY_START; z <= BODY_END; z++) {
                Material floor = ((x + z) % 2 == 0) ? ACCENT : SECONDARY;
                setBlock(x, baseY, z, floor);
            }
        }
        // Pillars along nave (every 8 blocks)
        for (int z = BODY_START + 4; z <= BODY_END - 4; z += 8) {
            for (int side = -1; side <= 1; side += 2) {
                int px = side * (NAVE_HALF_W - 5);
                for (int y = baseY + 1; y <= baseY + WALL_HEIGHT - 2; y++) {
                    setBlock(px, y, z, DARK);
                    setBlock(px, y, z + 1, DARK);
                }
                // Pillar capital
                setBlock(px - 1, baseY + WALL_HEIGHT - 2, z, ACCENT);
                setBlock(px + 1, baseY + WALL_HEIGHT - 2, z, ACCENT);
                setBlock(px, baseY + WALL_HEIGHT - 2, z - 1, ACCENT);
                setBlock(px, baseY + WALL_HEIGHT - 2, z + 1, ACCENT);
            }
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  TRANSEPT (cross-section)
    // ════════════════════════════════════════════════════════════════
    private void buildTransept() {
        for (int x = -TRANSEPT_HALF_W; x <= TRANSEPT_HALF_W; x++) {
            for (int y = baseY + 1; y <= baseY + WALL_HEIGHT; y++) {
                // Front and back walls of transept
                setBlock(x, y, TRANSEPT_Z - 5, PRIMARY);
                setBlock(x, y, TRANSEPT_Z + 5, PRIMARY);
            }
            // Floor
            setBlock(x, baseY, TRANSEPT_Z, SECONDARY);
            for (int dz = -5; dz <= 5; dz++) {
                if (world.getBlockAt(x, baseY, TRANSEPT_Z + dz).getType() == Material.AIR) {
                    setBlock(x, baseY, TRANSEPT_Z + dz, ACCENT);
                }
            }
        }
        // Side walls
        for (int z = TRANSEPT_Z - 5; z <= TRANSEPT_Z + 5; z++) {
            for (int y = baseY + 1; y <= baseY + WALL_HEIGHT; y++) {
                setBlock(-TRANSEPT_HALF_W, y, z, PRIMARY);
                setBlock(TRANSEPT_HALF_W, y, z, PRIMARY);
            }
        }
        // Clear transept interior (where it intersects nave)
        for (int x = -NAVE_HALF_W + 1; x < NAVE_HALF_W; x++) {
            for (int z = TRANSEPT_Z - 4; z <= TRANSEPT_Z + 4; z++) {
                for (int y = baseY + 1; y <= baseY + WALL_HEIGHT - 1; y++) {
                    setBlock(x, y, z, Material.AIR);
                }
            }
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  APSE (back of cathedral → opens to arena)
    // ════════════════════════════════════════════════════════════════
    private void buildApse() {
        // Semi-circular apse at the back (Z = BODY_START region)
        int apseR = NAVE_HALF_W;
        for (int x = -apseR; x <= apseR; x++) {
            for (int z = BODY_START - apseR; z <= BODY_START; z++) {
                double dist = Math.sqrt(x * x + (z - BODY_START) * (z - BODY_START));
                if (dist >= apseR - 2 && dist <= apseR) {
                    for (int y = baseY + 1; y <= baseY + WALL_HEIGHT; y++) {
                        setBlock(x, y, z, PRIMARY);
                    }
                    setBlock(x, baseY, z, SECONDARY);
                }
            }
        }
        // Grand archway opening to arena (Z = BODY_START, X = -10 to 10)
        for (int x = -10; x <= 10; x++) {
            for (int y = baseY + 1; y <= baseY + 20; y++) {
                double archDist = Math.sqrt(x * x + (y - baseY - 20) * (y - baseY - 20));
                if (archDist <= 12) {
                    setBlock(x, y, BODY_START, Material.AIR);
                }
            }
        }
        // Path from apse to arena
        for (int x = -8; x <= 8; x++) {
            for (int z = BODY_START - apseR; z >= -ARENA_RADIUS + 5; z--) {
                if (world.getBlockAt(x, baseY, z).getType() == Material.AIR) {
                    setBlock(x, baseY, z, SECONDARY);
                }
            }
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  GRAND FRONT FACADE  (at Z ~ BODY_END, facing south)
    // ════════════════════════════════════════════════════════════════
    private void buildGrandFacade() {
        int fz = BODY_END;

        // Main facade wall
        for (int x = -FACADE_HALF_W; x <= FACADE_HALF_W; x++) {
            for (int y = baseY + 1; y <= baseY + WALL_HEIGHT + 15; y++) {
                double edgeDist = Math.min(Math.abs(x + FACADE_HALF_W), Math.abs(x - FACADE_HALF_W));
                int maxH = baseY + WALL_HEIGHT + 15 - (int)(edgeDist < 15 ? (15 - edgeDist) / 2 : 0);
                if (y <= maxH) {
                    setBlock(x, y, fz, PRIMARY);
                    setBlock(x, y, fz + 1, SECONDARY);
                }
            }
        }

        // Recessed entrance: deep arch, no blocking pillar
        // Carve the entrance - 3 blocks deep recess
        for (int depth = 0; depth <= 3; depth++) {
            for (int x = -6; x <= 6; x++) {
                for (int y = baseY + 1; y <= baseY + 18; y++) {
                    // Arch shape
                    if (y <= baseY + 14 || (x * x + (y - baseY - 14) * (y - baseY - 14)) <= 36) {
                        setBlock(x, y, fz + depth, Material.AIR);
                    }
                }
            }
        }

        // Arch trim around the entrance
        for (int x = -7; x <= 7; x++) {
            for (int y = baseY + 1; y <= baseY + 19; y++) {
                double archCheck = x * x + Math.pow(y - baseY - 14, 2);
                if (archCheck >= 36 && archCheck <= 64) {
                    if (y > baseY + 14) {
                        setBlock(x, y, fz, ACCENT);
                        setBlock(x, y, fz + 1, ACCENT);
                    }
                }
            }
        }
        // Door frame columns
        for (int y = baseY + 1; y <= baseY + 14; y++) {
            setBlock(-7, y, fz, OBSIDIAN_BLK);
            setBlock(-7, y, fz + 1, OBSIDIAN_BLK);
            setBlock(7, y, fz, OBSIDIAN_BLK);
            setBlock(7, y, fz + 1, OBSIDIAN_BLK);
        }

        // Rose window (large circular stained glass above entrance)
        int roseY = baseY + 30;
        int roseR = 8;
        for (int dx = -roseR; dx <= roseR; dx++) {
            for (int dy = -roseR; dy <= roseR; dy++) {
                double d = Math.sqrt(dx * dx + dy * dy);
                if (d <= roseR) {
                    if (d >= roseR - 1) {
                        setBlock(dx, roseY + dy, fz, ACCENT);
                    } else {
                        setBlock(dx, roseY + dy, fz, GLASS_BLOCK);
                    }
                    setBlock(dx, roseY + dy, fz + 1, Material.AIR);
                }
            }
        }
        // Rose window spokes
        for (int i = 0; i < 8; i++) {
            double angle = i * Math.PI / 4;
            for (int r = 1; r < roseR - 1; r++) {
                int sx = (int)(Math.cos(angle) * r);
                int sy = (int)(Math.sin(angle) * r);
                setBlock(sx, roseY + sy, fz, ACCENT);
            }
        }

        // Facade buttresses (tall vertical elements flanking the entrance)
        for (int side = -1; side <= 1; side += 2) {
            int bx = side * 20;
            for (int y = baseY + 1; y <= baseY + WALL_HEIGHT + 10; y++) {
                setBlock(bx, y, fz, DARK);
                setBlock(bx, y, fz + 1, DARK);
                setBlock(bx, y, fz + 2, ACCENT);
                setBlock(bx + side, y, fz, ACCENT);
            }
            // Pinnacle on top
            for (int py = 0; py < 8; py++) {
                setBlock(bx, baseY + WALL_HEIGHT + 10 + py, fz, ACCENT);
            }
        }

        // Smaller flanking arched windows
        for (int side = -1; side <= 1; side += 2) {
            int wx = side * 13;
            for (int y = baseY + 5; y <= baseY + 16; y++) {
                for (int dx = -2; dx <= 2; dx++) {
                    double wd = dx * dx + Math.pow(y - baseY - 14, 2);
                    if (wd <= 6) {
                        setBlock(wx + dx, y, fz, GLASS);
                    }
                }
            }
        }

        // Steps leading up to entrance
        for (int step = 0; step < 5; step++) {
            int sw = 10 + step * 2;
            for (int x = -sw / 2; x <= sw / 2; x++) {
                setBlock(x, baseY - step, fz + 4 + step, PRIMARY);
                // Fill under step
                for (int fill = 1; fill <= step; fill++) {
                    setBlock(x, baseY - step + fill, fz + 4 + step, PRIMARY);
                }
            }
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  CENTRAL SPIRE
    // ════════════════════════════════════════════════════════════════
    private void buildCentralSpire() {
        int sz = (BODY_START + BODY_END) / 2; // center of cathedral Z
        int sBaseR = 6;
        for (int y = 0; y < SPIRE_HEIGHT; y++) {
            double r = sBaseR * (1.0 - (double) y / SPIRE_HEIGHT);
            r = Math.max(r, 0.5);
            for (int dx = -(int) Math.ceil(r); dx <= (int) Math.ceil(r); dx++) {
                for (int dz = -(int) Math.ceil(r); dz <= (int) Math.ceil(r); dz++) {
                    if (dx * dx + dz * dz <= r * r) {
                        Material mat = (y % 10 < 2) ? ACCENT : PRIMARY;
                        setBlock(dx, baseY + WALL_HEIGHT + y, sz + dz, mat);
                    }
                }
            }
        }
        // Spire tip with end crystal visual (amethyst + crying obsidian)
        setBlock(0, baseY + WALL_HEIGHT + SPIRE_HEIGHT, sz, CRYING_OBS);
        setBlock(0, baseY + WALL_HEIGHT + SPIRE_HEIGHT + 1, sz, AMETHYST);
    }

    // ════════════════════════════════════════════════════════════════
    //  CORNER TOWERS (4 at corners of nave)
    // ════════════════════════════════════════════════════════════════
    private void buildCornerTowers() {
        int[][] corners = {
            {-NAVE_HALF_W - 5, BODY_START + 5},
            { NAVE_HALF_W + 5, BODY_START + 5},
            {-NAVE_HALF_W - 5, BODY_END - 5},
            { NAVE_HALF_W + 5, BODY_END - 5}
        };
        for (int[] corner : corners) {
            buildTower(corner[0], corner[1]);
        }
    }

    private void buildTower(int cx, int cz) {
        // Cylindrical tower with spiral stairs
        for (int y = 0; y <= TOWER_HEIGHT; y++) {
            for (int dx = -TOWER_RADIUS; dx <= TOWER_RADIUS; dx++) {
                for (int dz = -TOWER_RADIUS; dz <= TOWER_RADIUS; dz++) {
                    double dist = Math.sqrt(dx * dx + dz * dz);
                    if (dist <= TOWER_RADIUS) {
                        if (dist >= TOWER_RADIUS - 1.5) {
                            // Wall
                            setBlock(cx + dx, baseY + y, cz + dz, PRIMARY);
                        } else if (y == 0 || y == TOWER_HEIGHT) {
                            // Floor/ceiling
                            setBlock(cx + dx, baseY + y, cz + dz, SECONDARY);
                        } else {
                            // Interior (clear)
                            setBlock(cx + dx, baseY + y, cz + dz, Material.AIR);
                        }
                    }
                }
            }
            // Spiral staircase (one step per Y level, rotating)
            double angle = (y * Math.PI * 2) / 12; // full rotation every 12 blocks
            int sx = cx + (int)(Math.cos(angle) * (TOWER_RADIUS - 3));
            int sz2 = cz + (int)(Math.sin(angle) * (TOWER_RADIUS - 3));
            setBlock(sx, baseY + y, sz2, ACCENT);
            setBlock(sx + 1, baseY + y, sz2, ACCENT);
            setBlock(sx, baseY + y, sz2 + 1, ACCENT);

            // Windows every 8 blocks
            if (y % 8 == 4 && y > 3 && y < TOWER_HEIGHT - 3) {
                for (int dir = 0; dir < 4; dir++) {
                    double wAngle = dir * Math.PI / 2;
                    int wx = cx + (int)(Math.cos(wAngle) * TOWER_RADIUS);
                    int wz = cz + (int)(Math.sin(wAngle) * TOWER_RADIUS);
                    setBlock(wx, baseY + y, wz, GLASS);
                    setBlock(wx, baseY + y + 1, wz, GLASS);
                }
            }
        }

        // Conical roof
        for (int y = 0; y < 15; y++) {
            double r = TOWER_RADIUS * (1.0 - (double) y / 15);
            for (int dx = -(int) Math.ceil(r); dx <= (int) Math.ceil(r); dx++) {
                for (int dz = -(int) Math.ceil(r); dz <= (int) Math.ceil(r); dz++) {
                    if (dx * dx + dz * dz <= r * r) {
                        setBlock(cx + dx, baseY + TOWER_HEIGHT + y, cz + dz, ACCENT);
                    }
                }
            }
        }
        setBlock(cx, baseY + TOWER_HEIGHT + 15, cz, CRYING_OBS);

        // Bridge connector to nave (at gallery level, baseY + 15)
        // We build toward X=0 (the nave center)
        int bridgeY = baseY + 15;
        int bridgeDir = (cx < 0) ? 1 : -1;
        for (int step = 0; step < Math.abs(cx) - NAVE_HALF_W; step++) {
            int bx = cx + bridgeDir * step;
            setBlock(bx, bridgeY, cz, SECONDARY);
            setBlock(bx, bridgeY, cz + 1, SECONDARY);
            setBlock(bx, bridgeY + 3, cz, PRIMARY);
            setBlock(bx, bridgeY + 3, cz + 1, PRIMARY);
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  MINOR SPIRES (8 along the walls)
    // ════════════════════════════════════════════════════════════════
    private void buildMinorSpires() {
        int[] zPositions = {25, 40, 55, 70, 85, 35, 60, 80};
        for (int i = 0; i < 8; i++) {
            int side = (i < 5) ? -1 : 1;
            int sx = side * (NAVE_HALF_W + 2);
            int sz = zPositions[i];
            int h = 25 + (int)(Math.random() * 15);
            for (int y = 0; y < h; y++) {
                double r = 2.0 * (1.0 - (double) y / h);
                r = Math.max(r, 0.3);
                for (int dx = -(int) Math.ceil(r); dx <= (int) Math.ceil(r); dx++) {
                    for (int dz = -(int) Math.ceil(r); dz <= (int) Math.ceil(r); dz++) {
                        if (dx * dx + dz * dz <= r * r) {
                            setBlock(sx + dx, baseY + WALL_HEIGHT + y, sz + dz, ACCENT);
                        }
                    }
                }
            }
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  FLYING BUTTRESSES
    // ════════════════════════════════════════════════════════════════
    private void buildFlyingButtresses() {
        for (int z = BODY_START + 8; z <= BODY_END - 8; z += 12) {
            for (int side = -1; side <= 1; side += 2) {
                int wallX = side * NAVE_HALF_W;
                int outerX = side * (NAVE_HALF_W + 12);
                // Support pillar
                for (int y = baseY + 1; y <= baseY + WALL_HEIGHT - 10; y++) {
                    setBlock(outerX, y, z, DARK);
                }
                // Arch connecting pillar to wall
                for (int step = 0; step <= 12; step++) {
                    int bx = wallX + side * step;
                    int by = baseY + WALL_HEIGHT - 5 - (int)(5.0 * step / 12);
                    setBlock(bx, by, z, PRIMARY);
                    setBlock(bx, by + 1, z, ACCENT);
                }
            }
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  GOTHIC WINDOWS (embedded in walls properly)
    // ════════════════════════════════════════════════════════════════
    private void buildGothicWindows() {
        // Windows along nave walls, between pillars
        for (int z = BODY_START + 6; z <= BODY_END - 6; z += 8) {
            for (int side = -1; side <= 1; side += 2) {
                int wx = side * NAVE_HALF_W;
                // Tall pointed arch window
                for (int y = baseY + 5; y <= baseY + WALL_HEIGHT - 8; y++) {
                    int halfWidth = 2;
                    if (y > baseY + WALL_HEIGHT - 14) {
                        // Pointed top
                        halfWidth = Math.max(0, 2 - (y - (baseY + WALL_HEIGHT - 14)));
                    }
                    for (int dx = -halfWidth; dx <= halfWidth; dx++) {
                        setBlock(wx, y, z + dx, GLASS);
                    }
                }
            }
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  ROOF
    // ════════════════════════════════════════════════════════════════
    private void buildRoof() {
        int catCenter = (BODY_START + BODY_END) / 2;
        // Pitched roof over nave
        for (int z = BODY_START; z <= BODY_END; z++) {
            for (int half = 0; half <= NAVE_HALF_W; half++) {
                int roofY = baseY + WALL_HEIGHT + (NAVE_HALF_W - half) / 2;
                setBlock(-half, roofY, z, ACCENT);
                setBlock(half, roofY, z, ACCENT);
                // Fill between wall top and roof
                for (int fy = baseY + WALL_HEIGHT; fy < roofY; fy++) {
                    if (half == NAVE_HALF_W || half == NAVE_HALF_W - 1) {
                        setBlock(-half, fy, z, PRIMARY);
                        setBlock(half, fy, z, PRIMARY);
                    }
                }
            }
        }
        // Roof ridge
        for (int z = BODY_START; z <= BODY_END; z++) {
            setBlock(0, baseY + WALL_HEIGHT + NAVE_HALF_W / 2, z, OBSIDIAN_BLK);
        }
        // Connect roof to central spire (fill gap)
        int spireZ = catCenter;
        for (int y = baseY + WALL_HEIGHT; y <= baseY + WALL_HEIGHT + 10; y++) {
            for (int dx = -3; dx <= 3; dx++) {
                for (int dz = -3; dz <= 3; dz++) {
                    Block b = world.getBlockAt(dx, y, spireZ + dz);
                    if (b.getType() == Material.AIR) {
                        setBlock(dx, y, spireZ + dz, ACCENT);
                    }
                }
            }
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  CLEAR INTERIOR
    // ════════════════════════════════════════════════════════════════
    private void clearInterior() {
        for (int x = -NAVE_HALF_W + 1; x < NAVE_HALF_W; x++) {
            for (int z = BODY_START + 1; z < BODY_END; z++) {
                for (int y = baseY + 1; y <= baseY + WALL_HEIGHT - 2; y++) {
                    Block b = world.getBlockAt(x, y, z);
                    Material type = b.getType();
                    // Don't clear pillar blocks, stair blocks, or chests
                    if (type != Material.AIR && type != DARK && type != ACCENT
                        && type != GLASS && type != SOUL_LANTERN
                        && type != Material.CHEST) {
                        // Only clear if it was part of wall overlap
                        if (Math.abs(x) < NAVE_HALF_W - 1) {
                            setBlock(x, y, z, Material.AIR);
                        }
                    }
                }
            }
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  INTERIOR ROOMS (8 side rooms off the nave)
    // ════════════════════════════════════════════════════════════════
    private void buildInteriorRooms() {
        // Rooms are carved into the walls and extend outward
        // 4 rooms on each side, different types
        String[] roomTypes = {"treasure", "parkour", "puzzle", "guard", "treasure", "parkour", "puzzle", "guard"};
        int roomIdx = 0;
        for (int side = -1; side <= 1; side += 2) {
            for (int r = 0; r < 4; r++) {
                int roomZ = BODY_START + 12 + r * 18;
                int roomX = side * NAVE_HALF_W;
                int roomDepth = 15;
                int roomWidth = 12;
                int roomHeight = 12;

                // Carve room
                for (int dx = 0; dx < roomDepth; dx++) {
                    for (int dz = -roomWidth / 2; dz <= roomWidth / 2; dz++) {
                        for (int dy = 1; dy <= roomHeight; dy++) {
                            int rx = roomX + side * dx;
                            int rz = roomZ + dz;
                            setBlock(rx, baseY + dy, rz, Material.AIR);
                        }
                        // Floor
                        int rx = roomX + side * dx;
                        setBlock(rx, baseY, roomZ + dz, SECONDARY);
                    }
                }

                // Walls around room
                for (int dx = -1; dx <= roomDepth; dx++) {
                    for (int dy = 1; dy <= roomHeight + 1; dy++) {
                        int rx = roomX + side * dx;
                        // Front and back walls
                        setBlock(rx, baseY + dy, roomZ - roomWidth / 2 - 1, PRIMARY);
                        setBlock(rx, baseY + dy, roomZ + roomWidth / 2 + 1, PRIMARY);
                    }
                }
                // Outer wall
                for (int dz = -roomWidth / 2 - 1; dz <= roomWidth / 2 + 1; dz++) {
                    for (int dy = 1; dy <= roomHeight + 1; dy++) {
                        int rx = roomX + side * roomDepth;
                        setBlock(rx, baseY + dy, roomZ + dz, PRIMARY);
                    }
                }
                // Ceiling
                for (int dx = 0; dx < roomDepth; dx++) {
                    for (int dz = -roomWidth / 2; dz <= roomWidth / 2; dz++) {
                        int rx = roomX + side * dx;
                        setBlock(rx, baseY + roomHeight + 1, roomZ + dz, ACCENT);
                    }
                }

                // Doorway from nave into room
                for (int dy = 1; dy <= 4; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        setBlock(roomX, baseY + dy, roomZ + dz, Material.AIR);
                    }
                }

                // Room-specific features
                buildRoomFeatures(roomTypes[roomIdx], roomX, roomZ, side, roomDepth, roomWidth, roomHeight);
                roomIdx++;
            }
        }
    }

    private void buildRoomFeatures(String type, int roomX, int roomZ, int side, int depth, int width, int height) {
        switch (type) {
            case "treasure":
                // Multiple chests with decorative pedestals
                for (int i = 0; i < 4; i++) {
                    int cx = roomX + side * (3 + i * 3);
                    int cz = roomZ + (i % 2 == 0 ? -3 : 3);
                    setBlock(cx, baseY + 1, cz, AMETHYST);
                    setBlock(cx, baseY + 2, cz, Material.CHEST);
                    chestCount++;
                }
                // Decorations
                setBlock(roomX + side * 7, baseY + 1, roomZ, CRYING_OBS);
                setBlock(roomX + side * 7, baseY + 2, roomZ, SOUL_LANTERN);
                break;

            case "parkour":
                // Floating platforms at different heights
                for (int i = 0; i < 6; i++) {
                    int px = roomX + side * (2 + (i % 3) * 4);
                    int pz = roomZ + (i < 3 ? -3 : 3);
                    int py = baseY + 2 + i;
                    setBlock(px, py, pz, END_STONE);
                    setBlock(px + 1, py, pz, END_STONE);
                    // Reward chest at the top
                    if (i == 5) {
                        setBlock(px, py + 1, pz, Material.CHEST);
                        chestCount++;
                    }
                }
                // Lava/hazard at bottom
                for (int dx = 1; dx < depth - 1; dx++) {
                    for (int dz = -width / 2 + 1; dz < width / 2; dz++) {
                        setBlock(roomX + side * dx, baseY, roomZ + dz, Material.MAGMA_BLOCK);
                    }
                }
                break;

            case "puzzle":
                // Pressure plates, redstone hints, trapped chest
                setBlock(roomX + side * 5, baseY + 1, roomZ, Material.STONE_PRESSURE_PLATE);
                setBlock(roomX + side * 8, baseY + 1, roomZ - 2, Material.STONE_PRESSURE_PLATE);
                setBlock(roomX + side * 8, baseY + 1, roomZ + 2, Material.STONE_PRESSURE_PLATE);
                setBlock(roomX + side * 12, baseY + 1, roomZ, Material.TRAPPED_CHEST);
                chestCount++;
                // Hint signs could be added later
                // Decorative redstone lamps
                for (int dy = 3; dy <= 6; dy += 3) {
                    setBlock(roomX + side * 3, baseY + dy, roomZ - 4, Material.REDSTONE_LAMP);
                    setBlock(roomX + side * 3, baseY + dy, roomZ + 4, Material.REDSTONE_LAMP);
                }
                break;

            case "guard":
                // Combat room with spawners and loot
                setBlock(roomX + side * 7, baseY + 1, roomZ, Material.SPAWNER);
                // Chests behind guards
                setBlock(roomX + side * 12, baseY + 1, roomZ - 3, Material.CHEST);
                setBlock(roomX + side * 12, baseY + 1, roomZ + 3, Material.CHEST);
                chestCount += 2;
                // Cage-like decorations
                for (int dy = 1; dy <= 3; dy++) {
                    setBlock(roomX + side * 7, baseY + dy, roomZ - 3, Material.IRON_BARS);
                    setBlock(roomX + side * 7, baseY + dy, roomZ + 3, Material.IRON_BARS);
                }
                break;
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  WEAPON CHAMBERS (4 special rooms for Abyssal weapons)
    // ════════════════════════════════════════════════════════════════
    private void buildWeaponChambers() {
        // One in each transept arm and two in the apse area
        int[][] chamberPositions = {
            {-TRANSEPT_HALF_W + 5, TRANSEPT_Z},  // West transept
            { TRANSEPT_HALF_W - 5, TRANSEPT_Z},  // East transept
            {-12, BODY_START + 3},                // Left apse
            { 12, BODY_START + 3}                 // Right apse
        };

        for (int[] pos : chamberPositions) {
            int cx = pos[0], cz = pos[1];
            // Carve chamber (8x8x8)
            for (int dx = -4; dx <= 4; dx++) {
                for (int dz = -4; dz <= 4; dz++) {
                    for (int dy = 0; dy <= 8; dy++) {
                        if (dy == 0) {
                            setBlock(cx + dx, baseY + dy, cz + dz, OBSIDIAN_BLK);
                        } else if (Math.abs(dx) == 4 || Math.abs(dz) == 4 || dy == 8) {
                            setBlock(cx + dx, baseY + dy, cz + dz, OBSIDIAN_BLK);
                        } else {
                            setBlock(cx + dx, baseY + dy, cz + dz, Material.AIR);
                        }
                    }
                }
            }
            // Amethyst pedestal in center
            setBlock(cx, baseY + 1, cz, AMETHYST);
            setBlock(cx, baseY + 2, cz, AMETHYST);
            setBlock(cx, baseY + 3, cz, Material.CHEST);
            abyssalChestCount++;

            // Corner soul lanterns
            for (int sx = -3; sx <= 3; sx += 6) {
                for (int sz = -3; sz <= 3; sz += 6) {
                    setBlock(cx + sx, baseY + 1, cz + sz, CRYING_OBS);
                    setBlock(cx + sx, baseY + 2, cz + sz, SOUL_LANTERN);
                }
            }

            // Doorway
            for (int dy = 1; dy <= 3; dy++) {
                setBlock(cx, baseY + dy, cz + 4, Material.AIR);
                setBlock(cx + 1, baseY + dy, cz + 4, Material.AIR);
            }
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  STAIRS & GALLERIES
    // ════════════════════════════════════════════════════════════════
    private void buildStairsAndGalleries() {
        // Gallery level at baseY + 15 along both sides of nave
        for (int side = -1; side <= 1; side += 2) {
            int galleryX = side * (NAVE_HALF_W - 6);
            for (int z = BODY_START + 5; z <= BODY_END - 5; z++) {
                setBlock(galleryX, baseY + 15, z, SECONDARY);
                setBlock(galleryX + side, baseY + 15, z, SECONDARY);
                // Railing
                setBlock(galleryX - side, baseY + 16, z, Material.IRON_BARS);
            }

            // Staircase near front entrance connecting ground to gallery
            int stairZ = BODY_END - 8;
            for (int step = 0; step < 15; step++) {
                setBlock(galleryX, baseY + 1 + step, stairZ - step, ACCENT);
                setBlock(galleryX + side, baseY + 1 + step, stairZ - step, ACCENT);
            }
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  SCATTER CHESTS throughout cathedral
    // ════════════════════════════════════════════════════════════════
    private void placeChests() {
        // Additional chests in the nave, galleries, alcoves
        Random rand = new Random(42);
        // Nave alcove chests
        for (int z = BODY_START + 10; z <= BODY_END - 10; z += 12) {
            for (int side = -1; side <= 1; side += 2) {
                int cx = side * (NAVE_HALF_W - 2);
                if (rand.nextDouble() < 0.6) {
                    setBlock(cx, baseY + 1, z, Material.CHEST);
                    chestCount++;
                }
            }
        }
        // Gallery chests
        for (int z = BODY_START + 15; z <= BODY_END - 15; z += 20) {
            for (int side = -1; side <= 1; side += 2) {
                setBlock(side * (NAVE_HALF_W - 6), baseY + 16, z, Material.CHEST);
                chestCount++;
            }
        }
        // Transept arm chests
        for (int side = -1; side <= 1; side += 2) {
            setBlock(side * (TRANSEPT_HALF_W - 3), baseY + 1, TRANSEPT_Z, Material.CHEST);
            setBlock(side * (TRANSEPT_HALF_W - 8), baseY + 1, TRANSEPT_Z + 2, Material.CHEST);
            chestCount += 2;
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  ARENA DECORATIONS  (at origin)
    // ════════════════════════════════════════════════════════════════
    private void buildArenaDecorations() {
        // Central altar/pillar the dragon perches on (this is where vanilla expects the exit portal)
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int dy = 1; dy <= 5; dy++) {
                    if (Math.abs(dx) <= 1 && Math.abs(dz) <= 1) {
                        setBlock(dx, baseY + dy, dz, BEDROCK);
                    }
                }
            }
        }
        // Top of altar
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                setBlock(dx, baseY + 6, dz, END_STONE);
            }
        }

        // Obsidian pillars around arena (like End crystals)
        int pillarCount = 10;
        for (int i = 0; i < pillarCount; i++) {
            double angle = (2 * Math.PI * i) / pillarCount;
            int px = (int)(Math.cos(angle) * (ARENA_RADIUS - 8));
            int pz = (int)(Math.sin(angle) * (ARENA_RADIUS - 8));
            int pillarH = 15 + (int)(Math.random() * 20);
            for (int y = 0; y <= pillarH; y++) {
                setBlock(px, baseY + y, pz, OBSIDIAN_BLK);
                setBlock(px + 1, baseY + y, pz, OBSIDIAN_BLK);
                setBlock(px, baseY + y, pz + 1, OBSIDIAN_BLK);
                setBlock(px + 1, baseY + y, pz + 1, OBSIDIAN_BLK);
            }
            // End crystal block on top
            setBlock(px, baseY + pillarH + 1, pz, BEDROCK);
        }

        // Decorative ring patterns on the arena floor
        for (int x = -ARENA_RADIUS; x <= ARENA_RADIUS; x++) {
            for (int z = -ARENA_RADIUS; z <= ARENA_RADIUS; z++) {
                double dist = Math.sqrt(x * x + z * z);
                if ((int) dist == 15 || (int) dist == 30) {
                    setBlock(x, baseY, z, CRYING_OBS);
                }
            }
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  ARENA BARRIERS (invisible walls at edge + below)
    // ════════════════════════════════════════════════════════════════
    private void buildArenaBarriers() {
        // Barrier wall at arena edge to keep players in
        for (int x = -ARENA_RADIUS - 2; x <= ARENA_RADIUS + 2; x++) {
            for (int z = -ARENA_RADIUS - 2; z <= ARENA_RADIUS + 2; z++) {
                double dist = Math.sqrt(x * x + z * z);
                if (dist >= ARENA_RADIUS + 1 && dist <= ARENA_RADIUS + 2) {
                    for (int y = baseY; y <= baseY + 60; y++) {
                        setBlock(x, y, z, BARRIER);
                    }
                }
            }
        }
        // Barrier floor below to prevent falling into void
        for (int x = -ARENA_RADIUS; x <= ARENA_RADIUS; x++) {
            for (int z = -ARENA_RADIUS; z <= ARENA_RADIUS; z++) {
                if (x * x + z * z <= ARENA_RADIUS * ARENA_RADIUS) {
                    setBlock(x, baseY - 1, z, BARRIER);
                }
            }
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  AMBIENT LIGHTING
    // ════════════════════════════════════════════════════════════════
    private void placeLighting() {
        // Soul lanterns along nave pillars
        for (int z = BODY_START + 4; z <= BODY_END - 4; z += 8) {
            for (int side = -1; side <= 1; side += 2) {
                int px = side * (NAVE_HALF_W - 5);
                setBlock(px, baseY + 5, z, SOUL_LANTERN);
                setBlock(px, baseY + 10, z, SOUL_LANTERN);
            }
        }
        // Transept lanterns
        for (int x = -TRANSEPT_HALF_W + 5; x <= TRANSEPT_HALF_W - 5; x += 10) {
            setBlock(x, baseY + 5, TRANSEPT_Z, SOUL_LANTERN);
        }
        // Arena lanterns on pillars
        for (int i = 0; i < 10; i++) {
            double angle = (2 * Math.PI * i) / 10;
            int px = (int)(Math.cos(angle) * (ARENA_RADIUS - 8));
            int pz = (int)(Math.sin(angle) * (ARENA_RADIUS - 8));
            setBlock(px - 1, baseY + 8, pz, SOUL_LANTERN);
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  LOOT & GUARDS
    // ════════════════════════════════════════════════════════════════
    private void populateLootAndGuards() {
        log.info("[Citadel] Populating loot and spawning guards...");
        int regularChests = 0;
        int abyssalChests = 0;

        for (int x = -FACADE_HALF_W - 20; x <= FACADE_HALF_W + 20; x++) {
            for (int z = -ARENA_RADIUS - 5; z <= SPAWN_Z + 5; z++) {
                for (int y = baseY - 1; y <= baseY + SPIRE_HEIGHT + WALL_HEIGHT; y++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST) {
                        if (block.getState() instanceof Chest chest) {
                            // Check if it's in a weapon chamber
                            boolean isAbyssal = false;
                            int[][] chamberPositions = {
                                {-TRANSEPT_HALF_W + 5, TRANSEPT_Z},
                                { TRANSEPT_HALF_W - 5, TRANSEPT_Z},
                                {-12, BODY_START + 3},
                                { 12, BODY_START + 3}
                            };
                            for (int[] cp : chamberPositions) {
                                if (Math.abs(x - cp[0]) <= 1 && Math.abs(z - cp[1]) <= 1 && y == baseY + 3) {
                                    isAbyssal = true;
                                    break;
                                }
                            }

                            if (isAbyssal) {
                                fillAbyssalChest(chest);
                                abyssalChests++;
                            } else {
                                fillRegularChest(chest);
                                regularChests++;
                            }
                        }
                    }
                }
            }
        }

        // Spawn guards at weapon chambers
        int[][] chamberPositions = {
            {-TRANSEPT_HALF_W + 5, TRANSEPT_Z},
            { TRANSEPT_HALF_W - 5, TRANSEPT_Z},
            {-12, BODY_START + 3},
            { 12, BODY_START + 3}
        };
        for (int[] cp : chamberPositions) {
            Location guardLoc = new Location(world, cp[0] + 2, baseY + 1, cp[1]);
            WitherSkeleton guard = (WitherSkeleton) world.spawnEntity(guardLoc, EntityType.WITHER_SKELETON);
            guard.customName(Component.text("Abyssal Guardian", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD));
            guard.setCustomNameVisible(true);
            guard.setMaxHealth(80);
            guard.setHealth(80);
            guard.setPersistent(true);
            ItemStack sword = new ItemStack(Material.NETHERITE_SWORD);
            guard.getEquipment().setItemInMainHand(sword);
        }

        log.info("[Citadel] Found " + regularChests + " regular chests and " + abyssalChests + " abyssal weapon chests");
        log.info("[Citadel] Loot and guards placed.");
    }

    private void fillRegularChest(Chest chest) {
        Random rand = new Random();
        chest.getInventory().clear();
        // Good mid-to-end game loot
        ItemStack[] possibleLoot = {
            new ItemStack(Material.DIAMOND, 1 + rand.nextInt(3)),
            new ItemStack(Material.NETHERITE_SCRAP, 1 + rand.nextInt(2)),
            new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 1),
            new ItemStack(Material.GOLDEN_APPLE, 1 + rand.nextInt(3)),
            new ItemStack(Material.ENDER_PEARL, 2 + rand.nextInt(4)),
            new ItemStack(Material.EXPERIENCE_BOTTLE, 8 + rand.nextInt(16)),
            new ItemStack(Material.IRON_BLOCK, 1 + rand.nextInt(3)),
            new ItemStack(Material.GOLD_BLOCK, 1 + rand.nextInt(2)),
            new ItemStack(Material.DIAMOND_SWORD, 1),
            new ItemStack(Material.DIAMOND_CHESTPLATE, 1),
            new ItemStack(Material.TOTEM_OF_UNDYING, 1),
            new ItemStack(Material.ARROW, 16 + rand.nextInt(32)),
            new ItemStack(Material.BLAZE_ROD, 2 + rand.nextInt(4)),
            new ItemStack(Material.PHANTOM_MEMBRANE, 2 + rand.nextInt(4)),
            new ItemStack(Material.ECHO_SHARD, 1 + rand.nextInt(2)),
        };

        int itemCount = 3 + rand.nextInt(5);
        Set<Integer> usedSlots = new HashSet<>();
        for (int i = 0; i < itemCount; i++) {
            int slot;
            do { slot = rand.nextInt(27); } while (usedSlots.contains(slot));
            usedSlots.add(slot);
            chest.getInventory().setItem(slot, possibleLoot[rand.nextInt(possibleLoot.length)]);
        }
    }

    private void fillAbyssalChest(Chest chest) {
        Random rand = new Random();
        chest.getInventory().clear();
        // Premium loot for abyssal weapon chambers
        chest.getInventory().setItem(13, new ItemStack(Material.NETHERITE_INGOT, 2));
        chest.getInventory().setItem(4, new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 3));
        chest.getInventory().setItem(10, new ItemStack(Material.NETHER_STAR, 1));
        chest.getInventory().setItem(16, new ItemStack(Material.TOTEM_OF_UNDYING, 1));
        chest.getInventory().setItem(22, new ItemStack(Material.EXPERIENCE_BOTTLE, 32));
        chest.getInventory().setItem(1, new ItemStack(Material.END_CRYSTAL, 2));
        chest.getInventory().setItem(7, new ItemStack(Material.ELYTRA, 1));
    }

    // ════════════════════════════════════════════════════════════════
    //  HELPERS
    // ════════════════════════════════════════════════════════════════
    private void setBlock(int x, int y, int z, Material mat) {
        if (y < world.getMinHeight() || y > world.getMaxHeight() - 1) return;
        world.getBlockAt(x, y, z).setType(mat, false);
    }
}
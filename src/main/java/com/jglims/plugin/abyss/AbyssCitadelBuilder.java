package com.jglims.plugin.abyss;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.legendary.LegendaryTier;
import com.jglims.plugin.legendary.LegendaryWeapon;
import com.jglims.plugin.legendary.LegendaryWeaponManager;
import com.jglims.plugin.powerups.PowerUpManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * AbyssCitadelBuilder v4.0 — Massive Gothic Abyssal Cathedral
 * Faithful recreation of the Blockwave Studios "Abyssal Citadel" 3D model.
 *
 * Architecture: A towering gothic cathedral with a central mega-spire (180+ blocks),
 * flanked by 10+ secondary spires of varying heights, a grand pointed-arch facade,
 * flying buttresses, gothic arched windows with purple glass, a circular front
 * courtyard with glowing purple path lines, 4 Abyssal weapon guardian chambers,
 * an external rear arena, floating amethyst crystals, and organic rocky terrain.
 *
 * NOT a square fortress — a vertical, cathedral-shaped masterpiece.
 */
public class AbyssCitadelBuilder {

    private final JGlimsPlugin plugin;
    private final World world;
    private final Random rng = new Random(42);

    // ─── Block Palette ────────────────────────────────────────
    private static final Material DS = Material.DEEPSLATE_BRICKS;      // primary dark wall
    private static final Material DT = Material.DEEPSLATE_TILES;       // secondary wall
    private static final Material PD = Material.POLISHED_DEEPSLATE;    // accents/pillars
    private static final Material CD = Material.CHISELED_DEEPSLATE;    // detail trim
    private static final Material NB = Material.NETHER_BRICKS;         // dark structural
    private static final Material RNB = Material.RED_NETHER_BRICKS;    // dark accent
    private static final Material BL = Material.BLACKSTONE;            // foundation
    private static final Material PBL = Material.POLISHED_BLACKSTONE;  // floor accent
    private static final Material PBB = Material.POLISHED_BLACKSTONE_BRICKS; // floor
    private static final Material OBS = Material.OBSIDIAN;             // structural dark
    private static final Material CRY = Material.CRYING_OBSIDIAN;      // glow accent
    private static final Material AME = Material.AMETHYST_BLOCK;       // purple accent
    private static final Material AMC = Material.AMETHYST_CLUSTER;     // crystal detail
    private static final Material PGP = Material.PURPLE_STAINED_GLASS_PANE; // windows (replaces nether portal)
    private static final Material PG = Material.PURPLE_STAINED_GLASS;  // window fill
    private static final Material MG = Material.MAGENTA_STAINED_GLASS_PANE; // window accent
    private static final Material IB = Material.IRON_BARS;             // railings
    private static final Material SL = Material.SOUL_LANTERN;          // lighting
    private static final Material SF = Material.SOUL_CAMPFIRE;         // fire lighting
    private static final Material ER = Material.END_ROD;               // spire tips & lighting
    private static final Material ES = Material.END_STONE_BRICKS;      // arena pillars
    private static final Material BK = Material.BEDROCK;               // arena floor
    private static final Material BA = Material.BARRIER;               // arena walls
    private static final Material STR = Material.DEEPSLATE_BRICK_STAIRS; // stairs
    private static final Material PIL = Material.POLISHED_DEEPSLATE;   // pillars
    private static final Material AIR = Material.AIR;
    private static final Material PUR = Material.PURPUR_BLOCK;         // purple structural

    // ─── Key dimensions ──────────────────────────────────────
    // The cathedral is oriented facing SOUTH (+Z).
    // Center is at (0, sY, 0). Front entrance faces +Z.
    private static final int CENTER_SPIRE_H = 180;   // tallest spire height
    private static final int NAVE_LENGTH = 80;        // front-to-back length of main hall
    private static final int NAVE_WIDTH = 30;         // half-width of the nave
    private static final int NAVE_HEIGHT = 50;        // interior ceiling height
    private static final int FACADE_WIDTH = 60;       // half-width of the front facade
    private static final int FACADE_HEIGHT = 80;      // front facade peak
    private static final int ARENA_RADIUS = 40;       // boss arena radius
    private static final int ARENA_DIST = 120;        // arena center distance behind cathedral

    private int sY; // surface Y level

    public AbyssCitadelBuilder(JGlimsPlugin plugin, World world) {
        this.plugin = plugin;
        this.world = world;
    }

    public void build() {
        long start = System.currentTimeMillis();
        sY = findSurface();
        plugin.getLogger().info("[Citadel] Building Gothic Abyssal Cathedral at Y=" + sY + "...");

        // Phase 1: Foundation & Ground
        buildFoundationPlatform();
        buildCircularCourtyard();

        // Phase 2: Main Cathedral Body
        buildNave();
        buildTransept();
        buildApse();
        buildFrontFacade();

        // Phase 3: Spires (the iconic skyline)
        buildCenterSpire();
        buildFlankerSpires();
        buildMinorSpires();

        // Phase 4: Gothic Details
        buildFlyingButtresses();
        buildGothicWindows();
        buildRoofLine();

        // Phase 5: Interior
        buildInteriorNave();
        buildInteriorChapels();
        buildAbyssalWeaponChambers();
        buildStairsToArena();

        // Phase 6: Arena (behind the cathedral)
        buildArena();
        buildArenaDecorations();
        buildArenaBarriers();
        buildArenaPath();

        // Phase 7: Environment
        buildRockyTerrain();
        buildFloatingCrystals();
        buildGlowingCourtLines();
        buildApproachPath();

        // Phase 8: Populate (delayed)
        new BukkitRunnable() {
            @Override public void run() {
                populateChests();
                spawnGuards();
                plugin.getLogger().info("[Citadel] Loot and guards placed.");
            }
        }.runTaskLater(plugin, 60L);

        long elapsed = System.currentTimeMillis() - start;
        plugin.getLogger().info("[Citadel] Structure complete in " + elapsed + "ms");
    }

    // ═══════════════════════════════════════════════════════════
    //  PHASE 1: FOUNDATION PLATFORM
    // ═══════════════════════════════════════════════════════════
    private void buildFoundationPlatform() {
        plugin.getLogger().info("[Citadel] Laying foundation platform...");
        // Elliptical platform — wider on Z (front-back) than X
        int radiusX = 110, radiusZ = 140;
        for (int x = -radiusX; x <= radiusX; x++) {
            for (int z = -radiusZ; z <= radiusZ; z++) {
                double norm = (double)(x*x)/(radiusX*radiusX) + (double)(z*z)/(radiusZ*radiusZ);
                if (norm > 1.0) continue;
                int ty = findTerrainY(x, z);
                double edgeFade = 1.0 - norm;
                int depth = (int)(8 * edgeFade) + 2;
                for (int y = sY - depth; y <= sY; y++) {
                    Material m;
                    if (y == sY) m = edgeFade > 0.5 ? PBB : (edgeFade > 0.3 ? BL : DT);
                    else if (y >= sY - 2) m = BL;
                    else m = DT;
                    s(x, y, z, m);
                }
                // Fill from terrain up
                for (int y = ty; y < sY - depth; y++) s(x, y, z, BL);
                // Clear above
                for (int y = sY + 1; y <= sY + CENTER_SPIRE_H + 20; y++) {
                    Block b = world.getBlockAt(x, y, z);
                    if (b.getType().isSolid() && b.getType() != BK) b.setType(AIR);
                }
            }
        }
    }

    private void buildCircularCourtyard() {
        plugin.getLogger().info("[Citadel] Building circular courtyard...");
        // Circular courtyard in front of the cathedral (centered at z=60)
        int courtZ = 65, courtR = 40;
        for (int x = -courtR; x <= courtR; x++) {
            for (int z = courtZ - courtR; z <= courtZ + courtR; z++) {
                double dist = Math.sqrt(x * x + (z - courtZ) * (z - courtZ));
                if (dist > courtR) continue;
                Material m;
                if (dist < 5) m = CRY;
                else if ((int)dist % 8 == 0) m = CRY;
                else if ((x + z) % 3 == 0) m = PBL;
                else m = PBB;
                s(x, sY, z, m);
                for (int dy = 1; dy <= 3; dy++) s(x, sY + dy, z, AIR);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  PHASE 2: MAIN CATHEDRAL BODY — NAVE
    // ═══════════════════════════════════════════════════════════
    private void buildNave() {
        plugin.getLogger().info("[Citadel] Building nave...");
        // The nave runs from z=40 (front) to z=-40 (back), centered on x=0
        // It has thick walls that taper inward as they go up (gothic shape)
        int frontZ = 40, backZ = -40;
        int wallThick = 4;

        for (int z = backZ; z <= frontZ; z++) {
            for (int y = sY; y <= sY + NAVE_HEIGHT; y++) {
                double heightRatio = (double)(y - sY) / NAVE_HEIGHT;
                // Gothic narrowing: walls get closer together as they go up
                int halfW = (int)(NAVE_WIDTH * (1.0 - heightRatio * 0.3));
                // Left wall
                for (int t = 0; t < wallThick; t++) {
                    Material m = wallMat(y, t);
                    s(-halfW - t, y, z, m);
                    s(halfW + t, y, z, m);
                }
            }
        }
        // Floor
        for (int x = -NAVE_WIDTH; x <= NAVE_WIDTH; x++) {
            for (int z = backZ; z <= frontZ; z++) {
                s(x, sY, z, (x + z) % 2 == 0 ? PBB : PBL);
            }
        }
    }

    private void buildTransept() {
        plugin.getLogger().info("[Citadel] Building transept...");
        // Cross-shaped transept at z=0, extending to x=±50
        int transW = 50, transD = 12, transH = 45;
        int wallThick = 3;
        for (int x = -transW; x <= transW; x++) {
            for (int z = -transD; z <= transD; z++) {
                for (int y = sY; y <= sY + transH; y++) {
                    double heightRatio = (double)(y - sY) / transH;
                    int halfD = (int)(transD * (1.0 - heightRatio * 0.25));
                    boolean isEdge = Math.abs(z) >= halfD - wallThick && Math.abs(z) <= halfD;
                    boolean isXEdge = Math.abs(x) >= transW - wallThick && Math.abs(x) <= transW;
                    if (isEdge || isXEdge) {
                        s(x, y, z, wallMat(y, 0));
                    } else if (y == sY) {
                        s(x, y, z, (x + z) % 2 == 0 ? PBB : PBL);
                    }
                }
            }
        }
    }

    private void buildApse() {
        plugin.getLogger().info("[Citadel] Building apse...");
        // Semicircular apse at the back (z = -40 to z = -65)
        int apseCZ = -40, apseR = 25, apseH = 55;
        for (int x = -apseR; x <= apseR; x++) {
            for (int z = apseCZ; z >= apseCZ - apseR; z--) {
                double dist = Math.sqrt(x * x + (z - apseCZ) * (z - apseCZ));
                if (dist > apseR) continue;
                for (int y = sY; y <= sY + apseH; y++) {
                    double heightRatio = (double)(y - sY) / apseH;
                    double rAtH = apseR * (1.0 - heightRatio * 0.35);
                    if (dist >= rAtH - 3 && dist <= rAtH) {
                        s(x, y, z, wallMat(y, 0));
                    } else if (y == sY && dist < rAtH - 3) {
                        s(x, y, z, PBB);
                    }
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  PHASE 2: FRONT FACADE
    // ═══════════════════════════════════════════════════════════
    private void buildFrontFacade() {
        plugin.getLogger().info("[Citadel] Building front facade...");
        int fz = 40; // front Z

        // The facade is a massive pointed shape, widest at bottom, narrowing to a peak
        for (int y = sY; y <= sY + FACADE_HEIGHT; y++) {
            double heightRatio = (double)(y - sY) / FACADE_HEIGHT;
            // Width narrows as we go up — gothic pointed arch shape
            int halfW = (int)(FACADE_WIDTH * (1.0 - heightRatio * heightRatio * 0.6));
            for (int x = -halfW; x <= halfW; x++) {
                // Outer surface
                for (int dz = 0; dz < 5; dz++) {
                    Material m;
                    if (dz == 0) m = CD; // front face trim
                    else if (heightRatio > 0.8) m = NB;
                    else m = wallMat(y, dz);
                    s(x, y, fz + dz, m);
                }
            }
        }

        // Grand entrance arch (pointed gothic arch)
        int archW = 8, archH = 25;
        for (int x = -archW; x <= archW; x++) {
            // Pointed arch profile: height depends on distance from center
            double xRatio = (double)Math.abs(x) / archW;
            int localH = (int)(archH * (1.0 - xRatio * xRatio));
            for (int y = sY + 1; y <= sY + localH; y++) {
                for (int dz = 0; dz < 5; dz++) {
                    s(x, y, fz + dz, AIR);
                }
            }
        }
        // Arch frame in obsidian
        for (int x = -archW - 1; x <= archW + 1; x++) {
            double xRatio = (double)Math.abs(x) / (archW + 1);
            int localH = (int)(archH * (1.0 - xRatio * xRatio));
            s(x, sY + localH, fz, OBS);
            s(x, sY + localH, fz + 1, OBS);
        }
        // Entrance pillars
        for (int side = -1; side <= 1; side += 2) {
            int px = side * (archW + 2);
            for (int y = sY; y <= sY + archH + 5; y++) {
                s(px, y, fz, OBS);
                s(px, y, fz + 1, OBS);
                s(px, y, fz - 1, CD);
            }
            s(px, sY + archH + 6, fz, SF);
        }

        // Secondary arched windows flanking entrance
        for (int side = -1; side <= 1; side += 2) {
            for (int i = 1; i <= 3; i++) {
                int wx = side * (archW + 6 + i * 10);
                if (Math.abs(wx) > FACADE_WIDTH - 5) continue;
                buildGothicWindowOnWall(wx, sY + 5, fz, 3, 12, true);
            }
        }

        // Rose window (circular) above entrance
        int roseY = sY + archH + 8, roseR = 7;
        for (int dx = -roseR; dx <= roseR; dx++) {
            for (int dy = -roseR; dy <= roseR; dy++) {
                double d = Math.sqrt(dx * dx + dy * dy);
                if (d <= roseR && d >= roseR - 1) {
                    s(dx, roseY + dy, fz, AME);
                } else if (d < roseR - 1) {
                    s(dx, roseY + dy, fz, PGP);
                    s(dx, roseY + dy, fz + 1, AIR);
                }
            }
        }
        s(0, roseY, fz - 1, ER); // center rod

        // Diamond/rhombus decorative element above rose window
        int diaY = roseY + roseR + 3, diaSize = 5;
        for (int dy = 0; dy <= diaSize; dy++) {
            int w = dy <= diaSize/2 ? dy : diaSize - dy;
            for (int dx = -w; dx <= w; dx++) {
                s(dx, diaY + dy, fz, AME);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  PHASE 3: SPIRES
    // ═══════════════════════════════════════════════════════════
    private void buildCenterSpire() {
        plugin.getLogger().info("[Citadel] Building center mega-spire...");
        // The central spire rises from the crossing (z=0) to 180 blocks
        buildSpire(0, sY, -10, 14, CENTER_SPIRE_H, true);
    }

    private void buildFlankerSpires() {
        plugin.getLogger().info("[Citadel] Building flanker spires...");
        // 4 tall flanker spires at the transept ends and facade corners
        buildSpire(-45, sY, 0, 10, 120, true);   // left transept
        buildSpire(45, sY, 0, 10, 120, true);    // right transept
        buildSpire(-40, sY, 38, 10, 100, true);  // left facade
        buildSpire(40, sY, 38, 10, 100, true);   // right facade
        // 2 tall spires behind apse
        buildSpire(-15, sY, -55, 8, 110, true);
        buildSpire(15, sY, -55, 8, 110, true);
    }

    private void buildMinorSpires() {
        plugin.getLogger().info("[Citadel] Building minor spires...");
        // Many smaller spires along the roofline and facade
        int[][] minorPos = {
            {-25, 30, 80}, {25, 30, 80},     // facade mid
            {-55, 0, 70}, {55, 0, 70},       // facade outer
            {-20, -30, 65}, {20, -30, 65},   // apse flankers
            {-35, 20, 55}, {35, 20, 55},     // nave mid
            {-50, 10, 50}, {50, 10, 50},     // transept mid
            {0, 35, 90},                      // center front
            {-30, -20, 60}, {30, -20, 60},   // back nave
            {-10, 25, 70}, {10, 25, 70},     // inner front
        };
        for (int[] pos : minorPos) {
            buildSpire(pos[0], sY, pos[1], 6 + rng.nextInt(3), pos[2], false);
        }
    }

    private void buildSpire(int cx, int baseY, int cz, int baseRadius, int height, boolean major) {
        for (int y = 0; y < height; y++) {
            double progress = (double) y / height;
            // Octagonal cross-section that narrows
            double radius = baseRadius * (1.0 - progress);
            if (radius < 0.5 && y > 10) break;
            int r = Math.max(0, (int) radius);

            Material m;
            if (progress > 0.85) m = NB;
            else if (y % 8 == 0) m = CD;
            else if (y % 5 == 0) m = PD;
            else m = DS;

            if (r == 0) {
                s(cx, baseY + y, cz, m);
            } else {
                // Build octagonal shell
                for (int dx = -r; dx <= r; dx++) {
                    for (int dz = -r; dz <= r; dz++) {
                        double d = Math.sqrt(dx * dx + dz * dz);
                        if (d <= r && d >= r - 2) {
                            s(cx + dx, baseY + y, cz + dz, m);
                        }
                        // Interior floor every 12 blocks
                        if (d < r - 2 && y % 12 == 0 && y > 0 && y < height - 15) {
                            s(cx + dx, baseY + y, cz + dz, PBB);
                        } else if (d < r - 2 && y % 12 != 0) {
                            s(cx + dx, baseY + y, cz + dz, AIR);
                        }
                    }
                }
            }

            // Windows on major spires
            if (major && r > 3 && y % 12 >= 3 && y % 12 <= 7 && y < height - 20) {
                // 4 cardinal windows
                for (int w = -1; w <= 1; w++) {
                    s(cx + w, baseY + y, cz + r, PGP);
                    s(cx + w, baseY + y, cz - r, PGP);
                    s(cx + r, baseY + y, cz + w, PGP);
                    s(cx - r, baseY + y, cz + w, PGP);
                }
            }

            // Ledges every 15 blocks
            if (y > 0 && y % 15 == 0 && r > 2) {
                for (int dx = -r - 1; dx <= r + 1; dx++) {
                    for (int dz = -r - 1; dz <= r + 1; dz++) {
                        double d = Math.sqrt(dx * dx + dz * dz);
                        if (d <= r + 1 && d >= r) {
                            s(cx + dx, baseY + y, cz + dz, CD);
                        }
                    }
                }
            }
        }

        // Pointed tip
        for (int dy = 0; dy < 6; dy++) {
            s(cx, baseY + height + dy, cz, dy < 4 ? NB : ER);
        }
        // Soul fire at base of major spires
        if (major) {
            s(cx + baseRadius + 1, baseY + 1, cz, SF);
            s(cx - baseRadius - 1, baseY + 1, cz, SF);
            s(cx, baseY + 1, cz + baseRadius + 1, SF);
            s(cx, baseY + 1, cz - baseRadius - 1, SF);
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  PHASE 4: GOTHIC DETAILS
    // ═══════════════════════════════════════════════════════════
    private void buildFlyingButtresses() {
        plugin.getLogger().info("[Citadel] Building flying buttresses...");
        // Arched supports from nave walls outward
        for (int z = -35; z <= 35; z += 10) {
            for (int side = -1; side <= 1; side += 2) {
                int startX = side * NAVE_WIDTH;
                int endX = side * (NAVE_WIDTH + 18);
                buildButtress(startX, sY, z, endX, sY + 30, z);
            }
        }
    }

    private void buildButtress(int x1, int y1, int z, int x2, int y2, int z2) {
        int dx = x2 > x1 ? 1 : -1;
        int steps = Math.abs(x2 - x1);
        for (int i = 0; i <= steps; i++) {
            double progress = (double) i / steps;
            // Arch shape: rises then comes back down
            int archY = (int)(Math.sin(progress * Math.PI) * 15);
            int baseY = y1 + (int)((y2 - y1) * (1.0 - progress));
            int y = baseY + archY;
            int x = x1 + dx * i;
            s(x, y, z, DS);
            s(x, y - 1, z, DS);
            // Vertical support pillar at the end
            if (i == steps) {
                for (int dy = 0; dy <= y - sY; dy++) s(x, sY + dy, z, DS);
            }
        }
    }

    private void buildGothicWindows() {
        plugin.getLogger().info("[Citadel] Building gothic windows...");
        // Tall pointed-arch windows along the nave walls
        for (int z = -30; z <= 30; z += 10) {
            // Left wall
            buildGothicWindowOnWall(-NAVE_WIDTH, sY + 5, z, 3, 15, false);
            // Right wall
            buildGothicWindowOnWall(NAVE_WIDTH, sY + 5, z, 3, 15, false);
        }
        // Transept windows
        for (int x = -40; x <= 40; x += 12) {
            if (Math.abs(x) < 15) continue;
            buildGothicWindowOnWall(x, sY + 5, -12, 2, 12, true);
            buildGothicWindowOnWall(x, sY + 5, 12, 2, 12, true);
        }
        // Apse windows (radial)
        for (int a = 0; a < 180; a += 30) {
            double rad = Math.toRadians(a);
            int wx = (int)(22 * Math.cos(rad));
            int wz = -40 + (int)(22 * -Math.sin(rad));
            buildGothicWindowOnWall(wx, sY + 8, wz, 2, 14, Math.abs(wx) > Math.abs(wz + 40));
        }
    }

    private void buildGothicWindowOnWall(int wx, int wy, int wz, int halfW, int height, boolean xFacing) {
        // Pointed arch window filled with purple glass panes
        for (int dy = 0; dy < height; dy++) {
            double hRatio = (double) dy / height;
            int localW = (int)(halfW * (1.0 - hRatio * hRatio));
            for (int d = -localW; d <= localW; d++) {
                if (xFacing) {
                    s(wx + d, wy + dy, wz, PGP);
                } else {
                    s(wx, wy + dy, wz + d, PGP);
                }
            }
        }
        // Frame
        for (int dy = 0; dy < height; dy++) {
            double hRatio = (double) dy / height;
            int localW = (int)(halfW * (1.0 - hRatio * hRatio));
            if (xFacing) {
                s(wx - localW - 1, wy + dy, wz, OBS);
                s(wx + localW + 1, wy + dy, wz, OBS);
            } else {
                s(wx, wy + dy, wz - localW - 1, OBS);
                s(wx, wy + dy, wz + localW + 1, OBS);
            }
        }
    }

    private void buildRoofLine() {
        plugin.getLogger().info("[Citadel] Building roof...");
        // Pointed roof along the nave
        for (int z = -40; z <= 40; z++) {
            for (int dy = 0; dy <= 15; dy++) {
                int halfW = NAVE_WIDTH - dy;
                if (halfW < 0) break;
                s(-halfW, sY + NAVE_HEIGHT + dy, z, DT);
                s(halfW, sY + NAVE_HEIGHT + dy, z, DT);
                if (dy == 15 || halfW == 0) {
                    s(0, sY + NAVE_HEIGHT + dy, z, CD);
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  PHASE 5: INTERIOR
    // ═══════════════════════════════════════════════════════════
    private void buildInteriorNave() {
        plugin.getLogger().info("[Citadel] Building interior nave...");
        // Clear interior and add pillars
        for (int x = -NAVE_WIDTH + 5; x <= NAVE_WIDTH - 5; x++) {
            for (int z = -35; z <= 35; z++) {
                for (int y = sY + 1; y <= sY + NAVE_HEIGHT - 2; y++) {
                    Block b = world.getBlockAt(x, y, z);
                    if (b.getType() != PGP && b.getType() != OBS && b.getType() != AME) {
                        s(x, y, z, AIR);
                    }
                }
            }
        }
        // Two rows of pillars along the nave
        for (int z = -30; z <= 30; z += 8) {
            for (int side = -1; side <= 1; side += 2) {
                int px = side * (NAVE_WIDTH - 8);
                for (int y = sY; y <= sY + NAVE_HEIGHT - 5; y++) {
                    s(px, y, z, PIL);
                    s(px + side, y, z, y % 6 == 0 ? CD : DS);
                }
                // Arch connecting pillar to ceiling
                for (int dy = 0; dy <= 5; dy++) {
                    int dx = side * dy;
                    s(px + dx, sY + NAVE_HEIGHT - 5 + dy, z, DS);
                }
                s(px, sY + NAVE_HEIGHT - 6, z, SL);
            }
        }
    }

    private void buildInteriorChapels() {
        plugin.getLogger().info("[Citadel] Building interior chapels...");
        // Side chapels along the transept arms
        for (int side = -1; side <= 1; side += 2) {
            for (int i = 0; i < 3; i++) {
                int cx = side * (20 + i * 10);
                int cz = 0;
                // Small room
                int w = 5, d = 5, h = 8;
                for (int y = sY + 1; y <= sY + h; y++) {
                    for (int x = cx - w; x <= cx + w; x++) {
                        s(x, y, cz - d, DS);
                        s(x, y, cz + d, DS);
                    }
                }
                // Floor
                for (int x = cx - w + 1; x < cx + w; x++) {
                    for (int z = cz - d + 1; z < cz + d; z++) {
                        s(x, sY, z, CRY);
                    }
                }
                // Chest
                s(cx, sY + 1, cz, Material.CHEST);
                s(cx, sY + h - 1, cz, SL);
            }
        }
    }

    private void buildAbyssalWeaponChambers() {
        plugin.getLogger().info("[Citadel] Building 4 Abyssal Weapon chambers...");
        // 4 chambers in the corners of the transept/nave intersection
        int[][] chamberPos = {
            {-NAVE_WIDTH - 10, -25},  // NW
            {NAVE_WIDTH + 10, -25},   // NE
            {-NAVE_WIDTH - 10, 25},   // SW
            {NAVE_WIDTH + 10, 25},    // SE
        };
        for (int i = 0; i < 4; i++) {
            buildWeaponChamber(chamberPos[i][0], sY, chamberPos[i][1], i);
        }
    }

    private void buildWeaponChamber(int cx, int baseY, int cz, int index) {
        int w = 10, d = 10, h = 12;
        // Obsidian chamber
        for (int y = baseY; y <= baseY + h; y++) {
            Material m = y % 4 == 0 ? OBS : DS;
            for (int x = cx - w; x <= cx + w; x++) { s(x, y, cz - d, m); s(x, y, cz + d, m); }
            for (int z = cz - d; z <= cz + d; z++) { s(cx - w, y, z, m); s(cx + w, y, z, m); }
        }
        // Interior
        for (int x = cx - w + 1; x < cx + w; x++) {
            for (int z = cz - d + 1; z < cz + d; z++) {
                s(x, baseY, z, (x + z) % 3 == 0 ? CRY : OBS);
                for (int y = baseY + 1; y < baseY + h; y++) s(x, y, z, AIR);
            }
        }
        // Ceiling
        for (int x = cx - w; x <= cx + w; x++) for (int z = cz - d; z <= cz + d; z++) s(x, baseY + h, z, OBS);
        // Pedestal with weapon chest
        for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) s(cx + dx, baseY, cz + dz, AME);
        s(cx, baseY + 1, cz, AME);
        s(cx, baseY + 2, cz, Material.CHEST);
        s(cx, baseY + 4, cz, ER);
        // Purple glass windows
        for (int dy = 3; dy <= 8; dy++) {
            s(cx, dy + baseY, cz - d, PGP); s(cx, dy + baseY, cz + d, PGP);
            s(cx - w, dy + baseY, cz, PGP); s(cx + w, dy + baseY, cz, PGP);
        }
        // Soul fire corners
        s(cx - w + 2, baseY + 1, cz - d + 2, SF);
        s(cx + w - 2, baseY + 1, cz - d + 2, SF);
        s(cx - w + 2, baseY + 1, cz + d - 2, SF);
        s(cx + w - 2, baseY + 1, cz + d - 2, SF);
        // Lanterns
        s(cx - 4, baseY + h - 1, cz, SL); s(cx + 4, baseY + h - 1, cz, SL);
        // Doorway
        for (int dy = 1; dy <= 4; dy++) for (int dz = -1; dz <= 1; dz++) {
            s(cx - w, baseY + dy, cz + dz, AIR);
            s(cx + w, baseY + dy, cz + dz, AIR);
        }
    }

    private void buildStairsToArena() {
        plugin.getLogger().info("[Citadel] Building path to arena...");
        // Covered walkway from apse (z=-65) to arena (z=-ARENA_DIST)
        int startZ = -65, endZ = -(ARENA_DIST - ARENA_RADIUS - 5);
        for (int z = startZ; z >= endZ; z--) {
            for (int x = -4; x <= 4; x++) {
                s(x, sY, z, PBB);
                s(x, sY + 6, z, DT);
                if (Math.abs(x) == 4) {
                    for (int dy = 1; dy <= 5; dy++) s(x, sY + dy, z, DS);
                } else {
                    for (int dy = 1; dy <= 5; dy++) s(x, sY + dy, z, AIR);
                }
            }
            if (z % 8 == 0) {
                s(-5, sY + 4, z, SL);
                s(5, sY + 4, z, SL);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  PHASE 6: ARENA (behind the cathedral)
    // ═══════════════════════════════════════════════════════════
    private void buildArena() {
        plugin.getLogger().info("[Citadel] Building arena...");
        int acx = 0, acz = -ARENA_DIST;
        // Circular arena at surface level (not underground!)
        for (int x = -ARENA_RADIUS; x <= ARENA_RADIUS; x++) {
            for (int z = -ARENA_RADIUS; z <= ARENA_RADIUS; z++) {
                if (x * x + z * z > ARENA_RADIUS * ARENA_RADIUS) continue;
                s(acx + x, sY, acz + z, BK);
                for (int dy = 1; dy <= 5; dy++) s(acx + x, sY + dy, acz + z, AIR);
            }
        }
        // Circular wall (8 blocks high)
        for (int y = sY; y <= sY + 8; y++) {
            for (int a = 0; a < 360; a++) {
                double rad = Math.toRadians(a);
                int wx = acx + (int) Math.round(ARENA_RADIUS * Math.cos(rad));
                int wz = acz + (int) Math.round(ARENA_RADIUS * Math.sin(rad));
                s(wx, y, wz, y <= sY + 2 ? OBS : DS);
            }
        }
        // Central altar
        for (int dx = -3; dx <= 3; dx++) for (int dz = -3; dz <= 3; dz++) {
            if (dx * dx + dz * dz <= 9) s(acx + dx, sY + 1, acz + dz, OBS);
        }
        s(acx, sY + 2, acz, Material.LODESTONE);
        s(acx, sY + 3, acz, AME);
        s(acx, sY + 4, acz, ER);
    }

    private void buildArenaDecorations() {
        int acx = 0, acz = -ARENA_DIST;
        // Soul campfire ring
        for (int a = 0; a < 360; a += 15) {
            double rad = Math.toRadians(a);
            int fx = acx + (int) Math.round((ARENA_RADIUS - 3) * Math.cos(rad));
            int fz = acz + (int) Math.round((ARENA_RADIUS - 3) * Math.sin(rad));
            s(fx, sY + 1, fz, SF);
        }
        // Obsidian pillars
        for (int a = 0; a < 360; a += 30) {
            double rad = Math.toRadians(a);
            int px = acx + (int) Math.round((ARENA_RADIUS - 2) * Math.cos(rad));
            int pz = acz + (int) Math.round((ARENA_RADIUS - 2) * Math.sin(rad));
            int ph = 5 + rng.nextInt(6);
            for (int dy = 1; dy <= ph; dy++) s(px, sY + dy, pz, ES);
            s(px, sY + ph + 1, pz, Material.POINTED_DRIPSTONE);
        }
        // Crying obsidian floor accents
        for (int x = -ARENA_RADIUS + 5; x <= ARENA_RADIUS - 5; x += 7) {
            for (int z = -ARENA_RADIUS + 5; z <= ARENA_RADIUS - 5; z += 7) {
                if (x * x + z * z < (ARENA_RADIUS - 5) * (ARENA_RADIUS - 5))
                    s(acx + x, sY, acz + z, CRY);
            }
        }
    }

    private void buildArenaBarriers() {
        plugin.getLogger().info("[Citadel] Building arena barriers...");
        int acx = 0, acz = -ARENA_DIST;
        // Barrier dome
        for (int y = sY + 9; y <= sY + 50; y++) {
            for (int a = 0; a < 360; a++) {
                double rad = Math.toRadians(a);
                s(acx + (int) Math.round((ARENA_RADIUS + 1) * Math.cos(rad)), y,
                  acz + (int) Math.round((ARENA_RADIUS + 1) * Math.sin(rad)), BA);
            }
        }
        // Ceiling
        for (int x = -ARENA_RADIUS - 1; x <= ARENA_RADIUS + 1; x++) {
            for (int z = -ARENA_RADIUS - 1; z <= ARENA_RADIUS + 1; z++) {
                if (x * x + z * z <= (ARENA_RADIUS + 1) * (ARENA_RADIUS + 1))
                    s(acx + x, sY + 50, acz + z, BA);
            }
        }
    }

    private void buildArenaPath() {
        // Opening in arena wall facing cathedral
        int acx = 0, acz = -ARENA_DIST;
        for (int x = -4; x <= 4; x++) {
            for (int dy = 1; dy <= 5; dy++) {
                s(acx + x, sY + dy, acz + ARENA_RADIUS, AIR);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  PHASE 7: ENVIRONMENT
    // ═══════════════════════════════════════════════════════════
    private void buildRockyTerrain() {
        plugin.getLogger().info("[Citadel] Building rocky terrain...");
        // Jagged rock spires around the cathedral (like the 3D model)
        // These are organic, pointed rock formations
        for (int i = 0; i < 60; i++) {
            double angle = rng.nextDouble() * Math.PI * 2;
            double dist = 70 + rng.nextDouble() * 50;
            int rx = (int)(dist * Math.cos(angle));
            int rz = (int)(dist * Math.sin(angle));
            // Skip if too close to arena
            if (Math.sqrt(rx * rx + (rz + ARENA_DIST) * (rz + ARENA_DIST)) < ARENA_RADIUS + 10) continue;

            int height = 15 + rng.nextInt(35);
            int baseR = 3 + rng.nextInt(5);
            Material rockMat = rng.nextDouble() < 0.3 ? AME : (rng.nextDouble() < 0.5 ? DT : BL);

            for (int y = 0; y < height; y++) {
                double r = baseR * (1.0 - (double) y / height);
                int ir = (int) r;
                for (int dx = -ir; dx <= ir; dx++) {
                    for (int dz = -ir; dz <= ir; dz++) {
                        if (dx * dx + dz * dz <= ir * ir) {
                            s(rx + dx, sY + y, rz + dz, rockMat);
                        }
                    }
                }
            }
        }

        // Large sweeping purple wave formations (the tentacle-like shapes)
        for (int wave = 0; wave < 4; wave++) {
            double startAngle = wave * Math.PI / 2 + 0.3;
            int startR = 80 + rng.nextInt(20);
            for (int t = 0; t < 40; t++) {
                double a = startAngle + t * 0.04;
                double r = startR + t * 1.5;
                int wx = (int)(r * Math.cos(a));
                int wz = (int)(r * Math.sin(a));
                int wy = sY + 20 + (int)(30 * Math.sin(t * 0.15));
                int wr = 4 + (int)(3 * Math.sin(t * 0.2));
                for (int dx = -wr; dx <= wr; dx++) {
                    for (int dz = -wr; dz <= wr; dz++) {
                        for (int dy = -wr; dy <= wr; dy++) {
                            if (dx * dx + dz * dz + dy * dy <= wr * wr) {
                                s(wx + dx, wy + dy, wz + dz, Material.PURPLE_WOOL);
                            }
                        }
                    }
                }
            }
        }
    }

    private void buildFloatingCrystals() {
        plugin.getLogger().info("[Citadel] Building floating crystals...");
        // Large floating amethyst crystals in the sky
        int[][] crystalPos = {
            {-30, 100, -20, 8}, {30, 110, 10, 7}, {-50, 90, 30, 6},
            {50, 105, -30, 7}, {0, 120, 50, 9}, {-60, 95, -40, 5},
            {40, 115, 40, 6}, {-20, 130, 0, 8},
        };
        for (int[] c : crystalPos) {
            buildCrystal(c[0], sY + c[1], c[2], c[3]);
        }
    }

    private void buildCrystal(int cx, int cy, int cz, int size) {
        // Diamond/elongated crystal shape
        for (int dy = -size; dy <= size; dy++) {
            double distFromCenter = (double) Math.abs(dy) / size;
            int r = (int)(size * 0.5 * (1.0 - distFromCenter));
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (dx * dx + dz * dz <= r * r) {
                        Material m = Math.abs(dy) > size * 0.7 ? AMC : AME;
                        s(cx + dx, cy + dy, cz + dz, m);
                    }
                }
            }
        }
    }

    private void buildGlowingCourtLines() {
        plugin.getLogger().info("[Citadel] Building glowing courtyard lines...");
        // Purple path lines radiating from the courtyard center
        int courtZ = 65;
        for (int a = 0; a < 360; a += 30) {
            double rad = Math.toRadians(a);
            for (int d = 5; d < 38; d++) {
                int lx = (int)(d * Math.cos(rad));
                int lz = courtZ + (int)(d * Math.sin(rad));
                s(lx, sY, lz, CRY);
            }
        }
        // Concentric rings
        for (int r = 10; r <= 35; r += 8) {
            for (int a = 0; a < 360; a += 3) {
                double rad = Math.toRadians(a);
                int lx = (int)(r * Math.cos(rad));
                int lz = courtZ + (int)(r * Math.sin(rad));
                s(lx, sY, lz, CRY);
            }
        }
    }

    private void buildApproachPath() {
        plugin.getLogger().info("[Citadel] Building approach path...");
        // Path from front courtyard outward
        for (int z = 100; z <= 130; z++) {
            for (int x = -5; x <= 5; x++) {
                int ty = findTerrainY(x, z);
                for (int y = ty; y <= sY; y++) s(x, y, z, Math.abs(x) <= 2 ? PBL : PBB);
                if (Math.abs(x) == 5 && z % 8 == 0) {
                    for (int dy = 1; dy <= 5; dy++) s(x, sY + dy, z, PIL);
                    s(x, sY + 6, z, SL);
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  PHASE 8: POPULATE
    // ═══════════════════════════════════════════════════════════
    private void populateChests() {
        plugin.getLogger().info("[Citadel] Populating chests with loot...");
        LegendaryWeaponManager weaponMgr = plugin.getLegendaryWeaponManager();
        PowerUpManager powerUpMgr = plugin.getPowerUpManager();

        List<Block> regularChests = new ArrayList<>();
        List<Block> abyssalChests = new ArrayList<>();

        for (int x = -130; x <= 130; x++) {
            for (int z = -170; z <= 140; z++) {
                for (int y = sY - 5; y <= sY + CENTER_SPIRE_H; y++) {
                    Block b = world.getBlockAt(x, y, z);
                    if (b.getType() != Material.CHEST) continue;
                    Block below = world.getBlockAt(x, y - 1, z);
                    if (below.getType() == AME) abyssalChests.add(b);
                    else regularChests.add(b);
                }
            }
        }
        plugin.getLogger().info("[Citadel] Found " + regularChests.size() + " regular chests and " + abyssalChests.size() + " abyssal weapon chests.");

        // Abyssal weapon chests
        LegendaryWeapon[] abyssalWeapons = LegendaryWeapon.byTier(LegendaryTier.ABYSSAL);
        for (int i = 0; i < abyssalChests.size() && i < abyssalWeapons.length; i++) {
            Block cb = abyssalChests.get(i);
            if (cb.getState() instanceof Chest chest) {
                Inventory inv = chest.getBlockInventory();
                inv.clear();
                if (weaponMgr != null) { ItemStack w = weaponMgr.createWeapon(abyssalWeapons[i]); if (w != null) inv.setItem(13, w); }
                inv.setItem(0, new ItemStack(Material.NETHER_STAR, 1));
                inv.setItem(4, new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 4));
                if (powerUpMgr != null) { inv.setItem(9, powerUpMgr.createPhoenixFeather()); inv.setItem(11, powerUpMgr.createHeartCrystal()); inv.setItem(15, powerUpMgr.createSoulFragment()); }
                inv.setItem(18, new ItemStack(Material.NETHERITE_INGOT, 2));
                inv.setItem(22, new ItemStack(Material.TOTEM_OF_UNDYING, 1));
                chest.update();
            }
        }

        // Regular chests
        LegendaryWeapon[] mythic = LegendaryWeapon.byTier(LegendaryTier.MYTHIC);
        LegendaryWeapon[] epic = LegendaryWeapon.byTier(LegendaryTier.EPIC);
        LegendaryWeapon[] rare = LegendaryWeapon.byTier(LegendaryTier.RARE);

        for (int i = 0; i < regularChests.size(); i++) {
            Block cb = regularChests.get(i);
            if (!(cb.getState() instanceof Chest chest)) continue;
            Inventory inv = chest.getBlockInventory(); inv.clear();
            int type = i % 5;
            switch (type) {
                case 0 -> {
                    if (weaponMgr != null && mythic.length > 0) { ItemStack w = weaponMgr.createWeapon(mythic[rng.nextInt(mythic.length)]); if (w != null) inv.setItem(13, w); }
                    inv.setItem(0, new ItemStack(Material.DIAMOND, 4 + rng.nextInt(8)));
                    inv.setItem(4, new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 1 + rng.nextInt(3)));
                }
                case 1 -> {
                    if (weaponMgr != null && epic.length > 0) { ItemStack w = weaponMgr.createWeapon(epic[rng.nextInt(epic.length)]); if (w != null) inv.setItem(13, w); }
                    inv.setItem(2, new ItemStack(Material.GOLDEN_APPLE, 3 + rng.nextInt(5)));
                }
                case 2 -> {
                    if (powerUpMgr != null) { inv.setItem(10, powerUpMgr.createHeartCrystal()); inv.setItem(12, powerUpMgr.createSoulFragment()); inv.setItem(14, powerUpMgr.createPhoenixFeather()); inv.setItem(16, powerUpMgr.createTitanResolve()); }
                    inv.setItem(4, new ItemStack(Material.EXPERIENCE_BOTTLE, 16 + rng.nextInt(32)));
                }
                case 3 -> {
                    inv.setItem(0, new ItemStack(Material.NETHERITE_INGOT, 1 + rng.nextInt(2)));
                    inv.setItem(2, new ItemStack(Material.TOTEM_OF_UNDYING, 1));
                    inv.setItem(4, new ItemStack(Material.ELYTRA, 1));
                    inv.setItem(6, new ItemStack(Material.DIAMOND_BLOCK, 1 + rng.nextInt(3)));
                    inv.setItem(8, new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 2));
                }
                case 4 -> {
                    if (weaponMgr != null && rare.length > 0) { ItemStack w = weaponMgr.createWeapon(rare[rng.nextInt(rare.length)]); if (w != null) inv.setItem(13, w); }
                    inv.setItem(4, new ItemStack(Material.DIAMOND, 6 + rng.nextInt(10)));
                    if (powerUpMgr != null) { inv.setItem(18, powerUpMgr.createVitalityShard()); inv.setItem(20, powerUpMgr.createBerserkerMark()); }
                }
            }
            chest.update();
        }
    }

    private void spawnGuards() {
        plugin.getLogger().info("[Citadel] Spawning guards...");
        // Courtyard guards
        for (int i = 0; i < 12; i++) {
            double a = rng.nextDouble() * Math.PI * 2;
            double d = 20 + rng.nextDouble() * 25;
            int gx = (int)(d * Math.cos(a));
            int gz = 65 + (int)(d * Math.sin(a));
            spawnGuard(gx, sY + 1, gz, "Abyssal Sentinel", false);
        }
        // Interior guards
        for (int z = -30; z <= 30; z += 15) {
            spawnGuard(-NAVE_WIDTH + 10, sY + 1, z, "Cathedral Warden", false);
            spawnGuard(NAVE_WIDTH - 10, sY + 1, z, "Cathedral Warden", false);
        }
        // Weapon chamber elite guards (6 per room)
        int[][] chamberPos = {{-NAVE_WIDTH-10,-25},{NAVE_WIDTH+10,-25},{-NAVE_WIDTH-10,25},{NAVE_WIDTH+10,25}};
        for (int[] cp : chamberPos) {
            for (int g = 0; g < 6; g++) {
                spawnGuard(cp[0] + rng.nextInt(8) - 4, sY + 1, cp[1] + rng.nextInt(8) - 4, "Abyssal Weapon Guardian", true);
            }
        }
        // Enderman patrols
        for (int i = 0; i < 8; i++) {
            int ex = -60 + rng.nextInt(120);
            int ez = -50 + rng.nextInt(100);
            Location loc = new Location(world, ex + 0.5, sY + 1, ez + 0.5);
            world.spawn(loc, Enderman.class, e -> {
                e.customName(Component.text("Void Wanderer", NamedTextColor.DARK_PURPLE));
                e.setCustomNameVisible(true); e.addScoreboardTag("abyss_citadel_mob"); e.setPersistent(true);
                Objects.requireNonNull(e.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(60); e.setHealth(60);
            });
        }
        // Arena guards
        for (int a = 0; a < 360; a += 45) {
            double rad = Math.toRadians(a);
            int gx = (int)((ARENA_RADIUS - 5) * Math.cos(rad));
            int gz = -ARENA_DIST + (int)((ARENA_RADIUS - 5) * Math.sin(rad));
            spawnGuard(gx, sY + 1, gz, "Arena Sentinel", false);
        }
    }

    private void spawnGuard(int x, int y, int z, String name, boolean elite) {
        Location loc = new Location(world, x + 0.5, y, z + 0.5);
        world.spawn(loc, WitherSkeleton.class, ws -> {
            ws.customName(Component.text(name, elite ? NamedTextColor.DARK_PURPLE : NamedTextColor.DARK_RED, elite ? TextDecoration.BOLD : TextDecoration.OBFUSCATED).decoration(TextDecoration.OBFUSCATED, false));
            ws.setCustomNameVisible(true); ws.addScoreboardTag("abyss_citadel_mob");
            if (elite) ws.addScoreboardTag("abyss_weapon_guardian");
            ws.setPersistent(true);
            double hp = elite ? 120 : 50;
            Objects.requireNonNull(ws.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(hp); ws.setHealth(hp);
            ws.getEquipment().setItemInMainHand(new ItemStack(elite ? Material.NETHERITE_SWORD : Material.STONE_SWORD));
            if (elite) { ws.getEquipment().setHelmet(new ItemStack(Material.NETHERITE_HELMET)); ws.getEquipment().setChestplate(new ItemStack(Material.NETHERITE_CHESTPLATE)); }
            else ws.getEquipment().setHelmet(new ItemStack(Material.CHAINMAIL_HELMET));
        });
    }

    // ═══════════════════════════════════════════════════════════
    //  UTILITIES
    // ═══════════════════════════════════════════════════════════
    private Material wallMat(int y, int layer) {
        if (layer == 0) {
            if (y % 8 == 0) return CD;
            if (y % 5 == 0) return PD;
            return DS;
        }
        if (layer == 1) return DT;
        return NB;
    }

    private void s(int x, int y, int z, Material m) {
        if (y < -64 || y > 319) return;
        world.getBlockAt(x, y, z).setType(m);
    }

    private int findSurface() {
        for (int y = 120; y > 1; y--) if (world.getBlockAt(0, y, 0).getType().isSolid()) return y;
        return 55;
    }

    private int findTerrainY(int x, int z) {
        for (int y = 120; y > 1; y--) if (world.getBlockAt(x, y, z).getType().isSolid()) return y;
        return 50;
    }
}
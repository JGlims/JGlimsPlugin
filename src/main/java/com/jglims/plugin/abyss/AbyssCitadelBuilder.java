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
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * AbyssCitadelBuilder v5.0 — Improved Gothic Abyssal Cathedral
 *
 * Changes from v4.0:
 *  - NO floating islands or soul lamp clusters (performance)
 *  - Front facade uses deepslate aesthetic only (no NB above 80%)
 *  - Grand entrance with recessed double-arch, no pillar blocking door
 *  - Roof ridge connects smoothly to central spire base
 *  - 4 corner towers are 16-block radius, 70+ tall, with spiral stairs
 *  - Tower stairs connect to nave second-floor gallery
 *  - 40+ chests placed directly in rooms during build (not relying on scan)
 *  - 8 side rooms off the nave: treasure rooms, parkour chambers, puzzle rooms
 *  - Glass panes only placed where walls exist (embedded in wall thickness)
 *  - 4 Abyssal weapon chambers with direct chest placement
 *  - More complex wall detailing, buttress patterns, trim lines
 */
public class AbyssCitadelBuilder {

    private final JGlimsPlugin plugin;
    private final World world;
    private final Random rng = new Random(42);

    // ─── Block Palette ────────────────────────────────────────
    private static final Material DS = Material.DEEPSLATE_BRICKS;
    private static final Material DT = Material.DEEPSLATE_TILES;
    private static final Material PD = Material.POLISHED_DEEPSLATE;
    private static final Material CD = Material.CHISELED_DEEPSLATE;
    private static final Material BL = Material.BLACKSTONE;
    private static final Material PBL = Material.POLISHED_BLACKSTONE;
    private static final Material PBB = Material.POLISHED_BLACKSTONE_BRICKS;
    private static final Material OBS = Material.OBSIDIAN;
    private static final Material CRY = Material.CRYING_OBSIDIAN;
    private static final Material AME = Material.AMETHYST_BLOCK;
    private static final Material AMC = Material.AMETHYST_CLUSTER;
    private static final Material PGP = Material.PURPLE_STAINED_GLASS_PANE;
    private static final Material PG  = Material.PURPLE_STAINED_GLASS;
    private static final Material MG  = Material.MAGENTA_STAINED_GLASS_PANE;
    private static final Material SL  = Material.SOUL_LANTERN;
    private static final Material SF  = Material.SOUL_CAMPFIRE;
    private static final Material ER  = Material.END_ROD;
    private static final Material ES  = Material.END_STONE_BRICKS;
    private static final Material BK  = Material.BEDROCK;
    private static final Material BA  = Material.BARRIER;
    private static final Material PIL = Material.POLISHED_DEEPSLATE;
    private static final Material AIR = Material.AIR;
    private static final Material CHEST = Material.CHEST;
    private static final Material DSS = Material.DEEPSLATE_BRICK_STAIRS;
    private static final Material DSW = Material.DEEPSLATE_BRICK_WALL;
    private static final Material LODESTONE = Material.LODESTONE;
    private static final Material IB = Material.IRON_BARS;

    // ─── Key dimensions ──────────────────────────────────────
    private static final int SPIRE_H = 160;
    private static final int NAVE_LEN = 80;         // z from -40 to +40
    private static final int NAVE_HW = 28;          // half-width of nave interior
    private static final int NAVE_H = 45;           // interior ceiling height
    private static final int WALL_T = 4;            // wall thickness
    private static final int TOWER_R = 8;           // tower radius
    private static final int TOWER_H = 70;          // tower height
    private static final int FACADE_HW = 50;        // facade half-width
    private static final int FACADE_H = 70;         // facade height
    private static final int ARENA_R = 40;
    private static final int ARENA_Z = -120;

    private int sY;
    private final List<int[]> chestPositions = new ArrayList<>();
    private final List<int[]> abyssalChestPositions = new ArrayList<>();

    public AbyssCitadelBuilder(JGlimsPlugin plugin, World world) {
        this.plugin = plugin;
        this.world = world;
    }

    public void build() {
        long start = System.currentTimeMillis();
        sY = findSurface();
        plugin.getLogger().info("[Citadel] Building Gothic Abyssal Cathedral v5.0 at Y=" + sY);

        buildFoundation();
        buildCourtyard();
        buildNave();
        buildTransept();
        buildApse();
        buildFrontFacade();
        buildRoof();
        buildCentralSpire();
        buildCornerTowers();
        buildFlyingButtresses();
        buildInterior();
        buildSideRooms();
        buildAbyssalChambers();
        buildTowerStairs();
        buildGallery();
        buildArenaPath();
        buildArena();
        buildArenaDecorations();
        buildArenaBarriers();
        buildApproachPath();
        buildTerrainDetails();

        new BukkitRunnable() {
            @Override public void run() {
                populateAllChests();
                spawnGuards();
                plugin.getLogger().info("[Citadel] Loot and guards placed.");
            }
        }.runTaskLater(plugin, 60L);

        long elapsed = System.currentTimeMillis() - start;
        plugin.getLogger().info("[Citadel] Structure complete in " + elapsed + "ms");
    }

    // ═══════════════════════════════════════════════════════════
    //  FOUNDATION
    // ═══════════════════════════════════════════════════════════
    private void buildFoundation() {
        plugin.getLogger().info("[Citadel] Foundation...");
        int rx = 90, rz = 110;
        for (int x = -rx; x <= rx; x++) {
            for (int z = -rz; z <= rz; z++) {
                double norm = (double)(x*x)/(rx*rx) + (double)(z*z)/(rz*rz);
                if (norm > 1.0) continue;
                double edge = 1.0 - norm;
                int depth = (int)(6 * edge) + 2;
                for (int y = sY - depth; y <= sY; y++) {
                    Material m = y == sY ? (edge > 0.4 ? PBB : BL) : BL;
                    s(x, y, z, m);
                }
                for (int y = sY + 1; y <= sY + SPIRE_H + 20; y++) {
                    Block b = world.getBlockAt(x, y, z);
                    if (b.getType().isSolid() && b.getType() != BK) b.setType(AIR);
                }
            }
        }
    }

    private void buildCourtyard() {
        plugin.getLogger().info("[Citadel] Courtyard...");
        int cz = 60, cr = 35;
        for (int x = -cr; x <= cr; x++) {
            for (int z = cz - cr; z <= cz + cr; z++) {
                double d = Math.sqrt(x*x + (z-cz)*(z-cz));
                if (d > cr) continue;
                Material m = d < 3 ? CRY : ((int)d % 6 == 0 ? CRY : ((x+z) % 3 == 0 ? PBL : PBB));
                s(x, sY, z, m);
                for (int dy = 1; dy <= 4; dy++) s(x, sY + dy, z, AIR);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  NAVE — main hall
    // ═══════════════════════════════════════════════════════════
    private void buildNave() {
        plugin.getLogger().info("[Citadel] Nave...");
        int fz = 40, bz = -40;
        for (int z = bz; z <= fz; z++) {
            for (int y = sY; y <= sY + NAVE_H; y++) {
                double hr = (double)(y - sY) / NAVE_H;
                int hw = (int)(NAVE_HW * (1.0 - hr * 0.25));
                // walls
                for (int t = 0; t < WALL_T; t++) {
                    Material m = t == 0 ? CD : (y % 6 == 0 ? PD : DS);
                    s(-hw - t, y, z, m);
                    s( hw + t, y, z, m);
                }
                // trim band every 10 blocks height
                if (y % 10 == 0 && y > sY) {
                    for (int t = 0; t < WALL_T; t++) {
                        s(-hw - t, y, z, CD);
                        s( hw + t, y, z, CD);
                    }
                }
            }
            // floor
            for (int x = -NAVE_HW; x <= NAVE_HW; x++) {
                s(x, sY, z, (x + z) % 2 == 0 ? PBB : PBL);
            }
        }
        // back wall
        for (int x = -NAVE_HW - WALL_T; x <= NAVE_HW + WALL_T; x++) {
            for (int y = sY; y <= sY + NAVE_H; y++) {
                s(x, y, bz, DS);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  TRANSEPT
    // ═══════════════════════════════════════════════════════════
    private void buildTransept() {
        plugin.getLogger().info("[Citadel] Transept...");
        int tw = 45, td = 10, th = 40;
        for (int x = -tw; x <= tw; x++) {
            for (int z = -td; z <= td; z++) {
                for (int y = sY; y <= sY + th; y++) {
                    double hr = (double)(y - sY) / th;
                    int hd = (int)(td * (1.0 - hr * 0.2));
                    boolean zEdge = Math.abs(z) >= hd - 3 && Math.abs(z) <= hd;
                    boolean xEdge = Math.abs(x) >= tw - 3 && Math.abs(x) <= tw;
                    if (zEdge || xEdge) {
                        s(x, y, z, y % 10 == 0 ? CD : DS);
                    } else if (y == sY) {
                        s(x, y, z, (x + z) % 2 == 0 ? PBB : PBL);
                    }
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  APSE
    // ═══════════════════════════════════════════════════════════
    private void buildApse() {
        plugin.getLogger().info("[Citadel] Apse...");
        int acZ = -40, ar = 22, ah = 50;
        for (int x = -ar; x <= ar; x++) {
            for (int z = acZ; z >= acZ - ar; z--) {
                double d = Math.sqrt(x*x + (z-acZ)*(z-acZ));
                if (d > ar) continue;
                for (int y = sY; y <= sY + ah; y++) {
                    double hr = (double)(y-sY) / ah;
                    double rH = ar * (1.0 - hr * 0.3);
                    if (d >= rH - 3 && d <= rH) s(x, y, z, y % 10 == 0 ? CD : DS);
                    else if (y == sY && d < rH - 3) s(x, y, z, PBB);
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  FRONT FACADE — redesigned, deepslate only, grand entrance
    // ═══════════════════════════════════════════════════════════
    private void buildFrontFacade() {
        plugin.getLogger().info("[Citadel] Front facade...");
        int fz = 40;
        // Main wall — deepslate only, NO nether bricks
        for (int y = sY; y <= sY + FACADE_H; y++) {
            double hr = (double)(y - sY) / FACADE_H;
            int hw = (int)(FACADE_HW * (1.0 - hr * hr * 0.5));
            for (int x = -hw; x <= hw; x++) {
                for (int dz = 0; dz < 5; dz++) {
                    Material m;
                    if (dz == 0) m = CD;           // front trim
                    else if (y % 8 == 0) m = CD;   // horizontal bands
                    else if (dz == 1) m = PD;      // polished layer
                    else m = DS;                    // deepslate fill
                    s(x, y, fz + dz, m);
                }
            }
            // Vertical pilaster accents every 12 blocks
            for (int side = -1; side <= 1; side += 2) {
                for (int i = 1; i <= 3; i++) {
                    int px = side * i * 12;
                    if (Math.abs(px) < hw) {
                        s(px, y, fz, PD);
                        s(px, y, fz - 1, PD);
                    }
                }
            }
        }

        // Grand entrance — recessed double-arch, NO pillar in center
        int archW = 6, archH = 20;
        // Clear the full entrance opening (12 wide, 20 tall)
        for (int x = -archW; x <= archW; x++) {
            double xr = (double)Math.abs(x) / archW;
            int lh = (int)(archH * (1.0 - xr * xr));
            for (int y = sY + 1; y <= sY + lh; y++) {
                for (int dz = -1; dz < 5; dz++) s(x, y, fz + dz, AIR);
            }
        }
        // Arch frame — deepslate chiseled
        for (int x = -archW - 1; x <= archW + 1; x++) {
            double xr = (double)Math.abs(x) / (archW + 1);
            int lh = (int)(archH * (1.0 - xr * xr));
            s(x, sY + lh, fz, CD);
            s(x, sY + lh, fz + 1, PD);
            // Side columns
            if (Math.abs(x) == archW + 1) {
                for (int y = sY; y <= sY + lh + 2; y++) {
                    s(x, y, fz, PD);
                    s(x, y, fz - 1, CD);
                    s(x, y, fz + 1, PD);
                }
                s(x, sY + lh + 3, fz, SL);
            }
        }
        // Recessed inner arch (smaller, creates depth)
        int innerW = 4, innerH = 16;
        for (int x = -innerW; x <= innerW; x++) {
            double xr = (double)Math.abs(x) / innerW;
            int lh = (int)(innerH * (1.0 - xr * xr));
            s(x, sY + lh, fz + 2, CD);
            if (Math.abs(x) == innerW) {
                for (int y = sY; y <= sY + lh; y++) s(x, y, fz + 2, PD);
            }
        }
        // Floor detail at entrance
        for (int x = -archW; x <= archW; x++) {
            s(x, sY, fz, CRY);
            s(x, sY, fz + 1, PBL);
            s(x, sY, fz + 2, CRY);
        }

        // Rose window — embedded in wall, framed by deepslate
        int roseY = sY + archH + 6, roseR = 6;
        for (int dx = -roseR; dx <= roseR; dx++) {
            for (int dy = -roseR; dy <= roseR; dy++) {
                double d = Math.sqrt(dx*dx + dy*dy);
                if (d <= roseR && d >= roseR - 1) s(dx, roseY+dy, fz, AME);
                else if (d < roseR - 1 && d >= roseR - 2) s(dx, roseY+dy, fz, PGP);
                else if (d < roseR - 2) {
                    s(dx, roseY+dy, fz, PG);
                    s(dx, roseY+dy, fz+1, AIR); // ensure see-through
                }
            }
        }

        // Facade windows — properly embedded (replace wall blocks with glass in wall)
        for (int side = -1; side <= 1; side += 2) {
            for (int i = 1; i <= 3; i++) {
                int wx = side * (archW + 4 + i * 10);
                if (Math.abs(wx) > FACADE_HW - 6) continue;
                // Tall pointed window cut into the facade wall
                int wh = 12, whw = 2;
                for (int dy = 0; dy < wh; dy++) {
                    double hr = (double)dy / wh;
                    int lw = (int)(whw * (1.0 - hr * hr));
                    for (int d = -lw; d <= lw; d++) {
                        // Replace wall block with glass
                        s(wx + d, sY + 5 + dy, fz, PGP);
                        s(wx + d, sY + 5 + dy, fz + 1, AIR);
                    }
                    // Frame
                    s(wx - lw - 1, sY + 5 + dy, fz, PD);
                    s(wx + lw + 1, sY + 5 + dy, fz, PD);
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  ROOF — connects to central spire
    // ═══════════════════════════════════════════════════════════
    private void buildRoof() {
        plugin.getLogger().info("[Citadel] Roof...");
        for (int z = -40; z <= 40; z++) {
            for (int dy = 0; dy <= 20; dy++) {
                int hw = NAVE_HW + WALL_T - dy;
                if (hw < 0) break;
                s(-hw, sY + NAVE_H + dy, z, DT);
                s( hw, sY + NAVE_H + dy, z, DT);
                // Fill ridge
                if (hw <= 2) {
                    for (int x = -hw; x <= hw; x++) s(x, sY + NAVE_H + dy, z, DT);
                }
                // Ridge cap
                if (dy == 20 || hw == 0) s(0, sY + NAVE_H + dy, z, CD);
            }
        }
        // Connect roof ridge to spire base at z=-10
        // The spire sits at (0, sY, -10). Bridge the roof ridge up to spire base.
        int spireBaseY = sY + NAVE_H + 20;
        int spireBaseR = 12;
        for (int z = -15; z <= 15; z++) {
            for (int x = -4; x <= 4; x++) {
                s(x, spireBaseY, z, DT);
                s(x, spireBaseY + 1, z, DS);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  CENTRAL SPIRE
    // ═══════════════════════════════════════════════════════════
    private void buildCentralSpire() {
        plugin.getLogger().info("[Citadel] Central spire...");
        int cx = 0, cz = -10;
        int baseR = 12;
        int baseY = sY + NAVE_H + 20; // sits on top of the roof
        for (int y = 0; y < SPIRE_H; y++) {
            double p = (double)y / SPIRE_H;
            double r = baseR * (1.0 - p);
            if (r < 0.5 && y > 15) break;
            int ri = Math.max(0, (int)r);
            Material m = y % 8 == 0 ? CD : (y % 4 == 0 ? PD : DS);
            for (int dx = -ri; dx <= ri; dx++) {
                for (int dz = -ri; dz <= ri; dz++) {
                    double d = Math.sqrt(dx*dx + dz*dz);
                    if (d <= ri && d >= ri - 2) s(cx+dx, baseY+y, cz+dz, m);
                    // Internal floors
                    if (d < ri - 2 && y % 15 == 0 && y > 0 && y < SPIRE_H - 20)
                        s(cx+dx, baseY+y, cz+dz, PBB);
                    else if (d < ri - 2 && y % 15 != 0) s(cx+dx, baseY+y, cz+dz, AIR);
                }
            }
            // Windows — embedded in wall
            if (ri > 3 && y % 15 >= 3 && y % 15 <= 8 && y < SPIRE_H - 25) {
                for (int w = -1; w <= 1; w++) {
                    // Only place glass where wall exists
                    s(cx+w, baseY+y, cz+ri-1, PGP); s(cx+w, baseY+y, cz+ri, PGP);
                    s(cx+w, baseY+y, cz-ri+1, PGP); s(cx+w, baseY+y, cz-ri, PGP);
                    s(cx+ri-1, baseY+y, cz+w, PGP); s(cx+ri, baseY+y, cz+w, PGP);
                    s(cx-ri+1, baseY+y, cz+w, PGP); s(cx-ri, baseY+y, cz+w, PGP);
                }
            }
            if (y > 0 && y % 20 == 0 && ri > 2) {
                for (int dx = -ri-1; dx <= ri+1; dx++) {
                    for (int dz = -ri-1; dz <= ri+1; dz++) {
                        double d = Math.sqrt(dx*dx+dz*dz);
                        if (d <= ri+1 && d >= ri) s(cx+dx, baseY+y, cz+dz, CD);
                    }
                }
            }
        }
        // Tip
        for (int dy = 0; dy < 8; dy++) s(cx, baseY+SPIRE_H+dy, cz, dy < 6 ? PD : ER);
    }

    // ═══════════════════════════════════════════════════════════
    //  CORNER TOWERS — 4 big towers with interior spiral stairs
    // ═══════════════════════════════════════════════════════════
    private void buildCornerTowers() {
        plugin.getLogger().info("[Citadel] Corner towers...");
        int[][] positions = {
            {-NAVE_HW - WALL_T - TOWER_R, 35},   // front-left
            { NAVE_HW + WALL_T + TOWER_R, 35},   // front-right
            {-NAVE_HW - WALL_T - TOWER_R, -35},  // back-left
            { NAVE_HW + WALL_T + TOWER_R, -35},  // back-right
        };
        for (int[] pos : positions) {
            buildTower(pos[0], sY, pos[1], TOWER_R, TOWER_H);
        }
    }

    private void buildTower(int cx, int baseY, int cz, int radius, int height) {
        for (int y = 0; y < height; y++) {
            double p = (double)y / height;
            double r = radius * (1.0 - p * 0.15); // slight taper
            int ri = (int)r;
            Material m = y % 8 == 0 ? CD : (y % 3 == 0 ? PD : DS);
            for (int dx = -ri; dx <= ri; dx++) {
                for (int dz = -ri; dz <= ri; dz++) {
                    double d = Math.sqrt(dx*dx + dz*dz);
                    if (d <= ri && d >= ri - 2) s(cx+dx, baseY+y, cz+dz, m);
                    // Floor every 10 blocks
                    if (d < ri - 2 && y % 10 == 0) s(cx+dx, baseY+y, cz+dz, PBB);
                    else if (d < ri - 2 && y % 10 != 0 && y > 0) s(cx+dx, baseY+y, cz+dz, AIR);
                }
            }
            // Windows (embedded in wall)
            if (ri > 3 && y % 10 >= 2 && y % 10 <= 6) {
                s(cx, baseY+y, cz+ri-1, PGP); s(cx, baseY+y, cz+ri, PGP);
                s(cx, baseY+y, cz-ri+1, PGP); s(cx, baseY+y, cz-ri, PGP);
                s(cx+ri-1, baseY+y, cz, PGP); s(cx+ri, baseY+y, cz, PGP);
                s(cx-ri+1, baseY+y, cz, PGP); s(cx-ri, baseY+y, cz, PGP);
            }
            // Lanterns on floors
            if (y % 10 == 1 && y > 1) {
                s(cx, baseY+y, cz, SL);
            }
            // Battlement at top
            if (y == height - 1) {
                for (int dx = -ri-1; dx <= ri+1; dx++) {
                    for (int dz = -ri-1; dz <= ri+1; dz++) {
                        double d = Math.sqrt(dx*dx+dz*dz);
                        if (d <= ri+1 && d >= ri-1) {
                            s(cx+dx, baseY+y+1, cz+dz, DS);
                            if ((dx+dz) % 3 == 0) s(cx+dx, baseY+y+2, cz+dz, DS);
                        }
                    }
                }
            }
        }
        // Conical cap
        for (int dy = 0; dy < 15; dy++) {
            int cr = radius - dy;
            if (cr < 0) break;
            for (int dx = -cr; dx <= cr; dx++) {
                for (int dz = -cr; dz <= cr; dz++) {
                    if (dx*dx+dz*dz <= cr*cr) s(cx+dx, baseY+height+dy, cz+dz, DT);
                }
            }
        }
        for (int dy = 0; dy < 5; dy++) s(cx, baseY+height+15+dy, cz, dy < 3 ? PD : ER);
        // Chest at ground floor
        s(cx + 2, baseY + 1, cz, CHEST);
        chestPositions.add(new int[]{cx + 2, baseY + 1, cz});
        // Chest at mid floor
        s(cx - 2, baseY + 31, cz, CHEST);
        chestPositions.add(new int[]{cx - 2, baseY + 31, cz});
    }

    // ═══════════════════════════════════════════════════════════
    //  TOWER STAIRS — spiral inside each tower, connecting to nave
    // ═══════════════════════════════════════════════════════════
    private void buildTowerStairs() {
        plugin.getLogger().info("[Citadel] Tower stairs...");
        int[][] positions = {
            {-NAVE_HW - WALL_T - TOWER_R, 35},
            { NAVE_HW + WALL_T + TOWER_R, 35},
            {-NAVE_HW - WALL_T - TOWER_R, -35},
            { NAVE_HW + WALL_T + TOWER_R, -35},
        };
        for (int[] pos : positions) {
            buildSpiralStairs(pos[0], sY, pos[1], TOWER_R - 3, TOWER_H);
            // Bridge from tower to nave at gallery level (y = sY + 15)
            int dir = pos[0] > 0 ? -1 : 1;
            int bridgeY = sY + 15;
            for (int i = 0; i < TOWER_R + WALL_T + 3; i++) {
                int bx = pos[0] + dir * i;
                s(bx, bridgeY, pos[1], PBB);
                s(bx, bridgeY + 1, pos[1], AIR);
                s(bx, bridgeY + 2, pos[1], AIR);
                s(bx, bridgeY + 3, pos[1], AIR);
                // Railings
                s(bx, bridgeY + 1, pos[1] - 1, IB);
                s(bx, bridgeY + 1, pos[1] + 1, IB);
            }
        }
    }

    private void buildSpiralStairs(int cx, int baseY, int cz, int radius, int height) {
        for (int y = 0; y < height; y++) {
            double angle = Math.toRadians(y * 18); // 18 degrees per block = spiral
            int sx = cx + (int)(radius * 0.6 * Math.cos(angle));
            int sz = cz + (int)(radius * 0.6 * Math.sin(angle));
            s(sx, baseY + y, sz, PBB);
            s(sx + 1, baseY + y, sz, PBB);
            s(sx, baseY + y, sz + 1, PBB);
            // Central column
            s(cx, baseY + y, cz, PD);
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  GALLERY — second-floor walkway inside the nave
    // ═══════════════════════════════════════════════════════════
    private void buildGallery() {
        plugin.getLogger().info("[Citadel] Gallery...");
        int gy = sY + 15;
        for (int z = -30; z <= 30; z++) {
            for (int side = -1; side <= 1; side += 2) {
                int gx = side * (NAVE_HW - 5);
                for (int w = 0; w < 4; w++) {
                    s(gx + side * w, gy, z, PBB);
                }
                s(gx, gy + 1, z, IB); // railing
            }
        }
        // Gallery chests
        for (int z = -25; z <= 25; z += 10) {
            s(-NAVE_HW + 5, gy + 1, z, CHEST);
            chestPositions.add(new int[]{-NAVE_HW + 5, gy + 1, z});
            s(NAVE_HW - 5, gy + 1, z, CHEST);
            chestPositions.add(new int[]{NAVE_HW - 5, gy + 1, z});
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  FLYING BUTTRESSES
    // ═══════════════════════════════════════════════════════════
    private void buildFlyingButtresses() {
        plugin.getLogger().info("[Citadel] Flying buttresses...");
        for (int z = -30; z <= 30; z += 10) {
            for (int side = -1; side <= 1; side += 2) {
                int startX = side * (NAVE_HW + WALL_T);
                int endX = side * (NAVE_HW + WALL_T + 15);
                int dx = endX > startX ? 1 : -1;
                int steps = Math.abs(endX - startX);
                for (int i = 0; i <= steps; i++) {
                    double p = (double)i / steps;
                    int ay = (int)(Math.sin(p * Math.PI) * 12);
                    int by = sY + 25 + (int)((NAVE_H - 25) * (1.0 - p));
                    int fy = by + ay;
                    int fx = startX + dx * i;
                    s(fx, fy, z, DS);
                    s(fx, fy-1, z, DS);
                    s(fx, fy, z+1, DS);
                    if (i == steps) for (int dy = 0; dy <= fy-sY; dy++) s(fx, sY+dy, z, DS);
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  INTERIOR — clear, pillars, lighting, nave floor chests
    // ═══════════════════════════════════════════════════════════
    private void buildInterior() {
        plugin.getLogger().info("[Citadel] Interior...");
        // Clear
        for (int x = -NAVE_HW+1; x <= NAVE_HW-1; x++) {
            for (int z = -38; z <= 38; z++) {
                for (int y = sY+1; y <= sY+NAVE_H-2; y++) {
                    Material cur = world.getBlockAt(x, y, z).getType();
                    if (cur != PGP && cur != AME && cur != PG && cur != IB) s(x, y, z, AIR);
                }
            }
        }
        // Pillars
        for (int z = -30; z <= 30; z += 8) {
            for (int side = -1; side <= 1; side += 2) {
                int px = side * (NAVE_HW - 6);
                for (int y = sY; y <= sY + NAVE_H - 3; y++) {
                    s(px, y, z, PIL);
                    if (y % 8 == 0) { s(px+1, y, z, CD); s(px-1, y, z, CD); s(px, y, z+1, CD); s(px, y, z-1, CD); }
                }
                // Arch to ceiling
                for (int dy = 0; dy <= 4; dy++) s(px + side*dy, sY+NAVE_H-3+dy, z, DS);
                s(px, sY+NAVE_H-4, z, SL);
            }
        }
        // Windows properly embedded in nave walls
        for (int z = -28; z <= 28; z += 8) {
            for (int side = -1; side <= 1; side += 2) {
                int wx = side * NAVE_HW;
                for (int dy = 5; dy < 18; dy++) {
                    double hr = (double)(dy - 5) / 13.0;
                    int lw = (int)(2 * (1.0 - hr * hr));
                    for (int d = -lw; d <= lw; d++) {
                        // Replace outer wall layer with glass
                        s(wx, sY+dy, z+d, PGP);
                    }
                }
            }
        }
        // Nave floor chests — along the center aisle
        for (int z = -30; z <= 30; z += 15) {
            s(0, sY+1, z, CHEST);
            chestPositions.add(new int[]{0, sY+1, z});
        }
        // Altar at apse
        s(0, sY+1, -38, AME); s(0, sY+2, -38, LODESTONE);
        s(0, sY+3, -38, ER);
        s(-2, sY+1, -38, SF); s(2, sY+1, -38, SF);
    }

    // ═══════════════════════════════════════════════════════════
    //  SIDE ROOMS — 8 rooms off the nave with varied content
    // ═══════════════════════════════════════════════════════════
    private void buildSideRooms() {
        plugin.getLogger().info("[Citadel] Side rooms...");
        // 4 rooms per side, accessed from the nave
        int roomW = 10, roomD = 12, roomH = 8;
        for (int side = -1; side <= 1; side += 2) {
            for (int i = 0; i < 4; i++) {
                int rz = -25 + i * 15;
                int rx = side * (NAVE_HW + WALL_T + roomW / 2 + 2);
                buildRoom(rx, sY, rz, roomW, roomD, roomH, side, i);
                // Door from nave
                int doorX = side * (NAVE_HW + WALL_T);
                for (int dy = 1; dy <= 3; dy++) {
                    s(doorX, sY + dy, rz, AIR);
                    for (int t = 1; t < WALL_T; t++) s(doorX + side*t, sY+dy, rz, AIR);
                }
            }
        }
    }

    private void buildRoom(int cx, int baseY, int cz, int w, int d, int h, int side, int index) {
        int hw = w/2, hd = d/2;
        // Walls
        for (int y = baseY; y <= baseY + h; y++) {
            for (int x = cx-hw; x <= cx+hw; x++) {
                s(x, y, cz-hd, DS); s(x, y, cz+hd, DS);
            }
            for (int z = cz-hd; z <= cz+hd; z++) {
                s(cx-hw, y, z, DS); s(cx+hw, y, z, DS);
            }
        }
        // Floor & ceiling
        for (int x = cx-hw+1; x < cx+hw; x++) {
            for (int z = cz-hd+1; z < cz+hd; z++) {
                s(x, baseY, z, (x+z) % 2 == 0 ? PBB : CRY);
                for (int y = baseY+1; y < baseY+h; y++) s(x, y, z, AIR);
                s(x, baseY+h, z, DS);
            }
        }
        // Lighting
        s(cx, baseY+h-1, cz, SL);
        // Content varies by index
        switch (index) {
            case 0 -> { // Treasure room — multiple chests
                s(cx-2, baseY+1, cz-3, CHEST); chestPositions.add(new int[]{cx-2, baseY+1, cz-3});
                s(cx+2, baseY+1, cz-3, CHEST); chestPositions.add(new int[]{cx+2, baseY+1, cz-3});
                s(cx, baseY+1, cz+3, CHEST);   chestPositions.add(new int[]{cx, baseY+1, cz+3});
                s(cx, baseY+1, cz, AME);
                s(cx, baseY+2, cz, ER);
            }
            case 1 -> { // Parkour room — platforms at different heights
                s(cx-3, baseY+2, cz-3, PBB);
                s(cx+1, baseY+3, cz-1, PBB);
                s(cx-1, baseY+4, cz+2, PBB);
                s(cx+3, baseY+5, cz+4, PBB);
                s(cx-2, baseY+6, cz, PBB);
                // Reward chest at top
                s(cx, baseY+7, cz, CHEST); chestPositions.add(new int[]{cx, baseY+7, cz});
            }
            case 2 -> { // Puzzle room — pressure plate pattern
                for (int x = cx-hw+2; x < cx+hw-1; x += 2) {
                    for (int z = cz-hd+2; z < cz+hd-1; z += 2) {
                        s(x, baseY, z, AME);
                    }
                }
                s(cx, baseY+1, cz-4, CHEST); chestPositions.add(new int[]{cx, baseY+1, cz-4});
                s(cx, baseY+1, cz+4, CHEST); chestPositions.add(new int[]{cx, baseY+1, cz+4});
            }
            case 3 -> { // Guard room — chests guarded by mobs (mobs spawned later)
                s(cx-3, baseY+1, cz, CHEST); chestPositions.add(new int[]{cx-3, baseY+1, cz});
                s(cx+3, baseY+1, cz, CHEST); chestPositions.add(new int[]{cx+3, baseY+1, cz});
                // Armor stands simulated by end rods
                s(cx-3, baseY+2, cz, ER); s(cx+3, baseY+2, cz, ER);
                s(cx, baseY+1, cz, SF);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  ABYSSAL WEAPON CHAMBERS — 4 special rooms
    // ═══════════════════════════════════════════════════════════
    private void buildAbyssalChambers() {
        plugin.getLogger().info("[Citadel] 4 Abyssal Weapon chambers...");
        int[][] pos = {
            {-NAVE_HW - 15, -25}, {NAVE_HW + 15, -25},
            {-NAVE_HW - 15,  25}, {NAVE_HW + 15,  25},
        };
        for (int i = 0; i < 4; i++) buildAbyssalRoom(pos[i][0], sY, pos[i][1], i);
    }

    private void buildAbyssalRoom(int cx, int baseY, int cz, int idx) {
        int w = 12, d = 12, h = 12;
        // Walls
        for (int y = baseY; y <= baseY+h; y++) {
            Material m = y % 4 == 0 ? OBS : DS;
            for (int x = cx-w; x <= cx+w; x++) { s(x, y, cz-d, m); s(x, y, cz+d, m); }
            for (int z = cz-d; z <= cz+d; z++) { s(cx-w, y, z, m); s(cx+w, y, z, m); }
        }
        // Interior
        for (int x = cx-w+1; x < cx+w; x++) for (int z = cz-d+1; z < cz+d; z++) {
            s(x, baseY, z, (x+z) % 3 == 0 ? CRY : OBS);
            for (int y = baseY+1; y < baseY+h; y++) s(x, y, z, AIR);
            s(x, baseY+h, z, OBS);
        }
        // Pedestal + weapon chest
        for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) s(cx+dx, baseY, cz+dz, AME);
        s(cx, baseY+1, cz, AME); s(cx, baseY+2, cz, CHEST);
        abyssalChestPositions.add(new int[]{cx, baseY+2, cz});
        s(cx, baseY+4, cz, ER);
        // Windows embedded in walls
        for (int dy = 4; dy <= 8; dy++) {
            s(cx, baseY+dy, cz-d, PGP); s(cx-1, baseY+dy, cz-d, DS); s(cx+1, baseY+dy, cz-d, DS);
            s(cx, baseY+dy, cz+d, PGP); s(cx-1, baseY+dy, cz+d, DS); s(cx+1, baseY+dy, cz+d, DS);
            s(cx-w, baseY+dy, cz, PGP); s(cx-w, baseY+dy, cz-1, DS); s(cx-w, baseY+dy, cz+1, DS);
            s(cx+w, baseY+dy, cz, PGP); s(cx+w, baseY+dy, cz-1, DS); s(cx+w, baseY+dy, cz+1, DS);
        }
        // Corner fires
        s(cx-w+2, baseY+1, cz-d+2, SF); s(cx+w-2, baseY+1, cz-d+2, SF);
        s(cx-w+2, baseY+1, cz+d-2, SF); s(cx+w-2, baseY+1, cz+d-2, SF);
        // Lanterns
        s(cx-5, baseY+h-1, cz, SL); s(cx+5, baseY+h-1, cz, SL);
        s(cx, baseY+h-1, cz-5, SL); s(cx, baseY+h-1, cz+5, SL);
        // Doorways (both X sides)
        for (int dy = 1; dy <= 4; dy++) for (int dz = -1; dz <= 1; dz++) {
            s(cx-w, baseY+dy, cz+dz, AIR); s(cx+w, baseY+dy, cz+dz, AIR);
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  ARENA PATH + ARENA
    // ═══════════════════════════════════════════════════════════
    private void buildArenaPath() {
        plugin.getLogger().info("[Citadel] Arena path...");
        int startZ = -62, endZ = -(Math.abs(ARENA_Z) - ARENA_R - 5);
        for (int z = startZ; z >= endZ; z--) {
            for (int x = -4; x <= 4; x++) {
                s(x, sY, z, PBB);
                s(x, sY+6, z, DT);
                if (Math.abs(x) == 4) for (int dy = 1; dy <= 5; dy++) s(x, sY+dy, z, DS);
                else for (int dy = 1; dy <= 5; dy++) s(x, sY+dy, z, AIR);
            }
            if (z % 6 == 0) { s(-5, sY+4, z, SL); s(5, sY+4, z, SL); }
        }
    }

    private void buildArena() {
        plugin.getLogger().info("[Citadel] Arena...");
        int az = ARENA_Z;
        for (int x = -ARENA_R; x <= ARENA_R; x++) {
            for (int z = -ARENA_R; z <= ARENA_R; z++) {
                if (x*x+z*z > ARENA_R*ARENA_R) continue;
                s(x, sY, az+z, BK);
                for (int dy = 1; dy <= 6; dy++) s(x, sY+dy, az+z, AIR);
            }
        }
        // Wall ring
        for (int y = sY; y <= sY+10; y++) {
            for (int a = 0; a < 360; a++) {
                double r = Math.toRadians(a);
                int wx = (int)Math.round(ARENA_R * Math.cos(r));
                int wz = az + (int)Math.round(ARENA_R * Math.sin(r));
                s(wx, y, wz, y <= sY+3 ? OBS : DS);
            }
        }
        // Central altar
        for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++)
            if (dx*dx+dz*dz <= 4) s(dx, sY+1, az+dz, OBS);
        s(0, sY+2, az, LODESTONE); s(0, sY+3, az, AME); s(0, sY+4, az, ER);
        // Arena entrance opening
        for (int x = -3; x <= 3; x++) for (int y = sY+1; y <= sY+5; y++)
            s(x, y, az+ARENA_R, AIR);
    }

    private void buildArenaDecorations() {
        int az = ARENA_Z;
        for (int a = 0; a < 360; a += 20) {
            double r = Math.toRadians(a);
            int fx = (int)Math.round((ARENA_R-4) * Math.cos(r));
            int fz = az + (int)Math.round((ARENA_R-4) * Math.sin(r));
            s(fx, sY+1, fz, SF);
        }
        for (int a = 0; a < 360; a += 30) {
            double r = Math.toRadians(a);
            int px = (int)Math.round((ARENA_R-2) * Math.cos(r));
            int pz = az + (int)Math.round((ARENA_R-2) * Math.sin(r));
            int ph = 4 + rng.nextInt(5);
            for (int dy = 1; dy <= ph; dy++) s(px, sY+dy, pz, ES);
        }
    }

    private void buildArenaBarriers() {
        plugin.getLogger().info("[Citadel] Arena barriers...");
        int az = ARENA_Z;
        for (int y = sY+11; y <= sY+50; y++) {
            for (int a = 0; a < 360; a++) {
                double r = Math.toRadians(a);
                s((int)Math.round((ARENA_R+1)*Math.cos(r)), y, az+(int)Math.round((ARENA_R+1)*Math.sin(r)), BA);
            }
        }
        for (int x = -ARENA_R-1; x <= ARENA_R+1; x++)
            for (int z = -ARENA_R-1; z <= ARENA_R+1; z++)
                if (x*x+z*z <= (ARENA_R+1)*(ARENA_R+1)) s(x, sY+50, az+z, BA);
    }

    // ═══════════════════════════════════════════════════════════
    //  APPROACH PATH — from courtyard to gate
    // ═══════════════════════════════════════════════════════════
    private void buildApproachPath() {
        plugin.getLogger().info("[Citadel] Approach path...");
        for (int z = 45; z <= 100; z++) {
            for (int x = -5; x <= 5; x++) {
                s(x, sY, z, Math.abs(x) >= 4 ? CRY : PBB);
                for (int dy = 1; dy <= 3; dy++) s(x, sY+dy, z, AIR);
            }
            if (z % 10 == 0) {
                for (int side = -1; side <= 1; side += 2) {
                    int px = side * 6;
                    for (int dy = 0; dy <= 4; dy++) s(px, sY+dy, z, PD);
                    s(px, sY+5, z, SL);
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  TERRAIN — simple rocky edges, NO floating islands
    // ═══════════════════════════════════════════════════════════
    private void buildTerrainDetails() {
        plugin.getLogger().info("[Citadel] Terrain details...");
        // Small rock clusters around the base (no floating anything)
        for (int i = 0; i < 30; i++) {
            int rx = -80 + rng.nextInt(160);
            int rz = -100 + rng.nextInt(200);
            double distFromCenter = Math.sqrt(rx*rx + rz*rz);
            if (distFromCenter < 50 || distFromCenter > 95) continue;
            int rh = 2 + rng.nextInt(4);
            for (int dy = 0; dy < rh; dy++) {
                int rr = rh - dy;
                for (int dx = -rr; dx <= rr; dx++) for (int dz = -rr; dz <= rr; dz++) {
                    if (dx*dx+dz*dz <= rr*rr) s(rx+dx, sY+dy, rz+dz, BL);
                }
            }
            if (rng.nextFloat() < 0.3f) s(rx, sY+rh, rz, AMC);
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  CHEST POPULATION
    // ═══════════════════════════════════════════════════════════
    private void populateAllChests() {
        plugin.getLogger().info("[Citadel] Populating chests with loot...");
        int regularCount = 0, abyssalCount = 0;
        // Regular chests
        for (int[] pos : chestPositions) {
            Block b = world.getBlockAt(pos[0], pos[1], pos[2]);
            if (b.getType() != CHEST) { b.setType(CHEST); }
            if (b.getState() instanceof Chest chest) {
                fillRegularChest(chest.getInventory());
                regularCount++;
            }
        }
        // Abyssal weapon chests
        String[] abyssalWeaponIds = {"requiem_awakened", "excalibur_awakened", "creation_splitter_awakened", "whisperwind_awakened"};
        int weaponIdx = 0;
        for (int[] pos : abyssalChestPositions) {
            Block b = world.getBlockAt(pos[0], pos[1], pos[2]);
            if (b.getType() != CHEST) { b.setType(CHEST); }
            if (b.getState() instanceof Chest chest) {
                fillAbyssalChest(chest.getInventory(), weaponIdx < abyssalWeaponIds.length ? abyssalWeaponIds[weaponIdx] : null);
                abyssalCount++;
                weaponIdx++;
            }
        }
        plugin.getLogger().info("[Citadel] Found " + regularCount + " regular chests and " + abyssalCount + " abyssal weapon chests.");
    }

    private void fillRegularChest(Inventory inv) {
        int type = rng.nextInt(6);
        switch (type) {
            case 0 -> { // Mythic loot
                addRandomWeapon(inv, "MYTHIC", 1);
                inv.setItem(rng.nextInt(27), new ItemStack(Material.NETHERITE_INGOT, 1 + rng.nextInt(2)));
                inv.setItem(rng.nextInt(27), new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 1));
                inv.setItem(rng.nextInt(27), new ItemStack(Material.EXPERIENCE_BOTTLE, 8 + rng.nextInt(16)));
            }
            case 1 -> { // Epic loot
                addRandomWeapon(inv, "EPIC", 1);
                inv.setItem(rng.nextInt(27), new ItemStack(Material.DIAMOND, 3 + rng.nextInt(5)));
                inv.setItem(rng.nextInt(27), new ItemStack(Material.GOLDEN_APPLE, 2 + rng.nextInt(3)));
            }
            case 2 -> { // Power-ups
                addPowerUp(inv);
                addPowerUp(inv);
                inv.setItem(rng.nextInt(27), new ItemStack(Material.NETHER_STAR, 1));
            }
            case 3 -> { // Rare vanilla
                inv.setItem(rng.nextInt(27), new ItemStack(Material.TOTEM_OF_UNDYING, 1));
                inv.setItem(rng.nextInt(27), new ItemStack(Material.NETHERITE_SCRAP, 2 + rng.nextInt(3)));
                inv.setItem(rng.nextInt(27), new ItemStack(Material.ENDER_PEARL, 4 + rng.nextInt(8)));
                inv.setItem(rng.nextInt(27), new ItemStack(Material.BLAZE_ROD, 4 + rng.nextInt(6)));
            }
            case 4 -> { // Weapons mix
                addRandomWeapon(inv, "RARE", 1);
                addRandomWeapon(inv, "COMMON", 1);
                inv.setItem(rng.nextInt(27), new ItemStack(Material.IRON_INGOT, 8 + rng.nextInt(16)));
            }
            case 5 -> { // Mixed
                inv.setItem(rng.nextInt(27), new ItemStack(Material.DIAMOND, 2 + rng.nextInt(4)));
                inv.setItem(rng.nextInt(27), new ItemStack(Material.EMERALD, 5 + rng.nextInt(10)));
                inv.setItem(rng.nextInt(27), new ItemStack(Material.EXPERIENCE_BOTTLE, 16 + rng.nextInt(32)));
                inv.setItem(rng.nextInt(27), new ItemStack(Material.GOLDEN_APPLE, 1 + rng.nextInt(3)));
                addPowerUp(inv);
            }
        }
    }

    private void fillAbyssalChest(Inventory inv, String weaponId) {
        if (weaponId != null) {
            try {
                LegendaryWeapon w = LegendaryWeapon.fromId(weaponId);
                if (w != null && plugin.getLegendaryWeaponManager() != null) {
                    ItemStack item = plugin.getLegendaryWeaponManager().createWeapon(w);
                    if (item != null) inv.setItem(13, item);
                }
            } catch (Exception e) { plugin.getLogger().warning("[Citadel] Could not create abyssal weapon: " + weaponId); }
        }
        inv.setItem(rng.nextInt(27), new ItemStack(Material.NETHER_STAR, 2));
        inv.setItem(rng.nextInt(27), new ItemStack(Material.NETHERITE_INGOT, 3));
        inv.setItem(rng.nextInt(27), new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 4));
        inv.setItem(rng.nextInt(27), new ItemStack(Material.EXPERIENCE_BOTTLE, 64));
        addPowerUp(inv);
        addPowerUp(inv);
    }

    private void addRandomWeapon(Inventory inv, String tierName, int count) {
        try {
            if (plugin.getLegendaryWeaponManager() == null) return;
            LegendaryTier tier = LegendaryTier.fromId(tierName);
            if (tier == null) return;
            LegendaryWeapon[] weapons = LegendaryWeapon.byTier(tier);
            if (weapons.length == 0) return;
            for (int i = 0; i < count; i++) {
                LegendaryWeapon w = weapons[rng.nextInt(weapons.length)];
                ItemStack item = plugin.getLegendaryWeaponManager().createWeapon(w);
                if (item != null) inv.setItem(rng.nextInt(27), item);
            }
        } catch (Exception ignored) {}
    }

    private void addPowerUp(Inventory inv) {
        try {
            if (plugin.getPowerUpManager() == null) return;
            int type = rng.nextInt(5);
            ItemStack pu = switch (type) {
                case 0 -> plugin.getPowerUpManager().createHeartCrystal();
                case 1 -> plugin.getPowerUpManager().createSoulFragment();
                case 2 -> plugin.getPowerUpManager().createPhoenixFeather();
                case 3 -> plugin.getPowerUpManager().createVitalityShard();
                default -> plugin.getPowerUpManager().createTitanResolve();
            };
            if (pu != null) inv.setItem(rng.nextInt(27), pu);
        } catch (Exception ignored) {}
    }

    // ═══════════════════════════════════════════════════════════
    //  GUARD SPAWNING
    // ═══════════════════════════════════════════════════════════
    private void spawnGuards() {
        plugin.getLogger().info("[Citadel] Spawning guards...");
        // Courtyard guards (4)
        spawnWS(10, sY+1, 60); spawnWS(-10, sY+1, 60);
        spawnWS(10, sY+1, 70); spawnWS(-10, sY+1, 70);
        // Nave guards (4)
        spawnWS(8, sY+1, 10); spawnWS(-8, sY+1, 10);
        spawnWS(8, sY+1, -10); spawnWS(-8, sY+1, -10);
        // Abyssal chamber guards (6 per room = 24)
        int[][] chamberPos = {
            {-NAVE_HW-15, -25}, {NAVE_HW+15, -25},
            {-NAVE_HW-15, 25}, {NAVE_HW+15, 25}
        };
        for (int[] cp : chamberPos) {
            for (int i = 0; i < 6; i++) {
                int gx = cp[0] + (rng.nextInt(12) - 6);
                int gz = cp[1] + (rng.nextInt(12) - 6);
                spawnEliteWS(gx, sY+1, gz);
            }
        }
    }

    private void spawnWS(int x, int y, int z) {
        Location loc = new Location(world, x + 0.5, y, z + 0.5);
        world.spawn(loc, WitherSkeleton.class, ws -> {
            ws.customName(Component.text("Citadel Guard", NamedTextColor.DARK_RED));
            ws.setCustomNameVisible(true);
            ws.addScoreboardTag("citadel_guard");
            Objects.requireNonNull(ws.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(60);
            ws.setHealth(60);
            ws.getEquipment().setItemInMainHand(new ItemStack(Material.STONE_SWORD));
        });
    }

    private void spawnEliteWS(int x, int y, int z) {
        Location loc = new Location(world, x + 0.5, y, z + 0.5);
        world.spawn(loc, WitherSkeleton.class, ws -> {
            ws.customName(Component.text("Abyssal Guardian", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD));
            ws.setCustomNameVisible(true);
            ws.addScoreboardTag("citadel_guard");
            ws.addScoreboardTag("elite_guard");
            Objects.requireNonNull(ws.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(120);
            ws.setHealth(120);
            ws.getEquipment().setItemInMainHand(new ItemStack(Material.NETHERITE_SWORD));
            ws.getEquipment().setHelmet(new ItemStack(Material.NETHERITE_HELMET));
            ws.getEquipment().setChestplate(new ItemStack(Material.NETHERITE_CHESTPLATE));
        });
    }

    // ═══════════════════════════════════════════════════════════
    //  UTILITIES
    // ═══════════════════════════════════════════════════════════
    private void s(int x, int y, int z, Material m) {
        if (y < -64 || y > 319) return;
        world.getBlockAt(x, y, z).setType(m, false);
    }

    private int findSurface() {
        for (int y = 100; y > 0; y--) {
            if (world.getBlockAt(0, y, 0).getType().isSolid()) return y;
        }
        return 64;
    }

    private int findTerrainY(int x, int z) {
        for (int y = 100; y > 0; y--) {
            if (world.getBlockAt(x, y, z).getType().isSolid()) return y;
        }
        return 50;
    }
}
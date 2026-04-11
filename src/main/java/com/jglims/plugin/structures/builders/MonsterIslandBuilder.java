package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;

/**
 * MonsterIslandBuilder — scorched, ash-covered volcanic island, home of
 * Godzilla. Features a massive central volcano with an active caldera, four
 * creature lair caves around the shore, a shipwreck, a dead forest and a
 * hidden research bunker.
 *
 * <p>Target footprint roughly 100×100 (half radius 50). This is one of the
 * biggest structures in the game.
 */
public final class MonsterIslandBuilder implements IStructureBuilder {

    private static final Material ST  = Material.STONE;
    private static final Material CST = Material.COBBLESTONE;
    private static final Material MCB = Material.MOSSY_COBBLESTONE;
    private static final Material BST = Material.BLACKSTONE;
    private static final Material BSL = Material.BASALT;
    private static final Material MAG = Material.MAGMA_BLOCK;
    private static final Material LAV = Material.LAVA;
    private static final Material SND = Material.SAND;
    private static final Material GRV = Material.GRAVEL;
    private static final Material DTW = Material.DEAD_BUSH;

    @Override
    public void build(StructureBuilder b) {
        // ─── 1. Island base ───────────────────────────────────────
        // Lower submerged rim
        b.filledCircle(0, -3, 0, 48, ST);
        b.filledCircle(0, -2, 0, 45, GRV);
        // Beach sand
        b.filledCircle(0, -1, 0, 42, SND);
        b.filledCircle(0,  0, 0, 38, SND);
        // Ash-covered interior
        b.filledCircle(0,  0, 0, 32, Material.COARSE_DIRT);
        b.scatter(-32, 0, -32, 32, 0, 32, Material.COARSE_DIRT, MAG, 0.05);
        b.scatter(-32, 0, -32, 32, 0, 32, Material.COARSE_DIRT, BST, 0.08);

        // ─── 2. Central volcano — 30 high, tapering cone ─────────
        for (int y = 0; y < 30; y++) {
            int r = 24 - y;
            if (r <= 2) break;
            b.circle(0, y, 0, r, BSL);
            // Scatter magma & blackstone into the slope
            if (y % 3 == 0) {
                b.circle(0, y, 0, r - 1, BST);
            }
        }
        // Caldera — hollow the top
        b.filledCircle(0, 22, 0, 8, Material.AIR);
        b.filledCircle(0, 23, 0, 8, Material.AIR);
        b.filledCircle(0, 21, 0, 7, LAV);     // active lava
        b.filledCircle(0, 21, 0, 6, MAG);     // magma floor ring
        // Chain of magma around the rim
        b.circle(0, 24, 0, 9, MAG);
        // Ash plume spires
        b.pillar(0, 25, 32, 0, BST);
        b.setBlock(0, 32, 0, MAG);

        // ─── 3. Lava flows running down one side ──────────────────
        for (int t = 0; t < 20; t++) {
            int x = t;
            int y = 22 - t;
            int z = 2 + t / 4;
            if (y < 1) break;
            b.setBlock(x, y, z, LAV);
            b.setBlock(x, y, z + 1, MAG);
            b.setBlock(x - 1, y, z, BST);
        }

        // ─── 4. Four creature lair caves around the shore ────────
        creatureLair(b,  35, 0,  10);
        creatureLair(b, -35, 0, -10);
        creatureLair(b,  10, 0,  35);
        creatureLair(b, -10, 0, -35);

        // ─── 5. Shipwreck on the east beach ──────────────────────
        shipwreck(b, 38, -1, 15);

        // ─── 6. Dead forest ring ─────────────────────────────────
        deadTree(b,  18, 0,  15);
        deadTree(b, -20, 0,  18);
        deadTree(b, -18, 0, -20);
        deadTree(b,  22, 0, -17);
        deadTree(b,  14, 0, -26);
        deadTree(b, -25, 0,  12);
        b.scatter(-30, 0, -30, 30, 0, 30, Material.COARSE_DIRT, Material.DEAD_BUSH, 0.03);

        // ─── 7. Hidden research bunker under the ash ──────────────
        b.fillBox(-25, -8, -25, -15, -2, -15, Material.AIR);
        b.fillWalls(-25, -8, -25, -15, -2, -15, Material.DEEPSLATE_BRICKS);
        b.fillFloor(-25, -25, -15, -15, -8, Material.POLISHED_DEEPSLATE);
        b.setBlock(-20, -7, -20, Material.CRAFTING_TABLE);
        b.setBlock(-19, -7, -20, Material.FURNACE);
        b.setBlock(-22, -7, -20, Material.LANTERN);
        b.placeChest(-21, -7, -17);
        b.placeChest(-19, -7, -17);
        // Shaft up to surface
        b.fillBox(-20, -2, -20, -20, 0, -20, Material.AIR);
        for (int y = -7; y <= -1; y++) b.setBlock(-20, y, -19, Material.LADDER);
        // Disguised hatch
        b.setBlock(-20, 0, -20, Material.IRON_TRAPDOOR);

        // ─── 8. Environmental polish — dead bushes & bones ───────
        for (int i = 0; i < 40; i++) {
            int bx = (int) (Math.random() * 60) - 30;
            int bz = (int) (Math.random() * 60) - 30;
            if (bx * bx + bz * bz > 800 || bx * bx + bz * bz < 400) continue;
            b.setBlock(bx, 1, bz, DTW);
        }
        // Scattered bone piles
        b.setBlock( 25, 1,  10, Material.BONE_BLOCK);
        b.setBlock( 26, 1,  10, Material.BONE_BLOCK);
        b.setBlock( 25, 2,  11, Material.BONE_BLOCK);
        b.setBlock(-28, 1, -12, Material.BONE_BLOCK);
        b.setBlock(-28, 2, -12, Material.BONE_BLOCK);

        // Perimeter warning lights (survivors' flares)
        b.setBlock( 40, 0,  0, Material.TORCH);
        b.setBlock(-40, 0,  0, Material.TORCH);
        b.setBlock( 0, 0,  40, Material.TORCH);
        b.setBlock( 0, 0, -40, Material.TORCH);

        // Boss spawn at the caldera's edge (Godzilla rises from the lava)
        b.setBossSpawn(0, 25, 0);
    }

    private void creatureLair(StructureBuilder b, int cx, int y, int cz) {
        // Semi-domed hollow cave
        for (int dy = 0; dy < 5; dy++)
            for (int dx = -4; dx <= 4; dx++)
                for (int dz = -4; dz <= 4; dz++)
                    if (dx * dx + dy * dy + dz * dz <= 20)
                        b.setBlock(cx + dx, y + dy, cz + dz, Material.AIR);
        // Shell
        for (int a = 0; a < 360; a += 15) {
            int r = 5;
            int px = cx + (int) Math.round(r * Math.cos(Math.toRadians(a)));
            int pz = cz + (int) Math.round(r * Math.sin(Math.toRadians(a)));
            b.setBlock(px, y, pz, BST);
            b.setBlock(px, y + 2, pz, MCB);
        }
        // Bone piles & chest
        b.setBlock(cx, y, cz, Material.BONE_BLOCK);
        b.setBlock(cx + 1, y, cz, Material.BONE_BLOCK);
        b.placeChest(cx, y + 1, cz + 2);
    }

    private void shipwreck(StructureBuilder b, int x, int y, int z) {
        // Broken hull — oak planks with decay
        for (int dx = -6; dx <= 6; dx++)
            for (int dz = -2; dz <= 2; dz++)
                b.setBlock(x + dx, y, z + dz, Material.OAK_PLANKS);
        for (int dx = -6; dx <= 6; dx++) {
            b.setBlock(x + dx, y + 1, z - 2, Material.OAK_PLANKS);
            b.setBlock(x + dx, y + 1, z + 2, Material.OAK_PLANKS);
        }
        // Broken mast
        b.pillar(x, y + 1, y + 8, z, Material.OAK_LOG);
        b.setBlock(x, y + 9, z, Material.OAK_LOG);
        // Tattered sail
        b.setBlock(x + 1, y + 6, z, Material.WHITE_WOOL);
        b.setBlock(x + 2, y + 5, z, Material.WHITE_WOOL);
        b.setBlock(x - 1, y + 7, z, Material.WHITE_WOOL);
        // Decay the whole thing
        b.decay(x - 6, y, z - 2, x + 6, y + 9, z + 2, 0.30);
        // Treasure from the wreck
        b.placeChest(x, y, z);
        b.placeChest(x - 3, y, z);
    }

    private void deadTree(StructureBuilder b, int x, int y, int z) {
        int h = 5 + (int) (Math.random() * 4);
        b.pillar(x, y, y + h, z, Material.DARK_OAK_LOG);
        // Jagged bare branches
        b.setBlock(x + 1, y + h - 1, z, Material.DARK_OAK_LOG);
        b.setBlock(x - 1, y + h - 2, z, Material.DARK_OAK_LOG);
        b.setBlock(x, y + h - 1, z + 1, Material.DARK_OAK_LOG);
        b.setBlock(x, y + h, z, Material.DARK_OAK_LOG);
    }
}

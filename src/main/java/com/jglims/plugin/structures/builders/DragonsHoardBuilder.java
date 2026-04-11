package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

/**
 * DragonsHoardBuilder — a concealed End-stone vault containing the treasure
 * of a fallen dragon. The centerpiece is a raised gold-block throne surrounded
 * by piles of plunder, four carved dragon-head pillars and a filled dome
 * ceiling lit with amethyst clusters.
 */
public final class DragonsHoardBuilder implements IStructureBuilder {

    private static final Material ES  = Material.END_STONE_BRICKS;
    private static final Material EST = Material.END_STONE;
    private static final Material OB  = Material.OBSIDIAN;
    private static final Material GLD = Material.GOLD_BLOCK;
    private static final Material DIA = Material.DIAMOND_BLOCK;
    private static final Material EMR = Material.EMERALD_BLOCK;
    private static final Material LZ  = Material.LAPIS_BLOCK;
    private static final Material AMT = Material.AMETHYST_BLOCK;
    private static final Material AMC = Material.AMETHYST_CLUSTER;

    @Override
    public void build(StructureBuilder b) {
        // ─── 1. Outer dome ─────────────────────────────────────────
        b.filledDome(0, 0, 0, 18, ES);
        // Hollow interior
        for (int y = 1; y <= 16; y++) {
            double r = Math.sqrt(17 * 17 - y * y);
            b.filledCircle(0, y, 0, (int) r - 1, Material.AIR);
        }
        // Floor — polished end-stone
        b.filledCircle(0, 0, 0, 17, EST);
        b.filledCircle(0, 0, 0, 15, ES);

        // ─── 2. Inner treasury floor — gold &amp; gem inlay ─────────
        b.filledCircle(0, 1, 0, 12, GLD);
        // Radial gem spokes
        for (int a = 0; a < 360; a += 45) {
            double rad = Math.toRadians(a);
            for (int r = 2; r <= 12; r++) {
                int px = (int) Math.round(r * Math.cos(rad));
                int pz = (int) Math.round(r * Math.sin(rad));
                b.setBlock(px, 1, pz, r % 2 == 0 ? DIA : EMR);
            }
        }

        // ─── 3. Four dragon-head pillars ────────────────────────
        dragonHeadPillar(b,  10,  10);
        dragonHeadPillar(b, -10,  10);
        dragonHeadPillar(b,  10, -10);
        dragonHeadPillar(b, -10, -10);

        // ─── 4. Central gold throne on obsidian plinth ───────────
        b.fillBox(-3, 1, -3, 3, 1, 3, OB);
        b.fillBox(-2, 2, -2, 2, 2, 2, GLD);
        b.setStairs(0, 3, -2, Material.PURPUR_STAIRS, BlockFace.SOUTH, false);
        b.setBlock(0, 3, -1, GLD);
        b.setBlock(-1, 3, -2, GLD);
        b.setBlock( 1, 3, -2, GLD);
        b.setBlock(-1, 4, -2, DIA);  // throne arm
        b.setBlock( 1, 4, -2, DIA);  // throne arm
        b.setBlock(0, 5, -2, EMR);   // throne back crown
        // Beacon under the throne
        b.setBlock(0, 2, 0, Material.BEACON);

        // ─── 5. Piles of plunder scattered around ────────────────
        plunderPile(b,  7, 2,  5);
        plunderPile(b, -7, 2,  5);
        plunderPile(b,  7, 2, -5);
        plunderPile(b, -7, 2, -5);
        plunderPile(b,  0, 2,  9);
        plunderPile(b,  0, 2, -9);

        // ─── 6. Iron bar "prison" walls separating loot zones ──
        for (int a = 22; a < 360; a += 45) {
            double rad = Math.toRadians(a);
            for (int r = 10; r <= 14; r++) {
                int px = (int) Math.round(r * Math.cos(rad));
                int pz = (int) Math.round(r * Math.sin(rad));
                b.setBlock(px, 2, pz, Material.IRON_BARS);
                b.setBlock(px, 3, pz, Material.IRON_BARS);
            }
        }

        // ─── 7. Ceiling amethyst chandelier ──────────────────────
        b.setBlock(0, 15, 0, AMT);
        for (int d = 1; d <= 4; d++) {
            b.setBlock(d, 15, 0, AMT);
            b.setBlock(-d, 15, 0, AMT);
            b.setBlock(0, 15, d, AMT);
            b.setBlock(0, 15, -d, AMT);
        }
        b.setBlock(0, 14, 0, AMC);
        b.setBlock(2, 14, 0, AMC);
        b.setBlock(-2, 14, 0, AMC);
        b.setBlock(0, 14, 2, AMC);
        b.setBlock(0, 14, -2, AMC);
        b.setBlock(0, 13, 0, Material.GLOWSTONE);

        // ─── 8. Hidden entrance from above ───────────────────────
        b.fillBox(0, 17, 0, 0, 20, 0, Material.AIR);
        b.setBlock(0, 17, 0, Material.TRAPPED_CHEST); // decoy
        for (int y = 1; y <= 16; y++) b.setBlock(-1, y, 0, Material.LADDER);

        // ─── 9. Treasure chests ─────────────────────────────────
        b.placeChest( 4, 2,  4);
        b.placeChest(-4, 2,  4);
        b.placeChest( 4, 2, -4);
        b.placeChest(-4, 2, -4);
        b.placeChest( 8, 2,  0);
        b.placeChest(-8, 2,  0);
        b.placeChest( 0, 2,  8);
        b.placeChest( 0, 2, -8);
    }

    /** A pillar with a carved dragon head at the top. */
    private void dragonHeadPillar(StructureBuilder b, int x, int z) {
        b.pillar(x, 1, 12, z, OB);
        b.pillar(x + 1, 1, 12, z, OB);
        b.pillar(x, 1, 12, z + 1, OB);
        b.pillar(x + 1, 1, 12, z + 1, OB);
        // Head block at top
        b.setBlock(x, 13, z, Material.DRAGON_HEAD);
        b.setBlock(x + 1, 13, z, Material.DRAGON_HEAD);
        // Capital ring
        b.setBlock(x - 1, 12, z, ES);
        b.setBlock(x + 2, 12, z, ES);
        b.setBlock(x, 12, z - 1, ES);
        b.setBlock(x, 12, z + 2, ES);
        // Gold bands
        b.setBlock(x, 6, z, GLD);
        b.setBlock(x + 1, 6, z, GLD);
    }

    /** A heaped pile of mixed gold &amp; gems. */
    private void plunderPile(StructureBuilder b, int x, int y, int z) {
        b.setBlock(x, y, z, GLD);
        b.setBlock(x + 1, y, z, GLD);
        b.setBlock(x - 1, y, z, GLD);
        b.setBlock(x, y, z + 1, GLD);
        b.setBlock(x, y, z - 1, GLD);
        b.setBlock(x, y + 1, z, EMR);
        b.setBlock(x + 1, y + 1, z, DIA);
        b.setBlock(x - 1, y + 1, z, LZ);
        b.setBlock(x, y + 1, z + 1, DIA);
        b.setBlock(x, y + 2, z, GLD);
    }
}

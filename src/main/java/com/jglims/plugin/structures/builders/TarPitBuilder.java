package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;

/**
 * TarPitBuilder — a vast sticky prehistoric tar pit with the skeletons of
 * multiple trapped dinosaurs half-submerged, a dead tree ring, and a
 * primitive paleontologist's research camp on the north edge complete with
 * tent, campfire, derrick and supply crates.
 */
public final class TarPitBuilder implements IStructureBuilder {

    private static final Material DT = Material.DIRT;
    private static final Material CD = Material.COARSE_DIRT;
    private static final Material BS = Material.BLACK_CONCRETE;  // the "tar"
    private static final Material SS = Material.SOUL_SAND;
    private static final Material BN = Material.BONE_BLOCK;

    @Override
    public void build(StructureBuilder b) {
        // ─── 1. Sunken circular pit — 22 radius, 4 deep ─────────
        b.filledCircle(0, -4, 0, 22, DT);
        for (int y = -3; y <= 0; y++) {
            double r = 22 - (y + 4) * 0.5;
            b.filledCircle(0, y, 0, (int) r, y == 0 ? BS : CD);
        }
        // Tar surface (black concrete) — intentionally at y=0
        b.filledCircle(0, 0, 0, 20, BS);
        // Soul-sand sticky rings around the tar for "bubbling" texture
        for (int i = 0; i < 30; i++) {
            int px = -18 + (int) (Math.random() * 37);
            int pz = -18 + (int) (Math.random() * 37);
            if (px * px + pz * pz > 380) continue;
            b.setBlock(px, 0, pz, SS);
        }
        // Occasional magma cracks (hot tar vents)
        b.scatter(-20, 0, -20, 20, 0, 20, BS, Material.MAGMA_BLOCK, 0.04);

        // ─── 2. Trapped dinosaur skeletons ───────────────────────
        // Giant theropod rising out of the center
        trappedTheropod(b, 0, 0);
        // Smaller sauropod on the east
        trappedSauropod(b, 12, 0);
        // A raptor pile on the west
        raptorPile(b, -12, 0);
        // A pterosaur wing emerging from the tar on the south
        wingSnag(b, 0, 12);

        // ─── 3. Dead tree ring ──────────────────────────────────
        for (int a = 0; a < 360; a += 45) {
            int tx = (int) Math.round(25 * Math.cos(Math.toRadians(a)));
            int tz = (int) Math.round(25 * Math.sin(Math.toRadians(a)));
            deadTree(b, tx, tz);
        }

        // ─── 4. Research camp on the north edge ─────────────────
        // Ground pad
        b.fillFloor(-8, -34, 8, -23, 1, Material.COARSE_DIRT);
        // Main tent
        b.fillBox(-4, 2, -30, 4, 2, -25, Material.RED_WOOL);
        b.fillBox(-4, 3, -30, -4, 4, -25, Material.RED_WOOL);
        b.fillBox( 4, 3, -30,  4, 4, -25, Material.RED_WOOL);
        for (int x = -3; x <= 3; x++)
            b.setBlock(x, 5, -27, Material.RED_WOOL);
        // Campfire
        b.setBlock( 0, 2, -23, Material.CAMPFIRE);
        b.setBlock( 1, 2, -22, Material.LANTERN);
        b.setBlock(-1, 2, -22, Material.LANTERN);
        // Derrick — wooden A-frame with a hoist
        b.pillar( 6, 2, 8, -28, Material.OAK_LOG);
        b.pillar(-6, 2, 8, -28, Material.OAK_LOG);
        b.setBlock( 0, 8, -28, Material.OAK_LOG);
        b.setBlock(-5, 7, -28, Material.OAK_LOG);
        b.setBlock(-4, 8, -28, Material.OAK_LOG);
        b.setBlock(-3, 8, -28, Material.OAK_LOG);
        b.setBlock(-1, 8, -28, Material.OAK_LOG);
        b.setBlock( 1, 8, -28, Material.OAK_LOG);
        b.setBlock( 3, 8, -28, Material.OAK_LOG);
        b.setBlock( 4, 8, -28, Material.OAK_LOG);
        b.setBlock( 5, 7, -28, Material.OAK_LOG);
        // Hoist rope with iron hook
        for (int y = 3; y <= 7; y++) b.setBlock(0, y, -28, Material.IRON_BARS);
        b.setBlock(0, 2, -28, Material.IRON_BARS);
        // Supply crates
        b.placeChest(-3, 2, -25);
        b.placeChest( 3, 2, -25);
        b.placeChest( 2, 2, -29);
        b.placeChest(-2, 2, -29);
        // Paleontologist props
        b.setBlock( 0, 2, -24, Material.CRAFTING_TABLE);
        b.setBlock( 2, 2, -24, Material.BARREL);
        b.setBlock(-2, 2, -24, Material.BARREL);
        b.setBlock( 1, 2, -24, Material.CARTOGRAPHY_TABLE);
        // Access path (coarse dirt) leading from camp to pit edge
        for (int z = -22; z <= -1; z++) {
            b.setBlock(0, 1, z, Material.COARSE_DIRT);
        }

        // ─── 5. Warning signs around the pit ─────────────────────
        for (int a = 0; a < 360; a += 45) {
            int sx = (int) Math.round(22 * Math.cos(Math.toRadians(a)));
            int sz = (int) Math.round(22 * Math.sin(Math.toRadians(a)));
            b.setBlock(sx, 1, sz, Material.OAK_FENCE);
            b.setBlock(sx, 2, sz, Material.OAK_WALL_SIGN);
        }

        // ─── 6. Ambient bones strewn around the pit edge ─────────
        for (int i = 0; i < 25; i++) {
            int bx = -24 + (int) (Math.random() * 49);
            int bz = -24 + (int) (Math.random() * 49);
            double d = Math.sqrt(bx * bx + bz * bz);
            if (d < 22 || d > 24) continue;
            b.setBlock(bx, 1, bz, BN);
        }
    }

    /** A giant theropod half-sunk into the tar, head and arms out. */
    private void trappedTheropod(StructureBuilder b, int cx, int cz) {
        // Spine (4 tall) coming out of the tar
        for (int y = 0; y < 5; y++) b.setBlock(cx, y, cz, BN);
        // Ribs arching forward
        b.setBlock(cx - 1, 2, cz, BN);
        b.setBlock(cx + 1, 2, cz, BN);
        b.setBlock(cx - 2, 3, cz, BN);
        b.setBlock(cx + 2, 3, cz, BN);
        b.setBlock(cx - 2, 4, cz, BN);
        b.setBlock(cx + 2, 4, cz, BN);
        // Skull raised in a death-scream
        b.setBlock(cx, 6, cz, BN);
        b.setBlock(cx, 6, cz - 1, BN);
        b.setBlock(cx, 5, cz - 1, BN);
        b.setBlock(cx, 5, cz + 1, BN);
        // Trailing clawed arms
        b.setBlock(cx - 3, 2, cz + 1, BN);
        b.setBlock(cx + 3, 2, cz + 1, BN);
    }

    private void trappedSauropod(StructureBuilder b, int cx, int cz) {
        // Long neck curling up
        b.setBlock(cx, 1, cz, BN);
        b.setBlock(cx, 2, cz, BN);
        b.setBlock(cx, 3, cz, BN);
        b.setBlock(cx + 1, 4, cz, BN);
        b.setBlock(cx + 2, 4, cz, BN);
        b.setBlock(cx + 3, 5, cz, BN);  // skull
        // Sunken ribs visible
        b.setBlock(cx - 2, 1, cz, BN);
        b.setBlock(cx - 1, 1, cz - 1, BN);
        b.setBlock(cx - 1, 1, cz + 1, BN);
    }

    private void raptorPile(StructureBuilder b, int cx, int cz) {
        b.setBlock(cx, 1, cz, BN);
        b.setBlock(cx + 1, 1, cz, BN);
        b.setBlock(cx, 1, cz + 1, BN);
        b.setBlock(cx - 1, 1, cz, BN);
        b.setBlock(cx, 2, cz, BN);
        b.setBlock(cx + 1, 2, cz + 1, Material.SKELETON_SKULL);
    }

    private void wingSnag(StructureBuilder b, int cx, int cz) {
        // A splayed wing skeleton
        for (int i = 0; i < 5; i++) {
            b.setBlock(cx + i, 1 + i / 2, cz - i, BN);
        }
        b.setBlock(cx, 1, cz, BN);
    }

    private void deadTree(StructureBuilder b, int x, int z) {
        int h = 4 + (int) (Math.random() * 4);
        b.pillar(x, 1, h + 1, z, Material.DARK_OAK_LOG);
        b.setBlock(x + 1, h, z, Material.DARK_OAK_LOG);
        b.setBlock(x - 1, h - 1, z, Material.DARK_OAK_LOG);
        b.setBlock(x, h, z + 1, Material.DARK_OAK_LOG);
    }
}

package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;

/**
 * SkeletonDragonLairBuilder — a sprawling bone cathedral built from the
 * remnants of a titan draconic corpse. A full ribcage vault forms the
 * ceiling, a spinal vertebrae path runs through the floor, and a skull
 * arch marks the entrance. Soul-fire braziers flicker in the gloom.
 */
public final class SkeletonDragonLairBuilder implements IStructureBuilder {

    private static final Material BN = Material.BONE_BLOCK;
    private static final Material SS = Material.SOUL_SOIL;
    private static final Material SF = Material.SOUL_FIRE;
    private static final Material SL = Material.SOUL_LANTERN;
    private static final Material DB = Material.DEEPSLATE_BRICKS;

    @Override
    public void build(StructureBuilder b) {
        // ─── 1. Excavated chamber — 30×12×24 ─────────────────────
        b.fillBox(-15, 0, -12, 15, 12, 12, Material.AIR);
        // Floor: soul soil + deepslate bricks
        b.fillFloor(-15, -12, 15, 12, 0, DB);
        b.scatter(-15, 0, -12, 15, 0, 12, DB, SS, 0.35);
        // Outer cave shell (weathered deepslate)
        b.fillWalls(-16, 0, -13, 16, 13, 13, Material.DEEPSLATE);
        b.scatter(-16, 0, -13, 16, 13, 13, Material.DEEPSLATE, Material.COBBLED_DEEPSLATE, 0.40);

        // ─── 2. Parametric ribcage vault — 12 pairs of arching ribs ─
        for (int i = 0; i < 12; i++) {
            int z = -11 + i * 2;
            arcRib(b, z);
        }

        // ─── 3. Central spinal vertebrae running through the floor ──
        for (int i = -10; i <= 10; i++) {
            b.setBlock(0, 1, i, BN);
            if (i % 2 == 0) {
                b.setBlock(-1, 1, i, BN);
                b.setBlock( 1, 1, i, BN);
            }
            if (i % 4 == 0) {
                b.setBlock(-2, 2, i, BN);
                b.setBlock( 2, 2, i, BN);
            }
        }

        // ─── 4. Giant skull arch at the north entrance ───────────
        skullArch(b, -12);

        // ─── 5. Shattered dragon egg at the south altar ──────────
        b.fillBox(-2, 1, 9, 2, 1, 11, Material.POLISHED_BLACKSTONE);
        b.setBlock(0, 2, 10, Material.DRAGON_EGG);
        b.setBlock(-1, 2, 9, Material.CRACKED_NETHER_BRICKS);
        b.setBlock( 1, 2, 9, Material.CRACKED_NETHER_BRICKS);
        // Bone fragments scattered around the egg
        b.setBlock(-3, 1, 10, BN);
        b.setBlock( 3, 1, 10, BN);
        b.setBlock(-2, 1, 11, BN);
        b.setBlock( 2, 1, 11, BN);

        // ─── 6. Soul-fire braziers along the nave ───────────────
        for (int z = -9; z <= 9; z += 3) {
            b.setBlock(-13, 1, z, Material.POLISHED_BLACKSTONE);
            b.setBlock(-13, 2, z, SF);
            b.setBlock( 13, 1, z, Material.POLISHED_BLACKSTONE);
            b.setBlock( 13, 2, z, SF);
        }
        // Overhead soul lanterns hanging from the ribs
        for (int z = -8; z <= 8; z += 4) {
            b.setBlock(0, 9, z, SL);
        }

        // ─── 7. Piles of smaller bones &amp; skulls ──────────────
        for (int i = 0; i < 20; i++) {
            int px = -13 + (int) (Math.random() * 27);
            int pz = -10 + (int) (Math.random() * 21);
            if (Math.abs(px) < 2) continue;
            b.setBlock(px, 1, pz, BN);
            if (Math.random() < 0.3) b.setBlock(px, 2, pz, Material.WITHER_SKELETON_SKULL);
        }

        // ─── 8. Loot chests around the egg and under the skull ──
        b.placeChest(-1, 2, 11);
        b.placeChest( 1, 2, 11);
        b.placeChest( 0, 2, 11);
        b.placeChest(-3, 1, -11);
        b.placeChest( 3, 1, -11);

        // ─── 9. Narrative tomb plaques ──────────────────────────
        b.setBlock(-5, 3, -10, Material.CHISELED_DEEPSLATE);
        b.setBlock( 5, 3, -10, Material.CHISELED_DEEPSLATE);
        b.setBlock(-5, 3,  10, Material.CHISELED_DEEPSLATE);
        b.setBlock( 5, 3,  10, Material.CHISELED_DEEPSLATE);

        b.setBossSpawn(0, 2, 0);
    }

    /** Builds one archig rib pair spanning the chamber — mirrored on both sides. */
    private void arcRib(StructureBuilder b, int z) {
        // Half-arc from wall up to 11 high
        for (int a = 0; a <= 90; a += 6) {
            double rad = Math.toRadians(a);
            int dx = (int) Math.round(14 * Math.cos(rad));
            int dy = (int) Math.round(11 * Math.sin(rad));
            b.setBlock(dx, 1 + dy, z, BN);
            b.setBlock(-dx, 1 + dy, z, BN);
        }
        // Reinforcing vertebra knot at the apex
        b.setBlock(0, 12, z, BN);
    }

    /** Giant carved dragon skull forming the entrance arch. */
    private void skullArch(StructureBuilder b, int zFace) {
        // Skull shell — 9 wide, 8 tall
        for (int dx = -4; dx <= 4; dx++) {
            b.setBlock(dx, 1, zFace, BN);
            b.setBlock(dx, 8, zFace, BN);
        }
        // Side walls
        for (int y = 2; y <= 7; y++) {
            b.setBlock(-4, y, zFace, BN);
            b.setBlock( 4, y, zFace, BN);
        }
        // Upper cheekbones
        b.setBlock(-3, 8, zFace, BN);
        b.setBlock( 3, 8, zFace, BN);
        b.setBlock(-4, 7, zFace, BN);
        b.setBlock( 4, 7, zFace, BN);
        // Eye sockets
        b.setBlock(-2, 5, zFace, Material.SOUL_FIRE);
        b.setBlock( 2, 5, zFace, Material.SOUL_FIRE);
        // Jagged teeth
        for (int dx = -3; dx <= 3; dx += 2) {
            b.setBlock(dx, 2, zFace, BN);
        }
        // Clear the passage through
        for (int y = 3; y <= 6; y++)
            for (int dx = -1; dx <= 1; dx++)
                b.setBlock(dx, y, zFace, Material.AIR);
    }
}

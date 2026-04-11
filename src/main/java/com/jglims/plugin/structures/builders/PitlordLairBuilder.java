package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

/**
 * PitlordLairBuilder — an infernal throne room carved into an underground
 * basalt cavern. Enter via a descending stair, cross a lava moat into a
 * pillared audience hall, face the Pitlord on a blackstone throne flanked
 * by massive chain-suspended fire braziers and wither-skull reliquaries.
 */
public final class PitlordLairBuilder implements IStructureBuilder {

    private static final Material BS  = Material.BLACKSTONE;
    private static final Material PBS = Material.POLISHED_BLACKSTONE;
    private static final Material PBB = Material.POLISHED_BLACKSTONE_BRICKS;
    private static final Material CBB = Material.CRACKED_POLISHED_BLACKSTONE_BRICKS;
    private static final Material CBS = Material.CHISELED_POLISHED_BLACKSTONE;
    private static final Material NB  = Material.NETHER_BRICKS;
    private static final Material RNB = Material.RED_NETHER_BRICKS;
    private static final Material LAV = Material.LAVA;
    private static final Material MAG = Material.MAGMA_BLOCK;
    private static final Material SF  = Material.SOUL_FIRE;
    private static final Material SL  = Material.SOUL_LANTERN;

    @Override
    public void build(StructureBuilder b) {
        // ─── 1. Descending entry staircase from the surface ─────
        for (int step = 0; step < 10; step++) {
            b.setStairs(0, -step, -20 + step, Material.POLISHED_BLACKSTONE_STAIRS,
                    BlockFace.SOUTH, false);
            b.setBlock(-1, -step, -20 + step, PBB);
            b.setBlock( 1, -step, -20 + step, PBB);
            b.setBlock(-2, -step, -20 + step, PBS);
            b.setBlock( 2, -step, -20 + step, PBS);
            // Hanging soul lanterns every 2 steps
            if (step % 2 == 0) {
                b.setBlock(-2, -step + 2, -20 + step, SL);
                b.setBlock( 2, -step + 2, -20 + step, SL);
            }
        }

        // ─── 2. Main audience hall — 20×8×28 carved out ─────────
        b.fillBox(-10, -10, -12, 10, -3, 14, Material.AIR);
        // Walls — polished blackstone bricks, heavily scattered
        b.fillWalls(-10, -10, -12, 10, -3, 14, PBB);
        b.scatter(-10, -10, -12, 10, -3, 14, PBB, CBB, 0.40);
        b.scatter(-10, -10, -12, 10, -3, 14, PBB, RNB, 0.10);
        // Floor — polished blackstone with red cracks
        b.fillFloor(-10, -12, 10, 14, -10, PBS);
        b.scatter(-10, -10, -12, 10, -10, 14, PBS, Material.NETHERRACK, 0.15);
        // Ceiling
        b.fillFloor(-10, -12, 10, 14, -3, PBB);
        b.scatter(-10, -3, -12, 10, -3, 14, PBB, NB, 0.20);

        // ─── 3. Lava moat separating entry from throne ──────────
        b.fillBox(-9, -10, -9, 9, -10, -7, LAV);
        b.setBlock(0, -10, -8, PBS);   // island stepping stone
        b.setBlock(0, -10, -9, PBS);

        // Bridge across (break in the moat)
        b.fillBox(-1, -10, -9, 1, -10, -7, PBS);

        // ─── 4. Row of 6 skull pillars flanking the hall ────────
        skullPillar(b, -8,  0);
        skullPillar(b, -8,  6);
        skullPillar(b,  8,  0);
        skullPillar(b,  8,  6);
        skullPillar(b, -8, -4);
        skullPillar(b,  8, -4);

        // ─── 5. Throne at the far end ────────────────────────────
        // Raised dais
        b.fillBox(-3, -10, 11, 3, -10, 13, PBB);
        b.fillBox(-2,  -9, 12, 2, -9, 13, PBS);
        // Throne seat
        b.setStairs(0, -8, 12, Material.POLISHED_BLACKSTONE_STAIRS, BlockFace.SOUTH, false);
        b.setBlock(0, -8, 13, CBS);
        b.setBlock(-1, -8, 13, CBS);
        b.setBlock( 1, -8, 13, CBS);
        // Throne back
        b.fillBox(-1, -7, 13, 1, -4, 13, CBS);
        b.setBlock(0, -3, 13, CBS);
        // Wither skull crown
        b.setBlock(0, -4, 12, Material.WITHER_SKELETON_SKULL);
        // Throne arm-rests with flames
        b.setBlock(-2, -8, 12, CBS);
        b.setBlock( 2, -8, 12, CBS);
        b.setBlock(-2, -7, 12, SF);
        b.setBlock( 2, -7, 12, SF);

        // ─── 6. Huge chain-suspended fire braziers on the ceiling ─
        hangingBrazier(b, -5,  0);
        hangingBrazier(b,  5,  0);
        hangingBrazier(b, -5,  6);
        hangingBrazier(b,  5,  6);

        // ─── 7. Torture pit between the throne and the column rows ─
        b.fillBox(-2, -10, 5, 2, -10, 7, LAV);
        // Iron cage above the pit
        for (int x = -2; x <= 2; x++)
            for (int z = 5; z <= 7; z++) {
                if (x == -2 || x == 2 || z == 5 || z == 7)
                    b.setBlock(x, -9, z, Material.IRON_BARS);
            }
        // Chains hanging down
        b.setBlock( 0, -4, 6, Material.IRON_BARS);
        b.setBlock( 0, -5, 6, Material.IRON_BARS);
        b.setBlock( 0, -6, 6, Material.IRON_BARS);
        b.setBlock( 0, -7, 6, Material.IRON_BARS);

        // ─── 8. Reliquary alcoves along the side walls ──────────
        reliquary(b, -9, -1);
        reliquary(b, -9,  8);
        reliquary(b,  9, -1);
        reliquary(b,  9,  8);

        // ─── 9. Loot chests near the throne and in reliquaries ──
        b.placeChest(-2, -9, 13);
        b.placeChest( 2, -9, 13);
        b.placeChest(-9, -9, -1);
        b.placeChest( 9, -9, -1);
        b.placeChest(-9, -9,  8);
        b.placeChest( 9, -9,  8);

        b.setBossSpawn(0, -8, 12);
    }

    private void skullPillar(StructureBuilder b, int x, int z) {
        for (int y = -9; y <= -4; y++) {
            b.setBlock(x, y, z, y == -9 ? PBB : PBS);
        }
        b.setBlock(x, -3, z, CBS);
        b.setBlock(x, -4, z, Material.WITHER_SKELETON_SKULL);
        // Flanking flames
        b.setBlock(x, -8, z + 1, SF);
        b.setBlock(x, -8, z - 1, SF);
    }

    private void hangingBrazier(StructureBuilder b, int x, int z) {
        // Ceiling anchor
        b.setBlock(x, -3, z, NB);
        // Chain
        b.setBlock(x, -4, z, Material.IRON_BARS);
        b.setBlock(x, -5, z, Material.IRON_BARS);
        b.setBlock(x, -6, z, Material.IRON_BARS);
        // Brazier bowl
        b.setBlock(x, -7, z, PBS);
        b.setBlock(x - 1, -7, z, PBS);
        b.setBlock(x + 1, -7, z, PBS);
        b.setBlock(x, -7, z - 1, PBS);
        b.setBlock(x, -7, z + 1, PBS);
        b.setBlock(x, -6, z, SF);
    }

    private void reliquary(StructureBuilder b, int x, int z) {
        // Alcove cut into the wall
        b.setBlock(x, -8, z, Material.AIR);
        b.setBlock(x, -7, z, Material.AIR);
        // Pedestal + skull
        b.setBlock(x, -8, z, PBS);
        b.setBlock(x, -7, z, Material.WITHER_SKELETON_SKULL);
        // Soul lantern above
        b.setBlock(x, -5, z, SL);
    }
}

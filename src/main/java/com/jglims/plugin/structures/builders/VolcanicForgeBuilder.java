package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

/**
 * VolcanicForgeBuilder — an active primordial smithy. Four cascading lava
 * falls pour from the basalt ceiling into a central quenching pool. A
 * massive anvil altar sits above the pool on a polished blackstone dais,
 * flanked by 6 smelter towers and rows of weapon display racks. The floor
 * is cracked polished blackstone with magma block cracks.
 */
public final class VolcanicForgeBuilder implements IStructureBuilder {

    private static final Material BS  = Material.BLACKSTONE;
    private static final Material PBS = Material.POLISHED_BLACKSTONE;
    private static final Material PBB = Material.POLISHED_BLACKSTONE_BRICKS;
    private static final Material CPB = Material.CRACKED_POLISHED_BLACKSTONE_BRICKS;
    private static final Material BSL = Material.BASALT;
    private static final Material BSLP= Material.POLISHED_BASALT;
    private static final Material MAG = Material.MAGMA_BLOCK;
    private static final Material LAV = Material.LAVA;
    private static final Material NETR= Material.NETHERITE_BLOCK;
    private static final Material CFE = Material.COPPER_BLOCK;
    private static final Material CCH = Material.IRON_BARS;

    @Override
    public void build(StructureBuilder b) {
        // ─── 1. Cavernous chamber — 30×14×30 ─────────────────────
        b.fillBox(-15, 0, -15, 15, 14, 15, Material.AIR);
        // Walls (basalt shell)
        b.fillWalls(-15, 0, -15, 15, 15, 15, BSL);
        b.scatter(-15, 0, -15, 15, 15, 15, BSL, BSLP, 0.30);
        b.scatter(-15, 0, -15, 15, 15, 15, BSL, MAG, 0.10);

        // Floor — cracked polished blackstone with magma cracks
        b.fillFloor(-15, -15, 15, 15, 0, PBB);
        b.scatter(-15, 0, -15, 15, 0, 15, PBB, CPB, 0.35);
        b.scatter(-15, 0, -15, 15, 0, 15, PBB, MAG, 0.12);

        // Ceiling — basalt with hanging dripstone
        b.fillFloor(-15, -15, 15, 15, 15, BSL);
        for (int i = 0; i < 12; i++) {
            int sx = -12 + (int) (Math.random() * 25);
            int sz = -12 + (int) (Math.random() * 25);
            b.setBlock(sx, 14, sz, Material.POINTED_DRIPSTONE);
        }

        // ─── 2. Central quench pool + anvil altar ────────────────
        // Deep pool ring
        b.fillBox(-4, 0, -4, 4, 0, 4, LAV);
        b.fillBox(-3, 0, -3, 3, 0, 3, Material.WATER); // center quench water
        // Rim around pool
        for (int a = 0; a < 360; a += 10) {
            int rx = (int) Math.round(5 * Math.cos(Math.toRadians(a)));
            int rz = (int) Math.round(5 * Math.sin(Math.toRadians(a)));
            b.setBlock(rx, 1, rz, PBS);
        }
        // Anvil dais rising from the pool
        b.pillar(0, 1, 3, 0, PBS);
        b.setBlock(0, 4, 0, Material.ANVIL);
        b.setBlock(-1, 4, 0, Material.ANVIL);
        b.setBlock( 1, 4, 0, Material.ANVIL);

        // ─── 3. Four lava fall cascades in the cardinals ────────
        lavaFall(b,  13,  0);
        lavaFall(b, -13,  0);
        lavaFall(b,  0,  13);
        lavaFall(b,  0, -13);

        // ─── 4. 6 smelter towers ringing the pool ───────────────
        smelterTower(b,  9,  9);
        smelterTower(b, -9,  9);
        smelterTower(b,  9, -9);
        smelterTower(b, -9, -9);
        smelterTower(b, 12,  0);
        smelterTower(b,-12,  0);

        // ─── 5. Weapon display racks along the walls ────────────
        weaponRack(b, -13, -8);
        weaponRack(b, -13,  0);
        weaponRack(b, -13,  8);
        weaponRack(b,  13, -8);
        weaponRack(b,  13,  0);
        weaponRack(b,  13,  8);

        // ─── 6. Hanging chain bridges crossing the chamber ────
        for (int x = -6; x <= 6; x++) {
            b.setBlock(x, 10, -10, CCH);
            b.setBlock(x, 10,  10, CCH);
        }
        for (int z = -6; z <= 6; z++) {
            b.setBlock(-10, 10, z, CCH);
            b.setBlock( 10, 10, z, CCH);
        }

        // ─── 7. Heat shimmer / torches for ambient light ────────
        for (int x = -12; x <= 12; x += 4) {
            b.setBlock(x, 2, -14, Material.SOUL_LANTERN);
            b.setBlock(x, 2,  14, Material.SOUL_LANTERN);
        }
        for (int z = -12; z <= 12; z += 4) {
            b.setBlock(-14, 2, z, Material.SOUL_LANTERN);
            b.setBlock( 14, 2, z, Material.SOUL_LANTERN);
        }

        // ─── 8. Raw material piles ──────────────────────────────
        b.fillBox(-14, 1, -14, -12, 2, -12, Material.RAW_IRON_BLOCK);
        b.fillBox( 12, 1,  12,  14, 2,  14, Material.RAW_COPPER_BLOCK);
        b.fillBox(-14, 1,  12, -12, 2,  14, Material.COAL_BLOCK);
        b.fillBox( 12, 1, -14,  14, 2, -12, NETR);

        // ─── 9. Loot — chests at each corner ───────────────────
        b.placeChest(-13, 1, -13);
        b.placeChest( 13, 1, -13);
        b.placeChest(-13, 1,  13);
        b.placeChest( 13, 1,  13);
        b.placeChest(  0, 4,  2);

        b.setBossSpawn(0, 5, 0);
    }

    /** A 14-tall lava cascade pouring from the ceiling into the pool. */
    private void lavaFall(StructureBuilder b, int x, int z) {
        // Basin for the cascade
        b.setBlock(x, 13, z, LAV);
        b.setBlock(x, 12, z, LAV);
        // Stepped flow down
        for (int y = 11; y >= 2; y--) {
            b.setBlock(x, y, z, LAV);
        }
        // Magma floor catcher
        b.setBlock(x, 1, z, MAG);
        // Splash cone around base
        b.setBlock(x + 1, 1, z, PBS);
        b.setBlock(x - 1, 1, z, PBS);
        b.setBlock(x, 1, z + 1, PBS);
        b.setBlock(x, 1, z - 1, PBS);
    }

    /** A 5-block-tall smelter tower with a blast furnace and copper cap. */
    private void smelterTower(StructureBuilder b, int x, int z) {
        b.pillar(x, 1, 5, z, BS);
        b.setBlock(x, 2, z, Material.BLAST_FURNACE);
        b.setBlock(x, 3, z, BS);
        b.setBlock(x, 4, z, CFE);
        b.setBlock(x, 5, z, Material.LIGHTNING_ROD);
        // Hot iron chunks around the base
        b.setBlock(x + 1, 1, z, Material.RAW_IRON_BLOCK);
        b.setBlock(x - 1, 1, z, Material.COAL_BLOCK);
    }

    /** A weapon display rack against the wall. */
    private void weaponRack(StructureBuilder b, int x, int z) {
        b.setBlock(x, 1, z, PBS);
        b.setBlock(x, 2, z, Material.ARMOR_STAND);
        b.setBlock(x, 3, z, BS);
    }
}

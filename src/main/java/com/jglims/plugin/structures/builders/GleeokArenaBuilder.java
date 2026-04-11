package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

/**
 * GleeokArenaBuilder — a sunken terraced bowl arena where the three-headed Gleeok
 * manifests. Features three elemental head-pillars (fire, frost, storm), a
 * central purpur altar, a triple gothic-arch entry, and spectator boss platforms.
 *
 * <p>Extracted verbatim from the original {@code DimensionStructureBuilders.buildGleeokArena}
 * method to give the structure its own file per the one-class-per-structure
 * architecture.
 */
public final class GleeokArenaBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // ─── Sunken arena bowl: floor at y=0 with terraced rim rising to y=24 ───
        // Outer foundation ring (heavy structural mass)
        for (int y = -3; y <= 0; y++) b.filledCircle(0, y, 0, 40, Material.END_STONE);
        // Polished stepped tier (6 tiers of seating climbing the bowl wall)
        for (int tier = 0; tier < 6; tier++) {
            int r = 38 - tier * 2;
            int yBase = tier * 3;
            // Tier face
            for (int yy = 0; yy < 3; yy++) b.circle(0, yBase + yy, 0, r, Material.PURPUR_BLOCK);
            // Tier walkway behind
            for (int a = 0; a < 360; a += 2) {
                int wx = (int) Math.round((r - 1) * Math.cos(Math.toRadians(a)));
                int wz = (int) Math.round((r - 1) * Math.sin(Math.toRadians(a)));
                b.setBlock(wx, yBase + 2, wz, Material.PURPUR_PILLAR);
            }
            // Stairs leading up the tier (used as seats, looking inward)
            for (int a = 0; a < 360; a += 18) {
                int sx = (int) Math.round(r * Math.cos(Math.toRadians(a)));
                int sz = (int) Math.round(r * Math.sin(Math.toRadians(a)));
                org.bukkit.block.BlockFace facing = (Math.abs(sx) > Math.abs(sz))
                        ? (sx > 0 ? org.bukkit.block.BlockFace.WEST : org.bukkit.block.BlockFace.EAST)
                        : (sz > 0 ? org.bukkit.block.BlockFace.NORTH : org.bukkit.block.BlockFace.SOUTH);
                b.setStairs(sx, yBase + 2, sz, Material.PURPUR_STAIRS, facing, false);
            }
        }

        // ─── Arena floor (sunken to y=1, with elemental rune inlay) ───
        b.filledCircle(0, 1, 0, 24, Material.END_STONE_BRICKS);
        // Three concentric rings of element-coded stone
        for (int a = 0; a < 360; a += 1) {
            int rx = (int) Math.round(22 * Math.cos(Math.toRadians(a)));
            int rz = (int) Math.round(22 * Math.sin(Math.toRadians(a)));
            b.setBlock(rx, 1, rz, Material.OBSIDIAN);
        }
        // Three triangular sigils inscribed in floor (one per head element)
        // Fire sigil (north): magma + crimson lines
        for (int i = -8; i <= 8; i++) {
            b.setBlock(i, 1, -16 + Math.abs(i), Material.MAGMA_BLOCK);
            b.setBlock(i, 1, -14 + Math.abs(i), Material.NETHERRACK);
        }
        // Frost sigil (southwest): blue ice + packed ice lines
        for (int i = -8; i <= 8; i++) {
            int dx = (int) (i * 0.5);
            int dz = (int) (i * 0.866);
            b.setBlock(13 + dx + Math.abs(i)/3, 1, 8 + dz - Math.abs(i)/3, Material.BLUE_ICE);
            b.setBlock(11 + dx + Math.abs(i)/3, 1, 6 + dz - Math.abs(i)/3, Material.PACKED_ICE);
        }
        // Storm sigil (southeast): lightning rod + amethyst
        for (int i = -8; i <= 8; i++) {
            int dx = (int) (i * 0.5);
            int dz = (int) (i * 0.866);
            b.setBlock(-13 - dx - Math.abs(i)/3, 1, 8 + dz - Math.abs(i)/3, Material.AMETHYST_BLOCK);
        }

        // Central altar: three-stepped purpur pyramid where Gleeok manifests
        b.filledCircle(0, 2, 0, 6, Material.PURPUR_BLOCK);
        b.filledCircle(0, 3, 0, 4, Material.PURPUR_PILLAR);
        b.filledCircle(0, 4, 0, 2, Material.OBSIDIAN);
        b.setBlock(0, 5, 0, Material.DRAGON_HEAD);
        // End-rod ring around altar
        for (int a = 0; a < 360; a += 30) {
            int rx = (int) Math.round(7 * Math.cos(Math.toRadians(a)));
            int rz = (int) Math.round(7 * Math.sin(Math.toRadians(a)));
            b.setBlock(rx, 2, rz, Material.END_ROD);
        }

        // ─── Three head-pillars (one per head: Fire, Frost, Storm) ───
        Material[] headBody = {Material.MAGMA_BLOCK, Material.BLUE_ICE, Material.AMETHYST_BLOCK};
        Material[] headBrick = {Material.NETHER_BRICKS, Material.PACKED_ICE, Material.PURPUR_PILLAR};
        Material[] headEye = {Material.SHROOMLIGHT, Material.SOUL_LANTERN, Material.SEA_LANTERN};
        for (int i = 0; i < 3; i++) {
            int a = i * 120 + 90;  // start at north
            int px = (int) Math.round(20 * Math.cos(Math.toRadians(a)));
            int pz = (int) Math.round(20 * Math.sin(Math.toRadians(a)));
            // Tapered base (3-block wide → 5-block wide)
            for (int yy = 1; yy <= 4; yy++) b.fillBox(px - 2, yy, pz - 2, px + 2, yy, pz + 2, headBrick[i]);
            for (int yy = 5; yy <= 12; yy++) b.fillBox(px - 1, yy, pz - 1, px + 1, yy, pz + 1, headBrick[i]);
            // Element core
            b.fillBox(px - 1, 5, pz - 1, px + 1, 11, pz + 1, headBody[i]);
            // Crystal head shape on top (a stylized dragon skull)
            b.fillBox(px - 3, 13, pz - 3, px + 3, 16, pz + 3, headBrick[i]);
            b.fillBox(px - 2, 14, pz - 2, px + 2, 15, pz + 2, headBody[i]);
            // Jaw protrusion
            b.fillBox(px - 2, 12, pz - 2, px + 2, 13, pz + 2, headBody[i]);
            // Eyes (two)
            b.setBlock(px - 1, 15, pz, headEye[i]);
            b.setBlock(px + 1, 15, pz, headEye[i]);
            // Forehead horn / crest
            b.setBlock(px, 17, pz, headEye[i]);
            b.setBlock(px - 2, 17, pz, headBody[i]);
            b.setBlock(px + 2, 17, pz, headBody[i]);
            // Element flare beam shooting up
            for (int yy = 18; yy <= 22; yy++) b.setBlock(px, yy, pz, headEye[i]);
        }

        // ─── Cover pillars: 6 destructible inner columns at varying heights ───
        for (int i = 0; i < 6; i++) {
            int a = i * 60 + 30;
            int px = (int) Math.round(13 * Math.cos(Math.toRadians(a)));
            int pz = (int) Math.round(13 * Math.sin(Math.toRadians(a)));
            int height = 5 + (i % 3) * 2;
            b.pillar(px, 2, 2 + height, pz, Material.PURPUR_PILLAR);
            b.setBlock(px, 3 + height, pz, Material.PURPUR_SLAB);
        }

        // ─── Grand triple-arch entrance (south, three gothic arches for the three heads) ───
        b.fillBox(-12, 1, -42, 12, 1, -38, Material.END_STONE_BRICKS);
        b.gothicArch(-7, 1, -40, 6, 8, Material.PURPUR_PILLAR);
        b.gothicArch(0, 1, -40, 8, 12, Material.PURPUR_PILLAR);
        b.gothicArch(7, 1, -40, 6, 8, Material.PURPUR_PILLAR);
        // Clear the arch interiors
        for (int x = -10; x <= 10; x++)
            for (int y = 2; y <= 11; y++)
                if (Math.abs(x) <= 3 || (Math.abs(x) >= 5 && Math.abs(x) <= 8))
                    b.setBlock(x, y, -40, Material.AIR);
        // Approach causeway with end rods
        for (int z = -41; z <= -25; z++) {
            b.setBlock(-3, 1, z, Material.PURPUR_PILLAR);
            b.setBlock(3, 1, z, Material.PURPUR_PILLAR);
            if (z % 4 == 0) {
                b.setBlock(-3, 2, z, Material.END_ROD);
                b.setBlock(3, 2, z, Material.END_ROD);
            }
        }

        // ─── Suspended skylight: dome of end-rod stars overhead ───
        for (int i = 0; i < 24; i++) {
            int a = i * 15;
            int sx = (int) Math.round(28 * Math.cos(Math.toRadians(a)));
            int sz = (int) Math.round(28 * Math.sin(Math.toRadians(a)));
            b.setBlock(sx, 28, sz, Material.END_ROD);
        }
        // Central skylight chandelier
        b.chandelier(0, 30, 0, 6);
        for (int dx = -2; dx <= 2; dx++)
            for (int dz = -2; dz <= 2; dz++)
                if (dx*dx + dz*dz <= 4) b.setBlock(dx, 30, dz, Material.PURPUR_BLOCK);

        // Spectator boss observation platforms above the heads
        for (int i = 0; i < 3; i++) {
            int a = i * 120 + 90;
            int px = (int) Math.round(34 * Math.cos(Math.toRadians(a)));
            int pz = (int) Math.round(34 * Math.sin(Math.toRadians(a)));
            b.fillBox(px - 3, 18, pz - 3, px + 3, 18, pz + 3, Material.PURPUR_BLOCK);
            b.fillBox(px - 3, 19, pz - 3, px + 3, 21, pz + 3, Material.AIR);
            b.battlements(px - 3, 19, pz - 3, px + 3, pz - 3, Material.PURPUR_PILLAR);
            b.battlements(px - 3, 19, pz + 3, px + 3, pz + 3, Material.PURPUR_PILLAR);
            b.battlements(px - 3, 19, pz - 3, px - 3, pz + 3, Material.PURPUR_PILLAR);
            b.battlements(px + 3, 19, pz - 3, px + 3, pz + 3, Material.PURPUR_PILLAR);
            b.placeChest(px, 19, pz);
        }

        // Treasure altar (under central altar)
        b.placeChest(0, 1, 0);
        b.placeChest(0, 1, 6);
        b.placeChest(0, 1, -6);

        b.setBossSpawn(0, 5, 0);
    }
}

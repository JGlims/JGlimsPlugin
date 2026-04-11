package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

/**
 * VoidShrineBuilder — a sacred floating End sanctuary with a central purpur-domed
 * temple, four satellite platforms (chorus garden, scrying pool, prayer hall,
 * meditation cell), connected by arched bridges under a hanging end-rod sky.
 *
 * <p>Extracted verbatim from the original {@code DimensionStructureBuilders.buildVoidShrine}
 * method to give the structure its own file per the one-class-per-structure
 * architecture.
 */
public final class VoidShrineBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // ─── Void Shrine: a sacred floating sanctuary in the End ───
        // Central temple under a purpur dome, 4 floating sub-platforms connected
        // by elegant arched bridges, chorus garden, scrying pool, void prayer hall.

        // ─── Central temple platform: stepped octagonal base ───
        b.filledCircle(0, -1, 0, 14, Material.END_STONE);
        b.filledCircle(0, 0, 0, 14, Material.END_STONE_BRICKS);
        b.filledCircle(0, 1, 0, 12, Material.PURPUR_BLOCK);
        b.filledCircle(0, 2, 0, 10, Material.PURPUR_PILLAR);
        // Decorative ring of inlay
        for (int a = 0; a < 360; a += 1) {
            int rx = (int) Math.round(11 * Math.cos(Math.toRadians(a)));
            int rz = (int) Math.round(11 * Math.sin(Math.toRadians(a)));
            b.setBlock(rx, 1, rz, Material.PURPUR_PILLAR);
        }
        // Stairs around the rim (cosmetic)
        for (int a = 0; a < 360; a += 6) {
            int sx = (int) Math.round(13 * Math.cos(Math.toRadians(a)));
            int sz = (int) Math.round(13 * Math.sin(Math.toRadians(a)));
            org.bukkit.block.BlockFace facing = (Math.abs(sx) > Math.abs(sz))
                    ? (sx > 0 ? org.bukkit.block.BlockFace.WEST : org.bukkit.block.BlockFace.EAST)
                    : (sz > 0 ? org.bukkit.block.BlockFace.NORTH : org.bukkit.block.BlockFace.SOUTH);
            b.setStairs(sx, 1, sz, Material.PURPUR_STAIRS, facing, false);
        }

        // ─── 8 Pillar ring inside the temple holding up the dome ───
        for (int i = 0; i < 8; i++) {
            int a = i * 45;
            int px = (int) Math.round(9 * Math.cos(Math.toRadians(a)));
            int pz = (int) Math.round(9 * Math.sin(Math.toRadians(a)));
            // Tapered base
            b.fillBox(px - 1, 2, pz - 1, px + 1, 3, pz + 1, Material.PURPUR_PILLAR);
            // Main pillar
            b.pillar(px, 4, 14, pz, Material.PURPUR_PILLAR);
            // Capital
            b.fillBox(px - 1, 14, pz - 1, px + 1, 14, pz + 1, Material.PURPUR_PILLAR);
            b.setBlock(px, 15, pz, Material.END_ROD);
        }

        // ─── Grand purpur dome over the central temple ───
        b.dome(0, 16, 0, 12, Material.PURPUR_BLOCK);
        // Inner dome lining for richer texture
        for (int yy = 0; yy <= 8; yy++) {
            double r = Math.sqrt(100 - yy * yy);
            for (int a = 0; a < 360; a += 15) {
                int dx = (int) Math.round(r * 0.9 * Math.cos(Math.toRadians(a)));
                int dz = (int) Math.round(r * 0.9 * Math.sin(Math.toRadians(a)));
                b.setBlock(dx, 16 + yy, dz, Material.PURPUR_PILLAR);
            }
        }
        // Oculus at the dome's apex
        b.setBlock(0, 26, 0, Material.AIR);
        b.setBlock(0, 25, 0, Material.END_ROD);
        // Hanging end-rod chandeliers
        b.chandelier(0, 24, 0, 6);
        b.chandelier(-5, 22, 0, 4);
        b.chandelier(5, 22, 0, 4);
        b.chandelier(0, 22, -5, 4);
        b.chandelier(0, 22, 5, 4);

        // ─── Central altar: stepped obsidian throne with end gateway ───
        b.fillBox(-3, 3, -3, 3, 3, 3, Material.OBSIDIAN);
        b.fillBox(-2, 4, -2, 2, 4, 2, Material.CRYING_OBSIDIAN);
        b.fillBox(-1, 5, -1, 1, 5, 1, Material.OBSIDIAN);
        b.setBlock(0, 6, 0, Material.END_GATEWAY);
        b.setBlock(0, 7, 0, Material.END_ROD);
        // Altar candles
        b.setBlock(-2, 4, -2, Material.SOUL_LANTERN);
        b.setBlock(2, 4, -2, Material.SOUL_LANTERN);
        b.setBlock(-2, 4, 2, Material.SOUL_LANTERN);
        b.setBlock(2, 4, 2, Material.SOUL_LANTERN);
        // Ender chest as the holy reliquary
        b.setBlock(0, 4, 3, Material.ENDER_CHEST);

        // ─── 4 satellite floating platforms with themed purposes ───

        // North: Chorus garden (a sacred grove)
        b.filledCircle(0, 0, -28, 7, Material.END_STONE_BRICKS);
        b.filledCircle(0, 1, -28, 6, Material.END_STONE);
        b.filledCircle(0, 2, -28, 5, Material.AIR);
        for (int x = -4; x <= 4; x += 2)
            for (int z = -32; z <= -24; z += 2) {
                if ((x + z) % 4 == 0) {
                    b.setBlock(x, 2, z, Material.CHORUS_PLANT);
                    b.setBlock(x, 3, z, Material.CHORUS_PLANT);
                    b.setBlock(x, 4, z, Material.CHORUS_FLOWER);
                }
            }
        // Garden lanterns
        b.setBlock(-5, 2, -28, Material.END_ROD);
        b.setBlock(5, 2, -28, Material.END_ROD);
        b.setBlock(0, 2, -33, Material.END_ROD);
        b.placeChest(0, 2, -28);

        // South: Scrying pool (water for divination)
        b.filledCircle(0, 0, 28, 7, Material.END_STONE_BRICKS);
        b.filledCircle(0, 1, 28, 6, Material.END_STONE);
        b.filledCircle(0, 2, 28, 4, Material.WATER);
        b.setBlock(0, 2, 28, Material.SEA_LANTERN);  // glowing center
        // Pool ring with prismarine
        for (int a = 0; a < 360; a += 30) {
            int px = (int) Math.round(5 * Math.cos(Math.toRadians(a)));
            int pz = 28 + (int) Math.round(5 * Math.sin(Math.toRadians(a)));
            b.setBlock(px, 2, pz, Material.PRISMARINE_BRICKS);
            if (a % 60 == 0) b.setBlock(px, 3, pz, Material.END_ROD);
        }
        b.placeChest(0, 2, 32);

        // East: Prayer hall (rows of pews facing the center)
        b.filledCircle(28, 0, 0, 7, Material.END_STONE_BRICKS);
        b.filledCircle(28, 1, 0, 6, Material.END_STONE);
        b.fillBox(24, 2, -5, 32, 7, 5, Material.END_STONE_BRICKS);
        b.fillBox(25, 2, -4, 31, 6, 4, Material.AIR);
        // Pews (stairs facing west toward central temple)
        for (int z = -3; z <= 3; z += 2) {
            b.setStairs(26, 2, z, Material.PURPUR_STAIRS, BlockFace.EAST, false);
            b.setStairs(27, 2, z, Material.PURPUR_STAIRS, BlockFace.EAST, false);
        }
        b.setBlock(30, 3, 0, Material.LECTERN);
        b.chandelier(28, 6, 0, 2);
        b.placeChest(30, 2, -3);

        // West: Meditation cell with monk supplies
        b.filledCircle(-28, 0, 0, 7, Material.END_STONE_BRICKS);
        b.filledCircle(-28, 1, 0, 6, Material.END_STONE);
        b.fillBox(-32, 2, -5, -24, 7, 5, Material.END_STONE_BRICKS);
        b.fillBox(-31, 2, -4, -25, 6, 4, Material.AIR);
        // Meditation mat in the center
        b.setBlock(-28, 2, 0, Material.PURPLE_CARPET);
        // Bookshelves
        for (int z = -3; z <= 3; z += 2) {
            b.setBlock(-31, 3, z, Material.BOOKSHELF);
            b.setBlock(-31, 4, z, Material.BOOKSHELF);
        }
        b.setBlock(-30, 3, -2, Material.ENCHANTING_TABLE);
        b.chandelier(-28, 6, 0, 2);
        b.placeChest(-30, 2, 3);

        // ─── Arched bridges connecting central temple to each satellite ───
        // North bridge
        for (int z = -14; z >= -22; z--) {
            int t = -14 - z;  // 0..8
            int yArch = 1 + (int) Math.round(Math.sin(t * Math.PI / 8) * 2);
            b.setBlock(-2, yArch, z, Material.PURPUR_BLOCK);
            b.setBlock(-1, yArch, z, Material.PURPUR_BLOCK);
            b.setBlock(0, yArch, z, Material.PURPUR_BLOCK);
            b.setBlock(1, yArch, z, Material.PURPUR_BLOCK);
            b.setBlock(2, yArch, z, Material.PURPUR_BLOCK);
            // Side rails
            if (t % 2 == 0) {
                b.setBlock(-3, yArch + 1, z, Material.PURPUR_PILLAR);
                b.setBlock(3, yArch + 1, z, Material.PURPUR_PILLAR);
            }
        }
        // South bridge (mirrored)
        for (int z = 14; z <= 22; z++) {
            int t = z - 14;
            int yArch = 1 + (int) Math.round(Math.sin(t * Math.PI / 8) * 2);
            for (int dx = -2; dx <= 2; dx++) b.setBlock(dx, yArch, z, Material.PURPUR_BLOCK);
            if (t % 2 == 0) {
                b.setBlock(-3, yArch + 1, z, Material.PURPUR_PILLAR);
                b.setBlock(3, yArch + 1, z, Material.PURPUR_PILLAR);
            }
        }
        // East bridge
        for (int x = 14; x <= 22; x++) {
            int t = x - 14;
            int yArch = 1 + (int) Math.round(Math.sin(t * Math.PI / 8) * 2);
            for (int dz = -2; dz <= 2; dz++) b.setBlock(x, yArch, dz, Material.PURPUR_BLOCK);
            if (t % 2 == 0) {
                b.setBlock(x, yArch + 1, -3, Material.PURPUR_PILLAR);
                b.setBlock(x, yArch + 1, 3, Material.PURPUR_PILLAR);
            }
        }
        // West bridge
        for (int x = -14; x >= -22; x--) {
            int t = -14 - x;
            int yArch = 1 + (int) Math.round(Math.sin(t * Math.PI / 8) * 2);
            for (int dz = -2; dz <= 2; dz++) b.setBlock(x, yArch, dz, Material.PURPUR_BLOCK);
            if (t % 2 == 0) {
                b.setBlock(x, yArch + 1, -3, Material.PURPUR_PILLAR);
                b.setBlock(x, yArch + 1, 3, Material.PURPUR_PILLAR);
            }
        }

        // Floating end rod constellations between platforms
        for (int i = 0; i < 12; i++) {
            int a = i * 30 + 15;
            int sx = (int) Math.round(18 * Math.cos(Math.toRadians(a)));
            int sz = (int) Math.round(18 * Math.sin(Math.toRadians(a)));
            int sy = 6 + (i % 3) * 2;
            b.setBlock(sx, sy, sz, Material.END_ROD);
        }

        b.setBossSpawn(0, 6, 0);
    }
}

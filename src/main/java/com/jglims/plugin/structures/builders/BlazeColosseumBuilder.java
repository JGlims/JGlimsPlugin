package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;

/**
 * BlazeColosseumBuilder — Nether brick arena with tiered magma seating,
 * fire pit center, blaze spawner frames, spectator boxes, trophy room.
 *
 * <p>Extracted verbatim from the original {@code DimensionStructureBuilders.buildBlazeColosseum}
 * method to give the structure its own file per the one-class-per-structure
 * architecture.
 */
public final class BlazeColosseumBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // Arena floor
        b.filledCircle(0, 0, 0, 22, Material.NETHER_BRICKS);
        b.filledCircle(0, 0, 0, 10, Material.BLACKSTONE);

        // Central fire pit
        b.filledCircle(0, 0, 0, 4, Material.MAGMA_BLOCK);
        b.setBlock(0, 1, 0, Material.FIRE);
        b.setBlock(-2, 1, 0, Material.FIRE);
        b.setBlock(2, 1, 0, Material.FIRE);
        b.setBlock(0, 1, -2, Material.FIRE);
        b.setBlock(0, 1, 2, Material.FIRE);

        // Tiered seating (4 tiers)
        for (int tier = 0; tier < 4; tier++) {
            int r = 14 + tier * 2;
            int y = tier + 1;
            b.circle(0, y, 0, r, Material.NETHER_BRICKS);
            // Magma block seats every other position
            for (int a = 0; a < 360; a += 8) {
                int sx = (int) (r * Math.cos(Math.toRadians(a)));
                int sz = (int) (r * Math.sin(Math.toRadians(a)));
                if (a % 16 == 0) b.setBlock(sx, y, sz, Material.MAGMA_BLOCK);
            }
        }

        // Outer wall
        for (int y = 0; y <= 8; y++) b.circle(0, y, 0, 22, Material.RED_NETHER_BRICKS);
        b.battlements(0, 9, 0, 22, 0, Material.NETHER_BRICKS); // ring simplified

        // Blaze spawner frames (6 pillars around arena)
        for (int a = 0; a < 360; a += 60) {
            int px = (int) (12 * Math.cos(Math.toRadians(a)));
            int pz = (int) (12 * Math.sin(Math.toRadians(a)));
            b.pillar(px, 1, 8, pz, Material.NETHER_BRICK_FENCE);
            b.setBlock(px, 9, pz, Material.LAVA);
            b.setBlock(px, 8, pz, Material.MAGMA_BLOCK);
            // Iron bars frame
            b.setBlock(px + 1, 5, pz, Material.IRON_BARS);
            b.setBlock(px - 1, 5, pz, Material.IRON_BARS);
            b.setBlock(px, 5, pz + 1, Material.IRON_BARS);
            b.setBlock(px, 5, pz - 1, Material.IRON_BARS);
        }

        // Spectator boxes (4 elevated platforms)
        int[][] specPos = {{-20, 0}, {20, 0}, {0, -20}, {0, 20}};
        for (int[] sp : specPos) {
            b.fillBox(sp[0] - 3, 5, sp[1] - 3, sp[0] + 3, 5, sp[1] + 3, Material.NETHER_BRICKS);
            b.fillBox(sp[0] - 2, 6, sp[1] - 2, sp[0] + 2, 6, sp[1] + 2, Material.RED_CARPET);
            b.setBlock(sp[0], 8, sp[1], Material.SOUL_LANTERN);
            // Railing
            b.fillBox(sp[0] - 3, 6, sp[1] - 3, sp[0] + 3, 7, sp[1] - 3, Material.NETHER_BRICK_FENCE);
        }

        // Trophy room (underground)
        b.fillBox(-8, -6, -8, 8, -1, 8, Material.RED_NETHER_BRICKS);
        b.fillBox(-7, -5, -7, 7, -1, 7, Material.AIR);
        b.fillFloor(-7, -7, 7, 7, -6, Material.POLISHED_BLACKSTONE);
        // Trophies — armor stands simulated with fences + skulls
        for (int x = -5; x <= 5; x += 5) {
            b.pillar(x, -5, -3, 0, Material.NETHER_BRICK_FENCE);
            b.setBlock(x, -2, 0, Material.WITHER_SKELETON_SKULL);
        }
        b.setBlock(0, -5, -5, Material.SOUL_LANTERN);
        b.setBlock(0, -5, 5, Material.SOUL_LANTERN);

        // Entrance tunnel
        b.fillBox(-2, 0, -22, 2, 4, -14, Material.AIR);
        b.fillWalls(-3, 0, -22, 3, 5, -14, Material.NETHER_BRICKS);

        b.placeChest(0, -5, 0);
        b.placeChest(5, 1, 5);
        b.placeChest(-5, 1, -5);
        b.setBossSpawn(0, 1, 0);
    }
}

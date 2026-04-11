package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;

/**
 * NetherKingsCastleBuilder — MASSIVE 250x250 blackstone fortress with outer walls,
 * towers, inner courtyard, massive throne room arena (40-radius with lava,
 * pillars, raised throne), treasure vaults, grand entrance.
 *
 * <p>Extracted verbatim from the original {@code DimensionStructureBuilders.buildNetherKingsCastle}
 * method to give the structure its own file per the one-class-per-structure
 * architecture.
 */
public final class NetherKingsCastleBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // === OUTER WALLS (perimeter at ~100 blocks from center) ===
        int ow = 100; // outer wall half-size
        b.fillWalls(-ow, 0, -ow, ow, 18, ow, Material.POLISHED_BLACKSTONE_BRICKS);
        // Clear interior of outer walls
        b.fillBox(-ow + 1, 1, -ow + 1, ow - 1, 17, ow - 1, Material.AIR);
        b.fillFloor(-ow, -ow, ow, ow, 0, Material.BLACKSTONE);

        // Outer wall battlements
        b.battlements(-ow, 19, -ow, ow, -ow, Material.POLISHED_BLACKSTONE);
        b.battlements(-ow, 19, ow, ow, ow, Material.POLISHED_BLACKSTONE);
        b.battlements(-ow, 19, -ow, -ow, ow, Material.POLISHED_BLACKSTONE);
        b.battlements(ow, 19, -ow, ow, ow, Material.POLISHED_BLACKSTONE);

        // Outer corner towers (4)
        int[][] outerCorners = {{-ow, -ow}, {ow, -ow}, {-ow, ow}, {ow, ow}};
        for (int[] c : outerCorners) {
            b.roundTower(c[0], 0, c[1], 6, 25, Material.POLISHED_BLACKSTONE_BRICKS,
                    Material.DEEPSLATE_TILES, Material.RED_STAINED_GLASS);
        }

        // Grand entrance gate (south)
        b.fillBox(-6, 1, -ow, 6, 14, -ow, Material.AIR);
        b.gothicArch(0, 1, -ow, 12, 15, Material.GILDED_BLACKSTONE);
        // Flanking towers at gate
        b.roundTower(-10, 0, -ow, 5, 22, Material.NETHER_BRICKS, Material.RED_NETHER_BRICKS,
                Material.ORANGE_STAINED_GLASS);
        b.roundTower(10, 0, -ow, 5, 22, Material.NETHER_BRICKS, Material.RED_NETHER_BRICKS,
                Material.ORANGE_STAINED_GLASS);

        // === INNER KEEP (40x40) ===
        int iw = 40;
        b.fillWalls(-iw, 0, -iw, iw, 25, iw, Material.NETHER_BRICKS);
        b.hollowBox(-iw, 1, -iw, iw, 25, iw);
        b.fillFloor(-iw + 1, -iw + 1, iw - 1, iw - 1, 0, Material.POLISHED_BLACKSTONE);

        // Inner keep towers (4 corners)
        int[][] innerCorners = {{-iw, -iw}, {iw, -iw}, {-iw, iw}, {iw, iw}};
        for (int[] c : innerCorners) {
            b.fillBox(c[0] - 4, 0, c[1] - 4, c[0] + 4, 30, c[1] + 4, Material.POLISHED_BLACKSTONE_BRICKS);
            b.fillBox(c[0] - 3, 1, c[1] - 3, c[0] + 3, 29, c[1] + 3, Material.AIR);
            b.setBlock(c[0], 31, c[1], Material.SOUL_LANTERN);
        }

        // Inner keep entrance
        b.fillBox(-4, 1, -iw, 4, 10, -iw, Material.AIR);
        b.gothicArch(0, 1, -iw, 8, 11, Material.GOLD_BLOCK);

        // === THRONE ROOM (40-radius circular arena) ===
        b.filledCircle(0, 0, 0, 35, Material.DEEPSLATE_TILES);
        b.filledCircle(0, 0, 0, 30, Material.POLISHED_BLACKSTONE);

        // Lava channels (cross pattern)
        for (int i = -25; i <= 25; i++) {
            b.setBlock(i, 0, 0, Material.LAVA);
            b.setBlock(0, 0, i, Material.LAVA);
        }
        // Safe walkways over lava
        for (int i = -25; i <= 25; i += 3) {
            b.setBlock(i, 0, 0, Material.POLISHED_BLACKSTONE);
            b.setBlock(0, 0, i, Material.POLISHED_BLACKSTONE);
        }

        // Arena pillars (8 in a ring)
        for (int a = 0; a < 360; a += 45) {
            int px = (int) (20 * Math.cos(Math.toRadians(a)));
            int pz = (int) (20 * Math.sin(Math.toRadians(a)));
            b.pillar(px, 1, 20, pz, Material.GILDED_BLACKSTONE);
            b.setBlock(px, 20, pz, Material.SOUL_LANTERN);
            b.setBlock(px, 10, pz, Material.SOUL_LANTERN);
        }

        // Raised throne platform (north of center)
        b.fillBox(-5, 0, 20, 5, 4, 30, Material.GOLD_BLOCK);
        b.fillBox(-4, 4, 21, 4, 4, 29, Material.RED_CARPET);
        // Throne
        b.setBlock(0, 5, 27, Material.GOLD_BLOCK);
        b.setBlock(0, 6, 27, Material.GOLD_BLOCK);
        b.setBlock(-1, 6, 27, Material.GOLD_BLOCK);
        b.setBlock(1, 6, 27, Material.GOLD_BLOCK);
        b.setBlock(0, 7, 27, Material.GOLD_BLOCK);
        // Steps
        for (int s = 0; s < 4; s++) b.fillBox(-3, s, 20 + s, 3, s, 20 + s, Material.POLISHED_BLACKSTONE_BRICK_STAIRS);

        // === TREASURE VAULTS (east & west wings) ===
        // East vault
        b.fillBox(iw + 2, 0, -15, iw + 20, 12, 15, Material.POLISHED_BLACKSTONE_BRICKS);
        b.fillBox(iw + 3, 1, -14, iw + 19, 11, 14, Material.AIR);
        b.fillFloor(iw + 3, -14, iw + 19, 14, 0, Material.GOLD_BLOCK);
        b.fillBox(iw + 10, 1, -5, iw + 15, 4, 5, Material.GOLD_BLOCK);

        // West vault
        b.fillBox(-iw - 20, 0, -15, -iw - 2, 12, 15, Material.POLISHED_BLACKSTONE_BRICKS);
        b.fillBox(-iw - 19, 1, -14, -iw - 3, 11, 14, Material.AIR);
        b.fillFloor(-iw - 19, -14, -iw - 3, 14, 0, Material.GOLD_BLOCK);

        // Courtyard features — lava pools in outer courtyard
        b.filledCircle(-60, 0, -60, 8, Material.LAVA);
        b.filledCircle(60, 0, -60, 8, Material.LAVA);
        b.filledCircle(-60, 0, 60, 8, Material.LAVA);
        b.filledCircle(60, 0, 60, 8, Material.LAVA);

        // Magma block paths connecting features
        for (int i = -ow + 5; i <= -iw - 5; i++) {
            b.setBlock(i, 0, 0, Material.MAGMA_BLOCK);
            b.setBlock(-i, 0, 0, Material.MAGMA_BLOCK);
        }

        // Ceiling for throne room
        b.dome(0, 25, 0, 30, Material.NETHER_BRICKS);
        b.chandelier(0, 25, 0, 5);
        b.chandelier(-15, 25, -15, 3);
        b.chandelier(15, 25, -15, 3);
        b.chandelier(-15, 25, 15, 3);
        b.chandelier(15, 25, 15, 3);

        // Wall lighting throughout
        b.wallLighting(-iw + 1, 10, -iw, iw - 1, -iw, 6, Material.SOUL_LANTERN);
        b.wallLighting(-iw + 1, 10, iw, iw - 1, iw, 6, Material.SOUL_LANTERN);

        b.placeChest(iw + 12, 1, 0);
        b.placeChest(-iw - 12, 1, 0);
        b.placeChest(0, 5, 25);
        b.placeChest(25, 1, 25);
        b.placeChest(-25, 1, -25);
        b.setBossSpawn(0, 1, 10);
    }
}

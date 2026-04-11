package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;

/**
 * CrimsonCitadelBuilder — Nether brick fortress with crimson wood accents,
 * lava moat, drawbridge, throne room, prison cells, guard posts.
 *
 * <p>Extracted verbatim from the original {@code DimensionStructureBuilders.buildCrimsonCitadel}
 * method to give the structure its own file per the one-class-per-structure
 * architecture.
 */
public final class CrimsonCitadelBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // Lava moat (ring around the fortress)
        for (int x = -19; x <= 19; x++)
            for (int z = -19; z <= 19; z++) {
                double dist = Math.sqrt(x * x + z * z);
                if (dist >= 16 && dist <= 19) b.setBlock(x, 0, z, Material.LAVA);
            }

        // Foundation platform
        b.fillBox(-15, 0, -15, 15, 0, 15, Material.POLISHED_BLACKSTONE_BRICKS);

        // Outer walls — nether brick with crimson accents
        b.fillWalls(-15, 1, -15, 15, 12, 15, Material.NETHER_BRICKS);
        b.hollowBox(-15, 1, -15, 15, 12, 15);

        // Crimson trim bands at y=4 and y=8
        for (int y : new int[]{4, 8}) {
            b.fillBox(-15, y, -15, 15, y, -15, Material.CRIMSON_PLANKS);
            b.fillBox(-15, y, 15, 15, y, 15, Material.CRIMSON_PLANKS);
            b.fillBox(-15, y, -15, -15, y, 15, Material.CRIMSON_PLANKS);
            b.fillBox(15, y, -15, 15, y, 15, Material.CRIMSON_PLANKS);
        }

        // Battlements on top
        b.battlements(-15, 13, -15, 15, -15, Material.NETHER_BRICKS);
        b.battlements(-15, 13, 15, 15, 15, Material.NETHER_BRICKS);
        b.battlements(-15, 13, -15, -15, 15, Material.NETHER_BRICKS);
        b.battlements(15, 13, -15, 15, 15, Material.NETHER_BRICKS);

        // Floor — crimson planks with blackstone border
        b.fillFloor(-14, -14, 14, 14, 0, Material.CRIMSON_PLANKS);
        b.fillBox(-14, 0, -14, -12, 0, 14, Material.POLISHED_BLACKSTONE);
        b.fillBox(12, 0, -14, 14, 0, 14, Material.POLISHED_BLACKSTONE);

        // Corner towers (4) with soul lanterns
        int[][] corners = {{-15, -15}, {15, -15}, {-15, 15}, {15, 15}};
        for (int[] c : corners) {
            b.fillBox(c[0] - 2, 0, c[1] - 2, c[0] + 2, 15, c[1] + 2, Material.RED_NETHER_BRICKS);
            b.fillBox(c[0] - 1, 1, c[1] - 1, c[0] + 1, 14, c[1] + 1, Material.AIR);
            b.setBlock(c[0], 14, c[1], Material.SOUL_LANTERN);
            b.spiralStaircase(c[0], 1, 14, c[1], 1, Material.NETHER_BRICK_STAIRS, Material.NETHER_BRICKS);
        }

        // Drawbridge (south entrance across moat)
        b.fillBox(-2, 0, -19, 2, 0, -15, Material.CRIMSON_PLANKS);
        b.fillBox(-2, 1, -15, 2, 4, -15, Material.AIR); // gate opening
        b.archDoorway(0, 1, -15, 4, 5, Material.NETHER_BRICKS);

        // Interior pillars — polished blackstone
        for (int x = -10; x <= 10; x += 5)
            for (int z = -10; z <= 10; z += 10) {
                b.pillar(x, 1, 11, z, Material.POLISHED_BLACKSTONE_BRICKS);
                b.setBlock(x, 11, z, Material.SOUL_LANTERN);
            }

        // Throne room (north section)
        b.fillBox(-6, 0, 8, 6, 0, 14, Material.RED_NETHER_BRICKS);
        b.fillBox(-1, 1, 12, 1, 1, 14, Material.CRIMSON_PLANKS);
        b.setBlock(0, 2, 13, Material.GOLD_BLOCK);
        b.setBlock(0, 3, 13, Material.GOLD_BLOCK);
        b.setBlock(-1, 3, 13, Material.CRIMSON_FENCE);
        b.setBlock(1, 3, 13, Material.CRIMSON_FENCE);
        b.setBlock(0, 1, 14, Material.CRIMSON_STAIRS);
        // Throne banners
        b.setBlock(-3, 5, 14, Material.RED_BANNER);
        b.setBlock(3, 5, 14, Material.RED_BANNER);

        // Prison cells (east wing)
        for (int cell = 0; cell < 3; cell++) {
            int cz = -10 + cell * 5;
            b.fillBox(8, 0, cz, 13, 4, cz + 3, Material.NETHER_BRICKS);
            b.fillBox(9, 1, cz + 1, 12, 3, cz + 2, Material.AIR);
            b.setBlock(8, 1, cz + 1, Material.IRON_BARS);
            b.setBlock(8, 2, cz + 1, Material.IRON_BARS);
            b.setBlock(8, 3, cz + 1, Material.IRON_BARS);
            b.setBlock(8, 1, cz + 2, Material.IRON_BARS);
            b.setBlock(8, 2, cz + 2, Material.IRON_BARS);
            b.setBlock(8, 3, cz + 2, Material.IRON_BARS);
        }

        // Wither skeleton guard posts (raised platforms)
        b.fillBox(-13, 1, -13, -11, 2, -11, Material.POLISHED_BLACKSTONE);
        b.setBlock(-12, 3, -12, Material.SOUL_TORCH);
        b.fillBox(11, 1, -13, 13, 2, -11, Material.POLISHED_BLACKSTONE);
        b.setBlock(12, 3, -12, Material.SOUL_TORCH);

        // Wall lighting
        b.wallLighting(-14, 6, -15, 14, -15, 4, Material.SOUL_LANTERN);
        b.wallLighting(-14, 6, 15, 14, 15, 4, Material.SOUL_LANTERN);

        // Ceiling detail
        b.fillFloor(-14, -14, 14, 14, 12, Material.NETHER_BRICKS);
        b.chandelier(0, 12, 0, 3);
        b.chandelier(-8, 12, -8, 2);
        b.chandelier(8, 12, -8, 2);

        b.placeChest(0, 1, 0);
        b.placeChest(11, 1, -12);
        b.placeChest(-11, 1, 12);
        b.placeChest(5, 1, 13);
        b.setBossSpawn(0, 1, 8);
    }
}

package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;

/**
 * CursedGraveyardBuilder — iron-fenced cemetery with mausoleum, tombstones, and soul fires.
 *
 * <p>Extracted verbatim from the original monolithic builder method.
 */
public final class CursedGraveyardBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // Ground
        b.filledCircle(0, 0, 0, 12, Material.PODZOL);
        b.scatter(-12, 0, -12, 12, 0, 12, Material.PODZOL, Material.SOUL_SOIL, 0.3);
        b.scatter(-12, 0, -12, 12, 0, 12, Material.PODZOL, Material.COARSE_DIRT, 0.15);

        // Iron fence perimeter
        b.circle(0, 1, 0, 12, Material.IRON_BARS);
        // Gate (south)
        b.setBlock(0, 1, -12, Material.AIR); b.setBlock(1, 1, -12, Material.AIR);
        b.pillar(-1, 1, 3, -12, Material.STONE_BRICK_WALL);
        b.pillar(2, 1, 3, -12, Material.STONE_BRICK_WALL);

        // 20+ tombstones (stone buttons on stone)
        int[][] gravePositions = {
            {-8, 3}, {-5, 5}, {-2, 7}, {1, 5}, {4, 3}, {7, 1},
            {-7, -1}, {-4, -3}, {-1, -6}, {2, -4}, {5, -2}, {8, 0},
            {-6, 8}, {-3, 9}, {0, 10}, {3, 8}, {6, 6},
            {-9, -4}, {-6, -7}, {3, -8}, {6, -5}
        };
        for (int[] g : gravePositions) {
            b.setBlock(g[0], 1, g[1], Material.STONE_BRICK_WALL);
            b.setBlock(g[0], 2, g[1], Material.STONE_BRICK_WALL);
            b.setBlock(g[0], 3, g[1], Material.STONE_BUTTON);
        }

        // Central mausoleum
        b.fillBox(-3, 0, -3, 3, 0, 3, Material.DEEPSLATE_BRICKS);
        b.fillWalls(-3, 1, -3, 3, 5, 3, Material.DEEPSLATE_BRICKS);
        b.hollowBox(-3, 1, -3, 3, 5, 3);
        b.fillBox(-3, 6, -3, 3, 6, 3, Material.DEEPSLATE_BRICK_SLAB);
        b.pyramidRoof(-4, -4, 4, 4, 6, Material.DEEPSLATE_TILES);
        // Door
        b.setBlock(0, 1, -3, Material.AIR); b.setBlock(0, 2, -3, Material.AIR);
        // Coffins inside
        b.setBlock(-1, 1, 1, Material.DARK_OAK_PLANKS);
        b.setBlock(-1, 1, 0, Material.DARK_OAK_PLANKS);
        b.setBlock(1, 1, 1, Material.DARK_OAK_PLANKS);
        b.setBlock(1, 1, 0, Material.DARK_OAK_PLANKS);
        b.setBlock(0, 4, 0, Material.SOUL_LANTERN);

        // Dead trees
        b.pillar(-9, 1, 6, -8, Material.DARK_OAK_LOG);
        b.setBlock(-9, 7, -8, Material.DARK_OAK_LOG);
        b.setBlock(-10, 6, -8, Material.DARK_OAK_LOG);
        b.setBlock(-8, 6, -8, Material.DARK_OAK_LOG);
        b.pillar(8, 1, 5, 7, Material.DARK_OAK_LOG);
        b.setBlock(9, 6, 7, Material.DARK_OAK_LOG);
        b.setBlock(7, 5, 7, Material.DARK_OAK_LOG);

        // Soul lanterns throughout
        b.setBlock(-5, 1, 0, Material.SOUL_LANTERN);
        b.setBlock(5, 1, 0, Material.SOUL_LANTERN);
        b.setBlock(0, 1, 6, Material.SOUL_LANTERN);
        b.setBlock(0, 1, -6, Material.SOUL_LANTERN);

        // Fog (soul fire)
        b.setBlock(-3, 0, -8, Material.SOUL_CAMPFIRE);
        b.setBlock(4, 0, 6, Material.SOUL_CAMPFIRE);
        b.setBlock(-7, 0, 5, Material.SOUL_CAMPFIRE);

        // Open grave (empty hole)
        b.fillBox(6, -2, -8, 8, 0, -6, Material.AIR);
        b.fillBox(5, -2, -9, 5, 0, -5, Material.COARSE_DIRT);
        b.fillBox(9, -2, -9, 9, 0, -5, Material.COARSE_DIRT);

        // Cobwebs
        b.scatter(-12, 1, -12, 12, 3, 12, Material.AIR, Material.COBWEB, 0.04);

        b.placeChest(0, 1, 0);
        b.placeChest(-5, 0, 4);
        b.setBossSpawn(0, 1, -5);
    }
}

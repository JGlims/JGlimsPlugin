package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;

/**
 * SoulSanctumBuilder — Soul sand temple with ghostly blue glass, meditation rooms,
 * sealed vault with soul fire perimeter.
 *
 * <p>Extracted verbatim from the original {@code DimensionStructureBuilders.buildSoulSanctum}
 * method to give the structure its own file per the one-class-per-structure
 * architecture.
 */
public final class SoulSanctumBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // Foundation
        b.fillBox(-14, 0, -14, 14, 0, 14, Material.SOUL_SOIL);

        // Outer walls — soul sand with deepslate frame
        b.fillWalls(-14, 1, -14, 14, 10, 14, Material.SOUL_SAND);
        b.hollowBox(-14, 1, -14, 14, 10, 14);

        // Deepslate edge pillars
        for (int[] c : new int[][]{{-14, -14}, {14, -14}, {-14, 14}, {14, 14}})
            b.pillar(c[0], 1, 10, c[1], Material.POLISHED_DEEPSLATE);

        // Floor pattern — alternating soul soil and blackstone
        for (int x = -13; x <= 13; x++)
            for (int z = -13; z <= 13; z++)
                b.setBlock(x, 0, z, (x + z) % 2 == 0 ? Material.SOUL_SOIL : Material.BLACKSTONE);

        // Blue stained glass windows every 4 blocks
        for (int i = -10; i <= 10; i += 4) {
            for (int y = 4; y <= 7; y++) {
                b.setBlock(i, y, -14, Material.LIGHT_BLUE_STAINED_GLASS);
                b.setBlock(i, y, 14, Material.LIGHT_BLUE_STAINED_GLASS);
                b.setBlock(-14, y, i, Material.LIGHT_BLUE_STAINED_GLASS);
                b.setBlock(14, y, i, Material.LIGHT_BLUE_STAINED_GLASS);
            }
        }

        // Soul fire perimeter (ring inside)
        b.circle(0, 1, 0, 11, Material.SOUL_CAMPFIRE);

        // Central altar — raised obsidian platform with soul fire
        b.fillBox(-3, 0, -3, 3, 2, 3, Material.POLISHED_BLACKSTONE);
        b.fillBox(-2, 2, -2, 2, 2, 2, Material.CRYING_OBSIDIAN);
        b.setBlock(0, 3, 0, Material.SOUL_CAMPFIRE);
        b.setBlock(-2, 3, -2, Material.SOUL_LANTERN);
        b.setBlock(2, 3, -2, Material.SOUL_LANTERN);
        b.setBlock(-2, 3, 2, Material.SOUL_LANTERN);
        b.setBlock(2, 3, 2, Material.SOUL_LANTERN);

        // Meditation rooms (4 alcoves in corners)
        int[][] alcoves = {{-12, -12}, {12, -12}, {-12, 12}, {12, 12}};
        for (int[] a : alcoves) {
            b.fillBox(a[0] - 2, 0, a[1] - 2, a[0] + 2, 5, a[1] + 2, Material.DEEPSLATE_BRICKS);
            b.fillBox(a[0] - 1, 1, a[1] - 1, a[0] + 1, 4, a[1] + 1, Material.AIR);
            b.setBlock(a[0], 0, a[1], Material.SOUL_SOIL);
            b.setBlock(a[0], 1, a[1], Material.SOUL_CAMPFIRE);
            b.setBlock(a[0], 4, a[1], Material.SOUL_LANTERN);
        }

        // Sealed vault (underground chamber)
        b.fillBox(-8, -6, -8, 8, -1, 8, Material.DEEPSLATE_BRICKS);
        b.fillBox(-7, -5, -7, 7, -1, 7, Material.AIR);
        b.fillFloor(-7, -7, 7, 7, -6, Material.SOUL_SOIL);
        b.circle(0, -5, 0, 5, Material.SOUL_CAMPFIRE);
        // Vault entrance (stairs down)
        for (int s = 0; s < 6; s++) b.setBlock(0, -s, -13 + s, Material.DEEPSLATE_BRICK_STAIRS);

        // Ceiling — dome shape using soul sand and lanterns
        b.dome(0, 10, 0, 10, Material.SOUL_SAND);
        b.setBlock(0, 19, 0, Material.SOUL_LANTERN);

        // Ghostly atmosphere — scattered soul torches
        b.wallLighting(-13, 5, -14, 13, -14, 5, Material.SOUL_TORCH);
        b.wallLighting(-13, 5, 14, 13, 14, 5, Material.SOUL_TORCH);

        b.placeChest(0, -5, 0);
        b.placeChest(5, 1, 5);
        b.placeChest(-5, 1, -5);
        b.setBossSpawn(0, 3, 5);
    }
}

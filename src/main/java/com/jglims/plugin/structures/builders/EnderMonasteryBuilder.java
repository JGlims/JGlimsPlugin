package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;

/**
 * EnderMonasteryBuilder — Peaceful end stone building with library, meditation hall,
 * training yard, monk cells, zen garden.
 *
 * <p>Extracted verbatim from the original {@code DimensionStructureBuilders.buildEnderMonastery}
 * method to give the structure its own file per the one-class-per-structure
 * architecture.
 */
public final class EnderMonasteryBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // Main building
        b.fillBox(-18, 0, -12, 18, 0, 12, Material.END_STONE_BRICKS);
        b.fillWalls(-18, 1, -12, 18, 12, 12, Material.END_STONE_BRICKS);
        b.hollowBox(-18, 1, -12, 18, 12, 12);
        b.fillFloor(-17, -11, 17, 11, 0, Material.PURPUR_BLOCK);

        // Gabled roof
        b.gabledRoof(-18, -12, 18, 12, 12, Material.PURPUR_STAIRS, Material.PURPUR_SLAB);

        // Central corridor
        b.fillBox(-1, 1, -11, 1, 8, 11, Material.AIR);
        b.fillBox(-1, 0, -11, 1, 0, 11, Material.END_STONE_BRICKS);

        // Library wing (west: -18 to -3)
        b.bookshelfWall(-16, 1, -11, -4, 5);
        b.bookshelfWall(-16, 1, 11, -4, 5);
        b.setBlock(-10, 1, 0, Material.LECTERN);
        b.setBlock(-12, 1, -5, Material.ENCHANTING_TABLE);
        b.chandelier(-10, 12, 0, 3);

        // Meditation hall (east: 3 to 18)
        b.fillFloor(3, -10, 17, 10, 0, Material.PURPUR_BLOCK);
        // Meditation mats (carpets)
        for (int x = 5; x <= 15; x += 5)
            for (int z = -8; z <= 8; z += 4)
                b.setBlock(x, 1, z, Material.PURPLE_CARPET);
        // Central meditation pillar
        b.pillar(10, 1, 8, 0, Material.PURPUR_PILLAR);
        b.setBlock(10, 9, 0, Material.END_ROD);
        b.chandelier(10, 12, 0, 2);

        // Training yard (courtyard outside south)
        b.fillBox(-10, 0, 13, 10, 0, 22, Material.END_STONE);
        b.fillBox(-10, 1, 13, -10, 3, 22, Material.END_STONE_BRICKS); // wall
        b.fillBox(10, 1, 13, 10, 3, 22, Material.END_STONE_BRICKS);
        // Training dummies (fence + skull)
        for (int x = -6; x <= 6; x += 4) {
            b.pillar(x, 1, 2, 18, Material.OAK_FENCE);
            b.setBlock(x, 3, 18, Material.SKELETON_SKULL);
        }

        // Monk cells (along north wall interior)
        for (int cell = 0; cell < 4; cell++) {
            int cx = -14 + cell * 8;
            b.fillBox(cx, 0, -11, cx + 4, 5, -8, Material.END_STONE_BRICKS);
            b.fillBox(cx + 1, 1, -10, cx + 3, 4, -9, Material.AIR);
            b.setBlock(cx + 2, 1, -10, Material.WHITE_BED);
            b.setBlock(cx + 1, 4, -10, Material.END_ROD);
        }

        // Zen garden (dead bushes + end stone, east yard)
        b.fillBox(12, 0, 13, 18, 0, 20, Material.END_STONE);
        for (int x = 13; x <= 17; x += 2)
            for (int z = 14; z <= 19; z += 2) {
                b.setBlock(x, 1, z, Material.DEAD_BUSH);
            }
        b.setBlock(15, 1, 17, Material.END_ROD); // garden light

        // Entrance (south wall center)
        b.archDoorway(0, 1, -12, 4, 6, Material.PURPUR_BLOCK);

        // Interior pillars
        for (int x = -14; x <= 14; x += 7) {
            b.pillar(x, 1, 10, -4, Material.PURPUR_PILLAR);
            b.pillar(x, 1, 10, 4, Material.PURPUR_PILLAR);
        }

        // Lighting
        b.wallLighting(-17, 6, -12, 17, -12, 5, Material.END_ROD);
        b.wallLighting(-17, 6, 12, 17, 12, 5, Material.END_ROD);

        b.placeChest(-12, 1, -3);
        b.placeChest(14, 1, 5);
        b.placeChest(-14, 1, 8);
        b.setBossSpawn(10, 1, 0);
    }
}

package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

/**
 * AbyssalCastleBuilder — a massive deepslate-and-blackstone cathedral-castle with
 * four corner mega-towers, a central domed keep, flying buttresses, gothic nave,
 * side chapels, throne room, and a subterranean crypt with sarcophagi.
 *
 * <p>Extracted verbatim from the original {@code DimensionStructureBuilders.buildAbyssalCastle}
 * method to give the structure its own file per the one-class-per-structure
 * architecture.
 */
public final class AbyssalCastleBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // === OUTER WALLS (120x80 footprint: -60 to 60 x, -40 to 40 z) ===
        b.fillBox(-60, 0, -40, 60, 0, 40, Material.DEEPSLATE_BRICKS);
        b.fillWalls(-60, 1, -40, 60, 20, 40, Material.DEEPSLATE_BRICKS);
        b.hollowBox(-60, 1, -40, 60, 20, 40);
        b.fillFloor(-59, -39, 59, 39, 0, Material.DEEPSLATE_TILES);

        // Blackstone base course
        b.fillBox(-60, 1, -40, 60, 2, -40, Material.POLISHED_BLACKSTONE);
        b.fillBox(-60, 1, 40, 60, 2, 40, Material.POLISHED_BLACKSTONE);
        b.fillBox(-60, 1, -40, -60, 2, 40, Material.POLISHED_BLACKSTONE);
        b.fillBox(60, 1, -40, 60, 2, 40, Material.POLISHED_BLACKSTONE);

        // === 4 CORNER MEGA-TOWERS (30 blocks tall) ===
        int[][] towerCorners = {{-60, -40}, {60, -40}, {-60, 40}, {60, 40}};
        for (int[] tc : towerCorners) {
            // Square tower base
            b.fillBox(tc[0] - 6, 0, tc[1] - 6, tc[0] + 6, 30, tc[1] + 6, Material.DEEPSLATE_BRICKS);
            b.fillBox(tc[0] - 5, 1, tc[1] - 5, tc[0] + 5, 29, tc[1] + 5, Material.AIR);
            // Spiral staircase
            b.spiralStaircase(tc[0], 1, 29, tc[1], 3, Material.DEEPSLATE_BRICK_STAIRS, Material.DEEPSLATE_BRICKS);
            // Conical roof
            b.spire(tc[0], 30, tc[1], 7, 10, Material.DEEPSLATE_TILES);
            // Top lantern
            b.setBlock(tc[0], 40, tc[1], Material.SOUL_LANTERN);
            // Battlements
            b.battlements(tc[0] - 6, 31, tc[1] - 6, tc[0] + 6, tc[1] - 6, Material.DEEPSLATE_BRICKS);
            b.battlements(tc[0] - 6, 31, tc[1] + 6, tc[0] + 6, tc[1] + 6, Material.DEEPSLATE_BRICKS);
            // Windows
            for (int y = 5; y <= 25; y += 5) {
                b.setBlock(tc[0], y, tc[1] - 6, Material.PURPLE_STAINED_GLASS);
                b.setBlock(tc[0], y, tc[1] + 6, Material.PURPLE_STAINED_GLASS);
                b.setBlock(tc[0] - 6, y, tc[1], Material.PURPLE_STAINED_GLASS);
                b.setBlock(tc[0] + 6, y, tc[1], Material.PURPLE_STAINED_GLASS);
            }
        }

        // === CENTRAL KEEP (30x30 with dome) ===
        b.fillWalls(-15, 0, -15, 15, 30, 15, Material.POLISHED_BLACKSTONE_BRICKS);
        b.hollowBox(-15, 1, -15, 15, 30, 15);
        b.fillFloor(-14, -14, 14, 14, 0, Material.CRYING_OBSIDIAN);

        // Dome atop central keep
        b.dome(0, 30, 0, 15, Material.DEEPSLATE_TILES);
        b.setBlock(0, 45, 0, Material.SOUL_LANTERN);

        // Rose windows (4 sides of keep)
        b.roseWindow(0, 20, -15, 5, Material.PURPLE_STAINED_GLASS, BlockFace.NORTH);
        b.roseWindow(0, 20, 15, 5, Material.PURPLE_STAINED_GLASS, BlockFace.SOUTH);
        b.roseWindow(-15, 20, 0, 5, Material.PURPLE_STAINED_GLASS, BlockFace.WEST);
        b.roseWindow(15, 20, 0, 5, Material.PURPLE_STAINED_GLASS, BlockFace.EAST);

        // === FLYING BUTTRESSES (connecting keep to outer walls) ===
        // North side
        b.flyingButtress(-15, 18, -15, -40, 8, -40, Material.DEEPSLATE_BRICKS);
        b.flyingButtress(15, 18, -15, 40, 8, -40, Material.DEEPSLATE_BRICKS);
        // South side
        b.flyingButtress(-15, 18, 15, -40, 8, 40, Material.DEEPSLATE_BRICKS);
        b.flyingButtress(15, 18, 15, 40, 8, 40, Material.DEEPSLATE_BRICKS);

        // === NAVE (central corridor from entrance to keep) ===
        b.fillBox(-6, 0, -40, 6, 18, -16, Material.DEEPSLATE_BRICKS);
        b.fillBox(-5, 1, -39, 5, 16, -16, Material.AIR);
        b.fillFloor(-5, -39, 5, -16, 0, Material.DEEPSLATE_TILES);
        // Nave pillars
        for (int z = -37; z <= -18; z += 4) {
            b.pillar(-5, 1, 15, z, Material.POLISHED_BLACKSTONE_BRICKS);
            b.pillar(5, 1, 15, z, Material.POLISHED_BLACKSTONE_BRICKS);
            b.setBlock(-5, 15, z, Material.SOUL_LANTERN);
            b.setBlock(5, 15, z, Material.SOUL_LANTERN);
        }
        // Gothic arches in nave ceiling
        for (int z = -36; z <= -20; z += 8)
            b.gothicArch(0, 12, z, 10, 6, Material.DEEPSLATE_BRICKS);

        // Grand entrance
        b.archDoorway(0, 1, -40, 8, 12, Material.POLISHED_BLACKSTONE);

        // === SIDE CHAPELS (east and west) ===
        // East chapel
        b.fillBox(16, 0, -10, 35, 12, 10, Material.DEEPSLATE_BRICKS);
        b.fillBox(17, 1, -9, 34, 11, 9, Material.AIR);
        b.fillFloor(17, -9, 34, 9, 0, Material.OBSIDIAN);
        // Chapel altar
        b.fillBox(30, 1, -2, 33, 3, 2, Material.CRYING_OBSIDIAN);
        b.setBlock(32, 4, 0, Material.SOUL_LANTERN);
        b.setBlock(31, 1, 0, Material.AMETHYST_BLOCK);
        // Pews
        for (int z = -7; z <= 7; z += 3)
            b.chair(20, 1, z, Material.POLISHED_BLACKSTONE_BRICK_STAIRS, BlockFace.EAST);

        // West chapel
        b.fillBox(-35, 0, -10, -16, 12, 10, Material.DEEPSLATE_BRICKS);
        b.fillBox(-34, 1, -9, -17, 11, 9, Material.AIR);
        b.fillFloor(-34, -9, -17, 9, 0, Material.OBSIDIAN);
        b.fillBox(-33, 1, -2, -30, 3, 2, Material.CRYING_OBSIDIAN);
        b.setBlock(-32, 4, 0, Material.SOUL_LANTERN);
        b.setBlock(-31, 1, 0, Material.AMETHYST_BLOCK);

        // === THRONE ROOM (inside keep) ===
        // Throne platform (north of keep center)
        b.fillBox(-4, 0, 8, 4, 3, 14, Material.OBSIDIAN);
        b.fillBox(-3, 3, 9, 3, 3, 13, Material.CRYING_OBSIDIAN);
        b.setBlock(0, 4, 12, Material.GOLD_BLOCK);
        b.setBlock(0, 5, 12, Material.GOLD_BLOCK);
        b.setBlock(-1, 5, 12, Material.AMETHYST_BLOCK);
        b.setBlock(1, 5, 12, Material.AMETHYST_BLOCK);
        b.setBlock(0, 6, 12, Material.AMETHYST_BLOCK);

        // Interior pillars in keep
        for (int x = -12; x <= 12; x += 6)
            for (int z = -12; z <= 12; z += 6) {
                if (Math.abs(x) < 5 && Math.abs(z) < 5) continue;
                b.pillar(x, 1, 28, z, Material.POLISHED_BLACKSTONE_BRICKS);
                b.setBlock(x, 28, z, Material.SOUL_LANTERN);
            }

        // Chandeliers in keep
        b.chandelier(0, 30, 0, 5);
        b.chandelier(-8, 30, -8, 3);
        b.chandelier(8, 30, -8, 3);
        b.chandelier(-8, 30, 8, 3);
        b.chandelier(8, 30, 8, 3);

        // === CRYPT (below the keep) ===
        b.fillBox(-20, -8, -20, 20, -1, 20, Material.DEEPSLATE_BRICKS);
        b.fillBox(-19, -7, -19, 19, -1, 19, Material.AIR);
        b.fillFloor(-19, -19, 19, 19, -8, Material.OBSIDIAN);

        // Crypt pillars
        for (int x = -15; x <= 15; x += 10)
            for (int z = -15; z <= 15; z += 10) {
                b.pillar(x, -7, -1, z, Material.POLISHED_BLACKSTONE_BRICKS);
            }

        // Sarcophagi (obsidian blocks)
        for (int i = -12; i <= 12; i += 8) {
            b.fillBox(i - 1, -7, -2, i + 1, -6, 2, Material.CRYING_OBSIDIAN);
        }

        // Crypt entrance (stairs down from keep)
        for (int s = 0; s < 8; s++) b.setBlock(0, -s, -14 + s, Material.DEEPSLATE_BRICK_STAIRS);

        // Amethyst crystal decorations
        b.setBlock(-14, 4, 0, Material.AMETHYST_CLUSTER);
        b.setBlock(14, 4, 0, Material.AMETHYST_CLUSTER);
        b.setBlock(0, 4, 14, Material.AMETHYST_CLUSTER);
        b.setBlock(0, 4, -14, Material.AMETHYST_CLUSTER);

        // Outer wall battlements
        b.battlements(-60, 21, -40, 60, -40, Material.DEEPSLATE_BRICKS);
        b.battlements(-60, 21, 40, 60, 40, Material.DEEPSLATE_BRICKS);
        b.battlements(-60, 21, -40, -60, 40, Material.DEEPSLATE_BRICKS);
        b.battlements(60, 21, -40, 60, 40, Material.DEEPSLATE_BRICKS);

        // Wall lighting for outer walls
        b.wallLighting(-58, 10, -40, 58, -40, 8, Material.SOUL_LANTERN);
        b.wallLighting(-58, 10, 40, 58, 40, 8, Material.SOUL_LANTERN);

        b.placeChest(0, 1, 0);
        b.placeChest(32, 1, 5);
        b.placeChest(-32, 1, -5);
        b.placeChest(0, -7, 10);
        b.placeChest(0, -7, -10);
        b.placeChest(0, 4, 10);
        b.setBossSpawn(0, -7, 0);
    }
}

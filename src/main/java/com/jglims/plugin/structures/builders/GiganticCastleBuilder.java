package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

/**
 * GiganticCastleBuilder — a massive curtain-walled stone-brick castle with 8 towers,
 * gatehouse, great hall, throne room, kitchen/bedchambers/chapel wings, underground
 * dungeon, and a courtyard well.
 *
 * <p>Extracted verbatim from the original {@code OverworldStructureBuilders.buildGiganticCastle}
 * method to give the structure its own file per the one-class-per-structure
 * architecture.
 */
public final class GiganticCastleBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // Curtain wall
        b.fillWalls(-38, 0, -38, 38, 14, 38, Material.STONE_BRICKS);
        b.hollowBox(-38, 1, -38, 38, 14, 38);
        b.fillFloor(-37, -37, 37, 37, 0, Material.COBBLESTONE);
        // Scattered grass in courtyard
        b.scatter(-30, 0, -30, 30, 0, 30, Material.COBBLESTONE, Material.GRASS_BLOCK, 0.2);

        // Battlements on curtain wall
        for (int x = -38; x <= 38; x += 2) {
            b.setBlock(x, 15, -38, Material.STONE_BRICK_WALL);
            b.setBlock(x, 15, 38, Material.STONE_BRICK_WALL);
        }
        for (int z = -38; z <= 38; z += 2) {
            b.setBlock(-38, 15, z, Material.STONE_BRICK_WALL);
            b.setBlock(38, 15, z, Material.STONE_BRICK_WALL);
        }

        // 8 Towers along the walls
        int[][] towerPos = {{-38, -38}, {0, -38}, {38, -38}, {-38, 0}, {38, 0}, {-38, 38}, {0, 38}, {38, 38}};
        for (int[] t : towerPos) {
            b.fillBox(t[0] - 3, 0, t[1] - 3, t[0] + 3, 22, t[1] + 3, Material.STONE_BRICKS);
            b.fillBox(t[0] - 2, 1, t[1] - 2, t[0] + 2, 21, t[1] + 2, Material.AIR);
            b.spire(t[0], 22, t[1], 4, 6, Material.DARK_OAK_PLANKS);
            for (int y = 1; y <= 21; y++) b.setBlock(t[0] + 1, y, t[1] - 2, Material.LADDER);
            b.setBlock(t[0], 20, t[1], Material.LANTERN);
        }

        // Gatehouse with portcullis (south)
        b.fillBox(-5, 0, -38, 5, 16, -32, Material.STONE_BRICKS);
        b.fillBox(-3, 1, -38, 3, 10, -32, Material.AIR);
        // Portcullis (iron bars)
        for (int x = -3; x <= 3; x++) {
            for (int y = 4; y <= 10; y++) {
                b.setBlock(x, y, -38, Material.IRON_BARS);
            }
        }
        b.fillBox(-3, 1, -38, 3, 3, -38, Material.AIR);  // Opening below portcullis

        // Great hall (interior 40x20)
        b.fillBox(-20, 0, -18, 20, 0, 2, Material.STONE_BRICKS);
        b.fillWalls(-20, 1, -18, 20, 12, 2, Material.STONE_BRICKS);
        b.hollowBox(-20, 1, -18, 20, 12, 2);
        b.fillBox(-20, 13, -18, 20, 13, 2, Material.OAK_PLANKS);
        b.fillFloor(-19, -17, 19, 1, 0, Material.POLISHED_ANDESITE);
        // Great hall entrance
        b.setBlock(0, 1, -18, Material.AIR); b.setBlock(0, 2, -18, Material.AIR); b.setBlock(0, 3, -18, Material.AIR);
        // Great hall pillars
        for (int x = -16; x <= 16; x += 8) {
            b.pillar(x, 1, 12, -15, Material.STONE_BRICK_WALL);
            b.pillar(x, 1, 12, 0, Material.STONE_BRICK_WALL);
        }
        // Chandeliers
        b.chandelier(-8, 13, -8, 3);
        b.chandelier(8, 13, -8, 3);
        b.chandelier(0, 13, -4, 4);
        // Dining tables
        for (int x = -12; x <= 12; x += 4) {
            b.table(x, 1, -8);
            b.table(x, 1, -4);
        }
        // Banners
        for (int x = -18; x <= 18; x += 6) {
            b.banner(x, 8, -18, Material.RED_BANNER);
            b.banner(x, 8, 2, Material.RED_BANNER);
        }

        // Throne room
        b.fillBox(-8, 0, 5, 8, 0, 18, Material.STONE_BRICKS);
        b.fillWalls(-8, 1, 5, 8, 10, 18, Material.STONE_BRICKS);
        b.hollowBox(-8, 1, 5, 8, 10, 18);
        b.fillFloor(-7, 6, 7, 17, 0, Material.RED_CARPET);
        b.setBlock(0, 1, 5, Material.AIR); b.setBlock(0, 2, 5, Material.AIR);
        // Throne
        b.setBlock(0, 1, 15, Material.GOLD_BLOCK);
        b.setBlock(0, 2, 15, Material.GOLD_BLOCK);
        b.setBlock(-1, 2, 16, Material.GOLD_BLOCK);
        b.setBlock(1, 2, 16, Material.GOLD_BLOCK);
        b.setBlock(0, 3, 16, Material.GOLD_BLOCK);
        b.setBlock(0, 1, 14, Material.RED_CARPET);

        // Kitchen (east wing)
        b.furnishedRoom(22, 0, -15, 35, -5, 5, Material.STONE_BRICKS, Material.STONE_BRICKS, Material.OAK_PLANKS);
        b.setBlock(25, 1, -12, Material.FURNACE);
        b.setBlock(26, 1, -12, Material.FURNACE);
        b.setBlock(27, 1, -12, Material.SMOKER);
        b.setBlock(30, 1, -10, Material.CAULDRON);
        b.setBlock(33, 1, -8, Material.BARREL);

        // Bedchambers (NE)
        b.furnishedRoom(22, 0, 10, 35, 25, 5, Material.STONE_BRICKS, Material.OAK_PLANKS, Material.OAK_PLANKS);
        b.setBlock(25, 1, 15, Material.RED_BED);
        b.setBlock(28, 1, 15, Material.RED_BED);
        b.setBlock(31, 1, 15, Material.RED_BED);

        // Chapel (west wing)
        b.furnishedRoom(-35, 0, -15, -22, -5, 5, Material.STONE_BRICKS, Material.STONE_BRICKS, Material.STONE_BRICKS);
        b.setBlock(-28, 1, -8, Material.GOLD_BLOCK); // Altar
        b.setBlock(-28, 2, -8, Material.CANDLE);
        b.bookshelfWall(-34, 1, -5, -30, 3);
        b.roseWindow(-28, 4, -15, 2, Material.YELLOW_STAINED_GLASS, BlockFace.NORTH);

        // Dungeon below
        b.fillBox(-10, -7, -10, 10, -1, 10, Material.DEEPSLATE_BRICKS);
        b.fillBox(-9, -6, -9, 9, -1, 9, Material.AIR);
        b.fillFloor(-9, -9, 9, 9, -7, Material.DEEPSLATE_TILES);
        for (int z = -8; z <= 8; z += 4) {
            b.setBlock(-8, -6, z, Material.IRON_BARS); b.setBlock(-8, -5, z, Material.IRON_BARS);
            b.setBlock(8, -6, z, Material.IRON_BARS); b.setBlock(8, -5, z, Material.IRON_BARS);
        }
        b.setBlock(0, -6, 0, Material.SOUL_LANTERN);

        // Courtyard well
        b.filledCircle(0, 0, -28, 2, Material.COBBLESTONE);
        b.setBlock(0, 0, -28, Material.WATER);
        b.pillar(-2, 1, 3, -28, Material.COBBLESTONE_WALL);
        b.pillar(2, 1, 3, -28, Material.COBBLESTONE_WALL);
        b.fillBox(-2, 4, -29, 2, 4, -27, Material.OAK_PLANKS);

        b.placeChest(0, 1, 0);
        b.placeChest(5, -6, 5);
        b.placeChest(-25, 1, -10);
        b.placeChest(28, 1, 18);
        b.setBossSpawn(0, 1, 10);
    }
}

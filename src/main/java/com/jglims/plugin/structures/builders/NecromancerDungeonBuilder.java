package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

/**
 * NecromancerDungeonBuilder — underground deepslate crypt with library, summoning chamber, and throne.
 *
 * <p>Extracted verbatim from the original monolithic builder method.
 */
public final class NecromancerDungeonBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // Underground crypt — main chamber
        b.fillWalls(-12, -1, -12, 12, 8, 12, Material.DEEPSLATE_BRICKS);
        b.hollowBox(-12, 0, -12, 12, 8, 12);
        b.fillFloor(-11, -11, 11, 11, -1, Material.SOUL_SAND);

        // Entrance stairway (descending from surface)
        for (int s = 0; s < 8; s++) {
            b.fillBox(-2, 8 - s, -14 - s, 2, 8 - s, -14 - s, Material.STONE_BRICK_STAIRS);
            b.fillBox(-2, 9 - s, -14 - s, 2, 12, -14 - s, Material.AIR);
        }
        b.fillBox(-2, 0, -12, 2, 5, -12, Material.AIR);  // Entrance door

        // Skull decorations (wither skeleton skulls on walls)
        for (int x = -10; x <= 10; x += 4) {
            b.setBlock(x, 5, -11, Material.WITHER_SKELETON_SKULL);
            b.setBlock(x, 5, 11, Material.WITHER_SKELETON_SKULL);
        }
        for (int z = -10; z <= 10; z += 4) {
            b.setBlock(-11, 5, z, Material.WITHER_SKELETON_SKULL);
            b.setBlock(11, 5, z, Material.WITHER_SKELETON_SKULL);
        }

        // Dark library (west wing)
        b.fillBox(-12, 0, -8, -6, 6, -2, Material.DEEPSLATE_BRICKS);
        b.fillBox(-11, 0, -7, -7, 5, -3, Material.AIR);
        b.setBlock(-6, 1, -5, Material.AIR); b.setBlock(-6, 2, -5, Material.AIR); // Door
        b.bookshelfWall(-11, 1, -7, -7, 3);
        b.bookshelfWall(-11, 1, -3, -7, 3);
        b.setBlock(-9, 1, -5, Material.LECTERN);
        b.setBlock(-8, 4, -5, Material.SOUL_LANTERN);

        // Summoning chamber (east wing)
        b.fillBox(6, 0, -8, 12, 6, -2, Material.DEEPSLATE_BRICKS);
        b.fillBox(7, 0, -7, 11, 5, -3, Material.AIR);
        b.setBlock(6, 1, -5, Material.AIR); b.setBlock(6, 2, -5, Material.AIR); // Door
        b.fillFloor(7, -7, 11, -3, 0, Material.SOUL_SAND);
        // Summoning circle
        b.circle(9, 0, -5, 2, Material.OBSIDIAN);
        b.setBlock(9, 1, -5, Material.SOUL_CAMPFIRE);
        b.setBlock(7, 1, -7, Material.GREEN_CANDLE);
        b.setBlock(11, 1, -7, Material.GREEN_CANDLE);
        b.setBlock(7, 1, -3, Material.GREEN_CANDLE);
        b.setBlock(11, 1, -3, Material.GREEN_CANDLE);

        // Central altar/throne area
        b.fillBox(-3, 0, 5, 3, 0, 10, Material.OBSIDIAN);
        b.setBlock(0, 1, 8, Material.OBSIDIAN);
        b.setBlock(0, 2, 8, Material.OBSIDIAN);
        b.setStairs(-1, 1, 8, Material.DARK_OAK_STAIRS, BlockFace.EAST, false);
        b.setStairs(1, 1, 8, Material.DARK_OAK_STAIRS, BlockFace.WEST, false);
        b.setBlock(0, 3, 9, Material.WITHER_SKELETON_SKULL);

        // Green candles throughout
        b.setBlock(-8, 1, 0, Material.GREEN_CANDLE);
        b.setBlock(8, 1, 0, Material.GREEN_CANDLE);
        b.setBlock(0, 1, 4, Material.GREEN_CANDLE);
        b.setBlock(-6, 1, 6, Material.GREEN_CANDLE);
        b.setBlock(6, 1, 6, Material.GREEN_CANDLE);

        // Pillars
        for (int[] p : new int[][]{{-8, -8}, {8, -8}, {-8, 8}, {8, 8}}) {
            b.pillar(p[0], 0, 7, p[1], Material.POLISHED_BLACKSTONE_BRICKS);
            b.setBlock(p[0], 7, p[1], Material.SOUL_LANTERN);
        }

        // Soul sand floor decorations
        b.scatter(-11, -1, -11, 11, -1, 11, Material.SOUL_SAND, Material.SOUL_SOIL, 0.15);

        // Coffins (dark oak planks)
        b.fillBox(-10, 0, 3, -8, 0, 5, Material.DARK_OAK_PLANKS);
        b.setBlock(-9, 1, 4, Material.DARK_OAK_SLAB);
        b.fillBox(8, 0, 3, 10, 0, 5, Material.DARK_OAK_PLANKS);
        b.setBlock(9, 1, 4, Material.DARK_OAK_SLAB);

        // Cobwebs
        b.scatter(-11, 5, -11, 11, 7, 11, Material.AIR, Material.COBWEB, 0.06);

        // Chains from ceiling
        b.setBlock(-4, 7, -4, Material.IRON_CHAIN); b.setBlock(-4, 6, -4, Material.IRON_CHAIN);
        b.setBlock(4, 7, 4, Material.IRON_CHAIN); b.setBlock(4, 6, 4, Material.IRON_CHAIN);

        b.placeChest(-10, 1, -5);
        b.placeChest(0, 1, 6);
        b.placeChest(9, 1, -5);
        b.setBossSpawn(0, 1, 2);
    }
}

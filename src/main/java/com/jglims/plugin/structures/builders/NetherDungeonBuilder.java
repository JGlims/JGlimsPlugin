package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

/**
 * NetherDungeonBuilder — Underground cell blocks, torture devices, warden room,
 * treasure vault, lava traps.
 *
 * <p>Extracted verbatim from the original {@code DimensionStructureBuilders.buildNetherDungeon}
 * method to give the structure its own file per the one-class-per-structure
 * architecture.
 */
public final class NetherDungeonBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // Underground shell
        b.fillBox(-14, -2, -10, 14, 8, 10, Material.NETHER_BRICKS);
        b.fillBox(-13, -1, -9, 13, 7, 9, Material.AIR);
        b.fillFloor(-13, -9, 13, 9, -2, Material.POLISHED_BLACKSTONE);

        // Surface entrance — trapdoor style
        b.fillBox(-2, 8, -2, 2, 10, 2, Material.NETHER_BRICKS);
        b.fillBox(-1, 8, -1, 1, 10, 1, Material.AIR);
        b.setBlock(0, 8, 0, Material.NETHER_BRICK_STAIRS);

        // Spiral staircase down
        b.spiralStaircase(0, -1, 8, 0, 2, Material.NETHER_BRICK_STAIRS, Material.NETHER_BRICKS);

        // Central corridor
        b.fillBox(-1, -1, -8, 1, 2, 8, Material.AIR);
        b.fillBox(-1, -2, -8, 1, -2, 8, Material.DEEPSLATE_BRICKS);

        // Cell blocks (left side)
        for (int cell = 0; cell < 4; cell++) {
            int cz = -7 + cell * 4;
            b.fillBox(-8, -2, cz, -3, 3, cz + 2, Material.NETHER_BRICKS);
            b.fillBox(-7, -1, cz, -4, 2, cz + 2, Material.AIR);
            b.fillFloor(-7, cz, -4, cz + 2, -2, Material.SOUL_SAND);
            // Iron bars as cell door
            for (int y = -1; y <= 2; y++) {
                b.setBlock(-3, y, cz, Material.IRON_BARS);
                b.setBlock(-3, y, cz + 1, Material.IRON_BARS);
                b.setBlock(-3, y, cz + 2, Material.IRON_BARS);
            }
            b.setBlock(-6, 2, cz + 1, Material.SOUL_LANTERN);
        }

        // Cell blocks (right side)
        for (int cell = 0; cell < 4; cell++) {
            int cz = -7 + cell * 4;
            b.fillBox(3, -2, cz, 8, 3, cz + 2, Material.NETHER_BRICKS);
            b.fillBox(4, -1, cz, 7, 2, cz + 2, Material.AIR);
            b.fillFloor(4, cz, 7, cz + 2, -2, Material.SOUL_SAND);
            for (int y = -1; y <= 2; y++) {
                b.setBlock(3, y, cz, Material.IRON_BARS);
                b.setBlock(3, y, cz + 1, Material.IRON_BARS);
                b.setBlock(3, y, cz + 2, Material.IRON_BARS);
            }
        }

        // Torture room (far east corner)
        b.fillBox(9, -2, -8, 13, 4, -4, Material.RED_NETHER_BRICKS);
        b.fillBox(10, -1, -7, 12, 3, -5, Material.AIR);
        b.setBlock(10, -1, -7, Material.ANVIL);      // torture device
        b.setBlock(12, -1, -7, Material.CAULDRON);
        b.setBlock(11, -1, -5, Material.IRON_BARS);
        b.setBlock(11, 0, -5, Material.IRON_BARS);
        b.setBlock(11, 3, -6, Material.SOUL_LANTERN);

        // Warden room (far north)
        b.fillBox(-8, -2, 5, -3, 4, 9, Material.POLISHED_BLACKSTONE_BRICKS);
        b.fillBox(-7, -1, 6, -4, 3, 8, Material.AIR);
        b.table(-6, -1, 7);
        b.chair(-5, -1, 7, Material.POLISHED_BLACKSTONE_BRICK_STAIRS, BlockFace.WEST);
        b.setBlock(-7, -1, 8, Material.RED_BED);
        b.setBlock(-4, 3, 7, Material.SOUL_LANTERN);

        // Lava traps — channels in the floor
        b.fillBox(-1, -2, -6, -1, -2, 6, Material.MAGMA_BLOCK);
        b.fillBox(1, -2, -6, 1, -2, 6, Material.MAGMA_BLOCK);

        // Treasure vault (far south behind locked bars)
        b.fillBox(-5, -2, -9, 5, 3, -9, Material.IRON_BARS);
        b.fillBox(-12, -2, -10, -3, 4, -10, Material.AIR);
        b.fillBox(-13, -2, -10, 13, -2, -10, Material.GOLD_BLOCK);

        b.placeChest(0, -1, -9);
        b.placeChest(-11, -1, -9);
        b.placeChest(11, -1, -9);
        b.placeChest(-6, -1, 6);
        b.setBossSpawn(0, -1, 0);
    }
}

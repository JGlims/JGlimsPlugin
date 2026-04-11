package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

/**
 * DungeonDeepBuilder — a three-level deepslate prison complex: main hall with cell
 * blocks and a torture room, corridor level with spawner rooms and a flooded section,
 * and a deep obsidian treasure vault.
 *
 * <p>Extracted verbatim from the original {@code OverworldStructureBuilders.buildDungeonDeep}
 * method to give the structure its own file per the one-class-per-structure
 * architecture.
 */
public final class DungeonDeepBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // Level 1: Main hall
        b.fillWalls(-14, 0, -14, 14, 6, 14, Material.DEEPSLATE_BRICKS);
        b.hollowBox(-14, 1, -14, 14, 6, 14);
        b.fillFloor(-13, -13, 13, 13, 0, Material.DEEPSLATE_TILES);
        b.scatter(-13, 0, -13, 13, 0, 13, Material.DEEPSLATE_TILES, Material.SCULK, 0.08);

        // Cell blocks along walls
        for (int z = -12; z <= 12; z += 4) {
            // West cells
            b.setBlock(-13, 1, z, Material.IRON_BARS); b.setBlock(-13, 2, z, Material.IRON_BARS);
            b.setBlock(-13, 3, z, Material.IRON_BARS);
            // East cells
            b.setBlock(13, 1, z, Material.IRON_BARS); b.setBlock(13, 2, z, Material.IRON_BARS);
            b.setBlock(13, 3, z, Material.IRON_BARS);
        }

        // Torture room (NE)
        b.fillBox(6, 1, -13, 13, 5, -6, Material.DEEPSLATE_BRICKS);
        b.fillBox(7, 1, -12, 12, 4, -7, Material.AIR);
        b.setBlock(8, 1, -10, Material.ANVIL);
        b.setBlock(10, 1, -10, Material.CAULDRON);
        b.setBlock(9, 1, -8, Material.IRON_CHAIN);
        b.setBlock(9, 2, -8, Material.IRON_CHAIN);
        b.setBlock(11, 1, -8, Material.SOUL_LANTERN);

        // Warden's office (NW)
        b.fillBox(-13, 1, -13, -6, 5, -6, Material.DEEPSLATE_BRICKS);
        b.fillBox(-12, 1, -12, -7, 4, -7, Material.AIR);
        b.setBlock(-7, 1, -9, Material.AIR); b.setBlock(-7, 2, -9, Material.AIR); // Door
        b.table(-10, 1, -10);
        b.chair(-9, 1, -10, Material.OAK_STAIRS, BlockFace.WEST);
        b.setBlock(-11, 1, -11, Material.BOOKSHELF);
        b.setBlock(-11, 2, -11, Material.BOOKSHELF);
        b.setBlock(-10, 3, -10, Material.LANTERN);

        // Level 2: Corridors (below)
        b.fillBox(-14, -8, -14, 14, -1, 14, Material.DEEPSLATE_BRICKS);
        b.fillBox(-2, -7, -13, 2, -2, 13, Material.AIR);  // Main corridor
        b.fillBox(-13, -7, -2, 13, -2, 2, Material.AIR);  // Cross corridor
        b.fillFloor(-13, -13, 13, 13, -8, Material.DEEPSLATE_TILES);

        // Stairway between levels
        for (int s = 0; s < 7; s++) {
            b.setBlock(5 + s, -s, 0, Material.DEEPSLATE_BRICK_STAIRS);
        }

        // Spawner rooms on level 2
        b.fillBox(-12, -7, -12, -4, -3, -4, Material.AIR);
        b.setBlock(-8, -7, -8, Material.MOSSY_COBBLESTONE);
        b.fillBox(4, -7, 4, 12, -3, 12, Material.AIR);
        b.setBlock(8, -7, 8, Material.MOSSY_COBBLESTONE);

        // Flooded section (SW)
        b.fillBox(-12, -7, 4, -4, -5, 12, Material.AIR);
        b.fillBox(-12, -7, 4, -4, -7, 12, Material.WATER);

        // Level 3: Treasure vault (deepest)
        b.fillBox(-8, -14, -8, 8, -9, 8, Material.DEEPSLATE_BRICKS);
        b.fillBox(-7, -13, -7, 7, -9, 7, Material.AIR);
        b.fillFloor(-7, -7, 7, 7, -14, Material.OBSIDIAN);
        b.setBlock(0, -13, 0, Material.GOLD_BLOCK);
        b.setBlock(-4, -13, -4, Material.SOUL_LANTERN);
        b.setBlock(4, -13, 4, Material.SOUL_LANTERN);

        // Cave-in decoration
        b.scatter(-13, -7, -13, 13, -2, 13, Material.AIR, Material.COBBLESTONE, 0.04);

        // Lighting
        b.wallLighting(-13, 4, -14, 13, -14, 5, Material.SOUL_LANTERN);
        b.wallLighting(-13, 4, 14, 13, 14, 5, Material.SOUL_LANTERN);

        b.placeChest(-10, 1, -10);
        b.placeChest(0, -13, 4);
        b.placeChest(10, -7, 0);
        b.setBossSpawn(0, 1, 0);
    }
}

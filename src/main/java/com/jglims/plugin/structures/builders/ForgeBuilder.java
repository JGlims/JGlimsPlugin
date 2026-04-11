package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

/**
 * ForgeBuilder — a dwarven-style mountain forge carved into a hillside, featuring a
 * gothic arched entrance, central anvil room, lava channels, piston bellows, smithing
 * workstations, a chimney shaft and chandeliers.
 *
 * <p>Extracted verbatim from the original {@code OverworldStructureBuilders.buildForge}
 * method to give the structure its own file per the one-class-per-structure
 * architecture.
 */
public final class ForgeBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // Mountain exterior (carved into hillside)
        for (int y = 0; y < 20; y++) {
            int r = 25 - (y * 25 / 20);
            if (r < 3) r = 3;
            b.filledCircle(0, y, 0, r, Material.STONE);
        }
        b.scatter(-25, 0, -25, 25, 20, 25, Material.STONE, Material.COBBLESTONE, 0.15);
        b.scatter(-25, 0, -25, 25, 20, 25, Material.STONE, Material.ANDESITE, 0.1);

        // Grand entrance (carved archway)
        b.fillBox(-4, 1, -26, 4, 8, -20, Material.AIR);
        b.gothicArch(0, 1, -25, 8, 9, Material.DEEPSLATE_BRICKS);
        // Entrance pillars
        b.pillar(-5, 1, 8, -25, Material.POLISHED_DEEPSLATE);
        b.pillar(5, 1, 8, -25, Material.POLISHED_DEEPSLATE);

        // Main forge hall (hollowed interior)
        b.fillBox(-18, 1, -20, 18, 14, 15, Material.AIR);
        b.fillFloor(-18, -20, 18, 15, 0, Material.DEEPSLATE_TILES);

        // Huge anvil room (center)
        b.fillBox(-3, 1, -5, 3, 1, 5, Material.IRON_BLOCK);
        b.setBlock(-1, 2, 0, Material.ANVIL);
        b.setBlock(0, 2, 0, Material.ANVIL);
        b.setBlock(1, 2, 0, Material.ANVIL);
        b.setBlock(0, 2, -2, Material.ANVIL);
        b.setBlock(0, 2, 2, Material.ANVIL);
        // Decorative iron block structure
        b.pillar(-3, 2, 4, 0, Material.IRON_BLOCK);
        b.pillar(3, 2, 4, 0, Material.IRON_BLOCK);

        // Lava channels (running along the sides)
        for (int z = -15; z <= 10; z++) {
            b.setBlock(-15, 0, z, Material.LAVA);
            b.setBlock(15, 0, z, Material.LAVA);
        }
        // Lava falls
        b.setBlock(-15, 8, -10, Material.LAVA);
        b.setBlock(15, 8, 5, Material.LAVA);

        // Bellows (pistons)
        b.setDirectional(-6, 1, 0, Material.PISTON, BlockFace.EAST);
        b.setDirectional(6, 1, 0, Material.PISTON, BlockFace.WEST);
        b.setDirectional(-6, 2, 0, Material.PISTON, BlockFace.EAST);
        b.setDirectional(6, 2, 0, Material.PISTON, BlockFace.WEST);

        // Smithing tables and workstations
        b.setBlock(-10, 1, -8, Material.SMITHING_TABLE);
        b.setBlock(-9, 1, -8, Material.SMITHING_TABLE);
        b.setBlock(-10, 1, -6, Material.GRINDSTONE);
        b.setBlock(-9, 1, -6, Material.BLAST_FURNACE);
        b.setBlock(10, 1, -8, Material.SMITHING_TABLE);
        b.setBlock(9, 1, -8, Material.CRAFTING_TABLE);
        b.setBlock(10, 1, -6, Material.CARTOGRAPHY_TABLE);

        // Weapon racks (armor stands)
        for (int z = 5; z <= 12; z += 2) {
            b.setBlock(-16, 1, z, Material.LIGHTNING_ROD);
            b.setBlock(16, 1, z, Material.LIGHTNING_ROD);
        }

        // Support pillars
        for (int[] p : new int[][]{{-12, -12}, {12, -12}, {-12, 8}, {12, 8}}) {
            b.pillar(p[0], 1, 14, p[1], Material.DEEPSLATE_BRICK_WALL);
            b.setBlock(p[0], 14, p[1], Material.LANTERN);
        }

        // Chimney (vertical shaft to surface)
        b.fillBox(-2, 14, -2, 2, 25, 2, Material.AIR);
        b.fillWalls(-3, 14, -3, 3, 25, 3, Material.DEEPSLATE_BRICKS);
        b.setBlock(0, 25, 0, Material.CAMPFIRE);

        // Chandeliers (chains + lanterns)
        b.chandelier(-8, 14, -5, 4);
        b.chandelier(8, 14, -5, 4);
        b.chandelier(0, 14, 5, 5);

        // Storage area (barrels and chests)
        for (int x = -16; x <= -12; x++) {
            b.setBlock(x, 1, -16, Material.BARREL);
            b.setBlock(x, 2, -16, Material.BARREL);
        }

        // Magma block floor accents
        b.scatter(-18, 0, -20, 18, 0, 15, Material.DEEPSLATE_TILES, Material.MAGMA_BLOCK, 0.05);

        b.placeChest(0, 1, -10);
        b.placeChest(-14, 1, -14);
        b.placeChest(14, 1, 10);
        b.setBossSpawn(0, 2, 3);
    }
}

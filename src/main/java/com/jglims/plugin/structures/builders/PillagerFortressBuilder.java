package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

/**
 * PillagerFortressBuilder — a cobblestone pillager stronghold with corner watchtowers,
 * banner-adorned gate, barracks, armory, command tent, courtyard campfire and an
 * underground prison.
 *
 * <p>Extracted verbatim from the original {@code OverworldStructureBuilders.buildPillagerFortress}
 * method to give the structure its own file per the one-class-per-structure
 * architecture.
 */
public final class PillagerFortressBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // Outer palisade walls
        b.fillWalls(-28, 0, -28, 28, 10, 28, Material.COBBLESTONE);
        b.hollowBox(-28, 1, -28, 28, 10, 28);
        b.fillFloor(-27, -27, 27, 27, 0, Material.STONE_BRICKS);

        // Battlements
        for (int x = -28; x <= 28; x += 2) {
            b.setBlock(x, 11, -28, Material.COBBLESTONE_WALL);
            b.setBlock(x, 11, 28, Material.COBBLESTONE_WALL);
        }
        for (int z = -28; z <= 28; z += 2) {
            b.setBlock(-28, 11, z, Material.COBBLESTONE_WALL);
            b.setBlock(28, 11, z, Material.COBBLESTONE_WALL);
        }

        // Corner watchtowers (4)
        for (int[] c : new int[][]{{-28, -28}, {28, -28}, {-28, 28}, {28, 28}}) {
            b.fillBox(c[0] - 3, 0, c[1] - 3, c[0] + 3, 16, c[1] + 3, Material.STONE_BRICKS);
            b.fillBox(c[0] - 2, 1, c[1] - 2, c[0] + 2, 15, c[1] + 2, Material.AIR);
            b.fillBox(c[0] - 4, 14, c[1] - 4, c[0] + 4, 14, c[1] + 4, Material.DARK_OAK_PLANKS);
            b.setBlock(c[0], 15, c[1], Material.LANTERN);
            for (int y = 1; y <= 14; y++) b.setBlock(c[0] + 1, y, c[1] - 2, Material.LADDER);
        }

        // Main gate
        b.fillBox(-3, 1, -28, 3, 7, -28, Material.AIR);
        b.setBlock(-4, 8, -28, Material.DARK_OAK_LOG); b.setBlock(4, 8, -28, Material.DARK_OAK_LOG);
        b.fillBox(-3, 7, -28, 3, 7, -28, Material.DARK_OAK_PLANKS);

        // Pillager banners
        for (int x = -2; x <= 2; x += 2) b.setBlock(x, 8, -27, Material.WHITE_BANNER);

        // Central courtyard
        b.filledCircle(0, 0, 0, 8, Material.SMOOTH_STONE);
        b.setBlock(0, 1, 0, Material.CAMPFIRE);
        b.pillar(-6, 1, 4, 0, Material.DARK_OAK_FENCE); b.pillar(6, 1, 4, 0, Material.DARK_OAK_FENCE);

        // Barracks (left side)
        b.fillBox(-25, 0, -15, -15, 0, -5, Material.STONE_BRICKS);
        b.fillWalls(-25, 1, -15, -15, 5, -5, Material.DARK_OAK_PLANKS);
        b.hollowBox(-25, 1, -15, -15, 5, -5);
        b.fillBox(-25, 6, -15, -15, 6, -5, Material.DARK_OAK_PLANKS);
        b.setBlock(-20, 1, -15, Material.AIR); b.setBlock(-20, 2, -15, Material.AIR);
        for (int bx = -24; bx <= -16; bx += 4) {
            b.setBlock(bx, 1, -12, Material.RED_BED);
            b.setBlock(bx, 1, -8, Material.RED_BED);
        }

        // Armory (right side)
        b.fillBox(15, 0, -15, 25, 0, -5, Material.STONE_BRICKS);
        b.fillWalls(15, 1, -15, 25, 5, -5, Material.COBBLESTONE);
        b.hollowBox(15, 1, -15, 25, 5, -5);
        b.fillBox(15, 6, -15, 25, 6, -5, Material.DARK_OAK_PLANKS);
        b.setBlock(20, 1, -15, Material.AIR); b.setBlock(20, 2, -15, Material.AIR);
        b.setBlock(16, 1, -13, Material.ANVIL); b.setBlock(17, 1, -13, Material.GRINDSTONE);
        b.setBlock(24, 1, -6, Material.BARREL); b.setBlock(23, 1, -6, Material.BARREL);

        // Command tent (rear)
        b.fillBox(-8, 0, 10, 8, 0, 22, Material.STONE_BRICKS);
        b.fillWalls(-8, 1, 10, 8, 6, 22, Material.DARK_OAK_PLANKS);
        b.hollowBox(-8, 1, 10, 8, 6, 22);
        b.fillBox(-8, 7, 10, 8, 7, 22, Material.DARK_OAK_PLANKS);
        b.setBlock(0, 1, 10, Material.AIR); b.setBlock(0, 2, 10, Material.AIR);
        b.setBlock(0, 1, 16, Material.CRAFTING_TABLE);
        b.setBlock(1, 1, 16, Material.CARTOGRAPHY_TABLE);
        b.banner(-7, 4, 10, Material.WHITE_BANNER);
        b.banner(7, 4, 10, Material.WHITE_BANNER);

        // Prison below
        b.fillBox(-10, -6, -10, 10, -1, 10, Material.STONE_BRICKS);
        b.fillBox(-9, -5, -9, 9, -1, 9, Material.AIR);
        for (int z = -8; z <= 8; z += 4) {
            b.setBlock(-8, -5, z, Material.IRON_BARS); b.setBlock(-8, -4, z, Material.IRON_BARS);
            b.setBlock(8, -5, z, Material.IRON_BARS); b.setBlock(8, -4, z, Material.IRON_BARS);
        }
        b.setBlock(0, -5, 0, Material.SOUL_LANTERN);

        // Banners on walls
        for (int x = -26; x <= 26; x += 8) {
            b.banner(x, 8, -27, Material.WHITE_BANNER);
            b.banner(x, 8, 27, Material.WHITE_BANNER);
        }

        b.placeChest(-22, 1, -10);
        b.placeChest(22, 1, -10);
        b.placeChest(0, 1, 18);
        b.placeChest(0, -5, 0);
        b.setBossSpawn(0, 1, 5);
    }
}

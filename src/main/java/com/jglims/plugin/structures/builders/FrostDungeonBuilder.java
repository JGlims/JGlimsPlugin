package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

/**
 * FrostDungeonBuilder — packed-ice main hall with frozen throne alcove, crystal formations, and powder snow traps.
 *
 * <p>Extracted verbatim from the original {@code OverworldStructureBuilders.buildFrostDungeon}
 * method to give the structure its own file per the one-class-per-structure
 * architecture.
 */
public final class FrostDungeonBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // Main hall
        b.fillWalls(-15, 0, -15, 15, 8, 15, Material.PACKED_ICE);
        b.hollowBox(-15, 1, -15, 15, 8, 15);
        b.fillFloor(-14, -14, 14, 14, 0, Material.BLUE_ICE);

        // Ice pillars
        for (int a = 0; a < 360; a += 45) {
            int px = (int) (10 * Math.cos(Math.toRadians(a)));
            int pz = (int) (10 * Math.sin(Math.toRadians(a)));
            b.pillar(px, 1, 7, pz, Material.PACKED_ICE);
            b.setBlock(px, 8, pz, Material.SEA_LANTERN);
        }

        // Frozen throne room (north alcove)
        b.fillBox(-5, 1, 8, 5, 5, 14, Material.AIR);
        b.fillBox(-5, 0, 8, 5, 0, 14, Material.BLUE_ICE);
        b.fillBox(-1, 1, 12, 1, 3, 12, Material.PACKED_ICE);
        b.setBlock(0, 4, 12, Material.SEA_LANTERN);
        // Throne
        b.setStairs(-1, 1, 13, Material.QUARTZ_STAIRS, BlockFace.EAST, false);
        b.setStairs(1, 1, 13, Material.QUARTZ_STAIRS, BlockFace.WEST, false);
        b.setBlock(0, 1, 13, Material.QUARTZ_BLOCK);
        b.setBlock(0, 2, 14, Material.QUARTZ_BLOCK);

        // Frost crystal formations
        for (int[] crystal : new int[][]{{-8, 5}, {8, -7}, {-6, -10}, {10, 3}}) {
            b.pillar(crystal[0], 1, 3, crystal[1], Material.ICE);
            b.setBlock(crystal[0], 4, crystal[1], Material.BLUE_ICE);
        }

        // Side corridors
        b.fillBox(-15, 1, -2, -20, 4, 2, Material.AIR);
        b.fillWalls(-20, 0, -5, -15, 5, 5, Material.PACKED_ICE);
        b.fillBox(15, 1, -2, 20, 4, 2, Material.AIR);
        b.fillWalls(15, 0, -5, 20, 5, 5, Material.PACKED_ICE);

        // Ice cell blocks in corridors
        b.setBlock(-18, 1, -4, Material.IRON_BARS); b.setBlock(-18, 2, -4, Material.IRON_BARS);
        b.setBlock(-18, 1, 4, Material.IRON_BARS); b.setBlock(-18, 2, 4, Material.IRON_BARS);
        b.setBlock(18, 1, -4, Material.IRON_BARS); b.setBlock(18, 2, -4, Material.IRON_BARS);
        b.setBlock(18, 1, 4, Material.IRON_BARS); b.setBlock(18, 2, 4, Material.IRON_BARS);

        // Powder snow traps
        b.scatter(-14, 0, -14, 14, 0, 14, Material.BLUE_ICE, Material.POWDER_SNOW, 0.08);

        // Blue ice accent details
        b.setBlock(-14, 4, -14, Material.BLUE_ICE);
        b.setBlock(14, 4, 14, Material.BLUE_ICE);
        b.setBlock(-14, 4, 14, Material.BLUE_ICE);
        b.setBlock(14, 4, -14, Material.BLUE_ICE);

        b.placeChest(0, 1, 10);
        b.placeChest(-18, 1, 0);
        b.placeChest(18, 1, 0);
        b.setBossSpawn(0, 1, 11);
    }
}

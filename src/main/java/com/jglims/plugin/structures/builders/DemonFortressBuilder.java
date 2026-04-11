package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;

/**
 * DemonFortressBuilder — Blackstone/nether brick hybrid, demonic symbols
 * (nether gold ore patterns), prison cells, forge room, altar, soul fire braziers.
 *
 * <p>Extracted verbatim from the original {@code DimensionStructureBuilders.buildDemonFortress}
 * method to give the structure its own file per the one-class-per-structure
 * architecture.
 */
public final class DemonFortressBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // Foundation
        b.fillBox(-18, 0, -18, 18, 0, 18, Material.BLACKSTONE);

        // Hybrid walls — alternating blackstone and nether brick layers
        for (int y = 1; y <= 14; y++) {
            Material wallMat = y % 3 == 0 ? Material.NETHER_BRICKS : Material.POLISHED_BLACKSTONE_BRICKS;
            b.fillBox(-18, y, -18, 18, y, -18, wallMat);
            b.fillBox(-18, y, 18, 18, y, 18, wallMat);
            b.fillBox(-18, y, -18, -18, y, 18, wallMat);
            b.fillBox(18, y, -18, 18, y, 18, wallMat);
        }
        b.hollowBox(-18, 1, -18, 18, 14, 18);

        // Floor with nether gold ore demonic symbol (pentagram-ish)
        b.fillFloor(-17, -17, 17, 17, 0, Material.BLACKSTONE);
        // Star pattern in nether gold ore
        for (int a = 0; a < 360; a += 72) {
            double rad1 = Math.toRadians(a);
            double rad2 = Math.toRadians(a + 144);
            int x1 = (int) (12 * Math.cos(rad1));
            int z1 = (int) (12 * Math.sin(rad1));
            int x2 = (int) (12 * Math.cos(rad2));
            int z2 = (int) (12 * Math.sin(rad2));
            b.flyingButtress(x1, 0, z1, x2, 0, z2, Material.NETHER_GOLD_ORE);
        }
        b.filledCircle(0, 0, 0, 3, Material.NETHER_GOLD_ORE);

        // Soul fire braziers (6 around perimeter)
        for (int a = 0; a < 360; a += 60) {
            int px = (int) (14 * Math.cos(Math.toRadians(a)));
            int pz = (int) (14 * Math.sin(Math.toRadians(a)));
            b.fillBox(px - 1, 0, pz - 1, px + 1, 2, pz + 1, Material.POLISHED_BLACKSTONE);
            b.setBlock(px, 3, pz, Material.SOUL_CAMPFIRE);
        }

        // Central altar — dark ritual platform
        b.fillBox(-3, 0, -3, 3, 3, 3, Material.CRYING_OBSIDIAN);
        b.fillBox(-2, 3, -2, 2, 3, 2, Material.OBSIDIAN);
        b.setBlock(0, 4, 0, Material.SOUL_CAMPFIRE);
        b.setBlock(-2, 4, -2, Material.CANDLE);
        b.setBlock(2, 4, -2, Material.CANDLE);
        b.setBlock(-2, 4, 2, Material.CANDLE);
        b.setBlock(2, 4, 2, Material.CANDLE);

        // Prison cells (NE corner)
        for (int cell = 0; cell < 3; cell++) {
            int cx = 10 + cell * 3;
            b.fillBox(cx, 0, -16, cx + 2, 5, -12, Material.NETHER_BRICKS);
            b.fillBox(cx, 1, -15, cx + 2, 4, -13, Material.AIR);
            for (int y = 1; y <= 4; y++) b.setBlock(cx, y, -12, Material.IRON_BARS);
            b.setBlock(cx + 1, 4, -14, Material.SOUL_LANTERN);
        }

        // Forge room (SW corner)
        b.fillBox(-17, 0, 10, -10, 8, 17, Material.NETHER_BRICKS);
        b.fillBox(-16, 1, 11, -11, 7, 16, Material.AIR);
        b.fillFloor(-16, 11, -11, 16, 0, Material.BLACKSTONE);
        b.setBlock(-14, 1, 14, Material.BLAST_FURNACE);
        b.setBlock(-13, 1, 14, Material.SMITHING_TABLE);
        b.setBlock(-12, 1, 14, Material.ANVIL);
        b.setBlock(-15, 1, 12, Material.LAVA);
        b.setBlock(-15, 1, 13, Material.LAVA);
        b.setBlock(-14, 7, 13, Material.SOUL_LANTERN);

        // Entrance
        b.archDoorway(0, 1, -18, 6, 8, Material.RED_NETHER_BRICKS);

        // Ceiling
        b.pyramidRoof(-18, -18, 18, 18, 14, Material.NETHER_BRICKS);

        // Interior pillars
        for (int x = -12; x <= 12; x += 8)
            for (int z = -12; z <= 12; z += 8) {
                if (Math.abs(x) < 5 && Math.abs(z) < 5) continue;
                b.pillar(x, 1, 13, z, Material.POLISHED_BLACKSTONE_BRICKS);
                b.setBlock(x, 13, z, Material.SOUL_LANTERN);
            }

        b.placeChest(-14, 1, 12);
        b.placeChest(14, 1, -14);
        b.placeChest(0, 4, 2);
        b.setBossSpawn(0, 4, 5);
    }
}

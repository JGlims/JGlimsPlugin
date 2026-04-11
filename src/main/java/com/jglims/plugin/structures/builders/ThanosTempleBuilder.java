package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;

/**
 * ThanosTempleBuilder — obsidian-and-purpur stepped pyramid with throne, six Infinity Stone pedestals, and underground vault.
 *
 * <p>Extracted verbatim from the original {@code OverworldStructureBuilders.buildThanosTemple}
 * method to give the structure its own file per the one-class-per-structure
 * architecture.
 */
public final class ThanosTempleBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // Stepped pyramid base (5 steps)
        for (int step = 0; step < 5; step++) {
            int r = 22 - step * 4;
            int y = step * 4;
            b.fillBox(-r, y, -r, r, y, r, Material.OBSIDIAN);
            b.fillWalls(-r, y + 1, -r, r, y + 3, r, Material.PURPUR_BLOCK);
            b.hollowBox(-r, y + 1, -r, r, y + 3, r);
        }

        // Top platform
        b.fillBox(-6, 20, -6, 6, 20, 6, Material.PURPUR_BLOCK);
        b.fillBox(-5, 21, -5, 5, 21, 5, Material.OBSIDIAN);

        // Purple glass dome
        b.dome(0, 22, 0, 6, Material.PURPLE_STAINED_GLASS);

        // Throne
        b.fillBox(-1, 21, 2, 1, 21, 4, Material.GOLD_BLOCK);
        b.setBlock(0, 22, 3, Material.GOLD_BLOCK); b.setBlock(0, 23, 3, Material.GOLD_BLOCK);
        b.setBlock(-1, 23, 3, Material.GOLD_BLOCK); b.setBlock(1, 23, 3, Material.GOLD_BLOCK);
        b.setBlock(0, 24, 3, Material.GOLD_BLOCK);

        // 6 Infinity Stone pedestals
        Material[] stoneMats = {Material.RED_CONCRETE, Material.BLUE_CONCRETE, Material.YELLOW_CONCRETE,
                Material.ORANGE_CONCRETE, Material.GREEN_CONCRETE, Material.PURPLE_CONCRETE};
        int[][] pedestals = {{-4, 0}, {4, 0}, {0, -4}, {0, 4}, {-3, -3}, {3, 3}};
        for (int i = 0; i < 6; i++) {
            b.setBlock(pedestals[i][0], 21, pedestals[i][1], Material.QUARTZ_PILLAR);
            b.setBlock(pedestals[i][0], 22, pedestals[i][1], Material.QUARTZ_PILLAR);
            b.setBlock(pedestals[i][0], 23, pedestals[i][1], stoneMats[i]);
        }

        // Entrance
        b.fillBox(-3, 1, -22, 3, 6, -22, Material.AIR);
        b.setBlock(-4, 7, -22, Material.PURPUR_PILLAR); b.setBlock(4, 7, -22, Material.PURPUR_PILLAR);

        // Grand staircase
        for (int s = 0; s < 20; s++) {
            b.fillBox(-2, s, -20 + s, 2, s, -20 + s, Material.PURPUR_STAIRS);
        }

        // Imposing columns along the staircase
        for (int s = 0; s < 20; s += 5) {
            b.pillar(-4, s, s + 5, -20 + s, Material.PURPUR_PILLAR);
            b.pillar(4, s, s + 5, -20 + s, Material.PURPUR_PILLAR);
        }

        // Soul lantern lighting
        for (int a = 0; a < 360; a += 45) {
            int lx = (int) (5 * Math.cos(Math.toRadians(a)));
            int lz = (int) (5 * Math.sin(Math.toRadians(a)));
            b.setBlock(lx, 22, lz, Material.SOUL_LANTERN);
        }

        // Underground vault
        b.fillBox(-15, -8, -15, 15, -1, 15, Material.OBSIDIAN);
        b.fillBox(-14, -7, -14, 14, -1, 14, Material.AIR);
        b.fillFloor(-14, -14, 14, 14, -8, Material.CRYING_OBSIDIAN);
        for (int[] c : new int[][]{{-10, -10}, {10, -10}, {-10, 10}, {10, 10}}) {
            b.pillar(c[0], -7, -1, c[1], Material.PURPUR_PILLAR);
            b.setBlock(c[0], -1, c[1], Material.END_ROD);
        }
        b.setBlock(0, -7, 0, Material.BEACON);

        // Decay
        b.decay(-22, 8, -22, 22, 20, 22, 0.08);
        b.scatter(-22, 0, -22, 22, 20, 22, Material.PURPUR_BLOCK, Material.CRACKED_STONE_BRICKS, 0.06);

        b.placeChest(0, 21, 0);
        b.placeChest(-12, -7, 0);
        b.placeChest(12, -7, 0);
        b.setBossSpawn(0, 21, -2);
    }
}

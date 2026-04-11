package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;

/**
 * WarriorSkyFortressBuilder — MASSIVE floating fortress: 100x50, quartz walls,
 * golden trim, grand entrance, throne room, sky bridges, open-air arena,
 * banner-adorned walls.
 *
 * <p>Extracted verbatim from the original {@code DimensionStructureBuilders.buildWarriorSkyFortress}
 * method to give the structure its own file per the one-class-per-structure
 * architecture.
 */
public final class WarriorSkyFortressBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // Main platform
        b.fillBox(-50, 0, -25, 50, 0, 25, Material.QUARTZ_BLOCK);
        b.fillBox(-48, 0, -23, 48, 0, 23, Material.QUARTZ_BRICKS);

        // Outer walls
        b.fillWalls(-50, 1, -25, 50, 16, 25, Material.QUARTZ_BLOCK);
        b.hollowBox(-50, 1, -25, 50, 16, 25);

        // Gold trim bands
        for (int y : new int[]{1, 8, 16}) {
            b.fillBox(-50, y, -25, 50, y, -25, Material.GOLD_BLOCK);
            b.fillBox(-50, y, 25, 50, y, 25, Material.GOLD_BLOCK);
            b.fillBox(-50, y, -25, -50, y, 25, Material.GOLD_BLOCK);
            b.fillBox(50, y, -25, 50, y, 25, Material.GOLD_BLOCK);
        }

        // Grand entrance with twin towers (south)
        b.fillBox(-5, 1, -25, 5, 12, -25, Material.AIR);
        b.gothicArch(0, 1, -25, 10, 13, Material.GOLD_BLOCK);

        // Twin entrance towers
        b.roundTower(-12, 0, -25, 5, 22, Material.QUARTZ_BLOCK,
                Material.GOLD_BLOCK, Material.YELLOW_STAINED_GLASS);
        b.roundTower(12, 0, -25, 5, 22, Material.QUARTZ_BLOCK,
                Material.GOLD_BLOCK, Material.YELLOW_STAINED_GLASS);

        // Battlements on all walls
        b.battlements(-50, 17, -25, 50, -25, Material.QUARTZ_BLOCK);
        b.battlements(-50, 17, 25, 50, 25, Material.QUARTZ_BLOCK);
        b.battlements(-50, 17, -25, -50, 25, Material.QUARTZ_BLOCK);
        b.battlements(50, 17, -25, 50, 25, Material.QUARTZ_BLOCK);

        // Banners on walls
        for (int x = -44; x <= 44; x += 8) {
            b.setBlock(x, 12, -25, Material.WHITE_BANNER);
            b.setBlock(x, 12, 25, Material.WHITE_BANNER);
        }

        // Throne room (center-north)
        b.fillBox(-12, 0, 8, 12, 20, 24, Material.QUARTZ_BRICKS);
        b.fillBox(-11, 1, 9, 11, 19, 23, Material.AIR);
        b.fillFloor(-11, 9, 11, 23, 0, Material.GOLD_BLOCK);
        // Throne
        b.fillBox(-2, 1, 20, 2, 3, 22, Material.GOLD_BLOCK);
        b.setBlock(0, 4, 21, Material.GOLD_BLOCK);
        b.setBlock(0, 5, 21, Material.GOLD_BLOCK);
        b.setBlock(-1, 5, 21, Material.GOLD_BLOCK);
        b.setBlock(1, 5, 21, Material.GOLD_BLOCK);
        // Carpet runner
        for (int z = 9; z <= 20; z++) b.setBlock(0, 1, z, Material.RED_CARPET);
        // Pillars
        for (int x = -8; x <= 8; x += 4) {
            b.pillar(x, 1, 18, 12, Material.QUARTZ_PILLAR);
        }
        b.chandelier(0, 20, 16, 4);

        // Open-air arena at top (rooftop)
        b.fillBox(-30, 16, -20, -10, 16, 0, Material.QUARTZ_BLOCK);
        b.filledCircle(-20, 16, -10, 10, Material.QUARTZ_BRICKS);
        // Arena pillars
        for (int a = 0; a < 360; a += 45) {
            int px = -20 + (int) (8 * Math.cos(Math.toRadians(a)));
            int pz = -10 + (int) (8 * Math.sin(Math.toRadians(a)));
            b.pillar(px, 17, 22, pz, Material.QUARTZ_PILLAR);
            b.setBlock(px, 22, pz, Material.END_ROD);
        }

        // Sky bridges (connecting sections)
        b.fillBox(-1, 10, -5, 1, 10, 8, Material.QUARTZ_BLOCK);
        b.fillBox(-1, 11, -5, 1, 11, 8, Material.AIR);

        // Side rooms (barracks east, armory west)
        // Barracks
        b.furnishedRoom(20, 0, -18, 45, -5, 4,
                Material.QUARTZ_BLOCK, Material.QUARTZ_BRICKS, Material.QUARTZ_BLOCK);
        for (int z = -16; z <= -8; z += 4) {
            b.setBlock(25, 1, z, Material.WHITE_BED);
            b.setBlock(35, 1, z, Material.WHITE_BED);
        }

        // Armory
        b.furnishedRoom(-45, 0, -18, -20, -5, 4,
                Material.QUARTZ_BLOCK, Material.QUARTZ_BRICKS, Material.QUARTZ_BLOCK);
        b.setBlock(-35, 1, -15, Material.SMITHING_TABLE);
        b.setBlock(-33, 1, -15, Material.ANVIL);
        b.setBlock(-31, 1, -15, Material.GRINDSTONE);

        // Floor pattern
        for (int x = -45; x <= 45; x += 4)
            for (int z = -20; z <= 20; z += 4)
                b.setBlock(x, 0, z, Material.GOLD_BLOCK);

        // Windows
        for (int x = -44; x <= 44; x += 6) {
            for (int y = 5; y <= 12; y++) {
                b.setBlock(x, y, -25, Material.YELLOW_STAINED_GLASS);
                b.setBlock(x, y, 25, Material.YELLOW_STAINED_GLASS);
            }
        }

        b.placeChest(0, 1, 20);
        b.placeChest(-35, 1, -12);
        b.placeChest(35, 1, -12);
        b.placeChest(-20, 17, -10);
        b.setBossSpawn(0, 1, 15);
    }
}

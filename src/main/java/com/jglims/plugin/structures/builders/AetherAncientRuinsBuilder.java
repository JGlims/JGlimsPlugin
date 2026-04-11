package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;

/**
 * AetherAncientRuinsBuilder — crumbling quartz ruins with broken columns, hidden altar, and overgrowth.
 *
 * <p>Extracted verbatim from the original monolithic builder method.
 */
public final class AetherAncientRuinsBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // Foundation remnants
        b.fillBox(-14, 0, -14, 14, 0, 14, Material.QUARTZ_BLOCK);
        b.scatter(-14, 0, -14, 14, 0, 14, Material.QUARTZ_BLOCK, Material.CRACKED_STONE_BRICKS, 0.2);

        // Broken columns (varying heights, some fallen)
        int[][] colPos = {{-10, -10}, {10, -10}, {-10, 10}, {10, 10},
                {-10, 0}, {10, 0}, {0, -10}, {0, 10}};
        for (int i = 0; i < colPos.length; i++) {
            int height = (i < 4) ? 3 + (i % 3) : 6 + (i % 2); // varying
            b.pillar(colPos[i][0], 1, height, colPos[i][1], Material.QUARTZ_PILLAR);
        }
        // Fallen column on ground
        b.fillBox(-5, 1, -8, -5, 1, -3, Material.QUARTZ_PILLAR);
        b.fillBox(3, 1, 5, 8, 1, 5, Material.QUARTZ_PILLAR);

        // Partial walls (ruins of rooms)
        b.fillBox(-12, 1, -12, -8, 4, -12, Material.QUARTZ_BLOCK);
        b.fillBox(-12, 1, -12, -12, 3, -8, Material.QUARTZ_BLOCK);
        b.decay(-12, 1, -12, -8, 4, -8, 0.3);

        b.fillBox(8, 1, 8, 12, 5, 12, Material.QUARTZ_BLOCK);
        b.fillBox(9, 1, 9, 11, 4, 11, Material.AIR);
        b.decay(8, 3, 8, 12, 5, 12, 0.25);

        // Overgrown (flowers everywhere)
        for (int x = -12; x <= 12; x += 2)
            for (int z = -12; z <= 12; z += 2)
                if (b.getBlock(x, 1, z) == Material.AIR)
                    b.setRandomBlock(x, 1, z, Material.DANDELION, Material.POPPY,
                            Material.AZURE_BLUET, Material.CORNFLOWER, Material.OXEYE_DAISY,
                            Material.GRASS_BLOCK);

        // Vine/moss coverage
        b.addVines(-12, 1, -12, 12, 6, 12, 0.15);
        b.scatter(-14, 0, -14, 14, 0, 14, Material.QUARTZ_BLOCK, Material.MOSS_BLOCK, 0.1);

        // Hidden chamber (underground)
        b.fillBox(-5, -5, -5, 5, -1, 5, Material.QUARTZ_BLOCK);
        b.fillBox(-4, -4, -4, 4, -1, 4, Material.AIR);
        b.fillFloor(-4, -4, 4, 4, -5, Material.QUARTZ_BRICKS);
        // Entrance (broken floor section)
        b.setBlock(0, 0, 0, Material.AIR);
        b.setBlock(0, -1, 0, Material.AIR);

        // ─── Ancient Portal Mimic altar in the hidden chamber ───
        // (This is an epic boss — the altar is the focal combat point)
        b.fillBox(-2, -4, -2, 2, -4, 2, Material.POLISHED_BLACKSTONE);
        b.fillBox(-1, -3, -1, 1, -3, 1, Material.CRYING_OBSIDIAN);
        b.setBlock(0, -2, 0, Material.CHISELED_QUARTZ_BLOCK);
        b.setBlock(0, -1, 0, Material.END_PORTAL_FRAME);
        // Runic inlay around the altar base
        for (int i = 0; i < 8; i++) {
            int a = i * 45;
            int rx = (int) Math.round(3 * Math.cos(Math.toRadians(a)));
            int rz = (int) Math.round(3 * Math.sin(Math.toRadians(a)));
            b.setBlock(rx, -4, rz, Material.CHISELED_QUARTZ_BLOCK);
        }

        // ─── Second broken chamber above ground (east): collapsed library ───
        b.fillBox(8, 1, -10, 13, 6, -5, Material.QUARTZ_BRICKS);
        b.fillBox(9, 1, -9, 12, 5, -6, Material.AIR);
        for (int z = -9; z <= -6; z++) b.setBlock(9, 2, z, Material.BOOKSHELF);
        for (int z = -9; z <= -6; z++) b.setBlock(12, 2, z, Material.BOOKSHELF);
        b.setBlock(11, 2, -8, Material.LECTERN);
        b.decay(8, 4, -10, 13, 6, -5, 0.4);
        b.addVines(8, 1, -10, 13, 6, -5, 0.2);

        // ─── Third chamber (west): shrine with broken statues ───
        b.fillBox(-13, 1, 5, -8, 6, 10, Material.QUARTZ_BRICKS);
        b.fillBox(-12, 1, 6, -9, 5, 9, Material.AIR);
        b.pillar(-11, 1, 4, 7, Material.QUARTZ_PILLAR);  // broken statue
        b.pillar(-10, 1, 3, 8, Material.QUARTZ_PILLAR);  // broken statue
        b.setBlock(-11, 5, 7, Material.PLAYER_HEAD);  // headless statue
        b.setBlock(-10, 4, 8, Material.SOUL_LANTERN);
        b.decay(-13, 3, 5, -8, 6, 10, 0.3);

        // ─── Fallen god statue in the middle of the ruin (large) ───
        b.fillBox(-3, 1, -1, 3, 1, 0, Material.QUARTZ_BLOCK);  // torso lying down
        b.setBlock(-2, 2, 0, Material.QUARTZ_PILLAR);  // upper arm
        b.setBlock(2, 2, 0, Material.QUARTZ_PILLAR);
        b.setBlock(3, 1, 1, Material.CHISELED_QUARTZ_BLOCK);  // face/head lying on side

        // ─── Ancient bone pile (past adventurers) ───
        b.setBlock(6, 1, -3, Material.BONE_BLOCK);
        b.setBlock(6, 2, -3, Material.SKELETON_SKULL);
        b.setBlock(-7, 1, 3, Material.BONE_BLOCK);
        b.setBlock(-7, 2, 3, Material.SKELETON_SKULL);

        // Treasure in rubble
        b.placeChest(-10, 0, -10);
        b.placeChest(10, 1, 10);
        b.placeChest(0, -4, 0);
        b.placeChest(11, 2, -8);   // library chest
        b.placeChest(-10, 2, 8);   // shrine chest
        b.setBossSpawn(0, -3, 0);  // boss spawns at altar
    }
}

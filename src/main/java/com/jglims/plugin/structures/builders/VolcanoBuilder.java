package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;

/**
 * VolcanoBuilder — 40-block basalt cone with lava core, obsidian treasure chamber, Magma Titan ritual arena, and forge.
 *
 * <p>Extracted verbatim from the original {@code OverworldStructureBuilders.buildVolcano}
 * method to give the structure its own file per the one-class-per-structure
 * architecture.
 */
public final class VolcanoBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // Cone shape (40 blocks high)
        for (int y = 0; y < 40; y++) {
            int r = 20 - (y * 20 / 40);
            if (r < 2) r = 2;
            // Outer shell: basalt
            b.filledCircle(0, y, 0, r, Material.BASALT);
            // Inner core: blackstone
            if (r > 4) b.filledCircle(0, y, 0, r - 3, Material.BLACKSTONE);
        }

        // Lava core
        for (int y = 1; y < 38; y++) {
            int r = Math.max(1, 5 - (y / 8));
            b.filledCircle(0, y, 0, r, Material.LAVA);
        }

        // Lava pool at top (crater)
        b.filledCircle(0, 38, 0, 3, Material.LAVA);
        b.filledCircle(0, 39, 0, 2, Material.LAVA);

        // Magma block veins running down exterior
        b.scatter(-20, 0, -20, 20, 40, 20, Material.BASALT, Material.MAGMA_BLOCK, 0.12);

        // Obsidian chamber at base (treasure room)
        b.fillBox(-6, 3, -6, 6, 8, 6, Material.AIR);
        b.fillBox(-6, 2, -6, 6, 2, 6, Material.OBSIDIAN);
        b.fillWalls(-6, 3, -6, 6, 8, 6, Material.OBSIDIAN);
        // Don't fill interior with obsidian
        b.fillBox(-5, 3, -5, 5, 7, 5, Material.AIR);

        // Lava channels inside chamber
        b.setBlock(-5, 3, 0, Material.LAVA);
        b.setBlock(5, 3, 0, Material.LAVA);
        b.setBlock(0, 3, -5, Material.LAVA);
        b.setBlock(0, 3, 5, Material.LAVA);

        // Treasure in chamber
        b.setBlock(0, 3, 0, Material.GOLD_BLOCK);
        b.setBlock(-2, 3, -2, Material.DIAMOND_BLOCK);

        // Lava tube entrance
        b.fillBox(-2, 3, -8, 2, 5, -6, Material.AIR);

        // Smoke area at top (soul campfires)
        b.setBlock(-1, 40, 0, Material.SOUL_CAMPFIRE);
        b.setBlock(1, 40, 0, Material.SOUL_CAMPFIRE);
        b.setBlock(0, 40, -1, Material.SOUL_CAMPFIRE);

        // Decorative obsidian outcrops
        b.setBlock(-12, 8, -12, Material.CRYING_OBSIDIAN);
        b.setBlock(10, 5, 10, Material.CRYING_OBSIDIAN);
        b.setBlock(-8, 12, 8, Material.CRYING_OBSIDIAN);

        // Lanterns in treasure cave
        b.setBlock(-4, 6, -4, Material.LANTERN);
        b.setBlock(4, 6, 4, Material.LANTERN);

        // ─── Magma Titan boss arena: ritual pit inside the chamber ───
        // Sunken ritual floor with magma runes
        for (int a = 0; a < 360; a += 1) {
            int rx = (int) Math.round(4 * Math.cos(Math.toRadians(a)));
            int rz = (int) Math.round(4 * Math.sin(Math.toRadians(a)));
            b.setBlock(rx, 2, rz, Material.MAGMA_BLOCK);
        }
        // Four forge pillars with hanging chains
        for (int[] p : new int[][]{{-4, -4}, {4, -4}, {-4, 4}, {4, 4}}) {
            b.pillar(p[0], 3, 7, p[1], Material.POLISHED_BLACKSTONE);
            b.setBlock(p[0], 7, p[1], Material.ANVIL);
            b.setBlock(p[0], 4, p[1], Material.IRON_CHAIN);
            b.setBlock(p[0], 5, p[1], Material.IRON_CHAIN);
            b.setBlock(p[0], 6, p[1], Material.LANTERN);
        }

        // ─── Exterior: basalt obelisks ringing the volcano base ───
        for (int i = 0; i < 8; i++) {
            int a = i * 45;
            int bx = (int) Math.round(22 * Math.cos(Math.toRadians(a)));
            int bz = (int) Math.round(22 * Math.sin(Math.toRadians(a)));
            b.pillar(bx, 0, 5, bz, Material.BASALT);
            b.setBlock(bx, 5, bz, Material.MAGMA_BLOCK);
            b.setBlock(bx, 6, bz, Material.SOUL_FIRE);
        }

        // ─── Charred skeletons of past adventurers ───
        b.setBlock(-9, 0, -3, Material.SKELETON_SKULL);
        b.setBlock(9, 0, 4, Material.SKELETON_SKULL);
        b.setBlock(-2, 0, 10, Material.WITHER_SKELETON_SKULL);
        b.setBlock(-11, 0, 8, Material.BONE_BLOCK);
        b.setBlock(11, 0, -7, Material.BONE_BLOCK);

        // ─── Forge area east of chamber: anvil + smithing table ───
        b.fillBox(6, 3, -2, 8, 3, 2, Material.POLISHED_BLACKSTONE);
        b.fillBox(6, 4, -2, 8, 5, 2, Material.AIR);
        b.setBlock(7, 4, 0, Material.SMITHING_TABLE);
        b.setBlock(7, 4, -1, Material.ANVIL);
        b.setBlock(7, 4, 1, Material.GRINDSTONE);
        b.setBlock(6, 5, 0, Material.LANTERN);

        // ─── Obsidian altar with nether star hanging above ───
        b.fillBox(-2, 3, -2, 2, 3, 2, Material.CRYING_OBSIDIAN);
        b.setBlock(0, 4, 0, Material.RESPAWN_ANCHOR);
        b.setBlock(0, 7, 0, Material.END_ROD);

        // Decay/weathering on the entire structure
        b.scatter(-22, 0, -22, 22, 40, 22, Material.BASALT, Material.BLACKSTONE, 0.15);

        b.placeChest(0, 3, 2);
        b.placeChest(-3, 3, -3);
        b.placeChest(3, 3, 3);
        b.placeChest(7, 4, 2);   // Forge chest
        b.setBossSpawn(0, 3, 0);
    }
}

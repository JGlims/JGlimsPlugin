package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;

/**
 * DragonDeathChestBuilder — ceremonial monument marking the fall of the
 * Ender Dragon. Not a large arena, but a haunting, reverent tableau: a
 * black obsidian pedestal with the fabled loot chest, flanked by four
 * carved dragon-head pillars, a ring of end rods, and a halo of purpur
 * runes inscribed into the bedrock.
 */
public final class DragonDeathChestBuilder implements IStructureBuilder {

    @Override
    public void build(StructureBuilder b) {
        // ─── 1. Circular rune floor ──────────────────────────────
        b.filledCircle(0, 0, 0, 10, Material.END_STONE_BRICKS);
        b.circle(0, 0, 0, 10, Material.OBSIDIAN);
        b.circle(0, 0, 0,  9, Material.CRYING_OBSIDIAN);
        b.filledCircle(0, 0, 0, 7, Material.PURPUR_BLOCK);
        b.filledCircle(0, 0, 0, 4, Material.POLISHED_BLACKSTONE);

        // Rune etchings (radial lines)
        for (int a = 0; a < 360; a += 22) {
            int rx = (int) Math.round(6 * Math.cos(Math.toRadians(a)));
            int rz = (int) Math.round(6 * Math.sin(Math.toRadians(a)));
            b.setBlock(rx, 0, rz, Material.MAGENTA_GLAZED_TERRACOTTA);
        }

        // ─── 2. Central obsidian pedestal ─────────────────────────
        b.fillBox(-2, 1, -2, 2, 1, 2, Material.OBSIDIAN);
        b.fillBox(-1, 2, -1, 1, 2, 1, Material.CRYING_OBSIDIAN);
        b.setBlock(0, 3, 0, Material.OBSIDIAN);

        // ─── 3. Four tall dragon-head pillars at the corners ────
        int[][] corners = {{6, 6}, {-6, 6}, {6, -6}, {-6, -6}};
        for (int[] c : corners) {
            int x = c[0], z = c[1];
            // Pillar shaft — gradient from obsidian to purpur
            for (int y = 1; y <= 6; y++) {
                b.setBlock(x, y, z, y <= 3 ? Material.OBSIDIAN : Material.PURPUR_PILLAR);
            }
            // Dragon head crown
            b.setBlock(x, 7, z, Material.DRAGON_HEAD);
            // End-rod halo above
            b.setBlock(x, 9, z, Material.END_ROD);
        }

        // ─── 4. Ring of end rods at cardinals &amp; diagonals ─────
        for (int a = 0; a < 360; a += 30) {
            int rx = (int) Math.round(9 * Math.cos(Math.toRadians(a)));
            int rz = (int) Math.round(9 * Math.sin(Math.toRadians(a)));
            b.setBlock(rx, 1, rz, Material.END_ROD);
        }

        // ─── 5. Dragon egg centerpiece + the fabled chest ────────
        b.setBlock(0, 4, 0, Material.DRAGON_EGG);
        b.placeChest(2, 2, 0);
        b.placeChest(-2, 2, 0);
        b.placeChest(0, 2, 2);
        b.placeChest(0, 2, -2);

        // ─── 6. Hovering broken dragon spine (decorative) ────────
        b.setBlock(-4, 5,  0, Material.BONE_BLOCK);
        b.setBlock(-3, 5,  0, Material.BONE_BLOCK);
        b.setBlock(-2, 5,  0, Material.BONE_BLOCK);
        b.setBlock( 2, 5,  0, Material.BONE_BLOCK);
        b.setBlock( 3, 5,  0, Material.BONE_BLOCK);
        b.setBlock( 4, 5,  0, Material.BONE_BLOCK);
        b.setBlock( 0, 5, -4, Material.BONE_BLOCK);
        b.setBlock( 0, 5,  4, Material.BONE_BLOCK);

        // ─── 7. Soul-fire braziers at each corner ────────────────
        b.setBlock( 9, 1,  9, Material.SOUL_FIRE);
        b.setBlock(-9, 1,  9, Material.SOUL_FIRE);
        b.setBlock( 9, 1, -9, Material.SOUL_FIRE);
        b.setBlock(-9, 1, -9, Material.SOUL_FIRE);
    }
}

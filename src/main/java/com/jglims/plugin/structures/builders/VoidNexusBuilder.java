package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;

/**
 * VoidNexusBuilder — Floating obsidian platforms connected by thin bridges,
 * central nexus chamber with end gateway, amethyst crystals, soul fire,
 * 4 surrounding pylons.
 *
 * <p>Extracted verbatim from the original {@code DimensionStructureBuilders.buildVoidNexus}
 * method to give the structure its own file per the one-class-per-structure
 * architecture.
 */
public final class VoidNexusBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // Central platform (main nexus)
        b.filledCircle(0, 0, 0, 14, Material.OBSIDIAN);
        b.filledCircle(0, 1, 0, 12, Material.END_STONE);
        b.filledCircle(0, 2, 0, 10, Material.CRYING_OBSIDIAN);

        // Central nexus chamber (cylinder)
        for (int y = 3; y <= 20; y++) b.circle(0, y, 0, 6, Material.OBSIDIAN);
        // Clear interior
        for (int y = 3; y <= 19; y++) b.filledCircle(0, y, 0, 5, Material.AIR);
        // Dome top
        b.dome(0, 20, 0, 6, Material.OBSIDIAN);

        // End gateway portal at center
        b.setBlock(0, 5, 0, Material.END_GATEWAY);
        b.setBlock(0, 6, 0, Material.END_GATEWAY);
        b.setBlock(0, 4, 0, Material.END_GATEWAY);
        // Obsidian frame
        b.fillBox(-1, 3, -1, 1, 3, 1, Material.CRYING_OBSIDIAN);
        b.fillBox(-1, 7, -1, 1, 7, 1, Material.CRYING_OBSIDIAN);

        // Amethyst crystal formations around nexus interior
        for (int a = 0; a < 360; a += 45) {
            int cx = (int) (4 * Math.cos(Math.toRadians(a)));
            int cz = (int) (4 * Math.sin(Math.toRadians(a)));
            b.setBlock(cx, 3, cz, Material.AMETHYST_BLOCK);
            b.setBlock(cx, 4, cz, Material.AMETHYST_CLUSTER);
            if (a % 90 == 0) {
                b.setBlock(cx, 5, cz, Material.AMETHYST_BLOCK);
                b.setBlock(cx, 6, cz, Material.AMETHYST_CLUSTER);
            }
        }

        // Void energy (soul fire ring)
        b.circle(0, 3, 0, 3, Material.SOUL_CAMPFIRE);

        // Nexus entrance
        b.fillBox(-2, 3, -6, 2, 7, -6, Material.AIR);

        // 4 surrounding pylons (at cardinal directions, connected by bridges)
        int[][] pylonPos = {{0, -22}, {0, 22}, {-22, 0}, {22, 0}};
        for (int[] pp : pylonPos) {
            // Pylon platform
            b.filledCircle(pp[0], 0, pp[1], 6, Material.OBSIDIAN);
            b.filledCircle(pp[0], 1, pp[1], 4, Material.END_STONE);

            // Pylon tower
            b.pillar(pp[0], 2, 18, pp[1], Material.OBSIDIAN);
            b.setBlock(pp[0], 19, pp[1], Material.END_ROD);
            b.setBlock(pp[0], 20, pp[1], Material.END_ROD);

            // Amethyst decorations at base
            b.setBlock(pp[0] - 2, 2, pp[1], Material.AMETHYST_BLOCK);
            b.setBlock(pp[0] + 2, 2, pp[1], Material.AMETHYST_BLOCK);
            b.setBlock(pp[0], 2, pp[1] - 2, Material.AMETHYST_BLOCK);
            b.setBlock(pp[0], 2, pp[1] + 2, Material.AMETHYST_BLOCK);
            b.setBlock(pp[0] - 2, 3, pp[1], Material.AMETHYST_CLUSTER);
            b.setBlock(pp[0] + 2, 3, pp[1], Material.AMETHYST_CLUSTER);

            // Thin bridge to center (2 wide)
            int dx = Integer.signum(pp[0]);
            int dz = Integer.signum(pp[1]);
            for (int i = 7; i <= 15; i++) {
                int bx = dx * i;
                int bz = dz * i;
                b.setBlock(bx, 1, bz, Material.OBSIDIAN);
                b.setBlock(bx + (dz != 0 ? 1 : 0), 1, bz + (dx != 0 ? 1 : 0), Material.OBSIDIAN);
            }
        }

        // Lighting
        b.setBlock(0, 19, 0, Material.SOUL_LANTERN);
        for (int y = 8; y <= 16; y += 4) {
            b.setBlock(5, y, 0, Material.SOUL_LANTERN);
            b.setBlock(-5, y, 0, Material.SOUL_LANTERN);
        }

        b.placeChest(3, 3, 3);
        b.placeChest(-3, 3, -3);
        b.placeChest(0, 2, 20);
        b.placeChest(0, 2, -20);
        b.setBossSpawn(0, 3, 5);
    }
}

package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

/**
 * ShatteredCathedralBuilder — Broken gothic cathedral, partially collapsed but grand.
 * Rose window, ribbed vault ceiling, altar, crypts, overgrown with chorus plants.
 *
 * <p>Extracted verbatim from the original {@code DimensionStructureBuilders.buildShatteredCathedral}
 * method to give the structure its own file per the one-class-per-structure
 * architecture.
 */
public final class ShatteredCathedralBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // Main nave shell (elongated)
        b.fillBox(-10, 0, -24, 10, 22, 24, Material.END_STONE_BRICKS);
        b.fillBox(-9, 1, -23, 9, 21, 23, Material.AIR);
        b.fillFloor(-9, -23, 9, 23, 0, Material.DEEPSLATE_TILES);

        // Ribbed vault ceiling (where intact, south half)
        for (int z = -10; z <= 23; z += 2) {
            b.setBlock(-6, 18, z, Material.DEEPSLATE_BRICKS);
            b.setBlock(6, 18, z, Material.DEEPSLATE_BRICKS);
            b.setBlock(-4, 20, z, Material.DEEPSLATE_BRICKS);
            b.setBlock(4, 20, z, Material.DEEPSLATE_BRICKS);
            b.setBlock(-2, 21, z, Material.DEEPSLATE_BRICKS);
            b.setBlock(2, 21, z, Material.DEEPSLATE_BRICKS);
            b.setBlock(0, 22, z, Material.DEEPSLATE_BRICKS);
        }

        // Rose window (north wall, facing outward)
        b.roseWindow(0, 14, -24, 6, Material.PURPLE_STAINED_GLASS, BlockFace.NORTH);

        // Collapse the north portion (decay)
        b.decay(-10, 12, -24, 10, 22, -14, 0.35);
        b.decay(-10, 1, -24, -6, 22, -18, 0.25);
        b.decay(6, 1, -24, 10, 22, -18, 0.25);

        // Fallen pillar debris on floor (north section)
        b.fillBox(-3, 1, -20, -1, 1, -16, Material.DEEPSLATE_BRICKS);
        b.fillBox(2, 1, -18, 4, 1, -14, Material.DEEPSLATE_BRICKS);

        // Altar (still standing, south end)
        b.fillBox(-3, 0, 18, 3, 2, 22, Material.CRYING_OBSIDIAN);
        b.fillBox(-2, 2, 19, 2, 2, 21, Material.OBSIDIAN);
        b.setBlock(0, 3, 20, Material.SOUL_CAMPFIRE);
        b.setBlock(0, 4, 20, Material.END_ROD);
        b.setBlock(-2, 3, 20, Material.CANDLE);
        b.setBlock(2, 3, 20, Material.CANDLE);

        // Standing pillars (alternating, some broken)
        for (int z = -15; z <= 18; z += 6) {
            b.pillar(-7, 1, 16, z, Material.POLISHED_BLACKSTONE_BRICKS);
            if (z > -10) b.pillar(7, 1, 16, z, Material.POLISHED_BLACKSTONE_BRICKS);
            else b.pillar(7, 1, 6, z, Material.POLISHED_BLACKSTONE_BRICKS); // broken
        }

        // Stained glass windows (where walls are intact)
        for (int z = -8; z <= 20; z += 4) {
            for (int y = 6; y <= 14; y++) {
                b.setBlock(-10, y, z, Material.PURPLE_STAINED_GLASS);
                b.setBlock(10, y, z, Material.PURPLE_STAINED_GLASS);
            }
        }

        // Overgrown with chorus plants (scattered)
        for (int x = -8; x <= 8; x += 4)
            for (int z = -20; z <= -10; z += 3) {
                b.setBlock(x, 1, z, Material.END_STONE);
                b.setBlock(x, 2, z, Material.CHORUS_PLANT);
                b.setBlock(x, 3, z, Material.CHORUS_FLOWER);
            }

        // Bell tower (front, partially collapsed)
        b.fillBox(-4, 0, -28, 4, 28, -24, Material.END_STONE_BRICKS);
        b.fillBox(-3, 1, -27, 3, 27, -25, Material.AIR);
        b.setBlock(0, 22, -26, Material.BELL);
        b.decay(-4, 18, -28, 4, 28, -24, 0.3);

        // Crypts (below nave)
        b.fillBox(-8, -6, -15, 8, -1, 15, Material.DEEPSLATE_BRICKS);
        b.fillBox(-7, -5, -14, 7, -1, 14, Material.AIR);
        b.fillFloor(-7, -14, 7, 14, -6, Material.OBSIDIAN);
        // Sarcophagi
        for (int z = -10; z <= 10; z += 5) {
            b.fillBox(-2, -5, z, 2, -4, z + 2, Material.CRYING_OBSIDIAN);
        }
        // Crypt entrance (stairs from nave)
        for (int s = 0; s < 6; s++) b.setBlock(0, -s, 16 + s, Material.DEEPSLATE_BRICK_STAIRS);

        // Scattered crying obsidian on ceiling
        b.scatter(-9, 19, -10, 9, 22, 23, Material.DEEPSLATE_BRICKS, Material.CRYING_OBSIDIAN, 0.08);

        // Entrance archway
        b.archDoorway(0, 1, -24, 6, 10, Material.DEEPSLATE_BRICKS);

        // Lighting (sparse, atmospheric)
        b.chandelier(0, 22, 5, 4);
        b.chandelier(0, 22, 15, 3);
        b.setBlock(-7, 8, 10, Material.SOUL_LANTERN);
        b.setBlock(7, 8, 10, Material.SOUL_LANTERN);
        b.setBlock(-7, 8, 0, Material.SOUL_LANTERN);
        b.setBlock(7, 8, 0, Material.SOUL_LANTERN);

        b.placeChest(0, 3, 19);
        b.placeChest(-6, 1, -10);
        b.placeChest(6, 1, 12);
        b.placeChest(0, -5, 0);
        b.setBossSpawn(0, 1, 10);
    }
}

package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;

/**
 * DragonkinTempleBuilder — stone brick temple with prismarine pillars, meditation pool, and training courtyard.
 *
 * <p>Extracted verbatim from the original monolithic builder method.
 */
public final class DragonkinTempleBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // Temple platform (stepped)
        for (int step = 0; step < 3; step++) {
            int r = 22 - step * 3;
            b.fillBox(-r, step, -r, r, step, r, Material.STONE_BRICKS);
        }

        // Main temple building
        b.fillWalls(-16, 3, -16, 16, 16, 16, Material.STONE_BRICKS);
        b.hollowBox(-16, 4, -16, 16, 16, 16);
        b.fillFloor(-15, -15, 15, 15, 3, Material.DARK_PRISMARINE);

        // Prismarine pillar columns (interior)
        for (int x = -12; x <= 12; x += 6)
            for (int z = -12; z <= 12; z += 6) {
                if (Math.abs(x) <= 3 && Math.abs(z) <= 3) continue;
                b.pillar(x, 4, 15, z, Material.PRISMARINE_BRICKS);
            }

        // Training courtyard (south exterior)
        b.fillBox(-12, 2, -22, 12, 2, -17, Material.STONE_BRICKS);
        b.fillBox(-10, 3, -21, 10, 3, -18, Material.SAND); // sand training floor
        // Training dummies
        for (int x = -8; x <= 8; x += 4) {
            b.pillar(x, 3, 5, -20, Material.OAK_FENCE);
            b.setBlock(x, 6, -20, Material.CARVED_PUMPKIN);
        }

        // Inner sanctum (raised platform center)
        b.fillBox(-5, 3, -5, 5, 5, 5, Material.PRISMARINE_BRICKS);
        b.fillBox(-4, 5, -4, 4, 5, 4, Material.DARK_PRISMARINE);
        b.setBlock(0, 6, 0, Material.BEACON);
        b.setBlock(0, 7, 0, Material.END_ROD);

        // Meditation pool (east wing)
        b.fillBox(8, 3, -6, 15, 3, 6, Material.PRISMARINE_BRICKS);
        b.fillBox(9, 3, -5, 14, 3, 5, Material.WATER);
        b.setBlock(12, 4, 0, Material.LILY_PAD);

        // Weapon displays (west wing)
        b.fillBox(-15, 3, -6, -8, 8, 6, Material.STONE_BRICKS);
        b.fillBox(-14, 4, -5, -9, 7, 5, Material.AIR);
        // Display cases (item frames simulated with fences)
        for (int z = -4; z <= 4; z += 2) {
            b.setBlock(-14, 4, z, Material.OAK_FENCE);
            b.setBlock(-14, 5, z, Material.OAK_FENCE);
        }
        b.wallLighting(-14, 6, -5, -9, -5, 3, Material.LANTERN);

        // Dragon carvings (gold blocks in dragon-scale pattern on walls)
        for (int y = 8; y <= 14; y += 2) {
            b.setBlock(-16, y, 0, Material.GOLD_BLOCK);
            b.setBlock(16, y, 0, Material.GOLD_BLOCK);
            b.setBlock(-16, y + 1, -1, Material.GOLD_BLOCK);
            b.setBlock(-16, y + 1, 1, Material.GOLD_BLOCK);
            b.setBlock(16, y + 1, -1, Material.GOLD_BLOCK);
            b.setBlock(16, y + 1, 1, Material.GOLD_BLOCK);
        }

        // Roof
        b.pyramidRoof(-16, -16, 16, 16, 16, Material.DARK_PRISMARINE);

        // Entrance (south)
        b.archDoorway(0, 3, -16, 6, 8, Material.PRISMARINE_BRICKS);

        // Lighting
        b.chandelier(0, 16, 0, 3);
        b.wallLighting(-15, 8, -16, 15, -16, 5, Material.LANTERN);
        b.wallLighting(-15, 8, 16, 15, 16, 5, Material.LANTERN);

        b.placeChest(0, 6, 3);
        b.placeChest(-12, 4, 0);
        b.placeChest(12, 4, 4);
        b.setBossSpawn(0, 6, -3);
    }
}

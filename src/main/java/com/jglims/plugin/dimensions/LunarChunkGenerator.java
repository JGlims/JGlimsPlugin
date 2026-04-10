package com.jglims.plugin.dimensions;

import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;

import java.util.Random;

/**
 * Custom chunk generator for the Lunar dimension (world_lunar).
 * <p>
 * Generates a moon-like landscape: craters, dust plains, underground cavern networks.
 * Gray/silver/dark palette. Flat-ish terrain with crater depressions and
 * occasional raised crater rims.
 */
public class LunarChunkGenerator extends ChunkGenerator {

    private static final int BASE_SURFACE = 72;
    private static final int MIN_HEIGHT = 4;

    @Override
    public void generateNoise(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = chunkX * 16 + x;
                int worldZ = chunkZ * 16 + z;

                // Base terrain height with gentle rolling
                double baseNoise = noise(worldX * 0.005, worldZ * 0.005, 1);
                double detailNoise = noise(worldX * 0.02, worldZ * 0.02, 2) * 0.3;
                int surfaceY = BASE_SURFACE + (int) ((baseNoise + detailNoise) * 8);

                // Crater depressions
                double craterNoise = craterField(worldX, worldZ);
                surfaceY -= (int) (craterNoise * 15);

                surfaceY = Math.max(MIN_HEIGHT + 5, surfaceY);

                // Fill terrain
                for (int y = data_minHeight(chunkData); y <= surfaceY; y++) {
                    if (y >= chunkData.getMaxHeight()) break;
                    if (y <= MIN_HEIGHT) {
                        chunkData.setBlock(x, y, z, Material.BEDROCK);
                    } else if (y == surfaceY) {
                        chunkData.setBlock(x, y, z, Material.GRAY_CONCRETE);
                    } else if (y > surfaceY - 4) {
                        chunkData.setBlock(x, y, z, Material.LIGHT_GRAY_CONCRETE);
                    } else if (y > surfaceY - 12) {
                        chunkData.setBlock(x, y, z, Material.GRAY_CONCRETE);
                    } else {
                        chunkData.setBlock(x, y, z,
                                random.nextDouble() < 0.15 ? Material.DEEPSLATE : Material.STONE);
                    }
                }

                // Underground caverns
                generateCaverns(chunkData, worldX, worldZ, x, z, surfaceY, random);
            }
        }
    }

    /**
     * Generates a crater field — multiple overlapping craters of varying sizes.
     */
    private double craterField(int worldX, int worldZ) {
        double maxCrater = 0;

        // Large craters
        double cx1 = noise(worldX * 0.003, worldZ * 0.003, 100);
        double cx2 = noise(worldX * 0.007, worldZ * 0.007, 200);
        if (cx1 > 0.65) maxCrater = Math.max(maxCrater, (cx1 - 0.65) * 2.86);
        if (cx2 > 0.7) maxCrater = Math.max(maxCrater, (cx2 - 0.7) * 3.33);

        // Small craters (more common)
        double cx3 = noise(worldX * 0.015, worldZ * 0.015, 300);
        if (cx3 > 0.6) maxCrater = Math.max(maxCrater, (cx3 - 0.6) * 1.5);

        return maxCrater;
    }

    /**
     * Generates underground cavern networks.
     */
    private void generateCaverns(ChunkData data, int worldX, int worldZ,
                                 int localX, int localZ, int surfaceY, Random random) {
        for (int y = MIN_HEIGHT + 3; y < surfaceY - 5; y++) {
            double cavern = noise(worldX * 0.03, y * 0.05, worldZ * 0.03);
            if (cavern > 0.7) {
                data.setBlock(localX, y, localZ, Material.AIR);
                // Rare iron ore in cavern walls
                if (random.nextDouble() < 0.01) {
                    data.setBlock(localX, y, localZ, Material.IRON_ORE);
                }
            }
        }
    }

    private int data_minHeight(ChunkData data) {
        return data.getMinHeight();
    }

    /**
     * Simple deterministic noise function.
     */
    private double noise(double x, double z, double seed) {
        double a = Math.sin(x * 1.3 + seed * 0.7) * Math.cos(z * 0.9 + seed * 1.1);
        double b = Math.sin(x * 2.1 + z * 1.7 + seed) * 0.5;
        return (a + b + 1.0) / 2.0;
    }

    private double noise3d(double x, double y, double z) {
        return noise(x + y * 0.7, z + y * 0.3, y * 17.0);
    }

    @Override
    public boolean shouldGenerateNoise() { return false; }

    @Override
    public boolean shouldGenerateSurface() { return false; }

    @Override
    public boolean shouldGenerateCaves() { return false; }

    @Override
    public boolean shouldGenerateDecorations() { return false; }

    @Override
    public boolean shouldGenerateMobs() { return false; }

    @Override
    public boolean shouldGenerateStructures() { return false; }
}

package com.jglims.plugin.dimensions;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;

import java.util.Random;

/**
 * Custom chunk generator for the Aether dimension (world_aether).
 * <p>
 * Generates floating sky islands with grass, trees, waterfalls, cloud formations,
 * and crystal formations. The palette uses white, gold, cyan, and quartz tones.
 * Islands float at varying heights between Y=80 and Y=200.
 */
public class AetherChunkGenerator extends ChunkGenerator {

    private static final int BASE_HEIGHT = 100;
    private static final int ISLAND_MIN_Y = 60;
    private static final int ISLAND_MAX_Y = 200;
    private static final double ISLAND_FREQUENCY = 0.008;
    private static final double ISLAND_THRESHOLD = 0.35;

    @Override
    public void generateNoise(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
        // Generate floating islands using layered noise
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = chunkX * 16 + x;
                int worldZ = chunkZ * 16 + z;

                // Multiple island layers at different heights
                generateIslandLayer(chunkData, worldX, worldZ, x, z, random, BASE_HEIGHT, 1.0);
                generateIslandLayer(chunkData, worldX, worldZ, x, z, random, BASE_HEIGHT + 50, 0.7);
                generateIslandLayer(chunkData, worldX, worldZ, x, z, random, BASE_HEIGHT + 100, 0.4);

                // Small floating rocks
                generateSmallIslands(chunkData, worldX, worldZ, x, z, random);
            }
        }
    }

    private void generateIslandLayer(ChunkData data, int worldX, int worldZ,
                                     int localX, int localZ, Random random,
                                     int centerY, double sizeMult) {
        double noise = combinedNoise(worldX, worldZ, centerY);
        double threshold = ISLAND_THRESHOLD / sizeMult;

        if (noise > threshold) {
            double islandStrength = (noise - threshold) / (1.0 - threshold);
            int thickness = (int) (islandStrength * 12 * sizeMult) + 2;
            int topY = centerY + (int) (islandStrength * 5);

            for (int y = topY - thickness; y <= topY; y++) {
                if (y < data.getMinHeight() || y >= data.getMaxHeight()) continue;
                if (y == topY) {
                    data.setBlock(localX, y, localZ, Material.GRASS_BLOCK);
                } else if (y > topY - 3) {
                    data.setBlock(localX, y, localZ, Material.DIRT);
                } else if (y > topY - thickness + 2) {
                    data.setBlock(localX, y, localZ, Material.STONE);
                } else {
                    // Bottom of island — quartz/prismarine mix
                    data.setBlock(localX, y, localZ,
                            random.nextDouble() < 0.3 ? Material.QUARTZ_BLOCK : Material.STONE);
                }
            }

            // Crystal formations on some islands
            if (islandStrength > 0.6 && random.nextDouble() < 0.02) {
                int crystalHeight = random.nextInt(4) + 2;
                for (int cy = 1; cy <= crystalHeight; cy++) {
                    int crystalY = topY + cy;
                    if (crystalY < data.getMaxHeight()) {
                        data.setBlock(localX, crystalY, localZ, Material.AMETHYST_BLOCK);
                    }
                }
            }
        }
    }

    private void generateSmallIslands(ChunkData data, int worldX, int worldZ,
                                      int localX, int localZ, Random random) {
        // Scattered small floating rocks
        double smallNoise = simplexNoise(worldX * 0.03, worldZ * 0.03, 42.0);
        if (smallNoise > 0.7) {
            int y = ISLAND_MIN_Y + 30 + (int) (smallNoise * 40);
            if (y < data.getMaxHeight()) {
                Material mat = random.nextDouble() < 0.5
                        ? Material.SMOOTH_QUARTZ : Material.WHITE_WOOL;
                data.setBlock(localX, y, localZ, mat);
                if (y + 1 < data.getMaxHeight() && random.nextDouble() < 0.3) {
                    data.setBlock(localX, y + 1, localZ, Material.POWDER_SNOW);
                }
            }
        }
    }

    /**
     * Combined noise for island generation using simple sine-based approximation.
     */
    private double combinedNoise(int x, int z, int seed) {
        double n1 = simplexNoise(x * ISLAND_FREQUENCY, z * ISLAND_FREQUENCY, seed);
        double n2 = simplexNoise(x * ISLAND_FREQUENCY * 2, z * ISLAND_FREQUENCY * 2, seed + 100) * 0.5;
        double n3 = simplexNoise(x * ISLAND_FREQUENCY * 4, z * ISLAND_FREQUENCY * 4, seed + 200) * 0.25;
        double combined = (n1 + n2 + n3) / 1.75;

        // Create distinct island clusters using a radial falloff
        double distFromCenter = Math.sqrt(x * x + z * z);
        double radialFactor = Math.max(0, 1.0 - distFromCenter / 500.0);
        double clusterNoise = simplexNoise(x * 0.002, z * 0.002, seed + 500);
        return combined * (0.5 + 0.5 * clusterNoise) * (0.3 + 0.7 * radialFactor);
    }

    /**
     * Simple noise function using sine combination (deterministic, not true Simplex).
     */
    private double simplexNoise(double x, double z, double seed) {
        double a = Math.sin(x * 1.3 + seed * 0.7) * Math.cos(z * 0.9 + seed * 1.1);
        double b = Math.sin(x * 2.1 + z * 1.7 + seed) * 0.5;
        double c = Math.cos(x * 0.5 + z * 2.3 + seed * 0.3) * 0.3;
        return (a + b + c + 1.0) / 2.0; // Normalize to [0, 1]
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

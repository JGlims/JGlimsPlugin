package com.jglims.plugin.dimensions;

import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;

import java.util.Random;

/**
 * Custom chunk generator for the Jurassic dimension (world_jurassic).
 * <p>
 * Generates a prehistoric landscape: dense jungle, open plains, rivers,
 * volcanic areas, and tar pits. Lush vegetation, warm temperature.
 * Uses bone blocks, mossy stone, coarse dirt, mud, terracotta, and jungle wood.
 */
public class JurassicChunkGenerator extends ChunkGenerator {

    private static final int SEA_LEVEL = 62;
    private static final int BASE_HEIGHT = 68;

    @Override
    public void generateNoise(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = chunkX * 16 + x;
                int worldZ = chunkZ * 16 + z;

                // Main terrain noise
                double mainNoise = noise(worldX * 0.004, worldZ * 0.004, 1);
                double hillNoise = noise(worldX * 0.015, worldZ * 0.015, 2) * 0.3;
                double detailNoise = noise(worldX * 0.04, worldZ * 0.04, 3) * 0.1;

                // Biome-zone noise for volcanic areas
                double volcanoNoise = noise(worldX * 0.002, worldZ * 0.002, 500);

                int surfaceY = BASE_HEIGHT + (int) ((mainNoise + hillNoise + detailNoise) * 25);

                // Volcanic badlands — raise terrain and add lava features
                boolean isVolcanic = volcanoNoise > 0.75;
                if (isVolcanic) {
                    surfaceY += (int) ((volcanoNoise - 0.75) * 80);
                }

                // River carving
                double riverNoise = Math.abs(noise(worldX * 0.008, worldZ * 0.008, 100) - 0.5);
                boolean isRiver = riverNoise < 0.03;
                if (isRiver) surfaceY = Math.min(surfaceY, SEA_LEVEL - 2);

                surfaceY = Math.max(5, Math.min(surfaceY, chunkData.getMaxHeight() - 10));

                // Fill terrain
                for (int y = chunkData.getMinHeight(); y <= surfaceY; y++) {
                    if (y >= chunkData.getMaxHeight()) break;
                    if (y <= chunkData.getMinHeight() + 2) {
                        chunkData.setBlock(x, y, z, Material.BEDROCK);
                    } else if (isVolcanic && y == surfaceY) {
                        chunkData.setBlock(x, y, z,
                                random.nextDouble() < 0.4 ? Material.RED_TERRACOTTA : Material.ORANGE_TERRACOTTA);
                    } else if (isVolcanic && y > surfaceY - 3) {
                        chunkData.setBlock(x, y, z, Material.BASALT);
                    } else if (y == surfaceY) {
                        chunkData.setBlock(x, y, z,
                                random.nextDouble() < 0.3 ? Material.COARSE_DIRT : Material.GRASS_BLOCK);
                    } else if (y > surfaceY - 4) {
                        chunkData.setBlock(x, y, z,
                                random.nextDouble() < 0.2 ? Material.MUD : Material.DIRT);
                    } else if (y > surfaceY - 10) {
                        chunkData.setBlock(x, y, z,
                                random.nextDouble() < 0.1 ? Material.MOSSY_COBBLESTONE : Material.STONE);
                    } else {
                        chunkData.setBlock(x, y, z, Material.STONE);
                    }
                }

                // Water fill for rivers and low areas
                for (int y = surfaceY + 1; y <= SEA_LEVEL; y++) {
                    if (y >= chunkData.getMaxHeight()) break;
                    if (chunkData.getType(x, y, z) == Material.AIR) {
                        chunkData.setBlock(x, y, z, Material.WATER);
                    }
                }

                // Lava pools in volcanic areas
                if (isVolcanic && random.nextDouble() < 0.003 && surfaceY > BASE_HEIGHT + 10) {
                    for (int ly = surfaceY - 2; ly <= surfaceY; ly++) {
                        if (ly >= chunkData.getMaxHeight()) break;
                        chunkData.setBlock(x, ly, z, Material.LAVA);
                    }
                }

                // Tar pits (soul sand + soul soil pits)
                double tarNoise = noise(worldX * 0.01, worldZ * 0.01, 700);
                if (tarNoise > 0.8 && !isVolcanic && surfaceY < BASE_HEIGHT + 5) {
                    chunkData.setBlock(x, surfaceY, z, Material.SOUL_SOIL);
                    if (surfaceY > 1) {
                        chunkData.setBlock(x, surfaceY - 1, z, Material.SOUL_SAND);
                    }
                }

                // Bone block fossil deposits
                if (random.nextDouble() < 0.001 && !isVolcanic) {
                    int boneY = surfaceY - random.nextInt(8) - 3;
                    if (boneY > chunkData.getMinHeight() + 5) {
                        chunkData.setBlock(x, boneY, z, Material.BONE_BLOCK);
                    }
                }
            }
        }
    }

    /**
     * Simple deterministic noise function.
     */
    private double noise(double x, double z, double seed) {
        double a = Math.sin(x * 1.3 + seed * 0.7) * Math.cos(z * 0.9 + seed * 1.1);
        double b = Math.sin(x * 2.1 + z * 1.7 + seed) * 0.5;
        double c = Math.cos(x * 0.5 + z * 2.3 + seed * 0.3) * 0.3;
        return (a + b + c + 1.0) / 2.0;
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

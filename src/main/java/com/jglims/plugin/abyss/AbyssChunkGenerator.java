package com.jglims.plugin.abyss;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;

import java.util.List;
import java.util.Random;

/**
 * Custom chunk generator for the Abyss dimension.
 * Generates floating islands similar to The End, but with a dark purple/obsidian theme.
 * No flat bedrock floor — pure void below the islands.
 */
public class AbyssChunkGenerator extends ChunkGenerator {

    private final Random seedRandom = new Random();

    @Override
    public void generateNoise(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
        // Generate floating islands using simplex-like noise approach
        long seed = worldInfo.getSeed();
        Random islandRng = new Random(seed ^ ((long) chunkX * 341873128712L + (long) chunkZ * 132897987541L));

        // Central island cluster (within ~200 blocks of 0,0)
        if (Math.abs(chunkX) <= 12 && Math.abs(chunkZ) <= 12) {
            generateCentralIsland(chunkX, chunkZ, chunkData, islandRng);
        }

        // Outer floating islands (scattered)
        generateOuterIslands(chunkX, chunkZ, chunkData, islandRng, seed);
    }

    private void generateCentralIsland(int chunkX, int chunkZ, ChunkData chunkData, Random rng) {
        int cx = chunkX * 16;
        int cz = chunkZ * 16;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = cx + x;
                int worldZ = cz + z;
                double dist = Math.sqrt(worldX * worldX + worldZ * worldZ);

                // Main platform: large island centered at 0,0 at Y=64
                if (dist < 80) {
                    double heightVariation = 8 * Math.sin(worldX * 0.05) * Math.cos(worldZ * 0.05);
                    double edgeFalloff = Math.max(0, 1.0 - dist / 80.0);
                    int thickness = (int) (6 * edgeFalloff + heightVariation * edgeFalloff);
                    int baseY = 64;

                    for (int dy = -thickness; dy <= thickness / 2; dy++) {
                        int y = baseY + dy;
                        if (y < 1 || y > 250) continue;
                        if (dy == thickness / 2) {
                            chunkData.setBlock(x, y, z, Material.END_STONE);
                        } else if (dy >= thickness / 2 - 1) {
                            chunkData.setBlock(x, y, z, rng.nextDouble() < 0.3 ? Material.PURPUR_BLOCK : Material.END_STONE);
                        } else {
                            chunkData.setBlock(x, y, z, rng.nextDouble() < 0.15 ? Material.OBSIDIAN : Material.DEEPSLATE);
                        }
                    }

                    // Surface features
                    if (thickness > 2 && rng.nextDouble() < 0.02) {
                        int surfaceY = baseY + thickness / 2 + 1;
                        // Amethyst cluster columns
                        for (int dy = 0; dy < 2 + rng.nextInt(3); dy++) {
                            chunkData.setBlock(x, surfaceY + dy, z, Material.AMETHYST_BLOCK);
                        }
                    }
                    if (thickness > 1 && rng.nextDouble() < 0.04) {
                        int surfaceY = baseY + thickness / 2 + 1;
                        chunkData.setBlock(x, surfaceY, z, Material.CRYING_OBSIDIAN);
                    }
                }

                // Secondary platforms at different Y levels near center
                if (dist > 30 && dist < 120) {
                    double noise = simplexNoise2D(worldX * 0.03, worldZ * 0.03, rng.nextLong());
                    if (noise > 0.6) {
                        int platformY = 40 + (int) (noise * 50);
                        int pThickness = 2 + (int) ((noise - 0.6) * 10);
                        for (int dy = 0; dy < pThickness; dy++) {
                            Material mat;
                            if (dy == pThickness - 1) mat = Material.END_STONE;
                            else if (rng.nextDouble() < 0.2) mat = Material.PURPUR_BLOCK;
                            else mat = Material.DEEPSLATE;
                            chunkData.setBlock(x, platformY + dy, z, mat);
                        }
                    }
                }
            }
        }
    }

    private void generateOuterIslands(int chunkX, int chunkZ, ChunkData chunkData, Random rng, long seed) {
        int cx = chunkX * 16;
        int cz = chunkZ * 16;

        // Use chunk coordinates to seed island generation deterministically
        Random chunkRng = new Random(seed ^ (chunkX * 0x5DEECE66DL + chunkZ * 0xBL));

        // Each chunk has a small chance to contain an island center
        if (chunkRng.nextDouble() < 0.08) {
            int islandX = chunkRng.nextInt(16);
            int islandZ = chunkRng.nextInt(16);
            int islandY = 30 + chunkRng.nextInt(80); // Y 30-110
            int islandRadius = 5 + chunkRng.nextInt(12);
            boolean isPurpur = chunkRng.nextDouble() < 0.3;

            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    double dist = Math.sqrt((x - islandX) * (x - islandX) + (z - islandZ) * (z - islandZ));
                    if (dist <= islandRadius) {
                        double edgeFalloff = 1.0 - dist / islandRadius;
                        int thickness = (int) (4 * edgeFalloff) + 1;
                        for (int dy = -thickness; dy <= thickness / 2; dy++) {
                            int y = islandY + dy;
                            if (y < 1 || y > 250) continue;
                            Material mat;
                            if (dy == thickness / 2) {
                                mat = isPurpur ? Material.PURPUR_BLOCK : Material.END_STONE;
                            } else {
                                mat = chunkRng.nextDouble() < 0.2 ? Material.OBSIDIAN : Material.DEEPSLATE;
                            }
                            chunkData.setBlock(x, y, z, mat);
                        }

                        // Surface decorations
                        if (dist < islandRadius * 0.5 && chunkRng.nextDouble() < 0.05) {
                            int surfY = islandY + thickness / 2 + 1;
                            chunkData.setBlock(x, surfY, z, Material.END_ROD);
                        }
                    }
                }
            }
        }
    }

    /**
     * Simple 2D noise approximation for island placement.
     */
    private double simplexNoise2D(double x, double z, long seed) {
        long n = (long) (x * 49632 + z * 325176 + seed);
        n = (n >> 13) ^ n;
        n = (n * (n * n * 60493 + 19990303) + 1376312589) & 0x7fffffffL;
        return 1.0 - (n / 1073741824.0);
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
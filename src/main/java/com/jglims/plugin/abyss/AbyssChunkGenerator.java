package com.jglims.plugin.abyss;

import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import java.util.Random;

public class AbyssChunkGenerator extends ChunkGenerator {

    @Override
    public void generateNoise(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
        long seed = worldInfo.getSeed();
        Random rng = new Random(seed ^ ((long) chunkX * 341873128712L + (long) chunkZ * 132897987541L));
        int cx = chunkX * 16, cz = chunkZ * 16;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int wx = cx + x, wz = cz + z;
                double dist = Math.sqrt(wx * wx + wz * wz);

                // Central island: radius 150 for the mega citadel
                if (dist < 160) {
                    double edgeFade = Math.max(0, 1.0 - dist / 160.0);
                    double n1 = Math.sin(wx * 0.04) * Math.cos(wz * 0.04) * 6;
                    double n2 = Math.sin(wx * 0.08 + 2) * Math.cos(wz * 0.06 + 1) * 3;
                    double n3 = Math.sin(wx * 0.15) * Math.cos(wz * 0.15) * 1.5;
                    double surface = 55 + (n1 + n2 + n3) * edgeFade;
                    int surfY = (int) surface;
                    int depth = (int) (14 * edgeFade) + 3;

                    for (int y = surfY - depth; y <= surfY; y++) {
                        if (y < 1 || y > 250) continue;
                        Material mat;
                        if (y == surfY) {
                            mat = dist < 80 ? Material.DEEPSLATE_TILES : (rng.nextDouble() < 0.15 ? Material.PURPUR_BLOCK : Material.END_STONE);
                        } else if (y >= surfY - 2) {
                            mat = rng.nextDouble() < 0.3 ? Material.PURPUR_BLOCK : Material.END_STONE;
                        } else if (y >= surfY - 5) {
                            mat = rng.nextDouble() < 0.2 ? Material.OBSIDIAN : Material.DEEPSLATE;
                        } else {
                            mat = rng.nextDouble() < 0.1 ? Material.CRYING_OBSIDIAN : Material.DEEPSLATE;
                        }
                        chunkData.setBlock(x, y, z, mat);
                    }

                    if (dist > 85 && edgeFade > 0.2) {
                        if (rng.nextDouble() < 0.015) {
                            for (int dy = 1; dy <= 3 + rng.nextInt(5); dy++)
                                if (surfY + dy < 250) chunkData.setBlock(x, surfY + dy, z, Material.AMETHYST_BLOCK);
                        }
                        if (rng.nextDouble() < 0.01) {
                            for (int dy = 1; dy <= 4 + rng.nextInt(8); dy++)
                                if (surfY + dy < 250) chunkData.setBlock(x, surfY + dy, z, Material.OBSIDIAN);
                        }
                        if (rng.nextDouble() < 0.02 && surfY + 1 < 250)
                            chunkData.setBlock(x, surfY + 1, z, Material.END_ROD);
                        if (rng.nextDouble() < 0.03 && surfY + 1 < 250)
                            chunkData.setBlock(x, surfY + 1, z, Material.CRYING_OBSIDIAN);
                    }
                }

                // Outer floating islands
                if (dist > 120) {
                    Random ir = new Random(seed ^ ((long)(wx / 24) * 49632L + (long)(wz / 24) * 325176L));
                    if (ir.nextDouble() < 0.10) {
                        int icx = (wx / 24) * 24 + 12, icz = (wz / 24) * 24 + 12;
                        double idist = Math.sqrt((wx - icx) * (wx - icx) + (wz - icz) * (wz - icz));
                        int irad = 6 + ir.nextInt(12);
                        if (idist < irad) {
                            double iFade = 1.0 - idist / irad;
                            int iY = 35 + ir.nextInt(60);
                            int iThick = (int)(4 * iFade) + 1;
                            for (int dy = -iThick; dy <= iThick / 2; dy++) {
                                int y = iY + dy;
                                if (y < 1 || y > 250) continue;
                                chunkData.setBlock(x, y, z, dy == iThick / 2
                                    ? (ir.nextDouble() < 0.3 ? Material.PURPUR_BLOCK : Material.END_STONE)
                                    : (ir.nextDouble() < 0.2 ? Material.OBSIDIAN : Material.DEEPSLATE));
                            }
                        }
                    }
                }
            }
        }
    }

    @Override public boolean shouldGenerateNoise() { return false; }
    @Override public boolean shouldGenerateSurface() { return false; }
    @Override public boolean shouldGenerateCaves() { return false; }
    @Override public boolean shouldGenerateDecorations() { return false; }
    @Override public boolean shouldGenerateMobs() { return false; }
    @Override public boolean shouldGenerateStructures() { return false; }
}
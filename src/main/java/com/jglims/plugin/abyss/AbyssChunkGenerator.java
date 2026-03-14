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
                double dist = Math.sqrt((double) wx * wx + (double) wz * wz);

                if (dist < 210) {
                    double edgeFade = Math.max(0, 1.0 - dist / 210.0);
                    double n1 = Math.sin(wx * 0.03) * Math.cos(wz * 0.03) * 8;
                    double n2 = Math.sin(wx * 0.07 + 2) * Math.cos(wz * 0.05 + 1) * 4;
                    double n3 = Math.sin(wx * 0.12) * Math.cos(wz * 0.12) * 2;
                    double surface = 50 + (n1 + n2 + n3) * edgeFade;
                    int surfY = (int) surface;
                    int depth = (int) (18 * edgeFade) + 4;

                    for (int y = surfY - depth; y <= surfY; y++) {
                        if (y < 1 || y > 250) continue;
                        Material mat;
                        if (y == surfY) {
                            if (dist < 110) {
                                mat = Material.DEEPSLATE_TILES;
                            } else if (dist < 150) {
                                mat = rng.nextDouble() < 0.2 ? Material.POLISHED_BLACKSTONE : Material.DEEPSLATE_TILES;
                            } else {
                                mat = rng.nextDouble() < 0.2 ? Material.PURPUR_BLOCK : Material.END_STONE;
                            }
                        } else if (y >= surfY - 2) {
                            mat = rng.nextDouble() < 0.3 ? Material.POLISHED_BLACKSTONE : Material.DEEPSLATE;
                        } else if (y >= surfY - 6) {
                            mat = rng.nextDouble() < 0.2 ? Material.OBSIDIAN : Material.DEEPSLATE;
                        } else {
                            mat = rng.nextDouble() < 0.15 ? Material.CRYING_OBSIDIAN : Material.DEEPSLATE;
                        }
                        chunkData.setBlock(x, y, z, mat);
                    }

                    if (dist > 120 && edgeFade > 0.15) {
                        if (rng.nextDouble() < 0.012) {
                            int h = 4 + rng.nextInt(8);
                            for (int dy = 1; dy <= h; dy++)
                                if (surfY + dy < 250) chunkData.setBlock(x, surfY + dy, z, Material.AMETHYST_BLOCK);
                            if (surfY + h + 1 < 250) chunkData.setBlock(x, surfY + h + 1, z, Material.AMETHYST_CLUSTER);
                        }
                        if (rng.nextDouble() < 0.008) {
                            int h = 5 + rng.nextInt(12);
                            for (int dy = 1; dy <= h; dy++)
                                if (surfY + dy < 250) chunkData.setBlock(x, surfY + dy, z, Material.OBSIDIAN);
                        }
                        if (rng.nextDouble() < 0.015 && surfY + 1 < 250)
                            chunkData.setBlock(x, surfY + 1, z, Material.END_ROD);
                        if (rng.nextDouble() < 0.025 && surfY + 1 < 250)
                            chunkData.setBlock(x, surfY + 1, z, Material.CRYING_OBSIDIAN);
                        if (rng.nextDouble() < 0.006 && surfY + 1 < 250)
                            chunkData.setBlock(x, surfY + 1, z, Material.SOUL_CAMPFIRE);
                    }
                }

                if (dist > 160) {
                    Random ir = new Random(seed ^ ((long)(wx / 28) * 49632L + (long)(wz / 28) * 325176L));
                    if (ir.nextDouble() < 0.12) {
                        int icx = (wx / 28) * 28 + 14, icz = (wz / 28) * 28 + 14;
                        double idist = Math.sqrt((double)(wx - icx) * (wx - icx) + (double)(wz - icz) * (wz - icz));
                        int irad = 7 + ir.nextInt(14);
                        if (idist < irad) {
                            double iFade = 1.0 - idist / irad;
                            int iY = 30 + ir.nextInt(70);
                            int iThick = (int)(5 * iFade) + 1;
                            for (int dy = -iThick; dy <= iThick / 2; dy++) {
                                int y = iY + dy;
                                if (y < 1 || y > 250) continue;
                                Material mat;
                                if (dy == iThick / 2) {
                                    mat = ir.nextDouble() < 0.3 ? Material.PURPUR_BLOCK : Material.END_STONE;
                                } else {
                                    mat = ir.nextDouble() < 0.2 ? Material.OBSIDIAN : Material.DEEPSLATE;
                                }
                                chunkData.setBlock(x, y, z, mat);
                            }
                            int topY = iY + iThick / 2;
                            if (iFade > 0.5 && topY + 1 < 250) {
                                if (ir.nextDouble() < 0.06)
                                    chunkData.setBlock(x, topY + 1, z, Material.SOUL_LANTERN);
                                if (ir.nextDouble() < 0.04) {
                                    for (int dy2 = 1; dy2 <= 3 + ir.nextInt(3); dy2++)
                                        if (topY + dy2 < 250) chunkData.setBlock(x, topY + dy2, z, Material.AMETHYST_BLOCK);
                                }
                            }
                        }
                    }
                }

                if (dist > 130 && dist < 200) {
                    double riverNoise = Math.sin(wx * 0.02 + wz * 0.015) * Math.cos(wx * 0.01 - wz * 0.02);
                    if (Math.abs(riverNoise) < 0.06) {
                        for (int y = 1; y < 55; y++) {
                            chunkData.setBlock(x, y, z, Material.AIR);
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
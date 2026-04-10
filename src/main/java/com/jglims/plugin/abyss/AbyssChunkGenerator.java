package com.jglims.plugin.abyss;

import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;

import java.util.Random;

/**
 * Enhanced Abyss dimension chunk generator with biome-like zones:
 * <ul>
 *   <li>Void Wastes (center) — open area around the citadel</li>
 *   <li>Crystal Fields — dense purple amethyst crystal formations</li>
 *   <li>Obsidian Spires — tall obsidian pillar forest</li>
 *   <li>Shattered Ground — floating chunks with void chasms</li>
 * </ul>
 * Enhanced with: massive obsidian pillars, void chasms with particle-ready gaps,
 * crystal formations that glow purple, ambient decorations (soul fire, end rods).
 */
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

                // Determine biome zone based on noise
                double zoneNoise = noise(wx * 0.006, wz * 0.006, seed);
                AbyssZone zone = getZone(dist, zoneNoise);

                // Main landmass (radius ~210 blocks)
                if (dist < 220) {
                    generateMainTerrain(chunkData, x, z, wx, wz, dist, rng, zone);
                }

                // Floating islands beyond main landmass
                if (dist > 160) {
                    generateFloatingIslands(chunkData, x, z, wx, wz, dist, seed, rng);
                }

                // Void river chasms
                if (dist > 100 && dist < 210) {
                    carveVoidRivers(chunkData, x, z, wx, wz);
                }
            }
        }
    }

    private enum AbyssZone { VOID_WASTES, CRYSTAL_FIELDS, OBSIDIAN_SPIRES, SHATTERED_GROUND }

    private AbyssZone getZone(double dist, double zoneNoise) {
        if (dist < 80) return AbyssZone.VOID_WASTES; // Center area for citadel
        if (zoneNoise < 0.3) return AbyssZone.CRYSTAL_FIELDS;
        if (zoneNoise < 0.6) return AbyssZone.OBSIDIAN_SPIRES;
        return AbyssZone.SHATTERED_GROUND;
    }

    private void generateMainTerrain(ChunkData data, int x, int z, int wx, int wz,
                                     double dist, Random rng, AbyssZone zone) {
        double edgeFade = Math.max(0, 1.0 - dist / 220.0);
        double n1 = Math.sin(wx * 0.03) * Math.cos(wz * 0.03) * 8;
        double n2 = Math.sin(wx * 0.07 + 2) * Math.cos(wz * 0.05 + 1) * 4;
        double n3 = Math.sin(wx * 0.12) * Math.cos(wz * 0.12) * 2;
        double surface = 50 + (n1 + n2 + n3) * edgeFade;
        int surfY = (int) surface;
        int depth = (int) (18 * edgeFade) + 4;

        // Generate terrain column
        for (int y = surfY - depth; y <= surfY; y++) {
            if (y < 1 || y > 250) continue;
            Material mat = getSurfaceMaterial(y, surfY, dist, rng, zone);
            data.setBlock(x, y, z, mat);
        }

        // Zone-specific surface features
        switch (zone) {
            case CRYSTAL_FIELDS -> placeCrystalFeatures(data, x, z, surfY, rng, edgeFade);
            case OBSIDIAN_SPIRES -> placeObsidianSpires(data, x, z, surfY, rng, edgeFade);
            case SHATTERED_GROUND -> placeShatteredFeatures(data, x, z, surfY, rng, edgeFade);
            case VOID_WASTES -> placeWastesFeatures(data, x, z, surfY, rng, edgeFade);
        }
    }

    private Material getSurfaceMaterial(int y, int surfY, double dist, Random rng, AbyssZone zone) {
        if (y == surfY) {
            return switch (zone) {
                case VOID_WASTES -> rng.nextDouble() < 0.15 ? Material.POLISHED_BLACKSTONE : Material.DEEPSLATE_TILES;
                case CRYSTAL_FIELDS -> rng.nextDouble() < 0.25 ? Material.AMETHYST_BLOCK : Material.DEEPSLATE_TILES;
                case OBSIDIAN_SPIRES -> rng.nextDouble() < 0.3 ? Material.OBSIDIAN : Material.POLISHED_BLACKSTONE;
                case SHATTERED_GROUND -> rng.nextDouble() < 0.2 ? Material.PURPUR_BLOCK : Material.END_STONE;
            };
        } else if (y >= surfY - 2) {
            return rng.nextDouble() < 0.3 ? Material.POLISHED_BLACKSTONE : Material.DEEPSLATE;
        } else if (y >= surfY - 6) {
            return rng.nextDouble() < 0.2 ? Material.OBSIDIAN : Material.DEEPSLATE;
        } else {
            return rng.nextDouble() < 0.15 ? Material.CRYING_OBSIDIAN : Material.DEEPSLATE;
        }
    }

    /**
     * Crystal Fields zone: dense amethyst crystal formations, glowing clusters,
     * budding amethyst, and soul lanterns among crystals.
     */
    private void placeCrystalFeatures(ChunkData data, int x, int z, int surfY, Random rng, double fade) {
        if (fade < 0.15) return;
        // Tall crystal columns
        if (rng.nextDouble() < 0.025) {
            int h = 6 + rng.nextInt(15);
            for (int dy = 1; dy <= h; dy++) {
                if (surfY + dy < 250) {
                    data.setBlock(x, surfY + dy, z,
                            rng.nextDouble() < 0.2 ? Material.BUDDING_AMETHYST : Material.AMETHYST_BLOCK);
                }
            }
            if (surfY + h + 1 < 250)
                data.setBlock(x, surfY + h + 1, z, Material.AMETHYST_CLUSTER);
        }
        // Small crystal clusters on ground
        if (rng.nextDouble() < 0.06 && surfY + 1 < 250)
            data.setBlock(x, surfY + 1, z, Material.SMALL_AMETHYST_BUD);
        // Soul lanterns among crystals
        if (rng.nextDouble() < 0.01 && surfY + 1 < 250)
            data.setBlock(x, surfY + 1, z, Material.SOUL_LANTERN);
    }

    /**
     * Obsidian Spires zone: massive obsidian pillars reaching 20-40 blocks high,
     * crying obsidian veins, end rod tips.
     */
    private void placeObsidianSpires(ChunkData data, int x, int z, int surfY, Random rng, double fade) {
        if (fade < 0.15) return;
        // Massive obsidian pillars
        if (rng.nextDouble() < 0.015) {
            int h = 15 + rng.nextInt(30);
            for (int dy = 1; dy <= h; dy++) {
                if (surfY + dy >= 250) break;
                Material mat = rng.nextDouble() < 0.15 ? Material.CRYING_OBSIDIAN : Material.OBSIDIAN;
                data.setBlock(x, surfY + dy, z, mat);
            }
            // End rod tip
            if (surfY + h + 1 < 250) data.setBlock(x, surfY + h + 1, z, Material.END_ROD);
        }
        // Smaller spikes
        if (rng.nextDouble() < 0.03) {
            int h = 3 + rng.nextInt(8);
            for (int dy = 1; dy <= h; dy++)
                if (surfY + dy < 250) data.setBlock(x, surfY + dy, z, Material.OBSIDIAN);
        }
        // Crying obsidian ground patches
        if (rng.nextDouble() < 0.04 && surfY + 1 < 250)
            data.setBlock(x, surfY + 1, z, Material.CRYING_OBSIDIAN);
    }

    /**
     * Shattered Ground zone: broken terrain with gaps, floating chunks,
     * purpur/end stone palette, void chasms.
     */
    private void placeShatteredFeatures(ChunkData data, int x, int z, int surfY, Random rng, double fade) {
        if (fade < 0.15) return;
        // Void gaps (remove terrain to create chasms)
        if (rng.nextDouble() < 0.04) {
            for (int dy = -3; dy <= 0; dy++)
                if (surfY + dy > 1) data.setBlock(x, surfY + dy, z, Material.AIR);
        }
        // Small floating blocks above
        if (rng.nextDouble() < 0.008) {
            int floatY = surfY + 5 + rng.nextInt(10);
            if (floatY < 250) {
                data.setBlock(x, floatY, z,
                        rng.nextDouble() < 0.5 ? Material.PURPUR_BLOCK : Material.END_STONE);
            }
        }
        // Soul campfire ambient
        if (rng.nextDouble() < 0.008 && surfY + 1 < 250)
            data.setBlock(x, surfY + 1, z, Material.SOUL_CAMPFIRE);
    }

    /**
     * Void Wastes zone (center): relatively flat for citadel, scattered decorations.
     */
    private void placeWastesFeatures(ChunkData data, int x, int z, int surfY, Random rng, double fade) {
        // End rods as ambient light
        if (rng.nextDouble() < 0.012 && surfY + 1 < 250)
            data.setBlock(x, surfY + 1, z, Material.END_ROD);
        // Soul fire braziers
        if (rng.nextDouble() < 0.005 && surfY + 1 < 250)
            data.setBlock(x, surfY + 1, z, Material.SOUL_CAMPFIRE);
        // Occasional crying obsidian decoration
        if (rng.nextDouble() < 0.02 && surfY + 1 < 250)
            data.setBlock(x, surfY + 1, z, Material.CRYING_OBSIDIAN);
    }

    /**
     * Floating islands beyond the main landmass.
     */
    private void generateFloatingIslands(ChunkData data, int x, int z, int wx, int wz,
                                         double dist, long seed, Random rng) {
        Random ir = new Random(seed ^ ((long) (wx / 28) * 49632L + (long) (wz / 28) * 325176L));
        if (ir.nextDouble() >= 0.12) return;

        int icx = (wx / 28) * 28 + 14, icz = (wz / 28) * 28 + 14;
        double idist = Math.sqrt((double) (wx - icx) * (wx - icx) + (double) (wz - icz) * (wz - icz));
        int irad = 7 + ir.nextInt(14);
        if (idist >= irad) return;

        double iFade = 1.0 - idist / irad;
        int iY = 30 + ir.nextInt(70);
        int iThick = (int) (5 * iFade) + 1;

        for (int dy = -iThick; dy <= iThick / 2; dy++) {
            int y = iY + dy;
            if (y < 1 || y > 250) continue;
            Material mat;
            if (dy == iThick / 2) {
                mat = ir.nextDouble() < 0.3 ? Material.PURPUR_BLOCK : Material.END_STONE;
            } else {
                mat = ir.nextDouble() < 0.2 ? Material.OBSIDIAN : Material.DEEPSLATE;
            }
            data.setBlock(x, y, z, mat);
        }

        // Island surface decorations
        int topY = iY + iThick / 2;
        if (iFade > 0.5 && topY + 1 < 250) {
            if (ir.nextDouble() < 0.06) data.setBlock(x, topY + 1, z, Material.SOUL_LANTERN);
            if (ir.nextDouble() < 0.05) {
                int h = 3 + ir.nextInt(5);
                for (int dy2 = 1; dy2 <= h; dy2++)
                    if (topY + dy2 < 250) data.setBlock(x, topY + dy2, z, Material.AMETHYST_BLOCK);
                if (topY + h + 1 < 250) data.setBlock(x, topY + h + 1, z, Material.AMETHYST_CLUSTER);
            }
        }
    }

    /**
     * Carves void river chasms through the terrain.
     */
    private void carveVoidRivers(ChunkData data, int x, int z, int wx, int wz) {
        double riverNoise = Math.sin(wx * 0.02 + wz * 0.015) * Math.cos(wx * 0.01 - wz * 0.02);
        if (Math.abs(riverNoise) < 0.07) {
            for (int y = 1; y < 55; y++) {
                data.setBlock(x, y, z, Material.AIR);
            }
        }
    }

    /**
     * Simple deterministic noise function.
     */
    private double noise(double x, double z, double seed) {
        double a = Math.sin(x * 1.3 + seed * 0.00007) * Math.cos(z * 0.9 + seed * 0.00011);
        double b = Math.sin(x * 2.1 + z * 1.7 + seed * 0.00003) * 0.5;
        return (a + b + 1.0) / 2.0;
    }

    @Override public boolean shouldGenerateNoise() { return false; }
    @Override public boolean shouldGenerateSurface() { return false; }
    @Override public boolean shouldGenerateCaves() { return false; }
    @Override public boolean shouldGenerateDecorations() { return false; }
    @Override public boolean shouldGenerateMobs() { return false; }
    @Override public boolean shouldGenerateStructures() { return false; }
}

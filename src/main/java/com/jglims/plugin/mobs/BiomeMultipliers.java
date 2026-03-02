package com.jglims.plugin.mobs;

import org.bukkit.block.Biome;
import java.util.HashMap;
import java.util.Map;

public class BiomeMultipliers {

    private static final Map<Biome, double[]> OVERWORLD = new HashMap<>();
    private static final Map<Biome, double[]> NETHER = new HashMap<>();

    public static void init() {
        OVERWORLD.clear();
        NETHER.clear();

        // Overworld: [healthMult, damageMult]
        OVERWORLD.put(Biome.PALE_GARDEN, new double[]{1.8, 1.8});
        OVERWORLD.put(Biome.DEEP_DARK, new double[]{2.0, 1.8});
        OVERWORLD.put(Biome.SWAMP, new double[]{1.3, 1.2});
        OVERWORLD.put(Biome.MANGROVE_SWAMP, new double[]{1.3, 1.2});
        OVERWORLD.put(Biome.DARK_FOREST, new double[]{1.4, 1.3});
        OVERWORLD.put(Biome.DRIPSTONE_CAVES, new double[]{1.5, 1.3});
        OVERWORLD.put(Biome.LUSH_CAVES, new double[]{0.8, 0.9});

        // Nether: [healthMult, damageMult]
        NETHER.put(Biome.NETHER_WASTES, new double[]{1.5, 1.4});
        NETHER.put(Biome.SOUL_SAND_VALLEY, new double[]{1.8, 1.6});
        NETHER.put(Biome.CRIMSON_FOREST, new double[]{1.6, 1.5});
        NETHER.put(Biome.WARPED_FOREST, new double[]{1.6, 1.5});
        NETHER.put(Biome.BASALT_DELTAS, new double[]{2.0, 1.8});
    }

    public static double[] getOverworldMultiplier(Biome biome) {
        return OVERWORLD.getOrDefault(biome, new double[]{1.0, 1.0});
    }

    public static double[] getNetherMultiplier(Biome biome) {
        return NETHER.getOrDefault(biome, new double[]{1.0, 1.0});
    }

    public static boolean isNetherBiome(Biome biome) {
        return NETHER.containsKey(biome);
    }
}
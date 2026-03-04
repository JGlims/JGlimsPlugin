package com.jglims.plugin.mobs;

import com.jglims.plugin.config.ConfigManager;
import org.bukkit.block.Biome;
import java.util.HashMap;
import java.util.Map;

public class BiomeMultipliers {

    private static final Map<Biome, double[]> OVERWORLD = new HashMap<>();
    private static final Map<Biome, double[]> NETHER = new HashMap<>();

    public static void init() {
        // Fallback defaults — overridden by initWithConfig() if called
        OVERWORLD.clear();
        NETHER.clear();
        OVERWORLD.put(Biome.PALE_GARDEN, new double[]{2.0, 2.0});
        OVERWORLD.put(Biome.DEEP_DARK, new double[]{2.5, 2.5});
        OVERWORLD.put(Biome.SWAMP, new double[]{1.4, 1.4});
        OVERWORLD.put(Biome.MANGROVE_SWAMP, new double[]{1.4, 1.4});
        OVERWORLD.put(Biome.DARK_FOREST, new double[]{1.5, 1.4});
        OVERWORLD.put(Biome.DRIPSTONE_CAVES, new double[]{1.6, 1.4});
        OVERWORLD.put(Biome.LUSH_CAVES, new double[]{0.8, 0.9});
        NETHER.put(Biome.NETHER_WASTES, new double[]{1.7, 1.7});
        NETHER.put(Biome.SOUL_SAND_VALLEY, new double[]{1.9, 1.9});
        NETHER.put(Biome.CRIMSON_FOREST, new double[]{2.0, 2.0});
        NETHER.put(Biome.WARPED_FOREST, new double[]{2.0, 2.0});
        NETHER.put(Biome.BASALT_DELTAS, new double[]{2.3, 2.3});
    }

    public static void initWithConfig(ConfigManager config) {
        OVERWORLD.clear();
        NETHER.clear();
        OVERWORLD.put(Biome.PALE_GARDEN, new double[]{config.getBiomePaleGarden(), config.getBiomePaleGarden()});
        OVERWORLD.put(Biome.DEEP_DARK, new double[]{config.getBiomeDeepDark(), config.getBiomeDeepDark()});
        OVERWORLD.put(Biome.SWAMP, new double[]{config.getBiomeSwamp(), config.getBiomeSwamp()});
        OVERWORLD.put(Biome.MANGROVE_SWAMP, new double[]{config.getBiomeSwamp(), config.getBiomeSwamp()});
        OVERWORLD.put(Biome.DARK_FOREST, new double[]{1.5, 1.4});
        OVERWORLD.put(Biome.DRIPSTONE_CAVES, new double[]{1.6, 1.4});
        OVERWORLD.put(Biome.LUSH_CAVES, new double[]{0.8, 0.9});
        NETHER.put(Biome.NETHER_WASTES, new double[]{config.getBiomeNetherWastesHealth(), config.getBiomeNetherWastesDamage()});
        NETHER.put(Biome.SOUL_SAND_VALLEY, new double[]{config.getBiomeSoulSandValleyHealth(), config.getBiomeSoulSandValleyDamage()});
        NETHER.put(Biome.CRIMSON_FOREST, new double[]{config.getBiomeCrimsonForestHealth(), config.getBiomeCrimsonForestDamage()});
        NETHER.put(Biome.WARPED_FOREST, new double[]{config.getBiomeWarpedForestHealth(), config.getBiomeWarpedForestDamage()});
        NETHER.put(Biome.BASALT_DELTAS, new double[]{config.getBiomeBasaltDeltasHealth(), config.getBiomeBasaltDeltasDamage()});
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

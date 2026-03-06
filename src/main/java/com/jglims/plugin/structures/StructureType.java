package com.jglims.plugin.structures;

import com.jglims.plugin.legendary.LegendaryTier;
import org.bukkit.World;
import org.bukkit.block.Biome;

import java.util.Set;

public enum StructureType {

    // ── Overworld Structures ──
    RUINED_COLOSSEUM("Ruined Colosseum", World.Environment.NORMAL, LegendaryTier.EPIC,
        40, 25, 40, true, "Gladiator King", 300,
        Set.of(Biome.PLAINS, Biome.SAVANNA, Biome.SAVANNA_PLATEAU, Biome.SUNFLOWER_PLAINS)),

    DRUIDS_GROVE("Druid's Grove", World.Environment.NORMAL, LegendaryTier.RARE,
        30, 20, 30, true, "Ancient Treant", 200,
        Set.of(Biome.FOREST, Biome.DARK_FOREST, Biome.OLD_GROWTH_BIRCH_FOREST)),

    SHREK_HOUSE("Shrek House", World.Environment.NORMAL, LegendaryTier.EPIC,
        15, 10, 15, true, "Shrek", 400,
        Set.of(Biome.SWAMP, Biome.MANGROVE_SWAMP)),

    MAGE_TOWER("Mage Tower", World.Environment.NORMAL, LegendaryTier.EPIC,
        12, 45, 12, true, "Arch Mage", 250,
        null),

    GIGANTIC_CASTLE("Gigantic Castle", World.Environment.NORMAL, LegendaryTier.MYTHIC,
        80, 50, 80, true, "Castle Lord", 350,
        Set.of(Biome.PLAINS, Biome.MEADOW, Biome.STONY_PEAKS, Biome.WINDSWEPT_HILLS)),

    FORTRESS("Fortress", World.Environment.NORMAL, LegendaryTier.EPIC,
        50, 30, 50, true, "Warlord", 300,
        Set.of(Biome.TAIGA, Biome.OLD_GROWTH_SPRUCE_TAIGA, Biome.STONY_SHORE, Biome.WINDSWEPT_HILLS)),

    CAMPING_SMALL("Camping Station (Small)", World.Environment.NORMAL, LegendaryTier.COMMON,
        10, 5, 10, false, null, 0,
        null),

    CAMPING_LARGE("Camping Station (Large)", World.Environment.NORMAL, LegendaryTier.RARE,
        20, 8, 20, true, "Bandit Leader", 150,
        null),

    ULTRA_VILLAGE("Ultra Village", World.Environment.NORMAL, LegendaryTier.RARE,
        100, 20, 100, false, null, 0,
        Set.of(Biome.PLAINS, Biome.DESERT, Biome.SAVANNA, Biome.SUNFLOWER_PLAINS)),

    WITCH_HOUSE_SWAMP("Witch House (Swamp)", World.Environment.NORMAL, LegendaryTier.RARE,
        12, 12, 12, true, "Coven Witch", 180,
        Set.of(Biome.SWAMP, Biome.MANGROVE_SWAMP)),

    WITCH_HOUSE_FOREST("Witch House (Forest)", World.Environment.NORMAL, LegendaryTier.COMMON,
        10, 10, 10, true, "Shadow Witch", 150,
        Set.of(Biome.DARK_FOREST)),

    ALLAY_SANCTUARY("Allay Sanctuary", World.Environment.NORMAL, LegendaryTier.RARE,
        25, 15, 25, false, null, 0,
        Set.of(Biome.MEADOW, Biome.FLOWER_FOREST)),

    VOLCANO("Volcano", World.Environment.NORMAL, LegendaryTier.EPIC,
        40, 60, 40, true, "Magma Titan", 400,
        Set.of(Biome.BADLANDS, Biome.ERODED_BADLANDS, Biome.STONY_PEAKS, Biome.WINDSWEPT_HILLS)),

    ANCIENT_TEMPLE("Ancient Temple", World.Environment.NORMAL, LegendaryTier.EPIC,
        35, 25, 35, true, "Temple Guardian", 250,
        Set.of(Biome.JUNGLE, Biome.BAMBOO_JUNGLE, Biome.DESERT)),

    ABANDONED_HOUSE("Abandoned House", World.Environment.NORMAL, LegendaryTier.COMMON,
        12, 8, 12, true, "Restless Spirit", 100,
        null),

    HOUSE_TREE("House-Tree", World.Environment.NORMAL, LegendaryTier.COMMON,
        15, 25, 15, false, null, 0,
        Set.of(Biome.FOREST, Biome.BIRCH_FOREST, Biome.OLD_GROWTH_BIRCH_FOREST)),

    DUNGEON_DEEP("Dungeon (Deep)", World.Environment.NORMAL, LegendaryTier.EPIC,
        30, 15, 30, true, "Dungeon Keeper", 300,
        null),

    THANOS_TEMPLE("Thanos Temple", World.Environment.NORMAL, LegendaryTier.MYTHIC,
        50, 40, 50, true, "Thanos", 800,
        Set.of(Biome.BADLANDS, Biome.ERODED_BADLANDS, Biome.STONY_PEAKS, Biome.WINDSWEPT_HILLS)),


    PILLAGER_FORTRESS("Pillager Fortress", World.Environment.NORMAL, LegendaryTier.EPIC,
        60, 35, 60, true, "Fortress Warlord", 400,
        Set.of(Biome.PLAINS, Biome.SAVANNA, Biome.TAIGA, Biome.WINDSWEPT_HILLS, Biome.STONY_PEAKS)),

    PILLAGER_AIRSHIP("Pillager Airship", World.Environment.NORMAL, LegendaryTier.RARE,
        30, 25, 15, true, "Sky Captain", 250,
        Set.of(Biome.PLAINS, Biome.MEADOW, Biome.SUNFLOWER_PLAINS, Biome.SAVANNA)),
    // ── Nether Structures ──
    CRIMSON_CITADEL("Crimson Citadel", World.Environment.NETHER, LegendaryTier.EPIC,
        40, 30, 40, true, "Crimson Warlord", 350,
        Set.of(Biome.CRIMSON_FOREST)),

    SOUL_SANCTUM("Soul Sanctum", World.Environment.NETHER, LegendaryTier.RARE,
        30, 20, 30, true, "Soul Reaper", 250,
        Set.of(Biome.SOUL_SAND_VALLEY)),

    BASALT_SPIRE("Basalt Spire", World.Environment.NETHER, LegendaryTier.EPIC,
        20, 50, 20, true, "Basalt Golem", 400,
        Set.of(Biome.BASALT_DELTAS)),

    NETHER_DUNGEON("Nether Dungeon", World.Environment.NETHER, LegendaryTier.RARE,
        30, 15, 30, true, "Blaze Lord", 300,
        Set.of(Biome.NETHER_WASTES)),

    PIGLIN_PALACE("Piglin Palace", World.Environment.NETHER, LegendaryTier.EPIC,
        50, 30, 50, true, "Piglin King", 400,
        Set.of(Biome.CRIMSON_FOREST)),

    // ── End Structures ──
    VOID_SHRINE("Void Shrine", World.Environment.THE_END, LegendaryTier.MYTHIC,
        25, 20, 25, true, "Void Sentinel", 300,
        null),

    ENDER_MONASTERY("Ender Monastery", World.Environment.THE_END, LegendaryTier.MYTHIC,
        40, 25, 40, true, "Ender Monk", 250,
        null),

    DRAGONS_HOARD("Dragon's Hoard", World.Environment.THE_END, LegendaryTier.MYTHIC,
        20, 10, 20, false, null, 0,
        null),

    END_RIFT_ARENA("End Rift Arena", World.Environment.THE_END, LegendaryTier.MYTHIC,
        50, 20, 50, true, "End Rift Dragon", 600,
        null),

    DRAGON_DEATH_CHEST("Ender Dragon Death Chest", World.Environment.THE_END, LegendaryTier.MYTHIC,
        10, 5, 10, false, null, 0,
        null);

    private final String displayName;
    private final World.Environment dimension;
    private final LegendaryTier lootTier;
    private final int sizeX, sizeY, sizeZ;
    private final boolean hasBoss;
    private final String bossName;
    private final int bossBaseHP;
    private final Set<Biome> validBiomes;

    StructureType(String displayName, World.Environment dimension, LegendaryTier lootTier,
                  int sizeX, int sizeY, int sizeZ,
                  boolean hasBoss, String bossName, int bossBaseHP,
                  Set<Biome> validBiomes) {
        this.displayName = displayName;
        this.dimension = dimension;
        this.lootTier = lootTier;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.hasBoss = hasBoss;
        this.bossName = bossName;
        this.bossBaseHP = bossBaseHP;
        this.validBiomes = validBiomes;
    }

    public String getDisplayName() { return displayName; }
    public World.Environment getDimension() { return dimension; }
    public LegendaryTier getLootTier() { return lootTier; }
    public int getSizeX() { return sizeX; }
    public int getSizeY() { return sizeY; }
    public int getSizeZ() { return sizeZ; }
    public boolean hasBoss() { return hasBoss; }
    public String getBossName() { return bossName; }
    public int getBossBaseHP() { return bossBaseHP; }
    public Set<Biome> getValidBiomes() { return validBiomes; }

    public boolean isValidBiome(Biome biome) {
        return validBiomes == null || validBiomes.contains(biome);
    }

    public double getGenerationChance() {
        return switch (lootTier) {
            case COMMON -> 0.008;
            case RARE -> 0.005;
            case EPIC -> 0.003;
            case MYTHIC -> 0.001;
            case ABYSSAL -> 0.0005;
        };
    }

    public static StructureType[] byDimension(World.Environment env) {
        java.util.List<StructureType> list = new java.util.ArrayList<>();
        for (StructureType st : values()) {
            if (st.dimension == env) list.add(st);
        }
        return list.toArray(new StructureType[0]);
    }
}
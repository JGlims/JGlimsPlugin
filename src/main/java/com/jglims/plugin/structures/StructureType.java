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

    // ── NEW Overworld Structures (Plus Part 2) ──
    FROST_DUNGEON("Frost Dungeon", World.Environment.NORMAL, LegendaryTier.EPIC,
        35, 20, 35, true, "Frost Warden", 350,
        Set.of(Biome.SNOWY_PLAINS, Biome.FROZEN_RIVER, Biome.SNOWY_TAIGA, Biome.ICE_SPIKES, Biome.FROZEN_PEAKS)),

    BANDIT_HIDEOUT("Bandit Hideout", World.Environment.NORMAL, LegendaryTier.RARE,
        25, 15, 25, true, "Bandit King", 200,
        Set.of(Biome.BADLANDS, Biome.ERODED_BADLANDS, Biome.DESERT, Biome.SAVANNA)),

    SUNKEN_RUINS("Sunken Ruins", World.Environment.NORMAL, LegendaryTier.EPIC,
        30, 15, 30, true, "Drowned Warlord", 300,
        Set.of(Biome.OCEAN, Biome.DEEP_OCEAN, Biome.LUKEWARM_OCEAN, Biome.DEEP_LUKEWARM_OCEAN, Biome.WARM_OCEAN)),

    CURSED_GRAVEYARD("Cursed Graveyard", World.Environment.NORMAL, LegendaryTier.EPIC,
        25, 12, 25, true, "Grave Revenant", 350,
        Set.of(Biome.DARK_FOREST, Biome.SWAMP, Biome.MANGROVE_SWAMP, Biome.TAIGA)),

    SKY_ALTAR("Sky Altar", World.Environment.NORMAL, LegendaryTier.MYTHIC,
        20, 30, 20, true, "Celestial Guardian", 500,
        Set.of(Biome.STONY_PEAKS, Biome.WINDSWEPT_HILLS, Biome.MEADOW, Biome.JAGGED_PEAKS, Biome.FROZEN_PEAKS)),

    FORGE("The Forge", World.Environment.NORMAL, LegendaryTier.MYTHIC,
        60, 40, 60, true, "Protector of the Forge", 900,
        Set.of(Biome.STONY_PEAKS, Biome.WINDSWEPT_HILLS, Biome.JAGGED_PEAKS)),

    OGRIN_HUT("Ogrin's Hut", World.Environment.NORMAL, LegendaryTier.EPIC,
        20, 15, 20, true, "Ogrin Giant", 400,
        Set.of(Biome.SWAMP, Biome.MANGROVE_SWAMP)),

    FAIRY_GLADE("Fairy Glade", World.Environment.NORMAL, LegendaryTier.RARE,
        30, 20, 30, true, "Winged Unicorn", 300,
        Set.of(Biome.CHERRY_GROVE)),

    JAPANESE_TEMPLE("Japanese Temple", World.Environment.NORMAL, LegendaryTier.RARE,
        25, 20, 25, false, null, 0,
        Set.of(Biome.CHERRY_GROVE)),

    MONSTER_ISLAND("Monster Island", World.Environment.NORMAL, LegendaryTier.MYTHIC,
        200, 40, 200, true, "Godzilla", 3000,
        Set.of(Biome.OCEAN, Biome.DEEP_OCEAN)),

    WEREWOLF_DEN("Werewolf Den", World.Environment.NORMAL, LegendaryTier.MYTHIC,
        20, 15, 20, true, "Whulvk Werewolf", 800,
        Set.of(Biome.DARK_FOREST, Biome.TAIGA)),

    NECROMANCER_DUNGEON("Necromancer Dungeon", World.Environment.NORMAL, LegendaryTier.RARE,
        25, 20, 25, true, "Necromancer Lord", 300,
        Set.of(Biome.DARK_FOREST)),

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

    // ── NEW Nether Structures (Plus Part 2) ──
    WITHER_SANCTUM("Wither Sanctum", World.Environment.NETHER, LegendaryTier.EPIC,
        40, 35, 40, true, "Wither Priest", 450,
        Set.of(Biome.SOUL_SAND_VALLEY, Biome.NETHER_WASTES)),

    BLAZE_COLOSSEUM("Blaze Colosseum", World.Environment.NETHER, LegendaryTier.EPIC,
        45, 25, 45, true, "Infernal Champion", 400,
        Set.of(Biome.NETHER_WASTES, Biome.BASALT_DELTAS)),

    NETHER_KINGS_CASTLE("Nether King's Castle", World.Environment.NETHER, LegendaryTier.MYTHIC,
        250, 60, 250, true, "Nether King", 1500,
        Set.of(Biome.NETHER_WASTES, Biome.CRIMSON_FOREST)),

    DEMON_FORTRESS("Demon Fortress", World.Environment.NETHER, LegendaryTier.EPIC,
        40, 30, 40, true, "Demon Lord", 400,
        Set.of(Biome.NETHER_WASTES, Biome.SOUL_SAND_VALLEY)),

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
        null),

    GLEEOK_ARENA("Gleeok Arena", World.Environment.THE_END, LegendaryTier.MYTHIC,
        80, 40, 80, true, "King Gleeok", 1800,
        null),

    ILLIDAN_PRISON("Illidan's Prison", World.Environment.THE_END, LegendaryTier.MYTHIC,
        40, 30, 40, true, "Illidan", 1200,
        null),

    SKELETON_DRAGON_LAIR("Skeleton Dragon Lair", World.Environment.THE_END, LegendaryTier.MYTHIC,
        35, 25, 35, true, "Skeleton Dragon", 800,
        null),

    // ── NEW Abyss Structures (Plus Part 2) ──
    // Note: These use CUSTOM environment but are filtered by world name "world_abyss"
    // The Abyss dimension uses THE_END environment internally
    ABYSSAL_CASTLE("Abyssal Castle", World.Environment.THE_END, LegendaryTier.ABYSSAL,
        120, 80, 120, true, "Abyssal Overlord", 1200,
        null),

    VOID_NEXUS("Void Nexus", World.Environment.THE_END, LegendaryTier.ABYSSAL,
        30, 40, 30, true, "Void Arbiter", 800,
        null),

    SHATTERED_CATHEDRAL("Shattered Cathedral", World.Environment.THE_END, LegendaryTier.ABYSSAL,
        50, 45, 50, true, "Fallen Archbishop", 1000,
        null),

    // ── Aether Structures ──
    // Note: Aether uses THE_END environment internally
    AETHER_VILLAGE("Aether Village", World.Environment.THE_END, LegendaryTier.RARE,
        80, 20, 80, false, null, 0,
        null),

    CRYSTAL_CAVERN("Crystal Cavern", World.Environment.THE_END, LegendaryTier.EPIC,
        40, 30, 40, true, "Crystal Guardian", 300,
        null),

    STORM_PEAK_TOWER("Storm Peak Tower", World.Environment.THE_END, LegendaryTier.EPIC,
        20, 50, 20, true, "Storm Keeper", 400,
        null),

    PITLORD_LAIR("Pitlord's Lair", World.Environment.THE_END, LegendaryTier.MYTHIC,
        40, 30, 40, true, "Pitlord", 900,
        null),

    WARRIOR_SKY_FORTRESS("Warrior's Sky Fortress", World.Environment.THE_END, LegendaryTier.ABYSSAL,
        100, 50, 100, true, "The Warrior", 2000,
        null),

    DRAGONKIN_TEMPLE("Dragonkin Temple", World.Environment.THE_END, LegendaryTier.MYTHIC,
        50, 35, 50, true, "Javion Dragonkin", 1100,
        null),

    AETHER_ANCIENT_RUINS("Ancient Ruins", World.Environment.THE_END, LegendaryTier.EPIC,
        30, 15, 30, true, "Ancient Portal Mimic", 120,
        null),

    // ── Lunar Structures ──
    // Note: Lunar uses NORMAL environment
    INVADERLING_OUTPOST("Invaderling Outpost", World.Environment.NORMAL, LegendaryTier.RARE,
        20, 15, 20, true, "Invaderling Captain", 100,
        null),

    UNDERGROUND_HIVE("Underground Hive", World.Environment.NORMAL, LegendaryTier.EPIC,
        40, 25, 40, true, "Hive Queen", 300,
        null),

    ALIEN_CITADEL("Alien Citadel", World.Environment.NORMAL, LegendaryTier.MYTHIC,
        80, 40, 80, true, "Invaderling Commander", 1000,
        null),

    METEOR_CRASH_SITE("Meteor Crash Site", World.Environment.NORMAL, LegendaryTier.EPIC,
        30, 10, 30, false, null, 0,
        null),

    OBSERVATION_TOWER("Observation Tower", World.Environment.NORMAL, LegendaryTier.RARE,
        15, 40, 15, false, null, 0,
        null),

    LUNAR_BASE("Abandoned Lunar Base", World.Environment.NORMAL, LegendaryTier.EPIC,
        40, 15, 40, false, null, 0,
        null),

    // ── Jurassic Structures ──
    // Note: Jurassic uses NORMAL environment
    BONE_ARENA("Bone Arena", World.Environment.NORMAL, LegendaryTier.MYTHIC,
        60, 30, 60, true, "T-Rex", 1200,
        null),

    DINOBOT_ARENA("Dinobot Arena", World.Environment.NORMAL, LegendaryTier.MYTHIC,
        50, 25, 50, true, "Dinobot", 800,
        null),

    RAPTOR_NEST("Raptor Nest", World.Environment.NORMAL, LegendaryTier.RARE,
        15, 8, 15, false, null, 0,
        null),

    WATERING_HOLE("Watering Hole", World.Environment.NORMAL, LegendaryTier.COMMON,
        30, 5, 30, false, null, 0,
        null),

    VOLCANIC_FORGE("Volcanic Forge", World.Environment.NORMAL, LegendaryTier.EPIC,
        25, 20, 25, true, "Forge Golem", 400,
        null),

    TAR_PIT("Tar Pit", World.Environment.NORMAL, LegendaryTier.RARE,
        20, 8, 20, false, null, 0,
        null),

    NESTING_GROUND("Nesting Ground", World.Environment.NORMAL, LegendaryTier.EPIC,
        40, 10, 40, true, "Parasaurolophus", 500,
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

    /** Returns true if this structure belongs to the Abyss dimension. */
    public boolean isAbyssDimension() {
        return this == ABYSSAL_CASTLE || this == VOID_NEXUS || this == SHATTERED_CATHEDRAL;
    }

    /**
     * Returns the target world name for this structure. Structures that belong
     * to custom dimensions return the specific world name; others return null
     * to indicate they use the standard dimension filtering.
     */
    public String getTargetWorldName() {
        return switch (this) {
            // Abyss structures
            case ABYSSAL_CASTLE, VOID_NEXUS, SHATTERED_CATHEDRAL -> "world_abyss";
            // Aether structures
            case AETHER_VILLAGE, CRYSTAL_CAVERN, STORM_PEAK_TOWER, PITLORD_LAIR,
                 WARRIOR_SKY_FORTRESS, DRAGONKIN_TEMPLE, AETHER_ANCIENT_RUINS -> "world_aether";
            // Lunar structures
            case INVADERLING_OUTPOST, UNDERGROUND_HIVE, ALIEN_CITADEL,
                 METEOR_CRASH_SITE, OBSERVATION_TOWER, LUNAR_BASE -> "world_lunar";
            // Jurassic structures
            case BONE_ARENA, DINOBOT_ARENA, RAPTOR_NEST, WATERING_HOLE,
                 VOLCANIC_FORGE, TAR_PIT, NESTING_GROUND -> "world_jurassic";
            // Standard structures use dimension-based filtering
            default -> null;
        };
    }

    /**
     * Returns true if this structure belongs to a specific custom dimension
     * (Abyss, Aether, Lunar, or Jurassic).
     */
    public boolean isCustomDimension() {
        return getTargetWorldName() != null;
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

    /** Returns only Abyss structures (for the custom Abyss dimension). */
    public static StructureType[] abyssStructures() {
        java.util.List<StructureType> list = new java.util.ArrayList<>();
        for (StructureType st : values()) {
            if (st.isAbyssDimension()) list.add(st);
        }
        return list.toArray(new StructureType[0]);
    }
}
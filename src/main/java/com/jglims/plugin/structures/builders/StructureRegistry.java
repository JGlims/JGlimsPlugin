package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureType;

import java.util.EnumMap;
import java.util.Map;

/**
 * Central dispatch table mapping every {@link StructureType} to its
 * {@link IStructureBuilder} implementation.
 *
 * <p>Structures that have been migrated to one-file-per-structure classes point
 * directly at those classes. Structures that still live in the legacy
 * monolithic files ({@link OverworldStructureBuilders} and
 * {@link DimensionStructureBuilders}) are dispatched via lambda wrappers so the
 * plugin keeps working throughout the migration.
 *
 * <p>The registry is consulted by
 * {@link com.jglims.plugin.structures.StructureManager#buildStructure(StructureType, StructureBuilder)}.
 */
public final class StructureRegistry {

    private static final Map<StructureType, IStructureBuilder> REGISTRY = new EnumMap<>(StructureType.class);

    static {
        // ═══════════════════════════════════════════════════════════════
        //  REWRITTEN — one-file-per-structure, high-quality upgrades
        // ═══════════════════════════════════════════════════════════════
        REGISTRY.put(StructureType.ANCIENT_TEMPLE,        new AncientTempleBuilder());
        REGISTRY.put(StructureType.SKY_ALTAR,             new SkyAltarBuilder());
        REGISTRY.put(StructureType.MONSTER_ISLAND,        new MonsterIslandBuilder());
        REGISTRY.put(StructureType.DRAGONS_HOARD,         new DragonsHoardBuilder());
        REGISTRY.put(StructureType.DRAGON_DEATH_CHEST,    new DragonDeathChestBuilder());
        REGISTRY.put(StructureType.SKELETON_DRAGON_LAIR,  new SkeletonDragonLairBuilder());
        REGISTRY.put(StructureType.VOLCANIC_FORGE,        new VolcanicForgeBuilder());
        REGISTRY.put(StructureType.TAR_PIT,               new TarPitBuilder());
        REGISTRY.put(StructureType.AETHER_VILLAGE,        new AetherVillageBuilder());
        REGISTRY.put(StructureType.CRYSTAL_CAVERN,        new CrystalCavernBuilder());
        REGISTRY.put(StructureType.PITLORD_LAIR,          new PitlordLairBuilder());

        // ═══════════════════════════════════════════════════════════════
        //  EXTRACTED — each structure now lives in its own one-file-per-structure
        //  builder class. Identical runtime behavior to the original monolithic
        //  methods; the extraction gives every structure clean architectural
        //  isolation so future per-structure edits don't touch a 170 KB file.
        // ═══════════════════════════════════════════════════════════════

        // Overworld — extracted
        REGISTRY.put(StructureType.CAMPING_SMALL,         new CampingSmallBuilder());
        REGISTRY.put(StructureType.CAMPING_LARGE,         new CampingLargeBuilder());
        REGISTRY.put(StructureType.ABANDONED_HOUSE,       new AbandonedHouseBuilder());
        REGISTRY.put(StructureType.WITCH_HOUSE_FOREST,    new WitchHouseForestBuilder());
        REGISTRY.put(StructureType.WITCH_HOUSE_SWAMP,     new WitchHouseSwampBuilder());
        REGISTRY.put(StructureType.HOUSE_TREE,            new HouseTreeBuilder());
        REGISTRY.put(StructureType.DRUIDS_GROVE,          new DruidsGroveBuilder());
        REGISTRY.put(StructureType.ALLAY_SANCTUARY,       new AllaySanctuaryBuilder());
        REGISTRY.put(StructureType.MAGE_TOWER,            new MageTowerBuilder());
        REGISTRY.put(StructureType.RUINED_COLOSSEUM,      new RuinedColosseumBuilder());
        REGISTRY.put(StructureType.SHREK_HOUSE,           new ShrekHouseBuilder());
        REGISTRY.put(StructureType.VOLCANO,               new VolcanoBuilder());
        REGISTRY.put(StructureType.FORTRESS,              new FortressBuilder());
        REGISTRY.put(StructureType.GIGANTIC_CASTLE,       new GiganticCastleBuilder());
        REGISTRY.put(StructureType.ULTRA_VILLAGE,         new UltraVillageBuilder());
        REGISTRY.put(StructureType.DUNGEON_DEEP,          new DungeonDeepBuilder());
        REGISTRY.put(StructureType.THANOS_TEMPLE,         new ThanosTempleBuilder());
        REGISTRY.put(StructureType.PILLAGER_FORTRESS,     new PillagerFortressBuilder());
        REGISTRY.put(StructureType.PILLAGER_AIRSHIP,      new PillagerAirshipBuilder());
        REGISTRY.put(StructureType.FROST_DUNGEON,         new FrostDungeonBuilder());
        REGISTRY.put(StructureType.FORGE,                 new ForgeBuilder());
        REGISTRY.put(StructureType.JAPANESE_TEMPLE,       new JapaneseTempleBuilder());

        // Overworld — extracted in second pass
        REGISTRY.put(StructureType.BANDIT_HIDEOUT,        new BanditHideoutBuilder());
        REGISTRY.put(StructureType.SUNKEN_RUINS,          new SunkenRuinsBuilder());
        REGISTRY.put(StructureType.CURSED_GRAVEYARD,      new CursedGraveyardBuilder());
        REGISTRY.put(StructureType.OGRIN_HUT,             new OgrinHutBuilder());
        REGISTRY.put(StructureType.FAIRY_GLADE,           new FairyGladeBuilder());
        REGISTRY.put(StructureType.WEREWOLF_DEN,          new WerewolfDenBuilder());
        REGISTRY.put(StructureType.NECROMANCER_DUNGEON,   new NecromancerDungeonBuilder());

        // Nether — extracted
        REGISTRY.put(StructureType.CRIMSON_CITADEL,       new CrimsonCitadelBuilder());
        REGISTRY.put(StructureType.SOUL_SANCTUM,          new SoulSanctumBuilder());
        REGISTRY.put(StructureType.BASALT_SPIRE,          new BasaltSpireBuilder());
        REGISTRY.put(StructureType.NETHER_DUNGEON,        new NetherDungeonBuilder());
        REGISTRY.put(StructureType.PIGLIN_PALACE,         new PiglinPalaceBuilder());
        REGISTRY.put(StructureType.WITHER_SANCTUM,        new WitherSanctumBuilder());
        REGISTRY.put(StructureType.BLAZE_COLOSSEUM,       new BlazeColosseumBuilder());
        REGISTRY.put(StructureType.NETHER_KINGS_CASTLE,   new NetherKingsCastleBuilder());
        REGISTRY.put(StructureType.DEMON_FORTRESS,        new DemonFortressBuilder());

        // End — extracted
        REGISTRY.put(StructureType.VOID_SHRINE,           new VoidShrineBuilder());
        REGISTRY.put(StructureType.ENDER_MONASTERY,       new EnderMonasteryBuilder());
        REGISTRY.put(StructureType.END_RIFT_ARENA,        new EndRiftArenaBuilder());
        REGISTRY.put(StructureType.GLEEOK_ARENA,          new GleeokArenaBuilder());
        REGISTRY.put(StructureType.ILLIDAN_PRISON,        new IllidanPrisonBuilder());

        // Abyss — extracted
        REGISTRY.put(StructureType.ABYSSAL_CASTLE,        new AbyssalCastleBuilder());
        REGISTRY.put(StructureType.VOID_NEXUS,            new VoidNexusBuilder());
        REGISTRY.put(StructureType.SHATTERED_CATHEDRAL,   new ShatteredCathedralBuilder());

        // Aether — extracted
        REGISTRY.put(StructureType.STORM_PEAK_TOWER,      new StormPeakTowerBuilder());
        REGISTRY.put(StructureType.WARRIOR_SKY_FORTRESS,  new WarriorSkyFortressBuilder());

        // Aether — extracted in second pass
        REGISTRY.put(StructureType.DRAGONKIN_TEMPLE,      new DragonkinTempleBuilder());
        REGISTRY.put(StructureType.AETHER_ANCIENT_RUINS,  new AetherAncientRuinsBuilder());

        // Lunar — extracted in second pass
        REGISTRY.put(StructureType.INVADERLING_OUTPOST,   new InvaderlingOutpostBuilder());
        REGISTRY.put(StructureType.UNDERGROUND_HIVE,      new UndergroundHiveBuilder());
        REGISTRY.put(StructureType.ALIEN_CITADEL,         new AlienCitadelBuilder());
        REGISTRY.put(StructureType.METEOR_CRASH_SITE,     new MeteorCrashSiteBuilder());
        REGISTRY.put(StructureType.OBSERVATION_TOWER,     new ObservationTowerBuilder());
        REGISTRY.put(StructureType.LUNAR_BASE,            new LunarBaseBuilder());

        // Jurassic — all extracted
        REGISTRY.put(StructureType.BONE_ARENA,            new BoneArenaBuilder());
        REGISTRY.put(StructureType.DINOBOT_ARENA,         new DinobotArenaBuilder());
        REGISTRY.put(StructureType.RAPTOR_NEST,           new RaptorNestBuilder());
        REGISTRY.put(StructureType.WATERING_HOLE,         new WateringHoleBuilder());
        REGISTRY.put(StructureType.NESTING_GROUND,        new NestingGroundBuilder());

        // ── Schematic-imported structures ──────────────────────────────
        REGISTRY.put(StructureType.ANCIENT_OAK_TREE,      new AncientOakTreeBuilder());
        REGISTRY.put(StructureType.BIG_TREE,              new BigTreeBuilder());
        REGISTRY.put(StructureType.TALL_TREE,             new TallTreeBuilder());
        REGISTRY.put(StructureType.GREAT_OAK_TREE_MOSS,   new GreatOakTreeMossBuilder());
        REGISTRY.put(StructureType.CAVE_HOUSE,            new CaveHouseBuilder());
        REGISTRY.put(StructureType.CLOCK_TOWER,           new ClockTowerBuilder());
        REGISTRY.put(StructureType.DARK_MANSION,          new DarkMansionBuilder());
        REGISTRY.put(StructureType.DESERT_CITY,           new DesertCityBuilder());
        REGISTRY.put(StructureType.DESERT_HOUSE,          new DesertHouseBuilder());
        REGISTRY.put(StructureType.GIANT_CHURCH,          new GiantChurchBuilder());
        REGISTRY.put(StructureType.GREAT_LIBRARY,         new GreatLibraryBuilder());
        REGISTRY.put(StructureType.GREAT_PYRAMID,         new GreatPyramidBuilder());
        REGISTRY.put(StructureType.JAPANESE_PAGODA,       new JapanesePagodaBuilder());
        REGISTRY.put(StructureType.JUNGLE_TEMPLE_SCHEM,   new JungleTempleSchemBuilder());
        REGISTRY.put(StructureType.AZTEC_PYRAMID,         new AztecPyramidBuilder());
        REGISTRY.put(StructureType.MAYA_TEMPLE,           new MayaTempleBuilder());
        REGISTRY.put(StructureType.MEDIEVAL_MINE,         new MedievalMineBuilder());
        REGISTRY.put(StructureType.MEDIEVAL_TAVERN_INN,   new MedievalTavernInnBuilder());
        REGISTRY.put(StructureType.ROUND_MEDIEVAL_HOUSE,  new RoundMedievalHouseBuilder());
        REGISTRY.put(StructureType.RUSTIC_MOTT_BAILEY,    new RusticMottBaileyBuilder());
        REGISTRY.put(StructureType.SMALL_MEDIEVAL_CASTLE, new SmallMedievalCastleBuilder());
        REGISTRY.put(StructureType.STONE_RUINS_SCHEM,     new StoneRuinsSchemBuilder());
        REGISTRY.put(StructureType.BLACK_PEARL_SHIP,      new BlackPearlShipBuilder());
        REGISTRY.put(StructureType.TOWER_OF_GODS,         new TowerOfGodsBuilder());
    }

    private StructureRegistry() {}

    /** Returns the builder for {@code type}, or {@code null} if none is registered. */
    public static IStructureBuilder get(StructureType type) {
        return REGISTRY.get(type);
    }

    /** Returns the number of structures currently wired to the registry. */
    public static int size() {
        return REGISTRY.size();
    }
}

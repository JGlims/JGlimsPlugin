package com.jglims.plugin.custommobs;

import com.jglims.plugin.legendary.LegendaryTier;
import org.bukkit.entity.EntityType;

/**
 * Registry of every custom mob in JGlimsPlugin.
 * <p>
 * Each constant stores metadata used by {@link CustomMobManager} for spawning,
 * loot generation, and BetterModel integration. The {@code modelName} field
 * corresponds to the .bbmodel filename (without extension) expected in
 * {@code plugins/BetterModel/models/} on the server.
 */
public enum CustomMobType {

    // ── BOSS / WORLD BOSS ──────────────────────────────────────────────
    SKELETON_DRAGON("skeleton_dragon", "Skeleton Dragon", "skeleton_dragon",
            EntityType.ZOMBIE, 800, 18, MobCategory.BOSS, LegendaryTier.MYTHIC,
            false, false, "A fragile dragon of bone, fast and deadly.", 2.8),

    REALISTIC_DRAGON("realistic_dragon", "End Rift Dragon", "realistic_dragon",
            EntityType.ZOMBIE, 600, 16, MobCategory.EVENT_BOSS, LegendaryTier.EPIC,
            false, false, "A terrifying dragon that emerges from End Rifts.", 3.0),

    THE_WARRIOR("the_warrior", "The Warrior", "the_warrior",
            EntityType.ZOMBIE, 2000, 30, MobCategory.WORLD_BOSS, LegendaryTier.ABYSSAL,
            false, false, "The absolute final boss of the Aether dimension.", 3.5),

    KING_GLEEOK("king_gleeok", "King Gleeok", "king_gleeok",
            EntityType.ZOMBIE, 1800, 25, MobCategory.WORLD_BOSS, LegendaryTier.MYTHIC,
            false, false, "Three-headed dragon, stronger than the Ender Dragon.", 4.0),

    RIPPER_ZOMBIE("ripper_zombie", "Ripper Zombie", "ripper_zombie",
            EntityType.ZOMBIE, 400, 15, MobCategory.EVENT_BOSS, LegendaryTier.EPIC,
            false, false, "Terrifying Blood Moon boss.", 1.5),

    GODZILLA("godzilla", "Godzilla", "godzilla",
            EntityType.ZOMBIE, 3000, 35, MobCategory.WORLD_BOSS, LegendaryTier.MYTHIC,
            false, false, "Legendary kaiju. The hardest non-Abyss boss.", 5.0),

    GHIDORAH("ghidorah", "Ghidorah", "ghidorah",
            EntityType.ZOMBIE, 2800, 30, MobCategory.WORLD_BOSS, LegendaryTier.MYTHIC,
            false, false, "Three-headed golden dragon kaiju.", 4.5),

    WITHER_STORM("wither_storm", "Wither Storm", "wither_storm",
            EntityType.ZOMBIE, 500, 12, MobCategory.BOSS, LegendaryTier.EPIC,
            false, false, "Destructive variant of the Wither.", 4.0),

    MUSHROOM_MONSTROSITY("mushroom_monstrosity", "Mushroom Monstrosity", "mooshroom_monstrosity",
            EntityType.ZOMBIE, 600, 14, MobCategory.WORLD_BOSS, LegendaryTier.MYTHIC,
            false, false, "Giant mushroom guardian of the mushroom biome.", 3.5),

    WENDIGO("wendigo", "Wendigo", "free_wendigo",
            EntityType.ZOMBIE, 500, 20, MobCategory.WORLD_BOSS, LegendaryTier.MYTHIC,
            false, false, "Terrifying Pale Garden nocturnal predator.", 1.0),

    NETHER_KING("nether_king", "Nether King", "kiljaeden",
            EntityType.ZOMBIE, 1500, 28, MobCategory.WORLD_BOSS, LegendaryTier.MYTHIC,
            false, false, "The absolute ruler of the Nether.", 3.5),

    PITLORD("pitlord", "Pitlord", "pitlord",
            EntityType.ZOMBIE, 900, 22, MobCategory.WORLD_BOSS, LegendaryTier.MYTHIC,
            false, false, "Demonic entity trapped in the Aether.", 2.5),

    ILLIDAN("illidan", "Illidan", "illidan",
            EntityType.ZOMBIE, 1200, 24, MobCategory.WORLD_BOSS, LegendaryTier.MYTHIC,
            false, false, "Fast, smart warglaive-wielding boss.", 1.8),

    WHULVK_WEREWOLF("whulvk_werewolf", "Whulvk Werewolf Lycan", "whulvk_werewolf_lycan",
            EntityType.ZOMBIE, 800, 18, MobCategory.WORLD_BOSS, LegendaryTier.MYTHIC,
            false, false, "Werewolf world boss, spawns only at night.", 1.8),

    JAVION_DRAGONKIN("javion_dragonkin", "Javion Dragonkin", "javion_dragonkin",
            EntityType.ZOMBIE, 1100, 24, MobCategory.WORLD_BOSS, LegendaryTier.MYTHIC,
            false, false, "Ancient armored dragon-humanoid.", 2.5),

    OGRIN_GIANT("ogrin_giant", "Ogrin Giant", "ogrin_giant",
            EntityType.ZOMBIE, 400, 14, MobCategory.WORLD_BOSS, LegendaryTier.EPIC,
            false, false, "Swamp boss with a humorous personality.", 2.5),

    FROSTMAW("frostmaw", "Frostmaw", "frostmew",
            EntityType.ZOMBIE, 700, 16, MobCategory.WORLD_BOSS, LegendaryTier.MYTHIC,
            false, false, "Ice beast with freeze mechanics.", 2.5),

    PROTECTOR_OF_FORGE("protector_of_forge", "Protector of the Forge", "golden_protector_of_the_forge",
            EntityType.ZOMBIE, 900, 20, MobCategory.WORLD_BOSS, LegendaryTier.MYTHIC,
            false, false, "Golden armored guardian of the Forge.", 2.0),

    WILDFIRE("wildfire", "Wildfire", "wildfire",
            EntityType.ZOMBIE, 500, 18, MobCategory.WORLD_BOSS, LegendaryTier.EPIC,
            false, false, "Enhanced Blaze boss in Nether Fortresses.", 1.5),

    SHADOWSAIL("shadowsail", "Shadowsail", "shadowsail",
            EntityType.ZOMBIE, 600, 18, MobCategory.WORLD_BOSS, LegendaryTier.MYTHIC,
            false, false, "Shadowy dragon found in End Cities.", 3.5),

    TRIAL_CHAMBER_DEFENDER("trial_chamber_defender", "Trial Chamber Defender", "trial_chamber_defender",
            EntityType.ZOMBIE, 600, 20, MobCategory.BOSS, LegendaryTier.EPIC,
            false, false, "The true boss of Trial Chambers.", 1.5),

    INVADERLING_COMMANDER("invaderling_commander", "Invaderling Commander", "invaderling_commander",
            EntityType.ZOMBIE, 1000, 22, MobCategory.WORLD_BOSS, LegendaryTier.MYTHIC,
            false, false, "Dimension boss of the Lunar.", 1.8),

    // ── JURASSIC BOSSES ────────────────────────────────────────────────
    T_REX("t_rex", "T-Rex", "t_rex",
            EntityType.ZOMBIE, 1200, 30, MobCategory.WORLD_BOSS, LegendaryTier.MYTHIC,
            false, false, "Apex predator of the Jurassic dimension.", 3.5),

    DINOBOT("dinobot", "Dinobot", "dinobot",
            EntityType.ZOMBIE, 800, 22, MobCategory.WORLD_BOSS, LegendaryTier.MYTHIC,
            false, false, "Mechanical dinosaur boss.", 2.5),

    PARASAUROLOPHUS("parasaurolophus", "Parasaurolophus", "parasaurolophus",
            EntityType.ZOMBIE, 500, 14, MobCategory.WORLD_BOSS, LegendaryTier.EPIC,
            false, false, "Large herbivore boss protecting its nest.", 2.5),

    MUTANT_ZOMBIE("mutant_zombie", "Mutant Zombie", "mutant_zombie",
            EntityType.ZOMBIE, 300, 16, MobCategory.EVENT_BOSS, LegendaryTier.EPIC,
            false, false, "Massive zombie variant for system bosses.", 1.8),

    // ── RIDEABLE / DUAL (BOSS + RIDEABLE) ──────────────────────────────
    LEATHERN_DRAKE("leathern_drake", "Leathern Drake", "leathern_drake",
            EntityType.ZOMBIE, 80, 6, MobCategory.RIDEABLE, LegendaryTier.COMMON,
            true, true, "Desert drake; slow but jumps incredibly high.", 2.0),

    AERGIA_WINGWALKER("aergia_wingwalker", "Aergia Wingwalker", "aergia_wingwalker",
            EntityType.ZOMBIE, 500, 14, MobCategory.WORLD_BOSS, LegendaryTier.EPIC,
            true, true, "Mountain dragon; hostile in armor, tameable without.", 2.2),

    DEMONIC_WINGWALKER("demonic_wingwalker", "Demonic Wingwalker", "demonic_wingwalker",
            EntityType.ZOMBIE, 700, 18, MobCategory.WORLD_BOSS, LegendaryTier.EPIC,
            true, true, "Nether dragon; stronger than Aergia.", 2.5),

    ICE_WYVERN("ice_wyvern", "Ice Wyvern", "ice_wyvern",
            EntityType.ZOMBIE, 60, 5, MobCategory.RIDEABLE, LegendaryTier.COMMON,
            true, true, "Small flying mount native to cold biomes.", 1.8),

    WINGED_UNICORN("winged_unicorn", "Winged Unicorn", "pegasus",
            EntityType.HORSE, 300, 10, MobCategory.WORLD_BOSS, LegendaryTier.RARE,
            true, true, "Best horse in the game; hostile if provoked."),

    SUBTERRANODON("subterranodon", "Subterranodon", "subterranodon",
            EntityType.ZOMBIE, 30, 0, MobCategory.PASSIVE, LegendaryTier.COMMON,
            true, true, "Flying dinosaur; basic flight mount.", 1.5),

    // ── HOSTILE MOBS ───────────────────────────────────────────────────
    BLUE_TROLL("blue_troll", "Blue Troll", "blue_troll",
            EntityType.ZOMBIE, 40, 8, MobCategory.HOSTILE, null,
            false, false, "Basic Aether hostile mob.", 1.5),

    LOW_POLY_TROLL("low_poly_troll", "Low Poly Troll", "low_poly_troll",
            EntityType.ZOMBIE, 50, 10, MobCategory.HOSTILE, null,
            false, false, "Stronger Aether troll with a charge attack.", 1.7),

    ANGLER("angler", "Angler", "angler",
            EntityType.ZOMBIE, 45, 12, MobCategory.HOSTILE, null,
            false, false, "Deep ocean ambush predator."),

    NECROMANCER("necromancer", "Necromancer", "necromancer",
            EntityType.ZOMBIE, 150, 6, MobCategory.HOSTILE, LegendaryTier.RARE,
            false, false, "Dark Forest caster that raises the dead.", 1.2),

    DEMON_GUY("demon_guy", "Demon", "demon_mob",
            EntityType.ZOMBIE, 80, 14, MobCategory.HOSTILE, LegendaryTier.COMMON,
            false, false, "Nether demon; neutral if player wears diamond.", 1.2),

    PRISMAMORPHA("prismamorpha", "Prismamorpha", "prismamorpha",
            EntityType.ZOMBIE, 70, 10, MobCategory.HOSTILE, null,
            false, false, "Corrupted guardian variant in Ocean Monuments."),

    REDSTONE_GOLEM("redstone_golem", "Redstone Golem", "redstone_golem",
            EntityType.ZOMBIE, 250, 16, MobCategory.HOSTILE, null,
            false, false, "Aggressive golem that attacks players on sight.", 1.8),

    GENERAL_PIGLIN("general_piglin", "General Piglin", "general_piglin",
            EntityType.ZOMBIE, 200, 15, MobCategory.HOSTILE, LegendaryTier.RARE,
            false, false, "Mini-boss guarding bastion treasure rooms.", 1.3),

    BASALT_GOLEM("basalt_golem", "Basalt Golem", "basalt_golem",
            EntityType.ZOMBIE, 200, 14, MobCategory.HOSTILE, null,
            false, false, "Aggressive golem in Basalt Deltas.", 1.7),

    CATACOMBS_GOLEM("catacombs_golem", "Catacombs Golem", "catacombs_golem",
            EntityType.ZOMBIE, 80, 10, MobCategory.HOSTILE, null,
            false, false, "Generates noise to attract the Warden.", 1.3),

    THE_KEEPER("the_keeper", "The Keeper", "the_keeper",
            EntityType.ZOMBIE, 200, 15, MobCategory.HOSTILE, null,
            false, false, "Enigmatic teleporter found in Aether and Lunar."),

    ANCIENT_RUNIC_PORTAL("ancient_runic_portal", "Ancient Runic Portal", "ancient_runic_portal",
            EntityType.ZOMBIE, 120, 12, MobCategory.HOSTILE, null,
            false, false, "Aether mimic disguised as a decorative portal.", 1.5),

    SOUL_STEALER("soul_stealer", "Soul Stealer", "the_soul_stealer",
            EntityType.ZOMBIE, 90, 8, MobCategory.HOSTILE, null,
            false, false, "Aether cave ghost that steals XP.", 0.8),

    INVADERLING_SOLDIER("invaderling_soldier", "Invaderling Soldier", "invaderling_soldier",
            EntityType.ZOMBIE, 35, 8, MobCategory.HOSTILE, null,
            false, false, "Basic Lunar hostile mob.", 0.9),

    INVADERLING_RIDER("invaderling_rider", "Invaderling Rider", "invaderling_rider",
            EntityType.ZOMBIE, 60, 10, MobCategory.HOSTILE, null,
            false, false, "Mounted Lunar soldier."),

    INVADERLING_ARCHER("invaderling_archer", "Invaderling Archer", "invaderling_archers",
            EntityType.ZOMBIE, 25, 7, MobCategory.HOSTILE, null,
            false, false, "Ranged Lunar attacker.", 0.9),

    SPINOSAURUS("spinosaurus", "Spinosaurus", "spinosaurus",
            EntityType.ZOMBIE, 120, 16, MobCategory.HOSTILE, null,
            false, false, "Semi-aquatic Jurassic predator.", 3.0),

    TREMORSAURUS("tremorsaurus", "Tremorsaurus", "tremorsaurus",
            EntityType.ZOMBIE, 100, 14, MobCategory.HOSTILE, null,
            false, false, "Ground-tremor causing Jurassic hostile.", 2.0),

    VELOCIRAPTOR("velociraptor", "Velociraptor", "velociraptor",
            EntityType.ZOMBIE, 30, 10, MobCategory.HOSTILE, null,
            false, false, "Jurassic pack hunter; spawns in groups."),

    BASILISK("basilisk", "Basilisk", "basilisk",
            EntityType.ZOMBIE, 80, 12, MobCategory.HOSTILE, null,
            false, false, "Jurassic cave serpent with petrify gaze.", 1.0),

    // ── PASSIVE / NEUTRAL ──────────────────────────────────────────────
    AXOLOTL_DRAGON("axolotl_dragon", "Axolotl Dragon", "axolotl_dragon",
            EntityType.ZOMBIE, 60, 6, MobCategory.PASSIVE, null,
            false, true, "Tiny adorable cave dragon; tameable.", 0.7),

    GRIZZLY_BEAR("grizzly_bear", "Grizzly Bear", "grizzly_bear",
            EntityType.ZOMBIE, 45, 10, MobCategory.NEUTRAL, null,
            false, false, "Neutral bear in forests and taigas."),

    GRASS_FATHER("grass_father", "Grass Father", "grand_grassling_father",
            EntityType.ZOMBIE, 60, 0, MobCategory.PASSIVE, null,
            false, false, "Gentle plains golem that plants flowers.", 1.8),

    UNICORN("unicorn", "Unicorn", "unicorn_no_wings",
            EntityType.HORSE, 40, 0, MobCategory.PASSIVE, null,
            false, true, "Beautiful horse variant; fast, high jump."),

    OVERGROWN_UNICORN("overgrown_unicorn", "Overgrown Unicorn", "overgrown_unicorn",
            EntityType.HORSE, 45, 0, MobCategory.PASSIVE, null,
            false, true, "Swamp unicorn; poison immune, plants flowers."),

    STEGOSAURUS("stegosaurus", "Stegosaurus", "stegosaurus",
            EntityType.ZOMBIE, 80, 0, MobCategory.PASSIVE, null,
            false, false, "Jurassic herbivore; retaliates if attacked.", 2.0),

    GROTTOCERATOPS("grottoceratops", "Grottoceratops", "grottoceratops",
            EntityType.ZOMBIE, 70, 0, MobCategory.PASSIVE, null,
            false, false, "Jurassic herbivore; charges with horns if hit.", 1.7),

    // ── CONSTRUCTIBLE ──────────────────────────────────────────────────
    OBSERVER_GOLEM("observer_golem", "Observer Golem", "observer_golem",
            EntityType.IRON_GOLEM, 200, 20, MobCategory.CONSTRUCTIBLE, null,
            false, false, "Stronger Iron Golem that never attacks players.", 1.5),

    STONE_GOLEM("stone_golem", "Stone Golem", "stone_golem",
            EntityType.IRON_GOLEM, 80, 10, MobCategory.CONSTRUCTIBLE, null,
            false, false, "Budget Iron Golem for village protection."),

    MUTANT_IRON_GOLEM("mutant_iron_golem", "Mutant Iron Golem", "mutant_iron_golem",
            EntityType.IRON_GOLEM, 400, 30, MobCategory.CONSTRUCTIBLE, null,
            false, false, "Massively buffed Iron Golem protector.", 2.0),

    // ── NPC MOBS ───────────────────────────────────────────────────────
    JAPANESE_DRAGON_NPC("japanese_dragon_npc", "Japanese Dragon", "japanese_dragon",
            EntityType.VILLAGER, 100, 0, MobCategory.NPC, null,
            false, false, "Ancient dragon trader in Cherry Blossom temples.", 3.0),

    STEVE_NPC("steve_npc", "Steve", "steve_npc",
            EntityType.VILLAGER, 100, 0, MobCategory.NPC, null,
            false, false, "Quest-giving NPC for the Overworld quest line."),

    MALGOSHA_NPC("malgosha_npc", "Malgosha", "malgosha",
            EntityType.VILLAGER, 100, 0, MobCategory.NPC, null,
            false, false, "Nether trader NPC near bastions."),

    GARRET_NPC("garret_npc", "Garret", "garret",
            EntityType.VILLAGER, 100, 0, MobCategory.NPC, null,
            false, false, "Trader NPC with a comprehensive shop."),

    NATALIE_NPC("natalie_npc", "Natalie", "natalie",
            EntityType.VILLAGER, 100, 0, MobCategory.NPC, null,
            false, false, "Quest-giving NPC for Explorer/Special quests."),

    SWEETNESS_COPPER_GOLEM("sweetness_copper_golem", "Sweetness Copper Golem", "sweetness_copper_golem",
            EntityType.VILLAGER, 100, 0, MobCategory.NPC, null,
            false, false, "Trader NPC selling redstone and maps."),

    GLARE_NPC("glare_npc", "Glare", "glare",
            EntityType.VILLAGER, 100, 0, MobCategory.NPC, null,
            false, false, "Cave trader NPC for ores and supplies.", 0.7),

    ANGEL_NPC("angel_npc", "Angel", "angel_npc",
            EntityType.VILLAGER, 100, 0, MobCategory.NPC, null,
            false, false, "Aether villager equivalent."),

    CENTAUR_NPC("centaur_npc", "Centaur", "centaur",
            EntityType.VILLAGER, 100, 0, MobCategory.NPC, null,
            false, false, "Aether combat trader."),

    ARQUIMAGE_NPC("arquimage_npc", "The Arquimage", "jasper_trader",
            EntityType.VILLAGER, 100, 0, MobCategory.NPC, null,
            false, false, "Master trader NPC in Mage Towers.");

    private final String id;
    private final String displayName;
    private final String modelName;
    private final EntityType baseEntityType;
    private final double maxHealth;
    private final double baseDamage;
    private final MobCategory category;
    private final LegendaryTier lootTier;
    private final boolean rideable;
    private final boolean tameable;
    private final String description;
    private final double hitboxScale;

    CustomMobType(String id, String displayName, String modelName,
                  EntityType baseEntityType, double maxHealth, double baseDamage,
                  MobCategory category, LegendaryTier lootTier,
                  boolean rideable, boolean tameable, String description) {
        this(id, displayName, modelName, baseEntityType, maxHealth, baseDamage,
                category, lootTier, rideable, tameable, description, 1.0);
    }

    CustomMobType(String id, String displayName, String modelName,
                  EntityType baseEntityType, double maxHealth, double baseDamage,
                  MobCategory category, LegendaryTier lootTier,
                  boolean rideable, boolean tameable, String description,
                  double hitboxScale) {
        this.id = id;
        this.displayName = displayName;
        this.modelName = modelName;
        this.baseEntityType = baseEntityType;
        this.maxHealth = maxHealth;
        this.baseDamage = baseDamage;
        this.category = category;
        this.lootTier = lootTier;
        this.rideable = rideable;
        this.tameable = tameable;
        this.description = description;
        this.hitboxScale = hitboxScale;
    }

    /**
     * Finds a CustomMobType by its string id.
     *
     * @param id the mob id (case-insensitive)
     * @return the matching type, or null
     */
    public static CustomMobType fromId(String id) {
        if (id == null) return null;
        for (CustomMobType type : values()) {
            if (type.id.equalsIgnoreCase(id)) return type;
        }
        return null;
    }

    /**
     * Returns all mobs belonging to the given category.
     */
    public static CustomMobType[] byCategory(MobCategory category) {
        java.util.List<CustomMobType> list = new java.util.ArrayList<>();
        for (CustomMobType type : values()) {
            if (type.category == category) list.add(type);
        }
        return list.toArray(new CustomMobType[0]);
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getModelName() { return modelName; }
    public EntityType getBaseEntityType() { return baseEntityType; }
    public double getMaxHealth() { return maxHealth; }
    public double getBaseDamage() { return baseDamage; }
    public MobCategory getCategory() { return category; }
    public LegendaryTier getLootTier() { return lootTier; }
    public boolean isRideable() { return rideable; }
    public boolean isTameable() { return tameable; }
    public String getDescription() { return description; }
    public double getHitboxScale() { return hitboxScale; }
}

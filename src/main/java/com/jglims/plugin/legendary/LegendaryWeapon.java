package com.jglims.plugin.legendary;

public enum LegendaryWeapon {

    // ============ LEGENDARY TIER (24 weapons) ============

    // --- Swords (Material: DIAMOND_SWORD) ---
    OCEANS_RAGE("oceans_rage", "Ocean's Rage", "DIAMOND_SWORD", 14, 30001,
            LegendaryTier.LEGENDARY, "Majestica", "aquantictrident",
            "Tidal Crash", "Riptide Surge", 12, 8),

    TRUE_EXCALIBUR("true_excalibur", "True Excalibur", "DIAMOND_SWORD", 15, 30002,
            LegendaryTier.LEGENDARY, "Majestica", "excalibur",
            "Holy Smite", "Divine Shield", 15, 10),

    MURAMASA("muramasa", "Muramasa", "DIAMOND_SWORD", 13, 30003,
            LegendaryTier.LEGENDARY, "Majestica", "muramasa",
            "Bleeding Strike", "Blood Frenzy", 10, 6),

    SOUL_COLLECTOR("soul_collector", "Soul Collector", "DIAMOND_SWORD", 12, 30004,
            LegendaryTier.LEGENDARY, "Majestica", "soul_collector",
            "Soul Drain", "Spirit Walk", 14, 10),

    VOID_HEART("void_heart", "Void Oculus", "DIAMOND_SWORD", 14, 30005,
            LegendaryTier.LEGENDARY, "Fantasy 3D", "void_oculus",
            "Void Collapse", "Dimensional Shift", 16, 12),

    DRAGONBONE("dragonbone", "Dragon Sword", "DIAMOND_SWORD", 15, 30006,
            LegendaryTier.LEGENDARY, "Fantasy 3D", "dragon_sword",
            "Dragon Breath", "Scale Armor", 14, 10),

    ECLIPSE_SABER("eclipse_saber", "Gloomsteel Katana", "DIAMOND_SWORD", 13, 30007,
            LegendaryTier.LEGENDARY, "Fantasy 3D", "gloomsteel_katana",
            "Eclipse Slash", "Shadow Veil", 12, 8),

    RAVENOUS_BLADE("ravenous_blade", "Ravenous Blade", "DIAMOND_SWORD", 14, 30008,
            LegendaryTier.LEGENDARY, "Fantasy 3D", "ravenous_blade",
            "Devour", "Hunger Aura", 10, 6),

    // --- Axes (Material: DIAMOND_AXE) ---
    CALAMITY_BLADE("calamity_blade", "Calamity Blade", "DIAMOND_AXE", 16, 30009,
            LegendaryTier.LEGENDARY, "Fantasy 3D", "calamity_blade",
            "Cataclysm", "Doom Aura", 18, 12),

    JADE_REAPER("jade_reaper", "Jade Halberd", "DIAMOND_AXE", 14, 30010,
            LegendaryTier.LEGENDARY, "Majestica", "jadehalberd",
            "Emerald Sweep", "Jade Barrier", 12, 8),

    FROSTSCYTHE("frostscythe", "Frost Scythe", "DIAMOND_AXE", 13, 30011,
            LegendaryTier.LEGENDARY, "Majestica", "frostscythe",
            "Frozen Tempest", "Permafrost", 14, 10),

    NIGHTSHADE("nightshade", "Divine Reaper", "DIAMOND_AXE", 15, 30012,
            LegendaryTier.LEGENDARY, "Majestica", "divine_reaper",
            "Shadow Harvest", "Nightfall", 16, 12),

    // --- Tridents (Material: TRIDENT) ---
    TIDECALLER("tidecaller", "Aquantic Trident", "TRIDENT", 12, 30013,
            LegendaryTier.LEGENDARY, "Majestica", "aquantictrident",
            "Tidal Wave", "Ocean's Blessing", 14, 10),

    REQUIEM("requiem", "Requiem of the Ninth Abyss", "TRIDENT", 14, 30014,
            LegendaryTier.LEGENDARY, "Majestica", "requiem_of_hell",
            "Abyssal Judgment", "Death's Echo", 18, 14),

    // --- Hoes (Material: DIAMOND_HOE) ---
    AMETHYST_SHURIKEN("amethyst_shuriken", "Amethyst Shuriken", "DIAMOND_HOE", 10, 30015,
            LegendaryTier.LEGENDARY, "Majestica", "amethyst_shuriken",
            "Shuriken Storm", "Crystal Shield", 8, 6),

    ROYAL_CHAKRAM("royal_chakram", "Royal Chakram", "DIAMOND_HOE", 11, 30016,
            LegendaryTier.LEGENDARY, "Majestica", "royalchakram",
            "Chakram Throw", "Royal Guard", 10, 8),

    // --- More Swords ---
    LEGENDARY_SWORD("legendary_sword", "Legendary Sword", "DIAMOND_SWORD", 16, 30017,
            LegendaryTier.LEGENDARY, "Majestica", "legendarysword",
            "Legendary Strike", "Valor Aura", 20, 15),

    PHOENIX_GRACE("phoenix_grace", "Pheonix Grace", "DIAMOND_SWORD", 13, 30018,
            LegendaryTier.LEGENDARY, "Majestica", "pheonixgrace",
            "Phoenix Rebirth", "Flame Wings", 16, 12),

    KALMIA_VEIL("kalmia_veil", "Bramblethorn", "DIAMOND_SWORD", 12, 30019,
            LegendaryTier.LEGENDARY, "Majestica", "bramblethorn",
            "Toxic Bloom", "Poison Mist", 10, 8),

    ABOMINABLE_SABER("abominable_saber", "Abominable Great Saber", "DIAMOND_SWORD", 15, 30020,
            LegendaryTier.LEGENDARY, "Majestica", "abominablegreatsaber",
            "Abominable Cleave", "Terror Aura", 14, 10),

    DEMONIC_BLADE("demonic_blade", "Demonic Blade", "DIAMOND_SWORD", 14, 30021,
            LegendaryTier.LEGENDARY, "Majestica", "demonicblade",
            "Demon's Wrath", "Hellfire Shroud", 12, 8),

    VOLCANIC_SABER("volcanic_saber", "Molten Blade", "DIAMOND_SWORD", 13, 30022,
            LegendaryTier.LEGENDARY, "Majestica", "moltenblade",
            "Eruption", "Magma Shield", 10, 6),

    // --- More Axes ---
    STARS_EDGE("stars_edge", "Star's Edge", "DIAMOND_AXE", 14, 30023,
            LegendaryTier.LEGENDARY, "Majestica", "stars_edge",
            "Stellar Cleave", "Constellation", 12, 8),

    INFERNO_AXE("inferno_axe", "Emberblade", "DIAMOND_AXE", 15, 30024,
            LegendaryTier.LEGENDARY, "Majestica", "emberblade",
            "Inferno Slam", "Blaze Aura", 14, 10),

    // ============ UNCOMMON TIER (20 weapons) ============

    // --- Diamond Swords ---
    NOCTURNE("nocturne", "Nocturne", "DIAMOND_SWORD", 10, 30025,
            LegendaryTier.UNCOMMON, "Fantasy 3D", "nocturne",
            "Shadow Slash", "Night Cloak", 8, 5),

    BLAZE_REAPER("blaze_reaper", "Windreaper", "DIAMOND_SWORD", 9, 30026,
            LegendaryTier.UNCOMMON, "Fantasy 3D", "windreaper",
            "Flame Sweep", "Heat Aura", 8, 5),

    TITANS_EDGE("titans_edge", "Zenith", "DIAMOND_SWORD", 10, 30027,
            LegendaryTier.UNCOMMON, "Fantasy 3D", "zenith",
            "Titan Smash", "Fortify", 10, 6),

    CRYSTAL_FANG("crystal_fang", "Crystal Frostblade", "DIAMOND_SWORD", 9, 30028,
            LegendaryTier.UNCOMMON, "Fantasy 3D", "crystal_frostblade",
            "Crystal Pierce", "Prism Shield", 8, 5),

    THUNDER_EDGE("thunder_edge", "Talonbrand", "DIAMOND_SWORD", 10, 30029,
            LegendaryTier.UNCOMMON, "Fantasy 3D", "talonbrand",
            "Lightning Strike", "Storm Shield", 10, 6),

    FROST_MOURNE("frost_mourne", "Ethereal Frostblade", "DIAMOND_SWORD", 9, 30030,
            LegendaryTier.UNCOMMON, "Fantasy 3D", "ethereal_frostblade",
            "Frost Nova", "Ice Armor", 8, 5),

    CORRUPTED_SABER("corrupted_saber", "Lycanbane", "DIAMOND_SWORD", 10, 30031,
            LegendaryTier.UNCOMMON, "Fantasy 3D", "lycanbane",
            "Corruption Wave", "Dark Shield", 10, 6),

    TWILIGHT_KATANA("twilight_katana", "Vesper", "DIAMOND_SWORD", 9, 30032,
            LegendaryTier.UNCOMMON, "Fantasy 3D", "vesper",
            "Twilight Slash", "Dusk Veil", 8, 5),

    OBSIDIAN_FANG("obsidian_fang", "Moonlight", "DIAMOND_SWORD", 10, 30033,
            LegendaryTier.UNCOMMON, "Fantasy 3D", "moonlight",
            "Obsidian Shatter", "Stone Skin", 10, 6),

    CELESTIAL_ARC("celestial_arc", "Solstice", "DIAMOND_SWORD", 9, 30034,
            LegendaryTier.UNCOMMON, "Fantasy 3D", "solstice",
            "Star Beam", "Celestial Aura", 8, 5),

    SUNFORGE("sunforge", "Winterthorn", "DIAMOND_SWORD", 10, 30035,
            LegendaryTier.UNCOMMON, "Fantasy 3D", "winterthorn",
            "Solar Flare", "Sun Shield", 10, 6),

    BLOOD_CRESCENT("blood_crescent", "Demon's Blood Blade", "DIAMOND_SWORD", 9, 30036,
            LegendaryTier.UNCOMMON, "Fantasy 3D", "demons_blood_blade",
            "Blood Moon", "Lifesteal", 8, 5),

    NETHER_GLAIVE("nether_glaive", "Demonic Sword", "DIAMOND_SWORD", 10, 30037,
            LegendaryTier.UNCOMMON, "Fantasy 3D", "demonic_sword",
            "Nether Sweep", "Wither Shield", 10, 6),

    EMERALD_SLICER("emerald_slicer", "Nature Sword", "DIAMOND_SWORD", 9, 30038,
            LegendaryTier.UNCOMMON, "Fantasy 3D", "nature_sword",
            "Emerald Cut", "Nature's Grace", 8, 5),

    AMETHYST_GREATBLADE("amethyst_greatblade", "Amethyst Greatblade", "DIAMOND_SWORD", 10, 30039,
            LegendaryTier.UNCOMMON, "Fantasy 3D", "amethyst_greatblade",
            "Amethyst Slam", "Crystal Barrier", 10, 6),

    // --- Golden Swords (Uncommon only) ---
    PRISM_SABER("prism_saber", "Grand Claymore", "GOLDEN_SWORD", 8, 30040,
            LegendaryTier.UNCOMMON, "Fantasy 3D", "grand_claymore",
            "Prism Burst", "Rainbow Shield", 8, 5),

    TEMPEST_CLAW("tempest_claw", "Gilded Phoenix Greataxe", "GOLDEN_SWORD", 8, 30041,
            LegendaryTier.UNCOMMON, "Fantasy 3D", "gilded_phoenix_greataxe",
            "Wind Slash", "Gale Shield", 8, 5),

    RUNIC_BLADE("runic_blade", "Vengeance Blade", "GOLDEN_SWORD", 8, 30042,
            LegendaryTier.UNCOMMON, "Fantasy 3D", "vengeance_blade",
            "Rune Blast", "Rune Ward", 8, 5),

    PHANTOM_EDGE("phantom_edge", "Azure Dagger", "GOLDEN_SWORD", 8, 30043,
            LegendaryTier.UNCOMMON, "Fantasy 3D", "azure_dagger",
            "Phase Strike", "Ghost Walk", 8, 5),

    STARFALL_SHARD("starfall_shard", "Oculus", "GOLDEN_SWORD", 8, 30044,
            LegendaryTier.UNCOMMON, "Fantasy 3D", "oculus",
            "Meteor Rain", "Star Shield", 8, 5);

    private final String id;
    private final String displayName;
    private final String baseMaterial;
    private final int baseDamage;
    private final int customModelData;
    private final LegendaryTier tier;
    private final String textureSource;
    private final String textureName;
    private final String rightClickAbility;
    private final String holdAbility;
    private final int rightClickCooldown;
    private final int holdCooldown;

    LegendaryWeapon(String id, String displayName, String baseMaterial, int baseDamage, int customModelData,
                    LegendaryTier tier, String textureSource, String textureName,
                    String rightClickAbility, String holdAbility,
                    int rightClickCooldown, int holdCooldown) {
        this.id = id;
        this.displayName = displayName;
        this.baseMaterial = baseMaterial;
        this.baseDamage = baseDamage;
        this.customModelData = customModelData;
        this.tier = tier;
        this.textureSource = textureSource;
        this.textureName = textureName;
        this.rightClickAbility = rightClickAbility;
        this.holdAbility = holdAbility;
        this.rightClickCooldown = rightClickCooldown;
        this.holdCooldown = holdCooldown;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getBaseMaterial() { return baseMaterial; }
    public int getBaseDamage() { return baseDamage; }
    public int getCustomModelData() { return customModelData; }
    public LegendaryTier getTier() { return tier; }
    public String getTextureSource() { return textureSource; }
    public String getTextureName() { return textureName; }
    public String getRightClickAbility() { return rightClickAbility; }
    public String getHoldAbility() { return holdAbility; }
    public int getRightClickCooldown() { return rightClickCooldown; }
    public int getHoldCooldown() { return holdCooldown; }

    public static LegendaryWeapon fromId(String id) {
        for (LegendaryWeapon weapon : values()) {
            if (weapon.id.equals(id)) return weapon;
        }
        return null;
    }

    public enum LegendaryTier {
        LEGENDARY,
        UNCOMMON
    }
}

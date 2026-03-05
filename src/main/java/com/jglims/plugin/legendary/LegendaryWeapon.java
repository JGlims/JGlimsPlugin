package com.jglims.plugin.legendary;

import org.bukkit.Material;

/**
 * All 44 legendary weapons.
 * 24 LEGENDARY (elite) + 20 UNCOMMON (common world-chest finds).
 *
 * CustomModelData ranges:
 *   30001–30024 = LEGENDARY
 *   30025–30044 = UNCOMMON
 */
public enum LegendaryWeapon {

    // ═══════════════════════════════════════════════════════════════
    // LEGENDARY TIER (24) — Boss drops + rare chests — DMG 11–18
    // ═══════════════════════════════════════════════════════════════

    OCEANS_RAGE("oceans_rage", "Ocean's Rage", Material.DIAMOND_SWORD, 14, 30001,
        LegendaryTier.LEGENDARY, "Majestica", "Ocean Rage",
        "Tidal Crash", "Riptide Surge", 8, 15),

    AQUATIC_SACRED_BLADE("aquatic_sacred_blade", "Aquatic Sacred Blade", Material.DIAMOND_SWORD, 13, 30002,
        LegendaryTier.LEGENDARY, "Majestica", "Aquantic Sacred Blade",
        "Aqua Heal", "Depth Pressure", 20, 25),

    TRUE_EXCALIBUR("true_excalibur", "True Excalibur", Material.DIAMOND_SWORD, 16, 30003,
        LegendaryTier.LEGENDARY, "Majestica", "Phoenixe's Grace",
        "Holy Smite", "Divine Shield", 10, 45),

    REQUIEM_NINTH_ABYSS("requiem_ninth_abyss", "Requiem of the Ninth Abyss", Material.DIAMOND_SWORD, 15, 30004,
        LegendaryTier.LEGENDARY, "Majestica", "Soul Devourer",
        "Soul Devour", "Abyss Gate", 12, 60),

    ROYAL_CHAKRAM("royal_chakram", "Royal Chakram", Material.DIAMOND_SWORD, 12, 30005,
        LegendaryTier.LEGENDARY, "Majestica", "Royal Chakram",
        "Chakram Throw", "Spinning Shield", 6, 20),

    BERSERKERS_GREATAXE("berserkers_greataxe", "Berserker's Greataxe", Material.DIAMOND_AXE, 17, 30006,
        LegendaryTier.LEGENDARY, "Fantasy 3D", "Berserker's Greataxe",
        "Berserker Slam", "Blood Rage", 10, 30),

    ACIDIC_CLEAVER("acidic_cleaver", "Acidic Cleaver", Material.DIAMOND_AXE, 14, 30007,
        LegendaryTier.LEGENDARY, "Fantasy 3D", "Treacherous Cleaver",
        "Acid Splash", "Corrosive Aura", 10, 25),

    BLACK_IRON_GREATSWORD("black_iron_greatsword", "Black Iron Greatsword", Material.DIAMOND_SWORD, 15, 30008,
        LegendaryTier.LEGENDARY, "Fantasy 3D", "Black Iron Greatsword",
        "Dark Slash", "Iron Fortress", 8, 30),

    MURAMASA("muramasa", "Muramasa", Material.DIAMOND_SWORD, 13, 30009,
        LegendaryTier.LEGENDARY, "Majestica", "Muramasa",
        "Crimson Flash", "Bloodlust", 6, 20),

    PHOENIXS_GRACE("phoenixs_grace", "Phoenix's Grace", Material.DIAMOND_AXE, 15, 30010,
        LegendaryTier.LEGENDARY, "Fantasy 3D", "Gilded Phoenix Greataxe",
        "Phoenix Strike", "Rebirth Flame", 10, 120),

    SOUL_COLLECTOR("soul_collector", "Soul Collector", Material.DIAMOND_SWORD, 14, 30011,
        LegendaryTier.LEGENDARY, "Majestica", "Soul Collector",
        "Soul Harvest", "Spirit Army", 8, 30),

    AMETHYST_SHURIKEN("amethyst_shuriken", "Amethyst Shuriken", Material.DIAMOND_SWORD, 11, 30012,
        LegendaryTier.LEGENDARY, "Majestica", "Amethyst Shuriken",
        "Shuriken Barrage", "Shadow Step", 7, 15),

    VALHAKYRA("valhakyra", "Valhakyra", Material.DIAMOND_SWORD, 15, 30013,
        LegendaryTier.LEGENDARY, "Majestica", "Valhakyra",
        "Valkyrie Dive", "Wings of Valor", 12, 25),

    WINDREAPER("windreaper", "Windreaper", Material.DIAMOND_SWORD, 13, 30014,
        LegendaryTier.LEGENDARY, "Fantasy 3D", "Windreaper",
        "Gale Slash", "Cyclone", 8, 20),

    PHANTOMGUARD("phantomguard", "Phantomguard", Material.DIAMOND_SWORD, 14, 30015,
        LegendaryTier.LEGENDARY, "Fantasy 3D", "Phantomguard Greatsword",
        "Spectral Cleave", "Phase Shift", 10, 35),

    MOONLIGHT("moonlight", "Moonlight", Material.DIAMOND_SWORD, 13, 30016,
        LegendaryTier.LEGENDARY, "Fantasy 3D", "Moonlight",
        "Lunar Beam", "Eclipse", 10, 30),

    ZENITH("zenith", "Zenith", Material.DIAMOND_SWORD, 18, 30017,
        LegendaryTier.LEGENDARY, "Fantasy 3D", "Zenith",
        "Final Judgment", "Ascension", 15, 60),

    SOLSTICE("solstice", "Solstice", Material.DIAMOND_SWORD, 14, 30018,
        LegendaryTier.LEGENDARY, "Fantasy 3D", "Solstice",
        "Solar Flare", "Daybreak", 10, 25),

    GRAND_CLAYMORE("grand_claymore", "Grand Claymore", Material.DIAMOND_SWORD, 16, 30019,
        LegendaryTier.LEGENDARY, "Fantasy 3D", "Grand Claymore",
        "Titan Swing", "Colossus Stance", 10, 30),

    CALAMITY_BLADE("calamity_blade", "Calamity Blade", Material.DIAMOND_SWORD, 15, 30020,
        LegendaryTier.LEGENDARY, "Majestica", "Calamity Blade",
        "Cataclysm", "Doomsday", 12, 35),

    DRAGON_SWORD("dragon_sword", "Dragon Sword", Material.DIAMOND_SWORD, 14, 30021,
        LegendaryTier.LEGENDARY, "Fantasy 3D", "Dragon Sword",
        "Dragon Breath", "Draconic Roar", 10, 25),

    TALONBRAND("talonbrand", "Talonbrand", Material.DIAMOND_SWORD, 13, 30022,
        LegendaryTier.LEGENDARY, "Fantasy 3D", "Talonbrand",
        "Talon Strike", "Predator's Mark", 8, 20),

    EMERALD_GREATCLEAVER("emerald_greatcleaver", "Emerald Greatcleaver", Material.DIAMOND_AXE, 16, 30023,
        LegendaryTier.LEGENDARY, "Fantasy 3D", "Emerald Greatcleaver",
        "Emerald Storm", "Gem Barrier", 10, 40),

    DEMONS_BLOOD_BLADE("demons_blood_blade", "Demon's Blood Blade", Material.DIAMOND_SWORD, 15, 30024,
        LegendaryTier.LEGENDARY, "Majestica", "Demon's Blood Blade",
        "Blood Rite", "Demonic Form", 8, 35),

    // ═══════════════════════════════════════════════════════════════
    // UNCOMMON LEGENDARY TIER (20) — World chests — DMG 10–13
    // ═══════════════════════════════════════════════════════════════

    NOCTURNE("nocturne", "Nocturne", Material.DIAMOND_SWORD, 12, 30025,
        LegendaryTier.UNCOMMON, "Fantasy 3D", "Nocturne",
        "Shadow Slash", "Night Cloak", 7, 20),

    GRAVESCEPTER("gravescepter", "Gravescepter", Material.DIAMOND_SWORD, 11, 30026,
        LegendaryTier.UNCOMMON, "Fantasy 3D", "Revenant's Gravescepter",
        "Grave Rise", "Death's Grasp", 15, 18),

    LYCANBANE("lycanbane", "Lycanbane", Material.DIAMOND_SWORD, 12, 30027,
        LegendaryTier.UNCOMMON, "Fantasy 3D", "Lycanbane",
        "Silver Strike", "Hunter's Sense", 8, 20),

    GLOOMSTEEL_KATANA("gloomsteel_katana", "Gloomsteel Katana", Material.DIAMOND_SWORD, 11, 30028,
        LegendaryTier.UNCOMMON, "Fantasy 3D", "Gloomsteel Katana",
        "Quick Draw", "Shadow Stance", 5, 18),

    VIRIDIAN_CLEAVER("viridian_cleaver", "Viridian Cleaver", Material.DIAMOND_AXE, 13, 30029,
        LegendaryTier.UNCOMMON, "Fantasy 3D", "Viridian Greataxe",
        "Verdant Slam", "Overgrowth", 8, 22),

    CRESCENT_EDGE("crescent_edge", "Crescent Edge", Material.DIAMOND_AXE, 12, 30030,
        LegendaryTier.UNCOMMON, "Fantasy 3D", "Crescent Greataxe",
        "Lunar Cleave", "Crescent Guard", 7, 20),

    GRAVECLEAVER("gravecleaver", "Gravecleaver", Material.DIAMOND_AXE, 12, 30031,
        LegendaryTier.UNCOMMON, "Fantasy 3D", "Revenant's Gravecleaver",
        "Bone Shatter", "Undying Rage", 10, 45),

    AMETHYST_GREATBLADE("amethyst_greatblade", "Amethyst Greatblade", Material.DIAMOND_SWORD, 11, 30032,
        LegendaryTier.UNCOMMON, "Fantasy 3D", "Amethyst Greatblade",
        "Crystal Burst", "Gem Resonance", 8, 25),

    FLAMBERGE("flamberge", "Flamberge", Material.DIAMOND_SWORD, 12, 30033,
        LegendaryTier.UNCOMMON, "Fantasy 3D", "Flamberge",
        "Flame Wave", "Ember Shield", 8, 18),

    CRYSTAL_FROSTBLADE("crystal_frostblade", "Crystal Frostblade", Material.DIAMOND_SWORD, 11, 30034,
        LegendaryTier.UNCOMMON, "Fantasy 3D", "Crystal Frostblade",
        "Frost Spike", "Permafrost", 7, 22),

    DEMONSLAYER("demonslayer", "Demonslayer", Material.DIAMOND_SWORD, 13, 30035,
        LegendaryTier.UNCOMMON, "Fantasy 3D", "Demonslayer's Greatsword",
        "Holy Rend", "Purifying Aura", 8, 20),

    VENGEANCE("vengeance", "Vengeance", Material.DIAMOND_SWORD, 10, 30036,
        LegendaryTier.UNCOMMON, "Fantasy 3D", "Vengeance Blade",
        "Retribution", "Grudge Mark", 12, 15),

    OCULUS("oculus", "Oculus", Material.DIAMOND_SWORD, 11, 30037,
        LegendaryTier.UNCOMMON, "Fantasy 3D", "Oculus",
        "All-Seeing Strike", "Third Eye", 8, 25),

    ANCIENT_GREATSLAB("ancient_greatslab", "Ancient Greatslab", Material.DIAMOND_SWORD, 13, 30038,
        LegendaryTier.UNCOMMON, "Fantasy 3D", "Ancient Greatslab",
        "Seismic Slam", "Stone Skin", 9, 22),

    // === TRIDENT LEGENDARIES (3) ===
    NEPTUNES_FANG("neptunes_fang", "Neptune's Fang", Material.TRIDENT, 12, 30039,
        LegendaryTier.UNCOMMON, "Majestica", "Frost Axe",
        "Riptide Slash", "Maelstrom", 7, 22),

    TIDECALLER("tidecaller", "Tidecaller", Material.TRIDENT, 11, 30040,
        LegendaryTier.UNCOMMON, "Majestica", "Aquantic Sacred Blade",
        "Tidal Spear", "Depth Ward", 8, 20),

    STORMFORK("stormfork", "Stormfork", Material.TRIDENT, 13, 30041,
        LegendaryTier.UNCOMMON, "Majestica", "Ocean Rage",
        "Lightning Javelin", "Thunder Shield", 10, 25),

    JADE_REAPER("jade_reaper", "Jade Reaper", Material.DIAMOND_HOE, 12, 30042,
        LegendaryTier.UNCOMMON, "Majestica", "Jade Halberd",
        "Jade Crescent", "Emerald Harvest", 7, 30),

    VINDICATOR("vindicator", "Vindicator", Material.DIAMOND_AXE, 11, 30043,
        LegendaryTier.UNCOMMON, "Fantasy 3D", "Vindicator",
        "Executioner's Chop", "Rally Cry", 8, 25),

    SPIDER_FANG("spider_fang", "Spider Fang", Material.DIAMOND_SWORD, 10, 30044,
        LegendaryTier.UNCOMMON, "Fantasy 3D", "Spider Sword",
        "Web Trap", "Wall Crawler", 8, 20);

    // ═══════════════════════════════════════════════════════════════

    private final String id;
    private final String displayName;
    private final Material baseMaterial;
    private final int baseDamage;
    private final int customModelData;
    private final LegendaryTier tier;
    private final String textureSource;
    private final String textureName;
    private final String rightClickAbilityName;
    private final String holdAbilityName;
    private final int rightClickCooldown;
    private final int holdCooldown;

    LegendaryWeapon(String id, String displayName, Material baseMaterial, int baseDamage,
                    int customModelData, LegendaryTier tier, String textureSource,
                    String textureName, String rightClickAbilityName, String holdAbilityName,
                    int rightClickCooldown, int holdCooldown) {
        this.id = id;
        this.displayName = displayName;
        this.baseMaterial = baseMaterial;
        this.baseDamage = baseDamage;
        this.customModelData = customModelData;
        this.tier = tier;
        this.textureSource = textureSource;
        this.textureName = textureName;
        this.rightClickAbilityName = rightClickAbilityName;
        this.holdAbilityName = holdAbilityName;
        this.rightClickCooldown = rightClickCooldown;
        this.holdCooldown = holdCooldown;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public Material getBaseMaterial() { return baseMaterial; }
    public int getBaseDamage() { return baseDamage; }
    public int getCustomModelData() { return customModelData; }
    public LegendaryTier getTier() { return tier; }
    public String getTextureSource() { return textureSource; }
    public String getTextureName() { return textureName; }
    public String getRightClickAbilityName() { return rightClickAbilityName; }
    public String getHoldAbilityName() { return holdAbilityName; }
    public int getRightClickCooldown() { return rightClickCooldown; }
    public int getHoldCooldown() { return holdCooldown; }

    /**
     * Look up a weapon by its string ID.
     */
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

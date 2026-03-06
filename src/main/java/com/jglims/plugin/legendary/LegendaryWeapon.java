package com.jglims.plugin.legendary;

import org.bukkit.Material;

/**
 * LegendaryWeapon - enum of all 44 legendary weapons.
 * v3.0.0 Phase 8c - Rewritten with 5-tier system.
 * Input: primary = right-click, alternate = crouch + right-click (rule A.10).
 */
public enum LegendaryWeapon {

    // COMMON TIER (damage 10-13, PCD 5-8s, ACD 15-25s, 10 particles)
    AMETHYST_SHURIKEN("amethyst_shuriken", "Amethyst Shuriken", Material.DIAMOND_SWORD, 11, 30012, LegendaryTier.COMMON, "amethyst_shuriken", "Shuriken Barrage", "Shadow Step", 7, 15),
    GRAVESCEPTER("gravescepter", "Gravescepter", Material.DIAMOND_SWORD, 11, 30026, LegendaryTier.COMMON, "revenants_gravescepter", "Grave Rise", "Death's Grasp", 8, 18),
    LYCANBANE("lycanbane", "Lycanbane", Material.DIAMOND_SWORD, 12, 30027, LegendaryTier.COMMON, "lycanbane", "Silver Strike", "Hunter's Sense", 8, 20),
    GLOOMSTEEL_KATANA("gloomsteel_katana", "Gloomsteel Katana", Material.DIAMOND_SWORD, 11, 30028, LegendaryTier.COMMON, "gloomsteel_katana", "Quick Draw", "Shadow Stance", 5, 18),
    VIRIDIAN_CLEAVER("viridian_cleaver", "Viridian Cleaver", Material.DIAMOND_AXE, 13, 30029, LegendaryTier.COMMON, "viridian_greataxe", "Verdant Slam", "Overgrowth", 8, 22),
    CRESCENT_EDGE("crescent_edge", "Crescent Edge", Material.DIAMOND_AXE, 12, 30030, LegendaryTier.COMMON, "crescent_greataxe", "Lunar Cleave", "Crescent Guard", 7, 20),
    GRAVECLEAVER("gravecleaver", "Gravecleaver", Material.DIAMOND_SWORD, 12, 30031, LegendaryTier.COMMON, "revenants_gravecleaver", "Bone Shatter", "Undying Rage", 8, 25),
    AMETHYST_GREATBLADE("amethyst_greatblade", "Amethyst Greatblade", Material.DIAMOND_SWORD, 11, 30032, LegendaryTier.COMMON, "amethyst_greatblade", "Crystal Burst", "Gem Resonance", 8, 25),
    FLAMBERGE("flamberge", "Flamberge", Material.DIAMOND_SWORD, 12, 30033, LegendaryTier.COMMON, "flamberge", "Flame Wave", "Ember Shield", 8, 18),
    CRYSTAL_FROSTBLADE("crystal_frostblade", "Crystal Frostblade", Material.DIAMOND_SWORD, 11, 30034, LegendaryTier.COMMON, "crystal_frostblade", "Frost Spike", "Permafrost", 7, 22),
    DEMONSLAYER("demonslayer", "Demonslayer", Material.DIAMOND_SWORD, 13, 30035, LegendaryTier.COMMON, "demonslayers_greatsword", "Holy Rend", "Purifying Aura", 8, 20),
    VENGEANCE("vengeance", "Vengeance", Material.DIAMOND_SWORD, 10, 30036, LegendaryTier.COMMON, "vengeance_blade", "Retribution", "Grudge Mark", 8, 15),
    OCULUS("oculus", "Oculus", Material.DIAMOND_SWORD, 11, 30037, LegendaryTier.COMMON, "oculus", "All-Seeing Strike", "Third Eye", 8, 25),
    ANCIENT_GREATSLAB("ancient_greatslab", "Ancient Greatslab", Material.DIAMOND_SWORD, 13, 30038, LegendaryTier.COMMON, "ancient_greatslab", "Seismic Slam", "Stone Skin", 8, 22),
    NEPTUNES_FANG("neptunes_fang", "Neptune's Fang", Material.TRIDENT, 12, 30039, LegendaryTier.COMMON, "neptunes_fang", "Riptide Slash", "Maelstrom", 7, 22),
    TIDECALLER("tidecaller", "Tidecaller", Material.TRIDENT, 11, 30040, LegendaryTier.COMMON, "tidecaller", "Tidal Spear", "Depth Ward", 8, 20),
    STORMFORK("stormfork", "Stormfork", Material.TRIDENT, 13, 30041, LegendaryTier.COMMON, "stormfork", "Lightning Javelin", "Thunder Shield", 8, 25),
    JADE_REAPER("jade_reaper", "Jade Reaper", Material.DIAMOND_HOE, 12, 30042, LegendaryTier.COMMON, "jadehalberd", "Jade Crescent", "Emerald Harvest", 7, 25),
    VINDICATOR("vindicator", "Vindicator", Material.DIAMOND_AXE, 11, 30043, LegendaryTier.COMMON, "vindicator", "Executioner's Chop", "Rally Cry", 8, 25),
    SPIDER_FANG("spider_fang", "Spider Fang", Material.DIAMOND_SWORD, 10, 30044, LegendaryTier.COMMON, "spider_sword", "Web Trap", "Wall Crawler", 8, 20),

    // RARE TIER (damage 12-15, PCD 7-10s, ACD 20-30s, 25 particles)
    OCEANS_RAGE("oceans_rage", "Ocean's Rage", Material.TRIDENT, 14, 30001, LegendaryTier.RARE, "stormbringer", "Stormbringer", "Riptide Surge", 8, 25),
    AQUATIC_SACRED_BLADE("aquatic_sacred_blade", "Aquatic Sacred Blade", Material.DIAMOND_SWORD, 13, 30002, LegendaryTier.RARE, "aquantic_sacred_blade", "Aqua Heal", "Depth Pressure", 10, 25),
    ROYAL_CHAKRAM("royal_chakram", "Royal Chakram", Material.DIAMOND_SWORD, 12, 30005, LegendaryTier.RARE, "royalchakram", "Chakram Throw", "Spinning Shield", 7, 20),
    ACIDIC_CLEAVER("acidic_cleaver", "Acidic Cleaver", Material.DIAMOND_AXE, 14, 30007, LegendaryTier.RARE, "treacherous_cleaver", "Acid Splash", "Corrosive Aura", 10, 25),
    MURAMASA("muramasa", "Muramasa", Material.DIAMOND_SWORD, 13, 30009, LegendaryTier.RARE, "muramasa", "Crimson Flash", "Bloodlust", 7, 20),
    WINDREAPER("windreaper", "Windreaper", Material.DIAMOND_SWORD, 13, 30014, LegendaryTier.RARE, "windreaper", "Gale Slash", "Cyclone", 8, 20),
    MOONLIGHT("moonlight", "Moonlight", Material.DIAMOND_SWORD, 13, 30016, LegendaryTier.RARE, "moonlight", "Lunar Beam", "Eclipse", 10, 30),
    TALONBRAND("talonbrand", "Talonbrand", Material.DIAMOND_SWORD, 13, 30022, LegendaryTier.RARE, "talonbrand", "Talon Strike", "Predator's Mark", 8, 20),

    // EPIC TIER (damage 14-17, PCD 8-12s, ACD 25-45s, 50 particles)
    BERSERKERS_GREATAXE("berserkers_greataxe", "Berserker's Greataxe", Material.DIAMOND_AXE, 17, 30006, LegendaryTier.EPIC, "berserkers_greataxe", "Berserker Slam", "Blood Rage", 10, 30),
    BLACK_IRON_GREATSWORD("black_iron_greatsword", "Black Iron Greatsword", Material.DIAMOND_SWORD, 15, 30008, LegendaryTier.EPIC, "black_iron_greatsword", "Dark Slash", "Iron Fortress", 8, 30),
    SOLSTICE("solstice", "Solstice", Material.DIAMOND_SWORD, 14, 30018, LegendaryTier.EPIC, "solstice", "Solar Flare", "Daybreak", 10, 25),
    GRAND_CLAYMORE("grand_claymore", "Grand Claymore", Material.DIAMOND_SWORD, 16, 30019, LegendaryTier.EPIC, "grand_claymore", "Titan Swing", "Colossus Stance", 10, 30),
    CALAMITY_BLADE("calamity_blade", "Calamity Blade", Material.DIAMOND_AXE, 15, 30020, LegendaryTier.EPIC, "calamity_blade", "Cataclysm", "Doomsday", 12, 35),
    EMERALD_GREATCLEAVER("emerald_greatcleaver", "Emerald Greatcleaver", Material.DIAMOND_AXE, 16, 30023, LegendaryTier.EPIC, "emerald_greatcleaver", "Emerald Storm", "Gem Barrier", 10, 40),
    DEMONS_BLOOD_BLADE("demons_blood_blade", "Demon's Blood Blade", Material.DIAMOND_SWORD, 15, 30024, LegendaryTier.EPIC, "demons_blood_blade", "Blood Rite", "Demonic Form", 8, 35),

    // MYTHIC TIER (damage 16-22, PCD 10-15s, ACD 30-60s, 100 particles)
    TRUE_EXCALIBUR("true_excalibur", "True Excalibur", Material.DIAMOND_SWORD, 20, 30003, LegendaryTier.MYTHIC, "excalibur", "Holy Smite", "Divine Shield", 10, 45),
    REQUIEM_NINTH_ABYSS("requiem_ninth_abyss", "Requiem of the Ninth Abyss", Material.DIAMOND_SWORD, 20, 30004, LegendaryTier.MYTHIC, "requiem_of_hell", "Soul Devour", "Abyss Gate", 12, 60),
    PHOENIXS_GRACE("phoenixs_grace", "Phoenix's Grace", Material.DIAMOND_AXE, 20, 30010, LegendaryTier.MYTHIC, "gilded_phoenix_greataxe", "Phoenix Strike", "Rebirth Flame", 10, 60),
    SOUL_COLLECTOR("soul_collector", "Soul Collector", Material.DIAMOND_SWORD, 19, 30011, LegendaryTier.MYTHIC, "soul_collector", "Soul Harvest", "Spirit Army", 10, 30),
    VALHAKYRA("valhakyra", "Valhakyra", Material.DIAMOND_SWORD, 18, 30013, LegendaryTier.MYTHIC, "valhakyra", "Valkyrie Dive", "Wings of Valor", 12, 45),
    PHANTOMGUARD("phantomguard", "Phantomguard Greatsword", Material.DIAMOND_SWORD, 19, 30015, LegendaryTier.MYTHIC, "phantomguard_greatsword", "Spectral Cleave", "Phase Shift", 10, 35),
    ZENITH("zenith", "Zenith", Material.DIAMOND_SWORD, 22, 30017, LegendaryTier.MYTHIC, "zenith", "Final Judgment", "Ascension", 15, 60),
    DRAGON_SWORD("dragon_sword", "Dragon Sword", Material.DIAMOND_SWORD, 18, 30021, LegendaryTier.MYTHIC, "dragon_sword", "Dragon Breath", "Draconic Roar", 10, 45),
    NOCTURNE("nocturne", "Nocturne", Material.DIAMOND_SWORD, 18, 30025, LegendaryTier.MYTHIC, "nocturne", "Shadow Slash", "Night Cloak", 10, 40);

    // ABYSSAL TIER - Weapons #60-63 added in Phase 16

    private final String id;
    private final String displayName;
    private final Material baseMaterial;
    private final int baseDamage;
    private final int customModelData;
    private final LegendaryTier tier;
    private final String textureName;
    private final String primaryAbilityName;
    private final String altAbilityName;
    private final int primaryCooldown;
    private final int altCooldown;

    LegendaryWeapon(String id, String displayName, Material baseMaterial, int baseDamage,
                    int customModelData, LegendaryTier tier, String textureName,
                    String primaryAbilityName, String altAbilityName,
                    int primaryCooldown, int altCooldown) {
        this.id = id;
        this.displayName = displayName;
        this.baseMaterial = baseMaterial;
        this.baseDamage = baseDamage;
        this.customModelData = customModelData;
        this.tier = tier;
        this.textureName = textureName;
        this.primaryAbilityName = primaryAbilityName;
        this.altAbilityName = altAbilityName;
        this.primaryCooldown = primaryCooldown;
        this.altCooldown = altCooldown;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public Material getBaseMaterial() { return baseMaterial; }
    public int getBaseDamage() { return baseDamage; }
    public int getCustomModelData() { return customModelData; }
    public LegendaryTier getTier() { return tier; }
    public String getTextureName() { return textureName; }
    public String getPrimaryAbilityName() { return primaryAbilityName; }
    public String getAltAbilityName() { return altAbilityName; }
    public int getPrimaryCooldown() { return primaryCooldown; }
    public int getAltCooldown() { return altCooldown; }

    @Deprecated public String getRightClickAbilityName() { return primaryAbilityName; }
    @Deprecated public String getHoldAbilityName() { return altAbilityName; }
    @Deprecated public int getRightClickCooldown() { return primaryCooldown; }
    @Deprecated public int getHoldCooldown() { return altCooldown; }

    public static LegendaryWeapon fromId(String id) {
        if (id == null) return null;
        for (LegendaryWeapon weapon : values()) {
            if (weapon.id.equals(id)) return weapon;
        }
        return null;
    }

    public static LegendaryWeapon[] byTier(LegendaryTier tier) {
        java.util.List<LegendaryWeapon> result = new java.util.ArrayList<>();
        for (LegendaryWeapon w : values()) {
            if (w.tier == tier) result.add(w);
        }
        return result.toArray(new LegendaryWeapon[0]);
    }

    public static LegendaryWeapon[] byMaterial(Material mat) {
        java.util.List<LegendaryWeapon> result = new java.util.ArrayList<>();
        for (LegendaryWeapon w : values()) {
            if (w.baseMaterial == mat) result.add(w);
        }
        return result.toArray(new LegendaryWeapon[0]);
    }
}
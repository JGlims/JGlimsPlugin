package com.jglims.plugin.legendary;

import org.bukkit.Material;

/**
 * LegendaryWeapon - enum of all legendary weapons.
 * v3.2.0 Phase 10 - Reduced cooldowns, increased damage across all tiers.
 * Input: primary = right-click, alternate = crouch + right-click (rule A.10).
 */
public enum LegendaryWeapon {

    // ================================================================
    // COMMON TIER (damage 12-15, PCD 3-6s, ACD 10-18s, 10 particles)
    // ================================================================
    AMETHYST_SHURIKEN("amethyst_shuriken", "Amethyst Shuriken", Material.DIAMOND_SWORD, 13, 30012, LegendaryTier.COMMON, "amethyst_shuriken", "Shuriken Barrage", "Shadow Step", 5, 10),
    GRAVESCEPTER("gravescepter", "Gravescepter", Material.DIAMOND_SWORD, 13, 30026, LegendaryTier.COMMON, "revenants_gravescepter", "Grave Rise", "Death's Grasp", 5, 12),
    LYCANBANE("lycanbane", "Lycanbane", Material.DIAMOND_SWORD, 14, 30027, LegendaryTier.COMMON, "lycanbane", "Silver Strike", "Hunter's Sense", 5, 14),
    GLOOMSTEEL_KATANA("gloomsteel_katana", "Gloomsteel Katana", Material.DIAMOND_SWORD, 13, 30028, LegendaryTier.COMMON, "gloomsteel_katana", "Quick Draw", "Shadow Stance", 3, 12),
    VIRIDIAN_CLEAVER("viridian_cleaver", "Viridian Cleaver", Material.DIAMOND_AXE, 15, 30029, LegendaryTier.COMMON, "viridian_greataxe", "Verdant Slam", "Overgrowth", 5, 15),
    CRESCENT_EDGE("crescent_edge", "Crescent Edge", Material.DIAMOND_AXE, 14, 30030, LegendaryTier.COMMON, "crescent_greataxe", "Lunar Cleave", "Crescent Guard", 5, 14),
    GRAVECLEAVER("gravecleaver", "Gravecleaver", Material.DIAMOND_SWORD, 14, 30031, LegendaryTier.COMMON, "revenants_gravecleaver", "Bone Shatter", "Undying Rage", 5, 18),
    AMETHYST_GREATBLADE("amethyst_greatblade", "Amethyst Greatblade", Material.DIAMOND_SWORD, 13, 30032, LegendaryTier.COMMON, "amethyst_greatblade", "Crystal Burst", "Gem Resonance", 5, 18),
    FLAMBERGE("flamberge", "Flamberge", Material.DIAMOND_SWORD, 14, 30033, LegendaryTier.COMMON, "flamberge", "Flame Wave", "Ember Shield", 5, 12),
    CRYSTAL_FROSTBLADE("crystal_frostblade", "Crystal Frostblade", Material.DIAMOND_SWORD, 13, 30034, LegendaryTier.COMMON, "crystal_frostblade", "Frost Spike", "Permafrost", 5, 15),
    DEMONSLAYER("demonslayer", "Demonslayer", Material.DIAMOND_SWORD, 15, 30035, LegendaryTier.COMMON, "demonslayers_greatsword", "Holy Rend", "Purifying Aura", 5, 14),
    VENGEANCE("vengeance", "Vengeance", Material.DIAMOND_SWORD, 12, 30036, LegendaryTier.COMMON, "vengeance_blade", "Retribution", "Grudge Mark", 5, 10),
    OCULUS("oculus", "Oculus", Material.DIAMOND_SWORD, 13, 30037, LegendaryTier.COMMON, "oculus", "All-Seeing Strike", "Third Eye", 5, 18),
    ANCIENT_GREATSLAB("ancient_greatslab", "Ancient Greatslab", Material.DIAMOND_SWORD, 15, 30038, LegendaryTier.COMMON, "ancient_greatslab", "Seismic Slam", "Stone Skin", 5, 15),
    NEPTUNES_FANG("neptunes_fang", "Neptune's Fang", Material.TRIDENT, 14, 30039, LegendaryTier.COMMON, "neptunes_fang", "Riptide Slash", "Maelstrom", 5, 15),
    TIDECALLER("tidecaller", "Tidecaller", Material.TRIDENT, 13, 30040, LegendaryTier.COMMON, "tidecaller", "Tidal Spear", "Depth Ward", 5, 14),
    STORMFORK("stormfork", "Stormfork", Material.TRIDENT, 15, 30041, LegendaryTier.COMMON, "stormfork", "Lightning Javelin", "Thunder Shield", 5, 18),
    JADE_REAPER("jade_reaper", "Jade Reaper", Material.DIAMOND_HOE, 14, 30042, LegendaryTier.COMMON, "jadehalberd", "Jade Crescent", "Emerald Harvest", 5, 18),
    VINDICATOR("vindicator", "Vindicator", Material.DIAMOND_AXE, 13, 30043, LegendaryTier.COMMON, "vindicator", "Executioner's Chop", "Rally Cry", 5, 18),
    SPIDER_FANG("spider_fang", "Spider Fang", Material.DIAMOND_SWORD, 12, 30044, LegendaryTier.COMMON, "spider_sword", "Web Trap", "Wall Crawler", 5, 14),

    // ================================================================
    // RARE TIER (damage 14-17, PCD 5-7s, ACD 14-22s, 25 particles)
    // ================================================================
    OCEANS_RAGE("oceans_rage", "Ocean's Rage", Material.TRIDENT, 16, 30001, LegendaryTier.RARE, "oceans_rage", "Stormbringer", "Riptide Surge", 5, 18),
    AQUATIC_SACRED_BLADE("aquatic_sacred_blade", "Aquatic Sacred Blade", Material.DIAMOND_SWORD, 15, 30002, LegendaryTier.RARE, "aquantic_sacred_blade", "Aqua Heal", "Depth Pressure", 7, 18),
    ROYAL_CHAKRAM("royal_chakram", "Royal Chakram", Material.DIAMOND_SWORD, 14, 30005, LegendaryTier.RARE, "royalchakram", "Chakram Throw", "Spinning Shield", 5, 14),
    ACIDIC_CLEAVER("acidic_cleaver", "Acidic Cleaver", Material.DIAMOND_AXE, 16, 30007, LegendaryTier.RARE, "treacherous_cleaver", "Acid Splash", "Corrosive Aura", 7, 18),
    MURAMASA("muramasa", "Muramasa", Material.DIAMOND_SWORD, 15, 30009, LegendaryTier.RARE, "muramasa", "Crimson Flash", "Bloodlust", 5, 14),
    WINDREAPER("windreaper", "Windreaper", Material.DIAMOND_SWORD, 15, 30014, LegendaryTier.RARE, "windreaper", "Gale Slash", "Cyclone", 5, 14),
    MOONLIGHT("moonlight", "Moonlight", Material.DIAMOND_SWORD, 15, 30016, LegendaryTier.RARE, "moonlight", "Lunar Beam", "Eclipse", 7, 22),
    TALONBRAND("talonbrand", "Talonbrand", Material.DIAMOND_SWORD, 15, 30022, LegendaryTier.RARE, "talonbrand", "Talon Strike", "Predator's Mark", 5, 14),

    // ================================================================
    // EPIC TIER (damage 17-20, PCD 5-8s, ACD 18-30s, 50 particles)
    // ================================================================
    BERSERKERS_GREATAXE("berserkers_greataxe", "Berserker's Greataxe", Material.DIAMOND_AXE, 20, 30006, LegendaryTier.EPIC, "berserkers_greataxe", "Berserker Slam", "Blood Rage", 7, 22),
    BLACK_IRON_GREATSWORD("black_iron_greatsword", "Black Iron Greatsword", Material.DIAMOND_SWORD, 18, 30008, LegendaryTier.EPIC, "black_iron_greatsword", "Dark Slash", "Iron Fortress", 5, 22),
    SOLSTICE("solstice", "Solstice", Material.DIAMOND_SWORD, 17, 30018, LegendaryTier.EPIC, "solstice", "Solar Flare", "Daybreak", 7, 18),
    GRAND_CLAYMORE("grand_claymore", "Grand Claymore", Material.DIAMOND_SWORD, 19, 30019, LegendaryTier.EPIC, "grand_claymore", "Titan Swing", "Colossus Stance", 7, 22),
    CALAMITY_BLADE("calamity_blade", "Calamity Blade", Material.DIAMOND_AXE, 18, 30020, LegendaryTier.EPIC, "calamity_blade", "Cataclysm", "Doomsday", 8, 25),
    EMERALD_GREATCLEAVER("emerald_greatcleaver", "Emerald Greatcleaver", Material.DIAMOND_AXE, 19, 30023, LegendaryTier.EPIC, "emerald_greatcleaver", "Emerald Storm", "Gem Barrier", 7, 28),
    DEMONS_BLOOD_BLADE("demons_blood_blade", "Demon's Blood Blade", Material.DIAMOND_SWORD, 18, 30024, LegendaryTier.EPIC, "demons_blood_blade", "Blood Rite", "Demonic Form", 5, 25),

    // ================================================================
    // MYTHIC TIER (damage 20-28, PCD 5-10s, ACD 20-40s, 100 particles)
    // ================================================================
    TRUE_EXCALIBUR("true_excalibur", "True Excalibur", Material.DIAMOND_SWORD, 28, 30003, LegendaryTier.MYTHIC, "excalibur", "Holy Smite", "Divine Shield", 5, 20),
    REQUIEM_NINTH_ABYSS("requiem_ninth_abyss", "Requiem of the Ninth Abyss", Material.DIAMOND_SWORD, 28, 30004, LegendaryTier.MYTHIC, "requiem_of_hell", "Soul Devour", "Abyss Gate", 5, 28),
    PHOENIXS_GRACE("phoenixs_grace", "Phoenix's Grace", Material.DIAMOND_AXE, 28, 30010, LegendaryTier.MYTHIC, "gilded_phoenix_greataxe", "Phoenix Strike", "Rebirth Flame", 5, 28),
    SOUL_COLLECTOR("soul_collector", "Soul Collector", Material.DIAMOND_SWORD, 27, 30011, LegendaryTier.MYTHIC, "soul_collector", "Soul Harvest", "Spirit Army", 5, 15),
    VALHAKYRA("valhakyra", "Valhakyra", Material.DIAMOND_SWORD, 26, 30013, LegendaryTier.MYTHIC, "valhakyra", "Valkyrie Dive", "Wings of Valor", 5, 20),
    PHANTOMGUARD("phantomguard", "Phantomguard Greatsword", Material.DIAMOND_SWORD, 27, 30015, LegendaryTier.MYTHIC, "phantomguard_greatsword", "Spectral Cleave", "Phase Shift", 5, 18),
    ZENITH("zenith", "Zenith", Material.DIAMOND_SWORD, 30, 30017, LegendaryTier.MYTHIC, "zenith", "Final Judgment", "Ascension", 7, 28),
    DRAGON_SWORD("dragon_sword", "Dragon Sword", Material.DIAMOND_SWORD, 26, 30021, LegendaryTier.MYTHIC, "dragon_sword", "Dragon Breath", "Draconic Roar", 5, 20),
    NOCTURNE("nocturne", "Nocturne", Material.DIAMOND_SWORD, 26, 30025, LegendaryTier.MYTHIC, "nocturne", "Shadow Slash", "Night Cloak", 5, 20),
    DIVINE_AXE_RHITTA("divine_axe_rhitta", "Divine Axe Rhitta", Material.DIAMOND_AXE, 30, 30045, LegendaryTier.MYTHIC, "divine_axe_rhitta", "Cruel Sun", "Sunshine", 5, 25),
    YORU("yoru", "Yoru", Material.DIAMOND_SWORD, 28, 30046, LegendaryTier.MYTHIC, "yoru", "World's Strongest Slash", "Dark Mirror", 6, 26),
    TENGENS_BLADE("tengens_blade", "Tengen's Blade", Material.DIAMOND_SWORD, 27, 30047, LegendaryTier.MYTHIC, "tengens_blade", "Sound Breathing", "Constant Flux", 5, 20),
    EDGE_ASTRAL_PLANE("edge_astral_plane", "Edge of the Astral Plane", Material.DIAMOND_SWORD, 29, 30048, LegendaryTier.MYTHIC, "edge_astral_plane", "Astral Rend", "Planar Shift", 6, 28),
    FALLEN_GODS_SPEAR("fallen_gods_spear", "Fallen God's Spear", Material.DIAMOND_SWORD, 28, 30049, LegendaryTier.MYTHIC, "fallen_gods_spear", "Divine Impale", "Heaven's Fall", 5, 25),
    NATURE_SWORD("nature_sword", "Nature Sword", Material.DIAMOND_SWORD, 26, 30050, LegendaryTier.MYTHIC, "nature_sword", "Gaia's Wrath", "Overgrowth Surge", 5, 20),
    HEAVENLY_PARTISAN("heavenly_partisan", "Heavenly Partisan", Material.DIAMOND_SWORD, 27, 30051, LegendaryTier.MYTHIC, "heavenly_partisan", "Holy Lance", "Celestial Judgment", 5, 22),
    SOUL_DEVOURER("soul_devourer", "Soul Devourer", Material.DIAMOND_SWORD, 28, 30052, LegendaryTier.MYTHIC, "soul_devourer", "Soul Rip", "Devouring Maw", 5, 25),
    MJOLNIR("mjolnir", "Mjolnir", Material.MACE, 30, 30053, LegendaryTier.MYTHIC, "mjolnir", "Thunderstrike", "Bifrost Slam", 6, 26),
    THOUSAND_DEMON_DAGGERS("thousand_demon_daggers", "Thousand Demon Daggers", Material.DIAMOND_SWORD, 26, 30054, LegendaryTier.MYTHIC, "thousand_demon_daggers", "Demon Barrage", "Infernal Dance", 5, 18),
    STAR_EDGE("star_edge", "Star Edge", Material.DIAMOND_SWORD, 28, 30055, LegendaryTier.MYTHIC, "star_edge", "Cosmic Slash", "Supernova", 6, 26),
    RIVERS_OF_BLOOD("rivers_of_blood", "Rivers of Blood", Material.DIAMOND_SWORD, 27, 30056, LegendaryTier.MYTHIC, "rivers_of_blood", "Corpse Piler", "Blood Tsunami", 5, 20),
    DRAGON_SLAYING_BLADE("dragon_slaying_blade", "Dragon Slaying Blade", Material.DIAMOND_SWORD, 28, 30057, LegendaryTier.MYTHIC, "dragon_slaying_blade", "Dragon Pierce", "Slayer's Fury", 5, 25),
    STOP_SIGN("stop_sign", "Stop Sign", Material.DIAMOND_AXE, 26, 30058, LegendaryTier.MYTHIC, "stop_sign", "Full Stop", "Road Rage", 5, 18),
    CREATION_SPLITTER("creation_splitter", "Creation Splitter", Material.DIAMOND_SWORD, 30, 30059, LegendaryTier.MYTHIC, "creation_splitter", "Reality Cleave", "Genesis Break", 7, 28),

    // ================================================================
    // ABYSSAL TIER (damage 28-40, PCD 6-12s, ACD 30-50s, 200 particles)
    // ================================================================
    REQUIEM_AWAKENED("requiem_awakened", "Requiem of the Ninth Abyss (Awakened)", Material.DIAMOND_SWORD, 38, 30060, LegendaryTier.ABYSSAL, "requiem_awakened", "Abyssal Devour", "Void Collapse", 6, 35),
    EXCALIBUR_AWAKENED("excalibur_awakened", "True Excalibur (Awakened)", Material.DIAMOND_SWORD, 36, 30061, LegendaryTier.ABYSSAL, "excalibur_awakened", "Divine Annihilation", "Sacred Realm", 5, 30),
    CREATION_SPLITTER_AWAKENED("creation_splitter_awakened", "Creation Splitter (Awakened)", Material.DIAMOND_SWORD, 40, 30062, LegendaryTier.ABYSSAL, "creation_splitter_awakened", "Reality Shatter", "Big Bang", 8, 40),
    WHISPERWIND_AWAKENED("whisperwind_awakened", "Whisperwind (Awakened)", Material.DIAMOND_SWORD, 34, 30063, LegendaryTier.ABYSSAL, "whisperwind_awakened", "Silent Storm", "Phantom Cyclone", 5, 28);



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

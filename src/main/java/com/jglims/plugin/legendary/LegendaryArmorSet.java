package com.jglims.plugin.legendary;

import org.bukkit.Material;
import net.kyori.adventure.text.format.TextColor;

/**
 * LegendaryArmorSet - defines all legendary and craftable armor sets.
 * Phase 15 - 13 armor sets (6 craftable, 7 legendary).
 * Defense range: 12 (Reinforced Leather) to 50 (Abyssal Plate).
 */
public enum LegendaryArmorSet {

    // ========================================================
    // CRAFTABLE SETS (workbench recipes, breakable, lower tier)
    // ========================================================

    REINFORCED_LEATHER("reinforced_leather", "Reinforced Leather", LegendaryTier.COMMON,
            Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS,
            30201, 30202, 30203, 30204,
            12, "Agility: permanent Speed I, +10% dodge chance",
            "Peripheral Vision: shows mob health bars within 8 blocks",
            "Padded Core: +2 max HP",
            "Flexible Joints: no movement speed penalty in water",
            "Soft Landing: -30% fall damage",
            "reinforced_leather_helmet", "reinforced_leather_chestplate", "reinforced_leather_leggings", "reinforced_leather_boots"),

    COPPER_ARMOR("copper_armor", "Copper Armor", LegendaryTier.COMMON,
            Material.CHAINMAIL_HELMET, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS,
            30211, 30212, 30213, 30214,
            14, "Lightning Rod: nearby lightning redirects to you (no damage), grants Speed II 5s",
            "Static Charge: melee attackers take 1 shock damage",
            "Conductive Plating: immune to lightning damage",
            "Copper Patina: +10% mining speed",
            "Grounded Steps: immune to Slowness from electric sources",
            "copper_armor_helmet", "copper_armor_chestplate", "copper_armor_leggings", "copper_armor_boots"),

    CHAINMAIL_REINFORCED("chainmail_reinforced", "Chainmail Reinforced", LegendaryTier.COMMON,
            Material.CHAINMAIL_HELMET, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS,
            30221, 30222, 30223, 30224,
            15, "Iron Will: +15% knockback resistance, immune to Mining Fatigue",
            "Chain Mesh: -10% projectile damage taken",
            "Linked Rings: thorns 1 (1 damage to melee attackers)",
            "Chainmail Grip: no weapon knockback penalty",
            "Steel Treads: no movement slow on soul sand",
            "chainmail_reinforced_helmet", "chainmail_reinforced_chestplate", "chainmail_reinforced_leggings", "chainmail_reinforced_boots"),

    AMETHYST_ARMOR("amethyst_armor", "Amethyst Armor", LegendaryTier.COMMON,
            Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS,
            30231, 30232, 30233, 30234,
            16, "Resonance: XP orbs within 10 blocks are auto-collected, +20% XP gain",
            "Crystal Clarity: Blindness and Darkness immunity",
            "Harmonic Core: passive Regeneration I when below 30% HP",
            "Amethyst Pulse: nearby hostile mobs glow (6 block radius)",
            "Gem Steps: leaves trail of amethyst particles",
            "amethyst_armor_helmet", "amethyst_armor_chestplate", "amethyst_armor_leggings", "amethyst_armor_boots"),

    BONE_ARMOR("bone_armor", "Bone Armor", LegendaryTier.COMMON,
            Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS,
            30241, 30242, 30243, 30244,
            16, "Undead Camouflage: undead mobs ignore you unless attacked",
            "Skull Helm: immune to Wither effect",
            "Rib Cage: -15% damage from undead mobs",
            "Bone Marrow: passive Regeneration I at night",
            "Ossified Boots: immune to Slowness",
            "bone_armor_helmet", "bone_armor_chestplate", "bone_armor_leggings", "bone_armor_boots"),

    SCULK_ARMOR("sculk_armor", "Sculk Armor", LegendaryTier.RARE,
            Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS,
            30251, 30252, 30253, 30254,
            18, "Echo Sense: see all entities through walls within 12 blocks (glowing), Vibration immunity",
            "Sonic Sight: permanent Night Vision in caves (y < 50)",
            "Sculk Tendrils: melee attackers get Slowness I for 3s",
            "Deep Dark Legs: Darkness immunity, +10% speed in deep dark biome",
            "Silent Steps: no vibrations produced when walking",
            "sculk_armor_helmet", "sculk_armor_chestplate", "sculk_armor_leggings", "sculk_armor_boots"),

    // ========================================================
    // LEGENDARY SETS (boss/structure drops, unbreakable)
    // ========================================================

    SHADOW_STALKER("shadow_stalker", "Shadow Stalker", LegendaryTier.RARE,
            Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS,
            30141, 30142, 30143, 30144,
            24, "Invisibility while crouching (armor hidden), +50% sneak attack damage",
            "Dark Vision: permanent Night Vision while sneaking",
            "Shadow Cloak: +4 sneak attack damage, 15% dodge chance while sneaking",
            "Silent Movement: no footstep sounds, Speed I while sneaking",
            "Shadow Step: no footstep particles, no fall damage while sneaking",
            "shadow_stalker_helmet", "shadow_stalker_chestplate", "shadow_stalker_leggings", "shadow_stalker_boots"),

    BLOOD_MOON("blood_moon", "Blood Moon", LegendaryTier.EPIC,
            Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS,
            30111, 30112, 30113, 30114,
            30, "8% lifesteal on melee, +4 HP per kill (max 20 extra, reset on death)",
            "Blood Sight: glowing enemies 40 blocks at night, see entity HP bars",
            "Crimson Heart: +4 HP per kill (max 20 bonus), heals 2 HP on kill",
            "Blood Rush: Speed II at night, Strength I when below 50% HP",
            "Silent Predator: no footstep sounds, mobs don't aggro from 5+ blocks at night",
            "blood_moon_helmet", "blood_moon_chestplate", "blood_moon_leggings", "blood_moon_boots"),

    NATURES_EMBRACE("natures_embrace", "Nature's Embrace", LegendaryTier.EPIC,
            Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS,
            30131, 30132, 30133, 30134,
            28, "Passive Regeneration II in forest/taiga biomes, Poison/Wither immunity",
            "Photosynthesis: passive Regeneration I in sunlight, Poison immunity",
            "Living Bark: thorns 4 damage (nature particles), +4 max HP",
            "Root Grip: immune to knockback in forest biomes, vine climb on any surface",
            "Nature's Path: crops grow 3x faster in 8-block radius, flowers spawn where you walk on grass",
            "natures_embrace_helmet", "natures_embrace_chestplate", "natures_embrace_leggings", "natures_embrace_boots"),

    FROST_WARDEN("frost_warden", "Frost Warden", LegendaryTier.EPIC,
            Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS,
            30151, 30152, 30153, 30154,
            30, "Freeze nearby enemies (Slowness III, 5-block radius, 2s interval), immune to all cold",
            "Frost Resistance: immune to Slowness, Freezing, and powder snow",
            "Blizzard Aura: enemies within 4 blocks get Slowness II + Mining Fatigue I",
            "Permafrost Legs: Ice Walk 3-block radius, +20% move speed on ice/snow",
            "Frost Treads: lava creates obsidian on contact, fire/lava immunity, water freeze trail",
            "frost_warden_helmet", "frost_warden_chestplate", "frost_warden_leggings", "frost_warden_boots"),

    VOID_WALKER("void_walker", "Void Walker", LegendaryTier.MYTHIC,
            Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS,
            30121, 30122, 30123, 30124,
            36, "Void Step: crouch+jump teleport 12 blocks (4s CD), no ender pearl damage",
            "Ender Sight: see invisible mobs (glowing), immune to Blindness/Darkness",
            "Void Shield: 20% chance to completely negate damage (ender particles)",
            "Rift Walk: Slow Falling, phase through 1-block-thick walls while sneaking",
            "Void Anchor: no fall damage, no void damage below y=0 (teleport to surface)",
            "void_walker_helmet", "void_walker_chestplate", "void_walker_leggings", "void_walker_boots"),

    DRAGON_KNIGHT("dragon_knight", "Dragon Knight", LegendaryTier.MYTHIC,
            Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS,
            30101, 30102, 30103, 30104,
            40, "+35% damage vs dragon/ender mobs, Dragon Roar AoE (15 dmg, 8 blocks, 30s CD)",
            "Dragon's Eye: Respiration III + see all entities within 20 blocks (glowing)",
            "Dragon Heart: +8 max HP, Fire Resistance permanent",
            "Dragon Scales: 60% knockback resistance, +15% melee damage always",
            "Dragon Talons: Feather Falling V, double jump (3s CD)",
            "dragon_knight_helmet", "dragon_knight_chestplate", "dragon_knight_leggings", "dragon_knight_boots"),

    ABYSSAL_PLATE("abyssal_plate", "Abyssal Plate", LegendaryTier.ABYSSAL,
            Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS,
            30161, 30162, 30163, 30164,
            50, "+40% melee damage, Wither/Poison/Darkness immunity, soul-fire thorns 12 dmg",
            "Abyssal Vision: permanent Night Vision, Darkness/Blindness/Wither immunity",
            "Soul Furnace: soul-fire thorns 12 dmg, Wither immunity, +10 max HP",
            "Abyssal Stride: Speed II permanent, 80% knockback resistance, +20% melee damage",
            "Obsidian Walker: full fire/lava/magma immunity, lava walking (obsidian forms), no fall damage",
            "abyssal_plate_helmet", "abyssal_plate_chestplate", "abyssal_plate_leggings", "abyssal_plate_boots");

    private final String id;
    private final String displayName;
    private final LegendaryTier tier;
    private final Material helmetMat, chestplateMat, leggingsMat, bootsMat;
    private final int helmetCmd, chestplateCmd, leggingsCmd, bootsCmd;
    private final int totalDefense;
    private final String setBonusDescription;
    private final String helmetPassive, chestplatePassive, leggingsPassive, bootsPassive;
    private final String helmetTexture, chestplateTexture, leggingsTexture, bootsTexture;

    LegendaryArmorSet(String id, String displayName, LegendaryTier tier,
                      Material helmetMat, Material chestplateMat, Material leggingsMat, Material bootsMat,
                      int helmetCmd, int chestplateCmd, int leggingsCmd, int bootsCmd,
                      int totalDefense, String setBonusDescription,
                      String helmetPassive, String chestplatePassive, String leggingsPassive, String bootsPassive,
                      String helmetTexture, String chestplateTexture, String leggingsTexture, String bootsTexture) {
        this.id = id;
        this.displayName = displayName;
        this.tier = tier;
        this.helmetMat = helmetMat; this.chestplateMat = chestplateMat;
        this.leggingsMat = leggingsMat; this.bootsMat = bootsMat;
        this.helmetCmd = helmetCmd; this.chestplateCmd = chestplateCmd;
        this.leggingsCmd = leggingsCmd; this.bootsCmd = bootsCmd;
        this.totalDefense = totalDefense;
        this.setBonusDescription = setBonusDescription;
        this.helmetPassive = helmetPassive; this.chestplatePassive = chestplatePassive;
        this.leggingsPassive = leggingsPassive; this.bootsPassive = bootsPassive;
        this.helmetTexture = helmetTexture; this.chestplateTexture = chestplateTexture;
        this.leggingsTexture = leggingsTexture; this.bootsTexture = bootsTexture;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public LegendaryTier getTier() { return tier; }
    public Material getHelmetMat() { return helmetMat; }
    public Material getChestplateMat() { return chestplateMat; }
    public Material getLeggingsMat() { return leggingsMat; }
    public Material getBootsMat() { return bootsMat; }
    public int getHelmetCmd() { return helmetCmd; }
    public int getChestplateCmd() { return chestplateCmd; }
    public int getLeggingsCmd() { return leggingsCmd; }
    public int getBootsCmd() { return bootsCmd; }
    public int getTotalDefense() { return totalDefense; }
    public String getSetBonusDescription() { return setBonusDescription; }
    public String getHelmetPassive() { return helmetPassive; }
    public String getChestplatePassive() { return chestplatePassive; }
    public String getLeggingsPassive() { return leggingsPassive; }
    public String getBootsPassive() { return bootsPassive; }
    public String getHelmetTexture() { return helmetTexture; }
    public String getChestplateTexture() { return chestplateTexture; }
    public String getLeggingsTexture() { return leggingsTexture; }
    public String getBootsTexture() { return bootsTexture; }

    public boolean isCraftable() {
        return this == REINFORCED_LEATHER || this == COPPER_ARMOR || this == CHAINMAIL_REINFORCED
            || this == AMETHYST_ARMOR || this == BONE_ARMOR || this == SCULK_ARMOR;
    }

    public Material getMaterialForSlot(ArmorSlot slot) {
        return switch (slot) {
            case HELMET -> helmetMat;
            case CHESTPLATE -> chestplateMat;
            case LEGGINGS -> leggingsMat;
            case BOOTS -> bootsMat;
        };
    }

    public int getCmdForSlot(ArmorSlot slot) {
        return switch (slot) {
            case HELMET -> helmetCmd;
            case CHESTPLATE -> chestplateCmd;
            case LEGGINGS -> leggingsCmd;
            case BOOTS -> bootsCmd;
        };
    }

    public String getPassiveForSlot(ArmorSlot slot) {
        return switch (slot) {
            case HELMET -> helmetPassive;
            case CHESTPLATE -> chestplatePassive;
            case LEGGINGS -> leggingsPassive;
            case BOOTS -> bootsPassive;
        };
    }

    public String getTextureForSlot(ArmorSlot slot) {
        return switch (slot) {
            case HELMET -> helmetTexture;
            case CHESTPLATE -> chestplateTexture;
            case LEGGINGS -> leggingsTexture;
            case BOOTS -> bootsTexture;
        };
    }

    public static LegendaryArmorSet fromId(String id) {
        if (id == null) return null;
        for (LegendaryArmorSet set : values()) {
            if (set.id.equals(id)) return set;
        }
        return null;
    }

    public static LegendaryArmorSet[] craftableSets() {
        java.util.List<LegendaryArmorSet> result = new java.util.ArrayList<>();
        for (LegendaryArmorSet set : values()) {
            if (set.isCraftable()) result.add(set);
        }
        return result.toArray(new LegendaryArmorSet[0]);
    }

    public static LegendaryArmorSet[] legendarySets() {
        java.util.List<LegendaryArmorSet> result = new java.util.ArrayList<>();
        for (LegendaryArmorSet set : values()) {
            if (!set.isCraftable()) result.add(set);
        }
        return result.toArray(new LegendaryArmorSet[0]);
    }

    public enum ArmorSlot {
        HELMET, CHESTPLATE, LEGGINGS, BOOTS;

        public static ArmorSlot fromMaterial(Material mat) {
            String name = mat.name();
            if (name.contains("HELMET") || name.contains("CAP")) return HELMET;
            if (name.contains("CHESTPLATE") || name.contains("TUNIC")) return CHESTPLATE;
            if (name.contains("LEGGINGS") || name.contains("PANTS")) return LEGGINGS;
            if (name.contains("BOOTS")) return BOOTS;
            return null;
        }
    }
}
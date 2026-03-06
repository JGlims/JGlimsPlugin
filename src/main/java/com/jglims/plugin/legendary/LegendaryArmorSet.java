package com.jglims.plugin.legendary;

import org.bukkit.Material;
import net.kyori.adventure.text.format.TextColor;

/**
 * LegendaryArmorSet - defines all legendary and craftable armor sets.
 * Phase 15 - Legendary Armor Sets.
 */
public enum LegendaryArmorSet {

    // LEGENDARY SETS (indestructible, boss/structure drops)
    DRAGON_KNIGHT("dragon_knight", "Dragon Knight", LegendaryTier.MYTHIC,
            Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS,
            30101, 30102, 30103, 30104,
            24, "+20% damage vs dragon-type mobs",
            "Respiration III equivalent", "+4 max HP", "Knockback resistance 50%", "Feather Falling V equivalent",
            "dragon_knight_helmet", "dragon_knight_chestplate", "dragon_knight_leggings", "dragon_knight_boots"),

    BLOOD_MOON("blood_moon", "Blood Moon", LegendaryTier.EPIC,
            Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS,
            30111, 30112, 30113, 30114,
            20, "5% lifesteal on all melee hits",
            "Glowing enemies within 30 blocks at night", "+2 HP per kill (max 10 extra, resets on death)", "Speed I at night", "Silent steps (no footstep sounds)",
            "blood_moon_helmet", "blood_moon_chestplate", "blood_moon_leggings", "blood_moon_boots"),

    VOID_WALKER("void_walker", "Void Walker", LegendaryTier.MYTHIC,
            Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS,
            30121, 30122, 30123, 30124,
            22, "Crouch+Jump to short-range teleport (8 blocks, 5s CD)",
            "See invisible mobs (glowing)", "No ender pearl damage", "Slow Falling", "No fall damage",
            "void_walker_helmet", "void_walker_chestplate", "void_walker_leggings", "void_walker_boots"),

    NATURES_EMBRACE("natures_embrace", "Nature's Embrace", LegendaryTier.EPIC,
            Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS,
            30131, 30132, 30133, 30134,
            18, "Passive Regeneration I in forest biomes",
            "Poison immunity", "Thorn damage (nature particles)", "Vine climb (climb any block while sneaking)", "Crop growth boost 3x in 5-block radius",
            "natures_embrace_helmet", "natures_embrace_chestplate", "natures_embrace_leggings", "natures_embrace_boots"),

    SHADOW_STALKER("shadow_stalker", "Shadow Stalker", LegendaryTier.RARE,
            Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS,
            30141, 30142, 30143, 30144,
            16, "Invisibility while crouching (armor hidden)",
            "Dark Vision (Night Vision while sneaking)", "+2 sneak attack damage", "Silent movement", "No footstep particles",
            "shadow_stalker_helmet", "shadow_stalker_chestplate", "shadow_stalker_leggings", "shadow_stalker_boots"),

    ABYSSAL_PLATE("abyssal_plate", "Abyssal Plate", LegendaryTier.ABYSSAL,
            Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS,
            30161, 30162, 30163, 30164,
            28, "+30% melee damage, Wither immunity",
            "Permanent Night Vision + Darkness immunity", "Soul-fire thorns (8 dmg to attackers) + Wither immunity", "Speed I permanent + 60% knockback resistance", "Full fire/lava immunity + obsidian walker",
            "abyssal_plate_helmet", "abyssal_plate_chestplate", "abyssal_plate_leggings", "abyssal_plate_boots"),

        FROST_WARDEN("frost_warden", "Frost Warden", LegendaryTier.EPIC,
            Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS,
            30151, 30152, 30153, 30154,
            20, "Freeze nearby enemies (Slowness II, 4-block radius, 3s interval)",
            "Frost Resistance (no Slowness/Freezing)", "Slowness aura (enemies in 3 blocks)", "Ice Walk (water freezes in 2-block radius)", "Lava creates obsidian on contact, fire immunity",
            "frost_warden_helmet", "frost_warden_chestplate", "frost_warden_leggings", "frost_warden_boots");

    // ABYSSAL_PLATE added in rebalance update

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
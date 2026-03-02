package com.jglims.plugin.enchantments;

public enum EnchantmentType {
    // Sword
    VAMPIRISM, BLEED, VENOMSTRIKE, LIFESTEAL,
    // Axe
    BERSERKER, LUMBERJACK, CLEAVE, TIMBER, GUILLOTINE,
    // Pickaxe
    VEINMINER, DRILL, AUTO_SMELT, MAGNETISM,
    // Shovel
    EXCAVATOR, REPLENISH,
    // Hoe
    HARVESTER, GREEN_THUMB,
    // Trident
    THUNDERLORD, SWIFTNESS, VITALITY, TIDAL_WAVE,
    // Universal melee
    FROSTBITE,
    // Bow & Crossbow
    EXPLOSIVE_ARROW, HOMING,
    // Crossbow only
    RAPIDFIRE,
    // Bow only
    SNIPER,
    // Helmet
    NIGHT_VISION, AQUA_LUNGS,
    // Chestplate
    FORTIFICATION, DEFLECTION,
    // Leggings
    SWIFTFOOT, DODGE,
    // Boots
    LEAPING, STOMP,
    // Elytra
    CUSHION, BOOST, GLIDER,
    // Universal
    SOULBOUND,
    // Dog armor
    BEST_BUDDIES,
    // Sickle
    SOUL_REAP, BLOOD_PRICE, REAPERS_MARK, WITHER_TOUCH;

    public int getMaxLevel() {
        return switch (this) {
            case VAMPIRISM, BERSERKER, THUNDERLORD -> 5;
            case BLEED, VENOMSTRIKE, LIFESTEAL, LUMBERJACK, CLEAVE, TIMBER,
                 GUILLOTINE, VEINMINER, DRILL, EXCAVATOR, HARVESTER,
                 SWIFTNESS, VITALITY, TIDAL_WAVE, FROSTBITE,
                 EXPLOSIVE_ARROW, HOMING, RAPIDFIRE, SNIPER,
                 AQUA_LUNGS, FORTIFICATION, DEFLECTION, SWIFTFOOT,
                 DODGE, LEAPING, STOMP, BOOST,
                 SOUL_REAP, BLOOD_PRICE, REAPERS_MARK, WITHER_TOUCH -> 3;
            case AUTO_SMELT, MAGNETISM, REPLENISH, GREEN_THUMB,
                 NIGHT_VISION, CUSHION, GLIDER, SOULBOUND, BEST_BUDDIES -> 1;
        };
    }
}
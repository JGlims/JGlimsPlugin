package com.jglims.plugin.enchantments;

public enum EnchantmentType {

    // === Sword ===
    VAMPIRISM(5),
    BLEED(3),
    VENOMSTRIKE(3),
    LIFESTEAL(3),
    CHAIN_LIGHTNING(3),
    FROSTBITE_BLADE(3),  // NEW Phase 9

    // === Axe ===
    BERSERKER(5),
    LUMBERJACK(3),
    CLEAVE(3),
    TIMBER(3),
    GUILLOTINE(3),
    WRATH(3),  // NEW Phase 9

    // === Pickaxe ===
    VEINMINER(3),
    DRILL(3),
    AUTO_SMELT(1),
    MAGNETISM(1),
    EXCAVATOR(3),
    PROSPECTOR(3),  // NEW Phase 9

    // === Shovel ===
    HARVESTER(3),
    BURIAL(3),        // NEW Phase 9
    EARTHSHATTER(3),  // NEW Phase 9

    // === Hoe / Sickle ===
    GREEN_THUMB(1),
    REPLENISH(1),
    HARVESTING_MOON(3),
    SOUL_HARVEST(3),   // NEW Phase 9
    REAPING_CURSE(3),  // NEW Phase 9
    CROP_REAPER(1),    // NEW Phase 9

    // === Bow ===
    EXPLOSIVE_ARROW(3),
    HOMING(3),
    RAPIDFIRE(3),
    SNIPER(3),
    FROSTBITE_ARROW(3),  // NEW Phase 9

    // === Crossbow ===
    THUNDERLORD(5),
    TIDAL_WAVE(3),

    // === Trident ===
    FROSTBITE(3),
    SOUL_REAP(3),
    BLOOD_PRICE(3),
    REAPERS_MARK(3),
    WITHER_TOUCH(3),
    TSUNAMI(3),  // NEW Phase 9

    // === Armor ===
    SWIFTNESS(3),
    VITALITY(3),
    AQUA_LUNGS(3),
    NIGHT_VISION(1),
    FORTIFICATION(3),
    DEFLECTION(3),
    SWIFTFOOT(3),
    DODGE(3),
    LEAPING(3),

    // === Elytra ===
    BOOST(3),
    CUSHION(1),
    GLIDER(1),
    STOMP(3),

    // === Mace ===
    SEISMIC_SLAM(3),
    MAGNETIZE(3),
    GRAVITY_WELL(3),
    MOMENTUM(3),
    TREMOR(3),  // NEW Phase 9

    // === Spear ===
    IMPALING_THRUST(3),
    EXTENDED_REACH(3),
    SKEWERING(3),
    PHANTOM_PIERCE(3),  // NEW Phase 9

    // === Universal ===
    SOULBOUND(1),
    BEST_BUDDIES(1);

    private final int maxLevel;

    EnchantmentType(int maxLevel) {
        this.maxLevel = maxLevel;
    }

    public int getMaxLevel() {
        return maxLevel;
    }
}

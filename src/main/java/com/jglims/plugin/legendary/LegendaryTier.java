package com.jglims.plugin.legendary;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

/**
 * LegendaryTier - standalone 5-tier enum for all legendary items.
 * Replaces the old inner enum LegendaryWeapon.LegendaryTier (LEGENDARY/UNCOMMON).
 * v3.0.0 Phase 8c
 */
public enum LegendaryTier {

    COMMON(
        "COMMON", "\u2726 COMMON \u2726", NamedTextColor.WHITE,
        10, 13, 5, 8, 15, 25, 10, 1.0
    ),
    RARE(
        "RARE", "\u2726 RARE \u2726", NamedTextColor.GREEN,
        12, 15, 7, 10, 20, 30, 25, 0.95
    ),
    EPIC(
        "EPIC", "\u2726 EPIC \u2726", NamedTextColor.DARK_PURPLE,
        14, 17, 8, 12, 25, 45, 50, 0.9
    ),
    MYTHIC(
        "MYTHIC", "\u2726 MYTHIC \u2726", NamedTextColor.GOLD,
        16, 22, 10, 15, 30, 60, 100, 0.85
    ),
    ABYSSAL(
        "ABYSSAL", "\u2726 ABYSSAL \u2726", TextColor.color(170, 0, 0),
        22, 30, 12, 18, 45, 90, 200, 0.8
    );

    private final String id;
    private final String displayTag;
    private final TextColor color;
    private final int minDamage;
    private final int maxDamage;
    private final int minPrimaryCooldown;
    private final int maxPrimaryCooldown;
    private final int minAltCooldown;
    private final int maxAltCooldown;
    private final int particleBudget;
    private final double cooldownMultiplier;

    LegendaryTier(String id, String displayTag, TextColor color,
                  int minDamage, int maxDamage,
                  int minPrimaryCooldown, int maxPrimaryCooldown,
                  int minAltCooldown, int maxAltCooldown,
                  int particleBudget, double cooldownMultiplier) {
        this.id = id;
        this.displayTag = displayTag;
        this.color = color;
        this.minDamage = minDamage;
        this.maxDamage = maxDamage;
        this.minPrimaryCooldown = minPrimaryCooldown;
        this.maxPrimaryCooldown = maxPrimaryCooldown;
        this.minAltCooldown = minAltCooldown;
        this.maxAltCooldown = maxAltCooldown;
        this.particleBudget = particleBudget;
        this.cooldownMultiplier = cooldownMultiplier;
    }

    public String getId() { return id; }
    public String getDisplayTag() { return displayTag; }
    public TextColor getColor() { return color; }
    public int getMinDamage() { return minDamage; }
    public int getMaxDamage() { return maxDamage; }
    public int getMinPrimaryCooldown() { return minPrimaryCooldown; }
    public int getMaxPrimaryCooldown() { return maxPrimaryCooldown; }
    public int getMinAltCooldown() { return minAltCooldown; }
    public int getMaxAltCooldown() { return maxAltCooldown; }
    public int getParticleBudget() { return particleBudget; }
    public double getCooldownMultiplier() { return cooldownMultiplier; }

    public int getPower() { return ordinal(); }

    public boolean isAtLeast(LegendaryTier other) {
        return this.ordinal() >= other.ordinal();
    }

    public static LegendaryTier fromId(String id) {
        if (id == null) return null;
        String upper = id.toUpperCase().trim();
        for (LegendaryTier tier : values()) {
            if (tier.id.equals(upper)) return tier;
        }
        if ("LEGENDARY".equals(upper)) return EPIC;
        if ("UNCOMMON".equals(upper)) return COMMON;
        return null;
    }

    public boolean validateDamage(int damage) {
        return damage >= minDamage && damage <= maxDamage;
    }

    public boolean validatePrimaryCooldown(int cd) {
        return cd >= minPrimaryCooldown && cd <= maxPrimaryCooldown;
    }

    public boolean validateAltCooldown(int cd) {
        return cd >= minAltCooldown && cd <= maxAltCooldown;
    }
}
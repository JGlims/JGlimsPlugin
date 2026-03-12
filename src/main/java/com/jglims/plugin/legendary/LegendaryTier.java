package com.jglims.plugin.legendary;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

/**
 * LegendaryTier - standalone 5-tier enum for all legendary items.
 * v3.2.0 Phase 10 - Updated ranges for reduced cooldowns and increased damage.
 */
public enum LegendaryTier {

    COMMON(
        "COMMON", "\u2726 COMMON \u2726", NamedTextColor.WHITE,
        12, 15, 3, 6, 10, 18, 10, 1.0
    ),
    RARE(
        "RARE", "\u2726 RARE \u2726", NamedTextColor.GREEN,
        14, 17, 5, 7, 14, 22, 25, 0.95
    ),
    EPIC(
        "EPIC", "\u2726 EPIC \u2726", NamedTextColor.DARK_PURPLE,
        17, 20, 5, 8, 18, 30, 50, 0.9
    ),
    MYTHIC(
        "MYTHIC", "\u2726 MYTHIC \u2726", NamedTextColor.GOLD,
        20, 30, 5, 10, 15, 40, 100, 0.85
    ),
    ABYSSAL(
        "ABYSSAL", "\u2726 ABYSSAL \u2726", TextColor.color(170, 0, 0),
        28, 40, 5, 12, 28, 50, 200, 0.8
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

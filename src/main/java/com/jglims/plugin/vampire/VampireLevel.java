package com.jglims.plugin.vampire;

import net.kyori.adventure.text.format.TextColor;

/**
 * Vampire progression levels, each with scaling stat bonuses.
 */
public enum VampireLevel {

    FLEDGLING("Fledgling", TextColor.color(180, 40, 40),
            8, 0, 0, 0.5, 0.5, 0.5, 2.0),
    VAMPIRE("Vampire", TextColor.color(200, 30, 30),
            12, 12, 0, 1.0, 1.0, 0.7, 3.0),
    ELDER_VAMPIRE("Elder Vampire", TextColor.color(220, 20, 20),
            16, 18, 15, 1.5, 1.2, 0.8, 5.0),
    VAMPIRE_LORD("Vampire Lord", TextColor.color(240, 10, 10),
            20, 24, 25, 1.8, 1.4, 0.85, 8.0),
    DRACULA("Dracula", TextColor.color(100, 0, 0),
            25, 32, 40, 2.0, 1.5, 0.9, 12.0);

    private final String displayName;
    private final TextColor color;
    private final double clawDamage;
    private final double teethDamage;
    private final double bloodShootDamage;
    private final double speedBonus;
    private final double jumpBonus;
    private final double fallDamageReduction;
    private final double baseDefense;

    VampireLevel(String displayName, TextColor color,
                 double clawDamage, double teethDamage, double bloodShootDamage,
                 double speedBonus, double jumpBonus, double fallDamageReduction,
                 double baseDefense) {
        this.displayName = displayName;
        this.color = color;
        this.clawDamage = clawDamage;
        this.teethDamage = teethDamage;
        this.bloodShootDamage = bloodShootDamage;
        this.speedBonus = speedBonus;
        this.jumpBonus = jumpBonus;
        this.fallDamageReduction = fallDamageReduction;
        this.baseDefense = baseDefense;
    }

    public String getDisplayName() { return displayName; }
    public TextColor getColor() { return color; }
    public double getClawDamage() { return clawDamage; }
    public double getTeethDamage() { return teethDamage; }
    public double getBloodShootDamage() { return bloodShootDamage; }
    public double getSpeedBonus() { return speedBonus; }
    public double getJumpBonus() { return jumpBonus; }
    public double getFallDamageReduction() { return fallDamageReduction; }
    public double getBaseDefense() { return baseDefense; }
}

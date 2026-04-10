package com.jglims.plugin.vampire;

import java.util.*;

/**
 * Holds a single player's complete vampire state. Persisted to flat file.
 */
public class VampireState {

    private final UUID playerUUID;
    private boolean isVampire;
    private VampireLevel level;
    private int bloodConsumed;
    private int evolversConsumed;
    private int superBloodConsumed;
    private boolean hasVampireRing;
    private final Set<String> unlockedAbilities;

    public VampireState(UUID playerUUID) {
        this.playerUUID = playerUUID;
        this.isVampire = false;
        this.level = VampireLevel.FLEDGLING;
        this.bloodConsumed = 0;
        this.evolversConsumed = 0;
        this.superBloodConsumed = 0;
        this.hasVampireRing = false;
        this.unlockedAbilities = new HashSet<>();
    }

    // ── Progression ────────────────────────────────────────────────────

    /**
     * Recalculates the vampire level based on consumables used.
     */
    public void recalculateLevel() {
        if (!isVampire) return;
        if (superBloodConsumed >= 5) {
            level = VampireLevel.DRACULA;
        } else if (evolversConsumed >= 20) {
            level = VampireLevel.VAMPIRE_LORD;
        } else if (evolversConsumed >= 10) {
            level = VampireLevel.ELDER_VAMPIRE;
        } else if (bloodConsumed >= 100) {
            level = VampireLevel.VAMPIRE;
        } else {
            level = VampireLevel.FLEDGLING;
        }
        recalculateAbilities();
    }

    /**
     * Updates which abilities are unlocked based on current level.
     */
    private void recalculateAbilities() {
        unlockedAbilities.clear();
        unlockedAbilities.add("vampire_claw"); // Always available
        if (level.ordinal() >= VampireLevel.VAMPIRE.ordinal()) {
            unlockedAbilities.add("vampire_teeth");
        }
        if (level.ordinal() >= VampireLevel.ELDER_VAMPIRE.ordinal()) {
            unlockedAbilities.add("vampire_teeth_alt");
            unlockedAbilities.add("bat_swarm");
        }
        if (level.ordinal() >= VampireLevel.VAMPIRE_LORD.ordinal()) {
            unlockedAbilities.add("bat_transform");
            unlockedAbilities.add("blood_nova");
            unlockedAbilities.add("crimson_mark");
        }
        if (level == VampireLevel.DRACULA) {
            unlockedAbilities.add("dracula_wrath");
            unlockedAbilities.add("domain_of_night");
        }
    }

    // ── Calculated Stats ───────────────────────────────────────────────

    /**
     * Returns effective claw damage, including blood consumption scaling.
     */
    public double getEffectiveClawDamage() {
        double base = level.getClawDamage();
        double bloodBonus = Math.min(bloodConsumed, 100) * 0.05;
        double evolverBonus = Math.min(evolversConsumed, 20) * 0.3;
        return base + bloodBonus + evolverBonus;
    }

    /**
     * Returns effective teeth damage.
     */
    public double getEffectiveTeethDamage() {
        double base = level.getTeethDamage();
        double bloodBonus = Math.min(bloodConsumed, 100) * 0.06;
        double evolverBonus = Math.min(evolversConsumed, 20) * 0.4;
        return base + bloodBonus + evolverBonus;
    }

    /**
     * Returns effective blood shoot damage.
     */
    public double getEffectiveBloodShootDamage() {
        double base = level.getBloodShootDamage();
        double evolverBonus = Math.min(evolversConsumed, 20) * 0.5;
        return base + evolverBonus;
    }

    /**
     * Whether the player is immune to sun damage.
     */
    public boolean isSunImmune() {
        return hasVampireRing || level == VampireLevel.DRACULA;
    }

    // ── Getters / Setters ──────────────────────────────────────────────

    public UUID getPlayerUUID() { return playerUUID; }
    public boolean isVampire() { return isVampire; }
    public void setVampire(boolean vampire) { isVampire = vampire; }
    public VampireLevel getLevel() { return level; }
    public void setLevel(VampireLevel level) { this.level = level; }
    public int getBloodConsumed() { return bloodConsumed; }
    public void setBloodConsumed(int bloodConsumed) { this.bloodConsumed = bloodConsumed; }
    public int getEvolversConsumed() { return evolversConsumed; }
    public void setEvolversConsumed(int evolversConsumed) { this.evolversConsumed = evolversConsumed; }
    public int getSuperBloodConsumed() { return superBloodConsumed; }
    public void setSuperBloodConsumed(int superBloodConsumed) { this.superBloodConsumed = superBloodConsumed; }
    public boolean hasVampireRing() { return hasVampireRing; }
    public void setHasVampireRing(boolean hasVampireRing) { this.hasVampireRing = hasVampireRing; }
    public Set<String> getUnlockedAbilities() { return Collections.unmodifiableSet(unlockedAbilities); }
}

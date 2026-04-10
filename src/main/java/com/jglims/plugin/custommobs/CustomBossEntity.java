package com.jglims.plugin.custommobs;

import com.jglims.plugin.JGlimsPlugin;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Abstract base for boss-type custom mobs. Adds boss bar management,
 * a phase system with configurable HP thresholds, attack pattern selection,
 * arena detection, a combat loop, death sequences, respawn cooldown,
 * and enrage mechanics.
 * <p>
 * Expected .bbmodel animations beyond the standard set: enrage, roar.
 */
public abstract class CustomBossEntity extends CustomMobEntity {

    /** Adventure boss bar shown to nearby players. */
    protected BossBar bossBar;

    /** Current combat phase (0-indexed). */
    protected int currentPhase = 0;

    /** HP percentage thresholds for phase transitions (descending order). */
    protected double[] phaseThresholds = {0.5, 0.25, 0.10};

    /** Ticks since the boss entered combat. */
    protected long combatTicks = 0;

    /** Whether the boss is currently in combat with players. */
    protected boolean inCombat = false;

    /** Map of player UUIDs to total damage dealt (for loot distribution). */
    protected final Map<UUID, Double> damageContributors = new HashMap<>();

    /** Cooldown tracking for attacks (attack name → tick of last use). */
    protected final Map<String, Long> attackCooldowns = new HashMap<>();

    /** Radius within which the boss detects and engages players. */
    protected double aggroRadius = 40.0;

    /** Radius of the boss arena (used for keeping players/boss in bounds). */
    protected double arenaRadius = 40.0;

    /** Center of the boss arena. Set during spawn or structure placement. */
    protected Location arenaCenter;

    /** Respawn cooldown in milliseconds. -1 means no respawn. */
    protected long respawnCooldownMs = -1;

    /** Whether the boss is enraged (below final phase threshold). */
    protected boolean enraged = false;

    protected CustomBossEntity(JGlimsPlugin plugin, CustomMobType mobType) {
        super(plugin, mobType);
    }

    // ── Lifecycle Overrides ────────────────────────────────────────────

    @Override
    public void spawn(Location location) {
        arenaCenter = location.clone();
        super.spawn(location);
    }

    @Override
    protected void onSpawn() {
        bossBar = BossBar.bossBar(
                Component.text(mobType.getDisplayName(), NamedTextColor.RED).decorate(TextDecoration.BOLD),
                1.0f,
                getBossBarColor(),
                getBossBarOverlay());
    }

    @Override
    protected void onTick() {
        updateBossBar();

        if (!inCombat) {
            Player nearest = findNearestPlayer(aggroRadius);
            if (nearest != null) {
                enterCombat();
            }
        } else {
            combatTicks++;
            checkPhaseTransitions();
            List<Player> nearby = findNearbyPlayers(aggroRadius * 1.5);
            if (nearby.isEmpty()) {
                leaveCombat();
                return;
            }
            combatTick();
        }
    }

    @Override
    protected void onDamage(double amount, Player source) {
        if (source != null) {
            damageContributors.merge(source.getUniqueId(), amount, Double::sum);
        }
        if (!inCombat) {
            enterCombat();
        }
    }

    @Override
    protected void onDeath(Player killer) {
        if (bossBar != null) {
            for (Player p : findNearbyPlayers(100)) {
                p.hideBossBar(bossBar);
            }
        }
        // XP orbs and sound
        if (hitboxEntity != null) {
            Location loc = hitboxEntity.getLocation();
            loc.getWorld().playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 2.0f, 0.8f);
        }
        inCombat = false;
    }

    @Override
    protected void cleanup() {
        if (bossBar != null) {
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                p.hideBossBar(bossBar);
            }
        }
        super.cleanup();
    }

    @Override
    protected long getTickRate() {
        return 2L; // Boss tick every 2 ticks (10 TPS combat loop)
    }

    // ── Combat ─────────────────────────────────────────────────────────

    /**
     * Called every tick while in combat. Subclasses implement their
     * attack pattern selection here.
     */
    protected abstract void combatTick();

    /**
     * Enters combat mode. Shows boss bar to nearby players.
     */
    protected void enterCombat() {
        inCombat = true;
        combatTicks = 0;
        List<Player> nearby = findNearbyPlayers(aggroRadius * 1.5);
        for (Player p : nearby) {
            p.showBossBar(bossBar);
        }
        playAnimation("walk", kr.toxicity.model.api.animation.AnimationIterator.Type.LOOP);
    }

    /**
     * Leaves combat. Hides boss bar, resets to idle.
     */
    protected void leaveCombat() {
        inCombat = false;
        combatTicks = 0;
        if (bossBar != null) {
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                p.hideBossBar(bossBar);
            }
        }
        playAnimation("idle", kr.toxicity.model.api.animation.AnimationIterator.Type.LOOP);
        // Heal to full if combat ends with no kill
        if (hitboxEntity != null) {
            hitboxEntity.setHealth(mobType.getMaxHealth());
        }
        currentPhase = 0;
        enraged = false;
        damageContributors.clear();
    }

    // ── Phase System ───────────────────────────────────────────────────

    /**
     * Checks if the boss should transition to a new phase based on HP percentage.
     */
    protected void checkPhaseTransitions() {
        if (hitboxEntity == null) return;
        double hpPercent = hitboxEntity.getHealth() / mobType.getMaxHealth();
        int newPhase = 0;
        for (double threshold : phaseThresholds) {
            if (hpPercent <= threshold) {
                newPhase++;
            } else {
                break;
            }
        }
        if (newPhase > currentPhase) {
            int oldPhase = currentPhase;
            currentPhase = newPhase;
            onPhaseTransition(oldPhase, newPhase);
        }
    }

    /**
     * Called when the boss transitions to a new phase.
     *
     * @param oldPhase the previous phase index
     * @param newPhase the new phase index
     */
    protected void onPhaseTransition(int oldPhase, int newPhase) {
        plugin.getLogger().info("[Boss] " + mobType.getDisplayName()
                + " transitioning from phase " + oldPhase + " to " + newPhase);
        // Announce phase transition to nearby players
        for (Player p : findNearbyPlayers(aggroRadius * 1.5)) {
            p.sendMessage(Component.text(mobType.getDisplayName() + " enters Phase " + (newPhase + 1) + "!",
                    NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD));
        }
    }

    // ── Boss Bar ───────────────────────────────────────────────────────

    /**
     * Updates the boss bar progress and manages player visibility.
     */
    protected void updateBossBar() {
        if (bossBar == null || hitboxEntity == null) return;
        float progress = (float) (hitboxEntity.getHealth() / mobType.getMaxHealth());
        bossBar.progress(Math.max(0f, Math.min(1f, progress)));

        // Show/hide boss bar for players entering/leaving range
        if (inCombat) {
            for (Player p : findNearbyPlayers(aggroRadius * 1.5)) {
                p.showBossBar(bossBar);
            }
        }
    }

    /**
     * Returns the boss bar color for this boss. Override to customize.
     */
    protected BossBar.Color getBossBarColor() {
        return BossBar.Color.RED;
    }

    /**
     * Returns the boss bar overlay style. Override to customize.
     */
    protected BossBar.Overlay getBossBarOverlay() {
        return BossBar.Overlay.NOTCHED_10;
    }

    // ── Attack Helpers ─────────────────────────────────────────────────

    /**
     * Checks if an attack is off cooldown.
     *
     * @param attackName the attack identifier
     * @param cooldownTicks cooldown duration in ticks
     * @return true if the attack can be used
     */
    protected boolean isAttackReady(String attackName, long cooldownTicks) {
        Long lastUsed = attackCooldowns.get(attackName);
        if (lastUsed == null) return true;
        return (combatTicks - lastUsed) >= cooldownTicks;
    }

    /**
     * Marks an attack as used (starts its cooldown).
     *
     * @param attackName the attack identifier
     */
    protected void useAttack(String attackName) {
        attackCooldowns.put(attackName, combatTicks);
    }

    /**
     * Returns the player who dealt the most damage (for primary loot target).
     *
     * @return the top damage contributor, or null
     */
    protected Player getTopDamageContributor() {
        UUID topUUID = null;
        double topDamage = 0;
        for (Map.Entry<UUID, Double> entry : damageContributors.entrySet()) {
            if (entry.getValue() > topDamage) {
                topDamage = entry.getValue();
                topUUID = entry.getKey();
            }
        }
        if (topUUID == null) return null;
        return plugin.getServer().getPlayer(topUUID);
    }

    /**
     * Sets the boss arena center and radius.
     *
     * @param center arena center location
     * @param radius arena radius in blocks
     */
    public void setArena(Location center, double radius) {
        this.arenaCenter = center.clone();
        this.arenaRadius = radius;
    }

    /**
     * Sets the phase thresholds for this boss.
     *
     * @param thresholds HP percentage thresholds in descending order
     */
    public void setPhaseThresholds(double... thresholds) {
        this.phaseThresholds = thresholds;
    }
}

package com.jglims.plugin.abyss;

import java.util.Optional;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Zombie;
import org.bukkit.scheduler.BukkitRunnable;

import com.jglims.plugin.JGlimsPlugin;

import kr.toxicity.model.api.BetterModel;
import kr.toxicity.model.api.animation.AnimationIterator;
import kr.toxicity.model.api.animation.AnimationModifier;
import kr.toxicity.model.api.bukkit.platform.BukkitAdapter;
import kr.toxicity.model.api.data.renderer.ModelRenderer;
import kr.toxicity.model.api.tracker.EntityTracker;
import net.kyori.adventure.text.Component;

/**
 * AbyssDragonModel — BetterModel-powered 3D dragon for the Abyss boss fight.
 *
 * <p>Manages an invisible Zombie hitbox entity with a BetterModel
 * {@link EntityTracker} that renders the {@code abyss_dragon.bbmodel} model.
 * All visual animations (idle, fly, attack, fire_breath, spawn, death, etc.)
 * are driven by BetterModel's animation system.
 *
 * <p>Expected bbmodel file: {@code plugins/BetterModel/models/abyss_dragon.bbmodel}
 * <br>Expected animations: idle, fly, attack, fire_breath, walk, spawn, death,
 * special_1, special_2
 *
 * <p>Called by {@link AbyssDragonBoss} to trigger attacks and phase transitions
 * during the boss fight.
 */
public class AbyssDragonModel {

    // ── Constants ────────────────────────────────────────────────────────

    /** Must match the .bbmodel filename (without extension) in BetterModel's models folder. */
    private static final String MODEL_NAME = "abyss_dragon";

    /** Base hitbox scale applied via the SCALE attribute. */
    private static final double BASE_SCALE = 3.0;

    /** Dragon max HP — must match AbyssDragonBoss.DRAGON_HP. */
    private static final double DRAGON_HP = 1500.0;

    // ── State ────────────────────────────────────────────────────────────

    private final JGlimsPlugin plugin;
    private final Logger logger;

    /** Invisible Zombie used for collision, HP bar, and as BetterModel's host entity. */
    private Zombie hitboxEntity;

    /** BetterModel tracker rendering the 3D model on top of the hitbox entity. */
    private EntityTracker tracker;

    private boolean alive = false;
    private int currentPhase = 1;

    /** Background task that enforces invisibility and ambient particles. */
    private int ambientTaskId = -1;

    // ── Constructor ──────────────────────────────────────────────────────

    public AbyssDragonModel(JGlimsPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    // ── Spawn ────────────────────────────────────────────────────────────

    /**
     * Spawns the dragon at the given location.
     *
     * <p>Creates an invisible Zombie hitbox, sets its HP to {@link #DRAGON_HP}
     * (or {@code requestedHp} if provided), attaches the BetterModel tracker,
     * and plays the spawn-then-idle animation sequence.
     *
     * @param location  spawn location (world must not be null)
     * @param requestedHp desired max HP (capped at 2048)
     * @return true if spawn succeeded
     */
    public boolean spawn(Location location, double requestedHp) {
        logger.info("[DragonModel] Spawning at " + formatLoc(location) + " HP=" + requestedHp);
        try {
            World world = location.getWorld();
            if (world == null) {
                logger.severe("[DragonModel] World is null!");
                return false;
            }

            double hp = Math.min(requestedHp, 2048);

            // ── Create invisible zombie hitbox ──
            hitboxEntity = (Zombie) world.spawnEntity(location, EntityType.ZOMBIE);
            hitboxEntity.setInvisible(true);
            hitboxEntity.setSilent(true);
            hitboxEntity.setAI(false);
            hitboxEntity.setGravity(false);
            hitboxEntity.setPersistent(true);
            hitboxEntity.setRemoveWhenFarAway(false);
            hitboxEntity.customName(Component.text("Abyss Dragon"));
            hitboxEntity.setCustomNameVisible(false);

            // Remove default zombie equipment
            if (hitboxEntity.getEquipment() != null) {
                hitboxEntity.getEquipment().clear();
            }

            // Set health
            try {
                hitboxEntity.getAttribute(Attribute.MAX_HEALTH).setBaseValue(hp);
                hitboxEntity.setHealth(hp);
            } catch (Exception e) {
                logger.warning("[DragonModel] Health setup error: " + e.getMessage());
            }

            // Set scale
            try {
                var scaleAttr = hitboxEntity.getAttribute(Attribute.SCALE);
                if (scaleAttr != null) scaleAttr.setBaseValue(BASE_SCALE);
            } catch (Exception ignored) {}

            // ── Attach BetterModel ──
            attachModel();

            // ── Spawn effects ──
            world.playSound(location, Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0f, 0.5f);
            world.playSound(location, Sound.ENTITY_WITHER_SPAWN, 1.5f, 0.7f);
            world.spawnParticle(Particle.SOUL_FIRE_FLAME, location, 200, 3, 3, 3, 0.1);
            world.spawnParticle(Particle.PORTAL, location, 300, 4, 4, 4, 1.0);
            world.strikeLightningEffect(location);

            alive = true;
            startAmbientTask();

            // ── Spawn animation: play "spawn" then transition to "idle" ──
            playAnimation("spawn", AnimationIterator.Type.PLAY_ONCE, () ->
                    playAnimation("idle", AnimationIterator.Type.LOOP));

            logger.info("[DragonModel] Spawn complete. Tracker=" + (tracker != null));
            return true;

        } catch (Exception e) {
            logger.severe("[DragonModel] SPAWN FAILED: " + e.getMessage());
            e.printStackTrace();
            cleanup();
            return false;
        }
    }

    /**
     * Overload that uses default HP.
     */
    public boolean spawn(Location location) {
        return spawn(location, DRAGON_HP);
    }

    // ── BetterModel attachment ───────────────────────────────────────────

    /**
     * Attaches the BetterModel renderer to the hitbox entity.
     * Does nothing (with a warning) if BetterModel is not loaded or the model
     * is missing from the registry.
     */
    private void attachModel() {
        if (!Bukkit.getPluginManager().isPluginEnabled("BetterModel")) {
            logger.warning("[DragonModel] BetterModel not available — dragon will be invisible.");
            return;
        }
        try {
            Optional<ModelRenderer> renderer = BetterModel.model(MODEL_NAME);
            if (renderer.isEmpty()) {
                logger.warning("[DragonModel] Model '" + MODEL_NAME
                        + "' not found! Ensure plugins/BetterModel/models/" + MODEL_NAME
                        + ".bbmodel exists and run /bettermodel reload");
                return;
            }
            tracker = renderer.get().getOrCreate(BukkitAdapter.adapt(hitboxEntity));
            if (tracker != null) {
                logger.info("[DragonModel] BetterModel EntityTracker attached for " + MODEL_NAME);
            } else {
                logger.warning("[DragonModel] getOrCreate returned null for " + MODEL_NAME);
            }
        } catch (Exception e) {
            logger.warning("[DragonModel] Failed to attach BetterModel: " + e.getMessage());
        }
    }

    // ── Animation ────────────────────────────────────────────────────────

    /**
     * Plays a named BetterModel animation.
     *
     * @param name animation name (must exist in the .bbmodel)
     * @param type loop behavior
     */
    public void playAnimation(String name, AnimationIterator.Type type) {
        if (tracker == null) return;
        try {
            tracker.animate(name, AnimationModifier.builder()
                    .type(type)
                    .start(5).end(5)
                    .build());
        } catch (Exception e) {
            logger.fine("[DragonModel] Animation '" + name + "' not available: " + e.getMessage());
        }
    }

    /**
     * Plays a named BetterModel animation with a completion callback.
     * If the tracker is null (BetterModel absent), the callback fires after
     * a 2-second fallback delay so the boss logic still progresses.
     *
     * @param name       animation name
     * @param type       loop behavior
     * @param callback   called when the animation finishes
     */
    public void playAnimation(String name, AnimationIterator.Type type, Runnable callback) {
        if (tracker == null) {
            // Fallback: run callback after delay so boss logic doesn't stall
            if (callback != null) {
                new BukkitRunnable() {
                    @Override
                    public void run() { callback.run(); }
                }.runTaskLater(plugin, 40L);
            }
            return;
        }
        try {
            tracker.animate(name, AnimationModifier.builder()
                    .type(type)
                    .override(true)
                    .start(5).end(5)
                    .build(), callback);
        } catch (Exception e) {
            logger.fine("[DragonModel] Animation '" + name + "' not available: " + e.getMessage());
            if (callback != null) {
                new BukkitRunnable() {
                    @Override
                    public void run() { callback.run(); }
                }.runTaskLater(plugin, 40L);
            }
        }
    }

    /**
     * Triggers a named attack animation (PLAY_ONCE).
     * Recognized names: attack, fire_breath, special_1, special_2
     *
     * @param attackName the attack animation name
     */
    public void triggerAttack(String attackName) {
        playAnimation(attackName, AnimationIterator.Type.PLAY_ONCE);
    }

    /**
     * Triggers a named attack animation with a completion callback.
     *
     * @param attackName the attack animation name
     * @param onComplete called when the animation finishes
     */
    public void triggerAttack(String attackName, Runnable onComplete) {
        playAnimation(attackName, AnimationIterator.Type.PLAY_ONCE, onComplete);
    }

    // ── Damage tint ──────────────────────────────────────────────────────

    /**
     * Flashes the model red to indicate damage.
     */
    public void damageTint() {
        if (tracker != null) {
            try {
                tracker.damageTint();
            } catch (Exception e) {
                logger.fine("[DragonModel] damageTint failed: " + e.getMessage());
            }
        }
    }

    /**
     * Visual damage feedback — red tint plus damage indicator particles.
     * Called by AbyssDragonBoss on each hit.
     */
    public void damageFlash() {
        damageTint();
        if (hitboxEntity != null && hitboxEntity.getWorld() != null) {
            hitboxEntity.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR,
                    hitboxEntity.getLocation().add(0, 2, 0), 10, 1, 1, 1, 0.1);
        }
    }

    /**
     * Alias for {@link #damageFlash()}.
     */
    public void damageFeedback() {
        damageFlash();
    }

    // ── Movement ─────────────────────────────────────────────────────────

    /**
     * Teleports the hitbox entity (and attached model) to the given location.
     *
     * @param location destination
     */
    public void moveTo(Location location) {
        if (hitboxEntity != null && !hitboxEntity.isDead()) {
            hitboxEntity.teleport(location);
        }
    }

    /**
     * Moves the dragon toward a target at the given speed (blocks per call).
     * Called each tick by AbyssDragonBoss's movement task.
     *
     * @param target destination
     * @param speed  blocks per call
     */
    public void moveToward(Location target, double speed) {
        if (hitboxEntity == null || hitboxEntity.isDead()) return;
        Location current = hitboxEntity.getLocation();
        var dir = target.toVector().subtract(current.toVector());
        if (dir.lengthSquared() < 1) return;
        var move = dir.normalize().multiply(speed);
        Location next = current.add(move);
        next.setYaw((float) Math.toDegrees(Math.atan2(-move.getX(), move.getZ())));
        hitboxEntity.teleport(next);
    }

    /**
     * Alias used by AbyssDragonBoss — moves toward target at default speed.
     *
     * @param target destination
     */
    public void setTarget(Location target) {
        if (hitboxEntity != null && hitboxEntity.isValid()) {
            moveToward(target, 0.3);
        }
    }

    // ── Attack visuals (called by AbyssDragonBoss) ───────────────────────

    /**
     * Plays the fire_breath animation and spawns a particle beam toward the target.
     *
     * @param target the breath target location
     */
    public void breathAttackVisual(Location target) {
        playAnimation("fire_breath", AnimationIterator.Type.PLAY_ONCE);

        // Particle beam effect
        if (hitboxEntity == null || target == null) return;
        Location mouth = hitboxEntity.getLocation().add(0, 3, 0);
        World w = mouth.getWorld();
        if (w == null) return;

        var beamDir = target.toVector().subtract(mouth.toVector()).normalize();
        double beamLength = Math.min(mouth.distance(target), 30);
        for (double d = 0; d < beamLength; d += 0.5) {
            Location p = mouth.clone().add(beamDir.clone().multiply(d));
            double spread = d * 0.06;
            w.spawnParticle(Particle.SOUL_FIRE_FLAME, p, 2, spread, spread, spread, 0.02);
        }
        w.playSound(mouth, Sound.ENTITY_ENDER_DRAGON_SHOOT, 1.5f, 0.5f);
    }

    /**
     * Plays the attack animation with a wing-gust visual.
     */
    public void wingGustVisual() {
        if (hitboxEntity == null) return;
        playAnimation("attack", AnimationIterator.Type.PLAY_ONCE);
        Location loc = hitboxEntity.getLocation().add(0, 2, 0);
        World w = loc.getWorld();
        if (w != null) {
            w.playSound(loc, Sound.ENTITY_ENDER_DRAGON_FLAP, 2.0f, 0.8f);
            w.spawnParticle(Particle.CLOUD, loc, 80, 4, 2, 4, 0.1);
        }
    }

    /**
     * Plays the walk animation (looping).
     *
     * @param target unused — kept for backward compatibility
     */
    public void playWalkAnimation(Location target) {
        playAnimation("walk", AnimationIterator.Type.LOOP);
    }

    /**
     * Plays the fly animation (looping).
     *
     * @param center unused — kept for backward compatibility
     */
    public void playFlyAnimation(Location center) {
        playAnimation("fly", AnimationIterator.Type.LOOP);
    }

    // ── Death sequence ───────────────────────────────────────────────────

    /**
     * Plays the death animation with HOLD_ON_LAST, then runs the callback
     * and cleans up. A 5-second safety timeout ensures cleanup even if the
     * animation callback never fires.
     *
     * @param onComplete called after the death animation finishes
     */
    public void deathSequence(Runnable onComplete) {
        alive = false;

        // Death particles and sound
        if (hitboxEntity != null && hitboxEntity.getWorld() != null) {
            Location loc = hitboxEntity.getLocation();
            World w = loc.getWorld();
            w.spawnParticle(Particle.SOUL_FIRE_FLAME, loc, 100, 3, 3, 3, 0.1);
            w.spawnParticle(Particle.PORTAL, loc, 200, 4, 4, 4, 1.0);
            w.playSound(loc, Sound.ENTITY_ENDER_DRAGON_DEATH, 2.0f, 0.7f);
            w.strikeLightningEffect(loc);
        }

        playAnimation("death", AnimationIterator.Type.HOLD_ON_LAST, () -> {
            if (onComplete != null) onComplete.run();
            cleanup();
        });

        // Safety timeout — force cleanup after 5 seconds if callback never fires
        new BukkitRunnable() {
            @Override
            public void run() {
                if (hitboxEntity != null) {
                    if (onComplete != null && alive) onComplete.run();
                    cleanup();
                }
            }
        }.runTaskLater(plugin, 100L);
    }

    /**
     * Plays death animation without callback. Called by AbyssDragonBoss.
     */
    public void playDeathAnimation() {
        deathSequence(null);
    }

    /**
     * Plays death animation with callback.
     *
     * @param onComplete called after death animation
     */
    public void playDeathAnimation(Runnable onComplete) {
        deathSequence(onComplete);
    }

    // ── Phase transitions ────────────────────────────────────────────────

    /**
     * Updates the dragon's phase. Plays dramatic effects and scales the hitbox.
     *
     * @param phase new phase number (1-4)
     */
    public void setPhase(int phase) {
        int old = this.currentPhase;
        this.currentPhase = phase;
        if (hitboxEntity == null) return;

        Location loc = hitboxEntity.getLocation();
        World world = loc.getWorld();
        if (world == null) return;

        world.playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 2.5f, 0.3f + (phase * 0.1f));
        world.spawnParticle(Particle.PORTAL, loc, 200, 3, 3, 3, 1.0);
        world.spawnParticle(Particle.SOUL_FIRE_FLAME, loc, 50, 2, 2, 2, 0.1);
        if (phase >= 3) {
            world.playSound(loc, Sound.ENTITY_WITHER_SPAWN, 1.0f, 1.2f);
        }

        // Scale up each phase
        try {
            var scaleAttr = hitboxEntity.getAttribute(Attribute.SCALE);
            if (scaleAttr != null) {
                scaleAttr.setBaseValue(BASE_SCALE + (phase - 1) * 0.5);
            }
        } catch (Exception ignored) {}

        logger.info("[DragonModel] Phase " + old + " -> " + phase);
    }

    /**
     * Alias for {@link #setPhase(int)}.
     */
    public void phaseTransition(int phase) {
        setPhase(phase);
    }

    // ── Ambient task ─────────────────────────────────────────────────────

    /**
     * Background task: enforces invisibility and spawns ambient particles
     * that intensify with each phase.
     */
    private void startAmbientTask() {
        ambientTaskId = new BukkitRunnable() {
            @Override
            public void run() {
                if (!alive || hitboxEntity == null || hitboxEntity.isDead()) {
                    cancel();
                    return;
                }
                // Enforce invisibility (riders/interactions can break it)
                hitboxEntity.setInvisible(true);

                // Ambient particles
                Location loc = hitboxEntity.getLocation().add(0, 2, 0);
                World w = loc.getWorld();
                if (w == null) return;
                w.spawnParticle(Particle.SOUL_FIRE_FLAME, loc, 3, 1.5, 1.5, 1.5, 0.02);
                if (currentPhase >= 2) w.spawnParticle(Particle.PORTAL, loc, 5, 2, 2, 2, 0.5);
                if (currentPhase >= 3) w.spawnParticle(Particle.REVERSE_PORTAL, loc, 8, 3, 3, 3, 0.3);
            }
        }.runTaskTimer(plugin, 2L, 4L).getTaskId();
    }

    // ── Cleanup / Despawn ────────────────────────────────────────────────

    /**
     * Closes the BetterModel tracker, cancels tasks, and removes the hitbox entity.
     */
    public void cleanup() {
        alive = false;

        if (ambientTaskId != -1) {
            Bukkit.getScheduler().cancelTask(ambientTaskId);
            ambientTaskId = -1;
        }

        if (tracker != null) {
            try { tracker.close(); } catch (Exception ignored) {}
            tracker = null;
        }

        if (hitboxEntity != null && !hitboxEntity.isDead()) {
            hitboxEntity.remove();
        }
        hitboxEntity = null;

        logger.info("[DragonModel] Cleanup complete.");
    }

    /**
     * Alias for {@link #cleanup()}.
     */
    public void despawn() {
        cleanup();
    }

    // ── Getters ──────────────────────────────────────────────────────────

    /** Returns the invisible Zombie hitbox entity, or null if despawned. */
    public Zombie getEntity() {
        return hitboxEntity;
    }

    /** Alias — used by AbyssDragonBoss. */
    public Zombie getHitboxEntity() {
        return hitboxEntity;
    }

    /** Alias — used by AbyssDragonBoss. */
    public Zombie getBaseEntity() {
        return hitboxEntity;
    }

    /** Returns the BetterModel EntityTracker, or null if BetterModel is absent. */
    public EntityTracker getTracker() {
        return tracker;
    }

    /** Returns true if the dragon is alive and the hitbox entity exists. */
    public boolean isAlive() {
        return alive && hitboxEntity != null && !hitboxEntity.isDead();
    }

    /** Returns the current location, or null if despawned. */
    public Location getLocation() {
        return hitboxEntity != null ? hitboxEntity.getLocation() : null;
    }

    /** Returns current HP, or 0 if dead/despawned. */
    public double getHealth() {
        return (hitboxEntity != null && !hitboxEntity.isDead()) ? hitboxEntity.getHealth() : 0;
    }

    /** Returns the current boss-fight phase (1-4). */
    public int getPhase() {
        return currentPhase;
    }

    // ── Utility ──────────────────────────────────────────────────────────

    private String formatLoc(Location l) {
        return String.format("(%.1f, %.1f, %.1f in %s)",
                l.getX(), l.getY(), l.getZ(),
                l.getWorld() != null ? l.getWorld().getName() : "null");
    }
}

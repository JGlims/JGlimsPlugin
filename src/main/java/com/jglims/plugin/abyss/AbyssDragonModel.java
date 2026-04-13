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
import org.bukkit.entity.Giant;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.jglims.plugin.JGlimsPlugin;

import kr.toxicity.model.api.BetterModel;
import kr.toxicity.model.api.animation.AnimationIterator;
import kr.toxicity.model.api.animation.AnimationModifier;
import kr.toxicity.model.api.bukkit.platform.BukkitAdapter;
import kr.toxicity.model.api.data.renderer.ModelRenderer;
import kr.toxicity.model.api.tracker.EntityTracker;
import kr.toxicity.model.api.tracker.TrackerModifier;
import kr.toxicity.model.api.tracker.TrackerUpdateAction;
import net.kyori.adventure.text.Component;

/**
 * AbyssDragonModel — BetterModel-powered 3D dragon for the Abyss boss fight.
 *
 * <p>Manages an invisible Zombie hitbox entity with a BetterModel
 * {@link EntityTracker} that renders the {@code abyss_dragon.bbmodel} model.
 * All visual animations (idle, fly, attack, fire_breath, walk, spawn, death,
 * special_1, special_2) are driven by BetterModel's animation system using the
 * keyframes auto-generated for the .bbmodel file.
 *
 * <p>Expected file: {@code plugins/BetterModel/models/abyss_dragon.bbmodel}
 *
 * <p>Phase visuals (glow, glow color, tint, brightness, scale) are applied via
 * {@link TrackerUpdateAction}. The model gracefully degrades to an invisible
 * Zombie hitbox with particle ambiance when BetterModel is not loaded.
 */
public class AbyssDragonModel {

    // ── Constants ────────────────────────────────────────────────────────

    /** Must match the .bbmodel filename (without extension) in BetterModel's models folder. */
    private static final String MODEL_NAME = "abyss_dragon";

    /**
     * Base hitbox scale applied via the SCALE attribute.
     * Was 3.0, reduced to 2.0 so the dragon actually fits inside the 55-block
     * Abyssal arena without clipping through the walls or ceiling.
     */
    private static final double BASE_SCALE = 2.0;

    /** Dragon max HP — must match AbyssDragonBoss.DRAGON_HP. */
    private static final double DRAGON_HP = 1500.0;

    /** Hard cap (Paper attribute.maxHealth.max default ceiling). */
    private static final double HP_CAP = 2048.0;

    // ── Pre-built AnimationModifier constants ────────────────────────────

    /** Looping idle animation, 10-tick blends. */
    public static final AnimationModifier IDLE_MOD = AnimationModifier.builder()
            .type(AnimationIterator.Type.LOOP)
            .start(10).end(10)
            .build();

    /** Looping flight animation, 8-tick blends. */
    public static final AnimationModifier FLY_MOD = AnimationModifier.builder()
            .type(AnimationIterator.Type.LOOP)
            .start(8).end(8)
            .build();

    /** Looping walk animation, 5-tick blends. */
    public static final AnimationModifier WALK_MOD = AnimationModifier.builder()
            .type(AnimationIterator.Type.LOOP)
            .start(5).end(5)
            .build();

    /** Single-shot melee attack, override active animation. */
    public static final AnimationModifier ATTACK_MOD = AnimationModifier.builder()
            .type(AnimationIterator.Type.PLAY_ONCE)
            .start(3).end(5)
            .override(Boolean.TRUE)
            .build();

    /** Single-shot fire breath attack with longer recovery. */
    public static final AnimationModifier FIRE_BREATH_MOD = AnimationModifier.builder()
            .type(AnimationIterator.Type.PLAY_ONCE)
            .start(5).end(8)
            .override(Boolean.TRUE)
            .build();

    /** One-shot spawn animation, plays from frame 0. */
    public static final AnimationModifier SPAWN_MOD = AnimationModifier.builder()
            .type(AnimationIterator.Type.PLAY_ONCE)
            .start(0).end(10)
            .override(Boolean.TRUE)
            .build();

    /** Death animation — holds on the last frame. */
    public static final AnimationModifier DEATH_MOD = AnimationModifier.builder()
            .type(AnimationIterator.Type.HOLD_ON_LAST)
            .start(5).end(0)
            .override(Boolean.TRUE)
            .build();

    /** Generic special-ability animation (special_1, special_2, etc.). */
    public static final AnimationModifier SPECIAL_MOD = AnimationModifier.builder()
            .type(AnimationIterator.Type.PLAY_ONCE)
            .start(3).end(5)
            .override(Boolean.TRUE)
            .build();

    // ── State ────────────────────────────────────────────────────────────

    private final JGlimsPlugin plugin;
    private final Logger logger;

    /** Invisible Zombie used for collision, HP bar, and as BetterModel's host entity. */
    // Was Zombie — switched to Giant so the invisible base entity has a
    // 12-block-tall x 1.8-wide hitbox. With BASE_SCALE 2.0 that's 24 x 3.6,
    // which matches the dragon's visual size so players can actually land hits.
    private Giant hitboxEntity;

    /** BetterModel tracker rendering the 3D model on top of the hitbox entity. */
    private EntityTracker tracker;

    private boolean alive = false;
    private int currentPhase = 1;
    private String currentAnimation = null;

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
     * @param location  spawn location (world must not be null)
     * @param requestedHp desired max HP (capped at {@link #HP_CAP})
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

            double hp = Math.min(requestedHp, HP_CAP);

            // ── Create invisible zombie hitbox ──
            hitboxEntity = (Giant) world.spawnEntity(location, EntityType.GIANT);
            hitboxEntity.setInvisible(true);
            hitboxEntity.setSilent(true);
            hitboxEntity.setAI(false);
            hitboxEntity.setGravity(false);
            hitboxEntity.setPersistent(true);
            hitboxEntity.setRemoveWhenFarAway(false);
            hitboxEntity.customName(Component.text("Abyss Dragon"));
            hitboxEntity.setCustomNameVisible(false);

            if (hitboxEntity.getEquipment() != null) {
                hitboxEntity.getEquipment().clear();
            }

            try {
                hitboxEntity.getAttribute(Attribute.MAX_HEALTH).setBaseValue(hp);
                hitboxEntity.setHealth(hp);
            } catch (Exception e) {
                logger.warning("[DragonModel] Health setup error: " + e.getMessage());
            }

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

            // ── Spawn animation: spawn → idle ──
            playAnimation("spawn", SPAWN_MOD, () -> playAnimation("idle", IDLE_MOD));

            logger.info("[DragonModel] Spawn complete. Tracker=" + (tracker != null));
            return true;

        } catch (Exception e) {
            logger.severe("[DragonModel] SPAWN FAILED: " + e.getMessage());
            e.printStackTrace();
            cleanup();
            return false;
        }
    }

    /** Spawn at default HP. */
    public boolean spawn(Location location) {
        return spawn(location, DRAGON_HP);
    }

    // ── BetterModel attachment ───────────────────────────────────────────

    /**
     * Attaches the BetterModel renderer to the hitbox entity using a custom
     * {@link TrackerModifier}: sightTrace=false, damageAnimation=true, damageTint=true.
     */
    private void attachModel() {
        if (!Bukkit.getPluginManager().isPluginEnabled("BetterModel")) {
            logger.warning("[DragonModel] BetterModel not available — dragon will be invisible.");
            return;
        }
        try {
            Optional<ModelRenderer> rendererOpt = BetterModel.model(MODEL_NAME);
            if (rendererOpt.isEmpty()) {
                logger.warning("[DragonModel] Model '" + MODEL_NAME
                        + "' not found! Ensure plugins/BetterModel/models/" + MODEL_NAME
                        + ".bbmodel exists and run /bettermodel reload");
                return;
            }
            ModelRenderer renderer = rendererOpt.get();
            TrackerModifier modifier = new TrackerModifier(false, true, true);
            tracker = renderer.create(BukkitAdapter.adapt(hitboxEntity), modifier, t -> {
                logger.info("[DragonModel] Tracker initializer fired");
            });
            if (tracker != null) {
                logger.info("[DragonModel] BetterModel EntityTracker attached for " + MODEL_NAME);
            } else {
                logger.warning("[DragonModel] create returned null for " + MODEL_NAME);
            }
        } catch (Exception e) {
            logger.warning("[DragonModel] Failed to attach BetterModel: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── Animation ────────────────────────────────────────────────────────

    /**
     * Plays a named BetterModel animation with the given modifier.
     *
     * @param name      animation name
     * @param modifier  pre-built {@link AnimationModifier}
     */
    public void playAnimation(String name, AnimationModifier modifier) {
        if (tracker == null) return;
        try {
            tracker.animate(name, modifier);
            currentAnimation = name;
        } catch (Exception e) {
            logger.fine("[DragonModel] Animation '" + name + "' not available: " + e.getMessage());
        }
    }

    /**
     * Plays a named animation with a completion callback. If the tracker is
     * absent, the callback fires after a 2-second fallback delay.
     */
    public void playAnimation(String name, AnimationModifier modifier, Runnable callback) {
        if (tracker == null) {
            if (callback != null) {
                new BukkitRunnable() {
                    @Override public void run() { callback.run(); }
                }.runTaskLater(plugin, 40L);
            }
            return;
        }
        try {
            tracker.animate(name, modifier, callback);
            currentAnimation = name;
        } catch (Exception e) {
            logger.fine("[DragonModel] Animation '" + name + "' not available: " + e.getMessage());
            if (callback != null) {
                new BukkitRunnable() {
                    @Override public void run() { callback.run(); }
                }.runTaskLater(plugin, 40L);
            }
        }
    }

    /**
     * Triggers a named one-shot attack animation using {@link #ATTACK_MOD}.
     */
    public void triggerAttack(String attackName) {
        playAnimation(attackName, ATTACK_MOD);
    }

    /** Triggers a named attack animation with a completion callback. */
    public void triggerAttack(String attackName, Runnable onComplete) {
        playAnimation(attackName, ATTACK_MOD, onComplete);
    }

    // ── Damage tint ──────────────────────────────────────────────────────

    /**
     * Flashes the model red to indicate damage (uses BetterModel's built-in damage tint).
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
     * Visual damage feedback — red tint plus DAMAGE_INDICATOR particles.
     * Called by {@link AbyssDragonBoss} on each hit.
     */
    public void damageFlash() {
        damageTint();
        if (hitboxEntity != null && hitboxEntity.getWorld() != null) {
            hitboxEntity.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR,
                    hitboxEntity.getLocation().add(0, 2, 0), 10, 1, 1, 1, 0.1);
        }
    }

    /** Alias for {@link #damageFlash()}. */
    public void damageFeedback() {
        damageFlash();
    }

    // ── Movement ─────────────────────────────────────────────────────────

    /** Teleports the hitbox entity (and attached model) to the given location. */
    public void moveTo(Location location) {
        if (hitboxEntity != null && !hitboxEntity.isDead()) {
            hitboxEntity.teleport(location);
        }
    }

    /**
     * Moves the dragon toward a target at the given speed (blocks per call).
     * Called each tick by AbyssDragonBoss's movement task.
     */
    public void moveToward(Location target, double speed) {
        if (hitboxEntity == null || hitboxEntity.isDead()) return;
        Location current = hitboxEntity.getLocation();
        Vector dir = target.toVector().subtract(current.toVector());
        if (dir.lengthSquared() < 1) return;
        Vector move = dir.normalize().multiply(speed);
        Location next = current.add(move);
        next.setYaw((float) Math.toDegrees(Math.atan2(-move.getX(), move.getZ())));
        hitboxEntity.teleport(next);
    }

    /** Alias used by AbyssDragonBoss — moves toward target at default speed. */
    public void setTarget(Location target) {
        if (hitboxEntity != null && hitboxEntity.isValid()) {
            moveToward(target, 0.3);
        }
    }

    // ── Attack visuals (called by AbyssDragonBoss) ───────────────────────

    /**
     * Plays the fire_breath animation and spawns a particle beam toward the target.
     */
    public void breathAttackVisual(Location target) {
        playAnimation("fire_breath", FIRE_BREATH_MOD);

        if (hitboxEntity == null || target == null) return;
        Location mouth = hitboxEntity.getLocation().add(0, 3, 0);
        World w = mouth.getWorld();
        if (w == null) return;

        Vector beamDir = target.toVector().subtract(mouth.toVector()).normalize();
        double beamLength = Math.min(mouth.distance(target), 30);
        for (double d = 0; d < beamLength; d += 0.5) {
            Location p = mouth.clone().add(beamDir.clone().multiply(d));
            double spread = d * 0.06;
            w.spawnParticle(Particle.SOUL_FIRE_FLAME, p, 2, spread, spread, spread, 0.02);
        }
        w.playSound(mouth, Sound.ENTITY_ENDER_DRAGON_SHOOT, 1.5f, 0.5f);
    }

    /** Plays the attack animation with a wing-gust visual (CLOUD particles + sound). */
    public void wingGustVisual() {
        if (hitboxEntity == null) return;
        playAnimation("attack", ATTACK_MOD);
        Location loc = hitboxEntity.getLocation().add(0, 2, 0);
        World w = loc.getWorld();
        if (w != null) {
            w.playSound(loc, Sound.ENTITY_ENDER_DRAGON_FLAP, 2.0f, 0.8f);
            w.spawnParticle(Particle.CLOUD, loc, 80, 4, 2, 4, 0.1);
        }
    }

    /** Plays the walk animation (looping). */
    public void playWalkAnimation(Location target) {
        playAnimation("walk", WALK_MOD);
    }

    /** Plays the fly animation (looping). */
    public void playFlyAnimation(Location center) {
        playAnimation("fly", FLY_MOD);
    }

    // ── Death sequence ───────────────────────────────────────────────────

    /**
     * Plays the death animation with HOLD_ON_LAST, then runs the callback
     * and cleans up. A 5-second safety timeout ensures cleanup even if the
     * animation callback never fires.
     */
    public void deathSequence(Runnable onComplete) {
        alive = false;

        if (hitboxEntity != null && hitboxEntity.getWorld() != null) {
            Location loc = hitboxEntity.getLocation();
            World w = loc.getWorld();
            w.spawnParticle(Particle.SOUL_FIRE_FLAME, loc, 100, 3, 3, 3, 0.1);
            w.spawnParticle(Particle.PORTAL, loc, 200, 4, 4, 4, 1.0);
            w.playSound(loc, Sound.ENTITY_ENDER_DRAGON_DEATH, 2.0f, 0.7f);
            w.strikeLightningEffect(loc);
        }

        playAnimation("death", DEATH_MOD, () -> {
            if (onComplete != null) onComplete.run();
            cleanup();
        });

        // Safety timeout — force cleanup after 5 seconds if callback never fires
        new BukkitRunnable() {
            @Override
            public void run() {
                if (hitboxEntity != null) {
                    if (onComplete != null) onComplete.run();
                    cleanup();
                }
            }
        }.runTaskLater(plugin, 100L);
    }

    /** Plays death animation without callback. Called by AbyssDragonBoss. */
    public void playDeathAnimation() {
        deathSequence(null);
    }

    /** Plays death animation with callback. */
    public void playDeathAnimation(Runnable onComplete) {
        deathSequence(onComplete);
    }

    // ── Phase transitions ────────────────────────────────────────────────

    /**
     * Updates the dragon's phase. Plays dramatic effects, scales the hitbox,
     * and adjusts the model's glow color / brightness via TrackerUpdateAction.
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

        // Visual effects via TrackerUpdateAction
        if (tracker != null) {
            try {
                int color;
                switch (phase) {
                    case 2: color = 0x6600AA; break;
                    case 3: color = 0xAA0066; break;
                    case 4: color = 0xFF0033; break;
                    default: color = 0x8800AA;
                }
                tracker.update(TrackerUpdateAction.glow(true));
                tracker.update(TrackerUpdateAction.glowColor(color));
                if (phase >= 3) {
                    tracker.update(TrackerUpdateAction.brightness(15, 15));
                }
            } catch (Exception e) {
                logger.fine("[DragonModel] Phase visuals failed: " + e.getMessage());
            }
        }

        logger.info("[DragonModel] Phase " + old + " -> " + phase);
    }

    /** Alias for {@link #setPhase(int)}. */
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
                hitboxEntity.setInvisible(true);

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

    /** Closes the BetterModel tracker, cancels tasks, removes the hitbox entity. */
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
        currentAnimation = null;

        logger.info("[DragonModel] Cleanup complete.");
    }

    /** Alias for {@link #cleanup()}. */
    public void despawn() {
        cleanup();
    }

    // ── Getters ──────────────────────────────────────────────────────────

    /** Returns the invisible Zombie hitbox entity, or null if despawned. */
    public Giant getEntity() {
        return hitboxEntity;
    }

    /** Alias used by AbyssDragonBoss. */
    public Giant getHitboxEntity() {
        return hitboxEntity;
    }

    /** Alias used by AbyssDragonBoss. */
    public Giant getBaseEntity() {
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

    /** Returns the name of the currently-playing animation, or null if none. */
    public String getCurrentAnimation() {
        return currentAnimation;
    }

    // ── Utility ──────────────────────────────────────────────────────────

    private String formatLoc(Location l) {
        return String.format("(%.1f, %.1f, %.1f in %s)",
                l.getX(), l.getY(), l.getZ(),
                l.getWorld() != null ? l.getWorld().getName() : "null");
    }
}

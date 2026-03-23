package com.jglims.plugin.abyss;

import java.lang.reflect.Method;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Zombie;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.jglims.plugin.JGlimsPlugin;

/**
 * AbyssDragonModel v11.0 — Hybrid BetterModel + Java-driven animation.
 *
 * BetterModel renders the 3D .bbmodel geometry on the hitbox zombie.
 * Java tasks drive idle/spawn/attack/breath/walk/fly motion via entity
 * teleportation and particle effects. If the .bbmodel later gains embedded
 * animations (idle, attack, death, etc.), the tryPlayBetterModelAnimation()
 * calls will pick them up automatically.
 *
 * FIXES from v10.0:
 *   - Removed non-existent import kr.toxicity.model.api.nms.PlatformEntity
 *   - BetterModel attachment uses reflection for full soft-dependency safety
 *   - All API calls wrapped in try/catch → no red lines even if BetterModel
 *     JAR is absent from the compile classpath at IDE refresh time
 */
public class AbyssDragonModel {

    // ===================== CONSTANTS =====================

    private static final String BETTERMODEL_NAME = "Void_Dragon"; // must match .bbmodel filename (no extension)
    private static final float BASE_SCALE = 3.0f;

    // Java-driven animation parameters
    private static final double IDLE_BOB_AMPLITUDE = 0.35;   // blocks up/down
    private static final double IDLE_BOB_SPEED = 0.08;       // radians per tick
    private static final float  IDLE_YAW_SPEED = 0.6f;       // degrees per tick
    private static final double FLY_CIRCLE_RADIUS = 12.0;
    private static final double FLY_CIRCLE_SPEED = 0.03;     // radians per tick
    private static final double FLY_ALTITUDE = 18.0;
    private static final double WALK_STEP = 0.25;            // blocks per tick

    // ===================== STATE =====================

    private final JGlimsPlugin plugin;
    private final Logger logger;
    private Zombie hitboxEntity;

    // BetterModel tracker — stored as Object for soft-dependency safety.
    // Actual type: kr.toxicity.model.api.tracker.EntityTracker
    private Object modelTracker;
    private boolean usingBetterModel = false;

    private int currentPhase = 1;
    private boolean alive = false;

    // Task IDs
    private int invisTaskId = -1;
    private int particleTaskId = -1;
    private int animationTaskId = -1;

    // Java-driven animation state
    private long tickCounter = 0;
    private AnimState animState = AnimState.IDLE;
    private Location anchorPoint;       // center point for idle bob / fly circle
    private int animTimer = 0;          // ticks remaining for timed animations
    private Runnable animCallback;      // called when timed animation ends
    private Location walkTarget;        // for WALK state

    private enum AnimState {
        IDLE, SPAWN, ATTACK, FIRE_BREATH, WALK, FLY, DEATH
    }

    // ===================== CONSTRUCTOR =====================

    public AbyssDragonModel(JGlimsPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    // ===================== SPAWN =====================

    public boolean spawn(Location location, double requestedHp) {
        logger.info("[DragonModel] Spawning at " + formatLoc(location) + " HP=" + requestedHp);
        try {
            World world = location.getWorld();
            if (world == null) { logger.severe("[DragonModel] World is null!"); return false; }
            double hp = Math.min(requestedHp, 2048);

            // --- Spawn invisible zombie hitbox ---
            hitboxEntity = (Zombie) world.spawnEntity(location, EntityType.ZOMBIE);
            hitboxEntity.setInvisible(true);
            hitboxEntity.setSilent(true);
            hitboxEntity.setAI(false);
            hitboxEntity.setGravity(false);
            hitboxEntity.setPersistent(true);
            hitboxEntity.setRemoveWhenFarAway(false);
            hitboxEntity.customName(net.kyori.adventure.text.Component.text("Abyss Dragon"));
            hitboxEntity.setCustomNameVisible(false);

            try {
                hitboxEntity.getAttribute(Attribute.MAX_HEALTH).setBaseValue(hp);
                hitboxEntity.setHealth(hp);
            } catch (Exception e) { logger.warning("[DragonModel] Health error: " + e.getMessage()); }

            try {
                var scaleAttr = hitboxEntity.getAttribute(Attribute.SCALE);
                if (scaleAttr != null) scaleAttr.setBaseValue(BASE_SCALE);
            } catch (Exception ignored) {}

            // --- Try BetterModel attachment ---
            usingBetterModel = tryAttachBetterModel();

            if (usingBetterModel) {
                logger.info("[DragonModel] BetterModel tracker attached successfully!");
                tryPlayBetterModelAnimation("idle", false); // attempt looped idle if bbmodel has it
            } else {
                logger.warning("[DragonModel] BetterModel NOT available — dragon hitbox will be invisible.");
                logger.warning("[DragonModel] Ensure BetterModel 2.1.0+ is installed and " +
                    BETTERMODEL_NAME + ".bbmodel is in plugins/BetterModel/models/");
            }

            // --- Initial effects ---
            anchorPoint = location.clone();
            spawnEffects(location, world);
            alive = true;

            // --- Start background tasks ---
            startInvisibilityEnforcer();
            startParticleTask();
            startAnimationTask();

            // --- Play spawn animation sequence ---
            playSpawnAnimation();

            logger.info("[DragonModel] Spawn complete. BetterModel=" + usingBetterModel);
            return true;
        } catch (Exception e) {
            logger.severe("[DragonModel] SPAWN FAILED: " + e.getMessage());
            e.printStackTrace();
            cleanup();
            return false;
        }
    }

    // ===================== BETTERMODEL INTEGRATION (Reflection) =====================

    /**
     * Attaches BetterModel to the hitbox entity using reflection.
     * This avoids ANY compile-time dependency on BetterModel classes,
     * which means zero red lines in VS Code even if the dependency
     * isn't resolving properly.
     */
    private boolean tryAttachBetterModel() {
        try {
            // Check plugin is loaded
            if (Bukkit.getPluginManager().getPlugin("BetterModel") == null) {
                logger.info("[DragonModel] BetterModel plugin not installed.");
                return false;
            }

            // BukkitAdapter.adapt(Entity) → PlatformEntity (opaque)
            Class<?> adapterClass = Class.forName("kr.toxicity.model.api.bukkit.BukkitAdapter");
            Method adaptMethod = adapterClass.getMethod("adapt", Object.class);
            Object platformEntity = adaptMethod.invoke(null, hitboxEntity);

            // BetterModel.model("Void_Dragon") → Optional<ModelRenderer>
            Class<?> bmClass = Class.forName("kr.toxicity.model.api.BetterModel");
            Method modelMethod = bmClass.getMethod("model", String.class);
            Object optionalRenderer = modelMethod.invoke(null, BETTERMODEL_NAME);

            // Optional.isPresent() / Optional.get()
            Method isPresent = optionalRenderer.getClass().getMethod("isPresent");
            if (!(boolean) isPresent.invoke(optionalRenderer)) {
                logger.warning("[DragonModel] Model '" + BETTERMODEL_NAME + "' not found in BetterModel registry!");
                logger.warning("[DragonModel] Ensure plugins/BetterModel/models/" + BETTERMODEL_NAME + ".bbmodel exists");
                logger.warning("[DragonModel] Run: /bettermodel reload");
                return false;
            }

            Method getMethod = optionalRenderer.getClass().getMethod("get");
            Object renderer = getMethod.invoke(optionalRenderer);

            // renderer.getOrCreate(platformEntity) → EntityTracker
            Method getOrCreate = renderer.getClass().getMethod("getOrCreate", platformEntity.getClass().getInterfaces()[0]);
            modelTracker = getOrCreate.invoke(renderer, platformEntity);

            if (modelTracker == null) {
                // Fallback: try create(platformEntity, TrackerModifier.DEFAULT, Consumer)
                try {
                    Class<?> tmClass = Class.forName("kr.toxicity.model.api.tracker.TrackerModifier");
                    Object defaultMod = tmClass.getField("DEFAULT").get(null);
                    Method createMethod = null;
                    for (Method m : renderer.getClass().getMethods()) {
                        if (m.getName().equals("create") && m.getParameterCount() == 3) {
                            createMethod = m;
                            break;
                        }
                    }
                    if (createMethod != null) {
                        // Use a no-op consumer
                        modelTracker = createMethod.invoke(renderer, platformEntity, defaultMod,
                            (java.util.function.Consumer<Object>) t -> {});
                    }
                } catch (Exception inner) {
                    logger.fine("[DragonModel] create() fallback failed: " + inner.getMessage());
                }
            }

            if (modelTracker != null) {
                logger.info("[DragonModel] BetterModel EntityTracker created for " + BETTERMODEL_NAME);
                return true;
            } else {
                logger.warning("[DragonModel] getOrCreate returned null");
                return false;
            }

        } catch (ClassNotFoundException e) {
            logger.info("[DragonModel] BetterModel API classes not on classpath: " + e.getMessage());
            return false;
        } catch (NoSuchMethodException e) {
            logger.warning("[DragonModel] BetterModel API method not found: " + e.getMessage());
            logger.warning("[DragonModel] Possible version mismatch. Expected bettermodel-bukkit-api 2.1.0+");
            return false;
        } catch (Exception e) {
            logger.warning("[DragonModel] BetterModel attachment failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Attempt to play a named animation from the .bbmodel via BetterModel.
     * If the .bbmodel has no animations, this silently does nothing.
     * @param name animation name (e.g. "idle", "attack", "death")
     * @param playOnce true for play-once, false for looping
     */
    private void tryPlayBetterModelAnimation(String name, boolean playOnce) {
        if (!usingBetterModel || modelTracker == null) return;
        try {
            Class<?> modClass = Class.forName("kr.toxicity.model.api.animation.AnimationModifier");
            Object modifier;
            if (playOnce) {
                modifier = modClass.getField("DEFAULT_WITH_PLAY_ONCE").get(null);
            } else {
                modifier = modClass.getField("DEFAULT").get(null);
            }

            // tracker.animate(String name, AnimationModifier mod, Runnable callback)
            Method animateMethod = null;
            for (Method m : modelTracker.getClass().getMethods()) {
                if (m.getName().equals("animate") && m.getParameterCount() == 3) {
                    Class<?>[] params = m.getParameterTypes();
                    if (params[0] == String.class) {
                        animateMethod = m;
                        break;
                    }
                }
            }

            if (animateMethod != null) {
                Runnable noop = () -> {};
                animateMethod.invoke(modelTracker, name, modifier, noop);
                logger.fine("[DragonModel] BetterModel animate('" + name + "') called.");
            }
        } catch (Exception e) {
            // Silently ignore — the .bbmodel probably doesn't have this animation
            logger.fine("[DragonModel] BetterModel animation '" + name + "' not available: " + e.getMessage());
        }
    }

    // ===================== JAVA-DRIVEN ANIMATION ENGINE =====================

    /**
     * Main animation task — runs every 2 ticks (10 TPS).
     * Drives the dragon's visual movement based on animState.
     */
    private void startAnimationTask() {
        animationTaskId = new BukkitRunnable() {
            @Override
            public void run() {
                if (!alive || hitboxEntity == null || hitboxEntity.isDead()) {
                    cancel();
                    return;
                }
                tickCounter += 2; // 2 ticks per run

                switch (animState) {
                    case IDLE -> tickIdle();
                    case SPAWN -> tickSpawn();
                    case ATTACK -> tickAttack();
                    case FIRE_BREATH -> tickFireBreath();
                    case WALK -> tickWalk();
                    case FLY -> tickFly();
                    case DEATH -> tickDeath();
                }
            }
        }.runTaskTimer(plugin, 2L, 2L).getTaskId();
    }

    // --- IDLE: gentle hover-bob + slow yaw rotation ---

    private void tickIdle() {
        if (anchorPoint == null) return;
        double bobOffset = Math.sin(tickCounter * IDLE_BOB_SPEED) * IDLE_BOB_AMPLITUDE;
        Location loc = anchorPoint.clone().add(0, bobOffset, 0);
        float yaw = (tickCounter * IDLE_YAW_SPEED) % 360;
        loc.setYaw(yaw);
        loc.setPitch(0);
        hitboxEntity.teleport(loc);
    }

    // --- SPAWN: dramatic rise from below + lightning ---

    private void playSpawnAnimation() {
        animState = AnimState.SPAWN;
        animTimer = 60; // 3 seconds (60 ticks at 2-tick interval = 30 iterations)
        tryPlayBetterModelAnimation("spawn", true);

        // Start below anchor, rise up
        if (anchorPoint != null && hitboxEntity != null) {
            Location start = anchorPoint.clone().add(0, -8, 0);
            hitboxEntity.teleport(start);
        }
    }

    private void tickSpawn() {
        animTimer -= 2;
        if (hitboxEntity == null || anchorPoint == null) return;

        // Rise from 8 blocks below to anchor over 60 ticks
        double progress = 1.0 - (animTimer / 60.0);
        progress = Math.min(1.0, Math.max(0.0, progress));
        // Ease-out curve
        double eased = 1.0 - Math.pow(1.0 - progress, 3);
        double yOffset = -8.0 + (8.0 * eased);

        Location loc = anchorPoint.clone().add(0, yOffset, 0);
        float yaw = (float)(progress * 720); // 2 full rotations during rise
        loc.setYaw(yaw);
        hitboxEntity.teleport(loc);

        // Particles during rise
        World w = loc.getWorld();
        if (w != null) {
            safeParticle(w, Particle.PORTAL, loc, 15, 2, 3, 2, 0.5);
            safeParticle(w, Particle.SOUL_FIRE_FLAME, loc, 5, 1.5, 1.5, 1.5, 0.05);
            if (animTimer == 40 || animTimer == 20 || animTimer == 4) {
                w.strikeLightningEffect(loc);
            }
        }

        if (animTimer <= 0) {
            animState = AnimState.IDLE;
            hitboxEntity.teleport(anchorPoint);
            tryPlayBetterModelAnimation("idle", false);
        }
    }

    // --- ATTACK: lunge forward toward target, snap back ---

    private Location attackTarget;
    private Location attackOrigin;

    public void playAttackAnimation(Location target) {
        attackTarget = target;
        attackOrigin = hitboxEntity != null ? hitboxEntity.getLocation().clone() : anchorPoint;
        animState = AnimState.ATTACK;
        animTimer = 24; // 1.2 seconds
        tryPlayBetterModelAnimation("attack", true);
    }

    private void tickAttack() {
        animTimer -= 2;
        if (hitboxEntity == null || attackOrigin == null) return;

        double progress = 1.0 - (animTimer / 24.0);
        progress = Math.min(1.0, Math.max(0.0, progress));

        Location loc;
        if (progress < 0.4) {
            // Phase 1: lunge toward target (40% of animation)
            double lungeProgress = progress / 0.4;
            Vector dir = attackTarget != null
                ? attackTarget.toVector().subtract(attackOrigin.toVector()).normalize()
                : attackOrigin.getDirection();
            loc = attackOrigin.clone().add(dir.multiply(lungeProgress * 5.0));
            loc.add(0, -lungeProgress * 2, 0); // dip down
        } else if (progress < 0.6) {
            // Phase 2: hold at strike point (20%)
            Vector dir = attackTarget != null
                ? attackTarget.toVector().subtract(attackOrigin.toVector()).normalize()
                : attackOrigin.getDirection();
            loc = attackOrigin.clone().add(dir.multiply(5.0));
            loc.add(0, -2, 0);
        } else {
            // Phase 3: pull back to origin (40%)
            double retreatProgress = (progress - 0.6) / 0.4;
            Vector dir = attackTarget != null
                ? attackTarget.toVector().subtract(attackOrigin.toVector()).normalize()
                : attackOrigin.getDirection();
            double dist = 5.0 * (1.0 - retreatProgress);
            double dip = -2.0 * (1.0 - retreatProgress);
            loc = attackOrigin.clone().add(dir.multiply(dist));
            loc.add(0, dip, 0);
        }

        // Face the target
        if (attackTarget != null) {
            Vector look = attackTarget.toVector().subtract(loc.toVector());
            loc.setYaw((float) Math.toDegrees(Math.atan2(-look.getX(), look.getZ())));
            loc.setPitch((float) Math.toDegrees(-Math.atan2(look.getY(), Math.sqrt(look.getX()*look.getX() + look.getZ()*look.getZ()))));
        }
        hitboxEntity.teleport(loc);

        // Impact particles at strike
        World w = loc.getWorld();
        if (w != null && progress >= 0.35 && progress <= 0.65) {
            safeParticle(w, Particle.CRIT, loc.clone().add(0, 1, 0), 10, 1, 1, 1, 0.2);
            safeParticle(w, Particle.SOUL_FIRE_FLAME, loc, 5, 0.5, 0.5, 0.5, 0.05);
        }

        if (animTimer <= 0) {
            animState = AnimState.IDLE;
            anchorPoint = attackOrigin.clone();
            hitboxEntity.teleport(anchorPoint);
            tryPlayBetterModelAnimation("idle", false);
        }
    }

    // --- FIRE BREATH: hold position, particle beam toward target ---

    private Location breathTarget;
    private Location breathOrigin;

    public void playFireBreathAnimation(Location target) {
        breathTarget = target;
        breathOrigin = hitboxEntity != null ? hitboxEntity.getLocation().clone() : anchorPoint;
        animState = AnimState.FIRE_BREATH;
        animTimer = 40; // 2 seconds
        tryPlayBetterModelAnimation("fire_breath", true);
    }

    private void tickFireBreath() {
        animTimer -= 2;
        if (hitboxEntity == null || breathOrigin == null) return;

        // Slight rear-back then lean forward
        double progress = 1.0 - (animTimer / 40.0);
        double leanOffset;
        if (progress < 0.15) {
            leanOffset = -(progress / 0.15) * 1.5; // rear back
        } else {
            leanOffset = -1.5 + ((progress - 0.15) / 0.85) * 2.5; // lean forward
        }

        Location loc = breathOrigin.clone().add(0, 0, 0);
        if (breathTarget != null) {
            Vector dir = breathTarget.toVector().subtract(breathOrigin.toVector()).normalize();
            loc.add(dir.multiply(leanOffset));
            loc.setYaw((float) Math.toDegrees(Math.atan2(-dir.getX(), dir.getZ())));
        }
        hitboxEntity.teleport(loc);

        // Fire breath beam particles
        World w = loc.getWorld();
        if (w != null && progress >= 0.15 && breathTarget != null) {
            Location mouth = loc.clone().add(0, 3, 0);
            Vector beamDir = breathTarget.toVector().subtract(mouth.toVector()).normalize();
            double beamLength = Math.min(mouth.distance(breathTarget), 30);
            for (double d = 0; d < beamLength; d += 0.5) {
                Location p = mouth.clone().add(beamDir.clone().multiply(d));
                double spread = d * 0.06;
                safeParticle(w, Particle.SOUL_FIRE_FLAME, p, 2, spread, spread, spread, 0.02);
                if (d % 2 < 0.5) {
                    safeParticle(w, Particle.SMOKE, p, 1, spread, spread, spread, 0.01);
                }
            }
            if (animTimer % 10 == 0) {
                w.playSound(mouth, Sound.ENTITY_ENDER_DRAGON_SHOOT, 1.5f, 0.5f);
            }
        }

        if (animTimer <= 0) {
            animState = AnimState.IDLE;
            hitboxEntity.teleport(breathOrigin);
            tryPlayBetterModelAnimation("idle", false);
        }
    }

    // --- WALK: move along ground toward a target ---

    public void playWalkAnimation(Location target) {
        walkTarget = target;
        animState = AnimState.WALK;
        tryPlayBetterModelAnimation("walk", false);
    }

    private void tickWalk() {
        if (hitboxEntity == null || walkTarget == null) {
            animState = AnimState.IDLE;
            return;
        }

        Location current = hitboxEntity.getLocation();
        Vector dir = walkTarget.toVector().subtract(current.toVector());
        double dist = dir.length();

        if (dist < 1.5) {
            // Arrived
            anchorPoint = current.clone();
            animState = AnimState.IDLE;
            tryPlayBetterModelAnimation("idle", false);
            return;
        }

        Vector step = dir.normalize().multiply(WALK_STEP);
        Location next = current.add(step);

        // Vertical bob to simulate leg movement
        double walkBob = Math.abs(Math.sin(tickCounter * 0.3)) * 0.3;
        next.setY(next.getY() + walkBob - 0.15);

        // Face direction
        next.setYaw((float) Math.toDegrees(Math.atan2(-step.getX(), step.getZ())));
        next.setPitch(0);
        hitboxEntity.teleport(next);

        // Dust particles at feet
        World w = next.getWorld();
        if (w != null && tickCounter % 6 == 0) {
            safeParticle(w, Particle.CLOUD, next.clone().add(0, -0.5, 0), 3, 0.5, 0.1, 0.5, 0.02);
        }
    }

    // --- FLY: circle around anchor at altitude ---

    public void playFlyAnimation(Location center) {
        anchorPoint = center.clone();
        animState = AnimState.FLY;
        tryPlayBetterModelAnimation("fly", false);
    }

    private void tickFly() {
        if (hitboxEntity == null || anchorPoint == null) return;

        double angle = tickCounter * FLY_CIRCLE_SPEED;
        double x = anchorPoint.getX() + Math.cos(angle) * FLY_CIRCLE_RADIUS;
        double z = anchorPoint.getZ() + Math.sin(angle) * FLY_CIRCLE_RADIUS;
        double y = anchorPoint.getY() + FLY_ALTITUDE + Math.sin(angle * 2) * 2; // slight altitude wave

        Location loc = new Location(anchorPoint.getWorld(), x, y, z);

        // Face tangent direction (perpendicular to radius)
        double tangentX = -Math.sin(angle);
        double tangentZ = Math.cos(angle);
        loc.setYaw((float) Math.toDegrees(Math.atan2(-tangentX, tangentZ)));
        loc.setPitch(-10); // slight downward tilt

        hitboxEntity.teleport(loc);

        // Wing-trail particles
        World w = loc.getWorld();
        if (w != null) {
            safeParticle(w, Particle.CLOUD, loc.clone().add(0, -1, 0), 2, 1, 0.3, 1, 0.01);
            if (tickCounter % 10 == 0) {
                safeParticle(w, Particle.SOUL_FIRE_FLAME, loc, 3, 1.5, 0.5, 1.5, 0.03);
            }
        }
    }

    // --- DEATH: spiral down + collapse ---

    public void playDeathAnimation(Runnable onComplete) {
        alive = false;
        animState = AnimState.DEATH;
        animTimer = 60; // 3 seconds
        animCallback = onComplete;
        tryPlayBetterModelAnimation("death", true);

        if (hitboxEntity != null) {
            anchorPoint = hitboxEntity.getLocation().clone();
        }
    }

    public void playDeathAnimation() {
        playDeathAnimation(null);
    }

    private void tickDeath() {
        animTimer -= 2;
        if (hitboxEntity == null || anchorPoint == null) return;

        double progress = 1.0 - (animTimer / 60.0);
        progress = Math.min(1.0, Math.max(0.0, progress));

        // Spiral down while spinning
        double spiralAngle = progress * Math.PI * 6; // 3 full rotations
        double spiralRadius = (1.0 - progress) * 4;
        double drop = progress * 12;

        double x = anchorPoint.getX() + Math.cos(spiralAngle) * spiralRadius;
        double z = anchorPoint.getZ() + Math.sin(spiralAngle) * spiralRadius;
        double y = anchorPoint.getY() - drop;

        Location loc = new Location(anchorPoint.getWorld(), x, y, z);
        loc.setYaw((float) Math.toDegrees(spiralAngle));
        loc.setPitch((float)(progress * 45)); // tilt downward as it falls
        hitboxEntity.teleport(loc);

        // Death particles
        World w = loc.getWorld();
        if (w != null) {
            safeParticle(w, Particle.SOUL_FIRE_FLAME, loc, 10, 2, 2, 2, 0.1);
            safeParticle(w, Particle.PORTAL, loc, 20, 3, 3, 3, 1.0);
            safeParticle(w, Particle.SMOKE, loc, 8, 1.5, 1.5, 1.5, 0.05);
            if (animTimer == 40 || animTimer == 20 || animTimer == 4) {
                w.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.5f);
                safeParticle(w, Particle.EXPLOSION_EMITTER, loc, 1, 0, 0, 0, 0);
            }
        }

        if (animTimer <= 0) {
            // Final explosion
            if (w != null) {
                safeParticle(w, Particle.EXPLOSION_EMITTER, loc, 3, 2, 2, 2, 0);
                w.playSound(loc, Sound.ENTITY_ENDER_DRAGON_DEATH, 2.0f, 0.7f);
            }
            cleanup();
            if (animCallback != null) {
                animCallback.run();
            }
        }
    }

    // ===================== EFFECTS & PARTICLES =====================

    private void spawnEffects(Location location, World world) {
        try {
            world.playSound(location, Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0f, 0.5f);
            world.playSound(location, Sound.ENTITY_WITHER_SPAWN, 1.5f, 0.7f);
            safeParticle(world, Particle.SOUL_FIRE_FLAME, location, 200, 3, 3, 3, 0.1);
            safeParticle(world, Particle.PORTAL, location, 300, 4, 4, 4, 1.0);
            safeParticle(world, Particle.SMOKE, location, 100, 3, 3, 3, 0.05);
            world.strikeLightningEffect(location);
        } catch (Exception e) {
            logger.warning("[DragonModel] Effects error: " + e.getMessage());
        }
    }

    private void safeParticle(World world, Particle particle, Location loc,
                               int count, double dx, double dy, double dz, double speed) {
        try {
            Class<?> dataType = particle.getDataType();
            if (dataType == Void.class || dataType == null) {
                world.spawnParticle(particle, loc, count, dx, dy, dz, speed);
            } else if (dataType == Float.class || dataType == float.class) {
                world.spawnParticle(particle, loc, count, dx, dy, dz, speed, 1.0f);
            } else {
                // Fallback: use FLAME which needs no data
                world.spawnParticle(Particle.FLAME, loc, count, dx, dy, dz, speed);
            }
        } catch (Exception e) {
            try {
                world.spawnParticle(Particle.FLAME, loc, count / 2, dx, dy, dz, speed);
            } catch (Exception ignored) {}
        }
    }

    // ===================== BACKGROUND TASKS =====================

    private void startInvisibilityEnforcer() {
        invisTaskId = new BukkitRunnable() {
            @Override
            public void run() {
                if (!alive || hitboxEntity == null || hitboxEntity.isDead()) { cancel(); return; }
                hitboxEntity.setInvisible(true);
            }
        }.runTaskTimer(plugin, 5L, 5L).getTaskId();
    }

    private void startParticleTask() {
        particleTaskId = new BukkitRunnable() {
            @Override
            public void run() {
                if (!alive || hitboxEntity == null || hitboxEntity.isDead()) { cancel(); return; }
                Location loc = hitboxEntity.getLocation().add(0, 2, 0);
                World w = loc.getWorld();
                if (w == null) return;
                safeParticle(w, Particle.SOUL_FIRE_FLAME, loc, 3, 1.5, 1.5, 1.5, 0.02);
                if (currentPhase >= 2) safeParticle(w, Particle.PORTAL, loc, 5, 2, 2, 2, 0.5);
                if (currentPhase >= 3) safeParticle(w, Particle.REVERSE_PORTAL, loc, 8, 3, 3, 3, 0.3);
            }
        }.runTaskTimer(plugin, 2L, 4L).getTaskId();
    }

    // ===================== MOVEMENT (called by AbyssDragonBoss) =====================

    public void setTarget(Location target) {
        if (hitboxEntity != null && hitboxEntity.isValid()) moveToward(target, 0.3);
    }

    public void moveToward(Location target, double speed) {
        if (hitboxEntity == null || hitboxEntity.isDead()) return;
        // When boss controller calls moveToward, override to WALK mode
        if (animState == AnimState.IDLE) {
            walkTarget = target;
            animState = AnimState.WALK;
        } else if (animState == AnimState.WALK) {
            walkTarget = target; // update target
        } else {
            // If in another animation, do direct teleport-move
            Location current = hitboxEntity.getLocation();
            Vector dir = target.toVector().subtract(current.toVector());
            if (dir.lengthSquared() < 1) return;
            Vector move = dir.normalize().multiply(speed);
            Location newLoc = current.add(move);
            newLoc.setYaw((float) Math.toDegrees(Math.atan2(-move.getX(), move.getZ())));
            hitboxEntity.teleport(newLoc);
        }
    }

    // ===================== ATTACK VISUALS (called by AbyssDragonBoss) =====================

    public void breathAttackVisual(Location target) {
        playFireBreathAnimation(target);
    }

    public void wingGustVisual() {
        if (hitboxEntity == null) return;
        // Quick attack animation for wing gust
        playAttackAnimation(hitboxEntity.getLocation().add(
            hitboxEntity.getLocation().getDirection().multiply(5)));
        Location loc = hitboxEntity.getLocation().add(0, 2, 0);
        if (loc.getWorld() != null) {
            loc.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_FLAP, 2.0f, 0.8f);
            safeParticle(loc.getWorld(), Particle.CLOUD, loc, 80, 4, 2, 4, 0.1);
        }
    }

    // ===================== PHASE TRANSITIONS =====================

    public void setPhase(int phase) {
        int old = this.currentPhase;
        this.currentPhase = phase;
        if (hitboxEntity == null) return;
        Location loc = hitboxEntity.getLocation();
        World world = loc.getWorld();
        if (world == null) return;

        world.playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 2.5f, 0.3f + (phase * 0.1f));
        safeParticle(world, Particle.PORTAL, loc, 200, 3, 3, 3, 1.0);
        safeParticle(world, Particle.SOUL_FIRE_FLAME, loc, 50, 2, 2, 2, 0.1);
        if (phase >= 3) {
            world.playSound(loc, Sound.ENTITY_WITHER_SPAWN, 1.0f, 1.2f);
        }

        // Scale up the zombie hitbox to feel bigger each phase
        try {
            var scaleAttr = hitboxEntity.getAttribute(Attribute.SCALE);
            if (scaleAttr != null) {
                double newScale = BASE_SCALE + (phase - 1) * 0.5;
                scaleAttr.setBaseValue(newScale);
            }
        } catch (Exception ignored) {}

        logger.info("[DragonModel] Phase " + old + " -> " + phase);
    }

    public void phaseTransition(int phase) { setPhase(phase); }

    public void damageFeedback() {
        if (hitboxEntity != null && hitboxEntity.getWorld() != null)
            safeParticle(hitboxEntity.getWorld(), Particle.DAMAGE_INDICATOR,
                hitboxEntity.getLocation().add(0, 2, 0), 10, 1, 1, 1, 0.1);
    }

    public void damageFlash() { damageFeedback(); }

    // ===================== CLEANUP =====================

    public void cleanup() {
        alive = false;
        animState = AnimState.IDLE;

        if (invisTaskId != -1) { Bukkit.getScheduler().cancelTask(invisTaskId); invisTaskId = -1; }
        if (particleTaskId != -1) { Bukkit.getScheduler().cancelTask(particleTaskId); particleTaskId = -1; }
        if (animationTaskId != -1) { Bukkit.getScheduler().cancelTask(animationTaskId); animationTaskId = -1; }

        // Close BetterModel tracker via reflection
        if (modelTracker != null) {
            try {
                Method closeMethod = modelTracker.getClass().getMethod("close");
                closeMethod.invoke(modelTracker);
            } catch (Exception e) {
                logger.fine("[DragonModel] Tracker close error: " + e.getMessage());
            }
            modelTracker = null;
        }

        if (hitboxEntity != null && !hitboxEntity.isDead()) {
            hitboxEntity.remove();
        }
        hitboxEntity = null;
        logger.info("[DragonModel] Cleanup complete.");
    }

    // ===================== GETTERS =====================

    public double getHealth() {
        return (hitboxEntity != null && !hitboxEntity.isDead()) ? hitboxEntity.getHealth() : 0;
    }

    public Location getLocation() {
        return hitboxEntity != null ? hitboxEntity.getLocation() : null;
    }

    public int getPhase() { return currentPhase; }

    public boolean isAlive() { return alive && hitboxEntity != null && !hitboxEntity.isDead(); }

    public Zombie getHitboxEntity() { return hitboxEntity; }

    public Zombie getBaseEntity() { return hitboxEntity; }

    public Entity getDisplayEntity() { return hitboxEntity; }

    // ===================== UTILITY =====================

    private String formatLoc(Location l) {
        return String.format("(%.1f, %.1f, %.1f in %s)",
            l.getX(), l.getY(), l.getZ(),
            l.getWorld() != null ? l.getWorld().getName() : "null");
    }
}

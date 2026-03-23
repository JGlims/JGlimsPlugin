package com.jglims.plugin.abyss;

import com.jglims.plugin.JGlimsPlugin;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.logging.Logger;

public class AbyssDragonModel {

    private static final float DRAGON_SCALE = 5.0f;
    private static final float RAGE_SCALE = 5.75f;
    private static final float ENRAGE_SCALE = 6.25f;
    private static final String BETTERMODEL_NAME = "Void_Dragon"; // must match .bbmodel filename (without extension)

    private final JGlimsPlugin plugin;
    private final Logger logger;
    private Zombie hitboxEntity;
    
    // BetterModel tracker — stored as Object for soft-dependency safety
    private Object modelTracker; // Actually kr.toxicity.model.api.tracker.EntityTracker
    private boolean usingBetterModel = false;
    
    private int currentPhase = 1;
    private boolean alive = false;
    private int invisTaskId = -1;
    private int particleTaskId = -1;

    public AbyssDragonModel(JGlimsPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    public boolean spawn(Location location, double requestedHp) {
        logger.info("[DragonModel] Spawning at " + formatLoc(location) + " HP=" + requestedHp);
        try {
            World world = location.getWorld();
            if (world == null) { logger.severe("[DragonModel] World is null!"); return false; }
            double hp = Math.min(requestedHp, 2048);

            // Spawn invisible zombie hitbox
            hitboxEntity = (Zombie) world.spawnEntity(location, EntityType.ZOMBIE);
            hitboxEntity.setInvisible(true);
            hitboxEntity.setSilent(true);
            hitboxEntity.setAI(false);
            hitboxEntity.setGravity(false);
            hitboxEntity.setPersistent(true);
            hitboxEntity.setRemoveWhenFarAway(false);
            hitboxEntity.setCustomName("Abyss Dragon");
            hitboxEntity.setCustomNameVisible(false);

            try {
                hitboxEntity.getAttribute(Attribute.MAX_HEALTH).setBaseValue(hp);
                hitboxEntity.setHealth(hp);
            } catch (Exception e) { logger.warning("[DragonModel] Health error: " + e.getMessage()); }

            try {
                var scaleAttr = hitboxEntity.getAttribute(Attribute.SCALE);
                if (scaleAttr != null) scaleAttr.setBaseValue(3.0);
            } catch (Exception ignored) {}

            // Try BetterModel attachment
            usingBetterModel = tryAttachBetterModel();

            if (usingBetterModel) {
                logger.info("[DragonModel] BetterModel attached! Playing idle animation.");
                tryPlayAnimation("idle", true); // loop idle
            } else {
                logger.warning("[DragonModel] BetterModel NOT available — dragon will be INVISIBLE.");
                logger.warning("[DragonModel] Ensure BetterModel 2.1.0 is installed and " + 
                    BETTERMODEL_NAME + ".bbmodel is in plugins/BetterModel/models/");
            }

            spawnEffects(location, world);
            alive = true;
            startInvisibilityEnforcer();
            startParticleTask();
            
            logger.info("[DragonModel] Spawned! BetterModel=" + usingBetterModel);
            return true;
        } catch (Exception e) {
            logger.severe("[DragonModel] SPAWN FAILED: " + e.getMessage());
            e.printStackTrace();
            cleanup();
            return false;
        }
    }

    /**
     * Attaches BetterModel to the hitbox entity using the proper API.
     * Uses compile-time API since build.gradle has the dependency,
     * but wrapped in try-catch for soft-dependency safety.
     */
    private boolean tryAttachBetterModel() {
        try {
            if (Bukkit.getPluginManager().getPlugin("BetterModel") == null) {
                logger.info("[DragonModel] BetterModel plugin not installed");
                return false;
            }

            // Use the proper API — BukkitAdapter.adapt() + BetterModel.model()
            kr.toxicity.model.api.nms.PlatformEntity platformEntity = 
                kr.toxicity.model.api.bukkit.BukkitAdapter.adapt(hitboxEntity);
            
            var optTracker = kr.toxicity.model.api.BetterModel.model(BETTERMODEL_NAME)
                .map(renderer -> renderer.getOrCreate(platformEntity));
            
            if (optTracker.isEmpty()) {
                logger.warning("[DragonModel] Model '" + BETTERMODEL_NAME + "' not found in BetterModel!");
                logger.warning("[DragonModel] Check: plugins/BetterModel/models/" + BETTERMODEL_NAME + ".bbmodel exists");
                logger.warning("[DragonModel] Run /bettermodel reload after placing the file");
                return false;
            }

            modelTracker = optTracker.get();
            logger.info("[DragonModel] BetterModel tracker created successfully!");
            return true;
            
        } catch (NoClassDefFoundError | ClassNotFoundException e) {
            logger.info("[DragonModel] BetterModel API not on classpath: " + e.getMessage());
            return false;
        } catch (Exception e) {
            logger.warning("[DragonModel] BetterModel attachment failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Play a BetterModel animation.
     * @param name animation name (must match animation name in .bbmodel)
     * @param loop true for looping (idle), false for play-once (attack, death)
     */
    private void tryPlayAnimation(String name, boolean loop) {
        if (!usingBetterModel || modelTracker == null) return;
        try {
            var tracker = (kr.toxicity.model.api.tracker.EntityTracker) modelTracker;
            kr.toxicity.model.api.animation.AnimationModifier modifier;
            
            if (loop) {
                modifier = kr.toxicity.model.api.animation.AnimationModifier.DEFAULT;
            } else {
                modifier = kr.toxicity.model.api.animation.AnimationModifier.DEFAULT_WITH_PLAY_ONCE;
            }
            
            tracker.animate(name, modifier, () -> {
                // Animation complete callback
                logger.fine("[DragonModel] Animation '" + name + "' completed");
            });
            
        } catch (Exception e) {
            logger.warning("[DragonModel] Animation '" + name + "' failed: " + e.getMessage());
        }
    }

    private void spawnEffects(Location location, World world) {
        try {
            world.playSound(location, Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0f, 0.5f);
            world.playSound(location, Sound.ENTITY_WITHER_SPAWN, 1.5f, 0.7f);
            safeParticle(world, Particle.SOUL_FIRE_FLAME, location, 200, 3, 3, 3, 0.1);
            safeParticle(world, Particle.PORTAL, location, 300, 4, 4, 4, 1.0);
            safeParticle(world, Particle.SMOKE, location, 100, 3, 3, 3, 0.05);
            world.strikeLightningEffect(location);
        } catch (Exception e) { logger.warning("[DragonModel] Effects error: " + e.getMessage()); }
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
                world.spawnParticle(Particle.FLAME, loc, count, dx, dy, dz, speed);
            }
        } catch (Exception e) {
            try { world.spawnParticle(Particle.FLAME, loc, count / 2, dx, dy, dz, speed); } 
            catch (Exception ignored) {}
        }
    }

    private void startInvisibilityEnforcer() {
        invisTaskId = new BukkitRunnable() {
            public void run() {
                if (!alive || hitboxEntity == null || hitboxEntity.isDead()) { cancel(); return; }
                hitboxEntity.setInvisible(true);
            }
        }.runTaskTimer(plugin, 5L, 5L).getTaskId();
    }

    private void startParticleTask() {
        particleTaskId = new BukkitRunnable() {
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

    // ==================== MOVEMENT ====================
    
    public void setTarget(Location target) {
        if (hitboxEntity != null && hitboxEntity.isValid()) moveToward(target, 0.3);
    }

    public void moveToward(Location target, double speed) {
        if (hitboxEntity == null || hitboxEntity.isDead()) return;
        Location current = hitboxEntity.getLocation();
        Vector dir = target.toVector().subtract(current.toVector());
        if (dir.lengthSquared() < 1) return;
        // Teleport-based movement for smooth BetterModel sync
        Vector move = dir.normalize().multiply(speed);
        Location newLoc = current.add(move);
        newLoc.setYaw((float) Math.toDegrees(Math.atan2(-move.getX(), move.getZ())));
        hitboxEntity.teleport(newLoc);
    }

    // ==================== ATTACK VISUALS ====================

    public void breathAttackVisual(Location target) {
        tryPlayAnimation("attack", false);
        Location from = hitboxEntity.getLocation().add(0, 3, 0);
        Vector dir = target.toVector().subtract(from.toVector()).normalize();
        World world = from.getWorld();
        world.playSound(from, Sound.ENTITY_ENDER_DRAGON_SHOOT, 2.0f, 0.5f);
        new BukkitRunnable() {
            int step = 0;
            public void run() {
                if (step >= 30 || !alive) { cancel(); return; }
                Location p = from.clone().add(dir.clone().multiply(step * 0.5));
                safeParticle(world, Particle.SOUL_FIRE_FLAME, p, 5, 0.3, 0.3, 0.3, 0.02);
                safeParticle(world, Particle.PORTAL, p, 3, 0.2, 0.2, 0.2, 0.1);
                step++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    public void wingGustVisual() {
        if (hitboxEntity == null) return;
        tryPlayAnimation("attack", false); // reuse attack anim for wing gust
        Location loc = hitboxEntity.getLocation().add(0, 2, 0);
        loc.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_FLAP, 2.0f, 0.8f);
        safeParticle(loc.getWorld(), Particle.CLOUD, loc, 80, 4, 2, 4, 0.1);
    }

    // ==================== PHASE TRANSITIONS ====================

    public void setPhase(int phase) {
        int old = this.currentPhase;
        this.currentPhase = phase;
        if (hitboxEntity == null) return;
        Location loc = hitboxEntity.getLocation();
        World world = loc.getWorld();
        world.playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 2.5f, 0.3f + (phase * 0.1f));
        safeParticle(world, Particle.PORTAL, loc, 200, 3, 3, 3, 1.0);
        safeParticle(world, Particle.SOUL_FIRE_FLAME, loc, 50, 2, 2, 2, 0.1);
        if (phase >= 3) {
            world.playSound(loc, Sound.ENTITY_WITHER_SPAWN, 1.0f, 1.2f);
        }
        // BetterModel handles scale via the entity's SCALE attribute, not display transforms
        logger.info("[DragonModel] Phase " + old + " -> " + phase);
    }

    public void phaseTransition(int phase) { setPhase(phase); }

    public void damageFeedback() {
        if (hitboxEntity != null)
            safeParticle(hitboxEntity.getWorld(), Particle.DAMAGE_INDICATOR, 
                hitboxEntity.getLocation().add(0,2,0), 10, 1, 1, 1, 0.1);
    }

    // ==================== DEATH ====================

    public void playDeathAnimation(Runnable onComplete) {
        alive = false;
        if (usingBetterModel && modelTracker != null) {
            tryPlayAnimation("death", false);
            // Wait for death animation to finish (~3 seconds = 60 ticks)
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                cleanup();
                if (onComplete != null) onComplete.run();
            }, 60L);
            return;
        }
        // No BetterModel — immediate cleanup
        cleanup();
        if (onComplete != null) onComplete.run();
    }

    public void cleanup() {
        alive = false;
        if (invisTaskId != -1) { Bukkit.getScheduler().cancelTask(invisTaskId); invisTaskId = -1; }
        if (particleTaskId != -1) { Bukkit.getScheduler().cancelTask(particleTaskId); particleTaskId = -1; }
        if (modelTracker != null) {
            try { 
                ((kr.toxicity.model.api.tracker.EntityTracker) modelTracker).close(); 
            } catch (Exception ignored) {}
            modelTracker = null;
        }
        if (hitboxEntity != null && !hitboxEntity.isDead()) { hitboxEntity.remove(); hitboxEntity = null; }
        logger.info("[DragonModel] Cleanup complete");
    }

    // ==================== GETTERS ====================
    
    public double getHealth() { return (hitboxEntity != null && !hitboxEntity.isDead()) ? hitboxEntity.getHealth() : 0; }
    public Location getLocation() { return hitboxEntity != null ? hitboxEntity.getLocation() : null; }
    public int getPhase() { return currentPhase; }
    public boolean isAlive() { return alive && hitboxEntity != null && !hitboxEntity.isDead(); }
    public Zombie getHitboxEntity() { return hitboxEntity; }
    public Zombie getBaseEntity() { return hitboxEntity; }
    public Entity getDisplayEntity() { return hitboxEntity; } // BetterModel renders on the hitbox entity itself
    public void damageFlash() { damageFeedback(); }
    public void playDeathAnimation() { playDeathAnimation(null); }
    private String formatLoc(Location l) { 
        return String.format("(%.1f, %.1f, %.1f in %s)", l.getX(), l.getY(), l.getZ(), 
            l.getWorld() != null ? l.getWorld().getName() : "null"); 
    }
}

package com.jglims.plugin.abyss;

import com.jglims.plugin.JGlimsPlugin;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;
import java.util.logging.Logger;

public class AbyssDragonModel {

    private static final float DRAGON_SCALE = 5.0f;
    private static final float RAGE_SCALE = 5.75f;
    private static final float ENRAGE_SCALE = 6.25f;
    private static final int CUSTOM_MODEL_DATA = 40000;
    private static final String BETTERMODEL_NAME = "abyss_dragon";

    private final JGlimsPlugin plugin;
    private final Logger logger;
    private Zombie hitboxEntity;
    private ItemDisplay displayEntity;
    private Object modelTracker;
    private boolean usingBetterModel = false;
    private int currentPhase = 1;
    private boolean alive = false;
    private int animationTaskId = -1;
    private int invisTaskId = -1;

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

            hitboxEntity = (Zombie) world.spawnEntity(location, EntityType.ZOMBIE);
            hitboxEntity.setInvisible(true);
            hitboxEntity.setSilent(true);
            hitboxEntity.setAI(false);
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

            usingBetterModel = tryAttachBetterModel();

            if (!usingBetterModel) {
                logger.info("[DragonModel] BetterModel not available, using ItemDisplay fallback");
                spawnItemDisplay(location);
            }

            spawnEffects(location, world);
            alive = true;
            startTasks();
            logger.info("[DragonModel] Spawned! BetterModel=" + usingBetterModel);
            return true;
        } catch (Exception e) {
            logger.severe("[DragonModel] SPAWN FAILED: " + e.getMessage());
            e.printStackTrace();
            cleanup();
            return false;
        }
    }

    private boolean tryAttachBetterModel() {
        try {
            if (Bukkit.getPluginManager().getPlugin("BetterModel") == null) {
                logger.info("[DragonModel] BetterModel plugin not installed");
                return false;
            }
            Class<?> bmClass = Class.forName("kr.toxicity.model.api.BetterModel");
            Class<?> adapterClass = Class.forName("kr.toxicity.model.api.nms.BukkitAdapter");
            java.lang.reflect.Method modelMethod = bmClass.getMethod("model", String.class);
            Object optRenderer = modelMethod.invoke(null, BETTERMODEL_NAME);
            java.util.Optional<?> opt = (java.util.Optional<?>) optRenderer;
            if (opt.isEmpty()) {
                logger.warning("[DragonModel] Model " + BETTERMODEL_NAME + " not found in BetterModel");
                return false;
            }
            Object renderer = opt.get();
            java.lang.reflect.Method adaptMethod = adapterClass.getMethod("adapt", Object.class);
            Object platformEntity = adaptMethod.invoke(null, hitboxEntity);
            Class<?> peClass = Class.forName("kr.toxicity.model.api.nms.PlatformEntity");
            java.lang.reflect.Method createMethod = renderer.getClass().getMethod("getOrCreate", peClass);
            modelTracker = createMethod.invoke(renderer, platformEntity);
            logger.info("[DragonModel] BetterModel attached!");
            return true;
        } catch (ClassNotFoundException e) {
            logger.info("[DragonModel] BetterModel API not on classpath: " + e.getMessage());
            return false;
        } catch (Exception e) {
            logger.warning("[DragonModel] BetterModel failed: " + e.getMessage());
            return false;
        }
    }

    private void tryPlayAnimation(String name) {
        if (!usingBetterModel || modelTracker == null) return;
        try {
            Class<?> modClass = Class.forName("kr.toxicity.model.api.data.renderer.AnimationModifier");
            Object defMod = modClass.getField("DEFAULT").get(null);
            modelTracker.getClass().getMethod("animate", String.class, modClass, Runnable.class)
                .invoke(modelTracker, name, defMod, (Runnable) () -> {});
        } catch (Exception ignored) {}
    }

    private void spawnItemDisplay(Location location) {
        World world = location.getWorld();
        try {
            displayEntity = (ItemDisplay) world.spawnEntity(location, EntityType.ITEM_DISPLAY);
            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) { meta.setCustomModelData(CUSTOM_MODEL_DATA); item.setItemMeta(meta); }
            displayEntity.setItemStack(item);
            displayEntity.setTransformation(new Transformation(
                new Vector3f(0, 2, 0), new AxisAngle4f(0, 0, 1, 0),
                new Vector3f(DRAGON_SCALE, DRAGON_SCALE, DRAGON_SCALE), new AxisAngle4f(0, 0, 1, 0)));
            displayEntity.setBillboard(Display.Billboard.CENTER);
            logger.info("[DragonModel] ItemDisplay fallback spawned");
        } catch (Exception e) {
            logger.severe("[DragonModel] ItemDisplay failed: " + e.getMessage());
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

    private void safeParticle(World world, Particle particle, Location loc, int count, double dx, double dy, double dz, double speed) {
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
            try { world.spawnParticle(Particle.FLAME, loc, count / 2, dx, dy, dz, speed); } catch (Exception ignored) {}
        }
    }

    private void startTasks() {
        invisTaskId = new BukkitRunnable() {
            public void run() {
                if (!alive || hitboxEntity == null || hitboxEntity.isDead()) { cancel(); return; }
                hitboxEntity.setInvisible(true);
            }
        }.runTaskTimer(plugin, 5L, 5L).getTaskId();

        if (!usingBetterModel && displayEntity != null) {
            animationTaskId = new BukkitRunnable() {
                float tick = 0;
                public void run() {
                    if (!alive || hitboxEntity == null || hitboxEntity.isDead()) { cancel(); return; }
                    tick += 0.1f;
                    try {
                        float bobY = (float)(2.0 + Math.sin(tick) * 0.5);
                        float rot = tick * 0.5f;
                        float scale = currentPhase >= 3 ? ENRAGE_SCALE : currentPhase >= 2 ? RAGE_SCALE : DRAGON_SCALE;
                        if (displayEntity != null && displayEntity.isValid()) {
                            displayEntity.setTransformation(new Transformation(
                                new Vector3f(0, bobY, 0), new AxisAngle4f(rot, 0, 1, 0),
                                new Vector3f(scale, scale, scale), new AxisAngle4f(0, 0, 1, 0)));
                            if (hitboxEntity != null) displayEntity.teleport(hitboxEntity.getLocation());
                        }
                        Location loc = hitboxEntity.getLocation().add(0, 2, 0);
                        safeParticle(loc.getWorld(), Particle.SOUL_FIRE_FLAME, loc, 3, 1.5, 1.5, 1.5, 0.02);
                        safeParticle(loc.getWorld(), Particle.PORTAL, loc, 5, 2, 2, 2, 0.5);
                    } catch (Exception e) { logger.warning("[DragonModel] Anim error: " + e.getMessage()); }
                }
            }.runTaskTimer(plugin, 2L, 2L).getTaskId();
        }
    }

    public void setTarget(Location target) {
        if (hitboxEntity != null && hitboxEntity.isValid()) moveToward(target, 0.3);
    }

    public void moveToward(Location target, double speed) {
        if (hitboxEntity == null || hitboxEntity.isDead()) return;
        Location current = hitboxEntity.getLocation();
        Vector dir = target.toVector().subtract(current.toVector());
        if (dir.lengthSquared() < 1) return;
        hitboxEntity.setVelocity(dir.normalize().multiply(speed));
    }

    public void breathAttackVisual(Location target) {
        tryPlayAnimation("attack");
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
        tryPlayAnimation("fly");
        Location loc = hitboxEntity.getLocation().add(0, 2, 0);
        loc.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_FLAP, 2.0f, 0.8f);
        safeParticle(loc.getWorld(), Particle.CLOUD, loc, 80, 4, 2, 4, 0.1);
        safeParticle(loc.getWorld(), Particle.SOUL_FIRE_FLAME, loc, 30, 3, 1, 3, 0.05);
    }

    public void setPhase(int phase) {
        int old = this.currentPhase;
        this.currentPhase = phase;
        if (hitboxEntity == null) return;
        Location loc = hitboxEntity.getLocation();
        World world = loc.getWorld();
        world.playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 2.5f, 0.3f + (phase * 0.1f));
        world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.8f);
        safeParticle(world, Particle.PORTAL, loc, 200, 3, 3, 3, 1.0);
        safeParticle(world, Particle.SOUL_FIRE_FLAME, loc, 50, 2, 2, 2, 0.1);
        if (phase >= 3) {
            world.playSound(loc, Sound.ENTITY_WITHER_SPAWN, 1.0f, 1.2f);
            tryPlayAnimation("fly");
        }
        logger.info("[DragonModel] Phase " + old + " -> " + phase);
    }

    public void phaseTransition(int phase) { setPhase(phase); }

    public void damageFeedback() {
        if (displayEntity != null && displayEntity.isValid()) {
            try {
                displayEntity.setBrightness(new Display.Brightness(15, 15));
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (displayEntity != null && displayEntity.isValid())
                        displayEntity.setBrightness(new Display.Brightness(0, 0));
                }, 3L);
            } catch (Exception ignored) {}
        }
        if (hitboxEntity != null)
            safeParticle(hitboxEntity.getWorld(), Particle.DAMAGE_INDICATOR, hitboxEntity.getLocation().add(0,2,0), 10, 1,1,1, 0.1);
    }

    public void playDeathAnimation(Runnable onComplete) {
        alive = false;
        if (usingBetterModel && modelTracker != null) {
            tryPlayAnimation("death");
            Bukkit.getScheduler().runTaskLater(plugin, () -> { cleanup(); if (onComplete != null) onComplete.run(); }, 60L);
            return;
        }
        if (hitboxEntity == null) { cleanup(); if (onComplete != null) onComplete.run(); return; }
        Location loc = hitboxEntity.getLocation();
        World world = loc.getWorld();
        new BukkitRunnable() {
            int t = 0;
            public void run() {
                if (t >= 60) { cancel(); cleanup(); if (onComplete != null) onComplete.run(); return; }
                float p = t / 60.0f;
                float s = DRAGON_SCALE * (1.0f - p);
                if (displayEntity != null && displayEntity.isValid()) {
                    try { displayEntity.setTransformation(new Transformation(
                        new Vector3f(0, 2-(p*2), 0), new AxisAngle4f(t*0.3f, 0,1,0),
                        new Vector3f(Math.max(s,0.1f), Math.max(s,0.1f), Math.max(s,0.1f)),
                        new AxisAngle4f(p*2, 1,0,0))); } catch (Exception ignored) {}
                }
                if (t % 5 == 0) {
                    world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.8f+(p*0.5f));
                    safeParticle(world, Particle.SOUL_FIRE_FLAME, loc.clone().add(0,2,0), 20, 2,2,2, 0.1);
                    safeParticle(world, Particle.PORTAL, loc.clone().add(0,2,0), 30, 3,3,3, 1.0);
                }
                t++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    public void cleanup() {
        alive = false;
        if (animationTaskId != -1) { try { Bukkit.getScheduler().cancelTask(animationTaskId); } catch (Exception ignored) {} animationTaskId = -1; }
        if (invisTaskId != -1) { try { Bukkit.getScheduler().cancelTask(invisTaskId); } catch (Exception ignored) {} invisTaskId = -1; }
        if (modelTracker != null) {
            try { modelTracker.getClass().getMethod("close").invoke(modelTracker); } catch (Exception ignored) {}
            modelTracker = null;
        }
        if (displayEntity != null && displayEntity.isValid()) { displayEntity.remove(); displayEntity = null; }
        if (hitboxEntity != null && !hitboxEntity.isDead()) { hitboxEntity.remove(); hitboxEntity = null; }
        logger.info("[DragonModel] Cleanup complete");
    }

    public double getHealth() { return (hitboxEntity != null && !hitboxEntity.isDead()) ? hitboxEntity.getHealth() : 0; }
    public Location getLocation() { return hitboxEntity != null ? hitboxEntity.getLocation() : null; }
    public int getPhase() { return currentPhase; }
    public boolean isAlive() { return alive && hitboxEntity != null && !hitboxEntity.isDead(); }
    public Zombie getHitboxEntity() { return hitboxEntity; }
    public Entity getDisplayEntity() { return usingBetterModel ? hitboxEntity : displayEntity; }
    private String formatLoc(Location l) { return String.format("(%.1f, %.1f, %.1f in %s)", l.getX(), l.getY(), l.getZ(), l.getWorld() != null ? l.getWorld().getName() : "null"); }

    // === Compatibility methods for AbyssDragonBoss ===
    public Zombie getBaseEntity() { return hitboxEntity; }
    public void damageFlash() { damageFeedback(); }
    public void playDeathAnimation() { playDeathAnimation(null); }
}
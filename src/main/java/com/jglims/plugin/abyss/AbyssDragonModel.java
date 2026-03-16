package com.jglims.plugin.abyss;

import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.jglims.plugin.JGlimsPlugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class AbyssDragonModel {

    private final JGlimsPlugin plugin;
    private Zombie baseEntity;
    private ItemDisplay dragonDisplay;
    private boolean alive = false;
    private int animationTaskId = -1;
    private float hoverPhase = 0f;
    private int currentPhase = 1;

    private static final float DRAGON_SCALE = 5.0f;
    private static final int CUSTOM_MODEL_DATA = 40000;

    public AbyssDragonModel(JGlimsPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Spawns the dragon at the given location with the specified HP.
     */
    public void spawn(Location location, double maxHp) {
        if (alive) return;
        World world = location.getWorld();
        if (world == null) return;

        baseEntity = world.spawn(location, Zombie.class, zombie -> {
            zombie.setInvisible(true);
            zombie.setSilent(true);
            zombie.setAI(false);
            zombie.setGravity(false);
            zombie.setInvulnerable(false);
            zombie.customName(Component.text("Abyssal Void Dragon", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD));
            zombie.setCustomNameVisible(true);
            zombie.setAdult();
            zombie.setCanPickupItems(false);
            zombie.setRemoveWhenFarAway(false);
            zombie.addScoreboardTag("abyss_dragon_base");

            Objects.requireNonNull(zombie.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(maxHp);
            zombie.setHealth(maxHp);

            if (zombie.getAttribute(Attribute.SCALE) != null) {
                zombie.getAttribute(Attribute.SCALE).setBaseValue(3.0);
            }
        });

        ItemStack dragonItem = new ItemStack(Material.PAPER);
        ItemMeta meta = dragonItem.getItemMeta();
        if (meta != null) {
            meta.setCustomModelData(CUSTOM_MODEL_DATA);
            dragonItem.setItemMeta(meta);
        }

        Location displayLoc = location.clone().add(0, 1.5, 0);
        dragonDisplay = world.spawn(displayLoc, ItemDisplay.class, display -> {
            display.setItemStack(dragonItem);
            display.setBillboard(Display.Billboard.FIXED);
            display.setViewRange(1.5f);
            display.addScoreboardTag("abyss_dragon_part");

            Quaternionf noRotation = new Quaternionf();
            display.setTransformation(new Transformation(
                    new Vector3f(-DRAGON_SCALE / 2f, -0.5f, -DRAGON_SCALE / 2f),
                    noRotation,
                    new Vector3f(DRAGON_SCALE, DRAGON_SCALE, DRAGON_SCALE),
                    noRotation
            ));

            display.setInterpolationDuration(3);
            display.setShadowRadius(4.0f);
            display.setShadowStrength(0.6f);
        });

        baseEntity.addPassenger(dragonDisplay);
        alive = true;
        startAnimation();

        world.playSound(location, Sound.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.HOSTILE, 2.0f, 0.5f);
        world.spawnParticle(Particle.DRAGON_BREATH, location, 100, 3, 2, 3, 0.05);
        world.spawnParticle(Particle.PORTAL, location, 200, 4, 3, 4, 1.0);
    }

    /**
     * Overload: spawn with default 1500 HP.
     */
    public void spawn(Location location) {
        spawn(location, 1500.0);
    }

    private void startAnimation() {
        animationTaskId = new BukkitRunnable() {
            @Override
            public void run() {
                if (!alive || baseEntity == null || baseEntity.isDead()) {
                    cleanup();
                    cancel();
                    return;
                }

                hoverPhase += 0.08f;

                Location loc = baseEntity.getLocation();
                double hoverY = Math.sin(hoverPhase) * 0.4;
                Vector velocity = new Vector(0, hoverY * 0.05, 0);
                baseEntity.setVelocity(velocity);

                Location particleLoc = loc.clone().add(0, 2, 0);
                loc.getWorld().spawnParticle(Particle.DRAGON_BREATH, particleLoc, 3, 2, 1, 2, 0.01);

                Location headLoc = loc.clone().add(loc.getDirection().multiply(2.5)).add(0, 3.5, 0);
                loc.getWorld().spawnParticle(Particle.END_ROD, headLoc, 1, 0.2, 0.1, 0.2, 0.01);

                double wingSpread = Math.cos(hoverPhase * 0.5) * 3;
                Location leftWing = loc.clone().add(-wingSpread, 2.5, 0);
                Location rightWing = loc.clone().add(wingSpread, 2.5, 0);
                loc.getWorld().spawnParticle(Particle.PORTAL, leftWing, 2, 0.5, 0.3, 0.5, 0.1);
                loc.getWorld().spawnParticle(Particle.PORTAL, rightWing, 2, 0.5, 0.3, 0.5, 0.1);

                if (currentPhase >= 3) {
                    loc.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, particleLoc, 2, 1.5, 1, 1.5, 0.02);
                }
                if (currentPhase >= 4) {
                    loc.getWorld().spawnParticle(Particle.REVERSE_PORTAL, particleLoc, 5, 2, 1.5, 2, 0.05);
                }

                if (dragonDisplay != null && dragonDisplay.isValid()) {
                    float yaw = (float) Math.sin(hoverPhase * 0.3) * 0.15f;
                    Quaternionf rotation = new Quaternionf().rotateY(yaw);
                    Transformation current = dragonDisplay.getTransformation();
                    dragonDisplay.setTransformation(new Transformation(
                            current.getTranslation(),
                            rotation,
                            current.getScale(),
                            new Quaternionf()
                    ));
                    dragonDisplay.setInterpolationDelay(0);
                    dragonDisplay.setInterpolationDuration(5);
                }
            }
        }.runTaskTimer(plugin, 0L, 2L).getTaskId();
    }

    // ── Methods called by AbyssDragonBoss ──

    /**
     * Moves the dragon toward a target location.
     */
    public void setTarget(Location target) {
        moveToward(target, 0.6);
    }

    public void moveToward(Location target, double speed) {
        if (baseEntity == null || baseEntity.isDead()) return;
        Location current = baseEntity.getLocation();
        Vector direction = target.toVector().subtract(current.toVector()).normalize().multiply(speed);
        baseEntity.setVelocity(direction);
        current.setDirection(direction);
        baseEntity.teleport(baseEntity.getLocation().setDirection(direction));
    }

    /**
     * Visual effect for the breath attack.
     */
    public void breathAttackVisual(Location target) {
        if (baseEntity == null || baseEntity.isDead()) return;
        Location from = baseEntity.getLocation().add(0, 3, 0);
        World world = from.getWorld();
        Vector dir = target.toVector().subtract(from.toVector()).normalize();

        for (double d = 0; d < from.distance(target) && d < 20; d += 0.5) {
            Location point = from.clone().add(dir.clone().multiply(d));
            world.spawnParticle(Particle.DRAGON_BREATH, point, 3, 0.3, 0.3, 0.3, 0.01);
            world.spawnParticle(Particle.SOUL_FIRE_FLAME, point, 1, 0.2, 0.2, 0.2, 0.01);
        }
        world.playSound(from, Sound.ENTITY_ENDER_DRAGON_SHOOT, SoundCategory.HOSTILE, 1.5f, 0.7f);
    }

    /**
     * Visual effect for a wing gust.
     */
    public void wingGustVisual() {
        if (baseEntity == null || baseEntity.isDead()) return;
        Location loc = baseEntity.getLocation().add(0, 2, 0);
        World world = loc.getWorld();

        for (int angle = 0; angle < 360; angle += 15) {
            double rad = Math.toRadians(angle);
            for (double r = 1; r < 8; r += 1) {
                double x = r * Math.cos(rad);
                double z = r * Math.sin(rad);
                world.spawnParticle(Particle.CLOUD, loc.clone().add(x, -0.5, z), 1, 0, 0, 0, 0.05);
            }
        }
        world.spawnParticle(Particle.EXPLOSION, loc, 3, 2, 0.5, 2, 0);
    }

    /**
     * Phase transition visual effect.
     */
    public void phaseTransition(int phase) {
        setPhase(phase + 1);
    }

    public void setPhase(int phase) {
        this.currentPhase = phase;
        if (baseEntity != null && !baseEntity.isDead()) {
            Location loc = baseEntity.getLocation();
            World world = loc.getWorld();

            switch (phase) {
                case 2 -> {
                    world.playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.HOSTILE, 2.0f, 0.7f);
                    world.spawnParticle(Particle.DRAGON_BREATH, loc.clone().add(0, 2, 0), 60, 3, 2, 3, 0.08);
                }
                case 3 -> {
                    world.playSound(loc, Sound.ENTITY_WITHER_AMBIENT, SoundCategory.HOSTILE, 1.5f, 0.5f);
                    world.spawnParticle(Particle.SOUL_FIRE_FLAME, loc.clone().add(0, 2, 0), 80, 3, 2, 3, 0.1);
                    if (dragonDisplay != null && dragonDisplay.isValid()) {
                        float rageScale = DRAGON_SCALE * 1.15f;
                        dragonDisplay.setTransformation(new Transformation(
                                new Vector3f(-rageScale / 2f, -0.5f, -rageScale / 2f),
                                new Quaternionf(),
                                new Vector3f(rageScale, rageScale, rageScale),
                                new Quaternionf()
                        ));
                    }
                }
                case 4 -> {
                    world.playSound(loc, Sound.ENTITY_ENDER_DRAGON_DEATH, SoundCategory.HOSTILE, 2.0f, 0.3f);
                    world.spawnParticle(Particle.REVERSE_PORTAL, loc.clone().add(0, 2, 0), 150, 4, 3, 4, 0.2);
                    world.spawnParticle(Particle.EXPLOSION, loc, 10, 3, 2, 3, 0.1);
                }
            }
        }
    }

    /**
     * Flashes the dragon red when taking damage.
     */
    public void damageFlash() {
        flashDamage();
    }

    public void flashDamage() {
        if (dragonDisplay == null || !dragonDisplay.isValid()) return;

        dragonDisplay.setGlowing(true);
        dragonDisplay.setGlowColorOverride(org.bukkit.Color.fromRGB(180, 30, 60));

        new BukkitRunnable() {
            @Override
            public void run() {
                if (dragonDisplay != null && dragonDisplay.isValid()) {
                    dragonDisplay.setGlowing(false);
                }
            }
        }.runTaskLater(plugin, 4L);

        if (baseEntity != null && !baseEntity.isDead()) {
            Location loc = baseEntity.getLocation().add(0, 2, 0);
            loc.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, loc, 8, 1.5, 1, 1.5, 0.1);
        }
    }

    /**
     * Plays the death animation and removes the dragon.
     */
    public void deathAnimation() {
        playDeathAnimation();
    }

    public void playDeathAnimation() {
        alive = false;

        if (baseEntity == null || baseEntity.isDead()) {
            cleanup();
            return;
        }

        Location loc = baseEntity.getLocation();
        World world = loc.getWorld();

        if (animationTaskId != -1) {
            Bukkit.getScheduler().cancelTask(animationTaskId);
            animationTaskId = -1;
        }

        world.playSound(loc, Sound.ENTITY_ENDER_DRAGON_DEATH, SoundCategory.HOSTILE, 2.5f, 0.5f);

        new BukkitRunnable() {
            int ticks = 0;
            final int DURATION = 60;

            @Override
            public void run() {
                if (ticks >= DURATION) {
                    world.spawnParticle(Particle.EXPLOSION_EMITTER, loc.clone().add(0, 2, 0), 5, 2, 2, 2, 0.1);
                    world.spawnParticle(Particle.DRAGON_BREATH, loc.clone().add(0, 2, 0), 200, 4, 3, 4, 0.15);
                    world.spawnParticle(Particle.END_ROD, loc.clone().add(0, 3, 0), 100, 3, 3, 3, 0.3);
                    world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 2.0f, 0.5f);

                    cleanup();
                    cancel();
                    return;
                }

                float progress = (float) ticks / DURATION;

                if (dragonDisplay != null && dragonDisplay.isValid()) {
                    float scale = DRAGON_SCALE * (1.0f - progress * 0.8f);
                    Quaternionf spin = new Quaternionf().rotateY(progress * 12.0f);
                    dragonDisplay.setTransformation(new Transformation(
                            new Vector3f(-scale / 2f, -0.5f + progress * -2f, -scale / 2f),
                            spin,
                            new Vector3f(scale, scale, scale),
                            new Quaternionf()
                    ));
                    dragonDisplay.setInterpolationDelay(0);
                    dragonDisplay.setInterpolationDuration(2);
                }

                world.spawnParticle(Particle.DRAGON_BREATH, loc.clone().add(0, 2, 0), 5, 2, 1.5, 2, 0.05);
                world.spawnParticle(Particle.PORTAL, loc.clone().add(0, 2, 0), 10, 2, 2, 2, 1.0);

                if (ticks % 10 == 0) {
                    world.playSound(loc, Sound.ENTITY_ENDER_DRAGON_HURT, SoundCategory.HOSTILE, 1.0f, 0.3f + progress);
                }

                ticks += 2;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    public void cleanup() {
        alive = false;

        if (animationTaskId != -1) {
            Bukkit.getScheduler().cancelTask(animationTaskId);
            animationTaskId = -1;
        }

        if (dragonDisplay != null && dragonDisplay.isValid()) {
            dragonDisplay.remove();
            dragonDisplay = null;
        }

        if (baseEntity != null && baseEntity.isValid()) {
            baseEntity.remove();
            baseEntity = null;
        }
    }

    // ── Getters ──

    public boolean isAlive() {
        return alive && baseEntity != null && !baseEntity.isDead();
    }

    public Zombie getBaseEntity() {
        return baseEntity;
    }

    public ItemDisplay getDragonDisplay() {
        return dragonDisplay;
    }

    public Location getLocation() {
        if (baseEntity != null && !baseEntity.isDead()) {
            return baseEntity.getLocation();
        }
        return null;
    }

    public double getHealth() {
        if (baseEntity != null && !baseEntity.isDead()) {
            return baseEntity.getHealth();
        }
        return 0;
    }

    public double getMaxHealth() {
        if (baseEntity != null && !baseEntity.isDead()) {
            return Objects.requireNonNull(baseEntity.getAttribute(Attribute.MAX_HEALTH)).getValue();
        }
        return 1500;
    }

    public int getCurrentPhase() {
        return currentPhase;
    }
}

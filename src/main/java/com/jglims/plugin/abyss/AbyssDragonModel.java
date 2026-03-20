package com.jglims.plugin.abyss;

import java.util.List;
import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.Color;
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
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.jglims.plugin.JGlimsPlugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * AbyssDragonModel v7.0 — Fixed spawn crash + string CMD for 1.21.4+ packs
 *
 * Uses invisible Zombie as hitbox + ItemDisplay with custom 3D model.
 * The spawn bug was caused by setHealth() inside the spawn lambda before Paper
 * flushes the MAX_HEALTH attribute. Fixed by setting health AFTER spawn returns.
 *
 * v7.0 change: sets BOTH integer CMD (40000) and string CMD ("abyss_dragon")
 * so the resource pack item definition JSON can match via the string "when" clause.
 */
public class AbyssDragonModel {

    private final JGlimsPlugin plugin;
    private Zombie baseEntity;
    private ItemDisplay dragonDisplay;
    private boolean alive = false;
    private int animationTaskId = -1;
    private float hoverPhase = 0f;
    private int currentPhase = 1;
    private Location lastKnownLocation;

    private static final float DRAGON_SCALE = 5.0f;
    private static final float RAGE_SCALE = DRAGON_SCALE * 1.15f;
    private static final float ENRAGE_SCALE = DRAGON_SCALE * 1.25f;
    private static final int CUSTOM_MODEL_DATA = 40000;
    private static final String CUSTOM_MODEL_DATA_STRING = "abyss_dragon";

    public AbyssDragonModel(JGlimsPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Spawns the dragon at the given location with the specified max HP.
     * CRITICAL FIX: Health attributes set AFTER spawn(), not inside lambda.
     */
    public void spawn(Location location, double maxHp) {
        if (alive) return;
        World world = location.getWorld();
        if (world == null) throw new IllegalStateException("World is null for dragon spawn location");

        this.lastKnownLocation = location.clone();

        plugin.getLogger().info("[DragonModel] Attempting spawn at " +
                location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() +
                " with " + maxHp + " HP");

        // Step 1: Spawn the invisible zombie base (hitbox entity)
        // DO NOT set health inside this lambda — Paper validates it before attribute flush
        baseEntity = world.spawn(location, Zombie.class, zombie -> {
            zombie.setInvisible(true);
            zombie.setSilent(true);
            zombie.setAI(false);
            zombie.setGravity(false);
            zombie.setInvulnerable(false);
            zombie.customName(Component.text("Abyssal Void Dragon", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD));
            zombie.setCustomNameVisible(false);
            zombie.setAdult();
            zombie.setCanPickupItems(false);
            zombie.setRemoveWhenFarAway(false);
            zombie.addScoreboardTag("abyss_dragon_base");
        });

        plugin.getLogger().info("[DragonModel] Zombie base spawned: " + baseEntity.getUniqueId());

        // Step 2: Set health attributes OUTSIDE the lambda (Paper-safe)
        try {
            Objects.requireNonNull(baseEntity.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(maxHp);
            baseEntity.setHealth(maxHp);
            plugin.getLogger().info("[DragonModel] Health set to " + maxHp);
        } catch (Exception e) {
            plugin.getLogger().severe("[DragonModel] Failed to set dragon health: " + e.getMessage());
            e.printStackTrace();
            baseEntity.remove();
            throw new RuntimeException("Dragon health setup failed", e);
        }

        // Step 3: Set scale attribute if available (Paper 1.20.5+)
        try {
            var scaleAttr = baseEntity.getAttribute(Attribute.SCALE);
            if (scaleAttr != null) {
                scaleAttr.setBaseValue(3.0);
                plugin.getLogger().info("[DragonModel] SCALE attribute set to 3.0");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("[DragonModel] Could not set SCALE attribute: " + e.getMessage());
        }

        // Step 4: Create the dragon visual item with BOTH integer and string CMD
        ItemStack dragonItem = new ItemStack(Material.PAPER);
        ItemMeta meta = dragonItem.getItemMeta();
        if (meta != null) {
            // Legacy integer CMD (backwards compatibility)
            meta.setCustomModelData(CUSTOM_MODEL_DATA);

            // String-based CMD for 1.21.4+ resource pack item definitions
            try {
                CustomModelDataComponent cmdComponent = meta.getCustomModelDataComponent();
                cmdComponent.setStrings(List.of(CUSTOM_MODEL_DATA_STRING));
                meta.setCustomModelDataComponent(cmdComponent);
                plugin.getLogger().info("[DragonModel] String CMD set: " + CUSTOM_MODEL_DATA_STRING);
            } catch (Exception e) {
                plugin.getLogger().warning("[DragonModel] Could not set string CMD (older API?): " + e.getMessage());
            }

            dragonItem.setItemMeta(meta);
        }

        // Step 5: Spawn the ItemDisplay entity
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

        plugin.getLogger().info("[DragonModel] ItemDisplay spawned: " + dragonDisplay.getUniqueId());

        // Step 6: Mount display on zombie
        baseEntity.addPassenger(dragonDisplay);
        alive = true;
        startAnimation();

        // Step 7: Spawn effects
        world.playSound(location, Sound.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.HOSTILE, 2.0f, 0.5f);
        world.playSound(location, Sound.ENTITY_WITHER_SPAWN, SoundCategory.HOSTILE, 1.0f, 0.7f);
        world.spawnParticle(Particle.DRAGON_BREATH, location, 150, 4, 3, 4, 0.08);
        world.spawnParticle(Particle.PORTAL, location, 300, 5, 4, 5, 1.0);
        world.spawnParticle(Particle.REVERSE_PORTAL, location, 100, 3, 2, 3, 0.5);

        plugin.getLogger().info("[DragonModel] Dragon spawned successfully at " +
                location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() +
                " with " + maxHp + " HP (CMD: " + CUSTOM_MODEL_DATA + " / \"" + CUSTOM_MODEL_DATA_STRING + "\")");
    }

    /**
     * Overload: spawn with default 1500 HP.
     */
    public void spawn(Location location) {
        spawn(location, 1500.0);
    }

    /**
     * Animation loop: hover bob, ambient particles, phase-based effects.
     */
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
                lastKnownLocation = loc.clone();

                // Hover bob — gentle sine wave
                double hoverY = Math.sin(hoverPhase) * 0.3;
                baseEntity.setVelocity(new Vector(0, hoverY * 0.04, 0));

                World world = loc.getWorld();
                if (world == null) return;

                // Body ambient particles
                Location bodyLoc = loc.clone().add(0, 2, 0);
                world.spawnParticle(Particle.DRAGON_BREATH, bodyLoc, 2, 1.5, 0.8, 1.5, 0.005);

                // Eye glow (near head, forward direction)
                Vector forward = loc.getDirection().normalize().multiply(3.0);
                Location headLoc = loc.clone().add(forward).add(0, 3.5, 0);
                world.spawnParticle(Particle.END_ROD, headLoc, 1, 0.15, 0.1, 0.15, 0.005);

                // Wing trail particles (spread based on hover phase)
                double wingSpread = Math.cos(hoverPhase * 0.5) * 4;
                Vector right = new Vector(-forward.getZ(), 0, forward.getX()).normalize();
                Location leftWing = loc.clone().add(right.clone().multiply(-wingSpread)).add(0, 2, 0);
                Location rightWing = loc.clone().add(right.clone().multiply(wingSpread)).add(0, 2, 0);
                world.spawnParticle(Particle.PORTAL, leftWing, 1, 0.4, 0.2, 0.4, 0.05);
                world.spawnParticle(Particle.PORTAL, rightWing, 1, 0.4, 0.2, 0.4, 0.05);

                // Phase-based ambient effects
                if (currentPhase >= 3) {
                    world.spawnParticle(Particle.SOUL_FIRE_FLAME, bodyLoc, 3, 2, 1, 2, 0.02);
                }
                if (currentPhase >= 4) {
                    world.spawnParticle(Particle.REVERSE_PORTAL, bodyLoc, 5, 2.5, 1.5, 2.5, 0.05);
                    world.spawnParticle(Particle.SOUL, bodyLoc, 2, 3, 2, 3, 0.02);
                }

                // Rotate display entity for liveliness
                if (dragonDisplay != null && dragonDisplay.isValid()) {
                    float yaw = (float) Math.sin(hoverPhase * 0.25) * 0.12f;
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

    // ═══════════════════════════════════════════════════════════
    //  MOVEMENT
    // ═══════════════════════════════════════════════════════════

    public void setTarget(Location target) {
        double speed = currentPhase >= 3 ? 0.8 : (currentPhase >= 2 ? 0.7 : 0.6);
        moveToward(target, speed);
    }

    public void moveToward(Location target, double speed) {
        if (baseEntity == null || baseEntity.isDead()) return;
        Location current = baseEntity.getLocation();
        double dist = current.distance(target);
        if (dist < 1.0) return;

        Vector direction = target.toVector().subtract(current.toVector()).normalize().multiply(
                Math.min(speed, dist * 0.5)
        );
        baseEntity.setVelocity(direction);

        // Face movement direction
        Location facing = current.clone();
        facing.setDirection(direction);
        baseEntity.teleport(baseEntity.getLocation().setDirection(direction));
    }

    // ═══════════════════════════════════════════════════════════
    //  VISUAL ATTACKS
    // ═══════════════════════════════════════════════════════════

    public void breathAttackVisual(Location target) {
        if (baseEntity == null || baseEntity.isDead()) return;
        Location from = baseEntity.getLocation().add(0, 3, 0);
        World world = from.getWorld();
        if (world == null) return;

        Vector dir = target.toVector().subtract(from.toVector()).normalize();
        double maxDist = Math.min(from.distance(target), 25);

        for (double d = 0; d < maxDist; d += 0.4) {
            Location point = from.clone().add(dir.clone().multiply(d));
            double spread = d * 0.08;
            world.spawnParticle(Particle.DRAGON_BREATH, point, 2, spread, spread, spread, 0.005);
            world.spawnParticle(Particle.SOUL_FIRE_FLAME, point, 1, spread * 0.5, spread * 0.5, spread * 0.5, 0.005);
            if (d > maxDist * 0.7) {
                world.spawnParticle(Particle.REVERSE_PORTAL, point, 1, spread, spread, spread, 0.02);
            }
        }
        world.playSound(from, Sound.ENTITY_ENDER_DRAGON_SHOOT, SoundCategory.HOSTILE, 1.5f, 0.6f);
    }

    public void wingGustVisual() {
        if (baseEntity == null || baseEntity.isDead()) return;
        Location loc = baseEntity.getLocation().add(0, 2, 0);
        World world = loc.getWorld();
        if (world == null) return;

        for (int angle = 0; angle < 360; angle += 12) {
            double rad = Math.toRadians(angle);
            for (double r = 1.5; r < 10; r += 1.5) {
                double x = r * Math.cos(rad);
                double z = r * Math.sin(rad);
                world.spawnParticle(Particle.CLOUD, loc.clone().add(x, -0.3, z), 1, 0, 0, 0, 0.03);
            }
        }
        world.spawnParticle(Particle.EXPLOSION, loc, 4, 2.5, 0.5, 2.5, 0);
        world.playSound(loc, Sound.ENTITY_ENDER_DRAGON_FLAP, SoundCategory.HOSTILE, 2f, 0.5f);
    }

    // ═══════════════════════════════════════════════════════════
    //  PHASE TRANSITIONS
    // ═══════════════════════════════════════════════════════════

    public void phaseTransition(int bossPhase) {
        setPhase(bossPhase + 1);
    }

    public void setPhase(int phase) {
        this.currentPhase = phase;
        if (baseEntity == null || baseEntity.isDead()) return;

        Location loc = baseEntity.getLocation();
        World world = loc.getWorld();
        if (world == null) return;

        switch (phase) {
            case 2 -> {
                world.playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.HOSTILE, 2.0f, 0.7f);
                world.spawnParticle(Particle.DRAGON_BREATH, loc.clone().add(0, 2, 0), 80, 4, 2, 4, 0.08);
            }
            case 3 -> {
                world.playSound(loc, Sound.ENTITY_WITHER_AMBIENT, SoundCategory.HOSTILE, 1.5f, 0.5f);
                world.spawnParticle(Particle.SOUL_FIRE_FLAME, loc.clone().add(0, 2, 0), 100, 4, 3, 4, 0.1);
                scaleDisplay(RAGE_SCALE);
            }
            case 4 -> {
                world.playSound(loc, Sound.ENTITY_ENDER_DRAGON_DEATH, SoundCategory.HOSTILE, 2.0f, 0.3f);
                world.playSound(loc, Sound.ENTITY_WITHER_SPAWN, SoundCategory.HOSTILE, 1.5f, 0.3f);
                world.spawnParticle(Particle.REVERSE_PORTAL, loc.clone().add(0, 2, 0), 200, 5, 4, 5, 0.2);
                world.spawnParticle(Particle.EXPLOSION, loc, 15, 4, 3, 4, 0.1);
                scaleDisplay(ENRAGE_SCALE);
            }
        }
    }

    private void scaleDisplay(float scale) {
        if (dragonDisplay == null || !dragonDisplay.isValid()) return;
        dragonDisplay.setTransformation(new Transformation(
                new Vector3f(-scale / 2f, -0.5f, -scale / 2f),
                new Quaternionf(),
                new Vector3f(scale, scale, scale),
                new Quaternionf()
        ));
        dragonDisplay.setInterpolationDelay(0);
        dragonDisplay.setInterpolationDuration(10);
    }

    // ═══════════════════════════════════════════════════════════
    //  DAMAGE FLASH
    // ═══════════════════════════════════════════════════════════

    public void damageFlash() {
        flashDamage();
    }

    public void flashDamage() {
        if (dragonDisplay == null || !dragonDisplay.isValid()) return;

        dragonDisplay.setGlowing(true);
        dragonDisplay.setGlowColorOverride(Color.fromRGB(180, 30, 60));

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
            World world = loc.getWorld();
            if (world != null) {
                world.spawnParticle(Particle.DAMAGE_INDICATOR, loc, 10, 2, 1.5, 2, 0.1);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  DEATH ANIMATION
    // ═══════════════════════════════════════════════════════════

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
        if (world == null) { cleanup(); return; }

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
                    Location deathLoc = lastKnownLocation != null ? lastKnownLocation : loc;
                    world.spawnParticle(Particle.EXPLOSION_EMITTER, deathLoc.clone().add(0, 2, 0), 5, 3, 3, 3, 0.1);
                    world.spawnParticle(Particle.DRAGON_BREATH, deathLoc.clone().add(0, 2, 0), 300, 5, 4, 5, 0.15);
                    world.spawnParticle(Particle.END_ROD, deathLoc.clone().add(0, 3, 0), 150, 4, 4, 4, 0.3);
                    world.spawnParticle(Particle.SOUL_FIRE_FLAME, deathLoc.clone().add(0, 2, 0), 100, 4, 3, 4, 0.2);
                    world.playSound(deathLoc, Sound.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 2.5f, 0.4f);

                    cleanup();
                    cancel();
                    return;
                }

                float progress = (float) ticks / DURATION;

                if (dragonDisplay != null && dragonDisplay.isValid()) {
                    float scale = DRAGON_SCALE * (1.0f - progress * 0.85f);
                    Quaternionf spin = new Quaternionf().rotateY(progress * 15.0f);
                    dragonDisplay.setTransformation(new Transformation(
                            new Vector3f(-scale / 2f, -0.5f + progress * -3f, -scale / 2f),
                            spin,
                            new Vector3f(scale, scale, scale),
                            new Quaternionf()
                    ));
                    dragonDisplay.setInterpolationDelay(0);
                    dragonDisplay.setInterpolationDuration(2);

                    dragonDisplay.setGlowing(true);
                    dragonDisplay.setGlowColorOverride(ticks % 6 < 3 ?
                            Color.fromRGB(180, 30, 60) : Color.fromRGB(120, 20, 200));
                }

                Location deathLoc = (baseEntity != null && !baseEntity.isDead()) ?
                        baseEntity.getLocation().add(0, 2, 0) :
                        (lastKnownLocation != null ? lastKnownLocation.clone().add(0, 2, 0) : loc.clone().add(0, 2, 0));

                world.spawnParticle(Particle.DRAGON_BREATH, deathLoc, 8, 2.5, 2, 2.5, 0.05);
                world.spawnParticle(Particle.PORTAL, deathLoc, 15, 3, 2, 3, 1.0);
                world.spawnParticle(Particle.SOUL, deathLoc, 3, 2, 1.5, 2, 0.03);

                if (ticks % 10 == 0) {
                    world.playSound(deathLoc, Sound.ENTITY_ENDER_DRAGON_HURT, SoundCategory.HOSTILE,
                            1.2f, 0.3f + progress * 0.8f);
                }
                if (ticks % 20 == 0) {
                    world.spawnParticle(Particle.EXPLOSION, deathLoc, 2, 2, 1, 2, 0);
                }

                ticks += 2;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    // ═══════════════════════════════════════════════════════════
    //  CLEANUP
    // ═══════════════════════════════════════════════════════════

    public void cleanup() {
        alive = false;

        if (animationTaskId != -1) {
            try { Bukkit.getScheduler().cancelTask(animationTaskId); } catch (Exception ignored) {}
            animationTaskId = -1;
        }

        if (dragonDisplay != null) {
            try { if (dragonDisplay.isValid()) dragonDisplay.remove(); } catch (Exception ignored) {}
            dragonDisplay = null;
        }

        if (baseEntity != null) {
            try { if (baseEntity.isValid()) baseEntity.remove(); } catch (Exception ignored) {}
            baseEntity = null;
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  GETTERS
    // ═══════════════════════════════════════════════════════════

    public boolean isAlive() {
        return alive && baseEntity != null && !baseEntity.isDead();
    }

    public Zombie getBaseEntity() { return baseEntity; }
    public ItemDisplay getDragonDisplay() { return dragonDisplay; }

    public Location getLocation() {
        if (baseEntity != null && !baseEntity.isDead()) {
            return baseEntity.getLocation();
        }
        return lastKnownLocation;
    }

    public double getHealth() {
        if (baseEntity != null && !baseEntity.isDead()) return baseEntity.getHealth();
        return 0;
    }

    public double getMaxHealth() {
        if (baseEntity != null && !baseEntity.isDead()) {
            return Objects.requireNonNull(baseEntity.getAttribute(Attribute.MAX_HEALTH)).getValue();
        }
        return 1500;
    }

    public int getCurrentPhase() { return currentPhase; }
}
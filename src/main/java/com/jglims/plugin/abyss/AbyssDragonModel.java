package com.jglims.plugin.abyss;

import com.jglims.plugin.JGlimsPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.*;

/**
 * AbyssDragonModel v2.0 — Single-model approach
 *
 * Uses ONE ItemDisplay entity on top of an invisible Zombie base.
 * The ItemDisplay shows the full dragon model from the resource pack.
 * Animation: gentle hovering oscillation + rotation toward movement direction.
 *
 * Base entity (Zombie): invisible, handles HP/damage/death, has custom hitbox.
 * Visual (ItemDisplay): shows the dragon model, rides as passenger on the Zombie.
 */
public class AbyssDragonModel {

    private final JGlimsPlugin plugin;
    private Zombie baseEntity;
    private ItemDisplay dragonDisplay;
    private BukkitRunnable animationLoop;
    private int animTick = 0;
    private boolean alive = false;
    private float bodyYaw = 0f;

    // Movement
    private Location currentTarget;
    private double moveSpeed = 0.35;
    private boolean hovering = false;

    public AbyssDragonModel(JGlimsPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Spawns the custom dragon at the given location.
     * @return the base Zombie entity that holds HP and takes damage
     */
    public Zombie spawn(Location location, double maxHealth) {
        World world = location.getWorld();
        if (world == null) return null;

        // 1. Spawn invisible base entity (the hitbox + HP holder)
        baseEntity = world.spawn(location, Zombie.class, z -> {
            z.setInvisible(true);
            z.setSilent(true);
            z.setAI(false);
            z.setGravity(false);
            z.setCollidable(true);
            z.setAdult();
            z.setCanPickupItems(false);
            z.setPersistent(true);
            z.setRemoveWhenFarAway(false);
            z.addScoreboardTag("abyss_dragon_base");
            z.customName(Component.text(
                "\u2620 Abyssal Dragon \u2620",
                NamedTextColor.DARK_PURPLE, TextDecoration.BOLD
            ));
            z.setCustomNameVisible(true);
            // Scale up the hitbox using the scale attribute (1.20.5+)
            try {
                var scaleAttr = z.getAttribute(Attribute.SCALE);
                if (scaleAttr != null) scaleAttr.setBaseValue(4.0); // 4x larger hitbox
            } catch (Exception e) {
                plugin.getLogger().warning("[DragonModel] Could not set scale attribute: " + e.getMessage());
            }
            Objects.requireNonNull(z.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(maxHealth);
            z.setHealth(maxHealth);
            z.getEquipment().clear();
        });

        // 2. Spawn the visual display entity
        dragonDisplay = world.spawn(location, ItemDisplay.class, d -> {
            d.setItemStack(createDragonItem());
            d.setPersistent(false);
            d.addScoreboardTag("abyss_dragon_part");
            d.setBillboard(Display.Billboard.FIXED);
            d.setViewRange(2.0f);
            // Scale the model up to dragon size (4x on each axis)
            d.setTransformationMatrix(
                new Matrix4f().scale(4.0f, 4.0f, 4.0f)
            );
            d.setInterpolationDuration(3);
            d.setInterpolationDelay(0);
        });

        // Mount display on the base entity
        baseEntity.addPassenger(dragonDisplay);

        alive = true;
        bodyYaw = location.getYaw();
        startAnimation();

        plugin.getLogger().info("[DragonModel] Spawned custom Abyssal Dragon at " +
            location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ());
        return baseEntity;
    }

    /**
     * Creates the item that the ItemDisplay shows.
     * Uses PAPER with custom_model_data string "abyss_dragon"
     * which maps to the dragon model in the resource pack.
     */
    private ItemStack createDragonItem() {
        ItemStack item = new ItemStack(Material.PAPER, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Abyssal Dragon Model"));
            meta.setCustomModelData(40000);
            item.setItemMeta(meta);
        }
        return item;
    }

    // ═══════════════════════════════════════════════════════════
    //  ANIMATION
    // ═══════════════════════════════════════════════════════════
    private void startAnimation() {
        animationLoop = new BukkitRunnable() {
            @Override
            public void run() {
                if (!alive || baseEntity == null || baseEntity.isDead() || !baseEntity.isValid()) {
                    cancel();
                    return;
                }
                animTick++;
                animateHover();
                doMovement();
            }
        };
        animationLoop.runTaskTimer(plugin, 1L, 2L);
    }

    private void animateHover() {
        if (dragonDisplay == null || !dragonDisplay.isValid()) return;

        // Gentle up/down oscillation
        float hover = (float)(Math.sin(animTick * 0.08) * 0.3);
        // Slight tilt based on movement
        float tilt = (float)(Math.sin(animTick * 0.05) * 0.05);

        dragonDisplay.setTransformationMatrix(
            new Matrix4f()
                .translate(0f, hover, 0f)
                .rotateY((float) Math.toRadians(-bodyYaw))
                .rotateX(tilt)
                .scale(4.0f, 4.0f, 4.0f)
        );
        dragonDisplay.setInterpolationDelay(0);
        dragonDisplay.setInterpolationDuration(2);
    }

    // ═══════════════════════════════════════════════════════════
    //  MOVEMENT
    // ═══════════════════════════════════════════════════════════
    private void doMovement() {
        if (baseEntity == null || !baseEntity.isValid()) return;

        if (currentTarget != null) {
            Location current = baseEntity.getLocation();
            Vector direction = currentTarget.toVector().subtract(current.toVector());
            double distance = direction.length();

            if (distance < 2.0) {
                hovering = true;
                currentTarget = null;
                baseEntity.setVelocity(new Vector(0, 0, 0));
                return;
            }

            direction.normalize().multiply(moveSpeed);
            if (direction.getY() > 0.5) direction.setY(0.5);
            if (direction.getY() < -0.3) direction.setY(-0.3);

            baseEntity.setVelocity(direction);

            // Smoothly rotate toward target
            float targetYaw = (float) Math.toDegrees(Math.atan2(-direction.getX(), direction.getZ()));
            float yawDiff = targetYaw - bodyYaw;
            while (yawDiff > 180) yawDiff -= 360;
            while (yawDiff < -180) yawDiff += 360;
            bodyYaw += yawDiff * 0.1f;
        } else if (hovering) {
            double hoverY = Math.sin(animTick * 0.05) * 0.02;
            baseEntity.setVelocity(new Vector(0, hoverY, 0));
        }
    }

    public void setTarget(Location target) {
        this.currentTarget = target;
        this.hovering = false;
    }

    public void hover() {
        this.currentTarget = null;
        this.hovering = true;
    }

    public void teleportTo(Location loc) {
        if (baseEntity != null && baseEntity.isValid()) {
            baseEntity.teleport(loc);
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  COMBAT VISUALS
    // ═══════════════════════════════════════════════════════════
    public void breathAttackVisual(Location targetLoc) {
        if (baseEntity == null || !baseEntity.isValid()) return;
        World world = baseEntity.getWorld();
        Location headLoc = baseEntity.getLocation().add(0, 3, 0);

        Vector dir = targetLoc.toVector().subtract(headLoc.toVector()).normalize();
        for (double d = 0; d < headLoc.distance(targetLoc) && d < 30; d += 0.5) {
            Location point = headLoc.clone().add(dir.clone().multiply(d));
            world.spawnParticle(Particle.DRAGON_BREATH, point, 3, 0.2, 0.2, 0.2, 0.01);
            world.spawnParticle(Particle.SOUL_FIRE_FLAME, point, 2, 0.1, 0.1, 0.1, 0.02);
        }
    }

    public void wingGustVisual() {
        if (baseEntity == null || !baseEntity.isValid()) return;
        World world = baseEntity.getWorld();
        Location loc = baseEntity.getLocation();
        world.spawnParticle(Particle.SWEEP_ATTACK, loc.clone().add(0, 2, 0), 30, 5, 2, 5, 0.1);
        world.spawnParticle(Particle.CLOUD, loc.clone().add(0, 2, 0), 50, 6, 1, 6, 0.15);
    }

    public void damageFlash() {
        if (dragonDisplay != null && dragonDisplay.isValid()) {
            dragonDisplay.setGlowColorOverride(Color.RED);
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (dragonDisplay != null && dragonDisplay.isValid()) {
                        dragonDisplay.setGlowColorOverride(null);
                    }
                }
            }.runTaskLater(plugin, 4L);
        }
    }

    public void phaseTransition(int phase) {
        if (baseEntity == null || !baseEntity.isValid()) return;
        World world = baseEntity.getWorld();
        Location loc = baseEntity.getLocation();

        for (int a = 0; a < 360; a += 10) {
            double rad = Math.toRadians(a);
            double px = loc.getX() + 8 * Math.cos(rad);
            double pz = loc.getZ() + 8 * Math.sin(rad);
            world.spawnParticle(Particle.SOUL_FIRE_FLAME,
                new Location(world, px, loc.getY() + 3, pz), 5, 0, 0, 0, 0.05);
        }

        // Scale up slightly per phase
        float scaleBoost = 4.0f + phase * 0.5f;
        if (dragonDisplay != null && dragonDisplay.isValid()) {
            dragonDisplay.setTransformationMatrix(
                new Matrix4f()
                    .rotateY((float) Math.toRadians(-bodyYaw))
                    .scale(scaleBoost, scaleBoost, scaleBoost)
            );
        }

        world.playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 2f, 0.5f + phase * 0.1f);
    }

    // ═══════════════════════════════════════════════════════════
    //  CLEANUP
    // ═══════════════════════════════════════════════════════════
    public void remove() {
        alive = false;
        if (animationLoop != null) try { animationLoop.cancel(); } catch (Exception ignored) {}
        if (dragonDisplay != null && dragonDisplay.isValid()) dragonDisplay.remove();
        if (baseEntity != null && baseEntity.isValid()) baseEntity.remove();
        dragonDisplay = null;
        baseEntity = null;
    }

    public void deathAnimation() {
        if (baseEntity == null) return;
        World world = baseEntity.getWorld();
        Location deathLoc = baseEntity.getLocation();
        alive = false;
        if (animationLoop != null) try { animationLoop.cancel(); } catch (Exception ignored) {}

        final ItemDisplay displayRef = dragonDisplay;
        final Zombie baseRef = baseEntity;

        new BukkitRunnable() {
            int tick = 0;
            @Override
            public void run() {
                tick++;
                if (tick > 40) {
                    if (displayRef != null && displayRef.isValid()) displayRef.remove();
                    if (baseRef != null && baseRef.isValid()) baseRef.remove();
                    cancel();
                    return;
                }
                float progress = tick / 40f;
                // Shrink + spin + fall
                if (displayRef != null && displayRef.isValid()) {
                    float shrink = 4.0f * (1f - progress * 0.9f);
                    float spin = progress * 720f;
                    displayRef.setTransformationMatrix(
                        new Matrix4f()
                            .translate(0f, -progress * 3f, 0f)
                            .rotateY((float) Math.toRadians(spin))
                            .scale(Math.max(0.2f, shrink))
                    );
                    displayRef.setInterpolationDelay(0);
                    displayRef.setInterpolationDuration(1);
                }
                // Particles
                world.spawnParticle(Particle.SOUL_FIRE_FLAME, deathLoc.clone().add(0, 3, 0),
                    20, 5, 3, 5, 0.1);
                world.spawnParticle(Particle.DRAGON_BREATH, deathLoc.clone().add(0, 2, 0),
                    15, 4, 2, 4, 0.05);
                if (tick % 5 == 0) {
                    world.spawnParticle(Particle.EXPLOSION, deathLoc.clone().add(0, 3, 0),
                        3, 3, 2, 3, 0.1);
                    world.playSound(deathLoc, Sound.ENTITY_GENERIC_EXPLODE, 0.7f, 0.8f);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);

        dragonDisplay = null;
        baseEntity = null;
    }

    // ═══════════════════════════════════════════════════════════
    //  GETTERS
    // ═══════════════════════════════════════════════════════════
    public Zombie getBaseEntity() { return baseEntity; }
    public boolean isAlive() {
        return alive && baseEntity != null && baseEntity.isValid() && !baseEntity.isDead();
    }
    public Location getLocation() {
        return baseEntity != null ? baseEntity.getLocation() : null;
    }
    public Location getHeadLocation() {
        if (baseEntity == null) return null;
        return baseEntity.getLocation().add(0, 3.5, 0);
    }
}
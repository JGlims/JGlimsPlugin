package com.jglims.plugin.abyss;

import com.jglims.plugin.JGlimsPlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.entity.EnderDragon;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.List;
import java.util.Objects;

/**
 * AbyssDragonModel v10.0 — Visual model for the custom Abyss Dragon boss.
 * Uses an invisible Zombie (hitbox) + ItemDisplay (visual).
 * v10 fixes: permanent invisibility enforcer, damage flash on display only,
 *            safe particle wrapper, improved death animation.
 */
public class AbyssDragonModel {

    private static final float DRAGON_SCALE = 5.0f;
    private static final float RAGE_SCALE = 5.75f;
    private static final float ENRAGE_SCALE = 6.25f;
    private static final int CUSTOM_MODEL_DATA = 40000;
    private static final String CUSTOM_MODEL_DATA_STRING = "abyss_dragon";

    private final JGlimsPlugin plugin;
    private Zombie baseEntity;
    private ItemDisplay displayEntity;
    private EnderDragon dragonEntity;
    private int animationTaskId = -1;
    private int invisTaskId = -1;
    private int currentPhase = 1;
    private boolean alive = false;

    public AbyssDragonModel(JGlimsPlugin plugin) {
        this.plugin = plugin;
    }

    // Safe particle spawning — wraps all particle calls to prevent crashes
    private void safeParticle(Location loc, Particle particle, int count,
                              double ox, double oy, double oz, double extra) {
        if (loc == null || loc.getWorld() == null) return;
        try {
            particle.builder()
                .location(loc).count(count)
                .offset(ox, oy, oz).extra(extra)
                .receivers(64, true).spawn();
        } catch (Exception e1) {
            try {
                loc.getWorld().spawnParticle(Particle.FLAME, loc, Math.max(1, count / 2),
                    ox, oy, oz, Math.min(extra, 0.05));
            } catch (Exception ignored) {}
        }
    }

    public boolean spawn(Location location, double maxHp) {
        plugin.getLogger().info("[DragonModel] === SPAWN START ===");
        plugin.getLogger().info("[DragonModel] Location: " + location.getBlockX() + "," +
            location.getBlockY() + "," + location.getBlockZ() +
            " World: " + (location.getWorld() != null ? location.getWorld().getName() : "null"));
        plugin.getLogger().info("[DragonModel] Requested HP: " + maxHp);

        if (location.getWorld() == null) {
            plugin.getLogger().severe("[DragonModel] ABORT: World is null!");
            return false;
        }

        double cappedHp = Math.min(maxHp, 2048.0);
        plugin.getLogger().info("[DragonModel] Capped HP: " + cappedHp);

        try {
            // === Spawn the invisible zombie base ===
            baseEntity = location.getWorld().spawn(location, Zombie.class, zombie -> {
                zombie.setInvisible(true);
                zombie.setSilent(true);
                zombie.setAI(false);
                zombie.setGravity(false);
                zombie.setInvulnerable(false);
                zombie.setAdult();
                zombie.setCanPickupItems(false);
                zombie.customName(Component.text("Abyssal Dragon"));
                zombie.setCustomNameVisible(false);
                zombie.setRemoveWhenFarAway(false);
                // Apply permanent invisibility to prevent damage flash revealing zombie
                zombie.addPotionEffect(new PotionEffect(
                    PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false, false));
            });
            plugin.getLogger().info("[DragonModel] Zombie spawned, ID: " + baseEntity.getEntityId());

            // === Set health OUTSIDE the lambda ===
            try {
                Objects.requireNonNull(baseEntity.getAttribute(Attribute.MAX_HEALTH))
                    .setBaseValue(cappedHp);
                baseEntity.setHealth(cappedHp);
                plugin.getLogger().info("[DragonModel] Health set: " + baseEntity.getHealth() +
                    "/" + Objects.requireNonNull(baseEntity.getAttribute(Attribute.MAX_HEALTH)).getBaseValue());
            } catch (Exception e) {
                plugin.getLogger().severe("[DragonModel] Health setup FAILED: " + e.getMessage());
                plugin.getLogger().severe("[DragonModel] Ensure spigot.yml attribute.maxHealth.max >= 2048");
                baseEntity.remove();
                return false;
            }

            // === Set scale for larger hitbox ===
            try {
                var scaleAttr = baseEntity.getAttribute(Attribute.SCALE);
                if (scaleAttr != null) {
                    scaleAttr.setBaseValue(3.0);
                    plugin.getLogger().info("[DragonModel] SCALE set to 3.0");
                }
            } catch (Exception e) {
                plugin.getLogger().info("[DragonModel] SCALE attribute not available: " + e.getMessage());
            }

            // === Create the ItemDisplay with custom model ===
            ItemStack modelItem = new ItemStack(Material.PAPER);
            ItemMeta meta = modelItem.getItemMeta();
            meta.setCustomModelData(CUSTOM_MODEL_DATA);
            try {
                CustomModelDataComponent cmd = meta.getCustomModelDataComponent();
                cmd.setStrings(List.of(CUSTOM_MODEL_DATA_STRING));
                meta.setCustomModelDataComponent(cmd);
                plugin.getLogger().info("[DragonModel] String CMD set: " + CUSTOM_MODEL_DATA_STRING);
            } catch (Exception e) {
                plugin.getLogger().warning("[DragonModel] String CMD failed (non-fatal): " + e.getMessage());
            }
            modelItem.setItemMeta(meta);

            Location displayLoc = location.clone().add(0, 2, 0);
            displayEntity = location.getWorld().spawn(displayLoc, ItemDisplay.class, display -> {
                display.setItemStack(modelItem);
                display.setBillboard(Display.Billboard.FIXED);
                display.setTransformation(new Transformation(
                    new Vector3f(0, 0, 0),
                    new AxisAngle4f(0, 0, 1, 0),
                    new Vector3f(DRAGON_SCALE, DRAGON_SCALE, DRAGON_SCALE),
                    new AxisAngle4f(0, 0, 1, 0)
                ));
                display.setViewRange(2.0f);
            });
            plugin.getLogger().info("[DragonModel] ItemDisplay spawned, ID: " + displayEntity.getEntityId());

            // === Spawn real EnderDragon as visual (no AI, no damage) ===
            try {
                Location dragonSpawnLoc = location.clone().add(0, 3, 0);
                dragonEntity = location.getWorld().spawn(dragonSpawnLoc, EnderDragon.class, dragon -> {
                    dragon.setAI(false);
                    dragon.setSilent(true);
                    dragon.setInvulnerable(true);
                    dragon.setGravity(false);
                    dragon.setPhase(EnderDragon.Phase.HOVERING);
                    dragon.customName(Component.text("Abyssal Dragon", net.kyori.adventure.text.format.NamedTextColor.DARK_PURPLE));
                    dragon.setCustomNameVisible(true);
                    dragon.setRemoveWhenFarAway(false);
                });
                plugin.getLogger().info("[DragonModel] EnderDragon visual spawned, ID: " + dragonEntity.getEntityId());
            } catch (Exception e) {
                plugin.getLogger().warning("[DragonModel] EnderDragon visual spawn failed (non-fatal): " + e.getMessage());
            }

            alive = true;
            startAnimation();
            startInvisibilityEnforcer();

            // Spawn effects
            location.getWorld().playSound(location, Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0f, 0.7f);
            location.getWorld().playSound(location, Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.5f);
            location.getWorld().strikeLightningEffect(location);
            safeParticle(location, Particle.DRAGON_BREATH, 200, 3, 3, 3, 0.1);
            safeParticle(location, Particle.PORTAL, 300, 5, 5, 5, 0.5);

            plugin.getLogger().info("[DragonModel] === SPAWN COMPLETE ===");
            return true;

        } catch (Exception e) {
            plugin.getLogger().severe("[DragonModel] SPAWN FAILED: " + e.getMessage());
            e.printStackTrace();
            cleanup();
            return false;
        }
    }

    public boolean spawn(Location location) {
        return spawn(location, 2000.0);
    }

    // ==================== INVISIBILITY ENFORCER ====================
    // Re-applies invisibility every 5 ticks to ensure the zombie is NEVER visible.
    // This prevents the red damage flash from revealing the zombie.

    private void startInvisibilityEnforcer() {
        invisTaskId = new BukkitRunnable() {
            @Override
            public void run() {
                if (!alive || baseEntity == null || baseEntity.isDead()) {
                    cancel();
                    return;
                }
                baseEntity.setInvisible(true);
                // Re-apply invisibility potion if it was cleared
                if (!baseEntity.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                    baseEntity.addPotionEffect(new PotionEffect(
                        PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false, false));
                }
            }
        }.runTaskTimer(plugin, 5L, 5L).getTaskId();
    }

    // ==================== ANIMATION ====================

    private void startAnimation() {
        animationTaskId = new BukkitRunnable() {
            double bobAngle = 0;
            float rotAngle = 0;

            @Override
            public void run() {
                if (!alive || baseEntity == null || baseEntity.isDead() ||
                    displayEntity == null || displayEntity.isDead()) {
                    cancel();
                    return;
                }

                // Hover bob
                bobAngle += 0.1;
                double bobY = Math.sin(bobAngle) * 0.5;
                Location baseLoc = baseEntity.getLocation();
                displayEntity.teleport(baseLoc.clone().add(0, 2 + bobY, 0));
                // Move the real dragon to follow
                if (dragonEntity != null && !dragonEntity.isDead()) {
                    dragonEntity.teleport(baseLoc.clone().add(0, 3 + bobY, 0));
                }

                // Slow rotation
                rotAngle += 0.02f;
                float scale = currentPhase >= 3 ? ENRAGE_SCALE :
                              currentPhase >= 2 ? RAGE_SCALE : DRAGON_SCALE;
                displayEntity.setTransformation(new Transformation(
                    new Vector3f(0, 0, 0),
                    new AxisAngle4f(rotAngle, 0, 1, 0),
                    new Vector3f(scale, scale, scale),
                    new AxisAngle4f(0, 0, 1, 0)
                ));

                // Ambient particles (reduced for performance)
                Location particleLoc = displayEntity.getLocation();
                safeParticle(particleLoc, Particle.DRAGON_BREATH, 3, 1.5, 1.5, 1.5, 0.01);

                if (currentPhase >= 2) {
                    safeParticle(particleLoc, Particle.SOUL_FIRE_FLAME, 2, 2, 2, 2, 0.02);
                }
                if (currentPhase >= 3) {
                    safeParticle(particleLoc, Particle.PORTAL, 5, 3, 3, 3, 0.5);
                }
            }
        }.runTaskTimer(plugin, 0L, 2L).getTaskId();
    }

    // ==================== MOVEMENT ====================

    public void setTarget(Location target) {
        if (baseEntity != null && !baseEntity.isDead()) {
            moveToward(target, 0.5);
        }
    }

    public void moveToward(Location target, double speed) {
        if (baseEntity == null || baseEntity.isDead()) return;
        Location current = baseEntity.getLocation();
        double dx = target.getX() - current.getX();
        double dy = target.getY() - current.getY();
        double dz = target.getZ() - current.getZ();
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (dist < 1.0) return;

        double factor = speed / dist;
        Location newLoc = current.clone().add(dx * factor, dy * factor, dz * factor);
        newLoc.setYaw((float) Math.toDegrees(Math.atan2(-dx, dz)));
        newLoc.setPitch(0);
        baseEntity.teleport(newLoc);
    }

    // ==================== ATTACK VISUALS ====================

    public void breathAttackVisual(Location target) {
        if (displayEntity == null) return;
        Location start = displayEntity.getLocation();
        World w = start.getWorld();
        if (w == null) return;

        double dx = target.getX() - start.getX();
        double dy = target.getY() - start.getY();
        double dz = target.getZ() - start.getZ();
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

        for (double t = 0; t < dist; t += 0.5) {
            double ratio = t / dist;
            Location point = start.clone().add(dx * ratio, dy * ratio, dz * ratio);
            safeParticle(point, Particle.DRAGON_BREATH, 3, 0.2, 0.2, 0.2, 0.01);
            safeParticle(point, Particle.SOUL_FIRE_FLAME, 2, 0.1, 0.1, 0.1, 0.02);
        }
        w.playSound(start, Sound.ENTITY_ENDER_DRAGON_SHOOT, 1.5f, 0.7f);
    }

    public void wingGustVisual() {
        if (displayEntity == null) return;
        Location loc = displayEntity.getLocation();
        World w = loc.getWorld();
        if (w == null) return;

        safeParticle(loc, Particle.CLOUD, 80, 5, 2, 5, 0.3);
        safeParticle(loc, Particle.SWEEP_ATTACK, 30, 4, 1, 4, 0.1);
        w.playSound(loc, Sound.ENTITY_ENDER_DRAGON_FLAP, 2.0f, 0.5f);
    }

    public void diveBombVisual(Location impactLoc) {
        if (impactLoc == null || impactLoc.getWorld() == null) return;
        World w = impactLoc.getWorld();
        safeParticle(impactLoc, Particle.EXPLOSION_EMITTER, 5, 3, 1, 3, 0.1);
        safeParticle(impactLoc, Particle.SOUL_FIRE_FLAME, 50, 5, 2, 5, 0.15);
        safeParticle(impactLoc, Particle.DRAGON_BREATH, 40, 4, 1, 4, 0.1);
        w.playSound(impactLoc, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.6f);
        w.playSound(impactLoc, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.5f, 1.2f);
    }

    public void shadowBoltVisual(Location from, Location to) {
        if (from == null || to == null || from.getWorld() == null) return;
        double dx = to.getX() - from.getX();
        double dy = to.getY() - from.getY();
        double dz = to.getZ() - from.getZ();
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

        for (double t = 0; t < dist; t += 0.8) {
            double ratio = t / dist;
            Location point = from.clone().add(dx * ratio, dy * ratio, dz * ratio);
            safeParticle(point, Particle.SOUL_FIRE_FLAME, 2, 0.1, 0.1, 0.1, 0.02);
            safeParticle(point, Particle.WITCH, 1, 0.05, 0.05, 0.05, 0);
        }
    }

    // ==================== PHASE TRANSITIONS ====================

    public void setPhase(int phase) {
        if (phase == currentPhase) return;
        currentPhase = phase;
        phaseTransition();
    }

    private void phaseTransition() {
        if (displayEntity == null) return;
        Location loc = displayEntity.getLocation();
        World w = loc.getWorld();
        if (w == null) return;

        w.playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0f, 0.5f + (currentPhase * 0.1f));
        safeParticle(loc, Particle.EXPLOSION, 10, 3, 3, 3, 0.1);
        safeParticle(loc, Particle.PORTAL, 200, 5, 5, 5, 1.0);

        if (currentPhase >= 3) {
            w.playSound(loc, Sound.ENTITY_WITHER_SPAWN, 0.8f, 1.2f);
            safeParticle(loc, Particle.SOUL_FIRE_FLAME, 100, 5, 5, 5, 0.1);
        }
        if (currentPhase >= 4) {
            w.playSound(loc, Sound.ENTITY_WARDEN_ROAR, 1.5f, 0.5f);
            w.strikeLightningEffect(loc);
        }
    }

    // ==================== DAMAGE / DEATH ====================

    public void damageFlash() {
        // Flash the ItemDisplay with brightness instead of making zombie glow
        // This prevents the zombie hitbox from ever becoming visible
        if (displayEntity != null && !displayEntity.isDead()) {
            displayEntity.setBrightness(new Display.Brightness(15, 15));
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (displayEntity != null && !displayEntity.isDead()) {
                        displayEntity.setBrightness(null); // reset to default
                    }
                }
            }.runTaskLater(plugin, 4L);
        }
        // Also spawn a small red particle burst at the model location for feedback
        if (displayEntity != null) {
            safeParticle(displayEntity.getLocation(), Particle.DAMAGE_INDICATOR, 5, 0.5, 0.5, 0.5, 0.1);
        }
    }

    public void playDeathAnimation() {
        alive = false;
        if (animationTaskId != -1) {
            Bukkit.getScheduler().cancelTask(animationTaskId);
            animationTaskId = -1;
        }
        if (invisTaskId != -1) {
            Bukkit.getScheduler().cancelTask(invisTaskId);
            invisTaskId = -1;
        }

        if (displayEntity == null || displayEntity.isDead()) {
            cleanup();
            return;
        }

        // Remove the zombie immediately so it can't be seen
        if (baseEntity != null && !baseEntity.isDead()) {
            baseEntity.remove();
            baseEntity = null;
        }

        Location loc = displayEntity.getLocation();
        World w = loc.getWorld();

        new BukkitRunnable() {
            int tick = 0;
            float scale = currentPhase >= 3 ? ENRAGE_SCALE : DRAGON_SCALE;
            float rot = 0;

            @Override
            public void run() {
                tick++;
                if (displayEntity == null || displayEntity.isDead() || tick > 60) {
                    if (w != null) {
                        safeParticle(loc, Particle.EXPLOSION_EMITTER, 5, 2, 2, 2, 0.1);
                        w.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.5f);
                        w.playSound(loc, Sound.ENTITY_ENDER_DRAGON_DEATH, 2.0f, 1.0f);
                    }
                    cleanup();
                    cancel();
                    return;
                }

                // Shrink and spin
                scale *= 0.97f;
                rot += 0.3f;
                try {
                    displayEntity.setTransformation(new Transformation(
                        new Vector3f(0, (float) (tick * 0.05), 0),
                        new AxisAngle4f(rot, 0, 1, 0),
                        new Vector3f(scale, scale, scale),
                        new AxisAngle4f(0, 0, 1, 0)
                    ));
                } catch (Exception ignored) {}

                if (w != null) {
                    safeParticle(loc, Particle.DRAGON_BREATH, 15, 3, 3, 3, 0.1);
                    safeParticle(loc, Particle.PORTAL, 20, 4, 4, 4, 0.5);
                    safeParticle(loc, Particle.SOUL_FIRE_FLAME, 10, 2, 2, 2, 0.08);
                    if (tick % 5 == 0) {
                        w.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 1.5f);
                    }
                    if (tick % 10 == 0) {
                        w.strikeLightningEffect(loc.clone().add(
                            (Math.random() - 0.5) * 10, 0, (Math.random() - 0.5) * 10));
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    public void cleanup() {
        alive = false;
        if (animationTaskId != -1) {
            Bukkit.getScheduler().cancelTask(animationTaskId);
            animationTaskId = -1;
        }
        if (invisTaskId != -1) {
            Bukkit.getScheduler().cancelTask(invisTaskId);
            invisTaskId = -1;
        }
        if (baseEntity != null && !baseEntity.isDead()) baseEntity.remove();
        if (displayEntity != null && !displayEntity.isDead()) displayEntity.remove();
        baseEntity = null;
        displayEntity = null;
    }

    // ==================== GETTERS ====================

    public double getHealth() {
        return baseEntity != null && !baseEntity.isDead() ? baseEntity.getHealth() : 0;
    }

    public double getMaxHealth() {
        if (baseEntity == null || baseEntity.isDead()) return 0;
        var attr = baseEntity.getAttribute(Attribute.MAX_HEALTH);
        return attr != null ? attr.getBaseValue() : 0;
    }

    public Location getLocation() {
        return baseEntity != null ? baseEntity.getLocation() : null;
    }

    public Location getDisplayLocation() {
        return displayEntity != null ? displayEntity.getLocation() : null;
    }

    public int getPhase() { return currentPhase; }
    public boolean isAlive() { return alive && baseEntity != null && !baseEntity.isDead(); }
    public Zombie getBaseEntity() { return baseEntity; }
    public ItemDisplay getDisplayEntity() { return displayEntity; }
}
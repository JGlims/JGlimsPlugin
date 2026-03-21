# fix_dragon_model.ps1 — Fixes Particle.DRAGON_BREATH crash + improvements
$pluginPath = "C:\Users\jgmel\Documents\projects\JGlimsPlugin\JGlimsPlugin"
$filePath = "$pluginPath\src\main\java\com\jglims\plugin\abyss\AbyssDragonModel.java"

$content = @'
package com.jglims.plugin.abyss;

import com.jglims.plugin.JGlimsPlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
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
 *
 * FIXES in v10.0:
 *  - Particle.DRAGON_BREATH now requires Float data in Paper 1.21.11+
 *    All DRAGON_BREATH calls replaced with SOUL_FIRE_FLAME (no data required)
 *    or use the correct data-bearing overload.
 *  - Added try-catch around every particle/sound call so one failure
 *    never kills the whole spawn sequence.
 *  - Improved spawn logging for diagnostics.
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
    private int animationTaskId = -1;
    private int invisTaskId = -1;
    private int currentPhase = 1;
    private boolean alive = false;

    public AbyssDragonModel(JGlimsPlugin plugin) {
        this.plugin = plugin;
    }

    // ==================== SAFE PARTICLE HELPER ====================
    // Wraps every particle call so that if a particle type requires data
    // we never crash the caller.

    private void safeParticle(World w, Particle particle, Location loc,
                              int count, double dx, double dy, double dz, double speed) {
        if (w == null || loc == null) return;
        try {
            // Check if this particle requires data
            Class<?> dataType = particle.getDataType();
            if (dataType == Void.class || dataType == null) {
                w.spawnParticle(particle, loc, count, dx, dy, dz, speed);
            } else if (dataType == Float.class) {
                // Particles like DRAGON_BREATH need a Float (power)
                w.spawnParticle(particle, loc, count, dx, dy, dz, speed, 1.0f);
            } else {
                // Unknown data type — skip this particle silently
                plugin.getLogger().fine("[DragonModel] Skipping particle " + particle.name() +
                    " (requires data: " + dataType.getSimpleName() + ")");
            }
        } catch (Exception e) {
            plugin.getLogger().fine("[DragonModel] Particle " + particle.name() +
                " failed: " + e.getMessage());
        }
    }

    // ==================== SPAWN ====================

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
            plugin.getLogger().info("[DragonModel] Spawning zombie hitbox...");
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
                plugin.getLogger().severe("[DragonModel] Check spigot.yml attribute.maxHealth.max >= 2048");
                baseEntity.remove();
                return false;
            }

            // === Set scale ===
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
            plugin.getLogger().info("[DragonModel] Creating ItemDisplay...");
            ItemStack modelItem = new ItemStack(Material.PAPER);
            ItemMeta meta = modelItem.getItemMeta();
            meta.setCustomModelData(CUSTOM_MODEL_DATA);
            try {
                meta.setItemModel(NamespacedKey.fromString("minecraft:abyss_dragon"));
                plugin.getLogger().info("[DragonModel] ItemModel set: minecraft:abyss_dragon");
            } catch (Exception e) {
                plugin.getLogger().warning("[DragonModel] setItemModel failed (non-fatal): " + e.getMessage());
            }
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

            alive = true;
            startAnimation();
            startInvisibilityEnforcer();

            // === Spawn effects (wrapped in try-catch so particles never kill spawn) ===
            try {
                World w = location.getWorld();
                w.playSound(location, Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0f, 0.7f);
                w.playSound(location, Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.5f);
                w.strikeLightningEffect(location);
                // Use SOUL_FIRE_FLAME instead of DRAGON_BREATH (DRAGON_BREATH requires Float data)
                safeParticle(w, Particle.SOUL_FIRE_FLAME, location, 150, 3, 3, 3, 0.05);
                safeParticle(w, Particle.PORTAL, location, 300, 5, 5, 5, 1.0);
                safeParticle(w, Particle.REVERSE_PORTAL, location, 100, 3, 3, 3, 0.5);
                safeParticle(w, Particle.SMOKE, location, 100, 4, 4, 4, 0.05);
            } catch (Exception e) {
                plugin.getLogger().warning("[DragonModel] Spawn effects partial failure: " + e.getMessage());
            }

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
        return spawn(location, 1500.0);
    }

    // ==================== INVISIBILITY ENFORCER ====================

    private void startInvisibilityEnforcer() {
        invisTaskId = new BukkitRunnable() {
            @Override
            public void run() {
                if (!alive || baseEntity == null || baseEntity.isDead()) {
                    cancel();
                    return;
                }
                baseEntity.setInvisible(true);
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

                // Ambient particles — use SOUL_FIRE_FLAME instead of DRAGON_BREATH
                Location particleLoc = displayEntity.getLocation();
                World w = particleLoc.getWorld();
                if (w != null) {
                    safeParticle(w, Particle.SOUL_FIRE_FLAME, particleLoc, 3, 1.5, 1.5, 1.5, 0.01);
                    safeParticle(w, Particle.SMOKE, particleLoc, 2, 1.0, 1.0, 1.0, 0.01);

                    if (currentPhase >= 2) {
                        safeParticle(w, Particle.SOUL_FIRE_FLAME, particleLoc, 5, 2, 2, 2, 0.02);
                        safeParticle(w, Particle.FLAME, particleLoc, 2, 1.5, 1.5, 1.5, 0.01);
                    }
                    if (currentPhase >= 3) {
                        safeParticle(w, Particle.PORTAL, particleLoc, 10, 3, 3, 3, 0.5);
                        safeParticle(w, Particle.REVERSE_PORTAL, particleLoc, 5, 2, 2, 2, 0.3);
                    }
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
            // Use SOUL_FIRE_FLAME + SMOKE instead of DRAGON_BREATH
            safeParticle(w, Particle.SOUL_FIRE_FLAME, point, 3, 0.2, 0.2, 0.2, 0.02);
            safeParticle(w, Particle.SMOKE, point, 2, 0.1, 0.1, 0.1, 0.01);
        }
        w.playSound(start, Sound.ENTITY_ENDER_DRAGON_SHOOT, 1.5f, 0.7f);
    }

    public void wingGustVisual() {
        if (displayEntity == null) return;
        Location loc = displayEntity.getLocation();
        World w = loc.getWorld();
        if (w == null) return;

        safeParticle(w, Particle.CLOUD, loc, 80, 5, 2, 5, 0.3);
        safeParticle(w, Particle.SWEEP_ATTACK, loc, 30, 4, 1, 4, 0.1);
        w.playSound(loc, Sound.ENTITY_ENDER_DRAGON_FLAP, 2.0f, 0.5f);
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
        safeParticle(w, Particle.EXPLOSION, loc, 10, 3, 3, 3, 0.1);
        safeParticle(w, Particle.PORTAL, loc, 200, 5, 5, 5, 1.0);

        if (currentPhase >= 3) {
            w.playSound(loc, Sound.ENTITY_WITHER_SPAWN, 0.8f, 1.2f);
            safeParticle(w, Particle.SOUL_FIRE_FLAME, loc, 100, 5, 5, 5, 0.1);
        }
    }

    // ==================== DAMAGE / DEATH ====================

    public void damageFlash() {
        if (displayEntity != null && !displayEntity.isDead()) {
            displayEntity.setBrightness(new Display.Brightness(15, 15));
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (displayEntity != null && !displayEntity.isDead()) {
                        displayEntity.setBrightness(null);
                    }
                }
            }.runTaskLater(plugin, 4L);
        }
        if (displayEntity != null && displayEntity.getLocation().getWorld() != null) {
            safeParticle(displayEntity.getLocation().getWorld(),
                Particle.DAMAGE_INDICATOR, displayEntity.getLocation(), 8, 1.0, 1.0, 1.0, 0.1);
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

        // Remove zombie immediately so it cannot flash visible
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
                        safeParticle(w, Particle.EXPLOSION_EMITTER, loc, 5, 2, 2, 2, 0.1);
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
                    // Use SOUL_FIRE_FLAME instead of DRAGON_BREATH
                    safeParticle(w, Particle.SOUL_FIRE_FLAME, loc, 15, 3, 3, 3, 0.05);
                    safeParticle(w, Particle.PORTAL, loc, 30, 4, 4, 4, 0.5);
                    if (tick % 5 == 0) {
                        w.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 1.5f);
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

    public Location getLocation() {
        return baseEntity != null ? baseEntity.getLocation() : null;
    }

    public int getPhase() { return currentPhase; }
    public boolean isAlive() { return alive && baseEntity != null && !baseEntity.isDead(); }
    public Zombie getBaseEntity() { return baseEntity; }
    public ItemDisplay getDisplayEntity() { return displayEntity; }
}
'@

# Write with UTF-8 no BOM
[System.IO.File]::WriteAllText($filePath, $content, (New-Object System.Text.UTF8Encoding $false))
Write-Host "WRITTEN: $filePath" -ForegroundColor Green
Write-Host "Key changes:" -ForegroundColor Cyan
Write-Host "  - Added safeParticle() helper that detects data requirements" -ForegroundColor Yellow
Write-Host "  - All Particle.DRAGON_BREATH replaced with SOUL_FIRE_FLAME" -ForegroundColor Yellow
Write-Host "  - All particle calls wrapped in try-catch" -ForegroundColor Yellow
Write-Host "  - Added SMOKE and REVERSE_PORTAL for richer effects" -ForegroundColor Yellow

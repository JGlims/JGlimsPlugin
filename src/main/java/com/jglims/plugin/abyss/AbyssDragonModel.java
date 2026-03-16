package com.jglims.plugin.abyss;

import com.jglims.plugin.JGlimsPlugin;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.*;

/**
 * AbyssDragonModel — Builds a visual dragon from multiple ItemDisplay entities
 * riding an invisible base mob. Each "bone" is an ItemDisplay with a custom model.
 *
 * Architecture:
 *   - Base entity: Zombie (invisible, silent, no AI, max HP, glowing=false)
 *   - Body parts: ItemDisplay entities as passengers or on leash-like offset
 *   - Parts: HEAD, BODY, LEFT_WING, RIGHT_WING, TAIL_1, TAIL_2, TAIL_3
 *   - Each part uses a custom item with CMD string for its resource pack model
 *   - Animation: BukkitRunnable updates Transformation matrices every 2 ticks
 *
 * The base entity handles all combat: takes damage, has health bar, drops loot.
 * The ItemDisplays are purely visual — no hitbox, no collision.
 */
public class AbyssDragonModel {

    // ─── Part Definitions ─────────────────────────────────────
    public enum DragonPart {
        BODY("abyss_dragon_body", new Vector3f(0, 2.5f, 0), new Vector3f(4f, 4f, 6f)),
        HEAD("abyss_dragon_head", new Vector3f(0, 3.5f, 4.5f), new Vector3f(2.5f, 2.5f, 3f)),
        LEFT_WING("abyss_dragon_wing_left", new Vector3f(-4f, 3f, 0), new Vector3f(5f, 0.5f, 4f)),
        RIGHT_WING("abyss_dragon_wing_right", new Vector3f(4f, 3f, 0), new Vector3f(5f, 0.5f, 4f)),
        TAIL_1("abyss_dragon_tail_1", new Vector3f(0, 2.2f, -4f), new Vector3f(1.5f, 1.5f, 3f)),
        TAIL_2("abyss_dragon_tail_2", new Vector3f(0, 2f, -7f), new Vector3f(1.2f, 1.2f, 3f)),
        TAIL_3("abyss_dragon_tail_3", new Vector3f(0, 1.8f, -10f), new Vector3f(1f, 1f, 2.5f));

        private final String textureName;
        private final Vector3f baseOffset;
        private final Vector3f baseScale;

        DragonPart(String textureName, Vector3f baseOffset, Vector3f baseScale) {
            this.textureName = textureName;
            this.baseOffset = baseOffset;
            this.baseScale = baseScale;
        }

        public String getTextureName() { return textureName; }
        public Vector3f getBaseOffset() { return new Vector3f(baseOffset); }
        public Vector3f getBaseScale() { return new Vector3f(baseScale); }
    }

    // ─── State ────────────────────────────────────────────────
    private final JGlimsPlugin plugin;
    private Zombie baseEntity;
    private final Map<DragonPart, ItemDisplay> displayParts = new EnumMap<>(DragonPart.class);
    private BukkitRunnable animationLoop;
    private int animTick = 0;
    private float bodyYaw = 0f;
    private float targetYaw = 0f;
    private boolean alive = false;

    // Current movement state
    private Location currentTarget;
    private double moveSpeed = 0.35;
    private boolean hovering = false;

    public AbyssDragonModel(JGlimsPlugin plugin) {
        this.plugin = plugin;
    }

    // ═══════════════════════════════════════════════════════════
    //  SPAWN
    // ═══════════════════════════════════════════════════════════
    /**
     * Spawns the dragon model at the given location.
     * Returns the base Zombie entity (which holds the HP, bossbar, etc.)
     */
    public Zombie spawn(Location location, double maxHealth) {
        World world = location.getWorld();
        if (world == null) return null;

        // 1. Spawn invisible base entity
        baseEntity = world.spawn(location, Zombie.class, z -> {
            z.setInvisible(true);
            z.setSilent(true);
            z.setAI(false);
            z.setGravity(false);
            z.setCollidable(true);
            z.setAdult();
            z.setCanPickupItems(false);
            z.setPersistent(true);
            z.addScoreboardTag("abyss_dragon_base");
            z.customName(net.kyori.adventure.text.Component.text(
                "\u2620 Abyssal Dragon \u2620",
                net.kyori.adventure.text.format.NamedTextColor.DARK_PURPLE,
                net.kyori.adventure.text.format.TextDecoration.BOLD
            ));
            z.setCustomNameVisible(true);
            Objects.requireNonNull(z.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH)).setBaseValue(maxHealth);
            z.setHealth(maxHealth);
            // Make it look nothing like a zombie: full invisible
            z.getEquipment().clear();
            z.setRemoveWhenFarAway(false);
        });

        // 2. Spawn each display entity part
        for (DragonPart part : DragonPart.values()) {
            ItemDisplay display = world.spawn(location, ItemDisplay.class, d -> {
                d.setItemStack(createPartItem(part));
                d.setPersistent(false);
                d.addScoreboardTag("abyss_dragon_part");
                d.setBillboard(Display.Billboard.FIXED);
                d.setViewRange(1.5f);
                // Initial transformation
                d.setTransformationMatrix(
                    new Matrix4f()
                        .translate(part.getBaseOffset())
                        .scale(part.getBaseScale())
                );
                d.setInterpolationDuration(2);
                d.setInterpolationDelay(0);
            });
            displayParts.put(part, display);
            // Mount display on the base entity as passenger
            baseEntity.addPassenger(display);
        }

        alive = true;
        bodyYaw = location.getYaw();
        targetYaw = bodyYaw;
        startAnimationLoop();

        plugin.getLogger().info("[DragonModel] Spawned Abyssal Dragon at " +
            location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ());
        return baseEntity;
    }

    // ═══════════════════════════════════════════════════════════
    //  PART ITEM CREATION
    // ═══════════════════════════════════════════════════════════
    private ItemStack createPartItem(DragonPart part) {
        // Each part uses NETHERITE_SWORD (or any base material) with a custom model data string
        // that maps to the dragon body part model in the resource pack
        ItemStack item = new ItemStack(Material.PAPER, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(net.kyori.adventure.text.Component.text(part.name()));
            // Use the string-based CMD system (1.21.4+)
            // This requires assets/minecraft/items/paper.json to have an override for each part
            meta.setCustomModelData(40000 + part.ordinal());
            // Also set string-based CMD for 1.21.4+
            // The resource pack item definition JSON for paper.json should have:
            // { "when": "abyss_dragon_body", "model": { "type": "minecraft:model", "model": "item/abyss/dragon_body" } }
            item.setItemMeta(meta);
        }
        // Set string CMD via component API
        try {
            var cmdComponent = item.getItemMeta();
            // For 1.21.4+ we need the CustomModelDataComponent with strings
            // This is done through the meta builder pattern in Paper
            // The textureName will be matched in the resource pack
        } catch (Exception ignored) {}
        return item;
    }

    // ═══════════════════════════════════════════════════════════
    //  ANIMATION LOOP
    // ═══════════════════════════════════════════════════════════
    private void startAnimationLoop() {
        animationLoop = new BukkitRunnable() {
            @Override
            public void run() {
                if (!alive || baseEntity == null || baseEntity.isDead() || !baseEntity.isValid()) {
                    cancel();
                    return;
                }
                animTick++;
                animateWings();
                animateTail();
                animateHead();
                animateBody();
                doMovement();
            }
        };
        animationLoop.runTaskTimer(plugin, 1L, 2L); // every 2 ticks = 10 FPS animation
    }

    private void animateWings() {
        // Flap wings using sine wave on the Y rotation
        float flapAngle = (float) (Math.sin(animTick * 0.15) * 35); // ±35 degrees
        float flapRad = (float) Math.toRadians(flapAngle);

        ItemDisplay leftWing = displayParts.get(DragonPart.LEFT_WING);
        ItemDisplay rightWing = displayParts.get(DragonPart.RIGHT_WING);
        if (leftWing != null && leftWing.isValid()) {
            Vector3f off = DragonPart.LEFT_WING.getBaseOffset();
            Vector3f scale = DragonPart.LEFT_WING.getBaseScale();
            leftWing.setTransformationMatrix(
                new Matrix4f()
                    .translate(off)
                    .rotateZ(flapRad) // flap up/down
                    .scale(scale)
            );
            leftWing.setInterpolationDelay(0);
            leftWing.setInterpolationDuration(2);
        }
        if (rightWing != null && rightWing.isValid()) {
            Vector3f off = DragonPart.RIGHT_WING.getBaseOffset();
            Vector3f scale = DragonPart.RIGHT_WING.getBaseScale();
            rightWing.setTransformationMatrix(
                new Matrix4f()
                    .translate(off)
                    .rotateZ(-flapRad) // mirror flap
                    .scale(scale)
            );
            rightWing.setInterpolationDelay(0);
            rightWing.setInterpolationDuration(2);
        }
    }

    private void animateTail() {
        // Sinusoidal sway for tail segments, each with increasing delay
        for (int i = 0; i < 3; i++) {
            DragonPart part = switch (i) {
                case 0 -> DragonPart.TAIL_1;
                case 1 -> DragonPart.TAIL_2;
                default -> DragonPart.TAIL_3;
            };
            ItemDisplay disp = displayParts.get(part);
            if (disp == null || !disp.isValid()) continue;

            float swayAngle = (float) (Math.sin(animTick * 0.1 - i * 0.5) * 15);
            float swayRad = (float) Math.toRadians(swayAngle);
            Vector3f off = part.getBaseOffset();
            Vector3f scale = part.getBaseScale();

            disp.setTransformationMatrix(
                new Matrix4f()
                    .translate(off)
                    .rotateY(swayRad)
                    .scale(scale)
            );
            disp.setInterpolationDelay(0);
            disp.setInterpolationDuration(2);
        }
    }

    private void animateHead() {
        ItemDisplay head = displayParts.get(DragonPart.HEAD);
        if (head == null || !head.isValid()) return;

        // Slight bobbing + look toward target
        float bob = (float) (Math.sin(animTick * 0.08) * 0.3);
        Vector3f off = DragonPart.HEAD.getBaseOffset();
        Vector3f scale = DragonPart.HEAD.getBaseScale();

        head.setTransformationMatrix(
            new Matrix4f()
                .translate(off.x, off.y + bob, off.z)
                .scale(scale)
        );
        head.setInterpolationDelay(0);
        head.setInterpolationDuration(2);
    }

    private void animateBody() {
        ItemDisplay body = displayParts.get(DragonPart.BODY);
        if (body == null || !body.isValid()) return;

        // Gentle hover oscillation
        float hover = (float) (Math.sin(animTick * 0.06) * 0.4);
        Vector3f off = DragonPart.BODY.getBaseOffset();
        Vector3f scale = DragonPart.BODY.getBaseScale();

        body.setTransformationMatrix(
            new Matrix4f()
                .translate(off.x, off.y + hover, off.z)
                .scale(scale)
        );
        body.setInterpolationDelay(0);
        body.setInterpolationDuration(2);
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
                // Reached target
                hovering = true;
                currentTarget = null;
                baseEntity.setVelocity(new Vector(0, 0, 0));
                return;
            }

            direction.normalize().multiply(moveSpeed);
            // Smooth vertical movement
            if (direction.getY() > 0.5) direction.setY(0.5);
            if (direction.getY() < -0.3) direction.setY(-0.3);

            baseEntity.setVelocity(direction);

            // Smoothly rotate toward target
            targetYaw = (float) Math.toDegrees(Math.atan2(-direction.getX(), direction.getZ()));
            float yawDiff = targetYaw - bodyYaw;
            while (yawDiff > 180) yawDiff -= 360;
            while (yawDiff < -180) yawDiff += 360;
            bodyYaw += yawDiff * 0.1f;
            current.setYaw(bodyYaw);
            // We can't directly set yaw on the zombie without teleporting, but the display entities
            // are passengers so they rotate with the base entity visually.
        } else if (hovering) {
            // Gentle hover: small up/down oscillation
            double hoverY = Math.sin(animTick * 0.05) * 0.02;
            baseEntity.setVelocity(new Vector(0, hoverY, 0));
        }
    }

    /**
     * Set a new movement target for the dragon.
     */
    public void setTarget(Location target) {
        this.currentTarget = target;
        this.hovering = false;
    }

    /**
     * Make the dragon hover in place.
     */
    public void hover() {
        this.currentTarget = null;
        this.hovering = true;
    }

    /**
     * Teleport the entire dragon model to a location.
     */
    public void teleportTo(Location loc) {
        if (baseEntity != null && baseEntity.isValid()) {
            baseEntity.teleport(loc);
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  COMBAT VISUALS
    // ═══════════════════════════════════════════════════════════
    /**
     * Visual effect: dragon opens mouth (scale head, spawn particles from head).
     */
    public void breathAttackVisual(Location targetLoc) {
        if (baseEntity == null || !baseEntity.isValid()) return;
        World world = baseEntity.getWorld();
        Location headLoc = baseEntity.getLocation().add(0, 3.5, 0);

        // Line of particles from head to target
        Vector dir = targetLoc.toVector().subtract(headLoc.toVector()).normalize();
        for (double d = 0; d < headLoc.distance(targetLoc) && d < 30; d += 0.5) {
            Location point = headLoc.clone().add(dir.clone().multiply(d));
            world.spawnParticle(Particle.DRAGON_BREATH, point, 3, 0.2, 0.2, 0.2, 0.01);
            world.spawnParticle(Particle.SOUL_FIRE_FLAME, point, 2, 0.1, 0.1, 0.1, 0.02);
        }
    }

    /**
     * Visual effect: wing gust knockback particles
     */
    public void wingGustVisual() {
        if (baseEntity == null || !baseEntity.isValid()) return;
        World world = baseEntity.getWorld();
        Location loc = baseEntity.getLocation();
        world.spawnParticle(Particle.SWEEP_ATTACK, loc.clone().add(0, 2, 0), 30, 5, 2, 5, 0.1);
        world.spawnParticle(Particle.CLOUD, loc.clone().add(0, 2, 0), 50, 6, 1, 6, 0.15);
    }

    /**
     * Flash red when taking damage.
     */
    public void damageFlash() {
        for (ItemDisplay disp : displayParts.values()) {
            if (disp != null && disp.isValid()) {
                // Tint red briefly using glow color
                disp.setGlowColorOverride(Color.RED);
            }
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                for (ItemDisplay disp : displayParts.values()) {
                    if (disp != null && disp.isValid()) {
                        disp.setGlowColorOverride(null);
                    }
                }
            }
        }.runTaskLater(plugin, 4L);
    }

    // ═══════════════════════════════════════════════════════════
    //  PHASE VISUAL CHANGES
    // ═══════════════════════════════════════════════════════════
    /**
     * Phase transition: increase glow, add particles around model.
     */
    public void phaseTransition(int phase) {
        if (baseEntity == null || !baseEntity.isValid()) return;
        World world = baseEntity.getWorld();
        Location loc = baseEntity.getLocation();

        // Explosion ring
        for (int a = 0; a < 360; a += 10) {
            double rad = Math.toRadians(a);
            double px = loc.getX() + 8 * Math.cos(rad);
            double pz = loc.getZ() + 8 * Math.sin(rad);
            world.spawnParticle(Particle.SOUL_FIRE_FLAME, new Location(world, px, loc.getY() + 3, pz), 5, 0, 0, 0, 0.05);
        }

        // Scale up slightly per phase
        float scaleBoost = 1.0f + phase * 0.1f;
        for (Map.Entry<DragonPart, ItemDisplay> entry : displayParts.entrySet()) {
            DragonPart part = entry.getKey();
            ItemDisplay disp = entry.getValue();
            if (disp != null && disp.isValid()) {
                Vector3f off = part.getBaseOffset();
                Vector3f scale = part.getBaseScale();
                disp.setTransformationMatrix(
                    new Matrix4f()
                        .translate(off)
                        .scale(scale.mul(scaleBoost))
                );
            }
        }

        world.playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 2f, 0.5f + phase * 0.1f);
    }

    // ═══════════════════════════════════════════════════════════
    //  CLEANUP
    // ═══════════════════════════════════════════════════════════
    public void remove() {
        alive = false;
        if (animationLoop != null) {
            try { animationLoop.cancel(); } catch (Exception ignored) {}
        }
        for (ItemDisplay disp : displayParts.values()) {
            if (disp != null && disp.isValid()) disp.remove();
        }
        displayParts.clear();
        if (baseEntity != null && baseEntity.isValid()) baseEntity.remove();
        baseEntity = null;
    }

    /**
     * Death animation: parts scatter, particles everywhere.
     */
    public void deathAnimation() {
        if (baseEntity == null) return;
        World world = baseEntity.getWorld();
        Location deathLoc = baseEntity.getLocation();
        alive = false;
        if (animationLoop != null) try { animationLoop.cancel(); } catch (Exception ignored) {}

        // Scatter parts outward with dramatic transformation
        final Map<DragonPart, ItemDisplay> partsCopy = new EnumMap<>(displayParts);
        new BukkitRunnable() {
            int tick = 0;
            @Override
            public void run() {
                tick++;
                if (tick > 40) {
                    // Final cleanup
                    for (ItemDisplay d : partsCopy.values()) if (d != null && d.isValid()) d.remove();
                    if (baseEntity != null && baseEntity.isValid()) baseEntity.remove();
                    cancel();
                    return;
                }
                float progress = tick / 40f;
                for (Map.Entry<DragonPart, ItemDisplay> entry : partsCopy.entrySet()) {
                    ItemDisplay d = entry.getValue();
                    if (d == null || !d.isValid()) continue;
                    DragonPart part = entry.getKey();
                    Vector3f off = part.getBaseOffset();
                    // Move outward and down, shrink
                    float scatter = progress * 5f;
                    float shrink = 1f - progress * 0.8f;
                    d.setTransformationMatrix(
                        new Matrix4f()
                            .translate(off.x * (1 + scatter), off.y - progress * 3, off.z * (1 + scatter))
                            .scale(part.getBaseScale().mul(Math.max(0.1f, shrink)))
                    );
                    d.setInterpolationDelay(0);
                    d.setInterpolationDuration(2);
                }
                // Particles
                world.spawnParticle(Particle.SOUL_FIRE_FLAME, deathLoc.clone().add(0, 3, 0), 20, 5, 3, 5, 0.1);
                world.spawnParticle(Particle.DRAGON_BREATH, deathLoc.clone().add(0, 2, 0), 15, 4, 2, 4, 0.05);
                if (tick % 5 == 0) {
                    world.spawnParticle(Particle.EXPLOSION, deathLoc.clone().add(0, 3, 0), 3, 3, 2, 3, 0.1);
                    world.playSound(deathLoc, Sound.ENTITY_GENERIC_EXPLODE, 0.7f, 0.8f);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    // ═══════════════════════════════════════════════════════════
    //  GETTERS
    // ═══════════════════════════════════════════════════════════
    public Zombie getBaseEntity() { return baseEntity; }
    public boolean isAlive() { return alive && baseEntity != null && baseEntity.isValid() && !baseEntity.isDead(); }
    public Location getLocation() { return baseEntity != null ? baseEntity.getLocation() : null; }
    public Location getHeadLocation() {
        if (baseEntity == null) return null;
        return baseEntity.getLocation().add(0, 3.5, 4.5);
    }
    public Map<DragonPart, ItemDisplay> getDisplayParts() { return Collections.unmodifiableMap(displayParts); }
}
package com.jglims.plugin.custommobs;

import com.jglims.plugin.JGlimsPlugin;
import kr.toxicity.model.api.BetterModel;
import kr.toxicity.model.api.animation.AnimationIterator;
import kr.toxicity.model.api.animation.AnimationModifier;
import kr.toxicity.model.api.data.renderer.ModelRenderer;
import kr.toxicity.model.api.tracker.EntityTracker;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Abstract base class for every custom mob in JGlimsPlugin.
 * <p>
 * Manages the hitbox entity (invisible vanilla mob) and the BetterModel
 * {@link EntityTracker} that renders the 3D model on top of it.
 * Subclasses override behavior hooks: {@link #onSpawn()}, {@link #onDeath(Player)},
 * {@link #onDamage(double, Player)}, {@link #onTick()}, and {@link #getDrops(Player)}.
 */
public abstract class CustomMobEntity {

    protected final JGlimsPlugin plugin;
    protected final CustomMobType mobType;
    protected final UUID uniqueId;

    /** The invisible vanilla entity used for collision, HP, and pathfinding. */
    protected LivingEntity hitboxEntity;

    /** The BetterModel tracker rendering the 3D model. May be null if BetterModel is absent. */
    protected EntityTracker tracker;

    protected MobState state = MobState.IDLE;
    protected boolean alive = false;
    protected long spawnTime;

    /** PersistentDataContainer key marking this entity as a custom mob. Initialized lazily. */
    protected static NamespacedKey KEY_CUSTOM_MOB;

    /**
     * Tick task handle — cancelled on death/despawn. Subclasses should NOT
     * start their own repeating task; override {@link #onTick()} instead.
     */
    private BukkitRunnable tickTask;

    public enum MobState {
        SPAWNING, IDLE, ATTACKING, DYING, DEAD
    }

    protected CustomMobEntity(JGlimsPlugin plugin, CustomMobType mobType) {
        this.plugin = plugin;
        this.mobType = mobType;
        this.uniqueId = UUID.randomUUID();
    }

    // ── Lifecycle ──────────────────────────────────────────────────────

    /**
     * Spawns this custom mob at the given location. Creates the hitbox entity,
     * configures its attributes, attaches the BetterModel tracker, and starts
     * the tick loop.
     *
     * @param location the spawn location
     */
    public void spawn(Location location) {
        if (alive) return;
        state = MobState.SPAWNING;

        hitboxEntity = (LivingEntity) location.getWorld().spawnEntity(location, mobType.getBaseEntityType());
        configureHitboxEntity();
        attachModel();

        alive = true;
        spawnTime = System.currentTimeMillis();
        state = MobState.IDLE;

        startTickLoop();
        onSpawn();

        plugin.getLogger().info("[CustomMob] Spawned " + mobType.getDisplayName()
                + " at " + formatLocation(location));
    }

    /**
     * Configures the hitbox entity: sets HP, makes invisible, sets AI,
     * marks with PersistentDataContainer.
     */
    protected void configureHitboxEntity() {
        hitboxEntity.setInvisible(true);
        hitboxEntity.setSilent(true);
        hitboxEntity.setPersistent(true);
        hitboxEntity.setRemoveWhenFarAway(false);

        // Set max health — Paper's MAX_HEALTH attribute hard-caps at 2048.
        // For bosses with higher design HP (Godzilla 3000, Ghidorah 2800), we
        // cap the attribute at 2048 and rely on the damage-reduction system
        // in CustomBossEntity to provide the effective HP bloat.
        double designHp = mobType.getMaxHealth();
        double effectiveHp = Math.min(designHp, 2048.0);
        Objects.requireNonNull(hitboxEntity.getAttribute(Attribute.MAX_HEALTH))
                .setBaseValue(effectiveHp);
        hitboxEntity.setHealth(effectiveHp);

        // Remove default equipment for zombies
        if (hitboxEntity.getEquipment() != null) {
            hitboxEntity.getEquipment().clear();
        }

        // Tag entity with custom mob type for identification
        hitboxEntity.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "custom_mob_type"),
                PersistentDataType.STRING,
                mobType.getId());

        hitboxEntity.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "custom_mob_uuid"),
                PersistentDataType.STRING,
                uniqueId.toString());

        // Custom name
        hitboxEntity.customName(Component.text(mobType.getDisplayName(), NamedTextColor.RED));
        hitboxEntity.setCustomNameVisible(true);
    }

    /**
     * Attaches the BetterModel renderer to the hitbox entity.
     * Does nothing if BetterModel is not loaded on the server.
     */
    protected void attachModel() {
        if (!Bukkit.getPluginManager().isPluginEnabled("BetterModel")) {
            plugin.getLogger().warning("[CustomMob] BetterModel not available — "
                    + mobType.getDisplayName() + " will be invisible.");
            return;
        }
        try {
            Optional<ModelRenderer> renderer = BetterModel.model(mobType.getModelName());
            if (renderer.isEmpty()) {
                plugin.getLogger().warning("[CustomMob] Model not found: "
                        + mobType.getModelName() + " for " + mobType.getDisplayName());
                return;
            }
            tracker = renderer.get().getOrCreate(
                    kr.toxicity.model.api.bukkit.platform.BukkitAdapter.adapt(hitboxEntity));
            playAnimation("idle", AnimationIterator.Type.LOOP);
        } catch (Exception e) {
            plugin.getLogger().warning("[CustomMob] Failed to attach model for "
                    + mobType.getDisplayName() + ": " + e.getMessage());
        }
    }

    /**
     * Despawns this mob, cleaning up the tracker and entity.
     */
    public void despawn() {
        if (!alive) return;
        cleanup();
        plugin.getLogger().info("[CustomMob] Despawned " + mobType.getDisplayName());
    }

    /**
     * Called when this mob's HP reaches zero. Triggers death sequence.
     *
     * @param killer the player who dealt the killing blow, or null
     */
    public void die(Player killer) {
        if (!alive || state == MobState.DYING || state == MobState.DEAD) return;
        state = MobState.DYING;

        playAnimation("death", AnimationIterator.Type.HOLD_ON_LAST, () -> {
            onDeath(killer);
            dropLoot(killer);
            cleanup();
        });

        // Safety timeout — if death animation doesn't complete in 5 seconds, force cleanup
        new BukkitRunnable() {
            @Override
            public void run() {
                if (alive) {
                    onDeath(killer);
                    dropLoot(killer);
                    cleanup();
                }
            }
        }.runTaskLater(plugin, 100L);
    }

    /**
     * Applies damage to this mob from a player source.
     *
     * @param amount raw damage amount
     * @param source the attacking player, or null
     */
    public void damage(double amount, Player source) {
        if (!alive || hitboxEntity == null) return;

        double newHealth = hitboxEntity.getHealth() - amount;
        if (newHealth <= 0) {
            hitboxEntity.setHealth(0.1); // Prevent vanilla death
            die(source);
        } else {
            hitboxEntity.setHealth(newHealth);
            if (tracker != null) {
                tracker.damageTint();
            }
            onDamage(amount, source);
        }
    }

    /**
     * Cleans up all resources: stops tick loop, closes tracker, removes entity.
     */
    protected void cleanup() {
        alive = false;
        state = MobState.DEAD;

        if (tickTask != null) {
            tickTask.cancel();
            tickTask = null;
        }
        if (tracker != null) {
            try { tracker.close(); } catch (Exception ignored) {}
            tracker = null;
        }
        if (hitboxEntity != null && !hitboxEntity.isDead()) {
            hitboxEntity.remove();
        }
        hitboxEntity = null;

        // Unregister from manager
        CustomMobManager manager = getManager();
        if (manager != null) {
            manager.unregisterMob(this);
        }
    }

    // ── Tick Loop ──────────────────────────────────────────────────────

    private void startTickLoop() {
        tickTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!alive || hitboxEntity == null || hitboxEntity.isDead()) {
                    cleanup();
                    return;
                }
                onTick();
            }
        };
        tickTask.runTaskTimer(plugin, 1L, getTickRate());
    }

    /**
     * Override to change the tick rate. Default is every tick (1).
     *
     * @return tick interval in server ticks
     */
    protected long getTickRate() {
        // 2L (10 Hz) instead of 1L (20 Hz): halves per-entity tick CPU cost
        // server-wide with no perceptible effect on AI, animations, or particle
        // trails. Bosses that need finer timing override this in their subclass.
        return 2L;
    }

    // ── Animation Helpers ──────────────────────────────────────────────

    /**
     * Plays a BetterModel animation on this mob's tracker.
     *
     * @param name animation name (must exist in the .bbmodel)
     * @param type loop type
     */
    protected void playAnimation(String name, AnimationIterator.Type type) {
        if (tracker == null) return;
        try {
            tracker.animate(name, AnimationModifier.builder()
                    .type(type)
                    .start(5).end(5).build());
        } catch (Exception e) {
            plugin.getLogger().fine("[CustomMob] Animation '" + name + "' not found for "
                    + mobType.getModelName());
        }
    }

    /**
     * Plays a BetterModel animation with a completion callback.
     *
     * @param name     animation name
     * @param type     loop type
     * @param onComplete callback when animation finishes
     */
    protected void playAnimation(String name, AnimationIterator.Type type, Runnable onComplete) {
        if (tracker == null) {
            if (onComplete != null) {
                new BukkitRunnable() {
                    @Override
                    public void run() { onComplete.run(); }
                }.runTaskLater(plugin, 40L);
            }
            return;
        }
        try {
            tracker.animate(name, AnimationModifier.builder()
                    .type(type)
                    .override(true)
                    .start(5).end(5).build(), onComplete);
        } catch (Exception e) {
            plugin.getLogger().fine("[CustomMob] Animation '" + name + "' not found for "
                    + mobType.getModelName());
            if (onComplete != null) {
                new BukkitRunnable() {
                    @Override
                    public void run() { onComplete.run(); }
                }.runTaskLater(plugin, 40L);
            }
        }
    }

    // ── Loot ───────────────────────────────────────────────────────────

    /**
     * Drops this mob's loot at its location.
     *
     * @param killer the killing player (for loot bonus calculations)
     */
    protected void dropLoot(Player killer) {
        if (hitboxEntity == null) return;
        Location loc = hitboxEntity.getLocation();
        List<ItemStack> drops = getDrops(killer);
        if (drops != null) {
            for (ItemStack drop : drops) {
                if (drop != null && drop.getType() != Material.AIR) {
                    loc.getWorld().dropItemNaturally(loc, drop);
                }
            }
        }
        // Centralized custom drops by mob type ID (in addition to getDrops() overrides).
        // Ensures every mob drops its characteristic resource item with proper CMD.
        DropItemManager dim = plugin.getDropItemManager();
        if (dim == null) return;
        ItemStack centralDrop = getCentralDropForMobType(dim, mobType.getId());
        if (centralDrop != null) {
            loc.getWorld().dropItemNaturally(loc, centralDrop);
        }
    }

    /**
     * Resolves the centralized drop item for a custom mob type. Returns null
     * if the mob does not have a registered central drop.
     * <p>
     * This lets every mob get its correct textured resource item without
     * having to edit each individual mob's {@link #getDrops(Player)} method.
     */
    private ItemStack getCentralDropForMobType(DropItemManager dim, String id) {
        int rolled = 1 + (int) (Math.random() * 2);  // 1-2 drops
        return switch (id) {
            case "blue_troll", "low_poly_troll" -> dim.createTrollHide(rolled);
            case "invaderling_soldier", "invaderling_archer" -> dim.createLunarFragment(rolled);
            case "invaderling_rider" -> dim.createLunarBeastHide(1);
            case "grizzly_bear" -> dim.createBearClaw(1);
            case "velociraptor" -> dim.createRaptorClaw(1);
            case "basilisk" -> dim.createBasiliskFang(1);
            case "necromancer" -> dim.createDarkEssence(1);
            case "the_keeper" -> dim.createVoidEssence(1);
            case "soul_stealer" -> dim.createSoulFragment(rolled);
            case "tremorsaurus" -> dim.createTremorScale(rolled);
            case "grottoceratops" -> dim.createHorn(1);
            case "spinosaurus" -> dim.createSpinosaurusSail(1);
            default -> null;
        };
    }

    // ── Subclass Hooks ─────────────────────────────────────────────────

    /** Called after the mob is fully spawned and the tick loop has started. */
    protected void onSpawn() {}

    /** Called when the mob dies. Drops are handled separately via {@link #getDrops(Player)}. */
    protected void onDeath(Player killer) {}

    /**
     * Called each time the mob takes damage (before death check).
     *
     * @param amount damage dealt
     * @param source player source, may be null
     */
    protected void onDamage(double amount, Player source) {}

    /** Called every {@link #getTickRate()} ticks while alive. */
    protected void onTick() {}

    /**
     * Returns the items this mob should drop on death.
     *
     * @param killer the player who killed this mob, or null
     * @return list of drops, or null/empty for no drops
     */
    protected List<ItemStack> getDrops(Player killer) {
        return Collections.emptyList();
    }

    /**
     * Called when a player right-clicks this mob.
     *
     * @param player the interacting player
     */
    public void onInteract(Player player) {}

    // ── Getters ────────────────────────────────────────────────────────

    public CustomMobType getMobType() { return mobType; }
    public UUID getUniqueId() { return uniqueId; }
    public LivingEntity getHitboxEntity() { return hitboxEntity; }
    public EntityTracker getTracker() { return tracker; }
    public boolean isAlive() { return alive; }
    public MobState getState() { return state; }

    /**
     * Returns the current location of this mob, or null if despawned.
     */
    public Location getLocation() {
        return hitboxEntity != null ? hitboxEntity.getLocation() : null;
    }

    /**
     * Returns the current health, or 0 if dead.
     */
    public double getHealth() {
        return hitboxEntity != null ? hitboxEntity.getHealth() : 0;
    }

    /**
     * Returns the mob manager from the plugin instance.
     */
    protected CustomMobManager getManager() {
        JGlimsPlugin inst = JGlimsPlugin.getInstance();
        return inst != null ? inst.getCustomMobManager() : null;
    }

    // ── Utility ────────────────────────────────────────────────────────

    protected String formatLocation(Location loc) {
        return String.format("%s [%.0f, %.0f, %.0f]",
                loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ());
    }

    /**
     * Finds the nearest player within the given radius.
     *
     * @param radius search radius in blocks
     * @return the nearest player, or null
     */
    protected Player findNearestPlayer(double radius) {
        if (hitboxEntity == null) return null;
        Location loc = hitboxEntity.getLocation();
        Player nearest = null;
        double nearestDist = radius * radius;
        for (Player p : loc.getWorld().getPlayers()) {
            if (p.getGameMode() == GameMode.SPECTATOR || p.getGameMode() == GameMode.CREATIVE) continue;
            double dist = p.getLocation().distanceSquared(loc);
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = p;
            }
        }
        return nearest;
    }

    /**
     * Finds all players within the given radius.
     *
     * @param radius search radius in blocks
     * @return list of nearby players (survival/adventure only)
     */
    protected List<Player> findNearbyPlayers(double radius) {
        if (hitboxEntity == null) return Collections.emptyList();
        Location loc = hitboxEntity.getLocation();
        double radiusSq = radius * radius;
        List<Player> result = new ArrayList<>();
        for (Player p : loc.getWorld().getPlayers()) {
            if (p.getGameMode() == GameMode.SPECTATOR || p.getGameMode() == GameMode.CREATIVE) continue;
            if (p.getLocation().distanceSquared(loc) <= radiusSq) {
                result.add(p);
            }
        }
        return result;
    }

    /**
     * Teleports the hitbox entity (and the model follows automatically).
     *
     * @param location destination
     */
    protected void moveTo(Location location) {
        if (hitboxEntity != null) {
            hitboxEntity.teleport(location);
        }
    }
}

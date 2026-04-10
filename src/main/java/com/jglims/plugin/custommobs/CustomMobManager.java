package com.jglims.plugin.custommobs;

import com.jglims.plugin.JGlimsPlugin;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central manager for all custom mobs in JGlimsPlugin. Handles registration,
 * spawning, despawning, and tracking of all active custom mob instances.
 * <p>
 * Other systems (structures, events, commands) use this manager to spawn
 * custom mobs at specific locations. The manager maintains a live registry
 * of all active mobs, keyed by their unique ID and by the hitbox entity's UUID.
 */
public class CustomMobManager {

    private final JGlimsPlugin plugin;

    /** All active custom mobs, keyed by their unique ID. */
    private final Map<UUID, CustomMobEntity> activeMobs = new ConcurrentHashMap<>();

    /** Quick lookup from Bukkit entity UUID to custom mob instance. */
    private final Map<UUID, CustomMobEntity> entityToMob = new ConcurrentHashMap<>();

    /** NamespacedKey for identifying custom mob entities via PersistentDataContainer. */
    private final NamespacedKey keyCustomMobType;
    private final NamespacedKey keyCustomMobUUID;

    public CustomMobManager(JGlimsPlugin plugin) {
        this.plugin = plugin;
        this.keyCustomMobType = new NamespacedKey(plugin, "custom_mob_type");
        this.keyCustomMobUUID = new NamespacedKey(plugin, "custom_mob_uuid");
    }

    // ── Registration ───────────────────────────────────────────────────

    /**
     * Registers an active custom mob in the tracking maps.
     * Called automatically by {@link CustomMobEntity#spawn(Location)}.
     *
     * @param mob the custom mob to register
     */
    public void registerMob(CustomMobEntity mob) {
        activeMobs.put(mob.getUniqueId(), mob);
        if (mob.getHitboxEntity() != null) {
            entityToMob.put(mob.getHitboxEntity().getUniqueId(), mob);
        }
    }

    /**
     * Unregisters a custom mob from the tracking maps.
     * Called automatically by {@link CustomMobEntity#cleanup()}.
     *
     * @param mob the custom mob to unregister
     */
    public void unregisterMob(CustomMobEntity mob) {
        activeMobs.remove(mob.getUniqueId());
        if (mob.getHitboxEntity() != null) {
            entityToMob.remove(mob.getHitboxEntity().getUniqueId());
        }
    }

    // ── Spawning ───────────────────────────────────────────────────────

    /**
     * Spawns a custom mob of the given type at the given location.
     * Uses the mob factory to create the correct subclass instance.
     *
     * @param type     the custom mob type to spawn
     * @param location the spawn location
     * @return the spawned mob instance, or null if creation failed
     */
    public CustomMobEntity spawnMob(CustomMobType type, Location location) {
        CustomMobEntity mob = CustomMobFactory.create(plugin, type);
        if (mob == null) {
            plugin.getLogger().warning("[CustomMobManager] No implementation for mob type: " + type.getId());
            return null;
        }
        mob.spawn(location);
        registerMob(mob);
        return mob;
    }

    /**
     * Despawns all active custom mobs. Called on plugin disable.
     */
    public void despawnAll() {
        for (CustomMobEntity mob : new ArrayList<>(activeMobs.values())) {
            mob.despawn();
        }
        activeMobs.clear();
        entityToMob.clear();
    }

    /**
     * Despawns all active custom mobs in the given world.
     *
     * @param worldName the world name
     */
    public void despawnInWorld(String worldName) {
        for (CustomMobEntity mob : new ArrayList<>(activeMobs.values())) {
            Location loc = mob.getLocation();
            if (loc != null && loc.getWorld().getName().equals(worldName)) {
                mob.despawn();
            }
        }
    }

    // ── Lookup ─────────────────────────────────────────────────────────

    /**
     * Finds the custom mob instance associated with a Bukkit entity.
     *
     * @param entity the Bukkit entity (typically a hitbox entity)
     * @return the custom mob, or null if not a custom mob
     */
    public CustomMobEntity getByEntity(Entity entity) {
        return entityToMob.get(entity.getUniqueId());
    }

    /**
     * Finds a custom mob by its unique ID.
     *
     * @param uniqueId the custom mob's UUID
     * @return the mob, or null
     */
    public CustomMobEntity getById(UUID uniqueId) {
        return activeMobs.get(uniqueId);
    }

    /**
     * Checks if a Bukkit entity is a custom mob hitbox.
     *
     * @param entity the entity to check
     * @return true if the entity belongs to a custom mob
     */
    public boolean isCustomMob(Entity entity) {
        if (entityToMob.containsKey(entity.getUniqueId())) return true;
        // Fallback: check PersistentDataContainer
        if (entity instanceof LivingEntity living) {
            return living.getPersistentDataContainer().has(keyCustomMobType, PersistentDataType.STRING);
        }
        return false;
    }

    /**
     * Returns the CustomMobType of a Bukkit entity, or null if it's not a custom mob.
     *
     * @param entity the entity to identify
     * @return the mob type, or null
     */
    public CustomMobType identifyEntity(Entity entity) {
        CustomMobEntity mob = entityToMob.get(entity.getUniqueId());
        if (mob != null) return mob.getMobType();
        // Fallback: check PersistentDataContainer
        if (entity instanceof LivingEntity living) {
            String typeId = living.getPersistentDataContainer().get(keyCustomMobType, PersistentDataType.STRING);
            if (typeId != null) return CustomMobType.fromId(typeId);
        }
        return null;
    }

    /**
     * Returns all active custom mobs.
     */
    public Collection<CustomMobEntity> getActiveMobs() {
        return Collections.unmodifiableCollection(activeMobs.values());
    }

    /**
     * Returns all active custom mobs of the given type.
     *
     * @param type the mob type to filter by
     */
    public List<CustomMobEntity> getActiveMobsOfType(CustomMobType type) {
        List<CustomMobEntity> result = new ArrayList<>();
        for (CustomMobEntity mob : activeMobs.values()) {
            if (mob.getMobType() == type) result.add(mob);
        }
        return result;
    }

    /**
     * Returns the count of active custom mobs.
     */
    public int getActiveMobCount() {
        return activeMobs.size();
    }

    /**
     * Returns the count of active custom mobs of the given type.
     */
    public int getActiveMobCount(CustomMobType type) {
        int count = 0;
        for (CustomMobEntity mob : activeMobs.values()) {
            if (mob.getMobType() == type) count++;
        }
        return count;
    }
}

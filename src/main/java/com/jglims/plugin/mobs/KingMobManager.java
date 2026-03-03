package com.jglims.plugin.mobs;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.config.ConfigManager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class KingMobManager implements Listener {

    private final JGlimsPlugin plugin;
    private final ConfigManager config;

    // Spawn counter per mob type
    private final Map<EntityType, Integer> spawnCounters = new HashMap<>();

    // Currently alive King (UUID and type)
    private UUID currentKingUUID = null;
    private EntityType currentKingType = null;

    // PDC tag is not needed for King mobs — we track by UUID in memory

    public KingMobManager(JGlimsPlugin plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
    }

    public boolean isKingMob(Entity entity) {
        return entity != null && entity.getUniqueId().equals(currentKingUUID);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!config.isKingMobEnabled()) return;

        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Monster)) return;

        World world = entity.getWorld();
        if (world.getEnvironment() != World.Environment.NORMAL) return;

        // Skip bosses
        if (entity instanceof EnderDragon || entity instanceof Wither
            || entity instanceof Warden || entity instanceof ElderGuardian) return;

        EntityType type = entity.getType();
        int count = spawnCounters.getOrDefault(type, 0) + 1;

        if (count >= config.getSpawnsPerKing()) {
            // Check if a King already exists
            if (currentKingUUID != null) {
                Entity existing = plugin.getServer().getEntity(currentKingUUID);
                if (existing != null && !existing.isDead() && existing.isValid()) {
                    // A King is still alive — just increment counter and don't spawn another
                    spawnCounters.put(type, count);
                    return;
                }
                // King is dead/gone, clear tracking
                currentKingUUID = null;
                currentKingType = null;
            }

            // Transform this mob into a King
            makeKing(entity);
            spawnCounters.put(type, 0); // Reset counter
        } else {
            spawnCounters.put(type, count);
        }
    }

    private void makeKing(LivingEntity entity) {
        // Apply King multipliers
        AttributeInstance maxHealth = entity.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealth != null) {
            double newHealth = maxHealth.getBaseValue() * config.getKingHealthMult();
            maxHealth.setBaseValue(newHealth);
            entity.setHealth(newHealth);
        }

        AttributeInstance attackDamage = entity.getAttribute(Attribute.ATTACK_DAMAGE);
        if (attackDamage != null) {
            attackDamage.setBaseValue(attackDamage.getBaseValue() * config.getKingDamageMult());
        }

        // Custom name
        String mobName = formatEntityType(entity.getType());
        entity.customName(Component.text("King " + mobName, NamedTextColor.GOLD)
            .decoration(TextDecoration.BOLD, true));
        entity.setCustomNameVisible(true);

        // Glowing effect
        entity.setGlowing(true);

        // Prevent natural despawn
        entity.setRemoveWhenFarAway(false);

        // Track
        currentKingUUID = entity.getUniqueId();
        currentKingType = entity.getType();

        plugin.getLogger().info("King " + mobName + " has spawned at " +
            entity.getLocation().getBlockX() + ", " +
            entity.getLocation().getBlockY() + ", " +
            entity.getLocation().getBlockZ());
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();

        if (!entity.getUniqueId().equals(currentKingUUID)) return;

        // King mob died — drop diamonds
        int diamonds = ThreadLocalRandom.current().nextInt(
            config.getKingDiamondMin(), config.getKingDiamondMax() + 1);
        event.getDrops().add(new ItemStack(Material.DIAMOND, diamonds));

        // Clear tracking
        String mobName = formatEntityType(currentKingType);
        plugin.getLogger().info("King " + mobName + " has been slain!");

        currentKingUUID = null;
        currentKingType = null;
    }

    private String formatEntityType(EntityType type) {
        String name = type.name().replace('_', ' ');
        StringBuilder sb = new StringBuilder();
        boolean cap = true;
        for (char c : name.toCharArray()) {
            if (c == ' ') { sb.append(' '); cap = true; }
            else if (cap) { sb.append(Character.toUpperCase(c)); cap = false; }
            else sb.append(Character.toLowerCase(c));
        }
        return sb.toString();
    }

    /**
     * Checks if an entity type is a "special mob" for the enchanted book drop system.
     */
    public static boolean isSpecialMob(Entity entity) {
        return entity instanceof IronGolem || entity instanceof ElderGuardian
            || entity instanceof EnderDragon || entity instanceof Warden
            || entity instanceof Wither || entity instanceof WitherSkeleton;
    }
}

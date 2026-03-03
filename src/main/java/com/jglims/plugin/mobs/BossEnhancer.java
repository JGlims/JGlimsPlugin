package com.jglims.plugin.mobs;

import com.jglims.plugin.config.ConfigManager;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class BossEnhancer implements Listener {

    private final ConfigManager config;
    private final JavaPlugin plugin;

    // Constructor matching what your JGlimsPlugin.java passes: (plugin, configManager, listener)
    // We accept plugin + config and ignore the third arg if present
    public BossEnhancer(JavaPlugin plugin, ConfigManager config, Listener... extra) {
        this.plugin = plugin;
        this.config = config;
    }

    // Also support simple (ConfigManager) constructor
    public BossEnhancer(ConfigManager config) {
        this.plugin = null;
        this.config = config;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBossSpawn(CreatureSpawnEvent event) {
        LivingEntity entity = event.getEntity();

        double healthMult = 1.0;
        double damageMult = 1.0;

        if (entity instanceof EnderDragon) {
            healthMult = config.getEnderDragonHealthMult();
            damageMult = config.getEnderDragonDamageMult();
        } else if (entity instanceof Wither) {
            healthMult = config.getWitherHealthMult();
            damageMult = config.getWitherDamageMult();
        } else if (entity instanceof Warden) {
            healthMult = config.getWardenHealthMult();
            damageMult = config.getWardenDamageMult();
        } else if (entity instanceof ElderGuardian) {
            healthMult = config.getElderGuardianHealthMult();
            damageMult = config.getElderGuardianDamageMult();
        } else {
            return;
        }

        if (healthMult == 1.0 && damageMult == 1.0) {
            return;
        }

        if (healthMult != 1.0) {
            AttributeInstance healthAttr = entity.getAttribute(Attribute.MAX_HEALTH);
            if (healthAttr != null) {
                double newMaxHealth = healthAttr.getBaseValue() * healthMult;
                healthAttr.setBaseValue(newMaxHealth);
                entity.setHealth(newMaxHealth);
            }
        }

        if (damageMult != 1.0) {
            NamespacedKey key = NamespacedKey.fromString("jglims:boss_damage_mult");
            entity.getPersistentDataContainer().set(key, PersistentDataType.DOUBLE, damageMult);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBossDamage(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        if (!(damager instanceof LivingEntity livingDamager)) return;

        NamespacedKey key = NamespacedKey.fromString("jglims:boss_damage_mult");
        if (livingDamager.getPersistentDataContainer().has(key, PersistentDataType.DOUBLE)) {
            double mult = livingDamager.getPersistentDataContainer().get(key, PersistentDataType.DOUBLE);
            event.setDamage(event.getDamage() * mult);
        }
    }
}

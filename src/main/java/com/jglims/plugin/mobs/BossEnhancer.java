package com.jglims.plugin.mobs;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.config.ConfigManager;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class BossEnhancer implements Listener {

    private final JGlimsPlugin plugin;
    private final ConfigManager config;

    public BossEnhancer(JGlimsPlugin plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBossSpawn(CreatureSpawnEvent event) {
        if (!config.isMobDifficultyEnabled()) return;

        LivingEntity entity = event.getEntity();
        double healthMult = 1.0;
        double damageMult = 1.0;

        if (entity instanceof EnderDragon) {
            healthMult = config.getBaselineHealth() * 3.5;
            damageMult = config.getBaselineDamage() * 3.0;
        } else if (entity instanceof Wither) {
            healthMult = config.getBaselineHealth() * 2.5;
            damageMult = config.getBaselineDamage() * 2.2;
        } else if (entity instanceof Warden) {
            healthMult = config.getBaselineHealth() * 2.0;
            damageMult = config.getBaselineDamage() * 2.5;
        } else if (entity instanceof ElderGuardian) {
            healthMult = config.getBaselineHealth() * 2.5;
            damageMult = config.getBaselineDamage() * 1.8;
        } else {
            return;
        }

        AttributeInstance maxHealth = entity.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealth != null) {
            double newHealth = maxHealth.getBaseValue() * healthMult;
            maxHealth.setBaseValue(newHealth);
            entity.setHealth(newHealth);
        }

        AttributeInstance attackDamage = entity.getAttribute(Attribute.ATTACK_DAMAGE);
        if (attackDamage != null) {
            attackDamage.setBaseValue(attackDamage.getBaseValue() * damageMult);
        }
    }
}

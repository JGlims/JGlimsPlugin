package com.jglims.plugin.mobs;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.config.ConfigManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Biome;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class MobDifficultyManager implements Listener {

    private final JGlimsPlugin plugin;
    private final ConfigManager config;

    public MobDifficultyManager(JGlimsPlugin plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
        BiomeMultipliers.init();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        LivingEntity entity = event.getEntity();

        // Only modify hostile mobs
        if (!(entity instanceof Monster) && !(entity instanceof Slime)
            && !(entity instanceof Phantom) && !(entity instanceof Ghast)
            && !(entity instanceof Shulker) && !(entity instanceof Hoglin)) {
            return;
        }

        // Creeper spawn reduction
        if (entity instanceof Creeper && config.isCreeperReductionEnabled()) {
            if (Math.random() < config.getCreeperCancelChance()) {
                event.setCancelled(true);
                return;
            }
        }

        if (!config.isMobDifficultyEnabled()) return;

        // Skip bosses - handled by BossEnhancer
        if (entity instanceof EnderDragon || entity instanceof Wither
            || entity instanceof Warden || entity instanceof ElderGuardian) {
            return;
        }

        Location loc = entity.getLocation();
        World world = loc.getWorld();
        if (world == null) return;

        double healthMult = config.getBaselineHealth();
        double damageMult = config.getBaselineDamage();
        double speedMult = 1.0;

        World.Environment env = world.getEnvironment();

        if (env == World.Environment.NORMAL) {
            // Distance scaling
            double dist = Math.sqrt(loc.getX() * loc.getX() + loc.getZ() * loc.getZ());
            double[] distMults = getDistanceMultipliers(dist);
            healthMult *= distMults[0];
            damageMult *= distMults[1];
            speedMult = distMults[2];

            // Biome scaling
            Biome biome = world.getBiome(loc);
            double[] biomeMults = BiomeMultipliers.getOverworldMultiplier(biome);
            healthMult *= biomeMults[0];
            damageMult *= biomeMults[1];

        } else if (env == World.Environment.NETHER) {
            Biome biome = world.getBiome(loc);
            double[] biomeMults = BiomeMultipliers.getNetherMultiplier(biome);
            healthMult *= biomeMults[0];
            damageMult *= biomeMults[1];

        } else if (env == World.Environment.THE_END) {
            healthMult *= 2.5;
            damageMult *= 2.0;
        }

        // Apply health
        AttributeInstance maxHealth = entity.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealth != null) {
            double newHealth = maxHealth.getBaseValue() * healthMult;
            maxHealth.setBaseValue(newHealth);
            entity.setHealth(newHealth);
        }

        // Apply damage
        AttributeInstance attackDamage = entity.getAttribute(Attribute.ATTACK_DAMAGE);
        if (attackDamage != null) {
            attackDamage.setBaseValue(attackDamage.getBaseValue() * damageMult);
        }

        // Apply speed
        if (speedMult > 1.0) {
            AttributeInstance speed = entity.getAttribute(Attribute.MOVEMENT_SPEED);
            if (speed != null) {
                speed.setBaseValue(speed.getBaseValue() * speedMult);
            }
        }
    }

        private double[] getDistanceMultipliers(double distance) {
        if (distance < 350)  return new double[]{1.0, 1.0, 1.0};
        if (distance < 700)  return new double[]{config.getDist350Health(), config.getDist350Damage(), 1.0};
        if (distance < 1000) return new double[]{config.getDist700Health(), config.getDist700Damage(), 1.05};
        if (distance < 2000) return new double[]{config.getDist1000Health(), config.getDist1000Damage(), 1.1};
        if (distance < 3000) return new double[]{config.getDist2000Health(), config.getDist2000Damage(), 1.15};
        if (distance < 5000) return new double[]{config.getDist3000Health(), config.getDist3000Damage(), 1.2};
        return new double[]{config.getDist5000Health(), config.getDist5000Damage(), 1.25};
    }

}

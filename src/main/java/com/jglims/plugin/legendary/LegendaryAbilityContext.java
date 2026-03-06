package com.jglims.plugin.legendary;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.config.ConfigManager;
import com.jglims.plugin.guilds.GuildManager;

/**
 * LegendaryAbilityContext - shared state and utility methods for legendary abilities.
 * Phase 8d - rule A.17 file-size split.
 */
final class LegendaryAbilityContext {

    final JGlimsPlugin plugin;
    final ConfigManager config;
    final LegendaryWeaponManager weaponManager;
    final GuildManager guildManager;

    final Map<UUID, Integer> bloodlustStacks = new HashMap<>();
    final Map<UUID, Long> bloodlustExpiry = new HashMap<>();
    final Map<UUID, Double> retributionDamageStored = new HashMap<>();
    final Map<UUID, Long> retributionExpiry = new HashMap<>();
    final Map<UUID, Integer> soulCount = new HashMap<>();
    final Map<UUID, UUID> grudgeTargets = new HashMap<>();
    final Map<UUID, Long> grudgeExpiry = new HashMap<>();
    final Map<UUID, Boolean> rebornReady = new HashMap<>();
    final Map<UUID, Long> rebornExpiry = new HashMap<>();
    final Map<UUID, Boolean> phaseShiftActive = new HashMap<>();
    final Map<UUID, Boolean> undyingRageActive = new HashMap<>();
    final Map<UUID, Long> undyingRageExpiry = new HashMap<>();
    final Map<UUID, Boolean> crescentParryActive = new HashMap<>();
    final Map<UUID, Boolean> emberShieldActive = new HashMap<>();
    final Map<UUID, Long> emberShieldExpiry = new HashMap<>();
    final Map<UUID, Boolean> thunderShieldActive = new HashMap<>();
    final Map<UUID, Long> thunderShieldExpiry = new HashMap<>();
    final Map<UUID, UUID> predatorMarkTarget = new HashMap<>();
    final Map<UUID, Long> predatorMarkExpiry = new HashMap<>();
    final Map<UUID, Integer> gemBarrierCharges = new HashMap<>();
    final Map<UUID, Long> gemBarrierExpiry = new HashMap<>();
    final Map<UUID, Boolean> shadowStanceActive = new HashMap<>();
    final Map<UUID, Long> shadowStanceExpiry = new HashMap<>();

    LegendaryAbilityContext(JGlimsPlugin plugin, ConfigManager config,
                            LegendaryWeaponManager weaponManager, GuildManager guildManager) {
        this.plugin = plugin;
        this.config = config;
        this.weaponManager = weaponManager;
        this.guildManager = guildManager;
    }

    List<LivingEntity> getNearbyEnemies(Location center, double radius, Player exclude) {
        List<LivingEntity> enemies = new ArrayList<>();
        for (Entity e : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
            if (!(e instanceof LivingEntity le) || e instanceof ArmorStand || e == exclude) continue;
            if (le.getLocation().distanceSquared(center) > radius * radius) continue;
            if (le instanceof Player target) {
                if (target.getGameMode() == GameMode.CREATIVE || target.getGameMode() == GameMode.SPECTATOR) continue;
                if (exclude != null && guildManager.areInSameGuild(exclude.getUniqueId(), target.getUniqueId())) continue;
            }
            enemies.add(le);
        }
        return enemies;
    }

    void dealDamage(Player attacker, LivingEntity target, double damage) {
        target.damage(damage, attacker);
    }

    LivingEntity getTargetEntity(Player player, double range) {
        Vector dir = player.getLocation().getDirection().normalize();
        Location start = player.getEyeLocation();
        LivingEntity closest = null;
        double closestDist = Double.MAX_VALUE;
        for (Entity e : player.getWorld().getNearbyEntities(start, range, range, range)) {
            if (!(e instanceof LivingEntity le) || e instanceof ArmorStand || e == player) continue;
            if (le instanceof Player tp) {
                if (tp.getGameMode() == GameMode.CREATIVE || tp.getGameMode() == GameMode.SPECTATOR) continue;
                if (guildManager.areInSameGuild(player.getUniqueId(), tp.getUniqueId())) continue;
            }
            Vector toEntity = le.getLocation().add(0, 1, 0).toVector().subtract(start.toVector());
            double dist = toEntity.length();
            if (dist > range) continue;
            double dot = toEntity.normalize().dot(dir);
            if (dot < 0.9) continue;
            if (dist < closestDist) { closestDist = dist; closest = le; }
        }
        return closest;
    }

    Vector rotateY(Vector v, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double x = v.getX() * cos - v.getZ() * sin;
        double z = v.getX() * sin + v.getZ() * cos;
        return v.setX(x).setZ(z);
    }
}
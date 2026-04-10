package com.jglims.plugin.custommobs;

import com.jglims.plugin.JGlimsPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Abstract base for world bosses — powerful bosses that spawn naturally in the
 * world based on biome, structure, or dimension rules. Adds natural spawn logic,
 * despawn timers, distance-based despawn, and player announcements.
 * <p>
 * Subclasses must implement {@link #combatTick()} and {@link #getDrops(Player)}.
 */
public abstract class CustomWorldBoss extends CustomBossEntity {

    /** How long (in ticks) before the boss despawns if not in combat. */
    protected long despawnTimerTicks = 12000L; // 10 minutes

    /** Ticks spent out of combat since spawn. */
    protected long idleTicks = 0;

    /** Maximum distance (squared) from spawn before the boss retreats/despawns. */
    protected double maxDistanceFromSpawnSq = 100.0 * 100.0;

    /** The location where this boss originally spawned. */
    protected Location spawnLocation;

    /** Announcement radius for spawn messages. */
    protected double announceRadius = 100.0;

    protected CustomWorldBoss(JGlimsPlugin plugin, CustomMobType mobType) {
        super(plugin, mobType);
    }

    @Override
    public void spawn(Location location) {
        this.spawnLocation = location.clone();
        super.spawn(location);
    }

    @Override
    protected void onSpawn() {
        super.onSpawn();
        announceSpawn();
    }

    @Override
    protected void onTick() {
        // Distance-based leash — if boss wanders too far from spawn, pull it back
        if (hitboxEntity != null && spawnLocation != null) {
            if (hitboxEntity.getLocation().distanceSquared(spawnLocation) > maxDistanceFromSpawnSq) {
                moveTo(spawnLocation);
            }
        }

        // Idle despawn timer
        if (!inCombat) {
            idleTicks++;
            if (idleTicks >= despawnTimerTicks) {
                plugin.getLogger().info("[WorldBoss] " + mobType.getDisplayName()
                        + " despawned due to idle timeout.");
                despawn();
                return;
            }
        } else {
            idleTicks = 0;
        }

        super.onTick();
    }

    /**
     * Announces the boss spawn to all players within range.
     */
    protected void announceSpawn() {
        List<Player> nearby = findNearbyPlayers(announceRadius);
        Component message = Component.text("A ", NamedTextColor.DARK_RED)
                .append(Component.text(mobType.getDisplayName(), NamedTextColor.RED)
                        .decorate(TextDecoration.BOLD))
                .append(Component.text(" has appeared nearby!", NamedTextColor.DARK_RED));

        for (Player p : nearby) {
            p.sendMessage(message);
            p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.8f, 0.6f);
        }
    }

    /**
     * Sets the idle despawn timer in ticks.
     *
     * @param ticks number of ticks before idle despawn
     */
    public void setDespawnTimer(long ticks) {
        this.despawnTimerTicks = ticks;
    }

    /**
     * Sets the maximum leash distance from the spawn point.
     *
     * @param distance max distance in blocks
     */
    public void setMaxDistanceFromSpawn(double distance) {
        this.maxDistanceFromSpawnSq = distance * distance;
    }
}

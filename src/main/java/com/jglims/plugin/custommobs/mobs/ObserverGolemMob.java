package com.jglims.plugin.custommobs.mobs;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.custommobs.CustomMobEntity;
import com.jglims.plugin.custommobs.CustomMobType;
import kr.toxicity.model.api.animation.AnimationIterator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Observer Golem - Constructible mob. HP 200. Never attacks players.
 * Attacks hostile mobs with wider aggro range (20 blocks).
 * Launch mob into air attack. Observation pulse every 30s (chat message listing
 * nearby threats to builder). Drops: 2 iron blocks, 1 observer.
 */
public class ObserverGolemMob extends CustomMobEntity {

    private UUID builderUUID;
    private long lastAttackTick = 0;
    private long lastPulseTick = 0;
    private static final long ATTACK_COOLDOWN = 20L;
    private static final long PULSE_INTERVAL = 600L; // 30 seconds

    public ObserverGolemMob(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.OBSERVER_GOLEM);
    }

    /**
     * Sets the builder (creator) of this golem.
     */
    public void setBuilder(Player builder) {
        this.builderUUID = builder.getUniqueId();
        if (hitboxEntity != null) {
            hitboxEntity.getPersistentDataContainer().set(
                    new NamespacedKey(plugin, "custom_mob_owner"),
                    PersistentDataType.STRING, builderUUID.toString());
            hitboxEntity.customName(Component.text(builder.getName() + "'s Observer Golem",
                    NamedTextColor.GOLD));
        }
    }

    @Override
    protected void onSpawn() {
        playAnimation("idle", AnimationIterator.Type.LOOP);
    }

    @Override
    protected void onTick() {
        if (hitboxEntity == null || !alive) return;

        long ticks = hitboxEntity.getTicksLived();

        // Observation Pulse
        if (ticks - lastPulseTick >= PULSE_INTERVAL) {
            lastPulseTick = ticks;
            observationPulse();
        }

        // Find and attack hostile mobs (20 block range)
        LivingEntity hostileTarget = findNearestHostileMob(20);
        if (hostileTarget != null) {
            if (hitboxEntity instanceof Mob mob) {
                mob.setTarget(hostileTarget);
            }

            double dist = hitboxEntity.getLocation().distanceSquared(hostileTarget.getLocation());

            // Launch attack - throw mob into air
            if (dist < 9 && canAttack()) {
                lastAttackTick = ticks;
                playAnimation("attack", AnimationIterator.Type.PLAY_ONCE);

                // Launch mob into the air
                Vector launch = new Vector(0, 2.0, 0);
                hostileTarget.setVelocity(launch);
                hostileTarget.damage(mobType.getBaseDamage(), hitboxEntity);

                hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                        Sound.ENTITY_IRON_GOLEM_ATTACK, 1.5f, 1.0f);
                hitboxEntity.getWorld().spawnParticle(Particle.CRIT,
                        hostileTarget.getLocation().add(0, 1, 0), 15, 0.5, 0.5, 0.5, 0.2);
            }
        }
    }

    private void observationPulse() {
        if (builderUUID == null) return;
        Player builder = plugin.getServer().getPlayer(builderUUID);
        if (builder == null || !builder.isOnline()) return;

        // Count nearby threats
        Location loc = hitboxEntity.getLocation();
        List<String> threats = new ArrayList<>();
        for (Entity e : loc.getWorld().getNearbyEntities(loc, 20, 10, 20)) {
            if (isHostileMob(e)) {
                threats.add(e.getType().name().toLowerCase().replace('_', ' '));
            }
        }

        hitboxEntity.getWorld().spawnParticle(Particle.ENCHANT,
                loc.add(0, 2, 0), 20, 2, 2, 2, 0.5);
        hitboxEntity.getWorld().playSound(loc, Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.5f);

        if (threats.isEmpty()) {
            builder.sendMessage(Component.text("[Observer Golem] ", NamedTextColor.GOLD)
                    .append(Component.text("No threats detected nearby.", NamedTextColor.GREEN)));
        } else {
            // Count by type
            Map<String, Integer> counts = new LinkedHashMap<>();
            for (String t : threats) {
                counts.merge(t, 1, Integer::sum);
            }
            StringBuilder sb = new StringBuilder();
            counts.forEach((name, count) -> {
                if (sb.length() > 0) sb.append(", ");
                sb.append(count).append("x ").append(name);
            });
            builder.sendMessage(Component.text("[Observer Golem] ", NamedTextColor.GOLD)
                    .append(Component.text("Threats: " + sb, NamedTextColor.RED)));
        }
    }

    @Override
    protected void onDamage(double amount, Player source) {
        // Never retaliates against players
        playAnimation("hurt", AnimationIterator.Type.PLAY_ONCE);
        hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                Sound.ENTITY_IRON_GOLEM_HURT, 1.0f, 1.0f);
    }

    @Override
    protected void onDeath(Player killer) {
        if (hitboxEntity != null) {
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                    Sound.ENTITY_IRON_GOLEM_DEATH, 1.0f, 1.0f);
        }
    }

    @Override
    protected List<ItemStack> getDrops(Player killer) {
        List<ItemStack> drops = new ArrayList<>();
        drops.add(new ItemStack(Material.IRON_BLOCK, 2));
        drops.add(new ItemStack(Material.OBSERVER, 1));
        return drops;
    }

    private LivingEntity findNearestHostileMob(double radius) {
        if (hitboxEntity == null) return null;
        Location loc = hitboxEntity.getLocation();
        double radiusSq = radius * radius;
        LivingEntity nearest = null;
        double nearestDist = radiusSq;

        for (Entity e : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
            if (isHostileMob(e)) {
                double dist = e.getLocation().distanceSquared(loc);
                if (dist < nearestDist) {
                    nearestDist = dist;
                    nearest = (LivingEntity) e;
                }
            }
        }
        return nearest;
    }

    private boolean isHostileMob(Entity e) {
        return e instanceof Monster && !(e instanceof Player);
    }

    private boolean canAttack() {
        return hitboxEntity.getTicksLived() - lastAttackTick >= ATTACK_COOLDOWN;
    }

    @Override
    protected long getTickRate() {
        return 5L;
    }
}

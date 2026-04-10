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
 * Mutant Iron Golem - Constructible mob. HP 400. Never attacks players.
 * Mega launch (15 blocks up), ground pound (10-block AoE).
 * Faster than regular golem. Drops: iron blocks x4.
 */
public class MutantIronGolemMob extends CustomMobEntity {

    private UUID builderUUID;
    private long lastAttackTick = 0;
    private long lastGroundPoundTick = 0;
    private static final long ATTACK_COOLDOWN = 20L;
    private static final long GROUND_POUND_COOLDOWN = 80L;

    public MutantIronGolemMob(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.MUTANT_IRON_GOLEM);
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
            hitboxEntity.customName(Component.text(builder.getName() + "'s Mutant Iron Golem",
                    NamedTextColor.WHITE));
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

        // Find and attack hostile mobs (20 block range)
        LivingEntity hostileTarget = findNearestHostileMob(20);
        if (hostileTarget != null) {
            if (hitboxEntity instanceof Mob mob) {
                mob.setTarget(hostileTarget);
            }

            double dist = hitboxEntity.getLocation().distanceSquared(hostileTarget.getLocation());

            // Mega Launch - throw mob 15 blocks into the air
            if (dist < 9 && canAttack()) {
                lastAttackTick = ticks;
                playAnimation("attack", AnimationIterator.Type.PLAY_ONCE);
                hostileTarget.damage(mobType.getBaseDamage(), hitboxEntity);
                hostileTarget.setVelocity(new Vector(0, 3.5, 0)); // ~15 blocks up
                hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                        Sound.ENTITY_IRON_GOLEM_ATTACK, 2.0f, 0.6f);
                hitboxEntity.getWorld().spawnParticle(Particle.CRIT,
                        hostileTarget.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.3);
            }

            // Ground Pound - 10-block AoE against hostile mobs
            if (canGroundPound()) {
                List<LivingEntity> nearbyHostiles = findNearbyHostileMobs(10);
                if (nearbyHostiles.size() >= 2) {
                    lastGroundPoundTick = ticks;
                    playAnimation("attack", AnimationIterator.Type.PLAY_ONCE);
                    Location loc = hitboxEntity.getLocation();

                    for (LivingEntity hostile : nearbyHostiles) {
                        hostile.damage(mobType.getBaseDamage() * 0.8, hitboxEntity);
                        Vector knock = hostile.getLocation().toVector()
                                .subtract(loc.toVector()).normalize().multiply(1.5);
                        knock.setY(0.8);
                        hostile.setVelocity(knock);
                    }

                    loc.getWorld().playSound(loc, Sound.ENTITY_RAVAGER_ROAR, 2.0f, 0.5f);
                    loc.getWorld().spawnParticle(Particle.BLOCK, loc,
                            40, 5, 1, 5, 0, Material.IRON_BLOCK.createBlockData());
                }
            }
        }
    }

    @Override
    protected void onDamage(double amount, Player source) {
        // Never retaliates against players
        playAnimation("hurt", AnimationIterator.Type.PLAY_ONCE);
        hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                Sound.ENTITY_IRON_GOLEM_HURT, 1.0f, 0.7f);
    }

    @Override
    protected void onDeath(Player killer) {
        if (hitboxEntity != null) {
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                    Sound.ENTITY_IRON_GOLEM_DEATH, 1.5f, 0.6f);
            hitboxEntity.getWorld().spawnParticle(Particle.BLOCK,
                    hitboxEntity.getLocation().add(0, 1.5, 0), 40, 1, 1, 1, 0,
                    Material.IRON_BLOCK.createBlockData());
        }
    }

    @Override
    protected List<ItemStack> getDrops(Player killer) {
        List<ItemStack> drops = new ArrayList<>();
        drops.add(new ItemStack(Material.IRON_BLOCK, 4));
        return drops;
    }

    private LivingEntity findNearestHostileMob(double radius) {
        if (hitboxEntity == null) return null;
        Location loc = hitboxEntity.getLocation();
        double radiusSq = radius * radius;
        LivingEntity nearest = null;
        double nearestDist = radiusSq;

        for (Entity e : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
            if (e instanceof Monster && !(e instanceof Player)) {
                double dist = e.getLocation().distanceSquared(loc);
                if (dist < nearestDist) {
                    nearestDist = dist;
                    nearest = (LivingEntity) e;
                }
            }
        }
        return nearest;
    }

    private List<LivingEntity> findNearbyHostileMobs(double radius) {
        if (hitboxEntity == null) return Collections.emptyList();
        Location loc = hitboxEntity.getLocation();
        List<LivingEntity> result = new ArrayList<>();
        for (Entity e : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
            if (e instanceof Monster && !(e instanceof Player)) {
                result.add((LivingEntity) e);
            }
        }
        return result;
    }

    private boolean canAttack() {
        return hitboxEntity.getTicksLived() - lastAttackTick >= ATTACK_COOLDOWN;
    }

    private boolean canGroundPound() {
        return hitboxEntity.getTicksLived() - lastGroundPoundTick >= GROUND_POUND_COOLDOWN;
    }

    @Override
    protected long getTickRate() {
        return 5L;
    }
}

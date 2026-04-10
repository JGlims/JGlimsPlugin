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
 * Stone Golem - Constructible mob. HP 80. Weaker Iron Golem.
 * Same peaceful AI - never attacks players, targets hostile mobs.
 * Drops cobblestone x8, poppy.
 */
public class StoneGolemMob extends CustomMobEntity {

    private UUID builderUUID;
    private long lastAttackTick = 0;
    private static final long ATTACK_COOLDOWN = 25L;

    public StoneGolemMob(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.STONE_GOLEM);
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
            hitboxEntity.customName(Component.text(builder.getName() + "'s Stone Golem",
                    NamedTextColor.GRAY));
        }
    }

    @Override
    protected void onSpawn() {
        playAnimation("idle", AnimationIterator.Type.LOOP);
    }

    @Override
    protected void onTick() {
        if (hitboxEntity == null || !alive) return;

        // Find and attack hostile mobs (16 block range)
        LivingEntity hostileTarget = findNearestHostileMob(16);
        if (hostileTarget != null) {
            if (hitboxEntity instanceof Mob mob) {
                mob.setTarget(hostileTarget);
            }

            double dist = hitboxEntity.getLocation().distanceSquared(hostileTarget.getLocation());

            if (dist < 9 && canAttack()) {
                lastAttackTick = hitboxEntity.getTicksLived();
                playAnimation("attack", AnimationIterator.Type.PLAY_ONCE);
                hostileTarget.damage(mobType.getBaseDamage(), hitboxEntity);
                Vector knockback = hostileTarget.getLocation().toVector()
                        .subtract(hitboxEntity.getLocation().toVector()).normalize().multiply(0.5);
                knockback.setY(0.3);
                hostileTarget.setVelocity(knockback);
                hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                        Sound.ENTITY_IRON_GOLEM_ATTACK, 1.0f, 1.2f);
            }
        }
    }

    @Override
    protected void onDamage(double amount, Player source) {
        // Never retaliates against players
        playAnimation("hurt", AnimationIterator.Type.PLAY_ONCE);
        hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                Sound.BLOCK_STONE_BREAK, 1.0f, 0.8f);
    }

    @Override
    protected void onDeath(Player killer) {
        if (hitboxEntity != null) {
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                    Sound.ENTITY_IRON_GOLEM_DEATH, 1.0f, 1.2f);
            hitboxEntity.getWorld().spawnParticle(Particle.BLOCK,
                    hitboxEntity.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0,
                    Material.STONE.createBlockData());
        }
    }

    @Override
    protected List<ItemStack> getDrops(Player killer) {
        List<ItemStack> drops = new ArrayList<>();
        drops.add(new ItemStack(Material.COBBLESTONE, 8));
        drops.add(new ItemStack(Material.POPPY, 1));
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

    private boolean canAttack() {
        return hitboxEntity.getTicksLived() - lastAttackTick >= ATTACK_COOLDOWN;
    }

    @Override
    protected long getTickRate() {
        return 5L;
    }
}

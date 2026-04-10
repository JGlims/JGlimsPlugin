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
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Stegosaurus - Passive herbivore. Grazes on grass. If attacked, retaliates
 * with tail thagomizer (15 dmg + knockback). Spawns in groups of 2-4.
 * Drops leather and bone.
 */
public class StegosaurusMob extends CustomMobEntity {

    private UUID provokerUUID;
    private long provokedUntilTick = 0;
    private long lastAttackTick = 0;
    private static final long ATTACK_COOLDOWN = 30L;
    private long lastGrazeTick = 0;

    public StegosaurusMob(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.STEGOSAURUS);
    }

    @Override
    protected void onSpawn() {
        playAnimation("idle", AnimationIterator.Type.LOOP);
    }

    @Override
    protected void onTick() {
        if (hitboxEntity == null || !alive) return;

        // Grazing behavior - eat grass occasionally
        long ticks = hitboxEntity.getTicksLived();
        if (ticks - lastGrazeTick >= 200) { // Every 10 seconds
            lastGrazeTick = ticks;
            playAnimation("eat", AnimationIterator.Type.PLAY_ONCE);
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                    Sound.ENTITY_COW_AMBIENT, 0.8f, 0.5f);
        }

        // Retaliation if provoked
        if (provokerUUID != null) {
            if (ticks > provokedUntilTick) {
                provokerUUID = null;
                if (hitboxEntity instanceof Mob mob) {
                    mob.setTarget(null);
                }
                playAnimation("idle", AnimationIterator.Type.LOOP);
                return;
            }

            Player target = plugin.getServer().getPlayer(provokerUUID);
            if (target == null || !target.isOnline()) {
                provokerUUID = null;
                return;
            }

            if (hitboxEntity instanceof Mob mob) {
                mob.setTarget(target);
            }

            double dist = hitboxEntity.getLocation().distanceSquared(target.getLocation());

            // Tail thagomizer attack
            if (dist < 16 && canAttack()) {
                lastAttackTick = ticks;
                playAnimation("attack", AnimationIterator.Type.PLAY_ONCE);
                target.damage(15, hitboxEntity);
                // Heavy knockback
                Vector knockback = target.getLocation().toVector()
                        .subtract(hitboxEntity.getLocation().toVector()).normalize().multiply(1.5);
                knockback.setY(0.6);
                target.setVelocity(knockback);
                hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                        Sound.ENTITY_IRON_GOLEM_ATTACK, 1.5f, 0.5f);
                hitboxEntity.getWorld().spawnParticle(Particle.CRIT,
                        target.getLocation().add(0, 1, 0), 15, 0.5, 0.5, 0.5, 0.2);
            }
        }
    }

    @Override
    protected void onDamage(double amount, Player source) {
        playAnimation("hurt", AnimationIterator.Type.PLAY_ONCE);
        hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                Sound.ENTITY_RAVAGER_HURT, 0.8f, 0.6f);
        if (source != null) {
            provokerUUID = source.getUniqueId();
            provokedUntilTick = hitboxEntity.getTicksLived() + 200; // 10 seconds
        }
    }

    @Override
    protected void onDeath(Player killer) {
        if (hitboxEntity != null) {
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                    Sound.ENTITY_RAVAGER_DEATH, 0.8f, 0.6f);
        }
        if (killer != null) {
            killer.giveExp(30);
        }
    }

    @Override
    protected List<ItemStack> getDrops(Player killer) {
        List<ItemStack> drops = new ArrayList<>();
        drops.add(new ItemStack(Material.LEATHER, 2 + (int) (Math.random() * 3)));
        drops.add(new ItemStack(Material.BONE, 2 + (int) (Math.random() * 2)));
        return drops;
    }

    private boolean canAttack() {
        return hitboxEntity.getTicksLived() - lastAttackTick >= ATTACK_COOLDOWN;
    }

    @Override
    protected long getTickRate() {
        return 5L;
    }
}

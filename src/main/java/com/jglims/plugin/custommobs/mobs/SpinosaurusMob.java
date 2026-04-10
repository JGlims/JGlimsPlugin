package com.jglims.plugin.custommobs.mobs;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.custommobs.CustomMobEntity;
import com.jglims.plugin.custommobs.CustomMobType;
import kr.toxicity.model.api.animation.AnimationIterator;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Spinosaurus - semi-aquatic Jurassic predator.
 * HP 120. Faster in water. Claw swipe, jaw snap, tail whip.
 */
public class SpinosaurusMob extends CustomMobEntity {

    private long lastClawTick = 0;
    private long lastSnapTick = 0;
    private long lastTailTick = 0;

    public SpinosaurusMob(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.SPINOSAURUS);
    }

    @Override
    protected void onSpawn() {
        playAnimation("idle", AnimationIterator.Type.LOOP);
    }

    @Override
    protected void onTick() {
        if (hitboxEntity == null || !alive) return;

        // Speed boost in water
        boolean inWater = hitboxEntity.isInWater();
        if (inWater) {
            hitboxEntity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10, 2, false, false));
            hitboxEntity.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 100, 0, false, false));
        }

        Player target = findNearestPlayer(20);
        if (target == null) {
            state = MobState.IDLE;
            return;
        }

        state = MobState.ATTACKING;
        if (hitboxEntity instanceof org.bukkit.entity.Mob mob) {
            mob.setTarget(target);
        }

        double dist = hitboxEntity.getLocation().distanceSquared(target.getLocation());
        long ticks = hitboxEntity.getTicksLived();

        // Jaw Snap - heavy damage, 3s cooldown
        if (dist < 9 && ticks - lastSnapTick >= 60) {
            lastSnapTick = ticks;
            playAnimation("bite", AnimationIterator.Type.PLAY_ONCE);
            double dmg = inWater ? mobType.getBaseDamage() * 1.5 : mobType.getBaseDamage();
            target.damage(dmg, hitboxEntity);
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_PHANTOM_BITE, 1.5f, 0.5f);
        }

        // Claw Swipe - faster melee, 1.5s cooldown
        if (dist < 12 && ticks - lastClawTick >= 30) {
            lastClawTick = ticks;
            playAnimation("claw", AnimationIterator.Type.PLAY_ONCE);
            target.damage(mobType.getBaseDamage() * 0.7, hitboxEntity);
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 0.7f);
        }

        // Tail Whip - knockback AoE behind, 5s cooldown
        if (dist < 16 && ticks - lastTailTick >= 100) {
            lastTailTick = ticks;
            playAnimation("tail_whip", AnimationIterator.Type.PLAY_ONCE);
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0f, 0.6f);
            for (Player p : findNearbyPlayers(4)) {
                Vector knockback = p.getLocation().toVector().subtract(hitboxEntity.getLocation().toVector()).normalize();
                p.setVelocity(knockback.multiply(1.5).setY(0.4));
                p.damage(6.0, hitboxEntity);
            }
        }
    }

    @Override
    protected void onDamage(double amount, Player source) {
        playAnimation("hurt", AnimationIterator.Type.PLAY_ONCE);
        hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_RAVAGER_HURT, 1.0f, 0.6f);
    }

    @Override
    protected void onDeath(Player killer) {
        if (hitboxEntity != null) {
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_RAVAGER_DEATH, 1.5f, 0.5f);
        }
        if (killer != null) {
            killer.giveExp(80);
        }
    }

    @Override
    protected List<ItemStack> getDrops(Player killer) {
        List<ItemStack> drops = new ArrayList<>();
        drops.add(new ItemStack(Material.BONE, 3 + (int) (Math.random() * 4)));
        drops.add(new ItemStack(Material.COD, 2 + (int) (Math.random() * 3)));
        return drops;
    }

    @Override
    protected long getTickRate() {
        return 5L;
    }
}

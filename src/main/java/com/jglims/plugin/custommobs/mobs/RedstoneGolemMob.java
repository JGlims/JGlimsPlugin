package com.jglims.plugin.custommobs.mobs;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.custommobs.CustomMobEntity;
import com.jglims.plugin.custommobs.CustomMobType;
import kr.toxicity.model.api.animation.AnimationIterator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Redstone Golem - aggressive to players only.
 * HP 250. Ground slam AoE, charge, redstone pulse (disables shields 3s).
 * Heavy footsteps heard from 20 blocks. Drops 4 redstone blocks if killed while sneaking, 2 otherwise.
 */
public class RedstoneGolemMob extends CustomMobEntity {

    private long lastSlamTick = 0;
    private long lastChargeTick = 0;
    private long lastPulseTick = 0;
    private int footstepCounter = 0;

    public RedstoneGolemMob(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.REDSTONE_GOLEM);
    }

    @Override
    protected void onSpawn() {
        playAnimation("idle", AnimationIterator.Type.LOOP);
    }

    @Override
    protected void onTick() {
        if (hitboxEntity == null || !alive) return;

        // Heavy footsteps
        footstepCounter++;
        if (footstepCounter % 10 == 0) {
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                    Sound.ENTITY_IRON_GOLEM_STEP, 3.0f, 0.5f);
            hitboxEntity.getWorld().spawnParticle(Particle.DUST, hitboxEntity.getLocation(),
                    5, 0.3, 0, 0.3, new Particle.DustOptions(Color.RED, 1.5f));
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

        // Ground Slam AoE - 5 block radius, 4s cooldown
        if (dist < 25 && ticks - lastSlamTick >= 80) {
            lastSlamTick = ticks;
            playAnimation("slam", AnimationIterator.Type.PLAY_ONCE);
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_IRON_GOLEM_ATTACK, 2.0f, 0.5f);
            hitboxEntity.getWorld().spawnParticle(Particle.BLOCK, hitboxEntity.getLocation(),
                    40, 3, 0.5, 3, Bukkit.createBlockData(Material.REDSTONE_BLOCK));

            for (Player p : findNearbyPlayers(5)) {
                p.damage(12.0, hitboxEntity);
                p.setVelocity(new Vector(0, 0.7, 0));
                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1, false, false));
            }
        }

        // Charge - 6s cooldown, distance 6-15 blocks
        if (dist > 36 && dist < 225 && ticks - lastChargeTick >= 120) {
            lastChargeTick = ticks;
            playAnimation("charge", AnimationIterator.Type.PLAY_ONCE);
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_RAVAGER_ROAR, 2.0f, 0.6f);
            Vector dir = target.getLocation().toVector().subtract(hitboxEntity.getLocation().toVector()).normalize();
            hitboxEntity.setVelocity(dir.multiply(2.0).setY(0.2));
        }

        // Redstone Pulse - disables shields for 3s, 8s cooldown
        if (dist < 64 && ticks - lastPulseTick >= 160) {
            lastPulseTick = ticks;
            playAnimation("pulse", AnimationIterator.Type.PLAY_ONCE);
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 2.0f, 0.5f);
            hitboxEntity.getWorld().spawnParticle(Particle.DUST, hitboxEntity.getLocation().add(0, 1, 0),
                    40, 4, 2, 4, new Particle.DustOptions(Color.RED, 2.0f));

            for (Player p : findNearbyPlayers(8)) {
                // Disable shield by setting cooldown
                p.setCooldown(Material.SHIELD, 60); // 3 seconds
                p.sendMessage(Component.text("Your shield overloads from the redstone pulse!", NamedTextColor.RED));
            }
        }
    }

    @Override
    protected void onDamage(double amount, Player source) {
        playAnimation("hurt", AnimationIterator.Type.PLAY_ONCE);
        hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_IRON_GOLEM_HURT, 1.5f, 0.5f);
    }

    @Override
    protected void onDeath(Player killer) {
        if (hitboxEntity != null) {
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_IRON_GOLEM_DEATH, 2.0f, 0.5f);
            hitboxEntity.getWorld().spawnParticle(Particle.DUST, hitboxEntity.getLocation().add(0, 1, 0),
                    50, 2, 2, 2, new Particle.DustOptions(Color.RED, 2.0f));
        }
        if (killer != null) {
            killer.giveExp(150);
        }
    }

    @Override
    protected List<ItemStack> getDrops(Player killer) {
        List<ItemStack> drops = new ArrayList<>();
        int count = (killer != null && killer.isSneaking()) ? 4 : 2;
        drops.add(new ItemStack(Material.REDSTONE_BLOCK, count));
        return drops;
    }

    @Override
    protected long getTickRate() {
        return 5L;
    }
}

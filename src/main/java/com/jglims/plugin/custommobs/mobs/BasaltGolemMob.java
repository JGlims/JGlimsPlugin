package com.jglims.plugin.custommobs.mobs;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.custommobs.CustomMobEntity;
import com.jglims.plugin.custommobs.CustomMobType;
import kr.toxicity.model.api.animation.AnimationIterator;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Basalt Golem - aggressive golem in Basalt Deltas.
 * HP 200. Basalt eruption, lava splash (fire 3s), ground slam. Fire immune.
 */
public class BasaltGolemMob extends CustomMobEntity {

    private long lastSlamTick = 0;
    private long lastEruptionTick = 0;
    private long lastLavaTick = 0;

    public BasaltGolemMob(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.BASALT_GOLEM);
    }

    @Override
    protected void configureHitboxEntity() {
        super.configureHitboxEntity();
        // Fire immune
        hitboxEntity.setFireTicks(0);
    }

    @Override
    protected void onSpawn() {
        playAnimation("idle", AnimationIterator.Type.LOOP);
    }

    @Override
    protected void onTick() {
        if (hitboxEntity == null || !alive) return;

        // Maintain fire immunity
        hitboxEntity.setFireTicks(0);

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

        // Ground Slam - AoE melee, 4s cooldown
        if (dist < 16 && ticks - lastSlamTick >= 80) {
            lastSlamTick = ticks;
            playAnimation("slam", AnimationIterator.Type.PLAY_ONCE);
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_IRON_GOLEM_ATTACK, 2.0f, 0.4f);
            hitboxEntity.getWorld().spawnParticle(Particle.BLOCK, hitboxEntity.getLocation(),
                    30, 2, 0.5, 2, Bukkit.createBlockData(Material.BASALT));
            for (Player p : findNearbyPlayers(4)) {
                p.damage(mobType.getBaseDamage(), hitboxEntity);
                p.setVelocity(new Vector(0, 0.6, 0));
            }
        }

        // Basalt Eruption - spawns damaging particles in an area, 8s cooldown
        if (dist < 144 && ticks - lastEruptionTick >= 160) {
            lastEruptionTick = ticks;
            playAnimation("eruption", AnimationIterator.Type.PLAY_ONCE);
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.5f);

            Location center = target.getLocation();
            new BukkitRunnable() {
                int tick = 0;
                @Override
                public void run() {
                    if (tick++ >= 5) { cancel(); return; }
                    Location burst = center.clone().add((Math.random() - 0.5) * 4, 0, (Math.random() - 0.5) * 4);
                    burst.getWorld().spawnParticle(Particle.LAVA, burst, 10, 0.5, 1, 0.5);
                    burst.getWorld().playSound(burst, Sound.BLOCK_LAVA_POP, 1.0f, 0.8f);
                    for (Player p : findNearbyPlayers(6)) {
                        if (p.getLocation().distanceSquared(burst) < 9) {
                            p.damage(5.0, hitboxEntity);
                        }
                    }
                }
            }.runTaskTimer(plugin, 0L, 10L);
        }

        // Lava Splash - sets players on fire, 6s cooldown
        if (dist < 64 && ticks - lastLavaTick >= 120) {
            lastLavaTick = ticks;
            playAnimation("lava_splash", AnimationIterator.Type.PLAY_ONCE);
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.5f, 0.5f);
            hitboxEntity.getWorld().spawnParticle(Particle.LAVA, hitboxEntity.getLocation().add(0, 1, 0),
                    20, 2, 1, 2);
            for (Player p : findNearbyPlayers(6)) {
                p.setFireTicks(60); // 3 seconds
                p.damage(4.0, hitboxEntity);
            }
        }
    }

    @Override
    protected void onDamage(double amount, Player source) {
        playAnimation("hurt", AnimationIterator.Type.PLAY_ONCE);
        hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_IRON_GOLEM_HURT, 1.5f, 0.4f);
    }

    @Override
    protected void onDeath(Player killer) {
        if (hitboxEntity != null) {
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_IRON_GOLEM_DEATH, 2.0f, 0.4f);
            hitboxEntity.getWorld().spawnParticle(Particle.LAVA, hitboxEntity.getLocation().add(0, 1, 0), 40, 1, 1, 1);
        }
        if (killer != null) {
            killer.giveExp(150);
        }
    }

    @Override
    protected List<ItemStack> getDrops(Player killer) {
        List<ItemStack> drops = new ArrayList<>();
        drops.add(new ItemStack(Material.BASALT, 4 + (int) (Math.random() * 5)));
        drops.add(new ItemStack(Material.MAGMA_CREAM, 2 + (int) (Math.random() * 3)));
        return drops;
    }

    @Override
    protected long getTickRate() {
        return 5L;
    }
}

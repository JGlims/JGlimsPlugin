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
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Ancient Runic Portal - Aether mimic disguised as a decorative portal.
 * HP 120. Motionless until player within 5 blocks.
 * Runic blast (teleports player randomly), portal slam (melee AoE), dimensional pull.
 */
public class AncientRunicPortalMob extends CustomMobEntity {

    private boolean dormant = true;
    private long lastBlastTick = 0;
    private long lastSlamTick = 0;
    private long lastPullTick = 0;

    public AncientRunicPortalMob(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.ANCIENT_RUNIC_PORTAL);
    }

    @Override
    protected void onSpawn() {
        playAnimation("dormant", AnimationIterator.Type.LOOP);
        if (hitboxEntity instanceof org.bukkit.entity.Mob mob) {
            mob.setAI(false);
        }
        // Hide name while dormant
        hitboxEntity.setCustomNameVisible(false);
    }

    @Override
    protected void onTick() {
        if (hitboxEntity == null || !alive) return;

        Player target = findNearestPlayer(dormant ? 5 : 20);
        if (target == null) {
            if (!dormant) state = MobState.IDLE;
            return;
        }

        double dist = hitboxEntity.getLocation().distanceSquared(target.getLocation());

        // Wake up
        if (dormant && dist < 25) {
            dormant = false;
            hitboxEntity.setCustomNameVisible(true);
            if (hitboxEntity instanceof org.bukkit.entity.Mob mob) {
                mob.setAI(true);
            }
            playAnimation("awaken", AnimationIterator.Type.PLAY_ONCE);
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_WARDEN_EMERGE, 1.5f, 1.2f);
            hitboxEntity.getWorld().spawnParticle(Particle.PORTAL, hitboxEntity.getLocation().add(0, 1, 0), 50, 1, 2, 1);
            for (Player p : findNearbyPlayers(10)) {
                p.sendMessage(Component.text("The portal awakens!", NamedTextColor.LIGHT_PURPLE));
            }
            return;
        }

        if (dormant) return;

        state = MobState.ATTACKING;
        if (hitboxEntity instanceof org.bukkit.entity.Mob mob) {
            mob.setTarget(target);
        }

        long ticks = hitboxEntity.getTicksLived();

        // Dimensional Pull - pull players toward the portal, 6s cooldown
        if (dist < 225 && dist > 16 && ticks - lastPullTick >= 120) {
            lastPullTick = ticks;
            playAnimation("pull", AnimationIterator.Type.PLAY_ONCE);
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.BLOCK_PORTAL_AMBIENT, 2.0f, 0.5f);
            for (Player p : findNearbyPlayers(12)) {
                Vector pull = hitboxEntity.getLocation().toVector().subtract(p.getLocation().toVector()).normalize().multiply(0.8);
                p.setVelocity(pull);
            }
        }

        // Runic Blast - teleports player to random nearby location, 8s cooldown
        if (dist < 100 && ticks - lastBlastTick >= 160) {
            lastBlastTick = ticks;
            playAnimation("blast", AnimationIterator.Type.PLAY_ONCE);
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 2.0f, 0.8f);
            target.damage(6.0, hitboxEntity);

            // Teleport player randomly within 10 blocks
            Location tp = target.getLocation().add(
                    (Math.random() - 0.5) * 20, 0, (Math.random() - 0.5) * 20);
            tp.setY(target.getWorld().getHighestBlockYAt(tp) + 1);
            target.teleport(tp);
            target.getWorld().playSound(tp, Sound.ENTITY_ENDERMAN_TELEPORT, 1.5f, 1.0f);
            target.getWorld().spawnParticle(Particle.PORTAL, tp.add(0, 1, 0), 30, 0.5, 1, 0.5);
        }

        // Portal Slam - melee AoE, 3s cooldown
        if (dist < 12 && ticks - lastSlamTick >= 60) {
            lastSlamTick = ticks;
            playAnimation("slam", AnimationIterator.Type.PLAY_ONCE);
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_IRON_GOLEM_ATTACK, 1.5f, 0.8f);
            hitboxEntity.getWorld().spawnParticle(Particle.ENCHANT, hitboxEntity.getLocation().add(0, 0.5, 0), 20, 2, 0.5, 2);
            for (Player p : findNearbyPlayers(4)) {
                p.damage(mobType.getBaseDamage(), hitboxEntity);
            }
        }
    }

    @Override
    protected void onDamage(double amount, Player source) {
        if (dormant) {
            dormant = false;
            hitboxEntity.setCustomNameVisible(true);
            if (hitboxEntity instanceof org.bukkit.entity.Mob mob) {
                mob.setAI(true);
            }
            playAnimation("awaken", AnimationIterator.Type.PLAY_ONCE);
        }
        playAnimation("hurt", AnimationIterator.Type.PLAY_ONCE);
    }

    @Override
    protected void onDeath(Player killer) {
        if (hitboxEntity != null) {
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 1.0f, 0.5f);
            hitboxEntity.getWorld().spawnParticle(Particle.PORTAL, hitboxEntity.getLocation().add(0, 1, 0), 60, 1, 2, 1);
        }
        if (killer != null) {
            killer.giveExp(80);
        }
    }

    @Override
    protected List<ItemStack> getDrops(Player killer) {
        List<ItemStack> drops = new ArrayList<>();
        drops.add(new ItemStack(Material.AMETHYST_SHARD, 3 + (int) (Math.random() * 4)));
        return drops;
    }

    @Override
    protected long getTickRate() {
        return 5L;
    }
}

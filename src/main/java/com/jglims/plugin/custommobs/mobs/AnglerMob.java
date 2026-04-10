package com.jglims.plugin.custommobs.mobs;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.custommobs.CustomMobEntity;
import com.jglims.plugin.custommobs.CustomMobType;
import kr.toxicity.model.api.animation.AnimationIterator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Angler - deep ocean ambush predator.
 * HP 45. Sits motionless until a player comes within 8 blocks, then lunges.
 * Bite attack + lure flash that causes blindness for 2s.
 */
public class AnglerMob extends CustomMobEntity {

    private boolean ambushing = true;
    private long lastBiteTick = 0;
    private long lastFlashTick = 0;
    private static final long BITE_COOLDOWN = 30L;
    private static final long FLASH_COOLDOWN = 80L;

    public AnglerMob(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.ANGLER);
    }

    @Override
    protected void onSpawn() {
        playAnimation("idle", AnimationIterator.Type.LOOP);
        // Start motionless
        if (hitboxEntity instanceof org.bukkit.entity.Mob mob) {
            mob.setAI(false);
        }
    }

    @Override
    protected void onTick() {
        if (hitboxEntity == null || !alive) return;

        Player target = findNearestPlayer(ambushing ? 8 : 20);
        if (target == null) {
            if (!ambushing) {
                state = MobState.IDLE;
            }
            return;
        }

        double dist = hitboxEntity.getLocation().distanceSquared(target.getLocation());

        // Ambush trigger
        if (ambushing && dist < 64) { // 8 blocks
            ambushing = false;
            if (hitboxEntity instanceof org.bukkit.entity.Mob mob) {
                mob.setAI(true);
            }
            playAnimation("lunge", AnimationIterator.Type.PLAY_ONCE);
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_GUARDIAN_AMBIENT, 1.5f, 0.5f);

            // Lunge toward player
            Vector dir = target.getLocation().toVector().subtract(hitboxEntity.getLocation().toVector()).normalize();
            hitboxEntity.setVelocity(dir.multiply(1.8).setY(0.3));
        }

        if (ambushing) return;

        state = MobState.ATTACKING;
        if (hitboxEntity instanceof org.bukkit.entity.Mob mob) {
            mob.setTarget(target);
        }

        long ticks = hitboxEntity.getTicksLived();

        // Lure flash - blindness
        if (dist < 100 && ticks - lastFlashTick >= FLASH_COOLDOWN) {
            lastFlashTick = ticks;
            playAnimation("flash", AnimationIterator.Type.PLAY_ONCE);
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_GUARDIAN_DEATH, 1.0f, 1.5f);
            hitboxEntity.getWorld().spawnParticle(Particle.FLASH, hitboxEntity.getLocation().add(0, 1, 0), 1);
            for (Player p : findNearbyPlayers(10)) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0, false, false));
            }
        }

        // Bite attack
        if (dist < 9 && ticks - lastBiteTick >= BITE_COOLDOWN) {
            lastBiteTick = ticks;
            playAnimation("bite", AnimationIterator.Type.PLAY_ONCE);
            target.damage(mobType.getBaseDamage(), hitboxEntity);
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_PHANTOM_BITE, 1.0f, 0.7f);
        }
    }

    @Override
    protected void onDamage(double amount, Player source) {
        // Wake up if hit while ambushing
        if (ambushing) {
            ambushing = false;
            if (hitboxEntity instanceof org.bukkit.entity.Mob mob) {
                mob.setAI(true);
            }
        }
        playAnimation("hurt", AnimationIterator.Type.PLAY_ONCE);
    }

    @Override
    protected void onDeath(Player killer) {
        if (hitboxEntity != null) {
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_GUARDIAN_DEATH, 1.0f, 0.6f);
        }
        if (killer != null) {
            killer.giveExp(40);
        }
    }

    @Override
    protected List<ItemStack> getDrops(Player killer) {
        List<ItemStack> drops = new ArrayList<>();
        drops.add(new ItemStack(Material.PRISMARINE_SHARD, 2 + (int) (Math.random() * 3)));
        return drops;
    }

    @Override
    protected long getTickRate() {
        return 5L;
    }
}

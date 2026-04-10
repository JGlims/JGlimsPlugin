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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Tremorsaurus - ground-tremor causing Jurassic hostile.
 * HP 100. Creates ground tremors (Slowness I to nearby). Stomp AoE, charge, bite.
 */
public class TremorsaurusMob extends CustomMobEntity {

    private long lastStompTick = 0;
    private long lastChargeTick = 0;
    private long lastBiteTick = 0;
    private int tremorCounter = 0;

    public TremorsaurusMob(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.TREMORSAURUS);
    }

    @Override
    protected void onSpawn() {
        playAnimation("idle", AnimationIterator.Type.LOOP);
    }

    @Override
    protected void onTick() {
        if (hitboxEntity == null || !alive) return;

        // Passive tremor effect
        tremorCounter++;
        if (tremorCounter % 20 == 0) {
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_RAVAGER_STEP, 2.0f, 0.4f);
            hitboxEntity.getWorld().spawnParticle(Particle.BLOCK, hitboxEntity.getLocation(),
                    10, 1.5, 0, 1.5, Bukkit.createBlockData(Material.DIRT));
            for (Player p : findNearbyPlayers(8)) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 30, 0, false, false));
            }
        }

        Player target = findNearestPlayer(18);
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

        // Stomp AoE - 4s cooldown
        if (dist < 25 && ticks - lastStompTick >= 80) {
            lastStompTick = ticks;
            playAnimation("stomp", AnimationIterator.Type.PLAY_ONCE);
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_RAVAGER_ROAR, 2.0f, 0.4f);
            hitboxEntity.getWorld().spawnParticle(Particle.BLOCK, hitboxEntity.getLocation(),
                    40, 3, 0.5, 3, Bukkit.createBlockData(Material.DIRT));
            for (Player p : findNearbyPlayers(5)) {
                p.damage(10.0, hitboxEntity);
                p.setVelocity(new Vector(0, 0.6, 0));
                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1, false, false));
            }
        }

        // Charge - 5s cooldown
        if (dist > 36 && dist < 256 && ticks - lastChargeTick >= 100) {
            lastChargeTick = ticks;
            playAnimation("charge", AnimationIterator.Type.PLAY_ONCE);
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_RAVAGER_ROAR, 1.5f, 0.5f);
            Vector dir = target.getLocation().toVector().subtract(hitboxEntity.getLocation().toVector()).normalize();
            hitboxEntity.setVelocity(dir.multiply(1.8).setY(0.1));
        }

        // Bite - 2s cooldown
        if (dist < 9 && ticks - lastBiteTick >= 40) {
            lastBiteTick = ticks;
            playAnimation("bite", AnimationIterator.Type.PLAY_ONCE);
            target.damage(mobType.getBaseDamage(), hitboxEntity);
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_PHANTOM_BITE, 1.5f, 0.5f);
        }
    }

    @Override
    protected void onDamage(double amount, Player source) {
        playAnimation("hurt", AnimationIterator.Type.PLAY_ONCE);
    }

    @Override
    protected void onDeath(Player killer) {
        if (hitboxEntity != null) {
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_RAVAGER_DEATH, 2.0f, 0.4f);
            hitboxEntity.getWorld().spawnParticle(Particle.BLOCK, hitboxEntity.getLocation(),
                    50, 2, 1, 2, Bukkit.createBlockData(Material.DIRT));
        }
        if (killer != null) {
            killer.giveExp(70);
        }
    }

    @Override
    protected List<ItemStack> getDrops(Player killer) {
        List<ItemStack> drops = new ArrayList<>();
        // Tremor Scale (renamed iron nugget)
        ItemStack scale = new ItemStack(Material.IRON_NUGGET, 2 + (int) (Math.random() * 3));
        ItemMeta meta = scale.getItemMeta();
        meta.displayName(Component.text("Tremor Scale", NamedTextColor.GRAY));
        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "custom_item"), PersistentDataType.STRING, "tremor_scale");
        scale.setItemMeta(meta);
        drops.add(scale);
        drops.add(new ItemStack(Material.BONE, 2 + (int) (Math.random() * 3)));
        return drops;
    }

    @Override
    protected long getTickRate() {
        return 5L;
    }
}

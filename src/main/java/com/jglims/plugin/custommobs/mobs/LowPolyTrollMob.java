package com.jglims.plugin.custommobs.mobs;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.custommobs.CustomMobEntity;
import com.jglims.plugin.custommobs.CustomMobType;
import kr.toxicity.model.api.animation.AnimationIterator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Low Poly Troll - stronger Aether troll with a charge attack.
 * HP 50, periodically charges at the player with a speed burst.
 */
public class LowPolyTrollMob extends CustomMobEntity {

    private long lastAttackTick = 0;
    private long lastChargeTick = 0;
    private boolean charging = false;
    private static final long ATTACK_COOLDOWN = 25L;
    private static final long CHARGE_COOLDOWN = 100L; // 5 seconds

    public LowPolyTrollMob(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.LOW_POLY_TROLL);
    }

    @Override
    protected void onSpawn() {
        playAnimation("idle", AnimationIterator.Type.LOOP);
    }

    @Override
    protected void onTick() {
        if (hitboxEntity == null || !alive) return;

        Player target = findNearestPlayer(18);
        if (target == null) {
            state = MobState.IDLE;
            charging = false;
            return;
        }

        state = MobState.ATTACKING;
        double dist = hitboxEntity.getLocation().distanceSquared(target.getLocation());

        if (hitboxEntity instanceof org.bukkit.entity.Mob mob) {
            mob.setTarget(target);
        }

        // Charge attack - speed burst for 2 seconds
        long ticks = hitboxEntity.getTicksLived();
        if (!charging && dist > 16 && dist < 400 && ticks - lastChargeTick >= CHARGE_COOLDOWN) {
            charging = true;
            lastChargeTick = ticks;
            playAnimation("charge", AnimationIterator.Type.PLAY_ONCE);
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_RAVAGER_ROAR, 1.0f, 1.2f);

            // Apply speed burst
            if (hitboxEntity instanceof org.bukkit.entity.LivingEntity le) {
                le.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 2, false, false));
            }

            // Launch toward player
            Vector dir = target.getLocation().toVector().subtract(hitboxEntity.getLocation().toVector()).normalize();
            hitboxEntity.setVelocity(dir.multiply(1.5));

            new BukkitRunnable() {
                @Override
                public void run() {
                    charging = false;
                }
            }.runTaskLater(plugin, 40L);
        }

        // Melee attack within 3 blocks
        if (dist < 9 && ticks - lastAttackTick >= ATTACK_COOLDOWN) {
            lastAttackTick = ticks;
            double dmg = charging ? mobType.getBaseDamage() * 1.5 : mobType.getBaseDamage();
            playAnimation("attack", AnimationIterator.Type.PLAY_ONCE);
            target.damage(dmg, hitboxEntity);
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1.0f, 0.8f);
        }
    }

    @Override
    protected void onDamage(double amount, Player source) {
        playAnimation("hurt", AnimationIterator.Type.PLAY_ONCE);
    }

    @Override
    protected void onDeath(Player killer) {
        if (hitboxEntity != null) {
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_ZOMBIE_DEATH, 1.0f, 0.5f);
        }
        if (killer != null) {
            killer.giveExp(45);
        }
    }

    @Override
    protected List<ItemStack> getDrops(Player killer) {
        List<ItemStack> drops = new ArrayList<>();
        ItemStack trollHide = new ItemStack(Material.LEATHER, 1 + (int) (Math.random() * 3));
        ItemMeta meta = trollHide.getItemMeta();
        meta.displayName(Component.text("Troll Hide", NamedTextColor.GREEN));
        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "custom_item"), PersistentDataType.STRING, "troll_hide");
        trollHide.setItemMeta(meta);
        drops.add(trollHide);
        return drops;
    }

    @Override
    protected long getTickRate() {
        return 5L;
    }
}

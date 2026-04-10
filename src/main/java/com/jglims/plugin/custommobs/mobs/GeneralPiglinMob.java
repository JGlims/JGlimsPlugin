package com.jglims.plugin.custommobs.mobs;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.custommobs.CustomMobEntity;
import com.jglims.plugin.custommobs.CustomMobType;
import kr.toxicity.model.api.animation.AnimationIterator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
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
 * General Piglin - mini-boss guarding bastion treasure rooms.
 * HP 200. Golden axe combo, war cry (buffs piglins), shield bash.
 * Drops 25% RARE legendary, gold blocks, golden apple.
 */
public class GeneralPiglinMob extends CustomMobEntity {

    private long lastComboTick = 0;
    private long lastWarCryTick = 0;
    private long lastBashTick = 0;
    private int comboHits = 0;

    public GeneralPiglinMob(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.GENERAL_PIGLIN);
    }

    @Override
    protected void onSpawn() {
        playAnimation("idle", AnimationIterator.Type.LOOP);
    }

    @Override
    protected void onTick() {
        if (hitboxEntity == null || !alive) return;

        Player target = findNearestPlayer(24);
        if (target == null) {
            state = MobState.IDLE;
            comboHits = 0;
            return;
        }

        state = MobState.ATTACKING;
        if (hitboxEntity instanceof org.bukkit.entity.Mob mob) {
            mob.setTarget(target);
        }

        double dist = hitboxEntity.getLocation().distanceSquared(target.getLocation());
        long ticks = hitboxEntity.getTicksLived();

        // War Cry - buff nearby piglins with Speed I, 12s cooldown
        if (ticks - lastWarCryTick >= 240) {
            lastWarCryTick = ticks;
            playAnimation("roar", AnimationIterator.Type.PLAY_ONCE);
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_PIGLIN_BRUTE_ANGRY, 2.0f, 0.6f);
            // Buff nearby piglins
            for (org.bukkit.entity.Entity entity : hitboxEntity.getNearbyEntities(15, 10, 15)) {
                if (entity instanceof LivingEntity le &&
                        (entity.getType() == org.bukkit.entity.EntityType.PIGLIN
                                || entity.getType() == org.bukkit.entity.EntityType.PIGLIN_BRUTE)) {
                    le.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 0, false, false));
                    le.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 200, 0, false, false));
                }
            }
        }

        // Shield Bash - close range knockback + stun, 5s cooldown
        if (dist < 12 && ticks - lastBashTick >= 100) {
            lastBashTick = ticks;
            playAnimation("bash", AnimationIterator.Type.PLAY_ONCE);
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ITEM_SHIELD_BLOCK, 2.0f, 0.6f);
            target.damage(8.0, hitboxEntity);
            Vector knockback = target.getLocation().toVector().subtract(hitboxEntity.getLocation().toVector()).normalize();
            target.setVelocity(knockback.multiply(1.2).setY(0.5));
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 2, false, false));
        }

        // Golden Axe Combo - 3-hit sequence, 2s between combos
        if (dist < 9 && ticks - lastComboTick >= 15) {
            lastComboTick = ticks;
            comboHits++;
            String anim = comboHits <= 2 ? "attack" : "heavy_attack";
            playAnimation(anim, AnimationIterator.Type.PLAY_ONCE);

            double dmg = comboHits >= 3 ? mobType.getBaseDamage() * 1.5 : mobType.getBaseDamage();
            target.damage(dmg, hitboxEntity);
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                    Sound.ENTITY_PIGLIN_BRUTE_HURT, 1.0f, 0.8f);

            if (comboHits >= 3) {
                comboHits = 0;
                lastComboTick = ticks + 25; // Longer pause after full combo
            }
        }
    }

    @Override
    protected void onDamage(double amount, Player source) {
        playAnimation("hurt", AnimationIterator.Type.PLAY_ONCE);
        hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_PIGLIN_BRUTE_HURT, 1.0f, 0.7f);
    }

    @Override
    protected void onDeath(Player killer) {
        if (hitboxEntity != null) {
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_PIGLIN_BRUTE_DEATH, 2.0f, 0.6f);
            hitboxEntity.getWorld().spawnParticle(Particle.FLAME, hitboxEntity.getLocation().add(0, 1, 0), 30, 1, 1, 1, 0.05);
        }
        if (killer != null) {
            killer.giveExp(200);
        }
    }

    @Override
    protected List<ItemStack> getDrops(Player killer) {
        List<ItemStack> drops = new ArrayList<>();
        drops.add(new ItemStack(Material.GOLD_BLOCK, 1 + (int) (Math.random() * 3)));
        drops.add(new ItemStack(Material.GOLDEN_APPLE, 1));

        // 25% chance RARE legendary
        if (Math.random() < 0.25) {
            ItemStack legendary = new ItemStack(Material.NETHER_STAR, 1);
            ItemMeta meta = legendary.getItemMeta();
            meta.displayName(Component.text("Rare Legendary Drop", NamedTextColor.GOLD));
            meta.getPersistentDataContainer().set(
                    new NamespacedKey(plugin, "legendary_drop"), PersistentDataType.STRING, "rare");
            legendary.setItemMeta(meta);
            drops.add(legendary);
        }
        return drops;
    }

    @Override
    protected long getTickRate() {
        return 5L;
    }
}

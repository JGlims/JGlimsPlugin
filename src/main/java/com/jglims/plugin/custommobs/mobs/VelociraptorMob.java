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
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Velociraptor - Jurassic pack hunter.
 * HP 30. Speed I always active. Spawns in groups of 3-5.
 * Coordinated attacks - all target same player. Drops raptor claw (flint renamed), bone.
 */
public class VelociraptorMob extends CustomMobEntity {

    private long lastAttackTick = 0;
    private long lastLeapTick = 0;
    private static final long ATTACK_COOLDOWN = 20L;
    private static final long LEAP_COOLDOWN = 60L;

    public VelociraptorMob(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.VELOCIRAPTOR);
    }

    @Override
    protected void onSpawn() {
        playAnimation("idle", AnimationIterator.Type.LOOP);
        // Always has Speed I
        hitboxEntity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, false, false));
    }

    @Override
    protected void onTick() {
        if (hitboxEntity == null || !alive) return;

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

        // Pounce/Leap attack - 3s cooldown
        if (dist > 9 && dist < 64 && ticks - lastLeapTick >= LEAP_COOLDOWN) {
            lastLeapTick = ticks;
            playAnimation("leap", AnimationIterator.Type.PLAY_ONCE);
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_PHANTOM_FLAP, 1.0f, 1.5f);
            Vector dir = target.getLocation().toVector().subtract(hitboxEntity.getLocation().toVector()).normalize();
            hitboxEntity.setVelocity(dir.multiply(1.2).setY(0.5));
        }

        // Claw attack - fast melee
        if (dist < 9 && ticks - lastAttackTick >= ATTACK_COOLDOWN) {
            lastAttackTick = ticks;
            playAnimation("attack", AnimationIterator.Type.PLAY_ONCE);
            target.damage(mobType.getBaseDamage(), hitboxEntity);
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.3f);
        }
    }

    @Override
    protected void onDamage(double amount, Player source) {
        playAnimation("hurt", AnimationIterator.Type.PLAY_ONCE);
        hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_FOX_HURT, 1.5f, 0.6f);

        // Pack behavior: alert nearby raptors to target the attacker
        if (source != null) {
            for (org.bukkit.entity.Entity entity : hitboxEntity.getNearbyEntities(15, 10, 15)) {
                if (entity instanceof org.bukkit.entity.Mob mob && entity.isInvisible()
                        && entity.getPersistentDataContainer().has(
                        new NamespacedKey(plugin, "custom_mob_type"), PersistentDataType.STRING)) {
                    String type = entity.getPersistentDataContainer().get(
                            new NamespacedKey(plugin, "custom_mob_type"), PersistentDataType.STRING);
                    if ("velociraptor".equals(type)) {
                        mob.setTarget(source);
                    }
                }
            }
        }
    }

    @Override
    protected void onDeath(Player killer) {
        if (hitboxEntity != null) {
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_FOX_DEATH, 1.0f, 0.5f);
        }
        if (killer != null) {
            killer.giveExp(20);
        }
    }

    @Override
    protected List<ItemStack> getDrops(Player killer) {
        List<ItemStack> drops = new ArrayList<>();
        // Raptor Claw (renamed flint)
        ItemStack claw = new ItemStack(Material.FLINT, 1);
        ItemMeta meta = claw.getItemMeta();
        meta.displayName(Component.text("Raptor Claw", NamedTextColor.YELLOW));
        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "custom_item"), PersistentDataType.STRING, "raptor_claw");
        claw.setItemMeta(meta);
        drops.add(claw);
        drops.add(new ItemStack(Material.BONE, 1 + (int) (Math.random() * 2)));
        return drops;
    }

    @Override
    protected long getTickRate() {
        return 5L;
    }
}

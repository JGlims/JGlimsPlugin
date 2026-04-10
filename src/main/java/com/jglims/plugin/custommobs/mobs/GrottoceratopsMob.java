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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Grottoceratops - Passive herbivore like a cow. If attacked, charges with
 * horns (12 dmg). Spawns in groups of 3-6. Drops leather, bone, horn (renamed bone).
 */
public class GrottoceratopsMob extends CustomMobEntity {

    private UUID provokerUUID;
    private long provokedUntilTick = 0;
    private long lastAttackTick = 0;
    private long lastChargeTick = 0;
    private static final long ATTACK_COOLDOWN = 25L;
    private static final long CHARGE_COOLDOWN = 60L;

    public GrottoceratopsMob(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.GROTTOCERATOPS);
    }

    @Override
    protected void onSpawn() {
        playAnimation("idle", AnimationIterator.Type.LOOP);
    }

    @Override
    protected void onTick() {
        if (hitboxEntity == null || !alive) return;

        if (provokerUUID == null) return;

        long ticks = hitboxEntity.getTicksLived();
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

        // Horn charge from distance
        if (dist > 25 && dist < 196 && canCharge()) {
            lastChargeTick = ticks;
            playAnimation("attack", AnimationIterator.Type.PLAY_ONCE);
            Vector charge = target.getLocation().toVector()
                    .subtract(hitboxEntity.getLocation().toVector()).normalize().multiply(1.8);
            charge.setY(0.1);
            hitboxEntity.setVelocity(charge);
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                    Sound.ENTITY_RAVAGER_ROAR, 1.0f, 0.8f);
        }

        // Horn gore melee
        if (dist < 12 && canAttack()) {
            lastAttackTick = ticks;
            playAnimation("attack", AnimationIterator.Type.PLAY_ONCE);
            target.damage(12, hitboxEntity);
            Vector knockback = target.getLocation().toVector()
                    .subtract(hitboxEntity.getLocation().toVector()).normalize().multiply(1.2);
            knockback.setY(0.5);
            target.setVelocity(knockback);
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                    Sound.ENTITY_RAVAGER_ATTACK, 1.0f, 0.7f);
        }
    }

    @Override
    protected void onDamage(double amount, Player source) {
        playAnimation("hurt", AnimationIterator.Type.PLAY_ONCE);
        hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                Sound.ENTITY_RAVAGER_HURT, 0.8f, 0.7f);
        if (source != null) {
            provokerUUID = source.getUniqueId();
            provokedUntilTick = hitboxEntity.getTicksLived() + 200;
        }
    }

    @Override
    protected void onDeath(Player killer) {
        if (hitboxEntity != null) {
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                    Sound.ENTITY_RAVAGER_DEATH, 0.8f, 0.7f);
        }
        if (killer != null) {
            killer.giveExp(25);
        }
    }

    @Override
    protected List<ItemStack> getDrops(Player killer) {
        List<ItemStack> drops = new ArrayList<>();
        drops.add(new ItemStack(Material.LEATHER, 2 + (int) (Math.random() * 3)));
        drops.add(new ItemStack(Material.BONE, 1 + (int) (Math.random() * 2)));

        // Horn (renamed bone)
        if (Math.random() < 0.5) {
            ItemStack horn = new ItemStack(Material.BONE, 1);
            ItemMeta meta = horn.getItemMeta();
            meta.displayName(Component.text("Grottoceratops Horn", NamedTextColor.YELLOW));
            meta.getPersistentDataContainer().set(
                    new NamespacedKey(plugin, "custom_item"), PersistentDataType.STRING, "grottoceratops_horn");
            horn.setItemMeta(meta);
            drops.add(horn);
        }

        return drops;
    }

    private boolean canAttack() {
        return hitboxEntity.getTicksLived() - lastAttackTick >= ATTACK_COOLDOWN;
    }

    private boolean canCharge() {
        return hitboxEntity.getTicksLived() - lastChargeTick >= CHARGE_COOLDOWN;
    }

    @Override
    protected long getTickRate() {
        return 5L;
    }
}

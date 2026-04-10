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
 * Grizzly Bear - Neutral mob. Peaceful until provoked or if cubs are nearby.
 * Has claw swipe and standing charge attacks.
 * Drops raw beef, leather, and bear claw (renamed bone).
 */
public class GrizzlyBearMob extends CustomMobEntity {

    private UUID provokerUUID;
    private long lastAttackTick = 0;
    private long provokedUntilTick = 0;
    private boolean standing = false;
    private static final long ATTACK_COOLDOWN = 20L;
    private static final long CHARGE_COOLDOWN = 60L;
    private long lastChargeTick = 0;

    public GrizzlyBearMob(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.GRIZZLY_BEAR);
    }

    @Override
    protected void onSpawn() {
        playAnimation("idle", AnimationIterator.Type.LOOP);
    }

    @Override
    protected void onTick() {
        if (hitboxEntity == null || !alive) return;

        // Check if still provoked
        if (provokerUUID != null && hitboxEntity.getTicksLived() > provokedUntilTick) {
            provokerUUID = null;
            state = MobState.IDLE;
            if (hitboxEntity instanceof Mob mob) {
                mob.setTarget(null);
            }
            playAnimation("idle", AnimationIterator.Type.LOOP);
            return;
        }

        if (provokerUUID != null) {
            Player target = plugin.getServer().getPlayer(provokerUUID);
            if (target == null || !target.isOnline()
                    || target.getLocation().distanceSquared(hitboxEntity.getLocation()) > 900) {
                provokerUUID = null;
                state = MobState.IDLE;
                return;
            }

            state = MobState.ATTACKING;
            if (hitboxEntity instanceof Mob mob) {
                mob.setTarget(target);
            }

            double dist = hitboxEntity.getLocation().distanceSquared(target.getLocation());

            // Standing charge attack when far
            if (dist > 25 && dist < 144 && canCharge()) {
                lastChargeTick = hitboxEntity.getTicksLived();
                standing = true;
                playAnimation("roar", AnimationIterator.Type.PLAY_ONCE);
                hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                        Sound.ENTITY_POLAR_BEAR_WARNING, 1.5f, 0.7f);

                // Charge toward player
                Vector dir = target.getLocation().toVector()
                        .subtract(hitboxEntity.getLocation().toVector()).normalize().multiply(1.2);
                dir.setY(0.15);
                hitboxEntity.setVelocity(dir);
            }

            // Claw swipe melee
            if (dist < 9 && canAttack()) {
                lastAttackTick = hitboxEntity.getTicksLived();
                playAnimation("attack", AnimationIterator.Type.PLAY_ONCE);
                target.damage(mobType.getBaseDamage(), hitboxEntity);
                target.setVelocity(hitboxEntity.getLocation().getDirection().normalize().multiply(0.8));
                hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                        Sound.ENTITY_POLAR_BEAR_HURT, 1.0f, 0.8f);
            }
        }
    }

    @Override
    protected void onDamage(double amount, Player source) {
        playAnimation("hurt", AnimationIterator.Type.PLAY_ONCE);
        hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                Sound.ENTITY_POLAR_BEAR_HURT, 1.0f, 0.8f);

        if (source != null) {
            provokerUUID = source.getUniqueId();
            provokedUntilTick = hitboxEntity.getTicksLived() + 200; // 10 seconds aggro
        }
    }

    @Override
    protected void onDeath(Player killer) {
        if (hitboxEntity != null) {
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                    Sound.ENTITY_POLAR_BEAR_DEATH, 1.0f, 0.8f);
        }
        if (killer != null) {
            killer.giveExp(25);
        }
    }

    @Override
    protected List<ItemStack> getDrops(Player killer) {
        List<ItemStack> drops = new ArrayList<>();
        drops.add(new ItemStack(Material.BEEF, 2 + (int) (Math.random() * 3)));
        drops.add(new ItemStack(Material.LEATHER, 1 + (int) (Math.random() * 2)));

        // Bear Claw (renamed bone)
        ItemStack claw = new ItemStack(Material.BONE, 1);
        ItemMeta meta = claw.getItemMeta();
        meta.displayName(Component.text("Bear Claw", NamedTextColor.GOLD));
        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "custom_item"), PersistentDataType.STRING, "bear_claw");
        claw.setItemMeta(meta);
        drops.add(claw);

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

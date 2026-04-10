package com.jglims.plugin.custommobs.mobs;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.custommobs.CustomMobEntity;
import com.jglims.plugin.custommobs.CustomMobType;
import kr.toxicity.model.api.animation.AnimationIterator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
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
 * Invaderling Rider - mounted Lunar soldier.
 * HP 60. Mounted on invisible horse. Charge attack. When mount dies, fights on foot stronger.
 * Drops lunar fragment x2, lunar beast hide (leather renamed).
 */
public class InvaderlingRiderMob extends CustomMobEntity {

    private Horse mount;
    private boolean mounted = true;
    private long lastChargeTick = 0;
    private long lastAttackTick = 0;
    private static final long CHARGE_COOLDOWN = 80L;
    private static final long ATTACK_COOLDOWN = 20L;

    public InvaderlingRiderMob(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.INVADERLING_RIDER);
    }

    @Override
    protected void onSpawn() {
        playAnimation("mounted_idle", AnimationIterator.Type.LOOP);
        // Spawn invisible horse mount
        if (hitboxEntity != null) {
            mount = (Horse) hitboxEntity.getWorld().spawnEntity(hitboxEntity.getLocation(), EntityType.HORSE);
            mount.setInvisible(true);
            mount.setSilent(true);
            mount.setTamed(true);
            mount.setAdult();
            mount.getInventory().setSaddle(new ItemStack(Material.SADDLE));
            mount.addPassenger(hitboxEntity);
            mount.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, false));
        }
    }

    @Override
    protected void onTick() {
        if (hitboxEntity == null || !alive) return;

        // Check if mount died
        if (mounted && (mount == null || mount.isDead())) {
            dismount();
        }

        Player target = findNearestPlayer(20);
        if (target == null) {
            state = MobState.IDLE;
            return;
        }

        state = MobState.ATTACKING;
        double dist = hitboxEntity.getLocation().distanceSquared(target.getLocation());
        long ticks = hitboxEntity.getTicksLived();

        if (mounted) {
            // Mounted: use mount for pathfinding
            if (mount != null) {
                mount.setTarget(target);
            }

            // Charge attack while mounted
            if (dist > 36 && dist < 400 && ticks - lastChargeTick >= CHARGE_COOLDOWN) {
                lastChargeTick = ticks;
                playAnimation("mounted_charge", AnimationIterator.Type.PLAY_ONCE);
                hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_HORSE_GALLOP, 2.0f, 1.2f);
                if (mount != null) {
                    Vector dir = target.getLocation().toVector().subtract(mount.getLocation().toVector()).normalize();
                    mount.setVelocity(dir.multiply(2.0).setY(0.2));
                }
            }

            // Mounted melee
            if (dist < 12 && ticks - lastAttackTick >= ATTACK_COOLDOWN) {
                lastAttackTick = ticks;
                playAnimation("mounted_attack", AnimationIterator.Type.PLAY_ONCE);
                target.damage(mobType.getBaseDamage(), hitboxEntity);
                hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.0f);
            }
        } else {
            // On foot: stronger, uses zombie AI
            if (hitboxEntity instanceof org.bukkit.entity.Mob mob) {
                mob.setTarget(target);
            }
            if (dist < 9 && ticks - lastAttackTick >= ATTACK_COOLDOWN) {
                lastAttackTick = ticks;
                playAnimation("attack", AnimationIterator.Type.PLAY_ONCE);
                target.damage(mobType.getBaseDamage() * 1.3, hitboxEntity);
                hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 1.0f);
            }
        }
    }

    private void dismount() {
        mounted = false;
        playAnimation("dismount", AnimationIterator.Type.PLAY_ONCE);
        hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_HORSE_DEATH, 1.0f, 1.2f);
        hitboxEntity.customName(Component.text("Dismounted Invaderling Rider", NamedTextColor.RED));
        // Buff on dismount - rage
        hitboxEntity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, false, false));

        for (Player p : findNearbyPlayers(15)) {
            p.sendMessage(Component.text("The Invaderling Rider dismounts in fury!", NamedTextColor.AQUA));
        }
    }

    @Override
    protected void onDamage(double amount, Player source) {
        playAnimation("hurt", AnimationIterator.Type.PLAY_ONCE);
        // Damage mount too if mounted
        if (mounted && mount != null && !mount.isDead()) {
            mount.damage(amount * 0.5);
        }
    }

    @Override
    protected void onDeath(Player killer) {
        if (mount != null && !mount.isDead()) {
            mount.remove();
        }
        if (hitboxEntity != null) {
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_VEX_DEATH, 1.0f, 1.0f);
        }
        if (killer != null) {
            killer.giveExp(50);
        }
    }

    @Override
    protected List<ItemStack> getDrops(Player killer) {
        List<ItemStack> drops = new ArrayList<>();
        // Lunar Fragment x2
        ItemStack lunarFrag = new ItemStack(Material.IRON_NUGGET, 2 + (int) (Math.random() * 2));
        ItemMeta meta = lunarFrag.getItemMeta();
        meta.displayName(Component.text("Lunar Fragment", NamedTextColor.AQUA));
        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "custom_item"), PersistentDataType.STRING, "lunar_fragment");
        lunarFrag.setItemMeta(meta);
        drops.add(lunarFrag);

        // Lunar Beast Hide (renamed leather)
        ItemStack hide = new ItemStack(Material.LEATHER, 1);
        ItemMeta hideMeta = hide.getItemMeta();
        hideMeta.displayName(Component.text("Lunar Beast Hide", NamedTextColor.DARK_AQUA));
        hideMeta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "custom_item"), PersistentDataType.STRING, "lunar_beast_hide");
        hide.setItemMeta(hideMeta);
        drops.add(hide);
        return drops;
    }

    @Override
    protected long getTickRate() {
        return 5L;
    }
}

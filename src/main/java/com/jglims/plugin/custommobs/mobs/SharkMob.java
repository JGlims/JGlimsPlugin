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
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Shark - hostile ocean predator.
 * HP 40. Patrols ocean, detects players within 15 blocks (30 if below 50% HP).
 * Bite + charge attacks. Drops shark tooth (flint renamed), raw cod.
 */
public class SharkMob extends CustomMobEntity {

    private long lastBiteTick = 0;
    private long lastChargeTick = 0;
    private static final long BITE_COOLDOWN = 25L;
    private static final long CHARGE_COOLDOWN = 80L;

    public SharkMob(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.SHARK);
    }

    @Override
    protected void onSpawn() {
        playAnimation("swim", AnimationIterator.Type.LOOP);
    }

    @Override
    protected void onTick() {
        if (hitboxEntity == null || !alive) return;

        // Blood in the water - extended detection when player is low HP
        double detectionRange = 15;
        Player target = findNearestPlayer(detectionRange);
        if (target == null) {
            // Check extended range for bleeding players
            target = findNearestPlayer(30);
            if (target != null && target.getHealth() > target.getMaxHealth() * 0.5) {
                target = null; // Only detect at long range if they're hurt
            }
        }

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

        // Charge attack - fast rush
        if (dist > 25 && dist < 225 && ticks - lastChargeTick >= CHARGE_COOLDOWN) {
            lastChargeTick = ticks;
            playAnimation("charge", AnimationIterator.Type.PLAY_ONCE);
            Vector dir = target.getLocation().toVector().subtract(hitboxEntity.getLocation().toVector()).normalize();
            hitboxEntity.setVelocity(dir.multiply(1.5));
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_DOLPHIN_SWIM, 2.0f, 0.5f);
        }

        // Bite attack
        if (dist < 9 && ticks - lastBiteTick >= BITE_COOLDOWN) {
            lastBiteTick = ticks;
            playAnimation("bite", AnimationIterator.Type.PLAY_ONCE);
            target.damage(mobType.getBaseDamage(), hitboxEntity);
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_PHANTOM_BITE, 1.5f, 0.6f);
        }
    }

    @Override
    protected void onDamage(double amount, Player source) {
        playAnimation("hurt", AnimationIterator.Type.PLAY_ONCE);
    }

    @Override
    protected void onDeath(Player killer) {
        if (hitboxEntity != null) {
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_DOLPHIN_DEATH, 1.0f, 0.5f);
        }
        if (killer != null) {
            killer.giveExp(30);
        }
    }

    @Override
    protected List<ItemStack> getDrops(Player killer) {
        List<ItemStack> drops = new ArrayList<>();
        // Shark Tooth (renamed flint)
        ItemStack tooth = new ItemStack(Material.FLINT, 1 + (int) (Math.random() * 2));
        ItemMeta meta = tooth.getItemMeta();
        meta.displayName(Component.text("Shark Tooth", NamedTextColor.WHITE));
        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "custom_item"), PersistentDataType.STRING, "shark_tooth");
        tooth.setItemMeta(meta);
        drops.add(tooth);
        drops.add(new ItemStack(Material.COD, 1 + (int) (Math.random() * 3)));
        return drops;
    }

    @Override
    protected long getTickRate() {
        return 5L;
    }
}

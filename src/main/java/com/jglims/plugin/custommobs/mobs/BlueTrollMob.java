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

import java.util.ArrayList;
import java.util.List;

/**
 * Blue Troll - slow, tough melee mob found in the Aether.
 * HP 40, basic melee attacks with knockback.
 */
public class BlueTrollMob extends CustomMobEntity {

    private long lastAttackTick = 0;
    private static final long ATTACK_COOLDOWN = 30L; // 1.5 seconds

    public BlueTrollMob(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.BLUE_TROLL);
    }

    @Override
    protected void onSpawn() {
        playAnimation("idle", AnimationIterator.Type.LOOP);
        plugin.getLogger().info("[BlueTroll] Spawned at " + formatLocation(hitboxEntity.getLocation()));
    }

    @Override
    protected void onTick() {
        if (hitboxEntity == null || !alive) return;

        Player target = findNearestPlayer(16);
        if (target == null) {
            state = MobState.IDLE;
            return;
        }

        state = MobState.ATTACKING;
        double dist = hitboxEntity.getLocation().distanceSquared(target.getLocation());

        // Move toward target (vanilla pathfinder handles this via zombie AI)
        if (hitboxEntity instanceof org.bukkit.entity.Mob mob) {
            mob.setTarget(target);
        }

        // Melee attack within 3 blocks
        if (dist < 9 && canAttack()) {
            lastAttackTick = hitboxEntity.getTicksLived();
            playAnimation("attack", AnimationIterator.Type.PLAY_ONCE);
            target.damage(mobType.getBaseDamage(), hitboxEntity);
            target.setVelocity(hitboxEntity.getLocation().getDirection().normalize().multiply(0.6));
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, 1.0f, 0.7f);
        }
    }

    private boolean canAttack() {
        return hitboxEntity.getTicksLived() - lastAttackTick >= ATTACK_COOLDOWN;
    }

    @Override
    protected void onDamage(double amount, Player source) {
        playAnimation("hurt", AnimationIterator.Type.PLAY_ONCE);
        hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_ZOMBIE_HURT, 1.0f, 0.6f);
    }

    @Override
    protected void onDeath(Player killer) {
        if (hitboxEntity != null) {
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_ZOMBIE_DEATH, 1.0f, 0.6f);
        }
        if (killer != null) {
            killer.giveExp(30);
        }
    }

    @Override
    protected List<ItemStack> getDrops(Player killer) {
        List<ItemStack> drops = new ArrayList<>();
        // Troll Hide (renamed leather)
        ItemStack trollHide = new ItemStack(Material.LEATHER, 1 + (int) (Math.random() * 2));
        ItemMeta meta = trollHide.getItemMeta();
        meta.displayName(Component.text("Troll Hide", NamedTextColor.BLUE));
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

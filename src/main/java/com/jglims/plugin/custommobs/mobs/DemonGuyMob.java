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
 * Demon Guy - Nether demon, neutral if player wears full diamond armor.
 * HP 80. Give it a diamond to barter a random item back.
 */
public class DemonGuyMob extends CustomMobEntity {

    private long lastAttackTick = 0;
    private static final long ATTACK_COOLDOWN = 25L;

    public DemonGuyMob(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.DEMON_GUY);
    }

    @Override
    protected void onSpawn() {
        playAnimation("idle", AnimationIterator.Type.LOOP);
    }

    @Override
    protected void onTick() {
        if (hitboxEntity == null || !alive) return;

        Player target = findNearestPlayer(16);
        if (target == null) {
            state = MobState.IDLE;
            return;
        }

        // Neutral if player wears full diamond armor
        if (isWearingFullDiamond(target)) {
            state = MobState.IDLE;
            if (hitboxEntity instanceof org.bukkit.entity.Mob mob) {
                mob.setTarget(null);
            }
            return;
        }

        state = MobState.ATTACKING;
        if (hitboxEntity instanceof org.bukkit.entity.Mob mob) {
            mob.setTarget(target);
        }

        double dist = hitboxEntity.getLocation().distanceSquared(target.getLocation());
        long ticks = hitboxEntity.getTicksLived();

        if (dist < 9 && ticks - lastAttackTick >= ATTACK_COOLDOWN) {
            lastAttackTick = ticks;
            playAnimation("attack", AnimationIterator.Type.PLAY_ONCE);
            target.damage(mobType.getBaseDamage(), hitboxEntity);
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_BLAZE_HURT, 1.0f, 0.8f);
        }
    }

    private boolean isWearingFullDiamond(Player player) {
        var equipment = player.getInventory();
        return equipment.getHelmet() != null && equipment.getHelmet().getType() == Material.DIAMOND_HELMET
                && equipment.getChestplate() != null && equipment.getChestplate().getType() == Material.DIAMOND_CHESTPLATE
                && equipment.getLeggings() != null && equipment.getLeggings().getType() == Material.DIAMOND_LEGGINGS
                && equipment.getBoots() != null && equipment.getBoots().getType() == Material.DIAMOND_BOOTS;
    }

    @Override
    public void onInteract(Player player) {
        if (hitboxEntity == null) return;

        // Barter: player gives diamond, gets random item back
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand.getType() == Material.DIAMOND && hand.getAmount() >= 1) {
            hand.setAmount(hand.getAmount() - 1);
            playAnimation("barter", AnimationIterator.Type.PLAY_ONCE);
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_PIGLIN_CELEBRATE, 1.0f, 0.7f);

            ItemStack reward = getBarterReward();
            player.getInventory().addItem(reward).values()
                    .forEach(overflow -> player.getWorld().dropItemNaturally(player.getLocation(), overflow));
            player.sendMessage(Component.text("The Demon accepts your diamond.", NamedTextColor.RED));
        }
    }

    private ItemStack getBarterReward() {
        Material[] rewards = {
                Material.GOLD_INGOT, Material.IRON_INGOT, Material.BLAZE_ROD,
                Material.FIRE_CHARGE, Material.ENDER_PEARL, Material.OBSIDIAN,
                Material.MAGMA_CREAM, Material.GOLDEN_APPLE
        };
        Material mat = rewards[(int) (Math.random() * rewards.length)];
        int amount = mat == Material.GOLDEN_APPLE ? 1 : 1 + (int) (Math.random() * 4);
        return new ItemStack(mat, amount);
    }

    @Override
    protected void onDamage(double amount, Player source) {
        playAnimation("hurt", AnimationIterator.Type.PLAY_ONCE);
        hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_BLAZE_HURT, 1.0f, 0.6f);
    }

    @Override
    protected void onDeath(Player killer) {
        if (hitboxEntity != null) {
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_BLAZE_DEATH, 1.0f, 0.6f);
        }
        if (killer != null) {
            killer.giveExp(60);
        }
    }

    @Override
    protected List<ItemStack> getDrops(Player killer) {
        List<ItemStack> drops = new ArrayList<>();
        drops.add(new ItemStack(Material.BLAZE_ROD, 1 + (int) (Math.random() * 3)));
        return drops;
    }

    @Override
    protected long getTickRate() {
        return 5L;
    }
}

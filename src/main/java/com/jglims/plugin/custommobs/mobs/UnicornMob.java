package com.jglims.plugin.custommobs.mobs;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.custommobs.CustomMobType;
import com.jglims.plugin.custommobs.RideableMobEntity;
import kr.toxicity.model.api.animation.AnimationIterator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Unicorn - Fast rideable horse-type mob. Faster than regular horses (0.3 speed).
 * Taming: approach slowly, offer golden apple, then mount to tame.
 * Sparkle particles when running.
 */
public class UnicornMob extends RideableMobEntity {

    private boolean goldenAppleFed = false;

    public UnicornMob(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.UNICORN);
        this.canFly = false;
        this.mountSpeed = 0.3;
        this.jumpStrength = 1.5;
        this.maxRiders = 1;
        this.tamingRequirement = 1.0;
        this.fallDamageReduction = 0.0;
    }

    @Override
    protected void onSpawn() {
        playAnimation("idle", AnimationIterator.Type.LOOP);
    }

    @Override
    protected void onTick() {
        super.onTick();
        if (hitboxEntity == null || !alive) return;

        // Sparkle particles when moving and has passengers
        if (tamed && hitboxEntity.getPassengers().size() > 0) {
            if (hitboxEntity.getVelocity().lengthSquared() > 0.01) {
                hitboxEntity.getWorld().spawnParticle(Particle.END_ROD,
                        hitboxEntity.getLocation().add(0, 0.5, 0),
                        3, 0.3, 0.2, 0.3, 0.02);
            }
        }
    }

    @Override
    public void onInteract(Player player) {
        if (tamed) {
            super.onInteract(player);
            return;
        }

        // Step 1: Feed golden apple
        if (!goldenAppleFed) {
            ItemStack hand = player.getInventory().getItemInMainHand();
            if (hand.getType() == Material.GOLDEN_APPLE) {
                hand.setAmount(hand.getAmount() - 1);
                goldenAppleFed = true;
                hitboxEntity.getWorld().spawnParticle(Particle.HEART,
                        hitboxEntity.getLocation().add(0, 1.5, 0), 8, 0.5, 0.3, 0.5, 0);
                hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                        Sound.ENTITY_HORSE_EAT, 1.0f, 1.2f);
                player.sendMessage(Component.text("The Unicorn accepts your offering! Try to mount it.",
                        NamedTextColor.LIGHT_PURPLE));
            } else {
                player.sendMessage(Component.text("The Unicorn eyes you cautiously. Try offering a golden apple.",
                        NamedTextColor.GRAY));
            }
            return;
        }

        // Step 2: Mount to tame
        if (!tamed) {
            // Check player is approaching slowly (not sprinting)
            if (player.isSprinting()) {
                player.sendMessage(Component.text("Approach slowly! The Unicorn is skittish.",
                        NamedTextColor.YELLOW));
                return;
            }
            addTamingProgress(1.0, player);
            if (tamed) {
                saddled = true; // Unicorns don't need a separate saddle
            }
        }
    }

    @Override
    protected void onTamed(Player player) {
        if (hitboxEntity != null) {
            hitboxEntity.customName(Component.text(player.getName() + "'s Unicorn",
                    NamedTextColor.LIGHT_PURPLE));
        }
    }

    @Override
    protected void onMount(Player player) {
        playAnimation("walk_mounted", AnimationIterator.Type.LOOP);
        player.sendMessage(Component.text("The Unicorn gallops with magical grace!",
                NamedTextColor.LIGHT_PURPLE));
    }

    @Override
    protected void onDismount(Player player) {
        playAnimation("idle", AnimationIterator.Type.LOOP);
    }

    @Override
    protected void onDamage(double amount, Player source) {
        playAnimation("hurt", AnimationIterator.Type.PLAY_ONCE);
        hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                Sound.ENTITY_HORSE_HURT, 1.0f, 1.2f);
    }

    @Override
    protected void onDeath(Player killer) {
        if (hitboxEntity != null) {
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                    Sound.ENTITY_HORSE_DEATH, 1.0f, 1.2f);
            hitboxEntity.getWorld().spawnParticle(Particle.END_ROD,
                    hitboxEntity.getLocation().add(0, 1, 0), 30, 1, 1, 1, 0.05);
        }
        if (killer != null) {
            killer.giveExp(30);
        }
    }

    @Override
    protected List<ItemStack> getDrops(Player killer) {
        List<ItemStack> drops = new ArrayList<>();
        drops.add(new ItemStack(Material.LEATHER, 2 + (int) (Math.random() * 2)));
        if (Math.random() < 0.2) {
            drops.add(new ItemStack(Material.GOLDEN_APPLE, 1));
        }
        return drops;
    }

    @Override
    protected long getTickRate() {
        return 5L;
    }
}

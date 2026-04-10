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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Leathern Drake - Desert drake, passive until attacked. Tameable by dropping
 * 5 rotten flesh near it, waiting for it to eat, then applying a saddle.
 * 2 riders, 2 chest slots (18 slots). Slower than horse (0.15 speed).
 * Special ability: powerful vertical leap (3x horse jump). Cannot fly.
 */
public class LeathernDrakeMob extends RideableMobEntity {

    private int fleshEaten = 0;
    private static final int FLESH_TO_TAME = 5;
    private boolean readyForSaddle = false;
    private UUID provokerUUID;
    private long provokedUntilTick = 0;
    private long lastMeleeAttackTick = 0;

    public LeathernDrakeMob(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.LEATHERN_DRAKE);
        this.canFly = false;
        this.mountSpeed = 0.15;
        this.jumpStrength = 1.0;
        this.maxRiders = 2;
        this.chestSlots = 18;
        this.tamingRequirement = 5.0;
        this.fallDamageReduction = 0.8;
        this.specialAbilityCooldownTicks = 60L; // 3 second cooldown
    }

    @Override
    protected void onSpawn() {
        playAnimation("idle", AnimationIterator.Type.LOOP);
    }

    @Override
    protected void onTick() {
        super.onTick();
        if (hitboxEntity == null || !alive) return;

        // Check for nearby dropped rotten flesh to eat (taming mechanic)
        if (!tamed && !readyForSaddle) {
            checkForFlesh();
        }

        // Aggro behavior if provoked
        if (provokerUUID != null && hitboxEntity.getTicksLived() <= provokedUntilTick) {
            Player target = plugin.getServer().getPlayer(provokerUUID);
            if (target != null && target.isOnline()) {
                if (hitboxEntity instanceof Mob mob) {
                    mob.setTarget(target);
                }
                double dist = hitboxEntity.getLocation().distanceSquared(target.getLocation());
                if (dist < 9 && canMeleeAttack()) {
                    lastMeleeAttackTick = hitboxEntity.getTicksLived();
                    playAnimation("attack", AnimationIterator.Type.PLAY_ONCE);
                    target.damage(mobType.getBaseDamage(), hitboxEntity);
                    hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                            Sound.ENTITY_ENDER_DRAGON_HURT, 0.8f, 1.5f);
                }
            }
        } else {
            provokerUUID = null;
        }
    }

    private void checkForFlesh() {
        if (hitboxEntity == null) return;
        for (Entity entity : hitboxEntity.getNearbyEntities(4, 2, 4)) {
            if (entity instanceof Item item) {
                ItemStack stack = item.getItemStack();
                if (stack.getType() == Material.ROTTEN_FLESH) {
                    int amount = Math.min(stack.getAmount(), FLESH_TO_TAME - fleshEaten);
                    fleshEaten += amount;
                    stack.setAmount(stack.getAmount() - amount);
                    if (stack.getAmount() <= 0) {
                        item.remove();
                    }
                    hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                            Sound.ENTITY_GENERIC_EAT, 1.0f, 0.8f);
                    hitboxEntity.getWorld().spawnParticle(Particle.HAPPY_VILLAGER,
                            hitboxEntity.getLocation().add(0, 1, 0), 5, 0.3, 0.3, 0.3, 0);

                    if (fleshEaten >= FLESH_TO_TAME) {
                        readyForSaddle = true;
                        // Announce to nearby players
                        for (Player p : findNearbyPlayers(10)) {
                            p.sendMessage(Component.text("The Leathern Drake looks content. Apply a saddle!",
                                    NamedTextColor.GOLD));
                        }
                    }
                    break;
                }
            }
        }
    }

    @Override
    public void onInteract(Player player) {
        if (tamed) {
            super.onInteract(player);
            return;
        }

        if (readyForSaddle) {
            ItemStack hand = player.getInventory().getItemInMainHand();
            if (hand.getType() == Material.SADDLE) {
                hand.setAmount(hand.getAmount() - 1);
                addTamingProgress(tamingRequirement, player);
                saddled = true;
            } else {
                player.sendMessage(Component.text("Apply a saddle to tame the drake!", NamedTextColor.GOLD));
            }
        } else {
            player.sendMessage(Component.text("Drop rotten flesh nearby for the drake to eat. ("
                    + fleshEaten + "/" + FLESH_TO_TAME + ")", NamedTextColor.GRAY));
        }
    }

    @Override
    protected void onTamed(Player player) {
        if (hitboxEntity != null) {
            hitboxEntity.customName(Component.text(player.getName() + "'s Leathern Drake",
                    NamedTextColor.GOLD));
        }
    }

    @Override
    protected void onMount(Player player) {
        playAnimation("walk_mounted", AnimationIterator.Type.LOOP);
        player.sendMessage(Component.text("The drake rumbles beneath you!", NamedTextColor.GOLD));
    }

    @Override
    protected void onDismount(Player player) {
        playAnimation("idle", AnimationIterator.Type.LOOP);
    }

    /**
     * Special ability: Powerful vertical leap (3x horse jump height).
     */
    @Override
    protected void onSpecialAbility(Player rider) {
        if (hitboxEntity == null) return;

        // Massive vertical leap
        Vector velocity = hitboxEntity.getVelocity();
        velocity.setY(2.5); // 3x normal horse jump
        hitboxEntity.setVelocity(velocity);

        playAnimation("jump", AnimationIterator.Type.PLAY_ONCE);
        hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                Sound.ENTITY_HORSE_JUMP, 1.5f, 0.6f);
        hitboxEntity.getWorld().spawnParticle(Particle.CLOUD,
                hitboxEntity.getLocation(), 15, 0.5, 0.2, 0.5, 0.1);

        rider.sendMessage(Component.text("The drake leaps!", NamedTextColor.GOLD));
    }

    @Override
    protected void onDamage(double amount, Player source) {
        playAnimation("hurt", AnimationIterator.Type.PLAY_ONCE);
        hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                Sound.ENTITY_ENDER_DRAGON_HURT, 0.8f, 1.5f);
        if (source != null && !tamed) {
            provokerUUID = source.getUniqueId();
            provokedUntilTick = hitboxEntity.getTicksLived() + 200;
        }
    }

    @Override
    protected void onDeath(Player killer) {
        if (hitboxEntity != null) {
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                    Sound.ENTITY_ENDER_DRAGON_DEATH, 0.5f, 1.5f);
            // Drop chest contents
            Location loc = hitboxEntity.getLocation();
            for (ItemStack item : chestContents.values()) {
                if (item != null) {
                    loc.getWorld().dropItemNaturally(loc, item);
                }
            }
        }
        if (killer != null) {
            killer.giveExp(40);
        }
    }

    @Override
    protected List<ItemStack> getDrops(Player killer) {
        List<ItemStack> drops = new ArrayList<>();
        drops.add(new ItemStack(Material.LEATHER, 3 + (int) (Math.random() * 3)));
        drops.add(new ItemStack(Material.BONE, 2));
        if (Math.random() < 0.3) {
            drops.add(new ItemStack(Material.SADDLE, 1));
        }
        return drops;
    }

    private boolean canMeleeAttack() {
        return hitboxEntity.getTicksLived() - lastMeleeAttackTick >= 25;
    }

    @Override
    protected long getTickRate() {
        return 5L;
    }
}

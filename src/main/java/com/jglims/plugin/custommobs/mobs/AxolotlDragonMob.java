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
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Axolotl Dragon - Tiny cave dragon, passive. Wanders naturally.
 * Attacks zombies and skeletons automatically. Tameable by feeding
 * tropical fish x3. Once tamed, follows owner and attacks hostiles.
 * Cannot be killed by its owner.
 */
public class AxolotlDragonMob extends CustomMobEntity {

    private boolean tamed = false;
    private UUID ownerUUID;
    private int feedCount = 0;
    private static final int FEEDS_TO_TAME = 3;
    private long lastAttackTick = 0;
    private static final long ATTACK_COOLDOWN = 25L;

    public AxolotlDragonMob(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.AXOLOTL_DRAGON);
    }

    @Override
    protected void onSpawn() {
        playAnimation("idle", AnimationIterator.Type.LOOP);
    }

    @Override
    protected void onTick() {
        if (hitboxEntity == null || !alive) return;

        if (tamed) {
            tamedTick();
        } else {
            wildTick();
        }
    }

    private void wildTick() {
        // Attack nearby zombies and skeletons automatically
        LivingEntity target = findNearestHostile(12);
        if (target != null) {
            if (hitboxEntity instanceof Mob mob) {
                mob.setTarget(target);
            }
            double dist = hitboxEntity.getLocation().distanceSquared(target.getLocation());
            if (dist < 6 && canAttack()) {
                lastAttackTick = hitboxEntity.getTicksLived();
                playAnimation("attack", AnimationIterator.Type.PLAY_ONCE);
                target.damage(mobType.getBaseDamage(), hitboxEntity);
                hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                        Sound.ENTITY_AXOLOTL_ATTACK, 1.0f, 1.2f);
            }
        }
    }

    private void tamedTick() {
        Player owner = getOwner();
        if (owner == null || !owner.isOnline()) return;

        double distToOwner = hitboxEntity.getLocation().distanceSquared(owner.getLocation());

        // Teleport if too far
        if (distToOwner > 400) {
            hitboxEntity.teleport(owner.getLocation().add(1, 0, 1));
            return;
        }

        // Follow owner if moderately far
        if (distToOwner > 16 && hitboxEntity instanceof Mob mob) {
            mob.getPathfinder().moveTo(owner.getLocation(), 1.2);
        }

        // Attack hostiles near owner
        LivingEntity hostile = findNearestHostileNearLocation(owner.getLocation(), 10);
        if (hostile != null) {
            if (hitboxEntity instanceof Mob mob) {
                mob.setTarget(hostile);
            }
            double dist = hitboxEntity.getLocation().distanceSquared(hostile.getLocation());
            if (dist < 6 && canAttack()) {
                lastAttackTick = hitboxEntity.getTicksLived();
                playAnimation("attack", AnimationIterator.Type.PLAY_ONCE);
                hostile.damage(mobType.getBaseDamage(), hitboxEntity);
                hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                        Sound.ENTITY_AXOLOTL_ATTACK, 1.0f, 1.2f);
            }
        }
    }

    @Override
    public void onInteract(Player player) {
        if (tamed) {
            if (isOwner(player)) {
                player.sendMessage(Component.text("Your Axolotl Dragon chirps happily!", NamedTextColor.LIGHT_PURPLE));
            }
            return;
        }

        // Taming: feed tropical fish
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand.getType() == Material.TROPICAL_FISH) {
            hand.setAmount(hand.getAmount() - 1);
            feedCount++;
            hitboxEntity.getWorld().spawnParticle(Particle.HEART,
                    hitboxEntity.getLocation().add(0, 1, 0), 3, 0.3, 0.3, 0.3, 0);
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                    Sound.ENTITY_AXOLOTL_IDLE_AIR, 1.0f, 1.3f);

            if (feedCount >= FEEDS_TO_TAME) {
                tame(player);
            } else {
                player.sendMessage(Component.text("The dragon nibbles the fish... ("
                        + feedCount + "/" + FEEDS_TO_TAME + ")", NamedTextColor.AQUA));
            }
        } else {
            player.sendMessage(Component.text("Try feeding it tropical fish!", NamedTextColor.GRAY));
        }
    }

    private void tame(Player player) {
        tamed = true;
        ownerUUID = player.getUniqueId();
        if (hitboxEntity != null) {
            hitboxEntity.getPersistentDataContainer().set(
                    new NamespacedKey(plugin, "custom_mob_owner"),
                    PersistentDataType.STRING, ownerUUID.toString());
            hitboxEntity.customName(Component.text(player.getName() + "'s Axolotl Dragon",
                    NamedTextColor.LIGHT_PURPLE));
            hitboxEntity.getWorld().spawnParticle(Particle.HEART,
                    hitboxEntity.getLocation().add(0, 1.5, 0), 15, 0.5, 0.5, 0.5, 0);
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                    Sound.ENTITY_AXOLOTL_IDLE_AIR, 1.0f, 1.5f);
        }
        player.sendMessage(Component.text("You have tamed the Axolotl Dragon!", NamedTextColor.GREEN));
    }

    @Override
    protected void onDamage(double amount, Player source) {
        // Cannot be killed by owner
        if (source != null && tamed && isOwner(source)) {
            if (hitboxEntity != null) {
                hitboxEntity.setHealth(Math.min(hitboxEntity.getHealth() + amount, mobType.getMaxHealth()));
            }
            source.sendMessage(Component.text("You can't hurt your own dragon!", NamedTextColor.RED));
            return;
        }
        playAnimation("hurt", AnimationIterator.Type.PLAY_ONCE);
        hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                Sound.ENTITY_AXOLOTL_HURT, 1.0f, 1.0f);
    }

    @Override
    protected void onDeath(Player killer) {
        if (hitboxEntity != null) {
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                    Sound.ENTITY_AXOLOTL_DEATH, 1.0f, 1.0f);
        }
        if (killer != null) {
            killer.giveExp(20);
        }
    }

    @Override
    protected List<ItemStack> getDrops(Player killer) {
        List<ItemStack> drops = new ArrayList<>();
        drops.add(new ItemStack(Material.TROPICAL_FISH, 1 + (int) (Math.random() * 3)));
        if (Math.random() < 0.3) {
            drops.add(new ItemStack(Material.PRISMARINE_SHARD, 1));
        }
        return drops;
    }

    private boolean canAttack() {
        return hitboxEntity.getTicksLived() - lastAttackTick >= ATTACK_COOLDOWN;
    }

    private boolean isOwner(Player player) {
        return ownerUUID != null && ownerUUID.equals(player.getUniqueId());
    }

    private Player getOwner() {
        if (ownerUUID == null) return null;
        return plugin.getServer().getPlayer(ownerUUID);
    }

    private LivingEntity findNearestHostile(double radius) {
        if (hitboxEntity == null) return null;
        return findNearestHostileNearLocation(hitboxEntity.getLocation(), radius);
    }

    private LivingEntity findNearestHostileNearLocation(Location loc, double radius) {
        double radiusSq = radius * radius;
        LivingEntity nearest = null;
        double nearestDist = radiusSq;
        for (Entity e : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
            if (e instanceof Zombie || e instanceof Skeleton || e instanceof Creeper
                    || e instanceof Spider) {
                double dist = e.getLocation().distanceSquared(loc);
                if (dist < nearestDist) {
                    nearestDist = dist;
                    nearest = (LivingEntity) e;
                }
            }
        }
        return nearest;
    }

    @Override
    protected long getTickRate() {
        return 5L;
    }
}

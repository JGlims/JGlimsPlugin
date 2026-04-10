package com.jglims.plugin.custommobs.mobs;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.custommobs.CustomBossEntity;
import com.jglims.plugin.custommobs.CustomMobType;
import com.jglims.plugin.legendary.LegendaryTier;
import com.jglims.plugin.legendary.LegendaryWeapon;
import com.jglims.plugin.legendary.LegendaryWeaponManager;
import kr.toxicity.model.api.animation.AnimationIterator;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Skeleton Dragon Boss - HP 800. Bone shard spray (6 projectiles),
 * tail sweep (melee AoE), dive bomb. No fire breath.
 * Drops: 1 MYTHIC legendary. Boss bar: white.
 */
public class SkeletonDragonBoss extends CustomBossEntity {

    public SkeletonDragonBoss(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.SKELETON_DRAGON);
        this.aggroRadius = 40.0;
        setPhaseThresholds(0.6, 0.3);
    }

    @Override
    protected BossBar.Color getBossBarColor() {
        return BossBar.Color.WHITE;
    }

    @Override
    protected void combatTick() {
        Player target = findNearestPlayer(aggroRadius);
        if (target == null) return;

        if (hitboxEntity instanceof Mob mob) {
            mob.setTarget(target);
        }

        double dist = hitboxEntity.getLocation().distanceSquared(target.getLocation());

        // Bone Shard Spray - 6 projectiles
        if (dist > 9 && dist < 400 && isAttackReady("bone_spray", 50)) {
            useAttack("bone_spray");
            playAnimation("attack", AnimationIterator.Type.PLAY_ONCE);
            Location origin = hitboxEntity.getLocation().add(0, 2, 0);
            Vector baseDir = target.getLocation().add(0, 1, 0).toVector()
                    .subtract(origin.toVector()).normalize();
            Random rand = new Random();

            for (int i = 0; i < 6; i++) {
                Vector spread = baseDir.clone().add(new Vector(
                        (rand.nextDouble() - 0.5) * 0.3,
                        (rand.nextDouble() - 0.5) * 0.2,
                        (rand.nextDouble() - 0.5) * 0.3)).normalize();

                Arrow arrow = hitboxEntity.getWorld().spawn(origin.clone(), Arrow.class);
                arrow.setVelocity(spread.multiply(2.0));
                arrow.setDamage(mobType.getBaseDamage() * 0.5);
                arrow.setShooter(hitboxEntity);
                arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
            }
            hitboxEntity.getWorld().playSound(origin, Sound.ENTITY_SKELETON_SHOOT, 2.0f, 0.5f);
            hitboxEntity.getWorld().spawnParticle(Particle.BLOCK, origin,
                    20, 1, 1, 1, 0, Material.BONE_BLOCK.createBlockData());
        }

        // Tail Sweep - melee AoE
        if (dist < 25 && isAttackReady("tail_sweep", 40)) {
            useAttack("tail_sweep");
            playAnimation("attack", AnimationIterator.Type.PLAY_ONCE);
            for (Player p : findNearbyPlayers(5)) {
                p.damage(mobType.getBaseDamage(), hitboxEntity);
                Vector knockback = p.getLocation().toVector()
                        .subtract(hitboxEntity.getLocation().toVector()).normalize().multiply(1.2);
                knockback.setY(0.5);
                p.setVelocity(knockback);
            }
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                    Sound.ENTITY_IRON_GOLEM_ATTACK, 1.5f, 0.5f);
        }

        // Dive Bomb - ranged aerial attack
        if (dist > 64 && dist < 900 && isAttackReady("dive_bomb", 80)) {
            useAttack("dive_bomb");
            playAnimation("attack", AnimationIterator.Type.PLAY_ONCE);
            // Teleport above target then dive
            Location above = target.getLocation().add(0, 10, 0);
            hitboxEntity.teleport(above);
            hitboxEntity.setVelocity(new Vector(0, -3, 0));

            // Damage on impact area after short delay
            new org.bukkit.scheduler.BukkitRunnable() {
                @Override
                public void run() {
                    if (hitboxEntity == null || !alive) return;
                    Location impactLoc = hitboxEntity.getLocation();
                    for (Player p : findNearbyPlayers(6)) {
                        p.damage(mobType.getBaseDamage() * 1.5, hitboxEntity);
                        Vector knock = p.getLocation().toVector()
                                .subtract(impactLoc.toVector()).normalize().multiply(1.5);
                        knock.setY(0.8);
                        p.setVelocity(knock);
                    }
                    impactLoc.getWorld().spawnParticle(Particle.EXPLOSION, impactLoc, 5, 2, 0.5, 2, 0);
                    impactLoc.getWorld().playSound(impactLoc, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.6f);
                }
            }.runTaskLater(plugin, 10L);
        }
    }

    @Override
    protected void onPhaseTransition(int oldPhase, int newPhase) {
        super.onPhaseTransition(oldPhase, newPhase);
        playAnimation("roar", AnimationIterator.Type.PLAY_ONCE);
        if (hitboxEntity != null) {
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                    Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0f, 0.5f);
            hitboxEntity.getWorld().spawnParticle(Particle.BLOCK,
                    hitboxEntity.getLocation().add(0, 2, 0), 40, 2, 2, 2, 0,
                    Material.BONE_BLOCK.createBlockData());
        }
    }

    @Override
    protected void onDeath(Player killer) {
        super.onDeath(killer);
        if (killer != null) {
            killer.giveExp(500);
        }
    }

    @Override
    protected List<ItemStack> getDrops(Player killer) {
        List<ItemStack> drops = new ArrayList<>();
        drops.add(new ItemStack(Material.BONE, 16 + (int) (Math.random() * 16)));
        drops.add(new ItemStack(Material.BONE_BLOCK, 4 + (int) (Math.random() * 4)));

        // 1 MYTHIC legendary
        LegendaryWeapon[] weapons = LegendaryWeapon.byTier(LegendaryTier.MYTHIC);
        if (weapons.length > 0) {
            LegendaryWeaponManager wm = JGlimsPlugin.getInstance().getLegendaryWeaponManager();
            drops.add(wm.createWeapon(weapons[new Random().nextInt(weapons.length)]));
        }
        return drops;
    }
}

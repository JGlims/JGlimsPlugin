package com.jglims.plugin.custommobs.mobs;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.custommobs.CustomMobType;
import com.jglims.plugin.custommobs.CustomWorldBoss;
import com.jglims.plugin.legendary.LegendaryTier;
import com.jglims.plugin.legendary.LegendaryWeapon;
import com.jglims.plugin.legendary.LegendaryWeaponManager;
import kr.toxicity.model.api.animation.AnimationIterator;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Dinobot — Mechanical dinosaur world boss in the Jurassic dimension.
 * <p>
 * Expected .bbmodel: dinobot.bbmodel
 * Animations: idle, walk, attack, laser, missile, slam, transform, shield, emp, death
 */
public class DinobotBoss extends CustomWorldBoss {

    private final Random random = new Random();
    private boolean shieldActive = false;

    public DinobotBoss(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.DINOBOT);
        setPhaseThresholds(0.40);
        aggroRadius = 45;
        arenaRadius = 45;
        despawnTimerTicks = 24000L;
    }

    @Override
    protected void combatTick() {
        if (hitboxEntity == null) return;
        Player target = findNearestPlayer(aggroRadius);
        if (target == null) return;
        double dist = hitboxEntity.getLocation().distanceSquared(target.getLocation());

        if (currentPhase == 0) {
            phaseOne(target, dist);
        } else {
            phaseTwo(target, dist);
        }
    }

    private void phaseOne(Player target, double dist) {
        // Laser beam — continuous for 3s
        if (dist < 625 && isAttackReady("laser", 80)) {
            useAttack("laser");
            playAnimation("laser", AnimationIterator.Type.PLAY_ONCE);
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_GUARDIAN_ATTACK, 2f, 0.5f);
            new BukkitRunnable() {
                int ticks = 0;
                @Override
                public void run() {
                    if (!alive || hitboxEntity == null || ticks >= 30) { cancel(); return; }
                    Location from = hitboxEntity.getLocation().add(0, 2.5, 0);
                    Vector dir = target.getLocation().add(0, 1, 0).toVector().subtract(from.toVector()).normalize();
                    for (double d = 0; d < 20; d += 0.5) {
                        Location p = from.clone().add(dir.clone().multiply(d));
                        p.getWorld().spawnParticle(Particle.DUST, p, 1, 0, 0, 0, 0,
                                new Particle.DustOptions(Color.RED, 1.5f));
                        if (p.distanceSquared(target.getLocation().add(0, 1, 0)) < 2) {
                            target.damage(4, hitboxEntity);
                            break;
                        }
                    }
                    ticks += 5;
                }
            }.runTaskTimer(plugin, 0L, 5L);
        }
        // Missile barrage
        if (dist < 900 && isAttackReady("missile", 100)) {
            useAttack("missile");
            playAnimation("attack", AnimationIterator.Type.PLAY_ONCE);
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 2f, 0.8f);
            for (int i = 0; i < 5; i++) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!alive || hitboxEntity == null) return;
                        Location loc = target.getLocation();
                        loc.getWorld().createExplosion(loc, 2f, false, false);
                        for (Player p : findNearbyPlayers(4)) {
                            if (p.getLocation().distanceSquared(loc) < 16) p.damage(8, hitboxEntity);
                        }
                    }
                }.runTaskLater(plugin, i * 8L);
            }
        }
        // Metal slam
        if (dist < 16 && isAttackReady("slam", 50)) {
            useAttack("slam");
            playAnimation("slam", AnimationIterator.Type.PLAY_ONCE);
            for (Player p : findNearbyPlayers(5)) {
                p.damage(22, hitboxEntity);
                p.setVelocity(new Vector(0, 0.8, 0));
            }
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_IRON_GOLEM_ATTACK, 2f, 0.3f);
        }
    }

    private void phaseTwo(Player target, double dist) {
        // Energy shield — 50% DR for 10s, 30s cooldown
        if (!shieldActive && isAttackReady("shield", 300)) {
            useAttack("shield");
            shieldActive = true;
            playAnimation("shield", AnimationIterator.Type.PLAY_ONCE);
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 2f, 1.5f);
            new BukkitRunnable() {
                @Override
                public void run() { shieldActive = false; }
            }.runTaskLater(plugin, 200L);
        }
        // EMP blast — removes all potion effects from nearby players
        if (isAttackReady("emp", 200)) {
            useAttack("emp");
            playAnimation("emp", AnimationIterator.Type.PLAY_ONCE);
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 2f, 0.5f);
            for (Player p : findNearbyPlayers(12)) {
                for (PotionEffect effect : p.getActivePotionEffects()) {
                    p.removePotionEffect(effect.getType());
                }
                p.damage(10, hitboxEntity);
                p.sendMessage(net.kyori.adventure.text.Component.text("EMP! All effects removed!",
                        net.kyori.adventure.text.format.NamedTextColor.RED));
            }
        }
        // Rapid laser — faster version
        if (dist < 625 && isAttackReady("rapid_laser", 40)) {
            useAttack("rapid_laser");
            Location from = hitboxEntity.getLocation().add(0, 2.5, 0);
            Vector dir = target.getLocation().add(0, 1, 0).toVector().subtract(from.toVector()).normalize();
            for (double d = 0; d < 20; d += 0.5) {
                Location p = from.clone().add(dir.clone().multiply(d));
                p.getWorld().spawnParticle(Particle.DUST, p, 1, 0, 0, 0, 0,
                        new Particle.DustOptions(Color.YELLOW, 1.2f));
            }
            target.damage(8, hitboxEntity);
        }
        phaseOne(target, dist);
    }

    @Override
    protected void onDamage(double amount, Player source) {
        super.onDamage(amount, source);
        if (shieldActive && hitboxEntity != null) {
            hitboxEntity.setHealth(Math.min(hitboxEntity.getMaxHealth(),
                    hitboxEntity.getHealth() + amount * 0.5));
        }
    }

    @Override
    protected void onPhaseTransition(int oldPhase, int newPhase) {
        super.onPhaseTransition(oldPhase, newPhase);
        playAnimation("transform", AnimationIterator.Type.PLAY_ONCE);
        hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_IRON_GOLEM_REPAIR, 2f, 0.5f);
    }

    @Override
    protected List<ItemStack> getDrops(Player killer) {
        List<ItemStack> drops = new ArrayList<>();
        LegendaryWeaponManager wm = JGlimsPlugin.getInstance().getLegendaryWeaponManager();
        LegendaryWeapon[] mythic = LegendaryWeapon.byTier(LegendaryTier.MYTHIC);
        if (mythic.length > 0) drops.add(wm.createWeapon(mythic[random.nextInt(mythic.length)]));
        drops.add(new ItemStack(Material.IRON_BLOCK, 8));
        drops.add(new ItemStack(Material.REDSTONE_BLOCK, 4));
        drops.add(new ItemStack(Material.DIAMOND, 12));
        return drops;
    }
}

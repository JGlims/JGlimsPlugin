package com.jglims.plugin.custommobs.mobs;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.custommobs.CustomMobType;
import com.jglims.plugin.custommobs.CustomWorldBoss;
import com.jglims.plugin.legendary.LegendaryTier;
import com.jglims.plugin.legendary.LegendaryWeapon;
import com.jglims.plugin.legendary.LegendaryWeaponManager;
import kr.toxicity.model.api.animation.AnimationIterator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * T-Rex — Jurassic dimension apex predator world boss.
 * <p>
 * Expected .bbmodel: t_rex.bbmodel
 * Animations: idle, walk, attack, bite, tail_sweep, stomp, roar, charge, death
 */
public class TRexBoss extends CustomWorldBoss {

    private final Random random = new Random();

    public TRexBoss(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.T_REX);
        setPhaseThresholds(0.60, 0.30, 0.10);
        aggroRadius = 50;
        arenaRadius = 50;
        despawnTimerTicks = 24000L;
    }

    @Override
    protected void combatTick() {
        if (hitboxEntity == null) return;
        Player target = findNearestPlayer(aggroRadius);
        if (target == null) return;
        double dist = hitboxEntity.getLocation().distanceSquared(target.getLocation());

        switch (currentPhase) {
            case 0 -> phaseOne(target, dist);
            case 1 -> phaseTwo(target, dist);
            default -> phaseThree(target, dist);
        }
    }

    private void phaseOne(Player target, double dist) {
        if (dist < 16 && isAttackReady("bite", 30)) {
            useAttack("bite");
            target.damage(30, hitboxEntity);
            playAnimation("bite", AnimationIterator.Type.PLAY_ONCE);
            target.getWorld().playSound(target.getLocation(), Sound.ENTITY_RAVAGER_ATTACK, 2f, 0.5f);
        } else if (dist < 36 && isAttackReady("tail_sweep", 50)) {
            useAttack("tail_sweep");
            playAnimation("tail_sweep", AnimationIterator.Type.PLAY_ONCE);
            for (Player p : findNearbyPlayers(6)) {
                p.damage(18, hitboxEntity);
                p.setVelocity(p.getLocation().toVector().subtract(hitboxEntity.getLocation().toVector()).normalize().multiply(1.5).setY(0.5));
            }
        } else if (dist < 64 && isAttackReady("stomp", 60)) {
            useAttack("stomp");
            playAnimation("stomp", AnimationIterator.Type.PLAY_ONCE);
            Location loc = hitboxEntity.getLocation();
            loc.getWorld().playSound(loc, Sound.ENTITY_RAVAGER_ROAR, 2f, 0.3f);
            for (Player p : findNearbyPlayers(8)) {
                p.damage(15, hitboxEntity);
                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 30, 1));
            }
        }
    }

    private void phaseTwo(Player target, double dist) {
        if (isAttackReady("roar", 100)) {
            useAttack("roar");
            playAnimation("roar", AnimationIterator.Type.PLAY_ONCE);
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 3f, 0.3f);
            for (Player p : findNearbyPlayers(15)) {
                p.damage(5, hitboxEntity);
                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 2));
                p.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 60, 0));
                p.sendMessage(Component.text("The T-Rex's roar paralyzes you!", NamedTextColor.RED));
            }
        }
        if (dist > 25 && dist < 400 && isAttackReady("charge", 80)) {
            useAttack("charge");
            playAnimation("attack", AnimationIterator.Type.PLAY_ONCE);
            Vector dir = target.getLocation().toVector().subtract(hitboxEntity.getLocation().toVector()).normalize().multiply(2);
            hitboxEntity.setVelocity(dir);
            for (Player p : findNearbyPlayers(4)) {
                p.damage(25, hitboxEntity);
                p.setVelocity(dir.clone().setY(0.8));
            }
        }
        if (dist < 25 && isAttackReady("jaw_grab", 120)) {
            useAttack("jaw_grab");
            target.damage(20, hitboxEntity);
            target.setVelocity(new Vector(random.nextGaussian() * 0.5, 1.2, random.nextGaussian() * 0.5).multiply(1.5));
            target.sendMessage(Component.text("The T-Rex grabs you in its jaws!", NamedTextColor.DARK_RED));
        }
        phaseOne(target, dist);
    }

    private void phaseThree(Player target, double dist) {
        // Ground tremors every ~5 seconds
        if (isAttackReady("tremor", 50)) {
            useAttack("tremor");
            for (Player p : findNearbyPlayers(20)) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20, 0));
                p.getWorld().spawnParticle(Particle.BLOCK, p.getLocation(), 20, 1, 0.2, 1, 0,
                        Material.COARSE_DIRT.createBlockData());
            }
        }
        phaseTwo(target, dist);
    }

    @Override
    protected void onPhaseTransition(int oldPhase, int newPhase) {
        super.onPhaseTransition(oldPhase, newPhase);
        if (newPhase == 1) {
            playAnimation("roar", AnimationIterator.Type.PLAY_ONCE);
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 3f, 0.2f);
        }
    }

    @Override
    protected List<ItemStack> getDrops(Player killer) {
        List<ItemStack> drops = new ArrayList<>();
        LegendaryWeaponManager wm = JGlimsPlugin.getInstance().getLegendaryWeaponManager();

        // 2 EPIC guaranteed
        LegendaryWeapon[] epic = LegendaryWeapon.byTier(LegendaryTier.EPIC);
        for (int i = 0; i < 2 && epic.length > 0; i++) {
            drops.add(wm.createWeapon(epic[random.nextInt(epic.length)]));
        }
        // 30% chance 1-2 MYTHIC
        LegendaryWeapon[] mythic = LegendaryWeapon.byTier(LegendaryTier.MYTHIC);
        if (random.nextDouble() < 0.3 && mythic.length > 0) {
            drops.add(wm.createWeapon(mythic[random.nextInt(mythic.length)]));
            if (random.nextDouble() < 0.3) drops.add(wm.createWeapon(mythic[random.nextInt(mythic.length)]));
        }
        drops.add(new ItemStack(Material.BONE_BLOCK, 32));
        drops.add(new ItemStack(Material.DIAMOND, 16));
        return drops;
    }
}

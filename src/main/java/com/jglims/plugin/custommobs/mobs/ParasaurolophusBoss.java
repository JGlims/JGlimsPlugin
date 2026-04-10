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
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Parasaurolophus — Minor Jurassic world boss at Nesting Grounds.
 * Only hostile if its eggs (decorative blocks) are damaged.
 * <p>
 * Expected .bbmodel: parasaurolophus.bbmodel
 * Animations: idle, walk, attack, headbutt, tail_sweep, stomp, rallying_call, death
 */
public class ParasaurolophusBoss extends CustomWorldBoss {

    private final Random random = new Random();
    private boolean enraged = false;

    public ParasaurolophusBoss(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.PARASAUROLOPHUS);
        setPhaseThresholds(0.50);
        aggroRadius = 30;
        arenaRadius = 40;
        despawnTimerTicks = 36000L;
    }

    @Override
    protected void onSpawn() {
        super.onSpawn();
        // Starts peaceful — will only enter combat if attacked
    }

    @Override
    protected void combatTick() {
        if (hitboxEntity == null || !enraged) return;
        Player target = findNearestPlayer(aggroRadius);
        if (target == null) return;
        double dist = hitboxEntity.getLocation().distanceSquared(target.getLocation());

        // Headbutt charge
        if (dist > 16 && dist < 400 && isAttackReady("headbutt", 60)) {
            useAttack("headbutt");
            playAnimation("headbutt", AnimationIterator.Type.PLAY_ONCE);
            Vector dir = target.getLocation().toVector().subtract(hitboxEntity.getLocation().toVector()).normalize().multiply(1.8);
            hitboxEntity.setVelocity(dir);
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_RAVAGER_ATTACK, 2f, 0.6f);
            for (Player p : findNearbyPlayers(4)) {
                p.damage(14, hitboxEntity);
                p.setVelocity(dir.clone().setY(0.6));
            }
        }
        // Tail sweep
        if (dist < 25 && isAttackReady("tail", 40)) {
            useAttack("tail");
            playAnimation("tail_sweep", AnimationIterator.Type.PLAY_ONCE);
            for (Player p : findNearbyPlayers(5)) {
                p.damage(12, hitboxEntity);
                p.setVelocity(p.getLocation().toVector().subtract(hitboxEntity.getLocation().toVector()).normalize().multiply(1.2).setY(0.3));
            }
        }
        // Rallying call — summon 4 Stegosaurus allies
        if (currentPhase >= 1 && isAttackReady("rally", 200)) {
            useAttack("rally");
            playAnimation("rallying_call", AnimationIterator.Type.PLAY_ONCE);
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_RAVAGER_ROAR, 3f, 1.2f);
            var mobManager = JGlimsPlugin.getInstance().getCustomMobManager();
            if (mobManager != null) {
                for (int i = 0; i < 4; i++) {
                    double angle = (Math.PI * 2 / 4) * i;
                    Location spawnLoc = hitboxEntity.getLocation().clone().add(Math.cos(angle) * 6, 0, Math.sin(angle) * 6);
                    spawnLoc.setY(spawnLoc.getWorld().getHighestBlockYAt(spawnLoc) + 1);
                    mobManager.spawnMob(CustomMobType.STEGOSAURUS, spawnLoc);
                }
            }
            for (Player p : findNearbyPlayers(30)) {
                p.sendMessage(net.kyori.adventure.text.Component.text(
                        "The Parasaurolophus calls for backup!", net.kyori.adventure.text.format.NamedTextColor.YELLOW));
            }
        }
        // Stomp AoE
        if (dist < 36 && isAttackReady("stomp", 50)) {
            useAttack("stomp");
            playAnimation("stomp", AnimationIterator.Type.PLAY_ONCE);
            for (Player p : findNearbyPlayers(6)) {
                p.damage(10, hitboxEntity);
            }
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_IRON_GOLEM_ATTACK, 2f, 0.5f);
        }
    }

    @Override
    protected void onDamage(double amount, Player source) {
        super.onDamage(amount, source);
        if (!enraged) {
            enraged = true;
            for (Player p : findNearbyPlayers(30)) {
                p.sendMessage(net.kyori.adventure.text.Component.text(
                        "The Parasaurolophus is ENRAGED!", net.kyori.adventure.text.format.NamedTextColor.RED));
            }
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_RAVAGER_ROAR, 3f, 0.8f);
        }
    }

    @Override
    protected List<ItemStack> getDrops(Player killer) {
        List<ItemStack> drops = new ArrayList<>();
        LegendaryWeaponManager wm = JGlimsPlugin.getInstance().getLegendaryWeaponManager();
        LegendaryWeapon[] epic = LegendaryWeapon.byTier(LegendaryTier.EPIC);
        if (epic.length > 0) drops.add(wm.createWeapon(epic[random.nextInt(epic.length)]));
        drops.add(new ItemStack(Material.BONE_BLOCK, 16));
        drops.add(new ItemStack(Material.DIAMOND, 8));
        return drops;
    }
}

package com.jglims.plugin.custommobs.mobs;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.custommobs.CustomBossEntity;
import com.jglims.plugin.custommobs.CustomMobType;
import com.jglims.plugin.legendary.LegendaryTier;
import com.jglims.plugin.legendary.LegendaryWeapon;
import com.jglims.plugin.legendary.LegendaryWeaponManager;
import kr.toxicity.model.api.animation.AnimationIterator;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Ripper Zombie Boss - HP 400. Blood Moon boss.
 * Claw swipe combo (3 fast hits), ground slam AoE, zombie summon (4 zombies),
 * frenzied charge. Drops: EPIC legendary + double blood moon drops. Boss bar: red.
 */
public class RipperZombieBoss extends CustomBossEntity {

    private int comboCount = 0;

    public RipperZombieBoss(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.RIPPER_ZOMBIE);
        this.aggroRadius = 35.0;
        setPhaseThresholds(0.5, 0.25);
    }

    @Override
    protected BossBar.Color getBossBarColor() {
        return BossBar.Color.RED;
    }

    @Override
    protected void combatTick() {
        Player target = findNearestPlayer(aggroRadius);
        if (target == null) return;

        if (hitboxEntity instanceof Mob mob) {
            mob.setTarget(target);
        }

        double dist = hitboxEntity.getLocation().distanceSquared(target.getLocation());

        // Claw Swipe Combo - 3 fast consecutive hits
        if (dist < 9 && isAttackReady("claw_combo", 50)) {
            useAttack("claw_combo");
            comboCount = 0;
            new org.bukkit.scheduler.BukkitRunnable() {
                @Override
                public void run() {
                    if (hitboxEntity == null || !alive || comboCount >= 3) {
                        cancel();
                        return;
                    }
                    Player t = findNearestPlayer(4);
                    if (t == null) {
                        cancel();
                        return;
                    }
                    comboCount++;
                    playAnimation("attack", AnimationIterator.Type.PLAY_ONCE);
                    t.damage(mobType.getBaseDamage() * 0.6, hitboxEntity);
                    hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                            Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1.5f, 1.2f);
                    hitboxEntity.getWorld().spawnParticle(Particle.CRIT,
                            t.getLocation().add(0, 1, 0), 8, 0.3, 0.3, 0.3, 0.1);
                }
            }.runTaskTimer(plugin, 0L, 5L);
        }

        // Ground Slam AoE
        if (dist < 36 && isAttackReady("ground_slam", 70)) {
            useAttack("ground_slam");
            playAnimation("attack", AnimationIterator.Type.PLAY_ONCE);
            Location loc = hitboxEntity.getLocation();
            for (Player p : findNearbyPlayers(6)) {
                p.damage(mobType.getBaseDamage() * 1.2, hitboxEntity);
                Vector knock = p.getLocation().toVector()
                        .subtract(loc.toVector()).normalize().multiply(1.0);
                knock.setY(0.8);
                p.setVelocity(knock);
            }
            loc.getWorld().playSound(loc, Sound.ENTITY_IRON_GOLEM_ATTACK, 2.0f, 0.5f);
            loc.getWorld().spawnParticle(Particle.BLOCK, loc,
                    30, 3, 0.5, 3, 0, Material.REDSTONE_BLOCK.createBlockData());
        }

        // Zombie Summon
        if (isAttackReady("zombie_summon", 120)) {
            useAttack("zombie_summon");
            Location loc = hitboxEntity.getLocation();
            Random rand = new Random();
            for (int i = 0; i < 4; i++) {
                Location spawnLoc = loc.clone().add(
                        rand.nextInt(6) - 3, 0, rand.nextInt(6) - 3);
                Zombie zombie = (Zombie) loc.getWorld().spawnEntity(spawnLoc, EntityType.ZOMBIE);
                zombie.setTarget(target);
            }
            loc.getWorld().playSound(loc, Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 2.0f, 0.5f);
        }

        // Frenzied Charge
        if (dist > 25 && dist < 225 && isAttackReady("frenzy_charge", 60)) {
            useAttack("frenzy_charge");
            playAnimation("attack", AnimationIterator.Type.PLAY_ONCE);
            Vector charge = target.getLocation().toVector()
                    .subtract(hitboxEntity.getLocation().toVector()).normalize().multiply(2.0);
            charge.setY(0.2);
            hitboxEntity.setVelocity(charge);
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                    Sound.ENTITY_RAVAGER_ROAR, 1.5f, 1.2f);
            hitboxEntity.getWorld().spawnParticle(Particle.DUST,
                    hitboxEntity.getLocation(), 15, 1, 0.5, 1, 0,
                    new Particle.DustOptions(Color.RED, 2.0f));
        }
    }

    @Override
    protected void onPhaseTransition(int oldPhase, int newPhase) {
        super.onPhaseTransition(oldPhase, newPhase);
        playAnimation("roar", AnimationIterator.Type.PLAY_ONCE);
        if (hitboxEntity != null) {
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                    Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 2.0f, 0.3f);
        }
    }

    @Override
    protected void onDeath(Player killer) {
        super.onDeath(killer);
        if (killer != null) {
            killer.giveExp(300);
        }
    }

    @Override
    protected List<ItemStack> getDrops(Player killer) {
        List<ItemStack> drops = new ArrayList<>();
        // Double blood moon drops
        drops.add(new ItemStack(Material.ROTTEN_FLESH, 16 + (int) (Math.random() * 16)));
        drops.add(new ItemStack(Material.IRON_INGOT, 4 + (int) (Math.random() * 4)));
        drops.add(new ItemStack(Material.GOLD_INGOT, 2 + (int) (Math.random() * 3)));

        // 1 EPIC legendary
        LegendaryWeapon[] weapons = LegendaryWeapon.byTier(LegendaryTier.EPIC);
        if (weapons.length > 0) {
            LegendaryWeaponManager wm = JGlimsPlugin.getInstance().getLegendaryWeaponManager();
            drops.add(wm.createWeapon(weapons[new Random().nextInt(weapons.length)]));
        }
        return drops;
    }
}

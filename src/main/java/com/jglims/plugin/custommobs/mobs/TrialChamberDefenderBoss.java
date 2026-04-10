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
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Trial Chamber Defender Boss - HP 600. 2 phases (40%).
 * P1: heavy slam, sweep, copper bolt projectiles, summons Breeze.
 * P2: copper armor (30% DR), electrified attacks (+4 lightning dmg), shockwaves every 10s.
 * Drops: 2 EPIC legendary, trial key x3, heavy core, wind charges x16.
 */
public class TrialChamberDefenderBoss extends CustomBossEntity {

    private boolean phase2Active = false;
    private long lastShockwaveTick = 0;

    public TrialChamberDefenderBoss(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.TRIAL_CHAMBER_DEFENDER);
        this.aggroRadius = 30.0;
        setPhaseThresholds(0.6, 0.4);
    }

    @Override
    protected BossBar.Color getBossBarColor() {
        return BossBar.Color.YELLOW;
    }

    @Override
    protected void combatTick() {
        Player target = findNearestPlayer(aggroRadius);
        if (target == null) return;

        if (hitboxEntity instanceof Mob mob) {
            mob.setTarget(target);
        }

        double dist = hitboxEntity.getLocation().distanceSquared(target.getLocation());
        double bonusDmg = phase2Active ? 4.0 : 0.0;

        // Phase 2: Shockwave every 10 seconds
        if (phase2Active && combatTicks - lastShockwaveTick >= 100) {
            lastShockwaveTick = combatTicks;
            Location loc = hitboxEntity.getLocation();
            for (Player p : findNearbyPlayers(8)) {
                p.damage(8 + bonusDmg, hitboxEntity);
                Vector knock = p.getLocation().toVector()
                        .subtract(loc.toVector()).normalize().multiply(1.5);
                knock.setY(0.6);
                p.setVelocity(knock);
            }
            loc.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, loc.add(0, 1, 0),
                    40, 4, 1, 4, 0.1);
            loc.getWorld().playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.5f);
        }

        // Heavy Slam
        if (dist < 12 && isAttackReady("heavy_slam", 35)) {
            useAttack("heavy_slam");
            playAnimation("attack", AnimationIterator.Type.PLAY_ONCE);
            target.damage(mobType.getBaseDamage() + bonusDmg, hitboxEntity);
            target.setVelocity(new Vector(0, 0.5, 0));
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                    Sound.ENTITY_IRON_GOLEM_ATTACK, 1.5f, 0.7f);
            if (phase2Active) {
                hitboxEntity.getWorld().spawnParticle(Particle.ELECTRIC_SPARK,
                        target.getLocation(), 10, 0.3, 0.3, 0.3, 0);
            }
        }

        // Sweep Attack
        if (dist < 16 && isAttackReady("sweep", 45)) {
            useAttack("sweep");
            playAnimation("attack", AnimationIterator.Type.PLAY_ONCE);
            for (Player p : findNearbyPlayers(4)) {
                p.damage(14 + bonusDmg, hitboxEntity);
                Vector knock = p.getLocation().toVector()
                        .subtract(hitboxEntity.getLocation().toVector()).normalize().multiply(1.0);
                knock.setY(0.4);
                p.setVelocity(knock);
            }
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                    Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.5f, 0.8f);
        }

        // Copper Bolt Projectiles
        if (dist > 16 && dist < 400 && isAttackReady("copper_bolt", 50)) {
            useAttack("copper_bolt");
            playAnimation("attack", AnimationIterator.Type.PLAY_ONCE);
            Location origin = hitboxEntity.getLocation().add(0, 2, 0);
            Vector dir = target.getLocation().add(0, 1, 0).toVector()
                    .subtract(origin.toVector()).normalize();

            for (int i = 0; i < 3; i++) {
                Arrow bolt = hitboxEntity.getWorld().spawn(origin.clone(), Arrow.class);
                Vector spread = dir.clone().add(new Vector(
                        (Math.random() - 0.5) * 0.15,
                        (Math.random() - 0.5) * 0.1,
                        (Math.random() - 0.5) * 0.15)).normalize();
                bolt.setVelocity(spread.multiply(2.5));
                bolt.setDamage(8 + bonusDmg);
                bolt.setShooter(hitboxEntity);
                bolt.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
//                 bolt.setGlowing(true); // REMOVED: no glowing on mobs
            }
            origin.getWorld().playSound(origin, Sound.ENTITY_BREEZE_SHOOT, 1.5f, 1.0f);
        }

        // Summon Breeze
        if (isAttackReady("summon_breeze", 150)) {
            useAttack("summon_breeze");
            Location loc = hitboxEntity.getLocation();
            Random rand = new Random();
            Location spawnLoc = loc.clone().add(rand.nextInt(6) - 3, 0, rand.nextInt(6) - 3);
            loc.getWorld().spawnEntity(spawnLoc, EntityType.BREEZE);
            loc.getWorld().playSound(loc, Sound.ENTITY_BREEZE_IDLE_GROUND, 2.0f, 0.8f);
            for (Player p : findNearbyPlayers(aggroRadius)) {
                p.sendMessage(Component.text("The Defender summons a Breeze!", NamedTextColor.YELLOW));
            }
        }
    }

    @Override
    protected void onDamage(double amount, Player source) {
        // Phase 2: 30% damage reduction from copper armor
        if (phase2Active) {
            double reduced = amount * 0.3;
            if (hitboxEntity != null) {
                hitboxEntity.setHealth(Math.min(hitboxEntity.getHealth() + reduced,
                        mobType.getMaxHealth()));
            }
        }
        super.onDamage(amount, source);
    }

    @Override
    protected void onPhaseTransition(int oldPhase, int newPhase) {
        super.onPhaseTransition(oldPhase, newPhase);

        if (newPhase >= 2 && !phase2Active) {
            phase2Active = true;
            lastShockwaveTick = combatTicks;
            playAnimation("enrage", AnimationIterator.Type.PLAY_ONCE);
            if (hitboxEntity != null) {
                Location loc = hitboxEntity.getLocation();
                loc.getWorld().playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2.0f, 0.8f);
                loc.getWorld().spawnParticle(Particle.ELECTRIC_SPARK,
                        loc.add(0, 2, 0), 50, 2, 2, 2, 0.1);
            }
            for (Player p : findNearbyPlayers(aggroRadius * 1.5)) {
                p.sendMessage(Component.text("The Defender's copper armor electrifies!",
                        NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
            }
        }
    }

    @Override
    protected void onDeath(Player killer) {
        super.onDeath(killer);
        if (hitboxEntity != null) {
            Location loc = hitboxEntity.getLocation();
            loc.getWorld().playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 2.0f, 1.0f);
            loc.getWorld().spawnParticle(Particle.ELECTRIC_SPARK,
                    loc.add(0, 2, 0), 60, 3, 3, 3, 0.1);
        }
        if (killer != null) {
            killer.giveExp(400);
        }
    }

    @Override
    protected List<ItemStack> getDrops(Player killer) {
        List<ItemStack> drops = new ArrayList<>();
        drops.add(new ItemStack(Material.TRIAL_KEY, 3));
        drops.add(new ItemStack(Material.HEAVY_CORE, 1));
        drops.add(new ItemStack(Material.WIND_CHARGE, 16));

        // 2 EPIC legendaries
        LegendaryWeapon[] weapons = LegendaryWeapon.byTier(LegendaryTier.EPIC);
        if (weapons.length > 0) {
            LegendaryWeaponManager wm = JGlimsPlugin.getInstance().getLegendaryWeaponManager();
            Random rand = new Random();
            for (int i = 0; i < 2; i++) {
                drops.add(wm.createWeapon(weapons[rand.nextInt(weapons.length)]));
            }
        }
        return drops;
    }
}

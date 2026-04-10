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
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Pitlord - Demonic entity. HP 900.
 * 3 phases (50%/20%). Cleave, ground fire, pit fiend summons,
 * fire aura, and berserk charges with explosions.
 */
public class PitlordBoss extends CustomWorldBoss {

    private final Random random = new Random();
    private boolean fireAuraActive = false;

    public PitlordBoss(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.PITLORD);
        phaseThresholds = new double[]{0.50, 0.20};
        aggroRadius = 35.0;
        arenaRadius = 40.0;
        announceRadius = 120.0;
    }

    @Override
    protected void onSpawn() {
        super.onSpawn();
        if (hitboxEntity != null) {
            Location loc = hitboxEntity.getLocation();
            loc.getWorld().playSound(loc, Sound.ENTITY_WITHER_SPAWN, 1.5f, 0.7f);
            loc.getWorld().spawnParticle(Particle.FLAME, loc, 50, 2, 2, 2, 0.1);
        }
    }

    // ── Combat Logic ──────────────────────────────────────────────────

    @Override
    protected void combatTick() {
        if (hitboxEntity == null) return;

        Player target = findNearestPlayer(aggroRadius);
        if (target == null) return;

        if (hitboxEntity instanceof Mob mob) {
            mob.setTarget(target);
        }

        double distSq = hitboxEntity.getLocation().distanceSquared(target.getLocation());

        switch (currentPhase) {
            case 0 -> phase1Tick(target, distSq);
            case 1 -> phase2Tick(target, distSq);
            default -> phase3Tick(target, distSq);
        }

        // Fire aura damage
        if (fireAuraActive && combatTicks % 10 == 0) {
            for (Player p : findNearbyPlayers(5)) {
                p.damage(3.0, hitboxEntity);
                p.setFireTicks(40);
                p.getWorld().spawnParticle(Particle.FLAME, p.getLocation(), 5, 0.3, 0.5, 0.3, 0.02);
            }
        }
    }

    private void phase1Tick(Player target, double distSq) {
        // Cleave
        if (distSq < 20 && isAttackReady("cleave", 30)) {
            useAttack("cleave");
            performCleave(target);
        }

        // Ground fire
        if (isAttackReady("ground_fire", 60)) {
            useAttack("ground_fire");
            performGroundFire();
        }
    }

    private void phase2Tick(Player target, double distSq) {
        // Summon pit fiends
        if (isAttackReady("summon_fiends", 150)) {
            useAttack("summon_fiends");
            summonPitFiends();
        }

        // Cleave (faster)
        if (distSq < 20 && isAttackReady("cleave", 22)) {
            useAttack("cleave");
            performCleave(target);
        }

        // Ground fire (faster)
        if (isAttackReady("ground_fire", 45)) {
            useAttack("ground_fire");
            performGroundFire();
        }
    }

    private void phase3Tick(Player target, double distSq) {
        // Berserk charge with explosions
        if (distSq > 25 && isAttackReady("berserk_charge", 50)) {
            useAttack("berserk_charge");
            performBerserkCharge(target);
        }

        // Very fast cleave
        if (distSq < 20 && isAttackReady("cleave", 15)) {
            useAttack("cleave");
            performCleave(target);
        }

        // Ground fire (very fast)
        if (isAttackReady("ground_fire", 30)) {
            useAttack("ground_fire");
            performGroundFire();
        }

        // Occasional summons
        if (isAttackReady("summon_fiends", 120)) {
            useAttack("summon_fiends");
            summonPitFiends();
        }
    }

    // ── Attack Implementations ────────────────────────────────────────

    private void performCleave(Player target) {
        playAnimation("attack", AnimationIterator.Type.PLAY_ONCE);
        hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.5f, 0.6f);

        // Hit all players in front arc
        Location loc = hitboxEntity.getLocation();
        Vector facing = loc.getDirection().normalize();
        for (Player p : findNearbyPlayers(5)) {
            Vector toPlayer = p.getLocation().toVector().subtract(loc.toVector()).normalize();
            if (toPlayer.dot(facing) > 0.3) {
                p.damage(22.0, hitboxEntity);
                p.setFireTicks(40);
            }
        }

        // Fire particle sweep
        for (double angle = -Math.PI / 4; angle <= Math.PI / 4; angle += Math.PI / 16) {
            Vector sweep = facing.clone().rotateAroundY(angle).multiply(4);
            loc.getWorld().spawnParticle(Particle.FLAME, loc.clone().add(sweep).add(0, 1, 0),
                    3, 0.1, 0.1, 0.1, 0.01);
        }
    }

    private void performGroundFire() {
        playAnimation("ground_slam", AnimationIterator.Type.PLAY_ONCE);
        Location loc = hitboxEntity.getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.6f);

        // Create fire patches on the ground
        for (int i = 0; i < 5; i++) {
            Location fireLoc = loc.clone().add(
                    random.nextGaussian() * 5, 0, random.nextGaussian() * 5);
            fireLoc.getWorld().spawnParticle(Particle.FLAME, fireLoc, 20, 0.5, 0.1, 0.5, 0.05);
            fireLoc.getWorld().spawnParticle(Particle.SMOKE, fireLoc, 10, 0.3, 0.3, 0.3, 0.02);
        }

        for (Player p : findNearbyPlayers(6)) {
            p.damage(14.0, hitboxEntity);
            p.setFireTicks(60);
        }
    }

    private void summonPitFiends() {
        playAnimation("summon", AnimationIterator.Type.PLAY_ONCE);
        Location loc = hitboxEntity.getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_EVOKER_PREPARE_SUMMON, 1.5f, 0.5f);

        for (Player p : findNearbyPlayers(aggroRadius)) {
            p.sendMessage(Component.text("The Pitlord summons Pit Fiends!",
                    NamedTextColor.RED).decorate(TextDecoration.ITALIC));
        }

        for (int i = 0; i < 3; i++) {
            double angle = (Math.PI * 2 / 3) * i;
            Location spawnLoc = loc.clone().add(Math.cos(angle) * 4, 0, Math.sin(angle) * 4);
            Zombie fiend = (Zombie) loc.getWorld().spawnEntity(spawnLoc, EntityType.ZOMBIE);
            fiend.customName(Component.text("Pit Fiend", NamedTextColor.DARK_RED));
            fiend.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).setBaseValue(50);
            fiend.setHealth(50);
        }
    }

    private void performBerserkCharge(Player target) {
        playAnimation("charge", AnimationIterator.Type.PLAY_ONCE);
        Location start = hitboxEntity.getLocation();
        Location end = target.getLocation();
        hitboxEntity.getWorld().playSound(start, Sound.ENTITY_RAVAGER_ROAR, 2.0f, 0.6f);

        for (Player p : findNearbyPlayers(aggroRadius)) {
            p.sendMessage(Component.text("The Pitlord charges!",
                    NamedTextColor.RED).decorate(TextDecoration.BOLD));
        }

        // Teleport to target with explosion
        moveTo(end);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!alive || hitboxEntity == null) return;
                Location loc = hitboxEntity.getLocation();
                loc.getWorld().createExplosion(loc, 0f, false, false);
                loc.getWorld().spawnParticle(Particle.FLAME, loc, 40, 2, 1, 2, 0.1);
                loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.7f);

                for (Player p : findNearbyPlayers(5)) {
                    p.damage(25.0, hitboxEntity);
                    Vector kb = p.getLocation().toVector()
                            .subtract(loc.toVector()).normalize().multiply(1.5).setY(0.8);
                    p.setVelocity(kb);
                    p.setFireTicks(60);
                }
            }
        }.runTaskLater(plugin, 5L);
    }

    // ── Phase Transitions ─────────────────────────────────────────────

    @Override
    protected void onPhaseTransition(int oldPhase, int newPhase) {
        super.onPhaseTransition(oldPhase, newPhase);
        Location loc = hitboxEntity.getLocation();

        if (newPhase == 1) {
            fireAuraActive = true;
            loc.getWorld().playSound(loc, Sound.ENTITY_BLAZE_AMBIENT, 2.0f, 0.5f);
            loc.getWorld().spawnParticle(Particle.FLAME, loc, 80, 3, 3, 3, 0.1);
            for (Player p : findNearbyPlayers(aggroRadius)) {
                p.sendMessage(Component.text("The Pitlord ignites with demonic fire!",
                        NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
            }
        } else if (newPhase == 2) {
            enraged = true;
            playAnimation("enrage", AnimationIterator.Type.PLAY_ONCE);
            loc.getWorld().playSound(loc, Sound.ENTITY_WITHER_SPAWN, 2.0f, 0.5f);
            for (Player p : findNearbyPlayers(aggroRadius)) {
                p.sendMessage(Component.text("THE PITLORD ENTERS BERSERK MODE!",
                        NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD));
            }
        }
    }

    // ── Death + Drops ─────────────────────────────────────────────────

    @Override
    protected void onDeath(Player killer) {
        super.onDeath(killer);
        fireAuraActive = false;
        if (hitboxEntity != null) {
            Location loc = hitboxEntity.getLocation();
            loc.getWorld().playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 2.0f, 0.8f);
            loc.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, loc, 150, 3, 3, 3, 0.5);
        }

        for (Player p : findNearbyPlayers(120)) {
            p.sendMessage(Component.text("The Pitlord has been banished!",
                    NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
        }

        if (killer != null) {
            killer.giveExp(3000);
        }
    }

    @Override
    protected List<ItemStack> getDrops(Player killer) {
        List<ItemStack> drops = new ArrayList<>();

        // 2 MYTHIC legendary weapons
        LegendaryWeapon[] mythicWeapons = LegendaryWeapon.byTier(LegendaryTier.MYTHIC);
        if (mythicWeapons.length > 0) {
            LegendaryWeaponManager wm = JGlimsPlugin.getInstance().getLegendaryWeaponManager();
            for (int i = 0; i < 2; i++) {
                drops.add(wm.createWeapon(mythicWeapons[random.nextInt(mythicWeapons.length)]));
            }
        }

        // Fire charges x16
        drops.add(new ItemStack(Material.FIRE_CHARGE, 16));

        return drops;
    }
}

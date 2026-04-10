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
 * Protector of the Forge - Golden armored guardian. HP 900.
 * Can levitate. 3 phases (50%/20%). Hammer strikes, ground slam,
 * flight with golden projectile rain, golden minion summons,
 * and devastating dive attacks.
 */
public class ProtectorOfForgeBoss extends CustomWorldBoss {

    private final Random random = new Random();
    private boolean isFlying = false;

    public ProtectorOfForgeBoss(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.PROTECTOR_OF_FORGE);
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
            loc.getWorld().playSound(loc, Sound.BLOCK_ANVIL_LAND, 2.0f, 0.5f);
            loc.getWorld().spawnParticle(Particle.WAX_ON, loc, 40, 2, 2, 2, 0.1);
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
    }

    private void phase1Tick(Player target, double distSq) {
        // Hammer strikes
        if (distSq < 16 && isAttackReady("hammer_strike", 30)) {
            useAttack("hammer_strike");
            performHammerStrike(target);
        }

        // Ground slam (AoE)
        if (isAttackReady("ground_slam", 60)) {
            useAttack("ground_slam");
            performGroundSlam();
        }
    }

    private void phase2Tick(Player target, double distSq) {
        // Take flight
        if (!isFlying && isAttackReady("take_flight", 150)) {
            useAttack("take_flight");
            startFlight();
        }

        if (isFlying) {
            // Golden projectile rain
            if (isAttackReady("golden_rain", 15)) {
                useAttack("golden_rain");
                performGoldenProjectileRain();
            }
        } else {
            // Ground combat
            if (distSq < 16 && isAttackReady("hammer_strike", 22)) {
                useAttack("hammer_strike");
                performHammerStrike(target);
            }

            if (isAttackReady("ground_slam", 50)) {
                useAttack("ground_slam");
                performGroundSlam();
            }
        }

        // Summon golden minions
        if (isAttackReady("summon_minions", 160)) {
            useAttack("summon_minions");
            summonGoldenMinions();
        }
    }

    private void phase3Tick(Player target, double distSq) {
        // Devastating dive attacks
        if (isAttackReady("dive_attack", 60)) {
            useAttack("dive_attack");
            performDevastatingDive(target);
        }

        // Very fast hammer
        if (distSq < 16 && isAttackReady("hammer_strike", 15)) {
            useAttack("hammer_strike");
            performHammerStrike(target);
        }

        // Fast ground slam
        if (isAttackReady("ground_slam", 35)) {
            useAttack("ground_slam");
            performGroundSlam();
        }

        // Flight transitions
        if (!isFlying && isAttackReady("take_flight", 100)) {
            useAttack("take_flight");
            startFlight();
        }

        if (isFlying && isAttackReady("golden_rain", 10)) {
            useAttack("golden_rain");
            performGoldenProjectileRain();
        }

        if (isAttackReady("summon_minions", 120)) {
            useAttack("summon_minions");
            summonGoldenMinions();
        }
    }

    // ── Attack Implementations ────────────────────────────────────────

    private void performHammerStrike(Player target) {
        playAnimation("hammer_attack", AnimationIterator.Type.PLAY_ONCE);
        hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                Sound.BLOCK_ANVIL_LAND, 1.5f, 0.7f);

        target.damage(20.0, hitboxEntity);
        target.setVelocity(hitboxEntity.getLocation().getDirection().normalize().multiply(0.8).setY(0.3));

        hitboxEntity.getWorld().spawnParticle(Particle.WAX_ON,
                target.getLocation(), 10, 0.3, 0.3, 0.3, 0.05);
    }

    private void performGroundSlam() {
        playAnimation("ground_slam", AnimationIterator.Type.PLAY_ONCE);
        Location loc = hitboxEntity.getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_IRON_GOLEM_ATTACK, 2.0f, 0.4f);
        loc.getWorld().spawnParticle(Particle.BLOCK, loc, 40, 3, 0.5, 3, 0,
                Material.GOLD_BLOCK.createBlockData());

        // Shockwave ring
        for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 8) {
            for (int r = 1; r <= 6; r++) {
                double x = Math.cos(angle) * r;
                double z = Math.sin(angle) * r;
                loc.getWorld().spawnParticle(Particle.WAX_ON,
                        loc.clone().add(x, 0.2, z), 2, 0, 0, 0, 0);
            }
        }

        for (Player p : findNearbyPlayers(7)) {
            p.damage(16.0, hitboxEntity);
            Vector kb = p.getLocation().toVector()
                    .subtract(loc.toVector()).normalize().multiply(1.5).setY(0.7);
            p.setVelocity(kb);
        }
    }

    private void startFlight() {
        isFlying = true;
        playAnimation("fly", AnimationIterator.Type.LOOP);
        Location above = hitboxEntity.getLocation().clone().add(0, 8, 0);
        moveTo(above);

        hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                Sound.ENTITY_IRON_GOLEM_STEP, 2.0f, 1.5f);

        for (Player p : findNearbyPlayers(aggroRadius)) {
            p.sendMessage(Component.text("The Protector takes flight!",
                    NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
        }

        // Land after 8 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!alive || hitboxEntity == null) return;
                isFlying = false;
                if (spawnLocation != null) moveTo(spawnLocation);
                playAnimation("walk", AnimationIterator.Type.LOOP);
            }
        }.runTaskLater(plugin, 80L);
    }

    private void performGoldenProjectileRain() {
        Location loc = hitboxEntity.getLocation();
        for (int i = 0; i < 3; i++) {
            Location target = loc.clone().add(
                    random.nextGaussian() * 8, -6, random.nextGaussian() * 8);
            target.getWorld().spawnParticle(Particle.WAX_ON, target, 15, 0.5, 0.5, 0.5, 0.05);
            target.getWorld().spawnParticle(Particle.FLAME, target, 5, 0.2, 0.2, 0.2, 0.01);
            target.getWorld().playSound(target, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.8f, 1.2f);

            for (Player p : findNearbyPlayers(aggroRadius)) {
                if (p.getLocation().distanceSquared(target) < 6) {
                    p.damage(8.0, hitboxEntity);
                }
            }
        }
    }

    private void summonGoldenMinions() {
        playAnimation("summon", AnimationIterator.Type.PLAY_ONCE);
        Location loc = hitboxEntity.getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_EVOKER_PREPARE_SUMMON, 1.5f, 0.8f);

        for (Player p : findNearbyPlayers(aggroRadius)) {
            p.sendMessage(Component.text("The Protector summons golden minions!",
                    NamedTextColor.GOLD).decorate(TextDecoration.ITALIC));
        }

        for (int i = 0; i < 3; i++) {
            double angle = (Math.PI * 2 / 3) * i;
            Location spawnLoc = loc.clone().add(Math.cos(angle) * 4, 0, Math.sin(angle) * 4);
            Zombie minion = (Zombie) loc.getWorld().spawnEntity(spawnLoc, EntityType.ZOMBIE);
            minion.customName(Component.text("Golden Guardian", NamedTextColor.GOLD));
            minion.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).setBaseValue(40);
            minion.setHealth(40);
        }
    }

    private void performDevastatingDive(Player target) {
        playAnimation("dive_attack", AnimationIterator.Type.PLAY_ONCE);
        Location above = target.getLocation().add(0, 12, 0);
        moveTo(above);

        hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                Sound.ENTITY_IRON_GOLEM_ATTACK, 2.0f, 0.5f);

        for (Player p : findNearbyPlayers(aggroRadius)) {
            p.sendMessage(Component.text("The Protector dives from above!",
                    NamedTextColor.RED).decorate(TextDecoration.BOLD));
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!alive || hitboxEntity == null) return;
                isFlying = false;
                moveTo(target.getLocation());
                Location loc = hitboxEntity.getLocation();
                loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2.5f, 0.5f);
                loc.getWorld().spawnParticle(Particle.EXPLOSION, loc, 5, 1, 1, 1, 0);
                loc.getWorld().spawnParticle(Particle.WAX_ON, loc, 60, 4, 1, 4, 0.1);

                for (Player p : findNearbyPlayers(7)) {
                    p.damage(30.0, hitboxEntity);
                    Vector kb = p.getLocation().toVector()
                            .subtract(loc.toVector()).normalize().multiply(2.0).setY(1.0);
                    p.setVelocity(kb);
                }
                playAnimation("walk", AnimationIterator.Type.LOOP);
            }
        }.runTaskLater(plugin, 15L);
    }

    // ── Phase Transitions ─────────────────────────────────────────────

    @Override
    protected void onPhaseTransition(int oldPhase, int newPhase) {
        super.onPhaseTransition(oldPhase, newPhase);
        Location loc = hitboxEntity.getLocation();

        if (newPhase == 1) {
            loc.getWorld().playSound(loc, Sound.BLOCK_ANVIL_LAND, 2.0f, 0.4f);
            loc.getWorld().spawnParticle(Particle.WAX_ON, loc, 80, 3, 3, 3, 0.15);
            for (Player p : findNearbyPlayers(aggroRadius)) {
                p.sendMessage(Component.text("The Protector channels the Forge's power!",
                        NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
            }
        } else if (newPhase == 2) {
            enraged = true;
            playAnimation("enrage", AnimationIterator.Type.PLAY_ONCE);
            loc.getWorld().playSound(loc, Sound.ENTITY_WITHER_SPAWN, 2.0f, 0.6f);
            loc.getWorld().spawnParticle(Particle.WAX_ON, loc, 150, 5, 5, 5, 0.2);
            for (Player p : findNearbyPlayers(aggroRadius)) {
                p.sendMessage(Component.text("THE FORGE DEMANDS VENGEANCE!",
                        NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD));
            }
        }
    }

    // ── Death + Drops ─────────────────────────────────────────────────

    @Override
    protected void onDeath(Player killer) {
        super.onDeath(killer);
        if (hitboxEntity != null) {
            Location loc = hitboxEntity.getLocation();
            loc.getWorld().playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 2.0f, 0.8f);
            loc.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, loc, 150, 3, 3, 3, 0.5);
        }

        for (Player p : findNearbyPlayers(120)) {
            p.sendMessage(Component.text("The Protector of the Forge has fallen!",
                    NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
        }

        if (killer != null) {
            killer.giveExp(3500);
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

        // 16 gold blocks
        drops.add(new ItemStack(Material.GOLD_BLOCK, 16));

        // 2 nether stars
        drops.add(new ItemStack(Material.NETHER_STAR, 2));

        return drops;
    }
}

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
 * Godzilla - Legendary kaiju. HP 3000.
 * 5 phases (80%/60%/40%/20%). Stomp, tail sweep, atomic breath,
 * roar stun, mob waves, and enrage with regen.
 */
public class GodzillaBoss extends CustomWorldBoss {

    private final Random random = new Random();
    private boolean atomicBreathCharging = false;
    private boolean enrageRegenActive = false;

    public GodzillaBoss(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.GODZILLA);
        phaseThresholds = new double[]{0.80, 0.60, 0.40, 0.20};
        aggroRadius = 60.0;
        arenaRadius = 70.0;
        announceRadius = 250.0;
    }

    @Override
    protected void onSpawn() {
        super.onSpawn();
        if (hitboxEntity != null) {
            Location loc = hitboxEntity.getLocation();
            loc.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 3.0f, 0.3f);
            loc.getWorld().spawnParticle(Particle.EXPLOSION, loc, 10, 3, 3, 3, 0);
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
            case 2 -> phase3Tick(target, distSq);
            case 3 -> phase4Tick(target, distSq);
            default -> phase5Tick(target, distSq);
        }

        // Enrage regen
        if (enrageRegenActive && combatTicks % 10 == 0) {
            double maxHp = mobType.getMaxHealth();
            double newHp = Math.min(hitboxEntity.getHealth() + 5.0, maxHp);
            hitboxEntity.setHealth(newHp);
            hitboxEntity.getWorld().spawnParticle(Particle.HEART, hitboxEntity.getLocation().add(0, 3, 0),
                    3, 0.5, 0.5, 0.5, 0);
        }
    }

    private void phase1Tick(Player target, double distSq) {
        // Stomp
        if (distSq < 36 && isAttackReady("stomp", 40)) {
            useAttack("stomp");
            performStomp();
        }

        // Tail sweep
        if (distSq < 64 && isAttackReady("tail_sweep", 50)) {
            useAttack("tail_sweep");
            performTailSweep();
        }
    }

    private void phase2Tick(Player target, double distSq) {
        phase1Tick(target, distSq);

        // Atomic breath (5s charge, blue particles)
        if (!atomicBreathCharging && isAttackReady("atomic_breath", 150)) {
            useAttack("atomic_breath");
            startAtomicBreathCharge(target, 50); // 5s = 50 ticks at rate 2
        }
    }

    private void phase3Tick(Player target, double distSq) {
        // Roar (AoE stun 3s)
        if (isAttackReady("roar_stun", 100)) {
            useAttack("roar_stun");
            performRoarStun();
        }

        // Faster stomp and tail sweep
        if (distSq < 36 && isAttackReady("stomp", 30)) {
            useAttack("stomp");
            performStomp();
        }

        if (distSq < 64 && isAttackReady("tail_sweep", 35)) {
            useAttack("tail_sweep");
            performTailSweep();
        }

        if (!atomicBreathCharging && isAttackReady("atomic_breath", 120)) {
            useAttack("atomic_breath");
            startAtomicBreathCharge(target, 50);
        }
    }

    private void phase4Tick(Player target, double distSq) {
        phase3Tick(target, distSq);

        // Summon waves of mobs
        if (isAttackReady("summon_wave", 200)) {
            useAttack("summon_wave");
            summonMobWave();
        }
    }

    private void phase5Tick(Player target, double distSq) {
        // Enrage: atomic breath 2s charge, all attacks faster
        if (isAttackReady("roar_stun", 60)) {
            useAttack("roar_stun");
            performRoarStun();
        }

        if (distSq < 36 && isAttackReady("stomp", 20)) {
            useAttack("stomp");
            performStomp();
        }

        if (distSq < 64 && isAttackReady("tail_sweep", 25)) {
            useAttack("tail_sweep");
            performTailSweep();
        }

        // Faster atomic breath (2s charge)
        if (!atomicBreathCharging && isAttackReady("atomic_breath", 80)) {
            useAttack("atomic_breath");
            startAtomicBreathCharge(target, 20); // 2s charge
        }

        if (isAttackReady("summon_wave", 150)) {
            useAttack("summon_wave");
            summonMobWave();
        }
    }

    // ── Attack Implementations ────────────────────────────────────────

    private void performStomp() {
        playAnimation("stomp", AnimationIterator.Type.PLAY_ONCE);
        Location loc = hitboxEntity.getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_IRON_GOLEM_ATTACK, 2.0f, 0.4f);
        loc.getWorld().spawnParticle(Particle.BLOCK, loc, 40, 2, 0.5, 2, 0,
                Material.STONE.createBlockData());

        for (Player p : findNearbyPlayers(6)) {
            p.damage(22.0, hitboxEntity);
            Vector kb = p.getLocation().toVector().subtract(loc.toVector())
                    .normalize().multiply(1.2).setY(0.8);
            p.setVelocity(kb);
        }
    }

    private void performTailSweep() {
        playAnimation("tail_sweep", AnimationIterator.Type.PLAY_ONCE);
        Location loc = hitboxEntity.getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 2.0f, 0.5f);

        // Semicircle behind the boss
        Vector facing = loc.getDirection().normalize();
        for (Player p : findNearbyPlayers(8)) {
            Vector toPlayer = p.getLocation().toVector().subtract(loc.toVector()).normalize();
            if (toPlayer.dot(facing) < 0.2) { // Behind or to the side
                p.damage(18.0, hitboxEntity);
                Vector kb = toPlayer.multiply(2.0).setY(0.5);
                p.setVelocity(kb);
            }
        }
    }

    private void startAtomicBreathCharge(Player target, int chargeTicks) {
        atomicBreathCharging = true;
        playAnimation("charge_breath", AnimationIterator.Type.PLAY_ONCE);

        for (Player p : findNearbyPlayers(aggroRadius)) {
            p.sendMessage(Component.text("Godzilla charges Atomic Breath!",
                    NamedTextColor.AQUA).decorate(TextDecoration.BOLD));
            p.playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 2.0f, 0.3f);
        }

        final Location targetLoc = target.getLocation().clone();

        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (!alive || hitboxEntity == null) { cancel(); atomicBreathCharging = false; return; }
                ticks += 2;
                Location mouth = hitboxEntity.getLocation().add(0, 4, 0);
                mouth.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, mouth, 12, 0.3, 0.3, 0.3, 0.05);
                mouth.getWorld().spawnParticle(Particle.END_ROD, mouth, 6, 0.2, 0.2, 0.2, 0.02);

                if (ticks >= chargeTicks) {
                    cancel();
                    fireAtomicBreath(targetLoc);
                    atomicBreathCharging = false;
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    private void fireAtomicBreath(Location targetLoc) {
        if (hitboxEntity == null) return;
        Location from = hitboxEntity.getLocation().add(0, 4, 0);
        Vector dir = targetLoc.toVector().subtract(from.toVector()).normalize();
        from.getWorld().playSound(from, Sound.ENTITY_WARDEN_SONIC_BOOM, 3.0f, 0.4f);

        playAnimation("atomic_breath", AnimationIterator.Type.PLAY_ONCE);

        // Beam particles
        for (int i = 0; i < 50; i++) {
            Location beamPoint = from.clone().add(dir.clone().multiply(i * 0.7));
            beamPoint.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, beamPoint, 8, 0.2, 0.2, 0.2, 0);
            beamPoint.getWorld().spawnParticle(Particle.END_ROD, beamPoint, 4, 0.1, 0.1, 0.1, 0);
        }

        // Damage players in beam path
        for (Player p : findNearbyPlayers(aggroRadius)) {
            Location pLoc = p.getLocation();
            Vector toPlayer = pLoc.toVector().subtract(from.toVector());
            double projLength = toPlayer.dot(dir);
            if (projLength > 0 && projLength < 35) {
                Vector closest = from.toVector().add(dir.clone().multiply(projLength));
                if (closest.distanceSquared(pLoc.toVector()) < 9) {
                    p.damage(30.0, hitboxEntity);
                    p.setFireTicks(100);
                }
            }
        }
    }

    private void performRoarStun() {
        playAnimation("roar", AnimationIterator.Type.PLAY_ONCE);
        Location loc = hitboxEntity.getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 3.0f, 0.3f);
        loc.getWorld().spawnParticle(Particle.SONIC_BOOM, loc, 5, 2, 2, 2, 0);

        for (Player p : findNearbyPlayers(15)) {
            p.damage(10.0, hitboxEntity);
            // Stun: slowness IV + blindness for 3 seconds
            p.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.SLOWNESS, 60, 3));
            p.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.BLINDNESS, 60, 0));
            p.sendMessage(Component.text("You are stunned by the roar!",
                    NamedTextColor.YELLOW).decorate(TextDecoration.ITALIC));
        }
    }

    private void summonMobWave() {
        playAnimation("roar", AnimationIterator.Type.PLAY_ONCE);
        Location loc = hitboxEntity.getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_EVOKER_PREPARE_SUMMON, 2.0f, 0.5f);

        for (Player p : findNearbyPlayers(aggroRadius)) {
            p.sendMessage(Component.text("Godzilla summons a horde!",
                    NamedTextColor.RED).decorate(TextDecoration.ITALIC));
        }

        // Spawn 6 zombies and 4 skeletons
        for (int i = 0; i < 6; i++) {
            Location spawnLoc = loc.clone().add(
                    random.nextGaussian() * 6, 0, random.nextGaussian() * 6);
            Zombie z = (Zombie) loc.getWorld().spawnEntity(spawnLoc, EntityType.ZOMBIE);
            z.customName(Component.text("Kaiju Spawn", NamedTextColor.DARK_GREEN));
        }
        for (int i = 0; i < 4; i++) {
            Location spawnLoc = loc.clone().add(
                    random.nextGaussian() * 8, 0, random.nextGaussian() * 8);
            loc.getWorld().spawnEntity(spawnLoc, EntityType.SKELETON);
        }
    }

    // ── Phase Transitions ─────────────────────────────────────────────

    @Override
    protected void onPhaseTransition(int oldPhase, int newPhase) {
        super.onPhaseTransition(oldPhase, newPhase);
        Location loc = hitboxEntity.getLocation();

        switch (newPhase) {
            case 1 -> {
                loc.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 2.5f, 0.4f);
                for (Player p : findNearbyPlayers(aggroRadius)) {
                    p.sendMessage(Component.text("Godzilla's spine begins to glow!",
                            NamedTextColor.AQUA).decorate(TextDecoration.BOLD));
                }
            }
            case 2 -> {
                loc.getWorld().playSound(loc, Sound.ENTITY_WARDEN_ROAR, 2.5f, 0.5f);
                for (Player p : findNearbyPlayers(aggroRadius)) {
                    p.sendMessage(Component.text("Godzilla lets out a deafening roar!",
                            NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
                }
            }
            case 3 -> {
                loc.getWorld().playSound(loc, Sound.ENTITY_WITHER_SPAWN, 2.0f, 0.6f);
                for (Player p : findNearbyPlayers(aggroRadius)) {
                    p.sendMessage(Component.text("Godzilla calls upon its army!",
                            NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD));
                }
            }
            case 4 -> {
                enraged = true;
                enrageRegenActive = true;
                playAnimation("enrage", AnimationIterator.Type.PLAY_ONCE);
                loc.getWorld().playSound(loc, Sound.ENTITY_WITHER_SPAWN, 3.0f, 0.3f);
                loc.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, loc, 150, 5, 5, 5, 0.2);
                for (Player p : findNearbyPlayers(aggroRadius)) {
                    p.sendMessage(Component.text("GODZILLA IS ENRAGED! IT REGENERATES!",
                            NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD));
                }
            }
        }
    }

    // ── Death + Drops ─────────────────────────────────────────────────

    @Override
    protected void onDeath(Player killer) {
        super.onDeath(killer);
        enrageRegenActive = false;
        if (hitboxEntity != null) {
            Location loc = hitboxEntity.getLocation();
            loc.getWorld().playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 2.5f, 0.6f);
            loc.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, loc, 400, 5, 5, 5, 0.5);
            for (int i = 0; i < 5; i++) {
                loc.getWorld().createExplosion(loc.clone().add(
                        random.nextGaussian() * 4, random.nextDouble() * 3,
                        random.nextGaussian() * 4), 0f, false, false);
            }
        }

        for (Player p : findNearbyPlayers(250)) {
            p.sendMessage(Component.text("Godzilla, the King of Monsters, has fallen!",
                    NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
        }

        if (killer != null) {
            killer.giveExp(10000);
        }
    }

    @Override
    protected List<ItemStack> getDrops(Player killer) {
        List<ItemStack> drops = new ArrayList<>();
        LegendaryWeaponManager wm = JGlimsPlugin.getInstance().getLegendaryWeaponManager();

        // 3 MYTHIC legendary weapons
        LegendaryWeapon[] mythicWeapons = LegendaryWeapon.byTier(LegendaryTier.MYTHIC);
        if (mythicWeapons.length > 0) {
            for (int i = 0; i < 3; i++) {
                drops.add(wm.createWeapon(mythicWeapons[random.nextInt(mythicWeapons.length)]));
            }
        }

        // 10% chance for ABYSSAL legendary
        LegendaryWeapon[] abyssalWeapons = LegendaryWeapon.byTier(LegendaryTier.ABYSSAL);
        if (abyssalWeapons.length > 0 && random.nextDouble() < 0.10) {
            drops.add(wm.createWeapon(abyssalWeapons[random.nextInt(abyssalWeapons.length)]));
        }

        // 5 nether stars
        drops.add(new ItemStack(Material.NETHER_STAR, 5));

        // 64 diamonds
        drops.add(new ItemStack(Material.DIAMOND, 64));

        // Dragon egg
        drops.add(new ItemStack(Material.DRAGON_EGG, 1));

        return drops;
    }
}

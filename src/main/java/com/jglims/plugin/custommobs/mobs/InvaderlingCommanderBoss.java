package com.jglims.plugin.custommobs.mobs;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.custommobs.CustomMobType;
import com.jglims.plugin.custommobs.CustomWorldBoss;
import com.jglims.plugin.legendary.LegendaryTier;
import com.jglims.plugin.legendary.LegendaryWeapon;
import com.jglims.plugin.legendary.LegendaryWeaponManager;
import kr.toxicity.model.api.animation.AnimationIterator;
import net.kyori.adventure.bossbar.BossBar;
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
 * Invaderling Commander - Lunar dimension boss. HP 1000.
 * 3 phases (60%/25%). P1: summons squads. P2: personal combat
 * with teleport strikes. P3: mech suit with energy blasts,
 * ground pound, and energy beam. Boss bar: purple.
 */
public class InvaderlingCommanderBoss extends CustomWorldBoss {

    private final Random random = new Random();
    private boolean inMechSuit = false;

    public InvaderlingCommanderBoss(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.INVADERLING_COMMANDER);
        phaseThresholds = new double[]{0.60, 0.25};
        aggroRadius = 45.0;
        arenaRadius = 50.0;
        announceRadius = 150.0;
    }

    @Override
    protected BossBar.Color getBossBarColor() {
        return BossBar.Color.PURPLE;
    }

    @Override
    protected void onSpawn() {
        super.onSpawn();
        if (hitboxEntity != null) {
            Location loc = hitboxEntity.getLocation();
            loc.getWorld().playSound(loc, Sound.ENTITY_WARDEN_ROAR, 2.0f, 1.0f);
            loc.getWorld().spawnParticle(Particle.END_ROD, loc, 40, 2, 2, 2, 0.1);
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
        // Summon squads
        if (isAttackReady("summon_squad", 100)) {
            useAttack("summon_squad");
            summonSquad();
        }

        // Rally command - buffs nearby minions
        if (isAttackReady("rally", 80)) {
            useAttack("rally");
            performRally();
        }

        // Basic ranged energy shot
        if (distSq > 16 && isAttackReady("energy_shot", 35)) {
            useAttack("energy_shot");
            performEnergyShot(target);
        }

        // Melee if close
        if (distSq < 12 && isAttackReady("melee", 30)) {
            useAttack("melee");
            playAnimation("attack", AnimationIterator.Type.PLAY_ONCE);
            target.damage(14.0, hitboxEntity);
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                    Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.5f, 1.0f);
        }
    }

    private void phase2Tick(Player target, double distSq) {
        // Fast melee combat
        if (distSq < 16 && isAttackReady("rapid_melee", 15)) {
            useAttack("rapid_melee");
            performRapidMelee(target);
        }

        // Teleport strikes
        if (distSq > 25 && isAttackReady("teleport_strike", 40)) {
            useAttack("teleport_strike");
            performTeleportStrike(target);
        }

        // Energy shot
        if (distSq > 16 && isAttackReady("energy_shot", 25)) {
            useAttack("energy_shot");
            performEnergyShot(target);
        }

        // Smaller squads
        if (isAttackReady("summon_squad", 120)) {
            useAttack("summon_squad");
            summonSquad();
        }
    }

    private void phase3Tick(Player target, double distSq) {
        // Mech suit attacks

        // AoE energy blast
        if (isAttackReady("energy_blast", 40)) {
            useAttack("energy_blast");
            performEnergyBlast();
        }

        // Ground pound
        if (isAttackReady("ground_pound", 60)) {
            useAttack("ground_pound");
            performGroundPound();
        }

        // Energy beam
        if (isAttackReady("energy_beam", 100)) {
            useAttack("energy_beam");
            performEnergyBeam(target);
        }

        // Mech melee
        if (distSq < 20 && isAttackReady("mech_punch", 20)) {
            useAttack("mech_punch");
            performMechPunch(target);
        }
    }

    // ── Attack Implementations ────────────────────────────────────────

    private void summonSquad() {
        playAnimation("summon", AnimationIterator.Type.PLAY_ONCE);
        Location loc = hitboxEntity.getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_EVOKER_PREPARE_SUMMON, 1.5f, 1.0f);

        for (Player p : findNearbyPlayers(aggroRadius)) {
            p.sendMessage(Component.text("The Commander calls reinforcements!",
                    NamedTextColor.LIGHT_PURPLE).decorate(TextDecoration.ITALIC));
        }

        int count = currentPhase == 0 ? 4 : 2;
        for (int i = 0; i < count; i++) {
            Location spawnLoc = loc.clone().add(
                    random.nextGaussian() * 5, 0, random.nextGaussian() * 5);
            Zombie soldier = (Zombie) loc.getWorld().spawnEntity(spawnLoc, EntityType.ZOMBIE);
            soldier.customName(Component.text("Invaderling Soldier", NamedTextColor.DARK_PURPLE));
            soldier.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).setBaseValue(35);
            soldier.setHealth(35);
            spawnLoc.getWorld().spawnParticle(Particle.END_ROD, spawnLoc, 10, 0.3, 0.5, 0.3, 0.05);
        }
    }

    private void performRally() {
        playAnimation("rally", AnimationIterator.Type.PLAY_ONCE);
        Location loc = hitboxEntity.getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_EVOKER_CELEBRATE, 2.0f, 0.8f);
        loc.getWorld().spawnParticle(Particle.END_ROD, loc, 20, 3, 2, 3, 0.05);

        for (Player p : findNearbyPlayers(aggroRadius)) {
            p.sendMessage(Component.text("The Commander rallies his troops!",
                    NamedTextColor.LIGHT_PURPLE));
        }
    }

    private void performEnergyShot(Player target) {
        playAnimation("shoot", AnimationIterator.Type.PLAY_ONCE);
        Location from = hitboxEntity.getLocation().add(0, 1.5, 0);
        Vector dir = target.getLocation().toVector().subtract(from.toVector()).normalize();
        from.getWorld().playSound(from, Sound.ENTITY_SHULKER_SHOOT, 1.5f, 1.0f);

        new BukkitRunnable() {
            Location current = from.clone();
            int steps = 0;
            @Override
            public void run() {
                if (!alive || steps > 25) { cancel(); return; }
                current.add(dir.clone().multiply(1.2));
                current.getWorld().spawnParticle(Particle.END_ROD, current, 3, 0.1, 0.1, 0.1, 0);
                current.getWorld().spawnParticle(Particle.WITCH, current, 2, 0.1, 0.1, 0.1, 0);

                for (Player p : findNearbyPlayers(aggroRadius)) {
                    if (p.getLocation().distanceSquared(current) < 4) {
                        p.damage(10.0, hitboxEntity);
                        cancel();
                        return;
                    }
                }
                steps++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void performRapidMelee(Player target) {
        playAnimation("rapid_attack", AnimationIterator.Type.PLAY_ONCE);
        target.damage(12.0, hitboxEntity);
        hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.2f);
        hitboxEntity.getWorld().spawnParticle(Particle.SWEEP_ATTACK,
                target.getLocation().add(0, 1, 0), 1, 0, 0, 0, 0);
    }

    private void performTeleportStrike(Player target) {
        playAnimation("teleport", AnimationIterator.Type.PLAY_ONCE);
        Location oldLoc = hitboxEntity.getLocation();
        oldLoc.getWorld().playSound(oldLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.5f, 1.2f);
        oldLoc.getWorld().spawnParticle(Particle.END_ROD, oldLoc, 15, 0.5, 0.5, 0.5, 0.1);

        // Teleport behind target
        Vector behind = target.getLocation().getDirection().normalize().multiply(-2);
        Location dest = target.getLocation().add(behind);
        moveTo(dest);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!alive || hitboxEntity == null) return;
                dest.getWorld().spawnParticle(Particle.END_ROD, dest, 15, 0.5, 0.5, 0.5, 0.1);
                target.damage(18.0, hitboxEntity);
                hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                        Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.5f, 0.8f);
            }
        }.runTaskLater(plugin, 3L);
    }

    private void performEnergyBlast() {
        playAnimation("energy_blast", AnimationIterator.Type.PLAY_ONCE);
        Location loc = hitboxEntity.getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_WARDEN_SONIC_BOOM, 1.5f, 1.0f);
        loc.getWorld().spawnParticle(Particle.END_ROD, loc, 50, 5, 2, 5, 0.1);
        loc.getWorld().spawnParticle(Particle.SONIC_BOOM, loc, 3, 2, 1, 2, 0);

        for (Player p : findNearbyPlayers(8)) {
            p.damage(18.0, hitboxEntity);
            Vector kb = p.getLocation().toVector()
                    .subtract(loc.toVector()).normalize().multiply(1.5).setY(0.6);
            p.setVelocity(kb);
        }
    }

    private void performGroundPound() {
        playAnimation("ground_pound", AnimationIterator.Type.PLAY_ONCE);
        Location loc = hitboxEntity.getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_IRON_GOLEM_ATTACK, 2.0f, 0.4f);
        loc.getWorld().spawnParticle(Particle.BLOCK, loc, 40, 3, 0.5, 3, 0,
                Material.STONE.createBlockData());
        loc.getWorld().spawnParticle(Particle.END_ROD, loc, 30, 3, 0.5, 3, 0.05);

        for (Player p : findNearbyPlayers(7)) {
            p.damage(22.0, hitboxEntity);
            p.setVelocity(new Vector(0, 1.0, 0));
        }
    }

    private void performEnergyBeam(Player target) {
        playAnimation("energy_beam", AnimationIterator.Type.PLAY_ONCE);
        Location from = hitboxEntity.getLocation().add(0, 2, 0);
        hitboxEntity.getWorld().playSound(from, Sound.ENTITY_GUARDIAN_ATTACK, 2.0f, 0.5f);

        for (Player p : findNearbyPlayers(aggroRadius)) {
            p.sendMessage(Component.text("The mech charges its energy beam!",
                    NamedTextColor.RED).decorate(TextDecoration.BOLD));
        }

        // Charge for 1.5 seconds then fire
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (!alive || hitboxEntity == null) { cancel(); return; }
                ticks += 2;
                Location chargeLoc = hitboxEntity.getLocation().add(0, 2, 0);
                chargeLoc.getWorld().spawnParticle(Particle.END_ROD, chargeLoc, 10, 0.3, 0.3, 0.3, 0.05);

                if (ticks >= 15) {
                    cancel();
                    fireEnergyBeam(target);
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    private void fireEnergyBeam(Player target) {
        if (hitboxEntity == null) return;
        Location from = hitboxEntity.getLocation().add(0, 2, 0);
        Vector dir = target.getLocation().toVector().subtract(from.toVector()).normalize();
        from.getWorld().playSound(from, Sound.ENTITY_WARDEN_SONIC_BOOM, 2.0f, 0.8f);

        for (int i = 0; i < 30; i++) {
            Location beamPoint = from.clone().add(dir.clone().multiply(i * 0.8));
            beamPoint.getWorld().spawnParticle(Particle.END_ROD, beamPoint, 5, 0.1, 0.1, 0.1, 0);
            beamPoint.getWorld().spawnParticle(Particle.WITCH, beamPoint, 3, 0.1, 0.1, 0.1, 0);
        }

        for (Player p : findNearbyPlayers(aggroRadius)) {
            Location pLoc = p.getLocation();
            Vector toPlayer = pLoc.toVector().subtract(from.toVector());
            double proj = toPlayer.dot(dir);
            if (proj > 0 && proj < 25) {
                Vector closest = from.toVector().add(dir.clone().multiply(proj));
                if (closest.distanceSquared(pLoc.toVector()) < 6) {
                    p.damage(28.0, hitboxEntity);
                }
            }
        }
    }

    private void performMechPunch(Player target) {
        playAnimation("mech_punch", AnimationIterator.Type.PLAY_ONCE);
        target.damage(20.0, hitboxEntity);
        target.setVelocity(hitboxEntity.getLocation().getDirection().normalize().multiply(1.5).setY(0.5));
        hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                Sound.ENTITY_IRON_GOLEM_ATTACK, 1.5f, 0.6f);
    }

    // ── Phase Transitions ─────────────────────────────────────────────

    @Override
    protected void onPhaseTransition(int oldPhase, int newPhase) {
        super.onPhaseTransition(oldPhase, newPhase);
        Location loc = hitboxEntity.getLocation();

        if (newPhase == 1) {
            loc.getWorld().playSound(loc, Sound.ENTITY_ENDERMAN_SCREAM, 2.0f, 1.0f);
            loc.getWorld().spawnParticle(Particle.END_ROD, loc, 50, 2, 2, 2, 0.1);
            for (Player p : findNearbyPlayers(aggroRadius)) {
                p.sendMessage(Component.text("The Commander enters personal combat!",
                        NamedTextColor.LIGHT_PURPLE).decorate(TextDecoration.BOLD));
            }
        } else if (newPhase == 2) {
            inMechSuit = true;
            enraged = true;
            playAnimation("mech_activate", AnimationIterator.Type.PLAY_ONCE);
            loc.getWorld().playSound(loc, Sound.ENTITY_WITHER_SPAWN, 2.0f, 0.8f);
            loc.getWorld().spawnParticle(Particle.END_ROD, loc, 100, 4, 4, 4, 0.2);
            for (Player p : findNearbyPlayers(aggroRadius)) {
                p.sendMessage(Component.text("The Commander activates the MECH SUIT!",
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
            loc.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, loc, 200, 3, 3, 3, 0.5);
            loc.getWorld().spawnParticle(Particle.END_ROD, loc, 80, 3, 3, 3, 0.2);
        }

        for (Player p : findNearbyPlayers(150)) {
            p.sendMessage(Component.text("The Invaderling Commander has been defeated!",
                    NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
        }

        if (killer != null) {
            killer.giveExp(5000);
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

        // 48 diamonds
        drops.add(new ItemStack(Material.DIAMOND, 48));

        // 2 nether stars
        drops.add(new ItemStack(Material.NETHER_STAR, 2));

        return drops;
    }
}

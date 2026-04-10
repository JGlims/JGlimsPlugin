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
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * King Gleeok - Three-headed dragon boss. HP 1800.
 * 3 phases (50%/25%). Each head attacks independently with
 * fire, ice, and lightning. Phase 3 central beam is a one-shot
 * if not dodged (3s charge with particles).
 */
public class KingGleeokBoss extends CustomWorldBoss {

    private final Random random = new Random();

    /** Independent head cooldown trackers. */
    private long fireHeadCooldown = 0;
    private long iceHeadCooldown = 0;
    private long lightningHeadCooldown = 0;

    /** Whether the central beam is currently charging. */
    private boolean beamCharging = false;

    public KingGleeokBoss(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.KING_GLEEOK);
        phaseThresholds = new double[]{0.50, 0.25};
        aggroRadius = 50.0;
        arenaRadius = 55.0;
        announceRadius = 200.0;
    }

    @Override
    protected void onSpawn() {
        super.onSpawn();
        if (hitboxEntity != null) {
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                    Sound.ENTITY_ENDER_DRAGON_GROWL, 2.5f, 0.4f);
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

        // Each head attacks independently every tick cycle
        tickFireHead(target);
        tickIceHead(target);
        tickLightningHead(target);

        switch (currentPhase) {
            case 0 -> phase1Extras(target);
            case 1 -> phase2Extras(target);
            default -> phase3Extras(target);
        }
    }

    // ── Independent Head Attacks ──────────────────────────────────────

    private void tickFireHead(Player target) {
        long cooldown = currentPhase >= 1 ? 40 : 60;
        if (combatTicks - fireHeadCooldown < cooldown) return;
        fireHeadCooldown = combatTicks;

        playAnimation("fire_head_attack", AnimationIterator.Type.PLAY_ONCE);
        Location loc = hitboxEntity.getLocation();
        Vector dir = target.getLocation().toVector().subtract(loc.toVector()).normalize();
        loc.getWorld().playSound(loc, Sound.ENTITY_BLAZE_SHOOT, 1.5f, 0.6f);

        // Fire projectile line
        for (int i = 1; i <= 12; i++) {
            Location pLoc = loc.clone().add(dir.clone().multiply(i)).add(0, 1.5, 0);
            loc.getWorld().spawnParticle(Particle.FLAME, pLoc, 8, 0.3, 0.3, 0.3, 0.02);
        }

        if (hitboxEntity.getLocation().distanceSquared(target.getLocation()) < 225) {
            target.damage(14.0, hitboxEntity);
            target.setFireTicks(80);
        }
    }

    private void tickIceHead(Player target) {
        long cooldown = currentPhase >= 1 ? 45 : 70;
        if (combatTicks - iceHeadCooldown < cooldown) return;
        iceHeadCooldown = combatTicks;

        playAnimation("ice_head_attack", AnimationIterator.Type.PLAY_ONCE);
        Location loc = hitboxEntity.getLocation();
        loc.getWorld().playSound(loc, Sound.BLOCK_GLASS_BREAK, 1.5f, 1.5f);

        // Ice burst towards target
        Vector dir = target.getLocation().toVector().subtract(loc.toVector()).normalize();
        for (int i = 1; i <= 10; i++) {
            Location pLoc = loc.clone().add(dir.clone().multiply(i)).add(0, 1.5, 0);
            loc.getWorld().spawnParticle(Particle.SNOWFLAKE, pLoc, 10, 0.4, 0.4, 0.4, 0.02);
        }

        if (hitboxEntity.getLocation().distanceSquared(target.getLocation()) < 196) {
            target.damage(12.0, hitboxEntity);
            target.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.SLOWNESS, 60, 1));
            target.setFreezeTicks(Math.min(target.getFreezeTicks() + 60, target.getMaxFreezeTicks()));
        }
    }

    private void tickLightningHead(Player target) {
        long cooldown = currentPhase >= 1 ? 50 : 80;
        if (combatTicks - lightningHeadCooldown < cooldown) return;
        lightningHeadCooldown = combatTicks;

        playAnimation("lightning_head_attack", AnimationIterator.Type.PLAY_ONCE);
        Location loc = hitboxEntity.getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.5f, 1.0f);

        // Lightning on a random nearby player
        List<Player> nearby = findNearbyPlayers(aggroRadius);
        if (!nearby.isEmpty()) {
            Player victim = nearby.get(random.nextInt(nearby.size()));
            victim.getWorld().strikeLightningEffect(victim.getLocation());
            victim.damage(16.0, hitboxEntity);
            victim.getWorld().spawnParticle(Particle.ELECTRIC_SPARK,
                    victim.getLocation(), 30, 0.5, 1, 0.5, 0.15);
        }
    }

    // ── Phase-specific extras ─────────────────────────────────────────

    private void phase1Extras(Player target) {
        // Tail swipe when player is close behind
        double dist = hitboxEntity.getLocation().distanceSquared(target.getLocation());
        if (dist < 25 && isAttackReady("tail_swipe", 50)) {
            useAttack("tail_swipe");
            playAnimation("tail_swipe", AnimationIterator.Type.PLAY_ONCE);
            target.damage(15.0, hitboxEntity);
            Vector knockback = target.getLocation().toVector()
                    .subtract(hitboxEntity.getLocation().toVector())
                    .normalize().multiply(1.5).setY(0.6);
            target.setVelocity(knockback);
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                    Sound.ENTITY_IRON_GOLEM_ATTACK, 1.5f, 0.8f);
        }
    }

    private void phase2Extras(Player target) {
        phase1Extras(target);

        // Coordinated triple breath — all heads fire at once
        if (isAttackReady("coordinated_breath", 120)) {
            useAttack("coordinated_breath");
            performCoordinatedBreath();
        }
    }

    private void phase3Extras(Player target) {
        phase2Extras(target);

        // Central beam (one-shot, 3s charge)
        if (!beamCharging && isAttackReady("central_beam", 200)) {
            useAttack("central_beam");
            startCentralBeamCharge(target);
        }
    }

    // ── Special Attack Implementations ────────────────────────────────

    private void performCoordinatedBreath() {
        Location loc = hitboxEntity.getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0f, 0.6f);

        for (Player p : findNearbyPlayers(15)) {
            p.sendMessage(Component.text("All three heads converge!",
                    NamedTextColor.RED).decorate(TextDecoration.ITALIC));
        }

        for (Player p : findNearbyPlayers(12)) {
            p.damage(25.0, hitboxEntity);
            p.setFireTicks(60);
            p.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.SLOWNESS, 40, 2));
            p.getWorld().spawnParticle(Particle.FLAME, p.getLocation(), 20, 0.5, 0.5, 0.5, 0.1);
            p.getWorld().spawnParticle(Particle.SNOWFLAKE, p.getLocation(), 20, 0.5, 0.5, 0.5, 0.1);
            p.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, p.getLocation(), 20, 0.5, 0.5, 0.5, 0.1);
        }
    }

    private void startCentralBeamCharge(Player target) {
        beamCharging = true;
        Location bossLoc = hitboxEntity.getLocation();
        playAnimation("charge_beam", AnimationIterator.Type.PLAY_ONCE);

        for (Player p : findNearbyPlayers(aggroRadius)) {
            p.sendMessage(Component.text("King Gleeok charges a devastating beam! MOVE!",
                    NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD));
            p.playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 2.0f, 0.3f);
        }

        // Save target location at start of charge
        final Location targetLoc = target.getLocation().clone();

        // Charge particles over 3 seconds (60 ticks)
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (!alive || hitboxEntity == null) { cancel(); beamCharging = false; return; }
                ticks++;
                Location loc = hitboxEntity.getLocation().add(0, 3, 0);
                loc.getWorld().spawnParticle(Particle.END_ROD, loc, 15, 0.5, 0.5, 0.5, 0.1);
                loc.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, loc, 10, 0.3, 0.3, 0.3, 0.05);

                // Warning marker at target location
                targetLoc.getWorld().spawnParticle(Particle.DUST,
                        targetLoc, 20, 2, 0.1, 2, 0,
                        new Particle.DustOptions(Color.RED, 2.0f));

                if (ticks >= 30) { // 3 seconds at tick rate 2
                    cancel();
                    fireCentralBeam(targetLoc);
                    beamCharging = false;
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    private void fireCentralBeam(Location targetLoc) {
        if (hitboxEntity == null) return;
        Location from = hitboxEntity.getLocation().add(0, 3, 0);
        hitboxEntity.getWorld().playSound(from, Sound.ENTITY_WARDEN_SONIC_BOOM, 2.5f, 0.5f);

        // Beam particle line
        Vector dir = targetLoc.toVector().subtract(from.toVector()).normalize();
        for (int i = 0; i < 40; i++) {
            Location beamPoint = from.clone().add(dir.clone().multiply(i * 0.8));
            beamPoint.getWorld().spawnParticle(Particle.END_ROD, beamPoint, 5, 0.1, 0.1, 0.1, 0);
            beamPoint.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, beamPoint, 3, 0.1, 0.1, 0.1, 0);
        }

        // One-shot damage to players still at target location
        for (Player p : findNearbyPlayers(aggroRadius)) {
            if (p.getLocation().distanceSquared(targetLoc) < 9) {
                p.damage(200.0, hitboxEntity); // One-shot if not dodged
                p.sendMessage(Component.text("You were hit by the beam!",
                        NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD));
            }
        }
    }

    // ── Phase Transitions ─────────────────────────────────────────────

    @Override
    protected void onPhaseTransition(int oldPhase, int newPhase) {
        super.onPhaseTransition(oldPhase, newPhase);
        Location loc = hitboxEntity.getLocation();

        if (newPhase == 1) {
            loc.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0f, 0.5f);
            loc.getWorld().spawnParticle(Particle.FLAME, loc, 80, 3, 3, 3, 0.1);
            for (Player p : findNearbyPlayers(aggroRadius)) {
                p.sendMessage(Component.text("King Gleeok's heads coordinate their attacks!",
                        NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
            }
        } else if (newPhase == 2) {
            loc.getWorld().playSound(loc, Sound.ENTITY_WITHER_SPAWN, 2.5f, 0.5f);
            loc.getWorld().spawnParticle(Particle.END_ROD, loc, 100, 4, 4, 4, 0.2);
            enraged = true;
            for (Player p : findNearbyPlayers(aggroRadius)) {
                p.sendMessage(Component.text("King Gleeok prepares its ultimate attack!",
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
            loc.getWorld().playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 2.0f, 0.7f);
            loc.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, loc, 300, 4, 4, 4, 0.5);
            for (int i = 0; i < 3; i++) {
                loc.getWorld().strikeLightningEffect(loc.clone().add(
                        random.nextGaussian() * 3, 0, random.nextGaussian() * 3));
            }
        }

        for (Player p : findNearbyPlayers(200)) {
            p.sendMessage(Component.text("King Gleeok has been slain!",
                    NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
        }

        if (killer != null) {
            killer.giveExp(8000);
        }
    }

    @Override
    protected List<ItemStack> getDrops(Player killer) {
        List<ItemStack> drops = new ArrayList<>();

        // Up to 5 MYTHIC legendary items
        LegendaryWeapon[] mythicWeapons = LegendaryWeapon.byTier(LegendaryTier.MYTHIC);
        if (mythicWeapons.length > 0) {
            LegendaryWeaponManager wm = JGlimsPlugin.getInstance().getLegendaryWeaponManager();
            int count = 3 + random.nextInt(3); // 3-5
            for (int i = 0; i < count; i++) {
                drops.add(wm.createWeapon(mythicWeapons[random.nextInt(mythicWeapons.length)]));
            }
        }

        // Dragon egg
        drops.add(new ItemStack(Material.DRAGON_EGG, 1));

        // 5 nether stars
        drops.add(new ItemStack(Material.NETHER_STAR, 5));

        // 64 diamonds
        drops.add(new ItemStack(Material.DIAMOND, 64));

        return drops;
    }
}

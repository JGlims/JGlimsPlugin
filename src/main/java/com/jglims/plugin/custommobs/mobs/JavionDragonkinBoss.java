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
 * Javion Dragonkin - Ancient armored dragon-humanoid. HP 1100.
 * 3 phases (50%/20%). Martial arts combos, flame breath,
 * ancient blade sweeping, and aerial dive with flame waves.
 */
public class JavionDragonkinBoss extends CustomWorldBoss {

    private final Random random = new Random();
    private boolean hasWings = false;

    public JavionDragonkinBoss(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.JAVION_DRAGONKIN);
        phaseThresholds = new double[]{0.50, 0.20};
        aggroRadius = 40.0;
        arenaRadius = 45.0;
        announceRadius = 120.0;
    }

    @Override
    protected void onSpawn() {
        super.onSpawn();
        if (hitboxEntity != null) {
            Location loc = hitboxEntity.getLocation();
            loc.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.5f, 0.9f);
            loc.getWorld().spawnParticle(Particle.FLAME, loc, 30, 1, 1, 1, 0.05);
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
        // Martial arts 3-hit combo
        if (distSq < 12 && isAttackReady("martial_combo", 30)) {
            useAttack("martial_combo");
            performMartialCombo(target);
        }

        // Flame breath
        if (distSq < 100 && distSq > 9 && isAttackReady("flame_breath", 60)) {
            useAttack("flame_breath");
            performFlameBreath(target);
        }

        // Roundhouse kick
        if (distSq < 16 && isAttackReady("roundhouse", 45)) {
            useAttack("roundhouse");
            performRoundhouseKick(target);
        }
    }

    private void phase2Tick(Player target, double distSq) {
        // Ancient blade sweeping attacks (extended range)
        if (distSq < 36 && isAttackReady("blade_sweep", 25)) {
            useAttack("blade_sweep");
            performBladeSweep(target);
        }

        // Flame breath (faster)
        if (distSq < 100 && distSq > 9 && isAttackReady("flame_breath", 45)) {
            useAttack("flame_breath");
            performFlameBreath(target);
        }

        // Martial combo still available
        if (distSq < 12 && isAttackReady("martial_combo", 25)) {
            useAttack("martial_combo");
            performMartialCombo(target);
        }

        // Dragon tail sweep
        if (isAttackReady("dragon_tail", 50)) {
            useAttack("dragon_tail");
            performDragonTailSweep();
        }
    }

    private void phase3Tick(Player target, double distSq) {
        // Has wings now - aerial dive
        if (isAttackReady("aerial_dive", 70)) {
            useAttack("aerial_dive");
            performAerialDive(target);
        }

        // Flame wave
        if (isAttackReady("flame_wave", 50)) {
            useAttack("flame_wave");
            performFlameWave();
        }

        // Faster blade sweep
        if (distSq < 36 && isAttackReady("blade_sweep", 18)) {
            useAttack("blade_sweep");
            performBladeSweep(target);
        }

        // Ground attacks still available
        if (distSq < 12 && isAttackReady("martial_combo", 18)) {
            useAttack("martial_combo");
            performMartialCombo(target);
        }

        if (distSq < 100 && distSq > 9 && isAttackReady("flame_breath", 35)) {
            useAttack("flame_breath");
            performFlameBreath(target);
        }
    }

    // ── Attack Implementations ────────────────────────────────────────

    private void performMartialCombo(Player target) {
        playAnimation("martial_combo", AnimationIterator.Type.PLAY_ONCE);

        for (int i = 0; i < 3; i++) {
            final int hit = i;
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!alive || hitboxEntity == null) return;
                    if (hitboxEntity.getLocation().distanceSquared(target.getLocation()) > 16) return;
                    target.damage(10.0, hitboxEntity);
                    hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                            Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.0f + hit * 0.15f);
                    hitboxEntity.getWorld().spawnParticle(Particle.SWEEP_ATTACK,
                            target.getLocation().add(0, 1, 0), 1, 0, 0, 0, 0);
                }
            }.runTaskLater(plugin, (long) i * 5);
        }
    }

    private void performFlameBreath(Player target) {
        playAnimation("flame_breath", AnimationIterator.Type.PLAY_ONCE);
        Location loc = hitboxEntity.getLocation();
        Vector dir = target.getLocation().toVector().subtract(loc.toVector()).normalize();
        loc.getWorld().playSound(loc, Sound.ENTITY_BLAZE_SHOOT, 1.5f, 0.7f);

        for (int i = 1; i <= 10; i++) {
            Location pLoc = loc.clone().add(dir.clone().multiply(i)).add(0, 1.5, 0);
            loc.getWorld().spawnParticle(Particle.FLAME, pLoc, 10, 0.4, 0.4, 0.4, 0.03);
        }

        for (Player p : findNearbyPlayers(10)) {
            Vector toPlayer = p.getLocation().toVector().subtract(loc.toVector()).normalize();
            if (toPlayer.dot(dir) > 0.5) {
                p.damage(15.0, hitboxEntity);
                p.setFireTicks(60);
            }
        }
    }

    private void performRoundhouseKick(Player target) {
        playAnimation("kick", AnimationIterator.Type.PLAY_ONCE);
        hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 1.5f, 0.8f);

        target.damage(14.0, hitboxEntity);
        Vector kb = target.getLocation().toVector()
                .subtract(hitboxEntity.getLocation().toVector())
                .normalize().multiply(1.8).setY(0.6);
        target.setVelocity(kb);
    }

    private void performBladeSweep(Player target) {
        playAnimation("blade_sweep", AnimationIterator.Type.PLAY_ONCE);
        Location loc = hitboxEntity.getLocation();
        hitboxEntity.getWorld().playSound(loc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.5f, 0.7f);

        // Extended range 6-block sweep
        Vector facing = loc.getDirection().normalize();
        for (double angle = -Math.PI / 3; angle <= Math.PI / 3; angle += Math.PI / 12) {
            Vector sweep = facing.clone().rotateAroundY(angle).multiply(6);
            loc.getWorld().spawnParticle(Particle.SWEEP_ATTACK,
                    loc.clone().add(sweep).add(0, 1, 0), 1, 0, 0, 0, 0);
        }

        for (Player p : findNearbyPlayers(6)) {
            Vector toPlayer = p.getLocation().toVector().subtract(loc.toVector()).normalize();
            if (toPlayer.dot(facing) > 0.0) {
                p.damage(18.0, hitboxEntity);
                p.setVelocity(toPlayer.multiply(0.8).setY(0.4));
            }
        }
    }

    private void performDragonTailSweep() {
        playAnimation("tail_sweep", AnimationIterator.Type.PLAY_ONCE);
        Location loc = hitboxEntity.getLocation();
        hitboxEntity.getWorld().playSound(loc, Sound.ENTITY_IRON_GOLEM_ATTACK, 1.5f, 0.8f);

        for (Player p : findNearbyPlayers(5)) {
            p.damage(12.0, hitboxEntity);
            Vector kb = p.getLocation().toVector()
                    .subtract(loc.toVector()).normalize().multiply(1.5).setY(0.5);
            p.setVelocity(kb);
        }
    }

    private void performAerialDive(Player target) {
        playAnimation("aerial_dive", AnimationIterator.Type.PLAY_ONCE);
        Location above = target.getLocation().add(0, 12, 0);
        moveTo(above);

        hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                Sound.ENTITY_ENDER_DRAGON_FLAP, 2.0f, 0.8f);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!alive || hitboxEntity == null) return;
                moveTo(target.getLocation());
                Location loc = hitboxEntity.getLocation();
                loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.8f);
                loc.getWorld().spawnParticle(Particle.FLAME, loc, 50, 3, 1, 3, 0.1);

                for (Player p : findNearbyPlayers(6)) {
                    p.damage(25.0, hitboxEntity);
                    p.setFireTicks(60);
                    Vector kb = p.getLocation().toVector()
                            .subtract(loc.toVector()).normalize().multiply(1.5).setY(0.8);
                    p.setVelocity(kb);
                }
            }
        }.runTaskLater(plugin, 15L);
    }

    private void performFlameWave() {
        playAnimation("flame_wave", AnimationIterator.Type.PLAY_ONCE);
        Location loc = hitboxEntity.getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_BLAZE_SHOOT, 2.0f, 0.5f);

        // Expanding flame ring
        for (int r = 1; r <= 10; r++) {
            final double radius = r;
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 10) {
                        double x = Math.cos(angle) * radius;
                        double z = Math.sin(angle) * radius;
                        loc.getWorld().spawnParticle(Particle.FLAME,
                                loc.clone().add(x, 0.3, z), 3, 0.1, 0.1, 0.1, 0.01);
                    }
                }
            }.runTaskLater(plugin, (long) (r * 2));
        }

        // Delayed damage
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : findNearbyPlayers(10)) {
                    p.damage(16.0, hitboxEntity);
                    p.setFireTicks(80);
                }
            }
        }.runTaskLater(plugin, 10L);
    }

    // ── Phase Transitions ─────────────────────────────────────────────

    @Override
    protected void onPhaseTransition(int oldPhase, int newPhase) {
        super.onPhaseTransition(oldPhase, newPhase);
        Location loc = hitboxEntity.getLocation();

        if (newPhase == 1) {
            playAnimation("draw_blade", AnimationIterator.Type.PLAY_ONCE);
            loc.getWorld().playSound(loc, Sound.ITEM_TRIDENT_RETURN, 2.0f, 0.6f);
            loc.getWorld().spawnParticle(Particle.ENCHANTED_HIT, loc, 30, 1, 1, 1, 0.1);
            for (Player p : findNearbyPlayers(aggroRadius)) {
                p.sendMessage(Component.text("Javion draws the Ancient Blade!",
                        NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
            }
        } else if (newPhase == 2) {
            hasWings = true;
            enraged = true;
            playAnimation("grow_wings", AnimationIterator.Type.PLAY_ONCE);
            loc.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0f, 0.7f);
            loc.getWorld().spawnParticle(Particle.FLAME, loc, 80, 3, 3, 3, 0.1);
            for (Player p : findNearbyPlayers(aggroRadius)) {
                p.sendMessage(Component.text("Javion unleashes his dragon wings!",
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
            p.sendMessage(Component.text("Javion Dragonkin has been defeated!",
                    NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
        }

        if (killer != null) {
            killer.giveExp(4000);
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

        // 32 diamonds
        drops.add(new ItemStack(Material.DIAMOND, 32));

        // 1 nether star
        drops.add(new ItemStack(Material.NETHER_STAR, 1));

        return drops;
    }
}

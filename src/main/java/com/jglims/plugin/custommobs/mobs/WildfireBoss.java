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
 * Wildfire - Enhanced blaze boss. HP 500.
 * Fire immune. Fire volley (8 projectiles), flame pillar,
 * fire nova (8-block AoE), shield mode (spinning fire 5s).
 * Single-phase boss with mixed attack pattern.
 */
public class WildfireBoss extends CustomWorldBoss {

    private final Random random = new Random();
    private boolean shieldActive = false;

    public WildfireBoss(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.WILDFIRE);
        phaseThresholds = new double[]{0.40};
        aggroRadius = 30.0;
        arenaRadius = 35.0;
        announceRadius = 80.0;
    }

    @Override
    protected void onSpawn() {
        super.onSpawn();
        if (hitboxEntity != null) {
            Location loc = hitboxEntity.getLocation();
            loc.getWorld().playSound(loc, Sound.ENTITY_BLAZE_AMBIENT, 2.0f, 0.5f);
            loc.getWorld().spawnParticle(Particle.FLAME, loc, 40, 2, 2, 2, 0.1);
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

        // Ambient fire particles
        if (combatTicks % 5 == 0) {
            hitboxEntity.getWorld().spawnParticle(Particle.FLAME,
                    hitboxEntity.getLocation().add(0, 1, 0), 5, 0.3, 0.3, 0.3, 0.02);
        }

        int phaseMod = currentPhase >= 1 ? 1 : 0;

        // Fire volley (8 projectiles)
        if (distSq > 9 && isAttackReady("fire_volley", 50 - phaseMod * 15)) {
            useAttack("fire_volley");
            performFireVolley(target);
        }

        // Flame pillar
        if (isAttackReady("flame_pillar", 70 - phaseMod * 20)) {
            useAttack("flame_pillar");
            performFlamePillar(target);
        }

        // Fire nova (8-block AoE)
        if (isAttackReady("fire_nova", 80 - phaseMod * 20)) {
            useAttack("fire_nova");
            performFireNova();
        }

        // Shield mode (5 seconds)
        if (!shieldActive && isAttackReady("shield_mode", 120 - phaseMod * 30)) {
            useAttack("shield_mode");
            activateShieldMode();
        }

        // Melee when close
        if (distSq < 12 && isAttackReady("fire_touch", 20)) {
            useAttack("fire_touch");
            target.damage(12.0, hitboxEntity);
            target.setFireTicks(60);
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                    Sound.ENTITY_BLAZE_HURT, 1.0f, 1.0f);
        }
    }

    // ── Attack Implementations ────────────────────────────────────────

    private void performFireVolley(Player target) {
        playAnimation("fire_volley", AnimationIterator.Type.PLAY_ONCE);
        Location loc = hitboxEntity.getLocation().add(0, 1.5, 0);
        loc.getWorld().playSound(loc, Sound.ENTITY_BLAZE_SHOOT, 2.0f, 0.8f);

        // 8 fire projectiles in a spread
        Vector baseDir = target.getLocation().toVector().subtract(loc.toVector()).normalize();
        for (int i = 0; i < 8; i++) {
            double spread = (i - 3.5) * 0.15;
            Vector dir = baseDir.clone().rotateAroundY(spread);
            final Vector fireDir = dir.normalize();

            new BukkitRunnable() {
                Location current = loc.clone();
                int steps = 0;
                @Override
                public void run() {
                    if (!alive || steps > 20) { cancel(); return; }
                    current.add(fireDir.clone().multiply(1.2));
                    current.getWorld().spawnParticle(Particle.FLAME, current, 3, 0.1, 0.1, 0.1, 0.01);

                    for (Player p : findNearbyPlayers(aggroRadius)) {
                        if (p.getLocation().distanceSquared(current) < 4) {
                            p.damage(6.0, hitboxEntity);
                            p.setFireTicks(40);
                            cancel();
                            return;
                        }
                    }
                    steps++;
                }
            }.runTaskTimer(plugin, (long) i * 2, 1L);
        }
    }

    private void performFlamePillar(Player target) {
        playAnimation("flame_pillar", AnimationIterator.Type.PLAY_ONCE);
        Location targetLoc = target.getLocation().clone();
        hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                Sound.ENTITY_BLAZE_SHOOT, 1.5f, 0.5f);

        // Warning circle
        targetLoc.getWorld().spawnParticle(Particle.DUST, targetLoc, 20, 1.5, 0.1, 1.5, 0,
                new Particle.DustOptions(Color.ORANGE, 1.5f));

        new BukkitRunnable() {
            @Override
            public void run() {
                // Eruption
                for (int y = 0; y < 8; y++) {
                    targetLoc.getWorld().spawnParticle(Particle.FLAME,
                            targetLoc.clone().add(0, y, 0), 15, 0.5, 0.2, 0.5, 0.05);
                }
                targetLoc.getWorld().playSound(targetLoc, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.8f);

                for (Player p : findNearbyPlayers(aggroRadius)) {
                    if (p.getLocation().distanceSquared(targetLoc) < 6) {
                        p.damage(14.0, hitboxEntity);
                        p.setFireTicks(80);
                        p.setVelocity(new Vector(0, 1.0, 0));
                    }
                }
            }
        }.runTaskLater(plugin, 15L);
    }

    private void performFireNova() {
        playAnimation("nova", AnimationIterator.Type.PLAY_ONCE);
        Location loc = hitboxEntity.getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.6f);

        // Expanding fire ring
        for (int r = 1; r <= 8; r++) {
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
            }.runTaskLater(plugin, (long) r * 2);
        }

        for (Player p : findNearbyPlayers(8)) {
            p.damage(15.0, hitboxEntity);
            p.setFireTicks(80);
            Vector kb = p.getLocation().toVector()
                    .subtract(loc.toVector()).normalize().multiply(1.5).setY(0.5);
            p.setVelocity(kb);
        }
    }

    private void activateShieldMode() {
        shieldActive = true;
        playAnimation("shield", AnimationIterator.Type.LOOP);
        Location loc = hitboxEntity.getLocation();
        loc.getWorld().playSound(loc, Sound.BLOCK_BEACON_ACTIVATE, 2.0f, 1.0f);

        for (Player p : findNearbyPlayers(aggroRadius)) {
            p.sendMessage(Component.text("Wildfire activates fire shield!",
                    NamedTextColor.RED).decorate(TextDecoration.ITALIC));
        }

        // Spinning fire blocks for 5 seconds (50 ticks)
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (!alive || hitboxEntity == null) { cancel(); shieldActive = false; return; }
                ticks += 2;
                Location center = hitboxEntity.getLocation().add(0, 1.5, 0);
                double angle = ticks * 0.3;

                for (int i = 0; i < 4; i++) {
                    double a = angle + (Math.PI / 2) * i;
                    double x = Math.cos(a) * 2.5;
                    double z = Math.sin(a) * 2.5;
                    center.getWorld().spawnParticle(Particle.FLAME,
                            center.clone().add(x, 0, z), 5, 0.1, 0.1, 0.1, 0.01);
                }

                // Damage players touching the shield
                for (Player p : findNearbyPlayers(3.5)) {
                    p.damage(5.0, hitboxEntity);
                    p.setFireTicks(40);
                }

                if (ticks >= 50) {
                    cancel();
                    shieldActive = false;
                    playAnimation("walk", AnimationIterator.Type.LOOP);
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    // ── Damage Override (fire immune) ─────────────────────────────────

    @Override
    protected void onDamage(double amount, Player source) {
        // Shield reduces damage
        if (shieldActive && hitboxEntity != null) {
            double restore = amount * 0.5;
            double maxHp = mobType.getMaxHealth();
            hitboxEntity.setHealth(Math.min(hitboxEntity.getHealth() + restore, maxHp));
        }
    }

    // ── Phase Transitions ─────────────────────────────────────────────

    @Override
    protected void onPhaseTransition(int oldPhase, int newPhase) {
        super.onPhaseTransition(oldPhase, newPhase);
        if (newPhase == 1) {
            enraged = true;
            Location loc = hitboxEntity.getLocation();
            loc.getWorld().playSound(loc, Sound.ENTITY_BLAZE_AMBIENT, 2.0f, 0.3f);
            loc.getWorld().spawnParticle(Particle.FLAME, loc, 80, 3, 3, 3, 0.15);
            for (Player p : findNearbyPlayers(aggroRadius)) {
                p.sendMessage(Component.text("Wildfire intensifies!",
                        NamedTextColor.RED).decorate(TextDecoration.BOLD));
            }
        }
    }

    // ── Death + Drops ─────────────────────────────────────────────────

    @Override
    protected void onDeath(Player killer) {
        super.onDeath(killer);
        shieldActive = false;
        if (hitboxEntity != null) {
            Location loc = hitboxEntity.getLocation();
            loc.getWorld().playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 2.0f, 0.9f);
            loc.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, loc, 100, 2, 2, 2, 0.3);
        }

        for (Player p : findNearbyPlayers(80)) {
            p.sendMessage(Component.text("The Wildfire has been extinguished!",
                    NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
        }

        if (killer != null) {
            killer.giveExp(1500);
        }
    }

    @Override
    protected List<ItemStack> getDrops(Player killer) {
        List<ItemStack> drops = new ArrayList<>();

        // 1 EPIC legendary weapon
        LegendaryWeapon[] epicWeapons = LegendaryWeapon.byTier(LegendaryTier.EPIC);
        if (epicWeapons.length > 0) {
            LegendaryWeaponManager wm = JGlimsPlugin.getInstance().getLegendaryWeaponManager();
            drops.add(wm.createWeapon(epicWeapons[random.nextInt(epicWeapons.length)]));
        }

        // Blaze rods x12
        drops.add(new ItemStack(Material.BLAZE_ROD, 12));

        // Fire charges x16
        drops.add(new ItemStack(Material.FIRE_CHARGE, 16));

        return drops;
    }
}

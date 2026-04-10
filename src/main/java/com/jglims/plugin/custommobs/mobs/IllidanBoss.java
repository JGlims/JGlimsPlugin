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

import java.util.*;

/**
 * Illidan - Fast mobile warglaive boss. HP 1200.
 * 3 phases (50%/20%). Rapid hit chains, dash attacks,
 * metamorphosis with flight and glaive boomerangs, and
 * a player marking mechanic for 200% damage.
 */
public class IllidanBoss extends CustomWorldBoss {

    private final Random random = new Random();
    private boolean metamorphosed = false;
    private UUID markedPlayerUUID = null;
    private long markedTick = 0;
    private static final long MARK_DURATION = 200; // 20 seconds at tick rate 2

    public IllidanBoss(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.ILLIDAN);
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
            loc.getWorld().playSound(loc, Sound.ENTITY_ENDERMAN_SCREAM, 2.0f, 0.8f);
            loc.getWorld().spawnParticle(Particle.WITCH, loc, 50, 2, 2, 2, 0.1);
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

        // Mark particle effects
        updateMarkEffects();

        double distSq = hitboxEntity.getLocation().distanceSquared(target.getLocation());

        switch (currentPhase) {
            case 0 -> phase1Tick(target, distSq);
            case 1 -> phase2Tick(target, distSq);
            default -> phase3Tick(target, distSq);
        }
    }

    private void phase1Tick(Player target, double distSq) {
        // Rapid 4-hit chain
        if (distSq < 16 && isAttackReady("hit_chain", 35)) {
            useAttack("hit_chain");
            performHitChain(target);
        }

        // Dash attack
        if (distSq > 25 && distSq < 400 && isAttackReady("dash_attack", 50)) {
            useAttack("dash_attack");
            performDashAttack(target);
        }
    }

    private void phase2Tick(Player target, double distSq) {
        // Metamorphosis: flight, glaive projectiles, flame crash
        if (!metamorphosed) return; // Transition triggers metamorphosis

        // Glaive boomerang projectile
        if (isAttackReady("glaive_throw", 40)) {
            useAttack("glaive_throw");
            performGlaiveBoomerang(target);
        }

        // Flame crash — dive from flight
        if (isAttackReady("flame_crash", 80)) {
            useAttack("flame_crash");
            performFlameCrash(target);
        }

        // Still uses dash attack
        if (distSq > 25 && isAttackReady("dash_attack", 35)) {
            useAttack("dash_attack");
            performDashAttack(target);
        }

        // Melee when close
        if (distSq < 16 && isAttackReady("hit_chain", 25)) {
            useAttack("hit_chain");
            performHitChain(target);
        }
    }

    private void phase3Tick(Player target, double distSq) {
        // Mark system
        if (markedPlayerUUID == null && isAttackReady("mark_player", 150)) {
            useAttack("mark_player");
            markPlayer(target);
        }

        // All phase 2 attacks (faster)
        if (isAttackReady("glaive_throw", 25)) {
            useAttack("glaive_throw");
            performGlaiveBoomerang(target);
        }

        if (isAttackReady("flame_crash", 60)) {
            useAttack("flame_crash");
            performFlameCrash(target);
        }

        if (distSq > 25 && isAttackReady("dash_attack", 25)) {
            useAttack("dash_attack");
            performDashAttack(target);
        }

        if (distSq < 16 && isAttackReady("hit_chain", 18)) {
            useAttack("hit_chain");
            performHitChain(target);
        }
    }

    // ── Attack Implementations ────────────────────────────────────────

    private double getMarkMultiplier(Player target) {
        if (markedPlayerUUID != null && target.getUniqueId().equals(markedPlayerUUID)) {
            return 2.0; // 200% damage to marked player
        }
        return 1.0;
    }

    private void performHitChain(Player target) {
        double multiplier = getMarkMultiplier(target);
        playAnimation("attack_chain", AnimationIterator.Type.PLAY_ONCE);

        // 4 rapid hits over 1 second
        for (int i = 0; i < 4; i++) {
            final int hit = i;
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!alive || hitboxEntity == null) return;
                    if (hitboxEntity.getLocation().distanceSquared(target.getLocation()) > 25) return;

                    target.damage(8.0 * multiplier, hitboxEntity);
                    hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                            Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.0f + hit * 0.1f);
                    hitboxEntity.getWorld().spawnParticle(Particle.SWEEP_ATTACK,
                            target.getLocation().add(0, 1, 0), 1, 0, 0, 0, 0);
                }
            }.runTaskLater(plugin, (long) i * 5);
        }
    }

    private void performDashAttack(Player target) {
        playAnimation("dash", AnimationIterator.Type.PLAY_ONCE);
        double multiplier = getMarkMultiplier(target);
        Location start = hitboxEntity.getLocation();

        hitboxEntity.getWorld().playSound(start, Sound.ENTITY_ENDERMAN_TELEPORT, 1.5f, 1.2f);
        hitboxEntity.getWorld().spawnParticle(Particle.WITCH, start, 15, 0.5, 0.5, 0.5, 0.1);

        // Teleport behind target
        Vector behind = target.getLocation().getDirection().normalize().multiply(-2);
        Location dest = target.getLocation().add(behind);
        moveTo(dest);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!alive || hitboxEntity == null) return;
                hitboxEntity.getWorld().spawnParticle(Particle.WITCH,
                        hitboxEntity.getLocation(), 15, 0.5, 0.5, 0.5, 0.1);
                target.damage(16.0 * multiplier, hitboxEntity);
                hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                        Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.5f, 0.8f);
            }
        }.runTaskLater(plugin, 3L);
    }

    private void performGlaiveBoomerang(Player target) {
        playAnimation("throw_glaive", AnimationIterator.Type.PLAY_ONCE);
        double multiplier = getMarkMultiplier(target);
        Location from = hitboxEntity.getLocation().add(0, 1.5, 0);
        Vector dir = target.getLocation().toVector().subtract(from.toVector()).normalize();
        from.getWorld().playSound(from, Sound.ITEM_TRIDENT_THROW, 1.5f, 1.2f);

        // Outgoing path
        new BukkitRunnable() {
            int step = 0;
            boolean returning = false;
            Location current = from.clone();
            @Override
            public void run() {
                if (!alive || hitboxEntity == null) { cancel(); return; }

                if (!returning) {
                    current.add(dir.clone().multiply(1.5));
                    step++;
                    current.getWorld().spawnParticle(Particle.ENCHANTED_HIT, current, 5, 0.2, 0.2, 0.2, 0);

                    // Check hit on outgoing
                    for (Player p : findNearbyPlayers(aggroRadius)) {
                        if (p.getLocation().distanceSquared(current) < 4) {
                            p.damage(12.0 * multiplier, hitboxEntity);
                        }
                    }

                    if (step >= 15) {
                        returning = true;
                    }
                } else {
                    if (hitboxEntity == null) { cancel(); return; }
                    Vector returnDir = hitboxEntity.getLocation().add(0, 1.5, 0).toVector()
                            .subtract(current.toVector()).normalize();
                    current.add(returnDir.multiply(1.5));
                    step--;
                    current.getWorld().spawnParticle(Particle.ENCHANTED_HIT, current, 5, 0.2, 0.2, 0.2, 0);

                    // Check hit on return
                    for (Player p : findNearbyPlayers(aggroRadius)) {
                        if (p.getLocation().distanceSquared(current) < 4) {
                            p.damage(10.0 * multiplier, hitboxEntity);
                        }
                    }

                    if (step <= 0) cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    private void performFlameCrash(Player target) {
        playAnimation("flame_crash", AnimationIterator.Type.PLAY_ONCE);
        double multiplier = getMarkMultiplier(target);
        Location above = target.getLocation().add(0, 10, 0);
        moveTo(above);

        hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                Sound.ENTITY_ENDER_DRAGON_GROWL, 1.5f, 1.0f);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!alive || hitboxEntity == null) return;
                moveTo(target.getLocation());
                Location loc = hitboxEntity.getLocation();
                loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.8f);
                loc.getWorld().spawnParticle(Particle.FLAME, loc, 60, 3, 1, 3, 0.1);

                for (Player p : findNearbyPlayers(6)) {
                    p.damage(20.0 * multiplier, hitboxEntity);
                    p.setFireTicks(60);
                    Vector kb = p.getLocation().toVector()
                            .subtract(loc.toVector()).normalize().multiply(1.5).setY(0.7);
                    p.setVelocity(kb);
                }
            }
        }.runTaskLater(plugin, 15L);
    }

    private void markPlayer(Player target) {
        markedPlayerUUID = target.getUniqueId();
        markedTick = combatTicks;
        playAnimation("mark", AnimationIterator.Type.PLAY_ONCE);

        target.sendMessage(Component.text("Illidan marks you for death! Take 200% damage!",
                NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD));
        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 1.5f, 0.8f);

        for (Player p : findNearbyPlayers(aggroRadius)) {
            if (!p.getUniqueId().equals(target.getUniqueId())) {
                p.sendMessage(Component.text(target.getName() + " has been marked by Illidan!",
                        NamedTextColor.RED));
            }
        }
    }

    private void updateMarkEffects() {
        if (markedPlayerUUID == null) return;

        // Check mark expiration
        if (combatTicks - markedTick >= MARK_DURATION) {
            Player marked = plugin.getServer().getPlayer(markedPlayerUUID);
            if (marked != null) {
                marked.sendMessage(Component.text("Illidan's mark fades.",
                        NamedTextColor.GRAY).decorate(TextDecoration.ITALIC));
            }
            markedPlayerUUID = null;
            return;
        }

        // Persistent particles on marked player (no glowing)
        Player marked = plugin.getServer().getPlayer(markedPlayerUUID);
        if (marked != null && marked.isOnline() && combatTicks % 5 == 0) {
            marked.getWorld().spawnParticle(Particle.DUST,
                    marked.getLocation().add(0, 2.2, 0), 8, 0.3, 0.1, 0.3, 0,
                    new Particle.DustOptions(Color.fromRGB(139, 0, 0), 1.5f));
            marked.getWorld().spawnParticle(Particle.WITCH,
                    marked.getLocation().add(0, 1, 0), 3, 0.3, 0.5, 0.3, 0);
        }
    }

    // ── Phase Transitions ─────────────────────────────────────────────

    @Override
    protected void onPhaseTransition(int oldPhase, int newPhase) {
        super.onPhaseTransition(oldPhase, newPhase);
        Location loc = hitboxEntity.getLocation();

        if (newPhase == 1) {
            metamorphosed = true;
            playAnimation("metamorphosis", AnimationIterator.Type.PLAY_ONCE);
            loc.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0f, 0.8f);
            loc.getWorld().spawnParticle(Particle.FLAME, loc, 100, 3, 3, 3, 0.15);
            loc.getWorld().spawnParticle(Particle.WITCH, loc, 50, 2, 2, 2, 0.1);
            for (Player p : findNearbyPlayers(aggroRadius)) {
                p.sendMessage(Component.text("Illidan undergoes Metamorphosis!",
                        NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD));
            }
        } else if (newPhase == 2) {
            enraged = true;
            loc.getWorld().playSound(loc, Sound.ENTITY_WITHER_SPAWN, 2.0f, 0.6f);
            for (Player p : findNearbyPlayers(aggroRadius)) {
                p.sendMessage(Component.text("YOU ARE NOT PREPARED!",
                        NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD));
            }
        }
    }

    // ── Death + Drops ─────────────────────────────────────────────────

    @Override
    protected void onDeath(Player killer) {
        super.onDeath(killer);
        markedPlayerUUID = null;
        if (hitboxEntity != null) {
            Location loc = hitboxEntity.getLocation();
            loc.getWorld().playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 2.0f, 0.8f);
            loc.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, loc, 200, 3, 3, 3, 0.5);
        }

        for (Player p : findNearbyPlayers(120)) {
            p.sendMessage(Component.text("Illidan has been defeated!",
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

        // Ender pearls x8
        drops.add(new ItemStack(Material.ENDER_PEARL, 8));

        // 32 diamonds
        drops.add(new ItemStack(Material.DIAMOND, 32));

        return drops;
    }
}

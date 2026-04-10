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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Shadowsail - Shadowy dragon in End Cities. HP 600.
 * Shadow breath (darkness AoE), phase shift (intangible, repositions),
 * shadow clone (50HP decoy). 2 phases (40%).
 */
public class ShadowsailBoss extends CustomWorldBoss {

    private final Random random = new Random();
    private boolean phaseShifting = false;
    private Zombie shadowClone = null;

    public ShadowsailBoss(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.SHADOWSAIL);
        phaseThresholds = new double[]{0.40};
        aggroRadius = 35.0;
        arenaRadius = 40.0;
        announceRadius = 100.0;
    }

    @Override
    protected void onSpawn() {
        super.onSpawn();
        if (hitboxEntity != null) {
            Location loc = hitboxEntity.getLocation();
            loc.getWorld().playSound(loc, Sound.ENTITY_ENDERMAN_SCREAM, 1.5f, 0.6f);
            loc.getWorld().spawnParticle(Particle.SMOKE, loc, 40, 2, 2, 2, 0.1);
        }
    }

    // ── Combat Logic ──────────────────────────────────────────────────

    @Override
    protected void combatTick() {
        if (hitboxEntity == null || phaseShifting) return;

        Player target = findNearestPlayer(aggroRadius);
        if (target == null) return;

        if (hitboxEntity instanceof Mob mob) {
            mob.setTarget(target);
        }

        double distSq = hitboxEntity.getLocation().distanceSquared(target.getLocation());
        int phaseMod = currentPhase >= 1 ? 1 : 0;

        // Shadow breath (darkness AoE)
        if (distSq < 144 && isAttackReady("shadow_breath", 50 - phaseMod * 15)) {
            useAttack("shadow_breath");
            performShadowBreath(target);
        }

        // Phase shift
        if (isAttackReady("phase_shift", 80 - phaseMod * 20)) {
            useAttack("phase_shift");
            performPhaseShift();
        }

        // Shadow clone
        if (shadowClone == null && isAttackReady("shadow_clone", 120 - phaseMod * 30)) {
            useAttack("shadow_clone");
            summonShadowClone();
        }

        // Melee claw attack
        if (distSq < 12 && isAttackReady("shadow_claw", 25 - phaseMod * 5)) {
            useAttack("shadow_claw");
            performShadowClaw(target);
        }

        // Shadow bolts (ranged)
        if (distSq > 16 && distSq < 225 && isAttackReady("shadow_bolt", 35 - phaseMod * 10)) {
            useAttack("shadow_bolt");
            performShadowBolt(target);
        }

        // Clean up dead clones
        if (shadowClone != null && (shadowClone.isDead() || !shadowClone.isValid())) {
            shadowClone = null;
        }
    }

    // ── Attack Implementations ────────────────────────────────────────

    private void performShadowBreath(Player target) {
        playAnimation("shadow_breath", AnimationIterator.Type.PLAY_ONCE);
        Location loc = hitboxEntity.getLocation();
        Vector dir = target.getLocation().toVector().subtract(loc.toVector()).normalize();
        loc.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_SHOOT, 1.5f, 0.5f);

        for (int i = 1; i <= 10; i++) {
            Location pLoc = loc.clone().add(dir.clone().multiply(i)).add(0, 1.5, 0);
            loc.getWorld().spawnParticle(Particle.SMOKE, pLoc, 12, 0.5, 0.5, 0.5, 0.03);
            loc.getWorld().spawnParticle(Particle.ASH, pLoc, 8, 0.3, 0.3, 0.3, 0.02);
        }

        for (Player p : findNearbyPlayers(12)) {
            Vector toPlayer = p.getLocation().toVector().subtract(loc.toVector()).normalize();
            if (toPlayer.dot(dir) > 0.4) {
                p.damage(14.0, hitboxEntity);
                p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 80, 0));
                p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0));
            }
        }
    }

    private void performPhaseShift() {
        phaseShifting = true;
        playAnimation("phase_shift", AnimationIterator.Type.PLAY_ONCE);
        Location oldLoc = hitboxEntity.getLocation();
        oldLoc.getWorld().playSound(oldLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 2.0f, 0.6f);
        oldLoc.getWorld().spawnParticle(Particle.SMOKE, oldLoc, 30, 1, 1, 1, 0.1);

        // Become temporarily intangible (invulnerable effect)
        hitboxEntity.setInvulnerable(true);

        for (Player p : findNearbyPlayers(aggroRadius)) {
            p.sendMessage(Component.text("Shadowsail fades into darkness...",
                    NamedTextColor.DARK_GRAY).decorate(TextDecoration.ITALIC));
        }

        // Reposition behind a random player after 2 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!alive || hitboxEntity == null) { phaseShifting = false; return; }

                List<Player> nearby = findNearbyPlayers(aggroRadius);
                if (!nearby.isEmpty()) {
                    Player victim = nearby.get(random.nextInt(nearby.size()));
                    Vector behind = victim.getLocation().getDirection().normalize().multiply(-3);
                    Location dest = victim.getLocation().add(behind);
                    moveTo(dest);

                    dest.getWorld().playSound(dest, Sound.ENTITY_ENDERMAN_TELEPORT, 2.0f, 0.8f);
                    dest.getWorld().spawnParticle(Particle.SMOKE, dest, 30, 1, 1, 1, 0.1);

                    // Surprise attack
                    victim.damage(12.0, hitboxEntity);
                    victim.sendMessage(Component.text("Shadowsail strikes from the shadows!",
                            NamedTextColor.DARK_PURPLE));
                }

                hitboxEntity.setInvulnerable(false);
                phaseShifting = false;
            }
        }.runTaskLater(plugin, 20L);
    }

    private void summonShadowClone() {
        playAnimation("summon", AnimationIterator.Type.PLAY_ONCE);
        Location loc = hitboxEntity.getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_EVOKER_PREPARE_SUMMON, 1.5f, 0.6f);

        Location cloneLoc = loc.clone().add(random.nextGaussian() * 3, 0, random.nextGaussian() * 3);
        shadowClone = (Zombie) loc.getWorld().spawnEntity(cloneLoc, EntityType.ZOMBIE);
        shadowClone.customName(Component.text("Shadow Clone", NamedTextColor.DARK_GRAY));
        shadowClone.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).setBaseValue(50);
        shadowClone.setHealth(50);
        shadowClone.setInvisible(false);

        cloneLoc.getWorld().spawnParticle(Particle.SMOKE, cloneLoc, 25, 0.5, 1, 0.5, 0.05);

        for (Player p : findNearbyPlayers(aggroRadius)) {
            p.sendMessage(Component.text("Shadowsail creates a shadow clone!",
                    NamedTextColor.DARK_GRAY).decorate(TextDecoration.ITALIC));
        }
    }

    private void performShadowClaw(Player target) {
        playAnimation("claw_attack", AnimationIterator.Type.PLAY_ONCE);
        hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                Sound.ENTITY_PHANTOM_BITE, 1.5f, 0.6f);

        target.damage(16.0, hitboxEntity);
        target.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 40, 0));
        target.getWorld().spawnParticle(Particle.SMOKE,
                target.getLocation().add(0, 1, 0), 10, 0.3, 0.3, 0.3, 0.05);
    }

    private void performShadowBolt(Player target) {
        playAnimation("shadow_bolt", AnimationIterator.Type.PLAY_ONCE);
        Location from = hitboxEntity.getLocation().add(0, 1.5, 0);
        Vector dir = target.getLocation().toVector().subtract(from.toVector()).normalize();
        from.getWorld().playSound(from, Sound.ENTITY_SHULKER_SHOOT, 1.5f, 0.6f);

        new BukkitRunnable() {
            Location current = from.clone();
            int steps = 0;
            @Override
            public void run() {
                if (!alive || steps > 25) { cancel(); return; }
                current.add(dir.clone().multiply(1.0));
                current.getWorld().spawnParticle(Particle.SMOKE, current, 5, 0.15, 0.15, 0.15, 0.01);
                current.getWorld().spawnParticle(Particle.ASH, current, 3, 0.1, 0.1, 0.1, 0);

                for (Player p : findNearbyPlayers(aggroRadius)) {
                    if (p.getLocation().distanceSquared(current) < 4) {
                        p.damage(10.0, hitboxEntity);
                        p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 60, 0));
                        cancel();
                        return;
                    }
                }
                steps++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    // ── Phase Transitions ─────────────────────────────────────────────

    @Override
    protected void onPhaseTransition(int oldPhase, int newPhase) {
        super.onPhaseTransition(oldPhase, newPhase);
        if (newPhase == 1) {
            enraged = true;
            Location loc = hitboxEntity.getLocation();
            loc.getWorld().playSound(loc, Sound.ENTITY_WITHER_SPAWN, 1.5f, 0.8f);
            loc.getWorld().spawnParticle(Particle.SMOKE, loc, 80, 3, 3, 3, 0.15);
            for (Player p : findNearbyPlayers(aggroRadius)) {
                p.sendMessage(Component.text("Shadowsail merges with the void!",
                        NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD));
            }
        }
    }

    // ── Death + Drops ─────────────────────────────────────────────────

    @Override
    protected void onDeath(Player killer) {
        super.onDeath(killer);
        if (shadowClone != null && !shadowClone.isDead()) {
            shadowClone.remove();
            shadowClone = null;
        }
        if (hitboxEntity != null) {
            Location loc = hitboxEntity.getLocation();
            loc.getWorld().playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 2.0f, 0.9f);
            loc.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, loc, 100, 2, 2, 2, 0.3);
        }

        for (Player p : findNearbyPlayers(100)) {
            p.sendMessage(Component.text("Shadowsail has been banished!",
                    NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
        }

        if (killer != null) {
            killer.giveExp(2000);
        }
    }

    @Override
    protected List<ItemStack> getDrops(Player killer) {
        List<ItemStack> drops = new ArrayList<>();

        // 1 MYTHIC legendary weapon
        LegendaryWeapon[] mythicWeapons = LegendaryWeapon.byTier(LegendaryTier.MYTHIC);
        if (mythicWeapons.length > 0) {
            LegendaryWeaponManager wm = JGlimsPlugin.getInstance().getLegendaryWeaponManager();
            drops.add(wm.createWeapon(mythicWeapons[random.nextInt(mythicWeapons.length)]));
        }

        // End stone x32
        drops.add(new ItemStack(Material.END_STONE, 32));

        // Ender pearls x4
        drops.add(new ItemStack(Material.ENDER_PEARL, 4));

        return drops;
    }
}

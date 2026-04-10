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
 * Ghidorah - Three-headed golden dragon kaiju. HP 2800.
 * 3 phases (50%/20%). Independent head attacks: gravity beams,
 * wind storm, bites. P2 heads coordinate, P3 regenerates.
 */
public class GhidorahBoss extends CustomWorldBoss {

    private final Random random = new Random();

    /** Track which heads are "disabled" (temporarily knocked out). */
    private boolean leftHeadDisabled = false;
    private boolean rightHeadDisabled = false;
    private boolean centerHeadDisabled = false;

    private long leftHeadCooldown = 0;
    private long centerHeadCooldown = 0;
    private long rightHeadCooldown = 0;

    public GhidorahBoss(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.GHIDORAH);
        phaseThresholds = new double[]{0.50, 0.20};
        aggroRadius = 55.0;
        arenaRadius = 65.0;
        announceRadius = 200.0;
    }

    @Override
    protected void onSpawn() {
        super.onSpawn();
        if (hitboxEntity != null) {
            Location loc = hitboxEntity.getLocation();
            loc.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 3.0f, 0.4f);
            loc.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, loc, 80, 4, 4, 4, 0.2);
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

        // Independent head attacks
        if (!leftHeadDisabled) tickLeftHead(target, distSq);
        if (!centerHeadDisabled) tickCenterHead(target, distSq);
        if (!rightHeadDisabled) tickRightHead(target, distSq);

        switch (currentPhase) {
            case 0 -> phase1Extras(target, distSq);
            case 1 -> phase2Extras(target, distSq);
            default -> phase3Extras(target, distSq);
        }
    }

    // ── Left Head: Gravity Beam ───────────────────────────────────────

    private void tickLeftHead(Player target, double distSq) {
        long cooldown = currentPhase >= 1 ? 45 : 65;
        if (combatTicks - leftHeadCooldown < cooldown) return;
        leftHeadCooldown = combatTicks;

        playAnimation("left_head_attack", AnimationIterator.Type.PLAY_ONCE);
        Location loc = hitboxEntity.getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_GUARDIAN_ATTACK, 1.5f, 0.5f);

        // Gravity beam - pulls target toward boss
        Vector dir = target.getLocation().toVector().subtract(loc.toVector()).normalize();
        for (int i = 1; i <= 15; i++) {
            Location pLoc = loc.clone().add(dir.clone().multiply(i)).add(0, 2, 0);
            loc.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, pLoc, 6, 0.2, 0.2, 0.2, 0.02);
        }

        if (distSq < 400) {
            target.damage(14.0, hitboxEntity);
            // Pull toward boss
            Vector pull = loc.toVector().subtract(target.getLocation().toVector())
                    .normalize().multiply(0.8).setY(0.3);
            target.setVelocity(pull);
        }
    }

    // ── Center Head: Bite ─────────────────────────────────────────────

    private void tickCenterHead(Player target, double distSq) {
        long cooldown = currentPhase >= 1 ? 30 : 50;
        if (combatTicks - centerHeadCooldown < cooldown) return;
        if (distSq > 25) return; // Only melee range
        centerHeadCooldown = combatTicks;

        playAnimation("center_head_bite", AnimationIterator.Type.PLAY_ONCE);
        hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                Sound.ENTITY_IRON_GOLEM_ATTACK, 1.5f, 0.6f);

        target.damage(20.0, hitboxEntity);
        target.setVelocity(hitboxEntity.getLocation().getDirection().normalize().multiply(0.6));
    }

    // ── Right Head: Wind Storm ────────────────────────────────────────

    private void tickRightHead(Player target, double distSq) {
        long cooldown = currentPhase >= 1 ? 55 : 80;
        if (combatTicks - rightHeadCooldown < cooldown) return;
        rightHeadCooldown = combatTicks;

        playAnimation("right_head_attack", AnimationIterator.Type.PLAY_ONCE);
        Location loc = hitboxEntity.getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_FLAP, 2.0f, 0.5f);

        // Wind storm AoE - pushes all nearby players away
        for (Player p : findNearbyPlayers(12)) {
            Vector push = p.getLocation().toVector().subtract(loc.toVector())
                    .normalize().multiply(2.0).setY(0.8);
            p.setVelocity(push);
            p.damage(10.0, hitboxEntity);
            p.getWorld().spawnParticle(Particle.CLOUD, p.getLocation(), 15, 0.5, 0.5, 0.5, 0.1);
        }
    }

    // ── Phase-specific extras ─────────────────────────────────────────

    private void phase1Extras(Player target, double distSq) {
        // Wing buffet
        if (isAttackReady("wing_buffet", 70)) {
            useAttack("wing_buffet");
            performWingBuffet();
        }
    }

    private void phase2Extras(Player target, double distSq) {
        // Coordinated attack - all three heads target same player
        if (isAttackReady("coordinated_strike", 100)) {
            useAttack("coordinated_strike");
            performCoordinatedStrike(target);
        }

        // Wing buffet (faster)
        if (isAttackReady("wing_buffet", 50)) {
            useAttack("wing_buffet");
            performWingBuffet();
        }
    }

    private void phase3Extras(Player target, double distSq) {
        phase2Extras(target, distSq);

        // Regenerate disabled heads
        if (combatTicks % 50 == 0) {
            if (leftHeadDisabled || rightHeadDisabled || centerHeadDisabled) {
                regenerateHeads();
            }
        }

        // Gravity storm - pulls all players in
        if (isAttackReady("gravity_storm", 120)) {
            useAttack("gravity_storm");
            performGravityStorm();
        }
    }

    // ── Special Attack Implementations ────────────────────────────────

    private void performWingBuffet() {
        playAnimation("wing_buffet", AnimationIterator.Type.PLAY_ONCE);
        Location loc = hitboxEntity.getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_FLAP, 2.5f, 0.4f);

        for (Player p : findNearbyPlayers(10)) {
            Vector push = p.getLocation().toVector().subtract(loc.toVector())
                    .normalize().multiply(2.5).setY(1.0);
            p.setVelocity(push);
            p.damage(12.0, hitboxEntity);
        }
        loc.getWorld().spawnParticle(Particle.CLOUD, loc, 40, 4, 2, 4, 0.1);
    }

    private void performCoordinatedStrike(Player target) {
        playAnimation("coordinated_attack", AnimationIterator.Type.PLAY_ONCE);
        Location loc = hitboxEntity.getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0f, 0.6f);

        for (Player p : findNearbyPlayers(aggroRadius)) {
            p.sendMessage(Component.text("Ghidorah's heads attack in unison!",
                    NamedTextColor.GOLD).decorate(TextDecoration.ITALIC));
        }

        // Triple gravity beam on target
        Vector dir = target.getLocation().toVector().subtract(loc.toVector()).normalize();
        for (int i = 1; i <= 20; i++) {
            Location pLoc = loc.clone().add(dir.clone().multiply(i)).add(0, 2, 0);
            loc.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, pLoc, 15, 0.3, 0.3, 0.3, 0.05);
        }

        if (hitboxEntity.getLocation().distanceSquared(target.getLocation()) < 625) {
            target.damage(35.0, hitboxEntity);
            target.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.SLOWNESS, 60, 2));
            target.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.WEAKNESS, 60, 1));
        }
    }

    private void performGravityStorm() {
        playAnimation("gravity_storm", AnimationIterator.Type.PLAY_ONCE);
        Location loc = hitboxEntity.getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_WARDEN_SONIC_BOOM, 2.5f, 0.4f);

        for (Player p : findNearbyPlayers(aggroRadius)) {
            p.sendMessage(Component.text("Ghidorah creates a gravity vortex!",
                    NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD));
        }

        // 4-second gravity pull
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (!alive || hitboxEntity == null) { cancel(); return; }
                ticks += 2;
                Location center = hitboxEntity.getLocation();
                center.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, center, 30, 5, 3, 5, 0.15);

                for (Player p : findNearbyPlayers(20)) {
                    Vector pull = center.toVector().subtract(p.getLocation().toVector())
                            .normalize().multiply(0.6);
                    p.setVelocity(p.getVelocity().add(pull));
                    p.damage(3.0, hitboxEntity);
                }

                if (ticks >= 40) cancel();
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    private void regenerateHeads() {
        Location loc = hitboxEntity.getLocation();
        if (leftHeadDisabled) {
            leftHeadDisabled = false;
            loc.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, loc.clone().add(-2, 3, 0),
                    20, 0.5, 0.5, 0.5, 0.1);
        }
        if (rightHeadDisabled) {
            rightHeadDisabled = false;
            loc.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, loc.clone().add(2, 3, 0),
                    20, 0.5, 0.5, 0.5, 0.1);
        }
        if (centerHeadDisabled) {
            centerHeadDisabled = false;
            loc.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, loc.clone().add(0, 4, 0),
                    20, 0.5, 0.5, 0.5, 0.1);
        }
        for (Player p : findNearbyPlayers(aggroRadius)) {
            p.sendMessage(Component.text("Ghidorah regenerates its heads!",
                    NamedTextColor.GOLD).decorate(TextDecoration.ITALIC));
        }
        loc.getWorld().playSound(loc, Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 2.0f, 0.5f);
    }

    // ── Phase Transitions ─────────────────────────────────────────────

    @Override
    protected void onPhaseTransition(int oldPhase, int newPhase) {
        super.onPhaseTransition(oldPhase, newPhase);
        Location loc = hitboxEntity.getLocation();

        if (newPhase == 1) {
            loc.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 2.5f, 0.5f);
            loc.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, loc, 100, 4, 4, 4, 0.3);
            for (Player p : findNearbyPlayers(aggroRadius)) {
                p.sendMessage(Component.text("Ghidorah's heads begin to coordinate!",
                        NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
            }
        } else if (newPhase == 2) {
            enraged = true;
            playAnimation("enrage", AnimationIterator.Type.PLAY_ONCE);
            loc.getWorld().playSound(loc, Sound.ENTITY_WITHER_SPAWN, 2.5f, 0.4f);
            loc.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, loc, 200, 5, 5, 5, 0.5);
            for (Player p : findNearbyPlayers(aggroRadius)) {
                p.sendMessage(Component.text("Ghidorah enters a regenerative frenzy!",
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
            loc.getWorld().playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 2.5f, 0.6f);
            loc.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, loc, 400, 5, 5, 5, 0.5);
            for (int i = 0; i < 3; i++) {
                loc.getWorld().strikeLightningEffect(loc.clone().add(
                        random.nextGaussian() * 4, 0, random.nextGaussian() * 4));
            }
        }

        for (Player p : findNearbyPlayers(200)) {
            p.sendMessage(Component.text("Ghidorah, the Golden Terror, has been slain!",
                    NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
        }

        if (killer != null) {
            killer.giveExp(9000);
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

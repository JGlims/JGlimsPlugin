package com.jglims.plugin.custommobs.mobs;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.custommobs.CustomMobType;
import com.jglims.plugin.custommobs.CustomWorldBoss;
import com.jglims.plugin.legendary.LegendaryTier;
import com.jglims.plugin.legendary.LegendaryWeapon;
import com.jglims.plugin.legendary.LegendaryWeaponManager;
import com.jglims.plugin.vampire.VampireManager;
import kr.toxicity.model.api.animation.AnimationIterator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Nether King - Absolute ruler of the Nether. HP 1500.
 * 4 phases (60%/35%/15%). Fire bolts, blaze summons, flaming sword melee,
 * lava geysers, flight rain of fire, and 5-minute enrage timer.
 */
public class NetherKingBoss extends CustomWorldBoss {

    private final Random random = new Random();
    private boolean isFlying = false;
    private boolean damageReductionActive = false;
    private long enrageTimerStart = -1;
    private static final long ENRAGE_TIMER_TICKS = 7500; // 5 minutes at tick rate 2

    public NetherKingBoss(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.NETHER_KING);
        phaseThresholds = new double[]{0.60, 0.35, 0.15};
        aggroRadius = 45.0;
        arenaRadius = 50.0;
        announceRadius = 150.0;
    }

    @Override
    protected void onSpawn() {
        super.onSpawn();
        if (hitboxEntity != null) {
            Location loc = hitboxEntity.getLocation();
            loc.getWorld().playSound(loc, Sound.ENTITY_WITHER_SPAWN, 2.0f, 0.6f);
            loc.getWorld().spawnParticle(Particle.FLAME, loc, 80, 3, 3, 3, 0.1);
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

        // Phase 4 enrage timer check
        if (currentPhase >= 3 && enrageTimerStart > 0) {
            if (combatTicks - enrageTimerStart >= ENRAGE_TIMER_TICKS) {
                performEnrageWipe();
                return;
            }
        }

        switch (currentPhase) {
            case 0 -> phase1Tick(target, distSq);
            case 1 -> phase2Tick(target, distSq);
            case 2 -> phase3Tick(target, distSq);
            default -> phase4Tick(target, distSq);
        }
    }

    private void phase1Tick(Player target, double distSq) {
        // Fire bolts
        if (distSq > 16 && isAttackReady("fire_bolt", 30)) {
            useAttack("fire_bolt");
            performFireBolt(target);
        }

        // Summon blazes
        if (isAttackReady("summon_blazes", 150)) {
            useAttack("summon_blazes");
            summonBlazes();
        }

        // Basic melee
        if (distSq < 16 && isAttackReady("melee", 25)) {
            useAttack("melee");
            playAnimation("attack", AnimationIterator.Type.PLAY_ONCE);
            target.damage(18.0, hitboxEntity);
            target.setFireTicks(40);
        }
    }

    private void phase2Tick(Player target, double distSq) {
        // Flaming sword melee (increased damage)
        if (distSq < 20 && isAttackReady("flaming_sword", 20)) {
            useAttack("flaming_sword");
            performFlamingSword(target);
        }

        // Lava geysers
        if (isAttackReady("lava_geyser", 60)) {
            useAttack("lava_geyser");
            performLavaGeyser();
        }

        // Fire bolts (faster)
        if (distSq > 16 && isAttackReady("fire_bolt", 20)) {
            useAttack("fire_bolt");
            performFireBolt(target);
        }

        if (isAttackReady("summon_blazes", 120)) {
            useAttack("summon_blazes");
            summonBlazes();
        }
    }

    private void phase3Tick(Player target, double distSq) {
        // Flight + rain of fire
        if (!isFlying && isAttackReady("take_flight", 200)) {
            useAttack("take_flight");
            startFlight();
        }

        if (isFlying) {
            // Rain of fire while flying
            if (isAttackReady("fire_rain", 15)) {
                useAttack("fire_rain");
                performFireRain();
            }
        } else {
            // Ground attacks
            if (distSq < 20 && isAttackReady("flaming_sword", 18)) {
                useAttack("flaming_sword");
                performFlamingSword(target);
            }
            if (isAttackReady("lava_geyser", 45)) {
                useAttack("lava_geyser");
                performLavaGeyser();
            }
        }
    }

    private void phase4Tick(Player target, double distSq) {
        // All attacks + 50% DR
        if (isFlying) {
            if (isAttackReady("fire_rain", 10)) {
                useAttack("fire_rain");
                performFireRain();
            }
        }

        if (distSq < 20 && isAttackReady("flaming_sword", 12)) {
            useAttack("flaming_sword");
            performFlamingSword(target);
        }

        if (distSq > 16 && isAttackReady("fire_bolt", 12)) {
            useAttack("fire_bolt");
            performFireBolt(target);
        }

        if (isAttackReady("lava_geyser", 30)) {
            useAttack("lava_geyser");
            performLavaGeyser();
        }

        if (isAttackReady("summon_blazes", 80)) {
            useAttack("summon_blazes");
            summonBlazes();
        }

        if (!isFlying && isAttackReady("take_flight", 120)) {
            useAttack("take_flight");
            startFlight();
        }
    }

    // ── Attack Implementations ────────────────────────────────────────

    private void performFireBolt(Player target) {
        playAnimation("cast", AnimationIterator.Type.PLAY_ONCE);
        Location loc = hitboxEntity.getLocation().add(0, 2, 0);
        Vector dir = target.getLocation().toVector().subtract(loc.toVector()).normalize();
        loc.getWorld().playSound(loc, Sound.ENTITY_BLAZE_SHOOT, 1.5f, 0.8f);

        for (int i = 1; i <= 15; i++) {
            Location pLoc = loc.clone().add(dir.clone().multiply(i));
            loc.getWorld().spawnParticle(Particle.FLAME, pLoc, 5, 0.15, 0.15, 0.15, 0.01);
        }

        if (hitboxEntity.getLocation().distanceSquared(target.getLocation()) < 225) {
            target.damage(12.0, hitboxEntity);
            target.setFireTicks(60);
        }
    }

    private void summonBlazes() {
        playAnimation("summon", AnimationIterator.Type.PLAY_ONCE);
        Location loc = hitboxEntity.getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_EVOKER_PREPARE_SUMMON, 1.5f, 0.6f);

        for (int i = 0; i < 3; i++) {
            Location spawnLoc = loc.clone().add(
                    random.nextGaussian() * 4, 1, random.nextGaussian() * 4);
            Blaze blaze = (Blaze) loc.getWorld().spawnEntity(spawnLoc, EntityType.BLAZE);
            blaze.customName(Component.text("Nether King's Blaze", NamedTextColor.GOLD));
        }

        for (Player p : findNearbyPlayers(aggroRadius)) {
            p.sendMessage(Component.text("The Nether King summons his blazes!",
                    NamedTextColor.GOLD).decorate(TextDecoration.ITALIC));
        }
    }

    private void performFlamingSword(Player target) {
        playAnimation("sword_attack", AnimationIterator.Type.PLAY_ONCE);
        hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.5f, 0.6f);

        target.damage(24.0, hitboxEntity);
        target.setFireTicks(80);
        target.setVelocity(hitboxEntity.getLocation().getDirection().normalize().multiply(0.8));

        // Fire particle sweep
        Location loc = hitboxEntity.getLocation();
        for (double angle = -Math.PI / 3; angle <= Math.PI / 3; angle += Math.PI / 12) {
            Vector sweep = loc.getDirection().clone().rotateAroundY(angle).multiply(3);
            loc.getWorld().spawnParticle(Particle.FLAME, loc.clone().add(sweep).add(0, 1, 0),
                    5, 0.1, 0.1, 0.1, 0.02);
        }
    }

    private void performLavaGeyser() {
        playAnimation("ground_slam", AnimationIterator.Type.PLAY_ONCE);
        List<Player> nearby = findNearbyPlayers(aggroRadius);
        if (nearby.isEmpty()) return;

        hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.5f);

        // Create geysers under random players
        for (int i = 0; i < Math.min(3, nearby.size()); i++) {
            Player victim = nearby.get(random.nextInt(nearby.size()));
            Location geyserLoc = victim.getLocation().clone();

            // Warning particles
            geyserLoc.getWorld().spawnParticle(Particle.LAVA, geyserLoc, 15, 0.5, 0, 0.5, 0);

            new BukkitRunnable() {
                @Override
                public void run() {
                    geyserLoc.getWorld().spawnParticle(Particle.FLAME, geyserLoc, 40, 0.3, 2, 0.3, 0.1);
                    geyserLoc.getWorld().spawnParticle(Particle.LAVA, geyserLoc, 20, 0.5, 1, 0.5, 0);
                    geyserLoc.getWorld().playSound(geyserLoc, Sound.BLOCK_LAVA_EXTINGUISH, 1.5f, 0.5f);

                    for (Player p : findNearbyPlayers(aggroRadius)) {
                        if (p.getLocation().distanceSquared(geyserLoc) < 9) {
                            p.damage(16.0, hitboxEntity);
                            p.setFireTicks(80);
                            p.setVelocity(new Vector(0, 1.2, 0));
                        }
                    }
                }
            }.runTaskLater(plugin, 20L);
        }
    }

    private void startFlight() {
        isFlying = true;
        playAnimation("fly", AnimationIterator.Type.LOOP);
        Location above = hitboxEntity.getLocation().clone().add(0, 10, 0);
        moveTo(above);

        hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                Sound.ENTITY_ENDER_DRAGON_FLAP, 2.0f, 0.6f);

        for (Player p : findNearbyPlayers(aggroRadius)) {
            p.sendMessage(Component.text("The Nether King takes flight!",
                    NamedTextColor.RED).decorate(TextDecoration.BOLD));
        }

        // Land after 10 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!alive || hitboxEntity == null) return;
                isFlying = false;
                if (spawnLocation != null) {
                    moveTo(spawnLocation);
                }
                playAnimation("walk", AnimationIterator.Type.LOOP);
            }
        }.runTaskLater(plugin, 100L);
    }

    private void performFireRain() {
        Location loc = hitboxEntity.getLocation();
        for (int i = 0; i < 3; i++) {
            Location target = loc.clone().add(
                    random.nextGaussian() * 10, -8, random.nextGaussian() * 10);
            target.getWorld().spawnParticle(Particle.FLAME, target, 20, 0.5, 0.5, 0.5, 0.05);
            target.getWorld().spawnParticle(Particle.LAVA, target, 5, 0.3, 0.3, 0.3, 0);

            for (Player p : findNearbyPlayers(aggroRadius)) {
                if (p.getLocation().distanceSquared(target) < 9) {
                    p.damage(10.0, hitboxEntity);
                    p.setFireTicks(60);
                }
            }
        }
    }

    private void performEnrageWipe() {
        Location loc = hitboxEntity.getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_WARDEN_SONIC_BOOM, 3.0f, 0.3f);

        for (Player p : findNearbyPlayers(aggroRadius * 2)) {
            p.damage(500.0, hitboxEntity);
            p.sendMessage(Component.text("The Nether King's fury consumes all!",
                    NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD));
        }
    }

    // ── Damage Override (Phase 4 DR) ──────────────────────────────────

    @Override
    protected void onDamage(double amount, Player source) {
        if (damageReductionActive && hitboxEntity != null) {
            // Restore 50% of damage taken
            double restore = amount * 0.5;
            double maxHp = mobType.getMaxHealth();
            hitboxEntity.setHealth(Math.min(hitboxEntity.getHealth() + restore, maxHp));
        }
    }

    // ── Phase Transitions ─────────────────────────────────────────────

    @Override
    protected void onPhaseTransition(int oldPhase, int newPhase) {
        super.onPhaseTransition(oldPhase, newPhase);
        Location loc = hitboxEntity.getLocation();

        switch (newPhase) {
            case 1 -> {
                loc.getWorld().playSound(loc, Sound.ENTITY_BLAZE_AMBIENT, 2.0f, 0.5f);
                for (Player p : findNearbyPlayers(aggroRadius)) {
                    p.sendMessage(Component.text("The Nether King draws his flaming blade!",
                            NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
                }
            }
            case 2 -> {
                loc.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0f, 0.5f);
                for (Player p : findNearbyPlayers(aggroRadius)) {
                    p.sendMessage(Component.text("The Nether King takes to the skies!",
                            NamedTextColor.RED).decorate(TextDecoration.BOLD));
                }
            }
            case 3 -> {
                enraged = true;
                damageReductionActive = true;
                enrageTimerStart = combatTicks;
                playAnimation("enrage", AnimationIterator.Type.PLAY_ONCE);
                loc.getWorld().playSound(loc, Sound.ENTITY_WITHER_SPAWN, 2.5f, 0.4f);
                loc.getWorld().spawnParticle(Particle.FLAME, loc, 150, 5, 5, 5, 0.15);
                for (Player p : findNearbyPlayers(aggroRadius)) {
                    p.sendMessage(Component.text("THE NETHER KING IS ENRAGED! 5 MINUTE TIMER!",
                            NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD));
                    p.sendMessage(Component.text("He takes 50% reduced damage!",
                            NamedTextColor.RED));
                }
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
            loc.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, loc, 200, 3, 3, 3, 0.5);
        }

        for (Player p : findNearbyPlayers(150)) {
            p.sendMessage(Component.text("The Nether King has been dethroned!",
                    NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
        }

        if (killer != null) {
            killer.giveExp(6000);
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

        // GUARANTEED Vampire Essence
        VampireManager vm = JGlimsPlugin.getInstance().getVampireManager();
        drops.add(vm.createVampireEssence());

        // 3 nether stars
        drops.add(new ItemStack(Material.NETHER_STAR, 3));

        // 4 netherite ingots
        drops.add(new ItemStack(Material.NETHERITE_INGOT, 4));

        // 48 diamonds
        drops.add(new ItemStack(Material.DIAMOND, 48));

        return drops;
    }
}

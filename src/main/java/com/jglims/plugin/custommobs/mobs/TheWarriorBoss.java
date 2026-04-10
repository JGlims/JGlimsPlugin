package com.jglims.plugin.custommobs.mobs;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.custommobs.CustomMobType;
import com.jglims.plugin.custommobs.CustomWorldBoss;
import com.jglims.plugin.legendary.LegendaryArmorManager;
import com.jglims.plugin.legendary.LegendaryArmorSet;
import com.jglims.plugin.legendary.LegendaryTier;
import com.jglims.plugin.legendary.LegendaryWeapon;
import com.jglims.plugin.legendary.LegendaryWeaponManager;
import kr.toxicity.model.api.animation.AnimationIterator;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
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
 * The Warrior - Final boss of the Aether dimension. HP 2000.
 * 4 phases (60%/30%/10%). Devastating melee, fire breath,
 * lightning strikes, shockwave, troll summons, and enrage aura.
 */
public class TheWarriorBoss extends CustomWorldBoss {

    private final Random random = new Random();
    private boolean damageAuraActive = false;

    public TheWarriorBoss(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.THE_WARRIOR);
        phaseThresholds = new double[]{0.60, 0.30, 0.10};
        aggroRadius = 45.0;
        arenaRadius = 50.0;
        announceRadius = 150.0;
    }

    @Override
    protected BossBar.Color getBossBarColor() {
        return BossBar.Color.RED;
    }

    @Override
    protected void onSpawn() {
        super.onSpawn();
        if (hitboxEntity != null) {
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                    Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0f, 0.5f);
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

        double dist = hitboxEntity.getLocation().distanceSquared(target.getLocation());

        switch (currentPhase) {
            case 0 -> phase1Tick(target, dist);
            case 1 -> phase2Tick(target, dist);
            case 2 -> phase3Tick(target, dist);
            default -> phase4Tick(target, dist);
        }

        // Phase 4 damage aura
        if (damageAuraActive && combatTicks % 10 == 0) {
            for (Player p : findNearbyPlayers(6)) {
                p.damage(4.0, hitboxEntity);
                p.getWorld().spawnParticle(Particle.FLAME, p.getLocation(), 5, 0.3, 0.5, 0.3, 0.02);
            }
        }
    }

    private void phase1Tick(Player target, double distSq) {
        // Melee swipes
        if (distSq < 16 && isAttackReady("melee_swipe", 25)) {
            useAttack("melee_swipe");
            playAnimation("attack", AnimationIterator.Type.PLAY_ONCE);
            target.damage(20.0, hitboxEntity);
            target.setVelocity(hitboxEntity.getLocation().getDirection().normalize().multiply(0.8));
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                    Sound.ENTITY_IRON_GOLEM_ATTACK, 1.5f, 0.7f);
        }

        // Fire breath (ranged cone)
        if (distSq < 100 && distSq > 9 && isAttackReady("fire_breath", 80)) {
            useAttack("fire_breath");
            performFireBreath();
        }
    }

    private void phase2Tick(Player target, double distSq) {
        // Lightning strikes on random nearby players
        if (isAttackReady("lightning", 60)) {
            useAttack("lightning");
            performLightningStrikes();
        }

        // Dive bomb — teleport above target and slam down
        if (distSq > 25 && isAttackReady("dive_bomb", 100)) {
            useAttack("dive_bomb");
            performDiveBomb(target);
        }

        // Still does melee when close
        if (distSq < 16 && isAttackReady("melee_swipe", 20)) {
            useAttack("melee_swipe");
            playAnimation("attack", AnimationIterator.Type.PLAY_ONCE);
            target.damage(25.0, hitboxEntity);
            target.setVelocity(hitboxEntity.getLocation().getDirection().normalize().multiply(1.0));
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                    Sound.ENTITY_IRON_GOLEM_ATTACK, 1.5f, 0.6f);
        }
    }

    private void phase3Tick(Player target, double distSq) {
        // Shockwave AoE
        if (isAttackReady("shockwave", 80)) {
            useAttack("shockwave");
            performShockwave();
        }

        // Summon Blue Trolls
        if (isAttackReady("summon_trolls", 200)) {
            useAttack("summon_trolls");
            summonBlueTrolls();
        }

        // Lightning + melee remain available
        if (isAttackReady("lightning", 50)) {
            useAttack("lightning");
            performLightningStrikes();
        }

        if (distSq < 16 && isAttackReady("melee_swipe", 18)) {
            useAttack("melee_swipe");
            playAnimation("attack", AnimationIterator.Type.PLAY_ONCE);
            target.damage(28.0, hitboxEntity);
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                    Sound.ENTITY_IRON_GOLEM_ATTACK, 1.5f, 0.5f);
        }
    }

    private void phase4Tick(Player target, double distSq) {
        // Enrage — all attacks faster, damage aura active
        if (isAttackReady("fire_breath", 40)) {
            useAttack("fire_breath");
            performFireBreath();
        }

        if (isAttackReady("lightning", 30)) {
            useAttack("lightning");
            performLightningStrikes();
        }

        if (isAttackReady("shockwave", 50)) {
            useAttack("shockwave");
            performShockwave();
        }

        if (distSq > 25 && isAttackReady("dive_bomb", 60)) {
            useAttack("dive_bomb");
            performDiveBomb(target);
        }

        if (distSq < 16 && isAttackReady("melee_swipe", 12)) {
            useAttack("melee_swipe");
            playAnimation("attack", AnimationIterator.Type.PLAY_ONCE);
            target.damage(35.0, hitboxEntity);
            target.setVelocity(hitboxEntity.getLocation().getDirection().normalize().multiply(1.2));
        }
    }

    // ── Attack Implementations ────────────────────────────────────────

    private void performFireBreath() {
        playAnimation("fire_breath", AnimationIterator.Type.PLAY_ONCE);
        Location loc = hitboxEntity.getLocation();
        Vector dir = loc.getDirection().normalize();
        hitboxEntity.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_SHOOT, 1.5f, 0.6f);

        for (int i = 1; i <= 8; i++) {
            Location particleLoc = loc.clone().add(dir.clone().multiply(i));
            loc.getWorld().spawnParticle(Particle.FLAME, particleLoc, 15, 0.5, 0.5, 0.5, 0.05);
        }

        for (Player p : findNearbyPlayers(10)) {
            Vector toPlayer = p.getLocation().toVector().subtract(loc.toVector()).normalize();
            if (toPlayer.dot(dir) > 0.5) {
                p.damage(18.0, hitboxEntity);
                p.setFireTicks(60);
            }
        }
    }

    private void performLightningStrikes() {
        playAnimation("roar", AnimationIterator.Type.PLAY_ONCE);
        hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2.0f, 0.8f);

        for (Player p : findNearbyPlayers(aggroRadius)) {
            if (random.nextDouble() < 0.5) {
                Location strikePos = p.getLocation();
                p.getWorld().strikeLightningEffect(strikePos);
                p.damage(15.0, hitboxEntity);
                p.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, strikePos, 20, 0.5, 1, 0.5, 0.1);
            }
        }
    }

    private void performDiveBomb(Player target) {
        playAnimation("jump", AnimationIterator.Type.PLAY_ONCE);
        Location above = target.getLocation().clone().add(0, 8, 0);
        moveTo(above);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!alive || hitboxEntity == null) return;
                moveTo(target.getLocation());
                hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                        Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.8f);
                hitboxEntity.getWorld().spawnParticle(Particle.EXPLOSION, hitboxEntity.getLocation(),
                        5, 1, 1, 1, 0);

                for (Player p : findNearbyPlayers(5)) {
                    p.damage(22.0, hitboxEntity);
                    Vector knockback = p.getLocation().toVector()
                            .subtract(hitboxEntity.getLocation().toVector())
                            .normalize().multiply(1.5).setY(0.6);
                    p.setVelocity(knockback);
                }
            }
        }.runTaskLater(plugin, 15L);
    }

    private void performShockwave() {
        playAnimation("stomp", AnimationIterator.Type.PLAY_ONCE);
        Location loc = hitboxEntity.getLocation();
        hitboxEntity.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.5f);

        for (double r = 1; r <= 8; r += 1) {
            final double radius = r;
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 8) {
                        double x = Math.cos(angle) * radius;
                        double z = Math.sin(angle) * radius;
                        loc.getWorld().spawnParticle(Particle.EXPLOSION,
                                loc.clone().add(x, 0.2, z), 1, 0, 0, 0, 0);
                    }
                }
            }.runTaskLater(plugin, (long) (r * 2));
        }

        for (Player p : findNearbyPlayers(8)) {
            p.damage(20.0, hitboxEntity);
            Vector knockback = p.getLocation().toVector()
                    .subtract(loc.toVector()).normalize().multiply(2.0).setY(0.8);
            p.setVelocity(knockback);
        }
    }

    private void summonBlueTrolls() {
        playAnimation("roar", AnimationIterator.Type.PLAY_ONCE);
        hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                Sound.ENTITY_EVOKER_PREPARE_SUMMON, 2.0f, 0.6f);

        for (Player p : findNearbyPlayers(aggroRadius)) {
            p.sendMessage(Component.text("The Warrior summons Blue Trolls to fight!",
                    NamedTextColor.RED).decorate(TextDecoration.ITALIC));
        }

        Location loc = hitboxEntity.getLocation();
        for (int i = 0; i < 4; i++) {
            double angle = (Math.PI * 2 / 4) * i;
            Location spawnLoc = loc.clone().add(Math.cos(angle) * 4, 0, Math.sin(angle) * 4);
            BlueTrollMob troll = new BlueTrollMob(plugin);
            troll.spawn(spawnLoc);
        }
    }

    // ── Phase Transitions ─────────────────────────────────────────────

    @Override
    protected void onPhaseTransition(int oldPhase, int newPhase) {
        super.onPhaseTransition(oldPhase, newPhase);
        Location loc = hitboxEntity.getLocation();

        switch (newPhase) {
            case 1 -> {
                loc.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0f, 0.7f);
                loc.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, loc, 50, 2, 2, 2, 0.2);
                for (Player p : findNearbyPlayers(aggroRadius)) {
                    p.sendMessage(Component.text("The Warrior channels the storm!",
                            NamedTextColor.DARK_AQUA).decorate(TextDecoration.BOLD));
                }
            }
            case 2 -> {
                loc.getWorld().playSound(loc, Sound.ENTITY_WITHER_SPAWN, 2.0f, 0.8f);
                for (Player p : findNearbyPlayers(aggroRadius)) {
                    p.sendMessage(Component.text("The Warrior roars, summoning reinforcements!",
                            NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD));
                }
            }
            case 3 -> {
                enraged = true;
                damageAuraActive = true;
                loc.getWorld().playSound(loc, Sound.ENTITY_WITHER_SPAWN, 2.5f, 0.4f);
                loc.getWorld().spawnParticle(Particle.FLAME, loc, 100, 3, 3, 3, 0.1);
                for (Player p : findNearbyPlayers(aggroRadius)) {
                    p.sendMessage(Component.text("THE WARRIOR IS ENRAGED!",
                            NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD));
                }
                playAnimation("enrage", AnimationIterator.Type.PLAY_ONCE);
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
            loc.getWorld().strikeLightningEffect(loc);
        }

        for (Player p : findNearbyPlayers(150)) {
            p.sendMessage(Component.text("The Warrior has been defeated!",
                    NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
        }

        if (killer != null) {
            killer.giveExp(5000);
        }
    }

    @Override
    protected List<ItemStack> getDrops(Player killer) {
        List<ItemStack> drops = new ArrayList<>();

        // 1 ABYSSAL legendary weapon
        LegendaryWeapon[] abyssalWeapons = LegendaryWeapon.byTier(LegendaryTier.ABYSSAL);
        if (abyssalWeapons.length > 0) {
            LegendaryWeaponManager wm = JGlimsPlugin.getInstance().getLegendaryWeaponManager();
            drops.add(wm.createWeapon(abyssalWeapons[random.nextInt(abyssalWeapons.length)]));
        }

        // MYTHIC armor piece
        LegendaryArmorSet[] armorSets = LegendaryArmorSet.values();
        if (armorSets.length > 0) {
            LegendaryArmorManager am = JGlimsPlugin.getInstance().getLegendaryArmorManager();
            LegendaryArmorSet set = armorSets[random.nextInt(armorSets.length)];
            LegendaryArmorSet.ArmorSlot[] slots = LegendaryArmorSet.ArmorSlot.values();
            drops.add(am.createArmorPiece(set, slots[random.nextInt(slots.length)]));
        }

        // 3 nether stars
        drops.add(new ItemStack(Material.NETHER_STAR, 3));

        // 32 diamonds
        drops.add(new ItemStack(Material.DIAMOND, 32));

        return drops;
    }
}

package com.jglims.plugin.custommobs.mobs;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.custommobs.CustomBossEntity;
import com.jglims.plugin.custommobs.CustomMobType;
import com.jglims.plugin.legendary.LegendaryTier;
import com.jglims.plugin.legendary.LegendaryWeapon;
import com.jglims.plugin.legendary.LegendaryWeaponManager;
import kr.toxicity.model.api.animation.AnimationIterator;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Realistic Dragon Boss - HP 600. For End Rift event.
 * Fire breath cone, tail whip, wing buffet, dive attack, summon endermites.
 * Drops: 2 EPIC legendary, 16 diamonds, dragon egg fragment.
 */
public class RealisticDragonBoss extends CustomBossEntity {

    public RealisticDragonBoss(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.REALISTIC_DRAGON);
        this.aggroRadius = 45.0;
        setPhaseThresholds(0.6, 0.3);
    }

    @Override
    protected BossBar.Color getBossBarColor() {
        return BossBar.Color.PURPLE;
    }

    @Override
    protected void combatTick() {
        Player target = findNearestPlayer(aggroRadius);
        if (target == null) return;

        if (hitboxEntity instanceof Mob mob) {
            mob.setTarget(target);
        }

        double dist = hitboxEntity.getLocation().distanceSquared(target.getLocation());

        // Fire Breath Cone
        if (dist < 144 && isAttackReady("fire_breath", 60)) {
            useAttack("fire_breath");
            playAnimation("attack", AnimationIterator.Type.PLAY_ONCE);
            Vector dir = target.getLocation().toVector()
                    .subtract(hitboxEntity.getLocation().toVector()).normalize();
            for (Player p : findNearbyPlayers(10)) {
                Vector toPlayer = p.getLocation().toVector()
                        .subtract(hitboxEntity.getLocation().toVector()).normalize();
                if (dir.dot(toPlayer) > 0.5) {
                    p.damage(14, hitboxEntity);
                    p.setFireTicks(80);
                }
            }
            // Fire particle cone
            Location start = hitboxEntity.getLocation().add(0, 2, 0);
            for (double d = 1; d < 10; d += 0.5) {
                Location flameLoc = start.clone().add(dir.clone().multiply(d));
                hitboxEntity.getWorld().spawnParticle(Particle.FLAME, flameLoc,
                        5, 0.3 * d / 5, 0.3, 0.3 * d / 5, 0.02);
            }
            hitboxEntity.getWorld().playSound(start, Sound.ENTITY_BLAZE_SHOOT, 2.0f, 0.6f);
        }

        // Tail Whip
        if (dist < 20 && isAttackReady("tail_whip", 35)) {
            useAttack("tail_whip");
            playAnimation("attack", AnimationIterator.Type.PLAY_ONCE);
            for (Player p : findNearbyPlayers(4.5)) {
                p.damage(12, hitboxEntity);
                Vector knock = p.getLocation().toVector()
                        .subtract(hitboxEntity.getLocation().toVector()).normalize().multiply(1.3);
                knock.setY(0.6);
                p.setVelocity(knock);
            }
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                    Sound.ENTITY_ENDER_DRAGON_FLAP, 1.5f, 0.5f);
        }

        // Wing Buffet - pushes players away
        if (dist < 64 && isAttackReady("wing_buffet", 80)) {
            useAttack("wing_buffet");
            playAnimation("attack", AnimationIterator.Type.PLAY_ONCE);
            for (Player p : findNearbyPlayers(8)) {
                Vector push = p.getLocation().toVector()
                        .subtract(hitboxEntity.getLocation().toVector()).normalize().multiply(2.0);
                push.setY(1.0);
                p.setVelocity(push);
                p.damage(8, hitboxEntity);
            }
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                    Sound.ENTITY_ENDER_DRAGON_FLAP, 2.0f, 0.8f);
        }

        // Dive Attack
        if (dist > 100 && isAttackReady("dive", 100)) {
            useAttack("dive");
            Location above = target.getLocation().add(0, 12, 0);
            hitboxEntity.teleport(above);
            hitboxEntity.setVelocity(new Vector(0, -3.5, 0));

            new org.bukkit.scheduler.BukkitRunnable() {
                @Override
                public void run() {
                    if (hitboxEntity == null || !alive) return;
                    for (Player p : findNearbyPlayers(5)) {
                        p.damage(20, hitboxEntity);
                    }
                    hitboxEntity.getWorld().spawnParticle(Particle.EXPLOSION,
                            hitboxEntity.getLocation(), 3, 2, 1, 2, 0);
                    hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                            Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.7f);
                }
            }.runTaskLater(plugin, 12L);
        }

        // Summon Endermites (phase 2+)
        if (currentPhase >= 1 && isAttackReady("summon_endermites", 120)) {
            useAttack("summon_endermites");
            Location loc = hitboxEntity.getLocation();
            Random rand = new Random();
            for (int i = 0; i < 3 + currentPhase; i++) {
                Location spawnLoc = loc.clone().add(
                        rand.nextInt(6) - 3, 0, rand.nextInt(6) - 3);
                loc.getWorld().spawnEntity(spawnLoc, EntityType.ENDERMITE);
            }
            hitboxEntity.getWorld().playSound(loc, Sound.ENTITY_ENDERMITE_AMBIENT, 2.0f, 0.5f);
            for (Player p : findNearbyPlayers(aggroRadius)) {
                p.sendMessage(Component.text("Endermites swarm from the rift!", NamedTextColor.DARK_PURPLE));
            }
        }
    }

    @Override
    protected void onPhaseTransition(int oldPhase, int newPhase) {
        super.onPhaseTransition(oldPhase, newPhase);
        playAnimation("roar", AnimationIterator.Type.PLAY_ONCE);
        if (hitboxEntity != null) {
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                    Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0f, 0.7f);
        }
    }

    @Override
    protected void onDeath(Player killer) {
        super.onDeath(killer);
        if (killer != null) {
            killer.giveExp(400);
        }
    }

    @Override
    protected List<ItemStack> getDrops(Player killer) {
        List<ItemStack> drops = new ArrayList<>();
        drops.add(new ItemStack(Material.DIAMOND, 16));

        // Dragon egg fragment (dragon egg)
        drops.add(new ItemStack(Material.DRAGON_EGG, 1));

        // 2 EPIC legendaries
        LegendaryWeapon[] weapons = LegendaryWeapon.byTier(LegendaryTier.EPIC);
        if (weapons.length > 0) {
            LegendaryWeaponManager wm = JGlimsPlugin.getInstance().getLegendaryWeaponManager();
            Random rand = new Random();
            for (int i = 0; i < 2; i++) {
                drops.add(wm.createWeapon(weapons[rand.nextInt(weapons.length)]));
            }
        }
        return drops;
    }
}

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
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Wendigo Boss - HP 500. TERRIFYING.
 * On spawn: Darkness to all within 50 blocks. Moves silently.
 * Fear scream (Darkness + Slowness 5s), claw slash (20 dmg),
 * vanish (invisible 5s then backstab 30 dmg).
 * P2 (below 30%): permanently invisible, only tracked by occasional particles.
 * Drops: 1 MYTHIC, phantom membranes x8.
 */
public class WendigoBoss extends CustomWorldBoss {

    private boolean phase2 = false;
    private boolean vanished = false;

    public WendigoBoss(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.WENDIGO);
        this.aggroRadius = 40.0;
        this.announceRadius = 50.0;
        setPhaseThresholds(0.5, 0.3);
    }

    @Override
    protected BossBar.Color getBossBarColor() {
        return BossBar.Color.WHITE;
    }

    @Override
    protected void onSpawn() {
        super.onSpawn();
        // Darkness to all within 50 blocks on spawn
        for (Player p : findNearbyPlayers(50)) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 200, 0));
            p.playSound(p.getLocation(), Sound.AMBIENT_CAVE, 2.0f, 0.3f);
        }
        // Silent movement
        if (hitboxEntity != null) {
            hitboxEntity.setSilent(true);
        }
    }

    @Override
    protected void combatTick() {
        Player target = findNearestPlayer(aggroRadius);
        if (target == null) return;

        if (hitboxEntity instanceof Mob mob) {
            mob.setTarget(target);
        }

        double dist = hitboxEntity.getLocation().distanceSquared(target.getLocation());

        // Phase 2: permanently invisible with occasional particle hints
        if (phase2) {
            if (hitboxEntity != null) {
                hitboxEntity.setInvisible(true);
                // Occasional particle hint every 3 seconds
                if (combatTicks % 30 == 0) {
                    hitboxEntity.getWorld().spawnParticle(Particle.SMOKE,
                            hitboxEntity.getLocation().add(0, 1, 0), 3, 0.5, 0.5, 0.5, 0.01);
                }
            }
        }

        // Fear Scream - Darkness + Slowness
        if (dist < 225 && isAttackReady("fear_scream", 100)) {
            useAttack("fear_scream");
            playAnimation("roar", AnimationIterator.Type.PLAY_ONCE);
            for (Player p : findNearbyPlayers(15)) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 100, 0));
                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 1));
                p.playSound(p.getLocation(), Sound.ENTITY_WARDEN_ROAR, 2.0f, 1.5f);
            }
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                    Sound.ENTITY_WARDEN_ROAR, 3.0f, 1.5f);
        }

        // Claw Slash - 20 dmg
        if (dist < 9 && isAttackReady("claw_slash", 30)) {
            useAttack("claw_slash");
            playAnimation("attack", AnimationIterator.Type.PLAY_ONCE);
            target.damage(20, hitboxEntity);
            target.setVelocity(hitboxEntity.getLocation().getDirection().normalize().multiply(0.8));
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                    Sound.ENTITY_WARDEN_ATTACK_IMPACT, 1.5f, 1.2f);
            hitboxEntity.getWorld().spawnParticle(Particle.CRIT,
                    target.getLocation().add(0, 1, 0), 15, 0.5, 0.5, 0.5, 0.2);
        }

        // Vanish + Backstab
        if (!vanished && isAttackReady("vanish", 120)) {
            useAttack("vanish");
            vanished = true;
            if (hitboxEntity != null) {
                hitboxEntity.setInvisible(true);
            }
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                    Sound.ENTITY_ENDERMAN_TELEPORT, 1.5f, 0.5f);
            hitboxEntity.getWorld().spawnParticle(Particle.SMOKE,
                    hitboxEntity.getLocation().add(0, 1, 0), 30, 1, 1, 1, 0.05);

            // Backstab after 5 seconds
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (hitboxEntity == null || !alive) return;
                    vanished = false;
                    if (!phase2 && hitboxEntity != null) {
                        hitboxEntity.setInvisible(false);
                    }

                    Player backstabTarget = findNearestPlayer(aggroRadius);
                    if (backstabTarget == null) return;

                    // Teleport behind target
                    Location behind = backstabTarget.getLocation()
                            .add(backstabTarget.getLocation().getDirection().multiply(-2));
                    behind.setY(backstabTarget.getLocation().getY());
                    hitboxEntity.teleport(behind);

                    playAnimation("attack", AnimationIterator.Type.PLAY_ONCE);
                    backstabTarget.damage(30, hitboxEntity);
                    backstabTarget.addPotionEffect(
                            new PotionEffect(PotionEffectType.DARKNESS, 60, 0));
                    hitboxEntity.getWorld().playSound(behind,
                            Sound.ENTITY_WARDEN_ATTACK_IMPACT, 2.0f, 0.5f);
                    hitboxEntity.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR,
                            backstabTarget.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0);

                    backstabTarget.sendMessage(Component.text("The Wendigo strikes from behind!",
                            NamedTextColor.DARK_RED).decorate(TextDecoration.ITALIC));
                }
            }.runTaskLater(plugin, 100L);
        }
    }

    @Override
    protected void onPhaseTransition(int oldPhase, int newPhase) {
        super.onPhaseTransition(oldPhase, newPhase);

        // Phase 2 at 30% - permanently invisible
        if (newPhase >= 2 && !phase2) {
            phase2 = true;
            if (hitboxEntity != null) {
                hitboxEntity.setInvisible(true);
            }
            for (Player p : findNearbyPlayers(aggroRadius * 1.5)) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 200, 0));
                p.sendMessage(Component.text("The Wendigo vanishes into the shadows...",
                        NamedTextColor.DARK_GRAY).decorate(TextDecoration.ITALIC));
                p.playSound(p.getLocation(), Sound.AMBIENT_CAVE, 3.0f, 0.1f);
            }
        }
    }

    @Override
    protected void announceSpawn() {
        List<Player> nearby = findNearbyPlayers(announceRadius);
        for (Player p : nearby) {
            p.sendMessage(Component.text("Something stalks the darkness...",
                    NamedTextColor.DARK_GRAY).decorate(TextDecoration.ITALIC));
            p.playSound(p.getLocation(), Sound.AMBIENT_CAVE, 2.0f, 0.3f);
        }
    }

    @Override
    protected void onDeath(Player killer) {
        super.onDeath(killer);
        if (hitboxEntity != null) {
            Location loc = hitboxEntity.getLocation();
            loc.getWorld().playSound(loc, Sound.ENTITY_WARDEN_DEATH, 2.0f, 1.2f);
            loc.getWorld().spawnParticle(Particle.SMOKE, loc.add(0, 1, 0), 50, 2, 2, 2, 0.05);
        }
        // Remove darkness from all nearby players
        for (Player p : findNearbyPlayers(50)) {
            p.removePotionEffect(PotionEffectType.DARKNESS);
        }
        if (killer != null) {
            killer.giveExp(500);
        }
    }

    @Override
    protected List<ItemStack> getDrops(Player killer) {
        List<ItemStack> drops = new ArrayList<>();
        drops.add(new ItemStack(Material.PHANTOM_MEMBRANE, 8));

        LegendaryWeapon[] weapons = LegendaryWeapon.byTier(LegendaryTier.MYTHIC);
        if (weapons.length > 0) {
            LegendaryWeaponManager wm = JGlimsPlugin.getInstance().getLegendaryWeaponManager();
            drops.add(wm.createWeapon(weapons[new Random().nextInt(weapons.length)]));
        }
        return drops;
    }
}

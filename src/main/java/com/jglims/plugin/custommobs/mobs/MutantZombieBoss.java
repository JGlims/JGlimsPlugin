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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Mutant Zombie Boss - HP 300. Heavy slam, zombie roar (summons 3 zombies),
 * ground pound, grab+throw (pick up player, throw 8 blocks, 15 dmg).
 * Context-dependent drops.
 */
public class MutantZombieBoss extends CustomBossEntity {

    public MutantZombieBoss(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.MUTANT_ZOMBIE);
        this.aggroRadius = 30.0;
        setPhaseThresholds(0.5, 0.25);
    }

    @Override
    protected BossBar.Color getBossBarColor() {
        return BossBar.Color.GREEN;
    }

    @Override
    protected void combatTick() {
        Player target = findNearestPlayer(aggroRadius);
        if (target == null) return;

        if (hitboxEntity instanceof Mob mob) {
            mob.setTarget(target);
        }

        double dist = hitboxEntity.getLocation().distanceSquared(target.getLocation());

        // Heavy Slam
        if (dist < 12 && isAttackReady("heavy_slam", 40)) {
            useAttack("heavy_slam");
            playAnimation("attack", AnimationIterator.Type.PLAY_ONCE);
            target.damage(mobType.getBaseDamage(), hitboxEntity);
            target.setVelocity(new Vector(0, 0.5, 0));
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                    Sound.ENTITY_IRON_GOLEM_ATTACK, 2.0f, 0.5f);
            hitboxEntity.getWorld().spawnParticle(Particle.BLOCK,
                    hitboxEntity.getLocation(), 20, 1.5, 0.5, 1.5, 0,
                    Material.STONE.createBlockData());
        }

        // Zombie Roar - summon 3 zombies
        if (isAttackReady("zombie_roar", 100)) {
            useAttack("zombie_roar");
            playAnimation("roar", AnimationIterator.Type.PLAY_ONCE);
            Location loc = hitboxEntity.getLocation();
            Random rand = new Random();
            for (int i = 0; i < 3; i++) {
                Location spawnLoc = loc.clone().add(
                        rand.nextInt(6) - 3, 0, rand.nextInt(6) - 3);
                Zombie zombie = (Zombie) loc.getWorld().spawnEntity(spawnLoc, EntityType.ZOMBIE);
                zombie.setTarget(target);
            }
            loc.getWorld().playSound(loc, Sound.ENTITY_ZOMBIE_AMBIENT, 2.0f, 0.3f);
        }

        // Ground Pound - AoE
        if (dist < 36 && isAttackReady("ground_pound", 60)) {
            useAttack("ground_pound");
            playAnimation("attack", AnimationIterator.Type.PLAY_ONCE);
            Location loc = hitboxEntity.getLocation();
            for (Player p : findNearbyPlayers(6)) {
                p.damage(12, hitboxEntity);
                Vector knock = p.getLocation().toVector()
                        .subtract(loc.toVector()).normalize().multiply(1.2);
                knock.setY(0.7);
                p.setVelocity(knock);
            }
            loc.getWorld().playSound(loc, Sound.ENTITY_RAVAGER_ROAR, 2.0f, 0.4f);
            loc.getWorld().spawnParticle(Particle.BLOCK, loc,
                    30, 3, 0.5, 3, 0, Material.STONE.createBlockData());
        }

        // Grab + Throw
        if (dist < 9 && isAttackReady("grab_throw", 80)) {
            useAttack("grab_throw");
            playAnimation("attack", AnimationIterator.Type.PLAY_ONCE);

            // "Grab" - freeze player briefly
            target.setVelocity(new Vector(0, 0, 0));
            target.sendMessage(Component.text("The Mutant Zombie grabs you!", NamedTextColor.RED));

            // Throw after short delay
            final Player throwTarget = target;
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (hitboxEntity == null || !alive) return;
                    Vector throwDir = throwTarget.getLocation().toVector()
                            .subtract(hitboxEntity.getLocation().toVector()).normalize().multiply(2.5);
                    throwDir.setY(1.0);
                    throwTarget.setVelocity(throwDir);
                    throwTarget.damage(15, hitboxEntity);
                    hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                            Sound.ENTITY_IRON_GOLEM_ATTACK, 1.5f, 0.4f);
                }
            }.runTaskLater(plugin, 15L);
        }
    }

    @Override
    protected void onDeath(Player killer) {
        super.onDeath(killer);
        if (hitboxEntity != null) {
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                    Sound.ENTITY_ZOMBIE_DEATH, 2.0f, 0.3f);
        }
        if (killer != null) {
            killer.giveExp(200);
        }
    }

    @Override
    protected List<ItemStack> getDrops(Player killer) {
        List<ItemStack> drops = new ArrayList<>();
        drops.add(new ItemStack(Material.ROTTEN_FLESH, 8 + (int) (Math.random() * 8)));
        drops.add(new ItemStack(Material.IRON_INGOT, 2 + (int) (Math.random() * 3)));

        // Context-dependent drops: EPIC legendary 50% chance
        if (Math.random() < 0.5) {
            LegendaryWeapon[] weapons = LegendaryWeapon.byTier(LegendaryTier.EPIC);
            if (weapons.length > 0) {
                LegendaryWeaponManager wm = JGlimsPlugin.getInstance().getLegendaryWeaponManager();
                drops.add(wm.createWeapon(weapons[new Random().nextInt(weapons.length)]));
            }
        }
        return drops;
    }
}

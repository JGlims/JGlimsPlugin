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
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Wither Storm Boss - HP 500. Stationary.
 * Destroys blocks in 5-block radius every 5s. Wither skull barrage (5 skulls),
 * tractor beam (pulls entities 15 blocks), wither aura (Wither I 10 blocks).
 * Drops: 2 nether stars, 1 EPIC legendary.
 */
public class WitherStormBoss extends CustomBossEntity {

    private long lastDestroyTick = 0;

    public WitherStormBoss(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.WITHER_STORM);
        this.aggroRadius = 40.0;
        setPhaseThresholds(0.5, 0.25);
    }

    @Override
    protected BossBar.Color getBossBarColor() {
        return BossBar.Color.PURPLE;
    }

    @Override
    protected void onSpawn() {
        super.onSpawn();
        // Stationary - disable AI
        if (hitboxEntity instanceof Mob mob) {
            mob.setAI(false);
        }
    }

    @Override
    protected void combatTick() {
        // Block destruction every 5 seconds
        if (combatTicks - lastDestroyTick >= 50) {
            lastDestroyTick = combatTicks;
            destroyNearbyBlocks();
        }

        // Wither Aura - passive Wither I to all within 10 blocks
        for (Player p : findNearbyPlayers(10)) {
            if (!p.hasPotionEffect(PotionEffectType.WITHER)) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 60, 0));
            }
        }

        Player target = findNearestPlayer(aggroRadius);
        if (target == null) return;

        // Wither Skull Barrage - 5 skulls
        if (isAttackReady("skull_barrage", 60)) {
            useAttack("skull_barrage");
            playAnimation("attack", AnimationIterator.Type.PLAY_ONCE);
            Location origin = hitboxEntity.getLocation().add(0, 3, 0);
            Random rand = new Random();

            for (int i = 0; i < 5; i++) {
                Vector dir = target.getLocation().add(0, 1, 0).toVector()
                        .subtract(origin.toVector()).normalize();
                dir.add(new Vector(
                        (rand.nextDouble() - 0.5) * 0.3,
                        (rand.nextDouble() - 0.5) * 0.2,
                        (rand.nextDouble() - 0.5) * 0.3));

                WitherSkull skull = hitboxEntity.getWorld().spawn(
                        origin.clone(), WitherSkull.class);
                skull.setDirection(dir);
                skull.setShooter(hitboxEntity);
                skull.setYield(0); // No block damage from skulls
            }
            origin.getWorld().playSound(origin, Sound.ENTITY_WITHER_SHOOT, 2.0f, 1.0f);
        }

        // Tractor Beam - pull entities within 15 blocks
        if (isAttackReady("tractor_beam", 80)) {
            useAttack("tractor_beam");
            playAnimation("attack", AnimationIterator.Type.PLAY_ONCE);
            Location bossLoc = hitboxEntity.getLocation();
            for (Player p : findNearbyPlayers(15)) {
                Vector pull = bossLoc.toVector().subtract(p.getLocation().toVector()).normalize().multiply(0.8);
                pull.setY(0.3);
                p.setVelocity(pull);
                p.damage(5, hitboxEntity);
            }
            bossLoc.getWorld().playSound(bossLoc, Sound.ENTITY_WITHER_AMBIENT, 2.0f, 0.3f);
            // Visual beam particles
            for (Player p : findNearbyPlayers(15)) {
                Vector dir = bossLoc.toVector().subtract(p.getLocation().toVector()).normalize();
                for (double d = 0; d < p.getLocation().distance(bossLoc); d += 0.5) {
                    Location particleLoc = p.getLocation().add(dir.clone().multiply(d)).add(0, 1, 0);
                    bossLoc.getWorld().spawnParticle(Particle.WITCH, particleLoc,
                            2, 0.1, 0.1, 0.1, 0);
                }
            }
        }
    }

    private void destroyNearbyBlocks() {
        if (hitboxEntity == null) return;
        Location center = hitboxEntity.getLocation();
        int radius = 5;
        Random rand = new Random();

        // Destroy random blocks within radius (not bedrock/obsidian)
        for (int i = 0; i < 15; i++) {
            int dx = rand.nextInt(radius * 2 + 1) - radius;
            int dy = rand.nextInt(radius * 2 + 1) - radius;
            int dz = rand.nextInt(radius * 2 + 1) - radius;
            Block block = center.getWorld().getBlockAt(
                    center.getBlockX() + dx, center.getBlockY() + dy, center.getBlockZ() + dz);
            if (block.getType() != Material.AIR && block.getType() != Material.BEDROCK
                    && block.getType() != Material.OBSIDIAN && block.getType() != Material.REINFORCED_DEEPSLATE) {
                center.getWorld().spawnParticle(Particle.BLOCK,
                        block.getLocation().add(0.5, 0.5, 0.5), 5, 0.3, 0.3, 0.3, 0,
                        block.getBlockData());
                block.setType(Material.AIR);
            }
        }
        center.getWorld().playSound(center, Sound.ENTITY_WITHER_BREAK_BLOCK, 1.5f, 0.8f);
    }

    @Override
    protected void onPhaseTransition(int oldPhase, int newPhase) {
        super.onPhaseTransition(oldPhase, newPhase);
        if (hitboxEntity != null) {
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                    Sound.ENTITY_WITHER_SPAWN, 2.0f, 0.5f);
        }
    }

    @Override
    protected void onDeath(Player killer) {
        super.onDeath(killer);
        if (hitboxEntity != null) {
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                    Sound.ENTITY_WITHER_DEATH, 2.0f, 1.0f);
        }
        if (killer != null) {
            killer.giveExp(350);
        }
    }

    @Override
    protected List<ItemStack> getDrops(Player killer) {
        List<ItemStack> drops = new ArrayList<>();
        drops.add(new ItemStack(Material.NETHER_STAR, 2));

        LegendaryWeapon[] weapons = LegendaryWeapon.byTier(LegendaryTier.EPIC);
        if (weapons.length > 0) {
            LegendaryWeaponManager wm = JGlimsPlugin.getInstance().getLegendaryWeaponManager();
            drops.add(wm.createWeapon(weapons[new Random().nextInt(weapons.length)]));
        }
        return drops;
    }
}

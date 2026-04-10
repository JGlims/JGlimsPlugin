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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Mushroom Monstrosity Boss - HP 600. 2 phases.
 * P1: melee slams, spore cloud (Poison II AoE), fungal growth.
 * P2 (below 40%): splits into 3 smaller mobs (200 HP each, if one survives
 * 30s it regens the others). Drops: 1 MYTHIC, mycelium, mushroom blocks x64.
 */
public class MushroomMonstrosityBoss extends CustomBossEntity {

    private boolean splitPhase = false;
    private final List<LivingEntity> splitMobs = new ArrayList<>();
    private long splitTick = 0;

    public MushroomMonstrosityBoss(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.MUSHROOM_MONSTROSITY);
        this.aggroRadius = 35.0;
        setPhaseThresholds(0.6, 0.4);
    }

    @Override
    protected BossBar.Color getBossBarColor() {
        return BossBar.Color.GREEN;
    }

    @Override
    protected void combatTick() {
        if (splitPhase) {
            splitPhaseTick();
            return;
        }

        Player target = findNearestPlayer(aggroRadius);
        if (target == null) return;

        if (hitboxEntity instanceof Mob mob) {
            mob.setTarget(target);
        }

        double dist = hitboxEntity.getLocation().distanceSquared(target.getLocation());

        // Melee Slam
        if (dist < 12 && isAttackReady("slam", 30)) {
            useAttack("slam");
            playAnimation("attack", AnimationIterator.Type.PLAY_ONCE);
            target.damage(mobType.getBaseDamage(), hitboxEntity);
            target.setVelocity(new Vector(0, 0.6, 0));
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                    Sound.ENTITY_IRON_GOLEM_ATTACK, 2.0f, 0.4f);
        }

        // Spore Cloud - Poison II AoE
        if (dist < 100 && isAttackReady("spore_cloud", 80)) {
            useAttack("spore_cloud");
            playAnimation("attack", AnimationIterator.Type.PLAY_ONCE);
            Location loc = hitboxEntity.getLocation();
            for (Player p : findNearbyPlayers(8)) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 1));
                p.damage(4, hitboxEntity);
            }
            loc.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, loc.add(0, 2, 0),
                    50, 4, 2, 4, 0);
            loc.getWorld().spawnParticle(Particle.DUST, loc,
                    30, 4, 2, 4, 0, new Particle.DustOptions(Color.GREEN, 2.0f));
            loc.getWorld().playSound(loc, Sound.BLOCK_WET_GRASS_BREAK, 2.0f, 0.3f);
        }

        // Fungal Growth - place mushrooms around
        if (isAttackReady("fungal_growth", 100)) {
            useAttack("fungal_growth");
            Location loc = hitboxEntity.getLocation();
            Random rand = new Random();
            for (int i = 0; i < 5; i++) {
                Location mushroomLoc = loc.clone().add(
                        rand.nextInt(8) - 4, 0, rand.nextInt(8) - 4);
                org.bukkit.block.Block block = mushroomLoc.getWorld()
                        .getHighestBlockAt(mushroomLoc).getRelative(0, 1, 0);
                if (block.getType() == Material.AIR) {
                    block.setType(rand.nextBoolean() ? Material.RED_MUSHROOM : Material.BROWN_MUSHROOM);
                }
            }
        }
    }

    @Override
    protected void onPhaseTransition(int oldPhase, int newPhase) {
        super.onPhaseTransition(oldPhase, newPhase);

        // Split into 3 smaller mobs at phase 2 (40% HP)
        if (newPhase >= 2 && !splitPhase) {
            splitPhase = true;
            splitTick = combatTicks;
            spawnSplitMobs();
            // Hide main entity
            if (hitboxEntity != null) {
                hitboxEntity.setInvisible(true);
                hitboxEntity.setAI(false);
                hitboxEntity.setInvulnerable(true);
            }
            for (Player p : findNearbyPlayers(aggroRadius)) {
                p.sendMessage(Component.text("The Monstrosity splits into three!", NamedTextColor.GREEN));
            }
        }
    }

    private void spawnSplitMobs() {
        if (hitboxEntity == null) return;
        Location center = hitboxEntity.getLocation();
        Random rand = new Random();

        for (int i = 0; i < 3; i++) {
            Location spawnLoc = center.clone().add(
                    rand.nextInt(6) - 3, 0, rand.nextInt(6) - 3);
            Zombie mini = (Zombie) center.getWorld().spawnEntity(spawnLoc, EntityType.ZOMBIE);
            mini.setAdult();
            Objects.requireNonNull(mini.getAttribute(
                    org.bukkit.attribute.Attribute.MAX_HEALTH)).setBaseValue(200);
            mini.setHealth(200);
            mini.customName(Component.text("Mushroom Fragment", NamedTextColor.GREEN));
            mini.setCustomNameVisible(true);
            if (mini.getEquipment() != null) {
                mini.getEquipment().setHelmet(new ItemStack(Material.RED_MUSHROOM_BLOCK));
            }
            splitMobs.add(mini);
        }
    }

    private void splitPhaseTick() {
        // Remove dead mobs from list
        splitMobs.removeIf(mob -> mob.isDead() || !mob.isValid());

        if (splitMobs.isEmpty()) {
            // All split mobs killed - boss dies
            die(getTopDamageContributor());
            return;
        }

        // If any survives 30 seconds, regen the dead ones
        if (combatTicks - splitTick >= 600 && splitMobs.size() < 3) { // 30 seconds
            splitTick = combatTicks;
            int toRespawn = 3 - splitMobs.size();
            Location center = splitMobs.get(0).getLocation();
            Random rand = new Random();

            for (int i = 0; i < toRespawn; i++) {
                Location spawnLoc = center.clone().add(
                        rand.nextInt(4) - 2, 0, rand.nextInt(4) - 2);
                Zombie mini = (Zombie) center.getWorld().spawnEntity(spawnLoc, EntityType.ZOMBIE);
                mini.setAdult();
                Objects.requireNonNull(mini.getAttribute(
                        org.bukkit.attribute.Attribute.MAX_HEALTH)).setBaseValue(200);
                mini.setHealth(200);
                mini.customName(Component.text("Mushroom Fragment", NamedTextColor.GREEN));
                mini.setCustomNameVisible(true);
                if (mini.getEquipment() != null) {
                    mini.getEquipment().setHelmet(new ItemStack(Material.RED_MUSHROOM_BLOCK));
                }
                splitMobs.add(mini);
            }

            for (Player p : findNearbyPlayers(aggroRadius)) {
                p.sendMessage(Component.text("The fragments regenerate!", NamedTextColor.DARK_GREEN));
            }
        }

        // Update boss bar based on total split mob HP
        if (bossBar != null) {
            double totalHp = splitMobs.stream().mapToDouble(LivingEntity::getHealth).sum();
            double maxHp = 600.0; // 3 * 200
            bossBar.progress(Math.max(0f, Math.min(1f, (float) (totalHp / maxHp))));
        }
    }

    @Override
    protected void onDeath(Player killer) {
        // Clean up split mobs
        for (LivingEntity mob : splitMobs) {
            if (mob.isValid()) {
                mob.remove();
            }
        }
        splitMobs.clear();
        super.onDeath(killer);
        if (killer != null) {
            killer.giveExp(500);
        }
    }

    @Override
    protected List<ItemStack> getDrops(Player killer) {
        List<ItemStack> drops = new ArrayList<>();
        drops.add(new ItemStack(Material.MYCELIUM, 16));
        drops.add(new ItemStack(Material.RED_MUSHROOM_BLOCK, 32));
        drops.add(new ItemStack(Material.BROWN_MUSHROOM_BLOCK, 32));

        // 1 MYTHIC legendary
        LegendaryWeapon[] weapons = LegendaryWeapon.byTier(LegendaryTier.MYTHIC);
        if (weapons.length > 0) {
            LegendaryWeaponManager wm = JGlimsPlugin.getInstance().getLegendaryWeaponManager();
            drops.add(wm.createWeapon(weapons[new Random().nextInt(weapons.length)]));
        }
        return drops;
    }
}

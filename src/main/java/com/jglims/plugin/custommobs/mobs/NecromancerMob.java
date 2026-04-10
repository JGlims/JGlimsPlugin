package com.jglims.plugin.custommobs.mobs;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.custommobs.CustomMobEntity;
import com.jglims.plugin.custommobs.CustomMobType;
import kr.toxicity.model.api.animation.AnimationIterator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Necromancer - Dark Forest caster mob.
 * HP 150. Soul bolt (homing ranged), raise dead, dark shield (50% DR), life drain.
 * Drops dark essence (ender pearl renamed), 5% chance RARE legendary.
 */
public class NecromancerMob extends CustomMobEntity {

    private long lastSoulBoltTick = 0;
    private long lastRaiseDeadTick = 0;
    private long lastDarkShieldTick = 0;
    private long lastLifeDrainTick = 0;
    private boolean darkShieldActive = false;
    private int summonCount = 0;
    private static final int MAX_SUMMONS = 6;

    public NecromancerMob(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.NECROMANCER);
    }

    @Override
    protected void onSpawn() {
        playAnimation("idle", AnimationIterator.Type.LOOP);
    }

    @Override
    protected void onTick() {
        if (hitboxEntity == null || !alive) return;

        Player target = findNearestPlayer(24);
        if (target == null) {
            state = MobState.IDLE;
            return;
        }

        state = MobState.ATTACKING;
        double dist = hitboxEntity.getLocation().distanceSquared(target.getLocation());
        long ticks = hitboxEntity.getTicksLived();

        // Keep distance - back away if too close
        if (dist < 16 && hitboxEntity instanceof org.bukkit.entity.Mob mob) {
            Vector away = hitboxEntity.getLocation().toVector().subtract(target.getLocation().toVector()).normalize();
            hitboxEntity.setVelocity(away.multiply(0.5));
        } else if (hitboxEntity instanceof org.bukkit.entity.Mob mob) {
            mob.setTarget(target);
        }

        // Dark Shield - 50% DR for 5 seconds, 15s cooldown
        if (!darkShieldActive && ticks - lastDarkShieldTick >= 300 && getHealth() < mobType.getMaxHealth() * 0.6) {
            lastDarkShieldTick = ticks;
            darkShieldActive = true;
            playAnimation("shield", AnimationIterator.Type.PLAY_ONCE);
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_EVOKER_PREPARE_SUMMON, 1.0f, 0.8f);
            hitboxEntity.getWorld().spawnParticle(Particle.WITCH, hitboxEntity.getLocation().add(0, 1, 0), 20, 0.5, 1, 0.5, 0.05);
            new BukkitRunnable() {
                @Override
                public void run() {
                    darkShieldActive = false;
                }
            }.runTaskLater(plugin, 100L);
        }

        // Raise Dead - summon 2-3 zombies, 10s cooldown
        if (summonCount < MAX_SUMMONS && ticks - lastRaiseDeadTick >= 200) {
            lastRaiseDeadTick = ticks;
            playAnimation("summon", AnimationIterator.Type.PLAY_ONCE);
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_EVOKER_CAST_SPELL, 1.0f, 0.6f);
            int count = 2 + (int) (Math.random() * 2);
            for (int i = 0; i < count; i++) {
                Location spawnLoc = hitboxEntity.getLocation().add(
                        (Math.random() - 0.5) * 4, 0, (Math.random() - 0.5) * 4);
                Zombie zombie = (Zombie) hitboxEntity.getWorld().spawnEntity(spawnLoc, EntityType.ZOMBIE);
                zombie.customName(Component.text("Raised Dead", NamedTextColor.DARK_GRAY));
                zombie.setTarget(target);
                summonCount++;
            }
        }

        // Soul Bolt - homing projectile, 3s cooldown
        if (dist > 9 && dist < 900 && ticks - lastSoulBoltTick >= 60) {
            lastSoulBoltTick = ticks;
            playAnimation("cast", AnimationIterator.Type.PLAY_ONCE);
            fireSoulBolt(target);
        }

        // Life Drain - heals self, 8s cooldown
        if (dist < 100 && ticks - lastLifeDrainTick >= 160 && getHealth() < mobType.getMaxHealth() * 0.7) {
            lastLifeDrainTick = ticks;
            playAnimation("drain", AnimationIterator.Type.PLAY_ONCE);
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_WITCH_DRINK, 1.0f, 0.5f);
            target.damage(4.0, hitboxEntity);
            double heal = Math.min(4.0, mobType.getMaxHealth() - getHealth());
            hitboxEntity.setHealth(getHealth() + heal);
            // Beam particles
            drawBeam(hitboxEntity.getLocation().add(0, 1, 0), target.getLocation().add(0, 1, 0));
        }
    }

    private void fireSoulBolt(Player target) {
        hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_VEX_CHARGE, 1.0f, 0.6f);
        Location start = hitboxEntity.getLocation().add(0, 1.5, 0);

        new BukkitRunnable() {
            Location pos = start.clone();
            int ticks = 0;

            @Override
            public void run() {
                if (ticks++ > 40 || !alive || target == null || !target.isOnline()) {
                    cancel();
                    return;
                }
                Vector dir = target.getLocation().add(0, 1, 0).toVector().subtract(pos.toVector()).normalize();
                pos.add(dir.multiply(0.8));
                pos.getWorld().spawnParticle(Particle.SOUL, pos, 3, 0.1, 0.1, 0.1, 0.01);

                if (pos.distanceSquared(target.getLocation().add(0, 1, 0)) < 2) {
                    target.damage(6.0, hitboxEntity);
                    target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 0, false, false));
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void drawBeam(Location from, Location to) {
        Vector dir = to.toVector().subtract(from.toVector());
        double length = dir.length();
        dir.normalize();
        for (double d = 0; d < length; d += 0.5) {
            Location point = from.clone().add(dir.clone().multiply(d));
            from.getWorld().spawnParticle(Particle.DUST, point, 1,
                    new Particle.DustOptions(Color.fromRGB(80, 0, 80), 1.0f));
        }
    }

    @Override
    protected void onDamage(double amount, Player source) {
        if (darkShieldActive && hitboxEntity != null) {
            // Restore half the damage
            double restore = amount * 0.5;
            hitboxEntity.setHealth(Math.min(getHealth() + restore, mobType.getMaxHealth()));
            hitboxEntity.getWorld().spawnParticle(Particle.WITCH, hitboxEntity.getLocation().add(0, 1, 0), 5);
        }
    }

    @Override
    protected void onDeath(Player killer) {
        if (hitboxEntity != null) {
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_EVOKER_DEATH, 1.0f, 0.6f);
            hitboxEntity.getWorld().spawnParticle(Particle.SOUL, hitboxEntity.getLocation().add(0, 1, 0), 30, 1, 1, 1, 0.05);
        }
        if (killer != null) {
            killer.giveExp(120);
        }
    }

    @Override
    protected List<ItemStack> getDrops(Player killer) {
        List<ItemStack> drops = new ArrayList<>();
        // Dark Essence (renamed ender pearl)
        ItemStack darkEssence = new ItemStack(Material.ENDER_PEARL, 1 + (int) (Math.random() * 2));
        ItemMeta meta = darkEssence.getItemMeta();
        meta.displayName(Component.text("Dark Essence", NamedTextColor.DARK_PURPLE));
        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "custom_item"), PersistentDataType.STRING, "dark_essence");
        darkEssence.setItemMeta(meta);
        drops.add(darkEssence);

        // 5% chance for RARE legendary placeholder
        if (Math.random() < 0.05) {
            ItemStack legendary = new ItemStack(Material.NETHER_STAR, 1);
            ItemMeta lmeta = legendary.getItemMeta();
            lmeta.displayName(Component.text("Rare Legendary Drop", NamedTextColor.GOLD));
            lmeta.getPersistentDataContainer().set(
                    new NamespacedKey(plugin, "legendary_drop"), PersistentDataType.STRING, "rare");
            legendary.setItemMeta(lmeta);
            drops.add(legendary);
        }
        return drops;
    }

    @Override
    protected long getTickRate() {
        return 5L;
    }
}

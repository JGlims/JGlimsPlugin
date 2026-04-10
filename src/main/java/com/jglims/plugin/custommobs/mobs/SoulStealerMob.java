package com.jglims.plugin.custommobs.mobs;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.custommobs.CustomMobEntity;
import com.jglims.plugin.custommobs.CustomMobType;
import kr.toxicity.model.api.animation.AnimationIterator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

/**
 * Soul Stealer - Aether cave ghost that steals XP.
 * HP 90. Each hit steals 1 XP level. At 10 stolen levels becomes empowered
 * (faster, 16 dmg). If killed empowered, drops all stolen XP + bonus.
 */
public class SoulStealerMob extends CustomMobEntity {

    private long lastAttackTick = 0;
    private int stolenLevels = 0;
    private boolean empowered = false;
    private static final int EMPOWER_THRESHOLD = 10;
    private static final long ATTACK_COOLDOWN = 25L;

    public SoulStealerMob(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.SOUL_STEALER);
    }

    @Override
    protected void onSpawn() {
        playAnimation("idle", AnimationIterator.Type.LOOP);
    }

    @Override
    protected void onTick() {
        if (hitboxEntity == null || !alive) return;

        Player target = findNearestPlayer(16);
        if (target == null) {
            state = MobState.IDLE;
            return;
        }

        state = MobState.ATTACKING;
        if (hitboxEntity instanceof org.bukkit.entity.Mob mob) {
            mob.setTarget(target);
        }

        double dist = hitboxEntity.getLocation().distanceSquared(target.getLocation());
        long ticks = hitboxEntity.getTicksLived();
        long cooldown = empowered ? ATTACK_COOLDOWN / 2 : ATTACK_COOLDOWN;

        // Empowered visual
        if (empowered && ticks % 10 == 0) {
            hitboxEntity.getWorld().spawnParticle(Particle.SOUL, hitboxEntity.getLocation().add(0, 1, 0),
                    5, 0.3, 0.5, 0.3, 0.02);
        }

        // Melee attack + soul steal
        if (dist < 9 && ticks - lastAttackTick >= cooldown) {
            lastAttackTick = ticks;

            double dmg = empowered ? 16.0 : mobType.getBaseDamage();
            playAnimation(empowered ? "empowered_attack" : "attack", AnimationIterator.Type.PLAY_ONCE);
            target.damage(dmg, hitboxEntity);
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_VEX_HURT, 1.0f, 0.5f);

            // Steal 1 XP level
            if (target.getLevel() > 0) {
                target.setLevel(target.getLevel() - 1);
                stolenLevels++;
                hitboxEntity.getWorld().spawnParticle(Particle.SOUL,
                        target.getLocation().add(0, 1.5, 0), 10, 0.3, 0.5, 0.3, 0.05);
                target.sendMessage(Component.text("The Soul Stealer siphons your experience! ("
                        + stolenLevels + " levels stolen)", NamedTextColor.DARK_AQUA));

                // Check for empower
                if (!empowered && stolenLevels >= EMPOWER_THRESHOLD) {
                    empowered = true;
                    hitboxEntity.addPotionEffect(
                            new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, false));
                    playAnimation("empower", AnimationIterator.Type.PLAY_ONCE);
                    hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                            Sound.ENTITY_WITHER_SPAWN, 1.0f, 1.5f);
                    hitboxEntity.customName(Component.text("Empowered Soul Stealer", NamedTextColor.DARK_PURPLE));

                    for (Player p : findNearbyPlayers(20)) {
                        p.sendMessage(Component.text("The Soul Stealer is empowered with stolen souls!",
                                NamedTextColor.DARK_PURPLE));
                    }
                }
            }
        }
    }

    @Override
    protected void onDamage(double amount, Player source) {
        playAnimation("hurt", AnimationIterator.Type.PLAY_ONCE);
        hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_VEX_HURT, 1.0f, 0.6f);
    }

    @Override
    protected void onDeath(Player killer) {
        if (hitboxEntity != null) {
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_VEX_DEATH, 1.5f, 0.5f);
            hitboxEntity.getWorld().spawnParticle(Particle.SOUL, hitboxEntity.getLocation().add(0, 1, 0),
                    30 + stolenLevels * 2, 1, 1, 1, 0.05);
        }
        if (killer != null) {
            // Return stolen XP + bonus if empowered
            int xpReturn = empowered ? stolenLevels * 2 + 50 : stolenLevels;
            for (int i = 0; i < xpReturn; i++) {
                killer.giveExp(7); // Roughly 1 level worth per iteration at low levels
            }
            killer.sendMessage(Component.text(
                    empowered ? "The empowered soul releases all stolen experience!" :
                            "The Soul Stealer's stolen experience dissipates.",
                    NamedTextColor.GREEN));
        }
    }

    @Override
    protected List<ItemStack> getDrops(Player killer) {
        List<ItemStack> drops = new ArrayList<>();
        // Soul Fragment (renamed lapis)
        ItemStack soulFrag = new ItemStack(Material.LAPIS_LAZULI, 1 + (int) (Math.random() * 3));
        ItemMeta meta = soulFrag.getItemMeta();
        meta.displayName(Component.text("Soul Fragment", NamedTextColor.AQUA));
        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "custom_item"), PersistentDataType.STRING, "soul_fragment");
        soulFrag.setItemMeta(meta);
        drops.add(soulFrag);
        return drops;
    }

    @Override
    protected long getTickRate() {
        return 5L;
    }
}

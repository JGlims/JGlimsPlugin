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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Invaderling Archer - ranged Lunar attacker.
 * HP 25. Keeps distance, fires fast no-gravity energy arrows.
 * Drops lunar fragment, arrows.
 */
public class InvaderlingArcherMob extends CustomMobEntity {

    private long lastShotTick = 0;
    private static final long SHOT_COOLDOWN = 40L;
    private static final double PREFERRED_RANGE_SQ = 144; // 12 blocks

    public InvaderlingArcherMob(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.INVADERLING_ARCHER);
    }

    @Override
    protected void onSpawn() {
        playAnimation("idle", AnimationIterator.Type.LOOP);
    }

    @Override
    protected void onTick() {
        if (hitboxEntity == null || !alive) return;

        Player target = findNearestPlayer(25);
        if (target == null) {
            state = MobState.IDLE;
            return;
        }

        state = MobState.ATTACKING;
        double dist = hitboxEntity.getLocation().distanceSquared(target.getLocation());
        long ticks = hitboxEntity.getTicksLived();

        // Keep distance - retreat if player is too close
        if (dist < 36) { // Less than 6 blocks
            Vector away = hitboxEntity.getLocation().toVector().subtract(target.getLocation().toVector()).normalize();
            hitboxEntity.setVelocity(away.multiply(0.5).setY(0));
        } else if (hitboxEntity instanceof org.bukkit.entity.Mob mob) {
            // Move toward preferred range
            if (dist > 625) { // More than 25 blocks
                mob.setTarget(target);
            }
        }

        // Fire energy arrow
        if (dist > 25 && dist < 900 && ticks - lastShotTick >= SHOT_COOLDOWN) {
            lastShotTick = ticks;
            playAnimation("shoot", AnimationIterator.Type.PLAY_ONCE);
            fireEnergyArrow(target);
        }
    }

    private void fireEnergyArrow(Player target) {
        hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1.0f, 1.5f);
        Location start = hitboxEntity.getLocation().add(0, 1.5, 0);
        // Direct line (no gravity) - faster than normal arrows
        Vector dir = target.getLocation().add(0, 1, 0).toVector().subtract(start.toVector()).normalize();

        new BukkitRunnable() {
            Location pos = start.clone();
            int ticks = 0;

            @Override
            public void run() {
                if (ticks++ > 40 || !alive) { cancel(); return; }
                pos.add(dir.clone().multiply(1.5)); // Fast projectile
                pos.getWorld().spawnParticle(Particle.DUST, pos, 2,
                        new Particle.DustOptions(Color.fromRGB(150, 220, 255), 0.8f));

                for (Player p : pos.getWorld().getPlayers()) {
                    if (p.getGameMode() == GameMode.SPECTATOR || p.getGameMode() == GameMode.CREATIVE) continue;
                    if (p.getLocation().add(0, 1, 0).distanceSquared(pos) < 2) {
                        p.damage(mobType.getBaseDamage(), hitboxEntity);
                        pos.getWorld().playSound(pos, Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.5f);
                        cancel();
                        return;
                    }
                }
                // Stop if hits a block
                if (pos.getBlock().getType().isSolid()) {
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    @Override
    protected void onDamage(double amount, Player source) {
        playAnimation("hurt", AnimationIterator.Type.PLAY_ONCE);
        // Flee when hit
        if (source != null && hitboxEntity != null) {
            Vector away = hitboxEntity.getLocation().toVector().subtract(source.getLocation().toVector()).normalize();
            hitboxEntity.setVelocity(away.multiply(0.7).setY(0.2));
        }
    }

    @Override
    protected void onDeath(Player killer) {
        if (hitboxEntity != null) {
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_VEX_DEATH, 1.0f, 1.3f);
        }
        if (killer != null) {
            killer.giveExp(20);
        }
    }

    @Override
    protected List<ItemStack> getDrops(Player killer) {
        List<ItemStack> drops = new ArrayList<>();
        ItemStack lunarFrag = new ItemStack(Material.IRON_NUGGET, 1);
        ItemMeta meta = lunarFrag.getItemMeta();
        meta.displayName(Component.text("Lunar Fragment", NamedTextColor.AQUA));
        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "custom_item"), PersistentDataType.STRING, "lunar_fragment");
        lunarFrag.setItemMeta(meta);
        drops.add(lunarFrag);
        drops.add(new ItemStack(Material.ARROW, 2 + (int) (Math.random() * 4)));
        return drops;
    }

    @Override
    protected long getTickRate() {
        return 5L;
    }
}

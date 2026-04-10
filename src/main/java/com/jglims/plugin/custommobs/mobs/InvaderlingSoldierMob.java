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
 * Invaderling Soldier - basic Lunar hostile mob.
 * HP 35. Energy blade slash (melee), energy bolt (ranged slow projectile).
 * Patrols in groups. Drops lunar fragment (iron nugget renamed).
 */
public class InvaderlingSoldierMob extends CustomMobEntity {

    private long lastSlashTick = 0;
    private long lastBoltTick = 0;
    private static final long SLASH_COOLDOWN = 25L;
    private static final long BOLT_COOLDOWN = 60L;

    public InvaderlingSoldierMob(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.INVADERLING_SOLDIER);
    }

    @Override
    protected void onSpawn() {
        playAnimation("idle", AnimationIterator.Type.LOOP);
    }

    @Override
    protected void onTick() {
        if (hitboxEntity == null || !alive) return;

        Player target = findNearestPlayer(18);
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

        // Energy Bolt - ranged slow projectile, 3s cooldown
        if (dist > 16 && dist < 400 && ticks - lastBoltTick >= BOLT_COOLDOWN) {
            lastBoltTick = ticks;
            playAnimation("cast", AnimationIterator.Type.PLAY_ONCE);
            fireEnergyBolt(target);
        }

        // Energy Blade Slash - melee
        if (dist < 9 && ticks - lastSlashTick >= SLASH_COOLDOWN) {
            lastSlashTick = ticks;
            playAnimation("slash", AnimationIterator.Type.PLAY_ONCE);
            target.damage(mobType.getBaseDamage(), hitboxEntity);
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.2f);
        }
    }

    private void fireEnergyBolt(Player target) {
        hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.8f, 1.5f);
        Location start = hitboxEntity.getLocation().add(0, 1.2, 0);
        Vector dir = target.getLocation().add(0, 1, 0).toVector().subtract(start.toVector()).normalize();

        new BukkitRunnable() {
            Location pos = start.clone();
            int ticks = 0;

            @Override
            public void run() {
                if (ticks++ > 60 || !alive) { cancel(); return; }
                pos.add(dir.clone().multiply(0.6));
                pos.getWorld().spawnParticle(Particle.DUST, pos, 2,
                        new Particle.DustOptions(Color.fromRGB(100, 200, 255), 1.0f));

                for (Player p : pos.getWorld().getPlayers()) {
                    if (p.getGameMode() == GameMode.SPECTATOR || p.getGameMode() == GameMode.CREATIVE) continue;
                    if (p.getLocation().add(0, 1, 0).distanceSquared(pos) < 2) {
                        p.damage(5.0, hitboxEntity);
                        pos.getWorld().playSound(pos, Sound.BLOCK_AMETHYST_BLOCK_HIT, 1.0f, 1.5f);
                        cancel();
                        return;
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    @Override
    protected void onDamage(double amount, Player source) {
        playAnimation("hurt", AnimationIterator.Type.PLAY_ONCE);
    }

    @Override
    protected void onDeath(Player killer) {
        if (hitboxEntity != null) {
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_VEX_DEATH, 1.0f, 1.2f);
        }
        if (killer != null) {
            killer.giveExp(25);
        }
    }

    @Override
    protected List<ItemStack> getDrops(Player killer) {
        List<ItemStack> drops = new ArrayList<>();
        ItemStack lunarFrag = new ItemStack(Material.IRON_NUGGET, 1 + (int) (Math.random() * 2));
        ItemMeta meta = lunarFrag.getItemMeta();
        meta.displayName(Component.text("Lunar Fragment", NamedTextColor.AQUA));
        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "custom_item"), PersistentDataType.STRING, "lunar_fragment");
        lunarFrag.setItemMeta(meta);
        drops.add(lunarFrag);
        return drops;
    }

    @Override
    protected long getTickRate() {
        return 5L;
    }
}

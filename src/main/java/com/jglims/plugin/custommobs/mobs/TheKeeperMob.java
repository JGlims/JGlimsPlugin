package com.jglims.plugin.custommobs.mobs;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.custommobs.CustomMobEntity;
import com.jglims.plugin.custommobs.CustomMobType;
import kr.toxicity.model.api.animation.AnimationIterator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Player;
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
 * The Keeper - enigmatic teleporter found in Aether and Lunar.
 * HP 200. Teleports with 5s cooldown, leaves lingering damage cloud.
 * Void touch (15 dmg + Darkness), teleport strike (behind player), void explosion on death.
 */
public class TheKeeperMob extends CustomMobEntity {

    private long lastTeleportTick = 0;
    private long lastVoidTouchTick = 0;
    private long lastStrikeTick = 0;
    private static final long TELEPORT_COOLDOWN = 100L;

    public TheKeeperMob(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.THE_KEEPER);
    }

    @Override
    protected void onSpawn() {
        playAnimation("idle", AnimationIterator.Type.LOOP);
    }

    @Override
    protected void onTick() {
        if (hitboxEntity == null || !alive) return;

        Player target = findNearestPlayer(15);
        if (target == null) {
            state = MobState.IDLE;
            return;
        }

        state = MobState.ATTACKING;
        double dist = hitboxEntity.getLocation().distanceSquared(target.getLocation());
        long ticks = hitboxEntity.getTicksLived();

        // Teleport Strike - teleport behind player and attack, 5s cooldown
        if (ticks - lastTeleportTick >= TELEPORT_COOLDOWN) {
            lastTeleportTick = ticks;
            // Leave lingering damage cloud at current position
            spawnDamageCloud(hitboxEntity.getLocation());

            // Teleport behind player
            Location behind = target.getLocation().clone();
            Vector dir = behind.getDirection().normalize().multiply(-2);
            behind.add(dir);
            behind.setY(target.getLocation().getY());

            playAnimation("teleport", AnimationIterator.Type.PLAY_ONCE);
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 2.0f, 0.5f);
            hitboxEntity.getWorld().spawnParticle(Particle.PORTAL, hitboxEntity.getLocation().add(0, 1, 0), 30, 0.5, 1, 0.5);

            moveTo(behind);

            hitboxEntity.getWorld().playSound(behind, Sound.ENTITY_ENDERMAN_TELEPORT, 2.0f, 0.5f);
            hitboxEntity.getWorld().spawnParticle(Particle.PORTAL, behind.clone().add(0, 1, 0), 30, 0.5, 1, 0.5);

            // Strike after teleport
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!alive || hitboxEntity == null) return;
                    if (hitboxEntity.getLocation().distanceSquared(target.getLocation()) < 16) {
                        playAnimation("strike", AnimationIterator.Type.PLAY_ONCE);
                        target.damage(10.0, hitboxEntity);
                        target.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 60, 0, false, false));
                    }
                }
            }.runTaskLater(plugin, 5L);
        }

        // Void Touch - 15 damage + Darkness, 6s cooldown
        if (dist < 9 && ticks - lastVoidTouchTick >= 120) {
            lastVoidTouchTick = ticks;
            playAnimation("void_touch", AnimationIterator.Type.PLAY_ONCE);
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1.0f, 0.5f);
            target.damage(15.0, hitboxEntity);
            target.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 100, 1, false, false));
            hitboxEntity.getWorld().spawnParticle(Particle.DUST, target.getLocation().add(0, 1, 0),
                    20, 0.5, 1, 0.5, new Particle.DustOptions(Color.fromRGB(20, 0, 40), 2.0f));
        }
    }

    private void spawnDamageCloud(Location loc) {
        AreaEffectCloud cloud = loc.getWorld().spawn(loc, AreaEffectCloud.class, c -> {
            c.setRadius(2.5f);
            c.setDuration(100); // 5 seconds
            c.setRadiusPerTick(-0.01f);
            c.addCustomEffect(new PotionEffect(PotionEffectType.INSTANT_DAMAGE, 1, 0), true);
            c.addCustomEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1), true);
            c.setColor(Color.fromRGB(30, 0, 50));
            c.setParticle(Particle.DUST, new Particle.DustOptions(Color.fromRGB(30, 0, 50), 1.5f));
        });
    }

    @Override
    protected void onDamage(double amount, Player source) {
        playAnimation("hurt", AnimationIterator.Type.PLAY_ONCE);
    }

    @Override
    protected void onDeath(Player killer) {
        if (hitboxEntity == null) return;

        // Void explosion on death - 6 block AoE
        Location loc = hitboxEntity.getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_WARDEN_SONIC_BOOM, 2.0f, 0.3f);
        loc.getWorld().spawnParticle(Particle.EXPLOSION, loc.add(0, 1, 0), 5, 1, 1, 1);
        loc.getWorld().spawnParticle(Particle.DUST, loc, 50, 3, 3, 3,
                new Particle.DustOptions(Color.fromRGB(20, 0, 40), 3.0f));

        for (Player p : findNearbyPlayers(6)) {
            p.damage(12.0);
            p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 100, 1, false, false));
            Vector knockback = p.getLocation().toVector().subtract(loc.toVector()).normalize().multiply(1.5).setY(0.5);
            p.setVelocity(knockback);
        }

        if (killer != null) {
            killer.giveExp(150);
        }
    }

    @Override
    protected List<ItemStack> getDrops(Player killer) {
        List<ItemStack> drops = new ArrayList<>();
        // Void Essence (renamed ender pearl)
        ItemStack voidEssence = new ItemStack(Material.ENDER_PEARL, 1 + (int) (Math.random() * 2));
        ItemMeta meta = voidEssence.getItemMeta();
        meta.displayName(Component.text("Void Essence", NamedTextColor.DARK_PURPLE));
        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "custom_item"), PersistentDataType.STRING, "void_essence");
        voidEssence.setItemMeta(meta);
        drops.add(voidEssence);
        drops.add(new ItemStack(Material.PHANTOM_MEMBRANE, 2 + (int) (Math.random() * 3)));
        return drops;
    }

    @Override
    protected long getTickRate() {
        return 5L;
    }
}

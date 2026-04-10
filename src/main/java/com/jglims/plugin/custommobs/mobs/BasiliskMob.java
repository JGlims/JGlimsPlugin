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
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Basilisk - Jurassic cave serpent.
 * HP 80. GAZE ATTACK: if player looks at it for 3+ seconds, applies Slowness V + Mining Fatigue III.
 * Venomous bite (Poison II 4s), tail constrict (immobilize 2s).
 */
public class BasiliskMob extends CustomMobEntity {

    private long lastBiteTick = 0;
    private long lastConstrictTick = 0;
    private final Map<UUID, Long> gazeTimers = new HashMap<>();

    private static final long BITE_COOLDOWN = 40L;
    private static final long CONSTRICT_COOLDOWN = 100L;
    private static final long GAZE_DURATION_MS = 3000L; // 3 seconds

    public BasiliskMob(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.BASILISK);
    }

    @Override
    protected void onSpawn() {
        playAnimation("idle", AnimationIterator.Type.LOOP);
    }

    @Override
    protected void onTick() {
        if (hitboxEntity == null || !alive) return;

        // Check gaze for all nearby players
        checkGaze();

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

        // Venomous Bite - Poison II for 4s, 2s cooldown
        if (dist < 9 && ticks - lastBiteTick >= BITE_COOLDOWN) {
            lastBiteTick = ticks;
            playAnimation("bite", AnimationIterator.Type.PLAY_ONCE);
            target.damage(mobType.getBaseDamage(), hitboxEntity);
            target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 80, 1, false, false));
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_PHANTOM_BITE, 1.5f, 0.4f);
            hitboxEntity.getWorld().spawnParticle(Particle.ITEM_SLIME, target.getLocation().add(0, 1, 0), 10, 0.3, 0.3, 0.3);
        }

        // Tail Constrict - immobilize for 2s, 5s cooldown
        if (dist < 12 && ticks - lastConstrictTick >= CONSTRICT_COOLDOWN) {
            lastConstrictTick = ticks;
            playAnimation("constrict", AnimationIterator.Type.PLAY_ONCE);
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_SILVERFISH_HURT, 1.5f, 0.5f);
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 9, false, false)); // Effective immobilize
            target.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 40, 128, false, false)); // Prevent jumping
            target.damage(4.0, hitboxEntity);
            target.sendMessage(Component.text("The Basilisk coils around you!", NamedTextColor.DARK_GREEN));
        }
    }

    /**
     * Checks if any player is looking directly at the basilisk for 3+ seconds.
     * If so, applies Slowness V + Mining Fatigue III for 5s.
     */
    private void checkGaze() {
        if (hitboxEntity == null) return;
        Location mobLoc = hitboxEntity.getLocation().add(0, 1, 0);
        long now = System.currentTimeMillis();

        for (Player player : findNearbyPlayers(16)) {
            Location eyeLoc = player.getEyeLocation();
            Vector lookDir = eyeLoc.getDirection().normalize();
            Vector toMob = mobLoc.toVector().subtract(eyeLoc.toVector()).normalize();

            double dot = lookDir.dot(toMob);
            // dot > 0.95 means player is looking almost directly at the mob
            if (dot > 0.95) {
                UUID pid = player.getUniqueId();
                gazeTimers.putIfAbsent(pid, now);

                long gazeStart = gazeTimers.get(pid);
                if (now - gazeStart >= GAZE_DURATION_MS) {
                    // Petrify!
                    playAnimation("gaze", AnimationIterator.Type.PLAY_ONCE);
                    hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 1.5f, 0.5f);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 4, false, false));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 100, 2, false, false));
                    player.sendMessage(Component.text("The Basilisk's gaze petrifies you!", NamedTextColor.DARK_RED));

                    hitboxEntity.getWorld().spawnParticle(Particle.DUST, player.getLocation().add(0, 1, 0),
                            20, 0.5, 1, 0.5, new Particle.DustOptions(Color.fromRGB(80, 80, 80), 1.5f));
                    gazeTimers.put(pid, now); // Reset timer
                }
            } else {
                gazeTimers.remove(player.getUniqueId());
            }
        }

        // Clean up disconnected players
        gazeTimers.keySet().removeIf(uuid -> {
            Player p = org.bukkit.Bukkit.getPlayer(uuid);
            return p == null || !p.isOnline();
        });
    }

    @Override
    protected void onDamage(double amount, Player source) {
        playAnimation("hurt", AnimationIterator.Type.PLAY_ONCE);
        hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_SILVERFISH_HURT, 1.5f, 0.5f);
    }

    @Override
    protected void onDeath(Player killer) {
        if (hitboxEntity != null) {
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_SILVERFISH_DEATH, 1.5f, 0.4f);
        }
        if (killer != null) {
            killer.giveExp(60);
        }
    }

    @Override
    protected List<ItemStack> getDrops(Player killer) {
        List<ItemStack> drops = new ArrayList<>();
        // Basilisk Fang (renamed bone)
        ItemStack fang = new ItemStack(Material.BONE, 1 + (int) (Math.random() * 2));
        ItemMeta meta = fang.getItemMeta();
        meta.displayName(Component.text("Basilisk Fang", NamedTextColor.DARK_GREEN));
        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "custom_item"), PersistentDataType.STRING, "basilisk_fang");
        fang.setItemMeta(meta);
        drops.add(fang);
        return drops;
    }

    @Override
    protected long getTickRate() {
        return 5L;
    }
}

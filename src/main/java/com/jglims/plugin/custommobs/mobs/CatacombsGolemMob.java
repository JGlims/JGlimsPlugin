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
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Catacombs Golem - stone fist melee mob.
 * HP 80. KEY MECHANIC: every attack and death generates vibrations that attract the Warden.
 * Drops sculk sensor, 15% echo shard, deepslate.
 */
public class CatacombsGolemMob extends CustomMobEntity {

    private long lastAttackTick = 0;
    private static final long ATTACK_COOLDOWN = 30L;

    public CatacombsGolemMob(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.CATACOMBS_GOLEM);
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

        // Stone Fist melee + vibration
        if (dist < 9 && ticks - lastAttackTick >= ATTACK_COOLDOWN) {
            lastAttackTick = ticks;
            playAnimation("attack", AnimationIterator.Type.PLAY_ONCE);
            target.damage(mobType.getBaseDamage(), hitboxEntity);
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                    Sound.ENTITY_IRON_GOLEM_ATTACK, 1.5f, 0.6f);

            // Generate vibration to attract Warden
            generateVibration();
        }
    }

    /**
     * Generates a sculk vibration at the golem's location.
     * This uses a loud sound that sculk sensors and the Warden detect.
     */
    private void generateVibration() {
        if (hitboxEntity == null) return;
        Location loc = hitboxEntity.getLocation();

        // Play sounds that sculk sensors detect
        loc.getWorld().playSound(loc, Sound.BLOCK_SCULK_SENSOR_CLICKING, 2.0f, 1.0f);
        loc.getWorld().playSound(loc, Sound.ENTITY_IRON_GOLEM_ATTACK, 2.0f, 0.5f);
        loc.getWorld().spawnParticle(Particle.SCULK_CHARGE_POP, loc.add(0, 0.5, 0), 10, 1, 0.5, 1);

        // Visual warning to players
        for (Player p : findNearbyPlayers(20)) {
            p.sendMessage(Component.text("The ground trembles... something stirs in the deep.",
                    NamedTextColor.DARK_AQUA));
        }
    }

    @Override
    protected void onDamage(double amount, Player source) {
        playAnimation("hurt", AnimationIterator.Type.PLAY_ONCE);
        hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.BLOCK_DEEPSLATE_BREAK, 1.5f, 0.7f);
        // Taking damage also generates vibrations
        generateVibration();
    }

    @Override
    protected void onDeath(Player killer) {
        if (hitboxEntity != null) {
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_IRON_GOLEM_DEATH, 1.5f, 0.5f);
            hitboxEntity.getWorld().spawnParticle(Particle.BLOCK, hitboxEntity.getLocation(),
                    30, 1, 1, 1, Bukkit.createBlockData(Material.DEEPSLATE));
            // Death generates a large vibration
            generateVibration();
        }
        if (killer != null) {
            killer.giveExp(60);
        }
    }

    @Override
    protected List<ItemStack> getDrops(Player killer) {
        List<ItemStack> drops = new ArrayList<>();
        drops.add(new ItemStack(Material.SCULK_SENSOR, 1));
        drops.add(new ItemStack(Material.DEEPSLATE, 3 + (int) (Math.random() * 4)));

        // 15% echo shard
        if (Math.random() < 0.15) {
            drops.add(new ItemStack(Material.ECHO_SHARD, 1));
        }
        return drops;
    }

    @Override
    protected long getTickRate() {
        return 5L;
    }
}

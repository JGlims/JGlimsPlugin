package com.jglims.plugin.custommobs.mobs;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.custommobs.CustomMobEntity;
import com.jglims.plugin.custommobs.CustomMobType;
import kr.toxicity.model.api.animation.AnimationIterator;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Prismamorpha - corrupted guardian variant in Ocean Monuments.
 * HP 70. Prism beam, crystal spike, sonar pulse (reveals invis).
 */
public class PrismamorphaMob extends CustomMobEntity {

    private long lastBeamTick = 0;
    private long lastSpikeTick = 0;
    private long lastSonarTick = 0;
    private Player beamTarget = null;
    private int beamChargeTicks = 0;

    public PrismamorphaMob(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.PRISMAMORPHA);
    }

    @Override
    protected void onSpawn() {
        playAnimation("idle", AnimationIterator.Type.LOOP);
    }

    @Override
    protected void onTick() {
        if (hitboxEntity == null || !alive) return;

        Player target = findNearestPlayer(20);
        if (target == null) {
            state = MobState.IDLE;
            beamTarget = null;
            beamChargeTicks = 0;
            return;
        }

        state = MobState.ATTACKING;
        if (hitboxEntity instanceof org.bukkit.entity.Mob mob) {
            mob.setTarget(target);
        }

        double dist = hitboxEntity.getLocation().distanceSquared(target.getLocation());
        long ticks = hitboxEntity.getTicksLived();

        // Sonar Pulse - reveals invisible players, 10s cooldown
        if (ticks - lastSonarTick >= 200) {
            lastSonarTick = ticks;
            playAnimation("sonar", AnimationIterator.Type.PLAY_ONCE);
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_GUARDIAN_AMBIENT, 2.0f, 1.5f);
            for (Player p : findNearbyPlayers(15)) {
                p.removePotionEffect(PotionEffectType.INVISIBILITY);
                p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 60, 0, false, false));
            }
        }

        // Prism Beam - guardian-like beam, 4s cooldown, charges for 1.5s
        if (dist > 9 && dist < 625 && ticks - lastBeamTick >= 80) {
            if (beamTarget == null) {
                beamTarget = target;
                beamChargeTicks = 0;
                playAnimation("beam_charge", AnimationIterator.Type.PLAY_ONCE);
                hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_GUARDIAN_ATTACK, 1.0f, 1.0f);
            }
            beamChargeTicks++;
            // Draw charging beam
            drawBeam(hitboxEntity.getLocation().add(0, 1, 0), beamTarget.getLocation().add(0, 1, 0));

            if (beamChargeTicks >= 30) { // 1.5s at tick rate 1 (but we run every 5 ticks, so 6 calls)
                lastBeamTick = ticks;
                beamTarget.damage(8.0, hitboxEntity);
                hitboxEntity.getWorld().playSound(beamTarget.getLocation(), Sound.ENTITY_GUARDIAN_HURT, 1.0f, 1.2f);
                beamTarget = null;
                beamChargeTicks = 0;
            }
        }

        // Crystal Spike - AoE damage around mob, 6s cooldown
        if (dist < 36 && ticks - lastSpikeTick >= 120) {
            lastSpikeTick = ticks;
            playAnimation("spike", AnimationIterator.Type.PLAY_ONCE);
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_BREAK, 2.0f, 0.6f);
            Location loc = hitboxEntity.getLocation();
            loc.getWorld().spawnParticle(Particle.BLOCK, loc.add(0, 0.5, 0), 30,
                    1.5, 0.5, 1.5, Bukkit.createBlockData(Material.PRISMARINE));
            for (Player p : findNearbyPlayers(5)) {
                p.damage(6.0, hitboxEntity);
                p.setVelocity(new Vector(0, 0.5, 0));
            }
        }
    }

    private void drawBeam(Location from, Location to) {
        Vector dir = to.toVector().subtract(from.toVector());
        double length = dir.length();
        dir.normalize();
        for (double d = 0; d < length; d += 0.5) {
            Location point = from.clone().add(dir.clone().multiply(d));
            from.getWorld().spawnParticle(Particle.DUST, point, 1,
                    new Particle.DustOptions(Color.fromRGB(0, 200, 200), 1.2f));
        }
    }

    @Override
    protected void onDamage(double amount, Player source) {
        playAnimation("hurt", AnimationIterator.Type.PLAY_ONCE);
    }

    @Override
    protected void onDeath(Player killer) {
        if (hitboxEntity != null) {
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(), Sound.ENTITY_GUARDIAN_DEATH, 1.0f, 0.8f);
        }
        if (killer != null) {
            killer.giveExp(55);
        }
    }

    @Override
    protected List<ItemStack> getDrops(Player killer) {
        List<ItemStack> drops = new ArrayList<>();
        // Double prismarine crystals
        drops.add(new ItemStack(Material.PRISMARINE_CRYSTALS, 4 + (int) (Math.random() * 4)));
        return drops;
    }

    @Override
    protected long getTickRate() {
        return 5L;
    }
}

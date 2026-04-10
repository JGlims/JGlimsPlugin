package com.jglims.plugin.custommobs.mobs;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.custommobs.CustomMobEntity;
import com.jglims.plugin.custommobs.CustomMobType;
import kr.toxicity.model.api.animation.AnimationIterator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Grass Father - Passive nature golem that wanders plains placing flowers
 * every 30 seconds. If attacked, nearby passive mobs become hostile to the
 * attacker for 10 seconds. Drops flowers and bone meal.
 */
public class GrassFatherMob extends CustomMobEntity {

    private long lastFlowerTick = 0;
    private static final long FLOWER_INTERVAL = 600L; // 30 seconds at 1 tick rate
    private static final Material[] FLOWERS = {
            Material.DANDELION, Material.POPPY, Material.BLUE_ORCHID,
            Material.ALLIUM, Material.AZURE_BLUET, Material.RED_TULIP,
            Material.ORANGE_TULIP, Material.WHITE_TULIP, Material.PINK_TULIP,
            Material.OXEYE_DAISY, Material.CORNFLOWER, Material.LILY_OF_THE_VALLEY
    };

    public GrassFatherMob(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.GRASS_FATHER);
    }

    @Override
    protected void onSpawn() {
        playAnimation("idle", AnimationIterator.Type.LOOP);
    }

    @Override
    protected void onTick() {
        if (hitboxEntity == null || !alive) return;

        long ticks = hitboxEntity.getTicksLived();

        // Place flowers every 30 seconds
        if (ticks - lastFlowerTick >= FLOWER_INTERVAL) {
            lastFlowerTick = ticks;
            placeFlower();
        }
    }

    private void placeFlower() {
        if (hitboxEntity == null) return;
        Location loc = hitboxEntity.getLocation();
        // Try up to 5 random nearby positions
        Random rand = new Random();
        for (int attempt = 0; attempt < 5; attempt++) {
            int dx = rand.nextInt(5) - 2;
            int dz = rand.nextInt(5) - 2;
            Block ground = loc.getWorld().getHighestBlockAt(
                    loc.getBlockX() + dx, loc.getBlockZ() + dz);
            Block above = ground.getRelative(0, 1, 0);
            if (ground.getType() == Material.GRASS_BLOCK && above.getType() == Material.AIR) {
                Material flower = FLOWERS[rand.nextInt(FLOWERS.length)];
                above.setType(flower);
                loc.getWorld().spawnParticle(Particle.HAPPY_VILLAGER,
                        above.getLocation().add(0.5, 0.5, 0.5), 8, 0.3, 0.3, 0.3, 0);
                playAnimation("place", AnimationIterator.Type.PLAY_ONCE);
                break;
            }
        }
    }

    @Override
    protected void onDamage(double amount, Player source) {
        playAnimation("hurt", AnimationIterator.Type.PLAY_ONCE);
        hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                Sound.BLOCK_GRASS_BREAK, 1.0f, 0.6f);

        if (source != null) {
            rallyCritters(source);
        }
    }

    /**
     * Makes nearby passive mobs hostile to the attacker for 10 seconds.
     */
    private void rallyCritters(Player attacker) {
        if (hitboxEntity == null) return;
        Location loc = hitboxEntity.getLocation();

        hitboxEntity.getWorld().playSound(loc, Sound.ENTITY_GOAT_SCREAMING_AMBIENT, 1.5f, 0.5f);
        hitboxEntity.getWorld().spawnParticle(Particle.ANGRY_VILLAGER,
                loc.add(0, 2, 0), 20, 2, 1, 2, 0);

        List<LivingEntity> nearbyMobs = new ArrayList<>();
        for (Entity e : loc.getWorld().getNearbyEntities(loc, 15, 10, 15)) {
            if (e instanceof Animals || e instanceof IronGolem) {
                nearbyMobs.add((LivingEntity) e);
            }
        }

        for (LivingEntity mob : nearbyMobs) {
            if (mob instanceof Mob creature) {
                creature.setTarget(attacker);
            }
        }

        attacker.sendMessage(Component.text("The creatures of nature turn against you!",
                NamedTextColor.DARK_GREEN));

        // Reset aggro after 10 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                for (LivingEntity mob : nearbyMobs) {
                    if (mob.isValid() && mob instanceof Mob creature) {
                        creature.setTarget(null);
                    }
                }
            }
        }.runTaskLater(plugin, 200L);
    }

    @Override
    protected void onDeath(Player killer) {
        if (hitboxEntity != null) {
            Location loc = hitboxEntity.getLocation();
            hitboxEntity.getWorld().playSound(loc, Sound.BLOCK_GRASS_BREAK, 1.5f, 0.5f);
            // Burst of flowers on death
            for (int i = 0; i < 5; i++) {
                loc.getWorld().spawnParticle(Particle.HAPPY_VILLAGER,
                        loc.add(0, 1, 0), 20, 1.5, 1, 1.5, 0);
            }
        }
        if (killer != null) {
            killer.giveExp(15);
        }
    }

    @Override
    protected List<ItemStack> getDrops(Player killer) {
        List<ItemStack> drops = new ArrayList<>();
        Random rand = new Random();
        // Random flowers
        for (int i = 0; i < 3 + rand.nextInt(4); i++) {
            drops.add(new ItemStack(FLOWERS[rand.nextInt(FLOWERS.length)], 1));
        }
        // Bone meal
        drops.add(new ItemStack(Material.BONE_MEAL, 4 + rand.nextInt(5)));
        return drops;
    }

    @Override
    protected long getTickRate() {
        return 10L;
    }
}

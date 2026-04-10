package com.jglims.plugin.custommobs.mobs;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.custommobs.CustomMobType;
import com.jglims.plugin.custommobs.RideableMobEntity;
import kr.toxicity.model.api.animation.AnimationIterator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Ice Wyvern - Small ice flying mount. Bare hands + no armor to initiate taming,
 * ride 30s, feed cooked cod + saddle to complete. 1 rider, no storage.
 * Slower flight than dragons. Special: ice breath projectile (5 dmg + Slowness III 3s, 4s CD).
 * Takes damage (1 heart/5s) in hot biomes.
 */
public class IceWyvernMob extends RideableMobEntity {

    private boolean calmStarted = false;
    private long calmRideStartTick = -1;
    private static final long CALM_RIDE_TICKS = 600L; // 30 seconds
    private boolean calmed = false;
    private boolean codFed = false;
    private long lastHeatDamageTick = 0;

    private static final Set<Biome> HOT_BIOMES = Set.of(
            Biome.DESERT, Biome.BADLANDS, Biome.ERODED_BADLANDS,
            Biome.WOODED_BADLANDS, Biome.NETHER_WASTES,
            Biome.SOUL_SAND_VALLEY, Biome.CRIMSON_FOREST,
            Biome.WARPED_FOREST, Biome.BASALT_DELTAS
    );

    public IceWyvernMob(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.ICE_WYVERN);
        this.canFly = true;
        this.mountSpeed = 0.18;
        this.jumpStrength = 1.0;
        this.maxRiders = 1;
        this.chestSlots = 0;
        this.tamingRequirement = 1.0;
        this.fallDamageReduction = 0.0;
        this.specialAbilityCooldownTicks = 80L; // 4s CD
    }

    @Override
    protected void onSpawn() {
        playAnimation("idle", AnimationIterator.Type.LOOP);
    }

    @Override
    protected void onTick() {
        super.onTick();
        if (hitboxEntity == null || !alive) return;

        // Hot biome damage
        Biome currentBiome = hitboxEntity.getLocation().getBlock().getBiome();
        if (HOT_BIOMES.contains(currentBiome)) {
            long ticks = hitboxEntity.getTicksLived();
            if (ticks - lastHeatDamageTick >= 100) { // 5 seconds
                lastHeatDamageTick = ticks;
                hitboxEntity.damage(2.0); // 1 heart
                hitboxEntity.getWorld().spawnParticle(Particle.DRIPPING_WATER,
                        hitboxEntity.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0);
            }
        }

        // Ice particles ambient
        hitboxEntity.getWorld().spawnParticle(Particle.SNOWFLAKE,
                hitboxEntity.getLocation().add(0, 0.5, 0), 1, 0.3, 0.2, 0.3, 0.01);

        // Calming ride progress
        if (calmRideStartTick > 0 && !hitboxEntity.getPassengers().isEmpty()) {
            long ridden = rideTicks - calmRideStartTick;
            if (ridden >= CALM_RIDE_TICKS) {
                calmed = true;
                calmRideStartTick = -1;
                for (Entity passenger : hitboxEntity.getPassengers()) {
                    if (passenger instanceof Player p) {
                        p.sendMessage(Component.text("The Ice Wyvern trusts you! Feed it cooked cod.",
                                NamedTextColor.AQUA));
                    }
                }
            }
        }

        // Follow owner if tamed
        if (tamed && hitboxEntity.getPassengers().isEmpty()) {
            Player owner = getOwner();
            if (owner != null) {
                double dist = hitboxEntity.getLocation().distanceSquared(owner.getLocation());
                if (dist > 400) {
                    hitboxEntity.teleport(owner.getLocation().add(1, 0, 1));
                } else if (dist > 16 && hitboxEntity instanceof Mob mob) {
                    mob.getPathfinder().moveTo(owner.getLocation(), 1.0);
                }
            }
        }
    }

    @Override
    public void onInteract(Player player) {
        if (tamed) {
            super.onInteract(player);
            return;
        }

        if (!calmStarted) {
            // Check bare hands + no armor
            if (isWearingArmor(player) || player.getInventory().getItemInMainHand().getType() != Material.AIR) {
                player.sendMessage(Component.text("Approach with bare hands and no armor!", NamedTextColor.AQUA));
                return;
            }
            calmStarted = true;
            hitboxEntity.addPassenger(player);
            calmRideStartTick = rideTicks;
            player.sendMessage(Component.text("Ride the Ice Wyvern for 30 seconds to calm it!",
                    NamedTextColor.AQUA));
            return;
        }

        if (!calmed) {
            player.sendMessage(Component.text("The wyvern is not yet calm.", NamedTextColor.GRAY));
            return;
        }

        ItemStack hand = player.getInventory().getItemInMainHand();
        if (!codFed) {
            if (hand.getType() == Material.COOKED_COD) {
                hand.setAmount(hand.getAmount() - 1);
                codFed = true;
                hitboxEntity.getWorld().spawnParticle(Particle.HEART,
                        hitboxEntity.getLocation().add(0, 1.5, 0), 8, 0.5, 0.3, 0.5, 0);
                player.sendMessage(Component.text("Now apply a saddle!", NamedTextColor.GREEN));
            } else {
                player.sendMessage(Component.text("Feed it cooked cod.", NamedTextColor.GRAY));
            }
            return;
        }

        if (hand.getType() == Material.SADDLE) {
            hand.setAmount(hand.getAmount() - 1);
            saddled = true;
            addTamingProgress(tamingRequirement, player);
        } else {
            player.sendMessage(Component.text("Apply a saddle to tame!", NamedTextColor.GRAY));
        }
    }

    @Override
    protected void onTamed(Player player) {
        if (hitboxEntity != null) {
            hitboxEntity.customName(Component.text(player.getName() + "'s Ice Wyvern",
                    NamedTextColor.AQUA));
        }
    }

    @Override
    protected void onMount(Player player) {
        playAnimation("fly_mounted", AnimationIterator.Type.LOOP);
    }

    @Override
    protected void onDismount(Player player) {
        playAnimation("idle", AnimationIterator.Type.LOOP);
    }

    /**
     * Ice Breath - 5 dmg + Slowness III for 3 seconds.
     */
    @Override
    protected void onSpecialAbility(Player rider) {
        if (hitboxEntity == null) return;

        Location loc = hitboxEntity.getLocation().add(0, 1, 0);
        Vector dir = rider.getLocation().getDirection().normalize();

        // Ice breath projectile trail
        new BukkitRunnable() {
            int ticks = 0;
            Location currentLoc = loc.clone();
            @Override
            public void run() {
                if (ticks++ > 20) {
                    cancel();
                    return;
                }
                currentLoc.add(dir.clone().multiply(1.5));
                currentLoc.getWorld().spawnParticle(Particle.SNOWFLAKE, currentLoc,
                        8, 0.3, 0.3, 0.3, 0.02);
                currentLoc.getWorld().spawnParticle(Particle.BLOCK, currentLoc,
                        3, 0.2, 0.2, 0.2, 0, Material.ICE.createBlockData());

                for (Entity e : currentLoc.getWorld().getNearbyEntities(currentLoc, 1.5, 1.5, 1.5)) {
                    if (e instanceof LivingEntity le && e != hitboxEntity
                            && !(e instanceof Player p && isOwner(p))) {
                        le.damage(5, hitboxEntity);
                        le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 2));
                        le.getWorld().spawnParticle(Particle.SNOWFLAKE,
                                le.getLocation().add(0, 1, 0), 10, 0.3, 0.3, 0.3, 0.02);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);

        hitboxEntity.getWorld().playSound(loc, Sound.BLOCK_POWDER_SNOW_STEP, 2.0f, 0.5f);
        rider.sendMessage(Component.text("Ice Breath!", NamedTextColor.AQUA));
    }

    @Override
    protected void onDamage(double amount, Player source) {
        playAnimation("hurt", AnimationIterator.Type.PLAY_ONCE);
        hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                Sound.ENTITY_PHANTOM_HURT, 1.0f, 1.5f);
    }

    @Override
    protected void onDeath(Player killer) {
        if (hitboxEntity != null) {
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                    Sound.ENTITY_PHANTOM_DEATH, 1.0f, 1.5f);
            hitboxEntity.getWorld().spawnParticle(Particle.SNOWFLAKE,
                    hitboxEntity.getLocation().add(0, 1, 0), 30, 1, 1, 1, 0.05);
        }
        if (killer != null) {
            killer.giveExp(25);
        }
    }

    @Override
    protected List<ItemStack> getDrops(Player killer) {
        List<ItemStack> drops = new ArrayList<>();
        drops.add(new ItemStack(Material.ICE, 2 + (int) (Math.random() * 3)));
        drops.add(new ItemStack(Material.PRISMARINE_SHARD, 1 + (int) (Math.random() * 2)));
        return drops;
    }

    private boolean isWearingArmor(Player player) {
        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (armor != null && armor.getType() != Material.AIR) return true;
        }
        return false;
    }

    private Player getOwner() {
        if (ownerUUID == null) return null;
        return plugin.getServer().getPlayer(ownerUUID);
    }

    @Override
    protected long getTickRate() {
        return 5L;
    }
}

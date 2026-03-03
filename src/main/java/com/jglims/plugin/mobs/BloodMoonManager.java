package com.jglims.plugin.mobs;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.config.ConfigManager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class BloodMoonManager implements Listener {

    private final JGlimsPlugin plugin;
    private final ConfigManager config;

    private boolean bloodMoonActive = false;
    private int bloodMoonCount = 0;
    private boolean checkedThisNight = false;

    // Blood Moon King tracking
    private UUID bloodMoonKingUUID = null;

    public BloodMoonManager(JGlimsPlugin plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
    }

    /**
     * Call this to start the blood moon check scheduler.
     */
    public void startScheduler() {
        int interval = config.getBloodMoonCheckInterval();
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!config.isBloodMoonEnabled()) return;

                for (World world : Bukkit.getWorlds()) {
                    if (world.getEnvironment() != World.Environment.NORMAL) continue;

                    long time = world.getTime();

                    // Night = 13000-23000
                    boolean isNight = time >= 13000 && time <= 23000;

                    if (isNight && !checkedThisNight) {
                        checkedThisNight = true;

                        // Roll for blood moon
                        if (ThreadLocalRandom.current().nextDouble() < config.getBloodMoonChance()) {
                            startBloodMoon(world);
                        }
                    } else if (!isNight && checkedThisNight) {
                        checkedThisNight = false;
                        if (bloodMoonActive) {
                            endBloodMoon(world);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, interval);
    }

    private void startBloodMoon(World world) {
        bloodMoonActive = true;
        bloodMoonCount++;

        // Announce
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(Component.text("A Blood Moon is rising...", NamedTextColor.DARK_RED)
                .decoration(TextDecoration.BOLD, true));
            // Pale-garden-fog style darkness effect
            p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 200, 0, true, false, false));
        }

        plugin.getLogger().info("Blood Moon #" + bloodMoonCount + " has begun!");

        // Every 10th Blood Moon, spawn a Blood Moon King
        if (bloodMoonCount % config.getBloodMoonBossEveryNth() == 0) {
            spawnBloodMoonKing(world);
        }

        // Apply fog-like effect periodically during blood moon
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!bloodMoonActive) {
                    cancel();
                    return;
                }
                for (Player p : world.getPlayers()) {
                    // Red particles around player
                    p.getWorld().spawnParticle(Particle.DUST,
                        p.getLocation().add(0, 2, 0), 5,
                        3, 2, 3, 0,
                        new Particle.DustOptions(org.bukkit.Color.RED, 1.5f));
                    // Re-apply mild darkness
                    if (!p.hasPotionEffect(PotionEffectType.DARKNESS)) {
                        p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 80, 0, true, false, false));
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 60L);
    }

    private void endBloodMoon(World world) {
        bloodMoonActive = false;

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(Component.text("The Blood Moon has faded.", NamedTextColor.GRAY));
            p.removePotionEffect(PotionEffectType.DARKNESS);
        }

        // Clear Blood Moon King if still alive
        if (bloodMoonKingUUID != null) {
            Entity entity = Bukkit.getEntity(bloodMoonKingUUID);
            if (entity != null && !entity.isDead()) {
                entity.remove();
                plugin.getLogger().info("Blood Moon King despawned at dawn.");
            }
            bloodMoonKingUUID = null;
        }

        plugin.getLogger().info("Blood Moon #" + bloodMoonCount + " has ended.");
    }

    private void spawnBloodMoonKing(World world) {
        // Find a random online player in this world to spawn near
        List<Player> candidates = new ArrayList<>();
        for (Player p : world.getPlayers()) {
            if (p.isOnline() && !p.isDead()) candidates.add(p);
        }
        if (candidates.isEmpty()) return;

        Player target = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
        Location spawnLoc = target.getLocation().add(
            ThreadLocalRandom.current().nextInt(-20, 21),
            0,
            ThreadLocalRandom.current().nextInt(-20, 21)
        );
        // Find the highest block at that X/Z
        spawnLoc.setY(world.getHighestBlockYAt(spawnLoc) + 1);

        Zombie king = world.spawn(spawnLoc, Zombie.class, zombie -> {
            zombie.setAdult();
            zombie.setBaby(false);
            zombie.setShouldBurnInDay(false);

            // Custom name
            zombie.customName(Component.text("Blood Moon King", NamedTextColor.DARK_RED)
                .decoration(TextDecoration.BOLD, true));
            zombie.setCustomNameVisible(true);
            zombie.setGlowing(true);
            zombie.setRemoveWhenFarAway(false);

            // Apply boss stats
            AttributeInstance maxHealth = zombie.getAttribute(Attribute.MAX_HEALTH);
            if (maxHealth != null) {
                double health = 20.0 * config.getBloodMoonBossHealthMult();
                maxHealth.setBaseValue(health);
                zombie.setHealth(health);
            }

            AttributeInstance attackDamage = zombie.getAttribute(Attribute.ATTACK_DAMAGE);
            if (attackDamage != null) {
                attackDamage.setBaseValue(3.0 * config.getBloodMoonBossDamageMult());
            }

            // Give diamond armor
            zombie.getEquipment().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
            zombie.getEquipment().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
            zombie.getEquipment().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
            zombie.getEquipment().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
            zombie.getEquipment().setItemInMainHand(new ItemStack(Material.NETHERITE_SWORD));

            // Don't drop equipped items
            zombie.getEquipment().setHelmetDropChance(0f);
            zombie.getEquipment().setChestplateDropChance(0f);
            zombie.getEquipment().setLeggingsDropChance(0f);
            zombie.getEquipment().setBootsDropChance(0f);
            zombie.getEquipment().setItemInMainHandDropChance(0f);
        });

        bloodMoonKingUUID = king.getUniqueId();

        // Announce
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(Component.text("The Blood Moon King has risen!", NamedTextColor.DARK_RED)
                .decoration(TextDecoration.BOLD, true));
        }

        plugin.getLogger().info("Blood Moon King spawned at " +
            spawnLoc.getBlockX() + ", " + spawnLoc.getBlockY() + ", " + spawnLoc.getBlockZ());
    }

    public boolean isBloodMoonActive() {
        return bloodMoonActive;
    }

    public boolean isBloodMoonKing(Entity entity) {
        return entity != null && entity.getUniqueId().equals(bloodMoonKingUUID);
    }

    // ========================================================================
    // Boost mob stats during Blood Moon (NOT creepers)
    // ========================================================================
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!bloodMoonActive) return;
        if (!config.isBloodMoonEnabled()) return;

        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Monster)) return;

        // Don't boost creepers (user requested creeper exclusion)
        if (entity instanceof Creeper) return;

        // Don't boost the Blood Moon King itself
        if (entity.getUniqueId().equals(bloodMoonKingUUID)) return;

        // Only overworld
        if (entity.getWorld().getEnvironment() != World.Environment.NORMAL) return;

        AttributeInstance maxHealth = entity.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealth != null) {
            double newHealth = maxHealth.getBaseValue() * config.getBloodMoonMobHealthMult();
            maxHealth.setBaseValue(newHealth);
            entity.setHealth(newHealth);
        }

        AttributeInstance attackDamage = entity.getAttribute(Attribute.ATTACK_DAMAGE);
        if (attackDamage != null) {
            attackDamage.setBaseValue(attackDamage.getBaseValue() * config.getBloodMoonMobDamageMult());
        }
    }

    // ========================================================================
    // Blood Moon King death + double drops
    // ========================================================================
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();

        // Blood Moon King drops
        if (entity.getUniqueId().equals(bloodMoonKingUUID)) {
            int diamonds = ThreadLocalRandom.current().nextInt(
                config.getBloodMoonBossDiamondMin(), config.getBloodMoonBossDiamondMax() + 1);
            event.getDrops().clear(); // Remove equipped armor drops
            event.getDrops().add(new ItemStack(Material.DIAMOND, diamonds));
            event.getDrops().add(new ItemStack(Material.NETHERITE_INGOT, 1));
            event.setDroppedExp(500);

            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage(Component.text("The Blood Moon King has been slain!", NamedTextColor.GOLD)
                    .decoration(TextDecoration.BOLD, true));
            }

            bloodMoonKingUUID = null;
            plugin.getLogger().info("Blood Moon King has been slain!");
            return;
        }

        // Double drops during Blood Moon for all mobs
        if (bloodMoonActive && config.isBloodMoonDoubleDrops()) {
            if (entity instanceof Monster && entity.getWorld().getEnvironment() == World.Environment.NORMAL) {
                List<ItemStack> extraDrops = new ArrayList<>();
                for (ItemStack drop : event.getDrops()) {
                    if (drop != null) {
                        extraDrops.add(drop.clone());
                    }
                }
                event.getDrops().addAll(extraDrops);
                event.setDroppedExp(event.getDroppedExp() * 2);
            }
        }
    }
}

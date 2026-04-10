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
import com.jglims.plugin.legendary.InfinityStoneManager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * Blood Moon event manager.
 * Phase 22: Added Infinity Stone fragment drops (0.1% per mob kill during Blood Moon).
 */
public class BloodMoonManager implements Listener {

    private final JGlimsPlugin plugin;
    private final ConfigManager config;

    private boolean bloodMoonActive = false;
    private int bloodMoonCount = 0;
    private boolean checkedThisNight = false;

    private UUID bloodMoonKingUUID = null;

    public BloodMoonManager(JGlimsPlugin plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void startScheduler() {
        int interval = config.getBloodMoonCheckInterval();
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!config.isBloodMoonEnabled()) return;

                for (World world : Bukkit.getWorlds()) {
                    if (world.getEnvironment() != World.Environment.NORMAL) continue;

                    long time = world.getTime();
                    boolean isNight = time >= 13000 && time <= 23000;

                    if (isNight && !checkedThisNight) {
                        checkedThisNight = true;
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

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(Component.text("A Blood Moon is rising...", NamedTextColor.DARK_RED)
                .decoration(TextDecoration.BOLD, true));
            p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 200, 0, true, false, false));
        }

        plugin.getLogger().info("Blood Moon #" + bloodMoonCount + " has begun!");

        if (bloodMoonCount % config.getBloodMoonBossEveryNth() == 0) {
            spawnBloodMoonKing(world);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!bloodMoonActive) { cancel(); return; }
                for (Player p : world.getPlayers()) {
                    p.getWorld().spawnParticle(Particle.DUST,
                        p.getLocation().add(0, 2, 0), 5,
                        3, 2, 3, 0,
                        new Particle.DustOptions(org.bukkit.Color.RED, 1.5f));
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
        List<Player> candidates = new ArrayList<>();
        for (Player p : world.getPlayers()) {
            if (p.isOnline() && !p.isDead()) candidates.add(p);
        }
        if (candidates.isEmpty()) return;

        Player target = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
        Location spawnLoc = target.getLocation().add(
            ThreadLocalRandom.current().nextInt(-20, 21), 0,
            ThreadLocalRandom.current().nextInt(-20, 21));
        spawnLoc.setY(world.getHighestBlockYAt(spawnLoc) + 1);

        // Spawn Ripper Zombie custom mob instead of vanilla zombie boss
        com.jglims.plugin.custommobs.CustomMobManager mobManager = plugin.getCustomMobManager();
        if (mobManager != null) {
            com.jglims.plugin.custommobs.CustomMobEntity mob = mobManager.spawnMob(
                    com.jglims.plugin.custommobs.CustomMobType.RIPPER_ZOMBIE, spawnLoc);
            if (mob != null && mob.getHitboxEntity() != null) {
                bloodMoonKingUUID = mob.getHitboxEntity().getUniqueId();
            }
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(Component.text("The Ripper Zombie has risen!", NamedTextColor.DARK_RED)
                .decoration(TextDecoration.BOLD, true));
        }

        plugin.getLogger().info("Blood Moon Ripper Zombie spawned at " +
            spawnLoc.getBlockX() + ", " + spawnLoc.getBlockY() + ", " + spawnLoc.getBlockZ());
    }

    public boolean isBloodMoonActive() { return bloodMoonActive; }

    public boolean isBloodMoonKing(Entity entity) {
        return entity != null && entity.getUniqueId().equals(bloodMoonKingUUID);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!bloodMoonActive) return;
        if (!config.isBloodMoonEnabled()) return;

        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Monster)) return;
        if (entity instanceof Creeper) return;
        if (entity.getUniqueId().equals(bloodMoonKingUUID)) return;
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

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();

        // Blood Moon King drops
        if (entity.getUniqueId().equals(bloodMoonKingUUID)) {
            int diamonds = ThreadLocalRandom.current().nextInt(
                config.getBloodMoonBossDiamondMin(), config.getBloodMoonBossDiamondMax() + 1);
            event.getDrops().clear();
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

        // Infinity Stone fragments now drop exclusively from Aether/Lunar dimension chests.
        // Blood Moon stone drops have been removed per the Gauntlet rework.

        // Double drops during Blood Moon
        if (bloodMoonActive && config.isBloodMoonDoubleDrops()) {
            if (entity instanceof Monster && entity.getWorld().getEnvironment() == World.Environment.NORMAL) {
                List<ItemStack> extraDrops = new ArrayList<>();
                for (ItemStack drop : event.getDrops()) {
                    if (drop != null) extraDrops.add(drop.clone());
                }
                event.getDrops().addAll(extraDrops);
                event.setDroppedExp(event.getDroppedExp() * 2);
            }
        }
    }
}
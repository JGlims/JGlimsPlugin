package com.jglims.plugin.mobs;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.config.ConfigManager;
import com.jglims.plugin.legendary.LegendaryTier;
import com.jglims.plugin.legendary.LegendaryWeapon;
import com.jglims.plugin.legendary.LegendaryWeaponManager;
import com.jglims.plugin.powerups.PowerUpManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Biome;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Roaming Boss Manager v2.0 â€” 6 world-roaming bosses.
 *
 * The Watcher:         Deep Dark, 800 HP, EPIC drops
 * Hellfire Drake:      Nether Wastes/Soul Sand Valley, 600 HP, EPIC drops
 * Frostbound Colossus: Snowy biomes, 700 HP, EPIC drops
 * Jungle Predator:     Jungle biomes, 500 HP, RARE/EPIC drops
 * End Wraith:          The End, 900 HP, MYTHIC drops
 * Abyssal Leviathan:   Abyss dimension, 1200 HP, ABYSSAL drops
 */
public class RoamingBossManager implements Listener {

    private final JGlimsPlugin plugin;
    private final ConfigManager config;
    private final Random random = new Random();

    private final NamespacedKey KEY_ROAMING_BOSS;
    private final NamespacedKey KEY_ROAMING_TYPE;

    // Active boss tracking
    private UUID activeWatcherUUID = null;
    private UUID activeHellfireDrakeUUID = null;
    private UUID activeFrostColossusUUID = null;
    private UUID activeJunglePredatorUUID = null;
    private UUID activeEndWraithUUID = null;
    private UUID activeAbyssalLeviathanUUID = null;

    // Spawn chances per 2-minute check
    private static final double WATCHER_SPAWN_CHANCE = 0.03;
    private static final double HELLFIRE_DRAKE_SPAWN_CHANCE = 0.04;
    private static final double FROST_COLOSSUS_SPAWN_CHANCE = 0.03;
    private static final double JUNGLE_PREDATOR_SPAWN_CHANCE = 0.04;
    private static final double END_WRAITH_SPAWN_CHANCE = 0.02;
    private static final double ABYSSAL_LEVIATHAN_SPAWN_CHANCE = 0.03;

    // Boss HP
    private static final double WATCHER_HP = 800.0;
    private static final double HELLFIRE_DRAKE_HP = 600.0;
    private static final double FROST_COLOSSUS_HP = 700.0;
    private static final double JUNGLE_PREDATOR_HP = 500.0;
    private static final double END_WRAITH_HP = 900.0;
    private static final double ABYSSAL_LEVIATHAN_HP = 1200.0;

    private static final long DESPAWN_TICKS = 12000L;
    private final Map<UUID, Long> lastSpecialAttack = new HashMap<>();

    public RoamingBossManager(JGlimsPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
        this.KEY_ROAMING_BOSS = new NamespacedKey(plugin, "roaming_boss");
        this.KEY_ROAMING_TYPE = new NamespacedKey(plugin, "roaming_boss_type");
    }

    public void startScheduler() {
        new BukkitRunnable() {
            @Override
            public void run() {
                checkWatcherSpawn();
                checkHellfireDrakeSpawn();
                checkFrostColossusSpawn();
                checkJunglePredatorSpawn();
                checkEndWraithSpawn();
                checkAbyssalLeviathanSpawn();
            }
        }.runTaskTimer(plugin, 2400L, 2400L);

        plugin.getLogger().info("Roaming boss scheduler started (The Watcher, Hellfire Drake, Frostbound Colossus, Jungle Predator, End Wraith, Abyssal Leviathan).");
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // THE WATCHER â€” Deep Dark biome
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

    private void checkWatcherSpawn() {
        if (activeWatcherUUID != null) {
            Entity existing = plugin.getServer().getEntity(activeWatcherUUID);
            if (existing != null && !existing.isDead()) return;
            activeWatcherUUID = null;
        }
        for (World world : plugin.getServer().getWorlds()) {
            if (world.getEnvironment() != World.Environment.NORMAL) continue;
            for (Player player : world.getPlayers()) {
                Biome biome = world.getBiome(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ());
                if (biome != Biome.DEEP_DARK) continue;
                if (player.getLocation().getBlockY() > 0) continue;
                if (random.nextDouble() < WATCHER_SPAWN_CHANCE) { spawnWatcher(player); return; }
            }
        }
    }

    private void spawnWatcher(Player nearPlayer) {
        Location loc = nearPlayer.getLocation().add(random.nextInt(30) - 15, 0, random.nextInt(30) - 15);
        loc.setY(loc.getWorld().getHighestBlockYAt(loc.getBlockX(), loc.getBlockZ()));
        if (loc.getBlockY() > 0) loc.setY(-20);

        Warden watcher = loc.getWorld().spawn(loc, Warden.class);
        watcher.customName(Component.text("The Watcher", TextColor.color(40, 0, 60)).decorate(TextDecoration.BOLD));
        watcher.setCustomNameVisible(true);
        if (watcher.getAttribute(Attribute.MAX_HEALTH) != null) {
            watcher.getAttribute(Attribute.MAX_HEALTH).setBaseValue(WATCHER_HP);
            watcher.setHealth(WATCHER_HP);
        }
        watcher.setPersistent(true);
        watcher.setRemoveWhenFarAway(false);
        watcher.setGlowing(true);
        watcher.getPersistentDataContainer().set(KEY_ROAMING_BOSS, PersistentDataType.BYTE, (byte) 1);
        watcher.getPersistentDataContainer().set(KEY_ROAMING_TYPE, PersistentDataType.STRING, "WATCHER");
        activeWatcherUUID = watcher.getUniqueId();

        Component announcement = Component.text("\u26A0 ", NamedTextColor.DARK_RED)
            .append(Component.text("The Watcher", TextColor.color(40, 0, 60)).decorate(TextDecoration.BOLD))
            .append(Component.text(" has emerged from the deep dark...", NamedTextColor.DARK_GRAY));
        for (Player p : loc.getNearbyPlayers(80)) {
            p.sendMessage(announcement);
            p.playSound(p.getLocation(), Sound.ENTITY_WARDEN_EMERGE, 1.0f, 0.5f);
        }

        setupDespawnTimer(watcher, () -> activeWatcherUUID = null, "The Watcher");

        new BukkitRunnable() {
            @Override public void run() {
                if (watcher.isDead() || !watcher.isValid()) { cancel(); return; }
                for (Player p : watcher.getLocation().getNearbyPlayers(20)) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 100, 0, false, false));
                    p.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 60, 1, false, false));
                }
                watcher.getWorld().spawnParticle(Particle.SCULK_CHARGE_POP, watcher.getLocation().add(0, 1, 0), 15, 2, 1, 2, 0.02);
            }
        }.runTaskTimer(plugin, 40L, 40L);

        plugin.getLogger().info("The Watcher spawned near " + nearPlayer.getName() + " at " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // HELLFIRE DRAKE â€” Nether
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

    private void checkHellfireDrakeSpawn() {
        if (activeHellfireDrakeUUID != null) {
            Entity existing = plugin.getServer().getEntity(activeHellfireDrakeUUID);
            if (existing != null && !existing.isDead()) return;
            activeHellfireDrakeUUID = null;
        }
        for (World world : plugin.getServer().getWorlds()) {
            if (world.getEnvironment() != World.Environment.NETHER) continue;
            for (Player player : world.getPlayers()) {
                Biome biome = world.getBiome(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ());
                if (biome != Biome.NETHER_WASTES && biome != Biome.SOUL_SAND_VALLEY) continue;
                if (random.nextDouble() < HELLFIRE_DRAKE_SPAWN_CHANCE) { spawnHellfireDrake(player); return; }
            }
        }
    }

    private void spawnHellfireDrake(Player nearPlayer) {
        Location loc = nearPlayer.getLocation().add(random.nextInt(40) - 20, 5 + random.nextInt(10), random.nextInt(40) - 20);
        if (loc.getBlockY() > 120) loc.setY(100);
        if (loc.getBlockY() < 35) loc.setY(45);

        Ghast drake = loc.getWorld().spawn(loc, Ghast.class);
        drake.customName(Component.text("Hellfire Drake", TextColor.color(255, 80, 0)).decorate(TextDecoration.BOLD));
        drake.setCustomNameVisible(true);
        if (drake.getAttribute(Attribute.MAX_HEALTH) != null) {
            drake.getAttribute(Attribute.MAX_HEALTH).setBaseValue(HELLFIRE_DRAKE_HP);
            drake.setHealth(HELLFIRE_DRAKE_HP);
        }
        drake.setPersistent(true);
        drake.setRemoveWhenFarAway(false);
        drake.setGlowing(true);
        drake.getPersistentDataContainer().set(KEY_ROAMING_BOSS, PersistentDataType.BYTE, (byte) 1);
        drake.getPersistentDataContainer().set(KEY_ROAMING_TYPE, PersistentDataType.STRING, "HELLFIRE_DRAKE");
        activeHellfireDrakeUUID = drake.getUniqueId();

        Component announcement = Component.text("\u2622 ", NamedTextColor.RED)
            .append(Component.text("Hellfire Drake", TextColor.color(255, 80, 0)).decorate(TextDecoration.BOLD))
            .append(Component.text(" soars through the Nether!", NamedTextColor.GOLD));
        for (Player p : loc.getNearbyPlayers(100)) {
            p.sendMessage(announcement);
            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.8f);
        }

        setupDespawnTimer(drake, () -> activeHellfireDrakeUUID = null, "Hellfire Drake");

        new BukkitRunnable() {
            @Override public void run() {
                if (drake.isDead() || !drake.isValid()) { cancel(); return; }
                Location drakeLoc = drake.getLocation();
                drakeLoc.getWorld().spawnParticle(Particle.FLAME, drakeLoc.add(0, -2, 0), 30, 5, 3, 5, 0.05);
                drakeLoc.getWorld().spawnParticle(Particle.LAVA, drakeLoc, 10, 3, 1, 3);
                long now = System.currentTimeMillis();
                long lastAttack = lastSpecialAttack.getOrDefault(drake.getUniqueId(), 0L);
                if (now - lastAttack > 6000) {
                    Player target = findNearestPlayer(drakeLoc, 40);
                    if (target != null) {
                        fireTripleSalvo(drake, target);
                        lastSpecialAttack.put(drake.getUniqueId(), now);
                    }
                }
            }
        }.runTaskTimer(plugin, 60L, 60L);

        plugin.getLogger().info("Hellfire Drake spawned near " + nearPlayer.getName() + " at " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
    }

    private void fireTripleSalvo(Ghast drake, Player target) {
        Location origin = drake.getLocation();
        Vector direction = target.getLocation().toVector().subtract(origin.toVector()).normalize();
        for (int i = 0; i < 3; i++) {
            final int delay = i * 5;
            new BukkitRunnable() {
                @Override public void run() {
                    if (drake.isDead() || !drake.isValid()) return;
                    Vector offset = direction.clone().add(new Vector(
                        (random.nextDouble() - 0.5) * 0.3, (random.nextDouble() - 0.5) * 0.2, (random.nextDouble() - 0.5) * 0.3
                    )).normalize();
                    LargeFireball fireball = drake.getWorld().spawn(drake.getLocation().add(0, -1, 0), LargeFireball.class);
                    fireball.setDirection(offset.multiply(1.5));
                    fireball.setYield(2.0f);
                    fireball.setIsIncendiary(true);
                    fireball.setShooter(drake);
                    drake.getWorld().playSound(drake.getLocation(), Sound.ENTITY_GHAST_SHOOT, 1.2f, 0.6f);
                }
            }.runTaskLater(plugin, delay);
        }
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // FROSTBOUND COLOSSUS â€” Snowy Overworld biomes
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

    private void checkFrostColossusSpawn() {
        if (activeFrostColossusUUID != null) {
            Entity existing = plugin.getServer().getEntity(activeFrostColossusUUID);
            if (existing != null && !existing.isDead()) return;
            activeFrostColossusUUID = null;
        }
        for (World world : plugin.getServer().getWorlds()) {
            if (world.getEnvironment() != World.Environment.NORMAL) continue;
            for (Player player : world.getPlayers()) {
                Biome biome = world.getBiome(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ());
                if (biome != Biome.SNOWY_PLAINS && biome != Biome.ICE_SPIKES && biome != Biome.FROZEN_PEAKS
                    && biome != Biome.SNOWY_TAIGA && biome != Biome.FROZEN_RIVER) continue;
                if (random.nextDouble() < FROST_COLOSSUS_SPAWN_CHANCE) { spawnFrostColossus(player); return; }
            }
        }
    }

    private void spawnFrostColossus(Player nearPlayer) {
        Location loc = nearPlayer.getLocation().add(random.nextInt(30) - 15, 0, random.nextInt(30) - 15);
        loc.setY(loc.getWorld().getHighestBlockYAt(loc.getBlockX(), loc.getBlockZ()));

        IronGolem colossus = loc.getWorld().spawn(loc, IronGolem.class);
        colossus.customName(Component.text("Frostbound Colossus", TextColor.color(130, 200, 255)).decorate(TextDecoration.BOLD));
        colossus.setCustomNameVisible(true);
        if (colossus.getAttribute(Attribute.MAX_HEALTH) != null) {
            colossus.getAttribute(Attribute.MAX_HEALTH).setBaseValue(FROST_COLOSSUS_HP);
            colossus.setHealth(FROST_COLOSSUS_HP);
        }
        if (colossus.getAttribute(Attribute.MOVEMENT_SPEED) != null) {
            colossus.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.35);
        }
        colossus.setPersistent(true);
        colossus.setRemoveWhenFarAway(false);
        colossus.setGlowing(true);
        colossus.setPlayerCreated(false);
        colossus.getPersistentDataContainer().set(KEY_ROAMING_BOSS, PersistentDataType.BYTE, (byte) 1);
        colossus.getPersistentDataContainer().set(KEY_ROAMING_TYPE, PersistentDataType.STRING, "FROST_COLOSSUS");
        activeFrostColossusUUID = colossus.getUniqueId();

        Component announcement = Component.text("\u2744 ", TextColor.color(130, 200, 255))
            .append(Component.text("Frostbound Colossus", TextColor.color(130, 200, 255)).decorate(TextDecoration.BOLD))
            .append(Component.text(" rises from the frozen wastes!", NamedTextColor.AQUA));
        for (Player p : loc.getNearbyPlayers(80)) {
            p.sendMessage(announcement);
            p.playSound(p.getLocation(), Sound.ENTITY_IRON_GOLEM_HURT, 1.0f, 0.4f);
        }

        setupDespawnTimer(colossus, () -> activeFrostColossusUUID = null, "Frostbound Colossus");

        // Frost aura: Slowness + snowflake particles
        new BukkitRunnable() {
            @Override public void run() {
                if (colossus.isDead() || !colossus.isValid()) { cancel(); return; }
                for (Player p : colossus.getLocation().getNearbyPlayers(15)) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 1, false, false));
                    p.setFreezeTicks(Math.min(p.getFreezeTicks() + 40, 140));
                }
                colossus.getWorld().spawnParticle(Particle.SNOWFLAKE, colossus.getLocation().add(0, 1.5, 0), 25, 3, 2, 3, 0.02);
                // Ground pound AoE every ~8 seconds
                long now = System.currentTimeMillis();
                long lastAttack = lastSpecialAttack.getOrDefault(colossus.getUniqueId(), 0L);
                if (now - lastAttack > 8000) {
                    List<Player> nearby = new ArrayList<>(colossus.getLocation().getNearbyPlayers(10));
                    if (!nearby.isEmpty()) {
                        lastSpecialAttack.put(colossus.getUniqueId(), now);
                        Location bossLoc = colossus.getLocation();
                        bossLoc.getWorld().spawnParticle(Particle.BLOCK, bossLoc, 30, 4, 0.5, 4, 0.1, Material.PACKED_ICE.createBlockData());
                        bossLoc.getWorld().playSound(bossLoc, Sound.ENTITY_IRON_GOLEM_ATTACK, 1.5f, 0.3f);
                        for (Player p : nearby) {
                            Vector kb = p.getLocation().toVector().subtract(bossLoc.toVector()).normalize().multiply(1.8).setY(0.9);
                            p.setVelocity(kb);
                            p.damage(10.0, colossus);
                            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 2, false, false));
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 40L, 40L);

        plugin.getLogger().info("Frostbound Colossus spawned near " + nearPlayer.getName() + " at " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // JUNGLE PREDATOR â€” Jungle biomes
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

    private void checkJunglePredatorSpawn() {
        if (activeJunglePredatorUUID != null) {
            Entity existing = plugin.getServer().getEntity(activeJunglePredatorUUID);
            if (existing != null && !existing.isDead()) return;
            activeJunglePredatorUUID = null;
        }
        for (World world : plugin.getServer().getWorlds()) {
            if (world.getEnvironment() != World.Environment.NORMAL) continue;
            for (Player player : world.getPlayers()) {
                Biome biome = world.getBiome(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ());
                if (biome != Biome.JUNGLE && biome != Biome.BAMBOO_JUNGLE && biome != Biome.SPARSE_JUNGLE) continue;
                if (random.nextDouble() < JUNGLE_PREDATOR_SPAWN_CHANCE) { spawnJunglePredator(player); return; }
            }
        }
    }

    private void spawnJunglePredator(Player nearPlayer) {
        Location loc = nearPlayer.getLocation().add(random.nextInt(25) - 12, 0, random.nextInt(25) - 12);
        loc.setY(loc.getWorld().getHighestBlockYAt(loc.getBlockX(), loc.getBlockZ()));

        Ravager predator = loc.getWorld().spawn(loc, Ravager.class);
        predator.customName(Component.text("Jungle Predator", TextColor.color(50, 180, 50)).decorate(TextDecoration.BOLD));
        predator.setCustomNameVisible(true);
        if (predator.getAttribute(Attribute.MAX_HEALTH) != null) {
            predator.getAttribute(Attribute.MAX_HEALTH).setBaseValue(JUNGLE_PREDATOR_HP);
            predator.setHealth(JUNGLE_PREDATOR_HP);
        }
        if (predator.getAttribute(Attribute.MOVEMENT_SPEED) != null) {
            predator.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.4);
        }
        predator.setPersistent(true);
        predator.setRemoveWhenFarAway(false);
        predator.setGlowing(true);
        predator.getPersistentDataContainer().set(KEY_ROAMING_BOSS, PersistentDataType.BYTE, (byte) 1);
        predator.getPersistentDataContainer().set(KEY_ROAMING_TYPE, PersistentDataType.STRING, "JUNGLE_PREDATOR");
        activeJunglePredatorUUID = predator.getUniqueId();

        Component announcement = Component.text("\uD83C\uDF3F ", TextColor.color(50, 180, 50))
            .append(Component.text("Jungle Predator", TextColor.color(50, 180, 50)).decorate(TextDecoration.BOLD))
            .append(Component.text(" stalks through the undergrowth!", NamedTextColor.DARK_GREEN));
        for (Player p : loc.getNearbyPlayers(60)) {
            p.sendMessage(announcement);
            p.playSound(p.getLocation(), Sound.ENTITY_RAVAGER_ROAR, 1.0f, 0.7f);
        }

        setupDespawnTimer(predator, () -> activeJunglePredatorUUID = null, "Jungle Predator");

        // Stealth pounce + poison fog
        new BukkitRunnable() {
            private boolean stealthActive = false;
            private int tickCount = 0;
            @Override public void run() {
                if (predator.isDead() || !predator.isValid()) { cancel(); return; }
                tickCount++;
                Location bossLoc = predator.getLocation();
                // Ambient vine particles
                bossLoc.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, bossLoc.add(0, 1, 0), 5, 1, 0.5, 1);
                // Stealth pounce: every ~10 seconds, go invisible for 3 seconds then leap
                long now = System.currentTimeMillis();
                long lastAttack = lastSpecialAttack.getOrDefault(predator.getUniqueId(), 0L);
                if (!stealthActive && now - lastAttack > 10000) {
                    Player target = findNearestPlayer(bossLoc, 30);
                    if (target != null) {
                        stealthActive = true;
                        lastSpecialAttack.put(predator.getUniqueId(), now);
                        predator.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 60, 0, false, false));
                        predator.setGlowing(false);
                        bossLoc.getWorld().playSound(bossLoc, Sound.ENTITY_CAT_HISS, 1.0f, 0.5f);
                        // After 3 seconds, leap and deal damage
                        new BukkitRunnable() {
                            @Override public void run() {
                                if (predator.isDead() || !predator.isValid()) return;
                                stealthActive = false;
                                predator.setGlowing(true);
                                predator.removePotionEffect(PotionEffectType.INVISIBILITY);
                                Player nearest = findNearestPlayer(predator.getLocation(), 20);
                                if (nearest != null) {
                                    Vector leap = nearest.getLocation().toVector().subtract(predator.getLocation().toVector()).normalize().multiply(2.0).setY(0.6);
                                    predator.setVelocity(leap);
                                    predator.getWorld().playSound(predator.getLocation(), Sound.ENTITY_RAVAGER_ATTACK, 1.2f, 0.8f);
                                    // Damage after short delay
                                    new BukkitRunnable() {
                                        @Override public void run() {
                                            if (predator.isDead() || !predator.isValid()) return;
                                            for (Player p : predator.getLocation().getNearbyPlayers(4)) {
                                                p.damage(12.0, predator);
                                                p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 1, false, false));
                                            }
                                            predator.getWorld().spawnParticle(Particle.ITEM_SLIME, predator.getLocation(), 30, 2, 1, 2);
                                        }
                                    }.runTaskLater(plugin, 10L);
                                }
                            }
                        }.runTaskLater(plugin, 60L);
                    }
                }
                // Poison fog every ~5 seconds
                if (tickCount % 5 == 0) {
                    bossLoc.getWorld().spawnParticle(Particle.ITEM_SLIME, bossLoc, 20, 4, 1, 4, 0.03);
                    for (Player p : bossLoc.getNearbyPlayers(8)) {
                        p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 60, 0, false, false));
                    }
                }
                if (predator.isDead() || !predator.isValid()) { cancel(); return; }
            }
        }.runTaskTimer(plugin, 40L, 40L);

        plugin.getLogger().info("Jungle Predator spawned near " + nearPlayer.getName() + " at " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // END WRAITH â€” The End
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

    private void checkEndWraithSpawn() {
        if (activeEndWraithUUID != null) {
            Entity existing = plugin.getServer().getEntity(activeEndWraithUUID);
            if (existing != null && !existing.isDead()) return;
            activeEndWraithUUID = null;
        }
        for (World world : plugin.getServer().getWorlds()) {
            if (world.getEnvironment() != World.Environment.THE_END) continue;
            if (world.getName().equalsIgnoreCase("world_abyss")) continue;
            for (Player player : world.getPlayers()) {
                // Must be on outer islands (far from 0,0)
                if (Math.abs(player.getLocation().getBlockX()) < 500 && Math.abs(player.getLocation().getBlockZ()) < 500) continue;
                if (random.nextDouble() < END_WRAITH_SPAWN_CHANCE) { spawnEndWraith(player); return; }
            }
        }
    }

    private void spawnEndWraith(Player nearPlayer) {
        Location loc = nearPlayer.getLocation().add(random.nextInt(30) - 15, 8 + random.nextInt(10), random.nextInt(30) - 15);

        Phantom wraith = loc.getWorld().spawn(loc, Phantom.class);
        wraith.setSize(6);
        wraith.customName(Component.text("End Wraith", TextColor.color(180, 0, 220)).decorate(TextDecoration.BOLD));
        wraith.setCustomNameVisible(true);
        if (wraith.getAttribute(Attribute.MAX_HEALTH) != null) {
            wraith.getAttribute(Attribute.MAX_HEALTH).setBaseValue(END_WRAITH_HP);
            wraith.setHealth(END_WRAITH_HP);
        }
        wraith.setPersistent(true);
        wraith.setRemoveWhenFarAway(false);
        wraith.setGlowing(true);
        wraith.getPersistentDataContainer().set(KEY_ROAMING_BOSS, PersistentDataType.BYTE, (byte) 1);
        wraith.getPersistentDataContainer().set(KEY_ROAMING_TYPE, PersistentDataType.STRING, "END_WRAITH");
        activeEndWraithUUID = wraith.getUniqueId();

        Component announcement = Component.text("\u2620 ", TextColor.color(180, 0, 220))
            .append(Component.text("End Wraith", TextColor.color(180, 0, 220)).decorate(TextDecoration.BOLD))
            .append(Component.text(" materializes from the void!", NamedTextColor.DARK_PURPLE));
        for (Player p : loc.getNearbyPlayers(100)) {
            p.sendMessage(announcement);
            p.playSound(p.getLocation(), Sound.ENTITY_PHANTOM_AMBIENT, 1.0f, 0.3f);
        }

        setupDespawnTimer(wraith, () -> activeEndWraithUUID = null, "End Wraith");

        // Void beam + phase shift
        new BukkitRunnable() {
            @Override public void run() {
                if (wraith.isDead() || !wraith.isValid()) { cancel(); return; }
                Location bossLoc = wraith.getLocation();
                bossLoc.getWorld().spawnParticle(Particle.REVERSE_PORTAL, bossLoc, 15, 2, 1, 2, 0.05);
                long now = System.currentTimeMillis();
                long lastAttack = lastSpecialAttack.getOrDefault(wraith.getUniqueId(), 0L);
                if (now - lastAttack > 7000) {
                    Player target = findNearestPlayer(bossLoc, 50);
                    if (target != null) {
                        lastSpecialAttack.put(wraith.getUniqueId(), now);
                        // Phase shift â€” teleport behind the target
                        Location behind = target.getLocation().add(target.getLocation().getDirection().multiply(-3).setY(3));
                        wraith.teleport(behind);
                        bossLoc.getWorld().spawnParticle(Particle.REVERSE_PORTAL, bossLoc, 30, 1, 1, 1, 0.1);
                        wraith.getWorld().spawnParticle(Particle.REVERSE_PORTAL, behind, 30, 1, 1, 1, 0.1);
                        wraith.getWorld().playSound(behind, Sound.ENTITY_ENDERMAN_TELEPORT, 1.5f, 0.5f);
                        // Void beam after teleport
                        new BukkitRunnable() {
                            @Override public void run() {
                                if (wraith.isDead() || !wraith.isValid()) return;
                                for (Player p : wraith.getLocation().getNearbyPlayers(12)) {
                                    p.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 40, 1, false, false));
                                    p.damage(14.0, wraith);
                                    p.getWorld().spawnParticle(Particle.DRAGON_BREATH, p.getLocation(), 20, 0.5, 1, 0.5, 0.02);
                                }
                                wraith.getWorld().playSound(wraith.getLocation(), Sound.ENTITY_PHANTOM_BITE, 1.5f, 0.4f);
                            }
                        }.runTaskLater(plugin, 15L);
                    }
                }
            }
        }.runTaskTimer(plugin, 40L, 40L);

        // Shadow clones: spawn 3 weaker phantoms at 50% HP
        new BukkitRunnable() {
            private boolean clonesSpawned = false;
            @Override public void run() {
                if (wraith.isDead() || !wraith.isValid()) { cancel(); return; }
                if (!clonesSpawned && wraith.getHealth() < END_WRAITH_HP * 0.5) {
                    clonesSpawned = true;
                    Component cloneMsg = Component.text("The ", NamedTextColor.DARK_PURPLE)
                        .append(Component.text("End Wraith", TextColor.color(180, 0, 220)).decorate(TextDecoration.BOLD))
                        .append(Component.text(" splits into shadow clones!", NamedTextColor.DARK_PURPLE));
                    for (Player p : wraith.getLocation().getNearbyPlayers(60)) p.sendMessage(cloneMsg);
                    for (int i = 0; i < 3; i++) {
                        Location cloneLoc = wraith.getLocation().add(random.nextInt(10) - 5, random.nextInt(5), random.nextInt(10) - 5);
                        Phantom clone = cloneLoc.getWorld().spawn(cloneLoc, Phantom.class);
                        clone.setSize(3);
                        clone.customName(Component.text("Shadow Clone", TextColor.color(120, 0, 160)));
                        clone.setCustomNameVisible(true);
                        if (clone.getAttribute(Attribute.MAX_HEALTH) != null) {
                            clone.getAttribute(Attribute.MAX_HEALTH).setBaseValue(100.0);
                            clone.setHealth(100.0);
                        }
                        clone.setPersistent(false);
                        clone.getPersistentDataContainer().set(KEY_ROAMING_BOSS, PersistentDataType.BYTE, (byte) 1);
                        clone.getPersistentDataContainer().set(KEY_ROAMING_TYPE, PersistentDataType.STRING, "END_WRAITH_CLONE");
                    }
                    wraith.getWorld().spawnParticle(Particle.REVERSE_PORTAL, wraith.getLocation(), 30, 3, 2, 3, 0.1);
                    wraith.getWorld().playSound(wraith.getLocation(), Sound.ENTITY_PHANTOM_FLAP, 2.0f, 0.3f);
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);

        plugin.getLogger().info("End Wraith spawned near " + nearPlayer.getName() + " at " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // ABYSSAL LEVIATHAN â€” Abyss dimension
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

    private void checkAbyssalLeviathanSpawn() {
        if (activeAbyssalLeviathanUUID != null) {
            Entity existing = plugin.getServer().getEntity(activeAbyssalLeviathanUUID);
            if (existing != null && !existing.isDead()) return;
            activeAbyssalLeviathanUUID = null;
        }
        for (World world : plugin.getServer().getWorlds()) {
            if (!world.getName().equalsIgnoreCase("world_abyss")) continue;
            for (Player player : world.getPlayers()) {
                if (random.nextDouble() < ABYSSAL_LEVIATHAN_SPAWN_CHANCE) { spawnAbyssalLeviathan(player); return; }
            }
        }
    }

    private void spawnAbyssalLeviathan(Player nearPlayer) {
        Location loc = nearPlayer.getLocation().add(random.nextInt(30) - 15, 0, random.nextInt(30) - 15);
        loc.setY(loc.getWorld().getHighestBlockYAt(loc.getBlockX(), loc.getBlockZ()) + 1);

        ElderGuardian leviathan = loc.getWorld().spawn(loc, ElderGuardian.class);
        leviathan.customName(Component.text("Abyssal Leviathan", TextColor.color(170, 0, 0)).decorate(TextDecoration.BOLD));
        leviathan.setCustomNameVisible(true);
        if (leviathan.getAttribute(Attribute.MAX_HEALTH) != null) {
            leviathan.getAttribute(Attribute.MAX_HEALTH).setBaseValue(ABYSSAL_LEVIATHAN_HP);
            leviathan.setHealth(ABYSSAL_LEVIATHAN_HP);
        }
        leviathan.setPersistent(true);
        leviathan.setRemoveWhenFarAway(false);
        leviathan.setGlowing(true);
        leviathan.getPersistentDataContainer().set(KEY_ROAMING_BOSS, PersistentDataType.BYTE, (byte) 1);
        leviathan.getPersistentDataContainer().set(KEY_ROAMING_TYPE, PersistentDataType.STRING, "ABYSSAL_LEVIATHAN");
        activeAbyssalLeviathanUUID = leviathan.getUniqueId();

        Component announcement = Component.text("\u2620 ", TextColor.color(170, 0, 0))
            .append(Component.text("Abyssal Leviathan", TextColor.color(170, 0, 0)).decorate(TextDecoration.BOLD))
            .append(Component.text(" awakens from the abyss!", TextColor.color(100, 0, 0)));
        for (Player p : loc.getNearbyPlayers(120)) {
            p.sendMessage(announcement);
            p.playSound(p.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 1.0f, 0.3f);
        }

        setupDespawnTimer(leviathan, () -> activeAbyssalLeviathanUUID = null, "Abyssal Leviathan");

        // Mining fatigue aura + tentacle slam AoE + rage mode
        new BukkitRunnable() {
            private boolean rageMode = false;
            @Override public void run() {
                if (leviathan.isDead() || !leviathan.isValid()) { cancel(); return; }
                Location bossLoc = leviathan.getLocation();
                // Mining fatigue aura
                for (Player p : bossLoc.getNearbyPlayers(25)) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 100, 2, false, false));
                }
                bossLoc.getWorld().spawnParticle(Particle.BUBBLE_POP, bossLoc.add(0, 1, 0), 20, 3, 2, 3, 0.02);
                // Rage mode at 30% HP
                if (!rageMode && leviathan.getHealth() < ABYSSAL_LEVIATHAN_HP * 0.3) {
                    rageMode = true;
                    leviathan.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, false));
                    leviathan.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 1, false, false));
                    Component rageMsg = Component.text("The ", TextColor.color(100, 0, 0))
                        .append(Component.text("Abyssal Leviathan", TextColor.color(170, 0, 0)).decorate(TextDecoration.BOLD))
                        .append(Component.text(" enters a frenzy!", TextColor.color(100, 0, 0)));
                    for (Player p : bossLoc.getNearbyPlayers(80)) {
                        p.sendMessage(rageMsg);
                        p.playSound(p.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_HURT, 2.0f, 0.2f);
                    }
                }
                // Tentacle slam every ~6s
                long now = System.currentTimeMillis();
                long lastAttack = lastSpecialAttack.getOrDefault(leviathan.getUniqueId(), 0L);
                long slamCooldown = rageMode ? 4000 : 6000;
                if (now - lastAttack > slamCooldown) {
                    List<Player> nearby = new ArrayList<>(bossLoc.getNearbyPlayers(12));
                    if (!nearby.isEmpty()) {
                        lastSpecialAttack.put(leviathan.getUniqueId(), now);
                        double damage = rageMode ? 18.0 : 12.0;
                        bossLoc.getWorld().playSound(bossLoc, Sound.ENTITY_ELDER_GUARDIAN_HURT, 1.5f, 0.4f);
                        bossLoc.getWorld().spawnParticle(Particle.EXPLOSION, bossLoc, 3, 2, 1, 2);
                        for (Player p : nearby) {
                            Vector kb = p.getLocation().toVector().subtract(bossLoc.toVector()).normalize().multiply(2.2).setY(1.0);
                            p.setVelocity(kb);
                            p.damage(damage, leviathan);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 40L, 40L);

        plugin.getLogger().info("Abyssal Leviathan spawned near " + nearPlayer.getName() + " at " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // EVENT HANDLERS
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

    @EventHandler
    public void onRoamingBossDamaged(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (!entity.getPersistentDataContainer().has(KEY_ROAMING_BOSS, PersistentDataType.BYTE)) return;
        String type = entity.getPersistentDataContainer().get(KEY_ROAMING_TYPE, PersistentDataType.STRING);
        if (type == null) return;

        if ("WATCHER".equals(type) && event.getDamager() instanceof Player player) {
            long now = System.currentTimeMillis();
            long lastAttack = lastSpecialAttack.getOrDefault(entity.getUniqueId(), 0L);
            if (now - lastAttack > 4000) {
                lastSpecialAttack.put(entity.getUniqueId(), now);
                Location bossLoc = entity.getLocation().add(0, 1, 0);
                Vector knockback = player.getLocation().toVector().subtract(bossLoc.toVector()).normalize().multiply(2.5).setY(0.8);
                player.setVelocity(knockback);
                player.damage(8.0, entity);
                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0));
                bossLoc.getWorld().spawnParticle(Particle.SONIC_BOOM, bossLoc, 1, 0, 0, 0);
                bossLoc.getWorld().playSound(bossLoc, Sound.ENTITY_WARDEN_SONIC_BOOM, 1.0f, 0.8f);
            }
        }
    }

    @EventHandler
    public void onRoamingBossDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (!entity.getPersistentDataContainer().has(KEY_ROAMING_BOSS, PersistentDataType.BYTE)) return;
        String type = entity.getPersistentDataContainer().get(KEY_ROAMING_TYPE, PersistentDataType.STRING);
        if (type == null) return;

        Location loc = entity.getLocation();
        PowerUpManager pum = plugin.getPowerUpManager();
        LegendaryWeaponManager wm = plugin.getLegendaryWeaponManager();

        switch (type) {
            case "WATCHER" -> { activeWatcherUUID = null; handleWatcherDeath(loc, event, pum, wm); }
            case "HELLFIRE_DRAKE" -> { activeHellfireDrakeUUID = null; handleHellfireDrakeDeath(loc, event, pum, wm); }
            case "FROST_COLOSSUS" -> { activeFrostColossusUUID = null; handleFrostColossusDeath(loc, event, pum, wm); }
            case "JUNGLE_PREDATOR" -> { activeJunglePredatorUUID = null; handleJunglePredatorDeath(loc, event, pum, wm); }
            case "END_WRAITH" -> { activeEndWraithUUID = null; handleEndWraithDeath(loc, event, pum, wm); }
            case "ABYSSAL_LEVIATHAN" -> { activeAbyssalLeviathanUUID = null; handleAbyssalLeviathanDeath(loc, event, pum, wm); }
            case "END_WRAITH_CLONE" -> {
                // Clones drop minor loot
                event.getDrops().add(new ItemStack(Material.ENDER_PEARL, 2 + random.nextInt(3)));
                loc.getWorld().spawnParticle(Particle.REVERSE_PORTAL, loc, 30, 1, 1, 1, 0.05);
            }
        }
        lastSpecialAttack.remove(entity.getUniqueId());
    }

    // â”€â”€ Death loot handlers â”€â”€

    private void handleWatcherDeath(Location loc, EntityDeathEvent event, PowerUpManager pum, LegendaryWeaponManager wm) {
        if (random.nextDouble() < 0.30) loc.getWorld().dropItemNaturally(loc, pum.createHeartCrystal());
        int souls = 3 + random.nextInt(3);
        for (int i = 0; i < souls; i++) loc.getWorld().dropItemNaturally(loc, pum.createSoulFragment());
        if (random.nextDouble() < 0.15) loc.getWorld().dropItemNaturally(loc, pum.createTitanResolve());
        dropWeaponFromTier(loc, wm, LegendaryTier.EPIC, 0.40, "The Watcher");
        event.getDrops().add(new ItemStack(Material.DIAMOND, 8 + random.nextInt(8)));
        event.getDrops().add(new ItemStack(Material.SCULK_CATALYST, 2 + random.nextInt(3)));
        event.getDrops().add(new ItemStack(Material.ECHO_SHARD, 4 + random.nextInt(4)));
        deathVFX(loc, Particle.SCULK_CHARGE_POP, Sound.ENTITY_WARDEN_DEATH, "The Watcher", TextColor.color(40, 0, 60), 80);
    }

    private void handleHellfireDrakeDeath(Location loc, EntityDeathEvent event, PowerUpManager pum, LegendaryWeaponManager wm) {
        if (random.nextDouble() < 0.20) loc.getWorld().dropItemNaturally(loc, pum.createHeartCrystal());
        int souls = 2 + random.nextInt(3);
        for (int i = 0; i < souls; i++) loc.getWorld().dropItemNaturally(loc, pum.createSoulFragment());
        dropWeaponFromTier(loc, wm, LegendaryTier.EPIC, 0.35, "Hellfire Drake");
        event.getDrops().add(new ItemStack(Material.DIAMOND, 6 + random.nextInt(6)));
        event.getDrops().add(new ItemStack(Material.NETHERITE_SCRAP, 2 + random.nextInt(3)));
        event.getDrops().add(new ItemStack(Material.BLAZE_ROD, 8 + random.nextInt(8)));
        event.getDrops().add(new ItemStack(Material.GHAST_TEAR, 3 + random.nextInt(4)));
        deathVFX(loc, Particle.FLAME, Sound.ENTITY_ENDER_DRAGON_DEATH, "Hellfire Drake", TextColor.color(255, 80, 0), 60);
    }

    private void handleFrostColossusDeath(Location loc, EntityDeathEvent event, PowerUpManager pum, LegendaryWeaponManager wm) {
        if (random.nextDouble() < 0.25) loc.getWorld().dropItemNaturally(loc, pum.createHeartCrystal());
        int souls = 3 + random.nextInt(3);
        for (int i = 0; i < souls; i++) loc.getWorld().dropItemNaturally(loc, pum.createSoulFragment());
        if (random.nextDouble() < 0.12) loc.getWorld().dropItemNaturally(loc, pum.createTitanResolve());
        dropWeaponFromTier(loc, wm, LegendaryTier.EPIC, 0.35, "Frostbound Colossus");
        event.getDrops().add(new ItemStack(Material.DIAMOND, 7 + random.nextInt(7)));
        event.getDrops().add(new ItemStack(Material.PACKED_ICE, 16 + random.nextInt(16)));
        event.getDrops().add(new ItemStack(Material.BLUE_ICE, 4 + random.nextInt(8)));
        event.getDrops().add(new ItemStack(Material.IRON_BLOCK, 3 + random.nextInt(4)));
        deathVFX(loc, Particle.SNOWFLAKE, Sound.ENTITY_IRON_GOLEM_DEATH, "Frostbound Colossus", TextColor.color(130, 200, 255), 80);
    }

    private void handleJunglePredatorDeath(Location loc, EntityDeathEvent event, PowerUpManager pum, LegendaryWeaponManager wm) {
        if (random.nextDouble() < 0.20) loc.getWorld().dropItemNaturally(loc, pum.createHeartCrystal());
        int souls = 2 + random.nextInt(3);
        for (int i = 0; i < souls; i++) loc.getWorld().dropItemNaturally(loc, pum.createSoulFragment());
        // Mixed RARE/EPIC drops
        if (random.nextDouble() < 0.25) {
            dropWeaponFromTier(loc, wm, LegendaryTier.EPIC, 1.0, "Jungle Predator");
        } else if (random.nextDouble() < 0.40) {
            dropWeaponFromTier(loc, wm, LegendaryTier.RARE, 1.0, "Jungle Predator");
        }
        event.getDrops().add(new ItemStack(Material.DIAMOND, 5 + random.nextInt(5)));
        event.getDrops().add(new ItemStack(Material.EMERALD, 8 + random.nextInt(12)));
        event.getDrops().add(new ItemStack(Material.BONE, 6 + random.nextInt(8)));
        event.getDrops().add(new ItemStack(Material.LEATHER, 4 + random.nextInt(6)));
        deathVFX(loc, Particle.HAPPY_VILLAGER, Sound.ENTITY_RAVAGER_DEATH, "Jungle Predator", TextColor.color(50, 180, 50), 50);
    }

    private void handleEndWraithDeath(Location loc, EntityDeathEvent event, PowerUpManager pum, LegendaryWeaponManager wm) {
        if (random.nextDouble() < 0.35) loc.getWorld().dropItemNaturally(loc, pum.createHeartCrystal());
        int souls = 4 + random.nextInt(4);
        for (int i = 0; i < souls; i++) loc.getWorld().dropItemNaturally(loc, pum.createSoulFragment());
        if (random.nextDouble() < 0.20) loc.getWorld().dropItemNaturally(loc, pum.createPhoenixFeather());
        dropWeaponFromTier(loc, wm, LegendaryTier.MYTHIC, 0.30, "End Wraith");
        event.getDrops().add(new ItemStack(Material.DIAMOND, 10 + random.nextInt(10)));
        event.getDrops().add(new ItemStack(Material.ENDER_PEARL, 8 + random.nextInt(8)));
        event.getDrops().add(new ItemStack(Material.END_CRYSTAL, 1 + random.nextInt(2)));
        event.getDrops().add(new ItemStack(Material.SHULKER_SHELL, 2 + random.nextInt(4)));
        deathVFX(loc, Particle.REVERSE_PORTAL, Sound.ENTITY_PHANTOM_DEATH, "End Wraith", TextColor.color(180, 0, 220), 100);
    }

    private void handleAbyssalLeviathanDeath(Location loc, EntityDeathEvent event, PowerUpManager pum, LegendaryWeaponManager wm) {
        loc.getWorld().dropItemNaturally(loc, pum.createHeartCrystal());
        int souls = 5 + random.nextInt(5);
        for (int i = 0; i < souls; i++) loc.getWorld().dropItemNaturally(loc, pum.createSoulFragment());
        if (random.nextDouble() < 0.25) loc.getWorld().dropItemNaturally(loc, pum.createTitanResolve());
        if (random.nextDouble() < 0.30) loc.getWorld().dropItemNaturally(loc, pum.createPhoenixFeather());
        dropWeaponFromTier(loc, wm, LegendaryTier.ABYSSAL, 0.15, "Abyssal Leviathan");
        if (random.nextDouble() < 0.40) dropWeaponFromTier(loc, wm, LegendaryTier.MYTHIC, 1.0, "Abyssal Leviathan");
        event.getDrops().add(new ItemStack(Material.DIAMOND, 15 + random.nextInt(10)));
        event.getDrops().add(new ItemStack(Material.NETHERITE_INGOT, 1 + random.nextInt(2)));
        event.getDrops().add(new ItemStack(Material.PRISMARINE_SHARD, 16 + random.nextInt(16)));
        event.getDrops().add(new ItemStack(Material.HEART_OF_THE_SEA, 1));
        deathVFX(loc, Particle.EXPLOSION, Sound.ENTITY_ELDER_GUARDIAN_DEATH, "Abyssal Leviathan", TextColor.color(170, 0, 0), 120);
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // UTILITIES
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

    private void setupDespawnTimer(LivingEntity boss, Runnable onDespawn, String name) {
        new BukkitRunnable() {
            @Override public void run() {
                if (!boss.isDead() && boss.isValid()) {
                    boss.remove();
                    onDespawn.run();
                    plugin.getLogger().info(name + " despawned (timeout).");
                }
            }
        }.runTaskLater(plugin, DESPAWN_TICKS);
    }

    private void dropWeaponFromTier(Location loc, LegendaryWeaponManager wm, LegendaryTier tier, double chance, String bossName) {
        LegendaryWeapon[] pool = LegendaryWeapon.byTier(tier);
        if (pool.length > 0 && random.nextDouble() < chance) {
            LegendaryWeapon weapon = pool[random.nextInt(pool.length)];
            ItemStack weaponItem = wm.createWeapon(weapon);
            if (weaponItem != null) {
                loc.getWorld().dropItemNaturally(loc, weaponItem);
                announceWeaponDrop(loc, bossName, weapon);
            }
        }
    }

    private void deathVFX(Location loc, Particle particle, Sound sound, String name, TextColor color, int particleCount) {
        loc.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, loc.add(0, 1, 0), particleCount, 2, 2, 2);
        loc.getWorld().spawnParticle(particle, loc, particleCount / 2, 3, 2, 3, 0.05);
        loc.getWorld().playSound(loc, sound, 1.0f, 0.5f);
        Component deathMsg = Component.text("\u2620 ", NamedTextColor.DARK_RED)
            .append(Component.text(name, color).decorate(TextDecoration.BOLD))
            .append(Component.text(" has been vanquished!", NamedTextColor.GRAY));
        for (Player p : loc.getNearbyPlayers(100)) {
            p.sendMessage(deathMsg);
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        }
        plugin.getLogger().info(name + " was defeated.");
    }

    private Player findNearestPlayer(Location loc, double range) {
        Player nearest = null;
        double minDist = range * range;
        for (Player p : loc.getNearbyPlayers(range)) {
            double dist = p.getLocation().distanceSquared(loc);
            if (dist < minDist) { minDist = dist; nearest = p; }
        }
        return nearest;
    }

    private void announceWeaponDrop(Location loc, String bossName, LegendaryWeapon weapon) {
        Component msg = Component.text("\u2694 ", NamedTextColor.GOLD)
            .append(Component.text(bossName, weapon.getTier().getColor()).decorate(TextDecoration.BOLD))
            .append(Component.text(" dropped ", NamedTextColor.GRAY))
            .append(Component.text(weapon.getDisplayName(), weapon.getTier().getColor()).decorate(TextDecoration.BOLD))
            .append(Component.text("!", NamedTextColor.GRAY));
        for (Player p : loc.getNearbyPlayers(60)) {
            p.sendMessage(msg);
            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.2f);
        }
    }

    // â”€â”€ Getters â”€â”€
    public boolean isWatcherAlive() { return activeWatcherUUID != null; }
    public boolean isHellfireDrakeAlive() { return activeHellfireDrakeUUID != null; }
    public boolean isFrostColossusAlive() { return activeFrostColossusUUID != null; }
    public boolean isJunglePredatorAlive() { return activeJunglePredatorUUID != null; }
    public boolean isEndWraithAlive() { return activeEndWraithUUID != null; }
    public boolean isAbyssalLeviathanAlive() { return activeAbyssalLeviathanUUID != null; }
    public NamespacedKey getKeyRoamingBoss() { return KEY_ROAMING_BOSS; }
}
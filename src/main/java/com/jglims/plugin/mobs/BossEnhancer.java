package com.jglims.plugin.mobs;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.config.ConfigManager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * BossEnhancer v2.0 — Phase 11: Enhanced Boss Mechanics (Section N).
 * Applies HP/damage multipliers AND adds new combat phases:
 *   - Ender Dragon: Enderman wave summons, lightning breath, enrage at 20%
 *   - Wither: Wither Storm phase at 50% HP (larger model, more skulls)
 *   - Elder Guardian: Water prison, guardian minion summons, mining fatigue burst
 *   - Warden: Stays unchanged per README request
 * Uses Adventure API (rule A.6), NamespacedKey(plugin, key) (rule A.9).
 */
public class BossEnhancer implements Listener {

    private final JGlimsPlugin plugin;
    private final ConfigManager config;

    // Track boss phase states
    private final Set<UUID> dragonEnraged = new HashSet<>();
    private final Set<UUID> witherStormActive = new HashSet<>();
    private final Set<UUID> elderPrisonActive = new HashSet<>();
    private final Map<UUID, BukkitRunnable> activeTasks = new HashMap<>();

    // PDC keys
    private final NamespacedKey KEY_DAMAGE_MULT;
    private final NamespacedKey KEY_ENHANCED;

    public BossEnhancer(JGlimsPlugin plugin, ConfigManager config, Listener... extra) {
        this.plugin = plugin;
        this.config = config;
        this.KEY_DAMAGE_MULT = new NamespacedKey(plugin, "boss_damage_mult");
        this.KEY_ENHANCED = new NamespacedKey(plugin, "boss_enhanced");
    }

    // ── SPAWN ENHANCEMENT ──

    @EventHandler(priority = EventPriority.HIGH)
    public void onBossSpawn(CreatureSpawnEvent event) {
        LivingEntity entity = event.getEntity();

        double healthMult = 1.0;
        double damageMult = 1.0;

        if (entity instanceof EnderDragon) {
            healthMult = config.getEnderDragonHealthMult();
            damageMult = config.getEnderDragonDamageMult();
        } else if (entity instanceof Wither) {
            healthMult = config.getWitherHealthMult();
            damageMult = config.getWitherDamageMult();
        } else if (entity instanceof Warden) {
            healthMult = config.getWardenHealthMult();
            damageMult = config.getWardenDamageMult();
        } else if (entity instanceof ElderGuardian) {
            healthMult = config.getElderGuardianHealthMult();
            damageMult = config.getElderGuardianDamageMult();
        } else {
            return;
        }

        // Apply HP multiplier
        if (healthMult != 1.0) {
            AttributeInstance healthAttr = entity.getAttribute(Attribute.MAX_HEALTH);
            if (healthAttr != null) {
                double newMaxHealth = healthAttr.getBaseValue() * healthMult;
                healthAttr.setBaseValue(newMaxHealth);
                entity.setHealth(newMaxHealth);
            }
        }

        // Store damage multiplier in PDC
        if (damageMult != 1.0) {
            entity.getPersistentDataContainer().set(KEY_DAMAGE_MULT, PersistentDataType.DOUBLE, damageMult);
        }

        // Mark as enhanced and start phase monitoring
        entity.getPersistentDataContainer().set(KEY_ENHANCED, PersistentDataType.BYTE, (byte) 1);

        if (entity instanceof EnderDragon dragon) {
            startDragonPhaseMonitor(dragon);
        } else if (entity instanceof Wither wither) {
            startWitherPhaseMonitor(wither);
        } else if (entity instanceof ElderGuardian elder) {
            startElderPhaseMonitor(elder);
        }
    }

    // ── DAMAGE MULTIPLIER ──

    @EventHandler(priority = EventPriority.HIGH)
    public void onBossDamage(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        if (!(damager instanceof LivingEntity livingDamager)) return;

        if (livingDamager.getPersistentDataContainer().has(KEY_DAMAGE_MULT, PersistentDataType.DOUBLE)) {
            double mult = livingDamager.getPersistentDataContainer().get(KEY_DAMAGE_MULT, PersistentDataType.DOUBLE);
            event.setDamage(event.getDamage() * mult);
        }
    }

    // ══════════════════════════════════════════════
    // ENDER DRAGON: Enderman waves, lightning, enrage
    // ══════════════════════════════════════════════

    private void startDragonPhaseMonitor(EnderDragon dragon) {
        UUID uid = dragon.getUniqueId();
        BukkitRunnable task = new BukkitRunnable() {
            private int waveTick = 0;
            private int lightningTick = 0;

            @Override
            public void run() {
                if (dragon.isDead() || !dragon.isValid()) {
                    dragonEnraged.remove(uid);
                    activeTasks.remove(uid);
                    cancel();
                    return;
                }

                AttributeInstance maxHP = dragon.getAttribute(Attribute.MAX_HEALTH);
                if (maxHP == null) return;
                double hpPercent = dragon.getHealth() / maxHP.getBaseValue();

                // Summon Enderman wave every 30 seconds (~600 ticks at 20/s, check runs every 40 ticks)
                waveTick++;
                if (waveTick >= 15) { // 15 * 40 ticks = 600 ticks = 30s
                    waveTick = 0;
                    summonDragonEndermanWave(dragon);
                }

                // Lightning breath every 15 seconds during combat
                lightningTick++;
                if (lightningTick >= 8) { // 8 * 40 = 320 ticks ≈ 16s
                    lightningTick = 0;
                    dragonLightningBreath(dragon);
                }

                // Enrage at 20% HP
                if (hpPercent <= 0.20 && !dragonEnraged.contains(uid)) {
                    dragonEnraged.add(uid);
                    triggerDragonEnrage(dragon);
                }
            }
        };
        task.runTaskTimer(plugin, 200L, 40L); // Start after 10s, check every 2s
        activeTasks.put(uid, task);
    }

    private void summonDragonEndermanWave(EnderDragon dragon) {
        World world = dragon.getWorld();
        Location center = dragon.getLocation();
        List<Player> nearby = new ArrayList<>(center.getNearbyPlayers(80));
        if (nearby.isEmpty()) return;

        int count = 3 + nearby.size(); // 3 base + 1 per player
        Random rand = new Random();

        for (Player p : nearby) {
            p.sendActionBar(Component.text("The Dragon summons Endermen!", NamedTextColor.DARK_PURPLE)
                    .decorate(TextDecoration.BOLD));
        }

        for (int i = 0; i < count; i++) {
            Player target = nearby.get(rand.nextInt(nearby.size()));
            Location spawnLoc = target.getLocation().add(
                    rand.nextDouble(-8, 8), 0, rand.nextDouble(-8, 8));
            spawnLoc.setY(world.getHighestBlockYAt(spawnLoc.getBlockX(), spawnLoc.getBlockZ()) + 1);

            Enderman enderman = world.spawn(spawnLoc, Enderman.class);
            AttributeInstance hp = enderman.getAttribute(Attribute.MAX_HEALTH);
            if (hp != null) { hp.setBaseValue(80); enderman.setHealth(80); }
            enderman.setTarget(target);
            enderman.setGlowing(true);
            enderman.customName(Component.text("Dragon's Servant", NamedTextColor.DARK_PURPLE));
            enderman.setCustomNameVisible(true);
        }

        world.spawnParticle(Particle.PORTAL, center, 100, 5, 3, 5, 0.5);
        world.playSound(center, Sound.ENTITY_ENDERMAN_SCREAM, 2.0f, 0.5f);
    }

    private void dragonLightningBreath(EnderDragon dragon) {
        World world = dragon.getWorld();
        List<Player> nearby = new ArrayList<>(dragon.getLocation().getNearbyPlayers(60));
        if (nearby.isEmpty()) return;

        Random rand = new Random();
        int strikes = 2 + rand.nextInt(3); // 2-4 lightning strikes
        for (int i = 0; i < strikes && i < nearby.size(); i++) {
            Player target = nearby.get(rand.nextInt(nearby.size()));
            Location strikeLoc = target.getLocation().add(rand.nextDouble(-3, 3), 0, rand.nextDouble(-3, 3));
            world.strikeLightning(strikeLoc);
        }
    }

    private void triggerDragonEnrage(EnderDragon dragon) {
        World world = dragon.getWorld();

        // Broadcast enrage
        for (Player p : world.getPlayers()) {
            p.showTitle(net.kyori.adventure.title.Title.title(
                    Component.text("THE DRAGON ENRAGES!", TextColor.color(170, 0, 0)).decorate(TextDecoration.BOLD),
                    Component.text("Its attacks grow more ferocious!", NamedTextColor.RED),
                    net.kyori.adventure.title.Title.Times.times(
                            java.time.Duration.ofMillis(300),
                            java.time.Duration.ofSeconds(2),
                            java.time.Duration.ofMillis(500))));
            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0f, 0.3f);
        }

        // Increase damage multiplier by 2x during enrage
        Double currentMult = dragon.getPersistentDataContainer().get(KEY_DAMAGE_MULT, PersistentDataType.DOUBLE);
        double newMult = (currentMult != null ? currentMult : 1.0) * 2.0;
        dragon.getPersistentDataContainer().set(KEY_DAMAGE_MULT, PersistentDataType.DOUBLE, newMult);

        // Summon a large Enderman wave
        summonDragonEndermanWave(dragon);
        summonDragonEndermanWave(dragon); // Double wave on enrage

        // Visual effects
        world.spawnParticle(Particle.DRAGON_BREATH, dragon.getLocation(), 200, 10, 5, 10, 0.1);
        world.playSound(dragon.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 3.0f, 0.2f);

        plugin.getLogger().info("Ender Dragon enraged at 20% HP!");
    }

    // ══════════════════════════════════════════════
    // WITHER: Wither Storm phase at 50% HP
    // ══════════════════════════════════════════════

    private void startWitherPhaseMonitor(Wither wither) {
        UUID uid = wither.getUniqueId();
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (wither.isDead() || !wither.isValid()) {
                    witherStormActive.remove(uid);
                    activeTasks.remove(uid);
                    cancel();
                    return;
                }

                AttributeInstance maxHP = wither.getAttribute(Attribute.MAX_HEALTH);
                if (maxHP == null) return;
                double hpPercent = wither.getHealth() / maxHP.getBaseValue();

                // Wither Storm at 50%
                if (hpPercent <= 0.50 && !witherStormActive.contains(uid)) {
                    witherStormActive.add(uid);
                    triggerWitherStorm(wither);
                }

                // During Wither Storm: extra skull barrage every 10 seconds
                if (witherStormActive.contains(uid)) {
                    witherStormSkullBarrage(wither);
                }
            }
        };
        task.runTaskTimer(plugin, 200L, 200L); // Every 10 seconds
        activeTasks.put(uid, task);
    }

    private void triggerWitherStorm(Wither wither) {
        World world = wither.getWorld();

        for (Player p : world.getPlayers()) {
            p.showTitle(net.kyori.adventure.title.Title.title(
                    Component.text("WITHER STORM", TextColor.color(50, 50, 50)).decorate(TextDecoration.BOLD),
                    Component.text("The Wither grows more powerful!", NamedTextColor.DARK_GRAY),
                    net.kyori.adventure.title.Title.Times.times(
                            java.time.Duration.ofMillis(300),
                            java.time.Duration.ofSeconds(3),
                            java.time.Duration.ofMillis(500))));
            p.playSound(p.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 2.0f, 0.3f);
            // Apply wither + darkness effect to all nearby players
            p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 100, 0, false, false));
        }

        // Increase size visually via name + glowing
        wither.customName(Component.text("Wither Storm", TextColor.color(80, 0, 80)).decorate(TextDecoration.BOLD));
        wither.setCustomNameVisible(true);
        wither.setGlowing(true);

        // Boost damage further
        Double currentMult = wither.getPersistentDataContainer().get(KEY_DAMAGE_MULT, PersistentDataType.DOUBLE);
        double newMult = (currentMult != null ? currentMult : 1.0) * 1.5;
        wither.getPersistentDataContainer().set(KEY_DAMAGE_MULT, PersistentDataType.DOUBLE, newMult);

        // Spawn Wither Skeleton guards
        Location loc = wither.getLocation();
        Random rand = new Random();
        for (int i = 0; i < 4; i++) {
            Location spawnLoc = loc.clone().add(rand.nextDouble(-5, 5), 0, rand.nextDouble(-5, 5));
            spawnLoc.setY(world.getHighestBlockYAt(spawnLoc.getBlockX(), spawnLoc.getBlockZ()) + 1);
            WitherSkeleton ws = world.spawn(spawnLoc, WitherSkeleton.class);
            AttributeInstance hp = ws.getAttribute(Attribute.MAX_HEALTH);
            if (hp != null) { hp.setBaseValue(60); ws.setHealth(60); }
            ws.customName(Component.text("Storm Guard", NamedTextColor.DARK_GRAY));
            ws.setCustomNameVisible(true);
            ws.setGlowing(true);
        }

        world.spawnParticle(Particle.SMOKE, loc, 200, 5, 5, 5, 0.1);
        world.playSound(loc, Sound.ENTITY_WITHER_SPAWN, 2.0f, 0.5f);

        plugin.getLogger().info("Wither Storm phase activated at 50% HP!");
    }

    private void witherStormSkullBarrage(Wither wither) {
        World world = wither.getWorld();
        List<Player> nearby = new ArrayList<>(wither.getLocation().getNearbyPlayers(40));
        if (nearby.isEmpty()) return;

        Random rand = new Random();
        // Extra dangerous wither skulls — apply wither effect to nearby players
        for (Player p : nearby) {
            if (rand.nextDouble() < 0.3) { // 30% chance per player per tick
                p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 60, 1, false, true));
                world.spawnParticle(Particle.SMOKE, p.getLocation().add(0, 1, 0), 15, 0.3, 0.5, 0.3, 0.02);
            }
        }
    }

    // ══════════════════════════════════════════════
    // ELDER GUARDIAN: Water prison, minion summons, fatigue burst
    // ══════════════════════════════════════════════

    private void startElderPhaseMonitor(ElderGuardian elder) {
        UUID uid = elder.getUniqueId();
        BukkitRunnable task = new BukkitRunnable() {
            private int tick = 0;

            @Override
            public void run() {
                if (elder.isDead() || !elder.isValid()) {
                    elderPrisonActive.remove(uid);
                    activeTasks.remove(uid);
                    cancel();
                    return;
                }

                tick++;

                // Water prison every 20 seconds
                if (tick % 4 == 0) { // 4 * 5s = 20s (task runs every 100 ticks = 5s)
                    elderWaterPrison(elder);
                }

                // Summon guardian minions every 30 seconds
                if (tick % 6 == 0) {
                    elderSummonMinions(elder);
                }

                // Mining fatigue burst every 15 seconds
                if (tick % 3 == 0) {
                    elderFatigueBurst(elder);
                }
            }
        };
        task.runTaskTimer(plugin, 200L, 100L); // Every 5 seconds
        activeTasks.put(uid, task);
    }

    private void elderWaterPrison(ElderGuardian elder) {
        World world = elder.getWorld();
        List<Player> nearby = new ArrayList<>(elder.getLocation().getNearbyPlayers(30));
        if (nearby.isEmpty()) return;

        Random rand = new Random();
        Player target = nearby.get(rand.nextInt(nearby.size()));

        // "Water prison" — slowness + levitation lock + bubble particles
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 3, false, true));
        target.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 80, 2, false, true));
        target.setVelocity(new Vector(0, 0, 0)); // Stop movement

        target.sendActionBar(Component.text("Water Prison! You're trapped!", NamedTextColor.DARK_AQUA)
                .decorate(TextDecoration.BOLD));

        // Bubble particle cage
        Location loc = target.getLocation();
        for (int i = 0; i < 4; i++) {
            final int delay = i * 10;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (target.isOnline()) {
                    world.spawnParticle(Particle.BUBBLE, loc.clone().add(0, 1, 0), 30, 1, 1.5, 1, 0.05);
                    world.spawnParticle(Particle.DRIPPING_WATER, loc.clone().add(0, 2, 0), 20, 0.5, 0.5, 0.5, 0);
                }
            }, delay);
        }

        world.playSound(loc, Sound.BLOCK_BUBBLE_COLUMN_WHIRLPOOL_INSIDE, 2.0f, 0.5f);
    }

    private void elderSummonMinions(ElderGuardian elder) {
        World world = elder.getWorld();
        Location loc = elder.getLocation();
        Random rand = new Random();

        int count = 2 + rand.nextInt(3); // 2-4 guardians
        for (int i = 0; i < count; i++) {
            Location spawnLoc = loc.clone().add(rand.nextDouble(-6, 6), rand.nextDouble(-2, 2), rand.nextDouble(-6, 6));
            Guardian guardian = world.spawn(spawnLoc, Guardian.class);
            AttributeInstance hp = guardian.getAttribute(Attribute.MAX_HEALTH);
            if (hp != null) { hp.setBaseValue(40); guardian.setHealth(40); }
            guardian.customName(Component.text("Guardian Minion", NamedTextColor.DARK_AQUA));
            guardian.setCustomNameVisible(true);

            // Target nearest player
            List<Player> nearby = new ArrayList<>(loc.getNearbyPlayers(30));
            if (!nearby.isEmpty()) {
                guardian.setTarget(nearby.get(rand.nextInt(nearby.size())));
            }
        }

        world.spawnParticle(Particle.BUBBLE_POP, loc, 40, 3, 2, 3, 0.05);
        world.playSound(loc, Sound.ENTITY_ELDER_GUARDIAN_CURSE, 1.5f, 0.8f);
    }

    private void elderFatigueBurst(ElderGuardian elder) {
        // Apply heavy mining fatigue to all players within 30 blocks
        for (Player p : elder.getLocation().getNearbyPlayers(30)) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 200, 2, false, true));
            p.getWorld().spawnParticle(Particle.ELDER_GUARDIAN,
                    p.getLocation().add(0, 1, 0), 1, 0, 0, 0, 0);
        }
    }
}
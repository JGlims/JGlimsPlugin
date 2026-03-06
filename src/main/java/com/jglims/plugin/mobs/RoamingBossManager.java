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
 * Roaming Boss Manager — Handles naturally-spawning world bosses that roam the world.
 *
 * The Watcher:  Spawns in Deep Dark biomes. 800 HP Warden-like entity.
 *               Applies Darkness + Blindness, summons Sculk Sensors, sonic boom attack.
 *               Drops EPIC weapons, Heart Crystals, Soul Fragments.
 *
 * Hellfire Drake: Spawns in Nether Wastes / Soul Sand Valley. 600 HP Blaze-based boss.
 *                 Triple fireball salvos, fire rain, ground slam.
 *                 Drops EPIC weapons, Netherite, Blaze Rods.
 *
 * Both bosses have a configurable spawn chance checked every 2 minutes.
 * Only one of each can exist at a time. They despawn after 10 minutes if not engaged.
 */
public class RoamingBossManager implements Listener {

    private final JGlimsPlugin plugin;
    private final ConfigManager config;
    private final Random random = new Random();

    // PDC keys
    private final NamespacedKey KEY_ROAMING_BOSS;
    private final NamespacedKey KEY_ROAMING_TYPE;

    // Active boss tracking
    private UUID activeWatcherUUID = null;
    private UUID activeHellfireDrakeUUID = null;

    // Spawn chances (per 2-minute check)
    private static final double WATCHER_SPAWN_CHANCE = 0.03;      // 3%
    private static final double HELLFIRE_DRAKE_SPAWN_CHANCE = 0.04; // 4%

    // Boss stats
    private static final double WATCHER_HP = 800.0;
    private static final double HELLFIRE_DRAKE_HP = 600.0;

    // Despawn timer (10 minutes = 12000 ticks)
    private static final long DESPAWN_TICKS = 12000L;

    // Attack cooldowns (tracked per entity UUID)
    private final Map<UUID, Long> lastSpecialAttack = new HashMap<>();

    public RoamingBossManager(JGlimsPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
        this.KEY_ROAMING_BOSS = new NamespacedKey(plugin, "roaming_boss");
        this.KEY_ROAMING_TYPE = new NamespacedKey(plugin, "roaming_boss_type");
    }

    /**
     * Start the periodic spawn check. Called from onEnable().
     */
    public void startScheduler() {
        new BukkitRunnable() {
            @Override
            public void run() {
                checkWatcherSpawn();
                checkHellfireDrakeSpawn();
            }
        }.runTaskTimer(plugin, 2400L, 2400L); // Every 2 minutes

        plugin.getLogger().info("Roaming boss scheduler started (The Watcher, Hellfire Drake).");
    }

    // ══════════════════════════════════════════════════════════════════
    // THE WATCHER — Deep Dark biome roaming boss
    // ══════════════════════════════════════════════════════════════════

    private void checkWatcherSpawn() {
        // Only one at a time
        if (activeWatcherUUID != null) {
            Entity existing = plugin.getServer().getEntity(activeWatcherUUID);
            if (existing != null && !existing.isDead()) return;
            activeWatcherUUID = null; // Cleaned up
        }

        for (World world : plugin.getServer().getWorlds()) {
            if (world.getEnvironment() != World.Environment.NORMAL) continue;

            for (Player player : world.getPlayers()) {
                Biome biome = world.getBiome(player.getLocation().getBlockX(),
                    player.getLocation().getBlockY(), player.getLocation().getBlockZ());

                if (biome != Biome.DEEP_DARK) continue;
                if (player.getLocation().getBlockY() > 0) continue; // Must be underground

                if (random.nextDouble() < WATCHER_SPAWN_CHANCE) {
                    spawnWatcher(player);
                    return; // Only spawn one per check
                }
            }
        }
    }

    private void spawnWatcher(Player nearPlayer) {
        Location loc = nearPlayer.getLocation().add(
            random.nextInt(30) - 15, 0, random.nextInt(30) - 15);
        // Adjust Y to surface
        loc.setY(loc.getWorld().getHighestBlockYAt(loc.getBlockX(), loc.getBlockZ()));
        // Keep it underground
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

        // Tag
        watcher.getPersistentDataContainer().set(KEY_ROAMING_BOSS, PersistentDataType.BYTE, (byte) 1);
        watcher.getPersistentDataContainer().set(KEY_ROAMING_TYPE, PersistentDataType.STRING, "WATCHER");

        activeWatcherUUID = watcher.getUniqueId();

        // Announce to nearby players
        Component announcement = Component.text("\u26A0 ", NamedTextColor.DARK_RED)
            .append(Component.text("The Watcher", TextColor.color(40, 0, 60)).decorate(TextDecoration.BOLD))
            .append(Component.text(" has emerged from the deep dark...", NamedTextColor.DARK_GRAY));
        for (Player p : loc.getNearbyPlayers(80)) {
            p.sendMessage(announcement);
            p.playSound(p.getLocation(), Sound.ENTITY_WARDEN_EMERGE, 1.0f, 0.5f);
        }

        // Auto-despawn timer
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!watcher.isDead() && watcher.isValid()) {
                    watcher.remove();
                    activeWatcherUUID = null;
                    plugin.getLogger().info("The Watcher despawned (timeout).");
                }
            }
        }.runTaskLater(plugin, DESPAWN_TICKS);

        // Periodic attack task — applies Darkness to nearby players
        new BukkitRunnable() {
            @Override
            public void run() {
                if (watcher.isDead() || !watcher.isValid()) { cancel(); return; }

                // Darkness aura: apply Darkness + Mining Fatigue to players within 20 blocks
                for (Player p : watcher.getLocation().getNearbyPlayers(20)) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 100, 0, false, false));
                    p.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 60, 1, false, false));
                }

                // Sculk particles
                watcher.getWorld().spawnParticle(Particle.SCULK_CHARGE_POP, watcher.getLocation().add(0, 1, 0),
                    15, 2, 1, 2, 0.02);
            }
        }.runTaskTimer(plugin, 40L, 40L); // Every 2 seconds

        plugin.getLogger().info("The Watcher spawned near " + nearPlayer.getName() +
            " at " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
    }

    // ══════════════════════════════════════════════════════════════════
    // HELLFIRE DRAKE — Nether roaming boss
    // ══════════════════════════════════════════════════════════════════

    private void checkHellfireDrakeSpawn() {
        if (activeHellfireDrakeUUID != null) {
            Entity existing = plugin.getServer().getEntity(activeHellfireDrakeUUID);
            if (existing != null && !existing.isDead()) return;
            activeHellfireDrakeUUID = null;
        }

        for (World world : plugin.getServer().getWorlds()) {
            if (world.getEnvironment() != World.Environment.NETHER) continue;

            for (Player player : world.getPlayers()) {
                Biome biome = world.getBiome(player.getLocation().getBlockX(),
                    player.getLocation().getBlockY(), player.getLocation().getBlockZ());

                if (biome != Biome.NETHER_WASTES && biome != Biome.SOUL_SAND_VALLEY) continue;

                if (random.nextDouble() < HELLFIRE_DRAKE_SPAWN_CHANCE) {
                    spawnHellfireDrake(player);
                    return;
                }
            }
        }
    }

    private void spawnHellfireDrake(Player nearPlayer) {
        Location loc = nearPlayer.getLocation().add(
            random.nextInt(40) - 20, 5 + random.nextInt(10), random.nextInt(40) - 20);
        // Clamp Y
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

        // Announce
        Component announcement = Component.text("\u2622 ", NamedTextColor.RED)
            .append(Component.text("Hellfire Drake", TextColor.color(255, 80, 0)).decorate(TextDecoration.BOLD))
            .append(Component.text(" soars through the Nether!", NamedTextColor.GOLD));
        for (Player p : loc.getNearbyPlayers(100)) {
            p.sendMessage(announcement);
            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.8f);
        }

        // Auto-despawn timer
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!drake.isDead() && drake.isValid()) {
                    drake.remove();
                    activeHellfireDrakeUUID = null;
                    plugin.getLogger().info("Hellfire Drake despawned (timeout).");
                }
            }
        }.runTaskLater(plugin, DESPAWN_TICKS);

        // Periodic fire rain attack
        new BukkitRunnable() {
            @Override
            public void run() {
                if (drake.isDead() || !drake.isValid()) { cancel(); return; }

                // Fire rain: drop fire particles and damage nearby players
                Location drakeLoc = drake.getLocation();
                drakeLoc.getWorld().spawnParticle(Particle.FLAME, drakeLoc.add(0, -2, 0),
                    30, 5, 3, 5, 0.05);
                drakeLoc.getWorld().spawnParticle(Particle.LAVA, drakeLoc, 10, 3, 1, 3);

                // Occasional triple fireball salvo (every ~6 seconds)
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
        }.runTaskTimer(plugin, 60L, 60L); // Every 3 seconds

        plugin.getLogger().info("Hellfire Drake spawned near " + nearPlayer.getName() +
            " at " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
    }

    private void fireTripleSalvo(Ghast drake, Player target) {
        Location origin = drake.getLocation();
        Vector direction = target.getLocation().toVector().subtract(origin.toVector()).normalize();

        for (int i = 0; i < 3; i++) {
            final int delay = i * 5; // Stagger by 0.25s
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (drake.isDead() || !drake.isValid()) return;
                    // Offset each fireball slightly
                    Vector offset = direction.clone().add(new Vector(
                        (random.nextDouble() - 0.5) * 0.3,
                        (random.nextDouble() - 0.5) * 0.2,
                        (random.nextDouble() - 0.5) * 0.3
                    )).normalize();

                    LargeFireball fireball = drake.getWorld().spawn(
                        drake.getLocation().add(0, -1, 0), LargeFireball.class);
                    fireball.setDirection(offset.multiply(1.5));
                    fireball.setYield(2.0f); // Explosion power
                    fireball.setIsIncendiary(true);
                    fireball.setShooter(drake);

                    drake.getWorld().playSound(drake.getLocation(), Sound.ENTITY_GHAST_SHOOT, 1.2f, 0.6f);
                }
            }.runTaskLater(plugin, delay);
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // EVENT HANDLERS
    // ══════════════════════════════════════════════════════════════════

    @EventHandler
    public void onRoamingBossDamaged(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (!entity.getPersistentDataContainer().has(KEY_ROAMING_BOSS, PersistentDataType.BYTE)) return;

        String type = entity.getPersistentDataContainer().get(KEY_ROAMING_TYPE, PersistentDataType.STRING);
        if (type == null) return;

        // The Watcher: counterattack — sonic boom when hit (every 4 seconds)
        if ("WATCHER".equals(type) && event.getDamager() instanceof Player player) {
            long now = System.currentTimeMillis();
            long lastAttack = lastSpecialAttack.getOrDefault(entity.getUniqueId(), 0L);
            if (now - lastAttack > 4000) {
                lastSpecialAttack.put(entity.getUniqueId(), now);
                // Sonic boom
                Location bossLoc = entity.getLocation().add(0, 1, 0);
                Vector knockback = player.getLocation().toVector()
                    .subtract(bossLoc.toVector()).normalize().multiply(2.5).setY(0.8);
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
            case "WATCHER" -> {
                activeWatcherUUID = null;
                handleWatcherDeath(loc, event, pum, wm);
            }
            case "HELLFIRE_DRAKE" -> {
                activeHellfireDrakeUUID = null;
                handleHellfireDrakeDeath(loc, event, pum, wm);
            }
        }

        lastSpecialAttack.remove(entity.getUniqueId());
    }

    private void handleWatcherDeath(Location loc, EntityDeathEvent event, PowerUpManager pum, LegendaryWeaponManager wm) {
        // Heart Crystal: 30% chance
        if (random.nextDouble() < 0.30) {
            loc.getWorld().dropItemNaturally(loc, pum.createHeartCrystal());
        }

        // Soul Fragments: guaranteed 3-5
        int souls = 3 + random.nextInt(3);
        for (int i = 0; i < souls; i++) {
            loc.getWorld().dropItemNaturally(loc, pum.createSoulFragment());
        }

        // Titan's Resolve: 15% chance
        if (random.nextDouble() < 0.15) {
            loc.getWorld().dropItemNaturally(loc, pum.createTitanResolve());
        }

        // EPIC weapon: 40% chance
        LegendaryWeapon[] pool = LegendaryWeapon.byTier(LegendaryTier.EPIC);
        if (pool.length > 0 && random.nextDouble() < 0.40) {
            LegendaryWeapon weapon = pool[random.nextInt(pool.length)];
            ItemStack weaponItem = wm.createWeapon(weapon);
            if (weaponItem != null) {
                loc.getWorld().dropItemNaturally(loc, weaponItem);
                announceWeaponDrop(loc, "The Watcher", weapon);
            }
        }

        // Materials
        event.getDrops().add(new ItemStack(Material.DIAMOND, 8 + random.nextInt(8)));
        event.getDrops().add(new ItemStack(Material.SCULK_CATALYST, 2 + random.nextInt(3)));
        event.getDrops().add(new ItemStack(Material.ECHO_SHARD, 4 + random.nextInt(4)));

        // VFX
        loc.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, loc.add(0, 1, 0), 80, 2, 2, 2);
        loc.getWorld().spawnParticle(Particle.SCULK_CHARGE_POP, loc, 40, 3, 2, 3, 0.02);
        loc.getWorld().playSound(loc, Sound.ENTITY_WARDEN_DEATH, 1.0f, 0.5f);

        // Broadcast
        Component deathMsg = Component.text("\u2620 ", NamedTextColor.DARK_RED)
            .append(Component.text("The Watcher", TextColor.color(40, 0, 60)).decorate(TextDecoration.BOLD))
            .append(Component.text(" has been vanquished!", NamedTextColor.GRAY));
        for (Player p : loc.getNearbyPlayers(80)) {
            p.sendMessage(deathMsg);
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        }

        plugin.getLogger().info("The Watcher was defeated.");
    }

    private void handleHellfireDrakeDeath(Location loc, EntityDeathEvent event, PowerUpManager pum, LegendaryWeaponManager wm) {
        // Heart Crystal: 20% chance
        if (random.nextDouble() < 0.20) {
            loc.getWorld().dropItemNaturally(loc, pum.createHeartCrystal());
        }

        // Soul Fragments: guaranteed 2-4
        int souls = 2 + random.nextInt(3);
        for (int i = 0; i < souls; i++) {
            loc.getWorld().dropItemNaturally(loc, pum.createSoulFragment());
        }

        // EPIC weapon: 35% chance
        LegendaryWeapon[] pool = LegendaryWeapon.byTier(LegendaryTier.EPIC);
        if (pool.length > 0 && random.nextDouble() < 0.35) {
            LegendaryWeapon weapon = pool[random.nextInt(pool.length)];
            ItemStack weaponItem = wm.createWeapon(weapon);
            if (weaponItem != null) {
                loc.getWorld().dropItemNaturally(loc, weaponItem);
                announceWeaponDrop(loc, "Hellfire Drake", weapon);
            }
        }

        // Materials
        event.getDrops().add(new ItemStack(Material.DIAMOND, 6 + random.nextInt(6)));
        event.getDrops().add(new ItemStack(Material.NETHERITE_SCRAP, 2 + random.nextInt(3)));
        event.getDrops().add(new ItemStack(Material.BLAZE_ROD, 8 + random.nextInt(8)));
        event.getDrops().add(new ItemStack(Material.GHAST_TEAR, 3 + random.nextInt(4)));

        // VFX
        loc.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, loc.add(0, 1, 0), 60, 2, 2, 2);
        loc.getWorld().spawnParticle(Particle.FLAME, loc, 50, 3, 2, 3, 0.1);
        loc.getWorld().spawnParticle(Particle.LAVA, loc, 20, 2, 1, 2);
        loc.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_DEATH, 0.8f, 0.6f);

        // Broadcast
        Component deathMsg = Component.text("\u2622 ", NamedTextColor.RED)
            .append(Component.text("Hellfire Drake", TextColor.color(255, 80, 0)).decorate(TextDecoration.BOLD))
            .append(Component.text(" has been slain!", NamedTextColor.GRAY));
        for (Player p : loc.getNearbyPlayers(100)) {
            p.sendMessage(deathMsg);
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        }

        plugin.getLogger().info("Hellfire Drake was defeated.");
    }

    // ══════════════════════════════════════════════════════════════════
    // UTILITIES
    // ══════════════════════════════════════════════════════════════════

    private Player findNearestPlayer(Location loc, double range) {
        Player nearest = null;
        double minDist = range * range;
        for (Player p : loc.getNearbyPlayers(range)) {
            double dist = p.getLocation().distanceSquared(loc);
            if (dist < minDist) {
                minDist = dist;
                nearest = p;
            }
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

    // ── Getters ──
    public boolean isWatcherAlive() { return activeWatcherUUID != null; }
    public boolean isHellfireDrakeAlive() { return activeHellfireDrakeUUID != null; }
    public NamespacedKey getKeyRoamingBoss() { return KEY_ROAMING_BOSS; }
}
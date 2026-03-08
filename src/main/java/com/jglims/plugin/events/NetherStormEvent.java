package com.jglims.plugin.events;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.legendary.LegendaryTier;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Nether Storm Event (Section G.3 of README).
 * 10% chance per Nether check cycle. Ghast swarms, enhanced Blaze spawns,
 * fire rain from the ceiling. Lasts 5 minutes.
 * Final boss: Infernal Overlord (Ghast, 400 HP, triple fireball).
 * Drops EPIC weapons.
 */
public class NetherStormEvent implements Listener {

    private final JGlimsPlugin plugin;
    private final EventManager eventManager;
    private final Random random = new Random();

    private boolean active = false;
    private World currentWorld;
    private LivingEntity boss;
    private int ticksElapsed;
    private static final int DURATION_TICKS = 6000; // 5 minutes = 6000 ticks
    private static final int WAVE_INTERVAL = 600; // 30 seconds between waves

    public NetherStormEvent(JGlimsPlugin plugin, EventManager eventManager) {
        this.plugin = plugin;
        this.eventManager = eventManager;
    }

    public void start(World world) {
        if (active) return;
        active = true;
        currentWorld = world;
        ticksElapsed = 0;

        eventManager.broadcastEvent(world, "\u26A1 NETHER STORM \u26A1",
            "The Nether trembles with fury!", NamedTextColor.RED);

        // Main event loop
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!active || currentWorld.getPlayers().isEmpty()) {
                    endEvent(false);
                    cancel();
                    return;
                }

                ticksElapsed += 20;

                // Ambient effects every tick-set (every second)
                doAmbientEffects();

                // Spawn waves every WAVE_INTERVAL ticks
                if (ticksElapsed % WAVE_INTERVAL == 0) {
                    int waveNum = ticksElapsed / WAVE_INTERVAL;
                    spawnWave(waveNum);
                }

                // Spawn boss at 4 minutes (4800 ticks)
                if (ticksElapsed == 4800 && (boss == null || boss.isDead())) {
                    spawnBoss();
                }

                // End at 5 minutes
                if (ticksElapsed >= DURATION_TICKS) {
                    endEvent(true);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private void doAmbientEffects() {
        for (Player player : currentWorld.getPlayers()) {
            Location loc = player.getLocation();
            // Fire rain particles
            for (int i = 0; i < 5; i++) {
                double rx = loc.getX() + random.nextGaussian() * 15;
                double rz = loc.getZ() + random.nextGaussian() * 15;
                double ry = loc.getY() + 10 + random.nextInt(20);
                Location particleLoc = new Location(currentWorld, rx, ry, rz);
                currentWorld.spawnParticle(Particle.LAVA, particleLoc, 3, 0.5, 0.5, 0.5);
                currentWorld.spawnParticle(Particle.FLAME, particleLoc, 2, 0.3, 0.3, 0.3);
            }
            // Occasional fire blocks (safe: on netherrack only)
            if (random.nextDouble() < 0.05) {
                int fx = loc.getBlockX() + random.nextInt(20) - 10;
                int fz = loc.getBlockZ() + random.nextInt(20) - 10;
                for (int fy = Math.min(loc.getBlockY() + 15, 120); fy > loc.getBlockY() - 5; fy--) {
                    if (currentWorld.getBlockAt(fx, fy, fz).getType() == Material.NETHERRACK
                        && currentWorld.getBlockAt(fx, fy + 1, fz).getType() == Material.AIR) {
                        currentWorld.getBlockAt(fx, fy + 1, fz).setType(Material.FIRE, false);
                        break;
                    }
                }
            }
            // Rumble sound
            if (ticksElapsed % 100 == 0) {
                player.playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 0.3f, 0.4f);
            }
        }
    }

    private void spawnWave(int waveNum) {
        List<Player> players = currentWorld.getPlayers();
        if (players.isEmpty()) return;

        Player target = players.get(random.nextInt(players.size()));
        Location center = target.getLocation();

        int ghastCount = 2 + waveNum;
        int blazeCount = 3 + waveNum * 2;

        // Spawn Ghasts above
        for (int i = 0; i < ghastCount; i++) {
            Location spawnLoc = center.clone().add(
                random.nextGaussian() * 20, 15 + random.nextInt(10), random.nextGaussian() * 20);
            // Clamp Y
            spawnLoc.setY(Math.min(spawnLoc.getY(), 115));
            spawnLoc.setY(Math.max(spawnLoc.getY(), 40));
            Ghast ghast = currentWorld.spawn(spawnLoc, Ghast.class);
            if (ghast.getAttribute(Attribute.MAX_HEALTH) != null) {
                ghast.getAttribute(Attribute.MAX_HEALTH).setBaseValue(30); // Tougher ghasts
                ghast.setHealth(30);
            }
        }

        // Spawn Blazes around
        for (int i = 0; i < blazeCount; i++) {
            Location spawnLoc = center.clone().add(
                random.nextGaussian() * 12, 2 + random.nextInt(5), random.nextGaussian() * 12);
            spawnLoc.setY(Math.min(spawnLoc.getY(), 115));
            Blaze blaze = currentWorld.spawn(spawnLoc, Blaze.class);
            if (blaze.getAttribute(Attribute.MAX_HEALTH) != null) {
                blaze.getAttribute(Attribute.MAX_HEALTH).setBaseValue(40);
                blaze.setHealth(40);
            }
        }

        // Spawn some MagmaCubes on later waves
        if (waveNum >= 3) {
            for (int i = 0; i < waveNum - 1; i++) {
                Location spawnLoc = center.clone().add(
                    random.nextGaussian() * 10, 1, random.nextGaussian() * 10);
                MagmaCube cube = currentWorld.spawn(spawnLoc, MagmaCube.class);
                cube.setSize(4 + random.nextInt(3)); // Big magma cubes
            }
        }

        // Announce wave
        for (Player p : players) {
            p.sendMessage(net.kyori.adventure.text.Component.text(
                "\u26A1 Wave " + waveNum + " approaches!", NamedTextColor.RED));
            p.playSound(p.getLocation(), Sound.ENTITY_BLAZE_AMBIENT, 1.0f, 0.5f);
        }
    }

    private void spawnBoss() {
        List<Player> players = currentWorld.getPlayers();
        if (players.isEmpty()) return;

        Player target = players.get(random.nextInt(players.size()));
        Location spawnLoc = target.getLocation().add(0, 20, 0);
        spawnLoc.setY(Math.min(spawnLoc.getY(), 110));

        boss = currentWorld.spawn(spawnLoc, Ghast.class);
        eventManager.configureBoss(boss, "\u2620 Infernal Overlord \u2620", 400,
            net.kyori.adventure.text.format.TextColor.color(255, 80, 0));
        eventManager.tagEventBoss(boss, "NETHER_STORM");

        // Boss announcement
        for (Player p : players) {
            p.showTitle(net.kyori.adventure.title.Title.title(
                net.kyori.adventure.text.Component.text("\u2620 INFERNAL OVERLORD \u2620",
                    net.kyori.adventure.text.format.TextColor.color(255, 80, 0))
                    .decorate(net.kyori.adventure.text.format.TextDecoration.BOLD),
                net.kyori.adventure.text.Component.text("The storm has a master!", NamedTextColor.GRAY),
                net.kyori.adventure.title.Title.Times.times(
                    java.time.Duration.ofMillis(300),
                    java.time.Duration.ofSeconds(2),
                    java.time.Duration.ofMillis(500))));
            p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.5f);
        }

        // Boss particle aura loop
        new BukkitRunnable() {
            @Override
            public void run() {
                if (boss == null || boss.isDead() || !active) {
                    cancel();
                    return;
                }
                Location bossLoc = boss.getLocation();
                currentWorld.spawnParticle(Particle.FLAME, bossLoc, 30, 2, 2, 2, 0.05);
                currentWorld.spawnParticle(Particle.LAVA, bossLoc, 10, 3, 3, 3);
                currentWorld.spawnParticle(Particle.SMOKE, bossLoc, 15, 2, 2, 2, 0.02);

                // Triple fireball attack: every 3 seconds, shoot 3 fireballs at nearest player
                if (boss.getTicksLived() % 60 == 0) {
                    Player nearest = null;
                    double minDist = Double.MAX_VALUE;
                    for (Player p : currentWorld.getPlayers()) {
                        double d = p.getLocation().distanceSquared(bossLoc);
                        if (d < minDist) { minDist = d; nearest = p; }
                    }
                    if (nearest != null) {
                        Vector dir = nearest.getLocation().add(0, 1, 0)
                            .subtract(bossLoc).toVector().normalize();
                        for (int i = 0; i < 3; i++) {
                            Vector spread = dir.clone().add(new Vector(
                                random.nextGaussian() * 0.15,
                                random.nextGaussian() * 0.1,
                                random.nextGaussian() * 0.15));
                            boss.launchProjectile(org.bukkit.entity.LargeFireball.class, spread.multiply(2));
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 5L, 5L);
    }

    @EventHandler
    public void onBossDeath(EntityDeathEvent event) {
        if (!active) return;
        LivingEntity entity = event.getEntity();
        if (!eventManager.isEventBoss(entity)) return;
        String type = eventManager.getEventType(entity);
        if (!"NETHER_STORM".equals(type)) return;

        Location loc = entity.getLocation();

        // Drop EPIC weapons (1-2)
        int weaponCount = 1 + eventManager.getRandom().nextInt(2);
        eventManager.dropEventWeapons(loc, LegendaryTier.EPIC, weaponCount);

        // Drop diamonds and loot
        eventManager.dropMiscLoot(loc, 8, 15);

        // Drop gold (thematic)
        loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.GOLD_INGOT, 10 + random.nextInt(15)));

        // Death effects
        currentWorld.spawnParticle(Particle.EXPLOSION, loc, 5, 2, 2, 2);
        currentWorld.spawnParticle(Particle.LAVA, loc, 30, 3, 3, 3);
        currentWorld.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.5f);

        boss = null;
        endEvent(true);
    }

    private void endEvent(boolean natural) {
        if (!active) return;
        active = false;

        if (currentWorld != null) {
            if (natural) {
                eventManager.broadcastEventEnd(currentWorld,
                    "\u2728 The Nether Storm subsides...", NamedTextColor.GOLD);
            }
            eventManager.endEvent(currentWorld);
        }
        currentWorld = null;
    }

    public boolean isActive() { return active; }
}
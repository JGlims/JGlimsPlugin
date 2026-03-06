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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Void Collapse Event (Section G.4 of README).
 * 5% chance when players are in the End. Void tentacles attack from below,
 * Endermen become aggressive. Lasts 5 minutes.
 * Boss: Void Leviathan (Elder Guardian variant, 500 HP, void beam).
 * Drops MYTHIC weapons.
 */
public class VoidCollapseEvent implements Listener {

    private final JGlimsPlugin plugin;
    private final EventManager eventManager;
    private final Random random = new Random();
    private boolean active = false;
    private World currentWorld;
    private LivingEntity boss;
    private int ticksElapsed;
    private static final int DURATION_TICKS = 6000;

    public VoidCollapseEvent(JGlimsPlugin plugin, EventManager eventManager) {
        this.plugin = plugin;
        this.eventManager = eventManager;
    }

    public void start(World world) {
        if (active) return;
        active = true;
        currentWorld = world;
        ticksElapsed = 0;

        eventManager.broadcastEvent(world, "\u2620 VOID COLLAPSE \u2620",
            "Reality fractures beneath your feet!", NamedTextColor.DARK_PURPLE);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!active || currentWorld.getPlayers().isEmpty()) {
                    endEvent(false); cancel(); return;
                }
                ticksElapsed += 20;
                doAmbientEffects();
                if (ticksElapsed % 800 == 0) spawnEndermen();
                if (ticksElapsed == 4200 && (boss == null || boss.isDead())) spawnBoss();
                if (ticksElapsed >= DURATION_TICKS) { endEvent(true); cancel(); }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private void doAmbientEffects() {
        for (Player player : currentWorld.getPlayers()) {
            Location loc = player.getLocation();
            // Void tentacle particles from below
            for (int i = 0; i < 8; i++) {
                double rx = loc.getX() + random.nextGaussian() * 10;
                double rz = loc.getZ() + random.nextGaussian() * 10;
                Location tentacle = new Location(currentWorld, rx, loc.getY() - 3, rz);
                currentWorld.spawnParticle(Particle.REVERSE_PORTAL, tentacle, 8, 0.3, 2, 0.3, 0.05);
                currentWorld.spawnParticle(Particle.PORTAL, tentacle, 5, 0.5, 3, 0.5, 0.1);
            }
            // Pull effect: slight downward velocity
            if (ticksElapsed % 60 == 0 && !player.isFlying()) {
                player.setVelocity(player.getVelocity().add(new Vector(0, -0.15, 0)));
                player.playSound(loc, Sound.BLOCK_PORTAL_AMBIENT, 0.4f, 0.3f);
            }
            // Blindness flickers
            if (random.nextDouble() < 0.03) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 30, 0, true, false));
            }
            // Rumble
            if (ticksElapsed % 100 == 0) {
                player.playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 0.3f, 0.2f);
            }
        }
    }

    private void spawnEndermen() {
        for (Player player : currentWorld.getPlayers()) {
            Location center = player.getLocation();
            int count = 5 + random.nextInt(5);
            for (int i = 0; i < count; i++) {
                Location spawnLoc = center.clone().add(
                    random.nextGaussian() * 15, 1 + random.nextInt(3), random.nextGaussian() * 15);
                Enderman enderman = currentWorld.spawn(spawnLoc, Enderman.class);
                if (enderman.getAttribute(Attribute.MAX_HEALTH) != null) {
                    enderman.getAttribute(Attribute.MAX_HEALTH).setBaseValue(60);
                    enderman.setHealth(60);
                }
                // Force aggro on the player
                enderman.setTarget(player);
            }
        }
    }

    private void spawnBoss() {
        List<Player> players = currentWorld.getPlayers();
        if (players.isEmpty()) return;
        Player target = players.get(random.nextInt(players.size()));
        Location spawnLoc = target.getLocation().add(0, 5, 0);

        boss = currentWorld.spawn(spawnLoc, ElderGuardian.class);
        eventManager.configureBoss(boss, "\u2620 Void Leviathan \u2620", 500,
            net.kyori.adventure.text.format.TextColor.color(100, 0, 170));
        eventManager.tagEventBoss(boss, "VOID_COLLAPSE");

        for (Player p : players) {
            p.showTitle(net.kyori.adventure.title.Title.title(
                net.kyori.adventure.text.Component.text("\u2620 VOID LEVIATHAN \u2620",
                    net.kyori.adventure.text.format.TextColor.color(100, 0, 170))
                    .decorate(net.kyori.adventure.text.format.TextDecoration.BOLD),
                net.kyori.adventure.text.Component.text("The void hungers...", NamedTextColor.GRAY)));
            p.playSound(p.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 1.0f, 0.5f);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                if (boss == null || boss.isDead() || !active) { cancel(); return; }
                Location bossLoc = boss.getLocation();
                currentWorld.spawnParticle(Particle.REVERSE_PORTAL, bossLoc, 40, 2, 2, 2, 0.1);
                currentWorld.spawnParticle(Particle.WITCH, bossLoc, 15, 1.5, 1.5, 1.5, 0.02);

                // Void beam: every 3 seconds, Wither + slow nearest player
                if (boss.getTicksLived() % 60 == 0) {
                    Player nearest = null;
                    double minDist = Double.MAX_VALUE;
                    for (Player p : currentWorld.getPlayers()) {
                        double d = p.getLocation().distanceSquared(bossLoc);
                        if (d < minDist) { minDist = d; nearest = p; }
                    }
                    if (nearest != null && minDist < 2500) { // 50 blocks
                        nearest.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 60, 1));
                        nearest.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1));
                        nearest.damage(6.0, boss);
                        // Beam visual
                        Vector dir = nearest.getLocation().subtract(bossLoc).toVector().normalize();
                        for (double t = 0; t < nearest.getLocation().distance(bossLoc); t += 0.5) {
                            Location point = bossLoc.clone().add(dir.clone().multiply(t));
                            currentWorld.spawnParticle(Particle.REVERSE_PORTAL, point, 3, 0.1, 0.1, 0.1, 0);
                        }
                        currentWorld.playSound(bossLoc, Sound.ENTITY_ELDER_GUARDIAN_HURT, 1.0f, 0.3f);
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
        if (!"VOID_COLLAPSE".equals(eventManager.getEventType(entity))) return;

        Location loc = entity.getLocation();
        int weaponCount = 1 + eventManager.getRandom().nextInt(2);
        eventManager.dropEventWeapons(loc, LegendaryTier.MYTHIC, weaponCount);
        eventManager.dropMiscLoot(loc, 12, 25);
        loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.ENDER_PEARL, 8 + random.nextInt(8)));

        currentWorld.spawnParticle(Particle.REVERSE_PORTAL, loc, 100, 3, 3, 3, 0.2);
        currentWorld.spawnParticle(Particle.EXPLOSION, loc, 5, 2, 2, 2);
        currentWorld.playSound(loc, Sound.ENTITY_ENDER_DRAGON_DEATH, 1.5f, 0.5f);

        boss = null;
        endEvent(true);
    }

    private void endEvent(boolean natural) {
        if (!active) return;
        active = false;
        if (currentWorld != null) {
            if (natural) eventManager.broadcastEventEnd(currentWorld,
                "\u2728 The Void recedes... for now.", NamedTextColor.LIGHT_PURPLE);
            eventManager.endEvent(currentWorld);
            // Remove blindness
            for (Player p : currentWorld.getPlayers()) {
                p.removePotionEffect(PotionEffectType.BLINDNESS);
            }
        }
        currentWorld = null;
    }

    public boolean isActive() { return active; }
}
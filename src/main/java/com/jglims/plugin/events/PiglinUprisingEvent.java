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
 * Piglin Uprising Event (Section G.3 of README).
 * 8% chance per Nether check. Massive Piglin army. Lasts 5 minutes.
 * Final boss: Piglin Emperor (Piglin Brute, 500 HP, golden mace).
 * Drops EPIC weapons + gold.
 */
public class PiglinUprisingEvent implements Listener {

    private final JGlimsPlugin plugin;
    private final EventManager eventManager;
    private final Random random = new Random();
    private boolean active = false;
    private World currentWorld;
    private LivingEntity boss;
    private int ticksElapsed;
    private static final int DURATION_TICKS = 6000;
    private static final int WAVE_INTERVAL = 600;

    public PiglinUprisingEvent(JGlimsPlugin plugin, EventManager eventManager) {
        this.plugin = plugin;
        this.eventManager = eventManager;
    }

    public void start(World world) {
        if (active) return;
        active = true;
        currentWorld = world;
        ticksElapsed = 0;

        eventManager.broadcastEvent(world, "\u2694 PIGLIN UPRISING \u2694",
            "The Piglins rally for war!", NamedTextColor.GOLD);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!active || currentWorld.getPlayers().isEmpty()) {
                    endEvent(false); cancel(); return;
                }
                ticksElapsed += 20;
                doAmbientEffects();
                if (ticksElapsed % WAVE_INTERVAL == 0) spawnWave(ticksElapsed / WAVE_INTERVAL);
                if (ticksElapsed == 4800 && (boss == null || boss.isDead())) spawnBoss();
                if (ticksElapsed >= DURATION_TICKS) { endEvent(true); cancel(); }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private void doAmbientEffects() {
        for (Player p : currentWorld.getPlayers()) {
            if (ticksElapsed % 80 == 0) {
                p.playSound(p.getLocation(), Sound.ENTITY_PIGLIN_BRUTE_ANGRY, 0.5f, 0.7f);
            }
            currentWorld.spawnParticle(Particle.WAX_OFF, p.getLocation().add(0, 5, 0), 5, 8, 3, 8);
        }
    }

    private void spawnWave(int waveNum) {
        List<Player> players = currentWorld.getPlayers();
        if (players.isEmpty()) return;
        Player target = players.get(random.nextInt(players.size()));
        Location center = target.getLocation();

        int bruteCount = 1 + waveNum;
        int piglinCount = 4 + waveNum * 3;

        for (int i = 0; i < piglinCount; i++) {
            Location spawnLoc = center.clone().add(random.nextGaussian() * 12, 1, random.nextGaussian() * 12);
            Piglin piglin = currentWorld.spawn(spawnLoc, Piglin.class);
            piglin.setImmuneToZombification(true);
            piglin.setIsAbleToHunt(true);
            if (piglin.getAttribute(Attribute.MAX_HEALTH) != null) {
                piglin.getAttribute(Attribute.MAX_HEALTH).setBaseValue(40);
                piglin.setHealth(40);
            }
        }
        for (int i = 0; i < bruteCount; i++) {
            Location spawnLoc = center.clone().add(random.nextGaussian() * 8, 1, random.nextGaussian() * 8);
            PiglinBrute brute = currentWorld.spawn(spawnLoc, PiglinBrute.class);
            brute.setImmuneToZombification(true);
            if (brute.getAttribute(Attribute.MAX_HEALTH) != null) {
                brute.getAttribute(Attribute.MAX_HEALTH).setBaseValue(80);
                brute.setHealth(80);
            }
        }
        if (waveNum >= 3) {
            for (int i = 0; i < waveNum - 1; i++) {
                Location spawnLoc = center.clone().add(random.nextGaussian() * 10, 1, random.nextGaussian() * 10);
                Hoglin hoglin = currentWorld.spawn(spawnLoc, Hoglin.class);
                hoglin.setImmuneToZombification(true);
                if (hoglin.getAttribute(Attribute.MAX_HEALTH) != null) {
                    hoglin.getAttribute(Attribute.MAX_HEALTH).setBaseValue(60);
                    hoglin.setHealth(60);
                }
            }
        }
        for (Player p : players) {
            p.sendMessage(net.kyori.adventure.text.Component.text(
                "\u2694 Piglin Wave " + waveNum + "!", NamedTextColor.GOLD));
            p.playSound(p.getLocation(), Sound.ENTITY_PIGLIN_BRUTE_ANGRY, 1.0f, 0.8f);
        }
    }

    private void spawnBoss() {
        List<Player> players = currentWorld.getPlayers();
        if (players.isEmpty()) return;
        Player target = players.get(random.nextInt(players.size()));
        Location spawnLoc = target.getLocation().add(0, 2, 0);

        boss = currentWorld.spawn(spawnLoc, PiglinBrute.class);
        ((PiglinBrute) boss).setImmuneToZombification(true);
        eventManager.configureBoss(boss, "\u2620 Piglin Emperor \u2620", 500,
            net.kyori.adventure.text.format.TextColor.color(255, 215, 0));
        eventManager.tagEventBoss(boss, "PIGLIN_UPRISING");

        // Give golden armor appearance
        boss.getEquipment().setHelmet(new ItemStack(Material.GOLDEN_HELMET));
        boss.getEquipment().setChestplate(new ItemStack(Material.GOLDEN_CHESTPLATE));
        boss.getEquipment().setLeggings(new ItemStack(Material.GOLDEN_LEGGINGS));
        boss.getEquipment().setBoots(new ItemStack(Material.GOLDEN_BOOTS));
        boss.getEquipment().setItemInMainHand(new ItemStack(Material.MACE));
        boss.getEquipment().setHelmetDropChance(0);
        boss.getEquipment().setChestplateDropChance(0);
        boss.getEquipment().setLeggingsDropChance(0);
        boss.getEquipment().setBootsDropChance(0);
        boss.getEquipment().setItemInMainHandDropChance(0);

        for (Player p : players) {
            p.showTitle(net.kyori.adventure.title.Title.title(
                net.kyori.adventure.text.Component.text("\u2620 PIGLIN EMPEROR \u2620",
                    net.kyori.adventure.text.format.TextColor.color(255, 215, 0))
                    .decorate(net.kyori.adventure.text.format.TextDecoration.BOLD),
                net.kyori.adventure.text.Component.text("Bow before the king!", NamedTextColor.GRAY)));
            p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.7f);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                if (boss == null || boss.isDead() || !active) { cancel(); return; }
                Location bossLoc = boss.getLocation();
                currentWorld.spawnParticle(Particle.WAX_OFF, bossLoc, 20, 1.5, 1.5, 1.5, 0.02);

                // Ground slam: every 4 seconds, damage + knockback nearby players
                if (boss.getTicksLived() % 80 == 0) {
                    currentWorld.spawnParticle(Particle.EXPLOSION, bossLoc, 3, 1, 0.5, 1);
                    currentWorld.playSound(bossLoc, Sound.ENTITY_IRON_GOLEM_ATTACK, 1.5f, 0.5f);
                    for (Player p : bossLoc.getNearbyPlayers(5)) {
                        p.damage(8.0, boss);
                        Vector kb = p.getLocation().subtract(bossLoc).toVector().normalize().multiply(1.5).setY(0.6);
                        p.setVelocity(kb);
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
        if (!"PIGLIN_UPRISING".equals(eventManager.getEventType(entity))) return;

        Location loc = entity.getLocation();
        int weaponCount = 1 + eventManager.getRandom().nextInt(2);
        eventManager.dropEventWeapons(loc, LegendaryTier.EPIC, weaponCount);
        eventManager.dropMiscLoot(loc, 10, 20);
        loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.GOLD_BLOCK, 5 + random.nextInt(10)));

        currentWorld.spawnParticle(Particle.EXPLOSION, loc, 5, 2, 2, 2);
        currentWorld.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.6f);

        boss = null;
        endEvent(true);
    }

    private void endEvent(boolean natural) {
        if (!active) return;
        active = false;
        if (currentWorld != null) {
            if (natural) eventManager.broadcastEventEnd(currentWorld,
                "\u2728 The Piglin Uprising is quelled!", NamedTextColor.GOLD);
            eventManager.endEvent(currentWorld);
        }
        currentWorld = null;
    }

    public boolean isActive() { return active; }
}
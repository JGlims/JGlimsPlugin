package com.jglims.plugin.events;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.legendary.LegendaryTier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
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

import java.time.Duration;
import java.util.*;

/**
 * Pillager War Party Event.
 * 6% chance per Overworld check cycle. Spawns a roaming war party of enhanced
 * Pillagers + Ravagers near a random player. Led by a War Captain (500 HP).
 * Lasts 8 minutes. Party actively hunts the nearest player.
 * Drops EPIC weapons on captain death.
 */
public class PillagerWarPartyEvent implements Listener {

    private final JGlimsPlugin plugin;
    private final EventManager eventManager;
    private final Random random = new Random();

    private boolean active = false;
    private World currentWorld;
    private LivingEntity captain;
    private final List<LivingEntity> partyMembers = new ArrayList<>();
    private int ticksElapsed;
    private static final int DURATION_TICKS = 9600; // 8 minutes
    private static final int REINFORCE_INTERVAL = 1200; // 60 seconds

    public PillagerWarPartyEvent(JGlimsPlugin plugin, EventManager eventManager) {
        this.plugin = plugin;
        this.eventManager = eventManager;
    }

    public void start(World world) {
        if (active) return;
        active = true;
        currentWorld = world;
        ticksElapsed = 0;
        partyMembers.clear();

        eventManager.broadcastEvent(world, "\u2694 PILLAGER WAR PARTY \u2694",
            "A warband marches across the land!", TextColor.color(120, 60, 30));

        spawnInitialParty();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!active || currentWorld.getPlayers().isEmpty()) {
                    endEvent(false);
                    cancel();
                    return;
                }
                ticksElapsed += 20;

                doPartyAI();
                doAmbientEffects();

                // Reinforcements every REINFORCE_INTERVAL
                if (ticksElapsed % REINFORCE_INTERVAL == 0 && ticksElapsed < DURATION_TICKS - 1200) {
                    spawnReinforcements();
                }

                // Timeout
                if (ticksElapsed >= DURATION_TICKS) {
                    endEvent(true);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private void spawnInitialParty() {
        List<Player> players = currentWorld.getPlayers();
        if (players.isEmpty()) return;
        Player target = players.get(random.nextInt(players.size()));
        Location center = findSafeSpawn(target.getLocation(), 40, 60);

        // War Captain (boss)
        captain = spawnCaptain(center);

        // 6 Pillagers
        for (int i = 0; i < 6; i++) {
            Location loc = center.clone().add(random.nextGaussian() * 5, 0, random.nextGaussian() * 5);
            loc.setY(currentWorld.getHighestBlockYAt(loc.getBlockX(), loc.getBlockZ()) + 1);
            Pillager p = currentWorld.spawn(loc, Pillager.class);
            enhancePillager(p, 60, 12);
            p.customName(Component.text("War Raider", NamedTextColor.DARK_RED));
            p.setCustomNameVisible(true);
            partyMembers.add(p);
        }

        // 2 Ravagers
        for (int i = 0; i < 2; i++) {
            Location loc = center.clone().add(random.nextGaussian() * 6, 0, random.nextGaussian() * 6);
            loc.setY(currentWorld.getHighestBlockYAt(loc.getBlockX(), loc.getBlockZ()) + 1);
            Ravager r = currentWorld.spawn(loc, Ravager.class);
            if (r.getAttribute(Attribute.MAX_HEALTH) != null) {
                r.getAttribute(Attribute.MAX_HEALTH).setBaseValue(200);
                r.setHealth(200);
            }
            r.customName(Component.text("War Ravager", TextColor.color(180, 50, 20)));
            r.setCustomNameVisible(true);
            partyMembers.add(r);
        }

        // 2 Vindicators
        for (int i = 0; i < 2; i++) {
            Location loc = center.clone().add(random.nextGaussian() * 4, 0, random.nextGaussian() * 4);
            loc.setY(currentWorld.getHighestBlockYAt(loc.getBlockX(), loc.getBlockZ()) + 1);
            Vindicator v = currentWorld.spawn(loc, Vindicator.class);
            enhanceVindicator(v, 80, 16);
            v.customName(Component.text("War Berserker", TextColor.color(200, 40, 40)));
            v.setCustomNameVisible(true);
            partyMembers.add(v);
        }

        // 1 Evoker
        Location evoLoc = center.clone().add(random.nextGaussian() * 3, 0, random.nextGaussian() * 3);
        evoLoc.setY(currentWorld.getHighestBlockYAt(evoLoc.getBlockX(), evoLoc.getBlockZ()) + 1);
        Evoker evoker = currentWorld.spawn(evoLoc, Evoker.class);
        if (evoker.getAttribute(Attribute.MAX_HEALTH) != null) {
            evoker.getAttribute(Attribute.MAX_HEALTH).setBaseValue(100);
            evoker.setHealth(100);
        }
        evoker.customName(Component.text("War Mage", TextColor.color(100, 0, 150)));
        evoker.setCustomNameVisible(true);
        partyMembers.add(evoker);
    }

    private LivingEntity spawnCaptain(Location center) {
        Location loc = center.clone();
        loc.setY(currentWorld.getHighestBlockYAt(loc.getBlockX(), loc.getBlockZ()) + 1);
        Vindicator cap = currentWorld.spawn(loc, Vindicator.class);
        eventManager.configureBoss(cap, "\u2694 War Captain \u2694", 500,
            TextColor.color(180, 30, 30));
        eventManager.tagEventBoss(cap, "PILLAGER_WAR_PARTY");
        cap.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, false));
        cap.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 1, false, false));
        cap.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 2, false, false));
        if (cap.getAttribute(Attribute.ATTACK_DAMAGE) != null) {
            cap.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(20);
        }
        cap.getEquipment().setItemInMainHand(new ItemStack(Material.NETHERITE_AXE));
        cap.getEquipment().setHelmet(new ItemStack(Material.NETHERITE_HELMET));
        cap.getEquipment().setChestplate(new ItemStack(Material.NETHERITE_CHESTPLATE));
        cap.getEquipment().setItemInMainHandDropChance(0f);
        cap.getEquipment().setHelmetDropChance(0f);
        cap.getEquipment().setChestplateDropChance(0f);
        partyMembers.add(cap);
        return cap;
    }

    private void spawnReinforcements() {
        if (captain == null || captain.isDead()) return;
        Location loc = captain.getLocation();

        for (int i = 0; i < 3; i++) {
            Location spawn = loc.clone().add(random.nextGaussian() * 8, 0, random.nextGaussian() * 8);
            spawn.setY(currentWorld.getHighestBlockYAt(spawn.getBlockX(), spawn.getBlockZ()) + 1);
            Pillager p = currentWorld.spawn(spawn, Pillager.class);
            enhancePillager(p, 50, 10);
            p.customName(Component.text("War Reinforcement", NamedTextColor.GRAY));
            p.setCustomNameVisible(true);
            partyMembers.add(p);
        }

        if (random.nextDouble() < 0.4) {
            Location spawn = loc.clone().add(random.nextGaussian() * 6, 0, random.nextGaussian() * 6);
            spawn.setY(currentWorld.getHighestBlockYAt(spawn.getBlockX(), spawn.getBlockZ()) + 1);
            Ravager r = currentWorld.spawn(spawn, Ravager.class);
            if (r.getAttribute(Attribute.MAX_HEALTH) != null) {
                r.getAttribute(Attribute.MAX_HEALTH).setBaseValue(150);
                r.setHealth(150);
            }
            r.customName(Component.text("War Beast", TextColor.color(160, 60, 20)));
            r.setCustomNameVisible(true);
            partyMembers.add(r);
        }

        for (Player p : currentWorld.getPlayers()) {
            p.sendMessage(Component.text("\u2694 Pillager reinforcements arrive!", NamedTextColor.DARK_RED));
            p.playSound(p.getLocation(), Sound.EVENT_RAID_HORN, 0.8f, 1.2f);
        }
    }

    private void doPartyAI() {
        // Captain phase 2 at 50% HP — speed and damage boost
        if (captain != null && !captain.isDead()) {
            double hpPercent = captain.getHealth() / captain.getAttribute(Attribute.MAX_HEALTH).getBaseValue();
            if (hpPercent <= 0.5 && !captain.getScoreboardTags().contains("phase2")) {
                captain.addScoreboardTag("phase2");
                captain.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2, false, false));
                captain.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 3, false, false));
                captain.customName(Component.text("\u2694 Enraged War Captain \u2694",
                    TextColor.color(255, 20, 20)).decorate(TextDecoration.BOLD));
                for (Player p : currentWorld.getPlayers()) {
                    p.sendMessage(Component.text("\u2620 The War Captain enters a frenzy!",
                        TextColor.color(255, 50, 50)).decorate(TextDecoration.BOLD));
                    p.playSound(p.getLocation(), Sound.ENTITY_RAVAGER_ROAR, 1.0f, 0.6f);
                }
            }

            // Captain aura — particles
            Location bLoc = captain.getLocation();
            currentWorld.spawnParticle(Particle.ANGRY_VILLAGER, bLoc.add(0, 2, 0), 5, 1, 0.5, 1);
        }

        // Clean up dead members
        partyMembers.removeIf(e -> e == null || e.isDead());
    }

    private void doAmbientEffects() {
        if (captain == null || captain.isDead()) return;
        // War drums sound
        if (ticksElapsed % 200 == 0) {
            for (Player p : currentWorld.getPlayers()) {
                if (p.getLocation().distanceSquared(captain.getLocation()) < 10000) { // 100 blocks
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 0.6f, 0.5f);
                }
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!active) return;
        LivingEntity entity = event.getEntity();
        if (!eventManager.isEventBoss(entity)) return;
        String type = eventManager.getEventType(entity);
        if (!"PILLAGER_WAR_PARTY".equals(type)) return;

        Location loc = entity.getLocation();

        // Drop EPIC weapons (1-2)
        int count = 1 + random.nextInt(2);
        eventManager.dropEventWeapons(loc, LegendaryTier.EPIC, count);
        eventManager.dropMiscLoot(loc, 6, 12);

        // Thematic drops
        loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.EMERALD, 10 + random.nextInt(20)));
        loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.CROSSBOW, 1));
        if (random.nextDouble() < 0.3) {
            loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.TOTEM_OF_UNDYING, 1));
        }

        // Death VFX
        currentWorld.spawnParticle(Particle.EXPLOSION, loc, 5, 2, 2, 2);
        currentWorld.spawnParticle(Particle.SMOKE, loc, 50, 3, 3, 3, 0.05);
        currentWorld.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.7f);

        // Announce
        for (Player p : currentWorld.getPlayers()) {
            p.sendMessage(Component.text("\u2694 The War Captain has fallen! The warband scatters!",
                NamedTextColor.GREEN).decorate(TextDecoration.BOLD));
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        }

        // Remaining mobs flee (remove after 10 seconds)
        new BukkitRunnable() {
            @Override
            public void run() {
                for (LivingEntity m : partyMembers) {
                    if (m != null && !m.isDead()) m.remove();
                }
                partyMembers.clear();
            }
        }.runTaskLater(plugin, 200L);

        endEvent(false);
    }

    private void endEvent(boolean timeout) {
        if (!active) return;
        active = false;
        eventManager.endEvent(currentWorld);

        if (timeout) {
            // Remove all war party mobs
            for (LivingEntity m : partyMembers) {
                if (m != null && !m.isDead()) m.remove();
            }
            partyMembers.clear();
            eventManager.broadcastEventEnd(currentWorld,
                "\u2694 The war party retreats into the wilderness...",
                TextColor.color(120, 120, 120));
        }

        plugin.getLogger().info("Pillager War Party event ended in " + currentWorld.getName());
    }

    // ── Utility ──

    private void enhancePillager(Pillager p, double hp, double damage) {
        if (p.getAttribute(Attribute.MAX_HEALTH) != null) {
            p.getAttribute(Attribute.MAX_HEALTH).setBaseValue(hp);
            p.setHealth(hp);
        }
        if (p.getAttribute(Attribute.ATTACK_DAMAGE) != null) {
            p.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(damage);
        }
        p.getEquipment().setItemInMainHand(new ItemStack(Material.CROSSBOW));
        p.getEquipment().setItemInMainHandDropChance(0f);
    }

    private void enhanceVindicator(Vindicator v, double hp, double damage) {
        if (v.getAttribute(Attribute.MAX_HEALTH) != null) {
            v.getAttribute(Attribute.MAX_HEALTH).setBaseValue(hp);
            v.setHealth(hp);
        }
        if (v.getAttribute(Attribute.ATTACK_DAMAGE) != null) {
            v.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(damage);
        }
        v.getEquipment().setItemInMainHand(new ItemStack(Material.IRON_AXE));
        v.getEquipment().setItemInMainHandDropChance(0f);
    }

    private Location findSafeSpawn(Location playerLoc, int minDist, int maxDist) {
        for (int attempt = 0; attempt < 20; attempt++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double dist = minDist + random.nextDouble() * (maxDist - minDist);
            int x = playerLoc.getBlockX() + (int)(Math.cos(angle) * dist);
            int z = playerLoc.getBlockZ() + (int)(Math.sin(angle) * dist);
            int y = currentWorld.getHighestBlockYAt(x, z) + 1;
            Material surface = currentWorld.getBlockAt(x, y - 1, z).getType();
            if (!surface.isAir() && surface != Material.WATER && surface != Material.LAVA && y > 50 && y < 200) {
                return new Location(currentWorld, x + 0.5, y, z + 0.5);
            }
        }
        // Fallback
        int x = playerLoc.getBlockX() + 50;
        int z = playerLoc.getBlockZ() + 50;
        int y = currentWorld.getHighestBlockYAt(x, z) + 1;
        return new Location(currentWorld, x + 0.5, y, z + 0.5);
    }

    public boolean isActive() { return active; }
}
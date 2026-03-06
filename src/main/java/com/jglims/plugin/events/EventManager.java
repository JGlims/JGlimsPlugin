package com.jglims.plugin.events;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.config.ConfigManager;
import com.jglims.plugin.legendary.LegendaryTier;
import com.jglims.plugin.legendary.LegendaryWeapon;
import com.jglims.plugin.legendary.LegendaryWeaponManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Central event manager. Schedules periodic checks for Nether Storm,
 * Piglin Uprising, Void Collapse, and manages the End Rift trigger.
 * Provides shared utility methods for boss spawning, loot drops,
 * announcements, and particle effects.
 *
 * Phase 20: Added EndRiftEvent integration (dragon-death trigger).
 */
public class EventManager {

    private final JGlimsPlugin plugin;
    private final ConfigManager config;
    private final LegendaryWeaponManager weaponManager;
    private final Random random = new Random();

    private final NamespacedKey KEY_EVENT_BOSS;
    private final NamespacedKey KEY_EVENT_TYPE;

    // Active event tracking
    private final Map<String, Long> activeEvents = new HashMap<>();
    private static final long EVENT_COOLDOWN_MS = 10 * 60 * 1000L; // 10 min between events per world

    // Sub-event handlers
    private NetherStormEvent netherStorm;
    private PiglinUprisingEvent piglinUprising;
    private VoidCollapseEvent voidCollapse;
    private PillagerWarPartyEvent pillagerWarParty;
    private PillagerSiegeEvent pillagerSiege;
    private EndRiftEvent endRift;

    public EventManager(JGlimsPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
        this.weaponManager = plugin.getLegendaryWeaponManager();
        this.KEY_EVENT_BOSS = new NamespacedKey(plugin, "event_boss");
        this.KEY_EVENT_TYPE = new NamespacedKey(plugin, "event_type");
    }

    /**
     * Initialize sub-events and start the scheduler. Called from onEnable().
     */
    public void initialize() {
        netherStorm = new NetherStormEvent(plugin, this);
        piglinUprising = new PiglinUprisingEvent(plugin, this);
        voidCollapse = new VoidCollapseEvent(plugin, this);
        pillagerWarParty = new PillagerWarPartyEvent(plugin, this);
        pillagerSiege = new PillagerSiegeEvent(plugin, this);
        endRift = new EndRiftEvent(plugin, this);
        startScheduler();
        plugin.getLogger().info("Event system loaded (Nether Storm, Piglin Uprising, Void Collapse, End Rift).");
    }

    private void startScheduler() {
        // Check every 60 seconds (1200 ticks) for event triggers
        new BukkitRunnable() {
            @Override
            public void run() {
                checkOverworldEvents();
                checkNetherEvents();
                checkEndEvents();
            }
        }.runTaskTimer(plugin, 1200L, 1200L);
    }


    private void checkOverworldEvents() {
        for (World world : plugin.getServer().getWorlds()) {
            if (world.getEnvironment() != World.Environment.NORMAL) continue;
            if (world.getPlayers().isEmpty()) continue;
            if (isEventActive(world.getName())) continue;
            if (isOnCooldown(world.getName())) continue;

            // Pillager War Party: 6% chance per check
            if (random.nextDouble() < 0.06) {
                startEvent(world, "PILLAGER_WAR_PARTY");
                pillagerWarParty.start(world);
                return;
            }
            // Pillager Siege: 4% chance per check (night only)
            if (world.getTime() >= 13000 && world.getTime() <= 23000) {
                if (random.nextDouble() < 0.04) {
                    startEvent(world, "PILLAGER_SIEGE");
                    pillagerSiege.start(world);
                    return;
                }
            }
        }
    }
    private void checkNetherEvents() {
        for (World world : plugin.getServer().getWorlds()) {
            if (world.getEnvironment() != World.Environment.NETHER) continue;
            if (world.getPlayers().isEmpty()) continue;
            if (isEventActive(world.getName())) continue;
            if (isOnCooldown(world.getName())) continue;

            // Nether Storm: 10% chance per check
            if (random.nextDouble() < 0.10) {
                startEvent(world, "NETHER_STORM");
                netherStorm.start(world);
                return;
            }
            // Piglin Uprising: 8% chance per check
            if (random.nextDouble() < 0.08) {
                startEvent(world, "PIGLIN_UPRISING");
                piglinUprising.start(world);
                return;
            }
        }
    }

    private void checkEndEvents() {
        for (World world : plugin.getServer().getWorlds()) {
            if (world.getEnvironment() != World.Environment.THE_END) continue;
            if (world.getPlayers().isEmpty()) continue;
            if (isEventActive(world.getName())) continue;
            if (isOnCooldown(world.getName())) continue;

            // Void Collapse: 5% chance per check
            if (random.nextDouble() < 0.05) {
                startEvent(world, "VOID_COLLAPSE");
                voidCollapse.start(world);
                return;
            }
        }
    }

    /**
     * Called when the Ender Dragon dies. Attempts to trigger the End Rift event
     * in the overworld. Returns true if the rift opened.
     */
    public boolean tryTriggerEndRift() {
        if (endRift.isActive()) return false;
        // Check if any overworld already has an active event
        for (World w : plugin.getServer().getWorlds()) {
            if (w.getEnvironment() == World.Environment.NORMAL && isEventActive(w.getName())) {
                return false;
            }
        }
        boolean triggered = endRift.tryTrigger();
        if (triggered) {
            // Find the overworld and mark it as having an active event
            plugin.getServer().getWorlds().stream()
                    .filter(w -> w.getEnvironment() == World.Environment.NORMAL)
                    .findFirst()
                    .ifPresent(w -> startEvent(w, "END_RIFT"));
        }
        return triggered;
    }

    //  Event state management 

    public void startEvent(World world, String eventType) {
        activeEvents.put(world.getName(), System.currentTimeMillis());
        plugin.getLogger().info("Event started: " + eventType + " in " + world.getName());
    }

    public void endEvent(World world) {
        activeEvents.put(world.getName(), -System.currentTimeMillis()); // Negative = cooldown timestamp
        plugin.getLogger().info("Event ended in " + world.getName());
    }

    public boolean isEventActive(String worldName) {
        Long ts = activeEvents.get(worldName);
        return ts != null && ts > 0;
    }

    private boolean isOnCooldown(String worldName) {
        Long ts = activeEvents.get(worldName);
        if (ts == null) return false;
        if (ts > 0) return true; // Active event = effectively on cooldown
        long endTime = Math.abs(ts);
        return (System.currentTimeMillis() - endTime) < EVENT_COOLDOWN_MS;
    }

    //  Shared utilities 

    /**
     * Broadcast a dramatic event message to all players in a world.
     */
    public void broadcastEvent(World world, String title, String subtitle, TextColor color) {
        Component titleComp = Component.text(title, color).decorate(TextDecoration.BOLD);
        Component subComp = Component.text(subtitle, NamedTextColor.GRAY);
        for (Player p : world.getPlayers()) {
            p.showTitle(net.kyori.adventure.title.Title.title(titleComp, subComp,
                net.kyori.adventure.title.Title.Times.times(
                    java.time.Duration.ofMillis(500),
                    java.time.Duration.ofSeconds(3),
                    java.time.Duration.ofMillis(1000))));
            p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.8f, 0.6f);
        }
    }

    /**
     * Broadcast event ending message.
     */
    public void broadcastEventEnd(World world, String message, TextColor color) {
        Component msg = Component.text(message, color).decorate(TextDecoration.BOLD);
        for (Player p : world.getPlayers()) {
            p.sendMessage(msg);
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        }
    }

    /**
     * Tag a mob as an event boss via PDC.
     */
    public void tagEventBoss(LivingEntity entity, String eventType) {
        entity.getPersistentDataContainer().set(KEY_EVENT_BOSS, PersistentDataType.BYTE, (byte) 1);
        entity.getPersistentDataContainer().set(KEY_EVENT_TYPE, PersistentDataType.STRING, eventType);
        entity.setPersistent(true);
        entity.setRemoveWhenFarAway(false);
    }

    /**
     * Check if a mob is an event boss.
     */
    public boolean isEventBoss(LivingEntity entity) {
        return entity.getPersistentDataContainer().has(KEY_EVENT_BOSS, PersistentDataType.BYTE);
    }

    public String getEventType(LivingEntity entity) {
        return entity.getPersistentDataContainer().get(KEY_EVENT_TYPE, PersistentDataType.STRING);
    }

    /**
     * Set boss HP with custom name and glow.
     */
    public void configureBoss(LivingEntity entity, String name, double hp, TextColor color) {
        entity.customName(Component.text(name, color).decorate(TextDecoration.BOLD));
        entity.setCustomNameVisible(true);
        if (entity.getAttribute(Attribute.MAX_HEALTH) != null) {
            entity.getAttribute(Attribute.MAX_HEALTH).setBaseValue(hp);
            entity.setHealth(hp);
        }
        entity.setGlowing(true);
    }

    /**
     * Drop EPIC weapons from a pool for event bosses.
     */
    public void dropEventWeapons(Location loc, LegendaryTier tier, int count) {
        LegendaryWeapon[] pool = LegendaryWeapon.byTier(tier);
        if (pool.length == 0) return;
        for (int i = 0; i < count; i++) {
            LegendaryWeapon weapon = pool[random.nextInt(pool.length)];
            ItemStack item = weaponManager.createWeapon(weapon);
            if (item != null) {
                loc.getWorld().dropItemNaturally(loc, item);
                // Announce
                Component msg = Component.text("\u2694 ", NamedTextColor.GOLD)
                    .append(Component.text(weapon.getDisplayName(), tier.getColor()).decorate(TextDecoration.BOLD))
                    .append(Component.text(" has dropped!", NamedTextColor.GRAY));
                for (Player p : loc.getNearbyPlayers(60)) {
                    p.sendMessage(msg);
                    p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.2f);
                }
            }
        }
    }

    /**
     * Drop diamonds and misc loot at a location.
     */
    public void dropMiscLoot(Location loc, int minDiamonds, int maxDiamonds) {
        int diamonds = minDiamonds + random.nextInt(maxDiamonds - minDiamonds + 1);
        loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.DIAMOND, diamonds));
        if (random.nextDouble() < 0.3) {
            loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.GOLD_INGOT, 5 + random.nextInt(10)));
        }
        if (random.nextDouble() < 0.2) {
            loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.NETHERITE_SCRAP, 1 + random.nextInt(2)));
        }
    }

    /**
     * Spawn particles in a radius around a location.
     */
    public void spawnRadiusParticles(Location center, Particle particle, int count, double radius) {
        center.getWorld().spawnParticle(particle, center, count, radius, radius / 2, radius);
    }

    //  Getters 
    public JGlimsPlugin getPlugin() { return plugin; }
    public Random getRandom() { return random; }
    public NamespacedKey getKeyEventBoss() { return KEY_EVENT_BOSS; }
    public NamespacedKey getKeyEventType() { return KEY_EVENT_TYPE; }
    public NetherStormEvent getNetherStorm() { return netherStorm; }
    public PiglinUprisingEvent getPiglinUprising() { return piglinUprising; }
    public VoidCollapseEvent getVoidCollapse() { return voidCollapse; }
    public PillagerWarPartyEvent getPillagerWarParty() { return pillagerWarParty; }
    public PillagerSiegeEvent getPillagerSiege() { return pillagerSiege; }
    public EndRiftEvent getEndRift() { return endRift; }
}
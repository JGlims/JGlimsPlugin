package com.jglims.plugin.abyss;

import com.jglims.plugin.JGlimsPlugin;
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
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Abyss Dragon Boss â€” the ultimate boss of the Abyss dimension.
 *
 * Spawns on the central island when a player first enters (or respawns after 30 min).
 * Uses a Wither entity as base (cannot spawn a second EnderDragon on non-End worlds cleanly).
 * 1500 HP, devastating attacks, drops all 4 ABYSSAL weapons (2-3 per kill).
 */
public class AbyssDragonBoss implements Listener {

    private final JGlimsPlugin plugin;
    private final AbyssDimensionManager dimensionManager;
    private final NamespacedKey KEY_ABYSS_DRAGON;
    private final Random random = new Random();

    private LivingEntity boss;
    private boolean active = false;
    private int tickCounter = 0;
    private long lastKillTime = 0;
    private static final long RESPAWN_COOLDOWN_MS = 30 * 60 * 1000L; // 30 minutes

    public AbyssDragonBoss(JGlimsPlugin plugin, AbyssDimensionManager dimensionManager) {
        this.plugin = plugin;
        this.dimensionManager = dimensionManager;
        this.KEY_ABYSS_DRAGON = new NamespacedKey(plugin, "abyss_dragon");
    }

    @EventHandler
    public void onPlayerEnterAbyss(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        World abyssWorld = dimensionManager.getAbyssWorld();
        if (abyssWorld == null) return;
        if (!player.getWorld().equals(abyssWorld)) return;

        // Check if boss should spawn
        // Always clean up vanilla Ender Dragon
            abyssWorld.getEntitiesByClass(org.bukkit.entity.EnderDragon.class).forEach(org.bukkit.entity.Entity::remove);
            if (!active && (boss == null || boss.isDead())) {
            if (System.currentTimeMillis() - lastKillTime > RESPAWN_COOLDOWN_MS) {
                // Delay spawn by 10 seconds to let player load in
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (!active && abyssWorld.getPlayers().size() > 0) {
                        spawnAbyssDragon();
                    }
                }, 200L);
            }
        }
    }

    private void spawnAbyssDragon() {
        World abyssWorld = dimensionManager.getAbyssWorld();
        if (abyssWorld == null) return;

        active = true;
        tickCounter = 0;

        // Spawn at center of the main island, high up
        int arenaY = abyssWorld.getHighestBlockYAt(0, 0) + 42; // arena is at baseY+40
        if (arenaY < 90) arenaY = 100;
        Location spawnLoc = new Location(abyssWorld, 0.5, arenaY + 5, 0.5);

        // Announce to all players in the Abyss
        for (Player p : abyssWorld.getPlayers()) {
            p.showTitle(net.kyori.adventure.title.Title.title(
                    Component.text("\u2620 ABYSS DRAGON \u2620", TextColor.color(170, 0, 0))
                            .decorate(TextDecoration.BOLD),
                    Component.text("The guardian of the Abyss awakens!", NamedTextColor.DARK_RED),
                    net.kyori.adventure.title.Title.Times.times(
                            java.time.Duration.ofMillis(500),
                            java.time.Duration.ofSeconds(4),
                            java.time.Duration.ofMillis(1000))));
            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0f, 0.2f);
            p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.3f);
        }

        // Spawn as Wither (boss bar built-in)
        boss = abyssWorld.spawn(spawnLoc, Wither.class, w -> {
            w.customName(Component.text("\u2620 Abyss Dragon \u2620", TextColor.color(170, 0, 0))
                    .decorate(TextDecoration.BOLD));
            w.setCustomNameVisible(true);
        });

        // Configure stats
        if (boss.getAttribute(Attribute.MAX_HEALTH) != null) {
            boss.getAttribute(Attribute.MAX_HEALTH).setBaseValue(1500);
            boss.setHealth(1500);
        }
        if (boss.getAttribute(Attribute.ATTACK_DAMAGE) != null) {
            boss.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(20);
        }
        boss.setGlowing(true);
        boss.setPersistent(true);
        boss.setRemoveWhenFarAway(false);
        boss.getPersistentDataContainer().set(KEY_ABYSS_DRAGON, PersistentDataType.BYTE, (byte) 1);

        // VFX
        abyssWorld.spawnParticle(Particle.DRAGON_BREATH, spawnLoc, 25, 5, 5, 5, 0.3);
        abyssWorld.spawnParticle(Particle.REVERSE_PORTAL, spawnLoc, 30, 5, 5, 5, 0.5);

        // Start boss attack loop
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!active || boss == null || boss.isDead()) {
                    cancel();
                    return;
                }
                tickCounter += 20;
                doBossAttacks();
            }
        }.runTaskTimer(plugin, 40L, 20L);

        plugin.getLogger().info("Abyss Dragon spawned at 0, 80, 0");
    }

    private void doBossAttacks() {
        if (boss == null || boss.isDead()) return;
        World world = boss.getWorld();
        Location bossLoc = boss.getLocation();
        List<Player> nearby = new ArrayList<>();
        for (Player p : world.getPlayers()) {
            if (p.getLocation().distanceSquared(bossLoc) < 10000) nearby.add(p); // 100 blocks
        }
        if (nearby.isEmpty()) return;

        // Void Breath: every 3 seconds
        if (tickCounter % 60 == 0) {
            Player target = nearby.get(random.nextInt(nearby.size()));
            target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 2, true, false));
            target.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 60, 0, true, false));
            target.damage(12.0, boss);
            // Beam VFX
            Vector dir = target.getLocation().subtract(bossLoc).toVector().normalize();
            double dist = Math.min(bossLoc.distance(target.getLocation()), 50);
            for (double t = 0; t < dist; t += 0.5) {
                Location point = bossLoc.clone().add(dir.clone().multiply(t));
                world.spawnParticle(Particle.DRAGON_BREATH, point, 2, 0.1, 0.1, 0.1, 0);
            }
        }

        // Void Collapse: every 8 seconds, pull all nearby players toward boss
        if (tickCounter % 160 == 0) {
            for (Player p : nearby) {
                Vector pull = bossLoc.toVector().subtract(p.getLocation().toVector()).normalize().multiply(0.8);
                p.setVelocity(p.getVelocity().add(pull));
                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1, true, false));
            }
            world.spawnParticle(Particle.REVERSE_PORTAL, bossLoc, 30, 8, 4, 8, 0.3);
            for (Player p : nearby) {
                p.playSound(bossLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.3f);
            }
        }

        // Lightning barrage: every 5 seconds
        if (tickCounter % 100 == 0) {
            for (int i = 0; i < 3; i++) {
                Player target = nearby.get(random.nextInt(nearby.size()));
                Location strikeLoc = target.getLocation().add(random.nextGaussian() * 3, 0, random.nextGaussian() * 3);
                world.strikeLightning(strikeLoc);
            }
        }

        // Summon Abyss minions: every 20 seconds
        if (tickCounter % 400 == 0) {
            for (int i = 0; i < 4; i++) {
                Location sLoc = bossLoc.clone().add(random.nextGaussian() * 8, 0, random.nextGaussian() * 8);
                sLoc.setY(world.getHighestBlockYAt(sLoc.getBlockX(), sLoc.getBlockZ()) + 1);
                Enderman minion = world.spawn(sLoc, Enderman.class);
                if (minion.getAttribute(Attribute.MAX_HEALTH) != null) {
                    minion.getAttribute(Attribute.MAX_HEALTH).setBaseValue(100);
                    minion.setHealth(100);
                }
                minion.customName(Component.text("Abyss Spawn", TextColor.color(100, 0, 0)));
                minion.setCustomNameVisible(true);
                minion.setGlowing(true);
                // Target nearest player
                if (!nearby.isEmpty()) minion.setTarget(nearby.get(random.nextInt(nearby.size())));
            }
        }

        // Enrage at 30% HP
        double healthPercent = boss.getHealth() / 1500.0;
        if (healthPercent < 0.3 && tickCounter % 40 == 0) {
            // Extra void breath
            for (Player p : nearby) {
                p.damage(6.0, boss);
                p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 60, 1, true, false));
            }
            world.spawnParticle(Particle.DRAGON_BREATH, bossLoc, 25, 6, 3, 6, 0.2);
        }

        // Ambient particles
        world.spawnParticle(Particle.DRAGON_BREATH, bossLoc, 15, 2, 2, 2, 0.03);
        world.spawnParticle(Particle.REVERSE_PORTAL, bossLoc, 10, 1.5, 1.5, 1.5, 0.05);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBossDamage(EntityDamageByEntityEvent event) {
        if (!active || boss == null) return;
        if (!event.getEntity().equals(boss)) return;
        // Reduce all damage by 30% (boss is very tanky)
        event.setDamage(event.getDamage() * 0.70);
        boss.getWorld().spawnParticle(Particle.REVERSE_PORTAL, boss.getLocation(), 8, 1, 1, 1, 0.05);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBossDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (!entity.getPersistentDataContainer().has(KEY_ABYSS_DRAGON, PersistentDataType.BYTE)) return;

        active = false;
        lastKillTime = System.currentTimeMillis();
        boss = null;
        Location loc = entity.getLocation();
        World world = entity.getWorld();

        plugin.getLogger().info("Abyss Dragon defeated!");

        // Clear default drops
        event.getDrops().clear();
        event.setDroppedExp(10000);

        // Drop 2-3 ABYSSAL weapons
        LegendaryWeaponManager wm = plugin.getLegendaryWeaponManager();
        LegendaryWeapon[] abyssalPool = LegendaryWeapon.byTier(LegendaryTier.ABYSSAL);
        int count = 2 + random.nextInt(2);
        List<LegendaryWeapon> pool = new ArrayList<>(Arrays.asList(abyssalPool));
        List<LegendaryWeapon> dropped = new ArrayList<>();

        for (int i = 0; i < count && !pool.isEmpty(); i++) {
            LegendaryWeapon weapon = pool.remove(random.nextInt(pool.size()));
            ItemStack item = wm.createWeapon(weapon);
            if (item != null) {
                world.dropItemNaturally(loc, item);
                dropped.add(weapon);
            }
        }

        // Drop power-ups
        PowerUpManager pum = plugin.getPowerUpManager();
        world.dropItemNaturally(loc, pum.createHeartCrystal());
        world.dropItemNaturally(loc, pum.createHeartCrystal());
        for (int i = 0; i < 5; i++) world.dropItemNaturally(loc, pum.createSoulFragment());
        if (random.nextDouble() < 0.5) world.dropItemNaturally(loc, pum.createPhoenixFeather());

        // Misc loot
        world.dropItemNaturally(loc, new ItemStack(Material.NETHERITE_INGOT, 3 + random.nextInt(3)));
        world.dropItemNaturally(loc, new ItemStack(Material.DIAMOND, 20 + random.nextInt(20)));
        world.dropItemNaturally(loc, new ItemStack(Material.NETHER_STAR, 2));
        world.dropItemNaturally(loc, new ItemStack(Material.EXPERIENCE_BOTTLE, 32));

        // Massive VFX
        world.spawnParticle(Particle.REVERSE_PORTAL, loc, 30, 8, 8, 8, 0.5);
        world.spawnParticle(Particle.DRAGON_BREATH, loc, 25, 5, 5, 5, 0.2);
        world.spawnParticle(Particle.EXPLOSION_EMITTER, loc, 2, 5, 5, 5);
        world.spawnParticle(Particle.TOTEM_OF_UNDYING, loc, 30, 3, 5, 3, 0.5);
        world.playSound(loc, Sound.ENTITY_ENDER_DRAGON_DEATH, 2.0f, 0.3f);
        world.playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 2.0f, 1.0f);

        // Broadcast
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(Component.empty());
            p.sendMessage(Component.text("  \u2726 ", TextColor.color(170, 0, 0))
                    .append(Component.text("THE ABYSS DRAGON HAS BEEN VANQUISHED!", TextColor.color(200, 0, 0))
                            .decorate(TextDecoration.BOLD)));
            p.sendMessage(Component.text("  Drops:", NamedTextColor.GRAY));
            for (LegendaryWeapon w : dropped) {
                p.sendMessage(Component.text("    \u25B6 ", NamedTextColor.DARK_GRAY)
                        .append(Component.text("[ABYSSAL] ", TextColor.color(170, 0, 0)).decorate(TextDecoration.BOLD))
                        .append(Component.text(w.getDisplayName(), TextColor.color(170, 0, 0)).decorate(TextDecoration.BOLD))
                        .append(Component.text(" (DMG " + w.getBaseDamage() + ")", NamedTextColor.GRAY)));
            }
            p.sendMessage(Component.empty());
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        }
    }

    public boolean isActive() { return active; }
    public LivingEntity getBoss() { return boss; }
}
package com.jglims.plugin.abyss;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.legendary.LegendaryTier;
import com.jglims.plugin.legendary.LegendaryWeapon;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.time.Duration;
import java.util.*;

/**
 * AbyssDragonBoss v9.0 — Complete boss fight controller.
 * Manages spawning, combat phases, attacks, and loot.
 */
public class AbyssDragonBoss implements Listener {

    // Boss stats
    private static final double DRAGON_HP = 1500.0;
    private static final double DAMAGE_REDUCTION = 0.25;
    private static final int ARENA_RADIUS = 40;
    private static final long RESPAWN_COOLDOWN_MS = 30 * 60 * 1000; // 30 min

    private final JGlimsPlugin plugin;
    private final AbyssDimensionManager dimensionManager;

    private AbyssDragonModel dragonModel;
    private BossBar bossBar;
    private boolean fightActive = false;
    private long lastFightEnd = 0;

    // Combat state
    private int combatTaskId = -1;
    private int movementTaskId = -1;
    private List<Location> waypoints = new ArrayList<>();
    private int currentWaypoint = 0;
    private int attackCooldown = 0;
    private final Set<UUID> fightParticipants = new HashSet<>();

    public AbyssDragonBoss(JGlimsPlugin plugin, AbyssDimensionManager dimensionManager) {
        this.plugin = plugin;
        this.dimensionManager = dimensionManager;
    }

    // ==================== TRIGGER ====================

    public void manualTrigger(Player player) {
        plugin.getLogger().info("[DragonBoss] manualTrigger called by " + player.getName());
        plugin.getLogger().info("[DragonBoss] Player world: " + player.getWorld().getName());
        plugin.getLogger().info("[DragonBoss] Fight active: " + fightActive);

        World abyss = dimensionManager.getAbyssWorld();
        if (abyss == null) {
            player.sendMessage(Component.text("The Abyss is not initialized!", NamedTextColor.RED));
            plugin.getLogger().warning("[DragonBoss] Abyss world is null!");
            return;
        }

        if (!player.getWorld().getName().equals(abyss.getName())) {
            player.sendMessage(Component.text("You must be in the Abyss!", NamedTextColor.RED));
            plugin.getLogger().warning("[DragonBoss] Player not in Abyss world. Player in: " +
                player.getWorld().getName() + ", Abyss is: " + abyss.getName());
            return;
        }

        if (fightActive) {
            player.sendMessage(Component.text("The dragon is already awakened!", NamedTextColor.RED));
            return;
        }

        long timeSinceLastFight = System.currentTimeMillis() - lastFightEnd;
        if (timeSinceLastFight < RESPAWN_COOLDOWN_MS && lastFightEnd > 0) {
            long remaining = (RESPAWN_COOLDOWN_MS - timeSinceLastFight) / 1000;
            player.sendMessage(Component.text("The dragon rests... " + remaining + "s until it awakens.",
                NamedTextColor.DARK_PURPLE));
            return;
        }

        startFight(player);
    }

    // ==================== FIGHT START ====================

    private void startFight(Player trigger) {
        plugin.getLogger().info("[DragonBoss] === STARTING FIGHT ===");

        World abyss = dimensionManager.getAbyssWorld();
        if (abyss == null) {
            plugin.getLogger().severe("[DragonBoss] Cannot start: abyss world is null");
            return;
        }

        // Find arena center
        int arenaY = findArenaY(abyss);
        Location spawnLoc = new Location(abyss, 0.5, arenaY + 15, AbyssDimensionManager.ARENA_CENTER_Z + 0.5);
        plugin.getLogger().info("[DragonBoss] Arena Y: " + arenaY + ", Spawn location: " +
            spawnLoc.getBlockX() + "," + spawnLoc.getBlockY() + "," + spawnLoc.getBlockZ());

        // Generate waypoints around the arena
        generateWaypoints(spawnLoc, arenaY);

        // Create dragon model
        dragonModel = new AbyssDragonModel(plugin);
        boolean spawned = dragonModel.spawn(spawnLoc, DRAGON_HP);

        if (!spawned) {
            plugin.getLogger().severe("[DragonBoss] Dragon model failed to spawn!");
            trigger.sendMessage(Component.text(
                "The dragon could not manifest. Check server logs for [DragonModel] errors.",
                NamedTextColor.RED));
            return;
        }

        plugin.getLogger().info("[DragonBoss] Dragon model spawned successfully!");

        // Create boss bar
        bossBar = BossBar.bossBar(
            Component.text("Abyssal Dragon", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD),
            1.0f,
            BossBar.Color.PURPLE,
            BossBar.Overlay.NOTCHED_10
        );

        fightActive = true;
        fightParticipants.clear();
        fightParticipants.add(trigger.getUniqueId());

        // Show boss bar and title to all players in the Abyss
        for (Player p : abyss.getPlayers()) {
            p.showBossBar(bossBar);
            p.showTitle(Title.title(
                Component.text("ABYSSAL DRAGON", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD),
                Component.text("The void incarnate awakens", NamedTextColor.GRAY),
                Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(2), Duration.ofMillis(500))
            ));
            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0f, 0.7f);
            fightParticipants.add(p.getUniqueId());
        }

        // Lightning effects at spawn
        abyss.strikeLightningEffect(spawnLoc);
        abyss.strikeLightningEffect(spawnLoc.clone().add(5, 0, 5));
        abyss.strikeLightningEffect(spawnLoc.clone().add(-5, 0, -5));

        // Start combat and movement loops
        startCombatLoop(abyss);
        startMovementLoop(abyss);

        plugin.getLogger().info("[DragonBoss] Fight started! Dragon HP: " + dragonModel.getHealth());
    }

    private int findArenaY(World abyss) {
        // The arena should have a bedrock or solid floor at z=-120
        for (int y = 100; y > 30; y--) {
            Material mat = abyss.getBlockAt(0, y, AbyssDimensionManager.ARENA_CENTER_Z).getType();
            if (mat.isSolid()) {
                plugin.getLogger().info("[DragonBoss] Found arena floor at Y=" + y + " (material: " + mat + ")");
                return y;
            }
        }
        plugin.getLogger().warning("[DragonBoss] No solid arena floor found! Using default Y=64");
        return 64;
    }

    private void generateWaypoints(Location center, int arenaY) {
        waypoints.clear();
        double cx = center.getX();
        double cz = center.getZ();
        World w = center.getWorld();

        // Circle of waypoints around the arena at varying heights
        for (int i = 0; i < 8; i++) {
            double angle = (2 * Math.PI * i) / 8;
            double x = cx + Math.cos(angle) * (ARENA_RADIUS - 5);
            double z = cz + Math.sin(angle) * (ARENA_RADIUS - 5);
            double y = arenaY + 10 + Math.random() * 15;
            waypoints.add(new Location(w, x, y, z));
        }
        // Center high point
        waypoints.add(new Location(w, cx, arenaY + 25, cz));
        // Dive points
        waypoints.add(new Location(w, cx + 15, arenaY + 5, cz));
        waypoints.add(new Location(w, cx - 15, arenaY + 5, cz));

        plugin.getLogger().info("[DragonBoss] Generated " + waypoints.size() + " waypoints");
    }

    // ==================== COMBAT LOOP ====================

    private void startCombatLoop(World abyss) {
        combatTaskId = new BukkitRunnable() {
            @Override
            public void run() {
                if (!fightActive || dragonModel == null || !dragonModel.isAlive()) {
                    cancel();
                    return;
                }

                // Update boss bar
                double maxHp = DRAGON_HP;
                double currentHp = dragonModel.getHealth();
                float progress = (float) Math.max(0, Math.min(1, currentHp / maxHp));
                bossBar.progress(progress);

                // Check phase transitions
                double hpPercent = currentHp / maxHp;
                if (hpPercent <= 0.10 && dragonModel.getPhase() < 4) {
                    dragonModel.setPhase(4);
                    phaseAnnounce(abyss, 4, "ENRAGED — THE ABYSS CONSUMES ALL");
                } else if (hpPercent <= 0.25 && dragonModel.getPhase() < 3) {
                    dragonModel.setPhase(3);
                    phaseAnnounce(abyss, 3, "The dragon unleashes its true power!");
                } else if (hpPercent <= 0.50 && dragonModel.getPhase() < 2) {
                    dragonModel.setPhase(2);
                    phaseAnnounce(abyss, 2, "The dragon enters a rage!");
                }

                // Show boss bar to any new players
                for (Player p : abyss.getPlayers()) {
                    fightParticipants.add(p.getUniqueId());
                    p.showBossBar(bossBar);
                }

                // Attack logic
                attackCooldown--;
                if (attackCooldown <= 0) {
                    List<Player> nearby = getPlayersInArena(abyss);
                    if (!nearby.isEmpty()) {
                        Player target = nearby.get((int)(Math.random() * nearby.size()));
                        performAttack(target, abyss);
                        attackCooldown = 3 + (int)(Math.random() * 3); // 6-12 seconds
                    }
                }

                // Check death
                if (currentHp <= 0) {
                    onDragonDeath(abyss);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 40L, 40L).getTaskId(); // every 2 seconds
    }

    private void startMovementLoop(World abyss) {
        movementTaskId = new BukkitRunnable() {
            @Override
            public void run() {
                if (!fightActive || dragonModel == null || !dragonModel.isAlive()) {
                    cancel();
                    return;
                }

                if (waypoints.isEmpty()) return;

                Location target = waypoints.get(currentWaypoint);
                dragonModel.moveToward(target, 0.8 + (dragonModel.getPhase() * 0.2));

                // Reached waypoint?
                Location dragonLoc = dragonModel.getLocation();
                if (dragonLoc != null && dragonLoc.distance(target) < 3.0) {
                    // Sometimes fly toward a player instead
                    List<Player> nearby = getPlayersInArena(abyss);
                    if (!nearby.isEmpty() && Math.random() < 0.3) {
                        Player p = nearby.get((int)(Math.random() * nearby.size()));
                        Location playerAbove = p.getLocation().clone().add(0, 8, 0);
                        dragonModel.moveToward(playerAbove, 1.2);
                    }
                    currentWaypoint = (currentWaypoint + 1) % waypoints.size();
                }
            }
        }.runTaskTimer(plugin, 10L, 10L).getTaskId(); // every 0.5 seconds
    }

    // ==================== ATTACKS ====================

    private void performAttack(Player target, World abyss) {
        int phase = dragonModel.getPhase();
        double roll = Math.random();

        if (phase >= 4) {
            // Enrage: lightning barrage + void pull
            enrageLightningBarrage(abyss, target);
        } else if (phase >= 3) {
            if (roll < 0.3) voidBreath(target);
            else if (roll < 0.5) groundPound(abyss);
            else if (roll < 0.7) summonMinions(abyss);
            else voidPull(abyss);
        } else if (phase >= 2) {
            if (roll < 0.4) voidBreath(target);
            else if (roll < 0.6) wingGust(abyss);
            else summonMinions(abyss);
        } else {
            if (roll < 0.4) voidBreath(target);
            else if (roll < 0.7) lightningStrike(abyss, target);
            else wingGust(abyss);
        }
    }

    private void voidBreath(Player target) {
        if (dragonModel == null) return;
        dragonModel.breathAttackVisual(target.getLocation());
        target.damage(8.0 + dragonModel.getPhase() * 2.0);
        target.sendMessage(Component.text("The dragon's void breath burns!", NamedTextColor.DARK_PURPLE));
    }

    private void lightningStrike(World abyss, Player target) {
        abyss.strikeLightningEffect(target.getLocation());
        target.damage(6.0);
    }

    private void wingGust(World abyss) {
        if (dragonModel == null) return;
        dragonModel.wingGustVisual();
        Location dragonLoc = dragonModel.getLocation();
        if (dragonLoc == null) return;

        for (Player p : getPlayersInArena(abyss)) {
            Vector knockback = p.getLocation().toVector().subtract(dragonLoc.toVector()).normalize().multiply(2.5);
            knockback.setY(0.8);
            p.setVelocity(knockback);
            p.damage(4.0);
        }
    }

    private void groundPound(World abyss) {
        if (dragonModel == null) return;
        Location loc = dragonModel.getLocation();
        if (loc == null) return;

        abyss.spawnParticle(Particle.EXPLOSION_EMITTER, loc, 3, 2, 0, 2, 0.1);
        abyss.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.5f);

        for (Player p : getPlayersInArena(abyss)) {
            if (p.getLocation().distance(loc) < 15) {
                p.damage(10.0);
                Vector up = new Vector(0, 1.5, 0);
                p.setVelocity(p.getVelocity().add(up));
            }
        }
    }

    private void voidPull(World abyss) {
        if (dragonModel == null) return;
        Location dragonLoc = dragonModel.getLocation();
        if (dragonLoc == null) return;

        abyss.spawnParticle(Particle.PORTAL, dragonLoc, 200, 8, 8, 8, 0.5);
        abyss.playSound(dragonLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 2.0f, 0.3f);

        for (Player p : getPlayersInArena(abyss)) {
            Vector pull = dragonLoc.toVector().subtract(p.getLocation().toVector()).normalize().multiply(0.8);
            p.setVelocity(p.getVelocity().add(pull));
            p.damage(3.0);
        }
    }

    private void summonMinions(World abyss) {
        if (dragonModel == null) return;
        Location loc = dragonModel.getLocation();
        if (loc == null) return;

        int count = Math.min(3 + dragonModel.getPhase(), 6);
        for (int i = 0; i < count; i++) {
            Location spawn = loc.clone().add(
                (Math.random() - 0.5) * 20, -5, (Math.random() - 0.5) * 20);

            if (Math.random() < 0.5) {
                Enderman e = abyss.spawn(spawn, Enderman.class);
                e.customName(Component.text("Void Minion", NamedTextColor.DARK_PURPLE));
            } else {
                WitherSkeleton ws = abyss.spawn(spawn, WitherSkeleton.class);
                ws.customName(Component.text("Abyss Guard", NamedTextColor.DARK_RED));
            }
        }

        abyss.playSound(loc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.5f, 0.5f);
        for (Player p : abyss.getPlayers()) {
            p.sendMessage(Component.text("Minions emerge from the void!", NamedTextColor.DARK_PURPLE));
        }
    }

    private void enrageLightningBarrage(World abyss, Player target) {
        new BukkitRunnable() {
            int strikes = 0;
            @Override
            public void run() {
                if (strikes >= 8 || !fightActive) { cancel(); return; }
                Location loc = target.getLocation().add(
                    (Math.random() - 0.5) * 10, 0, (Math.random() - 0.5) * 10);
                abyss.strikeLightningEffect(loc);
                strikes++;
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }

    private void phaseAnnounce(World abyss, int phase, String message) {
        for (Player p : abyss.getPlayers()) {
            p.showTitle(Title.title(
                Component.text("PHASE " + phase, NamedTextColor.DARK_RED, TextDecoration.BOLD),
                Component.text(message, NamedTextColor.GRAY),
                Title.Times.times(Duration.ofMillis(200), Duration.ofSeconds(2), Duration.ofMillis(500))
            ));
            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0f, 0.5f);
        }
        bossBar.color(phase >= 3 ? BossBar.Color.RED : BossBar.Color.PURPLE);
    }

    // ==================== DAMAGE HANDLING ====================

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!fightActive || dragonModel == null) return;
        if (dragonModel.getBaseEntity() == null) return;
        if (!event.getEntity().getUniqueId().equals(dragonModel.getBaseEntity().getUniqueId())) return;

        // Block environmental damage
        if (!(event instanceof EntityDamageByEntityEvent)) {
            event.setCancelled(true);
            return;
        }

        // Apply damage reduction
        double originalDamage = event.getDamage();
        double reduced = originalDamage * (1.0 - DAMAGE_REDUCTION);
        event.setDamage(reduced);

        dragonModel.damageFlash();

        // Check if dragon died
        double newHp = dragonModel.getHealth() - reduced;
        if (newHp <= 0) {
            event.setCancelled(true); // We handle death ourselves
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (fightActive) {
                        onDragonDeath(dragonModel.getLocation().getWorld());
                    }
                }
            }.runTaskLater(plugin, 1L);
        }
    }

    // ==================== DEATH / LOOT ====================

    private void onDragonDeath(World abyss) {
        if (!fightActive) return;
        fightActive = false;
        lastFightEnd = System.currentTimeMillis();

        plugin.getLogger().info("[DragonBoss] Dragon defeated!");

        // Cancel tasks
        if (combatTaskId != -1) Bukkit.getScheduler().cancelTask(combatTaskId);
        if (movementTaskId != -1) Bukkit.getScheduler().cancelTask(movementTaskId);
        combatTaskId = -1;
        movementTaskId = -1;

        // Boss bar
        bossBar.progress(0);
        bossBar.name(Component.text("DEFEATED", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH));

        // Death animation
        if (dragonModel != null) {
            dragonModel.playDeathAnimation();
        }

        Location deathLoc = dragonModel != null && dragonModel.getLocation() != null ?
            dragonModel.getLocation() : new Location(abyss, 0, 80, AbyssDimensionManager.ARENA_CENTER_Z);

        // Victory message
        for (Player p : abyss.getPlayers()) {
            p.showTitle(Title.title(
                Component.text("VICTORY!", NamedTextColor.GOLD, TextDecoration.BOLD),
                Component.text("The Abyssal Dragon has fallen!", NamedTextColor.YELLOW),
                Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(4), Duration.ofMillis(1000))
            ));
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);

            // Fireworks
            for (int i = 0; i < 5; i++) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Location fw = deathLoc.clone().add(
                            (Math.random() - 0.5) * 20, Math.random() * 10, (Math.random() - 0.5) * 20);
                        abyss.spawnParticle(Particle.FIREWORK, fw, 50, 2, 2, 2, 0.1);
                        abyss.playSound(fw, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.0f, 1.0f);
                    }
                }.runTaskLater(plugin, 20L + i * 20L);
            }
        }

        // Drop loot after death animation
        new BukkitRunnable() {
            @Override
            public void run() {
                dropLoot(abyss, deathLoc);
                // Remove boss bar after 10 seconds
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            p.hideBossBar(bossBar);
                        }
                    }
                }.runTaskLater(plugin, 200L);

                // Clean up minions
                abyss.getEntitiesByClass(Enderman.class).forEach(e -> {
                    if (e.customName() != null) e.remove();
                });
                abyss.getEntitiesByClass(WitherSkeleton.class).forEach(e -> {
                    if (e.customName() != null) e.remove();
                });
            }
        }.runTaskLater(plugin, 80L);
    }

    private void dropLoot(World abyss, Location loc) {
        // Dragon Egg
        abyss.dropItemNaturally(loc, new ItemStack(Material.DRAGON_EGG, 1));
        // Nether Star
        abyss.dropItemNaturally(loc, new ItemStack(Material.NETHER_STAR, 2));
        // Experience
        for (int i = 0; i < 20; i++) {
            Location xpLoc = loc.clone().add((Math.random()-0.5)*5, Math.random()*3, (Math.random()-0.5)*5);
            abyss.spawn(xpLoc, ExperienceOrb.class, orb -> orb.setExperience(50));
        }

        // Abyssal weapon drop (guaranteed)
        try {
            List<LegendaryWeapon> abyssals = LegendaryWeapon.byTier(LegendaryTier.ABYSSAL);
            if (!abyssals.isEmpty()) {
                LegendaryWeapon weapon = abyssals.get((int)(Math.random() * abyssals.size()));
                ItemStack weaponItem = plugin.getLegendaryWeaponManager().createWeaponItem(weapon);
                if (weaponItem != null) {
                    abyss.dropItemNaturally(loc, weaponItem);
                    plugin.getLogger().info("[DragonBoss] Dropped ABYSSAL weapon: " + weapon.getDisplayName());
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("[DragonBoss] Failed to drop legendary weapon: " + e.getMessage());
        }

        // Extra items
        abyss.dropItemNaturally(loc, new ItemStack(Material.NETHERITE_INGOT, 3));
        abyss.dropItemNaturally(loc, new ItemStack(Material.DIAMOND, 16));
        abyss.dropItemNaturally(loc, new ItemStack(Material.EMERALD, 32));

        // Exit portal
        Location portalLoc = new Location(abyss, 0, loc.getBlockY(), AbyssDimensionManager.ARENA_CENTER_Z);
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                abyss.getBlockAt(portalLoc.getBlockX() + x, portalLoc.getBlockY(),
                    portalLoc.getBlockZ() + z).setType(Material.END_GATEWAY);
            }
        }
    }

    // ==================== UTILITIES ====================

    private List<Player> getPlayersInArena(World abyss) {
        List<Player> result = new ArrayList<>();
        Location center = new Location(abyss, 0, 0, AbyssDimensionManager.ARENA_CENTER_Z);
        for (Player p : abyss.getPlayers()) {
            double dx = p.getLocation().getX() - center.getX();
            double dz = p.getLocation().getZ() - center.getZ();
            if (Math.sqrt(dx*dx + dz*dz) <= ARENA_RADIUS + 10) {
                result.add(p);
            }
        }
        return result;
    }

    public boolean isFightActive() { return fightActive; }
}
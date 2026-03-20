package com.jglims.plugin.abyss;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.legendary.LegendaryArmorManager;
import com.jglims.plugin.legendary.LegendaryArmorSet;
import com.jglims.plugin.legendary.LegendaryTier;
import com.jglims.plugin.legendary.LegendaryWeapon;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
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
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.time.Duration;
import java.util.*;

/**
 * AbyssDragonBoss v10.0 — Complete boss fight controller.
 * v10 changes: 2000 HP, 40% dmg reduction, faster attacks, more attack types,
 *              auto-trigger on arena entry, full ABYSSAL loot table (6 nether stars,
 *              all abyssal weapons, dragon egg, full abyssal plate armor),
 *              exit portal to overworld, performance optimizations.
 */
public class AbyssDragonBoss implements Listener {

    // === Boss Stats (increased difficulty) ===
    private static final double DRAGON_HP = 2000.0;
    private static final double DAMAGE_REDUCTION = 0.40; // 40% damage reduction
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
    private int ticksSinceFightStart = 0;
    private final Set<UUID> fightParticipants = new HashSet<>();

    public AbyssDragonBoss(JGlimsPlugin plugin, AbyssDimensionManager dimensionManager) {
        this.plugin = plugin;
        this.dimensionManager = dimensionManager;
    }

    // ==================== AUTO-TRIGGER ON ARENA ENTRY ====================

    @EventHandler
    public void onPlayerMoveInAbyss(PlayerMoveEvent event) {
        // Only check block changes for performance
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockY() == event.getTo().getBlockY() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;

        Player player = event.getPlayer();
        World abyss = dimensionManager.getAbyssWorld();
        if (abyss == null) return;
        if (!player.getWorld().getName().equals(abyss.getName())) return;
        if (fightActive) return;

        // Check cooldown
        long timeSinceLastFight = System.currentTimeMillis() - lastFightEnd;
        if (timeSinceLastFight < RESPAWN_COOLDOWN_MS && lastFightEnd > 0) return;

        // Check if player entered the arena zone
        Location loc = event.getTo();
        double dx = loc.getX() - 0;
        double dz = loc.getZ() - AbyssDimensionManager.ARENA_CENTER_Z;
        double distFromCenter = Math.sqrt(dx * dx + dz * dz);

        if (distFromCenter <= ARENA_RADIUS) {
            plugin.getLogger().info("[DragonBoss] Auto-trigger: " + player.getName() +
                " entered arena (dist=" + String.format("%.1f", distFromCenter) + ")");
            startFight(player);
        }
    }

    // ==================== MANUAL TRIGGER ====================

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
        if (fightActive) return; // Prevent double-trigger
        plugin.getLogger().info("[DragonBoss] === STARTING FIGHT ===");

        World abyss = dimensionManager.getAbyssWorld();
        if (abyss == null) {
            plugin.getLogger().severe("[DragonBoss] Cannot start: abyss world is null");
            return;
        }

        // Find arena center
        int arenaY = findArenaY(abyss);
        Location spawnLoc = new Location(abyss, 0.5, arenaY + 15,
            AbyssDimensionManager.ARENA_CENTER_Z + 0.5);
        plugin.getLogger().info("[DragonBoss] Arena Y: " + arenaY + ", Spawn location: " +
            spawnLoc.getBlockX() + "," + spawnLoc.getBlockY() + "," + spawnLoc.getBlockZ());

        // Generate waypoints
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
        ticksSinceFightStart = 0;
        attackCooldown = 5; // Grace period at start (10 seconds)
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

        // 12 waypoints in a circle at varying heights
        for (int i = 0; i < 12; i++) {
            double angle = (2 * Math.PI * i) / 12;
            double x = cx + Math.cos(angle) * (ARENA_RADIUS - 5);
            double z = cz + Math.sin(angle) * (ARENA_RADIUS - 5);
            double y = arenaY + 8 + Math.random() * 18;
            waypoints.add(new Location(w, x, y, z));
        }
        // Center high point
        waypoints.add(new Location(w, cx, arenaY + 25, cz));
        // Low dive points
        waypoints.add(new Location(w, cx + 20, arenaY + 4, cz + 10));
        waypoints.add(new Location(w, cx - 20, arenaY + 4, cz - 10));
        waypoints.add(new Location(w, cx, arenaY + 4, cz + 20));

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

                ticksSinceFightStart++;

                // Update boss bar
                double maxHp = DRAGON_HP;
                double currentHp = dragonModel.getHealth();
                float progress = (float) Math.max(0, Math.min(1, currentHp / maxHp));
                bossBar.progress(progress);

                // Phase transitions
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

                // Attack logic — gets faster in higher phases
                attackCooldown--;
                if (attackCooldown <= 0) {
                    List<Player> nearby = getPlayersInArena(abyss);
                    if (!nearby.isEmpty()) {
                        Player target = nearby.get((int) (Math.random() * nearby.size()));
                        performAttack(target, abyss);
                        // Attack rate increases with phase
                        int baseCooldown = switch (dragonModel.getPhase()) {
                            case 4 -> 1; // every 2 seconds
                            case 3 -> 2; // every 4 seconds
                            case 2 -> 2; // every 4 seconds
                            default -> 3; // every 6 seconds
                        };
                        attackCooldown = baseCooldown + (int) (Math.random() * 2);
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

                // Speed increases with phase
                double speed = 0.8 + (dragonModel.getPhase() * 0.3);
                Location target = waypoints.get(currentWaypoint);
                dragonModel.moveToward(target, speed);

                Location dragonLoc = dragonModel.getLocation();
                if (dragonLoc != null && dragonLoc.distance(target) < 3.0) {
                    // 40% chance to fly toward a player
                    List<Player> nearby = getPlayersInArena(abyss);
                    if (!nearby.isEmpty() && Math.random() < 0.40) {
                        Player p = nearby.get((int) (Math.random() * nearby.size()));
                        Location playerAbove = p.getLocation().clone().add(0, 6, 0);
                        dragonModel.moveToward(playerAbove, speed * 1.5);
                    }
                    currentWaypoint = (currentWaypoint + 1) % waypoints.size();
                }
            }
        }.runTaskTimer(plugin, 10L, 10L).getTaskId();
    }

    // ==================== ATTACKS ====================

    private void performAttack(Player target, World abyss) {
        int phase = dragonModel.getPhase();
        double roll = Math.random();

        if (phase >= 4) {
            // Enrage: multi-attack combos
            if (roll < 0.25) { enrageLightningBarrage(abyss, target); }
            else if (roll < 0.45) { voidPull(abyss); voidBreath(target); }
            else if (roll < 0.60) { shadowBoltBarrage(abyss); }
            else if (roll < 0.75) { diveBomb(abyss, target); }
            else if (roll < 0.85) { groundPound(abyss); summonMinions(abyss); }
            else { deathBeam(abyss, target); }
        } else if (phase >= 3) {
            if (roll < 0.20) voidBreath(target);
            else if (roll < 0.35) groundPound(abyss);
            else if (roll < 0.50) summonMinions(abyss);
            else if (roll < 0.65) voidPull(abyss);
            else if (roll < 0.80) shadowBoltBarrage(abyss);
            else diveBomb(abyss, target);
        } else if (phase >= 2) {
            if (roll < 0.25) voidBreath(target);
            else if (roll < 0.45) wingGust(abyss);
            else if (roll < 0.65) summonMinions(abyss);
            else if (roll < 0.80) lightningStrike(abyss, target);
            else diveBomb(abyss, target);
        } else {
            if (roll < 0.30) voidBreath(target);
            else if (roll < 0.55) lightningStrike(abyss, target);
            else if (roll < 0.75) wingGust(abyss);
            else shadowBoltBarrage(abyss);
        }
    }

    // --- Core attacks ---

    private void voidBreath(Player target) {
        if (dragonModel == null) return;
        dragonModel.breathAttackVisual(target.getLocation());
        // Damage all players in a cone toward the target
        Location dragonLoc = dragonModel.getLocation();
        if (dragonLoc == null) return;
        double baseDmg = 8.0 + dragonModel.getPhase() * 3.0;
        for (Player p : getPlayersInArena(dragonLoc.getWorld())) {
            if (p.getLocation().distance(target.getLocation()) < 5) {
                p.damage(baseDmg);
            }
        }
        target.sendMessage(Component.text("The dragon's void breath burns!", NamedTextColor.DARK_PURPLE));
    }

    private void lightningStrike(World abyss, Player target) {
        // Strike the target and 2 nearby locations
        abyss.strikeLightningEffect(target.getLocation());
        target.damage(8.0);
        for (int i = 0; i < 2; i++) {
            Location nearby = target.getLocation().add(
                (Math.random() - 0.5) * 6, 0, (Math.random() - 0.5) * 6);
            abyss.strikeLightningEffect(nearby);
            for (Player p : getPlayersInArena(abyss)) {
                if (p.getLocation().distance(nearby) < 3) p.damage(5.0);
            }
        }
    }

    private void wingGust(World abyss) {
        if (dragonModel == null) return;
        dragonModel.wingGustVisual();
        Location dragonLoc = dragonModel.getLocation();
        if (dragonLoc == null) return;

        for (Player p : getPlayersInArena(abyss)) {
            Vector knockback = p.getLocation().toVector()
                .subtract(dragonLoc.toVector()).normalize().multiply(3.0);
            knockback.setY(1.0);
            p.setVelocity(knockback);
            p.damage(6.0);
        }
    }

    private void groundPound(World abyss) {
        if (dragonModel == null) return;
        Location loc = dragonModel.getLocation();
        if (loc == null) return;

        // Visual effects
        for (int ring = 0; ring < 3; ring++) {
            final int r = ring;
            new BukkitRunnable() {
                @Override
                public void run() {
                    double radius = 5 + r * 5;
                    for (double angle = 0; angle < 2 * Math.PI; angle += 0.3) {
                        Location p = loc.clone().add(
                            Math.cos(angle) * radius, 0, Math.sin(angle) * radius);
                        abyss.spawnParticle(Particle.EXPLOSION, p, 1, 0, 0, 0, 0);
                    }
                    abyss.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.5f + r * 0.2f);
                }
            }.runTaskLater(plugin, r * 5L);
        }

        for (Player p : getPlayersInArena(abyss)) {
            if (p.getLocation().distance(loc) < 18) {
                p.damage(12.0);
                Vector up = new Vector(0, 1.8, 0);
                p.setVelocity(p.getVelocity().add(up));
            }
        }
    }

    private void voidPull(World abyss) {
        if (dragonModel == null) return;
        Location dragonLoc = dragonModel.getLocation();
        if (dragonLoc == null) return;

        abyss.playSound(dragonLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 2.0f, 0.3f);
        abyss.playSound(dragonLoc, Sound.ENTITY_WARDEN_SONIC_BOOM, 0.5f, 0.5f);

        for (Player p : getPlayersInArena(abyss)) {
            Vector pull = dragonLoc.toVector().subtract(p.getLocation().toVector()).normalize().multiply(1.2);
            p.setVelocity(p.getVelocity().add(pull));
            p.damage(5.0);
            p.sendMessage(Component.text("The void pulls you toward the dragon!",
                NamedTextColor.DARK_PURPLE));
        }
    }

    private void summonMinions(World abyss) {
        if (dragonModel == null) return;
        Location loc = dragonModel.getLocation();
        if (loc == null) return;

        int count = Math.min(3 + dragonModel.getPhase() * 2, 10);
        for (int i = 0; i < count; i++) {
            Location spawn = loc.clone().add(
                (Math.random() - 0.5) * 20, -5, (Math.random() - 0.5) * 20);

            double r = Math.random();
            if (r < 0.35) {
                Enderman e = abyss.spawn(spawn, Enderman.class);
                e.customName(Component.text("Void Minion", NamedTextColor.DARK_PURPLE));
            } else if (r < 0.65) {
                WitherSkeleton ws = abyss.spawn(spawn, WitherSkeleton.class);
                ws.customName(Component.text("Abyss Guard", NamedTextColor.DARK_RED));
            } else {
                Phantom ph = abyss.spawn(spawn.clone().add(0, 10, 0), Phantom.class);
                ph.customName(Component.text("Void Phantom", NamedTextColor.DARK_GRAY));
                ph.setSize(3);
            }
        }

        abyss.playSound(loc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.5f, 0.5f);
        for (Player p : abyss.getPlayers()) {
            p.sendMessage(Component.text("Minions emerge from the void!", NamedTextColor.DARK_PURPLE));
        }
    }

    // --- New attacks ---

    private void diveBomb(World abyss, Player target) {
        if (dragonModel == null) return;
        Location targetLoc = target.getLocation().clone();

        // Warning
        for (Player p : getPlayersInArena(abyss)) {
            if (p.getLocation().distance(targetLoc) < 10) {
                p.sendMessage(Component.text("The dragon prepares to dive!", NamedTextColor.RED));
            }
        }

        // Mark impact zone
        new BukkitRunnable() {
            int t = 0;
            @Override
            public void run() {
                t++;
                // Draw circle warning on ground
                for (double angle = 0; angle < 2 * Math.PI; angle += 0.5) {
                    Location ring = targetLoc.clone().add(Math.cos(angle) * 6, 0.5, Math.sin(angle) * 6);
                    abyss.spawnParticle(Particle.DUST, ring, 1, 0, 0, 0, 0,
                        new Particle.DustOptions(Color.RED, 2));
                }
                if (t >= 10) { // 1 second warning
                    // Impact
                    dragonModel.diveBombVisual(targetLoc);
                    for (Player p : getPlayersInArena(abyss)) {
                        double dist = p.getLocation().distance(targetLoc);
                        if (dist < 8) {
                            double dmg = 15.0 * (1.0 - dist / 8.0); // More damage closer
                            p.damage(Math.max(5.0, dmg));
                            Vector kb = p.getLocation().toVector()
                                .subtract(targetLoc.toVector()).normalize().multiply(2.0);
                            kb.setY(1.0);
                            p.setVelocity(kb);
                        }
                    }
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    private void shadowBoltBarrage(World abyss) {
        if (dragonModel == null) return;
        Location dragonLoc = dragonModel.getDisplayLocation();
        if (dragonLoc == null) dragonLoc = dragonModel.getLocation();
        if (dragonLoc == null) return;

        List<Player> targets = getPlayersInArena(abyss);
        if (targets.isEmpty()) return;

        final Location origin = dragonLoc.clone();
        int bolts = 3 + dragonModel.getPhase();
        for (int i = 0; i < bolts; i++) {
            final int delay = i * 4;
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!fightActive) return;
                    List<Player> current = getPlayersInArena(abyss);
                    if (current.isEmpty()) return;
                    Player t = current.get((int) (Math.random() * current.size()));
                    dragonModel.shadowBoltVisual(origin, t.getLocation());
                    t.damage(6.0 + dragonModel.getPhase());
                    abyss.playSound(t.getLocation(), Sound.ENTITY_SHULKER_BULLET_HIT, 1.0f, 0.5f);
                }
            }.runTaskLater(plugin, delay);
        }
    }

    private void deathBeam(World abyss, Player target) {
        if (dragonModel == null) return;
        Location dragonLoc = dragonModel.getDisplayLocation();
        if (dragonLoc == null) return;

        // Charge-up warning
        abyss.playSound(dragonLoc, Sound.ENTITY_WARDEN_SONIC_CHARGE, 2.0f, 0.5f);
        for (Player p : getPlayersInArena(abyss)) {
            p.sendMessage(Component.text("The dragon charges a devastating beam!",
                TextColor.color(170, 0, 0)));
        }

        // Fire after 1.5 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!fightActive || dragonModel == null) return;
                Location from = dragonModel.getDisplayLocation();
                if (from == null) return;
                Location to = target.getLocation();

                // Beam visual
                double dx = to.getX() - from.getX();
                double dy = to.getY() - from.getY();
                double dz = to.getZ() - from.getZ();
                double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

                for (double t = 0; t < dist; t += 0.3) {
                    double ratio = t / dist;
                    Location point = from.clone().add(dx * ratio, dy * ratio, dz * ratio);
                    abyss.spawnParticle(Particle.SOUL_FIRE_FLAME, point, 3, 0.15, 0.15, 0.15, 0.01);
                    abyss.spawnParticle(Particle.DRAGON_BREATH, point, 2, 0.1, 0.1, 0.1, 0.01);
                }

                abyss.playSound(from, Sound.ENTITY_WARDEN_SONIC_BOOM, 2.0f, 0.5f);

                // Damage all players along the beam line
                for (Player p : getPlayersInArena(abyss)) {
                    // Check distance from player to the beam line
                    Location pl = p.getLocation();
                    Vector beamDir = to.toVector().subtract(from.toVector()).normalize();
                    Vector toPlayer = pl.toVector().subtract(from.toVector());
                    double dot = toPlayer.dot(beamDir);
                    if (dot > 0 && dot < dist) {
                        Vector closest = from.toVector().add(beamDir.multiply(dot));
                        double distToBeam = pl.toVector().distance(closest);
                        if (distToBeam < 3.0) {
                            p.damage(20.0);
                            p.sendMessage(Component.text("The death beam tears through you!",
                                TextColor.color(170, 0, 0)));
                        }
                    }
                }
            }
        }.runTaskLater(plugin, 30L);
    }

    private void enrageLightningBarrage(World abyss, Player target) {
        new BukkitRunnable() {
            int strikes = 0;

            @Override
            public void run() {
                if (strikes >= 12 || !fightActive) {
                    cancel();
                    return;
                }
                Location loc = target.getLocation().add(
                    (Math.random() - 0.5) * 12, 0, (Math.random() - 0.5) * 12);
                abyss.strikeLightningEffect(loc);
                for (Player p : getPlayersInArena(abyss)) {
                    if (p.getLocation().distance(loc) < 3) p.damage(6.0);
                }
                strikes++;
            }
        }.runTaskTimer(plugin, 0L, 4L);
    }

    private void phaseAnnounce(World abyss, int phase, String message) {
        NamedTextColor titleColor = phase >= 4 ? NamedTextColor.DARK_RED : NamedTextColor.RED;
        for (Player p : abyss.getPlayers()) {
            p.showTitle(Title.title(
                Component.text("PHASE " + phase, titleColor, TextDecoration.BOLD),
                Component.text(message, NamedTextColor.GRAY),
                Title.Times.times(Duration.ofMillis(200), Duration.ofSeconds(2), Duration.ofMillis(500))
            ));
            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0f, 0.5f);
            if (phase >= 4) {
                p.playSound(p.getLocation(), Sound.ENTITY_WARDEN_ROAR, 1.0f, 0.5f);
            }
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

        // Track who attacked
        EntityDamageByEntityEvent damageByEntity = (EntityDamageByEntityEvent) event;
        if (damageByEntity.getDamager() instanceof Player attacker) {
            fightParticipants.add(attacker.getUniqueId());
        }

        // Check if dragon died
        double newHp = dragonModel.getHealth() - reduced;
        if (newHp <= 0) {
            event.setCancelled(true);
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
            dragonModel.getLocation() :
            new Location(abyss, 0, 80, AbyssDimensionManager.ARENA_CENTER_Z);

        // Victory message & effects
        for (Player p : abyss.getPlayers()) {
            p.showTitle(Title.title(
                Component.text("VICTORY!", NamedTextColor.GOLD, TextDecoration.BOLD),
                Component.text("The Abyssal Dragon has fallen!", NamedTextColor.YELLOW),
                Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(4), Duration.ofMillis(1000))
            ));
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);

            // Fireworks
            for (int i = 0; i < 8; i++) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Location fw = deathLoc.clone().add(
                            (Math.random() - 0.5) * 20, Math.random() * 10,
                            (Math.random() - 0.5) * 20);
                        abyss.spawnParticle(Particle.FIREWORK, fw, 50, 2, 2, 2, 0.1);
                        abyss.playSound(fw, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.0f, 1.0f);
                    }
                }.runTaskLater(plugin, 20L + i * 15L);
            }
        }

        // Drop loot after death animation
        new BukkitRunnable() {
            @Override
            public void run() {
                dropLoot(abyss, deathLoc);
                spawnExitPortal(abyss, deathLoc);

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
                for (Entity e : abyss.getEntities()) {
                    if (e instanceof Enderman || e instanceof WitherSkeleton || e instanceof Phantom) {
                        if (e.customName() != null) e.remove();
                    }
                }
            }
        }.runTaskLater(plugin, 80L);
    }

    private void dropLoot(World abyss, Location loc) {
        plugin.getLogger().info("[DragonBoss] === DROPPING LOOT ===");

        // === Dragon Egg (1) ===
        abyss.dropItemNaturally(loc, new ItemStack(Material.DRAGON_EGG, 1));
        plugin.getLogger().info("[DragonBoss] Dropped: Dragon Egg x1");

        // === Nether Stars (6) ===
        abyss.dropItemNaturally(loc, new ItemStack(Material.NETHER_STAR, 6));
        plugin.getLogger().info("[DragonBoss] Dropped: Nether Star x6");

        // === Experience (1000 XP total) ===
        for (int i = 0; i < 20; i++) {
            Location xpLoc = loc.clone().add(
                (Math.random() - 0.5) * 5, Math.random() * 3, (Math.random() - 0.5) * 5);
            abyss.spawn(xpLoc, ExperienceOrb.class, orb -> orb.setExperience(50));
        }

        // === ALL Abyssal weapons ===
        try {
            LegendaryWeapon[] abyssals = LegendaryWeapon.byTier(LegendaryTier.ABYSSAL);
            for (LegendaryWeapon weapon : abyssals) {
                ItemStack weaponItem = plugin.getLegendaryWeaponManager().createWeapon(weapon);
                if (weaponItem != null) {
                    abyss.dropItemNaturally(loc, weaponItem);
                    plugin.getLogger().info("[DragonBoss] Dropped ABYSSAL weapon: " +
                        weapon.getDisplayName());
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("[DragonBoss] Failed to drop legendary weapons: " +
                e.getMessage());
        }

        // === Full Abyssal Plate armor set ===
        try {
            LegendaryArmorManager armorManager = plugin.getLegendaryArmorManager();
            for (LegendaryArmorSet.ArmorSlot slot : LegendaryArmorSet.ArmorSlot.values()) {
                ItemStack armorPiece = armorManager.createArmorPiece(
                    LegendaryArmorSet.ABYSSAL_PLATE, slot);
                if (armorPiece != null) {
                    abyss.dropItemNaturally(loc, armorPiece);
                    plugin.getLogger().info("[DragonBoss] Dropped ABYSSAL armor: " +
                        LegendaryArmorSet.ABYSSAL_PLATE.getDisplayName() + " " + slot.name());
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("[DragonBoss] Failed to drop legendary armor: " +
                e.getMessage());
        }

        // === Bonus materials ===
        abyss.dropItemNaturally(loc, new ItemStack(Material.NETHERITE_INGOT, 5));
        abyss.dropItemNaturally(loc, new ItemStack(Material.DIAMOND, 32));
        abyss.dropItemNaturally(loc, new ItemStack(Material.EMERALD, 64));
        abyss.dropItemNaturally(loc, new ItemStack(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE, 2));

        // === Beacon effect at death location ===
        new BukkitRunnable() {
            int t = 0;
            @Override
            public void run() {
                t++;
                if (t > 100 || abyss.getPlayers().isEmpty()) { cancel(); return; }
                // Beacon-style light beam particles
                for (double y = 0; y < 30; y += 1.0) {
                    Location beam = loc.clone().add(0, y, 0);
                    abyss.spawnParticle(Particle.END_ROD, beam, 2, 0.1, 0, 0.1, 0.01);
                }
            }
        }.runTaskTimer(plugin, 0L, 5L);

        plugin.getLogger().info("[DragonBoss] === LOOT DROP COMPLETE ===");
    }

    // ==================== EXIT PORTAL ====================

    private void spawnExitPortal(World abyss, Location deathLoc) {
        // Place a 3x3 end gateway pad at ground level near the death location
        int groundY = findArenaY(abyss);
        Location portalCenter = new Location(abyss, 0, groundY + 1,
            AbyssDimensionManager.ARENA_CENTER_Z);

        plugin.getLogger().info("[DragonBoss] Spawning exit portal at " +
            portalCenter.getBlockX() + "," + portalCenter.getBlockY() + "," +
            portalCenter.getBlockZ());

        // Build a small platform + portal
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                // Obsidian border
                abyss.getBlockAt(portalCenter.getBlockX() + x,
                    portalCenter.getBlockY() - 1,
                    portalCenter.getBlockZ() + z).setType(Material.OBSIDIAN);
            }
        }
        // Inner 3x3 end gateway
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                abyss.getBlockAt(portalCenter.getBlockX() + x,
                    portalCenter.getBlockY(),
                    portalCenter.getBlockZ() + z).setType(Material.END_GATEWAY);
            }
        }

        // Effects
        abyss.playSound(portalCenter, Sound.BLOCK_END_PORTAL_SPAWN, 1.0f, 1.0f);
        abyss.spawnParticle(Particle.PORTAL, portalCenter, 200, 2, 2, 2, 0.5);
        abyss.spawnParticle(Particle.REVERSE_PORTAL, portalCenter, 100, 1, 1, 1, 0.1);

        // Announce
        for (Player p : abyss.getPlayers()) {
            p.sendMessage(Component.text(""));
            p.sendMessage(Component.text("A portal to the Overworld has appeared!",
                NamedTextColor.GREEN, TextDecoration.BOLD));
            p.sendMessage(Component.text("Step into the gateway to return.",
                NamedTextColor.GRAY));
            p.sendMessage(Component.text(""));
        }

        // Register a listener for stepping on the end gateway
        // to teleport players to the overworld
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                ticks++;
                // Portal lasts 5 minutes
                if (ticks > 6000 || abyss.getPlayers().isEmpty()) {
                    // Remove portal
                    for (int x = -1; x <= 1; x++) {
                        for (int z = -1; z <= 1; z++) {
                            abyss.getBlockAt(portalCenter.getBlockX() + x,
                                portalCenter.getBlockY(),
                                portalCenter.getBlockZ() + z).setType(Material.AIR);
                        }
                    }
                    cancel();
                    return;
                }

                // Check for players on the portal
                for (Player p : abyss.getPlayers()) {
                    Location pLoc = p.getLocation();
                    if (Math.abs(pLoc.getBlockX() - portalCenter.getBlockX()) <= 1 &&
                        Math.abs(pLoc.getBlockZ() - portalCenter.getBlockZ()) <= 1 &&
                        Math.abs(pLoc.getBlockY() - portalCenter.getBlockY()) <= 2) {

                        // Teleport to overworld spawn
                        World overworld = Bukkit.getWorlds().get(0); // main world
                        Location spawn = overworld.getSpawnLocation().add(0.5, 0, 0.5);
                        p.teleport(spawn);
                        p.sendMessage(Component.text("You have returned from the Abyss.",
                            NamedTextColor.GREEN));
                        p.playSound(spawn, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                        p.hideBossBar(bossBar);
                    }
                }

                // Particles on portal
                if (ticks % 10 == 0) {
                    abyss.spawnParticle(Particle.PORTAL, portalCenter, 30, 1, 0.5, 1, 0.3);
                    abyss.spawnParticle(Particle.END_ROD, portalCenter.clone().add(0, 1, 0),
                        10, 0.5, 1, 0.5, 0.02);
                }
            }
        }.runTaskTimer(plugin, 20L, 1L);
    }

    // ==================== UTILITIES ====================

    private List<Player> getPlayersInArena(World abyss) {
        List<Player> result = new ArrayList<>();
        if (abyss == null) return result;
        Location center = new Location(abyss, 0, 0, AbyssDimensionManager.ARENA_CENTER_Z);
        for (Player p : abyss.getPlayers()) {
            double dx = p.getLocation().getX() - center.getX();
            double dz = p.getLocation().getZ() - center.getZ();
            if (Math.sqrt(dx * dx + dz * dz) <= ARENA_RADIUS + 10) {
                result.add(p);
            }
        }
        return result;
    }

    public boolean isFightActive() { return fightActive; }
}
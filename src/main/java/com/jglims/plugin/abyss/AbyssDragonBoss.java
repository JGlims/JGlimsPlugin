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
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.time.Duration;
import java.util.*;

/**
 * AbyssDragonBoss v10.0 — Complete boss fight controller.
 * Fixed: arena detection, spawn safety, damage race condition, arena platform.
 */
public class AbyssDragonBoss implements Listener {

    private static final double DRAGON_HP = 1500.0;
    private static final double DAMAGE_REDUCTION = 0.25;
    private static final int ARENA_RADIUS = 55;
    private static final long RESPAWN_COOLDOWN_MS = 30 * 60 * 1000; // 30 min

    private final JGlimsPlugin plugin;
    private final AbyssDimensionManager dimensionManager;

    private AbyssDragonModel dragonModel;
    private BossBar bossBar;
    private boolean fightActive = false;
    private long lastFightEnd = 0;

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

        plugin.getLogger().info("[DragonBoss] Abyss world name: " + abyss.getName());

        if (!player.getWorld().getName().equals(abyss.getName())) {
            player.sendMessage(Component.text("You must be in the Abyss! Use /jglims abyss tp first.", NamedTextColor.RED));
            plugin.getLogger().warning("[DragonBoss] Player not in Abyss. Player world: " +
                player.getWorld().getName() + ", Abyss world: " + abyss.getName());
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

        player.sendMessage(Component.text("Summoning the Abyssal Dragon...", NamedTextColor.DARK_PURPLE));
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

        // Find arena center — use player's location as fallback
        int arenaY = findArenaY(abyss);
        plugin.getLogger().info("[DragonBoss] Detected arena Y: " + arenaY);

        // SAFETY: Ensure there's a solid platform at the arena
        ensureArenaPlatform(abyss, arenaY);

        Location spawnLoc = new Location(abyss, 0.5, arenaY + 15, AbyssDimensionManager.ARENA_CENTER_Z + 0.5);
        plugin.getLogger().info("[DragonBoss] Dragon spawn location: " +
            spawnLoc.getBlockX() + "," + spawnLoc.getBlockY() + "," + spawnLoc.getBlockZ());

        // Generate waypoints
        generateWaypoints(spawnLoc, arenaY);

        // Create dragon model
        dragonModel = new AbyssDragonModel(plugin);
        boolean spawned = dragonModel.spawn(spawnLoc, DRAGON_HP);

        if (!spawned) {
            plugin.getLogger().severe("[DragonBoss] Dragon model failed to spawn! Check [DragonModel] logs above.");
            trigger.sendMessage(Component.text(
                "The dragon could not manifest! Check server logs.", NamedTextColor.RED));
            trigger.sendMessage(Component.text(
                "Common fix: spigot.yml attribute.maxHealth.max must be >= 2048", NamedTextColor.YELLOW));
            return;
        }

        plugin.getLogger().info("[DragonBoss] Dragon model spawned successfully! HP: " + dragonModel.getHealth());

        // Transition to fly animation after spawn animation finishes (~5s)
        final Location spawnLocFinal = spawnLoc;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (dragonModel != null && dragonModel.isAlive()) {
                    dragonModel.playFlyAnimation(spawnLocFinal);
                }
            }
        }.runTaskLater(plugin, 100L);

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

        plugin.getLogger().info("[DragonBoss] Fight fully initialized! Dragon alive: " + dragonModel.isAlive());
    }

    /**
     * Finds the arena floor Y level by scanning downward at the arena center.
     * Falls back to player-relative Y or absolute 64 if nothing found.
     */
    private int findArenaY(World abyss) {
        int centerZ = AbyssDimensionManager.ARENA_CENTER_Z;

        // Scan from Y=150 down to Y=1 at several positions around arena center
        int[][] checkPositions = {{0, centerZ}, {5, centerZ}, {-5, centerZ}, {0, centerZ+5}, {0, centerZ-5}};

        for (int[] pos : checkPositions) {
            for (int y = 150; y > 1; y--) {
                Material mat = abyss.getBlockAt(pos[0], y, pos[1]).getType();
                if (mat.isSolid() && mat != Material.BARRIER) {
                    plugin.getLogger().info("[DragonBoss] Found arena floor at Y=" + y +
                        " (block: " + mat + " at " + pos[0] + "," + y + "," + pos[1] + ")");
                    return y;
                }
            }
        }

        // Fallback: scan where the terrain generator places ground (~Y50)
        for (int y = 80; y > 30; y--) {
            Material mat = abyss.getBlockAt(0, y, 0).getType();
            if (mat.isSolid()) {
                plugin.getLogger().info("[DragonBoss] No arena floor found at z=" + centerZ +
                    ". Using terrain level Y=" + y + " at origin.");
                return y;
            }
        }

        plugin.getLogger().warning("[DragonBoss] No solid ground found anywhere! Using default Y=50");
        return 50;
    }

    /**
     * Ensures there's a bedrock platform at the arena center so the fight
     * can proceed even if the citadel hasn't built properly.
     */
    private void ensureArenaPlatform(World abyss, int floorY) {
        int centerZ = AbyssDimensionManager.ARENA_CENTER_Z;
        Material floorCheck = abyss.getBlockAt(0, floorY, centerZ).getType();

        // If the floor is already solid, we're good
        if (floorCheck.isSolid() && floorCheck != Material.BARRIER) {
            plugin.getLogger().info("[DragonBoss] Arena floor verified: " + floorCheck + " at Y=" + floorY);
            return;
        }

        // Build emergency bedrock platform
        plugin.getLogger().warning("[DragonBoss] Arena floor not solid! Building emergency platform at Y=" + floorY);
        for (int x = -ARENA_RADIUS; x <= ARENA_RADIUS; x++) {
            for (int z = centerZ - ARENA_RADIUS; z <= centerZ + ARENA_RADIUS; z++) {
                double dist = Math.sqrt(x * x + (z - centerZ) * (z - centerZ));
                if (dist <= ARENA_RADIUS) {
                    abyss.getBlockAt(x, floorY, z).setType(Material.BEDROCK);
                }
            }
        }
        // Clear air above for the fight
        for (int x = -ARENA_RADIUS; x <= ARENA_RADIUS; x++) {
            for (int z = centerZ - ARENA_RADIUS; z <= centerZ + ARENA_RADIUS; z++) {
                double dist = Math.sqrt(x * x + (z - centerZ) * (z - centerZ));
                if (dist <= ARENA_RADIUS) {
                    for (int y = floorY + 1; y <= floorY + 40; y++) {
                        if (abyss.getBlockAt(x, y, z).getType().isSolid()) {
                            abyss.getBlockAt(x, y, z).setType(Material.AIR);
                        }
                    }
                }
            }
        }
        plugin.getLogger().info("[DragonBoss] Emergency arena platform built.");
    }

    private void generateWaypoints(Location center, int arenaY) {
        waypoints.clear();
        double cx = center.getX();
        double cz = center.getZ();
        World w = center.getWorld();

        for (int i = 0; i < 8; i++) {
            double angle = (2 * Math.PI * i) / 8;
            double x = cx + Math.cos(angle) * (ARENA_RADIUS - 5);
            double z = cz + Math.sin(angle) * (ARENA_RADIUS - 5);
            double y = arenaY + 10 + Math.random() * 15;
            waypoints.add(new Location(w, x, y, z));
        }
        waypoints.add(new Location(w, cx, arenaY + 25, cz));
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
                    plugin.getLogger().info("[DragonBoss] Combat loop ended. fightActive=" + fightActive +
                        " dragonAlive=" + (dragonModel != null ? dragonModel.isAlive() : "null"));
                    if (fightActive && (dragonModel == null || !dragonModel.isAlive())) {
                        // Dragon entity died unexpectedly
                        onDragonDeath(abyss);
                    }
                    cancel();
                    return;
                }

                double maxHp = DRAGON_HP;
                double currentHp = dragonModel.getHealth();
                float progress = (float) Math.max(0, Math.min(1, currentHp / maxHp));
                bossBar.progress(progress);

                double hpPercent = currentHp / maxHp;
                if (hpPercent <= 0.10 && dragonModel.getPhase() < 4) {
                    dragonModel.setPhase(4);
                    dragonModel.triggerAttack("special_2");
                    phaseAnnounce(abyss, 4, "ENRAGED — THE ABYSS CONSUMES ALL");
                } else if (hpPercent <= 0.25 && dragonModel.getPhase() < 3) {
                    dragonModel.setPhase(3);
                    dragonModel.triggerAttack("special_2");
                    phaseAnnounce(abyss, 3, "The dragon unleashes its true power!");
                } else if (hpPercent <= 0.50 && dragonModel.getPhase() < 2) {
                    dragonModel.setPhase(2);
                    dragonModel.triggerAttack("special_2");
                    phaseAnnounce(abyss, 2, "The dragon enters a rage!");
                }

                for (Player p : abyss.getPlayers()) {
                    fightParticipants.add(p.getUniqueId());
                    p.showBossBar(bossBar);
                }

                attackCooldown--;
                if (attackCooldown <= 0) {
                    List<Player> nearby = getPlayersInArena(abyss);
                    if (!nearby.isEmpty()) {
                        Player target = nearby.get((int)(Math.random() * nearby.size()));
                        performAttack(target, abyss);
                        attackCooldown = 3 + (int)(Math.random() * 3);
                    }
                }

                if (currentHp <= 0) {
                    onDragonDeath(abyss);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 40L, 40L).getTaskId();
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
                dragonModel.playFlyAnimation(target);
                dragonModel.moveToward(target, 0.8 + (dragonModel.getPhase() * 0.2));

                Location dragonLoc = dragonModel.getLocation();
                if (dragonLoc != null && dragonLoc.distance(target) < 3.0) {
                    List<Player> nearby = getPlayersInArena(abyss);
                    if (!nearby.isEmpty() && Math.random() < 0.3) {
                        Player p = nearby.get((int)(Math.random() * nearby.size()));
                        Location playerAbove = p.getLocation().clone().add(0, 8, 0);
                        dragonModel.moveToward(playerAbove, 1.2);
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
        dragonModel.triggerAttack("fire_breath", () ->
            dragonModel.playAnimation("fly", AbyssDragonModel.FLY_MOD));
        dragonModel.breathAttackVisual(target.getLocation());
        target.damage(8.0 + dragonModel.getPhase() * 2.0);
        target.sendMessage(Component.text("The dragon's void breath burns!", NamedTextColor.DARK_PURPLE));
    }

    private void lightningStrike(World abyss, Player target) {
        if (dragonModel != null) {
            dragonModel.triggerAttack("special_1");
        }
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

        dragonModel.triggerAttack("special_2");
        abyss.spawnParticle(Particle.EXPLOSION_EMITTER, loc, 3, 2, 0, 2, 0.1);
        abyss.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.5f);

        for (Player p : getPlayersInArena(abyss)) {
            if (p.getLocation().distance(loc) < 15) {
                p.damage(10.0);
                p.setVelocity(p.getVelocity().add(new Vector(0, 1.5, 0)));
            }
        }
    }

    private void voidPull(World abyss) {
        if (dragonModel == null) return;
        Location dragonLoc = dragonModel.getLocation();
        if (dragonLoc == null) return;

        dragonModel.triggerAttack("attack");
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

        dragonModel.triggerAttack("special_1");
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
        if (dragonModel != null) {
            dragonModel.triggerAttack("special_2");
        }
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

    // ==================== AUTO-TRIGGER ON ARENA ENTRY ====================
    //
    // Detects when a player steps into the Abyss arena and automatically
    // awakens the dragon (respecting the respawn cooldown). Previously the
    // fight could only be started via `/jglims boss ABYSS_DRAGON`, which
    // caused players who walked into the arena to see only the stage,
    // the wither skeleton guards, and the leftover gateway exit portal —
    // no dragon.
    //
    // Performance: the event fires on every head rotation too, so we cheap
    // out by bailing when the player's block coordinates don't change. The
    // actual distance check is also cheap (no sqrt).

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMoveIntoArena(PlayerMoveEvent event) {
        if (fightActive) return;
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;
        Player player = event.getPlayer();
        World abyss = dimensionManager.getAbyssWorld();
        if (abyss == null || !player.getWorld().getName().equals(abyss.getName())) return;
        // Cheap distance-squared check against arena center
        int dx = event.getTo().getBlockX();
        int dz = event.getTo().getBlockZ() - AbyssDimensionManager.ARENA_CENTER_Z;
        int distSq = dx * dx + dz * dz;
        int triggerRadius = ARENA_RADIUS - 2;
        if (distSq > triggerRadius * triggerRadius) return;
        // Cooldown check — silent abort (no spam)
        long timeSinceLastFight = System.currentTimeMillis() - lastFightEnd;
        if (timeSinceLastFight < RESPAWN_COOLDOWN_MS && lastFightEnd > 0) return;
        // Everything checks out — summon the dragon
        plugin.getLogger().info("[DragonBoss] Auto-trigger: " + player.getName() + " entered the arena.");
        startFight(player);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!fightActive || dragonModel == null) return;
        if (dragonModel.getBaseEntity() == null || dragonModel.getBaseEntity().isDead()) return;
        if (!event.getEntity().getUniqueId().equals(dragonModel.getBaseEntity().getUniqueId())) return;

        // Block environmental damage (fire, fall, suffocation, etc.)
        if (!(event instanceof EntityDamageByEntityEvent)) {
            event.setCancelled(true);
            return;
        }

        EntityDamageByEntityEvent dmgEvent = (EntityDamageByEntityEvent) event;

        // Only count damage from players (not from minions hitting the zombie)
        Entity damager = dmgEvent.getDamager();
        if (damager instanceof Projectile proj && proj.getShooter() instanceof Player) {
            // Projectile from player — allow
        } else if (!(damager instanceof Player)) {
            event.setCancelled(true);
            return;
        }

        // Apply damage reduction
        double originalDamage = event.getDamage();
        double reduced = originalDamage * (1.0 - DAMAGE_REDUCTION);
        event.setDamage(reduced);

        dragonModel.damageFlash();

        // Track participant
        if (damager instanceof Player p) {
            fightParticipants.add(p.getUniqueId());
        }

        // Schedule death check for next tick to avoid race condition
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!fightActive || dragonModel == null) return;
                double hp = dragonModel.getHealth();
                if (hp <= 0) {
                    onDragonDeath(dragonModel.getLocation() != null ?
                        dragonModel.getLocation().getWorld() : dimensionManager.getAbyssWorld());
                }
            }
        }.runTaskLater(plugin, 1L);
    }

    // ==================== DEATH / LOOT ====================

    private void onDragonDeath(World abyss) {
        if (!fightActive) return;
        fightActive = false;
        lastFightEnd = System.currentTimeMillis();

        plugin.getLogger().info("[DragonBoss] Dragon defeated!");

        if (combatTaskId != -1) Bukkit.getScheduler().cancelTask(combatTaskId);
        if (movementTaskId != -1) Bukkit.getScheduler().cancelTask(movementTaskId);
        combatTaskId = -1;
        movementTaskId = -1;

        bossBar.progress(0);
        bossBar.name(Component.text("DEFEATED", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH));

        Location deathLoc = dragonModel != null && dragonModel.getLocation() != null ?
            dragonModel.getLocation() : new Location(abyss, 0, 80, AbyssDimensionManager.ARENA_CENTER_Z);

        final Location deathLocFinal = deathLoc;
        if (dragonModel != null) {
            dragonModel.deathSequence(() -> {
                dropLoot(abyss, deathLocFinal);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (bossBar != null) {
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                p.hideBossBar(bossBar);
                            }
                        }
                        abyss.getEntitiesByClass(Enderman.class).forEach(e -> {
                            if (e.customName() != null) e.remove();
                        });
                        abyss.getEntitiesByClass(WitherSkeleton.class).forEach(e -> {
                            if (e.customName() != null) e.remove();
                        });
                    }
                }.runTaskLater(plugin, 200L);
            });
        }

        for (Player p : abyss.getPlayers()) {
            p.showTitle(Title.title(
                Component.text("VICTORY!", NamedTextColor.GOLD, TextDecoration.BOLD),
                Component.text("The Abyssal Dragon has fallen!", NamedTextColor.YELLOW),
                Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(4), Duration.ofMillis(1000))
            ));
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);

            for (int i = 0; i < 5; i++) {
                final int delay = i;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Location fw = deathLoc.clone().add(
                            (Math.random() - 0.5) * 20, Math.random() * 10, (Math.random() - 0.5) * 20);
                        abyss.spawnParticle(Particle.FIREWORK, fw, 50, 2, 2, 2, 0.1);
                        abyss.playSound(fw, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.0f, 1.0f);
                    }
                }.runTaskLater(plugin, 20L + delay * 20L);
            }
        }

        // Loot drop and cleanup are now handled by deathSequence callback above.
        // (Fallback: if dragonModel was null, drop immediately so loot is never lost.)
        if (dragonModel == null) {
            dropLoot(abyss, deathLoc);
            if (bossBar != null) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.hideBossBar(bossBar);
                }
            }
        }
    }

    private void dropLoot(World abyss, Location loc) {
        abyss.dropItemNaturally(loc, new ItemStack(Material.DRAGON_EGG, 1));
        abyss.dropItemNaturally(loc, new ItemStack(Material.NETHER_STAR, 2));

        for (int i = 0; i < 20; i++) {
            Location xpLoc = loc.clone().add((Math.random()-0.5)*5, Math.random()*3, (Math.random()-0.5)*5);
            abyss.spawn(xpLoc, ExperienceOrb.class, orb -> orb.setExperience(50));
        }

        // Guaranteed ABYSSAL weapon drop
        try {
            LegendaryWeapon[] abyssals = LegendaryWeapon.byTier(LegendaryTier.ABYSSAL);
            if (abyssals.length > 0) {
                LegendaryWeapon weapon = abyssals[(int)(Math.random() * abyssals.length)];
                ItemStack weaponItem = plugin.getLegendaryWeaponManager().createWeapon(weapon);
                if (weaponItem != null) {
                    abyss.dropItemNaturally(loc, weaponItem);
                    plugin.getLogger().info("[DragonBoss] Dropped ABYSSAL weapon: " + weapon.getDisplayName());
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("[DragonBoss] Failed to drop legendary weapon: " + e.getMessage());
        }

        abyss.dropItemNaturally(loc, new ItemStack(Material.NETHERITE_INGOT, 3));
        abyss.dropItemNaturally(loc, new ItemStack(Material.DIAMOND, 16));
        abyss.dropItemNaturally(loc, new ItemStack(Material.EMERALD, 32));

        // Exit portal — a single central gateway block (was 3x3 previously which
        // looked like a scattered cluster in the arena floor)
        abyss.getBlockAt(0, loc.getBlockY(), AbyssDimensionManager.ARENA_CENTER_Z)
                .setType(Material.END_GATEWAY);
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

    /**
     * Force cleanup — call this if the fight gets stuck.
     */
    public void forceCleanup() {
        fightActive = false;
        if (combatTaskId != -1) Bukkit.getScheduler().cancelTask(combatTaskId);
        if (movementTaskId != -1) Bukkit.getScheduler().cancelTask(movementTaskId);
        combatTaskId = -1;
        movementTaskId = -1;
        if (dragonModel != null) {
            dragonModel.cleanup();
            dragonModel = null;
        }
        if (bossBar != null) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.hideBossBar(bossBar);
            }
        }
        plugin.getLogger().info("[DragonBoss] Force cleanup completed.");
    }
}
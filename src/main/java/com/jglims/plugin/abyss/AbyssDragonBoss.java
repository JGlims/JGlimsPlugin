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
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.time.Duration;
import java.util.*;

/**
 * AbyssDragonBoss v5.0 — Custom Display Entity Dragon (no EnderDragon!)
 *
 * Uses AbyssDragonModel for visuals (ItemDisplay parts on invisible Zombie).
 * All attacks, phases, and movement are 100% custom.
 * No hardcoded vanilla dragon behavior to fight against.
 *
 * Features:
 *  - 1500 HP dragon with 20% damage reduction
 *  - 4-phase system at 75%, 50%, 25%, 10%
 *  - Custom BossBar (Adventure API)
 *  - Lightning, Void Breath, Minion spawns, Ground Pound, Void Pull, Enrage
 *  - Movement: fly between waypoints around the arena
 *  - Death animation with scattering display entities
 *  - Loot: same as before + exit portal
 */
public class AbyssDragonBoss implements Listener {

    // ─── Constants ────────────────────────────────────────────
    private static final double DRAGON_HP = 1024.0;
    private static final double DAMAGE_REDUCTION = 0.32;
    private static final int ARENA_R = 40;
    private static final int ARENA_CENTER_Z = -120;
    private static final int MAX_MINIONS = 6;
    private static final long RESPAWN_COOLDOWN_MS = 30L * 60 * 1000;

    // ─── State ────────────────────────────────────────────────
    private final JGlimsPlugin plugin;
    private final AbyssDimensionManager dimMgr;
    private AbyssDragonModel dragonModel;
    private boolean active = false;
    private long lastDeathTime = 0;
    private int attackTick = 0;
    private Location arenaCenter;
    private int arenaY;
    private BukkitRunnable combatLoop;
    private BukkitRunnable movementAI;
    private BossBar bossBar;
    private final Random rng = new Random();

    // Movement waypoints (generated around arena)
    private final List<Location> waypoints = new ArrayList<>();
    private int currentWaypointIndex = 0;

    // Phase tracking
    private int currentPhase = 0; // 0 = phase 1, 1 = phase 2, etc.

    public AbyssDragonBoss(JGlimsPlugin plugin, AbyssDimensionManager dimensionManager) {
        this.plugin = plugin;
        this.dimMgr = dimensionManager;
    }

    // ═══════════════════════════════════════════════════════════
    //  TRIGGER: Player walks on arena floor
    // ═══════════════════════════════════════════════════════════
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (active) return;
        Player player = event.getPlayer();
        World world = player.getWorld();
        if (!world.getName().equals("world_abyss")) return;
        if (lastDeathTime > 0 && System.currentTimeMillis() - lastDeathTime < RESPAWN_COOLDOWN_MS) return;

        Location loc = player.getLocation();
        org.bukkit.block.Block below = world.getBlockAt(loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ());
        Material belowType = below.getType();
        if (!belowType.isSolid() || belowType == Material.BARRIER || belowType == Material.AIR) return;
            // Only trigger on arena-like floor materials
            if (belowType != Material.BEDROCK && belowType != Material.OBSIDIAN
                    && belowType != Material.CRYING_OBSIDIAN && belowType != Material.END_STONE_BRICKS
                    && belowType != Material.DEEPSLATE_BRICKS && belowType != Material.POLISHED_BLACKSTONE_BRICKS
                    && belowType != Material.BLACKSTONE) return;

        arenaY = findArenaY(world);
        if (arenaY < 0) return;
        if (Math.abs(loc.getBlockY() - arenaY) > 6) return;

        double dx = loc.getX();
        double dz = loc.getZ() - ARENA_CENTER_Z;
        double distXZ = Math.sqrt(dx * dx + dz * dz);
        if (distXZ > ARENA_R) return;

        arenaCenter = new Location(world, 0.5, arenaY + 1, ARENA_CENTER_Z + 0.5);
        plugin.getLogger().info("[DragonBoss] Player " + player.getName() + " triggered boss at arena center");
        startFight(player);
    }

    /**
     * Manual trigger via /jglims abyss boss
     */
    public void manualTrigger(Player player) {
        if (active) {
            player.sendMessage(Component.text("The Abyssal Dragon is already active!", NamedTextColor.RED));
            return;
        }
        World world = player.getWorld();
        if (!world.getName().equals("world_abyss")) {
            player.sendMessage(Component.text("You must be in the Abyss!", NamedTextColor.RED));
            return;
        }
        arenaY = findArenaY(world);
        if (arenaY < 0) {
            arenaY = player.getLocation().getBlockY() - 1;
            plugin.getLogger().warning("[DragonBoss] Arena floor not found, using player Y: " + arenaY);
        }
        arenaCenter = new Location(world, 0.5, arenaY + 1, ARENA_CENTER_Z + 0.5);
        player.teleport(arenaCenter.clone().add(0, 0, 15));
        startFight(player);
    }

    // ═══════════════════════════════════════════════════════════
    //  START FIGHT
    // ═══════════════════════════════════════════════════════════
    private void startFight(Player trigger) {
        if (active) return;
        active = true;
        currentPhase = 0;
        World world = trigger.getWorld();

        // Cleanup any previous entities
        cleanupDragonEntities(world);
        clearMinions(world);

        // Generate flight waypoints (circle at various heights around arena)
        generateWaypoints();

        // Spawn the custom dragon model
        Location spawnLoc = arenaCenter.clone().add(0, 25, 0);
        dragonModel = new AbyssDragonModel(plugin);
        try {
            dragonModel.spawn(spawnLoc, DRAGON_HP);
        } catch (Exception e) {
            plugin.getLogger().severe("[DragonBoss] Failed to spawn dragon model: " + e.getMessage());
            e.printStackTrace();
            active = false;
            cleanupDragonEntities(world);
            return;
        }

        // Create boss bar
        bossBar = BossBar.bossBar(
            Component.text("\u2620 Abyssal Dragon \u2620", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD),
            1.0f,
            BossBar.Color.PURPLE,
            BossBar.Overlay.PROGRESS
        );
        for (Player p : world.getPlayers()) {
            p.showBossBar(bossBar);
        }

        // Title announcement
        Title.Times times = Title.Times.times(Duration.ofMillis(300), Duration.ofSeconds(3), Duration.ofMillis(500));
        for (Player p : world.getPlayers()) {
            p.showTitle(Title.title(
                Component.text("ABYSSAL DRAGON", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD),
                Component.text("Your soul will be consumed...", NamedTextColor.RED, TextDecoration.ITALIC),
                times
            ));
            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 2f, 0.5f);
            p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1f, 0.7f);
        }

        // Lightning for drama
        world.strikeLightningEffect(arenaCenter.clone().add(10, 0, 10));
        world.strikeLightningEffect(arenaCenter.clone().add(-10, 0, -10));
        world.strikeLightningEffect(arenaCenter.clone().add(10, 0, -10));
        world.strikeLightningEffect(arenaCenter.clone().add(-10, 0, 10));

        attackTick = 0;

        // Combat loop (attacks)
        combatLoop = new BukkitRunnable() {
            @Override
            public void run() {
                if (!active || dragonModel == null || !dragonModel.isAlive()) {
                    cancel();
                    return;
                }
                attackTick++;
                doAttacks(world);
                updateBossBar();
                checkPhaseTransitions();
                doAmbientParticles(world);
                showBossBarToNewPlayers(world);
            }
        };
        combatLoop.runTaskTimer(plugin, 80L, 40L); // every 2 seconds

        // Movement AI loop (independent of attacks)
        movementAI = new BukkitRunnable() {
            @Override
            public void run() {
                if (!active || dragonModel == null || !dragonModel.isAlive()) {
                    cancel();
                    return;
                }
                doMovementAI(world);
            }
        };
        movementAI.runTaskTimer(plugin, 20L, 10L); // every 0.5 seconds

        plugin.getLogger().info("[DragonBoss] Fight started! Custom display entity dragon spawned.");
    }

    // ═══════════════════════════════════════════════════════════
    //  WAYPOINT GENERATION
    // ═══════════════════════════════════════════════════════════
    private void generateWaypoints() {
        waypoints.clear();
        if (arenaCenter == null) return;
        World world = arenaCenter.getWorld();
        // Circle of 8 waypoints at varying heights
        for (int i = 0; i < 8; i++) {
            double angle = (i / 8.0) * Math.PI * 2;
            double r = ARENA_R * 0.6;
            double x = arenaCenter.getX() + r * Math.cos(angle);
            double z = arenaCenter.getZ() + r * Math.sin(angle);
            double y = arenaCenter.getY() + 15 + (i % 3) * 5; // varying heights: 15, 20, 25
            waypoints.add(new Location(world, x, y, z));
        }
        // Add some dive-bomb waypoints (low altitude)
        for (int i = 0; i < 4; i++) {
            double angle = (i / 4.0) * Math.PI * 2 + Math.PI / 8;
            double r = ARENA_R * 0.4;
            double x = arenaCenter.getX() + r * Math.cos(angle);
            double z = arenaCenter.getZ() + r * Math.sin(angle);
            waypoints.add(new Location(world, x, arenaCenter.getY() + 5, z));
        }
        currentWaypointIndex = 0;
    }

    // ═══════════════════════════════════════════════════════════
    //  MOVEMENT AI
    // ═══════════════════════════════════════════════════════════
    private void doMovementAI(World world) {
        if (dragonModel == null || !dragonModel.isAlive() || waypoints.isEmpty()) return;

        Location dragonLoc = dragonModel.getLocation();
        if (dragonLoc == null) return;

        Location target = waypoints.get(currentWaypointIndex);
        double dist = dragonLoc.distance(target);

        if (dist < 3.0) {
            // Pick next waypoint
            if (currentPhase >= 2 && rng.nextDouble() < 0.3) {
                // In later phases, sometimes target a player directly
                List<Player> players = getPlayersInArena(world);
                if (!players.isEmpty()) {
                    Player p = players.get(rng.nextInt(players.size()));
                    Location diveLoc = p.getLocation().clone().add(0, 4, 0);
                    dragonModel.setTarget(diveLoc);
                    return;
                }
            }
            currentWaypointIndex = rng.nextInt(waypoints.size());
        }

        dragonModel.setTarget(target);
    }

    // ═══════════════════════════════════════════════════════════
    //  COMBAT: Attacks
    // ═══════════════════════════════════════════════════════════
    private void doAttacks(World world) {
        if (dragonModel == null || !dragonModel.isAlive()) return;
        Zombie base = dragonModel.getBaseEntity();
        if (base == null) return;
        double hpPercent = base.getHealth() / DRAGON_HP;
        List<Player> nearbyPlayers = getPlayersInArena(world);
        if (nearbyPlayers.isEmpty()) return;

        // Lightning (every 10s)
        if (attackTick % 5 == 0) {
            int bolts = hpPercent < 0.25 ? 5 : hpPercent < 0.50 ? 4 : hpPercent < 0.75 ? 3 : 2;
            for (int i = 0; i < bolts; i++) {
                Player target = nearbyPlayers.get(rng.nextInt(nearbyPlayers.size()));
                Location strike = target.getLocation().add((rng.nextDouble() - 0.5) * 5, 0, (rng.nextDouble() - 0.5) * 5);
                world.strikeLightningEffect(strike);
                for (Player p : nearbyPlayers) {
                    if (p.getLocation().distance(strike) < 3.5) p.damage(7);
                }
            }
        }

        // Void Breath (every 8s)
        if (attackTick % 4 == 0) {
            Player target = nearbyPlayers.get(rng.nextInt(nearbyPlayers.size()));
            dragonModel.breathAttackVisual(target.getLocation());
            Location dragonLoc = dragonModel.getLocation();
            if (dragonLoc != null) {
                for (Player p : nearbyPlayers) {
                    if (p.getLocation().distance(dragonLoc) < 15) {
                        p.damage(6);
                        p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 80, 1));
                    }
                }
                world.playSound(dragonLoc, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.5f, 0.7f);
            }
        }

        // Minions (every 16s)
        if (attackTick % 8 == 0) {
            int current = countMinions(world);
            if (current < MAX_MINIONS) {
                int toSpawn = Math.min(2, MAX_MINIONS - current);
                for (int i = 0; i < toSpawn; i++) {
                    Location minionLoc = arenaCenter.clone().add((rng.nextDouble() - 0.5) * 30, 1, (rng.nextDouble() - 0.5) * 30);
                    if (i % 2 == 0) {
                        world.spawn(minionLoc, Enderman.class, e -> {
                            e.customName(Component.text("Void Servant", NamedTextColor.DARK_PURPLE));
                            e.setCustomNameVisible(true);
                            e.addScoreboardTag("abyss_minion");
                            Objects.requireNonNull(e.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(40);
                            e.setHealth(40);
                        });
                    } else {
                        world.spawn(minionLoc, WitherSkeleton.class, ws -> {
                            ws.customName(Component.text("Abyssal Guard", NamedTextColor.DARK_RED));
                            ws.setCustomNameVisible(true);
                            ws.addScoreboardTag("abyss_minion");
                            Objects.requireNonNull(ws.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(60);
                            ws.setHealth(60);
                            ws.getEquipment().setItemInMainHand(new ItemStack(Material.NETHERITE_SWORD));
                        });
                    }
                }
                world.playSound(arenaCenter, Sound.ENTITY_ENDERMAN_TELEPORT, 1.5f, 0.5f);
            }
        }

        // Wing Gust (every 12s in phase 2+)
        if (currentPhase >= 1 && attackTick % 6 == 0) {
            dragonModel.wingGustVisual();
            Location dragonLoc = dragonModel.getLocation();
            if (dragonLoc != null) {
                world.playSound(dragonLoc, Sound.ENTITY_ENDER_DRAGON_FLAP, 2f, 0.5f);
                for (Player p : nearbyPlayers) {
                    double dist = p.getLocation().distance(dragonLoc);
                    if (dist < 12) {
                        Vector kb = p.getLocation().toVector().subtract(dragonLoc.toVector()).normalize().multiply(1.5);
                        kb.setY(0.6);
                        p.setVelocity(p.getVelocity().add(kb));
                        p.damage(5);
                    }
                }
            }
        }

        // Ground Pound (phase 3+, every 12s)
        if (currentPhase >= 2 && attackTick % 6 == 3) {
            world.spawnParticle(Particle.EXPLOSION, arenaCenter, 10, 8, 1, 8, 0.1);
            world.playSound(arenaCenter, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.5f);
            for (Player p : nearbyPlayers) {
                double dist = p.getLocation().distance(arenaCenter);
                if (dist < 15) {
                    p.damage(8);
                    Vector kb = p.getLocation().toVector().subtract(arenaCenter.toVector()).normalize().multiply(1.2);
                    kb.setY(0.5);
                    p.setVelocity(p.getVelocity().add(kb));
                }
            }
        }

        // Void Pull (phase 4, constant)
        if (currentPhase >= 3) {
            Location dragonLoc = dragonModel.getLocation();
            if (dragonLoc != null) {
                for (Player p : nearbyPlayers) {
                    Vector pull = dragonLoc.toVector().subtract(p.getLocation().toVector()).normalize().multiply(0.35);
                    p.setVelocity(p.getVelocity().add(pull));
                    p.damage(2);
                    p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 60, 0));
                }
                world.spawnParticle(Particle.SOUL_FIRE_FLAME, dragonLoc, 40, 4, 4, 4, 0.05);
                world.spawnParticle(Particle.SOUL, dragonLoc, 25, 5, 5, 5, 0.02);
            }
        }

        // Enrage (below 10%)
        if (hpPercent < 0.10) {
            for (int a = 0; a < 360; a += 30) {
                double rad = Math.toRadians(a + attackTick * 10);
                int lx = (int) Math.round(arenaCenter.getX() + 15 * Math.cos(rad));
                int lz = (int) Math.round(arenaCenter.getZ() + 15 * Math.sin(rad));
                world.strikeLightningEffect(new Location(world, lx, arenaCenter.getY(), lz));
            }
            for (Player p : nearbyPlayers) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 0));
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  PHASE TRANSITIONS
    // ═══════════════════════════════════════════════════════════
    private void checkPhaseTransitions() {
        if (dragonModel == null || !dragonModel.isAlive()) return;
        Zombie base = dragonModel.getBaseEntity();
        if (base == null) return;
        double hpPercent = base.getHealth() / DRAGON_HP;

        int newPhase;
        if (hpPercent <= 0.10) newPhase = 3;
        else if (hpPercent <= 0.25) newPhase = 3;
        else if (hpPercent <= 0.50) newPhase = 2;
        else if (hpPercent <= 0.75) newPhase = 1;
        else newPhase = 0;

        if (newPhase > currentPhase) {
            currentPhase = newPhase;
            dragonModel.phaseTransition(currentPhase);
            World world = base.getWorld();
            String[] phaseNames = {"Phase 2: Fury Awakens", "Phase 3: Void Tremor", "Phase 4: Soul Harvest"};
            NamedTextColor[] phaseColors = {NamedTextColor.RED, NamedTextColor.DARK_PURPLE, NamedTextColor.DARK_RED};
            if (currentPhase > 0 && currentPhase <= 3) {
                Title.Times t = Title.Times.times(Duration.ofMillis(200), Duration.ofSeconds(2), Duration.ofMillis(500));
                for (Player p : world.getPlayers()) {
                    p.showTitle(Title.title(
                        Component.text("\u2620", NamedTextColor.DARK_PURPLE),
                        Component.text(phaseNames[currentPhase - 1], phaseColors[currentPhase - 1], TextDecoration.BOLD), t
                    ));
                    p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.5f, 0.6f);
                }
            }
            // Update boss bar color per phase
            BossBar.Color[] barColors = {BossBar.Color.PURPLE, BossBar.Color.RED, BossBar.Color.RED, BossBar.Color.RED};
            if (bossBar != null && currentPhase < barColors.length) {
                bossBar.color(barColors[currentPhase]);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  BOSS BAR
    // ═══════════════════════════════════════════════════════════
    private void updateBossBar() {
        if (bossBar == null || dragonModel == null || !dragonModel.isAlive()) return;
        Zombie base = dragonModel.getBaseEntity();
        if (base == null) return;
        float progress = (float) Math.max(0, Math.min(1, base.getHealth() / DRAGON_HP));
        bossBar.progress(progress);
    }

    private void showBossBarToNewPlayers(World world) {
        if (bossBar == null) return;
        for (Player p : world.getPlayers()) {
            p.showBossBar(bossBar);
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  DAMAGE HANDLING
    // ═══════════════════════════════════════════════════════════
    @EventHandler
    public void onBossDamage(EntityDamageEvent event) {
        if (!active || dragonModel == null) return;
        Zombie base = dragonModel.getBaseEntity();
        if (base == null || !(event.getEntity() instanceof Zombie z)) return;
        if (!z.getUniqueId().equals(base.getUniqueId())) return;

        // Apply damage reduction
        event.setDamage(event.getDamage() * (1.0 - DAMAGE_REDUCTION));

        // Cancel environmental damage
        EntityDamageEvent.DamageCause cause = event.getCause();
        if (cause == EntityDamageEvent.DamageCause.SUFFOCATION ||
            cause == EntityDamageEvent.DamageCause.DROWNING ||
            cause == EntityDamageEvent.DamageCause.FALL ||
            cause == EntityDamageEvent.DamageCause.FIRE ||
            cause == EntityDamageEvent.DamageCause.FIRE_TICK ||
            cause == EntityDamageEvent.DamageCause.LAVA) {
            event.setCancelled(true);
            return;
        }

        // Visual damage flash
        dragonModel.damageFlash();
    }

    // ═══════════════════════════════════════════════════════════
    //  DEATH
    // ═══════════════════════════════════════════════════════════
    @EventHandler
    public void onBossDeath(EntityDeathEvent event) {
        if (!active || dragonModel == null) return;
        Zombie base = dragonModel.getBaseEntity();
        if (base == null || !(event.getEntity() instanceof Zombie z)) return;
        if (!z.getUniqueId().equals(base.getUniqueId())) return;

        event.getDrops().clear();
        event.setDroppedExp(0);
        active = false;
        lastDeathTime = System.currentTimeMillis();

        if (combatLoop != null) try { combatLoop.cancel(); } catch (Exception ignored) {}
        if (movementAI != null) try { movementAI.cancel(); } catch (Exception ignored) {}

        World world = z.getWorld();
        clearMinions(world);

        // Remove boss bar
        if (bossBar != null) {
            for (Player p : Bukkit.getOnlinePlayers()) p.hideBossBar(bossBar);
            bossBar = null;
        }

        // Victory announcement
        Title.Times times = Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(5), Duration.ofMillis(1500));
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.showTitle(Title.title(
                Component.text("VICTORY!", NamedTextColor.GOLD, TextDecoration.BOLD),
                Component.text("The Abyssal Dragon has been vanquished!", NamedTextColor.YELLOW),
                times
            ));
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.5f, 1f);
        }

        // Death animation (scatters display parts)
        dragonModel.deathAnimation();

        // Drop loot after a delay (let animation play)
        new BukkitRunnable() {
            @Override
            public void run() {
                dropLoot(world);
                createExitPortal(world);
                // Fireworks
                new BukkitRunnable() {
                    int count = 0;
                    @Override
                    public void run() {
                        if (count++ >= 12 || arenaCenter == null) { cancel(); return; }
                        world.spawnParticle(Particle.FIREWORK, arenaCenter.clone().add(0, 5, 0), 120, 10, 10, 10, 0.3);
                        world.playSound(arenaCenter, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 1.5f, 1f);
                    }
                }.runTaskTimer(plugin, 0L, 20L);
            }
        }.runTaskLater(plugin, 50L); // 2.5 seconds for death anim

        dragonModel = null;
        plugin.getLogger().info("[DragonBoss] Dragon defeated! Custom model death animation played.");
    }

    // ═══════════════════════════════════════════════════════════
    //  LOOT
    // ═══════════════════════════════════════════════════════════
    private void dropLoot(World world) {
        if (arenaCenter == null) return;
        Location dropLoc = arenaCenter.clone().add(0, 1, 0);

        world.dropItemNaturally(dropLoc, new ItemStack(Material.DRAGON_EGG, 1));
        world.dropItemNaturally(dropLoc, new ItemStack(Material.NETHER_STAR, 3));
        world.dropItemNaturally(dropLoc, new ItemStack(Material.EXPERIENCE_BOTTLE, 64));
        world.dropItemNaturally(dropLoc, new ItemStack(Material.NETHERITE_INGOT, 4));
        world.dropItemNaturally(dropLoc, new ItemStack(Material.ELYTRA, 1));
        world.dropItemNaturally(dropLoc, new ItemStack(Material.TOTEM_OF_UNDYING, 2));
        world.dropItemNaturally(dropLoc, new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 8));
        world.dropItemNaturally(dropLoc, new ItemStack(Material.END_CRYSTAL, 4));

        try {
            if (plugin.getLegendaryWeaponManager() != null) {
                List<LegendaryWeapon> highTier = new ArrayList<>();
                for (LegendaryWeapon lw : LegendaryWeapon.values()) {
                    String tn = lw.getTier().name();
                    if (tn.equals("ABYSSAL") || tn.equals("MYTHIC")) highTier.add(lw);
                }
                for (int i = 0; i < 2 && !highTier.isEmpty(); i++) {
                    LegendaryWeapon chosen = highTier.get(rng.nextInt(highTier.size()));
                    ItemStack weapon = plugin.getLegendaryWeaponManager().createWeapon(chosen);
                    if (weapon != null) world.dropItemNaturally(dropLoc, weapon);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("[DragonBoss] Error dropping legendary weapons: " + e.getMessage());
        }

        try {
            if (plugin.getPowerUpManager() != null) {
                for (int i = 0; i < 2; i++) world.dropItemNaturally(dropLoc, plugin.getPowerUpManager().createPhoenixFeather());
                for (int i = 0; i < 3; i++) world.dropItemNaturally(dropLoc, plugin.getPowerUpManager().createHeartCrystal());
                for (int i = 0; i < 5; i++) world.dropItemNaturally(dropLoc, plugin.getPowerUpManager().createSoulFragment());
            }
        } catch (Exception e) {
            plugin.getLogger().warning("[DragonBoss] Error dropping power-ups: " + e.getMessage());
        }

        for (int i = 0; i < 25; i++) {
            Location orbLoc = dropLoc.clone().add((rng.nextDouble() - 0.5) * 4, 1, (rng.nextDouble() - 0.5) * 4);
            world.spawn(orbLoc, ExperienceOrb.class, o -> o.setExperience(500));
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  EXIT PORTAL
    // ═══════════════════════════════════════════════════════════
    private void createExitPortal(World world) {
        if (arenaCenter == null) return;
        int cx = arenaCenter.getBlockX(), cy = arenaCenter.getBlockY(), cz = arenaCenter.getBlockZ();
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                world.getBlockAt(cx + x, cy - 1, cz + z).setType(Material.BEDROCK);
                if (Math.abs(x) <= 1 && Math.abs(z) <= 1) {
                    world.getBlockAt(cx + x, cy, cz + z).setType(Material.END_PORTAL);
                }
            }
        }
        world.playSound(arenaCenter, Sound.BLOCK_END_PORTAL_SPAWN, 2f, 1f);
    }

    // ═══════════════════════════════════════════════════════════
    //  UTILITIES
    // ═══════════════════════════════════════════════════════════
    private List<Player> getPlayersInArena(World world) {
        List<Player> players = new ArrayList<>();
        for (Player p : world.getPlayers()) {
            if (arenaCenter != null && p.getLocation().distance(arenaCenter) <= ARENA_R + 15) players.add(p);
        }
        return players;
    }

    private int countMinions(World world) {
        int count = 0;
        for (Entity e : world.getEntities()) {
            if (e.getScoreboardTags().contains("abyss_minion") && !e.isDead()) count++;
        }
        return count;
    }

    private void clearMinions(World world) {
        for (Entity e : world.getEntities()) {
            if (e.getScoreboardTags().contains("abyss_minion")) e.remove();
        }
    }

    private void cleanupDragonEntities(World world) {
        for (Entity e : world.getEntities()) {
            if (e.getScoreboardTags().contains("abyss_dragon_base") ||
                e.getScoreboardTags().contains("abyss_dragon_part")) {
                e.remove();
            }
        }
        // Also remove any stray EnderDragons
        for (EnderDragon d : world.getEntitiesByClass(EnderDragon.class)) {
            d.remove();
        }
    }

    private int findArenaY(World world) {
        // Check arena center (0, y, -120) for any solid floor block
        for (int y = 200; y >= -64; y--) {
            Material mat = world.getBlockAt(0, y, ARENA_CENTER_Z).getType();
            if (mat == Material.BEDROCK || mat == Material.OBSIDIAN
                    || mat == Material.CRYING_OBSIDIAN || mat == Material.DEEPSLATE_BRICKS
                    || mat == Material.POLISHED_BLACKSTONE_BRICKS || mat == Material.BLACKSTONE
                    || mat == Material.END_STONE_BRICKS || mat == Material.DEEPSLATE_TILES) {
                return y;
            }
        }
        // Fallback: check center of citadel (0, y, 0)
        for (int y = 200; y >= -64; y--) {
            Material mat = world.getBlockAt(0, y, 0).getType();
            if (mat.isSolid() && mat != Material.BARRIER) return y;
        }
        plugin.getLogger().warning("[DragonBoss] findArenaY found no floor, returning 64 as fallback");
        return 64;
    }

    private void doAmbientParticles(World world) {
        if (arenaCenter == null) return;
        world.spawnParticle(Particle.ASH, arenaCenter, 20, 20, 12, 20, 0);
        if (dragonModel != null && dragonModel.isAlive()) {
            Location dloc = dragonModel.getLocation();
            if (dloc != null) {
                world.spawnParticle(Particle.DRAGON_BREATH, dloc, 12, 2, 1, 2, 0.01);
                world.spawnParticle(Particle.REVERSE_PORTAL, dloc, 8, 1, 1, 1, 0.05);
            }
        }
    }

    public boolean isActive() { return active; }
}
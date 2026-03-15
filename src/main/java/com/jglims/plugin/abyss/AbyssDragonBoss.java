package com.jglims.plugin.abyss;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.legendary.LegendaryTier;
import com.jglims.plugin.legendary.LegendaryWeapon;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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
 * AbyssDragonBoss v4.0 — Matched to AbyssCitadelBuilder v4.0 Gothic Cathedral
 *
 * KEY CHANGE: The arena is now BEHIND the cathedral at (0, sY, -120),
 * not at (0, sY, 0). The ArenaCenter Z coordinate must be -120.
 * Trigger detection scans for bedrock at x=0, z=-120.
 *
 * Features:
 *  - 1500 HP dragon with 20% damage reduction
 *  - 4-phase system: 75%, 50%, 25%, 10%
 *  - Lightning, Void Breath, Minion spawns, Ground Pound, Void Pull, Enrage
 *  - Loot: dragon egg, nether stars, netherite, elytra, MYTHIC/ABYSSAL weapons, power-ups
 *  - Exit portal on death
 *  - manualTrigger() for /jglims abyss boss
 */
public class AbyssDragonBoss implements Listener {

    // ─── Constants ────────────────────────────────────────────
    private static final double DRAGON_HP = 1500.0;
    private static final double DAMAGE_REDUCTION = 0.20;
    private static final int ARENA_R = 40;
    private static final int ARENA_CENTER_Z = -120; // v4.0 cathedral: arena behind apse
    private static final int MAX_MINIONS = 6;
    private static final long RESPAWN_COOLDOWN_MS = 30L * 60 * 1000; // 30 min

    // ─── State ────────────────────────────────────────────────
    private final JGlimsPlugin plugin;
    private final AbyssDimensionManager dimMgr;
    private EnderDragon dragon;
    private boolean active = false;
    private long lastDeathTime = 0;
    private int attackTick = 0;
    private int phaseTick = 0;
    private int phaseIndex = 0;
    private Location arenaCenter;
    private int arenaY;
    private BukkitRunnable combatLoop;
    private final Random rng = new Random();

    private static final EnderDragon.Phase[] DRAGON_PHASES = {
        EnderDragon.Phase.CHARGE_PLAYER,
        EnderDragon.Phase.BREATH_ATTACK,
        EnderDragon.Phase.STRAFING,
        EnderDragon.Phase.LAND_ON_PORTAL,
        EnderDragon.Phase.CHARGE_PLAYER,
        EnderDragon.Phase.SEARCH_FOR_BREATH_ATTACK_TARGET
    };

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

        // Must be standing on bedrock, obsidian, or crying obsidian (arena floor)
        Block below = world.getBlockAt(loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ());
        Material belowType = below.getType();
        if (belowType != Material.BEDROCK && belowType != Material.OBSIDIAN && belowType != Material.CRYING_OBSIDIAN) return;

        // Find the arena Y (bedrock floor at x=0, z=ARENA_CENTER_Z)
        arenaY = findArenaY(world);
        if (arenaY < 0) return;

        // Must be near arena Y level (within 6 blocks)
        if (Math.abs(loc.getBlockY() - arenaY) > 6) return;

        // Distance from (0, arenaY, ARENA_CENTER_Z) — the actual arena center
        double dx = loc.getX();
        double dz = loc.getZ() - ARENA_CENTER_Z;
        double distXZ = Math.sqrt(dx * dx + dz * dz);
        if (distXZ > ARENA_R) return;

        // All checks passed
        arenaCenter = new Location(world, 0.5, arenaY + 1, ARENA_CENTER_Z + 0.5);
        plugin.getLogger().info("[DragonBoss] Player " + player.getName() + " triggered boss at arena center (0, " + arenaY + ", " + ARENA_CENTER_Z + ")");
        startFight(player);
    }

    /**
     * Manual trigger — called from /jglims abyss boss
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
        // Teleport player to arena center first
        player.teleport(arenaCenter.clone().add(0, 0, 15)); // 15 blocks south of center
        startFight(player);
    }

    // ═══════════════════════════════════════════════════════════
    //  START FIGHT
    // ═══════════════════════════════════════════════════════════
    private void startFight(Player trigger) {
        if (active) return;
        active = true;
        World world = trigger.getWorld();

        for (EnderDragon d : world.getEntitiesByClass(EnderDragon.class)) d.remove();
        for (Wither w : world.getEntitiesByClass(Wither.class)) w.remove();
        clearMinions(world);

        Location spawnLoc = arenaCenter.clone().add(0, 20, 0);
        dragon = world.spawn(spawnLoc, EnderDragon.class, d -> {
            d.customName(Component.text("\u2620 Abyssal Dragon \u2620", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD));
            d.setCustomNameVisible(true);
            d.setGlowing(true);
            Objects.requireNonNull(d.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(DRAGON_HP);
            d.setHealth(DRAGON_HP);
            d.setPodium(arenaCenter);
            d.setPhase(EnderDragon.Phase.CIRCLING);
        });

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

        world.strikeLightningEffect(arenaCenter.clone().add(10, 0, 10));
        world.strikeLightningEffect(arenaCenter.clone().add(-10, 0, -10));
        world.strikeLightningEffect(arenaCenter.clone().add(10, 0, -10));
        world.strikeLightningEffect(arenaCenter.clone().add(-10, 0, 10));

        new BukkitRunnable() {
            @Override
            public void run() {
                if (dragon != null && dragon.isValid() && active) {
                    dragon.setPhase(EnderDragon.Phase.CHARGE_PLAYER);
                }
            }
        }.runTaskLater(plugin, 60L);

        attackTick = 0;
        phaseTick = 0;
        phaseIndex = 0;

        combatLoop = new BukkitRunnable() {
            @Override
            public void run() {
                if (!active || dragon == null || dragon.isDead() || !dragon.isValid()) {
                    cancel();
                    return;
                }
                attackTick++;
                phaseTick++;
                doAttacks(world);
                doPhaseCycle();
                doConfinement();
                doAmbientParticles(world);
            }
        };
        combatLoop.runTaskTimer(plugin, 80L, 40L);
        plugin.getLogger().info("[DragonBoss] Fight started! HP=" + DRAGON_HP + " Arena=(0, " + arenaY + ", " + ARENA_CENTER_Z + ")");
    }

    // ═══════════════════════════════════════════════════════════
    //  COMBAT: Attacks
    // ═══════════════════════════════════════════════════════════
    private void doAttacks(World world) {
        if (dragon == null || !dragon.isValid()) return;
        double hpPercent = dragon.getHealth() / DRAGON_HP;
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
            Location dragonLoc = dragon.getLocation();
            world.spawnParticle(Particle.DRAGON_BREATH, dragonLoc, 100, 5, 2, 5, 0.03);
            for (Player p : nearbyPlayers) {
                if (p.getLocation().distance(dragonLoc) < 12) {
                    p.damage(6);
                    p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 80, 1));
                }
            }
            world.playSound(dragonLoc, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.5f, 0.7f);
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

        // Ground Pound (phase 3+, every 12s)
        if (hpPercent < 0.50 && attackTick % 6 == 0) {
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
        if (hpPercent < 0.25) {
            for (Player p : nearbyPlayers) {
                Vector pull = dragon.getLocation().toVector().subtract(p.getLocation().toVector()).normalize().multiply(0.35);
                p.setVelocity(p.getVelocity().add(pull));
                p.damage(2);
                p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 60, 0));
            }
            world.spawnParticle(Particle.SOUL_FIRE_FLAME, dragon.getLocation(), 40, 4, 4, 4, 0.05);
            world.spawnParticle(Particle.SOUL, dragon.getLocation(), 25, 5, 5, 5, 0.02);
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

        // Phase transition titles
        if (hpPercent < 0.75 && hpPercent > 0.74 && attackTick % 10 == 0)
            showPhaseTitle(world, "Phase 2: Fury Awakens", NamedTextColor.RED);
        if (hpPercent < 0.50 && hpPercent > 0.49 && attackTick % 10 == 0)
            showPhaseTitle(world, "Phase 3: Void Tremor", NamedTextColor.DARK_PURPLE);
        if (hpPercent < 0.25 && hpPercent > 0.24 && attackTick % 10 == 0)
            showPhaseTitle(world, "Phase 4: Soul Harvest", NamedTextColor.DARK_RED);
    }

    private void showPhaseTitle(World world, String subtitle, NamedTextColor color) {
        Title.Times t = Title.Times.times(Duration.ofMillis(200), Duration.ofSeconds(2), Duration.ofMillis(500));
        for (Player p : world.getPlayers()) {
            p.showTitle(Title.title(
                Component.text("\u2620", NamedTextColor.DARK_PURPLE),
                Component.text(subtitle, color, TextDecoration.BOLD), t
            ));
            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.5f, 0.6f);
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  COMBAT: Phase cycling
    // ═══════════════════════════════════════════════════════════
    private void doPhaseCycle() {
        if (dragon == null || !dragon.isValid()) return;
        if (phaseTick % 3 != 0) return;
        phaseIndex = (phaseIndex + 1) % DRAGON_PHASES.length;
        try { dragon.setPhase(DRAGON_PHASES[phaseIndex]); }
        catch (Exception e) { dragon.setPhase(EnderDragon.Phase.CIRCLING); }
    }

    // ═══════════════════════════════════════════════════════════
    //  COMBAT: Confinement
    // ═══════════════════════════════════════════════════════════
    private void doConfinement() {
        if (dragon == null || !dragon.isValid() || arenaCenter == null) return;
        Location dl = dragon.getLocation();
        double dist = dl.distance(arenaCenter);
        if (dist > ARENA_R + 20) {
            dragon.setPodium(arenaCenter);
            dragon.teleport(arenaCenter.clone().add(0, 18, 0));
            dragon.setPhase(EnderDragon.Phase.CIRCLING);
        } else if (dist > ARENA_R) {
            dragon.setPodium(arenaCenter);
        }
        if (dl.getY() < arenaY - 3) dragon.teleport(arenaCenter.clone().add(0, 12, 0));
        if (dl.getY() > arenaY + 55) {
            dragon.setPodium(arenaCenter);
            dragon.setPhase(EnderDragon.Phase.FLY_TO_PORTAL);
        }
    }

    private void doAmbientParticles(World world) {
        if (arenaCenter == null) return;
        world.spawnParticle(Particle.ASH, arenaCenter, 20, 20, 12, 20, 0);
        if (dragon != null && dragon.isValid()) {
            world.spawnParticle(Particle.DRAGON_BREATH, dragon.getLocation(), 12, 2, 1, 2, 0.01);
            world.spawnParticle(Particle.REVERSE_PORTAL, dragon.getLocation(), 8, 1, 1, 1, 0.05);
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  DAMAGE REDUCTION
    // ═══════════════════════════════════════════════════════════
    @EventHandler
    public void onBossDamage(EntityDamageEvent event) {
        if (!active || !(event.getEntity() instanceof EnderDragon d)) return;
        if (dragon == null || !d.getUniqueId().equals(dragon.getUniqueId())) return;
        event.setDamage(event.getDamage() * (1.0 - DAMAGE_REDUCTION));
        EntityDamageEvent.DamageCause cause = event.getCause();
        if (cause == EntityDamageEvent.DamageCause.SUFFOCATION ||
            cause == EntityDamageEvent.DamageCause.DROWNING ||
            cause == EntityDamageEvent.DamageCause.FALL) {
            event.setCancelled(true);
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  DEATH
    // ═══════════════════════════════════════════════════════════
    @EventHandler
    public void onBossDeath(EntityDeathEvent event) {
        if (!active || !(event.getEntity() instanceof EnderDragon d)) return;
        if (dragon == null || !d.getUniqueId().equals(dragon.getUniqueId())) return;
        event.getDrops().clear();
        event.setDroppedExp(0);
        active = false;
        lastDeathTime = System.currentTimeMillis();
        if (combatLoop != null) try { combatLoop.cancel(); } catch (Exception ignored) {}
        World world = d.getWorld();
        clearMinions(world);

        Title.Times times = Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(5), Duration.ofMillis(1500));
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.showTitle(Title.title(
                Component.text("VICTORY!", NamedTextColor.GOLD, TextDecoration.BOLD),
                Component.text("The Abyssal Dragon has been vanquished!", NamedTextColor.YELLOW),
                times
            ));
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.5f, 1f);
        }

        dropLoot(world);
        createExitPortal(world);

        new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if (count++ >= 12 || arenaCenter == null) { cancel(); return; }
                world.spawnParticle(Particle.FIREWORK, arenaCenter.clone().add(0, 5, 0), 120, 10, 10, 10, 0.3);
                world.playSound(arenaCenter, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 1.5f, 1f);
            }
        }.runTaskTimer(plugin, 0L, 20L);

        dragon = null;
        plugin.getLogger().info("[DragonBoss] Dragon defeated! Loot dropped at arena center.");
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

        // 2 random MYTHIC/ABYSSAL weapons
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

        // Power-ups
        try {
            if (plugin.getPowerUpManager() != null) {
                for (int i = 0; i < 2; i++) world.dropItemNaturally(dropLoc, plugin.getPowerUpManager().createPhoenixFeather());
                for (int i = 0; i < 3; i++) world.dropItemNaturally(dropLoc, plugin.getPowerUpManager().createHeartCrystal());
                for (int i = 0; i < 5; i++) world.dropItemNaturally(dropLoc, plugin.getPowerUpManager().createSoulFragment());
            }
        } catch (Exception e) {
            plugin.getLogger().warning("[DragonBoss] Error dropping power-ups: " + e.getMessage());
        }

        // XP orbs
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

    /**
     * Find arena Y by scanning for bedrock at (0, y, ARENA_CENTER_Z).
     * The v4.0 citadel places the arena at z=-120.
     */
    private int findArenaY(World world) {
        // Primary: scan at the arena's actual Z coordinate
        for (int y = 120; y > 0; y--) {
            if (world.getBlockAt(0, y, ARENA_CENTER_Z).getType() == Material.BEDROCK) return y;
        }
        for (int y = 0; y >= -64; y--) {
            if (world.getBlockAt(0, y, ARENA_CENTER_Z).getType() == Material.BEDROCK) return y;
        }
        // Fallback: try z=0 (old location)
        for (int y = 120; y > 0; y--) {
            if (world.getBlockAt(0, y, 0).getType() == Material.BEDROCK) return y;
        }
        return -1;
    }

    public boolean isActive() { return active; }
}
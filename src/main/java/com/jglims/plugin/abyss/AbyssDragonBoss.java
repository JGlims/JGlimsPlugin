package com.jglims.plugin.abyss;

import com.jglims.plugin.JGlimsPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.entity.WitherSkeleton;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class AbyssDragonBoss implements Listener {

    // ── Constants ─────────────────────────────────────────────
    private static final double DRAGON_MAX_HP = 1200.0;
    private static final double DAMAGE_REDUCTION = 0.15;
    private static final int ARENA_RADIUS = 35;
    private static final int MAX_MINIONS = 6;
    private static final long RESPAWN_COOLDOWN_MS = 30L * 60L * 1000L;

    private static final int LIGHTNING_INTERVAL = 5;
    private static final int BREATH_INTERVAL = 4;
    private static final int MINION_INTERVAL = 8;
    private static final int PHASE_CYCLE_INTERVAL = 3;

    // ── State ─────────────────────────────────────────────────
    private final JGlimsPlugin plugin;
    private final AbyssDimensionManager dimensionManager;
    private EnderDragon dragon;
    private boolean active = false;
    private long lastFightEndTime = 0;
    private int attackTick = 0;
    private int phaseTick = 0;
    private Location arenaCenter;
    private int arenaY;
    private BukkitRunnable attackLoop;
    private final Random random = new Random();

    private static final EnderDragon.Phase[] COMBAT_PHASES = {
            EnderDragon.Phase.CHARGE_PLAYER,
            EnderDragon.Phase.BREATH_ATTACK,
            EnderDragon.Phase.STRAFING,
            EnderDragon.Phase.LAND_ON_PORTAL,
            EnderDragon.Phase.CHARGE_PLAYER,
            EnderDragon.Phase.SEARCH_FOR_BREATH_ATTACK_TARGET
    };
    private int currentPhaseIndex = 0;

    public AbyssDragonBoss(JGlimsPlugin plugin, AbyssDimensionManager dimensionManager) {
        this.plugin = plugin;
        this.dimensionManager = dimensionManager;
    }

    // ── Arena detection ───────────────────────────────────────

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (active) return;
        Player player = event.getPlayer();
        World world = player.getWorld();
        if (!world.getName().equals("world_abyss")) return;

        if (System.currentTimeMillis() - lastFightEndTime < RESPAWN_COOLDOWN_MS && lastFightEndTime > 0) return;

        Location loc = player.getLocation();
        Block below = world.getBlockAt(loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ());
        if (below.getType() != Material.BEDROCK) return;

        if (Math.abs(loc.getX()) > ARENA_RADIUS && Math.abs(loc.getZ()) > ARENA_RADIUS) return;

        arenaY = findArenaY(world);
        if (arenaY < 0) return;
        if (Math.abs(loc.getBlockY() - arenaY) > 5) return;

        arenaCenter = new Location(world, 0.5, arenaY + 1, 0.5);
        startBossFight(player);
    }

    // ── Boss fight start ──────────────────────────────────────

    private void startBossFight(Player trigger) {
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
            Objects.requireNonNull(d.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(DRAGON_MAX_HP);
            d.setHealth(DRAGON_MAX_HP);
            d.setPodium(arenaCenter);
            d.setPhase(EnderDragon.Phase.CIRCLING);
        });

        Title.Times times = Title.Times.times(
                Duration.ofMillis(300),
                Duration.ofSeconds(3),
                Duration.ofMillis(500)
        );
        Title title = Title.title(
                Component.text("ABYSSAL DRAGON", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD),
                Component.text("Your soul will be consumed...", NamedTextColor.RED, TextDecoration.ITALIC),
                times
        );
        for (Player p : world.getPlayers()) {
            p.showTitle(title);
            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0f, 0.5f);
            p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.7f);
        }

        world.strikeLightningEffect(arenaCenter.clone().add(5, 0, 5));
        world.strikeLightningEffect(arenaCenter.clone().add(-5, 0, -5));
        world.spawnParticle(Particle.EXPLOSION, arenaCenter, 10, 3, 3, 3, 0);

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
        currentPhaseIndex = 0;
        attackLoop = new BukkitRunnable() {
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
        attackLoop.runTaskTimer(plugin, 80L, 40L);
    }

    // ── Attack logic ──────────────────────────────────────────

    private void doAttacks(World world) {
        if (dragon == null || !dragon.isValid()) return;
        double hpPercent = dragon.getHealth() / DRAGON_MAX_HP;
        List<Player> nearby = getArenaPlayers(world);
        if (nearby.isEmpty()) return;

        if (attackTick % LIGHTNING_INTERVAL == 0) {
            int strikes = hpPercent < 0.3 ? 4 : (hpPercent < 0.6 ? 3 : 2);
            for (int i = 0; i < strikes; i++) {
                Player target = nearby.get(random.nextInt(nearby.size()));
                Location tLoc = target.getLocation().add(
                        (random.nextDouble() - 0.5) * 4, 0, (random.nextDouble() - 0.5) * 4
                );
                world.strikeLightningEffect(tLoc);
                for (Player p : nearby) {
                    if (p.getLocation().distance(tLoc) < 3.0) {
                        p.damage(6.0);
                    }
                }
            }
        }

        if (attackTick % BREATH_INTERVAL == 0) {
            Location dLoc = dragon.getLocation();
            world.spawnParticle(Particle.DRAGON_BREATH, dLoc, 80, 4, 2, 4, 0.02);
            for (Player p : nearby) {
                if (p.getLocation().distance(dLoc) < 10.0) {
                    p.damage(5.0);
                    p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 60, 1));
                }
            }
            world.playSound(dLoc, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.5f, 0.7f);
        }

        if (attackTick % MINION_INTERVAL == 0) {
            int currentMinions = countMinions(world);
            if (currentMinions < MAX_MINIONS) {
                int toSpawn = Math.min(2, MAX_MINIONS - currentMinions);
                for (int i = 0; i < toSpawn; i++) {
                    Location mLoc = arenaCenter.clone().add(
                            (random.nextDouble() - 0.5) * 20,
                            1,
                            (random.nextDouble() - 0.5) * 20
                    );
                    if (i % 2 == 0) {
                        world.spawn(mLoc, Enderman.class, e -> {
                            e.customName(Component.text("Void Servant", NamedTextColor.DARK_PURPLE));
                            e.addScoreboardTag("abyss_minion");
                            Objects.requireNonNull(e.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(40.0);
                            e.setHealth(40.0);
                        });
                    } else {
                        world.spawn(mLoc, WitherSkeleton.class, ws -> {
                            ws.customName(Component.text("Abyssal Guard", NamedTextColor.DARK_RED));
                            ws.addScoreboardTag("abyss_minion");
                            Objects.requireNonNull(ws.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(60.0);
                            ws.setHealth(60.0);
                            ws.getEquipment().setItemInMainHand(new ItemStack(Material.NETHERITE_SWORD));
                        });
                    }
                }
                world.playSound(arenaCenter, Sound.ENTITY_ENDERMAN_TELEPORT, 1.5f, 0.5f);
            }
        }

        if (hpPercent < 0.20) {
            for (Player p : nearby) {
                Vector pull = dragon.getLocation().toVector()
                        .subtract(p.getLocation().toVector()).normalize().multiply(0.3);
                p.setVelocity(p.getVelocity().add(pull));
                p.damage(2.0);
                p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 40, 0));
            }
            world.spawnParticle(Particle.SOUL_FIRE_FLAME, dragon.getLocation(), 30, 3, 3, 3, 0.05);
            world.spawnParticle(Particle.SOUL, dragon.getLocation(), 20, 4, 4, 4, 0.02);
        }
    }

    private void doPhaseCycle() {
        if (dragon == null || !dragon.isValid()) return;
        if (phaseTick % PHASE_CYCLE_INTERVAL != 0) return;
        currentPhaseIndex = (currentPhaseIndex + 1) % COMBAT_PHASES.length;
        try {
            dragon.setPhase(COMBAT_PHASES[currentPhaseIndex]);
        } catch (Exception ignored) {
            dragon.setPhase(EnderDragon.Phase.CIRCLING);
        }
    }

    private void doConfinement() {
        if (dragon == null || !dragon.isValid() || arenaCenter == null) return;
        Location dLoc = dragon.getLocation();
        double dist = dLoc.distance(arenaCenter);

        if (dist > ARENA_RADIUS + 15) {
            dragon.setPodium(arenaCenter);
            dragon.teleport(arenaCenter.clone().add(0, 15, 0));
            dragon.setPhase(EnderDragon.Phase.CIRCLING);
        } else if (dist > ARENA_RADIUS) {
            dragon.setPodium(arenaCenter);
        }

        if (dLoc.getY() < arenaY - 2) {
            dragon.teleport(arenaCenter.clone().add(0, 10, 0));
        }
        if (dLoc.getY() > arenaY + 50) {
            dragon.setPodium(arenaCenter);
            dragon.setPhase(EnderDragon.Phase.FLY_TO_PORTAL);
        }
    }

    // ── Damage reduction ──────────────────────────────────────

    @EventHandler
    public void onBossDamage(EntityDamageEvent event) {
        if (!active) return;
        if (!(event.getEntity() instanceof EnderDragon)) return;
        EnderDragon d = (EnderDragon) event.getEntity();
        if (dragon == null || !d.getUniqueId().equals(dragon.getUniqueId())) return;

        event.setDamage(event.getDamage() * (1.0 - DAMAGE_REDUCTION));

        EntityDamageEvent.DamageCause cause = event.getCause();
        if (cause == EntityDamageEvent.DamageCause.SUFFOCATION
                || cause == EntityDamageEvent.DamageCause.DROWNING
                || cause == EntityDamageEvent.DamageCause.FALL) {
            event.setCancelled(true);
        }
    }

    // ── Boss death ────────────────────────────────────────────

    @EventHandler
    public void onBossDeath(EntityDeathEvent event) {
        if (!active) return;
        if (!(event.getEntity() instanceof EnderDragon)) return;
        EnderDragon d = (EnderDragon) event.getEntity();
        if (dragon == null || !d.getUniqueId().equals(dragon.getUniqueId())) return;

        event.getDrops().clear();
        event.setDroppedExp(0);

        active = false;
        lastFightEndTime = System.currentTimeMillis();
        if (attackLoop != null) {
            try { attackLoop.cancel(); } catch (Exception ignored) {}
        }

        World world = d.getWorld();
        clearMinions(world);

        Title.Times times = Title.Times.times(
                Duration.ofMillis(500),
                Duration.ofSeconds(5),
                Duration.ofMillis(1500)
        );
        Title victoryTitle = Title.title(
                Component.text("VICTORY!", NamedTextColor.GOLD, TextDecoration.BOLD),
                Component.text("The Abyssal Dragon has been vanquished!", NamedTextColor.YELLOW),
                times
        );
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.showTitle(victoryTitle);
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.5f, 1.0f);
        }

        dropLoot(world);
        createExitPortal(world);

        new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if (count++ >= 10 || arenaCenter == null) {
                    cancel();
                    return;
                }
                world.spawnParticle(Particle.FIREWORK, arenaCenter.clone().add(0, 5, 0), 100, 8, 8, 8, 0.3);
                world.spawnParticle(Particle.TOTEM_OF_UNDYING, arenaCenter.clone().add(0, 3, 0), 50, 5, 5, 5, 0.2);
                world.playSound(arenaCenter, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 1.5f, 1.0f);
            }
        }.runTaskTimer(plugin, 0L, 20L);

        dragon = null;
    }

    // ── Loot drops ────────────────────────────────────────────

    private void dropLoot(World world) {
        if (arenaCenter == null) return;
        Location lootLoc = arenaCenter.clone().add(0, 1, 0);

        // Core drops
        world.dropItemNaturally(lootLoc, new ItemStack(Material.NETHER_STAR, 3));
        world.dropItemNaturally(lootLoc, new ItemStack(Material.EXPERIENCE_BOTTLE, 64));
        world.dropItemNaturally(lootLoc, new ItemStack(Material.NETHERITE_INGOT, 4));
        world.dropItemNaturally(lootLoc, new ItemStack(Material.DRAGON_EGG, 1));
        world.dropItemNaturally(lootLoc, new ItemStack(Material.ELYTRA, 1));
        world.dropItemNaturally(lootLoc, new ItemStack(Material.TOTEM_OF_UNDYING, 2));
        world.dropItemNaturally(lootLoc, new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 8));
        world.dropItemNaturally(lootLoc, new ItemStack(Material.END_CRYSTAL, 4));

        // Legendary weapons from plugin managers (safe calls)
        try {
            if (plugin.getLegendaryWeaponManager() != null) {
                // Use the manager's existing methods to create weapons
                // Drop 2 ABYSSAL or MYTHIC tier weapons
                for (int i = 0; i < 2; i++) {
                    try {
                        // LegendaryWeaponManager.createWeaponItem(LegendaryWeapon) is the standard method
                        com.jglims.plugin.legendary.LegendaryWeapon[] abyssalWeapons =
                            com.jglims.plugin.legendary.LegendaryWeapon.values();
                        List<com.jglims.plugin.legendary.LegendaryWeapon> highTier = new ArrayList<>();
                        for (com.jglims.plugin.legendary.LegendaryWeapon lw : abyssalWeapons) {
                            String tierName = lw.getTier().name();
                            if (tierName.equals("ABYSSAL") || tierName.equals("MYTHIC")) {
                                highTier.add(lw);
                            }
                        }
                        if (!highTier.isEmpty()) {
                            com.jglims.plugin.legendary.LegendaryWeapon chosen =
                                highTier.get(random.nextInt(highTier.size()));
                            ItemStack weapon = plugin.getLegendaryWeaponManager().createWeaponItem(chosen);
                            if (weapon != null) {
                                world.dropItemNaturally(lootLoc, weapon);
                            }
                        }
                    } catch (Exception ignored) {}
                }
            }
        } catch (Exception ignored) {}

        try {
            if (plugin.getPowerUpManager() != null) {
                // Drop power-up items using known item types
                world.dropItemNaturally(lootLoc, plugin.getPowerUpManager().createHeartCrystal());
                world.dropItemNaturally(lootLoc, plugin.getPowerUpManager().createSoulFragment());
                world.dropItemNaturally(lootLoc, plugin.getPowerUpManager().createPhoenixFeather());
            }
        } catch (Exception ignored) {}

        // XP orbs
        for (int i = 0; i < 20; i++) {
            Location orbLoc = lootLoc.clone().add(
                    (random.nextDouble() - 0.5) * 3, 1, (random.nextDouble() - 0.5) * 3
            );
            world.spawn(orbLoc, ExperienceOrb.class, orb -> orb.setExperience(500));
        }
    }

    // ── Exit portal ───────────────────────────────────────────

    private void createExitPortal(World world) {
        if (arenaCenter == null) return;
        int cx = arenaCenter.getBlockX();
        int cy = arenaCenter.getBlockY();
        int cz = arenaCenter.getBlockZ();

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                world.getBlockAt(cx + x, cy - 1, cz + z).setType(Material.BEDROCK);
                if (x == 0 || z == 0) {
                    world.getBlockAt(cx + x, cy, cz + z).setType(Material.END_PORTAL);
                }
            }
        }
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (Math.abs(x) == 2 || Math.abs(z) == 2) {
                    world.getBlockAt(cx + x, cy - 1, cz + z).setType(Material.BEDROCK);
                    world.getBlockAt(cx + x, cy, cz + z).setType(Material.BEDROCK);
                }
            }
        }
        world.playSound(arenaCenter, Sound.BLOCK_END_PORTAL_SPAWN, 2.0f, 1.0f);
    }

    // ── Helpers ───────────────────────────────────────────────

    private List<Player> getArenaPlayers(World world) {
        List<Player> players = new ArrayList<>();
        for (Player p : world.getPlayers()) {
            if (arenaCenter != null && p.getLocation().distance(arenaCenter) <= ARENA_RADIUS + 10) {
                players.add(p);
            }
        }
        return players;
    }

    private int countMinions(World world) {
        int count = 0;
        for (Entity e : world.getEntities()) {
            if (e.getScoreboardTags().contains("abyss_minion") && !e.isDead()) {
                count++;
            }
        }
        return count;
    }

    private void clearMinions(World world) {
        for (Entity e : world.getEntities()) {
            if (e.getScoreboardTags().contains("abyss_minion")) {
                e.remove();
            }
        }
    }

    private int findArenaY(World world) {
        for (int y = 80; y > 0; y--) {
            if (world.getBlockAt(0, y, 0).getType() == Material.BEDROCK) {
                return y;
            }
        }
        return -1;
    }

    private void doAmbientParticles(World world) {
        if (arenaCenter == null) return;
        world.spawnParticle(Particle.ASH, arenaCenter, 15, 15, 10, 15, 0);
        world.spawnParticle(Particle.SOUL_FIRE_FLAME, arenaCenter.clone().add(0, 10, 0), 5, 10, 5, 10, 0.01);
        if (dragon != null && dragon.isValid()) {
            world.spawnParticle(Particle.DRAGON_BREATH, dragon.getLocation(), 10, 2, 1, 2, 0.01);
        }
    }

    public boolean isActive() { return active; }
}
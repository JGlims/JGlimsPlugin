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
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.time.Duration;
import java.util.*;

/**
 * AbyssDragonBoss v5.0
 *
 * CHANGES:
 *  - Dragon spawns when a player ENTERS the Abyss dimension (PlayerChangedWorldEvent)
 *  - Dragon is confined to arena at (0, sY, -120) via ARENA_CENTER_Z
 *  - Max 3 minions instead of 6 (less enderman spam)
 *  - Better confinement: teleport dragon back more aggressively
 *  - manualTrigger() still works as backup
 *  - 30-minute cooldown between fights
 */
public class AbyssDragonBoss implements Listener {

    private static final double DRAGON_HP = 1500.0;
    private static final double DAMAGE_REDUCTION = 0.20;
    private static final int ARENA_R = 40;
    private static final int ARENA_CENTER_Z = -120;
    private static final int MAX_MINIONS = 3;
    private static final long RESPAWN_COOLDOWN_MS = 30L * 60 * 1000;

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
    //  TRIGGER: Player enters Abyss dimension
    // ═══════════════════════════════════════════════════════════
    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        if (!player.getWorld().getName().equals("world_abyss")) return;
        if (active) return;
        if (lastDeathTime > 0 && System.currentTimeMillis() - lastDeathTime < RESPAWN_COOLDOWN_MS) return;

        // Delay spawn slightly so player loads in first
        new BukkitRunnable() {
            @Override
            public void run() {
                if (active) return;
                World world = player.getWorld();
                if (!world.getName().equals("world_abyss")) return;
                arenaY = findArenaY(world);
                if (arenaY < 0) {
                    arenaY = dimMgr.findSafeY(world, 0, ARENA_CENTER_Z) - 1;
                }
                arenaCenter = new Location(world, 0.5, arenaY + 1, ARENA_CENTER_Z + 0.5);
                plugin.getLogger().info("[DragonBoss] Player " + player.getName() + " entered Abyss — spawning dragon at (0, " + arenaY + ", " + ARENA_CENTER_Z + ")");
                startFight(player);
            }
        }.runTaskLater(plugin, 100L); // 5 seconds after entering
    }

    /**
     * Manual trigger — /jglims abyss boss
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
        if (arenaY < 0) arenaY = player.getLocation().getBlockY() - 1;
        arenaCenter = new Location(world, 0.5, arenaY + 1, ARENA_CENTER_Z + 0.5);
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

        Location spawnLoc = arenaCenter.clone().add(0, 15, 0);
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
                Component.text("It awaits you in the arena...", NamedTextColor.RED, TextDecoration.ITALIC),
                times
            ));
            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 2f, 0.5f);
            p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1f, 0.7f);
        }

        world.strikeLightningEffect(arenaCenter.clone().add(10, 0, 10));
        world.strikeLightningEffect(arenaCenter.clone().add(-10, 0, -10));

        new BukkitRunnable() {
            @Override
            public void run() {
                if (dragon != null && dragon.isValid() && active) {
                    dragon.setPhase(EnderDragon.Phase.CIRCLING);
                    dragon.setPodium(arenaCenter);
                }
            }
        }.runTaskLater(plugin, 40L);

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
        plugin.getLogger().info("[DragonBoss] Fight started! HP=" + DRAGON_HP);
    }

    // ═══════════════════════════════════════════════════════════
    //  ATTACKS
    // ═══════════════════════════════════════════════════════════
    private void doAttacks(World world) {
        if (dragon == null || !dragon.isValid()) return;
        double hp = dragon.getHealth() / DRAGON_HP;
        List<Player> players = getPlayersInArena(world);
        if (players.isEmpty()) return;

        if (attackTick % 5 == 0) {
            int bolts = hp < 0.25 ? 4 : hp < 0.50 ? 3 : 2;
            for (int i = 0; i < bolts; i++) {
                Player t = players.get(rng.nextInt(players.size()));
                Location strike = t.getLocation().add((rng.nextDouble()-0.5)*5, 0, (rng.nextDouble()-0.5)*5);
                world.strikeLightningEffect(strike);
                for (Player p : players) if (p.getLocation().distance(strike) < 3.5) p.damage(7);
            }
        }
        if (attackTick % 4 == 0) {
            Location dl = dragon.getLocation();
            world.spawnParticle(Particle.DRAGON_BREATH, dl, 80, 4, 2, 4, 0.03);
            for (Player p : players) {
                if (p.getLocation().distance(dl) < 12) {
                    p.damage(6);
                    p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 80, 1));
                }
            }
        }
        if (attackTick % 10 == 0) { // less frequent minion spawning
            int cur = countMinions(world);
            if (cur < MAX_MINIONS) {
                Location ml = arenaCenter.clone().add((rng.nextDouble()-0.5)*30, 1, (rng.nextDouble()-0.5)*30);
                if (rng.nextBoolean()) {
                    world.spawn(ml, WitherSkeleton.class, ws -> {
                        ws.customName(Component.text("Abyssal Guard", NamedTextColor.DARK_RED));
                        ws.setCustomNameVisible(true);
                        ws.addScoreboardTag("abyss_minion");
                        Objects.requireNonNull(ws.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(60);
                        ws.setHealth(60);
                        ws.getEquipment().setItemInMainHand(new ItemStack(Material.NETHERITE_SWORD));
                    });
                } else {
                    world.spawn(ml, Enderman.class, e -> {
                        e.customName(Component.text("Void Servant", NamedTextColor.DARK_PURPLE));
                        e.setCustomNameVisible(true);
                        e.addScoreboardTag("abyss_minion");
                        Objects.requireNonNull(e.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(40);
                        e.setHealth(40);
                    });
                }
            }
        }
        if (hp < 0.50 && attackTick % 6 == 0) {
            world.spawnParticle(Particle.EXPLOSION, arenaCenter, 10, 8, 1, 8, 0.1);
            world.playSound(arenaCenter, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.5f);
            for (Player p : players) {
                if (p.getLocation().distance(arenaCenter) < 15) {
                    p.damage(8);
                    Vector kb = p.getLocation().toVector().subtract(arenaCenter.toVector()).normalize().multiply(1.2);
                    kb.setY(0.5);
                    p.setVelocity(p.getVelocity().add(kb));
                }
            }
        }
        if (hp < 0.25) {
            for (Player p : players) {
                Vector pull = dragon.getLocation().toVector().subtract(p.getLocation().toVector()).normalize().multiply(0.3);
                p.setVelocity(p.getVelocity().add(pull));
                p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 60, 0));
            }
            world.spawnParticle(Particle.SOUL_FIRE_FLAME, dragon.getLocation(), 30, 4, 4, 4, 0.05);
        }
        if (hp < 0.10) {
            for (int a = 0; a < 360; a += 45) {
                double r = Math.toRadians(a + attackTick * 8);
                world.strikeLightningEffect(new Location(world,
                    arenaCenter.getX() + 12*Math.cos(r), arenaCenter.getY(), arenaCenter.getZ() + 12*Math.sin(r)));
            }
        }
        if (hp < 0.75 && hp > 0.74 && attackTick % 10 == 0) showPhaseTitle(world, "Phase 2: Fury Awakens", NamedTextColor.RED);
        if (hp < 0.50 && hp > 0.49 && attackTick % 10 == 0) showPhaseTitle(world, "Phase 3: Void Tremor", NamedTextColor.DARK_PURPLE);
        if (hp < 0.25 && hp > 0.24 && attackTick % 10 == 0) showPhaseTitle(world, "Phase 4: Soul Harvest", NamedTextColor.DARK_RED);
    }

    private void showPhaseTitle(World world, String sub, NamedTextColor c) {
        Title.Times t = Title.Times.times(Duration.ofMillis(200), Duration.ofSeconds(2), Duration.ofMillis(500));
        for (Player p : world.getPlayers()) {
            p.showTitle(Title.title(Component.text("\u2620", NamedTextColor.DARK_PURPLE), Component.text(sub, c, TextDecoration.BOLD), t));
            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.5f, 0.6f);
        }
    }

    private void doPhaseCycle() {
        if (dragon == null || !dragon.isValid()) return;
        if (phaseTick % 3 != 0) return;
        phaseIndex = (phaseIndex + 1) % DRAGON_PHASES.length;
        try { dragon.setPhase(DRAGON_PHASES[phaseIndex]); }
        catch (Exception e) { dragon.setPhase(EnderDragon.Phase.CIRCLING); }
    }

    // ═══════════════════════════════════════════════════════════
    //  CONFINEMENT — keep dragon at arena
    // ═══════════════════════════════════════════════════════════
    private void doConfinement() {
        if (dragon == null || !dragon.isValid() || arenaCenter == null) return;
        Location dl = dragon.getLocation();
        double dist = dl.distance(arenaCenter);
        // Aggressive confinement: if more than arena radius + 10, teleport back
        if (dist > ARENA_R + 10) {
            dragon.teleport(arenaCenter.clone().add(0, 12, 0));
            dragon.setPodium(arenaCenter);
            dragon.setPhase(EnderDragon.Phase.CIRCLING);
        } else if (dist > ARENA_R) {
            dragon.setPodium(arenaCenter);
        }
        if (dl.getY() < arenaY - 3) dragon.teleport(arenaCenter.clone().add(0, 10, 0));
        if (dl.getY() > arenaY + 50) {
            dragon.setPodium(arenaCenter);
            dragon.setPhase(EnderDragon.Phase.FLY_TO_PORTAL);
        }
        // Re-set podium every tick to keep it focused
        dragon.setPodium(arenaCenter);
    }

    private void doAmbientParticles(World world) {
        if (arenaCenter == null) return;
        world.spawnParticle(Particle.ASH, arenaCenter, 15, 15, 8, 15, 0);
        if (dragon != null && dragon.isValid()) {
            world.spawnParticle(Particle.DRAGON_BREATH, dragon.getLocation(), 8, 2, 1, 2, 0.01);
        }
    }

    @EventHandler
    public void onBossDamage(EntityDamageEvent event) {
        if (!active || !(event.getEntity() instanceof EnderDragon d)) return;
        if (dragon == null || !d.getUniqueId().equals(dragon.getUniqueId())) return;
        event.setDamage(event.getDamage() * (1.0 - DAMAGE_REDUCTION));
        EntityDamageEvent.DamageCause cause = event.getCause();
        if (cause == EntityDamageEvent.DamageCause.SUFFOCATION || cause == EntityDamageEvent.DamageCause.DROWNING || cause == EntityDamageEvent.DamageCause.FALL)
            event.setCancelled(true);
    }

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
                Component.text("The Abyssal Dragon has been vanquished!", NamedTextColor.YELLOW), times));
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.5f, 1f);
        }
        dropLoot(world);
        createExitPortal(world);
        new BukkitRunnable() {
            int c = 0;
            @Override public void run() {
                if (c++ >= 12 || arenaCenter == null) { cancel(); return; }
                world.spawnParticle(Particle.FIREWORK, arenaCenter.clone().add(0,5,0), 100, 10, 10, 10, 0.3);
                world.playSound(arenaCenter, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 1.5f, 1f);
            }
        }.runTaskTimer(plugin, 0L, 20L);
        dragon = null;
        plugin.getLogger().info("[DragonBoss] Dragon defeated!");
    }

    private void dropLoot(World world) {
        if (arenaCenter == null) return;
        Location dl = arenaCenter.clone().add(0, 1, 0);
        world.dropItemNaturally(dl, new ItemStack(Material.DRAGON_EGG, 1));
        world.dropItemNaturally(dl, new ItemStack(Material.NETHER_STAR, 3));
        world.dropItemNaturally(dl, new ItemStack(Material.EXPERIENCE_BOTTLE, 64));
        world.dropItemNaturally(dl, new ItemStack(Material.NETHERITE_INGOT, 4));
        world.dropItemNaturally(dl, new ItemStack(Material.ELYTRA, 1));
        world.dropItemNaturally(dl, new ItemStack(Material.TOTEM_OF_UNDYING, 2));
        world.dropItemNaturally(dl, new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 8));
        world.dropItemNaturally(dl, new ItemStack(Material.END_CRYSTAL, 4));
        try {
            if (plugin.getLegendaryWeaponManager() != null) {
                List<LegendaryWeapon> high = new ArrayList<>();
                for (LegendaryWeapon lw : LegendaryWeapon.values()) {
                    String tn = lw.getTier().name();
                    if (tn.equals("ABYSSAL") || tn.equals("MYTHIC")) high.add(lw);
                }
                for (int i = 0; i < 2 && !high.isEmpty(); i++) {
                    ItemStack w = plugin.getLegendaryWeaponManager().createWeapon(high.get(rng.nextInt(high.size())));
                    if (w != null) world.dropItemNaturally(dl, w);
                }
            }
        } catch (Exception e) { plugin.getLogger().warning("[DragonBoss] Weapon drop error: " + e.getMessage()); }
        try {
            if (plugin.getPowerUpManager() != null) {
                for (int i = 0; i < 2; i++) world.dropItemNaturally(dl, plugin.getPowerUpManager().createPhoenixFeather());
                for (int i = 0; i < 3; i++) world.dropItemNaturally(dl, plugin.getPowerUpManager().createHeartCrystal());
                for (int i = 0; i < 5; i++) world.dropItemNaturally(dl, plugin.getPowerUpManager().createSoulFragment());
            }
        } catch (Exception e) { plugin.getLogger().warning("[DragonBoss] Power-up drop error: " + e.getMessage()); }
        for (int i = 0; i < 20; i++) {
            Location ol = dl.clone().add((rng.nextDouble()-0.5)*4, 1, (rng.nextDouble()-0.5)*4);
            world.spawn(ol, ExperienceOrb.class, o -> o.setExperience(500));
        }
    }

    private void createExitPortal(World world) {
        if (arenaCenter == null) return;
        int cx = arenaCenter.getBlockX(), cy = arenaCenter.getBlockY(), cz = arenaCenter.getBlockZ();
        for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++) {
            world.getBlockAt(cx+x, cy-1, cz+z).setType(Material.BEDROCK);
            if (Math.abs(x) <= 1 && Math.abs(z) <= 1) world.getBlockAt(cx+x, cy, cz+z).setType(Material.END_PORTAL);
        }
        world.playSound(arenaCenter, Sound.BLOCK_END_PORTAL_SPAWN, 2f, 1f);
    }

    private List<Player> getPlayersInArena(World world) {
        List<Player> p = new ArrayList<>();
        for (Player pl : world.getPlayers())
            if (arenaCenter != null && pl.getLocation().distance(arenaCenter) <= ARENA_R + 15) p.add(pl);
        return p;
    }
    private int countMinions(World world) {
        int c = 0;
        for (Entity e : world.getEntities()) if (e.getScoreboardTags().contains("abyss_minion") && !e.isDead()) c++;
        return c;
    }
    private void clearMinions(World world) {
        for (Entity e : world.getEntities()) if (e.getScoreboardTags().contains("abyss_minion")) e.remove();
    }
    private int findArenaY(World world) {
        for (int y = 120; y > 0; y--) if (world.getBlockAt(0, y, ARENA_CENTER_Z).getType() == Material.BEDROCK) return y;
        for (int y = 0; y >= -64; y--) if (world.getBlockAt(0, y, ARENA_CENTER_Z).getType() == Material.BEDROCK) return y;
        for (int y = 120; y > 0; y--) if (world.getBlockAt(0, y, 0).getType() == Material.BEDROCK) return y;
        return -1;
    }
    public boolean isActive() { return active; }
}
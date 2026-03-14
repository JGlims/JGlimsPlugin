package com.jglims.plugin.abyss;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.legendary.LegendaryTier;
import com.jglims.plugin.legendary.LegendaryWeapon;
import com.jglims.plugin.legendary.LegendaryWeaponManager;
import com.jglims.plugin.powerups.PowerUpManager;
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

public class AbyssDragonBoss implements Listener {

    // ── Constants ──────────────────────────────────────────────────────
    private static final double DRAGON_MAX_HP = 1500.0;
    private static final double DAMAGE_REDUCTION = 0.20;
    private static final int ARENA_RADIUS = 40;
    private static final int MAX_MINIONS = 4;
    private static final long RESPAWN_COOLDOWN_MS = 30L * 60 * 1000;

    // ── State ──────────────────────────────────────────────────────────
    private final JGlimsPlugin plugin;
    private final AbyssDimensionManager dimMgr;
    private EnderDragon dragon;
    private boolean active = false;
    private long lastEnd = 0;
    private int atkTick = 0;
    private int phaseTick = 0;
    private int phaseIdx = 0;
    private int currentPhase = 1;
    private Location center;
    private int arenaY;
    private BukkitRunnable loop;
    private final Random rng = new Random();

    private static final EnderDragon.Phase[] CLOSE_PHASES = {
        EnderDragon.Phase.CHARGE_PLAYER,
        EnderDragon.Phase.BREATH_ATTACK,
        EnderDragon.Phase.STRAFING,
        EnderDragon.Phase.LAND_ON_PORTAL,
        EnderDragon.Phase.CHARGE_PLAYER,
        EnderDragon.Phase.SEARCH_FOR_BREATH_ATTACK_TARGET
    };

    // ── Constructor ────────────────────────────────────────────────────
    public AbyssDragonBoss(JGlimsPlugin plugin, AbyssDimensionManager dimensionManager) {
        this.plugin = plugin;
        this.dimMgr = dimensionManager;
    }

    // ══════════════════════════════════════════════════════════════════
    //  TRIGGER
    // ══════════════════════════════════════════════════════════════════
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (active) return;
        Player p = event.getPlayer();
        World w = p.getWorld();
        if (!w.getName().equals("world_abyss")) return;
        if (System.currentTimeMillis() - lastEnd < RESPAWN_COOLDOWN_MS && lastEnd > 0) return;

        Location loc = p.getLocation();
        Block below = w.getBlockAt(loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ());
        if (below.getType() != Material.BEDROCK) return;
        if (Math.abs(loc.getX()) > ARENA_RADIUS && Math.abs(loc.getZ()) > ARENA_RADIUS) return;

        arenaY = findArenaY(w);
        if (arenaY < 0 || Math.abs(loc.getBlockY() - arenaY) > 5) return;

        center = new Location(w, 0.5, arenaY + 1, 0.5);
        startFight(p);
    }

    // ══════════════════════════════════════════════════════════════════
    //  START
    // ══════════════════════════════════════════════════════════════════
    private void startFight(Player trigger) {
        if (active) return;
        active = true;
        currentPhase = 1;
        World w = trigger.getWorld();

        // Clean up any existing entities
        for (EnderDragon d : w.getEntitiesByClass(EnderDragon.class)) d.remove();
        for (Wither wt : w.getEntitiesByClass(Wither.class)) wt.remove();
        clearMinions(w);

        // Spawn dragon close to arena
        Location spawn = center.clone().add(0, 8, 0);
        dragon = w.spawn(spawn, EnderDragon.class, d -> {
            d.customName(Component.text("\u2620 Abyssal Dragon \u2620", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD));
            d.setCustomNameVisible(true);
            d.setGlowing(true);
            Objects.requireNonNull(d.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(DRAGON_MAX_HP);
            d.setHealth(DRAGON_MAX_HP);
            d.setPodium(center);
            d.setPhase(EnderDragon.Phase.CIRCLING);
        });

        // Title and sounds
        Title.Times tt = Title.Times.times(Duration.ofMillis(300), Duration.ofSeconds(3), Duration.ofMillis(500));
        for (Player p : w.getPlayers()) {
            p.showTitle(Title.title(
                Component.text("ABYSSAL DRAGON", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD),
                Component.text("Your soul will be consumed...", NamedTextColor.RED, TextDecoration.ITALIC), tt));
            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 2f, 0.5f);
            p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1f, 0.7f);
        }
        w.strikeLightningEffect(center.clone().add(5, 0, 5));
        w.strikeLightningEffect(center.clone().add(-5, 0, -5));

        // After 3 seconds, force into close-combat phase
        new BukkitRunnable() {
            @Override public void run() {
                if (dragon != null && dragon.isValid() && active) {
                    dragon.setPhase(EnderDragon.Phase.CHARGE_PLAYER);
                }
            }
        }.runTaskLater(plugin, 60L);

        atkTick = 0;
        phaseTick = 0;
        phaseIdx = 0;

        // Main combat loop – every 2 seconds (40 ticks)
        loop = new BukkitRunnable() {
            @Override public void run() {
                if (!active || dragon == null || dragon.isDead() || !dragon.isValid()) {
                    cancel();
                    return;
                }
                atkTick++;
                phaseTick++;
                doPhaseManagement(w);
                doAttacks(w);
                doCycle();
                doConfine();
                doParticles(w);
            }
        };
        loop.runTaskTimer(plugin, 80L, 40L);
    }

    // ══════════════════════════════════════════════════════════════════
    //  PHASE MANAGEMENT (4 phases based on HP %)
    // ══════════════════════════════════════════════════════════════════
    private void doPhaseManagement(World w) {
        if (dragon == null || !dragon.isValid()) return;
        double hpPct = dragon.getHealth() / DRAGON_MAX_HP;
        int newPhase;
        if (hpPct > 0.75) newPhase = 1;
        else if (hpPct > 0.50) newPhase = 2;
        else if (hpPct > 0.25) newPhase = 3;
        else newPhase = 4;

        if (newPhase != currentPhase) {
            currentPhase = newPhase;
            String phaseName;
            NamedTextColor color;
            switch (currentPhase) {
                case 2:
                    phaseName = "GROUND POUND";
                    color = NamedTextColor.GOLD;
                    break;
                case 3:
                    phaseName = "VOID PULL";
                    color = NamedTextColor.DARK_RED;
                    break;
                case 4:
                    phaseName = "ENRAGE";
                    color = NamedTextColor.RED;
                    break;
                default:
                    phaseName = "AWAKEN";
                    color = NamedTextColor.DARK_PURPLE;
                    break;
            }
            Title.Times tt = Title.Times.times(Duration.ofMillis(200), Duration.ofSeconds(2), Duration.ofMillis(300));
            for (Player p : w.getPlayers()) {
                p.showTitle(Title.title(
                    Component.text("Phase " + currentPhase + ": " + phaseName, color, TextDecoration.BOLD),
                    Component.text("The dragon's power shifts!", NamedTextColor.GRAY, TextDecoration.ITALIC), tt));
                p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 2f, 0.3f + (currentPhase * 0.15f));
            }
            // Lightning burst on phase change
            for (int i = 0; i < currentPhase; i++) {
                double a = rng.nextDouble() * Math.PI * 2;
                w.strikeLightningEffect(center.clone().add(Math.cos(a) * 15, 0, Math.sin(a) * 15));
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  ATTACKS
    // ══════════════════════════════════════════════════════════════════
    private void doAttacks(World w) {
        if (dragon == null || !dragon.isValid()) return;
        double hp = dragon.getHealth() / DRAGON_MAX_HP;
        List<Player> near = getPlayers(w);
        if (near.isEmpty()) return;

        // ── Lightning strikes (every 5 loops = 10 seconds) ──
        if (atkTick % 5 == 0) {
            int n = currentPhase >= 4 ? 5 : currentPhase >= 3 ? 4 : currentPhase >= 2 ? 3 : 2;
            for (int i = 0; i < n; i++) {
                Player t = near.get(rng.nextInt(near.size()));
                Location tl = t.getLocation().add((rng.nextDouble() - 0.5) * 4, 0, (rng.nextDouble() - 0.5) * 4);
                w.strikeLightningEffect(tl);
                for (Player p : near) {
                    if (p.getLocation().distance(tl) < 3) p.damage(6);
                }
            }
        }

        // ── Void breath (every 4 loops = 8 seconds) ──
        if (atkTick % 4 == 0) {
            Location dl = dragon.getLocation();
            w.spawnParticle(Particle.DRAGON_BREATH, dl, 80, 4, 2, 4, 0.02);
            for (Player p : near) {
                if (p.getLocation().distance(dl) < 10) {
                    p.damage(5);
                    p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 60, 1));
                }
            }
            w.playSound(dl, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.5f, 0.7f);
        }

        // ── Ground pound (phase 2+, every 6 loops = 12 seconds) ──
        if (currentPhase >= 2 && atkTick % 6 == 0) {
            w.spawnParticle(Particle.EXPLOSION, center, 30, 8, 1, 8, 0.1);
            w.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 2f, 0.5f);
            for (Player p : near) {
                if (p.getLocation().distance(center) < 20) {
                    p.damage(8);
                    Vector kb = p.getLocation().toVector().subtract(center.toVector()).normalize().multiply(1.5).setY(0.6);
                    p.setVelocity(p.getVelocity().add(kb));
                }
            }
        }

        // ── Void pull (phase 3+, every 3 loops = 6 seconds) ──
        if (currentPhase >= 3 && atkTick % 3 == 0) {
            for (Player p : near) {
                Vector pull = dragon.getLocation().toVector().subtract(p.getLocation().toVector()).normalize().multiply(0.4);
                p.setVelocity(p.getVelocity().add(pull));
                p.damage(3);
                p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 40, 0));
            }
            w.spawnParticle(Particle.SOUL_FIRE_FLAME, dragon.getLocation(), 40, 3, 3, 3, 0.05);
            w.spawnParticle(Particle.SOUL, dragon.getLocation(), 20, 4, 4, 4, 0.02);
        }

        // ── Enrage attacks (phase 4, every 2 loops = 4 seconds) ──
        if (currentPhase >= 4 && atkTick % 2 == 0) {
            for (Player p : near) {
                p.damage(4);
                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1));
                w.spawnParticle(Particle.SCULK_CHARGE_POP, p.getLocation(), 15, 1, 1, 1, 0.05);
            }
            // Rapid lightning ring
            for (int a = 0; a < 360; a += 45) {
                double r = Math.toRadians(a);
                w.strikeLightningEffect(center.clone().add(Math.cos(r) * (ARENA_RADIUS - 5), 0, Math.sin(r) * (ARENA_RADIUS - 5)));
            }
        }

        // ── Minion spawning (every 8 loops = 16 seconds, max 4) ──
        if (atkTick % 8 == 0) {
            int cur = countMinions(w);
            if (cur < MAX_MINIONS) {
                int s = Math.min(2, MAX_MINIONS - cur);
                for (int i = 0; i < s; i++) {
                    Location ml = center.clone().add((rng.nextDouble() - 0.5) * 20, 1, (rng.nextDouble() - 0.5) * 20);
                    if (i % 2 == 0) {
                        w.spawn(ml, Enderman.class, e -> {
                            e.customName(Component.text("Void Servant", NamedTextColor.DARK_PURPLE));
                            e.addScoreboardTag("abyss_minion");
                            Objects.requireNonNull(e.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(40);
                            e.setHealth(40);
                        });
                    } else {
                        w.spawn(ml, WitherSkeleton.class, ws -> {
                            ws.customName(Component.text("Abyssal Guard", NamedTextColor.DARK_RED));
                            ws.addScoreboardTag("abyss_minion");
                            Objects.requireNonNull(ws.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(60);
                            ws.setHealth(60);
                            ws.getEquipment().setItemInMainHand(new ItemStack(Material.NETHERITE_SWORD));
                        });
                    }
                }
                w.playSound(center, Sound.ENTITY_ENDERMAN_TELEPORT, 1.5f, 0.5f);
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  PHASE CYCLING (close-combat oriented)
    // ══════════════════════════════════════════════════════════════════
    private void doCycle() {
        if (dragon == null || !dragon.isValid()) return;
        if (phaseTick % 3 != 0) return;
        phaseIdx = (phaseIdx + 1) % CLOSE_PHASES.length;
        try {
            dragon.setPhase(CLOSE_PHASES[phaseIdx]);
        } catch (Exception e) {
            dragon.setPhase(EnderDragon.Phase.CIRCLING);
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  CONFINEMENT
    // ══════════════════════════════════════════════════════════════════
    private void doConfine() {
        if (dragon == null || !dragon.isValid() || center == null) return;
        Location dl = dragon.getLocation();
        double dist = dl.distance(center);
        if (dist > ARENA_RADIUS + 15) {
            dragon.setPodium(center);
            dragon.teleport(center.clone().add(0, 15, 0));
            dragon.setPhase(EnderDragon.Phase.CIRCLING);
        } else if (dist > ARENA_RADIUS) {
            dragon.setPodium(center);
        }
        if (dl.getY() < arenaY - 2) dragon.teleport(center.clone().add(0, 10, 0));
        if (dl.getY() > arenaY + 50) {
            dragon.setPodium(center);
            dragon.setPhase(EnderDragon.Phase.FLY_TO_PORTAL);
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  PARTICLES
    // ══════════════════════════════════════════════════════════════════
    private void doParticles(World w) {
        if (center == null) return;
        w.spawnParticle(Particle.ASH, center, 15, 15, 10, 15, 0);
        if (dragon != null && dragon.isValid()) {
            w.spawnParticle(Particle.DRAGON_BREATH, dragon.getLocation(), 10, 2, 1, 2, 0.01);
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  DAMAGE HANDLER
    // ══════════════════════════════════════════════════════════════════
    @EventHandler
    public void onBossDamage(EntityDamageEvent event) {
        if (!active || !(event.getEntity() instanceof EnderDragon)) return;
        EnderDragon d = (EnderDragon) event.getEntity();
        if (dragon == null || !d.getUniqueId().equals(dragon.getUniqueId())) return;
        event.setDamage(event.getDamage() * (1 - DAMAGE_REDUCTION));
        EntityDamageEvent.DamageCause c = event.getCause();
        if (c == EntityDamageEvent.DamageCause.SUFFOCATION
            || c == EntityDamageEvent.DamageCause.DROWNING
            || c == EntityDamageEvent.DamageCause.FALL) {
            event.setCancelled(true);
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  DEATH → LOOT
    // ══════════════════════════════════════════════════════════════════
    @EventHandler
    public void onBossDeath(EntityDeathEvent event) {
        if (!active || !(event.getEntity() instanceof EnderDragon)) return;
        EnderDragon d = (EnderDragon) event.getEntity();
        if (dragon == null || !d.getUniqueId().equals(dragon.getUniqueId())) return;

        event.getDrops().clear();
        event.setDroppedExp(0);
        active = false;
        lastEnd = System.currentTimeMillis();

        if (loop != null) {
            try { loop.cancel(); } catch (Exception ignored) {}
        }

        World w = d.getWorld();
        clearMinions(w);

        // Victory title
        Title.Times tt = Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(5), Duration.ofMillis(1500));
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.showTitle(Title.title(
                Component.text("VICTORY!", NamedTextColor.GOLD, TextDecoration.BOLD),
                Component.text("The Abyssal Dragon has been vanquished!", NamedTextColor.YELLOW), tt));
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.5f, 1f);
        }

        dropLoot(w);
        createExit(w);

        // Firework celebration
        new BukkitRunnable() {
            int c = 0;
            @Override public void run() {
                if (c++ >= 10 || center == null) { cancel(); return; }
                w.spawnParticle(Particle.FIREWORK, center.clone().add(0, 5, 0), 100, 8, 8, 8, 0.3);
                w.playSound(center, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 1.5f, 1f);
            }
        }.runTaskTimer(plugin, 0L, 20L);

        dragon = null;
    }

    // ══════════════════════════════════════════════════════════════════
    //  LOOT DROPS – includes all 4 ABYSSAL weapons
    // ══════════════════════════════════════════════════════════════════
    private void dropLoot(World w) {
        if (center == null) return;
        Location l = center.clone().add(0, 1, 0);

        // Guaranteed drops
        w.dropItemNaturally(l, new ItemStack(Material.DRAGON_EGG, 1));
        w.dropItemNaturally(l, new ItemStack(Material.NETHER_STAR, 3));
        w.dropItemNaturally(l, new ItemStack(Material.EXPERIENCE_BOTTLE, 64));
        w.dropItemNaturally(l, new ItemStack(Material.NETHERITE_INGOT, 4));
        w.dropItemNaturally(l, new ItemStack(Material.ELYTRA, 1));
        w.dropItemNaturally(l, new ItemStack(Material.TOTEM_OF_UNDYING, 2));
        w.dropItemNaturally(l, new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 8));
        w.dropItemNaturally(l, new ItemStack(Material.END_CRYSTAL, 4));

        // Drop all 4 ABYSSAL weapons
        LegendaryWeaponManager weaponMgr = plugin.getLegendaryWeaponManager();
        if (weaponMgr != null) {
            LegendaryWeapon[] abyssals = LegendaryWeapon.byTier(LegendaryTier.ABYSSAL);
            for (LegendaryWeapon aw : abyssals) {
                ItemStack weaponItem = weaponMgr.createWeapon(aw);
                if (weaponItem != null) {
                    w.dropItemNaturally(l, weaponItem);
                }
            }
        }

        // Power-up drops
        PowerUpManager puMgr = plugin.getPowerUpManager();
        if (puMgr != null) {
            try {
                ItemStack phoenix = puMgr.createPhoenixFeather();
                if (phoenix != null) { phoenix.setAmount(2); w.dropItemNaturally(l, phoenix); }
                ItemStack heart = puMgr.createHeartCrystal();
                if (heart != null) { heart.setAmount(3); w.dropItemNaturally(l, heart); }
                ItemStack soul = puMgr.createSoulFragment();
                if (soul != null) { soul.setAmount(5); w.dropItemNaturally(l, soul); }
            } catch (Exception ignored) {}
        }

        // XP orbs
        for (int i = 0; i < 20; i++) {
            Location ol = l.clone().add((rng.nextDouble() - 0.5) * 6, 0.5, (rng.nextDouble() - 0.5) * 6);
            w.spawn(ol, ExperienceOrb.class, orb -> orb.setExperience(100 + rng.nextInt(200)));
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  EXIT PORTAL
    // ══════════════════════════════════════════════════════════════════
    private void createExit(World w) {
        if (center == null) return;
        int cy = arenaY + 1;
        // Obsidian ring
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (x * x + z * z <= 4) {
                    w.getBlockAt(x, cy, z).setType(Material.OBSIDIAN);
                }
            }
        }
        // End portal blocks in center
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                w.getBlockAt(x, cy, z).setType(Material.END_PORTAL);
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  UTILITY
    // ══════════════════════════════════════════════════════════════════
    private List<Player> getPlayers(World w) {
        List<Player> result = new ArrayList<>();
        if (center == null) return result;
        for (Player p : w.getPlayers()) {
            if (p.getLocation().distance(center) < ARENA_RADIUS + 20) {
                result.add(p);
            }
        }
        return result;
    }

    private int countMinions(World w) {
        int count = 0;
        for (Entity e : w.getEntities()) {
            if (e.getScoreboardTags().contains("abyss_minion") && !e.isDead()) count++;
        }
        return count;
    }

    private void clearMinions(World w) {
        for (Entity e : w.getEntities()) {
            if (e.getScoreboardTags().contains("abyss_minion")) e.remove();
        }
    }

    private int findArenaY(World w) {
        for (int y = 0; y < 60; y++) {
            if (w.getBlockAt(0, y, 0).getType() == Material.BEDROCK) return y;
        }
        return -1;
    }

    public boolean isActive() { return active; }
    public JGlimsPlugin getPlugin() { return plugin; }
}
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
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Abyss Dragon Boss — Phase 3 Rework.
 * 
 * Dual boss fight on the BEDROCK arena.
 * Phase 1: Ender Dragon (1000 HP, custom attacks, aggressive)
 * Phase 2 (at 40% HP): Abyss Herald (Wither, 1000 HP) joins
 * Both must die. On final death: exit portal + Dragon Egg + ABYSSAL loot.
 * 
 * Boss ONLY spawns when a player steps on BEDROCK in the arena area.
 * Vanilla dragons are continuously removed by AbyssDimensionManager.
 */
public class AbyssDragonBoss implements Listener {

    private final JGlimsPlugin plugin;
    private final AbyssDimensionManager dimensionManager;
    private final NamespacedKey KEY_ABYSS_DRAGON;
    private final NamespacedKey KEY_ABYSS_HERALD;
    private final Random random = new Random();

    private EnderDragon dragonBoss;
    private Wither heraldBoss;
    private boolean active = false;
    private boolean heraldSpawned = false;
    private int tickCounter = 0;
    private long lastKillTime = 0;
    private int bossesAlive = 0;
    private BukkitRunnable attackTask;
    private static final long RESPAWN_COOLDOWN_MS = 30 * 60 * 1000L;
    private static final double DRAGON_MAX_HP = 1000;
    private static final double HERALD_MAX_HP = 1000;
    private static final double HERALD_SPAWN_THRESHOLD = 0.40; // 40% HP

    public AbyssDragonBoss(JGlimsPlugin plugin, AbyssDimensionManager dimensionManager) {
        this.plugin = plugin;
        this.dimensionManager = dimensionManager;
        this.KEY_ABYSS_DRAGON = new NamespacedKey(plugin, "abyss_dragon");
        this.KEY_ABYSS_HERALD = new NamespacedKey(plugin, "abyss_herald");
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (active) return;
        Player player = event.getPlayer();
        World abyssWorld = dimensionManager.getAbyssWorld();
        if (abyssWorld == null || !player.getWorld().equals(abyssWorld)) return;
        if (System.currentTimeMillis() - lastKillTime < RESPAWN_COOLDOWN_MS) return;

        Location loc = player.getLocation();
        // Check if standing on BEDROCK within arena radius (30 blocks from center)
        Block below = loc.clone().subtract(0, 1, 0).getBlock();
        if (below.getType() != Material.BEDROCK) return;

        double dist = Math.sqrt(loc.getX() * loc.getX() + loc.getZ() * loc.getZ());
        if (dist > 32) return; // outside arena

        plugin.getLogger().info("[Abyss] Player " + player.getName() + " stepped on arena bedrock! Starting boss fight...");
        startBossFight(abyssWorld);
    }

    private void startBossFight(World abyssWorld) {
        active = true;
        heraldSpawned = false;
        tickCounter = 0;
        bossesAlive = 1;

        // Find arena Y: scan for bedrock platform
        int arenaY = 110;
        for (int y = 150; y > 50; y--) {
            if (abyssWorld.getBlockAt(0, y, 0).getType() == Material.BEDROCK) {
                arenaY = y;
                break;
            }
        }
        int spawnY = arenaY + 15;

        plugin.getLogger().info("[Abyss] BOSS FIGHT! Arena Y=" + arenaY + ", Dragon spawns at Y=" + spawnY);

        // Remove ALL existing ender dragons and withers
        for (EnderDragon d : abyssWorld.getEntitiesByClass(EnderDragon.class)) d.remove();
        for (Wither w : abyssWorld.getEntitiesByClass(Wither.class)) w.remove();

        Location spawnLoc = new Location(abyssWorld, 0.5, spawnY, 0.5);

        // Dramatic announcement
        for (Player p : abyssWorld.getPlayers()) {
            p.showTitle(net.kyori.adventure.title.Title.title(
                Component.text("\u2620 ABYSS DRAGON \u2620", TextColor.color(170, 0, 0)).decorate(TextDecoration.BOLD),
                Component.text("The eternal guardian awakens!", NamedTextColor.DARK_RED),
                net.kyori.adventure.title.Title.Times.times(
                    java.time.Duration.ofMillis(500), java.time.Duration.ofSeconds(4), java.time.Duration.ofMillis(1000))));
            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0f, 0.2f);
            p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.5f);
            p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 60, 0, false, false));
        }

        // Spawn Ender Dragon with 1000 HP
        dragonBoss = abyssWorld.spawn(spawnLoc, EnderDragon.class, dragon -> {
            dragon.customName(Component.text("\u2620 Abyss Dragon \u2620", TextColor.color(170, 0, 0))
                    .decorate(TextDecoration.BOLD));
            dragon.setCustomNameVisible(true);
            dragon.getPersistentDataContainer().set(KEY_ABYSS_DRAGON, PersistentDataType.BYTE, (byte) 1);
            dragon.setGlowing(true);
        });

        if (dragonBoss.getAttribute(Attribute.MAX_HEALTH) != null) {
            dragonBoss.getAttribute(Attribute.MAX_HEALTH).setBaseValue(DRAGON_MAX_HP);
            dragonBoss.setHealth(DRAGON_MAX_HP);
        }

        // Spawn VFX
        abyssWorld.spawnParticle(Particle.DRAGON_BREATH, spawnLoc, 80, 10, 10, 10, 0.3);
        abyssWorld.spawnParticle(Particle.REVERSE_PORTAL, spawnLoc, 60, 10, 10, 10, 0.5);
        abyssWorld.spawnParticle(Particle.EXPLOSION_EMITTER, spawnLoc, 3, 5, 5, 5);

        // Attack loop
        attackTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!active) { cancel(); return; }
                tickCounter += 20;
                doAttacks();
                checkPhase2();
            }
        };
        attackTask.runTaskTimer(plugin, 60L, 20L);
    }

    private void checkPhase2() {
        if (heraldSpawned) return;
        if (dragonBoss == null || dragonBoss.isDead()) return;
        if (dragonBoss.getHealth() <= DRAGON_MAX_HP * HERALD_SPAWN_THRESHOLD) {
            heraldSpawned = true;
            bossesAlive = 2;
            World abyssWorld = dimensionManager.getAbyssWorld();
            if (abyssWorld == null) return;

            int arenaY = 110;
            for (int y = 150; y > 50; y--) {
                if (abyssWorld.getBlockAt(0, y, 0).getType() == Material.BEDROCK) { arenaY = y; break; }
            }

            Location heraldLoc = new Location(abyssWorld, 15.5, arenaY + 5, 15.5);

            for (Player p : abyssWorld.getPlayers()) {
                p.showTitle(net.kyori.adventure.title.Title.title(
                    Component.text("\u2620 ABYSS HERALD \u2620", TextColor.color(100, 0, 170)).decorate(TextDecoration.BOLD),
                    Component.text("A dark herald answers the dragon's call!", NamedTextColor.DARK_PURPLE),
                    net.kyori.adventure.title.Title.Times.times(
                        java.time.Duration.ofMillis(300), java.time.Duration.ofSeconds(3), java.time.Duration.ofMillis(500))));
                p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 2.0f, 0.3f);
                p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 40, 0, false, false));
            }

            heraldBoss = abyssWorld.spawn(heraldLoc, Wither.class, w -> {
                w.customName(Component.text("\u2620 Abyss Herald \u2620", TextColor.color(100, 0, 170))
                        .decorate(TextDecoration.BOLD));
                w.setCustomNameVisible(true);
                w.getPersistentDataContainer().set(KEY_ABYSS_HERALD, PersistentDataType.BYTE, (byte) 1);
                w.setGlowing(true);
            });

            if (heraldBoss.getAttribute(Attribute.MAX_HEALTH) != null) {
                heraldBoss.getAttribute(Attribute.MAX_HEALTH).setBaseValue(HERALD_MAX_HP);
                heraldBoss.setHealth(HERALD_MAX_HP);
            }

            abyssWorld.spawnParticle(Particle.REVERSE_PORTAL, heraldLoc, 60, 5, 5, 5, 0.5);
            plugin.getLogger().info("[Abyss] Abyss Herald spawned (Phase 2)! 1000 HP");
        }
    }

    private void doAttacks() {
        World world = dimensionManager.getAbyssWorld();
        if (world == null) return;
        List<Player> nearby = new ArrayList<>();
        for (Player p : world.getPlayers()) {
            double dist = Math.sqrt(p.getLocation().getX() * p.getLocation().getX()
                    + p.getLocation().getZ() * p.getLocation().getZ());
            if (dist < 80) nearby.add(p);
        }
        if (nearby.isEmpty()) return;

        // Lightning barrage every 4s (more frequent)
        if (tickCounter % 80 == 0) {
            for (int i = 0; i < 5; i++) {
                Player t = nearby.get(random.nextInt(nearby.size()));
                Location strike = t.getLocation().add(
                        random.nextGaussian() * 3, 0, random.nextGaussian() * 3);
                world.strikeLightning(strike);
            }
        }

        // Void Breath every 3s — damage + wither effect
        if (tickCounter % 60 == 0 && dragonBoss != null && !dragonBoss.isDead()) {
            for (Player p : nearby) {
                if (p.getLocation().distance(dragonBoss.getLocation()) < 25) {
                    p.damage(8.0, dragonBoss);
                    p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 60, 1, false, true));
                    p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 40, 0, false, false));
                    // Particle beam from dragon to player
                    Location from = dragonBoss.getLocation();
                    Location to = p.getLocation().add(0, 1, 0);
                    int steps = 20;
                    for (int s = 0; s < steps; s++) {
                        double t = s / (double) steps;
                        Location point = from.clone().add(
                                (to.getX() - from.getX()) * t,
                                (to.getY() - from.getY()) * t,
                                (to.getZ() - from.getZ()) * t);
                        world.spawnParticle(Particle.DRAGON_BREATH, point, 2, 0.1, 0.1, 0.1, 0.01);
                    }
                }
            }
        }

        // Summon Enderman minions every 12s
        if (tickCounter % 240 == 0) {
            int arenaY = findArenaY(world);
            for (int i = 0; i < 4; i++) {
                Location sLoc = new Location(world,
                        random.nextGaussian() * 18, arenaY + 1, random.nextGaussian() * 18);
                Enderman m = world.spawn(sLoc, Enderman.class);
                if (m.getAttribute(Attribute.MAX_HEALTH) != null) {
                    m.getAttribute(Attribute.MAX_HEALTH).setBaseValue(100);
                    m.setHealth(100);
                }
                m.customName(Component.text("Abyss Spawn", TextColor.color(100, 0, 0)));
                m.setCustomNameVisible(true);
                m.setGlowing(true);
                if (!nearby.isEmpty()) m.setTarget(nearby.get(random.nextInt(nearby.size())));
            }
        }

        // Wither Skeleton wave every 16s
        if (tickCounter % 320 == 0) {
            int arenaY = findArenaY(world);
            for (int i = 0; i < 5; i++) {
                Location sLoc = new Location(world,
                        random.nextGaussian() * 14, arenaY + 1, random.nextGaussian() * 14);
                WitherSkeleton ws = world.spawn(sLoc, WitherSkeleton.class);
                ws.customName(Component.text("Abyssal Guard", TextColor.color(80, 0, 80)));
                ws.setCustomNameVisible(true);
                if (ws.getAttribute(Attribute.MAX_HEALTH) != null) {
                    ws.getAttribute(Attribute.MAX_HEALTH).setBaseValue(60);
                    ws.setHealth(60);
                }
            }
        }

        // Herald special: Void Pull every 6s when Phase 2
        if (heraldSpawned && heraldBoss != null && !heraldBoss.isDead() && tickCounter % 120 == 0) {
            for (Player p : nearby) {
                if (p.getLocation().distance(heraldBoss.getLocation()) < 30) {
                    org.bukkit.util.Vector pull = heraldBoss.getLocation().toVector()
                            .subtract(p.getLocation().toVector()).normalize().multiply(1.5);
                    p.setVelocity(pull);
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1, false, false));
                    world.spawnParticle(Particle.REVERSE_PORTAL, p.getLocation(), 20, 1, 1, 1, 0.3);
                }
            }
        }

        // Enrage: extra damage when dragon < 20%
        if (dragonBoss != null && !dragonBoss.isDead() && dragonBoss.getHealth() < DRAGON_MAX_HP * 0.2) {
            if (tickCounter % 40 == 0) {
                for (Player p : nearby) {
                    if (p.getLocation().distance(dragonBoss.getLocation()) < 20) {
                        p.damage(6.0, dragonBoss);
                        p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 80, 2, false, true));
                    }
                }
                world.spawnParticle(Particle.DRAGON_BREATH,
                        dragonBoss.getLocation(), 40, 12, 5, 12, 0.1);
            }
        }

        // Ambient particles
        int arenaY = findArenaY(world);
        Location center = new Location(world, 0, arenaY + 5, 0);
        world.spawnParticle(Particle.DRAGON_BREATH, center, 15, 12, 5, 12, 0.03);
        world.spawnParticle(Particle.REVERSE_PORTAL, center, 10, 8, 3, 8, 0.03);
    }

    private int findArenaY(World world) {
        for (int y = 150; y > 50; y--) {
            if (world.getBlockAt(0, y, 0).getType() == Material.BEDROCK) return y;
        }
        return 110;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBossDamage(EntityDamageByEntityEvent event) {
        if (!active) return;
        boolean isDragon = event.getEntity().equals(dragonBoss);
        boolean isHerald = heraldBoss != null && event.getEntity().equals(heraldBoss);
        if (isDragon || isHerald) {
            event.setDamage(event.getDamage() * 0.80); // 20% DR (less than before, dragon is beefier)
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBossDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        boolean isDragon = entity.getPersistentDataContainer().has(KEY_ABYSS_DRAGON, PersistentDataType.BYTE);
        boolean isHerald = entity.getPersistentDataContainer().has(KEY_ABYSS_HERALD, PersistentDataType.BYTE);
        if (!isDragon && !isHerald) return;

        event.getDrops().clear();
        event.setDroppedExp(0);
        bossesAlive--;

        String bossName = isDragon ? "Abyss Dragon" : "Abyss Herald";
        plugin.getLogger().info("[Abyss] " + bossName + " defeated! Remaining: " + bossesAlive);

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(Component.text("  \u2620 " + bossName + " has fallen!", TextColor.color(170, 0, 0))
                    .decorate(TextDecoration.BOLD));
        }

        // Partial drops per boss
        Location dropLoc = entity.getLocation();
        World world = entity.getWorld();
        if (isDragon) {
            // Dragon drops some loot immediately
            world.dropItemNaturally(dropLoc, new ItemStack(Material.NETHER_STAR, 2));
            world.dropItemNaturally(dropLoc, new ItemStack(Material.EXPERIENCE_BOTTLE, 32));
            // XP
            event.setDroppedExp(8000);
        }
        if (isHerald) {
            world.dropItemNaturally(dropLoc, new ItemStack(Material.NETHERITE_INGOT, 3 + random.nextInt(3)));
            event.setDroppedExp(6000);
        }

        if (bossesAlive <= 0) {
            onAllBossesDefeated(dropLoc, world);
        }
    }

    private void onAllBossesDefeated(Location loc, World world) {
        active = false;
        lastKillTime = System.currentTimeMillis();
        dragonBoss = null;
        heraldBoss = null;
        if (attackTask != null) attackTask.cancel();

        plugin.getLogger().info("[Abyss] ALL BOSSES DEFEATED! Spawning rewards...");

        // ── ABYSSAL Loot ──
        LegendaryWeaponManager wm = plugin.getLegendaryWeaponManager();
        LegendaryWeapon[] abyssalPool = LegendaryWeapon.byTier(LegendaryTier.ABYSSAL);
        int abyssalCount = 2 + random.nextInt(2); // 2-3
        List<LegendaryWeapon> pool = new ArrayList<>(Arrays.asList(abyssalPool));
        List<LegendaryWeapon> dropped = new ArrayList<>();
        for (int i = 0; i < abyssalCount && !pool.isEmpty(); i++) {
            LegendaryWeapon w = pool.remove(random.nextInt(pool.size()));
            ItemStack item = wm.createWeapon(w);
            if (item != null) { world.dropItemNaturally(loc, item); dropped.add(w); }
        }
        // 1-2 MYTHIC weapons
        LegendaryWeapon[] mythicPool = LegendaryWeapon.byTier(LegendaryTier.MYTHIC);
        for (int i = 0; i < 1 + random.nextInt(2) && mythicPool.length > 0; i++) {
            LegendaryWeapon w = mythicPool[random.nextInt(mythicPool.length)];
            ItemStack item = wm.createWeapon(w);
            if (item != null) world.dropItemNaturally(loc, item);
        }

        // Power-ups
        PowerUpManager pum = plugin.getPowerUpManager();
        world.dropItemNaturally(loc, pum.createHeartCrystal());
        world.dropItemNaturally(loc, pum.createHeartCrystal());
        world.dropItemNaturally(loc, pum.createHeartCrystal());
        world.dropItemNaturally(loc, pum.createPhoenixFeather());
        world.dropItemNaturally(loc, pum.createPhoenixFeather());
        world.dropItemNaturally(loc, pum.createKeepInventorer());
        for (int i = 0; i < 10; i++) world.dropItemNaturally(loc, pum.createSoulFragment());
        world.dropItemNaturally(loc, pum.createTitanResolve());
        world.dropItemNaturally(loc, pum.createTitanResolve());
        world.dropItemNaturally(loc, pum.createBerserkerMark());
        world.dropItemNaturally(loc, pum.createBerserkerMark());
        world.dropItemNaturally(loc, pum.createVitalityShard());

        // Materials
        world.dropItemNaturally(loc, new ItemStack(Material.NETHERITE_INGOT, 8 + random.nextInt(8)));
        world.dropItemNaturally(loc, new ItemStack(Material.DIAMOND, 48 + random.nextInt(32)));
        world.dropItemNaturally(loc, new ItemStack(Material.NETHER_STAR, 5));
        world.dropItemNaturally(loc, new ItemStack(Material.EXPERIENCE_BOTTLE, 64));
        world.dropItemNaturally(loc, new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 5 + random.nextInt(5)));
        world.dropItemNaturally(loc, new ItemStack(Material.TOTEM_OF_UNDYING, 3));

        // ── Dragon Egg ──
        world.getBlockAt(loc.getBlockX(), loc.getBlockY() + 1, loc.getBlockZ()).setType(Material.DRAGON_EGG);

        // ── Exit Portal (3x3 END_PORTAL with bedrock frame) ──
        int arenaY = findArenaY(world);
        int portalZ = 8; // offset from center
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                world.getBlockAt(dx, arenaY + 1, portalZ + dz).setType(Material.END_PORTAL);
            }
        }
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (Math.abs(dx) == 2 || Math.abs(dz) == 2) {
                    world.getBlockAt(dx, arenaY + 1, portalZ + dz).setType(Material.BEDROCK);
                }
            }
        }

        // Massive VFX
        world.spawnParticle(Particle.REVERSE_PORTAL, loc, 100, 12, 12, 12, 0.5);
        world.spawnParticle(Particle.DRAGON_BREATH, loc, 60, 10, 10, 10, 0.2);
        world.spawnParticle(Particle.EXPLOSION_EMITTER, loc, 8, 8, 8, 8);
        world.spawnParticle(Particle.TOTEM_OF_UNDYING, loc, 80, 8, 12, 8, 0.5);
        world.playSound(loc, Sound.ENTITY_ENDER_DRAGON_DEATH, 2.0f, 0.3f);
        world.playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 2.0f, 1.0f);

        // Broadcast
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(Component.empty());
            p.sendMessage(Component.text("  \u2726 ", TextColor.color(170, 0, 0))
                .append(Component.text("THE ABYSS HAS BEEN CONQUERED!", TextColor.color(255, 50, 50))
                    .decorate(TextDecoration.BOLD)));
            p.sendMessage(Component.text("  ABYSSAL Drops:", NamedTextColor.GRAY));
            for (LegendaryWeapon w : dropped) {
                p.sendMessage(Component.text("    \u25B6 ", NamedTextColor.DARK_GRAY)
                    .append(Component.text("[ABYSSAL] ", TextColor.color(170, 0, 0)).decorate(TextDecoration.BOLD))
                    .append(Component.text(w.getDisplayName(), TextColor.color(170, 0, 0)).decorate(TextDecoration.BOLD))
                    .append(Component.text(" (DMG " + w.getBaseDamage() + ")", NamedTextColor.GRAY)));
            }
            p.sendMessage(Component.text("  + Dragon Egg, Exit Portal, Power-Ups, Materials", NamedTextColor.GOLD));
            p.sendMessage(Component.empty());
            p.showTitle(net.kyori.adventure.title.Title.title(
                Component.text("\u2726 ABYSS CONQUERED \u2726", TextColor.color(255, 215, 0)).decorate(TextDecoration.BOLD),
                Component.text("The darkness has been vanquished!", NamedTextColor.GOLD),
                net.kyori.adventure.title.Title.Times.times(
                    java.time.Duration.ofMillis(500), java.time.Duration.ofSeconds(5), java.time.Duration.ofMillis(1000))));
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        }
    }

    public boolean isActive() { return active; }
    public LivingEntity getBoss() { return dragonBoss; }
}
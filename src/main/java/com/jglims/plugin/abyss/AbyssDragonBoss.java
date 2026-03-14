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
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Abyss Dragon Boss — Dual boss fight on the bedrock arena.
 * Phase 1: Ender Dragon (immune to all but arrows/melee on perch)
 * Phase 2 (at 50%): Abyss Herald (Wither) joins the fight
 * Both must die. On final death: End portal + Dragon Egg + ABYSSAL loot.
 * Boss spawns ONLY when a player steps onto the arena floor.
 */
public class AbyssDragonBoss implements Listener {

    private final JGlimsPlugin plugin;
    private final AbyssDimensionManager dimensionManager;
    private final NamespacedKey KEY_ABYSS_DRAGON;
    private final NamespacedKey KEY_ABYSS_HERALD;
    private final Random random = new Random();

    private EnderDragon dragonBoss;
    private LivingEntity heraldBoss;
    private boolean active = false;
    private boolean heraldSpawned = false;
    private int tickCounter = 0;
    private long lastKillTime = 0;
    private int bossesAlive = 0;
    private static final long RESPAWN_COOLDOWN_MS = 30 * 60 * 1000L;

    public AbyssDragonBoss(JGlimsPlugin plugin, AbyssDimensionManager dimensionManager) {
        this.plugin = plugin;
        this.dimensionManager = dimensionManager;
        this.KEY_ABYSS_DRAGON = new NamespacedKey(plugin, "abyss_dragon");
        this.KEY_ABYSS_HERALD = new NamespacedKey(plugin, "abyss_herald");
    }

    /** Detect player stepping on the arena to trigger the boss. */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (active) return;
        Player player = event.getPlayer();
        World abyssWorld = dimensionManager.getAbyssWorld();
        if (abyssWorld == null || !player.getWorld().equals(abyssWorld)) return;
        if (System.currentTimeMillis() - lastKillTime < RESPAWN_COOLDOWN_MS) return;

        Location loc = player.getLocation();
        // Arena is at baseY + 50 (keep height). Check if player is on the arena level.
        int arenaY = abyssWorld.getHighestBlockYAt(0, 0); // top of the citadel center
        // Player must be within 30 blocks of center and near arena Y
        double dist = Math.sqrt(loc.getX() * loc.getX() + loc.getZ() * loc.getZ());
        if (dist < 28 && Math.abs(loc.getY() - arenaY) < 5) {
            // Check block below is bedrock (arena floor)
            if (player.getLocation().subtract(0, 1, 0).getBlock().getType() == Material.BEDROCK) {
                startBossFight(abyssWorld);
            }
        }
    }

    private void startBossFight(World abyssWorld) {
        active = true;
        heraldSpawned = false;
        tickCounter = 0;
        bossesAlive = 1;

        int arenaY = abyssWorld.getHighestBlockYAt(0, 0) + 5;
        if (arenaY < 90) arenaY = 110;

        plugin.getLogger().info("[Abyss] BOSS FIGHT STARTING! Spawning Abyss Dragon at Y=" + arenaY);

        // Remove any existing vanilla ender dragons
        for (EnderDragon d : abyssWorld.getEntitiesByClass(EnderDragon.class)) d.remove();
        for (Wither w : abyssWorld.getEntitiesByClass(Wither.class)) w.remove();

        Location spawnLoc = new Location(abyssWorld, 0.5, arenaY, 0.5);

        // Announce
        for (Player p : abyssWorld.getPlayers()) {
            p.showTitle(net.kyori.adventure.title.Title.title(
                Component.text("\u2620 ABYSS DRAGON \u2620", TextColor.color(170, 0, 0)).decorate(TextDecoration.BOLD),
                Component.text("The guardian of the Abyss awakens!", NamedTextColor.DARK_RED),
                net.kyori.adventure.title.Title.Times.times(java.time.Duration.ofMillis(500), java.time.Duration.ofSeconds(4), java.time.Duration.ofMillis(1000))));
            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0f, 0.2f);
            p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.5f);
        }

        // Spawn the Ender Dragon
        dragonBoss = abyssWorld.spawn(spawnLoc, EnderDragon.class, dragon -> {
            dragon.customName(Component.text("\u2620 Abyss Dragon \u2620", TextColor.color(170, 0, 0)).decorate(TextDecoration.BOLD));
            dragon.setCustomNameVisible(true);
            dragon.getPersistentDataContainer().set(KEY_ABYSS_DRAGON, PersistentDataType.BYTE, (byte) 1);
        });

        // Buff the dragon
        if (dragonBoss.getAttribute(Attribute.MAX_HEALTH) != null) {
            dragonBoss.getAttribute(Attribute.MAX_HEALTH).setBaseValue(600);
            dragonBoss.setHealth(600);
        }

        abyssWorld.spawnParticle(Particle.DRAGON_BREATH, spawnLoc, 50, 8, 8, 8, 0.3);
        abyssWorld.spawnParticle(Particle.REVERSE_PORTAL, spawnLoc, 50, 8, 8, 8, 0.5);

        // Attack loop
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!active) { cancel(); return; }
                tickCounter += 20;
                doCustomAttacks();
                checkPhase2();
            }
        }.runTaskTimer(plugin, 60L, 20L);
    }

    private void checkPhase2() {
        if (heraldSpawned) return;
        if (dragonBoss == null || dragonBoss.isDead()) return;
        if (dragonBoss.getHealth() <= 300) { // 50% HP
            heraldSpawned = true;
            bossesAlive = 2;
            World abyssWorld = dimensionManager.getAbyssWorld();
            if (abyssWorld == null) return;

            int arenaY = abyssWorld.getHighestBlockYAt(0, 0) + 3;
            Location heraldLoc = new Location(abyssWorld, 10.5, arenaY, 10.5);

            for (Player p : abyssWorld.getPlayers()) {
                p.showTitle(net.kyori.adventure.title.Title.title(
                    Component.text("\u2620 ABYSS HERALD \u2620", TextColor.color(100, 0, 170)).decorate(TextDecoration.BOLD),
                    Component.text("The Herald joins the battle!", NamedTextColor.DARK_PURPLE),
                    net.kyori.adventure.title.Title.Times.times(java.time.Duration.ofMillis(300), java.time.Duration.ofSeconds(3), java.time.Duration.ofMillis(500))));
                p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 2.0f, 0.3f);
            }

            heraldBoss = abyssWorld.spawn(heraldLoc, Wither.class, w -> {
                w.customName(Component.text("\u2620 Abyss Herald \u2620", TextColor.color(100, 0, 170)).decorate(TextDecoration.BOLD));
                w.setCustomNameVisible(true);
                w.getPersistentDataContainer().set(KEY_ABYSS_HERALD, PersistentDataType.BYTE, (byte) 1);
            });

            if (heraldBoss.getAttribute(Attribute.MAX_HEALTH) != null) {
                heraldBoss.getAttribute(Attribute.MAX_HEALTH).setBaseValue(800);
                heraldBoss.setHealth(800);
            }
            heraldBoss.setGlowing(true);
            plugin.getLogger().info("[Abyss] Abyss Herald spawned (Phase 2)!");
        }
    }

    private void doCustomAttacks() {
        World world = dimensionManager.getAbyssWorld();
        if (world == null) return;
        List<Player> nearby = new ArrayList<>();
        for (Player p : world.getPlayers()) {
            double dist = Math.sqrt(p.getLocation().getX() * p.getLocation().getX() + p.getLocation().getZ() * p.getLocation().getZ());
            if (dist < 60) nearby.add(p);
        }
        if (nearby.isEmpty()) return;

        // Lightning barrage every 5s
        if (tickCounter % 100 == 0) {
            for (int i = 0; i < 4; i++) {
                Player t = nearby.get(random.nextInt(nearby.size()));
                world.strikeLightning(t.getLocation().add(random.nextGaussian() * 4, 0, random.nextGaussian() * 4));
            }
        }
        // Summon Enderman minions every 15s
        if (tickCounter % 300 == 0) {
            for (int i = 0; i < 3; i++) {
                Location sLoc = new Location(world, random.nextGaussian() * 15, world.getHighestBlockYAt(0, 0) + 1, random.nextGaussian() * 15);
                Enderman m = world.spawn(sLoc, Enderman.class);
                if (m.getAttribute(Attribute.MAX_HEALTH) != null) { m.getAttribute(Attribute.MAX_HEALTH).setBaseValue(80); m.setHealth(80); }
                m.customName(Component.text("Abyss Spawn", TextColor.color(100, 0, 0)));
                m.setCustomNameVisible(true);
                if (!nearby.isEmpty()) m.setTarget(nearby.get(random.nextInt(nearby.size())));
            }
        }
        // Wither Skeleton wave every 20s
        if (tickCounter % 400 == 0) {
            for (int i = 0; i < 4; i++) {
                Location sLoc = new Location(world, random.nextGaussian() * 12, world.getHighestBlockYAt(0, 0) + 1, random.nextGaussian() * 12);
                WitherSkeleton ws = world.spawn(sLoc, WitherSkeleton.class);
                ws.customName(Component.text("Abyssal Guard", TextColor.color(80, 0, 80)));
                ws.setCustomNameVisible(true);
            }
        }
        // Ambient particles
        Location center = new Location(world, 0, world.getHighestBlockYAt(0, 0) + 5, 0);
        world.spawnParticle(Particle.DRAGON_BREATH, center, 20, 10, 5, 10, 0.05);
        world.spawnParticle(Particle.REVERSE_PORTAL, center, 15, 8, 3, 8, 0.05);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBossDamage(EntityDamageByEntityEvent event) {
        if (!active) return;
        if (event.getEntity().equals(dragonBoss) || (heraldBoss != null && event.getEntity().equals(heraldBoss))) {
            event.setDamage(event.getDamage() * 0.75); // 25% DR
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBossDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        boolean isDragon = entity.getPersistentDataContainer().has(KEY_ABYSS_DRAGON, PersistentDataType.BYTE);
        boolean isHerald = entity.getPersistentDataContainer().has(KEY_ABYSS_HERALD, PersistentDataType.BYTE);
        if (!isDragon && !isHerald) return;

        event.getDrops().clear();

        bossesAlive--;
        String bossName = isDragon ? "Abyss Dragon" : "Abyss Herald";
        plugin.getLogger().info("[Abyss] " + bossName + " defeated! Bosses remaining: " + bossesAlive);

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(Component.text("  \u2620 " + bossName + " has fallen!", TextColor.color(170, 0, 0)).decorate(TextDecoration.BOLD));
        }

        if (bossesAlive <= 0) {
            onAllBossesDefeated(entity.getLocation(), entity.getWorld());
        }
    }

    private void onAllBossesDefeated(Location loc, World world) {
        active = false;
        lastKillTime = System.currentTimeMillis();
        dragonBoss = null;
        heraldBoss = null;

        plugin.getLogger().info("[Abyss] ALL BOSSES DEFEATED! Spawning rewards...");

        // ── ABYSSAL Loot (guaranteed) ──
        LegendaryWeaponManager wm = plugin.getLegendaryWeaponManager();
        LegendaryWeapon[] abyssalPool = LegendaryWeapon.byTier(LegendaryTier.ABYSSAL);
        int count = 2 + random.nextInt(2); // 2-3 ABYSSAL weapons
        List<LegendaryWeapon> pool = new ArrayList<>(Arrays.asList(abyssalPool));
        List<LegendaryWeapon> dropped = new ArrayList<>();
        for (int i = 0; i < count && !pool.isEmpty(); i++) {
            LegendaryWeapon w = pool.remove(random.nextInt(pool.size()));
            ItemStack item = wm.createWeapon(w);
            if (item != null) { world.dropItemNaturally(loc, item); dropped.add(w); }
        }
        // Also drop 1-2 MYTHIC
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
        world.dropItemNaturally(loc, pum.createPhoenixFeather());
        world.dropItemNaturally(loc, pum.createKeepInventorer());
        for (int i = 0; i < 8; i++) world.dropItemNaturally(loc, pum.createSoulFragment());
        world.dropItemNaturally(loc, pum.createTitanResolve());
        world.dropItemNaturally(loc, pum.createBerserkerMark());

        // Materials
        world.dropItemNaturally(loc, new ItemStack(Material.NETHERITE_INGOT, 5 + random.nextInt(5)));
        world.dropItemNaturally(loc, new ItemStack(Material.DIAMOND, 32 + random.nextInt(32)));
        world.dropItemNaturally(loc, new ItemStack(Material.NETHER_STAR, 3));
        world.dropItemNaturally(loc, new ItemStack(Material.EXPERIENCE_BOTTLE, 64));

        // ── Dragon Egg ──
        world.getBlockAt(loc.getBlockX(), loc.getBlockY() + 1, loc.getBlockZ()).setType(Material.DRAGON_EGG);
        plugin.getLogger().info("[Abyss] Dragon Egg placed!");

        // ── End Portal (exit) ──
        int portalY = world.getHighestBlockYAt(loc.getBlockX(), loc.getBlockZ()) + 1;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                world.getBlockAt(loc.getBlockX() + dx, portalY, loc.getBlockZ() + dz + 5).setType(Material.END_PORTAL);
            }
        }
        // Bedrock frame around portal
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (Math.abs(dx) == 2 || Math.abs(dz) == 2) {
                    world.getBlockAt(loc.getBlockX() + dx, portalY, loc.getBlockZ() + dz + 5).setType(Material.BEDROCK);
                }
            }
        }
        plugin.getLogger().info("[Abyss] Exit portal created!");

        // Massive VFX
        world.spawnParticle(Particle.REVERSE_PORTAL, loc, 60, 10, 10, 10, 0.5);
        world.spawnParticle(Particle.DRAGON_BREATH, loc, 40, 8, 8, 8, 0.2);
        world.spawnParticle(Particle.EXPLOSION_EMITTER, loc, 5, 5, 5, 5);
        world.spawnParticle(Particle.TOTEM_OF_UNDYING, loc, 50, 5, 8, 5, 0.5);
        world.playSound(loc, Sound.ENTITY_ENDER_DRAGON_DEATH, 2.0f, 0.3f);
        world.playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 2.0f, 1.0f);

        // Broadcast
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(Component.empty());
            p.sendMessage(Component.text("  \u2726 ", TextColor.color(170, 0, 0))
                .append(Component.text("THE ABYSS HAS BEEN CONQUERED!", TextColor.color(255, 50, 50)).decorate(TextDecoration.BOLD)));
            p.sendMessage(Component.text("  ABYSSAL Drops:", NamedTextColor.GRAY));
            for (LegendaryWeapon w : dropped) {
                p.sendMessage(Component.text("    \u25B6 ", NamedTextColor.DARK_GRAY)
                    .append(Component.text("[ABYSSAL] ", TextColor.color(170, 0, 0)).decorate(TextDecoration.BOLD))
                    .append(Component.text(w.getDisplayName(), TextColor.color(170, 0, 0)).decorate(TextDecoration.BOLD))
                    .append(Component.text(" (DMG " + w.getBaseDamage() + ")", NamedTextColor.GRAY)));
            }
            p.sendMessage(Component.text("  + Dragon Egg, Exit Portal, Power-Ups, Materials", NamedTextColor.GOLD));
            p.sendMessage(Component.empty());
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        }
    }

    public boolean isActive() { return active; }
    public LivingEntity getBoss() { return dragonBoss; }
}
package com.jglims.plugin.events;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.legendary.LegendaryTier;
import com.jglims.plugin.legendary.LegendaryWeapon;
import com.jglims.plugin.legendary.LegendaryWeaponManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * End Rift Overworld Event (Phase 20, README Section G.2).
 *
 * Trigger: 10% chance when Ender Dragon dies.
 * A massive purple portal ring (15x15) opens at a random location within
 * 500 blocks of world spawn (overworld). Waves of Endermen, Shulkers,
 * and Phantoms spawn for 10 minutes, culminating in the End Rift Dragon
 * (600 HP, 2x damage, void breath = Wither + Blindness, lightning strikes,
 * can summon Shulker turrets). Drops 1-2 MYTHIC weapons from End Rift pool.
 * Rift closes after boss death or 15-minute timeout.
 */
public class EndRiftEvent implements Listener {

    private final JGlimsPlugin plugin;
    private final EventManager eventManager;
    private final LegendaryWeaponManager weaponManager;
    private final Random random = new Random();

    private boolean active = false;
    private World riftWorld;
    private Location riftCenter;
    private LivingEntity boss;
    private int ticksElapsed;
    private boolean bossSpawned = false;
    private final List<Entity> spawnedEntities = new ArrayList<>();

    // 15 minutes max duration
    private static final int MAX_DURATION_TICKS = 18000;
    // Boss spawns at 10 minutes
    private static final int BOSS_SPAWN_TICK = 12000;
    // Wave intervals
    private static final int WAVE_INTERVAL_TICKS = 1200; // every 60 seconds

    // End Rift weapon pool
    private static final LegendaryWeapon[] END_RIFT_POOL = {
            LegendaryWeapon.TENGENS_BLADE,
            LegendaryWeapon.SOUL_DEVOURER,
            LegendaryWeapon.STAR_EDGE,
            LegendaryWeapon.CREATION_SPLITTER,
            LegendaryWeapon.STOP_SIGN
    };

    private final NamespacedKey KEY_RIFT_MOB;
    private final NamespacedKey KEY_RIFT_BOSS;

    public EndRiftEvent(JGlimsPlugin plugin, EventManager eventManager) {
        this.plugin = plugin;
        this.eventManager = eventManager;
        this.weaponManager = plugin.getLegendaryWeaponManager();
        this.KEY_RIFT_MOB = new NamespacedKey(plugin, "end_rift_mob");
        this.KEY_RIFT_BOSS = new NamespacedKey(plugin, "end_rift_boss");
    }

    /**
     * Attempt to trigger the End Rift after an Ender Dragon death.
     * Called from EventManager when dragon dies. Returns true if rift opened.
     */
    public boolean tryTrigger() {
        if (active) return false;
        if (random.nextDouble() >= 0.10) return false; // 10% chance

        // Find the main overworld
        World overworld = plugin.getServer().getWorlds().stream()
                .filter(w -> w.getEnvironment() == World.Environment.NORMAL)
                .findFirst()
                .orElse(null);
        if (overworld == null) return false;
        if (overworld.getPlayers().isEmpty()) return false;

        start(overworld);
        return true;
    }

    public void start(World world) {
        if (active) return;
        active = true;
        bossSpawned = false;
        riftWorld = world;
        ticksElapsed = 0;
        spawnedEntities.clear();

        // Pick random location within 500 blocks of spawn
        Location spawn = world.getSpawnLocation();
        int ox = spawn.getBlockX() + random.nextInt(1001) - 500;
        int oz = spawn.getBlockZ() + random.nextInt(1001) - 500;
        int oy = world.getHighestBlockYAt(ox, oz) + 1;
        riftCenter = new Location(world, ox + 0.5, oy, oz + 0.5);

        plugin.getLogger().info("End Rift opening at " + ox + ", " + oy + ", " + oz + " in " + world.getName());

        // Build the portal ring structure
        buildPortalRing();

        // Broadcast to ALL online players (cross-dimension event announcement)
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.showTitle(net.kyori.adventure.title.Title.title(
                    Component.text("\u2726 END RIFT OPENED \u2726", TextColor.color(150, 0, 255))
                            .decorate(TextDecoration.BOLD),
                    Component.text("A rift to the End has torn open in the Overworld!", NamedTextColor.GRAY),
                    net.kyori.adventure.title.Title.Times.times(
                            java.time.Duration.ofMillis(500),
                            java.time.Duration.ofSeconds(4),
                            java.time.Duration.ofMillis(1000))));
            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.5f);
            p.playSound(p.getLocation(), Sound.BLOCK_END_PORTAL_SPAWN, 0.8f, 0.7f);
            p.sendMessage(Component.empty());
            p.sendMessage(Component.text("  \u2726 ", NamedTextColor.DARK_PURPLE)
                    .append(Component.text("AN END RIFT HAS OPENED!", TextColor.color(150, 0, 255)).decorate(TextDecoration.BOLD)));
            p.sendMessage(Component.text("  Location: ", NamedTextColor.GRAY)
                    .append(Component.text(ox + ", " + oy + ", " + oz, NamedTextColor.LIGHT_PURPLE)));
            p.sendMessage(Component.text("  Defeat the End Rift Dragon to close it and claim MYTHIC rewards!", NamedTextColor.GOLD));
            p.sendMessage(Component.empty());
        }

        // Start the main event loop
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!active) { cancel(); return; }

                // Check if overworld has players nearby (within 200 blocks)
                boolean playersNearby = false;
                for (Player p : riftWorld.getPlayers()) {
                    if (p.getLocation().distanceSquared(riftCenter) < 40000) { // 200 blocks
                        playersNearby = true;
                        break;
                    }
                }

                ticksElapsed += 20;

                // Ambient effects always run if rift is active
                doAmbientEffects();

                // Waves only if players nearby
                if (playersNearby) {
                    if (ticksElapsed % WAVE_INTERVAL_TICKS == 0 && ticksElapsed < BOSS_SPAWN_TICK) {
                        int waveNum = ticksElapsed / WAVE_INTERVAL_TICKS;
                        spawnWave(waveNum);
                    }

                    // Spawn boss at 10 minutes
                    if (ticksElapsed >= BOSS_SPAWN_TICK && !bossSpawned) {
                        spawnBoss();
                    }

                    // Boss special attacks
                    if (bossSpawned && boss != null && !boss.isDead()) {
                        doBossAttacks();
                    }
                }

                // Timeout at 15 minutes
                if (ticksElapsed >= MAX_DURATION_TICKS) {
                    endEvent(true);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    /**
     * Build a 15x15 portal ring out of end stone bricks and crying obsidian
     * with end portal frame blocks on the corners.
     */
    private void buildPortalRing() {
        int cx = riftCenter.getBlockX();
        int cy = riftCenter.getBlockY();
        int cz = riftCenter.getBlockZ();
        int radius = 7;
        int height = 10;

        // Clear the area first
        for (int x = -radius - 1; x <= radius + 1; x++) {
            for (int z = -radius - 1; z <= radius + 1; z++) {
                for (int y = 0; y < height + 2; y++) {
                    Location loc = new Location(riftWorld, cx + x, cy + y, cz + z);
                    if (loc.getBlock().getType() != Material.BEDROCK) {
                        loc.getBlock().setType(Material.AIR);
                    }
                }
            }
        }

        // Build ring frame
        for (int angle = 0; angle < 360; angle += 5) {
            double rad = Math.toRadians(angle);
            int bx = (int) Math.round(cx + radius * Math.cos(rad));
            int bz = (int) Math.round(cz + radius * Math.sin(rad));

            // Pillars at cardinal directions
            boolean isCardinal = (angle % 90 <= 5 || angle % 90 >= 85);

            for (int y = 0; y < height; y++) {
                Location loc = new Location(riftWorld, bx, cy + y, bz);
                if (isCardinal) {
                    loc.getBlock().setType(Material.CRYING_OBSIDIAN);
                } else if (y == 0 || y == height - 1) {
                    loc.getBlock().setType(Material.END_STONE_BRICKS);
                } else if (angle % 30 < 5) {
                    loc.getBlock().setType(Material.PURPUR_BLOCK);
                }
            }

            // Top frame
            Location top = new Location(riftWorld, bx, cy + height, bz);
            top.getBlock().setType(Material.END_STONE_BRICKS);
        }

        // Place end portal frames at the four corners
        int[][] corners = {{cx + radius, cz}, {cx - radius, cz}, {cx, cz + radius}, {cx, cz - radius}};
        for (int[] corner : corners) {
            Location c = new Location(riftWorld, corner[0], cy, corner[1]);
            c.getBlock().setType(Material.END_PORTAL_FRAME);
            new Location(riftWorld, corner[0], cy + height, corner[1]).getBlock().setType(Material.END_PORTAL_FRAME);
        }

        // Ender crystal decorations on corners
        for (int[] corner : corners) {
            Location crystalLoc = new Location(riftWorld, corner[0] + 0.5, cy + height + 1, corner[1] + 0.5);
            riftWorld.spawn(crystalLoc, EnderCrystal.class, ec -> {
                ec.setShowingBottom(false);
                ec.setInvulnerable(true);
                ec.setBeamTarget(riftCenter.clone().add(0, 5, 0));
            });
        }

        // Platform floor
        for (int x = -radius + 1; x < radius; x++) {
            for (int z = -radius + 1; z < radius; z++) {
                if (x * x + z * z < (radius - 1) * (radius - 1)) {
                    Location floor = new Location(riftWorld, cx + x, cy - 1, cz + z);
                    floor.getBlock().setType(Material.END_STONE_BRICKS);
                }
            }
        }
    }

    private void doAmbientEffects() {
        if (riftCenter == null || riftWorld == null) return;

        // Purple portal particles in the center column
        riftWorld.spawnParticle(Particle.REVERSE_PORTAL, riftCenter.clone().add(0, 5, 0),
                50, 3, 4, 3, 0.05);
        riftWorld.spawnParticle(Particle.PORTAL, riftCenter.clone().add(0, 5, 0),
                80, 5, 5, 5, 0.5);
        riftWorld.spawnParticle(Particle.END_ROD, riftCenter.clone().add(0, 8, 0),
                15, 6, 1, 6, 0.02);

        // Ring glow
        int radius = 7;
        for (int angle = 0; angle < 360; angle += 15) {
            double rad = Math.toRadians(angle + ticksElapsed * 0.5);
            double x = riftCenter.getX() + radius * Math.cos(rad);
            double z = riftCenter.getZ() + radius * Math.sin(rad);
            riftWorld.spawnParticle(Particle.WITCH, new Location(riftWorld, x, riftCenter.getY() + 5, z),
                    3, 0.2, 2, 0.2, 0);
        }

        // Ominous sound every 5 seconds
        if (ticksElapsed % 100 == 0) {
            for (Player p : riftWorld.getPlayers()) {
                if (p.getLocation().distanceSquared(riftCenter) < 90000) { // 300 blocks
                    p.playSound(riftCenter, Sound.BLOCK_PORTAL_AMBIENT, 0.5f, 0.3f);
                }
            }
        }

        // Dragon growl every 30 seconds
        if (ticksElapsed % 600 == 0 && ticksElapsed > 0) {
            for (Player p : riftWorld.getPlayers()) {
                if (p.getLocation().distanceSquared(riftCenter) < 160000) { // 400 blocks
                    p.playSound(riftCenter, Sound.ENTITY_ENDER_DRAGON_GROWL, 0.4f, 0.4f);
                }
            }
        }
    }

    private void spawnWave(int waveNum) {
        plugin.getLogger().info("End Rift Wave " + waveNum + " spawning...");
        Location center = riftCenter.clone().add(0, 2, 0);

        // Announce wave
        for (Player p : riftWorld.getPlayers()) {
            if (p.getLocation().distanceSquared(riftCenter) < 40000) {
                p.sendMessage(Component.text("  \u26A0 ", NamedTextColor.DARK_PURPLE)
                        .append(Component.text("Wave " + waveNum + " emerging from the rift!", TextColor.color(150, 0, 255))));
                p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_STARE, 0.7f, 0.5f);
            }
        }

        // Scale with wave number
        int endermen = 4 + waveNum * 2;
        int shulkers = Math.min(waveNum, 4);
        int phantoms = waveNum >= 3 ? waveNum * 2 : 0;

        // Spawn Endermen
        for (int i = 0; i < endermen; i++) {
            Location spawnLoc = getRandomSpawnLoc(center, 12);
            Enderman e = riftWorld.spawn(spawnLoc, Enderman.class);
            configureRiftMob(e, 60 + waveNum * 10);
            spawnedEntities.add(e);
            // Force aggro on nearest player
            Player nearest = getNearestPlayer(spawnLoc, 50);
            if (nearest != null) e.setTarget(nearest);
        }

        // Spawn Shulkers
        for (int i = 0; i < shulkers; i++) {
            Location spawnLoc = getRandomSpawnLoc(center, 8);
            Shulker s = riftWorld.spawn(spawnLoc, Shulker.class);
            configureRiftMob(s, 40 + waveNum * 5);
            s.setColor(DyeColor.PURPLE);
            spawnedEntities.add(s);
        }

        // Spawn Phantoms (later waves)
        for (int i = 0; i < phantoms; i++) {
            Location spawnLoc = center.clone().add(
                    random.nextGaussian() * 15, 8 + random.nextInt(5), random.nextGaussian() * 15);
            Phantom ph = riftWorld.spawn(spawnLoc, Phantom.class);
            configureRiftMob(ph, 30 + waveNum * 5);
            ph.setSize(2 + waveNum);
            spawnedEntities.add(ph);
        }

        // Spawn particles at wave start
        riftWorld.spawnParticle(Particle.REVERSE_PORTAL, center, 30, 5, 5, 5, 0.3);
        riftWorld.spawnParticle(Particle.EXPLOSION, center, 3, 3, 3, 3);
    }

    private void configureRiftMob(LivingEntity entity, double hp) {
        entity.getPersistentDataContainer().set(KEY_RIFT_MOB, PersistentDataType.BYTE, (byte) 1);
        if (entity.getAttribute(Attribute.MAX_HEALTH) != null) {
            entity.getAttribute(Attribute.MAX_HEALTH).setBaseValue(hp);
            entity.setHealth(hp);
        }
        entity.setGlowing(true);
        entity.setRemoveWhenFarAway(false);
        entity.setPersistent(true);
    }

    private void spawnBoss() {
        if (bossSpawned) return;
        bossSpawned = true;

        Location spawnLoc = riftCenter.clone().add(0, 6, 0);

        // Dramatic pre-spawn
        for (Player p : riftWorld.getPlayers()) {
            if (p.getLocation().distanceSquared(riftCenter) < 160000) {
                p.showTitle(net.kyori.adventure.title.Title.title(
                        Component.text("\u2620 END RIFT DRAGON \u2620", TextColor.color(120, 0, 200))
                                .decorate(TextDecoration.BOLD),
                        Component.text("The master of the rift emerges!", NamedTextColor.GRAY),
                        net.kyori.adventure.title.Title.Times.times(
                                java.time.Duration.ofMillis(500),
                                java.time.Duration.ofSeconds(3),
                                java.time.Duration.ofMillis(1000))));
                p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.5f, 0.3f);
                p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.5f);
            }
        }

        // Spawn as a Wither (we cannot spawn a second EnderDragon cleanly in the overworld,
        // so we use a Wither reskinned/renamed as the End Rift Dragon â€” same boss bar behavior)
        boss = riftWorld.spawn(spawnLoc, Wither.class, w -> {
            w.customName(Component.text("\u2620 End Rift Dragon \u2620", TextColor.color(120, 0, 200))
                    .decorate(TextDecoration.BOLD));
            w.setCustomNameVisible(true);
        });

        // Configure boss stats
        eventManager.tagEventBoss(boss, "END_RIFT");
        boss.getPersistentDataContainer().set(KEY_RIFT_BOSS, PersistentDataType.BYTE, (byte) 1);

        if (boss.getAttribute(Attribute.MAX_HEALTH) != null) {
            boss.getAttribute(Attribute.MAX_HEALTH).setBaseValue(600);
            boss.setHealth(600);
        }
        if (boss.getAttribute(Attribute.ATTACK_DAMAGE) != null) {
            boss.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(16);
        }
        boss.setGlowing(true);
        boss.setRemoveWhenFarAway(false);

        // Massive spawn VFX
        riftWorld.spawnParticle(Particle.REVERSE_PORTAL, spawnLoc, 30, 5, 5, 5, 0.5);
        riftWorld.spawnParticle(Particle.SOUL_FIRE_FLAME, spawnLoc, 25, 3, 3, 3, 0.1);
        riftWorld.spawnParticle(Particle.EXPLOSION_EMITTER, spawnLoc, 2, 2, 2, 2);
        riftWorld.playSound(spawnLoc, Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0f, 0.4f);

        // Broadcast boss spawn
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(Component.empty());
            p.sendMessage(Component.text("  \u2620 ", TextColor.color(120, 0, 200))
                    .append(Component.text("THE END RIFT DRAGON HAS EMERGED!", TextColor.color(150, 0, 255))
                            .decorate(TextDecoration.BOLD)));
            p.sendMessage(Component.text("  Defeat it to close the rift and claim MYTHIC weapons!", NamedTextColor.GOLD));
            p.sendMessage(Component.empty());
        }
    }

    /**
     * Boss special attacks: void breath, lightning, shulker turrets.
     */
    private void doBossAttacks() {
        if (boss == null || boss.isDead()) return;
        Location bossLoc = boss.getLocation();

        // Void Breath: every 4 seconds, Wither + Blindness to nearest player within 20 blocks
        if (ticksElapsed % 80 == 0) {
            Player target = getNearestPlayer(bossLoc, 20);
            if (target != null) {
                target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 80, 1, true, false));
                target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0, true, false));
                target.damage(8.0, boss);

                // Beam visual from boss to target
                Vector dir = target.getLocation().subtract(bossLoc).toVector().normalize();
                double dist = bossLoc.distance(target.getLocation());
                for (double t = 0; t < dist; t += 0.5) {
                    Location point = bossLoc.clone().add(dir.clone().multiply(t));
                    riftWorld.spawnParticle(Particle.SOUL_FIRE_FLAME, point, 3, 0.1, 0.1, 0.1, 0);
                }
                riftWorld.playSound(bossLoc, Sound.ENTITY_ENDER_DRAGON_HURT, 0.8f, 0.3f);
            }
        }

        // Lightning strikes: every 6 seconds, strike random player within 30 blocks
        if (ticksElapsed % 120 == 0) {
            List<Player> nearby = new ArrayList<>();
            for (Player p : riftWorld.getPlayers()) {
                if (p.getLocation().distanceSquared(bossLoc) < 900) nearby.add(p);
            }
            if (!nearby.isEmpty()) {
                Player target = nearby.get(random.nextInt(nearby.size()));
                riftWorld.strikeLightning(target.getLocation());
                riftWorld.spawnParticle(Particle.REVERSE_PORTAL, target.getLocation(), 30, 1, 1, 1, 0.1);
            }
        }

        // Summon Shulker turrets: every 20 seconds, spawn 2 shulkers
        if (ticksElapsed % 400 == 0) {
            for (int i = 0; i < 2; i++) {
                Location sLoc = bossLoc.clone().add(random.nextGaussian() * 5, 0, random.nextGaussian() * 5);
                sLoc.setY(riftWorld.getHighestBlockYAt(sLoc.getBlockX(), sLoc.getBlockZ()) + 1);
                Shulker turret = riftWorld.spawn(sLoc, Shulker.class);
                configureRiftMob(turret, 50);
                turret.setColor(DyeColor.MAGENTA);
                turret.customName(Component.text("Rift Turret", NamedTextColor.DARK_PURPLE));
                turret.setCustomNameVisible(true);
                spawnedEntities.add(turret);
            }
            // Announce
            for (Player p : riftWorld.getPlayers()) {
                if (p.getLocation().distanceSquared(bossLoc) < 2500) {
                    p.sendMessage(Component.text("  \u25B6 ", NamedTextColor.DARK_PURPLE)
                            .append(Component.text("The Dragon summons Shulker Turrets!", NamedTextColor.LIGHT_PURPLE)));
                }
            }
        }

        // Boss ambient particles
        riftWorld.spawnParticle(Particle.SOUL_FIRE_FLAME, bossLoc, 15, 2, 2, 2, 0.02);
        riftWorld.spawnParticle(Particle.REVERSE_PORTAL, bossLoc, 10, 1.5, 1.5, 1.5, 0.05);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!active) return;
        LivingEntity entity = event.getEntity();

        // Check if it's the End Rift Dragon boss
        if (entity.getPersistentDataContainer().has(KEY_RIFT_BOSS, PersistentDataType.BYTE)) {
            handleBossDeath(event);
            return;
        }

        // Rift mob death â€” small XP bonus
        if (entity.getPersistentDataContainer().has(KEY_RIFT_MOB, PersistentDataType.BYTE)) {
            event.setDroppedExp(event.getDroppedExp() * 2);
            // Small chance to drop ender pearls
            if (random.nextDouble() < 0.25) {
                event.getDrops().add(new ItemStack(Material.ENDER_PEARL, 1 + random.nextInt(2)));
            }
        }
    }

    private void handleBossDeath(EntityDeathEvent event) {
        Location loc = event.getEntity().getLocation();
        plugin.getLogger().info("End Rift Dragon defeated!");

        // Clear default drops
        event.getDrops().clear();
        event.setDroppedExp(5000);

        // Drop 1-2 MYTHIC weapons from End Rift pool
        int weaponCount = 1 + random.nextInt(2);
        List<LegendaryWeapon> pool = new ArrayList<>(Arrays.asList(END_RIFT_POOL));
        List<LegendaryWeapon> dropped = new ArrayList<>();
        for (int i = 0; i < weaponCount && !pool.isEmpty(); i++) {
            LegendaryWeapon weapon = pool.remove(random.nextInt(pool.size()));
            ItemStack item = weaponManager.createWeapon(weapon);
            if (item != null) {
                riftWorld.dropItemNaturally(loc, item);
                dropped.add(weapon);
            }
        }

        // Drop misc loot
        riftWorld.dropItemNaturally(loc, new ItemStack(Material.DIAMOND, 15 + random.nextInt(16)));
        riftWorld.dropItemNaturally(loc, new ItemStack(Material.ENDER_PEARL, 10 + random.nextInt(10)));
        riftWorld.dropItemNaturally(loc, new ItemStack(Material.EXPERIENCE_BOTTLE, 16 + random.nextInt(16)));
        if (random.nextDouble() < 0.3) {
            riftWorld.dropItemNaturally(loc, new ItemStack(Material.NETHERITE_INGOT, 1));
        }
        if (random.nextDouble() < 0.15) {
            riftWorld.dropItemNaturally(loc, new ItemStack(Material.NETHER_STAR, 1));
        }

        // Massive VFX
        riftWorld.spawnParticle(Particle.REVERSE_PORTAL, loc, 30, 5, 5, 5, 0.5);
        riftWorld.spawnParticle(Particle.SOUL_FIRE_FLAME, loc, 25, 3, 3, 3, 0.2);
        riftWorld.spawnParticle(Particle.EXPLOSION_EMITTER, loc, 2, 3, 3, 3);
        riftWorld.spawnParticle(Particle.TOTEM_OF_UNDYING, loc, 30, 2, 4, 2, 0.5);
        riftWorld.playSound(loc, Sound.ENTITY_ENDER_DRAGON_DEATH, 2.0f, 0.5f);
        riftWorld.playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 2.0f, 1.0f);

        // Broadcast to all players
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(Component.empty());
            p.sendMessage(Component.text("  \u2726 ", TextColor.color(150, 0, 255))
                    .append(Component.text("THE END RIFT DRAGON HAS BEEN SLAIN!", TextColor.color(180, 50, 255))
                            .decorate(TextDecoration.BOLD)));
            p.sendMessage(Component.text("  The rift collapses... ", NamedTextColor.GRAY));
            p.sendMessage(Component.text("  Drops: ", NamedTextColor.GRAY));
            for (LegendaryWeapon w : dropped) {
                p.sendMessage(Component.text("    \u25B6 ", NamedTextColor.DARK_GRAY)
                        .append(Component.text("[MYTHIC] ", w.getTier().getColor()).decorate(TextDecoration.BOLD))
                        .append(Component.text(w.getDisplayName(), w.getTier().getColor()).decorate(TextDecoration.BOLD))
                        .append(Component.text(" (DMG " + w.getBaseDamage() + ")", NamedTextColor.GRAY)));
            }
            p.sendMessage(Component.empty());
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        }

        boss = null;
        endEvent(false);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!active || boss == null || boss.isDead()) return;
        if (!(event.getEntity().equals(boss))) return;

        // Boss damage resistance: reduce all damage by 20% to make fight last longer
        event.setDamage(event.getDamage() * 0.80);

        // Visual feedback on hit
        Location bossLoc = boss.getLocation();
        riftWorld.spawnParticle(Particle.REVERSE_PORTAL, bossLoc, 10, 1, 1, 1, 0.1);
    }

    private void endEvent(boolean timeout) {
        if (!active) return;
        active = false;
        plugin.getLogger().info("End Rift event ending" + (timeout ? " (timeout)" : " (boss killed)"));

        // Destroy the portal ring
        if (riftCenter != null && riftWorld != null) {
            destroyPortalRing();
        }

        // Remove all spawned entities
        for (Entity e : spawnedEntities) {
            if (e != null && !e.isDead() && e.isValid()) {
                e.remove();
            }
        }
        spawnedEntities.clear();

        // Remove ender crystals near rift
        if (riftCenter != null && riftWorld != null) {
            for (Entity e : riftWorld.getNearbyEntities(riftCenter, 15, 15, 15)) {
                if (e instanceof EnderCrystal) {
                    e.remove();
                }
            }
        }

        // Remove boss if still alive (timeout)
        if (boss != null && !boss.isDead()) {
            boss.remove();
            boss = null;
        }

        if (riftWorld != null) {
            if (timeout) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendMessage(Component.text("  \u2726 ", NamedTextColor.DARK_PURPLE)
                            .append(Component.text("The End Rift has collapsed on its own...",
                                    NamedTextColor.LIGHT_PURPLE)));
                }
            }
            eventManager.endEvent(riftWorld);
        }

        riftWorld = null;
        riftCenter = null;
    }

    private void destroyPortalRing() {
        int cx = riftCenter.getBlockX();
        int cy = riftCenter.getBlockY();
        int cz = riftCenter.getBlockZ();
        int radius = 7;
        int height = 11;

        for (int x = -radius - 1; x <= radius + 1; x++) {
            for (int z = -radius - 1; z <= radius + 1; z++) {
                for (int y = -1; y < height + 2; y++) {
                    Location loc = new Location(riftWorld, cx + x, cy + y, cz + z);
                    Material type = loc.getBlock().getType();
                    if (type == Material.CRYING_OBSIDIAN || type == Material.END_STONE_BRICKS
                            || type == Material.PURPUR_BLOCK || type == Material.END_PORTAL_FRAME) {
                        loc.getBlock().setType(Material.AIR);
                        // Destruction particles
                        riftWorld.spawnParticle(Particle.REVERSE_PORTAL, loc.toCenterLocation(), 5, 0.3, 0.3, 0.3, 0.02);
                    }
                }
            }
        }

        // Final collapse VFX
        riftWorld.spawnParticle(Particle.EXPLOSION_EMITTER, riftCenter.clone().add(0, 2, 0), 10, 5, 5, 5);
        riftWorld.playSound(riftCenter, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.5f);
    }

    private Location getRandomSpawnLoc(Location center, int radius) {
        double angle = random.nextDouble() * Math.PI * 2;
        double dist = 3 + random.nextDouble() * radius;
        int x = (int) (center.getX() + dist * Math.cos(angle));
        int z = (int) (center.getZ() + dist * Math.sin(angle));
        int y = riftWorld.getHighestBlockYAt(x, z) + 1;
        return new Location(riftWorld, x + 0.5, y, z + 0.5);
    }

    private Player getNearestPlayer(Location loc, double range) {
        Player nearest = null;
        double minDist = range * range;
        for (Player p : riftWorld.getPlayers()) {
            double d = p.getLocation().distanceSquared(loc);
            if (d < minDist) { minDist = d; nearest = p; }
        }
        return nearest;
    }

    public boolean isActive() { return active; }
    public Location getRiftCenter() { return riftCenter; }
}
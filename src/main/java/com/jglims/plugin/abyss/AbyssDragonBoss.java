package com.jglims.plugin.abyss;

import com.jglims.plugin.JGlimsPlugin;
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
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.time.Duration;
import java.util.*;
import java.util.logging.Logger;

/**
 * Abyssal Dragon Boss v6.0
 * 
 * Spawns a REAL Ender Dragon at origin (0, Y, 0) in world_abyss.
 * The dragon naturally stays near origin since that's its hardcoded podium.
 * Arena is built at origin, so the dragon's natural AI = our desired behavior.
 * 
 * We still use setPodium() as extra insurance and confine via teleport if needed.
 */
public class AbyssDragonBoss implements Listener {

    private static final String WORLD_NAME = "world_abyss";
    private static final int ARENA_RADIUS = 45;
    private static final double DRAGON_HP = 500.0;  // Vanilla dragon = 200, we boost
    private static final double DAMAGE_REDUCTION = 0.15;  // 15% damage reduction
    private static final int CONFINE_RADIUS = ARENA_RADIUS + 15;  // Teleport back if beyond this

    private final JGlimsPlugin plugin;
    private final Logger log;

    private EnderDragon activeDragon = null;
    private boolean bossActive = false;
    private int currentPhase = 1;
    private BukkitRunnable combatLoop = null;
    private int combatTick = 0;

    public AbyssDragonBoss(JGlimsPlugin plugin) {
        this.plugin = plugin;
        this.log = plugin.getLogger();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    //  AUTO-SPAWN when player enters the Abyss dimension
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        World abyssWorld = Bukkit.getWorld(WORLD_NAME);
        if (abyssWorld == null) return;
        if (!player.getWorld().getName().equals(WORLD_NAME)) return;
        if (bossActive) return;

        // Remove any existing dragons first
        for (Entity e : abyssWorld.getEntities()) {
            if (e instanceof EnderDragon) {
                e.remove();
            }
        }

        // Spawn dragon 5 seconds after arrival
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline() || !player.getWorld().getName().equals(WORLD_NAME)) return;
            if (bossActive) return;
            spawnDragon(abyssWorld, player);
        }, 100L); // 5 seconds
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    //  SPAWN THE REAL ENDER DRAGON
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    public void spawnDragon(World world, Player triggerPlayer) {
        if (bossActive) {
            if (triggerPlayer != null) {
                triggerPlayer.sendMessage(Component.text("The Abyssal Dragon is already active!", NamedTextColor.RED));
            }
            return;
        }

        log.info("[AbyssDragon] Spawning Ender Dragon at origin in " + WORLD_NAME);

        int arenaY = findArenaY(world);
        Location spawnLoc = new Location(world, 0.5, arenaY + 15, 0.5);

        // Spawn the dragon
        activeDragon = world.spawn(spawnLoc, EnderDragon.class, dragon -> {
            // Set phase to CIRCLING (0) - this gives it full AI
            dragon.setPhase(EnderDragon.Phase.CIRCLING);

            // Set podium to origin - this is where it orbits and perches
            dragon.setPodium(new Location(world, 0, arenaY, 0));

            // Boost HP
            if (dragon.getAttribute(Attribute.MAX_HEALTH) != null) {
                dragon.getAttribute(Attribute.MAX_HEALTH).setBaseValue(DRAGON_HP);
            }
            dragon.setHealth(DRAGON_HP);

            // Custom name
            dragon.customName(Component.text("Abyssal Dragon", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD));
            dragon.setCustomNameVisible(false);  // Boss bar shows the name instead
        });

        if (activeDragon == null || activeDragon.isDead()) {
            log.warning("[AbyssDragon] Failed to spawn dragon!");
            return;
        }

        bossActive = true;
        currentPhase = 1;
        combatTick = 0;

        // Announce to all players in the dimension
        for (Player p : world.getPlayers()) {
            p.showTitle(Title.title(
                Component.text("The Abyssal Dragon", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD),
                Component.text("has awakened...", NamedTextColor.LIGHT_PURPLE),
                Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500))
            ));
            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0f, 0.5f);
        }

        // Start combat loop
        startCombatLoop(world);

        log.info("[AbyssDragon] Dragon spawned! HP=" + DRAGON_HP + " at Y=" + arenaY);
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    //  COMBAT LOOP (runs every 10 ticks = 0.5s)
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    private void startCombatLoop(World world) {
        if (combatLoop != null) {
            combatLoop.cancel();
        }

        combatLoop = new BukkitRunnable() {
            @Override
            public void run() {
                if (!bossActive || activeDragon == null || activeDragon.isDead()) {
                    bossActive = false;
                    this.cancel();
                    return;
                }

                combatTick++;
                int arenaY = findArenaY(world);
                Location arenaCenter = new Location(world, 0.5, arenaY + 1, 0.5);

                // â”€â”€ Confinement check â”€â”€
                // If dragon drifts too far, nudge it back by resetting podium
                Location dragonLoc = activeDragon.getLocation();
                double distFromOrigin = Math.sqrt(dragonLoc.getX() * dragonLoc.getX() + dragonLoc.getZ() * dragonLoc.getZ());

                if (distFromOrigin > CONFINE_RADIUS) {
                    // Reset podium to origin to call it back
                    activeDragon.setPodium(new Location(world, 0, arenaY, 0));
                    activeDragon.setPhase(EnderDragon.Phase.CIRCLING);
                    log.info("[AbyssDragon] Dragon exceeded radius, resetting podium. Dist=" + (int) distFromOrigin);
                }

                // â”€â”€ Phase management â”€â”€
                double hp = activeDragon.getHealth();
                double maxHp = DRAGON_HP;
                double hpPercent = hp / maxHp;

                int newPhase;
                if (hpPercent > 0.75) newPhase = 1;
                else if (hpPercent > 0.50) newPhase = 2;
                else if (hpPercent > 0.25) newPhase = 3;
                else newPhase = 4;

                if (newPhase != currentPhase) {
                    currentPhase = newPhase;
                    announcePhase(world, newPhase, hpPercent);
                }

                // â”€â”€ Phase-specific attacks (every few seconds) â”€â”€
                List<Player> nearbyPlayers = new ArrayList<>();
                for (Player p : world.getPlayers()) {
                    if (p.getGameMode() != GameMode.SPECTATOR && p.getGameMode() != GameMode.CREATIVE) {
                        nearbyPlayers.add(p);
                    }
                }
                if (nearbyPlayers.isEmpty()) return;

                // Phase 2+: spawn minions occasionally
                if (currentPhase >= 2 && combatTick % 40 == 0) { // every 20 seconds
                    int minionCount = countMinions(world);
                    if (minionCount < 3) {
                        spawnMinion(world, arenaCenter);
                    }
                }

                // Phase 3+: lightning strikes
                if (currentPhase >= 3 && combatTick % 20 == 0) { // every 10 seconds
                    Player target = nearbyPlayers.get(new Random().nextInt(nearbyPlayers.size()));
                    Location strikeLoc = target.getLocation();
                    world.strikeLightningEffect(strikeLoc);
                    // Actual damage via nearby explosion
                    world.createExplosion(strikeLoc, 2.0f, false, false);
                }

                // Phase 4: enrage - more frequent attacks, fire charge rain
                if (currentPhase >= 4 && combatTick % 10 == 0) { // every 5 seconds
                    for (Player p : nearbyPlayers) {
                        Location above = p.getLocation().add(0, 15, 0);
                        world.spawn(above, org.bukkit.entity.SmallFireball.class, fb -> {
                            fb.setDirection(new Vector(0, -1, 0));
                            fb.setIsIncendiary(false);
                            fb.setYield(0);
                        });
                    }
                }

                // â”€â”€ Ambient particles â”€â”€
                if (combatTick % 4 == 0) {
                    world.spawnParticle(Particle.DRAGON_BREATH,
                        arenaCenter.getX(), arenaCenter.getY() + 5, arenaCenter.getZ(),
                        5, 3, 3, 3, 0.01);
                }
            }
        };
        combatLoop.runTaskTimer(plugin, 10L, 10L);
    }

    private void announcePhase(World world, int phase, double hpPercent) {
        String[] phaseNames = {"", "Awakening", "Fury", "Wrath", "Oblivion"};
        NamedTextColor[] colors = {NamedTextColor.WHITE, NamedTextColor.YELLOW, NamedTextColor.GOLD, NamedTextColor.RED, NamedTextColor.DARK_RED};

        for (Player p : world.getPlayers()) {
            p.showTitle(Title.title(
                Component.text("Phase " + phase + ": " + phaseNames[phase], colors[phase], TextDecoration.BOLD),
                Component.text((int)(hpPercent * 100) + "% HP remaining", NamedTextColor.GRAY),
                Title.Times.times(Duration.ofMillis(300), Duration.ofSeconds(2), Duration.ofMillis(300))
            ));
            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0f, 0.7f + phase * 0.1f);
        }
        log.info("[AbyssDragon] Phase " + phase + ": " + phaseNames[phase] + " (" + (int)(hpPercent * 100) + "% HP)");
    }

    private void spawnMinion(World world, Location center) {
        double angle = Math.random() * Math.PI * 2;
        double dist = 10 + Math.random() * 20;
        Location loc = center.clone().add(Math.cos(angle) * dist, 1, Math.sin(angle) * dist);
        loc.setY(findSafeY(world, loc.getBlockX(), loc.getBlockZ(), center.getBlockY()));

        WitherSkeleton minion = (WitherSkeleton) world.spawnEntity(loc, EntityType.WITHER_SKELETON);
        minion.customName(Component.text("Dragon's Servant", NamedTextColor.DARK_GRAY));
        minion.setCustomNameVisible(true);
        minion.setMaxHealth(40);
        minion.setHealth(40);
        minion.addScoreboardTag("abyss_minion");
        minion.getEquipment().setItemInMainHand(new ItemStack(Material.STONE_SWORD));
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

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    //  DAMAGE HANDLER (reduce damage to make boss tougher)
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    @EventHandler
    public void onDragonDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof EnderDragon dragon)) return;
        if (!bossActive || activeDragon == null) return;
        if (!dragon.getWorld().getName().equals(WORLD_NAME)) return;

        // Apply damage reduction
        double originalDamage = event.getDamage();
        double reducedDamage = originalDamage * (1.0 - DAMAGE_REDUCTION);
        event.setDamage(reducedDamage);

        // Phase 4 enrage: reflect some damage
        if (currentPhase >= 4 && event.getDamager() instanceof Player player) {
            double reflect = originalDamage * 0.1;
            Bukkit.getScheduler().runTask(plugin, () -> player.damage(reflect));
        }
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    //  DEATH HANDLER
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    @EventHandler
    public void onDragonDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof EnderDragon dragon)) return;
        if (!dragon.getWorld().getName().equals(WORLD_NAME)) return;
        if (!bossActive) return;

        log.info("[AbyssDragon] The Abyssal Dragon has been defeated!");

        bossActive = false;
        activeDragon = null;
        if (combatLoop != null) {
            combatLoop.cancel();
            combatLoop = null;
        }

        World world = dragon.getWorld();
        int arenaY = findArenaY(world);

        // Clear minions
        clearMinions(world);

        // Drop event: cancel vanilla drops, add custom
        event.getDrops().clear();
        event.setDroppedExp(0);

        Location dropLoc = new Location(world, 0.5, arenaY + 2, 0.5);

        // Custom loot drops
        world.dropItemNaturally(dropLoc, new ItemStack(Material.DRAGON_EGG, 1));
        world.dropItemNaturally(dropLoc, new ItemStack(Material.NETHER_STAR, 3));
        world.dropItemNaturally(dropLoc, new ItemStack(Material.EXPERIENCE_BOTTLE, 64));
        world.dropItemNaturally(dropLoc, new ItemStack(Material.NETHERITE_INGOT, 4));
        world.dropItemNaturally(dropLoc, new ItemStack(Material.ELYTRA, 1));
        world.dropItemNaturally(dropLoc, new ItemStack(Material.TOTEM_OF_UNDYING, 2));
        world.dropItemNaturally(dropLoc, new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 8));
        world.dropItemNaturally(dropLoc, new ItemStack(Material.END_CRYSTAL, 4));

        // Bonus end-game drops (replacing legendary weapon logic to avoid compile issues)
        world.dropItemNaturally(dropLoc, new ItemStack(Material.NETHERITE_SWORD, 1));
        world.dropItemNaturally(dropLoc, new ItemStack(Material.NETHERITE_CHESTPLATE, 1));
        world.dropItemNaturally(dropLoc, new ItemStack(Material.DIAMOND_BLOCK, 8));
        world.dropItemNaturally(dropLoc, new ItemStack(Material.ECHO_SHARD, 4));

        // XP orbs
        for (int i = 0; i < 20; i++) {
            world.spawn(dropLoc.clone().add(Math.random() * 4 - 2, 1, Math.random() * 4 - 2),
                ExperienceOrb.class, orb -> orb.setExperience(500));
        }

        // Victory announcement
        for (Player p : world.getPlayers()) {
            p.showTitle(Title.title(
                Component.text("VICTORY!", NamedTextColor.GOLD, TextDecoration.BOLD),
                Component.text("The Abyssal Dragon has fallen!", NamedTextColor.YELLOW),
                Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(5), Duration.ofSeconds(2))
            ));
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 2.0f, 1.0f);
        }

        // Create exit portal at origin after 10 seconds
        Bukkit.getScheduler().runTaskLater(plugin, () -> createExitPortal(world, arenaY), 200L);
    }

    private void createExitPortal(World world, int arenaY) {
        // Bedrock frame + end portal blocks
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                world.getBlockAt(dx, arenaY + 1, dz).setType(Material.BEDROCK, false);
                if (Math.abs(dx) <= 1 && Math.abs(dz) <= 1) {
                    world.getBlockAt(dx, arenaY + 2, dz).setType(Material.END_PORTAL, false);
                }
            }
        }
        // Frame edges
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (Math.abs(dx) == 2 || Math.abs(dz) == 2) {
                    world.getBlockAt(dx, arenaY + 1, dz).setType(Material.BEDROCK, false);
                    world.getBlockAt(dx, arenaY + 2, dz).setType(Material.BEDROCK, false);
                }
            }
        }

        for (Player p : world.getPlayers()) {
            p.sendMessage(Component.text("An exit portal has appeared at the arena center!", NamedTextColor.GREEN));
        }
        log.info("[AbyssDragon] Exit portal created at origin.");
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    //  MANUAL TRIGGER
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    public void manualTrigger(Player player) {
        World world = Bukkit.getWorld(WORLD_NAME);
        if (world == null) {
            player.sendMessage(Component.text("Abyss dimension not loaded!", NamedTextColor.RED));
            return;
        }
        if (!player.getWorld().getName().equals(WORLD_NAME)) {
            // Teleport player to arena first
            int arenaY = findArenaY(world);
            player.teleport(new Location(world, 0.5, arenaY + 1, 0.5));
        }
        spawnDragon(world, player);
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    //  HELPERS
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    private int findArenaY(World world) {
        // Arena is at origin (0, Y, 0) - find the bedrock floor
        for (int y = 120; y >= 0; y--) {
            if (world.getBlockAt(0, y, 0).getType() == Material.BEDROCK) {
                return y;
            }
        }
        return 50; // Default (should match builder's baseY)
    }

    private int findSafeY(World world, int x, int z, int defaultY) {
        for (int y = defaultY + 10; y >= defaultY - 10; y--) {
            if (world.getBlockAt(x, y, z).getType() != Material.AIR
                && world.getBlockAt(x, y + 1, z).getType() == Material.AIR
                && world.getBlockAt(x, y + 2, z).getType() == Material.AIR) {
                return y + 1;
            }
        }
        return defaultY;
    }

    public boolean isBossActive() { return bossActive; }
}
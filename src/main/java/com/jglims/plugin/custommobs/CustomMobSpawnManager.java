package com.jglims.plugin.custommobs;

import com.jglims.plugin.JGlimsPlugin;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Handles natural spawning of custom mobs in appropriate biomes and dimensions.
 * Runs on a periodic scheduler and spawns mobs near online players based on
 * configurable spawn rates, dimension rules, and population caps.
 */
public class CustomMobSpawnManager {

    private final JGlimsPlugin plugin;
    private final CustomMobManager mobManager;
    private final Random random = new Random();

    /** Maximum total custom mobs alive at once (server-wide). */
    private static final int MAX_TOTAL_MOBS = 100;

    /** Maximum mobs of a single type alive at once. */
    private static final int MAX_PER_TYPE = 10;

    /** Spawn check interval in ticks (every 30 seconds). */
    // Was 600L (30s). Halved to 300L so custom-mob dimensions (especially
    // Jurassic) feel alive faster after a player joins.
    private static final long SPAWN_INTERVAL = 300L;

    /** Spawn attempt radius around players (in blocks). */
    private static final int SPAWN_RADIUS = 60;

    /** Minimum distance from player for spawns (in blocks). */
    private static final int MIN_SPAWN_DISTANCE = 24;

    /**
     * Spawn rule: defines where and how often a mob type naturally spawns.
     */
    private record SpawnRule(
            CustomMobType type,
            double chancePerCheck,
            Set<String> dimensions,
            Set<Biome> biomes,
            int maxActive
    ) {}

    private final List<SpawnRule> spawnRules = new ArrayList<>();

    public CustomMobSpawnManager(JGlimsPlugin plugin, CustomMobManager mobManager) {
        this.plugin = plugin;
        this.mobManager = mobManager;
        registerSpawnRules();
    }

    /**
     * Starts the periodic spawn scheduler.
     */
    public void startScheduler() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (mobManager.getActiveMobCount() >= MAX_TOTAL_MOBS) return;
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    if (player.getGameMode() == GameMode.SPECTATOR) continue;
                    attemptSpawnsNearPlayer(player);
                }
            }
        }.runTaskTimer(plugin, SPAWN_INTERVAL, SPAWN_INTERVAL);
        plugin.getLogger().info("[CustomMobSpawn] Natural spawn scheduler started.");
    }

    /**
     * Attempts to spawn custom mobs near a given player based on their
     * current dimension and biome.
     */
    private void attemptSpawnsNearPlayer(Player player) {
        Location playerLoc = player.getLocation();
        String worldName = playerLoc.getWorld().getName();

        for (SpawnRule rule : spawnRules) {
            // Check dimension
            if (!rule.dimensions().isEmpty() && !rule.dimensions().contains(worldName)) continue;

            // Check population cap
            if (mobManager.getActiveMobCount(rule.type()) >= rule.maxActive()) continue;

            // Roll spawn chance
            if (random.nextDouble() > rule.chancePerCheck()) continue;

            // Find spawn location
            Location spawnLoc = findSpawnLocation(playerLoc, rule);
            if (spawnLoc == null) continue;

            // Check biome if required
            if (!rule.biomes().isEmpty()) {
                Biome biome = spawnLoc.getWorld().getBiome(spawnLoc);
                if (!rule.biomes().contains(biome)) continue;
            }

            mobManager.spawnMob(rule.type(), spawnLoc);
        }
    }

    /**
     * Finds a valid spawn location near the player.
     *
     * @param playerLoc the player's location
     * @param rule      the spawn rule (for biome/dimension filtering)
     * @return a valid spawn location, or null if none found after 5 attempts
     */
    private Location findSpawnLocation(Location playerLoc, SpawnRule rule) {
        World world = playerLoc.getWorld();
        for (int attempt = 0; attempt < 5; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double distance = MIN_SPAWN_DISTANCE + random.nextDouble() * (SPAWN_RADIUS - MIN_SPAWN_DISTANCE);
            int x = (int) (playerLoc.getX() + Math.cos(angle) * distance);
            int z = (int) (playerLoc.getZ() + Math.sin(angle) * distance);
            int y = world.getHighestBlockYAt(x, z) + 1;

            if (y < world.getMinHeight() + 5 || y > world.getMaxHeight() - 10) continue;

            Location loc = new Location(world, x + 0.5, y, z + 0.5);
            // Basic validity: block below is solid, block at spawn is air
            if (loc.clone().add(0, -1, 0).getBlock().getType().isSolid()
                    && !loc.getBlock().getType().isSolid()) {
                return loc;
            }
        }
        return null;
    }

    /**
     * Registers all natural spawn rules. Each rule defines which mob type
     * spawns where, how often, and the population cap.
     */
    private void registerSpawnRules() {
        // ── Overworld ──
        addRule(CustomMobType.GRIZZLY_BEAR, 0.03, Set.of("world"), biomes(
                Biome.FOREST, Biome.BIRCH_FOREST, Biome.OLD_GROWTH_BIRCH_FOREST,
                Biome.OLD_GROWTH_PINE_TAIGA, Biome.OLD_GROWTH_SPRUCE_TAIGA, Biome.TAIGA), 6);

        addRule(CustomMobType.GRASS_FATHER, 0.005, Set.of("world"), biomes(
                Biome.PLAINS, Biome.SUNFLOWER_PLAINS, Biome.MEADOW), 2);

        addRule(CustomMobType.UNICORN, 0.005, Set.of("world"), biomes(Biome.CHERRY_GROVE), 2);

        addRule(CustomMobType.OVERGROWN_UNICORN, 0.005, Set.of("world"), biomes(
                Biome.SWAMP, Biome.MANGROVE_SWAMP), 2);

        addRule(CustomMobType.LEATHERN_DRAKE, 0.008, Set.of("world"), biomes(
                Biome.DESERT, Biome.BADLANDS, Biome.ERODED_BADLANDS), 3);

        addRule(CustomMobType.ICE_WYVERN, 0.008, Set.of("world"), biomes(
                Biome.SNOWY_PLAINS, Biome.SNOWY_SLOPES, Biome.FROZEN_PEAKS, Biome.ICE_SPIKES), 3);

        addRule(CustomMobType.NECROMANCER, 0.015, Set.of("world"), biomes(
                Biome.DARK_FOREST), 4);

        addRule(CustomMobType.ANGLER, 0.02, Set.of("world"), biomes(
                Biome.DEEP_OCEAN, Biome.DEEP_COLD_OCEAN, Biome.DEEP_FROZEN_OCEAN,
                Biome.DEEP_LUKEWARM_OCEAN), 5);

        addRule(CustomMobType.REDSTONE_GOLEM, 0.01, Set.of("world"), Set.of(), 4);

        // ── Nether ──
        addRule(CustomMobType.DEMON_GUY, 0.02, Set.of("world_nether"), Set.of(), 6);
        addRule(CustomMobType.BASALT_GOLEM, 0.015, Set.of("world_nether"), biomes(
                Biome.BASALT_DELTAS), 4);

        // ── End ──
        addRule(CustomMobType.SHADOWSAIL, 0.005, Set.of("world_the_end"), Set.of(), 2);

        // ── Aether ──
        addRule(CustomMobType.BLUE_TROLL, 0.06, Set.of("world_aether"), Set.of(), 8);
        addRule(CustomMobType.LOW_POLY_TROLL, 0.04, Set.of("world_aether"), Set.of(), 6);
        addRule(CustomMobType.THE_KEEPER, 0.01, Set.of("world_aether", "world_lunar"), Set.of(), 3);
        addRule(CustomMobType.SOUL_STEALER, 0.02, Set.of("world_aether"), Set.of(), 4);

        // ── Lunar ──
        addRule(CustomMobType.INVADERLING_SOLDIER, 0.06, Set.of("world_lunar"), Set.of(), 8);
        addRule(CustomMobType.INVADERLING_ARCHER, 0.04, Set.of("world_lunar"), Set.of(), 6);
        addRule(CustomMobType.INVADERLING_RIDER, 0.02, Set.of("world_lunar"), Set.of(), 4);

        // ── Jurassic ── (density boosted ~4x — player reported the dimension
        // felt empty, and the old rates produced maybe one dino every few
        // minutes). Raised chances and max-active caps across the board.
        addRule(CustomMobType.VELOCIRAPTOR,   0.22, Set.of("world_jurassic"), Set.of(), 16);
        addRule(CustomMobType.SPINOSAURUS,    0.08, Set.of("world_jurassic"), Set.of(),  6);
        addRule(CustomMobType.TREMORSAURUS,   0.06, Set.of("world_jurassic"), Set.of(),  6);
        addRule(CustomMobType.STEGOSAURUS,    0.12, Set.of("world_jurassic"), Set.of(), 10);
        addRule(CustomMobType.GROTTOCERATOPS, 0.12, Set.of("world_jurassic"), Set.of(), 10);
        addRule(CustomMobType.BASILISK,       0.06, Set.of("world_jurassic"), Set.of(),  6);
        addRule(CustomMobType.SUBTERRANODON,  0.08, Set.of("world_jurassic"), Set.of(),  6);
        // Additional ambient population — grizzly bears make sense in the
        // Jurassic valleys and weren't previously spawning there at all
        addRule(CustomMobType.GRIZZLY_BEAR,   0.10, Set.of("world_jurassic"), Set.of(),  8);
    }

    private void addRule(CustomMobType type, double chance, Set<String> dimensions,
                         Set<Biome> biomes, int maxActive) {
        spawnRules.add(new SpawnRule(type, chance, dimensions, biomes, maxActive));
    }

    @SafeVarargs
    private static <T> Set<T> biomes(T... values) {
        return Set.of(values);
    }
}

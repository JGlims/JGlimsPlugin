package com.jglims.plugin.dimensions;

import com.jglims.plugin.JGlimsPlugin;
import org.bukkit.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Manages the Aether dimension (world_aether).
 * <p>
 * Environment: Floating sky islands, bright heavenly atmosphere.
 * THE_END environment with custom sky. Always daytime, clear weather.
 * Players get intermittent Slow Falling (reduced gravity feel).
 * White/gold/cyan palette. Portal: Glowstone frame + Water Bucket activation.
 */
public class AetherDimensionManager implements Listener {

    private static final String WORLD_NAME = "world_aether";

    private final JGlimsPlugin plugin;
    private World aetherWorld;

    public AetherDimensionManager(JGlimsPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Creates and initializes the Aether world.
     */
    public void initialize() {
        plugin.getLogger().info("[Aether] Initializing Aether dimension...");

        WorldCreator creator = new WorldCreator(WORLD_NAME);
        creator.environment(World.Environment.THE_END);
        creator.generator(new AetherChunkGenerator());
        creator.generateStructures(false);
        aetherWorld = Bukkit.createWorld(creator);

        if (aetherWorld == null) {
            plugin.getLogger().severe("[Aether] FAILED to create world_aether!");
            return;
        }

        // Configure world settings
        aetherWorld.setDifficulty(Difficulty.HARD);
        aetherWorld.setTime(6000); // Permanent noon
        aetherWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        aetherWorld.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        aetherWorld.setGameRule(GameRule.KEEP_INVENTORY, false);
        aetherWorld.setClearWeatherDuration(Integer.MAX_VALUE);

        // Set spawn point on the nearest island surface
        Location spawn = findSafeSpawn();
        aetherWorld.setSpawnLocation(spawn);

        // Slow Falling effect scheduler for all players in the Aether
        startGravityEffect();

        plugin.getLogger().info("[Aether] Aether dimension initialized. Spawn: "
                + spawn.getBlockX() + ", " + spawn.getBlockY() + ", " + spawn.getBlockZ());
    }

    /**
     * Periodically applies Slow Falling to players in the Aether
     * to simulate reduced gravity.
     */
    private void startGravityEffect() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (aetherWorld == null) return;
                for (Player player : aetherWorld.getPlayers()) {
                    // Intermittent slow falling (always active in Aether)
                    if (!player.hasPotionEffect(PotionEffectType.SLOW_FALLING)) {
                        player.addPotionEffect(new PotionEffect(
                                PotionEffectType.SLOW_FALLING, 100, 0, false, false, false));
                    }
                }
            }
        }.runTaskTimer(plugin, 60L, 60L);
    }

    /**
     * Apply effects when a player enters the Aether.
     */
    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        if (player.getWorld().getName().equals(WORLD_NAME)) {
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.SLOW_FALLING, 200, 0, false, false, false));
            player.sendMessage(net.kyori.adventure.text.Component.text(
                    "You have entered the Aether.", net.kyori.adventure.text.format.TextColor.color(100, 200, 255)));
        }
    }

    /**
     * Finds a safe spawn location by scanning near origin for solid ground.
     */
    private Location findSafeSpawn() {
        if (aetherWorld == null) return new Location(aetherWorld, 0, 100, 0);
        for (int y = 200; y > 50; y--) {
            if (aetherWorld.getBlockAt(0, y - 1, 0).getType().isSolid()
                    && !aetherWorld.getBlockAt(0, y, 0).getType().isSolid()
                    && !aetherWorld.getBlockAt(0, y + 1, 0).getType().isSolid()) {
                return new Location(aetherWorld, 0.5, y, 0.5);
            }
        }
        return new Location(aetherWorld, 0, 120, 0);
    }

    public World getAetherWorld() { return aetherWorld; }
    public static String getWorldName() { return WORLD_NAME; }
}

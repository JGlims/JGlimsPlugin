package com.jglims.plugin.dimensions;

import com.jglims.plugin.JGlimsPlugin;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Manages the Lunar dimension (world_lunar).
 * <p>
 * Environment: Moon-like landscape with low gravity, dark sky.
 * Players get permanent Jump Boost II + Slow Falling + Night Vision.
 * Portal: End Stone frame + Ender Eye activation.
 */
public class LunarDimensionManager implements Listener {

    private static final String WORLD_NAME = "world_lunar";

    private final JGlimsPlugin plugin;
    private World lunarWorld;

    public LunarDimensionManager(JGlimsPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Creates and initializes the Lunar world.
     */
    public void initialize() {
        plugin.getLogger().info("[Lunar] Initializing Lunar dimension...");

        WorldCreator creator = new WorldCreator(WORLD_NAME);
        creator.environment(World.Environment.NORMAL);
        creator.generator(new LunarChunkGenerator());
        creator.generateStructures(false);
        lunarWorld = Bukkit.createWorld(creator);

        if (lunarWorld == null) {
            plugin.getLogger().severe("[Lunar] FAILED to create world_lunar!");
            return;
        }

        lunarWorld.setDifficulty(Difficulty.HARD);
        lunarWorld.setTime(18000); // Permanent midnight
        lunarWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        lunarWorld.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        lunarWorld.setClearWeatherDuration(Integer.MAX_VALUE);

        Location spawn = findSafeSpawn();
        lunarWorld.setSpawnLocation(spawn);

        startLunarEffects();

        plugin.getLogger().info("[Lunar] Lunar dimension initialized. Spawn: "
                + spawn.getBlockX() + ", " + spawn.getBlockY() + ", " + spawn.getBlockZ());
    }

    /**
     * Applies low-gravity and night vision effects to all players in the Lunar dimension.
     */
    private void startLunarEffects() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (lunarWorld == null) return;
                for (Player player : lunarWorld.getPlayers()) {
                    player.addPotionEffect(new PotionEffect(
                            PotionEffectType.JUMP_BOOST, 100, 1, false, false, false));
                    player.addPotionEffect(new PotionEffect(
                            PotionEffectType.SLOW_FALLING, 100, 0, false, false, false));
                    player.addPotionEffect(new PotionEffect(
                            PotionEffectType.NIGHT_VISION, 400, 0, false, false, false));
                }
            }
        }.runTaskTimer(plugin, 60L, 60L);
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        if (player.getWorld().getName().equals(WORLD_NAME)) {
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.JUMP_BOOST, 200, 1, false, false, false));
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.SLOW_FALLING, 200, 0, false, false, false));
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.NIGHT_VISION, 600, 0, false, false, false));
            player.sendMessage(net.kyori.adventure.text.Component.text(
                    "You have landed on the Lunar surface.",
                    net.kyori.adventure.text.format.TextColor.color(180, 180, 200)));
        }
    }

    private Location findSafeSpawn() {
        if (lunarWorld == null) return new Location(lunarWorld, 0, 80, 0);
        for (int y = 120; y > 20; y--) {
            if (lunarWorld.getBlockAt(0, y - 1, 0).getType().isSolid()
                    && !lunarWorld.getBlockAt(0, y, 0).getType().isSolid()
                    && !lunarWorld.getBlockAt(0, y + 1, 0).getType().isSolid()) {
                return new Location(lunarWorld, 0.5, y, 0.5);
            }
        }
        return new Location(lunarWorld, 0, 80, 0);
    }

    public World getLunarWorld() { return lunarWorld; }
    public static String getWorldName() { return WORLD_NAME; }
}

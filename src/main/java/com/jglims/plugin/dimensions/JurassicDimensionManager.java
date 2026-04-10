package com.jglims.plugin.dimensions;

import com.jglims.plugin.JGlimsPlugin;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

/**
 * Manages the Jurassic dimension (world_jurassic).
 * <p>
 * Environment: Prehistoric jungle/plains world. NORMAL environment.
 * Lush, dense vegetation. Warm temperature.
 * Portal: Bone Block frame + Flint and Steel activation.
 */
public class JurassicDimensionManager implements Listener {

    private static final String WORLD_NAME = "world_jurassic";

    private final JGlimsPlugin plugin;
    private World jurassicWorld;

    public JurassicDimensionManager(JGlimsPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Creates and initializes the Jurassic world.
     */
    public void initialize() {
        plugin.getLogger().info("[Jurassic] Initializing Jurassic dimension...");

        WorldCreator creator = new WorldCreator(WORLD_NAME);
        creator.environment(World.Environment.NORMAL);
        creator.generator(new JurassicChunkGenerator());
        creator.generateStructures(false);
        jurassicWorld = Bukkit.createWorld(creator);

        if (jurassicWorld == null) {
            plugin.getLogger().severe("[Jurassic] FAILED to create world_jurassic!");
            return;
        }

        jurassicWorld.setDifficulty(Difficulty.HARD);
        jurassicWorld.setTime(6000); // Warm daytime
        jurassicWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
        jurassicWorld.setGameRule(GameRule.DO_WEATHER_CYCLE, true);

        Location spawn = findSafeSpawn();
        jurassicWorld.setSpawnLocation(spawn);

        plugin.getLogger().info("[Jurassic] Jurassic dimension initialized. Spawn: "
                + spawn.getBlockX() + ", " + spawn.getBlockY() + ", " + spawn.getBlockZ());
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        if (player.getWorld().getName().equals(WORLD_NAME)) {
            player.sendMessage(net.kyori.adventure.text.Component.text(
                    "You have entered the Jurassic dimension.",
                    net.kyori.adventure.text.format.TextColor.color(100, 180, 60)));
        }
    }

    private Location findSafeSpawn() {
        if (jurassicWorld == null) return new Location(jurassicWorld, 0, 80, 0);
        for (int y = 120; y > 40; y--) {
            if (jurassicWorld.getBlockAt(0, y - 1, 0).getType().isSolid()
                    && !jurassicWorld.getBlockAt(0, y, 0).getType().isSolid()
                    && !jurassicWorld.getBlockAt(0, y + 1, 0).getType().isSolid()) {
                return new Location(jurassicWorld, 0.5, y, 0.5);
            }
        }
        return new Location(jurassicWorld, 0, 80, 0);
    }

    public World getJurassicWorld() { return jurassicWorld; }
    public static String getWorldName() { return WORLD_NAME; }
}

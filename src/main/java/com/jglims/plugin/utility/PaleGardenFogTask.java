package com.jglims.plugin.utility;

import com.jglims.plugin.JGlimsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PaleGardenFogTask implements Runnable {

    private final JGlimsPlugin plugin;

    public PaleGardenFogTask(JGlimsPlugin plugin) {
        this.plugin = plugin;
    }

    public void start(int intervalTicks) {
        Bukkit.getScheduler().runTaskTimer(plugin, this, 0L, intervalTicks);
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getWorld().getBiome(player.getLocation()) == Biome.PALE_GARDEN) {
                // Apply very mild Darkness (level 0 = weakest)
                player.addPotionEffect(new PotionEffect(
                    PotionEffectType.DARKNESS,
                    80, // 4 seconds (re-applied every 2s, so always active)
                    0,  // level 0 (mildest)
                    true, // ambient
                    false, // no particles
                    false  // no icon
                ));
            } else {
                // Remove darkness if they left Pale Garden
                if (player.hasPotionEffect(PotionEffectType.DARKNESS)) {
                    player.removePotionEffect(PotionEffectType.DARKNESS);
                }
            }
        }
    }
}
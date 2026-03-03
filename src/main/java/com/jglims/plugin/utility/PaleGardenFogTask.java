package com.jglims.plugin.utility;

import org.bukkit.Bukkit;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.jglims.plugin.JGlimsPlugin;

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
        boolean bloodMoonActive = plugin.getBloodMoonManager() != null
            && plugin.getBloodMoonManager().isBloodMoonActive();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getWorld().getBiome(player.getLocation()) == Biome.PALE_GARDEN) {
                player.addPotionEffect(new PotionEffect(
                    PotionEffectType.DARKNESS,
                    80, 0, true, false, false
                ));
            } else {
                // Only remove darkness if Blood Moon is NOT active
                if (!bloodMoonActive && player.hasPotionEffect(PotionEffectType.DARKNESS)) {
                    player.removePotionEffect(PotionEffectType.DARKNESS);
                }
            }
        }
    }
}
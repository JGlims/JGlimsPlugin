package com.jglims.plugin.utility;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.config.ConfigManager;
import org.bukkit.event.Listener;

public class DropRateListener implements Listener {

    private final JGlimsPlugin plugin;
    private final ConfigManager config;

    public DropRateListener(JGlimsPlugin plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
    }

    // TODO: Implement boosted Prismarine Shard drops from Guardians/Elder Guardians
    // TODO: Implement boosted Ghast Tear drops
    // TODO: Implement boosted Echo Shard chance in Ancient City chests
}
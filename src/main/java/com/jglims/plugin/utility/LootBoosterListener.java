package com.jglims.plugin.utility;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.config.ConfigManager;
import org.bukkit.event.Listener;

public class LootBoosterListener implements Listener {

    private final JGlimsPlugin plugin;
    private final ConfigManager config;

    public LootBoosterListener(JGlimsPlugin plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
    }

    // TODO: Implement guaranteed enchanted book in generated chests (LootGenerateEvent)
    // TODO: Implement better librarian first-tier trades
}
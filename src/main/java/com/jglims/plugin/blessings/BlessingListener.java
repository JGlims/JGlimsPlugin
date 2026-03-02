package com.jglims.plugin.blessings;

import com.jglims.plugin.JGlimsPlugin;
import org.bukkit.event.Listener;

public class BlessingListener implements Listener {

    private final JGlimsPlugin plugin;
    private final BlessingManager blessingManager;

    public BlessingListener(JGlimsPlugin plugin, BlessingManager blessingManager) {
        this.plugin = plugin;
        this.blessingManager = blessingManager;
    }

    // TODO: Implement right-click consume for C's, Ami's, La's Bless
    // TODO: Implement PlayerRespawnEvent to re-apply blessings
    // TODO: Implement PlayerJoinEvent to re-apply blessings
}
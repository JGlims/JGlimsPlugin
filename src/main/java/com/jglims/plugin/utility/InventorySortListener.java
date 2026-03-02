package com.jglims.plugin.utility;

import com.jglims.plugin.JGlimsPlugin;
import org.bukkit.event.Listener;

public class InventorySortListener implements Listener {

    private final JGlimsPlugin plugin;

    public InventorySortListener(JGlimsPlugin plugin) {
        this.plugin = plugin;
    }

    // TODO: Implement shift-click on empty slot to sort inventory
    // Categories: Weapons > Tools > Armor > Blocks > Food > Potions > Enchanted Books > Materials > Misc
}
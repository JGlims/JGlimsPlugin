package com.jglims.plugin.enchantments;

import com.jglims.plugin.JGlimsPlugin;
import org.bukkit.event.Listener;

public class AnvilRecipeListener implements Listener {

    private final JGlimsPlugin plugin;
    private final CustomEnchantManager enchantManager;

    public AnvilRecipeListener(JGlimsPlugin plugin, CustomEnchantManager enchantManager) {
        this.plugin = plugin;
        this.enchantManager = enchantManager;
    }

    // TODO: Implement anvil recipe detection for custom enchantment books
    // TODO: Implement "Too Expensive" removal
    // TODO: Implement XP cost reduction
}
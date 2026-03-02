package com.jglims.plugin.utility;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.enchantments.CustomEnchantManager;
import org.bukkit.event.Listener;

public class EnchantTransferListener implements Listener {

    private final JGlimsPlugin plugin;
    private final CustomEnchantManager enchantManager;

    public EnchantTransferListener(JGlimsPlugin plugin, CustomEnchantManager enchantManager) {
        this.plugin = plugin;
        this.enchantManager = enchantManager;
    }

    // TODO: Implement tool + book in anvil -> enchanted book with all enchants, 0 XP
}
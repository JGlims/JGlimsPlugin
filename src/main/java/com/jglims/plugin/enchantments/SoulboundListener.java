package com.jglims.plugin.enchantments;

import com.jglims.plugin.JGlimsPlugin;
import org.bukkit.event.Listener;

public class SoulboundListener implements Listener {

    private final JGlimsPlugin plugin;
    private final CustomEnchantManager enchantManager;

    public SoulboundListener(JGlimsPlugin plugin, CustomEnchantManager enchantManager) {
        this.plugin = plugin;
        this.enchantManager = enchantManager;
    }

    // TODO: Implement keep-on-death for Soulbound items
    // TODO: Implement "lostsoul" rename -> Soulbound book conversion
    // TODO: Enforce one Soulbound item limit per inventory
}
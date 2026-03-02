package com.jglims.plugin.enchantments;

import com.jglims.plugin.JGlimsPlugin;
import org.bukkit.event.Listener;

public class EnchantmentEffectListener implements Listener {

    private final JGlimsPlugin plugin;
    private final CustomEnchantManager enchantManager;

    public EnchantmentEffectListener(JGlimsPlugin plugin, CustomEnchantManager enchantManager) {
        this.plugin = plugin;
        this.enchantManager = enchantManager;
    }

    // TODO: Implement on-hit effects (Vampirism, Bleed, Venomstrike, Lifesteal, Frostbite, etc.)
    // TODO: Implement on-equip effects (Night Vision, Aqua Lungs, Swiftfoot, Leaping, etc.)
    // TODO: Implement on-break effects (Veinminer, Drill, Timber, Auto-Smelt, Magnetism, etc.)
    // TODO: Implement Berserker damage scaling
    // TODO: Implement Dodge, Deflection, Stomp, Fortification
    // TODO: Implement Elytra enchants (Cushion, Boost, Glider)
    // TODO: Implement BestBuddies wolf armor effect
}
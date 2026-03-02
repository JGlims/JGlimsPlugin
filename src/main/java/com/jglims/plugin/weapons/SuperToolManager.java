package com.jglims.plugin.weapons;

import com.jglims.plugin.JGlimsPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class SuperToolManager implements Listener {

    private final JGlimsPlugin plugin;
    private final NamespacedKey superToolKey;

    public SuperToolManager(JGlimsPlugin plugin) {
        this.plugin = plugin;
        this.superToolKey = new NamespacedKey(plugin, "is_super_tool");
    }

    public NamespacedKey getSuperToolKey() {
        return superToolKey;
    }

    public boolean isSuperTool(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(superToolKey, PersistentDataType.BYTE);
    }

    // TODO: Implement super tool crafting recipes
    // TODO: Implement super tool stat application (+2 damage for non-netherite, +1 for netherite)
    // TODO: Implement Super Elytra (doubled durability)
}
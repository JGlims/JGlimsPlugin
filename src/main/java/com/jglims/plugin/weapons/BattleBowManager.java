package com.jglims.plugin.weapons;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import com.jglims.plugin.JGlimsPlugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class BattleBowManager {

    private final JGlimsPlugin plugin;
    private final NamespacedKey battleBowKey;
    private final NamespacedKey battleCrossbowKey;

    public BattleBowManager(JGlimsPlugin plugin) {
        this.plugin = plugin;
        this.battleBowKey = new NamespacedKey(plugin, "is_battle_bow");
        this.battleCrossbowKey = new NamespacedKey(plugin, "is_battle_crossbow");
    }

    public NamespacedKey getBattleBowKey() { return battleBowKey; }
    public NamespacedKey getBattleCrossbowKey() { return battleCrossbowKey; }

    public boolean isBattleBow(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(battleBowKey, PersistentDataType.BYTE);
    }

    public boolean isBattleCrossbow(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(battleCrossbowKey, PersistentDataType.BYTE);
    }

    public ItemStack createBattleBow() {
        ItemStack bow = new ItemStack(Material.BOW, 1);
        ItemMeta meta = bow.getItemMeta();
        if (meta == null) return null;

        meta.getPersistentDataContainer().set(battleBowKey, PersistentDataType.BYTE, (byte) 1);
        meta.displayName(Component.text("Battle Bow", NamedTextColor.GOLD)
            .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Custom Weapon", NamedTextColor.DARK_PURPLE)
            .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Can be upgraded to Super", NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);

        bow.setItemMeta(meta);
        return bow;
    }

    public ItemStack createBattleCrossbow() {
        ItemStack crossbow = new ItemStack(Material.CROSSBOW, 1);
        ItemMeta meta = crossbow.getItemMeta();
        if (meta == null) return null;

        meta.getPersistentDataContainer().set(battleCrossbowKey, PersistentDataType.BYTE, (byte) 1);
        meta.displayName(Component.text("Battle Crossbow", NamedTextColor.GOLD)
            .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Custom Weapon", NamedTextColor.DARK_PURPLE)
            .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Can be upgraded to Super", NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);

        crossbow.setItemMeta(meta);
        return crossbow;
    }
}

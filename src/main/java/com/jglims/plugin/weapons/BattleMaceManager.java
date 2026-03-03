package com.jglims.plugin.weapons;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import com.jglims.plugin.JGlimsPlugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class BattleMaceManager {

    private final JGlimsPlugin plugin;
    private final NamespacedKey battleMaceKey;
    private final NamespacedKey battleMaceDamageKey;
    private final NamespacedKey battleMaceSpeedKey;

    public BattleMaceManager(JGlimsPlugin plugin) {
        this.plugin = plugin;
        this.battleMaceKey = new NamespacedKey(plugin, "is_battle_mace");
        this.battleMaceDamageKey = new NamespacedKey(plugin, "battle_mace_damage");
        this.battleMaceSpeedKey = new NamespacedKey(plugin, "battle_mace_speed");
    }

    public NamespacedKey getBattleMaceKey() {
        return battleMaceKey;
    }

    public boolean isBattleMace(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(battleMaceKey, PersistentDataType.BYTE);
    }

    /**
     * Creates a Battle Mace from a vanilla mace.
     * Battle Mace: 12 base damage, 0.7 attack speed.
     */
    public ItemStack createBattleMace() {
        ItemStack mace = new ItemStack(Material.MACE, 1);
        ItemMeta meta = mace.getItemMeta();
        if (meta == null) return null;

        meta.getPersistentDataContainer().set(battleMaceKey, PersistentDataType.BYTE, (byte) 1);

        meta.displayName(Component.text("Battle Mace", NamedTextColor.DARK_RED)
            .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Custom Weapon", NamedTextColor.DARK_PURPLE)
            .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("+12.0 Attack Damage", NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("0.7 Attack Speed", NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Can be upgraded to Super", NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        // Damage: 12.0 total, player base 1.0, so modifier = 11.0
        meta.addAttributeModifier(Attribute.ATTACK_DAMAGE,
            new AttributeModifier(battleMaceDamageKey, 11.0,
                AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND));

        // Speed: 0.7 = 4.0 - 3.3
        meta.addAttributeModifier(Attribute.ATTACK_SPEED,
            new AttributeModifier(battleMaceSpeedKey, -3.3,
                AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND));

        mace.setItemMeta(meta);
        return mace;
    }
}

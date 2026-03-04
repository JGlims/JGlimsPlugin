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

/**
 * BattleTridentManager creates a Battle Trident from a vanilla trident.
 * Battle Trident = vanilla trident damage (9) + 1 = 10, same attack speed (1.1).
 * Required before upgrading to Super.
 */
public class BattleTridentManager {

    private final JGlimsPlugin plugin;
    private final NamespacedKey battleTridentKey;
    private final NamespacedKey battleTridentDamageKey;
    private final NamespacedKey battleTridentSpeedKey;

    public BattleTridentManager(JGlimsPlugin plugin) {
        this.plugin = plugin;
        this.battleTridentKey = new NamespacedKey(plugin, "is_battle_trident");
        this.battleTridentDamageKey = new NamespacedKey(plugin, "battle_trident_damage");
        this.battleTridentSpeedKey = new NamespacedKey(plugin, "battle_trident_speed");
    }

    public NamespacedKey getBattleTridentKey() { return battleTridentKey; }

    public boolean isBattleTrident(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(battleTridentKey, PersistentDataType.BYTE);
    }

    /**
     * Creates a Battle Trident from a vanilla trident.
     * Damage = 9 (vanilla) + 1 = 10, attack speed = 1.1 (unchanged).
     */
    public ItemStack createBattleTrident() {
        ItemStack battleTrident = new ItemStack(Material.TRIDENT, 1);
        ItemMeta meta = battleTrident.getItemMeta();
        if (meta == null) return null;

        // Mark as battle trident in PDC
        meta.getPersistentDataContainer().set(battleTridentKey, PersistentDataType.BYTE, (byte) 1);

        double damage = 10.0; // 9 + 1

        // Set display name
        meta.displayName(Component.text("Battle Trident", NamedTextColor.DARK_RED)
                .decoration(TextDecoration.ITALIC, false));

        // Set lore
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Custom Weapon", NamedTextColor.DARK_PURPLE)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("+" + String.format("%.1f", damage) + " Attack Damage", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("1.1 Attack Speed", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Can be upgraded to Super", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        // Attack Damage modifier: 10.0 - 1.0 (player base) = 9.0
        meta.addAttributeModifier(Attribute.ATTACK_DAMAGE,
                new AttributeModifier(battleTridentDamageKey, damage - 1.0,
                        AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND));

        // Attack Speed: 1.1 = 4.0 - 2.9
        meta.addAttributeModifier(Attribute.ATTACK_SPEED,
                new AttributeModifier(battleTridentSpeedKey, -2.9,
                        AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND));

        // Enchantment glint
        meta.setEnchantmentGlintOverride(true);

        battleTrident.setItemMeta(meta);
        return battleTrident;
    }
}

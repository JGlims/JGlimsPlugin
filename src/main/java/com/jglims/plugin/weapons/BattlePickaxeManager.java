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
 * BattlePickaxeManager creates Battle Pickaxes from vanilla pickaxes.
 * Battle Pickaxe = vanilla pickaxe damage + 1, same attack speed (1.2).
 * Retains all mining enchantments and normal mining functionality.
 * Required before upgrading to Super.
 */
public class BattlePickaxeManager {

    private final JGlimsPlugin plugin;
    private final NamespacedKey battlePickaxeKey;
    private final NamespacedKey battlePickaxeDamageKey;
    private final NamespacedKey battlePickaxeSpeedKey;

    public BattlePickaxeManager(JGlimsPlugin plugin) {
        this.plugin = plugin;
        this.battlePickaxeKey = new NamespacedKey(plugin, "is_battle_pickaxe");
        this.battlePickaxeDamageKey = new NamespacedKey(plugin, "battle_pickaxe_damage");
        this.battlePickaxeSpeedKey = new NamespacedKey(plugin, "battle_pickaxe_speed");
    }

    public NamespacedKey getBattlePickaxeKey() { return battlePickaxeKey; }

    public boolean isBattlePickaxe(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(battlePickaxeKey, PersistentDataType.BYTE);
    }

    /**
     * Creates a Battle Pickaxe from a vanilla pickaxe material.
     * Damage = vanilla pickaxe damage + 1, attack speed = 1.2 (unchanged).
     *
     * Material    | Vanilla | Battle
     * Wood        | 2       | 3
     * Stone       | 3       | 4
     * Iron        | 4       | 5
     * Gold        | 2       | 3
     * Diamond     | 5       | 6
     * Netherite   | 6       | 7
     */
    public ItemStack createBattlePickaxe(Material pickaxeMaterial) {
        double damage = getBattlePickaxeDamage(pickaxeMaterial);
        if (damage < 0) return null;

        ItemStack battlePickaxe = new ItemStack(pickaxeMaterial, 1);
        ItemMeta meta = battlePickaxe.getItemMeta();
        if (meta == null) return null;

        // Mark as battle pickaxe in PDC
        meta.getPersistentDataContainer().set(battlePickaxeKey, PersistentDataType.BYTE, (byte) 1);

        // Set display name
        String tierName = getTierName(pickaxeMaterial);
        NamedTextColor nameColor = pickaxeMaterial.name().startsWith("NETHERITE")
                ? NamedTextColor.DARK_RED : NamedTextColor.WHITE;
        meta.displayName(Component.text(tierName + " Battle Pickaxe", nameColor)
                .decoration(TextDecoration.ITALIC, false));

        // Set lore
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Custom Weapon", NamedTextColor.DARK_PURPLE)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("+" + String.format("%.1f", damage) + " Attack Damage", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("1.2 Attack Speed", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("All mining enchantments retained", NamedTextColor.GREEN)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Can be upgraded to Super", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        // Attack Damage modifier: damage - 1.0 (player base)
        meta.addAttributeModifier(Attribute.ATTACK_DAMAGE,
                new AttributeModifier(battlePickaxeDamageKey, damage - 1.0,
                        AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND));

        // Attack Speed: 1.2 = 4.0 - 2.8
        meta.addAttributeModifier(Attribute.ATTACK_SPEED,
                new AttributeModifier(battlePickaxeSpeedKey, -2.8,
                        AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND));

        // Enchantment glint
        meta.setEnchantmentGlintOverride(true);

        battlePickaxe.setItemMeta(meta);
        return battlePickaxe;
    }

    private double getBattlePickaxeDamage(Material mat) {
        return switch (mat) {
            case WOODEN_PICKAXE  -> 3.0;   // 2 + 1
            case STONE_PICKAXE   -> 4.0;   // 3 + 1
            case IRON_PICKAXE    -> 5.0;   // 4 + 1
            case GOLDEN_PICKAXE  -> 3.0;   // 2 + 1
            case DIAMOND_PICKAXE -> 6.0;   // 5 + 1
            case NETHERITE_PICKAXE -> 7.0; // 6 + 1
            default -> -1;
        };
    }

    public Material getBattlePickaxeIngredient(Material pickaxeMaterial) {
        return switch (pickaxeMaterial) {
            case WOODEN_PICKAXE   -> Material.OAK_PLANKS;
            case STONE_PICKAXE    -> Material.COBBLESTONE;
            case IRON_PICKAXE     -> Material.IRON_INGOT;
            case GOLDEN_PICKAXE   -> Material.GOLD_INGOT;
            case DIAMOND_PICKAXE  -> Material.DIAMOND;
            case NETHERITE_PICKAXE -> Material.NETHERITE_INGOT;
            default -> null;
        };
    }

    private String getTierName(Material mat) {
        return switch (mat) {
            case WOODEN_PICKAXE   -> "Wooden";
            case STONE_PICKAXE    -> "Stone";
            case IRON_PICKAXE     -> "Iron";
            case GOLDEN_PICKAXE   -> "Golden";
            case DIAMOND_PICKAXE  -> "Diamond";
            case NETHERITE_PICKAXE -> "Netherite";
            default -> "Unknown";
        };
    }

    public Material[] getBattlePickaxeTiers() {
        return new Material[]{
                Material.WOODEN_PICKAXE, Material.STONE_PICKAXE, Material.IRON_PICKAXE,
                Material.GOLDEN_PICKAXE, Material.DIAMOND_PICKAXE, Material.NETHERITE_PICKAXE
        };
    }
}

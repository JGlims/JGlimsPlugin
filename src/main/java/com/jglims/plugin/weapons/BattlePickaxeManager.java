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
        double vanillaDamage = getVanillaPickaxeDamage(pickaxeMaterial);
        if (vanillaDamage < 0) return null;
        double battleDamage = vanillaDamage + 1.0;

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

        // Set lore — uniform pattern
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Custom Weapon", NamedTextColor.DARK_PURPLE)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("Attack Damage: ", NamedTextColor.GRAY)
                .append(Component.text(String.format("%.0f", vanillaDamage), NamedTextColor.GREEN))
                .append(Component.text(" +", NamedTextColor.GRAY))
                .append(Component.text("1", NamedTextColor.YELLOW))
                .append(Component.text(" = ", NamedTextColor.GRAY))
                .append(Component.text(String.format("%.0f", battleDamage), NamedTextColor.WHITE))
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Attack Speed: ", NamedTextColor.GRAY)
                .append(Component.text("1.2", NamedTextColor.WHITE))
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Mining enchantments retained", NamedTextColor.GREEN)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("\u25C6 Diamond: Ore Pulse", NamedTextColor.AQUA)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("\u25C6 Netherite: Seismic Resonance", NamedTextColor.DARK_RED)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("Can be upgraded to Super", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        // Attack Damage modifier: damage - 1.0 (player base)
        meta.addAttributeModifier(Attribute.ATTACK_DAMAGE,
                new AttributeModifier(battlePickaxeDamageKey, battleDamage - 1.0,
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

    private double getVanillaPickaxeDamage(Material mat) {
        return switch (mat) {
            case WOODEN_PICKAXE  -> 2.0;
            case STONE_PICKAXE   -> 3.0;
            case IRON_PICKAXE    -> 4.0;
            case GOLDEN_PICKAXE  -> 2.0;
            case DIAMOND_PICKAXE -> 5.0;
            case NETHERITE_PICKAXE -> 6.0;
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

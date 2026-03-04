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
 * BattleSwordManager creates Battle Swords from vanilla swords.
 * Battle Sword = vanilla sword damage + 1, same attack speed (1.6).
 * Required before upgrading to Super.
 */
public class BattleSwordManager {

    private final JGlimsPlugin plugin;
    private final NamespacedKey battleSwordKey;
    private final NamespacedKey battleSwordDamageKey;
    private final NamespacedKey battleSwordSpeedKey;

    public BattleSwordManager(JGlimsPlugin plugin) {
        this.plugin = plugin;
        this.battleSwordKey = new NamespacedKey(plugin, "is_battle_sword");
        this.battleSwordDamageKey = new NamespacedKey(plugin, "battle_sword_damage");
        this.battleSwordSpeedKey = new NamespacedKey(plugin, "battle_sword_speed");
    }

    public NamespacedKey getBattleSwordKey() { return battleSwordKey; }

    public boolean isBattleSword(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(battleSwordKey, PersistentDataType.BYTE);
    }

    /**
     * Creates a Battle Sword from a vanilla sword material.
     * Damage = vanilla sword damage + 1, attack speed = 1.6 (unchanged).
     *
     * Material    | Vanilla | Battle
     * Wood        | 4       | 5
     * Stone       | 5       | 6
     * Iron        | 6       | 7
     * Gold        | 4       | 5
     * Diamond     | 7       | 8
     * Netherite   | 8       | 9
     */
    public ItemStack createBattleSword(Material swordMaterial) {
        double vanillaDamage = getVanillaSwordDamage(swordMaterial);
        if (vanillaDamage < 0) return null;
        double battleDamage = vanillaDamage + 1.0;

        ItemStack battleSword = new ItemStack(swordMaterial, 1);
        ItemMeta meta = battleSword.getItemMeta();
        if (meta == null) return null;

        // Mark as battle sword in PDC
        meta.getPersistentDataContainer().set(battleSwordKey, PersistentDataType.BYTE, (byte) 1);

        // Set display name
        String tierName = getTierName(swordMaterial);
        NamedTextColor nameColor = swordMaterial.name().startsWith("NETHERITE")
                ? NamedTextColor.DARK_RED : NamedTextColor.WHITE;
        meta.displayName(Component.text(tierName + " Battle Sword", nameColor)
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
                .append(Component.text("1.6", NamedTextColor.WHITE))
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("\u25C6 Diamond: Dash Strike", NamedTextColor.AQUA)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("\u25C6 Netherite: Dimensional Cleave", NamedTextColor.DARK_RED)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("Can be upgraded to Super", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        // Attack Damage modifier: damage - 1.0 (player base)
        meta.addAttributeModifier(Attribute.ATTACK_DAMAGE,
                new AttributeModifier(battleSwordDamageKey, battleDamage - 1.0,
                        AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND));

        // Attack Speed: 1.6 = 4.0 - 2.4
        meta.addAttributeModifier(Attribute.ATTACK_SPEED,
                new AttributeModifier(battleSwordSpeedKey, -2.4,
                        AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND));

        // Enchantment glint
        meta.setEnchantmentGlintOverride(true);

        battleSword.setItemMeta(meta);
        return battleSword;
    }

    private double getVanillaSwordDamage(Material mat) {
        return switch (mat) {
            case WOODEN_SWORD  -> 4.0;
            case STONE_SWORD   -> 5.0;
            case IRON_SWORD    -> 6.0;
            case GOLDEN_SWORD  -> 4.0;
            case DIAMOND_SWORD -> 7.0;
            case NETHERITE_SWORD -> 8.0;
            default -> -1;
        };
    }

    public Material getBattleSwordIngredient(Material swordMaterial) {
        return switch (swordMaterial) {
            case WOODEN_SWORD   -> Material.OAK_PLANKS;
            case STONE_SWORD    -> Material.COBBLESTONE;
            case IRON_SWORD     -> Material.IRON_INGOT;
            case GOLDEN_SWORD   -> Material.GOLD_INGOT;
            case DIAMOND_SWORD  -> Material.DIAMOND;
            case NETHERITE_SWORD -> Material.NETHERITE_INGOT;
            default -> null;
        };
    }

    private String getTierName(Material mat) {
        return switch (mat) {
            case WOODEN_SWORD   -> "Wooden";
            case STONE_SWORD    -> "Stone";
            case IRON_SWORD     -> "Iron";
            case GOLDEN_SWORD   -> "Golden";
            case DIAMOND_SWORD  -> "Diamond";
            case NETHERITE_SWORD -> "Netherite";
            default -> "Unknown";
        };
    }

    public Material[] getBattleSwordTiers() {
        return new Material[]{
                Material.WOODEN_SWORD, Material.STONE_SWORD, Material.IRON_SWORD,
                Material.GOLDEN_SWORD, Material.DIAMOND_SWORD, Material.NETHERITE_SWORD
        };
    }
}

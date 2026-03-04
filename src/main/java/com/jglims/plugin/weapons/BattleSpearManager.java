package com.jglims.plugin.weapons;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

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
 * BattleSpearManager creates Battle Spears from vanilla spears.
 * Battle Spear = vanilla spear jab damage + 1, same attack speed.
 * Required before upgrading to Super.
 *
 * Material    | Vanilla Jab | Battle Jab | Attack Speed
 * Wooden      | 1.0         | 2.0        | 1.54
 * Stone       | 2.0         | 3.0        | 1.33
 * Copper      | 2.0         | 3.0        | 1.18
 * Golden      | 2.0         | 3.0        | 1.05
 * Iron        | 3.0         | 4.0        | 1.05
 * Diamond     | 4.0         | 5.0        | 0.95
 * Netherite   | 5.0         | 6.0        | 0.87
 */
public class BattleSpearManager {

    private final JGlimsPlugin plugin;
    private final NamespacedKey battleSpearKey;
    private final NamespacedKey battleSpearDamageKey;
    private final NamespacedKey battleSpearSpeedKey;

    // All spear materials for quick lookups
    private static final Set<Material> SPEAR_MATERIALS = EnumSet.of(
            Material.WOODEN_SPEAR, Material.STONE_SPEAR, Material.COPPER_SPEAR,
            Material.GOLDEN_SPEAR, Material.IRON_SPEAR, Material.DIAMOND_SPEAR,
            Material.NETHERITE_SPEAR
    );

    public BattleSpearManager(JGlimsPlugin plugin) {
        this.plugin = plugin;
        this.battleSpearKey = new NamespacedKey(plugin, "is_battle_spear");
        this.battleSpearDamageKey = new NamespacedKey(plugin, "battle_spear_damage");
        this.battleSpearSpeedKey = new NamespacedKey(plugin, "battle_spear_speed");
    }

    public NamespacedKey getBattleSpearKey() { return battleSpearKey; }

    public boolean isBattleSpear(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(battleSpearKey, PersistentDataType.BYTE);
    }

    public static boolean isSpearMaterial(Material mat) {
        return SPEAR_MATERIALS.contains(mat);
    }

    /**
     * Creates a Battle Spear from a vanilla spear material.
     */
    public ItemStack createBattleSpear(Material spearMaterial) {
        double damage = getBattleSpearDamage(spearMaterial);
        if (damage < 0) return null;
        double attackSpeed = getSpearAttackSpeed(spearMaterial);

        ItemStack battleSpear = new ItemStack(spearMaterial, 1);
        ItemMeta meta = battleSpear.getItemMeta();
        if (meta == null) return null;

        // Mark as battle spear in PDC
        meta.getPersistentDataContainer().set(battleSpearKey, PersistentDataType.BYTE, (byte) 1);

        // Set display name
        String tierName = getTierName(spearMaterial);
        NamedTextColor nameColor = spearMaterial.name().startsWith("NETHERITE")
                ? NamedTextColor.DARK_RED : NamedTextColor.WHITE;
        meta.displayName(Component.text(tierName + " Battle Spear", nameColor)
                .decoration(TextDecoration.ITALIC, false));

        // Set lore
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Custom Weapon", NamedTextColor.DARK_PURPLE)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("+" + String.format("%.1f", damage) + " Attack Damage", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text(String.format("%.2f", attackSpeed) + " Attack Speed", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Can be upgraded to Super", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        // Attack Damage modifier: damage - 1.0 (player base)
        meta.addAttributeModifier(Attribute.ATTACK_DAMAGE,
                new AttributeModifier(battleSpearDamageKey, damage - 1.0,
                        AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND));

        // Attack Speed modifier: speed - 4.0
        meta.addAttributeModifier(Attribute.ATTACK_SPEED,
                new AttributeModifier(battleSpearSpeedKey, attackSpeed - 4.0,
                        AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND));

        // Enchantment glint
        meta.setEnchantmentGlintOverride(true);

        battleSpear.setItemMeta(meta);
        return battleSpear;
    }

    private double getBattleSpearDamage(Material mat) {
        return switch (mat) {
            case WOODEN_SPEAR    -> 2.0;  // 1 + 1
            case STONE_SPEAR     -> 3.0;  // 2 + 1
            case COPPER_SPEAR    -> 3.0;  // 2 + 1
            case GOLDEN_SPEAR    -> 3.0;  // 2 + 1
            case IRON_SPEAR      -> 4.0;  // 3 + 1
            case DIAMOND_SPEAR   -> 5.0;  // 4 + 1
            case NETHERITE_SPEAR -> 6.0;  // 5 + 1
            default -> -1;
        };
    }

    private double getSpearAttackSpeed(Material mat) {
        return switch (mat) {
            case WOODEN_SPEAR    -> 1.54;
            case STONE_SPEAR     -> 1.33;
            case COPPER_SPEAR    -> 1.18;
            case GOLDEN_SPEAR    -> 1.05;
            case IRON_SPEAR      -> 1.05;
            case DIAMOND_SPEAR   -> 0.95;
            case NETHERITE_SPEAR -> 0.87;
            default -> 1.0;
        };
    }

    public Material getBattleSpearIngredient(Material spearMaterial) {
        return switch (spearMaterial) {
            case WOODEN_SPEAR    -> Material.OAK_PLANKS;
            case STONE_SPEAR     -> Material.COBBLESTONE;
            case COPPER_SPEAR    -> Material.COPPER_INGOT;
            case GOLDEN_SPEAR    -> Material.GOLD_INGOT;
            case IRON_SPEAR      -> Material.IRON_INGOT;
            case DIAMOND_SPEAR   -> Material.DIAMOND;
            case NETHERITE_SPEAR -> Material.NETHERITE_INGOT;
            default -> null;
        };
    }

    private String getTierName(Material mat) {
        return switch (mat) {
            case WOODEN_SPEAR    -> "Wooden";
            case STONE_SPEAR     -> "Stone";
            case COPPER_SPEAR    -> "Copper";
            case GOLDEN_SPEAR    -> "Golden";
            case IRON_SPEAR      -> "Iron";
            case DIAMOND_SPEAR   -> "Diamond";
            case NETHERITE_SPEAR -> "Netherite";
            default -> "Unknown";
        };
    }

    public Material[] getBattleSpearTiers() {
        return new Material[]{
                Material.WOODEN_SPEAR, Material.STONE_SPEAR, Material.COPPER_SPEAR,
                Material.GOLDEN_SPEAR, Material.IRON_SPEAR, Material.DIAMOND_SPEAR,
                Material.NETHERITE_SPEAR
        };
    }
}

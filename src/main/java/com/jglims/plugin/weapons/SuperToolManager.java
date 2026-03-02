package com.jglims.plugin.weapons;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import com.jglims.plugin.JGlimsPlugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class SuperToolManager implements Listener {

    private final JGlimsPlugin plugin;
    private final NamespacedKey superToolKey;
    private final NamespacedKey superDamageKey;

    public SuperToolManager(JGlimsPlugin plugin) {
        this.plugin = plugin;
        this.superToolKey = new NamespacedKey(plugin, "is_super_tool");
        this.superDamageKey = new NamespacedKey(plugin, "super_damage");
    }

    public NamespacedKey getSuperToolKey() {
        return superToolKey;
    }

    public boolean isSuperTool(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(superToolKey, PersistentDataType.BYTE);
    }

    /**
     * Creates a Super Tool from the given base tool.
     *
     * Non-netherite: +2 attack damage, "Super" prefix in aqua.
     * Netherite: +1 attack damage, "Super" prefix in dark red.
     * Super Elytra: doubled durability, "Super Elytra" in gold.
     *
     * Preserves all existing enchantments and custom enchantments.
     */
    public ItemStack createSuperTool(ItemStack baseTool) {
        if (baseTool == null || baseTool.getType() == Material.AIR) return null;

        Material mat = baseTool.getType();
        ItemStack superTool = baseTool.clone();
        ItemMeta meta = superTool.getItemMeta();
        if (meta == null) return null;

        // Mark as super tool in PDC
        meta.getPersistentDataContainer().set(superToolKey, PersistentDataType.BYTE, (byte) 1);

        boolean isNetherite = mat.name().startsWith("NETHERITE");
        boolean isElytra = mat == Material.ELYTRA;

        // Set display name
        if (isElytra) {
            meta.displayName(Component.text("Super Elytra", NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));
        } else {
            String baseName = getToolBaseName(mat);
            NamedTextColor color = isNetherite ? NamedTextColor.DARK_RED : NamedTextColor.AQUA;
            meta.displayName(Component.text("Super " + baseName, color)
                .decoration(TextDecoration.ITALIC, false));
        }

        // Set lore
        List<Component> existingLore = meta.lore() != null ? new ArrayList<>(meta.lore()) : new ArrayList<>();

        if (isElytra) {
            existingLore.add(0, Component.text("Doubled Durability", NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));
            // Double the max durability by setting the damage to negative (repair)
            // Actually, we can't change max durability directly in Bukkit API.
            // Instead, we'll use Unbreaking-like logic or just mark it in PDC
            // and handle durability reduction in an event listener.
            meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "super_elytra_durability"), PersistentDataType.BYTE, (byte) 1);
        } else {
            double bonusDmg = isNetherite ? 1.0 : 2.0;
            existingLore.add(0, Component.text("Super Tool", NamedTextColor.AQUA)
                .decoration(TextDecoration.ITALIC, false));
            existingLore.add(1, Component.text("+" + (int) bonusDmg + " Bonus Attack Damage", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));

            // Add damage modifier
            meta.addAttributeModifier(Attribute.ATTACK_DAMAGE,
                new AttributeModifier(superDamageKey, bonusDmg,
                    AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND));
        }

        meta.lore(existingLore);
        superTool.setItemMeta(meta);
        return superTool;
    }

    /**
     * Returns the display name for a tool type (e.g., "Diamond Sword", "Iron Pickaxe").
     */
    private String getToolBaseName(Material mat) {
        String name = mat.name().replace('_', ' ');
        StringBuilder sb = new StringBuilder();
        boolean capitalize = true;
        for (char c : name.toCharArray()) {
            if (c == ' ') {
                sb.append(' ');
                capitalize = true;
            } else if (capitalize) {
                sb.append(Character.toUpperCase(c));
                capitalize = false;
            } else {
                sb.append(Character.toLowerCase(c));
            }
        }
        return sb.toString();
    }

    /**
     * Returns true if this material can be made into a super tool.
     */
    public boolean canBeSuperTool(Material mat) {
        String name = mat.name();
        return name.endsWith("_SWORD") || name.endsWith("_PICKAXE") || name.endsWith("_AXE")
            || name.endsWith("_SHOVEL") || name.endsWith("_HOE")
            || mat == Material.ELYTRA || mat == Material.TRIDENT
            || mat == Material.BOW || mat == Material.CROSSBOW
            || mat == Material.SHIELD;
    }

    /**
     * Returns the ingredient material for the super tool recipe edges.
     * Non-netherite: same material as the tool tier (8x surrounding).
     * Netherite: 4x Diamond corners + 4x Netherite Ingot edges.
     * Elytra: 8x Diamond.
     */
    public Material getSuperIngredient(Material toolMaterial) {
        String name = toolMaterial.name();
        if (name.startsWith("WOODEN_")) return Material.OAK_PLANKS;
        if (name.startsWith("STONE_")) return Material.COBBLESTONE;
        if (name.startsWith("IRON_")) return Material.IRON_INGOT;
        if (name.startsWith("GOLDEN_")) return Material.GOLD_INGOT;
        if (name.startsWith("DIAMOND_")) return Material.DIAMOND;
        if (name.startsWith("NETHERITE_")) return null; // Special recipe
        if (toolMaterial == Material.ELYTRA) return Material.DIAMOND;
        if (toolMaterial == Material.TRIDENT) return Material.DIAMOND;
        if (toolMaterial == Material.BOW) return Material.DIAMOND;
        if (toolMaterial == Material.CROSSBOW) return Material.DIAMOND;
        if (toolMaterial == Material.SHIELD) return Material.DIAMOND;
        return null;
    }

    /**
     * Super Elytra durability handler — 50% chance to negate durability loss.
     * Combined with Glider enchant, this could stack (both roll independently).
     * This is checked in EnchantmentEffectListener's PlayerItemDamageEvent.
     * We expose a check method here.
     */
    public boolean hasSuperElytraDurability(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer()
            .has(new NamespacedKey(plugin, "super_elytra_durability"), PersistentDataType.BYTE);
    }
}

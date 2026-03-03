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
    private final NamespacedKey superSpeedKey;

    public SuperToolManager(JGlimsPlugin plugin) {
        this.plugin = plugin;
        this.superToolKey = new NamespacedKey(plugin, "is_super_tool");
        this.superDamageKey = new NamespacedKey(plugin, "super_damage");
        this.superSpeedKey = new NamespacedKey(plugin, "super_speed");
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
     * BUG 1 FIX: When adding an ATTACK_DAMAGE attribute modifier, we must
     * also include the base weapon damage explicitly, because Bukkit replaces
     * the implicit vanilla damage when any modifier is added.
     * The modifier value = (vanillaBaseDamage + bonusDmg - 1.0)
     * because the player has a base attack damage of 1.0.
     * We also need to preserve the vanilla attack speed.
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
            existingLore.add(0, Component.text("Super Elytra", NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));
            existingLore.add(1, Component.text("50% chance to negate durability loss", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
            meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "super_elytra_durability"), PersistentDataType.BYTE, (byte) 1);
        } else {
            double bonusDmg = isNetherite ? 1.0 : 2.0;
            double vanillaBaseDamage = getVanillaBaseDamage(mat);
            double vanillaAttackSpeed = getVanillaAttackSpeed(mat);

            existingLore.add(0, Component.text("Super Tool", NamedTextColor.AQUA)
                .decoration(TextDecoration.ITALIC, false));
            existingLore.add(1, Component.text("+" + (int) bonusDmg + " Bonus Attack Damage", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));

            // BUG 1 FIX: Include base weapon damage + bonus in the modifier.
            // Player base attack = 1.0, so modifier = vanillaBaseDamage + bonusDmg - 1.0
            // This makes the total = 1.0 (player) + modifier = vanillaBaseDamage + bonusDmg
            if (vanillaBaseDamage > 0) {
                double totalModifier = vanillaBaseDamage + bonusDmg - 1.0;
                meta.addAttributeModifier(Attribute.ATTACK_DAMAGE,
                    new AttributeModifier(superDamageKey, totalModifier,
                        AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND));

                // Also preserve the vanilla attack speed
                // Player base attack speed = 4.0, so modifier = vanillaAttackSpeed - 4.0
                double speedModifier = vanillaAttackSpeed - 4.0;
                meta.addAttributeModifier(Attribute.ATTACK_SPEED,
                    new AttributeModifier(superSpeedKey, speedModifier,
                        AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND));
            }
            // For items without vanilla damage (bow, crossbow, shield), skip the modifier
        }

        meta.lore(existingLore);
        superTool.setItemMeta(meta);
        return superTool;
    }

    /**
     * Returns the vanilla base attack damage for a tool/weapon material.
     * These are the values shown in-game (total damage dealt = this value).
     * Returns 0 for items that don't have attack damage (bow, crossbow, etc).
     */
    private double getVanillaBaseDamage(Material mat) {
        return switch (mat) {
            // Swords
            case WOODEN_SWORD -> 4.0;
            case STONE_SWORD -> 5.0;
            case IRON_SWORD -> 6.0;
            case GOLDEN_SWORD -> 4.0;
            case DIAMOND_SWORD -> 7.0;
            case NETHERITE_SWORD -> 8.0;
            // Axes
            case WOODEN_AXE -> 7.0;
            case STONE_AXE -> 9.0;
            case IRON_AXE -> 9.0;
            case GOLDEN_AXE -> 7.0;
            case DIAMOND_AXE -> 9.0;
            case NETHERITE_AXE -> 10.0;
            // Pickaxes
            case WOODEN_PICKAXE -> 2.0;
            case STONE_PICKAXE -> 3.0;
            case IRON_PICKAXE -> 4.0;
            case GOLDEN_PICKAXE -> 2.0;
            case DIAMOND_PICKAXE -> 5.0;
            case NETHERITE_PICKAXE -> 6.0;
            // Shovels
            case WOODEN_SHOVEL -> 2.5;
            case STONE_SHOVEL -> 3.5;
            case IRON_SHOVEL -> 4.5;
            case GOLDEN_SHOVEL -> 2.5;
            case DIAMOND_SHOVEL -> 5.5;
            case NETHERITE_SHOVEL -> 6.5;
            // Hoes
            case WOODEN_HOE -> 1.0;
            case STONE_HOE -> 1.0;
            case IRON_HOE -> 1.0;
            case GOLDEN_HOE -> 1.0;
            case DIAMOND_HOE -> 1.0;
            case NETHERITE_HOE -> 1.0;
            // Trident
            case TRIDENT -> 9.0;
            // Non-melee items: no base attack damage modifier needed
            default -> 0;
        };
    }

    /**
     * Returns the vanilla attack speed for a tool/weapon material.
     * Player base attack speed is 4.0.
     */
    private double getVanillaAttackSpeed(Material mat) {
        return switch (mat) {
            // Swords: 1.6
            case WOODEN_SWORD, STONE_SWORD, IRON_SWORD, GOLDEN_SWORD,
                 DIAMOND_SWORD, NETHERITE_SWORD -> 1.6;
            // Axes: varies
            case WOODEN_AXE, STONE_AXE -> 0.8;
            case IRON_AXE -> 0.9;
            case GOLDEN_AXE -> 1.0;
            case DIAMOND_AXE -> 1.0;
            case NETHERITE_AXE -> 1.0;
            // Pickaxes: 1.2
            case WOODEN_PICKAXE, STONE_PICKAXE, IRON_PICKAXE, GOLDEN_PICKAXE,
                 DIAMOND_PICKAXE, NETHERITE_PICKAXE -> 1.2;
            // Shovels: 1.0
            case WOODEN_SHOVEL, STONE_SHOVEL, IRON_SHOVEL, GOLDEN_SHOVEL,
                 DIAMOND_SHOVEL, NETHERITE_SHOVEL -> 1.0;
            // Hoes: varies
            case WOODEN_HOE, GOLDEN_HOE -> 1.0;
            case STONE_HOE -> 2.0;
            case IRON_HOE -> 3.0;
            case DIAMOND_HOE, NETHERITE_HOE -> 4.0;
            // Trident: 1.1
            case TRIDENT -> 1.1;
            // Default
            default -> 4.0;
        };
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
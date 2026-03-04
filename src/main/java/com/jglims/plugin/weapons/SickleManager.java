package com.jglims.plugin.weapons;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import com.jglims.plugin.JGlimsPlugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class SickleManager implements Listener {

    private final JGlimsPlugin plugin;
    private final NamespacedKey sickleKey;
    private final NamespacedKey sickleDamageKey;
    private final NamespacedKey sickleSpeedKey;

    public SickleManager(JGlimsPlugin plugin) {
        this.plugin = plugin;
        this.sickleKey = new NamespacedKey(plugin, "is_sickle");
        this.sickleDamageKey = new NamespacedKey(plugin, "sickle_damage");
        this.sickleSpeedKey = new NamespacedKey(plugin, "sickle_speed");
    }

    public NamespacedKey getSickleKey() {
        return sickleKey;
    }

    public boolean isSickle(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(sickleKey, PersistentDataType.BYTE);
    }

    /**
     * Creates a sickle from a base hoe material.
     * Sickle = equivalent sword damage + 1, attack speed 1.1.
     *
     * Material    | Sword Dmg | Sickle Dmg | Attack Speed
     * Wood        | 4         | 5          | 1.1
     * Stone       | 5         | 6          | 1.1
     * Iron        | 6         | 7          | 1.1
     * Gold        | 4         | 5          | 1.1
     * Diamond     | 7         | 8          | 1.1
     * Netherite   | 8         | 9          | 1.1
     */
    public ItemStack createSickle(Material hoeMaterial) {
        double swordDamage = getEquivalentSwordDamage(hoeMaterial);
        if (swordDamage < 0) return null;
        double sickleDamage = swordDamage + 1.0;

        ItemStack sickle = new ItemStack(hoeMaterial, 1);
        ItemMeta meta = sickle.getItemMeta();
        if (meta == null) return null;

        // Mark as sickle in PDC
        meta.getPersistentDataContainer().set(sickleKey, PersistentDataType.BYTE, (byte) 1);

        // Set display name
        String tierName = getTierName(hoeMaterial);
        NamedTextColor nameColor = hoeMaterial == Material.NETHERITE_HOE
            ? NamedTextColor.DARK_RED : NamedTextColor.WHITE;
        meta.displayName(Component.text(tierName + " Sickle", nameColor)
            .decoration(TextDecoration.ITALIC, false));

        // Set lore — uniform pattern
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Custom Weapon", NamedTextColor.DARK_PURPLE)
            .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("Attack Damage: ", NamedTextColor.GRAY)
            .append(Component.text(String.format("%.0f", swordDamage), NamedTextColor.GREEN))
            .append(Component.text(" +", NamedTextColor.GRAY))
            .append(Component.text("1", NamedTextColor.YELLOW))
            .append(Component.text(" = ", NamedTextColor.GRAY))
            .append(Component.text(String.format("%.0f", sickleDamage), NamedTextColor.WHITE))
            .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Attack Speed: ", NamedTextColor.GRAY)
            .append(Component.text("1.1", NamedTextColor.WHITE))
            .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Tilling disabled", NamedTextColor.RED)
            .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("\u25C6 Diamond: Harvest Storm", NamedTextColor.AQUA)
            .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("\u25C6 Netherite: Reaper's Scythe", NamedTextColor.DARK_RED)
            .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("Can be upgraded to Super", NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);

        // Remove default attribute display and set custom attributes
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        // Attack Damage: modifier = damage - 1.0
        meta.addAttributeModifier(Attribute.ATTACK_DAMAGE,
            new AttributeModifier(sickleDamageKey, sickleDamage - 1.0,
                AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND));

        // Attack Speed: 1.1 = 4.0 - 2.9
        meta.addAttributeModifier(Attribute.ATTACK_SPEED,
            new AttributeModifier(sickleSpeedKey, -2.9,
                AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND));

        // Enchantment glint
        meta.setEnchantmentGlintOverride(true);

        sickle.setItemMeta(meta);
        return sickle;
    }

    /**
     * Returns the equivalent sword damage for computing sickle damage.
     * Sickle damage = sword damage + 1
     */
    private double getEquivalentSwordDamage(Material mat) {
        return switch (mat) {
            case WOODEN_HOE -> 4.0;
            case STONE_HOE -> 5.0;
            case IRON_HOE -> 6.0;
            case GOLDEN_HOE -> 4.0;
            case DIAMOND_HOE -> 7.0;
            case NETHERITE_HOE -> 8.0;
            default -> -1;
        };
    }

    /**
     * Returns the ingredient material used in the crafting recipe.
     */
    public Material getSickleIngredient(Material hoeMaterial) {
        return switch (hoeMaterial) {
            case WOODEN_HOE -> Material.OAK_PLANKS;
            case STONE_HOE -> Material.COBBLESTONE;
            case IRON_HOE -> Material.IRON_INGOT;
            case GOLDEN_HOE -> Material.GOLD_INGOT;
            case DIAMOND_HOE -> Material.DIAMOND;
            case NETHERITE_HOE -> Material.NETHERITE_INGOT;
            default -> null;
        };
    }

    private String getTierName(Material mat) {
        return switch (mat) {
            case WOODEN_HOE -> "Wooden";
            case STONE_HOE -> "Stone";
            case IRON_HOE -> "Iron";
            case GOLDEN_HOE -> "Golden";
            case DIAMOND_HOE -> "Diamond";
            case NETHERITE_HOE -> "Netherite";
            default -> "Unknown";
        };
    }

    /**
     * Returns all hoe materials that have sickle variants.
     */
    public Material[] getSickleTiers() {
        return new Material[]{
            Material.WOODEN_HOE, Material.STONE_HOE, Material.IRON_HOE,
            Material.GOLDEN_HOE, Material.DIAMOND_HOE, Material.NETHERITE_HOE
        };
    }

    // Prevent sickles from tilling soil
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        ItemStack item = event.getItem();
        if (item == null) return;
        if (!isSickle(item)) return;

        if (event.getClickedBlock() != null) {
            Material blockType = event.getClickedBlock().getType();
            if (blockType == Material.DIRT || blockType == Material.GRASS_BLOCK
                || blockType == Material.DIRT_PATH || blockType == Material.COARSE_DIRT
                || blockType == Material.ROOTED_DIRT || blockType == Material.FARMLAND
                || blockType == Material.MOSS_BLOCK || blockType == Material.MUD
                || blockType == Material.MYCELIUM || blockType == Material.PODZOL) {
                event.setCancelled(true);
            }
        }
    }
}

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

public class BattleAxeManager implements Listener {

    private final JGlimsPlugin plugin;
    private final NamespacedKey battleAxeKey;
    private final NamespacedKey battleAxeDamageKey;
    private final NamespacedKey battleAxeSpeedKey;

    public BattleAxeManager(JGlimsPlugin plugin) {
        this.plugin = plugin;
        this.battleAxeKey = new NamespacedKey(plugin, "is_battle_axe");
        this.battleAxeDamageKey = new NamespacedKey(plugin, "battle_axe_damage");
        this.battleAxeSpeedKey = new NamespacedKey(plugin, "battle_axe_speed");
    }

    public NamespacedKey getBattleAxeKey() {
        return battleAxeKey;
    }

    public boolean isBattleAxe(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(battleAxeKey, PersistentDataType.BYTE);
    }

    /**
     * Creates a Battle Axe from a base axe material.
     * Battle Axe = sickle damage + 1, attack speed = 0.9
     */
    public ItemStack createBattleAxe(Material axeMaterial) {
        double damage = getBattleAxeDamage(axeMaterial);
        if (damage < 0) return null;

        ItemStack battleAxe = new ItemStack(axeMaterial, 1);
        ItemMeta meta = battleAxe.getItemMeta();
        if (meta == null) return null;

        // Mark as battle axe in PDC
        meta.getPersistentDataContainer().set(battleAxeKey, PersistentDataType.BYTE, (byte) 1);

        // Set display name
        String tierName = getTierName(axeMaterial);
        NamedTextColor nameColor = axeMaterial.name().startsWith("NETHERITE")
            ? NamedTextColor.DARK_RED : NamedTextColor.WHITE;
        meta.displayName(Component.text(tierName + " Battle Axe", nameColor)
            .decoration(TextDecoration.ITALIC, false));

        // Set lore
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Custom Weapon", NamedTextColor.DARK_PURPLE)
            .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("+" + String.format("%.1f", damage) + " Attack Damage", NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("0.9 Attack Speed", NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        // Attack Damage modifier = damage - 1.0 (player base)
        meta.addAttributeModifier(Attribute.ATTACK_DAMAGE,
            new AttributeModifier(battleAxeDamageKey, damage - 1.0,
                AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND));

        // Attack Speed: 0.9 = 4.0 - 3.1
        meta.addAttributeModifier(Attribute.ATTACK_SPEED,
            new AttributeModifier(battleAxeSpeedKey, -3.1,
                AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND));

        battleAxe.setItemMeta(meta);
        return battleAxe;
    }

    private double getBattleAxeDamage(Material mat) {
        return switch (mat) {
            case WOODEN_AXE -> 6.0;     // Sickle 5 + 1
            case STONE_AXE -> 7.0;      // Sickle 6 + 1
            case IRON_AXE -> 8.0;       // Sickle 7 + 1
            case GOLDEN_AXE -> 6.0;     // Sickle 5 + 1
            case DIAMOND_AXE -> 9.0;    // Sickle 8 + 1
            case NETHERITE_AXE -> 10.0;  // Sickle 9 + 1
            default -> -1;
        };
    }

    public Material getBattleAxeIngredient(Material axeMaterial) {
        return switch (axeMaterial) {
            case WOODEN_AXE -> Material.OAK_PLANKS;
            case STONE_AXE -> Material.COBBLESTONE;
            case IRON_AXE -> Material.IRON_INGOT;
            case GOLDEN_AXE -> Material.GOLD_INGOT;
            case DIAMOND_AXE -> Material.DIAMOND;
            case NETHERITE_AXE -> Material.NETHERITE_INGOT;
            default -> null;
        };
    }

    public Material getBattleAxeBlock(Material axeMaterial) {
        return switch (axeMaterial) {
            case WOODEN_AXE -> Material.OAK_LOG;
            case STONE_AXE -> Material.COBBLESTONE;
            case IRON_AXE -> Material.IRON_BLOCK;
            case GOLDEN_AXE -> Material.GOLD_BLOCK;
            case DIAMOND_AXE -> Material.DIAMOND_BLOCK;
            case NETHERITE_AXE -> Material.NETHERITE_INGOT; // Special recipe
            default -> null;
        };
    }

    private String getTierName(Material mat) {
        return switch (mat) {
            case WOODEN_AXE -> "Wooden";
            case STONE_AXE -> "Stone";
            case IRON_AXE -> "Iron";
            case GOLDEN_AXE -> "Golden";
            case DIAMOND_AXE -> "Diamond";
            case NETHERITE_AXE -> "Netherite";
            default -> "Unknown";
        };
    }

    public Material[] getBattleAxeTiers() {
        return new Material[]{
            Material.WOODEN_AXE, Material.STONE_AXE, Material.IRON_AXE,
            Material.GOLDEN_AXE, Material.DIAMOND_AXE, Material.NETHERITE_AXE
        };
    }

    // Prevent Battle Axes from stripping logs
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        ItemStack item = event.getItem();
        if (item == null) return;
        if (!isBattleAxe(item)) return;

        if (event.getClickedBlock() != null) {
            Material blockType = event.getClickedBlock().getType();
            String name = blockType.name();
            // Block stripping logs, scraping copper, etc.
            if (name.endsWith("_LOG") || name.endsWith("_WOOD")
                || name.endsWith("_STEM") || name.contains("COPPER")
                || blockType == Material.PUMPKIN) {
                event.setCancelled(true);
            }
        }
    }
}

package com.jglims.plugin.utility;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.jglims.plugin.JGlimsPlugin;

public class InventorySortListener implements Listener {

    private final JGlimsPlugin plugin;

    public InventorySortListener(JGlimsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!plugin.getConfigManager().isInventorySortEnabled()) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!event.isShiftClick()) return;

        // Must click on an empty slot
        ItemStack clicked = event.getCurrentItem();
        if (clicked != null && clicked.getType() != Material.AIR) return;

        Inventory inv = event.getClickedInventory();
        if (inv == null) return;

        // Only sort container inventories (chests, barrels, shulkers, etc.) and player inventory
        InventoryType type = inv.getType();
        if (type != InventoryType.CHEST && type != InventoryType.BARREL
            && type != InventoryType.SHULKER_BOX && type != InventoryType.ENDER_CHEST
            && type != InventoryType.PLAYER && type != InventoryType.DISPENSER
            && type != InventoryType.DROPPER && type != InventoryType.HOPPER) return;

        event.setCancelled(true);
        sortInventory(inv);
    }

    private void sortInventory(Inventory inv) {
        // Collect all non-null, non-air items
        List<ItemStack> items = new ArrayList<>();
        int size = inv.getSize();

        // For player inventory, only sort the main inventory (slots 9-35), not hotbar or armor
        int startSlot = 0;
        int endSlot = size;
        if (inv.getType() == InventoryType.PLAYER) {
            startSlot = 9;
            endSlot = 36;
        }

        for (int i = startSlot; i < endSlot; i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                items.add(item);
                inv.setItem(i, null);
            }
        }

        // Sort by category, then alphabetically
        items.sort(Comparator.comparingInt((ItemStack item) -> getCategoryIndex(item.getType()))
            .thenComparing(item -> item.getType().name()));

        // Place sorted items back
        int slot = startSlot;
        for (ItemStack item : items) {
            if (slot >= endSlot) break;
            inv.setItem(slot, item);
            slot++;
        }
    }

    /**
     * Category indices (lower = sorted first):
     * 0 = Weapons, 1 = Tools, 2 = Armor, 3 = Blocks, 4 = Food,
     * 5 = Potions, 6 = Enchanted Books, 7 = Materials, 8 = Misc
     */
    private int getCategoryIndex(Material mat) {
        String name = mat.name();

        // Weapons (including spears and mace)
        if (name.endsWith("_SWORD") || name.endsWith("_SPEAR")
            || mat == Material.BOW || mat == Material.CROSSBOW
            || mat == Material.TRIDENT || mat == Material.MACE
            || name.endsWith("_AXE")) return 0;

        // Tools
        if (name.endsWith("_PICKAXE") || name.endsWith("_SHOVEL") || name.endsWith("_HOE")
            || mat == Material.FISHING_ROD || mat == Material.SHEARS
            || mat == Material.FLINT_AND_STEEL || mat == Material.SPYGLASS
            || mat == Material.COMPASS || mat == Material.CLOCK) return 1;

        // Armor
        if (name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE")
            || name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS")
            || mat == Material.ELYTRA || mat == Material.SHIELD
            || mat == Material.WOLF_ARMOR) return 2;

        // Blocks
        if (mat.isBlock()) return 3;

        // Food
        if (mat.isEdible()) return 4;

        // Potions
        if (mat == Material.POTION || mat == Material.SPLASH_POTION
            || mat == Material.LINGERING_POTION || mat == Material.TIPPED_ARROW) return 5;

        // Enchanted Books
        if (mat == Material.ENCHANTED_BOOK) return 6;

        // Common materials
        if (mat == Material.IRON_INGOT || mat == Material.GOLD_INGOT || mat == Material.DIAMOND
            || mat == Material.EMERALD || mat == Material.NETHERITE_INGOT
            || mat == Material.COPPER_INGOT || mat == Material.LAPIS_LAZULI
            || mat == Material.REDSTONE || mat == Material.COAL
            || mat == Material.STICK || mat == Material.STRING
            || mat == Material.LEATHER || mat == Material.PAPER
            || mat == Material.BONE || mat == Material.GUNPOWDER
            || mat == Material.BLAZE_ROD || mat == Material.ENDER_PEARL
            || mat == Material.GHAST_TEAR || mat == Material.SLIME_BALL
            || name.endsWith("_DYE") || name.endsWith("_INGOT")
            || name.endsWith("_NUGGET")) return 7;

        return 8; // Misc
    }
}

package com.jglims.plugin.utility;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.enchantments.CustomEnchantManager;
import com.jglims.plugin.enchantments.EnchantmentType;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.view.AnvilView;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EnchantTransferListener implements Listener {

    private final JGlimsPlugin plugin;
    private final CustomEnchantManager enchantManager;

    public EnchantTransferListener(JGlimsPlugin plugin, CustomEnchantManager enchantManager) {
        this.plugin = plugin;
        this.enchantManager = enchantManager;
    }

    // ========================================================================
    // ENCHANTMENT TRANSFER: Tool (slot 1) + Book (slot 2) in anvil
    // Result: Enchanted Book with ALL enchants from the tool. Tool consumed. 0 XP.
    // Uses AnvilView for setRepairCost()
    // ========================================================================
    @EventHandler(priority = EventPriority.LOW)
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        if (!plugin.getConfigManager().isEnchantTransferEnabled()) return;

        AnvilInventory inv = event.getInventory();
        AnvilView anvilView = (AnvilView) event.getView();

        ItemStack firstSlot = inv.getItem(0);
        ItemStack secondSlot = inv.getItem(1);

        if (firstSlot == null || secondSlot == null) return;

        // First slot must be a tool/weapon (not a book), second slot must be a plain book
        if (firstSlot.getType() == Material.ENCHANTED_BOOK || firstSlot.getType() == Material.BOOK) return;
        if (secondSlot.getType() != Material.BOOK) return;

        // Check if the tool has any enchantments (vanilla or custom)
        Map<Enchantment, Integer> vanillaEnchants = firstSlot.getEnchantments();
        Map<EnchantmentType, Integer> customEnchants = enchantManager.getAllCustomEnchants(firstSlot);

        if (vanillaEnchants.isEmpty() && customEnchants.isEmpty()) return;

        // Create result: Enchanted Book with all enchants
        ItemStack result = new ItemStack(Material.ENCHANTED_BOOK, 1);
        EnchantmentStorageMeta resultMeta = (EnchantmentStorageMeta) result.getItemMeta();
        if (resultMeta == null) return;

        // Transfer vanilla enchantments
        for (Map.Entry<Enchantment, Integer> entry : vanillaEnchants.entrySet()) {
            resultMeta.addStoredEnchant(entry.getKey(), entry.getValue(), true);
        }

        // Transfer custom enchantments via PDC
        PersistentDataContainer resultPdc = resultMeta.getPersistentDataContainer();
        for (Map.Entry<EnchantmentType, Integer> entry : customEnchants.entrySet()) {
            resultPdc.set(enchantManager.getKey(entry.getKey()),
                PersistentDataType.INTEGER, entry.getValue());
        }

        // Build lore showing all enchantments
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Transferred Enchantments:", NamedTextColor.GOLD)
            .decoration(TextDecoration.ITALIC, false));

        for (Map.Entry<Enchantment, Integer> entry : vanillaEnchants.entrySet()) {
            String name = formatEnchantName(entry.getKey().getKey().getKey());
            lore.add(Component.text(" " + name + " " + toRoman(entry.getValue()), NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        }

        for (Map.Entry<EnchantmentType, Integer> entry : customEnchants.entrySet()) {
            String name = formatEnchantName(entry.getKey().name());
            lore.add(Component.text(" " + name + " " + toRoman(entry.getValue()), NamedTextColor.AQUA)
                .decoration(TextDecoration.ITALIC, false));
        }

        resultMeta.displayName(Component.text("Enchanted Book", NamedTextColor.YELLOW)
            .decoration(TextDecoration.ITALIC, false));
        resultMeta.lore(lore);
        result.setItemMeta(resultMeta);

        event.setResult(result);
        anvilView.setRepairCost(0);
    }

    // Consume both inputs when result is taken
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory() instanceof AnvilInventory inv)) return;
        if (event.getSlotType() != InventoryType.SlotType.RESULT) return;
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
        if (!(event.getWhoClicked() instanceof Player)) return;

        ItemStack firstSlot = inv.getItem(0);
        ItemStack secondSlot = inv.getItem(1);
        if (firstSlot == null || secondSlot == null) return;

        // Only handle our transfer operation (tool + plain book)
        if (firstSlot.getType() == Material.ENCHANTED_BOOK || firstSlot.getType() == Material.BOOK) return;
        if (secondSlot.getType() != Material.BOOK) return;

        // Check result is an enchanted book with our lore
        ItemStack result = event.getCurrentItem();
        if (result.getType() != Material.ENCHANTED_BOOK) return;
        if (!result.hasItemMeta() || result.getItemMeta().lore() == null) return;

        // Consume both slots
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            inv.setItem(0, null);
            inv.setItem(1, null);
        });
    }

    // ========================================================================
    // UTILITY
    // ========================================================================
    private String formatEnchantName(String raw) {
        String name = raw.replace('_', ' ');
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

    private String toRoman(int level) {
        return switch (level) {
            case 1 -> "I"; case 2 -> "II"; case 3 -> "III";
            case 4 -> "IV"; case 5 -> "V";
            default -> String.valueOf(level);
        };
    }
}

package com.jglims.plugin.enchantments;

import com.jglims.plugin.JGlimsPlugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.view.AnvilView;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SoulboundListener implements Listener {

    private final JGlimsPlugin plugin;
    private final CustomEnchantManager enchantManager;

    public SoulboundListener(JGlimsPlugin plugin, CustomEnchantManager enchantManager) {
        this.plugin = plugin;
        this.enchantManager = enchantManager;
    }

    // ========================================================================
    // KEEP ON DEATH — Soulbound items stay in inventory
    // ========================================================================
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        List<ItemStack> drops = event.getDrops();
        List<ItemStack> soulboundItems = new ArrayList<>();

        Iterator<ItemStack> it = drops.iterator();
        while (it.hasNext()) {
            ItemStack item = it.next();
            if (item != null && enchantManager.hasEnchant(item, EnchantmentType.SOULBOUND)) {
                soulboundItems.add(item.clone());
                it.remove();
            }
        }

        if (!soulboundItems.isEmpty()) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                for (ItemStack item : soulboundItems) {
                    Map<Integer, ItemStack> overflow = player.getInventory().addItem(item);
                    for (ItemStack leftover : overflow.values()) {
                        player.getWorld().dropItemNaturally(player.getLocation(), leftover);
                    }
                }
            }, 5L);
        }
    }

    // ========================================================================
    // LOSTSOUL CONVERSION — Rename to "lostsoul" in anvil → enchanted book
    // ONE-PER-INVENTORY — Prevent applying Soulbound if player already has one
    // Uses AnvilView for getRenameText() and setRepairCost()
    // ========================================================================
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        AnvilInventory inv = event.getInventory();
        AnvilView anvilView = (AnvilView) event.getView();

        ItemStack firstSlot = inv.getItem(0);
        ItemStack secondSlot = inv.getItem(1);

        if (firstSlot == null) return;

        // --- LOSTSOUL CONVERSION ---
        if (enchantManager.hasEnchant(firstSlot, EnchantmentType.SOULBOUND)
                && (secondSlot == null || secondSlot.getType() == Material.AIR)) {

            String renameText = anvilView.getRenameText();
            if (renameText != null && renameText.equalsIgnoreCase("lostsoul")) {
                ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
                EnchantmentStorageMeta bookMeta = (EnchantmentStorageMeta) book.getItemMeta();
                if (bookMeta != null) {
                    bookMeta.getPersistentDataContainer().set(
                        enchantManager.getKey(EnchantmentType.SOULBOUND),
                        PersistentDataType.INTEGER, 1);
                    bookMeta.displayName(Component.text("Soulbound I", NamedTextColor.LIGHT_PURPLE)
                        .decoration(TextDecoration.ITALIC, false));
                    List<Component> lore = new ArrayList<>();
                    lore.add(Component.text("Custom Enchantment", NamedTextColor.DARK_PURPLE)
                        .decoration(TextDecoration.ITALIC, false));
                    lore.add(Component.text("Item is kept on death", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false));
                    lore.add(Component.text("Apply to item in anvil", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false));
                    bookMeta.lore(lore);
                    book.setItemMeta(bookMeta);
                }
                event.setResult(book);
                anvilView.setRepairCost(0);
                return;
            }
        }

        // --- ONE-PER-INVENTORY ENFORCEMENT (PrepareAnvil) ---
        if (!(event.getView().getPlayer() instanceof Player player)) return;

        boolean wouldAddSoulbound = false;

        // Case 1: item + Totem of Undying (direct Soulbound application)
        if (secondSlot != null && secondSlot.getType() == Material.TOTEM_OF_UNDYING
                && !enchantManager.hasEnchant(firstSlot, EnchantmentType.SOULBOUND)) {
            wouldAddSoulbound = true;
        }

        // Case 2: item + Soulbound book
        if (secondSlot != null && secondSlot.getType() == Material.ENCHANTED_BOOK
                && secondSlot.hasItemMeta()) {
            if (secondSlot.getItemMeta().getPersistentDataContainer()
                    .has(enchantManager.getKey(EnchantmentType.SOULBOUND), PersistentDataType.INTEGER)) {
                if (!enchantManager.hasEnchant(firstSlot, EnchantmentType.SOULBOUND)) {
                    wouldAddSoulbound = true;
                }
            }
        }

        if (wouldAddSoulbound && countSoulboundItems(player) >= 1) {
            event.setResult(null);
            player.sendActionBar(Component.text("You can only have 1 Soulbound item!", NamedTextColor.RED));
        }
    }

    // ========================================================================
    // BUG FIX: Also enforce soulbound limit when player TAKES the result
    // ========================================================================
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory() instanceof AnvilInventory inv)) return;
        if (event.getSlotType() != InventoryType.SlotType.RESULT) return;
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack result = event.getCurrentItem();

        if (!enchantManager.hasEnchant(result, EnchantmentType.SOULBOUND)) return;

        ItemStack firstSlot = inv.getItem(0);
        if (firstSlot != null && enchantManager.hasEnchant(firstSlot, EnchantmentType.SOULBOUND)) {
            return;
        }

        if (countSoulboundItems(player) >= 1) {
            event.setCancelled(true);
            player.sendActionBar(Component.text("You can only have 1 Soulbound item!", NamedTextColor.RED));
        }
    }

    // ========================================================================
    // UTILITY: Count Soulbound items in player's inventory
    // ========================================================================
    private int countSoulboundItems(Player player) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && enchantManager.hasEnchant(item, EnchantmentType.SOULBOUND)) {
                count++;
            }
        }
        return count;
    }
}

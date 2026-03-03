package com.jglims.plugin.enchantments;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import com.jglims.plugin.JGlimsPlugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CustomEnchantManager {

    private final JGlimsPlugin plugin;
    private final Map<EnchantmentType, NamespacedKey> keys = new EnumMap<>(EnchantmentType.class);

    // Conflict map: if enchA conflicts with enchB, both directions are stored
    private final Map<EnchantmentType, Set<EnchantmentType>> conflicts = new EnumMap<>(EnchantmentType.class);

    public CustomEnchantManager(JGlimsPlugin plugin) {
        this.plugin = plugin;

        // Register keys
        for (EnchantmentType type : EnchantmentType.values()) {
            keys.put(type, new NamespacedKey(plugin, type.name().toLowerCase()));
        }

        // Initialize conflict sets
        for (EnchantmentType type : EnchantmentType.values()) {
            conflicts.put(type, EnumSet.noneOf(EnchantmentType.class));
        }

        // Register custom↔custom conflicts
        addConflict(EnchantmentType.VAMPIRISM, EnchantmentType.BERSERKER);
        addConflict(EnchantmentType.VAMPIRISM, EnchantmentType.LIFESTEAL);
        addConflict(EnchantmentType.VAMPIRISM, EnchantmentType.BLOOD_PRICE);
        addConflict(EnchantmentType.BERSERKER, EnchantmentType.LIFESTEAL);
        addConflict(EnchantmentType.BLEED, EnchantmentType.FROSTBITE);
        addConflict(EnchantmentType.BLEED, EnchantmentType.VENOMSTRIKE);
        addConflict(EnchantmentType.BLEED, EnchantmentType.WITHER_TOUCH);
        addConflict(EnchantmentType.VENOMSTRIKE, EnchantmentType.WITHER_TOUCH);
        addConflict(EnchantmentType.EXPLOSIVE_ARROW, EnchantmentType.HOMING);
        addConflict(EnchantmentType.BLOOD_PRICE, EnchantmentType.LIFESTEAL);
        addConflict(EnchantmentType.VEINMINER, EnchantmentType.DRILL);
        addConflict(EnchantmentType.MOMENTUM, EnchantmentType.SWIFTFOOT);

        // Mace conflicts (NEW v1.2.0)
        addConflict(EnchantmentType.SEISMIC_SLAM, EnchantmentType.MAGNETIZE);

        plugin.getLogger().info("Registered " + EnchantmentType.values().length + " custom enchantments.");
    }

    private void addConflict(EnchantmentType a, EnchantmentType b) {
        conflicts.computeIfAbsent(a, k -> EnumSet.noneOf(EnchantmentType.class)).add(b);
        conflicts.computeIfAbsent(b, k -> EnumSet.noneOf(EnchantmentType.class)).add(a);
    }

    public NamespacedKey getKey(EnchantmentType type) {
        return keys.get(type);
    }

    public boolean hasEnchant(ItemStack item, EnchantmentType type) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(getKey(type), PersistentDataType.INTEGER);
    }

    public int getEnchantLevel(ItemStack item, EnchantmentType type) {
        if (item == null || !item.hasItemMeta()) return 0;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        Integer level = pdc.get(getKey(type), PersistentDataType.INTEGER);
        return level != null ? level : 0;
    }

    public void setEnchant(ItemStack item, EnchantmentType type, int level) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        meta.getPersistentDataContainer().set(getKey(type), PersistentDataType.INTEGER, level);
        item.setItemMeta(meta);
    }

    public void removeEnchant(ItemStack item, EnchantmentType type) {
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().remove(getKey(type));
        item.setItemMeta(meta);
    }

    public Set<EnchantmentType> getConflicts(EnchantmentType type) {
        return conflicts.getOrDefault(type, EnumSet.noneOf(EnchantmentType.class));
    }

    public boolean hasConflict(ItemStack item, EnchantmentType newEnchant) {
        Set<EnchantmentType> conflictSet = getConflicts(newEnchant);
        for (EnchantmentType existing : conflictSet) {
            if (hasEnchant(item, existing)) return true;
        }
        return false;
    }

    public Map<EnchantmentType, Integer> getAllCustomEnchants(ItemStack item) {
        Map<EnchantmentType, Integer> result = new EnumMap<>(EnchantmentType.class);
        if (item == null || !item.hasItemMeta()) return result;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        for (EnchantmentType type : EnchantmentType.values()) {
            Integer level = pdc.get(getKey(type), PersistentDataType.INTEGER);
            if (level != null) {
                result.put(type, level);
            }
        }
        return result;
    }

    /**
     * Counts total number of enchantments (custom + vanilla) on an item.
     */
    public int getTotalEnchantCount(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return 0;
        int count = item.getEnchantments().size();
        count += getAllCustomEnchants(item).size();
        return count;
    }

    public void listEnchantments(CommandSender sender) {
        sender.sendMessage(Component.text("=== JGlims Custom Enchantments ===", NamedTextColor.GOLD));
        for (EnchantmentType type : EnchantmentType.values()) {
            sender.sendMessage(Component.text(" - " + type.name() + " (max " + type.getMaxLevel() + ")", NamedTextColor.YELLOW));
        }
        sender.sendMessage(Component.text("Total: " + EnchantmentType.values().length + " enchantments", NamedTextColor.GREEN));
    }
}

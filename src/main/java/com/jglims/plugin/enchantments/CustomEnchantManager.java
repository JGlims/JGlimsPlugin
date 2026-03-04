package com.jglims.plugin.enchantments;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class CustomEnchantManager {

    private final JavaPlugin plugin;
    private final Map<EnchantmentType, NamespacedKey> keys = new EnumMap<>(EnchantmentType.class);
    private final Map<EnchantmentType, Set<EnchantmentType>> conflictMap = new EnumMap<>(EnchantmentType.class);

    public CustomEnchantManager(JavaPlugin plugin) {
        this.plugin = plugin;
        registerKeys();
        registerConflicts();
    }

    private void registerKeys() {
        for (EnchantmentType type : EnchantmentType.values()) {
            keys.put(type, new NamespacedKey(plugin, "enchant_" + type.name().toLowerCase()));
        }
    }

    private void registerConflicts() {
        // Bidirectional conflict registration
        addConflict(EnchantmentType.VAMPIRISM, EnchantmentType.LIFESTEAL);
        addConflict(EnchantmentType.BERSERKER, EnchantmentType.BLOOD_PRICE);
        addConflict(EnchantmentType.BLEED, EnchantmentType.VENOMSTRIKE);
        addConflict(EnchantmentType.WITHER_TOUCH, EnchantmentType.CHAIN_LIGHTNING);
        addConflict(EnchantmentType.EXPLOSIVE_ARROW, EnchantmentType.HOMING);
        addConflict(EnchantmentType.VEINMINER, EnchantmentType.DRILL);
        addConflict(EnchantmentType.MOMENTUM, EnchantmentType.SWIFTFOOT);
        addConflict(EnchantmentType.SEISMIC_SLAM, EnchantmentType.MAGNETIZE);

        // Spear conflict triangle: pick one of three
        addConflict(EnchantmentType.IMPALING_THRUST, EnchantmentType.EXTENDED_REACH);
        addConflict(EnchantmentType.EXTENDED_REACH, EnchantmentType.SKEWERING);
        addConflict(EnchantmentType.IMPALING_THRUST, EnchantmentType.SKEWERING);

        // === Phase 9 conflicts ===
        addConflict(EnchantmentType.FROSTBITE_BLADE, EnchantmentType.VENOMSTRIKE);
        addConflict(EnchantmentType.WRATH, EnchantmentType.CLEAVE);
        addConflict(EnchantmentType.PROSPECTOR, EnchantmentType.AUTO_SMELT);
        addConflict(EnchantmentType.BURIAL, EnchantmentType.HARVESTER);
        addConflict(EnchantmentType.SOUL_HARVEST, EnchantmentType.GREEN_THUMB);
        addConflict(EnchantmentType.REAPING_CURSE, EnchantmentType.REPLENISH);
        addConflict(EnchantmentType.FROSTBITE_ARROW, EnchantmentType.EXPLOSIVE_ARROW);
        addConflict(EnchantmentType.TSUNAMI, EnchantmentType.FROSTBITE);
        addConflict(EnchantmentType.TREMOR, EnchantmentType.GRAVITY_WELL);
        addConflict(EnchantmentType.PHANTOM_PIERCE, EnchantmentType.SKEWERING);

    }

    private void addConflict(EnchantmentType a, EnchantmentType b) {
        conflictMap.computeIfAbsent(a, k -> EnumSet.noneOf(EnchantmentType.class)).add(b);
        conflictMap.computeIfAbsent(b, k -> EnumSet.noneOf(EnchantmentType.class)).add(a);
    }

    public NamespacedKey getKey(EnchantmentType type) {
        return keys.get(type);
    }

    /**
     * Gets the level of a custom enchantment on an item.
     * Returns 0 if not present.
     */
    public int getEnchantLevel(ItemStack item, EnchantmentType type) {
        if (item == null || !item.hasItemMeta()) return 0;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        NamespacedKey key = keys.get(type);
        if (key == null) return 0;
        return pdc.getOrDefault(key, PersistentDataType.INTEGER, 0);
    }

    /**
     * Sets a custom enchantment on an item.
     */
    public void setEnchant(ItemStack item, EnchantmentType type, int level) {
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        NamespacedKey key = keys.get(type);
        if (key == null) return;

        if (level <= 0) {
            meta.getPersistentDataContainer().remove(key);
        } else {
            meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, level);
        }
        item.setItemMeta(meta);
    }

    /**
     * Removes a custom enchantment from an item.
     */
    public void removeEnchant(ItemStack item, EnchantmentType type) {
        setEnchant(item, type, 0);
    }

    /**
     * Checks if an item has a specific custom enchantment.
     */
    public boolean hasEnchant(ItemStack item, EnchantmentType type) {
        return getEnchantLevel(item, type) > 0;
    }

    /**
     * Gets all custom enchantments on an item.
     */
    public Map<EnchantmentType, Integer> getEnchantments(ItemStack item) {
        Map<EnchantmentType, Integer> result = new EnumMap<>(EnchantmentType.class);
        if (item == null || !item.hasItemMeta()) return result;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        for (EnchantmentType type : EnchantmentType.values()) {
            NamespacedKey key = keys.get(type);
            if (key != null && pdc.has(key, PersistentDataType.INTEGER)) {
                int level = pdc.getOrDefault(key, PersistentDataType.INTEGER, 0);
                if (level > 0) {
                    result.put(type, level);
                }
            }
        }
        return result;
    }

    /**
     * Counts total custom enchantments on an item.
     */
    public int countEnchantments(ItemStack item) {
        return getEnchantments(item).size();
    }

    /**
     * Checks if adding an enchantment would cause a conflict with existing enchantments.
     */
    public boolean hasConflict(ItemStack item, EnchantmentType newEnchant) {
        Set<EnchantmentType> conflicts = conflictMap.get(newEnchant);
        if (conflicts == null || conflicts.isEmpty()) return false;
        Map<EnchantmentType, Integer> existing = getEnchantments(item);
        for (EnchantmentType conflict : conflicts) {
            if (existing.containsKey(conflict)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the set of enchantments that conflict with the given type.
     */
    public Set<EnchantmentType> getConflicts(EnchantmentType type) {
        return conflictMap.getOrDefault(type, EnumSet.noneOf(EnchantmentType.class));
    }

    /**
     * Lists all custom enchantments with their max levels (for /jglims enchants command).
     */
    public List<String> listAllEnchantments() {
        List<String> list = new ArrayList<>();
        for (EnchantmentType type : EnchantmentType.values()) {
            String name = type.name().replace('_', ' ');
            // Title case
            StringBuilder sb = new StringBuilder();
            for (String word : name.split(" ")) {
                sb.append(Character.toUpperCase(word.charAt(0)))
                  .append(word.substring(1).toLowerCase())
                  .append(' ');
            }
            list.add("§e" + sb.toString().trim() + " §7(Max " + type.getMaxLevel() + ")");
        }
        return list;
    }

    /**
     * Copies all custom enchantments from one item to another.
     * Used during super-tool upgrades to prevent enchantment loss.
     */
    public void copyAllEnchantments(ItemStack source, ItemStack target) {
        Map<EnchantmentType, Integer> enchants = getEnchantments(source);
        for (Map.Entry<EnchantmentType, Integer> entry : enchants.entrySet()) {
            setEnchant(target, entry.getKey(), entry.getValue());
        }
    }

    /**
     * Alias for getEnchantments() — backward compatibility with existing code.
     */
    public Map<EnchantmentType, Integer> getAllCustomEnchants(ItemStack item) {
        return getEnchantments(item);
    }

    /**
     * Alias for listAllEnchantments() — backward compatibility.
     * Accepts a CommandSender and sends the list directly.
     */
    public void listEnchantments(org.bukkit.command.CommandSender sender) {
        List<String> enchants = listAllEnchantments();
        sender.sendMessage("§6§l=== Custom Enchantments (" + enchants.size() + ") ===");
        for (String line : enchants) {
            sender.sendMessage(line);
        }
    }

}

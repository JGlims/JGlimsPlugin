package com.jglims.plugin.enchantments;

import com.jglims.plugin.JGlimsPlugin;
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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class AnvilRecipeListener implements Listener {

    private final JGlimsPlugin plugin;
    private final CustomEnchantManager enchantManager;

    // Recipe registry: EnchantmentType -> [required book enchant, ingredient material, level-matching behavior]
    private final Map<EnchantmentType, AnvilRecipe> recipes = new EnumMap<>(EnchantmentType.class);

    public AnvilRecipeListener(JGlimsPlugin plugin, CustomEnchantManager enchantManager) {
        this.plugin = plugin;
        this.enchantManager = enchantManager;
        registerRecipes();
    }

    private void registerRecipes() {
        // === SWORD ===
        // Vampirism: Sharpness book + Netherite Scrap -> Vampirism (level matches Sharpness, max V)
        recipes.put(EnchantmentType.VAMPIRISM, new AnvilRecipe(Enchantment.SHARPNESS, Material.NETHERITE_SCRAP, true, 5));
        // Bleed: Sharpness book + Redstone Block -> Bleed (level matches, max III)
        recipes.put(EnchantmentType.BLEED, new AnvilRecipe(Enchantment.SHARPNESS, Material.REDSTONE_BLOCK, true, 3));
        // Venomstrike: Sharpness book + Spider Eye -> Venomstrike (level matches, max III)
        recipes.put(EnchantmentType.VENOMSTRIKE, new AnvilRecipe(Enchantment.SHARPNESS, Material.SPIDER_EYE, true, 3));
        // Lifesteal: Sharpness book + Ghast Tear -> Lifesteal (level matches, max III)
        recipes.put(EnchantmentType.LIFESTEAL, new AnvilRecipe(Enchantment.SHARPNESS, Material.GHAST_TEAR, true, 3));

        // === AXE ===
        // Berserker: Sharpness book + Bone -> Berserker (level matches, max V)
        recipes.put(EnchantmentType.BERSERKER, new AnvilRecipe(Enchantment.SHARPNESS, Material.BONE, true, 5));
        // Lumberjack: Efficiency book + Iron Ingot -> Lumberjack (level matches, max III)
        recipes.put(EnchantmentType.LUMBERJACK, new AnvilRecipe(Enchantment.EFFICIENCY, Material.IRON_INGOT, true, 3));
        // Cleave: Sharpness book + Iron Block -> Cleave (level matches, max III)
        recipes.put(EnchantmentType.CLEAVE, new AnvilRecipe(Enchantment.SHARPNESS, Material.IRON_BLOCK, true, 3));
        // Timber: Efficiency book + Oak Log -> Timber (level matches, max III)
        recipes.put(EnchantmentType.TIMBER, new AnvilRecipe(Enchantment.EFFICIENCY, Material.OAK_LOG, true, 3));
        // Guillotine: Sharpness book + Wither Skeleton Skull -> Guillotine (level matches, max III)
        recipes.put(EnchantmentType.GUILLOTINE, new AnvilRecipe(Enchantment.SHARPNESS, Material.WITHER_SKELETON_SKULL, true, 3));

        // === PICKAXE ===
        // Veinminer: Efficiency book + Diamond -> Veinminer (level matches, max III)
        recipes.put(EnchantmentType.VEINMINER, new AnvilRecipe(Enchantment.EFFICIENCY, Material.DIAMOND, true, 3));
        // Drill: Efficiency book + Iron Block -> Drill (level matches, max III)
        recipes.put(EnchantmentType.DRILL, new AnvilRecipe(Enchantment.EFFICIENCY, Material.IRON_BLOCK, true, 3));
        // Auto-Smelt: Efficiency book + Furnace -> Auto-Smelt (always I)
        recipes.put(EnchantmentType.AUTO_SMELT, new AnvilRecipe(Enchantment.EFFICIENCY, Material.FURNACE, false, 1));
        // Magnetism: Efficiency book + Lapis Block -> Magnetism (always I)
        recipes.put(EnchantmentType.MAGNETISM, new AnvilRecipe(Enchantment.EFFICIENCY, Material.LAPIS_BLOCK, false, 1));

        // === SHOVEL ===
        // Excavator: Efficiency book + Gold Ingot -> Excavator (level matches, max III)
        recipes.put(EnchantmentType.EXCAVATOR, new AnvilRecipe(Enchantment.EFFICIENCY, Material.GOLD_INGOT, true, 3));
        // Replenish: Efficiency book + Wheat Seeds -> Replenish (always I)
        recipes.put(EnchantmentType.REPLENISH, new AnvilRecipe(Enchantment.EFFICIENCY, Material.WHEAT_SEEDS, false, 1));

        // === HOE ===
        // Harvester: Efficiency book + Wheat -> Harvester (level matches, max III)
        recipes.put(EnchantmentType.HARVESTER, new AnvilRecipe(Enchantment.EFFICIENCY, Material.WHEAT, true, 3));
        // Green Thumb: Efficiency book + Bone Meal -> Green Thumb (always I)
        recipes.put(EnchantmentType.GREEN_THUMB, new AnvilRecipe(Enchantment.EFFICIENCY, Material.BONE_MEAL, false, 1));

        // === TRIDENT ===
        // Thunderlord: Unbreaking book + Blaze Rod -> Thunderlord (level matches, max V)
        recipes.put(EnchantmentType.THUNDERLORD, new AnvilRecipe(Enchantment.UNBREAKING, Material.BLAZE_ROD, true, 5));
        // Swiftness: Unbreaking book + Sugar -> Swiftness (level matches, max III)
        recipes.put(EnchantmentType.SWIFTNESS, new AnvilRecipe(Enchantment.UNBREAKING, Material.SUGAR, true, 3));
        // Vitality: Unbreaking book + Glistering Melon Slice -> Vitality (level matches, max III)
        recipes.put(EnchantmentType.VITALITY, new AnvilRecipe(Enchantment.UNBREAKING, Material.GLISTERING_MELON_SLICE, true, 3));
        // Tidal Wave: Unbreaking book + Heart of the Sea -> Tidal Wave (level matches, max III)
        recipes.put(EnchantmentType.TIDAL_WAVE, new AnvilRecipe(Enchantment.UNBREAKING, Material.HEART_OF_THE_SEA, true, 3));

        // === UNIVERSAL MELEE ===
        // Frostbite: Sharpness book + Packed Ice -> Frostbite (level matches, max III)
        recipes.put(EnchantmentType.FROSTBITE, new AnvilRecipe(Enchantment.SHARPNESS, Material.PACKED_ICE, true, 3));

        // === BOW & CROSSBOW ===
        // Explosive Arrow: Power book + TNT -> Explosive Arrow (level matches, max III)
        recipes.put(EnchantmentType.EXPLOSIVE_ARROW, new AnvilRecipe(Enchantment.POWER, Material.TNT, true, 3));
        // Homing: Power book + Ender Eye -> Homing (level matches, max II)
        recipes.put(EnchantmentType.HOMING, new AnvilRecipe(Enchantment.POWER, Material.ENDER_EYE, true, 2));
        // Rapidfire: Unbreaking book + String -> Rapidfire (level matches, max III)
        recipes.put(EnchantmentType.RAPIDFIRE, new AnvilRecipe(Enchantment.UNBREAKING, Material.STRING, true, 3));
        // Sniper: Power book + Spyglass -> Sniper (level matches, max III)
        recipes.put(EnchantmentType.SNIPER, new AnvilRecipe(Enchantment.POWER, Material.SPYGLASS, true, 3));

        // === HELMET ===
        // Night Vision: Protection book + Golden Carrot -> Night Vision (always I)
        recipes.put(EnchantmentType.NIGHT_VISION, new AnvilRecipe(Enchantment.PROTECTION, Material.GOLDEN_CARROT, false, 1));
        // Aqua Lungs: Protection book + Pufferfish -> Aqua Lungs (level matches, max II)
        recipes.put(EnchantmentType.AQUA_LUNGS, new AnvilRecipe(Enchantment.PROTECTION, Material.PUFFERFISH, true, 2));

        // === CHESTPLATE ===
        // Fortification: Protection book + Diamond -> Fortification (level matches, max III)
        recipes.put(EnchantmentType.FORTIFICATION, new AnvilRecipe(Enchantment.PROTECTION, Material.DIAMOND, true, 3));
        // Deflection: Protection book + Shield -> Deflection (level matches, max II)
        recipes.put(EnchantmentType.DEFLECTION, new AnvilRecipe(Enchantment.PROTECTION, Material.SHIELD, true, 2));

        // === LEGGINGS ===
        // Swiftfoot: Protection book + Sugar -> Swiftfoot (level matches, max III)
        recipes.put(EnchantmentType.SWIFTFOOT, new AnvilRecipe(Enchantment.PROTECTION, Material.SUGAR, true, 3));
        // Dodge: Protection book + Feather -> Dodge (level matches, max II)
        recipes.put(EnchantmentType.DODGE, new AnvilRecipe(Enchantment.PROTECTION, Material.FEATHER, true, 2));

        // === BOOTS ===
        // Leaping: Protection book + Rabbit Foot -> Leaping (level matches, max III)
        recipes.put(EnchantmentType.LEAPING, new AnvilRecipe(Enchantment.PROTECTION, Material.RABBIT_FOOT, true, 3));
        // Stomp: Protection book + Anvil -> Stomp (level matches, max III)
        recipes.put(EnchantmentType.STOMP, new AnvilRecipe(Enchantment.PROTECTION, Material.ANVIL, true, 3));

        // === ELYTRA ===
        // Cushion: Unbreaking book + Feather -> Cushion (always I)
        recipes.put(EnchantmentType.CUSHION, new AnvilRecipe(Enchantment.UNBREAKING, Material.FEATHER, false, 1));
        // Boost: Unbreaking book + Firework Rocket -> Boost (level matches, max III)
        recipes.put(EnchantmentType.BOOST, new AnvilRecipe(Enchantment.UNBREAKING, Material.FIREWORK_ROCKET, true, 3));
        // Glider: Unbreaking book + Phantom Membrane -> Glider (always I)
        recipes.put(EnchantmentType.GLIDER, new AnvilRecipe(Enchantment.UNBREAKING, Material.PHANTOM_MEMBRANE, false, 1));

        // === SICKLE ===
        // Soul Reap: Sharpness book + Soul Sand -> Soul Reap (level matches, max III)
        recipes.put(EnchantmentType.SOUL_REAP, new AnvilRecipe(Enchantment.SHARPNESS, Material.SOUL_SAND, true, 3));
        // Blood Price: Sharpness book + Nether Wart -> Blood Price (level matches, max III)
        recipes.put(EnchantmentType.BLOOD_PRICE, new AnvilRecipe(Enchantment.SHARPNESS, Material.NETHER_WART, true, 3));
        // Reaper's Mark: Sharpness book + Ender Eye -> Reaper's Mark (level matches, max II)
        recipes.put(EnchantmentType.REAPERS_MARK, new AnvilRecipe(Enchantment.SHARPNESS, Material.ENDER_EYE, true, 2));
        // Wither Touch: Sharpness book + Wither Rose -> Wither Touch (level matches, max II)
        recipes.put(EnchantmentType.WITHER_TOUCH, new AnvilRecipe(Enchantment.SHARPNESS, Material.WITHER_ROSE, true, 2));

        // NOTE: Soulbound is handled separately (any item + Totem of Undying)
        // NOTE: BestBuddies is a crafting recipe (Bone + Diamond), not anvil

        plugin.getLogger().info("Registered " + recipes.size() + " anvil enchantment recipes.");
    }

    // ========================================================================
    // PREPARE ANVIL EVENT — shows the result before the player clicks
    // ========================================================================
    @EventHandler(priority = EventPriority.HIGH)
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        AnvilInventory inv = event.getInventory();
        ItemStack firstSlot = inv.getItem(0);
        ItemStack secondSlot = inv.getItem(1);

        if (firstSlot == null || secondSlot == null) return;

        // --- CASE 1: Soulbound (any item + Totem of Undying) ---
        if (secondSlot.getType() == Material.TOTEM_OF_UNDYING) {
            handleSoulboundPrepare(event, inv, firstSlot, secondSlot);
            return;
        }

        // --- CASE 2: Enchantment book creation (enchanted book in slot 1 + material in slot 2) ---
        if (firstSlot.getType() == Material.ENCHANTED_BOOK && !secondSlot.getType().equals(Material.ENCHANTED_BOOK) && !secondSlot.getType().equals(Material.BOOK)) {
            handleBookCreationPrepare(event, inv, firstSlot, secondSlot);
            return;
        }

        // --- CASE 3: Applying custom enchanted book to an item ---
        if (secondSlot.getType() == Material.ENCHANTED_BOOK && firstSlot.getType() != Material.ENCHANTED_BOOK) {
            handleBookApplicationPrepare(event, inv, firstSlot, secondSlot);
            return;
        }

        // --- CASE 4: Normal anvil operation — apply Too Expensive removal & XP reduction ---
        handleVanillaAnvilTweaks(event, inv);
    }

    // ========================================================================
    // SOULBOUND PREPARATION
    // ========================================================================
    private void handleSoulboundPrepare(PrepareAnvilEvent event, AnvilInventory inv, ItemStack tool, ItemStack totem) {
        // Check if item already has Soulbound
        if (enchantManager.hasEnchant(tool, EnchantmentType.SOULBOUND)) {
            event.setResult(null);
            return;
        }

        // Create result: clone tool + add Soulbound
        ItemStack result = tool.clone();
        enchantManager.setEnchant(result, EnchantmentType.SOULBOUND, 1);

        // Add Soulbound to lore
        ItemMeta meta = result.getItemMeta();
        if (meta != null) {
            List<Component> lore = meta.lore() != null ? new ArrayList<>(meta.lore()) : new ArrayList<>();
            lore.add(Component.text("Soulbound", NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
            result.setItemMeta(meta);
        }

        event.setResult(result);
        inv.setRepairCost(0);
    }

    // ========================================================================
    // CUSTOM ENCHANTMENT BOOK CREATION
    // ========================================================================
    private void handleBookCreationPrepare(PrepareAnvilEvent event, AnvilInventory inv, ItemStack book, ItemStack ingredient) {
        if (!(book.getItemMeta() instanceof EnchantmentStorageMeta bookMeta)) return;

        Material ingredientType = ingredient.getType();

        // Find matching recipe
        for (Map.Entry<EnchantmentType, AnvilRecipe> entry : recipes.entrySet()) {
            AnvilRecipe recipe = entry.getValue();
            EnchantmentType enchType = entry.getKey();

            if (recipe.ingredient != ingredientType) continue;
            if (!bookMeta.hasStoredEnchant(recipe.requiredEnchant)) continue;

            // Match found! Calculate level
            int bookLevel = bookMeta.getStoredEnchantLevel(recipe.requiredEnchant);
            int resultLevel;
            if (recipe.levelMatches) {
                resultLevel = Math.min(bookLevel, recipe.maxLevel);
            } else {
                resultLevel = recipe.maxLevel; // Fixed level (e.g., always I)
            }

            // Create the custom enchanted book
            ItemStack result = new ItemStack(Material.ENCHANTED_BOOK);
            EnchantmentStorageMeta resultMeta = (EnchantmentStorageMeta) result.getItemMeta();
            if (resultMeta == null) return;

            // Store the custom enchantment in PDC
            resultMeta.getPersistentDataContainer().set(
                enchantManager.getKey(enchType),
                PersistentDataType.INTEGER,
                resultLevel
            );

            // Set display name with color
            NamedTextColor nameColor = getEnchantColor(enchType);
            String levelStr = toRoman(resultLevel);
            resultMeta.displayName(
                Component.text(formatEnchantName(enchType) + " " + levelStr, nameColor)
                    .decoration(TextDecoration.ITALIC, false)
            );

            // Add lore describing the enchantment
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Custom Enchantment", NamedTextColor.DARK_PURPLE)
                .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Apply to item in anvil", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
            resultMeta.lore(lore);

            result.setItemMeta(resultMeta);
            event.setResult(result);
            inv.setRepairCost(0);
            return;
        }
        // No recipe matched — let vanilla handle it
    }

    // ========================================================================
    // APPLYING CUSTOM ENCHANTED BOOK TO AN ITEM
    // ========================================================================
    private void handleBookApplicationPrepare(PrepareAnvilEvent event, AnvilInventory inv, ItemStack tool, ItemStack book) {
        if (!(book.getItemMeta() instanceof EnchantmentStorageMeta bookMeta)) return;

        PersistentDataContainer bookPdc = bookMeta.getPersistentDataContainer();

        // Find which custom enchantment this book holds
        EnchantmentType foundType = null;
        int foundLevel = 0;
        for (EnchantmentType type : EnchantmentType.values()) {
            Integer level = bookPdc.get(enchantManager.getKey(type), PersistentDataType.INTEGER);
            if (level != null) {
                foundType = type;
                foundLevel = level;
                break;
            }
        }

        if (foundType == null) {
            // Not a custom enchanted book — let vanilla handle, but still apply tweaks
            handleVanillaAnvilTweaks(event, inv);
            return;
        }

        // Check for conflicts with existing custom enchantments on the tool
        if (enchantManager.hasConflict(tool, foundType)) {
            event.setResult(null);
            return;
        }

        // Check for conflicts with vanilla enchantments on the tool
        if (hasVanillaConflict(tool, foundType)) {
            event.setResult(null);
            return;
        }

        // Create result: clone tool + add enchantment
        ItemStack result = tool.clone();
        enchantManager.setEnchant(result, foundType, foundLevel);

        // Update lore to show the enchantment
        ItemMeta meta = result.getItemMeta();
        if (meta != null) {
            List<Component> lore = meta.lore() != null ? new ArrayList<>(meta.lore()) : new ArrayList<>();
            NamedTextColor color = getEnchantColor(foundType);
            String levelStr = toRoman(foundLevel);
            lore.add(Component.text(formatEnchantName(foundType) + " " + levelStr, color)
                .decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
            result.setItemMeta(meta);
        }

        event.setResult(result);
        inv.setRepairCost(0);
    }

    // ========================================================================
    // VANILLA ANVIL TWEAKS (Too Expensive removal, XP reduction)
    // ========================================================================
    private void handleVanillaAnvilTweaks(PrepareAnvilEvent event, AnvilInventory inv) {
        if (!plugin.getConfigManager().isRemoveTooExpensive()) return;

        // Remove "Too Expensive" by capping repair cost at 39
        if (inv.getRepairCost() >= 40) {
            inv.setRepairCost(39);
        }

        // Apply XP cost reduction
        double reduction = plugin.getConfigManager().getXpCostReduction();
        if (reduction > 0) {
            int currentCost = inv.getRepairCost();
            int newCost = Math.max(1, (int) (currentCost * (1.0 - reduction)));
            inv.setRepairCost(newCost);
        }
    }

    // ========================================================================
    // ANVIL RESULT CLICK — consume ingredients properly
    // ========================================================================
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory() instanceof AnvilInventory inv)) return;
        if (event.getSlotType() != InventoryType.SlotType.RESULT) return;
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack result = event.getCurrentItem();
        ItemStack firstSlot = inv.getItem(0);
        ItemStack secondSlot = inv.getItem(1);

        if (firstSlot == null || secondSlot == null) return;

        // Check if this is a custom operation by looking for custom enchant in result PDC
        boolean isCustomOperation = false;
        if (result.hasItemMeta()) {
            PersistentDataContainer pdc = result.getItemMeta().getPersistentDataContainer();
            for (EnchantmentType type : EnchantmentType.values()) {
                if (pdc.has(enchantManager.getKey(type), PersistentDataType.INTEGER)) {
                    isCustomOperation = true;
                    break;
                }
            }
        }

        if (!isCustomOperation) return;

        // For custom operations: consume both input slots, give result
        // The event will handle giving the result item to the cursor
        // We just need to clear the input slots after
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            inv.setItem(0, null);
            inv.setItem(1, null);
        });
    }

    // ========================================================================
    // VANILLA ENCHANTMENT CONFLICT CHECKS
    // ========================================================================
    private boolean hasVanillaConflict(ItemStack tool, EnchantmentType customEnchant) {
        if (tool == null || !tool.hasItemMeta()) return false;
        Map<Enchantment, Integer> vanillaEnchants = tool.getEnchantments();

        return switch (customEnchant) {
            case FROSTBITE -> vanillaEnchants.containsKey(Enchantment.FIRE_ASPECT);
            case THUNDERLORD -> vanillaEnchants.containsKey(Enchantment.CHANNELING);
            case TIDAL_WAVE -> vanillaEnchants.containsKey(Enchantment.RIPTIDE);
            case EXPLOSIVE_ARROW -> vanillaEnchants.containsKey(Enchantment.INFINITY);
            case HOMING -> vanillaEnchants.containsKey(Enchantment.MULTISHOT);
            case RAPIDFIRE -> vanillaEnchants.containsKey(Enchantment.QUICK_CHARGE);
            case AQUA_LUNGS -> vanillaEnchants.containsKey(Enchantment.RESPIRATION);
            case DEFLECTION -> vanillaEnchants.containsKey(Enchantment.PROJECTILE_PROTECTION);
            case STOMP -> vanillaEnchants.containsKey(Enchantment.FEATHER_FALLING);
            case SOULBOUND -> vanillaEnchants.containsKey(Enchantment.VANISHING_CURSE);
            case CLEAVE -> vanillaEnchants.containsKey(Enchantment.SWEEPING_EDGE);
            case AUTO_SMELT -> vanillaEnchants.containsKey(Enchantment.SILK_TOUCH)
                || vanillaEnchants.containsKey(Enchantment.FORTUNE);
            case DRILL -> {
                // Only Drill III conflicts with Silk Touch
                int drillLevel = enchantManager.getEnchantLevel(tool, EnchantmentType.DRILL);
                // If tool already has Drill and we're adding more, check existing level
                // If the book is Drill III, check Silk Touch
                yield false; // Conflict checked at a higher level
            }
            case VEINMINER -> enchantManager.hasEnchant(tool, EnchantmentType.DRILL);
            default -> false;
        };
    }

    // ========================================================================
    // UTILITY METHODS
    // ========================================================================
    private NamedTextColor getEnchantColor(EnchantmentType type) {
        return switch (type) {
            case VAMPIRISM, BLEED, BLOOD_PRICE -> NamedTextColor.RED;
            case BERSERKER -> NamedTextColor.DARK_RED;
            case VENOMSTRIKE, WITHER_TOUCH -> NamedTextColor.DARK_GREEN;
            case LIFESTEAL -> NamedTextColor.DARK_RED;
            case FROSTBITE -> NamedTextColor.AQUA;
            case THUNDERLORD -> NamedTextColor.YELLOW;
            case SOULBOUND -> NamedTextColor.LIGHT_PURPLE;
            case SOUL_REAP, REAPERS_MARK -> NamedTextColor.DARK_PURPLE;
            default -> NamedTextColor.BLUE;
        };
    }

    private String formatEnchantName(EnchantmentType type) {
        String name = type.name().replace('_', ' ');
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
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> String.valueOf(level);
        };
    }

    // ========================================================================
    // INNER CLASS: Recipe definition
    // ========================================================================
    private record AnvilRecipe(
        Enchantment requiredEnchant,
        Material ingredient,
        boolean levelMatches,
        int maxLevel
    ) {}
}
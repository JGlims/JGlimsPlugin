package com.jglims.plugin.enchantments;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.weapons.SuperToolManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class AnvilRecipeListener implements Listener {

    private final JGlimsPlugin plugin;
    private final CustomEnchantManager enchantManager;

    // Recipe registry: EnchantmentType -> AnvilRecipe definition
    private final Map<EnchantmentType, AnvilRecipe> recipes = new EnumMap<>(EnchantmentType.class);

    // Key to detect "definitive" (Netherite tier 3) super tools that bypass conflicts
    private final NamespacedKey superTierKey;

    public AnvilRecipeListener(JGlimsPlugin plugin, CustomEnchantManager enchantManager) {
        this.plugin = plugin;
        this.enchantManager = enchantManager;
        this.superTierKey = new NamespacedKey(plugin, "super_tool_tier");
        registerRecipes();
    }

    private void registerRecipes() {
        // ===================== SWORD =====================
        recipes.put(EnchantmentType.VAMPIRISM,
            new AnvilRecipe(Enchantment.SHARPNESS, Material.NETHERITE_SCRAP, true, 5));
        recipes.put(EnchantmentType.BLEED,
            new AnvilRecipe(Enchantment.SHARPNESS, Material.REDSTONE_BLOCK, true, 3));
        recipes.put(EnchantmentType.VENOMSTRIKE,
            new AnvilRecipe(Enchantment.SHARPNESS, Material.SPIDER_EYE, true, 3));
        recipes.put(EnchantmentType.LIFESTEAL,
            new AnvilRecipe(Enchantment.SHARPNESS, Material.GHAST_TEAR, true, 3));
        // FEATURE 14: Chain Lightning (Sword)
        recipes.put(EnchantmentType.CHAIN_LIGHTNING,
            new AnvilRecipe(Enchantment.SHARPNESS, Material.LIGHTNING_ROD, true, 3));

        // ===================== AXE =====================
        recipes.put(EnchantmentType.BERSERKER,
            new AnvilRecipe(Enchantment.SHARPNESS, Material.BONE, true, 5));
        recipes.put(EnchantmentType.LUMBERJACK,
            new AnvilRecipe(Enchantment.EFFICIENCY, Material.IRON_INGOT, true, 3));
        recipes.put(EnchantmentType.CLEAVE,
            new AnvilRecipe(Enchantment.SHARPNESS, Material.IRON_BLOCK, true, 3));
        recipes.put(EnchantmentType.TIMBER,
            new AnvilRecipe(Enchantment.EFFICIENCY, Material.OAK_LOG, true, 3));
        recipes.put(EnchantmentType.GUILLOTINE,
            new AnvilRecipe(Enchantment.SHARPNESS, Material.WITHER_SKELETON_SKULL, true, 3));

        // ===================== PICKAXE =====================
        recipes.put(EnchantmentType.VEINMINER,
            new AnvilRecipe(Enchantment.EFFICIENCY, Material.DIAMOND, true, 3));
        recipes.put(EnchantmentType.DRILL,
            new AnvilRecipe(Enchantment.EFFICIENCY, Material.IRON_BLOCK, true, 3));
        recipes.put(EnchantmentType.AUTO_SMELT,
            new AnvilRecipe(Enchantment.EFFICIENCY, Material.FURNACE, false, 1));
        recipes.put(EnchantmentType.MAGNETISM,
            new AnvilRecipe(Enchantment.EFFICIENCY, Material.LAPIS_BLOCK, false, 1));
        // FEATURE 14: Gravity Well (Pickaxe)
        recipes.put(EnchantmentType.GRAVITY_WELL,
            new AnvilRecipe(Enchantment.EFFICIENCY, Material.ENDER_PEARL, true, 3));

        // ===================== SHOVEL =====================
        recipes.put(EnchantmentType.EXCAVATOR,
            new AnvilRecipe(Enchantment.EFFICIENCY, Material.GOLD_INGOT, true, 3));
        recipes.put(EnchantmentType.REPLENISH,
            new AnvilRecipe(Enchantment.EFFICIENCY, Material.WHEAT_SEEDS, false, 1));

        // ===================== HOE =====================
        recipes.put(EnchantmentType.HARVESTER,
            new AnvilRecipe(Enchantment.EFFICIENCY, Material.WHEAT, true, 3));
        recipes.put(EnchantmentType.GREEN_THUMB,
            new AnvilRecipe(Enchantment.EFFICIENCY, Material.BONE_MEAL, false, 1));

        // ===================== SICKLE =====================
        recipes.put(EnchantmentType.SOUL_REAP,
            new AnvilRecipe(Enchantment.SHARPNESS, Material.SOUL_SAND, true, 3));
        recipes.put(EnchantmentType.BLOOD_PRICE,
            new AnvilRecipe(Enchantment.SHARPNESS, Material.NETHER_WART, true, 3));
        recipes.put(EnchantmentType.REAPERS_MARK,
            new AnvilRecipe(Enchantment.SHARPNESS, Material.ENDER_EYE, true, 2));
        recipes.put(EnchantmentType.WITHER_TOUCH,
            new AnvilRecipe(Enchantment.SHARPNESS, Material.WITHER_ROSE, true, 2));
        // FEATURE 14: Harvesting Moon (Sickle)
        recipes.put(EnchantmentType.HARVESTING_MOON,
            new AnvilRecipe(Enchantment.SHARPNESS, Material.QUARTZ, true, 3));

        // ===================== TRIDENT =====================
        recipes.put(EnchantmentType.THUNDERLORD,
            new AnvilRecipe(Enchantment.UNBREAKING, Material.BLAZE_ROD, true, 5));
        recipes.put(EnchantmentType.SWIFTNESS,
            new AnvilRecipe(Enchantment.UNBREAKING, Material.SUGAR, true, 3));
        recipes.put(EnchantmentType.VITALITY,
            new AnvilRecipe(Enchantment.UNBREAKING, Material.GLISTERING_MELON_SLICE, true, 3));
        recipes.put(EnchantmentType.TIDAL_WAVE,
            new AnvilRecipe(Enchantment.UNBREAKING, Material.HEART_OF_THE_SEA, true, 3));

        // ===================== UNIVERSAL MELEE =====================
        recipes.put(EnchantmentType.FROSTBITE,
            new AnvilRecipe(Enchantment.SHARPNESS, Material.PACKED_ICE, true, 3));

        // ===================== BOW & CROSSBOW =====================
        recipes.put(EnchantmentType.EXPLOSIVE_ARROW,
            new AnvilRecipe(Enchantment.POWER, Material.TNT, true, 3));
        recipes.put(EnchantmentType.HOMING,
            new AnvilRecipe(Enchantment.POWER, Material.ENDER_EYE, true, 2));
        recipes.put(EnchantmentType.RAPIDFIRE,
            new AnvilRecipe(Enchantment.UNBREAKING, Material.STRING, true, 3));
        recipes.put(EnchantmentType.SNIPER,
            new AnvilRecipe(Enchantment.POWER, Material.SPYGLASS, true, 3));

        // ===================== HELMET =====================
        recipes.put(EnchantmentType.NIGHT_VISION,
            new AnvilRecipe(Enchantment.PROTECTION, Material.GOLDEN_CARROT, false, 1));
        recipes.put(EnchantmentType.AQUA_LUNGS,
            new AnvilRecipe(Enchantment.PROTECTION, Material.PUFFERFISH, true, 2));

        // ===================== CHESTPLATE =====================
        recipes.put(EnchantmentType.FORTIFICATION,
            new AnvilRecipe(Enchantment.PROTECTION, Material.DIAMOND, true, 3));
        recipes.put(EnchantmentType.DEFLECTION,
            new AnvilRecipe(Enchantment.PROTECTION, Material.SHIELD, true, 2));

        // ===================== LEGGINGS =====================
        recipes.put(EnchantmentType.SWIFTFOOT,
            new AnvilRecipe(Enchantment.PROTECTION, Material.SUGAR, true, 3));
        recipes.put(EnchantmentType.DODGE,
            new AnvilRecipe(Enchantment.PROTECTION, Material.FEATHER, true, 2));

        // ===================== BOOTS =====================
        recipes.put(EnchantmentType.LEAPING,
            new AnvilRecipe(Enchantment.PROTECTION, Material.RABBIT_FOOT, true, 3));
        recipes.put(EnchantmentType.STOMP,
            new AnvilRecipe(Enchantment.PROTECTION, Material.ANVIL, true, 3));
        // FEATURE 14: Momentum (Boots)
        recipes.put(EnchantmentType.MOMENTUM,
            new AnvilRecipe(Enchantment.PROTECTION, Material.WIND_CHARGE, true, 3));

        // ===================== ELYTRA =====================
        recipes.put(EnchantmentType.CUSHION,
            new AnvilRecipe(Enchantment.UNBREAKING, Material.FEATHER, false, 1));
        recipes.put(EnchantmentType.BOOST,
            new AnvilRecipe(Enchantment.UNBREAKING, Material.FIREWORK_ROCKET, true, 3));
        recipes.put(EnchantmentType.GLIDER,
            new AnvilRecipe(Enchantment.UNBREAKING, Material.PHANTOM_MEMBRANE, false, 1));

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
        if (firstSlot.getType() == Material.ENCHANTED_BOOK
                && !secondSlot.getType().equals(Material.ENCHANTED_BOOK)
                && !secondSlot.getType().equals(Material.BOOK)) {
            handleBookCreationPrepare(event, inv, firstSlot, secondSlot);
            return;
        }

        // --- CASE 3: Applying custom enchanted book to an item ---
        if (secondSlot.getType() == Material.ENCHANTED_BOOK
                && firstSlot.getType() != Material.ENCHANTED_BOOK) {
            handleBookApplicationPrepare(event, inv, firstSlot, secondSlot);
            return;
        }

        // --- CASE 4: Normal anvil operation — apply Too Expensive removal & XP reduction ---
        handleVanillaAnvilTweaks(event, inv);
    }

    // ========================================================================
    // SOULBOUND PREPARATION
    // ========================================================================
    private void handleSoulboundPrepare(PrepareAnvilEvent event, AnvilInventory inv,
                                        ItemStack tool, ItemStack totem) {
        // Check if item already has Soulbound
        if (enchantManager.hasEnchant(tool, EnchantmentType.SOULBOUND)) {
            event.setResult(null);
            return;
        }

        // Create result: clone tool + add Soulbound
        ItemStack result = tool.clone();
        enchantManager.setEnchant(result, EnchantmentType.SOULBOUND, 1);

        // Add Soulbound to lore + glint
        ItemMeta meta = result.getItemMeta();
        if (meta != null) {
            List<Component> lore = meta.lore() != null ? new ArrayList<>(meta.lore()) : new ArrayList<>();
            lore.add(Component.text("Soulbound", NamedTextColor.LIGHT_PURPLE)
                .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Item is kept on death", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);

            // BUG 3 FIX: Add enchantment glint
            addEnchantGlint(meta);

            result.setItemMeta(meta);
        }

        event.setResult(result);
        inv.setRepairCost(0);
    }

    // ========================================================================
    // CUSTOM ENCHANTMENT BOOK CREATION
    // ========================================================================
    private void handleBookCreationPrepare(PrepareAnvilEvent event, AnvilInventory inv,
                                           ItemStack book, ItemStack ingredient) {
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

            // BUG 6 FIX: Add description lore for the enchantment
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Custom Enchantment", NamedTextColor.DARK_PURPLE)
                .decoration(TextDecoration.ITALIC, false));
            String desc = getEnchantDescription(enchType, resultLevel);
            if (desc != null) {
                lore.add(Component.text(desc, NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            }
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
    private void handleBookApplicationPrepare(PrepareAnvilEvent event, AnvilInventory inv,
                                              ItemStack tool, ItemStack book) {
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

        // FEATURE 7: Super Netherite (tier 3) tools bypass ALL enchantment conflicts
        boolean isDefinitive = isDefinitiveNetheriteSuperTool(tool);

        if (!isDefinitive) {
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
        }

        // Create result: clone tool + add enchantment
        ItemStack result = tool.clone();
        enchantManager.setEnchant(result, foundType, foundLevel);

        // Update lore to show the enchantment + description
        ItemMeta meta = result.getItemMeta();
        if (meta != null) {
            List<Component> lore = meta.lore() != null ? new ArrayList<>(meta.lore()) : new ArrayList<>();
            NamedTextColor color = getEnchantColor(foundType);
            String levelStr = toRoman(foundLevel);
            lore.add(Component.text(formatEnchantName(foundType) + " " + levelStr, color)
                .decoration(TextDecoration.ITALIC, false));

            // BUG 6 FIX: Add description to the applied item
            String desc = getEnchantDescription(foundType, foundLevel);
            if (desc != null) {
                lore.add(Component.text("  " + desc, NamedTextColor.DARK_GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            }
            meta.lore(lore);

            // BUG 3 FIX: Add enchantment glint
            addEnchantGlint(meta);

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
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            inv.setItem(0, null);
            inv.setItem(1, null);
        });
    }

    // ========================================================================
    // FEATURE 7: Check if tool is a Definitive (Netherite-tier 3) Super Tool
    // These tools ignore ALL enchantment conflicts and have no enchantment cap.
    // v1.1.0 FIX: Now checks super_tool_tier (INTEGER) instead of is_super_tool (BYTE)
    // ========================================================================
    private boolean isDefinitiveNetheriteSuperTool(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();

        // Check for super_tool_tier key (v1.1.0 tiered system)
        Integer tier = pdc.get(superTierKey, PersistentDataType.INTEGER);
        return tier != null && tier >= 3;
    }

    // ========================================================================
    // BUG 3 FIX: Add a hidden vanilla enchantment for glint effect
    // ========================================================================
    private void addEnchantGlint(ItemMeta meta) {
        // Only add if item doesn't already have vanilla enchantments
        if (!meta.hasEnchants()) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
    }

    // ========================================================================
    // BUG 6 FIX: Enchantment descriptions
    // ========================================================================
    private String getEnchantDescription(EnchantmentType type, int level) {
        return switch (type) {
            // --- Sword ---
            case VAMPIRISM -> "Grants Regeneration " + toRoman(getVampirismRegenLevel(level)) + " on hit";
            case BLEED -> "Inflicts bleeding damage over " + level + "s";
            case VENOMSTRIKE -> "Poisons target on hit for " + (level <= 1 ? "3s" : "5s");
            case LIFESTEAL -> "Heals " + (level * 5) + "% of damage dealt";
            case CHAIN_LIGHTNING -> "Chains lightning to " + (level + 1) + " nearby enemies within "
                + switch (level) { case 1 -> "4"; case 2 -> "5"; default -> "6"; } + " blocks";

            // --- Axe ---
            case BERSERKER -> "Bonus damage based on missing health";
            case LUMBERJACK -> (level * 10) + "% chance to disable shields";
            case CLEAVE -> "Deals " + switch (level) { case 1 -> "20%"; case 2 -> "35%"; default -> "50%"; }
                + " AoE damage";
            case TIMBER -> "Chops connected logs (max "
                + switch (level) { case 1 -> "8"; case 2 -> "16"; default -> "64"; } + ")";
            case GUILLOTINE -> switch (level) { case 1 -> "3%"; case 2 -> "6%"; default -> "10%"; }
                + " instant kill below 30% HP";

            // --- Pickaxe ---
            case VEINMINER -> "Mines connected ores (max "
                + switch (level) { case 1 -> "8"; case 2 -> "16"; default -> "32"; } + ")";
            case DRILL -> "Mines a "
                + switch (level) { case 1 -> "1x3"; case 2 -> "3x3"; default -> "3x3x2"; } + " area";
            case AUTO_SMELT -> "Automatically smelts mined ores";
            case MAGNETISM -> "Pulls nearby items to you";
            case GRAVITY_WELL -> "Pulls mined blocks inward (radius "
                + switch (level) { case 1 -> "3"; case 2 -> "5"; default -> "7"; } + ")";

            // --- Shovel ---
            case EXCAVATOR -> "Digs a "
                + switch (level) { case 1 -> "1x3"; case 2 -> "3x3"; default -> "3x3x2"; } + " area";
            case REPLENISH -> "Chance to find seeds and shards";

            // --- Hoe ---
            case HARVESTER -> "Harvests crops in a " + level + " block radius";
            case GREEN_THUMB -> "Tills soil and applies bonemeal in area";

            // --- Sickle ---
            case SOUL_REAP -> "Gain Strength on kill (duration "
                + switch (level) { case 1 -> "3s"; case 2 -> "5s"; default -> "7s"; } + ")";
            case BLOOD_PRICE -> "+" + (0.5 + level * 0.5) + " bonus damage, costs 1 HP";
            case REAPERS_MARK -> "Marks target for +"
                + (level == 1 ? "10%" : "15%") + " damage";
            case WITHER_TOUCH -> "Applies Wither " + toRoman(level) + " on hit";
            case HARVESTING_MOON -> "+" + (level * 50) + "% bonus XP from mob kills";

            // --- Trident ---
            case THUNDERLORD -> switch (level) {
                    case 1 -> "15%"; case 2 -> "25%"; case 3 -> "35%";
                    case 4 -> "45%"; default -> "55%";
                } + " chance to summon lightning";
            case SWIFTNESS -> "Grants Speed" + (level >= 3 ? " II" : " I") + " while holding trident";
            case VITALITY -> "Grants +" + (level * 2) + " max HP while holding trident";
            case TIDAL_WAVE -> "Knocks back targets and grants Dolphin's Grace";

            // --- Universal Melee ---
            case FROSTBITE -> "Applies Slowness on hit for " + (level * 0.5) + "s";

            // --- Bow & Crossbow ---
            case EXPLOSIVE_ARROW -> "Arrows explode on impact (radius "
                + switch (level) { case 1 -> "1"; case 2 -> "1.5"; default -> "2"; } + ")";
            case HOMING -> "Arrows track nearby enemies (range "
                + (level == 1 ? "5" : "8") + ")";
            case RAPIDFIRE -> "Reduces crossbow draw time by " + (level * 15) + "%";
            case SNIPER -> "+" + (level * 10) + "% bonus damage at 20+ blocks";

            // --- Helmet ---
            case NIGHT_VISION -> "Permanent Night Vision effect";
            case AQUA_LUNGS -> "Water Breathing" + (level >= 2 ? " + Conduit Power" : "");

            // --- Chestplate ---
            case FORTIFICATION -> switch (level) { case 1 -> "5%"; case 2 -> "8%"; default -> "12%"; }
                + " damage reduction";
            case DEFLECTION -> (level == 1 ? "10%" : "20%") + " chance to reflect projectiles";

            // --- Leggings ---
            case SWIFTFOOT -> "Grants Speed" + (level >= 3 ? " II" : " I");
            case DODGE -> (level == 1 ? "8%" : "15%") + " chance to dodge melee attacks";

            // --- Boots ---
            case LEAPING -> "Grants Jump Boost " + toRoman(level);
            case STOMP -> "Converts fall damage to AoE (" + (level * 2) + " dmg)";
            case MOMENTUM -> "+" + (level * 5) + "% movement speed while sprinting";

            // --- Elytra ---
            case CUSHION -> "Negates elytra crash and fall damage";
            case BOOST -> "Sneak while gliding for a speed boost";
            case GLIDER -> "50% less elytra durability loss";

            // --- Universal ---
            case SOULBOUND -> "Item is kept on death";

            // --- Dog Armor ---
            case BEST_BUDDIES -> "Wolf takes/deals 95% less damage, gets Regen II";
        };
    }

    /**
     * BUG 4 FIX: Returns the Regeneration amplifier for the given Vampirism level.
     * I→0 (Regen I), II→1 (Regen II), III→1 (Regen II), IV→2 (Regen III), V→3 (Regen IV)
     */
    private int getVampirismRegenLevel(int vampirismLevel) {
        return switch (vampirismLevel) {
            case 1 -> 1;  // Regen I   (amplifier 0)
            case 2 -> 2;  // Regen II  (amplifier 1)
            case 3 -> 2;  // Regen II  (amplifier 1)
            case 4 -> 3;  // Regen III (amplifier 2)
            default -> 4; // Regen IV  (amplifier 3)
        };
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
            case VEINMINER -> enchantManager.hasEnchant(tool, EnchantmentType.DRILL);
            case DRILL -> enchantManager.hasEnchant(tool, EnchantmentType.VEINMINER);
            case GRAVITY_WELL -> vanillaEnchants.containsKey(Enchantment.SILK_TOUCH);
            case CHAIN_LIGHTNING -> vanillaEnchants.containsKey(Enchantment.FIRE_ASPECT);
            case MOMENTUM -> vanillaEnchants.containsKey(Enchantment.FROST_WALKER);
            default -> false;
        };
    }

    // ========================================================================
    // UTILITY METHODS
    // ========================================================================
    private NamedTextColor getEnchantColor(EnchantmentType type) {
        return switch (type) {
            case VAMPIRISM, BLEED, BLOOD_PRICE -> NamedTextColor.RED;
            case BERSERKER, LIFESTEAL -> NamedTextColor.DARK_RED;
            case VENOMSTRIKE, WITHER_TOUCH -> NamedTextColor.DARK_GREEN;
            case FROSTBITE -> NamedTextColor.AQUA;
            case THUNDERLORD, CHAIN_LIGHTNING -> NamedTextColor.YELLOW;
            case SOULBOUND -> NamedTextColor.LIGHT_PURPLE;
            case SOUL_REAP, REAPERS_MARK -> NamedTextColor.DARK_PURPLE;
            case GRAVITY_WELL -> NamedTextColor.DARK_AQUA;
            case MOMENTUM -> NamedTextColor.WHITE;
            case HARVESTING_MOON -> NamedTextColor.GOLD;
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
    // INNER RECORD: Recipe definition
    // ========================================================================
    private record AnvilRecipe(
        Enchantment requiredEnchant,
        Material ingredient,
        boolean levelMatches,
        int maxLevel
    ) {}
}

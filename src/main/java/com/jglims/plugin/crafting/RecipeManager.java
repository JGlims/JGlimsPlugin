package com.jglims.plugin.crafting;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.enchantments.CustomEnchantManager;
import com.jglims.plugin.enchantments.EnchantmentType;
import com.jglims.plugin.weapons.SickleManager;
import com.jglims.plugin.weapons.SuperToolManager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class RecipeManager implements Listener {

    private final JGlimsPlugin plugin;
    private final SickleManager sickleManager;
    private final SuperToolManager superToolManager;

    public RecipeManager(JGlimsPlugin plugin, SickleManager sickleManager, SuperToolManager superToolManager) {
        this.plugin = plugin;
        this.sickleManager = sickleManager;
        this.superToolManager = superToolManager;
    }

    public void registerAllRecipes() {
        registerEyeOfEnder();
        registerTotemOfUndying();
        registerEnchantedGoldenApple();
        registerBlessings();
        registerSickles();
        registerBestBuddies();
        // Note: Super Tools use generic recipes handled via PrepareItemCraftEvent
        // because there are too many tool types to register individually
        registerSuperToolRecipes();
        plugin.getLogger().info("All custom crafting recipes registered.");
    }

    // ========================================================================
    // EYE OF ENDER (replaces vanilla — vanilla removed in VanillaRecipeRemover)
    // [ Ghast Tear   ] [Prismarine Sh.] [ Ghast Tear   ]
    // [ Echo Shard   ] [ Ender Pearl  ] [ Blaze Powder ]
    // [ Ghast Tear   ] [ Wind Charge  ] [ Ghast Tear   ]
    // ========================================================================
    private void registerEyeOfEnder() {
        NamespacedKey key = new NamespacedKey(plugin, "custom_ender_eye");
        ShapedRecipe recipe = new ShapedRecipe(key, new ItemStack(Material.ENDER_EYE, 1));
        recipe.shape("GPG", "EYB", "GWG");
        recipe.setIngredient('G', Material.GHAST_TEAR);
        recipe.setIngredient('P', Material.PRISMARINE_SHARD);
        recipe.setIngredient('E', Material.ECHO_SHARD);
        recipe.setIngredient('Y', Material.ENDER_PEARL);
        recipe.setIngredient('B', Material.BLAZE_POWDER);
        recipe.setIngredient('W', Material.WIND_CHARGE);
        plugin.getServer().addRecipe(recipe);
    }

    // ========================================================================
    // TOTEM OF UNDYING
    // [ Gold Nugget ] [ Gold Nugget ] [ Gold Nugget ]
    // [   Emerald   ] [  Gold Ingot ] [   Emerald   ]
    // [ Gold Nugget ] [ Gold Nugget ] [ Gold Nugget ]
    // ========================================================================
    private void registerTotemOfUndying() {
        NamespacedKey key = new NamespacedKey(plugin, "custom_totem");
        ShapedRecipe recipe = new ShapedRecipe(key, new ItemStack(Material.TOTEM_OF_UNDYING, 1));
        recipe.shape("NNN", "EGE", "NNN");
        recipe.setIngredient('N', Material.GOLD_NUGGET);
        recipe.setIngredient('E', Material.EMERALD);
        recipe.setIngredient('G', Material.GOLD_INGOT);
        plugin.getServer().addRecipe(recipe);
    }

    // ========================================================================
    // ENCHANTED GOLDEN APPLE
    // [ Gold Block   ] [ Torchflower  ] [ Gold Block   ]
    // [ Torchflower  ] [    Apple     ] [ Torchflower  ]
    // [ Gold Block   ] [ Torchflower  ] [ Gold Block   ]
    // ========================================================================
    private void registerEnchantedGoldenApple() {
        NamespacedKey key = new NamespacedKey(plugin, "custom_enchanted_golden_apple");
        ShapedRecipe recipe = new ShapedRecipe(key, new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 1));
        recipe.shape("GTG", "TAT", "GTG");
        recipe.setIngredient('G', Material.GOLD_BLOCK);
        recipe.setIngredient('T', Material.TORCHFLOWER);
        recipe.setIngredient('A', Material.APPLE);
        plugin.getServer().addRecipe(recipe);
    }

    // ========================================================================
    // BLESSINGS
    // All three share the same shape, differ only in the bottom-center ingredient:
    // [ Gold Block ] [ Torchflower ] [ Gold Block ]
    // [ Diamond    ] [ Smithing T. ] [ Diamond    ]
    // [ Gold Ingot ] [  *varies*   ] [ Gold Ingot ]
    //
    // C's Bless:  Melon Slice   -> Glistering Melon, gold name
    // Ami's Bless: Carrot       -> Golden Carrot, red name
    // La's Bless:  Apple        -> Golden Apple, blue name
    // ========================================================================
    private void registerBlessings() {
        registerBlessing("c_bless", Material.MELON_SLICE, Material.GLISTERING_MELON_SLICE,
            "C's Bless", NamedTextColor.GOLD);
        registerBlessing("ami_bless", Material.CARROT, Material.GOLDEN_CARROT,
            "Ami's Bless", NamedTextColor.RED);
        registerBlessing("la_bless", Material.APPLE, Material.GOLDEN_APPLE,
            "La's Bless", NamedTextColor.BLUE);
    }

    private void registerBlessing(String id, Material bottomCenter, Material resultMaterial,
                                   String displayName, NamedTextColor color) {
        NamespacedKey key = new NamespacedKey(plugin, id);

        // Create the result item with custom name and PDC tag
        ItemStack result = new ItemStack(resultMaterial, 1);
        ItemMeta meta = result.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(displayName, color)
                .decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Right-click to consume", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Permanent stat boost", NamedTextColor.DARK_PURPLE)
                .decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
            meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, id + "_item"), PersistentDataType.BYTE, (byte) 1);
            result.setItemMeta(meta);
        }

        ShapedRecipe recipe = new ShapedRecipe(key, result);
        recipe.shape("GTG", "DSd", "gBg");
        recipe.setIngredient('G', Material.GOLD_BLOCK);
        recipe.setIngredient('T', Material.TORCHFLOWER);
        recipe.setIngredient('D', Material.DIAMOND);
        recipe.setIngredient('S', Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE);
        recipe.setIngredient('d', Material.DIAMOND);
        recipe.setIngredient('g', Material.GOLD_INGOT);
        recipe.setIngredient('B', bottomCenter);
        plugin.getServer().addRecipe(recipe);
    }

    // ========================================================================
    // SICKLES — 6 tiers
    // Hoe in center, surrounded by 8x material
    // ========================================================================
    private void registerSickles() {
        for (Material hoeMat : sickleManager.getSickleTiers()) {
            Material ingredient = sickleManager.getSickleIngredient(hoeMat);
            if (ingredient == null) continue;

            ItemStack sickle = sickleManager.createSickle(hoeMat);
            if (sickle == null) continue;

            String tierName = hoeMat.name().replace("_HOE", "").toLowerCase();
            NamespacedKey key = new NamespacedKey(plugin, "sickle_" + tierName);

            ShapedRecipe recipe = new ShapedRecipe(key, sickle);
            recipe.shape("MMM", "MHM", "MMM");
            recipe.setIngredient('M', ingredient);
            recipe.setIngredient('H', hoeMat);
            plugin.getServer().addRecipe(recipe);
        }
    }

    // ========================================================================
    // BEST BUDDIES — Shapeless: Bone + Diamond
    // Result: Wolf Armor with BestBuddies enchantment
    // ========================================================================
    private void registerBestBuddies() {
        NamespacedKey key = new NamespacedKey(plugin, "best_buddies");

        ItemStack result = new ItemStack(Material.WOLF_ARMOR, 1);
        ItemMeta meta = result.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Best Buddies Armor", NamedTextColor.LIGHT_PURPLE)
                .decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Best Buddies I", NamedTextColor.LIGHT_PURPLE)
                .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Wolf takes 95% less damage", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Wolf deals 95% less damage", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Wolf gets Regen II", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);

            // Add BestBuddies enchantment via PDC
            CustomEnchantManager enchantManager = plugin.getEnchantManager();
            meta.getPersistentDataContainer().set(
                enchantManager.getKey(EnchantmentType.BEST_BUDDIES),
                PersistentDataType.INTEGER, 1);
            result.setItemMeta(meta);
        }

        ShapelessRecipe recipe = new ShapelessRecipe(key, result);
        recipe.addIngredient(Material.BONE);
        recipe.addIngredient(Material.DIAMOND);
        plugin.getServer().addRecipe(recipe);
    }

    // ========================================================================
    // SUPER TOOLS — Generic shaped recipes
    // Non-netherite: tool center + 8x same-tier material
    // Netherite: tool center + 4x Diamond (corners) + 4x Netherite Ingot (edges)
    // Elytra: elytra center + 8x Diamond
    // ========================================================================
    private void registerSuperToolRecipes() {
        // Register non-netherite super tools for common tool types
        Material[][] nonNetheriteTools = {
            // {tool, ingredient}
            {Material.WOODEN_SWORD, Material.OAK_PLANKS},
            {Material.WOODEN_PICKAXE, Material.OAK_PLANKS},
            {Material.WOODEN_AXE, Material.OAK_PLANKS},
            {Material.WOODEN_SHOVEL, Material.OAK_PLANKS},
            {Material.WOODEN_HOE, Material.OAK_PLANKS},
            {Material.STONE_SWORD, Material.COBBLESTONE},
            {Material.STONE_PICKAXE, Material.COBBLESTONE},
            {Material.STONE_AXE, Material.COBBLESTONE},
            {Material.STONE_SHOVEL, Material.COBBLESTONE},
            {Material.STONE_HOE, Material.COBBLESTONE},
            {Material.IRON_SWORD, Material.IRON_INGOT},
            {Material.IRON_PICKAXE, Material.IRON_INGOT},
            {Material.IRON_AXE, Material.IRON_INGOT},
            {Material.IRON_SHOVEL, Material.IRON_INGOT},
            {Material.IRON_HOE, Material.IRON_INGOT},
            {Material.GOLDEN_SWORD, Material.GOLD_INGOT},
            {Material.GOLDEN_PICKAXE, Material.GOLD_INGOT},
            {Material.GOLDEN_AXE, Material.GOLD_INGOT},
            {Material.GOLDEN_SHOVEL, Material.GOLD_INGOT},
            {Material.GOLDEN_HOE, Material.GOLD_INGOT},
            {Material.DIAMOND_SWORD, Material.DIAMOND},
            {Material.DIAMOND_PICKAXE, Material.DIAMOND},
            {Material.DIAMOND_AXE, Material.DIAMOND},
            {Material.DIAMOND_SHOVEL, Material.DIAMOND},
            {Material.DIAMOND_HOE, Material.DIAMOND},
            {Material.BOW, Material.DIAMOND},
            {Material.CROSSBOW, Material.DIAMOND},
            {Material.SHIELD, Material.DIAMOND},
            {Material.TRIDENT, Material.DIAMOND},
        };

        for (Material[] pair : nonNetheriteTools) {
            Material toolMat = pair[0];
            Material ingredient = pair[1];

            ItemStack superResult = superToolManager.createSuperTool(new ItemStack(toolMat));
            if (superResult == null) continue;

            String name = toolMat.name().toLowerCase();
            NamespacedKey key = new NamespacedKey(plugin, "super_" + name);

            ShapedRecipe recipe = new ShapedRecipe(key, superResult);
            recipe.shape("MMM", "MTM", "MMM");
            recipe.setIngredient('M', ingredient);
            recipe.setIngredient('T', toolMat);
            plugin.getServer().addRecipe(recipe);
        }

        // Netherite super tools: 4x Diamond corners + 4x Netherite Ingot edges + tool center
        Material[] netheriteTools = {
            Material.NETHERITE_SWORD, Material.NETHERITE_PICKAXE, Material.NETHERITE_AXE,
            Material.NETHERITE_SHOVEL, Material.NETHERITE_HOE
        };

        for (Material toolMat : netheriteTools) {
            ItemStack superResult = superToolManager.createSuperTool(new ItemStack(toolMat));
            if (superResult == null) continue;

            String name = toolMat.name().toLowerCase();
            NamespacedKey key = new NamespacedKey(plugin, "super_" + name);

            ShapedRecipe recipe = new ShapedRecipe(key, superResult);
            recipe.shape("DND", "NTN", "DND");
            recipe.setIngredient('D', Material.DIAMOND);
            recipe.setIngredient('N', Material.NETHERITE_INGOT);
            recipe.setIngredient('T', toolMat);
            plugin.getServer().addRecipe(recipe);
        }

        // Super Elytra: 8x Diamond + Elytra center
        ItemStack superElytra = superToolManager.createSuperTool(new ItemStack(Material.ELYTRA));
        if (superElytra != null) {
            NamespacedKey key = new NamespacedKey(plugin, "super_elytra");
            ShapedRecipe recipe = new ShapedRecipe(key, superElytra);
            recipe.shape("DDD", "DED", "DDD");
            recipe.setIngredient('D', Material.DIAMOND);
            recipe.setIngredient('E', Material.ELYTRA);
            plugin.getServer().addRecipe(recipe);
        }
    }

    // ========================================================================
    // PREPARE CRAFT EVENT — Prevent sickles from being used as regular hoes
    // in vanilla recipes, and handle super tool enchantment preservation
    // ========================================================================
    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        if (event.getRecipe() == null) return;

        // If any ingredient is a sickle being used in a non-sickle recipe, block it
        for (ItemStack ingredient : event.getInventory().getMatrix()) {
            if (ingredient != null && sickleManager.isSickle(ingredient)) {
                // Check if this is one of our sickle recipes
                if (event.getRecipe() instanceof ShapedRecipe shaped) {
                    if (shaped.getKey().getNamespace().equals(plugin.getName().toLowerCase())) {
                        continue; // Our recipe, allow it
                    }
                }
                // Block sickles from vanilla recipes
                event.getInventory().setResult(null);
                return;
            }
        }
    }
}

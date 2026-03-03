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
import com.jglims.plugin.weapons.BattleAxeManager;
import com.jglims.plugin.weapons.BattleBowManager;
import com.jglims.plugin.weapons.BattleMaceManager;
import com.jglims.plugin.weapons.SickleManager;
import com.jglims.plugin.weapons.SuperToolManager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class RecipeManager implements Listener {

    private final JGlimsPlugin plugin;
    private final SickleManager sickleManager;
    private final BattleAxeManager battleAxeManager;
    private final BattleBowManager battleBowManager;
    private final BattleMaceManager battleMaceManager;
    private final SuperToolManager superToolManager;

    public RecipeManager(JGlimsPlugin plugin, SickleManager sickleManager,
                         BattleAxeManager battleAxeManager, BattleBowManager battleBowManager,
                         BattleMaceManager battleMaceManager, SuperToolManager superToolManager) {
        this.plugin = plugin;
        this.sickleManager = sickleManager;
        this.battleAxeManager = battleAxeManager;
        this.battleBowManager = battleBowManager;
        this.battleMaceManager = battleMaceManager;
        this.superToolManager = superToolManager;
    }

    public void registerAllRecipes() {
        registerEyeOfEnder();
        registerTotemOfUndying();
        registerEnchantedGoldenApple();
        registerBlessings();
        registerSickles();
        registerBestBuddies();
        registerBattleAxes();
        registerBattleBow();
        registerBattleCrossbow();
        registerBattleMace();
        registerSuperToolRecipes();
        plugin.getLogger().info("All custom crafting recipes registered.");
    }

    // ========================================================================
    // EYE OF ENDER
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
        ItemStack result = new ItemStack(resultMaterial, 1);
        ItemMeta meta = result.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(displayName, color).decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Right-click to consume", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Permanent stat boost", NamedTextColor.DARK_PURPLE).decoration(TextDecoration.ITALIC, false));
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
    // SICKLES
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
    // BEST BUDDIES
    // ========================================================================
    private void registerBestBuddies() {
        NamespacedKey key = new NamespacedKey(plugin, "best_buddies");
        ItemStack result = new ItemStack(Material.WOLF_ARMOR, 1);
        ItemMeta meta = result.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Best Buddies Armor", NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Best Buddies I", NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Wolf takes 95% less damage", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Wolf deals 95% less damage", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Wolf gets Regen II", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
            CustomEnchantManager enchantManager = plugin.getEnchantManager();
            meta.getPersistentDataContainer().set(
                enchantManager.getKey(EnchantmentType.BEST_BUDDIES), PersistentDataType.INTEGER, 1);
            result.setItemMeta(meta);
        }
        ShapelessRecipe recipe = new ShapelessRecipe(key, result);
        recipe.addIngredient(Material.BONE);
        recipe.addIngredient(Material.DIAMOND);
        plugin.getServer().addRecipe(recipe);
    }

    // ========================================================================
    // BATTLE AXES
    // ========================================================================
    private void registerBattleAxes() {
        for (Material axeMat : battleAxeManager.getBattleAxeTiers()) {
            ItemStack battleAxe = battleAxeManager.createBattleAxe(axeMat);
            if (battleAxe == null) continue;
            String tierName = axeMat.name().replace("_AXE", "").toLowerCase();
            NamespacedKey key = new NamespacedKey(plugin, "battle_axe_" + tierName);

            if (axeMat == Material.NETHERITE_AXE) {
                ShapedRecipe recipe = new ShapedRecipe(key, battleAxe);
                recipe.shape("NNN", "NAN", "NNN");
                recipe.setIngredient('N', Material.NETHERITE_INGOT);
                recipe.setIngredient('A', Material.NETHERITE_AXE);
                plugin.getServer().addRecipe(recipe);
            } else {
                Material ingredient = battleAxeManager.getBattleAxeIngredient(axeMat);
                Material block = battleAxeManager.getBattleAxeBlock(axeMat);
                if (ingredient == null || block == null) continue;
                ShapedRecipe recipe = new ShapedRecipe(key, battleAxe);
                recipe.shape("MBM", "MAM", "MMM");
                recipe.setIngredient('M', ingredient);
                recipe.setIngredient('B', block);
                recipe.setIngredient('A', axeMat);
                plugin.getServer().addRecipe(recipe);
            }
        }
    }

    // ========================================================================
    // BATTLE BOW
    // ========================================================================
    private void registerBattleBow() {
        ItemStack battleBow = battleBowManager.createBattleBow();
        if (battleBow == null) return;
        NamespacedKey key = new NamespacedKey(plugin, "battle_bow");
        ShapedRecipe recipe = new ShapedRecipe(key, battleBow);
        recipe.shape("III", "IBI", "III");
        recipe.setIngredient('I', Material.IRON_INGOT);
        recipe.setIngredient('B', Material.BOW);
        plugin.getServer().addRecipe(recipe);
    }

    // ========================================================================
    // BATTLE CROSSBOW
    // ========================================================================
    private void registerBattleCrossbow() {
        ItemStack battleCrossbow = battleBowManager.createBattleCrossbow();
        if (battleCrossbow == null) return;
        NamespacedKey key = new NamespacedKey(plugin, "battle_crossbow");
        ShapedRecipe recipe = new ShapedRecipe(key, battleCrossbow);
        recipe.shape("III", "ICI", "III");
        recipe.setIngredient('I', Material.IRON_INGOT);
        recipe.setIngredient('C', Material.CROSSBOW);
        plugin.getServer().addRecipe(recipe);
    }

    // ========================================================================
    // BATTLE MACE
    // ========================================================================
    private void registerBattleMace() {
        ItemStack battleMace = battleMaceManager.createBattleMace();
        if (battleMace == null) return;
        NamespacedKey key = new NamespacedKey(plugin, "battle_mace");
        ShapedRecipe recipe = new ShapedRecipe(key, battleMace);
        recipe.shape("BBB", "BMB", "BBB");
        recipe.setIngredient('B', Material.BREEZE_ROD);
        recipe.setIngredient('M', Material.MACE);
        plugin.getServer().addRecipe(recipe);
    }

    // ========================================================================
    // SUPER TOOL RECIPES
    // FIXED: Only registers super versions for valid items:
    //   - Swords, Pickaxes, Shovels, Trident, Shield, Elytra (directly)
    //   - Sickles (via hoe material + sickle PDC check at craft time)
    //   - Battle Axes (via axe material + battle axe PDC check at craft time)
    //   - Battle Bow, Battle Crossbow (special recipes)
    //   - NO regular axes, NO regular hoes, NO regular bows/crossbows
    // ========================================================================
    private void registerSuperToolRecipes() {
        // === STANDARD SUPER TOOLS (no conflicts) ===
        Material[] standardSuperTools = {
            Material.WOODEN_SWORD, Material.STONE_SWORD, Material.IRON_SWORD,
            Material.GOLDEN_SWORD, Material.DIAMOND_SWORD, Material.NETHERITE_SWORD,
            Material.WOODEN_PICKAXE, Material.STONE_PICKAXE, Material.IRON_PICKAXE,
            Material.GOLDEN_PICKAXE, Material.DIAMOND_PICKAXE, Material.NETHERITE_PICKAXE,
            Material.WOODEN_SHOVEL, Material.STONE_SHOVEL, Material.IRON_SHOVEL,
            Material.GOLDEN_SHOVEL, Material.DIAMOND_SHOVEL, Material.NETHERITE_SHOVEL,
            Material.TRIDENT, Material.SHIELD, Material.ELYTRA
        };

        for (Material toolMat : standardSuperTools) {
            registerSuperTierRecipes(toolMat);
        }

        // === SICKLE SUPER TOOLS (hoe materials, but only sickles can be super) ===
        // We register them using the sickle item as the result base
        for (Material hoeMat : sickleManager.getSickleTiers()) {
            ItemStack sickle = sickleManager.createSickle(hoeMat);
            if (sickle == null) continue;
            String name = "sickle_" + hoeMat.name().toLowerCase();

            // Iron tier
            ItemStack ironResult = superToolManager.createSuperTool(sickle.clone(), SuperToolManager.TIER_IRON);
            if (ironResult != null) {
                NamespacedKey key = new NamespacedKey(plugin, "super_iron_" + name);
                ShapedRecipe recipe = new ShapedRecipe(key, ironResult);
                recipe.shape("MMM", "MTM", "MMM");
                recipe.setIngredient('M', Material.IRON_INGOT);
                recipe.setIngredient('T', hoeMat);
                plugin.getServer().addRecipe(recipe);
            }

            // Diamond tier
            ItemStack diamondResult = superToolManager.createSuperTool(sickle.clone(), SuperToolManager.TIER_DIAMOND);
            if (diamondResult != null) {
                NamespacedKey key = new NamespacedKey(plugin, "super_diamond_" + name);
                ShapedRecipe recipe = new ShapedRecipe(key, diamondResult);
                recipe.shape("MMM", "MTM", "MMM");
                recipe.setIngredient('M', Material.DIAMOND);
                recipe.setIngredient('T', hoeMat);
                plugin.getServer().addRecipe(recipe);
            }

            // Netherite tier
            ItemStack netheriteResult = superToolManager.createSuperTool(sickle.clone(), SuperToolManager.TIER_NETHERITE);
            if (netheriteResult != null) {
                NamespacedKey key = new NamespacedKey(plugin, "super_netherite_" + name);
                ShapedRecipe recipe = new ShapedRecipe(key, netheriteResult);
                recipe.shape("MMM", "MTM", "MMM");
                recipe.setIngredient('M', Material.NETHERITE_INGOT);
                recipe.setIngredient('T', hoeMat);
                plugin.getServer().addRecipe(recipe);
            }
        }

        // === BATTLE AXE SUPER TOOLS (axe materials, but only battle axes can be super) ===
        for (Material axeMat : battleAxeManager.getBattleAxeTiers()) {
            ItemStack battleAxe = battleAxeManager.createBattleAxe(axeMat);
            if (battleAxe == null) continue;
            String name = "battle_axe_" + axeMat.name().toLowerCase();

            // Iron tier
            ItemStack ironResult = superToolManager.createSuperTool(battleAxe.clone(), SuperToolManager.TIER_IRON);
            if (ironResult != null) {
                NamespacedKey key = new NamespacedKey(plugin, "super_iron_" + name);
                ShapedRecipe recipe = new ShapedRecipe(key, ironResult);
                recipe.shape("MMM", "MTM", "MMM");
                recipe.setIngredient('M', Material.IRON_INGOT);
                recipe.setIngredient('T', axeMat);
                plugin.getServer().addRecipe(recipe);
            }

            // Diamond tier
            ItemStack diamondResult = superToolManager.createSuperTool(battleAxe.clone(), SuperToolManager.TIER_DIAMOND);
            if (diamondResult != null) {
                NamespacedKey key = new NamespacedKey(plugin, "super_diamond_" + name);
                ShapedRecipe recipe = new ShapedRecipe(key, diamondResult);
                recipe.shape("MMM", "MTM", "MMM");
                recipe.setIngredient('M', Material.DIAMOND);
                recipe.setIngredient('T', axeMat);
                plugin.getServer().addRecipe(recipe);
            }

            // Netherite tier
            ItemStack netheriteResult = superToolManager.createSuperTool(battleAxe.clone(), SuperToolManager.TIER_NETHERITE);
            if (netheriteResult != null) {
                NamespacedKey key = new NamespacedKey(plugin, "super_netherite_" + name);
                ShapedRecipe recipe = new ShapedRecipe(key, netheriteResult);
                recipe.shape("MMM", "MTM", "MMM");
                recipe.setIngredient('M', Material.NETHERITE_INGOT);
                recipe.setIngredient('T', axeMat);
                plugin.getServer().addRecipe(recipe);
            }
        }

        // === SUPER BATTLE BOW (Iron tier — 8 string + bow center) ===
        ItemStack superBattleBowIron = superToolManager.createSuperTool(
            battleBowManager.createBattleBow(), SuperToolManager.TIER_IRON);
        if (superBattleBowIron != null) {
            NamespacedKey key = new NamespacedKey(plugin, "super_iron_battle_bow");
            ShapedRecipe recipe = new ShapedRecipe(key, superBattleBowIron);
            recipe.shape("SSS", "SBS", "SSS");
            recipe.setIngredient('S', Material.STRING);
            recipe.setIngredient('B', Material.BOW);
            plugin.getServer().addRecipe(recipe);
        }

        // === SUPER BATTLE CROSSBOW (Iron tier — 8 string + crossbow center) ===
        ItemStack superBattleCrossbowIron = superToolManager.createSuperTool(
            battleBowManager.createBattleCrossbow(), SuperToolManager.TIER_IRON);
        if (superBattleCrossbowIron != null) {
            NamespacedKey key = new NamespacedKey(plugin, "super_iron_battle_crossbow");
            ShapedRecipe recipe = new ShapedRecipe(key, superBattleCrossbowIron);
            recipe.shape("SSS", "SCS", "SSS");
            recipe.setIngredient('S', Material.STRING);
            recipe.setIngredient('C', Material.CROSSBOW);
            plugin.getServer().addRecipe(recipe);
        }
    }

    /**
     * Helper: registers Iron, Diamond, and Netherite super tier recipes for a standard tool.
     */
    private void registerSuperTierRecipes(Material toolMat) {
        String name = toolMat.name().toLowerCase();

        // Iron tier
        ItemStack ironResult = superToolManager.createSuperTool(new ItemStack(toolMat), SuperToolManager.TIER_IRON);
        if (ironResult != null) {
            NamespacedKey key = new NamespacedKey(plugin, "super_iron_" + name);
            ShapedRecipe recipe = new ShapedRecipe(key, ironResult);
            recipe.shape("MMM", "MTM", "MMM");
            recipe.setIngredient('M', Material.IRON_INGOT);
            recipe.setIngredient('T', toolMat);
            plugin.getServer().addRecipe(recipe);
        }

        // Diamond tier
        ItemStack diamondResult = superToolManager.createSuperTool(new ItemStack(toolMat), SuperToolManager.TIER_DIAMOND);
        if (diamondResult != null) {
            NamespacedKey key = new NamespacedKey(plugin, "super_diamond_" + name);
            ShapedRecipe recipe = new ShapedRecipe(key, diamondResult);
            recipe.shape("MMM", "MTM", "MMM");
            recipe.setIngredient('M', Material.DIAMOND);
            recipe.setIngredient('T', toolMat);
            plugin.getServer().addRecipe(recipe);
        }

        // Netherite tier
        ItemStack netheriteResult = superToolManager.createSuperTool(new ItemStack(toolMat), SuperToolManager.TIER_NETHERITE);
        if (netheriteResult != null) {
            NamespacedKey key = new NamespacedKey(plugin, "super_netherite_" + name);
            ShapedRecipe recipe = new ShapedRecipe(key, netheriteResult);
            recipe.shape("MMM", "MTM", "MMM");
            recipe.setIngredient('M', Material.NETHERITE_INGOT);
            recipe.setIngredient('T', toolMat);
            plugin.getServer().addRecipe(recipe);
        }
    }

    // ========================================================================
    // PREPARE CRAFT EVENT
    // Handles:
    //  1. Blocking custom weapons from vanilla recipes
    //  2. Ensuring only SICKLES craft from hoe+surround (not plain hoes)
    //  3. Ensuring only BATTLE AXES craft from axe+surround (not plain axes)
    //  4. Super tool upgrade via crafting table
    // ========================================================================
    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        if (event.getRecipe() == null) return;

        ItemStack[] matrix = event.getInventory().getMatrix();

        // --- Block custom weapons from non-plugin recipes ---
        for (ItemStack ingredient : matrix) {
            if (ingredient != null) {
                if (sickleManager.isSickle(ingredient) || battleAxeManager.isBattleAxe(ingredient)
                    || battleMaceManager.isBattleMace(ingredient)) {
                    if (event.getRecipe() instanceof ShapedRecipe shaped) {
                        if (shaped.getKey().getNamespace().equals(plugin.getName().toLowerCase())) {
                            continue;
                        }
                    }
                    if (event.getRecipe() instanceof ShapelessRecipe shapeless) {
                        if (shapeless.getKey().getNamespace().equals(plugin.getName().toLowerCase())) {
                            continue;
                        }
                    }
                    event.getInventory().setResult(null);
                    return;
                }
            }
        }

        // --- Super Sickle/Battle Axe crafting guard ---
        // If the recipe result is a super tool based on hoe/axe material,
        // verify the center item is actually a sickle/battle axe
        if (event.getRecipe() instanceof ShapedRecipe shaped) {
            String recipeKey = shaped.getKey().getKey();

            // Check if this is a super sickle recipe
            if (recipeKey.startsWith("super_") && recipeKey.contains("sickle_")) {
                ItemStack centerItem = matrix.length >= 5 ? matrix[4] : null;
                if (centerItem != null && !sickleManager.isSickle(centerItem)) {
                    // Center is a plain hoe, not a sickle — block the craft
                    event.getInventory().setResult(null);
                    return;
                }
            }

            // Check if this is a super battle axe recipe
            if (recipeKey.startsWith("super_") && recipeKey.contains("battle_axe_")) {
                ItemStack centerItem = matrix.length >= 5 ? matrix[4] : null;
                if (centerItem != null && !battleAxeManager.isBattleAxe(centerItem)) {
                    // Center is a plain axe, not a battle axe — block the craft
                    event.getInventory().setResult(null);
                    return;
                }
            }
        }

        // --- Super tool upgrade via crafting table ---
        ItemStack centerItem = matrix.length >= 5 ? matrix[4] : null;
        if (centerItem != null && superToolManager.isSuperTool(centerItem)) {
            int currentTier = superToolManager.getSuperTier(centerItem);

            Material surroundMat = null;
            for (int i = 0; i < matrix.length; i++) {
                if (i == 4) continue;
                if (matrix[i] != null && matrix[i].getType() != Material.AIR) {
                    surroundMat = matrix[i].getType();
                    break;
                }
            }

            int targetTier = SuperToolManager.TIER_NONE;
            if (surroundMat == Material.DIAMOND) targetTier = SuperToolManager.TIER_DIAMOND;
            else if (surroundMat == Material.NETHERITE_INGOT) targetTier = SuperToolManager.TIER_NETHERITE;
            else if (surroundMat == Material.IRON_INGOT) targetTier = SuperToolManager.TIER_IRON;

            if (targetTier > currentTier) {
                ItemStack upgraded = superToolManager.upgradeSuperTool(centerItem, targetTier);
                if (upgraded != null) {
                    event.getInventory().setResult(upgraded);
                }
            }
        }
    }
}
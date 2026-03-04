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
import com.jglims.plugin.weapons.*;

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
    private final BattleSwordManager battleSwordManager;
    private final BattlePickaxeManager battlePickaxeManager;
    private final BattleTridentManager battleTridentManager;
    private final BattleSpearManager battleSpearManager;
    private final BattleShovelManager battleShovelManager;
    private final SpearManager spearManager;

    public RecipeManager(JGlimsPlugin plugin, SickleManager sickleManager,
                         BattleAxeManager battleAxeManager, BattleBowManager battleBowManager,
                         BattleMaceManager battleMaceManager, SuperToolManager superToolManager,
                         BattleSwordManager battleSwordManager, BattlePickaxeManager battlePickaxeManager,
                         BattleTridentManager battleTridentManager, BattleSpearManager battleSpearManager,
                         BattleShovelManager battleShovelManager, SpearManager spearManager) {
        this.plugin = plugin;
        this.sickleManager = sickleManager;
        this.battleAxeManager = battleAxeManager;
        this.battleBowManager = battleBowManager;
        this.battleMaceManager = battleMaceManager;
        this.superToolManager = superToolManager;
        this.battleSwordManager = battleSwordManager;
        this.battlePickaxeManager = battlePickaxeManager;
        this.battleTridentManager = battleTridentManager;
        this.battleSpearManager = battleSpearManager;
        this.battleShovelManager = battleShovelManager;
        this.spearManager = spearManager;
    }

    public void registerAllRecipes() {
        // Special items
        registerEyeOfEnder();
        registerTotemOfUndying();
        registerEnchantedGoldenApple();
        registerBlessings();
        registerBestBuddies();

        // Battle weapons (all types)
        registerSickles();
        registerBattleAxes();
        registerBattleSwords();
        registerBattlePickaxes();
        registerBattleBow();
        registerBattleCrossbow();
        registerBattleMace();
        registerBattleTrident();
        registerBattleShovels();
        registerBattleSpears();

        // Super tool recipes (all require battle versions)
        registerSuperToolRecipes();

        plugin.getLogger().info("All custom crafting recipes registered (v1.3.0 — all weapons require Battle version for Super).");
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
    // SICKLES (Hoe -> Sickle, same as before)
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
    // BATTLE SWORDS (NEW)
    // Recipe: 3 matching material on top, sword in center, 5 matching material remaining
    // Pattern: MMM / MSM / MMM (sword center + 8 matching material)
    // ========================================================================
    private void registerBattleSwords() {
        for (Material swordMat : battleSwordManager.getBattleSwordTiers()) {
            Material ingredient = battleSwordManager.getBattleSwordIngredient(swordMat);
            if (ingredient == null) continue;
            ItemStack battleSword = battleSwordManager.createBattleSword(swordMat);
            if (battleSword == null) continue;
            String tierName = swordMat.name().replace("_SWORD", "").toLowerCase();
            NamespacedKey key = new NamespacedKey(plugin, "battle_sword_" + tierName);
            ShapedRecipe recipe = new ShapedRecipe(key, battleSword);
            recipe.shape("MMM", "MSM", "MMM");
            recipe.setIngredient('M', ingredient);
            recipe.setIngredient('S', swordMat);
            plugin.getServer().addRecipe(recipe);
        }
    }

    // ========================================================================
    // BATTLE PICKAXES (NEW)
    // Pattern: MMM / MPM / MMM (pickaxe center + 8 matching material)
    // ========================================================================
    private void registerBattlePickaxes() {
        for (Material pickMat : battlePickaxeManager.getBattlePickaxeTiers()) {
            Material ingredient = battlePickaxeManager.getBattlePickaxeIngredient(pickMat);
            if (ingredient == null) continue;
            ItemStack battlePick = battlePickaxeManager.createBattlePickaxe(pickMat);
            if (battlePick == null) continue;
            String tierName = pickMat.name().replace("_PICKAXE", "").toLowerCase();
            NamespacedKey key = new NamespacedKey(plugin, "battle_pickaxe_" + tierName);
            ShapedRecipe recipe = new ShapedRecipe(key, battlePick);
            recipe.shape("MMM", "MPM", "MMM");
            recipe.setIngredient('M', ingredient);
            recipe.setIngredient('P', pickMat);
            plugin.getServer().addRecipe(recipe);
        }
    }

    // ========================================================================
    // BATTLE AXES (same as before, existing recipe structure)
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
    // BATTLE SHOVELS (NEW registration — BattleShovelManager already exists)
    // Pattern: MMM / MSM / MMM (shovel center + 8 matching material)
    // ========================================================================
    private void registerBattleShovels() {
        for (BattleShovelManager.ShovelTier tier : BattleShovelManager.ShovelTier.values()) {
            Material ingredient = tier.getIngredient();
            ItemStack battleShovel = battleShovelManager.createBattleShovel(tier);
            if (battleShovel == null) continue;
            String tierName = tier.name().toLowerCase();
            NamespacedKey key = new NamespacedKey(plugin, "battle_shovel_" + tierName);
            ShapedRecipe recipe = new ShapedRecipe(key, battleShovel);
            recipe.shape("MMM", "MSM", "MMM");
            recipe.setIngredient('M', ingredient);
            recipe.setIngredient('S', tier.getShovelMaterial());
            plugin.getServer().addRecipe(recipe);
        }
    }

    // ========================================================================
    // BATTLE SPEARS (NEW)
    // Pattern: MMM / MSM / MMM (spear center + 8 matching material)
    // ========================================================================
    private void registerBattleSpears() {
        for (Material spearMat : battleSpearManager.getBattleSpearTiers()) {
            Material ingredient = battleSpearManager.getBattleSpearIngredient(spearMat);
            if (ingredient == null) continue;
            ItemStack battleSpear = battleSpearManager.createBattleSpear(spearMat);
            if (battleSpear == null) continue;
            String tierName = spearMat.name().replace("_SPEAR", "").toLowerCase();
            NamespacedKey key = new NamespacedKey(plugin, "battle_spear_" + tierName);
            ShapedRecipe recipe = new ShapedRecipe(key, battleSpear);
            recipe.shape("MMM", "MSM", "MMM");
            recipe.setIngredient('M', ingredient);
            recipe.setIngredient('S', spearMat);
            plugin.getServer().addRecipe(recipe);
        }
    }

    // ========================================================================
    // BATTLE BOW (same as before)
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
    // BATTLE CROSSBOW (same as before)
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
    // BATTLE MACE (same as before)
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
    // BATTLE TRIDENT (NEW)
    // Pattern: 8 Prismarine Shards + Trident center
    // ========================================================================
    private void registerBattleTrident() {
        ItemStack battleTrident = battleTridentManager.createBattleTrident();
        if (battleTrident == null) return;
        NamespacedKey key = new NamespacedKey(plugin, "battle_trident");
        ShapedRecipe recipe = new ShapedRecipe(key, battleTrident);
        recipe.shape("PPP", "PTP", "PPP");
        recipe.setIngredient('P', Material.PRISMARINE_SHARD);
        recipe.setIngredient('T', Material.TRIDENT);
        plugin.getServer().addRecipe(recipe);
    }

    // ========================================================================
    // SUPER TOOL RECIPES
    // v1.3.0: ALL super upgrades require a Battle version.
    // We register placeholder recipes (tool center + 8 upgrade material),
    // then validate PDC at craft time via onPrepareCraft.
    // ========================================================================
    private void registerSuperToolRecipes() {
        // === TIERED BATTLE WEAPONS ===
        // Battle Swords
        for (Material swordMat : battleSwordManager.getBattleSwordTiers()) {
            String name = "battle_sword_" + swordMat.name().toLowerCase();
            registerAllSuperTiers(swordMat, name);
        }

        // Battle Pickaxes
        for (Material pickMat : battlePickaxeManager.getBattlePickaxeTiers()) {
            String name = "battle_pickaxe_" + pickMat.name().toLowerCase();
            registerAllSuperTiers(pickMat, name);
        }

        // Battle Shovels
        for (BattleShovelManager.ShovelTier tier : BattleShovelManager.ShovelTier.values()) {
            String name = "battle_shovel_" + tier.name().toLowerCase();
            registerAllSuperTiers(tier.getShovelMaterial(), name);
        }

        // Sickles (hoe-based)
        for (Material hoeMat : sickleManager.getSickleTiers()) {
            ItemStack sickle = sickleManager.createSickle(hoeMat);
            if (sickle == null) continue;
            String name = "sickle_" + hoeMat.name().toLowerCase();
            registerSuperTiersForBattleItem(sickle, hoeMat, name);
        }

        // Battle Axes
        for (Material axeMat : battleAxeManager.getBattleAxeTiers()) {
            ItemStack battleAxe = battleAxeManager.createBattleAxe(axeMat);
            if (battleAxe == null) continue;
            String name = "battle_axe_" + axeMat.name().toLowerCase();
            registerSuperTiersForBattleItem(battleAxe, axeMat, name);
        }

        // Battle Spears
        for (Material spearMat : battleSpearManager.getBattleSpearTiers()) {
            ItemStack battleSpear = battleSpearManager.createBattleSpear(spearMat);
            if (battleSpear == null) continue;
            String name = "battle_spear_" + spearMat.name().toLowerCase();
            registerSuperTiersForBattleItem(battleSpear, spearMat, name);
        }

        // === TIERLESS BATTLE WEAPONS ===
        // Battle Trident
        ItemStack battleTrident = battleTridentManager.createBattleTrident();
        if (battleTrident != null) {
            registerSuperTiersForBattleItem(battleTrident, Material.TRIDENT, "battle_trident");
        }

        // Battle Bow
        ItemStack battleBow = battleBowManager.createBattleBow();
        if (battleBow != null) {
            // Iron tier: 8 String + Battle Bow center
            ItemStack superBowIron = superToolManager.createSuperTool(battleBow.clone(), SuperToolManager.TIER_IRON);
            if (superBowIron != null) {
                NamespacedKey key = new NamespacedKey(plugin, "super_iron_battle_bow");
                ShapedRecipe recipe = new ShapedRecipe(key, superBowIron);
                recipe.shape("SSS", "SBS", "SSS");
                recipe.setIngredient('S', Material.STRING);
                recipe.setIngredient('B', Material.BOW);
                plugin.getServer().addRecipe(recipe);
            }
            // Diamond + Netherite tiers: normal upgrade material
            registerSuperTierSingle(battleBow, Material.BOW, "battle_bow", SuperToolManager.TIER_DIAMOND, Material.DIAMOND);
            registerSuperTierSingle(battleBow, Material.BOW, "battle_bow", SuperToolManager.TIER_NETHERITE, Material.NETHERITE_INGOT);
        }

        // Battle Crossbow
        ItemStack battleCrossbow = battleBowManager.createBattleCrossbow();
        if (battleCrossbow != null) {
            // Iron tier: 8 String + Battle Crossbow center
            ItemStack superCbowIron = superToolManager.createSuperTool(battleCrossbow.clone(), SuperToolManager.TIER_IRON);
            if (superCbowIron != null) {
                NamespacedKey key = new NamespacedKey(plugin, "super_iron_battle_crossbow");
                ShapedRecipe recipe = new ShapedRecipe(key, superCbowIron);
                recipe.shape("SSS", "SCS", "SSS");
                recipe.setIngredient('S', Material.STRING);
                recipe.setIngredient('C', Material.CROSSBOW);
                plugin.getServer().addRecipe(recipe);
            }
            registerSuperTierSingle(battleCrossbow, Material.CROSSBOW, "battle_crossbow", SuperToolManager.TIER_DIAMOND, Material.DIAMOND);
            registerSuperTierSingle(battleCrossbow, Material.CROSSBOW, "battle_crossbow", SuperToolManager.TIER_NETHERITE, Material.NETHERITE_INGOT);
        }

        // Battle Mace
        ItemStack battleMace = battleMaceManager.createBattleMace();
        if (battleMace != null) {
            registerSuperTiersForBattleItem(battleMace, Material.MACE, "battle_mace");
        }

        // === NON-BATTLE ITEMS (Elytra, Shield) ===
        registerAllSuperTiers(Material.ELYTRA, "elytra");
        registerAllSuperTiers(Material.SHIELD, "shield");
    }

    /**
     * Registers Iron, Diamond, and Netherite super tier recipes for a material.
     * The center item in the crafting grid uses this material, surrounded by 8 upgrade material.
     * PDC validation happens at craft time.
     */
    private void registerAllSuperTiers(Material baseMat, String namePrefix) {
        Material[] upgradeMats = { Material.IRON_INGOT, Material.DIAMOND, Material.NETHERITE_INGOT };
        int[] tiers = { SuperToolManager.TIER_IRON, SuperToolManager.TIER_DIAMOND, SuperToolManager.TIER_NETHERITE };
        String[] tierNames = { "iron", "diamond", "netherite" };

        for (int i = 0; i < 3; i++) {
            // Create a placeholder result using a basic item (will be overridden at craft time for battle items)
            ItemStack baseItem = new ItemStack(baseMat);
            ItemStack result = superToolManager.createSuperTool(baseItem, tiers[i]);
            if (result == null) continue;
            NamespacedKey key = new NamespacedKey(plugin, "super_" + tierNames[i] + "_" + namePrefix);
            ShapedRecipe recipe = new ShapedRecipe(key, result);
            recipe.shape("MMM", "MTM", "MMM");
            recipe.setIngredient('M', upgradeMats[i]);
            recipe.setIngredient('T', baseMat);
            plugin.getServer().addRecipe(recipe);
        }
    }

    /**
     * Registers all 3 super tiers for a pre-built battle item.
     */
    private void registerSuperTiersForBattleItem(ItemStack battleItem, Material baseMat, String namePrefix) {
        Material[] upgradeMats = { Material.IRON_INGOT, Material.DIAMOND, Material.NETHERITE_INGOT };
        int[] tiers = { SuperToolManager.TIER_IRON, SuperToolManager.TIER_DIAMOND, SuperToolManager.TIER_NETHERITE };
        String[] tierNames = { "iron", "diamond", "netherite" };

        for (int i = 0; i < 3; i++) {
            ItemStack result = superToolManager.createSuperTool(battleItem.clone(), tiers[i]);
            if (result == null) continue;
            NamespacedKey key = new NamespacedKey(plugin, "super_" + tierNames[i] + "_" + namePrefix);
            ShapedRecipe recipe = new ShapedRecipe(key, result);
            recipe.shape("MMM", "MTM", "MMM");
            recipe.setIngredient('M', upgradeMats[i]);
            recipe.setIngredient('T', baseMat);
            plugin.getServer().addRecipe(recipe);
        }
    }

    /**
     * Registers a single super tier for a battle item with a custom upgrade material.
     */
    private void registerSuperTierSingle(ItemStack battleItem, Material baseMat, String namePrefix,
                                          int tier, Material upgradeMat) {
        ItemStack result = superToolManager.createSuperTool(battleItem.clone(), tier);
        if (result == null) return;
        String tierName = switch (tier) {
            case SuperToolManager.TIER_DIAMOND -> "diamond";
            case SuperToolManager.TIER_NETHERITE -> "netherite";
            default -> "iron";
        };
        NamespacedKey key = new NamespacedKey(plugin, "super_" + tierName + "_" + namePrefix);
        ShapedRecipe recipe = new ShapedRecipe(key, result);
        recipe.shape("MMM", "MTM", "MMM");
        recipe.setIngredient('M', upgradeMat);
        recipe.setIngredient('T', baseMat);
        plugin.getServer().addRecipe(recipe);
    }

    // ========================================================================
    // PREPARE CRAFT EVENT
    // Handles:
    //  1. Blocking custom weapons from vanilla recipes
    //  2. Ensuring only battle items become super (PDC guard)
    //  3. Super tool upgrade via crafting table (existing super + 8 upgrade material)
    // ========================================================================
    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        if (event.getRecipe() == null) return;

        ItemStack[] matrix = event.getInventory().getMatrix();

        // --- Block custom weapons from non-plugin recipes ---
        for (ItemStack ingredient : matrix) {
            if (ingredient != null && isAnyBattleWeapon(ingredient)) {
                if (event.getRecipe() instanceof ShapedRecipe shaped) {
                    if (shaped.getKey().getNamespace().equals(plugin.getName().toLowerCase())) continue;
                }
                if (event.getRecipe() instanceof ShapelessRecipe shapeless) {
                    if (shapeless.getKey().getNamespace().equals(plugin.getName().toLowerCase())) continue;
                }
                event.getInventory().setResult(null);
                return;
            }
        }

        // --- Super recipe PDC guard ---
        // If the recipe is a super_* recipe, ensure the center item is a Battle item
        if (event.getRecipe() instanceof ShapedRecipe shaped) {
            String recipeKey = shaped.getKey().getKey();

            if (recipeKey.startsWith("super_")) {
                ItemStack centerItem = matrix.length >= 5 ? matrix[4] : null;

                if (centerItem != null) {
                    // If it's already a super tool being upgraded, that's fine
                    if (superToolManager.isSuperTool(centerItem)) {
                        // Allow upgrade — handled below
                    } else if (!superToolManager.isBattleItem(centerItem)) {
                        // Center is a VANILLA item, not a battle item — block the craft
                        event.getInventory().setResult(null);
                        return;
                    }

                    // Specific checks for sickle recipes
                    if (recipeKey.contains("sickle_") && !sickleManager.isSickle(centerItem)) {
                        event.getInventory().setResult(null);
                        return;
                    }

                    // Specific checks for battle axe recipes
                    if (recipeKey.contains("battle_axe_") && !battleAxeManager.isBattleAxe(centerItem)) {
                        event.getInventory().setResult(null);
                        return;
                    }

                    // Specific checks for battle sword recipes
                    if (recipeKey.contains("battle_sword_") && !battleSwordManager.isBattleSword(centerItem)) {
                        event.getInventory().setResult(null);
                        return;
                    }

                    // Specific checks for battle pickaxe recipes
                    if (recipeKey.contains("battle_pickaxe_") && !battlePickaxeManager.isBattlePickaxe(centerItem)) {
                        event.getInventory().setResult(null);
                        return;
                    }

                    // Specific checks for battle shovel recipes
                    if (recipeKey.contains("battle_shovel_") && !battleShovelManager.isBattleShovel(centerItem)) {
                        event.getInventory().setResult(null);
                        return;
                    }

                    // Specific checks for battle spear recipes
                    if (recipeKey.contains("battle_spear_") && !battleSpearManager.isBattleSpear(centerItem)) {
                        event.getInventory().setResult(null);
                        return;
                    }

                    // Specific checks for battle trident recipes
                    if (recipeKey.contains("battle_trident") && centerItem.getType() == Material.TRIDENT
                            && !battleTridentManager.isBattleTrident(centerItem)) {
                        event.getInventory().setResult(null);
                        return;
                    }

                    // Specific checks for battle bow recipes
                    if (recipeKey.contains("battle_bow") && !recipeKey.contains("crossbow")
                            && centerItem.getType() == Material.BOW
                            && !battleBowManager.isBattleBow(centerItem)) {
                        event.getInventory().setResult(null);
                        return;
                    }

                    // Specific checks for battle crossbow recipes
                    if (recipeKey.contains("battle_crossbow") && centerItem.getType() == Material.CROSSBOW
                            && !battleBowManager.isBattleCrossbow(centerItem)) {
                        event.getInventory().setResult(null);
                        return;
                    }

                    // Specific checks for battle mace recipes
                    if (recipeKey.contains("battle_mace") && centerItem.getType() == Material.MACE
                            && !battleMaceManager.isBattleMace(centerItem)) {
                        event.getInventory().setResult(null);
                        return;
                    }
                }
            }
        }

        // --- Super tool upgrade via crafting table ---
        ItemStack centerItem = matrix.length >= 5 ? matrix[4] : null;
        if (centerItem != null && superToolManager.isSuperTool(centerItem)) {
            int currentTier = superToolManager.getSuperTier(centerItem);

            Material surroundMat = null;
            boolean allSame = true;
            for (int i = 0; i < matrix.length; i++) {
                if (i == 4) continue;
                if (matrix[i] != null && matrix[i].getType() != Material.AIR) {
                    if (surroundMat == null) {
                        surroundMat = matrix[i].getType();
                    } else if (matrix[i].getType() != surroundMat) {
                        allSame = false;
                    }
                }
            }

            if (allSame && surroundMat != null) {
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

    /**
     * Checks if an item is any custom battle/sickle weapon.
     */
    private boolean isAnyBattleWeapon(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return sickleManager.isSickle(item)
                || battleAxeManager.isBattleAxe(item)
                || battleMaceManager.isBattleMace(item)
                || battleBowManager.isBattleBow(item)
                || battleBowManager.isBattleCrossbow(item)
                || battleSwordManager.isBattleSword(item)
                || battlePickaxeManager.isBattlePickaxe(item)
                || battleTridentManager.isBattleTrident(item)
                || battleSpearManager.isBattleSpear(item)
                || battleShovelManager.isBattleShovel(item)
                || superToolManager.isSuperTool(item);
    }
}

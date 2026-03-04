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
import com.jglims.plugin.weapons.BattlePickaxeManager;
import com.jglims.plugin.weapons.BattleShovelManager;
import com.jglims.plugin.weapons.BattleSpearManager;
import com.jglims.plugin.weapons.BattleSwordManager;
import com.jglims.plugin.weapons.BattleTridentManager;
import com.jglims.plugin.weapons.SickleManager;
import com.jglims.plugin.weapons.SpearManager;
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
        registerEyeOfEnder();
        registerTotemOfUndying();
        registerEnchantedGoldenApple();
        registerBlessings();
        registerBestBuddies();

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

        registerSuperToolRecipes();

        plugin.getLogger().info("All custom crafting recipes registered (v1.3.0 — 1-on-top for Battle, 3-on-top for Super).");
    }

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

    private void registerTotemOfUndying() {
        NamespacedKey key = new NamespacedKey(plugin, "custom_totem");
        ShapedRecipe recipe = new ShapedRecipe(key, new ItemStack(Material.TOTEM_OF_UNDYING, 1));
        recipe.shape("NNN", "EGE", "NNN");
        recipe.setIngredient('N', Material.GOLD_NUGGET);
        recipe.setIngredient('E', Material.EMERALD);
        recipe.setIngredient('G', Material.GOLD_INGOT);
        plugin.getServer().addRecipe(recipe);
    }

    private void registerEnchantedGoldenApple() {
        NamespacedKey key = new NamespacedKey(plugin, "custom_enchanted_golden_apple");
        ShapedRecipe recipe = new ShapedRecipe(key, new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 1));
        recipe.shape("GTG", "TAT", "GTG");
        recipe.setIngredient('G', Material.GOLD_BLOCK);
        recipe.setIngredient('T', Material.TORCHFLOWER);
        recipe.setIngredient('A', Material.APPLE);
        plugin.getServer().addRecipe(recipe);
    }

    private void registerBlessings() {
        registerBlessing("c_bless", Material.MELON_SLICE, Material.GLISTERING_MELON_SLICE, "C's Bless", NamedTextColor.GOLD);
        registerBlessing("ami_bless", Material.CARROT, Material.GOLDEN_CARROT, "Ami's Bless", NamedTextColor.RED);
        registerBlessing("la_bless", Material.APPLE, Material.GOLDEN_APPLE, "La's Bless", NamedTextColor.BLUE);
    }

    private void registerBlessing(String id, Material bottomCenter, Material resultMaterial, String displayName, NamedTextColor color) {
        NamespacedKey key = new NamespacedKey(plugin, id);
        ItemStack result = new ItemStack(resultMaterial, 1);
        ItemMeta meta = result.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(displayName, color).decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Right-click to consume", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Permanent stat boost", NamedTextColor.DARK_PURPLE).decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
            meta.getPersistentDataContainer().set(new NamespacedKey(plugin, id + "_item"), PersistentDataType.BYTE, (byte) 1);
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
            meta.getPersistentDataContainer().set(enchantManager.getKey(EnchantmentType.BEST_BUDDIES), PersistentDataType.INTEGER, 1);
            result.setItemMeta(meta);
        }
        ShapelessRecipe recipe = new ShapelessRecipe(key, result);
        recipe.addIngredient(Material.BONE);
        recipe.addIngredient(Material.DIAMOND);
        plugin.getServer().addRecipe(recipe);
    }

    private void registerSickles() {
        for (Material hoeMat : sickleManager.getSickleTiers()) {
            Material ingredient = sickleManager.getSickleIngredient(hoeMat);
            if (ingredient == null) continue;
            ItemStack sickle = sickleManager.createSickle(hoeMat);
            if (sickle == null) continue;
            String tierName = hoeMat.name().replace("_HOE", "").toLowerCase();
            NamespacedKey key = new NamespacedKey(plugin, "sickle_" + tierName);
            ShapedRecipe recipe = new ShapedRecipe(key, sickle);
            recipe.shape(" I ", " H ", "   ");
            recipe.setIngredient('I', ingredient);
            recipe.setIngredient('H', hoeMat);
            plugin.getServer().addRecipe(recipe);
        }
    }

    private void registerBattleSwords() {
        for (Material swordMat : battleSwordManager.getBattleSwordTiers()) {
            Material ingredient = battleSwordManager.getBattleSwordIngredient(swordMat);
            if (ingredient == null) continue;
            ItemStack battleSword = battleSwordManager.createBattleSword(swordMat);
            if (battleSword == null) continue;
            String tierName = swordMat.name().replace("_SWORD", "").toLowerCase();
            NamespacedKey key = new NamespacedKey(plugin, "battle_sword_" + tierName);
            ShapedRecipe recipe = new ShapedRecipe(key, battleSword);
            recipe.shape(" I ", " S ", "   ");
            recipe.setIngredient('I', ingredient);
            recipe.setIngredient('S', swordMat);
            plugin.getServer().addRecipe(recipe);
        }
    }

    private void registerBattlePickaxes() {
        for (Material pickMat : battlePickaxeManager.getBattlePickaxeTiers()) {
            Material ingredient = battlePickaxeManager.getBattlePickaxeIngredient(pickMat);
            if (ingredient == null) continue;
            ItemStack battlePick = battlePickaxeManager.createBattlePickaxe(pickMat);
            if (battlePick == null) continue;
            String tierName = pickMat.name().replace("_PICKAXE", "").toLowerCase();
            NamespacedKey key = new NamespacedKey(plugin, "battle_pickaxe_" + tierName);
            ShapedRecipe recipe = new ShapedRecipe(key, battlePick);
            recipe.shape(" I ", " P ", "   ");
            recipe.setIngredient('I', ingredient);
            recipe.setIngredient('P', pickMat);
            plugin.getServer().addRecipe(recipe);
        }
    }

    private void registerBattleAxes() {
        for (Material axeMat : battleAxeManager.getBattleAxeTiers()) {
            Material ingredient = battleAxeManager.getBattleAxeIngredient(axeMat);
            if (ingredient == null) continue;
            ItemStack battleAxe = battleAxeManager.createBattleAxe(axeMat);
            if (battleAxe == null) continue;
            String tierName = axeMat.name().replace("_AXE", "").toLowerCase();
            NamespacedKey key = new NamespacedKey(plugin, "battle_axe_" + tierName);
            ShapedRecipe recipe = new ShapedRecipe(key, battleAxe);
            recipe.shape(" I ", " A ", "   ");
            recipe.setIngredient('I', ingredient);
            recipe.setIngredient('A', axeMat);
            plugin.getServer().addRecipe(recipe);
        }
    }

    private void registerBattleShovels() {
        for (BattleShovelManager.ShovelTier tier : BattleShovelManager.ShovelTier.values()) {
            Material ingredient = tier.getIngredient();
            ItemStack battleShovel = battleShovelManager.createBattleShovel(tier);
            if (battleShovel == null) continue;
            String tierName = tier.name().toLowerCase();
            NamespacedKey key = new NamespacedKey(plugin, "battle_shovel_" + tierName);
            ShapedRecipe recipe = new ShapedRecipe(key, battleShovel);
            recipe.shape(" I ", " S ", "   ");
            recipe.setIngredient('I', ingredient);
            recipe.setIngredient('S', tier.getShovelMaterial());
            plugin.getServer().addRecipe(recipe);
        }
    }

    private void registerBattleSpears() {
        for (Material spearMat : battleSpearManager.getBattleSpearTiers()) {
            Material ingredient = battleSpearManager.getBattleSpearIngredient(spearMat);
            if (ingredient == null) continue;
            ItemStack battleSpear = battleSpearManager.createBattleSpear(spearMat);
            if (battleSpear == null) continue;
            String tierName = spearMat.name().replace("_SPEAR", "").toLowerCase();
            NamespacedKey key = new NamespacedKey(plugin, "battle_spear_" + tierName);
            ShapedRecipe recipe = new ShapedRecipe(key, battleSpear);
            recipe.shape(" I ", " S ", "   ");
            recipe.setIngredient('I', ingredient);
            recipe.setIngredient('S', spearMat);
            plugin.getServer().addRecipe(recipe);
        }
    }

    private void registerBattleBow() {
        ItemStack battleBow = battleBowManager.createBattleBow();
        if (battleBow == null) return;
        NamespacedKey key = new NamespacedKey(plugin, "battle_bow");
        ShapedRecipe recipe = new ShapedRecipe(key, battleBow);
        recipe.shape(" K ", " B ", "   ");
        recipe.setIngredient('K', Material.STICK);
        recipe.setIngredient('B', Material.BOW);
        plugin.getServer().addRecipe(recipe);
    }

    private void registerBattleCrossbow() {
        ItemStack battleCrossbow = battleBowManager.createBattleCrossbow();
        if (battleCrossbow == null) return;
        NamespacedKey key = new NamespacedKey(plugin, "battle_crossbow");
        ShapedRecipe recipe = new ShapedRecipe(key, battleCrossbow);
        recipe.shape(" K ", " C ", "   ");
        recipe.setIngredient('K', Material.STICK);
        recipe.setIngredient('C', Material.CROSSBOW);
        plugin.getServer().addRecipe(recipe);
    }

    private void registerBattleMace() {
        ItemStack battleMace = battleMaceManager.createBattleMace();
        if (battleMace == null) return;
        NamespacedKey key = new NamespacedKey(plugin, "battle_mace");
        ShapedRecipe recipe = new ShapedRecipe(key, battleMace);
        recipe.shape(" K ", " M ", "   ");
        recipe.setIngredient('K', Material.STICK);
        recipe.setIngredient('M', Material.MACE);
        plugin.getServer().addRecipe(recipe);
    }

    private void registerBattleTrident() {
        ItemStack battleTrident = battleTridentManager.createBattleTrident();
        if (battleTrident == null) return;
        NamespacedKey key = new NamespacedKey(plugin, "battle_trident");
        ShapedRecipe recipe = new ShapedRecipe(key, battleTrident);
        recipe.shape(" K ", " T ", "   ");
        recipe.setIngredient('K', Material.STICK);
        recipe.setIngredient('T', Material.TRIDENT);
        plugin.getServer().addRecipe(recipe);
    }

    private void registerSuperToolRecipes() {
        for (Material swordMat : battleSwordManager.getBattleSwordTiers()) {
            registerAllSuperTiers(swordMat, "battle_sword_" + swordMat.name().toLowerCase());
        }
        for (Material pickMat : battlePickaxeManager.getBattlePickaxeTiers()) {
            registerAllSuperTiers(pickMat, "battle_pickaxe_" + pickMat.name().toLowerCase());
        }
        for (BattleShovelManager.ShovelTier tier : BattleShovelManager.ShovelTier.values()) {
            registerAllSuperTiers(tier.getShovelMaterial(), "battle_shovel_" + tier.name().toLowerCase());
        }
        for (Material hoeMat : sickleManager.getSickleTiers()) {
            ItemStack sickle = sickleManager.createSickle(hoeMat);
            if (sickle == null) continue;
            registerSuperTiersForBattleItem(sickle, hoeMat, "sickle_" + hoeMat.name().toLowerCase());
        }
        for (Material axeMat : battleAxeManager.getBattleAxeTiers()) {
            ItemStack battleAxe = battleAxeManager.createBattleAxe(axeMat);
            if (battleAxe == null) continue;
            registerSuperTiersForBattleItem(battleAxe, axeMat, "battle_axe_" + axeMat.name().toLowerCase());
        }
        for (Material spearMat : battleSpearManager.getBattleSpearTiers()) {
            ItemStack battleSpear = battleSpearManager.createBattleSpear(spearMat);
            if (battleSpear == null) continue;
            registerSuperTiersForBattleItem(battleSpear, spearMat, "battle_spear_" + spearMat.name().toLowerCase());
        }
        ItemStack battleTrident = battleTridentManager.createBattleTrident();
        if (battleTrident != null) registerSuperTiersForBattleItem(battleTrident, Material.TRIDENT, "battle_trident");
        ItemStack battleBow = battleBowManager.createBattleBow();
        if (battleBow != null) registerSuperTiersForBattleItem(battleBow, Material.BOW, "battle_bow");
        ItemStack battleCrossbow = battleBowManager.createBattleCrossbow();
        if (battleCrossbow != null) registerSuperTiersForBattleItem(battleCrossbow, Material.CROSSBOW, "battle_crossbow");
        ItemStack battleMace = battleMaceManager.createBattleMace();
        if (battleMace != null) registerSuperTiersForBattleItem(battleMace, Material.MACE, "battle_mace");
        registerAllSuperTiers(Material.ELYTRA, "elytra");
        registerAllSuperTiers(Material.SHIELD, "shield");
    }

    private void registerAllSuperTiers(Material baseMat, String namePrefix) {
        Material[] upgradeMats = { Material.IRON_INGOT, Material.DIAMOND, Material.NETHERITE_INGOT };
        int[] tiers = { SuperToolManager.TIER_IRON, SuperToolManager.TIER_DIAMOND, SuperToolManager.TIER_NETHERITE };
        String[] tierNames = { "iron", "diamond", "netherite" };
        for (int i = 0; i < 3; i++) {
            ItemStack baseItem = new ItemStack(baseMat);
            ItemStack result = superToolManager.createSuperTool(baseItem, tiers[i]);
            if (result == null) continue;
            NamespacedKey key = new NamespacedKey(plugin, "super_" + tierNames[i] + "_" + namePrefix);
            ShapedRecipe recipe = new ShapedRecipe(key, result);
            recipe.shape("MMM", " T ", "   ");
            recipe.setIngredient('M', upgradeMats[i]);
            recipe.setIngredient('T', baseMat);
            plugin.getServer().addRecipe(recipe);
        }
    }

    private void registerSuperTiersForBattleItem(ItemStack battleItem, Material baseMat, String namePrefix) {
        Material[] upgradeMats = { Material.IRON_INGOT, Material.DIAMOND, Material.NETHERITE_INGOT };
        int[] tiers = { SuperToolManager.TIER_IRON, SuperToolManager.TIER_DIAMOND, SuperToolManager.TIER_NETHERITE };
        String[] tierNames = { "iron", "diamond", "netherite" };
        for (int i = 0; i < 3; i++) {
            ItemStack result = superToolManager.createSuperTool(battleItem.clone(), tiers[i]);
            if (result == null) continue;
            NamespacedKey key = new NamespacedKey(plugin, "super_" + tierNames[i] + "_" + namePrefix);
            ShapedRecipe recipe = new ShapedRecipe(key, result);
            recipe.shape("MMM", " T ", "   ");
            recipe.setIngredient('M', upgradeMats[i]);
            recipe.setIngredient('T', baseMat);
            plugin.getServer().addRecipe(recipe);
        }
    }

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        if (event.getRecipe() == null) return;
        ItemStack[] matrix = event.getInventory().getMatrix();

        // --- 1. Block custom weapons from non-plugin recipes ---
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

        // --- 2. Block battle-to-battle re-crafting ---
        // If a battle/sickle recipe is matched but the center item is ALREADY battle, block it
        if (event.getRecipe() instanceof ShapedRecipe shaped) {
            String rKey = shaped.getKey().getKey();
            if (rKey.startsWith("battle_") || rKey.startsWith("sickle_")) {
                // For pattern " I " / " T " / "   ", the tool is slot 4
                ItemStack center = matrix.length >= 5 ? matrix[4] : null;
                if (center != null && isAnyBattleWeapon(center)) {
                    event.getInventory().setResult(null);
                    return;
                }
            }
        }

        // --- 3. Super recipe PDC guard ---
        if (event.getRecipe() instanceof ShapedRecipe shaped) {
            String recipeKey = shaped.getKey().getKey();
            if (recipeKey.startsWith("super_")) {
                ItemStack centerItem = matrix.length >= 5 ? matrix[4] : null;
                if (centerItem != null) {
                    if (superToolManager.isSuperTool(centerItem)) {
                        // allow upgrade
                    } else if (!superToolManager.isBattleItem(centerItem)) {
                        event.getInventory().setResult(null);
                        return;
                    }
                    if (recipeKey.contains("sickle_") && !sickleManager.isSickle(centerItem)) { event.getInventory().setResult(null); return; }
                    if (recipeKey.contains("battle_axe_") && !battleAxeManager.isBattleAxe(centerItem)) { event.getInventory().setResult(null); return; }
                    if (recipeKey.contains("battle_sword_") && !battleSwordManager.isBattleSword(centerItem)) { event.getInventory().setResult(null); return; }
                    if (recipeKey.contains("battle_pickaxe_") && !battlePickaxeManager.isBattlePickaxe(centerItem)) { event.getInventory().setResult(null); return; }
                    if (recipeKey.contains("battle_shovel_") && !battleShovelManager.isBattleShovel(centerItem)) { event.getInventory().setResult(null); return; }
                    if (recipeKey.contains("battle_spear_") && !battleSpearManager.isBattleSpear(centerItem)) { event.getInventory().setResult(null); return; }
                    if (recipeKey.contains("battle_trident") && centerItem.getType() == Material.TRIDENT && !battleTridentManager.isBattleTrident(centerItem)) { event.getInventory().setResult(null); return; }
                    if (recipeKey.contains("battle_bow") && !recipeKey.contains("crossbow") && centerItem.getType() == Material.BOW && !battleBowManager.isBattleBow(centerItem)) { event.getInventory().setResult(null); return; }
                    if (recipeKey.contains("battle_crossbow") && centerItem.getType() == Material.CROSSBOW && !battleBowManager.isBattleCrossbow(centerItem)) { event.getInventory().setResult(null); return; }
                    if (recipeKey.contains("battle_mace") && centerItem.getType() == Material.MACE && !battleMaceManager.isBattleMace(centerItem)) { event.getInventory().setResult(null); return; }
                }
            }
        }

        // --- 4. Super tool upgrade via crafting table ---
        ItemStack centerItem = matrix.length >= 5 ? matrix[4] : null;
        if (centerItem != null && superToolManager.isSuperTool(centerItem)) {
            int currentTier = superToolManager.getSuperTier(centerItem);
            Material topMat = null;
            boolean topRowValid = true;
            for (int i = 0; i <= 2; i++) {
                if (matrix[i] == null || matrix[i].getType() == Material.AIR) { topRowValid = false; break; }
                if (topMat == null) topMat = matrix[i].getType();
                else if (matrix[i].getType() != topMat) { topRowValid = false; break; }
            }
            boolean otherSlotsEmpty = true;
            for (int slot : new int[]{ 3, 5, 6, 7, 8 }) {
                if (slot < matrix.length && matrix[slot] != null && matrix[slot].getType() != Material.AIR) { otherSlotsEmpty = false; break; }
            }
            if (topRowValid && otherSlotsEmpty && topMat != null) {
                int targetTier = SuperToolManager.TIER_NONE;
                if (topMat == Material.IRON_INGOT) targetTier = SuperToolManager.TIER_IRON;
                else if (topMat == Material.DIAMOND) targetTier = SuperToolManager.TIER_DIAMOND;
                else if (topMat == Material.NETHERITE_INGOT) targetTier = SuperToolManager.TIER_NETHERITE;
                if (targetTier > currentTier) {
                    ItemStack upgraded = superToolManager.upgradeSuperTool(centerItem, targetTier);
                    if (upgraded != null) event.getInventory().setResult(upgraded);
                }
            }
        }
    }

    private boolean isAnyBattleWeapon(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return sickleManager.isSickle(item) || battleAxeManager.isBattleAxe(item) || battleMaceManager.isBattleMace(item)
                || battleBowManager.isBattleBow(item) || battleBowManager.isBattleCrossbow(item) || battleSwordManager.isBattleSword(item)
                || battlePickaxeManager.isBattlePickaxe(item) || battleTridentManager.isBattleTrident(item)
                || battleSpearManager.isBattleSpear(item) || battleShovelManager.isBattleShovel(item) || superToolManager.isSuperTool(item);
    }
}

package com.jglims.plugin.utility;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.AbstractVillager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.entity.VillagerReplenishTradeEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.inventory.MerchantRecipe;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.config.ConfigManager;

public class VillagerTradeListener implements Listener {

    private final JGlimsPlugin plugin;
    private final ConfigManager config;

    public VillagerTradeListener(JGlimsPlugin plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
    }

    // ========================================================================
    // When a villager acquires a new trade, reduce its price and make unlimited
    // ========================================================================
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onVillagerAcquireTrade(VillagerAcquireTradeEvent event) {
        if (!config.isVillagerTradesEnabled()) return;

        MerchantRecipe original = event.getRecipe();
        MerchantRecipe modified = modifyRecipe(original);
        event.setRecipe(modified);
    }

    // ========================================================================
    // When trades replenish (restock), re-apply our modifications
    // ========================================================================
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onVillagerReplenishTrade(VillagerReplenishTradeEvent event) {
        if (!config.isVillagerTradesEnabled()) return;

        MerchantRecipe original = event.getRecipe();
        MerchantRecipe modified = modifyRecipe(original);
        event.setRecipe(modified);
    }

    // ========================================================================
    // When a player opens a merchant inventory, modify all existing trades
    // ========================================================================
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!config.isVillagerTradesEnabled()) return;
        if (event.getInventory().getType() != InventoryType.MERCHANT) return;
        if (!(event.getInventory() instanceof MerchantInventory merchantInv)) return;

        AbstractVillager villager = (AbstractVillager) merchantInv.getMerchant();
        List<MerchantRecipe> recipes = new ArrayList<>();
        for (MerchantRecipe recipe : villager.getRecipes()) {
            recipes.add(modifyRecipe(recipe));
        }
        villager.setRecipes(recipes);
    }

    private MerchantRecipe modifyRecipe(MerchantRecipe original) {
        // Reduce price
        double reduction = config.getVillagerPriceReduction();
        List<ItemStack> ingredients = new ArrayList<>(original.getIngredients());

        for (int i = 0; i < ingredients.size(); i++) {
            ItemStack ingredient = ingredients.get(i);
            if (ingredient != null && ingredient.getAmount() > 1) {
                int newAmount = Math.max(1, (int) Math.ceil(ingredient.getAmount() * (1.0 - reduction)));
                ingredient = ingredient.clone();
                ingredient.setAmount(newAmount);
                ingredients.set(i, ingredient);
            }
        }

        // Create modified recipe
        int maxUses = config.isDisableTradeLocking() ? Integer.MAX_VALUE : original.getMaxUses();
        MerchantRecipe modified = new MerchantRecipe(
            original.getResult(), 0, maxUses,
            original.hasExperienceReward(), original.getVillagerExperience(),
            original.getPriceMultiplier(), original.getDemand(),
            original.getSpecialPrice()
        );
        modified.setIngredients(ingredients);

        return modified;
    }
}

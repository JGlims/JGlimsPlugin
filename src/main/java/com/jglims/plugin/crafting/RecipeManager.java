package com.jglims.plugin.crafting;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.weapons.SickleManager;
import com.jglims.plugin.weapons.SuperToolManager;
import org.bukkit.event.Listener;

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
        plugin.getLogger().info("Registering custom recipes...");
        // TODO: Register Eye of Ender recipe
        // TODO: Register Totem of Undying recipe
        // TODO: Register Enchanted Golden Apple recipe
        // TODO: Register C's Bless, Ami's Bless, La's Bless recipes
        // TODO: Register Sickle recipes (all materials)
        // TODO: Register Super Tool recipes (all materials)
        // TODO: Register Super Elytra recipe
        // TODO: Register BestBuddies book recipe
        plugin.getLogger().info("Custom recipes registered.");
    }
}
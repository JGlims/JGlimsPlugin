package com.jglims.plugin.crafting;

import com.jglims.plugin.JGlimsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;

public class VanillaRecipeRemover {

    public static void remove(JGlimsPlugin plugin) {
        // Remove vanilla Eye of Ender recipe
        boolean removed = Bukkit.removeRecipe(NamespacedKey.minecraft("ender_eye"));
        if (removed) {
            plugin.getLogger().info("Removed vanilla Eye of Ender recipe.");
        } else {
            plugin.getLogger().warning("Could not find vanilla Eye of Ender recipe to remove.");
        }
    }
}
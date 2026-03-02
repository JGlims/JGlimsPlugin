package com.jglims.plugin.weapons;

import com.jglims.plugin.JGlimsPlugin;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class SickleManager implements Listener {

    private final JGlimsPlugin plugin;
    private final NamespacedKey sickleKey;

    public SickleManager(JGlimsPlugin plugin) {
        this.plugin = plugin;
        this.sickleKey = new NamespacedKey(plugin, "is_sickle");
    }

    public NamespacedKey getSickleKey() {
        return sickleKey;
    }

    public boolean isSickle(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(sickleKey, PersistentDataType.BYTE);
    }

    // Prevent sickles from tilling soil
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        ItemStack item = event.getItem();
        if (item == null) return;
        if (isSickle(item)) {
            Material blockType = event.getClickedBlock() != null ? event.getClickedBlock().getType() : null;
            if (blockType == Material.DIRT || blockType == Material.GRASS_BLOCK
                || blockType == Material.DIRT_PATH || blockType == Material.COARSE_DIRT
                || blockType == Material.ROOTED_DIRT || blockType == Material.FARMLAND) {
                event.setCancelled(true);
            }
        }
    }

    // TODO: Implement sickle crafting recipes
    // TODO: Implement sickle stat application (damage, attack speed)
}
package com.jglims.plugin.blessings;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import com.jglims.plugin.JGlimsPlugin;

public class BlessingListener implements Listener {

    private final JGlimsPlugin plugin;
    private final BlessingManager blessingManager;

    public BlessingListener(JGlimsPlugin plugin, BlessingManager blessingManager) {
        this.plugin = plugin;
        this.blessingManager = blessingManager;
    }

    // ========================================================================
    // RIGHT-CLICK CONSUME — Apply blessing and consume item
    // ========================================================================
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR
            && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        // Only process main hand to prevent double-fire
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) return;

        var pdc = item.getItemMeta().getPersistentDataContainer();

        boolean consumed = false;

        if (pdc.has(blessingManager.getCBlessItemKey(), PersistentDataType.BYTE)) {
            consumed = blessingManager.applyCBless(player);
        } else if (pdc.has(blessingManager.getAmiBlessItemKey(), PersistentDataType.BYTE)) {
            consumed = blessingManager.applyAmiBless(player);
        } else if (pdc.has(blessingManager.getLaBlessItemKey(), PersistentDataType.BYTE)) {
            consumed = blessingManager.applyLaBless(player);
        }

        if (consumed) {
            // Remove one from the stack
            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
            } else {
                player.getInventory().setItemInMainHand(null);
            }
            event.setCancelled(true);
        }
    }

    // ========================================================================
    // REAPPLY ON RESPAWN — Blessings persist through death via PDC on player
    // ========================================================================
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            blessingManager.reapplyAllModifiers(event.getPlayer());
        }, 5L);
    }

    // ========================================================================
    // REAPPLY ON JOIN — In case server restarted
    // ========================================================================
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            blessingManager.reapplyAllModifiers(event.getPlayer());
        }, 10L);
    }
}

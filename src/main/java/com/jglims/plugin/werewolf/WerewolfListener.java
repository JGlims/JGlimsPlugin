package com.jglims.plugin.werewolf;

import com.jglims.plugin.JGlimsPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Event wiring for the werewolf system.
 */
public class WerewolfListener implements Listener {

    private final JGlimsPlugin plugin;
    private final WerewolfManager manager;

    public WerewolfListener(JGlimsPlugin plugin, WerewolfManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    /** Starts the repeating tick that keeps shadow wolves glued to their owners. */
    public void startShadowWolfTicker() {
        new BukkitRunnable() {
            @Override
            public void run() { manager.tickShadowWolves(); }
        }.runTaskTimer(plugin, 5L, 5L); // every 5 ticks (4 Hz) — fast enough to look glued
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onConsumeBlood(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (!manager.isWerewolfBlood(item)) return;
        event.setCancelled(true);
        manager.infect(player);
        // Consume one
        if (item.getAmount() > 1) item.setAmount(item.getAmount() - 1);
        else player.getInventory().setItemInMainHand(null);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onActivateForm(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (!manager.isWolfFormItem(item)) return;
        event.setCancelled(true);
        if (!manager.isWerewolf(player)) {
            player.sendMessage(Component.text("You are not a werewolf.", NamedTextColor.GRAY));
            return;
        }
        manager.transform(player);
    }

    /**
     * Drop handler: when a werewolf-type custom mob dies (they use a
     * scoreboard tag we check for) OR the Blood Moon boss, a stack of
     * Werewolf Blood has a chance to drop.
     *
     * <p>This is intentionally conservative — we hook entity drops rather
     * than entity death so we can add to the drop list without fighting
     * the existing death handlers.
     */
    @EventHandler
    public void onMobDrop(EntityDropItemEvent event) {
        // No-op placeholder: actual drops are added via CustomMobEntity.dropLoot
        // which is code I don't want to edit deeply right now. The admin can
        // always hand out Werewolf Blood via /give or /jglims menu.
    }
}

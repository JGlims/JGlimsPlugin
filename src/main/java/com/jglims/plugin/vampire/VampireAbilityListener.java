package com.jglims.plugin.vampire;

import com.jglims.plugin.JGlimsPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * Event listener for the vampire ability system.
 * <ul>
 *   <li>Sneak + Swap Hand (F) → opens the ability GUI</li>
 *   <li>Right-click with ability in offhand → fires that ability</li>
 *   <li>Click in ability GUI → equips ability to offhand</li>
 *   <li>Bite-marked victims take bonus damage from their marker</li>
 * </ul>
 */
public class VampireAbilityListener implements Listener {

    private final JGlimsPlugin plugin;
    private final VampireManager vampireManager;
    private final VampireAbilityManager abilityManager;

    public VampireAbilityListener(JGlimsPlugin plugin, VampireManager vampireManager,
                                  VampireAbilityManager abilityManager) {
        this.plugin = plugin;
        this.vampireManager = vampireManager;
        this.abilityManager = abilityManager;
    }

    /**
     * Sneak + Swap Hands (F) → opens the ability GUI instead of swapping.
     * Plain swap (not sneaking) still works normally.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onSwapHand(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        if (!vampireManager.isVampire(player)) return;
        if (!player.isSneaking()) return;
        event.setCancelled(true);
        abilityManager.openAbilityGui(player);
    }

    /**
     * Right-click with an ability item in the offhand (and empty main hand or
     * non-ability main hand) fires the ability. We use the main-hand interact
     * because offhand right-click often triggers weird edge cases.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        if (!vampireManager.isVampire(player)) return;

        ItemStack offhand = player.getInventory().getItemInOffHand();
        String abilityId = abilityManager.getAbilityId(offhand);
        if (abilityId == null) return;

        // Only fire from the main-hand interact event (avoids double-fire)
        if (event.getHand() != EquipmentSlot.HAND) return;

        event.setCancelled(true);
        abilityManager.executeAbility(player, abilityId);
    }

    /**
     * Clicking an ability in the GUI equips it to the offhand slot.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onGuiClick(InventoryClickEvent event) {
        if (event.getView().title() == null) return;
        String title = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
                .plainText().serialize(event.getView().title());
        if (!abilityManager.isAbilityGui(title)) return;

        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null) return;
        String abilityId = abilityManager.getAbilityId(clicked);
        if (abilityId == null) return;

        // Equip to offhand (swap with whatever's there)
        ItemStack currentOff = player.getInventory().getItemInOffHand();
        player.getInventory().setItemInOffHand(clicked.clone());
        if (currentOff != null && currentOff.getType() != org.bukkit.Material.AIR) {
            // Only drop back to inventory if not another ability
            if (abilityManager.getAbilityId(currentOff) == null) {
                player.getInventory().addItem(currentOff).forEach((i, stack) ->
                        player.getWorld().dropItemNaturally(player.getLocation(), stack));
            }
        }
        player.closeInventory();
        player.sendActionBar(Component.text("Equipped " + clicked.getItemMeta().displayName(),
                NamedTextColor.DARK_RED));
    }

    /**
     * Applies the bite-mark damage multiplier on subsequent hits.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof LivingEntity victim)) return;
        if (!vampireManager.isVampire(attacker)) return;

        double mult = abilityManager.getBiteMarkMultiplier(victim, attacker);
        if (mult != 1.0) {
            event.setDamage(event.getDamage() * mult);
        }
    }
}

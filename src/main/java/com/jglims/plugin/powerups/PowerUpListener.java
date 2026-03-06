package com.jglims.plugin.powerups;

import com.jglims.plugin.JGlimsPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class PowerUpListener implements Listener {

    private final JGlimsPlugin plugin;
    private final PowerUpManager manager;

    public PowerUpListener(JGlimsPlugin plugin, PowerUpManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    // ── Right-click consumption ──
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!event.getAction().isRightClick()) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        String type = meta.getPersistentDataContainer().get(manager.getKeyPowerUpType(), PersistentDataType.STRING);
        if (type == null) return;

        boolean consumed = false;
        switch (type) {
            case "heart_crystal" -> consumed = manager.consumeHeartCrystal(player);
            case "soul_fragment" -> consumed = manager.consumeSoulFragment(player);
            case "titan_resolve" -> consumed = manager.consumeTitanResolve(player);
            case "keep_inventorer" -> consumed = manager.consumeKeepInventorer(player);
            case "phoenix_feather" -> {
                manager.addPhoenixFeather(player);
                consumed = true;
            }
        }

        if (consumed) {
            item.setAmount(item.getAmount() - 1);
            event.setCancelled(true);
        }
    }

    // ── Auto-pickup soul fragments ──
    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        ItemStack item = event.getItem().getItemStack();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        String type = meta.getPersistentDataContainer().get(manager.getKeyPowerUpType(), PersistentDataType.STRING);
        if ("soul_fragment".equals(type)) {
            int count = item.getAmount();
            for (int i = 0; i < count; i++) {
                if (!manager.consumeSoulFragment(player)) break;
            }
            event.setCancelled(true);
            event.getItem().remove();
        }
    }

    // ── Soul fragment damage multiplier ──
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        double multiplier = manager.getDamageMultiplier(player);
        if (multiplier > 1.0) {
            event.setDamage(event.getDamage() * multiplier);
        }
    }

    // ── Phoenix Feather auto-revive + Keep Inventory ──
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        // Phoenix Feather check
        if (manager.usePhoenixFeather(player)) {
            event.setCancelled(true);
            double maxHP = player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue();
            player.setHealth(maxHP * 0.5);
            player.getWorld().spawnParticle(Particle.FLAME, player.getLocation().add(0, 1, 0), 50, 1, 1.5, 1);
            player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation().add(0, 1, 0), 30, 0.5, 1, 0.5);
            player.playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1.0f, 1.0f);
            int remaining = player.getPersistentDataContainer().getOrDefault(manager.getKeyPhoenixFeathers(), PersistentDataType.INTEGER, 0);
            player.sendMessage(Component.text("Phoenix Feather saved you from death!", TextColor.color(255, 160, 30)).decorate(TextDecoration.BOLD)
                .append(Component.text(" (" + remaining + " remaining)", NamedTextColor.GOLD)));
            plugin.getLogger().info(player.getName() + " was saved by Phoenix Feather (" + remaining + " left)");
            return;
        }

        // Keep Inventory check
        if (manager.hasKeepInventory(player)) {
            event.setKeepInventory(true);
            event.getDrops().clear();
            event.setKeepLevel(true);
            event.setDroppedExp(0);
            player.sendMessage(Component.text("KeepInventorer preserved your inventory!", TextColor.color(0, 255, 200)));
        }
    }

    // ── Reapply boosts on join ──
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Delay by 1 tick to ensure player is fully loaded
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            manager.reapplyAllBoosts(event.getPlayer());
        }, 1L);
    }
}
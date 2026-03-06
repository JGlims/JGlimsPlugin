package com.jglims.plugin.quests;

import com.jglims.plugin.JGlimsPlugin;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.persistence.PersistentDataType;

/**
 * Bridges game events to the QuestManager for quest progress tracking.
 */
public class QuestProgressListener implements Listener {

    private final JGlimsPlugin plugin;
    private final QuestManager questManager;

    public QuestProgressListener(JGlimsPlugin plugin, QuestManager questManager) {
        this.plugin = plugin;
        this.questManager = questManager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();
        if (killer == null) return;

        // Check if it's a roaming boss
        if (entity.getPersistentDataContainer().has(plugin.getRoamingBossManager().getKeyRoamingBoss(), PersistentDataType.BYTE)) {
            String bossType = entity.getPersistentDataContainer().get(
                new org.bukkit.NamespacedKey(plugin, "roaming_boss_type"), PersistentDataType.STRING);
            if (bossType != null) {
                questManager.onRoamingBossKill(killer, bossType);
            }
            return;
        }

        // Check if it's a king mob
        if (entity.getPersistentDataContainer().has(new org.bukkit.NamespacedKey(plugin, "king_mob"), PersistentDataType.BYTE)) {
            questManager.onKingMobKill(killer);
            return;
        }

        // Ender Dragon
        if (entity instanceof EnderDragon) {
            questManager.onEnderDragonKill(killer);
            return;
        }

        // Hostile mob kill
        if (entity instanceof Monster || entity instanceof Slime || entity instanceof Phantom
                || entity instanceof Ghast || entity instanceof Shulker) {
            questManager.onHostileMobKill(killer, entity);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Material type = event.getBlock().getType();
        if (type == Material.DIAMOND_ORE || type == Material.DEEPSLATE_DIAMOND_ORE
                || type == Material.ANCIENT_DEBRIS
                || type == Material.EMERALD_ORE || type == Material.DEEPSLATE_EMERALD_ORE) {
            questManager.onOreMine(event.getPlayer(), type);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        World.Environment env = player.getWorld().getEnvironment();
        questManager.onDimensionVisit(player, env);

        // Abyss entry
        if (player.getWorld().getName().equalsIgnoreCase("world_abyss")) {
            questManager.onAbyssEntry(player);
        }
    }
}
package com.jglims.plugin.guilds;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import com.jglims.plugin.JGlimsPlugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class GuildListener implements Listener {

    private final JGlimsPlugin plugin;
    private final GuildManager guildManager;

    public GuildListener(JGlimsPlugin plugin, GuildManager guildManager) {
        this.plugin = plugin;
        this.guildManager = guildManager;
    }

    /**
     * Prevents friendly fire between guild members if guilds.friendly-fire is false.
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!plugin.getConfigManager().isGuildsEnabled()) return;
        if (plugin.getConfigManager().isGuildsFriendlyFire()) return; // FF allowed

        if (!(event.getEntity() instanceof Player victim)) return;
        if (!(event.getDamager() instanceof Player attacker)) return;

        if (guildManager.areInSameGuild(attacker.getUniqueId(), victim.getUniqueId())) {
            event.setCancelled(true);
            attacker.sendActionBar(Component.text("You cannot attack guild members!", NamedTextColor.RED));
        }
    }
}

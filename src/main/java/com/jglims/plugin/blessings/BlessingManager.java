package com.jglims.plugin.blessings;

import com.jglims.plugin.JGlimsPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class BlessingManager {

    private final JGlimsPlugin plugin;
    private final NamespacedKey cBlessKey;
    private final NamespacedKey amiBlessKey;
    private final NamespacedKey laBlessKey;

    public BlessingManager(JGlimsPlugin plugin) {
        this.plugin = plugin;
        this.cBlessKey = new NamespacedKey(plugin, "c_bless_count");
        this.amiBlessKey = new NamespacedKey(plugin, "ami_bless_count");
        this.laBlessKey = new NamespacedKey(plugin, "la_bless_count");
    }

    public NamespacedKey getCBlessKey() { return cBlessKey; }
    public NamespacedKey getAmiBlessKey() { return amiBlessKey; }
    public NamespacedKey getLaBlessKey() { return laBlessKey; }

    public int getBlessCount(Player player, NamespacedKey key) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        Integer val = pdc.get(key, PersistentDataType.INTEGER);
        return val != null ? val : 0;
    }

    public void setBlessCount(Player player, NamespacedKey key, int count) {
        player.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, count);
    }

    public void showStats(CommandSender sender, Player target) {
        int cCount = getBlessCount(target, cBlessKey);
        int amiCount = getBlessCount(target, amiBlessKey);
        int laCount = getBlessCount(target, laBlessKey);

        sender.sendMessage(Component.text("=== Blessings for " + target.getName() + " ===", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("C's Bless: " + cCount + "/" + plugin.getConfigManager().getCBlessMaxUses()
            + " (+" + cCount + " hearts)", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("Ami's Bless: " + amiCount + "/" + plugin.getConfigManager().getAmiBlessMaxUses()
            + " (+" + (amiCount * plugin.getConfigManager().getAmiBlessDmgPerUse()) + "% damage)", NamedTextColor.RED));
        sender.sendMessage(Component.text("La's Bless: " + laCount + "/" + plugin.getConfigManager().getLaBlessMaxUses()
            + " (+" + (laCount * plugin.getConfigManager().getLaBlessDefPerUse()) + "% defense)", NamedTextColor.AQUA));
    }

    // TODO: Implement blessing application logic (apply health, damage, defense)
    // TODO: Implement re-application on respawn (PlayerRespawnEvent)
}
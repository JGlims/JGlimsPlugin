package com.jglims.plugin.magic;

import com.jglims.plugin.JGlimsPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

/**
 * Manages magic items in JGlimsPlugin. Currently handles the Wand of Wands.
 * Extensible for future magic items.
 */
public class MagicItemManager {

    private final JGlimsPlugin plugin;
    private final NamespacedKey keyMagicItem;
    private final NamespacedKey keyWandOfWands;

    /** Cooldown tracking: player UUID + ability name → expiry timestamp. */
    private final Map<String, Long> cooldowns = new HashMap<>();

    public MagicItemManager(JGlimsPlugin plugin) {
        this.plugin = plugin;
        this.keyMagicItem = new NamespacedKey(plugin, "magic_item");
        this.keyWandOfWands = new NamespacedKey(plugin, "wand_of_wands");
    }

    // ── Item Creation ──────────────────────────────────────────────────

    /**
     * Creates the Wand of Wands magic item.
     * Found in Abyss chests (3%) and sold by The Arquimage NPC.
     *
     * @return the Wand of Wands ItemStack
     */
    public ItemStack createWandOfWands() {
        ItemStack wand = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = wand.getItemMeta();

        meta.displayName(Component.text("Wand of Wands", TextColor.color(170, 0, 170))
                .decorate(TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));

        meta.lore(List.of(
                Component.text(""),
                Component.text("Left Click", NamedTextColor.RED).decorate(TextDecoration.BOLD)
                        .append(Component.text(" — Stupefy", NamedTextColor.GRAY))
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("  Red bolt, 8 damage, 1s cooldown", NamedTextColor.DARK_GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text(""),
                Component.text("Right Click", NamedTextColor.GOLD).decorate(TextDecoration.BOLD)
                        .append(Component.text(" — Wingardium Leviosa", NamedTextColor.GRAY))
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("  Golden bolt, levitates target, 10s cooldown", NamedTextColor.DARK_GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text(""),
                Component.text("Crouch + Right Click", NamedTextColor.GREEN).decorate(TextDecoration.BOLD)
                        .append(Component.text(" — Avada Kedavra", NamedTextColor.GRAY))
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("  Green death bolt, 30s cooldown", NamedTextColor.DARK_GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text(""),
                Component.text("MYTHIC", TextColor.color(255, 85, 255)).decorate(TextDecoration.BOLD)
                        .decoration(TextDecoration.ITALIC, false)
        ));

        meta.setCustomModelData(40010);
        meta.getPersistentDataContainer().set(keyMagicItem, PersistentDataType.STRING, "wand_of_wands");
        meta.getPersistentDataContainer().set(keyWandOfWands, PersistentDataType.BYTE, (byte) 1);

        wand.setItemMeta(meta);
        return wand;
    }

    // ── Item Identification ────────────────────────────────────────────

    /**
     * Checks if an ItemStack is the Wand of Wands.
     */
    public boolean isWandOfWands(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(keyWandOfWands);
    }

    /**
     * Checks if an ItemStack is any magic item.
     */
    public boolean isMagicItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(keyMagicItem);
    }

    // ── Cooldown Management ────────────────────────────────────────────

    /**
     * Checks if an ability is off cooldown for a player.
     *
     * @param playerUUID the player UUID
     * @param ability    the ability name
     * @return true if the ability can be used
     */
    public boolean isReady(UUID playerUUID, String ability) {
        String key = playerUUID.toString() + ":" + ability;
        Long expiry = cooldowns.get(key);
        if (expiry == null) return true;
        if (System.currentTimeMillis() >= expiry) {
            cooldowns.remove(key);
            return true;
        }
        return false;
    }

    /**
     * Sets a cooldown for an ability.
     *
     * @param playerUUID the player UUID
     * @param ability    the ability name
     * @param seconds    cooldown duration in seconds
     */
    public void setCooldown(UUID playerUUID, String ability, double seconds) {
        String key = playerUUID.toString() + ":" + ability;
        cooldowns.put(key, System.currentTimeMillis() + (long) (seconds * 1000));
    }

    /**
     * Returns remaining cooldown in seconds, or 0 if ready.
     */
    public double getRemainingCooldown(UUID playerUUID, String ability) {
        String key = playerUUID.toString() + ":" + ability;
        Long expiry = cooldowns.get(key);
        if (expiry == null) return 0;
        long remaining = expiry - System.currentTimeMillis();
        return remaining > 0 ? remaining / 1000.0 : 0;
    }
}

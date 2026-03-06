package com.jglims.plugin.powerups;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.config.ConfigManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.UUID;

public class PowerUpManager {

    private final JGlimsPlugin plugin;

    // PDC keys
    private final NamespacedKey KEY_POWERUP_TYPE;
    private final NamespacedKey KEY_HEART_CRYSTALS;
    private final NamespacedKey KEY_SOUL_FRAGMENTS;
    private final NamespacedKey KEY_TITAN_RESOLVE;
    private final NamespacedKey KEY_KEEP_INVENTORY;
    private final NamespacedKey KEY_PHOENIX_FEATHERS;

    // Limits
    public static final int MAX_HEART_CRYSTALS = 10;
    public static final int MAX_SOUL_FRAGMENTS = 50;
    public static final double SOUL_FRAGMENT_DAMAGE_PERCENT = 0.5;

    public PowerUpManager(JGlimsPlugin plugin) {
        this.plugin = plugin;
        KEY_POWERUP_TYPE = new NamespacedKey(plugin, "powerup_type");
        KEY_HEART_CRYSTALS = new NamespacedKey(plugin, "heart_crystals");
        KEY_SOUL_FRAGMENTS = new NamespacedKey(plugin, "soul_fragments");
        KEY_TITAN_RESOLVE = new NamespacedKey(plugin, "titan_resolve");
        KEY_KEEP_INVENTORY = new NamespacedKey(plugin, "keep_inventory");
        KEY_PHOENIX_FEATHERS = new NamespacedKey(plugin, "phoenix_feathers");
    }

    // ── Item creation ──

    public ItemStack createHeartCrystal() {
        ItemStack item = new ItemStack(Material.RED_DYE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Heart Crystal", TextColor.color(255, 50, 50)).decorate(TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
            Component.text(""),
            Component.text("Permanently grants +1 max heart", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
            Component.text("Right-click to consume", NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false),
            Component.text("Max: " + MAX_HEART_CRYSTALS + " (" + (MAX_HEART_CRYSTALS * 2) + " extra HP)", NamedTextColor.DARK_RED).decoration(TextDecoration.ITALIC, false)
        ));
        meta.getPersistentDataContainer().set(KEY_POWERUP_TYPE, PersistentDataType.STRING, "heart_crystal");
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createSoulFragment() {
        ItemStack item = new ItemStack(Material.PURPLE_DYE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Soul Fragment", TextColor.color(160, 50, 255)).decorate(TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
            Component.text(""),
            Component.text("Permanently grants +0.5% damage", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
            Component.text("Auto-consumed on pickup", NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false),
            Component.text("Max: " + MAX_SOUL_FRAGMENTS + " (+" + (int)(MAX_SOUL_FRAGMENTS * SOUL_FRAGMENT_DAMAGE_PERCENT) + "% total)", NamedTextColor.DARK_PURPLE).decoration(TextDecoration.ITALIC, false)
        ));
        meta.getPersistentDataContainer().set(KEY_POWERUP_TYPE, PersistentDataType.STRING, "soul_fragment");
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createTitanResolve() {
        ItemStack item = new ItemStack(Material.IRON_NUGGET);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Titan's Resolve", TextColor.color(180, 180, 180)).decorate(TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
            Component.text(""),
            Component.text("Permanently grants +10% knockback resistance", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
            Component.text("Right-click to consume", NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false),
            Component.text("One-time use", NamedTextColor.DARK_RED).decoration(TextDecoration.ITALIC, false)
        ));
        meta.getPersistentDataContainer().set(KEY_POWERUP_TYPE, PersistentDataType.STRING, "titan_resolve");
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createPhoenixFeather() {
        ItemStack item = new ItemStack(Material.FEATHER);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Phoenix Feather", TextColor.color(255, 160, 30)).decorate(TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
            Component.text(""),
            Component.text("Auto-revives you on death at 50% HP", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
            Component.text("Consumed on use", NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false),
            Component.text("Stackable — each feather = 1 revive", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false)
        ));
        meta.getPersistentDataContainer().set(KEY_POWERUP_TYPE, PersistentDataType.STRING, "phoenix_feather");
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createKeepInventorer() {
        ItemStack item = new ItemStack(Material.ENDER_EYE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("KeepInventorer", TextColor.color(0, 255, 200)).decorate(TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
            Component.text(""),
            Component.text("Permanently enables keep-inventory", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
            Component.text("Right-click to consume", NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false),
            Component.text("One-time use — permanent effect", NamedTextColor.DARK_AQUA).decoration(TextDecoration.ITALIC, false)
        ));
        meta.getPersistentDataContainer().set(KEY_POWERUP_TYPE, PersistentDataType.STRING, "keep_inventorer");
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    // ── Getters for PDC keys ──

    public NamespacedKey getKeyPowerUpType() { return KEY_POWERUP_TYPE; }
    public NamespacedKey getKeyHeartCrystals() { return KEY_HEART_CRYSTALS; }
    public NamespacedKey getKeySoulFragments() { return KEY_SOUL_FRAGMENTS; }
    public NamespacedKey getKeyTitanResolve() { return KEY_TITAN_RESOLVE; }
    public NamespacedKey getKeyKeepInventory() { return KEY_KEEP_INVENTORY; }
    public NamespacedKey getKeyPhoenixFeathers() { return KEY_PHOENIX_FEATHERS; }

    // ── Application methods ──

    public boolean consumeHeartCrystal(Player player) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        int current = pdc.getOrDefault(KEY_HEART_CRYSTALS, PersistentDataType.INTEGER, 0);
        if (current >= MAX_HEART_CRYSTALS) {
            player.sendMessage(Component.text("You already have the maximum " + MAX_HEART_CRYSTALS + " Heart Crystals!", NamedTextColor.RED));
            return false;
        }
        current++;
        pdc.set(KEY_HEART_CRYSTALS, PersistentDataType.INTEGER, current);
        applyHealthBoost(player, current);
        player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0, 2, 0), 15, 0.5, 0.5, 0.5);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
        player.sendMessage(Component.text("Heart Crystal consumed! (", NamedTextColor.GREEN)
            .append(Component.text(current + "/" + MAX_HEART_CRYSTALS, TextColor.color(255, 50, 50)))
            .append(Component.text(") — +" + (current * 2) + " max HP", NamedTextColor.GREEN)));
        return true;
    }

    public boolean consumeSoulFragment(Player player) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        int current = pdc.getOrDefault(KEY_SOUL_FRAGMENTS, PersistentDataType.INTEGER, 0);
        if (current >= MAX_SOUL_FRAGMENTS) {
            player.sendMessage(Component.text("You already have the maximum " + MAX_SOUL_FRAGMENTS + " Soul Fragments!", NamedTextColor.RED));
            return false;
        }
        current++;
        pdc.set(KEY_SOUL_FRAGMENTS, PersistentDataType.INTEGER, current);
        player.getWorld().spawnParticle(Particle.SOUL, player.getLocation().add(0, 1, 0), 10, 0.3, 0.5, 0.3);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 0.8f);
        double totalBonus = current * SOUL_FRAGMENT_DAMAGE_PERCENT;
        player.sendMessage(Component.text("Soul Fragment absorbed! (", TextColor.color(160, 50, 255))
            .append(Component.text(current + "/" + MAX_SOUL_FRAGMENTS, NamedTextColor.LIGHT_PURPLE))
            .append(Component.text(") — +" + String.format("%.1f", totalBonus) + "% damage", TextColor.color(160, 50, 255))));
        return true;
    }

    public boolean consumeTitanResolve(Player player) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        int current = pdc.getOrDefault(KEY_TITAN_RESOLVE, PersistentDataType.INTEGER, 0);
        if (current >= 1) {
            player.sendMessage(Component.text("You already have Titan's Resolve!", NamedTextColor.RED));
            return false;
        }
        pdc.set(KEY_TITAN_RESOLVE, PersistentDataType.INTEGER, 1);
        applyKnockbackResistance(player);
        player.getWorld().spawnParticle(Particle.CRIT, player.getLocation().add(0, 1, 0), 30, 0.5, 1, 0.5);
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 0.7f);
        player.sendMessage(Component.text("Titan's Resolve activated! +10% knockback resistance permanently!", NamedTextColor.GRAY).decorate(TextDecoration.BOLD));
        return true;
    }

    public boolean consumeKeepInventorer(Player player) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        int current = pdc.getOrDefault(KEY_KEEP_INVENTORY, PersistentDataType.INTEGER, 0);
        if (current >= 1) {
            player.sendMessage(Component.text("You already have permanent keep-inventory!", NamedTextColor.RED));
            return false;
        }
        pdc.set(KEY_KEEP_INVENTORY, PersistentDataType.INTEGER, 1);
        player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0, 1, 0), 25, 0.5, 1, 0.5);
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.2f);
        player.sendMessage(Component.text("KeepInventorer activated! You will permanently keep your inventory on death!", TextColor.color(0, 255, 200)).decorate(TextDecoration.BOLD));
        return true;
    }

    public int addPhoenixFeather(Player player) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        int current = pdc.getOrDefault(KEY_PHOENIX_FEATHERS, PersistentDataType.INTEGER, 0);
        current++;
        pdc.set(KEY_PHOENIX_FEATHERS, PersistentDataType.INTEGER, current);
        player.getWorld().spawnParticle(Particle.FLAME, player.getLocation().add(0, 1, 0), 20, 0.4, 0.8, 0.4);
        player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_AMBIENT, 0.7f, 1.5f);
        player.sendMessage(Component.text("Phoenix Feather stored! (", NamedTextColor.GOLD)
            .append(Component.text(current + " feathers", TextColor.color(255, 160, 30)))
            .append(Component.text(") — auto-revive on death", NamedTextColor.GOLD)));
        return current;
    }

    // ── Stat application helpers ──

    public void applyHealthBoost(Player player, int heartCrystals) {
        AttributeInstance attr = player.getAttribute(Attribute.MAX_HEALTH);
        if (attr == null) return;
        // Remove old modifier if exists
        NamespacedKey modKey = new NamespacedKey(plugin, "heart_crystal_boost");
        attr.getModifiers().stream()
            .filter(m -> m.getKey().equals(modKey))
            .forEach(attr::removeModifier);
        if (heartCrystals > 0) {
            double extraHP = heartCrystals * 2.0;
            attr.addModifier(new AttributeModifier(modKey, extraHP, AttributeModifier.Operation.ADD_NUMBER));
        }
    }

    public void applyKnockbackResistance(Player player) {
        AttributeInstance attr = player.getAttribute(Attribute.KNOCKBACK_RESISTANCE);
        if (attr == null) return;
        NamespacedKey modKey = new NamespacedKey(plugin, "titan_resolve_boost");
        attr.getModifiers().stream()
            .filter(m -> m.getKey().equals(modKey))
            .forEach(attr::removeModifier);
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        int titan = pdc.getOrDefault(KEY_TITAN_RESOLVE, PersistentDataType.INTEGER, 0);
        if (titan > 0) {
            attr.addModifier(new AttributeModifier(modKey, 0.1, AttributeModifier.Operation.ADD_NUMBER));
        }
    }

    public double getDamageMultiplier(Player player) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        int fragments = pdc.getOrDefault(KEY_SOUL_FRAGMENTS, PersistentDataType.INTEGER, 0);
        return 1.0 + (fragments * SOUL_FRAGMENT_DAMAGE_PERCENT / 100.0);
    }

    public boolean hasKeepInventory(Player player) {
        return player.getPersistentDataContainer().getOrDefault(KEY_KEEP_INVENTORY, PersistentDataType.INTEGER, 0) >= 1;
    }

    public boolean usePhoenixFeather(Player player) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        int feathers = pdc.getOrDefault(KEY_PHOENIX_FEATHERS, PersistentDataType.INTEGER, 0);
        if (feathers <= 0) return false;
        pdc.set(KEY_PHOENIX_FEATHERS, PersistentDataType.INTEGER, feathers - 1);
        return true;
    }

    /** Re-apply all persistent boosts on join (health, knockback resistance). */
    public void reapplyAllBoosts(Player player) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        int hearts = pdc.getOrDefault(KEY_HEART_CRYSTALS, PersistentDataType.INTEGER, 0);
        if (hearts > 0) applyHealthBoost(player, hearts);
        int titan = pdc.getOrDefault(KEY_TITAN_RESOLVE, PersistentDataType.INTEGER, 0);
        if (titan > 0) applyKnockbackResistance(player);
    }

    public void showPowerUpStats(Player player) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        int hearts = pdc.getOrDefault(KEY_HEART_CRYSTALS, PersistentDataType.INTEGER, 0);
        int souls = pdc.getOrDefault(KEY_SOUL_FRAGMENTS, PersistentDataType.INTEGER, 0);
        int titan = pdc.getOrDefault(KEY_TITAN_RESOLVE, PersistentDataType.INTEGER, 0);
        int keep = pdc.getOrDefault(KEY_KEEP_INVENTORY, PersistentDataType.INTEGER, 0);
        int feathers = pdc.getOrDefault(KEY_PHOENIX_FEATHERS, PersistentDataType.INTEGER, 0);

        player.sendMessage(Component.text("=== Power-Up Stats ===", NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
        player.sendMessage(Component.text("Heart Crystals: ", NamedTextColor.GRAY).append(Component.text(hearts + "/" + MAX_HEART_CRYSTALS + " (+" + (hearts * 2) + " HP)", TextColor.color(255, 50, 50))));
        player.sendMessage(Component.text("Soul Fragments: ", NamedTextColor.GRAY).append(Component.text(souls + "/" + MAX_SOUL_FRAGMENTS + " (+" + String.format("%.1f", souls * SOUL_FRAGMENT_DAMAGE_PERCENT) + "% dmg)", TextColor.color(160, 50, 255))));
        player.sendMessage(Component.text("Titan's Resolve: ", NamedTextColor.GRAY).append(Component.text(titan > 0 ? "Active (+10% KB resist)" : "Not acquired", titan > 0 ? NamedTextColor.GREEN : NamedTextColor.DARK_GRAY)));
        player.sendMessage(Component.text("KeepInventory: ", NamedTextColor.GRAY).append(Component.text(keep > 0 ? "Active (permanent)" : "Not acquired", keep > 0 ? NamedTextColor.GREEN : NamedTextColor.DARK_GRAY)));
        player.sendMessage(Component.text("Phoenix Feathers: ", NamedTextColor.GRAY).append(Component.text(feathers + " (auto-revives remaining)", TextColor.color(255, 160, 30))));
    }

    public JGlimsPlugin getPlugin() { return plugin; }
}
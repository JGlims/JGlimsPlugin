package com.jglims.plugin.blessings;

import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.config.ConfigManager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class BlessingManager {

    private final JGlimsPlugin plugin;

    // PDC keys for tracking how many times each blessing has been used
    private final NamespacedKey cBlessUsesKey;
    private final NamespacedKey amiBlessUsesKey;
    private final NamespacedKey laBlessUsesKey;

    // Attribute modifier keys
    private final NamespacedKey cBlessModKey;
    private final NamespacedKey amiBlessModKey;
    private final NamespacedKey laBlessModKey;

    // PDC keys for identifying blessing items
    private final NamespacedKey cBlessItemKey;
    private final NamespacedKey amiBlessItemKey;
    private final NamespacedKey laBlessItemKey;

    public BlessingManager(JGlimsPlugin plugin) {
        this.plugin = plugin;
        this.cBlessUsesKey = new NamespacedKey(plugin, "c_bless_uses");
        this.amiBlessUsesKey = new NamespacedKey(plugin, "ami_bless_uses");
        this.laBlessUsesKey = new NamespacedKey(plugin, "la_bless_uses");
        this.cBlessModKey = new NamespacedKey(plugin, "c_bless_health");
        this.amiBlessModKey = new NamespacedKey(plugin, "ami_bless_damage");
        this.laBlessModKey = new NamespacedKey(plugin, "la_bless_defense");
        this.cBlessItemKey = new NamespacedKey(plugin, "c_bless_item");
        this.amiBlessItemKey = new NamespacedKey(plugin, "ami_bless_item");
        this.laBlessItemKey = new NamespacedKey(plugin, "la_bless_item");
    }

    public NamespacedKey getCBlessItemKey() { return cBlessItemKey; }
    public NamespacedKey getAmiBlessItemKey() { return amiBlessItemKey; }
    public NamespacedKey getLaBlessItemKey() { return laBlessItemKey; }

    // ========================================================================
    // C'S BLESS: +1 heart (2 HP) per use, max 10 uses (base 20 -> max 40 HP)
    // ========================================================================
    public boolean applyCBless(Player player) {
        ConfigManager cfg = plugin.getConfigManager();
        PersistentDataContainer pdc = player.getPersistentDataContainer();

        int currentUses = pdc.getOrDefault(cBlessUsesKey, PersistentDataType.INTEGER, 0);
        if (currentUses >= cfg.getCBlessMaxUses()) {
            player.sendMessage(Component.text("You have already used C's Bless the maximum number of times!",
                NamedTextColor.RED));
            return false;
        }

        int newUses = currentUses + 1;
        pdc.set(cBlessUsesKey, PersistentDataType.INTEGER, newUses);

        // Apply health modifier
        applyHealthModifier(player, newUses);

        player.sendMessage(Component.text("C's Bless applied! +" + newUses + " bonus heart(s) ("
            + newUses + "/" + cfg.getCBlessMaxUses() + ")", NamedTextColor.GOLD));
        return true;
    }

    // ========================================================================
    // AMI'S BLESS: +2% melee damage per use, max 10 uses (+20% total)
    // ========================================================================
    public boolean applyAmiBless(Player player) {
        ConfigManager cfg = plugin.getConfigManager();
        PersistentDataContainer pdc = player.getPersistentDataContainer();

        int currentUses = pdc.getOrDefault(amiBlessUsesKey, PersistentDataType.INTEGER, 0);
        if (currentUses >= cfg.getAmiBlessMaxUses()) {
            player.sendMessage(Component.text("You have already used Ami's Bless the maximum number of times!",
                NamedTextColor.RED));
            return false;
        }

        int newUses = currentUses + 1;
        pdc.set(amiBlessUsesKey, PersistentDataType.INTEGER, newUses);

        // Apply damage modifier
        applyDamageModifier(player, newUses);

        double totalPercent = newUses * cfg.getAmiBlessDmgPerUse();
        player.sendMessage(Component.text("Ami's Bless applied! +" + String.format("%.0f", totalPercent)
            + "% melee damage (" + newUses + "/" + cfg.getAmiBlessMaxUses() + ")", NamedTextColor.RED));
        return true;
    }

    // ========================================================================
    // LA'S BLESS: +2 armor points per use, max 10 uses (+20 total)
    // BUG 5 FIX: Changed from +1.0 to using config value (default 2.0)
    // This maximizes visible armor icons in the HUD
    // ========================================================================
    public boolean applyLaBless(Player player) {
        ConfigManager cfg = plugin.getConfigManager();
        PersistentDataContainer pdc = player.getPersistentDataContainer();

        int currentUses = pdc.getOrDefault(laBlessUsesKey, PersistentDataType.INTEGER, 0);
        if (currentUses >= cfg.getLaBlessMaxUses()) {
            player.sendMessage(Component.text("You have already used La's Bless the maximum number of times!",
                NamedTextColor.RED));
            return false;
        }

        int newUses = currentUses + 1;
        pdc.set(laBlessUsesKey, PersistentDataType.INTEGER, newUses);

        // Apply armor modifier
        applyArmorModifier(player, newUses);

        double totalArmor = newUses * cfg.getLaBlessDefPerUse();
        player.sendMessage(Component.text("La's Bless applied! +" + String.format("%.0f", totalArmor)
            + " armor points (" + newUses + "/" + cfg.getLaBlessMaxUses() + ")", NamedTextColor.BLUE));
        return true;
    }

    // ========================================================================
    // REAPPLY ALL MODIFIERS — Called on respawn and join
    // ========================================================================
    public void reapplyAllModifiers(Player player) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();

        int cUses = pdc.getOrDefault(cBlessUsesKey, PersistentDataType.INTEGER, 0);
        if (cUses > 0) applyHealthModifier(player, cUses);

        int amiUses = pdc.getOrDefault(amiBlessUsesKey, PersistentDataType.INTEGER, 0);
        if (amiUses > 0) applyDamageModifier(player, amiUses);

        int laUses = pdc.getOrDefault(laBlessUsesKey, PersistentDataType.INTEGER, 0);
        if (laUses > 0) applyArmorModifier(player, laUses);
    }

    // ========================================================================
    // SHOW STATS — /jglims stats <player>
    // ========================================================================
    public void showStats(CommandSender sender, Player target) {
        PersistentDataContainer pdc = target.getPersistentDataContainer();
        ConfigManager cfg = plugin.getConfigManager();

        int cUses = pdc.getOrDefault(cBlessUsesKey, PersistentDataType.INTEGER, 0);
        int amiUses = pdc.getOrDefault(amiBlessUsesKey, PersistentDataType.INTEGER, 0);
        int laUses = pdc.getOrDefault(laBlessUsesKey, PersistentDataType.INTEGER, 0);

        sender.sendMessage(Component.text("=== Blessing Stats: " + target.getName() + " ===", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("C's Bless: " + cUses + "/" + cfg.getCBlessMaxUses()
            + " (+" + cUses + " hearts)", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("Ami's Bless: " + amiUses + "/" + cfg.getAmiBlessMaxUses()
            + " (+" + String.format("%.0f", amiUses * cfg.getAmiBlessDmgPerUse()) + "% damage)", NamedTextColor.RED));
        sender.sendMessage(Component.text("La's Bless: " + laUses + "/" + cfg.getLaBlessMaxUses()
            + " (+" + String.format("%.0f", laUses * cfg.getLaBlessDefPerUse()) + " armor)", NamedTextColor.BLUE));
    }

    // ========================================================================
    // INTERNAL: Apply attribute modifiers
    // ========================================================================
    private void applyHealthModifier(Player player, int uses) {
        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealth == null) return;

        // Remove old modifier
        maxHealth.getModifiers().stream()
            .filter(m -> m.getKey().equals(cBlessModKey))
            .forEach(maxHealth::removeModifier);

        // Add new: +2 HP per use (1 heart = 2 HP)
        double bonus = uses * 2.0;
        maxHealth.addModifier(new AttributeModifier(
            cBlessModKey, bonus, AttributeModifier.Operation.ADD_NUMBER));
    }

    private void applyDamageModifier(Player player, int uses) {
        AttributeInstance attackDamage = player.getAttribute(Attribute.ATTACK_DAMAGE);
        if (attackDamage == null) return;

        // Remove old modifier
        attackDamage.getModifiers().stream()
            .filter(m -> m.getKey().equals(amiBlessModKey))
            .forEach(attackDamage::removeModifier);

        // Add new: +2% per use as a multiplier
        double bonusPercent = uses * plugin.getConfigManager().getAmiBlessDmgPerUse() / 100.0;
        attackDamage.addModifier(new AttributeModifier(
            amiBlessModKey, bonusPercent, AttributeModifier.Operation.MULTIPLY_SCALAR_1));
    }

    // BUG 5 FIX: Uses config value (default 2.0) instead of hardcoded 1.0
    private void applyArmorModifier(Player player, int uses) {
        AttributeInstance armor = player.getAttribute(Attribute.ARMOR);
        if (armor == null) return;

        // Remove old modifier
        armor.getModifiers().stream()
            .filter(m -> m.getKey().equals(laBlessModKey))
            .forEach(armor::removeModifier);

        // Add new: uses * config value armor points per use (default 2.0)
        double bonusArmor = uses * plugin.getConfigManager().getLaBlessDefPerUse();
        armor.addModifier(new AttributeModifier(
            laBlessModKey, bonusArmor, AttributeModifier.Operation.ADD_NUMBER));
    }
}
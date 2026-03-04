package com.jglims.plugin.weapons;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.config.ConfigManager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class WeaponMasteryManager implements Listener {

    private final JGlimsPlugin plugin;
    private final ConfigManager config;

    // PDC keys for each weapon class kill count
    private final NamespacedKey swordKillsKey;
    private final NamespacedKey axeKillsKey;
    private final NamespacedKey pickaxeKillsKey;
    private final NamespacedKey shovelKillsKey;
    private final NamespacedKey sickleKillsKey;
    private final NamespacedKey bowKillsKey;
    private final NamespacedKey crossbowKillsKey;
    private final NamespacedKey tridentKillsKey;
    private final NamespacedKey maceKillsKey;
    private final NamespacedKey spearKillsKey;

    // Attribute modifier key
    private final NamespacedKey masteryModKey;

    // Cache: player UUID -> current weapon class
    private final Map<UUID, String> lastWeaponClass = new HashMap<>();

    public WeaponMasteryManager(JGlimsPlugin plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
        this.swordKillsKey = new NamespacedKey(plugin, "mastery_sword");
        this.axeKillsKey = new NamespacedKey(plugin, "mastery_axe");
        this.pickaxeKillsKey = new NamespacedKey(plugin, "mastery_pickaxe");
        this.shovelKillsKey = new NamespacedKey(plugin, "mastery_shovel");
        this.sickleKillsKey = new NamespacedKey(plugin, "mastery_sickle");
        this.bowKillsKey = new NamespacedKey(plugin, "mastery_bow");
        this.crossbowKillsKey = new NamespacedKey(plugin, "mastery_crossbow");
        this.tridentKillsKey = new NamespacedKey(plugin, "mastery_trident");
        this.maceKillsKey = new NamespacedKey(plugin, "mastery_mace");
        this.spearKillsKey = new NamespacedKey(plugin, "mastery_spear");
        this.masteryModKey = new NamespacedKey(plugin, "mastery_damage");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        if (!config.isWeaponMasteryEnabled()) return;

        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Monster)) return;

        Player killer = entity.getKiller();
        if (killer == null) return;

        ItemStack weapon = killer.getInventory().getItemInMainHand();
        String weaponClass = getWeaponClass(killer, weapon);
        if (weaponClass == null) return;

        NamespacedKey key = getKeyForClass(weaponClass);
        if (key == null) return;

        PersistentDataContainer pdc = killer.getPersistentDataContainer();
        int kills = pdc.getOrDefault(key, PersistentDataType.INTEGER, 0) + 1;
        pdc.set(key, PersistentDataType.INTEGER, kills);

        applyMasteryModifier(killer, weaponClass);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!config.isWeaponMasteryEnabled()) return;
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            Player player = event.getPlayer();
            if (player.isOnline()) {
                ItemStack weapon = player.getInventory().getItemInMainHand();
                String wc = getWeaponClass(player, weapon);
                if (wc != null) applyMasteryModifier(player, wc);
            }
        }, 10L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (!config.isWeaponMasteryEnabled()) return;
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            Player player = event.getPlayer();
            if (player.isOnline()) {
                ItemStack weapon = player.getInventory().getItemInMainHand();
                String wc = getWeaponClass(player, weapon);
                if (wc != null) applyMasteryModifier(player, wc);
            }
        }, 5L);
    }

    public void applyMasteryModifier(Player player, String weaponClass) {
        AttributeInstance attackDamage = player.getAttribute(Attribute.ATTACK_DAMAGE);
        if (attackDamage == null) return;

        // Remove old modifier
        attackDamage.getModifiers().stream()
                .filter(m -> m.getKey().equals(masteryModKey))
                .forEach(attackDamage::removeModifier);

        if (weaponClass == null) return;

        NamespacedKey key = getKeyForClass(weaponClass);
        if (key == null) return;

        int kills = player.getPersistentDataContainer().getOrDefault(key, PersistentDataType.INTEGER, 0);
        int maxKills = config.getMasteryMaxKills();
        double maxBonus = config.getMasteryMaxBonusPercent() / 100.0;

        double bonusPercent = Math.min((double) kills / maxKills, 1.0) * maxBonus;

        if (bonusPercent > 0) {
            attackDamage.addModifier(new AttributeModifier(
                    masteryModKey, bonusPercent, AttributeModifier.Operation.MULTIPLY_SCALAR_1));
        }

        lastWeaponClass.put(player.getUniqueId(), weaponClass);
    }

    public void removeMasteryModifier(Player player) {
        AttributeInstance attackDamage = player.getAttribute(Attribute.ATTACK_DAMAGE);
        if (attackDamage == null) return;
        attackDamage.getModifiers().stream()
                .filter(m -> m.getKey().equals(masteryModKey))
                .forEach(attackDamage::removeModifier);
        lastWeaponClass.remove(player.getUniqueId());
    }

    public void showMastery(Player player) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        int maxK = config.getMasteryMaxKills();
        double maxB = config.getMasteryMaxBonusPercent();

        player.sendMessage(Component.text("=== Weapon Mastery ===", NamedTextColor.GOLD));

        String[] classes = {"sword", "axe", "pickaxe", "shovel", "sickle", "bow", "crossbow", "trident", "mace", "spear"};
        for (String wc : classes) {
            NamespacedKey key = getKeyForClass(wc);
            if (key == null) continue;
            int kills = pdc.getOrDefault(key, PersistentDataType.INTEGER, 0);
            double bonus = Math.min((double) kills / maxK, 1.0) * maxB;
            player.sendMessage(Component.text(
                    " " + capitalize(wc) + ": " + kills + "/" + maxK + " kills (+" + String.format("%.1f", bonus) + "% damage)",
                    NamedTextColor.YELLOW));
        }
    }

    private String getWeaponClass(Player player, ItemStack weapon) {
        if (weapon == null || weapon.getType().isAir()) return null;
        String matName = weapon.getType().name();

        // Check custom battle weapons first (specific PDC markers)
        if (plugin.getSickleManager().isSickle(weapon)) return "sickle";
        if (plugin.getBattleAxeManager().isBattleAxe(weapon)) return "axe";
        if (plugin.getBattleSwordManager().isBattleSword(weapon)) return "sword";
        if (plugin.getBattlePickaxeManager().isBattlePickaxe(weapon)) return "pickaxe";
        if (plugin.getBattleTridentManager().isBattleTrident(weapon)) return "trident";
        if (plugin.getBattleSpearManager().isBattleSpear(weapon)) return "spear";
        if (plugin.getBattleShovelManager().isBattleShovel(weapon)) return "shovel";
        if (plugin.getBattleMaceManager() != null && plugin.getBattleMaceManager().isBattleMace(weapon)) return "mace";
        if (plugin.getBattleBowManager().isBattleBow(weapon)) return "bow";
        if (plugin.getBattleBowManager().isBattleCrossbow(weapon)) return "crossbow";

        // Fallback to vanilla material checks
        if (matName.endsWith("_SWORD")) return "sword";
        if (matName.endsWith("_AXE")) return "axe";
        if (matName.endsWith("_PICKAXE")) return "pickaxe";
        if (matName.endsWith("_SHOVEL")) return "shovel";
        if (matName.endsWith("_SPEAR")) return "spear";
        if (weapon.getType() == Material.BOW) return "bow";
        if (weapon.getType() == Material.CROSSBOW) return "crossbow";
        if (weapon.getType() == Material.TRIDENT) return "trident";
        if (weapon.getType() == Material.MACE) return "mace";

        return null;
    }

    private NamespacedKey getKeyForClass(String weaponClass) {
        return switch (weaponClass) {
            case "sword" -> swordKillsKey;
            case "axe" -> axeKillsKey;
            case "pickaxe" -> pickaxeKillsKey;
            case "shovel" -> shovelKillsKey;
            case "sickle" -> sickleKillsKey;
            case "bow" -> bowKillsKey;
            case "crossbow" -> crossbowKillsKey;
            case "trident" -> tridentKillsKey;
            case "mace" -> maceKillsKey;
            case "spear" -> spearKillsKey;
            default -> null;
        };
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}

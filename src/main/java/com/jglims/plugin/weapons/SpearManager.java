package com.jglims.plugin.weapons;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import com.jglims.plugin.config.ConfigManager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class SpearManager {

    private final JavaPlugin plugin;
    private final ConfigManager config;

    // PDC keys
    private final NamespacedKey superTierKey; // Stores super tier: 1=Iron, 2=Diamond, 3=Netherite

    /**
     * Spear material tiers. Spears skip the "Battle" step entirely.
     * Path: Vanilla Spear -> Super Iron -> Super Diamond -> Super Netherite (Definitive)
     */
    public enum SpearTier {
        WOODEN(Material.WOODEN_SPEAR, Material.OAK_PLANKS, 1.0, 1.54),
        STONE(Material.STONE_SPEAR, Material.COBBLESTONE, 2.0, 1.33),
        COPPER(Material.COPPER_SPEAR, Material.COPPER_INGOT, 2.0, 1.18),
        GOLDEN(Material.GOLDEN_SPEAR, Material.GOLD_INGOT, 2.0, 1.05),
        IRON(Material.IRON_SPEAR, Material.IRON_INGOT, 3.0, 1.05),
        DIAMOND(Material.DIAMOND_SPEAR, Material.DIAMOND, 4.0, 0.95),
        NETHERITE(Material.NETHERITE_SPEAR, Material.NETHERITE_INGOT, 5.0, 0.87);

        private final Material spearMaterial;
        private final Material repairIngredient;
        private final double jabDamage;
        private final double attackSpeed;

        SpearTier(Material spearMaterial, Material repairIngredient, double jabDamage, double attackSpeed) {
            this.spearMaterial = spearMaterial;
            this.repairIngredient = repairIngredient;
            this.jabDamage = jabDamage;
            this.attackSpeed = attackSpeed;
        }

        public Material getSpearMaterial() { return spearMaterial; }
        public Material getRepairIngredient() { return repairIngredient; }
        public double getJabDamage() { return jabDamage; }
        public double getAttackSpeed() { return attackSpeed; }

        public static SpearTier fromMaterial(Material mat) {
            for (SpearTier tier : values()) {
                if (tier.spearMaterial == mat) return tier;
            }
            return null;
        }

        /**
         * Returns the correct upgrade ingredient material for super upgrades.
         * Iron Super = IRON_INGOT, Diamond Super = DIAMOND, Netherite Super = NETHERITE_INGOT
         */
        public static Material getSuperUpgradeIngredient(int superTier) {
            return switch (superTier) {
                case 1 -> Material.IRON_INGOT;
                case 2 -> Material.DIAMOND;
                case 3 -> Material.NETHERITE_INGOT;
                default -> null;
            };
        }
    }

    // All spear materials for quick lookups
    private static final Set<Material> SPEAR_MATERIALS = EnumSet.of(
            Material.WOODEN_SPEAR, Material.STONE_SPEAR, Material.COPPER_SPEAR,
            Material.GOLDEN_SPEAR, Material.IRON_SPEAR, Material.DIAMOND_SPEAR,
            Material.NETHERITE_SPEAR
    );

    public SpearManager(JavaPlugin plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
        this.superTierKey = new NamespacedKey(plugin, "super_spear_tier");
    }

    /**
     * Checks if a material is any spear type.
     */
    public static boolean isSpear(Material mat) {
        return SPEAR_MATERIALS.contains(mat);
    }

    /**
     * Checks if an item is a spear.
     */
    public boolean isSpear(ItemStack item) {
        return item != null && SPEAR_MATERIALS.contains(item.getType());
    }

    /**
     * Gets the super tier of a spear (0 = not super, 1 = Iron, 2 = Diamond, 3 = Netherite).
     */
    public int getSuperTier(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return 0;
        return item.getItemMeta().getPersistentDataContainer()
                .getOrDefault(superTierKey, PersistentDataType.INTEGER, 0);
    }

    /**
     * Checks if a spear is a super spear.
     */
    public boolean isSuperSpear(ItemStack item) {
        return getSuperTier(item) > 0;
    }

    /**
     * Gets the SpearTier for a given item.
     */
    public SpearTier getSpearTier(ItemStack item) {
        if (item == null) return null;
        return SpearTier.fromMaterial(item.getType());
    }

    /**
     * Creates a Super Spear from an existing spear.
     * Spears skip the Battle step: Vanilla -> Super Iron -> Super Diamond -> Super Netherite.
     *
     * @param baseSpear The existing spear item (vanilla or lower super tier)
     * @param newSuperTier The target super tier (1=Iron, 2=Diamond, 3=Netherite)
     * @return The new super spear, or null if invalid upgrade
     */
    public ItemStack createSuperSpear(ItemStack baseSpear, int newSuperTier) {
        if (baseSpear == null || !isSpear(baseSpear)) return null;
        if (newSuperTier < 1 || newSuperTier > 3) return null;

        SpearTier spearTier = getSpearTier(baseSpear);
        if (spearTier == null) return null;

        // Validate upgrade path
        int currentSuper = getSuperTier(baseSpear);
        if (newSuperTier <= currentSuper) return null; // Can't downgrade

        // Clone to preserve enchantments
        ItemStack result = baseSpear.clone();
        ItemMeta meta = result.getItemMeta();
        if (meta == null) return null;

        // Set super tier
        meta.getPersistentDataContainer().set(superTierKey, PersistentDataType.INTEGER, newSuperTier);

        // Calculate accumulated damage bonus
        double baseDamage = spearTier.getJabDamage();
        double totalBonus = 0;
        for (int i = 1; i <= newSuperTier; i++) {
            totalBonus += switch (i) {
                case 1 -> config.getSuperIronBonusDamage();
                case 2 -> config.getSuperDiamondBonusDamage();
                case 3 -> config.getSuperNetheriteBonusDamage();
                default -> 0;
            };
        }
        double totalDamage = baseDamage + totalBonus;

        // Super tier name
        String superName = switch (newSuperTier) {
            case 1 -> "Iron";
            case 2 -> "Diamond";
            case 3 -> "Netherite";
            default -> "Unknown";
        };

        String tierName = spearTier.name().charAt(0) + spearTier.name().substring(1).toLowerCase();

        // Display name — Adventure API
        if (newSuperTier == 3) {
            meta.displayName(Component.text("\u2726 Definitive " + tierName + " Spear \u2726", NamedTextColor.DARK_RED)
                    .decoration(TextDecoration.ITALIC, false)
                    .decoration(TextDecoration.BOLD, true));
        } else {
            meta.displayName(Component.text("Super " + superName + " " + tierName + " Spear",
                            newSuperTier == 2 ? NamedTextColor.AQUA : NamedTextColor.WHITE)
                    .decoration(TextDecoration.ITALIC, false));
        }

        // Build lore — Adventure API uniform pattern
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Custom Weapon", NamedTextColor.DARK_PURPLE)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("Attack Damage: ", NamedTextColor.GRAY)
                .append(Component.text(String.format("%.0f", baseDamage), NamedTextColor.GREEN))
                .append(Component.text(" +", NamedTextColor.GRAY))
                .append(Component.text(String.format("%.0f", totalBonus), NamedTextColor.YELLOW))
                .append(Component.text(" = ", NamedTextColor.GRAY))
                .append(Component.text(String.format("%.0f", totalDamage), NamedTextColor.WHITE))
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Attack Speed: ", NamedTextColor.GRAY)
                .append(Component.text(String.format("%.2f", spearTier.getAttackSpeed()), NamedTextColor.WHITE))
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Attack Range: ", NamedTextColor.GRAY)
                .append(Component.text("2.0 - 4.5 blocks", NamedTextColor.WHITE))
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());

        // Ability info
        switch (newSuperTier) {
            case 1 -> {
                lore.add(Component.text("Super Iron \u2014 No special ability", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text("Upgrade to Diamond for abilities.", NamedTextColor.DARK_GRAY)
                        .decoration(TextDecoration.ITALIC, false));
            }
            case 2 -> {
                lore.add(Component.text("\u25C6 Right-click: Phantom Lunge", NamedTextColor.AQUA)
                        .decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text("Dash forward 8 blocks, damaging all", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text("enemies in your path. 7s cooldown", NamedTextColor.DARK_GRAY)
                        .decoration(TextDecoration.ITALIC, false));
            }
            case 3 -> {
                lore.add(Component.text("\u25C6 Right-click: Spear of the Void", NamedTextColor.DARK_RED)
                        .decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text("Hurl a spectral spear that pierces", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text("all enemies in its 30-block path, then", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text("detonates, pulling enemies inward.", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text("+2% damage per enchantment | No enchant limit", NamedTextColor.DARK_RED)
                        .decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text("14s cooldown", NamedTextColor.DARK_GRAY)
                        .decoration(TextDecoration.ITALIC, false));
            }
        }

        // Enchantment glint
        meta.setEnchantmentGlintOverride(true);

        result.setItemMeta(meta);
        return result;
    }

    /**
     * Validates whether a super upgrade is valid for a given spear and target tier.
     */
    public boolean isValidUpgrade(ItemStack spear, int targetSuperTier) {
        if (!isSpear(spear)) return false;
        int current = getSuperTier(spear);
        return targetSuperTier > current && targetSuperTier <= 3;
    }

    public NamespacedKey getSuperTierKey() {
        return superTierKey;
    }
}

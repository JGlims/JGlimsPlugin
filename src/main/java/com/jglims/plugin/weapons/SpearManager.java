package com.jglims.plugin.weapons;

import com.jglims.plugin.config.ConfigManager;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

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
        private final double jabDamage;   // Java Edition jab damage (in HP, i.e. half-hearts)
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

        // For tiered spears, enforce material matching for direct upgrades:
        // Iron spear + 8 iron ingots -> Super Iron Iron Spear
        // OR sequential: Super Iron spear + 8 diamonds -> Super Diamond spear
        // We allow sequential upgrades regardless of base material.

        // Clone to preserve enchantments
        ItemStack result = baseSpear.clone();
        ItemMeta meta = result.getItemMeta();
        if (meta == null) return null;

        // Set super tier
        meta.getPersistentDataContainer().set(superTierKey, PersistentDataType.INTEGER, newSuperTier);

        // Calculate damage
        double baseDamage = spearTier.getJabDamage();
        double bonusDamage = switch (newSuperTier) {
            case 1 -> config.getSuperIronBonusDamage();      // +1
            case 2 -> config.getSuperDiamondBonusDamage();    // +2
            case 3 -> config.getSuperNetheriteBonusDamage();  // +2 (over diamond, so +4 total if sequential)
            default -> 0;
        };

        // For sequential upgrades, accumulated bonus is the sum
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

        // Display name
        String displayName = newSuperTier == 3
                ? "§5✦ Definitive " + tierName + " Spear §5✦"
                : "§6⚔ Super " + superName + " " + tierName + " Spear";
        meta.setDisplayName(displayName);

        // Build lore
        List<String> lore = new ArrayList<>();
        lore.add("§7Custom Weapon");
        lore.add("");
        lore.add("§f⚔ Attack Damage: §a" + String.format("%.1f", baseDamage)
                + " §7+ §e" + String.format("%.1f", totalBonus)
                + " §7= §c" + String.format("%.1f", totalDamage));
        lore.add("§f⚡ Attack Speed: §b" + String.format("%.2f", spearTier.getAttackSpeed()));
        lore.add("§f⚔ Attack Range: §92.0 - 4.5 blocks");
        lore.add("");

        // Ability info
        switch (newSuperTier) {
            case 1 -> {
                lore.add("§7✦ Super Iron — No special ability");
                lore.add("§8Upgrade to Diamond for abilities.");
            }
            case 2 -> {
                lore.add("§d✦ Right-click: §fPhantom Lunge");
                lore.add("§7Dash forward 8 blocks, damaging all");
                lore.add("§7enemies in your path. §812s cooldown");
                lore.add("");
                lore.add("§7Diamond Super Weapon");
            }
            case 3 -> {
                lore.add("§5✦ Right-click: §fSpear of the Void");
                lore.add("§7Hurl a spectral spear that §cpierces");
                lore.add("§7all enemies in its 30-block path, then");
                lore.add("§7detonates, pulling enemies inward.");
                lore.add("§5+2% damage per enchantment §7| §5No enchant limit");
                lore.add("§820s cooldown");
                lore.add("");
                lore.add("§5§lDefinitive Super Weapon");
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
        // Must be exactly the next tier up, or a direct jump from 0
        return targetSuperTier > current && targetSuperTier <= 3;
    }

    public NamespacedKey getSuperTierKey() {
        return superTierKey;
    }
}

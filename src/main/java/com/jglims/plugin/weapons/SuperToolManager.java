package com.jglims.plugin.weapons;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.enchantments.CustomEnchantManager;
import com.jglims.plugin.enchantments.EnchantmentType;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class SuperToolManager implements Listener {

    private final JGlimsPlugin plugin;
    private final NamespacedKey superToolKey;       // stores tier: 1=Iron, 2=Diamond, 3=Netherite
    private final NamespacedKey superDamageKey;
    private final NamespacedKey superSpeedKey;
    private final NamespacedKey superElytraDurabilityKey;

    // Tier constants
    public static final int TIER_NONE = 0;
    public static final int TIER_IRON = 1;
    public static final int TIER_DIAMOND = 2;
    public static final int TIER_NETHERITE = 3;

    public SuperToolManager(JGlimsPlugin plugin) {
        this.plugin = plugin;
        this.superToolKey = new NamespacedKey(plugin, "super_tool_tier");
        this.superDamageKey = new NamespacedKey(plugin, "super_damage");
        this.superSpeedKey = new NamespacedKey(plugin, "super_speed");
        this.superElytraDurabilityKey = new NamespacedKey(plugin, "super_elytra_durability");
    }

    public NamespacedKey getSuperToolKey() {
        return superToolKey;
    }

    public boolean isSuperTool(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        return pdc.has(superToolKey, PersistentDataType.INTEGER);
    }

    public int getSuperTier(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return TIER_NONE;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        Integer tier = pdc.get(superToolKey, PersistentDataType.INTEGER);
        return tier != null ? tier : TIER_NONE;
    }

    /**
     * Checks if an item is a (Netherite) Super Netherite tool — the ultimate tier.
     * This means it's NETHERITE base material AND (Netherite) Super tier.
     */
    public boolean isDefinitiveSuperNetherite(ItemStack item) {
        if (item == null) return false;
        return getSuperTier(item) == TIER_NETHERITE && isNetheriteMaterial(item.getType());
    }

    /**
     * Creates a Super Tool at the specified tier from the given base tool.
     *
     * Tier 1 (Iron): +1 damage only, no ability
     * Tier 2 (Diamond): +2 damage (non-netherite) or +1 (netherite base), enchantment-dependent abilities
     * Tier 3 (Netherite): +3 damage (non-netherite) or +2 (netherite base), definitive abilities for netherite base
     *
     * Trident exception: Diamond +3, Netherite +5
     */
    public ItemStack createSuperTool(ItemStack baseTool, int tier) {
        if (baseTool == null || baseTool.getType() == Material.AIR) return null;
        if (tier < TIER_IRON || tier > TIER_NETHERITE) return null;

        Material mat = baseTool.getType();
        ItemStack superTool = baseTool.clone();
        ItemMeta meta = superTool.getItemMeta();
        if (meta == null) return null;

        // Store tier in PDC
        meta.getPersistentDataContainer().set(superToolKey, PersistentDataType.INTEGER, tier);

        boolean isNetherite = isNetheriteMaterial(mat);
        boolean isElytra = mat == Material.ELYTRA;
        boolean isTrident = mat == Material.TRIDENT;
        boolean isShield = mat == Material.SHIELD;
        boolean isBow = mat == Material.BOW;
        boolean isCrossbow = mat == Material.CROSSBOW;

        String tierPrefix = getTierPrefix(tier);
        NamedTextColor tierColor = getTierColor(tier, isNetherite);

        // Set display name
        if (isElytra) {
            meta.displayName(Component.text(tierPrefix + " Elytra", tierColor)
                .decoration(TextDecoration.ITALIC, false));
        } else {
            String baseName = getToolBaseName(mat);
            meta.displayName(Component.text(tierPrefix + " " + baseName, tierColor)
                .decoration(TextDecoration.ITALIC, false));
        }

        // Build lore
        List<Component> existingLore = meta.lore() != null ? new ArrayList<>(meta.lore()) : new ArrayList<>();
        // Remove old super tool lore entries if upgrading
        existingLore.removeIf(c -> {
            String plain = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(c);
            return plain.startsWith("Super Tool") || plain.startsWith("(Iron) Super")
                || plain.startsWith("(Diamond) Super") || plain.startsWith("(Netherite) Super")
                || plain.contains("Bonus Attack Damage") || plain.contains("durability save")
                || plain.contains("No enchantment conflicts") || plain.contains("Definitive ability");
        });

        if (isElytra) {
            double durabilitySave = getElytraDurabilitySave(tier);
            meta.getPersistentDataContainer().set(superElytraDurabilityKey, PersistentDataType.INTEGER, tier);

            existingLore.add(0, Component.text(tierPrefix + " Elytra", tierColor)
                .decoration(TextDecoration.ITALIC, false));
            existingLore.add(1, Component.text((int)(durabilitySave * 100) + "% durability save chance", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
            if (tier == TIER_NETHERITE) {
                existingLore.add(2, Component.text("No enchantment conflicts", NamedTextColor.LIGHT_PURPLE)
                    .decoration(TextDecoration.ITALIC, false));
            }
        } else if (isShield || isBow || isCrossbow) {
            // Non-melee items: no damage modifier, just mark and lore
            existingLore.add(0, Component.text(tierPrefix + " Tool", tierColor)
                .decoration(TextDecoration.ITALIC, false));
            if (tier >= TIER_DIAMOND) {
                existingLore.add(1, Component.text("Special ability unlocked", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            }
            if (tier == TIER_NETHERITE && isNetherite) {
                existingLore.add(Component.text("Definitive ability", NamedTextColor.LIGHT_PURPLE)
                    .decoration(TextDecoration.ITALIC, false));
                existingLore.add(Component.text("No enchantment conflicts", NamedTextColor.LIGHT_PURPLE)
                    .decoration(TextDecoration.ITALIC, false));
            }
        } else {
            double bonusDmg = getBonusDamage(mat, tier, isTrident);
            double vanillaBaseDamage = getVanillaBaseDamage(mat);
            double vanillaAttackSpeed = getVanillaAttackSpeed(mat);

            existingLore.add(0, Component.text(tierPrefix + " Tool", tierColor)
                .decoration(TextDecoration.ITALIC, false));
            existingLore.add(1, Component.text("+" + formatDmg(bonusDmg) + " Bonus Attack Damage", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));

            if (tier >= TIER_DIAMOND) {
                existingLore.add(2, Component.text("Special ability unlocked", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            }

            if (tier == TIER_NETHERITE && isNetherite) {
                existingLore.add(Component.text("Definitive ability", NamedTextColor.LIGHT_PURPLE)
                    .decoration(TextDecoration.ITALIC, false));
                existingLore.add(Component.text("No enchantment conflicts", NamedTextColor.LIGHT_PURPLE)
                    .decoration(TextDecoration.ITALIC, false));
            }

            // Remove old attribute modifiers
            meta.removeAttributeModifier(Attribute.ATTACK_DAMAGE);
            meta.removeAttributeModifier(Attribute.ATTACK_SPEED);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

            if (vanillaBaseDamage > 0) {
                double totalModifier = vanillaBaseDamage + bonusDmg - 1.0;
                meta.addAttributeModifier(Attribute.ATTACK_DAMAGE,
                    new AttributeModifier(superDamageKey, totalModifier,
                        AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND));

                double speedModifier = vanillaAttackSpeed - 4.0;
                meta.addAttributeModifier(Attribute.ATTACK_SPEED,
                    new AttributeModifier(superSpeedKey, speedModifier,
                        AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND));
            }
        }

        meta.lore(existingLore);
        superTool.setItemMeta(meta);
        return superTool;
    }

    /**
     * Upgrades an existing super tool to a higher tier, preserving all enchantments.
     */
    public ItemStack upgradeSuperTool(ItemStack existingSuper, int newTier) {
        if (existingSuper == null) return null;
        int currentTier = getSuperTier(existingSuper);
        if (newTier <= currentTier) return null;

        // Preserve all enchantments (vanilla + custom)
        Map<Enchantment, Integer> vanillaEnchants = existingSuper.getEnchantments();
        CustomEnchantManager enchantManager = plugin.getEnchantManager();
        Map<EnchantmentType, Integer> customEnchants = enchantManager.getAllCustomEnchants(existingSuper);

        // Create new super at higher tier
        // Strip the old super PDC first by creating a base clone
        ItemStack result = createSuperTool(existingSuper, newTier);
        if (result == null) return null;

        // Re-apply all vanilla enchantments
        for (Map.Entry<Enchantment, Integer> entry : vanillaEnchants.entrySet()) {
            result.addUnsafeEnchantment(entry.getKey(), entry.getValue());
        }

        // Re-apply all custom enchantments
        for (Map.Entry<EnchantmentType, Integer> entry : customEnchants.entrySet()) {
            enchantManager.setEnchant(result, entry.getKey(), entry.getValue());
        }

        return result;
    }

    private double getBonusDamage(Material mat, int tier, boolean isTrident) {
        boolean isNetherite = isNetheriteMaterial(mat);
        if (isTrident) {
            return switch (tier) {
                case TIER_IRON -> 1.0;
                case TIER_DIAMOND -> 3.0;
                case TIER_NETHERITE -> 5.0;
                default -> 0;
            };
        }
        if (isNetherite) {
            return switch (tier) {
                case TIER_IRON -> 1.0;
                case TIER_DIAMOND -> 1.0;
                case TIER_NETHERITE -> 2.0;
                default -> 0;
            };
        }
        return switch (tier) {
            case TIER_IRON -> 1.0;
            case TIER_DIAMOND -> 2.0;
            case TIER_NETHERITE -> 3.0;
            default -> 0;
        };
    }

    private double getElytraDurabilitySave(int tier) {
        return switch (tier) {
            case TIER_IRON -> 0.30;
            case TIER_DIAMOND -> 0.50;
            case TIER_NETHERITE -> 0.70;
            default -> 0;
        };
    }

    public int getElytraDurabilityTier(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return 0;
        Integer tier = item.getItemMeta().getPersistentDataContainer()
            .get(superElytraDurabilityKey, PersistentDataType.INTEGER);
        return tier != null ? tier : 0;
    }

    public boolean hasSuperElytraDurability(ItemStack item) {
        return getElytraDurabilityTier(item) > 0;
    }

    private String getTierPrefix(int tier) {
        return switch (tier) {
            case TIER_IRON -> "(Iron) Super";
            case TIER_DIAMOND -> "(Diamond) Super";
            case TIER_NETHERITE -> "(Netherite) Super";
            default -> "Super";
        };
    }

    private NamedTextColor getTierColor(int tier, boolean isNetherite) {
        return switch (tier) {
            case TIER_IRON -> NamedTextColor.WHITE;
            case TIER_DIAMOND -> NamedTextColor.AQUA;
            case TIER_NETHERITE -> isNetherite ? NamedTextColor.DARK_RED : NamedTextColor.DARK_PURPLE;
            default -> NamedTextColor.WHITE;
        };
    }

    public static boolean isNetheriteMaterial(Material mat) {
        return mat.name().startsWith("NETHERITE_");
    }

    /**
     * Checks if a material can become a Super tool.
     * Regular Axes, Bows, and Crossbows CANNOT — must be Battle variants.
     * Hoes CAN (for sickles which are hoe-based).
     */
    public boolean canBeSuperTool(Material mat) {
        String name = mat.name();
        // Regular axes cannot be super
        if (name.endsWith("_AXE")) return false;
        // Regular bows/crossbows cannot be super
        if (mat == Material.BOW || mat == Material.CROSSBOW) return false;

        return name.endsWith("_SWORD") || name.endsWith("_PICKAXE")
            || name.endsWith("_SHOVEL") || name.endsWith("_HOE")
            || mat == Material.ELYTRA || mat == Material.TRIDENT
            || mat == Material.SHIELD;
    }

    /**
     * Returns the ingredient material for each super tier.
     */
    public Material getIronTierIngredient() { return Material.IRON_INGOT; }
    public Material getDiamondTierIngredient() { return Material.DIAMOND; }
    public Material getNetheriteTierIngredient() { return Material.NETHERITE_INGOT; }

    /**
     * Returns the special ingredient for (Iron) Super Battle Bow/Crossbow
     */
    public Material getBattleBowIronIngredient() { return Material.STRING; }

    public double getVanillaBaseDamage(Material mat) {
        return switch (mat) {
            case WOODEN_SWORD -> 4.0;
            case STONE_SWORD -> 5.0;
            case IRON_SWORD -> 6.0;
            case GOLDEN_SWORD -> 4.0;
            case DIAMOND_SWORD -> 7.0;
            case NETHERITE_SWORD -> 8.0;
            case WOODEN_AXE -> 7.0;
            case STONE_AXE -> 9.0;
            case IRON_AXE -> 9.0;
            case GOLDEN_AXE -> 7.0;
            case DIAMOND_AXE -> 9.0;
            case NETHERITE_AXE -> 10.0;
            case WOODEN_PICKAXE -> 2.0;
            case STONE_PICKAXE -> 3.0;
            case IRON_PICKAXE -> 4.0;
            case GOLDEN_PICKAXE -> 2.0;
            case DIAMOND_PICKAXE -> 5.0;
            case NETHERITE_PICKAXE -> 6.0;
            case WOODEN_SHOVEL -> 2.5;
            case STONE_SHOVEL -> 3.5;
            case IRON_SHOVEL -> 4.5;
            case GOLDEN_SHOVEL -> 2.5;
            case DIAMOND_SHOVEL -> 5.5;
            case NETHERITE_SHOVEL -> 6.5;
            case WOODEN_HOE -> 1.0;
            case STONE_HOE -> 1.0;
            case IRON_HOE -> 1.0;
            case GOLDEN_HOE -> 1.0;
            case DIAMOND_HOE -> 1.0;
            case NETHERITE_HOE -> 1.0;
            case TRIDENT -> 9.0;
            default -> 0;
        };
    }

    public double getVanillaAttackSpeed(Material mat) {
        return switch (mat) {
            case WOODEN_SWORD, STONE_SWORD, IRON_SWORD, GOLDEN_SWORD,
                 DIAMOND_SWORD, NETHERITE_SWORD -> 1.6;
            case WOODEN_AXE, STONE_AXE -> 0.8;
            case IRON_AXE -> 0.9;
            case GOLDEN_AXE, DIAMOND_AXE, NETHERITE_AXE -> 1.0;
            case WOODEN_PICKAXE, STONE_PICKAXE, IRON_PICKAXE, GOLDEN_PICKAXE,
                 DIAMOND_PICKAXE, NETHERITE_PICKAXE -> 1.2;
            case WOODEN_SHOVEL, STONE_SHOVEL, IRON_SHOVEL, GOLDEN_SHOVEL,
                 DIAMOND_SHOVEL, NETHERITE_SHOVEL -> 1.0;
            case WOODEN_HOE, GOLDEN_HOE -> 1.0;
            case STONE_HOE -> 2.0;
            case IRON_HOE -> 3.0;
            case DIAMOND_HOE, NETHERITE_HOE -> 4.0;
            case TRIDENT -> 1.1;
            default -> 4.0;
        };
    }

    private String getToolBaseName(Material mat) {
        String name = mat.name().replace('_', ' ');
        StringBuilder sb = new StringBuilder();
        boolean capitalize = true;
        for (char c : name.toCharArray()) {
            if (c == ' ') {
                sb.append(' ');
                capitalize = true;
            } else if (capitalize) {
                sb.append(Character.toUpperCase(c));
                capitalize = false;
            } else {
                sb.append(Character.toLowerCase(c));
            }
        }
        return sb.toString();
    }

    private String formatDmg(double dmg) {
        if (dmg == (int) dmg) return String.valueOf((int) dmg);
        return String.format("%.1f", dmg);
    }
}

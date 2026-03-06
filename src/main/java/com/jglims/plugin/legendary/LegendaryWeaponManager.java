package com.jglims.plugin.legendary;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.persistence.PersistentDataType;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.legendary.LegendaryWeapon.LegendaryTier;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * LegendaryWeaponManager — creates legendary weapon ItemStacks.
 *
 * PDC Tags on every legendary weapon:
 *   is_legendary_weapon  (BYTE, 1)
 *   legendary_id         (STRING, e.g. "oceans_rage")
 *   legendary_tier       (STRING, "legendary" or "uncommon")
 *   legendary_cooldown   (LONG, timestamp of last ability use — set by listener)
 *   legendary_damage     (attribute key)
 *   legendary_speed      (attribute key)
 *
 * All legendary weapons are unbreakable, un-enchantable (blocked by AnvilRecipeListener),
 * have no enchantment glint, and display custom lore.
 *
 * Resource pack model selection uses the 1.21.4+ string-based CustomModelData system.
 * The textureName from LegendaryWeapon is set as strings[0] on the item.
 * The resource pack's assets/minecraft/items/*.json files use a "select" model type
 * with "property": "minecraft:custom_model_data" and string-based "when" cases.
 */
public class LegendaryWeaponManager {

    private final JGlimsPlugin plugin;
    private final NamespacedKey legendaryKey;
    private final NamespacedKey legendaryIdKey;
    private final NamespacedKey legendaryTierKey;
    private final NamespacedKey legendaryCooldownKey;
    private final NamespacedKey legendaryDamageKey;
    private final NamespacedKey legendarySpeedKey;

    public LegendaryWeaponManager(JGlimsPlugin plugin) {
        this.plugin = plugin;
        this.legendaryKey = new NamespacedKey(plugin, "is_legendary_weapon");
        this.legendaryIdKey = new NamespacedKey(plugin, "legendary_id");
        this.legendaryTierKey = new NamespacedKey(plugin, "legendary_tier");
        this.legendaryCooldownKey = new NamespacedKey(plugin, "legendary_cooldown");
        this.legendaryDamageKey = new NamespacedKey(plugin, "legendary_damage");
        this.legendarySpeedKey = new NamespacedKey(plugin, "legendary_speed");
    }

    // ── Accessors ───────────────────────────────────────────────────

    public NamespacedKey getLegendaryKey() { return legendaryKey; }
    public NamespacedKey getLegendaryIdKey() { return legendaryIdKey; }
    public NamespacedKey getLegendaryTierKey() { return legendaryTierKey; }
    public NamespacedKey getLegendaryCooldownKey() { return legendaryCooldownKey; }

    // ── Detection ───────────────────────────────────────────────────

    public boolean isLegendary(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer()
                .has(legendaryKey, PersistentDataType.BYTE);
    }

    public LegendaryWeapon identify(ItemStack item) {
        if (!isLegendary(item)) return null;
        String id = item.getItemMeta().getPersistentDataContainer()
                .get(legendaryIdKey, PersistentDataType.STRING);
        return id != null ? LegendaryWeapon.fromId(id) : null;
    }

    public LegendaryTier getTier(ItemStack item) {
        if (!isLegendary(item)) return null;
        String tier = item.getItemMeta().getPersistentDataContainer()
                .get(legendaryTierKey, PersistentDataType.STRING);
        if ("legendary".equals(tier)) return LegendaryTier.LEGENDARY;
        if ("uncommon".equals(tier)) return LegendaryTier.UNCOMMON;
        return null;
    }

    // ── Item Creation ───────────────────────────────────────────────

    /**
     * Creates a legendary weapon ItemStack from the enum definition.
     */
    public ItemStack createWeapon(LegendaryWeapon weapon) {
        ItemStack item = new ItemStack(weapon.getBaseMaterial(), 1);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;

        // PDC tags
        meta.getPersistentDataContainer().set(legendaryKey, PersistentDataType.BYTE, (byte) 1);
        meta.getPersistentDataContainer().set(legendaryIdKey, PersistentDataType.STRING, weapon.getId());
        meta.getPersistentDataContainer().set(legendaryTierKey, PersistentDataType.STRING,
                weapon.getTier() == LegendaryTier.LEGENDARY ? "legendary" : "uncommon");

        // Unbreakable, no glint
        meta.setUnbreakable(true);
        meta.setEnchantmentGlintOverride(false);

        // ── 1.21.4+ string-based CustomModelData ────────────────────
        // The textureName (e.g. "stormbringer") is set as strings[0].
        // The resource pack items/*.json uses select → custom_model_data
        // with "when": "stormbringer" to pick the correct model.
        CustomModelDataComponent cmd = meta.getCustomModelDataComponent();
        cmd.setStrings(List.of(weapon.getTextureName()));
        meta.setCustomModelDataComponent(cmd);

        // Display name
        NamedTextColor nameColor = weapon.getTier() == LegendaryTier.LEGENDARY
                ? NamedTextColor.DARK_PURPLE : NamedTextColor.GOLD;
        meta.displayName(Component.text(weapon.getDisplayName(), nameColor)
                .decorate(TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false));

        // Lore
        List<Component> lore = buildLore(weapon);
        meta.lore(lore);

        // Hide default attributes
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);

        // Set damage attribute
        double attackSpeed = getAttackSpeed(weapon.getBaseMaterial());
        meta.addAttributeModifier(Attribute.ATTACK_DAMAGE,
                new AttributeModifier(legendaryDamageKey, weapon.getBaseDamage() - 1.0,
                        AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND));
        meta.addAttributeModifier(Attribute.ATTACK_SPEED,
                new AttributeModifier(legendarySpeedKey, attackSpeed - 4.0,
                        AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND));

        item.setItemMeta(meta);
        return item;
    }

    // ── Lore Builder ────────────────────────────────────────────────

    private List<Component> buildLore(LegendaryWeapon weapon) {
        List<Component> lore = new ArrayList<>();
        boolean isElite = weapon.getTier() == LegendaryTier.LEGENDARY;

        // Tier tag
        if (isElite) {
            lore.add(Component.text("\u2726 LEGENDARY \u2726", NamedTextColor.DARK_PURPLE)
                    .decorate(TextDecoration.BOLD)
                    .decoration(TextDecoration.ITALIC, false));
        } else {
            lore.add(Component.text("\u2726 UNCOMMON LEGENDARY \u2726", NamedTextColor.GOLD)
                    .decorate(TextDecoration.BOLD)
                    .decoration(TextDecoration.ITALIC, false));
        }

        // Texture source
        lore.add(Component.text("Texture: " + weapon.getTextureSource() + " \u2014 " + weapon.getTextureName(),
                NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, true));
        lore.add(Component.empty());

        // Stats
        lore.add(Component.text("Attack Damage: ", NamedTextColor.GRAY)
                .append(Component.text(weapon.getBaseDamage(), NamedTextColor.RED)
                        .decorate(TextDecoration.BOLD))
                .decoration(TextDecoration.ITALIC, false));

        double speed = getAttackSpeed(weapon.getBaseMaterial());
        lore.add(Component.text("Attack Speed: ", NamedTextColor.GRAY)
                .append(Component.text(String.format("%.1f", speed), NamedTextColor.WHITE))
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());

        // Abilities
        lore.add(Component.text("\u25B6 Right-Click: ", NamedTextColor.GREEN)
                .append(Component.text(weapon.getRightClickAbilityName(), NamedTextColor.WHITE))
                .append(Component.text(" (" + weapon.getRightClickCooldown() + "s)", NamedTextColor.DARK_GRAY))
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("\u25B6 Hold (2s): ", NamedTextColor.LIGHT_PURPLE)
                .append(Component.text(weapon.getHoldAbilityName(), NamedTextColor.WHITE))
                .append(Component.text(" (" + weapon.getHoldCooldown() + "s)", NamedTextColor.DARK_GRAY))
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());

        // Properties
        lore.add(Component.text("Unbreakable", NamedTextColor.BLUE)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Cannot be enchanted", NamedTextColor.RED)
                .decoration(TextDecoration.ITALIC, false));

        // Base item type hint (for tridents)
        if (weapon.getBaseMaterial() == Material.TRIDENT) {
            lore.add(Component.text("Throwable \u2014 returns automatically", NamedTextColor.AQUA)
                    .decoration(TextDecoration.ITALIC, false));
        }

        return lore;
    }

    // ── Attack Speed by Material ────────────────────────────────────

    private double getAttackSpeed(Material mat) {
        return switch (mat) {
            case DIAMOND_SWORD, NETHERITE_SWORD, IRON_SWORD, GOLDEN_SWORD,
                 STONE_SWORD, WOODEN_SWORD -> 1.6;
            case DIAMOND_AXE, NETHERITE_AXE -> 1.0;
            case TRIDENT -> 1.1;
            case DIAMOND_HOE, NETHERITE_HOE -> 4.0;
            case MACE -> 0.6;
            default -> 1.6;
        };
    }
}
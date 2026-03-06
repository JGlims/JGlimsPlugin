package com.jglims.plugin.legendary;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import com.jglims.plugin.JGlimsPlugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class LegendaryArmorManager {

    private final JGlimsPlugin plugin;
    private final NamespacedKey armorSetKey;
    private final NamespacedKey armorSlotKey;

    public LegendaryArmorManager(JGlimsPlugin plugin) {
        this.plugin = plugin;
        this.armorSetKey = new NamespacedKey(plugin, "legendary_armor_set");
        this.armorSlotKey = new NamespacedKey(plugin, "legendary_armor_slot");
    }

    public ItemStack createArmorPiece(LegendaryArmorSet set, LegendaryArmorSet.ArmorSlot slot) {
        Material mat = set.getMaterialForSlot(slot);
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        TextColor tierColor = set.getTier().getColor();
        String slotName = slot.name().charAt(0) + slot.name().substring(1).toLowerCase();

        meta.displayName(Component.text(set.getDisplayName() + " " + slotName, tierColor)
                .decorate(TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("[" + set.getTier().getId() + " Armor]", tierColor)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("Passive: ", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
                .append(Component.text(set.getPassiveForSlot(slot), NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false)));
        lore.add(Component.empty());
        lore.add(Component.text("Set Bonus (4/4): ", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false)
                .append(Component.text(set.getSetBonusDescription(), NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false)));
        lore.add(Component.empty());
        lore.add(Component.text("Total Defense: " + set.getTotalDefense(), NamedTextColor.BLUE).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Set: " + set.getDisplayName(), tierColor).decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);

        meta.setUnbreakable(true);
        meta.addEnchant(Enchantment.UNBREAKING, 10, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
        meta.setCustomModelData(set.getCmdForSlot(slot));

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(armorSetKey, PersistentDataType.STRING, set.getId());
        pdc.set(armorSlotKey, PersistentDataType.STRING, slot.name());

        EquipmentSlotGroup eslot = switch (slot) {
            case HELMET -> EquipmentSlotGroup.HEAD;
            case CHESTPLATE -> EquipmentSlotGroup.CHEST;
            case LEGGINGS -> EquipmentSlotGroup.LEGS;
            case BOOTS -> EquipmentSlotGroup.FEET;
        };

        double quarter = set.getTotalDefense() / 4.0;
        double actualDef = switch (slot) {
            case HELMET -> Math.floor(quarter);
            case CHESTPLATE -> Math.ceil(quarter * 1.4);
            case LEGGINGS -> Math.ceil(quarter * 1.2);
            case BOOTS -> Math.floor(quarter * 0.8);
        };

        meta.addAttributeModifier(Attribute.ARMOR,
                new AttributeModifier(new NamespacedKey(plugin, "la_def_" + set.getId() + "_" + slot.name().toLowerCase()),
                        actualDef, AttributeModifier.Operation.ADD_NUMBER, eslot));

        if (set.getTier().isAtLeast(LegendaryTier.MYTHIC)) {
            meta.addAttributeModifier(Attribute.ARMOR_TOUGHNESS,
                    new AttributeModifier(new NamespacedKey(plugin, "la_tough_" + set.getId() + "_" + slot.name().toLowerCase()),
                            4.0, AttributeModifier.Operation.ADD_NUMBER, eslot));
        } else if (set.getTier().isAtLeast(LegendaryTier.EPIC)) {
            meta.addAttributeModifier(Attribute.ARMOR_TOUGHNESS,
                    new AttributeModifier(new NamespacedKey(plugin, "la_tough_" + set.getId() + "_" + slot.name().toLowerCase()),
                            2.0, AttributeModifier.Operation.ADD_NUMBER, eslot));
        }

        // Dragon Knight chestplate: +4 max HP
        if (set == LegendaryArmorSet.DRAGON_KNIGHT && slot == LegendaryArmorSet.ArmorSlot.CHESTPLATE) {
            meta.addAttributeModifier(Attribute.MAX_HEALTH,
                    new AttributeModifier(new NamespacedKey(plugin, "la_hp_dragon_knight"),
                            4.0, AttributeModifier.Operation.ADD_NUMBER, eslot));
        }

        // Dragon Knight leggings: 50% knockback resistance
        if (set == LegendaryArmorSet.DRAGON_KNIGHT && slot == LegendaryArmorSet.ArmorSlot.LEGGINGS) {
            meta.addAttributeModifier(Attribute.KNOCKBACK_RESISTANCE,
                    new AttributeModifier(new NamespacedKey(plugin, "la_kb_dragon_knight"),
                            0.5, AttributeModifier.Operation.ADD_NUMBER, eslot));
        }

        item.setItemMeta(meta);
        return item;
    }

    public LegendaryArmorSet identifySet(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        String setId = pdc.get(armorSetKey, PersistentDataType.STRING);
        return LegendaryArmorSet.fromId(setId);
    }

    public LegendaryArmorSet.ArmorSlot identifySlot(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        String slotName = pdc.get(armorSlotKey, PersistentDataType.STRING);
        if (slotName == null) return null;
        try { return LegendaryArmorSet.ArmorSlot.valueOf(slotName); } catch (Exception e) { return null; }
    }

    public boolean hasFullSet(Player player, LegendaryArmorSet set) {
        return identifySet(player.getInventory().getHelmet()) == set
                && identifySet(player.getInventory().getChestplate()) == set
                && identifySet(player.getInventory().getLeggings()) == set
                && identifySet(player.getInventory().getBoots()) == set;
    }

    public int countSetPieces(Player player, LegendaryArmorSet set) {
        int count = 0;
        if (identifySet(player.getInventory().getHelmet()) == set) count++;
        if (identifySet(player.getInventory().getChestplate()) == set) count++;
        if (identifySet(player.getInventory().getLeggings()) == set) count++;
        if (identifySet(player.getInventory().getBoots()) == set) count++;
        return count;
    }

    public boolean isWearingPiece(Player player, LegendaryArmorSet set, LegendaryArmorSet.ArmorSlot slot) {
        ItemStack piece = switch (slot) {
            case HELMET -> player.getInventory().getHelmet();
            case CHESTPLATE -> player.getInventory().getChestplate();
            case LEGGINGS -> player.getInventory().getLeggings();
            case BOOTS -> player.getInventory().getBoots();
        };
        return identifySet(piece) == set;
    }

    public LegendaryArmorSet getActiveFullSet(Player player) {
        LegendaryArmorSet helmetSet = identifySet(player.getInventory().getHelmet());
        if (helmetSet == null) return null;
        if (hasFullSet(player, helmetSet)) return helmetSet;
        return null;
    }

    public NamespacedKey getArmorSetKey() { return armorSetKey; }
    public NamespacedKey getArmorSlotKey() { return armorSlotKey; }
}
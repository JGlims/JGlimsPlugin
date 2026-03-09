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
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import java.util.List;
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
        String tierTag = set.isCraftable() ? "Craftable" : set.getTier().getId();
        lore.add(Component.text("[" + tierTag + " Armor]", tierColor)
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

        // Craftable sets are breakable; legendary sets are unbreakable
        if (!set.isCraftable()) {
            meta.setUnbreakable(true);
            meta.addEnchant(Enchantment.UNBREAKING, 10, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
        } else {
            meta.addEnchant(Enchantment.UNBREAKING, 3, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
        }

        // String-based CMD for 1.21.4+ item model definitions
        CustomModelDataComponent cmdComp = meta.getCustomModelDataComponent();
        String slotSuffix = switch (slot) {
            case HELMET -> "_helmet";
            case CHESTPLATE -> "_chestplate";
            case LEGGINGS -> "_leggings";
            case BOOTS -> "_boots";
        };
        cmdComp.setStrings(List.of(set.getId().replace(" ", "_").toLowerCase() + slotSuffix));
        meta.setCustomModelDataComponent(cmdComp);

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(armorSetKey, PersistentDataType.STRING, set.getId());
        pdc.set(armorSlotKey, PersistentDataType.STRING, slot.name());

        EquipmentSlotGroup eslot = switch (slot) {
            case HELMET -> EquipmentSlotGroup.HEAD;
            case CHESTPLATE -> EquipmentSlotGroup.CHEST;
            case LEGGINGS -> EquipmentSlotGroup.LEGS;
            case BOOTS -> EquipmentSlotGroup.FEET;
        };

        // === Defense distribution ===
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

        // === Armor toughness by tier ===
        if (set.getTier().isAtLeast(LegendaryTier.MYTHIC)) {
            meta.addAttributeModifier(Attribute.ARMOR_TOUGHNESS,
                    new AttributeModifier(new NamespacedKey(plugin, "la_tough_" + set.getId() + "_" + slot.name().toLowerCase()),
                            4.0, AttributeModifier.Operation.ADD_NUMBER, eslot));
        } else if (set.getTier().isAtLeast(LegendaryTier.EPIC)) {
            meta.addAttributeModifier(Attribute.ARMOR_TOUGHNESS,
                    new AttributeModifier(new NamespacedKey(plugin, "la_tough_" + set.getId() + "_" + slot.name().toLowerCase()),
                            2.0, AttributeModifier.Operation.ADD_NUMBER, eslot));
        } else if (set.getTier().isAtLeast(LegendaryTier.RARE)) {
            meta.addAttributeModifier(Attribute.ARMOR_TOUGHNESS,
                    new AttributeModifier(new NamespacedKey(plugin, "la_tough_" + set.getId() + "_" + slot.name().toLowerCase()),
                            1.0, AttributeModifier.Operation.ADD_NUMBER, eslot));
        }

        // === Per-set attribute bonuses ===
        applySetAttributes(meta, set, slot, eslot);

        item.setItemMeta(meta);
        return item;
    }

    private void applySetAttributes(ItemMeta meta, LegendaryArmorSet set, LegendaryArmorSet.ArmorSlot slot, EquipmentSlotGroup eslot) {
        switch (set) {
            // â”€â”€ CRAFTABLE SETS â”€â”€

            case REINFORCED_LEATHER -> {
                // Chestplate: +2 max HP
                if (slot == LegendaryArmorSet.ArmorSlot.CHESTPLATE) {
                    addHP(meta, set, 2.0, eslot);
                }
                // Leggings: +5% movement speed
                if (slot == LegendaryArmorSet.ArmorSlot.LEGGINGS) {
                    addSpeed(meta, set, 0.05, eslot);
                }
            }
            case COPPER_ARMOR -> {
                // Leggings: +10% mining speed via attack speed (proxy)
                if (slot == LegendaryArmorSet.ArmorSlot.LEGGINGS) {
                    addSpeed(meta, set, 0.03, eslot);
                }
            }
            case CHAINMAIL_REINFORCED -> {
                // Full set concept: +15% KB resist spread across pieces
                if (slot == LegendaryArmorSet.ArmorSlot.CHESTPLATE) {
                    addKBResist(meta, set, 0.08, eslot);
                }
                if (slot == LegendaryArmorSet.ArmorSlot.LEGGINGS) {
                    addKBResist(meta, set, 0.07, eslot);
                }
            }
            case AMETHYST_ARMOR -> {
                // Chestplate: +2 HP (harmonic core)
                if (slot == LegendaryArmorSet.ArmorSlot.CHESTPLATE) {
                    addHP(meta, set, 2.0, eslot);
                }
            }
            case BONE_ARMOR -> {
                // Chestplate: +2 HP
                if (slot == LegendaryArmorSet.ArmorSlot.CHESTPLATE) {
                    addHP(meta, set, 2.0, eslot);
                }
            }
            case SCULK_ARMOR -> {
                // Leggings: +5% speed
                if (slot == LegendaryArmorSet.ArmorSlot.LEGGINGS) {
                    addSpeed(meta, set, 0.05, eslot);
                }
            }

            // â”€â”€ LEGENDARY SETS â”€â”€

            case SHADOW_STALKER -> {
                // Leggings: +8% speed
                if (slot == LegendaryArmorSet.ArmorSlot.LEGGINGS) {
                    addSpeed(meta, set, 0.08, eslot);
                }
            }
            case BLOOD_MOON -> {
                // Chestplate: +4 HP
                if (slot == LegendaryArmorSet.ArmorSlot.CHESTPLATE) {
                    addHP(meta, set, 4.0, eslot);
                }
                // Leggings: +10% speed
                if (slot == LegendaryArmorSet.ArmorSlot.LEGGINGS) {
                    addSpeed(meta, set, 0.10, eslot);
                }
            }
            case NATURES_EMBRACE -> {
                // Chestplate: +4 HP (Living Bark)
                if (slot == LegendaryArmorSet.ArmorSlot.CHESTPLATE) {
                    addHP(meta, set, 4.0, eslot);
                }
                // Leggings: +20% KB resist (Root Grip)
                if (slot == LegendaryArmorSet.ArmorSlot.LEGGINGS) {
                    addKBResist(meta, set, 0.20, eslot);
                }
            }
            case FROST_WARDEN -> {
                // Chestplate: +4 HP
                if (slot == LegendaryArmorSet.ArmorSlot.CHESTPLATE) {
                    addHP(meta, set, 4.0, eslot);
                }
                // Leggings: +10% speed on ice (base +5%)
                if (slot == LegendaryArmorSet.ArmorSlot.LEGGINGS) {
                    addSpeed(meta, set, 0.05, eslot);
                }
            }
            case VOID_WALKER -> {
                // Chestplate: +4 HP
                if (slot == LegendaryArmorSet.ArmorSlot.CHESTPLATE) {
                    addHP(meta, set, 4.0, eslot);
                }
                // Leggings: +10% speed
                if (slot == LegendaryArmorSet.ArmorSlot.LEGGINGS) {
                    addSpeed(meta, set, 0.10, eslot);
                }
            }
            case DRAGON_KNIGHT -> {
                // Chestplate: +8 max HP (Dragon Heart)
                if (slot == LegendaryArmorSet.ArmorSlot.CHESTPLATE) {
                    addHP(meta, set, 8.0, eslot);
                }
                // Leggings: 60% knockback resistance (Dragon Scales)
                if (slot == LegendaryArmorSet.ArmorSlot.LEGGINGS) {
                    addKBResist(meta, set, 0.60, eslot);
                }
                // Boots: +10% speed
                if (slot == LegendaryArmorSet.ArmorSlot.BOOTS) {
                    addSpeed(meta, set, 0.10, eslot);
                }
            }
            case ABYSSAL_PLATE -> {
                // Chestplate: +10 max HP (Soul Furnace)
                if (slot == LegendaryArmorSet.ArmorSlot.CHESTPLATE) {
                    addHP(meta, set, 10.0, eslot);
                }
                // Leggings: 80% knockback resistance (Abyssal Stride)
                if (slot == LegendaryArmorSet.ArmorSlot.LEGGINGS) {
                    addKBResist(meta, set, 0.80, eslot);
                }
                // Boots: +15% speed
                if (slot == LegendaryArmorSet.ArmorSlot.BOOTS) {
                    addSpeed(meta, set, 0.15, eslot);
                }
            }
            default -> {}
        }
    }

    // === Attribute helper methods ===

    private void addHP(ItemMeta meta, LegendaryArmorSet set, double amount, EquipmentSlotGroup eslot) {
        meta.addAttributeModifier(Attribute.MAX_HEALTH,
                new AttributeModifier(new NamespacedKey(plugin, "la_hp_" + set.getId()),
                        amount, AttributeModifier.Operation.ADD_NUMBER, eslot));
    }

    private void addKBResist(ItemMeta meta, LegendaryArmorSet set, double amount, EquipmentSlotGroup eslot) {
        meta.addAttributeModifier(Attribute.KNOCKBACK_RESISTANCE,
                new AttributeModifier(new NamespacedKey(plugin, "la_kb_" + set.getId()),
                        amount, AttributeModifier.Operation.ADD_NUMBER, eslot));
    }

    private void addSpeed(ItemMeta meta, LegendaryArmorSet set, double amount, EquipmentSlotGroup eslot) {
        meta.addAttributeModifier(Attribute.MOVEMENT_SPEED,
                new AttributeModifier(new NamespacedKey(plugin, "la_spd_" + set.getId()),
                        amount, AttributeModifier.Operation.MULTIPLY_SCALAR_1, eslot));
    }

    // === Identification methods ===

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

package com.jglims.plugin.crafting;

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

import java.util.ArrayList;
import java.util.List;

/**
 * Central factory for crafted items with functional abilities. Every item
 * returned has display name, lore, CustomModelData, PDC tag, and (where
 * relevant) attribute modifiers. The functional effects are applied in
 * {@link CraftedItemListener}.
 */
public class CraftedItemManager {

    // CMD values — must match _pack_setup.py CRAFTED_TABLE.
    public static final int CMD_BLOOD_CHALICE = 42001;
    public static final int CMD_FANG_DAGGER = 42002;
    public static final int CMD_TROLL_HELMET = 42003;
    public static final int CMD_TROLL_CHESTPLATE = 42004;
    public static final int CMD_TROLL_LEGGINGS = 42005;
    public static final int CMD_TROLL_BOOTS = 42006;
    public static final int CMD_LUNAR_BLADE = 42007;
    public static final int CMD_RAPTOR_GAUNTLET = 42008;
    public static final int CMD_SHARK_TOOTH_NECKLACE = 42009;
    public static final int CMD_TREMOR_SHIELD = 42010;
    public static final int CMD_VOID_SCEPTER = 42011;
    public static final int CMD_SOUL_LANTERN_ITEM = 42012;
    public static final int CMD_DINOSAUR_BONE_BOW = 42013;
    public static final int CMD_CRIMSON_ELIXIR = 42014;
    public static final int CMD_NIGHTWALKER_CLOAK = 42015;

    private final JGlimsPlugin plugin;
    /** Identifies an ItemStack as a CraftedItemManager product; value = id string. */
    public final NamespacedKey keyCraftedId;
    /** Blood Chalice charge counter (int, 0..5). */
    public final NamespacedKey keyChaliceCharges;
    /** Crimson Elixir usage counter (int, 0..3). */
    public final NamespacedKey keyElixirUses;

    public CraftedItemManager(JGlimsPlugin plugin) {
        this.plugin = plugin;
        this.keyCraftedId = new NamespacedKey(plugin, "crafted_item_id");
        this.keyChaliceCharges = new NamespacedKey(plugin, "chalice_charges");
        this.keyElixirUses = new NamespacedKey(plugin, "elixir_uses");
    }

    public String getCraftedId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer()
                .get(keyCraftedId, PersistentDataType.STRING);
    }

    // ── Shared builder ─────────────────────────────────────────────────

    private ItemStack build(Material mat, int cmd, String id, String name, TextColor color,
                            List<Component> lore, boolean unbreakable) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name, color).decorate(TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);
        meta.setCustomModelData(cmd);
        if (unbreakable) meta.setUnbreakable(true);
        meta.getPersistentDataContainer().set(keyCraftedId, PersistentDataType.STRING, id);
        item.setItemMeta(meta);
        return item;
    }

    private List<Component> lore(String... lines) {
        List<Component> list = new ArrayList<>();
        for (String l : lines) {
            list.add(Component.text(l, NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
        }
        return list;
    }

    // ── Items ──────────────────────────────────────────────────────────

    /** Stores 5 Vampire Blood charges, consumed via right-click. */
    public ItemStack createBloodChalice() {
        ItemStack item = build(Material.BOWL, CMD_BLOOD_CHALICE, "blood_chalice",
                "Blood Chalice", TextColor.color(160, 0, 0),
                lore(
                        "Holds up to 5 Vampire Blood.",
                        "Right-click to drain one charge.",
                        "Charges: 5 / 5"
                ), false);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(keyChaliceCharges, PersistentDataType.INTEGER, 5);
        item.setItemMeta(meta);
        return item;
    }

    public int getChaliceCharges(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return 0;
        Integer v = item.getItemMeta().getPersistentDataContainer()
                .get(keyChaliceCharges, PersistentDataType.INTEGER);
        return v == null ? 0 : v;
    }

    public void setChaliceCharges(ItemStack item, int charges) {
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(keyChaliceCharges, PersistentDataType.INTEGER, charges);
        // Refresh lore to show current charges
        meta.lore(lore(
                "Holds up to 5 Vampire Blood.",
                "Right-click to drain one charge.",
                "Charges: " + charges + " / 5"
        ));
        item.setItemMeta(meta);
    }

    public ItemStack createFangDagger() {
        return build(Material.IRON_SWORD, CMD_FANG_DAGGER, "fang_dagger",
                "Fang Dagger", TextColor.color(100, 180, 60),
                lore(
                        "A venomous blade forged from a basilisk fang.",
                        "+12 Attack Damage",
                        "Applies Poison II for 3s on hit",
                        "Unbreakable"
                ), true);
    }

    public ItemStack createTrollHelmet() {
        return build(Material.LEATHER_HELMET, CMD_TROLL_HELMET, "troll_helmet",
                "Troll Helmet", TextColor.color(60, 120, 200),
                lore("Part of the Troll Leather set.", "+4 Toughness", "Thorns I"), false);
    }

    public ItemStack createTrollChestplate() {
        return build(Material.LEATHER_CHESTPLATE, CMD_TROLL_CHESTPLATE, "troll_chestplate",
                "Troll Chestplate", TextColor.color(60, 120, 200),
                lore("Part of the Troll Leather set.", "+4 Toughness", "Thorns I"), false);
    }

    public ItemStack createTrollLeggings() {
        return build(Material.LEATHER_LEGGINGS, CMD_TROLL_LEGGINGS, "troll_leggings",
                "Troll Leggings", TextColor.color(60, 120, 200),
                lore("Part of the Troll Leather set.", "+4 Toughness", "Thorns I"), false);
    }

    public ItemStack createTrollBoots() {
        return build(Material.LEATHER_BOOTS, CMD_TROLL_BOOTS, "troll_boots",
                "Troll Boots", TextColor.color(60, 120, 200),
                lore("Part of the Troll Leather set.", "+4 Toughness", "Thorns I"), false);
    }

    public ItemStack createLunarBlade() {
        return build(Material.DIAMOND_SWORD, CMD_LUNAR_BLADE, "lunar_blade",
                "Lunar Blade", TextColor.color(200, 200, 230),
                lore(
                        "A blade forged from lunar alloy.",
                        "+14 Attack Damage",
                        "Smite V",
                        "Glows at night"
                ), true);
    }

    public ItemStack createRaptorGauntlet() {
        return build(Material.IRON_SWORD, CMD_RAPTOR_GAUNTLET, "raptor_gauntlet",
                "Raptor Gauntlet", TextColor.color(180, 100, 40),
                lore(
                        "Claws of a velociraptor bound to your hand.",
                        "+10 Attack Damage",
                        "+50% Attack Speed",
                        "Right-click: pounce forward"
                ), true);
    }

    public ItemStack createSharkToothNecklace() {
        return build(Material.GOLD_NUGGET, CMD_SHARK_TOOTH_NECKLACE, "shark_tooth_necklace",
                "Shark Tooth Necklace", TextColor.color(220, 220, 210),
                lore(
                        "Hold in your offhand.",
                        "Water Breathing",
                        "Dolphin's Grace"
                ), true);
    }

    public ItemStack createTremorShield() {
        return build(Material.SHIELD, CMD_TREMOR_SHIELD, "tremor_shield",
                "Tremor Shield", TextColor.color(120, 100, 80),
                lore(
                        "Forged from hardened tremorsaurus scales.",
                        "50% projectile reflect",
                        "Shockwave when blocking a melee hit"
                ), true);
    }

    public ItemStack createVoidScepter() {
        return build(Material.BLAZE_ROD, CMD_VOID_SCEPTER, "void_scepter",
                "Void Scepter", TextColor.color(80, 0, 150),
                lore(
                        "Right-click to phase through reality.",
                        "Teleport up to 15 blocks (through walls)",
                        "Cooldown: 8 seconds"
                ), true);
    }

    public ItemStack createSoulLanternItem() {
        return build(Material.LANTERN, CMD_SOUL_LANTERN_ITEM, "soul_lantern_item",
                "Soul Lantern", TextColor.color(70, 150, 220),
                lore(
                        "Hold in offhand.",
                        "Nearby hostiles take Slowness II",
                        "and Weakness I (8-block radius)"
                ), true);
    }

    public ItemStack createDinosaurBoneBow() {
        return build(Material.BOW, CMD_DINOSAUR_BONE_BOW, "dinosaur_bone_bow",
                "Dinosaur Bone Bow", TextColor.color(200, 180, 140),
                lore(
                        "A bow carved from ancient bones.",
                        "+8 Arrow Damage",
                        "Arrows ignore gravity"
                ), true);
    }

    public ItemStack createCrimsonElixir() {
        ItemStack item = build(Material.POTION, CMD_CRIMSON_ELIXIR, "crimson_elixir",
                "Crimson Elixir", TextColor.color(160, 0, 0),
                lore(
                        "Vampire-only elixir.",
                        "Permanently adds +2 claw damage.",
                        "Usable 3 times maximum."
                ), false);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(keyElixirUses, PersistentDataType.INTEGER, 0);
        item.setItemMeta(meta);
        return item;
    }

    public int getElixirUses(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return 0;
        Integer v = item.getItemMeta().getPersistentDataContainer()
                .get(keyElixirUses, PersistentDataType.INTEGER);
        return v == null ? 0 : v;
    }

    public void setElixirUses(ItemStack item, int uses) {
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(keyElixirUses, PersistentDataType.INTEGER, uses);
        item.setItemMeta(meta);
    }

    public ItemStack createNightwalkerCloak() {
        return build(Material.LEATHER_CHESTPLATE, CMD_NIGHTWALKER_CLOAK, "nightwalker_cloak",
                "Nightwalker Cloak", TextColor.color(50, 0, 80),
                lore(
                        "Only vampires can wear this.",
                        "At night: Invisibility + Speed II",
                        "The chestplate restriction does not apply."
                ), true);
    }
}

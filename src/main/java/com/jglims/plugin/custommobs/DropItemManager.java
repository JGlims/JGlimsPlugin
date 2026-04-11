package com.jglims.plugin.custommobs;

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

import java.util.List;

/**
 * Central factory for custom mob drop items. Every method returns a
 * fully-configured ItemStack with display name, lore, CustomModelData,
 * and a PDC tag marking it as a custom drop item.
 * <p>
 * Each drop texture is registered in the resource pack under
 * assets/minecraft/textures/item/ with a matching model JSON and
 * a CMD entry in the base material's item JSON.
 */
public class DropItemManager {

    // CMD values — must match _pack_setup.py and the master CMD allocation table.
    public static final int CMD_TROLL_HIDE = 41001;
    public static final int CMD_LUNAR_FRAGMENT = 41002;
    public static final int CMD_LUNAR_BEAST_HIDE = 41003;
    public static final int CMD_SHARK_TOOTH = 41004;
    public static final int CMD_BEAR_CLAW = 41005;
    public static final int CMD_RAPTOR_CLAW = 41006;
    public static final int CMD_BASILISK_FANG = 41007;
    public static final int CMD_DARK_ESSENCE = 41008;
    public static final int CMD_VOID_ESSENCE = 41009;
    public static final int CMD_SOUL_FRAGMENT = 41010;
    public static final int CMD_TREMOR_SCALE = 41011;
    public static final int CMD_HORN = 41012;
    public static final int CMD_SPINOSAURUS_SAIL = 41013;

    private final JGlimsPlugin plugin;
    private final NamespacedKey keyDropItem;

    public DropItemManager(JGlimsPlugin plugin) {
        this.plugin = plugin;
        this.keyDropItem = new NamespacedKey(plugin, "drop_item");
    }

    public NamespacedKey getKeyDropItem() { return keyDropItem; }

    /**
     * Returns the drop-item ID of a stack, or null if it isn't a custom drop.
     */
    public String getDropItemId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer()
                .get(keyDropItem, PersistentDataType.STRING);
    }

    // ── Factory helper ─────────────────────────────────────────────────

    private ItemStack build(Material mat, int amount, int cmd, String id,
                            String displayName, TextColor color,
                            List<Component> lore) {
        ItemStack item = new ItemStack(mat, amount);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(displayName, color)
                .decorate(TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);
        meta.setCustomModelData(cmd);
        meta.getPersistentDataContainer().set(keyDropItem, PersistentDataType.STRING, id);
        item.setItemMeta(meta);
        return item;
    }

    // ── Drop creators ──────────────────────────────────────────────────

    public ItemStack createTrollHide(int amount) {
        return build(Material.LEATHER, amount, CMD_TROLL_HIDE, "troll_hide",
                "Troll Hide", TextColor.color(60, 120, 200),
                List.of(
                        Component.text("Thick hide of a slain troll.", NamedTextColor.GRAY)
                                .decoration(TextDecoration.ITALIC, false),
                        Component.text("Crafting: Troll Leather Armor", NamedTextColor.DARK_GRAY)
                                .decoration(TextDecoration.ITALIC, false)
                ));
    }

    public ItemStack createLunarFragment(int amount) {
        return build(Material.IRON_NUGGET, amount, CMD_LUNAR_FRAGMENT, "lunar_fragment",
                "Lunar Fragment", TextColor.color(200, 200, 230),
                List.of(
                        Component.text("A shard of lunar alloy from an Invaderling.", NamedTextColor.GRAY)
                                .decoration(TextDecoration.ITALIC, false),
                        Component.text("Crafting: Lunar Blade", NamedTextColor.DARK_GRAY)
                                .decoration(TextDecoration.ITALIC, false)
                ));
    }

    public ItemStack createLunarBeastHide(int amount) {
        return build(Material.LEATHER, amount, CMD_LUNAR_BEAST_HIDE, "lunar_beast_hide",
                "Lunar Beast Hide", TextColor.color(150, 170, 220),
                List.of(
                        Component.text("Hide of a lunar-mutated beast.", NamedTextColor.GRAY)
                                .decoration(TextDecoration.ITALIC, false),
                        Component.text("Rare crafting reagent.", NamedTextColor.DARK_GRAY)
                                .decoration(TextDecoration.ITALIC, false)
                ));
    }

    public ItemStack createSharkTooth(int amount) {
        return build(Material.FLINT, amount, CMD_SHARK_TOOTH, "shark_tooth",
                "Shark Tooth", TextColor.color(220, 220, 210),
                List.of(
                        Component.text("Razor-sharp tooth pulled from a shark.", NamedTextColor.GRAY)
                                .decoration(TextDecoration.ITALIC, false),
                        Component.text("Crafting: Shark Tooth Necklace", NamedTextColor.DARK_GRAY)
                                .decoration(TextDecoration.ITALIC, false)
                ));
    }

    public ItemStack createBearClaw(int amount) {
        return build(Material.BONE, amount, CMD_BEAR_CLAW, "bear_claw",
                "Bear Claw", TextColor.color(140, 90, 50),
                List.of(
                        Component.text("Massive claw of a grizzly bear.", NamedTextColor.GRAY)
                                .decoration(TextDecoration.ITALIC, false),
                        Component.text("Crafting reagent.", NamedTextColor.DARK_GRAY)
                                .decoration(TextDecoration.ITALIC, false)
                ));
    }

    public ItemStack createRaptorClaw(int amount) {
        return build(Material.FLINT, amount, CMD_RAPTOR_CLAW, "raptor_claw",
                "Raptor Claw", TextColor.color(180, 100, 40),
                List.of(
                        Component.text("Curved killing claw of a velociraptor.", NamedTextColor.GRAY)
                                .decoration(TextDecoration.ITALIC, false),
                        Component.text("Crafting: Raptor Gauntlet", NamedTextColor.DARK_GRAY)
                                .decoration(TextDecoration.ITALIC, false)
                ));
    }

    public ItemStack createBasiliskFang(int amount) {
        return build(Material.BONE, amount, CMD_BASILISK_FANG, "basilisk_fang",
                "Basilisk Fang", TextColor.color(100, 180, 60),
                List.of(
                        Component.text("Venom-etched fang of a basilisk.", NamedTextColor.GRAY)
                                .decoration(TextDecoration.ITALIC, false),
                        Component.text("Crafting: Fang Dagger", NamedTextColor.DARK_GRAY)
                                .decoration(TextDecoration.ITALIC, false)
                ));
    }

    public ItemStack createDarkEssence(int amount) {
        return build(Material.ENDER_PEARL, amount, CMD_DARK_ESSENCE, "dark_essence",
                "Dark Essence", TextColor.color(80, 0, 120),
                List.of(
                        Component.text("Concentrated darkness from a necromancer.", NamedTextColor.GRAY)
                                .decoration(TextDecoration.ITALIC, false)
                ));
    }

    public ItemStack createVoidEssence(int amount) {
        return build(Material.ENDER_PEARL, amount, CMD_VOID_ESSENCE, "void_essence",
                "Void Essence", TextColor.color(50, 0, 100),
                List.of(
                        Component.text("A piece of pure void from The Keeper.", NamedTextColor.GRAY)
                                .decoration(TextDecoration.ITALIC, false),
                        Component.text("Crafting: Void Scepter", NamedTextColor.DARK_GRAY)
                                .decoration(TextDecoration.ITALIC, false)
                ));
    }

    public ItemStack createSoulFragment(int amount) {
        return build(Material.LAPIS_LAZULI, amount, CMD_SOUL_FRAGMENT, "soul_fragment",
                "Soul Fragment", TextColor.color(70, 150, 220),
                List.of(
                        Component.text("A captured soul from the Soul Stealer.", NamedTextColor.GRAY)
                                .decoration(TextDecoration.ITALIC, false),
                        Component.text("Crafting: Soul Lantern (item)", NamedTextColor.DARK_GRAY)
                                .decoration(TextDecoration.ITALIC, false)
                ));
    }

    public ItemStack createTremorScale(int amount) {
        return build(Material.IRON_NUGGET, amount, CMD_TREMOR_SCALE, "tremor_scale",
                "Tremor Scale", TextColor.color(120, 100, 80),
                List.of(
                        Component.text("Rugged scale of a tremorsaurus.", NamedTextColor.GRAY)
                                .decoration(TextDecoration.ITALIC, false),
                        Component.text("Crafting: Tremor Shield", NamedTextColor.DARK_GRAY)
                                .decoration(TextDecoration.ITALIC, false)
                ));
    }

    public ItemStack createHorn(int amount) {
        return build(Material.BONE, amount, CMD_HORN, "horn",
                "Grottoceratops Horn", TextColor.color(200, 180, 140),
                List.of(
                        Component.text("Massive horn torn from a grottoceratops.", NamedTextColor.GRAY)
                                .decoration(TextDecoration.ITALIC, false),
                        Component.text("Crafting: Dinosaur Bone Bow", NamedTextColor.DARK_GRAY)
                                .decoration(TextDecoration.ITALIC, false)
                ));
    }

    public ItemStack createSpinosaurusSail(int amount) {
        return build(Material.LEATHER, amount, CMD_SPINOSAURUS_SAIL, "spinosaurus_sail",
                "Spinosaurus Sail", TextColor.color(140, 80, 40),
                List.of(
                        Component.text("Broad back-sail of a spinosaurus.", NamedTextColor.GRAY)
                                .decoration(TextDecoration.ITALIC, false),
                        Component.text("Crafting: Nightwalker Cloak", NamedTextColor.DARK_GRAY)
                                .decoration(TextDecoration.ITALIC, false)
                ));
    }
}

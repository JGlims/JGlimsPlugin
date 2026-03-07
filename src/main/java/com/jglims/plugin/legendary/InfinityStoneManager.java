package com.jglims.plugin.legendary;

import com.jglims.plugin.JGlimsPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

/**
 * Manages the 6 Infinity Stone fragments and finished Infinity Stones.
 * Fragments drop during Blood Moon (0.1% per mob kill) â€” secret, undocumented.
 * Finished stones are crafted: Fragment + Nether Star in anvil (handled by AnvilRecipeListener).
 * Phase 22.
 */
public class InfinityStoneManager {

    private final JGlimsPlugin plugin;
    private final NamespacedKey KEY_STONE_TYPE;
    private final NamespacedKey KEY_STONE_FINISHED;

    public enum StoneType {
        POWER("Power Stone", "power", TextColor.color(150, 0, 255), Material.PURPLE_DYE, 40010),
        SPACE("Space Stone", "space", TextColor.color(0, 100, 255), Material.BLUE_DYE, 40011),
        REALITY("Reality Stone", "reality", TextColor.color(255, 0, 0), Material.RED_DYE, 40012),
        SOUL("Soul Stone", "soul", TextColor.color(255, 150, 0), Material.ORANGE_DYE, 40013),
        TIME("Time Stone", "time", TextColor.color(0, 200, 50), Material.GREEN_DYE, 40014),
        MIND("Mind Stone", "mind", TextColor.color(255, 255, 0), Material.YELLOW_DYE, 40015);

        private final String displayName;
        private final String id;
        private final TextColor color;
        private final Material material;
        private final int customModelData;

        StoneType(String displayName, String id, TextColor color, Material material, int customModelData) {
            this.displayName = displayName;
            this.id = id;
            this.color = color;
            this.material = material;
            this.customModelData = customModelData;
        }

        public String getDisplayName() { return displayName; }
        public String getId() { return id; }
        public TextColor getColor() { return color; }
        public Material getMaterial() { return material; }
        public int getCustomModelData() { return customModelData; }

        public static StoneType fromId(String id) {
            if (id == null) return null;
            for (StoneType st : values()) {
                if (st.id.equalsIgnoreCase(id)) return st;
            }
            return null;
        }
    }

    public InfinityStoneManager(JGlimsPlugin plugin) {
        this.plugin = plugin;
        KEY_STONE_TYPE = new NamespacedKey(plugin, "infinity_stone_type");
        KEY_STONE_FINISHED = new NamespacedKey(plugin, "infinity_stone_finished");
    }

    /**
     * Create a raw Infinity Stone fragment (dropped during Blood Moon).
     */
    public ItemStack createFragment(StoneType type) {
        ItemStack item = new ItemStack(type.getMaterial());
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(type.getDisplayName() + " Fragment", type.getColor())
                .decorate(TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text(""),
                Component.text("A mysterious glowing fragment...", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                Component.text("It pulses with cosmic energy.", NamedTextColor.DARK_PURPLE).decoration(TextDecoration.ITALIC, false),
                Component.text(""),
                Component.text("Combine with a Nether Star", NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false),
                Component.text("in an Anvil to awaken it.", NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false)
        ));
        meta.getPersistentDataContainer().set(KEY_STONE_TYPE, PersistentDataType.STRING, type.getId());
        meta.getPersistentDataContainer().set(KEY_STONE_FINISHED, PersistentDataType.BYTE, (byte) 0);
        meta.setCustomModelData(type.getCustomModelData());
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Create a finished Infinity Stone (after anvil combine with Nether Star).
     */
    public ItemStack createFinishedStone(StoneType type) {
        ItemStack item = new ItemStack(type.getMaterial());
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(type.getDisplayName(), type.getColor())
                .decorate(TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text(""),
                Component.text("An Infinity Stone of immeasurable power.", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false),
                Component.text(""),
                Component.text("One of six stones needed to forge", NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false),
                Component.text("the Infinity Gauntlet.", NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false)
        ));
        meta.getPersistentDataContainer().set(KEY_STONE_TYPE, PersistentDataType.STRING, type.getId());
        meta.getPersistentDataContainer().set(KEY_STONE_FINISHED, PersistentDataType.BYTE, (byte) 1);
        meta.setCustomModelData(type.getCustomModelData() + 100); // finished = +100 CMD offset
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Check if an item is any infinity stone (fragment or finished).
     */
    public boolean isInfinityStone(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(KEY_STONE_TYPE, PersistentDataType.STRING);
    }

    /**
     * Check if an item is a fragment (not yet finished).
     */
    public boolean isFragment(ItemStack item) {
        if (!isInfinityStone(item)) return false;
        byte finished = item.getItemMeta().getPersistentDataContainer().getOrDefault(KEY_STONE_FINISHED, PersistentDataType.BYTE, (byte) 0);
        return finished == 0;
    }

    /**
     * Check if an item is a finished infinity stone.
     */
    public boolean isFinishedStone(ItemStack item) {
        if (!isInfinityStone(item)) return false;
        byte finished = item.getItemMeta().getPersistentDataContainer().getOrDefault(KEY_STONE_FINISHED, PersistentDataType.BYTE, (byte) 0);
        return finished == 1;
    }

    /**
     * Get the stone type of an item (or null if not a stone).
     */
    public StoneType getStoneType(ItemStack item) {
        if (!isInfinityStone(item)) return null;
        String id = item.getItemMeta().getPersistentDataContainer().get(KEY_STONE_TYPE, PersistentDataType.STRING);
        if (id == null) return null;
        for (StoneType st : StoneType.values()) {
            if (st.getId().equals(id)) return st;
        }
        return null;
    }

    /**
     * Get a random stone fragment for Blood Moon drops.
     */
    public ItemStack createRandomFragment() {
        StoneType[] types = StoneType.values();
        StoneType chosen = types[java.util.concurrent.ThreadLocalRandom.current().nextInt(types.length)];
        return createFragment(chosen);
    }

    public NamespacedKey getKeyStoneType() { return KEY_STONE_TYPE; }
    public NamespacedKey getKeyStoneFinished() { return KEY_STONE_FINISHED; }
}
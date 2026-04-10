package com.jglims.plugin.custommobs.mobs;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.custommobs.CustomMobType;
import com.jglims.plugin.custommobs.CustomNpcEntity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Random;

/**
 * Sweetness Copper Golem NPC - Friendly village trader.
 * Sells redstone components, copper items, custom utility items,
 * and occasionally treasure maps to rare structures.
 */
public class SweetnessCopperGolemNpc extends CustomNpcEntity {

    private static final TextColor COPPER_ORANGE = TextColor.color(200, 130, 70);
    private static final TextColor WARM_YELLOW = TextColor.color(255, 220, 120);
    private final Random random = new Random();

    public SweetnessCopperGolemNpc(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.SWEETNESS_COPPER_GOLEM);
        this.tradeRefreshDays = 3;

        dialogueLines.add(Component.text("*click* *whirr* Greetings, friend!", COPPER_ORANGE));
        dialogueLines.add(Component.text("I am Sweetness, a copper golem who chose to stay awake!", NamedTextColor.WHITE));
        dialogueLines.add(Component.text("Most of my kind oxidize and freeze... but not me!", COPPER_ORANGE));
        dialogueLines.add(Component.text("I collect redstone gadgets and maps to hidden places.", NamedTextColor.WHITE));
        dialogueLines.add(Component.text("Want to see what I've found? *happy clicking*", WARM_YELLOW)
                .decorate(TextDecoration.ITALIC));
    }

    @Override
    protected void initializeTrades() {
        // ── Redstone Components ──
        addTrade(new ItemStack(Material.EMERALD, 2), new ItemStack(Material.REDSTONE, 32), 10);
        addTrade(new ItemStack(Material.EMERALD, 4), new ItemStack(Material.REPEATER, 8), 8);
        addTrade(new ItemStack(Material.EMERALD, 4), new ItemStack(Material.COMPARATOR, 8), 8);
        addTrade(new ItemStack(Material.EMERALD, 3), new ItemStack(Material.PISTON, 4), 8);
        addTrade(new ItemStack(Material.EMERALD, 4), new ItemStack(Material.STICKY_PISTON, 4), 6);
        addTrade(new ItemStack(Material.EMERALD, 2), new ItemStack(Material.REDSTONE_TORCH, 16), 10);
        addTrade(new ItemStack(Material.EMERALD, 3), new ItemStack(Material.OBSERVER, 4), 8);
        addTrade(new ItemStack(Material.EMERALD, 4), new ItemStack(Material.HOPPER, 4), 6);
        addTrade(new ItemStack(Material.EMERALD, 3), new ItemStack(Material.DROPPER, 4), 8);
        addTrade(new ItemStack(Material.EMERALD, 3), new ItemStack(Material.DISPENSER, 4), 8);

        // ── Copper Items ──
        addTrade(new ItemStack(Material.EMERALD, 2), new ItemStack(Material.COPPER_BLOCK, 8), 10);
        addTrade(new ItemStack(Material.EMERALD, 4), new ItemStack(Material.LIGHTNING_ROD, 4), 6);
        addTrade(new ItemStack(Material.EMERALD, 3), new ItemStack(Material.SPYGLASS, 1), 3);
        addTrade(new ItemStack(Material.EMERALD, 6), new ItemStack(Material.COPPER_BULB, 8), 5);

        // ── Custom Utility Items ──
        // Copper Compass (tracks nearest structure)
        ItemStack copperCompass = new ItemStack(Material.COMPASS);
        ItemMeta compassMeta = copperCompass.getItemMeta();
        compassMeta.displayName(Component.text("Copper Compass", COPPER_ORANGE)
                .decorate(TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
        compassMeta.lore(List.of(
                Component.text(""),
                Component.text("Points toward the nearest custom structure.", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("Crafted by Sweetness himself!", NamedTextColor.DARK_GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        compassMeta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "custom_item"), PersistentDataType.STRING, "copper_compass");
        copperCompass.setItemMeta(compassMeta);
        addTrade(new ItemStack(Material.EMERALD, 12), copperCompass, 2);

        // Redstone Lantern (glowstone renamed)
        ItemStack redstoneLantern = new ItemStack(Material.REDSTONE_LAMP, 4);
        ItemMeta lanternMeta = redstoneLantern.getItemMeta();
        lanternMeta.displayName(Component.text("Redstone Lantern", TextColor.color(255, 80, 80))
                .decoration(TextDecoration.ITALIC, false));
        redstoneLantern.setItemMeta(lanternMeta);
        addTrade(new ItemStack(Material.EMERALD, 6), redstoneLantern, 5);

        // ── Treasure Maps to Rare Structures ──
        // Structure Treasure Map
        ItemStack treasureMap = new ItemStack(Material.MAP);
        ItemMeta mapMeta = treasureMap.getItemMeta();
        mapMeta.displayName(Component.text("Treasure Map: Hidden Structure", NamedTextColor.GOLD)
                .decorate(TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
        mapMeta.lore(List.of(
                Component.text(""),
                Component.text("Reveals the location of a nearby", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("custom structure when right-clicked.", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("Single use.", NamedTextColor.DARK_GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        mapMeta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "custom_item"), PersistentDataType.STRING, "structure_treasure_map");
        treasureMap.setItemMeta(mapMeta);
        addTrade(new ItemStack(Material.EMERALD, 24), new ItemStack(Material.DIAMOND, 4), treasureMap, 1);

        // ── Buy Trades ──
        addTrade(new ItemStack(Material.COPPER_INGOT, 32), new ItemStack(Material.EMERALD, 4), 10);
        addTrade(new ItemStack(Material.REDSTONE_BLOCK, 16), new ItemStack(Material.EMERALD, 6), 8);
    }
}

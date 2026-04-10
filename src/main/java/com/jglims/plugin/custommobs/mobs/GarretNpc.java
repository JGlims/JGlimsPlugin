package com.jglims.plugin.custommobs.mobs;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.custommobs.CustomMobType;
import com.jglims.plugin.custommobs.CustomNpcEntity;
import com.jglims.plugin.enchantments.CustomEnchantManager;
import com.jglims.plugin.enchantments.EnchantmentType;
import com.jglims.plugin.powerups.PowerUpManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

/**
 * Garret NPC - General trader found in villages.
 * Sells custom crafting materials, enchantment books, power-up items,
 * arrows, potions, and building materials.
 */
public class GarretNpc extends CustomNpcEntity {

    private static final TextColor MERCHANT_BROWN = TextColor.color(180, 130, 70);

    public GarretNpc(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.GARRET_NPC);
        this.tradeRefreshDays = 3;

        dialogueLines.add(Component.text("Welcome, welcome! Garret's the name, goods are the game!", MERCHANT_BROWN));
        dialogueLines.add(Component.text("I travel between villages selling the finest wares.", NamedTextColor.WHITE));
        dialogueLines.add(Component.text("Enchantments, power-ups, potions — you name it!", MERCHANT_BROWN));
        dialogueLines.add(Component.text("Fair prices, quality goods. Take a look!", NamedTextColor.GOLD)
                .decorate(TextDecoration.BOLD));
    }

    @Override
    protected void initializeTrades() {
        PowerUpManager pum = JGlimsPlugin.getInstance().getPowerUpManager();
        CustomEnchantManager em = JGlimsPlugin.getInstance().getEnchantManager();

        // ── Power-Up Items (16 diamonds each) ──
        addTrade(new ItemStack(Material.DIAMOND, 16), pum.createHeartCrystal(), 3);
        addTrade(new ItemStack(Material.DIAMOND, 16), pum.createSoulFragment(), 3);
        addTrade(new ItemStack(Material.DIAMOND, 16), pum.createTitanResolve(), 2);
        addTrade(new ItemStack(Material.DIAMOND, 16), pum.createVitalityShard(), 2);
        addTrade(new ItemStack(Material.DIAMOND, 16), pum.createBerserkerMark(), 2);

        // ── Enchantment Books ──
        // Veinminer III
        ItemStack veinBook = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta veinMeta = veinBook.getItemMeta();
        veinMeta.displayName(Component.text("Enchantment: Veinminer III", NamedTextColor.GREEN)
                .decoration(TextDecoration.ITALIC, false));
        veinMeta.lore(List.of(
                Component.text(""),
                Component.text("Apply to a pickaxe on an anvil.", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        veinMeta.getPersistentDataContainer().set(
                em.getKey(EnchantmentType.VEINMINER), PersistentDataType.INTEGER, 3);
        veinBook.setItemMeta(veinMeta);
        addTrade(new ItemStack(Material.DIAMOND, 8), veinBook, 2);

        // Swiftness III
        ItemStack swiftBook = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta swiftMeta = swiftBook.getItemMeta();
        swiftMeta.displayName(Component.text("Enchantment: Swiftness III", NamedTextColor.AQUA)
                .decoration(TextDecoration.ITALIC, false));
        swiftMeta.lore(List.of(
                Component.text(""),
                Component.text("Apply to armor on an anvil.", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        swiftMeta.getPersistentDataContainer().set(
                em.getKey(EnchantmentType.SWIFTNESS), PersistentDataType.INTEGER, 3);
        swiftBook.setItemMeta(swiftMeta);
        addTrade(new ItemStack(Material.DIAMOND, 8), swiftBook, 2);

        // Soulbound I
        ItemStack soulBook = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta soulMeta = soulBook.getItemMeta();
        soulMeta.displayName(Component.text("Enchantment: Soulbound I", NamedTextColor.DARK_PURPLE)
                .decoration(TextDecoration.ITALIC, false));
        soulMeta.lore(List.of(
                Component.text(""),
                Component.text("Item is kept on death. Apply on an anvil.", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        soulMeta.getPersistentDataContainer().set(
                em.getKey(EnchantmentType.SOULBOUND), PersistentDataType.INTEGER, 1);
        soulBook.setItemMeta(soulMeta);
        addTrade(new ItemStack(Material.DIAMOND, 12), soulBook, 1);

        // Fortification III
        ItemStack fortBook = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta fortMeta = fortBook.getItemMeta();
        fortMeta.displayName(Component.text("Enchantment: Fortification III", NamedTextColor.BLUE)
                .decoration(TextDecoration.ITALIC, false));
        fortMeta.lore(List.of(
                Component.text(""),
                Component.text("Apply to armor on an anvil.", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        fortMeta.getPersistentDataContainer().set(
                em.getKey(EnchantmentType.FORTIFICATION), PersistentDataType.INTEGER, 3);
        fortBook.setItemMeta(fortMeta);
        addTrade(new ItemStack(Material.DIAMOND, 10), fortBook, 2);

        // ── Arrows ──
        addTrade(new ItemStack(Material.EMERALD, 4), new ItemStack(Material.ARROW, 64), 10);
        addTrade(new ItemStack(Material.EMERALD, 8), new ItemStack(Material.SPECTRAL_ARROW, 32), 5);
        addTrade(new ItemStack(Material.EMERALD, 12), new ItemStack(Material.TIPPED_ARROW, 16), 3);

        // ── Potions ──
        ItemStack strengthPot = new ItemStack(Material.POTION);
        PotionMeta strMeta = (PotionMeta) strengthPot.getItemMeta();
        strMeta.addCustomEffect(new PotionEffect(PotionEffectType.STRENGTH, 6000, 1), true);
        strMeta.displayName(Component.text("Potion of Strength II", NamedTextColor.RED)
                .decoration(TextDecoration.ITALIC, false));
        strengthPot.setItemMeta(strMeta);
        addTrade(new ItemStack(Material.EMERALD, 8), strengthPot, 3);

        ItemStack regenPot = new ItemStack(Material.POTION);
        PotionMeta regenMeta = (PotionMeta) regenPot.getItemMeta();
        regenMeta.addCustomEffect(new PotionEffect(PotionEffectType.REGENERATION, 1800, 1), true);
        regenMeta.displayName(Component.text("Potion of Regeneration II", NamedTextColor.LIGHT_PURPLE)
                .decoration(TextDecoration.ITALIC, false));
        regenPot.setItemMeta(regenMeta);
        addTrade(new ItemStack(Material.EMERALD, 10), regenPot, 3);

        // ── Building Materials ──
        addTrade(new ItemStack(Material.EMERALD, 2), new ItemStack(Material.DARK_OAK_LOG, 32), 10);
        addTrade(new ItemStack(Material.EMERALD, 4), new ItemStack(Material.STONE_BRICKS, 64), 10);
        addTrade(new ItemStack(Material.EMERALD, 6), new ItemStack(Material.DEEPSLATE_BRICKS, 64), 8);
        addTrade(new ItemStack(Material.EMERALD, 4), new ItemStack(Material.GLASS, 64), 10);
    }
}

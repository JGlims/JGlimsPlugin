package com.jglims.plugin.custommobs.mobs;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.custommobs.CustomMobType;
import com.jglims.plugin.custommobs.CustomNpcEntity;
import com.jglims.plugin.enchantments.CustomEnchantManager;
import com.jglims.plugin.enchantments.EnchantmentType;
import com.jglims.plugin.legendary.LegendaryTier;
import com.jglims.plugin.legendary.LegendaryWeapon;
import com.jglims.plugin.legendary.LegendaryWeaponManager;
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
import java.util.Random;

/**
 * Japanese Dragon NPC - Ancient guardian found in Cherry Blossom temples.
 * Sells rare legendary weapons, cherry blossom cosmetics, and unique enchantment books.
 * Buys cherry blossoms and bamboo from players.
 */
public class JapaneseDragonNpc extends CustomNpcEntity {

    private static final TextColor CHERRY_PINK = TextColor.color(255, 150, 180);
    private final Random random = new Random();

    public JapaneseDragonNpc(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.JAPANESE_DRAGON_NPC);
        this.tradeRefreshDays = 3;

        dialogueLines.add(Component.text("...the petals whisper of your arrival, traveler.", CHERRY_PINK));
        dialogueLines.add(Component.text("I am the guardian of this temple, as old as the first cherry tree.", NamedTextColor.WHITE));
        dialogueLines.add(Component.text("For a thousand years I have watched the blossoms bloom and wilt.", CHERRY_PINK));
        dialogueLines.add(Component.text("If you bring offerings worthy of the sakura, I may share", NamedTextColor.WHITE));
        dialogueLines.add(Component.text("the weapons of those who walked this path before you.", CHERRY_PINK));
        dialogueLines.add(Component.text("Trade wisely. The petals do not fall for the unworthy.", NamedTextColor.GOLD)
                .decorate(TextDecoration.ITALIC));
    }

    @Override
    protected void initializeTrades() {
        LegendaryWeaponManager wm = JGlimsPlugin.getInstance().getLegendaryWeaponManager();
        LegendaryWeapon[] rareWeapons = LegendaryWeapon.byTier(LegendaryTier.RARE);

        // 3 random RARE legendary weapons (32 emeralds + 8 diamonds each)
        if (rareWeapons.length > 0) {
            List<Integer> indices = new java.util.ArrayList<>();
            for (int i = 0; i < rareWeapons.length; i++) indices.add(i);
            java.util.Collections.shuffle(indices, random);
            int count = Math.min(3, rareWeapons.length);
            for (int i = 0; i < count; i++) {
                addTrade(
                        new ItemStack(Material.EMERALD, 32),
                        new ItemStack(Material.DIAMOND, 8),
                        wm.createWeapon(rareWeapons[indices.get(i)]),
                        1
                );
            }
        }

        // Cherry Blossom Talisman (cosmetic)
        ItemStack talisman = new ItemStack(Material.PINK_DYE);
        ItemMeta talismanMeta = talisman.getItemMeta();
        talismanMeta.displayName(Component.text("Cherry Blossom Talisman", CHERRY_PINK)
                .decorate(TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
        talismanMeta.lore(List.of(
                Component.text(""),
                Component.text("A blessed talisman from the temple.", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("Emits cherry petal particles when held.", NamedTextColor.DARK_GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        talismanMeta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "custom_item"), PersistentDataType.STRING, "cherry_talisman");
        talisman.setItemMeta(talismanMeta);
        addTrade(new ItemStack(Material.EMERALD, 16), talisman, 3);

        // Sakura Petals (renamed pink petals)
        ItemStack petals = new ItemStack(Material.PINK_PETALS, 16);
        ItemMeta petalMeta = petals.getItemMeta();
        petalMeta.displayName(Component.text("Sakura Petals", CHERRY_PINK)
                .decoration(TextDecoration.ITALIC, false));
        petals.setItemMeta(petalMeta);
        addTrade(new ItemStack(Material.EMERALD, 4), petals, 5);

        // Cherry Blossom Lantern (end rod renamed)
        ItemStack lantern = new ItemStack(Material.END_ROD, 4);
        ItemMeta lanternMeta = lantern.getItemMeta();
        lanternMeta.displayName(Component.text("Cherry Blossom Lantern", CHERRY_PINK)
                .decoration(TextDecoration.ITALIC, false));
        lantern.setItemMeta(lanternMeta);
        addTrade(new ItemStack(Material.EMERALD, 8), lantern, 5);

        // Enchantment books — Vampirism III and Lifesteal III
        CustomEnchantManager em = JGlimsPlugin.getInstance().getEnchantManager();

        ItemStack vampBook = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta vampMeta = vampBook.getItemMeta();
        vampMeta.displayName(Component.text("Enchantment: Vampirism III", NamedTextColor.LIGHT_PURPLE)
                .decoration(TextDecoration.ITALIC, false));
        vampMeta.lore(List.of(
                Component.text(""),
                Component.text("Apply to a sword on an anvil.", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        vampMeta.getPersistentDataContainer().set(
                em.getKey(EnchantmentType.VAMPIRISM), PersistentDataType.INTEGER, 3);
        vampBook.setItemMeta(vampMeta);
        addTrade(new ItemStack(Material.DIAMOND, 12), vampBook, 2);

        ItemStack frostBook = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta frostMeta = frostBook.getItemMeta();
        frostMeta.displayName(Component.text("Enchantment: Frostbite Blade III", NamedTextColor.AQUA)
                .decoration(TextDecoration.ITALIC, false));
        frostMeta.lore(List.of(
                Component.text(""),
                Component.text("Apply to a sword on an anvil.", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        frostMeta.getPersistentDataContainer().set(
                em.getKey(EnchantmentType.FROSTBITE_BLADE), PersistentDataType.INTEGER, 3);
        frostBook.setItemMeta(frostMeta);
        addTrade(new ItemStack(Material.DIAMOND, 12), frostBook, 2);

        // Buy trades: cherry blossoms and bamboo
        addTrade(new ItemStack(Material.CHERRY_LOG, 32), new ItemStack(Material.EMERALD, 8), 10);
        addTrade(new ItemStack(Material.PINK_PETALS, 64), new ItemStack(Material.EMERALD, 6), 10);
        addTrade(new ItemStack(Material.BAMBOO, 64), new ItemStack(Material.EMERALD, 4), 10);
    }
}

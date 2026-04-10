package com.jglims.plugin.custommobs.mobs;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.custommobs.CustomMobType;
import com.jglims.plugin.custommobs.CustomNpcEntity;
import com.jglims.plugin.enchantments.CustomEnchantManager;
import com.jglims.plugin.enchantments.EnchantmentType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

/**
 * Centaur NPC - Aether village combat trader.
 * Sells weapons, armor, arrows, combat potions, and Aether battle items.
 * Buys troll hides, soul fragments, and keeper void essence.
 */
public class CentaurNpc extends CustomNpcEntity {

    private static final TextColor BRONZE = TextColor.color(180, 140, 80);
    private static final TextColor BATTLE_RED = TextColor.color(200, 60, 60);

    public CentaurNpc(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.CENTAUR_NPC);
        this.tradeRefreshDays = 3;

        dialogueLines.add(Component.text("Halt! I am Centaur, warrior of the Aether guard.", BRONZE));
        dialogueLines.add(Component.text("You smell of the surface world... but you carry yourself well.", NamedTextColor.WHITE));
        dialogueLines.add(Component.text("The Aether is dangerous. Trolls, Keepers, Soul Stealers...", BRONZE));
        dialogueLines.add(Component.text("You'll need proper gear if you want to survive here.", NamedTextColor.WHITE));
        dialogueLines.add(Component.text("I sell the finest Aether battle equipment. Take a look.", BATTLE_RED)
                .decorate(TextDecoration.BOLD));
    }

    @Override
    protected void initializeTrades() {
        CustomEnchantManager em = JGlimsPlugin.getInstance().getEnchantManager();

        // ── Weapons ──
        addTrade(new ItemStack(Material.DIAMOND, 4), new ItemStack(Material.DIAMOND_SWORD, 1), 3);
        addTrade(new ItemStack(Material.DIAMOND, 5), new ItemStack(Material.DIAMOND_AXE, 1), 3);
        addTrade(new ItemStack(Material.DIAMOND, 3), new ItemStack(Material.BOW, 1), 3);
        addTrade(new ItemStack(Material.DIAMOND, 4), new ItemStack(Material.CROSSBOW, 1), 3);
        addTrade(new ItemStack(Material.DIAMOND, 6), new ItemStack(Material.MACE, 1), 2);
        addTrade(new ItemStack(Material.DIAMOND, 5), new ItemStack(Material.TRIDENT, 1), 2);

        // ── Armor ──
        addTrade(new ItemStack(Material.DIAMOND, 8), new ItemStack(Material.DIAMOND_CHESTPLATE, 1), 2);
        addTrade(new ItemStack(Material.DIAMOND, 6), new ItemStack(Material.DIAMOND_LEGGINGS, 1), 2);
        addTrade(new ItemStack(Material.DIAMOND, 5), new ItemStack(Material.DIAMOND_HELMET, 1), 2);
        addTrade(new ItemStack(Material.DIAMOND, 4), new ItemStack(Material.DIAMOND_BOOTS, 1), 2);
        addTrade(new ItemStack(Material.DIAMOND, 3), new ItemStack(Material.SHIELD, 1), 3);

        // ── Arrows ──
        addTrade(new ItemStack(Material.EMERALD, 2), new ItemStack(Material.ARROW, 64), 10);
        addTrade(new ItemStack(Material.EMERALD, 8), new ItemStack(Material.SPECTRAL_ARROW, 32), 5);

        // ── Combat Potions ──
        ItemStack strengthPot = new ItemStack(Material.POTION);
        PotionMeta strMeta = (PotionMeta) strengthPot.getItemMeta();
        strMeta.addCustomEffect(new PotionEffect(PotionEffectType.STRENGTH, 6000, 1), true);
        strMeta.displayName(Component.text("Aether War Brew", BATTLE_RED)
                .decoration(TextDecoration.ITALIC, false));
        strengthPot.setItemMeta(strMeta);
        addTrade(new ItemStack(Material.EMERALD, 8), strengthPot, 3);

        ItemStack speedPot = new ItemStack(Material.POTION);
        PotionMeta spdMeta = (PotionMeta) speedPot.getItemMeta();
        spdMeta.addCustomEffect(new PotionEffect(PotionEffectType.SPEED, 6000, 1), true);
        spdMeta.displayName(Component.text("Centaur's Swiftness", BRONZE)
                .decoration(TextDecoration.ITALIC, false));
        speedPot.setItemMeta(spdMeta);
        addTrade(new ItemStack(Material.EMERALD, 6), speedPot, 3);

        ItemStack resPot = new ItemStack(Material.POTION);
        PotionMeta resMeta = (PotionMeta) resPot.getItemMeta();
        resMeta.addCustomEffect(new PotionEffect(PotionEffectType.RESISTANCE, 3600, 1), true);
        resMeta.displayName(Component.text("Aether Ward Potion", TextColor.color(150, 180, 255))
                .decoration(TextDecoration.ITALIC, false));
        resPot.setItemMeta(resMeta);
        addTrade(new ItemStack(Material.EMERALD, 10), resPot, 3);

        // ── Aether Battle Items ──
        // Aether Lance (renamed diamond sword with custom tag)
        ItemStack aetherLance = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta lanceMeta = aetherLance.getItemMeta();
        lanceMeta.displayName(Component.text("Aether Lance", TextColor.color(180, 220, 255))
                .decorate(TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
        lanceMeta.lore(List.of(
                Component.text(""),
                Component.text("Forged in the Aether clouds.", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("Extra damage to Aether mobs.", NamedTextColor.AQUA)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        lanceMeta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "custom_item"), PersistentDataType.STRING, "aether_lance");
        aetherLance.setItemMeta(lanceMeta);
        addTrade(new ItemStack(Material.DIAMOND, 12), new ItemStack(Material.EMERALD, 8), aetherLance, 1);

        // Berserker enchantment book
        ItemStack berserkBook = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta berserkMeta = berserkBook.getItemMeta();
        berserkMeta.displayName(Component.text("Enchantment: Berserker V", BATTLE_RED)
                .decoration(TextDecoration.ITALIC, false));
        berserkMeta.lore(List.of(
                Component.text(""),
                Component.text("Apply to an axe on an anvil.", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        berserkMeta.getPersistentDataContainer().set(
                em.getKey(EnchantmentType.BERSERKER), PersistentDataType.INTEGER, 5);
        berserkBook.setItemMeta(berserkMeta);
        addTrade(new ItemStack(Material.DIAMOND, 16), berserkBook, 1);

        // ── Buy Trades ──
        // Troll Hide
        ItemStack trollHide = new ItemStack(Material.LEATHER, 8);
        ItemMeta hideMeta = trollHide.getItemMeta();
        hideMeta.displayName(Component.text("Troll Hide", NamedTextColor.BLUE));
        hideMeta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "custom_item"), PersistentDataType.STRING, "troll_hide");
        trollHide.setItemMeta(hideMeta);
        addTrade(trollHide, new ItemStack(Material.EMERALD, 12), 8);

        // Soul Fragments (buy from players)
        addTrade(JGlimsPlugin.getInstance().getPowerUpManager().createSoulFragment(),
                new ItemStack(Material.DIAMOND, 4), 5);

        // Generic valuable drops
        addTrade(new ItemStack(Material.ENDER_PEARL, 16), new ItemStack(Material.EMERALD, 8), 5);
    }
}

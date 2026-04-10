package com.jglims.plugin.custommobs.mobs;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.custommobs.CustomMobType;
import com.jglims.plugin.custommobs.CustomNpcEntity;
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
 * Angel NPC - Aether village trader.
 * Sells Aether-specific items: cloud blocks, golden feathers,
 * sky crystals, and occasionally EPIC legendary weapons.
 */
public class AngelNpc extends CustomNpcEntity {

    private static final TextColor CELESTIAL_WHITE = TextColor.color(230, 240, 255);
    private static final TextColor DIVINE_GOLD = TextColor.color(255, 230, 140);
    private final Random random = new Random();

    public AngelNpc(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.ANGEL_NPC);
        this.tradeRefreshDays = 3;

        dialogueLines.add(Component.text("Be at peace, mortal. You have reached the Aether.", CELESTIAL_WHITE));
        dialogueLines.add(Component.text("I am a guardian of this celestial realm.", NamedTextColor.WHITE));
        dialogueLines.add(Component.text("The skies hold treasures beyond earthly comprehension.", CELESTIAL_WHITE));
        dialogueLines.add(Component.text("Cloud-forged goods, golden feathers of ascension,", NamedTextColor.WHITE));
        dialogueLines.add(Component.text("and crystals that channel the sky itself.", DIVINE_GOLD));
        dialogueLines.add(Component.text("If you prove worthy, I may offer weapons of legend.", DIVINE_GOLD)
                .decorate(TextDecoration.ITALIC));
    }

    @Override
    protected void initializeTrades() {
        // ── Cloud Blocks (white wool renamed) ──
        ItemStack cloudBlock = new ItemStack(Material.WHITE_WOOL, 16);
        ItemMeta cloudMeta = cloudBlock.getItemMeta();
        cloudMeta.displayName(Component.text("Cloud Block", CELESTIAL_WHITE)
                .decoration(TextDecoration.ITALIC, false));
        cloudMeta.lore(List.of(
                Component.text(""),
                Component.text("Solidified Aether cloud. Light as air.", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        cloudMeta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "custom_item"), PersistentDataType.STRING, "cloud_block");
        cloudBlock.setItemMeta(cloudMeta);
        addTrade(new ItemStack(Material.EMERALD, 8), cloudBlock, 10);

        // Dense Cloud Block
        ItemStack denseCloud = new ItemStack(Material.WHITE_CONCRETE, 16);
        ItemMeta denseMeta = denseCloud.getItemMeta();
        denseMeta.displayName(Component.text("Dense Cloud Block", TextColor.color(200, 210, 230))
                .decoration(TextDecoration.ITALIC, false));
        denseMeta.lore(List.of(
                Component.text(""),
                Component.text("A denser cloud. Good for building.", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        denseMeta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "custom_item"), PersistentDataType.STRING, "dense_cloud_block");
        denseCloud.setItemMeta(denseMeta);
        addTrade(new ItemStack(Material.EMERALD, 6), denseCloud, 10);

        // ── Golden Feather (gold nugget renamed) ──
        ItemStack goldenFeather = new ItemStack(Material.GOLD_NUGGET, 4);
        ItemMeta featherMeta = goldenFeather.getItemMeta();
        featherMeta.displayName(Component.text("Golden Feather", DIVINE_GOLD)
                .decorate(TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
        featherMeta.lore(List.of(
                Component.text(""),
                Component.text("A feather from an Aether angel.", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("Grants slow falling when held.", NamedTextColor.DARK_GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        featherMeta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "custom_item"), PersistentDataType.STRING, "golden_feather");
        goldenFeather.setItemMeta(featherMeta);
        addTrade(new ItemStack(Material.EMERALD, 12), goldenFeather, 5);

        // ── Sky Crystal (amethyst shard renamed) ──
        ItemStack skyCrystal = new ItemStack(Material.AMETHYST_SHARD, 4);
        ItemMeta crystalMeta = skyCrystal.getItemMeta();
        crystalMeta.displayName(Component.text("Sky Crystal", TextColor.color(150, 180, 255))
                .decorate(TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
        crystalMeta.lore(List.of(
                Component.text(""),
                Component.text("Crystallized Aether energy.", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("Used in Aether crafting recipes.", NamedTextColor.DARK_GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        crystalMeta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "custom_item"), PersistentDataType.STRING, "sky_crystal");
        skyCrystal.setItemMeta(crystalMeta);
        addTrade(new ItemStack(Material.EMERALD, 10), skyCrystal, 5);

        // ── Aether Glow Dust (glowstone dust renamed) ──
        ItemStack glowDust = new ItemStack(Material.GLOWSTONE_DUST, 8);
        ItemMeta dustMeta = glowDust.getItemMeta();
        dustMeta.displayName(Component.text("Aether Glow Dust", TextColor.color(255, 255, 180))
                .decoration(TextDecoration.ITALIC, false));
        glowDust.setItemMeta(dustMeta);
        addTrade(new ItemStack(Material.EMERALD, 4), glowDust, 8);

        // ── Aether building materials ──
        addTrade(new ItemStack(Material.EMERALD, 4), new ItemStack(Material.QUARTZ_BLOCK, 32), 10);
        addTrade(new ItemStack(Material.EMERALD, 6), new ItemStack(Material.SEA_LANTERN, 8), 5);
        addTrade(new ItemStack(Material.EMERALD, 3), new ItemStack(Material.END_ROD, 8), 8);

        // ── Occasionally EPIC legendary weapon (very high price) ──
        LegendaryWeaponManager wm = JGlimsPlugin.getInstance().getLegendaryWeaponManager();
        LegendaryWeapon[] epicWeapons = LegendaryWeapon.byTier(LegendaryTier.EPIC);
        if (epicWeapons.length > 0) {
            LegendaryWeapon chosen = epicWeapons[random.nextInt(epicWeapons.length)];
            addTrade(
                    new ItemStack(Material.DIAMOND, 48),
                    new ItemStack(Material.NETHER_STAR, 4),
                    wm.createWeapon(chosen),
                    1
            );
        }
    }
}

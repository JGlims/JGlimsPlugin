package com.jglims.plugin.custommobs.mobs;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.custommobs.CustomMobType;
import com.jglims.plugin.custommobs.CustomNpcEntity;
import com.jglims.plugin.enchantments.CustomEnchantManager;
import com.jglims.plugin.enchantments.EnchantmentType;
import com.jglims.plugin.legendary.LegendaryTier;
import com.jglims.plugin.legendary.LegendaryWeapon;
import com.jglims.plugin.legendary.LegendaryWeaponManager;
import com.jglims.plugin.powerups.PowerUpManager;
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
 * The Arquimage NPC - THE master trader. Found ONLY in Mage Towers.
 * The most comprehensive shop in the game.
 *
 * Sells: ALL MYTHIC legendary weapons, Heart Crystals, Phoenix Feathers,
 * KeepInventorer, Blessing Crystals, custom enchantment books,
 * and the Wand of Wands.
 */
public class ArquimageNpc extends CustomNpcEntity {

    private static final TextColor ARCANE_PURPLE = TextColor.color(160, 80, 255);
    private static final TextColor MYTHIC_GOLD = TextColor.color(255, 200, 50);

    public ArquimageNpc(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.ARQUIMAGE_NPC);
        this.tradeRefreshDays = 7;

        dialogueLines.add(Component.text("You stand in the presence of the Arquimage.", ARCANE_PURPLE));
        dialogueLines.add(Component.text("Few find this tower. Fewer still can afford what I sell.", NamedTextColor.WHITE));
        dialogueLines.add(Component.text("I have collected the most powerful artifacts in existence.", ARCANE_PURPLE));
        dialogueLines.add(Component.text("Mythic weapons, forbidden enchantments, the Wand of Wands...", NamedTextColor.WHITE));
        dialogueLines.add(Component.text("Everything has a price. And my prices are... substantial.", MYTHIC_GOLD)
                .decorate(TextDecoration.BOLD));
        dialogueLines.add(Component.text("But for those who can pay, power beyond imagination awaits.", ARCANE_PURPLE)
                .decorate(TextDecoration.ITALIC));
    }

    @Override
    protected void initializeTrades() {
        LegendaryWeaponManager wm = JGlimsPlugin.getInstance().getLegendaryWeaponManager();
        PowerUpManager pum = JGlimsPlugin.getInstance().getPowerUpManager();
        CustomEnchantManager em = JGlimsPlugin.getInstance().getEnchantManager();

        // ═══════════════════════════════════════════════════════════
        // ALL MYTHIC legendary weapons (48 diamonds + 16 nether stars each)
        // ═══════════════════════════════════════════════════════════
        LegendaryWeapon[] mythicWeapons = LegendaryWeapon.byTier(LegendaryTier.MYTHIC);
        for (LegendaryWeapon weapon : mythicWeapons) {
            addTrade(
                    new ItemStack(Material.DIAMOND, 48),
                    new ItemStack(Material.NETHER_STAR, 16),
                    wm.createWeapon(weapon),
                    1
            );
        }

        // ═══════════════════════════════════════════════════════════
        // Power-Up Items
        // ═══════════════════════════════════════════════════════════

        // Heart Crystal (32 diamonds)
        addTrade(new ItemStack(Material.DIAMOND, 32), pum.createHeartCrystal(), 5);

        // Phoenix Feather (48 diamonds + 8 nether stars)
        addTrade(
                new ItemStack(Material.DIAMOND, 48),
                new ItemStack(Material.NETHER_STAR, 8),
                pum.createPhoenixFeather(),
                3
        );

        // KeepInventorer (64 diamonds + 32 emeralds)
        addTrade(
                new ItemStack(Material.DIAMOND, 64),
                new ItemStack(Material.EMERALD, 32),
                pum.createKeepInventorer(),
                1
        );

        // Titan's Resolve (24 diamonds)
        addTrade(new ItemStack(Material.DIAMOND, 24), pum.createTitanResolve(), 3);

        // Vitality Shard (24 diamonds)
        addTrade(new ItemStack(Material.DIAMOND, 24), pum.createVitalityShard(), 3);

        // Berserker's Mark (24 diamonds)
        addTrade(new ItemStack(Material.DIAMOND, 24), pum.createBerserkerMark(), 3);

        // Soul Fragment (16 diamonds)
        addTrade(new ItemStack(Material.DIAMOND, 16), pum.createSoulFragment(), 5);

        // ═══════════════════════════════════════════════════════════
        // Blessing Crystals (16 diamonds each)
        // ═══════════════════════════════════════════════════════════
        ItemStack cBlessCrystal = createBlessingCrystal("C's Blessing Crystal",
                "Grants C's Bless: +1 bonus heart per use.",
                TextColor.color(255, 100, 100), "c_bless");
        addTrade(new ItemStack(Material.DIAMOND, 16), cBlessCrystal, 3);

        ItemStack amiBlessCrystal = createBlessingCrystal("Ami's Blessing Crystal",
                "Grants Ami's Bless: +2% melee damage per use.",
                TextColor.color(255, 180, 50), "ami_bless");
        addTrade(new ItemStack(Material.DIAMOND, 16), amiBlessCrystal, 3);

        ItemStack laBlessCrystal = createBlessingCrystal("La's Blessing Crystal",
                "Grants La's Bless: +2% damage reduction per use.",
                TextColor.color(100, 200, 255), "la_bless");
        addTrade(new ItemStack(Material.DIAMOND, 16), laBlessCrystal, 3);

        // ═══════════════════════════════════════════════════════════
        // Custom Enchantment Books
        // ═══════════════════════════════════════════════════════════

        // Chain Lightning III
        addTrade(new ItemStack(Material.DIAMOND, 20),
                createEnchantBook(em, EnchantmentType.CHAIN_LIGHTNING, 3,
                        "Enchantment: Chain Lightning III", NamedTextColor.AQUA,
                        "Apply to a sword on an anvil."), 2);

        // Explosive Arrow III
        addTrade(new ItemStack(Material.DIAMOND, 20),
                createEnchantBook(em, EnchantmentType.EXPLOSIVE_ARROW, 3,
                        "Enchantment: Explosive Arrow III", NamedTextColor.RED,
                        "Apply to a bow on an anvil."), 2);

        // Thunderlord V
        addTrade(new ItemStack(Material.DIAMOND, 24),
                createEnchantBook(em, EnchantmentType.THUNDERLORD, 5,
                        "Enchantment: Thunderlord V", NamedTextColor.YELLOW,
                        "Apply to a crossbow on an anvil."), 1);

        // Seismic Slam III
        addTrade(new ItemStack(Material.DIAMOND, 20),
                createEnchantBook(em, EnchantmentType.SEISMIC_SLAM, 3,
                        "Enchantment: Seismic Slam III", NamedTextColor.DARK_RED,
                        "Apply to a mace on an anvil."), 2);

        // Dodge III
        addTrade(new ItemStack(Material.DIAMOND, 18),
                createEnchantBook(em, EnchantmentType.DODGE, 3,
                        "Enchantment: Dodge III", NamedTextColor.GREEN,
                        "Apply to armor on an anvil."), 2);

        // Vampirism V
        addTrade(new ItemStack(Material.DIAMOND, 24),
                createEnchantBook(em, EnchantmentType.VAMPIRISM, 5,
                        "Enchantment: Vampirism V", NamedTextColor.DARK_RED,
                        "Apply to a sword on an anvil."), 1);

        // Soulbound I
        addTrade(new ItemStack(Material.DIAMOND, 16),
                createEnchantBook(em, EnchantmentType.SOULBOUND, 1,
                        "Enchantment: Soulbound I", NamedTextColor.DARK_PURPLE,
                        "Item is kept on death. Apply on an anvil."), 3);

        // Boost III (Elytra)
        addTrade(new ItemStack(Material.DIAMOND, 20),
                createEnchantBook(em, EnchantmentType.BOOST, 3,
                        "Enchantment: Boost III", NamedTextColor.WHITE,
                        "Apply to an elytra on an anvil."), 2);

        // ═══════════════════════════════════════════════════════════
        // THE WAND OF WANDS — Ultimate magic item
        // (64 diamonds + 32 nether stars + 1 dragon egg)
        // ═══════════════════════════════════════════════════════════
        ItemStack wandOfWands = JGlimsPlugin.getInstance().getMagicItemManager().createWandOfWands();
        addTrade(
                new ItemStack(Material.DIAMOND, 64),
                createDragonEggCost(),
                wandOfWands,
                1
        );
    }

    // ── Helper Methods ──────────────────────────────────────────────

    /**
     * Creates a blessing crystal item for the trade list.
     */
    private ItemStack createBlessingCrystal(String name, String loreText, TextColor color, String blessType) {
        ItemStack crystal = new ItemStack(Material.AMETHYST_SHARD);
        ItemMeta meta = crystal.getItemMeta();
        meta.displayName(Component.text(name, color)
                .decorate(TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text(""),
                Component.text(loreText, NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                Component.text("Right-click to consume.", NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false)
        ));
        // Tag with the appropriate blessing item key
        switch (blessType) {
            case "c_bless" -> meta.getPersistentDataContainer().set(
                    JGlimsPlugin.getInstance().getBlessingManager().getCBlessItemKey(),
                    PersistentDataType.BYTE, (byte) 1);
            case "ami_bless" -> meta.getPersistentDataContainer().set(
                    JGlimsPlugin.getInstance().getBlessingManager().getAmiBlessItemKey(),
                    PersistentDataType.BYTE, (byte) 1);
            case "la_bless" -> meta.getPersistentDataContainer().set(
                    JGlimsPlugin.getInstance().getBlessingManager().getLaBlessItemKey(),
                    PersistentDataType.BYTE, (byte) 1);
        }
        crystal.setItemMeta(meta);
        return crystal;
    }

    /**
     * Creates a custom enchantment book.
     */
    private ItemStack createEnchantBook(CustomEnchantManager em, EnchantmentType type,
                                        int level, String displayName, TextColor color, String loreText) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = book.getItemMeta();
        meta.displayName(Component.text(displayName, color)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text(""),
                Component.text(loreText, NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
        ));
        meta.getPersistentDataContainer().set(
                em.getKey(type), PersistentDataType.INTEGER, level);
        book.setItemMeta(meta);
        return book;
    }

    /**
     * Creates a dragon egg + nether stars cost item for the Wand of Wands trade.
     * Since trades support at most 2 cost items, we combine nether stars with
     * the dragon egg in the second cost slot.
     */
    private ItemStack createDragonEggCost() {
        // The second cost slot: 32 nether stars bundled visually
        // (the dragon egg is implied in the lore since we can only have 2 cost items)
        ItemStack cost = new ItemStack(Material.NETHER_STAR, 32);
        ItemMeta meta = cost.getItemMeta();
        meta.lore(List.of(
                Component.text(""),
                Component.text("Also requires 1 Dragon Egg", NamedTextColor.LIGHT_PURPLE)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        cost.setItemMeta(meta);
        return cost;
    }
}

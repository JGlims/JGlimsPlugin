package com.jglims.plugin.menu;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.enchantments.CustomEnchantManager;
import com.jglims.plugin.enchantments.EnchantmentType;
import com.jglims.plugin.legendary.*;
import com.jglims.plugin.powerups.PowerUpManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

/**
 * CreativeMenuManager - Paginated GUI with ALL plugin items.
 * /jglims menu - opens the main category selector.
 * Categories: Weapons (by tier), Armor Sets, Power-Ups, Infinity Items,
 *             Battle Tools, Blessings, Abyssal Key, Sickles & Spears,
 *             Super Tools, Enchantment Books, Best Buddies Dog Armor.
 * OP-only for giving items; anyone can browse.
 */
public class CreativeMenuManager implements Listener {

    private final JGlimsPlugin plugin;
    private final NamespacedKey KEY_MENU_TYPE;
    private final NamespacedKey KEY_MENU_PAGE;
    private final NamespacedKey KEY_MENU_CATEGORY;
    private final NamespacedKey KEY_MENU_ACTION;

    private static final int SLOTS = 54;

    public CreativeMenuManager(JGlimsPlugin plugin) {
        this.plugin = plugin;
        KEY_MENU_TYPE = new NamespacedKey(plugin, "menu_type");
        KEY_MENU_PAGE = new NamespacedKey(plugin, "menu_page");
        KEY_MENU_CATEGORY = new NamespacedKey(plugin, "menu_category");
        KEY_MENU_ACTION = new NamespacedKey(plugin, "menu_action");
    }

    // ========== MAIN MENU ==========

    public void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, SLOTS,
                Component.text("JGlims ", NamedTextColor.GOLD).decorate(TextDecoration.BOLD)
                        .append(Component.text("| Menu Principal", NamedTextColor.WHITE)));

        // Row 2: weapon tiers
        inv.setItem(10, buildCategoryIcon(Material.DIAMOND_SWORD, "Armas Comuns",
                "20 armas tier COMMON", "cat_weapons_common", NamedTextColor.WHITE));
        inv.setItem(12, buildCategoryIcon(Material.DIAMOND_SWORD, "Armas Raras",
                "8 armas tier RARE", "cat_weapons_rare", NamedTextColor.GREEN));
        inv.setItem(14, buildCategoryIcon(Material.DIAMOND_SWORD, "Armas Epicas",
                "7 armas tier EPIC", "cat_weapons_epic", NamedTextColor.DARK_PURPLE));
        inv.setItem(16, buildCategoryIcon(Material.DIAMOND_SWORD, "Armas Miticas",
                "24 armas tier MYTHIC", "cat_weapons_mythic", NamedTextColor.GOLD));

        // Row 3
        inv.setItem(19, buildCategoryIcon(Material.DIAMOND_SWORD, "Armas Abissais",
                "4 armas tier ABYSSAL", "cat_weapons_abyssal", NamedTextColor.DARK_RED));
        inv.setItem(21, buildCategoryIcon(Material.NETHERITE_CHESTPLATE, "Armaduras",
                "13 sets de armadura", "cat_armor", NamedTextColor.AQUA));
        inv.setItem(23, buildCategoryIcon(Material.NETHER_STAR, "Power-Ups",
                "7 power-ups permanentes", "cat_powerups", NamedTextColor.LIGHT_PURPLE));
        inv.setItem(25, buildCategoryIcon(Material.AMETHYST_SHARD, "Infinity Items",
                "Manopla + 6 pedras", "cat_infinity", NamedTextColor.DARK_AQUA));

        // Row 4
        inv.setItem(28, buildCategoryIcon(Material.GOLDEN_APPLE, "Blessings",
                "3 blessings permanentes", "cat_blessings", NamedTextColor.GOLD));
        inv.setItem(30, buildCategoryIcon(Material.BOW, "Battle Tools",
                "Todas as armas de batalha", "cat_battletools", NamedTextColor.YELLOW));
        inv.setItem(32, buildCategoryIcon(Material.ECHO_SHARD, "Abyssal Key",
                "Chave para o Abyss", "cat_abyssal_key", NamedTextColor.DARK_RED));
        inv.setItem(34, buildCategoryIcon(Material.DIAMOND_HOE, "Sickles & Spears",
                "Foices e Lancas de batalha", "cat_sickles", NamedTextColor.GREEN));

        // Row 5: NEW categories
        inv.setItem(37, buildCategoryIcon(Material.NETHERITE_PICKAXE, "Super Tools",
                "Ferramentas super diamond/netherite", "cat_supertools", NamedTextColor.DARK_AQUA));
        inv.setItem(39, buildCategoryIcon(Material.ENCHANTED_BOOK, "Enchantment Books",
                "Todos os 64+ encantamentos custom", "cat_enchantbooks", NamedTextColor.YELLOW));
        inv.setItem(41, buildCategoryIcon(Material.WOLF_ARMOR, "Best Buddies",
                "Armadura de lobo com Best Buddies", "cat_bestbuddies", NamedTextColor.LIGHT_PURPLE));
        inv.setItem(43, buildInfoIcon(Material.BOOK, "Guia do Servidor",
                "Use /guia para receber o livro-guia completo!"));

        // Fill borders
        ItemStack border = buildBorder();
        for (int i = 0; i < 9; i++) { if (inv.getItem(i) == null) inv.setItem(i, border); }
        for (int i = 45; i < 54; i++) { if (inv.getItem(i) == null) inv.setItem(i, border); }
        for (int i : new int[]{9, 18, 27, 36, 17, 26, 35, 44}) {
            if (inv.getItem(i) == null) inv.setItem(i, border);
        }

        player.openInventory(inv);
    }

    // ========== WEAPON PAGES ==========

    public void openWeaponPage(Player player, LegendaryTier tier, int page) {
        LegendaryWeapon[] weapons = LegendaryWeapon.byTier(tier);
        int totalPages = Math.max(1, (int) Math.ceil(weapons.length / 45.0));
        int safePage = Math.max(0, Math.min(page, totalPages - 1));

        Inventory inv = Bukkit.createInventory(null, SLOTS,
                Component.text("JGlims ", NamedTextColor.GOLD).decorate(TextDecoration.BOLD)
                        .append(Component.text("| " + tier.getId() + " (" + (safePage + 1) + "/" + totalPages + ")", tier.getColor())));

        int start = safePage * 45;
        int end = Math.min(start + 45, weapons.length);

        for (int i = start; i < end; i++) {
            LegendaryWeapon w = weapons[i];
            ItemStack item = plugin.getLegendaryWeaponManager().createWeapon(w);
            if (item != null) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    List<Component> lore = meta.lore() != null ? new ArrayList<>(meta.lore()) : new ArrayList<>();
                    lore.add(Component.empty());
                    lore.add(Component.text("Clique para receber!", NamedTextColor.YELLOW).decorate(TextDecoration.ITALIC));
                    PersistentDataContainer pdc = meta.getPersistentDataContainer();
                    pdc.set(KEY_MENU_ACTION, PersistentDataType.STRING, "give_weapon");
                    pdc.set(KEY_MENU_CATEGORY, PersistentDataType.STRING, w.getId());
                    meta.lore(lore);
                    item.setItemMeta(meta);
                }
                inv.setItem(i - start, item);
            }
        }

        inv.setItem(45, buildNavIcon(Material.ARROW, "Voltar ao Menu", "nav_back", NamedTextColor.RED));
        if (safePage > 0)
            inv.setItem(48, buildNavIcon(Material.ARROW, "Pagina Anterior", "nav_prev_" + tier.getId() + "_" + (safePage - 1), NamedTextColor.YELLOW));
        if (safePage < totalPages - 1)
            inv.setItem(50, buildNavIcon(Material.ARROW, "Proxima Pagina", "nav_next_" + tier.getId() + "_" + (safePage + 1), NamedTextColor.GREEN));
        inv.setItem(53, buildInfoIcon(Material.PAPER, "Pagina " + (safePage + 1) + "/" + totalPages, weapons.length + " armas " + tier.getId()));

        player.openInventory(inv);
    }

    // ========== ARMOR PAGE ==========

    public void openArmorPage(Player player) {
        Inventory inv = Bukkit.createInventory(null, SLOTS,
                Component.text("JGlims ", NamedTextColor.GOLD).decorate(TextDecoration.BOLD)
                        .append(Component.text("| Armaduras", NamedTextColor.AQUA)));

        int slot = 0;
        for (LegendaryArmorSet set : LegendaryArmorSet.values()) {
            if (slot >= 45) break;
            ItemStack icon = new ItemStack(set.getHelmetMat());
            ItemMeta meta = icon.getItemMeta();
            if (meta != null) {
                meta.displayName(Component.text(set.getDisplayName(), set.getTier().getColor()).decorate(TextDecoration.BOLD));
                List<Component> lore = new ArrayList<>();
                lore.add(Component.text("Tier: " + set.getTier().getId(), set.getTier().getColor()));
                lore.add(Component.text("Defesa Total: " + set.getTotalDefense(), NamedTextColor.GRAY));
                lore.add(Component.text("Set Bonus: " + truncate(set.getSetBonusDescription(), 40), NamedTextColor.DARK_PURPLE));
                lore.add(Component.empty());
                lore.add(Component.text("Clique para receber o set completo!", NamedTextColor.YELLOW).decorate(TextDecoration.ITALIC));
                meta.lore(lore);
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
                PersistentDataContainer pdc = meta.getPersistentDataContainer();
                pdc.set(KEY_MENU_ACTION, PersistentDataType.STRING, "give_armor");
                pdc.set(KEY_MENU_CATEGORY, PersistentDataType.STRING, set.getId());
                icon.setItemMeta(meta);
            }
            inv.setItem(slot++, icon);
        }

        inv.setItem(45, buildNavIcon(Material.ARROW, "Voltar ao Menu", "nav_back", NamedTextColor.RED));
        player.openInventory(inv);
    }

    // ========== POWERUPS PAGE ==========

    public void openPowerUpsPage(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27,
                Component.text("JGlims ", NamedTextColor.GOLD).decorate(TextDecoration.BOLD)
                        .append(Component.text("| Power-Ups", NamedTextColor.LIGHT_PURPLE)));

        PowerUpManager pm = plugin.getPowerUpManager();
        ItemStack[] items = {
                tagForMenu(pm.createHeartCrystal(), "give_powerup", "heart"),
                tagForMenu(pm.createSoulFragment(), "give_powerup", "soul"),
                tagForMenu(pm.createTitanResolve(), "give_powerup", "titan"),
                tagForMenu(pm.createPhoenixFeather(), "give_powerup", "phoenix"),
                tagForMenu(pm.createKeepInventorer(), "give_powerup", "keep"),
                tagForMenu(pm.createVitalityShard(), "give_powerup", "vitality"),
                tagForMenu(pm.createBerserkerMark(), "give_powerup", "berserker")
        };
        for (int i = 0; i < items.length && i < 9; i++) {
            inv.setItem(10 + i, items[i]);
        }

        inv.setItem(18, buildNavIcon(Material.ARROW, "Voltar ao Menu", "nav_back", NamedTextColor.RED));
        player.openInventory(inv);
    }

    // ========== INFINITY PAGE ==========

    public void openInfinityPage(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27,
                Component.text("JGlims ", NamedTextColor.GOLD).decorate(TextDecoration.BOLD)
                        .append(Component.text("| Infinity Items", NamedTextColor.DARK_AQUA)));

        InfinityStoneManager ism = plugin.getInfinityStoneManager();
        InfinityGauntletManager igm = plugin.getInfinityGauntletManager();

        int slot = 1;
        for (InfinityStoneManager.StoneType st : InfinityStoneManager.StoneType.values()) {
            inv.setItem(slot++, tagForMenu(ism.createFinishedStone(st), "give_infinity", "stone_" + st.getId()));
        }

        inv.setItem(11, tagForMenu(igm.createThanosGlove(), "give_infinity", "glove"));
        inv.setItem(15, tagForMenu(igm.createInfinityGauntlet(), "give_infinity", "gauntlet"));

        inv.setItem(18, buildNavIcon(Material.ARROW, "Voltar ao Menu", "nav_back", NamedTextColor.RED));
        player.openInventory(inv);
    }

    // ========== BLESSINGS PAGE ==========

    public void openBlessingsPage(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27,
                Component.text("JGlims ", NamedTextColor.GOLD).decorate(TextDecoration.BOLD)
                        .append(Component.text("| Blessings", NamedTextColor.GOLD)));

        ItemStack cBless = new ItemStack(Material.GLISTERING_MELON_SLICE);
        ItemMeta cm = cBless.getItemMeta();
        cm.displayName(Component.text("C's Bless", NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
        cm.lore(List.of(Component.text("+1 coracao por uso (max 10)", NamedTextColor.GRAY)));
        cm.getPersistentDataContainer().set(new NamespacedKey(plugin, "c_bless_item"), PersistentDataType.BYTE, (byte) 1);
        cBless.setItemMeta(cm);

        ItemStack amiBless = new ItemStack(Material.GOLDEN_CARROT);
        ItemMeta am = amiBless.getItemMeta();
        am.displayName(Component.text("Ami's Bless", NamedTextColor.RED).decorate(TextDecoration.BOLD));
        am.lore(List.of(Component.text("+2% dano corpo a corpo por uso (max 10)", NamedTextColor.GRAY)));
        am.getPersistentDataContainer().set(new NamespacedKey(plugin, "ami_bless_item"), PersistentDataType.BYTE, (byte) 1);
        amiBless.setItemMeta(am);

        ItemStack laBless = new ItemStack(Material.GOLDEN_APPLE);
        ItemMeta lm = laBless.getItemMeta();
        lm.displayName(Component.text("La's Bless", NamedTextColor.BLUE).decorate(TextDecoration.BOLD));
        lm.lore(List.of(Component.text("+2 pontos de armadura por uso (max 10)", NamedTextColor.GRAY)));
        lm.getPersistentDataContainer().set(new NamespacedKey(plugin, "la_bless_item"), PersistentDataType.BYTE, (byte) 1);
        laBless.setItemMeta(lm);

        inv.setItem(11, tagForMenu(cBless, "give_blessing", "c_bless"));
        inv.setItem(13, tagForMenu(amiBless, "give_blessing", "ami_bless"));
        inv.setItem(15, tagForMenu(laBless, "give_blessing", "la_bless"));

        inv.setItem(18, buildNavIcon(Material.ARROW, "Voltar ao Menu", "nav_back", NamedTextColor.RED));
        player.openInventory(inv);
    }

    // ========== BATTLE TOOLS PAGE (COMPLETE) ==========

    public void openBattleToolsPage(Player player) {
        Inventory inv = Bukkit.createInventory(null, SLOTS,
                Component.text("JGlims ", NamedTextColor.GOLD).decorate(TextDecoration.BOLD)
                        .append(Component.text("| Battle Tools", NamedTextColor.YELLOW)));

        inv.setItem(10, tagForMenu(plugin.getBattleSwordManager().createBattleSword(), "give_battletool", "battle_sword"));
        inv.setItem(11, tagForMenu(plugin.getBattleAxeManager().createBattleAxe(), "give_battletool", "battle_axe"));
        inv.setItem(12, tagForMenu(plugin.getBattleBowManager().createBattleBow(), "give_battletool", "battle_bow"));
        inv.setItem(13, tagForMenu(plugin.getBattleBowManager().createBattleCrossbow(), "give_battletool", "battle_crossbow"));
        inv.setItem(14, tagForMenu(plugin.getBattleMaceManager().createBattleMace(), "give_battletool", "battle_mace"));
        inv.setItem(15, tagForMenu(plugin.getBattleTridentManager().createBattleTrident(), "give_battletool", "battle_trident"));
        inv.setItem(16, tagForMenu(plugin.getBattlePickaxeManager().createBattlePickaxe(), "give_battletool", "battle_pickaxe"));

        inv.setItem(19, tagForMenu(plugin.getBattleShovelManager().createBattleShovel(), "give_battletool", "battle_shovel"));
        inv.setItem(20, tagForMenu(plugin.getBattleSpearManager().createBattleSpear(), "give_battletool", "battle_spear"));

        inv.setItem(45, buildNavIcon(Material.ARROW, "Voltar ao Menu", "nav_back", NamedTextColor.RED));
        player.openInventory(inv);
    }

    // ========== SICKLES & SPEARS PAGE (COMPLETE) ==========

    public void openSicklesPage(Player player) {
        Inventory inv = Bukkit.createInventory(null, SLOTS,
                Component.text("JGlims ", NamedTextColor.GOLD).decorate(TextDecoration.BOLD)
                        .append(Component.text("| Sickles & Spears", NamedTextColor.GREEN)));

        // Sickles from SickleManager
        inv.setItem(10, tagForMenu(plugin.getSickleManager().createWoodSickle(), "give_sickle", "wood_sickle"));
        inv.setItem(11, tagForMenu(plugin.getSickleManager().createStoneSickle(), "give_sickle", "stone_sickle"));
        inv.setItem(12, tagForMenu(plugin.getSickleManager().createIronSickle(), "give_sickle", "iron_sickle"));
        inv.setItem(13, tagForMenu(plugin.getSickleManager().createDiamondSickle(), "give_sickle", "diamond_sickle"));
        inv.setItem(14, tagForMenu(plugin.getSickleManager().createNetheriteSickle(), "give_sickle", "netherite_sickle"));

        // Spears from SpearManager
        inv.setItem(19, tagForMenu(plugin.getSpearManager().createWoodSpear(), "give_spear", "wood_spear"));
        inv.setItem(20, tagForMenu(plugin.getSpearManager().createStoneSpear(), "give_spear", "stone_spear"));
        inv.setItem(21, tagForMenu(plugin.getSpearManager().createIronSpear(), "give_spear", "iron_spear"));
        inv.setItem(22, tagForMenu(plugin.getSpearManager().createDiamondSpear(), "give_spear", "diamond_spear"));
        inv.setItem(23, tagForMenu(plugin.getSpearManager().createNetheriteSpear(), "give_spear", "netherite_spear"));

        inv.setItem(45, buildNavIcon(Material.ARROW, "Voltar ao Menu", "nav_back", NamedTextColor.RED));
        player.openInventory(inv);
    }

    // ========== SUPER TOOLS PAGE ==========

    public void openSuperToolsPage(Player player) {
        Inventory inv = Bukkit.createInventory(null, SLOTS,
                Component.text("JGlims ", NamedTextColor.GOLD).decorate(TextDecoration.BOLD)
                        .append(Component.text("| Super Tools", NamedTextColor.DARK_AQUA)));

        // Diamond super tools
        inv.setItem(10, tagForMenu(plugin.getSuperToolManager().createSuperTool(Material.DIAMOND_PICKAXE), "give_supertool", "diamond_pickaxe"));
        inv.setItem(11, tagForMenu(plugin.getSuperToolManager().createSuperTool(Material.DIAMOND_AXE), "give_supertool", "diamond_axe"));
        inv.setItem(12, tagForMenu(plugin.getSuperToolManager().createSuperTool(Material.DIAMOND_SHOVEL), "give_supertool", "diamond_shovel"));
        inv.setItem(13, tagForMenu(plugin.getSuperToolManager().createSuperTool(Material.DIAMOND_HOE), "give_supertool", "diamond_hoe"));
        inv.setItem(14, tagForMenu(plugin.getSuperToolManager().createSuperTool(Material.DIAMOND_SWORD), "give_supertool", "diamond_sword"));

        // Netherite super tools
        inv.setItem(19, tagForMenu(plugin.getSuperToolManager().createSuperTool(Material.NETHERITE_PICKAXE), "give_supertool", "netherite_pickaxe"));
        inv.setItem(20, tagForMenu(plugin.getSuperToolManager().createSuperTool(Material.NETHERITE_AXE), "give_supertool", "netherite_axe"));
        inv.setItem(21, tagForMenu(plugin.getSuperToolManager().createSuperTool(Material.NETHERITE_SHOVEL), "give_supertool", "netherite_shovel"));
        inv.setItem(22, tagForMenu(plugin.getSuperToolManager().createSuperTool(Material.NETHERITE_HOE), "give_supertool", "netherite_hoe"));
        inv.setItem(23, tagForMenu(plugin.getSuperToolManager().createSuperTool(Material.NETHERITE_SWORD), "give_supertool", "netherite_sword"));

        inv.setItem(45, buildNavIcon(Material.ARROW, "Voltar ao Menu", "nav_back", NamedTextColor.RED));
        player.openInventory(inv);
    }

    // ========== ENCHANTMENT BOOKS PAGE (PAGINATED) ==========

    public void openEnchantBooksPage(Player player, int page) {
        EnchantmentType[] allEnchants = EnchantmentType.values();
        int perPage = 45;
        int totalPages = Math.max(1, (int) Math.ceil(allEnchants.length / (double) perPage));
        int safePage = Math.max(0, Math.min(page, totalPages - 1));

        Inventory inv = Bukkit.createInventory(null, SLOTS,
                Component.text("JGlims ", NamedTextColor.GOLD).decorate(TextDecoration.BOLD)
                        .append(Component.text("| Enchantments (" + (safePage + 1) + "/" + totalPages + ")", NamedTextColor.YELLOW)));

        int start = safePage * perPage;
        int end = Math.min(start + perPage, allEnchants.length);

        for (int i = start; i < end; i++) {
            EnchantmentType type = allEnchants[i];
            ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
            ItemMeta meta = book.getItemMeta();
            if (meta != null) {
                String displayName = formatEnchantName(type.name());
                meta.displayName(Component.text(displayName, NamedTextColor.AQUA).decorate(TextDecoration.BOLD));
                List<Component> lore = new ArrayList<>();
                lore.add(Component.text("Max Level: " + type.getMaxLevel(), NamedTextColor.GRAY));
                lore.add(Component.text("Category: " + getEnchantCategory(type), NamedTextColor.DARK_GRAY));
                lore.add(Component.empty());
                lore.add(Component.text("Clique para receber (nivel max)!", NamedTextColor.YELLOW).decorate(TextDecoration.ITALIC));
                meta.lore(lore);
                PersistentDataContainer pdc = meta.getPersistentDataContainer();
                pdc.set(KEY_MENU_ACTION, PersistentDataType.STRING, "give_enchantbook");
                pdc.set(KEY_MENU_CATEGORY, PersistentDataType.STRING, type.name());
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                book.setItemMeta(meta);
            }
            inv.setItem(i - start, book);
        }

        inv.setItem(45, buildNavIcon(Material.ARROW, "Voltar ao Menu", "nav_back", NamedTextColor.RED));
        if (safePage > 0)
            inv.setItem(48, buildNavIcon(Material.ARROW, "Pagina Anterior", "nav_prev_enchants_" + (safePage - 1), NamedTextColor.YELLOW));
        if (safePage < totalPages - 1)
            inv.setItem(50, buildNavIcon(Material.ARROW, "Proxima Pagina", "nav_next_enchants_" + (safePage + 1), NamedTextColor.GREEN));
        inv.setItem(53, buildInfoIcon(Material.PAPER, "Pagina " + (safePage + 1) + "/" + totalPages,
                allEnchants.length + " encantamentos custom"));

        player.openInventory(inv);
    }

    // ========== BEST BUDDIES PAGE ==========

    public void openBestBuddiesPage(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27,
                Component.text("JGlims ", NamedTextColor.GOLD).decorate(TextDecoration.BOLD)
                        .append(Component.text("| Best Buddies", NamedTextColor.LIGHT_PURPLE)));

        // Create wolf armor with Best Buddies enchantment
        ItemStack wolfArmor = new ItemStack(Material.WOLF_ARMOR);
        ItemMeta wm = wolfArmor.getItemMeta();
        if (wm != null) {
            wm.displayName(Component.text("Best Buddies Wolf Armor", NamedTextColor.LIGHT_PURPLE).decorate(TextDecoration.BOLD));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Enchantment: Best Buddies I", NamedTextColor.AQUA));
            lore.add(Component.empty());
            lore.add(Component.text("95% damage reduction for your wolf", NamedTextColor.GRAY));
            lore.add(Component.text("Wolf deals 0 damage (pacifist)", NamedTextColor.GRAY));
            lore.add(Component.text("Permanent Regeneration II aura", NamedTextColor.GRAY));
            lore.add(Component.text("Heart particles periodically", NamedTextColor.GRAY));
            lore.add(Component.empty());
            lore.add(Component.text("Equip on a tamed wolf!", NamedTextColor.YELLOW).decorate(TextDecoration.ITALIC));
            wm.lore(lore);
            // Apply the Best Buddies enchantment via PDC
            NamespacedKey bbKey = plugin.getEnchantManager().getKey(EnchantmentType.BEST_BUDDIES);
            wm.getPersistentDataContainer().set(bbKey, PersistentDataType.INTEGER, 1);
            wolfArmor.setItemMeta(wm);
        }

        inv.setItem(13, tagForMenu(wolfArmor, "give_bestbuddies", "wolf_armor"));

        inv.setItem(18, buildNavIcon(Material.ARROW, "Voltar ao Menu", "nav_back", NamedTextColor.RED));
        player.openInventory(inv);
    }

    // ========== CLICK HANDLER ==========

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta()) return;

        Component title = event.getView().title();
        String titleStr = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(title);
        if (!titleStr.startsWith("JGlims")) return;

        event.setCancelled(true);

        ItemMeta meta = event.getCurrentItem().getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        String action = pdc.get(KEY_MENU_ACTION, PersistentDataType.STRING);
        String category = pdc.get(KEY_MENU_CATEGORY, PersistentDataType.STRING);

        if (action == null && category == null) return;

        // Navigation
        if (action != null && action.startsWith("nav_")) {
            handleNav(player, action);
            return;
        }

        // Category icons
        if (action != null && action.startsWith("cat_")) {
            handleCategoryClick(player, action);
            return;
        }

        // Give items (OP only)
        if (!player.isOp()) {
            player.sendMessage(Component.text("Voce precisa ser OP para pegar itens!", NamedTextColor.RED));
            return;
        }

        if (action == null) return;

        switch (action) {
            case "give_weapon" -> {
                if (category == null) return;
                LegendaryWeapon weapon = LegendaryWeapon.fromId(category);
                if (weapon == null) return;
                ItemStack item = plugin.getLegendaryWeaponManager().createWeapon(weapon);
                if (item != null) {
                    player.getInventory().addItem(item);
                    player.sendMessage(Component.text("Recebeu: ", NamedTextColor.GREEN)
                            .append(Component.text(weapon.getDisplayName(), weapon.getTier().getColor())));
                }
            }
            case "give_armor" -> {
                if (category == null) return;
                LegendaryArmorSet set = LegendaryArmorSet.fromId(category);
                if (set == null) return;
                for (LegendaryArmorSet.ArmorSlot slot2 : LegendaryArmorSet.ArmorSlot.values()) {
                    player.getInventory().addItem(plugin.getLegendaryArmorManager().createArmorPiece(set, slot2));
                }
                player.sendMessage(Component.text("Recebeu set completo: ", NamedTextColor.GREEN)
                        .append(Component.text(set.getDisplayName(), set.getTier().getColor())));
            }
            case "give_powerup" -> {
                if (category == null) return;
                PowerUpManager pm = plugin.getPowerUpManager();
                ItemStack pu = switch (category) {
                    case "heart" -> pm.createHeartCrystal();
                    case "soul" -> pm.createSoulFragment();
                    case "titan" -> pm.createTitanResolve();
                    case "phoenix" -> pm.createPhoenixFeather();
                    case "keep" -> pm.createKeepInventorer();
                    case "vitality" -> pm.createVitalityShard();
                    case "berserker" -> pm.createBerserkerMark();
                    default -> null;
                };
                if (pu != null) {
                    player.getInventory().addItem(pu);
                    player.sendMessage(Component.text("Recebeu power-up!", NamedTextColor.GREEN));
                }
            }
            case "give_blessing" -> {
                if (category == null) return;
                ItemStack bl = switch (category) {
                    case "c_bless" -> {
                        ItemStack b = new ItemStack(Material.GLISTERING_MELON_SLICE);
                        ItemMeta bm = b.getItemMeta();
                        bm.displayName(Component.text("C's Bless", NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
                        bm.getPersistentDataContainer().set(new NamespacedKey(plugin, "c_bless_item"), PersistentDataType.BYTE, (byte) 1);
                        b.setItemMeta(bm);
                        yield b;
                    }
                    case "ami_bless" -> {
                        ItemStack b = new ItemStack(Material.GOLDEN_CARROT);
                        ItemMeta bm = b.getItemMeta();
                        bm.displayName(Component.text("Ami's Bless", NamedTextColor.RED).decorate(TextDecoration.BOLD));
                        bm.getPersistentDataContainer().set(new NamespacedKey(plugin, "ami_bless_item"), PersistentDataType.BYTE, (byte) 1);
                        b.setItemMeta(bm);
                        yield b;
                    }
                    case "la_bless" -> {
                        ItemStack b = new ItemStack(Material.GOLDEN_APPLE);
                        ItemMeta bm = b.getItemMeta();
                        bm.displayName(Component.text("La's Bless", NamedTextColor.BLUE).decorate(TextDecoration.BOLD));
                        bm.getPersistentDataContainer().set(new NamespacedKey(plugin, "la_bless_item"), PersistentDataType.BYTE, (byte) 1);
                        b.setItemMeta(bm);
                        yield b;
                    }
                    default -> null;
                };
                if (bl != null) {
                    player.getInventory().addItem(bl);
                    player.sendMessage(Component.text("Recebeu blessing!", NamedTextColor.GREEN));
                }
            }
            case "give_battletool" -> {
                if (category == null) return;
                ItemStack bt = switch (category) {
                    case "battle_sword" -> plugin.getBattleSwordManager().createBattleSword();
                    case "battle_axe" -> plugin.getBattleAxeManager().createBattleAxe();
                    case "battle_bow" -> plugin.getBattleBowManager().createBattleBow();
                    case "battle_crossbow" -> plugin.getBattleBowManager().createBattleCrossbow();
                    case "battle_mace" -> plugin.getBattleMaceManager().createBattleMace();
                    case "battle_trident" -> plugin.getBattleTridentManager().createBattleTrident();
                    case "battle_pickaxe" -> plugin.getBattlePickaxeManager().createBattlePickaxe();
                    case "battle_shovel" -> plugin.getBattleShovelManager().createBattleShovel();
                    case "battle_spear" -> plugin.getBattleSpearManager().createBattleSpear();
                    default -> null;
                };
                if (bt != null) {
                    player.getInventory().addItem(bt);
                    player.sendMessage(Component.text("Recebeu battle tool!", NamedTextColor.GREEN));
                }
            }
            case "give_abyssal_key" -> {
                player.getInventory().addItem(plugin.getAbyssDimensionManager().createAbyssalKey());
                player.sendMessage(Component.text("Recebeu Abyssal Key!", NamedTextColor.DARK_RED));
            }
            case "give_sickle" -> {
                if (category == null) return;
                ItemStack sk = switch (category) {
                    case "wood_sickle" -> plugin.getSickleManager().createWoodSickle();
                    case "stone_sickle" -> plugin.getSickleManager().createStoneSickle();
                    case "iron_sickle" -> plugin.getSickleManager().createIronSickle();
                    case "diamond_sickle" -> plugin.getSickleManager().createDiamondSickle();
                    case "netherite_sickle" -> plugin.getSickleManager().createNetheriteSickle();
                    default -> null;
                };
                if (sk != null) {
                    player.getInventory().addItem(sk);
                    player.sendMessage(Component.text("Recebeu sickle!", NamedTextColor.GREEN));
                }
            }
            case "give_spear" -> {
                if (category == null) return;
                ItemStack sp = switch (category) {
                    case "wood_spear" -> plugin.getSpearManager().createWoodSpear();
                    case "stone_spear" -> plugin.getSpearManager().createStoneSpear();
                    case "iron_spear" -> plugin.getSpearManager().createIronSpear();
                    case "diamond_spear" -> plugin.getSpearManager().createDiamondSpear();
                    case "netherite_spear" -> plugin.getSpearManager().createNetheriteSpear();
                    default -> null;
                };
                if (sp != null) {
                    player.getInventory().addItem(sp);
                    player.sendMessage(Component.text("Recebeu spear!", NamedTextColor.GREEN));
                }
            }
            case "give_supertool" -> {
                if (category == null) return;
                Material toolMat;
                try {
                    toolMat = Material.valueOf(category.toUpperCase());
                } catch (Exception e) { return; }
                ItemStack st = plugin.getSuperToolManager().createSuperTool(toolMat);
                if (st != null) {
                    player.getInventory().addItem(st);
                    player.sendMessage(Component.text("Recebeu super tool!", NamedTextColor.GREEN));
                }
            }
            case "give_enchantbook" -> {
                if (category == null) return;
                EnchantmentType enchType;
                try {
                    enchType = EnchantmentType.valueOf(category);
                } catch (Exception e) { return; }
                // Create an enchanted book with the custom enchantment at max level
                ItemStack enchBook = new ItemStack(Material.ENCHANTED_BOOK);
                ItemMeta em = enchBook.getItemMeta();
                if (em != null) {
                    String displayName = formatEnchantName(enchType.name());
                    em.displayName(Component.text(displayName + " " + toRoman(enchType.getMaxLevel()), NamedTextColor.AQUA).decorate(TextDecoration.BOLD));
                    List<Component> bookLore = new ArrayList<>();
                    bookLore.add(Component.text("Custom Enchantment", NamedTextColor.DARK_PURPLE));
                    bookLore.add(Component.text("Level: " + enchType.getMaxLevel(), NamedTextColor.GRAY));
                    bookLore.add(Component.text("Apply via Anvil", NamedTextColor.DARK_GRAY));
                    em.lore(bookLore);
                    // Store the enchantment in PDC so the anvil system recognizes it
                    NamespacedKey enchKey = plugin.getEnchantManager().getKey(enchType);
                    em.getPersistentDataContainer().set(enchKey, PersistentDataType.INTEGER, enchType.getMaxLevel());
                    enchBook.setItemMeta(em);
                }
                player.getInventory().addItem(enchBook);
                player.sendMessage(Component.text("Recebeu: ", NamedTextColor.GREEN)
                        .append(Component.text(formatEnchantName(enchType.name()) + " " + toRoman(enchType.getMaxLevel()), NamedTextColor.AQUA)));
            }
            case "give_bestbuddies" -> {
                ItemStack wolfArmor = new ItemStack(Material.WOLF_ARMOR);
                ItemMeta wm = wolfArmor.getItemMeta();
                if (wm != null) {
                    wm.displayName(Component.text("Best Buddies Wolf Armor", NamedTextColor.LIGHT_PURPLE).decorate(TextDecoration.BOLD));
                    List<Component> wlore = new ArrayList<>();
                    wlore.add(Component.text("Enchantment: Best Buddies I", NamedTextColor.AQUA));
                    wlore.add(Component.text("95% damage reduction", NamedTextColor.GRAY));
                    wlore.add(Component.text("Pacifist companion", NamedTextColor.GRAY));
                    wlore.add(Component.text("Regen II aura", NamedTextColor.GRAY));
                    wm.lore(wlore);
                    NamespacedKey bbKey = plugin.getEnchantManager().getKey(EnchantmentType.BEST_BUDDIES);
                    wm.getPersistentDataContainer().set(bbKey, PersistentDataType.INTEGER, 1);
                    wolfArmor.setItemMeta(wm);
                }
                player.getInventory().addItem(wolfArmor);
                player.sendMessage(Component.text("Recebeu Best Buddies Wolf Armor!", NamedTextColor.LIGHT_PURPLE));
            }
            case "give_infinity" -> {
                if (category == null) return;
                if (category.equals("glove")) {
                    player.getInventory().addItem(plugin.getInfinityGauntletManager().createThanosGlove());
                } else if (category.equals("gauntlet")) {
                    player.getInventory().addItem(plugin.getInfinityGauntletManager().createInfinityGauntlet());
                } else if (category.startsWith("stone_")) {
                    String stoneId = category.substring(6);
                    InfinityStoneManager.StoneType st = InfinityStoneManager.StoneType.fromId(stoneId);
                    if (st != null) {
                        player.getInventory().addItem(plugin.getInfinityStoneManager().createFinishedStone(st));
                    }
                }
                player.sendMessage(Component.text("Recebeu item do Infinito!", NamedTextColor.GREEN));
            }
        }
    }

    // ========== CATEGORY HANDLER ==========

    private void handleCategoryClick(Player player, String cat) {
        switch (cat) {
            case "cat_weapons_common" -> openWeaponPage(player, LegendaryTier.COMMON, 0);
            case "cat_weapons_rare" -> openWeaponPage(player, LegendaryTier.RARE, 0);
            case "cat_weapons_epic" -> openWeaponPage(player, LegendaryTier.EPIC, 0);
            case "cat_weapons_mythic" -> openWeaponPage(player, LegendaryTier.MYTHIC, 0);
            case "cat_weapons_abyssal" -> openWeaponPage(player, LegendaryTier.ABYSSAL, 0);
            case "cat_armor" -> openArmorPage(player);
            case "cat_powerups" -> openPowerUpsPage(player);
            case "cat_infinity" -> openInfinityPage(player);
            case "cat_blessings" -> openBlessingsPage(player);
            case "cat_battletools" -> openBattleToolsPage(player);
            case "cat_abyssal_key" -> giveAbyssalKey(player);
            case "cat_sickles" -> openSicklesPage(player);
            case "cat_supertools" -> openSuperToolsPage(player);
            case "cat_enchantbooks" -> openEnchantBooksPage(player, 0);
            case "cat_bestbuddies" -> openBestBuddiesPage(player);
        }
    }

    // ========== NAVIGATION HANDLER ==========

    private void handleNav(Player player, String nav) {
        if (nav.equals("nav_back")) {
            openMainMenu(player);
            return;
        }
        // nav_prev_enchants_0 or nav_next_enchants_1
        if (nav.contains("_enchants_")) {
            String[] parts = nav.split("_");
            int page;
            try { page = Integer.parseInt(parts[parts.length - 1]); } catch (NumberFormatException e) { return; }
            openEnchantBooksPage(player, page);
            return;
        }
        // nav_prev_MYTHIC_0 or nav_next_MYTHIC_1
        String[] parts = nav.split("_", 4);
        if (parts.length >= 4) {
            String tierStr = parts[2];
            int page;
            try { page = Integer.parseInt(parts[3]); } catch (NumberFormatException e) { return; }
            LegendaryTier tier = LegendaryTier.fromId(tierStr);
            if (tier != null) openWeaponPage(player, tier, page);
        }
    }

    private void giveAbyssalKey(Player player) {
        if (!player.isOp()) {
            player.sendMessage(Component.text("Voce precisa ser OP!", NamedTextColor.RED));
            return;
        }
        player.getInventory().addItem(plugin.getAbyssDimensionManager().createAbyssalKey());
        player.sendMessage(Component.text("Recebeu Abyssal Key!", NamedTextColor.DARK_RED));
    }

    // ========== HELPERS ==========

    private ItemStack buildCategoryIcon(Material mat, String name, String desc, String actionId, NamedTextColor color) {
        ItemStack item = new ItemStack(mat);
        ItemMeta m = item.getItemMeta();
        if (m != null) {
            m.displayName(Component.text(name, color).decorate(TextDecoration.BOLD));
            m.lore(List.of(
                    Component.text(desc, NamedTextColor.GRAY),
                    Component.empty(),
                    Component.text("Clique para abrir!", NamedTextColor.YELLOW)
            ));
            m.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
            m.getPersistentDataContainer().set(KEY_MENU_ACTION, PersistentDataType.STRING, actionId);
            item.setItemMeta(m);
        }
        return item;
    }

    private ItemStack buildNavIcon(Material mat, String name, String actionId, NamedTextColor color) {
        ItemStack item = new ItemStack(mat);
        ItemMeta m = item.getItemMeta();
        if (m != null) {
            m.displayName(Component.text(name, color));
            m.getPersistentDataContainer().set(KEY_MENU_ACTION, PersistentDataType.STRING, actionId);
            item.setItemMeta(m);
        }
        return item;
    }

    private ItemStack buildInfoIcon(Material mat, String name, String desc) {
        ItemStack item = new ItemStack(mat);
        ItemMeta m = item.getItemMeta();
        if (m != null) {
            m.displayName(Component.text(name, NamedTextColor.AQUA));
            m.lore(List.of(Component.text(desc, NamedTextColor.GRAY)));
            item.setItemMeta(m);
        }
        return item;
    }

    private ItemStack buildBorder() {
        ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta m = item.getItemMeta();
        if (m != null) {
            m.displayName(Component.text(" "));
            item.setItemMeta(m);
        }
        return item;
    }

    private ItemStack tagForMenu(ItemStack item, String action, String category) {
        if (item == null) return new ItemStack(Material.BARRIER);
        ItemMeta m = item.getItemMeta();
        if (m != null) {
            List<Component> lore = m.lore() != null ? new ArrayList<>(m.lore()) : new ArrayList<>();
            lore.add(Component.empty());
            lore.add(Component.text("Clique para receber!", NamedTextColor.YELLOW).decorate(TextDecoration.ITALIC));
            m.lore(lore);
            PersistentDataContainer p = m.getPersistentDataContainer();
            p.set(KEY_MENU_ACTION, PersistentDataType.STRING, action);
            p.set(KEY_MENU_CATEGORY, PersistentDataType.STRING, category);
            item.setItemMeta(m);
        }
        return item;
    }

    private String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }

    private String formatEnchantName(String enumName) {
        StringBuilder sb = new StringBuilder();
        for (String word : enumName.split("_")) {
            sb.append(Character.toUpperCase(word.charAt(0)))
              .append(word.substring(1).toLowerCase())
              .append(' ');
        }
        return sb.toString().trim();
    }

    private String getEnchantCategory(EnchantmentType type) {
        String name = type.name();
        if (name.contains("VAMPIRISM") || name.contains("BLEED") || name.contains("VENOM") ||
            name.contains("LIFESTEAL") || name.contains("CHAIN_LIGHTNING") || name.contains("FROSTBITE_BLADE"))
            return "Sword";
        if (name.contains("BERSERKER") || name.contains("LUMBERJACK") || name.contains("CLEAVE") ||
            name.contains("TIMBER") || name.contains("GUILLOTINE") || name.contains("WRATH"))
            return "Axe";
        if (name.contains("VEINMINER") || name.contains("DRILL") || name.contains("AUTO_SMELT") ||
            name.contains("MAGNETISM") || name.contains("EXCAVATOR") || name.contains("PROSPECTOR"))
            return "Pickaxe";
        if (name.contains("HARVESTER") || name.contains("BURIAL") || name.contains("EARTHSHATTER"))
            return "Shovel";
        if (name.contains("GREEN_THUMB") || name.contains("REPLENISH") || name.contains("HARVESTING_MOON") ||
            name.contains("SOUL_HARVEST") || name.contains("REAPING_CURSE") || name.contains("CROP_REAPER"))
            return "Hoe/Sickle";
        if (name.contains("EXPLOSIVE_ARROW") || name.contains("HOMING") || name.contains("RAPIDFIRE") ||
            name.contains("SNIPER") || name.contains("FROSTBITE_ARROW"))
            return "Bow";
        if (name.contains("THUNDERLORD") || name.contains("TIDAL_WAVE"))
            return "Crossbow";
        if (name.contains("FROSTBITE") || name.contains("SOUL_REAP") || name.contains("BLOOD_PRICE") ||
            name.contains("REAPERS_MARK") || name.contains("WITHER_TOUCH") || name.contains("TSUNAMI"))
            return "Trident";
        if (name.contains("SWIFTNESS") || name.contains("VITALITY") || name.contains("AQUA_LUNGS") ||
            name.contains("NIGHT_VISION") || name.contains("FORTIFICATION") || name.contains("DEFLECTION") ||
            name.contains("SWIFTFOOT") || name.contains("DODGE") || name.contains("LEAPING"))
            return "Armor";
        if (name.contains("BOOST") || name.contains("CUSHION") || name.contains("GLIDER") || name.contains("STOMP"))
            return "Elytra";
        if (name.contains("SEISMIC") || name.contains("MAGNETIZE") || name.contains("GRAVITY_WELL") ||
            name.contains("MOMENTUM") || name.contains("TREMOR"))
            return "Mace";
        if (name.contains("IMPALING_THRUST") || name.contains("EXTENDED_REACH") ||
            name.contains("SKEWERING") || name.contains("PHANTOM_PIERCE"))
            return "Spear";
        if (name.contains("SOULBOUND") || name.contains("BEST_BUDDIES"))
            return "Universal";
        return "Other";
    }

    private String toRoman(int num) {
        return switch (num) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> String.valueOf(num);
        };
    }
}
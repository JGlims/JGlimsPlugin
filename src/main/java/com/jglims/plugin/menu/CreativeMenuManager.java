package com.jglims.plugin.menu;

import com.jglims.plugin.JGlimsPlugin;
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
 * Categories: Weapons (by tier), Armor Sets, Power-Ups, Infinity Items, Battle Tools.
 * OP-only for giving items; anyone can browse.
 */
public class CreativeMenuManager implements Listener {

    private final JGlimsPlugin plugin;
    private final NamespacedKey KEY_MENU_TYPE;
    private final NamespacedKey KEY_MENU_PAGE;
    private final NamespacedKey KEY_MENU_CATEGORY;
    private final NamespacedKey KEY_MENU_ACTION;

    private static final String MENU_TITLE_PREFIX = "\u00a76\u00a7lJGlims";
    private static final int SLOTS = 54; // 6 rows

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

        // Row 2: categories centered
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

        // Row 5: info
        inv.setItem(40, buildInfoIcon(Material.BOOK, "Guia do Servidor",
                "Use /guia para receber o livro-guia completo!"));

        // Fill borders with glass
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

        // Navigation row (slot 45-53)
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
        int slot = 10;
        ItemStack[] items = {
                tagForMenu(pm.createHeartCrystal(), "give_powerup", "heart"),
                tagForMenu(pm.createSoulFragment(), "give_powerup", "soul"),
                tagForMenu(pm.createTitanResolve(), "give_powerup", "titan"),
                tagForMenu(pm.createPhoenixFeather(), "give_powerup", "phoenix"),
                tagForMenu(pm.createKeepInventorer(), "give_powerup", "keep"),
                tagForMenu(pm.createVitalityShard(), "give_powerup", "vitality"),
                tagForMenu(pm.createBerserkerMark(), "give_powerup", "berserker")
        };
        for (ItemStack it : items) {
            if (slot == 13) slot = 14; // skip middle for spacing
            inv.setItem(slot++, it);
            if (slot > 16) break;
        }
        // Place remaining in row below if needed
        if (items.length > 5) {
            inv.setItem(12, items[5]);
            if (items.length > 6) inv.setItem(14, items[6]);
        }
        // Rewrite: place all 7 in a row
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
        // 6 finished stones
        for (InfinityStoneManager.StoneType st : InfinityStoneManager.StoneType.values()) {
            inv.setItem(slot++, tagForMenu(ism.createFinishedStone(st), "give_infinity", "stone_" + st.getId()));
        }

        // Thanos Glove + Gauntlet in row 2
        inv.setItem(11, tagForMenu(igm.createThanosGlove(), "give_infinity", "glove"));
        inv.setItem(15, tagForMenu(igm.createInfinityGauntlet(), "give_infinity", "gauntlet"));

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

        // Navigation actions
        if (action != null && action.startsWith("nav_")) {
            handleNav(player, action);
            return;
        }

        // Category icons from main menu
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

    // ========== HELPERS ==========

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
        }
    }

    private void handleNav(Player player, String nav) {
        if (nav.equals("nav_back")) {
            openMainMenu(player);
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

    private ItemStack buildCategoryIcon(Material mat, String name, String desc, String actionId, NamedTextColor color) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(name, color).decorate(TextDecoration.BOLD));
            meta.lore(List.of(
                    Component.text(desc, NamedTextColor.GRAY),
                    Component.empty(),
                    Component.text("Clique para abrir!", NamedTextColor.YELLOW)
            ));
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
            meta.getPersistentDataContainer().set(KEY_MENU_ACTION, PersistentDataType.STRING, actionId);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack buildNavIcon(Material mat, String name, String actionId, NamedTextColor color) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(name, color));
            meta.getPersistentDataContainer().set(KEY_MENU_ACTION, PersistentDataType.STRING, actionId);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack buildInfoIcon(Material mat, String name, String desc) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(name, NamedTextColor.AQUA));
            meta.lore(List.of(Component.text(desc, NamedTextColor.GRAY)));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack buildBorder() {
        ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(" "));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack tagForMenu(ItemStack item, String action, String category) {
        if (item == null) return new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            List<Component> lore = meta.lore() != null ? new ArrayList<>(meta.lore()) : new ArrayList<>();
            lore.add(Component.empty());
            lore.add(Component.text("Clique para receber!", NamedTextColor.YELLOW).decorate(TextDecoration.ITALIC));
            meta.lore(lore);
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(KEY_MENU_ACTION, PersistentDataType.STRING, action);
            pdc.set(KEY_MENU_CATEGORY, PersistentDataType.STRING, category);
            item.setItemMeta(meta);
        }
        return item;
    }

    private String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }
}
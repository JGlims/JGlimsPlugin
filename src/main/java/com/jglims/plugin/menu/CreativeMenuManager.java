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

    public void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, SLOTS,
                Component.text("JGlims ", NamedTextColor.GOLD).decorate(TextDecoration.BOLD)
                        .append(Component.text("| Menu Principal", NamedTextColor.WHITE)));

        inv.setItem(10, buildCategoryIcon(Material.DIAMOND_SWORD, "Armas Comuns", "20 armas tier COMMON", "cat_weapons_common", NamedTextColor.WHITE));
        inv.setItem(11, buildCategoryIcon(Material.IRON_SWORD, "Itens Criados", "15 itens craftaveis (Lunar, Fang, Void, etc.)", "cat_crafted", NamedTextColor.AQUA));
        inv.setItem(12, buildCategoryIcon(Material.DIAMOND_SWORD, "Armas Raras", "8 armas tier RARE", "cat_weapons_rare", NamedTextColor.GREEN));
        inv.setItem(13, buildCategoryIcon(Material.BONE, "Drops de Mobs", "13 itens dropados por mobs custom", "cat_drops", NamedTextColor.GRAY));
        inv.setItem(14, buildCategoryIcon(Material.DIAMOND_SWORD, "Armas Epicas", "7 armas tier EPIC", "cat_weapons_epic", NamedTextColor.DARK_PURPLE));
        inv.setItem(15, buildCategoryIcon(Material.REDSTONE, "Itens de Vampiro", "Blood, Essence, Evolver, Ring + Habilidades", "cat_vampire", NamedTextColor.DARK_RED));
        inv.setItem(16, buildCategoryIcon(Material.DIAMOND_SWORD, "Armas Miticas", "24 armas tier MYTHIC", "cat_weapons_mythic", NamedTextColor.GOLD));

        inv.setItem(19, buildCategoryIcon(Material.DIAMOND_SWORD, "Armas Abissais", "4 armas tier ABYSSAL", "cat_weapons_abyssal", NamedTextColor.DARK_RED));
        inv.setItem(21, buildCategoryIcon(Material.NETHERITE_CHESTPLATE, "Armaduras", "13 sets de armadura", "cat_armor", NamedTextColor.AQUA));
        inv.setItem(23, buildCategoryIcon(Material.NETHER_STAR, "Power-Ups", "7 power-ups permanentes", "cat_powerups", NamedTextColor.LIGHT_PURPLE));
        inv.setItem(25, buildCategoryIcon(Material.AMETHYST_SHARD, "Infinity Items", "Manopla + 6 pedras", "cat_infinity", NamedTextColor.DARK_AQUA));

        inv.setItem(28, buildCategoryIcon(Material.GOLDEN_APPLE, "Blessings", "3 blessings permanentes", "cat_blessings", NamedTextColor.GOLD));
        inv.setItem(32, buildCategoryIcon(Material.ECHO_SHARD, "Abyssal Key", "Chave para o Abyss", "cat_abyssal_key", NamedTextColor.DARK_RED));
        inv.setItem(34, buildCategoryIcon(Material.BONE, "Werewolf Items", "Moon Stone + Wolf Form", "cat_werewolf", NamedTextColor.LIGHT_PURPLE));

        inv.setItem(38, buildCategoryIcon(Material.BLAZE_ROD, "Itens Magicos", "Wand of Wands e outros itens magicos", "cat_magic", NamedTextColor.LIGHT_PURPLE));
        inv.setItem(39, buildCategoryIcon(Material.ENCHANTED_BOOK, "Enchantment Books", "Todos os 64+ encantamentos custom", "cat_enchantbooks", NamedTextColor.YELLOW));
        inv.setItem(41, buildCategoryIcon(Material.WOLF_ARMOR, "Best Buddies", "Armadura de lobo com Best Buddies", "cat_bestbuddies", NamedTextColor.LIGHT_PURPLE));
        inv.setItem(43, buildInfoIcon(Material.BOOK, "Guia do Servidor", "Use /guia para receber o livro-guia completo!"));

        ItemStack border = buildBorder();
        for (int i = 0; i < 9; i++) { if (inv.getItem(i) == null) inv.setItem(i, border); }
        for (int i = 45; i < 54; i++) { if (inv.getItem(i) == null) inv.setItem(i, border); }
        for (int i : new int[]{9, 18, 27, 36, 17, 26, 35, 44}) {
            if (inv.getItem(i) == null) inv.setItem(i, border);
        }

        player.openInventory(inv);
    }

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

    // ========== SICKLES & SPEARS PAGE ==========

    public void openSicklesPage(Player player) {
        Inventory inv = Bukkit.createInventory(null, SLOTS,
                Component.text("JGlims ", NamedTextColor.GOLD).decorate(TextDecoration.BOLD)
                        .append(Component.text("| Sickles & Spears", NamedTextColor.GREEN)));

        inv.setItem(10, tagForMenu(plugin.getSickleManager().createSickle(Material.WOODEN_HOE), "give_sickle", "WOODEN_HOE"));
        inv.setItem(11, tagForMenu(plugin.getSickleManager().createSickle(Material.STONE_HOE), "give_sickle", "STONE_HOE"));
        inv.setItem(12, tagForMenu(plugin.getSickleManager().createSickle(Material.IRON_HOE), "give_sickle", "IRON_HOE"));
        inv.setItem(13, tagForMenu(plugin.getSickleManager().createSickle(Material.DIAMOND_HOE), "give_sickle", "DIAMOND_HOE"));
        inv.setItem(14, tagForMenu(plugin.getSickleManager().createSickle(Material.NETHERITE_HOE), "give_sickle", "NETHERITE_HOE"));

        inv.setItem(19, tagForMenu(new ItemStack(Material.WOODEN_SPEAR), "give_spear", "WOODEN_SPEAR"));
        inv.setItem(20, tagForMenu(new ItemStack(Material.STONE_SPEAR), "give_spear", "STONE_SPEAR"));
        inv.setItem(21, tagForMenu(new ItemStack(Material.IRON_SPEAR), "give_spear", "IRON_SPEAR"));
        inv.setItem(22, tagForMenu(new ItemStack(Material.DIAMOND_SPEAR), "give_spear", "DIAMOND_SPEAR"));
        inv.setItem(23, tagForMenu(new ItemStack(Material.NETHERITE_SPEAR), "give_spear", "NETHERITE_SPEAR"));

        inv.setItem(45, buildNavIcon(Material.ARROW, "Voltar ao Menu", "nav_back", NamedTextColor.RED));
        player.openInventory(inv);
    }

    // ========== ENCHANTMENT BOOKS PAGE ==========

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
        inv.setItem(53, buildInfoIcon(Material.PAPER, "Pagina " + (safePage + 1) + "/" + totalPages, allEnchants.length + " encantamentos custom"));

        player.openInventory(inv);
    }

    // ========== BEST BUDDIES PAGE ==========

    public void openBestBuddiesPage(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27,
                Component.text("JGlims ", NamedTextColor.GOLD).decorate(TextDecoration.BOLD)
                        .append(Component.text("| Best Buddies", NamedTextColor.LIGHT_PURPLE)));

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
            lore.add(Component.empty());
            lore.add(Component.text("Equip on a tamed wolf!", NamedTextColor.YELLOW).decorate(TextDecoration.ITALIC));
            wm.lore(lore);
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

        if (action != null && action.startsWith("nav_")) { handleNav(player, action); return; }
        if (action != null && action.startsWith("cat_")) { handleCategoryClick(player, action); return; }

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
            case "give_abyssal_key" -> {
                player.getInventory().addItem(plugin.getAbyssDimensionManager().createAbyssalKey());
                player.sendMessage(Component.text("Recebeu Abyssal Key!", NamedTextColor.DARK_RED));
            }
            case "give_moon_stone" -> {
                if (plugin.getWerewolfManager() == null) return;
                player.getInventory().addItem(plugin.getWerewolfManager().createWerewolfBlood());
                player.sendMessage(Component.text("Recebeu Moon Stone!", NamedTextColor.LIGHT_PURPLE));
            }
            case "give_wolf_form" -> {
                if (plugin.getWerewolfManager() == null) return;
                player.getInventory().addItem(plugin.getWerewolfManager().createWolfFormItem());
                player.sendMessage(Component.text("Recebeu Wolf Form ability!", NamedTextColor.LIGHT_PURPLE));
            }
            case "give_enchantbook" -> {
                if (category == null) return;
                EnchantmentType enchType;
                try { enchType = EnchantmentType.valueOf(category); } catch (Exception e) { return; }
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
            case "give_crafted" -> giveCraftedItem(player, category);
            case "give_drop" -> giveDropItem(player, category);
            case "give_vampire" -> giveVampireItem(player, category);
            case "give_magic" -> giveMagicItem(player, category);
        }
    }

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
            case "cat_abyssal_key" -> giveAbyssalKey(player);
            case "cat_werewolf" -> openWerewolfItemsPage(player);
            case "cat_enchantbooks" -> openEnchantBooksPage(player, 0);
            case "cat_bestbuddies" -> openBestBuddiesPage(player);
            case "cat_crafted" -> openCraftedItemsPage(player);
            case "cat_drops" -> openDropItemsPage(player);
            case "cat_vampire" -> openVampireItemsPage(player);
            case "cat_magic" -> openMagicItemsPage(player);
        }
    }

    private void handleNav(Player player, String nav) {
        if (nav.equals("nav_back")) { openMainMenu(player); return; }
        if (nav.contains("_enchants_")) {
            String[] parts = nav.split("_");
            int page;
            try { page = Integer.parseInt(parts[parts.length - 1]); } catch (NumberFormatException e) { return; }
            openEnchantBooksPage(player, page);
            return;
        }
        String[] parts = nav.split("_", 4);
        if (parts.length >= 4) {
            String tierStr = parts[2];
            int page;
            try { page = Integer.parseInt(parts[3]); } catch (NumberFormatException e) { return; }
            LegendaryTier tier = LegendaryTier.fromId(tierStr);
            if (tier != null) openWeaponPage(player, tier, page);
        }
    }

    public void openWerewolfItemsPage(Player player) {
        Inventory inv = Bukkit.createInventory(null, SLOTS,
                Component.text("JGlims ", NamedTextColor.GOLD).decorate(TextDecoration.BOLD)
                        .append(Component.text("| Itens de Werewolf", NamedTextColor.LIGHT_PURPLE)));
        var wm = plugin.getWerewolfManager();
        if (wm != null) {
            inv.setItem(11, tagForMenu(wm.createWerewolfBlood(), "give_moon_stone", "MOON_STONE"));
            inv.setItem(13, tagForMenu(wm.createWolfFormItem(), "give_wolf_form", "WOLF_FORM"));
        }
        inv.setItem(45, buildNavIcon(Material.ARROW, "Voltar ao Menu", "nav_back", NamedTextColor.RED));
        ItemStack border = buildBorder();
        for (int i = 0; i < SLOTS; i++) { if (inv.getItem(i) == null) inv.setItem(i, border); }
        player.openInventory(inv);
    }

    private void giveAbyssalKey(Player player) {
        if (!player.isOp()) { player.sendMessage(Component.text("Voce precisa ser OP!", NamedTextColor.RED)); return; }
        player.getInventory().addItem(plugin.getAbyssDimensionManager().createAbyssalKey());
        player.sendMessage(Component.text("Recebeu Abyssal Key!", NamedTextColor.DARK_RED));
    }

    private ItemStack buildCategoryIcon(Material mat, String name, String desc, String actionId, NamedTextColor color) {
        ItemStack item = new ItemStack(mat);
        ItemMeta m = item.getItemMeta();
        if (m != null) {
            m.displayName(Component.text(name, color).decorate(TextDecoration.BOLD));
            m.lore(List.of(Component.text(desc, NamedTextColor.GRAY), Component.empty(), Component.text("Clique para abrir!", NamedTextColor.YELLOW)));
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
        if (m != null) { m.displayName(Component.text(" ")); item.setItemMeta(m); }
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

    private String truncate(String s, int max) { return s.length() <= max ? s : s.substring(0, max) + "..."; }

    private String formatEnchantName(String enumName) {
        StringBuilder sb = new StringBuilder();
        for (String word : enumName.split("_")) {
            sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1).toLowerCase()).append(' ');
        }
        return sb.toString().trim();
    }

    private String getEnchantCategory(EnchantmentType type) {
        String name = type.name();
        if (name.contains("VAMPIRISM") || name.contains("BLEED") || name.contains("VENOM") || name.contains("LIFESTEAL") || name.contains("CHAIN_LIGHTNING") || name.contains("FROSTBITE_BLADE")) return "Sword";
        if (name.contains("BERSERKER") || name.contains("LUMBERJACK") || name.contains("CLEAVE") || name.contains("TIMBER") || name.contains("GUILLOTINE") || name.contains("WRATH")) return "Axe";
        if (name.contains("VEINMINER") || name.contains("DRILL") || name.contains("AUTO_SMELT") || name.contains("MAGNETISM") || name.contains("EXCAVATOR") || name.contains("PROSPECTOR")) return "Pickaxe";
        if (name.contains("HARVESTER") || name.contains("BURIAL") || name.contains("EARTHSHATTER")) return "Shovel";
        if (name.contains("GREEN_THUMB") || name.contains("REPLENISH") || name.contains("HARVESTING_MOON") || name.contains("SOUL_HARVEST") || name.contains("REAPING_CURSE") || name.contains("CROP_REAPER")) return "Hoe/Sickle";
        if (name.contains("EXPLOSIVE_ARROW") || name.contains("HOMING") || name.contains("RAPIDFIRE") || name.contains("SNIPER") || name.contains("FROSTBITE_ARROW")) return "Bow";
        if (name.contains("THUNDERLORD") || name.contains("TIDAL_WAVE")) return "Crossbow";
        if (name.contains("FROSTBITE") || name.contains("SOUL_REAP") || name.contains("BLOOD_PRICE") || name.contains("REAPERS_MARK") || name.contains("WITHER_TOUCH") || name.contains("TSUNAMI")) return "Trident";
        if (name.contains("SWIFTNESS") || name.contains("VITALITY") || name.contains("AQUA_LUNGS") || name.contains("NIGHT_VISION") || name.contains("FORTIFICATION") || name.contains("DEFLECTION") || name.contains("SWIFTFOOT") || name.contains("DODGE") || name.contains("LEAPING")) return "Armor";
        if (name.contains("BOOST") || name.contains("CUSHION") || name.contains("GLIDER") || name.contains("STOMP")) return "Elytra";
        if (name.contains("SEISMIC") || name.contains("MAGNETIZE") || name.contains("GRAVITY_WELL") || name.contains("MOMENTUM") || name.contains("TREMOR")) return "Mace";
        if (name.contains("IMPALING_THRUST") || name.contains("EXTENDED_REACH") || name.contains("SKEWERING") || name.contains("PHANTOM_PIERCE")) return "Spear";
        if (name.contains("SOULBOUND") || name.contains("BEST_BUDDIES")) return "Universal";
        return "Other";
    }

    private String toRoman(int num) {
        return switch (num) { case 1 -> "I"; case 2 -> "II"; case 3 -> "III"; case 4 -> "IV"; case 5 -> "V"; default -> String.valueOf(num); };
    }

    // ═══════════════════════════════════════════════════════════════
    //  NEW PAGES — Crafted / Drops / Vampire / Magic
    // ═══════════════════════════════════════════════════════════════

    public void openCraftedItemsPage(Player player) {
        Inventory inv = Bukkit.createInventory(null, SLOTS,
                Component.text("JGlims ", NamedTextColor.GOLD).decorate(TextDecoration.BOLD)
                        .append(Component.text("| Itens Criados", NamedTextColor.AQUA)));
        com.jglims.plugin.crafting.CraftedItemManager cim = plugin.getCraftedItemManager();
        if (cim != null) {
            ItemStack[] items = {
                    tagForMenu(cim.createBloodChalice(),      "give_crafted", "blood_chalice"),
                    tagForMenu(cim.createFangDagger(),        "give_crafted", "fang_dagger"),
                    tagForMenu(cim.createLunarBlade(),        "give_crafted", "lunar_blade"),
                    tagForMenu(cim.createRaptorGauntlet(),    "give_crafted", "raptor_gauntlet"),
                    tagForMenu(cim.createSharkToothNecklace(),"give_crafted", "shark_tooth_necklace"),
                    tagForMenu(cim.createTremorShield(),      "give_crafted", "tremor_shield"),
                    tagForMenu(cim.createVoidScepter(),       "give_crafted", "void_scepter"),
                    tagForMenu(cim.createSoulLanternItem(),   "give_crafted", "soul_lantern_item"),
                    tagForMenu(cim.createDinosaurBoneBow(),   "give_crafted", "dinosaur_bone_bow"),
                    tagForMenu(cim.createCrimsonElixir(),     "give_crafted", "crimson_elixir"),
                    tagForMenu(cim.createNightwalkerCloak(),  "give_crafted", "nightwalker_cloak"),
                    tagForMenu(cim.createTrollHelmet(),       "give_crafted", "troll_helmet"),
                    tagForMenu(cim.createTrollChestplate(),   "give_crafted", "troll_chestplate"),
                    tagForMenu(cim.createTrollLeggings(),     "give_crafted", "troll_leggings"),
                    tagForMenu(cim.createTrollBoots(),        "give_crafted", "troll_boots"),
            };
            int slot = 10;
            for (ItemStack it : items) {
                if (it == null) continue;
                inv.setItem(slot, it);
                slot++;
                if ((slot + 1) % 9 == 0) slot += 2; // skip right/left borders
            }
        }
        inv.setItem(45, buildNavIcon(Material.ARROW, "Voltar ao Menu", "nav_back", NamedTextColor.RED));
        player.openInventory(inv);
    }

    public void openDropItemsPage(Player player) {
        Inventory inv = Bukkit.createInventory(null, SLOTS,
                Component.text("JGlims ", NamedTextColor.GOLD).decorate(TextDecoration.BOLD)
                        .append(Component.text("| Drops de Mobs", NamedTextColor.GRAY)));
        com.jglims.plugin.custommobs.DropItemManager dim = plugin.getDropItemManager();
        if (dim != null) {
            ItemStack[] items = {
                    tagForMenu(dim.createTrollHide(1),       "give_drop", "troll_hide"),
                    tagForMenu(dim.createLunarFragment(1),   "give_drop", "lunar_fragment"),
                    tagForMenu(dim.createLunarBeastHide(1),  "give_drop", "lunar_beast_hide"),
                    tagForMenu(dim.createSharkTooth(1),      "give_drop", "shark_tooth"),
                    tagForMenu(dim.createBearClaw(1),        "give_drop", "bear_claw"),
                    tagForMenu(dim.createRaptorClaw(1),      "give_drop", "raptor_claw"),
                    tagForMenu(dim.createBasiliskFang(1),    "give_drop", "basilisk_fang"),
                    tagForMenu(dim.createDarkEssence(1),     "give_drop", "dark_essence"),
                    tagForMenu(dim.createVoidEssence(1),     "give_drop", "void_essence"),
                    tagForMenu(dim.createSoulFragment(1),    "give_drop", "soul_fragment"),
                    tagForMenu(dim.createTremorScale(1),     "give_drop", "tremor_scale"),
                    tagForMenu(dim.createHorn(1),            "give_drop", "horn"),
                    tagForMenu(dim.createSpinosaurusSail(1), "give_drop", "spinosaurus_sail"),
            };
            int slot = 10;
            for (ItemStack it : items) {
                if (it == null) continue;
                inv.setItem(slot, it);
                slot++;
                if ((slot + 1) % 9 == 0) slot += 2;
            }
        }
        inv.setItem(45, buildNavIcon(Material.ARROW, "Voltar ao Menu", "nav_back", NamedTextColor.RED));
        player.openInventory(inv);
    }

    public void openVampireItemsPage(Player player) {
        Inventory inv = Bukkit.createInventory(null, SLOTS,
                Component.text("JGlims ", NamedTextColor.GOLD).decorate(TextDecoration.BOLD)
                        .append(Component.text("| Itens de Vampiro", NamedTextColor.DARK_RED)));
        com.jglims.plugin.vampire.VampireManager vm = plugin.getVampireManager();
        if (vm != null) {
            inv.setItem(10, tagForMenu(vm.createVampireBlood(),   "give_vampire", "blood"));
            inv.setItem(11, tagForMenu(vm.createVampireEssence(), "give_vampire", "essence"));
            inv.setItem(12, tagForMenu(vm.createVampireEvolver(), "give_vampire", "evolver"));
            inv.setItem(13, tagForMenu(vm.createSuperBlood(),     "give_vampire", "super_blood"));
            inv.setItem(14, tagForMenu(vm.createVampireRing(),    "give_vampire", "ring"));
        }
        inv.setItem(45, buildNavIcon(Material.ARROW, "Voltar ao Menu", "nav_back", NamedTextColor.RED));
        player.openInventory(inv);
    }

    public void openMagicItemsPage(Player player) {
        Inventory inv = Bukkit.createInventory(null, SLOTS,
                Component.text("JGlims ", NamedTextColor.GOLD).decorate(TextDecoration.BOLD)
                        .append(Component.text("| Itens Magicos", NamedTextColor.LIGHT_PURPLE)));
        com.jglims.plugin.magic.MagicItemManager mim = plugin.getMagicItemManager();
        if (mim != null) {
            inv.setItem(13, tagForMenu(mim.createWandOfWands(), "give_magic", "wand_of_wands"));
        }
        inv.setItem(45, buildNavIcon(Material.ARROW, "Voltar ao Menu", "nav_back", NamedTextColor.RED));
        player.openInventory(inv);
    }

    // ═══════════════════════════════════════════════════════════════
    //  GIVE HELPERS — look up the raw item from the manager and give it
    // ═══════════════════════════════════════════════════════════════

    private void giveCraftedItem(Player player, String id) {
        if (id == null) return;
        com.jglims.plugin.crafting.CraftedItemManager cim = plugin.getCraftedItemManager();
        if (cim == null) return;
        ItemStack item = switch (id) {
            case "blood_chalice"        -> cim.createBloodChalice();
            case "fang_dagger"          -> cim.createFangDagger();
            case "lunar_blade"          -> cim.createLunarBlade();
            case "raptor_gauntlet"      -> cim.createRaptorGauntlet();
            case "shark_tooth_necklace" -> cim.createSharkToothNecklace();
            case "tremor_shield"        -> cim.createTremorShield();
            case "void_scepter"         -> cim.createVoidScepter();
            case "soul_lantern_item"    -> cim.createSoulLanternItem();
            case "dinosaur_bone_bow"    -> cim.createDinosaurBoneBow();
            case "crimson_elixir"       -> cim.createCrimsonElixir();
            case "nightwalker_cloak"    -> cim.createNightwalkerCloak();
            case "troll_helmet"         -> cim.createTrollHelmet();
            case "troll_chestplate"     -> cim.createTrollChestplate();
            case "troll_leggings"       -> cim.createTrollLeggings();
            case "troll_boots"          -> cim.createTrollBoots();
            default -> null;
        };
        if (item != null) {
            player.getInventory().addItem(item);
            player.sendMessage(Component.text("Recebeu item craftado!", NamedTextColor.AQUA));
        }
    }

    private void giveDropItem(Player player, String id) {
        if (id == null) return;
        com.jglims.plugin.custommobs.DropItemManager dim = plugin.getDropItemManager();
        if (dim == null) return;
        ItemStack item = switch (id) {
            case "troll_hide"       -> dim.createTrollHide(4);
            case "lunar_fragment"   -> dim.createLunarFragment(4);
            case "lunar_beast_hide" -> dim.createLunarBeastHide(4);
            case "shark_tooth"      -> dim.createSharkTooth(4);
            case "bear_claw"        -> dim.createBearClaw(4);
            case "raptor_claw"      -> dim.createRaptorClaw(4);
            case "basilisk_fang"    -> dim.createBasiliskFang(4);
            case "dark_essence"     -> dim.createDarkEssence(4);
            case "void_essence"     -> dim.createVoidEssence(4);
            case "soul_fragment"    -> dim.createSoulFragment(4);
            case "tremor_scale"     -> dim.createTremorScale(4);
            case "horn"             -> dim.createHorn(4);
            case "spinosaurus_sail" -> dim.createSpinosaurusSail(4);
            default -> null;
        };
        if (item != null) {
            player.getInventory().addItem(item);
            player.sendMessage(Component.text("Recebeu drop item! (x4)", NamedTextColor.GRAY));
        }
    }

    private void giveVampireItem(Player player, String id) {
        if (id == null) return;
        com.jglims.plugin.vampire.VampireManager vm = plugin.getVampireManager();
        if (vm == null) return;
        ItemStack item = switch (id) {
            case "blood"       -> vm.createVampireBlood();
            case "essence"     -> vm.createVampireEssence();
            case "evolver"     -> vm.createVampireEvolver();
            case "super_blood" -> vm.createSuperBlood();
            case "ring"        -> vm.createVampireRing();
            default -> null;
        };
        if (item != null) {
            player.getInventory().addItem(item);
            player.sendMessage(Component.text("Recebeu item de vampiro!", NamedTextColor.DARK_RED));
        }
    }

    private void giveMagicItem(Player player, String id) {
        if (id == null) return;
        com.jglims.plugin.magic.MagicItemManager mim = plugin.getMagicItemManager();
        if (mim == null) return;
        ItemStack item = switch (id) {
            case "wand_of_wands" -> mim.createWandOfWands();
            default -> null;
        };
        if (item != null) {
            player.getInventory().addItem(item);
            player.sendMessage(Component.text("Recebeu item magico!", NamedTextColor.LIGHT_PURPLE));
        }
    }
}
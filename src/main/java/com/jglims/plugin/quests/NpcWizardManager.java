package com.jglims.plugin.quests;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.legendary.LegendaryTier;
import com.jglims.plugin.legendary.LegendaryWeapon;
import com.jglims.plugin.legendary.LegendaryWeaponManager;
import com.jglims.plugin.powerups.PowerUpManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * NPC Wizard Manager — A mysterious traveling wizard that sells exclusive items.
 * Spawns near random players every ~30 minutes and stays for 10 minutes.
 * Opens a custom shop GUI when right-clicked.
 */
public class NpcWizardManager implements Listener {

    private final JGlimsPlugin plugin;
    private final Random random = new Random();

    private final NamespacedKey KEY_WIZARD_NPC;
    private final NamespacedKey KEY_WIZARD_SHOP_ITEM;

    private UUID activeWizardUUID = null;

    private static final String SHOP_TITLE = "\u2728 Arcane Emporium \u2728";

    // Shop item definitions
    private enum ShopItem {
        HEART_CRYSTAL("Heart Crystal", Material.RED_DYE, TextColor.color(255, 50, 50), 16, Material.DIAMOND, 4, Material.EMERALD),
        SOUL_FRAGMENT("Soul Fragment", Material.PURPLE_DYE, TextColor.color(160, 50, 255), 8, Material.DIAMOND, 2, Material.ECHO_SHARD),
        PHOENIX_FEATHER("Phoenix Feather", Material.FEATHER, TextColor.color(255, 160, 30), 32, Material.DIAMOND, 1, Material.NETHER_STAR),
        TITAN_RESOLVE("Titan's Resolve", Material.IRON_NUGGET, TextColor.color(180, 180, 180), 24, Material.DIAMOND, 8, Material.NETHERITE_SCRAP),
        KEEP_INVENTORER("KeepInventorer", Material.ENDER_EYE, TextColor.color(0, 255, 200), 64, Material.DIAMOND, 4, Material.ECHO_SHARD),
        RARE_WEAPON("Random RARE Weapon", Material.DIAMOND_SWORD, NamedTextColor.GREEN, 48, Material.DIAMOND, 0, null),
        EPIC_WEAPON("Random EPIC Weapon", Material.DIAMOND_SWORD, NamedTextColor.DARK_PURPLE, 64, Material.DIAMOND, 16, Material.EMERALD);

        final String name;
        final Material icon;
        final TextColor color;
        final int primaryCost;
        final Material primaryMaterial;
        final int secondaryCost;
        final Material secondaryMaterial;

        ShopItem(String name, Material icon, TextColor color, int primaryCost, Material primaryMaterial, int secondaryCost, Material secondaryMaterial) {
            this.name = name; this.icon = icon; this.color = color;
            this.primaryCost = primaryCost; this.primaryMaterial = primaryMaterial;
            this.secondaryCost = secondaryCost; this.secondaryMaterial = secondaryMaterial;
        }
    }

    public NpcWizardManager(JGlimsPlugin plugin) {
        this.plugin = plugin;
        KEY_WIZARD_NPC = new NamespacedKey(plugin, "wizard_npc");
        KEY_WIZARD_SHOP_ITEM = new NamespacedKey(plugin, "wizard_shop_item");
    }

    public void startScheduler() {
        new BukkitRunnable() {
            @Override
            public void run() {
                // Clean up dead wizard
                if (activeWizardUUID != null) {
                    Entity e = plugin.getServer().getEntity(activeWizardUUID);
                    if (e == null || e.isDead() || !e.isValid()) activeWizardUUID = null;
                }
                if (activeWizardUUID != null) return; // Only one wizard at a time
                List<Player> players = new ArrayList<>(plugin.getServer().getOnlinePlayers());
                if (players.isEmpty()) return;
                if (random.nextDouble() < 0.50) {
                    Player target = players.get(random.nextInt(players.size()));
                    if (target.getWorld().getEnvironment() == World.Environment.NORMAL) {
                        spawnWizard(target);
                    }
                }
            }
        }.runTaskTimer(plugin, 12000L, 36000L); // First at 10min, then every 30min

        plugin.getLogger().info("NPC Wizard scheduler started.");
    }

    private void spawnWizard(Player nearPlayer) {
        Location loc = nearPlayer.getLocation().add(random.nextInt(16) - 8, 0, random.nextInt(16) - 8);
        loc.setY(loc.getWorld().getHighestBlockYAt(loc.getBlockX(), loc.getBlockZ()) + 1);

        WanderingTrader wizard = loc.getWorld().spawn(loc, WanderingTrader.class);
        wizard.customName(Component.text("Archmage Eldric", TextColor.color(100, 50, 200)).decorate(TextDecoration.BOLD));
        wizard.setCustomNameVisible(true);
        wizard.setAI(true);
        wizard.setInvulnerable(true);
        wizard.setPersistent(true);
        wizard.setRemoveWhenFarAway(false);
        wizard.setGlowing(true);
        wizard.getPersistentDataContainer().set(KEY_WIZARD_NPC, PersistentDataType.BYTE, (byte) 1);
        // Clear default trades
        wizard.setRecipes(new ArrayList<>());
        activeWizardUUID = wizard.getUniqueId();

        // Announce
        Component msg = Component.text("\u2728 ", TextColor.color(100, 50, 200))
            .append(Component.text("Archmage Eldric", TextColor.color(100, 50, 200)).decorate(TextDecoration.BOLD))
            .append(Component.text(" has appeared nearby!", NamedTextColor.LIGHT_PURPLE));
        for (Player p : loc.getNearbyPlayers(60)) {
            p.sendMessage(msg);
            p.playSound(p.getLocation(), Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1.0f, 1.0f);
        }

        // Ambient particles
        new BukkitRunnable() {
            @Override
            public void run() {
                if (wizard.isDead() || !wizard.isValid()) { cancel(); return; }
                wizard.getWorld().spawnParticle(Particle.ENCHANT, wizard.getLocation().add(0, 2, 0), 10, 0.5, 0.5, 0.5, 0.5);
            }
        }.runTaskTimer(plugin, 20L, 20L);

        // Despawn after 10 minutes
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!wizard.isDead() && wizard.isValid()) {
                    for (Player p : wizard.getLocation().getNearbyPlayers(40)) {
                        p.sendMessage(Component.text("\u2728 ", TextColor.color(100, 50, 200))
                            .append(Component.text("Archmage Eldric", TextColor.color(100, 50, 200)).decorate(TextDecoration.BOLD))
                            .append(Component.text(" vanishes in a puff of smoke...", NamedTextColor.GRAY)));
                    }
                    wizard.getWorld().spawnParticle(Particle.LARGE_SMOKE, wizard.getLocation(), 40, 0.5, 1, 0.5, 0.05);
                    wizard.getWorld().playSound(wizard.getLocation(), Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, 1.0f, 0.8f);
                    wizard.remove();
                    activeWizardUUID = null;
                }
            }
        }.runTaskLater(plugin, 12000L);

        plugin.getLogger().info("Archmage Eldric spawned near " + nearPlayer.getName());
    }

    // ══════════════════════════════════════════════════════════════════
    // SHOP GUI
    // ══════════════════════════════════════════════════════════════════

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        if (!entity.getPersistentDataContainer().has(KEY_WIZARD_NPC, PersistentDataType.BYTE)) return;
        event.setCancelled(true);
        openShop(event.getPlayer());
    }

    private void openShop(Player player) {
        Inventory shop = Bukkit.createInventory(null, 27, Component.text(SHOP_TITLE, TextColor.color(100, 50, 200)).decorate(TextDecoration.BOLD));

        // Fill border with glass panes
        ItemStack border = new ItemStack(Material.PURPLE_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.displayName(Component.text(" "));
        border.setItemMeta(borderMeta);
        for (int i = 0; i < 27; i++) {
            if (i < 9 || i >= 18 || i % 9 == 0 || i % 9 == 8) shop.setItem(i, border);
        }

        // Place shop items in slots 10-16
        ShopItem[] items = ShopItem.values();
        for (int i = 0; i < items.length && i < 7; i++) {
            shop.setItem(10 + i, createShopIcon(items[i], i));
        }

        player.openInventory(shop);
        player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.0f);
    }

    private ItemStack createShopIcon(ShopItem item, int slot) {
        ItemStack icon = new ItemStack(item.icon);
        ItemMeta meta = icon.getItemMeta();
        meta.displayName(Component.text(item.name, item.color).decorate(TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text(""));
        lore.add(Component.text("Cost:", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("  " + item.primaryCost + "x " + formatMaterial(item.primaryMaterial), NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false));
        if (item.secondaryMaterial != null && item.secondaryCost > 0) {
            lore.add(Component.text("  " + item.secondaryCost + "x " + formatMaterial(item.secondaryMaterial), NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false));
        }
        lore.add(Component.text(""));
        lore.add(Component.text("Click to purchase!", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);
        meta.getPersistentDataContainer().set(KEY_WIZARD_SHOP_ITEM, PersistentDataType.INTEGER, slot);
        icon.setItemMeta(meta);
        return icon;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        Component title = event.getView().title();
        if (title == null) return;
        // Check if this is our wizard shop
        if (!net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(title).contains("Arcane Emporium")) return;
        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;
        if (!clicked.getItemMeta().getPersistentDataContainer().has(KEY_WIZARD_SHOP_ITEM, PersistentDataType.INTEGER)) return;

        int slot = clicked.getItemMeta().getPersistentDataContainer().get(KEY_WIZARD_SHOP_ITEM, PersistentDataType.INTEGER);
        ShopItem[] items = ShopItem.values();
        if (slot < 0 || slot >= items.length) return;

        ShopItem item = items[slot];
        if (!hasPayment(player, item)) {
            player.sendMessage(Component.text("You don't have enough materials!", NamedTextColor.RED));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        takePayment(player, item);
        giveShopItem(player, item);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        player.getWorld().spawnParticle(Particle.ENCHANT, player.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.5);
    }

    private boolean hasPayment(Player player, ShopItem item) {
        if (!player.getInventory().contains(item.primaryMaterial, item.primaryCost)) return false;
        if (item.secondaryMaterial != null && item.secondaryCost > 0) {
            if (!player.getInventory().contains(item.secondaryMaterial, item.secondaryCost)) return false;
        }
        return true;
    }

    private void takePayment(Player player, ShopItem item) {
        removeItems(player, item.primaryMaterial, item.primaryCost);
        if (item.secondaryMaterial != null && item.secondaryCost > 0) {
            removeItems(player, item.secondaryMaterial, item.secondaryCost);
        }
    }

    private void removeItems(Player player, Material mat, int amount) {
        int remaining = amount;
        for (int i = 0; i < player.getInventory().getSize() && remaining > 0; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack == null || stack.getType() != mat) continue;
            // Don't take custom items (check for display name)
            if (stack.hasItemMeta() && stack.getItemMeta().hasDisplayName()) continue;
            int take = Math.min(stack.getAmount(), remaining);
            stack.setAmount(stack.getAmount() - take);
            remaining -= take;
        }
    }

    private void giveShopItem(Player player, ShopItem item) {
        PowerUpManager pum = plugin.getPowerUpManager();
        LegendaryWeaponManager wm = plugin.getLegendaryWeaponManager();
        ItemStack reward;

        switch (item) {
            case HEART_CRYSTAL -> reward = pum.createHeartCrystal();
            case SOUL_FRAGMENT -> reward = pum.createSoulFragment();
            case PHOENIX_FEATHER -> reward = pum.createPhoenixFeather();
            case TITAN_RESOLVE -> reward = pum.createTitanResolve();
            case KEEP_INVENTORER -> reward = pum.createKeepInventorer();
            case RARE_WEAPON -> {
                LegendaryWeapon[] pool = LegendaryWeapon.byTier(LegendaryTier.RARE);
                if (pool.length == 0) return;
                LegendaryWeapon w = pool[random.nextInt(pool.length)];
                reward = wm.createWeapon(w);
                if (reward != null) {
                    player.sendMessage(Component.text("  You received: ", NamedTextColor.GREEN)
                        .append(Component.text(w.getDisplayName(), LegendaryTier.RARE.getColor()).decorate(TextDecoration.BOLD)));
                }
            }
            case EPIC_WEAPON -> {
                LegendaryWeapon[] pool = LegendaryWeapon.byTier(LegendaryTier.EPIC);
                if (pool.length == 0) return;
                LegendaryWeapon w = pool[random.nextInt(pool.length)];
                reward = wm.createWeapon(w);
                if (reward != null) {
                    player.sendMessage(Component.text("  You received: ", NamedTextColor.GREEN)
                        .append(Component.text(w.getDisplayName(), LegendaryTier.EPIC.getColor()).decorate(TextDecoration.BOLD)));
                }
            }
            default -> { return; }
        }

        if (reward != null) {
            player.getInventory().addItem(reward);
            player.sendMessage(Component.text("Purchase successful!", NamedTextColor.GREEN));
        }
    }

    private String formatMaterial(Material mat) {
        if (mat == null) return "";
        return mat.name().toLowerCase().replace("_", " ");
    }

    public boolean isWizardAlive() { return activeWizardUUID != null; }
    public NamespacedKey getKeyWizardNpc() { return KEY_WIZARD_NPC; }
}
package com.jglims.plugin.custommobs;

import com.jglims.plugin.JGlimsPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * Abstract base for NPC-type custom mobs. Adds trade GUI, custom trade definitions,
 * dialogue system, quest delivery, structure-bound spawning, persistence, and name display.
 * <p>
 * NPCs are invulnerable, never despawn, and do not attack.
 */
public abstract class CustomNpcEntity extends CustomMobEntity {

    /**
     * A single trade offered by this NPC.
     */
    public record NpcTrade(
            ItemStack costItem1,
            ItemStack costItem2,
            ItemStack result,
            int maxUses,
            int currentUses
    ) {
        /**
         * Creates a trade with a single cost item.
         */
        public NpcTrade(ItemStack cost, ItemStack result, int maxUses) {
            this(cost, null, result, maxUses, 0);
        }
    }

    /** All trades this NPC offers. Refreshed periodically. */
    protected final List<NpcTrade> trades = new ArrayList<>();

    /** Dialogue lines shown on first interaction. */
    protected final List<Component> dialogueLines = new ArrayList<>();

    /** Set of player UUIDs who have seen the introduction dialogue. */
    protected final Set<UUID> dialogueShown = new HashSet<>();

    /** How often trades refresh (in Minecraft days, 0 = never). */
    protected int tradeRefreshDays = 3;

    /** The Minecraft day when trades were last refreshed. */
    protected long lastTradeRefreshDay = -1;

    protected CustomNpcEntity(JGlimsPlugin plugin, CustomMobType mobType) {
        super(plugin, mobType);
    }

    // ── Lifecycle ──────────────────────────────────────────────────────

    @Override
    protected void configureHitboxEntity() {
        super.configureHitboxEntity();
        // NPCs are invulnerable and never despawn
        hitboxEntity.setInvulnerable(true);
        hitboxEntity.setPersistent(true);
        hitboxEntity.setRemoveWhenFarAway(false);
        hitboxEntity.setAI(false);
        hitboxEntity.setSilent(true);

        // Custom name with NPC color
        hitboxEntity.customName(
                Component.text(mobType.getDisplayName(), NamedTextColor.GOLD)
                        .decorate(TextDecoration.BOLD));
        hitboxEntity.setCustomNameVisible(true);
    }

    @Override
    protected void onSpawn() {
        initializeTrades();
    }

    @Override
    protected void onTick() {
        // Check trade refresh
        if (tradeRefreshDays > 0 && hitboxEntity != null) {
            long currentDay = hitboxEntity.getWorld().getFullTime() / 24000L;
            if (lastTradeRefreshDay < 0) {
                lastTradeRefreshDay = currentDay;
            } else if (currentDay - lastTradeRefreshDay >= tradeRefreshDays) {
                lastTradeRefreshDay = currentDay;
                refreshTrades();
            }
        }
    }

    @Override
    protected long getTickRate() {
        return 100L; // NPCs tick slowly — every 5 seconds
    }

    // ── Interaction ────────────────────────────────────────────────────

    @Override
    public void onInteract(Player player) {
        // Show dialogue on first interaction
        if (!dialogueShown.contains(player.getUniqueId()) && !dialogueLines.isEmpty()) {
            showDialogue(player);
            dialogueShown.add(player.getUniqueId());
            return;
        }
        // Open trade GUI
        openTradeGui(player);
    }

    /**
     * Shows the introduction dialogue to a player.
     *
     * @param player the player to show dialogue to
     */
    protected void showDialogue(Player player) {
        player.sendMessage(Component.text(""));
        player.sendMessage(Component.text("── " + mobType.getDisplayName() + " ──",
                NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
        for (Component line : dialogueLines) {
            player.sendMessage(line);
        }
        player.sendMessage(Component.text(""));
        player.sendMessage(Component.text("[Right-click again to trade]", NamedTextColor.YELLOW));
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_AMBIENT, 1.0f, 1.0f);
    }

    /**
     * Opens the NPC trade GUI. Uses a chest-based inventory where cost items
     * are displayed alongside result items, styled like a villager trade menu.
     *
     * @param player the player opening the trade GUI
     */
    protected void openTradeGui(Player player) {
        if (trades.isEmpty()) {
            player.sendMessage(Component.text(mobType.getDisplayName()
                    + " has nothing to trade right now.", NamedTextColor.YELLOW));
            return;
        }

        int rows = Math.min(6, (int) Math.ceil(trades.size() / 3.0) + 1);
        Inventory gui = Bukkit.createInventory(null, rows * 9,
                Component.text(mobType.getDisplayName() + " — Trades"));

        int slot = 0;
        for (int i = 0; i < trades.size() && slot < gui.getSize() - 2; i++) {
            NpcTrade trade = trades.get(i);
            if (trade.currentUses() >= trade.maxUses()) continue;

            // Cost item 1
            gui.setItem(slot, createTradeDisplayItem(trade.costItem1(), true));
            slot++;

            // Cost item 2 (or filler)
            if (trade.costItem2() != null) {
                gui.setItem(slot, createTradeDisplayItem(trade.costItem2(), true));
            } else {
                gui.setItem(slot, createFillerItem());
            }
            slot++;

            // Result item
            gui.setItem(slot, createTradeDisplayItem(trade.result(), false));
            slot++;
        }

        player.openInventory(gui);
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_TRADE, 1.0f, 1.0f);
    }

    /**
     * Creates a display item for the trade GUI with appropriate lore.
     */
    private ItemStack createTradeDisplayItem(ItemStack original, boolean isCost) {
        if (original == null) return createFillerItem();
        ItemStack display = original.clone();
        ItemMeta meta = display.getItemMeta();
        if (meta != null) {
            List<Component> lore = meta.lore() != null ? new ArrayList<>(meta.lore()) : new ArrayList<>();
            lore.add(Component.text(""));
            lore.add(isCost
                    ? Component.text("Cost", NamedTextColor.RED)
                    : Component.text("You receive", NamedTextColor.GREEN));
            meta.lore(lore);
            display.setItemMeta(meta);
        }
        return display;
    }

    /**
     * Creates a gray stained glass pane filler for empty trade slots.
     */
    private ItemStack createFillerItem() {
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(" "));
            filler.setItemMeta(meta);
        }
        return filler;
    }

    // ── Trade Management ───────────────────────────────────────────────

    /**
     * Override to define the initial trades for this NPC.
     * Called once on spawn.
     */
    protected abstract void initializeTrades();

    /**
     * Called when trades should be refreshed. Default behavior
     * clears and reinitializes all trades.
     */
    protected void refreshTrades() {
        trades.clear();
        initializeTrades();
        plugin.getLogger().info("[NPC] " + mobType.getDisplayName() + " trades refreshed.");
    }

    /**
     * Adds a trade to this NPC.
     *
     * @param cost1  primary cost item
     * @param cost2  secondary cost item (nullable)
     * @param result the result item
     * @param maxUses maximum times this trade can be used before refresh
     */
    protected void addTrade(ItemStack cost1, ItemStack cost2, ItemStack result, int maxUses) {
        trades.add(new NpcTrade(cost1, cost2, result, maxUses, 0));
    }

    /**
     * Adds a trade with a single cost item.
     */
    protected void addTrade(ItemStack cost, ItemStack result, int maxUses) {
        trades.add(new NpcTrade(cost, result, maxUses));
    }
}

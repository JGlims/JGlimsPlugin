package com.jglims.plugin.utility;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.config.ConfigManager;

public class LootBoosterListener implements Listener {

    private final JGlimsPlugin plugin;
    private final ConfigManager config;

    // Pool of enchantments for random book generation
    private static final Enchantment[] BOOK_ENCHANT_POOL = {
        Enchantment.SHARPNESS, Enchantment.PROTECTION, Enchantment.EFFICIENCY,
        Enchantment.UNBREAKING, Enchantment.POWER, Enchantment.FORTUNE,
        Enchantment.SILK_TOUCH, Enchantment.MENDING, Enchantment.LOOTING,
        Enchantment.FEATHER_FALLING, Enchantment.FIRE_ASPECT, Enchantment.KNOCKBACK,
        Enchantment.RESPIRATION, Enchantment.AQUA_AFFINITY, Enchantment.THORNS,
        Enchantment.FIRE_PROTECTION, Enchantment.BLAST_PROTECTION,
        Enchantment.PROJECTILE_PROTECTION, Enchantment.DEPTH_STRIDER,
        Enchantment.FROST_WALKER, Enchantment.SWEEPING_EDGE,
        Enchantment.INFINITY, Enchantment.FLAME, Enchantment.PUNCH,
        Enchantment.QUICK_CHARGE, Enchantment.PIERCING, Enchantment.MULTISHOT,
        Enchantment.CHANNELING, Enchantment.RIPTIDE, Enchantment.LOYALTY,
        Enchantment.IMPALING
    };

    public LootBoosterListener(JGlimsPlugin plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
    }

    // ========================================================================
    // LOOT GENERATE — Guaranteed enchanted book in chests
    // Also boosts Echo Shard chance in Ancient City loot
    // ========================================================================
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onLootGenerate(LootGenerateEvent event) {
        if (!config.isLootBoosterEnabled()) return;

        List<ItemStack> loot = event.getLoot();

        // Guaranteed enchanted book in every generated chest
        if (config.isChestEnchantedBook()) {
            boolean hasBook = false;
            for (ItemStack item : loot) {
                if (item != null && item.getType() == Material.ENCHANTED_BOOK) {
                    hasBook = true;
                    break;
                }
            }
            if (!hasBook) {
                loot.add(createRandomEnchantedBook());
            }
        }

        // Boost Echo Shard chance (check if loot table is ancient city)
        String lootTableKey = event.getLootTable().getKey().getKey();
        if (lootTableKey.contains("ancient_city")) {
            boolean hasEchoShard = false;
            for (ItemStack item : loot) {
                if (item != null && item.getType() == Material.ECHO_SHARD) {
                    hasEchoShard = true;
                    break;
                }
            }
            if (!hasEchoShard && ThreadLocalRandom.current().nextDouble() < config.getEchoShardChance()) {
                int amount = ThreadLocalRandom.current().nextInt(1, 4); // 1-3
                loot.add(new ItemStack(Material.ECHO_SHARD, amount));
            }
        }
    }

    private ItemStack createRandomEnchantedBook() {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK, 1);
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) book.getItemMeta();
        if (meta != null) {
            Enchantment enchant = BOOK_ENCHANT_POOL[
                ThreadLocalRandom.current().nextInt(BOOK_ENCHANT_POOL.length)];
            int maxLevel = enchant.getMaxLevel();
            int level = ThreadLocalRandom.current().nextInt(1, maxLevel + 1);
            meta.addStoredEnchant(enchant, level, false);
            book.setItemMeta(meta);
        }
        return book;
    }
}

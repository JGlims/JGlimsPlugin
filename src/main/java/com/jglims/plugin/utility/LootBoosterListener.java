package com.jglims.plugin.utility;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.config.ConfigManager;
import com.jglims.plugin.enchantments.CustomEnchantManager;
import com.jglims.plugin.enchantments.EnchantmentType;
import com.jglims.plugin.mobs.KingMobManager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class LootBoosterListener implements Listener {

    private final JGlimsPlugin plugin;
    private final ConfigManager config;

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

    // Custom enchantments that can drop as books (exclude Soulbound and Best Buddies)
    private static final EnchantmentType[] CUSTOM_ENCHANT_POOL;

    static {
        EnchantmentType[] all = EnchantmentType.values();
        java.util.List<EnchantmentType> filtered = new java.util.ArrayList<>();
        for (EnchantmentType type : all) {
            if (type != EnchantmentType.SOULBOUND && type != EnchantmentType.BEST_BUDDIES) {
                filtered.add(type);
            }
        }
        CUSTOM_ENCHANT_POOL = filtered.toArray(new EnchantmentType[0]);
    }

    public LootBoosterListener(JGlimsPlugin plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
    }

    // ========================================================================
    // CHEST LOOT — Guaranteed enchanted book + Echo Shard boost
    // ========================================================================
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onLootGenerate(LootGenerateEvent event) {
        if (!config.isLootBoosterEnabled()) return;

        List<ItemStack> loot = event.getLoot();

        if (config.isChestEnchantedBook()) {
            boolean hasBook = false;
            for (ItemStack item : loot) {
                if (item != null && item.getType() == Material.ENCHANTED_BOOK) {
                    hasBook = true;
                    break;
                }
            }
            if (!hasBook) {
                loot.add(createRandomVanillaEnchantedBook());
            }
        }

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
                int amount = ThreadLocalRandom.current().nextInt(1, 4);
                loot.add(new ItemStack(Material.ECHO_SHARD, amount));
            }
        }
    }

    // ========================================================================
    // MOB DEATH — Enchanted book drops (NEW v1.1.0)
    // ========================================================================
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        if (!config.isMobBookDropsEnabled()) return;

        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Monster) && !KingMobManager.isSpecialMob(entity)) return;

        Player killer = entity.getKiller();
        int lootingLevel = 0;
        if (killer != null) {
            ItemStack weapon = killer.getInventory().getItemInMainHand();
            lootingLevel = weapon.getEnchantmentLevel(Enchantment.LOOTING);
        }

        // Check if boss/special mob
        boolean isSpecial = KingMobManager.isSpecialMob(entity)
            || entity instanceof EnderDragon || entity instanceof Wither
            || entity instanceof Warden || entity instanceof ElderGuardian;

        // Check if this is a King mob
        KingMobManager kingManager = plugin.getKingMobManager();
        if (kingManager != null && kingManager.isKingMob(entity)) {
            isSpecial = true;
        }

        if (isSpecial) {
            // 15% base + 5% per looting level to drop custom enchantment book
            double chance = config.getBossCustomBookChance()
                + (lootingLevel * config.getLootingBonusBoss());
            if (ThreadLocalRandom.current().nextDouble() < chance) {
                event.getDrops().add(createRandomCustomEnchantedBook());
            }
        }

        // All hostile mobs: 5% base + 2% per looting level for vanilla book
        double regularChance = config.getHostileMobBookChance()
            + (lootingLevel * config.getLootingBonusRegular());
        if (ThreadLocalRandom.current().nextDouble() < regularChance) {
            event.getDrops().add(createRandomVanillaEnchantedBook());
        }
    }

    private ItemStack createRandomVanillaEnchantedBook() {
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

    private ItemStack createRandomCustomEnchantedBook() {
        EnchantmentType enchType = CUSTOM_ENCHANT_POOL[
            ThreadLocalRandom.current().nextInt(CUSTOM_ENCHANT_POOL.length)];
        int level = ThreadLocalRandom.current().nextInt(1, enchType.getMaxLevel() + 1);

        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK, 1);
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) book.getItemMeta();
        if (meta == null) return book;

        CustomEnchantManager enchantManager = plugin.getEnchantManager();
        meta.getPersistentDataContainer().set(
            enchantManager.getKey(enchType), PersistentDataType.INTEGER, level);

        String name = formatEnchantName(enchType.name()) + " " + toRoman(level);
        meta.displayName(Component.text(name, NamedTextColor.LIGHT_PURPLE)
            .decoration(TextDecoration.ITALIC, false));

        java.util.ArrayList<Component> lore = new java.util.ArrayList<>();
        lore.add(Component.text("Custom Enchantment", NamedTextColor.DARK_PURPLE)
            .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Apply to item in anvil", NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);

        book.setItemMeta(meta);
        return book;
    }

    private String formatEnchantName(String raw) {
        String name = raw.replace('_', ' ');
        StringBuilder sb = new StringBuilder();
        boolean cap = true;
        for (char c : name.toCharArray()) {
            if (c == ' ') { sb.append(' '); cap = true; }
            else if (cap) { sb.append(Character.toUpperCase(c)); cap = false; }
            else sb.append(Character.toLowerCase(c));
        }
        return sb.toString();
    }

    private String toRoman(int level) {
        return switch (level) {
            case 1 -> "I"; case 2 -> "II"; case 3 -> "III";
            case 4 -> "IV"; case 5 -> "V";
            default -> String.valueOf(level);
        };
    }
}

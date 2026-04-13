package com.jglims.plugin.legendary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.ElderGuardian;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Warden;
import org.bukkit.entity.Wither;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.loot.LootTable;
import org.bukkit.persistence.PersistentDataType;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.abyss.AbyssDimensionManager;
import com.jglims.plugin.enchantments.CustomEnchantManager;
import com.jglims.plugin.enchantments.EnchantmentType;
import com.jglims.plugin.mobs.BloodMoonManager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * LegendaryLootListener - boss drops and structure chest legendary injection.
 * v3.2.0 Phase 10 - Enhanced loot: more legendary weapons, custom enchant books in
 * structure chests, boss drops, dragon death chest, and blood moon kings.
 */
public class LegendaryLootListener implements Listener {

    private final JGlimsPlugin plugin;
    private final LegendaryWeaponManager weaponManager;
    private final BloodMoonManager bloodMoonManager;

    // Enchantments that can appear as book drops from bosses and chests
    private static final EnchantmentType[] BOSS_BOOK_POOL = {
            EnchantmentType.VAMPIRISM, EnchantmentType.BERSERKER, EnchantmentType.BLEED,
            EnchantmentType.CHAIN_LIGHTNING, EnchantmentType.LIFESTEAL, EnchantmentType.CLEAVE,
            EnchantmentType.GUILLOTINE, EnchantmentType.WRATH, EnchantmentType.FROSTBITE_BLADE,
            EnchantmentType.VENOMSTRIKE, EnchantmentType.THUNDERLORD, EnchantmentType.SOULBOUND,
            EnchantmentType.FORTIFICATION, EnchantmentType.DEFLECTION, EnchantmentType.DODGE,
            EnchantmentType.SEISMIC_SLAM, EnchantmentType.MOMENTUM, EnchantmentType.FROSTBITE,
            EnchantmentType.SOUL_REAP, EnchantmentType.WITHER_TOUCH, EnchantmentType.TSUNAMI,
            EnchantmentType.EXPLOSIVE_ARROW, EnchantmentType.HOMING, EnchantmentType.SNIPER
    };

    // ── BOSS DROP POOLS ──

    private static final LegendaryWeapon[] ELDER_GUARDIAN_POOL = {
            LegendaryWeapon.OCEANS_RAGE, LegendaryWeapon.NEPTUNES_FANG,
            LegendaryWeapon.TIDECALLER, LegendaryWeapon.STORMFORK
    };

    private static final LegendaryWeapon[] WITHER_POOL = {
            LegendaryWeapon.BERSERKERS_GREATAXE, LegendaryWeapon.BLACK_IRON_GREATSWORD,
            LegendaryWeapon.CALAMITY_BLADE, LegendaryWeapon.DEMONS_BLOOD_BLADE,
            LegendaryWeapon.GRAND_CLAYMORE, LegendaryWeapon.EMERALD_GREATCLEAVER
    };

    private static final LegendaryWeapon[] WARDEN_POOL = {
            LegendaryWeapon.TRUE_EXCALIBUR, LegendaryWeapon.REQUIEM_NINTH_ABYSS,
            LegendaryWeapon.GRAND_CLAYMORE, LegendaryWeapon.EMERALD_GREATCLEAVER,
            LegendaryWeapon.SOLSTICE, LegendaryWeapon.ZENITH, LegendaryWeapon.PHANTOMGUARD
    };

    private static final LegendaryWeapon[] DRAGON_DEATH_CHEST_POOL = {
            LegendaryWeapon.PHOENIXS_GRACE, LegendaryWeapon.TRUE_EXCALIBUR,
            LegendaryWeapon.REQUIEM_NINTH_ABYSS, LegendaryWeapon.ZENITH,
            LegendaryWeapon.PHANTOMGUARD, LegendaryWeapon.VALHAKYRA,
            LegendaryWeapon.DRAGON_SWORD, LegendaryWeapon.SOUL_COLLECTOR,
            LegendaryWeapon.NOCTURNE,
            LegendaryWeapon.DIVINE_AXE_RHITTA,
            LegendaryWeapon.EDGE_ASTRAL_PLANE,
            LegendaryWeapon.HEAVENLY_PARTISAN,
            LegendaryWeapon.MJOLNIR,
            LegendaryWeapon.RIVERS_OF_BLOOD
    };

    private static final LegendaryWeapon[] END_RIFT_POOL = {
            LegendaryWeapon.TENGENS_BLADE,
            LegendaryWeapon.SOUL_DEVOURER,
            LegendaryWeapon.STAR_EDGE,
            LegendaryWeapon.CREATION_SPLITTER,
            LegendaryWeapon.STOP_SIGN
    };

    private static final LegendaryWeapon[] BLOOD_MOON_RARE_POOL = {
            LegendaryWeapon.MURAMASA, LegendaryWeapon.MOONLIGHT, LegendaryWeapon.TALONBRAND
    };

    private static final LegendaryWeapon[] BLOOD_MOON_EPIC_POOL = {
            LegendaryWeapon.BERSERKERS_GREATAXE, LegendaryWeapon.BLACK_IRON_GREATSWORD,
            LegendaryWeapon.DEMONS_BLOOD_BLADE
    };

    // ── STRUCTURE LOOT (increased chances) ──

    private record StructureLoot(String lootTableContains, double chance, LegendaryWeapon[] pool) {}

    private static final StructureLoot[] STRUCTURE_LOOT = {
            new StructureLoot("end_city", 0.25, new LegendaryWeapon[]{
                    LegendaryWeapon.VALHAKYRA, LegendaryWeapon.PHANTOMGUARD,
                    LegendaryWeapon.ZENITH, LegendaryWeapon.DRAGON_SWORD,
                    LegendaryWeapon.SOUL_COLLECTOR, LegendaryWeapon.NOCTURNE,
                    LegendaryWeapon.YORU, LegendaryWeapon.FALLEN_GODS_SPEAR,
                    LegendaryWeapon.NATURE_SWORD, LegendaryWeapon.THOUSAND_DEMON_DAGGERS,
                    LegendaryWeapon.DRAGON_SLAYING_BLADE}),
            new StructureLoot("nether_bridge", 0.18, new LegendaryWeapon[]{
                    LegendaryWeapon.MURAMASA, LegendaryWeapon.ACIDIC_CLEAVER, LegendaryWeapon.FLAMBERGE}),
            new StructureLoot("bastion", 0.20, new LegendaryWeapon[]{
                    LegendaryWeapon.ROYAL_CHAKRAM, LegendaryWeapon.WINDREAPER, LegendaryWeapon.TALONBRAND}),
            new StructureLoot("stronghold", 0.22, new LegendaryWeapon[]{
                    LegendaryWeapon.OCULUS, LegendaryWeapon.ANCIENT_GREATSLAB, LegendaryWeapon.GRAND_CLAYMORE}),
            new StructureLoot("simple_dungeon", 0.18, new LegendaryWeapon[]{
                    LegendaryWeapon.GRAVESCEPTER, LegendaryWeapon.GLOOMSTEEL_KATANA, LegendaryWeapon.SPIDER_FANG}),
            new StructureLoot("abandoned_mineshaft", 0.18, new LegendaryWeapon[]{
                    LegendaryWeapon.SPIDER_FANG, LegendaryWeapon.GLOOMSTEEL_KATANA, LegendaryWeapon.VENGEANCE}),
            new StructureLoot("desert_pyramid", 0.15, new LegendaryWeapon[]{
                    LegendaryWeapon.ANCIENT_GREATSLAB, LegendaryWeapon.DEMONSLAYER}),
            new StructureLoot("jungle_temple", 0.15, new LegendaryWeapon[]{
                    LegendaryWeapon.VIRIDIAN_CLEAVER, LegendaryWeapon.JADE_REAPER}),
            new StructureLoot("ocean_monument", 0.18, new LegendaryWeapon[]{
                    LegendaryWeapon.NEPTUNES_FANG, LegendaryWeapon.TIDECALLER, LegendaryWeapon.STORMFORK}),
            new StructureLoot("woodland_mansion", 0.20, new LegendaryWeapon[]{
                    LegendaryWeapon.LYCANBANE, LegendaryWeapon.VINDICATOR, LegendaryWeapon.DEMONSLAYER}),
            new StructureLoot("pillager_outpost", 0.18, new LegendaryWeapon[]{
                    LegendaryWeapon.CRESCENT_EDGE, LegendaryWeapon.VINDICATOR}),
            new StructureLoot("shipwreck", 0.15, new LegendaryWeapon[]{
                    LegendaryWeapon.NEPTUNES_FANG, LegendaryWeapon.STORMFORK}),
            new StructureLoot("buried_treasure", 0.22, new LegendaryWeapon[]{
                    LegendaryWeapon.TIDECALLER, LegendaryWeapon.AQUATIC_SACRED_BLADE}),
            new StructureLoot("ancient_city", 0.20, new LegendaryWeapon[]{
                    LegendaryWeapon.GRAVESCEPTER, LegendaryWeapon.GRAVECLEAVER, LegendaryWeapon.NOCTURNE}),
            new StructureLoot("trail_ruins", 0.15, new LegendaryWeapon[]{
                    LegendaryWeapon.AMETHYST_GREATBLADE, LegendaryWeapon.ANCIENT_GREATSLAB}),
            new StructureLoot("igloo", 0.25, new LegendaryWeapon[]{
                    LegendaryWeapon.CRYSTAL_FROSTBLADE}),
            new StructureLoot("ruined_portal", 0.12, new LegendaryWeapon[]{
                    LegendaryWeapon.FLAMBERGE, LegendaryWeapon.MOONLIGHT}),
            new StructureLoot("trial_chambers", 0.15, new LegendaryWeapon[]{
                    LegendaryWeapon.VENGEANCE, LegendaryWeapon.SPIDER_FANG, LegendaryWeapon.AMETHYST_SHURIKEN}),
            new StructureLoot("ocean_ruin", 0.12, new LegendaryWeapon[]{
                    LegendaryWeapon.STORMFORK, LegendaryWeapon.OCEANS_RAGE})
    };

    public LegendaryLootListener(JGlimsPlugin plugin, LegendaryWeaponManager weaponManager,
                                  BloodMoonManager bloodMoonManager) {
        this.plugin = plugin;
        this.weaponManager = weaponManager;
        this.bloodMoonManager = bloodMoonManager;
    }

    // ── STRUCTURE CHEST INJECTION (now also injects custom enchant books) ──

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onLootGenerate(LootGenerateEvent event) {
        LootTable lootTable = event.getLootTable();
        if (lootTable == null) return;
        String tableKey = lootTable.getKey().toString().toLowerCase();
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        List<ItemStack> loot = event.getLoot();
        for (StructureLoot sl : STRUCTURE_LOOT) {
            if (tableKey.contains(sl.lootTableContains)) {
                // Legendary weapon injection
                if (rand.nextDouble() < sl.chance) {
                    LegendaryWeapon weapon = sl.pool[rand.nextInt(sl.pool.length)];
                    ItemStack weaponItem = weaponManager.createWeapon(weapon);
                    if (weaponItem != null) {
                        loot.add(weaponItem);
                        plugin.getLogger().info("[" + weapon.getTier().getId() + "] "
                                + weapon.getDisplayName() + " injected into " + sl.lootTableContains + " chest!");
                    }
                }
                // Custom enchant book injection (20% base chance for any structure)
                if (rand.nextDouble() < 0.20) {
                    ItemStack book = createCustomEnchantBook(1, 3);
                    if (book != null) loot.add(book);
                }
            }
        }
    }

    // ── BOSS DEATH DROPS ──

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof EnderDragon) { handleDragonDeath(entity); return; }
        if (entity instanceof ElderGuardian) {
            dropFromPool(event, ELDER_GUARDIAN_POOL, 1, 2, "Elder Guardian");
            dropCustomBooks(event, 1, 2, 2, 4);
            return;
        }
        if (entity instanceof Warden) {
            dropFromPool(event, WARDEN_POOL, 1, 3, "Warden");
            dropCustomBooks(event, 2, 3, 3, 5);
            return;
        }
        if (entity instanceof Wither) {
            if (plugin.getEventManager().isEventBoss(entity)) return;
            dropFromPool(event, WITHER_POOL, 1, 2, "Wither");
            dropCustomBooks(event, 1, 3, 2, 5);
            return;
        }
        // Blood Moon King
        if (bloodMoonManager.isBloodMoonKing(entity)) {
            ThreadLocalRandom rand = ThreadLocalRandom.current();
            LegendaryWeapon rareWeapon = BLOOD_MOON_RARE_POOL[rand.nextInt(BLOOD_MOON_RARE_POOL.length)];
            ItemStack rareItem = weaponManager.createWeapon(rareWeapon);
            if (rareItem != null) { event.getDrops().add(rareItem); announceWeaponDrop(rareWeapon, "Blood Moon King", entity.getLocation()); }
            if (rand.nextDouble() < 0.25) {
                LegendaryWeapon epicWeapon = BLOOD_MOON_EPIC_POOL[rand.nextInt(BLOOD_MOON_EPIC_POOL.length)];
                ItemStack epicItem = weaponManager.createWeapon(epicWeapon);
                if (epicItem != null) { event.getDrops().add(epicItem); announceWeaponDrop(epicWeapon, "Blood Moon King", entity.getLocation()); }
            }
            // Blood Moon King also drops 1-2 books
            dropCustomBooks(event, 1, 2, 2, 4);
            // Guaranteed Werewolf Blood drop — this is currently the only
            // in-world source of the werewolf infection item.
            if (plugin.getWerewolfManager() != null) {
                event.getDrops().add(plugin.getWerewolfManager().createWerewolfBlood());
            }
        }
    }

    // ── DRAGON DEATH → CHEST + END RIFT TRIGGER ──

    private void handleDragonDeath(LivingEntity dragon) {
        handleDragonDeathChest(dragon);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            boolean riftOpened = plugin.getEventManager().tryTriggerEndRift();
            if (riftOpened) {
                plugin.getLogger().info("End Rift triggered after Ender Dragon death!");
            } else {
                plugin.getLogger().info("End Rift did not trigger this time (10% chance).");
            }
        }, 100L);
    }

    private void handleDragonDeathChest(LivingEntity dragon) {
        Location loc = dragon.getLocation();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Location chestLoc = new Location(loc.getWorld(), 0, loc.getWorld().getHighestBlockYAt(0, 0) + 1, 0);
            Block chestBlock = chestLoc.getBlock();
            chestBlock.setType(Material.CHEST);
            if (!(chestBlock.getState() instanceof Chest chest)) {
                plugin.getLogger().warning("Could not place Dragon death chest. Dropping items instead.");
                dropDragonLootAtLocation(loc);
                return;
            }
            ThreadLocalRandom rand = ThreadLocalRandom.current();
            int weaponCount = rand.nextInt(2, 4); // 2-3 weapons
            List<LegendaryWeapon> pool = new ArrayList<>(Arrays.asList(DRAGON_DEATH_CHEST_POOL));
            List<LegendaryWeapon> selected = new ArrayList<>();
            for (int i = 0; i < weaponCount && !pool.isEmpty(); i++) {
                selected.add(pool.remove(rand.nextInt(pool.size())));
            }
            int[] weaponSlots = {11, 13, 15};
            for (int i = 0; i < selected.size(); i++) {
                ItemStack weaponItem = weaponManager.createWeapon(selected.get(i));
                if (weaponItem != null) chest.getInventory().setItem(weaponSlots[i], weaponItem);
            }

            // Add 2-3 custom enchant books to the chest
            int bookCount = 2 + rand.nextInt(2);
            int[] bookSlots = {2, 4, 6, 8};
            for (int i = 0; i < bookCount && i < bookSlots.length; i++) {
                ItemStack book = createCustomEnchantBook(3, 5);
                if (book != null) chest.getInventory().setItem(bookSlots[i], book);
            }

            // VFX
            loc.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, chestLoc.clone().add(0.5, 1, 0.5), 100, 1, 2, 1, 0.05);
            loc.getWorld().spawnParticle(Particle.END_ROD, chestLoc.clone().add(0.5, 2, 0.5), 50, 0.5, 3, 0.5, 0.02);
            loc.getWorld().playSound(chestLoc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 2.0f, 1.0f);

            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage(Component.empty());
                p.sendMessage(Component.text("  \u2726 ", NamedTextColor.DARK_PURPLE)
                        .append(Component.text("THE ENDER DRAGON HAS FALLEN!", NamedTextColor.LIGHT_PURPLE).decorate(TextDecoration.BOLD))
                        .append(Component.text(" \u2726", NamedTextColor.DARK_PURPLE)));
                p.sendMessage(Component.text("  A chest of legendary weapons and enchanted tomes has appeared!", NamedTextColor.GOLD));
                p.sendMessage(Component.text("  Contents: ", NamedTextColor.GRAY)
                        .append(Component.text(selected.size() + " MYTHIC weapons + " + bookCount + " enchant books", NamedTextColor.GOLD).decorate(TextDecoration.BOLD)));
                for (LegendaryWeapon w : selected) {
                    p.sendMessage(Component.text("    \u25B6 ", NamedTextColor.DARK_GRAY)
                            .append(Component.text(w.getDisplayName(), w.getTier().getColor()).decorate(TextDecoration.BOLD))
                            .append(Component.text(" (DMG " + w.getBaseDamage() + ")", NamedTextColor.GRAY)));
                }
                p.sendMessage(Component.empty());
            }
            // Drop Abyssal Key
            AbyssDimensionManager adm = plugin.getAbyssDimensionManager();
            if (adm != null) {
                chest.getInventory().setItem(22, adm.createAbyssalKey());
                plugin.getLogger().info("Abyssal Key placed in Dragon death chest.");
                for (Player pk : Bukkit.getOnlinePlayers()) {
                    pk.sendMessage(Component.text("  \u2726 An ", NamedTextColor.GRAY)
                            .append(Component.text("Abyssal Key", net.kyori.adventure.text.format.TextColor.color(170, 0, 0)).decorate(TextDecoration.BOLD))
                            .append(Component.text(" was found in the chest!", NamedTextColor.GRAY)));
                }
            }
            plugin.getLogger().info("Dragon death chest placed with " + selected.size() + " MYTHIC weapons + " + bookCount + " books.");
        }, 5L);
    }

    private void dropDragonLootAtLocation(Location loc) {
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        int count = rand.nextInt(2, 4);
        List<LegendaryWeapon> pool = new ArrayList<>(Arrays.asList(DRAGON_DEATH_CHEST_POOL));
        for (int i = 0; i < count && !pool.isEmpty(); i++) {
            LegendaryWeapon weapon = pool.remove(rand.nextInt(pool.size()));
            ItemStack weaponItem = weaponManager.createWeapon(weapon);
            if (weaponItem != null) loc.getWorld().dropItemNaturally(loc, weaponItem);
        }
        // Also drop books if chest fails
        for (int i = 0; i < 2; i++) {
            ItemStack book = createCustomEnchantBook(3, 5);
            if (book != null) loc.getWorld().dropItemNaturally(loc, book);
        }
    }

    private void dropFromPool(EntityDeathEvent event, LegendaryWeapon[] pool, int min, int max, String bossName) {
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        int count = Math.min(rand.nextInt(min, max + 1), pool.length);
        List<LegendaryWeapon> available = new ArrayList<>(Arrays.asList(pool));
        for (int i = 0; i < count && !available.isEmpty(); i++) {
            int idx = rand.nextInt(available.size());
            LegendaryWeapon weapon = available.remove(idx);
            ItemStack weaponItem = weaponManager.createWeapon(weapon);
            if (weaponItem != null) { event.getDrops().add(weaponItem); announceWeaponDrop(weapon, bossName, event.getEntity().getLocation()); }
        }
    }

    /**
     * Drops custom enchant books alongside boss loot.
     */
    private void dropCustomBooks(EntityDeathEvent event, int minBooks, int maxBooks, int minLevel, int maxLevel) {
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        int count = minBooks + rand.nextInt(maxBooks - minBooks + 1);
        for (int i = 0; i < count; i++) {
            ItemStack book = createCustomEnchantBook(minLevel, maxLevel);
            if (book != null) event.getDrops().add(book);
        }
    }

    /**
     * Creates a custom enchanted book ItemStack with a random enchantment.
     */
    private ItemStack createCustomEnchantBook(int minLevel, int maxLevel) {
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        EnchantmentType enchant = BOSS_BOOK_POOL[rand.nextInt(BOSS_BOOK_POOL.length)];
        int level = Math.min(minLevel + rand.nextInt(maxLevel - minLevel + 1), enchant.getMaxLevel());

        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = book.getItemMeta();
        if (meta == null) return null;

        CustomEnchantManager cem = plugin.getEnchantManager();
        NamespacedKey key = cem.getKey(enchant);
        meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, level);

        String enchantName = enchant.name().replace('_', ' ');
        StringBuilder sb = new StringBuilder();
        for (String word : enchantName.split(" ")) {
            sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1).toLowerCase()).append(' ');
        }
        String displayName = sb.toString().trim();

        meta.displayName(Component.text(displayName + " " + toRoman(level), NamedTextColor.AQUA)
                .decorate(TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Custom Enchantment Book", NamedTextColor.LIGHT_PURPLE)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Apply on Anvil", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);

        book.setItemMeta(meta);
        return book;
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

    // ── ANNOUNCEMENTS ──

    private void announceWeaponDrop(LegendaryWeapon weapon, String source, Location loc) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(Component.text("\u2726 ", NamedTextColor.DARK_PURPLE)
                    .append(Component.text("[" + weapon.getTier().getId() + "] ", weapon.getTier().getColor()).decorate(TextDecoration.BOLD))
                    .append(Component.text(weapon.getDisplayName(), weapon.getTier().getColor()).decorate(TextDecoration.BOLD))
                    .append(Component.text(" dropped from ", NamedTextColor.GRAY))
                    .append(Component.text(source, NamedTextColor.RED).decorate(TextDecoration.BOLD))
                    .append(Component.text("!", NamedTextColor.GRAY)));
        }
        if (loc != null && loc.getWorld() != null) {
            loc.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, loc.clone().add(0, 1, 0), 30, 0.5, 1, 0.5, 0.2);
            loc.getWorld().playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 2.0f, 1.2f);
        }
        plugin.getLogger().info("[" + weapon.getTier().getId() + "] '" + weapon.getDisplayName() + "' dropped from " + source + "!");
    }
}

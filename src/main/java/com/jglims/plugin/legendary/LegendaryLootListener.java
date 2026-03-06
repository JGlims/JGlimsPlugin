package com.jglims.plugin.legendary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
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
import org.bukkit.loot.LootTable;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.mobs.BloodMoonManager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * LegendaryLootListener - boss drops and structure chest legendary injection.
 * v3.0.0 Phase 8c - Rewritten for 5-tier drop tables.
 */
public class LegendaryLootListener implements Listener {

    private final JGlimsPlugin plugin;
    private final LegendaryWeaponManager weaponManager;
    private final BloodMoonManager bloodMoonManager;

    private static final LegendaryWeapon[] ELDER_GUARDIAN_POOL = {
            LegendaryWeapon.OCEANS_RAGE
    };

    private static final LegendaryWeapon[] WITHER_POOL = {
            LegendaryWeapon.BERSERKERS_GREATAXE, LegendaryWeapon.BLACK_IRON_GREATSWORD,
            LegendaryWeapon.CALAMITY_BLADE, LegendaryWeapon.DEMONS_BLOOD_BLADE
    };

    private static final LegendaryWeapon[] WARDEN_POOL = {
            LegendaryWeapon.TRUE_EXCALIBUR, LegendaryWeapon.REQUIEM_NINTH_ABYSS,
            LegendaryWeapon.GRAND_CLAYMORE, LegendaryWeapon.EMERALD_GREATCLEAVER,
            LegendaryWeapon.SOLSTICE
    };

    private static final LegendaryWeapon[] DRAGON_DEATH_CHEST_POOL = {
            LegendaryWeapon.PHOENIXS_GRACE, LegendaryWeapon.TRUE_EXCALIBUR,
            LegendaryWeapon.REQUIEM_NINTH_ABYSS, LegendaryWeapon.ZENITH,
            LegendaryWeapon.PHANTOMGUARD, LegendaryWeapon.VALHAKYRA,
            LegendaryWeapon.DRAGON_SWORD, LegendaryWeapon.SOUL_COLLECTOR,
            LegendaryWeapon.NOCTURNE
    };

    private static final LegendaryWeapon[] BLOOD_MOON_RARE_POOL = {
            LegendaryWeapon.MURAMASA, LegendaryWeapon.MOONLIGHT, LegendaryWeapon.TALONBRAND
    };

    private static final LegendaryWeapon[] BLOOD_MOON_EPIC_POOL = {
            LegendaryWeapon.BERSERKERS_GREATAXE, LegendaryWeapon.BLACK_IRON_GREATSWORD,
            LegendaryWeapon.DEMONS_BLOOD_BLADE
    };

    private record StructureLoot(String lootTableContains, double chance, LegendaryWeapon[] pool) {}

    private static final StructureLoot[] STRUCTURE_LOOT = {
            new StructureLoot("end_city", 0.15, new LegendaryWeapon[]{
                    LegendaryWeapon.VALHAKYRA, LegendaryWeapon.PHANTOMGUARD,
                    LegendaryWeapon.ZENITH, LegendaryWeapon.DRAGON_SWORD,
                    LegendaryWeapon.SOUL_COLLECTOR, LegendaryWeapon.NOCTURNE}),
            new StructureLoot("nether_bridge", 0.12, new LegendaryWeapon[]{
                    LegendaryWeapon.MURAMASA, LegendaryWeapon.ACIDIC_CLEAVER}),
            new StructureLoot("bastion", 0.12, new LegendaryWeapon[]{
                    LegendaryWeapon.ROYAL_CHAKRAM, LegendaryWeapon.WINDREAPER}),
            new StructureLoot("stronghold", 0.15, new LegendaryWeapon[]{
                    LegendaryWeapon.OCULUS, LegendaryWeapon.ANCIENT_GREATSLAB}),
            new StructureLoot("simple_dungeon", 0.12, new LegendaryWeapon[]{
                    LegendaryWeapon.GRAVESCEPTER, LegendaryWeapon.GLOOMSTEEL_KATANA, LegendaryWeapon.SPIDER_FANG}),
            new StructureLoot("abandoned_mineshaft", 0.12, new LegendaryWeapon[]{
                    LegendaryWeapon.SPIDER_FANG, LegendaryWeapon.GLOOMSTEEL_KATANA}),
            new StructureLoot("desert_pyramid", 0.10, new LegendaryWeapon[]{
                    LegendaryWeapon.ANCIENT_GREATSLAB, LegendaryWeapon.DEMONSLAYER}),
            new StructureLoot("jungle_temple", 0.10, new LegendaryWeapon[]{
                    LegendaryWeapon.VIRIDIAN_CLEAVER, LegendaryWeapon.JADE_REAPER}),
            new StructureLoot("ocean_monument", 0.12, new LegendaryWeapon[]{
                    LegendaryWeapon.NEPTUNES_FANG, LegendaryWeapon.TIDECALLER, LegendaryWeapon.STORMFORK}),
            new StructureLoot("woodland_mansion", 0.12, new LegendaryWeapon[]{
                    LegendaryWeapon.LYCANBANE, LegendaryWeapon.VINDICATOR}),
            new StructureLoot("pillager_outpost", 0.12, new LegendaryWeapon[]{
                    LegendaryWeapon.CRESCENT_EDGE, LegendaryWeapon.VINDICATOR}),
            new StructureLoot("shipwreck", 0.10, new LegendaryWeapon[]{
                    LegendaryWeapon.NEPTUNES_FANG, LegendaryWeapon.STORMFORK}),
            new StructureLoot("buried_treasure", 0.15, new LegendaryWeapon[]{
                    LegendaryWeapon.TIDECALLER}),
            new StructureLoot("ancient_city", 0.12, new LegendaryWeapon[]{
                    LegendaryWeapon.GRAVESCEPTER, LegendaryWeapon.GRAVECLEAVER}),
            new StructureLoot("trail_ruins", 0.10, new LegendaryWeapon[]{
                    LegendaryWeapon.AMETHYST_GREATBLADE, LegendaryWeapon.ANCIENT_GREATSLAB}),
            new StructureLoot("igloo", 0.18, new LegendaryWeapon[]{
                    LegendaryWeapon.CRYSTAL_FROSTBLADE}),
            new StructureLoot("ruined_portal", 0.08, new LegendaryWeapon[]{
                    LegendaryWeapon.FLAMBERGE}),
            new StructureLoot("trial_chambers", 0.10, new LegendaryWeapon[]{
                    LegendaryWeapon.VENGEANCE, LegendaryWeapon.SPIDER_FANG}),
            new StructureLoot("ocean_ruin", 0.08, new LegendaryWeapon[]{
                    LegendaryWeapon.STORMFORK})
    };

    public LegendaryLootListener(JGlimsPlugin plugin, LegendaryWeaponManager weaponManager,
                                  BloodMoonManager bloodMoonManager) {
        this.plugin = plugin;
        this.weaponManager = weaponManager;
        this.bloodMoonManager = bloodMoonManager;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onLootGenerate(LootGenerateEvent event) {
        LootTable lootTable = event.getLootTable();
        if (lootTable == null) return;
        String tableKey = lootTable.getKey().toString().toLowerCase();
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        List<ItemStack> loot = event.getLoot();
        for (StructureLoot sl : STRUCTURE_LOOT) {
            if (tableKey.contains(sl.lootTableContains)) {
                if (rand.nextDouble() < sl.chance) {
                    LegendaryWeapon weapon = sl.pool[rand.nextInt(sl.pool.length)];
                    ItemStack weaponItem = weaponManager.createWeapon(weapon);
                    if (weaponItem != null) {
                        loot.add(weaponItem);
                        plugin.getLogger().info("[" + weapon.getTier().getId() + "] "
                                + weapon.getDisplayName() + " injected into " + sl.lootTableContains + " chest!");
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof EnderDragon) { handleDragonDeathChest(entity); return; }
        if (entity instanceof ElderGuardian) { dropFromPool(event, ELDER_GUARDIAN_POOL, 1, 1, "Elder Guardian"); return; }
        if (entity instanceof Warden) { dropFromPool(event, WARDEN_POOL, 1, 2, "Warden"); return; }
        if (entity instanceof Wither) { dropFromPool(event, WITHER_POOL, 1, 2, "Wither"); return; }
        if (bloodMoonManager.isBloodMoonKing(entity)) {
            ThreadLocalRandom rand = ThreadLocalRandom.current();
            LegendaryWeapon rareWeapon = BLOOD_MOON_RARE_POOL[rand.nextInt(BLOOD_MOON_RARE_POOL.length)];
            ItemStack rareItem = weaponManager.createWeapon(rareWeapon);
            if (rareItem != null) { event.getDrops().add(rareItem); announceWeaponDrop(rareWeapon, "Blood Moon King", entity.getLocation()); }
            if (rand.nextDouble() < 0.15) {
                LegendaryWeapon epicWeapon = BLOOD_MOON_EPIC_POOL[rand.nextInt(BLOOD_MOON_EPIC_POOL.length)];
                ItemStack epicItem = weaponManager.createWeapon(epicWeapon);
                if (epicItem != null) { event.getDrops().add(epicItem); announceWeaponDrop(epicWeapon, "Blood Moon King", entity.getLocation()); }
            }
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
            int count = rand.nextInt(2, 4);
            List<LegendaryWeapon> pool = new ArrayList<>(Arrays.asList(DRAGON_DEATH_CHEST_POOL));
            List<LegendaryWeapon> selected = new ArrayList<>();
            for (int i = 0; i < count && !pool.isEmpty(); i++) {
                selected.add(pool.remove(rand.nextInt(pool.size())));
            }
            int[] slots = {11, 13, 15};
            for (int i = 0; i < selected.size(); i++) {
                ItemStack weaponItem = weaponManager.createWeapon(selected.get(i));
                if (weaponItem != null) chest.getInventory().setItem(slots[i], weaponItem);
            }
            loc.getWorld().spawnParticle(Particle.DRAGON_BREATH, chestLoc.clone().add(0.5, 1, 0.5), 100, 1, 2, 1, 0.05);
            loc.getWorld().spawnParticle(Particle.END_ROD, chestLoc.clone().add(0.5, 2, 0.5), 50, 0.5, 3, 0.5, 0.02);
            loc.getWorld().playSound(chestLoc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 2.0f, 1.0f);
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage(Component.empty());
                p.sendMessage(Component.text("  \u2726 ", NamedTextColor.DARK_PURPLE)
                        .append(Component.text("THE ENDER DRAGON HAS FALLEN!", NamedTextColor.LIGHT_PURPLE).decorate(TextDecoration.BOLD))
                        .append(Component.text(" \u2726", NamedTextColor.DARK_PURPLE)));
                p.sendMessage(Component.text("  A chest of legendary weapons has appeared at the portal!", NamedTextColor.GOLD));
                p.sendMessage(Component.text("  Contents: ", NamedTextColor.GRAY)
                        .append(Component.text(selected.size() + " MYTHIC weapons", NamedTextColor.GOLD).decorate(TextDecoration.BOLD)));
                for (LegendaryWeapon w : selected) {
                    p.sendMessage(Component.text("    \u25B6 ", NamedTextColor.DARK_GRAY)
                            .append(Component.text(w.getDisplayName(), w.getTier().getColor()).decorate(TextDecoration.BOLD))
                            .append(Component.text(" (DMG " + w.getBaseDamage() + ")", NamedTextColor.GRAY)));
                }
                p.sendMessage(Component.empty());
            }
            plugin.getLogger().info("Dragon death chest placed with " + selected.size() + " MYTHIC weapons.");
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
    }

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
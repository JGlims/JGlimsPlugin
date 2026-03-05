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
import org.bukkit.entity.Entity;
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
import com.jglims.plugin.legendary.LegendaryWeapon.LegendaryTier;
import com.jglims.plugin.mobs.BloodMoonManager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * LegendaryLootListener — handles all legendary weapon drops.
 *
 * Drop sources:
 *   1. Structure chests (via LootGenerateEvent loot table key matching)
 *   2. Boss kills (Elder Guardian, Warden, Wither — guaranteed 1 from pool)
 *   3. Ender Dragon death — spawns a death chest with 6 guaranteed LEGENDARY weapons
 *   4. Blood Moon King — 10% chance for any random legendary
 *
 * v1.4.0 — Phase 8
 */
public class LegendaryLootListener implements Listener {

    private final JGlimsPlugin plugin;
    private final LegendaryWeaponManager weaponManager;
    private final BloodMoonManager bloodMoonManager;

    // ═══════════════════════════════════════════════════════════════
    // BOSS DROP POOLS (guaranteed 1 from pool)
    // ═══════════════════════════════════════════════════════════════

    private static final LegendaryWeapon[] ELDER_GUARDIAN_POOL = {
            LegendaryWeapon.OCEANS_RAGE, LegendaryWeapon.AQUATIC_SACRED_BLADE
    };

    private static final LegendaryWeapon[] WARDEN_POOL = {
            LegendaryWeapon.TRUE_EXCALIBUR, LegendaryWeapon.REQUIEM_NINTH_ABYSS,
            LegendaryWeapon.ROYAL_CHAKRAM, LegendaryWeapon.BERSERKERS_GREATAXE,
            LegendaryWeapon.ACIDIC_CLEAVER, LegendaryWeapon.BLACK_IRON_GREATSWORD
    };

    private static final LegendaryWeapon[] WITHER_POOL = {
            LegendaryWeapon.MURAMASA, LegendaryWeapon.PHOENIXS_GRACE,
            LegendaryWeapon.SOUL_COLLECTOR, LegendaryWeapon.AMETHYST_SHURIKEN
    };

    // Ender Dragon death chest — 6 GUARANTEED strong legendaries
    private static final LegendaryWeapon[] DRAGON_DEATH_CHEST_POOL = {
            LegendaryWeapon.ZENITH, LegendaryWeapon.PHANTOMGUARD,
            LegendaryWeapon.MOONLIGHT, LegendaryWeapon.DRAGON_SWORD,
            LegendaryWeapon.TRUE_EXCALIBUR, LegendaryWeapon.VALHAKYRA,
            LegendaryWeapon.BERSERKERS_GREATAXE, LegendaryWeapon.GRAND_CLAYMORE,
            LegendaryWeapon.REQUIEM_NINTH_ABYSS, LegendaryWeapon.CALAMITY_BLADE,
            LegendaryWeapon.DEMONS_BLOOD_BLADE, LegendaryWeapon.BLACK_IRON_GREATSWORD,
            LegendaryWeapon.PHOENIXS_GRACE, LegendaryWeapon.SOUL_COLLECTOR
    };

    // ═══════════════════════════════════════════════════════════════
    // STRUCTURE CHEST DROP TABLES
    // ═══════════════════════════════════════════════════════════════

    // LEGENDARY tier structure drops
    private record StructureLoot(String lootTableContains, double chance, LegendaryWeapon[] pool) {}

    private static final StructureLoot[] LEGENDARY_STRUCTURE_LOOT = {
            // End City — 25% — big pool
            new StructureLoot("end_city", 0.25, new LegendaryWeapon[]{
                    LegendaryWeapon.VALHAKYRA, LegendaryWeapon.WINDREAPER,
                    LegendaryWeapon.PHANTOMGUARD, LegendaryWeapon.MOONLIGHT,
                    LegendaryWeapon.ZENITH, LegendaryWeapon.DRAGON_SWORD
            }),
            // Stronghold — 15%
            new StructureLoot("stronghold", 0.15, new LegendaryWeapon[]{
                    LegendaryWeapon.SOLSTICE, LegendaryWeapon.TRUE_EXCALIBUR
            }),
            // Nether Fortress — 12%
            new StructureLoot("nether_bridge", 0.12, new LegendaryWeapon[]{
                    LegendaryWeapon.CALAMITY_BLADE, LegendaryWeapon.MURAMASA,
                    LegendaryWeapon.PHOENIXS_GRACE
            }),
            // Bastion Remnant — 12%
            new StructureLoot("bastion", 0.12, new LegendaryWeapon[]{
                    LegendaryWeapon.GRAND_CLAYMORE, LegendaryWeapon.BERSERKERS_GREATAXE
            }),
            // Ocean Monument — 10%
            new StructureLoot("ocean_monument", 0.10, new LegendaryWeapon[]{
                    LegendaryWeapon.OCEANS_RAGE, LegendaryWeapon.AQUATIC_SACRED_BLADE
            }),
            // Dungeon (simple_dungeon) — 8%
            new StructureLoot("simple_dungeon", 0.08, new LegendaryWeapon[]{
                    LegendaryWeapon.TALONBRAND, LegendaryWeapon.ACIDIC_CLEAVER,
                    LegendaryWeapon.AMETHYST_SHURIKEN
            }),
            // Desert Temple — 8%
            new StructureLoot("desert_pyramid", 0.08, new LegendaryWeapon[]{
                    LegendaryWeapon.EMERALD_GREATCLEAVER, LegendaryWeapon.SOUL_COLLECTOR
            }),
            // Jungle Temple — 8%
            new StructureLoot("jungle_temple", 0.08, new LegendaryWeapon[]{
                    LegendaryWeapon.EMERALD_GREATCLEAVER, LegendaryWeapon.SOUL_COLLECTOR
            })
    };

    // UNCOMMON tier structure drops (each weapon has individual % per structure)
    private record UncommonLoot(String lootTableContains, double chance, LegendaryWeapon weapon) {}

    private static final UncommonLoot[] UNCOMMON_STRUCTURE_LOOT = {
            // Nocturne — Mineshaft 15%, Dungeon 12%
            new UncommonLoot("abandoned_mineshaft", 0.15, LegendaryWeapon.NOCTURNE),
            new UncommonLoot("simple_dungeon", 0.12, LegendaryWeapon.NOCTURNE),
            // Gravescepter — Dungeon 12%, Ancient City 12%
            new UncommonLoot("simple_dungeon", 0.12, LegendaryWeapon.GRAVESCEPTER),
            new UncommonLoot("ancient_city", 0.12, LegendaryWeapon.GRAVESCEPTER),
            // Lycanbane — Woodland Mansion 15%
            new UncommonLoot("woodland_mansion", 0.15, LegendaryWeapon.LYCANBANE),
            // Gloomsteel Katana — Mineshaft 12%, Dungeon 12%
            new UncommonLoot("abandoned_mineshaft", 0.12, LegendaryWeapon.GLOOMSTEEL_KATANA),
            new UncommonLoot("simple_dungeon", 0.12, LegendaryWeapon.GLOOMSTEEL_KATANA),
            // Viridian Cleaver — Jungle Temple 18%
            new UncommonLoot("jungle_temple", 0.18, LegendaryWeapon.VIRIDIAN_CLEAVER),
            // Crescent Edge — Pillager Outpost 15%
            new UncommonLoot("pillager_outpost", 0.15, LegendaryWeapon.CRESCENT_EDGE),
            // Gravecleaver — Ancient City 12%
            new UncommonLoot("ancient_city", 0.12, LegendaryWeapon.GRAVECLEAVER),
            // Amethyst Greatblade — Trail Ruins 10%
            new UncommonLoot("trail_ruins", 0.10, LegendaryWeapon.AMETHYST_GREATBLADE),
            // Flamberge — Nether Fortress 10%, Ruined Portal 8%
            new UncommonLoot("nether_bridge", 0.10, LegendaryWeapon.FLAMBERGE),
            new UncommonLoot("ruined_portal", 0.08, LegendaryWeapon.FLAMBERGE),
            // Crystal Frostblade — Igloo 20%, Snow structures 15%
            new UncommonLoot("igloo", 0.20, LegendaryWeapon.CRYSTAL_FROSTBLADE),
            new UncommonLoot("village_snowy", 0.15, LegendaryWeapon.CRYSTAL_FROSTBLADE),
            // Demonslayer — Nether Fortress 10%, Desert Temple 10%
            new UncommonLoot("nether_bridge", 0.10, LegendaryWeapon.DEMONSLAYER),
            new UncommonLoot("desert_pyramid", 0.10, LegendaryWeapon.DEMONSLAYER),
            // Vengeance — Trial Chambers 12%, Dungeon 8%
            new UncommonLoot("trial_chambers", 0.12, LegendaryWeapon.VENGEANCE),
            new UncommonLoot("simple_dungeon", 0.08, LegendaryWeapon.VENGEANCE),
            // Oculus — Stronghold 10%, End City 8%
            new UncommonLoot("stronghold", 0.10, LegendaryWeapon.OCULUS),
            new UncommonLoot("end_city", 0.08, LegendaryWeapon.OCULUS),
            // Ancient Greatslab — Desert Temple 12%, Trail Ruins 12%
            new UncommonLoot("desert_pyramid", 0.12, LegendaryWeapon.ANCIENT_GREATSLAB),
            new UncommonLoot("trail_ruins", 0.12, LegendaryWeapon.ANCIENT_GREATSLAB),
            // Neptune's Fang — Ocean Monument 15%, Shipwreck 10%
            new UncommonLoot("ocean_monument", 0.15, LegendaryWeapon.NEPTUNES_FANG),
            new UncommonLoot("shipwreck", 0.10, LegendaryWeapon.NEPTUNES_FANG),
            // Tidecaller — Ocean Monument 12%, Buried Treasure 15%
            new UncommonLoot("ocean_monument", 0.12, LegendaryWeapon.TIDECALLER),
            new UncommonLoot("buried_treasure", 0.15, LegendaryWeapon.TIDECALLER),
            // Stormfork — Shipwreck 12%, Ocean Ruins 10%
            new UncommonLoot("shipwreck", 0.12, LegendaryWeapon.STORMFORK),
            new UncommonLoot("ocean_ruin", 0.10, LegendaryWeapon.STORMFORK),
            // Jade Reaper — Jungle Temple 15%, Woodland Mansion 10%
            new UncommonLoot("jungle_temple", 0.15, LegendaryWeapon.JADE_REAPER),
            new UncommonLoot("woodland_mansion", 0.10, LegendaryWeapon.JADE_REAPER),
            // Vindicator — Pillager Outpost 15%, Woodland Mansion 12%
            new UncommonLoot("pillager_outpost", 0.15, LegendaryWeapon.VINDICATOR),
            new UncommonLoot("woodland_mansion", 0.12, LegendaryWeapon.VINDICATOR),
            // Spider Fang — Mineshaft 15%, Trial Chambers 10%
            new UncommonLoot("abandoned_mineshaft", 0.15, LegendaryWeapon.SPIDER_FANG),
            new UncommonLoot("trial_chambers", 0.10, LegendaryWeapon.SPIDER_FANG)
    };

    public LegendaryLootListener(JGlimsPlugin plugin, LegendaryWeaponManager weaponManager,
                                  BloodMoonManager bloodMoonManager) {
        this.plugin = plugin;
        this.weaponManager = weaponManager;
        this.bloodMoonManager = bloodMoonManager;
    }

    // ════════════════════════════════════════════════════════════════
    // STRUCTURE CHEST DROPS — LootGenerateEvent
    // ════════════════════════════════════════════════════════════════

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onLootGenerate(LootGenerateEvent event) {
        LootTable lootTable = event.getLootTable();
        if (lootTable == null) return;

        String tableKey = lootTable.getKey().toString().toLowerCase();
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        List<ItemStack> loot = event.getLoot();

        // Check LEGENDARY tier structure drops
        for (StructureLoot sl : LEGENDARY_STRUCTURE_LOOT) {
            if (tableKey.contains(sl.lootTableContains)) {
                if (rand.nextDouble() < sl.chance) {
                    LegendaryWeapon weapon = sl.pool[rand.nextInt(sl.pool.length)];
                    ItemStack weaponItem = weaponManager.createWeapon(weapon);
                    if (weaponItem != null) {
                        loot.add(weaponItem);
                        plugin.getLogger().info("Legendary weapon '" + weapon.getDisplayName()
                                + "' injected into " + sl.lootTableContains + " chest!");
                    }
                }
                break; // Only one legendary-tier check per chest
            }
        }

        // Check UNCOMMON tier structure drops (multiple can roll independently)
        for (UncommonLoot ul : UNCOMMON_STRUCTURE_LOOT) {
            if (tableKey.contains(ul.lootTableContains)) {
                if (rand.nextDouble() < ul.chance) {
                    ItemStack weaponItem = weaponManager.createWeapon(ul.weapon);
                    if (weaponItem != null) {
                        loot.add(weaponItem);
                        plugin.getLogger().info("Uncommon legendary '" + ul.weapon.getDisplayName()
                                + "' injected into " + ul.lootTableContains + " chest!");
                    }
                }
            }
        }
    }

    // ════════════════════════════════════════════════════════════════
    // BOSS DEATH DROPS
    // ════════════════════════════════════════════════════════════════

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();

        // ── Ender Dragon death → spawn chest with 6 GUARANTEED legendaries ──
        if (entity instanceof EnderDragon) {
            handleDragonDeathChest(entity);
            return;
        }

        // ── Elder Guardian → guaranteed 1 from pool ──
        if (entity instanceof ElderGuardian) {
            dropBossLegendary(event, ELDER_GUARDIAN_POOL, "Elder Guardian");
            return;
        }

        // ── Warden → guaranteed 1 from pool ──
        if (entity instanceof Warden) {
            dropBossLegendary(event, WARDEN_POOL, "Warden");
            return;
        }

        // ── Wither → guaranteed 1 from pool ──
        if (entity instanceof Wither) {
            dropBossLegendary(event, WITHER_POOL, "Wither");
            return;
        }

        // ── Blood Moon King → 10% chance for any random legendary ──
        if (bloodMoonManager.isBloodMoonKing(entity)) {
            if (ThreadLocalRandom.current().nextDouble() < 0.10) {
                LegendaryWeapon[] allLegendaries = Arrays.stream(LegendaryWeapon.values())
                        .filter(w -> w.getTier() == LegendaryTier.LEGENDARY)
                        .toArray(LegendaryWeapon[]::new);
                LegendaryWeapon weapon = allLegendaries[ThreadLocalRandom.current().nextInt(allLegendaries.length)];
                ItemStack weaponItem = weaponManager.createWeapon(weapon);
                if (weaponItem != null) {
                    event.getDrops().add(weaponItem);
                    announceWeaponDrop(weapon, "Blood Moon King", entity.getLocation());
                }
            }
        }
    }

    // ── Helper: Drop guaranteed 1 legendary from a boss pool ──
    private void dropBossLegendary(EntityDeathEvent event, LegendaryWeapon[] pool, String bossName) {
        LegendaryWeapon weapon = pool[ThreadLocalRandom.current().nextInt(pool.length)];
        ItemStack weaponItem = weaponManager.createWeapon(weapon);
        if (weaponItem != null) {
            event.getDrops().add(weaponItem);
            announceWeaponDrop(weapon, bossName, event.getEntity().getLocation());
        }
    }

    // ════════════════════════════════════════════════════════════════
    // ENDER DRAGON DEATH CHEST — 6 GUARANTEED LEGENDARIES
    // ════════════════════════════════════════════════════════════════

    private void handleDragonDeathChest(LivingEntity dragon) {
        Location loc = dragon.getLocation();

        // Schedule for 1 tick later to ensure chunks loaded
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // Find the end portal (0, ~64, 0) or use dragon death location
            Location chestLoc = new Location(loc.getWorld(), 0, loc.getWorld().getHighestBlockYAt(0, 0) + 1, 0);

            // Make sure the spot is safe — place chest on top of the portal
            Block chestBlock = chestLoc.getBlock();
            chestBlock.setType(Material.CHEST);

            if (!(chestBlock.getState() instanceof Chest chest)) {
                // Fallback: drop items at location
                plugin.getLogger().warning("Could not place Dragon death chest at " + chestLoc + ". Dropping items instead.");
                dropDragonLootAtLocation(loc);
                return;
            }

            // Pick 6 unique legendaries from the death chest pool
            List<LegendaryWeapon> pool = new ArrayList<>(Arrays.asList(DRAGON_DEATH_CHEST_POOL));
            List<LegendaryWeapon> selected = new ArrayList<>();
            for (int i = 0; i < 6 && !pool.isEmpty(); i++) {
                int idx = ThreadLocalRandom.current().nextInt(pool.size());
                selected.add(pool.remove(idx));
            }

            // Fill chest
            for (int i = 0; i < selected.size(); i++) {
                ItemStack weaponItem = weaponManager.createWeapon(selected.get(i));
                if (weaponItem != null) {
                    chest.getInventory().setItem(i + 11, weaponItem); // Center-ish slots
                }
            }

            // Dramatic effects
            loc.getWorld().spawnParticle(Particle.DRAGON_BREATH, chestLoc.clone().add(0.5, 1, 0.5), 100, 1, 2, 1, 0.05);
            loc.getWorld().spawnParticle(Particle.END_ROD, chestLoc.clone().add(0.5, 2, 0.5), 50, 0.5, 3, 0.5, 0.02);
            loc.getWorld().playSound(chestLoc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 2.0f, 1.0f);

            // Announce to all players
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage(Component.empty());
                p.sendMessage(Component.text("  \u2726 ", NamedTextColor.DARK_PURPLE)
                        .append(Component.text("THE ENDER DRAGON HAS FALLEN!", NamedTextColor.LIGHT_PURPLE)
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text(" \u2726", NamedTextColor.DARK_PURPLE)));
                p.sendMessage(Component.text("  A chest of legendary weapons has appeared at the portal!",
                        NamedTextColor.GOLD));
                p.sendMessage(Component.text("  Contents: ", NamedTextColor.GRAY)
                        .append(Component.text(selected.size() + " legendary weapons", NamedTextColor.DARK_PURPLE)
                                .decorate(TextDecoration.BOLD)));
                for (LegendaryWeapon w : selected) {
                    p.sendMessage(Component.text("    \u25B6 ", NamedTextColor.DARK_GRAY)
                            .append(Component.text(w.getDisplayName(), NamedTextColor.DARK_PURPLE)
                                    .decorate(TextDecoration.BOLD))
                            .append(Component.text(" (DMG " + w.getBaseDamage() + ")", NamedTextColor.GRAY)));
                }
                p.sendMessage(Component.empty());
            }

            plugin.getLogger().info("Dragon death chest placed at " + chestLoc.getBlockX() + ", "
                    + chestLoc.getBlockY() + ", " + chestLoc.getBlockZ() + " with " + selected.size() + " legendaries.");
        }, 5L);
    }

    /**
     * Fallback: Drop dragon legendaries at location if chest can't be placed.
     */
    private void dropDragonLootAtLocation(Location loc) {
        List<LegendaryWeapon> pool = new ArrayList<>(Arrays.asList(DRAGON_DEATH_CHEST_POOL));
        for (int i = 0; i < 6 && !pool.isEmpty(); i++) {
            int idx = ThreadLocalRandom.current().nextInt(pool.size());
            LegendaryWeapon weapon = pool.remove(idx);
            ItemStack weaponItem = weaponManager.createWeapon(weapon);
            if (weaponItem != null) {
                loc.getWorld().dropItemNaturally(loc, weaponItem);
            }
        }
    }

    // ════════════════════════════════════════════════════════════════
    // UTILITY: Announce legendary weapon drops
    // ════════════════════════════════════════════════════════════════

    private void announceWeaponDrop(LegendaryWeapon weapon, String source, Location loc) {
        NamedTextColor tierColor = weapon.getTier() == LegendaryTier.LEGENDARY
                ? NamedTextColor.DARK_PURPLE : NamedTextColor.GOLD;

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(Component.text("\u2726 ", NamedTextColor.DARK_PURPLE)
                    .append(Component.text(weapon.getDisplayName(), tierColor).decorate(TextDecoration.BOLD))
                    .append(Component.text(" dropped from ", NamedTextColor.GRAY))
                    .append(Component.text(source, NamedTextColor.RED).decorate(TextDecoration.BOLD))
                    .append(Component.text("!", NamedTextColor.GRAY)));
        }

        // Particle + sound at drop location
        if (loc != null && loc.getWorld() != null) {
            loc.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, loc.clone().add(0, 1, 0), 30, 0.5, 1, 0.5, 0.2);
            loc.getWorld().playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 2.0f, 1.2f);
        }

        plugin.getLogger().info("Legendary '" + weapon.getDisplayName() + "' dropped from " + source + "!");
    }
}

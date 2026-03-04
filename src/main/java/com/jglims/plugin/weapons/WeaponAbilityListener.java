package com.jglims.plugin.weapons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.config.ConfigManager;
import com.jglims.plugin.enchantments.CustomEnchantManager;

/**
 * WeaponAbilityListener — v1.3.0 FINAL (HOTFIX 2)
 *
 * HOTFIX 2 CHANGES:
 *   - Particle.DRAGON_BREATH now requires Float data in PaperMC 1.21.11
 *     Replaced all DRAGON_BREATH with SOUL (no data required, similar dark aesthetic)
 *     This was causing runtime exceptions in all abilities that used DRAGON_BREATH
 */
public class WeaponAbilityListener implements Listener {

    private final JGlimsPlugin plugin;
    private final ConfigManager config;
    private final CustomEnchantManager enchantManager;
    private final SuperToolManager superToolManager;
    private final SpearManager spearManager;
    private final BattleShovelManager battleShovelManager;

    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();
    private final NamespacedKey superTierKey;

    public WeaponAbilityListener(JGlimsPlugin plugin, ConfigManager config,
                                  CustomEnchantManager enchantManager,
                                  SuperToolManager superToolManager,
                                  SpearManager spearManager,
                                  BattleShovelManager battleShovelManager) {
        this.plugin = plugin;
        this.config = config;
        this.enchantManager = enchantManager;
        this.superToolManager = superToolManager;
        this.spearManager = spearManager;
        this.battleShovelManager = battleShovelManager;
        this.superTierKey = new NamespacedKey(plugin, "super_tool_tier");
    }

    // ============================================================
    // UTILITY: Cooldown management
    // ============================================================

    private boolean isOnCooldown(Player player, String abilityName) {
        Map<String, Long> playerCooldowns = cooldowns.get(player.getUniqueId());
        if (playerCooldowns == null) return false;
        Long expiry = playerCooldowns.get(abilityName);
        if (expiry == null) return false;
        if (System.currentTimeMillis() >= expiry) {
            playerCooldowns.remove(abilityName);
            return false;
        }
        return true;
    }

    private long getRemainingCooldown(Player player, String abilityName) {
        Map<String, Long> playerCooldowns = cooldowns.get(player.getUniqueId());
        if (playerCooldowns == null) return 0;
        Long expiry = playerCooldowns.get(abilityName);
        if (expiry == null) return 0;
        long remaining = expiry - System.currentTimeMillis();
        return Math.max(0, remaining);
    }

    private void setCooldown(Player player, String abilityName, int seconds) {
        cooldowns.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
                .put(abilityName, System.currentTimeMillis() + (seconds * 1000L));
    }

    private void sendCooldownMessage(Player player, String abilityName) {
        long remaining = getRemainingCooldown(player, abilityName);
        double secondsLeft = remaining / 1000.0;
        player.sendActionBar("\u00a7c" + abilityName + " on cooldown: \u00a7e" + String.format("%.1f", secondsLeft) + "s");
    }

    // ============================================================
    // UTILITY: Get super tier from any weapon
    // ============================================================

    private int getSuperTier(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return 0;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        int spearTier = pdc.getOrDefault(spearManager.getSuperTierKey(), PersistentDataType.INTEGER, 0);
        if (spearTier > 0) return spearTier;
        return pdc.getOrDefault(superTierKey, PersistentDataType.INTEGER, 0);
    }

    // ============================================================
    // UTILITY: Get weapon class from material
    // ============================================================

    private String getWeaponClass(Material mat) {
        String name = mat.name();
        if (name.endsWith("_SWORD")) return "SWORD";
        if (name.endsWith("_AXE")) return "AXE";
        if (name.endsWith("_PICKAXE")) return "PICKAXE";
        if (name.endsWith("_SHOVEL")) return "SHOVEL";
        if (name.endsWith("_HOE")) return "HOE";
        if (name.endsWith("_SPEAR")) return "SPEAR";
        if (mat == Material.BOW) return "BOW";
        if (mat == Material.CROSSBOW) return "CROSSBOW";
        if (mat == Material.TRIDENT) return "TRIDENT";
        if (mat == Material.MACE) return "MACE";
        return "UNKNOWN";
    }

    // ============================================================
    // UTILITY: Damage an entity respecting Ender Dragon exception
    // ============================================================

    private void dealAbilityDamage(Player player, LivingEntity target, double damage, boolean isDefinitive) {
        if (isDefinitive && target instanceof EnderDragon) {
            double reduction = config.getEnderDragonAbilityDamageReduction();
            target.damage(damage * reduction, player);
            return;
        }
        target.damage(damage, player);
    }

    // ============================================================
    // UTILITY: Get nearby enemies
    // ============================================================

    private List<LivingEntity> getNearbyEnemies(Location center, double radius, Player exclude) {
        List<LivingEntity> enemies = new ArrayList<>();
        for (Entity e : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
            if (e instanceof LivingEntity le && !(e instanceof Player) && !(e instanceof ArmorStand) && e != exclude) {
                if (le.getLocation().distanceSquared(center) <= radius * radius) {
                    enemies.add(le);
                }
            }
        }
        return enemies;
    }

    // ============================================================
    // MAIN EVENT: Right-click ability activation
    // ============================================================

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) return;

        int superTier = getSuperTier(item);
        if (superTier < 2) return;

        boolean isDefinitive = superTier >= 3;
        String weaponClass = getWeaponClass(item.getType());

        switch (weaponClass) {
            case "SWORD" -> handleSwordAbility(player, item, isDefinitive);
            case "AXE" -> handleAxeAbility(player, item, isDefinitive);
            case "PICKAXE" -> handlePickaxeAbility(player, item, isDefinitive);
            case "SHOVEL" -> handleShovelAbility(player, item, isDefinitive);
            case "HOE" -> handleSickleAbility(player, item, isDefinitive);
            case "SPEAR" -> handleSpearAbility(player, item, isDefinitive);
            case "BOW" -> handleBowAbility(player, item, isDefinitive);
            case "CROSSBOW" -> handleCrossbowAbility(player, item, isDefinitive);
            case "TRIDENT" -> handleTridentAbility(player, item, isDefinitive);
            case "MACE" -> handleMaceAbility(player, item, isDefinitive);
        }
    }

    // ================================================================
    // SWORD — "Dash Strike" / "Dimensional Cleave"
    // ================================================================

    private void handleSwordAbility(Player player, ItemStack item, boolean isDefinitive) {
        String abilityName = isDefinitive ? "Dimensional Cleave" : "Dash Strike";
        int cooldownSec = isDefinitive ? 12 : 6;

        if (isOnCooldown(player, abilityName)) { sendCooldownMessage(player, abilityName); return; }
        setCooldown(player, abilityName, cooldownSec);

        if (!isDefinitive) {
            player.setVelocity(player.getLocation().getDirection().multiply(1.8));
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.5f, 1.2f);

            new BukkitRunnable() {
                int ticks = 0;
                @Override public void run() {
                    if (ticks >= 8) { cancel(); return; }
                    Location loc = player.getLocation();
                    player.getWorld().spawnParticle(Particle.SWEEP_ATTACK, loc.clone().add(0, 1, 0), 3, 0.5, 0.3, 0.5, 0);
                    player.getWorld().spawnParticle(Particle.CRIT, loc, 8, 0.6, 0.4, 0.6, 0.1);
                    for (LivingEntity enemy : getNearbyEnemies(loc, 2.5, player)) {
                        dealAbilityDamage(player, enemy, 8.0, false);
                        enemy.setVelocity(player.getLocation().getDirection().multiply(0.5));
                    }
                    ticks++;
                }
            }.runTaskTimer(plugin, 0L, 2L);

        } else {
            player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1.0f, 1.5f);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 2.0f, 0.5f);

            Location center = player.getLocation().add(player.getLocation().getDirection().multiply(4));
            center.setY(player.getLocation().getY());

            new BukkitRunnable() {
                int ticks = 0;
                @Override public void run() {
                    if (ticks >= 15) { cancel(); return; }
                    double radius = 2.0 + (ticks * 0.6);
                    for (int i = 0; i < 40; i++) {
                        double angle = (2 * Math.PI / 40) * i;
                        double x = Math.cos(angle) * radius;
                        double z = Math.sin(angle) * radius;
                        Location particleLoc = center.clone().add(x, 0.5, z);
                        // FIX: replaced DRAGON_BREATH with SOUL (no data required)
                        player.getWorld().spawnParticle(Particle.SOUL, particleLoc, 1, 0, 0, 0, 0);
                        if (ticks % 3 == 0) {
                            player.getWorld().spawnParticle(Particle.REVERSE_PORTAL, particleLoc, 2, 0.1, 0.5, 0.1, 0.05);
                        }
                    }
                    if (ticks % 3 == 0) {
                        for (LivingEntity enemy : getNearbyEnemies(center, radius, player)) {
                            dealAbilityDamage(player, enemy, 15.0, true);
                            enemy.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 2));
                        }
                    }
                    player.getWorld().spawnParticle(Particle.EXPLOSION, center.clone().add(0, 1, 0), 1, 0, 0, 0, 0);
                    player.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, center, 15, 1.5, 2, 1.5, 0.02);
                    ticks++;
                }
            }.runTaskTimer(plugin, 0L, 2L);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, center, 3, 0, 0, 0, 0);
                player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, center, 100, 3, 2, 3, 0.5);
                player.getWorld().playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.6f);
                for (LivingEntity enemy : getNearbyEnemies(center, 8.0, player)) {
                    dealAbilityDamage(player, enemy, 25.0, true);
                    Vector knockback = enemy.getLocation().toVector().subtract(center.toVector()).normalize().multiply(1.5);
                    knockback.setY(0.6);
                    enemy.setVelocity(knockback);
                }
            }, 30L);
        }
    }

    // ================================================================
    // AXE — "Bloodthirst" / "Ragnarok Cleave"
    // ================================================================

    private void handleAxeAbility(Player player, ItemStack item, boolean isDefinitive) {
        String abilityName = isDefinitive ? "Ragnarok Cleave" : "Bloodthirst";
        int cooldownSec = isDefinitive ? 15 : 8;

        if (isOnCooldown(player, abilityName)) { sendCooldownMessage(player, abilityName); return; }
        setCooldown(player, abilityName, cooldownSec);

        if (!isDefinitive) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 100, 1));
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 100, 0));
            player.playSound(player.getLocation(), Sound.ENTITY_RAVAGER_ROAR, 0.8f, 1.2f);

            ItemMeta meta = item.getItemMeta();
            meta.getPersistentDataContainer().set(
                    new NamespacedKey(plugin, "bloodthirst_active"),
                    PersistentDataType.LONG, System.currentTimeMillis() + 5000L);
            item.setItemMeta(meta);

            new BukkitRunnable() {
                int ticks = 0;
                @Override public void run() {
                    if (ticks >= 50) { cancel(); return; }
                    Location loc = player.getLocation().add(0, 1, 0);
                    player.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, loc, 4, 0.5, 0.5, 0.5, 0.1);
                    player.getWorld().spawnParticle(Particle.DUST, loc, 6, 0.6, 0.6, 0.6, 0,
                            new Particle.DustOptions(Color.fromRGB(180, 0, 0), 1.5f));
                    ticks++;
                }
            }.runTaskTimer(plugin, 0L, 2L);

            player.sendActionBar("\u00a7c\u00a7l\u2620 BLOODTHIRST ACTIVE \u2620");

        } else {
            player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1.5f, 0.7f);
            player.playSound(player.getLocation(), Sound.ITEM_MACE_SMASH_GROUND_HEAVY, 2.0f, 0.8f);

            Location center = player.getLocation();

            new BukkitRunnable() {
                int ticks = 0;
                @Override public void run() {
                    if (ticks >= 20) { cancel(); return; }
                    double radius = ticks * 0.5;
                    for (int i = 0; i < 30 + (ticks * 2); i++) {
                        double angle = Math.random() * 2 * Math.PI;
                        double r = Math.random() * radius;
                        Location particleLoc = center.clone().add(Math.cos(angle) * r, 0.1, Math.sin(angle) * r);
                        player.getWorld().spawnParticle(Particle.LAVA, particleLoc, 1, 0, 0, 0, 0);
                        player.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, particleLoc, 1, 0, 0.5, 0, 0.02);
                        if (ticks > 5) {
                            player.getWorld().spawnParticle(Particle.FLAME, particleLoc, 1, 0, 0.3, 0, 0.01);
                        }
                    }
                    ticks++;
                }
            }.runTaskTimer(plugin, 0L, 1L);

            for (int wave = 0; wave < 3; wave++) {
                final double waveRadius = 4.0 + (wave * 3.0);
                final double waveDamage = 20.0 - (wave * 4.0);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    for (LivingEntity enemy : getNearbyEnemies(center, waveRadius, player)) {
                        dealAbilityDamage(player, enemy, waveDamage, true);
                        Vector launch = enemy.getLocation().toVector().subtract(center.toVector()).normalize().multiply(1.2);
                        launch.setY(0.8 + Math.random() * 0.5);
                        enemy.setVelocity(launch);
                        enemy.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 80, 1));
                    }
                    player.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, center, 1, 0, 0, 0, 0);
                    player.getWorld().playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.9f + (float)(waveRadius * 0.05));
                }, wave * 8L);
            }

            player.sendActionBar("\u00a74\u00a7l\u2620 RAGNAROK CLEAVE \u2620");
        }
    }

    // ================================================================
    // PICKAXE — "Ore Pulse" / "Seismic Resonance"
    // ================================================================

    private void handlePickaxeAbility(Player player, ItemStack item, boolean isDefinitive) {
        String abilityName = isDefinitive ? "Seismic Resonance" : "Ore Pulse";
        int cooldownSec = isDefinitive ? 18 : 10;

        if (isOnCooldown(player, abilityName)) { sendCooldownMessage(player, abilityName); return; }
        setCooldown(player, abilityName, cooldownSec);

        int oreRadius = isDefinitive ? config.getOreDetectRadiusNetherite() : config.getOreDetectRadiusDiamond();
        int debrisRadius = isDefinitive ? config.getOreDetectAncientDebrisRadiusNetherite() : config.getOreDetectAncientDebrisRadiusDiamond();
        int durationTicks = config.getOreDetectDurationTicks();

        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, isDefinitive ? 0.5f : 1.0f);
        if (isDefinitive) { player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_HEARTBEAT, 1.5f, 0.6f); }

        Location playerLoc = player.getLocation();
        World world = player.getWorld();
        Map<Location, OreType> foundOres = new LinkedHashMap<>();
        int maxRadius = Math.max(oreRadius, debrisRadius);

        for (int x = -maxRadius; x <= maxRadius; x++) {
            for (int y = -maxRadius; y <= maxRadius; y++) {
                for (int z = -maxRadius; z <= maxRadius; z++) {
                    Block block = world.getBlockAt(playerLoc.getBlockX() + x, playerLoc.getBlockY() + y, playerLoc.getBlockZ() + z);
                    double distSq = x * x + y * y + z * z;
                    OreType oreType = classifyOre(block.getType());
                    if (oreType == null) continue;
                    if (oreType == OreType.ANCIENT_DEBRIS) {
                        if (distSq <= debrisRadius * debrisRadius) foundOres.put(block.getLocation(), oreType);
                    } else {
                        if (distSq <= oreRadius * oreRadius) foundOres.put(block.getLocation(), oreType);
                    }
                }
            }
        }

        int totalOres = foundOres.size();
        long debrisCount = foundOres.values().stream().filter(o -> o == OreType.ANCIENT_DEBRIS).count();

        if (totalOres == 0) {
            player.sendActionBar("\u00a77Ore Pulse: \u00a7fNo ores detected nearby.");
        } else {
            String msg = isDefinitive
                    ? "\u00a75\u00a7l SEISMIC RESONANCE: \u00a7f" + totalOres + " ores detected"
                    : "\u00a7e Ore Pulse: \u00a7f" + totalOres + " ores detected";
            if (debrisCount > 0) msg += " \u00a77| \u00a74\u00a7l" + debrisCount + " Ancient Debris!";
            player.sendActionBar(msg);
            if (debrisCount > 0) player.sendMessage("\u00a74\u00a7l " + debrisCount + " Ancient Debris detected within " + debrisRadius + " blocks!");
        }

        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 5) { cancel(); return; }
                double r = tick * (isDefinitive ? 3.0 : 2.0);
                for (int i = 0; i < 20; i++) {
                    double angle = Math.random() * 2 * Math.PI;
                    Location pLoc = playerLoc.clone().add(Math.cos(angle) * r, 0.5, Math.sin(angle) * r);
                    world.spawnParticle(isDefinitive ? Particle.SOUL_FIRE_FLAME : Particle.ENCHANT, pLoc, 2, 0, 0.3, 0, 0.05);
                }
                tick++;
            }
        }.runTaskTimer(plugin, 0L, 3L);

        new BukkitRunnable() {
            int ticks = 0;
            @Override public void run() {
                if (ticks >= durationTicks || !player.isOnline()) { cancel(); return; }
                for (Map.Entry<Location, OreType> entry : foundOres.entrySet()) {
                    Location oreLoc = entry.getKey().clone().add(0.5, 0.5, 0.5);
                    OreType type = entry.getValue();
                    if (ticks % 5 == 0) {
                        player.spawnParticle(Particle.DUST, oreLoc, 3, 0.2, 0.2, 0.2, 0, type.getDustOptions());
                        if (type == OreType.ANCIENT_DEBRIS) {
                            player.spawnParticle(Particle.FLAME, oreLoc, 1, 0.1, 0.1, 0.1, 0.01);
                            if (ticks % 20 == 0) player.spawnParticle(Particle.SOUL_FIRE_FLAME, oreLoc, 5, 0.3, 0.3, 0.3, 0.02);
                        }
                    }
                }
                ticks++;
            }
        }.runTaskTimer(plugin, 5L, 1L);
    }

    private enum OreType {
        COAL(Color.fromRGB(50, 50, 50)), IRON(Color.fromRGB(210, 150, 100)), COPPER(Color.fromRGB(180, 100, 50)),
        GOLD(Color.fromRGB(255, 215, 0)), REDSTONE(Color.fromRGB(255, 0, 0)), LAPIS(Color.fromRGB(30, 30, 200)),
        EMERALD(Color.fromRGB(0, 200, 50)), DIAMOND(Color.fromRGB(100, 230, 255)), ANCIENT_DEBRIS(Color.fromRGB(100, 50, 20)),
        NETHER_GOLD(Color.fromRGB(255, 200, 50)), NETHER_QUARTZ(Color.fromRGB(240, 240, 230));
        private final Color color;
        OreType(Color color) { this.color = color; }
        public Particle.DustOptions getDustOptions() { return new Particle.DustOptions(color, 1.5f); }
    }

    private OreType classifyOre(Material mat) {
        return switch (mat) {
            case COAL_ORE, DEEPSLATE_COAL_ORE -> OreType.COAL;
            case IRON_ORE, DEEPSLATE_IRON_ORE -> OreType.IRON;
            case COPPER_ORE, DEEPSLATE_COPPER_ORE -> OreType.COPPER;
            case GOLD_ORE, DEEPSLATE_GOLD_ORE -> OreType.GOLD;
            case REDSTONE_ORE, DEEPSLATE_REDSTONE_ORE -> OreType.REDSTONE;
            case LAPIS_ORE, DEEPSLATE_LAPIS_ORE -> OreType.LAPIS;
            case EMERALD_ORE, DEEPSLATE_EMERALD_ORE -> OreType.EMERALD;
            case DIAMOND_ORE, DEEPSLATE_DIAMOND_ORE -> OreType.DIAMOND;
            case ANCIENT_DEBRIS -> OreType.ANCIENT_DEBRIS;
            case NETHER_GOLD_ORE -> OreType.NETHER_GOLD;
            case NETHER_QUARTZ_ORE -> OreType.NETHER_QUARTZ;
            default -> null;
        };
    }

    // ================================================================
    // SHOVEL — "Earthen Wall" / "Tectonic Upheaval"
    // ================================================================

    private void handleShovelAbility(Player player, ItemStack item, boolean isDefinitive) {
        String abilityName = isDefinitive ? "Tectonic Upheaval" : "Earthen Wall";
        int cooldownSec = isDefinitive ? 14 : 8;

        if (isOnCooldown(player, abilityName)) { sendCooldownMessage(player, abilityName); return; }
        setCooldown(player, abilityName, cooldownSec);

        if (!isDefinitive) {
            player.playSound(player.getLocation(), Sound.BLOCK_GRAVEL_BREAK, 2.0f, 0.5f);
            player.playSound(player.getLocation(), Sound.ENTITY_IRON_GOLEM_ATTACK, 1.0f, 0.7f);
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 80, 1));
            Location center = player.getLocation();
            for (LivingEntity enemy : getNearbyEnemies(center, 6.0, player)) {
                enemy.setVelocity(new Vector(0, 1.2, 0));
                dealAbilityDamage(player, enemy, 6.0, false);
            }
            for (int i = 0; i < 50; i++) {
                double angle = Math.random() * 2 * Math.PI;
                double r = Math.random() * 5;
                Location pLoc = center.clone().add(Math.cos(angle) * r, 0.2, Math.sin(angle) * r);
                player.getWorld().spawnParticle(Particle.BLOCK, pLoc, 5, 0.3, 0.5, 0.3, 0.1, Material.DIRT.createBlockData());
            }
            player.sendActionBar("\u00a7e Earthen Wall \u2014 Enemies launched!");

        } else {
            player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_EMERGE, 2.0f, 0.5f);
            player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1.0f, 0.3f);
            Location center = player.getLocation();
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 160, 2));

            new BukkitRunnable() {
                int ticks = 0;
                @Override public void run() {
                    if (ticks >= 30) { cancel(); return; }
                    double radius = ticks * 0.4;
                    for (int i = 0; i < 25 + ticks; i++) {
                        double angle = Math.random() * 2 * Math.PI;
                        double r = Math.random() * radius;
                        Location pLoc = center.clone().add(Math.cos(angle) * r, 0.1, Math.sin(angle) * r);
                        player.getWorld().spawnParticle(Particle.BLOCK, pLoc, 3, 0.1, 0.8, 0.1, 0.3, Material.NETHERRACK.createBlockData());
                        player.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, pLoc, 1, 0, 1, 0, 0.05);
                    }
                    if (ticks % 10 == 5) {
                        for (LivingEntity enemy : getNearbyEnemies(center, radius, player)) {
                            dealAbilityDamage(player, enemy, 14.0, true);
                            Vector launch = enemy.getLocation().toVector().subtract(center.toVector()).normalize().multiply(0.8);
                            launch.setY(1.8);
                            enemy.setVelocity(launch);
                            enemy.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 120, 3));
                        }
                        player.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, center.clone().add(0, 1, 0), 2, 2, 0, 2, 0);
                        player.getWorld().playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.5f);
                    }
                    ticks++;
                }
            }.runTaskTimer(plugin, 0L, 1L);

            player.sendActionBar("\u00a74\u00a7l TECTONIC UPHEAVAL ");
        }
    }

    // ================================================================
    // SICKLE — "Harvest Storm" / "Reaper's Scythe" (REVAMPED)
    // ================================================================

    private void handleSickleAbility(Player player, ItemStack item, boolean isDefinitive) {
        String abilityName = isDefinitive ? "Reaper's Scythe" : "Harvest Storm";
        int cooldownSec = isDefinitive ? 14 : 6;

        if (isOnCooldown(player, abilityName)) { sendCooldownMessage(player, abilityName); return; }
        setCooldown(player, abilityName, cooldownSec);

        if (!isDefinitive) {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.5f, 0.8f);
            Location center = player.getLocation().add(0, 1, 0);
            for (LivingEntity enemy : getNearbyEnemies(center, 5.0, player)) {
                dealAbilityDamage(player, enemy, 7.0, false);
                new BukkitRunnable() {
                    int bleedTicks = 0;
                    @Override public void run() {
                        if (bleedTicks >= 4 || enemy.isDead()) { cancel(); return; }
                        enemy.damage(6.0);
                        enemy.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, enemy.getLocation().add(0, 1, 0), 5, 0.3, 0.3, 0.3, 0);
                        bleedTicks++;
                    }
                }.runTaskTimer(plugin, 20L, 20L);
            }
            new BukkitRunnable() {
                int tick = 0;
                @Override public void run() {
                    if (tick >= 10) { cancel(); return; }
                    for (int i = 0; i < 12; i++) {
                        double angle = ((2 * Math.PI) / 12) * i + (tick * 0.6);
                        Location pLoc = center.clone().add(Math.cos(angle) * 4, 0, Math.sin(angle) * 4);
                        player.getWorld().spawnParticle(Particle.SWEEP_ATTACK, pLoc, 1, 0, 0, 0, 0);
                        player.getWorld().spawnParticle(Particle.CRIT, pLoc, 2, 0.1, 0.1, 0.1, 0.1);
                    }
                    tick++;
                }
            }.runTaskTimer(plugin, 0L, 1L);
            player.sendActionBar("\u00a7a\u2620 Harvest Storm!");

        } else {
            // REAPER'S SCYTHE — 3-phase revamp
            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.8f, 1.5f);
            player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_EMERGE, 1.0f, 0.5f);
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 2.0f);

            Location center = player.getLocation().clone();
            final double VORTEX_RADIUS = 10.0;
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 80, 0));

            new BukkitRunnable() {
                int tick = 0;
                double totalHealed = 0;
                int totalDamageDealt = 0;

                @Override public void run() {
                    if (tick > 60 || !player.isOnline()) {
                        cancel();
                        if (totalHealed > 0 || totalDamageDealt > 0) {
                            player.sendMessage("\u00a75\u00a7l\u2620 Reaper's Scythe \u00a7r\u00a75harvested "
                                    + "\u00a7c" + totalDamageDealt + " total damage \u00a75and healed "
                                    + "\u00a7a" + String.format("%.1f", totalHealed) + " HP\u00a75.");
                        }
                        return;
                    }

                    World world = center.getWorld();

                    // PHASE 1: DARK SUMMONING VORTEX (ticks 0-20)
                    if (tick <= 20) {
                        double spiralRadius = VORTEX_RADIUS * (1.0 - (tick / 25.0));
                        for (int arm = 0; arm < 3; arm++) {
                            double baseAngle = (2 * Math.PI / 3) * arm + (tick * 0.4);
                            for (double r = spiralRadius; r > 0; r -= 0.8) {
                                double angle = baseAngle + (spiralRadius - r) * 0.3;
                                Location pLoc = center.clone().add(Math.cos(angle) * r, 0.2, Math.sin(angle) * r);
                                world.spawnParticle(Particle.SOUL_FIRE_FLAME, pLoc, 1, 0.05, 0.1, 0.05, 0.005);
                                if (r > spiralRadius * 0.6) {
                                    world.spawnParticle(Particle.DUST, pLoc.clone().add(0, 0.5, 0), 1, 0.1, 0.2, 0.1, 0,
                                            new Particle.DustOptions(Color.fromRGB(30, 0, 50), 2.0f));
                                }
                            }
                        }
                        for (double y = 0; y < 3; y += 0.3) {
                            double cAngle = tick * 0.5 + y * 2;
                            double cR = 0.5 + y * 0.3;
                            world.spawnParticle(Particle.SOUL_FIRE_FLAME, center.clone().add(Math.cos(cAngle) * cR, y, Math.sin(cAngle) * cR), 1, 0, 0, 0, 0);
                        }
                        if (tick % 4 == 0) {
                            for (LivingEntity enemy : getNearbyEnemies(center, VORTEX_RADIUS, player)) {
                                Vector pull = center.toVector().subtract(enemy.getLocation().toVector()).normalize().multiply(0.6);
                                pull.setY(0.1);
                                enemy.setVelocity(enemy.getVelocity().add(pull));
                                enemy.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 2));
                                enemy.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1));
                                world.spawnParticle(Particle.DUST, enemy.getLocation().add(0, 1, 0), 5, 0.3, 0.5, 0.3, 0,
                                        new Particle.DustOptions(Color.fromRGB(20, 0, 30), 1.8f));
                            }
                        }
                        if (tick % 10 == 0) world.playSound(center, Sound.ENTITY_WARDEN_HEARTBEAT, 2.0f, 0.5f);
                    }

                    // PHASE 2: SOUL REAP CYCLONE (ticks 20-60)
                    if (tick >= 20 && tick <= 60) {
                        double blade1Angle = (tick - 20) * 0.45;
                        double blade2Angle = blade1Angle + Math.PI;
                        for (double bladeAngle : new double[]{blade1Angle, blade2Angle}) {
                            for (double r = 0.5; r <= 8.0; r += 0.4) {
                                double angle = bladeAngle + (r * 0.15);
                                Location pLoc = center.clone().add(Math.cos(angle) * r, 0.3 + Math.sin(r + tick * 0.2) * 0.5, Math.sin(angle) * r);
                                world.spawnParticle(Particle.SOUL_FIRE_FLAME, pLoc, 1, 0.02, 0.05, 0.02, 0.002);
                                if (r > 6.0) {
                                    // FIX: replaced DRAGON_BREATH with SOUL
                                    world.spawnParticle(Particle.SOUL, pLoc, 1, 0.05, 0.05, 0.05, 0.01);
                                    world.spawnParticle(Particle.ENCHANT, pLoc, 1, 0, 0.3, 0, 0.05);
                                }
                                if (r < 2.0) {
                                    world.spawnParticle(Particle.DUST, pLoc, 1, 0.1, 0.1, 0.1, 0,
                                            new Particle.DustOptions(Color.fromRGB(60, 0, 80), 1.5f));
                                }
                            }
                        }
                        if (tick % 2 == 0) {
                            for (int i = 0; i < 24; i++) {
                                double ringAngle = (2 * Math.PI / 24) * i + tick * 0.1;
                                double ringR = 8.0 + Math.sin(tick * 0.3 + i) * 0.5;
                                Location ringLoc = center.clone().add(Math.cos(ringAngle) * ringR, 0.1, Math.sin(ringAngle) * ringR);
                                world.spawnParticle(Particle.SOUL_FIRE_FLAME, ringLoc, 1, 0, 0.1, 0, 0.005);
                            }
                        }
                        for (double y = 0; y < 5; y += 0.5) {
                            world.spawnParticle(Particle.SOUL_FIRE_FLAME,
                                    center.clone().add(Math.sin(tick * 0.3 + y) * 0.3, y, Math.cos(tick * 0.3 + y) * 0.3), 1, 0, 0, 0, 0.01);
                        }
                        if (tick % 5 == 0) {
                            List<LivingEntity> enemies = getNearbyEnemies(center, VORTEX_RADIUS, player);
                            for (LivingEntity enemy : enemies) {
                                dealAbilityDamage(player, enemy, 12.0, true);
                                totalDamageDealt += 12;
                                enemy.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 80, 2));
                                enemy.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1));
                                world.spawnParticle(Particle.SOUL_FIRE_FLAME, enemy.getLocation().add(0, 1, 0), 8, 0.3, 0.5, 0.3, 0.02);
                                world.spawnParticle(Particle.DAMAGE_INDICATOR, enemy.getLocation().add(0, 1.5, 0), 3, 0.2, 0.2, 0.2, 0.1);
                                Vector pull = center.toVector().subtract(enemy.getLocation().toVector());
                                if (pull.lengthSquared() > 1) {
                                    pull = pull.normalize().multiply(0.3); pull.setY(0.05);
                                    enemy.setVelocity(enemy.getVelocity().add(pull));
                                }
                                double maxHp = player.getAttribute(Attribute.MAX_HEALTH).getValue();
                                double newHealth = Math.min(player.getHealth() + 3.0, maxHp);
                                player.setHealth(newHealth);
                                totalHealed += 3.0;
                            }
                            if (!enemies.isEmpty()) {
                                world.spawnParticle(Particle.HEART, player.getLocation().add(0, 2.2, 0), 3, 0.3, 0.3, 0.3, 0);
                                world.spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation().add(0, 1, 0), 5, 0.3, 0.5, 0.3, 0.05);
                            }
                            world.playSound(center, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.5f, 0.5f);
                        }
                        if (tick % 15 == 0) world.playSound(center, Sound.ENTITY_WARDEN_HEARTBEAT, 1.5f, 0.4f);
                        if (tick == 30) world.playSound(center, Sound.ENTITY_WITHER_AMBIENT, 0.5f, 1.5f);
                        if (tick == 45) world.playSound(center, Sound.ENTITY_WARDEN_SONIC_BOOM, 0.5f, 1.8f);
                    }

                    // PHASE 3: DEATH HARVEST EXPLOSION (tick 60)
                    if (tick == 60) {
                        world.spawnParticle(Particle.SOUL_FIRE_FLAME, center.clone().add(0, 1, 0), 200, 5, 3, 5, 0.15);
                        // FIX: replaced DRAGON_BREATH with SOUL
                        world.spawnParticle(Particle.SOUL, center.clone().add(0, 1, 0), 120, 6, 2, 6, 0.1);
                        world.spawnParticle(Particle.EXPLOSION_EMITTER, center.clone().add(0, 1, 0), 5, 1, 1, 1, 0);
                        world.spawnParticle(Particle.REVERSE_PORTAL, center.clone().add(0, 2, 0), 100, 4, 3, 4, 0.3);
                        world.spawnParticle(Particle.TOTEM_OF_UNDYING, center.clone().add(0, 1, 0), 80, 3, 2, 3, 0.4);
                        for (double y = 0; y < 15; y += 0.3) {
                            double beamR = Math.max(0, 2.0 - y * 0.12);
                            for (int i = 0; i < 4; i++) {
                                double bAngle = (2 * Math.PI / 4) * i + y * 0.5;
                                world.spawnParticle(Particle.SOUL_FIRE_FLAME, center.clone().add(Math.cos(bAngle) * beamR, y, Math.sin(bAngle) * beamR), 1, 0, 0, 0, 0.02);
                            }
                        }
                        world.playSound(center, Sound.ENTITY_WARDEN_SONIC_BOOM, 2.0f, 0.3f);
                        world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.5f);
                        world.playSound(center, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 2.0f, 0.6f);
                        for (LivingEntity enemy : getNearbyEnemies(center, VORTEX_RADIUS + 2, player)) {
                            dealAbilityDamage(player, enemy, 20.0, true);
                            totalDamageDealt += 20;
                            Vector launch = enemy.getLocation().toVector().subtract(center.toVector()).normalize().multiply(2.0);
                            launch.setY(1.0);
                            enemy.setVelocity(launch);
                            enemy.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 160, 2));
                            enemy.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 100, 0));
                            world.spawnParticle(Particle.SOUL_FIRE_FLAME, enemy.getLocation().add(0, 1, 0), 15, 0.3, 0.5, 0.3, 0.05);
                        }
                        new BukkitRunnable() {
                            int lingerTick = 0;
                            @Override public void run() {
                                if (lingerTick >= 40) { cancel(); return; }
                                double fade = 1.0 - (lingerTick / 40.0);
                                int count = (int)(15 * fade);
                                world.spawnParticle(Particle.SOUL_FIRE_FLAME, center.clone().add(0, 0.5, 0), count, 4 * fade, 1.5 * fade, 4 * fade, 0.02);
                                // FIX: replaced DRAGON_BREATH with SOUL
                                world.spawnParticle(Particle.SOUL, center.clone().add(0, 0.3, 0), count / 2, 3 * fade, 0.5, 3 * fade, 0.01);
                                lingerTick++;
                            }
                        }.runTaskTimer(plugin, 0L, 1L);
                    }

                    tick++;
                }
            }.runTaskTimer(plugin, 0L, 1L);

            player.sendActionBar("\u00a75\u00a7l\u2620 REAPER'S SCYTHE \u2620");
        }
    }

    // ================================================================
    // SPEAR — "Phantom Lunge" / "Spear of the Void"
    // ================================================================

    private void handleSpearAbility(Player player, ItemStack item, boolean isDefinitive) {
        String abilityName = isDefinitive ? "Spear of the Void" : "Phantom Lunge";
        int cooldownSec = isDefinitive ? 14 : 8;

        if (isOnCooldown(player, abilityName)) { sendCooldownMessage(player, abilityName); return; }
        setCooldown(player, abilityName, cooldownSec);

        if (!isDefinitive) {
            Vector direction = player.getLocation().getDirection().normalize();
            player.setVelocity(direction.clone().multiply(2.5));
            player.playSound(player.getLocation(), Sound.ENTITY_BREEZE_SHOOT, 1.5f, 1.2f);
            player.playSound(player.getLocation(), Sound.ENTITY_PHANTOM_FLAP, 1.0f, 1.5f);

            new BukkitRunnable() {
                int tick = 0;
                final Set<UUID> hit = new HashSet<>();
                @Override public void run() {
                    if (tick >= 10) { cancel(); return; }
                    Location loc = player.getLocation();
                    player.getWorld().spawnParticle(Particle.END_ROD, loc.clone().add(0, 1, 0), 8, 0.3, 0.5, 0.3, 0.02);
                    player.getWorld().spawnParticle(Particle.CRIT, loc.clone().add(0, 1, 0), 5, 0.4, 0.3, 0.4, 0.15);
                    player.getWorld().spawnParticle(Particle.DUST, loc.clone().add(0, 1, 0), 10, 0.2, 0.6, 0.2, 0,
                            new Particle.DustOptions(Color.fromRGB(180, 240, 255), 1.8f));
                    for (LivingEntity enemy : getNearbyEnemies(loc, 2.0, player)) {
                        if (hit.add(enemy.getUniqueId())) {
                            dealAbilityDamage(player, enemy, 8.0, false);
                            enemy.setVelocity(direction.clone().multiply(0.4).setY(0.3));
                            player.getWorld().spawnParticle(Particle.SWEEP_ATTACK, enemy.getLocation().add(0, 1, 0), 3, 0.2, 0.2, 0.2, 0);
                        }
                    }
                    tick++;
                }
            }.runTaskTimer(plugin, 0L, 1L);

            player.sendActionBar("\u00a7b Phantom Lunge!");

        } else {
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 2.0f);
            player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_THROW, 2.0f, 0.5f);

            Vector direction = player.getLocation().getDirection().normalize();
            Location start = player.getEyeLocation().clone();

            new BukkitRunnable() {
                int tick = 0;
                Location current = start.clone();
                final Set<UUID> hit = new HashSet<>();
                boolean detonated = false;

                @Override public void run() {
                    if (tick >= 30 || detonated) {
                        if (!detonated) detonate(current);
                        cancel();
                        return;
                    }
                    current.add(direction.clone().multiply(1.0));
                    if (current.getBlock().getType().isSolid()) {
                        detonate(current);
                        detonated = true;
                        cancel();
                        return;
                    }
                    // FIX: replaced DRAGON_BREATH with SOUL
                    player.getWorld().spawnParticle(Particle.SOUL, current, 8, 0.1, 0.1, 0.1, 0.02);
                    player.getWorld().spawnParticle(Particle.REVERSE_PORTAL, current, 5, 0.2, 0.2, 0.2, 0.1);
                    player.getWorld().spawnParticle(Particle.DUST, current, 5, 0.15, 0.15, 0.15, 0,
                            new Particle.DustOptions(Color.fromRGB(80, 0, 120), 2.0f));
                    for (Entity e : current.getWorld().getNearbyEntities(current, 1.5, 1.5, 1.5)) {
                        if (e instanceof LivingEntity le && !(e instanceof Player) && !(e instanceof ArmorStand)) {
                            if (hit.add(e.getUniqueId())) {
                                dealAbilityDamage(player, le, 18.0, true);
                                le.getWorld().spawnParticle(Particle.EXPLOSION, le.getLocation().add(0, 1, 0), 3, 0.2, 0.2, 0.2, 0);
                            }
                        }
                    }
                    tick++;
                }

                private void detonate(Location impactLoc) {
                    World world = impactLoc.getWorld();
                    world.spawnParticle(Particle.EXPLOSION_EMITTER, impactLoc, 5, 0, 0, 0, 0);
                    world.spawnParticle(Particle.REVERSE_PORTAL, impactLoc, 150, 3, 3, 3, 0.5);
                    // FIX: replaced DRAGON_BREATH with SOUL
                    world.spawnParticle(Particle.SOUL, impactLoc, 80, 4, 2, 4, 0.1);
                    world.spawnParticle(Particle.TOTEM_OF_UNDYING, impactLoc, 60, 2, 2, 2, 0.3);
                    world.playSound(impactLoc, Sound.ENTITY_WARDEN_SONIC_BOOM, 2.0f, 0.5f);
                    world.playSound(impactLoc, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.7f);
                    for (LivingEntity enemy : getNearbyEnemies(impactLoc, 5.0, player)) {
                        Vector pull = impactLoc.toVector().subtract(enemy.getLocation().toVector()).normalize().multiply(1.5);
                        pull.setY(0.4);
                        enemy.setVelocity(pull);
                        enemy.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1));
                        if (!hit.contains(enemy.getUniqueId())) {
                            dealAbilityDamage(player, enemy, 10.0, true);
                        }
                    }
                    new BukkitRunnable() {
                        int t = 0;
                        @Override public void run() {
                            if (t >= 40) { cancel(); return; }
                            world.spawnParticle(Particle.REVERSE_PORTAL, impactLoc, 15, 2, 0.5, 2, 0.1);
                            // FIX: replaced DRAGON_BREATH with SOUL
                            world.spawnParticle(Particle.SOUL, impactLoc, 5, 1.5, 0.3, 1.5, 0.02);
                            t++;
                        }
                    }.runTaskTimer(plugin, 0L, 2L);
                }
            }.runTaskTimer(plugin, 0L, 1L);

            player.sendActionBar("\u00a75\u00a7l SPEAR OF THE VOID ");
        }
    }

    // ================================================================
    // BOW — "Arrow Storm" / "Celestial Volley"
    // ================================================================

    private void handleBowAbility(Player player, ItemStack item, boolean isDefinitive) {
        String abilityName = isDefinitive ? "Celestial Volley" : "Arrow Storm";
        int cooldownSec = isDefinitive ? 14 : 8;

        if (isOnCooldown(player, abilityName)) { sendCooldownMessage(player, abilityName); return; }
        setCooldown(player, abilityName, cooldownSec);

        if (!isDefinitive) {
            player.playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1.5f, 1.3f);
            new BukkitRunnable() {
                int arrows = 0;
                @Override public void run() {
                    if (arrows >= 5 || !player.isOnline()) { cancel(); return; }
                    Arrow arrow = player.launchProjectile(Arrow.class);
                    arrow.setVelocity(player.getLocation().getDirection().multiply(2.5));
                    arrow.setDamage(6.0);
                    arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
                    arrow.setLifetimeTicks(1100);
                    arrow.setCritical(true);
                    player.getWorld().spawnParticle(Particle.CRIT, player.getEyeLocation(), 5, 0.2, 0.2, 0.2, 0.1);
                    player.playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 0.8f, 1.5f + (arrows * 0.1f));
                    arrows++;
                }
            }.runTaskTimer(plugin, 0L, 3L);
            player.sendActionBar("\u00a7e Arrow Storm!");

        } else {
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0f, 1.5f);
            player.playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 2.0f, 0.5f);

            Location target = player.getLocation().add(player.getLocation().getDirection().multiply(10));
            target.setY(player.getLocation().getY());

            player.getWorld().spawnParticle(Particle.FIREWORK, player.getLocation().add(0, 2, 0), 30, 0.5, 1, 0.5, 0.2);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.playSound(target, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 1.5f);
                for (int i = 0; i < 12; i++) {
                    final int index = i;
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        double offsetX = (Math.random() - 0.5) * 8;
                        double offsetZ = (Math.random() - 0.5) * 8;
                        Location spawnLoc = target.clone().add(offsetX, 25, offsetZ);
                        Arrow arrow = player.getWorld().spawnArrow(spawnLoc, new Vector(0, -3, 0), 2.0f, 0);
                        arrow.setShooter(player);
                        arrow.setDamage(10.0);
                        arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
                        arrow.setLifetimeTicks(1100);
                        arrow.setCritical(true);
                        arrow.setFireTicks(100);

                        new BukkitRunnable() {
                            int t = 0;
                            @Override public void run() {
                                if (t >= 30 || arrow.isDead() || arrow.isOnGround()) {
                                    if (arrow.isOnGround() || arrow.isDead()) {
                                        Location impactLoc = arrow.getLocation();
                                        player.getWorld().spawnParticle(Particle.EXPLOSION, impactLoc, 2, 0, 0, 0, 0);
                                        player.getWorld().spawnParticle(Particle.FLAME, impactLoc, 15, 1, 0.5, 1, 0.05);
                                        for (LivingEntity enemy : getNearbyEnemies(impactLoc, 3.0, player)) {
                                            dealAbilityDamage(player, enemy, 8.0, true);
                                        }
                                    }
                                    cancel(); return;
                                }
                                player.getWorld().spawnParticle(Particle.END_ROD, arrow.getLocation(), 3, 0.1, 0.1, 0.1, 0.02);
                                player.getWorld().spawnParticle(Particle.FIREWORK, arrow.getLocation(), 2, 0.05, 0.05, 0.05, 0.01);
                                t++;
                            }
                        }.runTaskTimer(plugin, 0L, 1L);
                    }, index * 2L);
                }
            }, 15L);

            player.sendActionBar("\u00a76\u00a7l CELESTIAL VOLLEY ");
        }
    }

    // ================================================================
    // CROSSBOW — "Chain Shot" / "Thunder Barrage"
    // ================================================================

    private void handleCrossbowAbility(Player player, ItemStack item, boolean isDefinitive) {
        String abilityName = isDefinitive ? "Thunder Barrage" : "Chain Shot";
        int cooldownSec = isDefinitive ? 15 : 8;

        if (isOnCooldown(player, abilityName)) { sendCooldownMessage(player, abilityName); return; }
        setCooldown(player, abilityName, cooldownSec);

        if (!isDefinitive) {
            player.playSound(player.getLocation(), Sound.ITEM_CROSSBOW_SHOOT, 1.5f, 1.0f);
            Vector baseDir = player.getLocation().getDirection().normalize();
            for (int bolt = 0; bolt < 3; bolt++) {
                final int boltIndex = bolt;
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    double spread = (boltIndex - 1) * 0.1;
                    Vector dir = baseDir.clone().rotateAroundY(spread);
                    Arrow arrow = player.launchProjectile(Arrow.class);
                    arrow.setVelocity(dir.multiply(3.0));
                    arrow.setDamage(8.0);
                    arrow.setPierceLevel(3);
                    arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
                    arrow.setLifetimeTicks(1100);
                    arrow.setCritical(true);
                    player.getWorld().spawnParticle(Particle.CRIT, player.getEyeLocation(), 5, 0.1, 0.1, 0.1, 0.2);
                    player.playSound(player.getLocation(), Sound.ITEM_CROSSBOW_SHOOT, 0.7f, 1.2f + (boltIndex * 0.15f));
                    new BukkitRunnable() {
                        int t = 0; boolean chained = false;
                        @Override public void run() {
                            if (t >= 40 || arrow.isDead() || chained) {
                                if ((arrow.isDead() || arrow.isOnGround()) && !chained) {
                                    chained = true;
                                    Location impactLoc = arrow.getLocation();
                                    for (LivingEntity enemy : getNearbyEnemies(impactLoc, 3.0, player)) {
                                        dealAbilityDamage(player, enemy, 4.0, false);
                                        enemy.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, enemy.getLocation().add(0, 1, 0), 8, 0.3, 0.3, 0.3, 0.1);
                                    }
                                }
                                cancel(); return;
                            }
                            player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, arrow.getLocation(), 2, 0.05, 0.05, 0.05, 0.01);
                            t++;
                        }
                    }.runTaskTimer(plugin, 0L, 1L);
                }, bolt * 4L);
            }
            player.sendActionBar("\u00a7b Chain Shot!");

        } else {
            player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.5f, 0.8f);
            player.playSound(player.getLocation(), Sound.ITEM_CROSSBOW_SHOOT, 2.0f, 0.5f);
            Vector baseDir = player.getLocation().getDirection().normalize();
            for (int bolt = 0; bolt < 6; bolt++) {
                final int boltIndex = bolt;
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    double spread = (boltIndex - 2.5) * 0.12;
                    Vector dir = baseDir.clone().rotateAroundY(spread);
                    Arrow arrow = player.launchProjectile(Arrow.class);
                    arrow.setVelocity(dir.multiply(3.5));
                    arrow.setDamage(12.0);
                    arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
                    arrow.setLifetimeTicks(1100);
                    arrow.setCritical(true);
                    arrow.setFireTicks(60);
                    player.playSound(player.getLocation(), Sound.ITEM_CROSSBOW_SHOOT, 0.6f, 0.8f + (boltIndex * 0.1f));
                    new BukkitRunnable() {
                        int t = 0; boolean exploded = false;
                        @Override public void run() {
                            if (t >= 60 || arrow.isDead() || exploded) {
                                if (!exploded) { exploded = true; explodeBolt(arrow.getLocation()); }
                                cancel(); return;
                            }
                            if (arrow.isOnGround()) { exploded = true; explodeBolt(arrow.getLocation()); cancel(); return; }
                            player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, arrow.getLocation(), 4, 0.1, 0.1, 0.1, 0.05);
                            player.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, arrow.getLocation(), 2, 0.05, 0.05, 0.05, 0.01);
                            t++;
                        }
                        private void explodeBolt(Location impactLoc) {
                            World world = impactLoc.getWorld();
                            world.spawnParticle(Particle.EXPLOSION, impactLoc, 3, 0.5, 0.5, 0.5, 0);
                            world.spawnParticle(Particle.ELECTRIC_SPARK, impactLoc, 30, 2, 1, 2, 0.2);
                            world.spawnParticle(Particle.FLAME, impactLoc, 20, 1.5, 0.5, 1.5, 0.05);
                            world.playSound(impactLoc, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1.0f, 1.0f);
                            for (LivingEntity enemy : getNearbyEnemies(impactLoc, 4.0, player)) {
                                dealAbilityDamage(player, enemy, 14.0, true);
                                Vector kb = enemy.getLocation().toVector().subtract(impactLoc.toVector()).normalize().multiply(0.8);
                                kb.setY(0.5); enemy.setVelocity(kb);
                                enemy.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 2));
                            }
                            arrow.remove();
                        }
                    }.runTaskTimer(plugin, 0L, 1L);
                }, bolt * 3L);
            }
            player.sendActionBar("\u00a7e\u00a7l THUNDER BARRAGE ");
        }
    }

    // ================================================================
    // TRIDENT — "Tidal Surge" / "Poseidon's Wrath"
    // ================================================================

    private void handleTridentAbility(Player player, ItemStack item, boolean isDefinitive) {
        String abilityName = isDefinitive ? "Poseidon's Wrath" : "Tidal Surge";
        int cooldownSec = isDefinitive ? 14 : 8;

        if (isOnCooldown(player, abilityName)) { sendCooldownMessage(player, abilityName); return; }
        setCooldown(player, abilityName, cooldownSec);

        if (!isDefinitive) {
            player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_RIPTIDE_3, 1.5f, 1.0f);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_SPLASH_HIGH_SPEED, 1.0f, 0.8f);
            Location center = player.getLocation();
            Vector forward = center.getDirection().normalize();
            for (LivingEntity enemy : getNearbyEnemies(center, 8.0, player)) {
                Vector toEnemy = enemy.getLocation().toVector().subtract(center.toVector()).normalize();
                if (forward.dot(toEnemy) > 0.3) {
                    dealAbilityDamage(player, enemy, 8.0, false);
                    Vector knockback = toEnemy.multiply(1.5); knockback.setY(0.5);
                    enemy.setVelocity(knockback);
                    enemy.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 0));
                    enemy.getWorld().spawnParticle(Particle.SPLASH, enemy.getLocation().add(0, 1, 0), 15, 0.3, 0.3, 0.3, 0.1);
                }
            }
            for (int i = 0; i < 30; i++) {
                double dist = 1 + Math.random() * 7;
                double sp = (Math.random() - 0.5) * 1.2;
                Vector offset = forward.clone().multiply(dist).add(new Vector(sp, Math.random() * 1.5, sp));
                player.getWorld().spawnParticle(Particle.SPLASH, center.clone().add(offset), 3, 0.2, 0.3, 0.2, 0.05);
                player.getWorld().spawnParticle(Particle.DRIPPING_WATER, center.clone().add(offset), 2, 0.1, 0.2, 0.1, 0);
            }
            player.sendActionBar("\u00a7b Tidal Surge!");

        } else {
            player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_THUNDER, 2.0f, 0.5f);
            player.playSound(player.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 0.5f, 1.5f);
            Location center = player.getLocation();
            Vector forward = center.getDirection().normalize();
            for (int wave = 0; wave < 3; wave++) {
                final int waveNum = wave;
                final double waveDamage = 16.0 - (wave * 3.0);
                final double waveRadius = 4.0 + (wave * 3.0);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    Location waveCenter = center.clone().add(forward.clone().multiply(waveNum * 3));
                    for (int i = 0; i < 40; i++) {
                        double angle = (2 * Math.PI / 40) * i;
                        double r = waveRadius * (0.5 + Math.random() * 0.5);
                        Location pLoc = waveCenter.clone().add(Math.cos(angle) * r, 0.3 + Math.random() * 1.5, Math.sin(angle) * r);
                        player.getWorld().spawnParticle(Particle.SPLASH, pLoc, 5, 0.2, 0.3, 0.2, 0.05);
                        player.getWorld().spawnParticle(Particle.DRIPPING_WATER, pLoc, 3, 0.1, 0.5, 0.1, 0);
                    }
                    player.getWorld().spawnParticle(Particle.EXPLOSION, waveCenter, 2, 1, 0.5, 1, 0);
                    player.getWorld().playSound(waveCenter, Sound.ENTITY_PLAYER_SPLASH_HIGH_SPEED, 1.5f, 0.7f + (waveNum * 0.15f));
                    for (LivingEntity enemy : getNearbyEnemies(waveCenter, waveRadius, player)) {
                        dealAbilityDamage(player, enemy, waveDamage, true);
                        Vector kb = enemy.getLocation().toVector().subtract(waveCenter.toVector()).normalize().multiply(1.0 + waveNum * 0.3);
                        kb.setY(0.6); enemy.setVelocity(kb);
                        enemy.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 1));
                    }
                }, wave * 10L);
            }
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Location strikeTarget = center.clone().add(forward.clone().multiply(8));
                player.getWorld().strikeLightningEffect(strikeTarget);
                player.getWorld().playSound(strikeTarget, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2.0f, 0.8f);
                for (LivingEntity enemy : getNearbyEnemies(strikeTarget, 4.0, player)) {
                    dealAbilityDamage(player, enemy, 10.0, true);
                    enemy.setFireTicks(60);
                }
                player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, strikeTarget, 50, 2, 2, 2, 0.3);
            }, 35L);
            player.sendActionBar("\u00a71\u00a7l POSEIDON'S WRATH ");
        }
    }

    // ================================================================
    // MACE — "Ground Slam" / "Meteor Strike" (FIXED — invulnerability)
    // ================================================================

    private void handleMaceAbility(Player player, ItemStack item, boolean isDefinitive) {
        String abilityName = isDefinitive ? "Meteor Strike" : "Ground Slam";
        int cooldownSec = isDefinitive ? 15 : 8;

        if (isOnCooldown(player, abilityName)) { sendCooldownMessage(player, abilityName); return; }
        setCooldown(player, abilityName, cooldownSec);

        if (!isDefinitive) {
            player.playSound(player.getLocation(), Sound.ITEM_MACE_SMASH_GROUND, 2.0f, 1.0f);
            Location center = player.getLocation();
            for (LivingEntity enemy : getNearbyEnemies(center, 6.0, player)) {
                dealAbilityDamage(player, enemy, 10.0, false);
                enemy.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 2));
                enemy.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 60, 1));
            }
            for (int i = 0; i < 60; i++) {
                double angle = Math.random() * 2 * Math.PI;
                double r = Math.random() * 5;
                Location pLoc = center.clone().add(Math.cos(angle) * r, 0.1, Math.sin(angle) * r);
                player.getWorld().spawnParticle(Particle.BLOCK, pLoc, 3, 0.2, 0.3, 0.2, 0.1, Material.STONE.createBlockData());
                player.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, pLoc, 1, 0, 0.3, 0, 0.02);
            }
            player.getWorld().spawnParticle(Particle.EXPLOSION, center, 3, 1, 0.5, 1, 0);
            player.sendActionBar("\u00a76 Ground Slam!");

        } else {
            // METEOR STRIKE — Player gets full invulnerability before launch
            player.playSound(player.getLocation(), Sound.ITEM_MACE_SMASH_GROUND_HEAVY, 2.0f, 0.5f);
            player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1.0f, 1.5f);

            // Resistance 255 = complete immunity
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 200, 255, false, false, true));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 60, 0, false, false, false));
            player.setVelocity(new Vector(0, 3.5, 0));

            // Ascent trail
            new BukkitRunnable() {
                int ascendTick = 0;
                @Override public void run() {
                    if (ascendTick >= 20 || !player.isOnline()) { cancel(); return; }
                    Location loc = player.getLocation();
                    player.getWorld().spawnParticle(Particle.FLAME, loc, 10, 0.3, 0.5, 0.3, 0.05);
                    player.getWorld().spawnParticle(Particle.LAVA, loc, 3, 0.2, 0.2, 0.2, 0);
                    player.getWorld().spawnParticle(Particle.DUST, loc, 8, 0.4, 0.6, 0.4, 0,
                            new Particle.DustOptions(Color.fromRGB(255, 100, 0), 2.0f));
                    ascendTick++;
                }
            }.runTaskTimer(plugin, 0L, 1L);

            // After ascent, slam down
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.removePotionEffect(PotionEffectType.SLOW_FALLING);
                player.setVelocity(new Vector(0, -4.0, 0));
                player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.7f, 2.0f);

                new BukkitRunnable() {
                    int descendTick = 0;
                    Location launchLoc = player.getLocation().clone();
                    boolean landed = false;

                    @Override public void run() {
                        if (descendTick >= 60 || landed || !player.isOnline()) {
                            if (!landed) performImpact(player.getLocation());
                            cancel(); return;
                        }
                        Location loc = player.getLocation();
                        player.getWorld().spawnParticle(Particle.FLAME, loc, 15, 0.4, 0.8, 0.4, 0.08);
                        player.getWorld().spawnParticle(Particle.LAVA, loc, 5, 0.3, 0.3, 0.3, 0);
                        player.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, loc, 8, 0.3, 1.0, 0.3, 0.05);
                        player.getWorld().spawnParticle(Particle.DUST, loc, 10, 0.5, 0.8, 0.5, 0,
                                new Particle.DustOptions(Color.fromRGB(255, 60, 0), 2.5f));
                        if (player.isOnGround() || (descendTick > 3 && player.getVelocity().getY() > -0.1)) {
                            landed = true;
                            performImpact(loc);
                            cancel(); return;
                        }
                        descendTick++;
                    }

                    private void performImpact(Location impactLoc) {
                        World world = impactLoc.getWorld();
                        double fallDist = Math.max(0, launchLoc.getY() - impactLoc.getY());
                        double baseDamage = Math.min(35.0, 15.0 + fallDist * 0.5);

                        world.playSound(impactLoc, Sound.ITEM_MACE_SMASH_GROUND_HEAVY, 2.0f, 0.5f);
                        world.playSound(impactLoc, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.6f);
                        world.playSound(impactLoc, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 2.0f, 0.7f);
                        world.playSound(impactLoc, Sound.ENTITY_WARDEN_SONIC_BOOM, 1.5f, 0.8f);

                        world.spawnParticle(Particle.EXPLOSION_EMITTER, impactLoc, 5, 1, 0.5, 1, 0);
                        world.spawnParticle(Particle.LAVA, impactLoc, 80, 5, 1, 5, 0.3);
                        world.spawnParticle(Particle.FLAME, impactLoc, 100, 6, 2, 6, 0.15);
                        world.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, impactLoc, 50, 4, 3, 4, 0.1);
                        world.spawnParticle(Particle.TOTEM_OF_UNDYING, impactLoc, 60, 3, 2, 3, 0.3);

                        // Shockwave rings
                        new BukkitRunnable() {
                            int ringTick = 0;
                            @Override public void run() {
                                if (ringTick >= 15) { cancel(); return; }
                                double ringR = ringTick * 0.8;
                                for (int i = 0; i < 30; i++) {
                                    double angle = (2 * Math.PI / 30) * i;
                                    Location ringLoc = impactLoc.clone().add(Math.cos(angle) * ringR, 0.2, Math.sin(angle) * ringR);
                                    world.spawnParticle(Particle.FLAME, ringLoc, 1, 0, 0.1, 0, 0.01);
                                    world.spawnParticle(Particle.BLOCK, ringLoc, 2, 0.1, 0.2, 0.1, 0.05, Material.STONE.createBlockData());
                                }
                                ringTick++;
                            }
                        }.runTaskTimer(plugin, 0L, 1L);

                        for (LivingEntity enemy : getNearbyEnemies(impactLoc, 12.0, player)) {
                            double dist = enemy.getLocation().distance(impactLoc);
                            double distanceFactor = 1.0 - (dist / 12.0) * 0.4;
                            dealAbilityDamage(player, enemy, baseDamage * distanceFactor, true);
                            Vector kb = enemy.getLocation().toVector().subtract(impactLoc.toVector()).normalize().multiply(2.0);
                            kb.setY(1.0); enemy.setVelocity(kb);
                            enemy.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 3));
                            enemy.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 80, 2));
                            enemy.setFireTicks(60);
                            world.spawnParticle(Particle.EXPLOSION, enemy.getLocation().add(0, 1, 0), 2, 0.3, 0.3, 0.3, 0);
                        }

                        // Remove invulnerability 1 second after landing
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            player.removePotionEffect(PotionEffectType.RESISTANCE);
                        }, 20L);
                    }
                }.runTaskTimer(plugin, 0L, 1L);
            }, 25L);

            player.sendActionBar("\u00a76\u00a7l METEOR STRIKE ");
        }
    }
}

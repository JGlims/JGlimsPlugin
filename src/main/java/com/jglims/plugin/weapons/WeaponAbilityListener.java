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

public class WeaponAbilityListener implements Listener {

    private final JGlimsPlugin plugin;
    private final ConfigManager config;
    private final CustomEnchantManager enchantManager;
    private final SuperToolManager superToolManager;
    private final SpearManager spearManager;
    private final BattleShovelManager battleShovelManager;

    // Cooldown tracking: Player UUID -> Ability Name -> Expiry timestamp
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    // PDC key for super tool tier (shared with SuperToolManager)
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
    // UTILITY: Get super tier from any weapon (spear or general super tool)
    // ============================================================

    private int getSuperTier(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return 0;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        // Check spear super tier first
        int spearTier = pdc.getOrDefault(spearManager.getSuperTierKey(), PersistentDataType.INTEGER, 0);
        if (spearTier > 0) return spearTier;
        // Check general super tool tier
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
    // SWORD — "Dash Strike" (Diamond) / "Dimensional Cleave" (Netherite)
    // ================================================================

    private void handleSwordAbility(Player player, ItemStack item, boolean isDefinitive) {
        String abilityName = isDefinitive ? "Dimensional Cleave" : "Dash Strike";
        int cooldownSec = isDefinitive ? 12 : 6;

        if (isOnCooldown(player, abilityName)) {
            sendCooldownMessage(player, abilityName);
            return;
        }
        setCooldown(player, abilityName, cooldownSec);

        if (!isDefinitive) {
            player.setVelocity(player.getLocation().getDirection().multiply(1.8));
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.5f, 1.2f);

            new BukkitRunnable() {
                int ticks = 0;
                @Override
                public void run() {
                    if (ticks >= 8) { cancel(); return; }
                    Location loc = player.getLocation();
                    player.getWorld().spawnParticle(Particle.SWEEP_ATTACK, loc.add(0, 1, 0), 3, 0.5, 0.3, 0.5, 0);
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
                @Override
                public void run() {
                    if (ticks >= 15) { cancel(); return; }
                    double radius = 2.0 + (ticks * 0.6);
                    for (int i = 0; i < 40; i++) {
                        double angle = (2 * Math.PI / 40) * i;
                        double x = Math.cos(angle) * radius;
                        double z = Math.sin(angle) * radius;
                        Location particleLoc = center.clone().add(x, 0.5, z);
                        player.getWorld().spawnParticle(Particle.DRAGON_BREATH, particleLoc, 1, 0, 0, 0, 0);
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
    // AXE — "Bloodthirst" (Diamond) / "Ragnarok Cleave" (Netherite)
    // ================================================================

    private void handleAxeAbility(Player player, ItemStack item, boolean isDefinitive) {
        String abilityName = isDefinitive ? "Ragnarok Cleave" : "Bloodthirst";
        int cooldownSec = isDefinitive ? 15 : 8;

        if (isOnCooldown(player, abilityName)) {
            sendCooldownMessage(player, abilityName);
            return;
        }
        setCooldown(player, abilityName, cooldownSec);

        if (!isDefinitive) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 100, 1));
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 100, 0));
            player.playSound(player.getLocation(), Sound.ENTITY_RAVAGER_ROAR, 0.8f, 1.2f);

            ItemMeta meta = item.getItemMeta();
            meta.getPersistentDataContainer().set(
                    new NamespacedKey(plugin, "bloodthirst_active"),
                    PersistentDataType.LONG,
                    System.currentTimeMillis() + 5000L
            );
            item.setItemMeta(meta);

            new BukkitRunnable() {
                int ticks = 0;
                @Override
                public void run() {
                    if (ticks >= 50) { cancel(); return; }
                    Location loc = player.getLocation().add(0, 1, 0);
                    player.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, loc, 4, 0.5, 0.5, 0.5, 0.1);
                    player.getWorld().spawnParticle(Particle.DUST, loc, 6,
                            0.6, 0.6, 0.6, 0,
                            new Particle.DustOptions(Color.fromRGB(180, 0, 0), 1.5f));
                    ticks++;
                }
            }.runTaskTimer(plugin, 0L, 2L);

            player.sendActionBar("\u00a7c\u00a7l BLOODTHIRST ACTIVE \u00a7c\u00a7l");

        } else {
            player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1.5f, 0.7f);
            player.playSound(player.getLocation(), Sound.ITEM_MACE_SMASH_GROUND_HEAVY, 2.0f, 0.8f);

            Location center = player.getLocation();

            new BukkitRunnable() {
                int ticks = 0;
                @Override
                public void run() {
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

            player.sendActionBar("\u00a74\u00a7l RAGNAROK CLEAVE ");
        }
    }

    // ================================================================
    // PICKAXE — "Ore Pulse" (Diamond) / "Seismic Resonance" (Netherite)
    // ================================================================

    private void handlePickaxeAbility(Player player, ItemStack item, boolean isDefinitive) {
        String abilityName = isDefinitive ? "Seismic Resonance" : "Ore Pulse";
        int cooldownSec = isDefinitive ? 18 : 10;

        if (isOnCooldown(player, abilityName)) {
            sendCooldownMessage(player, abilityName);
            return;
        }
        setCooldown(player, abilityName, cooldownSec);

        int oreRadius = isDefinitive ? config.getOreDetectRadiusNetherite() : config.getOreDetectRadiusDiamond();
        int debrisRadius = isDefinitive
                ? config.getOreDetectAncientDebrisRadiusNetherite()
                : config.getOreDetectAncientDebrisRadiusDiamond();
        int durationTicks = config.getOreDetectDurationTicks();

        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, isDefinitive ? 0.5f : 1.0f);
        if (isDefinitive) {
            player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_HEARTBEAT, 1.5f, 0.6f);
        }

        Location playerLoc = player.getLocation();
        World world = player.getWorld();
        Map<Location, OreType> foundOres = new LinkedHashMap<>();

        int maxRadius = Math.max(oreRadius, debrisRadius);

        for (int x = -maxRadius; x <= maxRadius; x++) {
            for (int y = -maxRadius; y <= maxRadius; y++) {
                for (int z = -maxRadius; z <= maxRadius; z++) {
                    Block block = world.getBlockAt(
                            playerLoc.getBlockX() + x,
                            playerLoc.getBlockY() + y,
                            playerLoc.getBlockZ() + z
                    );
                    double distSq = x * x + y * y + z * z;
                    OreType oreType = classifyOre(block.getType());

                    if (oreType == null) continue;

                    if (oreType == OreType.ANCIENT_DEBRIS) {
                        if (distSq <= debrisRadius * debrisRadius) {
                            foundOres.put(block.getLocation(), oreType);
                        }
                    } else {
                        if (distSq <= oreRadius * oreRadius) {
                            foundOres.put(block.getLocation(), oreType);
                        }
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
            if (debrisCount > 0) {
                msg += " \u00a77| \u00a74\u00a7l" + debrisCount + " Ancient Debris!";
            }
            player.sendActionBar(msg);
            if (debrisCount > 0) {
                player.sendMessage("\u00a74\u00a7l " + debrisCount + " Ancient Debris detected within " + debrisRadius + " blocks!");
            }
        }

        new BukkitRunnable() {
            int tick = 0;
            @Override
            public void run() {
                if (tick >= 5) { cancel(); return; }
                double r = tick * (isDefinitive ? 3.0 : 2.0);
                for (int i = 0; i < 20; i++) {
                    double angle = Math.random() * 2 * Math.PI;
                    Location pLoc = playerLoc.clone().add(Math.cos(angle) * r, 0.5, Math.sin(angle) * r);
                    world.spawnParticle(
                            isDefinitive ? Particle.SOUL_FIRE_FLAME : Particle.ENCHANT,
                            pLoc, 2, 0, 0.3, 0, 0.05
                    );
                }
                tick++;
            }
        }.runTaskTimer(plugin, 0L, 3L);

        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= durationTicks || !player.isOnline()) { cancel(); return; }
                for (Map.Entry<Location, OreType> entry : foundOres.entrySet()) {
                    Location oreLoc = entry.getKey().clone().add(0.5, 0.5, 0.5);
                    OreType type = entry.getValue();
                    Particle.DustOptions dust = type.getDustOptions();

                    if (ticks % 5 == 0) {
                        player.spawnParticle(Particle.DUST, oreLoc, 3, 0.2, 0.2, 0.2, 0, dust);
                        if (type == OreType.ANCIENT_DEBRIS) {
                            player.spawnParticle(Particle.FLAME, oreLoc, 1, 0.1, 0.1, 0.1, 0.01);
                            if (ticks % 20 == 0) {
                                player.spawnParticle(Particle.SOUL_FIRE_FLAME, oreLoc, 5, 0.3, 0.3, 0.3, 0.02);
                            }
                        }
                    }
                }
                ticks++;
            }
        }.runTaskTimer(plugin, 5L, 1L);
    }

    private enum OreType {
        COAL(Color.fromRGB(50, 50, 50)),
        IRON(Color.fromRGB(210, 150, 100)),
        COPPER(Color.fromRGB(180, 100, 50)),
        GOLD(Color.fromRGB(255, 215, 0)),
        REDSTONE(Color.fromRGB(255, 0, 0)),
        LAPIS(Color.fromRGB(30, 30, 200)),
        EMERALD(Color.fromRGB(0, 200, 50)),
        DIAMOND(Color.fromRGB(100, 230, 255)),
        ANCIENT_DEBRIS(Color.fromRGB(100, 50, 20)),
        NETHER_GOLD(Color.fromRGB(255, 200, 50)),
        NETHER_QUARTZ(Color.fromRGB(240, 240, 230));

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
    // SHOVEL — "Earthen Wall" (Diamond) / "Tectonic Upheaval" (Netherite)
    // ================================================================

    private void handleShovelAbility(Player player, ItemStack item, boolean isDefinitive) {
        String abilityName = isDefinitive ? "Tectonic Upheaval" : "Earthen Wall";
        int cooldownSec = isDefinitive ? 14 : 8;

        if (isOnCooldown(player, abilityName)) {
            sendCooldownMessage(player, abilityName);
            return;
        }
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
                player.getWorld().spawnParticle(Particle.BLOCK, pLoc, 5, 0.3, 0.5, 0.3, 0.1,
                        Material.DIRT.createBlockData());
            }

            player.sendActionBar("\u00a7e Earthen Wall \u2014 Enemies launched!");

        } else {
            player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_EMERGE, 2.0f, 0.5f);
            player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1.0f, 0.3f);

            Location center = player.getLocation();
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 160, 2));

            new BukkitRunnable() {
                int ticks = 0;
                @Override
                public void run() {
                    if (ticks >= 30) { cancel(); return; }
                    double radius = ticks * 0.4;
                    for (int i = 0; i < 25 + ticks; i++) {
                        double angle = Math.random() * 2 * Math.PI;
                        double r = Math.random() * radius;
                        Location pLoc = center.clone().add(Math.cos(angle) * r, 0.1, Math.sin(angle) * r);
                        player.getWorld().spawnParticle(Particle.BLOCK, pLoc, 3, 0.1, 0.8, 0.1, 0.3,
                                Material.NETHERRACK.createBlockData());
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
    // SICKLE (HOE) — "Harvest Storm" (Diamond) / "Reaper's Scythe" (Netherite)
    // ================================================================

    private void handleSickleAbility(Player player, ItemStack item, boolean isDefinitive) {
        String abilityName = isDefinitive ? "Reaper's Scythe" : "Harvest Storm";
        int cooldownSec = isDefinitive ? 14 : 6;

        if (isOnCooldown(player, abilityName)) {
            sendCooldownMessage(player, abilityName);
            return;
        }
        setCooldown(player, abilityName, cooldownSec);

        if (!isDefinitive) {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.5f, 0.8f);

            Location center = player.getLocation().add(0, 1, 0);
            for (LivingEntity enemy : getNearbyEnemies(center, 5.0, player)) {
                dealAbilityDamage(player, enemy, 7.0, false);

                new BukkitRunnable() {
                    int bleedTicks = 0;
                    @Override
                    public void run() {
                        if (bleedTicks >= 4 || enemy.isDead()) { cancel(); return; }
                        enemy.damage(2.0);
                        enemy.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, enemy.getLocation().add(0, 1, 0), 5, 0.3, 0.3, 0.3, 0);
                        bleedTicks++;
                    }
                }.runTaskTimer(plugin, 20L, 20L);
            }

            new BukkitRunnable() {
                int tick = 0;
                @Override
                public void run() {
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

            player.sendActionBar("\u00a7a Harvest Storm!");

        } else {
            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.7f, 1.5f);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 2.0f, 0.3f);

            Location center = player.getLocation().add(0, 1, 0);

            new BukkitRunnable() {
                int tick = 0;
                double totalHealed = 0;
                @Override
                public void run() {
                    if (tick >= 40) {
                        cancel();
                        if (totalHealed > 0) {
                            player.sendMessage("\u00a75 Reaper's Scythe healed \u00a7c" + String.format("%.1f", totalHealed) + " HP \u00a75from enemies.");
                        }
                        return;
                    }

                    double angle1 = tick * 0.35;
                    double angle2 = angle1 + Math.PI;
                    double radius = 7.0;

                    for (double a : new double[]{angle1, angle2}) {
                        for (double r = 1; r <= radius; r += 0.5) {
                            Location pLoc = center.clone().add(Math.cos(a) * r, 0, Math.sin(a) * r);
                            player.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, pLoc, 1, 0, 0, 0, 0);
                            if (r > radius - 1) {
                                player.getWorld().spawnParticle(Particle.DRAGON_BREATH, pLoc, 2, 0.1, 0.1, 0.1, 0);
                            }
                        }
                    }

                    if (tick % 4 == 0) {
                        for (LivingEntity enemy : getNearbyEnemies(center, radius, player)) {
                            dealAbilityDamage(player, enemy, 10.0, true);
                            enemy.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 80, 1));

                            double heal = 1.5;
                            double newHealth = Math.min(player.getHealth() + heal,
                                    player.getAttribute(Attribute.MAX_HEALTH).getValue());
                            player.setHealth(newHealth);
                            totalHealed += heal;
                            player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0, 2, 0), 2, 0.3, 0.3, 0.3, 0);
                        }
                    }
                    tick++;
                }
            }.runTaskTimer(plugin, 0L, 1L);

            player.sendActionBar("\u00a75\u00a7l REAPER'S SCYTHE ");
        }
    }

    // ================================================================
    // SPEAR — "Phantom Lunge" (Diamond) / "Spear of the Void" (Netherite)
    // ================================================================

    private void handleSpearAbility(Player player, ItemStack item, boolean isDefinitive) {
        String abilityName = isDefinitive ? "Spear of the Void" : "Phantom Lunge";
        int cooldownSec = isDefinitive ? 14 : 8;

        if (isOnCooldown(player, abilityName)) {
            sendCooldownMessage(player, abilityName);
            return;
        }
        setCooldown(player, abilityName, cooldownSec);

        if (!isDefinitive) {
            Vector direction = player.getLocation().getDirection().normalize();
            player.setVelocity(direction.clone().multiply(2.5));
            player.playSound(player.getLocation(), Sound.ENTITY_BREEZE_SHOOT, 1.5f, 1.2f);
            player.playSound(player.getLocation(), Sound.ENTITY_PHANTOM_FLAP, 1.0f, 1.5f);

            new BukkitRunnable() {
                int tick = 0;
                final Set<UUID> hit = new HashSet<>();
                @Override
                public void run() {
                    if (tick >= 10) { cancel(); return; }
                    Location loc = player.getLocation();

                    player.getWorld().spawnParticle(Particle.END_ROD, loc.clone().add(0, 1, 0), 8, 0.3, 0.5, 0.3, 0.02);
                    player.getWorld().spawnParticle(Particle.CRIT, loc.clone().add(0, 1, 0), 5, 0.4, 0.3, 0.4, 0.15);
                    player.getWorld().spawnParticle(Particle.DUST, loc.clone().add(0, 1, 0), 10,
                            0.2, 0.6, 0.2, 0,
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

                @Override
                public void run() {
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

                    player.getWorld().spawnParticle(Particle.DRAGON_BREATH, current, 8, 0.1, 0.1, 0.1, 0.02);
                    player.getWorld().spawnParticle(Particle.REVERSE_PORTAL, current, 5, 0.2, 0.2, 0.2, 0.1);
                    player.getWorld().spawnParticle(Particle.DUST, current, 5,
                            0.15, 0.15, 0.15, 0,
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
                    world.spawnParticle(Particle.DRAGON_BREATH, impactLoc, 80, 4, 2, 4, 0.1);
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
                        @Override
                        public void run() {
                            if (t >= 40) { cancel(); return; }
                            world.spawnParticle(Particle.REVERSE_PORTAL, impactLoc, 15, 2, 0.5, 2, 0.1);
                            world.spawnParticle(Particle.DRAGON_BREATH, impactLoc, 5, 1.5, 0.3, 1.5, 0.02);
                            t++;
                        }
                    }.runTaskTimer(plugin, 0L, 2L);
                }
            }.runTaskTimer(plugin, 0L, 1L);

            player.sendActionBar("\u00a75\u00a7l SPEAR OF THE VOID ");
        }
    }

    // ================================================================
    // BOW — "Arrow Storm" (Diamond) / "Celestial Volley" (Netherite)
    // ================================================================

    private void handleBowAbility(Player player, ItemStack item, boolean isDefinitive) {
        String abilityName = isDefinitive ? "Celestial Volley" : "Arrow Storm";
        int cooldownSec = isDefinitive ? 14 : 8;

        if (isOnCooldown(player, abilityName)) {
            sendCooldownMessage(player, abilityName);
            return;
        }
        setCooldown(player, abilityName, cooldownSec);

        if (!isDefinitive) {
            // DIAMOND: Arrow Storm — Rapid-fire 5 arrows in quick succession
            player.playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1.5f, 1.3f);

            new BukkitRunnable() {
                int arrows = 0;
                @Override
                public void run() {
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
            // NETHERITE DEFINITIVE: Celestial Volley — Launch 12 arrows skyward that rain down
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0f, 1.5f);
            player.playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 2.0f, 0.5f);

            Location target = player.getLocation().add(player.getLocation().getDirection().multiply(10));
            target.setY(player.getLocation().getY());

            // Visual launch effect
            player.getWorld().spawnParticle(Particle.FIREWORK, player.getLocation().add(0, 2, 0), 30, 0.5, 1, 0.5, 0.2);

            // Delayed rain of arrows from the sky
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.playSound(target, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 1.5f);

                for (int i = 0; i < 12; i++) {
                    final int index = i;
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        double offsetX = (Math.random() - 0.5) * 8;
                        double offsetZ = (Math.random() - 0.5) * 8;
                        Location spawnLoc = target.clone().add(offsetX, 30, offsetZ);

                        Arrow arrow = player.getWorld().spawnArrow(spawnLoc, new Vector(0, -1, 0), 2.0f, 0);
                        arrow.setShooter(player);
                        arrow.setDamage(10.0);
                        arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
                        arrow.setLifetimeTicks(1100);
                        arrow.setCritical(true);
                        arrow.setFireTicks(100);

                        player.getWorld().spawnParticle(Particle.FIREWORK, spawnLoc, 5, 0.2, 0.2, 0.2, 0.1);

                        // Track each arrow for AoE impact
                        new BukkitRunnable() {
                            int ticks = 0;
                            @Override
                            public void run() {
                                if (ticks >= 60 || arrow.isDead() || arrow.isOnGround()) {
                                    if (!arrow.isDead()) {
                                        Location hitLoc = arrow.getLocation();
                                        for (LivingEntity enemy : getNearbyEnemies(hitLoc, 3.0, player)) {
                                            dealAbilityDamage(player, enemy, 8.0, true);
                                        }
                                        player.getWorld().spawnParticle(Particle.EXPLOSION, hitLoc, 1, 0, 0, 0, 0);
                                        player.getWorld().spawnParticle(Particle.FLAME, hitLoc, 15, 1.5, 0.5, 1.5, 0.05);
                                    }
                                    cancel();
                                    return;
                                }
                                // Trail particles
                                player.getWorld().spawnParticle(Particle.FLAME, arrow.getLocation(), 2, 0, 0, 0, 0.02);
                                player.getWorld().spawnParticle(Particle.CRIT, arrow.getLocation(), 3, 0.1, 0.1, 0.1, 0.05);
                                ticks++;
                            }
                        }.runTaskTimer(plugin, 0L, 1L);

                    }, index * 2L);
                }
            }, 20L);

            player.sendActionBar("\u00a76\u00a7l CELESTIAL VOLLEY ");
        }
    }

    // ================================================================
    // CROSSBOW — "Chain Shot" (Diamond) / "Thunder Barrage" (Netherite)
    // FIX #1: Replaced Sound.BLOCK_LIGHTNING_ROD_TOGGLE_ON with
    //         Sound.ENTITY_LIGHTNING_BOLT_IMPACT (confirmed exists in 1.21.11)
    // ================================================================

    private void handleCrossbowAbility(Player player, ItemStack item, boolean isDefinitive) {
        String abilityName = isDefinitive ? "Thunder Barrage" : "Chain Shot";
        int cooldownSec = isDefinitive ? 15 : 8;

        if (isOnCooldown(player, abilityName)) {
            sendCooldownMessage(player, abilityName);
            return;
        }
        setCooldown(player, abilityName, cooldownSec);

        if (!isDefinitive) {
            // DIAMOND: Chain Shot — Fire 3 piercing bolts that chain damage
            player.playSound(player.getLocation(), Sound.ITEM_CROSSBOW_SHOOT, 1.5f, 1.0f);

            Vector direction = player.getLocation().getDirection().normalize();

            for (int i = 0; i < 3; i++) {
                final int boltIndex = i;
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    // Slight spread for visual effect
                    double spread = (boltIndex - 1) * 0.15;
                    Vector boltDir = direction.clone().add(new Vector(spread, 0, spread)).normalize();

                    Arrow bolt = player.launchProjectile(Arrow.class);
                    bolt.setVelocity(boltDir.multiply(3.0));
                    bolt.setDamage(8.0);
                    bolt.setPierceLevel(3);
                    bolt.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
                    bolt.setLifetimeTicks(1100);
                    bolt.setCritical(true);

                    player.playSound(player.getLocation(), Sound.ITEM_CROSSBOW_SHOOT, 0.8f, 1.2f + (boltIndex * 0.15f));

                    // Track bolt for chain damage effect
                    new BukkitRunnable() {
                        int ticks = 0;
                        final Set<UUID> chainedEntities = new HashSet<>();
                        @Override
                        public void run() {
                            if (ticks >= 40 || bolt.isDead()) {
                                if (bolt.isOnGround() || bolt.isDead()) {
                                    Location hitLoc = bolt.getLocation();
                                    // Chain damage to nearby enemies
                                    for (LivingEntity enemy : getNearbyEnemies(hitLoc, 3.0, player)) {
                                        if (chainedEntities.add(enemy.getUniqueId())) {
                                            dealAbilityDamage(player, enemy, 4.0, false);
                                            player.getWorld().spawnParticle(Particle.CRIT, enemy.getLocation().add(0, 1, 0), 8, 0.3, 0.3, 0.3, 0.1);
                                        }
                                    }
                                    // FIX #1: Was Sound.BLOCK_LIGHTNING_ROD_TOGGLE_ON (does not exist)
                                    player.playSound(hitLoc, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1.5f, 1.2f);
                                    player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, hitLoc, 15, 1, 1, 1, 0.1);
                                }
                                cancel();
                                return;
                            }
                            // Trail particles
                            player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, bolt.getLocation(), 2, 0.05, 0.05, 0.05, 0.01);
                            player.getWorld().spawnParticle(Particle.CRIT, bolt.getLocation(), 1, 0, 0, 0, 0.05);
                            ticks++;
                        }
                    }.runTaskTimer(plugin, 0L, 1L);

                }, i * 4L);
            }

            player.sendActionBar("\u00a7b Chain Shot!");

        } else {
            // NETHERITE DEFINITIVE: Thunder Barrage — 6 explosive bolts with lightning effects
            player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 0.8f);
            player.playSound(player.getLocation(), Sound.ITEM_CROSSBOW_SHOOT, 2.0f, 0.5f);

            Vector direction = player.getLocation().getDirection().normalize();

            for (int i = 0; i < 6; i++) {
                final int boltIndex = i;
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    // Wider spread pattern for 6 bolts
                    double spreadX = (Math.random() - 0.5) * 0.3;
                    double spreadY = (Math.random() - 0.5) * 0.15;
                    Vector boltDir = direction.clone().add(new Vector(spreadX, spreadY, spreadX)).normalize();

                    Arrow bolt = player.launchProjectile(Arrow.class);
                    bolt.setVelocity(boltDir.multiply(3.5));
                    bolt.setDamage(12.0);
                    bolt.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
                    bolt.setLifetimeTicks(1100);
                    bolt.setCritical(true);
                    bolt.setFireTicks(60);

                    player.playSound(player.getLocation(), Sound.ITEM_CROSSBOW_SHOOT, 1.0f, 0.7f + (boltIndex * 0.1f));
                    player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, player.getEyeLocation(), 10, 0.3, 0.3, 0.3, 0.2);

                    // Track bolt for explosive impact
                    new BukkitRunnable() {
                        int ticks = 0;
                        @Override
                        public void run() {
                            if (ticks >= 60 || bolt.isDead() || bolt.isOnGround()) {
                                Location hitLoc = bolt.getLocation();
                                // Explosive AoE
                                for (LivingEntity enemy : getNearbyEnemies(hitLoc, 4.0, player)) {
                                    dealAbilityDamage(player, enemy, 14.0, true);
                                    enemy.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1));
                                    Vector knockback = enemy.getLocation().toVector().subtract(hitLoc.toVector()).normalize().multiply(0.8);
                                    knockback.setY(0.4);
                                    enemy.setVelocity(knockback);
                                }
                                player.getWorld().spawnParticle(Particle.EXPLOSION, hitLoc, 2, 0.5, 0.5, 0.5, 0);
                                player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, hitLoc, 30, 2, 1, 2, 0.15);
                                player.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, hitLoc, 15, 1.5, 0.5, 1.5, 0.05);
                                // FIX #1 also applies here: use confirmed sound
                                player.getWorld().playSound(hitLoc, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1.5f, 0.9f);
                                cancel();
                                return;
                            }
                            // Trail particles — electric + fire
                            player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, bolt.getLocation(), 3, 0.1, 0.1, 0.1, 0.02);
                            player.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, bolt.getLocation(), 1, 0, 0, 0, 0.01);
                            ticks++;
                        }
                    }.runTaskTimer(plugin, 0L, 1L);

                }, i * 3L);
            }

            player.sendActionBar("\u00a74\u00a7l THUNDER BARRAGE ");
        }
    }

    // ================================================================
    // TRIDENT — "Tidal Surge" (Diamond) / "Poseidon's Wrath" (Netherite)
    // ================================================================

    private void handleTridentAbility(Player player, ItemStack item, boolean isDefinitive) {
        String abilityName = isDefinitive ? "Poseidon's Wrath" : "Tidal Surge";
        int cooldownSec = isDefinitive ? 14 : 8;

        if (isOnCooldown(player, abilityName)) {
            sendCooldownMessage(player, abilityName);
            return;
        }
        setCooldown(player, abilityName, cooldownSec);

        if (!isDefinitive) {
            // DIAMOND: Tidal Surge — Water blast + knockback + Slowness I
            player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_RIPTIDE_3, 1.5f, 1.0f);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_SPLASH_HIGH_SPEED, 1.0f, 0.8f);

            Location center = player.getLocation();
            Vector direction = center.getDirection().normalize();

            // Water wave cone in front of player
            new BukkitRunnable() {
                int tick = 0;
                @Override
                public void run() {
                    if (tick >= 10) { cancel(); return; }
                    double distance = tick * 1.0;
                    Location waveLoc = center.clone().add(direction.clone().multiply(distance));

                    // Water particles in a cone
                    for (int i = 0; i < 15; i++) {
                        double spread = tick * 0.3;
                        double offX = (Math.random() - 0.5) * spread;
                        double offZ = (Math.random() - 0.5) * spread;
                        Location pLoc = waveLoc.clone().add(offX, Math.random() * 1.5, offZ);
                        player.getWorld().spawnParticle(Particle.SPLASH, pLoc, 3, 0.1, 0.2, 0.1, 0.1);
                        player.getWorld().spawnParticle(Particle.BUBBLE, pLoc, 2, 0.1, 0.1, 0.1, 0.05);
                    }

                    for (LivingEntity enemy : getNearbyEnemies(waveLoc, 3.0, player)) {
                        dealAbilityDamage(player, enemy, 8.0, false);
                        Vector knockback = direction.clone().multiply(1.5).setY(0.5);
                        enemy.setVelocity(knockback);
                        enemy.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 0));
                    }
                    tick++;
                }
            }.runTaskTimer(plugin, 0L, 2L);

            player.sendActionBar("\u00a73 Tidal Surge!");

        } else {
            // NETHERITE DEFINITIVE: Poseidon's Wrath — 3 water waves + lightning strike
            player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_THUNDER, 2.0f, 0.5f);
            player.playSound(player.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 0.5f, 1.5f);

            Location center = player.getLocation();

            // 3 expanding water waves
            double[] waveDamages = {16.0, 13.0, 10.0};
            double[] waveRadii = {4.0, 7.0, 10.0};

            for (int wave = 0; wave < 3; wave++) {
                final double waveRadius = waveRadii[wave];
                final double waveDamage = waveDamages[wave];
                final int waveNum = wave;

                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    // Water ring particles
                    for (int i = 0; i < 40 + (waveNum * 10); i++) {
                        double angle = (2 * Math.PI / (40 + waveNum * 10)) * i;
                        Location ringLoc = center.clone().add(Math.cos(angle) * waveRadius, 0.3, Math.sin(angle) * waveRadius);
                        player.getWorld().spawnParticle(Particle.SPLASH, ringLoc, 5, 0.2, 0.3, 0.2, 0.1);
                        player.getWorld().spawnParticle(Particle.BUBBLE, ringLoc, 3, 0.1, 0.2, 0.1, 0.05);
                        if (waveNum >= 2) {
                            player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, ringLoc, 2, 0.1, 0.3, 0.1, 0.05);
                        }
                    }

                    for (LivingEntity enemy : getNearbyEnemies(center, waveRadius, player)) {
                        dealAbilityDamage(player, enemy, waveDamage, true);
                        Vector knockback = enemy.getLocation().toVector().subtract(center.toVector()).normalize().multiply(1.0);
                        knockback.setY(0.6);
                        enemy.setVelocity(knockback);
                        enemy.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 1));
                    }

                    player.getWorld().playSound(center, Sound.ENTITY_PLAYER_SPLASH_HIGH_SPEED, 2.0f, 0.7f + (waveNum * 0.2f));
                }, wave * 10L);
            }

            // Lightning strike at the end
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                // Find the nearest enemy for a targeted lightning strike, or strike center
                Location strikeLoc = center.clone();
                List<LivingEntity> nearby = getNearbyEnemies(center, 10.0, player);
                if (!nearby.isEmpty()) {
                    strikeLoc = nearby.get(0).getLocation();
                }
                player.getWorld().strikeLightningEffect(strikeLoc);
                for (LivingEntity enemy : getNearbyEnemies(strikeLoc, 3.0, player)) {
                    dealAbilityDamage(player, enemy, 10.0, true);
                    enemy.setFireTicks(60);
                }
                player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, strikeLoc, 40, 2, 2, 2, 0.2);
                player.getWorld().playSound(strikeLoc, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 2.0f, 0.8f);
            }, 35L);

            player.sendActionBar("\u00a71\u00a7l POSEIDON'S WRATH ");
        }
    }

    // ================================================================
    // MACE — "Ground Slam" (Diamond) / "Meteor Strike" (Netherite)
    // ================================================================

    private void handleMaceAbility(Player player, ItemStack item, boolean isDefinitive) {
        String abilityName = isDefinitive ? "Meteor Strike" : "Ground Slam";
        int cooldownSec = isDefinitive ? 15 : 8;

        if (isOnCooldown(player, abilityName)) {
            sendCooldownMessage(player, abilityName);
            return;
        }
        setCooldown(player, abilityName, cooldownSec);

        if (!isDefinitive) {
            // DIAMOND: Ground Slam — AoE slam with stun
            player.playSound(player.getLocation(), Sound.ITEM_MACE_SMASH_GROUND, 2.0f, 0.8f);

            Location center = player.getLocation();

            // Ground shockwave particles
            for (int i = 0; i < 60; i++) {
                double angle = Math.random() * 2 * Math.PI;
                double r = Math.random() * 5;
                Location pLoc = center.clone().add(Math.cos(angle) * r, 0.15, Math.sin(angle) * r);
                player.getWorld().spawnParticle(Particle.BLOCK, pLoc, 4, 0.2, 0.3, 0.2, 0.1,
                        Material.STONE.createBlockData());
                player.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, pLoc, 1, 0, 0.5, 0, 0.03);
            }
            player.getWorld().spawnParticle(Particle.EXPLOSION, center.clone().add(0, 0.5, 0), 3, 1, 0, 1, 0);

            for (LivingEntity enemy : getNearbyEnemies(center, 5.0, player)) {
                dealAbilityDamage(player, enemy, 10.0, false);
                // Stun: Slowness III + Mining Fatigue II
                enemy.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 2));
                enemy.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 60, 1));
                Vector knockback = enemy.getLocation().toVector().subtract(center.toVector()).normalize().multiply(0.6);
                knockback.setY(0.3);
                enemy.setVelocity(knockback);
            }

            player.sendActionBar("\u00a7e Ground Slam!");

        } else {
            // NETHERITE DEFINITIVE: Meteor Strike — Launch up, then slam down for AoE
            player.playSound(player.getLocation(), Sound.ITEM_MACE_SMASH_GROUND_HEAVY, 1.0f, 0.5f);
            player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_EMERGE, 0.7f, 1.5f);

            Location startLoc = player.getLocation().clone();

            // Phase 1: Launch player upward
            player.setVelocity(new Vector(0, 2.5, 0));
            player.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, startLoc, 30, 1, 0.5, 1, 0.1);

            // Phase 2: After reaching peak, slam down
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!player.isOnline()) return;

                // Pull player back down hard
                player.setVelocity(new Vector(0, -3.5, 0));
                player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1.5f, 1.0f);

                // Trail particles while falling
                new BukkitRunnable() {
                    int tick = 0;
                    @Override
                    public void run() {
                        if (tick >= 20 || player.isOnGround()) {
                            // Impact!
                            Location impactLoc = player.getLocation();
                            double fallDistance = startLoc.getY() - impactLoc.getY() + 12; // +12 from initial launch
                            double impactDamage = Math.min(25.0, 5.0 + fallDistance * 1.0);

                            player.getWorld().playSound(impactLoc, Sound.ITEM_MACE_SMASH_GROUND_HEAVY, 2.0f, 0.6f);
                            player.getWorld().playSound(impactLoc, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.8f);

                            // Massive impact particles
                            player.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, impactLoc, 3, 0, 0, 0, 0);
                            for (int i = 0; i < 80; i++) {
                                double angle = Math.random() * 2 * Math.PI;
                                double r = Math.random() * 6;
                                Location pLoc = impactLoc.clone().add(Math.cos(angle) * r, 0.1, Math.sin(angle) * r);
                                player.getWorld().spawnParticle(Particle.BLOCK, pLoc, 3, 0.1, 0.5, 0.1, 0.2,
                                        Material.DEEPSLATE.createBlockData());
                                player.getWorld().spawnParticle(Particle.LAVA, pLoc, 1, 0, 0, 0, 0);
                            }
                            player.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, impactLoc, 40, 4, 1, 4, 0.1);

                            // Damage all nearby enemies
                            for (LivingEntity enemy : getNearbyEnemies(impactLoc, 8.0, player)) {
                                // Distance-based damage: closer = more damage
                                double dist = enemy.getLocation().distance(impactLoc);
                                double distFactor = 1.0 - (dist / 8.0);
                                double actualDamage = impactDamage * Math.max(0.3, distFactor);
                                dealAbilityDamage(player, enemy, actualDamage, true);

                                enemy.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 2));
                                enemy.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 80, 1));
                                Vector knockback = enemy.getLocation().toVector().subtract(impactLoc.toVector()).normalize().multiply(1.5);
                                knockback.setY(0.8);
                                enemy.setVelocity(knockback);
                            }

                            // Protect player from fall damage briefly
                            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 20, 4));

                            cancel();
                            return;
                        }
                        // Falling trail
                        player.getWorld().spawnParticle(Particle.FLAME, player.getLocation(), 5, 0.3, 0.3, 0.3, 0.05);
                        player.getWorld().spawnParticle(Particle.LAVA, player.getLocation(), 2, 0.2, 0.2, 0.2, 0);
                        player.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, player.getLocation(), 3, 0.5, 0.2, 0.5, 0.02);
                        tick++;
                    }
                }.runTaskTimer(plugin, 0L, 1L);

            }, 15L);

            player.sendActionBar("\u00a74\u00a7l METEOR STRIKE ");
        }
    }
}

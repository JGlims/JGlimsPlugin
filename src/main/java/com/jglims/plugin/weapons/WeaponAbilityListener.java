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
import org.bukkit.event.entity.EntityDamageByEntityEvent;
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
        int cooldownSec = isDefinitive ? 18 : 10;

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
        int cooldownSec = isDefinitive ? 22 : 12;

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
        int cooldownSec = isDefinitive ? 25 : 15;

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
        int cooldownSec = isDefinitive ? 20 : 12;

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
        int cooldownSec = isDefinitive ? 20 : 10;

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
        int cooldownSec = isDefinitive ? 20 : 12;

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
        int cooldownSec = isDefinitive ? 20 : 12;

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
                    // Set high lifetime so it despawns quickly after hitting
                    arrow.setLifetimeTicks(1100);
                    arrow.setCritical(true);

                    player.getWorld().spawnParticle(Particle.CRIT, player.getEyeLocation(), 5, 0.2, 0.2, 0.2, 0.1);
                    player.playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 0.8f, 1.5f + (arrows * 0.1f));

                    arrows++;
                }
            }.runTaskTimer(plugin, 0L, 3L);

            player.sendActionBar("\u00a7e Arrow Storm!");

        } else {
            // NETHERITE DEFINITIVE: Celestial Volley — Launch 12 arrows skyward that rain down on enemies
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0f, 1.5f);
            player.playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 2.0f, 0.5f);

            Location target = player.getLocation().add(player.getLocation().getDirection().multiply(10));
            target.setY(player.getLocation().getY());

            // Visual launch effect
            player.getWorld().spawnParticle(Particle.FIREWORK, player.getLocation().add(0, 2, 0), 30, 0.5, 1, 0.5, 0.2);

            // Delayed rain of arrows from the sky
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.playSound(target, Sound.ENTITY_ARROW_HIT, 2.0f, 0.7f);
                player.getWorld().spawnParticle(Particle.CLOUD, target.clone().add(0, 15, 0), 50, 3, 1, 3, 0.1);

                for (int i = 0; i < 12; i++) {
                    final int index = i;
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        double offsetX = (Math.random() - 0.5) * 6;
                        double offsetZ = (Math.random() - 0.5) * 6;
                        Location spawnLoc = target.clone().add(offsetX, 15, offsetZ);

                        Arrow arrow = player.getWorld().spawnArrow(spawnLoc, new Vector(0, -2, 0), 2.0f, 1.0f);
                        arrow.setShooter(player);
                        arrow.setDamage(10.0);
                        arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
                        arrow.setCritical(true);
                        arrow.setLifetimeTicks(1100);

                        player.getWorld().spawnParticle(Particle.END_ROD, spawnLoc, 5, 0.1, 0.1, 0.1, 0.05);
                        player.getWorld().spawnParticle(Particle.DUST, spawnLoc, 3,
                                0.2, 0.2, 0.2, 0,
                                new Particle.DustOptions(Color.fromRGB(255, 200, 50), 1.5f));
                    }, index * 2L);
                }

                // Impact particles after all arrows land
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    player.getWorld().spawnParticle(Particle.EXPLOSION, target, 5, 3, 0.5, 3, 0);
                    player.getWorld().playSound(target, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.2f);
                }, 30L);
            }, 15L);

            player.sendActionBar("\u00a76\u00a7l CELESTIAL VOLLEY ");
        }
    }

    // ================================================================
    // CROSSBOW — "Chain Shot" (Diamond) / "Thunder Barrage" (Netherite)
    // ================================================================

    private void handleCrossbowAbility(Player player, ItemStack item, boolean isDefinitive) {
        String abilityName = isDefinitive ? "Thunder Barrage" : "Chain Shot";
        int cooldownSec = isDefinitive ? 22 : 14;

        if (isOnCooldown(player, abilityName)) {
            sendCooldownMessage(player, abilityName);
            return;
        }
        setCooldown(player, abilityName, cooldownSec);

        if (!isDefinitive) {
            // DIAMOND: Chain Shot — Fire a bolt that chains to 3 nearby enemies
            player.playSound(player.getLocation(), Sound.ITEM_CROSSBOW_SHOOT, 1.5f, 1.0f);

            Arrow bolt = player.launchProjectile(Arrow.class);
            bolt.setVelocity(player.getLocation().getDirection().multiply(3.0));
            bolt.setDamage(8.0);
            bolt.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
            bolt.setCritical(true);
            bolt.setLifetimeTicks(1100);

            // Track the bolt for chain effect
            new BukkitRunnable() {
                int tick = 0;
                boolean chained = false;
                @Override
                public void run() {
                    if (tick >= 60 || !bolt.isValid() || bolt.isDead()) {
                        cancel();
                        return;
                    }

                    // Trail particles
                    if (bolt.isValid()) {
                        bolt.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, bolt.getLocation(), 3, 0.1, 0.1, 0.1, 0);
                    }

                    // Check if bolt hit something (is in block or has low velocity after initial frames)
                    if (tick > 5 && bolt.isInBlock() && !chained) {
                        chained = true;
                        Location hitLoc = bolt.getLocation();
                        List<LivingEntity> nearby = getNearbyEnemies(hitLoc, 6.0, player);
                        int chains = 0;
                        Location lastLoc = hitLoc.clone();

                        for (LivingEntity target : nearby) {
                            if (chains >= 3) break;
                            final Location fromLoc = lastLoc.clone();
                            final LivingEntity chainTarget = target;
                            final int chainDelay = chains;

                            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                dealAbilityDamage(player, chainTarget, 6.0 - chainDelay, false);
                                chainTarget.getWorld().spawnParticle(Particle.ELECTRIC_SPARK,
                                        chainTarget.getLocation().add(0, 1, 0), 15, 0.3, 0.5, 0.3, 0.1);
                                chainTarget.playEffect(org.bukkit.EntityEffect.HURT);

                                // Draw chain line between targets
                                Vector dir = chainTarget.getLocation().add(0, 1, 0).toVector()
                                        .subtract(fromLoc.toVector());
                                double dist = dir.length();
                                dir.normalize();
                                for (double d = 0; d < dist; d += 0.5) {
                                    Location pLoc = fromLoc.clone().add(dir.clone().multiply(d));
                                    chainTarget.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, pLoc, 1, 0, 0, 0, 0);
                                }
                            }, chainDelay * 5L);

                            lastLoc = target.getLocation().add(0, 1, 0);
                            chains++;
                        }

                        player.playSound(hitLoc, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1.5f, 1.2f);
                        cancel();
                    }
                    tick++;
                }
            }.runTaskTimer(plugin, 0L, 1L);

            player.sendActionBar("\u00a7b Chain Shot!");

        } else {
            // NETHERITE DEFINITIVE: Thunder Barrage — 3 explosive lightning bolts
            player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.2f);
            player.playSound(player.getLocation(), Sound.ITEM_CROSSBOW_SHOOT, 2.0f, 0.5f);

            Vector direction = player.getLocation().getDirection().normalize();

            for (int bolt_i = 0; bolt_i < 3; bolt_i++) {
                final int boltIndex = bolt_i;
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    // Slight spread for each bolt
                    double spread = (boltIndex - 1) * 0.15;
                    Vector boltDir = direction.clone().add(new Vector(
                            spread * Math.cos(Math.PI / 2), 0, spread * Math.sin(Math.PI / 2)
                    )).normalize();

                    Location boltStart = player.getEyeLocation().clone();

                    new BukkitRunnable() {
                        int tick = 0;
                        Location current = boltStart.clone();

                        @Override
                        public void run() {
                            if (tick >= 20) {
                                explodeAt(current);
                                cancel();
                                return;
                            }

                            current.add(boltDir.clone().multiply(1.5));

                            if (current.getBlock().getType().isSolid()) {
                                explodeAt(current);
                                cancel();
                                return;
                            }

                            // Lightning trail
                            player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, current, 8, 0.2, 0.2, 0.2, 0.05);
                            player.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, current, 3, 0.1, 0.1, 0.1, 0.02);

                            // Check for entity hit
                            for (Entity e : current.getWorld().getNearbyEntities(current, 1.5, 1.5, 1.5)) {
                                if (e instanceof LivingEntity le && !(e instanceof Player) && !(e instanceof ArmorStand)) {
                                    explodeAt(current);
                                    cancel();
                                    return;
                                }
                            }
                            tick++;
                        }

                        private void explodeAt(Location loc) {
                            World world = loc.getWorld();
                            world.spawnParticle(Particle.EXPLOSION_EMITTER, loc, 1, 0, 0, 0, 0);
                            world.spawnParticle(Particle.ELECTRIC_SPARK, loc, 40, 2, 2, 2, 0.2);
                            world.spawnParticle(Particle.FLASH, loc, 2, 0, 0, 0, 0);
                            world.playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1.5f, 1.0f);

                            // Strike lightning visually (no fire)
                            world.strikeLightningEffect(loc);

                            for (LivingEntity enemy : getNearbyEnemies(loc, 4.0, player)) {
                                dealAbilityDamage(player, enemy, 14.0, true);
                                Vector kb = enemy.getLocation().toVector().subtract(loc.toVector()).normalize().multiply(0.8);
                                kb.setY(0.5);
                                enemy.setVelocity(kb);
                                enemy.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1));
                            }
                        }
                    }.runTaskTimer(plugin, 0L, 1L);

                    player.playSound(player.getLocation(), Sound.ITEM_CROSSBOW_SHOOT, 1.0f, 0.8f + (boltIndex * 0.2f));
                }, boltIndex * 8L);
            }

            player.sendActionBar("\u00a7e\u00a7l THUNDER BARRAGE ");
        }
    }

    // ================================================================
    // TRIDENT — "Tidal Strike" (Diamond) / "Poseidon's Wrath" (Netherite)
    // ================================================================

    private void handleTridentAbility(Player player, ItemStack item, boolean isDefinitive) {
        String abilityName = isDefinitive ? "Poseidon's Wrath" : "Tidal Strike";
        int cooldownSec = isDefinitive ? 22 : 12;

        if (isOnCooldown(player, abilityName)) {
            sendCooldownMessage(player, abilityName);
            return;
        }
        setCooldown(player, abilityName, cooldownSec);

        if (!isDefinitive) {
            // DIAMOND: Tidal Strike — Summon a water burst that pushes enemies away
            player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_RIPTIDE_3, 1.0f, 1.0f);
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_SPLASH, 2.0f, 0.8f);

            Location center = player.getLocation();

            // Water burst visual
            new BukkitRunnable() {
                int tick = 0;
                @Override
                public void run() {
                    if (tick >= 10) { cancel(); return; }
                    double radius = 1.0 + tick * 0.8;
                    for (int i = 0; i < 25; i++) {
                        double angle = Math.random() * 2 * Math.PI;
                        double r = Math.random() * radius;
                        Location pLoc = center.clone().add(Math.cos(angle) * r, 0.3, Math.sin(angle) * r);
                        player.getWorld().spawnParticle(Particle.SPLASH, pLoc, 3, 0.2, 0.3, 0.2, 0.1);
                        player.getWorld().spawnParticle(Particle.BUBBLE, pLoc, 2, 0.2, 0.2, 0.2, 0.05);
                        player.getWorld().spawnParticle(Particle.DUST, pLoc, 2,
                                0.3, 0.3, 0.3, 0,
                                new Particle.DustOptions(Color.fromRGB(30, 100, 200), 1.3f));
                    }

                    // Push enemies at peak
                    if (tick == 5) {
                        for (LivingEntity enemy : getNearbyEnemies(center, 7.0, player)) {
                            dealAbilityDamage(player, enemy, 8.0, false);
                            Vector push = enemy.getLocation().toVector().subtract(center.toVector()).normalize().multiply(1.8);
                            push.setY(0.6);
                            enemy.setVelocity(push);
                        }
                    }
                    tick++;
                }
            }.runTaskTimer(plugin, 0L, 2L);

            player.sendActionBar("\u00a73 Tidal Strike!");

        } else {
            // NETHERITE DEFINITIVE: Poseidon's Wrath — Large vortex + lightning strike
            player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_THUNDER, 2.0f, 0.6f);
            player.playSound(player.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 0.5f, 1.5f);

            Location center = player.getLocation().add(player.getLocation().getDirection().multiply(6));
            center.setY(player.getLocation().getY());

            // Vortex visual
            new BukkitRunnable() {
                int tick = 0;
                @Override
                public void run() {
                    if (tick >= 40) { cancel(); return; }
                    double radius = 3.0 + Math.sin(tick * 0.3) * 2.0;
                    double height = tick * 0.15;

                    for (int i = 0; i < 30; i++) {
                        double angle = (2 * Math.PI / 30) * i + (tick * 0.4);
                        Location pLoc = center.clone().add(
                                Math.cos(angle) * radius,
                                height * (i / 30.0),
                                Math.sin(angle) * radius
                        );
                        player.getWorld().spawnParticle(Particle.SPLASH, pLoc, 2, 0.1, 0.1, 0.1, 0.05);
                        player.getWorld().spawnParticle(Particle.DUST, pLoc, 1,
                                0.1, 0.1, 0.1, 0,
                                new Particle.DustOptions(Color.fromRGB(20, 80, 180), 1.5f));
                    }

                    // Pull enemies toward center
                    if (tick % 5 == 0) {
                        for (LivingEntity enemy : getNearbyEnemies(center, 8.0, player)) {
                            Vector pull = center.toVector().subtract(enemy.getLocation().toVector()).normalize().multiply(0.5);
                            pull.setY(0.2);
                            enemy.setVelocity(enemy.getVelocity().add(pull));
                            if (tick % 10 == 0) {
                                dealAbilityDamage(player, enemy, 8.0, true);
                            }
                        }
                    }

                    // Periodic lightning
                    if (tick == 15 || tick == 30) {
                        player.getWorld().strikeLightningEffect(center);
                        player.getWorld().playSound(center, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 2.0f, 0.8f);
                        for (LivingEntity enemy : getNearbyEnemies(center, 5.0, player)) {
                            dealAbilityDamage(player, enemy, 12.0, true);
                            enemy.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 2));
                        }
                    }
                    tick++;
                }
            }.runTaskTimer(plugin, 0L, 1L);

            // Final slam
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, center, 3, 0, 0, 0, 0);
                player.getWorld().spawnParticle(Particle.SPLASH, center, 200, 5, 3, 5, 0.5);
                player.getWorld().playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.5f);
                for (LivingEntity enemy : getNearbyEnemies(center, 6.0, player)) {
                    dealAbilityDamage(player, enemy, 15.0, true);
                    Vector kb = enemy.getLocation().toVector().subtract(center.toVector()).normalize().multiply(2.0);
                    kb.setY(1.0);
                    enemy.setVelocity(kb);
                }
            }, 42L);

            player.sendActionBar("\u00a71\u00a7l POSEIDON'S WRATH ");
        }
    }

    // ================================================================
    // MACE — "Ground Pound" (Diamond) / "Cataclysm" (Netherite)
    // ================================================================

    private void handleMaceAbility(Player player, ItemStack item, boolean isDefinitive) {
        String abilityName = isDefinitive ? "Cataclysm" : "Ground Pound";
        int cooldownSec = isDefinitive ? 25 : 14;

        if (isOnCooldown(player, abilityName)) {
            sendCooldownMessage(player, abilityName);
            return;
        }
        setCooldown(player, abilityName, cooldownSec);

        if (!isDefinitive) {
            // DIAMOND: Ground Pound — Single powerful shockwave centered on player
            player.playSound(player.getLocation(), Sound.ITEM_MACE_SMASH_GROUND_HEAVY, 2.0f, 0.8f);
            player.playSound(player.getLocation(), Sound.ENTITY_IRON_GOLEM_ATTACK, 1.5f, 0.6f);

            Location center = player.getLocation();

            // Shockwave ring
            new BukkitRunnable() {
                int tick = 0;
                @Override
                public void run() {
                    if (tick >= 8) { cancel(); return; }
                    double radius = 1.0 + tick * 1.0;
                    for (int i = 0; i < 30; i++) {
                        double angle = (2 * Math.PI / 30) * i;
                        Location pLoc = center.clone().add(Math.cos(angle) * radius, 0.2, Math.sin(angle) * radius);
                        player.getWorld().spawnParticle(Particle.BLOCK, pLoc, 3, 0.2, 0.3, 0.2, 0.1,
                                Material.STONE.createBlockData());
                        player.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, pLoc, 1, 0, 0.5, 0, 0.02);
                    }

                    // Damage at expanding ring
                    if (tick == 3 || tick == 6) {
                        for (LivingEntity enemy : getNearbyEnemies(center, radius, player)) {
                            dealAbilityDamage(player, enemy, 10.0, false);
                            Vector kb = enemy.getLocation().toVector().subtract(center.toVector()).normalize().multiply(1.0);
                            kb.setY(0.5);
                            enemy.setVelocity(kb);
                        }
                    }
                    tick++;
                }
            }.runTaskTimer(plugin, 0L, 2L);

            player.sendActionBar("\u00a76 Ground Pound!");

        } else {
            // NETHERITE DEFINITIVE: Cataclysm — Three expanding ground slams with increasing radius
            player.playSound(player.getLocation(), Sound.ITEM_MACE_SMASH_GROUND_HEAVY, 2.0f, 0.3f);
            player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1.0f, 0.5f);

            Location center = player.getLocation();

            // Give player resistance during the animation
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 60, 2));

            for (int slam = 0; slam < 3; slam++) {
                final int slamIndex = slam;
                final double slamRadius = 4.0 + (slam * 3.0);
                final double slamDamage = 18.0 - (slam * 3.0);

                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    // Slam visual
                    player.getWorld().playSound(center, Sound.ITEM_MACE_SMASH_GROUND_HEAVY, 2.0f, 0.6f + (slamIndex * 0.2f));

                    new BukkitRunnable() {
                        int tick = 0;
                        @Override
                        public void run() {
                            if (tick >= 6) { cancel(); return; }
                            double r = (tick / 6.0) * slamRadius;
                            for (int i = 0; i < 40; i++) {
                                double angle = (2 * Math.PI / 40) * i;
                                Location pLoc = center.clone().add(Math.cos(angle) * r, 0.15, Math.sin(angle) * r);
                                player.getWorld().spawnParticle(Particle.BLOCK, pLoc, 2, 0.1, 0.2, 0.1, 0.1,
                                        Material.DEEPSLATE.createBlockData());
                                if (tick % 2 == 0) {
                                    player.getWorld().spawnParticle(Particle.LAVA, pLoc, 1, 0, 0, 0, 0);
                                }
                            }
                            tick++;
                        }
                    }.runTaskTimer(plugin, 0L, 1L);

                    // Damage all enemies in radius
                    for (LivingEntity enemy : getNearbyEnemies(center, slamRadius, player)) {
                        dealAbilityDamage(player, enemy, slamDamage, true);
                        Vector kb = enemy.getLocation().toVector().subtract(center.toVector()).normalize().multiply(1.5);
                        kb.setY(0.7 + (slamIndex * 0.2));
                        enemy.setVelocity(kb);
                        enemy.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1 + slamIndex));
                    }

                    player.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, center, 1, 0, 0, 0, 0);
                }, slamIndex * 12L);
            }

            // Final explosion
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, center, 80, 3, 2, 3, 0.5);
                player.getWorld().playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.4f);
            }, 38L);

            player.sendActionBar("\u00a74\u00a7l CATACLYSM ");
        }
    }

    // ================================================================
    // BLOODTHIRST Lifesteal Handler (for Axe diamond ability)
    // ================================================================

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBloodthirstHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || !item.hasItemMeta()) return;

        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        NamespacedKey bloodthirstKey = new NamespacedKey(plugin, "bloodthirst_active");
        Long expiry = pdc.getOrDefault(bloodthirstKey, PersistentDataType.LONG, 0L);

        if (expiry > 0 && System.currentTimeMillis() < expiry) {
            // Heal 30% of damage dealt
            double heal = event.getFinalDamage() * 0.30;
            double maxHealth = player.getAttribute(Attribute.MAX_HEALTH).getValue();
            double newHealth = Math.min(player.getHealth() + heal, maxHealth);
            player.setHealth(newHealth);

            // Visual feedback
            player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0, 2, 0), 2, 0.3, 0.3, 0.3, 0);
            player.getWorld().spawnParticle(Particle.DUST, player.getLocation().add(0, 1.5, 0), 4,
                    0.3, 0.3, 0.3, 0,
                    new Particle.DustOptions(Color.fromRGB(200, 0, 0), 1.2f));
        }
    }
}

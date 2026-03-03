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
        player.sendActionBar("§c" + abilityName + " on cooldown: §e" + String.format("%.1f", secondsLeft) + "s");
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
        // Ender Dragon exception: definitive abilities deal normal damage only
        if (isDefinitive && target instanceof EnderDragon) {
            target.damage(damage * 0.3, player); // Drastically reduced
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
        // Abilities only on Diamond (2) and Netherite (3) super weapons
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
            // DIAMOND: Dash Strike — Dash forward 6 blocks, deal damage to all in path
            player.setVelocity(player.getLocation().getDirection().multiply(1.8));
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.5f, 1.2f);

            new BukkitRunnable() {
                int ticks = 0;
                @Override
                public void run() {
                    if (ticks >= 8) { cancel(); return; }
                    Location loc = player.getLocation();

                    // Sweep particles
                    player.getWorld().spawnParticle(Particle.SWEEP_ATTACK, loc.add(0, 1, 0), 3, 0.5, 0.3, 0.5, 0);
                    player.getWorld().spawnParticle(Particle.CRIT, loc, 8, 0.6, 0.4, 0.6, 0.1);

                    // Damage nearby enemies
                    for (LivingEntity enemy : getNearbyEnemies(loc, 2.5, player)) {
                        dealAbilityDamage(player, enemy, 8.0, false);
                        enemy.setVelocity(player.getLocation().getDirection().multiply(0.5));
                    }
                    ticks++;
                }
            }.runTaskTimer(plugin, 0L, 2L);

        } else {
            // NETHERITE DEFINITIVE: Dimensional Cleave
            // Slash creates a 12-block wide rift in front of player dealing massive damage
            player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1.0f, 1.5f);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 2.0f, 0.5f);

            Location center = player.getLocation().add(player.getLocation().getDirection().multiply(4));
            center.setY(player.getLocation().getY());

            // Create dimensional rift visual
            new BukkitRunnable() {
                int ticks = 0;
                @Override
                public void run() {
                    if (ticks >= 15) { cancel(); return; }

                    // Expanding ring of particles
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

                    // Damage enemies within radius
                    if (ticks % 3 == 0) {
                        for (LivingEntity enemy : getNearbyEnemies(center, radius, player)) {
                            dealAbilityDamage(player, enemy, 15.0, true);
                            enemy.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 2));
                        }
                    }

                    // Central column effect
                    player.getWorld().spawnParticle(Particle.EXPLOSION, center.clone().add(0, 1, 0), 1, 0, 0, 0, 0);
                    player.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, center, 15, 1.5, 2, 1.5, 0.02);

                    ticks++;
                }
            }.runTaskTimer(plugin, 0L, 2L);

            // Final explosion at end
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
            // DIAMOND: Bloodthirst — 5 seconds of lifesteal (heal 30% of damage dealt)
            // + attack speed boost + rage particles
            player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 100, 1));
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 100, 0));
            player.playSound(player.getLocation(), Sound.ENTITY_RAVAGER_ROAR, 0.8f, 1.2f);

            // Store bloodthirst state in PDC
            ItemMeta meta = item.getItemMeta();
            meta.getPersistentDataContainer().set(
                    new NamespacedKey(plugin, "bloodthirst_active"),
                    PersistentDataType.LONG,
                    System.currentTimeMillis() + 5000L
            );
            item.setItemMeta(meta);

            // Rage aura particles for duration
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

            player.sendActionBar("§c§l⚔ BLOODTHIRST ACTIVE §c§l⚔");

        } else {
            // NETHERITE DEFINITIVE: Ragnarok Cleave
            // Slam axe into ground creating a massive shockwave (10-block radius)
            // All enemies take damage, are launched upward, and receive Weakness
            player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1.5f, 0.7f);
            player.playSound(player.getLocation(), Sound.ITEM_MACE_SMASH_GROUND_HEAVY, 2.0f, 0.8f);

            Location center = player.getLocation();

            // Ground crack visual
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

            // Damage waves at intervals
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

            player.sendActionBar("§4§l✦ RAGNAROK CLEAVE ✦");
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

        // Both tiers detect ores — Netherite has larger radius and finds Ancient Debris easily
        int oreRadius = isDefinitive ? config.getOreDetectRadiusNetherite() : config.getOreDetectRadiusDiamond();
        int debrisRadius = isDefinitive
                ? config.getOreDetectAncientDebrisRadiusNetherite()   // 40 blocks!
                : config.getOreDetectAncientDebrisRadiusDiamond();    // 24 blocks
        int durationTicks = config.getOreDetectDurationTicks();

        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, isDefinitive ? 0.5f : 1.0f);
        if (isDefinitive) {
            player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_HEARTBEAT, 1.5f, 0.6f);
        }

        // Scan for ores
        Location playerLoc = player.getLocation();
        World world = player.getWorld();
        Map<Location, OreType> foundOres = new LinkedHashMap<>();

        // Determine max scan radius (debris might be bigger)
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

                    // Ancient Debris uses the larger radius
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

        // Send results to player
        int totalOres = foundOres.size();
        long debrisCount = foundOres.values().stream().filter(o -> o == OreType.ANCIENT_DEBRIS).count();

        if (totalOres == 0) {
            player.sendActionBar("§7Ore Pulse: §fNo ores detected nearby.");
        } else {
            String msg = isDefinitive
                    ? "§5§l✦ SEISMIC RESONANCE: §f" + totalOres + " ores detected"
                    : "§e✦ Ore Pulse: §f" + totalOres + " ores detected";
            if (debrisCount > 0) {
                msg += " §7| §4§l" + debrisCount + " Ancient Debris!";
            }
            player.sendActionBar(msg);
            if (debrisCount > 0) {
                player.sendMessage("§4§l⚠ " + debrisCount + " Ancient Debris detected within " + debrisRadius + " blocks!");
            }
        }

        // Visual pulse outward
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

        // Highlight ore locations with particles for duration
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= durationTicks || !player.isOnline()) { cancel(); return; }
                for (Map.Entry<Location, OreType> entry : foundOres.entrySet()) {
                    Location oreLoc = entry.getKey().clone().add(0.5, 0.5, 0.5);
                    OreType type = entry.getValue();
                    Particle.DustOptions dust = type.getDustOptions();

                    // Only show every few ticks to reduce particle load
                    if (ticks % 5 == 0) {
                        // Show to the specific player only
                        player.spawnParticle(Particle.DUST, oreLoc, 3, 0.2, 0.2, 0.2, 0, dust);

                        // Ancient Debris gets extra flashy particles
                        if (type == OreType.ANCIENT_DEBRIS) {
                            player.spawnParticle(Particle.FLAME, oreLoc, 1, 0.1, 0.1, 0.1, 0.01);
                            if (ticks % 20 == 0) {
                                player.spawnParticle(Particle.SOUL_FIRE_FLAME, oreLoc, 5, 0.3, 0.3, 0.3, 0.02);
                            }
                        }
                    }
                    ticks++;
                }
            }
        }.runTaskTimer(plugin, 5L, 1L);
    }

    // Ore classification for particle colors
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
            // DIAMOND: Earthen Wall — Launch all enemies within 6 blocks into the air
            // and give the player Resistance II for 4 seconds
            player.playSound(player.getLocation(), Sound.BLOCK_GRAVEL_BREAK, 2.0f, 0.5f);
            player.playSound(player.getLocation(), Sound.ENTITY_IRON_GOLEM_ATTACK, 1.0f, 0.7f);
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 80, 1));

            Location center = player.getLocation();
            for (LivingEntity enemy : getNearbyEnemies(center, 6.0, player)) {
                enemy.setVelocity(new Vector(0, 1.2, 0));
                dealAbilityDamage(player, enemy, 6.0, false);
            }

            // Ground eruption particles
            for (int i = 0; i < 50; i++) {
                double angle = Math.random() * 2 * Math.PI;
                double r = Math.random() * 5;
                Location pLoc = center.clone().add(Math.cos(angle) * r, 0.2, Math.sin(angle) * r);
                player.getWorld().spawnParticle(Particle.BLOCK, pLoc, 5, 0.3, 0.5, 0.3, 0.1,
                        Material.DIRT.createBlockData());
            }

            player.sendActionBar("§e✦ Earthen Wall — Enemies launched!");

        } else {
            // NETHERITE DEFINITIVE: Tectonic Upheaval
            // Massive ground eruption in a 12-block radius. Three waves of damage.
            // Enemies are launched high, take fall damage, and are buried in slowness.
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

                    // Ground crack particles
                    for (int i = 0; i < 25 + ticks; i++) {
                        double angle = Math.random() * 2 * Math.PI;
                        double r = Math.random() * radius;
                        Location pLoc = center.clone().add(Math.cos(angle) * r, 0.1, Math.sin(angle) * r);
                        player.getWorld().spawnParticle(Particle.BLOCK, pLoc, 3, 0.1, 0.8, 0.1, 0.3,
                                Material.NETHERRACK.createBlockData());
                        player.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, pLoc, 1, 0, 1, 0, 0.05);
                    }

                    // Damage and launch at wave intervals
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

            player.sendActionBar("§4§l✦ TECTONIC UPHEAVAL ✦");
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
            // DIAMOND: Harvest Storm — Spin attack hitting all enemies in 5-block radius
            // Applies bleeding DoT for 4 seconds
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.5f, 0.8f);

            Location center = player.getLocation().add(0, 1, 0);
            for (LivingEntity enemy : getNearbyEnemies(center, 5.0, player)) {
                dealAbilityDamage(player, enemy, 7.0, false);

                // Bleed DoT — damage every second for 4 seconds
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

            // Spinning sweep visual
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

            player.sendActionBar("§a✦ Harvest Storm!");

        } else {
            // NETHERITE DEFINITIVE: Reaper's Scythe
            // Creates a spectral scythe arc that sweeps 360 degrees twice
            // Dealing damage, life-stealing, and applying Wither II
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
                            player.sendMessage("§5✦ Reaper's Scythe healed §c" + String.format("%.1f", totalHealed) + " HP §5from enemies.");
                        }
                        return;
                    }

                    // Dual spinning arcs
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

                    // Damage on every 4th tick
                    if (tick % 4 == 0) {
                        for (LivingEntity enemy : getNearbyEnemies(center, radius, player)) {
                            dealAbilityDamage(player, enemy, 10.0, true);
                            enemy.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 80, 1));

                            // Life steal 15% of damage dealt
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

            player.sendActionBar("§5§l✦ REAPER'S SCYTHE ✦");
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
            // DIAMOND: Phantom Lunge — Dash 8 blocks in look direction, damage all in path
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

                    // Trail particles
                    player.getWorld().spawnParticle(Particle.END_ROD, loc.clone().add(0, 1, 0), 8, 0.3, 0.5, 0.3, 0.02);
                    player.getWorld().spawnParticle(Particle.CRIT, loc.clone().add(0, 1, 0), 5, 0.4, 0.3, 0.4, 0.15);

                    // White/cyan afterimage trail
                    player.getWorld().spawnParticle(Particle.DUST, loc.clone().add(0, 1, 0), 10,
                            0.2, 0.6, 0.2, 0,
                            new Particle.DustOptions(Color.fromRGB(180, 240, 255), 1.8f));

                    // Damage enemies in path (each only once)
                    for (LivingEntity enemy : getNearbyEnemies(loc, 2.0, player)) {
                        if (hit.add(enemy.getUniqueId())) {
                            double spearDamage = 8.0; // base spear damage × 1.5
                            dealAbilityDamage(player, enemy, spearDamage, false);
                            enemy.setVelocity(direction.clone().multiply(0.4).setY(0.3));
                            player.getWorld().spawnParticle(Particle.SWEEP_ATTACK, enemy.getLocation().add(0, 1, 0), 3, 0.2, 0.2, 0.2, 0);
                        }
                    }
                    tick++;
                }
            }.runTaskTimer(plugin, 0L, 1L);

            player.sendActionBar("§b✦ Phantom Lunge!");

        } else {
            // NETHERITE DEFINITIVE: Spear of the Void
            // Throw a spectral spear 30 blocks forward. Pierces all enemies.
            // On reaching max distance or hitting a block, DETONATES:
            // 5-block AoE pull + Slowness II for 3 seconds.
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

                    // Move projectile 1 block per tick
                    current.add(direction.clone().multiply(1.0));

                    // Check if hit a solid block
                    if (current.getBlock().getType().isSolid()) {
                        detonate(current);
                        detonated = true;
                        cancel();
                        return;
                    }

                    // Trail particles — dramatic dragon breath trail
                    player.getWorld().spawnParticle(Particle.DRAGON_BREATH, current, 8, 0.1, 0.1, 0.1, 0.02);
                    player.getWorld().spawnParticle(Particle.REVERSE_PORTAL, current, 5, 0.2, 0.2, 0.2, 0.1);
                    player.getWorld().spawnParticle(Particle.DUST, current, 5,
                            0.15, 0.15, 0.15, 0,
                            new Particle.DustOptions(Color.fromRGB(80, 0, 120), 2.0f));

                    // Pierce enemies in path
                    for (Entity e : current.getWorld().getNearbyEntities(current, 1.5, 1.5, 1.5)) {
                        if (e instanceof LivingEntity le && !(e instanceof Player) && !(e instanceof ArmorStand)) {
                            if (hit.add(e.getUniqueId())) {
                                double pierceDamage = 18.0; // base × 3
                                dealAbilityDamage(player, le, pierceDamage, true);
                                le.getWorld().spawnParticle(Particle.EXPLOSION, le.getLocation().add(0, 1, 0), 3, 0.2, 0.2, 0.2, 0);
                            }
                        }
                    }
                    tick++;
                }

                private void detonate(Location impactLoc) {
                    // Massive detonation
                    World world = impactLoc.getWorld();
                    world.spawnParticle(Particle.EXPLOSION_EMITTER, impactLoc, 5, 0, 0, 0, 0);
                    world.spawnParticle(Particle.REVERSE_PORTAL, impactLoc, 150, 3, 3, 3, 0.5);
                    world.spawnParticle(Particle.DRAGON_BREATH, impactLoc, 80, 4, 2, 4, 0.1);
                    world.spawnParticle(Particle.TOTEM_OF_UNDYING, impactLoc, 60, 2, 2, 2, 0.3);
                    world.playSound(impactLoc, Sound.ENTITY_WARDEN_SONIC_BOOM, 2.0f, 0.5f);
                    world.playSound(impactLoc, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.7f);

                    // Pull all enemies in 5-block radius toward impact and apply Slowness
                    for (LivingEntity enemy : getNearbyEnemies(impactLoc, 5.0, player)) {
                        Vector pull = impactLoc.toVector().subtract(enemy.getLocation().toVector()).normalize().multiply(1.5);
                        pull.setY(0.4);
                        enemy.setVelocity(pull);
                        enemy.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1));
                        if (!hit.contains(enemy.getUniqueId())) {
                            dealAbilityDamage(player, enemy, 10.0, true);
                        }
                    }

                    // Lingering void portal at impact
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

            player.sendActionBar("§5§l✦ SPEAR OF THE VOID ✦");
        }
    }

    // ================================================================
    // BOW — "Arrow Storm" (Diamond) / "Celestial Barrage" (Netherite)
    // ================================================================

    private void handleBowAbility(Player player, ItemStack item, boolean isDefinitive) {
        String abilityName = isDefinitive ? "Celestial Barrage" : "Arrow Storm";
        int cooldownSec = isDefinitive ? 25 : 12;

        if (isOnCooldown(player, abilityName)) {
            sendCooldownMessage(player, abilityName);
            return;
        }
        setCooldown(player, abilityName, cooldownSec);

        if (!isDefinitive) {
            // DIAMOND: Arrow Storm — Fire 8 arrows in a spread pattern
            player.playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 2.0f, 1.5f);

            Vector baseDir = player.getLocation().getDirection().normalize();
            for (int i = 0; i < 8; i++) {
                double spread = (i - 3.5) * 0.08;
                Vector arrowDir = baseDir.clone().add(new Vector(
                        Math.random() * 0.1 - 0.05 + spread,
                        Math.random() * 0.06 - 0.03,
                        Math.random() * 0.1 - 0.05
                )).normalize().multiply(2.5);

                Arrow arrow = player.launchProjectile(Arrow.class, arrowDir);
                arrow.setDamage(6.0);
                arrow.setPickupStatus(Arrow.PickupStatus.DISALLOWED);
                arrow.setCritical(true);

                // Schedule arrow removal after 5 seconds
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (arrow.isValid() && !arrow.isDead()) arrow.remove();
                }, 100L);
            }

            // Particle burst at launch
            player.getWorld().spawnParticle(Particle.CRIT, player.getEyeLocation().add(baseDir), 20, 0.5, 0.3, 0.5, 0.2);

            player.sendActionBar("§e✦ Arrow Storm — 8 arrows fired!");

        } else {
            // NETHERITE DEFINITIVE: Celestial Barrage
            // Rain 25 glowing arrows from the sky in a target area (15-block radius at cursor)
            // Each arrow explodes with particles on impact
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.6f, 2.0f);
            player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.5f);

            // Target location: where player is looking, up to 30 blocks away
            Location target = player.getTargetBlockExact(30) != null
                    ? player.getTargetBlockExact(30).getLocation().add(0, 1, 0)
                    : player.getLocation().add(player.getLocation().getDirection().multiply(15));

            // Beacon of light marking target area
            player.getWorld().spawnParticle(Particle.END_ROD, target, 50, 0, 10, 0, 0.05);

            // Rain arrows over 2 seconds
            new BukkitRunnable() {
                int arrowsFired = 0;
                @Override
                public void run() {
                    if (arrowsFired >= 25) { cancel(); return; }

                    // Random position within 6-block radius of target, high up
                    double angle = Math.random() * 2 * Math.PI;
                    double r = Math.random() * 6;
                    Location spawnLoc = target.clone().add(Math.cos(angle) * r, 25 + Math.random() * 10, Math.sin(angle) * r);

                    Arrow arrow = player.getWorld().spawnArrow(spawnLoc, new Vector(0, -3, 0), 2.0f, 0.5f);
                    arrow.setShooter(player);
                    arrow.setDamage(8.0);
                    arrow.setCritical(true);
                    arrow.setPickupStatus(Arrow.PickupStatus.DISALLOWED);
                    arrow.setGlowing(true);
                    arrow.setFireTicks(100);

                    // Particle trail on arrow
                    new BukkitRunnable() {
                        int t = 0;
                        @Override
                        public void run() {
                            if (t >= 60 || arrow.isDead() || !arrow.isValid() || arrow.isOnGround()) {
                                if (!arrow.isDead() && arrow.isValid()) {
                                    // Impact explosion particles
                                    arrow.getWorld().spawnParticle(Particle.EXPLOSION, arrow.getLocation(), 1, 0, 0, 0, 0);
                                    arrow.getWorld().spawnParticle(Particle.FLAME, arrow.getLocation(), 10, 0.5, 0.3, 0.5, 0.05);
                                    arrow.remove();
                                }
                                cancel();
                                return;
                            }
                            arrow.getWorld().spawnParticle(Particle.FIREWORK, arrow.getLocation(), 2, 0, 0, 0, 0.05);
                            t++;
                        }
                    }.runTaskTimer(plugin, 1L, 1L);

                    arrowsFired++;
                }
            }.runTaskTimer(plugin, 5L, 2L);

            // Final impact after arrows land
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, target, 3, 3, 0, 3, 0);
                player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, target, 80, 5, 3, 5, 0.3);
                player.getWorld().playSound(target, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.8f);
            }, 60L);

            player.sendActionBar("§6§l✦ CELESTIAL BARRAGE ✦");
        }
    }

    // ================================================================
    // CROSSBOW — "Grapple Shot" (Diamond) / "Annihilation Volley" (Netherite)
    // ================================================================

    private void handleCrossbowAbility(Player player, ItemStack item, boolean isDefinitive) {
        String abilityName = isDefinitive ? "Annihilation Volley" : "Grapple Shot";
        int cooldownSec = isDefinitive ? 22 : 10;

        if (isOnCooldown(player, abilityName)) {
            sendCooldownMessage(player, abilityName);
            return;
        }
        setCooldown(player, abilityName, cooldownSec);

        if (!isDefinitive) {
            // DIAMOND: Grapple Shot — Fire a grapple projectile that pulls you to the impact point
            // Also damages and stuns enemies near the impact
            player.playSound(player.getLocation(), Sound.ENTITY_FISHING_BOBBER_THROW, 2.0f, 0.5f);

            Vector direction = player.getLocation().getDirection().normalize();
            Location projectileLoc = player.getEyeLocation().clone();

            new BukkitRunnable() {
                int tick = 0;
                Location current = projectileLoc.clone();
                @Override
                public void run() {
                    if (tick >= 25) { cancel(); return; }

                    current.add(direction.clone().multiply(1.5));

                    // Trail
                    player.getWorld().spawnParticle(Particle.CRIT, current, 3, 0.05, 0.05, 0.05, 0);
                    player.getWorld().spawnParticle(Particle.DUST, current, 3,
                            0.05, 0.05, 0.05, 0,
                            new Particle.DustOptions(Color.fromRGB(150, 150, 150), 1.0f));

                    // Check for solid block hit
                    if (current.getBlock().getType().isSolid()) {
                        // Pull player to impact point
                        Vector pull = current.toVector().subtract(player.getLocation().toVector()).normalize().multiply(2.0);
                        pull.setY(Math.max(pull.getY(), 0.5));
                        player.setVelocity(pull);
                        player.playSound(player.getLocation(), Sound.ENTITY_BREEZE_LAND, 1.5f, 1.0f);

                        // Damage and stun nearby enemies
                        for (LivingEntity enemy : getNearbyEnemies(current, 3.0, player)) {
                            dealAbilityDamage(player, enemy, 5.0, false);
                            enemy.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 3));
                        }
                        player.getWorld().spawnParticle(Particle.EXPLOSION, current, 1, 0, 0, 0, 0);
                        cancel();
                        return;
                    }
                    tick++;
                }
            }.runTaskTimer(plugin, 0L, 1L);

            player.sendActionBar("§7✦ Grapple Shot!");

        } else {
            // NETHERITE DEFINITIVE: Annihilation Volley
            // Fire 5 explosive bolts in a tight spread. Each bolt explodes on impact dealing
            // AoE damage, leaving fire, and applying Weakness.
            player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 0.8f, 2.0f);
            player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 2.0f, 0.5f);

            Vector baseDir = player.getLocation().getDirection().normalize();

            for (int i = 0; i < 5; i++) {
                final int index = i;
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    double spread = (index - 2) * 0.06;
                    Vector boltDir = baseDir.clone().add(new Vector(spread, 0, spread * 0.5)).normalize().multiply(2.0);

                    Arrow bolt = player.launchProjectile(Arrow.class, boltDir);
                    bolt.setDamage(4.0);
                    bolt.setCritical(true);
                    bolt.setFireTicks(100);
                    bolt.setGlowing(true);
                    bolt.setPickupStatus(Arrow.PickupStatus.DISALLOWED);

                    new BukkitRunnable() {
                        int t = 0;
                        @Override
                        public void run() {
                            if (t >= 60 || bolt.isDead() || !bolt.isValid() || bolt.isOnGround()) {
                                Location impact = bolt.getLocation();
                                // Explosion on impact
                                bolt.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, impact, 1, 0, 0, 0, 0);
                                bolt.getWorld().spawnParticle(Particle.FLAME, impact, 30, 2, 1, 2, 0.1);
                                bolt.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, impact, 15, 1.5, 1, 1.5, 0.05);
                                bolt.getWorld().playSound(impact, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 1.0f);

                                for (LivingEntity enemy : getNearbyEnemies(impact, 4.0, player)) {
                                    dealAbilityDamage(player, enemy, 12.0, true);
                                    enemy.setFireTicks(60);
                                    enemy.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 1));
                                }
                                if (bolt.isValid()) bolt.remove();
                                cancel();
                                return;
                            }
                            // Trail
                            bolt.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, bolt.getLocation(), 3, 0.05, 0.05, 0.05, 0);
                            t++;
                        }
                    }.runTaskTimer(plugin, 1L, 1L);
                }, i * 4L); // Staggered fire
            }

            player.sendActionBar("§5§l✦ ANNIHILATION VOLLEY ✦");
        }
    }

    // ================================================================
    // TRIDENT — "Riptide Surge" (Diamond) / "Poseidon's Wrath" (Netherite)
    // ================================================================

    private void handleTridentAbility(Player player, ItemStack item, boolean isDefinitive) {
        String abilityName = isDefinitive ? "Poseidon's Wrath" : "Riptide Surge";
        int cooldownSec = isDefinitive ? 22 : 12;

        if (isOnCooldown(player, abilityName)) {
            sendCooldownMessage(player, abilityName);
            return;
        }
        setCooldown(player, abilityName, cooldownSec);

        if (!isDefinitive) {
            // DIAMOND: Riptide Surge — Launch forward (works without rain!) leaving a water trail
            // Enemies in path take damage and get knocked aside
            Vector direction = player.getLocation().getDirection().normalize();
            player.setVelocity(direction.clone().multiply(2.5).setY(0.5));
            player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_RIPTIDE_3, 1.5f, 1.2f);

            new BukkitRunnable() {
                int tick = 0;
                @Override
                public void run() {
                    if (tick >= 12) { cancel(); return; }
                    Location loc = player.getLocation();
                    player.getWorld().spawnParticle(Particle.SPLASH, loc, 15, 0.4, 0.4, 0.4, 0.1);
                    player.getWorld().spawnParticle(Particle.DRIPPING_WATER, loc, 8, 0.3, 0.5, 0.3, 0);
                    player.getWorld().spawnParticle(Particle.BUBBLE_COLUMN_UP, loc, 5, 0.2, 0.2, 0.2, 0);

                    for (LivingEntity enemy : getNearbyEnemies(loc, 2.5, player)) {
                        dealAbilityDamage(player, enemy, 7.0, false);
                        Vector knockback = loc.getDirection().getCrossProduct(new Vector(0, 1, 0)).normalize().multiply(1.0);
                        knockback.setY(0.5);
                        enemy.setVelocity(knockback);
                    }
                    tick++;
                }
            }.runTaskTimer(plugin, 0L, 1L);

            player.sendActionBar("§b✦ Riptide Surge!");

        } else {
            // NETHERITE DEFINITIVE: Poseidon's Wrath
            // Summon a tidal wave that crashes outward from the player (15-block range)
            // Enemies are dragged inward, struck by lightning, and drowned with water particles
            player.playSound(player.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 1.0f, 0.5f);
            player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_THUNDER, 2.0f, 0.7f);
            player.playSound(player.getLocation(), Sound.WEATHER_RAIN_ABOVE, 2.0f, 0.5f);

            Location center = player.getLocation();

            new BukkitRunnable() {
                int tick = 0;
                @Override
                public void run() {
                    if (tick >= 40) { cancel(); return; }

                    double radius = tick * 0.4;

                    // Tidal wave ring
                    for (int i = 0; i < 30; i++) {
                        double angle = (2 * Math.PI / 30) * i + (tick * 0.2);
                        Location pLoc = center.clone().add(Math.cos(angle) * radius, 0.5, Math.sin(angle) * radius);
                        player.getWorld().spawnParticle(Particle.SPLASH, pLoc, 5, 0.2, 0.8, 0.2, 0.1);
                        player.getWorld().spawnParticle(Particle.DRIPPING_WATER, pLoc, 3, 0.1, 0.5, 0.1, 0);
                        if (tick > 10) {
                            player.getWorld().spawnParticle(Particle.BUBBLE_COLUMN_UP, pLoc, 2, 0.1, 1, 0.1, 0);
                        }
                    }

                    // Lightning strikes on random enemies every 8 ticks
                    if (tick % 8 == 0 && tick > 5) {
                        List<LivingEntity> enemies = getNearbyEnemies(center, radius, player);
                        if (!enemies.isEmpty()) {
                            LivingEntity target = enemies.get((int) (Math.random() * enemies.size()));
                            player.getWorld().strikeLightningEffect(target.getLocation());
                            dealAbilityDamage(player, target, 15.0, true);
                            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 2));
                        }
                    }

                    // Pull enemies inward every 5 ticks
                    if (tick % 5 == 0) {
                        for (LivingEntity enemy : getNearbyEnemies(center, radius, player)) {
                            Vector pull = center.toVector().subtract(enemy.getLocation().toVector()).normalize().multiply(0.6);
                            pull.setY(0.2);
                            enemy.setVelocity(pull);
                            dealAbilityDamage(player, enemy, 3.0, true);
                        }
                    }
                    tick++;
                }
            }.runTaskTimer(plugin, 0L, 1L);

            // Final lightning storm
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                for (int i = 0; i < 5; i++) {
                    double a = Math.random() * 2 * Math.PI;
                    double r = Math.random() * 8;
                    Location strike = center.clone().add(Math.cos(a) * r, 0, Math.sin(a) * r);
                    player.getWorld().strikeLightningEffect(strike);
                }
                player.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, center, 2, 0, 0, 0, 0);
            }, 42L);

            player.sendActionBar("§1§l✦ POSEIDON'S WRATH ✦");
        }
    }

    // ================================================================
    // MACE — "Ground Slam" (Diamond) / "World Breaker" (Netherite)
    // ================================================================

    private void handleMaceAbility(Player player, ItemStack item, boolean isDefinitive) {
        String abilityName = isDefinitive ? "World Breaker" : "Ground Slam";
        int cooldownSec = isDefinitive ? 25 : 12;

        if (isOnCooldown(player, abilityName)) {
            sendCooldownMessage(player, abilityName);
            return;
        }
        setCooldown(player, abilityName, cooldownSec);

        if (!isDefinitive) {
            // DIAMOND: Ground Slam — Jump up and slam down, AoE damage in 6 blocks
            player.setVelocity(new Vector(0, 1.5, 0));
            player.playSound(player.getLocation(), Sound.ITEM_MACE_SMASH_GROUND, 2.0f, 0.8f);

            // Slam after brief delay
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.setVelocity(new Vector(0, -2.0, 0));

                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    Location impactLoc = player.getLocation();
                    player.getWorld().spawnParticle(Particle.EXPLOSION, impactLoc, 3, 1, 0, 1, 0);
                    player.getWorld().spawnParticle(Particle.BLOCK, impactLoc, 40, 3, 0.5, 3, 0.1,
                            Material.STONE.createBlockData());
                    player.playSound(impactLoc, Sound.ITEM_MACE_SMASH_GROUND_HEAVY, 2.0f, 0.7f);

                    for (LivingEntity enemy : getNearbyEnemies(impactLoc, 6.0, player)) {
                        dealAbilityDamage(player, enemy, 12.0, false);
                        Vector kb = enemy.getLocation().toVector().subtract(impactLoc.toVector()).normalize().multiply(1.0);
                        kb.setY(0.7);
                        enemy.setVelocity(kb);
                        enemy.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 2));
                    }
                }, 8L);
            }, 10L);

            player.sendActionBar("§e✦ Ground Slam!");

        } else {
            // NETHERITE DEFINITIVE: World Breaker
            // Launch player very high, then slam with apocalyptic force
            // 15-block AoE, multiple damage waves, ground fissure effect
            player.setVelocity(new Vector(0, 3.0, 0));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 10, 0)); // Brief float
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 2.0f, 0.5f);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.removePotionEffect(PotionEffectType.SLOW_FALLING);
                player.setVelocity(new Vector(0, -4.0, 0));

                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    Location impact = player.getLocation();
                    player.playSound(impact, Sound.ENTITY_WARDEN_SONIC_BOOM, 2.0f, 0.3f);
                    player.playSound(impact, Sound.ITEM_MACE_SMASH_GROUND_HEAVY, 2.0f, 0.3f);
                    player.playSound(impact, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.5f);

                    // Massive visual explosion
                    player.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, impact, 5, 2, 0, 2, 0);
                    player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, impact, 150, 5, 5, 5, 0.5);
                    player.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, impact, 50, 6, 1, 6, 0.05);

                    // Prevent player fall damage
                    player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 20, 255));

                    // Three expanding damage waves
                    for (int wave = 0; wave < 3; wave++) {
                        final double waveRadius = 5.0 + (wave * 5.0);
                        final double waveDmg = 25.0 - (wave * 6.0);
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            // Ring particles
                            for (int i = 0; i < 50; i++) {
                                double angle = (2 * Math.PI / 50) * i;
                                Location ringLoc = impact.clone().add(Math.cos(angle) * waveRadius, 0.3, Math.sin(angle) * waveRadius);
                                player.getWorld().spawnParticle(Particle.FLAME, ringLoc, 3, 0, 0.5, 0, 0.01);
                                player.getWorld().spawnParticle(Particle.BLOCK, ringLoc, 5, 0.5, 0.5, 0.5, 0,
                                        Material.NETHERRACK.createBlockData());
                            }

                            for (LivingEntity enemy : getNearbyEnemies(impact, waveRadius, player)) {
                                dealAbilityDamage(player, enemy, waveDmg, true);
                                Vector launch = enemy.getLocation().toVector().subtract(impact.toVector()).normalize().multiply(2.0);
                                launch.setY(1.2);
                                enemy.setVelocity(launch);
                                enemy.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 2));
                                enemy.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 3));
                            }
                            player.getWorld().playSound(impact, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.7f + (float)(waveRadius * 0.02));
                        }, wave * 5L);
                    }
                }, 10L);
            }, 15L);

            player.sendActionBar("§4§l✦ WORLD BREAKER ✦");
        }
    }

    // ================================================================
    // ON-HIT: Bloodthirst lifesteal check (Axe Diamond ability)
    // ================================================================

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;

        ItemStack weapon = player.getInventory().getItemInMainHand();
        if (weapon == null || !weapon.hasItemMeta()) return;

        // Check for active Bloodthirst (Axe Diamond ability)
        NamespacedKey bloodthirstKey = new NamespacedKey(plugin, "bloodthirst_active");
        PersistentDataContainer pdc = weapon.getItemMeta().getPersistentDataContainer();
        if (pdc.has(bloodthirstKey, PersistentDataType.LONG)) {
            long expiry = pdc.getOrDefault(bloodthirstKey, PersistentDataType.LONG, 0L);
            if (System.currentTimeMillis() < expiry) {
                // Heal 30% of damage dealt
                double heal = event.getFinalDamage() * 0.3;
                double newHealth = Math.min(player.getHealth() + heal,
                        player.getAttribute(Attribute.MAX_HEALTH).getValue());
                player.setHealth(newHealth);
                player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0, 2, 0), 2, 0.3, 0.2, 0.3, 0);
            } else {
                // Expired — remove the key
                ItemMeta meta = weapon.getItemMeta();
                meta.getPersistentDataContainer().remove(bloodthirstKey);
                weapon.setItemMeta(meta);
            }
        }

        // Netherite +2% per enchantment damage bonus
        int superTier = getSuperTier(weapon);
        if (superTier >= 3) {
            int totalEnchants = enchantManager.countEnchantments(weapon);
            // Also count vanilla enchantments
            if (weapon.hasItemMeta() && weapon.getItemMeta().hasEnchants()) {
                totalEnchants += weapon.getItemMeta().getEnchants().size();
            }
            if (totalEnchants > 0) {
                double bonusPercent = totalEnchants * config.getSuperNethPerEnchantBonus();
                double bonusDamage = event.getDamage() * (bonusPercent / 100.0);
                event.setDamage(event.getDamage() + bonusDamage);
            }
        }
    }
}

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
 * WeaponAbilityListener — v1.3.0 FINAL
 * Right-click abilities for all 10 weapon classes.
 * Requires Super Diamond (tier 2) or Super Netherite (tier 3).
 *
 * CHANGES IN THIS VERSION:
 *   - Fixed Sound.BLOCK_LIGHTNING_ROD_TOGGLE_ON → Sound.ENTITY_LIGHTNING_BOLT_IMPACT
 *   - Reduced ALL cooldowns (see table in reference doc Section 10)
 *   - COMPLETELY REVAMPED Reaper's Scythe (Sickle Definitive):
 *       Dark soul vortex, 10-block radius, pulls enemies in, Wither III,
 *       blue fire + soul particles, massive damage, healing factor, explosion finale
 *   - FIXED Meteor Strike (Mace Definitive):
 *       Grants brief invulnerability (Resistance 255) before impact instead of killing player
 *   - All Netherite Definitive abilities deal 30% damage to Ender Dragon
 */
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
        if (superTier < 2) return; // Only tier 2+ get abilities

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
            // DIAMOND: Dash Strike — fast dash forward dealing 8 dmg/tick to enemies in path
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
            // NETHERITE DEFINITIVE: Dimensional Cleave — expanding rift AoE, 15/tick + 25 final
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

            // Final explosion after 30 ticks (1.5 seconds)
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
            // DIAMOND: Bloodthirst — 5s buff: lifesteal + Haste + Strength
            player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 100, 1));
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 100, 0));
            player.playSound(player.getLocation(), Sound.ENTITY_RAVAGER_ROAR, 0.8f, 1.2f);

            // Store bloodthirst expiry on the weapon
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

            player.sendActionBar("\u00a7c\u00a7l\u2620 BLOODTHIRST ACTIVE \u2620");

        } else {
            // NETHERITE DEFINITIVE: Ragnarok Cleave — 3 expanding shockwaves 20/16/12 dmg
            player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1.5f, 0.7f);
            player.playSound(player.getLocation(), Sound.ITEM_MACE_SMASH_GROUND_HEAVY, 2.0f, 0.8f);

            Location center = player.getLocation();

            // Expanding ground particles
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

            // 3 damage waves: 4-block (20 dmg), 7-block (16 dmg), 10-block (12 dmg)
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

        // Scan for ores in radius
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
                    ? "\u00a75\u00a7l\u26cf SEISMIC RESONANCE: \u00a7f" + totalOres + " ores detected"
                    : "\u00a7e\u26cf Ore Pulse: \u00a7f" + totalOres + " ores detected";
            if (debrisCount > 0) {
                msg += " \u00a77| \u00a74\u00a7l" + debrisCount + " Ancient Debris!";
            }
            player.sendActionBar(msg);
            if (debrisCount > 0) {
                player.sendMessage("\u00a74\u00a7l\u26a0 " + debrisCount + " Ancient Debris detected within " + debrisRadius + " blocks!");
            }
        }

        // Expanding pulse ring visual
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

        // Player-specific ore highlighting (only caster sees particles)
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

    // Ore type classification with colored particles
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
            // DIAMOND: Earthen Wall — 6 dmg + launch + Resistance II
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

            player.sendActionBar("\u00a7e\u26e8 Earthen Wall \u2014 Enemies launched!");

        } else {
            // NETHERITE DEFINITIVE: Tectonic Upheaval — 14/wave + Resistance II + Slowness III
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
                    // Damage pulse every 10 ticks offset by 5
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

            player.sendActionBar("\u00a74\u00a7l\u26e8 TECTONIC UPHEAVAL \u26e8");
        }
    }

    // ================================================================
    // SICKLE (HOE) — "Harvest Storm" (Diamond) / "Reaper's Scythe" (Netherite)
    // *** COMPLETELY REVAMPED v1.3.0 DEFINITIVE ABILITY ***
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
            // DIAMOND: Harvest Storm — 7 initial + 24 bleed (5-block AoE spin)
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.5f, 0.8f);

            Location center = player.getLocation().add(0, 1, 0);
            for (LivingEntity enemy : getNearbyEnemies(center, 5.0, player)) {
                dealAbilityDamage(player, enemy, 7.0, false);

                // 4 bleed ticks dealing 6 each = 24 total bleed
                new BukkitRunnable() {
                    int bleedTicks = 0;
                    @Override
                    public void run() {
                        if (bleedTicks >= 4 || enemy.isDead()) { cancel(); return; }
                        enemy.damage(6.0);
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

            player.sendActionBar("\u00a7a\u2620 Harvest Storm!");

        } else {
            // ==================================================================
            // NETHERITE DEFINITIVE: REAPER'S SCYTHE — COMPLETE REWORK v1.3.0
            // ==================================================================
            // Phase 1 (ticks 0-20):  Dark summoning vortex — pull enemies inward,
            //                        soul fire + wither particles, Wither III applied
            // Phase 2 (ticks 20-60): Soul Reap Cyclone — spinning blue fire blades,
            //                        12 dmg/pulse every 5 ticks, Wither III refresh,
            //                        player heals 3 HP per enemy per pulse
            // Phase 3 (tick 60):     Death Harvest Explosion — 20 dmg AoE burst,
            //                        massive soul fire eruption, enemies launched
            // Total potential damage: ~12*8 pulses + 20 explosion = 116+ per enemy
            // ==================================================================

            // Sound: Wither spawn + Warden emerge + Ender Dragon growl
            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.8f, 1.5f);
            player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_EMERGE, 1.0f, 0.5f);
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 2.0f);

            Location center = player.getLocation().clone();
            final double VORTEX_RADIUS = 10.0;

            // Grant the player Resistance I during the entire ability (protection)
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 80, 0));

            new BukkitRunnable() {
                int tick = 0;
                double totalHealed = 0;
                int totalDamageDealt = 0;

                @Override
                public void run() {
                    if (tick > 60 || !player.isOnline()) {
                        cancel();
                        // Summary message
                        if (totalHealed > 0 || totalDamageDealt > 0) {
                            player.sendMessage("\u00a75\u00a7l\u2620 Reaper's Scythe \u00a7r\u00a75harvested "
                                    + "\u00a7c" + totalDamageDealt + " total damage \u00a75and healed "
                                    + "\u00a7a" + String.format("%.1f", totalHealed) + " HP\u00a75.");
                        }
                        return;
                    }

                    World world = center.getWorld();

                    // ==========================================
                    // PHASE 1: DARK SUMMONING VORTEX (ticks 0-20)
                    // ==========================================
                    if (tick <= 20) {
                        // Contracting spiral particles — blue/purple soul fire
                        double spiralRadius = VORTEX_RADIUS * (1.0 - (tick / 25.0));
                        for (int arm = 0; arm < 3; arm++) {
                            double baseAngle = (2 * Math.PI / 3) * arm + (tick * 0.4);
                            for (double r = spiralRadius; r > 0; r -= 0.8) {
                                double angle = baseAngle + (spiralRadius - r) * 0.3;
                                double x = Math.cos(angle) * r;
                                double z = Math.sin(angle) * r;
                                Location pLoc = center.clone().add(x, 0.2, z);

                                // Blue soul fire
                                world.spawnParticle(Particle.SOUL_FIRE_FLAME, pLoc, 1, 0.05, 0.1, 0.05, 0.005);
                                // Dark wither particles at outer edge
                                if (r > spiralRadius * 0.6) {
                                    world.spawnParticle(Particle.DUST, pLoc.clone().add(0, 0.5, 0), 1,
                                            0.1, 0.2, 0.1, 0,
                                            new Particle.DustOptions(Color.fromRGB(30, 0, 50), 2.0f));
                                }
                            }
                        }

                        // Rising column of soul particles at center
                        for (double y = 0; y < 3; y += 0.3) {
                            double cAngle = tick * 0.5 + y * 2;
                            double cR = 0.5 + y * 0.3;
                            world.spawnParticle(Particle.SOUL_FIRE_FLAME,
                                    center.clone().add(Math.cos(cAngle) * cR, y, Math.sin(cAngle) * cR),
                                    1, 0, 0, 0, 0);
                        }

                        // Pull enemies toward center every 4 ticks
                        if (tick % 4 == 0) {
                            for (LivingEntity enemy : getNearbyEnemies(center, VORTEX_RADIUS, player)) {
                                Vector pull = center.toVector().subtract(enemy.getLocation().toVector()).normalize().multiply(0.6);
                                pull.setY(0.1);
                                enemy.setVelocity(enemy.getVelocity().add(pull));
                                // Apply Wither III to all caught enemies
                                enemy.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 2));
                                enemy.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1));
                                // Wither particle on enemy
                                world.spawnParticle(Particle.DUST, enemy.getLocation().add(0, 1, 0), 5,
                                        0.3, 0.5, 0.3, 0,
                                        new Particle.DustOptions(Color.fromRGB(20, 0, 30), 1.8f));
                            }
                        }

                        // Heartbeat sound every 10 ticks
                        if (tick % 10 == 0) {
                            world.playSound(center, Sound.ENTITY_WARDEN_HEARTBEAT, 2.0f, 0.5f);
                        }
                    }

                    // ==========================================
                    // PHASE 2: SOUL REAP CYCLONE (ticks 20-60)
                    // ==========================================
                    if (tick >= 20 && tick <= 60) {
                        // Spinning double-blade blue fire arcs
                        double blade1Angle = (tick - 20) * 0.45;
                        double blade2Angle = blade1Angle + Math.PI;

                        for (double bladeAngle : new double[]{blade1Angle, blade2Angle}) {
                            for (double r = 0.5; r <= 8.0; r += 0.4) {
                                double angle = bladeAngle + (r * 0.15);
                                double x = Math.cos(angle) * r;
                                double z = Math.sin(angle) * r;
                                Location pLoc = center.clone().add(x, 0.3 + Math.sin(r + tick * 0.2) * 0.5, z);

                                // Blue soul fire blade
                                world.spawnParticle(Particle.SOUL_FIRE_FLAME, pLoc, 1, 0.02, 0.05, 0.02, 0.002);

                                // Outer edge: dragon breath + enchant glow
                                if (r > 6.0) {
                                    world.spawnParticle(Particle.DRAGON_BREATH, pLoc, 1, 0.05, 0.05, 0.05, 0.01);
                                    world.spawnParticle(Particle.ENCHANT, pLoc, 1, 0, 0.3, 0, 0.05);
                                }

                                // Inner core: wither roses / dark dust
                                if (r < 2.0) {
                                    world.spawnParticle(Particle.DUST, pLoc, 1, 0.1, 0.1, 0.1, 0,
                                            new Particle.DustOptions(Color.fromRGB(60, 0, 80), 1.5f));
                                }
                            }
                        }

                        // Ground-level ring of blue fire
                        if (tick % 2 == 0) {
                            for (int i = 0; i < 24; i++) {
                                double ringAngle = (2 * Math.PI / 24) * i + tick * 0.1;
                                double ringR = 8.0 + Math.sin(tick * 0.3 + i) * 0.5;
                                Location ringLoc = center.clone().add(Math.cos(ringAngle) * ringR, 0.1, Math.sin(ringAngle) * ringR);
                                world.spawnParticle(Particle.SOUL_FIRE_FLAME, ringLoc, 1, 0, 0.1, 0, 0.005);
                            }
                        }

                        // Center pillar: upward soul stream
                        for (double y = 0; y < 5; y += 0.5) {
                            world.spawnParticle(Particle.SOUL_FIRE_FLAME,
                                    center.clone().add(Math.sin(tick * 0.3 + y) * 0.3, y, Math.cos(tick * 0.3 + y) * 0.3),
                                    1, 0, 0, 0, 0.01);
                        }

                        // DAMAGE PULSE every 5 ticks (8 pulses total in 40 ticks)
                        if (tick % 5 == 0) {
                            List<LivingEntity> enemies = getNearbyEnemies(center, VORTEX_RADIUS, player);
                            for (LivingEntity enemy : enemies) {
                                // 12 damage per pulse
                                dealAbilityDamage(player, enemy, 12.0, true);
                                totalDamageDealt += 12;

                                // Refresh Wither III
                                enemy.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 80, 2));
                                // Slowness II
                                enemy.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1));

                                // Wither + damage particles on enemy
                                world.spawnParticle(Particle.SOUL_FIRE_FLAME, enemy.getLocation().add(0, 1, 0),
                                        8, 0.3, 0.5, 0.3, 0.02);
                                world.spawnParticle(Particle.DAMAGE_INDICATOR, enemy.getLocation().add(0, 1.5, 0),
                                        3, 0.2, 0.2, 0.2, 0.1);

                                // Light pull toward center to keep them in the vortex
                                Vector pull = center.toVector().subtract(enemy.getLocation().toVector());
                                if (pull.lengthSquared() > 1) {
                                    pull = pull.normalize().multiply(0.3);
                                    pull.setY(0.05);
                                    enemy.setVelocity(enemy.getVelocity().add(pull));
                                }

                                // HEALING: 3 HP per enemy per pulse
                                double maxHp = player.getAttribute(Attribute.MAX_HEALTH).getValue();
                                double heal = 3.0;
                                double newHealth = Math.min(player.getHealth() + heal, maxHp);
                                player.setHealth(newHealth);
                                totalHealed += heal;
                            }

                            // Healing visual on player
                            if (!enemies.isEmpty()) {
                                world.spawnParticle(Particle.HEART, player.getLocation().add(0, 2.2, 0),
                                        3, 0.3, 0.3, 0.3, 0);
                                world.spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation().add(0, 1, 0),
                                        5, 0.3, 0.5, 0.3, 0.05);
                            }

                            // Scythe slash sound
                            world.playSound(center, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.5f, 0.5f);
                        }

                        // Ambient reaper sounds
                        if (tick % 15 == 0) {
                            world.playSound(center, Sound.ENTITY_WARDEN_HEARTBEAT, 1.5f, 0.4f);
                        }
                        if (tick == 30) {
                            world.playSound(center, Sound.ENTITY_WITHER_AMBIENT, 0.5f, 1.5f);
                        }
                        if (tick == 45) {
                            world.playSound(center, Sound.ENTITY_WARDEN_SONIC_BOOM, 0.5f, 1.8f);
                        }
                    }

                    // ==========================================
                    // PHASE 3: DEATH HARVEST EXPLOSION (tick 60)
                    // ==========================================
                    if (tick == 60) {
                        // Massive soul fire eruption
                        world.spawnParticle(Particle.SOUL_FIRE_FLAME, center.clone().add(0, 1, 0),
                                200, 5, 3, 5, 0.15);
                        world.spawnParticle(Particle.DRAGON_BREATH, center.clone().add(0, 1, 0),
                                120, 6, 2, 6, 0.1);
                        world.spawnParticle(Particle.EXPLOSION_EMITTER, center.clone().add(0, 1, 0),
                                5, 1, 1, 1, 0);
                        world.spawnParticle(Particle.REVERSE_PORTAL, center.clone().add(0, 2, 0),
                                100, 4, 3, 4, 0.3);
                        world.spawnParticle(Particle.TOTEM_OF_UNDYING, center.clone().add(0, 1, 0),
                                80, 3, 2, 3, 0.4);

                        // Upward soul beam
                        for (double y = 0; y < 15; y += 0.3) {
                            double beamR = Math.max(0, 2.0 - y * 0.12);
                            for (int i = 0; i < 4; i++) {
                                double bAngle = (2 * Math.PI / 4) * i + y * 0.5;
                                world.spawnParticle(Particle.SOUL_FIRE_FLAME,
                                        center.clone().add(Math.cos(bAngle) * beamR, y, Math.sin(bAngle) * beamR),
                                        1, 0, 0, 0, 0.02);
                            }
                        }

                        // Explosion sounds
                        world.playSound(center, Sound.ENTITY_WARDEN_SONIC_BOOM, 2.0f, 0.3f);
                        world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.5f);
                        world.playSound(center, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 2.0f, 0.6f);

                        // 20 damage explosion + launch enemies outward
                        for (LivingEntity enemy : getNearbyEnemies(center, VORTEX_RADIUS + 2, player)) {
                            dealAbilityDamage(player, enemy, 20.0, true);
                            totalDamageDealt += 20;

                            // Launch outward
                            Vector launch = enemy.getLocation().toVector().subtract(center.toVector()).normalize().multiply(2.0);
                            launch.setY(1.0);
                            enemy.setVelocity(launch);

                            // Final Wither III + Darkness
                            enemy.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 160, 2));
                            enemy.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 100, 0));

                            // Death mark particles on each enemy
                            world.spawnParticle(Particle.SOUL_FIRE_FLAME, enemy.getLocation().add(0, 1, 0),
                                    15, 0.3, 0.5, 0.3, 0.05);
                        }

                        // Lingering soul vortex particles for 2 more seconds
                        new BukkitRunnable() {
                            int lingerTick = 0;
                            @Override
                            public void run() {
                                if (lingerTick >= 40) { cancel(); return; }
                                double fade = 1.0 - (lingerTick / 40.0);
                                int count = (int) (15 * fade);
                                world.spawnParticle(Particle.SOUL_FIRE_FLAME, center.clone().add(0, 0.5, 0),
                                        count, 4 * fade, 1.5 * fade, 4 * fade, 0.02);
                                world.spawnParticle(Particle.DRAGON_BREATH, center.clone().add(0, 0.3, 0),
                                        count / 2, 3 * fade, 0.5, 3 * fade, 0.01);
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
            // DIAMOND: Phantom Lunge — 8 piercing damage, 8-block dash
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

            player.sendActionBar("\u00a7b\u2694 Phantom Lunge!");

        } else {
            // NETHERITE DEFINITIVE: Spear of the Void — 18 pierce + 10 detonation (30-block projectile)
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

                    // Stop on solid block
                    if (current.getBlock().getType().isSolid()) {
                        detonate(current);
                        detonated = true;
                        cancel();
                        return;
                    }

                    // Projectile trail particles
                    player.getWorld().spawnParticle(Particle.DRAGON_BREATH, current, 8, 0.1, 0.1, 0.1, 0.02);
                    player.getWorld().spawnParticle(Particle.REVERSE_PORTAL, current, 5, 0.2, 0.2, 0.2, 0.1);
                    player.getWorld().spawnParticle(Particle.DUST, current, 5,
                            0.15, 0.15, 0.15, 0,
                            new Particle.DustOptions(Color.fromRGB(80, 0, 120), 2.0f));

                    // Pierce enemies for 18 dmg
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
                    // Explosion visuals
                    world.spawnParticle(Particle.EXPLOSION_EMITTER, impactLoc, 5, 0, 0, 0, 0);
                    world.spawnParticle(Particle.REVERSE_PORTAL, impactLoc, 150, 3, 3, 3, 0.5);
                    world.spawnParticle(Particle.DRAGON_BREATH, impactLoc, 80, 4, 2, 4, 0.1);
                    world.spawnParticle(Particle.TOTEM_OF_UNDYING, impactLoc, 60, 2, 2, 2, 0.3);
                    world.playSound(impactLoc, Sound.ENTITY_WARDEN_SONIC_BOOM, 2.0f, 0.5f);
                    world.playSound(impactLoc, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.7f);

                    // 10 detonation damage + pull + slow
                    for (LivingEntity enemy : getNearbyEnemies(impactLoc, 5.0, player)) {
                        Vector pull = impactLoc.toVector().subtract(enemy.getLocation().toVector()).normalize().multiply(1.5);
                        pull.setY(0.4);
                        enemy.setVelocity(pull);
                        enemy.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1));
                        if (!hit.contains(enemy.getUniqueId())) {
                            dealAbilityDamage(player, enemy, 10.0, true);
                        }
                    }

                    // Lingering void rift
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

            player.sendActionBar("\u00a75\u00a7l\u2694 SPEAR OF THE VOID \u2694");
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
            // DIAMOND: Arrow Storm — 5 rapid-fire arrows, 6 dmg each
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

            player.sendActionBar("\u00a7e\u27b3 Arrow Storm!");

        } else {
            // NETHERITE DEFINITIVE: Celestial Volley — 12 arrows from sky, 10/arrow + 8 AoE impact
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0f, 1.5f);
            player.playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 2.0f, 0.5f);

            Location target = player.getLocation().add(player.getLocation().getDirection().multiply(10));
            target.setY(player.getLocation().getY());

            // Launch visual
            player.getWorld().spawnParticle(Particle.FIREWORK, player.getLocation().add(0, 2, 0), 30, 0.5, 1, 0.5, 0.2);

            // Delayed rain of arrows from the sky
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

                        // Glowing trail
                        new BukkitRunnable() {
                            int t = 0;
                            @Override
                            public void run() {
                                if (t >= 30 || arrow.isDead() || arrow.isOnGround()) {
                                    // Impact AoE
                                    if (arrow.isOnGround() || arrow.isDead()) {
                                        Location impactLoc = arrow.getLocation();
                                        player.getWorld().spawnParticle(Particle.EXPLOSION, impactLoc, 2, 0, 0, 0, 0);
                                        player.getWorld().spawnParticle(Particle.FLAME, impactLoc, 15, 1, 0.5, 1, 0.05);
                                        for (LivingEntity enemy : getNearbyEnemies(impactLoc, 3.0, player)) {
                                            dealAbilityDamage(player, enemy, 8.0, true);
                                        }
                                    }
                                    cancel();
                                    return;
                                }
                                player.getWorld().spawnParticle(Particle.END_ROD, arrow.getLocation(), 3, 0.1, 0.1, 0.1, 0.02);
                                player.getWorld().spawnParticle(Particle.FIREWORK, arrow.getLocation(), 2, 0.05, 0.05, 0.05, 0.01);
                                t++;
                            }
                        }.runTaskTimer(plugin, 0L, 1L);
                    }, index * 2L);
                }
            }, 15L);

            player.sendActionBar("\u00a76\u00a7l\u2600 CELESTIAL VOLLEY \u2600");
        }
    }

    // ================================================================
    // CROSSBOW — "Chain Shot" (Diamond) / "Thunder Barrage" (Netherite)
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
            // DIAMOND: Chain Shot — 3 piercing bolts, 8 + 4 chain damage
            player.playSound(player.getLocation(), Sound.ITEM_CROSSBOW_SHOOT, 1.5f, 1.0f);

            Vector baseDir = player.getLocation().getDirection().normalize();
            for (int bolt = 0; bolt < 3; bolt++) {
                final int boltIndex = bolt;
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    // Slightly spread direction
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

                    // Chain damage: when bolt hits, nearby enemies take 4 extra dmg
                    new BukkitRunnable() {
                        int t = 0;
                        boolean chained = false;
                        @Override
                        public void run() {
                            if (t >= 40 || arrow.isDead() || chained) {
                                if ((arrow.isDead() || arrow.isOnGround()) && !chained) {
                                    chained = true;
                                    Location impactLoc = arrow.getLocation();
                                    for (LivingEntity enemy : getNearbyEnemies(impactLoc, 3.0, player)) {
                                        dealAbilityDamage(player, enemy, 4.0, false);
                                        enemy.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, enemy.getLocation().add(0, 1, 0), 8, 0.3, 0.3, 0.3, 0.1);
                                    }
                                }
                                cancel();
                                return;
                            }
                            player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, arrow.getLocation(), 2, 0.05, 0.05, 0.05, 0.01);
                            t++;
                        }
                    }.runTaskTimer(plugin, 0L, 1L);
                }, bolt * 4L);
            }

            player.sendActionBar("\u00a7b\u26a1 Chain Shot!");

        } else {
            // NETHERITE DEFINITIVE: Thunder Barrage — 6 explosive bolts, 12 + 14 AoE
            player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.5f, 0.8f);
            player.playSound(player.getLocation(), Sound.ITEM_CROSSBOW_SHOOT, 2.0f, 0.5f);

            Vector baseDir = player.getLocation().getDirection().normalize();

            for (int bolt = 0; bolt < 6; bolt++) {
                final int boltIndex = bolt;
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    // Fan pattern
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

                    // Explosive bolt tracker
                    new BukkitRunnable() {
                        int t = 0;
                        boolean exploded = false;
                        @Override
                        public void run() {
                            if (t >= 60 || arrow.isDead() || exploded) {
                                if (!exploded) {
                                    exploded = true;
                                    explodeBolt(arrow.getLocation());
                                }
                                cancel();
                                return;
                            }
                            if (arrow.isOnGround()) {
                                exploded = true;
                                explodeBolt(arrow.getLocation());
                                cancel();
                                return;
                            }
                            // Thunder trail
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
                                kb.setY(0.5);
                                enemy.setVelocity(kb);
                                enemy.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 2));
                            }
                            arrow.remove();
                        }
                    }.runTaskTimer(plugin, 0L, 1L);
                }, bolt * 3L);
            }

            player.sendActionBar("\u00a7e\u00a7l\u26a1 THUNDER BARRAGE \u26a1");
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
            // DIAMOND: Tidal Surge — 8 dmg + water knockback + Slowness I
            player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_RIPTIDE_3, 1.5f, 1.0f);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_SPLASH_HIGH_SPEED, 1.0f, 0.8f);

            Location center = player.getLocation();
            Vector forward = center.getDirection().normalize();

            // Water burst cone in front of player
            for (LivingEntity enemy : getNearbyEnemies(center, 8.0, player)) {
                Vector toEnemy = enemy.getLocation().toVector().subtract(center.toVector()).normalize();
                double dot = forward.dot(toEnemy);
                if (dot > 0.3) { // ~70 degree cone
                    dealAbilityDamage(player, enemy, 8.0, false);
                    Vector knockback = toEnemy.multiply(1.5);
                    knockback.setY(0.5);
                    enemy.setVelocity(knockback);
                    enemy.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 0));
                    enemy.getWorld().spawnParticle(Particle.SPLASH, enemy.getLocation().add(0, 1, 0), 15, 0.3, 0.3, 0.3, 0.1);
                }
            }

            // Water splash particles
            for (int i = 0; i < 30; i++) {
                double dist = 1 + Math.random() * 7;
                double spread = (Math.random() - 0.5) * 1.2;
                Vector offset = forward.clone().multiply(dist).add(new Vector(spread, Math.random() * 1.5, spread));
                Location pLoc = center.clone().add(offset);
                player.getWorld().spawnParticle(Particle.SPLASH, pLoc, 3, 0.2, 0.3, 0.2, 0.05);
                player.getWorld().spawnParticle(Particle.DRIPPING_WATER, pLoc, 2, 0.1, 0.2, 0.1, 0);
            }

            player.sendActionBar("\u00a7b\u2660 Tidal Surge!");

        } else {
            // NETHERITE DEFINITIVE: Poseidon's Wrath — 3 water waves (16/13/10) + lightning strike
            player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_THUNDER, 2.0f, 0.5f);
            player.playSound(player.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 0.5f, 1.5f);

            Location center = player.getLocation();
            Vector forward = center.getDirection().normalize();

            // 3 expanding water waves
            for (int wave = 0; wave < 3; wave++) {
                final int waveNum = wave;
                final double waveDamage = 16.0 - (wave * 3.0); // 16, 13, 10
                final double waveRadius = 4.0 + (wave * 3.0); // 4, 7, 10

                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    Location waveCenter = center.clone().add(forward.clone().multiply(waveNum * 3));

                    // Water ring particles
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
                        kb.setY(0.6);
                        enemy.setVelocity(kb);
                        enemy.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 1));
                    }
                }, wave * 10L);
            }

            // Lightning strike at target location after all waves
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

            player.sendActionBar("\u00a71\u00a7l\u2660 POSEIDON'S WRATH \u2660");
        }
    }

    // ================================================================
    // MACE — "Ground Slam" (Diamond) / "Meteor Strike" (Netherite)
    // *** FIXED v1.3.0: Invulnerability before impact instead of instant kill ***
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
            // DIAMOND: Ground Slam — 10 + stun (Slowness III + Mining Fatigue)
            player.playSound(player.getLocation(), Sound.ITEM_MACE_SMASH_GROUND, 2.0f, 1.0f);

            Location center = player.getLocation();
            for (LivingEntity enemy : getNearbyEnemies(center, 6.0, player)) {
                dealAbilityDamage(player, enemy, 10.0, false);
                enemy.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 2));
                enemy.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 60, 1));
            }

            // Ground crack particles
            for (int i = 0; i < 60; i++) {
                double angle = Math.random() * 2 * Math.PI;
                double r = Math.random() * 5;
                Location pLoc = center.clone().add(Math.cos(angle) * r, 0.1, Math.sin(angle) * r);
                player.getWorld().spawnParticle(Particle.BLOCK, pLoc, 3, 0.2, 0.3, 0.2, 0.1,
                        Material.STONE.createBlockData());
                player.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, pLoc, 1, 0, 0.3, 0, 0.02);
            }
            player.getWorld().spawnParticle(Particle.EXPLOSION, center, 3, 1, 0.5, 1, 0);

            player.sendActionBar("\u00a76\u2b50 Ground Slam!");

        } else {
            // ==================================================================
            // NETHERITE DEFINITIVE: METEOR STRIKE — FIXED v1.3.0
            // Player is launched high, granted Resistance 255 (full immunity)
            // and Slow Falling, then slammed down with massive AoE.
            // Invulnerability is removed 1 second AFTER landing, not before.
            // Damage: 15-35 based on fall distance, 12-block AoE.
            // ==================================================================

            player.playSound(player.getLocation(), Sound.ITEM_MACE_SMASH_GROUND_HEAVY, 2.0f, 0.5f);
            player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1.0f, 1.5f);

            // Phase 1: Grant FULL INVULNERABILITY + Slow Falling BEFORE launch
            // Resistance 255 = 100% damage reduction (immune to fall damage and all damage)
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 200, 255, false, false, true));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 60, 0, false, false, false));

            // Phase 2: Launch player high into the air
            player.setVelocity(new Vector(0, 3.5, 0));

            // Flame trail during ascent
            new BukkitRunnable() {
                int ascendTick = 0;
                @Override
                public void run() {
                    if (ascendTick >= 20 || !player.isOnline()) { cancel(); return; }
                    Location loc = player.getLocation();
                    player.getWorld().spawnParticle(Particle.FLAME, loc, 10, 0.3, 0.5, 0.3, 0.05);
                    player.getWorld().spawnParticle(Particle.LAVA, loc, 3, 0.2, 0.2, 0.2, 0);
                    player.getWorld().spawnParticle(Particle.DUST, loc, 8, 0.4, 0.6, 0.4, 0,
                            new Particle.DustOptions(Color.fromRGB(255, 100, 0), 2.0f));
                    ascendTick++;
                }
            }.runTaskTimer(plugin, 0L, 1L);

            // Phase 3: After ascent, remove Slow Falling and slam down
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.removePotionEffect(PotionEffectType.SLOW_FALLING);
                // Slam downward
                player.setVelocity(new Vector(0, -4.0, 0));

                player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.7f, 2.0f);

                // Meteor trail during descent
                new BukkitRunnable() {
                    int descendTick = 0;
                    Location launchLoc = player.getLocation().clone();
                    boolean landed = false;

                    @Override
                    public void run() {
                        if (descendTick >= 60 || landed || !player.isOnline()) {
                            if (!landed) performImpact(player.getLocation());
                            cancel();
                            return;
                        }

                        Location loc = player.getLocation();

                        // Fire + meteor trail
                        player.getWorld().spawnParticle(Particle.FLAME, loc, 15, 0.4, 0.8, 0.4, 0.08);
                        player.getWorld().spawnParticle(Particle.LAVA, loc, 5, 0.3, 0.3, 0.3, 0);
                        player.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, loc, 8, 0.3, 1.0, 0.3, 0.05);
                        player.getWorld().spawnParticle(Particle.DUST, loc, 10, 0.5, 0.8, 0.5, 0,
                                new Particle.DustOptions(Color.fromRGB(255, 60, 0), 2.5f));

                        // Check if player has landed (on ground or close to it)
                        if (player.isOnGround() || (descendTick > 3 && player.getVelocity().getY() > -0.1)) {
                            landed = true;
                            performImpact(loc);
                            cancel();
                            return;
                        }

                        descendTick++;
                    }

                    private void performImpact(Location impactLoc) {
                        World world = impactLoc.getWorld();

                        // Calculate damage based on fall distance
                        double fallDist = Math.max(0, launchLoc.getY() - impactLoc.getY());
                        double baseDamage = Math.min(35.0, 15.0 + fallDist * 0.5);

                        // Massive impact sounds
                        world.playSound(impactLoc, Sound.ITEM_MACE_SMASH_GROUND_HEAVY, 2.0f, 0.5f);
                        world.playSound(impactLoc, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.6f);
                        world.playSound(impactLoc, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 2.0f, 0.7f);
                        world.playSound(impactLoc, Sound.ENTITY_WARDEN_SONIC_BOOM, 1.5f, 0.8f);

                        // Massive impact particles
                        world.spawnParticle(Particle.EXPLOSION_EMITTER, impactLoc, 5, 1, 0.5, 1, 0);
                        world.spawnParticle(Particle.LAVA, impactLoc, 80, 5, 1, 5, 0.3);
                        world.spawnParticle(Particle.FLAME, impactLoc, 100, 6, 2, 6, 0.15);
                        world.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, impactLoc, 50, 4, 3, 4, 0.1);
                        world.spawnParticle(Particle.TOTEM_OF_UNDYING, impactLoc, 60, 3, 2, 3, 0.3);

                        // Expanding shockwave rings
                        new BukkitRunnable() {
                            int ringTick = 0;
                            @Override
                            public void run() {
                                if (ringTick >= 15) { cancel(); return; }
                                double ringR = ringTick * 0.8;
                                for (int i = 0; i < 30; i++) {
                                    double angle = (2 * Math.PI / 30) * i;
                                    Location ringLoc = impactLoc.clone().add(Math.cos(angle) * ringR, 0.2, Math.sin(angle) * ringR);
                                    world.spawnParticle(Particle.FLAME, ringLoc, 1, 0, 0.1, 0, 0.01);
                                    world.spawnParticle(Particle.BLOCK, ringLoc, 2, 0.1, 0.2, 0.1, 0.05,
                                            Material.STONE.createBlockData());
                                }
                                ringTick++;
                            }
                        }.runTaskTimer(plugin, 0L, 1L);

                        // Damage all enemies in 12-block radius
                        for (LivingEntity enemy : getNearbyEnemies(impactLoc, 12.0, player)) {
                            double dist = enemy.getLocation().distance(impactLoc);
                            double distanceFactor = 1.0 - (dist / 12.0) * 0.4; // 60-100% dmg based on distance
                            double finalDamage = baseDamage * distanceFactor;

                            dealAbilityDamage(player, enemy, finalDamage, true);

                            // Knockback + launch
                            Vector kb = enemy.getLocation().toVector().subtract(impactLoc.toVector()).normalize().multiply(2.0);
                            kb.setY(1.0);
                            enemy.setVelocity(kb);

                            // Stun effects
                            enemy.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 3));
                            enemy.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 80, 2));

                            // Fire
                            enemy.setFireTicks(60);

                            // Impact particle on each enemy
                            world.spawnParticle(Particle.EXPLOSION, enemy.getLocation().add(0, 1, 0), 2, 0.3, 0.3, 0.3, 0);
                        }

                        // Phase 4: Remove invulnerability 1 SECOND after landing (20 ticks)
                        // This gives the player a safe buffer after impact
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            player.removePotionEffect(PotionEffectType.RESISTANCE);
                        }, 20L);
                    }
                }.runTaskTimer(plugin, 0L, 1L);
            }, 25L); // 25 ticks = 1.25 seconds of ascent

            player.sendActionBar("\u00a76\u00a7l\u2604 METEOR STRIKE \u2604");
        }
    }
}

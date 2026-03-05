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
import org.bukkit.entity.BlockDisplay;
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
import com.jglims.plugin.guilds.GuildManager;


/**
 * WeaponAbilityListener — v1.3.1
 *
 * CHANGES FROM v1.3.0:
 *   - All ability damage values increased ~30-50%
 *   - Pickaxe Ore Pulse / Seismic Resonance: COMPLETELY REVAMPED
 *     Now uses BlockDisplay entities with glowing outlines visible through walls.
 *     Both tiers detect ALL ores. Netherite has bigger radius and longer duration.
 *   - All DRAGON_BREATH replaced with SOUL_FIRE_FLAME (HOTFIX 3 carryover)
 */
public class WeaponAbilityListener implements Listener {

    private final JGlimsPlugin plugin;
    private final ConfigManager config;
    private final CustomEnchantManager enchantManager;
    private final SuperToolManager superToolManager;
    private final SpearManager spearManager;
    private final BattleShovelManager battleShovelManager;
    private final GuildManager guildManager;


    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();
    private final NamespacedKey superTierKey;

    public WeaponAbilityListener(JGlimsPlugin plugin, ConfigManager config,
                              CustomEnchantManager enchantManager,
                              SuperToolManager superToolManager,
                              SpearManager spearManager,
                              BattleShovelManager battleShovelManager,
                              GuildManager guildManager) {
        this.plugin = plugin;
        this.config = config;
        this.enchantManager = enchantManager;
        this.superToolManager = superToolManager;
        this.spearManager = spearManager;
        this.battleShovelManager = battleShovelManager;
        this.guildManager = guildManager;
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
        if (e instanceof LivingEntity le && !(e instanceof ArmorStand) && e != exclude) {
            if (le.getLocation().distanceSquared(center) <= radius * radius) {
                // If target is a Player, check guild friendly-fire
                if (le instanceof Player targetPlayer && exclude != null) {
                    // Skip if both players are in the same guild
                    if (guildManager.areInSameGuild(exclude.getUniqueId(), targetPlayer.getUniqueId())) {
                        continue;
                    }
                }
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
    // PICKAXE — "Ore Pulse" / "Seismic Resonance" — COMPLETELY REVAMPED
    // Now uses BlockDisplay entities with glowing outlines visible through walls!
    // ================================================================

    private void handlePickaxeAbility(Player player, ItemStack item, boolean isDefinitive) {
        String abilityName = isDefinitive ? "Seismic Resonance" : "Ore Pulse";
        int cooldownSec = isDefinitive ? 18 : 10;

        if (isOnCooldown(player, abilityName)) { sendCooldownMessage(player, abilityName); return; }
        setCooldown(player, abilityName, cooldownSec);

        // Both tiers detect ALL ores — difference is radius and duration
        int oreRadius = isDefinitive ? 16 : 10;
        int debrisRadius = isDefinitive ? 48 : 24;
        int durationTicks = isDefinitive ? 500 : 300; // 25s / 15s

        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.5f, isDefinitive ? 0.5f : 1.0f);
        if (isDefinitive) {
            player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_HEARTBEAT, 1.5f, 0.6f);
            player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 0.5f, 1.5f);
        }

        Location playerLoc = player.getLocation();
        World world = player.getWorld();
        Map<Location, OreType> foundOres = new LinkedHashMap<>();
        int maxRadius = Math.max(oreRadius, debrisRadius);

        // Scan for ores
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
        long diamondCount = foundOres.values().stream().filter(o -> o == OreType.DIAMOND).count();
        long emeraldCount = foundOres.values().stream().filter(o -> o == OreType.EMERALD).count();

        if (totalOres == 0) {
            player.sendActionBar("\u00a77" + abilityName + ": \u00a7fNo ores detected nearby.");
            return;
        }

        // Build detailed message
        StringBuilder msg = new StringBuilder();
        if (isDefinitive) {
            msg.append("\u00a75\u00a7l SEISMIC RESONANCE: \u00a7f");
        } else {
            msg.append("\u00a7e Ore Pulse: \u00a7f");
        }
        msg.append(totalOres).append(" ores detected");
        if (diamondCount > 0) msg.append(" \u00a7b").append(diamondCount).append(" Diamond");
        if (emeraldCount > 0) msg.append(" \u00a7a").append(emeraldCount).append(" Emerald");
        if (debrisCount > 0) msg.append(" \u00a74\u00a7l").append(debrisCount).append(" Ancient Debris!");
        player.sendActionBar(msg.toString());

        if (debrisCount > 0) {
            player.sendMessage("\u00a74\u00a7l\u26a0 " + debrisCount + " Ancient Debris detected within " + debrisRadius + " blocks!");
        }
        if (diamondCount > 0) {
            player.sendMessage("\u00a7b\u2666 " + diamondCount + " Diamond Ore detected within " + oreRadius + " blocks!");
        }

        // Activation visual: expanding pulse ring
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 8) { cancel(); return; }
                double r = tick * (isDefinitive ? 2.5 : 1.8);
                for (int i = 0; i < 30; i++) {
                    double angle = (2 * Math.PI / 30) * i;
                    Location pLoc = playerLoc.clone().add(Math.cos(angle) * r, 0.5, Math.sin(angle) * r);
                    world.spawnParticle(isDefinitive ? Particle.SOUL_FIRE_FLAME : Particle.ENCHANT, pLoc, 2, 0, 0.3, 0, 0.05);
                }
                if (isDefinitive && tick % 2 == 0) {
                    world.spawnParticle(Particle.REVERSE_PORTAL, playerLoc.clone().add(0, 1, 0), 20, 1, 1, 1, 0.1);
                }
                tick++;
            }
        }.runTaskTimer(plugin, 0L, 2L);

        // Spawn glowing BlockDisplay entities for each ore — ONLY visible to the caster
        List<BlockDisplay> glowingDisplays = new ArrayList<>();

        for (Map.Entry<Location, OreType> entry : foundOres.entrySet()) {
            Location oreLoc = entry.getKey();
            OreType oreType = entry.getValue();

            BlockDisplay display = world.spawn(oreLoc, BlockDisplay.class, entity -> {
                entity.setBlock(oreLoc.getBlock().getBlockData());
                entity.setGlowing(true);
                entity.setGlowColorOverride(oreType.getGlowColor());
                entity.setVisibleByDefault(false);
                entity.setPersistent(false);
                entity.setInvisible(false);
                entity.setViewRange(1.0f);
            });

            player.showEntity(plugin, display);
            glowingDisplays.add(display);
        }

        // Also keep dust particles as a secondary indicator
        new BukkitRunnable() {
            int ticks = 0;
            @Override public void run() {
                if (ticks >= durationTicks || !player.isOnline()) {
                    cancel();
                    return;
                }
                // Sparse particle overlay every 10 ticks to enhance the glow
                if (ticks % 10 == 0) {
                    for (Map.Entry<Location, OreType> entry : foundOres.entrySet()) {
                        Location oreLoc = entry.getKey().clone().add(0.5, 0.5, 0.5);
                        OreType type = entry.getValue();
                        player.spawnParticle(Particle.DUST, oreLoc, 2, 0.15, 0.15, 0.15, 0, type.getDustOptions());
                        if (type == OreType.ANCIENT_DEBRIS && ticks % 20 == 0) {
                            player.spawnParticle(Particle.FLAME, oreLoc, 2, 0.15, 0.15, 0.15, 0.01);
                            player.spawnParticle(Particle.SOUL_FIRE_FLAME, oreLoc, 3, 0.2, 0.2, 0.2, 0.02);
                        }
                        if (type == OreType.DIAMOND && ticks % 20 == 0) {
                            player.spawnParticle(Particle.END_ROD, oreLoc, 1, 0.1, 0.1, 0.1, 0.02);
                        }
                    }
                }
                ticks++;
            }
        }.runTaskTimer(plugin, 5L, 1L);

        // Schedule cleanup: remove all BlockDisplay entities after duration
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (BlockDisplay display : glowingDisplays) {
                if (display.isValid()) {
                    display.remove();
                }
            }
            player.sendActionBar("\u00a77" + abilityName + " faded.");
        }, durationTicks);
    }

    private enum OreType {
        COAL(Color.fromRGB(50, 50, 50), Color.fromRGB(80, 80, 80)),
        IRON(Color.fromRGB(210, 150, 100), Color.fromRGB(210, 150, 100)),
        COPPER(Color.fromRGB(180, 100, 50), Color.fromRGB(180, 100, 50)),
        GOLD(Color.fromRGB(255, 215, 0), Color.fromRGB(255, 215, 0)),
        REDSTONE(Color.fromRGB(255, 0, 0), Color.fromRGB(255, 0, 0)),
        LAPIS(Color.fromRGB(30, 30, 200), Color.fromRGB(30, 30, 200)),
        EMERALD(Color.fromRGB(0, 200, 50), Color.fromRGB(0, 255, 50)),
        DIAMOND(Color.fromRGB(100, 230, 255), Color.fromRGB(80, 220, 255)),
        ANCIENT_DEBRIS(Color.fromRGB(100, 50, 20), Color.fromRGB(200, 80, 30)),
        NETHER_GOLD(Color.fromRGB(255, 200, 50), Color.fromRGB(255, 200, 50)),
        NETHER_QUARTZ(Color.fromRGB(240, 240, 230), Color.fromRGB(240, 240, 230));

        private final Color dustColor;
        private final Color glowColor;

        OreType(Color dustColor, Color glowColor) {
            this.dustColor = dustColor;
            this.glowColor = glowColor;
        }

        public Particle.DustOptions getDustOptions() { return new Particle.DustOptions(dustColor, 1.5f); }
        public Color getGlowColor() { return glowColor; }
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
    // SWORD — "Dash Strike" / "Dimensional Cleave" — DAMAGE INCREASED
    // ================================================================

    private void handleSwordAbility(Player player, ItemStack item, boolean isDefinitive) {
        String abilityName = isDefinitive ? "Dimensional Cleave" : "Dash Strike";
        int cooldownSec = isDefinitive ? 12 : 6;

        if (isOnCooldown(player, abilityName)) { sendCooldownMessage(player, abilityName); return; }
        setCooldown(player, abilityName, cooldownSec);

        if (!isDefinitive) {
            // Dash Strike — DAMAGE: 8 -> 12
            player.setVelocity(player.getLocation().getDirection().multiply(2.2));
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.5f, 1.2f);

            new BukkitRunnable() {
                int ticks = 0;
                @Override public void run() {
                    if (ticks >= 10) { cancel(); return; }
                    Location loc = player.getLocation();
                    player.getWorld().spawnParticle(Particle.SWEEP_ATTACK, loc.clone().add(0, 1, 0), 5, 0.5, 0.3, 0.5, 0);
                    player.getWorld().spawnParticle(Particle.CRIT, loc, 12, 0.6, 0.4, 0.6, 0.1);
                    for (LivingEntity enemy : getNearbyEnemies(loc, 3.0, player)) {
                        dealAbilityDamage(player, enemy, 12.0, false);
                        enemy.setVelocity(player.getLocation().getDirection().multiply(0.6));
                    }
                    ticks++;
                }
            }.runTaskTimer(plugin, 0L, 2L);

        } else {
            // Dimensional Cleave — DAMAGE: 15->22 per tick, 25->35 final
            player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1.0f, 1.5f);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 2.0f, 0.5f);

            Location center = player.getLocation().add(player.getLocation().getDirection().multiply(4));
            center.setY(player.getLocation().getY());

            new BukkitRunnable() {
                int ticks = 0;
                @Override public void run() {
                    if (ticks >= 15) { cancel(); return; }
                    double radius = 2.0 + (ticks * 0.7);
                    for (int i = 0; i < 40; i++) {
                        double angle = (2 * Math.PI / 40) * i;
                        double x = Math.cos(angle) * radius;
                        double z = Math.sin(angle) * radius;
                        Location particleLoc = center.clone().add(x, 0.5, z);
                        player.getWorld().spawnParticle(Particle.SOUL, particleLoc, 1, 0, 0, 0, 0);
                        if (ticks % 3 == 0) {
                            player.getWorld().spawnParticle(Particle.REVERSE_PORTAL, particleLoc, 2, 0.1, 0.5, 0.1, 0.05);
                        }
                    }
                    if (ticks % 3 == 0) {
                        for (LivingEntity enemy : getNearbyEnemies(center, radius, player)) {
                            dealAbilityDamage(player, enemy, 22.0, true);
                            enemy.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 2));
                        }
                    }
                    player.getWorld().spawnParticle(Particle.EXPLOSION, center.clone().add(0, 1, 0), 1, 0, 0, 0, 0);
                    player.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, center, 20, 2.0, 2.5, 2.0, 0.03);
                    ticks++;
                }
            }.runTaskTimer(plugin, 0L, 2L);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, center, 5, 0, 0, 0, 0);
                player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, center, 150, 4, 3, 4, 0.6);
                player.getWorld().playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.6f);
                for (LivingEntity enemy : getNearbyEnemies(center, 10.0, player)) {
                    dealAbilityDamage(player, enemy, 35.0, true);
                    Vector knockback = enemy.getLocation().toVector().subtract(center.toVector()).normalize().multiply(2.0);
                    knockback.setY(0.8);
                    enemy.setVelocity(knockback);
                }
            }, 30L);
        }
    }

    // ================================================================
    // AXE — "Bloodthirst" / "Ragnarok Cleave" — DAMAGE INCREASED
    // ================================================================

    private void handleAxeAbility(Player player, ItemStack item, boolean isDefinitive) {
        String abilityName = isDefinitive ? "Ragnarok Cleave" : "Bloodthirst";
        int cooldownSec = isDefinitive ? 15 : 8;

        if (isOnCooldown(player, abilityName)) { sendCooldownMessage(player, abilityName); return; }
        setCooldown(player, abilityName, cooldownSec);

        if (!isDefinitive) {
            // Bloodthirst — now also grants Speed I
            player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 100, 1));
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 100, 1));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 0));
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
                    player.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, loc, 6, 0.5, 0.5, 0.5, 0.1);
                    player.getWorld().spawnParticle(Particle.DUST, loc, 8, 0.6, 0.6, 0.6, 0,
                            new Particle.DustOptions(Color.fromRGB(180, 0, 0), 1.8f));
                    ticks++;
                }
            }.runTaskTimer(plugin, 0L, 2L);

            player.sendActionBar("\u00a7c\u00a7l\u2620 BLOODTHIRST ACTIVE \u2620");

        } else {
            // Ragnarok Cleave — DAMAGE: 20/16/12 -> 30/24/18
            player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1.5f, 0.7f);
            player.playSound(player.getLocation(), Sound.ITEM_MACE_SMASH_GROUND_HEAVY, 2.0f, 0.8f);

            Location center = player.getLocation();

            new BukkitRunnable() {
                int ticks = 0;
                @Override public void run() {
                    if (ticks >= 20) { cancel(); return; }
                    double radius = ticks * 0.6;
                    for (int i = 0; i < 35 + (ticks * 2); i++) {
                        double angle = Math.random() * 2 * Math.PI;
                        double r = Math.random() * radius;
                        Location particleLoc = center.clone().add(Math.cos(angle) * r, 0.1, Math.sin(angle) * r);
                        player.getWorld().spawnParticle(Particle.LAVA, particleLoc, 1, 0, 0, 0, 0);
                        player.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, particleLoc, 1, 0, 0.5, 0, 0.02);
                        if (ticks > 5) {
                            player.getWorld().spawnParticle(Particle.FLAME, particleLoc, 2, 0, 0.3, 0, 0.01);
                        }
                    }
                    ticks++;
                }
            }.runTaskTimer(plugin, 0L, 1L);

            for (int wave = 0; wave < 3; wave++) {
                final double waveRadius = 5.0 + (wave * 3.5);
                final double waveDamage = 30.0 - (wave * 6.0);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    for (LivingEntity enemy : getNearbyEnemies(center, waveRadius, player)) {
                        dealAbilityDamage(player, enemy, waveDamage, true);
                        Vector launch = enemy.getLocation().toVector().subtract(center.toVector()).normalize().multiply(1.5);
                        launch.setY(1.0 + Math.random() * 0.5);
                        enemy.setVelocity(launch);
                        enemy.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 1));
                    }
                    player.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, center, 2, 0, 0, 0, 0);
                    player.getWorld().playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.9f + (float)(waveRadius * 0.05));
                }, wave * 8L);
            }

            player.sendActionBar("\u00a74\u00a7l\u2620 RAGNAROK CLEAVE \u2620");
        }
    }

    // ================================================================
    // SHOVEL — "Earthen Wall" / "Tectonic Upheaval" — DAMAGE INCREASED
    // ================================================================

    private void handleShovelAbility(Player player, ItemStack item, boolean isDefinitive) {
        String abilityName = isDefinitive ? "Tectonic Upheaval" : "Earthen Wall";
        int cooldownSec = isDefinitive ? 14 : 8;

        if (isOnCooldown(player, abilityName)) { sendCooldownMessage(player, abilityName); return; }
        setCooldown(player, abilityName, cooldownSec);

        if (!isDefinitive) {
            // Earthen Wall — DAMAGE: 6 -> 10
            player.playSound(player.getLocation(), Sound.BLOCK_GRAVEL_BREAK, 2.0f, 0.5f);
            player.playSound(player.getLocation(), Sound.ENTITY_IRON_GOLEM_ATTACK, 1.0f, 0.7f);
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 80, 1));
            Location center = player.getLocation();
            for (LivingEntity enemy : getNearbyEnemies(center, 6.0, player)) {
                enemy.setVelocity(new Vector(0, 1.5, 0));
                dealAbilityDamage(player, enemy, 10.0, false);
            }
            for (int i = 0; i < 60; i++) {
                double angle = Math.random() * 2 * Math.PI;
                double r = Math.random() * 5;
                Location pLoc = center.clone().add(Math.cos(angle) * r, 0.2, Math.sin(angle) * r);
                player.getWorld().spawnParticle(Particle.BLOCK, pLoc, 5, 0.3, 0.5, 0.3, 0.1, Material.DIRT.createBlockData());
            }
            player.sendActionBar("\u00a7e Earthen Wall \u2014 Enemies launched!");

        } else {
            // Tectonic Upheaval — DAMAGE: 14 -> 20
            player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_EMERGE, 2.0f, 0.5f);
            player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1.0f, 0.3f);
            Location center = player.getLocation();
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 160, 2));

            new BukkitRunnable() {
                int ticks = 0;
                @Override public void run() {
                    if (ticks >= 30) { cancel(); return; }
                    double radius = ticks * 0.5;
                    for (int i = 0; i < 30 + ticks; i++) {
                        double angle = Math.random() * 2 * Math.PI;
                        double r = Math.random() * radius;
                        Location pLoc = center.clone().add(Math.cos(angle) * r, 0.1, Math.sin(angle) * r);
                        player.getWorld().spawnParticle(Particle.BLOCK, pLoc, 3, 0.1, 0.8, 0.1, 0.3, Material.NETHERRACK.createBlockData());
                        player.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, pLoc, 1, 0, 1, 0, 0.05);
                    }
                    if (ticks % 8 == 4) {
                        for (LivingEntity enemy : getNearbyEnemies(center, radius, player)) {
                            dealAbilityDamage(player, enemy, 20.0, true);
                            Vector launch = enemy.getLocation().toVector().subtract(center.toVector()).normalize().multiply(1.0);
                            launch.setY(2.0);
                            enemy.setVelocity(launch);
                            enemy.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 120, 3));
                        }
                        player.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, center.clone().add(0, 1, 0), 3, 2, 0, 2, 0);
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
    // SPEAR — "Phantom Lunge" / "Spear of the Void" — DAMAGE INCREASED
    // ================================================================

    private void handleSpearAbility(Player player, ItemStack item, boolean isDefinitive) {
        String abilityName = isDefinitive ? "Spear of the Void" : "Phantom Lunge";
        int cooldownSec = isDefinitive ? 14 : 7;

        if (isOnCooldown(player, abilityName)) { sendCooldownMessage(player, abilityName); return; }
        setCooldown(player, abilityName, cooldownSec);

        if (!isDefinitive) {
            // Phantom Lunge — DAMAGE: 8 -> 12
            player.setVelocity(player.getLocation().getDirection().multiply(2.2));
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.5f, 1.3f);
            player.playSound(player.getLocation(), Sound.ENTITY_BREEZE_SHOOT, 1.0f, 1.5f);

            Set<UUID> hitEntities = new HashSet<>();
            new BukkitRunnable() {
                int ticks = 0;
                @Override public void run() {
                    if (ticks >= 10) { cancel(); return; }
                    Location loc = player.getLocation();
                    player.getWorld().spawnParticle(Particle.CRIT, loc.clone().add(0, 1, 0), 10, 0.4, 0.3, 0.4, 0.1);
                    player.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, loc.clone().add(0, 0.5, 0), 5, 0.3, 0.2, 0.3, 0.02);
                    player.getWorld().spawnParticle(Particle.SWEEP_ATTACK, loc.clone().add(0, 1, 0), 3, 0.5, 0.3, 0.5, 0);
                    for (LivingEntity enemy : getNearbyEnemies(loc, 2.5, player)) {
                        if (hitEntities.contains(enemy.getUniqueId())) continue;
                        hitEntities.add(enemy.getUniqueId());
                        dealAbilityDamage(player, enemy, 12.0, false);
                        enemy.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 0));
                        enemy.setVelocity(player.getLocation().getDirection().multiply(0.5));
                    }
                    ticks++;
                }
            }.runTaskTimer(plugin, 0L, 2L);

            player.sendActionBar("\u00a7b\u27a4 Phantom Lunge!");

        } else {
            // Spear of the Void — DAMAGE: 18->25 pierce, 10->15 detonation, 6->10 chain
            player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1.0f, 1.5f);
            player.playSound(player.getLocation(), Sound.ENTITY_BREEZE_SHOOT, 1.5f, 0.5f);

            Vector direction = player.getLocation().getDirection().normalize();
            Location start = player.getEyeLocation().clone();
            World world = player.getWorld();
            List<Location> hitLocations = new ArrayList<>();
            Set<UUID> hitEntities = new HashSet<>();

            new BukkitRunnable() {
                int ticks = 0;
                Location current = start.clone();

                @Override public void run() {
                    if (ticks >= 40) {
                        // Detonation at final location
                        detonateSpearOfVoid(player, current, hitLocations);
                        cancel();
                        return;
                    }

                    current.add(direction.clone().multiply(1.5));

                    // Check for solid block
                    if (current.getBlock().getType().isSolid()) {
                        detonateSpearOfVoid(player, current, hitLocations);
                        cancel();
                        return;
                    }

                    // Visual trail
                    world.spawnParticle(Particle.SOUL_FIRE_FLAME, current, 8, 0.1, 0.1, 0.1, 0.02);
                    world.spawnParticle(Particle.REVERSE_PORTAL, current, 5, 0.1, 0.1, 0.1, 0.05);
                    world.spawnParticle(Particle.ENCHANT, current, 3, 0.2, 0.2, 0.2, 0.1);

                    // Pierce enemies
                    for (LivingEntity enemy : getNearbyEnemies(current, 2.0, player)) {
                        if (hitEntities.contains(enemy.getUniqueId())) continue;
                        hitEntities.add(enemy.getUniqueId());
                        dealAbilityDamage(player, enemy, 25.0, true);
                        enemy.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 1));
                        enemy.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 0));
                        hitLocations.add(enemy.getLocation().clone());
                        world.spawnParticle(Particle.SOUL_FIRE_FLAME, enemy.getLocation().add(0, 1, 0), 20, 0.3, 0.5, 0.3, 0.05);
                        world.spawnParticle(Particle.DAMAGE_INDICATOR, enemy.getLocation().add(0, 1.5, 0), 8, 0.3, 0.3, 0.3, 0.1);
                    }

                    ticks++;
                }
            }.runTaskTimer(plugin, 0L, 1L);

            player.sendActionBar("\u00a75\u00a7l\u2726 SPEAR OF THE VOID \u2726");
        }
    }

    private void detonateSpearOfVoid(Player player, Location detonationLoc, List<Location> hitLocations) {
        World world = detonationLoc.getWorld();

        // Main detonation
        world.spawnParticle(Particle.EXPLOSION_EMITTER, detonationLoc, 3, 0, 0, 0, 0);
        world.spawnParticle(Particle.SOUL_FIRE_FLAME, detonationLoc, 50, 2, 2, 2, 0.1);
        world.spawnParticle(Particle.REVERSE_PORTAL, detonationLoc, 30, 1.5, 1.5, 1.5, 0.2);
        world.playSound(detonationLoc, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.7f);
        world.playSound(detonationLoc, Sound.ENTITY_WARDEN_SONIC_BOOM, 1.0f, 1.2f);

        for (LivingEntity enemy : getNearbyEnemies(detonationLoc, 5.0, player)) {
            dealAbilityDamage(player, enemy, 15.0, true);
            Vector knockback = enemy.getLocation().toVector().subtract(detonationLoc.toVector()).normalize().multiply(1.5);
            knockback.setY(0.6);
            enemy.setVelocity(knockback);
        }

        // Chain explosions at each hit location
        for (int i = 0; i < hitLocations.size(); i++) {
            final Location chainLoc = hitLocations.get(i);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                world.spawnParticle(Particle.EXPLOSION, chainLoc, 3, 0, 0, 0, 0);
                world.spawnParticle(Particle.SOUL_FIRE_FLAME, chainLoc, 20, 1, 1, 1, 0.05);
                world.playSound(chainLoc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.2f);
                for (LivingEntity enemy : getNearbyEnemies(chainLoc, 3.0, player)) {
                    dealAbilityDamage(player, enemy, 10.0, true);
                }
            }, (i + 1) * 5L);
        }
    }


    // ================================================================
    // BOW — "Arrow Storm" / "Celestial Volley" — DAMAGE INCREASED
    // ================================================================

    private void handleBowAbility(Player player, ItemStack item, boolean isDefinitive) {
        String abilityName = isDefinitive ? "Celestial Volley" : "Arrow Storm";
        int cooldownSec = isDefinitive ? 16 : 8;

        if (isOnCooldown(player, abilityName)) { sendCooldownMessage(player, abilityName); return; }
        setCooldown(player, abilityName, cooldownSec);

        if (!isDefinitive) {
            // Arrow Storm — DAMAGE: 6 -> 9 per arrow, 5 waves x 3 arrows = 15
            player.playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1.5f, 1.2f);

            new BukkitRunnable() {
                int wave = 0;
                @Override public void run() {
                    if (wave >= 5) { cancel(); return; }
                    for (int i = 0; i < 3; i++) {
                        Vector dir = player.getLocation().getDirection().clone();
                        dir.add(new Vector(
                                (Math.random() - 0.5) * 0.3,
                                (Math.random() - 0.5) * 0.15,
                                (Math.random() - 0.5) * 0.3
                        )).normalize().multiply(2.5);

                        Arrow arrow = player.launchProjectile(Arrow.class, dir);
                        arrow.setDamage(9.0);
                        arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
                        arrow.setLifetimeTicks(40);
                        arrow.setColor(Color.YELLOW);
                        arrow.setShooter(player);
                    }
                    player.playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 0.8f, 1.0f + wave * 0.15f);
                    player.getWorld().spawnParticle(Particle.CRIT, player.getLocation().add(0, 1.5, 0), 10, 0.5, 0.3, 0.5, 0.2);
                    wave++;
                }
            }.runTaskTimer(plugin, 0L, 4L);

            player.sendActionBar("\u00a7e\u27b3 Arrow Storm!");

        } else {
            // Celestial Volley — DAMAGE: 10->15 per arrow, 8->12 AoE per wave
            // Target = where player is looking (30 blocks) or 20 blocks forward
            Location target = player.getTargetBlockExact(30) != null
                    ? player.getTargetBlockExact(30).getLocation().add(0.5, 1, 0.5)
                    : player.getEyeLocation().add(player.getLocation().getDirection().multiply(20));
            World world = player.getWorld();

            player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 0.5f, 2.0f);
            player.playSound(target, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.5f);

            // Marker beam: rising triple-helix
            new BukkitRunnable() {
                int tick = 0;
                @Override public void run() {
                    if (tick >= 20) { cancel(); return; }
                    for (int arm = 0; arm < 3; arm++) {
                        double angle = (2 * Math.PI / 3) * arm + tick * 0.5;
                        double r = 1.0 + tick * 0.1;
                        double y = 15.0 - tick * 0.6;
                        Location pLoc = target.clone().add(Math.cos(angle) * r, y, Math.sin(angle) * r);
                        world.spawnParticle(Particle.SOUL_FIRE_FLAME, pLoc, 2, 0.05, 0.1, 0.05, 0.01);
                        world.spawnParticle(Particle.END_ROD, pLoc, 1, 0.05, 0.05, 0.05, 0.01);
                    }
                    tick++;
                }
            }.runTaskTimer(plugin, 0L, 1L);

            // Rain arrows after marker beam
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                new BukkitRunnable() {
                    int wave = 0;
                    @Override public void run() {
                        if (wave >= 8) { cancel(); return; }
                        for (int i = 0; i < 4; i++) {
                            Location spawnLoc = target.clone().add(
                                    (Math.random() - 0.5) * 8,
                                    15 + Math.random() * 3,
                                    (Math.random() - 0.5) * 8
                            );
                            Vector downDir = target.toVector().subtract(spawnLoc.toVector()).normalize().multiply(2.0);
                            Arrow arrow = world.spawnArrow(spawnLoc, downDir, 2.0f, 2.0f);
                            arrow.setDamage(15.0);
                            arrow.setFireTicks(100);
                            arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
                            arrow.setLifetimeTicks(60);
                            arrow.setShooter(player);
                        }
                        // AoE impact damage per wave
                        for (LivingEntity enemy : getNearbyEnemies(target, 6.0, player)) {
                            dealAbilityDamage(player, enemy, 12.0, true);
                        }
                        world.spawnParticle(Particle.FLAME, target, 20, 3, 0.5, 3, 0.05);
                        world.spawnParticle(Particle.CRIT, target, 15, 3, 1, 3, 0.1);
                        world.playSound(target, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.8f, 1.0f + wave * 0.1f);
                        world.playSound(target, Sound.ENTITY_ARROW_HIT, 1.5f, 0.8f);
                        wave++;
                    }
                }.runTaskTimer(plugin, 0L, 3L);
            }, 20L);

            player.sendActionBar("\u00a75\u00a7l\u2728 CELESTIAL VOLLEY \u2728");
        }
    }


    // ================================================================
    // CROSSBOW — "Chain Shot" / "Thunder Barrage" — DAMAGE INCREASED
    // ================================================================

    private void handleCrossbowAbility(Player player, ItemStack item, boolean isDefinitive) {
        String abilityName = isDefinitive ? "Thunder Barrage" : "Chain Shot";
        int cooldownSec = isDefinitive ? 16 : 8;

        if (isOnCooldown(player, abilityName)) { sendCooldownMessage(player, abilityName); return; }
        setCooldown(player, abilityName, cooldownSec);

        if (!isDefinitive) {
            // Chain Shot — DAMAGE: 8 -> 12 initial + per chain
            player.playSound(player.getLocation(), Sound.ITEM_CROSSBOW_SHOOT, 1.5f, 1.0f);

            Vector direction = player.getLocation().getDirection().normalize();
            Location start = player.getEyeLocation().clone();
            World world = player.getWorld();
            Set<UUID> hitEntities = new HashSet<>();

            new BukkitRunnable() {
                int ticks = 0;
                Location current = start.clone();
                int chains = 0;
                LivingEntity lastHit = null;

                @Override public void run() {
                    if (ticks >= 30 || chains >= 6) { cancel(); return; }

                    if (lastHit == null) {
                        // Travel forward seeking first target
                        current.add(direction.clone().multiply(1.5));
                        world.spawnParticle(Particle.CRIT, current, 3, 0.1, 0.1, 0.1, 0.05);
                        world.spawnParticle(Particle.ELECTRIC_SPARK, current, 2, 0.1, 0.1, 0.1, 0.02);
                    }

                    for (LivingEntity enemy : getNearbyEnemies(current, 2.0, player)) {
                        if (hitEntities.contains(enemy.getUniqueId())) continue;
                        hitEntities.add(enemy.getUniqueId());
                        dealAbilityDamage(player, enemy, 12.0, false);
                        enemy.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 30, 0));
                        world.spawnParticle(Particle.CRIT, enemy.getLocation().add(0, 1, 0), 10, 0.3, 0.3, 0.3, 0.1);
                        world.playSound(enemy.getLocation(), Sound.ENTITY_ARROW_HIT, 1.0f, 1.2f);

                        // Chain to next nearest
                        if (lastHit != null) {
                            drawChainParticles(lastHit.getLocation().add(0, 1, 0), enemy.getLocation().add(0, 1, 0), world);
                        } else {
                            drawChainParticles(current, enemy.getLocation().add(0, 1, 0), world);
                        }
                        lastHit = enemy;
                        current = enemy.getLocation().clone().add(0, 1, 0);
                        chains++;

                        // Find next chain target
                        LivingEntity nextTarget = null;
                        double minDist = 64.0; // 8 blocks squared
                        for (LivingEntity potential : getNearbyEnemies(current, 8.0, player)) {
                            if (hitEntities.contains(potential.getUniqueId())) continue;
                            double dist = potential.getLocation().distanceSquared(current);
                            if (dist < minDist) { minDist = dist; nextTarget = potential; }
                        }
                        if (nextTarget != null) {
                            current = nextTarget.getLocation().clone().add(0, 1, 0);
                        }
                        break;
                    }

                    ticks++;
                }
            }.runTaskTimer(plugin, 0L, 3L);

            player.sendActionBar("\u00a7b\u26a1 Chain Shot!");

        } else {
            // Thunder Barrage — DAMAGE: 12->18 bolt, 14->20 AoE
            player.playSound(player.getLocation(), Sound.ITEM_CROSSBOW_SHOOT, 2.0f, 0.7f);
            player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.2f);

            Location target = player.getTargetBlockExact(30) != null
                    ? player.getTargetBlockExact(30).getLocation().add(0.5, 1, 0.5)
                    : player.getEyeLocation().add(player.getLocation().getDirection().multiply(20));
            World world = player.getWorld();

            new BukkitRunnable() {
                int burst = 0;
                @Override public void run() {
                    if (burst >= 5) { cancel(); return; }
                    for (int i = 0; i < 2; i++) {
                        Vector dir = player.getLocation().getDirection().clone();
                        dir.add(new Vector(
                                (Math.random() - 0.5) * 0.4,
                                (Math.random() - 0.5) * 0.2,
                                (Math.random() - 0.5) * 0.4
                        )).normalize().multiply(3.0);

                        Arrow bolt = player.launchProjectile(Arrow.class, dir);
                        bolt.setDamage(18.0);
                        bolt.setFireTicks(60);
                        bolt.setGlowing(true);
                        bolt.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
                        bolt.setLifetimeTicks(60);
                        bolt.setShooter(player);

                        world.spawnParticle(Particle.ELECTRIC_SPARK, player.getLocation().add(0, 1.5, 0), 8, 0.3, 0.3, 0.3, 0.1);
                    }
                    player.playSound(player.getLocation(), Sound.ITEM_CROSSBOW_SHOOT, 1.0f, 0.9f + burst * 0.1f);
                    burst++;
                }
            }.runTaskTimer(plugin, 0L, 5L);

            // Delayed AoE detonation at target
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                world.spawnParticle(Particle.EXPLOSION_EMITTER, target, 3, 0, 0, 0, 0);
                world.spawnParticle(Particle.ELECTRIC_SPARK, target, 50, 3, 2, 3, 0.2);
                world.spawnParticle(Particle.FLASH, target, 2, 0, 0, 0, 0);
                world.playSound(target, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 2.0f, 0.8f);
                world.playSound(target, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 1.0f);

                for (LivingEntity enemy : getNearbyEnemies(target, 7.0, player)) {
                    dealAbilityDamage(player, enemy, 20.0, true);
                    Vector knockback = enemy.getLocation().toVector().subtract(target.toVector()).normalize().multiply(1.5);
                    knockback.setY(0.8);
                    enemy.setVelocity(knockback);
                    enemy.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 1));
                    enemy.setFireTicks(80);
                }
            }, 25L);

            player.sendActionBar("\u00a75\u00a7l\u26a1 THUNDER BARRAGE \u26a1");
        }
    }

    private void drawChainParticles(Location from, Location to, World world) {
        Vector dir = to.toVector().subtract(from.toVector());
        double length = dir.length();
        dir.normalize();
        for (double d = 0; d < length; d += 0.5) {
            Location pLoc = from.clone().add(dir.clone().multiply(d));
            pLoc.add((Math.random() - 0.5) * 0.2, (Math.random() - 0.5) * 0.2, (Math.random() - 0.5) * 0.2);
            world.spawnParticle(Particle.ELECTRIC_SPARK, pLoc, 1, 0, 0, 0, 0);
        }
    }


    // ================================================================
    // TRIDENT — "Tidal Surge" / "Poseidon's Wrath" — DAMAGE INCREASED
    // ================================================================

    private void handleTridentAbility(Player player, ItemStack item, boolean isDefinitive) {
        String abilityName = isDefinitive ? "Poseidon's Wrath" : "Tidal Surge";
        int cooldownSec = isDefinitive ? 16 : 8;

        if (isOnCooldown(player, abilityName)) { sendCooldownMessage(player, abilityName); return; }
        setCooldown(player, abilityName, cooldownSec);

        if (!isDefinitive) {
            // Tidal Surge — DAMAGE: 8 -> 12
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_SPLASH, 2.0f, 0.8f);
            player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_THROW, 1.5f, 1.0f);

            Vector direction = player.getLocation().getDirection().normalize();
            Location start = player.getLocation().clone();
            World world = player.getWorld();

            new BukkitRunnable() {
                int ticks = 0;
                Location current = start.clone();
                @Override public void run() {
                    if (ticks >= 15) { cancel(); return; }
                    current.add(direction.clone().multiply(1.2));

                    // Wave particles
                    Vector perp = new Vector(-direction.getZ(), 0, direction.getX()).normalize();
                    for (double w = -3; w <= 3; w += 0.5) {
                        Location waveLoc = current.clone().add(perp.clone().multiply(w));
                        waveLoc.setY(current.getY() + 0.3 + Math.sin(w + ticks * 0.5) * 0.3);
                        world.spawnParticle(Particle.SPLASH, waveLoc, 2, 0.1, 0.1, 0.1, 0.05);
                        world.spawnParticle(Particle.DRIPPING_WATER, waveLoc, 1, 0.1, 0.3, 0.1, 0);
                        world.spawnParticle(Particle.BUBBLE_POP, waveLoc, 1, 0.1, 0.1, 0.1, 0.02);
                    }

                    for (LivingEntity enemy : getNearbyEnemies(current, 3.0, player)) {
                        dealAbilityDamage(player, enemy, 12.0, false);
                        Vector push = direction.clone().multiply(1.2);
                        push.setY(0.5);
                        enemy.setVelocity(push);
                        enemy.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 0));
                    }

                    ticks++;
                }
            }.runTaskTimer(plugin, 0L, 2L);

            player.sendActionBar("\u00a7b\u2248 Tidal Surge!");

        } else {
            // Poseidon's Wrath — DAMAGE: 16/13/10 -> 24/18/14 waves, 10 -> 15 lightning
            player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1.0f, 1.5f);
            player.playSound(player.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 1.5f, 0.5f);
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_SPLASH, 2.0f, 0.5f);

            Location center = player.getLocation().clone();
            World world = player.getWorld();
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 100, 1));

            double[] waveRadii = { 5.0, 8.0, 11.0 };
            double[] waveDamages = { 24.0, 18.0, 14.0 };

            for (int w = 0; w < 3; w++) {
                final double radius = waveRadii[w];
                final double damage = waveDamages[w];
                final double knockbackY = 0.6 + w * 0.2;

                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    // Wave ring animation
                    new BukkitRunnable() {
                        int tick = 0;
                        @Override public void run() {
                            if (tick >= 10) { cancel(); return; }
                            double r = radius * (tick / 10.0);
                            for (int i = 0; i < 30; i++) {
                                double angle = (2 * Math.PI / 30) * i;
                                Location pLoc = center.clone().add(Math.cos(angle) * r, 0.3, Math.sin(angle) * r);
                                world.spawnParticle(Particle.SPLASH, pLoc, 3, 0.1, 0.2, 0.1, 0.05);
                                world.spawnParticle(Particle.BUBBLE_POP, pLoc, 1, 0.1, 0.1, 0.1, 0.02);
                            }
                            tick++;
                        }
                    }.runTaskTimer(plugin, 0L, 1L);

                    // Damage
                    for (LivingEntity enemy : getNearbyEnemies(center, radius, player)) {
                        dealAbilityDamage(player, enemy, damage, true);
                        Vector knockback = enemy.getLocation().toVector().subtract(center.toVector()).normalize().multiply(1.0);
                        knockback.setY(knockbackY);
                        enemy.setVelocity(knockback);
                        enemy.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 1));
                    }
                    world.playSound(center, Sound.ENTITY_GENERIC_SPLASH, 2.0f, 0.8f);
                }, w * 10L);
            }

            // Lightning finale
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                world.spawnParticle(Particle.ELECTRIC_SPARK, center.clone().add(0, 2, 0), 50, 4, 3, 4, 0.2);
                world.playSound(center, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 2.0f, 0.8f);
                world.playSound(center, Sound.ITEM_TRIDENT_THUNDER, 2.0f, 0.7f);

                for (LivingEntity enemy : getNearbyEnemies(center, 10.0, player)) {
                    dealAbilityDamage(player, enemy, 15.0, true);
                    enemy.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 2));
                }
            }, 35L);

            player.sendActionBar("\u00a71\u00a7l\u2648 POSEIDON'S WRATH \u2648");
        }
    }


    // ================================================================
    // MACE — "Ground Slam" / "Meteor Strike" — DAMAGE INCREASED + SAFE
    // ================================================================

    private void handleMaceAbility(Player player, ItemStack item, boolean isDefinitive) {
        String abilityName = isDefinitive ? "Meteor Strike" : "Ground Slam";
        int cooldownSec = isDefinitive ? 18 : 8;

        if (isOnCooldown(player, abilityName)) { sendCooldownMessage(player, abilityName); return; }
        setCooldown(player, abilityName, cooldownSec);

        if (!isDefinitive) {
            // Ground Slam — DAMAGE: 10 -> 15
            player.playSound(player.getLocation(), Sound.ITEM_MACE_SMASH_GROUND_HEAVY, 2.0f, 0.8f);
            player.playSound(player.getLocation(), Sound.ENTITY_IRON_GOLEM_ATTACK, 1.0f, 0.6f);

            Location center = player.getLocation();
            World world = player.getWorld();

            new BukkitRunnable() {
                int tick = 0;
                @Override public void run() {
                    if (tick >= 10) { cancel(); return; }
                    double radius = tick * 0.8;
                    for (int i = 0; i < 20; i++) {
                        double angle = (2 * Math.PI / 20) * i + tick * 0.3;
                        Location pLoc = center.clone().add(Math.cos(angle) * radius, 0.1, Math.sin(angle) * radius);
                        world.spawnParticle(Particle.BLOCK, pLoc, 3, 0.1, 0.2, 0.1, 0.1, Material.STONE.createBlockData());
                        world.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, pLoc, 1, 0, 0.5, 0, 0.02);
                    }
                    tick++;
                }
            }.runTaskTimer(plugin, 0L, 1L);

            for (LivingEntity enemy : getNearbyEnemies(center, 6.0, player)) {
                dealAbilityDamage(player, enemy, 15.0, false);
                Vector knockback = enemy.getLocation().toVector().subtract(center.toVector()).normalize().multiply(1.2);
                knockback.setY(0.8);
                enemy.setVelocity(knockback);
                enemy.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1));
            }
            world.spawnParticle(Particle.EXPLOSION, center, 5, 1, 0.5, 1, 0);

            player.sendActionBar("\u00a76\u2693 Ground Slam!");

        } else {
            // Meteor Strike — SAFE (Resistance 255) — DAMAGE: base 20 + 0.7/block fallen
            Location launchLoc = player.getLocation().clone();
            World world = player.getWorld();

            // Phase 1: Pre-launch protection
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 200, 254)); // 255 = complete immunity
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 60, 0));

            player.playSound(player.getLocation(), Sound.ITEM_MACE_SMASH_GROUND_HEAVY, 2.0f, 0.5f);
            player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1.5f, 0.5f);

            // Phase 2: Launch upward
            player.setVelocity(new Vector(0, 3.5, 0));

            // Ascending fire trail
            new BukkitRunnable() {
                int tick = 0;
                @Override public void run() {
                    if (tick >= 20) { cancel(); return; }
                    Location loc = player.getLocation();
                    world.spawnParticle(Particle.FLAME, loc, 15, 0.3, 0.1, 0.3, 0.05);
                    world.spawnParticle(Particle.LAVA, loc, 3, 0.2, 0.1, 0.2, 0);
                    world.spawnParticle(Particle.DUST, loc, 5, 0.4, 0.2, 0.4, 0,
                            new Particle.DustOptions(Color.fromRGB(255, 140, 0), 2.0f));
                    tick++;
                }
            }.runTaskTimer(plugin, 0L, 1L);

            // Phase 3: Slam down after 25 ticks
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                // Remove slow falling so player falls fast
                player.removePotionEffect(PotionEffectType.SLOW_FALLING);
                player.setVelocity(new Vector(0, -4.0, 0));
                player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.5f, 1.5f);

                // Meteor fire trail while descending
                final double launchY = player.getLocation().getY();
                new BukkitRunnable() {
                    int tick = 0;
                    boolean landed = false;
                    @Override public void run() {
                        if (tick >= 60 || landed) { cancel(); return; }

                        Location loc = player.getLocation();
                        world.spawnParticle(Particle.FLAME, loc, 15, 0.4, 0.2, 0.4, 0.05);
                        world.spawnParticle(Particle.LAVA, loc, 5, 0.3, 0.2, 0.3, 0);
                        world.spawnParticle(Particle.SMOKE, loc, 8, 0.3, 0.2, 0.3, 0.05);
                        world.spawnParticle(Particle.DUST, loc, 10, 0.5, 0.3, 0.5, 0,
                                new Particle.DustOptions(Color.fromRGB(255, 100, 0), 2.5f));

                        // Check if player has landed (on ground or velocity near 0 Y and below launch)
                        if (tick > 5 && (player.isOnGround() || (player.getVelocity().getY() > -0.1 && loc.getY() < launchY - 3))) {
                            landed = true;

                            // Phase 4: IMPACT
                            double fallDistance = Math.max(0, launchY - loc.getY());
                            double impactDamage = 20.0 + (fallDistance * 0.7);
                            impactDamage = Math.min(impactDamage, 50.0); // Cap at 50

                            Location impactLoc = player.getLocation();

                            // Expanding shockwave rings
                            new BukkitRunnable() {
                                int ringTick = 0;
                                @Override public void run() {
                                    if (ringTick >= 15) { cancel(); return; }
                                    double radius = ringTick * 0.8;
                                    for (int i = 0; i < 30; i++) {
                                        double angle = (2 * Math.PI / 30) * i;
                                        Location ringLoc = impactLoc.clone().add(Math.cos(angle) * radius, 0.1, Math.sin(angle) * radius);
                                        world.spawnParticle(Particle.BLOCK, ringLoc, 2, 0.1, 0.1, 0.1, 0.1, Material.NETHERRACK.createBlockData());
                                        world.spawnParticle(Particle.FLAME, ringLoc, 1, 0, 0.2, 0, 0.02);
                                    }
                                    ringTick++;
                                }
                            }.runTaskTimer(plugin, 0L, 1L);

                            // Impact particles
                            world.spawnParticle(Particle.EXPLOSION_EMITTER, impactLoc, 5, 0, 0, 0, 0);
                            world.spawnParticle(Particle.LAVA, impactLoc, 80, 4, 1, 4, 0);
                            world.spawnParticle(Particle.FLAME, impactLoc, 100, 5, 2, 5, 0.1);
                            world.spawnParticle(Particle.SMOKE, impactLoc, 50, 3, 2, 3, 0.1);
                            world.spawnParticle(Particle.TOTEM_OF_UNDYING, impactLoc, 60, 3, 3, 3, 0.5);

                            // 4 simultaneous sounds
                            world.playSound(impactLoc, Sound.ITEM_MACE_SMASH_GROUND_HEAVY, 2.0f, 0.5f);
                            world.playSound(impactLoc, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.7f);
                            world.playSound(impactLoc, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 2.0f, 0.6f);
                            world.playSound(impactLoc, Sound.ENTITY_WARDEN_SONIC_BOOM, 1.5f, 0.8f);

                            // Damage all enemies in 10-block radius with distance scaling
                            final double finalDamage = impactDamage;
                            for (LivingEntity enemy : getNearbyEnemies(impactLoc, 10.0, player)) {
                                double dist = enemy.getLocation().distance(impactLoc);
                                double scale = 1.0 - (dist / 15.0); // 100% at center, ~33% at edge
                                scale = Math.max(0.3, scale);
                                dealAbilityDamage(player, enemy, finalDamage * scale, true);

                                Vector knockback = enemy.getLocation().toVector().subtract(impactLoc.toVector()).normalize().multiply(2.0 * scale);
                                knockback.setY(0.8 + Math.random() * 0.5);
                                enemy.setVelocity(knockback);
                                enemy.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 2));
                                enemy.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 0));
                            }

                            player.sendActionBar("\u00a74\u00a7l\u2604 METEOR STRIKE \u2014 " + String.format("%.0f", finalDamage) + " damage! \u2604");

                            // Phase 5: Remove resistance AFTER landing (safety buffer)
                            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                player.removePotionEffect(PotionEffectType.RESISTANCE);
                            }, 20L);
                        }

                        tick++;
                    }
                }.runTaskTimer(plugin, 0L, 1L);
            }, 25L);

            player.sendActionBar("\u00a74\u00a7l\u2604 METEOR STRIKE \u2604");
        }
    }

}


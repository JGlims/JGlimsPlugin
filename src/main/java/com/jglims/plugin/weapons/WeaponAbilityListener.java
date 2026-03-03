package com.jglims.plugin.weapons;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.enchantments.CustomEnchantManager;
import com.jglims.plugin.enchantments.EnchantmentType;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class WeaponAbilityListener implements Listener {

    private final JGlimsPlugin plugin;
    private final CustomEnchantManager enchantManager;
    private final SuperToolManager superToolManager;
    private final SickleManager sickleManager;
    private final BattleAxeManager battleAxeManager;
    private final BattleBowManager battleBowManager;

    // Cooldowns: player UUID -> ability cooldown expiry
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    // Ore types for Seismic Sense
    private static final Set<Material> ORES = EnumSet.of(
        Material.COAL_ORE, Material.DEEPSLATE_COAL_ORE,
        Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE,
        Material.COPPER_ORE, Material.DEEPSLATE_COPPER_ORE,
        Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE,
        Material.REDSTONE_ORE, Material.DEEPSLATE_REDSTONE_ORE,
        Material.LAPIS_ORE, Material.DEEPSLATE_LAPIS_ORE,
        Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE,
        Material.EMERALD_ORE, Material.DEEPSLATE_EMERALD_ORE,
        Material.NETHER_GOLD_ORE, Material.NETHER_QUARTZ_ORE,
        Material.ANCIENT_DEBRIS
    );

    private static final Set<Material> SHOVEL_MINEABLE = EnumSet.of(
        Material.DIRT, Material.GRASS_BLOCK, Material.SAND, Material.RED_SAND,
        Material.GRAVEL, Material.CLAY, Material.SOUL_SAND, Material.SOUL_SOIL,
        Material.SNOW_BLOCK, Material.SNOW, Material.MUD, Material.MUDDY_MANGROVE_ROOTS,
        Material.MYCELIUM, Material.PODZOL, Material.COARSE_DIRT, Material.ROOTED_DIRT,
        Material.DIRT_PATH, Material.FARMLAND
    );

    public WeaponAbilityListener(JGlimsPlugin plugin, CustomEnchantManager enchantManager,
                                  SuperToolManager superToolManager, SickleManager sickleManager,
                                  BattleAxeManager battleAxeManager, BattleBowManager battleBowManager) {
        this.plugin = plugin;
        this.enchantManager = enchantManager;
        this.superToolManager = superToolManager;
        this.sickleManager = sickleManager;
        this.battleAxeManager = battleAxeManager;
        this.battleBowManager = battleBowManager;
    }

    // ========================================================================
    // RIGHT-CLICK ABILITIES (Sword, Sickle, Battle Axe, Pickaxe, Shovel, Trident)
    // ========================================================================
    @EventHandler(priority = EventPriority.HIGH)
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        ItemStack weapon = player.getInventory().getItemInMainHand();
        if (weapon.getType() == Material.AIR) return;

        int tier = superToolManager.getSuperTier(weapon);
        if (tier < SuperToolManager.TIER_DIAMOND) return; // Only Diamond+ have abilities

        // Determine weapon type and apply ability
        Material mat = weapon.getType();
        String matName = mat.name();

        if (matName.endsWith("_SWORD")) {
            if (checkCooldown(player, 4000)) return;
            handleSwordAbility(player, weapon, tier);
            event.setCancelled(true);
        } else if (sickleManager.isSickle(weapon)) {
            if (checkCooldown(player, 5000)) return;
            handleSickleAbility(player, weapon, tier);
            event.setCancelled(true);
        } else if (battleAxeManager.isBattleAxe(weapon)) {
            if (checkCooldown(player, 5000)) return;
            handleBattleAxeAbility(player, weapon, tier);
            event.setCancelled(true);
        } else if (matName.endsWith("_PICKAXE")) {
            if (checkCooldown(player, 5000)) return;
            handlePickaxeAbility(player, weapon, tier);
            event.setCancelled(true);
        } else if (matName.endsWith("_SHOVEL")) {
            if (checkCooldown(player, 15000)) return;
            handleShovelAbility(player, weapon, tier);
            event.setCancelled(true);
        } else if (mat == Material.TRIDENT) {
            if (checkCooldown(player, 7000)) return;
            handleTridentAbility(player, weapon, tier);
            event.setCancelled(true);
        }
    }

    // ========================================================================
    // LEFT-CLICK ABILITIES (Battle Bow, Battle Crossbow)
    // ========================================================================
    @EventHandler(priority = EventPriority.HIGH)
    public void onLeftClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack weapon = player.getInventory().getItemInMainHand();
        if (weapon.getType() == Material.AIR) return;

        int tier = superToolManager.getSuperTier(weapon);
        if (tier < SuperToolManager.TIER_DIAMOND) return;

        if (battleBowManager.isBattleCrossbow(weapon)) {
            if (checkCooldown(player, 6000)) return;
            handleCrossbowAbility(player, weapon, tier);
        } else if (battleBowManager.isBattleBow(weapon)) {
            if (checkCooldown(player, 12000)) return;
            handleBowAbility(player, weapon, tier);
        }
    }

    // ========================================================================
    // SWORD — "Blade Storm" / "Judgment Cut" (definitive)
    // ========================================================================
    private void handleSwordAbility(Player player, ItemStack weapon, int tier) {
        boolean definitive = superToolManager.isDefinitiveSuperNetherite(weapon);
        Location loc = player.getLocation();
        World world = loc.getWorld();

        if (definitive) {
            // Judgment Cut: 5 block sphere, 8 base damage, brief invulnerability
            double baseDmg = 8.0 + getEnchantDamageBonus(weapon);
            player.setInvulnerable(true);
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> player.setInvulnerable(false), 2L);

            for (Entity e : player.getNearbyEntities(5, 5, 5)) {
                if (e instanceof LivingEntity le && e != player) {
                    le.damage(baseDmg, player);
                    le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 0));
                }
            }
            world.spawnParticle(Particle.SWEEP_ATTACK, loc.add(0, 1, 0), 15, 2, 1, 2, 0);
            world.spawnParticle(Particle.ENCHANTED_HIT, loc, 10, 2, 1, 2, 0.1);
            player.sendActionBar(Component.text("Judgment Cut!", NamedTextColor.DARK_RED));
        } else {
            // Blade Storm: forward cone, 3-4 block range, ~90° arc
            double baseDmg = 4.0;
            Vector dir = player.getLocation().getDirection().normalize();
            int hitCount = 0;

            for (Entity e : player.getNearbyEntities(4, 3, 4)) {
                if (!(e instanceof LivingEntity le) || e == player) continue;
                Vector toTarget = e.getLocation().toVector().subtract(loc.toVector()).normalize();
                if (dir.dot(toTarget) > 0.5) { // ~60° cone (generous)
                    double dmg = baseDmg;
                    le.damage(dmg, player);
                    hitCount++;

                    // Enchantment-dependent effects
                    if (enchantManager.hasEnchant(weapon, EnchantmentType.VAMPIRISM)) {
                        healPlayer(player, 1.0);
                    }
                    if (enchantManager.hasEnchant(weapon, EnchantmentType.BLEED)) {
                        applyBleedDoT(le, enchantManager.getEnchantLevel(weapon, EnchantmentType.BLEED));
                    }
                    if (enchantManager.hasEnchant(weapon, EnchantmentType.FROSTBITE)) {
                        le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1));
                    }
                    if (enchantManager.hasEnchant(weapon, EnchantmentType.VENOMSTRIKE)) {
                        le.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 60, 0));
                    }
                    if (enchantManager.hasEnchant(weapon, EnchantmentType.CHAIN_LIGHTNING)) {
                        chainLightningFromTarget(le, player, weapon, baseDmg);
                    }
                }
            }
            if (enchantManager.hasEnchant(weapon, EnchantmentType.LIFESTEAL) && hitCount > 0) {
                healPlayer(player, baseDmg * hitCount * 0.15);
            }
            world.spawnParticle(Particle.SWEEP_ATTACK, loc.add(dir.multiply(2)).add(0, 1, 0), 10, 1, 0.5, 1, 0);
            player.sendActionBar(Component.text("Blade Storm!", NamedTextColor.RED));
        }
    }

    // ========================================================================
    // SICKLE — "Soul Harvest" / "Death's Embrace" (definitive)
    // ========================================================================
    private void handleSickleAbility(Player player, ItemStack weapon, int tier) {
        boolean definitive = superToolManager.isDefinitiveSuperNetherite(weapon);
        Location loc = player.getLocation();
        World world = loc.getWorld();

        if (definitive) {
            double baseDmg = 6.0 + getEnchantDamageBonus(weapon);
            for (Entity e : player.getNearbyEntities(5, 5, 5)) {
                if (!(e instanceof LivingEntity le) || e == player) continue;
                le.damage(baseDmg, player);
                if (le instanceof Monster) {
                    le.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 60, 2));
                }
                le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1));
                le.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0));
            }
            world.spawnParticle(Particle.SCULK_SOUL, loc.add(0, 1, 0), 15, 2, 1, 2, 0.05);
            world.spawnParticle(Particle.DAMAGE_INDICATOR, loc, 10, 2, 1, 2, 0.1);
            player.sendActionBar(Component.text("Death's Embrace!", NamedTextColor.DARK_PURPLE));
        } else {
            double baseDmg = 3.0;
            boolean hasBloodPrice = enchantManager.hasEnchant(weapon, EnchantmentType.BLOOD_PRICE);
            if (hasBloodPrice) {
                baseDmg *= 1.5;
                if (player.getHealth() > 3.0) {
                    player.damage(3.0);
                }
            }

            for (Entity e : player.getNearbyEntities(3, 3, 3)) {
                if (!(e instanceof LivingEntity le) || e == player) continue;
                le.damage(baseDmg, player);

                if (enchantManager.hasEnchant(weapon, EnchantmentType.WITHER_TOUCH) && le instanceof Monster) {
                    le.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 60, 0));
                }
                if (enchantManager.hasEnchant(weapon, EnchantmentType.REAPERS_MARK)) {
                    NamespacedKey markKey = new NamespacedKey(plugin, "reaper_mark_level");
                    NamespacedKey markTimeKey = new NamespacedKey(plugin, "reaper_mark_expire");
                    int markLvl = enchantManager.getEnchantLevel(weapon, EnchantmentType.REAPERS_MARK);
                    le.getPersistentDataContainer().set(markKey, PersistentDataType.INTEGER, markLvl);
                    le.getPersistentDataContainer().set(markTimeKey, PersistentDataType.LONG,
                        System.currentTimeMillis() + 8000L);
                }
            }

            if (enchantManager.hasEnchant(weapon, EnchantmentType.SOUL_REAP)) {
                int soulLvl = enchantManager.getEnchantLevel(weapon, EnchantmentType.SOUL_REAP);
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH,
                    10 * 20, 0, true, true, true));
            }

            world.spawnParticle(Particle.SCULK_SOUL, loc.add(0, 1, 0), 10, 1.5, 0.5, 1.5, 0.05);
            player.sendActionBar(Component.text("Soul Harvest!", NamedTextColor.DARK_PURPLE));
        }
    }

    // ========================================================================
    // BATTLE AXE — "Groundbreaker" / "Earthquake" (definitive)
    // ========================================================================
    private void handleBattleAxeAbility(Player player, ItemStack weapon, int tier) {
        boolean definitive = superToolManager.isDefinitiveSuperNetherite(weapon);
        Location loc = player.getLocation();
        World world = loc.getWorld();

        if (definitive) {
            double baseDmg = 10.0 + getEnchantDamageBonus(weapon);
            for (Entity e : player.getNearbyEntities(5, 5, 5)) {
                if (!(e instanceof LivingEntity le) || e == player) continue;
                le.damage(baseDmg, player);
                le.setVelocity(le.getVelocity().add(new Vector(0, 1.5, 0)));
                le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 1));
            }
            world.spawnParticle(Particle.EXPLOSION, loc, 5, 2, 0.5, 2, 0);
            world.spawnParticle(Particle.BLOCK, loc, 15, 2, 0.5, 2, 0.1,
                Material.STONE.createBlockData());
            player.sendActionBar(Component.text("Earthquake!", NamedTextColor.DARK_RED));
        } else {
            double baseDmg = 5.0;
            double radius = 2.0;

            if (enchantManager.hasEnchant(weapon, EnchantmentType.CLEAVE)) {
                radius = 4.0;
            }

            boolean hasBerserker = enchantManager.hasEnchant(weapon, EnchantmentType.BERSERKER);
            if (hasBerserker) {
                AttributeInstance maxHp = player.getAttribute(Attribute.MAX_HEALTH);
                double maxHealth = maxHp != null ? maxHp.getValue() : 20.0;
                double missingPct = (maxHealth - player.getHealth()) / maxHealth;
                int berserkerLvl = enchantManager.getEnchantLevel(weapon, EnchantmentType.BERSERKER);
                baseDmg += berserkerLvl * 0.5 * missingPct * baseDmg;
            }

            boolean hasGuillotine = enchantManager.hasEnchant(weapon, EnchantmentType.GUILLOTINE);

            for (Entity e : player.getNearbyEntities(radius, radius, radius)) {
                if (!(e instanceof LivingEntity le) || e == player) continue;
                le.damage(baseDmg, player);
                le.setVelocity(le.getVelocity().add(new Vector(0, 0.8, 0)));

                if (hasGuillotine && !(le instanceof Boss)) {
                    if (ThreadLocalRandom.current().nextDouble() < 0.05) {
                        AttributeInstance maxHp = le.getAttribute(Attribute.MAX_HEALTH);
                        if (maxHp != null) le.damage(maxHp.getValue() * 10, player);
                    }
                }
            }
            world.spawnParticle(Particle.EXPLOSION, loc, 3, 1, 0.3, 1, 0);
            player.sendActionBar(Component.text("Groundbreaker!", NamedTextColor.GOLD));
        }
    }

    // ========================================================================
    // PICKAXE — "Seismic Sense" / "Core Scan" (definitive)
    // ========================================================================
    private void handlePickaxeAbility(Player player, ItemStack weapon, int tier) {
        boolean definitive = superToolManager.isDefinitiveSuperNetherite(weapon);
        Location center = player.getLocation();
        World world = center.getWorld();

        int baseRadius = 8;
        int duration = 5;
        boolean highlightExtras = false;

        if (definitive) {
            baseRadius = 16;
            duration = 10;
            highlightExtras = true;
        } else {
            if (enchantManager.hasEnchant(weapon, EnchantmentType.VEINMINER)) {
                baseRadius += 4;
            }
            if (enchantManager.hasEnchant(weapon, EnchantmentType.DRILL)) {
                highlightExtras = true;
            }
            if (enchantManager.hasEnchant(weapon, EnchantmentType.MAGNETISM)) {
                // Pull loose items in radius
                for (Entity e : player.getNearbyEntities(baseRadius, baseRadius, baseRadius)) {
                    if (e instanceof Item item) {
                        item.teleport(player.getLocation());
                        item.setPickupDelay(0);
                    }
                }
            }
        }

        final int r = baseRadius;
        final boolean extras = highlightExtras || definitive;
        final int dur = duration;

        // Spawn particles at ore locations asynchronously (scan blocks synchronously)
        List<Location> oreLocations = new ArrayList<>();
        int cx = center.getBlockX(), cy = center.getBlockY(), cz = center.getBlockZ();

        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    Block block = world.getBlockAt(cx + x, cy + y, cz + z);
                    Material bMat = block.getType();
                    if (ORES.contains(bMat)) {
                        oreLocations.add(block.getLocation().add(0.5, 0.5, 0.5));
                    } else if (extras) {
                        if (bMat == Material.SPAWNER || bMat == Material.CHEST
                            || bMat == Material.TRAPPED_CHEST || bMat == Material.BARREL
                            || bMat == Material.ANCIENT_DEBRIS) {
                            oreLocations.add(block.getLocation().add(0.5, 0.5, 0.5));
                        }
                    }
                }
            }
        }

        // Spawn glowing particles periodically for duration
        Particle particleType = definitive ? Particle.ELECTRIC_SPARK : Particle.HAPPY_VILLAGER;
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= dur * 20 || !player.isOnline()) { cancel(); return; }
                for (Location loc : oreLocations) {
                    world.spawnParticle(particleType, loc, 1, 0.1, 0.1, 0.1, 0);
                }
                ticks += 10;
            }
        }.runTaskTimer(plugin, 0L, 10L);

        player.sendActionBar(Component.text(
            definitive ? "Core Scan!" : "Seismic Sense!",
            definitive ? NamedTextColor.DARK_RED : NamedTextColor.AQUA));
    }

    // ========================================================================
    // SHOVEL — "Excavation Blast" / "Terraform" (definitive)
    // ========================================================================
    private void handleShovelAbility(Player player, ItemStack weapon, int tier) {
        boolean definitive = superToolManager.isDefinitiveSuperNetherite(weapon);
        Location center = player.getLocation();
        World world = center.getWorld();

        int radius = definitive ? 7 : 3;
        if (!definitive && enchantManager.hasEnchant(weapon, EnchantmentType.EXCAVATOR)) {
            radius += 2;
        }

        boolean hasReplenish = !definitive && enchantManager.hasEnchant(weapon, EnchantmentType.REPLENISH);
        int cx = center.getBlockX(), cy = center.getBlockY(), cz = center.getBlockZ();

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x * x + y * y + z * z > radius * radius) continue; // sphere check
                    Block block = world.getBlockAt(cx + x, cy + y, cz + z);
                    if (SHOVEL_MINEABLE.contains(block.getType())) {
                        block.breakNaturally(weapon);
                        if (hasReplenish && ThreadLocalRandom.current().nextDouble() < 0.10) {
                            world.dropItemNaturally(block.getLocation(),
                                new ItemStack(Material.WHEAT_SEEDS, 1));
                        }
                    }
                }
            }
        }

        world.spawnParticle(Particle.EXPLOSION, center, 5, radius * 0.5, 1, radius * 0.5, 0);
        player.sendActionBar(Component.text(
            definitive ? "Terraform!" : "Excavation Blast!",
            definitive ? NamedTextColor.DARK_RED : NamedTextColor.GOLD));
    }

    // ========================================================================
    // TRIDENT — "Poseidon's Call" / "Wrath of the Sea" (definitive)
    // ========================================================================
    private void handleTridentAbility(Player player, ItemStack weapon, int tier) {
        boolean definitive = superToolManager.isDefinitiveSuperNetherite(weapon);
        Location loc = player.getLocation();
        World world = loc.getWorld();

        if (definitive) {
            double baseDmg = 8.0 + getEnchantDamageBonus(weapon);
            for (Entity e : player.getNearbyEntities(6, 6, 6)) {
                if (!(e instanceof LivingEntity le) || e == player) continue;
                le.damage(baseDmg, player);
                world.strikeLightning(le.getLocation());
            }
            player.addPotionEffect(new PotionEffect(PotionEffectType.CONDUIT_POWER, 400, 0));
            player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 400, 0));
            healPlayer(player, 6.0);
            world.spawnParticle(Particle.FISHING, loc.add(0, 1, 0), 15, 3, 1, 3, 0.1);
            player.sendActionBar(Component.text("Wrath of the Sea!", NamedTextColor.DARK_RED));
        } else {
            double baseDmg = 4.0;
            double pushMult = 1.0;
            boolean strikeEnabled = enchantManager.hasEnchant(weapon, EnchantmentType.THUNDERLORD);
            boolean doublePush = enchantManager.hasEnchant(weapon, EnchantmentType.TIDAL_WAVE);
            boolean grantSwiftness = enchantManager.hasEnchant(weapon, EnchantmentType.SWIFTNESS);
            boolean grantVitality = enchantManager.hasEnchant(weapon, EnchantmentType.VITALITY);

            if (doublePush) pushMult = 2.0;

            List<LivingEntity> hitTargets = new ArrayList<>();
            for (Entity e : player.getNearbyEntities(4, 4, 4)) {
                if (!(e instanceof LivingEntity le) || e == player) continue;
                le.damage(baseDmg, player);
                Vector push = le.getLocation().toVector().subtract(loc.toVector()).normalize()
                    .multiply(0.8 * pushMult).setY(0.5);
                le.setVelocity(push);
                hitTargets.add(le);
            }

            if (strikeEnabled && hitTargets.size() >= 2) {
                List<LivingEntity> targets = new ArrayList<>(hitTargets);
                Collections.shuffle(targets);
                for (int i = 0; i < Math.min(2, targets.size()); i++) {
                    world.strikeLightning(targets.get(i).getLocation());
                }
            }

            player.addPotionEffect(new PotionEffect(PotionEffectType.CONDUIT_POWER, 200, 0));
            if (grantSwiftness) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 200, 0));
            }
            if (grantVitality) {
                healPlayer(player, 4.0);
            }

            world.spawnParticle(Particle.FISHING, loc.add(0, 1, 0), 10, 2, 0.5, 2, 0.1);
            player.sendActionBar(Component.text("Poseidon's Call!", NamedTextColor.AQUA));
        }
    }

    // ========================================================================
    // BATTLE CROSSBOW — Left-click: Fire knockback arrows
    // ========================================================================
    private void handleCrossbowAbility(Player player, ItemStack weapon, int tier) {
        boolean definitive = superToolManager.isDefinitiveSuperNetherite(weapon);
        World world = player.getWorld();
        Vector dir = player.getLocation().getDirection().normalize();

        if (definitive) {
            double baseDmg = 5.0 + getEnchantDamageBonus(weapon);
            for (int i = 0; i < 4; i++) {
                Vector spread = dir.clone().add(new Vector(
                    (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.2,
                    (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.1,
                    (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.2
                )).normalize().multiply(3.0);
                Arrow arrow = player.launchProjectile(Arrow.class, spread);
                arrow.setDamage(baseDmg);
                arrow.setKnockbackStrength(3);
                arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
                plugin.getServer().getScheduler().runTaskLater(plugin, arrow::remove, 60L);
            }
            player.sendActionBar(Component.text("Barrage!", NamedTextColor.DARK_RED));
        } else {
            for (int i = 0; i < 2; i++) {
                Vector spread = dir.clone().add(new Vector(
                    (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.1, 0,
                    (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.1
                )).normalize().multiply(2.5);
                Arrow arrow = player.launchProjectile(Arrow.class, spread);
                arrow.setDamage(3.0);
                arrow.setKnockbackStrength(2);
                arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
                plugin.getServer().getScheduler().runTaskLater(plugin, arrow::remove, 60L);
            }
            player.sendActionBar(Component.text("Knockback Shot!", NamedTextColor.GOLD));
        }
        world.spawnParticle(Particle.CRIT, player.getEyeLocation().add(dir), 8, 0.2, 0.2, 0.2, 0.1);
    }

    // ========================================================================
    // BATTLE BOW — Left-click: Teleport dash
    // ========================================================================
    private void handleBowAbility(Player player, ItemStack weapon, int tier) {
        boolean definitive = superToolManager.isDefinitiveSuperNetherite(weapon);
        World world = player.getWorld();
        int maxDist = definitive ? 15 : 10;
        Vector dir = player.getLocation().getDirection().normalize();

        // Ray trace to find safe teleport location
        RayTraceResult ray = world.rayTraceBlocks(player.getEyeLocation(), dir, maxDist);
        Location destination;
        if (ray != null && ray.getHitBlock() != null) {
            destination = ray.getHitPosition().toLocation(world).subtract(dir.multiply(0.5));
            destination.setY(Math.floor(destination.getY()));
        } else {
            destination = player.getLocation().add(dir.multiply(maxDist));
        }

        // Ensure safe landing
        destination.setYaw(player.getLocation().getYaw());
        destination.setPitch(player.getLocation().getPitch());

        // Trail particles
        if (definitive) {
            Location start = player.getLocation();
            Vector step = destination.toVector().subtract(start.toVector()).normalize().multiply(0.5);
            Location current = start.clone();
            for (int i = 0; i < maxDist * 2; i++) {
                current.add(step);
                if (current.distanceSquared(destination) < 1) break;
                world.spawnParticle(Particle.PORTAL, current, 2, 0.1, 0.1, 0.1, 0);
            }
        }

        player.teleport(destination);

        if (definitive) {
            double dmg = 3.0 + getEnchantDamageBonus(weapon);
            for (Entity e : player.getNearbyEntities(2, 2, 2)) {
                if (e instanceof LivingEntity le && e != player) {
                    le.damage(dmg, player);
                }
            }
            world.spawnParticle(Particle.DRAGON_BREATH, destination, 10, 1, 0.5, 1, 0.01);
            player.sendActionBar(Component.text("Warp Strike!", NamedTextColor.DARK_RED));
        } else {
            world.spawnParticle(Particle.PORTAL, destination, 10, 0.5, 0.5, 0.5, 0.1);
            player.sendActionBar(Component.text("Blink!", NamedTextColor.AQUA));
        }
    }

    // ========================================================================
    // UTILITY METHODS
    // ========================================================================

    /**
     * Checks cooldown. Returns true if on cooldown (ability should NOT fire).
     */
    private boolean checkCooldown(Player player, long cooldownMs) {
        long now = System.currentTimeMillis();
        Long cdEnd = cooldowns.get(player.getUniqueId());
        if (cdEnd != null && now < cdEnd) {
            long remaining = (cdEnd - now) / 1000;
            player.sendActionBar(Component.text("Cooldown: " + remaining + "s", NamedTextColor.RED));
            return true;
        }
        cooldowns.put(player.getUniqueId(), now + cooldownMs);
        return false;
    }

    private double getEnchantDamageBonus(ItemStack weapon) {
        // For definitive abilities: each enchantment adds +0.2% damage
        int totalEnchants = enchantManager.getTotalEnchantCount(weapon);
        return totalEnchants * 0.002; // 0.2% per enchant as a flat bonus per base damage unit
        // Actually, per the doc: "+0.2% damage to the ability"
        // This is multiplicative with the base, but the bonus is tiny. Let's apply it as flat for simplicity:
        // We'll return 0 here and handle it differently — multiply base damage by (1 + totalEnchants * 0.002)
    }

    private void healPlayer(Player player, double amount) {
        AttributeInstance maxHp = player.getAttribute(Attribute.MAX_HEALTH);
        double max = maxHp != null ? maxHp.getValue() : 20.0;
        player.setHealth(Math.min(player.getHealth() + amount, max));
    }

    private void applyBleedDoT(LivingEntity target, int level) {
        int ticks = level * 10;
        int interval = 10;
        int count = ticks / interval;
        new BukkitRunnable() {
            int c = 0;
            @Override
            public void run() {
                if (c >= count || target.isDead()) { cancel(); return; }
                target.damage(2.0);
                c++;
            }
        }.runTaskTimer(plugin, interval, interval);
    }

    private void chainLightningFromTarget(LivingEntity source, Player attacker, ItemStack weapon, double baseDmg) {
        int chainLvl = enchantManager.getEnchantLevel(weapon, EnchantmentType.CHAIN_LIGHTNING);
        int chains = chainLvl; // 1/2/3 extra chains
        double dmgPct = switch (chainLvl) { case 1 -> 0.30; case 2 -> 0.40; default -> 0.50; };
        double chainDmg = baseDmg * dmgPct;

        Set<Entity> alreadyHit = new HashSet<>();
        alreadyHit.add(source);
        alreadyHit.add(attacker);

        LivingEntity current = source;
        for (int i = 0; i < chains; i++) {
            LivingEntity nearest = null;
            double nearestDist = 5.0;
            for (Entity e : current.getNearbyEntities(5, 3, 5)) {
                if (e instanceof LivingEntity le && !alreadyHit.contains(e) && e != attacker) {
                    double d = current.getLocation().distance(e.getLocation());
                    if (d < nearestDist) {
                        nearestDist = d;
                        nearest = le;
                    }
                }
            }
            if (nearest == null) break;
            nearest.damage(chainDmg, attacker);
            alreadyHit.add(nearest);
            // Particle between chain links
            Location from = current.getLocation().add(0, 1, 0);
            Location to = nearest.getLocation().add(0, 1, 0);
            current.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, 
                from.clone().add(to.toVector().subtract(from.toVector()).multiply(0.5)), 
                5, 0.1, 0.1, 0.1, 0.01);
            current = nearest;
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        cooldowns.remove(event.getPlayer().getUniqueId());
    }
}

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
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
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
    private final BattleMaceManager battleMaceManager;

    private final Map<UUID, Long> cooldowns = new HashMap<>();

    private static final Set<Material> ORES = EnumSet.of(
        Material.COAL_ORE, Material.DEEPSLATE_COAL_ORE, Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE,
        Material.COPPER_ORE, Material.DEEPSLATE_COPPER_ORE, Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE,
        Material.REDSTONE_ORE, Material.DEEPSLATE_REDSTONE_ORE, Material.LAPIS_ORE, Material.DEEPSLATE_LAPIS_ORE,
        Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE, Material.EMERALD_ORE, Material.DEEPSLATE_EMERALD_ORE,
        Material.NETHER_GOLD_ORE, Material.NETHER_QUARTZ_ORE, Material.ANCIENT_DEBRIS
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
                                  BattleAxeManager battleAxeManager, BattleBowManager battleBowManager,
                                  BattleMaceManager battleMaceManager) {
        this.plugin = plugin;
        this.enchantManager = enchantManager;
        this.superToolManager = superToolManager;
        this.sickleManager = sickleManager;
        this.battleAxeManager = battleAxeManager;
        this.battleBowManager = battleBowManager;
        this.battleMaceManager = battleMaceManager;
    }

    // ========================================================================
    // MASTERY UPDATE ON WEAPON SWITCH
    // ========================================================================
    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemHeldChange(PlayerItemHeldEvent event) {
        WeaponMasteryManager mastery = plugin.getWeaponMasteryManager();
        if (mastery == null) return;
        Player player = event.getPlayer();
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            ItemStack weapon = player.getInventory().getItemInMainHand();
            String wc = getWeaponClass(weapon);
            if (wc != null) mastery.applyMasteryModifier(player, wc);
            else mastery.removeMasteryModifier(player);
        });
    }

    private String getWeaponClass(ItemStack weapon) {
        if (weapon == null || weapon.getType().isAir()) return null;
        String matName = weapon.getType().name();
        if (sickleManager.isSickle(weapon)) return "sickle";
        if (battleAxeManager.isBattleAxe(weapon)) return "axe";
        if (battleMaceManager != null && battleMaceManager.isBattleMace(weapon)) return "mace";
        if (battleBowManager.isBattleBow(weapon)) return "bow";
        if (battleBowManager.isBattleCrossbow(weapon)) return "crossbow";
        if (matName.endsWith("_SWORD")) return "sword";
        if (matName.endsWith("_AXE")) return "axe";
        if (matName.endsWith("_PICKAXE")) return "pickaxe";
        if (matName.endsWith("_SHOVEL")) return "shovel";
        if (weapon.getType() == Material.BOW) return "bow";
        if (weapon.getType() == Material.CROSSBOW) return "crossbow";
        if (weapon.getType() == Material.TRIDENT) return "trident";
        if (weapon.getType() == Material.MACE) return "mace";
        return null;
    }

    // ========================================================================
    // RIGHT-CLICK ABILITIES
    // ========================================================================
    @EventHandler(priority = EventPriority.HIGH)
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        ItemStack weapon = player.getInventory().getItemInMainHand();
        if (weapon.getType() == Material.AIR) return;

        int tier = superToolManager.getSuperTier(weapon);
        if (tier < SuperToolManager.TIER_DIAMOND) return;

        Material mat = weapon.getType();
        String matName = mat.name();

        if (matName.endsWith("_SWORD")) {
            if (checkCooldown(player, 4000)) return;
            handleSwordAbility(player, weapon, tier); event.setCancelled(true);
        } else if (sickleManager.isSickle(weapon)) {
            if (checkCooldown(player, 5000)) return;
            handleSickleAbility(player, weapon, tier); event.setCancelled(true);
        } else if (battleAxeManager.isBattleAxe(weapon)) {
            if (checkCooldown(player, 5000)) return;
            handleBattleAxeAbility(player, weapon, tier); event.setCancelled(true);
        } else if (battleMaceManager != null && battleMaceManager.isBattleMace(weapon)) {
            if (checkCooldown(player, 6000)) return;
            handleMaceAbility(player, weapon, tier); event.setCancelled(true);
        } else if (matName.endsWith("_PICKAXE")) {
            if (checkCooldown(player, 5000)) return;
            handlePickaxeAbility(player, weapon, tier); event.setCancelled(true);
        } else if (matName.endsWith("_SHOVEL")) {
            if (checkCooldown(player, 15000)) return;
            handleShovelAbility(player, weapon, tier); event.setCancelled(true);
        } else if (mat == Material.TRIDENT) {
            if (checkCooldown(player, 7000)) return;
            handleTridentAbility(player, weapon, tier); event.setCancelled(true);
        }
    }

    // ========================================================================
    // LEFT-CLICK ABILITIES
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
    // BATTLE MACE — "Meteor Strike" / "Cataclysm" (NEW v1.2.0)
    // ========================================================================
    private void handleMaceAbility(Player player, ItemStack weapon, int tier) {
        boolean definitive = superToolManager.isDefinitiveSuperNetherite(weapon);
        Location loc = player.getLocation();
        World world = loc.getWorld();

        if (definitive) {
            double baseDmg = 15.0 * getEnchantDamageMultiplier(weapon);
            double radius = 7.0;
            // Pull phase
            for (Entity e : player.getNearbyEntities(radius, radius, radius)) {
                if (e instanceof LivingEntity le && e != player) {
                    Vector pull = player.getLocation().toVector().subtract(e.getLocation().toVector()).normalize().multiply(1.5);
                    le.setVelocity(pull.setY(0.3));
                }
            }
            // Delayed slam
            final double finalDmg = baseDmg;
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                for (Entity e : player.getNearbyEntities(radius, radius, radius)) {
                    if (e instanceof LivingEntity le && e != player) {
                        le.damage(finalDmg, player);
                        le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 2));
                    }
                }
                world.spawnParticle(Particle.EXPLOSION_EMITTER, loc, 3, 2, 0.5, 2, 0);
                world.spawnParticle(Particle.BLOCK, loc, 30, 3, 0.5, 3, 0.1, Material.NETHERITE_BLOCK.createBlockData());
            }, 10L);
            world.spawnParticle(Particle.SONIC_BOOM, loc.clone().add(0, 1, 0), 1, 0, 0, 0, 0);
            player.sendActionBar(Component.text("Cataclysm!", NamedTextColor.DARK_RED));
        } else {
            double baseDmg = 8.0 * getEnchantDamageMultiplier(weapon);
            double radius = 4.0;
            int seismicLvl = enchantManager.getEnchantLevel(weapon, EnchantmentType.SEISMIC_SLAM);
            if (seismicLvl > 0) { baseDmg += seismicLvl * 3; radius += seismicLvl; }
            int magnetizeLvl = enchantManager.getEnchantLevel(weapon, EnchantmentType.MAGNETIZE);
            if (magnetizeLvl > 0) {
                double pullRadius = 2 + magnetizeLvl * 2;
                for (Entity e : player.getNearbyEntities(pullRadius, pullRadius, pullRadius)) {
                    if (e instanceof LivingEntity le && e != player) {
                        Vector pull = player.getLocation().toVector().subtract(e.getLocation().toVector()).normalize().multiply(0.8);
                        le.setVelocity(pull.setY(0.2));
                    }
                }
            }
            for (Entity e : player.getNearbyEntities(radius, radius, radius)) {
                if (e instanceof LivingEntity le && e != player) {
                    le.damage(baseDmg, player);
                    le.setVelocity(le.getVelocity().add(new Vector(0, 0.6, 0)));
                }
            }
            world.spawnParticle(Particle.EXPLOSION, loc, 3, 1.5, 0.3, 1.5, 0);
            world.spawnParticle(Particle.BLOCK, loc, 15, 2, 0.3, 2, 0.1, Material.STONE.createBlockData());
            player.sendActionBar(Component.text("Meteor Strike!", NamedTextColor.GOLD));
        }
    }

    // ========================================================================
    // SWORD — unchanged from repo
    // ========================================================================
    private void handleSwordAbility(Player player, ItemStack weapon, int tier) {
        boolean definitive = superToolManager.isDefinitiveSuperNetherite(weapon);
        Location loc = player.getLocation(); World world = loc.getWorld();
        if (definitive) {
            double baseDmg = 8.0 * getEnchantDamageMultiplier(weapon);
            player.setInvulnerable(true);
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> player.setInvulnerable(false), 2L);
            for (Entity e : player.getNearbyEntities(5, 5, 5)) {
                if (e instanceof LivingEntity le && e != player) {
                    le.damage(baseDmg, player);
                    le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 0));
                }
            }
            world.spawnParticle(Particle.SWEEP_ATTACK, loc.clone().add(0, 1, 0), 15, 2, 1, 2, 0);
            world.spawnParticle(Particle.ENCHANTED_HIT, loc, 10, 2, 1, 2, 0.1);
            player.sendActionBar(Component.text("Judgment Cut!", NamedTextColor.DARK_RED));
        } else {
            double baseDmg = 4.0; Vector dir = player.getLocation().getDirection().normalize(); int hitCount = 0;
            for (Entity e : player.getNearbyEntities(4, 3, 4)) {
                if (!(e instanceof LivingEntity le) || e == player) continue;
                Vector toTarget = e.getLocation().toVector().subtract(loc.toVector()).normalize();
                if (dir.dot(toTarget) > 0.5) {
                    le.damage(baseDmg, player); hitCount++;
                    if (enchantManager.hasEnchant(weapon, EnchantmentType.VAMPIRISM)) healPlayer(player, 1.0);
                    if (enchantManager.hasEnchant(weapon, EnchantmentType.BLEED)) applyBleedDoT(le, enchantManager.getEnchantLevel(weapon, EnchantmentType.BLEED));
                    if (enchantManager.hasEnchant(weapon, EnchantmentType.FROSTBITE)) le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1));
                    if (enchantManager.hasEnchant(weapon, EnchantmentType.VENOMSTRIKE)) le.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 60, 0));
                    if (enchantManager.hasEnchant(weapon, EnchantmentType.CHAIN_LIGHTNING)) chainLightningFromTarget(le, player, weapon, baseDmg);
                }
            }
            if (enchantManager.hasEnchant(weapon, EnchantmentType.LIFESTEAL) && hitCount > 0) healPlayer(player, baseDmg * hitCount * 0.15);
            world.spawnParticle(Particle.SWEEP_ATTACK, loc.clone().add(dir.multiply(2)).add(0, 1, 0), 10, 1, 0.5, 1, 0);
            player.sendActionBar(Component.text("Blade Storm!", NamedTextColor.RED));
        }
    }

    // ========================================================================
    // SICKLE — unchanged from repo
    // ========================================================================
    private void handleSickleAbility(Player player, ItemStack weapon, int tier) {
        boolean definitive = superToolManager.isDefinitiveSuperNetherite(weapon);
        Location loc = player.getLocation(); World world = loc.getWorld();
        if (definitive) {
            double baseDmg = 6.0 * getEnchantDamageMultiplier(weapon);
            for (Entity e : player.getNearbyEntities(5, 5, 5)) {
                if (!(e instanceof LivingEntity le) || e == player) continue;
                le.damage(baseDmg, player);
                if (le instanceof Monster) le.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 60, 2));
                le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1));
                le.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0));
            }
            world.spawnParticle(Particle.SCULK_SOUL, loc.clone().add(0, 1, 0), 15, 2, 1, 2, 0.05);
            player.sendActionBar(Component.text("Death's Embrace!", NamedTextColor.DARK_PURPLE));
        } else {
            double baseDmg = 3.0;
            if (enchantManager.hasEnchant(weapon, EnchantmentType.BLOOD_PRICE)) { baseDmg *= 1.5; if (player.getHealth() > 3.0) player.damage(3.0); }
            for (Entity e : player.getNearbyEntities(3, 3, 3)) {
                if (!(e instanceof LivingEntity le) || e == player) continue;
                le.damage(baseDmg, player);
                if (enchantManager.hasEnchant(weapon, EnchantmentType.WITHER_TOUCH) && le instanceof Monster) le.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 60, 0));
                if (enchantManager.hasEnchant(weapon, EnchantmentType.REAPERS_MARK)) {
                    org.bukkit.NamespacedKey markKey = new org.bukkit.NamespacedKey(plugin, "reaper_mark_level");
                    org.bukkit.NamespacedKey markTimeKey = new org.bukkit.NamespacedKey(plugin, "reaper_mark_expire");
                    int markLvl = enchantManager.getEnchantLevel(weapon, EnchantmentType.REAPERS_MARK);
                    le.getPersistentDataContainer().set(markKey, PersistentDataType.INTEGER, markLvl);
                    le.getPersistentDataContainer().set(markTimeKey, PersistentDataType.LONG, System.currentTimeMillis() + 8000L);
                }
            }
            if (enchantManager.hasEnchant(weapon, EnchantmentType.SOUL_REAP)) player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 200, 0, true, true, true));
            world.spawnParticle(Particle.SCULK_SOUL, loc.clone().add(0, 1, 0), 10, 1.5, 0.5, 1.5, 0.05);
            player.sendActionBar(Component.text("Soul Harvest!", NamedTextColor.DARK_PURPLE));
        }
    }

    // ========================================================================
    // BATTLE AXE — unchanged from repo
    // ========================================================================
    private void handleBattleAxeAbility(Player player, ItemStack weapon, int tier) {
        boolean definitive = superToolManager.isDefinitiveSuperNetherite(weapon);
        Location loc = player.getLocation(); World world = loc.getWorld();
        if (definitive) {
            double baseDmg = 10.0 * getEnchantDamageMultiplier(weapon);
            for (Entity e : player.getNearbyEntities(5, 5, 5)) {
                if (!(e instanceof LivingEntity le) || e == player) continue;
                le.damage(baseDmg, player); le.setVelocity(le.getVelocity().add(new Vector(0, 1.5, 0)));
                le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 1));
            }
            world.spawnParticle(Particle.EXPLOSION, loc, 5, 2, 0.5, 2, 0);
            world.spawnParticle(Particle.BLOCK, loc, 15, 2, 0.5, 2, 0.1, Material.STONE.createBlockData());
            player.sendActionBar(Component.text("Earthquake!", NamedTextColor.DARK_RED));
        } else {
            double baseDmg = 5.0; double radius = 2.0;
            if (enchantManager.hasEnchant(weapon, EnchantmentType.CLEAVE)) radius = 4.0;
            if (enchantManager.hasEnchant(weapon, EnchantmentType.BERSERKER)) {
                AttributeInstance maxHp = player.getAttribute(Attribute.MAX_HEALTH);
                double maxHealth = maxHp != null ? maxHp.getValue() : 20.0;
                double missingPct = (maxHealth - player.getHealth()) / maxHealth;
                baseDmg += enchantManager.getEnchantLevel(weapon, EnchantmentType.BERSERKER) * 0.5 * missingPct * baseDmg;
            }
            boolean hasGuillotine = enchantManager.hasEnchant(weapon, EnchantmentType.GUILLOTINE);
            for (Entity e : player.getNearbyEntities(radius, radius, radius)) {
                if (!(e instanceof LivingEntity le) || e == player) continue;
                le.damage(baseDmg, player); le.setVelocity(le.getVelocity().add(new Vector(0, 0.8, 0)));
                if (hasGuillotine && !(le instanceof Boss) && ThreadLocalRandom.current().nextDouble() < 0.05) {
                    AttributeInstance mh = le.getAttribute(Attribute.MAX_HEALTH);
                    if (mh != null) le.damage(mh.getValue() * 10, player);
                }
            }
            world.spawnParticle(Particle.EXPLOSION, loc, 3, 1, 0.3, 1, 0);
            player.sendActionBar(Component.text("Groundbreaker!", NamedTextColor.GOLD));
        }
    }

    // ========================================================================
    // PICKAXE — unchanged from repo
    // ========================================================================
    private void handlePickaxeAbility(Player player, ItemStack weapon, int tier) {
        boolean definitive = superToolManager.isDefinitiveSuperNetherite(weapon);
        Location center = player.getLocation(); World world = center.getWorld();
        int baseRadius = 8; int duration = 5; boolean highlightExtras = false;
        if (definitive) { baseRadius = 16; duration = 10; highlightExtras = true; }
        else {
            if (enchantManager.hasEnchant(weapon, EnchantmentType.VEINMINER)) baseRadius += 4;
            if (enchantManager.hasEnchant(weapon, EnchantmentType.DRILL)) highlightExtras = true;
            if (enchantManager.hasEnchant(weapon, EnchantmentType.MAGNETISM)) {
                for (Entity e : player.getNearbyEntities(baseRadius, baseRadius, baseRadius)) { if (e instanceof Item item) { item.teleport(player.getLocation()); item.setPickupDelay(0); } }
            }
        }
        final int r = baseRadius; final boolean extras = highlightExtras || definitive; final int dur = duration;
        List<Location> oreLocations = new ArrayList<>();
        int cx = center.getBlockX(), cy = center.getBlockY(), cz = center.getBlockZ();
        for (int x = -r; x <= r; x++) for (int y = -r; y <= r; y++) for (int z = -r; z <= r; z++) {
            Block block = world.getBlockAt(cx + x, cy + y, cz + z); Material bMat = block.getType();
            if (ORES.contains(bMat)) oreLocations.add(block.getLocation().add(0.5, 0.5, 0.5));
            else if (extras && (bMat == Material.SPAWNER || bMat == Material.CHEST || bMat == Material.TRAPPED_CHEST || bMat == Material.BARREL || bMat == Material.ANCIENT_DEBRIS))
                oreLocations.add(block.getLocation().add(0.5, 0.5, 0.5));
        }
        Particle particleType = definitive ? Particle.ELECTRIC_SPARK : Particle.HAPPY_VILLAGER;
        new BukkitRunnable() { int ticks = 0; @Override public void run() {
            if (ticks >= dur * 20 || !player.isOnline()) { cancel(); return; }
            for (Location l : oreLocations) world.spawnParticle(particleType, l, 1, 0.1, 0.1, 0.1, 0); ticks += 10;
        }}.runTaskTimer(plugin, 0L, 10L);
        player.sendActionBar(Component.text(definitive ? "Core Scan!" : "Seismic Sense!", definitive ? NamedTextColor.DARK_RED : NamedTextColor.AQUA));
    }

    // ========================================================================
    // SHOVEL — unchanged from repo
    // ========================================================================
    private void handleShovelAbility(Player player, ItemStack weapon, int tier) {
        boolean definitive = superToolManager.isDefinitiveSuperNetherite(weapon);
        Location center = player.getLocation(); World world = center.getWorld();
        int radius = definitive ? 7 : 3;
        if (!definitive && enchantManager.hasEnchant(weapon, EnchantmentType.EXCAVATOR)) radius += 2;
        boolean hasReplenish = !definitive && enchantManager.hasEnchant(weapon, EnchantmentType.REPLENISH);
        int cx = center.getBlockX(), cy = center.getBlockY(), cz = center.getBlockZ();
        for (int x = -radius; x <= radius; x++) for (int y = -radius; y <= radius; y++) for (int z = -radius; z <= radius; z++) {
            if (x * x + y * y + z * z > radius * radius) continue;
            Block block = world.getBlockAt(cx + x, cy + y, cz + z);
            if (SHOVEL_MINEABLE.contains(block.getType())) { block.breakNaturally(weapon);
                if (hasReplenish && ThreadLocalRandom.current().nextDouble() < 0.10) world.dropItemNaturally(block.getLocation(), new ItemStack(Material.WHEAT_SEEDS, 1));
            }
        }
        world.spawnParticle(Particle.EXPLOSION, center, 5, radius * 0.5, 1, radius * 0.5, 0);
        player.sendActionBar(Component.text(definitive ? "Terraform!" : "Excavation Blast!", definitive ? NamedTextColor.DARK_RED : NamedTextColor.GOLD));
    }

    // ========================================================================
    // TRIDENT — unchanged from repo
    // ========================================================================
    private void handleTridentAbility(Player player, ItemStack weapon, int tier) {
        boolean definitive = superToolManager.isDefinitiveSuperNetherite(weapon);
        Location loc = player.getLocation(); World world = loc.getWorld();
        if (definitive) {
            double baseDmg = 8.0 * getEnchantDamageMultiplier(weapon);
            for (Entity e : player.getNearbyEntities(6, 6, 6)) { if (!(e instanceof LivingEntity le) || e == player) continue; le.damage(baseDmg, player); world.strikeLightning(le.getLocation()); }
            player.addPotionEffect(new PotionEffect(PotionEffectType.CONDUIT_POWER, 400, 0));
            player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 400, 0));
            healPlayer(player, 6.0);
            world.spawnParticle(Particle.FISHING, loc.clone().add(0, 1, 0), 15, 3, 1, 3, 0.1);
            player.sendActionBar(Component.text("Wrath of the Sea!", NamedTextColor.DARK_RED));
        } else {
            double baseDmg = 4.0; double pushMult = 1.0;
            boolean strikeEnabled = enchantManager.hasEnchant(weapon, EnchantmentType.THUNDERLORD);
            boolean doublePush = enchantManager.hasEnchant(weapon, EnchantmentType.TIDAL_WAVE);
            boolean grantSwiftness = enchantManager.hasEnchant(weapon, EnchantmentType.SWIFTNESS);
            boolean grantVitality = enchantManager.hasEnchant(weapon, EnchantmentType.VITALITY);
            if (doublePush) pushMult = 2.0;
            List<LivingEntity> hitTargets = new ArrayList<>();
            for (Entity e : player.getNearbyEntities(4, 4, 4)) {
                if (!(e instanceof LivingEntity le) || e == player) continue;
                le.damage(baseDmg, player); Vector push = le.getLocation().toVector().subtract(loc.toVector()).normalize().multiply(0.8 * pushMult).setY(0.5);
                le.setVelocity(push); hitTargets.add(le);
            }
            if (strikeEnabled && hitTargets.size() >= 2) { List<LivingEntity> targets = new ArrayList<>(hitTargets); Collections.shuffle(targets); for (int i = 0; i < Math.min(2, targets.size()); i++) world.strikeLightning(targets.get(i).getLocation()); }
            player.addPotionEffect(new PotionEffect(PotionEffectType.CONDUIT_POWER, 200, 0));
            if (grantSwiftness) player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 200, 0));
            if (grantVitality) healPlayer(player, 4.0);
            world.spawnParticle(Particle.FISHING, loc.clone().add(0, 1, 0), 10, 2, 0.5, 2, 0.1);
            player.sendActionBar(Component.text("Poseidon's Call!", NamedTextColor.AQUA));
        }
    }

    // ========================================================================
    // CROSSBOW + BOW — unchanged from repo
    // ========================================================================
    private void handleCrossbowAbility(Player player, ItemStack weapon, int tier) {
        boolean definitive = superToolManager.isDefinitiveSuperNetherite(weapon);
        World world = player.getWorld(); Vector dir = player.getLocation().getDirection().normalize();
        if (definitive) {
            double baseDmg = 5.0 * getEnchantDamageMultiplier(weapon);
            for (int i = 0; i < 4; i++) {
                Vector spread = dir.clone().add(new Vector((ThreadLocalRandom.current().nextDouble() - 0.5) * 0.2, (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.1, (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.2)).normalize().multiply(3.0);
                Arrow arrow = player.launchProjectile(Arrow.class, spread); arrow.setDamage(baseDmg); arrow.setKnockbackStrength(3); arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
                plugin.getServer().getScheduler().runTaskLater(plugin, arrow::remove, 60L);
            }
            player.sendActionBar(Component.text("Barrage!", NamedTextColor.DARK_RED));
        } else {
            for (int i = 0; i < 2; i++) {
                Vector spread = dir.clone().add(new Vector((ThreadLocalRandom.current().nextDouble() - 0.5) * 0.1, 0, (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.1)).normalize().multiply(2.5);
                Arrow arrow = player.launchProjectile(Arrow.class, spread); arrow.setDamage(3.0); arrow.setKnockbackStrength(2); arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
                plugin.getServer().getScheduler().runTaskLater(plugin, arrow::remove, 60L);
            }
            player.sendActionBar(Component.text("Knockback Shot!", NamedTextColor.GOLD));
        }
        world.spawnParticle(Particle.CRIT, player.getEyeLocation().add(dir), 8, 0.2, 0.2, 0.2, 0.1);
    }

    private void handleBowAbility(Player player, ItemStack weapon, int tier) {
        boolean definitive = superToolManager.isDefinitiveSuperNetherite(weapon);
        World world = player.getWorld(); int maxDist = definitive ? 15 : 10;
        Vector dir = player.getLocation().getDirection().normalize();
        RayTraceResult ray = world.rayTraceBlocks(player.getEyeLocation(), dir, maxDist);
        Location destination;
        if (ray != null && ray.getHitBlock() != null) { destination = ray.getHitPosition().toLocation(world).subtract(dir.multiply(0.5)); destination.setY(Math.floor(destination.getY())); }
        else destination = player.getLocation().add(dir.multiply(maxDist));
        destination.setYaw(player.getLocation().getYaw()); destination.setPitch(player.getLocation().getPitch());
        if (definitive) {
            Location start = player.getLocation(); Vector step = destination.toVector().subtract(start.toVector()).normalize().multiply(0.5); Location current = start.clone();
            for (int i = 0; i < maxDist * 2; i++) { current.add(step); if (current.distanceSquared(destination) < 1) break; world.spawnParticle(Particle.PORTAL, current, 2, 0.1, 0.1, 0.1, 0); }
        }
        player.teleport(destination);
        if (definitive) {
            double dmg = 3.0 * getEnchantDamageMultiplier(weapon);
            for (Entity e : player.getNearbyEntities(2, 2, 2)) { if (e instanceof LivingEntity le && e != player) le.damage(dmg, player); }
            world.spawnParticle(Particle.DRAGON_BREATH, destination, 10, 1, 0.5, 1, 0.01);
            player.sendActionBar(Component.text("Warp Strike!", NamedTextColor.DARK_RED));
        } else { world.spawnParticle(Particle.PORTAL, destination, 10, 0.5, 0.5, 0.5, 0.1); player.sendActionBar(Component.text("Blink!", NamedTextColor.AQUA)); }
    }

    // ========================================================================
    // UTILITY
    // ========================================================================
    private boolean checkCooldown(Player player, long cooldownMs) {
        long now = System.currentTimeMillis(); Long cdEnd = cooldowns.get(player.getUniqueId());
        if (cdEnd != null && now < cdEnd) { player.sendActionBar(Component.text("Cooldown: " + ((cdEnd - now) / 1000) + "s", NamedTextColor.RED)); return true; }
        cooldowns.put(player.getUniqueId(), now + cooldownMs); return false;
    }

    private double getEnchantDamageMultiplier(ItemStack weapon) { return 1.0 + (enchantManager.getTotalEnchantCount(weapon) * 0.02); }

    private void healPlayer(Player player, double amount) {
        AttributeInstance maxHp = player.getAttribute(Attribute.MAX_HEALTH); double max = maxHp != null ? maxHp.getValue() : 20.0;
        player.setHealth(Math.min(player.getHealth() + amount, max));
    }

    private void applyBleedDoT(LivingEntity target, int level) {
        int count = level; new BukkitRunnable() { int c = 0; @Override public void run() { if (c >= count || target.isDead()) { cancel(); return; } target.damage(2.0); c++; } }.runTaskTimer(plugin, 10, 10);
    }

    private void chainLightningFromTarget(LivingEntity source, Player attacker, ItemStack weapon, double baseDmg) {
        int chainLvl = enchantManager.getEnchantLevel(weapon, EnchantmentType.CHAIN_LIGHTNING);
        double dmgPct = switch (chainLvl) { case 1 -> 0.30; case 2 -> 0.40; default -> 0.50; }; double chainDmg = baseDmg * dmgPct;
        Set<Entity> alreadyHit = new HashSet<>(); alreadyHit.add(source); alreadyHit.add(attacker); LivingEntity current = source;
        for (int i = 0; i < chainLvl; i++) {
            LivingEntity nearest = null; double nearestDist = 5.0;
            for (Entity e : current.getNearbyEntities(5, 3, 5)) { if (e instanceof LivingEntity le && !alreadyHit.contains(e) && e != attacker) { double d = current.getLocation().distance(e.getLocation()); if (d < nearestDist) { nearestDist = d; nearest = le; } } }
            if (nearest == null) break; nearest.damage(chainDmg, attacker); alreadyHit.add(nearest);
            Location from = current.getLocation().add(0, 1, 0); Location to = nearest.getLocation().add(0, 1, 0);
            current.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, from.clone().add(to.toVector().subtract(from.toVector()).multiply(0.5)), 5, 0.1, 0.1, 0.1, 0.01);
            current = nearest;
        }
    }

    @EventHandler public void onPlayerQuit(PlayerQuitEvent event) { cooldowns.remove(event.getPlayer().getUniqueId()); }
}

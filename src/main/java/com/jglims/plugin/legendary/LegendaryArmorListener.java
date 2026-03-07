package com.jglims.plugin.legendary;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.jglims.plugin.JGlimsPlugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class LegendaryArmorListener implements Listener {

    private final JGlimsPlugin plugin;
    private final LegendaryArmorManager armorManager;
    private final Random random = new Random();

    private final Map<UUID, Integer> bloodMoonBonusHP = new HashMap<>();
    private final Map<UUID, Long> voidWalkerTeleportCooldown = new HashMap<>();
    private final Map<UUID, Long> dragonKnightDoubleJumpCD = new HashMap<>();
    private final Map<UUID, Long> dragonKnightRoarCooldown = new HashMap<>();
    private final Map<UUID, Long> frostWardenLastFreeze = new HashMap<>();
    private final Map<UUID, Boolean> shadowStalkerInvisible = new HashMap<>();

    public LegendaryArmorListener(JGlimsPlugin plugin, LegendaryArmorManager armorManager) {
        this.plugin = plugin;
        this.armorManager = armorManager;
        startPassiveEffectTask();
    }

    private void startPassiveEffectTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    if (player.getGameMode() == GameMode.SPECTATOR) continue;
                    applyPassiveEffects(player);
                }
            }
        }.runTaskTimer(plugin, 40L, 20L);
    }

    private void applyPassiveEffects(Player player) {
        applyHelmetPassives(player);
        applyChestplatePassives(player);
        applyLeggingsPassives(player);
        applyBootsPassives(player);
        LegendaryArmorSet fullSet = armorManager.getActiveFullSet(player);
        if (fullSet != null) applySetBonus(player, fullSet);
    }

    // ===== HELMET PASSIVES =====

    private void applyHelmetPassives(Player player) {
        LegendaryArmorSet set = armorManager.identifySet(player.getInventory().getHelmet());
        if (set == null) return;
        switch (set) {
            case REINFORCED_LEATHER -> {
                for (Entity e : player.getNearbyEntities(8, 8, 8)) {
                    if (e instanceof Monster mob) {
                        mob.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 30, 0, true, false, false));
                    }
                }
            }
            case AMETHYST_ARMOR -> {
                player.removePotionEffect(PotionEffectType.BLINDNESS);
                player.removePotionEffect(PotionEffectType.DARKNESS);
            }
            case BONE_ARMOR -> {
                player.removePotionEffect(PotionEffectType.WITHER);
            }
            case SCULK_ARMOR -> {
                if (player.getLocation().getY() < 50) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 80, 0, true, false, false));
                }
            }
            case SHADOW_STALKER -> {
                if (player.isSneaking()) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 40, 0, true, false, false));
                }
            }
            case BLOOD_MOON -> {
                World world = player.getWorld();
                if (world.getEnvironment() == World.Environment.NORMAL && world.getTime() >= 13000 && world.getTime() <= 23000) {
                    for (Entity entity : player.getNearbyEntities(40, 40, 40)) {
                        if (entity instanceof Monster mob) {
                            mob.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 30, 0, true, false, false));
                        }
                    }
                }
            }
            case NATURES_EMBRACE -> {
                player.removePotionEffect(PotionEffectType.POISON);
                World world = player.getWorld();
                if (world.getEnvironment() == World.Environment.NORMAL
                        && world.getTime() < 13000
                        && player.getLocation().getBlock().getLightFromSky() >= 12) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 40, 0, true, false, false));
                }
            }
            case FROST_WARDEN -> {
                player.removePotionEffect(PotionEffectType.SLOWNESS);
                player.setFreezeTicks(0);
            }
            case VOID_WALKER -> {
                player.removePotionEffect(PotionEffectType.BLINDNESS);
                player.removePotionEffect(PotionEffectType.DARKNESS);
                for (Entity entity : player.getNearbyEntities(20, 20, 20)) {
                    if (entity instanceof LivingEntity le && le.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                        le.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 30, 0, true, false, false));
                    }
                }
            }
            case DRAGON_KNIGHT -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 40, 2, true, false, false));
                for (Entity e : player.getNearbyEntities(20, 20, 20)) {
                    if (e instanceof LivingEntity le && !(le instanceof Player)) {
                        le.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 30, 0, true, false, false));
                    }
                }
            }
            case ABYSSAL_PLATE -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 400, 0, true, false, false));
                player.removePotionEffect(PotionEffectType.DARKNESS);
                player.removePotionEffect(PotionEffectType.BLINDNESS);
                player.removePotionEffect(PotionEffectType.WITHER);
            }
            default -> {}
        }
    }

    // ===== CHESTPLATE PASSIVES =====

    private void applyChestplatePassives(Player player) {
        LegendaryArmorSet set = armorManager.identifySet(player.getInventory().getChestplate());
        if (set == null) return;
        switch (set) {
            case AMETHYST_ARMOR -> {
                AttributeInstance maxHpAttr = player.getAttribute(Attribute.MAX_HEALTH);
                double maxHp = maxHpAttr != null ? maxHpAttr.getValue() : 20.0;
                if (player.getHealth() < maxHp * 0.3) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 40, 0, true, false, false));
                }
            }
            case FROST_WARDEN -> {
                for (Entity entity : player.getNearbyEntities(4, 4, 4)) {
                    if (entity instanceof Monster mob) {
                        mob.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1, true, false, false));
                        mob.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 40, 0, true, false, false));
                    }
                }
            }
            case DRAGON_KNIGHT -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 400, 0, true, false, false));
            }
            case ABYSSAL_PLATE -> {
                player.removePotionEffect(PotionEffectType.WITHER);
            }
            default -> {}
        }
    }

    // ===== LEGGINGS PASSIVES =====

    private void applyLeggingsPassives(Player player) {
        LegendaryArmorSet set = armorManager.identifySet(player.getInventory().getLeggings());
        if (set == null) return;
        switch (set) {
            case REINFORCED_LEATHER -> {
                if (player.isInWater()) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 40, 0, true, false, false));
                }
            }
            case COPPER_ARMOR -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 40, 0, true, false, false));
            }
            case AMETHYST_ARMOR -> {
                for (Entity e : player.getNearbyEntities(6, 6, 6)) {
                    if (e instanceof Monster mob) {
                        mob.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 30, 0, true, false, false));
                    }
                }
            }
            case BONE_ARMOR -> {
                World world = player.getWorld();
                if (world.getEnvironment() == World.Environment.NORMAL && world.getTime() >= 13000 && world.getTime() <= 23000) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 40, 0, true, false, false));
                }
            }
            case SCULK_ARMOR -> {
                player.removePotionEffect(PotionEffectType.DARKNESS);
                String biome = player.getLocation().getBlock().getBiome().name().toLowerCase();
                if (biome.contains("deep_dark")) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 0, true, false, false));
                }
            }
            case SHADOW_STALKER -> {
                if (player.isSneaking()) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 0, true, false, false));
                }
            }
            case BLOOD_MOON -> {
                World world = player.getWorld();
                if (world.getEnvironment() == World.Environment.NORMAL && world.getTime() >= 13000 && world.getTime() <= 23000) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 1, true, false, false));
                }
                AttributeInstance maxHpAttr = player.getAttribute(Attribute.MAX_HEALTH);
                double maxHp = maxHpAttr != null ? maxHpAttr.getValue() : 20.0;
                if (player.getHealth() < maxHp * 0.5) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 40, 0, true, false, false));
                }
            }
            case NATURES_EMBRACE -> {
                String biome = player.getLocation().getBlock().getBiome().name().toLowerCase();
                if (biome.contains("forest") || biome.contains("grove") || biome.contains("taiga")) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 40, 0, true, false, false));
                }
            }
            case FROST_WARDEN -> {
                Material below = player.getLocation().clone().add(0, -1, 0).getBlock().getType();
                if (below == Material.ICE || below == Material.PACKED_ICE || below == Material.BLUE_ICE
                        || below == Material.SNOW_BLOCK || below == Material.POWDER_SNOW) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 1, true, false, false));
                }
            }
            case VOID_WALKER -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 40, 0, true, false, false));
            }
            case ABYSSAL_PLATE -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 1, true, false, false));
            }
            default -> {}
        }
    }

    // ===== BOOTS PASSIVES =====

    private void applyBootsPassives(Player player) {
        LegendaryArmorSet set = armorManager.identifySet(player.getInventory().getBoots());
        if (set == null) return;
        switch (set) {
            case COPPER_ARMOR -> {
                player.removePotionEffect(PotionEffectType.SLOWNESS);
            }
            case CHAINMAIL_REINFORCED -> {
                Material below = player.getLocation().clone().add(0, -1, 0).getBlock().getType();
                if (below == Material.SOUL_SAND || below == Material.SOUL_SOIL) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 0, true, false, false));
                }
            }
            case AMETHYST_ARMOR -> {
                player.getWorld().spawnParticle(Particle.DUST, player.getLocation(),
                        2, 0.2, 0, 0.2, 0, new Particle.DustOptions(org.bukkit.Color.fromRGB(160, 50, 255), 0.8f));
            }
            case BONE_ARMOR -> {
                player.removePotionEffect(PotionEffectType.SLOWNESS);
            }
            case FROST_WARDEN -> {
                player.setFireTicks(0);
                player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 40, 0, true, false, false));
            }
            case ABYSSAL_PLATE -> {
                player.setFireTicks(0);
                player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 400, 0, true, false, false));
            }
            default -> {}
        }
    }

    // ===== SET BONUSES (full 4/4) =====

    private void applySetBonus(Player player, LegendaryArmorSet set) {
        switch (set) {
            case REINFORCED_LEATHER -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 0, true, false, false));
            }
            case COPPER_ARMOR -> {
                player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, player.getLocation().add(0, 2.2, 0),
                        1, 0.1, 0.1, 0.1, 0);
            }
            case BONE_ARMOR -> {
                for (Entity e : player.getNearbyEntities(12, 12, 12)) {
                    if (e instanceof Monster mob) {
                        String type = mob.getType().name().toLowerCase();
                        if (type.contains("zombie") || type.contains("skeleton") || type.contains("wither")
                                || type.contains("phantom") || type.contains("drowned") || type.contains("stray")
                                || type.contains("husk") || type.contains("zoglin")) {
                            if (mob.getTarget() == player) {
                                mob.setTarget(null);
                            }
                        }
                    }
                }
            }
            case SCULK_ARMOR -> {
                for (Entity e : player.getNearbyEntities(12, 12, 12)) {
                    if (e instanceof LivingEntity le && !(le instanceof Player)) {
                        le.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 30, 0, true, false, false));
                    }
                }
            }
            case NATURES_EMBRACE -> {
                String biomeName = player.getLocation().getBlock().getBiome().name().toLowerCase();
                if (biomeName.contains("forest") || biomeName.contains("grove") || biomeName.contains("taiga")) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 40, 1, true, false, false));
                }
            }
            case FROST_WARDEN -> {
                UUID uid = player.getUniqueId();
                long now = System.currentTimeMillis();
                if (now - frostWardenLastFreeze.getOrDefault(uid, 0L) >= 2000) {
                    frostWardenLastFreeze.put(uid, now);
                    for (Entity entity : player.getNearbyEntities(5, 5, 5)) {
                        if (entity instanceof Monster mob) {
                            mob.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 2, true, false, false));
                            mob.setFreezeTicks(80);
                            mob.getWorld().spawnParticle(Particle.SNOWFLAKE, mob.getLocation().add(0, 1, 0), 12, 0.3, 0.5, 0.3, 0.02);
                        }
                    }
                    player.getWorld().spawnParticle(Particle.SNOWFLAKE, player.getLocation().add(0, 1, 0), 20, 2.0, 0.5, 2.0, 0.01);
                }
            }
            case DRAGON_KNIGHT -> {
                player.getWorld().spawnParticle(Particle.DRAGON_BREATH, player.getLocation().add(0, 0.5, 0),
                        3, 0.5, 0.2, 0.5, 0.01);
            }
            case ABYSSAL_PLATE -> {
                player.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, player.getLocation().add(0, 0.5, 0),
                        3, 0.5, 0.2, 0.5, 0.01);
            }
            default -> {}
        }
    }

    // ===== DAMAGE DEALT EVENT =====

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Attacker is Player
        if (event.getDamager() instanceof Player attacker && event.getEntity() instanceof LivingEntity target) {
            LegendaryArmorSet fullSet = armorManager.getActiveFullSet(attacker);

            // Shadow Stalker: +50% sneak damage (full set)
            if (fullSet == LegendaryArmorSet.SHADOW_STALKER && attacker.isSneaking()) {
                event.setDamage(event.getDamage() * 1.50);
                attacker.getWorld().spawnParticle(Particle.SMOKE, target.getLocation().add(0, 1, 0), 15, 0.3, 0.5, 0.3, 0.02);
            }
            // Shadow Stalker chestplate: +4 flat sneak damage
            if (attacker.isSneaking() && armorManager.isWearingPiece(attacker, LegendaryArmorSet.SHADOW_STALKER, LegendaryArmorSet.ArmorSlot.CHESTPLATE)) {
                event.setDamage(event.getDamage() + 4.0);
            }

            // Dragon Knight: +35% vs dragon/ender mobs, +15% always
            if (fullSet == LegendaryArmorSet.DRAGON_KNIGHT) {
                String targetType = target.getType().name().toLowerCase();
                if (targetType.contains("dragon") || targetType.contains("ender") || targetType.contains("enderman")
                        || targetType.contains("shulker") || targetType.contains("endermite")) {
                    event.setDamage(event.getDamage() * 1.35);
                    attacker.getWorld().spawnParticle(Particle.DRAGON_BREATH, target.getLocation().add(0, 1, 0), 20, 0.4, 0.6, 0.4, 0.05);
                }
                event.setDamage(event.getDamage() * 1.15);
            }

            // Abyssal Plate full set: +40% melee damage
            if (fullSet == LegendaryArmorSet.ABYSSAL_PLATE) {
                event.setDamage(event.getDamage() * 1.40);
                attacker.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, target.getLocation().add(0, 1, 0), 12, 0.3, 0.5, 0.3, 0.03);
            }

            // Abyssal Plate leggings only (no full set): +20%
            if (fullSet != LegendaryArmorSet.ABYSSAL_PLATE
                    && armorManager.isWearingPiece(attacker, LegendaryArmorSet.ABYSSAL_PLATE, LegendaryArmorSet.ArmorSlot.LEGGINGS)) {
                event.setDamage(event.getDamage() * 1.20);
            }

            // Blood Moon: 8% lifesteal
            if (fullSet == LegendaryArmorSet.BLOOD_MOON) {
                double healed = event.getFinalDamage() * 0.08;
                AttributeInstance maxHpAttr = attacker.getAttribute(Attribute.MAX_HEALTH);
                double maxHp = maxHpAttr != null ? maxHpAttr.getValue() : 20.0;
                attacker.setHealth(Math.min(attacker.getHealth() + healed, maxHp));
            }

            // Blood Moon chestplate: +4 HP per kill (max 20 extra)
            if (target.getHealth() - event.getFinalDamage() <= 0) {
                if (armorManager.isWearingPiece(attacker, LegendaryArmorSet.BLOOD_MOON, LegendaryArmorSet.ArmorSlot.CHESTPLATE)) {
                    UUID uid = attacker.getUniqueId();
                    int currentBonus = bloodMoonBonusHP.getOrDefault(uid, 0);
                    if (currentBonus < 20) {
                        bloodMoonBonusHP.put(uid, currentBonus + 4);
                        AttributeInstance maxHpAttr = attacker.getAttribute(Attribute.MAX_HEALTH);
                        if (maxHpAttr != null) {
                            maxHpAttr.setBaseValue(20.0 + bloodMoonBonusHP.get(uid));
                        }
                        double maxHp = maxHpAttr != null ? maxHpAttr.getValue() : 20.0;
                        attacker.setHealth(Math.min(attacker.getHealth() + 2.0, maxHp));
                        attacker.sendActionBar(Component.text("Blood Moon: +" + bloodMoonBonusHP.get(uid) + " bonus HP",
                                TextColor.color(170, 0, 0)).decorate(TextDecoration.BOLD));
                        attacker.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, attacker.getLocation().add(0, 1, 0),
                                8, 0.3, 0.5, 0.3, 0.02);
                    }
                }
            }
        }

        // Victim is Player (retaliatory effects)
        if (event.getEntity() instanceof Player victim && event.getDamager() instanceof LivingEntity attacker) {
            // Abyssal Plate chestplate: soul-fire thorns 12 dmg
            if (armorManager.isWearingPiece(victim, LegendaryArmorSet.ABYSSAL_PLATE, LegendaryArmorSet.ArmorSlot.CHESTPLATE)) {
                attacker.damage(12.0, victim);
                attacker.setFireTicks(100);
                attacker.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, attacker.getLocation().add(0, 1, 0), 20, 0.4, 0.6, 0.4, 0.05);
            }

            // Nature's Embrace chestplate: thorns 4
            if (armorManager.isWearingPiece(victim, LegendaryArmorSet.NATURES_EMBRACE, LegendaryArmorSet.ArmorSlot.CHESTPLATE)) {
                attacker.damage(4.0, victim);
                attacker.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, attacker.getLocation().add(0, 1, 0), 12, 0.3, 0.5, 0.3, 0);
            }

            // Copper Armor helmet: static shock 1 dmg
            if (armorManager.isWearingPiece(victim, LegendaryArmorSet.COPPER_ARMOR, LegendaryArmorSet.ArmorSlot.HELMET)) {
                attacker.damage(1.0, victim);
                attacker.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, attacker.getLocation().add(0, 1, 0), 6, 0.2, 0.4, 0.2, 0);
            }

            // Chainmail Reinforced chestplate: thorns 1
            if (armorManager.isWearingPiece(victim, LegendaryArmorSet.CHAINMAIL_REINFORCED, LegendaryArmorSet.ArmorSlot.CHESTPLATE)) {
                attacker.damage(1.0, victim);
            }

            // Sculk Armor chestplate: Slowness I to attacker
            if (armorManager.isWearingPiece(victim, LegendaryArmorSet.SCULK_ARMOR, LegendaryArmorSet.ArmorSlot.CHESTPLATE)) {
                attacker.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 0, true, false, false));
            }

            // Shadow Stalker: 15% dodge while sneaking
            if (victim.isSneaking()
                    && armorManager.isWearingPiece(victim, LegendaryArmorSet.SHADOW_STALKER, LegendaryArmorSet.ArmorSlot.CHESTPLATE)) {
                if (random.nextDouble() < 0.15) {
                    event.setCancelled(true);
                    victim.getWorld().spawnParticle(Particle.SMOKE, victim.getLocation().add(0, 1, 0), 15, 0.3, 0.5, 0.3, 0.05);
                    victim.playSound(victim.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 2.0f);
                    victim.sendActionBar(Component.text("Dodged!", NamedTextColor.GRAY).decorate(TextDecoration.ITALIC));
                    return;
                }
            }

            // Reinforced Leather full set: 10% dodge
            if (armorManager.hasFullSet(victim, LegendaryArmorSet.REINFORCED_LEATHER)) {
                if (random.nextDouble() < 0.10) {
                    event.setCancelled(true);
                    victim.getWorld().spawnParticle(Particle.CLOUD, victim.getLocation().add(0, 1, 0), 8, 0.3, 0.5, 0.3, 0.02);
                    victim.sendActionBar(Component.text("Dodged!", NamedTextColor.WHITE).decorate(TextDecoration.ITALIC));
                    return;
                }
            }

            // Void Walker chestplate: 20% full negate
            if (armorManager.isWearingPiece(victim, LegendaryArmorSet.VOID_WALKER, LegendaryArmorSet.ArmorSlot.CHESTPLATE)) {
                if (random.nextDouble() < 0.20) {
                    event.setCancelled(true);
                    victim.getWorld().spawnParticle(Particle.PORTAL, victim.getLocation().add(0, 1, 0), 25, 0.3, 0.5, 0.3, 0.5);
                    victim.playSound(victim.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.4f, 1.5f);
                    victim.sendActionBar(Component.text("Void Shield!", NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD));
                    return;
                }
            }

            // Bone Armor chestplate: -15% from undead
            if (armorManager.isWearingPiece(victim, LegendaryArmorSet.BONE_ARMOR, LegendaryArmorSet.ArmorSlot.CHESTPLATE)) {
                String type = attacker.getType().name().toLowerCase();
                if (type.contains("zombie") || type.contains("skeleton") || type.contains("wither")
                        || type.contains("phantom") || type.contains("drowned") || type.contains("stray")
                        || type.contains("husk")) {
                    event.setDamage(event.getDamage() * 0.85);
                }
            }
        }
    }

    // ===== DAMAGE TAKEN EVENT =====

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        EntityDamageEvent.DamageCause cause = event.getCause();

        if (cause == EntityDamageEvent.DamageCause.WITHER) {
            if (armorManager.hasFullSet(player, LegendaryArmorSet.ABYSSAL_PLATE)
                    || armorManager.isWearingPiece(player, LegendaryArmorSet.BONE_ARMOR, LegendaryArmorSet.ArmorSlot.HELMET)) {
                event.setCancelled(true);
                return;
            }
        }

        if (cause == EntityDamageEvent.DamageCause.FALL) {
            if (armorManager.isWearingPiece(player, LegendaryArmorSet.VOID_WALKER, LegendaryArmorSet.ArmorSlot.BOOTS)
                    || armorManager.isWearingPiece(player, LegendaryArmorSet.DRAGON_KNIGHT, LegendaryArmorSet.ArmorSlot.BOOTS)
                    || armorManager.isWearingPiece(player, LegendaryArmorSet.ABYSSAL_PLATE, LegendaryArmorSet.ArmorSlot.BOOTS)) {
                event.setCancelled(true);
                return;
            }
            if (player.isSneaking() && armorManager.isWearingPiece(player, LegendaryArmorSet.SHADOW_STALKER, LegendaryArmorSet.ArmorSlot.BOOTS)) {
                event.setCancelled(true);
                return;
            }
            if (armorManager.isWearingPiece(player, LegendaryArmorSet.REINFORCED_LEATHER, LegendaryArmorSet.ArmorSlot.BOOTS)) {
                event.setDamage(event.getDamage() * 0.7);
                return;
            }
        }

        if (cause == EntityDamageEvent.DamageCause.FIRE || cause == EntityDamageEvent.DamageCause.FIRE_TICK
                || cause == EntityDamageEvent.DamageCause.LAVA || cause == EntityDamageEvent.DamageCause.HOT_FLOOR) {
            if (armorManager.isWearingPiece(player, LegendaryArmorSet.ABYSSAL_PLATE, LegendaryArmorSet.ArmorSlot.BOOTS)
                    || armorManager.isWearingPiece(player, LegendaryArmorSet.FROST_WARDEN, LegendaryArmorSet.ArmorSlot.BOOTS)
                    || armorManager.isWearingPiece(player, LegendaryArmorSet.DRAGON_KNIGHT, LegendaryArmorSet.ArmorSlot.CHESTPLATE)) {
                event.setCancelled(true);
                return;
            }
        }

        if (cause == EntityDamageEvent.DamageCause.LIGHTNING) {
            if (armorManager.isWearingPiece(player, LegendaryArmorSet.COPPER_ARMOR, LegendaryArmorSet.ArmorSlot.CHESTPLATE)) {
                event.setCancelled(true);
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1, true, false, false));
                player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, player.getLocation().add(0, 2, 0), 30, 0.5, 1, 0.5, 0.1);
                player.sendActionBar(Component.text("Lightning Absorbed!", NamedTextColor.YELLOW).decorate(TextDecoration.BOLD));
                return;
            }
        }

        if (cause == EntityDamageEvent.DamageCause.PROJECTILE) {
            if (armorManager.isWearingPiece(player, LegendaryArmorSet.CHAINMAIL_REINFORCED, LegendaryArmorSet.ArmorSlot.HELMET)) {
                event.setDamage(event.getDamage() * 0.9);
            }
        }

        if (cause == EntityDamageEvent.DamageCause.VOID) {
            if (armorManager.isWearingPiece(player, LegendaryArmorSet.VOID_WALKER, LegendaryArmorSet.ArmorSlot.BOOTS)) {
                event.setCancelled(true);
                Location surface = player.getWorld().getHighestBlockAt(player.getLocation()).getLocation().add(0, 1, 0);
                player.teleport(surface);
                player.getWorld().spawnParticle(Particle.PORTAL, surface.clone().add(0, 1, 0), 40, 0.5, 1, 0.5, 0.5);
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.8f);
                player.sendActionBar(Component.text("Void Anchor: Saved!", NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD));
                return;
            }
        }
    }

    // ===== DEATH EVENT =====

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        UUID uid = event.getEntity().getUniqueId();
        if (bloodMoonBonusHP.containsKey(uid)) {
            bloodMoonBonusHP.remove(uid);
            AttributeInstance maxHpAttr = event.getEntity().getAttribute(Attribute.MAX_HEALTH);
            if (maxHpAttr != null) {
                maxHpAttr.setBaseValue(20.0);
            }
        }
    }

    // ===== SNEAK TOGGLE =====

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        UUID uid = player.getUniqueId();
        if (event.isSneaking()) {
            if (armorManager.hasFullSet(player, LegendaryArmorSet.SHADOW_STALKER)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, true, false, false));
                shadowStalkerInvisible.put(uid, true);
                player.sendActionBar(Component.text("Shadow Cloak: Active", NamedTextColor.DARK_GRAY).decorate(TextDecoration.ITALIC));
            }
            if (armorManager.hasFullSet(player, LegendaryArmorSet.DRAGON_KNIGHT)) {
                long now = System.currentTimeMillis();
                if (now - dragonKnightRoarCooldown.getOrDefault(uid, 0L) >= 30000) {
                    dragonKnightRoarCooldown.put(uid, now);
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0f, 0.6f);
                    player.getWorld().spawnParticle(Particle.DRAGON_BREATH, player.getLocation().add(0, 1, 0), 60, 4, 2, 4, 0.1);
                    for (Entity e : player.getNearbyEntities(8, 8, 8)) {
                        if (e instanceof LivingEntity le && !(le instanceof Player)) {
                            le.damage(15.0, player);
                            Vector kb = le.getLocation().toVector().subtract(player.getLocation().toVector()).normalize().multiply(1.5).setY(0.5);
                            le.setVelocity(kb);
                        }
                    }
                    player.sendActionBar(Component.text("Dragon Roar!", TextColor.color(255, 100, 0)).decorate(TextDecoration.BOLD));
                }
            }
        } else {
            if (shadowStalkerInvisible.getOrDefault(uid, false)) {
                player.removePotionEffect(PotionEffectType.INVISIBILITY);
                shadowStalkerInvisible.put(uid, false);
            }
        }
    }

    // ===== MOVEMENT EVENT =====

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location loc = player.getLocation();

        // Void Walker: teleport on sneak + jump
        if (player.isSneaking() && armorManager.hasFullSet(player, LegendaryArmorSet.VOID_WALKER)) {
            Vector velocity = player.getVelocity();
            if (velocity.getY() > 0.1) {
                UUID uid = player.getUniqueId();
                long now = System.currentTimeMillis();
                if (now - voidWalkerTeleportCooldown.getOrDefault(uid, 0L) >= 4000) {
                    voidWalkerTeleportCooldown.put(uid, now);
                    Vector dir = player.getLocation().getDirection().normalize().multiply(12);
                    Location target = player.getLocation().add(dir);
                    target.setY(target.getWorld().getHighestBlockYAt(target) + 1);
                    if (target.getBlock().getType().isAir() && target.clone().add(0, 1, 0).getBlock().getType().isAir()) {
                        Location from = player.getLocation();
                        player.teleport(target);
                        from.getWorld().spawnParticle(Particle.PORTAL, from.add(0, 1, 0), 40, 0.3, 0.5, 0.3, 0.5);
                        target.getWorld().spawnParticle(Particle.PORTAL, target.add(0, 1, 0), 40, 0.3, 0.5, 0.3, 0.5);
                        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.2f);
                        player.sendActionBar(Component.text("Void Step!", NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD));
                    }
                }
            }
        }

        // Dragon Knight: double jump
        if (player.isSneaking() && armorManager.isWearingPiece(player, LegendaryArmorSet.DRAGON_KNIGHT, LegendaryArmorSet.ArmorSlot.BOOTS)) {
            Vector velocity = player.getVelocity();
            if (velocity.getY() > 0.1 && !player.isOnGround()) {
                UUID uid = player.getUniqueId();
                long now = System.currentTimeMillis();
                if (now - dragonKnightDoubleJumpCD.getOrDefault(uid, 0L) >= 3000) {
                    dragonKnightDoubleJumpCD.put(uid, now);
                    Vector boost = player.getLocation().getDirection().normalize().multiply(0.8).setY(0.9);
                    player.setVelocity(boost);
                    player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 15, 0.5, 0.2, 0.5, 0.05);
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.8f, 1.2f);
                    player.sendActionBar(Component.text("Dragon Leap!", TextColor.color(255, 100, 0)).decorate(TextDecoration.BOLD));
                }
            }
        }

        // Frost Warden boots/leggings: freeze water to ice
        if (armorManager.isWearingPiece(player, LegendaryArmorSet.FROST_WARDEN, LegendaryArmorSet.ArmorSlot.BOOTS)
                || armorManager.isWearingPiece(player, LegendaryArmorSet.FROST_WARDEN, LegendaryArmorSet.ArmorSlot.LEGGINGS)) {
            int radius = armorManager.isWearingPiece(player, LegendaryArmorSet.FROST_WARDEN, LegendaryArmorSet.ArmorSlot.LEGGINGS) ? 3 : 2;
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = loc.clone().add(x, -1, z).getBlock();
                    if (block.getType() == Material.WATER) {
                        block.setType(Material.FROSTED_ICE);
                    }
                }
            }
        }

        // Abyssal Plate boots: lava walking
        if (armorManager.isWearingPiece(player, LegendaryArmorSet.ABYSSAL_PLATE, LegendaryArmorSet.ArmorSlot.BOOTS)) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    Block block = loc.clone().add(x, -1, z).getBlock();
                    if (block.getType() == Material.LAVA) {
                        block.setType(Material.OBSIDIAN);
                        final Block b = block;
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (b.getType() == Material.OBSIDIAN) {
                                    b.setType(Material.LAVA);
                                }
                            }
                        }.runTaskLater(plugin, 100L);
                    }
                }
            }
        }

        // Nature's Embrace boots: flowers on grass
        if (armorManager.isWearingPiece(player, LegendaryArmorSet.NATURES_EMBRACE, LegendaryArmorSet.ArmorSlot.BOOTS)) {
            Block below = loc.clone().add(0, -1, 0).getBlock();
            Block atFeet = loc.getBlock();
            if ((below.getType() == Material.GRASS_BLOCK || below.getType() == Material.DIRT)
                    && atFeet.getType() == Material.AIR) {
                if (random.nextDouble() < 0.15) {
                    Material[] flowers = {Material.DANDELION, Material.POPPY, Material.BLUE_ORCHID,
                            Material.ALLIUM, Material.AZURE_BLUET, Material.CORNFLOWER, Material.LILY_OF_THE_VALLEY};
                    atFeet.setType(flowers[random.nextInt(flowers.length)]);
                    final Block f = atFeet;
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Material ft = f.getType();
                            if (ft == Material.DANDELION || ft == Material.POPPY || ft == Material.BLUE_ORCHID
                                    || ft == Material.ALLIUM || ft == Material.AZURE_BLUET
                                    || ft == Material.CORNFLOWER || ft == Material.LILY_OF_THE_VALLEY) {
                                f.setType(Material.AIR);
                            }
                        }
                    }.runTaskLater(plugin, 160L);
                }
            }
        }
    }
}
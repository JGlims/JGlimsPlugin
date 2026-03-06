package com.jglims.plugin.legendary;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Biome;
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
import net.kyori.adventure.text.format.TextDecoration;

public class LegendaryArmorListener implements Listener {

    private final JGlimsPlugin plugin;
    private final LegendaryArmorManager armorManager;
    private final Map<UUID, Integer> bloodMoonBonusHP = new HashMap<>();
    private final Map<UUID, Long> voidWalkerTeleportCooldown = new HashMap<>();
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

    private void applyHelmetPassives(Player player) {
        LegendaryArmorSet set = armorManager.identifySet(player.getInventory().getHelmet());
        if (set == null) return;
        switch (set) {
            case DRAGON_KNIGHT -> player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 40, 0, true, false, false));
            case BLOOD_MOON -> {
                World world = player.getWorld();
                if (world.getEnvironment() == World.Environment.NORMAL && world.getTime() >= 13000 && world.getTime() <= 23000) {
                    for (Entity entity : player.getNearbyEntities(30, 30, 30)) {
                        if (entity instanceof Monster mob) {
                            mob.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 30, 0, true, false, false));
                        }
                    }
                }
            }
            case VOID_WALKER -> {
                for (Entity entity : player.getNearbyEntities(20, 20, 20)) {
                    if (entity instanceof LivingEntity le && le.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                        le.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 30, 0, true, false, false));
                    }
                }
            }
            case NATURES_EMBRACE -> player.removePotionEffect(PotionEffectType.POISON);
            case SHADOW_STALKER -> {
                if (player.isSneaking()) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 40, 0, true, false, false));
                }
            }
            case FROST_WARDEN -> {
                player.removePotionEffect(PotionEffectType.SLOWNESS);
                player.setFreezeTicks(0);
            }
            default -> {}
        }
    }

    private void applyChestplatePassives(Player player) {
        LegendaryArmorSet set = armorManager.identifySet(player.getInventory().getChestplate());
        if (set == null) return;
        switch (set) {
            case FROST_WARDEN -> {
                for (Entity entity : player.getNearbyEntities(3, 3, 3)) {
                    if (entity instanceof Monster mob) {
                        mob.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 30, 1, true, false, false));
                    }
                }
            }
            default -> {}
        }
    }

    private void applyLeggingsPassives(Player player) {
        LegendaryArmorSet set = armorManager.identifySet(player.getInventory().getLeggings());
        if (set == null) return;
        switch (set) {
            case BLOOD_MOON -> {
                World world = player.getWorld();
                if (world.getEnvironment() == World.Environment.NORMAL && world.getTime() >= 13000 && world.getTime() <= 23000) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 0, true, false, false));
                }
            }
            case VOID_WALKER -> player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 40, 0, true, false, false));
            case SHADOW_STALKER -> {
                if (player.isSneaking()) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 0, true, false, false));
                }
            }
            default -> {}
        }
    }

    private void applyBootsPassives(Player player) {
        LegendaryArmorSet set = armorManager.identifySet(player.getInventory().getBoots());
        if (set == null) return;
        switch (set) {
            case FROST_WARDEN -> {
                player.setFireTicks(0);
                player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 40, 0, true, false, false));
            }
            default -> {}
        }
    }

    private void applySetBonus(Player player, LegendaryArmorSet set) {
        switch (set) {
            case NATURES_EMBRACE -> {
                String biomeName = player.getLocation().getBlock().getBiome().name().toLowerCase();
                if (biomeName.contains("forest") || biomeName.contains("grove") || biomeName.contains("taiga")) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 40, 0, true, false, false));
                }
            }
            case FROST_WARDEN -> {
                UUID uid = player.getUniqueId();
                long now = System.currentTimeMillis();
                if (now - frostWardenLastFreeze.getOrDefault(uid, 0L) >= 3000) {
                    frostWardenLastFreeze.put(uid, now);
                    for (Entity entity : player.getNearbyEntities(4, 4, 4)) {
                        if (entity instanceof Monster mob) {
                            mob.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1, true, false, false));
                            mob.setFreezeTicks(60);
                            mob.getWorld().spawnParticle(Particle.SNOWFLAKE, mob.getLocation().add(0, 1, 0), 8, 0.3, 0.5, 0.3, 0.02);
                        }
                    }
                    player.getWorld().spawnParticle(Particle.SNOWFLAKE, player.getLocation().add(0, 1, 0), 15, 1.5, 0.5, 1.5, 0.01);
                }
            }
            default -> {}
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player attacker && event.getEntity() instanceof LivingEntity target) {
            LegendaryArmorSet fullSet = armorManager.getActiveFullSet(attacker);
            if (fullSet == LegendaryArmorSet.DRAGON_KNIGHT) {
                String targetType = target.getType().name().toLowerCase();
                if (targetType.contains("dragon") || targetType.contains("ender")) {
                    event.setDamage(event.getDamage() * 1.2);
                    attacker.getWorld().spawnParticle(Particle.DRAGON_BREATH, target.getLocation().add(0, 1, 0), 15, 0.3, 0.5, 0.3, 0.05);
                }
            }
            if (fullSet == LegendaryArmorSet.BLOOD_MOON) {
                double healed = event.getFinalDamage() * 0.05;
                double maxHp = attacker.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue();
                attacker.setHealth(Math.min(attacker.getHealth() + healed, maxHp));
            }
            if (attacker.isSneaking() && armorManager.isWearingPiece(attacker, LegendaryArmorSet.SHADOW_STALKER, LegendaryArmorSet.ArmorSlot.CHESTPLATE)) {
                event.setDamage(event.getDamage() + 2.0);
            }
            // Blood Moon chestplate: +2 HP per kill
            if (target.getHealth() - event.getFinalDamage() <= 0) {
                if (armorManager.isWearingPiece(attacker, LegendaryArmorSet.BLOOD_MOON, LegendaryArmorSet.ArmorSlot.CHESTPLATE)) {
                    UUID uid = attacker.getUniqueId();
                    int currentBonus = bloodMoonBonusHP.getOrDefault(uid, 0);
                    if (currentBonus < 10) {
                        bloodMoonBonusHP.put(uid, currentBonus + 2);
                        double newMax = 20.0 + bloodMoonBonusHP.get(uid);
                        attacker.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).setBaseValue(newMax);
                        attacker.sendActionBar(Component.text("Blood Moon: +" + bloodMoonBonusHP.get(uid) + " bonus HP", NamedTextColor.DARK_RED));
                    }
                }
            }
        }
        if (event.getEntity() instanceof Player victim && event.getDamager() instanceof LivingEntity attacker) {
            if (armorManager.isWearingPiece(victim, LegendaryArmorSet.NATURES_EMBRACE, LegendaryArmorSet.ArmorSlot.CHESTPLATE)) {
                attacker.damage(3.0, victim);
                attacker.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, attacker.getLocation().add(0, 1, 0), 10, 0.3, 0.5, 0.3, 0);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            if (armorManager.isWearingPiece(player, LegendaryArmorSet.VOID_WALKER, LegendaryArmorSet.ArmorSlot.BOOTS)
                    || armorManager.isWearingPiece(player, LegendaryArmorSet.DRAGON_KNIGHT, LegendaryArmorSet.ArmorSlot.BOOTS)) {
                event.setCancelled(true);
                return;
            }
        }
        if ((event.getCause() == EntityDamageEvent.DamageCause.FIRE
                || event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK
                || event.getCause() == EntityDamageEvent.DamageCause.LAVA
                || event.getCause() == EntityDamageEvent.DamageCause.HOT_FLOOR)
                && armorManager.isWearingPiece(player, LegendaryArmorSet.FROST_WARDEN, LegendaryArmorSet.ArmorSlot.BOOTS)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        UUID uid = event.getEntity().getUniqueId();
        if (bloodMoonBonusHP.containsKey(uid)) {
            bloodMoonBonusHP.remove(uid);
            event.getEntity().getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).setBaseValue(20.0);
        }
    }

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
        } else {
            if (shadowStalkerInvisible.getOrDefault(uid, false)) {
                player.removePotionEffect(PotionEffectType.INVISIBILITY);
                shadowStalkerInvisible.put(uid, false);
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.isSneaking() && armorManager.hasFullSet(player, LegendaryArmorSet.VOID_WALKER)) {
            Vector velocity = player.getVelocity();
            if (velocity.getY() > 0.1) {
                UUID uid = player.getUniqueId();
                long now = System.currentTimeMillis();
                if (now - voidWalkerTeleportCooldown.getOrDefault(uid, 0L) >= 5000) {
                    voidWalkerTeleportCooldown.put(uid, now);
                    Vector dir = player.getLocation().getDirection().normalize().multiply(8);
                    Location target = player.getLocation().add(dir);
                    target.setY(target.getWorld().getHighestBlockYAt(target) + 1);
                    if (target.getBlock().getType().isAir() && target.clone().add(0, 1, 0).getBlock().getType().isAir()) {
                        Location from = player.getLocation();
                        player.teleport(target);
                        from.getWorld().spawnParticle(Particle.PORTAL, from.add(0, 1, 0), 30, 0.3, 0.5, 0.3, 0.5);
                        target.getWorld().spawnParticle(Particle.PORTAL, target.add(0, 1, 0), 30, 0.3, 0.5, 0.3, 0.5);
                        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.2f);
                        player.sendActionBar(Component.text("Void Step!", NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD));
                    }
                }
            }
        }
        if (armorManager.isWearingPiece(player, LegendaryArmorSet.FROST_WARDEN, LegendaryArmorSet.ArmorSlot.BOOTS)) {
            Location loc = player.getLocation();
            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    Block block = loc.clone().add(x, -1, z).getBlock();
                    if (block.getType() == Material.WATER) {
                        block.setType(Material.FROSTED_ICE);
                    }
                }
            }
        }
    }
}
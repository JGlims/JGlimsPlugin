package com.jglims.plugin.enchantments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Boss;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Trident;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.weapons.BattleAxeManager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class EnchantmentEffectListener implements Listener {

    private final JGlimsPlugin plugin;
    private final CustomEnchantManager enchantManager;

    private final Map<UUID, Long> boostCooldowns = new HashMap<>();
    private final Map<UUID, Long> gravityWellCooldowns = new HashMap<>();
    private final Set<UUID> processingBlockBreak = new HashSet<>();

    // Momentum tracking: player UUID -> consecutive sprint ticks
    private final Map<UUID, Integer> momentumTicks = new HashMap<>();

    private final NamespacedKey reaperMarkKey;
    private final NamespacedKey reaperMarkTimeKey;
    private final NamespacedKey soulReapAttackerKey;
    private final NamespacedKey soulReapLevelKey;
    private final NamespacedKey vitalityModKey;

    // Axe nerf modifier key
    private final NamespacedKey axeNerfSpeedKey;

    public EnchantmentEffectListener(JGlimsPlugin plugin, CustomEnchantManager enchantManager) {
        this.plugin = plugin;
        this.enchantManager = enchantManager;
        this.reaperMarkKey = new NamespacedKey(plugin, "reaper_mark_level");
        this.reaperMarkTimeKey = new NamespacedKey(plugin, "reaper_mark_expire");
        this.soulReapAttackerKey = new NamespacedKey(plugin, "soul_reap_attacker");
        this.soulReapLevelKey = new NamespacedKey(plugin, "soul_reap_level");
        this.vitalityModKey = new NamespacedKey(plugin, "vitality_health");
        this.axeNerfSpeedKey = new NamespacedKey(plugin, "axe_nerf_speed");
    }

    // ========================================================================
    // MELEE/PROJECTILE DAMAGE — ATTACKER & DEFENDER ENCHANTMENTS
    // ========================================================================
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {

        // --- DEFENDER ENCHANTMENTS (armor) ---
        if (event.getEntity() instanceof Player victim) {
            if (handleDefenderEnchantments(event, victim)) return;
        }

        // --- BEST BUDDIES: Wolf taking damage ---
        if (event.getEntity() instanceof Wolf wolf) {
            EntityEquipment equip = wolf.getEquipment();
            if (equip != null) {
                ItemStack wolfArmor = equip.getItem(EquipmentSlot.BODY);
                if (wolfArmor != null && enchantManager.hasEnchant(wolfArmor, EnchantmentType.BEST_BUDDIES)) {
                    event.setDamage(event.getDamage() * 0.05);
                    wolf.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 1, true, false, false));
                }
            }
        }

        // --- BEST BUDDIES: Wolf dealing damage ---
        if (event.getDamager() instanceof Wolf wolf) {
            EntityEquipment equip = wolf.getEquipment();
            if (equip != null) {
                ItemStack wolfArmor = equip.getItem(EquipmentSlot.BODY);
                if (wolfArmor != null && enchantManager.hasEnchant(wolfArmor, EnchantmentType.BEST_BUDDIES)) {
                    event.setDamage(event.getDamage() * 0.05);
                }
            }
        }

        // --- ATTACKER ENCHANTMENTS (weapon) ---
        Player attacker = getPlayerAttacker(event);
        if (attacker == null) return;

        ItemStack weapon = attacker.getInventory().getItemInMainHand();
        if (weapon.getType() == Material.AIR) return;

        Entity target = event.getEntity();
        if (!(target instanceof LivingEntity livingTarget)) return;

        double baseDamage = event.getDamage();

        // --- Berserker ---
        int berserkerLvl = enchantManager.getEnchantLevel(weapon, EnchantmentType.BERSERKER);
        if (berserkerLvl > 0) {
            AttributeInstance maxHpAttr = attacker.getAttribute(Attribute.MAX_HEALTH);
            double maxHp = maxHpAttr != null ? maxHpAttr.getValue() : 20.0;
            double missingPercent = (maxHp - attacker.getHealth()) / maxHp;
            double bonus = berserkerLvl * 0.5 * missingPercent * baseDamage;
            event.setDamage(baseDamage + bonus);
            baseDamage = event.getDamage();
        }

        // --- Blood Price ---
        int bloodPriceLvl = enchantManager.getEnchantLevel(weapon, EnchantmentType.BLOOD_PRICE);
        if (bloodPriceLvl > 0) {
            double bonusDmg = 0.5 + (bloodPriceLvl * 0.5);
            event.setDamage(baseDamage + bonusDmg);
            baseDamage = event.getDamage();
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (attacker.isOnline() && attacker.getHealth() > 1.0) attacker.damage(1.0);
            });
        }

        // --- Reaper's Mark: bonus damage if target is marked ---
        PersistentDataContainer targetPdc = livingTarget.getPersistentDataContainer();
        if (targetPdc.has(reaperMarkKey, PersistentDataType.INTEGER)) {
            long expireTime = targetPdc.getOrDefault(reaperMarkTimeKey, PersistentDataType.LONG, 0L);
            if (System.currentTimeMillis() < expireTime) {
                int markLvl = targetPdc.getOrDefault(reaperMarkKey, PersistentDataType.INTEGER, 0);
                double bonusPct = markLvl == 1 ? 0.10 : 0.15;
                event.setDamage(baseDamage * (1.0 + bonusPct));
                baseDamage = event.getDamage();
            } else {
                targetPdc.remove(reaperMarkKey);
                targetPdc.remove(reaperMarkTimeKey);
            }
        }

        // --- Reaper's Mark: apply mark on hit ---
        int reapersMarkLvl = enchantManager.getEnchantLevel(weapon, EnchantmentType.REAPERS_MARK);
        if (reapersMarkLvl > 0) {
            targetPdc.set(reaperMarkKey, PersistentDataType.INTEGER, reapersMarkLvl);
            long durationMs = reapersMarkLvl == 1 ? 5000L : 8000L;
            targetPdc.set(reaperMarkTimeKey, PersistentDataType.LONG, System.currentTimeMillis() + durationMs);
        }

        // --- Vampirism (BUG 4 FIX applied) ---
        int vampirismLvl = enchantManager.getEnchantLevel(weapon, EnchantmentType.VAMPIRISM);
        if (vampirismLvl > 0) {
            int amp = switch (vampirismLvl) {
                case 1 -> 0; case 2 -> 1; case 3 -> 1; case 4 -> 2; default -> 3;
            };
            int dur = switch (vampirismLvl) {
                case 1 -> 40; case 2 -> 60; case 3 -> 80; case 4 -> 100; default -> 100;
            };
            attacker.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, dur, amp, true, false, false));
        }

        // --- Lifesteal ---
        int lifestealLvl = enchantManager.getEnchantLevel(weapon, EnchantmentType.LIFESTEAL);
        if (lifestealLvl > 0) {
            double pct = switch (lifestealLvl) { case 1 -> 0.05; case 2 -> 0.10; default -> 0.15; };
            double heal = baseDamage * pct;
            if (heal > 0) {
                AttributeInstance maxHpAttr = attacker.getAttribute(Attribute.MAX_HEALTH);
                double maxHp = maxHpAttr != null ? maxHpAttr.getValue() : 20.0;
                attacker.setHealth(Math.min(attacker.getHealth() + heal, maxHp));
            }
        }

        // --- Bleed ---
        int bleedLvl = enchantManager.getEnchantLevel(weapon, EnchantmentType.BLEED);
        if (bleedLvl > 0) {
            int totalTicks = bleedLvl * 10;
            int interval = 10;
            int ticks = totalTicks / interval;
            new BukkitRunnable() {
                int count = 0;
                @Override
                public void run() {
                    if (count >= ticks || livingTarget.isDead()) { cancel(); return; }
                    livingTarget.damage(2.0);
                    count++;
                }
            }.runTaskTimer(plugin, interval, interval);
        }

        // --- Venomstrike ---
        int venomLvl = enchantManager.getEnchantLevel(weapon, EnchantmentType.VENOMSTRIKE);
        if (venomLvl > 0) {
            int amp = venomLvl >= 3 ? 1 : 0;
            int dur = venomLvl <= 1 ? 60 : 100;
            livingTarget.addPotionEffect(new PotionEffect(PotionEffectType.POISON, dur, amp, true, true, true));
        }

        // --- Frostbite ---
        int frostLvl = enchantManager.getEnchantLevel(weapon, EnchantmentType.FROSTBITE);
        if (frostLvl > 0) {
            int dur = frostLvl * 10;
            livingTarget.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, dur, 0, true, true, true));
        }

        // --- Frostbite Blade (Phase 9) ---
        int frostBladeLvl = enchantManager.getEnchantLevel(weapon, EnchantmentType.FROSTBITE_BLADE);
        if (frostBladeLvl > 0) {
            int dur = frostBladeLvl * 30; // 1.5s, 3s, 4.5s
            livingTarget.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, dur, frostBladeLvl - 1, true, true, true));
            livingTarget.setFreezeTicks(Math.max(livingTarget.getFreezeTicks(), dur + 20));
            livingTarget.getWorld().spawnParticle(Particle.SNOWFLAKE, livingTarget.getLocation().add(0, 1, 0), 8, 0.3, 0.5, 0.3, 0.02);
        }


        // --- Wither Touch ---
        int witherLvl = enchantManager.getEnchantLevel(weapon, EnchantmentType.WITHER_TOUCH);
        if (witherLvl > 0) {
            int amp = witherLvl - 1;
            int dur = witherLvl == 1 ? 60 : 100;
            livingTarget.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, dur, amp, true, true, true));
        }

        // --- Chain Lightning (NEW v1.1.0) ---
        int chainLvl = enchantManager.getEnchantLevel(weapon, EnchantmentType.CHAIN_LIGHTNING);
        if (chainLvl > 0) {
            int chains = chainLvl;
            double dmgPct = switch (chainLvl) { case 1 -> 0.30; case 2 -> 0.40; default -> 0.50; };
            double chainDmg = baseDamage * dmgPct;
            Set<Entity> hit = new HashSet<>();
            hit.add(livingTarget);
            hit.add(attacker);
            LivingEntity current = livingTarget;
            for (int i = 0; i < chains; i++) {
                LivingEntity nearest = null;
                double nearestDist = 5.0;
                for (Entity e : current.getNearbyEntities(5, 3, 5)) {
                    if (e instanceof LivingEntity le && !hit.contains(e) && e != attacker) {
                        double d = current.getLocation().distance(e.getLocation());
                        if (d < nearestDist) { nearestDist = d; nearest = le; }
                    }
                }
                if (nearest == null) break;
                nearest.damage(chainDmg, attacker);
                hit.add(nearest);
                Location from = current.getLocation().add(0, 1, 0);
                Location to = nearest.getLocation().add(0, 1, 0);
                current.getWorld().spawnParticle(Particle.ELECTRIC_SPARK,
                    from.clone().add(to.toVector().subtract(from.toVector()).multiply(0.5)),
                    5, 0.1, 0.1, 0.1, 0.01);
                current = nearest;
            }
        }

        // --- Lumberjack ---
        int lumberjackLvl = enchantManager.getEnchantLevel(weapon, EnchantmentType.LUMBERJACK);
        if (lumberjackLvl > 0 && livingTarget instanceof Player targetPlayer && targetPlayer.isBlocking()) {
            double chance = switch (lumberjackLvl) { case 1 -> 0.10; case 2 -> 0.20; default -> 0.30; };
            if (ThreadLocalRandom.current().nextDouble() < chance) {
                targetPlayer.setCooldown(Material.SHIELD, 100);
            }
        }

        // --- Cleave ---
        int cleaveLvl = enchantManager.getEnchantLevel(weapon, EnchantmentType.CLEAVE);
        if (cleaveLvl > 0) {
            double dmgPct = switch (cleaveLvl) { case 1 -> 0.20; case 2 -> 0.35; default -> 0.50; };
            double radius = switch (cleaveLvl) { case 1 -> 2.0; case 2 -> 2.5; default -> 3.0; };
            double clvDmg = baseDamage * dmgPct;
            for (Entity nearby : livingTarget.getNearbyEntities(radius, radius, radius)) {
                if (nearby instanceof Monster m && m != attacker && m != livingTarget) {
                    m.damage(clvDmg, attacker);
                }
            }
        }

        // --- Wrath (Phase 9) ---
        int wrathLvl = enchantManager.getEnchantLevel(weapon, EnchantmentType.WRATH);
        if (wrathLvl > 0) {
            NamespacedKey wrathTargetKey = new NamespacedKey(plugin, "wrath_target");
            NamespacedKey wrathStackKey = new NamespacedKey(plugin, "wrath_stacks");
            NamespacedKey wrathTimeKey = new NamespacedKey(plugin, "wrath_time");
            PersistentDataContainer attackerPdc = attacker.getPersistentDataContainer();
            String targetUuid = livingTarget.getUniqueId().toString();
            String lastTarget = attackerPdc.getOrDefault(wrathTargetKey, PersistentDataType.STRING, "");
            long lastTime = attackerPdc.getOrDefault(wrathTimeKey, PersistentDataType.LONG, 0L);
            long now = System.currentTimeMillis();
            int stacks;
            if (targetUuid.equals(lastTarget) && (now - lastTime) < 3000L) {
                stacks = Math.min(attackerPdc.getOrDefault(wrathStackKey, PersistentDataType.INTEGER, 0) + 1, 3);
            } else {
                stacks = 1;
            }
            attackerPdc.set(wrathTargetKey, PersistentDataType.STRING, targetUuid);
            attackerPdc.set(wrathStackKey, PersistentDataType.INTEGER, stacks);
            attackerPdc.set(wrathTimeKey, PersistentDataType.LONG, now);
            double wrathBonus = stacks * (wrathLvl * 0.10) * baseDamage;
            event.setDamage(baseDamage + wrathBonus);
            baseDamage = event.getDamage();
        }


        // --- Guillotine ---
        int guillotineLvl = enchantManager.getEnchantLevel(weapon, EnchantmentType.GUILLOTINE);
        if (guillotineLvl > 0 && !(livingTarget instanceof Boss)) {
            AttributeInstance tMaxHp = livingTarget.getAttribute(Attribute.MAX_HEALTH);
            double tMax = tMaxHp != null ? tMaxHp.getValue() : 20.0;
            if (livingTarget.getHealth() / tMax < 0.30) {
                double killChance = switch (guillotineLvl) { case 1 -> 0.03; case 2 -> 0.06; default -> 0.10; };
                if (ThreadLocalRandom.current().nextDouble() < killChance) {
                    event.setDamage(tMax * 10);
                }
            }
        }

        // --- Soul Reap: tag target ---
        int soulReapLvl = enchantManager.getEnchantLevel(weapon, EnchantmentType.SOUL_REAP);
        if (soulReapLvl > 0) {
            targetPdc.set(soulReapAttackerKey, PersistentDataType.STRING, attacker.getUniqueId().toString());
            targetPdc.set(soulReapLevelKey, PersistentDataType.INTEGER, soulReapLvl);
        }

        // --- Soul Harvest (Phase 9) ---
        int soulHarvestLvl = enchantManager.getEnchantLevel(weapon, EnchantmentType.SOUL_HARVEST);
        if (soulHarvestLvl > 0) {
            int witherDur = soulHarvestLvl * 40; // 2s, 4s, 6s
            int witherAmp = soulHarvestLvl - 1;
            livingTarget.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, witherDur, witherAmp, true, true, true));
            // Grant Regen to attacker while target has Wither
            attacker.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, witherDur, soulHarvestLvl - 1, true, false, false));
            livingTarget.getWorld().spawnParticle(Particle.SOUL, livingTarget.getLocation().add(0, 1, 0), 6, 0.3, 0.4, 0.3, 0.02);
        }

        // --- Burial (Phase 9) ---
        int burialLvl = enchantManager.getEnchantLevel(weapon, EnchantmentType.BURIAL);
        if (burialLvl > 0) {
            int dur = burialLvl * 20; // 1s, 2s, 3s
            livingTarget.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, dur, 2, true, true, true)); // Slowness III
            livingTarget.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, dur, 0, true, true, true)); // Blindness I
            livingTarget.getWorld().spawnParticle(Particle.ASH, livingTarget.getLocation().add(0, 1, 0), 10, 0.3, 0.5, 0.3, 0.02);
        }

        // --- Tremor (Phase 9) ---
        int tremorLvl = enchantManager.getEnchantLevel(weapon, EnchantmentType.TREMOR);
        if (tremorLvl > 0) {
            double tremorRadius = tremorLvl * 2.0;
            for (Entity nearby : livingTarget.getNearbyEntities(tremorRadius, tremorRadius, tremorRadius)) {
                if (nearby instanceof LivingEntity le && nearby != attacker) {
                    le.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 60, 1, true, true, true)); // Mining Fatigue II, 3s
                }
            }
            livingTarget.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 60, 1, true, true, true));
            livingTarget.getWorld().spawnParticle(Particle.BLOCK, livingTarget.getLocation(), 15, tremorRadius * 0.5, 0.2, tremorRadius * 0.5, 0.01,
                Material.STONE.createBlockData());
        }

        // --- Phantom Pierce (Phase 9) ---
        int phantomPierceLvl = enchantManager.getEnchantLevel(weapon, EnchantmentType.PHANTOM_PIERCE);
        if (phantomPierceLvl > 0 && attacker.getAttackCooldown() >= 0.9f) { // Only on charged attacks
            int pierceCt = phantomPierceLvl;
            Vector atkDir = attacker.getLocation().getDirection().normalize();
            Set<Entity> pierced = new HashSet<>();
            pierced.add(livingTarget);
            pierced.add(attacker);
            LivingEntity current = livingTarget;
            for (int i = 0; i < pierceCt; i++) {
                LivingEntity next = null;
                double bestDot = 0.5; // Must be roughly in front (within ~60 degrees)
                double bestDist = 4.0;
                for (Entity e : current.getNearbyEntities(4, 2, 4)) {
                    if (e instanceof LivingEntity le && !pierced.contains(e)) {
                        Vector toEntity = e.getLocation().toVector().subtract(current.getLocation().toVector()).normalize();
                        double dot = atkDir.dot(toEntity);
                        double d = current.getLocation().distance(e.getLocation());
                        if (dot > bestDot && d < bestDist) {
                            bestDot = dot;
                            bestDist = d;
                            next = le;
                        }
                    }
                }
                if (next == null) break;
                next.damage(baseDamage, attacker);
                pierced.add(next);
                // Draw pierce line
                Location from = current.getLocation().add(0, 1, 0);
                Location to = next.getLocation().add(0, 1, 0);
                current.getWorld().spawnParticle(Particle.END_ROD,
                    from.clone().add(to.toVector().subtract(from.toVector()).multiply(0.5)),
                    3, 0.1, 0.1, 0.1, 0.01);
                current = next;
            }
        }


        // --- Normal Axe Combat Nerf (NEW v1.1.0) ---
        if (plugin.getConfigManager().isAxeNerfEnabled()) {
            String matName = weapon.getType().name();
            if (matName.endsWith("_AXE")) {
                BattleAxeManager bam = plugin.getBattleAxeManager();
                if (bam != null && !bam.isBattleAxe(weapon)) {
                    // This is a regular axe — apply damage reduction via attack cooldown
                    // Set the player's attack cooldown high
                    attacker.setCooldown(weapon.getType(), 40); // 2 seconds = 40 ticks
                }
            }
        }
    }

    // ========================================================================
    // DEFENDER ENCHANTMENTS
    // ========================================================================
    private boolean handleDefenderEnchantments(EntityDamageByEntityEvent event, Player victim) {
        ItemStack leggings = victim.getInventory().getLeggings();
        if (leggings != null) {
            int dodgeLvl = enchantManager.getEnchantLevel(leggings, EnchantmentType.DODGE);
            if (dodgeLvl > 0 && (event.getDamager() instanceof LivingEntity)) {
                double chance = dodgeLvl == 1 ? 0.08 : 0.15;
                if (ThreadLocalRandom.current().nextDouble() < chance) {
                    event.setCancelled(true);
                    victim.getWorld().spawnParticle(Particle.SMOKE, victim.getLocation().add(0, 1, 0), 5, 0.3, 0.3, 0.3, 0.01);
                    return true;
                }
            }
        }

        ItemStack chestplate = victim.getInventory().getChestplate();
        if (chestplate != null && event.getDamager() instanceof Projectile projectile) {
            int deflectLvl = enchantManager.getEnchantLevel(chestplate, EnchantmentType.DEFLECTION);
            if (deflectLvl > 0) {
                double chance = deflectLvl == 1 ? 0.10 : 0.20;
                if (ThreadLocalRandom.current().nextDouble() < chance) {
                    event.setCancelled(true);
                    if (projectile.getShooter() instanceof LivingEntity shooter) {
                        Vector dir = shooter.getLocation().toVector()
                            .subtract(victim.getLocation().toVector()).normalize();
                        victim.launchProjectile(Arrow.class, dir.multiply(projectile.getVelocity().length()));
                    }
                    projectile.remove();
                    return true;
                }
            }
        }

        if (chestplate != null) {
            int fortLvl = enchantManager.getEnchantLevel(chestplate, EnchantmentType.FORTIFICATION);
            if (fortLvl > 0) {
                double reduction = switch (fortLvl) { case 1 -> 0.05; case 2 -> 0.08; default -> 0.12; };
                event.setDamage(event.getDamage() * (1.0 - reduction));
            }
        }

        return false;
    }

    // ========================================================================
    // ENTITY DEATH — Soul Reap + Harvesting Moon (NEW v1.1.0)
    // ========================================================================
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        PersistentDataContainer pdc = entity.getPersistentDataContainer();

        // Soul Reap
        if (pdc.has(soulReapAttackerKey, PersistentDataType.STRING)) {
            String uuidStr = pdc.get(soulReapAttackerKey, PersistentDataType.STRING);
            Integer level = pdc.get(soulReapLevelKey, PersistentDataType.INTEGER);
            if (uuidStr != null && level != null) {
                Player attacker = plugin.getServer().getPlayer(UUID.fromString(uuidStr));
                if (attacker != null && attacker.isOnline()) {
                    int dur = switch (level) { case 1 -> 60; case 2 -> 100; default -> 140; };
                    PotionEffect existing = attacker.getPotionEffect(PotionEffectType.STRENGTH);
                    int newAmp = (existing != null && existing.getAmplifier() < 1) ? 1 : 0;
                    attacker.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, dur, newAmp, true, true, true));
                }
            }
        }

        // Harvesting Moon (NEW v1.1.0): bonus XP
        Player killer = entity.getKiller();
        if (killer != null) {
            ItemStack weapon = killer.getInventory().getItemInMainHand();
            int harvestMoonLvl = enchantManager.getEnchantLevel(weapon, EnchantmentType.HARVESTING_MOON);
            if (harvestMoonLvl > 0) {
                double xpMult = harvestMoonLvl * 0.50; // 50% per level
                int bonusXp = (int) (event.getDroppedExp() * xpMult);
                event.setDroppedExp(event.getDroppedExp() + bonusXp);
            }
        }

        // --- Reaping Curse (Phase 9) ---
        if (killer != null) {
            ItemStack killerWeapon = killer.getInventory().getItemInMainHand();
            int reapingCurseLvl = enchantManager.getEnchantLevel(killerWeapon, EnchantmentType.REAPING_CURSE);
            if (reapingCurseLvl > 0) {
                Location deathLoc = entity.getLocation();
                double cloudRadius = switch (reapingCurseLvl) { case 1 -> 2.0; case 2 -> 3.0; default -> 4.0; };
                int cloudDur = 60; // 3 seconds
                new BukkitRunnable() {
                    int ticks = 0;
                    @Override
                    public void run() {
                        if (ticks >= cloudDur) { cancel(); return; }
                        deathLoc.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, deathLoc.clone().add(0, 0.5, 0),
                            3, cloudRadius * 0.3, 0.2, cloudRadius * 0.3, 0.01);
                        if (ticks % 10 == 0) { // Damage pulse every 0.5s
                            for (Entity nearby : deathLoc.getWorld().getNearbyEntities(deathLoc, cloudRadius, cloudRadius, cloudRadius)) {
                                if (nearby instanceof LivingEntity le && nearby != killer && !(nearby instanceof Player)) {
                                    le.damage(4.0, killer);
                                }
                            }
                        }
                        ticks += 5;
                    }
                }.runTaskTimer(plugin, 0L, 5L);
            }

            // --- Crop Reaper (Phase 9) ---
            int cropReaperLvl = enchantManager.getEnchantLevel(killerWeapon, EnchantmentType.CROP_REAPER);
            if (cropReaperLvl > 0) {
                // Base 15% + 5% per Looting level
                int lootingLvl = killerWeapon.getEnchantmentLevel(org.bukkit.enchantments.Enchantment.LOOTING);
                double chance = 0.15 + (lootingLvl * 0.05);
                if (ThreadLocalRandom.current().nextDouble() < chance) {
                    Material[] crops = { Material.WHEAT, Material.CARROT, Material.POTATO,
                                         Material.BEETROOT, Material.WHEAT_SEEDS, Material.MELON_SLICE };
                    Material drop = crops[ThreadLocalRandom.current().nextInt(crops.length)];
                    entity.getWorld().dropItemNaturally(entity.getLocation(), new ItemStack(drop, 1 + ThreadLocalRandom.current().nextInt(3)));
                }
            }
        }

    }

    // ========================================================================
    // PROJECTILE HIT — Explosive Arrow, Thunderlord, Tidal Wave, Sniper
    // ========================================================================
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player shooter)) return;
        Projectile projectile = event.getEntity();
        ItemStack weapon = shooter.getInventory().getItemInMainHand();

        if (projectile instanceof AbstractArrow) {
            int explosiveLvl = enchantManager.getEnchantLevel(weapon, EnchantmentType.EXPLOSIVE_ARROW);
            if (explosiveLvl > 0) {
                Location loc = projectile.getLocation();
                float radius = switch (explosiveLvl) { case 1 -> 1.0f; case 2 -> 1.5f; default -> 2.0f; };
                double aoeDmg = switch (explosiveLvl) { case 1 -> 2.0; case 2 -> 4.0; default -> 6.0; };
                loc.getWorld().createExplosion(loc, 0f, false, false);
                for (Entity nearby : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
                    if (nearby instanceof LivingEntity le && nearby != shooter) {
                        le.damage(aoeDmg, shooter);
                    }
                }
            }

            if (weapon.getType() == Material.BOW) {
                int sniperLvl = enchantManager.getEnchantLevel(weapon, EnchantmentType.SNIPER);
                if (sniperLvl > 0 && event.getHitEntity() instanceof LivingEntity target) {
                    double dist = shooter.getLocation().distance(target.getLocation());
                    if (dist > 20.0) {
                        double bonusPct = switch (sniperLvl) { case 1 -> 0.10; case 2 -> 0.20; default -> 0.30; };
                        double bonusDmg = projectile.getVelocity().length() * bonusPct * 2.0;
                        plugin.getServer().getScheduler().runTask(plugin, () -> {
                            if (!target.isDead()) target.damage(bonusDmg, shooter);
                        });
                    }
                }
            }

            // --- Frostbite Arrow (Phase 9) ---
            int frostArrowLvl = enchantManager.getEnchantLevel(weapon, EnchantmentType.FROSTBITE_ARROW);
            if (frostArrowLvl > 0 && event.getHitEntity() instanceof LivingEntity frostTarget) {
                int dur = frostArrowLvl * 30; // 1.5s, 3s, 4.5s
                frostTarget.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, dur, frostArrowLvl - 1, true, true, true));
                frostTarget.setFreezeTicks(Math.max(frostTarget.getFreezeTicks(), dur + 20));
                frostTarget.setFireTicks(0); // Extinguish fire
                frostTarget.getWorld().spawnParticle(Particle.SNOWFLAKE, frostTarget.getLocation().add(0, 1, 0), 10, 0.4, 0.5, 0.4, 0.02);
            }

        }

        if (projectile instanceof Trident) {
            int thunderLvl = enchantManager.getEnchantLevel(weapon, EnchantmentType.THUNDERLORD);
            if (thunderLvl > 0 && event.getHitEntity() instanceof LivingEntity target) {
                double chance = switch (thunderLvl) {
                    case 1 -> 0.15; case 2 -> 0.25; case 3 -> 0.35; case 4 -> 0.45; default -> 0.55;
                };
                if (ThreadLocalRandom.current().nextDouble() < chance) {
                    target.getWorld().strikeLightning(target.getLocation());
                }
            }

            int tidalLvl = enchantManager.getEnchantLevel(weapon, EnchantmentType.TIDAL_WAVE);
            if (tidalLvl > 0 && event.getHitEntity() instanceof LivingEntity target) {
                double pushStr = switch (tidalLvl) { case 1 -> 3.0; case 2 -> 4.0; default -> 5.0; };
                Vector pushDir = target.getLocation().toVector()
                    .subtract(shooter.getLocation().toVector()).normalize()
                    .multiply(pushStr * 0.4).setY(0.4);
                target.setVelocity(pushDir);
            }

            // --- Tsunami (Phase 9) ---
            int tsunamiLvl = enchantManager.getEnchantLevel(weapon, EnchantmentType.TSUNAMI);
            if (tsunamiLvl > 0 && event.getHitEntity() instanceof LivingEntity tsunamiTarget) {
                World world = tsunamiTarget.getWorld();
                boolean inWaterOrRain = tsunamiTarget.isInWater() || world.hasStorm();
                if (inWaterOrRain) {
                    double pushDist = switch (tsunamiLvl) { case 1 -> 3.0; case 2 -> 5.0; default -> 7.0; };
                    double radius = pushDist * 0.6;
                    Location impactLoc = tsunamiTarget.getLocation();
                    for (Entity nearby : world.getNearbyEntities(impactLoc, radius, radius, radius)) {
                        if (nearby instanceof LivingEntity le && nearby != shooter) {
                            Vector push = le.getLocation().toVector().subtract(impactLoc.toVector()).normalize()
                                .multiply(pushDist * 0.5).setY(0.5);
                            le.setVelocity(push);
                            le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 0, true, true, true));
                        }
                    }
                    world.spawnParticle(Particle.SPLASH, impactLoc.clone().add(0, 0.5, 0), 30, radius * 0.5, 0.3, radius * 0.5, 0.1);
                    world.spawnParticle(Particle.BUBBLE, impactLoc.clone().add(0, 1, 0), 15, radius * 0.3, 0.3, radius * 0.3, 0.05);
                }
            }

        }
    }

    // ========================================================================
    // PROJECTILE LAUNCH — Homing arrows
    // ========================================================================
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player shooter)) return;
        Projectile projectile = event.getEntity();
        ItemStack weapon = shooter.getInventory().getItemInMainHand();

        if (!(projectile instanceof AbstractArrow)) return;
        if (weapon.getType() != Material.BOW && weapon.getType() != Material.CROSSBOW) return;

        int homingLvl = enchantManager.getEnchantLevel(weapon, EnchantmentType.HOMING);
        if (homingLvl <= 0) return;

        double searchRadius = homingLvl == 1 ? 5.0 : 8.0;
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (projectile.isDead() || projectile.isOnGround() || ticks > 60) { cancel(); return; }
                LivingEntity nearest = null;
                double nearestDist = searchRadius;
                for (Entity e : projectile.getNearbyEntities(searchRadius, searchRadius, searchRadius)) {
                    if (e instanceof Monster m && !m.isDead()) {
                        double d = projectile.getLocation().distance(m.getLocation());
                        if (d < nearestDist) { nearestDist = d; nearest = m; }
                    }
                }
                if (nearest != null) {
                    Vector toTarget = nearest.getLocation().add(0, nearest.getHeight() / 2, 0).toVector()
                        .subtract(projectile.getLocation().toVector()).normalize();
                    Vector current = projectile.getVelocity();
                    double speed = current.length();
                    Vector blended = current.normalize().multiply(0.7).add(toTarget.multiply(0.3)).normalize().multiply(speed);
                    projectile.setVelocity(blended);
                }
                ticks++;
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    // ========================================================================
    // BLOCK BREAK — Veinminer, Drill, Timber, Excavator, Auto-Smelt, Magnetism
    // ========================================================================
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (tool.getType() == Material.AIR) return;
        if (processingBlockBreak.contains(player.getUniqueId())) return;

        Block block = event.getBlock();

        int timberLvl = enchantManager.getEnchantLevel(tool, EnchantmentType.TIMBER);
        if (timberLvl > 0 && isLog(block.getType())) {
            // Block Battle Axes from using Timber
            BattleAxeManager bam = plugin.getBattleAxeManager();
            if (bam != null && bam.isBattleAxe(tool)) {
                // Battle axes cannot use Timber — do nothing
            } else {
                int max = switch (timberLvl) { case 1 -> 8; case 2 -> 16; default -> 64; };
                breakConnectedBlocks(player, block, max, EnchantmentEffectListener::isLog);
                decayNearbyLeaves(block.getLocation(), 6);
                return;
            }
        }

        int veinLvl = enchantManager.getEnchantLevel(tool, EnchantmentType.VEINMINER);
        if (veinLvl > 0 && isOre(block.getType())) {
            int max = switch (veinLvl) { case 1 -> 8; case 2 -> 16; default -> 32; };
            Material oreType = block.getType();
            breakConnectedBlocks(player, block, max, mat -> mat == oreType);
            return;
        }

        int drillLvl = enchantManager.getEnchantLevel(tool, EnchantmentType.DRILL);
        if (drillLvl > 0 && isPickaxeMineable(block.getType())) {
            breakDrillPattern(player, block, drillLvl);
            return;
        }

        int excLvl = enchantManager.getEnchantLevel(tool, EnchantmentType.EXCAVATOR);
        if (excLvl > 0 && isShovelMineable(block.getType())) {
            breakDrillPattern(player, block, excLvl);
            return;
        }

        // --- Earthshatter (Phase 9) ---
        int earthshatterLvl = enchantManager.getEnchantLevel(tool, EnchantmentType.EARTHSHATTER);
        if (earthshatterLvl > 0 && isShovelMineable(block.getType())) {
            Material targetMat = block.getType();
            int radius = earthshatterLvl; // 1, 2, or 3
            processingBlockBreak.add(player.getUniqueId());
            try {
                int broken = 0;
                int maxBlocks = radius * radius * radius * 4; // reasonable cap
                for (int x = -radius; x <= radius; x++) {
                    for (int y = -radius; y <= radius; y++) {
                        for (int z = -radius; z <= radius; z++) {
                            if (x == 0 && y == 0 && z == 0) continue;
                            Block relative = block.getRelative(x, y, z);
                            if (relative.getType() == targetMat && broken < maxBlocks) {
                                relative.breakNaturally(tool);
                                broken++;
                            }
                        }
                    }
                }
            } finally {
                processingBlockBreak.remove(player.getUniqueId());
            }
            block.getWorld().spawnParticle(Particle.BLOCK, block.getLocation().add(0.5, 0.5, 0.5),
                15, radius * 0.5, 0.3, radius * 0.5, 0.01, block.getBlockData());
            return;
        }


        int autoSmeltLvl = enchantManager.getEnchantLevel(tool, EnchantmentType.AUTO_SMELT);
        if (autoSmeltLvl > 0 && isSmeltableOre(block.getType())) {
            Material smelted = getSmeltedDrop(block.getType());
            if (smelted != null) {
                event.setDropItems(false);
                block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(smelted, 1));
            }
        }

        // --- Prospector (Phase 9) ---
        int prospectorLvl = enchantManager.getEnchantLevel(tool, EnchantmentType.PROSPECTOR);
        if (prospectorLvl > 0 && isOre(block.getType())) {
            double chance = prospectorLvl * 0.05; // 5%, 10%, 15%
            if (ThreadLocalRandom.current().nextDouble() < chance) {
                // Double the drops by breaking again virtually
                for (ItemStack drop : block.getDrops(tool, player)) {
                    block.getWorld().dropItemNaturally(block.getLocation(), drop.clone());
                }
                block.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, block.getLocation().add(0.5, 0.5, 0.5), 5, 0.3, 0.3, 0.3, 0.01);
            }
        }


        int magLvl = enchantManager.getEnchantLevel(tool, EnchantmentType.MAGNETISM);
        if (magLvl > 0) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                for (Entity entity : player.getNearbyEntities(5, 5, 5)) {
                    if (entity instanceof Item item) {
                        item.teleport(player.getLocation());
                        item.setPickupDelay(0);
                    }
                }
            }, 2L);
        }
    }

    // ========================================================================
    // PLAYER INTERACT — Harvester, Green Thumb, Replenish, Gravity Well
    // ========================================================================
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null) return;

        // Gravity Well (NEW v1.1.0) — Right-click with pickaxe
        int gravWellLvl = enchantManager.getEnchantLevel(item, EnchantmentType.GRAVITY_WELL);
        if (gravWellLvl > 0 && item.getType().name().endsWith("_PICKAXE")) {
            long now = System.currentTimeMillis();
            Long cdEnd = gravityWellCooldowns.get(player.getUniqueId());
            if (cdEnd != null && now < cdEnd) {
                long remaining = (cdEnd - now) / 1000;
                player.sendActionBar(Component.text("Gravity Well cooldown: " + remaining + "s", NamedTextColor.RED));
                return;
            }
            int radius = gravWellLvl == 1 ? 8 : 12;
            for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
                if (entity instanceof Item itemEntity) {
                    itemEntity.teleport(player.getLocation());
                    itemEntity.setPickupDelay(0);
                }
            }
            gravityWellCooldowns.put(player.getUniqueId(), now + 3000L);
            player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation().add(0, 1, 0), 15, 1, 0.5, 1, 0.5);
            player.sendActionBar(Component.text("Gravity Well!", NamedTextColor.DARK_AQUA));
            event.setCancelled(true);
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block clicked = event.getClickedBlock();
        if (clicked == null) return;

        int harvesterLvl = enchantManager.getEnchantLevel(item, EnchantmentType.HARVESTER);
        if (harvesterLvl > 0 && isHoe(item.getType())) {
            int radius = switch (harvesterLvl) { case 1 -> 1; case 2 -> 2; default -> 3; };
            harvestArea(player, clicked, radius);
            event.setCancelled(true);
            return;
        }

        int greenThumbLvl = enchantManager.getEnchantLevel(item, EnchantmentType.GREEN_THUMB);
        if (greenThumbLvl > 0 && isHoe(item.getType())) {
            if (clicked.getType() == Material.GRASS_BLOCK || clicked.getType() == Material.DIRT) {
                for (int x = -2; x <= 2; x++) {
                    for (int z = -2; z <= 2; z++) {
                        Block target = clicked.getRelative(x, 0, z);
                        if (target.getType() == Material.GRASS_BLOCK || target.getType() == Material.DIRT) {
                            target.setType(Material.FARMLAND);
                        }
                    }
                }
                event.setCancelled(true);
            } else if (clicked.getBlockData() instanceof Ageable) {
                clicked.applyBoneMeal(BlockFace.UP);
                event.setCancelled(true);
            }
            return;
        }

        int replenishLvl = enchantManager.getEnchantLevel(item, EnchantmentType.REPLENISH);
        if (replenishLvl > 0 && isShovel(item.getType())) {
            if (clicked.getType() == Material.SHORT_GRASS || clicked.getType() == Material.TALL_GRASS) {
                if (ThreadLocalRandom.current().nextDouble() < 0.15) {
                    clicked.getWorld().dropItemNaturally(clicked.getLocation(), new ItemStack(Material.WHEAT_SEEDS, 1));
                }
            } else if (clicked.getType() == Material.SAND && isNearWater(clicked)) {
                if (ThreadLocalRandom.current().nextDouble() < 0.10) {
                    clicked.getWorld().dropItemNaturally(clicked.getLocation(), new ItemStack(Material.PRISMARINE_SHARD, 1));
                }
            }
        }
    }

    // ========================================================================
    // ELYTRA BOOST
    // ========================================================================
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        if (!event.isSneaking()) return;
        Player player = event.getPlayer();
        if (!player.isGliding()) return;

        ItemStack chest = player.getInventory().getChestplate();
        if (chest == null || chest.getType() != Material.ELYTRA) return;

        int boostLvl = enchantManager.getEnchantLevel(chest, EnchantmentType.BOOST);
        if (boostLvl <= 0) return;

        long now = System.currentTimeMillis();
        Long cdEnd = boostCooldowns.get(player.getUniqueId());
        if (cdEnd != null && now < cdEnd) {
            long remaining = (cdEnd - now) / 1000;
            player.sendActionBar(Component.text("Boost cooldown: " + remaining + "s", NamedTextColor.RED));
            return;
        }

        Vector dir = player.getLocation().getDirection().normalize();
        double mult = switch (boostLvl) { case 1 -> 1.5; case 2 -> 2.0; default -> 2.5; };
        player.setVelocity(dir.multiply(mult));

        int cdSec = switch (boostLvl) { case 1 -> 8; case 2 -> 6; default -> 4; };
        boostCooldowns.put(player.getUniqueId(), now + (cdSec * 1000L));
        player.getWorld().spawnParticle(Particle.FIREWORK, player.getLocation(), 10, 0.5, 0.5, 0.5, 0.1);
    }

    // ========================================================================
    // DAMAGE EVENT — Cushion, Stomp
    // ========================================================================
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        if (event.getCause() == EntityDamageEvent.DamageCause.FLY_INTO_WALL
            || event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            ItemStack chest = player.getInventory().getChestplate();
            if (chest != null && chest.getType() == Material.ELYTRA) {
                if (enchantManager.getEnchantLevel(chest, EnchantmentType.CUSHION) > 0) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            ItemStack boots = player.getInventory().getBoots();
            if (boots != null) {
                int stompLvl = enchantManager.getEnchantLevel(boots, EnchantmentType.STOMP);
                if (stompLvl > 0) {
                    event.setCancelled(true);
                    double aoeDmg = stompLvl * 2.0;
                    for (Entity nearby : player.getNearbyEntities(3, 3, 3)) {
                        if (nearby instanceof LivingEntity le && nearby != player) {
                            le.damage(aoeDmg, player);
                        }
                    }
                    player.getWorld().spawnParticle(Particle.EXPLOSION, player.getLocation(), 3, 1, 0.5, 1, 0.01);
                }
            }
        }
    }

    // ========================================================================
    // GLIDER + SUPER ELYTRA DURABILITY
    // ========================================================================
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerItemDamage(PlayerItemDamageEvent event) {
        ItemStack item = event.getItem();
        if (item.getType() != Material.ELYTRA) return;

        if (enchantManager.getEnchantLevel(item, EnchantmentType.GLIDER) > 0) {
            if (ThreadLocalRandom.current().nextBoolean()) {
                event.setCancelled(true);
                return;
            }
        }

        int elytraTier = plugin.getSuperToolManager().getElytraDurabilityTier(item);
        if (elytraTier > 0) {
            double saveChance = switch (elytraTier) {
                case 1 -> 0.30; case 2 -> 0.50; case 3 -> 0.70; default -> 0;
            };
            if (ThreadLocalRandom.current().nextDouble() < saveChance) {
                event.setCancelled(true);
            }
        }
    }

    // ========================================================================
    // PASSIVE EFFECTS — Armor/held equip changes
    // ========================================================================
    @EventHandler(priority = EventPriority.MONITOR)
    public void onArmorChange(PlayerArmorChangeEvent event) {
        updatePassiveEffects(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemHeld(PlayerItemHeldEvent event) {
        plugin.getServer().getScheduler().runTask(plugin, () -> updatePassiveEffects(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> updatePassiveEffects(event.getPlayer()), 5L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> updatePassiveEffects(event.getPlayer()), 5L);
    }

    private void updatePassiveEffects(Player player) {
        if (!player.isOnline()) return;

        ItemStack helmet = player.getInventory().getHelmet();
        boolean hasNV = helmet != null && enchantManager.getEnchantLevel(helmet, EnchantmentType.NIGHT_VISION) > 0;
        int aquaLvl = helmet != null ? enchantManager.getEnchantLevel(helmet, EnchantmentType.AQUA_LUNGS) : 0;

        applyOrRemoveEffect(player, PotionEffectType.NIGHT_VISION, hasNV, 0);
        applyOrRemoveEffect(player, PotionEffectType.WATER_BREATHING, aquaLvl > 0, 0);
        applyOrRemoveEffect(player, PotionEffectType.CONDUIT_POWER, aquaLvl >= 2, 0);

        ItemStack leggings = player.getInventory().getLeggings();
        int swiftfootLvl = leggings != null ? enchantManager.getEnchantLevel(leggings, EnchantmentType.SWIFTFOOT) : 0;
        int speedAmpFromLegs = swiftfootLvl >= 3 ? 1 : (swiftfootLvl > 0 ? 0 : -1);

        ItemStack boots = player.getInventory().getBoots();
        int leapingLvl = boots != null ? enchantManager.getEnchantLevel(boots, EnchantmentType.LEAPING) : 0;
        applyOrRemoveEffect(player, PotionEffectType.JUMP_BOOST, leapingLvl > 0, leapingLvl - 1);

        ItemStack mainHand = player.getInventory().getItemInMainHand();
        boolean holdingTrident = mainHand.getType() == Material.TRIDENT;
        int swiftnessLvl = holdingTrident ? enchantManager.getEnchantLevel(mainHand, EnchantmentType.SWIFTNESS) : 0;
        int vitalityLvl = holdingTrident ? enchantManager.getEnchantLevel(mainHand, EnchantmentType.VITALITY) : 0;

        int speedAmpFromTrident = swiftnessLvl >= 3 ? 1 : (swiftnessLvl > 0 ? 0 : -1);
        if (swiftnessLvl >= 2 && leapingLvl == 0) {
            applyOrRemoveEffect(player, PotionEffectType.JUMP_BOOST, true, 0);
        }

        int bestSpeedAmp = Math.max(speedAmpFromLegs, speedAmpFromTrident);
        applyOrRemoveEffect(player, PotionEffectType.SPEED, bestSpeedAmp >= 0, Math.max(0, bestSpeedAmp));

        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.getModifiers().stream()
                .filter(m -> m.getKey().equals(vitalityModKey))
                .forEach(maxHealth::removeModifier);
            if (vitalityLvl > 0) {
                double bonus = vitalityLvl * 2.0;
                double current = maxHealth.getBaseValue();
                bonus = Math.min(bonus, 40.0 - current);
                if (bonus > 0) {
                    maxHealth.addModifier(new AttributeModifier(
                        vitalityModKey, bonus, AttributeModifier.Operation.ADD_NUMBER));
                }
            }
        }

        // Axe nerf: apply/remove slow attack speed when holding regular axe
        if (plugin.getConfigManager().isAxeNerfEnabled()) {
            String matName = mainHand.getType().name();
            boolean holdingRegularAxe = matName.endsWith("_AXE");
            BattleAxeManager bam = plugin.getBattleAxeManager();
            if (holdingRegularAxe && bam != null && bam.isBattleAxe(mainHand)) {
                holdingRegularAxe = false; // It's a battle axe, not a regular axe
            }

            AttributeInstance attackSpeed = player.getAttribute(Attribute.ATTACK_SPEED);
            if (attackSpeed != null) {
                attackSpeed.getModifiers().stream()
                    .filter(m -> m.getKey().equals(axeNerfSpeedKey))
                    .forEach(attackSpeed::removeModifier);
                if (holdingRegularAxe) {
                    // Set attack speed to 0.5 by applying a large negative modifier
                    // Player base = 4.0, vanilla axe modifier already in place.
                    // We want total = 0.5, so we add (0.5 - current value)
                    // But it's simpler: just add a big negative to push it down
                    double currentSpeed = attackSpeed.getValue();
                    double targetSpeed = plugin.getConfigManager().getAxeNerfAttackSpeed();
                    double needed = targetSpeed - currentSpeed;
                    if (needed < 0) {
                        attackSpeed.addModifier(new AttributeModifier(
                            axeNerfSpeedKey, needed, AttributeModifier.Operation.ADD_NUMBER));
                    }
                }
            }
        }
    }

    // ========================================================================
    // MOMENTUM (NEW v1.1.0) — Speed boost while sprinting
    // ========================================================================
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
            && event.getFrom().getBlockY() == event.getTo().getBlockY()
            && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;

        Player player = event.getPlayer();

        // Tidal Wave: Dolphin's Grace in water
        if (player.isInWater()) {
            ItemStack mainHand = player.getInventory().getItemInMainHand();
            if (mainHand.getType() == Material.TRIDENT) {
                int tidalLvl = enchantManager.getEnchantLevel(mainHand, EnchantmentType.TIDAL_WAVE);
                if (tidalLvl > 0) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 60, 0, true, false, false));
                }
            }
        }

        // Momentum enchantment
        ItemStack boots = player.getInventory().getBoots();
        if (boots == null) return;
        int momentumLvl = enchantManager.getEnchantLevel(boots, EnchantmentType.MOMENTUM);
        if (momentumLvl <= 0) {
            momentumTicks.remove(player.getUniqueId());
            return;
        }

        if (player.isSprinting()) {
            int ticks = momentumTicks.getOrDefault(player.getUniqueId(), 0) + 1;
            momentumTicks.put(player.getUniqueId(), ticks);

            // Every 20 ticks (1 second), increase speed
            double speedPerSec = switch (momentumLvl) { case 1 -> 0.05; case 2 -> 0.07; default -> 0.10; };
            double maxSpeed = switch (momentumLvl) { case 1 -> 0.15; case 2 -> 0.21; default -> 0.30; };
            int seconds = ticks / 20;
            double speedBoost = Math.min(seconds * speedPerSec, maxSpeed);

            if (speedBoost > 0) {
                // Apply as speed potion — amplifier 0 = 20% speed, so we scale
                // Speed I = +20%, Speed II = +40%. We want arbitrary %.
                // Use attribute modifier instead
                AttributeInstance speedAttr = player.getAttribute(Attribute.MOVEMENT_SPEED);
                if (speedAttr != null) {
                    NamespacedKey momentumKey = new NamespacedKey(plugin, "momentum_speed");
                    speedAttr.getModifiers().stream()
                        .filter(m -> m.getKey().equals(momentumKey))
                        .forEach(speedAttr::removeModifier);
                    speedAttr.addModifier(new AttributeModifier(
                        momentumKey, speedBoost, AttributeModifier.Operation.MULTIPLY_SCALAR_1));
                }
            }
        } else {
            // Not sprinting — reset momentum
            if (momentumTicks.containsKey(player.getUniqueId())) {
                momentumTicks.remove(player.getUniqueId());
                AttributeInstance speedAttr = player.getAttribute(Attribute.MOVEMENT_SPEED);
                if (speedAttr != null) {
                    NamespacedKey momentumKey = new NamespacedKey(plugin, "momentum_speed");
                    speedAttr.getModifiers().stream()
                        .filter(m -> m.getKey().equals(momentumKey))
                        .forEach(speedAttr::removeModifier);
                }
            }
        }
    }

    private void applyOrRemoveEffect(Player player, PotionEffectType type, boolean shouldHave, int amplifier) {
        if (shouldHave) {
            player.addPotionEffect(new PotionEffect(type,
                PotionEffect.INFINITE_DURATION, Math.max(0, amplifier), true, false, false));
        } else {
            PotionEffect existing = player.getPotionEffect(type);
            if (existing != null && existing.isAmbient() && !existing.hasParticles()) {
                player.removePotionEffect(type);
            }
        }
    }

    // ========================================================================
    // PLAYER QUIT — Clean up
    // ========================================================================
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        boostCooldowns.remove(uuid);
        gravityWellCooldowns.remove(uuid);
        momentumTicks.remove(uuid);

        // Remove momentum speed modifier
        Player player = event.getPlayer();
        AttributeInstance speedAttr = player.getAttribute(Attribute.MOVEMENT_SPEED);
        if (speedAttr != null) {
            NamespacedKey momentumKey = new NamespacedKey(plugin, "momentum_speed");
            speedAttr.getModifiers().stream()
                .filter(m -> m.getKey().equals(momentumKey))
                .forEach(speedAttr::removeModifier);
        }

        // Remove axe nerf modifier
        AttributeInstance attackSpeed = player.getAttribute(Attribute.ATTACK_SPEED);
        if (attackSpeed != null) {
            attackSpeed.getModifiers().stream()
                .filter(m -> m.getKey().equals(axeNerfSpeedKey))
                .forEach(attackSpeed::removeModifier);
        }
    }

    // ========================================================================
    // UTILITIES
    // ========================================================================
    private Player getPlayerAttacker(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player p) return p;
        if (event.getDamager() instanceof Projectile proj && proj.getShooter() instanceof Player p) return p;
        return null;
    }

    private static boolean isLog(Material mat) {
        String name = mat.name();
        return name.endsWith("_LOG") || name.endsWith("_WOOD")
            || mat == Material.CRIMSON_STEM || mat == Material.WARPED_STEM
            || mat == Material.STRIPPED_CRIMSON_STEM || mat == Material.STRIPPED_WARPED_STEM;
    }

    private static boolean isOre(Material mat) {
        return mat.name().endsWith("_ORE") || mat == Material.ANCIENT_DEBRIS;
    }

    private static boolean isPickaxeMineable(Material mat) {
        String name = mat.name();
        return name.endsWith("_ORE") || name.contains("STONE") || name.contains("DEEPSLATE")
            || name.contains("BRICK") || name.contains("SANDSTONE") || name.contains("PRISMARINE")
            || mat == Material.OBSIDIAN || mat == Material.NETHERRACK || mat == Material.BASALT
            || mat == Material.BLACKSTONE || mat == Material.END_STONE || mat == Material.COBBLESTONE
            || mat == Material.ANDESITE || mat == Material.DIORITE || mat == Material.GRANITE
            || mat == Material.TERRACOTTA || mat == Material.ANCIENT_DEBRIS;
    }

    private static boolean isShovelMineable(Material mat) {
        return switch (mat) {
            case DIRT, GRASS_BLOCK, SAND, RED_SAND, GRAVEL, CLAY,
                 SOUL_SAND, SOUL_SOIL, SNOW_BLOCK, SNOW, MUD,
                 MUDDY_MANGROVE_ROOTS, MYCELIUM, PODZOL, COARSE_DIRT,
                 ROOTED_DIRT, DIRT_PATH, FARMLAND -> true;
            default -> false;
        };
    }

    private static boolean isSmeltableOre(Material mat) {
        return switch (mat) {
            case IRON_ORE, DEEPSLATE_IRON_ORE, GOLD_ORE, DEEPSLATE_GOLD_ORE,
                 COPPER_ORE, DEEPSLATE_COPPER_ORE, ANCIENT_DEBRIS -> true;
            default -> false;
        };
    }

    private static boolean isHoe(Material mat) { return mat.name().endsWith("_HOE"); }
    private static boolean isShovel(Material mat) { return mat.name().endsWith("_SHOVEL"); }

    private Material getSmeltedDrop(Material ore) {
        return switch (ore) {
            case IRON_ORE, DEEPSLATE_IRON_ORE -> Material.IRON_INGOT;
            case GOLD_ORE, DEEPSLATE_GOLD_ORE -> Material.GOLD_INGOT;
            case COPPER_ORE, DEEPSLATE_COPPER_ORE -> Material.COPPER_INGOT;
            case ANCIENT_DEBRIS -> Material.NETHERITE_SCRAP;
            default -> null;
        };
    }

    private void breakConnectedBlocks(Player player, Block origin, int maxBlocks,
                                       java.util.function.Predicate<Material> matcher) {
        processingBlockBreak.add(player.getUniqueId());
        try {
            Queue<Block> queue = new LinkedList<>();
            Set<Block> visited = new HashSet<>();
            queue.add(origin);
            visited.add(origin);
            int broken = 0;
            ItemStack tool = player.getInventory().getItemInMainHand();
            while (!queue.isEmpty() && broken < maxBlocks) {
                Block current = queue.poll();
                if (broken > 0) current.breakNaturally(tool);
                broken++;
                for (BlockFace face : new BlockFace[]{BlockFace.UP, BlockFace.DOWN,
                    BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST}) {
                    Block neighbor = current.getRelative(face);
                    if (!visited.contains(neighbor) && matcher.test(neighbor.getType())) {
                        visited.add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }
        } finally {
            processingBlockBreak.remove(player.getUniqueId());
        }
    }

    private void breakDrillPattern(Player player, Block origin, int level) {
        processingBlockBreak.add(player.getUniqueId());
        try {
            BlockFace facing = getPlayerFacing(player);
            List<Block> toBreak = new ArrayList<>();
            if (level == 1) {
                toBreak.add(origin.getRelative(BlockFace.UP));
                toBreak.add(origin.getRelative(BlockFace.DOWN));
            } else if (level == 2) {
                addPlaneBlocks(toBreak, origin, facing, 1);
            } else {
                addPlaneBlocks(toBreak, origin, facing, 1);
                Block behind = origin.getRelative(facing);
                addPlaneBlocks(toBreak, behind, facing, 1);
            }
            ItemStack tool = player.getInventory().getItemInMainHand();
            for (Block b : toBreak) {
                if (b.equals(origin)) continue;
                if (b.getType() != Material.AIR && b.getType() != Material.BEDROCK
                    && !b.getType().name().contains("COMMAND") && b.getType() != Material.BARRIER) {
                    b.breakNaturally(tool);
                }
            }
        } finally {
            processingBlockBreak.remove(player.getUniqueId());
        }
    }

    private void addPlaneBlocks(List<Block> list, Block center, BlockFace facing, int radius) {
        BlockFace[] axes = getPerpendicular(facing);
        for (int a = -radius; a <= radius; a++) {
            for (int b = -radius; b <= radius; b++) {
                list.add(center.getRelative(axes[0], a).getRelative(axes[1], b));
            }
        }
    }

    private BlockFace[] getPerpendicular(BlockFace face) {
        return switch (face) {
            case NORTH, SOUTH -> new BlockFace[]{BlockFace.EAST, BlockFace.UP};
            case EAST, WEST -> new BlockFace[]{BlockFace.NORTH, BlockFace.UP};
            case UP, DOWN -> new BlockFace[]{BlockFace.EAST, BlockFace.NORTH};
            default -> new BlockFace[]{BlockFace.EAST, BlockFace.UP};
        };
    }

    private BlockFace getPlayerFacing(Player player) {
        float pitch = player.getLocation().getPitch();
        if (pitch < -45) return BlockFace.UP;
        if (pitch > 45) return BlockFace.DOWN;
        float yaw = player.getLocation().getYaw();
        if (yaw < 0) yaw += 360;
        if (yaw < 45 || yaw >= 315) return BlockFace.SOUTH;
        if (yaw < 135) return BlockFace.WEST;
        if (yaw < 225) return BlockFace.NORTH;
        return BlockFace.EAST;
    }

    private void harvestArea(Player player, Block center, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                Block block = center.getRelative(x, 0, z);
                if (block.getBlockData() instanceof Ageable ageable) {
                    if (ageable.getAge() >= ageable.getMaximumAge()) {
                        block.breakNaturally(player.getInventory().getItemInMainHand());
                        block.setType(block.getType()); // replant
                    }
                }
            }
        }
    }

    private void decayNearbyLeaves(Location loc, int radius) {
        World world = loc.getWorld();
        if (world == null) return;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block b = world.getBlockAt(loc.getBlockX() + x, loc.getBlockY() + y, loc.getBlockZ() + z);
                    if (b.getType().name().endsWith("_LEAVES")) {
                        // Trigger leaf decay by updating the block
                        b.getState().update(true);
                    }
                }
            }
        }
    }

    private boolean isNearWater(Block block) {
        for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST,
            BlockFace.WEST, BlockFace.UP, BlockFace.DOWN}) {
            if (block.getRelative(face).getType() == Material.WATER) return true;
        }
        return false;
    }
}

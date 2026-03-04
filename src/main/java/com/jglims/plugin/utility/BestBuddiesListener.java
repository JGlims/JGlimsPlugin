package com.jglims.plugin.utility;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.config.ConfigManager;
import com.jglims.plugin.enchantments.EnchantmentType;

/**
 * BestBuddiesListener — handles the custom BEST_BUDDIES enchantment on wolf armor.
 *
 * When a wolf wears wolf armor that has the BEST_BUDDIES PDC flag:
 *   - 95 % incoming damage reduction (configurable)
 *   - Wolf deals 0 melee damage (pacifist companion)
 *   - Permanent Regeneration II aura
 *   - Heart particles periodically
 */
public class BestBuddiesListener implements Listener {

    private final JGlimsPlugin plugin;
    private final ConfigManager config;
    private final NamespacedKey bestBuddiesKey;
    private final NamespacedKey bestBuddiesAppliedKey;

    // Track wolves with active Best Buddies for the regen task
    private final Map<UUID, Integer> regenTasks = new HashMap<>();

    // Unique UUID for the attack-damage attribute modifier
    private static final String BEST_BUDDIES_MODIFIER_NAME = "best_buddies_zero_damage";

    public BestBuddiesListener(JGlimsPlugin plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
        this.bestBuddiesKey = new NamespacedKey(plugin, EnchantmentType.BEST_BUDDIES.name().toLowerCase());
        this.bestBuddiesAppliedKey = new NamespacedKey(plugin, "best_buddies_applied");

        // Start a repeating task to scan for Best Buddies wolves and maintain their effects
        startGlobalScanTask();
    }

    // ============================================================
    // Check if an ItemStack has the BEST_BUDDIES enchantment
    // ============================================================

    private boolean hasBestBuddies(ItemStack item) {
        if (item == null || item.getType().isAir()) return false;
        if (!item.hasItemMeta()) return false;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        return pdc.has(bestBuddiesKey, PersistentDataType.INTEGER);
    }

    // ============================================================
    // Get the wolf's body armor via EquipmentSlot.BODY
    // ============================================================

    private ItemStack getWolfBodyArmor(Wolf wolf) {
        EntityEquipment equipment = wolf.getEquipment();
        if (equipment == null) return null;
        return equipment.getItem(EquipmentSlot.BODY);
    }

    // ============================================================
    // Check if a wolf currently has Best Buddies armor equipped
    // ============================================================

    private boolean wolfHasBestBuddies(Wolf wolf) {
        ItemStack bodyArmor = getWolfBodyArmor(wolf);
        return hasBestBuddies(bodyArmor);
    }

    // ============================================================
    // EVENT: Damage reduction for wolves wearing Best Buddies armor
    // ============================================================

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onWolfDamaged(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Wolf wolf)) return;
        if (!wolf.isTamed()) return;
        if (!wolfHasBestBuddies(wolf)) return;

        // Apply configured damage reduction (default 95%)
        double reduction = config.getDogArmorReduction();
        double reduced = event.getDamage() * (1.0 - reduction);
        event.setDamage(Math.max(0, reduced));

        // Visual feedback
        Location loc = wolf.getLocation().add(0, 0.5, 0);
        wolf.getWorld().spawnParticle(Particle.HEART, loc, 2, 0.3, 0.3, 0.3, 0);
        wolf.getWorld().spawnParticle(Particle.DUST, loc, 5,
                0.4, 0.4, 0.4, 0,
                new Particle.DustOptions(Color.fromRGB(100, 200, 255), 1.2f));
    }

    // ============================================================
    // EVENT: Wolves with Best Buddies deal 0 damage (pacifist)
    // ============================================================

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onWolfAttack(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        if (!(damager instanceof Wolf wolf)) return;
        if (!wolf.isTamed()) return;
        if (!wolfHasBestBuddies(wolf)) return;

        // Cancel all damage dealt by the wolf
        event.setDamage(0);
        event.setCancelled(true);

        // Friendly particles instead of attack
        Location loc = wolf.getLocation().add(0, 0.8, 0);
        wolf.getWorld().spawnParticle(Particle.HEART, loc, 3, 0.4, 0.3, 0.4, 0);
    }

    // ============================================================
    // EVENT: When player right-clicks a wolf (equipping armor),
    // apply Best Buddies effects if the armor has the enchantment
    // ============================================================

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteractWolf(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Wolf wolf)) return;
        if (!wolf.isTamed()) return;

        // Delayed check — the armor might not be applied until after this event
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (wolfHasBestBuddies(wolf)) {
                applyBestBuddiesEffects(wolf);
                Player owner = (Player) wolf.getOwner();
                if (owner != null && owner.isOnline()) {
                    owner.sendActionBar("\u00a7b\u2764 Best Buddies armor applied! Your wolf is now protected. \u2764");
                    owner.playSound(owner.getLocation(), Sound.ENTITY_WOLF_WHINE, 0.5f, 1.5f);
                    owner.playSound(owner.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.8f);
                }
            } else {
                removeBestBuddiesEffects(wolf);
            }
        }, 2L);
    }

    // ============================================================
    // Apply Best Buddies passive effects to a wolf
    // ============================================================

    private void applyBestBuddiesEffects(Wolf wolf) {
        UUID wolfId = wolf.getUniqueId();

        // Mark wolf in PDC
        PersistentDataContainer pdc = wolf.getPersistentDataContainer();
        pdc.set(bestBuddiesAppliedKey, PersistentDataType.BYTE, (byte) 1);

        // Apply permanent Regeneration II
        wolf.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 1, true, false, false));

        // Start heart particle task if not already running
        if (!regenTasks.containsKey(wolfId)) {
            int taskId = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!wolf.isValid() || wolf.isDead() || !wolfHasBestBuddies(wolf)) {
                        removeBestBuddiesEffects(wolf);
                        cancel();
                        return;
                    }
                    // Heart particles every 2 seconds
                    Location loc = wolf.getLocation().add(0, 0.8, 0);
                    wolf.getWorld().spawnParticle(Particle.HEART, loc, 1, 0.2, 0.2, 0.2, 0);
                    wolf.getWorld().spawnParticle(Particle.DUST, loc, 3,
                            0.3, 0.3, 0.3, 0,
                            new Particle.DustOptions(Color.fromRGB(255, 150, 200), 0.8f));

                    // Refresh regen if needed
                    if (!wolf.hasPotionEffect(PotionEffectType.REGENERATION)) {
                        wolf.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 1, true, false, false));
                    }
                }
            }.runTaskTimer(plugin, 0L, 40L).getTaskId();
            regenTasks.put(wolfId, taskId);
        }
    }

    // ============================================================
    // Remove Best Buddies effects from a wolf
    // ============================================================

    private void removeBestBuddiesEffects(Wolf wolf) {
        UUID wolfId = wolf.getUniqueId();

        // Remove PDC marker
        PersistentDataContainer pdc = wolf.getPersistentDataContainer();
        pdc.remove(bestBuddiesAppliedKey);

        // Remove regen
        wolf.removePotionEffect(PotionEffectType.REGENERATION);

        // Cancel particle task
        Integer taskId = regenTasks.remove(wolfId);
        if (taskId != null) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
    }

    // ============================================================
    // EVENT: Re-apply effects when chunks load (persistence)
    // ============================================================

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        for (Entity entity : event.getChunk().getEntities()) {
            if (entity instanceof Wolf wolf && wolf.isTamed()) {
                if (wolfHasBestBuddies(wolf)) {
                    Bukkit.getScheduler().runTaskLater(plugin, () -> applyBestBuddiesEffects(wolf), 5L);
                }
            }
        }
    }

    // ============================================================
    // Global scan task — catches any wolves that might have been
    // missed by events (world load, teleport, etc.)
    // ============================================================

    private void startGlobalScanTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (var world : Bukkit.getWorlds()) {
                    for (Wolf wolf : world.getEntitiesByClass(Wolf.class)) {
                        if (!wolf.isTamed()) continue;
                        if (wolfHasBestBuddies(wolf) && !regenTasks.containsKey(wolf.getUniqueId())) {
                            applyBestBuddiesEffects(wolf);
                        } else if (!wolfHasBestBuddies(wolf) && regenTasks.containsKey(wolf.getUniqueId())) {
                            removeBestBuddiesEffects(wolf);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 100L, 200L); // Every 10 seconds
    }
}

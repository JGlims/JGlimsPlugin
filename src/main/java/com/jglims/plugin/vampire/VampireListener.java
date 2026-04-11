package com.jglims.plugin.vampire;

import com.jglims.plugin.JGlimsPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Event listener for all vampire mechanics: transformation, restrictions,
 * day/night cycle effects, item consumption, blood drops, armor blocking,
 * potion blocking, and keep inventory.
 */
public class VampireListener implements Listener {

    private final JGlimsPlugin plugin;
    private final VampireManager manager;

    public VampireListener(JGlimsPlugin plugin, VampireManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    /**
     * Starts the periodic task that applies vampire day/night effects.
     */
    public void startDayNightCycle() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    if (!manager.isVampire(player)) continue;
                    applyVampireEffects(player);
                }
            }
        }.runTaskTimer(plugin, 40L, 40L); // Every 2 seconds
    }

    // ── Transformation via Vampire Essence ─────────────────────────────

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (manager.isVampireEssence(item)) {
            event.setCancelled(true);
            manager.transformPlayer(player);
            item.setAmount(item.getAmount() - 1);
            return;
        }

        if (!manager.isVampire(player)) return;

        if (manager.isVampireBlood(item)) {
            event.setCancelled(true);
            manager.consumeBlood(player);
            item.setAmount(item.getAmount() - 1);
        } else if (manager.isVampireEvolver(item)) {
            event.setCancelled(true);
            manager.consumeEvolver(player);
            item.setAmount(item.getAmount() - 1);
        } else if (manager.isSuperBlood(item)) {
            event.setCancelled(true);
            manager.consumeSuperBlood(player);
            item.setAmount(item.getAmount() - 1);
        } else if (manager.isVampireRingItem(item)) {
            event.setCancelled(true);
            manager.applyVampireRing(player);
            item.setAmount(item.getAmount() - 1);
        }
    }

    // ── Armor Restriction ──────────────────────────────────────────────

    /**
     * Prevents vampires from equipping armor (except Elytra in chestplate slot).
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!manager.isVampire(player)) return;

        int slot = event.getRawSlot();
        // Armor slots in player inventory: 5=helmet, 6=chestplate, 7=leggings, 8=boots
        if (slot >= 5 && slot <= 8) {
            ItemStack cursor = event.getCursor();
            // Allow Elytra and Nightwalker Cloak in chestplate slot
            if (slot == 6) {
                if (cursor != null && cursor.getType() == Material.ELYTRA) return;
                if (cursor != null && cursor.hasItemMeta()
                        && cursor.getItemMeta().getPersistentDataContainer().has(
                                new org.bukkit.NamespacedKey(plugin, "crafted_item_id"),
                                org.bukkit.persistence.PersistentDataType.STRING)) {
                    String id = cursor.getItemMeta().getPersistentDataContainer().get(
                            new org.bukkit.NamespacedKey(plugin, "crafted_item_id"),
                            org.bukkit.persistence.PersistentDataType.STRING);
                    if ("nightwalker_cloak".equals(id)) return;
                }
            }
            // Block all other armor
            if (isArmor(cursor)) {
                event.setCancelled(true);
                player.sendMessage(Component.text("Vampires cannot wear armor!", NamedTextColor.RED));
            }
        }
    }

    // ── Potion Restriction ─────────────────────────────────────────────

    /**
     * Prevents vampires from gaining effects from self-consumed potions.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        if (!manager.isVampire(player)) return;

        Material type = event.getItem().getType();
        if (type == Material.POTION || type == Material.SPLASH_POTION
                || type == Material.LINGERING_POTION) {
            // Remove all potion effects after a tick (after vanilla applies them)
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (PotionEffect effect : player.getActivePotionEffects()) {
                        // Keep vampire-system-applied effects (Night Vision, etc.)
                        if (effect.getType() == PotionEffectType.NIGHT_VISION) continue;
                        if (effect.getType() == PotionEffectType.SPEED) continue;
                        if (effect.getType() == PotionEffectType.JUMP_BOOST) continue;
                        // Remove player-consumed effects
                        if (effect.getDuration() < 600 * 20) { // Less than 10 min = likely self-consumed
                            player.removePotionEffect(effect.getType());
                        }
                    }
                    player.sendMessage(Component.text("Potions have no effect on vampires!",
                            NamedTextColor.RED));
                }
            }.runTaskLater(plugin, 1L);
        }
    }

    // ── Keep Inventory ─────────────────────────────────────────────────

    /**
     * Vampires always keep inventory on death.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (!manager.isVampire(player)) return;

        event.setKeepInventory(true);
        event.setKeepLevel(true);
        event.getDrops().clear();
        event.setDroppedExp(0);
    }

    // ── Fall Damage Reduction ──────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!manager.isVampire(player)) return;

        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            VampireState state = manager.getOrCreateState(player.getUniqueId());
            double reduction = state.getLevel().getFallDamageReduction();
            event.setDamage(event.getDamage() * (1.0 - reduction));
        }
    }

    // ── Blood Drops from Villager Kills ────────────────────────────────

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();
        if (killer == null || !manager.isVampire(killer)) return;

        // Villager kills drop Vampire Blood
        if (entity instanceof Villager) {
            int amount = 1 + (Math.random() < 0.5 ? 1 : 0);
            for (int i = 0; i < amount; i++) {
                entity.getWorld().dropItemNaturally(entity.getLocation(),
                        manager.createVampireBlood());
            }
        }

        // Abyss dimension kills drop Super Blood
        if (entity.getWorld().getName().equals("world_abyss")) {
            entity.getWorld().dropItemNaturally(entity.getLocation(),
                    manager.createSuperBlood());
        }
    }

    // ── Day/Night Cycle Effects ────────────────────────────────────────

    /**
     * Applies appropriate vampire buffs or debuffs based on the current
     * time of day and environment.
     */
    private void applyVampireEffects(Player player) {
        VampireState state = manager.getOrCreateState(player.getUniqueId());
        VampireLevel level = state.getLevel();
        boolean isNight = isNighttime(player);

        if (isNight) {
            applyNightBuffs(player, level);
        } else {
            applyDayDebuffs(player, state);
        }
    }

    private void applyNightBuffs(Player player, VampireLevel level) {
        // Night Vision
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION,
                100, 0, false, false, false));

        // Speed bonus
        int speedLevel = Math.max(0, (int) (level.getSpeedBonus()) - 1);
        if (speedLevel >= 0) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,
                    100, speedLevel, false, false, false));
        }

        // Jump boost
        int jumpLevel = Math.max(0, (int) (level.getJumpBonus()) - 1);
        if (jumpLevel >= 0) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST,
                    100, jumpLevel, false, false, false));
        }

        // Water breathing
        player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING,
                100, 0, false, false, false));

        // DRACULA endgame buffs
        if (level == VampireLevel.DRACULA) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 100, 2, false, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 100, 0, false, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 1, false, false, false));
            // Ring holders get bonus haste for mining/crafting flair
            VampireState draculaState = manager.getOrCreateState(player.getUniqueId());
            if (draculaState.hasVampireRing()) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 100, 1, false, false, false));
            }
        }

        // Dracula flight — creative-mode flying for max-level vampires
        if (level == VampireLevel.DRACULA) {
            if (!player.getAllowFlight()) {
                player.setAllowFlight(true);
                player.sendActionBar(net.kyori.adventure.text.Component.text(
                        "Vampire Flight active", net.kyori.adventure.text.format.NamedTextColor.DARK_RED));
            }
            // If sprinting while flying, boost speed like elytra + firework
            if (player.isFlying() && player.isSprinting()) {
                player.setFlySpeed(0.14f); // Fast elytra-like speed (default is 0.05)
                player.setGliding(true);   // Elytra flying animation
                // Particle trail
                player.getWorld().spawnParticle(org.bukkit.Particle.DUST,
                        player.getLocation(), 5, 0.3, 0.3, 0.3, 0,
                        new org.bukkit.Particle.DustOptions(org.bukkit.Color.RED, 1.0f));
            } else if (player.isFlying()) {
                player.setFlySpeed(0.06f); // Normal fly speed
                player.setGliding(false);
            }
        } else {
            // Non-Dracula vampires: revoke flight if they had it
            revokeFlightIfNeeded(player);
        }
    }

    private void applyDayDebuffs(Player player, VampireState state) {
        if (state.isSunImmune()) {
            // Dracula keeps flight even during day (sun immune)
            if (state.getLevel() == VampireLevel.DRACULA) {
                if (!player.getAllowFlight()) player.setAllowFlight(true);
                if (player.isFlying() && player.isSprinting()) {
                    player.setFlySpeed(0.14f);
                    player.setGliding(true);
                    player.getWorld().spawnParticle(org.bukkit.Particle.DUST,
                            player.getLocation(), 5, 0.3, 0.3, 0.3, 0,
                            new org.bukkit.Particle.DustOptions(org.bukkit.Color.RED, 1.0f));
                } else if (player.isFlying()) {
                    player.setFlySpeed(0.06f);
                    player.setGliding(false);
                }
            }
            // Still get night vision and reduced buffs
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION,
                    100, 0, false, false, false));
            return;
        }
        // Not sun immune during day — revoke flight
        revokeFlightIfNeeded(player);

        // Sun damage
        double damageInterval = switch (state.getLevel()) {
            case FLEDGLING -> 2.0;
            case VAMPIRE -> 2.5;
            case ELDER_VAMPIRE -> 3.0;
            case VAMPIRE_LORD -> 4.0;
            case DRACULA -> 5.0; // Should not reach here, Dracula is sun immune
        };

        // Apply damage (this runs every 2 seconds)
        if (player.getHealth() > 2) {
            player.damage(1.0);
        }

        // Wither effect
        player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER,
                60, 0, false, false, false));

        // Slowness
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,
                60, 0, false, false, false));
    }

    /**
     * Revokes creative flight from a player if they're not in creative/spectator mode.
     * Called when a non-Dracula vampire is in daylight or when vampirism is removed.
     */
    private void revokeFlightIfNeeded(Player player) {
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (player.getAllowFlight()) {
            player.setGliding(false);
            player.setAllowFlight(false);
            player.setFlying(false);
            player.setFlySpeed(0.05f); // Reset to default
        }
    }

    /**
     * Determines if the player is in a "nighttime" environment for vampire purposes.
     * Night: actual nighttime, underground (Y < 50), Nether, End, Abyss, or any
     * custom dimension without a day cycle.
     */
    private boolean isNighttime(Player player) {
        World world = player.getWorld();
        String worldName = world.getName();

        // Custom dimensions are always "night" for vampires
        if (worldName.equals("world_nether") || worldName.equals("world_the_end")
                || worldName.equals("world_abyss") || worldName.equals("world_aether")
                || worldName.equals("world_lunar") || worldName.equals("world_jurassic")) {
            return true;
        }

        // Underground
        if (player.getLocation().getY() < 50) return true;

        // Check sky exposure
        Location loc = player.getLocation();
        if (loc.getBlock().getLightFromSky() < 10) return true;

        // Actual nighttime (13000-23000 ticks)
        long time = world.getTime();
        return time >= 13000 && time <= 23000;
    }

    private boolean isArmor(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        String name = item.getType().name();
        return name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE")
                || name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS")
                || name.equals("TURTLE_HELMET");
    }

    // ═══════════════════════════════════════════════════════════════
    //  VAMPIRE INNATE ABILITY — MERCHANT MIND MANIPULATION
    // ═══════════════════════════════════════════════════════════════
    //
    // Every time a vampire selects a trade from a villager (or NPC wizard/quest
    // giver with a merchant inventory), there is a 50% chance the vampire mind-
    // controls the merchant and receives the result item *for free* — no
    // ingredients consumed, no merchant refresh charge. Passive, always on,
    // costs no cooldown; a core perk of being an undead predator.
    //
    // Implementation: hook MerchantInventory clicks in the RESULT slot (slot 2
    // of a villager trade GUI). When the trade is valid and the clicker is a
    // vampire, roll the 50% chance. On success, give the player the result
    // item directly and cancel the event so the ingredients stay in place.

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onVampireMerchantClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (!manager.isVampire(player)) return;
        if (!(e.getClickedInventory() instanceof org.bukkit.inventory.MerchantInventory merchant)) return;
        // Only the result slot (slot 2 in a merchant GUI) matters
        if (e.getSlot() != 2) return;
        org.bukkit.inventory.ItemStack result = e.getCurrentItem();
        if (result == null || result.getType() == Material.AIR) return;
        // Must actually have a selected trade
        if (merchant.getSelectedRecipe() == null) return;

        // 50% mind-manipulation roll
        if (Math.random() >= 0.50) return;

        // FREE TRADE — cancel the normal transaction, give the item directly
        e.setCancelled(true);
        org.bukkit.inventory.ItemStack give = result.clone();
        java.util.Map<Integer, org.bukkit.inventory.ItemStack> overflow =
                player.getInventory().addItem(give);
        for (org.bukkit.inventory.ItemStack leftover : overflow.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), leftover);
        }

        // Feedback
        player.sendActionBar(Component.text("✦ Mind manipulated — free trade!", NamedTextColor.DARK_RED));
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_AMBIENT, 0.7f, 1.6f);
        player.getWorld().spawnParticle(
                Particle.SOUL,
                player.getLocation().add(0, 1.2, 0),
                15, 0.4, 0.6, 0.4, 0.02);
        player.getWorld().spawnParticle(
                Particle.DUST,
                player.getLocation().add(0, 1.0, 0),
                10, 0.3, 0.3, 0.3,
                new Particle.DustOptions(Color.fromRGB(120, 0, 0), 1.2f));
    }
}

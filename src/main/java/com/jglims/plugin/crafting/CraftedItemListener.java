package com.jglims.plugin.crafting;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.vampire.VampireManager;
import com.jglims.plugin.vampire.VampireState;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles every functional effect for CraftedItemManager items:
 * Blood Chalice, Fang Dagger, Raptor Gauntlet, Shark Tooth Necklace,
 * Tremor Shield, Void Scepter, Soul Lantern, Dinosaur Bone Bow,
 * Crimson Elixir, Nightwalker Cloak.
 */
public class CraftedItemListener implements Listener {

    private final JGlimsPlugin plugin;
    private final CraftedItemManager craftedManager;
    private final VampireManager vampireManager;

    /** Cooldown tracking for right-click abilities (Void Scepter, Raptor Gauntlet, etc.). */
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    /** Crimson Elixir permanent claw damage bonus per player (persisted via vampire state not needed — runtime cache). */
    private final Map<UUID, Double> crimsonClawBonus = new HashMap<>();

    public CraftedItemListener(JGlimsPlugin plugin, CraftedItemManager craftedManager,
                               VampireManager vampireManager) {
        this.plugin = plugin;
        this.craftedManager = craftedManager;
        this.vampireManager = vampireManager;
        startOffhandTask();
    }

    public double getCrimsonBonus(UUID playerId) {
        return crimsonClawBonus.getOrDefault(playerId, 0.0);
    }

    // ── Right-click abilities ──────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null) return;
        String id = craftedManager.getCraftedId(item);
        if (id == null) return;

        switch (id) {
            case "blood_chalice" -> {
                event.setCancelled(true);
                useBloodChalice(player, item);
            }
            case "void_scepter" -> {
                event.setCancelled(true);
                useVoidScepter(player);
            }
            case "raptor_gauntlet" -> {
                event.setCancelled(true);
                useRaptorGauntlet(player);
            }
            case "crimson_elixir" -> {
                event.setCancelled(true);
                useCrimsonElixir(player, item);
            }
        }
    }

    private void useBloodChalice(Player player, ItemStack item) {
        if (!vampireManager.isVampire(player)) {
            player.sendMessage(Component.text("Only vampires can drink from the chalice.",
                    NamedTextColor.RED));
            return;
        }
        int charges = craftedManager.getChaliceCharges(item);
        if (charges <= 0) {
            player.sendActionBar(Component.text("Chalice is empty.", NamedTextColor.GRAY));
            return;
        }
        craftedManager.setChaliceCharges(item, charges - 1);
        vampireManager.consumeBlood(player);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_DRINK, 1.2f, 0.7f);
    }

    private void useVoidScepter(Player player) {
        if (isOnCooldown(player.getUniqueId(), "void_scepter")) {
            player.sendActionBar(Component.text("Void Scepter cooling down...",
                    NamedTextColor.GRAY));
            return;
        }
        World world = player.getWorld();
        Location eye = player.getEyeLocation();
        Vector dir = eye.getDirection();

        // Raycast to find non-phased target
        Location target = eye.clone().add(dir.clone().multiply(15));
        // Keep player safe — ensure air at destination
        for (int attempts = 0; attempts < 15; attempts++) {
            if (world.getBlockAt(target).getType().isAir()
                    && world.getBlockAt(target.clone().add(0, 1, 0)).getType().isAir()) {
                break;
            }
            target.add(dir.clone().multiply(-1));
        }

        // Particle trail before and after
        Location start = player.getLocation();
        for (double d = 0; d < 15; d += 0.5) {
            world.spawnParticle(Particle.PORTAL, start.clone().add(dir.clone().multiply(d)),
                    3, 0.2, 0.2, 0.2, 0);
        }
        player.teleport(target);
        world.playSound(target, Sound.ENTITY_ENDERMAN_TELEPORT, 1.2f, 0.6f);
        world.spawnParticle(Particle.REVERSE_PORTAL, target, 50, 0.5, 1, 0.5, 0.1);

        setCooldown(player.getUniqueId(), "void_scepter", 8000);
    }

    private void useRaptorGauntlet(Player player) {
        if (isOnCooldown(player.getUniqueId(), "raptor_gauntlet")) {
            player.sendActionBar(Component.text("Gauntlet cooling down...", NamedTextColor.GRAY));
            return;
        }
        Vector forward = player.getLocation().getDirection().normalize().multiply(2.0);
        forward.setY(Math.max(forward.getY(), 0.45));
        player.setVelocity(forward);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FOX_AMBIENT, 1.2f, 1.4f);
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(),
                20, 0.5, 0.1, 0.5, 0.05);
        setCooldown(player.getUniqueId(), "raptor_gauntlet", 4000);
    }

    private void useCrimsonElixir(Player player, ItemStack item) {
        if (!vampireManager.isVampire(player)) {
            player.sendMessage(Component.text("Only vampires can drink this elixir.",
                    NamedTextColor.RED));
            return;
        }
        int uses = craftedManager.getElixirUses(item);
        if (uses >= 3) {
            player.sendMessage(Component.text("This elixir has been fully consumed.",
                    NamedTextColor.RED));
            return;
        }
        craftedManager.setElixirUses(item, uses + 1);
        double newBonus = crimsonClawBonus.getOrDefault(player.getUniqueId(), 0.0) + 2.0;
        crimsonClawBonus.put(player.getUniqueId(), newBonus);

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_DRINK, 1.0f, 0.8f);
        player.getWorld().spawnParticle(Particle.DUST, player.getLocation().add(0, 1, 0),
                30, 0.5, 0.5, 0.5, 0, new Particle.DustOptions(Color.RED, 1.5f));
        player.sendMessage(Component.text("Permanent claw damage +2 (" + (uses + 1) + "/3 uses)",
                NamedTextColor.DARK_RED));

        if (uses + 1 >= 3) {
            // Consume the empty bottle
            item.setAmount(item.getAmount() - 1);
        }
    }

    // ── Passive on-hit effects ─────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof LivingEntity victim)) return;

        ItemStack main = attacker.getInventory().getItemInMainHand();
        String id = craftedManager.getCraftedId(main);
        if (id == null) return;

        switch (id) {
            case "fang_dagger" -> {
                event.setDamage(event.getDamage() + 12);  // +12 base
                victim.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 60, 1));
                attacker.getWorld().spawnParticle(Particle.ITEM_SLIME,
                        victim.getLocation().add(0, 1, 0), 10, 0.3, 0.3, 0.3, 0);
            }
            case "lunar_blade" -> {
                event.setDamage(event.getDamage() + 14);
                if (isUndead(victim)) {
                    event.setDamage(event.getDamage() + 10);  // Smite V-equivalent
                }
            }
            case "raptor_gauntlet" -> {
                event.setDamage(event.getDamage() + 10);
            }
        }
    }

    /** Tremor Shield projectile reflect + shockwave on melee block. */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player defender)) return;
        if (!defender.isBlocking()) return;
        ItemStack off = defender.getInventory().getItemInOffHand();
        if (!"tremor_shield".equals(craftedManager.getCraftedId(off))) return;

        Entity damager = event.getDamager();
        if (damager instanceof Projectile proj) {
            // Reflect 50% — spawn a new arrow at the defender aimed back at shooter
            if (Math.random() < 0.5 && proj.getShooter() instanceof LivingEntity shooter) {
                Vector back = shooter.getLocation().toVector()
                        .subtract(defender.getLocation().toVector()).normalize().multiply(1.5);
                Arrow reflected = defender.getWorld().spawnArrow(
                        defender.getEyeLocation(), back, 1.5f, 1.0f);
                reflected.setShooter(defender);
                proj.remove();
                event.setCancelled(true);
            }
        } else if (damager instanceof LivingEntity attacker) {
            // Shockwave
            Location loc = defender.getLocation();
            defender.getWorld().spawnParticle(Particle.EXPLOSION, loc, 3, 1, 0, 1, 0);
            defender.getWorld().playSound(loc, Sound.BLOCK_ANVIL_LAND, 1.0f, 0.6f);
            for (Entity nearby : defender.getWorld().getNearbyEntities(loc, 4, 4, 4)) {
                if (nearby == defender) continue;
                if (nearby instanceof LivingEntity le) {
                    Vector push = le.getLocation().toVector().subtract(loc.toVector())
                            .normalize().multiply(1.2);
                    push.setY(0.4);
                    le.setVelocity(push);
                    le.damage(6, defender);
                }
            }
        }
    }

    /** Dinosaur Bone Bow: no-gravity arrows + extra damage. */
    @EventHandler(priority = EventPriority.HIGH)
    public void onBowShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        ItemStack bow = event.getBow();
        if (bow == null) return;
        if (!"dinosaur_bone_bow".equals(craftedManager.getCraftedId(bow))) return;

        Entity proj = event.getProjectile();
        if (proj instanceof Arrow arrow) {
            arrow.setGravity(false);
            arrow.setDamage(arrow.getDamage() + 8);
            arrow.setGlowing(true);
        }
    }

    // ── Passive offhand / equipped tasks ───────────────────────────────

    /**
     * Periodic check for offhand-passive items (Shark Tooth Necklace,
     * Soul Lantern) and worn Nightwalker Cloak.
     */
    private void startOffhandTask() {
        new BukkitRunnable() {
            @Override public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    if (player.getGameMode() == GameMode.SPECTATOR) continue;

                    ItemStack off = player.getInventory().getItemInOffHand();
                    String offId = craftedManager.getCraftedId(off);
                    if ("shark_tooth_necklace".equals(offId)) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING,
                                80, 0, false, false, false));
                        player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE,
                                80, 0, false, false, false));
                    }
                    if ("soul_lantern_item".equals(offId)) {
                        for (Entity e : player.getWorld().getNearbyEntities(
                                player.getLocation(), 8, 8, 8)) {
                            if (!(e instanceof Mob mob)) continue;
                            mob.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,
                                    60, 1, false, false, false));
                            mob.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS,
                                    60, 0, false, false, false));
                        }
                    }

                    // Nightwalker Cloak
                    ItemStack chest = player.getInventory().getChestplate();
                    if (chest != null && "nightwalker_cloak".equals(craftedManager.getCraftedId(chest))) {
                        if (vampireManager.isVampire(player) && isNight(player)) {
                            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY,
                                    80, 0, false, false, false));
                            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,
                                    80, 1, false, false, false));
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 40L, 40L);
    }

    // ── Helpers ────────────────────────────────────────────────────────

    private boolean isNight(Player player) {
        long time = player.getWorld().getTime();
        if (time >= 13000 && time <= 23000) return true;
        String w = player.getWorld().getName();
        return w.equals("world_nether") || w.equals("world_the_end")
                || w.equals("world_abyss") || w.equals("world_lunar");
    }

    private boolean isUndead(LivingEntity e) {
        String n = e.getType().name();
        return n.contains("ZOMBIE") || n.contains("SKELETON") || n.contains("PHANTOM")
                || n.contains("DROWNED") || n.contains("HUSK") || n.contains("STRAY")
                || n.contains("WITHER");
    }

    private boolean isOnCooldown(UUID playerId, String key) {
        Map<String, Long> m = cooldowns.get(playerId);
        if (m == null) return false;
        Long expiry = m.get(key);
        return expiry != null && expiry > System.currentTimeMillis();
    }

    private void setCooldown(UUID playerId, String key, long durationMs) {
        cooldowns.computeIfAbsent(playerId, k -> new HashMap<>())
                .put(key, System.currentTimeMillis() + durationMs);
    }
}

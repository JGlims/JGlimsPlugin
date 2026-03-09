package com.jglims.plugin.legendary;

import com.jglims.plugin.JGlimsPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import java.util.List;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Manages the Thanos Glove, Infinity Gauntlet, and Thanos boss.
 * Phase 22 â€” README Section K.
 *
 * Thanos Glove: drops from Thanos boss (100%).
 * Infinity Gauntlet: crafted from Thanos Glove + all 6 finished Infinity Stones.
 * Right-click: kills 50% of loaded hostile mobs in current dimension (300s CD).
 */
public class InfinityGauntletManager implements Listener {

    private final JGlimsPlugin plugin;
    private final InfinityStoneManager stoneManager;
    private final NamespacedKey KEY_THANOS_GLOVE;
    private final NamespacedKey KEY_INFINITY_GAUNTLET;
    private final NamespacedKey KEY_THANOS_BOSS;

    // Cooldown: player UUID -> expiry timestamp
    private final Map<UUID, Long> gauntletCooldowns = new HashMap<>();
    private static final long COOLDOWN_MS = 300_000L; // 5 minutes

    public InfinityGauntletManager(JGlimsPlugin plugin, InfinityStoneManager stoneManager) {
        this.plugin = plugin;
        this.stoneManager = stoneManager;
        KEY_THANOS_GLOVE = new NamespacedKey(plugin, "thanos_glove");
        KEY_INFINITY_GAUNTLET = new NamespacedKey(plugin, "infinity_gauntlet");
        KEY_THANOS_BOSS = new NamespacedKey(plugin, "thanos_boss");
    }

    // â”€â”€ Item creation â”€â”€

    public ItemStack createThanosGlove() {
        ItemStack item = new ItemStack(Material.GOLDEN_CHESTPLATE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Thanos Glove", TextColor.color(200, 150, 50))
                .decorate(TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text(""),
                Component.text("A golden gauntlet of immense power.", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false),
                Component.text("It yearns for the Infinity Stones...", NamedTextColor.DARK_PURPLE).decoration(TextDecoration.ITALIC, false),
                Component.text(""),
                Component.text("Combine with all 6 Infinity Stones", NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false),
                Component.text("in a crafting table.", NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false)
        ));
        meta.getPersistentDataContainer().set(KEY_THANOS_GLOVE, PersistentDataType.BYTE, (byte) 1);
        CustomModelDataComponent cmdComp = meta.getCustomModelDataComponent();
            cmdComp.setStrings(List.of("thanos_glove"));
            meta.setCustomModelDataComponent(cmdComp);
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createInfinityGauntlet() {
        ItemStack item = new ItemStack(Material.GOLDEN_CHESTPLATE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Infinity Gauntlet", TextColor.color(255, 215, 0))
                .decorate(TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text(""),
                Component.text("With all six stones, I could simply", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false),
                Component.text("snap my fingers. They would all", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false),
                Component.text("cease to exist.", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false),
                Component.text(""),
                Component.text("\u2726 THE SNAP \u2726", TextColor.color(255, 215, 0)).decorate(TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false),
                Component.text("Right-click: Eliminate 50% of all", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                Component.text("hostile mobs in your dimension.", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                Component.text("Cooldown: 5 minutes", NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false),
                Component.text(""),
                Component.text("Power", TextColor.color(150, 0, 255)).decoration(TextDecoration.ITALIC, false)
                        .append(Component.text(" \u2022 ", NamedTextColor.DARK_GRAY))
                        .append(Component.text("Space", TextColor.color(0, 100, 255)))
                        .append(Component.text(" \u2022 ", NamedTextColor.DARK_GRAY))
                        .append(Component.text("Reality", TextColor.color(255, 0, 0))),
                Component.text("Soul", TextColor.color(255, 150, 0)).decoration(TextDecoration.ITALIC, false)
                        .append(Component.text(" \u2022 ", NamedTextColor.DARK_GRAY))
                        .append(Component.text("Time", TextColor.color(0, 200, 50)))
                        .append(Component.text(" \u2022 ", NamedTextColor.DARK_GRAY))
                        .append(Component.text("Mind", TextColor.color(255, 255, 0)))
        ));
        meta.getPersistentDataContainer().set(KEY_INFINITY_GAUNTLET, PersistentDataType.BYTE, (byte) 1);
        CustomModelDataComponent cmdComp2 = meta.getCustomModelDataComponent();
            cmdComp2.setStrings(List.of("infinity_gauntlet"));
            meta.setCustomModelDataComponent(cmdComp2);
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
        item.setItemMeta(meta);
        return item;
    }

    // â”€â”€ Identification â”€â”€

    public boolean isThanosGlove(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(KEY_THANOS_GLOVE, PersistentDataType.BYTE);
    }

    public boolean isInfinityGauntlet(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(KEY_INFINITY_GAUNTLET, PersistentDataType.BYTE);
    }

    public boolean isThanosBoss(LivingEntity entity) {
        return entity.getPersistentDataContainer().has(KEY_THANOS_BOSS, PersistentDataType.BYTE);
    }

    // â”€â”€ The Snap â”€â”€

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        ItemStack held = player.getInventory().getItemInMainHand();
        if (!isInfinityGauntlet(held)) return;

        event.setCancelled(true);

        // Check cooldown
        long now = System.currentTimeMillis();
        Long expiry = gauntletCooldowns.get(player.getUniqueId());
        if (expiry != null && now < expiry) {
            long remaining = (expiry - now) / 1000;
            player.sendMessage(Component.text("The Gauntlet needs to recharge... ", NamedTextColor.RED)
                    .append(Component.text(remaining + "s remaining", NamedTextColor.GRAY)));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            return;
        }

        // Execute The Snap
        gauntletCooldowns.put(player.getUniqueId(), now + COOLDOWN_MS);
        performSnap(player);
    }

    private void performSnap(Player player) {
        World world = player.getWorld();
        Location loc = player.getLocation();

        // Phase 1: Dramatic build-up (1 second)
        player.sendMessage(Component.text(""));
        player.sendMessage(Component.text("  \u2726 ", TextColor.color(255, 215, 0))
                .append(Component.text("I am... inevitable.", NamedTextColor.GOLD).decorate(TextDecoration.BOLD)));

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getWorld().equals(world)) {
                p.playSound(loc, Sound.BLOCK_BEACON_POWER_SELECT, 2.0f, 0.5f);
            }
        }

        // Phase 2: The Snap (after 1 second delay)
        new BukkitRunnable() {
            @Override
            public void run() {
                // Collect all hostile mobs in the dimension
                List<LivingEntity> hostiles = new ArrayList<>();
                for (LivingEntity entity : world.getLivingEntities()) {
                    if (!(entity instanceof Monster)) continue;
                    if (entity instanceof Player) continue;

                    // Exclude bosses
                    if (entity instanceof EnderDragon) continue;
                    if (entity instanceof Wither) continue;
                    if (entity instanceof Warden) continue;
                    if (entity instanceof ElderGuardian) continue;

                    // Exclude event bosses
                    if (plugin.getEventManager().isEventBoss(entity)) continue;

                    // Exclude structure bosses (check PDC)
                    if (entity.getPersistentDataContainer().has(
                            new NamespacedKey(plugin, "structure_boss"), PersistentDataType.BYTE)) continue;

                    // Exclude king mobs
                    if (entity.getPersistentDataContainer().has(
                            new NamespacedKey(plugin, "king_mob"), PersistentDataType.BYTE)) continue;

                    // Exclude Blood Moon King
                    if (plugin.getBloodMoonManager().isBloodMoonKing(entity)) continue;

                    // Exclude Thanos boss
                    if (isThanosBoss(entity)) continue;

                    hostiles.add(entity);
                }

                // Kill 50%
                Collections.shuffle(hostiles);
                int toKill = hostiles.size() / 2;
                int killed = 0;

                for (int i = 0; i < toKill; i++) {
                    LivingEntity victim = hostiles.get(i);
                    // Snap VFX on each victim
                    Location vLoc = victim.getLocation();
                    world.spawnParticle(Particle.SOUL_FIRE_FLAME, vLoc, 15, 0.5, 1, 0.5, 0.05);
                    world.spawnParticle(Particle.DUST, vLoc, 10, 0.5, 1, 0.5, 0,
                            new Particle.DustOptions(org.bukkit.Color.fromRGB(255, 215, 0), 1.5f));
                    victim.setHealth(0);
                    killed++;
                }

                // Snap VFX at player
                world.spawnParticle(Particle.SOUL_FIRE_FLAME, loc.clone().add(0, 1, 0), 200, 3, 3, 3, 0.1);
                world.spawnParticle(Particle.DUST, loc.clone().add(0, 2, 0), 100, 5, 5, 5, 0,
                        new Particle.DustOptions(org.bukkit.Color.fromRGB(255, 215, 0), 3.0f));
                world.spawnParticle(Particle.TOTEM_OF_UNDYING, loc.clone().add(0, 1, 0), 50, 1, 2, 1, 0.5);
                world.spawnParticle(Particle.EXPLOSION_EMITTER, loc, 2, 1, 1, 1);

                // Sound
                for (Player p : world.getPlayers()) {
                    p.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.3f);
                    p.playSound(loc, Sound.BLOCK_BEACON_DEACTIVATE, 2.0f, 0.5f);
                    p.playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.5f, 0.5f);
                }

                // Broadcast
                int finalKilled = killed;
                int total = hostiles.size();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getWorld().equals(world)) {
                        p.sendMessage(Component.text(""));
                        p.showTitle(net.kyori.adventure.title.Title.title(
                                Component.text("* S N A P *", TextColor.color(255, 215, 0)).decorate(TextDecoration.BOLD),
                                Component.text(finalKilled + " of " + total + " hostile mobs eliminated.", NamedTextColor.GRAY),
                                net.kyori.adventure.title.Title.Times.times(
                                        java.time.Duration.ofMillis(200),
                                        java.time.Duration.ofSeconds(3),
                                        java.time.Duration.ofMillis(1500))));
                    }
                }

                plugin.getLogger().info(player.getName() + " used the Infinity Gauntlet: " +
                        finalKilled + "/" + total + " hostile mobs snapped in " + world.getName());
            }
        }.runTaskLater(plugin, 20L);
    }

    // â”€â”€ Thanos Boss Death â”€â”€

    @EventHandler
    public void onThanosDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (!isThanosBoss(entity)) return;

        event.getDrops().clear();
        event.setDroppedExp(2000);

        // Guaranteed Thanos Glove drop
        Location loc = entity.getLocation();
        loc.getWorld().dropItemNaturally(loc, createThanosGlove());

        // Extra loot
        loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.DIAMOND, 20 + ThreadLocalRandom.current().nextInt(15)));
        loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.NETHERITE_INGOT, 2));
        loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.GOLD_BLOCK, 8 + ThreadLocalRandom.current().nextInt(8)));

        // VFX
        loc.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, loc, 30, 3, 3, 3, 0.5);
        loc.getWorld().spawnParticle(Particle.DUST, loc, 40, 3, 3, 3, 0,
                new Particle.DustOptions(org.bukkit.Color.fromRGB(150, 0, 255), 2.0f));
        loc.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, loc, 2, 2, 2, 2);
        loc.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_DEATH, 2.0f, 0.5f);

        // Broadcast
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(Component.text(""));
            p.sendMessage(Component.text("  \u2726 ", TextColor.color(150, 0, 255))
                    .append(Component.text("THANOS HAS BEEN DEFEATED!", TextColor.color(200, 150, 50)).decorate(TextDecoration.BOLD)));
            p.sendMessage(Component.text("  The Thanos Glove has dropped!", NamedTextColor.GOLD));
            p.sendMessage(Component.text(""));
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        }

        plugin.getLogger().info("Thanos boss defeated! Thanos Glove dropped.");
    }

    // â”€â”€ Thanos Boss Spawning (called by StructureBossManager) â”€â”€

    /**
     * Configure an entity as the Thanos boss. Called by StructureBossManager
     * when a THANOS_TEMPLE structure boss is spawned.
     */
    public void configureThanosBoss(LivingEntity entity) {
        entity.getPersistentDataContainer().set(KEY_THANOS_BOSS, PersistentDataType.BYTE, (byte) 1);
        entity.customName(Component.text("\u2726 Thanos \u2726", TextColor.color(150, 0, 255))
                .decorate(TextDecoration.BOLD));
        entity.setCustomNameVisible(true);
        if (entity.getAttribute(Attribute.MAX_HEALTH) != null) {
            entity.getAttribute(Attribute.MAX_HEALTH).setBaseValue(800);
            entity.setHealth(800);
        }
        if (entity.getAttribute(Attribute.ATTACK_DAMAGE) != null) {
            entity.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(20);
        }
        if (entity.getAttribute(Attribute.MOVEMENT_SPEED) != null) {
            entity.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.28);
        }
        entity.setGlowing(true);
        entity.setRemoveWhenFarAway(false);
        entity.setPersistent(true);

        // Equip purple/gold themed armor
        if (entity instanceof Zombie zombie) {
            zombie.setAdult();
            zombie.setShouldBurnInDay(false);
            zombie.getEquipment().setHelmet(createPurpleGoldArmor(Material.GOLDEN_HELMET));
            zombie.getEquipment().setChestplate(createPurpleGoldArmor(Material.GOLDEN_CHESTPLATE));
            zombie.getEquipment().setLeggings(createPurpleGoldArmor(Material.GOLDEN_LEGGINGS));
            zombie.getEquipment().setBoots(createPurpleGoldArmor(Material.GOLDEN_BOOTS));
            zombie.getEquipment().setItemInMainHand(new ItemStack(Material.NETHERITE_AXE));
            zombie.getEquipment().setHelmetDropChance(0f);
            zombie.getEquipment().setChestplateDropChance(0f);
            zombie.getEquipment().setLeggingsDropChance(0f);
            zombie.getEquipment().setBootsDropChance(0f);
            zombie.getEquipment().setItemInMainHandDropChance(0f);
        }

        // Start boss ability task
        startThanosBossAI(entity);
    }

    private ItemStack createPurpleGoldArmor(Material mat) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
        item.setItemMeta(meta);
        return item;
    }

    private void startThanosBossAI(LivingEntity boss) {
        new BukkitRunnable() {
            int ticks = 0;
            boolean phase2 = false;

            @Override
            public void run() {
                if (boss == null || boss.isDead() || !boss.isValid()) { cancel(); return; }
                ticks += 10;
                World world = boss.getWorld();
                Location bLoc = boss.getLocation();

                // Phase check: at 50% HP, enter phase 2
                double hpPercent = boss.getHealth() / 800.0;
                if (hpPercent <= 0.5 && !phase2) {
                    phase2 = true;
                    if (boss.getAttribute(Attribute.MOVEMENT_SPEED) != null) {
                        boss.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.36);
                    }
                    if (boss.getAttribute(Attribute.ATTACK_DAMAGE) != null) {
                        boss.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(30);
                    }
                    for (Player p : world.getPlayers()) {
                        if (p.getLocation().distanceSquared(bLoc) < 10000) {
                            p.sendMessage(Component.text("  Thanos: ", TextColor.color(150, 0, 255))
                                    .append(Component.text("\"You should have gone for the head.\"", NamedTextColor.GOLD)
                                            .decorate(TextDecoration.ITALIC)));
                            p.playSound(bLoc, Sound.ENTITY_WITHER_AMBIENT, 1.0f, 0.3f);
                        }
                    }
                    world.spawnParticle(Particle.DUST, bLoc, 50, 3, 3, 3, 0,
                            new Particle.DustOptions(org.bukkit.Color.fromRGB(150, 0, 255), 2.0f));
                }

                // Ground slam: every 5 seconds (100 ticks), AoE damage in 5 block radius
                if (ticks % 100 == 0) {
                    double slamDmg = phase2 ? 15 : 10;
                    double slamRadius = phase2 ? 7 : 5;
                    for (Entity e : world.getNearbyEntities(bLoc, slamRadius, slamRadius, slamRadius)) {
                        if (e instanceof Player p && p.getLocation().distanceSquared(bLoc) < slamRadius * slamRadius) {
                            p.damage(slamDmg, boss);
                            p.setVelocity(p.getLocation().subtract(bLoc).toVector().normalize().multiply(1.5).setY(0.5));
                        }
                    }
                    world.spawnParticle(Particle.EXPLOSION, bLoc, 5, slamRadius / 2, 0.5, slamRadius / 2);
                    world.spawnParticle(Particle.DUST, bLoc, 40, slamRadius, 0.5, slamRadius, 0,
                            new Particle.DustOptions(org.bukkit.Color.fromRGB(150, 0, 255), 2.0f));
                    world.playSound(bLoc, Sound.ENTITY_IRON_GOLEM_ATTACK, 2.0f, 0.5f);
                }

                // Energy beam: every 8 seconds (160 ticks), Wither II + Slowness III on nearest
                if (ticks % 160 == 0) {
                    Player nearest = null;
                    double minDist = 2500; // 50 blocks
                    for (Player p : world.getPlayers()) {
                        double d = p.getLocation().distanceSquared(bLoc);
                        if (d < minDist) { minDist = d; nearest = p; }
                    }
                    if (nearest != null) {
                        int beamDuration = phase2 ? 100 : 60; // 5s or 3s
                        nearest.addPotionEffect(new org.bukkit.potion.PotionEffect(
                                org.bukkit.potion.PotionEffectType.WITHER, beamDuration, 1, true, false));
                        nearest.addPotionEffect(new org.bukkit.potion.PotionEffect(
                                org.bukkit.potion.PotionEffectType.SLOWNESS, beamDuration, 2, true, false));
                        nearest.damage(phase2 ? 10 : 6, boss);

                        // Beam visual
                        org.bukkit.util.Vector dir = nearest.getLocation().subtract(bLoc).toVector().normalize();
                        double dist = Math.min(bLoc.distance(nearest.getLocation()), 50);
                        for (double t = 0; t < dist; t += 0.5) {
                            Location point = bLoc.clone().add(dir.clone().multiply(t));
                            world.spawnParticle(Particle.DUST, point, 2, 0.1, 0.1, 0.1, 0,
                                    new Particle.DustOptions(org.bukkit.Color.fromRGB(150, 0, 255), 1.5f));
                        }
                        world.playSound(bLoc, Sound.ENTITY_GUARDIAN_ATTACK, 1.5f, 0.3f);
                    }
                }

                // Ambient particles
                world.spawnParticle(Particle.DUST, bLoc.clone().add(0, 1.5, 0), 5, 0.5, 1, 0.5, 0,
                        new Particle.DustOptions(org.bukkit.Color.fromRGB(150, 0, 255), 1.0f));
                world.spawnParticle(Particle.ENCHANT, bLoc.clone().add(0, 2, 0), 3, 0.5, 0.5, 0.5);
                if (boss.isDead() || !boss.isValid()) { cancel(); return; }
            }
        }.runTaskTimer(plugin, 10L, 10L);
    }

    public NamespacedKey getKeyThanosGlove() { return KEY_THANOS_GLOVE; }
    public NamespacedKey getKeyInfinityGauntlet() { return KEY_INFINITY_GAUNTLET; }
    public NamespacedKey getKeyThanosBoss() { return KEY_THANOS_BOSS; }
    public InfinityStoneManager getStoneManager() { return stoneManager; }
}
package com.jglims.plugin.mobs;

import com.jglims.plugin.JGlimsPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.ElderGuardian;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Warden;
import org.bukkit.entity.Wither;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class BossMasteryManager implements Listener {

    private final JGlimsPlugin plugin;

    // PDC keys for each boss title
    private final NamespacedKey KEY_WITHER_SLAYER;
    private final NamespacedKey KEY_GUARDIAN_SLAYER;
    private final NamespacedKey KEY_WARDEN_SLAYER;
    private final NamespacedKey KEY_DRAGON_SLAYER;
    private final NamespacedKey KEY_GOD_SLAYER;

    // Track which players damaged a boss during a fight (UUID of boss -> set of player UUIDs)
    private final Map<UUID, Set<UUID>> bossDamageTracker = new HashMap<>();

    // Boss type identifiers
    public enum BossType {
        WITHER("Wither Slayer", 0.05, TextColor.color(80, 80, 80)),
        ELDER_GUARDIAN("Guardian Slayer", 0.07, TextColor.color(100, 180, 180)),
        WARDEN("Warden Slayer", 0.10, TextColor.color(30, 80, 100)),
        ENDER_DRAGON("Dragon Slayer", 0.15, TextColor.color(200, 100, 255)),
        GOD_SLAYER("God Slayer", 0.25, TextColor.color(255, 215, 0));

        private final String title;
        private final double resistanceBonus;
        private final TextColor color;

        BossType(String title, double resistanceBonus, TextColor color) {
            this.title = title;
            this.resistanceBonus = resistanceBonus;
            this.color = color;
        }

        public String getTitle() { return title; }
        public double getResistanceBonus() { return resistanceBonus; }
        public TextColor getColor() { return color; }
    }

    public BossMasteryManager(JGlimsPlugin plugin) {
        this.plugin = plugin;
        KEY_WITHER_SLAYER = new NamespacedKey(plugin, "boss_wither_slayer");
        KEY_GUARDIAN_SLAYER = new NamespacedKey(plugin, "boss_guardian_slayer");
        KEY_WARDEN_SLAYER = new NamespacedKey(plugin, "boss_warden_slayer");
        KEY_DRAGON_SLAYER = new NamespacedKey(plugin, "boss_dragon_slayer");
        KEY_GOD_SLAYER = new NamespacedKey(plugin, "boss_god_slayer");
    }

    // ── Track damage dealt to bosses ──
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity damaged = event.getEntity();
        if (!isBoss(damaged)) return;

        Player attacker = getPlayerAttacker(event);
        if (attacker == null) return;

        bossDamageTracker.computeIfAbsent(damaged.getUniqueId(), k -> new HashSet<>()).add(attacker.getUniqueId());
    }

    // ── Award titles on boss death ──
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (!isBoss(entity)) return;

        BossType type = getBossType(entity);
        if (type == null) return;

        Set<UUID> participants = bossDamageTracker.remove(entity.getUniqueId());
        if (participants == null || participants.isEmpty()) return;

        for (UUID playerId : participants) {
            Player player = plugin.getServer().getPlayer(playerId);
            if (player == null || !player.isOnline()) continue;

            boolean alreadyHad = hasTitle(player, type);
            awardTitle(player, type);

            if (!alreadyHad) {
                // Announce the new title
                player.sendMessage(Component.text(""));
                player.sendMessage(Component.text("  BOSS MASTERY UNLOCKED!", NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
                player.sendMessage(Component.text("  " + type.getTitle(), type.getColor()).decorate(TextDecoration.BOLD)
                    .append(Component.text(" — +" + (int)(type.getResistanceBonus() * 100) + "% damage resistance", NamedTextColor.GRAY)));
                player.sendMessage(Component.text(""));

                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation().add(0, 2, 0), 40, 0.5, 1, 0.5);

                // Check for God Slayer
                if (type != BossType.GOD_SLAYER && hasAllBossTitles(player)) {
                    awardGodSlayer(player);
                }

                plugin.getLogger().info(player.getName() + " earned boss title: " + type.getTitle());
            }
        }
    }

    // ── Apply damage resistance based on titles ──
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerTakeDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        double resistance = getTotalResistance(player);
        if (resistance > 0) {
            double reduced = event.getDamage() * (1.0 - resistance);
            event.setDamage(Math.max(0, reduced));
        }
    }

    // ── God Slayer also gives +20% damage ──
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerDealDamage(EntityDamageByEntityEvent event) {
        Player attacker = getPlayerAttacker(event);
        if (attacker == null) return;

        if (hasTitle(attacker, BossType.GOD_SLAYER)) {
            event.setDamage(event.getDamage() * 1.20);
        }
    }

    // ── Helper methods ──

    private boolean isBoss(Entity entity) {
        return entity instanceof Wither
            || entity instanceof ElderGuardian
            || entity instanceof Warden
            || entity instanceof EnderDragon;
    }

    private BossType getBossType(Entity entity) {
        if (entity instanceof Wither) return BossType.WITHER;
        if (entity instanceof ElderGuardian) return BossType.ELDER_GUARDIAN;
        if (entity instanceof Warden) return BossType.WARDEN;
        if (entity instanceof EnderDragon) return BossType.ENDER_DRAGON;
        return null;
    }

    private Player getPlayerAttacker(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player p) return p;
        if (event.getDamager() instanceof org.bukkit.entity.Projectile proj && proj.getShooter() instanceof Player p) return p;
        return null;
    }

    private NamespacedKey getKey(BossType type) {
        return switch (type) {
            case WITHER -> KEY_WITHER_SLAYER;
            case ELDER_GUARDIAN -> KEY_GUARDIAN_SLAYER;
            case WARDEN -> KEY_WARDEN_SLAYER;
            case ENDER_DRAGON -> KEY_DRAGON_SLAYER;
            case GOD_SLAYER -> KEY_GOD_SLAYER;
        };
    }

    public boolean hasTitle(Player player, BossType type) {
        return player.getPersistentDataContainer().getOrDefault(getKey(type), PersistentDataType.INTEGER, 0) >= 1;
    }

    private void awardTitle(Player player, BossType type) {
        player.getPersistentDataContainer().set(getKey(type), PersistentDataType.INTEGER, 1);
    }

    private boolean hasAllBossTitles(Player player) {
        return hasTitle(player, BossType.WITHER)
            && hasTitle(player, BossType.ELDER_GUARDIAN)
            && hasTitle(player, BossType.WARDEN)
            && hasTitle(player, BossType.ENDER_DRAGON);
    }

    private void awardGodSlayer(Player player) {
        awardTitle(player, BossType.GOD_SLAYER);

        player.sendMessage(Component.text(""));
        player.sendMessage(Component.text("  ULTIMATE TITLE UNLOCKED!", TextColor.color(255, 215, 0)).decorate(TextDecoration.BOLD));
        player.sendMessage(Component.text("  GOD SLAYER", TextColor.color(255, 215, 0)).decorate(TextDecoration.BOLD)
            .append(Component.text(" — +25% resistance, +20% damage", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("  This title REPLACES all other boss titles.", NamedTextColor.DARK_GRAY));
        player.sendMessage(Component.text(""));

        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, 0.8f, 1.2f);
        player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0, 2, 0), 80, 1, 2, 1);
        player.getWorld().spawnParticle(Particle.FLAME, player.getLocation().add(0, 1, 0), 50, 1, 1.5, 1);

        // Broadcast to all players
        Component broadcast = Component.text(player.getName(), TextColor.color(255, 215, 0)).decorate(TextDecoration.BOLD)
            .append(Component.text(" has earned the ", NamedTextColor.GRAY))
            .append(Component.text("GOD SLAYER", TextColor.color(255, 215, 0)).decorate(TextDecoration.BOLD))
            .append(Component.text(" title!", NamedTextColor.GRAY));
        plugin.getServer().broadcast(broadcast);

        plugin.getLogger().info(player.getName() + " earned GOD SLAYER title!");
    }

    /**
     * Calculate total resistance. If God Slayer is active, use ONLY its value (25%).
     * Otherwise, sum individual boss title resistances.
     */
    public double getTotalResistance(Player player) {
        if (hasTitle(player, BossType.GOD_SLAYER)) {
            return BossType.GOD_SLAYER.getResistanceBonus();
        }
        double total = 0;
        if (hasTitle(player, BossType.WITHER)) total += BossType.WITHER.getResistanceBonus();
        if (hasTitle(player, BossType.ELDER_GUARDIAN)) total += BossType.ELDER_GUARDIAN.getResistanceBonus();
        if (hasTitle(player, BossType.WARDEN)) total += BossType.WARDEN.getResistanceBonus();
        if (hasTitle(player, BossType.ENDER_DRAGON)) total += BossType.ENDER_DRAGON.getResistanceBonus();
        return total;
    }

    public void showBossTitles(Player player) {
        player.sendMessage(Component.text("=== Boss Mastery Titles ===", NamedTextColor.GOLD).decorate(TextDecoration.BOLD));

        boolean godSlayer = hasTitle(player, BossType.GOD_SLAYER);
        if (godSlayer) {
            player.sendMessage(Component.text("  GOD SLAYER", BossType.GOD_SLAYER.getColor()).decorate(TextDecoration.BOLD)
                .append(Component.text(" — +25% resistance, +20% damage (replaces all)", NamedTextColor.GRAY)));
        }

        for (BossType type : new BossType[]{BossType.WITHER, BossType.ELDER_GUARDIAN, BossType.WARDEN, BossType.ENDER_DRAGON}) {
            boolean has = hasTitle(player, type);
            Component status = has
                ? Component.text("  " + type.getTitle(), type.getColor()).decorate(TextDecoration.BOLD)
                    .append(Component.text(" — +" + (int)(type.getResistanceBonus() * 100) + "% resistance", NamedTextColor.GRAY))
                    .append(godSlayer ? Component.text(" (superseded by God Slayer)", NamedTextColor.DARK_GRAY) : Component.empty())
                : Component.text("  " + type.getTitle(), NamedTextColor.DARK_GRAY)
                    .append(Component.text(" — Not earned", NamedTextColor.DARK_GRAY));
            player.sendMessage(status);
        }

        double totalResist = getTotalResistance(player);
        player.sendMessage(Component.text("  Total resistance: ", NamedTextColor.GRAY)
            .append(Component.text(String.format("%.0f%%", totalResist * 100), NamedTextColor.GREEN)));
        if (godSlayer) {
            player.sendMessage(Component.text("  Damage bonus: ", NamedTextColor.GRAY)
                .append(Component.text("+20%", NamedTextColor.GREEN)));
        }
    }
}
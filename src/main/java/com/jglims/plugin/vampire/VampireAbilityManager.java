package com.jglims.plugin.vampire;

import com.jglims.plugin.JGlimsPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Manages the 5-slot vampire ability system: creates ability items, handles
 * ability execution (claw, bite, bat, blood nova, domain of night), cooldown
 * tracking, and the ability selection GUI.
 * <p>
 * Abilities evolve with vampire level; higher-tier vampires get upgraded
 * versions of the same abilities.
 */
public class VampireAbilityManager {

    public static final String CLAW = "vampire_claw";
    public static final String BITE = "vampire_bite";
    public static final String BAT = "bat_ability";
    public static final String NOVA = "blood_nova";
    public static final String DOMAIN = "domain_of_night";

    public static final int CMD_CLAW = 50001;
    public static final int CMD_BITE = 50002;
    public static final int CMD_BAT = 50003;
    public static final int CMD_NOVA = 50004;
    public static final int CMD_DOMAIN = 50005;

    private static final String GUI_TITLE = "Vampire Abilities";

    private final JGlimsPlugin plugin;
    private final VampireManager vampireManager;

    /** PDC key marking a stack as a vampire ability item. Value = ability ID string. */
    public final NamespacedKey keyAbilityId;
    /** PDC key on vampire bite victims — they take 25% more damage from the biter. */
    public final NamespacedKey keyBiteMark;
    /** PDC key tracking which player marked them. */
    public final NamespacedKey keyBiteMarker;

    /** Cooldown expiry timestamps: {player UUID → {ability ID → unix ms}}. */
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();
    /** Claw hit counters for Dracula's every-5th-hit blood explosion. */
    private final Map<UUID, Integer> clawHitCounter = new HashMap<>();
    /** Players currently inside a Domain of Night (UUID → expiry ms). */
    private final Map<UUID, Long> activeDomains = new HashMap<>();

    public VampireAbilityManager(JGlimsPlugin plugin, VampireManager vampireManager) {
        this.plugin = plugin;
        this.vampireManager = vampireManager;
        this.keyAbilityId = new NamespacedKey(plugin, "vampire_ability_id");
        this.keyBiteMark = new NamespacedKey(plugin, "vampire_bite_mark_until");
        this.keyBiteMarker = new NamespacedKey(plugin, "vampire_bite_marker");
        startCooldownCleanup();
        startDomainTask();
    }

    // ── Ability item creation ──────────────────────────────────────────

    /**
     * Creates the ability item for a specific ability. Display name reflects
     * the vampire's current tier. Items are marked via PDC so right-click
     * detection is fast.
     */
    public ItemStack createAbilityItem(String abilityId, int tier) {
        Material mat;
        int cmd;
        String displayName;
        NamedTextColor color;
        List<Component> lore = new ArrayList<>();

        switch (abilityId) {
            case CLAW -> {
                mat = Material.IRON_SWORD;
                cmd = CMD_CLAW;
                displayName = "Vampire Claw " + romanTier(tier);
                color = NamedTextColor.DARK_RED;
                lore.add(Component.text("Your basic attack — evolves with level.",
                        NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text("Hold in offhand and right-click to slash.",
                        NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false));
                if (tier >= 2) lore.add(Component.text("• Lifesteal on hit",
                        NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
                if (tier >= 3) lore.add(Component.text("• 3-hit combo",
                        NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
                if (tier >= 4) lore.add(Component.text("• Shockwave projectile",
                        NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
                if (tier >= 5) lore.add(Component.text("• Phases through blocks",
                        NamedTextColor.DARK_RED).decoration(TextDecoration.ITALIC, false));
                if (tier >= 5) lore.add(Component.text("• Blood explosion every 5 hits",
                        NamedTextColor.DARK_RED).decoration(TextDecoration.ITALIC, false));
            }
            case BITE -> {
                mat = Material.BONE;
                cmd = CMD_BITE;
                displayName = "Vampire Bite " + romanTier(tier);
                color = NamedTextColor.DARK_RED;
                lore.add(Component.text("Lunge forward and bite your enemy.",
                        NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text("Restores health on hit.",
                        NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
                if (tier >= 2) lore.add(Component.text("• Chains to nearby enemies",
                        NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
                if (tier >= 3) lore.add(Component.text("• Marks target (+25% dmg)",
                        NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
                if (tier >= 4) lore.add(Component.text("• Executes below 15% HP",
                        NamedTextColor.DARK_RED).decoration(TextDecoration.ITALIC, false));
            }
            case BAT -> {
                mat = Material.PHANTOM_MEMBRANE;
                cmd = CMD_BAT;
                displayName = tier >= 3 ? "Bat Transform " + romanTier(tier)
                        : "Bat Swarm " + romanTier(tier);
                color = NamedTextColor.DARK_PURPLE;
                lore.add(Component.text("Summon bats to attack your enemies.",
                        NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
                if (tier >= 2) lore.add(Component.text("• Invisibility + Speed III for 8s",
                        NamedTextColor.DARK_PURPLE).decoration(TextDecoration.ITALIC, false));
                if (tier >= 3) lore.add(Component.text("• Dissolve into 12 bats",
                        NamedTextColor.DARK_PURPLE).decoration(TextDecoration.ITALIC, false));
                if (tier >= 3) lore.add(Component.text("• Teleport to cursor (40 blocks)",
                        NamedTextColor.DARK_PURPLE).decoration(TextDecoration.ITALIC, false));
                if (tier >= 3) lore.add(Component.text("• Invulnerable for 5s",
                        NamedTextColor.DARK_PURPLE).decoration(TextDecoration.ITALIC, false));
            }
            case NOVA -> {
                mat = Material.REDSTONE_BLOCK;
                cmd = CMD_NOVA;
                displayName = "Blood Nova " + romanTier(tier);
                color = NamedTextColor.DARK_RED;
                lore.add(Component.text("Explode in a wave of blood.",
                        NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text("Damage scales with blood consumed.",
                        NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
                if (tier >= 2) lore.add(Component.text("• Pulls enemies to center",
                        NamedTextColor.DARK_RED).decoration(TextDecoration.ITALIC, false));
                if (tier >= 2) lore.add(Component.text("• Applies Wither II",
                        NamedTextColor.DARK_RED).decoration(TextDecoration.ITALIC, false));
            }
            case DOMAIN -> {
                mat = Material.BLACK_DYE;
                cmd = CMD_DOMAIN;
                displayName = "Domain of Night";
                color = NamedTextColor.BLACK;
                lore.add(Component.text("Unleash the Domain. 30 seconds of eternal night.",
                        NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text("Enemies are blinded and slowed.",
                        NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text("Your abilities cooldown at half rate inside.",
                        NamedTextColor.DARK_RED).decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text("The ultimate Dracula power.",
                        NamedTextColor.BLACK).decoration(TextDecoration.ITALIC, false));
            }
            default -> {
                return new ItemStack(Material.AIR);
            }
        }

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(displayName, color).decorate(TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);
        meta.setCustomModelData(cmd);
        meta.setUnbreakable(true);
        meta.getPersistentDataContainer().set(keyAbilityId, PersistentDataType.STRING, abilityId);
        item.setItemMeta(meta);
        return item;
    }

    /** Returns the ability ID of an item, or null. */
    public String getAbilityId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer()
                .get(keyAbilityId, PersistentDataType.STRING);
    }

    // ── GUI ────────────────────────────────────────────────────────────

    /**
     * Opens the ability selection GUI. Row 0: 5 ability slots (unlocked/locked).
     * Row 1: vampire stats. Row 2: instructions.
     */
    public void openAbilityGui(Player player) {
        VampireState state = vampireManager.getOrCreateState(player.getUniqueId());
        if (!state.isVampire()) {
            player.sendMessage(Component.text("Only vampires have abilities.", NamedTextColor.RED));
            return;
        }
        Inventory gui = Bukkit.createInventory(null, 27,
                Component.text(GUI_TITLE, NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD));

        // Row 0: 5 ability slots at positions 2, 3, 4, 5, 6
        String[] abilities = {CLAW, BITE, BAT, NOVA, DOMAIN};
        int[] slotPositions = {2, 3, 4, 5, 6};
        for (int i = 0; i < abilities.length; i++) {
            String ab = abilities[i];
            int tier = state.getAbilityTier(ab);
            if (tier > 0) {
                gui.setItem(slotPositions[i], createAbilityItem(ab, tier));
            } else {
                gui.setItem(slotPositions[i], createLockedSlot(ab));
            }
        }

        // Row 1: vampire stats display (slot 13 = center)
        gui.setItem(13, createStatsDisplay(state));

        // Row 2: instructions
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.displayName(Component.text("How to use abilities", NamedTextColor.GOLD)
                .decorate(TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
        infoMeta.lore(List.of(
                Component.text("1. Click an unlocked ability to equip it", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("2. It goes into your offhand slot", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("3. Right-click with empty main hand to trigger", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("4. Sneak + Swap Hands (F) to reopen this menu", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        info.setItemMeta(infoMeta);
        gui.setItem(22, info);

        // Fill borders with red glass
        ItemStack filler = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta fMeta = filler.getItemMeta();
        fMeta.displayName(Component.text(" "));
        filler.setItemMeta(fMeta);
        for (int i = 0; i < 27; i++) {
            if (gui.getItem(i) == null) gui.setItem(i, filler);
        }

        player.openInventory(gui);
    }

    private ItemStack createLockedSlot(String abilityId) {
        ItemStack locked = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = locked.getItemMeta();
        String unlockLevel = switch (abilityId) {
            case BITE -> "Vampire";
            case BAT -> "Elder Vampire";
            case NOVA -> "Vampire Lord";
            case DOMAIN -> "Dracula";
            default -> "?";
        };
        meta.displayName(Component.text("Locked Ability", NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("Unlocks at: " + unlockLevel, NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        locked.setItemMeta(meta);
        return locked;
    }

    private ItemStack createStatsDisplay(VampireState state) {
        ItemStack stats = new ItemStack(Material.GHAST_TEAR);
        ItemMeta meta = stats.getItemMeta();
        meta.displayName(Component.text(state.getLevel().getDisplayName(),
                state.getLevel().getColor()).decorate(TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("Blood: " + state.getBloodConsumed() + "/100", NamedTextColor.DARK_RED)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("Evolvers: " + state.getEvolversConsumed() + "/20", NamedTextColor.DARK_PURPLE)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("Super Blood: " + state.getSuperBloodConsumed() + "/5", NamedTextColor.RED)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text(""),
                Component.text("Claw Dmg: " + String.format("%.1f", state.getEffectiveClawDamage()),
                        NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                Component.text("Bite Dmg: " + String.format("%.1f", state.getEffectiveTeethDamage()),
                        NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                Component.text("Sun Immune: " + (state.isSunImmune() ? "YES" : "NO"),
                        state.isSunImmune() ? NamedTextColor.GREEN : NamedTextColor.RED)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        stats.setItemMeta(meta);
        return stats;
    }

    public boolean isAbilityGui(String title) {
        return title != null && title.contains(GUI_TITLE);
    }

    // ── Ability execution ──────────────────────────────────────────────

    /**
     * Central dispatcher — routes ability activations to the correct handler
     * based on ID and the player's current vampire tier.
     */
    public void executeAbility(Player player, String abilityId) {
        VampireState state = vampireManager.getOrCreateState(player.getUniqueId());
        if (!state.isVampire()) return;
        int tier = state.getAbilityTier(abilityId);
        if (tier == 0) {
            player.sendActionBar(Component.text("Ability locked!", NamedTextColor.RED));
            return;
        }
        long cdMs = getCooldownMs(player.getUniqueId(), abilityId);
        if (cdMs > 0) {
            player.sendActionBar(Component.text("Cooldown: " + (cdMs / 1000 + 1) + "s",
                    NamedTextColor.GRAY));
            return;
        }

        switch (abilityId) {
            case CLAW -> executeClaw(player, state, tier);
            case BITE -> executeBite(player, state, tier);
            case BAT -> executeBat(player, state, tier);
            case NOVA -> executeNova(player, state, tier);
            case DOMAIN -> executeDomain(player, state);
        }
    }

    private void executeClaw(Player player, VampireState state, int tier) {
        double range = switch (tier) {
            case 1, 2 -> 3.0;
            case 3 -> 3.5;
            case 4 -> 4.0;
            default -> 5.0;
        };
        double arcDeg = switch (tier) {
            case 1, 2 -> 120;
            case 3 -> 180;
            case 4 -> 220;
            default -> 270;
        };
        double rawDamage = state.getEffectiveClawDamage();
        if (plugin.getCraftedItemListener() != null) {
            rawDamage += plugin.getCraftedItemListener().getCrimsonBonus(player.getUniqueId());
        }
        final double damage = rawDamage;
        double lifestealPct = switch (tier) {
            case 1 -> 0.0;
            case 2 -> 0.15;
            case 3 -> 0.25;
            case 4 -> 0.30;
            default -> 0.40;
        };
        boolean phaseThroughWalls = tier >= 5;

        // Particle slash
        Location origin = player.getEyeLocation();
        Vector forward = origin.getDirection();
        World world = player.getWorld();
        world.playSound(origin, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.2f, 0.7f);
        world.spawnParticle(Particle.SWEEP_ATTACK,
                origin.clone().add(forward.clone().multiply(1.5)), 5, 0.3, 0.3, 0.3, 0);
        world.spawnParticle(Particle.DUST,
                origin.clone().add(forward.clone().multiply(1.5)), 30, 0.8, 0.8, 0.8, 0,
                new Particle.DustOptions(Color.RED, 1.5f));

        // Hit detection — arc in front of player
        List<LivingEntity> hits = new ArrayList<>();
        double halfArcRad = Math.toRadians(arcDeg / 2);
        for (Entity e : world.getNearbyEntities(origin, range, range, range)) {
            if (!(e instanceof LivingEntity le)) continue;
            if (e == player) continue;
            if (e instanceof Player p && isVampireAlly(player, p)) continue;
            Vector toTarget = le.getLocation().add(0, 1, 0).toVector()
                    .subtract(origin.toVector()).normalize();
            double angle = Math.acos(Math.max(-1, Math.min(1, forward.dot(toTarget))));
            if (angle > halfArcRad) continue;
            double dist = le.getLocation().distance(player.getLocation());
            if (dist > range) continue;
            // Line of sight unless phasing
            if (!phaseThroughWalls && !player.hasLineOfSight(le)) continue;
            hits.add(le);
        }

        double totalDealt = 0;
        for (LivingEntity victim : hits) {
            damageMob(player, victim, damage);
            totalDealt += damage;
            world.spawnParticle(Particle.DUST, victim.getLocation().add(0, 1, 0),
                    10, 0.3, 0.3, 0.3, 0, new Particle.DustOptions(Color.RED, 1.2f));
        }

        // Lifesteal
        if (lifestealPct > 0 && totalDealt > 0) {
            healPlayer(player, totalDealt * lifestealPct);
        }

        // Tier 3+: 3-hit combo — schedule two more strikes
        if (tier >= 3) {
            new BukkitRunnable() {
                int strike = 0;
                @Override public void run() {
                    if (++strike > 2 || !player.isOnline()) { cancel(); return; }
                    for (LivingEntity victim : hits) {
                        if (victim.isDead() || !victim.isValid()) continue;
                        damageMob(player, victim, damage * 0.6);
                        world.spawnParticle(Particle.DUST, victim.getLocation().add(0, 1, 0),
                                5, 0.2, 0.2, 0.2, 0, new Particle.DustOptions(Color.RED, 1.0f));
                    }
                    world.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.8f, 1.1f);
                }
            }.runTaskTimer(plugin, 4L, 4L);
        }

        // Tier 4: shockwave projectile
        if (tier >= 4) {
            new BukkitRunnable() {
                int step = 0;
                @Override public void run() {
                    if (++step > 12 || !player.isOnline()) { cancel(); return; }
                    Location p = origin.clone().add(forward.clone().multiply(step * 0.5));
                    world.spawnParticle(Particle.DUST, p, 15, 0.8, 0.8, 0.8, 0,
                            new Particle.DustOptions(Color.RED, 2.0f));
                    world.spawnParticle(Particle.SWEEP_ATTACK, p, 2, 0.4, 0.4, 0.4, 0);
                    for (Entity e : world.getNearbyEntities(p, 1.5, 1.5, 1.5)) {
                        if (!(e instanceof LivingEntity le) || e == player) continue;
                        if (e instanceof Player pp && isVampireAlly(player, pp)) continue;
                        if (hits.contains(le)) continue;
                        damageMob(player, le, damage * 0.6);
                        hits.add(le);
                    }
                }
            }.runTaskTimer(plugin, 2L, 2L);
        }

        // Tier 5: every 5th hit → blood explosion
        if (tier >= 5 && !hits.isEmpty()) {
            int count = clawHitCounter.getOrDefault(player.getUniqueId(), 0) + 1;
            clawHitCounter.put(player.getUniqueId(), count);
            if (count % 5 == 0) {
                Location center = hits.get(0).getLocation();
                world.spawnParticle(Particle.EXPLOSION_EMITTER, center, 1);
                world.spawnParticle(Particle.DUST, center, 80, 3, 3, 3, 0,
                        new Particle.DustOptions(Color.fromRGB(120, 0, 0), 3.0f));
                world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.6f);
                for (Entity e : world.getNearbyEntities(center, 3, 3, 3)) {
                    if (!(e instanceof LivingEntity le) || e == player) continue;
                    damageMob(player, le, damage * 1.2);
                }
            }
        }

        setCooldown(player.getUniqueId(), abilityCdMs(CLAW, tier, player));
    }

    private void executeBite(Player player, VampireState state, int tier) {
        double lunge = switch (tier) { case 1 -> 3; case 2 -> 5; default -> 6; };
        double damage = state.getEffectiveTeethDamage();
        double healPct = switch (tier) { case 1 -> 0.50; case 2 -> 0.60; case 3 -> 0.70; default -> 1.0; };
        int chainCount = switch (tier) { case 1 -> 0; case 2 -> 2; case 3 -> 3; default -> 99; };
        boolean execute = tier >= 4;

        World world = player.getWorld();

        // Lunge effect: velocity boost forward
        Vector boost = player.getLocation().getDirection().normalize().multiply(lunge * 0.35);
        boost.setY(Math.max(boost.getY(), 0.2));
        player.setVelocity(boost);
        world.playSound(player.getLocation(), Sound.ENTITY_WITHER_SHOOT, 1.2f, 0.5f);
        world.spawnParticle(Particle.DUST, player.getLocation().add(0, 1, 0),
                30, 0.6, 0.6, 0.6, 0, new Particle.DustOptions(Color.fromRGB(140, 0, 0), 1.5f));

        // Find primary target
        LivingEntity primary = null;
        for (Entity e : world.getNearbyEntities(player.getLocation(), lunge + 1, 3, lunge + 1)) {
            if (!(e instanceof LivingEntity le) || e == player) continue;
            if (e instanceof Player p && isVampireAlly(player, p)) continue;
            if (!player.hasLineOfSight(le)) continue;
            if (primary == null || le.getLocation().distance(player.getLocation())
                    < primary.getLocation().distance(player.getLocation())) {
                primary = le;
            }
        }

        if (primary == null) {
            player.sendActionBar(Component.text("No target in range.", NamedTextColor.GRAY));
            setCooldown(player.getUniqueId(), 1000);  // Short penalty CD
            return;
        }

        applyBite(player, state, primary, damage, healPct, execute, world);

        // Chain to nearby enemies
        if (chainCount > 0) {
            List<LivingEntity> chained = new ArrayList<>();
            chained.add(primary);
            for (Entity e : world.getNearbyEntities(primary.getLocation(), 4, 4, 4)) {
                if (!(e instanceof LivingEntity le) || chained.contains(le)) continue;
                if (e == player) continue;
                if (e instanceof Player p && isVampireAlly(player, p)) continue;
                chained.add(le);
                applyBite(player, state, le, damage * 0.75, healPct * 0.5, execute, world);
                if (chained.size() - 1 >= chainCount) break;
            }
        }

        // Tier 3+: mark the primary for 8s (25% bonus damage from caster)
        if (tier >= 3) {
            long until = System.currentTimeMillis() + 8000;
            primary.getPersistentDataContainer().set(keyBiteMark, PersistentDataType.LONG, until);
            primary.getPersistentDataContainer().set(keyBiteMarker, PersistentDataType.STRING,
                    player.getUniqueId().toString());
        }

        setCooldown(player.getUniqueId(), abilityCdMs(BITE, tier, player));
    }

    private void applyBite(Player player, VampireState state, LivingEntity victim, double dmg,
                           double healPct, boolean executeLowHP, World world) {
        // Execute below 15% HP
        double vHp = victim.getHealth();
        double vMax = victim.getAttribute(Attribute.MAX_HEALTH) != null
                ? victim.getAttribute(Attribute.MAX_HEALTH).getValue() : vHp;
        if (executeLowHP && vHp / vMax <= 0.15) {
            damageMob(player, victim, vHp + 100);  // Ensure kill
            healPlayer(player, player.getAttribute(Attribute.MAX_HEALTH).getValue());  // Full heal
        } else {
            damageMob(player, victim, dmg);
            healPlayer(player, dmg * healPct);
        }
        world.spawnParticle(Particle.DUST, victim.getLocation().add(0, 1, 0),
                25, 0.4, 0.4, 0.4, 0, new Particle.DustOptions(Color.RED, 1.8f));
        world.playSound(victim.getLocation(), Sound.ENTITY_WITHER_HURT, 0.8f, 1.4f);
    }

    private void executeBat(Player player, VampireState state, int tier) {
        World world = player.getWorld();
        Location loc = player.getLocation();

        if (tier == 1) {
            // Summon 8 bats
            summonBats(player, loc, 8, 6);
            world.playSound(loc, Sound.ENTITY_BAT_TAKEOFF, 1.5f, 0.8f);
            setCooldown(player.getUniqueId(), abilityCdMs(BAT, tier, player));
            return;
        }
        if (tier == 2) {
            // Bats + invisibility + speed
            summonBats(player, loc, 8, 6);
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 160, 0, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 160, 2, false, false));
            world.playSound(loc, Sound.ENTITY_BAT_TAKEOFF, 1.5f, 1.2f);
            world.spawnParticle(Particle.CLOUD, loc.add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.05);
            setCooldown(player.getUniqueId(), abilityCdMs(BAT, tier, player));
            return;
        }
        // Tier 3: FULL BAT FORM — player literally becomes a flying bat for
        // 8 seconds. Invisible, can fly freely (creative-style), a shadow
        // bat entity glued to their position so others see a bat in the sky.
        // At the end of the duration, reverts automatically. Alternative
        // use for enemies: any mob within 4 blocks of the player during the
        // transform takes 20 damage (the "bat dive" hit).

        // Dissolve visual
        world.playSound(loc, Sound.ENTITY_BAT_TAKEOFF, 2f, 0.5f);
        world.spawnParticle(Particle.CLOUD, loc.add(0, 1, 0), 60, 0.8, 0.8, 0.8, 0.1);
        world.spawnParticle(Particle.DUST, loc.add(0, 1, 0), 60, 0.8, 0.8, 0.8, 0,
                new Particle.DustOptions(Color.fromRGB(120, 0, 0), 2.0f));
        summonBats(player, loc, 6, 8);

        // Apply invisibility + resistance for the duration
        final int durationTicks = 160; // 8 seconds
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, durationTicks, 4, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, durationTicks, 0, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, durationTicks, 1, false, false));

        // Elytra glide for the duration — visually matches the vampire bat-form animation
        final boolean wasAllowFlight = player.getAllowFlight();
        final boolean wasFlying = player.isFlying();
        final boolean wasGliding = player.isGliding();
        player.setAllowFlight(true);
        player.setGliding(true);
        // Give a small upward boost so the glide has air to work with
        player.setVelocity(player.getVelocity().add(new org.bukkit.util.Vector(0, 0.8, 0)));

        // Spawn the shadow bat that visually represents the player's form
        Bat shadowBat = (Bat) world.spawnEntity(player.getLocation().add(0, 0.5, 0), EntityType.BAT);
        shadowBat.setCustomName("§4§l" + player.getName());
        shadowBat.setCustomNameVisible(false);
        shadowBat.setInvulnerable(true);
        shadowBat.setSilent(true);
        shadowBat.setPersistent(true);

        // Initial bat-dive AoE damage at the player's starting location
        for (Entity e : world.getNearbyEntities(loc, 4, 4, 4)) {
            if (!(e instanceof LivingEntity le) || e == player) continue;
            if (e instanceof Player p && isVampireAlly(player, p)) continue;
            damageMob(player, le, 20);
        }

        // Per-tick task: keep the shadow bat glued to the player's location
        new BukkitRunnable() {
            int t = 0;
            @Override public void run() {
                t += 2;
                if (t >= durationTicks || !player.isOnline() || shadowBat.isDead()) {
                    shadowBat.remove();
                    if (player.isOnline()) {
                        player.setGliding(wasGliding);
                        player.setFlying(wasFlying);
                        player.setAllowFlight(wasAllowFlight);
                        player.getWorld().playSound(player.getLocation(),
                                Sound.ENTITY_BAT_DEATH, 1.2f, 0.6f);
                        player.getWorld().spawnParticle(Particle.SMOKE,
                                player.getLocation().add(0, 1, 0), 20, 0.4, 0.4, 0.4, 0.02);
                    }
                    cancel();
                    return;
                }
                if (!shadowBat.isDead()) {
                    shadowBat.teleport(player.getLocation().add(0, 0.6, 0));
                    player.getWorld().spawnParticle(Particle.DUST,
                            player.getLocation().add(0, 0.8, 0), 2, 0.3, 0.3, 0.3, 0,
                            new Particle.DustOptions(Color.fromRGB(60, 0, 0), 1.0f));
                }
                // Re-apply gliding each tick so Paper doesn't drop the state on ground contact
                if (!player.isGliding()) player.setGliding(true);
            }
        }.runTaskTimer(plugin, 0L, 2L);

        setCooldown(player.getUniqueId(), abilityCdMs(BAT, tier, player));
    }

    /** Summons attack bats using vanilla Bat entities with AI overrides. */
    private void summonBats(Player owner, Location loc, int count, int durationSeconds) {
        World world = loc.getWorld();
        List<Bat> bats = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Bat bat = (Bat) world.spawnEntity(loc.clone().add(
                    Math.random() * 2 - 1, 1 + Math.random(), Math.random() * 2 - 1),
                    EntityType.BAT);
            bat.setCustomName("§4Vampire Bat");
            bats.add(bat);
        }
        // Attack + expire task
        final int expireTicks = durationSeconds * 20;
        new BukkitRunnable() {
            int ticks = 0;
            @Override public void run() {
                if (ticks >= expireTicks || !owner.isOnline()) {
                    for (Bat b : bats) if (!b.isDead()) b.remove();
                    cancel();
                    return;
                }
                LivingEntity nearest = null;
                double best = 16 * 16;
                for (Entity e : world.getNearbyEntities(owner.getLocation(), 16, 16, 16)) {
                    if (!(e instanceof LivingEntity le) || e == owner || e instanceof Bat) continue;
                    if (e instanceof Player p && isVampireAlly(owner, p)) continue;
                    double d = e.getLocation().distanceSquared(owner.getLocation());
                    if (d < best) { best = d; nearest = le; }
                }
                if (nearest != null) {
                    Location nl = nearest.getLocation();
                    for (Bat b : bats) {
                        if (b.isDead()) continue;
                        b.teleport(nl.clone().add(Math.random() * 2 - 1, 1 + Math.random(),
                                Math.random() * 2 - 1));
                        if (ticks % 10 == 0) {
                            damageMob(owner, nearest, 3);
                            world.spawnParticle(Particle.DUST, nearest.getLocation().add(0, 1, 0),
                                    5, 0.2, 0.2, 0.2, 0, new Particle.DustOptions(Color.RED, 1.0f));
                        }
                    }
                }
                ticks++;
            }
        }.runTaskTimer(plugin, 2L, 2L);
    }

    private void executeNova(Player player, VampireState state, int tier) {
        World world = player.getWorld();
        Location center = player.getLocation();
        int blood = state.getBloodConsumed();
        int evolvers = state.getEvolversConsumed();
        double radius = tier == 1 ? 6.0 : 10.0;
        double damage = tier == 1 ? (15 + blood * 0.1)
                : (25 + blood * 0.15 + evolvers * 1.5);
        double healPct = tier == 1 ? 0.25 : 0.40;
        boolean pull = tier >= 2;
        boolean wither = tier >= 2;

        world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 2f, 0.5f);
        world.playSound(center, Sound.ITEM_TOTEM_USE, 1.5f, 0.8f);

        // Pull phase (tier 2+)
        if (pull) {
            for (Entity e : world.getNearbyEntities(center, radius * 1.5, radius * 1.5, radius * 1.5)) {
                if (!(e instanceof LivingEntity le) || e == player) continue;
                if (e instanceof Player p && isVampireAlly(player, p)) continue;
                Vector dir = center.toVector().subtract(le.getLocation().toVector()).normalize().multiply(1.2);
                dir.setY(0.4);
                le.setVelocity(dir);
            }
        }

        // Detonation after 15 ticks (gives pull time to work)
        final double finalDmg = damage;
        new BukkitRunnable() {
            @Override public void run() {
                if (!player.isOnline()) return;
                // Dust sphere
                for (int i = 0; i < 90; i++) {
                    double phi = Math.acos(1 - 2 * (i + 0.5) / 90.0);
                    double theta = Math.PI * (1 + Math.sqrt(5)) * i;
                    double x = radius * Math.sin(phi) * Math.cos(theta);
                    double y = radius * Math.cos(phi);
                    double z = radius * Math.sin(phi) * Math.sin(theta);
                    world.spawnParticle(Particle.DUST, center.clone().add(x, y, z), 1, 0, 0, 0, 0,
                            new Particle.DustOptions(Color.fromRGB(180, 0, 0), 2.0f));
                }
                world.spawnParticle(Particle.EXPLOSION_EMITTER, center, 3, 1, 1, 1, 0);
                double totalDealt = 0;
                for (Entity e : world.getNearbyEntities(center, radius, radius, radius)) {
                    if (!(e instanceof LivingEntity le) || e == player) continue;
                    if (e instanceof Player p && isVampireAlly(player, p)) continue;
                    double d = le.getLocation().distance(center);
                    if (d > radius) continue;
                    damageMob(player, le, finalDmg);
                    totalDealt += finalDmg;
                    if (wither) {
                        le.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 1));
                    }
                }
                if (totalDealt > 0) {
                    healPlayer(player, totalDealt * healPct);
                }
            }
        }.runTaskLater(plugin, pull ? 15L : 2L);

        setCooldown(player.getUniqueId(), abilityCdMs(NOVA, tier, player));
    }

    private void executeDomain(Player player, VampireState state) {
        World world = player.getWorld();
        Location center = player.getLocation();
        double radius = 30.0;
        int durationTicks = 30 * 20;

        world.playSound(center, Sound.ENTITY_WITHER_SPAWN, 3f, 0.3f);
        world.playSound(center, Sound.ENTITY_ENDER_DRAGON_GROWL, 3f, 0.5f);
        world.strikeLightningEffect(center);

        // Save the world's previous time to restore when the domain ends
        final long previousTime = world.getTime();
        world.setTime(18000);  // Midnight

        activeDomains.put(player.getUniqueId(), System.currentTimeMillis() + durationTicks * 50L);

        // Vampire self-buff
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, durationTicks, 2));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, durationTicks, 2));
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, durationTicks, 2));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, durationTicks, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, durationTicks, 0));

        new BukkitRunnable() {
            int ticks = 0;
            @Override public void run() {
                if (ticks >= durationTicks || !player.isOnline()) {
                    activeDomains.remove(player.getUniqueId());
                    world.setTime(previousTime);
                    if (player.isOnline()) {
                        player.sendMessage(Component.text("The Domain fades...", NamedTextColor.DARK_GRAY));
                    }
                    cancel();
                    return;
                }

                // Dome boundary particles
                if (ticks % 4 == 0) {
                    for (int i = 0; i < 30; i++) {
                        double theta = Math.PI * 2 * Math.random();
                        double phi = Math.acos(2 * Math.random() - 1);
                        double x = radius * Math.sin(phi) * Math.cos(theta);
                        double y = radius * Math.cos(phi);
                        double z = radius * Math.sin(phi) * Math.sin(theta);
                        if (y < 0) y = -y;  // Upper dome only
                        world.spawnParticle(Particle.DUST, center.clone().add(x, y, z), 1, 0, 0, 0, 0,
                                new Particle.DustOptions(Color.fromRGB(60, 0, 0), 1.5f));
                    }
                }

                // Heartbeat sound
                if (ticks % 20 == 0) {
                    world.playSound(center, Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 1.5f, 0.5f);
                }

                // Random lightning strikes
                if (ticks % 40 == 0) {
                    double dx = (Math.random() - 0.5) * radius;
                    double dz = (Math.random() - 0.5) * radius;
                    world.strikeLightningEffect(center.clone().add(dx, 0, dz));
                }

                // Debuff enemies inside
                for (Entity e : world.getNearbyEntities(center, radius, radius, radius)) {
                    if (!(e instanceof LivingEntity le) || e == player) continue;
                    if (e instanceof Player p && (vampireManager.isVampire(p) || isVampireAlly(player, p))) continue;
                    le.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 60, 0, false, false));
                    le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1, false, false));
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        setCooldown(player.getUniqueId(), 120_000);
    }

    // ── Helpers ────────────────────────────────────────────────────────

    /**
     * Damages an entity using the custom mob system if applicable, else vanilla.
     */
    private void damageMob(Player attacker, LivingEntity victim, double amount) {
        if (victim.isDead() || !victim.isValid()) return;
        // Check for custom mob
        if (plugin.getCustomMobManager() != null) {
            var cm = plugin.getCustomMobManager().getByEntity(victim);
            if (cm != null) {
                cm.damage(amount, attacker);
                return;
            }
        }
        // Vanilla damage
        victim.damage(amount, attacker);
    }

    /** Heals a player safely, clamped to max HP. */
    private void healPlayer(Player player, double amount) {
        double max = player.getAttribute(Attribute.MAX_HEALTH) != null
                ? player.getAttribute(Attribute.MAX_HEALTH).getValue() : 20;
        player.setHealth(Math.min(max, player.getHealth() + amount));
    }

    /**
     * Returns whether two players are vampire allies (same guild or both vampires).
     * Used to prevent friendly fire between vampires in the same party.
     */
    private boolean isVampireAlly(Player caster, Player other) {
        if (caster.equals(other)) return true;
        if (plugin.getGuildManager() != null
                && plugin.getGuildManager().areInSameGuild(caster.getUniqueId(), other.getUniqueId())) {
            return true;
        }
        return false;
    }

    public boolean isInDomain(Player player) {
        Long expiry = activeDomains.get(player.getUniqueId());
        return expiry != null && expiry > System.currentTimeMillis();
    }

    private long abilityCdMs(String abilityId, int tier, Player player) {
        long base = switch (abilityId) {
            case CLAW -> 1500L;
            case BITE -> tier <= 1 ? 5000L : tier == 2 ? 4000L : tier == 3 ? 4000L : 3000L;
            case BAT -> tier == 1 ? 15000L : tier == 2 ? 20000L : 25000L;
            case NOVA -> tier == 1 ? 25000L : 20000L;
            case DOMAIN -> 120000L;
            default -> 3000L;
        };
        if (isInDomain(player)) base /= 2;
        return base;
    }

    private void setCooldown(UUID playerId, long durationMs) {
        // Determine which ability — pull from the player's offhand
        Player p = Bukkit.getPlayer(playerId);
        if (p == null) return;
        String id = getAbilityId(p.getInventory().getItemInOffHand());
        if (id == null) return;
        cooldowns.computeIfAbsent(playerId, k -> new HashMap<>())
                .put(id, System.currentTimeMillis() + durationMs);
    }

    public long getCooldownMs(UUID playerId, String abilityId) {
        Map<String, Long> m = cooldowns.get(playerId);
        if (m == null) return 0;
        Long expiry = m.get(abilityId);
        if (expiry == null) return 0;
        long rem = expiry - System.currentTimeMillis();
        return Math.max(0, rem);
    }

    private String romanTier(int tier) {
        return switch (tier) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> "";
        };
    }

    private void startCooldownCleanup() {
        new BukkitRunnable() {
            @Override public void run() {
                long now = System.currentTimeMillis();
                cooldowns.values().forEach(m -> m.entrySet().removeIf(e -> e.getValue() < now));
                cooldowns.entrySet().removeIf(e -> e.getValue().isEmpty());
                activeDomains.entrySet().removeIf(e -> e.getValue() < now);
            }
        }.runTaskTimer(plugin, 600L, 600L);  // Every 30s
    }

    private void startDomainTask() {
        // No-op placeholder: domain effects applied per-cast via the per-tick BukkitRunnable
    }

    // ── Bite mark lookup (used by listener) ────────────────────────────

    public double getBiteMarkMultiplier(LivingEntity victim, Player attacker) {
        Long until = victim.getPersistentDataContainer().get(keyBiteMark, PersistentDataType.LONG);
        if (until == null || until < System.currentTimeMillis()) return 1.0;
        String marker = victim.getPersistentDataContainer().get(keyBiteMarker, PersistentDataType.STRING);
        if (marker == null || !marker.equals(attacker.getUniqueId().toString())) return 1.0;
        return 1.25;
    }
}

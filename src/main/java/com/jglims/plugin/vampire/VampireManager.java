package com.jglims.plugin.vampire;

import com.jglims.plugin.JGlimsPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.*;
import java.util.*;

/**
 * Central manager for the Vampire transformation system.
 * Tracks which players are vampires, their progression state,
 * and handles persistence to flat files.
 */
public class VampireManager {

    private final JGlimsPlugin plugin;
    private final Map<UUID, VampireState> vampireStates = new HashMap<>();
    private final File dataFolder;

    /** NamespacedKey for vampire-specific items in PersistentDataContainer. */
    private final NamespacedKey keyVampireItem;
    private final NamespacedKey keyVampireEssence;
    private final NamespacedKey keyVampireBlood;
    private final NamespacedKey keyVampireEvolver;
    private final NamespacedKey keySuperBlood;
    private final NamespacedKey keyVampireRing;

    public VampireManager(JGlimsPlugin plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "vampires");
        if (!dataFolder.exists()) dataFolder.mkdirs();

        keyVampireItem = new NamespacedKey(plugin, "vampire_item");
        keyVampireEssence = new NamespacedKey(plugin, "vampire_essence");
        keyVampireBlood = new NamespacedKey(plugin, "vampire_blood");
        keyVampireEvolver = new NamespacedKey(plugin, "vampire_evolver");
        keySuperBlood = new NamespacedKey(plugin, "super_blood");
        keyVampireRing = new NamespacedKey(plugin, "vampire_ring");

        loadAll();
    }

    // ── Transformation ─────────────────────────────────────────────────

    /**
     * Transforms a player into a vampire. Wipes all existing plugin buffs.
     *
     * @param player the player to transform
     */
    public void transformPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        VampireState state = getOrCreateState(uuid);

        if (state.isVampire()) {
            player.sendMessage(Component.text("You are already a vampire!", NamedTextColor.RED));
            return;
        }

        // Block werewolves from becoming vampires (symmetrical guard).
        if (plugin.getWerewolfManager() != null && plugin.getWerewolfManager().isWerewolf(player)) {
            player.sendMessage(Component.text("Your wolf blood rejects the vampire curse.",
                    NamedTextColor.RED));
            return;
        }

        // Check: player must have empty armor slots
        if (!isArmorEmpty(player)) {
            player.sendMessage(Component.text("Remove ALL armor before transforming!",
                    NamedTextColor.RED));
            return;
        }

        // Check: player must have empty main inventory + offhand.
        for (ItemStack it : player.getInventory().getContents()) {
            if (it != null && it.getType() != Material.AIR) {
                player.sendMessage(Component.text("You must empty your entire inventory before transforming.",
                        NamedTextColor.RED));
                return;
            }
        }

        // Wipe existing plugin buffs
        wipePlayerBuffs(player);

        // Transform
        state.setVampire(true);
        state.setLevel(VampireLevel.FLEDGLING);
        state.recalculateLevel();

        // Visual effects
        player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.DARKNESS, 40, 0, false, false));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.5f);
        player.getWorld().spawnParticle(Particle.DUST,
                player.getLocation().add(0, 1, 0), 50, 0.5, 1, 0.5, 0,
                new Particle.DustOptions(Color.RED, 2.0f));

        player.sendMessage(Component.text(""));
        player.sendMessage(Component.text("You have become a VAMPIRE!",
                VampireLevel.FLEDGLING.getColor()).decorate(TextDecoration.BOLD));
        player.sendMessage(Component.text("All previous buffs have been wiped.",
                NamedTextColor.GRAY));
        player.sendMessage(Component.text("Your only weapons are now your vampire abilities.",
                NamedTextColor.GRAY));
        player.sendMessage(Component.text("Tip: sneak + swap-hands (F) to open your ability menu.",
                NamedTextColor.GOLD));
        player.sendMessage(Component.text("Or run: /jglims vampire " + player.getName() + " abilities",
                NamedTextColor.GRAY));
        player.sendMessage(Component.text(""));

        save(state);
        plugin.getLogger().info("[Vampire] " + player.getName() + " transformed into a vampire.");
    }

    /**
     * Removes vampirism from a player. Does NOT restore previous buffs.
     *
     * @param player the player to cure
     */
    public void removeVampirism(Player player) {
        VampireState state = getOrCreateState(player.getUniqueId());
        state.setVampire(false);
        state.setLevel(VampireLevel.FLEDGLING);
        state.setBloodConsumed(0);
        state.setEvolversConsumed(0);
        state.setSuperBloodConsumed(0);
        state.setHasVampireRing(false);
        state.recalculateLevel();
        save(state);

        // Revoke vampire flight
        if (player.getGameMode() != org.bukkit.GameMode.CREATIVE
                && player.getGameMode() != org.bukkit.GameMode.SPECTATOR) {
            player.setGliding(false);
            player.setAllowFlight(false);
            player.setFlying(false);
            player.setFlySpeed(0.05f);
        }

        // Clear vampire-applied potion effects
        player.removePotionEffect(org.bukkit.potion.PotionEffectType.NIGHT_VISION);
        player.removePotionEffect(org.bukkit.potion.PotionEffectType.SPEED);
        player.removePotionEffect(org.bukkit.potion.PotionEffectType.JUMP_BOOST);
        player.removePotionEffect(org.bukkit.potion.PotionEffectType.WATER_BREATHING);

        // Reset max health to default
        var healthAttr = player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH);
        if (healthAttr != null) healthAttr.setBaseValue(20.0);
        player.setHealth(Math.min(player.getHealth(), 20.0));

        player.sendMessage(Component.text("Your vampirism has been removed. You are human again.",
                NamedTextColor.GREEN));
        plugin.getLogger().info("[Vampire] " + player.getName() + " vampirism removed.");
    }

    // ── Consumable Processing ──────────────────────────────────────────

    /**
     * Processes consumption of a vampire blood item.
     *
     * @param player the vampire player
     */
    public void consumeBlood(Player player) {
        VampireState state = getOrCreateState(player.getUniqueId());
        if (!state.isVampire()) return;
        if (state.getBloodConsumed() >= 50) {
            player.sendMessage(Component.text("Your body is saturated with blood. (50/50)",
                    NamedTextColor.YELLOW));
            return;
        }

        state.setBloodConsumed(state.getBloodConsumed() + 1);
        VampireLevel oldLevel = state.getLevel();
        state.recalculateLevel();

        player.sendMessage(Component.text("Blood consumed! (" + state.getBloodConsumed() + "/50)",
                NamedTextColor.DARK_RED));

        if (state.getLevel() != oldLevel) {
            announceEvolution(player, state.getLevel());
        }

        // Restore some hunger
        player.setFoodLevel(Math.min(20, player.getFoodLevel() + 4));
        player.setSaturation(Math.min(20f, player.getSaturation() + 2f));

        save(state);
    }

    /**
     * Processes consumption of a vampire evolver item.
     *
     * @param player the vampire player
     */
    public void consumeEvolver(Player player) {
        VampireState state = getOrCreateState(player.getUniqueId());
        if (!state.isVampire()) return;
        if (state.getEvolversConsumed() >= 10) {
            player.sendMessage(Component.text("You have consumed the maximum evolvers!",
                    NamedTextColor.YELLOW));
            return;
        }

        state.setEvolversConsumed(state.getEvolversConsumed() + 1);
        VampireLevel oldLevel = state.getLevel();
        state.recalculateLevel();

        player.sendMessage(Component.text("Evolver consumed! (" + state.getEvolversConsumed() + "/10)",
                NamedTextColor.DARK_PURPLE));

        if (state.getLevel() != oldLevel) {
            announceEvolution(player, state.getLevel());
        }

        save(state);
    }

    /**
     * Processes consumption of super blood.
     *
     * @param player the vampire player
     */
    public void consumeSuperBlood(Player player) {
        VampireState state = getOrCreateState(player.getUniqueId());
        if (!state.isVampire()) return;
        if (state.getSuperBloodConsumed() >= 3) {
            player.sendMessage(Component.text("You have reached the pinnacle of the curse. (3/3)",
                    NamedTextColor.YELLOW));
            return;
        }

        state.setSuperBloodConsumed(state.getSuperBloodConsumed() + 1);
        VampireLevel oldLevel = state.getLevel();
        state.recalculateLevel();

        player.sendMessage(Component.text("Super Blood consumed! ("
                + state.getSuperBloodConsumed() + "/3)", NamedTextColor.DARK_RED));

        if (state.getLevel() != oldLevel) {
            announceEvolution(player, state.getLevel());
        }

        save(state);
    }

    /**
     * Applies the vampire ring (permanent sun immunity).
     *
     * @param player the vampire player
     */
    public void applyVampireRing(Player player) {
        VampireState state = getOrCreateState(player.getUniqueId());
        if (!state.isVampire()) return;
        state.setHasVampireRing(true);
        save(state);
        player.sendMessage(Component.text("Vampire Ring applied! You are now immune to sunlight.",
                NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
    }

    // ── Item Creation ──────────────────────────────────────────────────

    /**
     * Creates a Vampire Essence item (dropped by the Nether King).
     */
    public ItemStack createVampireEssence() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Vampire Essence", VampireLevel.FLEDGLING.getColor())
                .decorate(TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text(""),
                Component.text("Right-click with empty armor", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("to become a vampire.", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text(""),
                Component.text("WARNING: ", NamedTextColor.RED).decorate(TextDecoration.BOLD)
                        .append(Component.text("All buffs will be wiped!", NamedTextColor.RED))
                        .decoration(TextDecoration.ITALIC, false)
        ));
        meta.setCustomModelData(40002);
        meta.getPersistentDataContainer().set(keyVampireEssence, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Creates a Vampire Blood item (dropped by villagers killed by vampires).
     */
    public ItemStack createVampireBlood() {
        ItemStack item = new ItemStack(Material.REDSTONE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Vampire Blood", NamedTextColor.DARK_RED)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("Consume to advance vampire power.", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        meta.setCustomModelData(40001);
        meta.getPersistentDataContainer().set(keyVampireBlood, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Creates a Vampire Evolver item (found in End City chests).
     */
    public ItemStack createVampireEvolver() {
        ItemStack item = new ItemStack(Material.ECHO_SHARD);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Vampire Evolver", NamedTextColor.DARK_PURPLE)
                .decorate(TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("Consume to evolve your vampirism.", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        meta.setCustomModelData(40003);
        meta.getPersistentDataContainer().set(keyVampireEvolver, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Creates a Super Blood item (dropped by any mob killed by a vampire in the Abyss).
     */
    public ItemStack createSuperBlood() {
        ItemStack item = new ItemStack(Material.MAGMA_CREAM);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Super Blood", NamedTextColor.DARK_RED)
                .decorate(TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("The ultimate vampire fuel.", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("Consume 5 to become Dracula.", NamedTextColor.DARK_RED)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        meta.setCustomModelData(40004);
        meta.getPersistentDataContainer().set(keySuperBlood, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Creates a Vampire Ring item (crafted: Glowstone + Netherite Ingot).
     */
    public ItemStack createVampireRing() {
        ItemStack item = new ItemStack(Material.GOLD_NUGGET);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Vampire Ring", NamedTextColor.GOLD)
                .decorate(TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("Consume to gain permanent sun immunity.", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        meta.setCustomModelData(40005);
        meta.getPersistentDataContainer().set(keyVampireRing, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    // ── Item Identification ────────────────────────────────────────────

    public boolean isVampireEssence(ItemStack item) {
        return item != null && item.hasItemMeta()
                && item.getItemMeta().getPersistentDataContainer().has(keyVampireEssence);
    }

    public boolean isVampireBlood(ItemStack item) {
        return item != null && item.hasItemMeta()
                && item.getItemMeta().getPersistentDataContainer().has(keyVampireBlood);
    }

    public boolean isVampireEvolver(ItemStack item) {
        return item != null && item.hasItemMeta()
                && item.getItemMeta().getPersistentDataContainer().has(keyVampireEvolver);
    }

    public boolean isSuperBlood(ItemStack item) {
        return item != null && item.hasItemMeta()
                && item.getItemMeta().getPersistentDataContainer().has(keySuperBlood);
    }

    public boolean isVampireRingItem(ItemStack item) {
        return item != null && item.hasItemMeta()
                && item.getItemMeta().getPersistentDataContainer().has(keyVampireRing);
    }

    // ── State Access ───────────────────────────────────────────────────

    /**
     * Returns the vampire state for a player, creating one if needed.
     */
    public VampireState getOrCreateState(UUID playerUUID) {
        return vampireStates.computeIfAbsent(playerUUID, VampireState::new);
    }

    /**
     * Whether the player is currently a vampire.
     */
    public boolean isVampire(UUID playerUUID) {
        VampireState state = vampireStates.get(playerUUID);
        return state != null && state.isVampire();
    }

    /**
     * Whether the player is currently a vampire.
     */
    public boolean isVampire(Player player) {
        return isVampire(player.getUniqueId());
    }

    /**
     * Shows vampire info to a command sender.
     */
    public void showVampireInfo(Player target, org.bukkit.command.CommandSender viewer) {
        VampireState state = getOrCreateState(target.getUniqueId());
        viewer.sendMessage(Component.text("=== Vampire Info: " + target.getName() + " ===",
                NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD));
        if (!state.isVampire()) {
            viewer.sendMessage(Component.text("Not a vampire.", NamedTextColor.GRAY));
            return;
        }
        viewer.sendMessage(Component.text("Level: ", NamedTextColor.GRAY)
                .append(Component.text(state.getLevel().getDisplayName(), state.getLevel().getColor())));
        viewer.sendMessage(Component.text("Blood: " + state.getBloodConsumed() + "/50", NamedTextColor.GRAY));
        viewer.sendMessage(Component.text("Evolvers: " + state.getEvolversConsumed() + "/10", NamedTextColor.GRAY));
        viewer.sendMessage(Component.text("Super Blood: " + state.getSuperBloodConsumed() + "/3", NamedTextColor.GRAY));
        viewer.sendMessage(Component.text("Ring: " + (state.hasVampireRing() ? "Yes" : "No"), NamedTextColor.GRAY));
        viewer.sendMessage(Component.text("Claw Dmg: " + String.format("%.1f", state.getEffectiveClawDamage()), NamedTextColor.GRAY));
        viewer.sendMessage(Component.text("Sun Immune: " + state.isSunImmune(), NamedTextColor.GRAY));
    }

    // ── Helpers ─────────────────────────────────────────────────────────

    private boolean isArmorEmpty(Player player) {
        var equipment = player.getInventory();
        return (equipment.getHelmet() == null || equipment.getHelmet().getType() == Material.AIR)
                && (equipment.getChestplate() == null || equipment.getChestplate().getType() == Material.AIR)
                && (equipment.getLeggings() == null || equipment.getLeggings().getType() == Material.AIR)
                && (equipment.getBoots() == null || equipment.getBoots().getType() == Material.AIR);
    }

    /**
     * Wipes all existing plugin buffs from the player. Called during transformation.
     * Note: resetPlayer methods will be added to managers in a later phase.
     * For now, this logs the intent and clears potion effects.
     */
    private void wipePlayerBuffs(Player player) {
        // Clear all active potion effects
        for (org.bukkit.potion.PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        // Reset max health to default
        var healthAttr = player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH);
        if (healthAttr != null) {
            healthAttr.setBaseValue(20.0);
        }
        player.setHealth(20.0);
        plugin.getLogger().info("[Vampire] Wiped all buffs for " + player.getName());
    }

    private void announceEvolution(Player player, VampireLevel newLevel) {
        player.sendMessage(Component.text(""));
        player.sendMessage(Component.text("EVOLUTION!", newLevel.getColor())
                .decorate(TextDecoration.BOLD));
        player.sendMessage(Component.text("You are now a " + newLevel.getDisplayName() + "!",
                newLevel.getColor()));
        player.sendMessage(Component.text(""));
        player.getWorld().playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 0.8f);
        player.getWorld().spawnParticle(Particle.DUST,
                player.getLocation().add(0, 1, 0), 80, 1, 1, 1, 0,
                new Particle.DustOptions(Color.RED, 2.0f));
    }

    // ── Persistence ────────────────────────────────────────────────────

    private void loadAll() {
        File[] files = dataFolder.listFiles((dir, name) -> name.endsWith(".dat"));
        if (files == null) return;
        for (File file : files) {
            try {
                Properties props = new Properties();
                try (FileInputStream fis = new FileInputStream(file)) {
                    props.load(fis);
                }
                UUID uuid = UUID.fromString(props.getProperty("uuid"));
                VampireState state = new VampireState(uuid);
                state.setVampire(Boolean.parseBoolean(props.getProperty("isVampire", "false")));
                state.setLevel(VampireLevel.valueOf(props.getProperty("level", "FLEDGLING")));
                state.setBloodConsumed(Integer.parseInt(props.getProperty("bloodConsumed", "0")));
                state.setEvolversConsumed(Integer.parseInt(props.getProperty("evolversConsumed", "0")));
                state.setSuperBloodConsumed(Integer.parseInt(props.getProperty("superBloodConsumed", "0")));
                state.setHasVampireRing(Boolean.parseBoolean(props.getProperty("hasVampireRing", "false")));
                state.recalculateLevel();
                vampireStates.put(uuid, state);
            } catch (Exception e) {
                plugin.getLogger().warning("[Vampire] Failed to load: " + file.getName() + " — " + e.getMessage());
            }
        }
        plugin.getLogger().info("[Vampire] Loaded " + vampireStates.size() + " vampire states.");
    }

    /**
     * Saves a single vampire state to disk.
     */
    public void save(VampireState state) {
        Properties props = new Properties();
        props.setProperty("uuid", state.getPlayerUUID().toString());
        props.setProperty("isVampire", String.valueOf(state.isVampire()));
        props.setProperty("level", state.getLevel().name());
        props.setProperty("bloodConsumed", String.valueOf(state.getBloodConsumed()));
        props.setProperty("evolversConsumed", String.valueOf(state.getEvolversConsumed()));
        props.setProperty("superBloodConsumed", String.valueOf(state.getSuperBloodConsumed()));
        props.setProperty("hasVampireRing", String.valueOf(state.hasVampireRing()));

        File file = new File(dataFolder, state.getPlayerUUID().toString() + ".dat");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            props.store(fos, "JGlimsPlugin Vampire Data");
        } catch (IOException e) {
            plugin.getLogger().warning("[Vampire] Failed to save: " + e.getMessage());
        }
    }

    /**
     * Saves all vampire states. Called on plugin disable.
     */
    public void saveAll() {
        for (VampireState state : vampireStates.values()) {
            save(state);
        }
    }
}

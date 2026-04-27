package com.jglims.plugin.werewolf;

import com.jglims.plugin.JGlimsPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

/**
 * Manages the werewolf system. Parallel to {@code VampireManager} but much
 * simpler: infection is triggered by consuming Werewolf Blood (dropped by the
 * Blood Moon boss and werewolf-type custom mobs); werewolves have a single
 * transformation ability locked in their hotbar.
 */
public class WerewolfManager {

    private final JGlimsPlugin plugin;
    private final Map<UUID, WerewolfState> states = new HashMap<>();

    public final NamespacedKey keyWerewolfBlood;
    public final NamespacedKey keyWolfFormAbility;

    public static final int WOLF_FORM_CMD = 43001;
    public static final int WEREWOLF_BLOOD_CMD = 43002;

    public WerewolfManager(JGlimsPlugin plugin) {
        this.plugin = plugin;
        this.keyWerewolfBlood = new NamespacedKey(plugin, "werewolf_blood");
        this.keyWolfFormAbility = new NamespacedKey(plugin, "wolf_form_ability");
    }

    // ── State accessors ──────────────────────────────────────────────────

    public WerewolfState getOrCreateState(UUID uuid) {
        return states.computeIfAbsent(uuid, WerewolfState::new);
    }

    public boolean isWerewolf(Player player) {
        WerewolfState s = states.get(player.getUniqueId());
        return s != null && s.isWerewolf();
    }

    public boolean isInWolfForm(Player player) {
        WerewolfState s = states.get(player.getUniqueId());
        return s != null && s.isInWolfForm();
    }

    // ── Items ────────────────────────────────────────────────────────────

    /**
     * Creates a Werewolf Blood item. Consuming this (right-click with the
     * item in hand) infects the player and gives them the wolf-form ability.
     */
    public ItemStack createWerewolfBlood() {
        ItemStack item = new ItemStack(Material.REDSTONE, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Moon Stone", NamedTextColor.LIGHT_PURPLE)
                    .decorate(TextDecoration.BOLD)
                    .decoration(TextDecoration.ITALIC, false));
            meta.lore(List.of(
                    Component.text("A cold shard of crystallised moonlight,", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text("pulsing with the lunar curse. Right-click", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text("to consume. The transformation is", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text("permanent — choose carefully.", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.empty(),
                    Component.text("✦ Infects on consume", NamedTextColor.LIGHT_PURPLE)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text("✦ Dropped by the Blood Moon King", NamedTextColor.DARK_GRAY)
                            .decoration(TextDecoration.ITALIC, false)
            ));
            meta.setCustomModelData(WEREWOLF_BLOOD_CMD);
            meta.getPersistentDataContainer().set(keyWerewolfBlood, PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Creates the hotbar ability item that toggles wolf form.
     */
    public ItemStack createWolfFormItem() {
        ItemStack item = new ItemStack(Material.BONE, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("◆ Wolf Form ◆", NamedTextColor.DARK_RED)
                    .decorate(TextDecoration.BOLD)
                    .decoration(TextDecoration.ITALIC, false));
            meta.lore(List.of(
                    Component.text("The beast within you howls.", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.empty(),
                    Component.text("Right-click to shift forms.", NamedTextColor.YELLOW)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.empty(),
                    Component.text("In wolf form:", NamedTextColor.WHITE)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text(" ▸ Night Vision", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text(" ▸ Speed I", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text(" ▸ Strength I", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text(" ▸ +6 hearts", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.empty(),
                    Component.text("Only works at night in worlds", NamedTextColor.DARK_GRAY)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text("with a day/night cycle.", NamedTextColor.DARK_GRAY)
                            .decoration(TextDecoration.ITALIC, false)
            ));
            meta.setCustomModelData(WOLF_FORM_CMD);
            meta.getPersistentDataContainer().set(keyWolfFormAbility, PersistentDataType.BYTE, (byte) 1);
            meta.setUnbreakable(true);
            item.setItemMeta(meta);
        }
        return item;
    }

    public boolean isWerewolfBlood(ItemStack item) {
        if (item == null || item.getType() != Material.REDSTONE) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(keyWerewolfBlood, PersistentDataType.BYTE);
    }

    public boolean isWolfFormItem(ItemStack item) {
        if (item == null || item.getType() != Material.BONE) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(keyWolfFormAbility, PersistentDataType.BYTE);
    }

    // ── Infection ────────────────────────────────────────────────────────

    /**
     * Converts a player into a werewolf. Gives them the wolf form ability
     * and plays a dramatic infection animation.
     */
    public void infect(Player player) {
        WerewolfState state = getOrCreateState(player.getUniqueId());
        if (state.isWerewolf()) {
            player.sendMessage(Component.text("You are already a werewolf.", NamedTextColor.GRAY));
            return;
        }
        // Block if they're a vampire — the two don't mix
        if (plugin.getVampireManager() != null && plugin.getVampireManager().isVampire(player)) {
            player.sendMessage(Component.text("Your vampire blood rejects the werewolf curse.",
                    NamedTextColor.DARK_RED));
            return;
        }
        // Require fully empty inventory + offhand + armor.
        for (ItemStack it : player.getInventory().getContents()) {
            if (it != null && it.getType() != Material.AIR) {
                player.sendMessage(Component.text("You must empty your entire inventory before transforming.",
                        NamedTextColor.RED));
                return;
            }
        }
        for (ItemStack it : player.getInventory().getArmorContents()) {
            if (it != null && it.getType() != Material.AIR) {
                player.sendMessage(Component.text("You must remove all armor before transforming.",
                        NamedTextColor.RED));
                return;
            }
        }
        state.setWerewolf(true);
        // Infection effects
        player.getWorld().spawnParticle(Particle.DUST, player.getLocation().add(0, 1, 0),
                60, 0.5, 1, 0.5, new Particle.DustOptions(Color.fromRGB(80, 0, 0), 1.5f));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WOLF_GROWL, 1.5f, 0.6f);
        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("✦ THE BLOOD TAKES HOLD ✦", NamedTextColor.DARK_RED)
                .decorate(TextDecoration.BOLD));
        player.sendMessage(Component.text("You feel a primal rage surge through you.", NamedTextColor.RED));
        player.sendMessage(Component.text("When the moon rises, so will the wolf.", NamedTextColor.GRAY));
        player.sendMessage(Component.empty());
        // Give the hotbar ability
        player.getInventory().addItem(createWolfFormItem());
    }

    // ── Transformation ───────────────────────────────────────────────────

    public boolean canTransformNow(Player player) {
        World w = player.getWorld();
        // If the world's day/night cycle is disabled, allow at any time.
        Boolean cycleOn = w.getGameRuleValue(GameRule.DO_DAYLIGHT_CYCLE);
        if (cycleOn != null && !cycleOn) return true;
        // Otherwise, must be night time.
        return !w.isDayTime();
    }

    /**
     * Transforms the player into wolf form: makes them invisible, spawns a
     * shadowing tamed wolf at their feet, applies the stat buffs. Reversed
     * by calling {@link #revert(Player)}.
     */
    public void transform(Player player) {
        WerewolfState state = getOrCreateState(player.getUniqueId());
        if (state.isInWolfForm()) { revert(player); return; }
        if (!state.isWerewolf()) return;
        if (!canTransformNow(player)) {
            player.sendActionBar(Component.text("✦ The wolf cannot emerge in daylight.",
                    NamedTextColor.YELLOW));
            return;
        }

        // Save and strip armor so the invisible player reads as fully hidden.
        state.setSavedHelmet(player.getInventory().getHelmet());
        state.setSavedChestplate(player.getInventory().getChestplate());
        state.setSavedLeggings(player.getInventory().getLeggings());
        state.setSavedBoots(player.getInventory().getBoots());
        player.getInventory().setHelmet(null);
        player.getInventory().setChestplate(null);
        player.getInventory().setLeggings(null);
        player.getInventory().setBoots(null);

        // Invisibility — large duration, reapplied by the tick handler
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, false, true, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 0, false, true, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false, false));

        // +6 hearts via attribute modifier
        var maxHp = player.getAttribute(Attribute.MAX_HEALTH);
        if (maxHp != null) {
            // Set base +12 (6 hearts) temporarily
            maxHp.setBaseValue(maxHp.getBaseValue() + 12);
            player.setHealth(Math.min(player.getHealth() + 12, maxHp.getBaseValue()));
        }

        // Spawn the shadow wolf
        Location loc = player.getLocation();
        Wolf wolf = loc.getWorld().spawn(loc, Wolf.class, w -> {
            w.setOwner(player);
            w.setTamed(true);
            w.setAngry(false);
            w.setCollarColor(DyeColor.RED);
            w.setPersistent(true);
            w.setRemoveWhenFarAway(false);
            w.customName(Component.text(player.getName(), NamedTextColor.DARK_RED));
            w.setCustomNameVisible(false);
            w.setInvulnerable(true);
            w.setSilent(true);
        });
        state.setWolfEntityUUID(wolf.getUniqueId());
        state.setInWolfForm(true);

        // Feedback
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WOLF_GROWL, 1.3f, 0.8f);
        player.getWorld().spawnParticle(Particle.POOF, player.getLocation().add(0, 1, 0), 30, 0.5, 0.8, 0.5, 0.05);
        player.sendActionBar(Component.text("✦ Wolf Form ✦", NamedTextColor.DARK_RED));
    }

    public void revert(Player player) {
        WerewolfState state = getOrCreateState(player.getUniqueId());
        if (!state.isInWolfForm()) return;
        // Remove the shadow wolf
        UUID wolfUUID = state.getWolfEntityUUID();
        if (wolfUUID != null) {
            var entity = Bukkit.getEntity(wolfUUID);
            if (entity != null) entity.remove();
        }
        state.setWolfEntityUUID(null);
        // Restore armor
        player.getInventory().setHelmet(state.getSavedHelmet());
        player.getInventory().setChestplate(state.getSavedChestplate());
        player.getInventory().setLeggings(state.getSavedLeggings());
        player.getInventory().setBoots(state.getSavedBoots());
        state.setSavedHelmet(null);
        state.setSavedChestplate(null);
        state.setSavedLeggings(null);
        state.setSavedBoots(null);
        // Remove effects
        player.removePotionEffect(PotionEffectType.INVISIBILITY);
        player.removePotionEffect(PotionEffectType.SPEED);
        player.removePotionEffect(PotionEffectType.STRENGTH);
        player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        // Revert the HP bonus
        var maxHp = player.getAttribute(Attribute.MAX_HEALTH);
        if (maxHp != null && maxHp.getBaseValue() >= 32) {
            maxHp.setBaseValue(maxHp.getBaseValue() - 12);
            if (player.getHealth() > maxHp.getBaseValue()) player.setHealth(maxHp.getBaseValue());
        }
        state.setInWolfForm(false);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WOLF_WHINE, 1.0f, 1.2f);
        player.sendActionBar(Component.text("✦ Human Form ✦", NamedTextColor.GRAY));
    }

    /**
     * Iterates all transformed players and keeps their shadow wolf snapped
     * to their current location. Called every few ticks by the listener.
     */
    public void tickShadowWolves() {
        for (WerewolfState state : states.values()) {
            if (!state.isInWolfForm()) continue;
            Player p = Bukkit.getPlayer(state.getPlayerUUID());
            if (p == null || !p.isOnline()) continue;
            UUID wolfUUID = state.getWolfEntityUUID();
            if (wolfUUID == null) continue;
            var entity = Bukkit.getEntity(wolfUUID);
            if (entity == null) { state.setInWolfForm(false); continue; }
            // Teleport wolf to player
            if (entity.getLocation().distanceSquared(p.getLocation()) > 0.25) {
                entity.teleport(p.getLocation());
            }
            // If the world turned daytime on the player, force revert
            if (!canTransformNow(p)) {
                revert(p);
            }
        }
    }

    /** On plugin disable, revert everyone and clean up shadow wolves. */
    public void cleanupAll() {
        for (WerewolfState state : new ArrayList<>(states.values())) {
            if (!state.isInWolfForm()) continue;
            Player p = Bukkit.getPlayer(state.getPlayerUUID());
            if (p != null) revert(p);
        }
    }
}

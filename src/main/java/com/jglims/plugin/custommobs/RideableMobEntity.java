package com.jglims.plugin.custommobs;

import com.jglims.plugin.JGlimsPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Abstract base for rideable custom mobs. Adds mount/dismount handling,
 * multi-passenger support, taming system, sitting/following behavior,
 * lead attachment, chest storage, special ability on jump, and fall damage reduction.
 * <p>
 * Expected .bbmodel animations beyond standard: idle_mounted, walk_mounted,
 * fly_mounted, jump.
 */
public abstract class RideableMobEntity extends CustomMobEntity {

    /** UUID of the player who tamed this mob, or null if untamed. */
    protected UUID ownerUUID;

    /** Whether this mob is currently tamed. */
    protected boolean tamed = false;

    /** Whether this mob has a saddle equipped. */
    protected boolean saddled = false;

    /** Maximum number of simultaneous riders. */
    protected int maxRiders = 1;

    /** Whether this mob can fly (full 3D movement control). */
    protected boolean canFly = false;

    /** Movement speed when mounted. */
    protected double mountSpeed = 0.15;

    /** Jump strength when mounted. */
    protected double jumpStrength = 1.0;

    /** Number of chest storage slots (0 = no storage). */
    protected int chestSlots = 0;

    /** Chest inventory contents, indexed by slot. */
    protected final Map<Integer, ItemStack> chestContents = new HashMap<>();

    /** Whether this mob is currently being sat (stay in place). */
    protected boolean sitting = false;

    /** Taming progress — how much of the taming requirement is met. */
    protected double tamingProgress = 0;

    /** Total taming requirement before the mob is tamed. */
    protected double tamingRequirement = 1.0;

    /** Fall damage reduction multiplier for mounted players (0 = no fall damage). */
    protected double fallDamageReduction = 0.5;

    /** Cooldown for the special ability in ticks. */
    protected long specialAbilityCooldownTicks = 100L;

    /** Tick of last special ability use. */
    protected long lastSpecialAbilityTick = 0;

    /** Current tick counter for ability cooldown. */
    protected long rideTicks = 0;

    protected RideableMobEntity(JGlimsPlugin plugin, CustomMobType mobType) {
        super(plugin, mobType);
    }

    // ── Taming ─────────────────────────────────────────────────────────

    /**
     * Advances taming progress. Called when the player performs the correct
     * taming action (feeding the right item, riding for enough time, etc.).
     *
     * @param amount how much progress to add
     * @param player the player attempting to tame
     */
    public void addTamingProgress(double amount, Player player) {
        if (tamed) return;
        tamingProgress += amount;
        if (tamingProgress >= tamingRequirement) {
            tame(player);
        }
    }

    /**
     * Tames this mob to the given player.
     *
     * @param player the new owner
     */
    public void tame(Player player) {
        tamed = true;
        ownerUUID = player.getUniqueId();

        if (hitboxEntity != null) {
            hitboxEntity.getPersistentDataContainer().set(
                    new NamespacedKey(plugin, "custom_mob_owner"),
                    PersistentDataType.STRING,
                    ownerUUID.toString());
        }

        player.sendMessage(Component.text("You have tamed the " + mobType.getDisplayName() + "!",
                NamedTextColor.GREEN));
        if (hitboxEntity != null) {
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                    Sound.ENTITY_HORSE_AMBIENT, 1.0f, 1.2f);
            hitboxEntity.getWorld().spawnParticle(Particle.HEART,
                    hitboxEntity.getLocation().add(0, 1.5, 0), 10, 0.5, 0.5, 0.5, 0);
        }

        onTamed(player);
    }

    /** Called after the mob is successfully tamed. Override for custom behavior. */
    protected void onTamed(Player player) {}

    // ── Mounting ───────────────────────────────────────────────────────

    /**
     * Attempts to mount a player onto this mob.
     *
     * @param player the player trying to mount
     * @return true if mounting was successful
     */
    public boolean mount(Player player) {
        if (!tamed) {
            player.sendMessage(Component.text("This creature is not tamed!", NamedTextColor.RED));
            return false;
        }
        if (!saddled) {
            player.sendMessage(Component.text("This creature needs a saddle first!", NamedTextColor.RED));
            return false;
        }
        if (hitboxEntity == null) return false;

        List<org.bukkit.entity.Entity> passengers = hitboxEntity.getPassengers();
        if (passengers.size() >= maxRiders) {
            player.sendMessage(Component.text("This creature is full!", NamedTextColor.RED));
            return false;
        }

        hitboxEntity.addPassenger(player);
        playAnimation("idle_mounted", kr.toxicity.model.api.animation.AnimationIterator.Type.LOOP);
        onMount(player);
        return true;
    }

    /**
     * Dismounts a player from this mob.
     *
     * @param player the player to dismount
     */
    public void dismount(Player player) {
        if (hitboxEntity != null) {
            hitboxEntity.removePassenger(player);
        }
        if (hitboxEntity == null || hitboxEntity.getPassengers().isEmpty()) {
            playAnimation("idle", kr.toxicity.model.api.animation.AnimationIterator.Type.LOOP);
        }
        onDismount(player);
    }

    /** Called after a player mounts. Override for custom behavior. */
    protected void onMount(Player player) {}

    /** Called after a player dismounts. Override for custom behavior. */
    protected void onDismount(Player player) {}

    // ── Special Ability ────────────────────────────────────────────────

    /**
     * Triggers the mount's special ability (activated by the jump key while mounted).
     *
     * @param rider the player triggering the ability
     */
    public void triggerSpecialAbility(Player rider) {
        if (rideTicks - lastSpecialAbilityTick < specialAbilityCooldownTicks) {
            long remaining = (specialAbilityCooldownTicks - (rideTicks - lastSpecialAbilityTick)) / 20;
            rider.sendMessage(Component.text("Ability on cooldown! " + remaining + "s remaining.",
                    NamedTextColor.RED));
            return;
        }
        lastSpecialAbilityTick = rideTicks;
        onSpecialAbility(rider);
    }

    /**
     * Executes the mount-specific special ability. Override in subclasses.
     *
     * @param rider the player who activated the ability
     */
    protected void onSpecialAbility(Player rider) {}

    // ── Saddle ─────────────────────────────────────────────────────────

    /**
     * Applies a saddle to this mob.
     *
     * @param player the player applying the saddle
     */
    public void applySaddle(Player player) {
        if (saddled) {
            player.sendMessage(Component.text("Already saddled!", NamedTextColor.YELLOW));
            return;
        }
        saddled = true;
        if (hitboxEntity != null) {
            hitboxEntity.getPersistentDataContainer().set(
                    new NamespacedKey(plugin, "custom_mob_saddled"),
                    PersistentDataType.BYTE, (byte) 1);
        }
        player.sendMessage(Component.text("Saddle applied to " + mobType.getDisplayName() + "!",
                NamedTextColor.GREEN));
    }

    // ── Storage ────────────────────────────────────────────────────────

    /**
     * Opens the chest storage GUI for this mob.
     *
     * @param player the player viewing the storage
     */
    public void openStorage(Player player) {
        if (chestSlots <= 0) {
            player.sendMessage(Component.text("This creature has no storage!", NamedTextColor.RED));
            return;
        }
        // Create a simple inventory GUI
        int rows = (int) Math.ceil(chestSlots / 9.0);
        org.bukkit.inventory.Inventory inv = Bukkit.createInventory(null,
                rows * 9, Component.text(mobType.getDisplayName() + " Storage"));
        for (Map.Entry<Integer, ItemStack> entry : chestContents.entrySet()) {
            if (entry.getKey() < inv.getSize()) {
                inv.setItem(entry.getKey(), entry.getValue());
            }
        }
        player.openInventory(inv);
    }

    // ── Tick ───────────────────────────────────────────────────────────

    @Override
    protected void onTick() {
        rideTicks++;

        // If sitting, don't move
        if (sitting && hitboxEntity != null) {
            hitboxEntity.setAI(false);
        }
    }

    // ── Interaction ────────────────────────────────────────────────────

    @Override
    public void onInteract(Player player) {
        if (!tamed) {
            // Taming interaction is handled by subclass-specific logic
            return;
        }

        // Shift+right-click for storage
        if (player.isSneaking() && chestSlots > 0) {
            openStorage(player);
            return;
        }

        // Normal right-click to mount
        mount(player);
    }

    // ── Getters/Setters ────────────────────────────────────────────────

    public boolean isTamed() { return tamed; }
    public UUID getOwnerUUID() { return ownerUUID; }
    public boolean isSaddled() { return saddled; }
    public boolean isSitting() { return sitting; }
    public boolean canFly() { return canFly; }

    public void setSitting(boolean sitting) {
        this.sitting = sitting;
        if (hitboxEntity != null) {
            hitboxEntity.setAI(!sitting);
        }
    }

    /**
     * Checks whether the given player is the owner of this mob.
     *
     * @param player the player to check
     * @return true if the player owns this mob
     */
    public boolean isOwner(Player player) {
        return tamed && ownerUUID != null && ownerUUID.equals(player.getUniqueId());
    }
}

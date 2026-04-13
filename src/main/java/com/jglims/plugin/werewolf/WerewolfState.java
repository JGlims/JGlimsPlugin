package com.jglims.plugin.werewolf;

import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * Per-player werewolf state. Lightweight — only tracks whether the player is
 * infected and whether they're currently in wolf form, plus references we
 * need to restore state when they transform back.
 *
 * <p>Werewolves are simpler than vampires: no progression tiers, no multiple
 * abilities. The sole ability — "turn into a wolf" — is locked in the hotbar
 * from the moment the player is infected. In wolf form the player gets night
 * vision, Speed I, extra damage and extra hearts; they can only transform at
 * night in day/night cycle dimensions (Overworld, Nether, Jurassic), but can
 * transform any time in dimensions without a day cycle (Abyss, Aether, Lunar).
 */
public class WerewolfState {

    private final UUID playerUUID;
    private boolean isWerewolf;
    private boolean inWolfForm;
    /** UUID of the Wolf entity that "shadows" the player while transformed. */
    private UUID wolfEntityUUID;
    /** Armor saved when transforming, restored on revert. */
    private ItemStack savedHelmet;
    private ItemStack savedChestplate;
    private ItemStack savedLeggings;
    private ItemStack savedBoots;

    public WerewolfState(UUID playerUUID) {
        this.playerUUID = playerUUID;
        this.isWerewolf = false;
        this.inWolfForm = false;
    }

    public UUID getPlayerUUID() { return playerUUID; }

    public boolean isWerewolf() { return isWerewolf; }
    public void setWerewolf(boolean v) { this.isWerewolf = v; }

    public boolean isInWolfForm() { return inWolfForm; }
    public void setInWolfForm(boolean v) { this.inWolfForm = v; }

    public UUID getWolfEntityUUID() { return wolfEntityUUID; }
    public void setWolfEntityUUID(UUID uuid) { this.wolfEntityUUID = uuid; }

    public ItemStack getSavedHelmet() { return savedHelmet; }
    public void setSavedHelmet(ItemStack i) { this.savedHelmet = i; }
    public ItemStack getSavedChestplate() { return savedChestplate; }
    public void setSavedChestplate(ItemStack i) { this.savedChestplate = i; }
    public ItemStack getSavedLeggings() { return savedLeggings; }
    public void setSavedLeggings(ItemStack i) { this.savedLeggings = i; }
    public ItemStack getSavedBoots() { return savedBoots; }
    public void setSavedBoots(ItemStack i) { this.savedBoots = i; }
}

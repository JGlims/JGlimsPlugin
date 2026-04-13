package com.jglims.plugin.structures;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.legendary.LegendaryTier;
import com.jglims.plugin.legendary.LegendaryWeapon;
import com.jglims.plugin.legendary.LegendaryWeaponManager;
import com.jglims.plugin.powerups.PowerUpManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Fills structure chests with tier-appropriate loot including legendary weapons,
 * power-ups, and vanilla materials.
 */
public class StructureLootPopulator {

    private final JGlimsPlugin plugin;
    private final LegendaryWeaponManager weaponManager;
    private final PowerUpManager powerUpManager;
    private final Random random = new Random();

    public StructureLootPopulator(JGlimsPlugin plugin) {
        this.plugin = plugin;
        this.weaponManager = plugin.getLegendaryWeaponManager();
        this.powerUpManager = plugin.getPowerUpManager();
    }

    /**
     * Populate a chest at the given location based on the structure's loot tier.
     */
    public void populateChest(Location chestLoc, StructureType structureType) {
        Block block = chestLoc.getBlock();
        Chest chest;
        if (block.getState() instanceof Chest c) {
            chest = c;
        } else {
            // Previously this returned silently, which made it impossible to
            // diagnose empty-chest bugs in structures. Now we log a clear
            // warning and *replace* the block with a chest so the loot still
            // lands somewhere visible (the typical cause is a later build
            // phase in the same structure overwriting the chest).
            plugin.getLogger().warning("[LootPopulator] Expected CHEST at "
                    + chestLoc.getBlockX() + "," + chestLoc.getBlockY() + "," + chestLoc.getBlockZ()
                    + " for " + structureType.name() + " but block is "
                    + block.getType() + " — forcing CHEST.");
            block.setType(Material.CHEST, false);
            if (!(block.getState() instanceof Chest forced)) {
                plugin.getLogger().warning("[LootPopulator] Chest forced-place failed, skipping loot.");
                return;
            }
            chest = forced;
        }
        Inventory inv = chest.getInventory();
        inv.clear();

        LegendaryTier tier = structureType.getLootTier();
        List<ItemStack> loot = new ArrayList<>();

        // ── Vanilla loot (tier-scaled) ──
        addVanillaLoot(loot, tier);

        // ── Legendary weapon chance ──
        addLegendaryWeapon(loot, tier);

        // ── Power-up chances ──
        addPowerUps(loot, tier);

        // ── Dimension-specific loot (Infinity Stones, Thanos Glove, Vampire Evolver) ──
        addDimensionLoot(loot, chestLoc);

        // ── Place loot in random slots ──
        for (ItemStack item : loot) {
            int slot = random.nextInt(inv.getSize());
            // Find an empty slot near the random one
            for (int i = 0; i < inv.getSize(); i++) {
                int trySlot = (slot + i) % inv.getSize();
                if (inv.getItem(trySlot) == null) {
                    inv.setItem(trySlot, item);
                    break;
                }
            }
        }

        chest.update(true);
    }

    private void addVanillaLoot(List<ItemStack> loot, LegendaryTier tier) {
        switch (tier) {
            case COMMON -> {
                loot.add(new ItemStack(Material.IRON_INGOT, 3 + random.nextInt(6)));
                loot.add(new ItemStack(Material.GOLD_INGOT, 2 + random.nextInt(4)));
                loot.add(new ItemStack(Material.BREAD, 4 + random.nextInt(8)));
                if (random.nextDouble() < 0.3) loot.add(new ItemStack(Material.DIAMOND, 1));
                if (random.nextDouble() < 0.5) loot.add(new ItemStack(Material.GOLDEN_APPLE, 1));
                loot.add(new ItemStack(Material.ARROW, 8 + random.nextInt(16)));
                if (random.nextDouble() < 0.2) loot.add(new ItemStack(Material.SADDLE, 1));
            }
            case RARE -> {
                loot.add(new ItemStack(Material.IRON_INGOT, 5 + random.nextInt(8)));
                loot.add(new ItemStack(Material.GOLD_INGOT, 4 + random.nextInt(6)));
                loot.add(new ItemStack(Material.DIAMOND, 1 + random.nextInt(3)));
                loot.add(new ItemStack(Material.GOLDEN_APPLE, 1 + random.nextInt(2)));
                if (random.nextDouble() < 0.3) loot.add(new ItemStack(Material.EMERALD, 3 + random.nextInt(5)));
                if (random.nextDouble() < 0.2) loot.add(new ItemStack(Material.ENDER_PEARL, 2 + random.nextInt(3)));
                loot.add(new ItemStack(Material.EXPERIENCE_BOTTLE, 5 + random.nextInt(10)));
            }
            case EPIC -> {
                loot.add(new ItemStack(Material.DIAMOND, 3 + random.nextInt(5)));
                loot.add(new ItemStack(Material.GOLD_INGOT, 8 + random.nextInt(12)));
                loot.add(new ItemStack(Material.EMERALD, 5 + random.nextInt(8)));
                loot.add(new ItemStack(Material.GOLDEN_APPLE, 2 + random.nextInt(3)));
                if (random.nextDouble() < 0.3) loot.add(new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 1));
                if (random.nextDouble() < 0.2) loot.add(new ItemStack(Material.NETHERITE_SCRAP, 1 + random.nextInt(2)));
                loot.add(new ItemStack(Material.EXPERIENCE_BOTTLE, 10 + random.nextInt(15)));
                if (random.nextDouble() < 0.15) loot.add(new ItemStack(Material.TOTEM_OF_UNDYING, 1));
            }
            case MYTHIC -> {
                loot.add(new ItemStack(Material.DIAMOND, 5 + random.nextInt(8)));
                loot.add(new ItemStack(Material.NETHERITE_INGOT, 1 + random.nextInt(2)));
                loot.add(new ItemStack(Material.EMERALD, 10 + random.nextInt(15)));
                loot.add(new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 1 + random.nextInt(2)));
                loot.add(new ItemStack(Material.TOTEM_OF_UNDYING, 1));
                loot.add(new ItemStack(Material.EXPERIENCE_BOTTLE, 20 + random.nextInt(20)));
                if (random.nextDouble() < 0.3) loot.add(new ItemStack(Material.NETHER_STAR, 1));
                if (random.nextDouble() < 0.2) loot.add(new ItemStack(Material.NETHERITE_SCRAP, 2 + random.nextInt(3)));
            }
            case ABYSSAL -> {
                loot.add(new ItemStack(Material.NETHERITE_INGOT, 2 + random.nextInt(3)));
                loot.add(new ItemStack(Material.DIAMOND, 10 + random.nextInt(15)));
                loot.add(new ItemStack(Material.NETHER_STAR, 1 + random.nextInt(2)));
                loot.add(new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 2 + random.nextInt(3)));
                loot.add(new ItemStack(Material.TOTEM_OF_UNDYING, 2));
                loot.add(new ItemStack(Material.EXPERIENCE_BOTTLE, 32 + random.nextInt(32)));
            }
        }
    }

    private void addLegendaryWeapon(List<ItemStack> loot, LegendaryTier tier) {
        double chance = switch (tier) {
            case COMMON -> 0.10;
            case RARE -> 0.15;
            case EPIC -> 0.25;
            case MYTHIC -> 0.40;
            case ABYSSAL -> 0.60;
        };

        if (random.nextDouble() < chance) {
            // Pick a weapon from this tier or one tier below
            LegendaryWeapon[] pool = LegendaryWeapon.byTier(tier);
            if (pool.length == 0) {
                // Try one tier lower
                LegendaryTier lower = lowerTier(tier);
                if (lower != null) pool = LegendaryWeapon.byTier(lower);
            }
            if (pool.length > 0) {
                LegendaryWeapon weapon = pool[random.nextInt(pool.length)];
                ItemStack weaponItem = weaponManager.createWeapon(weapon);
                if (weaponItem != null) loot.add(weaponItem);
            }
        }
    }

    private void addPowerUps(List<ItemStack> loot, LegendaryTier tier) {
        // Heart Crystal: EPIC+ structures, 10% base
        if (tier.ordinal() >= LegendaryTier.EPIC.ordinal()) {
            double heartChance = switch (tier) {
                case EPIC -> 0.10;
                case MYTHIC -> 0.20;
                case ABYSSAL -> 0.35;
                default -> 0;
            };
            if (random.nextDouble() < heartChance) {
                loot.add(powerUpManager.createHeartCrystal());
            }
        }

        // Soul Fragment: any structure, scaled chance
        double soulChance = switch (tier) {
            case COMMON -> 0.05;
            case RARE -> 0.10;
            case EPIC -> 0.15;
            case MYTHIC -> 0.25;
            case ABYSSAL -> 0.40;
        };
        if (random.nextDouble() < soulChance) {
            int count = 1 + random.nextInt(tier.ordinal() + 1);
            for (int i = 0; i < count; i++) {
                loot.add(powerUpManager.createSoulFragment());
            }
        }

        // Phoenix Feather: MYTHIC+ structures
        if (tier.ordinal() >= LegendaryTier.MYTHIC.ordinal() && random.nextDouble() < 0.15) {
            loot.add(powerUpManager.createPhoenixFeather());
        }
    }

    private LegendaryTier lowerTier(LegendaryTier tier) {
        return switch (tier) {
            case RARE -> LegendaryTier.COMMON;
            case EPIC -> LegendaryTier.RARE;
            case MYTHIC -> LegendaryTier.EPIC;
            case ABYSSAL -> LegendaryTier.MYTHIC;
            default -> null;
        };
    }

    /**
     * Adds dimension-specific loot to chests based on which world the chest is in.
     * <ul>
     *   <li>Aether chests: 2% chance for Space, Reality, or Mind Infinity Stone fragment</li>
     *   <li>Lunar chests: 2% chance for Power, Soul, or Time Infinity Stone fragment</li>
     *   <li>Abyss chests: 10% chance for Thanos Glove, 3% chance for Wand of Wands</li>
     *   <li>End City chests: 5% chance for Vampire Evolver</li>
     * </ul>
     */
    private void addDimensionLoot(List<ItemStack> loot, Location chestLoc) {
        String worldName = chestLoc.getWorld().getName();
        var stoneManager = plugin.getInfinityStoneManager();
        var gauntletManager = plugin.getInfinityGauntletManager();

        switch (worldName) {
            case "world_aether" -> {
                // 2% chance for an Aether Infinity Stone fragment (Space, Reality, Mind)
                if (random.nextDouble() < 0.02 && stoneManager != null) {
                    com.jglims.plugin.legendary.InfinityStoneManager.StoneType[] aetherStones = {
                            com.jglims.plugin.legendary.InfinityStoneManager.StoneType.SPACE,
                            com.jglims.plugin.legendary.InfinityStoneManager.StoneType.REALITY,
                            com.jglims.plugin.legendary.InfinityStoneManager.StoneType.MIND
                    };
                    loot.add(stoneManager.createFragment(aetherStones[random.nextInt(3)]));
                }
            }
            case "world_lunar" -> {
                // 2% chance for a Lunar Infinity Stone fragment (Power, Soul, Time)
                if (random.nextDouble() < 0.02 && stoneManager != null) {
                    com.jglims.plugin.legendary.InfinityStoneManager.StoneType[] lunarStones = {
                            com.jglims.plugin.legendary.InfinityStoneManager.StoneType.POWER,
                            com.jglims.plugin.legendary.InfinityStoneManager.StoneType.SOUL,
                            com.jglims.plugin.legendary.InfinityStoneManager.StoneType.TIME
                    };
                    loot.add(stoneManager.createFragment(lunarStones[random.nextInt(3)]));
                }
            }
            case "world_abyss" -> {
                // 10% chance for Thanos Glove
                if (random.nextDouble() < 0.10 && gauntletManager != null) {
                    loot.add(gauntletManager.createThanosGlove());
                }
                // 3% chance for Wand of Wands
                var magicManager = plugin.getMagicItemManager();
                if (random.nextDouble() < 0.03 && magicManager != null) {
                    loot.add(magicManager.createWandOfWands());
                }
            }
            case "world_the_end" -> {
                // 5% chance for Vampire Evolver in End City chests
                var vampireManager = plugin.getVampireManager();
                if (random.nextDouble() < 0.05 && vampireManager != null) {
                    loot.add(vampireManager.createVampireEvolver());
                }
            }
        }
    }
}
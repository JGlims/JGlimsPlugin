package com.jglims.plugin.config;

import org.bukkit.configuration.file.FileConfiguration;

import com.jglims.plugin.JGlimsPlugin;

public class ConfigManager {

    private final JGlimsPlugin plugin;
    private FileConfiguration config;

    // Mob difficulty
    private boolean mobDifficultyEnabled;
    private double baselineHealth;
    private double baselineDamage;

    // Creeper reduction
    private boolean creeperReductionEnabled;
    private double creeperCancelChance;

    // Pale Garden fog
    private boolean paleGardenFogEnabled;
    private int paleGardenFogInterval;

    // Loot booster
    private boolean lootBoosterEnabled;
    private boolean chestEnchantedBook;
    private int guardianPrismarineMin;
    private int guardianPrismarineMax;
    private int elderGuardianPrismarineMin;
    private int elderGuardianPrismarineMax;
    private int ghastTearMin;
    private int ghastTearMax;
    private double echoShardChance;

    // Blessings
    private int cBlessMaxUses;
    private int cBlessHeartsPerUse;
    private int amiBlessMaxUses;
    private double amiBlessDmgPerUse;
    private int laBlessMaxUses;
    private double laBlessDefPerUse;

    // Anvil
    private boolean removeTooExpensive;
    private double xpCostReduction;

    // Toggles
    private boolean inventorySortEnabled;
    private boolean enchantTransferEnabled;
    private boolean sickleEnabled;
    private boolean superToolsEnabled;
    private boolean dropRateBoosterEnabled;

    public ConfigManager(JGlimsPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();

        // Mob difficulty
        mobDifficultyEnabled = config.getBoolean("mob-difficulty.enabled", true);
        baselineHealth = config.getDouble("mob-difficulty.baseline.health", 1.5);
        baselineDamage = config.getDouble("mob-difficulty.baseline.damage", 1.5);

        // Creeper
        creeperReductionEnabled = config.getBoolean("creeper.enabled", true);
        creeperCancelChance = config.getDouble("creeper.cancel-chance", 0.5);

        // Pale Garden fog
        paleGardenFogEnabled = config.getBoolean("pale-garden-fog.enabled", true);
        paleGardenFogInterval = config.getInt("pale-garden-fog.check-interval", 40);

        // Loot booster
        lootBoosterEnabled = config.getBoolean("loot-booster.enabled", true);
        chestEnchantedBook = config.getBoolean("loot-booster.chest-enchanted-book", true);
        var guardianShards = config.getIntegerList("loot-booster.guardian-prismarine-shards");
        guardianPrismarineMin = guardianShards.size() >= 1 ? guardianShards.get(0) : 1;
        guardianPrismarineMax = guardianShards.size() >= 2 ? guardianShards.get(1) : 3;
        var elderShards = config.getIntegerList("loot-booster.elder-guardian-prismarine-shards");
        elderGuardianPrismarineMin = elderShards.size() >= 1 ? elderShards.get(0) : 3;
        elderGuardianPrismarineMax = elderShards.size() >= 2 ? elderShards.get(1) : 5;
        var ghastDrops = config.getIntegerList("loot-booster.ghast-tears");
        ghastTearMin = ghastDrops.size() >= 1 ? ghastDrops.get(0) : 1;
        ghastTearMax = ghastDrops.size() >= 2 ? ghastDrops.get(1) : 2;
        echoShardChance = config.getDouble("loot-booster.echo-shard-chance", 0.40);

        // Blessings
        cBlessMaxUses = config.getInt("blessings.c-bless.max-uses", 10);
        cBlessHeartsPerUse = config.getInt("blessings.c-bless.hearts-per-use", 1);
        amiBlessMaxUses = config.getInt("blessings.ami-bless.max-uses", 10);
        amiBlessDmgPerUse = config.getDouble("blessings.ami-bless.damage-percent-per-use", 2.0);
        laBlessMaxUses = config.getInt("blessings.la-bless.max-uses", 10);
        // BUG 5 FIX: Changed key from defense-percent-per-use to defense-per-use, default 2.0
        laBlessDefPerUse = config.getDouble("blessings.la-bless.defense-per-use", 2.0);

        // Anvil
        removeTooExpensive = config.getBoolean("anvil.remove-too-expensive", true);
        xpCostReduction = config.getDouble("anvil.xp-cost-reduction", 0.50);

        // Toggles
        inventorySortEnabled = config.getBoolean("inventory-sort.enabled", true);
        enchantTransferEnabled = config.getBoolean("enchantment-transfer.enabled", true);
        sickleEnabled = config.getBoolean("sickle.enabled", true);
        superToolsEnabled = config.getBoolean("super-tools.enabled", true);
        dropRateBoosterEnabled = config.getBoolean("drop-rate-booster.enabled", true);

        plugin.getLogger().info("Configuration loaded.");
    }

    public FileConfiguration getConfig() { return config; }
    public boolean isMobDifficultyEnabled() { return mobDifficultyEnabled; }
    public double getBaselineHealth() { return baselineHealth; }
    public double getBaselineDamage() { return baselineDamage; }
    public boolean isCreeperReductionEnabled() { return creeperReductionEnabled; }
    public double getCreeperCancelChance() { return creeperCancelChance; }
    public boolean isPaleGardenFogEnabled() { return paleGardenFogEnabled; }
    public int getPaleGardenFogInterval() { return paleGardenFogInterval; }
    public boolean isLootBoosterEnabled() { return lootBoosterEnabled; }
    public boolean isChestEnchantedBook() { return chestEnchantedBook; }
    public int getGuardianPrismarineMin() { return guardianPrismarineMin; }
    public int getGuardianPrismarineMax() { return guardianPrismarineMax; }
    public int getElderGuardianPrismarineMin() { return elderGuardianPrismarineMin; }
    public int getElderGuardianPrismarineMax() { return elderGuardianPrismarineMax; }
    public int getGhastTearMin() { return ghastTearMin; }
    public int getGhastTearMax() { return ghastTearMax; }
    public double getEchoShardChance() { return echoShardChance; }
    public int getCBlessMaxUses() { return cBlessMaxUses; }
    public int getCBlessHeartsPerUse() { return cBlessHeartsPerUse; }
    public int getAmiBlessMaxUses() { return amiBlessMaxUses; }
    public double getAmiBlessDmgPerUse() { return amiBlessDmgPerUse; }
    public int getLaBlessMaxUses() { return laBlessMaxUses; }
    public double getLaBlessDefPerUse() { return laBlessDefPerUse; }
    public boolean isRemoveTooExpensive() { return removeTooExpensive; }
    public double getXpCostReduction() { return xpCostReduction; }
    public boolean isInventorySortEnabled() { return inventorySortEnabled; }
    public boolean isEnchantTransferEnabled() { return enchantTransferEnabled; }
    public boolean isSickleEnabled() { return sickleEnabled; }
    public boolean isSuperToolsEnabled() { return superToolsEnabled; }
    public boolean isDropRateBoosterEnabled() { return dropRateBoosterEnabled; }
}
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

    // Mob book drops (NEW v1.1.0)
    private boolean mobBookDropsEnabled;
    private double hostileMobBookChance;
    private double bossCustomBookChance;
    private double lootingBonusRegular;
    private double lootingBonusBoss;

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
    private boolean battleAxeEnabled;
    private boolean battleBowEnabled;
    private boolean battleMaceEnabled;
    private boolean superToolsEnabled;
    private boolean dropRateBoosterEnabled;

    // Drop rate booster extras (NEW v1.1.0)
    private double tridentDropChance;
    private int breezeWindChargeMin;
    private int breezeWindChargeMax;

    // Villager trades (NEW v1.1.0)
    private boolean villagerTradesEnabled;
    private double villagerPriceReduction;
    private boolean disableTradeLocking;

    // King mob (NEW v1.1.0)
    private boolean kingMobEnabled;
    private int spawnsPerKing;
    private double kingHealthMult;
    private double kingDamageMult;
    private int kingDiamondMin;
    private int kingDiamondMax;

    // Axe nerf (NEW v1.1.0)
    private boolean axeNerfEnabled;
    private double axeNerfAttackSpeed;

    // Weapon mastery (NEW v1.2.0)
    private boolean weaponMasteryEnabled;
    private int masteryMaxKills;
    private double masteryMaxBonusPercent;

    // Blood moon (NEW v1.2.0)
    private boolean bloodMoonEnabled;
    private int bloodMoonCheckInterval;
    private double bloodMoonChance;
    private double bloodMoonMobHealthMult;
    private double bloodMoonMobDamageMult;
    private int bloodMoonBossEveryNth;
    private double bloodMoonBossHealthMult;
    private double bloodMoonBossDamageMult;
    private int bloodMoonBossDiamondMin;
    private int bloodMoonBossDiamondMax;
    private boolean bloodMoonDoubleDrops;

    // Guilds (NEW v1.2.0)
    private boolean guildsEnabled;
    private int guildsMaxMembers;
    private boolean guildsFriendlyFire;

    public ConfigManager(JGlimsPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();

        // Mob difficulty
        mobDifficultyEnabled = config.getBoolean("mob-difficulty.enabled", true);
        baselineHealth = config.getDouble("mob-difficulty.baseline.health", 2.0);
        baselineDamage = config.getDouble("mob-difficulty.baseline.damage", 2.0);

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

        // Mob book drops
        mobBookDropsEnabled = config.getBoolean("mob-book-drops.enabled", true);
        hostileMobBookChance = config.getDouble("mob-book-drops.hostile-mob-chance", 0.05);
        bossCustomBookChance = config.getDouble("mob-book-drops.boss-custom-chance", 0.15);
        lootingBonusRegular = config.getDouble("mob-book-drops.looting-bonus-regular", 0.02);
        lootingBonusBoss = config.getDouble("mob-book-drops.looting-bonus-boss", 0.05);

        // Blessings
        cBlessMaxUses = config.getInt("blessings.c-bless.max-uses", 10);
        cBlessHeartsPerUse = config.getInt("blessings.c-bless.hearts-per-use", 1);
        amiBlessMaxUses = config.getInt("blessings.ami-bless.max-uses", 10);
        amiBlessDmgPerUse = config.getDouble("blessings.ami-bless.damage-percent-per-use", 2.0);
        laBlessMaxUses = config.getInt("blessings.la-bless.max-uses", 10);
        laBlessDefPerUse = config.getDouble("blessings.la-bless.defense-per-use", 2.0);

        // Anvil
        removeTooExpensive = config.getBoolean("anvil.remove-too-expensive", true);
        xpCostReduction = config.getDouble("anvil.xp-cost-reduction", 0.50);

        // Toggles
        inventorySortEnabled = config.getBoolean("inventory-sort.enabled", true);
        enchantTransferEnabled = config.getBoolean("enchantment-transfer.enabled", true);
        sickleEnabled = config.getBoolean("sickle.enabled", true);
        battleAxeEnabled = config.getBoolean("battle-axe.enabled", true);
        battleBowEnabled = config.getBoolean("battle-bow.enabled", true);
        battleMaceEnabled = config.getBoolean("battle-mace.enabled", true);
        superToolsEnabled = config.getBoolean("super-tools.enabled", true);
        dropRateBoosterEnabled = config.getBoolean("drop-rate-booster.enabled", true);

        // Drop rate extras
        tridentDropChance = config.getDouble("drop-rate-booster.trident-drop-chance", 0.35);
        breezeWindChargeMin = config.getInt("drop-rate-booster.breeze-wind-charge-min", 2);
        breezeWindChargeMax = config.getInt("drop-rate-booster.breeze-wind-charge-max", 5);

        // Villager trades
        villagerTradesEnabled = config.getBoolean("villager-trades.enabled", true);
        villagerPriceReduction = config.getDouble("villager-trades.price-reduction", 0.50);
        disableTradeLocking = config.getBoolean("villager-trades.disable-trade-locking", true);

        // King mob
        kingMobEnabled = config.getBoolean("king-mob.enabled", true);
        spawnsPerKing = config.getInt("king-mob.spawns-per-king", 500);
        kingHealthMult = config.getDouble("king-mob.health-multiplier", 10.0);
        kingDamageMult = config.getDouble("king-mob.damage-multiplier", 3.0);
        kingDiamondMin = config.getInt("king-mob.diamond-drop-min", 3);
        kingDiamondMax = config.getInt("king-mob.diamond-drop-max", 9);

        // Axe nerf
        axeNerfEnabled = config.getBoolean("axe-nerf.enabled", true);
        axeNerfAttackSpeed = config.getDouble("axe-nerf.attack-speed", 0.5);

        // Weapon mastery (NEW v1.2.0)
        weaponMasteryEnabled = config.getBoolean("weapon-mastery.enabled", true);
        masteryMaxKills = config.getInt("weapon-mastery.max-kills", 1000);
        masteryMaxBonusPercent = config.getDouble("weapon-mastery.max-bonus-percent", 10.0);

        // Blood moon (NEW v1.2.0)
        bloodMoonEnabled = config.getBoolean("blood-moon.enabled", true);
        bloodMoonCheckInterval = config.getInt("blood-moon.check-interval-ticks", 100);
        bloodMoonChance = config.getDouble("blood-moon.chance", 0.15);
        bloodMoonMobHealthMult = config.getDouble("blood-moon.mob-health-multiplier", 1.5);
        bloodMoonMobDamageMult = config.getDouble("blood-moon.mob-damage-multiplier", 1.3);
        bloodMoonBossEveryNth = config.getInt("blood-moon.boss-every-nth", 10);
        bloodMoonBossHealthMult = config.getDouble("blood-moon.boss-health-multiplier", 20.0);
        bloodMoonBossDamageMult = config.getDouble("blood-moon.boss-damage-multiplier", 5.0);
        bloodMoonBossDiamondMin = config.getInt("blood-moon.boss-diamond-min", 5);
        bloodMoonBossDiamondMax = config.getInt("blood-moon.boss-diamond-max", 15);
        bloodMoonDoubleDrops = config.getBoolean("blood-moon.double-drops", true);

        // Guilds (NEW v1.2.0)
        guildsEnabled = config.getBoolean("guilds.enabled", true);
        guildsMaxMembers = config.getInt("guilds.max-members", 10);
        guildsFriendlyFire = config.getBoolean("guilds.friendly-fire", false);

        plugin.getLogger().info("Configuration loaded.");
    }

    // === Existing getters (unchanged) ===
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
    public boolean isMobBookDropsEnabled() { return mobBookDropsEnabled; }
    public double getHostileMobBookChance() { return hostileMobBookChance; }
    public double getBossCustomBookChance() { return bossCustomBookChance; }
    public double getLootingBonusRegular() { return lootingBonusRegular; }
    public double getLootingBonusBoss() { return lootingBonusBoss; }
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
    public boolean isBattleAxeEnabled() { return battleAxeEnabled; }
    public boolean isBattleBowEnabled() { return battleBowEnabled; }
    public boolean isBattleMaceEnabled() { return battleMaceEnabled; }
    public boolean isSuperToolsEnabled() { return superToolsEnabled; }
    public boolean isDropRateBoosterEnabled() { return dropRateBoosterEnabled; }
    public double getTridentDropChance() { return tridentDropChance; }
    public int getBreezeWindChargeMin() { return breezeWindChargeMin; }
    public int getBreezeWindChargeMax() { return breezeWindChargeMax; }
    public boolean isVillagerTradesEnabled() { return villagerTradesEnabled; }
    public double getVillagerPriceReduction() { return villagerPriceReduction; }
    public boolean isDisableTradeLocking() { return disableTradeLocking; }
    public boolean isKingMobEnabled() { return kingMobEnabled; }
    public int getSpawnsPerKing() { return spawnsPerKing; }
    public double getKingHealthMult() { return kingHealthMult; }
    public double getKingDamageMult() { return kingDamageMult; }
    public int getKingDiamondMin() { return kingDiamondMin; }
    public int getKingDiamondMax() { return kingDiamondMax; }
    public boolean isAxeNerfEnabled() { return axeNerfEnabled; }
    public double getAxeNerfAttackSpeed() { return axeNerfAttackSpeed; }

    // === NEW v1.2.0 getters ===
    public boolean isWeaponMasteryEnabled() { return weaponMasteryEnabled; }
    public int getMasteryMaxKills() { return masteryMaxKills; }
    public double getMasteryMaxBonusPercent() { return masteryMaxBonusPercent; }
    public boolean isBloodMoonEnabled() { return bloodMoonEnabled; }
    public int getBloodMoonCheckInterval() { return bloodMoonCheckInterval; }
    public double getBloodMoonChance() { return bloodMoonChance; }
    public double getBloodMoonMobHealthMult() { return bloodMoonMobHealthMult; }
    public double getBloodMoonMobDamageMult() { return bloodMoonMobDamageMult; }
    public int getBloodMoonBossEveryNth() { return bloodMoonBossEveryNth; }
    public double getBloodMoonBossHealthMult() { return bloodMoonBossHealthMult; }
    public double getBloodMoonBossDamageMult() { return bloodMoonBossDamageMult; }
    public int getBloodMoonBossDiamondMin() { return bloodMoonBossDiamondMin; }
    public int getBloodMoonBossDiamondMax() { return bloodMoonBossDiamondMax; }
    public boolean isBloodMoonDoubleDrops() { return bloodMoonDoubleDrops; }
    public boolean isGuildsEnabled() { return guildsEnabled; }
    public int getGuildsMaxMembers() { return guildsMaxMembers; }
    public boolean isGuildsFriendlyFire() { return guildsFriendlyFire; }
}

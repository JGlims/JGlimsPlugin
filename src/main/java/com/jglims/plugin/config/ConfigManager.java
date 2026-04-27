package com.jglims.plugin.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {

    private final JavaPlugin plugin;
    private FileConfiguration config;

    // --- Mob Difficulty ---
    private boolean mobDifficultyEnabled;
    private double baselineHealthMultiplier;
    private double baselineDamageMultiplier;

    // Distance multipliers (overworld)
    private double dist350Health, dist350Damage;
    private double dist700Health, dist700Damage;
    private double dist1000Health, dist1000Damage;
    private double dist2000Health, dist2000Damage;
    private double dist3000Health, dist3000Damage;
    private double dist5000Health, dist5000Damage;

    // Biome multipliers
    private double biomePaleGarden;
    private double biomeDeepDark;
    private double biomeSwamp;
    private double biomeNetherWastesHealth, biomeNetherWastesDamage;
    private double biomeSoulSandValleyHealth, biomeSoulSandValleyDamage;
    private double biomeCrimsonForestHealth, biomeCrimsonForestDamage;
    private double biomeWarpedForestHealth, biomeWarpedForestDamage;
    private double biomeBasaltDeltasHealth, biomeBasaltDeltasDamage;
    private double biomeEndHealth, biomeEndDamage;

    // --- Boss Enhancer ---
    private double enderDragonHealthMult, enderDragonDamageMult;
    private double witherHealthMult, witherDamageMult;
    private double wardenHealthMult, wardenDamageMult;
    private double elderGuardianHealthMult, elderGuardianDamageMult;

    // --- Creeper Reduction ---
    private boolean creeperReductionEnabled;
    private double creeperCancelChance;

    // --- Pale Garden Fog ---
    private boolean paleGardenFogEnabled;
    private int paleGardenFogCheckInterval;

    // --- Loot Booster ---
    private boolean lootBoosterEnabled;
    private boolean chestEnchantedBook;
    private int guardianShardsMin, guardianShardsMax;
    private int elderGuardianShardsMin, elderGuardianShardsMax;
    private int ghastTearsMin, ghastTearsMax;
    private double echoShardChance;

    // --- Mob Book Drops ---
    private boolean mobBookDropsEnabled;
    private double hostileBookChance;
    private double bossCustomBookChance;
    private double lootingBonusRegular;
    private double lootingBonusBoss;

    // --- Blessings ---
    private int cBlessMaxUses;
    private int cBlessHealPerUse;
    private int amiBlessMaxUses;
    private double amiBlessDamagePercentPerUse;
    private int laBlessMaxUses;
    private double laBlessDefensePercentPerUse;

    // --- Anvil ---
    private boolean removeToolExpensive;
    private double xpCostReduction;

    // --- Toggles ---
    private boolean inventorySortEnabled;
    private boolean enchantTransferEnabled;
    private boolean sickleEnabled;
    private boolean battleAxeEnabled;
    private boolean battleBowEnabled;
    private boolean battleMaceEnabled;
    private boolean battleShovelEnabled;
    private boolean superToolsEnabled;
    private boolean dropRateBoosterEnabled;
    private boolean spearEnabled;

    // --- Drop Rate Booster ---
    private double tridentDropChance;
    private int breezeWindChargeMin, breezeWindChargeMax;

    // --- Villager Trades ---
    private boolean villagerTradesEnabled;
    private double priceReduction;
    private boolean disableTradeLocking;

    // --- King Mob ---
    private boolean kingMobEnabled;
    private int spawnsPerKing;
    private double kingHealthMultiplier;
    private double kingDamageMultiplier;
    private int kingDiamondDropMin, kingDiamondDropMax;

    // --- Axe Nerf ---
    private boolean axeNerfEnabled;
    private double axeAttackSpeed;

    // --- Weapon Mastery ---
    private boolean weaponMasteryEnabled;
    private int masteryMaxKills;
    private double masteryMaxBonusPercent;

    // --- Blood Moon ---
    private boolean bloodMoonEnabled;
    private int bloodMoonCheckInterval;
    private double bloodMoonChance;
    private double bloodMoonMobHealthMult;
    private double bloodMoonMobDamageMult;
    private int bloodMoonBossEveryNth;
    private double bloodMoonBossHealthMult;
    private double bloodMoonBossDamageMult;
    private int bloodMoonBossDiamondMin, bloodMoonBossDiamondMax;
    private boolean bloodMoonDoubleDrops;

    // --- Guilds ---
    private boolean guildsEnabled;
    private int guildsMaxMembers;
    private boolean guildsFriendlyFire;

    // --- Best Buddies (Dog Armor) ---
    private double dogArmorDamageReduction;

    // --- Super Tools ---
    private double superIronBonusDamage;
    private double superDiamondBonusDamage;
    private double superNetheriteBonusDamage;
    private double superNethPerEnchantBonus;

    // --- Ore Detect (Super Pickaxe) ---
    private int oreDetectRadiusDiamond;
    private int oreDetectRadiusNetherite;
    private int oreDetectAncientDebrisRadiusDiamond;
    private int oreDetectAncientDebrisRadiusNetherite;
    private int oreDetectDurationTicks;

    // --- Weapon Abilities ---
    private double enderDragonAbilityDamageReduction;
    private double netheriteEnchantBonusPercent;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();

        // --- Mob Difficulty ---
        mobDifficultyEnabled = config.getBoolean("mob-difficulty.enabled", true);
        baselineHealthMultiplier = config.getDouble("mob-difficulty.baseline-health-multiplier", 1.0);
        baselineDamageMultiplier = config.getDouble("mob-difficulty.baseline-damage-multiplier", 1.0);

        // Distance multipliers
        dist350Health = config.getDouble("mob-difficulty.distance.350.health", 1.5);
        dist350Damage = config.getDouble("mob-difficulty.distance.350.damage", 1.3);
        dist700Health = config.getDouble("mob-difficulty.distance.700.health", 2.0);
        dist700Damage = config.getDouble("mob-difficulty.distance.700.damage", 1.6);
        dist1000Health = config.getDouble("mob-difficulty.distance.1000.health", 2.5);
        dist1000Damage = config.getDouble("mob-difficulty.distance.1000.damage", 1.9);
        dist2000Health = config.getDouble("mob-difficulty.distance.2000.health", 3.0);
        dist2000Damage = config.getDouble("mob-difficulty.distance.2000.damage", 2.2);
        dist3000Health = config.getDouble("mob-difficulty.distance.3000.health", 3.5);
        dist3000Damage = config.getDouble("mob-difficulty.distance.3000.damage", 2.5);
        dist5000Health = config.getDouble("mob-difficulty.distance.5000.health", 4.0);
        dist5000Damage = config.getDouble("mob-difficulty.distance.5000.damage", 3.0);

        // Biome multipliers
        biomePaleGarden = config.getDouble("mob-difficulty.biome.pale-garden", 2.0);
        biomeDeepDark = config.getDouble("mob-difficulty.biome.deep-dark", 2.5);
        biomeSwamp = config.getDouble("mob-difficulty.biome.swamp", 1.4);
        biomeNetherWastesHealth = config.getDouble("mob-difficulty.biome.nether-wastes.health", 1.7);
        biomeNetherWastesDamage = config.getDouble("mob-difficulty.biome.nether-wastes.damage", 1.7);
        biomeSoulSandValleyHealth = config.getDouble("mob-difficulty.biome.soul-sand-valley.health", 1.9);
        biomeSoulSandValleyDamage = config.getDouble("mob-difficulty.biome.soul-sand-valley.damage", 1.9);
        biomeCrimsonForestHealth = config.getDouble("mob-difficulty.biome.crimson-forest.health", 2.0);
        biomeCrimsonForestDamage = config.getDouble("mob-difficulty.biome.crimson-forest.damage", 2.0);
        biomeWarpedForestHealth = config.getDouble("mob-difficulty.biome.warped-forest.health", 2.0);
        biomeWarpedForestDamage = config.getDouble("mob-difficulty.biome.warped-forest.damage", 2.0);
        biomeBasaltDeltasHealth = config.getDouble("mob-difficulty.biome.basalt-deltas.health", 2.3);
        biomeBasaltDeltasDamage = config.getDouble("mob-difficulty.biome.basalt-deltas.damage", 2.3);
        biomeEndHealth = config.getDouble("mob-difficulty.biome.end.health", 2.5);
        biomeEndDamage = config.getDouble("mob-difficulty.biome.end.damage", 2.0);

        // --- Boss Enhancer ---
        enderDragonHealthMult = config.getDouble("boss-enhancer.ender-dragon.health", 3.5);
        enderDragonDamageMult = config.getDouble("boss-enhancer.ender-dragon.damage", 3.0);
        witherHealthMult = config.getDouble("boss-enhancer.wither.health", 1.0);
        witherDamageMult = config.getDouble("boss-enhancer.wither.damage", 1.0);
        wardenHealthMult = config.getDouble("boss-enhancer.warden.health", 1.0);
        wardenDamageMult = config.getDouble("boss-enhancer.warden.damage", 1.0);
        elderGuardianHealthMult = config.getDouble("boss-enhancer.elder-guardian.health", 2.5);
        elderGuardianDamageMult = config.getDouble("boss-enhancer.elder-guardian.damage", 1.8);

        // --- Creeper Reduction ---
        creeperReductionEnabled = config.getBoolean("creeper-reduction.enabled", true);
        creeperCancelChance = config.getDouble("creeper-reduction.cancel-chance", 0.5);

        // --- Pale Garden Fog ---
        paleGardenFogEnabled = config.getBoolean("pale-garden-fog.enabled", true);
        paleGardenFogCheckInterval = config.getInt("pale-garden-fog.check-interval", 40);

        // --- Loot Booster ---
        lootBoosterEnabled = config.getBoolean("loot-booster.enabled", true);
        chestEnchantedBook = config.getBoolean("loot-booster.chest-enchanted-book", true);
        guardianShardsMin = config.getInt("loot-booster.guardian-shards-min", 1);
        guardianShardsMax = config.getInt("loot-booster.guardian-shards-max", 3);
        elderGuardianShardsMin = config.getInt("loot-booster.elder-guardian-shards-min", 3);
        elderGuardianShardsMax = config.getInt("loot-booster.elder-guardian-shards-max", 5);
        ghastTearsMin = config.getInt("loot-booster.ghast-tears-min", 1);
        ghastTearsMax = config.getInt("loot-booster.ghast-tears-max", 2);
        echoShardChance = config.getDouble("loot-booster.echo-shard-chance", 0.40);

        // --- Mob Book Drops ---
        mobBookDropsEnabled = config.getBoolean("mob-book-drops.enabled", true);
        hostileBookChance = config.getDouble("mob-book-drops.hostile-chance", 0.05);
        bossCustomBookChance = config.getDouble("mob-book-drops.boss-custom-chance", 0.15);
        lootingBonusRegular = config.getDouble("mob-book-drops.looting-bonus-regular", 0.02);
        lootingBonusBoss = config.getDouble("mob-book-drops.looting-bonus-boss", 0.05);

        // --- Blessings ---
        cBlessMaxUses = config.getInt("blessings.c-bless.max-uses", 10);
        cBlessHealPerUse = config.getInt("blessings.c-bless.heal-per-use", 1);
        amiBlessMaxUses = config.getInt("blessings.ami-bless.max-uses", 10);
        amiBlessDamagePercentPerUse = config.getDouble("blessings.ami-bless.damage-percent-per-use", 2.0);
        laBlessMaxUses = config.getInt("blessings.la-bless.max-uses", 10);
        laBlessDefensePercentPerUse = config.getDouble("blessings.la-bless.defense-percent-per-use", 2.0);

        // --- Anvil ---
        removeToolExpensive = config.getBoolean("anvil.remove-too-expensive", true);
        xpCostReduction = config.getDouble("anvil.xp-cost-reduction", 0.5);

        // --- Toggles ---
        inventorySortEnabled = config.getBoolean("toggles.inventory-sort", true);
        enchantTransferEnabled = config.getBoolean("toggles.enchant-transfer", true);
        sickleEnabled = config.getBoolean("toggles.sickle", true);
        battleAxeEnabled = config.getBoolean("toggles.battle-axe", true);
        battleBowEnabled = config.getBoolean("toggles.battle-bow", true);
        battleMaceEnabled = config.getBoolean("toggles.battle-mace", true);
        battleShovelEnabled = config.getBoolean("toggles.battle-shovel", true);
        superToolsEnabled = config.getBoolean("toggles.super-tools", true);
        dropRateBoosterEnabled = config.getBoolean("toggles.drop-rate-booster", true);
        spearEnabled = config.getBoolean("toggles.spear", true);

        // --- Drop Rate Booster ---
        tridentDropChance = config.getDouble("drop-rate-booster.trident-drop-chance", 0.35);
        breezeWindChargeMin = config.getInt("drop-rate-booster.breeze-wind-charge-min", 2);
        breezeWindChargeMax = config.getInt("drop-rate-booster.breeze-wind-charge-max", 5);

        // --- Villager Trades ---
        villagerTradesEnabled = config.getBoolean("villager-trades.enabled", true);
        priceReduction = config.getDouble("villager-trades.price-reduction", 0.50);
        disableTradeLocking = config.getBoolean("villager-trades.disable-trade-locking", true);

        // --- King Mob ---
        kingMobEnabled = config.getBoolean("king-mob.enabled", true);
        spawnsPerKing = config.getInt("king-mob.spawns-per-king", 100);
        kingHealthMultiplier = config.getDouble("king-mob.health-multiplier", 10.0);
        kingDamageMultiplier = config.getDouble("king-mob.damage-multiplier", 3.0);
        kingDiamondDropMin = config.getInt("king-mob.diamond-drop-min", 3);
        kingDiamondDropMax = config.getInt("king-mob.diamond-drop-max", 9);

        // --- Axe Nerf ---
        axeNerfEnabled = config.getBoolean("axe-nerf.enabled", true);
        axeAttackSpeed = config.getDouble("axe-nerf.attack-speed", 0.5);

        // --- Weapon Mastery ---
        weaponMasteryEnabled = config.getBoolean("weapon-mastery.enabled", true);
        masteryMaxKills = config.getInt("weapon-mastery.max-kills", 1000);
        masteryMaxBonusPercent = config.getDouble("weapon-mastery.max-bonus-percent", 10.0);

        // --- Blood Moon ---
        bloodMoonEnabled = config.getBoolean("blood-moon.enabled", true);
        bloodMoonCheckInterval = config.getInt("blood-moon.check-interval", 100);
        bloodMoonChance = config.getDouble("blood-moon.chance", 0.02);
        bloodMoonMobHealthMult = config.getDouble("blood-moon.mob-health-multiplier", 1.5);
        bloodMoonMobDamageMult = config.getDouble("blood-moon.mob-damage-multiplier", 1.3);
        bloodMoonBossEveryNth = config.getInt("blood-moon.boss-every-nth", 10);
        bloodMoonBossHealthMult = config.getDouble("blood-moon.boss-health-multiplier", 20.0);
        bloodMoonBossDamageMult = config.getDouble("blood-moon.boss-damage-multiplier", 5.0);
        bloodMoonBossDiamondMin = config.getInt("blood-moon.boss-diamond-min", 5);
        bloodMoonBossDiamondMax = config.getInt("blood-moon.boss-diamond-max", 15);
        bloodMoonDoubleDrops = config.getBoolean("blood-moon.double-drops", true);

        // --- Guilds ---
        guildsEnabled = config.getBoolean("guilds.enabled", true);
        guildsMaxMembers = config.getInt("guilds.max-members", 10);
        guildsFriendlyFire = config.getBoolean("guilds.friendly-fire", false);

        // --- Best Buddies (Dog Armor) ---
        dogArmorDamageReduction = config.getDouble("best-buddies.dog-armor-damage-reduction", 0.95);

        // --- Super Tools ---
        superIronBonusDamage = config.getDouble("super-tools.iron-bonus-damage", 1.0);
        superDiamondBonusDamage = config.getDouble("super-tools.diamond-bonus-damage", 2.0);
        superNetheriteBonusDamage = config.getDouble("super-tools.netherite-bonus-damage", 2.0);
        superNethPerEnchantBonus = config.getDouble("super-tools.netherite-per-enchant-bonus-percent", 2.0);

        // --- Ore Detect ---
        oreDetectRadiusDiamond = config.getInt("ore-detect.radius-diamond", 8);
        oreDetectRadiusNetherite = config.getInt("ore-detect.radius-netherite", 12);
        oreDetectAncientDebrisRadiusDiamond = config.getInt("ore-detect.ancient-debris-radius-diamond", 24);
        oreDetectAncientDebrisRadiusNetherite = config.getInt("ore-detect.ancient-debris-radius-netherite", 40);
        oreDetectDurationTicks = config.getInt("ore-detect.duration-ticks", 200);

        // --- Weapon Abilities ---
        enderDragonAbilityDamageReduction = config.getDouble("weapon-abilities.ender-dragon-damage-reduction", 0.30);
        netheriteEnchantBonusPercent = config.getDouble("weapon-abilities.netherite-enchant-bonus-percent", 2.0);
    }

    // ========================
    // PRIMARY GETTERS
    // ========================

    // Mob Difficulty
    public boolean isMobDifficultyEnabled() { return mobDifficultyEnabled; }
    public double getBaselineHealthMultiplier() { return baselineHealthMultiplier; }
    public double getBaselineDamageMultiplier() { return baselineDamageMultiplier; }
    public double getDist350Health() { return dist350Health; }
    public double getDist350Damage() { return dist350Damage; }
    public double getDist700Health() { return dist700Health; }
    public double getDist700Damage() { return dist700Damage; }
    public double getDist1000Health() { return dist1000Health; }
    public double getDist1000Damage() { return dist1000Damage; }
    public double getDist2000Health() { return dist2000Health; }
    public double getDist2000Damage() { return dist2000Damage; }
    public double getDist3000Health() { return dist3000Health; }
    public double getDist3000Damage() { return dist3000Damage; }
    public double getDist5000Health() { return dist5000Health; }
    public double getDist5000Damage() { return dist5000Damage; }
    public double getBiomePaleGarden() { return biomePaleGarden; }
    public double getBiomeDeepDark() { return biomeDeepDark; }
    public double getBiomeSwamp() { return biomeSwamp; }
    public double getBiomeNetherWastesHealth() { return biomeNetherWastesHealth; }
    public double getBiomeNetherWastesDamage() { return biomeNetherWastesDamage; }
    public double getBiomeSoulSandValleyHealth() { return biomeSoulSandValleyHealth; }
    public double getBiomeSoulSandValleyDamage() { return biomeSoulSandValleyDamage; }
    public double getBiomeCrimsonForestHealth() { return biomeCrimsonForestHealth; }
    public double getBiomeCrimsonForestDamage() { return biomeCrimsonForestDamage; }
    public double getBiomeWarpedForestHealth() { return biomeWarpedForestHealth; }
    public double getBiomeWarpedForestDamage() { return biomeWarpedForestDamage; }
    public double getBiomeBasaltDeltasHealth() { return biomeBasaltDeltasHealth; }
    public double getBiomeBasaltDeltasDamage() { return biomeBasaltDeltasDamage; }
    public double getBiomeEndHealth() { return biomeEndHealth; }
    public double getBiomeEndDamage() { return biomeEndDamage; }

    // Boss Enhancer
    public double getEnderDragonHealthMult() { return enderDragonHealthMult; }
    public double getEnderDragonDamageMult() { return enderDragonDamageMult; }
    public double getWitherHealthMult() { return witherHealthMult; }
    public double getWitherDamageMult() { return witherDamageMult; }
    public double getWardenHealthMult() { return wardenHealthMult; }
    public double getWardenDamageMult() { return wardenDamageMult; }
    public double getElderGuardianHealthMult() { return elderGuardianHealthMult; }
    public double getElderGuardianDamageMult() { return elderGuardianDamageMult; }

    // Creeper Reduction
    public boolean isCreeperReductionEnabled() { return creeperReductionEnabled; }
    public double getCreeperCancelChance() { return creeperCancelChance; }

    // Pale Garden Fog
    public boolean isPaleGardenFogEnabled() { return paleGardenFogEnabled; }
    public int getPaleGardenFogCheckInterval() { return paleGardenFogCheckInterval; }

    // Loot Booster
    public boolean isLootBoosterEnabled() { return lootBoosterEnabled; }
    public boolean isChestEnchantedBook() { return chestEnchantedBook; }
    public int getGuardianShardsMin() { return guardianShardsMin; }
    public int getGuardianShardsMax() { return guardianShardsMax; }
    public int getElderGuardianShardsMin() { return elderGuardianShardsMin; }
    public int getElderGuardianShardsMax() { return elderGuardianShardsMax; }
    public int getGhastTearsMin() { return ghastTearsMin; }
    public int getGhastTearsMax() { return ghastTearsMax; }
    public double getEchoShardChance() { return echoShardChance; }

    // Mob Book Drops
    public boolean isMobBookDropsEnabled() { return mobBookDropsEnabled; }
    public double getHostileBookChance() { return hostileBookChance; }
    public double getBossCustomBookChance() { return bossCustomBookChance; }
    public double getLootingBonusRegular() { return lootingBonusRegular; }
    public double getLootingBonusBoss() { return lootingBonusBoss; }

    // Blessings
    public int getCBlessMaxUses() { return cBlessMaxUses; }
    public int getCBlessHealPerUse() { return cBlessHealPerUse; }
    public int getAmiBlessMaxUses() { return amiBlessMaxUses; }
    public double getAmiBlessDamagePercentPerUse() { return amiBlessDamagePercentPerUse; }
    public int getLaBlessMaxUses() { return laBlessMaxUses; }
    public double getLaBlessDefensePercentPerUse() { return laBlessDefensePercentPerUse; }

    // Anvil
    public boolean isRemoveTooExpensive() { return removeToolExpensive; }
    public double getXpCostReduction() { return xpCostReduction; }

    // Toggles
    public boolean isInventorySortEnabled() { return inventorySortEnabled; }
    public boolean isEnchantTransferEnabled() { return enchantTransferEnabled; }
    public boolean isSickleEnabled() { return sickleEnabled; }
    public boolean isBattleAxeEnabled() { return battleAxeEnabled; }
    public boolean isBattleBowEnabled() { return battleBowEnabled; }
    public boolean isBattleMaceEnabled() { return battleMaceEnabled; }
    public boolean isBattleShovelEnabled() { return battleShovelEnabled; }
    public boolean isSuperToolsEnabled() { return superToolsEnabled; }
    public boolean isDropRateBoosterEnabled() { return dropRateBoosterEnabled; }
    public boolean isSpearEnabled() { return spearEnabled; }

    // Drop Rate Booster
    public double getTridentDropChance() { return tridentDropChance; }
    public int getBreezeWindChargeMin() { return breezeWindChargeMin; }
    public int getBreezeWindChargeMax() { return breezeWindChargeMax; }

    // Villager Trades
    public boolean isVillagerTradesEnabled() { return villagerTradesEnabled; }
    public double getPriceReduction() { return priceReduction; }
    public boolean isDisableTradeLocking() { return disableTradeLocking; }

    // King Mob
    public boolean isKingMobEnabled() { return kingMobEnabled; }
    public int getSpawnsPerKing() { return spawnsPerKing; }
    public double getKingHealthMultiplier() { return kingHealthMultiplier; }
    public double getKingDamageMultiplier() { return kingDamageMultiplier; }
    public int getKingDiamondDropMin() { return kingDiamondDropMin; }
    public int getKingDiamondDropMax() { return kingDiamondDropMax; }

    // Axe Nerf
    public boolean isAxeNerfEnabled() { return axeNerfEnabled; }
    public double getAxeAttackSpeed() { return axeAttackSpeed; }

    // Weapon Mastery
    public boolean isWeaponMasteryEnabled() { return weaponMasteryEnabled; }
    public int getMasteryMaxKills() { return masteryMaxKills; }
    public double getMasteryMaxBonusPercent() { return masteryMaxBonusPercent; }

    // Blood Moon
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

    // Guilds
    public boolean isGuildsEnabled() { return guildsEnabled; }
    public int getGuildsMaxMembers() { return guildsMaxMembers; }
    public boolean isGuildsFriendlyFire() { return guildsFriendlyFire; }

    // Best Buddies
    public double getDogArmorDamageReduction() { return dogArmorDamageReduction; }

    // Super Tools
    public double getSuperIronBonusDamage() { return superIronBonusDamage; }
    public double getSuperDiamondBonusDamage() { return superDiamondBonusDamage; }
    public double getSuperNetheriteBonusDamage() { return superNetheriteBonusDamage; }
    public double getSuperNethPerEnchantBonus() { return superNethPerEnchantBonus; }

    // Ore Detect
    public int getOreDetectRadiusDiamond() { return oreDetectRadiusDiamond; }
    public int getOreDetectRadiusNetherite() { return oreDetectRadiusNetherite; }
    public int getOreDetectAncientDebrisRadiusDiamond() { return oreDetectAncientDebrisRadiusDiamond; }
    public int getOreDetectAncientDebrisRadiusNetherite() { return oreDetectAncientDebrisRadiusNetherite; }
    public int getOreDetectDurationTicks() { return oreDetectDurationTicks; }

    // Weapon Abilities
    public double getEnderDragonAbilityDamageReduction() { return enderDragonAbilityDamageReduction; }
    public double getNetheriteEnchantBonusPercent() { return netheriteEnchantBonusPercent; }

    // ============================================================
    // ALIAS METHODS — backward compatibility with existing code
    // ============================================================

    // JGlimsPlugin.java uses this alias
    public int getPaleGardenFogInterval() { return paleGardenFogCheckInterval; }

    // BlessingManager aliases
    public double getAmiBlessDmgPerUse() { return amiBlessDamagePercentPerUse; }
    public double getLaBlessDefPerUse() { return laBlessDefensePercentPerUse; }

    // MobDifficultyManager aliases
    public double getBaselineHealth() { return baselineHealthMultiplier; }
    public double getBaselineDamage() { return baselineDamageMultiplier; }

    // KingMobManager aliases
    public double getKingHealthMult() { return kingHealthMultiplier; }
    public double getKingDamageMult() { return kingDamageMultiplier; }
    public int getKingDiamondMin() { return kingDiamondDropMin; }
    public int getKingDiamondMax() { return kingDiamondDropMax; }

    // EnchantmentEffectListener alias
    public double getAxeNerfAttackSpeed() { return axeAttackSpeed; }

    // DropRateListener aliases
    public int getGuardianPrismarineMin() { return guardianShardsMin; }
    public int getGuardianPrismarineMax() { return guardianShardsMax; }
    public int getElderGuardianPrismarineMin() { return elderGuardianShardsMin; }
    public int getElderGuardianPrismarineMax() { return elderGuardianShardsMax; }
    public int getGhastTearMin() { return ghastTearsMin; }
    public int getGhastTearMax() { return ghastTearsMax; }

    // LootBoosterListener alias
    public double getHostileMobBookChance() { return hostileBookChance; }

    // VillagerTradeListener alias
    public double getVillagerPriceReduction() { return priceReduction; }

    // Dog armor alias
    public double getDogArmorReduction() { return dogArmorDamageReduction; }

    // Super tools aliases from summary document
    public double getSuperToolBonusDamageIron() { return superIronBonusDamage; }
    public double getSuperToolBonusDamageDiamond() { return superDiamondBonusDamage; }
    public double getSuperToolBonusDamageNetherite() { return superNetheriteBonusDamage; }
    public double getSuperToolEnchantBonusPercent() { return superNethPerEnchantBonus; }
    public double getSuperEnchantBonusPercent() { return superNethPerEnchantBonus; }

    // Ore detect aliases
    public int getAncientDebrisDetectDiamondRadius() { return oreDetectAncientDebrisRadiusDiamond; }
    public int getAncientDebrisDetectNetheriteRadius() { return oreDetectAncientDebrisRadiusNetherite; }
    public int getAncientDebrisDetectRadiusDiamond() { return oreDetectAncientDebrisRadiusDiamond; }
    public int getAncientDebrisDetectRadiusNetherite() { return oreDetectAncientDebrisRadiusNetherite; }
    public int getOreDetectDiamondRadius() { return oreDetectRadiusDiamond; }
    public int getOreDetectNetheriteRadius() { return oreDetectRadiusNetherite; }

    // Pale garden extended aliases
    public int getPaleGardenFogDuration() { return config.getInt("pale-garden-fog.effect-duration", 80); }
    public int getPaleGardenFogRadius() { return config.getInt("pale-garden-fog.check-radius", 48); }
    public int getPaleGardenSlowInterval() { return config.getInt("pale-garden-fog.slowness-check-interval", 60); }
    public int getPaleGardenSlowDuration() { return config.getInt("pale-garden-fog.slowness-effect-duration", 100); }
    public int getPaleGardenSlowRadius() { return config.getInt("pale-garden-fog.slowness-check-radius", 32); }

    // Ami bless aliases
    public int getAmiBlessTotalUses() { return amiBlessMaxUses; }
    public int getAmiBlessMaxUses2() { return amiBlessMaxUses; }

}

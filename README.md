JGLIMSPLUGIN — DEFINITIVE PROJECT SUMMARY & REFERENCE DOCUMENT
Version: 1.3.0 (in development) Author: JGlims Repository: https://github.com/JGlims/JGlimsPlugin Date: March 4, 2026 Build Status: BUILD SUCCESSFUL (after applying 3 pending fixes listed in Section 20) Current Phase: Phase 2 in progress (Bug Fixes & Missing Systems)

TABLE OF CONTENTS
Server Infrastructure
Build & Deploy Workflow
Docker Commands
build.gradle (Full)
plugin.yml (Full)
config.yml (Full)
Complete File Inventory & Package Structure
Uniform Weapon Progression System
All 52 Custom Enchantments (+ Planned Expansions)
Weapon Abilities (Updated Reduced Cooldowns)
Mob Difficulty, Bosses, Events
Additional Systems (Guilds, Blessings, Loot, Mastery, etc.)
Configuration Defaults (All Fields)
Commands
All PDC Keys
API Notes for PaperMC 1.21.11
What Was Done (Complete History of All Changes)
Current Bugs / Pending Fixes (MUST DO)
Future: New Enchantments, Items & Ideas (Phase 9+)
Future: Custom Textures via GeyserMC + Rainbow (Final Phase)
Key Links
Step-by-Step Phase Plan
Instructions for the Next Chat
1. SERVER INFRASTRUCTURE
Hardware: OCI VM — 4 OCPUs, 24 GB system RAM, 47 GB boot disk, Ubuntu.

Docker container: mc-crossplay running itzg/minecraft-server:latest.

Key environment variables: EULA=TRUE, TYPE=PAPER, VERSION=1.21.11, MEMORY=8G, ONLINE_MODE=false, RCON_PASSWORD=JGlims2026Rcon, PLUGINS pointing to Geyser-Spigot and Floodgate-Spigot auto-downloads from download.geysermc.org.

Ports: 25565/TCP (Java), 19132/UDP (Bedrock via Geyser), 25575/TCP (RCON).

Installed plugins: JGlimsPlugin v1.3.0 (our plugin), Geyser-Spigot v2.9.4, Floodgate v2.2.5, Chunky v1.4.40. SkinsRestorer v15.10.1 pending installation.

World data: ~/minecraft-server/data/ with world, world_nether, world_the_end.

Performance target: TPS 20 (100 FPS equivalent), stay under 6 GB of the 8 GB heap. Never break farms; only alter health/damage/speed on mobs; use lightweight listeners. Use player-specific spawnParticle to reduce network load.

2. BUILD & DEPLOY WORKFLOW
Build (Windows):

.\gradlew.bat clean jar
Output JAR: build/libs/JGlimsPlugin-1.3.0.jar

Build (Linux/Mac):

./gradlew clean jar
Deploy:

Copyscp build/libs/JGlimsPlugin-1.3.0.jar MinecraftServer:~/
ssh MinecraftServer
cp ~/JGlimsPlugin-1.3.0.jar ~/minecraft-server/data/plugins/
rm ~/minecraft-server/data/plugins/JGlimsPlugin-1.2.0.jar
docker restart mc-crossplay
docker logs mc-crossplay 2>&1 | grep -i jglims
Build configuration: Java 21 toolchain, io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT as compileOnly, Maven Central + PaperMC repo (https://repo.papermc.io/repository/maven-public/), reproducible build, base name JGlimsPlugin.

3. DOCKER COMMANDS
Creative test server:

Copydocker run -d --name mc-crossplay \
  -p 25565:25565 -p 19132:19132/udp -p 25575:25575 \
  -e EULA=TRUE -e TYPE=PAPER -e VERSION=1.21.11 \
  -e MEMORY=8G -e ONLINE_MODE=false -e GAMEMODE=creative -e DIFFICULTY=hard \
  -e RCON_PASSWORD=JGlims2026Rcon \
  -e PLUGINS="https://download.geysermc.org/v2/projects/geyser/versions/latest/builds/latest/downloads/spigot,https://download.geysermc.org/v2/projects/floodgate/versions/latest/builds/latest/downloads/spigot" \
  -v ~/minecraft-server/data:/data \
  itzg/minecraft-server:latest
Switch to survival:

Copydocker rm -f mc-crossplay
# Optionally delete creative world:
# rm -rf ~/minecraft-server/data/world ~/minecraft-server/data/world_nether ~/minecraft-server/data/world_the_end
docker run -d --name mc-crossplay \
  -p 25565:25565 -p 19132:19132/udp -p 25575:25575 \
  -e EULA=TRUE -e TYPE=PAPER -e VERSION=1.21.11 \
  -e MEMORY=8G -e ONLINE_MODE=false -e GAMEMODE=survival -e DIFFICULTY=hard \
  -e RCON_PASSWORD=JGlims2026Rcon \
  -e PLUGINS="https://download.geysermc.org/v2/projects/geyser/versions/latest/builds/latest/downloads/spigot,https://download.geysermc.org/v2/projects/floodgate/versions/latest/builds/latest/downloads/spigot" \
  -v ~/minecraft-server/data:/data \
  itzg/minecraft-server:latest
Useful Docker commands:

Copydocker logs mc-crossplay --tail 100              # View recent logs
docker logs mc-crossplay 2>&1 | grep -i jglims   # Check plugin loaded
docker exec -i mc-crossplay rcon-cli              # Enter RCON console
docker exec mc-crossplay mc-health                # Check server health
docker stats mc-crossplay                         # Live resource usage
docker restart mc-crossplay                       # Restart server
docker rm -f mc-crossplay                         # Remove container
4. build.gradle (FULL FILE)
Copyplugins {
    id 'java'
}

group = 'com.jglims.plugin'
version = '1.3.0'

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
    maven {
        name = "papermc"
        url = "https://repo.papermc.io/repository/maven-public/"
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
}

tasks.jar {
    archiveBaseName.set('JGlimsPlugin')
}
5. plugin.yml (FULL FILE)
Copyname: JGlimsPlugin
version: 1.3.0
main: com.jglims.plugin.JGlimsPlugin
api-version: 1.21
author: JGlims
description: >-
  Custom enchantments, battle axes, battle bows, battle maces, super tools,
  sickles, blessings, mob difficulty, inventory sorting, weapon mastery,
  blood moon events, guilds, and much more.

commands:
  jglims:
    description: JGlimsPlugin admin commands
    usage: /jglims <reload|stats|enchants|sort|mastery>
    permission: jglims.admin
  guild:
    description: Guild management commands
    usage: /guild <create|invite|join|leave|kick|disband|info|list>

permissions:
  jglims.admin:
    description: Access to all JGlimsPlugin admin commands
    default: op
6. config.yml (FULL FILE)
Copy# ==============================================
# JGlimsPlugin v1.3.0 Configuration
# ==============================================

# --- Mob Difficulty ---
mob-difficulty:
  enabled: true
  baseline-health-multiplier: 1.0
  baseline-damage-multiplier: 1.0
  distance:
    350:
      health: 1.5
      damage: 1.3
    700:
      health: 2.0
      damage: 1.6
    1000:
      health: 2.5
      damage: 1.9
    2000:
      health: 3.0
      damage: 2.2
    3000:
      health: 3.5
      damage: 2.5
    5000:
      health: 4.0
      damage: 3.0
  biome:
    pale-garden: 2.0
    deep-dark: 2.5
    swamp: 1.4
    nether-wastes:
      health: 1.7
      damage: 1.7
    soul-sand-valley:
      health: 1.9
      damage: 1.9
    crimson-forest:
      health: 2.0
      damage: 2.0
    warped-forest:
      health: 2.0
      damage: 2.0
    basalt-deltas:
      health: 2.3
      damage: 2.3
    end:
      health: 2.5
      damage: 2.0

# --- Boss Enhancer ---
boss-enhancer:
  ender-dragon:
    health: 3.5
    damage: 3.0
  wither:
    health: 1.0
    damage: 1.0
  warden:
    health: 1.0
    damage: 1.0
  elder-guardian:
    health: 2.5
    damage: 1.8

# --- Creeper Reduction ---
creeper-reduction:
  enabled: true
  cancel-chance: 0.5

# --- Pale Garden Fog ---
pale-garden-fog:
  enabled: true
  check-interval: 40

# --- Loot Booster ---
loot-booster:
  enabled: true
  chest-enchanted-book: true
  guardian-shards-min: 1
  guardian-shards-max: 3
  elder-guardian-shards-min: 3
  elder-guardian-shards-max: 5
  ghast-tears-min: 1
  ghast-tears-max: 2
  echo-shard-chance: 0.40

# --- Mob Book Drops ---
mob-book-drops:
  enabled: true
  hostile-chance: 0.05
  boss-custom-chance: 0.15
  looting-bonus-regular: 0.02
  looting-bonus-boss: 0.05

# --- Blessings ---
blessings:
  c-bless:
    max-uses: 10
    heal-per-use: 1
  ami-bless:
    max-uses: 10
    damage-percent-per-use: 2.0
  la-bless:
    max-uses: 10
    defense-percent-per-use: 2.0

# --- Anvil ---
anvil:
  remove-too-expensive: true
  xp-cost-reduction: 0.5

# --- Toggles ---
toggles:
  inventory-sort: true
  enchant-transfer: true
  sickle: true
  battle-axe: true
  battle-bow: true
  battle-mace: true
  battle-shovel: true
  super-tools: true
  drop-rate-booster: true
  spear: true

# --- Drop Rate Booster ---
drop-rate-booster:
  trident-drop-chance: 0.35
  breeze-wind-charge-min: 2
  breeze-wind-charge-max: 5

# --- Villager Trades ---
villager-trades:
  enabled: true
  price-reduction: 0.50
  disable-trade-locking: true

# --- King Mob ---
king-mob:
  enabled: true
  spawns-per-king: 500
  health-multiplier: 10.0
  damage-multiplier: 3.0
  diamond-drop-min: 3
  diamond-drop-max: 9

# --- Axe Nerf ---
axe-nerf:
  enabled: true
  attack-speed: 0.5

# --- Weapon Mastery ---
weapon-mastery:
  enabled: true
  max-kills: 1000
  max-bonus-percent: 10.0

# --- Blood Moon ---
blood-moon:
  enabled: true
  check-interval: 100
  chance: 0.15
  mob-health-multiplier: 1.5
  mob-damage-multiplier: 1.3
  boss-every-nth: 10
  boss-health-multiplier: 20.0
  boss-damage-multiplier: 5.0
  boss-diamond-min: 5
  boss-diamond-max: 15
  double-drops: true

# --- Guilds ---
guilds:
  enabled: true
  max-members: 10
  friendly-fire: false

# --- Best Buddies (Dog Armor) ---
best-buddies:
  dog-armor-damage-reduction: 0.95

# --- Super Tools ---
super-tools:
  iron-bonus-damage: 1.0
  diamond-bonus-damage: 2.0
  netherite-bonus-damage: 2.0
  netherite-per-enchant-bonus-percent: 2.0

# --- Ore Detect (Super Pickaxe Ability) ---
ore-detect:
  radius-diamond: 8
  radius-netherite: 12
  ancient-debris-radius-diamond: 24
  ancient-debris-radius-netherite: 40
  duration-ticks: 200

# --- Weapon Abilities ---
weapon-abilities:
  ender-dragon-damage-reduction: 0.30
  netherite-enchant-bonus-percent: 2.0
Copy
7. COMPLETE FILE INVENTORY & PACKAGE STRUCTURE
All source: src/main/java/com/jglims/plugin/. Resources: src/main/resources/.

Package: com.jglims.plugin (root)
File	Description
JGlimsPlugin.java	Main class. onEnable initializes ALL managers in dependency order, registers ALL listeners, starts scheduled tasks. onCommand handles /guild (8 subcommands) and /jglims (5 subcommands). Accessor methods for every manager. BUG: BestBuddiesListener is NOT registered — must add.
Package: com.jglims.plugin.config
File	Description
ConfigManager.java	Loads config.yml with defaults for every system. Provides 100+ primary getters and backward-compatibility alias methods. Constructor: (JavaPlugin). Method: loadConfig(). Key getters: getEnderDragonAbilityDamageReduction() (NOT getEnderDragonDamageReduction), getDogArmorReduction(), getOreDetectRadiusDiamond(), getOreDetectRadiusNetherite(), getOreDetectAncientDebrisRadiusDiamond(), getOreDetectAncientDebrisRadiusNetherite(), getOreDetectDurationTicks(), getSuperIronBonusDamage(), getSuperDiamondBonusDamage(), getSuperNetheriteBonusDamage(), getSuperNethPerEnchantBonus(), getPaleGardenFogCheckInterval() / getPaleGardenFogInterval() (alias), and many more.
Package: com.jglims.plugin.enchantments
File	Description
EnchantmentType.java	Enum of all 52 custom enchantments with getMaxLevel(). Categories: Sword (5), Axe (5), Pickaxe (5), Shovel (1), Hoe/Sickle (3), Bow (4), Crossbow (2), Trident (5), Armor (9), Elytra (4), Mace (4), Spear (3), Universal (2).
CustomEnchantManager.java	Registry of NamespacedKeys, conflict map, get/set/remove/list/copy enchantments via PersistentDataContainer. Used by all listeners that read enchantment data.
AnvilRecipeListener.java	52 anvil recipes. Uses AnvilView API (not deprecated AnvilInventory). Handles soulbound, book creation, book application, conflict checks, XP cost reduction, "Too Expensive" removal. Includes v1.3.0 spear enchantments (IMPALING_THRUST, EXTENDED_REACH, SKEWERING).
EnchantmentEffectListener.java	ALL enchantment behaviors: damage-event handlers, defender handlers, projectile handlers, block-break handlers, interact handlers, movement/passive handlers. Implements every enchantment's actual gameplay effect.
SoulboundListener.java	Keep-on-death for SOULBOUND items, lostsoul conversion (item drops with glow), one-per-inventory enforcement. Uses AnvilView API.
Package: com.jglims.plugin.weapons
File	Status	Description
BattleSwordManager.java	NEW v1.3.0	Battle Swords (+1 dmg, 1.6 speed). PDC: is_battle_sword (BYTE).
BattlePickaxeManager.java	NEW v1.3.0	Battle Pickaxes (+1 dmg, retains mining speed). PDC: is_battle_pickaxe (BYTE).
BattleTridentManager.java	NEW v1.3.0	Battle Trident (10 dmg, 1.1 speed). PDC: is_battle_trident (BYTE).
BattleSpearManager.java	NEW v1.3.0	Battle Spears (+1 dmg per tier). PDC: is_battle_spear (BYTE).
BattleAxeManager.java	Existing	Battle Axes (sickle dmg + 1, 0.9 speed). PDC: is_battle_axe (BYTE). Blocks stripping.
BattleBowManager.java	Existing	Battle Bow/Crossbow. PDC: is_battle_bow (BYTE), is_battle_crossbow (BYTE).
BattleMaceManager.java	Existing	Battle Mace (12 dmg, 0.7 speed). PDC: is_battle_mace (BYTE).
BattleShovelManager.java	Existing v1.3.0	Battle Shovels (+1.5 dmg, 1.2 speed). PDC: battle_shovel (BOOLEAN). Blocks path-making. Constructor: (JGlimsPlugin, ConfigManager).
SickleManager.java	Existing	Sickles from hoes (sword dmg + 1, 1.1 speed). PDC: is_sickle (BYTE). Blocks tilling.
SpearManager.java	Existing v1.3.0	Super spear tiers via PDC super_spear_tier (INTEGER 1/2/3). SpearTier enum. Constructor: (JGlimsPlugin, ConfigManager). Method: getSuperTierKey() returns the NamespacedKey.
SuperToolManager.java	MODIFIED v1.3.0	3-tier super system (Iron=1, Diamond=2, Netherite=3). isBattleItem() checks all 10 PDC keys. createSuperTool() requires battle item (gate). Lore format: Attack Damage: [base] +[battle] +[super] = [total]. Preserves enchantments through upgrades. Auto-applies Efficiency (I/II/III by tier) to super shovels.
WeaponAbilityListener.java	MODIFIED v1.3.0	Right-click abilities for Diamond(tier≥2)/Netherite(tier≥3) super tools. All 10 weapon classes implemented with complete abilities. Constructor: (JGlimsPlugin, ConfigManager, CustomEnchantManager, SuperToolManager, SpearManager, BattleShovelManager). BUG: contains Sound.BLOCK_LIGHTNING_ROD_TOGGLE_ON which does NOT exist — must replace with Sound.ENTITY_LIGHTNING_BOLT_IMPACT. DESIRED: reduce all cooldowns (see Section 10).
WeaponMasteryManager.java	MODIFIED v1.3.0	10 weapon classes (sword, axe, pickaxe, shovel, sickle, bow, crossbow, trident, mace, spear). Linear scaling: kills/1000 = up to 10% max damage bonus. PDC keys: mastery_sword through mastery_spear (INTEGER), mastery_damage (AttributeModifier key).
Package: com.jglims.plugin.crafting
File	Description
RecipeManager.java	MODIFIED v1.3.0. 12-arg constructor: (JGlimsPlugin, SickleManager, BattleAxeManager, BattleBowManager, BattleMaceManager, SuperToolManager, BattleSwordManager, BattlePickaxeManager, BattleTridentManager, BattleSpearManager, BattleShovelManager, SpearManager). Registers ALL battle + super recipes. PDC guard blocks vanilla→Super (must be battle first).
VanillaRecipeRemover.java	Removes conflicting vanilla recipes. Static method: remove(JavaPlugin).
Package: com.jglims.plugin.mobs
File	Description
MobDifficultyManager.java	Distance + biome mob scaling. Reads all distance/biome values from ConfigManager. Implements Listener.
BossEnhancer.java	Boss health/damage multipliers. Dragon 3.5×/3.0×, Elder Guardian 2.5×/1.8×, Wither/Warden vanilla. Constructor: (JGlimsPlugin, ConfigManager).
KingMobManager.java	Every 500 hostile mob spawns → King variant (10× HP, 3× damage, gold glowing, no despawn, one per world, drops 3-9 diamonds). Constructor: (JGlimsPlugin, ConfigManager).
BloodMoonManager.java	15% chance each night. 1.5×/1.3× mob multipliers, Darkness + red particles, double drops/XP. Every 10th Blood Moon: Blood Moon King (Zombie, diamond armor, Netherite sword, 400 HP, 5× dmg, drops 5-15 diamonds + netherite scrap). Constructor: (JGlimsPlugin, ConfigManager). Method: startScheduler().
Package: com.jglims.plugin.guilds
File	Description
GuildManager.java	CRUD operations: createGuild, invitePlayer, joinGuild, leaveGuild, kickPlayer, disbandGuild, showGuildInfo, listGuilds. Persists to guilds.yml. Max 10 members. Constructor: (JGlimsPlugin, ConfigManager).
GuildListener.java	Friendly fire prevention between guild members. Constructor: (JGlimsPlugin, GuildManager).
Package: com.jglims.plugin.blessings
File	Description
BlessingManager.java	3 blessings: C's (health +1 heart/use), Ami's (damage +2%/use), La's (armor +2%/use). Permanent via AttributeModifiers. Method: showStats(CommandSender, Player). Constructor: (JGlimsPlugin).
BlessingListener.java	Right-click consumption of blessing items, PDC persistence, join/respawn reapply of permanent buffs. Constructor: (JGlimsPlugin, BlessingManager).
Package: com.jglims.plugin.effects
File	Description
PaleGardenFogTask.java	Every N ticks (default 40), applies Darkness to players in Pale Garden biome. Constructor: (JGlimsPlugin). Method: start(int intervalTicks).
Package: com.jglims.plugin.utility
File	Description
BestBuddiesListener.java	BEST_BUDDIES enchantment on wolf armor. 95% incoming damage reduction (configurable), 0 melee damage (pacifist wolf), permanent Regeneration II, heart particles. Uses wolf.getEquipment().getItem(EquipmentSlot.BODY) for armor detection. Handles chunk load persistence, global scan task every 10 seconds. Constructor: (JGlimsPlugin, ConfigManager). NOT YET REGISTERED in JGlimsPlugin.java.
InventorySortListener.java	Mouse Tweaks-style shift-click sorting. Updated for all 10 weapon categories. Constructor: (JGlimsPlugin).
EnchantTransferListener.java	Enchanted item + plain book in anvil → enchanted book with all enchantments, 0 XP cost. Uses AnvilView. Constructor: (JGlimsPlugin, CustomEnchantManager).
LootBoosterListener.java	Enchanted books in chest loot. Mob drops: hostile 5%, boss 15% (+looting bonus). Guardian prismarine shards 1-3, Elder Guardian 3-5, echo shard chance 40%. Constructor: (JGlimsPlugin, ConfigManager).
DropRateListener.java	Trident 35% from Drowned, Breeze 2-5 wind charges. Constructor: (JGlimsPlugin, ConfigManager).
VillagerTradeListener.java	50% price reduction, trade locking disabled. Constructor: (JGlimsPlugin, ConfigManager).
Resources: src/main/resources/
File	Description
plugin.yml	See Section 5.
config.yml	See Section 6.
8. UNIFORM WEAPON PROGRESSION SYSTEM (v1.3.0)
THE RULE: ALL weapons must become Battle before they can become Super. Vanilla → Battle (+1 dmg) → Super Iron (Battle +1) → Super Diamond (Battle +2, unlocks Diamond ability) → Super Netherite (Battle Definitive +3, unlocks Netherite ability). Exceptions: Elytra and Shield bypass Battle, go directly to Super.

Battle Conversion Details:

Weapon	Vanilla Dmg	Battle Dmg	Battle Speed	Recipe
Sword	4-8 (by material)	5-9	1.6	8 matching material + sword center
Pickaxe	2-6	3-7	1.2	8 matching material + pickaxe center
Axe	special formula	+1 over sickle	0.9	ingredient + block + axe (special pattern)
Shovel	2.5-6.5	4-8	1.2	8 matching material + shovel center
Hoe→Sickle	1	5-9 (sword dmg +1)	1.1	8 matching material + hoe center
Bow	N/A (ranged)	N/A	N/A	8 iron ingots + bow center
Crossbow	N/A (ranged)	N/A	N/A	8 iron ingots + crossbow center
Mace	11	12	0.7	8 breeze rods + mace center
Trident	9	10	1.1	8 prismarine shards + trident center
Spear	1-5 (by material)	2-6	0.87-1.54	8 matching material + spear center
Super Tier Bonuses:

Tier	PDC Value	Bonus Damage	Ability	Special Rules
Iron	1	+1	None	—
Diamond	2	+2 (non-neth) / +1 (neth base)	Right-click Diamond ability	—
Netherite	3	+3 (non-neth) / +2 (neth base)	Right-click Definitive ability	No enchant conflicts, +2% dmg/enchant, no enchant limit
Trident exception: Diamond +3, Netherite +5.

Lore format: Attack Damage: [vanilla base] +[battle +1] +[super bonus] = [total]

Super recipe: Super Iron + 8 diamonds → Super Diamond. Super Diamond + 8 netherite ingots → Super Netherite. All upgrades preserve existing enchantments.

Gatekeeper: SuperToolManager.isBattleItem(ItemStack) checks all 10 PDC keys (is_battle_sword, is_battle_axe, is_battle_pickaxe, is_sickle, battle_shovel, is_battle_bow, is_battle_crossbow, is_battle_mace, is_battle_trident, is_battle_spear). Items without a battle PDC key cannot become Super.

9. ALL 52 CUSTOM ENCHANTMENTS (+ PLANNED EXPANSIONS)
Current Enchantments (52):
Sword (5): VAMPIRISM(5)↔LIFESTEAL(3) conflict, BLEED(3)↔VENOMSTRIKE(3) conflict, CHAIN_LIGHTNING(3)↔WITHER_TOUCH conflict

Axe (5): BERSERKER(5)↔BLOOD_PRICE conflict, LUMBERJACK(3), CLEAVE(3), TIMBER(3), GUILLOTINE(3)

Pickaxe (5): VEINMINER(3)↔DRILL(3) conflict, AUTO_SMELT(1), MAGNETISM(1), EXCAVATOR(3)

Shovel (1): HARVESTER(3)

Hoe/Sickle (3): GREEN_THUMB(1), REPLENISH(1), HARVESTING_MOON(3)

Bow (4): EXPLOSIVE_ARROW(3)↔HOMING(3) conflict, RAPIDFIRE(3), SNIPER(3)

Crossbow (2): THUNDERLORD(5), TIDAL_WAVE(3)

Trident (5): FROSTBITE(3), SOUL_REAP(3), BLOOD_PRICE(3)↔BERSERKER conflict, REAPERS_MARK(3), WITHER_TOUCH(3)↔CHAIN_LIGHTNING conflict

Armor (9): SWIFTNESS(3), VITALITY(3), AQUA_LUNGS(3), NIGHT_VISION(1), FORTIFICATION(3), DEFLECTION(3), SWIFTFOOT(3)↔MOMENTUM conflict, DODGE(3), LEAPING(3)

Elytra (4): BOOST(3), CUSHION(1), GLIDER(1), STOMP(3)

Mace (4): SEISMIC_SLAM(3)↔MAGNETIZE(3) conflict, GRAVITY_WELL(3), MOMENTUM(3)↔SWIFTFOOT conflict

Spear (3) — NEW v1.3.0: IMPALING_THRUST(3), EXTENDED_REACH(3), SKEWERING(3) — conflict triangle (each conflicts with the other two)

Universal (2): SOULBOUND(1), BEST_BUDDIES(1)

PLANNED NEW ENCHANTMENTS (Phase 9+) — 12 total:
Sickle (3): SOUL_HARVEST(3) — Wither I-III on hit + Regen I-III for player while Wither active, conflicts GREEN_THUMB. REAPING_CURSE(3) — lingering Instant Damage cloud at corpse on kill, conflicts REPLENISH. CROP_REAPER(1) — kills have 15%+5%/Looting chance to drop seeds/wheat/carrots/potatoes.

Shovel (2): BURIAL(3) — Slowness III + Blindness I for 1-3s on hit, conflicts HARVESTER. EARTHSHATTER(3) — breaking a block sends shockwave breaking identical blocks in 1-3 radius.

Sword (1): FROSTBITE_BLADE(3) — Slowness I-III + freeze visual on hit, conflicts VENOMSTRIKE.

Axe (1): WRATH(3) — consecutive hits within 3s on same target: +10/20/30% damage, resets on target switch, conflicts CLEAVE.

Pickaxe (1): PROSPECTOR(3) — 5/10/15% chance double ore yield (stacks with Fortune), conflicts AUTO_SMELT.

Trident (1): TSUNAMI(3) — thrown hit creates water burst pushing mobs 3/5/7 blocks + Slowness I 2s (rain/water only), conflicts FROSTBITE.

Bow (1): FROSTBITE_ARROW(3) — arrows apply Slowness I-III + extinguish fire, conflicts EXPLOSIVE_ARROW.

Mace (1): TREMOR(3) — on hit, mobs within 2/4/6 blocks get Mining Fatigue II for 3s, conflicts GRAVITY_WELL.

Spear (1): PHANTOM_PIERCE(3) — charged spear attacks pierce 1/2/3 targets dealing full damage, conflicts SKEWERING.

10. WEAPON ABILITIES — UPDATED REDUCED COOLDOWNS
All abilities require Super Diamond (tier ≥ 2) or Super Netherite (tier ≥ 3). Activated by right-click with the super weapon in main hand.

Weapon	Diamond Ability	Old CD	New CD	Damage	Netherite Ability	Old CD	New CD	Damage
Sword	Dash Strike	10s	6s	8/tick (dash)	Dimensional Cleave	18s	12s	15/tick + 25 final (12-block rift AoE)
Axe	Bloodthirst	12s	8s	buff: 5s lifesteal + Haste + Strength	Ragnarok Cleave	22s	15s	20/16/12 (3 expanding shockwaves)
Pickaxe	Ore Pulse	15s	10s	none (detect ores 8-block, debris 24-block)	Seismic Resonance	25s	18s	none (detect ores 12-block, debris 40-block)
Shovel	Earthen Wall	12s	8s	6 + launch + Resistance II	Tectonic Upheaval	20s	14s	14/wave + Resistance II + Slowness III
Sickle	Harvest Storm	10s	6s	7 initial + 2×4 bleed (5-block AoE spin)	Reaper's Scythe	20s	14s	10/tick + 1.5 lifesteal + Wither II (7-block dual arc)
Spear	Phantom Lunge	12s	8s	8 piercing (8-block dash)	Spear of the Void	20s	14s	18 pierce + 10 detonation (30-block projectile)
Bow	Arrow Storm	12s	8s	6/arrow × 5 rapid-fire	Celestial Volley	20s	14s	10/arrow × 12 from sky + 8 AoE impact
Crossbow	Chain Shot	12s	8s	8 + 4 chain × 3 piercing bolts	Thunder Barrage	22s	15s	12 + 14 AoE × 6 explosive bolts
Trident	Tidal Surge	12s	8s	8 + water knockback + Slowness I	Poseidon's Wrath	20s	14s	16/13/10 (3 water waves) + 10 lightning strike
Mace	Ground Slam	12s	8s	10 + stun (Slowness III + Mining Fatigue)	Meteor Strike	22s	15s	5-25 distance-based (launch up + slam AoE)
Ender Dragon exception: All Netherite Definitive abilities deal only 30% damage to the Ender Dragon. Controlled by config.getEnderDragonAbilityDamageReduction() (default 0.30). Diamond abilities are NOT reduced against the Dragon.

Bloodthirst special mechanic: When activated, stores bloodthirst_active LONG PDC key on the weapon with expiry timestamp. During the 5-second window, all melee hits heal the player (implemented in EnchantmentEffectListener or checked inline).

Ore Pulse / Seismic Resonance: Uses player-specific spawnParticle for ore highlighting (only the caster sees the ore particles). Each ore type has a unique color: Coal(50,50,50), Iron(210,150,100), Copper(180,100,50), Gold(255,215,0), Redstone(255,0,0), Lapis(30,30,200), Emerald(0,200,50), Diamond(100,230,255), Ancient Debris(100,50,20) + flame particles, Nether Gold(255,200,50), Nether Quartz(240,240,230).

11. MOB DIFFICULTY, BOSSES, EVENTS
Baseline: 1.0× health, 1.0× damage (vanilla).

Distance scaling (Overworld only): 0-349 blocks = none. 350 = 1.5×/1.3×. 700 = 2.0×/1.6×. 1000 = 2.5×/1.9×. 2000 = 3.0×/2.2×. 3000 = 3.5×/2.5×. 5000+ = 4.0×/3.0×. Scaling is incremental between thresholds.

Biome multipliers (stack with distance): Pale Garden 2.0×, Deep Dark 2.5×, Swamp 1.4×, Nether Wastes 1.7×/1.7×, Soul Sand Valley 1.9×/1.9×, Crimson Forest 2.0×/2.0×, Warped Forest 2.0×/2.0×, Basalt Deltas 2.3×/2.3×, End 2.5×/2.0×.

Boss Enhancer: Ender Dragon 3.5×/3.0×, Elder Guardian 2.5×/1.8×, Wither = vanilla (1.0×/1.0×), Warden = vanilla (1.0×/1.0×).

Creeper reduction: 50% of creeper spawns are cancelled (cancel-chance: 0.5).

King Mob: Every 500 hostile mob spawns → one King variant. King stats: 10× health, 3× damage. Visual: gold glowing, no despawn. Drops: 3-9 diamonds. Limit: one per world at a time.

Blood Moon: 15% chance each night (checked every 100 ticks). During Blood Moon: 1.5×/1.3× all mob health/damage, Darkness effect on all players, red particles in the sky, double drops and XP from all mobs. Every 10th Blood Moon: Blood Moon King spawns — a Zombie with diamond armor, Netherite sword, 400 HP (20.0× base), 5.0× damage. Blood Moon King drops: 5-15 diamonds + netherite scrap on death.

12. ADDITIONAL SYSTEMS
Guilds: Full guild system. Commands: /guild create <name>, invite <player>, join (accepts pending invite), leave, kick <player>, disband, info (shows guild details), list (shows all guilds). Max 10 members per guild. Friendly fire disabled between guild members. Data persists to plugins/JGlimsPlugin/guilds.yml.

Blessings (3 types):

C's Bless: 10 uses, +1 heart (2 HP) per use, permanent via AttributeModifier on MAX_HEALTH.
Ami's Bless: 10 uses, +2% attack damage per use, permanent via AttributeModifier on ATTACK_DAMAGE.
La's Bless: 10 uses, +2% armor per use, permanent via AttributeModifier on ARMOR.
Right-click to consume. PDC tracks uses remaining. Effects reapply on join/respawn.
Best Buddies: Apply BEST_BUDDIES enchantment to Wolf Armor (via anvil). When equipped on a tamed wolf: wolf takes 95% less damage (configurable), wolf deals 0 melee damage (pacifist), wolf gets permanent Regeneration II, heart particles every 2 seconds. Wolf armor detected via wolf.getEquipment().getItem(EquipmentSlot.BODY). Effects persist across chunk loads and server restarts via PDC marker (best_buddies_applied BYTE) and global scan task.

Weapon Mastery: 10 weapon classes tracked separately: sword, axe, pickaxe, shovel, sickle, bow, crossbow, trident, mace, spear. Linear scaling: (kills / 1000) × 10% = max 10% damage bonus at 1000 kills. PDC keys: mastery_sword through mastery_spear (INTEGER kill count). Damage bonus applied via mastery_damage AttributeModifier. View progress: /jglims mastery.

Inventory Sort: Shift-click any empty slot in a container inventory to sort all items. Mouse Tweaks-style behavior. Sorts by material type with special handling for all 10 weapon categories.

Enchant Transfer: Place an enchanted item in slot 1 and a plain Book in slot 2 of an anvil. Result: Enchanted Book containing all enchantments from the original item. Cost: 0 XP. The original item is consumed.

Loot Booster: Chest loot: guaranteed enchanted book (vanilla or custom) added to generated chest loot. Mob drops: hostile mobs have 5% chance to drop a random enchanted book (+2% per Looting level). Boss mobs (Dragon, Wither, Elder Guardian, Warden) have 15% chance to drop a custom enchanted book (+5% per Looting). Guardian drops: 1-3 prismarine shards extra. Elder Guardian drops: 3-5 prismarine shards extra. Ghast drops: 1-2 ghast tears extra. Echo Shard: 40% chance from Warden.

Drop Rate Booster: Trident drop rate from Drowned: 35% (vanilla is 8.5%). Breeze wind charge drops: 2-5 (vanilla is 1-2).

Villager Trades: All villager prices reduced by 50%. Trade locking (villager refusing to trade after too many trades) is disabled.

Axe Nerf: All axes have attack speed set to 0.5 (much slower than vanilla). This makes swords and other weapons more viable.

Pale Garden Fog: Repeating task (every 40 ticks by default). Players within the Pale Garden biome receive Darkness effect. Creates atmospheric horror feel.

13. COMMANDS
/guild (Player-only)
Subcommand	Usage	Description
create	/guild create <name>	Create a new guild
invite	/guild invite <player>	Invite a player to your guild
join	/guild join	Accept a pending guild invitation
leave	/guild leave	Leave your current guild
kick	/guild kick <player>	Kick a member (guild leader only)
disband	/guild disband	Disband your guild (leader only)
info	/guild info	Show your guild's details
list	/guild list	List all guilds on the server
/jglims (Requires jglims.admin permission, default: op)
Subcommand	Usage	Description
reload	/jglims reload	Reload config.yml
stats	/jglims stats <player>	Show blessing stats for a player
enchants	/jglims enchants	List all 52 custom enchantments
sort	/jglims sort	Info about inventory sorting
mastery	/jglims mastery	Show your weapon mastery progress (player-only)
14. ALL PDC KEYS
Key	Type	Used By	Purpose
is_battle_sword	BYTE	BattleSwordManager	Marks item as a battle sword
is_battle_axe	BYTE	BattleAxeManager	Marks item as a battle axe
is_battle_pickaxe	BYTE	BattlePickaxeManager	Marks item as a battle pickaxe
is_sickle	BYTE	SickleManager	Marks item as a sickle (converted hoe)
battle_shovel	BOOLEAN	BattleShovelManager	Marks item as a battle shovel
is_battle_bow	BYTE	BattleBowManager	Marks item as a battle bow
is_battle_crossbow	BYTE	BattleBowManager	Marks item as a battle crossbow
is_battle_mace	BYTE	BattleMaceManager	Marks item as a battle mace
is_battle_trident	BYTE	BattleTridentManager	Marks item as a battle trident
is_battle_spear	BYTE	BattleSpearManager	Marks item as a battle spear
super_tool_tier	INTEGER (1/2/3)	SuperToolManager	Super tier: 1=Iron, 2=Diamond, 3=Netherite
super_spear_tier	INTEGER (1/2/3)	SpearManager	Super spear tier (separate from general)
super_elytra_durability	INTEGER	SuperToolManager	Custom durability for super elytra
boss_damage_mult	DOUBLE	BossEnhancer	Stored damage multiplier on boss entity
mastery_sword	INTEGER	WeaponMasteryManager	Kill count for sword mastery
mastery_axe	INTEGER	WeaponMasteryManager	Kill count for axe mastery
mastery_pickaxe	INTEGER	WeaponMasteryManager	Kill count for pickaxe mastery
mastery_shovel	INTEGER	WeaponMasteryManager	Kill count for shovel mastery
mastery_sickle	INTEGER	WeaponMasteryManager	Kill count for sickle mastery
mastery_bow	INTEGER	WeaponMasteryManager	Kill count for bow mastery
mastery_crossbow	INTEGER	WeaponMasteryManager	Kill count for crossbow mastery
mastery_trident	INTEGER	WeaponMasteryManager	Kill count for trident mastery
mastery_mace	INTEGER	WeaponMasteryManager	Kill count for mace mastery
mastery_spear	INTEGER	WeaponMasteryManager	Kill count for spear mastery
mastery_damage	NamespacedKey	WeaponMasteryManager	AttributeModifier key for mastery bonus
bloodthirst_active	LONG	WeaponAbilityListener	Timestamp when Bloodthirst expires
best_buddies_applied	BYTE	BestBuddiesListener	Marks wolf as having active Best Buddies
c_bless_item	BYTE	RecipeManager	Marks item as C's Blessing
ami_bless_item	BYTE	RecipeManager	Marks item as Ami's Blessing
la_bless_item	BYTE	RecipeManager	Marks item as La's Blessing
Per-EnchantmentType (52 keys)	INTEGER (level)	CustomEnchantManager	e.g., vampirism, bleed, best_buddies etc. — stores enchantment level on item
15. API NOTES FOR PAPERMC 1.21.11
These are critical notes to avoid compilation errors:

Wolf armor detection: Use wolf.getEquipment().getItem(EquipmentSlot.BODY). The method getBodyArmor() does NOT exist on EntityEquipment. EquipmentSlot.BODY is valid for wolves, horses, happy ghasts, nautiluses.

Arrow lifetime: Use AbstractArrow.setLifetimeTicks(int). The method setLife(int) does NOT exist. Import: org.bukkit.entity.AbstractArrow.

Sound constants: In PaperMC 1.21.11, Sound is an interface (not an enum). This means it has static final fields. Sound.BLOCK_LIGHTNING_ROD_TOGGLE_ON does NOT exist. Use Sound.ENTITY_LIGHTNING_BOLT_IMPACT instead (confirmed to exist). Other confirmed sounds: Sound.ITEM_MACE_SMASH_GROUND, Sound.ITEM_MACE_SMASH_GROUND_HEAVY, Sound.ENTITY_WARDEN_SONIC_BOOM, Sound.ENTITY_WARDEN_EMERGE, Sound.ENTITY_WARDEN_HEARTBEAT, Sound.ITEM_TRIDENT_RIPTIDE_3, Sound.ITEM_TRIDENT_THUNDER, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, Sound.ENTITY_BREEZE_SHOOT, Sound.ITEM_CROSSBOW_SHOOT, Sound.ENTITY_ELDER_GUARDIAN_CURSE.

ConfigManager getters: Use config.getEnderDragonAbilityDamageReduction(). The method getEnderDragonDamageReduction() does NOT exist. Always check the actual getter names in ConfigManager.java.

AnvilView API: Use AnvilView (from org.bukkit.inventory.view.AnvilView) for all anvil inventory operations. The old AnvilInventory.setRepairCost() etc. are deprecated. Use anvilView.setRepairCost(), anvilView.setMaximumRepairCost() etc.

AttributeModifiers: Use the modern constructor: new AttributeModifier(NamespacedKey, amount, Operation). The UUID-based constructor is deprecated.

Adventure API: Use net.kyori.adventure.text.Component for chat messages (not legacy ChatColor). Example: Component.text("message", NamedTextColor.RED).

EquipmentSlotGroup: Required for some AttributeModifier applications. Import: org.bukkit.inventory.EquipmentSlotGroup.

16. WHAT WAS DONE (Complete History)
Phase 1 — Compilation Error Fixes (6 files modified):
#	File	Fix
1	ConfigManager.java	Added getPaleGardenFogInterval() alias, added new config fields for ore-detect / weapon-abilities / super-tools, added 20+ backward-compatibility alias getters
2	JGlimsPlugin.java	Fixed WeaponAbilityListener to use 6-arg constructor (added SpearManager + BattleShovelManager params), added SpearManager and BattleShovelManager field declarations and initialization
3	AnvilRecipeListener.java	Migrated 7 deprecated AnvilInventory method calls to AnvilView API, added 3 spear enchantment recipes (IMPALING_THRUST, EXTENDED_REACH, SKEWERING)
4	SoulboundListener.java	Migrated 2 deprecated AnvilInventory calls to AnvilView
5	EnchantTransferListener.java	Migrated 1 deprecated AnvilInventory call to AnvilView
Phase 1.5 — Battle-for-All Refactor (8 files):
#	File	Change
1	BattleSwordManager.java	NEW — created battle swords for all 5 material tiers
2	BattlePickaxeManager.java	NEW — created battle pickaxes for all 5 material tiers
3	BattleTridentManager.java	NEW — created battle trident
4	BattleSpearManager.java	NEW — created battle spears for all tiers
5	SuperToolManager.java	MODIFIED — added isBattleItem() checking all 10 PDC keys, battle-required gating in createSuperTool(), spear/mace damage tables
6	RecipeManager.java	MODIFIED — 12-arg constructor, all battle recipes + super recipes, PDC guard preventing vanilla→Super
7	JGlimsPlugin.java	MODIFIED — 4 new manager fields (BattleSwordManager, BattlePickaxeManager, BattleTridentManager, BattleSpearManager), all initialization and accessor methods
8	WeaponMasteryManager.java	MODIFIED — added spear mastery class, all new battle weapon recognition
Both phases: BUILD SUCCESSFUL, 0 errors.

Phase 2 — Bug Fixes & Missing Systems (current session, March 4, 2026):
#	File	Change	Status
1	BestBuddiesListener.java	Created complete implementation from scratch. Uses EquipmentSlot.BODY for wolf armor, 95% DR, 0 damage, Regen II, heart particles, chunk persistence, global scan.	DONE — file exists in repo
2	WeaponAbilityListener.java	Implemented all 10 weapon class abilities (Bow: Arrow Storm / Celestial Volley, Crossbow: Chain Shot / Thunder Barrage, Trident: Tidal Surge / Poseidon's Wrath, Mace: Ground Slam / Meteor Strike). Fixed arrow.setLifetimeTicks().	DONE — file exists in repo BUT has Sound bug
3	WeaponAbilityListener.java	Replace Sound.BLOCK_LIGHTNING_ROD_TOGGLE_ON → Sound.ENTITY_LIGHTNING_BOLT_IMPACT	PENDING — must fix
4	WeaponAbilityListener.java	Reduce all cooldowns (see Section 10 table)	PENDING — must apply
5	JGlimsPlugin.java	Add pm.registerEvents(new BestBuddiesListener(this, configManager), this);	PENDING — must add
17. CURRENT BUGS / PENDING FIXES (MUST DO IMMEDIATELY)
There are exactly 3 pending fixes that must be applied before the build is clean:

Fix 1: WeaponAbilityListener.java — Invalid Sound constant
Location: handleCrossbowAbility method, inside the Chain Shot Diamond ability's bolt tracking BukkitRunnable, in the if (bolt.isOnGround() || bolt.isDead()) block. Error: Sound.BLOCK_LIGHTNING_ROD_TOGGLE_ON does not exist in PaperMC 1.21.11. Fix: Replace with Sound.ENTITY_LIGHTNING_BOLT_IMPACT (confirmed exists).

Copy// WRONG:
player.playSound(hitLoc, Sound.BLOCK_LIGHTNING_ROD_TOGGLE_ON, 1.5f, 1.2f);
// CORRECT:
player.playSound(hitLoc, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1.5f, 1.2f);
Fix 2: WeaponAbilityListener.java — Reduce all cooldowns
Location: Every handleXxxAbility method, the int cooldownSec = ... line. Change: Apply the reduced cooldown values from the table in Section 10. Every Diamond ability goes to 6-10s, every Netherite ability goes to 12-18s.

Fix 3: JGlimsPlugin.java — Register BestBuddiesListener
Location: onEnable() method, in the listener registration block (around the other pm.registerEvents(...) calls). Add this line:

Copypm.registerEvents(new BestBuddiesListener(this, configManager), this);
The import is already covered by import com.jglims.plugin.utility.*;.

After applying these 3 fixes: .\gradlew.bat clean jar must produce BUILD SUCCESSFUL with 0 errors.

18. FUTURE: NEW ENCHANTMENTS, ITEMS & IDEAS (Phase 9+)
Planned New Items & Recipes:
Item	Recipe	Mechanic
Warden's Echo (accessory)	Echo Shard + Sculk Catalyst + Amethyst Shard in anvil	Worn in offhand: nearby hostile mobs glow (Spectral) within 16 blocks when sneaking. Uses durability.
Totem of the Blood Moon	1 Totem of Undying + 4 Netherite Scrap + 4 Redstone Blocks	On death during Blood Moon, revive with full health + Strength II for 10s. Single use.
Beacon of the King	9 Diamond Blocks + Nether Star center	Placed at King Mob's death location: grants all nearby players (32 blocks) Haste II + Luck for 5 minutes. Single use.
Enchanted Shulker Shell	Shulker Shell + 4 Ender Pearls + 4 Chorus Fruit	"Portable Ender Chest" — right-click opens ender chest inventory anywhere. 50 uses.
Elemental Arrows (4 types)	Arrow + ingredient (Magma Cream / Snowball / Wind Charge / Glowstone)	Fire Arrow (ignites), Frost Arrow (slows), Wind Arrow (high knockback), Glow Arrow (Spectral effect). Craftable in stacks of 8.
Guild Banner	Banner + guild-specific recipe	Placeable banner that grants guild members within 16 blocks Speed I or Resistance I.
19. FUTURE: CUSTOM TEXTURES VIA GEYSERMC + RAINBOW (Final Phase)
Goal: Give every Battle and Super weapon a unique visual appearance visible to both Java and Bedrock players.

How It Works:

Java players: PaperMC 1.21.4+ item_model component. Plugin sets meta.setItemModel(new NamespacedKey("jglims", "battle_diamond_sword")) on every custom weapon. Java clients use a server resource pack.
Bedrock players: Geyser Custom Items API v2 maps Java item_model values to custom Bedrock items. Requires mapping JSON + Bedrock resource pack.
Implementation Steps:

Plugin-side: Add item_model component to every Battle and Super weapon in the creation methods.
Java Resource Pack: Create assets/jglims/items/, assets/jglims/models/item/, assets/jglims/textures/item/ with custom 16×16 or 32×32 textures. Host on web server, configure server.properties.
Geyser Mapping: Create plugins/Geyser-Spigot/custom_mappings/mappings.json with v2-format definitions mapping each item_model to a Bedrock custom item.
Bedrock Resource Pack: Create pack with item_texture.json and matching textures. Place in plugins/Geyser-Spigot/packs/.
Validate: Use Rainbow (Fabric client mod) — join server, hold each custom item, run /rainbow map then /rainbow finish to auto-generate and compare packs.
Config required: Set enable-custom-content: true in Geyser's config.yml.

Texture Design Ideas: Battle = colored border + "B" insignia. Super Iron = silver metallic sheen. Super Diamond = cyan crystalline glow. Super Netherite = dark crimson/purple aura with molten lava veins.

Tools: Rainbow (generates mappings + Bedrock pack automatically), Thunder (simple Java→Bedrock texture conversion), PackConverter (full pack conversion library).

20. KEY LINKS
Resource	URL
GitHub Repository	https://github.com/JGlims/JGlimsPlugin
PaperMC API Javadoc (1.21.11)	https://jd.papermc.io/paper/1.21.11/
AnvilView API	https://jd.papermc.io/paper/1.21.11/org/bukkit/inventory/view/AnvilView.html
Sound API (Interface)	https://jd.papermc.io/paper/1.21.11/org/bukkit/Sound.html
Wolf API	https://jd.papermc.io/paper/1.21.11/org/bukkit/entity/Wolf.html
AbstractArrow API	https://jd.papermc.io/paper/1.21.11/org/bukkit/entity/AbstractArrow.html
EquipmentSlot API	https://jd.papermc.io/paper/1.21.11/org/bukkit/inventory/EquipmentSlot.html
EntityEquipment API	https://jd.papermc.io/paper/1.21.11/org/bukkit/inventory/EntityEquipment.html
LivingEntity API	https://jd.papermc.io/paper/1.21.11/org/bukkit/entity/LivingEntity.html
PaperMC Downloads	https://papermc.io/downloads/paper
Docker Minecraft Server	https://hub.docker.com/r/itzg/minecraft-server
Docker MC Docs	https://docker-minecraft-server.readthedocs.io/
Geyser Downloads	https://download.geysermc.org/
Geyser Custom Items v2 Docs	https://geysermc.org/wiki/geyser/custom-items/
Geyser Resource Pack Docs	https://geysermc.org/wiki/geyser/packs/
Rainbow Wiki	https://geysermc.org/wiki/other/rainbow/
Rainbow Download	https://geysermc.org/download/?project=other-projects&rainbow=expanded
Hydraulic Wiki	https://geysermc.org/wiki/other/hydraulic/
Thunder Wiki	https://geysermc.org/wiki/other/thunder/
PackConverter GitHub	https://github.com/GeyserMC/PackConverter
Example Geyser Mappings	https://github.com/eclipseisoffline/geyser-example-mappings/
Chunky (Hangar)	https://hangar.papermc.io/pop4959/Chunky/versions/1.4.40
SkinsRestorer (Hangar)	https://hangar.papermc.io/SRTeam/SkinsRestorer
Minecraft Spear Wiki	https://minecraft.wiki/w/Spear
Bedrock Resource Pack Dev	https://wiki.bedrock.dev/guide/project-setup.html
Bedrock Custom Item Textures	https://wiki.bedrock.dev/items/items-intro#applying-textures
SpigotMC Wolf Armor Thread	https://www.spigotmc.org/threads/checking-if-a-wolf-has-armor.681502/
21. STEP-BY-STEP PHASE PLAN
Phase 1 — Compilation Fixes: DONE. Fixed 6 files. Migrated to AnvilView API, added spear enchantments.

Phase 1.5 — Battle-for-All Refactor: DONE. Created 4 new Battle managers, modified SuperToolManager/RecipeManager/JGlimsPlugin/WeaponMasteryManager.

Phase 2 — Missing Systems & Bug Fixes: IN PROGRESS.

 Implement BestBuddiesListener.java (wolf 95% DR, 0 dmg, Regen II)
 Implement all 10 weapon abilities (Bow, Crossbow, Trident, Mace complete)
 Fix Sound.BLOCK_LIGHTNING_ROD_TOGGLE_ON → Sound.ENTITY_LIGHTNING_BOLT_IMPACT
 Reduce all weapon ability cooldowns (see Section 10)
 Register BestBuddiesListener in JGlimsPlugin.java
 Add Efficiency auto-apply for Super Battle Shovels (I/II/III by tier) — verify this works
 Compile: .\gradlew.bat clean jar → BUILD SUCCESSFUL
Phase 3 — Recipe System Testing:

Verify all 10 battle recipes work in-game (one per weapon type)
Verify all super recipes require battle PDC key (vanilla item → super must fail)
Verify sequential upgrade chain: Battle → Super Iron → Super Diamond → Super Netherite
Test enchantment preservation through all upgrade steps
Verify spear recipes specifically (new in v1.3.0)
Phase 4 — Super Tool System Polish:

Enforce material-matching for tiered weapons (e.g., diamond battle sword + 8 iron → super iron sword must fail if material mismatch)
Implement "+2% damage per enchantment" for Netherite Definitive
Implement "no enchantment limit" for Netherite Definitive
Implement "no enchantment conflicts" for Netherite Definitive
Verify SuperToolManager auto-applies Efficiency to super shovels
Phase 5 — Ability System Completion:

Test all 20 abilities in-game (10 Diamond + 10 Netherite)
Verify Ender Dragon damage reduction (30%) works
Verify ore-detect radii match config
Enhanced definitive particle effects (polish visuals)
Verify ability only activates with proper super tier
Phase 6 — Quality & Balance:

Axe nerf verification (attack speed 0.5)
Lore consistency review across all weapon types
Cross-material prevention (can't mix materials in upgrades)
Full enchantment preservation audit
Soulbound interaction with super weapons
Phase 7 — Testing & Integration:

Deploy to creative server
Full regression test: every recipe, every enchantment, every mob system, every ability
TPS verification under load
Memory profiling (stay under 6 GB)
Phase 8 — Go Live:

Delete creative world, recreate survival
Install SkinsRestorer
Pre-generate chunks with Chunky (chunky start, radius 5000)
Final deploy and launch
Phase 9 — Enchantment Expansion:

Add 12 new enchantments to EnchantmentType enum
Add to AnvilRecipeListener (12 new recipes)
Implement in EnchantmentEffectListener (12 new behaviors)
Add new items (Warden's Echo, Elemental Arrows, Guild Banners, etc.)
Test all new content
Phase 10 — Custom Textures (Final Phase):

Create Java resource pack with custom item models
Add item_model component to all custom weapons in plugin code
Create Geyser mapping JSON for Bedrock definitions
Create Bedrock resource pack with matching textures
Use Rainbow to validate and auto-generate packs
Deploy packs, enable enable-custom-content: true in Geyser
Test on both Java and Bedrock clients
22. INSTRUCTIONS FOR THE NEXT CHAT
When continuing this project in a new chat session, provide this entire document and follow these rules:

Always start by reading the GitHub repository: Fetch the latest source from https://github.com/JGlims/JGlimsPlugin and check every file before making changes. The repo is the source of truth. Use raw.githubusercontent.com URLs to fetch individual files.

Always provide complete file replacements: Never provide partial snippets or "add this here." Every file must be a complete copy-paste replacement with the full file path. Example: src/main/java/com/jglims/plugin/weapons/WeaponAbilityListener.java followed by the entire file content.

Include full file paths for every file: e.g., src/main/java/com/jglims/plugin/weapons/BattleSwordManager.java.

Work in phases: Follow the phase plan (Phase 2 completion is next — apply the 3 pending fixes). Complete one phase at a time. Verify build success before proceeding.

Check the PaperMC 1.21.11 API: Use https://jd.papermc.io/paper/1.21.11/ for API verification. Use AnvilView (not deprecated AnvilInventory), Adventure components (not legacy ChatColor), EquipmentSlotGroup for AttributeModifiers, PersistentDataContainer for all custom data.

Maintain backward compatibility: Never break existing PDC data on items players already have.

Performance matters: TPS 20 target. Avoid heavy tick-frequency operations. Use player-specific spawnParticle to reduce network load. Ore-detect scans should only run on activation, not continuously.

The uniform rule: ALL weapons require Battle before they can become Super. Only Elytra and Shield bypass this. SuperToolManager.isBattleItem() is the gatekeeper. Never allow vanilla items to become Super directly.

Deliver files in dependency order: ConfigManager → Enchantment system (EnchantmentType, CustomEnchantManager) → Weapon managers → SuperToolManager → RecipeManager → JGlimsPlugin → Listeners.

Test the build after every phase: .\gradlew.bat clean jar must produce BUILD SUCCESSFUL with 0 errors before moving to the next phase.

Deep think before coding: Before writing any file, read the existing version on GitHub, understand all dependencies, plan the changes, then deliver. Explain the reasoning behind every change.

Be complete and explicit: When creating a new system, explain what events to listen for, what PDC keys to check, what the behavior should be, and deliver the full file ready to compile.

Sound constants: Always verify Sound constants exist in the PaperMC 1.21.11 Javadoc before using them. Sound is an interface, not an enum. Use https://jd.papermc.io/paper/1.21.11/org/bukkit/Sound.html to check.

ConfigManager getters: Always check the exact getter name exists in ConfigManager.java before calling it. Common mistakes: getEnderDragonDamageReduction() (WRONG) vs getEnderDragonAbilityDamageReduction() (CORRECT), getDogArmorDamageReduction() vs getDogArmorReduction() (alias, both work).

Constructor signatures matter: BattleShovelManager(JGlimsPlugin, ConfigManager), SpearManager(JGlimsPlugin, ConfigManager), WeaponAbilityListener(JGlimsPlugin, ConfigManager, CustomEnchantManager, SuperToolManager, SpearManager, BattleShovelManager), BestBuddiesListener(JGlimsPlugin, ConfigManager). All other battle managers take just (JGlimsPlugin).

This document captures the complete state of JGlimsPlugin v1.3.0 as of March 4, 2026. Phase 2 has 3 pending fixes. After applying them, the build will be green. The future vision extends through enchantment expansion (Phase 9) and custom crossplay textures via GeyserMC (Phase 10).










JGLIMSPLUGIN — COMPLETE DEFINITIVE REFERENCE DOCUMENT
Version: 1.3.0 (deployed and running) Author: JGlims Repository: https://github.com/JGlims/JGlimsPlugin Date: March 4, 2026 Build Status: BUILD SUCCESSFUL Current Phase: Phase 3 — In-Game Testing Server Status: LIVE — JGlimsPlugin v1.3.0 enabled in 949ms

TABLE OF CONTENTS
Server Infrastructure
Build & Deploy Workflow
Docker Commands
build.gradle (Full)
plugin.yml (Full)
config.yml (Full)
Complete File Inventory & Package Structure
Uniform Weapon Progression System
All 52 Custom Enchantments (+ Planned Expansions)
Weapon Abilities (Updated Reduced Cooldowns)
Mob Difficulty, Bosses, Events
Additional Systems (Guilds, Blessings, Loot, Mastery, etc.)
Configuration Defaults (All Fields)
Commands
All PDC Keys
API Notes for PaperMC 1.21.11
What Was Done (Complete History of All Changes)
Known Issues & Observations
Future: New Enchantments, Items & Ideas (Phase 9+)
Future: Custom Textures via GeyserMC + Rainbow (Final Phase)
Key Links
Step-by-Step Phase Plan
Instructions for the Next Chat
1. SERVER INFRASTRUCTURE
Hardware: OCI VM — 4 OCPUs, 24 GB system RAM, 47 GB boot disk, Ubuntu.

Docker container: mc-crossplay running itzg/minecraft-server:latest.

Key environment variables: EULA=TRUE, TYPE=PAPER, VERSION=1.21.11, MEMORY=8G, ONLINE_MODE=false, RCON_PASSWORD=JGlims2026Rcon, PLUGINS pointing to Geyser-Spigot and Floodgate-Spigot auto-downloads from download.geysermc.org.

Ports: 25565/TCP (Java), 19132/UDP (Bedrock via Geyser), 25575/TCP (RCON).

Installed plugins: JGlimsPlugin v1.3.0 (our plugin), Geyser-Spigot v2.9.4-SNAPSHOT, Floodgate v2.2.5-SNAPSHOT (b130-d73192f), Chunky v1.4.40, SkinsRestorer v15.10.1.

World data: ~/minecraft-server/data/ with world, world_nether, world_the_end.

Performance target: TPS 20 (100 FPS equivalent), stay under 6 GB of the 8 GB heap. Never break farms; only alter health/damage/speed on mobs; use lightweight listeners. Use player-specific spawnParticle to reduce network load.

2. BUILD & DEPLOY WORKFLOW
Build (Windows):

.\gradlew.bat clean jar
Output JAR: build/libs/JGlimsPlugin-1.3.0.jar

Build (Linux/Mac):

./gradlew clean jar
Deploy:

Copyscp build/libs/JGlimsPlugin-1.3.0.jar MinecraftServer:~/
ssh MinecraftServer
sudo rm -f ~/minecraft-server/data/plugins/JGlimsPlugin-*.jar
sudo rm -rf ~/minecraft-server/data/plugins/.paper-remapped/
sudo cp ~/JGlimsPlugin-1.3.0.jar ~/minecraft-server/data/plugins/
docker restart mc-crossplay
docker logs mc-crossplay 2>&1 | grep -i jglims
IMPORTANT: Always delete the .paper-remapped/ cache folder when replacing plugin JARs. Paper caches remapped versions there. If the old cached JAR remains alongside the new one, Paper will throw an "Ambiguous plugin name" error and load the wrong version.

Build configuration: Java 21 toolchain, io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT as compileOnly, Maven Central + PaperMC repo (https://repo.papermc.io/repository/maven-public/), reproducible build, base name JGlimsPlugin.

3. DOCKER COMMANDS
Creative test server:

Copydocker run -d --name mc-crossplay \
  -p 25565:25565 -p 19132:19132/udp -p 25575:25575 \
  -e EULA=TRUE -e TYPE=PAPER -e VERSION=1.21.11 \
  -e MEMORY=8G -e ONLINE_MODE=false -e GAMEMODE=creative -e DIFFICULTY=hard \
  -e RCON_PASSWORD=JGlims2026Rcon \
  -e PLUGINS="https://download.geysermc.org/v2/projects/geyser/versions/latest/builds/latest/downloads/spigot,https://download.geysermc.org/v2/projects/floodgate/versions/latest/builds/latest/downloads/spigot" \
  -v ~/minecraft-server/data:/data \
  itzg/minecraft-server:latest
Switch to survival:

Copydocker rm -f mc-crossplay
# Optionally delete creative world:
# rm -rf ~/minecraft-server/data/world ~/minecraft-server/data/world_nether ~/minecraft-server/data/world_the_end
docker run -d --name mc-crossplay \
  -p 25565:25565 -p 19132:19132/udp -p 25575:25575 \
  -e EULA=TRUE -e TYPE=PAPER -e VERSION=1.21.11 \
  -e MEMORY=8G -e ONLINE_MODE=false -e GAMEMODE=survival -e DIFFICULTY=hard \
  -e RCON_PASSWORD=JGlims2026Rcon \
  -e PLUGINS="https://download.geysermc.org/v2/projects/geyser/versions/latest/builds/latest/downloads/spigot,https://download.geysermc.org/v2/projects/floodgate/versions/latest/builds/latest/downloads/spigot" \
  -v ~/minecraft-server/data:/data \
  itzg/minecraft-server:latest
Useful Docker commands:

Copydocker logs mc-crossplay --tail 100              # View recent logs
docker logs mc-crossplay 2>&1 | grep -i jglims   # Check plugin loaded
docker exec -i mc-crossplay rcon-cli              # Enter RCON console
docker exec mc-crossplay mc-health                # Check server health
docker stats mc-crossplay                         # Live resource usage
docker restart mc-crossplay                       # Restart server
docker rm -f mc-crossplay                         # Remove container
4. build.gradle (FULL FILE)
Copyplugins {
    id 'java'
}

group = 'com.jglims.plugin'
version = '1.3.0'

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
    maven {
        name = "papermc"
        url = "https://repo.papermc.io/repository/maven-public/"
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
}

tasks.jar {
    archiveBaseName.set('JGlimsPlugin')
}
5. plugin.yml (FULL FILE)
Copyname: JGlimsPlugin
version: 1.3.0
main: com.jglims.plugin.JGlimsPlugin
api-version: 1.21
author: JGlims
description: >-
  Custom enchantments, battle axes, battle bows, battle maces, super tools,
  sickles, blessings, mob difficulty, inventory sorting, weapon mastery,
  blood moon events, guilds, and much more.

commands:
  jglims:
    description: JGlimsPlugin admin commands
    usage: /jglims <reload|stats|enchants|sort|mastery>
    permission: jglims.admin
  guild:
    description: Guild management commands
    usage: /guild <create|invite|join|leave|kick|disband|info|list>

permissions:
  jglims.admin:
    description: Access to all JGlimsPlugin admin commands
    default: op
6. config.yml (FULL FILE)
Copy# ==============================================
# JGlimsPlugin v1.3.0 Configuration
# ==============================================

# --- Mob Difficulty ---
mob-difficulty:
  enabled: true
  baseline-health-multiplier: 1.0
  baseline-damage-multiplier: 1.0
  distance:
    350:
      health: 1.5
      damage: 1.3
    700:
      health: 2.0
      damage: 1.6
    1000:
      health: 2.5
      damage: 1.9
    2000:
      health: 3.0
      damage: 2.2
    3000:
      health: 3.5
      damage: 2.5
    5000:
      health: 4.0
      damage: 3.0
  biome:
    pale-garden: 2.0
    deep-dark: 2.5
    swamp: 1.4
    nether-wastes:
      health: 1.7
      damage: 1.7
    soul-sand-valley:
      health: 1.9
      damage: 1.9
    crimson-forest:
      health: 2.0
      damage: 2.0
    warped-forest:
      health: 2.0
      damage: 2.0
    basalt-deltas:
      health: 2.3
      damage: 2.3
    end:
      health: 2.5
      damage: 2.0

# --- Boss Enhancer ---
boss-enhancer:
  ender-dragon:
    health: 3.5
    damage: 3.0
  wither:
    health: 1.0
    damage: 1.0
  warden:
    health: 1.0
    damage: 1.0
  elder-guardian:
    health: 2.5
    damage: 1.8

# --- Creeper Reduction ---
creeper-reduction:
  enabled: true
  cancel-chance: 0.5

# --- Pale Garden Fog ---
pale-garden-fog:
  enabled: true
  check-interval: 40

# --- Loot Booster ---
loot-booster:
  enabled: true
  chest-enchanted-book: true
  guardian-shards-min: 1
  guardian-shards-max: 3
  elder-guardian-shards-min: 3
  elder-guardian-shards-max: 5
  ghast-tears-min: 1
  ghast-tears-max: 2
  echo-shard-chance: 0.40

# --- Mob Book Drops ---
mob-book-drops:
  enabled: true
  hostile-chance: 0.05
  boss-custom-chance: 0.15
  looting-bonus-regular: 0.02
  looting-bonus-boss: 0.05

# --- Blessings ---
blessings:
  c-bless:
    max-uses: 10
    heal-per-use: 1
  ami-bless:
    max-uses: 10
    damage-percent-per-use: 2.0
  la-bless:
    max-uses: 10
    defense-percent-per-use: 2.0

# --- Anvil ---
anvil:
  remove-too-expensive: true
  xp-cost-reduction: 0.5

# --- Toggles ---
toggles:
  inventory-sort: true
  enchant-transfer: true
  sickle: true
  battle-axe: true
  battle-bow: true
  battle-mace: true
  battle-shovel: true
  super-tools: true
  drop-rate-booster: true
  spear: true

# --- Drop Rate Booster ---
drop-rate-booster:
  trident-drop-chance: 0.35
  breeze-wind-charge-min: 2
  breeze-wind-charge-max: 5

# --- Villager Trades ---
villager-trades:
  enabled: true
  price-reduction: 0.50
  disable-trade-locking: true

# --- King Mob ---
king-mob:
  enabled: true
  spawns-per-king: 500
  health-multiplier: 10.0
  damage-multiplier: 3.0
  diamond-drop-min: 3
  diamond-drop-max: 9

# --- Axe Nerf ---
axe-nerf:
  enabled: true
  attack-speed: 0.5

# --- Weapon Mastery ---
weapon-mastery:
  enabled: true
  max-kills: 1000
  max-bonus-percent: 10.0

# --- Blood Moon ---
blood-moon:
  enabled: true
  check-interval: 100
  chance: 0.15
  mob-health-multiplier: 1.5
  mob-damage-multiplier: 1.3
  boss-every-nth: 10
  boss-health-multiplier: 20.0
  boss-damage-multiplier: 5.0
  boss-diamond-min: 5
  boss-diamond-max: 15
  double-drops: true

# --- Guilds ---
guilds:
  enabled: true
  max-members: 10
  friendly-fire: false

# --- Best Buddies (Dog Armor) ---
best-buddies:
  dog-armor-damage-reduction: 0.95

# --- Super Tools ---
super-tools:
  iron-bonus-damage: 1.0
  diamond-bonus-damage: 2.0
  netherite-bonus-damage: 2.0
  netherite-per-enchant-bonus-percent: 2.0

# --- Ore Detect (Super Pickaxe Ability) ---
ore-detect:
  radius-diamond: 8
  radius-netherite: 12
  ancient-debris-radius-diamond: 24
  ancient-debris-radius-netherite: 40
  duration-ticks: 200

# --- Weapon Abilities ---
weapon-abilities:
  ender-dragon-damage-reduction: 0.30
  netherite-enchant-bonus-percent: 2.0
Copy
7. COMPLETE FILE INVENTORY & PACKAGE STRUCTURE
All source: src/main/java/com/jglims/plugin/. Resources: src/main/resources/.

Package: com.jglims.plugin (root)
JGlimsPlugin.java — Main class. onEnable initializes ALL managers in dependency order, registers ALL listeners (including BestBuddiesListener as of v1.3.0 Phase 2), starts scheduled tasks. onCommand handles /guild (8 subcommands) and /jglims (5 subcommands). Accessor methods for every manager.

Package: com.jglims.plugin.config
ConfigManager.java — Loads config.yml with defaults for every system. Provides 100+ primary getters and backward-compatibility alias methods. Constructor: (JavaPlugin). Method: loadConfig(). Key getters: getEnderDragonAbilityDamageReduction() (NOT getEnderDragonDamageReduction), getDogArmorReduction(), getOreDetectRadiusDiamond(), getOreDetectRadiusNetherite(), getOreDetectAncientDebrisRadiusDiamond(), getOreDetectAncientDebrisRadiusNetherite(), getOreDetectDurationTicks(), getSuperIronBonusDamage(), getSuperDiamondBonusDamage(), getSuperNetheriteBonusDamage(), getSuperNethPerEnchantBonus(), getPaleGardenFogCheckInterval() / getPaleGardenFogInterval() (alias), and many more.

Package: com.jglims.plugin.enchantments
EnchantmentType.java — Enum of all 52 custom enchantments with getMaxLevel(). Categories: Sword (5), Axe (5), Pickaxe (5), Shovel (1), Hoe/Sickle (3), Bow (4), Crossbow (2), Trident (5), Armor (9), Elytra (4), Mace (4), Spear (3), Universal (2).

CustomEnchantManager.java — Registry of NamespacedKeys, conflict map, get/set/remove/list/copy enchantments via PersistentDataContainer. Used by all listeners that read enchantment data.

AnvilRecipeListener.java — 52 anvil recipes (server reports 50 registered — may need investigation). Uses AnvilView API (not deprecated AnvilInventory). Handles soulbound, book creation, book application, conflict checks, XP cost reduction, "Too Expensive" removal. Includes v1.3.0 spear enchantments (IMPALING_THRUST, EXTENDED_REACH, SKEWERING).

EnchantmentEffectListener.java — ALL enchantment behaviors: damage-event handlers, defender handlers, projectile handlers, block-break handlers, interact handlers, movement/passive handlers. Implements every enchantment's actual gameplay effect.

SoulboundListener.java — Keep-on-death for SOULBOUND items, lostsoul conversion (item drops with glow), one-per-inventory enforcement. Uses AnvilView API.

Package: com.jglims.plugin.weapons
BattleSwordManager.java — NEW v1.3.0. Battle Swords (+1 dmg, 1.6 speed). PDC: is_battle_sword (BYTE).

BattlePickaxeManager.java — NEW v1.3.0. Battle Pickaxes (+1 dmg, retains mining speed). PDC: is_battle_pickaxe (BYTE).

BattleTridentManager.java — NEW v1.3.0. Battle Trident (10 dmg, 1.1 speed). PDC: is_battle_trident (BYTE).

BattleSpearManager.java — NEW v1.3.0. Battle Spears (+1 dmg per tier). PDC: is_battle_spear (BYTE).

BattleAxeManager.java — Existing. Battle Axes (sickle dmg + 1, 0.9 speed). PDC: is_battle_axe (BYTE). Blocks stripping.

BattleBowManager.java — Existing. Battle Bow/Crossbow. PDC: is_battle_bow (BYTE), is_battle_crossbow (BYTE).

BattleMaceManager.java — Existing. Battle Mace (12 dmg, 0.7 speed). PDC: is_battle_mace (BYTE).

BattleShovelManager.java — Existing v1.3.0. Battle Shovels (+1.5 dmg, 1.2 speed). PDC: battle_shovel (BOOLEAN). Blocks path-making. Constructor: (JGlimsPlugin, ConfigManager).

SickleManager.java — Existing. Sickles from hoes (sword dmg + 1, 1.1 speed). PDC: is_sickle (BYTE). Blocks tilling.

SpearManager.java — Existing v1.3.0. Super spear tiers via PDC super_spear_tier (INTEGER 1/2/3). SpearTier enum. Constructor: (JGlimsPlugin, ConfigManager). Method: getSuperTierKey() returns the NamespacedKey.

SuperToolManager.java — MODIFIED v1.3.0. 3-tier super system (Iron=1, Diamond=2, Netherite=3). isBattleItem() checks all 10 PDC keys. createSuperTool() requires battle item (gate). Lore format: Attack Damage: [base] +[battle] +[super] = [total]. Preserves enchantments through upgrades. Auto-applies Efficiency (I/II/III by tier) to super shovels.

WeaponAbilityListener.java — MODIFIED v1.3.0 Phase 2. Right-click abilities for Diamond(tier≥2)/Netherite(tier≥3) super tools. All 10 weapon classes implemented with complete abilities. Constructor: (JGlimsPlugin, ConfigManager, CustomEnchantManager, SuperToolManager, SpearManager, BattleShovelManager). Phase 2 fixes applied: Sound.ENTITY_LIGHTNING_BOLT_IMPACT (replacing non-existent BLOCK_LIGHTNING_ROD_TOGGLE_ON), Particle.CRIT (replacing non-existent CRIT_MAGIC), all reduced cooldowns.

WeaponMasteryManager.java — MODIFIED v1.3.0. 10 weapon classes (sword, axe, pickaxe, shovel, sickle, bow, crossbow, trident, mace, spear). Linear scaling: kills/1000 = up to 10% max damage bonus. PDC keys: mastery_sword through mastery_spear (INTEGER), mastery_damage (AttributeModifier key).

Package: com.jglims.plugin.crafting
RecipeManager.java — MODIFIED v1.3.0. 12-arg constructor: (JGlimsPlugin, SickleManager, BattleAxeManager, BattleBowManager, BattleMaceManager, SuperToolManager, BattleSwordManager, BattlePickaxeManager, BattleTridentManager, BattleSpearManager, BattleShovelManager, SpearManager). Registers ALL battle + super recipes. PDC guard blocks vanilla→Super (must be battle first).

VanillaRecipeRemover.java — Removes conflicting vanilla recipes. Static method: remove(JavaPlugin).

Package: com.jglims.plugin.mobs
MobDifficultyManager.java — Distance + biome mob scaling. Reads all distance/biome values from ConfigManager. Implements Listener.

BossEnhancer.java — Boss health/damage multipliers. Dragon 3.5×/3.0×, Elder Guardian 2.5×/1.8×, Wither/Warden vanilla. Constructor: (JGlimsPlugin, ConfigManager).

KingMobManager.java — Every 500 hostile mob spawns → King variant (10× HP, 3× damage, gold glowing, no despawn, one per world, drops 3-9 diamonds). Constructor: (JGlimsPlugin, ConfigManager).

BloodMoonManager.java — 15% chance each night. 1.5×/1.3× mob multipliers, Darkness + red particles, double drops/XP. Every 10th Blood Moon: Blood Moon King (Zombie, diamond armor, Netherite sword, 400 HP, 5× dmg, drops 5-15 diamonds + netherite scrap). Constructor: (JGlimsPlugin, ConfigManager). Method: startScheduler().

Package: com.jglims.plugin.guilds
GuildManager.java — CRUD operations: createGuild, invitePlayer, joinGuild, leaveGuild, kickPlayer, disbandGuild, showGuildInfo, listGuilds. Persists to guilds.yml. Max 10 members. Constructor: (JGlimsPlugin, ConfigManager).

GuildListener.java — Friendly fire prevention between guild members. Constructor: (JGlimsPlugin, GuildManager).

Package: com.jglims.plugin.blessings
BlessingManager.java — 3 blessings: C's (health +1 heart/use), Ami's (damage +2%/use), La's (armor +2%/use). Permanent via AttributeModifiers. Method: showStats(CommandSender, Player). Constructor: (JGlimsPlugin).

BlessingListener.java — Right-click consumption of blessing items, PDC persistence, join/respawn reapply of permanent buffs. Constructor: (JGlimsPlugin, BlessingManager).

Package: com.jglims.plugin.effects
PaleGardenFogTask.java — Every N ticks (default 40), applies Darkness to players in Pale Garden biome. Constructor: (JGlimsPlugin). Method: start(int intervalTicks).

Package: com.jglims.plugin.utility
BestBuddiesListener.java — v1.3.0 Phase 2. BEST_BUDDIES enchantment on wolf armor. 95% incoming damage reduction (configurable), 0 melee damage (pacifist wolf), permanent Regeneration II, heart particles. Uses wolf.getEquipment().getItem(EquipmentSlot.BODY) for armor detection. Handles chunk load persistence, global scan task every 10 seconds. Constructor: (JGlimsPlugin, ConfigManager). NOW REGISTERED in JGlimsPlugin.java (Phase 2 Fix #3).

InventorySortListener.java — Mouse Tweaks-style shift-click sorting. Updated for all 10 weapon categories. Constructor: (JGlimsPlugin).

EnchantTransferListener.java — Enchanted item + plain book in anvil → enchanted book with all enchantments, 0 XP cost. Uses AnvilView. Constructor: (JGlimsPlugin, CustomEnchantManager).

LootBoosterListener.java — Enchanted books in chest loot. Mob drops: hostile 5%, boss 15% (+looting bonus). Guardian prismarine shards 1-3, Elder Guardian 3-5, echo shard chance 40%. Constructor: (JGlimsPlugin, ConfigManager).

DropRateListener.java — Trident 35% from Drowned, Breeze 2-5 wind charges. Constructor: (JGlimsPlugin, ConfigManager).

VillagerTradeListener.java — 50% price reduction, trade locking disabled. Constructor: (JGlimsPlugin, ConfigManager).

Resources: src/main/resources/
plugin.yml — See Section 5. config.yml — See Section 6.

8. UNIFORM WEAPON PROGRESSION SYSTEM (v1.3.0)
THE RULE: ALL weapons must become Battle before they can become Super. Vanilla → Battle (+1 dmg) → Super Iron (Battle +1) → Super Diamond (Battle +2, unlocks Diamond ability) → Super Netherite (Battle Definitive +3, unlocks Netherite ability). Exceptions: Elytra and Shield bypass Battle, go directly to Super.

Battle Conversion Details:

Weapon	Vanilla Dmg	Battle Dmg	Battle Speed	Recipe
Sword	4-8 (by material)	5-9	1.6	8 matching material + sword center
Pickaxe	2-6	3-7	1.2	8 matching material + pickaxe center
Axe	special formula	+1 over sickle	0.9	ingredient + block + axe (special pattern)
Shovel	2.5-6.5	4-8	1.2	8 matching material + shovel center
Hoe→Sickle	1	5-9 (sword dmg +1)	1.1	8 matching material + hoe center
Bow	N/A (ranged)	N/A	N/A	8 iron ingots + bow center
Crossbow	N/A (ranged)	N/A	N/A	8 iron ingots + crossbow center
Mace	11	12	0.7	8 breeze rods + mace center
Trident	9	10	1.1	8 prismarine shards + trident center
Spear	1-5 (by material)	2-6	0.87-1.54	8 matching material + spear center
Super Tier Bonuses:

Tier	PDC Value	Bonus Damage	Ability	Special Rules
Iron	1	+1	None	—
Diamond	2	+2 (non-neth) / +1 (neth base)	Right-click Diamond ability	—
Netherite	3	+3 (non-neth) / +2 (neth base)	Right-click Definitive ability	No enchant conflicts, +2% dmg/enchant, no enchant limit
Trident exception: Diamond +3, Netherite +5.

Lore format: Attack Damage: [vanilla base] +[battle +1] +[super bonus] = [total]

Super recipe: Super Iron + 8 diamonds → Super Diamond. Super Diamond + 8 netherite ingots → Super Netherite. All upgrades preserve existing enchantments.

Gatekeeper: SuperToolManager.isBattleItem(ItemStack) checks all 10 PDC keys (is_battle_sword, is_battle_axe, is_battle_pickaxe, is_sickle, battle_shovel, is_battle_bow, is_battle_crossbow, is_battle_mace, is_battle_trident, is_battle_spear). Items without a battle PDC key cannot become Super.

9. ALL 52 CUSTOM ENCHANTMENTS (+ PLANNED EXPANSIONS)
Current Enchantments (52):
Sword (5): VAMPIRISM(5)↔LIFESTEAL(3) conflict, BLEED(3)↔VENOMSTRIKE(3) conflict, CHAIN_LIGHTNING(3)↔WITHER_TOUCH conflict

Axe (5): BERSERKER(5)↔BLOOD_PRICE conflict, LUMBERJACK(3), CLEAVE(3), TIMBER(3), GUILLOTINE(3)

Pickaxe (5): VEINMINER(3)↔DRILL(3) conflict, AUTO_SMELT(1), MAGNETISM(1), EXCAVATOR(3)

Shovel (1): HARVESTER(3)

Hoe/Sickle (3): GREEN_THUMB(1), REPLENISH(1), HARVESTING_MOON(3)

Bow (4): EXPLOSIVE_ARROW(3)↔HOMING(3) conflict, RAPIDFIRE(3), SNIPER(3)

Crossbow (2): THUNDERLORD(5), TIDAL_WAVE(3)

Trident (5): FROSTBITE(3), SOUL_REAP(3), BLOOD_PRICE(3)↔BERSERKER conflict, REAPERS_MARK(3), WITHER_TOUCH(3)↔CHAIN_LIGHTNING conflict

Armor (9): SWIFTNESS(3), VITALITY(3), AQUA_LUNGS(3), NIGHT_VISION(1), FORTIFICATION(3), DEFLECTION(3), SWIFTFOOT(3)↔MOMENTUM conflict, DODGE(3), LEAPING(3)

Elytra (4): BOOST(3), CUSHION(1), GLIDER(1), STOMP(3)

Mace (4): SEISMIC_SLAM(3)↔MAGNETIZE(3) conflict, GRAVITY_WELL(3), MOMENTUM(3)↔SWIFTFOOT conflict

Spear (3) — NEW v1.3.0: IMPALING_THRUST(3), EXTENDED_REACH(3), SKEWERING(3) — conflict triangle (each conflicts with the other two)

Universal (2): SOULBOUND(1), BEST_BUDDIES(1)

PLANNED NEW ENCHANTMENTS (Phase 9+) — 12 total:
Sickle (3): SOUL_HARVEST(3) — Wither I-III on hit + Regen I-III for player while Wither active, conflicts GREEN_THUMB. REAPING_CURSE(3) — lingering Instant Damage cloud at corpse on kill, conflicts REPLENISH. CROP_REAPER(1) — kills have 15%+5%/Looting chance to drop seeds/wheat/carrots/potatoes.

Shovel (2): BURIAL(3) — Slowness III + Blindness I for 1-3s on hit, conflicts HARVESTER. EARTHSHATTER(3) — breaking a block sends shockwave breaking identical blocks in 1-3 radius.

Sword (1): FROSTBITE_BLADE(3) — Slowness I-III + freeze visual on hit, conflicts VENOMSTRIKE.

Axe (1): WRATH(3) — consecutive hits within 3s on same target: +10/20/30% damage, resets on target switch, conflicts CLEAVE.

Pickaxe (1): PROSPECTOR(3) — 5/10/15% chance double ore yield (stacks with Fortune), conflicts AUTO_SMELT.

Trident (1): TSUNAMI(3) — thrown hit creates water burst pushing mobs 3/5/7 blocks + Slowness I 2s (rain/water only), conflicts FROSTBITE.

Bow (1): FROSTBITE_ARROW(3) — arrows apply Slowness I-III + extinguish fire, conflicts EXPLOSIVE_ARROW.

Mace (1): TREMOR(3) — on hit, mobs within 2/4/6 blocks get Mining Fatigue II for 3s, conflicts GRAVITY_WELL.

Spear (1): PHANTOM_PIERCE(3) — charged spear attacks pierce 1/2/3 targets dealing full damage, conflicts SKEWERING.

10. WEAPON ABILITIES — ALL REDUCED COOLDOWNS (v1.3.0 Phase 2)
All abilities require Super Diamond (tier ≥ 2) or Super Netherite (tier ≥ 3). Activated by right-click with the super weapon in main hand.

Weapon	Diamond Ability	CD	Damage	Netherite Ability	CD	Damage
Sword	Dash Strike	6s	8/tick (dash)	Dimensional Cleave	12s	15/tick + 25 final (12-block rift AoE)
Axe	Bloodthirst	8s	buff: 5s lifesteal + Haste + Strength	Ragnarok Cleave	15s	20/16/12 (3 expanding shockwaves)
Pickaxe	Ore Pulse	10s	none (detect ores 8-block, debris 24-block)	Seismic Resonance	18s	none (detect ores 12-block, debris 40-block)
Shovel	Earthen Wall	8s	6 + launch + Resistance II	Tectonic Upheaval	14s	14/wave + Resistance II + Slowness III
Sickle	Harvest Storm	6s	7 initial + 2×4 bleed (5-block AoE spin)	Reaper's Scythe	14s	10/tick + 1.5 lifesteal + Wither II (7-block dual arc)
Spear	Phantom Lunge	8s	8 piercing (8-block dash)	Spear of the Void	14s	18 pierce + 10 detonation (30-block projectile)
Bow	Arrow Storm	8s	6/arrow × 5 rapid-fire	Celestial Volley	14s	10/arrow × 12 from sky + 8 AoE impact
Crossbow	Chain Shot	8s	8 + 4 chain × 3 piercing bolts	Thunder Barrage	15s	12 + 14 AoE × 6 explosive bolts
Trident	Tidal Surge	8s	8 + water knockback + Slowness I	Poseidon's Wrath	14s	16/13/10 (3 water waves) + 10 lightning strike
Mace	Ground Slam	8s	10 + stun (Slowness III + Mining Fatigue)	Meteor Strike	15s	5-25 distance-based (launch up + slam AoE)
Ender Dragon exception: All Netherite Definitive abilities deal only 30% damage to the Ender Dragon. Controlled by config.getEnderDragonAbilityDamageReduction() (default 0.30). Diamond abilities are NOT reduced against the Dragon.

Bloodthirst special mechanic: When activated, stores bloodthirst_active LONG PDC key on the weapon with expiry timestamp. During the 5-second window, all melee hits heal the player.

Ore Pulse / Seismic Resonance: Uses player-specific spawnParticle for ore highlighting (only the caster sees the ore particles). Each ore type has a unique color: Coal(50,50,50), Iron(210,150,100), Copper(180,100,50), Gold(255,215,0), Redstone(255,0,0), Lapis(30,30,200), Emerald(0,200,50), Diamond(100,230,255), Ancient Debris(100,50,20) + flame particles, Nether Gold(255,200,50), Nether Quartz(240,240,230).

11. MOB DIFFICULTY, BOSSES, EVENTS
Baseline: 1.0× health, 1.0× damage (vanilla).

Distance scaling (Overworld only): 0-349 blocks = none. 350 = 1.5×/1.3×. 700 = 2.0×/1.6×. 1000 = 2.5×/1.9×. 2000 = 3.0×/2.2×. 3000 = 3.5×/2.5×. 5000+ = 4.0×/3.0×. Scaling is incremental between thresholds.

Biome multipliers (stack with distance): Pale Garden 2.0×, Deep Dark 2.5×, Swamp 1.4×, Nether Wastes 1.7×/1.7×, Soul Sand Valley 1.9×/1.9×, Crimson Forest 2.0×/2.0×, Warped Forest 2.0×/2.0×, Basalt Deltas 2.3×/2.3×, End 2.5×/2.0×.

Boss Enhancer: Ender Dragon 3.5×/3.0×, Elder Guardian 2.5×/1.8×, Wither = vanilla (1.0×/1.0×), Warden = vanilla (1.0×/1.0×).

Creeper reduction: 50% of creeper spawns are cancelled (cancel-chance: 0.5).

King Mob: Every 500 hostile mob spawns → one King variant. King stats: 10× health, 3× damage. Visual: gold glowing, no despawn. Drops: 3-9 diamonds. Limit: one per world at a time.

Blood Moon: 15% chance each night (checked every 100 ticks). During Blood Moon: 1.5×/1.3× all mob health/damage, Darkness effect on all players, red particles in the sky, double drops and XP from all mobs. Every 10th Blood Moon: Blood Moon King spawns — a Zombie with diamond armor, Netherite sword, 400 HP (20.0× base), 5.0× damage. Blood Moon King drops: 5-15 diamonds + netherite scrap on death.

12. ADDITIONAL SYSTEMS
Guilds: Full guild system. Commands: /guild create , invite , join, leave, kick , disband, info, list. Max 10 members per guild. Friendly fire disabled between guild members. Data persists to plugins/JGlimsPlugin/guilds.yml.

Blessings (3 types): C's Bless: 10 uses, +1 heart (2 HP) per use, permanent via AttributeModifier on MAX_HEALTH. Ami's Bless: 10 uses, +2% attack damage per use, permanent via AttributeModifier on ATTACK_DAMAGE. La's Bless: 10 uses, +2% armor per use, permanent via AttributeModifier on ARMOR. Right-click to consume. PDC tracks uses remaining. Effects reapply on join/respawn.

Best Buddies: Apply BEST_BUDDIES enchantment to Wolf Armor (via anvil). When equipped on a tamed wolf: wolf takes 95% less damage (configurable), wolf deals 0 melee damage (pacifist), wolf gets permanent Regeneration II, heart particles every 2 seconds. Wolf armor detected via wolf.getEquipment().getItem(EquipmentSlot.BODY). Effects persist across chunk loads and server restarts via PDC marker (best_buddies_applied BYTE) and global scan task.

Weapon Mastery: 10 weapon classes tracked separately. Linear scaling: (kills / 1000) × 10% = max 10% damage bonus at 1000 kills. PDC keys: mastery_sword through mastery_spear (INTEGER kill count). Damage bonus applied via mastery_damage AttributeModifier. View progress: /jglims mastery.

Inventory Sort: Shift-click any empty slot in a container inventory to sort all items.

Enchant Transfer: Enchanted item + plain Book in anvil → Enchanted Book with all enchantments, 0 XP cost.

Loot Booster: Chest loot: guaranteed enchanted book. Mob drops: hostile 5%, boss 15% (+looting bonus). Guardian: 1-3 prismarine shards extra. Elder Guardian: 3-5 shards. Ghast: 1-2 tears extra. Echo Shard: 40% from Warden.

Drop Rate Booster: Trident 35% from Drowned (vanilla 8.5%). Breeze 2-5 wind charges (vanilla 1-2).

Villager Trades: All prices reduced by 50%. Trade locking disabled.

Axe Nerf: All axes attack speed set to 0.5.

Pale Garden Fog: Darkness effect applied every 40 ticks to players in Pale Garden biome.

13. COMMANDS
/guild (Player-only)
Subcommand	Usage	Description
create	/guild create	Create a new guild
invite	/guild invite	Invite a player
join	/guild join	Accept pending invite
leave	/guild leave	Leave guild
kick	/guild kick	Kick member (leader only)
disband	/guild disband	Disband guild (leader only)
info	/guild info	Show guild details
list	/guild list	List all guilds
/jglims (Requires jglims.admin, default: op)
Subcommand	Usage	Description
reload	/jglims reload	Reload config.yml
stats	/jglims stats	Show blessing stats
enchants	/jglims enchants	List all 52 enchantments
sort	/jglims sort	Inventory sort info
mastery	/jglims mastery	Show weapon mastery (player-only)
14. ALL PDC KEYS
Key	Type	Used By	Purpose
is_battle_sword	BYTE	BattleSwordManager	Battle sword marker
is_battle_axe	BYTE	BattleAxeManager	Battle axe marker
is_battle_pickaxe	BYTE	BattlePickaxeManager	Battle pickaxe marker
is_sickle	BYTE	SickleManager	Sickle marker
battle_shovel	BOOLEAN	BattleShovelManager	Battle shovel marker
is_battle_bow	BYTE	BattleBowManager	Battle bow marker
is_battle_crossbow	BYTE	BattleBowManager	Battle crossbow marker
is_battle_mace	BYTE	BattleMaceManager	Battle mace marker
is_battle_trident	BYTE	BattleTridentManager	Battle trident marker
is_battle_spear	BYTE	BattleSpearManager	Battle spear marker
super_tool_tier	INTEGER (1/2/3)	SuperToolManager	Super tier
super_spear_tier	INTEGER (1/2/3)	SpearManager	Super spear tier
super_elytra_durability	INTEGER	SuperToolManager	Super elytra durability
boss_damage_mult	DOUBLE	BossEnhancer	Boss damage multiplier
mastery_sword	INTEGER	WeaponMasteryManager	Sword kill count
mastery_axe	INTEGER	WeaponMasteryManager	Axe kill count
mastery_pickaxe	INTEGER	WeaponMasteryManager	Pickaxe kill count
mastery_shovel	INTEGER	WeaponMasteryManager	Shovel kill count
mastery_sickle	INTEGER	WeaponMasteryManager	Sickle kill count
mastery_bow	INTEGER	WeaponMasteryManager	Bow kill count
mastery_crossbow	INTEGER	WeaponMasteryManager	Crossbow kill count
mastery_trident	INTEGER	WeaponMasteryManager	Trident kill count
mastery_mace	INTEGER	WeaponMasteryManager	Mace kill count
mastery_spear	INTEGER	WeaponMasteryManager	Spear kill count
mastery_damage	NamespacedKey	WeaponMasteryManager	Mastery damage modifier key
bloodthirst_active	LONG	WeaponAbilityListener	Bloodthirst expiry timestamp
best_buddies_applied	BYTE	BestBuddiesListener	Wolf Best Buddies marker
c_bless_item	BYTE	RecipeManager	C's Blessing item marker
ami_bless_item	BYTE	RecipeManager	Ami's Blessing item marker
la_bless_item	BYTE	RecipeManager	La's Blessing item marker
Per-EnchantmentType (52 keys)	INTEGER (level)	CustomEnchantManager	Enchantment level on item
15. API NOTES FOR PAPERMC 1.21.11
These are critical notes to avoid compilation errors:

Wolf armor detection: Use wolf.getEquipment().getItem(EquipmentSlot.BODY). The method getBodyArmor() does NOT exist on EntityEquipment. EquipmentSlot.BODY is valid for wolves, horses, happy ghasts, nautiluses.

Arrow lifetime: Use AbstractArrow.setLifetimeTicks(int). The method setLife(int) does NOT exist.

Sound constants: In PaperMC 1.21.11, Sound is an interface (not an enum). Sound.BLOCK_LIGHTNING_ROD_TOGGLE_ON does NOT exist — use Sound.ENTITY_LIGHTNING_BOLT_IMPACT. Confirmed sounds: ITEM_MACE_SMASH_GROUND, ITEM_MACE_SMASH_GROUND_HEAVY, ENTITY_WARDEN_SONIC_BOOM, ENTITY_WARDEN_EMERGE, ENTITY_WARDEN_HEARTBEAT, ITEM_TRIDENT_RIPTIDE_3, ITEM_TRIDENT_THUNDER, ENTITY_LIGHTNING_BOLT_THUNDER, ENTITY_BREEZE_SHOOT, ITEM_CROSSBOW_SHOOT, ENTITY_ELDER_GUARDIAN_CURSE, ENTITY_LIGHTNING_BOLT_IMPACT.

Particle constants: Particle.CRIT_MAGIC does NOT exist in 1.21.11. Use Particle.CRIT or Particle.ENCHANTED_HIT instead. Always verify particle names against the API before using them.

ConfigManager getters: Use config.getEnderDragonAbilityDamageReduction(). The method getEnderDragonDamageReduction() does NOT exist.

AnvilView API: Use AnvilView (from org.bukkit.inventory.view.AnvilView) for all anvil operations. Old AnvilInventory methods are deprecated.

AttributeModifiers: Use new AttributeModifier(NamespacedKey, amount, Operation). UUID-based constructor is deprecated.

Adventure API: Use net.kyori.adventure.text.Component for chat messages (not legacy ChatColor).

16. WHAT WAS DONE (Complete History of All Changes)
Phase 1 — Compilation Error Fixes (6 files modified):
ConfigManager.java — Added getPaleGardenFogInterval() alias, added new config fields for ore-detect / weapon-abilities / super-tools, added 20+ backward-compatibility alias getters
JGlimsPlugin.java — Fixed WeaponAbilityListener to use 6-arg constructor (added SpearManager + BattleShovelManager params)
AnvilRecipeListener.java — Migrated 7 deprecated AnvilInventory method calls to AnvilView API, added 3 spear enchantment recipes
SoulboundListener.java — Migrated 2 deprecated AnvilInventory calls to AnvilView
EnchantTransferListener.java — Migrated 1 deprecated AnvilInventory call to AnvilView
Phase 1.5 — Battle-for-All Refactor (8 files):
BattleSwordManager.java — NEW — created battle swords for all 5 material tiers
BattlePickaxeManager.java — NEW — created battle pickaxes for all 5 material tiers
BattleTridentManager.java — NEW — created battle trident
BattleSpearManager.java — NEW — created battle spears for all tiers
SuperToolManager.java — MODIFIED — added isBattleItem() checking all 10 PDC keys, battle-required gating
RecipeManager.java — MODIFIED — 12-arg constructor, all battle + super recipes, PDC guard
JGlimsPlugin.java — MODIFIED — 4 new manager fields, initialization, accessor methods
WeaponMasteryManager.java — MODIFIED — added spear mastery class
Both phases: BUILD SUCCESSFUL, 0 errors.

Phase 2 — Bug Fixes & Missing Systems (March 4, 2026):
BestBuddiesListener.java — Complete implementation (wolf 95% DR, 0 dmg, Regen II) — DONE
WeaponAbilityListener.java — Implemented all 10 weapon abilities (Bow, Crossbow, Trident, Mace complete) — DONE
WeaponAbilityListener.java — Fixed Sound.BLOCK_LIGHTNING_ROD_TOGGLE_ON → Sound.ENTITY_LIGHTNING_BOLT_IMPACT — DONE
WeaponAbilityListener.java — Fixed Particle.CRIT_MAGIC → Particle.CRIT (discovered during build) — DONE
WeaponAbilityListener.java — Applied all reduced cooldowns per Section 10 table — DONE
JGlimsPlugin.java — Added pm.registerEvents(new BestBuddiesListener(this, configManager), this); — DONE
Deployment — Resolved "Ambiguous plugin name" error by clearing .paper-remapped/ cache — DONE
Phase 2 Result: BUILD SUCCESSFUL. Server running v1.3.0. All systems enabled. Blood Moon triggered on first night.

17. KNOWN ISSUES & OBSERVATIONS
Anvil recipe count: Server logs "Registered 50 anvil enchantment recipes" — expected 52 (49 from v1.2.0 + 3 spear enchantments = 52). May indicate 2 spear recipes are not registering correctly, or the counter includes/excludes differently. Needs investigation during Phase 3 testing. Not a blocker.

Deprecation warnings: Build output shows "Some input files use or override a deprecated API" — this is non-critical, just informational. The deprecated methods are likely from legacy Bukkit APIs we haven't fully migrated yet.

Particle.CRIT_MAGIC: This was discovered as a compilation error during Phase 2. The particle was renamed in modern Paper. Fixed by using Particle.CRIT instead. Added to API notes for future reference.

Plugin permissions on server: Files in the Docker volume are owned by root. Always use sudo cp when deploying JARs to the plugins folder.

Paper remapping cache: When replacing plugin JARs, always delete ~/minecraft-server/data/plugins/.paper-remapped/ to prevent the "Ambiguous plugin name" conflict between the old cached JAR and the new JAR.

18. FUTURE: NEW ENCHANTMENTS, ITEMS & IDEAS (Phase 9+)
Planned New Items & Recipes:
Item	Recipe	Mechanic
Warden's Echo (accessory)	Echo Shard + Sculk Catalyst + Amethyst Shard in anvil	Worn in offhand: nearby hostile mobs glow (Spectral) within 16 blocks when sneaking. Uses durability.
Totem of the Blood Moon	1 Totem of Undying + 4 Netherite Scrap + 4 Redstone Blocks	On death during Blood Moon, revive with full health + Strength II for 10s. Single use.
Beacon of the King	9 Diamond Blocks + Nether Star center	Placed at King Mob's death location: grants all nearby players (32 blocks) Haste II + Luck for 5 minutes. Single use.
Enchanted Shulker Shell	Shulker Shell + 4 Ender Pearls + 4 Chorus Fruit	"Portable Ender Chest" — right-click opens ender chest inventory anywhere. 50 uses.
Elemental Arrows (4 types)	Arrow + ingredient (Magma Cream / Snowball / Wind Charge / Glowstone)	Fire Arrow (ignites), Frost Arrow (slows), Wind Arrow (high knockback), Glow Arrow (Spectral effect). Craftable in stacks of 8.
Guild Banner	Banner + guild-specific recipe	Placeable banner that grants guild members within 16 blocks Speed I or Resistance I.
19. FUTURE: CUSTOM TEXTURES VIA GEYSERMC + RAINBOW (Final Phase)
Goal: Give every Battle and Super weapon a unique visual appearance visible to both Java and Bedrock players.

How It Works: Java players see custom models via PaperMC 1.21.4+ item_model component + server resource pack. Bedrock players see custom items via Geyser Custom Items API v2 mapping + Bedrock resource pack.

Implementation Steps:

Plugin-side: Add item_model component to every Battle and Super weapon
Java Resource Pack: Custom 16×16 or 32×32 textures, host on web server
Geyser Mapping: v2-format mappings.json mapping each item_model to Bedrock custom item
Bedrock Resource Pack: item_texture.json + matching textures in Geyser packs folder
Validate: Use Rainbow (Fabric mod) to auto-generate and verify packs
Texture Design Ideas: Battle = colored border + "B" insignia. Super Iron = silver metallic sheen. Super Diamond = cyan crystalline glow. Super Netherite = dark crimson/purple aura with molten lava veins.

20. KEY LINKS
Resource	URL
GitHub Repository	https://github.com/JGlims/JGlimsPlugin
PaperMC API Javadoc (1.21.11)	https://jd.papermc.io/paper/1.21.11/
AnvilView API	https://jd.papermc.io/paper/1.21.11/org/bukkit/inventory/view/AnvilView.html
Sound API (Interface)	https://jd.papermc.io/paper/1.21.11/org/bukkit/Sound.html
Wolf API	https://jd.papermc.io/paper/1.21.11/org/bukkit/entity/Wolf.html
AbstractArrow API	https://jd.papermc.io/paper/1.21.11/org/bukkit/entity/AbstractArrow.html
EquipmentSlot API	https://jd.papermc.io/paper/1.21.11/org/bukkit/inventory/EquipmentSlot.html
EntityEquipment API	https://jd.papermc.io/paper/1.21.11/org/bukkit/inventory/EntityEquipment.html
LivingEntity API	https://jd.papermc.io/paper/1.21.11/org/bukkit/entity/LivingEntity.html
PaperMC Downloads	https://papermc.io/downloads/paper
Docker Minecraft Server	https://hub.docker.com/r/itzg/minecraft-server
Docker MC Docs	https://docker-minecraft-server.readthedocs.io/
Geyser Downloads	https://download.geysermc.org/
Geyser Custom Items v2	https://geysermc.org/wiki/geyser/custom-items/
Geyser Resource Packs	https://geysermc.org/wiki/geyser/packs/
Rainbow Wiki	https://geysermc.org/wiki/other/rainbow/
Rainbow Download	https://geysermc.org/download/?project=other-projects&rainbow=expanded
Hydraulic Wiki	https://geysermc.org/wiki/other/hydraulic/
Thunder Wiki	https://geysermc.org/wiki/other/thunder/
PackConverter GitHub	https://github.com/GeyserMC/PackConverter
Example Geyser Mappings	https://github.com/eclipseisoffline/geyser-example-mappings/
Chunky (Hangar)	https://hangar.papermc.io/pop4959/Chunky/versions/1.4.40
SkinsRestorer (Hangar)	https://hangar.papermc.io/SRTeam/SkinsRestorer
Minecraft Spear Wiki	https://minecraft.wiki/w/Spear
Bedrock Resource Pack Dev	https://wiki.bedrock.dev/guide/project-setup.html
Bedrock Custom Item Textures	https://wiki.bedrock.dev/items/items-intro#applying-textures
SpigotMC Wolf Armor Thread	https://www.spigotmc.org/threads/checking-if-a-wolf-has-armor.681502/
21. STEP-BY-STEP PHASE PLAN
Phase 1 — Compilation Fixes: DONE. Fixed 6 files. Migrated to AnvilView API, added spear enchantments.

Phase 1.5 — Battle-for-All Refactor: DONE. Created 4 new Battle managers, modified SuperToolManager/RecipeManager/JGlimsPlugin/WeaponMasteryManager.

Phase 2 — Missing Systems & Bug Fixes: DONE.

✅ Implement BestBuddiesListener.java (wolf 95% DR, 0 dmg, Regen II)
✅ Implement all 10 weapon abilities (Bow, Crossbow, Trident, Mace complete)
✅ Fix Sound.BLOCK_LIGHTNING_ROD_TOGGLE_ON → Sound.ENTITY_LIGHTNING_BOLT_IMPACT
✅ Fix Particle.CRIT_MAGIC → Particle.CRIT
✅ Reduce all weapon ability cooldowns (see Section 10)
✅ Register BestBuddiesListener in JGlimsPlugin.java
✅ Compile: BUILD SUCCESSFUL
✅ Deploy: v1.3.0 running on server
Phase 3 — In-Game Testing (CURRENT):

Test all 10 battle recipes
Test vanilla→super block (gatekeeper)
Test full upgrade chain (Battle → Super Iron → Diamond → Netherite)
Test enchantment preservation through upgrades
Test all 20 weapon abilities (10 Diamond + 10 Netherite)
Test Ender Dragon damage reduction
Test Best Buddies wolf system
Test all custom enchantments
Test mob difficulty, King Mob, Blood Moon
Test guilds, blessings, mastery, sort, loot
Test admin commands
Console error check
Investigate "50 anvil recipes" vs expected 52
Phase 4 — Super Tool System Polish:

Enforce material-matching for tiered weapons
Implement "+2% damage per enchantment" for Netherite Definitive
Implement "no enchantment limit" for Netherite Definitive
Implement "no enchantment conflicts" for Netherite Definitive
Verify SuperToolManager auto-applies Efficiency to super shovels
Phase 5 — Ability System Completion:

Test all 20 abilities in-game
Verify Ender Dragon damage reduction
Enhanced definitive particle effects
Verify ability only activates with proper super tier
Phase 6 — Quality & Balance:

Axe nerf verification
Lore consistency review
Cross-material prevention
Full enchantment preservation audit
Soulbound interaction with super weapons
Phase 7 — Testing & Integration:

Deploy to creative server
Full regression test
TPS verification under load
Memory profiling (stay under 6 GB)
Phase 8 — Go Live:

Delete creative world, recreate survival
Pre-generate chunks with Chunky (radius 5000)
Final deploy and launch
Phase 9 — Enchantment Expansion:

Add 12 new enchantments to EnchantmentType enum
Add to AnvilRecipeListener (12 new recipes)
Implement in EnchantmentEffectListener (12 new behaviors)
Add new items (Warden's Echo, Elemental Arrows, Guild Banners, etc.)
Phase 10 — Custom Textures (Final Phase):

Create Java resource pack
Add item_model component to all custom weapons
Create Geyser mapping JSON
Create Bedrock resource pack
Use Rainbow to validate
Deploy and test on both platforms
22. INSTRUCTIONS FOR THE NEXT CHAT
When continuing this project in a new chat session, provide this entire document and follow these rules:

Always start by reading the GitHub repository: Fetch the latest source from https://github.com/JGlims/JGlimsPlugin and check every file before making changes. The repo is the source of truth. Use raw.githubusercontent.com URLs to fetch individual files.

Always provide complete file replacements: Never provide partial snippets or "add this here." Every file must be a complete copy-paste replacement with the full file path.

Include full file paths for every file: e.g., src/main/java/com/jglims/plugin/weapons/BattleSwordManager.java

Work in phases: Follow the phase plan. Complete one phase at a time. Verify build success before proceeding.

Check the PaperMC 1.21.11 API: Use https://jd.papermc.io/paper/1.21.11/ for API verification. Use AnvilView (not deprecated AnvilInventory), Adventure components (not legacy ChatColor), EquipmentSlotGroup for AttributeModifiers, PersistentDataContainer for all custom data.

Maintain backward compatibility: Never break existing PDC data on items players already have.

Performance matters: TPS 20 target. Avoid heavy tick-frequency operations. Use player-specific spawnParticle.

The uniform rule: ALL weapons require Battle before Super. Only Elytra and Shield bypass this. SuperToolManager.isBattleItem() is the gatekeeper.

Deliver files in dependency order: ConfigManager → Enchantment system → Weapon managers → SuperToolManager → RecipeManager → JGlimsPlugin → Listeners.

Test the build after every phase: .\gradlew.bat clean jar must produce BUILD SUCCESSFUL with 0 errors.

Deep think before coding: Read the existing version on GitHub, understand all dependencies, plan changes, then deliver.

Sound constants: Always verify against https://jd.papermc.io/paper/1.21.11/org/bukkit/Sound.html before using. Sound is an interface, not an enum. Known invalid: BLOCK_LIGHTNING_ROD_TOGGLE_ON. Known valid: ENTITY_LIGHTNING_BOLT_IMPACT.

Particle constants: Always verify against the API. Known invalid: CRIT_MAGIC. Known valid: CRIT, ENCHANTED_HIT.

ConfigManager getters: Check exact names. Wrong: getEnderDragonDamageReduction(). Right: getEnderDragonAbilityDamageReduction().

Constructor signatures matter: BattleShovelManager(JGlimsPlugin, ConfigManager), SpearManager(JGlimsPlugin, ConfigManager), WeaponAbilityListener(JGlimsPlugin, ConfigManager, CustomEnchantManager, SuperToolManager, SpearManager, BattleShovelManager), BestBuddiesListener(JGlimsPlugin, ConfigManager). All other battle managers take just (JGlimsPlugin).

Deployment requires clearing cache: Always run sudo rm -rf ~/minecraft-server/data/plugins/.paper-remapped/ before deploying a new JAR version to prevent "Ambiguous plugin name" errors.

Use sudo for plugin operations: The Docker volume is root-owned. Use sudo cp, sudo rm for plugin file operations.

This document captures the complete state of JGlimsPlugin v1.3.0 as of March 4, 2026. Phase 2 is COMPLETE. Phase 3 (In-Game Testing) is CURRENT. The future vision extends through enchantment expansion (Phase 9) and custom crossplay textures via GeyserMC (Phase 10).
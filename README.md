JGLIMSPLUGIN — COMPLETE DEFINITIVE REFERENCE & SUMMARY DOCUMENT
Version: 1.3.0 (deployed and running, with v1.3.0 fixes applied) Author: JGlims Repository: https://github.com/JGlims/JGlimsPlugin Date: March 4, 2026 Build Status: BUILD SUCCESSFUL Current Phase: Phase 2 complete (Bug Fixes & Missing Systems) — Phase 3+ ready Platform: PaperMC 1.21.11, Java 21, Docker on OCI VM

TABLE OF CONTENTS
Server Infrastructure
Build & Deploy Workflow
Docker Commands (Full Reference)
build.gradle (Full File)
plugin.yml (Full File)
config.yml (Full File — with ALL v1.3.0 changes noted)
Complete File Inventory & Package Structure
Uniform Weapon Progression System (The Rule)
All 52 Custom Enchantments (+ 12 Planned Expansions)
Weapon Abilities — Complete Table (Updated Reduced Cooldowns)
Mob Difficulty, Bosses, King Mobs, Events
Additional Systems (Guilds, Blessings, Loot, Mastery, Best Buddies, etc.)
Commands Reference
All PersistentDataContainer (PDC) Keys
API Notes for PaperMC 1.21.11 (CRITICAL — read before writing ANY code)
Complete History of All Problems Found & Solved
Remaining Bugs / Pending Fixes (MUST DO NEXT)
Future Plans: New Enchantments, Items & Ideas (Phase 9+)
Future: Custom Textures via GeyserMC + Rainbow (Final Phase)
Key Links
Step-by-Step Phase Plan
Rules & Instructions for the Next Chat
1. SERVER INFRASTRUCTURE
Hardware: Oracle Cloud Infrastructure (OCI) VM — 4 OCPUs, 24 GB system RAM, 47 GB boot disk, Ubuntu Linux.

Docker container: Named mc-crossplay, running image itzg/minecraft-server:latest.

Key environment variables:

EULA=TRUE
TYPE=PAPER
VERSION=1.21.11
MEMORY=8G
ONLINE_MODE=false
RCON_PASSWORD=JGlims2026Rcon
GAMEMODE=survival (or creative for testing)
DIFFICULTY=hard
PLUGINS="https://download.geysermc.org/v2/projects/geyser/versions/latest/builds/latest/downloads/spigot,https://download.geysermc.org/v2/projects/floodgate/versions/latest/builds/latest/downloads/spigot"
Ports:

25565/TCP — Java Edition
19132/UDP — Bedrock Edition (via Geyser)
25575/TCP — RCON
Installed plugins:

JGlimsPlugin v1.3.0 (our plugin)
Geyser-Spigot v2.9.4
Floodgate v2.2.5
Chunky v1.4.40
SkinsRestorer v15.10.1 (pending installation)
World data: ~/minecraft-server/data/ containing world, world_nether, world_the_end.

Performance targets:

TPS: 20 (100% server tick rate)
Stay under 6 GB of the 8 GB heap
Never break farms — only alter health/damage/speed on mobs
Use lightweight listeners
Use player.spawnParticle() (player-specific) instead of world.spawnParticle() when only one player needs to see particles (e.g., Ore Pulse) to reduce network load
2. BUILD & DEPLOY WORKFLOW
Build on Windows:

Copy.\gradlew.bat clean jar
Build on Linux/Mac:

Copy./gradlew clean jar
Output JAR: build/libs/JGlimsPlugin-1.3.0.jar

Deploy to server:

Copyscp build/libs/JGlimsPlugin-1.3.0.jar MinecraftServer:~/
ssh MinecraftServer
cp ~/JGlimsPlugin-1.3.0.jar ~/minecraft-server/data/plugins/
rm ~/minecraft-server/data/plugins/JGlimsPlugin-1.2.0.jar
docker restart mc-crossplay
docker logs mc-crossplay 2>&1 | grep -i jglims
Build configuration: Java 21 toolchain, io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT as compileOnly, Maven Central + PaperMC repo (https://repo.papermc.io/repository/maven-public/), reproducible build, base name JGlimsPlugin.

3. DOCKER COMMANDS (FULL REFERENCE)
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
Useful commands:

Copydocker logs mc-crossplay --tail 100           # View recent logs
docker logs mc-crossplay 2>&1 | grep -i jglims # Check plugin loaded
docker exec -i mc-crossplay rcon-cli           # Enter RCON console
docker exec mc-crossplay mc-health             # Check server health
docker stats mc-crossplay                      # Live resource usage
docker restart mc-crossplay                    # Restart server
docker rm -f mc-crossplay                      # Remove container
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
6. config.yml (FULL FILE — v1.3.0 with changes noted)
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

# --- King Mob ---                    <<<< CHANGED: was 500, now 100
king-mob:
  enabled: true
  spawns-per-king: 100               # CHANGED v1.3.0: was 500
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
Config change log for v1.3.0: king-mob.spawns-per-king changed from 500 to 100. ConfigManager default also updated to 100.

7. COMPLETE FILE INVENTORY & PACKAGE STRUCTURE
All source: src/main/java/com/jglims/plugin/. Resources: src/main/resources/.

Package: com.jglims.plugin (root)
File	Description
JGlimsPlugin.java	Main class. onEnable initializes ALL managers in dependency order, registers ALL listeners, starts scheduled tasks. onCommand handles /guild (8 subcommands) and /jglims (5 subcommands). Accessor methods for every manager. BUG: BestBuddiesListener is NOT registered — must add.
Package: com.jglims.plugin.config
File	Description
ConfigManager.java	Loads config.yml with defaults for every system. 100+ primary getters and backward-compatibility alias methods. Constructor: (JavaPlugin). Key getters: getEnderDragonAbilityDamageReduction() (NOT getEnderDragonDamageReduction), getDogArmorReduction(), getOreDetectRadiusDiamond(), getOreDetectRadiusNetherite(), getOreDetectAncientDebrisRadiusDiamond(), getOreDetectAncientDebrisRadiusNetherite(), getOreDetectDurationTicks(), getSuperIronBonusDamage(), getSuperDiamondBonusDamage(), getSuperNetheriteBonusDamage(), getSuperNethPerEnchantBonus(), getPaleGardenFogCheckInterval() / getPaleGardenFogInterval() (alias). CHANGED v1.3.0: King mob default now reads 100 instead of 500.
Package: com.jglims.plugin.enchantments
File	Description
EnchantmentType.java	Enum of all 52 custom enchantments with getMaxLevel(). Categories: Sword(5), Axe(5), Pickaxe(5), Shovel(1), Hoe/Sickle(3), Bow(4), Crossbow(2), Trident(5), Armor(9), Elytra(4), Mace(4), Spear(3), Universal(2).
CustomEnchantManager.java	Registry of NamespacedKeys, conflict map, get/set/remove/list/copy enchantments via PersistentDataContainer. Used by all listeners.
AnvilRecipeListener.java	52 anvil recipes. Uses AnvilView API (not deprecated AnvilInventory). Handles soulbound, book creation, book application, conflict checks, XP cost reduction, "Too Expensive" removal. Includes v1.3.0 spear enchantments.
EnchantmentEffectListener.java	ALL enchantment behaviors: damage-event handlers, defender handlers, projectile handlers, block-break handlers, interact handlers, movement/passive handlers. Implements every enchantment's actual gameplay effect.
SoulboundListener.java	Keep-on-death for SOULBOUND items, lostsoul conversion (item drops with glow), one-per-inventory enforcement. Uses AnvilView API.
Package: com.jglims.plugin.weapons
File	Status	Description
BattleSwordManager.java	NEW v1.3.0	Battle Swords (+1 dmg, 1.6 speed). PDC: is_battle_sword (BYTE). Glint enabled. Adventure API lore.
BattlePickaxeManager.java	NEW v1.3.0	Battle Pickaxes (+1 dmg, retains mining speed). PDC: is_battle_pickaxe (BYTE). Glint enabled.
BattleTridentManager.java	NEW v1.3.0	Battle Trident (10 dmg, 1.1 speed). PDC: is_battle_trident (BYTE). Glint enabled.
BattleSpearManager.java	NEW v1.3.0	Battle Spears (+1 dmg per tier). PDC: is_battle_spear (BYTE). Glint enabled.
BattleAxeManager.java	UPDATED v1.3.0	Battle Axes (sickle dmg+1, 0.9 speed). PDC: is_battle_axe (BYTE). Blocks stripping. Glint enabled. Consistent lore format.
BattleBowManager.java	UPDATED v1.3.0	Battle Bow/Crossbow. PDC: is_battle_bow (BYTE), is_battle_crossbow (BYTE). UPDATED: Glint enabled. Adventure API Components for display name and lore. Ability description lines added.
BattleMaceManager.java	Existing	Battle Mace (12 dmg, 0.7 speed). PDC: is_battle_mace (BYTE). Glint enabled.
BattleShovelManager.java	UPDATED v1.3.0	Battle Shovels (+1.5 dmg, 1.2 speed). PDC: battle_shovel (BOOLEAN). Blocks path-making. Constructor: (JGlimsPlugin, ConfigManager). UPDATED: Migrated from § codes to Adventure API. Glint enabled. Consistent lore.
SickleManager.java	Existing	Sickles from hoes (sword dmg+1, 1.1 speed). PDC: is_sickle (BYTE). Blocks tilling.
SpearManager.java	Existing v1.3.0	Super spear tiers via PDC super_spear_tier (INTEGER 1/2/3). SpearTier enum with all materials. Constructor: (JGlimsPlugin, ConfigManager). Method: getSuperTierKey().
SuperToolManager.java	MODIFIED v1.3.0	3-tier super system (Iron=1, Diamond=2, Netherite=3). isBattleItem() checks all 10 PDC keys. createSuperTool() requires battle item (gate). Lore format: Attack Damage: [base] +[battle] +[super] = [total]. Preserves enchantments through upgrades. Auto-applies Efficiency (I/II/III by tier) to super shovels.
WeaponAbilityListener.java	MODIFIED v1.3.0	Right-click abilities for all 10 weapon classes (20 total abilities). Constructor: (JGlimsPlugin, ConfigManager, CustomEnchantManager, SuperToolManager, SpearManager, BattleShovelManager). ALL BUGS FIXED. Sound bug fixed. Cooldowns reduced. Sickle Reaper's Scythe completely revamped. Mace Meteor Strike fixed with invulnerability.
WeaponMasteryManager.java	MODIFIED v1.3.0	10 weapon classes. Linear scaling: kills/1000 = up to 10% max damage bonus. PDC keys: mastery_sword through mastery_spear.
Package: com.jglims.plugin.crafting
File	Description
RecipeManager.java	MODIFIED v1.3.0. 12-arg constructor. Registers ALL battle + super recipes. PDC guard blocks vanilla→Super (must be battle first). ADDED v1.3.0: onPrepareCraft check to block battle→battle re-crafting (battle-to-battle block). Only normal tools can become battle tools.
VanillaRecipeRemover.java	Removes conflicting vanilla recipes. Static method: remove(JavaPlugin).
Package: com.jglims.plugin.mobs
File	Description
MobDifficultyManager.java	Distance + biome mob scaling. Reads all config values. Implements Listener.
BossEnhancer.java	Boss health/damage multipliers. Dragon 3.5×/3.0×, Elder Guardian 2.5×/1.8×, Wither/Warden vanilla. Constructor: (JGlimsPlugin, ConfigManager).
KingMobManager.java	CHANGED v1.3.0: Now every 100 hostile mob spawns (was 500). King stats: ×10 health, ×3 damage. Gold glowing, no despawn. One per world. Drops 3-9 diamonds. Constructor: (JGlimsPlugin, ConfigManager).
BloodMoonManager.java	15% chance each night. 1.5×/1.3× mob multipliers. Darkness + red particles. Double drops/XP. Every 10th Blood Moon: Blood Moon King (Zombie, diamond armor, Netherite sword, 400 HP, ×5 dmg, drops 5-15 diamonds + netherite scrap). Constructor: (JGlimsPlugin, ConfigManager). Method: startScheduler().
Package: com.jglims.plugin.guilds
File	Description
GuildManager.java	CRUD: createGuild, invitePlayer, joinGuild, leaveGuild, kickPlayer, disbandGuild, showGuildInfo, listGuilds. Persists to guilds.yml. Max 10 members. Constructor: (JGlimsPlugin, ConfigManager).
GuildListener.java	Friendly fire prevention between guild members. Constructor: (JGlimsPlugin, GuildManager).
Package: com.jglims.plugin.blessings
File	Description
BlessingManager.java	3 blessings: C's (health), Ami's (damage), La's (armor). Permanent via AttributeModifiers. Method: showStats(CommandSender, Player). Constructor: (JGlimsPlugin).
BlessingListener.java	Right-click consumption, PDC persistence, join/respawn reapply. Constructor: (JGlimsPlugin, BlessingManager).
Package: com.jglims.plugin.effects
File	Description
PaleGardenFogTask.java	Every N ticks (default 40), applies Darkness to Pale Garden players. Constructor: (JGlimsPlugin). Method: start(int intervalTicks).
Package: com.jglims.plugin.utility
File	Description
BestBuddiesListener.java	BEST_BUDDIES enchantment on wolf armor. 95% damage reduction, 0 melee damage (pacifist wolf), permanent Regen II, heart particles. Uses wolf.getEquipment().getItem(EquipmentSlot.BODY). Chunk load persistence + global scan every 10s. Constructor: (JGlimsPlugin, ConfigManager). NOT YET REGISTERED.
InventorySortListener.java	Shift-click sorting. All 10 weapon categories. Constructor: (JGlimsPlugin).
EnchantTransferListener.java	Enchanted item + book in anvil → enchanted book, 0 XP. Uses AnvilView. Constructor: (JGlimsPlugin, CustomEnchantManager).
LootBoosterListener.java	Enchanted books in chest loot. Mob drops: hostile 5%, boss 15%. Guardian shards, echo shards, ghast tears. Constructor: (JGlimsPlugin, ConfigManager).
DropRateListener.java	Trident 35% from Drowned, Breeze 2-5 wind charges. Constructor: (JGlimsPlugin, ConfigManager).
VillagerTradeListener.java	50% price reduction, trade locking disabled. Constructor: (JGlimsPlugin, ConfigManager).
Resources: src/main/resources/
File	Description
plugin.yml	See Section 5.
config.yml	See Section 6.
8. UNIFORM WEAPON PROGRESSION SYSTEM (The Rule)
THE RULE: ALL weapons must become Battle before they can become Super. The upgrade path is:

Vanilla → Battle (+1 dmg) → Super Iron (+1) → Super Diamond (+2, unlocks Diamond ability) → Super Netherite (+3, unlocks Definitive ability)
Exceptions: Elytra and Shield bypass Battle, go directly to Super.

Battle Conversion Details
Weapon	Vanilla Dmg	Battle Dmg	Battle Speed	Recipe
Sword	4-8 (by material)	5-9	1.6	8 matching material + sword center
Pickaxe	2-6	3-7	1.2	8 matching material + pickaxe center
Axe	special formula	+1 over sickle	0.9	ingredient + block + axe (special pattern)
Shovel	2.5-6.5	4-8	1.2	8 matching material + shovel center
Hoe→Sickle	1	5-9 (sword dmg+1)	1.1	8 matching material + hoe center
Bow	N/A (ranged)	N/A	N/A	8 iron ingots + bow center
Crossbow	N/A (ranged)	N/A	N/A	8 iron ingots + crossbow center
Mace	11	12	0.7	8 breeze rods + mace center
Trident	9	10	1.1	8 prismarine shards + trident center
Spear	1-5 (by material)	2-6	0.87-1.54	8 matching material + spear center
Super Tier Bonuses
Tier	PDC Value	Bonus Damage	Ability	Special Rules
Iron	1	+1	None	—
Diamond	2	+2 (non-neth) / +1 (neth base)	Right-click Diamond ability	—
Netherite	3	+3 (non-neth) / +2 (neth base)	Right-click Definitive ability	No enchant conflicts, +2% dmg/enchant, no enchant limit
Trident exception: Diamond +3, Netherite +5.

Lore format: Attack Damage: [vanilla base] +[battle +1] +[super bonus] = [total]

Super recipe: Super Iron + 8 diamonds → Super Diamond. Super Diamond + 8 netherite ingots → Super Netherite. All upgrades preserve existing enchantments.

Gatekeeper: SuperToolManager.isBattleItem(ItemStack) checks all 10 PDC keys (is_battle_sword, is_battle_axe, is_battle_pickaxe, is_sickle, battle_shovel, is_battle_bow, is_battle_crossbow, is_battle_mace, is_battle_trident, is_battle_spear). Items without a battle PDC key cannot become Super.

Battle-to-Battle block (v1.3.0 fix): RecipeManager.onPrepareCraft now checks if the center item already has any battle PDC key. If so, the craft result is set to null. This prevents re-crafting a battle weapon into another battle weapon. Only normal (non-battle) tools can enter the battle crafting path.

9. ALL 52 CUSTOM ENCHANTMENTS (+ 12 Planned Expansions)
Current Enchantments (52)
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

Planned New Enchantments (Phase 9+) — 12 total
Sickle (3): SOUL_HARVEST(3) — Wither I-III on hit + Regen I-III for player while Wither active, conflicts GREEN_THUMB. REAPING_CURSE(3) — lingering Instant Damage cloud at corpse on kill, conflicts REPLENISH. CROP_REAPER(1) — kills have 15%+5%/Looting chance to drop seeds/wheat/carrots/potatoes.

Shovel (2): BURIAL(3) — Slowness III + Blindness I for 1-3s on hit, conflicts HARVESTER. EARTHSHATTER(3) — breaking a block sends shockwave breaking identical blocks in 1-3 radius.

Sword (1): FROSTBITE_BLADE(3) — Slowness I-III + freeze visual on hit, conflicts VENOMSTRIKE.

Axe (1): WRATH(3) — consecutive hits within 3s on same target: +10/20/30% damage, resets on target switch, conflicts CLEAVE.

Pickaxe (1): PROSPECTOR(3) — 5/10/15% chance double ore yield (stacks with Fortune), conflicts AUTO_SMELT.

Trident (1): TSUNAMI(3) — thrown hit creates water burst pushing mobs 3/5/7 blocks + Slowness I 2s (rain/water only), conflicts FROSTBITE.

Bow (1): FROSTBITE_ARROW(3) — arrows apply Slowness I-III + extinguish fire, conflicts EXPLOSIVE_ARROW.

Mace (1): TREMOR(3) — on hit, mobs within 2/4/6 blocks get Mining Fatigue II for 3s, conflicts GRAVITY_WELL.

Spear (1): PHANTOM_PIERCE(3) — charged spear attacks pierce 1/2/3 targets dealing full damage, conflicts SKEWERING.

10. WEAPON ABILITIES — COMPLETE TABLE (Updated Reduced Cooldowns)
All abilities require Super Diamond (tier 2) or Super Netherite (tier 3). Activated by right-click with the super weapon in main hand.

Weapon	Diamond Ability	Old CD	New CD	Damage	Netherite Definitive Ability	Old CD	New CD	Damage
Sword	Dash Strike	10s	6s	8/tick (dash)	Dimensional Cleave	18s	12s	15/tick + 25 final (12-block rift AoE)
Axe	Bloodthirst	12s	8s	buff: 5s lifesteal + Haste + Strength	Ragnarok Cleave	22s	15s	20/16/12 (3 expanding shockwaves)
Pickaxe	Ore Pulse	15s	10s	none (detect ores 8-block, debris 24-block)	Seismic Resonance	25s	18s	none (detect ores 12-block, debris 40-block)
Shovel	Earthen Wall	12s	8s	6 + launch + Resistance II	Tectonic Upheaval	20s	14s	14/wave + Resistance II + Slowness III
Sickle	Harvest Storm	10s	6s	7 initial + 24 bleed (5-block AoE)	Reaper's Scythe ★	20s	14s	REVAMPED — see below
Spear	Phantom Lunge	12s	8s	8 piercing (8-block dash)	Spear of the Void	20s	14s	18 pierce + 10 detonation (30-block projectile)
Bow	Arrow Storm	12s	8s	6/arrow × 5 rapid-fire	Celestial Volley	20s	14s	10/arrow × 12 from sky + 8 AoE impact
Crossbow	Chain Shot	12s	8s	8 + 4 chain × 3 piercing bolts	Thunder Barrage	22s	15s	12 + 14 AoE × 6 explosive bolts
Trident	Tidal Surge	12s	8s	8 + water knockback + Slowness I	Poseidon's Wrath	20s	14s	16/13/10 (3 water waves) + 10 lightning
Mace	Ground Slam	12s	8s	10 + stun (Slowness III + Mining Fatigue)	Meteor Strike ★	22s	15s	FIXED — 15-35 distance-based (see below)
★ Reaper's Scythe — COMPLETE REWORK (v1.3.0)
Three-phase ability over 60 ticks (3 seconds):

Phase 1 (ticks 0-20) — Dark Summoning Vortex:

3 contracting spiral arms of soul fire particles spiraling inward across 10-block radius
Rising column of soul fire at center
Enemies pulled toward center every 4 ticks with velocity manipulation
Wither III + Slowness I applied to all caught enemies
Dark purple dust particles on enemies
Warden heartbeat sound every 10 ticks
Phase 2 (ticks 20-60) — Soul Reap Cyclone:

Spinning dual-blade arcs of blue soul fire sweeping the entire 10-block radius
Each blade arm traces from center outward with soul fire, dragon breath at edges, dark dust at core
Ground-level ring of 24 blue fire points rotating around the perimeter
Center pillar: upward soul stream spiraling
DAMAGE: 12 damage per pulse every 5 ticks = 8 pulses = 96 base damage per enemy
WITHER III refreshed every pulse (80 ticks duration)
SLOWNESS II applied every pulse
HEALING: Player heals 3 HP per enemy per pulse (massive sustain in groups)
Heart + Totem particles on player when healing
Light pull keeps enemies in the vortex
Ambient sounds: sweep slashes, heartbeat, wither ambient, warden sonic boom
Phase 3 (tick 60) — Death Harvest Explosion:

200 soul fire flame particles, 120 dragon breath, 5 explosion emitters, 100 reverse portal, 80 totem particles
Upward soul beam shooting 15 blocks high with spiral arms
20 damage AoE burst to all enemies in 12-block radius
Enemies launched outward with velocity 2.0
Wither III (160 ticks) + Darkness (100 ticks) on all enemies
Death mark: soul fire particles on each hit enemy
Lingering fading soul vortex for 2 more seconds
Three explosion sounds: Warden Sonic Boom + Generic Explode + Lightning Bolt Impact
Total potential per enemy: 96 (cyclone) + 20 (explosion) = 116+ damage plus Wither III ticking plus constant player healing.

★ Meteor Strike — FIXED (v1.3.0)
The old version could kill the player from fall damage. Fixed behavior:

Phase 1 — Pre-launch protection:

Player receives Resistance 255 (complete immunity to ALL damage) for 200 ticks
Player receives Slow Falling for 60 ticks (controlled ascent)
Flame + lava + orange dust trail during ascent
Phase 2 — Launch (tick 0):

Player launched upward with velocity Y=3.5
Heavy mace smash + warden sonic boom sounds
Ascending fire trail for 20 ticks
Phase 3 — Slam (tick 25):

Slow Falling removed → player falls fast
Velocity set to Y=-4.0 (forced slam)
Ender Dragon growl sound
Meteor fire trail: 15 flame, 5 lava, 8 smoke, 10 orange dust particles per tick
Phase 4 — Impact (on ground detection):

Fall distance calculated from launch height → 15-35 damage (0.5 dmg per block fallen)
12-block AoE radius with distance-scaled damage (60-100% based on proximity)
Expanding shockwave rings (15 ticks of 30-particle flame rings)
5 explosion emitters + 80 lava + 100 flame + 50 smoke + 60 totem particles
4 simultaneous sounds: Heavy Mace Smash + Explode + Lightning Impact + Warden Sonic Boom
All enemies: knockback 2.0 + launch Y=1.0 + Slowness III (80t) + Mining Fatigue II (80t) + Fire (60t)
Phase 5 — Safe buffer:

Resistance 255 removed 20 ticks (1 second) AFTER landing
Player is completely safe during the entire ability
Special Rules
Ender Dragon exception: All Netherite Definitive abilities deal only 30% damage to the Ender Dragon. Controlled by config.getEnderDragonAbilityDamageReduction() (default 0.30). Diamond abilities are NOT reduced.

Bloodthirst special mechanic: When activated, stores bloodthirst_active LONG PDC key on the weapon with expiry timestamp. During the 5-second window, all melee hits heal the player (implemented in EnchantmentEffectListener or checked inline).

Ore Pulse / Seismic Resonance: Uses player.spawnParticle() (player-specific) for ore highlighting — only the caster sees particles. Each ore type has a unique color: Coal(50,50,50), Iron(210,150,100), Copper(180,100,50), Gold(255,215,0), Redstone(255,0,0), Lapis(30,30,200), Emerald(0,200,50), Diamond(100,230,255), Ancient Debris(100,50,20) + flame particles, Nether Gold(255,200,50), Nether Quartz(240,240,230).

11. MOB DIFFICULTY, BOSSES, KING MOBS, EVENTS
Distance Scaling (Overworld only)
Distance from spawn	Health Multiplier	Damage Multiplier
0-349	1.0×	1.0×
350	1.5×	1.3×
700	2.0×	1.6×
1000	2.5×	1.9×
2000	3.0×	2.2×
3000	3.5×	2.5×
5000+	4.0×	3.0×
Scaling is incremental (interpolated) between thresholds.

Biome Multipliers (stack with distance)
Biome	Health	Damage
Pale Garden	2.0×	2.0×
Deep Dark	2.5×	2.5×
Swamp	1.4×	1.4×
Nether Wastes	1.7×	1.7×
Soul Sand Valley	1.9×	1.9×
Crimson Forest	2.0×	2.0×
Warped Forest	2.0×	2.0×
Basalt Deltas	2.3×	2.3×
End	2.5×	2.0×
Boss Enhancer
Boss	Health	Damage
Ender Dragon	3.5×	3.0×
Elder Guardian	2.5×	1.8×
Wither	1.0× (vanilla)	1.0×
Warden	1.0× (vanilla)	1.0×
Creeper Reduction
50% of creeper spawns are cancelled (cancel-chance: 0.5).

King Mob (CHANGED v1.3.0: was every 500, now every 100)
Every 100 hostile mob spawns → one King variant
King stats: ×10 health, ×3 damage
Visual: gold glowing, no despawn, custom golden name
Drops: 3-9 diamonds
Limit: one per world at a time
Blood Moon
15% chance each night (checked every 100 ticks)
During Blood Moon: 1.5×/1.3× all mob health/damage, Darkness effect on all players, red particles in sky, double drops and XP
Every 10th Blood Moon: Blood Moon King spawns — Zombie with diamond armor, Netherite sword, 400 HP (20.0 base × 20.0 multiplier), ×5 damage. Drops 5-15 diamonds + netherite scrap on death.
12. ADDITIONAL SYSTEMS
Guilds
Full guild system with 8 subcommands
Max 10 members per guild
Friendly fire disabled between guild members
Data persists to plugins/JGlimsPlugin/guilds.yml
Blessings (3 types)
Blessing	Uses	Effect per Use	Attribute
C's Bless	10	+1 heart (2 HP)	MAX_HEALTH
Ami's Bless	10	+2% attack damage	ATTACK_DAMAGE
La's Bless	10	+2% armor	ARMOR
Right-click to consume. PDC tracks uses remaining. Permanent via AttributeModifiers. Effects reapply on join/respawn.

Best Buddies (Wolf Armor)
Apply BEST_BUDDIES enchantment to Wolf Armor (via anvil)
Wolf takes 95% less damage (configurable), deals 0 melee (pacifist), gets permanent Regen II, heart particles every 2s
Detection: wolf.getEquipment().getItem(EquipmentSlot.BODY)
Persistence: PDC marker best_buddies_applied (BYTE) + global scan task every 10s
Weapon Mastery
10 weapon classes tracked: sword, axe, pickaxe, shovel, sickle, bow, crossbow, trident, mace, spear
Linear scaling: (kills / 1000) × 10% = max 10% damage bonus at 1000 kills
PDC keys: mastery_sword through mastery_spear (INTEGER)
Damage bonus via mastery_damage AttributeModifier
View: /jglims mastery
Inventory Sort
Shift-click any empty slot in a container to sort
Sorts by material type with special handling for all 10 weapon categories
Enchant Transfer
Enchanted item in slot 1 + plain Book in slot 2 of anvil → Enchanted Book with all enchantments
Cost: 0 XP. Original item consumed.
Loot Booster
Chest loot: guaranteed enchanted book added
Mob drops: hostile 5% chance (+2% per Looting), boss 15% (+5% per Looting)
Guardian: 1-3 prismarine shards extra. Elder Guardian: 3-5 extra
Ghast: 1-2 ghast tears extra. Warden: 40% echo shard chance
Drop Rate Booster
Trident from Drowned: 35% (vanilla 8.5%)
Breeze wind charges: 2-5 (vanilla 1-2)
Villager Trades
All prices reduced 50%. Trade locking disabled.
Axe Nerf
All axes: attack speed set to 0.5 (much slower than vanilla) — makes swords viable
Pale Garden Fog
Repeating task (every 40 ticks). Players in Pale Garden biome get Darkness effect.
13. COMMANDS REFERENCE
/guild (Player-only)
Subcommand	Usage	Description
create	/guild create <name>	Create a new guild
invite	/guild invite <player>	Invite a player to your guild
join	/guild join	Accept a pending guild invitation
leave	/guild leave	Leave your current guild
kick	/guild kick <player>	Kick a member (leader only)
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
14. ALL PERSISTENTDATACONTAINER (PDC) KEYS
Key	Type	Used By	Purpose
is_battle_sword	BYTE	BattleSwordManager	Marks item as battle sword
is_battle_axe	BYTE	BattleAxeManager	Marks item as battle axe
is_battle_pickaxe	BYTE	BattlePickaxeManager	Marks item as battle pickaxe
is_sickle	BYTE	SickleManager	Marks item as sickle (converted hoe)
battle_shovel	BOOLEAN	BattleShovelManager	Marks item as battle shovel
is_battle_bow	BYTE	BattleBowManager	Marks item as battle bow
is_battle_crossbow	BYTE	BattleBowManager	Marks item as battle crossbow
is_battle_mace	BYTE	BattleMaceManager	Marks item as battle mace
is_battle_trident	BYTE	BattleTridentManager	Marks item as battle trident
is_battle_spear	BYTE	BattleSpearManager	Marks item as battle spear
super_tool_tier	INTEGER (1/2/3)	SuperToolManager	Super tier: 1=Iron, 2=Diamond, 3=Netherite
super_spear_tier	INTEGER (1/2/3)	SpearManager	Super spear tier (separate key)
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
Per-EnchantmentType (52 keys)	INTEGER (level)	CustomEnchantManager	e.g., vampirism, bleed, best_buddies etc.
15. API NOTES FOR PAPERMC 1.21.11 (CRITICAL — READ BEFORE WRITING ANY CODE)
These are hard-learned lessons that will cause compilation failures if ignored:

1. Wolf armor detection:

Copywolf.getEquipment().getItem(EquipmentSlot.BODY)
The method getBodyArmor() does NOT exist on EntityEquipment. EquipmentSlot.BODY is valid for wolves, horses, happy ghasts, nautiluses.

2. Arrow lifetime:

Copyarrow.setLifetimeTicks(int)
The method setLife(int) does NOT exist. Import: org.bukkit.entity.AbstractArrow.

3. Sound constants: In PaperMC 1.21.11, Sound is an interface (not an enum). It has static final fields.

Sound.BLOCK_LIGHTNING_ROD_TOGGLE_ON does NOT exist → Use Sound.ENTITY_LIGHTNING_BOLT_IMPACT
Confirmed working sounds: ITEM_MACE_SMASH_GROUND, ITEM_MACE_SMASH_GROUND_HEAVY, ENTITY_WARDEN_SONIC_BOOM, ENTITY_WARDEN_EMERGE, ENTITY_WARDEN_HEARTBEAT, ITEM_TRIDENT_RIPTIDE_3, ITEM_TRIDENT_THUNDER, ENTITY_LIGHTNING_BOLT_THUNDER, ENTITY_BREEZE_SHOOT, ITEM_CROSSBOW_SHOOT, ENTITY_ELDER_GUARDIAN_CURSE, ENTITY_LIGHTNING_BOLT_IMPACT
4. ConfigManager getter names:

Copyconfig.getEnderDragonAbilityDamageReduction()  // CORRECT
config.getEnderDragonDamageReduction()          // DOES NOT EXIST
Always check the actual getter name in ConfigManager.java.

5. AnvilView API:

CopyAnvilView anvilView = (AnvilView) event.getView();
anvilView.setRepairCost(cost);
anvilView.setMaximumRepairCost(maxCost);
Import: org.bukkit.inventory.view.AnvilView. The old AnvilInventory.setRepairCost() etc. are deprecated.

6. AttributeModifiers:

Copynew AttributeModifier(new NamespacedKey(plugin, "key_name"), amount, AttributeModifier.Operation.ADD_NUMBER)
The UUID-based constructor is deprecated. Always use the NamespacedKey constructor.

7. Adventure API:

Copyimport net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
meta.displayName(Component.text("Battle Sword", NamedTextColor.GOLD));
Do NOT use legacy ChatColor or § color codes for new code. Existing code with § works but should be migrated.

8. Material._SPEAR does not exist in vanilla: Spears are custom items. The getWeaponClass() method in WeaponAbilityListener checks name.endsWith("_SPEAR") but this relies on the spear materials being correctly defined. SpearManager uses its own isSpearMaterial() check.

16. COMPLETE HISTORY OF ALL PROBLEMS FOUND & SOLVED
Problem 1: Sound Constant Does Not Exist
What: WeaponAbilityListener.java used Sound.BLOCK_LIGHTNING_ROD_TOGGLE_ON
Why it broke: This constant does not exist in PaperMC 1.21.11 — Sound is an interface, not an enum
Fix: Replaced with Sound.ENTITY_LIGHTNING_BOLT_IMPACT everywhere
Status: ✅ FIXED in the new WeaponAbilityListener.java
Problem 2: Battle-to-Battle Recipe Bug
What: A player could place a Battle Axe in a Battle Sword recipe and get a Battle Sword that was also still a Battle Axe
Why: RecipeManager did not check if the input item was already a battle item
Fix: Added onPrepareCraft event handler in RecipeManager.java that checks the center item for ANY of the 10 battle PDC keys. If found, result is set to null.
Status: ✅ FIXED in updated RecipeManager.java
Problem 3: Sickle Reaper's Scythe Was Weak/Boring
What: The old definitive ability was a simple dual-arc of soul fire with 10 dmg/tick and 1.5 HP lifesteal — underwhelming for a Netherite Definitive
Request: Complete rework with incredible visuals, Wither effect, blue fire, healing, very high damage
Fix: Completely rebuilt as a 3-phase ability (Dark Summoning Vortex → Soul Reap Cyclone → Death Harvest Explosion) with 116+ total damage, Wither III, massive blue soul fire particles, 3 HP/enemy/pulse healing, darkness finale
Status: ✅ FIXED in the new WeaponAbilityListener.java
Problem 4: Mace Meteor Strike Could Kill the Player
What: The old Meteor Strike launched the player high up but didn't protect against fall damage — the player would die from their own ability
Fix: Player now receives Resistance 255 (complete immunity) + Slow Falling BEFORE launch. Slow Falling removed before slam. Resistance removed 1 second AFTER landing.
Status: ✅ FIXED in the new WeaponAbilityListener.java
Problem 5: King Mob Spawn Rate Too Low
What: King mobs only spawned every 500 hostile mob spawns — too rare to be fun
Request: Change to 1 per 100 hostile mobs
Fix: Changed config.yml value king-mob.spawns-per-king from 500 to 100. Updated ConfigManager.java default.
Status: ✅ FIXED in config.yml and ConfigManager.java
Problem 6: Inconsistent Battle Weapon Descriptions
What: Different battle weapons had different lore formats — some used legacy § codes, some had no ability descriptions, some had no glint
Fix: Updated BattleBowManager.java and BattleShovelManager.java to use Adventure API Components, added meta.setEnchantmentGlintOverride(true), added consistent lore format with damage, speed, ability names
Status: ✅ FIXED in updated manager files
Problem 7: All Cooldowns Were Too Long
What: Original cooldowns (10-25 seconds) made abilities feel sluggish
Fix: Reduced all 20 cooldowns as per the table in Section 10 (e.g., Sword 10s→6s, Dimensional Cleave 18s→12s)
Status: ✅ FIXED in WeaponAbilityListener.java
Problem 8: BestBuddiesListener Not Registered
What: BestBuddiesListener exists as a complete class but is NOT registered in JGlimsPlugin.onEnable()
Fix needed: Add registration line in JGlimsPlugin.java
Status: ⚠️ STILL PENDING — see Section 17
Problem 9: Legacy Color Codes
What: Some manager files still use § color codes instead of Adventure API
Fix: Migrated BattleBowManager and BattleShovelManager to Adventure API. Other files should be migrated over time.
Status: ⚠️ PARTIALLY DONE — remaining files use §-codes which still work but should be migrated
17. REMAINING BUGS / PENDING FIXES (MUST DO NEXT)
BUG 1: BestBuddiesListener Not Registered (CRITICAL)
File: JGlimsPlugin.java
Issue: BestBuddiesListener is a fully implemented class with constructor (JGlimsPlugin, ConfigManager) but the onEnable() method never calls getServer().getPluginManager().registerEvents(new BestBuddiesListener(this, configManager), this);
Fix: Add the registration line in onEnable() after other listener registrations
Priority: HIGH — wolf armor system is completely non-functional without this
BUG 2: config.yml on GitHub Still Shows 500 for King Mob
File: config.yml in the repository
Issue: The GitHub version may still have spawns-per-king: 500
Fix: Push the updated config.yml with spawns-per-king: 100
Priority: MEDIUM — ConfigManager default handles it, but config file should match
BUG 3: Remaining Legacy Color Code Files
Files: SickleManager.java, BattleAxeManager.java, BattleMaceManager.java, and others
Issue: Still use § color codes instead of Adventure API Components
Fix: Migrate each file to use Component.text() with NamedTextColor
Priority: LOW — works fine, just inconsistent
BUG 4: Spear Material Detection Edge Case
File: WeaponAbilityListener.java
Issue: getWeaponClass() checks name.endsWith("_SPEAR") but vanilla Minecraft has no _SPEAR material. Spear detection works via SpearManager.isSpearMaterial() in other contexts. Need to verify WeaponAbilityListener correctly detects spear abilities.
Fix: Verify spear right-click triggers correctly; may need to add SpearManager check in the event handler
Priority: MEDIUM — test in-game
BUG 5: Verify All Damage Values Match Documentation
Issue: Cross-reference all battle weapon manager damage values against the tables in Section 8 to ensure consistency
Fix: Manual audit of each manager's damage constants
Priority: MEDIUM — was requested but needs in-depth verification
18. FUTURE PLANS: NEW ENCHANTMENTS, ITEMS & IDEAS (Phase 9+)
12 New Enchantments (detailed in Section 9)
SOUL_HARVEST, REAPING_CURSE, CROP_REAPER, BURIAL, EARTHSHATTER, FROSTBITE_BLADE, WRATH, PROSPECTOR, TSUNAMI, FROSTBITE_ARROW, TREMOR, PHANTOM_PIERCE

New Item Ideas
Battle Shield — custom shield with PDC, upgradeable to Super
Battle Elytra — already partially implemented via super_elytra_durability PDC key
Custom Arrows — specialized arrow types (frost, fire, void)
Totem alternatives — custom totems with different effects
System Ideas
Dungeon system — procedurally generated dungeons with custom mobs and loot
Quest system — NPC-given quests with rewards
Economy — coin-based trading system
Player shops — chest shops with custom GUI
Skill trees — per-weapon-class skill tree unlocked by mastery
Seasonal events — Halloween, Christmas, etc. with special mobs and drops
19. FUTURE: CUSTOM TEXTURES VIA GEYSERMC + RAINBOW (Final Phase)
This is the final visual polish phase. The plan is to use GeyserMC's custom model data system combined with a resource pack to give battle/super weapons unique textures visible to both Java and Bedrock players. Research needed on the Rainbow system for Geyser-based texture packs.

20. KEY LINKS
Resource	URL
GitHub Repository	https://github.com/JGlims/JGlimsPlugin
PaperMC API Javadocs	https://jd.papermc.io/paper/1.21.1/
PaperMC Maven Repo	https://repo.papermc.io/repository/maven-public/
Geyser Downloads	https://download.geysermc.org
Adventure API Docs	https://docs.advntr.dev/
itzg Docker Image	https://hub.docker.com/r/itzg/minecraft-server
Bukkit Sound List	Check PaperMC Javadocs for org.bukkit.Sound interface
21. STEP-BY-STEP PHASE PLAN
Phase	Description	Status
Phase 1	Core systems: enchantments, battle weapons, sickles, blessings, mob difficulty	✅ COMPLETE
Phase 2	Bug fixes & missing systems: new battle weapons (sword, pickaxe, trident, spear, shovel), super tool gatekeeper, weapon abilities, mastery	✅ COMPLETE (with fixes applied)
Phase 3	Register BestBuddiesListener, fix remaining bugs from Section 17, audit all damage values	⬜ NEXT
Phase 4	Migrate all legacy §-codes to Adventure API across all files	⬜ PLANNED
Phase 5	Add remaining weapon abilities polish (visual effects, balance testing)	⬜ PLANNED
Phase 6	Implement SkinsRestorer integration	⬜ PLANNED
Phase 7	Guild system expansion (levels, perks, guild wars)	⬜ PLANNED
Phase 8	Blood Moon polish, dungeon system prototype	⬜ PLANNED
Phase 9	12 new enchantments (SOUL_HARVEST, BURIAL, WRATH, etc.)	⬜ PLANNED
Phase 10	Custom textures via GeyserMC + Rainbow	⬜ FINAL
22. RULES & INSTRUCTIONS FOR THE NEXT CHAT
Paste this entire document at the start of the next chat as context. Then follow these rules:

RULE 1: ALWAYS READ THE GITHUB FIRST
Before making ANY changes, ALWAYS fetch the latest code from https://github.com/JGlims/JGlimsPlugin. Read the actual source file before modifying it. Never assume you know what the code looks like — always verify by reading the raw file from the repository.

RULE 2: ALWAYS GIVE COMPLETE FILES
Never give partial code snippets with "// ... rest unchanged" or similar. Every modified file must be provided in FULL, complete, copy-paste-ready form from the first line to the last line. If a file is too long, split across messages but always provide the COMPLETE code.

RULE 3: DEEP THINKING / HIGH QUALITY
Think deeply about every change. Consider side effects, edge cases, API compatibility (see Section 15), and cross-file dependencies. Check constructor signatures before changing them. Verify Sound constants exist. Test that PDC keys match across files.

RULE 4: RESPECT THE API NOTES (Section 15)
Never use getBodyArmor(), setLife(), Sound.BLOCK_LIGHTNING_ROD_TOGGLE_ON, getEnderDragonDamageReduction(), UUID-based AttributeModifier constructors, or AnvilInventory instead of AnvilView. These will cause compilation failures.

RULE 5: RESPECT THE WEAPON PROGRESSION SYSTEM (Section 8)
All weapons follow: Vanilla → Battle → Super Iron → Super Diamond → Super Netherite. Never bypass the Battle step. Always check isBattleItem() before creating super tools.

RULE 6: PRESERVE ENCHANTMENTS
All upgrade paths must preserve existing custom and vanilla enchantments. Use CustomEnchantManager.copyEnchantments() when transforming items.

RULE 7: CONSTRUCTORS MATTER
Files have specific constructor signatures. Always match them. Key constructors to remember:

WeaponAbilityListener(JGlimsPlugin, ConfigManager, CustomEnchantManager, SuperToolManager, SpearManager, BattleShovelManager)
RecipeManager(JGlimsPlugin, SickleManager, BattleAxeManager, BattleBowManager, BattleMaceManager, SuperToolManager, BattleSwordManager, BattlePickaxeManager, BattleTridentManager, BattleSpearManager, BattleShovelManager, SpearManager) — 12 arguments
BattleShovelManager(JGlimsPlugin, ConfigManager)
SpearManager(JGlimsPlugin, ConfigManager)
RULE 8: PERFORMANCE
Use player.spawnParticle() instead of world.spawnParticle() when only one player needs to see particles. Keep scheduled tasks lightweight. Target TPS 20, under 6 GB heap.

RULE 9: WHAT TO DO NEXT (Priority Order)
Register BestBuddiesListener in JGlimsPlugin.java — provide the COMPLETE updated JGlimsPlugin.java
Verify spear ability detection works in WeaponAbilityListener
Audit all damage values across all managers vs the tables in this document
Push updated config.yml with spawns-per-king: 100
Begin Phase 4: migrate remaining § color codes to Adventure API
Begin Phase 9: implement the 12 new enchantments
RULE 10: COMMUNICATION STYLE
Explain what you're changing and why. Flag any inconsistencies or potential bugs. Provide build and deploy commands after every change. Confirm sound constants exist before using them. When in doubt, check the GitHub.

END OF JGLIMSPLUGIN DEFINITIVE REFERENCE DOCUMENT v1.3.0 Date: March 4, 2026 Next action: Paste this document into a new chat and continue with Phase 3.
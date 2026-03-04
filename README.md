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





JGLIMSPLUGIN v1.3.0 — EXHAUSTIVE PROJECT HANDOFF DOCUMENT
For starting a brand-new chat with full context

Repository: https://github.com/JGlims/JGlimsPlugin Date: March 4, 2026 | Platform: PaperMC 1.21.11, Java 21, Docker on OCI VM

SECTION 1: SERVER INFRASTRUCTURE (COMPLETE)
Hardware: Oracle Cloud Infrastructure (OCI) VM — 4 OCPUs, 24 GB system RAM, 47 GB boot disk, Ubuntu Linux.

Docker container name: mc-crossplay, image itzg/minecraft-server:latest.

Environment variables (exact):

EULA=TRUE
TYPE=PAPER
VERSION=1.21.11
MEMORY=8G
ONLINE_MODE=false
RCON_PASSWORD=JGlims2026Rcon
GAMEMODE=survival
DIFFICULTY=hard
PLUGINS="https://download.geysermc.org/v2/projects/geyser/versions/latest/builds/latest/downloads/spigot,https://download.geysermc.org/v2/projects/floodgate/versions/latest/builds/latest/downloads/spigot"
Ports: 25565/TCP (Java Edition), 19132/UDP (Bedrock via Geyser), 25575/TCP (RCON).

Installed plugins: JGlimsPlugin v1.3.0 (our custom plugin), Geyser-Spigot v2.9.4, Floodgate v2.2.5, Chunky v1.4.40, SkinsRestorer v15.10.1 (BROKEN — needs clean install, see Bug C below).

World data path: ~/minecraft-server/data/ containing world, world_nether, world_the_end.

Performance rules: TPS target 20 (100%). Stay under 6 GB heap. Never break farms (only alter health/damage/speed on mobs, never AI/spawning). Use player.spawnParticle() for per-player effects instead of world.spawnParticle() when only one player should see the effect (e.g., Ore Pulse). Use lightweight listeners.

SECTION 2: COMPLETE DOCKER COMMANDS
Copy# === CREATIVE TEST SERVER ===
docker run -d --name mc-crossplay \
  -p 25565:25565 -p 19132:19132/udp -p 25575:25575 \
  -e EULA=TRUE -e TYPE=PAPER -e VERSION=1.21.11 \
  -e MEMORY=8G -e ONLINE_MODE=false -e GAMEMODE=creative -e DIFFICULTY=hard \
  -e RCON_PASSWORD=JGlims2026Rcon \
  -e PLUGINS="https://download.geysermc.org/v2/projects/geyser/versions/latest/builds/latest/downloads/spigot,https://download.geysermc.org/v2/projects/floodgate/versions/latest/builds/latest/downloads/spigot" \
  -v ~/minecraft-server/data:/data \
  itzg/minecraft-server:latest

# === SWITCH TO SURVIVAL ===
docker rm -f mc-crossplay
# Same command above but with -e GAMEMODE=survival

# === ESSENTIAL COMMANDS ===
docker logs mc-crossplay --tail 100              # Recent logs
docker logs mc-crossplay 2>&1 | grep -i jglims   # Check plugin loaded
docker logs mc-crossplay 2>&1 | grep -i "skin"   # Check SkinsRestorer
docker exec -i mc-crossplay rcon-cli              # Enter RCON console
docker exec mc-crossplay mc-health                # Server health
docker stats mc-crossplay                         # Live resource usage
docker restart mc-crossplay                       # Restart
docker rm -f mc-crossplay                         # Destroy container
SECTION 3: BUILD & DEPLOY WORKFLOW (EXACT STEPS)
Build (Windows): .\gradlew.bat clean jar Build (Linux/Mac): ./gradlew clean jar Output JAR: build/libs/JGlimsPlugin-1.3.0.jar

Deploy to server (exact commands):

Copyscp build/libs/JGlimsPlugin-1.3.0.jar MinecraftServer:~/
ssh MinecraftServer
sudo rm ~/minecraft-server/data/plugins/JGlimsPlugin-*.jar
cp ~/JGlimsPlugin-1.3.0.jar ~/minecraft-server/data/plugins/
docker restart mc-crossplay
sleep 15
docker logs mc-crossplay --since 30s 2>&1 | grep -i jglims
build.gradle (EXACT — version 1.3.0):

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
plugin.yml (EXACT):

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
SECTION 4: COMPLETE FILE INVENTORY WITH EXACT SIZES AND GITHUB RAW URLS
Every source file in the project, with the raw URL pattern: https://raw.githubusercontent.com/JGlims/JGlimsPlugin/refs/heads/main/{path}

Root Config & Resources
File	Size	Raw URL
build.gradle	478B	.../build.gradle
src/main/resources/plugin.yml	689B	.../src/main/resources/plugin.yml
src/main/resources/config.yml	3,851B	.../src/main/resources/config.yml
README.md	49,349B	.../README.md
com.jglims.plugin (root package)
File	Size	Constructor Args	Listeners Registered	Notes
JGlimsPlugin.java	13,117B	N/A (extends JavaPlugin)	22 listeners registered in onEnable()	Main class. Handles /guild (8 subcmds) + /jglims (5 subcmds). Initializes ALL managers in exact dependency order. BestBuddiesListener IS registered (line pm.registerEvents(new BestBuddiesListener(this, configManager), this); exists in the current code). Has accessor methods for every manager.
com.jglims.plugin.config
File	Size	Constructor	Key Methods
ConfigManager.java	29,211B	(JavaPlugin)	loadConfig(), getEnderDragonAbilityDamageReduction() (returns 0.30), getDogArmorReduction() (returns 0.95), getOreDetectRadiusDiamond() (8), getOreDetectRadiusNetherite() (12), getOreDetectAncientDebrisRadiusDiamond() (24), getOreDetectAncientDebrisRadiusNetherite() (40), getOreDetectDurationTicks() (200), getSuperIronBonusDamage() (1.0), getSuperDiamondBonusDamage() (2.0), getSuperNetheriteBonusDamage() (2.0), getSuperNethPerEnchantBonus() (2.0), getKingMobSpawnsPerKing() (100), getBloodMoonChance() (0.15), getBloodMoonBossEveryNth() (10), getBloodMoonBossHealthMult() (20.0), getBloodMoonBossDamageMult() (5.0), isPaleGardenFogEnabled(), getPaleGardenFogCheckInterval() (40), isBloodMoonEnabled(), getBloodMoonCheckInterval() (100), 100+ more getters. Has backward-compatibility alias methods.
com.jglims.plugin.enchantments
File	Size	Constructor	Description
EnchantmentType.java	1,513B	N/A (enum)	52 enum values. Each has getMaxLevel(). Categories: Sword(5), Axe(5), Pickaxe(5), Shovel(1), Hoe/Sickle(3), Bow(4), Crossbow(2), Trident(5), Armor(9), Elytra(4), Mace(4), Spear(3), Universal(2).
CustomEnchantManager.java	7,565B	(JGlimsPlugin)	Creates NamespacedKey for each of 52 enchantments. Conflict map: VAMPIRISM↔LIFESTEAL, BLEED↔VENOMSTRIKE, CHAIN_LIGHTNING↔WITHER_TOUCH, BERSERKER↔BLOOD_PRICE, VEINMINER↔DRILL, EXPLOSIVE_ARROW↔HOMING, SEISMIC_SLAM↔MAGNETIZE, SWIFTFOOT↔MOMENTUM, IMPALING_THRUST↔EXTENDED_REACH↔SKEWERING (triangle). Methods: getKey(EnchantmentType), getLevel(ItemStack, EnchantmentType), setLevel(ItemStack, EnchantmentType, int), removeEnchant(ItemStack, EnchantmentType), listEnchantments(CommandSender), copyEnchantments(ItemStack from, ItemStack to), hasConflict(EnchantmentType, EnchantmentType).
AnvilRecipeListener.java	27,829B	(JGlimsPlugin, CustomEnchantManager)	52 anvil recipes (item + ingredient → enchanted item). Uses AnvilView API (NOT deprecated AnvilInventory). Handles: book creation (extract enchant to book), book application (apply book to item), conflict checks, level upgrades (same enchant + same level → level+1 up to max), XP cost reduction (config anvil.xp-cost-reduction: 0.5), removes "Too Expensive" cap. Soulbound items blocked from combining. Includes v1.3.0 spear enchantments (IMPALING_THRUST, EXTENDED_REACH, SKEWERING).
EnchantmentEffectListener.java	56,155B	(JGlimsPlugin, CustomEnchantManager)	ALL 52 enchantment gameplay behaviors. Handlers: onEntityDamageByEntity (attacker enchants: VAMPIRISM heal, LIFESTEAL heal, BLEED DoT, VENOMSTRIKE poison, CHAIN_LIGHTNING arc, WITHER_TOUCH, BERSERKER low-HP bonus, BLOOD_PRICE self-damage+bonus, CLEAVE AoE, GUILLOTINE head drop, SOUL_REAP wither, REAPERS_MARK mark+bonus, FROSTBITE slowness+freeze, THUNDERLORD lightning, TIDAL_WAVE knockback, SEISMIC_SLAM AoE, MAGNETIZE pull, GRAVITY_WELL slow, MOMENTUM speed, IMPALING_THRUST bonus, EXTENDED_REACH range, SKEWERING pierce). Defender enchants: FORTIFICATION flat reduction, DEFLECTION reflection, DODGE chance, VITALITY regen. Projectile enchants: EXPLOSIVE_ARROW explosion, HOMING tracking, RAPIDFIRE speed, SNIPER distance bonus. Block-break: VEINMINER chain, DRILL column, AUTO_SMELT smelt, MAGNETISM collect, EXCAVATOR 3x3, HARVESTER crop, TIMBER tree, LUMBERJACK log. Passive: SWIFTNESS speed, AQUA_LUNGS water breathing, NIGHT_VISION, SWIFTFOOT sprint, LEAPING jump, BOOST elytra rocket, CUSHION fall cancel, GLIDER slow fall, STOMP landing damage. Interact: GREEN_THUMB bone meal, REPLENISH crop, HARVESTING_MOON night harvest. Bloodthirst melee heal check.
SoulboundListener.java	7,961B	(JGlimsPlugin, CustomEnchantManager)	On death: soulbound items kept in inventory. Non-soulbound items drop normally. LostSoul mechanic: soulbound items that somehow end up on the ground get converted to glowing entities. One soulbound item per inventory slot enforced. Uses AnvilView API.
com.jglims.plugin.weapons (ALL 13 files)
File	Size	Constructor	PDC Keys	Implements Listener?	Key Data
BattleSwordManager.java	5,947B	(JGlimsPlugin)	is_battle_sword (BYTE), battle_sword_damage (NamespacedKey for modifier), battle_sword_speed (NamespacedKey for modifier)	No	Damage: Wood 5, Stone 6, Iron 7, Gold 5, Diamond 8, Netherite 9. Speed: 1.6 (modifier -2.4). Tiers: all 6 sword materials. Ingredient: matching material planks/cobble/ingot/etc. Glint enabled.
BattlePickaxeManager.java	6,301B	(JGlimsPlugin)	is_battle_pickaxe (BYTE), battle_pickaxe_damage, battle_pickaxe_speed	No	Damage: Wood 3, Stone 4, Iron 5, Gold 3, Diamond 6, Netherite 7. Speed: 1.2 (modifier -2.8). Retains mining enchantments. Glint enabled.
BattleAxeManager.java	6,670B	(JGlimsPlugin)	is_battle_axe (BYTE), battle_axe_damage, battle_axe_speed	Yes — blocks log stripping, copper scraping, pumpkin carving on right-click	Damage: Wood 6, Stone 7, Iron 8, Gold 6, Diamond 9, Netherite 10 (sickle +1). Speed: 0.9 (modifier -3.1).
BattleShovelManager.java	7,734B	(JGlimsPlugin, ConfigManager)	battle_shovel (BOOLEAN)	Yes — blocks path-making on dirt/grass/mud/etc	Has ShovelTier enum: WOODEN/STONE/COPPER/GOLDEN/IRON/DIAMOND/NETHERITE with getShovelMaterial(), getIngredient(), getBaseDamage(), getBaseSpeedModifier(). BATTLE_DAMAGE map: Wood 4, Stone 5, Copper 5, Gold 4, Iron 6, Diamond 7, Netherite 8. Battle speed: -2.8 (yielding 1.2). Damage modifier = damage - 1.0.
BattleBowManager.java	4,152B	(JGlimsPlugin)	is_battle_bow (BYTE), is_battle_crossbow (BYTE)	No	createBattleBow() and createBattleCrossbow(). No attribute modifiers (ranged). Lore includes ability descriptions. Glint enabled.
BattleMaceManager.java	3,290B	(JGlimsPlugin)	is_battle_mace (BYTE), battle_mace_damage, battle_mace_speed	No	Damage: 12.0 (modifier 11.0). Speed: 0.7 (modifier -3.3). Single tier only.
BattleTridentManager.java	3,899B	(JGlimsPlugin)	is_battle_trident (BYTE), battle_trident_damage, battle_trident_speed	No	Damage: 10.0 (vanilla 9 + 1, modifier 9.0). Speed: 1.1 (modifier -2.9). Single tier only. Glint enabled.
BattleSpearManager.java	7,262B	(JGlimsPlugin)	is_battle_spear (BYTE)	No	Battle spear = vanilla spear +1 damage per tier. getBattleSpearTiers() returns all spear materials. getBattleSpearIngredient(Material) maps to matching ingredient. createBattleSpear(Material) creates with PDC.
SickleManager.java	7,305B	(JGlimsPlugin)	is_sickle (BYTE), sickle_damage, sickle_speed	Yes — blocks tilling on dirt/grass/farmland/etc	Damage = equivalent sword +1: Wood 5, Stone 6, Iron 7, Gold 5, Diamond 8, Netherite 9. Speed: 1.1 (modifier -2.9). getSickleTiers() returns all 6 hoe materials. getSickleIngredient() maps to matching material.
SpearManager.java	9,663B	(JavaPlugin, ConfigManager)	super_spear_tier (INTEGER: 0/1/2/3)	No	Spears skip Battle step entirely. Path: Vanilla → Super Iron → Super Diamond → Super Netherite. SpearTier enum: WOODEN(1.0 dmg, 1.54 speed), STONE(2.0, 1.33), COPPER(2.0, 1.18), GOLDEN(2.0, 1.05), IRON(3.0, 1.05), DIAMOND(4.0, 0.95), NETHERITE(5.0, 0.87). createSuperSpear(ItemStack, int tier) creates super spear. Accumulated super bonus: Iron +1, Diamond +1+2=+3, Netherite +1+2+2=+5. isSpear(Material) static check. getSuperTierKey() returns the super_spear_tier NamespacedKey. NOTE: SpearManager uses legacy § color codes (meta.setDisplayName()) not Adventure API — this is inconsistent with other managers but functional.
SuperToolManager.java	22,175B	(JGlimsPlugin)	super_tool_tier (INTEGER: 0/1/2/3), super_tool_damage (DOUBLE), super_tool_speed (DOUBLE), super_elytra_durability (INTEGER)	Yes (registered but no visible events in summary)	TIER constants: TIER_NONE=0, TIER_IRON=1, TIER_DIAMOND=2, TIER_NETHERITE=3. isBattleItem(ItemStack) checks ALL 10 PDC keys (is_battle_sword, is_battle_axe, is_battle_pickaxe, is_sickle, battle_shovel, is_battle_bow, is_battle_crossbow, is_battle_mace, is_battle_trident, is_battle_spear). createSuperTool(ItemStack base, int tier) — RETURNS NULL if base is not a battle item (the gatekeeper). upgradeSuperTool(ItemStack existing, int newTier) preserves all vanilla + custom enchantments. Damage bonuses: Iron +1, Diamond +2 (or +1 if netherite base), Netherite +3 (or +2 if netherite base). Trident exception: Diamond +3, Netherite +5. Elytra durability save: 30% iron, 50% diamond, 70% netherite. Auto-applies Efficiency by tier to super shovels. getVanillaBaseDamage(Material) and getVanillaAttackSpeed(Material) provide lookup tables. Lore: Attack Damage: [base] +[battle] +[super] = [total].
WeaponAbilityListener.java	77,345B	(JGlimsPlugin, ConfigManager, CustomEnchantManager, SuperToolManager, SpearManager, BattleShovelManager)	Uses super_tool_tier + super_spear_tier to determine ability tier, bloodthirst_active (LONG) on axe items	No (registered externally)	20 abilities (10 weapons × Diamond + Netherite). Full details in Section 7 below. File is very large (77KB). The crawler tool truncates at ~10K tokens when fetching the raw URL. You can reliably get the first ~60% (through sickle Reaper's Scythe linger ticks). The last ~40% (spear, bow, crossbow, trident, mace methods) must be reconstructed.
WeaponMasteryManager.java	9,803B	(JGlimsPlugin, ConfigManager)	mastery_sword, mastery_axe, mastery_pickaxe, mastery_shovel, mastery_sickle, mastery_bow, mastery_crossbow, mastery_trident, mastery_mace, mastery_spear (all INTEGER)	Yes	10 weapon classes. Kill tracking per weapon type. Linear scaling: (kills / maxKills) × maxBonusPercent where maxKills=1000, maxBonusPercent=10.0. So at 1000 kills = +10% damage. Applied as mastery_damage AttributeModifier. View with /jglims mastery.
com.jglims.plugin.crafting
File	Size	Constructor	Description
RecipeManager.java	26,435B	12 args: (JGlimsPlugin, SickleManager, BattleAxeManager, BattleBowManager, BattleMaceManager, SuperToolManager, BattleSwordManager, BattlePickaxeManager, BattleTridentManager, BattleSpearManager, BattleShovelManager, SpearManager)	HAS BUG A (see below). Registers all recipes in registerAllRecipes(). Battle recipe pattern: " I " / " T " / " " (1 ingredient on top, tool in center). Super recipe pattern: "MMM" / " T " / " " (3 materials on top, tool in center). onPrepareCraft event handler has 4 guards: (1) blocks custom weapons in non-plugin recipes, (2) blocks battle→battle re-crafting (center item already has battle PDC → null), (3) super recipe PDC guard (must be battle item or existing super), (4) super tool upgrade (existing super + 3 higher-tier materials → upgraded super preserving enchants). Uses two registration methods: registerAllSuperTiers(Material, String) for items WITHOUT battle PDC (Elytra, Shield), and registerSuperTiersForBattleItem(ItemStack, Material, String) for items WITH battle PDC. THE BUG: Swords, pickaxes, and shovels incorrectly use registerAllSuperTiers instead of registerSuperTiersForBattleItem.
VanillaRecipeRemover.java	588B	Static remove(JavaPlugin)	Removes conflicting vanilla recipes.
com.jglims.plugin.mobs
File	Size	Constructor	Description
MobDifficultyManager.java	4,391B	(JGlimsPlugin, ConfigManager)	Distance-from-spawn + biome multiplier scaling. On CreatureSpawnEvent, applies health and damage multipliers. 6-tier distance: 350/700/1000/2000/3000/5000 blocks. 10 biome multipliers. Interpolates between thresholds.
BossEnhancer.java	3,345B	(JGlimsPlugin, ConfigManager)	On boss spawn: Dragon 3.5× health / 3.0× damage, Elder Guardian 2.5× / 1.8×, Wither 1.0× / 1.0×, Warden 1.0× / 1.0×.
KingMobManager.java	5,972B	(JGlimsPlugin, ConfigManager)	Every 100 hostile mob spawns → King variant. King: ×10 health, ×3 damage, gold glowing, custom golden name, no despawn, 3-9 diamond drops. One per world max. Counter tracked globally.
BloodMoonManager.java	12,103B	(JGlimsPlugin, ConfigManager)	startScheduler() checks every 100 ticks. 15% chance each night (13000-23000 ticks). During Blood Moon: 1.5× health / 1.3× damage on all monsters (NOT creepers), Darkness effect on players, red dust particles, double drops + double XP. Every 10th Blood Moon: Blood Moon King (Zombie, diamond armor, Netherite sword, 20 base × 20.0 mult = 400 HP, ×5 damage, drops 5-15 diamonds + 1 netherite ingot, 500 XP). King despawns at dawn if not killed.
BiomeMultipliers.java	1,697B	N/A	Biome multiplier data: Pale Garden 2.0×, Deep Dark 2.5×, Swamp 1.4×, Nether Wastes 1.7×, Soul Sand Valley 1.9×, Crimson Forest 2.0×, Warped Forest 2.0×, Basalt Deltas 2.3×, End 2.5× health / 2.0× damage.
com.jglims.plugin.guilds
File	Size	Constructor	Description
GuildManager.java	13,679B	(JGlimsPlugin, ConfigManager)	Full CRUD: createGuild, invitePlayer, joinGuild, leaveGuild, kickPlayer, disbandGuild, showGuildInfo, listGuilds. Persists to plugins/JGlimsPlugin/guilds.yml. Max 10 members (configurable).
GuildListener.java	1,456B	(JGlimsPlugin, GuildManager)	onEntityDamageByEntity: if attacker and victim are both players in same guild → cancel damage (friendly fire prevention).
com.jglims.plugin.blessings
File	Size	Constructor	Description
BlessingManager.java	10,296B	(JGlimsPlugin)	3 blessings: C's (health, +1 heart/use, 10 uses), Ami's (damage, +2%/use, 10 uses), La's (armor, +2%/use, 10 uses). Applied as permanent AttributeModifier on MAX_HEALTH, ATTACK_DAMAGE, ARMOR. showStats(CommandSender, Player) shows all blessing stats.
BlessingListener.java	3,452B	(JGlimsPlugin, BlessingManager)	Right-click consumption: checks PDC for c_bless_item, ami_bless_item, la_bless_item (BYTE). Tracks remaining uses in PDC. On player join/respawn: re-applies blessing AttributeModifiers from PDC data.
com.jglims.plugin.utility
File	Size	Constructor	Description
BestBuddiesListener.java	11,609B	(JGlimsPlugin, ConfigManager)	BEST_BUDDIES on wolf armor. 95% incoming damage reduction (configurable). Wolf deals 0 melee (cancel EntityDamageByEntityEvent). Permanent Regen II. Heart + dust particles every 2s. Detection: wolf.getEquipment().getItem(EquipmentSlot.BODY). PDC: best_buddies_applied (BYTE) on wolf entity. Chunk load persistence + global scan task every 10s. Right-click equip detection with 2-tick delayed check.
InventorySortListener.java	5,282B	(JGlimsPlugin)	Shift-click empty slot in container → sort. Handles all 10 weapon categories for proper ordering.
EnchantTransferListener.java	7,135B	(JGlimsPlugin, CustomEnchantManager)	Enchanted item + plain Book in anvil → Enchanted Book with all custom enchantments extracted. Cost: 0 XP. Original item consumed. Uses AnvilView API.
LootBoosterListener.java	8,991B	(JGlimsPlugin, ConfigManager)	Chest loot: guaranteed random enchanted book added to any chest. Mob drops: hostile 5% (+2% per Looting level), boss 15% (+5% per Looting level) to drop enchanted book. Guardian: 1-3 extra prismarine shards. Elder Guardian: 3-5 extra. Ghast: 1-2 extra ghast tears. Warden: 40% echo shard chance.
DropRateListener.java	3,289B	(JGlimsPlugin, ConfigManager)	Trident from Drowned: 35% (vanilla ~8.5%). Breeze: 2-5 wind charges (vanilla 1-2).
VillagerTradeListener.java	4,244B	(JGlimsPlugin, ConfigManager)	All villager trade prices reduced 50%. Trade locking disabled (villagers never lock trades).
PaleGardenFogTask.java	1,356B	(JGlimsPlugin)	start(int intervalTicks). Repeating task every N ticks (default 40). Players in Pale Garden biome get Darkness I effect.
SECTION 5: THE WEAPON PROGRESSION SYSTEM (COMPLETE)
THE RULE: ALL weapons must become Battle before they can become Super. The path is:

Vanilla Tool → Battle (+1 dmg, PDC marker, enchant glint) → Super Iron (+1 bonus) → Super Diamond (+2, unlocks Diamond ability) → Super Netherite (+3, unlocks Definitive ability)
Exceptions: Elytra and Shield bypass Battle (go directly to Super). Spears bypass Battle (SpearManager handles its own super tiers separately from SuperToolManager).

Complete Battle Weapon Stats
Weapon	Material	Vanilla Dmg	Battle Dmg	Battle Speed	Recipe Ingredient	PDC Key
Sword	Wood	4	5	1.6	OAK_PLANKS	is_battle_sword
Sword	Stone	5	6	1.6	COBBLESTONE	is_battle_sword
Sword	Iron	6	7	1.6	IRON_INGOT	is_battle_sword
Sword	Gold	4	5	1.6	GOLD_INGOT	is_battle_sword
Sword	Diamond	7	8	1.6	DIAMOND	is_battle_sword
Sword	Netherite	8	9	1.6	NETHERITE_INGOT	is_battle_sword
Pickaxe	Wood	2	3	1.2	OAK_PLANKS	is_battle_pickaxe
Pickaxe	Stone	3	4	1.2	COBBLESTONE	is_battle_pickaxe
Pickaxe	Iron	4	5	1.2	IRON_INGOT	is_battle_pickaxe
Pickaxe	Gold	2	3	1.2	GOLD_INGOT	is_battle_pickaxe
Pickaxe	Diamond	5	6	1.2	DIAMOND	is_battle_pickaxe
Pickaxe	Netherite	6	7	1.2	NETHERITE_INGOT	is_battle_pickaxe
Axe	Wood	—	6	0.9	OAK_PLANKS	is_battle_axe
Axe	Stone	—	7	0.9	COBBLESTONE	is_battle_axe
Axe	Iron	—	8	0.9	IRON_INGOT	is_battle_axe
Axe	Gold	—	6	0.9	GOLD_INGOT	is_battle_axe
Axe	Diamond	—	9	0.9	DIAMOND	is_battle_axe
Axe	Netherite	—	10	0.9	NETHERITE_INGOT	is_battle_axe
Sickle (Hoe)	Wood	1	5	1.1	OAK_PLANKS	is_sickle
Sickle (Hoe)	Stone	1	6	1.1	COBBLESTONE	is_sickle
Sickle (Hoe)	Iron	1	7	1.1	IRON_INGOT	is_sickle
Sickle (Hoe)	Gold	1	5	1.1	GOLD_INGOT	is_sickle
Sickle (Hoe)	Diamond	1	8	1.1	DIAMOND	is_sickle
Sickle (Hoe)	Netherite	1	9	1.1	NETHERITE_INGOT	is_sickle
Shovel	Wood	2.5	4	1.2	OAK_PLANKS	battle_shovel
Shovel	Stone	3.5	5	1.2	COBBLESTONE	battle_shovel
Shovel	Copper	3.5	5	1.2	COPPER_INGOT	battle_shovel
Shovel	Gold	2.5	4	1.2	GOLD_INGOT	battle_shovel
Shovel	Iron	4.5	6	1.2	IRON_INGOT	battle_shovel
Shovel	Diamond	5.5	7	1.2	DIAMOND	battle_shovel
Shovel	Netherite	6.5	8	1.2	NETHERITE_INGOT	battle_shovel
Bow	—	ranged	ranged	—	STICK (1 on top)	is_battle_bow
Crossbow	—	ranged	ranged	—	STICK (1 on top)	is_battle_crossbow
Mace	11	12	0.7	STICK (1 on top)	is_battle_mace	
Trident	9	10	1.1	STICK (1 on top)	is_battle_trident	
Spear	varies	+1 per tier	varies	matching material	is_battle_spear	
Super Tier Bonuses
Tier	PDC super_tool_tier	Bonus Dmg (normal base)	Bonus Dmg (netherite base)	Trident Exception	Elytra Durability Save	Ability
Iron	1	+1	+1	+1	30%	None
Diamond	2	+2	+1	+3	50%	Right-click Diamond ability
Netherite	3	+3	+2	+5	70%	Right-click Definitive ability, +2% dmg/enchant, no enchant limit
Crafting Recipe Patterns
Battle recipe:     Super recipe:     Super upgrade (crafting table):
    " I "              "MMM"              "MMM"
    " T "              " T "              " S "    (S = existing super tool)
    "   "              "   "              "   "
SECTION 6: ALL 52 CUSTOM ENCHANTMENTS (COMPLETE WITH EXACT MAX LEVELS & CONFLICTS)
Sword (5)
Enchant	Max	Conflict	Effect
VAMPIRISM	5	↔LIFESTEAL	Heal (level × 0.5) HP on melee hit
LIFESTEAL	3	↔VAMPIRISM	Heal (damage × level × 5%) HP on hit
BLEED	3	↔VENOMSTRIKE	Apply bleed DoT: 1 HP/s for (level × 2) seconds
VENOMSTRIKE	3	↔BLEED	Apply Poison (level) for 3-5s on hit
CHAIN_LIGHTNING	3	↔WITHER_TOUCH	Lightning arcs to (level) nearby enemies within (3 + level) blocks
Axe (5)
Enchant	Max	Conflict	Effect
BERSERKER	5	↔BLOOD_PRICE	Below 50% HP: +(level × 10%) damage
LUMBERJACK	3	—	Chop entire tree (level × 8 max logs)
CLEAVE	3	—	AoE damage (50% main hit) to enemies within (1 + level) blocks
TIMBER	3	—	Break adjacent logs of same type up to (level × 5) blocks
GUILLOTINE	3	—	(level × 5%) chance to drop mob head
Pickaxe (5)
Enchant	Max	Conflict	Effect
VEINMINER	3	↔DRILL	Mine connected ore veins up to (level × 8) blocks
DRILL	3	↔VEINMINER	Mine (1 + level × 2) blocks deep in a column
AUTO_SMELT	1	—	Mined ores drop smelted form
MAGNETISM	1	—	Mined items teleport to player inventory
EXCAVATOR	3	—	Mine (2 × level + 1)² area (3×3, 5×5, 7×7)
Shovel (1)
Enchant	Max	Conflict	Effect
HARVESTER	3	—	Break crops in (level × 2 + 1)² area and auto-replant
Hoe/Sickle (3)
Enchant	Max	Conflict	Effect
GREEN_THUMB	1	—	Right-click: bone meal effect on crops in 3×3 area
REPLENISH	1	—	Breaking mature crops auto-replants
HARVESTING_MOON	3	—	At night: (level × 10%) chance to double crop drops
Bow (4)
Enchant	Max	Conflict	Effect
EXPLOSIVE_ARROW	3	↔HOMING	Arrows explode on impact (power = level, no block damage)
HOMING	3	↔EXPLOSIVE_ARROW	Arrows track nearest entity within (5 + level × 3) blocks
RAPIDFIRE	3	—	Draw speed +(level × 20%) faster
SNIPER	3	—	+(level × 15%) damage per 10 blocks distance
Crossbow (2)
Enchant	Max	Conflict	Effect
THUNDERLORD	5	—	Every 3rd hit on same target: lightning strike dealing (level × 3) bonus damage
TIDAL_WAVE	3	—	Bolts apply knockback (level × 1.5) + Slowness I for (level) seconds
Trident (5)
Enchant	Max	Conflict	Effect
FROSTBITE	3	—	Apply Slowness (level) + freeze effect for (2 + level) seconds
SOUL_REAP	3	—	Apply Wither (level) for (3 + level) seconds
BLOOD_PRICE	3	↔BERSERKER	Self-damage 2 HP, +((level × 2) + 2) bonus damage
REAPERS_MARK	3	—	Mark target: next hit within 5s deals +(level × 30%) bonus
WITHER_TOUCH	3	↔CHAIN_LIGHTNING	Apply Wither (level + 1) for 5s on hit
Armor (9)
Enchant	Max	Conflict	Effect
SWIFTNESS	3	—	Permanent Speed (level) while worn
VITALITY	3	—	+(level × 2) max hearts while worn
AQUA_LUNGS	3	—	Water Breathing + Conduit Power while worn
NIGHT_VISION	1	—	Permanent Night Vision while worn
FORTIFICATION	3	—	Flat -(level × 1.0) damage reduction on all hits
DEFLECTION	3	—	(level × 5%) chance to reflect damage to attacker
SWIFTFOOT	3	↔MOMENTUM	+(level × 10%) sprint speed
DODGE	3	—	(level × 5%) chance to completely dodge attack (0 damage + teleport sound)
LEAPING	3	—	Jump Boost (level) while worn
Elytra (4)
Enchant	Max	Conflict	Effect
BOOST	3	—	Firework-like boost on right-click (strength = level, 10s cooldown)
CUSHION	1	—	Negate all fall damage while wearing elytra
GLIDER	1	—	Slow Falling effect while gliding
STOMP	3	—	Landing deals (fall distance × level × 0.5) AoE damage
Mace (4)
Enchant	Max	Conflict	Effect
SEISMIC_SLAM	3	↔MAGNETIZE	AoE knockback + (level × 2) damage in (2 + level) block radius on hit
MAGNETIZE	3	↔SEISMIC_SLAM	Pull enemies within (3 + level × 2) blocks toward you on hit
GRAVITY_WELL	3	—	Slow enemies within (2 + level) blocks (Slowness level) on hit
MOMENTUM	3	↔SWIFTFOOT	Each consecutive hit within 3s: +(level × 5%) speed, stacks up to 5
Spear (3) — NEW v1.3.0
Enchant	Max	Conflict	Effect
IMPALING_THRUST	3	↔EXTENDED_REACH, ↔SKEWERING	+(level × 15%) damage on melee thrust
EXTENDED_REACH	3	↔IMPALING_THRUST, ↔SKEWERING	+(level × 0.5) block melee reach
SKEWERING	3	↔IMPALING_THRUST, ↔EXTENDED_REACH	Melee hits pierce through (level) additional entities
Universal (2)
Enchant	Max	Conflict	Effect
SOULBOUND	1	—	Keep item on death
BEST_BUDDIES	1	—	Wolf armor: 95% DR, 0 melee, Regen II, hearts
SECTION 7: ALL 20 WEAPON ABILITIES (COMPLETE WITH CURRENT + PLANNED DAMAGE VALUES)
Activation: Right-click with super weapon in main hand. Requires Super Diamond (tier ≥ 2). Super Diamond = Diamond ability. Super Netherite (tier ≥ 3) = Definitive ability.

Ender Dragon rule: ALL Netherite Definitive abilities deal only 30% damage to the Dragon (configurable via getEnderDragonAbilityDamageReduction()). Diamond abilities are NOT reduced.

SWORD — "Dash Strike" / "Dimensional Cleave"
Dash Strike (Diamond, 6s CD): Player launched forward (velocity ×1.8). 8 ticks of SWEEP_ATTACK + CRIT particles. Each tick: 8 dmg (→12 planned) to enemies within 2.5 blocks + push.

Dimensional Cleave (Netherite, 12s CD): Warden Sonic Boom sound. Center = 4 blocks in front. 15-tick expanding ring (radius 2.0 → 11.0). Ring: 40 SOUL particles + REVERSE_PORTAL. Every 3 ticks: 15 dmg (→22 planned) to enemies in radius + Slowness II (60t). EXPLOSION + SOUL_FIRE_FLAME at center. Delayed 30-tick final burst: EXPLOSION_EMITTER + TOTEM_OF_UNDYING particles + 25 dmg (→35 planned) in 8-block radius + knockback 1.5 + Y 0.6.

AXE — "Bloodthirst" / "Ragnarok Cleave"
Bloodthirst (Diamond, 8s CD): Haste II + Strength I for 100 ticks. Stores bloodthirst_active LONG PDC on weapon with 5s expiry. 50-tick DAMAGE_INDICATOR + red DUST particles on player. During active window, melee hits heal player (implemented in EnchantmentEffectListener). Planned addition: also grant Speed I.

Ragnarok Cleave (Netherite, 15s CD): Warden Sonic Boom + Heavy Mace Smash sounds. 20-tick expanding LAVA + CAMPFIRE_SMOKE + FLAME particles. 3 delayed waves (0, 8, 16 ticks): Wave 1: 4-block radius, 20 dmg (→30), Wave 2: 7-block radius, 16 dmg (→24), Wave 3: 10-block radius, 12 dmg (→18). Each wave: knockback 1.2 + Y 0.8-1.3 + Weakness II (80t). EXPLOSION_EMITTER per wave.

PICKAXE — "Ore Pulse" / "Seismic Resonance"
Ore Pulse (Diamond, 10s CD): No damage. Detects all ore types within radius. Config: ore-detect.radius-diamond: 8, ancient-debris-radius-diamond: 24, duration-ticks: 200. Expanding ENCHANT particle rings. Per-player particle rendering (player.spawnParticle()). Each ore type has unique color: Coal(50,50,50), Iron(210,150,100), Copper(180,100,50), Gold(255,215,0), Redstone(255,0,0), Lapis(30,30,200), Emerald(0,200,50), Diamond(100,230,255), Ancient Debris(100,50,20 + FLAME + SOUL_FIRE_FLAME), Nether Gold(255,200,50), Nether Quartz(240,240,230). Reports total ores + debris count in actionbar + chat message.

Seismic Resonance (Netherite, 18s CD): Same as Ore Pulse but larger. Config: radius-netherite: 12, ancient-debris-radius-netherite: 40. SOUL_FIRE_FLAME expanding rings instead of ENCHANT. Warden Heartbeat sound.

SHOVEL — "Earthen Wall" / "Tectonic Upheaval"
Earthen Wall (Diamond, 8s CD): Gravel Break + Iron Golem Attack sounds. Resistance II (80t). 6-block radius: enemies launched upward (Y 1.2) + 6 dmg (→10 planned). 50 BLOCK(DIRT) particles.

Tectonic Upheaval (Netherite, 14s CD): Warden Emerge + Sonic Boom sounds. Resistance III (160t). 30-tick expanding radius (0.4/tick). BLOCK(NETHERRACK) + CAMPFIRE_SMOKE particles. Every 10 ticks (offset 5): 14 dmg (→20 planned) to enemies in radius + launch Y 1.8 + Slowness IV (120t). EXPLOSION_EMITTER + Generic Explode sound.

SICKLE — "Harvest Storm" / "Reaper's Scythe"
Harvest Storm (Diamond, 6s CD): Sweep Attack sound. 5-block AoE: 7 initial dmg (→10 planned) + bleed DoT (4 ticks × 6 dmg each = 24 total, →4 × 8 = 32 planned). 10-tick rotating SWEEP_ATTACK + CRIT ring animation (12 points, radius 4).

Reaper's Scythe (Netherite, 14s CD) — COMPLETE 3-PHASE REWORK v1.3.0:

Phase 1 (ticks 0-20) — Dark Summoning Vortex: 3 contracting spiral arms of SOUL_FIRE_FLAME (radius 10→0). Rising SOUL_FIRE_FLAME column at center. Every 4 ticks: enemies pulled toward center + Wither III (100t) + Slowness I (60t). Purple DUST on enemies. Warden Heartbeat every 10 ticks.
Phase 2 (ticks 20-60) — Soul Reap Cyclone: Dual spinning blades of SOUL_FIRE_FLAME (0.5→8 blocks). Edge: SOUL + ENCHANT. Core: purple DUST. 24-point rotating ring at perimeter. Rising spiral at center. Every 5 ticks: 12 dmg (→16 planned) to all enemies + Wither III + Slowness II + player heals 3 HP/enemy/pulse. = 8 pulses × 12 = 96 base damage per enemy (→128 planned). Heart + TOTEM particles on heal. Sweep + Heartbeat + Wither + Sonic Boom sounds.
Phase 3 (tick 60) — Death Harvest Explosion: 200 SOUL_FIRE_FLAME + 120 SOUL + 5 EXPLOSION_EMITTER + 100 REVERSE_PORTAL + 80 TOTEM particles. 15-block upward soul beam. 20 dmg (→28 planned) AoE to enemies in 12-block radius. Launch 2.0 + Y 1.0. Wither III (160t) + Darkness (100t). Lingering fade for 40 ticks. 3 simultaneous sounds: Warden Sonic Boom + Generic Explode + Lightning Impact.
Total per enemy: 96 + 20 = 116 (→128 + 28 = 156 planned) + Wither III DoT + constant player healing.
Summary message at end showing total damage dealt and HP healed.
SPEAR — "Phantom Lunge" / "Spear of the Void"
Phantom Lunge (Diamond, 7s CD): Player launched forward (velocity ×2.2). CRIT + SOUL_FIRE_FLAME + SWEEP_ATTACK trail. 10 ticks. Pierces enemies: 8 dmg (→12 planned) each + push + Slowness I (40t). Tracks hit entities to avoid double-hit.

Spear of the Void (Netherite, 14s CD): Warden Sonic Boom + Breeze Shoot sounds. 40-tick projectile traveling 1.5 blocks/tick (60 blocks max). SOUL_FIRE_FLAME + REVERSE_PORTAL + ENCHANT visuals. Pierces all enemies: 18 dmg (→25 planned) each + Slowness II (80t) + Weakness I (60t). Records hit locations. On solid block or tick 40: detonation — EXPLOSION_EMITTER + 50 SOUL_FIRE_FLAME in 5-block radius for 10 dmg (→15 planned) + knockback 1.5. Chain explosions at each hit location (delayed 5 ticks each): EXPLOSION + 20 SOUL_FIRE_FLAME for 6 dmg (→10 planned) in 3-block radius.

BOW — "Arrow Storm" / "Celestial Volley"
Arrow Storm (Diamond, 8s CD): 5 waves (3 arrows each = 15 arrows total). Arrows spread in cone from player direction. Each arrow: 6 dmg (→9 planned). CRIT particles. Arrow sounds with rising pitch. Arrows set to DISALLOWED pickup, life=40, yellow color.

Celestial Volley (Netherite, 16s CD): Target = block player is looking at (30 blocks) or 20 blocks forward. 20-tick marker beam: rising triple-helix of SOUL_FIRE_FLAME + END_ROD from above. After 20 ticks: 8 waves (4 arrows each = 32 arrows raining from Y+15). Each arrow: 10 dmg (→15 planned), fire ticks 100. AoE impact: 8 dmg (→12 planned) per wave to enemies in 6-block radius. FLAME + CRIT particles. Lightning Impact + Arrow Hit sounds.

CROSSBOW — "Chain Shot" / "Thunder Barrage"
Chain Shot (Diamond, 8s CD): Fires initial bolt that travels forward. On first enemy hit: 8 dmg (→12 planned). Then chains to nearest unhit enemy within 8 blocks up to 5 times: 8 dmg (→12 planned) per chain. ELECTRIC_SPARK chain visual between targets. CRIT particles on each hit. Slowness I (30t) per target.

Thunder Barrage (Netherite, 16s CD): 5 bursts (2 bolts each = 10 bolts total). Each bolt: 12 dmg (→18 planned), fire ticks 60, glowing. ELECTRIC_SPARK particles. Crossbow sounds with pitch variation. After 25 ticks: delayed AoE at target location — EXPLOSION_EMITTER + 50 ELECTRIC_SPARK + FLASH particles. 14 dmg (→20 planned) in 7-block radius + knockback 1.5 + Y 0.8 + Slowness II (80t) + fire (80t). Lightning Impact + Generic Explode sounds.

Helper: drawChainParticles(Location from, Location to, World): Draws ELECTRIC_SPARK particles every 0.5 blocks between two points with random offset.

TRIDENT — "Tidal Surge" / "Poseidon's Wrath"
Tidal Surge (Diamond, 8s CD): Splash + Trident Throw sounds. 15-tick wave traveling forward (1.2 blocks/tick). SPLASH + DRIPPING_WATER + BUBBLE_POP particles in 3-block wide wave. 8 dmg (→12 planned) to enemies + push forward + Y 0.5.

Poseidon's Wrath (Netherite, 16s CD): Warden Sonic Boom + Elder Guardian Curse + Splash sounds. Resistance II (100t). 3 expanding wave rings (delayed 10 ticks apart). Wave 1: 5-block radius, 16 dmg (→24 planned). Wave 2: 8-block radius, 13 dmg (→18 planned). Wave 3: 11-block radius, 10 dmg (→14 planned). Each: 10-tick ring animation with SPLASH + BUBBLE_POP. Enemies: knockback + Y 0.6-1.0 + Slowness II (80t). After 35 ticks: lightning strike effect at center + 50 ELECTRIC_SPARK. 10 dmg (→15 planned) in 10-block radius + Slowness III (100t).

MACE — "Ground Slam" / "Meteor Strike"
Ground Slam (Diamond, 8s CD): Heavy Mace Smash + Iron Golem Attack sounds. 10-tick shockwave ring (0.8 radius/tick) of BLOCK(STONE) + CAMPFIRE_SMOKE. 10 dmg (→15 planned) in 6-block radius + knockback 1.2 + Y 0.8 + Slowness II (60t). 5 EXPLOSION particles.

Meteor Strike (Netherite, 18s CD) — FIXED v1.3.0:

Protection: Resistance 255 (complete immunity) for 200 ticks + Slow Falling for 60 ticks.
Launch (tick 0): Player velocity Y=3.5 (was 2.5 in old version). Heavy Mace Smash + Warden Sonic Boom sounds. 20-tick ascending FLAME + LAVA trail.
Slam (tick 25): Slow Falling removed. Velocity set to Y=-4.0. Ender Dragon Growl sound. Descending FLAME + LAVA + SMOKE + orange DUST meteor trail.
Impact (on ground): Fall distance calculated → damage = 15 base (→20 planned) + 0.5/block fallen (→0.7 planned). Typical total: 20-35 damage. 10-block AoE with distance-scaled damage (30%-100% based on proximity). 15-tick expanding shockwave of BLOCK(NETHERRACK) + FLAME rings. 5 EXPLOSION_EMITTER + 80 LAVA + 100 FLAME + 50 SMOKE + 60 TOTEM particles. 4 sounds: Heavy Mace Smash + Generic Explode + Lightning Impact + Warden Sonic Boom. Enemies: knockback 2.0 × proximity + Y 0.8-1.3 + Slowness III (80t) + Weakness I (60t).
Safety: Resistance 255 removed 20 ticks AFTER landing. Player cannot die during the ability.
SECTION 8: COMPLETE config.yml (ALL VALUES)
Copy# ==============================================
# JGlimsPlugin v1.3.0 Configuration
# ==============================================

mob-difficulty:
  enabled: true
  baseline-health-multiplier: 1.0
  baseline-damage-multiplier: 1.0
  distance:
    350:  { health: 1.5, damage: 1.3 }
    700:  { health: 2.0, damage: 1.6 }
    1000: { health: 2.5, damage: 1.9 }
    2000: { health: 3.0, damage: 2.2 }
    3000: { health: 3.5, damage: 2.5 }
    5000: { health: 4.0, damage: 3.0 }
  biome:
    pale-garden: 2.0
    deep-dark: 2.5
    swamp: 1.4
    nether-wastes:    { health: 1.7, damage: 1.7 }
    soul-sand-valley: { health: 1.9, damage: 1.9 }
    crimson-forest:   { health: 2.0, damage: 2.0 }
    warped-forest:    { health: 2.0, damage: 2.0 }
    basalt-deltas:    { health: 2.3, damage: 2.3 }
    end:              { health: 2.5, damage: 2.0 }

boss-enhancer:
  ender-dragon:    { health: 3.5, damage: 3.0 }
  wither:          { health: 1.0, damage: 1.0 }
  warden:          { health: 1.0, damage: 1.0 }
  elder-guardian:   { health: 2.5, damage: 1.8 }

creeper-reduction: { enabled: true, cancel-chance: 0.5 }
pale-garden-fog:   { enabled: true, check-interval: 40 }

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

mob-book-drops:
  enabled: true
  hostile-chance: 0.05
  boss-custom-chance: 0.15
  looting-bonus-regular: 0.02
  looting-bonus-boss: 0.05

blessings:
  c-bless:   { max-uses: 10, heal-per-use: 1 }
  ami-bless: { max-uses: 10, damage-percent-per-use: 2.0 }
  la-bless:  { max-uses: 10, defense-percent-per-use: 2.0 }

anvil: { remove-too-expensive: true, xp-cost-reduction: 0.5 }

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

drop-rate-booster: { trident-drop-chance: 0.35, breeze-wind-charge-min: 2, breeze-wind-charge-max: 5 }
villager-trades: { enabled: true, price-reduction: 0.50, disable-trade-locking: true }

king-mob:
  enabled: true
  spawns-per-king: 100        # CHANGED v1.3.0: was 500
  health-multiplier: 10.0
  damage-multiplier: 3.0
  diamond-drop-min: 3
  diamond-drop-max: 9

axe-nerf: { enabled: true, attack-speed: 0.5 }
weapon-mastery: { enabled: true, max-kills: 1000, max-bonus-percent: 10.0 }

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

guilds: { enabled: true, max-members: 10, friendly-fire: false }
best-buddies: { dog-armor-damage-reduction: 0.95 }
super-tools: { iron-bonus-damage: 1.0, diamond-bonus-damage: 2.0, netherite-bonus-damage: 2.0, netherite-per-enchant-bonus-percent: 2.0 }

ore-detect:
  radius-diamond: 8
  radius-netherite: 12
  ancient-debris-radius-diamond: 24
  ancient-debris-radius-netherite: 40
  duration-ticks: 200

weapon-abilities:
  ender-dragon-damage-reduction: 0.30
  netherite-enchant-bonus-percent: 2.0
Copy
SECTION 9: COMPLETE PDC KEY REFERENCE
Key Name	Type	Owner Class	Description
is_battle_sword	BYTE	BattleSwordManager	Battle sword marker
battle_sword_damage	NamespacedKey (modifier)	BattleSwordManager	AttributeModifier key
battle_sword_speed	NamespacedKey (modifier)	BattleSwordManager	AttributeModifier key
is_battle_axe	BYTE	BattleAxeManager	Battle axe marker
battle_axe_damage	NamespacedKey (modifier)	BattleAxeManager	AttributeModifier key
battle_axe_speed	NamespacedKey (modifier)	BattleAxeManager	AttributeModifier key
is_battle_pickaxe	BYTE	BattlePickaxeManager	Battle pickaxe marker
battle_pickaxe_damage	NamespacedKey (modifier)	BattlePickaxeManager	AttributeModifier key
battle_pickaxe_speed	NamespacedKey (modifier)	BattlePickaxeManager	AttributeModifier key
battle_shovel	BOOLEAN	BattleShovelManager	Battle shovel marker (note: BOOLEAN, not BYTE)
is_battle_bow	BYTE	BattleBowManager	Battle bow marker
is_battle_crossbow	BYTE	BattleBowManager	Battle crossbow marker
is_battle_mace	BYTE	BattleMaceManager	Battle mace marker
battle_mace_damage	NamespacedKey (modifier)	BattleMaceManager	AttributeModifier key
battle_mace_speed	NamespacedKey (modifier)	BattleMaceManager	AttributeModifier key
is_battle_trident	BYTE	BattleTridentManager	Battle trident marker
battle_trident_damage	NamespacedKey (modifier)	BattleTridentManager	AttributeModifier key
battle_trident_speed	NamespacedKey (modifier)	BattleTridentManager	AttributeModifier key
is_battle_spear	BYTE	BattleSpearManager	Battle spear marker
is_sickle	BYTE	SickleManager	Sickle marker
sickle_damage	NamespacedKey (modifier)	SickleManager	AttributeModifier key
sickle_speed	NamespacedKey (modifier)	SickleManager	AttributeModifier key
super_tool_tier	INTEGER (0/1/2/3)	SuperToolManager	Super tier for all weapons except spears
super_tool_damage	DOUBLE	SuperToolManager	Stored damage value
super_tool_speed	DOUBLE	SuperToolManager	Stored speed value
super_elytra_durability	INTEGER	SuperToolManager	Elytra durability save %
super_spear_tier	INTEGER (0/1/2/3)	SpearManager	Super tier for spears only
bloodthirst_active	LONG (expiry ms)	WeaponAbilityListener	Bloodthirst ability window
best_buddies_applied	BYTE	BestBuddiesListener	Marks wolf as having BB effects
c_bless_item	BYTE	BlessingManager/Listener	C's Bless item marker
ami_bless_item	BYTE	BlessingManager/Listener	Ami's Bless item marker
la_bless_item	BYTE	BlessingManager/Listener	La's Bless item marker
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
All 52 enchant keys	INTEGER (level)	CustomEnchantManager	e.g., vampirism, bleed, etc.
SECTION 10: PAPERMC 1.21.11 API RULES (CRITICAL)
Adventure API mandatory — Component.text() for names/lore, NOT legacy § codes. meta.displayName(Component.text(...)), meta.lore(List<Component>). Exception: SpearManager still uses legacy meta.setDisplayName() — should be migrated.
AnvilView API — AnvilView view = (AnvilView) event.getView(); NOT deprecated AnvilInventory.
Particle.SOUL_FIRE_FLAME — Requires Float data in PaperMC 1.21.11 in some contexts. All Particle.DRAGON_BREATH replaced with Particle.SOUL (no data required). This was causing runtime exceptions.
Attribute enum — Attribute.MAX_HEALTH, Attribute.ATTACK_DAMAGE, Attribute.ATTACK_SPEED, Attribute.ARMOR (NOT deprecated GENERIC_* names).
AttributeModifier — Constructor: new AttributeModifier(NamespacedKey, value, Operation, EquipmentSlotGroup). The NamespacedKey constructor (not UUID) is the current API.
EquipmentSlot.BODY — For wolf armor: wolf.getEquipment().getItem(EquipmentSlot.BODY).
player.spawnParticle() — Per-player rendering (only caster sees). Use for ore detection, personal buffs. world.spawnParticle() for globally visible effects.
Material.WOODEN_SPEAR, STONE_SPEAR, etc. — Spears are native to 1.21.11 (from the Bundles of Bravery update).
meta.setEnchantmentGlintOverride(true) — Forces enchant glint without actual enchantments.
ItemFlag.HIDE_ATTRIBUTES — Hides default attribute tooltip, replaced by custom lore.
SECTION 11: BUGS — SOLVED, PENDING, AND THEIR EXACT FIXES
BUG A: Super Sword/Pickaxe/Shovel Recipes Don't Register (HIGH PRIORITY — NOT YET DEPLOYED)
Status: Fix designed and provided. NOT deployed to server.

Root cause: In RecipeManager.registerSuperToolRecipes(), lines for swords, pickaxes, and shovels call registerAllSuperTiers(Material, String), which creates a plain new ItemStack(baseMat) WITHOUT the battle PDC marker. When SuperToolManager.createSuperTool() internally calls isBattleItem(), it returns false → createSuperTool() returns null → recipe never registered.

The other weapons (axes, sickles, spears, trident, bow, crossbow, mace) correctly use registerSuperTiersForBattleItem(ItemStack, Material, String) which passes an actual battle item with PDC.

Exact code location: RecipeManager.java, method registerSuperToolRecipes(), lines that begin with:

Copyfor (Material swordMat : battleSwordManager.getBattleSwordTiers()) {
    registerAllSuperTiers(swordMat, "battle_sword_" + swordMat.name().toLowerCase());
}
(Same pattern for pickaxes and shovels.)

Exact fix — replace those 3 loops with:

Copyfor (Material swordMat : battleSwordManager.getBattleSwordTiers()) {
    ItemStack battleSword = battleSwordManager.createBattleSword(swordMat);
    if (battleSword == null) continue;
    registerSuperTiersForBattleItem(battleSword, swordMat, "battle_sword_" + swordMat.name().toLowerCase());
}
for (Material pickMat : battlePickaxeManager.getBattlePickaxeTiers()) {
    ItemStack battlePick = battlePickaxeManager.createBattlePickaxe(pickMat);
    if (battlePick == null) continue;
    registerSuperTiersForBattleItem(battlePick, pickMat, "battle_pickaxe_" + pickMat.name().toLowerCase());
}
for (BattleShovelManager.ShovelTier tier : BattleShovelManager.ShovelTier.values()) {
    ItemStack battleShovel = battleShovelManager.createBattleShovel(tier);
    if (battleShovel == null) continue;
    registerSuperTiersForBattleItem(battleShovel, tier.getShovelMaterial(), "battle_shovel_" + tier.name().toLowerCase());
}
Keep registerAllSuperTiers() ONLY for Elytra and Shield (they have no battle step).

BUG B: BestBuddiesListener Registration
Status: ALREADY FIXED in current JGlimsPlugin.java on GitHub. The line pm.registerEvents(new BestBuddiesListener(this, configManager), this); EXISTS in the current code (confirmed by fetching the full main class). This was originally flagged as missing but has since been added.

BUG C: SkinsRestorer Not Working (NOT YET FIXED)
Status: Not deployed.

Root cause: Server runs ONLINE_MODE=false (required for Geyser/Bedrock crossplay). Skins require SkinsRestorer. Docker logs showed "ambiguous plugin name" because two copies existed: plugins/.paper-remapped/SkinsRestorer.jar and plugins/.paper-remapped/SkinsRestorer-15.10.1.jar.

Fix procedure:

Copycd ~/minecraft-server/data/plugins/
sudo rm -f SkinsRestorer*.jar
sudo rm -rf SkinsRestorer/ .paper-remapped/SkinsRestorer*
sudo wget -O SkinsRestorer.jar "https://cdn.modrinth.com/data/TsLS8Py5/versions/eweOlDty/SkinsRestorer.jar"
ls -lh SkinsRestorer.jar   # should be ~7.78 MB
docker restart mc-crossplay
sleep 30
docker logs mc-crossplay --since 1m 2>&1 | grep -i "skin"
# In-game: /skin <YourMojangUsername>
If Geyser is used, ensure disableOnJoinSkins: false in plugins/SkinsRestorer/config.yml.

BUG D: WeaponAbilityListener Damage Too Low (PARTIALLY APPLIED)
Status: User applied the first half (sword, axe, pickaxe, shovel, sickle damage values changed in the file they have locally). The second half (spear, bow, crossbow, trident, mace) was NOT applied because the 77KB file truncates when fetched.

What the user has currently: The first half of WeaponAbilityListener with updated damage values for sword through sickle. The second half (spear onward) still has original values from GitHub.

Planned damage changes — complete table:

Weapon	Ability	Current Value	Target Value	Applied?
Sword	Dash Strike per tick	8.0	12.0	YES (user applied)
Sword	Dimensional Cleave per tick	15.0	22.0	YES
Sword	Dimensional Cleave final burst	25.0	35.0	YES
Axe	Ragnarok wave 1/2/3	20/16/12	30/24/18	YES
Axe	Bloodthirst	buff only	buff + Speed I	YES
Shovel	Earthen Wall	6.0	10.0	YES
Shovel	Tectonic Upheaval	14.0	20.0	YES
Sickle	Harvest Storm initial	7.0	10.0	YES
Sickle	Harvest Storm bleed/tick	6.0	8.0	YES
Sickle	Reaper's Scythe pulse	12.0	16.0	YES
Sickle	Reaper's Scythe final	20.0	28.0	YES
Spear	Phantom Lunge	8.0	12.0	NO
Spear	Spear of the Void pierce	18.0	25.0	NO
Spear	Spear of the Void detonation	10.0	15.0	NO
Spear	Chain explosion per hit	6.0	10.0	NO
Bow	Arrow Storm per arrow	6.0	9.0	NO
Bow	Celestial Volley per arrow	10.0	15.0	NO
Bow	Celestial Volley AoE per wave	8.0	12.0	NO
Crossbow	Chain Shot initial + chain	8.0	12.0	NO
Crossbow	Thunder Barrage bolt damage	12.0	18.0	NO
Crossbow	Thunder Barrage AoE	14.0	20.0	NO
Trident	Tidal Surge	8.0	12.0	NO
Trident	Poseidon's Wrath wave 1/2/3	16/13/10	24/18/14	NO
Trident	Poseidon's lightning	10.0	15.0	NO
Mace	Ground Slam	10.0	15.0	NO
Mace	Meteor Strike base	~15	20.0	NO
Mace	Meteor Strike per-block mult	0.5	0.7	NO
Strategy for next chat: The file is 77,345 bytes. The crawler truncates at ~10K tokens. Fetch the raw URL to get the first ~60% (verified: it covers through sickle Reaper's Scythe Phase 3 linger ticks). Then provide only the remaining methods (spear, bow, crossbow, trident, mace) as replacement code with updated damage values. The user can paste them after the sickle section.

SECTION 12: COMMANDS REFERENCE (COMPLETE)
/guild (Player-only, no permission required)
Subcommand	Usage	Description
create	/guild create <name>	Create a new guild (you become leader)
invite	/guild invite <player>	Invite player to your guild (leader only)
join	/guild join	Accept pending invitation
leave	/guild leave	Leave your current guild
kick	/guild kick <player>	Kick member (leader only)
disband	/guild disband	Destroy the guild (leader only)
info	/guild info	Show your guild's details
list	/guild list	List all guilds on the server
/jglims (Requires jglims.admin permission, default: op)
Subcommand	Usage	Description
reload	/jglims reload	Reload config.yml from disk
stats	/jglims stats <player>	Show blessing stats for a player
enchants	/jglims enchants	List all 52 custom enchantments
sort	/jglims sort	Info message about shift-click sorting
mastery	/jglims mastery	Show your weapon mastery progress (player-only)
SECTION 13: PLANNED FUTURE WORK
New Enchantments (12 planned for Phase 9+)
Category	Enchant	Max	Conflicts	Effect
Sickle	SOUL_HARVEST	3	↔GREEN_THUMB	Wither I-III on hit + Regen I-III for player while Wither active on target
Sickle	REAPING_CURSE	3	↔REPLENISH	Lingering Instant Damage cloud at corpse location on kill
Sickle	CROP_REAPER	1	—	Kills have 15%+5%/Looting chance to drop seeds/wheat/carrots/potatoes
Shovel	BURIAL	3	↔HARVESTER	Slowness III + Blindness I for 1-3s on hit
Shovel	EARTHSHATTER	3	—	Breaking a block sends shockwave breaking identical blocks in 1-3 block radius
Sword	FROSTBITE_BLADE	3	↔VENOMSTRIKE	Slowness I-III + freeze visual on hit
Axe	WRATH	3	↔CLEAVE	Consecutive hits within 3s on same target: +10/20/30% damage, resets on switch
Pickaxe	PROSPECTOR	3	↔AUTO_SMELT	5/10/15% chance double ore yield (stacks with Fortune)
Trident	TSUNAMI	3	↔FROSTBITE	Thrown hit creates water burst pushing mobs 3/5/7 blocks + Slowness I 2s (rain/water)
Bow	FROSTBITE_ARROW	3	↔EXPLOSIVE_ARROW	Arrows apply Slowness I-III + extinguish fire
Mace	TREMOR	3	↔GRAVITY_WELL	On hit, mobs within 2/4/6 blocks get Mining Fatigue II for 3s
Spear	PHANTOM_PIERCE	3	↔SKEWERING	Charged spear attacks pierce 1/2/3 targets dealing full damage
Custom Textures via GeyserMC + Resource Packs (Final Phase)
Custom weapon models/textures for battle and super items visible to both Java and Bedrock players.

SECTION 14: INITIALIZATION ORDER IN JGlimsPlugin.onEnable()
The exact order matters because of dependencies:

1. ConfigManager (must be first — all managers read config)
2. CustomEnchantManager (no deps beyond plugin)
3. BlessingManager (no deps beyond plugin)
4. SickleManager (no deps beyond plugin)
5. BattleAxeManager (no deps beyond plugin)
6. BattleBowManager (no deps beyond plugin)
7. BattleMaceManager (no deps beyond plugin)
8. BattleSwordManager (no deps beyond plugin)
9. BattlePickaxeManager (no deps beyond plugin)
10. BattleTridentManager (no deps beyond plugin)
11. BattleSpearManager (no deps beyond plugin)
12. SuperToolManager (no deps beyond plugin — but checks all battle PDC keys)
13. SpearManager (deps: plugin, configManager)
14. BattleShovelManager (deps: plugin, configManager)
15. RecipeManager (deps: ALL weapon managers + spearManager — 12 constructor args)
    → registerAllRecipes() called immediately
16. VanillaRecipeRemover.remove(this)
17. MobDifficultyManager (deps: plugin, configManager)
18. KingMobManager (deps: plugin, configManager)
19. BloodMoonManager (deps: plugin, configManager)
20. WeaponMasteryManager (deps: plugin, configManager)
21. GuildManager (deps: plugin, configManager)
22. Register ALL 22 event listeners
23. Start PaleGardenFogTask (if enabled)
24. Start BloodMoonManager scheduler (if enabled)
SECTION 15: EXACT LISTENER REGISTRATION ORDER
Copypm.registerEvents(new AnvilRecipeListener(this, enchantManager), this);
pm.registerEvents(new EnchantmentEffectListener(this, enchantManager), this);
pm.registerEvents(new SoulboundListener(this, enchantManager), this);
pm.registerEvents(new BlessingListener(this, blessingManager), this);
pm.registerEvents(mobDifficultyManager, this);
pm.registerEvents(new BossEnhancer(this, configManager), this);
pm.registerEvents(kingMobManager, this);
pm.registerEvents(new InventorySortListener(this), this);
pm.registerEvents(new EnchantTransferListener(this, enchantManager), this);
pm.registerEvents(new LootBoosterListener(this, configManager), this);
pm.registerEvents(new DropRateListener(this, configManager), this);
pm.registerEvents(new VillagerTradeListener(this, configManager), this);
pm.registerEvents(sickleManager, this);
pm.registerEvents(battleAxeManager, this);
pm.registerEvents(battleShovelManager, this);
pm.registerEvents(superToolManager, this);
pm.registerEvents(recipeManager, this);
pm.registerEvents(weaponMasteryManager, this);
pm.registerEvents(bloodMoonManager, this);
pm.registerEvents(new GuildListener(this, guildManager), this);
pm.registerEvents(new WeaponAbilityListener(this, configManager, enchantManager, superToolManager, spearManager, battleShovelManager), this);
pm.registerEvents(new BestBuddiesListener(this, configManager), this);
SECTION 16: INSTRUCTIONS FOR THE NEXT CHAT
Paste this entire document as the first message in the new chat.

First task: Fix RecipeManager.java — Apply the 3-loop fix described in Bug A. This is the highest-priority bug. Only 6 lines change.

Second task: Complete WeaponAbilityListener damage buff — The user has a local copy with the first half (sword through sickle) already buffed. The second half (spear, bow, crossbow, trident, mace) needs the damage values updated. Strategy: fetch the raw file URL, note where it truncates (~sickle linger ticks), then provide ONLY the remaining methods as a replacement block.

Third task: Fix SkinsRestorer — Follow the clean install procedure.

Fourth task: Begin Phase 9+ — Add the 12 new enchantments. This involves: adding to EnchantmentType.java enum, adding conflict entries to CustomEnchantManager.java, adding anvil recipes to AnvilRecipeListener.java, implementing behaviors in EnchantmentEffectListener.java.

Always read GitHub first. Raw URL pattern: https://raw.githubusercontent.com/JGlims/JGlimsPlugin/refs/heads/main/src/main/java/com/jglims/plugin/{path}. The repository tree API: https://api.github.com/repos/JGlims/JGlimsPlugin/git/trees/main?recursive=1.

The 77KB file problem. WeaponAbilityListener.java is 77,345 bytes. The crawler tool truncates at ~10K tokens per fetch. You can reliably get the first ~60% (through sickle Reaper's Scythe). For the rest, use the damage values in this document to reconstruct or provide surgical patches.

Never break what works. All PDC keys, all enchantments, all particle effects, all sounds, all recipes must be preserved. Test checklist: battle crafting for all 10 types, super crafting (especially swords/pickaxes/shovels after fix), battle→battle re-craft blocked, vanilla→super blocked, abilities trigger for Diamond+Netherite, ore detection works, blood moon triggers, king mobs spawn, blessings work, wolf armor works.

SpearManager inconsistency. SpearManager uses legacy meta.setDisplayName() with § color codes instead of Adventure API Component.text(). This should be migrated for consistency but is not a runtime bug.

Axe nerf note. The axe attack speed is nerfed to 0.5 via config + EnchantmentEffectListener. This is intentional to make swords viable. Battle axes have their own 0.9 speed via AttributeModifier which overrides this for battle axes specifically.
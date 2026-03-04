JGlimsPlugin — DEFINITIVE PROJECT SUMMARY & REFERENCE DOCUMENT
Version: 1.3.0 (in development) Author: JGlims Repository: https://github.com/JGlims/JGlimsPlugin Date: March 4, 2026 Build Status: ✅ BUILD SUCCESSFUL — Phase 1 complete, Battle-for-All refactor complete, ready for Phase 2

TABLE OF CONTENTS
Server Infrastructure
Build & Deploy Workflow
Docker Commands
Complete File Inventory & Package Structure
Uniform Weapon Progression System
All 52 Custom Enchantments (+ Planned Expansions)
Weapon Abilities
Mob Difficulty, Bosses, Events
Additional Systems (Guilds, Blessings, Loot, Mastery, etc.)
Configuration Defaults
Commands
All PDC Keys
What Was Done in the Previous Chat
FUTURE: New Enchantments, Items & Ideas (Phase 9+)
FUTURE: Custom Textures via GeyserMC + Rainbow (Final Phase)
Key Links
Step-by-Step Phase Plan
Instructions for the Next Chat
1. SERVER INFRASTRUCTURE
Hardware: OCI VM — 4 OCPUs, 24 GB system RAM, 47 GB boot disk, Ubuntu.

Docker container: mc-crossplay running itzg/minecraft-server:latest.

Key environment variables: EULA=TRUE, TYPE=PAPER, VERSION=1.21.11, MEMORY=8G, ONLINE_MODE=false, RCON_PASSWORD=JGlims2026Rcon, PLUGINS pointing to Geyser-Spigot and Floodgate-Spigot auto-downloads from download.geysermc.org.

Ports: 25565/TCP (Java), 19132/UDP (Bedrock), 25575/TCP (RCON).

Installed plugins: JGlimsPlugin v1.3.0 (our plugin), Geyser-Spigot v2.9.4, Floodgate v2.2.5, Chunky v1.4.40. SkinsRestorer v15.10.1 pending installation.

World data: ~/minecraft-server/data/ with world, world_nether, world_the_end.

Performance target: TPS 20 (≥100 FPS equivalent), stay under 6 GB of the 8 GB heap. Never break farms; only alter health/damage/speed on mobs; use lightweight listeners.

2. BUILD & DEPLOY WORKFLOW
Build (Windows):

.\gradlew.bat clean jar
Output JAR: build/libs/JGlimsPlugin-1.3.0.jar

Deploy:

Copyscp build/libs/JGlimsPlugin-1.3.0.jar MinecraftServer:~/
ssh MinecraftServer
cp ~/JGlimsPlugin-1.3.0.jar ~/minecraft-server/data/plugins/
rm ~/minecraft-server/data/plugins/JGlimsPlugin-1.2.0.jar
docker restart mc-crossplay
docker logs mc-crossplay 2>&1 | grep -i jglims
Build configuration (build.gradle): Java 21 toolchain, io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT as compileOnly, Maven Central + PaperMC repo (https://repo.papermc.io/repository/maven-public/), reproducible build, base name JGlimsPlugin.

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
4. COMPLETE FILE INVENTORY & PACKAGE STRUCTURE
All source: src/main/java/com/jglims/plugin/. Resources: src/main/resources/.

Package: com.jglims.plugin (root)
File	Description
JGlimsPlugin.java	Main class. onEnable initializes ALL managers in dependency order, registers ALL listeners, starts scheduled tasks. onCommand handles /guild and /jglims. Accessor methods for every manager.
Package: com.jglims.plugin.config
File	Description
ConfigManager.java	Loads config.yml with defaults for every system. Provides getters for all settings. Backward-compatibility alias methods. loadConfig() called once from JGlimsPlugin.onEnable().
Package: com.jglims.plugin.enchantments
File	Description
EnchantmentType.java	Enum of all 52 custom enchantments with getMaxLevel().
CustomEnchantManager.java	Registry of NamespacedKeys, conflict map, get/set/remove/list/copy enchantments via PersistentDataContainer.
AnvilRecipeListener.java	52 anvil recipes. Uses AnvilView API (not deprecated AnvilInventory). Handles soulbound, book creation, book application, conflict checks, XP cost reduction, "Too Expensive" removal. Includes v1.3.0 spear enchantments.
EnchantmentEffectListener.java	ALL enchantment behaviors: damage-event, defender, projectile, block-break, interact, movement/passive.
SoulboundListener.java	Keep-on-death, lostsoul conversion, one-per-inventory enforcement. Uses AnvilView API.
Package: com.jglims.plugin.weapons
File	Status	Description
BattleSwordManager.java	NEW v1.3.0	Battle Swords (+1 dmg, 1.6 speed). PDC: is_battle_sword.
BattlePickaxeManager.java	NEW v1.3.0	Battle Pickaxes (+1 dmg, retains mining). PDC: is_battle_pickaxe.
BattleTridentManager.java	NEW v1.3.0	Battle Trident (10 dmg, 1.1 speed). PDC: is_battle_trident.
BattleSpearManager.java	NEW v1.3.0	Battle Spears (+1 dmg per tier). PDC: is_battle_spear.
BattleAxeManager.java	Existing	Battle Axes (sickle dmg + 1, 0.9 speed). PDC: is_battle_axe. Blocks stripping.
BattleBowManager.java	Existing	Battle Bow/Crossbow. PDC: is_battle_bow, is_battle_crossbow.
BattleMaceManager.java	Existing	Battle Mace (12 dmg, 0.7 speed). PDC: is_battle_mace.
BattleShovelManager.java	Existing v1.3.0	Battle Shovels (+1.5 dmg, 1.2 speed). PDC: battle_shovel (BOOLEAN). Blocks path-making.
SickleManager.java	Existing	Sickles from hoes (sword dmg + 1, 1.1 speed). PDC: is_sickle. Blocks tilling.
SpearManager.java	Existing v1.3.0	Super spear tiers via PDC super_spear_tier. SpearTier enum.
SuperToolManager.java	MODIFIED v1.3.0	3-tier super system. isBattleItem() checks all 10 PDC keys. createSuperTool() requires battle item. Lore: base + battle(+1) + super = total. Preserves enchantments.
WeaponAbilityListener.java	Existing	Right-click abilities for Diamond/Netherite super. Per-weapon-class with cooldowns/particles/sounds. Dragon exception.
WeaponMasteryManager.java	MODIFIED v1.3.0	10 weapon classes (including spear). Kills/1000 × 10% max bonus.
Package: com.jglims.plugin.crafting
File	Description
RecipeManager.java	MODIFIED v1.3.0 — 12-arg constructor. Registers ALL battle + super recipes. PDC guard blocks vanilla→Super.
VanillaRecipeRemover.java	Removes conflicting vanilla recipes.
Package: com.jglims.plugin.mobs
File	Description
MobDifficultyManager.java	Distance + biome mob scaling. Baseline 1.0/1.0.
BossEnhancer.java	Dragon 3.5×/3.0×, Elder Guardian 2.5×/1.8×, Wither/Warden vanilla.
KingMobManager.java	500 spawns → King (10×hp, 3×dmg, 3-9 diamonds). One per world.
BloodMoonManager.java	15%/night. 1.5×/1.3× mobs, Darkness, double drops. Every 10th: Blood Moon King.
Package: com.jglims.plugin.guilds
File	Description
GuildManager.java	CRUD, persists guilds.yml, max 10 members.
GuildListener.java	Friendly fire prevention.
Package: com.jglims.plugin.blessings
File	Description
BlessingManager.java	3 blessings: C's (health), Ami's (damage%), La's (armor).
BlessingListener.java	Right-click consumption, PDC persistence, join/respawn reapply.
Package: com.jglims.plugin.effects
File	Description
PaleGardenFogTask.java	Every 40 ticks, fog in Pale Garden biome.
Package: com.jglims.plugin.utility
File	Description
InventorySortListener.java	Mouse Tweaks-style shift-click sorting.
EnchantTransferListener.java	Enchanted item + book in anvil = enchantment book, 0 XP. Uses AnvilView.
LootBoosterListener.java	Books in chest loot, mob book drops, guardian shards, echo shards.
DropRateListener.java	Trident drop 35%, Breeze wind charges 2-5.
VillagerTradeListener.java	50% price reduction, trade locking disabled.
Resources
File	Description
plugin.yml	Commands /guild, /jglims, version 1.3.0.
config.yml	Default configuration.
5. UNIFORM WEAPON PROGRESSION SYSTEM (v1.3.0)
The Rule: ALL weapons must become Battle before they can become Super.
Vanilla → Battle (+1 dmg) → Super Iron Battle (+1) → Super Diamond Battle (+2) → Super Netherite Battle (Definitive, +3)
Exceptions: Elytra and Shield — no Battle step, go directly to Super.

Battle Conversion Details
Weapon	Vanilla → Battle	Speed	Recipe Pattern
Sword	4-8 → 5-9	1.6	8 matching material + sword center
Pickaxe	2-6 → 3-7	1.2	8 matching material + pickaxe center
Axe	special formula	0.9	ingredient + block + axe (special pattern)
Shovel	2.5-6.5 → 4-8	1.2	8 matching material + shovel center
Hoe→Sickle	1 → 5-9	1.1	8 matching material + hoe center
Bow	N/A melee	N/A	8 iron ingots + bow center
Crossbow	N/A melee	N/A	8 iron ingots + crossbow center
Mace	11 → 12	0.7	8 breeze rods + mace center
Trident	9 → 10	1.1	8 prismarine shards + trident center
Spear	1-5 → 2-6	0.87-1.54	8 matching material + spear center
Super Tier Bonuses
Tier	Bonus	Ability	Special Rules
Iron	+1	None	—
Diamond	+2 (non-neth) / +1 (neth base)	Right-click ability	—
Netherite	+3 (non-neth) / +2 (neth base)	Definitive ability	No enchant conflicts, +2% dmg/enchant, no limit
Trident exception: Diamond +3, Netherite +5.

Lore format: Attack Damage: [vanilla base] +[battle +1] +[super bonus] = [total]

6. ALL 52 CUSTOM ENCHANTMENTS + PLANNED EXPANSIONS
Current Enchantments (52)
Sword (5): VAMPIRISM(5)↔LIFESTEAL(3), BLEED(3)↔VENOMSTRIKE(3), CHAIN_LIGHTNING(3)↔WITHER_TOUCH

Axe (5): BERSERKER(5)↔BLOOD_PRICE, LUMBERJACK(3), CLEAVE(3), TIMBER(3), GUILLOTINE(3)

Pickaxe (5): VEINMINER(3)↔DRILL(3), AUTO_SMELT(1), MAGNETISM(1), EXCAVATOR(3)

Shovel (1): HARVESTER(3)

Hoe/Sickle (3): GREEN_THUMB(1), REPLENISH(1), HARVESTING_MOON(3)

Bow (4): EXPLOSIVE_ARROW(3)↔HOMING(3), RAPIDFIRE(3), SNIPER(3)

Crossbow (2): THUNDERLORD(5), TIDAL_WAVE(3)

Trident (5): FROSTBITE(3), SOUL_REAP(3), BLOOD_PRICE(3)↔BERSERKER, REAPERS_MARK(3), WITHER_TOUCH(3)↔CHAIN_LIGHTNING

Armor (9): SWIFTNESS(3), VITALITY(3), AQUA_LUNGS(3), NIGHT_VISION(1), FORTIFICATION(3), DEFLECTION(3), SWIFTFOOT(3)↔MOMENTUM, DODGE(3), LEAPING(3)

Elytra (4): BOOST(3), CUSHION(1), GLIDER(1), STOMP(3)

Mace (4): SEISMIC_SLAM(3)↔MAGNETIZE(3), GRAVITY_WELL(3), MOMENTUM(3)↔SWIFTFOOT

Spear (3) — v1.3.0: IMPALING_THRUST(3), EXTENDED_REACH(3), SKEWERING(3) — conflict triangle

Universal (2): SOULBOUND(1), BEST_BUDDIES(1)

PLANNED NEW ENCHANTMENTS (Phase 9+)
These are ideas for expanding the enchantment system to make every weapon feel unique and give players more build diversity:

Sickle — New Enchantments (3 planned):

Name	Max	Mechanic	Conflict
SOUL_HARVEST	3	Applies Wither I-III to hit mob; player receives Regeneration I-III while the Wither is active. A dark counterpart to Vampirism — you drain their life force via decay.	GREEN_THUMB
REAPING_CURSE	3	On kill, spawns a lingering Instant Damage cloud at the corpse location (radius scales with level). Turns the sickle into an AoE denial weapon.	REPLENISH
CROP_REAPER	1	Killing a mob has a chance (15%+5%/Looting) to drop seeds, wheat, carrots, or potatoes. Thematic: the sickle "harvests" living things.	—
Shovel — New Enchantments (2 planned):

Name	Max	Mechanic	Conflict
BURIAL	3	On hit, buries the target in Slowness III + Blindness I for 1-3 seconds (scales with level). Shovels bury things.	HARVESTER
EARTHSHATTER	3	Breaking a block with a shovel sends a shockwave that breaks all identical blocks in a 1-3 block radius (like a shovel version of Veinminer but for soft blocks: dirt, sand, gravel, clay, soul sand).	—
Sword — New Enchantment (1 planned):

Name	Max	Mechanic	Conflict
FROSTBITE_BLADE	3	Applies Slowness I-III and a freezing visual effect on hit. Equivalent to Trident's FROSTBITE but for swords. Allows sword users to access crowd control.	VENOMSTRIKE
Axe — New Enchantment (1 planned):

Name	Max	Mechanic	Conflict
WRATH	3	Each consecutive hit within 3 seconds on the same target increases damage by 10%/20%/30%. Resets if you switch targets or wait too long. Rewards commitment.	CLEAVE
Pickaxe — New Enchantment (1 planned):

Name	Max	Mechanic	Conflict
PROSPECTOR	3	Mining ores has a 5%/10%/15% chance to drop double the normal yield (stacks with Fortune). A passive mining economy booster.	AUTO_SMELT
Trident — New Enchantment (1 planned):

Name	Max	Mechanic	Conflict
TSUNAMI	3	On thrown trident hit, creates a water burst that pushes all mobs in a 3/5/7 block radius away from impact and applies Slowness I for 2s. Only works in rain or water.	FROSTBITE
Bow — New Enchantment (1 planned):

Name	Max	Mechanic	Conflict
FROSTBITE_ARROW	3	Arrows apply Slowness I-III and extinguish fire on hit targets. Equivalent to Trident's FROSTBITE for ranged combat.	EXPLOSIVE_ARROW
Mace — New Enchantment (1 planned):

Name	Max	Mechanic	Conflict
TREMOR	3	On hit, all mobs within 2/4/6 blocks receive Mining Fatigue II for 3 seconds (simulates the ground trembling, slowing their attacks).	GRAVITY_WELL
Spear — New Enchantment (1 planned):

Name	Max	Mechanic	Conflict
PHANTOM_PIERCE	3	Charged spear attacks pierce through the first 1/2/3 targets, dealing full damage to each. Turns the spear into a line-AoE weapon.	SKEWERING
PLANNED NEW ITEMS & RECIPES (Phase 9+)
Item	Recipe Idea	Mechanic
Warden's Echo (accessory)	Echo Shard + Sculk Catalyst + Amethyst Shard in anvil	Worn in offhand: nearby hostile mobs glow (like Spectral) within 16 blocks when sneaking. Uses durability.
Totem of the Blood Moon	1 Totem of Undying + 4 Netherite Scrap + 4 Redstone Blocks	On death during Blood Moon, revive with full health + Strength II for 10s. Single use.
Beacon of the King	9 Diamond Blocks + Nether Star center	Placed at the King Mob's death location: grants all nearby players (32 blocks) Haste II + Luck for 5 minutes. Single use.
Enchanted Shulker Shell	Shulker Shell + 4 Ender Pearls + 4 Chorus Fruit	Craft into a "Portable Ender Chest" — right-click opens ender chest inventory anywhere. 50 uses.
Elemental Arrows (4 types)	Arrow + relevant ingredient (Magma Cream, Snowball, Wind Charge, Glowstone)	Fire Arrow (ignites), Frost Arrow (slows), Wind Arrow (high knockback), Glow Arrow (Spectral effect). Craftable in stacks of 8.
Guild Banner	Banner + guild-specific recipe	Placeable banner that grants guild members within 16 blocks a small buff (Speed I or Resistance I).
7. WEAPON ABILITIES (Super Diamond & Netherite)
Weapon	Diamond Ability	Netherite Definitive
Sword	Dash Strike (dash + path damage, 10s CD)	Dimensional Cleave (12-block rift, massive AoE, 18s CD)
Axe	Bloodthirst (5s lifesteal + haste, 12s CD)	Ragnarok Cleave (10-block shockwave, 3 waves, 22s CD)
Pickaxe	Ore Pulse (detect ores 16-24 block, 15s CD)	Seismic Resonance (40-block Ancient Debris, 25s CD)
Shovel	Earthen Wall (launch enemies + Resistance, 12s CD)	Tectonic Upheaval (12-block eruption, 20s CD)
Sickle	Harvest Storm (spin AoE + bleed, 10s CD)	Reaper's Scythe (dual arc + lifesteal + Wither, 20s CD)
Spear	Phantom Lunge (8-block dash + pierce, 12s CD)	Spear of the Void (30-block projectile + detonation, 20s CD)
Bow	To be implemented	To be implemented
Crossbow	To be implemented	To be implemented
Trident	To be implemented	To be implemented
Mace	To be implemented	To be implemented
Ender Dragon exception: Definitive abilities deal only 30% damage to the Ender Dragon.

8. MOB DIFFICULTY, BOSSES, EVENTS
Baseline: 1.0× health, 1.0× damage (vanilla).

Distance scaling (Overworld): 0-349 = none → 350 = 1.5×/1.3× → incrementally up to 5000 = 4.0×/3.0×.

Biome multipliers: Pale Garden 2.0×, Deep Dark 2.5×, Swamp 1.4×, Nether 1.7-2.3×, End 2.5×/2.0×.

Bosses: Dragon 3.5×/3.0×, Elder Guardian 2.5×/1.8×, Wither/Warden = vanilla.

Creeper reduction: 50% spawn cancel.

King Mob: 500 spawns → King (10×hp, 3×dmg, 3-9 diamonds, gold glowing, no despawn, one per world).

Blood Moon: 15% chance/night, 1.5×/1.3× mobs, Darkness + red particles, double drops/XP. Every 10th: Blood Moon King (Zombie, diamond armor, Netherite sword, 400 HP, 5×dmg, 5-15 diamonds + netherite on death).

9. ADDITIONAL SYSTEMS
Guilds: Create/invite/join/leave/kick/disband/info/list. Max 10 members. Friendly fire disabled. Persists guilds.yml.

Blessings (3): C's Bless (10 uses, +1 heart/use), Ami's Bless (10 uses, +2% dmg/use), La's Bless (10 uses, +2% armor/use). Right-click to consume. Permanent via AttributeModifiers.

Best Buddies: Bone + Diamond → Wolf Armor. Wolf takes 95% less damage, deals 0 damage, gets Regen II.

Weapon Mastery: 10 classes (sword/axe/pickaxe/shovel/sickle/bow/crossbow/trident/mace/spear). Linear: kills/1000 × 10% max dmg bonus.

Inventory Sort: Shift-click empty slot to sort (Mouse Tweaks-style).

Enchant Transfer: Enchanted item + plain book in anvil = book with all enchantments, 0 XP.

Loot Booster: Enchanted books in chest loot. Mob drops: hostile 5%, boss 15% (+looting). Guardian shards, echo shards (40%).

Drop Rate: Trident 35% from Drowned, Breeze 2-5 wind charges.

Villager Trades: 50% price reduction, trade locking disabled.

Axe Nerf: Attack speed 0.5 on all axes.

Pale Garden Fog: Atmospheric effects every 40 ticks.

10. CONFIGURATION DEFAULTS
Copymob-difficulty: { enabled: true, baseline-health: 1.0, baseline-damage: 1.0 }
creeper-reduction: { enabled: true, cancel-chance: 0.5 }
pale-garden-fog: { enabled: true, check-interval: 40 }
loot-booster: { enabled: true, chest-enchanted-book: true, echo-shard-chance: 0.40 }
mob-book-drops: { enabled: true, hostile-chance: 0.05, boss-custom-chance: 0.15 }
blessings: { c-bless: {max-uses: 10, heal: 1}, ami-bless: {max-uses: 10, dmg: 2%}, la-bless: {max-uses: 10, def: 2%} }
anvil: { remove-too-expensive: true, xp-cost-reduction: 0.5 }
drop-rate-booster: { trident-drop-chance: 0.35, breeze-wind-charge: {min: 2, max: 5} }
villager-trades: { enabled: true, price-reduction: 0.50, disable-trade-locking: true }
king-mob: { enabled: true, spawns-per-king: 500, health-mult: 10.0, damage-mult: 3.0, diamond-drop: {min: 3, max: 9} }
axe-nerf: { enabled: true, attack-speed: 0.5 }
weapon-mastery: { enabled: true, max-kills: 1000, max-bonus-percent: 10.0 }
blood-moon: { enabled: true, check-interval: 100, chance: 0.15, mob-health: 1.5, mob-damage: 1.3, boss-every-nth: 10, boss-health-mult: 20.0, boss-damage-mult: 5.0, boss-diamond: {min: 5, max: 15}, double-drops: true }
guilds: { enabled: true, max-members: 10, friendly-fire: false }
dog-armor-reduction: 0.95
super-tool: { iron-bonus: 1.0, diamond-bonus: 2.0, netherite-bonus: 3.0 }
ore-detect: { diamond-radius: 16, netherite-radius: 24, debris-diamond: 24, debris-netherite: 40, duration-ticks: 200 }
11. COMMANDS
/guild (Player-only): create <name>, invite <player>, join, leave, kick <player>, disband, info, list

/jglims: reload, stats <player>, enchants, sort, mastery

12. ALL PDC KEYS
Key	Type	Used By
is_battle_sword	BYTE	BattleSwordManager
is_battle_axe	BYTE	BattleAxeManager
is_battle_pickaxe	BYTE	BattlePickaxeManager
is_sickle	BYTE	SickleManager
battle_shovel	BOOLEAN	BattleShovelManager
is_battle_bow	BYTE	BattleBowManager
is_battle_crossbow	BYTE	BattleBowManager
is_battle_mace	BYTE	BattleMaceManager
is_battle_trident	BYTE	BattleTridentManager
is_battle_spear	BYTE	BattleSpearManager
super_tool_tier	INTEGER (1/2/3)	SuperToolManager
super_spear_tier	INTEGER (1/2/3)	SpearManager
super_elytra_durability	INTEGER	SuperToolManager
boss_damage_mult	DOUBLE	BossEnhancer
mastery_sword thru mastery_spear	INTEGER	WeaponMasteryManager
mastery_damage	(AttributeModifier key)	WeaponMasteryManager
bloodthirst_active	LONG	WeaponAbilityListener
Per-EnchantmentType keys	INTEGER (level)	CustomEnchantManager
c_bless_item, ami_bless_item, la_bless_item	BYTE	RecipeManager
13. WHAT WAS DONE IN THE PREVIOUS CHAT
Phase 1 — Compilation Error Fixes (6 files)
#	File	Fix
1	ConfigManager.java	Added getPaleGardenFogInterval() alias, new config fields, backward-compat getters
2	JGlimsPlugin.java	Fixed WeaponAbilityListener 6-arg constructor, added SpearManager + BattleShovelManager
3	AnvilRecipeListener.java	Migrated 7 deprecated AnvilInventory calls → AnvilView, added spear enchantment recipes
4	SoulboundListener.java	Migrated 2 deprecated calls → AnvilView
5	EnchantTransferListener.java	Migrated 1 deprecated call → AnvilView
Battle-for-All Refactor (8 files)
#	File	Change
1	BattleSwordManager.java	NEW
2	BattlePickaxeManager.java	NEW
3	BattleTridentManager.java	NEW
4	BattleSpearManager.java	NEW
5	SuperToolManager.java	MODIFIED — isBattleItem(), battle-required gating, spear/mace damage tables
6	RecipeManager.java	MODIFIED — 12-arg constructor, all battle + super recipes, PDC guard
7	JGlimsPlugin.java	MODIFIED — 4 new managers, all accessor methods
8	WeaponMasteryManager.java	MODIFIED — spear mastery, all new battle recognition
Both phases: BUILD SUCCESSFUL, 0 errors.

14. FUTURE: New Enchantments, Items & Ideas (Phase 9+)
This section is the creative expansion plan — to be implemented after all core systems are stable. See Section 6 above for the full enchantment expansion table (12 new enchantments planned across all weapon types), including the signature SOUL_HARVEST sickle enchantment (Wither → Regen drain mechanic), FROSTBITE_BLADE for swords, BURIAL for shovels, WRATH for axes, PROSPECTOR for pickaxes, TSUNAMI for tridents, and more.

New item ideas include Warden's Echo (offhand mob detection), Totem of the Blood Moon (Blood Moon revival), Elemental Arrows (4 types), Guild Banners (area buff), Enchanted Shulker Shell (portable ender chest), and a Beacon of the King (King Mob death location buff).

15. FUTURE: Custom Textures via GeyserMC + Rainbow (FINAL PHASE)
The Goal
Give every Battle and Super weapon a unique visual appearance that both Java and Bedrock players can see. A Diamond Battle Sword should look different from a vanilla Diamond Sword. A Netherite Super Definitive weapon should have a dramatically different, glowing model.

How It Works — The GeyserMC Custom Items Pipeline
For Java players: Minecraft 1.21.4+ introduced the item_model component. PaperMC plugins can set this component on any ItemStack (via meta.setItemModel(NamespacedKey)). Java clients use a server resource pack to map that model to a custom texture/3D model. This means our plugin can set item_model on every Battle/Super weapon, and Java players see the custom texture automatically through a resource pack.

For Bedrock players: Geyser's Custom Items API (v2) allows mapping Java item_model values to custom Bedrock items with their own textures. This requires two things: a Geyser mapping JSON file (custom_mappings/mappings.json) and a Bedrock resource pack (placed in Geyser's packs/ folder). Geyser sends the Bedrock resource pack automatically to connecting Bedrock clients.

Tools Available
Tool	What It Does	Status
Rainbow (GeyserMC)	Fabric client mod. Join server, hold custom items, run /rainbow map. Automatically generates both the Geyser mapping JSON and the Bedrock resource pack.	Early development but functional. Download: https://geysermc.org/download/?project=other-projects&rainbow=expanded
Hydraulic (GeyserMC)	Server-side mod for modded Java servers. Auto-generates Bedrock equivalents of mod items.	Early development, NOT for production. Not needed for our plugin-only setup.
Thunder (GeyserMC)	Converts simple Java resource packs (block/item textures) to Bedrock format.	Limited scope but useful for basic texture swaps.
PackConverter (GeyserMC)	Library for converting full Java resource packs to Bedrock.	GitHub: https://github.com/GeyserMC/PackConverter
Implementation Plan (Step by Step)
Step 1 — Plugin-side (Java): Add item_model component to every Battle and Super weapon when created. Example:

Copymeta.setItemModel(new NamespacedKey("jglims", "battle_diamond_sword"));
This gives each custom weapon a unique item model identifier that can be targeted by resource packs.

Step 2 — Java Resource Pack: Create a server resource pack with custom textures for each item model. Structure:

assets/jglims/items/battle_diamond_sword.json     → item model definition
assets/jglims/models/item/battle_diamond_sword.json → model (can reference custom texture)
assets/jglims/textures/item/battle_diamond_sword.png → 16×16 or 32×32 texture
Host the resource pack on a web server and configure PaperMC's server.properties to send it to Java clients.

Step 3 — Geyser Mapping: Create custom_mappings/mappings.json with v2-format definitions mapping each item_model to a Bedrock custom item:

Copy{
  "format_version": 2,
  "items": {
    "minecraft:diamond_sword": [
      {
        "type": "definition",
        "model": "jglims:battle_diamond_sword",
        "bedrock_identifier": "jglims:battle_diamond_sword",
        "display_name": "Diamond Battle Sword",
        "bedrock_options": {
          "icon": "jglims.battle_diamond_sword",
          "display_handheld": true,
          "creative_category": "equipment",
          "creative_group": "itemGroup.name.sword"
        }
      }
    ]
  }
}
Step 4 — Bedrock Resource Pack: Create a Bedrock resource pack with matching textures and item_texture.json:

Copy{
  "resource_pack_name": "JGlimsPlugin",
  "texture_name": "atlas.items",
  "texture_data": {
    "jglims.battle_diamond_sword": {
      "textures": ["textures/items/battle_diamond_sword"]
    }
  }
}
Place in Geyser's packs/ folder. Bedrock players automatically download it on connect.

Step 5 — Use Rainbow to Validate: Install Rainbow on a Fabric client, join the server, hold each custom weapon, run /rainbow map, then /rainbow finish. Compare its auto-generated output against our manual packs to catch any issues.

Texture Design Ideas
Weapon State	Visual Theme
Battle (any material)	Slightly modified vanilla texture with a colored border/glow edge and a small "B" insignia
Super Iron	Silver metallic sheen overlay, iron-colored trim
Super Diamond	Cyan crystalline glow, diamond particle overlay
Super Netherite (Definitive)	Dark crimson/purple aura, molten lava veins in the blade, dramatic glow
Key Constraints
Geyser does NOT auto-convert Java resource packs → must create both manually (or use Rainbow/Thunder).
Bedrock custom items cannot modify vanilla item behavior at runtime — we handle all behavior server-side via PDC.
enable-custom-content: true must be set in Geyser's config.yml.
Resource packs go in plugins/Geyser-Spigot/packs/ on our server.
Mapping files go in plugins/Geyser-Spigot/custom_mappings/.
Links
Resource	URL
Geyser Custom Items v2 Docs	https://geysermc.org/wiki/geyser/custom-items/
Geyser Resource Pack Docs	https://geysermc.org/wiki/geyser/packs/
Rainbow Wiki	https://geysermc.org/wiki/other/rainbow/
Rainbow Download	https://geysermc.org/download/?project=other-projects&rainbow=expanded
Hydraulic Wiki	https://geysermc.org/wiki/other/hydraulic/
Thunder Wiki	https://geysermc.org/wiki/other/thunder/
Bedrock Resource Pack Guide	https://wiki.bedrock.dev/guide/project-setup.html#rp-manifest
Bedrock Custom Item Textures	https://wiki.bedrock.dev/items/items-intro#applying-textures
Example Geyser Mappings Repo	https://github.com/eclipseisoffline/geyser-example-mappings/
PackConverter	https://github.com/GeyserMC/PackConverter
16. KEY LINKS
Resource	URL
GitHub Repository	https://github.com/JGlims/JGlimsPlugin
PaperMC API Javadoc (1.21.11)	https://jd.papermc.io/paper/1.21.11/
AnvilView API	https://jd.papermc.io/paper/1.21.11/org/bukkit/inventory/view/AnvilView.html
PaperMC Downloads	https://papermc.io/downloads/paper
Docker Minecraft Server	https://hub.docker.com/r/itzg/minecraft-server
Docker MC Docs	https://docker-minecraft-server.readthedocs.io/
Geyser Downloads	https://download.geysermc.org/
Geyser Custom Items v2	https://geysermc.org/wiki/geyser/custom-items/
Geyser Resource Packs	https://geysermc.org/wiki/geyser/packs/
Rainbow (Pack Converter)	https://geysermc.org/wiki/other/rainbow/
Hydraulic (Mod Bridge)	https://geysermc.org/wiki/other/hydraulic/
Chunky (Hangar)	https://hangar.papermc.io/pop4959/Chunky/versions/1.4.40
SkinsRestorer (Hangar)	https://hangar.papermc.io/SRTeam/SkinsRestorer
Minecraft Spear Wiki	https://minecraft.wiki/w/Spear
Bedrock Resource Pack Dev	https://wiki.bedrock.dev/guide/project-setup.html
17. STEP-BY-STEP PHASE PLAN
✅ Phase 1 — Compilation Fixes (DONE)
Fixed all errors, migrated to AnvilView API, added spear enchantments.

✅ Phase 1.5 — Battle-for-All Refactor (DONE)
Created 4 new Battle managers, modified SuperToolManager/RecipeManager/JGlimsPlugin/WeaponMasteryManager.

Phase 2 — Missing Systems & Fixes (NEXT)
Implement BestBuddiesListener.java (wolf 95% DR, 0 dmg, Regen II)
Implement remaining weapon abilities (Bow, Crossbow, Trident, Mace)
Verify/polish InventorySortListener
Add Efficiency auto-apply for Super Battle Shovels (I/II/III by tier)
Phase 3 — Recipe System Testing
Verify all battle recipes in-game
Verify all super recipes require battle PDC
Verify sequential upgrade (Super Iron + 8 diamonds → Super Diamond)
Test enchantment preservation through all upgrades
Phase 4 — Super Tool System Polish
Enforce material-matching for tiered weapons
Implement "+2% damage per enchantment" for Netherite Definitive
Implement "no enchantment limit" for Netherite Definitive
Implement "no enchantment conflicts" for Netherite Definitive
Phase 5 — Ability System Completion
All 10 weapon class abilities (complete Bow, Crossbow, Trident, Mace)
Enhanced definitive particles
Verify ore-detect radii
Ability checks only super-tool tier
Phase 6 — Quality & Balance
Axe nerf verification
Lore consistency review
Cross-material prevention
Full enchantment preservation audit
Phase 7 — Testing & Integration
Deploy to creative server
Full regression test (every recipe, enchantment, mob system)
TPS verification
Phase 8 — Go Live
Delete creative world, recreate survival
Install SkinsRestorer
Pre-generate chunks with Chunky
Launch
Phase 9 — Enchantment Expansion
Implement 12 new enchantments (SOUL_HARVEST, BURIAL, FROSTBITE_BLADE, WRATH, PROSPECTOR, etc.)
Implement new items (Warden's Echo, Elemental Arrows, etc.)
Add to AnvilRecipeListener, EnchantmentEffectListener, EnchantmentType enum
Phase 10 — Custom Textures (Final)
Create Java resource pack with custom item models for all Battle/Super weapons
Set item_model component on all custom weapons in plugin code
Create Geyser mapping JSON for Bedrock item definitions
Create Bedrock resource pack with matching textures
Use Rainbow to validate and generate packs
Deploy packs to server, enable enable-custom-content: true in Geyser config
Test on both Java and Bedrock clients
18. INSTRUCTIONS FOR THE NEXT CHAT
When continuing this project in a new chat session, provide this entire document and follow these rules:

Always start by reading the GitHub repository: Fetch the latest source from https://github.com/JGlims/JGlimsPlugin and check every file before making changes. The repo is the source of truth.

Always provide complete file replacements: Never provide partial snippets or "add this here." Every file must be a complete copy-paste replacement with the full file path.

Include full file paths: e.g., src/main/java/com/jglims/plugin/weapons/BattleSwordManager.java.

Work in phases: Follow the phase plan (Phase 2 is next). Complete one phase at a time. Verify build success before proceeding.

Check the PaperMC 1.21.11 API: Use https://jd.papermc.io/paper/1.21.11/ for API verification. Use AnvilView (not deprecated AnvilInventory), Adventure components (not legacy ChatColor), EquipmentSlotGroup for AttributeModifiers, PersistentDataContainer for all data.

Maintain backward compatibility: Never break existing PDC data on items players already have.

Performance matters: TPS 20 target. Avoid heavy tick-frequency operations. Use player-specific spawnParticle to reduce network load.

The uniform rule: ALL weapons require Battle to become Super. Only Elytra and Shield bypass this. SuperToolManager.isBattleItem() is the gatekeeper.

Deliver files in dependency order: ConfigManager → Enchantment system → Weapon managers → SuperToolManager → RecipeManager → JGlimsPlugin → Listeners.

Test the build after every phase: .\gradlew.bat clean jar must produce BUILD SUCCESSFUL with 0 errors.

Deep think before coding: Before writing any file, read the existing version on GitHub, understand all dependencies, plan the changes, then deliver. Explain the reasoning behind every change.

Be complete and explicit: When creating a new system (like BestBuddiesListener), explain what events to listen for, what PDC keys to check, what the behavior should be, and deliver the full file ready to compile.

This document captures the complete state of JGlimsPlugin as of March 4, 2026. The build is green. Phase 2 begins next. The future vision extends through enchantment expansion (Phase 9) and custom crossplay textures via GeyserMC (Phase 10).
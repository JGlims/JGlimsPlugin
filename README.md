JGLIMSPLUGIN — ULTIMATE DEFINITIVE PROJECT SUMMARY v12.0
Compiled: 2026-03-20 | Author: JGlims (jg.melo.lima2005@gmail.com) | Plugin Version: 2.0.0 | Target Version: 3.0.0

A. PROJECT METADATA
Repository: https://github.com/JGlims/JGlimsPlugin

Plugin: JGlimsPlugin v2.0.0 — A comprehensive Minecraft server-side plugin for Paper 1.21+ featuring 63 legendary weapons with unique dual abilities, 13 custom armor sets, 7 power-up consumables, an Infinity Gauntlet system, 64 custom enchantments, 6 random server events, 6 roaming world bosses, 40 procedurally-placed structures, a complete quest system, guild system, and the custom Abyss dimension with a gothic cathedral citadel and multi-phase dragon boss fight. Written in Java, built with Gradle, targeting the PaperMC API.

Server Environment:

The server runs inside a Docker container named mc-crossplay on an Oracle Cloud ARM64 (Ampere) free-tier instance. The public IP is 144.22.198.184. Java Edition connects on port 25565; Bedrock Edition connects on port 19132 via GeyserMC crossplay bridge. The server software is Paper 1.21.11-127 running on Java 25 (OpenJDK 25.0.2+10-LTS, Eclipse Adoptium Temurin). Crossplay is provided by GeyserMC 2.9.4-SNAPSHOT and Floodgate 2.2.5-SNAPSHOT. Other plugins include SkinsRestorer 15.11.0 and Chunky 1.4.40 for pre-generation. The JVM is allocated 8 GB of RAM. Online mode is disabled to support crossplay. The default game mode is creative. Maximum players is 15. View distance is 10 chunks and simulation distance is 6 chunks. Difficulty is set per-world (Hard in the Abyss, configurable elsewhere).

SSH Access: Key file at C:\Users\jgmel\Documents\projects\server_minecraft\ssh-key-2026-02-25.key, user ubuntu@144.22.198.184 (confirmed NOT opc).

Local Development Path: C:\Users\jgmel\Documents\projects\JGlimsPlugin\JGlimsPlugin

Resource Pack Working Directory: C:\Users\jgmel\Documents\projects\JGlimsPlugin\resourcepack-work\JGlimsResourcePack

Server Resource Pack Source (original full pack): C:\Users\jgmel\Documents\projects\JGlimsPlugin\JGlimsResourcePack-v2.1-server

Docker Compose File (on server): /home/ubuntu/minecraft-server/docker-compose.yml

Data Volume Mount: /home/ubuntu/minecraft-server/data:/data (bind mount, NOT a Docker volume)

Build Output: JGlimsPlugin-2.0.0.jar (711,934 bytes). Built using .\gradlew clean build from the local dev path.

Build Configuration (build.gradle): Applies the java plugin, sets group com.jglims.plugin and version 2.0.0, uses Java toolchain version 21, pulls dependencies from Maven Central and the PaperMC Maven repository (https://repo.papermc.io/repository/maven-public/), adds a compile-only dependency on io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT, and configures the jar archive base name to JGlimsPlugin.

Plugin Descriptor (plugin.yml): Name JGlimsPlugin, version 2.0.0, main class com.jglims.plugin.JGlimsPlugin, api-version 1.21, author JGlims. Defines three commands: /jglims (admin command with subcommands: reload, stats, enchants, sort, mastery, legendary, armor, powerup, bosstitles, gauntlet, menu, guia, help — requires jglims.admin permission, defaults to ops), /guild (subcommands: create, invite, join, leave, kick, disband, info, list), and /guia (alias: /guide, gives Portuguese guide books).

B. CURRENT RESOURCE PACK STATE
What Is Currently Live
The server is running resource pack v9.0, hosted on GitHub Releases. This is the latest pack after multiple iterations of fixes.

Current docker-compose.yml settings:

RESOURCE_PACK: https://github.com/JGlims/JGlimsPlugin/releases/download/v9.0/JGlimsResourcePack-v9.0.zip
RESOURCE_PACK_SHA1: 0e6151b68727aff120a9648c8f295078ccfee809
RESOURCE_PACK_ENFORCE: TRUE
CRITICAL RULE: The resource pack URL and SHA-1 are set in docker-compose.yml as environment variables. The Docker image (itzg/minecraft-server) overwrites server.properties from these env vars on EVERY container start. Editing server.properties directly is useless — you MUST edit docker-compose.yml and run docker compose down && docker compose up -d for changes to persist.

What Works Right Now (Corrected Status as of 2026-03-20)
All items below are confirmed working:

All 63 legendary weapon textures and 3D models — sourced from the Fantasy Weapons and Blades of Majestica asset packs, converted to the 1.21.4+ assets/minecraft/items/*.json format using string-based custom_model_data. Every weapon renders its correct custom model in hand and inventory.

All 63 legendary weapon abilities — each weapon has a primary ability (right-click) and an alternate ability (crouch + right-click), all functional.

All 13 armor set textures — both inventory icons AND worn textures are now working correctly. The worn armor textures use the 1.21.4+ equippable component system with assets/minecraft/equipment/*.json files pointing to PNGs under assets/minecraft/textures/entity/equipment/humanoid/ and humanoid_leggings/. This was previously broken but has been fixed.

All 7 power-up item textures — Heart Crystal, Soul Fragment, Titan's Resolve, Phoenix Feather, KeepInventorer, Vitality Shard, and Berserker Mark all display their correct custom textures. The PowerUpManager.java now uses string-based CustomModelDataComponent correctly.

Infinity Gauntlet and Thanos Glove textures — custom 3D models render correctly.

Infinity Stone textures — all 6 stones (Power, Space, Reality, Soul, Time, Mind) display correctly. Fixed in resource pack v7.0.

Abyss portal activation — the 4×5 purpur frame + Abyssal Key interaction now works correctly. Players can right-click a purpur frame with the Abyssal Key item, the portal detects the valid frame shape, fills the interior with END_GATEWAY blocks (temporary, 30-second duration), and teleports the player into the Abyss dimension when they step through.

Abyss Dragon basic item model — a basic 3D blocky dragon model exists at assets/minecraft/models/item/abyss/abyss_dragon.json with a 64×64 texture, mapped to CustomModelData ID 40000 / string "abyss_dragon". This renders on the ItemDisplay entity in-game but is visually low-quality (11 cuboid elements, very blocky).

Plugin loads cleanly — JGlimsPlugin 2.0.0 initializes without errors, the Abyss dimension is created (world_abyss), all events are loaded, all listeners are registered.

What Does NOT Work / Needs Rework
The Abyss dimension / dragon / citadel experience as a whole — while the portal activation works, the actual Abyss gameplay experience has critical issues that need a comprehensive rework:

The Abyss Dragon model quality is poor. The current model (abyss_dragon.json) is a basic 11-element blocky representation made of cuboids. It does not look like a convincing dragon. The plan is to replace it with a high-quality Sketchfab-sourced Blockbench model (see Section Q below).

The Abyss Dragon boss fight mechanics need tuning. The AbyssDragonBoss.java fight controller works but the visual experience is underwhelming because the dragon model is a small blocky shape scaled up via ItemDisplay. Combat feel needs improvement.

The Abyssal Citadel (AbyssCitadelBuilder.java, 54 KB) generates a massive gothic cathedral structure but may have performance issues during generation and visual refinement needs.

One Creation Splitter ability has a bug — needs identification of which ability (primary or alt) in LegendaryPrimaryAbilities.java or LegendaryAltAbilities.java is malfunctioning.

Missing animated textures for awakened weapons — Whisperwind Awakened, Creation Splitter Awakened, and Edge of the Astral Plane should have animated texture variants (.png.mcmeta animation files) but do not. Excalibur Awakened has its animated texture working (fixed in v4.0.6–v4.0.9).

The Abyssal Key item uses a vanilla Echo Shard texture — it should have a unique custom texture.

Resource Pack Version History
Version	Size	SHA-1	Description
v4.0.0	3.04 MB	63b8e30d...	pack_format 75, initial 63 weapons, item definitions for 1.21.4+
v4.0.2	3.09 MB	626edf29...	Real textures, 3D models
v4.0.3	3.17 MB	6cd61288...	All 15 missing 3D weapon models from v2.1 pack
v4.0.4	3.19 MB	ca60f59f...	Fixed 5 remaining buggy weapons
v4.0.5	3.19 MB	2147a804...	Fixed excalibur + requiem awakened
v4.0.6	3.19 MB	7d03a108...	Added animation mcmeta for excalibur textures
v4.0.7	3.17 MB	c433786c...	Fix Excalibur 3D models: extract 128×128 frame from spritesheet
v4.0.8	3.17 MB	45aeea8a...	Excalibur Awakened: animated fire texture
v4.0.9	3.17 MB	1b9f9e6c...	Excalibur Awakened: majestica model with blue fire + effect-sheet
v4.1.0	3.52 MB	a7a0e72c...	Added armor, infinity, powerup textures + models. Bow models ready.
v5.0	3.52 MB	dc5cdce0...	Repackaged for new release URL
v6.0	—	26704612...	(intermediate update)
v7.0	—	a7212467...	Fixed Infinity Stone textures, Creation Splitter particle bug
v8.0	—	—	Abyss Dragon model added
v9.0	~3.68 MB	0e6151b6...	CURRENT — Dragon visual fix
The broken "v4.1.1" file (9.5 MB, created by PowerShell's Compress-Archive) must NEVER be used.

How Minecraft 1.21.4+ Custom Textures Work
Minecraft 1.21.4+ uses item model definitions in assets/minecraft/items/*.json. Each file uses type: "minecraft:select" with property: "minecraft:custom_model_data" to choose which model to render based on the custom_model_data component's strings list. The Java plugin sets the string via CustomModelDataComponent.setStrings(List.of("texture_name")). The "when" value in the JSON must exactly match the string set by the plugin.

For worn armor textures in 1.21.4+, the equippable component must set an asset_id that points to an assets/minecraft/equipment/<name>.json file, which in turn references textures in assets/minecraft/textures/entity/equipment/humanoid/ and humanoid_leggings/.

How to Update the Resource Pack
Make changes to files in the resource-pack working directory.
DO NOT use PowerShell Compress-Archive — it produces zips with wrong structure. Use 7-Zip CLI or the manual zip method that preserves pack.mcmeta at root.
Get the SHA-1: (Get-FileHash -Path $zipPath -Algorithm SHA1).Hash.ToLower()
Upload as a NEW release version (v10.0, v11.0, etc. — never overwrite old versions): gh release create v10.0 $zipPath --repo JGlims/JGlimsPlugin --title "Resource Pack v10.0" --notes "SHA-1: $sha1"
Update docker-compose.yml on the server with new URL + SHA-1 via sed.
Recreate container: docker compose down && docker compose up -d
C. COMPLETE CODEBASE (68 Java files, ~1.27 MB total)
Root Package — com.jglims.plugin
JGlimsPlugin.java (34,896 B) — Main plugin class. onEnable() initializes all managers and listeners, registers all three commands (/jglims, /guild, /guia), creates the Abyss dimension, and logs startup. Contains the command dispatcher for the /jglims admin command with its 14+ subcommands.

abyss Package (5 files)
AbyssChunkGenerator.java (7,341 B) — Custom ChunkGenerator that produces a void-based terrain for world_abyss. Generates a roughly 210-block-radius landmass centered at origin using layered sinusoidal noise. Surface materials include deepslate tiles (inner), polished blackstone (mid), purpur blocks and end stone (outer). Features random amethyst columns, obsidian spikes, end rods, crying obsidian, and soul campfires as decorations. Beyond radius 160, generates floating islands at random Y levels. Includes void rivers carved between radius 130–200. All vanilla generation (noise, surface, caves, decorations, mobs, structures) is disabled.

AbyssCitadelBuilder.java (54,253 B) — The largest structure builder in the plugin. Constructs a massive gothic cathedral called the "Abyssal Citadel" using a Blockwave Studios-inspired design. The architecture consists of: an elliptical foundation platform (110×140 blocks), a circular courtyard with glowing crying obsidian lines (radius 40, centered at z=65), a main nave (80 blocks long × 60 wide × 50 tall with gothic narrowing walls), a cross-shaped transept (extending ±50 blocks on X, 45 blocks tall), a semicircular apse (radius 25, 55 blocks tall), a massive front facade (60 half-width, 80 blocks tall) with a pointed-arch entrance (8 half-width, 25 blocks tall), a rose window (radius 9 with radiating spokes pattern), a gable with finial, a center mega-spire (180 blocks tall), 6 flanker spires (100–120 blocks), 15 minor spires (50–90 blocks), flying buttresses along the nave, gothic pointed-arch windows with purple glass panes throughout, a pointed roof, interior nave with two rows of pillars, interior side chapels with loot chests, 4 Abyssal Weapon guardian chambers (obsidian rooms with amethyst pedestals, weapon chests, soul fire, and purple glass windows), a covered walkway from the apse to the boss arena, and the boss arena itself (radius 40, bedrock floor, barrier walls, 8 end stone pillars with amethyst caps, end rods, crying obsidian accents, and soul campfire pedestals), plus rocky terrain features, floating amethyst crystal formations, and an approach path. Block palette uses deepslate bricks, deepslate tiles, polished deepslate, chiseled deepslate, blackstone, polished blackstone bricks, obsidian, crying obsidian, amethyst blocks/clusters, purple stained glass panes, magenta stained glass panes, iron bars, soul lanterns, soul campfire, end rods, end stone bricks, bedrock, barrier, purpur, and air. After construction, populates chests with ABYSSAL and MYTHIC tier loot and spawns Enderman/WitherSkeleton guards.

AbyssDimensionManager.java (18,501 B) — Creates and manages the world_abyss world. Before world load, writes a paper-world.yml that disables scan-for-legacy-ender-dragon. Creates the world with THE_END environment using AbyssChunkGenerator. Disables game rules: mob spawning, weather cycle, fire tick, mob griefing, advancement announcements, death messages. Sets difficulty to HARD, time to midnight. CRITICAL: Actively disables the vanilla DragonBattle every 10 seconds — removes the phantom Ender Dragon boss bar, kills any stray EnderDragon entities, and hides/removes the vanilla BossBar. If the citadel hasn't been built, triggers AbyssCitadelBuilder. Runs an ambient mob spawner (Endermen named "Void Wanderer" and Wither Skeletons named "Abyssal Sentinel") that avoids the arena vicinity. Creates the Abyssal Key item (Echo Shard base material, CMD 9001, PersistentDataContainer tag abyssal_key). Handles portal interaction: RIGHT_CLICK_BLOCK on PURPUR_BLOCK while holding the Abyssal Key searches for a valid 4-wide × 5-tall purpur frame (inner opening 2×3), fills interior with END_GATEWAY blocks, plays sounds and particle effects, and auto-removes the gateway after 30 seconds. Player teleportation is triggered by PlayerMoveEvent detecting entry into active gateway blocks, with a 5-second cooldown. Teleports to z=115.5 (south of citadel, facing north). Arena center constant: ARENA_CENTER_Z = -120.

AbyssDragonBoss.java (23,877 B) — Complete boss fight controller implementing Listener. Constants: 1500 HP, 25% damage reduction, arena radius 40 blocks, 30-minute respawn cooldown. The manualTrigger(Player) method validates the player is in world_abyss, checks cooldowns, then calls startFight(). Fight initialization finds the arena floor Y-level, generates 11 waypoints in a circle (8 perimeter + 1 center high + 2 dive points), creates an AbyssDragonModel, sets up a purple notched boss bar, shows titles/sounds to all players, and spawns lightning effects. Two concurrent scheduled tasks run: a combat loop (every 2 seconds / 40 ticks) that updates the boss bar progress, checks phase transitions at 50% HP (phase 2), 25% HP (phase 3), and 10% HP (phase 4/enrage), selects random nearby players for attacks, and checks for death; and a movement loop (every 0.5 seconds / 10 ticks) that moves the dragon toward current waypoint at speed 0.8 + (phase × 0.2), with a 30% chance to divert toward a random player when reaching a waypoint. Attack methods vary by phase: Phase 1 — void breath (8+ damage, breath particles), lightning strike (6 damage), wing gust (4 damage + knockback). Phase 2 — adds summon minions (3-6 Endermen/Wither Skeletons). Phase 3 — adds ground pound (10 damage, vertical knockback to players within 15 blocks) and void pull (portal particles, pulls players toward dragon). Phase 4 (enrage) — enrage lightning barrage (8 rapid lightning strikes near target). Phase announcements show titles, change boss bar to red at phase 3+. Damage handling: blocks environmental damage on the base zombie entity, applies 25% reduction to player damage, triggers damage flash on the model. On death: cancels tasks, zeroes boss bar, plays death animation, shows VICTORY title with fireworks, drops loot after 4-second delay (dragon egg, 2 nether stars, 1000 XP in 20 orbs, 1 guaranteed random ABYSSAL legendary weapon, 3 netherite ingots, 16 diamonds, 32 emeralds), builds a 3×3 END_GATEWAY exit portal, cleans up named minions, hides boss bar after 10 seconds.

AbyssDragonModel.java (17,894 B) — Visual model implementation using an invisible Zombie (hitbox entity) + ItemDisplay (visual entity). Constants: base scale 5.0, rage scale 5.75, enrage scale 6.25, CMD integer 40000, CMD string "abyss_dragon". The spawn() method creates an invisible, silent, no-AI, no-gravity adult Zombie with health capped at Math.min(maxHp, 2048.0) (requires spigot.yml attribute.maxHealth.max >= 2048.0). Sets SCALE attribute to 3.0 on the zombie for hitbox size. Creates a PAPER ItemStack with both legacy integer CMD (40000) and modern string-based CustomModelDataComponent ("abyss_dragon"), plus setItemModel(NamespacedKey.fromString("minecraft:abyss_dragon")). Spawns an ItemDisplay at +2Y with FIXED billboard, scale transformation of (5,5,5), and view range 2.0. Starts an invisibility enforcer (every 5 ticks, reapplies invisibility potion to zombie). Starts an animation loop (every 2 ticks) that bobs the display sinusoidally (±0.5 blocks), slowly rotates it around the Y axis (0.02 rad/tick), scales per phase (5.0 → 5.75 → 6.25), and spawns ambient particles (DRAGON_BREATH always, SOUL_FIRE_FLAME at phase 2+, PORTAL at phase 3+). Movement via moveToward() teleports the zombie toward a target at configurable speed, updating yaw to face the direction of travel. Attack visuals: breathAttackVisual() draws a particle beam (DRAGON_BREATH + SOUL_FIRE_FLAME) from display to target with dragon shoot sound; wingGustVisual() spawns CLOUD + SWEEP_ATTACK particles with dragon flap sound. Phase transitions trigger growl, explosion particles, portal particles, and wither spawn sound at phase 3+. damageFlash() sets display brightness to max for 4 ticks and spawns DAMAGE_INDICATOR particles. playDeathAnimation() removes the zombie immediately, then runs a 60-tick shrink-and-spin animation (scale × 0.97 per tick, rotation += 0.3 per tick, rising translation) with continuous DRAGON_BREATH and PORTAL particles, explosion sounds every 5 ticks, culminating in EXPLOSION_EMITTER and ENDER_DRAGON_DEATH sound. cleanup() cancels all tasks and removes both entities.

blessings Package (2 files)
BlessingListener.java (3,452 B) — Listens for blessing crystal use events. BlessingManager.java (10,296 B) — Manages three blessing types: C-Bless (heal +1 HP per use, max 10 uses), Ami-Bless (+2% damage per use, max 10), La-Bless (+2% defense per use, max 10). Stats displayed via /jglims stats.

config Package (1 file)
ConfigManager.java (29,211 B) — Reads all config.yml values and provides getters. Configuration sections cover: mob difficulty (distance-based scaling from 350 to 5000 blocks), biome multipliers, boss enhancer (Ender Dragon 3.5× HP, Wither 1.0×, Warden 1.0×, Elder Guardian 2.0× HP), creeper spawn reduction (50% cancel), Pale Garden fog effect, loot booster, blessings, anvil modification (remove "Too Expensive" + 50% XP reduction), feature toggles for 10 systems, drop rate booster (35% trident drop from Drowned), villager trade modifications (50% price reduction, no trade locking), king mob configuration (every 100th mob, 7× HP), axe nerf (0.5 attack speed), weapon mastery (max 1000 kills → +20% bonus), blood moon (15% chance per night, 12× boss HP), guilds (max 10 members, no friendly fire), dog armor (95% damage reduction), super tools (2% bonus per enchantment for netherite), and ore detection radii.

crafting Package (2 files)
RecipeManager.java (34,774 B) — Registers all custom shaped/shapeless recipes: 8 battle tool types (sword, axe, bow, mace, shovel, pickaxe, trident, spear), diamond and netherite super tools, sickles, Infinity Stone combining (colored glass + redstone → finished Stone), Infinity Gauntlet assembly (Thanos Glove + all 6 Stones), and all 24 craftable armor pieces (6 sets × 4 slots). VanillaRecipeRemover.java (588 B) — Removes conflicting vanilla recipes on startup.

enchantments Package (5 files)
AnvilRecipeListener.java (33,648 B) — Custom anvil UI interceptor handling enchantment combining, Infinity Stone assembly, and enchantment conflict detection. CustomEnchantManager.java (8,853 B) — Defines all 64 custom enchantments with level caps, conflict pairs, and applicable item type restrictions. EnchantmentEffectListener.java (69,495 B) — The largest single source file. Implements all 64 enchantment effects with their trigger conditions and gameplay mechanics. EnchantmentType.java (1,942 B) — Enum categorizing enchantment types. SoulboundListener.java (7,961 B) — Prevents items with the Soulbound enchantment from dropping on player death.

events Package (7 files)
EventManager.java (10,367 B) — Central event scheduler running every 60 seconds (1200 ticks). Checks for random event triggers, manages 10-minute per-world cooldowns, handles broadcasts, boss tagging, weapon drops, and miscellaneous loot drops.

NetherStormEvent.java (11,421 B) — Ghast swarms, enhanced Blaze spawns, fire rain particles, Infernal Overlord boss (Ghast base, 400 HP). PiglinUprisingEvent.java (8,956 B) — Massive Piglin army spawn, Piglin Emperor boss (Piglin Brute base, 500 HP). VoidCollapseEvent.java (8,961 B) — Void particles pulling players down, Void Leviathan boss (Elder Guardian variant, 500 HP). PillagerWarPartyEvent.java (19,957 B) — Overworld Pillager raid with escalating waves. PillagerSiegeEvent.java (19,146 B) — Nighttime Pillager siege with Fortress Warlord boss. EndRiftEvent.java (30,058 B) — Creates a 15×15 portal, spawns waves of End mobs, End Rift Dragon boss (600 HP Ender Dragon variant).

guilds Package (2 files)
GuildListener.java (1,456 B) — Guild event hooks. GuildManager.java (13,679 B) — Full CRUD operations, invitation system, friendly fire toggle, persistence via flat-file storage.

legendary Package (15 files — core of the plugin)
LegendaryTier.java (3,673 B) — 5-tier enum. COMMON: 12–15 damage, 10 particle budget, 1.0× cooldown multiplier. RARE: 14–17 damage, 25 particles, 0.95×. EPIC: 17–20 damage, 50 particles, 0.9×. MYTHIC: 20–30 damage, 100 particles, 0.85×. ABYSSAL: 28–40 damage, 200 particles, 0.8×. Color scheme: COMMON=white, RARE=aqua, EPIC=gold, MYTHIC=light purple, ABYSSAL=dark red (RGB 170,0,0). Includes backward-compatibility mappings: "LEGENDARY" → EPIC, "UNCOMMON" → COMMON.

LegendaryWeapon.java (15,034 B) — Enum of 63 weapons with fields: id, displayName, baseMaterial, baseDamage, customModelData (legacy int 30001–30063), tier, textureName (the critical string that must match the resource pack JSON "when" value), primaryAbilityName, altAbilityName, primaryCooldown, altCooldown. Distribution: 20 COMMON, 8 RARE, 7 EPIC, 24 MYTHIC, 4 ABYSSAL. Provides fromId(), byTier(), byMaterial().

LegendaryWeaponManager.java (7,937 B) — Creates weapon ItemStacks with string-based CustomModelDataComponent, PersistentDataContainer tags, tier-colored display names via Adventure API, and lore showing ability names and cooldowns. All items are set as Unbreakable.

LegendaryAbilityListener.java (18,032 B) — Event dispatcher: right-click → primary ability, crouch + right-click → alternate ability. Routes to the appropriate handler based on the weapon's PersistentDataContainer tag.

LegendaryPrimaryAbilities.java (69,951 B) — Implementation of all 63 primary abilities. Each weapon has a unique ability with particles, damage calculations, entity spawning, and cooldowns.

LegendaryAltAbilities.java (73,055 B) — Implementation of all 63 alternate abilities. The second-largest source file.

LegendaryAbilityContext.java (4,941 B) — Shared state maps (bloodlust stacks, retribution damage, soul collector counts, phase shift active status, etc.) and utility methods: getNearbyEnemies(), dealDamage(), getTargetEntity(), rotateY(), spawnThrottled() (particle throttle), trackTask(), trackSummon(), cleanupPlayer().

LegendaryArmorSet.java (16,160 B) — Enum of 13 armor sets. Each set defines: id, displayName, tier, base material per slot (helmet/chestplate/leggings/boots), command IDs per slot (range 30101–30254), total defense value, set-bonus description, passive ability strings per piece, and texture file names per piece. Craftable sets: Reinforced Leather (12 def), Copper (14 def), Chainmail Reinforced (15 def), Amethyst (16 def), Bone (16 def), Sculk (18 def). Legendary sets: Shadow Stalker (24 def, RARE), Blood Moon (30 def, EPIC), Nature's Embrace (28 def, EPIC), Frost Warden (30 def, EPIC), Void Walker (36 def, MYTHIC), Dragon Knight (40 def, MYTHIC), Abyssal Plate (50 def, ABYSSAL — set bonus: +30% damage, Wither immunity).

LegendaryArmorManager.java (16,059 B) — Creates armor ItemStacks with the equippable component, sets asset_id for worn texture rendering, applies passives and set bonuses.

LegendaryArmorListener.java (38,729 B) — Implements all set bonuses and per-piece passive effects for all 13 armor sets.

LegendaryLootListener.java (22,565 B) — Drop table logic for bosses, structures, and chests. Tiered probability system.

InfinityStoneManager.java (8,029 B) — 6 stone types: Power (purple), Space (blue), Reality (red), Soul (orange), Time (green), Mind (yellow). Handles fragment creation, finished stone creation, and anvil combining logic.

InfinityGauntletManager.java (23,370 B) — Creates Thanos Glove, Infinity Gauntlet, and implements the snap ability (right-click kills 50% of all loaded hostile mobs in the current dimension, 300-second cooldown). Includes snap animation with particle effects and sounds.

menu Package (2 files)
CreativeMenuManager.java (45,503 B) — GUI inventory menus for browsing all weapons (by tier), armor sets, power-ups, Infinity Stones, and other items. Supports pagination. GuideBookManager.java (29,396 B) — Generates multi-volume written books in Portuguese (PT-BR) explaining all plugin systems, given via /guia.

mobs Package (7 files)
BiomeMultipliers.java (3,080 B) — Biome-specific mob stat scaling lookups.

BloodMoonManager.java (12,329 B) — Blood Moon event system: 15% chance per night, red sky visual, mob stat buffs, boss-tier mob spawns every 10th regular mob, double drops from all mobs, 0.1% chance to drop Infinity Stone fragments.

BossEnhancer.java (19,998 B) — Enhances vanilla bosses: Ender Dragon 3.5× HP with custom abilities, Wither 1.0× with abilities, Warden 1.0× with abilities, Elder Guardian 2.0× HP with abilities. Each boss gets custom attack patterns.

BossMasteryManager.java (12,294 B) — Tracks boss kill participation and awards permanent titles with passive bonuses: Wither Slayer (+5%), Guardian Slayer (+7%), Warden Slayer (+10%), Dragon Slayer (+15%), Abyssal Conqueror (+20%), God Slayer (+25% damage / +20% defense).

KingMobManager.java (5,972 B) — Every Nth mob spawn (configurable, default every 100th) becomes a "King" mob with 7× HP, 2.5× damage, and diamond drops.

MobDifficultyManager.java (4,744 B) — Distance-from-spawn scaling system. Mobs become progressively harder as players venture further (350-block intervals up to 5000 blocks).

RoamingBossManager.java (57,427 B) — The second-largest source file. Implements 6 roaming world bosses: The Watcher (Deep Dark biome, Warden base, 800 HP, EPIC drops, Darkness + Mining Fatigue aura), Hellfire Drake (Nether wastes, Ghast base, 600 HP, EPIC, triple fireball salvo), Frostbound Colossus (Snowy biomes, Iron Golem base, 700 HP, EPIC, freeze aura + ground pound), Jungle Predator (Jungle, Ravager base, 500 HP, RARE/EPIC, stealth pounce + poison fog), End Wraith (The End outer islands, Phantom size 6, 900 HP, MYTHIC, void beam + phase shift + shadow clones at 50% HP), Abyssal Leviathan (Abyss dimension, 1200 HP, ABYSSAL drops). Each boss has a 2–4% spawn chance per 2-minute check, auto-despawn after 10 minutes, custom particle effects, unique attack patterns per phase, and tier-appropriate weapon/loot drops.

powerups Package (2 files)
PowerUpManager.java (26,207 B) — 7 power-up items, all using string-based CustomModelDataComponent: Heart Crystal (RED_DYE base, +2 max HP per use, max 40 uses → +80 HP), Soul Fragment (PURPLE_DYE, +1.0% damage per use, max 100 → +100%), Titan's Resolve (IRON_NUGGET, +10% knockback resistance + 2% DR per use, max 5 → 50% KB resist + 10% DR), Phoenix Feather (FEATHER, auto-revive on death with 4 hearts, stackable/consumes one per death), KeepInventorer (ENDER_EYE, permanent keep-inventory flag, one-time use), Vitality Shard (PRISMARINE_SHARD, +5% DR per use, max 10 → 50% DR), Berserker's Mark (BLAZE_POWDER, +3% attack speed per use, max 10 → 30%). Maximum total passive DR is capped at 75%.

PowerUpListener.java (5,607 B) — Handles right-click consumption events and death events (Phoenix Feather auto-revive, KeepInventory enforcement).

quests Package (3 files)
QuestManager.java (27,940 B) — 6 quest lines (Overworld, Nether, End, Special, Boss, Explorer) with progress tracking, tiered rewards, and a scheduler. QuestProgressListener.java (3,231 B) — Monitors kills, exploration milestones, and item collection for quest advancement. NpcWizardManager.java (15,501 B) — Archmage NPC villager (purple robes) that sells exclusive MYTHIC weapons (rotating stock of 3 for 48 diamonds + 16 Nether Stars each), Heart Crystals, Phoenix Feathers, KeepInventorer (64 diamonds + 32 emeralds), and Blessing Crystals.

structures Package (5 files)
StructureType.java (10,780 B) — Enum of 40 structure types: 24 Overworld (Ruined Colosseum, Druid's Grove, Shrek House, Gigantic Castle, Thanos Temple, Pillager Fortress, Pillager Airship, Frost Dungeon, Bandit Hideout, Sunken Ruins, Cursed Graveyard, Sky Altar, and more), 7 Nether (Crimson Citadel, Soul Sanctum, etc.), 5 End (Void Shrine, Ender Monastery, Dragon's Hoard, etc.), 3 Abyss (Abyssal Castle 120×80×120, Void Nexus, Shattered Cathedral), plus the Ender Dragon Death Chest structure. Each structure has: display name, dimension, loot tier, size (X/Y/Z), boss flag, boss name and base HP, and valid biome set. Generation chances: COMMON 0.8%, RARE 0.5%, EPIC 0.3%, MYTHIC 0.1%, ABYSSAL 0.05% per chunk. isAbyssDimension() check uses world name "world_abyss".

StructureManager.java (49,819 B) — Chunk-based generation with 300-block minimum spacing, biome filtering, and programmatic block placement for all 40 structure types. StructureBuilder.java (8,714 B) — Block placement API. StructureLootPopulator.java (8,554 B) — Chest filling per structure type/tier. StructureBossManager.java (14,554 B) — Mini-boss spawning, health bar management, and tracking.

utility Package (7 files)
BestBuddiesListener.java (11,609 B) — Wolf/dog armor system with 95% damage reduction for tamed wolves wearing armor. DropRateListener.java (3,289 B) — 35% trident drop rate boost from Drowned, plus breeze wind charge drops. EnchantTransferListener.java (7,135 B) — Transfer enchantments between items. InventorySortListener.java (5,282 B) — Shift-click on empty inventory slot to auto-sort. LootBoosterListener.java (8,991 B) — Enhanced chest/mob loot tables. PaleGardenFogTask.java (1,356 B) — Fog visual effect when players are in the Pale Garden biome. VillagerTradeListener.java (4,244 B) — 50% price reduction on all villager trades, removes trade locking.

weapons Package (13 files)
BattleAxeManager.java (8,303 B), BattleBowManager.java (4,871 B), BattleMaceManager.java (4,279 B), BattlePickaxeManager.java (7,168 B), BattleShovelManager.java (8,451 B), BattleSpearManager.java (8,339 B), BattleSwordManager.java (6,821 B), BattleTridentManager.java (4,737 B), SickleManager.java (8,061 B), SpearManager.java (11,128 B), SuperToolManager.java (22,176 B) — 11 non-legendary weapon type managers creating tiered variants (wood through netherite) with custom attributes.

WeaponAbilityListener.java (81,738 B) — The third-largest source file. Implements 20+ non-legendary weapon abilities for battle tools and super tools.

WeaponMasteryManager.java (9,803 B) — Kill-based mastery progression: Novice (0 kills), Fighter (100 kills, +1% damage), Experienced Fighter (500 kills, +5%), Master (1000 kills, +15%).

D. LEGENDARY WEAPONS — COMPLETE TABLE (63 weapons)
COMMON Tier (20 weapons, damage 12–15, particle budget 10)
#	ID	Display Name	Material	Dmg	CMD	Texture Name	Primary Ability	Alt Ability	PCD	ACD
1	amethyst_shuriken	Amethyst Shuriken	DIAMOND_SWORD	13	30012	amethyst_shuriken	Shuriken Barrage	Shadow Step	5	10
2	gravescepter	Gravescepter	DIAMOND_SWORD	13	30026	revenants_gravescepter	Grave Rise	Death's Grasp	5	12
3	lycanbane	Lycanbane	DIAMOND_SWORD	14	30027	lycanbane	Silver Strike	Hunter's Sense	5	14
4	gloomsteel_katana	Gloomsteel Katana	DIAMOND_SWORD	13	30028	gloomsteel_katana	Quick Draw	Shadow Stance	3	12
5	viridian_cleaver	Viridian Cleaver	DIAMOND_AXE	15	30029	viridian_greataxe	Verdant Slam	Overgrowth	5	15
6	crescent_edge	Crescent Edge	DIAMOND_AXE	14	30030	crescent_greataxe	Lunar Cleave	Crescent Guard	5	14
7	gravecleaver	Gravecleaver	DIAMOND_SWORD	14	30031	revenants_gravecleaver	Bone Shatter	Undying Rage	5	18
8	amethyst_greatblade	Amethyst Greatblade	DIAMOND_SWORD	13	30032	amethyst_greatblade	Crystal Burst	Gem Resonance	5	18
9	flamberge	Flamberge	DIAMOND_SWORD	14	30033	flamberge	Flame Slash	Fire Wall	5	15
10	crystal_frostblade	Crystal Frostblade	DIAMOND_SWORD	13	30034	crystal_frostblade	Frost Strike	Blizzard Shield	5	15
11	demonslayer	Demonslayer	DIAMOND_SWORD	15	30035	demonslayer	Demon Cleave	Infernal Rage	5	18
12	vengeance	Vengeance	DIAMOND_SWORD	14	30036	vengeance	Retribution	Blood Price	5	15
13	oculus	Oculus	DIAMOND_SWORD	12	30037	oculus	All-Seeing Strike	Mind's Eye	5	12
14	ancient_greatslab	Ancient Greatslab	DIAMOND_SWORD	15	30038	ancient_greatslab	Ground Slam	Stone Wall	5	18
15	neptunes_fang	Neptune's Fang	TRIDENT	14	30039	neptunes_fang	Tidal Strike	Ocean Shield	5	15
16	tidecaller	Tidecaller	TRIDENT	13	30040	tidecaller	Wave Crash	Riptide Dash	5	12
17	stormfork	Stormfork	TRIDENT	14	30041	stormfork	Thunder Bolt	Storm Shield	5	15
18	jade_reaper	Jade Reaper	DIAMOND_SWORD	13	30042	jade_reaper	Jade Slash	Emerald Shield	3	10
19	vindicator	Vindicator	DIAMOND_AXE	15	30043	vindicator	Cleaving Strike	Berserker Rush	5	15
20	spider_fang	Spider Fang	DIAMOND_SWORD	12	30044	spider_fang	Venom Strike	Web Trap	5	12
RARE Tier (8 weapons, damage 14–17, particle budget 25)
#	ID	Display Name	Material	Dmg	CMD	Texture Name	Primary	Alt	PCD	ACD
21	oceans_rage	Ocean's Rage	DIAMOND_SWORD	16	30001	oceansrage	Tsunami Strike	Maelstrom	6	20
22	aquatic_sacred_blade	Aquatic Sacred Blade	DIAMOND_SWORD	15	30002	aquatic_sacred_blade	Sacred Torrent	Blessing of the Deep	6	22
23	royal_chakram	Royal Chakram	DIAMOND_SWORD	14	30003	royal_chakram	Chakram Throw	Shield of Blades	5	18
24	acidic_cleaver	Acidic Cleaver	DIAMOND_AXE	17	30004	acidic_cleaver	Acid Splash	Corrode	6	22
25	muramasa	Muramasa	DIAMOND_SWORD	16	30005	muramasa	Bloodlust	Cursed Edge	5	18
26	windreaper	Windreaper	DIAMOND_SWORD	15	30006	windreaper	Gale Slash	Tempest	6	20
27	moonlight	Moonlight	DIAMOND_SWORD	15	30007	moonlight	Lunar Beam	Eclipse	6	22
28	talonbrand	Talonbrand	DIAMOND_SWORD	16	30008	talonbrand	Raptor Strike	Falcon Dive	5	18
EPIC Tier (7 weapons, damage 17–20, particle budget 50)
#	ID	Display Name	Material	Dmg	CMD	Texture Name	Primary	Alt	PCD	ACD
29	berserkers_greataxe	Berserker's Greataxe	DIAMOND_AXE	20	30009	berserkers_greataxe	Rampage	War Cry	7	25
30	black_iron_greatsword	Black Iron Greatsword	DIAMOND_SWORD	19	30010	black_iron_greatsword	Iron Curtain	Unstoppable Force	7	25
31	solstice	Solstice	DIAMOND_SWORD	18	30011	solstice	Solar Flare	Equinox	7	28
32	grand_claymore	Grand Claymore	DIAMOND_SWORD	19	30013	grand_claymore	Heavy Cleave	Counter Stance	7	25
33	calamity_blade	Calamity Blade	DIAMOND_SWORD	18	30014	calamity_blade	Disaster Strike	Chaos Zone	7	28
34	emerald_greatcleaver	Emerald Greatcleaver	DIAMOND_AXE	20	30015	emerald_greatcleaver	Emerald Shatter	Jade Fortify	7	25
35	demons_blood_blade	Demon's Blood Blade	DIAMOND_SWORD	19	30016	demons_blood_blade	Blood Drain	Demon Form	7	30
MYTHIC Tier (24 weapons, damage 20–30, particle budget 100)
#	ID	Display Name	Material	Dmg	CMD	Texture Name	Primary	Alt	PCD	ACD
36	true_excalibur	True Excalibur	DIAMOND_SWORD	25	30017	true_excalibur	Holy Smite	Divine Shield	8	30
37	requiem_of_the_ninth_abyss	Requiem of the Ninth Abyss	DIAMOND_SWORD	26	30018	requiem_ninth	Abyss Scream	Void Embrace	8	35
38	phoenixs_grace	Phoenix's Grace	DIAMOND_SWORD	24	30019	phoenixs_grace	Phoenix Strike	Rebirth Flame	8	30
39	soul_collector	Soul Collector	DIAMOND_SWORD	23	30020	soul_collector	Soul Harvest	Soul Storm	8	30
40	valhakyra	Valhakyra	DIAMOND_AXE	27	30021	valhakyra	Valkyrie's Descent	Wings of War	8	35
41	phantomguard	Phantomguard	DIAMOND_SWORD	22	30022	phantomguard	Phase Shift	Spectral Army	8	35
42	zenith	Zenith	DIAMOND_SWORD	25	30023	zenith	Zenith Strike	Heavenly Light	8	30
43	dragon_sword	Dragon Sword	DIAMOND_SWORD	26	30024	dragon_sword	Dragon Breath	Scale Shield	8	30
44	nocturne	Nocturne	DIAMOND_SWORD	24	30025	nocturne	Night Slash	Shadow Realm	8	35
45	divine_axe_rhitta	Divine Axe Rhitta	DIAMOND_AXE	30	30045	divineaxerhitta	Cruel Sun	Sunshine	10	40
46	yoru	Yoru	DIAMOND_SWORD	26	30046	yoru	Black Blade	Worlds Strongest Slash	8	35
47	tengens_blade	Tengen's Blade	DIAMOND_SWORD	24	30047	tengensblade	Sound Breathing	Festival Performance	8	30
48	edge_astral_plane	Edge of the Astral Plane	DIAMOND_SWORD	25	30048	edge_astral_plane	Astral Slash	Dimension Rift	8	35
49	fallen_gods_spear	Fallen God's Spear	TRIDENT	27	30049	fallen_gods_spear	God's Judgment	Divine Wrath	10	40
50	nature_sword	Nature Sword	DIAMOND_SWORD	23	30050	nature_sword	Vine Whip	Nature's Wrath	8	30
51	heavenly_partisan	Heavenly Partisan	DIAMOND_SWORD	25	30051	heavenly_partisan	Heaven's Strike	Celestial Judgment	8	35
52	soul_devourer	Soul Devourer	DIAMOND_SWORD	26	30052	soul_devourer	Devour	Soul Explosion	8	35
53	mjolnir	Mjölnir	DIAMOND_AXE	28	30053	mjolnir	Thunder God	Worthy	10	40
54	thousand_demon_daggers	Thousand Demon Daggers	DIAMOND_SWORD	22	30054	thousanddemondaggers	Demon Barrage	Hellstorm	5	28
55	star_edge	Star Edge	DIAMOND_SWORD	25	30055	star_edge	Star Slash	Constellation	8	30
56	rivers_of_blood	Rivers of Blood	DIAMOND_SWORD	24	30056	riversofblood	Corpse Piler	Blood Loss	5	25
57	dragon_slaying_blade	Dragon Slaying Blade	DIAMOND_SWORD	27	30057	dragonslayingblade	Dragon Cleave	Dragon Fear	8	35
58	stop_sign	Stop Sign	DIAMOND_SWORD	20	30058	stop_sign	STOP	Yield	8	30
59	creation_splitter	Creation Splitter	DIAMOND_AXE	30	30059	creationsplitter	Reality Rift	World Break	10	50
ABYSSAL Tier (4 weapons, damage 28–40, particle budget 200)
#	ID	Display Name	Material	Dmg	CMD	Texture Name	Primary	Alt	PCD	ACD
60	requiem_awakened	Requiem Awakened	NETHERITE_SWORD	38	30060	requiem_awakened	Abyss Annihilation	Void Apocalypse	12	50
61	excalibur_awakened	Excalibur Awakened	NETHERITE_SWORD	34	30061	excalibur_awakened	Holy Annihilation	Sacred Apocalypse	12	45
62	creation_splitter_awakened	Creation Splitter Awakened	NETHERITE_AXE	40	30062	creation_splitter_awakened	Creation's End	Dimensional Collapse	12	50
63	whisperwind_awakened	Whisperwind Awakened	NETHERITE_SWORD	30	30063	whisperwind_awakened	Silent Storm	Zephyr's Embrace	10	40
Weapons with Fuzzy Texture Name Mappings
Some weapons have texture names that differ from their IDs due to model pack naming conventions: divine_axe_rhitta → divineaxerhitta, tengens_blade → tengensblade, thousand_demon_daggers → thousanddemondaggers, rivers_of_blood → riversofblood, dragon_slaying_blade → dragonslayingblade, creation_splitter → creationsplitter.

E. ARMOR SETS — COMPLETE TABLE (13 sets)
Craftable Armor Sets (6)
Set Name	Tier	Total Defense	Material	Helmet CMD	Chest CMD	Legs CMD	Boots CMD	Set Bonus
Reinforced Leather	COMMON	12	LEATHER	30101	30102	30103	30104	Minor protection boost
Copper Armor	COMMON	14	IRON	30111	30112	30113	30114	Electrical resistance
Chainmail Reinforced	COMMON	15	CHAINMAIL	30121	30122	30123	30124	Projectile reduction
Amethyst Armor	RARE	16	DIAMOND	30131	30132	30133	30134	Crystal resonance
Bone Armor	COMMON	16	IRON	30141	30142	30143	30144	Undead affinity
Sculk Armor	RARE	18	DIAMOND	30151	30152	30153	30154	Vibration sensing
Legendary Armor Sets (7)
Set Name	Tier	Total Defense	Material	Set Bonus Description
Shadow Stalker	RARE	24	NETHERITE	Stealth + speed in darkness
Blood Moon	EPIC	30	NETHERITE	Lifesteal on hit
Nature's Embrace	EPIC	28	NETHERITE	Regeneration in forests/jungles
Frost Warden	EPIC	30	NETHERITE	Freeze aura + ice resistance
Void Walker	MYTHIC	36	NETHERITE	Teleport dash + void resistance
Dragon Knight	MYTHIC	40	NETHERITE	Fire immunity + dragon breath
Abyssal Plate	ABYSSAL	50	NETHERITE	+30% damage, Wither immunity
All sets have per-piece passives (helmet, chestplate, leggings, boots) in addition to the full-set bonus. Inventory icons use string-based CMD. Worn textures use the equippable component's asset_id pointing to assets/minecraft/equipment/<set_name>.json.

F. POWER-UP ITEMS (7)
Item	Base Material	CMD String	Effect	Max Uses	Cumulative Max
Heart Crystal	RED_DYE	heart_crystal	+2 max HP	40	+80 HP
Soul Fragment	PURPLE_DYE	soul_fragment	+1% damage	100	+100%
Titan's Resolve	IRON_NUGGET	titans_resolve	+10% KB resist + 2% DR	5	50% KB + 10% DR
Phoenix Feather	FEATHER	phoenix_feather	Auto-revive on death (4 hearts)	Stackable	Consumes 1 per death
KeepInventorer	ENDER_EYE	keep_inventorer	Permanent keep-inventory	1	One-time toggle
Vitality Shard	PRISMARINE_SHARD	vitality_shard	+5% DR	10	50% DR
Berserker's Mark	BLAZE_POWDER	berserker_mark	+3% attack speed	10	30% attack speed
Total passive damage reduction capped at 75% across all sources.

G. INFINITY GAUNTLET SYSTEM
The Thanos boss (800 HP, 2 phases) drops the Thanos Glove on death. Six Infinity Stone fragments (Power/Space/Reality/Soul/Time/Mind) have a 0.1% drop chance from any mob killed during a Blood Moon event. Each fragment is combined with a Nether Star in the custom anvil to create a finished Stone. The Thanos Glove combined with all 6 finished Stones in the anvil produces the Infinity Gauntlet. Right-clicking with the Infinity Gauntlet triggers the "snap" ability: kills 50% of all loaded hostile mobs in the current dimension, plays a dimensional shockwave animation with particles and sounds. Cooldown: 300 seconds. The Thanos Glove, individual Stones, and the Infinity Gauntlet each have unique custom textures.

H. EVENTS (6 active)
All events are managed by EventManager.java, which runs a check every 60 seconds with 10-minute per-world cooldowns.

Nether Storm — Ghast swarms, enhanced Blaze spawns, fire rain visual. Boss: Infernal Overlord (Ghast, 400 HP). Piglin Uprising — Massive Piglin army. Boss: Piglin Emperor (Piglin Brute, 500 HP). Void Collapse — Void particles pulling players down. Boss: Void Leviathan (Elder Guardian variant, 500 HP). Pillager War Party — Overworld Pillager raid with escalating waves. Pillager Siege — Nighttime siege event with Fortress Warlord boss. End Rift — 15×15 portal, wave-based End mob spawns. Boss: End Rift Dragon (600 HP).

I. STRUCTURES (40 types)
24 Overworld structures, 7 Nether structures, 5 End structures, 3 Abyss structures (Abyssal Castle 120×80×120, Void Nexus, Shattered Cathedral), plus the Ender Dragon Death Chest. Generation chances range from 0.8% (COMMON) to 0.05% (ABYSSAL) per chunk. Each structure supports an optional mini-boss with custom HP, attack patterns, and a health bar. Loot is tier-appropriate, drawn from StructureLootPopulator.java. Minimum spacing between structures: 300 blocks.

J. OTHER SYSTEMS
64 Custom Enchantments — Defined in CustomEnchantManager.java with level caps, conflict pairs, and item type restrictions. Effects implemented in EnchantmentEffectListener.java (69.5 KB).

Blessings — Three types of blessing crystals: C-Bless (heal, +1 HP per use, max 10), Ami-Bless (damage, +2% per use, max 10), La-Bless (defense, +2% per use, max 10).

Guilds — Create, invite, join, leave, kick, disband. Max 10 members. Friendly fire toggle. Persistence via flat-file storage.

Blood Moon — 15% chance per night. Red sky, all mob stats buffed, boss-tier mob every 10th spawn, double drops, 0.1% Infinity Stone fragment drops.

King Mobs — Every 100th mob spawn becomes a "King" with 7× HP, 2.5× damage, diamond drops.

Mob Difficulty Scaling — Distance-from-spawn progressive scaling from 350 to 5000 blocks.

Boss Mastery Titles — 6 titles with cumulative passive bonuses up to +25% damage / +20% defense.

Weapon Mastery — 4 tiers: Novice (0 kills), Fighter (100, +1%), Experienced (500, +5%), Master (1000, +15%).

NPC Wizard — Archmage villager selling exclusive MYTHIC weapons, power-ups, and blessing crystals.

Quest System — 6 quest lines (Overworld, Nether, End, Special, Boss, Explorer).

Creative Menu — GUI inventory browser for all plugin items by category/tier.

Guide Book — Multi-volume written books in Portuguese (PT-BR).

11 Battle Weapon Types — Sword, axe, bow, mace, shovel, pickaxe, trident, spear, sickle, super tools (diamond + netherite).

6 Roaming World Bosses — The Watcher (800 HP), Hellfire Drake (600 HP), Frostbound Colossus (700 HP), Jungle Predator (500 HP), End Wraith (900 HP), Abyssal Leviathan (1200 HP).

Dog Armor System — 95% damage reduction for tamed wolves.

Villager Trade Modifications — 50% price reduction, no trade locking.

K. DEPLOYMENT PROCEDURES
Build & Deploy Plugin
Copy$sshKey = "C:\Users\jgmel\Documents\projects\server_minecraft\ssh-key-2026-02-25.key"
$remoteHost = "ubuntu@144.22.198.184"

cd "C:\Users\jgmel\Documents\projects\JGlimsPlugin\JGlimsPlugin"
.\gradlew clean build

$jar = "build\libs\JGlimsPlugin-2.0.0.jar"
scp -i $sshKey $jar "${remoteHost}:/tmp/JGlimsPlugin.jar"
ssh -i $sshKey $remoteHost "docker cp /tmp/JGlimsPlugin.jar mc-crossplay:/data/plugins/JGlimsPlugin.jar && docker restart mc-crossplay"
Deploy Resource Pack (new version)
Copy# 1. Zip the pack (use 7-Zip, NOT Compress-Archive)
# 2. Get SHA-1
$sha1 = (Get-FileHash -Path $zipPath -Algorithm SHA1).Hash.ToLower()
# 3. Upload to GitHub as new release
gh release create v10.0 $zipPath --repo JGlims/JGlimsPlugin --title "Resource Pack v10.0" --notes "SHA-1: $sha1"
# 4. Update docker-compose.yml on server with new URL + SHA-1
# 5. docker compose down && docker compose up -d
L. PROBLEMS SOLVED (historical)
BOM build failure: PowerShell's default UTF-8 BOM corrupted BossEnhancer.java — fixed by restoring from GitHub and stripping BOM. Particle throttle for lag reduction: added spawnThrottled, trackTask, trackSummon, cleanupPlayer to LegendaryAbilityContext.java. Missing Particle import after patching. Resource pack URL pointing to nonexistent file (v4.1.0 path). Docker-compose overwriting server.properties on every start. File ownership (docker cp sets root, container runs as uid 1000). Sed escaping issues with Minecraft property file colons. Infinity Stone texture rendering (fixed in v7.0). Creation Splitter particle effects (fixed in v7.0). Dragon model visual issues (fixed v8.0 → v9.0). Phantom Ender Dragon boss bar in world_abyss (fixed by disableVanillaDragonBattle()).

M. MANDATORY DEVELOPMENT RULES
Always use [System.IO.File]::WriteAllText($path, $content, [System.Text.UTF8Encoding]::new($false)) to avoid BOM when writing Java files from PowerShell.
NEVER use PowerShell Compress-Archive for resource packs — use 7-Zip CLI.
Resource pack URL/SHA-1 changes go in docker-compose.yml, NOT server.properties.
After docker-compose changes: docker compose down && docker compose up -d.
SSH user is ubuntu, NOT opc.
Each resource pack update gets a new major version (v10.0, v11.0, etc.) — never overwrite.
Use Adventure API exclusively (no deprecated ChatColor).
All custom item metadata via PersistentDataContainer with NamespacedKey.
String-based CustomModelDataComponent for all item textures.
Two abilities per weapon: primary (right-click) and alternate (crouch + right-click).
Particle budgets per tier: COMMON 10, RARE 25, EPIC 50, MYTHIC 100, ABYSSAL 200.
All legendary items must be indestructible (Unbreakable: true).
Resource pack pack_format: 75.
Item JSON files in assets/minecraft/items/*.json use string-based custom_model_data.
File-size limit per source file: ~120 KB; split if exceeding.
N. FULL EXPANSION ROADMAP (from README)
Phases 20–35 are planned: End Rift Event enhancements, Enhanced Pillager Warfare, Infinity Gauntlet + Thanos improvements, Quest Villagers & NPC Wizard expansions, Abyss Dimension full rework, Legendary Ranged Weapons (15 bows/crossbows), Ocean Expansion (6 structures + Kraken event), World Bosses (4 Overworld + 3 Nether + 1 End), Dungeon Generator (procedural multi-room dungeons), Legendary Shields & Off-hand Items (16 items), Pet System (8 tameable mythical pets), Seasons & Weather system, Fishing Overhaul, Guild Wars & Territories, Hardcore Challenge Modes. Total planned content: 75+ legendary weapons, 15+ armor sets, 43+ structures, 8+ events, 8 world bosses, 8 pets, procedural dungeon system, full quest system, guild territory/war PvP, 4+ dimensions.

O. ABYSS DIMENSION — DETAILED TECHNICAL STATE
World Generation (AbyssChunkGenerator.java)
The Abyss is a custom THE_END environment world named world_abyss. Terrain is generated within a 210-block radius from origin using layered sinusoidal noise (sin(wx * 0.03) * cos(wz * 0.03) * 8 + sin(wx * 0.07 + 2) * cos(wz * 0.05 + 1) * 4 + sin(wx * 0.12) * cos(wz * 0.12) * 2), producing rolling surface at ~Y50. Surface materials transition from deepslate tiles (inner) through polished blackstone to purpur/end stone (outer). Subsurface layers use obsidian and crying obsidian. Decorative features include amethyst columns (1.2% chance, 4–12 blocks tall), obsidian spikes (0.8%, 5–17 blocks), end rods (1.5%), crying obsidian boulders (2.5%), and soul campfires (0.6%). Beyond radius 160, floating islands generate at random Y levels (30–100) with 7–21 block radius and 5-block thickness. Void rivers are carved between radius 130–200 using sinusoidal noise.

The Abyssal Citadel (AbyssCitadelBuilder.java)
The citadel is built at (0, surfaceY, 0) facing south (+Z direction). It is the most complex programmatic structure in the plugin at 54 KB. Key dimensions: foundation platform ellipse 110×140, center spire 180 blocks tall, nave 80 blocks long by 60 wide by 50 tall, facade 120 blocks wide by 80 tall, boss arena at z=-120 with 40-block radius. The build is executed in 8 sequential phases: foundation platform, main cathedral body (nave + transept + apse + facade), spires (1 center + 6 flanker + 15 minor), gothic details (buttresses + windows + roof), interior (nave clearance + chapels + weapon chambers), arena, environment (rocky terrain + floating crystals + courtyard), and delayed population (chests + guards). The arena floor is bedrock with barrier walls extending to Y+50, 8 end stone pillars with amethyst caps at the perimeter, and a soul campfire at the center.

Dragon Boss Fight (AbyssDragonBoss.java + AbyssDragonModel.java)
The fight is triggered via /jglims admin command (calls manualTrigger(Player)). The dragon has 1500 HP with 25% passive damage reduction. The model is a Zombie (hitbox) + ItemDisplay (visual) pair. The ItemDisplay renders a custom item model referenced by CMD string "abyss_dragon" / integer 40000, scaled to 5.0× (6.25× when enraged). The fight has 4 phases with escalating attacks: void breath (8+ damage), lightning strikes, wing gust (knockback), minion summoning (3–6 Endermen/Wither Skeletons), ground pound (AoE 10 damage), void pull (physics pull toward dragon), and enrage lightning barrage (8 rapid strikes). Death drops guaranteed ABYSSAL weapon + major loot.

Current Problem: Dragon Model Quality
The current abyss_dragon.json model is a basic 11-element cuboid representation. It looks like a small blocky shape that gets scaled up 5×. This is unsatisfactory. The plan is to replace it with a high-quality Sketchfab/Blockbench model.

P. ABYSS DRAGON REWORK — SKETCHFAB MODEL IMPLEMENTATION PLAN
The Problem
The current dragon model is a basic 11-cuboid blocky shape in assets/minecraft/models/item/abyss/abyss_dragon.json. When rendered on the ItemDisplay at 5× scale, it looks like a scaled-up simple shape rather than a convincing dragon. Minecraft Java Edition item models have a hard limit: coordinates must be within [-16, -16, -16] to [32, 32, 32] (a 48-unit cube), and while there's no hard cap on the number of cuboid elements, complex models with hundreds of elements become unwieldy and may cause client-side rendering performance issues. The practical sweet spot is 100–300 elements for a detailed item model.


Implementation Workflow
Step 1: Download the Blockbench model. Download the .bbmodel file from Sketchfab (use the "Download 3D Model" button; select the original format). The ArtsByKev Void Dragon is the primary choice.

Step 2: Open in Blockbench and convert to Java Block/Item format. Open the .bbmodel file in Blockbench (https://blockbench.net). If it's a Generic Model, convert it via File → Convert Project → Java Block/Item. This is necessary because Minecraft Java Edition resource packs only understand the Java Block/Item JSON format for item models.

Step 3: Check element count and simplify if needed. Minecraft item models can have many elements but practical limits apply. If the model exceeds ~200-300 elements, use Blockbench's editing tools to simplify (merge adjacent cubes, remove internal geometry, reduce detail on unseen faces). The model coordinates must all fit within the [-16, -16, -16] to [32, 32, 32] coordinate space.

Step 4: Retexture for the Abyss theme. If the downloaded model doesn't match the dark-purple/void aesthetic, retexture it in Blockbench. Use dark purples (#2d002d, #4a0080), deep blacks (#0d0d0d), glowing purple accents (#aa00ff, #7700cc), and touches of soul-fire blue (#00bbff) and crying-obsidian magenta (#cc00cc). The texture should be a power-of-two PNG (64×64, 128×128, or 256×256 depending on detail level).

Step 5: Export as Java Block/Item Model. File → Export → Export Block/Item Model. This produces a .json file and associated .png texture. Name them abyss_dragon.json and abyss_dragon.png.

Step 6: Place in the resource pack.

Model JSON → assets/minecraft/models/item/abyss/abyss_dragon.json
Texture PNG → assets/minecraft/textures/item/abyss/abyss_dragon.png
Ensure the model JSON references the texture correctly: "textures": {"0": "item/abyss/abyss_dragon"}
Step 7: Verify the item definition. Ensure assets/minecraft/items/paper.json has a "when": "abyss_dragon" case that points to "model": {"type": "minecraft:model", "model": "item/abyss/abyss_dragon"}.

Step 8: Test in-game. Build the resource pack (7-Zip), upload as v10.0, update docker-compose.yml, restart. Use /jglims legendary or the creative menu to verify the ItemDisplay renders the new model. Then trigger the dragon fight to test at scale 5.0.

Alternative: Multi-Part Display Entity Dragon
If a single-item-model approach doesn't produce satisfactory results (too blocky, too few elements allowed), an alternative is to construct the dragon from multiple ItemDisplay entities riding the same base Zombie (via Entity.addPassenger()), each displaying a different body part (head, body, left wing, right wing, tail, legs). This allows each part to have its own model with up to ~300 elements, effectively creating a 1200+ element composite dragon. Each part would have its own scale and rotation offset via Transformation. The AbyssDragonModel.java would need to be refactored to manage multiple displays and animate them semi-independently (wings flapping, head turning toward target, tail swaying). This is significantly more complex but would produce a dramatically better visual.

Multi-part approach code sketch:

Copy// In AbyssDragonModel spawn():
ItemDisplay body = spawnPart(location, "abyss_dragon_body", 5.0f);
ItemDisplay head = spawnPart(location.add(0, 1, 3), "abyss_dragon_head", 4.0f);
ItemDisplay leftWing = spawnPart(location.add(-3, 0.5, 0), "abyss_dragon_wing_left", 5.0f);
ItemDisplay rightWing = spawnPart(location.add(3, 0.5, 0), "abyss_dragon_wing_right", 5.0f);
ItemDisplay tail = spawnPart(location.add(0, 0, -4), "abyss_dragon_tail", 4.5f);

baseEntity.addPassenger(body);
baseEntity.addPassenger(head);
baseEntity.addPassenger(leftWing);
baseEntity.addPassenger(rightWing);
baseEntity.addPassenger(tail);

// Animation: use interpolated transformations for wing flapping
// body.setTransformation(...); body.setInterpolationDuration(10);
This approach leverages Paper's setTeleportDuration() and setInterpolationDuration() for smooth client-side animation without server-side tick overhead.

Q. OUTSTANDING WORK — ALL TASKS (Priority Order)
PRIORITY 1: HIGHEST — Abyss Dragon Rework
1a. Replace the Abyss Dragon model — Download the ArtsByKev "Void Dragon" from Sketchfab, convert to Java Block/Item format in Blockbench, retexture if needed to match the Abyss color palette (dark purple, void black, soul-fire accents), export as abyss_dragon.json + abyss_dragon.png, place in the resource pack, test at 5× scale on ItemDisplay.

Required resource pack files:

assets/minecraft/models/item/abyss/abyss_dragon.json — new high-quality model JSON
assets/minecraft/textures/item/abyss/abyss_dragon.png — new high-quality texture PNG (64×64 or 128×128)
assets/minecraft/items/paper.json — verify "when": "abyss_dragon" case exists and points to the correct model
1b. Evaluate multi-part display entity approach — If the single-model approach is still insufficient, refactor AbyssDragonModel.java to use multiple ItemDisplay passengers (body, head, wings, tail), each with their own model file. This requires creating 5 model JSONs and 5 texture PNGs (or a shared texture atlas), and refactoring the animation/movement code.

1c. Tune the Abyss Dragon boss fight — The fight mechanics in AbyssDragonBoss.java work but the experience needs polish: better attack visual feedback, more dramatic phase transitions, refined movement patterns (the dragon should swoop and dive rather than linearly slide between waypoints), and difficulty balancing.

1d. Test and fix the Abyssal Citadel — Verify the citadel generates correctly, check for structural gaps or floating blocks, ensure all 4 weapon chambers have correctly populated chests, verify the arena is sealed (barrier walls), and confirm the walkway connects properly to the arena.

PRIORITY 2: HIGH — Bug Fixes
2a. Creation Splitter ability bug — One of the two abilities (either "Reality Rift" primary or "World Break" alt) for the MYTHIC Creation Splitter (CMD 30059) or the ABYSSAL Creation Splitter Awakened (CMD 30062, "Creation's End" / "Dimensional Collapse") has a bug. Need to inspect both LegendaryPrimaryAbilities.java and LegendaryAltAbilities.java for the CREATION_SPLITTER and CREATION_SPLITTER_AWAKENED switch cases and test in-game.

2b. Abyssal Key custom texture — The Abyssal Key currently uses the vanilla Echo Shard texture. It needs a unique custom model/texture (purple crystal key design). Requires adding a model JSON, a texture PNG, and a "when" case in assets/minecraft/items/echo_shard.json for CMD 9001 / string "abyssal_key".

PRIORITY 3: HIGH — Missing Animated Textures
3a. Whisperwind Awakened animated texture — needs .png.mcmeta animation file. 3b. Creation Splitter Awakened animated texture — needs .png.mcmeta. 3c. Edge of the Astral Plane animated texture — needs .png.mcmeta. (Excalibur Awakened already has working animation from v4.0.6–v4.0.9. Requiem Awakened status needs verification.)

PRIORITY 4: MEDIUM — Performance Optimization
4a. Investigate TPS during gameplay with /tps. 4b. Limit entity counts from abilities (max 6 summoned entities per player). 4c. Cancel previous BukkitRunnables before starting new ones for the same ability. 4d. Reduce particle counts per tier if causing lag. 4e. Throttle structure generation checks in StructureManager.java (49 KB). 4f. Optimize RoamingBossManager.java (57 KB — may have expensive tick operations). 4g. Review EnchantmentEffectListener.java (69 KB — likely runs checks every damage/interact event).

PRIORITY 5: MEDIUM — New Content
5a. World Weapons category — New weapon type between COMMON and MYTHIC damage. New files: WorldWeapon.java (enum), WorldWeaponManager.java, WorldWeaponAbilities.java, WorldWeaponListener.java. Uses remaining unused Fantasy 3D / Majestica textures.

5b. Magic Items category — Unique items with 3 ability modes (left-click, right-click, crouch+right-click). Example: Wand of Wands (red shot, Vingardium Leviosa, Avada Kedavra). New package: magic/. Custom textures by JGlims.

5c. More structures, mini-bosses, world bosses — Follow existing roadmap Phases 21, 26, 27.

5d. Additional custom items — Legendary shields, fishing rods, pets, consumable battle items (War Horn, Grappling Hook, Warp Scroll).

PRIORITY 6: LOW — Future Dimensions
6a. Lunar Dimension — Moon-themed reduced-gravity dimension. Portal: 4×5 End Stone frame + flint and steel. Custom world generator with low gravity, space-themed structures, unique mobs.

6b. Aether Dimension — Classic Aether mod recreation. Portal: water bucket on glowstone frame. Floating sky islands, cloud blocks, flying mobs.

R. SUMMARY OF CURRENT STATUS
System	Status	Notes
63 Legendary Weapons (textures)	COMPLETE	All textures and 3D models working
63 Legendary Weapons (abilities)	COMPLETE	Except Creation Splitter bug
13 Armor Sets (inventory icons)	COMPLETE	All custom textures working
13 Armor Sets (worn textures)	COMPLETE	Equipment JSON + humanoid PNGs implemented
7 Power-Up Items (textures)	COMPLETE	String CMD working
7 Power-Up Items (effects)	COMPLETE	All functional
Infinity Gauntlet System	COMPLETE	Stones, Glove, Gauntlet, snap ability
Abyss Portal Activation	COMPLETE	4×5 purpur frame + key working
Abyss Dimension (world gen)	COMPLETE	Custom terrain generates correctly
Abyssal Citadel	BUILT	Needs testing/refinement
Abyss Dragon Model	NEEDS REWORK	Current model too basic, Sketchfab replacement planned
Abyss Dragon Boss Fight	FUNCTIONAL	Mechanics work, visual polish needed
Creation Splitter Ability	BUG	One ability malfunctioning
Abyssal Key Texture	MISSING	Uses vanilla Echo Shard appearance
Awakened Weapon Animations	PARTIAL	Excalibur Awakened done; 3 others missing
6 Server Events	COMPLETE	All functional
40 Structures	COMPLETE	All generating with loot and bosses
6 Roaming Bosses	COMPLETE	All spawning and functional
64 Custom Enchantments	COMPLETE	All effects implemented
Guild System	COMPLETE	CRUD, invites, friendly fire
Quest System	COMPLETE	6 quest lines
Blood Moon	COMPLETE	15% nightly chance
Guide Book (PT-BR)	COMPLETE	Multi-volume
Creative Menu	COMPLETE	All items browsable
World Weapons	NOT STARTED	Planned
Magic Items	NOT STARTED	Planned
Lunar Dimension	NOT STARTED	Planned
Aether Dimension	NOT STARTED	Planned

ACTUALLY THE CITADEL NEEDS FULL REWORK, AND THE DRAGON ISNT APPEARING WHEN I TYPE THE COMMAND, SO, THERE ARE EVEN MORE THINGS TO DO, AND WE STILL NEED TO MAKE THE SKETCHFAB MODELS WORK, IF IT WORK FOR THE ABYSS DRAGON, THEN WE WILL MAKE IT FOR ALL THE CUSTOM BOSSES/MINI-BOSSES AND MORE, CREATE THE MAGICAL MOBS, MUCH MORE THINGS, USING THE SKETCHFAB MOB MODELS AND STRUCTURES, YOU CAN SEE MY COLLECTIONS AT https://sketchfab.com/JGlims/collections and the github in https://github.com/JGlims/JGlimsPlugin, it is public. we will prioritize the models that have animation. read the instructions-citadel for more details building the citadel and read the schematic images
END OF DEFINITIVE PROJECT SUMMARY v12.0 
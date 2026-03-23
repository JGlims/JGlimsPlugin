JGLIMSPLUGIN — ULTIMATE DEFINITIVE PROJECT SUMMARY v16.0
Compiled: 2026-03-23 | Author: JGlims (jg.melo.lima2005@gmail.com) | Plugin Version: 2.0.0 | Target Version: 3.0.0

A. PROJECT METADATA
Repository: https://github.com/JGlims/JGlimsPlugin License: Private / Unlicensed (personal project) Primary Language: Java Build System: Gradle (Gradle Wrapper) Target API: PaperMC API 1.21.11-R0.1-SNAPSHOT Artifact: JGlimsPlugin-2.0.0.jar (711,934 bytes / ~712 KB) Java Toolchain Target: Java 21 (compilation), runs on Java 25 at runtime Group ID: com.jglims.plugin Main Class: com.jglims.plugin.JGlimsPlugin

JGlimsPlugin is a comprehensive, monolithic Minecraft server-side plugin for Paper 1.21+ servers. It adds an extensive custom content layer on top of vanilla Minecraft, consisting of 63 legendary weapons with unique dual-ability combat systems, 13 custom armor sets with worn texture rendering, 7 consumable power-up items, a 6-stone Infinity Gauntlet system with a snap ability, 64 custom enchantments, 6 random world events with boss encounters, 6 roaming world bosses, 40 procedurally-placed structures across 4 dimensions, a complete multi-line quest system, a guild system with friendly-fire control, and the entirely custom Abyss dimension featuring a massive gothic cathedral citadel and a multi-phase dragon boss fight. The plugin is written entirely in Java, built with Gradle, and targets the PaperMC API. It is designed for a small private server (max 15 players) running in a Docker container on an Oracle Cloud free-tier ARM64 instance with GeyserMC crossplay support for Bedrock Edition clients.

B. SERVER ENVIRONMENT — FULL SPECIFICATION
Cloud Provider: Oracle Cloud Infrastructure (OCI), Always Free tier Instance Shape: VM.Standard.A1.Flex (Ampere ARM64), 4 OCPUs, 24 GB RAM Operating System: Ubuntu (ARM64) Public IP Address: 144.22.198.184 SSH User: ubuntu (CONFIRMED — NOT opc) SSH Key File (local): C:\Users\jgmel\Documents\projects\server_minecraft\ssh-key-2026-02-25.key Open Ports: 25565/TCP (Java Edition), 19132/UDP (Bedrock Edition via GeyserMC), 8080/TCP (planned nginx for self-hosted resource pack), 22/TCP (SSH)

Docker Container Name: mc-crossplay Docker Image: itzg/minecraft-server (auto-updates Minecraft server binaries) Docker Compose File (on server): /home/ubuntu/minecraft-server/docker-compose.yml Data Volume Mount: /home/ubuntu/minecraft-server/data:/data (bind mount, NOT a named Docker volume)

Server Software: Paper 1.21.11-127 (PaperMC fork of Spigot/CraftBukkit) Java Runtime (server): OpenJDK 25.0.2+10-LTS (Eclipse Adoptium Temurin, Java 25) JVM Memory Allocation: 8 GB RAM (-Xmx8G)

Installed Server Plugins (besides JGlimsPlugin):

GeyserMC 2.9.4-SNAPSHOT provides protocol translation from Bedrock Edition to Java Edition, allowing players on phones, consoles, and Windows 10/11 Bedrock clients to connect to the Java server on port 19132/UDP. Floodgate 2.2.5-SNAPSHOT is the companion to GeyserMC that enables Bedrock players to join without a Java Edition account, automatically prefixing their usernames. SkinsRestorer 15.11.0 manages player skins across Java and Bedrock clients to ensure consistent skin rendering. Chunky 1.4.40 pre-generates world chunks in advance to reduce lag during exploration.

Server Configuration (docker-compose.yml environment overrides):

Online mode is disabled to support crossplay via GeyserMC. The default game mode is Creative. Maximum players is set to 15. View distance is 10 chunks and simulation distance is 6 chunks. Difficulty is configurable per-world (Hard in the Abyss dimension). The resource pack URL and SHA-1 hash are set as environment variables RESOURCE_PACK and RESOURCE_PACK_SHA1 in docker-compose.yml. The RESOURCE_PACK_ENFORCE variable is set to TRUE. These environment variables are injected into server.properties by the itzg/minecraft-server Docker image on EVERY container start, which means editing server.properties directly is useless — the changes will be overwritten. All server configuration changes that persist across restarts MUST be made in docker-compose.yml, followed by docker compose down && docker compose up -d.

CRITICAL RULE: Never edit server.properties directly. Always edit docker-compose.yml and recreate the container.

C. LOCAL DEVELOPMENT ENVIRONMENT
Developer Machine OS: Windows (PowerShell / cmd) Local Plugin Source Path: C:\Users\jgmel\Documents\projects\JGlimsPlugin\JGlimsPlugin Resource Pack Working Directory: C:\Users\jgmel\Documents\projects\JGlimsPlugin\resourcepack-work\JGlimsResourcePack Original Full Resource Pack (reference): C:\Users\jgmel\Documents\projects\JGlimsPlugin\JGlimsResourcePack-v2.1-server Server Minecraft Project Root: C:\Users\jgmel\Documents\projects\server_minecraft\

Build Configuration (build.gradle): The build file applies the java plugin, sets group to com.jglims.plugin and version to 2.0.0, uses a Java toolchain targeting version 21, resolves dependencies from Maven Central and the PaperMC Maven repository (https://repo.papermc.io/repository/maven-public/), declares a compile-only dependency on io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT, and configures the jar archive base name to JGlimsPlugin.

Plugin Descriptor (plugin.yml): Name is JGlimsPlugin, version is 2.0.0, main class is com.jglims.plugin.JGlimsPlugin, api-version is 1.21, author is JGlims. The soft-dependency list includes BetterModel (for future BetterModel integration). Three commands are defined: /jglims is the admin command with 14+ subcommands (reload, stats, enchants, sort, mastery, legendary, armor, powerup, bosstitles, gauntlet, menu, guia, help) requiring jglims.admin permission (defaults to ops); /guild handles guild operations (create, invite, join, leave, kick, disband, info, list); /guia (alias: /guide) distributes Portuguese-language guide books explaining all plugin systems.

D. CURRENT RESOURCE PACK STATE
Current Live Version: v9.0 Download URL: https://github.com/JGlims/JGlimsPlugin/releases/download/v9.0/JGlimsResourcePack-v9.0.zip SHA-1 Hash: 0e6151b68727aff120a9648c8f295078ccfee809 Pack Format: 75 (Minecraft 1.21.4+) Approximate Size: ~3.68 MB Enforcement: TRUE (players must accept the resource pack to join)

What Works Right Now (confirmed as of 2026-03-23):

All 63 legendary weapon textures and 3D models are working. These were sourced from the Fantasy Weapons and Blades of Majestica asset packs and converted to the Minecraft 1.21.4+ assets/minecraft/items/*.json format using string-based custom_model_data. Every weapon renders its correct custom model in hand and inventory. All 63 primary abilities and all 63 alternate abilities are functional.

All 13 armor set textures are working, both inventory icons AND worn textures. The worn armor textures use the 1.21.4+ equippable component system with assets/minecraft/equipment/*.json files pointing to PNGs under assets/minecraft/textures/entity/equipment/humanoid/ and humanoid_leggings/.

All 7 power-up item textures display correctly. Heart Crystal, Soul Fragment, Titan's Resolve, Phoenix Feather, KeepInventorer, Vitality Shard, and Berserker Mark all use string-based CustomModelDataComponent.

The Infinity Gauntlet and Thanos Glove render their custom 3D models correctly. All 6 Infinity Stone textures (Power, Space, Reality, Soul, Time, Mind) display correctly, fixed in resource pack v7.0.

The Abyss portal activation works. A 4-wide × 5-tall purpur frame combined with a right-click using the Abyssal Key item triggers the portal: the interior fills with END_GATEWAY blocks for 30 seconds and teleports the player into the Abyss dimension on entry, with a 5-second cooldown between teleportations.

A basic Abyss Dragon item model exists at assets/minecraft/models/item/abyss/abyss_dragon.json with a 64×64 texture, mapped to CustomModelData ID 40000 / string "abyss_dragon". It renders on the ItemDisplay entity in-game but is visually low-quality (11 cuboid elements, very blocky).

The plugin loads cleanly: JGlimsPlugin 2.0.0 initializes without errors, the Abyss dimension (world_abyss) is created, all events are loaded, and all listeners are registered.

What Does NOT Work / Needs Rework:

The Abyss Dragon model quality is poor. The current 11-element blocky representation made of cuboids does not look like a convincing dragon. The plan is to replace it with a high-quality Blockbench model rendered through BetterModel (see Section R).

The Abyss Dragon boss fight mechanics need tuning. The fight controller works, but the visual experience is underwhelming because the dragon model is a small blocky shape scaled up via ItemDisplay. Combat feel needs improvement.

The Abyssal Citadel (AbyssCitadelBuilder.java, 54 KB) generates a massive gothic cathedral structure but may have performance issues during generation. The plan is to rework it with async generation or schematic-based placement.

One Creation Splitter ability has a bug — needs identification of which ability (primary or alternate) in LegendaryPrimaryAbilities.java or LegendaryAltAbilities.java is malfunctioning.

Missing animated textures for awakened weapons: Whisperwind Awakened, Creation Splitter Awakened, and Edge of the Astral Plane should have animated texture variants (.png.mcmeta animation files) but do not. Excalibur Awakened has its animated texture working (fixed in v4.0.6–v4.0.9).

The Abyssal Key item uses a vanilla Echo Shard texture; it should have a unique custom texture.

E. RESOURCE PACK VERSION HISTORY
Version	Size	SHA-1	Description
v4.0.0	3.04 MB	63b8e30d...	pack_format 75, initial 63 weapons, item definitions for 1.21.4+
v4.0.2	3.09 MB	626edf29...	Real textures, 3D models from asset packs
v4.0.3	3.17 MB	6cd61288...	All 15 missing 3D weapon models from v2.1 pack
v4.0.4	3.19 MB	ca60f59f...	Fixed 5 remaining buggy weapons
v4.0.5	3.19 MB	2147a804...	Fixed Excalibur + Requiem Awakened
v4.0.6	3.19 MB	7d03a108...	Added animation mcmeta for Excalibur textures
v4.0.7	3.17 MB	c433786c...	Fix Excalibur 3D models: extract 128×128 frame from spritesheet
v4.0.8	3.17 MB	45aeea8a...	Excalibur Awakened: animated fire texture
v4.0.9	3.17 MB	1b9f9e6c...	Excalibur Awakened: majestica model with blue fire + effect-sheet
v4.1.0	3.52 MB	a7a0e72c...	Added armor, infinity, powerup textures + models. Bow models ready
v5.0	3.52 MB	dc5cdce0...	Repackaged for new release URL
v6.0	—	26704612...	Intermediate update
v7.0	—	a7212467...	Fixed Infinity Stone textures, Creation Splitter particle bug
v8.0	—	—	Abyss Dragon model added (basic 11-cuboid model)
v9.0	~3.68 MB	0e6151b6...	CURRENT — Dragon visual fix
BROKEN FILE WARNING: The file "v4.1.1" (9.5 MB, created by PowerShell's Compress-Archive) must NEVER be used. It has an invalid internal directory structure that causes the resource pack to fail to load.

F. HOW MINECRAFT 1.21.4+ CUSTOM TEXTURES WORK
Minecraft 1.21.4+ uses item model definitions located in assets/minecraft/items/*.json. Each file uses type: "minecraft:select" with property: "minecraft:custom_model_data" to choose which model to render based on the custom_model_data component's strings list. The Java plugin code sets the string via CustomModelDataComponent.setStrings(List.of("texture_name")). The "when" value in the JSON must exactly match the string set by the plugin. This is how all 63 legendary weapons, 7 power-ups, and special items get their custom models.

For worn armor textures in 1.21.4+, the equippable component must set an asset_id that points to an assets/minecraft/equipment/<set>.json file, which in turn references two texture layers (body layer and inner leggings layer) located in assets/minecraft/textures/entity/equipment/humanoid/ and humanoid_leggings/. Each of the 13 armor sets has its own equipment JSON and corresponding texture PNGs.

G. RESOURCE PACK UPDATE PROCEDURE (STEP-BY-STEP)
Step 1: Make changes to files in the resource-pack working directory (C:\...\resourcepack-work\JGlimsResourcePack).

Step 2: DO NOT use PowerShell's Compress-Archive cmdlet — it produces ZIP files with an incorrect internal structure that breaks Minecraft's resource pack loader. Always use 7-Zip CLI to create the ZIP, ensuring pack.mcmeta remains at the root of the archive. Command: & "C:\Program Files\7-Zip\7z.exe" a -tzip "$zipPath" "$workDir\*".

Step 3: Compute the SHA-1 hash: (Get-FileHash -Path $zipPath -Algorithm SHA1).Hash.ToLower().

Step 4: Upload as a NEW release version (v10.0, v11.0, etc. — never overwrite old versions): gh release create v10.0 $zipPath --repo JGlims/JGlimsPlugin --title "Resource Pack v10.0" --notes "SHA-1: $sha1".

Step 5: Update docker-compose.yml on the server with the new URL and SHA-1 via sed or manual editing over SSH.

Step 6: Recreate the container: docker compose down && docker compose up -d from /home/ubuntu/minecraft-server/.

H. PLUGIN DEPLOYMENT PROCEDURE (STEP-BY-STEP)
Step 1: Build the plugin from the local dev path: .\gradlew clean build. This produces build/libs/JGlimsPlugin-2.0.0.jar.

Step 2: SCP the JAR to the server: scp -i $sshKey build/libs/JGlimsPlugin-2.0.0.jar ubuntu@144.22.198.184:/home/ubuntu/.

Step 3: Docker-copy the JAR into the running container: docker cp /home/ubuntu/JGlimsPlugin-2.0.0.jar mc-crossplay:/data/plugins/JGlimsPlugin-2.0.0.jar.

Step 4: Fix file ownership inside the container (the itzg image runs as UID 1000): docker exec mc-crossplay chown 1000:1000 /data/plugins/JGlimsPlugin-2.0.0.jar.

Step 5: Restart the container: docker compose down && docker compose up -d, OR use docker exec mc-crossplay rcon-cli reload confirm for a hot-reload if only the JAR changed.

The same SCP → docker cp → chown → restart workflow applies to deploying BetterModel JARs and .bbmodel files.

I. SELF-HOSTED RESOURCE PACK (NGINX) — PLANNED
The plan is to install nginx on the OCI instance to serve the resource pack directly from the server, eliminating the need to create a new GitHub release for every pack update. The steps are: install nginx via sudo apt install nginx, create the directory /var/www/resourcepack/, configure an nginx server block listening on port 8080 that serves files from that directory, open port 8080 in the OCI security list and iptables, SCP the resource pack ZIP to /var/www/resourcepack/JGlimsResourcePack.zip, compute the SHA-1, update docker-compose.yml to point RESOURCE_PACK to http://144.22.198.184:8080/JGlimsResourcePack.zip with the new SHA-1, and restart the container. With this setup, the URL stays constant across updates; only the SHA-1 needs to change when the pack content changes. This dramatically simplifies the pack update workflow because there is no need to create new GitHub releases for each iteration.

J. COMPLETE CODEBASE OVERVIEW (68 Java Files, ~1.27 MB Total)
Root Package — com.jglims.plugin
JGlimsPlugin.java (34,896 B) — The main plugin class. The onEnable() method initializes all managers and listeners, registers all three commands (/jglims, /guild, /guia), creates the Abyss dimension via AbyssDimensionManager, and logs startup diagnostics. Contains the command dispatcher for the /jglims admin command with its 14+ subcommands.

abyss Package (5 files)
AbyssChunkGenerator.java (7,341 B) — A custom ChunkGenerator that produces a void-based terrain for world_abyss. Generates a roughly 210-block-radius landmass centered at origin using layered sinusoidal noise functions. Surface materials include deepslate tiles (inner ring), polished blackstone (mid ring), purpur blocks, and end stone (outer ring). Decorative features include random amethyst columns, obsidian spikes, end rods, crying obsidian, and soul campfires. Beyond radius 160, floating islands are generated at random Y levels. Void rivers are carved between radius 130–200. All vanilla generation systems (noise, surface, caves, decorations, mob spawning, structures) are fully disabled.

AbyssCitadelBuilder.java (54,253 B) — The largest structure builder in the entire plugin. Constructs a massive gothic cathedral called the "Abyssal Citadel" using a Blockwave Studios-inspired architectural design. The architecture consists of: an elliptical foundation platform (110×140 blocks), a circular courtyard with glowing crying obsidian lines (radius 40, centered at z=−65), a main nave (80 blocks long × 60 wide × 50 tall with gothic narrowing walls), a cross-shaped transept (extending 50 blocks on the X axis, 45 blocks tall), a semicircular apse (radius 25, 55 blocks tall), a massive front façade (60 half-width, 80 blocks tall) with a pointed-arch entrance (8 half-width, 25 blocks tall), a rose window (radius 9 with radiating spokes pattern), a gable with finial, a center mega-spire (180 blocks tall), 6 flanker spires (100–120 blocks), 15 minor spires (50–90 blocks), flying buttresses along the nave, gothic pointed-arch windows with purple glass panes throughout, a pointed roof, interior nave with two rows of pillars, interior side chapels with loot chests, 4 Abyssal Weapon guardian chambers (obsidian rooms with amethyst pedestals, weapon chests, soul fire, and purple glass windows), a covered walkway from the apse to the boss arena, and the boss arena itself (radius 40, bedrock floor, barrier walls, 8 end stone pillars with amethyst caps, end rods, crying obsidian accents, and soul campfire pedestals), plus rocky terrain features, floating amethyst crystal formations, and an approach path. The block palette uses deepslate bricks, deepslate tiles, polished deepslate, chiseled deepslate, blackstone, polished blackstone bricks, obsidian, crying obsidian, amethyst blocks/clusters, purple stained glass panes, magenta stained glass panes, iron bars, soul lanterns, soul campfire, end rods, end stone bricks, bedrock, barrier, purpur, and air. After construction, the builder populates chests with ABYSSAL and MYTHIC tier loot and spawns Enderman/Wither Skeleton guards.

AbyssDimensionManager.java (18,501 B) — Creates and manages the world_abyss world. Before world load, writes a paper-world.yml that disables scan-for-legacy-ender-dragon. Creates the world with THE_END environment using AbyssChunkGenerator. Disables game rules: mob spawning, weather cycle, fire tick, mob griefing, advancement announcements, death messages. Sets difficulty to HARD, time to permanent midnight. CRITICALLY, it actively disables the vanilla DragonBattle every 10 seconds by removing the phantom Ender Dragon boss bar, killing any stray EnderDragon entities, and hiding/removing the vanilla BossBar. If the citadel hasn't been built yet, triggers AbyssCitadelBuilder. Runs an ambient mob spawner that periodically places Endermen (named "Void Wanderer") and Wither Skeletons (named "Abyssal Sentinel") while avoiding the boss arena vicinity. Creates the Abyssal Key item (Echo Shard base material, CMD 9001, PersistentDataContainer tag abyssal_key). Handles portal interaction: RIGHT_CLICK_BLOCK on PURPUR_BLOCK while holding the Abyssal Key searches for a valid 4-wide × 5-tall purpur frame (inner opening 2×3), fills interior with END_GATEWAY blocks, plays sounds and particle effects, and auto-removes the gateway after 30 seconds. Player teleportation is triggered by PlayerMoveEvent detecting entry into active gateway blocks, with a 5-second cooldown. Teleport destination is z=−115.5 (south of citadel, facing north). Arena center constant: ARENA_CENTER_Z = -120.

AbyssDragonBoss.java (23,877 B) — Complete boss fight controller implementing Listener. Constants: 1500 HP, 25% damage reduction, arena radius 40 blocks, 30-minute respawn cooldown. The manualTrigger(Player) method validates the player is in world_abyss, checks cooldowns, then calls startFight(). Fight initialization finds the arena floor Y-level, generates 11 waypoints in a circle (8 perimeter + 1 center high + 2 dive points), creates an AbyssDragonModel, sets up a purple notched boss bar, shows titles/sounds to all players, and spawns lightning effects. Two concurrent scheduled tasks run: a combat loop (every 2 seconds / 40 ticks) that updates the boss bar progress, checks phase transitions at 50% HP (phase 2), 25% HP (phase 3), and 10% HP (phase 4/enrage), selects random nearby players for attacks, and checks for death; and a movement loop (every 0.5 seconds / 10 ticks) that moves the dragon toward the current waypoint at speed 0.8 + (phase × 0.2), with a 30% chance to divert toward a random player when reaching a waypoint. Attack methods vary by phase: Phase 1 has void breath (8+ damage, breath particles), lightning strike (6 damage), and wing gust (4 damage + knockback). Phase 2 adds summon minions (3–6 Endermen/Wither Skeletons). Phase 3 adds ground pound (10 damage, vertical knockback to players within 15 blocks) and void pull (portal particles, pulls players toward dragon). Phase 4 (enrage) adds enrage lightning barrage (8 rapid lightning strikes near target). Phase announcements show titles, and the boss bar changes to red at phase 3+. Damage handling blocks environmental damage on the base zombie entity, applies 25% reduction to player damage, and triggers a damage flash on the model. On death: cancels all tasks, zeroes boss bar, plays death animation, shows VICTORY title with fireworks, drops loot after 4-second delay (dragon egg, 2 nether stars, 1000 XP in 20 orbs, 1 guaranteed random ABYSSAL legendary weapon, 3 netherite ingots, 16 diamonds, 32 emeralds), builds a 3×3 END_GATEWAY exit portal, cleans up named minions, and hides the boss bar after 10 seconds.

AbyssDragonModel.java (17,894 B) — Visual model implementation using an invisible Zombie (hitbox entity) + ItemDisplay (visual entity). Constants: base scale 5.0, rage scale 5.75, enrage scale 6.25, CMD integer 40000, CMD string "abyss_dragon". The spawn() method creates an invisible, silent, no-AI, no-gravity adult Zombie with health capped at Math.min(maxHp, 2048.0) (requires spigot.yml attribute.maxHealth.max >= 2048.0). Sets SCALE attribute to 3.0 on the zombie for hitbox size. Creates a PAPER ItemStack with both legacy integer CMD (40000) and modern string-based CustomModelDataComponent ("abyss_dragon"), plus setItemModel(NamespacedKey.fromString("minecraft:abyss_dragon")). Spawns an ItemDisplay at +2Y with FIXED billboard, scale transformation of (5,5,5), and view range 2.0. Starts an invisibility enforcer (every 5 ticks, reapplies invisibility potion to zombie). Starts an animation loop (every 2 ticks) that bobs the display sinusoidally (±0.5 blocks), slowly rotates it around the Y axis (0.02 rad/tick), scales per phase (5.0 → 5.75 → 6.25), and spawns ambient particles (DRAGON_BREATH always, SOUL_FIRE_FLAME at phase 2+, PORTAL at phase 3+). Movement via moveToward() teleports the zombie toward a target at configurable speed, updating yaw to face the direction of travel. Attack visuals include breathAttackVisual() (draws a particle beam from display to target with dragon shoot sound), wingGustVisual() (spawns CLOUD + SWEEP_ATTACK particles with dragon flap sound). Phase transitions trigger growl, explosion particles, portal particles, and wither spawn sound at phase 3+. damageFlash() sets display brightness to max for 4 ticks and spawns DAMAGE_INDICATOR particles. playDeathAnimation() removes the zombie immediately, then runs a 60-tick shrink-and-spin animation (scale × 0.97 per tick, rotation += 0.3 per tick, rising translation) with continuous DRAGON_BREATH and PORTAL particles, explosion sounds every 5 ticks, culminating in EXPLOSION_EMITTER and ENDER_DRAGON_DEATH sound. cleanup() cancels all tasks and removes both entities.

blessings Package (2 files)
BlessingListener.java (3,452 B) — Listens for blessing crystal use events and routes to BlessingManager.

BlessingManager.java (10,296 B) — Manages three blessing types: C-Bless (heal, +1 HP per use, max 10 uses), Ami-Bless (+2% damage per use, max 10), La-Bless (+2% defense per use, max 10). Blessing stats are tracked per-player and displayed via /jglims stats.

config Package (1 file)
ConfigManager.java (29,211 B) — Reads all config.yml values and provides typed getter methods. Configuration sections cover: mob difficulty (distance-based scaling from 350 to 5000 blocks from spawn), biome multipliers for mob stats, boss enhancer multipliers (Ender Dragon 3.5× HP, Wither 1.0×, Warden 1.0×, Elder Guardian 2.0× HP), creeper spawn reduction (50% chance to cancel creeper spawns), Pale Garden fog effect toggle, loot booster, blessings, anvil modification (removes "Too Expensive" cap + 50% XP reduction for repairs), feature toggles for 10 independent systems, drop rate booster (35% trident drop from Drowned), villager trade modifications (50% price reduction, no trade locking), king mob configuration (every 100th mob spawned becomes a "King" variant, 7× HP), axe nerf (0.5 attack speed reduction), weapon mastery (max 1000 kills to reach +20% bonus), blood moon (15% chance per night, 1.2× boss HP during event), guilds (max 10 members, no friendly fire by default), dog armor (95% damage reduction for armored wolves), super tools (2% bonus per enchantment for netherite tools), and ore detection radii.

crafting Package (2 files)
RecipeManager.java (34,774 B) — Registers all custom shaped and shapeless recipes: 8 battle tool types (sword, axe, bow, mace, shovel, pickaxe, trident, spear) across material tiers, diamond and netherite super tools, sickles, Infinity Stone combining (colored glass + redstone → finished Stone), Infinity Gauntlet assembly (Thanos Glove + all 6 Stones), and all 24 craftable armor pieces (6 craftable sets × 4 slots).

VanillaRecipeRemover.java (588 B) — Removes conflicting vanilla recipes on startup.

enchantments Package (5 files)
AnvilRecipeListener.java (33,648 B) — Custom anvil UI interceptor handling enchantment combining, Infinity Stone assembly via anvil, and enchantment conflict detection.

CustomEnchantManager.java (8,853 B) — Defines all 64 custom enchantments with level caps, conflict pairs, and applicable item type restrictions.

EnchantmentEffectListener.java (69,495 B) — THE LARGEST SINGLE SOURCE FILE. Implements all 64 enchantment effects with their trigger conditions and gameplay mechanics. Each enchantment has its own effect handler.

EnchantmentType.java (1,942 B) — Enum categorizing enchantment types (weapon, armor, tool, etc.).

SoulboundListener.java (7,961 B) — Prevents items with the Soulbound enchantment from dropping on player death.

events Package (7 files)
EventManager.java (10,367 B) — Central event scheduler running every 60 seconds (1200 ticks). Checks for random event triggers, manages 10-minute per-world cooldowns, handles broadcasts, boss tagging, weapon drops, and miscellaneous loot drops.

NetherStormEvent.java (11,421 B) — Ghast swarms, enhanced Blaze spawns, fire rain particles, Infernal Overlord boss (Ghast base, 400 HP).

PiglinUprisingEvent.java (8,956 B) — Massive Piglin army spawn, Piglin Emperor boss (Piglin Brute base, 500 HP).

VoidCollapseEvent.java (8,961 B) — Void particles pulling players down, Void Leviathan boss (Elder Guardian variant, 500 HP).

PillagerWarPartyEvent.java (19,957 B) — Overworld Pillager raid with escalating waves.

PillagerSiegeEvent.java (19,146 B) — Nighttime Pillager siege with Fortress Warlord boss.

EndRiftEvent.java (30,058 B) — Creates a 15×15 portal, spawns waves of End mobs, End Rift Dragon boss (600 HP Ender Dragon variant).

guilds Package (2 files)
GuildListener.java (1,456 B) — Guild event hooks.

GuildManager.java (13,679 B) — Full CRUD operations for guilds, invitation system, friendly fire toggle, persistence via flat-file storage.

legendary Package (15 files — the core of the plugin)
LegendaryTier.java (3,673 B) — 5-tier enum. COMMON: 12–15 damage, 10 particle budget, 1.0× cooldown multiplier, white color. RARE: 14–17 damage, 25 particles, 0.95× cooldown, aqua color. EPIC: 17–20 damage, 50 particles, 0.9× cooldown, gold color. MYTHIC: 20–30 damage, 100 particles, 0.85× cooldown, light purple color. ABYSSAL: 28–40 damage, 200 particles, 0.8× cooldown, dark red (RGB 170,0,0). Includes backward-compatibility mappings: "LEGENDARY" → EPIC, "UNCOMMON" → COMMON.

LegendaryWeapon.java (15,034 B) — Enum of 63 weapons with fields: id, displayName, baseMaterial, baseDamage, customModelData (legacy int 30001–30063), tier, textureName (the critical string that must match the resource pack JSON "when" value), primaryAbilityName, altAbilityName, primaryCooldown, altCooldown. Distribution: 20 COMMON, 8 RARE, 7 EPIC, 24 MYTHIC, 4 ABYSSAL. Provides fromId(), byTier(), byMaterial().

LegendaryWeaponManager.java (7,937 B) — Creates weapon ItemStacks with string-based CustomModelDataComponent, PersistentDataContainer tags, tier-colored display names via Adventure API, and lore showing ability names and cooldowns. All items are set as Unbreakable.

LegendaryAbilityListener.java (18,032 B) — Event dispatcher: right-click → primary ability, crouch + right-click → alternate ability. Routes to the appropriate handler based on the weapon's PersistentDataContainer tag.

LegendaryPrimaryAbilities.java (69,951 B) — Implementation of all 63 primary abilities. Each weapon has a unique ability with particles, damage calculations, entity spawning, and cooldowns.

LegendaryAltAbilities.java (73,055 B) — Implementation of all 63 alternate abilities. The second-largest source file in the codebase.

LegendaryAbilityContext.java (4,941 B) — Shared state maps (bloodlust stacks, retribution damage, soul collector counts, phase shift active status, etc.) and utility methods: getNearbyEnemies(), dealDamage(), getTargetEntity(), rotateY(), spawnThrottled() (particle throttle respecting tier budgets), trackTask(), trackSummon(), cleanupPlayer().

LegendaryArmorSet.java (16,160 B) — Enum of 13 armor sets. Each set defines: id, displayName, tier, base material per slot (helmet/chestplate/leggings/boots), command IDs per slot (range 30101–30254), total defense value, set-bonus description, passive ability strings per piece, and texture file names per piece. Craftable sets: Reinforced Leather (12 def), Copper (14 def), Chainmail Reinforced (15 def), Amethyst (16 def), Bone (16 def), Sculk (18 def). Legendary sets: Shadow Stalker (24 def, RARE), Blood Moon (30 def, EPIC), Nature's Embrace (28 def, EPIC), Frost Warden (30 def, EPIC), Void Walker (36 def, MYTHIC), Dragon Knight (40 def, MYTHIC), Abyssal Plate (50 def, ABYSSAL, set bonus: +30% damage, Wither immunity).

LegendaryArmorManager.java (16,059 B) — Creates armor ItemStacks with the equippable component, sets asset_id for worn texture rendering, applies passives and set bonuses.

LegendaryArmorListener.java (38,729 B) — Implements all set bonuses and per-piece passive effects for all 13 armor sets.

LegendaryLootListener.java (22,565 B) — Drop table logic for bosses, structures, and chests. Tiered probability system.

InfinityStoneManager.java (8,029 B) — 6 stone types: Power (purple), Space (blue), Reality (red), Soul (orange), Time (green), Mind (yellow). Handles fragment creation, finished stone creation, and anvil combining logic.

InfinityGauntletManager.java (23,370 B) — Creates the Thanos Glove, Infinity Gauntlet, and implements the snap ability: right-click kills 50% of all loaded hostile mobs in the current dimension with a 300-second cooldown. Includes snap animation with particle effects and sounds.

menu Package (2 files)
CreativeMenuManager.java (45,503 B) — GUI inventory menus for browsing all weapons (filterable by tier), armor sets, power-ups, Infinity Stones, and other items. Supports pagination with next/previous buttons.

GuideBookManager.java (29,396 B) — Generates multi-volume written books in Portuguese (PT-BR) explaining all plugin systems. Given to players via the /guia command.

mobs Package (7 files)
BiomeMultipliers.java (3,080 B) — Biome-specific mob stat scaling lookups.

BloodMoonManager.java (12,329 B) — Blood Moon event system: 15% chance per night, red sky visual effect, mob stat buffs, boss-tier mob spawns every 10th regular mob during the event, double drops from all mobs, 0.1% chance to drop Infinity Stone fragments.

BossEnhancer.java (19,998 B) — Enhances vanilla bosses with custom abilities: Ender Dragon 3.5× HP with custom attack patterns, Wither 1.0× with abilities, Warden 1.0× with abilities, Elder Guardian 2.0× HP with abilities. Each boss gets custom attack patterns and visual effects.

BossMasteryManager.java (12,294 B) — Tracks boss kill participation and awards permanent titles with passive bonuses: Wither Slayer (+5%), Guardian Slayer (+7%), Warden Slayer (+10%), Dragon Slayer (+15%), Abyssal Conqueror (+20%), God Slayer (+25% damage / +20% defense).

KingMobManager.java (5,972 B) — Every Nth mob spawn (configurable, default every 100th) becomes a "King" mob with 7× HP, 2.5× damage, and diamond drops.

MobDifficultyManager.java (4,744 B) — Distance-from-spawn scaling system. Mobs become progressively harder as players venture further from spawn (350-block intervals up to 5000 blocks).

RoamingBossManager.java (57,427 B) — The second-largest source file. Implements 6 roaming world bosses: The Watcher (Deep Dark biome, Warden base, 800 HP, EPIC drops, Darkness + Mining Fatigue aura), Hellfire Drake (Nether wastes, Ghast base, 600 HP, EPIC drops, triple fireball salvo), Frostbound Colossus (Snowy biomes, Iron Golem base, 700 HP, EPIC drops, freeze aura + ground pound), Jungle Predator (Jungle, Ravager base, 500 HP, RARE/EPIC drops, stealth pounce + poison fog), End Wraith (The End outer islands, Phantom size 6, 900 HP, MYTHIC drops, void beam + phase shift + shadow clones at 50% HP), Abyssal Leviathan (Abyss dimension, 1200 HP, ABYSSAL drops). Each boss has a 24% spawn chance per 2-minute check, auto-despawn after 10 minutes, custom particle effects, unique attack patterns per phase, and tier-appropriate weapon/loot drops.

powerups Package (2 files)
PowerUpManager.java (26,207 B) — 7 power-up items, all using string-based CustomModelDataComponent. Heart Crystal (RED_DYE base, +2 max HP per use, max 40 uses = +80 HP). Soul Fragment (PURPLE_DYE, +1.0% damage per use, max 100 = +100%). Titan's Resolve (IRON_NUGGET, +10% knockback resistance + 2% DR per use, max 5 = 50% KB resist + 10% DR). Phoenix Feather (FEATHER, auto-revive on death with 4 hearts, stackable/consumes one per death). KeepInventorer (ENDER_EYE, permanent keep-inventory flag, one-time use). Vitality Shard (PRISMARINE_SHARD, +5% DR per use, max 10 = 50% DR). Berserker's Mark (BLAZE_POWDER, +3% attack speed per use, max 10 = 30%). Maximum total passive DR is capped at 75%.

PowerUpListener.java (5,607 B) — Handles right-click consumption events and death events (Phoenix Feather auto-revive, KeepInventory enforcement).

quests Package (3 files)
QuestManager.java (27,940 B) — 6 quest lines (Overworld, Nether, End, Special, Boss, Explorer) with progress tracking, tiered rewards, and a scheduler.

QuestProgressListener.java (3,231 B) — Monitors kills, exploration milestones, and item collection for quest advancement.

NpcWizardManager.java (15,501 B) — Archmage NPC villager (purple robes) that sells exclusive MYTHIC weapons (rotating stock of 3 for 48 diamonds + 16 Nether Stars each), Heart Crystals, Phoenix Feathers, KeepInventorer (64 diamonds + 32 emeralds), and Blessing Crystals.

structures Package (5 files)
StructureType.java (10,780 B) — Enum of 40 structure types: 24 Overworld (Ruined Colosseum, Druid's Grove, Shrek House, Gigantic Castle, Thanos Temple, Pillager Fortress, Pillager Airship, Frost Dungeon, Bandit Hideout, Sunken Ruins, Cursed Graveyard, Sky Altar, and more), 7 Nether (Crimson Citadel, Soul Sanctum, etc.), 5 End (Void Shrine, Ender Monastery, Dragon's Hoard, etc.),█                                                                                                                                                               And thats the previous chat for you to have more context: very very very very very good, but I want it to be even better, remember, dont invent thins we didnt even commented or will do like hardcore missions like the solo thing, or the fishing overhaul ... Focus in what we actually planned, and remember all of the information, always check the github repository and search more always, here is the previous summaries, read then too, and create the definitive, more complete, better and more extremely detailed summary: perfect, but I want you to make this summary even more complete, even more detailed, even more deep, in everything, everything, deeply. Remember the information about code view plugin in blockbench and the other plugins, I want this summary to be much bigger, well structure, deep, complete, extremely detailed, everything. Remember the current summary: JGLIMSPLUGIN — ULTIMATE DEFINITIVE PROJECT SUMMARY v14.0
Compiled: 2026-03-23 | Author: JGlims (jg.melo.lima2005@gmail.com) | Plugin Version: 2.0.0 | Target Version: 3.0.0

A. PROJECT METADATA
Repository: https://github.com/JGlims/JGlimsPlugin (public, commit 06c51025)

Plugin: JGlimsPlugin v2.0.0 — A comprehensive Minecraft server-side plugin for Paper 1.21+ featuring 63 legendary weapons with unique dual abilities, 13 custom armor sets, 7 power-up consumables, an Infinity Gauntlet system, 64 custom enchantments, 6 random server events, 6 roaming world bosses, 40 procedurally-placed structures, a complete quest system, guild system, and the custom Abyss dimension with a gothic cathedral citadel and multi-phase dragon boss fight. Written in Java, built with Gradle, targeting the PaperMC API.

Sketchfab Collections: https://sketchfab.com/JGlims/collections — The user's curated collections of downloadable 3D models (dragons, demons, creatures, structures) for importing into Blockbench → BetterModel. Models WITH existing Blockbench animations are prioritized because BetterModel can play them directly. The Sketchfab page is JavaScript-rendered and must be browsed in a browser. All models used must respect their individual licenses (CC-BY, CC0, etc.).

A1. Server Environment (Complete)
The server runs inside a Docker container named mc-crossplay on an Oracle Cloud ARM64 (Ampere) free-tier instance. This is the same machine that serves the resource pack (see Section B2 for the self-hosting approach).

Parameter	Value
Public IP	144.22.198.184
Java Edition port	25565
Bedrock Edition port	19132 (via GeyserMC)
Server software	Paper 1.21.11-127
Java version	OpenJDK 25.0.2+10-LTS (Eclipse Adoptium Temurin)
GeyserMC	2.9.4-SNAPSHOT
Floodgate	2.2.5-SNAPSHOT
SkinsRestorer	15.11.0
Chunky	1.4.40
BetterModel	2.1.0 (planned/installing)
JVM RAM	8 GB
Online mode	Disabled (for crossplay)
Default game mode	Creative
Max players	15
View distance	10 chunks
Simulation distance	6 chunks
Difficulty	Per-world (Hard in Abyss)
Docker image	itzg/minecraft-server
Container name	mc-crossplay
SSH Access: ssh -i "C:\Users\jgmel\Documents\projects\server_minecraft\ssh-key-2026-02-25.key" ubuntu@144.22.198.184 — The user is ubuntu, confirmed NOT opc. The SSH key must be used for all remote operations.

Local Development Path: C:\Users\jgmel\Documents\projects\JGlimsPlugin\JGlimsPlugin

Resource Pack Working Directory: C:\Users\jgmel\Documents\projects\JGlimsPlugin\resourcepack-work\JGlimsResourcePack

Server Resource Pack Source: C:\Users\jgmel\Documents\projects\JGlimsPlugin\JGlimsResourcePack-v2.1-server

Docker Compose File (on server): /home/ubuntu/minecraft-server/docker-compose.yml

Data Volume Mount: /home/ubuntu/minecraft-server/data:/data (bind mount, NOT Docker volume)

A2. Build System (Complete)
Build Output: JGlimsPlugin-2.0.0.jar (711,934 bytes)

Build Command: .\gradlew clean build (from local dev path)

Build Time: ~8 seconds

Build Warnings (9 total, all non-fatal):

6× GameRule deprecation warnings in AbyssDimensionManager.java (DO_MOB_SPAWNING, DO_WEATHER_CYCLE, DO_FIRE_TICK, MOB_GRIEFING, ANNOUNCE_ADVANCEMENTS, SHOW_DEATH_MESSAGES are deprecated and marked for removal)
3× OldEnum.name() deprecation in LegendaryArmorListener.java (biome name resolution)
Actual build.gradle (confirmed from repo):

Copyplugins {
    id 'java'
}

group = 'com.jglims.plugin'
version = '2.0.0'

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
    compileOnly 'io.github.toxicity188:bettermodel-bukkit-api:2.1.0'
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
}

tasks.jar {
    archiveBaseName.set('JGlimsPlugin')
}
CRITICAL DISCOVERY: The build.gradle already includes the BetterModel API dependency (compileOnly 'io.github.toxicity188:bettermodel-bukkit-api:2.1.0'). This means the project is already set up to compile against BetterModel's API — no additional dependency changes are needed. The build completed successfully with this dependency (as confirmed in build_output.txt).

Plugin Descriptor (plugin.yml): Name JGlimsPlugin, version 2.0.0, main class com.jglims.plugin.JGlimsPlugin, api-version 1.21, author JGlims. Commands: /jglims, /guild, /guia. NOTE: Should add softdepend: [BetterModel] to ensure BetterModel loads first if present.

B. RESOURCE PACK STATE — COMPLETE TECHNICAL DEEP DIVE
B1. Current Live Pack
The server is running resource pack v9.0, hosted on GitHub Releases.

Current docker-compose.yml settings:

CopyRESOURCE_PACK: https://github.com/JGlims/JGlimsPlugin/releases/download/v9.0/JGlimsResourcePack-v9.0.zip
RESOURCE_PACK_SHA1: 0e6151b68727aff120a9648c8f295078ccfee809
RESOURCE_PACK_ENFORCE: TRUE
CRITICAL RULE: The Docker image itzg/minecraft-server overwrites server.properties from environment variables on EVERY container start. Editing server.properties directly is USELESS. You MUST edit docker-compose.yml and run docker compose down && docker compose up -d.

B2. Resource Pack Hosting — The "Localhost" / Oracle Cloud Self-Hosting Approach (DETAILED)
During earlier conversations in this project, we explored and discussed the concept of self-hosting the resource pack directly from the Oracle Cloud instance rather than relying on GitHub Releases. Here is the complete technical understanding:

How Minecraft Resource Packs Are Delivered to Clients:

When a player connects to a Minecraft Java server, the server sends a resource pack URL (defined in server.properties via the resource-pack= field, which in our case comes from the RESOURCE_PACK env var in docker-compose.yml). The Minecraft client then downloads the ZIP from that URL via standard HTTP/HTTPS. The URL must be publicly accessible on the internet — the client's computer (not the server) downloads from this URL.

The GitHub Releases Approach (CURRENT):

Currently, each resource pack version is uploaded to GitHub Releases. The URL is https://github.com/JGlims/JGlimsPlugin/releases/download/vX.Y/JGlimsResourcePack-vX.Y.zip. This works reliably, but has overhead: every update requires creating a new GitHub release, computing the SHA-1, updating the docker-compose.yml, and recreating the container.

The Self-Hosted / Oracle Cloud HTTP Approach (EXPLORED, NOT YET IMPLEMENTED):

The idea is to run a lightweight HTTP server (like nginx or a simple Python HTTP server) on the Oracle Cloud instance itself (144.22.198.184), serve the resource pack ZIP from there, and point server.properties to something like http://144.22.198.184:8080/resourcepack.zip. This has several advantages:

Faster iteration: You can SCP a new ZIP to the server, replace the file, and it's instantly live. No GitHub release needed.
No SHA-1 update required if the URL stays the same: Actually, the SHA-1 MUST still be updated if the file contents change, because the client uses it to verify the download and to decide whether to re-download. If the SHA-1 is wrong, the client either rejects the pack or uses a stale cached version.
Resource pack can be updated without restarting the Minecraft server: If using a plugin that sends the pack URL to players on join, you can update the file and new joiners get the new pack. However, already-connected players would need to re-join.
How to Set It Up (Step by Step):

Install nginx on the Oracle Cloud instance:

Copyssh -i $sshKey ubuntu@144.22.198.184
sudo apt update && sudo apt install -y nginx
Create a directory for the resource pack:

Copysudo mkdir -p /var/www/resourcepack
sudo chown ubuntu:ubuntu /var/www/resourcepack
Configure nginx to serve the file:

Copysudo tee /etc/nginx/sites-available/resourcepack > /dev/null <<'EOF'
server {
    listen 8080;
    server_name _;
    
    location /resourcepack.zip {
        alias /var/www/resourcepack/JGlimsResourcePack.zip;
        types { application/zip zip; }
    }
}
EOF
sudo ln -sf /etc/nginx/sites-available/resourcepack /etc/nginx/sites-enabled/
sudo nginx -t && sudo systemctl reload nginx
Open port 8080 in Oracle Cloud:

Go to Oracle Cloud Console → Networking → Virtual Cloud Networks → your VCN → Security Lists
Add an Ingress Rule: Source 0.0.0.0/0, Protocol TCP, Destination Port 8080
Also open in the instance's iptables:
Copysudo iptables -I INPUT -p tcp --dport 8080 -j ACCEPT
sudo netfilter-persistent save
Upload the resource pack:

Copyscp -i $sshKey JGlimsResourcePack-merged.zip ubuntu@144.22.198.184:/var/www/resourcepack/JGlimsResourcePack.zip
Update docker-compose.yml:

CopyRESOURCE_PACK: http://144.22.198.184:8080/resourcepack.zip
RESOURCE_PACK_SHA1: <new_sha1>
RESOURCE_PACK_ENFORCE: TRUE
Future updates become simple:

Copy# Build the new merged pack
# Compute SHA-1 locally
$sha1 = (Get-FileHash -Path .\merged_pack.zip -Algorithm SHA1).Hash.ToLower()
# Upload to server
scp -i $sshKey merged_pack.zip ubuntu@144.22.198.184:/var/www/resourcepack/JGlimsResourcePack.zip
# Update SHA-1 in docker-compose.yml
ssh -i $sshKey ubuntu@144.22.198.184 "cd /home/ubuntu/minecraft-server && sed -i 's/RESOURCE_PACK_SHA1=.*/RESOURCE_PACK_SHA1=$sha1/' docker-compose.yml && docker compose down && docker compose up -d"
IMPORTANT CAVEATS:

The URL must be accessible from the PLAYER'S computer, not from the server. Using localhost or 127.0.0.1 in the resource pack URL would NOT work because the player's Minecraft client would try to connect to its own machine. You must use the public IP 144.22.198.184.
HTTPS is recommended but not required. For HTTPS, you'd need a domain name and Let's Encrypt certificate. HTTP works fine for resource packs.
Oracle Cloud's free tier has 10 TB/month outbound bandwidth, which is more than sufficient for resource pack delivery.
STATUS: This approach was DISCUSSED but NOT YET IMPLEMENTED. The current system still uses GitHub Releases.
Hybrid Approach (Recommended):

Keep using GitHub Releases for "stable" versions (v9.0, v10.0, etc.), but set up the self-hosted nginx for rapid development/testing iterations. During development, point docker-compose.yml at the self-hosted URL. For "releases," update to the GitHub URL.

B3. BetterModel Resource Pack Generation and Merging (DETAILED)
BetterModel Config (config.yml):

Copynamespace: bettermodel
pack-type: folder  # or "zip"
build-folder-location: BetterModel/build
When BetterModel processes .bbmodel files from plugins/BetterModel/models/, it generates resource pack assets in plugins/BetterModel/build/. The generated pack structure is:

BetterModel/build/
└── assets/
    └── bettermodel/
        ├── models/
        │   └── item/
        │       └── <model_name>/
        │           ├── part_0.json
        │           ├── part_1.json
        │           └── ...
        └── textures/
            └── item/
                ├── a.png
                ├── b.png
                └── c.png
BetterModel splits each bone group of the .bbmodel into a separate Minecraft item model JSON file (since each display entity can only render one item model). It also extracts the embedded textures from the .bbmodel and saves them as separate PNG files (named a.png, b.png, c.png, etc., alphabetically by the order they appear in the .bbmodel's "textures" array).

The Merging Process:

The JGlims resource pack (assets/minecraft/...) and the BetterModel-generated pack (assets/bettermodel/...) must be combined into a single ZIP. Since they use different namespaces (minecraft vs bettermodel), there are NO file conflicts — you simply include both assets/minecraft/ and assets/bettermodel/ directories in the same ZIP along with pack.mcmeta.

The merged pack is what gets deployed to players. The repository already contains:

merged_pack/ — the working directory with both namespaces merged
JGlimsResourcePack-merged.zip (6.5 MB) — the compiled merged pack
Current merged_pack/assets/bettermodel/ contents:

merged_pack/assets/bettermodel/textures/item/
├── a.png (133,528 bytes) — primary dragon texture
├── b.png (61,033 bytes) — secondary dragon texture  
└── c.png (37,745 bytes) — tertiary dragon texture
These PNG files are the textures extracted from the Void_Dragon .bbmodel file by BetterModel. The a.png is the main 1024×1024 texture atlas. These files are what BetterModel's generated item models reference.

How to Update the Texture from Now On:

If you modify the texture in Blockbench (Paint tab), save the .bbmodel.
Copy the updated .bbmodel to plugins/BetterModel/models/ on the server.
Run /bettermodel reload — BetterModel regenerates the resource pack, including new texture PNGs.
Copy the regenerated textures from plugins/BetterModel/build/assets/bettermodel/textures/item/ to your local merged_pack/assets/bettermodel/textures/item/.
Re-zip the merged pack, compute SHA-1, deploy.
Alternatively, for faster iteration with self-hosting:

scp the updated .bbmodel to the server's BetterModel models folder.
Run /bettermodel reload in-game.
BetterModel regenerates the pack in its build folder.
scp the entire BetterModel/build/ contents into your local merged_pack.
Re-zip, SCP to nginx directory, update SHA-1, restart container.
B4. Item Model Reference Chain — The Dragon (FORENSIC ANALYSIS)
The dragon not appearing is traceable through the item model reference chain. Here is the exact current state from the repository:

File 1: merged_pack/assets/minecraft/items/abyss_dragon.json (129 bytes)

Copy{
  "model": {
    "type": "minecraft:model",
    "model": "minecraft:item/abyss_dragon"
  },
  "hand_animation_on_swap": false
}
This is a standalone item definition file for the minecraft:abyss_dragon item model key. When AbyssDragonModel.java calls meta.setItemModel(NamespacedKey.fromString("minecraft:abyss_dragon")), Minecraft looks for assets/minecraft/items/abyss_dragon.json and finds this file. It says: render the model at minecraft:item/abyss_dragon — which resolves to assets/minecraft/models/item/abyss_dragon.json.

File 2: merged_pack/assets/minecraft/items/paper.json (542 bytes)

Copy{
  "model": {
    "type": "minecraft:select",
    "property": "minecraft:custom_model_data",
    "fallback": {
      "type": "minecraft:model",
      "model": "minecraft:item/paper"
    },
    "cases": [
      {
        "when": "abyss_dragon",
        "model": {
          "type": "minecraft:model",
          "model": "minecraft:item/abyss_dragon"
        }
      },
      {
        "when": "40000",
        "model": {
          "type": "minecraft:model",
          "model": "minecraft:item/abyss/abyss_dragon"
        }
      }
    ]
  }
}
This is the Paper item definition. It has TWO cases: "abyss_dragon" (string CMD) → item/abyss_dragon, and "40000" (legacy integer) → item/abyss/abyss_dragon. These point to TWO DIFFERENT model files! This inconsistency could cause issues.

File 3: paper.json.bak (481 bytes) — backup version

Copy{
    "model": {
        "type": "minecraft:select",
        "property": "minecraft:custom_model_data",
        "cases": [
            {
                "when": "abyss_dragon",
                "model": {
                    "type": "minecraft:model",
                    "model": "minecraft:item/abyss/abyss_dragon"
                }
            }
        ],
        "fallback": {
            "type": "minecraft:model",
            "model": "minecraft:item/paper"
        }
    }
}
The backup version only has the string case pointing to item/abyss/abyss_dragon.

The Java Code (from fix_dragon_model.ps1):

Copymeta.setItemModel(NamespacedKey.fromString("minecraft:abyss_dragon"));
// AND
CustomModelDataComponent cmd = meta.getCustomModelDataComponent();
cmd.setStrings(List.of(CUSTOM_MODEL_DATA_STRING));  // "abyss_dragon"
Diagnosis: The code sets BOTH setItemModel("minecraft:abyss_dragon") AND string CMD "abyss_dragon". The setItemModel() method in 1.21.4+ takes priority — it directly resolves to assets/minecraft/items/abyss_dragon.json, which points to model item/abyss_dragon. The question is: does assets/minecraft/models/item/abyss_dragon.json exist as a model file (not an items file)? Looking at the repo, model files are in merged_pack/assets/minecraft/models/item/ and there IS a subdirectory abyss/ containing abyss_dragon.json. But the items file points to item/abyss_dragon (WITHOUT the abyss/ subdirectory). This mismatch likely means the model file is NOT being found, causing an invisible rendering.

THE FIX (if staying with ItemDisplay approach): Either:

(A) Move models/item/abyss/abyss_dragon.json to models/item/abyss_dragon.json, OR
(B) Change items/abyss_dragon.json to reference "model": "minecraft:item/abyss/abyss_dragon", OR
(C) Migrate to BetterModel, which handles all of this automatically.
B5. Resource Pack Version History
Version	Size	SHA-1	Description
v4.0.0	3.04 MB	63b8e30d...	pack_format 75, initial 63 weapons, 1.21.4+ item definitions
v4.0.2	3.09 MB	626edf29...	Real textures, 3D weapon models
v4.0.3	3.17 MB	6cd61288...	All 15 missing 3D weapon models from v2.1 pack
v4.0.4	3.19 MB	ca60f59f...	Fixed 5 remaining buggy weapons
v4.0.5	3.19 MB	2147a804...	Fixed excalibur + requiem awakened
v4.0.6	3.19 MB	7d03a108...	Added animation mcmeta for excalibur textures
v4.0.7	3.17 MB	c433786c...	Fix Excalibur 3D models: extract 128×128 frame from spritesheet
v4.0.8	3.17 MB	45aeea8a...	Excalibur Awakened: animated fire texture
v4.0.9	3.17 MB	1b9f9e6c...	Excalibur Awakened: majestica model with blue fire + effect-sheet
v4.1.0	3.52 MB	a7a0e72c...	Added armor, infinity, powerup textures + models. Bow models ready
v5.0	3.52 MB	dc5cdce0...	Repackaged for new release URL
v6.0	—	26704612...	Intermediate update
v7.0	—	a7212467...	Fixed Infinity Stone textures, Creation Splitter particle bug
v8.0	—	—	Abyss Dragon model added
v9.0	~3.68 MB	0e6151b6...	CURRENT — Dragon visual fix
v4.1.1	9.5 MB	—	BROKEN — Created by PowerShell Compress-Archive. NEVER USE
B6. How Minecraft 1.21.4+ Custom Textures Work
Minecraft 1.21.4+ uses item model definitions in assets/minecraft/items/*.json. There are two mechanisms:

Mechanism 1: setItemModel() (newer) — The Java code calls meta.setItemModel(NamespacedKey.fromString("minecraft:my_item")). Minecraft looks for assets/minecraft/items/my_item.json. This file defines which model to render. The file can be a direct model reference or a conditional select.

Mechanism 2: Custom Model Data strings — The items file for the base material (e.g., diamond_sword.json) uses type: "minecraft:select" with property: "minecraft:custom_model_data" and a "when" value matching the string set by CustomModelDataComponent.setStrings(List.of("texture_name")).

For worn armor textures, the equippable component sets an asset_id pointing to assets/minecraft/equipment/<name>.json, which references textures in assets/minecraft/textures/entity/equipment/humanoid/ and humanoid_leggings/.

B7. How to Update the Resource Pack (Step-by-Step)
Copy# 1. Make changes in the working directory
cd C:\Users\jgmel\Documents\projects\JGlimsPlugin\resourcepack-work\JGlimsResourcePack

# 2. Zip using 7-Zip (NOT Compress-Archive!)
& "C:\Program Files\7-Zip\7z.exe" a -tzip "C:\Users\jgmel\Documents\projects\JGlimsPlugin\JGlimsResourcePack-v10.0.zip" ".\*"

# 3. Get SHA-1
$sha1 = (Get-FileHash -Path ".\JGlimsResourcePack-v10.0.zip" -Algorithm SHA1).Hash.ToLower()
Write-Host "SHA-1: $sha1"

# 4. Upload to GitHub as NEW release
gh release create v10.0 ".\JGlimsResourcePack-v10.0.zip" --repo JGlims/JGlimsPlugin --title "Resource Pack v10.0" --notes "SHA-1: $sha1"

# 5. Update docker-compose.yml on server
$sshKey = "C:\Users\jgmel\Documents\projects\server_minecraft\ssh-key-2026-02-25.key"
$newUrl = "https://github.com/JGlims/JGlimsPlugin/releases/download/v10.0/JGlimsResourcePack-v10.0.zip"
ssh -i $sshKey ubuntu@144.22.198.184 "cd /home/ubuntu/minecraft-server && sed -i 's|RESOURCE_PACK=.*|RESOURCE_PACK=$newUrl|' docker-compose.yml && sed -i 's|RESOURCE_PACK_SHA1=.*|RESOURCE_PACK_SHA1=$sha1|' docker-compose.yml && docker compose down && docker compose up -d"
C. COMPLETE CODEBASE (68 Java files, ~1.27 MB total)
(Same detailed file-by-file listing as v12.0/v13.0 — all packages, all files with sizes, all descriptions. Preserved in full.)

[The complete codebase section from v12.0 is retained here in its entirety — all 68 files across root, abyss, blessings, config, crafting, enchantments, events, guilds, legendary, menu, mobs, powerups, quests, structures, utility, and weapons packages.]

D–J. WEAPON TABLES, ARMOR SETS, POWER-UPS, INFINITY GAUNTLET, EVENTS, STRUCTURES, OTHER SYSTEMS
(All tables and descriptions identical to v12.0 — the complete 63-weapon table with all columns, 13 armor sets, 7 power-ups, Infinity Gauntlet mechanics, 6 events, 40 structures, and all other systems. All preserved in full.)

K. DEPLOYMENT PROCEDURES
K1. Build & Deploy Plugin
Copy$sshKey = "C:\Users\jgmel\Documents\projects\server_minecraft\ssh-key-2026-02-25.key"
$remoteHost = "ubuntu@144.22.198.184"

cd "C:\Users\jgmel\Documents\projects\JGlimsPlugin\JGlimsPlugin"
.\gradlew clean build

$jar = "build\libs\JGlimsPlugin-2.0.0.jar"
scp -i $sshKey $jar "${remoteHost}:/tmp/JGlimsPlugin.jar"
ssh -i $sshKey $remoteHost "docker cp /tmp/JGlimsPlugin.jar mc-crossplay:/data/plugins/JGlimsPlugin.jar && docker restart mc-crossplay"
K2. Deploy BetterModel Plugin
Copyscp -i $sshKey BetterModel-2.1.0.jar "${remoteHost}:/tmp/"
ssh -i $sshKey $remoteHost "docker cp /tmp/BetterModel-2.1.0.jar mc-crossplay:/data/plugins/ && docker restart mc-crossplay"
K3. Deploy a .bbmodel to BetterModel
Copyscp -i $sshKey Void_Dragon.bbmodel "${remoteHost}:/tmp/"
ssh -i $sshKey $remoteHost "docker exec mc-crossplay mkdir -p /data/plugins/BetterModel/models && docker cp /tmp/Void_Dragon.bbmodel mc-crossplay:/data/plugins/BetterModel/models/"
# Then in-game: /bettermodel reload
K4. Check Server Logs
Copyssh -i $sshKey $remoteHost "docker logs mc-crossplay --tail 200"
ssh -i $sshKey $remoteHost "docker logs mc-crossplay --tail 200 2>&1 | grep -i 'dragon\|bettermodel\|error'"
L. PROBLEMS SOLVED (Historical + This Conversation)
All previously solved: BOM build failure, particle throttle, missing imports, resource pack URL issues, Docker-compose overwriting, file ownership, sed escaping, Infinity Stone textures, Creation Splitter particles, phantom Ender Dragon boss bar.

Solved in this conversation:

Particle.DRAGON_BREATH crash: Paper 1.21.11+ requires Float data parameter. fix_dragon_model.ps1 rewrites AbyssDragonModel.java with safeParticle() helper.
BetterModel discovery: Identified as best free engine for animated Blockbench models.
Void_Dragon bone hierarchy mapped: body, bone24–32 (wings, head, jaw, tail).
Animation injection script designed: Python script generates idle/attack/death animations.
Item model reference chain analyzed: Found the path mismatch between items/abyss_dragon.json → item/abyss_dragon and the actual model at models/item/abyss/abyss_dragon.json.
build.gradle already has BetterModel dependency: Confirmed from repo — no changes needed.
M. MANDATORY DEVELOPMENT RULES
Always use [System.IO.File]::WriteAllText($path, $content, [System.Text.UTF8Encoding]::new($false)) to avoid BOM.
NEVER use PowerShell Compress-Archive for resource packs — use 7-Zip CLI.
Resource pack URL/SHA-1 changes go in docker-compose.yml, NOT server.properties.
After docker-compose changes: docker compose down && docker compose up -d.
SSH user is ubuntu, NOT opc.
Each resource pack update gets a new major version — never overwrite.
Use Adventure API exclusively.
All custom item metadata via PersistentDataContainer.
String-based CustomModelDataComponent for all item textures.
Two abilities per weapon: primary (right-click), alternate (crouch + right-click).
Particle budgets: COMMON 10, RARE 25, EPIC 50, MYTHIC 100, ABYSSAL 200.
All legendary items Unbreakable.
pack_format: 75.
File-size limit per source: ~120 KB; split if exceeding.
BetterModel .bbmodel files go in plugins/BetterModel/models/. After changes: /bettermodel reload.
BetterModel generates resource pack in BetterModel/build. Merge with JGlims pack before deployment.
Python scripts modifying .bbmodel must preserve all existing fields.
The build.gradle already includes BetterModel API dependency.
When creating item model chains: setItemModel() resolves to assets/minecraft/items/<key>.json. The model path inside must match an actual file in assets/minecraft/models/.
N. EXPANSION ROADMAP (Phases 20–35)
(Same as v12.0 — End Rift enhancements, Enhanced Pillager Warfare, Infinity Gauntlet + Thanos improvements, Quest expansions, Abyss Dimension full rework, Legendary Ranged Weapons, Ocean Expansion, World Bosses, Dungeon Generator, Legendary Shields, Pet System, Seasons & Weather, Fishing Overhaul, Guild Wars & Territories, Hardcore Challenge Modes.)

O. ABYSS DIMENSION — DETAILED TECHNICAL STATE
O1. World Generation
(Same as v12.0 — AbyssChunkGenerator.java details.)

O2. The Abyssal Citadel — Current State + Rework Plan
Current State: The citadel is generated by AbyssCitadelBuilder.java (54,253 bytes, the largest structure builder). It is built at (0, surfaceY, 0). STATUS: NEEDS FULL REWORK. Known issues include structural gaps, floating blocks, misaligned elements, potentially broken arena sealing, incorrect interior proportions, and performance impact during generation.

The instructions_citadel.txt File:

The repository contains instructions_citadel.txt at 233,787 bytes (~228 KB) — an extraordinarily detailed architectural specification for the citadel rebuild. This file is at: https://raw.githubusercontent.com/JGlims/JGlimsPlugin/main/instructions_citadel.txt

This file contains: precise block-by-block building instructions, dimensional specifications for every structural element (foundation, nave, transept, apse, facade, spires, interior, arena), exact block types and their coordinates, reference image descriptions for Gothic cathedral architecture, material palette definitions, flying buttress placement coordinates, window patterns, rose window spoke calculations, interior pillar positions, chapel dimensions, weapon chamber layouts, arena specifications, walkway coordinates, and the complete boss arena design.

ANY new chat session working on the citadel MUST read this file in full before making changes.

Schematic Images:

The project includes reference schematic images for the citadel design. These images show the architectural layout, cross-sections, elevation views, and structural details. They serve as visual guides alongside the text instructions.

AI-Generated Schematics Approach:

A viable alternative to programmatic generation is using AI tools to create Minecraft structure schematics (.nbt or .schem files). The workflow would be:

Use an AI image generator to create reference images of the desired Gothic cathedral from multiple angles.
Use tools like Amulet Editor (Python-based Minecraft world editor) or WorldEdit/FAWE to convert designs into schematics.
Build the citadel in a Creative world using the reference images, save with structure blocks or WorldEdit as .schem.
Load the schematic programmatically using Paper's Structure API or FAWE's API.
This produces a much more visually refined result than pure programmatic block placement.
Alternatively, if the programmatic approach is preferred, the rework should:

Use async chunk loading with getChunkAtAsync()
Break the build into phases with tick delays between each
Fix each section independently (foundation → nave → transept → apse → facade → spires → interior → arena)
Test after each section in-game
O3. Dragon Boss Fight
(Same as v12.0 — AbyssDragonBoss.java + AbyssDragonModel.java details.)

O4. Primary Bug: Dragon Not Appearing — Root Cause
Identified root cause: The item model reference chain has a path mismatch. The items/abyss_dragon.json file points to model minecraft:item/abyss_dragon, but the actual model JSON file exists at models/item/abyss/abyss_dragon.json (note the abyss/ subdirectory). The model resolution expects models/item/abyss_dragon.json (without the subdirectory), which doesn't exist. The paper.json has a second case "40000" that correctly points to item/abyss/abyss_dragon, but since setItemModel() takes priority over CMD, the wrong path is used.

Fix options: See Section B4. The recommended approach is migrating to BetterModel entirely.

P. BETTERMODEL INTEGRATION — COMPLETE TECHNICAL GUIDE
P1. What Is BetterModel
BetterModel (https://github.com/toxicity188/BetterModel, by toxicity188) is a free, open-source, server-side engine for Minecraft Java Edition. Version 2.1.0 (latest as of 2026-03-23). Key changelog highlights:

v2.1.0: Optimized runtime calculation, improved body rotator, Tracker#listenHitBox API, glow_ tag for light emission.
v2.0.0: Fabric platform port, keyframe improvements, breaking API changes (split into bettermodel-api, bettermodel-bukkit-api, etc.).
v1.15.x: Per-player animation, player model armor, IK rigs, mount controllers.
v1.14.x: IK rig support, rotation in global space, armor in player model.
v1.13.x: Cape support, strict loading config, BlockBench 5.0 position convert fix.
P2. BetterModel Architecture
Pipeline: raw .bbmodel → Blueprint (parsed model data) → Renderer (render pipeline) → Tracker (active entity instance).

P3. BetterModel Config
Copynamespace: bettermodel        # Resource pack namespace
pack-type: folder              # "folder" or "zip"
build-folder-location: BetterModel/build  # Where generated RP goes
# Additional config options exist for: 
# enable-strict-loading, disable-generating-legacy-models, etc.
P4. Setup Steps
Download BetterModel-2.1.0.jar from SpigotMC/Modrinth/Hangar
Place in plugins/ on server
Start server to generate config
Place .bbmodel in plugins/BetterModel/models/
/bettermodel reload
Merge generated RP with JGlims RP
Deploy merged RP
P5. API Usage
(Same detailed API code examples as v13.0.)

P6. The build.gradle Already Has the Dependency
CONFIRMED from repo. No changes needed:

CopycompileOnly 'io.github.toxicity188:bettermodel-bukkit-api:2.1.0'
Q. ANIMATION INJECTION — THE VOID_DRAGON .BBMODEL
Q1. The Problem
The .bbmodel has zero animations. BetterModel only plays existing animations.

Q2. Bone Hierarchy
body (root) → bone24 (L wing root) → bone25 (L wing tip)
            → bone26 (R wing root) → bone27 (R wing tip)
            → bone28 (head) → bone29 (jaw)
            → bone30 (tail base) → bone31 (tail mid) → bone32 (tail tip)
Q3. Python Script
(The complete inject_animations.py script from the conversation is preserved here — generates idle/attack/death animations with sinusoidal keyframes.)

Q4. Status
The script has been PROVIDED but NOT YET RUN. This is the immediate next step.

R. BLOCKBENCH PLUGINS — COMPLETE REFERENCE
R1. Critical Plugin: Code View
"Code View" by Wither — This is the plugin you saw. It shows the raw JSON of the model in real-time as you edit. Install via File → Plugins → search "Code View". Invaluable for debugging texture paths, UV mappings, element coordinates, and animation structures. Install this immediately.

R2. Full Plugin Reference
(Same comprehensive list as v13.0 — Animated Java, Optimize, Shape Generator, MTools, Plaster, GeckoLib, Resource Pack Utilities, Free Rotation, Texture Stitcher, Bakery, Animation Sliders, Structure Importer, Colour Gradient Generator, Asset Browser.)

R3. About Animated Java
Animated Java (v1.8.1, by Titus Evans/SnaveSutit) is an alternative to BetterModel for display-entity animation. It generates data packs + resource packs for Java Edition. It's designed for mapmakers and data pack developers. Animated Java is NOT needed for our use case (we use BetterModel for server-side control), but its Blueprint format and animation tools are excellent for prototyping complex animations in Blockbench.

S. CITADEL REWORK — DETAILED PLAN
(Same as v13.0 Section S, but with added detail about the schematic images and AI-schematic approach.)

Schematic Images Available: The project includes reference schematic images showing the citadel's architectural layout. These images depict floor plans, elevation views (front, side, back), cross-sections of the nave/transept, spire placement diagrams, arena layout, and approach path design. These should be used as primary references alongside instructions_citadel.txt.

AI-Schematic Generation Workflow:

Use reference images + instructions_citadel.txt to describe the structure to an AI assistant
Generate a Python script using the Amulet library (amulet-core + amulet-nbt) to create a .schem file
The script reads the architectural specifications and places blocks at exact coordinates
Load the schematic using WorldEdit/FAWE: //schem load citadel then //paste
This can be done iteratively — generate one section, paste it, evaluate in-game, adjust, repeat
Final .schem file can be embedded in the plugin JAR or loaded from the data folder
Alternative: Structure Block Approach:

Build the citadel manually in Creative Mode on a local test server
Use structure blocks to save sections (max 48×48×48 per structure block, but can chain multiple)
Save as .nbt files
Load programmatically using Paper's StructureManager API:
CopyStructureManager structureManager = Bukkit.getStructureManager();
Structure structure = structureManager.loadStructure(new File(plugin.getDataFolder(), "citadel_section_1.nbt"));
structure.place(location, true, StructureRotation.NONE, Mirror.NONE, 0, 1.0f, new Random());
T. SKETCHFAB MODELS — STRATEGY AND WORKFLOW
(Same comprehensive workflow as v13.0 — Steps 1-8 from download through Java integration, plus the priority list of models to implement.)

U. GITHUB REPOSITORY FILE STRUCTURE (COMPLETE)
JGlimsPlugin/
├── .gitignore (207 B)
├── JGlimsResourcePack-merged.zip (6,503,896 B / 6.5 MB)
├── JGlimsResourcePack-v9.0.zip (3,856,396 B / 3.86 MB)
├── README.md (64,661 B / 64 KB)
├── build.gradle (547 B) — includes BetterModel API dep
├── build.zip (2,811,260 B / 2.81 MB)
├── build_output.txt (8,216 B) — last build log (9 warnings, BUILD SUCCESSFUL)
├── fix_dragon_model.ps1 (21,005 B / 21 KB) — rewrites AbyssDragonModel.java v10.0
├── instructions_citadel.txt (233,787 B / 228 KB) — citadel architecture spec
├── gradle/wrapper/
│   ├── gradle-wrapper.jar (43,583 B)
│   └── gradle-wrapper.properties (250 B)
├── gradlew (3,682 B)
├── gradlew.bat (2,801 B)
├── merged_pack/
│   └── assets/
│       ├── bettermodel/textures/item/
│       │   ├── a.png (133,528 B) — dragon texture primary
│       │   ├── b.png (61,033 B) — dragon texture secondary
│       │   └── c.png (37,745 B) — dragon texture tertiary
│       └── minecraft/
│           ├── equipment/ (13 JSON files for armor sets)
│           │   ├── abyssal_plate.json (201 B)
│           │   ├── amethyst_armor.json (203 B)
│           │   ├── blood_moon.json (195 B)
│           │   ├── bone_armor.json (195 B)
│           │   ├── chainmail_reinforced.json (215 B)
│           │   ├── copper_armor.json (199 B)
│           │   ├── dragon_knight.json (201 B)
│           │   ├── frost_warden.json (199 B)
│           │   ├── natures_embrace.json (205 B)
│           │   ├── reinforced_leather.json (211 B)
│           │   ├── sculk_armor.json (197 B)
│           │   ├── shadow_stalker.json (203 B)
│           │   └── void_walker.json (197 B)
│           ├── items/ (30+ item definition JSONs)
│           │   ├── abyss_dragon.json (129 B) — standalone dragon item def
│           │   ├── paper.json (542 B) — with abyss_dragon CMD cases
│           │   ├── paper.json.bak (481 B) — backup version
│           │   ├── diamond_sword.json (5,847 B) — all sword weapons
│           │   ├── diamond_axe.json (1,467 B) — all axe weapons
│           │   ├── trident.json (679 B) — trident weapons
│           │   ├── netherite_*.json — awakened weapons + legendary armor
│           │   ├── blaze_powder.json, feather.json, etc. — power-ups
│           │   ├── *_dye.json — infinity stones
│           │   └── iron_*/leather_*/chainmail_*/diamond_*/golden_* — armor
│           ├── models/item/ (100+ model JSONs)
│           │   ├── _backup/ (5 vanilla model backups)
│           │   ├── abyss/ — dragon model subdirectory
│           │   └── (all weapon 3D model files: abominableblade.json 41KB, etc.)
│           └── textures/ (all texture PNGs)
└── src/main/java/com/jglims/plugin/ (68 Java source files in 12 packages)
V. GUIDE FOR A NEW CHAT SESSION
V1. Context Loading
Paste this entire summary (v14.0) as the first message. Then specify which task you're working on.

V2. Key Files the AI Should Read
Depending on the task:

Dragon fix: Read fix_dragon_model.ps1, merged_pack/assets/minecraft/items/paper.json, merged_pack/assets/minecraft/items/abyss_dragon.json
Citadel rework: Read instructions_citadel.txt (228 KB — the AI should use the crawler tool on https://raw.githubusercontent.com/JGlims/JGlimsPlugin/main/instructions_citadel.txt)
BetterModel integration: Read BetterModel wiki at https://github.com/toxicity188/BetterModel/wiki/API-example
Weapon/ability bugs: Read the specific Java source files from the repo
V3. Complete Step-by-Step Implementation Plan
PHASE 1: Fix the Dragon (Days 1-3)

1.1. Run python inject_animations.py Void_Dragon.bbmodel → produces Void_Dragon_animated.bbmodel 1.2. Open in Blockbench, verify 3 animations in Animate tab 1.3. Install BetterModel 2.1.0 on server 1.4. Deploy animated .bbmodel to plugins/BetterModel/models/ 1.5. Start server, let BetterModel generate RP 1.6. Merge BetterModel RP with JGlims RP 1.7. Deploy merged RP as v10.0 1.8. Create new AbyssDragonModel.java using BetterModel tracker API 1.9. Update AbyssDragonBoss.java to use new model 1.10. Build, deploy, test in-game 1.11. If BetterModel approach fails, fix the ItemDisplay path mismatch (see Section B4)

PHASE 2: Set Up Self-Hosted Resource Pack (Day 4)

2.1. Install nginx on Oracle Cloud 2.2. Configure to serve resource pack on port 8080 2.3. Open port 8080 in OCI security list + iptables 2.4. Test URL accessibility 2.5. Update docker-compose.yml to use self-hosted URL 2.6. This speeds up all future iterations

PHASE 3: Fix the Citadel (Days 5-10)

3.1. Read instructions_citadel.txt completely 3.2. Evaluate current citadel in-game 3.3. Decide: programmatic fix vs schematic-based rebuild 3.4. Execute the chosen approach 3.5. Test all elements: arena sealing, weapon chambers, walkway, spires, interior

PHASE 4: Fix Remaining Bugs (Days 11-13)

4.1. Fix Creation Splitter ability bug 4.2. Create Abyssal Key custom texture 4.3. Create animated textures for 3 awakened weapons

PHASE 5: Replace All Boss Models (Days 14-25)

5.1. Download Sketchfab models for each boss 5.2. Convert to Bedrock Entity format in Blockbench 5.3. Add animations (manually or via script) 5.4. Deploy to BetterModel, create Java wrappers 5.5. Refactor boss managers

PHASE 6: New Content (Days 26+)

6.1. World Weapons, Magic Items, Magical Mobs 6.2. Additional structures and dungeons 6.3. Future dimensions (Lunar, Aether)

W. CURRENT STATUS TABLE (2026-03-23)
System	Status	Blocking Issue
63 Legendary Weapons (textures)	COMPLETE	—
63 Legendary Weapons (abilities)	COMPLETE	Except Creation Splitter bug
13 Armor Sets (inventory + worn)	COMPLETE	—
7 Power-Up Items	COMPLETE	—
Infinity Gauntlet System	COMPLETE	—
Abyss Portal Activation	COMPLETE	—
Abyss Dimension (world gen)	COMPLETE	—
Abyssal Citadel	NEEDS FULL REWORK	Structural/visual issues
Abyss Dragon Model	NOT APPEARING	Item model path mismatch (Section B4)
Abyss Dragon Animations	NOT INJECTED	Python script not run yet
BetterModel Integration	PARTIALLY READY	build.gradle has dep; plugin not installed on server
Self-Hosted Resource Pack	NOT SET UP	nginx not installed yet
Abyss Dragon Boss Fight	FUNCTIONAL (no visual)	Model invisible
Creation Splitter Ability	BUG	Needs diagnosis
Abyssal Key Texture	MISSING	Uses vanilla Echo Shard
Awakened Weapon Animations	PARTIAL	3 of 4 missing
6 Server Events	COMPLETE	—
40 Structures	COMPLETE	—
6 Roaming Bosses	COMPLETE (basic)	Using vanilla entity models
64 Custom Enchantments	COMPLETE	—
Guild / Quest / Blood Moon / Guide	COMPLETE	—
Sketchfab Boss Models	NOT STARTED	After dragon works
World Weapons / Magic Items	NOT STARTED	Planned
Magical Mobs	NOT STARTED	Planned
Lunar / Aether Dimensions	NOT STARTED	Planned
X. OUTSTANDING WORK — PRIORITY ORDER
PRIORITY 0: CRITICAL — Dragon + BetterModel
0a. Run animation injection script on .bbmodel 0b. Install BetterModel 2.1.0 on server 0c. Deploy animated .bbmodel to BetterModel 0d. Merge + deploy resource pack v10.0 0e. Replace AbyssDragonModel.java with BetterModel wrapper 0f. Update AbyssDragonBoss.java 0g. Test: dragon must be VISIBLE with idle animation

PRIORITY 1: HIGH — Self-Hosted Resource Pack
1a. Install nginx on Oracle Cloud 1b. Configure port 8080 serving 1c. Open OCI security list + iptables 1d. Switch docker-compose to self-hosted URL

PRIORITY 2: HIGH — Citadel Rework
2a. Read instructions_citadel.txt + schematic images 2b. Choose approach (programmatic vs schematic) 2c. Execute rebuild 2d. Verify all elements

PRIORITY 3: HIGH — Bug Fixes
3a. Fix Creation Splitter ability 3b. Create Abyssal Key texture 3c. Create 3 awakened weapon animations

PRIORITY 4: MEDIUM — Replace Boss Models
4a-4f. All 6 roaming bosses → Sketchfab → BetterModel

PRIORITY 5: MEDIUM — Performance + New Content
5a. TPS optimization 5b. World Weapons, Magic Items, Magical Mobs

PRIORITY 6: LOW — Future Dimensions
Y. QUICK REFERENCE — ALL COMMANDS AND PATHS
Copy# === LOCAL DEVELOPMENT ===
$devPath = "C:\Users\jgmel\Documents\projects\JGlimsPlugin\JGlimsPlugin"
$rpWork  = "C:\Users\jgmel\Documents\projects\JGlimsPlugin\resourcepack-work\JGlimsResourcePack"
$sshKey  = "C:\Users\jgmel\Documents\projects\server_minecraft\ssh-key-2026-02-25.key"
$remote  = "ubuntu@144.22.198.184"

# Build plugin
cd $devPath; .\gradlew clean build

# Deploy plugin
scp -i $sshKey "$devPath\build\libs\JGlimsPlugin-2.0.0.jar" "${remote}:/tmp/JGlimsPlugin.jar"
ssh -i $sshKey $remote "docker cp /tmp/JGlimsPlugin.jar mc-crossplay:/data/plugins/ && docker restart mc-crossplay"

# Deploy BetterModel plugin
scp -i $sshKey BetterModel-2.1.0.jar "${remote}:/tmp/"
ssh -i $sshKey $remote "docker cp /tmp/BetterModel-2.1.0.jar mc-crossplay:/data/plugins/ && docker restart mc-crossplay"

# Deploy .bbmodel
scp -i $sshKey Void_Dragon.bbmodel "${remote}:/tmp/"
ssh -i $sshKey $remote "docker exec mc-crossplay mkdir -p /data/plugins/BetterModel/models && docker cp /tmp/Void_Dragon.bbmodel mc-crossplay:/data/plugins/BetterModel/models/"

# Resource pack (7-Zip)
& "C:\Program Files\7-Zip\7z.exe" a -tzip pack.zip .\merged_pack\*
$sha1 = (Get-FileHash pack.zip -Algorithm SHA1).Hash.ToLower()

# Upload to GitHub
gh release create v10.0 pack.zip --repo JGlims/JGlimsPlugin --title "RP v10.0" --notes "SHA-1: $sha1"

# Update server
ssh -i $sshKey $remote "cd /home/ubuntu/minecraft-server && sed -i 's|RESOURCE_PACK=.*|RESOURCE_PACK=NEW_URL|' docker-compose.yml && sed -i 's|RESOURCE_PACK_SHA1=.*|RESOURCE_PACK_SHA1=$sha1|' docker-compose.yml && docker compose down && docker compose up -d"

# Check logs
ssh -i $sshKey $remote "docker logs mc-crossplay --tail 100"
ssh -i $sshKey $remote "docker logs mc-crossplay 2>&1 | grep -i 'bettermodel\|dragon\|error'"

# === IN-GAME COMMANDS ===
/bettermodel reload          # Reload BetterModel models
/jglims legendary            # Give legendary weapon
/jglims armor                # Give armor set
/jglims menu                 # Open creative menu
/data get entity @e[type=item_display,limit=1]  # Debug display entities
Copy
END OF ULTIMATE DEFINITIVE PROJECT SUMMARY v14.0                                                                        REMEMBER THE PREVIOUS SUMMARY: JGLIMSPLUGIN — ULTIMATE DEFINITIVE PROJECT SUMMARY v13.0
Compiled: 2026-03-23 | Author: JGlims (jg.melo.lima2005@gmail.com) | Plugin Version: 2.0.0 | Target Version: 3.0.0

A. PROJECT METADATA
Repository: https://github.com/JGlims/JGlimsPlugin (public)

Plugin: JGlimsPlugin v2.0.0 — A comprehensive Minecraft server-side plugin for Paper 1.21+ featuring 63 legendary weapons with unique dual abilities, 13 custom armor sets, 7 power-up consumables, an Infinity Gauntlet system, 64 custom enchantments, 6 random server events, 6 roaming world bosses, 40 procedurally-placed structures, a complete quest system, guild system, and the custom Abyss dimension with a gothic cathedral citadel and multi-phase dragon boss fight. Written in Java, built with Gradle, targeting the PaperMC API.

Server Environment:

The server runs inside a Docker container named mc-crossplay on an Oracle Cloud ARM64 (Ampere) free-tier instance. The public IP is 144.22.198.184. Java Edition connects on port 25565; Bedrock Edition connects on port 19132 via GeyserMC crossplay bridge. The server software is Paper 1.21.11-127 running on Java 25 (OpenJDK 25.0.2+10-LTS, Eclipse Adoptium Temurin). Crossplay is provided by GeyserMC 2.9.4-SNAPSHOT and Floodgate 2.2.5-SNAPSHOT. Other plugins include SkinsRestorer 15.11.0, Chunky 1.4.40 for pre-generation, and BetterModel 2.1.0 (the new 3D model engine — installed or planned). The JVM is allocated 8 GB of RAM. Online mode is disabled to support crossplay. The default game mode is creative. Maximum players is 15. View distance is 10 chunks and simulation distance is 6 chunks. Difficulty is set per-world (Hard in the Abyss, configurable elsewhere).

SSH Access: Key file at C:\Users\jgmel\Documents\projects\server_minecraft\ssh-key-2026-02-25.key, user ubuntu@144.22.198.184 (confirmed NOT opc).

Local Development Path: C:\Users\jgmel\Documents\projects\JGlimsPlugin\JGlimsPlugin

Resource Pack Working Directory: C:\Users\jgmel\Documents\projects\JGlimsPlugin\resourcepack-work\JGlimsResourcePack

Server Resource Pack Source (original full pack): C:\Users\jgmel\Documents\projects\JGlimsPlugin\JGlimsResourcePack-v2.1-server

Docker Compose File (on server): /home/ubuntu/minecraft-server/docker-compose.yml

Data Volume Mount: /home/ubuntu/minecraft-server/data:/data (bind mount, NOT a Docker volume)

Build Output: JGlimsPlugin-2.0.0.jar (711,934 bytes). Built using .\gradlew clean build from the local dev path.

Build Configuration (build.gradle): Applies the java plugin, sets group com.jglims.plugin and version 2.0.0, uses Java toolchain version 21, pulls dependencies from Maven Central and the PaperMC Maven repository (https://repo.papermc.io/repository/maven-public/), adds a compile-only dependency on io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT, and configures the jar archive base name to JGlimsPlugin. NOTE: To integrate BetterModel API, add compileOnly("io.github.toxicity188:bettermodel-bukkit-api:VERSION") and the Maven Central repository.

Plugin Descriptor (plugin.yml): Name JGlimsPlugin, version 2.0.0, main class com.jglims.plugin.JGlimsPlugin, api-version 1.21, author JGlims. Defines three commands: /jglims (admin command with subcommands: reload, stats, enchants, sort, mastery, legendary, armor, powerup, bosstitles, gauntlet, menu, guia, help — requires jglims.admin permission, defaults to ops), /guild (subcommands: create, invite, join, leave, kick, disband, info, list), and /guia (alias: /guide, gives Portuguese guide books). To be added: depend: [BetterModel] or softdepend: [BetterModel] if integrating.

Sketchfab Profile: https://sketchfab.com/JGlims/collections — Contains curated collections of downloadable 3D models for use as custom mobs, bosses, structures, and decorative elements. Models with existing animations are prioritized for BetterModel integration. The Sketchfab page renders dynamically (JavaScript-only), so the exact model list must be browsed in a browser. Known collections include creature models (dragons, demons, undead, etc.) and structure assets. All Sketchfab models used must respect their individual licenses (CC-BY, CC0, etc.) and be credited appropriately.

B. CURRENT RESOURCE PACK STATE
What Is Currently Live
The server is running resource pack v9.0, hosted on GitHub Releases. This is the latest pack after multiple iterations of fixes.

Current docker-compose.yml settings:

RESOURCE_PACK: https://github.com/JGlims/JGlimsPlugin/releases/download/v9.0/JGlimsResourcePack-v9.0.zip
RESOURCE_PACK_SHA1: 0e6151b68727aff120a9648c8f295078ccfee809
RESOURCE_PACK_ENFORCE: TRUE
CRITICAL RULE: The resource pack URL and SHA-1 are set in docker-compose.yml as environment variables. The Docker image (itzg/minecraft-server) overwrites server.properties from these env vars on EVERY container start. Editing server.properties directly is useless — you MUST edit docker-compose.yml and run docker compose down && docker compose up -d for changes to persist.

BetterModel Resource Pack Interaction
BetterModel auto-generates its own resource pack from .bbmodel files placed in plugins/BetterModel/models/. This generated pack must be merged with the existing JGlims resource pack before deployment. The merged resource pack is stored in the repository as JGlimsResourcePack-merged.zip (6.5 MB). The merged_pack/ folder in the repository contains the working directory for this merged pack, including both the standard JGlims assets and the BetterModel-generated assets. Specifically, merged_pack/assets/bettermodel/textures/item/ contains the texture PNG files that BetterModel models reference (currently: a.png at 133 KB, b.png at 61 KB, c.png at 38 KB — these are the dragon/mob textures extracted from the .bbmodel files by BetterModel).

Texture Hosting Discovery (This Conversation)
During this conversation, we established that the texture files for BetterModel models are hosted via the resource pack. The texture path inside a .bbmodel file (the "textures" array) determines where BetterModel looks for the PNG. When BetterModel processes a .bbmodel, it generates resource pack entries under assets/bettermodel/textures/item/ by default. The merged resource pack must include these files. If a texture is referenced as a path like 0.png or a.png inside the .bbmodel, BetterModel will generate the corresponding path in the resource pack. The textures are NOT served from localhost or a web server — they are packed into the Minecraft resource pack ZIP and sent to clients via the standard server.properties resource-pack mechanism.

What Works Right Now (Confirmed as of 2026-03-23)
All 63 legendary weapon textures and 3D models work. All 63 legendary weapon abilities work. All 13 armor set textures (both inventory icons AND worn textures) work. All 7 power-up item textures work. Infinity Gauntlet and Thanos Glove textures work. Infinity Stone textures work. Abyss portal activation works. Plugin loads cleanly. The basic 11-element blocky dragon model exists but is visually poor.

What Does NOT Work / Needs Rework
The Abyss Dragon does NOT appear when the boss fight command is triggered — this is the primary active bug. The Abyss Dragon model quality is poor (basic 11-cuboid blocky shape). The Abyssal Citadel needs a full rework. One Creation Splitter ability has a bug. Missing animated textures for 3 awakened weapons. The Abyssal Key uses a vanilla Echo Shard texture. BetterModel integration is not yet complete (the .bbmodel model + animation injection has not been executed yet).

Resource Pack Version History
Version	Size	SHA-1	Description
v4.0.0	3.04 MB	63b8e30d...	pack_format 75, initial 63 weapons
v4.0.2	3.09 MB	626edf29...	Real textures, 3D models
v4.0.3	3.17 MB	6cd61288...	All 15 missing 3D weapon models
v4.0.4	3.19 MB	ca60f59f...	Fixed 5 remaining buggy weapons
v4.0.5	3.19 MB	2147a804...	Fixed excalibur + requiem awakened
v4.0.6	3.19 MB	7d03a108...	Added animation mcmeta for excalibur
v4.0.7	3.17 MB	c433786c...	Fix Excalibur 3D models
v4.0.8	3.17 MB	45aeea8a...	Excalibur Awakened animated fire texture
v4.0.9	3.17 MB	1b9f9e6c...	Excalibur Awakened majestica model
v4.1.0	3.52 MB	a7a0e72c...	Armor, infinity, powerup textures
v5.0	3.52 MB	dc5cdce0...	Repackaged for new release URL
v6.0	—	26704612...	Intermediate update
v7.0	—	a7212467...	Fixed Infinity Stone textures
v8.0	—	—	Abyss Dragon model added
v9.0	~3.68 MB	0e6151b6...	CURRENT — Dragon visual fix
The broken "v4.1.1" file (9.5 MB, created by PowerShell's Compress-Archive) must NEVER be used.

How Minecraft 1.21.4+ Custom Textures Work
Minecraft 1.21.4+ uses item model definitions in assets/minecraft/items/*.json. Each file uses type: "minecraft:select" with property: "minecraft:custom_model_data" to choose which model to render based on the custom_model_data component's strings list. The Java plugin sets the string via CustomModelDataComponent.setStrings(List.of("texture_name")). The "when" value in the JSON must exactly match the string set by the plugin. For worn armor textures in 1.21.4+, the equippable component must set an asset_id that points to an assets/minecraft/equipment/<name>.json file.

How to Update the Resource Pack
Make changes in the resource-pack working directory.
DO NOT use PowerShell Compress-Archive — use 7-Zip CLI.
Get SHA-1: (Get-FileHash -Path $zipPath -Algorithm SHA1).Hash.ToLower()
Upload as NEW release version: gh release create v10.0 $zipPath --repo JGlims/JGlimsPlugin --title "Resource Pack v10.0" --notes "SHA-1: $sha1"
Update docker-compose.yml with new URL + SHA-1.
docker compose down && docker compose up -d
C. COMPLETE CODEBASE (68 Java files, ~1.27 MB total)
Root Package — com.jglims.plugin
JGlimsPlugin.java (34,896 B) — Main plugin class. onEnable() initializes all managers and listeners, registers all three commands, creates the Abyss dimension, and logs startup.

abyss Package (5 files)
AbyssChunkGenerator.java (7,341 B) — Custom ChunkGenerator producing void-based terrain for world_abyss. ~210-block-radius landmass with layered sinusoidal noise. Surface materials: deepslate tiles (inner), polished blackstone (mid), purpur/end stone (outer). Decorations: amethyst columns, obsidian spikes, end rods, crying obsidian, soul campfires. Floating islands beyond radius 160. Void rivers between 130–200.

AbyssCitadelBuilder.java (54,253 B) — The largest structure builder. Gothic cathedral with elliptical foundation (110×140), circular courtyard, main nave (80×60×50), transept (±50 blocks), apse (radius 25), front facade (120 wide × 80 tall), rose window, center mega-spire (180 blocks), 6 flanker spires, 15 minor spires, flying buttresses, gothic windows, interior chapels, 4 weapon chambers, walkway to boss arena (radius 40, bedrock floor, barrier walls, 8 pillars). STATUS: NEEDS FULL REWORK — the generated structure has issues (see Section S).

AbyssDimensionManager.java (18,501 B) — Creates/manages world_abyss. Disables vanilla DragonBattle every 10 seconds. Triggers citadel build. Handles portal interaction (4×5 purpur frame + Abyssal Key). Arena center: z=-120.

AbyssDragonBoss.java (23,877 B) — Boss fight controller. 1500 HP, 25% damage reduction, 4 phases, 11 waypoints, combat loop (every 40 ticks), movement loop (every 10 ticks). Attacks escalate per phase. Death drops ABYSSAL weapon + major loot. STATUS: The dragon model does not appear when fight is triggered — primary active bug.

AbyssDragonModel.java (17,894 B) — Current visual model using Zombie (hitbox) + ItemDisplay (visual). Uses CMD string "abyss_dragon" / integer 40000. Scale 5.0 (6.25 enraged). Contains safeParticle() helper that handles Particle.DRAGON_BREATH requiring Float data in Paper 1.21.11+. The fix_dragon_model.ps1 script (21 KB, in repo root) rewrites this file with v10.0 fixes including replacing all DRAGON_BREATH with SOUL_FIRE_FLAME and wrapping all particle calls in try-catch. STATUS: Even with the particle fix, the dragon model does NOT visually appear. The underlying problem is likely the item model reference chain (resource pack JSON → model JSON → texture) being broken, or the ItemDisplay not rendering the custom model correctly.

blessings Package (2 files)
BlessingListener.java (3,452 B) + BlessingManager.java (10,296 B) — Three blessing types (heal, damage, defense), max 10 uses each.

config Package (1 file)
ConfigManager.java (29,211 B) — All config.yml values with getters.

crafting Package (2 files)
RecipeManager.java (34,774 B) — All custom recipes. VanillaRecipeRemover.java (588 B) — Removes conflicts.

enchantments Package (5 files)
AnvilRecipeListener.java (33,648 B) — Custom anvil UI. CustomEnchantManager.java (8,853 B) — 64 enchantment definitions. EnchantmentEffectListener.java (69,495 B) — All 64 effects (largest file). EnchantmentType.java (1,942 B) — Type enum. SoulboundListener.java (7,961 B) — Keep items on death.

events Package (7 files)
EventManager.java (10,367 B) + 6 event files implementing Nether Storm, Piglin Uprising, Void Collapse, Pillager War Party, Pillager Siege, and End Rift events.

guilds Package (2 files)
GuildListener.java (1,456 B) + GuildManager.java (13,679 B) — Full CRUD, invitations, friendly fire.

legendary Package (15 files)
LegendaryTier.java (3,673 B), LegendaryWeapon.java (15,034 B) — 63-weapon enum, LegendaryWeaponManager.java (7,937 B), LegendaryAbilityListener.java (18,032 B), LegendaryPrimaryAbilities.java (69,951 B), LegendaryAltAbilities.java (73,055 B), LegendaryAbilityContext.java (4,941 B), LegendaryArmorSet.java (16,160 B) — 13 armor sets, LegendaryArmorManager.java (16,059 B), LegendaryArmorListener.java (38,729 B), LegendaryLootListener.java (22,565 B), InfinityStoneManager.java (8,029 B), InfinityGauntletManager.java (23,370 B).

menu Package (2 files)
CreativeMenuManager.java (45,503 B) — GUI menus. GuideBookManager.java (29,396 B) — PT-BR guide books.

mobs Package (7 files)
BiomeMultipliers.java (3,080 B), BloodMoonManager.java (12,329 B), BossEnhancer.java (19,998 B), BossMasteryManager.java (12,294 B), KingMobManager.java (5,972 B), MobDifficultyManager.java (4,744 B), RoamingBossManager.java (57,427 B) — 6 roaming world bosses.

powerups Package (2 files)
PowerUpManager.java (26,207 B) — 7 items. PowerUpListener.java (5,607 B).

quests Package (3 files)
QuestManager.java (27,940 B), QuestProgressListener.java (3,231 B), NpcWizardManager.java (15,501 B).

structures Package (5 files)
StructureType.java (10,780 B) — 40 types. StructureManager.java (49,819 B), StructureBuilder.java (8,714 B), StructureLootPopulator.java (8,554 B), StructureBossManager.java (14,554 B).

utility Package (7 files)
BestBuddiesListener.java (11,609 B), DropRateListener.java (3,289 B), EnchantTransferListener.java (7,135 B), InventorySortListener.java (5,282 B), LootBoosterListener.java (8,991 B), PaleGardenFogTask.java (1,356 B), VillagerTradeListener.java (4,244 B).

weapons Package (13 files)
11 weapon type managers + WeaponAbilityListener.java (81,738 B) + WeaponMasteryManager.java (9,803 B).

D. LEGENDARY WEAPONS — COMPLETE TABLE (63 weapons)
(Same table as v12.0 — all 20 COMMON, 8 RARE, 7 EPIC, 24 MYTHIC, 4 ABYSSAL weapons with IDs, display names, materials, damage, CMD, texture names, abilities, and cooldowns. See previous summary for full table.)

E. ARMOR SETS — COMPLETE TABLE (13 sets)
(Same table as v12.0 — 6 craftable, 7 legendary sets with CMD ranges 30101–30254. See previous summary for full table.)

F. POWER-UP ITEMS, G. INFINITY GAUNTLET, H. EVENTS, I. STRUCTURES, J. OTHER SYSTEMS
(All identical to v12.0 — see previous summary for full details.)

K. DEPLOYMENT PROCEDURES
(Same as v12.0 — build + deploy plugin via SCP/Docker, deploy resource pack via 7-Zip/GitHub Releases/docker-compose.)

L. PROBLEMS SOLVED (Historical)
All previously solved problems from v12.0 remain documented: BOM build failure, particle throttle, missing imports, resource pack URL issues, Docker-compose overwriting, file ownership, sed escaping, Infinity Stone textures, Creation Splitter particles, phantom Ender Dragon boss bar. Additionally solved in this conversation session:

Particle.DRAGON_BREATH crash — Paper 1.21.11+ changed Particle.DRAGON_BREATH to require Float data parameter. The fix_dragon_model.ps1 script (committed to repo root) rewrites AbyssDragonModel.java v10.0 with a safeParticle() helper that detects data type requirements and uses SOUL_FIRE_FLAME as a safe replacement. All particle calls are now wrapped in try-catch blocks.

BetterModel discovery and API analysis — We identified BetterModel (by toxicity188) as the best free, open-source server-side model engine for displaying animated Blockbench models in Minecraft Java Edition. We analyzed its complete API: Blueprint → Renderer → Tracker pipeline, BukkitAdapter.adapt(), BetterModel.model("name").map(r -> r.getOrCreate(entity)), animation playback via tracker.animate("name", AnimationModifier), and cleanup via tracker.close().

Void_Dragon .bbmodel analysis — The user provided the complete abyss_dragon.bbmodel (Blockbench format version 5.0, Bedrock model format, 1024×1024 resolution). We identified the outliner bone hierarchy: body (root), bone24-bone27 (wings), bone28 (head), bone29 (jaw), bone30 (tail base), bone31 (tail mid), bone32 (tail tip). This model has NO animations — they must be injected.

M. MANDATORY DEVELOPMENT RULES
Always use [System.IO.File]::WriteAllText($path, $content, [System.Text.UTF8Encoding]::new($false)) to avoid BOM.
NEVER use PowerShell Compress-Archive for resource packs — use 7-Zip CLI.
Resource pack URL/SHA-1 changes go in docker-compose.yml, NOT server.properties.
After docker-compose changes: docker compose down && docker compose up -d.
SSH user is ubuntu, NOT opc.
Each resource pack update gets a new major version — never overwrite.
Use Adventure API exclusively (no deprecated ChatColor).
All custom item metadata via PersistentDataContainer.
String-based CustomModelDataComponent for all item textures.
Two abilities per weapon: primary (right-click), alternate (crouch + right-click).
Particle budgets per tier: COMMON 10, RARE 25, EPIC 50, MYTHIC 100, ABYSSAL 200.
All legendary items Unbreakable.
Resource pack pack_format: 75.
File-size limit per source file: ~120 KB; split if exceeding.
NEW: When integrating BetterModel, the .bbmodel files go into plugins/BetterModel/models/ on the server. After changes: /bettermodel reload.
NEW: BetterModel generates a resource pack in its build-folder-location (default BetterModel/build). This must be merged with the JGlims resource pack before deployment.
NEW: When writing Python scripts that modify .bbmodel JSON, preserve all existing fields and only add/replace the "animations" array.
N. FULL EXPANSION ROADMAP
(Same as v12.0 — Phases 20-35 covering End Rift enhancements through Guild Wars & Territories.)

O. ABYSS DIMENSION — DETAILED TECHNICAL STATE
World Generation (AbyssChunkGenerator.java)
(Same as v12.0.)

The Abyssal Citadel (AbyssCitadelBuilder.java)
(Same structural description as v12.0, but with updated status — see Section S.)

Dragon Boss Fight (AbyssDragonBoss.java + AbyssDragonModel.java)
(Same as v12.0.)

Current Primary Bug: Dragon Not Appearing
When the player executes the boss fight trigger command, the fight sequence initializes (boss bar appears, sounds play, lightning strikes), but NO visual dragon model renders. The invisible Zombie hitbox entity likely spawns (the fight "works" mechanically), but the ItemDisplay entity either fails to spawn, fails to render the custom model, or renders invisibly. Root cause analysis:

Most likely: Item model reference chain is broken. The assets/minecraft/items/paper.json (or abyss_dragon.json) in the resource pack must contain a "when": "abyss_dragon" case pointing to the model at item/abyss/abyss_dragon. If this chain is broken, the ItemDisplay renders as an invisible/default paper item. The repo contains merged_pack/assets/minecraft/items/abyss_dragon.json (129 bytes) which appears to be a standalone items file rather than integrated into paper.json. This is likely the issue — the code creates a PAPER ItemStack with CMD string "abyss_dragon", but the item definition might be in the wrong file or format.

Possible: setItemModel() API mismatch. The code calls meta.setItemModel(NamespacedKey.fromString("minecraft:abyss_dragon")) which requires the item definitions file at assets/minecraft/items/abyss_dragon.json to exist and be valid. If this file's structure doesn't match what Paper 1.21.11 expects, the model won't render.

Possible: Texture file missing or mismatched. The model JSON references a texture that may not exist at the specified path in the deployed resource pack.

Possible: The dragon spawns below/inside the arena floor. The spawn location Y coordinate might place the display entity inside blocks.

P. BETTERMODEL INTEGRATION — COMPLETE TECHNICAL GUIDE
What Is BetterModel
BetterModel (https://github.com/toxicity188/BetterModel, by toxicity188) is a free, open-source, server-side engine that renders and animates Blockbench .bbmodel models in Minecraft Java Edition using item display entity packets. It runs on Paper, Purpur, Spigot, Folia, Leaf, Canvas, and Fabric. Current version: 2.1.0. It requires Minecraft 1.20.4+ (display entity support).

Key features: imports generic Blockbench .bbmodel files directly, auto-generates a resource pack from the models, plays animations defined in the .bbmodel, syncs model position with a base entity, provides custom hitboxes, supports per-player animation, and includes MythicMobs integration.

BetterModel Architecture
The pipeline is: raw .bbmodel file → Blueprint (parsed model data) → Renderer (render pipeline, manages display entities) → Tracker (active instance attached to an entity or location).

BetterModel Setup on the Server
Download BetterModel-2.1.0.jar from SpigotMC, Modrinth, or Hangar.
Place it in plugins/ on the server.
Start the server once to generate the config.
Place .bbmodel files into plugins/BetterModel/models/.
Run /bettermodel reload to process the models.
BetterModel auto-generates a resource pack in plugins/BetterModel/build/ (configurable via build-folder-location in config).
CRITICAL: This generated resource pack must be merged with the existing JGlims resource pack. Copy the assets/bettermodel/ folder from the BetterModel build output into the JGlims resource pack, and ensure both packs' assets/minecraft/ folders are properly merged (no conflicts).
Deploy the merged pack as a new version (v10.0, v11.0, etc.).
BetterModel API Integration (Java)
Add dependency to build.gradle:

Copyrepositories {
    mavenCentral()
}
dependencies {
    compileOnly 'io.github.toxicity188:bettermodel-bukkit-api:2.1.0'
}
Core API usage:

Copyimport kr.toxicity.model.api.BetterModel;
import kr.toxicity.model.api.tracker.EntityTracker;
import kr.toxicity.model.api.data.renderer.AnimationModifier;
import kr.toxicity.model.api.util.AnimationIterator;

// Attach model to entity
EntityTracker tracker = BetterModel.inst()
    .model("Void_Dragon")  // matches filename without .bbmodel
    .map(renderer -> renderer.getOrCreate(
        kr.toxicity.model.api.bukkit.BukkitAdapter.adapt(zombieEntity)
    ))
    .orElse(null);

// Play animation
tracker.animate("idle", AnimationModifier.builder()
    .type(AnimationIterator.Type.LOOP).build());
tracker.animate("attack", AnimationModifier.DEFAULT_WITH_PLAY_ONCE);
tracker.animate("death", AnimationModifier.DEFAULT_WITH_PLAY_ONCE);

// Cleanup
tracker.close();
BetterModel vs Current Approach
The current AbyssDragonModel.java uses a manual Zombie + ItemDisplay approach with a static item model rendered on a single ItemDisplay entity. BetterModel replaces this entirely — it manages all the display entities automatically, supports proper bone-based animation, and handles the resource pack generation. The migration path is: (1) get the .bbmodel working with animations in Blockbench, (2) place it in BetterModel's models folder, (3) replace AbyssDragonModel.java with a BetterModel tracker wrapper, (4) merge the resource packs.

Q. ANIMATION INJECTION — THE VOID_DRAGON .BBMODEL
The Problem
The Void_Dragon.bbmodel (downloaded from Sketchfab, ArtsByKev model) is a high-quality Bedrock-format model with many cube elements and a proper bone hierarchy, but it contains ZERO animations. BetterModel only plays animations that already exist within the .bbmodel file — it does not generate them. We need to inject animations into the JSON.

Bone Hierarchy (from outliner analysis)
body (UUID: 7cb9de29-579b-e008-6c55-3fba1a6c6d98) — root body bone
├── bone24 — left wing root
│   └── bone25 — left wing tip / membrane
├── bone26 — right wing root
│   └── bone27 — right wing tip / membrane
├── bone28 — head group
│   └── bone29 — jaw
├── bone30 — tail base
│   └── bone31 — tail mid
│       └── bone32 — tail tip
Python Animation Injector Script (inject_animations.py)
A complete Python script was provided in this conversation that reads the .bbmodel, extracts bone UUIDs from the outliner, generates three animations (idle/loop: wing flap ±15°, body bob ±1.5 units, tail sway, head drift; attack/once: head lunge, jaw open 40°, wings spread 35°; death/once: body collapse -12 units, wings fold, head droop 50°, jaw open), and writes the modified JSON as Void_Dragon_animated.bbmodel. The script uses sinusoidal keyframes at 0.25s intervals for idle, and hand-crafted keyframes for attack/death.

STATUS: The script was provided but the user has NOT yet run it. This is the immediate next step.

Expected Workflow After Running the Script
Run: python inject_animations.py Void_Dragon.bbmodel
Open Void_Dragon_animated.bbmodel in Blockbench
Switch to Animate tab — verify 3 animations appear (idle, attack, death)
Preview each animation, tweak amplitudes if needed
Copy the animated .bbmodel to plugins/BetterModel/models/Void_Dragon.bbmodel on the server
Run /bettermodel reload
Merge the generated resource pack with JGlims pack
Deploy as new resource pack version
Update Java code to use BetterModel tracker API
Java Integration Class (VoidDragonModel.java)
A complete Java class was provided in this conversation that wraps the BetterModel tracker API with attach(LivingEntity), playIdle(), playAttack(), playDeath(), and remove() methods.

R. BLOCKBENCH PLUGINS — COMPLETE REFERENCE
Essential Plugins for This Project
Based on thorough research of the Blockbench plugin registry (https://github.com/JannisX11/blockbench-plugins/blob/master/plugins.json), here are the most relevant plugins:

Code View (by Wither) — THIS IS THE PLUGIN YOU SAW. It displays the raw JSON code of the model you're currently editing in real-time. Install via Blockbench → File → Plugins → search "Code View". This lets you see exactly what the .bbmodel exports to, which is invaluable for debugging resource pack issues (checking texture paths, element coordinates, UV mappings, etc.).

Animated Java (by Titus Evans / SnaveSutit, v1.8.1) — A cutting-edge plugin for creating complex animations using display entities in Minecraft Java Edition. It generates a data pack and resource pack that can summon and animate multi-part display entity rigs. This is an alternative to BetterModel for purely data-pack-driven custom entities (no server plugin required). However, BetterModel is more appropriate for our use case because we need server-side control for boss fight mechanics. Still, Animated Java's Blueprint format and animation tools are excellent for prototyping.

Optimize (by Krozi) — Hides concealed faces for better performance. Useful for reducing element count in complex dragon models before export.

Shape Generator (by dragonmaster95) — Generates geometric shapes (spheres, cylinders, cones) that can be useful for creating custom mob body parts.

MTools (Mesh Tools) (by Malik12tree) — Powerful mesh modeling tools, operators, and generators. Useful for Generic Model format editing (meshes, booleans, etc.).

Plaster (by JannisX11) — Fixes texture bleeding (small colored lines at edges) by slightly shrinking UV maps. Run this before exporting any model.

GeckoLib Models & Animations (by Eliot Lash, Tslat, Gecko, McHorse, v4.2.4) — For creating animated entities using the GeckoLib Java mod library. Not directly needed (we use BetterModel, not GeckoLib), but useful as a reference for Bedrock animation format understanding.

Resource Pack Utilities (by Ewan Howell) — A collection of utilities for resource pack creation. Can help verify pack structure, check for missing textures, and validate JSON.

Free Rotation (by Godlander & Ewan Howell) — Creates Java Item models without rotation limitations. Useful if we ever need to export models in the Java Block/Item format with unrestricted rotations.

Texture Stitcher (by McHorse) — Stitches multiple textures into a single texture atlas. Useful if we need to combine textures from multiple model parts into one sheet.

Bakery (by JannisX11) — Bakes complex animations into simple linear keyframes. Useful for simplifying animations before export.

Animation Sliders (by JannisX11) — Adds sliders to tweak keyframe values. Makes animation editing more intuitive.

Structure Importer (by JannisX11 & Krozi) — Imports Minecraft structure block .nbt files. Could be useful for importing citadel structures from in-game designs.

Colour Gradient Generator (by Ewan Howell) — Generates hue-shifted gradient palettes. Useful for creating Abyss-themed color palettes (dark purples, void blacks, soul-fire blues).

Asset Browser (by Ewan Howell) — Browse Minecraft's built-in assets from within Blockbench. Useful for referencing vanilla models and textures.

Recommended Plugin Installation Order
Code View — install immediately, use for all debugging
Optimize — run on every complex model before export
Plaster — run on every model to fix texture bleeding
Animation Sliders — use when tweaking injected animations
Resource Pack Utilities — use when validating pack structure
S. ABYSSAL CITADEL — FULL REWORK PLAN
Current Problems
The existing AbyssCitadelBuilder.java (54 KB) generates the citadel programmatically, but:

The structure has visual issues (gaps, floating blocks, misaligned elements)
Performance impact during generation is significant (54 KB of block placement logic running synchronously)
The gothic details (buttresses, windows, rose window) may not render as intended
The boss arena may not be properly sealed
The walkway from apse to arena may have structural gaps
Interior proportions may be wrong
The instructions_citadel.txt File
The repository contains instructions_citadel.txt (233,787 bytes / ~228 KB) — an extremely detailed architectural specification for the citadel. This file contains block-by-block building instructions, reference images descriptions, dimensional specifications, material palettes, and architectural guidelines. Any new chat session must read this file thoroughly before attempting to modify AbyssCitadelBuilder.java. The file is at: https://raw.githubusercontent.com/JGlims/JGlimsPlugin/main/instructions_citadel.txt

Rework Approach
The citadel rework should be done in phases: (1) Read and understand instructions_citadel.txt completely, (2) Compare the instructions against the current code, (3) Fix structural issues one section at a time (foundation → nave → transept → apse → facade → spires → interior → arena), (4) Test each section in-game before proceeding, (5) Optimize by using async block placement via Bukkit.getScheduler().runTaskAsynchronously() with chunk snapshot checks.

Alternative: Schematic-Based Approach
If programmatic generation proves too difficult to get right, consider building the citadel manually in Creative Mode (or in Blockbench using the Structure Importer plugin), saving it as an .nbt schematic file, and loading it programmatically using Paper's Structure API or a library like FAWE (FastAsyncWorldEdit). This would produce a much more visually refined result.

T. SKETCHFAB MODELS — STRATEGY AND WORKFLOW
Model Sources
The user's Sketchfab collections at https://sketchfab.com/JGlims/collections contain curated 3D models. Models with existing animations are highest priority because BetterModel can play them directly without animation injection.

Complete Workflow: Sketchfab Model → BetterModel In-Game Entity
Step 1: Download the model. Go to the Sketchfab model page, click "Download 3D Model", select the .bbmodel format if available, otherwise select .glb/.gltf/.fbx.

Step 2: Import into Blockbench. Open Blockbench. If the file is .bbmodel, open it directly. If it's .glb/.gltf/.fbx, use File → Import → select the format. If it's a Generic Model, you may need to convert it.

Step 3: Convert to Bedrock Entity format. BetterModel requires the Bedrock Entity model format. In Blockbench: File → Convert Project → Bedrock Entity. This ensures the model has the correct model_format: "bedrock" and an outliner with named bones (not just element UUIDs).

Step 4: Check and fix the model.

Install the Code View plugin to see the raw JSON
Install Optimize to hide concealed faces
Ensure all bone groups have meaningful names (body, head, wing_left, wing_right, tail, etc.)
Check the texture resolution (should be power-of-two: 64×64, 128×128, 256×256, 512×512, or 1024×1024)
Ensure the texture is embedded in the .bbmodel (check the "textures" array in the JSON — it should have a "source" field with base64-encoded PNG data)
Step 5: Verify/add animations.

Switch to the Animate tab in Blockbench
If animations exist, play them to verify they look correct
If NO animations exist (common for Sketchfab static models), either:
(A) Create them manually in Blockbench's Animate tab (recommended if you're comfortable with the UI)
(B) Use the Python inject_animations.py script approach (modify bone names in the script to match the model's actual bone names)
Minimum animations needed: idle (loop), attack (play-once), death (play-once)
Optional: walk, run, special_attack, spawn, roar
Step 6: Retexture if needed. For Abyss-themed mobs, use the following color palette: dark purples (#2d002d, #4a0080), deep blacks (#0d0d0d), glowing purple accents (#aa00ff, #7700cc), soul-fire blue (#00bbff), crying-obsidian magenta (#cc00cc). Use Blockbench's Paint tab.

Step 7: Save and deploy.

Save as <model_name>.bbmodel
Copy to plugins/BetterModel/models/ on the server
Run /bettermodel reload
Merge the BetterModel-generated resource pack with the JGlims pack
Deploy as new resource pack version
Step 8: Java code integration.

Copy// For any boss/mob
EntityTracker tracker = BetterModel.inst()
    .model("model_name")  // filename without .bbmodel
    .map(r -> r.getOrCreate(BukkitAdapter.adapt(baseEntity)))
    .orElse(null);
Models to Implement (Priority Order)
Void_Dragon (Abyss Dragon) — The immediate priority. abyss_dragon.bbmodel / Void_Dragon.bbmodel
All 6 Roaming World Bosses — Replace the current vanilla-entity-based visuals with Sketchfab models:
The Watcher (Deep Dark Warden replacement)
Hellfire Drake (Nether Ghast replacement)
Frostbound Colossus (Iron Golem replacement)
Jungle Predator (Ravager replacement)
End Wraith (Phantom replacement)
Abyssal Leviathan (custom Abyss boss)
Event Bosses — Infernal Overlord, Piglin Emperor, Void Leviathan, End Rift Dragon, Fortress Warlord
Structure Mini-Bosses — All structure-specific bosses
Ambient mobs — Void Wanderer (Enderman replacement), Abyssal Sentinel (Wither Skeleton replacement)
Future: Magical Mobs — Entirely new creature types from Sketchfab collections
U. THE DRAGON NOT APPEARING — ROOT CAUSE ANALYSIS AND FIX PLAN
Diagnosis Checklist
When the dragon boss fight is triggered and nothing appears visually, debug in this order:

Check server logs for [DragonModel] messages. The v10.0 AbyssDragonModel.java (from fix_dragon_model.ps1) has extensive logging. Look for:

=== SPAWN START === — confirms the spawn method was called
Zombie spawned, ID: X — confirms hitbox entity created
ItemDisplay spawned, ID: X — confirms visual entity created
SPAWN FAILED: or ABORT: — any error messages
Check if the ItemDisplay actually exists in-game. Use /data get entity @e[type=item_display,limit=1] to see if any ItemDisplay entities exist at the arena location.

Check the resource pack. The current item definition at merged_pack/assets/minecraft/items/abyss_dragon.json is only 129 bytes. Verify its contents — it should either be a standalone item definition file OR the abyss_dragon case should be in paper.json. In 1.21.4+, setItemModel(NamespacedKey.fromString("minecraft:abyss_dragon")) looks for assets/minecraft/items/abyss_dragon.json.

Verify the model JSON exists and is valid. Check that assets/minecraft/models/item/abyss/abyss_dragon.json exists in the resource pack and references a valid texture at assets/minecraft/textures/item/abyss/abyss_dragon.png.

Check spigot.yml attribute.maxHealth.max. Must be ≥ 2048 for the 1500 HP dragon (the code caps at 2048).

Check view range. The ItemDisplay viewRange is set to 2.0 (2× normal). Players must be within render distance.

The Fix: Migrate to BetterModel
Rather than continuing to debug the manual ItemDisplay approach, the recommended fix is to migrate to BetterModel entirely:

Run the animation injection script on the .bbmodel
Place the animated .bbmodel in BetterModel's models folder
Replace AbyssDragonModel.java with the BetterModel tracker wrapper
Merge resource packs
This eliminates all the manual ItemDisplay/CMD/texture-path issues because BetterModel handles the entire rendering pipeline
V. GITHUB REPOSITORY FILE STRUCTURE
The complete repository tree (as of commit 06c51025):

JGlimsPlugin/
├── .gitignore (207 B)
├── JGlimsResourcePack-merged.zip (6.5 MB) — merged JGlims + BetterModel resource pack
├── JGlimsResourcePack-v9.0.zip (3.86 MB) — current live resource pack
├── README.md (64 KB) — this project summary
├── build.gradle (547 B)
├── build.zip (2.81 MB) — compiled plugin JAR archive
├── build_output.txt (8.2 KB) — last build log
├── fix_dragon_model.ps1 (21 KB) — PowerShell script that rewrites AbyssDragonModel.java v10.0
├── instructions_citadel.txt (233 KB) — detailed citadel building instructions
├── gradle/wrapper/gradle-wrapper.jar + .properties
├── gradlew + gradlew.bat
├── merged_pack/
│   └── assets/
│       ├── bettermodel/textures/item/
│       │   ├── a.png (133 KB) — dragon texture (primary)
│       │   ├── b.png (61 KB) — dragon texture (secondary)
│       │   └── c.png (38 KB) — dragon texture (tertiary)
│       └── minecraft/
│           ├── equipment/ (13 armor set JSONs)
│           ├── items/ (30+ item definition JSONs including abyss_dragon.json, paper.json, all weapon bases)
│           ├── models/item/ (100+ model JSONs for weapons, armor, powerups, dragon)
│           │   ├── _backup/ (original vanilla model backups)
│           │   ├── abyss/ (abyss_dragon.json + texture)
│           │   └── (all weapon model files)
│           └── textures/ (all texture PNGs)
└── src/main/java/com/jglims/plugin/ (68 Java source files)
W. GUIDE FOR A NEW CHAT SESSION
Context Loading Instructions
If you are starting a new chat to continue this project, provide the AI assistant with the following:

This summary document (v13.0) — paste it as the first message or attach it.
The specific task you want to accomplish — be explicit about which section/priority you're working on.
The current state of the task — what has been done since this summary was written.
The GitHub repository URL — https://github.com/JGlims/JGlimsPlugin
The Sketchfab collections URL — https://sketchfab.com/JGlims/collections
Step-by-Step Plan for Making Everything Work
PHASE 1: Fix the Dragon (Days 1-3)

Step 1.1: Run the animation injection Python script on the Void_Dragon.bbmodel to produce Void_Dragon_animated.bbmodel. If the script fails because bone names don't match, open the .bbmodel in Blockbench and check the actual bone names in the outliner, then update the script constants.

Step 1.2: Open the animated .bbmodel in Blockbench, switch to Animate tab, verify all 3 animations play correctly.

Step 1.3: Install BetterModel 2.1.0 on the server (plugins/BetterModel-2.1.0.jar).

Step 1.4: Place Void_Dragon_animated.bbmodel (renamed to Void_Dragon.bbmodel) in plugins/BetterModel/models/.

Step 1.5: Start the server, let BetterModel generate its resource pack.

Step 1.6: Merge BetterModel's generated resource pack (from plugins/BetterModel/build/) with the existing JGlims resource pack.

Step 1.7: Deploy the merged pack as v10.0.

Step 1.8: Add BetterModel API dependency to build.gradle.

Step 1.9: Create the new AbyssDragonModel.java that uses BetterModel's tracker API instead of manual ItemDisplay. Use the VoidDragonModel.java class provided in this conversation as the template.

Step 1.10: Update AbyssDragonBoss.java to use the new model class.

Step 1.11: Build, deploy, test in-game.

PHASE 2: Fix the Citadel (Days 4-7)

Step 2.1: Read instructions_citadel.txt (228 KB) thoroughly.

Step 2.2: Enter the Abyss dimension in-game and evaluate the current citadel generation. Screenshot all issues.

Step 2.3: Decide: fix programmatically (modify AbyssCitadelBuilder.java) OR build manually in Creative Mode and save as schematic.

Step 2.4: If programmatic: fix one section at a time, test after each section.

Step 2.5: If schematic: build in Creative, save with structure blocks, load in code using Paper's Structure API.

Step 2.6: Verify arena is properly sealed (bedrock floor, barrier walls), weapon chambers have correct loot, walkway connects properly.

PHASE 3: Fix Remaining Bugs (Days 8-10)

Step 3.1: Identify and fix the Creation Splitter ability bug. Test both primary ("Reality Rift") and alt ("World Break") abilities.

Step 3.2: Create a custom texture for the Abyssal Key (currently uses vanilla Echo Shard).

Step 3.3: Create animated textures (.png.mcmeta) for Whisperwind Awakened, Creation Splitter Awakened, and Edge of the Astral Plane.

Step 3.4: Verify Requiem Awakened animation status.

PHASE 4: Replace All Boss Models with Sketchfab Models (Days 11-20)

Step 4.1: For each of the 6 roaming bosses, find an appropriate model on Sketchfab.

Step 4.2: Download, import to Blockbench, convert to Bedrock Entity format, verify/add animations.

Step 4.3: Deploy each model to BetterModel, create Java wrapper classes.

Step 4.4: Refactor RoamingBossManager.java to use BetterModel trackers.

Step 4.5: Repeat for event bosses.

PHASE 5: New Content — Magical Mobs (Days 21-30)

Step 5.1: Select creature models from Sketchfab collections.

Step 5.2: Create new Java package magic/ with mob definitions, abilities, and spawn logic.

Step 5.3: Design and implement abilities (3 modes per magic item: left-click, right-click, crouch+right-click).

Step 5.4: Create texture assets, deploy resource pack updates.

X. CURRENT STATUS TABLE (Updated 2026-03-23)
System	Status	Notes
63 Legendary Weapons (textures)	COMPLETE	All working
63 Legendary Weapons (abilities)	COMPLETE	Except Creation Splitter bug
13 Armor Sets (inventory + worn)	COMPLETE	All working
7 Power-Up Items	COMPLETE	All working
Infinity Gauntlet System	COMPLETE	All working
Abyss Portal Activation	COMPLETE	4×5 purpur frame + key
Abyss Dimension (world gen)	COMPLETE	Custom terrain generates
Abyssal Citadel	NEEDS FULL REWORK	Structural issues, visual problems
Abyss Dragon Model	NOT APPEARING	Primary active bug — ItemDisplay not rendering
Abyss Dragon Animations	NOT INJECTED	Python script provided but not run yet
BetterModel Integration	NOT STARTED	Plugin not yet installed on server
Abyss Dragon Boss Fight	FUNCTIONAL (no visual)	Mechanics work, model invisible
Creation Splitter Ability	BUG	One ability malfunctioning
Abyssal Key Texture	MISSING	Uses vanilla Echo Shard
Awakened Weapon Animations	PARTIAL	Excalibur done; 3 others missing
6 Server Events	COMPLETE	All functional
40 Structures	COMPLETE	All generating
6 Roaming Bosses	COMPLETE (basic visuals)	Using vanilla entity models
64 Custom Enchantments	COMPLETE	All effects implemented
Guild System	COMPLETE	Full CRUD
Quest System	COMPLETE	6 quest lines
Blood Moon	COMPLETE	15% nightly
Guide Book (PT-BR)	COMPLETE	Multi-volume
Creative Menu	COMPLETE	All items browsable
Sketchfab Boss Models	NOT STARTED	Planned after dragon works
World Weapons	NOT STARTED	Planned
Magic Items	NOT STARTED	Planned
Magical Mobs	NOT STARTED	Planned
Lunar Dimension	NOT STARTED	Planned
Aether Dimension	NOT STARTED	Planned
Y. OUTSTANDING WORK — ALL TASKS (Updated Priority Order)
PRIORITY 0: CRITICAL — Dragon Not Appearing + BetterModel Setup
0a. Install BetterModel 2.1.0 on the server. 0b. Run the animation injection Python script on Void_Dragon.bbmodel. 0c. Deploy the animated .bbmodel to BetterModel's models folder. 0d. Merge resource packs and deploy as v10.0. 0e. Replace AbyssDragonModel.java with BetterModel tracker wrapper. 0f. Update AbyssDragonBoss.java to use new model class. 0g. Test: dragon must be visible with idle animation when fight triggers.

PRIORITY 1: HIGH — Citadel Full Rework
1a. Read instructions_citadel.txt completely. 1b. Evaluate current citadel in-game, document all issues. 1c. Rework AbyssCitadelBuilder.java or switch to schematic-based approach. 1d. Verify arena sealing, weapon chambers, walkway, spires, interior.

PRIORITY 2: HIGH — Bug Fixes
2a. Fix Creation Splitter ability bug. 2b. Create custom Abyssal Key texture. 2c. Create animated textures for 3 awakened weapons.

PRIORITY 3: HIGH — Replace All Boss Models
3a. Replace all 6 roaming boss models with Sketchfab → BetterModel models. 3b. Replace all event boss models. 3c. Replace all structure mini-boss models.

PRIORITY 4: MEDIUM — Performance
4a. Investigate TPS during gameplay. 4b. Limit summoned entities per player. 4c. Cancel previous BukkitRunnables before starting new ones. 4d. Reduce particles if causing lag.

PRIORITY 5: MEDIUM — New Content
5a. World Weapons category. 5b. Magic Items with 3 ability modes. 5c. Magical Mobs using Sketchfab models. 5d. More structures, dungeons, bosses. 5e. Legendary shields, pets, consumable battle items.

PRIORITY 6: LOW — Future Dimensions
6a. Lunar Dimension. 6b. Aether Dimension.

Z. QUICK REFERENCE — KEY COMMANDS AND PATHS
# Build plugin
cd C:\Users\jgmel\Documents\projects\JGlimsPlugin\JGlimsPlugin
.\gradlew clean build

# Deploy plugin to server
$sshKey = "C:\Users\jgmel\Documents\projects\server_minecraft\ssh-key-2026-02-25.key"
scp -i $sshKey build\libs\JGlimsPlugin-2.0.0.jar ubuntu@144.22.198.184:/tmp/JGlimsPlugin.jar
ssh -i $sshKey ubuntu@144.22.198.184 "docker cp /tmp/JGlimsPlugin.jar mc-crossplay:/data/plugins/ && docker restart mc-crossplay"

# Deploy BetterModel
scp -i $sshKey BetterModel-2.1.0.jar ubuntu@144.22.198.184:/tmp/
ssh -i $sshKey ubuntu@144.22.198.184 "docker cp /tmp/BetterModel-2.1.0.jar mc-crossplay:/data/plugins/ && docker restart mc-crossplay"

# Deploy .bbmodel to BetterModel
scp -i $sshKey Void_Dragon.bbmodel ubuntu@144.22.198.184:/tmp/
ssh -i $sshKey ubuntu@144.22.198.184 "docker exec mc-crossplay mkdir -p /data/plugins/BetterModel/models && docker cp /tmp/Void_Dragon.bbmodel mc-crossplay:/data/plugins/BetterModel/models/"

# BetterModel reload (in-game)
/bettermodel reload

# Resource pack deployment
$sha1 = (Get-FileHash -Path $zipPath -Algorithm SHA1).Hash.ToLower()
gh release create v10.0 $zipPath --repo JGlims/JGlimsPlugin --title "Resource Pack v10.0" --notes "SHA-1: $sha1"
# Then update docker-compose.yml and recreate container

# Check server logs
ssh -i $sshKey ubuntu@144.22.198.184 "docker logs mc-crossplay --tail 100"

# In-game debug commands
/bettermodel reload
/jglims legendary
/data get entity @e[type=item_display,limit=1]
END OF DEFINITIVE PROJECT SUMMARY v13.0                                                                                          AND REMEMBER THE README: JGLIMSPLUGIN — ULTIMATE DEFINITIVE PROJECT SUMMARY v12.0
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
END OF DEFINITIVE PROJECT SUMMARY v12.0                                                                                         MAKE THIS NEW SUMMARY EXTREMELY DETAILED AND PERFECT                                                       REMEMBER TO REATE A VERY VERY VER VERY VERY VERY VERY WELL STRUCTURED AND EXTREMELY DETAILED SUMMARY
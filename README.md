JGLIMSPLUGIN — ULTIMATE DEFINITIVE PROJECT SUMMARY v17.0
Compiled: 2026‑03‑23 | Author: JGlims (jg.melo.lima2005@gmail.com) | Plugin Version: 2.0.0 | Target Version: 3.0.0

A. PROJECT METADATA
Repository: https://github.com/JGlims/JGlimsPlugin License: Private / Unlicensed (personal project) Primary Language: Java Build System: Gradle (Gradle Wrapper) Target API: PaperMC API 1.21.11‑R0.1‑SNAPSHOT Artifact: JGlimsPlugin-2.0.0.jar (711,934 bytes / ~712 KB) Java Toolchain Target: Java 21 (compilation), runs on Java 25 at runtime Group ID: com.jglims.plugin Main Class: com.jglims.plugin.JGlimsPlugin API Version: 1.21

JGlimsPlugin is a comprehensive, monolithic Minecraft server-side plugin for Paper 1.21+ servers. It adds an extensive custom content layer on top of vanilla Minecraft, consisting of 63 legendary weapons with unique dual-ability combat systems, 13 custom armor sets with worn texture rendering, 7 consumable power-up items, a 6-stone Infinity Gauntlet system with a snap ability, 64 custom enchantments, 6 random world events with boss encounters, 6 roaming world bosses, 40 procedurally-placed structures across 4 dimensions, a complete multi-line quest system, a guild system with friendly-fire control, and the entirely custom Abyss dimension featuring a massive gothic cathedral citadel and a multi-phase dragon boss fight. The plugin is written entirely in Java, built with Gradle, and targets the PaperMC API. It is designed for a small private server (max 15 players) running in a Docker container on an Oracle Cloud free-tier ARM64 instance with GeyserMC crossplay support for Bedrock Edition clients.

Sketchfab Collections: https://sketchfab.com/JGlims/collections — Curated collections of downloadable 3D models (dragons, demons, creatures, structures) for importing into Blockbench and rendering via BetterModel. Models WITH existing Blockbench animations are prioritized because BetterModel can play them directly. All models used must respect their individual licenses (CC-BY, CC0, etc.).

A1. BUILD SYSTEM
Build Output: JGlimsPlugin-2.0.0.jar (711,934 bytes) Build Command: .\gradlew clean build (from local dev path) Build Time: ~8 seconds Build Warnings (9 total, all non-fatal): 6× GameRule deprecation warnings in AbyssDimensionManager.java (DO_MOB_SPAWNING, DO_WEATHER_CYCLE, DO_FIRE_TICK, MOB_GRIEFING, ANNOUNCE_ADVANCEMENTS, SHOW_DEATH_MESSAGES are deprecated), 3× OldEnum.name() deprecation in LegendaryArmorListener.java.

Actual build.gradle (confirmed from repository):

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
CRITICAL DISCOVERY: The build.gradle already includes the BetterModel API dependency (compileOnly 'io.github.toxicity188:bettermodel-bukkit-api:2.1.0'). The project is already set up to compile against BetterModel's API — no additional dependency changes are needed. The build completed successfully with this dependency.

Plugin Descriptor (plugin.yml): Name JGlimsPlugin, version 2.0.0, main class com.jglims.plugin.JGlimsPlugin, api-version 1.21, author JGlims. The soft-dependency list includes BetterModel (for BetterModel integration). Three commands are defined: /jglims is the admin command with 14+ subcommands (reload, stats, enchants, sort, mastery, legendary, armor, powerup, bosstitles, gauntlet, menu, guia, help) requiring jglims.admin permission (defaults to ops); /guild handles guild operations (create, invite, join, leave, kick, disband, info, list); /guia (alias: /guide) distributes Portuguese-language guide books.

B. SERVER ENVIRONMENT — FULL SPECIFICATION
Cloud Provider: Oracle Cloud Infrastructure (OCI), Always Free tier Instance Shape: VM.Standard.A1.Flex (Ampere ARM64), 4 OCPUs, 24 GB RAM Operating System: Ubuntu (ARM64) Public IP Address: 144.22.198.184 SSH User: ubuntu (CONFIRMED — NOT opc) SSH Key File (local): C:\Users\jgmel\Documents\projects\server_minecraft\ssh-key-2026-02-25.key Open Ports: 25565/TCP (Java Edition), 19132/UDP (Bedrock Edition via GeyserMC), 8080/TCP (nginx for self-hosted resource pack), 22/TCP (SSH)

Docker Container Name: mc-crossplay Docker Image: itzg/minecraft-server (auto-updates Minecraft server binaries, overwrites server.properties from environment variables on every container start) Docker Compose File (on server): /home/ubuntu/minecraft-server/docker-compose.yml Data Volume Mount: /home/ubuntu/minecraft-server/data:/data (bind mount, NOT a named Docker volume)

Server Software: Paper 1.21.11-127 Java Runtime (server): OpenJDK 25.0.2+10-LTS (Eclipse Adoptium Temurin, Java 25) JVM Memory Allocation: 8 GB RAM (-Xmx8G)

Server Configuration (docker-compose.yml environment overrides): Online mode disabled (for crossplay), default game mode Creative, max players 15, view distance 10 chunks, simulation distance 6 chunks, difficulty per-world (Hard in the Abyss). Resource pack URL, SHA-1, and enforcement are set as environment variables RESOURCE_PACK, RESOURCE_PACK_SHA1, and RESOURCE_PACK_ENFORCE in docker-compose.yml. The itzg/minecraft-server Docker image overwrites server.properties on EVERY container start.

CRITICAL RULE: Never edit server.properties directly. Always edit docker-compose.yml and recreate the container with docker compose down && docker compose up -d.

B1. INSTALLED SERVER PLUGINS (Third-Party)
GeyserMC 2.9.4-SNAPSHOT — A bridge/proxy that allows Minecraft Bedrock Edition clients to join Minecraft Java Edition servers, enabling true crossplay between both editions. It translates Bedrock packet protocols into Java packet protocols in real time. Bedrock players connect on port 19132/UDP. Supports Bedrock 1.21.11 and Java 1.21.11. Handles inventory translation, entity rendering differences, form-based UIs, skin translation, and most gameplay mechanics. Website: https://geysermc.org/

Floodgate 2.2.5-SNAPSHOT — Companion plugin for GeyserMC enabling hybrid-mode authentication. Allows Bedrock Edition players to join without a Java Edition account by handling Bedrock Xbox Live authentication server-side, prefixing Bedrock usernames (typically with .) to avoid Java Edition name conflicts. Provides API for other plugins to detect Bedrock players. Download: https://hangar.papermc.io/GeyserMC/Floodgate

SkinsRestorer 15.11.0 — The most popular skin plugin for Minecraft. Restores skins for offline-mode servers, giving players the ability to change skins via /skin command. Since this server runs with online-mode=false for crossplay, SkinsRestorer ensures player skins display correctly. Version 15.11.0 added ely.by support. Supports Bukkit, BungeeCord, Folia, Paper, Purpur, Spigot, Velocity, Fabric, NeoForge across Minecraft 1.8–1.21.11. Website: https://skinsrestorer.net/

Chunky 1.4.40 — Chunk pre-generation plugin that generates world terrain in advance to eliminate lag during exploration. Supports configurable radius, shape, world selection, pause/resume. Commands: /chunky start, /chunky radius, /chunky center, /chunky world, /chunky pause, /chunky cancel. Repository: https://github.com/pop4959/Chunky

BetterModel 2.1.0 — Server-side engine for Minecraft Java that renders and animates Blockbench .bbmodel models using item display entity packets. Imports generic Blockbench models, auto-generates resource packs, plays animations, syncs with base entity, provides custom hitboxes, supports 12-limb player animation, and has MythicMobs integration. Built by toxicity188. Compared to ModelEngine, it reduces network overhead, offers faster updates, a more flexible API, and restores vanilla entity behavior. The build.gradle already has the compile-only dependency on bettermodel-bukkit-api:2.1.0. The plugin's .bbmodel files go into plugins/BetterModel/models/ on the server; after changes, run /bettermodel reload. BetterModel generates its resource pack in BetterModel/build/, which must be merged with the JGlims resource pack before deployment. Downloads: Modrinth (https://modrinth.com/plugin/bettermodel), Hangar (https://hangar.papermc.io/toxicity188/BetterModel), GitHub (https://github.com/toxicity188/BetterModel). Wiki: https://github.com/toxicity188/BetterModel/wiki

C. LOCAL DEVELOPMENT ENVIRONMENT
Developer Machine OS: Windows (PowerShell / cmd) Local Plugin Source Path: C:\Users\jgmel\Documents\projects\JGlimsPlugin\JGlimsPlugin Resource Pack Working Directory: C:\Users\jgmel\Documents\projects\JGlimsPlugin\resourcepack-work\JGlimsResourcePack Original Full Resource Pack (reference): C:\Users\jgmel\Documents\projects\JGlimsPlugin\JGlimsResourcePack-v2.1-server Server Minecraft Project Root: C:\Users\jgmel\Documents\projects\server_minecraft\

D. RESOURCE PACK — CURRENT STATE
D1. Live Version
The server is running resource pack v9.0, now self-hosted on the Oracle Cloud instance via nginx on port 8080.

Current URL: http://144.22.198.184:8080/JGlimsResourcePack.zip (self-hosted via nginx) SHA-1 Hash: 0e6151b68727aff120a9648c8f295078ccfee809 Pack Format: 75 (Minecraft 1.21.4+) Size: ~3.68 MB (standalone JGlims pack); merged pack with BetterModel assets is ~6.5 MB Enforcement: TRUE

The resource pack is now self-hosted on the Oracle Cloud instance via nginx listening on port 8080. This was a major workflow improvement over the previous GitHub Releases approach — with self-hosting, the URL stays constant (http://144.22.198.184:8080/JGlimsResourcePack.zip); only the SHA-1 needs to change when pack content changes. No more creating new GitHub releases for every iteration.

Self-Hosted Setup (COMPLETED): nginx is installed on the OCI instance, configured to serve files from /var/www/resourcepack/ on port 8080. Port 8080 is open in the OCI security list and iptables. The resource pack ZIP is uploaded via SCP to /var/www/resourcepack/JGlimsResourcePack.zip. The docker-compose.yml RESOURCE_PACK variable points to http://144.22.198.184:8080/JGlimsResourcePack.zip.

GitHub Releases are still maintained for "stable" versions (v9.0, v10.0, etc.) as archives. The self-hosted URL is used for rapid development iterations.

D2. What Works in the Current Pack
All 63 legendary weapon textures and 3D models render correctly (sourced from Fantasy Weapons and Blades of Majestica asset packs, converted to 1.21.4+ format using string-based custom_model_data). All 63 primary and alternate abilities are functional. All 13 armor set textures display correctly (inventory icons AND worn armor via equippable component). All 7 power-up item textures display correctly. The Infinity Gauntlet, Thanos Glove, and all 6 Infinity Stone textures render correctly. The Abyss portal activation system works. The plugin loads cleanly without errors.

D3. What Does NOT Work / Needs Rework
The Abyss Dragon boss fight: the dragon model does NOT visually appear when the fight is triggered (boss bar appears, sounds play, mechanics work, but no visible dragon). The root cause is an item model reference chain mismatch (see Section D5). The plan is to migrate to BetterModel entirely, which eliminates all manual ItemDisplay/CMD/texture-path issues.

The Abyssal Citadel (AbyssCitadelBuilder.java, 54 KB) generates but needs a FULL REWORK — structural gaps, floating blocks, misaligned elements, potentially broken arena sealing, performance impact during generation.

One Creation Splitter ability has a bug (either "Reality Rift" primary or "World Break" alt). Missing animated textures for Whisperwind Awakened, Creation Splitter Awakened, and Edge of the Astral Plane (Excalibur Awakened's animated texture works). The Abyssal Key uses a vanilla Echo Shard texture instead of a custom one.

D4. BetterModel Resource Pack Merging
BetterModel auto-generates its own resource pack from .bbmodel files placed in plugins/BetterModel/models/. The generated pack structure is under assets/bettermodel/ (models and textures). Since BetterModel uses the bettermodel namespace and JGlims uses the minecraft namespace, there are NO file conflicts — both assets/minecraft/ and assets/bettermodel/ coexist in the same ZIP. The merged pack is stored in the repository as JGlimsResourcePack-merged.zip (~6.5 MB). The merged_pack/ folder contains the working directory.

Current merged_pack/assets/bettermodel/textures/item/ contains the dragon texture PNGs extracted from the .bbmodel by BetterModel: a.png (133 KB, primary dragon texture 1024×1024), b.png (61 KB, secondary), c.png (38 KB, tertiary).

Texture Hosting Discovery: Textures for BetterModel models are packed into the Minecraft resource pack ZIP and delivered to clients via the standard resource-pack mechanism. They are NOT served from localhost or a separate web server.

How to Update the Merged Pack: Modify the .bbmodel in Blockbench, save, copy to plugins/BetterModel/models/ on the server, run /bettermodel reload, copy regenerated textures from plugins/BetterModel/build/assets/bettermodel/textures/item/ to the local merged_pack/, re-zip with 7-Zip, compute SHA-1, SCP to nginx directory, update SHA-1 in docker-compose.yml, restart container.

D5. Item Model Reference Chain — Dragon Diagnosis
The dragon not appearing is traceable through the item model reference chain. The code sets BOTH setItemModel(NamespacedKey.fromString("minecraft:abyss_dragon")) AND string CMD "abyss_dragon". The setItemModel() method takes priority in 1.21.4+ and resolves to assets/minecraft/items/abyss_dragon.json, which points to model item/abyss_dragon. However, the actual model file exists at models/item/abyss/abyss_dragon.json (note the abyss/ subdirectory). This path mismatch means the model is NOT being found, causing invisible rendering.

THE FIX: Migrate to BetterModel entirely. BetterModel handles the entire rendering pipeline (display entities, resource pack generation, model resolution), eliminating all manual ItemDisplay/CMD/texture-path issues.

D6. Resource Pack Version History
Version	Size	SHA-1 (prefix)	Description
v4.0.0	3.04 MB	63b8e30d…	pack_format 75, initial 63 weapons, 1.21.4+ item definitions
v4.0.2	3.09 MB	626edf29…	Real textures, 3D weapon models
v4.0.3	3.17 MB	6cd61288…	All 15 missing 3D weapon models from v2.1 pack
v4.0.4	3.19 MB	ca60f59f…	Fixed 5 remaining buggy weapons
v4.0.5	3.19 MB	2147a804…	Fixed Excalibur + Requiem Awakened
v4.0.6	3.19 MB	7d03a108…	Added animation mcmeta for Excalibur textures
v4.0.7	3.17 MB	c433786c…	Fix Excalibur 3D models: extract 128×128 frame
v4.0.8	3.17 MB	45aeea8a…	Excalibur Awakened: animated fire texture
v4.0.9	3.17 MB	1b9f9e6c…	Excalibur Awakened: majestica model + blue fire
v4.1.0	3.52 MB	a7a0e72c…	Armor, infinity, powerup textures + bow models
v5.0	3.52 MB	dc5cdce0…	Repackaged for new release URL
v6.0	—	26704612…	Intermediate update
v7.0	—	a7212467…	Fixed Infinity Stone textures, Creation Splitter particles
v8.0	—	—	Abyss Dragon model added (basic 11-cuboid)
v9.0	~3.68 MB	0e6151b6…	CURRENT — Dragon visual fix
v4.1.1	9.5 MB	—	BROKEN — Created by PowerShell Compress-Archive. NEVER USE
E. HOW MINECRAFT 1.21.4+ CUSTOM TEXTURES WORK
Minecraft 1.21.4+ uses item model definitions in assets/minecraft/items/*.json. There are two mechanisms:

Mechanism 1: setItemModel() (newer) — The Java code calls meta.setItemModel(NamespacedKey.fromString("minecraft:my_item")). Minecraft looks for assets/minecraft/items/my_item.json. This file defines which model to render, either as a direct reference or a conditional select.

Mechanism 2: Custom Model Data strings — The items file for the base material (e.g., diamond_sword.json) uses type: "minecraft:select" with property: "minecraft:custom_model_data" and a "when" value matching the string set by CustomModelDataComponent.setStrings(List.of("texture_name")).

For worn armor textures, the equippable component sets an asset_id pointing to assets/minecraft/equipment/<name>.json, which references textures in assets/minecraft/textures/entity/equipment/humanoid/ and humanoid_leggings/.

F. RESOURCE PACK UPDATE PROCEDURE
Step 1: Make changes in the resource-pack working directory. Step 2: DO NOT use PowerShell Compress-Archive — use 7-Zip CLI: & "C:\Program Files\7-Zip\7z.exe" a -tzip "$zipPath" "$workDir\*". Step 3: Compute SHA-1: (Get-FileHash -Path $zipPath -Algorithm SHA1).Hash.ToLower(). Step 4 (GitHub): Upload as a NEW release version: gh release create v10.0 $zipPath --repo JGlims/JGlimsPlugin --title "Resource Pack v10.0" --notes "SHA-1: $sha1". Step 4 (Self-hosted — faster): SCP to nginx: scp -i $sshKey $zipPath ubuntu@144.22.198.184:/var/www/resourcepack/JGlimsResourcePack.zip. Step 5: Update docker-compose.yml on the server with the new SHA-1 via sed. Step 6: Recreate container: docker compose down && docker compose up -d.

G. PLUGIN DEPLOYMENT PROCEDURE
Step 1: Build: .\gradlew clean build → produces build/libs/JGlimsPlugin-2.0.0.jar. Step 2: SCP to server: scp -i $sshKey build/libs/JGlimsPlugin-2.0.0.jar ubuntu@144.22.198.184:/tmp/. Step 3: Docker-copy: docker cp /tmp/JGlimsPlugin-2.0.0.jar mc-crossplay:/data/plugins/. Step 4: Fix ownership: docker exec mc-crossplay chown 1000:1000 /data/plugins/JGlimsPlugin-2.0.0.jar. Step 5: Restart: docker compose down && docker compose up -d, OR RCON hot-reload.

The same workflow applies to deploying BetterModel JARs and .bbmodel files (copy to plugins/BetterModel/models/, then /bettermodel reload in-game).

H. COMPLETE CODEBASE OVERVIEW (68 Java Files, ~1.27 MB Total)
Root Package — com.jglims.plugin
JGlimsPlugin.java (34,896 B) — Main plugin class. onEnable() initializes all managers and listeners, registers all three commands, creates the Abyss dimension via AbyssDimensionManager, and logs startup diagnostics. Contains the command dispatcher for /jglims with its 14+ subcommands.

abyss Package (5 files)
AbyssChunkGenerator.java (7,341 B) — Custom ChunkGenerator producing void-based terrain for world_abyss. ~210-block-radius landmass centered at origin using layered sinusoidal noise. Surface: deepslate tiles (inner), polished blackstone (mid), purpur/end stone (outer). Decorations: amethyst columns (1.2%), obsidian spikes (0.8%), end rods (1.5%), crying obsidian (2.5%), soul campfires (0.6%). Floating islands beyond radius 160. Void rivers between 130–200. All vanilla generation disabled.

AbyssCitadelBuilder.java (54,253 B) — The largest structure builder. Constructs a massive gothic cathedral ("Abyssal Citadel") with: elliptical foundation (110×140), circular courtyard, main nave (80×60×50), cross-shaped transept (±50 X, 45 tall), semicircular apse (radius 25, 55 tall), front façade (120 wide × 80 tall), pointed-arch entrance, rose window (radius 9), gable with finial, center mega-spire (180 blocks), 6 flanker spires (100–120), 15 minor spires (50–90), flying buttresses, gothic windows, interior nave with pillar rows, side chapels with loot, 4 Abyssal Weapon guardian chambers, covered walkway to boss arena (radius 40, bedrock floor, barrier walls, 8 pillars). Block palette: deepslate bricks/tiles, polished deepslate, chiseled deepslate, blackstone, polished blackstone bricks, obsidian, crying obsidian, amethyst, purple/magenta stained glass panes, iron bars, soul lanterns/campfire, end rods/stone bricks, bedrock, barrier, purpur. Populates chests with ABYSSAL/MYTHIC loot. STATUS: NEEDS FULL REWORK. The repository contains instructions_citadel.txt (233,787 B / ~228 KB) at https://raw.githubusercontent.com/JGlims/JGlimsPlugin/main/instructions_citadel.txt — an extremely detailed architectural specification. ANY new session working on the citadel MUST read this file. Reference schematic images are also available.

AbyssDimensionManager.java (18,501 B) — Creates/manages world_abyss. Disables scan-for-legacy-ender-dragon via paper-world.yml. Creates world with THE_END environment. Disables game rules (mob spawning, weather, fire tick, mob griefing, advancements, death messages). HARD difficulty, permanent midnight. Actively disables vanilla DragonBattle every 10 seconds. Triggers citadel build. Spawns ambient mobs ("Void Wanderer" Endermen, "Abyssal Sentinel" Wither Skeletons). Creates Abyssal Key item (Echo Shard, CMD 9001, tag abyssal_key). Handles portal: RIGHT_CLICK_BLOCK on PURPUR_BLOCK with key → 4×5 frame detection → END_GATEWAY fill (30s) → teleport on entry (5s cooldown) → destination z=−115.5. Arena center: ARENA_CENTER_Z = -120.

AbyssDragonBoss.java (23,877 B) — Boss fight controller. 1500 HP, 25% DR, arena radius 40, 30-min respawn cooldown. Fight: 11 waypoints, purple notched boss bar. Combat loop (40 ticks): boss bar, phase checks (50%/25%/10% HP), attack selection, death check. Movement loop (10 ticks): speed 0.8 + (phase × 0.2), 30% player-divert chance. Phases: P1 (void breath 8+ dmg, lightning 6 dmg, wing gust 4 dmg + KB), P2 (+minion summon 3–6), P3 (+ground pound 10 dmg AoE 15 blocks, +void pull), P4 (enrage lightning barrage 8 strikes). Death: dragon egg, 2 nether stars, 1000 XP, 1 guaranteed ABYSSAL weapon, 3 netherite ingots, 16 diamonds, 32 emeralds, 3×3 exit portal.

AbyssDragonModel.java (17,894 B) — Current visual model: invisible Zombie (hitbox, SCALE 3.0, max HP 2048) + ItemDisplay (visual, CMD 40000/"abyss_dragon", FIXED billboard, scale 5.0/5.75/6.25). Animation loop (2 ticks): sinusoidal bob, Y-axis rotation, phase-based scaling, ambient particles. Contains safeParticle() helper for DRAGON_BREATH requiring Float data in Paper 1.21.11+. The fix_dragon_model.ps1 script (21 KB, repo root) rewrites this file with v10.0 fixes. STATUS: Being replaced by BetterModel tracker approach.

blessings Package (2 files)
BlessingListener.java (3,452 B) + BlessingManager.java (10,296 B) — Three blessing types: C-Bless (heal, +1 HP/use, max 10), Ami-Bless (+2% damage/use, max 10), La-Bless (+2% defense/use, max 10). Stats via /jglims stats.

config Package (1 file)
ConfigManager.java (29,211 B) — All config.yml values. Sections: mob difficulty (350–5000 blocks), biome multipliers, boss enhancer (Ender Dragon 3.5× HP, Wither 1.0×, Warden 1.0×, Elder Guardian 2.0×), creeper spawn reduction (50%), Pale Garden fog, loot booster, blessings, anvil mod (no "Too Expensive" + 50% XP reduction), 10 feature toggles, drop rate booster (35% trident from Drowned), villager trade mod (50% price, no locking), king mob (every 100th, 7× HP), axe nerf (0.5 attack speed), weapon mastery (max 1000 kills → +20%), blood moon (15%/night, 1.2× boss HP), guilds (max 10, no FF), dog armor (95% DR), super tools (2%/enchant for netherite), ore detection radii.

crafting Package (2 files)
RecipeManager.java (34,774 B) — All custom recipes: 8 battle tool types, diamond/netherite super tools, sickles, Infinity Stone combining, Infinity Gauntlet assembly, 24 craftable armor pieces. VanillaRecipeRemover.java (588 B) — Removes conflicts.

enchantments Package (5 files)
AnvilRecipeListener.java (33,648 B) — Custom anvil UI, enchant combining, Stone assembly, conflict detection. CustomEnchantManager.java (8,853 B) — 64 enchantment definitions. EnchantmentEffectListener.java (69,495 B) — LARGEST FILE. All 64 effects. EnchantmentType.java (1,942 B) — Type enum. SoulboundListener.java (7,961 B) — Keep items on death.

events Package (7 files)
EventManager.java (10,367 B) — Scheduler (1200 ticks), 10-min cooldowns. NetherStormEvent.java (11,421 B) — Ghast swarms, Infernal Overlord (400 HP). PiglinUprisingEvent.java (8,956 B) — Piglin Emperor (500 HP). VoidCollapseEvent.java (8,961 B) — Void Leviathan (500 HP). PillagerWarPartyEvent.java (19,957 B) — Escalating waves. PillagerSiegeEvent.java (19,146 B) — Fortress Warlord boss. EndRiftEvent.java (30,058 B) — 15×15 portal, End Rift Dragon (600 HP).

guilds Package (2 files)
GuildListener.java (1,456 B) + GuildManager.java (13,679 B) — Full CRUD, invitations, friendly fire toggle, flat-file persistence.

legendary Package (15 files — core of the plugin)
LegendaryTier.java (3,673 B) — 5 tiers: COMMON (12–15 dmg, 10 particles, 1.0× CD, white), RARE (14–17, 25, 0.95×, aqua), EPIC (17–20, 50, 0.9×, gold), MYTHIC (20–30, 100, 0.85×, light purple), ABYSSAL (28–40, 200, 0.8×, dark red RGB 170,0,0). Backward compat: "LEGENDARY" → EPIC, "UNCOMMON" → COMMON.

LegendaryWeapon.java (15,034 B) — 63-weapon enum with id, displayName, baseMaterial, baseDamage, customModelData (30001–30063), tier, textureName, primaryAbilityName, altAbilityName, primaryCooldown, altCooldown. Distribution: 20 COMMON, 8 RARE, 7 EPIC, 24 MYTHIC, 4 ABYSSAL.

LegendaryWeaponManager.java (7,937 B) — Creates ItemStacks with string CMD, PDC tags, Adventure API tier-colored names, ability lore. All Unbreakable.

LegendaryAbilityListener.java (18,032 B) — Right-click → primary, crouch + right-click → alternate.

LegendaryPrimaryAbilities.java (69,951 B) — All 63 primary abilities.

LegendaryAltAbilities.java (73,055 B) — All 63 alt abilities. Second-largest file.

LegendaryAbilityContext.java (4,941 B) — Shared state maps, getNearbyEnemies(), dealDamage(), getTargetEntity(), rotateY(), spawnThrottled(), trackTask(), trackSummon(), cleanupPlayer().

LegendaryArmorSet.java (16,160 B) — 13 armor sets: 6 craftable (Reinforced Leather 12 def, Copper 14, Chainmail Reinforced 15, Amethyst 16, Bone 16, Sculk 18), 7 legendary (Shadow Stalker 24/RARE, Blood Moon 30/EPIC, Nature's Embrace 28/EPIC, Frost Warden 30/EPIC, Void Walker 36/MYTHIC, Dragon Knight 40/MYTHIC, Abyssal Plate 50/ABYSSAL +30% dmg + Wither immunity). CMD range 30101–30254.

LegendaryArmorManager.java (16,059 B) — Equippable component, asset_id for worn textures.

LegendaryArmorListener.java (38,729 B) — All set bonuses and per-piece passives.

LegendaryLootListener.java (22,565 B) — Tiered drop tables.

InfinityStoneManager.java (8,029 B) — 6 stones: Power (purple), Space (blue), Reality (red), Soul (orange), Time (green), Mind (yellow). Fragment + Nether Star → Stone via anvil.

InfinityGauntletManager.java (23,370 B) — Thanos Glove + 6 Stones → Gauntlet. Snap: kills 50% loaded hostiles, 300s cooldown.

menu Package (2 files)
CreativeMenuManager.java (45,503 B) — Paginated GUI menus. GuideBookManager.java (29,396 B) — PT-BR multi-volume books via /guia.

mobs Package (7 files)
BiomeMultipliers.java (3,080 B), BloodMoonManager.java (12,329 B) — 15%/night, red sky, buff mobs, boss every 10th spawn, double drops, 0.1% Stone fragments. BossEnhancer.java (19,998 B) — Enhanced vanilla bosses. BossMasteryManager.java (12,294 B) — Titles: Wither Slayer +5%, Guardian +7%, Warden +10%, Dragon +15%, Abyssal Conqueror +20%, God Slayer +25%/+20%. KingMobManager.java (5,972 B) — Every 100th mob → King (7× HP, 2.5× dmg). MobDifficultyManager.java (4,744 B) — Distance scaling 350–5000 blocks. RoamingBossManager.java (57,427 B) — Second-largest file. 6 bosses: The Watcher (Deep Dark, 800 HP, EPIC), Hellfire Drake (Nether, 600 HP, EPIC), Frostbound Colossus (Snowy, 700 HP, EPIC), Jungle Predator (Jungle, 500 HP, RARE/EPIC), End Wraith (End, 900 HP, MYTHIC), Abyssal Leviathan (Abyss, 1200 HP, ABYSSAL). 2–4% spawn/2min, 10min despawn.

powerups Package (2 files)
PowerUpManager.java (26,207 B) — 7 items: Heart Crystal (RED_DYE, +2 HP, max 40 = +80 HP), Soul Fragment (PURPLE_DYE, +1% dmg, max 100 = +100%), Titan's Resolve (IRON_NUGGET, +10% KB resist + 2% DR, max 5), Phoenix Feather (FEATHER, auto-revive 4 hearts), KeepInventorer (ENDER_EYE, permanent keep-inv, one-time), Vitality Shard (PRISMARINE_SHARD, +5% DR, max 10 = 50%), Berserker's Mark (BLAZE_POWDER, +3% attack speed, max 10 = 30%). DR capped at 75%. PowerUpListener.java (5,607 B) — Consumption + death events.

quests Package (3 files)
QuestManager.java (27,940 B) — 6 quest lines (Overworld, Nether, End, Special, Boss, Explorer). QuestProgressListener.java (3,231 B) — Kill/explore/collect tracking. NpcWizardManager.java (15,501 B) — Archmage villager selling MYTHIC weapons (48 diamonds + 16 Nether Stars), Heart Crystals, Phoenix Feathers, KeepInventorer (64 diamonds + 32 emeralds), Blessing Crystals.

structures Package (5 files)
StructureType.java (10,780 B) — 40 types: 24 Overworld, 7 Nether, 5 End, 3 Abyss (Abyssal Castle 120×80×120, Void Nexus, Shattered Cathedral), + Ender Dragon Death Chest. Generation: COMMON 0.8%, RARE 0.5%, EPIC 0.3%, MYTHIC 0.1%, ABYSSAL 0.05%/chunk. 300-block min spacing. StructureManager.java (49,819 B), StructureBuilder.java (8,714 B), StructureLootPopulator.java (8,554 B), StructureBossManager.java (14,554 B).

utility Package (7 files)
BestBuddiesListener.java (11,609 B) — Wolf armor (95% DR). DropRateListener.java (3,289 B) — 35% trident boost. EnchantTransferListener.java (7,135 B). InventorySortListener.java (5,282 B). LootBoosterListener.java (8,991 B). PaleGardenFogTask.java (1,356 B). VillagerTradeListener.java (4,244 B) — 50% price, no locking.

weapons Package (13 files)
BattleAxeManager.java (8,303 B), BattleBowManager.java (4,871 B), BattleMaceManager.java (4,279 B), BattlePickaxeManager.java (7,168 B), BattleShovelManager.java (8,451 B), BattleSpearManager.java (8,339 B), BattleSwordManager.java (6,821 B), BattleTridentManager.java (4,737 B), SickleManager.java (8,061 B), SpearManager.java (11,128 B), SuperToolManager.java (22,176 B) — 11 non-legendary weapon types (wood through netherite).

WeaponAbilityListener.java (81,738 B) — Third-largest file. 20+ non-legendary abilities.

WeaponMasteryManager.java (9,803 B) — Novice (0), Fighter (100, +1%), Experienced (500, +5%), Master (1000, +15%).

I. LEGENDARY WEAPONS — COMPLETE TABLE (63 weapons)
COMMON Tier (20 weapons, damage 12–15, particle budget 10)
#	ID	Display Name	Material	Dmg	CMD	Texture Name	Primary	Alt	PCD	ACD
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
Fuzzy Texture Name Mappings: divine_axe_rhitta → divineaxerhitta, tengens_blade → tengensblade, thousand_demon_daggers → thousanddemondaggers, rivers_of_blood → riversofblood, dragon_slaying_blade → dragonslayingblade, creation_splitter → creationsplitter.

J. BETTERMODEL INTEGRATION — COMPLETE TECHNICAL GUIDE
J1. Architecture
Pipeline: raw .bbmodel → Blueprint (parsed model data) → Renderer (render pipeline, manages display entities) → Tracker (active instance attached to an entity).

J2. API Usage
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

// Play animations
tracker.animate("idle", AnimationModifier.builder()
    .type(AnimationIterator.Type.LOOP).build());
tracker.animate("attack", AnimationModifier.DEFAULT_WITH_PLAY_ONCE);
tracker.animate("death", AnimationModifier.DEFAULT_WITH_PLAY_ONCE);

// Cleanup
tracker.close();
J3. Dragon .bbmodel — Bone Hierarchy
The Void_Dragon .bbmodel (ArtsByKev model, Blockbench format v5.0, Bedrock Entity, 1024×1024 textures) has the following bone hierarchy:

body (root) → bone24 (L wing root) → bone25 (L wing tip)
            → bone26 (R wing root) → bone27 (R wing tip)
            → bone28 (head) → bone29 (jaw)
            → bone30 (tail base) → bone31 (tail mid) → bone32 (tail tip)
J4. Animation Status — CURRENT WORK IN PROGRESS
The .bbmodel originally had ZERO animations. BetterModel only plays animations that exist within the .bbmodel. A Python animation injection script (inject_animations.py) was provided that generates idle/attack/death animations with sinusoidal keyframes matched to the bone hierarchy. The dragon model has been improved to a good quality level. The current active work is in the animation phase — creating and refining animations using Blockbench's Code View plugin (to edit raw JSON directly) and/or VS Code for direct .bbmodel JSON editing.

J5. BetterModel Resource Pack Config
Copynamespace: bettermodel
pack-type: folder  # or "zip"
build-folder-location: BetterModel/build
K. BLOCKBENCH PLUGINS — COMPLETE REFERENCE
K1. Installing Blockbench Plugins
Blockbench has a built-in plugin browser. Access via File → Plugins (or the puzzle piece icon in the toolbar). The Available tab lists all community plugins from the official registry at https://github.com/JannisX11/blockbench-plugins/blob/master/plugins.json. Click Install on any plugin. Plugins can also be loaded from local .js files via drag-and-drop or File → Plugins → Load Plugin from File.

K2. Code View Plugin — CRITICAL FOR THIS PROJECT
Code View (by "wither", v1.0.1) — This is the plugin being used in the current animation workflow. It displays the raw JSON of the model in real-time as you edit in Blockbench. When installed (via File → Plugins → search "Code View"), it adds a panel that shows the complete .bbmodel JSON, updated live as you make changes. The plugin uses the developer_mode icon and works in both desktop and web variants of Blockbench. This is invaluable for: debugging texture paths and UV mappings, verifying element coordinates and bone hierarchies, editing animation keyframes directly in JSON, comparing model structure before and after changes, and understanding exactly what Blockbench exports.

Current usage: The Code View plugin is being used alongside VS Code for the dragon animation process. The workflow is: open the .bbmodel in Blockbench, use Code View to inspect the raw JSON, switch to the Animate tab to visually preview, then either edit keyframes in the Blockbench UI or switch to VS Code for bulk/precise JSON editing of the "animations" array. This hybrid approach (visual preview in Blockbench + precise JSON editing in VS Code) is the most efficient for creating BetterModel-compatible animations.

K3. Other Relevant Blockbench Plugins
Animated Java (by Titus Evans/SnaveSutit, v1.8.1) — Creates complex animations using display entities for Java Edition. Generates data packs + resource packs. Desktop only. An alternative to BetterModel for data-pack-driven animation, but NOT needed for our use case (we use BetterModel for server-side control). Its animation tools are excellent for prototyping.

Optimize (by Krozi) — Hides concealed faces for better performance. Run on every complex model before export.

Plaster (by JannisX11) — Fixes texture bleeding (colored lines at UV edges) by slightly shrinking UV maps. Run before exporting any model.

Shape Generator (by dragonmaster95) — Generates geometric shapes (spheres, cylinders, cones) for custom mob body parts.

MTools / Mesh Tools (by Malik12tree) — Powerful mesh modeling tools, operators, and generators for Generic Model format.

GeckoLib Models & Animations (by Eliot Lash, Tslat, Gecko, McHorse, v4.2.4) — For GeckoLib Java mod animations. Useful reference for Bedrock animation format understanding.

Resource Pack Utilities (by Ewan Howell, v1.9.2) — Utilities for resource-pack creation: verify pack structure, check missing textures, validate JSON. Desktop only.

Free Rotation (by Godlander & Ewan Howell, v1.2.1) — Creates Java item models without rotation limitations. Desktop only.

Animation Sliders (by JannisX11) — Adds sliders to tweak keyframe values. Makes animation editing more intuitive.

Structure Importer (by JannisX11 & Krozi) — Imports Minecraft .nbt structure files. Could be useful for importing citadel structures from in-game designs.

Asset Browser (by Ewan Howell, v1.2.1) — Browse Minecraft's built-in assets from within Blockbench. Desktop only.

Colour Gradient Generator (by Ewan Howell) — Generates hue-shifted gradient palettes. Useful for Abyss-themed color work.

Scene Recorder (by Ewan Howell, v2.1.1) — Higher-quality recording replacing built-in GIF. Desktop only.

Texture Stitcher (by McHorse) — Stitches multiple textures into a single atlas.

Bakery (by JannisX11) — Bakes complex animations into simple linear keyframes.

Recommended Installation Order: Code View (install first — use for all debugging), Optimize (run on every model), Plaster (fix texture bleeding), Animation Sliders (tweak animations), Resource Pack Utilities (validate packs).

L. PROBLEMS SOLVED (Historical + This Conversation)
Previously solved: BOM build failure, particle throttle for lag reduction, missing Particle import, resource pack URL issues, Docker-compose overwriting server.properties, file ownership (UID 1000), sed escaping, Infinity Stone textures, Creation Splitter particles, phantom Ender Dragon boss bar in world_abyss.

Solved in this conversation: Particle.DRAGON_BREATH crash (Paper 1.21.11+ requires Float data parameter — fix_dragon_model.ps1 rewrites with safeParticle() helper). BetterModel discovery and API analysis (identified as best free engine). Void_Dragon bone hierarchy mapped. Animation injection script designed (Python, generates idle/attack/death). Item model reference chain mismatch diagnosed (items/abyss_dragon.json → item/abyss_dragon vs actual model at models/item/abyss/abyss_dragon.json). build.gradle confirmed to already have BetterModel dependency. Self-hosted resource pack set up via nginx on Oracle Cloud port 8080. Dragon model quality improved to good level — now in animation phase.

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
Particle budgets: COMMON 10, RARE 25, EPIC 50, MYTHIC 100, ABYSSAL 200.
All legendary items Unbreakable.
pack_format: 75.
File-size limit per source: ~120 KB; split if exceeding.
BetterModel .bbmodel files go in plugins/BetterModel/models/. After changes: /bettermodel reload.
BetterModel generates resource pack in BetterModel/build/. Merge with JGlims pack before deployment.
Python scripts modifying .bbmodel must preserve all existing fields.
The build.gradle already includes BetterModel API dependency.
When creating item model chains: setItemModel() resolves to assets/minecraft/items/<key>.json. The model path inside must match an actual file in assets/minecraft/models/.
N. GITHUB REPOSITORY FILE STRUCTURE
JGlimsPlugin/
├── .gitignore (207 B)
├── JGlimsResourcePack-merged.zip (6.5 MB) — merged JGlims + BetterModel RP
├── JGlimsResourcePack-v9.0.zip (3.86 MB) — current standalone RP
├── README.md (64 KB) — project summary
├── build.gradle (547 B) — includes BetterModel API dep
├── build.zip (2.81 MB) — compiled plugin archive
├── build_output.txt (8.2 KB) — last build log (9 warnings, BUILD SUCCESSFUL)
├── fix_dragon_model.ps1 (21 KB) — rewrites AbyssDragonModel.java v10.0
├── instructions_citadel.txt (233 KB) — citadel architecture spec
├── gradle/wrapper/ (jar + properties)
├── gradlew + gradlew.bat
├── merged_pack/
│   └── assets/
│       ├── bettermodel/textures/item/
│       │   ├── a.png (133 KB) — dragon texture primary
│       │   ├── b.png (61 KB) — dragon texture secondary
│       │   └── c.png (38 KB) — dragon texture tertiary
│       └── minecraft/
│           ├── equipment/ (13 armor set JSONs)
│           ├── items/ (30+ item definition JSONs)
│           │   ├── abyss_dragon.json (129 B)
│           │   ├── paper.json (542 B) + paper.json.bak
│           │   ├── diamond_sword.json (5,847 B)
│           │   ├── diamond_axe.json (1,467 B)
│           │   ├── trident.json (679 B)
│           │   └── (all other weapon/armor/powerup/stone items)
│           ├── models/item/ (100+ model JSONs)
│           │   ├── _backup/ (vanilla model backups)
│           │   ├── abyss/ (dragon model subdir)
│           │   └── (all weapon 3D model files)
│           └── textures/ (all texture PNGs)
└── src/main/java/com/jglims/plugin/ (68 Java files, 12 packages)
O. CURRENT STATUS TABLE (2026-03-23)
System	Status	Notes
63 Legendary Weapons (textures)	COMPLETE	All rendering correctly
63 Legendary Weapons (abilities)	COMPLETE	Except Creation Splitter bug
13 Armor Sets (inventory + worn)	COMPLETE	All working
7 Power-Up Items	COMPLETE	All working
Infinity Gauntlet System	COMPLETE	Stones, Glove, Gauntlet, snap
64 Custom Enchantments	COMPLETE	All effects implemented
6 Server Events	COMPLETE	All functional
40 Structures	COMPLETE	All generating with loot/bosses
6 Roaming Bosses	COMPLETE (basic visuals)	Using vanilla entity models
Guild / Quest / Blood Moon / Guide	COMPLETE	All functional
Self-Hosted Resource Pack (nginx)	COMPLETE	Port 8080 on OCI
BetterModel Gradle Dependency	COMPLETE	Already in build.gradle
Abyss Portal Activation	COMPLETE	4×5 purpur + key
Abyss Dimension (world gen)	COMPLETE	Custom terrain
Abyss Dragon Model Quality	GOOD	High-quality Blockbench model ready
Abyss Dragon Animations	IN PROGRESS	Using Code View plugin + VS Code
BetterModel Server Installation	IN PROGRESS	Plugin setup underway
Abyss Dragon Boss Fight Visual	NOT WORKING	Model doesn't appear; migrating to BetterModel
Abyssal Citadel	NEEDS FULL REWORK	Structural/visual issues
Creation Splitter Ability	BUG	Needs diagnosis
Abyssal Key Texture	MISSING	Uses vanilla Echo Shard
Awakened Weapon Animations	PARTIAL	Excalibur done; 3 others missing
Sketchfab Boss Models	NOT STARTED	After dragon works
World Weapons / Magic Items	NOT STARTED	Planned
Magical Mobs	NOT STARTED	Planned
P. OUTSTANDING WORK — PRIORITY ORDER
PRIORITY 0: CRITICAL — Dragon Animations + BetterModel
0a. Complete dragon animations in Blockbench using Code View plugin / VS Code (idle loop, attack play-once, death play-once). Currently in progress. 0b. Deploy animated .bbmodel to plugins/BetterModel/models/ on server. 0c. Ensure BetterModel 2.1.0 is installed and running on server. 0d. Merge BetterModel-generated RP with JGlims RP, deploy via nginx. 0e. Replace AbyssDragonModel.java with BetterModel tracker wrapper class. 0f. Update AbyssDragonBoss.java to use new model. 0g. Test: dragon must be VISIBLE with idle animation when fight triggers.

PRIORITY 1: HIGH — Citadel Full Rework
1a. Read instructions_citadel.txt (228 KB) + reference schematic images completely. 1b. Evaluate current citadel in-game, document all issues. 1c. Choose approach: programmatic fix (async generation with getChunkAtAsync()) vs schematic-based rebuild (build in Creative, save as .schem/.nbt, load via Paper Structure API). 1d. Execute rebuild section by section (foundation → nave → transept → apse → façade → spires → interior → arena). 1e. Verify arena sealing, weapon chambers, walkway, spires, interior.

PRIORITY 2: HIGH — Bug Fixes
2a. Fix Creation Splitter ability bug (inspect both primary/alt in LegendaryPrimaryAbilities.java / LegendaryAltAbilities.java). 2b. Create custom Abyssal Key texture (model + texture + CMD case in echo_shard.json). 2c. Create animated textures (.png.mcmeta) for Whisperwind Awakened, Creation Splitter Awakened, Edge of the Astral Plane.

PRIORITY 3: HIGH — Replace All Boss Models with Sketchfab → BetterModel
3a. For each of the 6 roaming bosses, find appropriate Sketchfab models (prioritize models WITH existing animations). 3b. Download, import to Blockbench, convert to Bedrock Entity, verify/add animations. 3c. Deploy to BetterModel, create Java wrapper classes. 3d. Refactor RoamingBossManager.java to use BetterModel trackers. 3e. Repeat for event bosses and structure mini-bosses.

PRIORITY 4: MEDIUM — Performance + New Content
4a. TPS optimization, entity count limits, particle reduction. 4b. World Weapons category, Magic Items with 3 ability modes. 4c. Magical Mobs using Sketchfab models. 4d. Additional structures, dungeons.

PRIORITY 5: LOW — Future Dimensions
5a. Lunar Dimension (moon-themed, reduced gravity). 5b. Aether Dimension (floating sky islands).

Q. QUICK REFERENCE — ALL COMMANDS AND PATHS
Copy# === LOCAL PATHS ===
$devPath = "C:\Users\jgmel\Documents\projects\JGlimsPlugin\JGlimsPlugin"
$rpWork  = "C:\Users\jgmel\Documents\projects\JGlimsPlugin\resourcepack-work\JGlimsResourcePack"
$sshKey  = "C:\Users\jgmel\Documents\projects\server_minecraft\ssh-key-2026-02-25.key"
$remote  = "ubuntu@144.22.198.184"

# === BUILD & DEPLOY PLUGIN ===
cd $devPath; .\gradlew clean build
scp -i $sshKey "$devPath\build\libs\JGlimsPlugin-2.0.0.jar" "${remote}:/tmp/JGlimsPlugin.jar"
ssh -i $sshKey $remote "docker cp /tmp/JGlimsPlugin.jar mc-crossplay:/data/plugins/ && docker restart mc-crossplay"

# === DEPLOY BETTERMODEL ===
scp -i $sshKey BetterModel-2.1.0.jar "${remote}:/tmp/"
ssh -i $sshKey $remote "docker cp /tmp/BetterModel-2.1.0.jar mc-crossplay:/data/plugins/ && docker restart mc-crossplay"

# === DEPLOY .BBMODEL ===
scp -i $sshKey Void_Dragon.bbmodel "${remote}:/tmp/"
ssh -i $sshKey $remote "docker exec mc-crossplay mkdir -p /data/plugins/BetterModel/models && docker cp /tmp/Void_Dragon.bbmodel mc-crossplay:/data/plugins/BetterModel/models/"
# Then in-game: /bettermodel reload

# === RESOURCE PACK (SELF-HOSTED — FAST) ===
& "C:\Program Files\7-Zip\7z.exe" a -tzip pack.zip .\merged_pack\*
$sha1 = (Get-FileHash pack.zip -Algorithm SHA1).Hash.ToLower()
scp -i $sshKey pack.zip "${remote}:/var/www/resourcepack/JGlimsResourcePack.zip"
ssh -i $sshKey $remote "cd /home/ubuntu/minecraft-server && sed -i 's|RESOURCE_PACK_SHA1=.*|RESOURCE_PACK_SHA1=$sha1|' docker-compose.yml && docker compose down && docker compose up -d"

# === RESOURCE PACK (GITHUB RELEASE — ARCHIVE) ===
gh release create v10.0 pack.zip --repo JGlims/JGlimsPlugin --title "RP v10.0" --notes "SHA-1: $sha1"

# === CHECK LOGS ===
ssh -i $sshKey $remote "docker logs mc-crossplay --tail 100"
ssh -i $sshKey $remote "docker logs mc-crossplay 2>&1 | grep -i 'bettermodel\|dragon\|error'"

# === IN-GAME COMMANDS ===
/bettermodel reload
/jglims legendary
/jglims armor
/jglims menu
/data get entity @e[type=item_display,limit=1]
Copy
R. GUIDE FOR A NEW CHAT SESSION
R1. Context Loading
Paste this entire summary (v17.0) as the first message, then specify which task you're working on.

R2. Key Files to Read (Depending on Task)
Dragon animation: Read fix_dragon_model.ps1, inspect the .bbmodel in Blockbench with Code View plugin, reference BetterModel wiki at https://github.com/toxicity188/BetterModel/wiki/API-example.

Citadel rework: Read instructions_citadel.txt (228 KB) via crawler on https://raw.githubusercontent.com/JGlims/JGlimsPlugin/main/instructions_citadel.txt. Also review schematic images.

Weapon/ability bugs: Read the specific Java source files from the repo.

Resource pack issues: Read merged_pack/assets/minecraft/items/paper.json, abyss_dragon.json, and the relevant model JSONs.

R3. What Has Been Accomplished vs What Remains
DONE: All 63 weapons + abilities, all 13 armor sets, all 7 power-ups, Infinity Gauntlet, 64 enchantments, 6 events, 40 structures, 6 roaming bosses, guilds, quests, Blood Moon, guide books, creative menu, self-hosted resource pack on nginx, BetterModel Gradle dependency, good-quality dragon model.

IN PROGRESS: Dragon animation (using Code View plugin in Blockbench + VS Code JSON editing), BetterModel server integration.

REMAINING: Complete dragon animations → deploy via BetterModel → fix dragon visibility → citadel rework → bug fixes (Creation Splitter, Abyssal Key texture, 3 awakened weapon animations) → replace all boss models with Sketchfab/BetterModel → new content (World Weapons, Magic Items, Magical Mobs).

END OF ULTIMATE DEFINITIVE PROJECT SUMMARY v17.0













JGLIMSPLUGIN — PROJECT CONTINUATION SUMMARY v17.1
Compiled: 2026-03-27 | Continues from: v17.0 README (already in repo) | Purpose: Paste this at the start of the next chat so the AI has full context to complete the remaining dragon animation + code integration work.

WHAT THE v17.0 README ALREADY COVERS (do NOT repeat)
The full v17.0 README is committed to https://github.com/JGlims/JGlimsPlugin and covers: project metadata, build system, server environment (OCI ARM64, Docker, Paper 1.21.1-1), local dev paths, resource pack state (self-hosted nginx, SHA-1 0e6151b6, pack format 75, ~3.68 MB standalone / ~6.5 MB merged), all 63 weapons, 13 armor sets, 7 power-ups, Infinity Gauntlet, 64 enchantments, 6 events, 6 roaming bosses, 40 structures, quest system, guild system, Abyss dimension, citadel, full codebase overview (68 Java files, ~1.27 MB), deployment procedures, and the complete outstanding work priority list.

DO NOT regenerate any of the above. Start from the state described below.

CURRENT STATE OF THE ABYSS DRAGON (as of 2026-03-27)
A. The .bbmodel File
Location: C:\Users\jgmel\Downloads\abyss_dragon.bbmodel File size: ~481,816 bytes (after JSON fix) Backups exist: .bak, .bak2, .bak3 in the same directory Geometry: Full dragon model with 40+ bones including body, head, jaw, left_wing, right_wing, tail, and many sub-bones (bone1 through bone43) Textures: 3 embedded PNGs — a.png (1024×1024, 133 KB primary), b.png (61 KB secondary), c.png (38 KB tertiary)

B. Animation Status — NEEDS COMPLETE REDO
The file currently contains 7 animations that were injected via PowerShell: idle, fly, attack, fire_breath, walk, spawn, death. However, these animations are fundamentally inadequate:

Too short: Most are under 1 second effective duration despite nominal lengths of 2–5 seconds
Not fluid: Keyframes use basic interpolation with insufficient intermediate poses
Minimal bone coverage: Only animate body, head, jaw, left_wing, right_wing, tail — the 40+ sub-bones (wing segments, tail segments, leg bones, neck segments) are untouched, making the animation look stiff and puppet-like
Not vivid: Small rotation/position values, no dramatic sweeps or weight-shifts
Red laser artifact: The model may contain a beam/laser bone group that should be deleted in Blockbench (Edit mode → right-click bone → Delete)
What good animations need:

idle (4+ seconds, LOOP): Deep breathing body bob, slow head sway with neck segments, wing micro-adjustments across all wing sub-bones, sinuous tail wave propagating through tail segments, occasional jaw open/close
fly (3+ seconds, LOOP): Powerful wing strokes with proper down-stroke/up-stroke across all wing segments (not just root bone), body pitch oscillation, tail streaming, head angled down
attack (2.5+ seconds, PLAY_ONCE): Wind-up rear-back with weight shift, explosive forward lunge, jaw snap at peak, wing flare for balance, tail whip, full recovery to neutral
fire_breath (4+ seconds, PLAY_ONCE): Head raises, jaw opens wide (45°+), head sweeps left-to-right during 2.5s burn, neck segments follow with delay, wings spread and brace, body leans back, recovery. NO laser/beam bone animation — attacks are custom-coded with particles
walk (3+ seconds, LOOP): Ground-level body sway, gentle wing half-beats, head look-around, tail counter-swing, if leg bones exist they should animate
spawn (5+ seconds, PLAY_ONCE): Dramatic entrance — rise from below ground, spin, scale from tiny to full size, wings unfold from folded position, jaw roar, head reveal
death (5+ seconds, HOLD last frame): Head rears in pain, body spirals and tilts, wings go limp and droop, body falls and shrinks, tail trails behind
The interpolation must use catmullrom or smooth for fluidity, not linear.

C. Previous Injection Attempt — FAILED
A PowerShell script was provided to replace the animations block in the .bbmodel file. The script did not execute — it hung because the terminal was waiting for the here-string closure ('@). The script needs to be provided in a way that can actually be pasted and run in PowerShell without hanging (either as a .ps1 file to execute, or with the JSON written to a temp file first, or split into manageable chunks).

Lesson for next session: Do NOT use inline PowerShell here-strings with massive JSON. Instead, either:

Write the JSON to a separate .json temp file first, then have a short script read and inject it, OR
Provide a downloadable .ps1 script file, OR
Provide the complete .bbmodel file content for manual replacement
D. AbyssDragonModel.java — NEEDS FULL REWRITE
Current file: src/main/java/com/jglims/plugin/abyss/AbyssDragonModel.java (17,894 bytes, ~800 lines) Current approach: Old ItemDisplay + CustomModelData approach with reflection-based BetterModel soft-dependency. Contains manual Java-driven animation loop (every 2 ticks), sinusoidal bob, Y-axis rotation, particle effects, phase scaling, all coded in Java.

Target approach: Complete rewrite as a BetterModel tracker wrapper using the direct API (no reflection). The new version must:

Use BetterModel's direct API — BetterModel.model("abyss_dragon"), renderer.getOrCreate(BaseEntity.of(zombie)), tracker.animate(name, modifier, callback)
Keep all existing public methods that AbyssDragonBoss.java calls — spawn(Location), playAttackAnimation(), playFireBreathAnimation(), playWalkAnimation(), playFlyAnimation(), playDeathAnimation(), damageFeedback(), onPhaseChange(int), moveTo(Location), cleanup(), getLocation(), isAlive(), getHitboxEntity()
Add proper AnimationModifier presets for each animation:
idle: LOOP, 10-tick lerp-in/out
fly: LOOP, 8-tick lerp
walk: LOOP, 5-tick lerp
attack: PLAY_ONCE, 3-tick lerp-in, 5-tick lerp-out, override=true
fire_breath: PLAY_ONCE, 5-tick lerp-in, 8-tick lerp-out, override=true
spawn: PLAY_ONCE, 0-tick lerp-in, 10-tick lerp-out, override=true
death: HOLD, 5-tick lerp-in, 0-tick lerp-out, override=true
Include TrackerUpdateAction calls for phase transitions (glow, tint, brightness)
Include damageTint() for hit feedback
Keep particle effects (DRAGON_BREATH ambient, phase transition particles) using Bukkit scheduler alongside the BetterModel animation — BetterModel handles the model animation, but particles and sounds are still our responsibility
Be comprehensive — not a skeleton 180-line file. Include all the helper methods, logging, null-safety, state tracking, and robustness of the original 800-line version. Target ~400–600 lines.
A draft 180-line version was provided in the previous session but was deemed too short. The next session must produce the full, production-ready replacement.

E. AbyssDragonBoss.java — NEEDS ATTACK METHOD UPDATES
Current file: src/main/java/com/jglims/plugin/abyss/AbyssDragonBoss.java (23,877 bytes) What works: Boss fight flow (manual trigger, arena detection, emergency platform, boss bar, lightning, combat loop, movement loop, phase changes, 8 attack types, death handling, loot drops, exit portal). All attack DAMAGE LOGIC is functional.

What needs to change:

Attack methods (voidBreath, lightningStrike, wingGust, groundPound, voidPull, summonMinions, enrageLightningBarrage) currently do NOT trigger model animations — they just do damage/particles. Each must now call the appropriate model.playXxxAnimation(callback) method to synchronize visuals with mechanics.
The movement loop must call model.playFlyAnimation() or model.playWalkAnimation() depending on whether the dragon is airborne or ground-level.
onDragonDeath() must call model.playDeathAnimation(afterDeath) where afterDeath contains the loot/fireworks/portal/cleanup logic.
Phase change handler must call model.onPhaseChange(newPhase).
The damage handler must call model.damageFeedback().
The startFight() method must create the model with model.spawn(location) which triggers the spawn animation automatically.
The integration pattern is: model animations are VISUAL ONLY and run in parallel with the game-logic timers. The boss code starts a BukkitRunnable for damage/particles, and separately triggers the model animation. The animation callback (when the animation finishes) transitions back to idle/fly.

F. BetterModel API Quick Reference (for code generation)
Maven artifact: io.github.toxicity188:bettermodel-bukkit-api:2.1.0 Package: kr.toxicity.model.api

Copy// Get model renderer
Optional<ModelRenderer> renderer = BetterModel.model("abyss_dragon");

// Create or get tracker attached to a living entity
EntityTracker tracker = renderer.get().getOrCreate(BaseEntity.of(zombieEntity));

// Play animations
tracker.animate("idle");                                              // default loop
tracker.animate("attack", AnimationModifier.DEFAULT_WITH_PLAY_ONCE);  // play once
tracker.animate("death", modifier, () -> { /* on complete */ });      // with callback

// Stop / replace
tracker.stopAnimation("walk");
tracker.replace("idle", "fly", AnimationModifier.DEFAULT);

// AnimationModifier builder
AnimationModifier mod = AnimationModifier.builder()
    .start(10)              // lerp-in ticks
    .end(5)                 // lerp-out ticks
    .speed(1.5F)            // playback speed
    .type(AnimationIterator.Type.LOOP)       // LOOP, PLAY_ONCE, HOLD
    .override(true)         // override other animations on same bones
    .build();

// Visual effects
tracker.damageTint();                                        // red flash on hit
tracker.damageTintValue(0xFF0000);                          // custom tint color
tracker.update(TrackerUpdateAction.glow(true));             // outline glow
tracker.update(TrackerUpdateAction.glowColor(0x8800AA));    // glow color
tracker.update(TrackerUpdateAction.tint(0xFF0000));         // model tint
tracker.update(TrackerUpdateAction.brightness(15, 15));     // full bright
tracker.update(TrackerUpdateAction.composite(               // combine multiple
    TrackerUpdateAction.glow(true),
    TrackerUpdateAction.glowColor(0xFF0000),
    TrackerUpdateAction.brightness(15, 15)
));

// Lifecycle
tracker.close();          // remove model + despawn packets
tracker.despawn();        // despawn without closing
tracker.isClosed();       // check state
tracker.handleCloseEvent((t, reason) -> { ... });

// Movement — EntityTracker follows the entity automatically,
// so just teleport the zombie: hitboxEntity.teleport(targetLocation);
Copy
G. Deployment Checklist (after code is done)
Rename abyss_dragon.bbmodel if needed (filename = model name in API)
scp abyss_dragon.bbmodel ubuntu@144.22.198.184:/tmp/
docker cp /tmp/abyss_dragon.bbmodel mc-crossplay:/data/plugins/BetterModel/models/
In-game: /bettermodel reload
Copy generated textures from plugins/BetterModel/build/assets/bettermodel/ into local merged resource pack
Re-zip with 7-Zip, compute SHA-1, SCP to nginx, update docker-compose.yml, recreate container
Build plugin: .\gradlew clean build
Deploy JAR via SCP + docker cp
Restart container
In-game: trigger boss fight, verify all 7 animations, verify damage sync, verify phase transitions, verify death sequence
H. Files the Next Session Must Produce
Working PowerShell injection script (or alternative method) that replaces the .bbmodel animations with long, fluid, multi-bone, catmullrom/smooth animations — must NOT hang in terminal
Complete AbyssDragonModel.java (~400–600 lines) — full BetterModel tracker wrapper with all public methods, animation presets, phase effects, particle helpers, movement, cleanup, logging, null-safety
Updated AbyssDragonBoss.java — all attack methods wired to model animations, movement loop calls fly/walk animations, death calls death animation with callback, phase changes call visual effects, damage calls damageTint
END OF CONTINUATION SUMMARY v17.1 — Paste this at the start of the next chat session.














# ═══════════════════════════════════════════════════════════════════════════════
# JGLIMSPLUGIN v2.0.0 — FINAL PRODUCTION MASTER PROMPT
# Complete audit, vampire system, item integration, performance, deployment
# ═══════════════════════════════════════════════════════════════════════════════

# IDENTITY & MISSION
You are a senior Minecraft plugin architect with 15 years of Java, PaperMC API,
resource pack engineering, and server optimization experience. Your mission is to
bring JGlimsPlugin to 100% production-ready status in a SINGLE SESSION. You will
audit every system, fix every bug, create every missing feature, integrate every
texture, optimize every hotpath, and output a deployment-ready plugin + resource
pack. You plan first, execute in batches, minimize redundant file reads, and
never ask questions.

# ═══════════════════════════════════════════════════════════════════════════════
# PROJECT CONTEXT
# ═══════════════════════════════════════════════════════════════════════════════

## Repository & Paths
- Local source: C:\Users\jgmel\Documents\projects\JGlimsPlugin\JGlimsPlugin\
- GitHub: https://github.com/JGlims/JGlimsPlugin (READ for full context)
- Build command: cd C:\Users\jgmel\Documents\projects\JGlimsPlugin\JGlimsPlugin && .\gradlew clean build
- Output JAR: build\libs\JGlimsPlugin-2.0.0.jar
- BBModels staged: C:\Users\jgmel\Documents\projects\JGlimsPlugin\bbmodels\
- BBModels deploy ready: C:\Users\jgmel\Documents\projects\JGlimsPlugin\JGlimsPlugin\bbmodels_deploy\
- Resource pack workdir: C:\Users\jgmel\Documents\projects\JGlimsPlugin\resourcepack-work\JGlimsResourcePack\
- Merged pack (THIS is what ships): C:\Users\jgmel\Documents\projects\JGlimsPlugin\JGlimsPlugin\merged_pack\
- Texture PNGs location: merged_pack\assets\minecraft\textures\item\ (ALREADY PLACED THERE)

## Server Infrastructure
- Oracle Cloud ARM64 free-tier VM, IP: 144.22.198.184
- SSH key: C:\Users\jgmel\Documents\projects\server_minecraft\ssh-key-2026-02-25.key
- SSH user: ubuntu
- SSH command template: ssh -i "C:\Users\jgmel\Documents\projects\server_minecraft\ssh-key-2026-02-25.key" ubuntu@144.22.198.184
- SCP command template: scp -i "C:\Users\jgmel\Documents\projects\server_minecraft\ssh-key-2026-02-25.key" <local_file> ubuntu@144.22.198.184:<remote_path>
- Docker container: mc-crossplay (itzg/minecraft-server)
- Ports: 25565 (Java), 19132 (Bedrock/Geyser), 8080 (nginx resource pack), 22 (SSH)
- Server data volume: /home/ubuntu/minecraft-server/data
- Plugin path on server: /home/ubuntu/minecraft-server/data/plugins/
- BetterModel models on server: /home/ubuntu/minecraft-server/data/plugins/BetterModel/models/
- Resource pack served at: http://144.22.198.184:8080/JGlimsResourcePack.zip
- Resource pack upload path: /var/www/resourcepack/JGlimsResourcePack.zip
- docker-compose location: /home/ubuntu/minecraft-server/docker-compose.yml
- Resource pack SHA1 is stored in docker-compose.yml as RESOURCE_PACK_SHA1

## Tech Stack
- Java 21, PaperMC 1.21.4, Gradle
- BetterModel 2.1.0 API (compileOnly 'io.github.toxicity188:bettermodel-bukkit-api:2.1.0')
- Adventure API (net.kyori) for text components
- pack_format: 75

## Exclusion List (PERMANENTLY SKIP these mobs — no models, no code, no animations):
low_poly_troll, necromancer, demon_mob, prismamorpha, observer_golem,
wildfire, pegasus, mutant_zombie, stone_golem, general_piglin

# ═══════════════════════════════════════════════════════════════════════════════
# PHASE 0: DEEP PROJECT AUDIT (Read & Understand Everything)
# ═══════════════════════════════════════════════════════════════════════════════

Before writing ANY code, perform a complete audit. Read each file ONCE.

## 0A: Read the GitHub README.md completely
- Understand every feature, every system, every mob, every weapon, every armor set
- Understand the full game design vision

## 0B: Read RESOURCE_PACK_ADDITIONS.md
- Cross-reference the 72 mob model list against bbmodels_deploy/ and bbmodels/
- Note which mobs have .bbmodel files and which are missing

## 0C: Read BETTERMODEL_SETUP_GUIDE.txt
- Understand how BetterModel loads models, the expected YAML config, reload commands

## 0D: Read ALL source files (one pass each, in this order):
1. JGlimsPlugin.java — understand manager registration, command routing, scheduler setup
2. vampire/ package: VampireLevel.java, VampireState.java, VampireManager.java, VampireListener.java
3. magic/ package: MagicItemManager.java, MagicItemListener.java
4. custommobs/ package: CustomMobType.java, CustomMobEntity.java, CustomBossEntity.java,
   CustomMobFactory.java, CustomMobListener.java, CustomMobManager.java,
   CustomMobSpawnManager.java, CustomNpcEntity.java, CustomWorldBoss.java,
   MobCategory.java, RideableMobEntity.java
5. crafting/ package: RecipeManager.java, VanillaRecipeRemover.java
6. structures/ package: StructureType.java, StructureManager.java, StructureBuilder.java,
   OverworldStructureBuilders.java, DimensionStructureBuilders.java,
   StructureBossManager.java, StructureLootPopulator.java
7. legendary/ package: all files (LegendaryWeapon, LegendaryWeaponManager, LegendaryArmorSet,
   LegendaryArmorManager, LegendaryAbilityListener, LegendaryLootListener,
   InfinityStoneManager, InfinityGauntletManager)
8. dimensions/ package: DimensionPortalManager, AetherDimensionManager,
   LunarDimensionManager, JurassicDimensionManager
9. abyss/ package: AbyssDimensionManager, AbyssDragonBoss
10. powerups/: PowerUpManager, PowerUpListener
11. blessings/: BlessingManager, BlessingListener
12. enchantments/: CustomEnchantManager, EnchantmentEffectListener, AnvilRecipeListener
13. guilds/: GuildManager, GuildListener
14. quests/: QuestManager, QuestProgressListener, NpcWizardManager
15. events/: EventManager and all event classes
16. mobs/: BloodMoonManager, RoamingBossManager, BossMasteryManager, etc.
17. weapons/: all Battle*Manager, SickleManager, SpearManager, etc.
18. menu/: CreativeMenuManager, GuideBookManager
19. utility/: all utility listeners and tasks

## 0E: Scan the merged_pack folder
- List all files in merged_pack/assets/minecraft/textures/item/
- List all files in merged_pack/assets/minecraft/models/item/
- List all files in merged_pack/assets/minecraft/items/
- Check pack.mcmeta exists with pack_format 75

## 0F: Scan bbmodels_deploy/ and bbmodels/
- List all .bbmodel files that exist
- Compare against CustomMobType.java enum entries
- Note mismatches (mobs that reference models that don't exist)

## 0G: Create AUDIT_REPORT.md at project root with:
- Total Java source files and line count
- Every custom mob: name, category, has .bbmodel (Y/N), has animations (Y/N), model name matches code (Y/N)
- Every custom item: name, base material, CMD value, has texture PNG (Y/N), has model JSON (Y/N), has item JSON entry (Y/N), has functional use (Y/N), what the use is
- Every crafting recipe: name, ingredients, output, registered (Y/N)
- Every structure: name, associated boss, quality rating (stub/basic/good/excellent based on op count and complexity)
- Every system: name, has manager (Y/N), has listener (Y/N), registered in main class (Y/N), potential bugs spotted
- Resource pack: total PNGs, total model JSONs, total item JSONs, orphaned files, missing references
- Performance concerns spotted during read

# ═══════════════════════════════════════════════════════════════════════════════
# PHASE 1: VAMPIRE ABILITY SYSTEM (NEW — the crown jewel feature)
# ═══════════════════════════════════════════════════════════════════════════════

## Context: Ability Design Philosophy
Vampires trade ALL armor and potions for raw power. A max Dracula at night must be
SLIGHTLY stronger than a full Abyssal legendary + all power-ups + Infinity Gauntlet player.
The tradeoff is fragility: no armor, no potions, sun damage, must hunt blood.

Vampires are COOL and POWERFUL. Abilities must feel epic, with dramatic particles,
screen-shaking sounds, and meaningful tactical depth.

## IMPORTANT: Maximum 5 ability hotbar slots
The current VampireState.java defines 9 ability IDs. This is too many for the hotbar.
REDESIGN the ability system as follows:

### 5 Core Ability Slots (evolve with vampire level):

SLOT 1 — VAMPIRE CLAW (always available, evolves with level)
- This is the vampire's BASIC ATTACK replacing the bare hand. It is NOT optional.
- FLEDGLING: simple claw slash, 3-block range, 120° arc, damage = getEffectiveClawDamage(), 1.5s CD
- VAMPIRE: adds lifesteal (heal 15% of damage), claw leaves red particle trail
- ELDER VAMPIRE: claw becomes 3-hit combo (rapid 3 slashes in 0.5s), 180° arc, lifesteal 25%
- VAMPIRE LORD: claw creates a shockwave that travels 6 blocks forward dealing 60% damage to anything in path
- DRACULA: claw hits phase through blocks, 5-block range, 270° arc, lifesteal 40%, every 5th hit triggers a blood explosion (3-block AoE) dealing extra damage

SLOT 2 — VAMPIRE BITE (unlocked at VAMPIRE, evolves)
- VAMPIRE: Lunge 3 blocks + bite, damage = getEffectiveTeethDamage(), heals 50%, 5s CD
- ELDER VAMPIRE: Lunge 5 blocks, bite chains to 2 nearby enemies, heals 60%, 4s CD
- VAMPIRE LORD: Bite marks target for 8s (25% extra damage from you), chain to 3 enemies, heal 70%
- DRACULA: Bite EXECUTES targets below 15% HP instantly, chain to ALL enemies in 4-block radius, full HP heal on kill, 3s CD

SLOT 3 — BAT SWARM / BAT TRANSFORM (unlocked at ELDER VAMPIRE, evolves)
- ELDER VAMPIRE: Summon 8 bats that attack nearest enemy 6s, each deals 3 dmg, 15s CD
- VAMPIRE LORD: Bats + you become invisible with SPEED 3 for 8 seconds, 20s CD
- DRACULA: Full bat transformation — you dissolve into 12 bats, become invulnerable for 5s, teleport to cursor location (up to 40 blocks), reform dealing 20 AoE damage at arrival, 25s CD

SLOT 4 — BLOOD NOVA (unlocked at VAMPIRE LORD, evolves)
- VAMPIRE LORD: 6-block radius AoE, 15 + (blood×0.1) damage, heal 25% of total dealt, 25s CD, red dust sphere
- DRACULA: 10-block radius, 25 + (blood×0.15) + (evolvers×1.5) damage, heal 40%, applies Wither II to survivors for 5s, pulls enemies toward center before detonation, 20s CD

SLOT 5 — DOMAIN OF NIGHT (DRACULA only — ultimate ability)
- Creates 30-block radius dome: forced night for 30s, non-vampires get Darkness + Slowness II
- Vampire inside gets STRENGTH 3, SPEED 3, REGENERATION 3, RESISTANCE 1
- All vampire abilities have halved cooldowns inside the domain
- Visual: dark particle dome border, red lightning strikes inside, ambient heartbeat sound
- 120s CD

### Update VampireState.java:
- Change recalculateAbilities() to only track: vampire_claw (always), vampire_bite (VAMPIRE+), bat_ability (ELDER+), blood_nova (LORD+), domain_of_night (DRACULA)
- Add ability tier tracking (so the system knows which evolution of each ability to use)
- Buff Dracula stats: clawDamage 22→25, teethDamage 28→32, bloodShootDamage 35→40, baseDefense 10→12

### Update VampireLevel.java:
- Update DRACULA enum values to match the buffed stats above

### Create VampireAbilityManager.java (~400-500 lines):
- Manages the 5-slot ability system
- Ability items use these materials with CMD:
  * vampire_claw: IRON_SWORD, CMD 50001
  * vampire_bite: BONE, CMD 50002
  * bat_ability: PHANTOM_MEMBRANE, CMD 50003
  * blood_nova: REDSTONE_BLOCK, CMD 50004
  * domain_of_night: BLACK_DYE, CMD 50005
- Ability GUI: Sneak + F (swap hand) opens a 3-row chest GUI
  * Row 1: 5 ability slots showing unlocked abilities (custom items with textures)
  * Row 2: vampire stats display (claw dmg, teeth dmg, level, blood count, etc.)
  * Row 3: locked abilities as GRAY_STAINED_GLASS_PANE with "Unlocked at [LEVEL]" lore
- Clicking an unlocked ability equips it in the player's offhand
- Right-clicking with an ability item in offhand triggers the ability
- Each ability: particles, sounds, cooldown, damage, healing, everything implemented
- Cooldown feedback via action bar

### Create VampireAbilityListener.java (~250-350 lines):
- PlayerSwapHandItemsEvent while sneaking → open GUI
- PlayerInteractEvent RIGHT_CLICK with ability item in offhand → execute ability
- InventoryClickEvent in ability GUI → equip ability
- EntityDamageByEntityEvent → apply vampire_bite mark bonus damage
- All cooldown tracking with HashMap<UUID, Map<String, Long>>

### Update VampireListener.java:
- In applyNightBuffs() for DRACULA: STRENGTH 3, RESISTANCE 1, REGENERATION 2
- If hasVampireRing: additional HASTE 2

### Register in JGlimsPlugin.java:
- Add VampireAbilityManager vampireAbilityManager field + getter
- Instantiate after vampireManager
- Register VampireAbilityListener

# ═══════════════════════════════════════════════════════════════════════════════
# PHASE 2: ITEM TEXTURE INTEGRATION & RESOURCE PACK
# ═══════════════════════════════════════════════════════════════════════════════

## Texture PNGs Already in merged_pack/assets/minecraft/textures/item/:
(the user has placed these — verify they exist, if any are missing log it)

### Ability icons (5 used, others available as alternates):
vampire_claw.png, vampire_teeth.png, vampire_teeth_alt.png, bat_swarm.png,
bat_transform.png, blood_nova.png, crimson_mark.png, dracula_wrath.png,
domain_of_night.png, vampire_symbol.png

### Vampire consumable items:
vampire_blood.png, vampire_essence.png, vampire_evolver.png, super_blood.png, vampire_ring.png

### Wand of Wands:
wand_of_wands.png

### Mob drop items:
troll_hide.png, lunar_fragment.png, lunar_beast_hide.png, shark_tooth.png,
bear_claw.png, raptor_claw.png, basilisk_fang.png, dark_essence.png,
void_essence.png, soul_fragment.png, tremor_scale.png, horn.png, spinosaurus_sail.png

## For EVERY texture PNG that exists, create:

### A) Model JSON at merged_pack/assets/minecraft/models/item/<name>.json:
{
  "parent": "minecraft:item/generated",
  "textures": { "layer0": "minecraft:item/<name>" }
}

### B) Base material item JSON at merged_pack/assets/minecraft/items/<base_material>.json:
Use the 1.21.4+ select model format with CustomModelData cases.
If the base material JSON already exists (e.g., diamond_sword.json), ADD the new case
to the existing cases array. Do NOT overwrite existing entries.
Format:
{
  "model": {
    "type": "minecraft:select",
    "property": "minecraft:custom_model_data",
    "fallback": { "type": "minecraft:model", "model": "minecraft:item/<vanilla_model>" },
    "cases": [
      { "when": "<CMD_NUMBER>", "model": { "type": "minecraft:model", "model": "minecraft:item/<custom_name>" } }
    ]
  }
}

### CMD Allocation Table (follow exactly):
| Item | Base Material | CMD | Texture File |
|------|--------------|-----|-------------|
| Wand of Wands | blaze_rod | 40010 | wand_of_wands.png |
| Vampire Blood | redstone | 40001 | vampire_blood.png |
| Vampire Essence | nether_star | 40002 | vampire_essence.png |
| Vampire Evolver | echo_shard | 40003 | vampire_evolver.png |
| Super Blood | magma_cream | 40004 | super_blood.png |
| Vampire Ring | gold_nugget | 40005 | vampire_ring.png |
| Vampire Claw ability | iron_sword | 50001 | vampire_claw.png |
| Vampire Bite ability | bone | 50002 | vampire_teeth.png |
| Bat Ability | phantom_membrane | 50003 | bat_swarm.png |
| Blood Nova ability | redstone_block | 50004 | blood_nova.png |
| Domain of Night ability | black_dye | 50005 | domain_of_night.png |
| Troll Hide | leather | 41001 | troll_hide.png |
| Lunar Fragment | iron_nugget | 41002 | lunar_fragment.png |
| Lunar Beast Hide | leather | 41003 | lunar_beast_hide.png |
| Shark Tooth | flint | 41004 | shark_tooth.png |
| Bear Claw | bone | 41005 | bear_claw.png |
| Raptor Claw | flint | 41006 | raptor_claw.png |
| Basilisk Fang | bone | 41007 | basilisk_fang.png |
| Dark Essence | ender_pearl | 41008 | dark_essence.png |
| Void Essence | ender_pearl | 41009 | void_essence.png |
| Soul Fragment | lapis_lazuli | 41010 | soul_fragment.png |
| Tremor Scale | iron_nugget | 41011 | tremor_scale.png |
| Horn | bone | 41012 | horn.png |
| Spinosaurus Sail | leather | 41013 | spinosaurus_sail.png |

NOTE: For base materials that share multiple CMDs (e.g., bone has 41005, 41007, 41012,
and 50002), merge ALL cases into ONE item JSON with multiple case entries.

## Update VampireManager.java:
- Add meta.setCustomModelData() calls to: createVampireEssence (40002), createVampireEvolver (40003), createSuperBlood (40004), createVampireRing (40005)
- Do NOT change any other functionality

# ═══════════════════════════════════════════════════════════════════════════════
# PHASE 3: MOB DROP ITEM SYSTEM
# ═══════════════════════════════════════════════════════════════════════════════

## Create DropItemManager.java in com.jglims.plugin.custommobs:
- createTrollHide(), createLunarFragment(), createLunarBeastHide(), createSharkTooth(),
  createBearClaw(), createRaptorClaw(), createBasiliskFang(), createDarkEssence(),
  createVoidEssence(), createSoulFragment(), createTremorScale(), createHorn(),
  createSpinosaurusSail()
- Each method: correct Material, display name (colored), lore describing what it's from
  and what it crafts into, PDC tag "drop_item" with the item ID, CustomModelData per table above
- Register in JGlimsPlugin.java

## Update CustomMobListener.java:
- Replace any hardcoded vanilla item drops with DropItemManager calls
- Ensure every mob that should drop a custom item actually does
- Match drops to the RESOURCE_PACK_ADDITIONS.md table

# ═══════════════════════════════════════════════════════════════════════════════
# PHASE 4: NEW CRAFTED ITEMS WITH FUNCTIONAL ABILITIES
# ═══════════════════════════════════════════════════════════════════════════════

## Create CraftedItemManager.java in com.jglims.plugin.crafting:
Create methods for each item below. Every item MUST have a functional use.

1. Blood Chalice (BOWL, CMD 42001) — stores 5 Vampire Blood charges, right-click to consume one
2. Fang Dagger (IRON_SWORD, CMD 42002) — 12 dmg, Poison II 3s on hit, unbreakable
3. Troll Leather Armor set (LEATHER_*, CMD 42003-42006) — +4 toughness/piece, Thorns I
4. Lunar Blade (DIAMOND_SWORD, CMD 42007) — 14 dmg, Smite V, glows at night
5. Raptor Gauntlet (IRON_SWORD, CMD 42008) — 10 dmg, +50% attack speed, right-click pounce dash
6. Shark Tooth Necklace (GOLD_NUGGET, CMD 42009) — offhand, Water Breathing + Dolphin's Grace
7. Tremor Shield (SHIELD, CMD 42010) — 50% projectile reflect, shockwave on block
8. Void Scepter (BLAZE_ROD, CMD 42011) — right-click 15-block teleport through walls, 8s CD
9. Soul Lantern (LANTERN, CMD 42012) — offhand, Slowness II + Weakness I to hostiles in 8 blocks
10. Dinosaur Bone Bow (BOW, CMD 42013) — +8 arrow damage, no-gravity arrows
11. Crimson Elixir (POTION, CMD 42014) — vampire-only, +2 permanent claw damage (max 3 uses)
12. Nightwalker Cloak (LEATHER_CHESTPLATE, CMD 42015) — only chestplate vampires can wear, Invisibility + Speed 2 at night

## Create CraftedItemListener.java in com.jglims.plugin.crafting:
- Void Scepter: PlayerInteractEvent right-click → raycast 15 blocks, teleport, 8s CD, ender particles
- Blood Chalice: right-click → consume 1 charge (stored in PDC), same effect as consuming Vampire Blood
- Raptor Gauntlet: right-click → lunge 6 blocks forward, deal damage on contact, 4s CD
- Soul Lantern: repeating task checking offhand for soul lantern, apply debuffs to nearby hostiles
- Nightwalker Cloak: repeating task, if equipped + vampire + night → Invisibility, Speed
- Fang Dagger: EntityDamageByEntityEvent → apply Poison II for 3s
- Tremor Shield: EntityDamageByEntityEvent when blocking → shockwave, projectile reflect
- Dinosaur Bone Bow: ProjectileLaunchEvent → setGravity(false), add +8 damage on hit
- Crimson Elixir: PlayerInteractEvent → check vampire, check PDC uses counter ≤3, buff claw damage permanently

## Add 12 shaped/shapeless recipes to RecipeManager.java
(ingredients from Phase 3 drop items + vanilla materials, as specified in the item list above)

## Register CraftedItemManager and CraftedItemListener in JGlimsPlugin.java

## Generate textures for the 12 crafted items:
You CANNOT generate images. Instead, for each item that lacks a texture PNG,
create a placeholder model JSON that uses the vanilla base material texture as fallback.
Log which textures the user still needs to generate. DO NOT block on missing textures.

# ═══════════════════════════════════════════════════════════════════════════════
# PHASE 5: FULL SYSTEM VERIFICATION & BUG FIXING
# ═══════════════════════════════════════════════════════════════════════════════

## 5A: Custom Mob Verification
For EVERY entry in CustomMobType.java:
- Verify the model name matches an existing .bbmodel file in bbmodels_deploy/
- Verify the mob has proper HP, damage, category, spawn rules
- Verify the mob drops the correct custom drop item (not a raw vanilla item)
- Fix any mob whose model name doesn't match its .bbmodel filename
- Skip excluded mobs (the 10 in the exclusion list)

## 5B: Custom Item Verification
For EVERY custom item in the project:
- Verify it has a create method
- Verify it has a CustomModelData value
- Verify the CMD matches a resource pack model JSON
- Verify it has a functional use (crafting ingredient, consumable, weapon, armor, ability)
- If any item is "decoration only" with no use, ADD a use (crafting recipe, special effect, etc.)
- Print the complete item verification table

## 5C: Resource Pack Integrity Check
- Verify every model JSON references a texture that exists
- Verify every item JSON references a model JSON that exists
- Verify no duplicate CMD values across different items
- Verify pack.mcmeta has pack_format: 75
- Remove any orphaned files (JSONs pointing to missing textures, backup files like .bak)

## 5D: Structure Quality Audit
- For every structure in StructureType.java, read its builder method
- Rate each: STUB (<15 ops), BASIC (15-30), GOOD (30-60), EXCELLENT (60+)
- For any rated STUB: add at minimum 15 more block-placement operations with
  interesting geometry (columns, arches, floor patterns, loot chests, decorative blocks)
- For any rated BASIC: add at minimum 10 more ops (elevation changes, rubble, vegetation, lighting)
- PRIORITIZE: boss arenas, dimension-specific structures, quest locations
- The 11 weak structures from the previous session audit (MeteorCrashSite, AetherAncientRuins,
  DruidsGrove, AllaySanctuary, SunkenRuins, Volcano, UndergroundHive, NestingGround, etc.)
  — improve ALL of them

## 5E: Animation Verification
- Check that bbmodels_deploy/MANIFEST.txt exists and lists all animated models
- Cross-reference with CustomMobType model names
- For models with <3 bones or 0 animations, log them as "needs Blockbench work"
- shark.bbmodel: if un-rigged (0 bones), log it

## 5F: Compilation & Runtime Bug Detection
- Read every Java file looking for:
  * NullPointerException risks (unchecked .get() calls, missing null checks on player.getInventory())
  * Missing event registrations in JGlimsPlugin.java
  * Managers that are instantiated but never used
  * Listeners that reference managers not yet initialized (order-of-operations bugs)
  * Deprecated PaperMC API calls
  * Resource leaks (unclosed streams, schedulers not cancelled in onDisable)
  * ConcurrentModificationException risks in collections iterated during events
  * Hardcoded paths or values that should be configurable
- FIX every bug found

# ═══════════════════════════════════════════════════════════════════════════════
# PHASE 6: PERFORMANCE OPTIMIZATION
# ═══════════════════════════════════════════════════════════════════════════════

This server runs on a FREE-TIER ARM64 VM with limited CPU/RAM.
Every tick matters. Optimize for high FPS and zero lag spikes.

## 6A: Scheduler Optimization
- Audit every BukkitRunnable and runTaskTimer call
- Any task running every tick (1L) that doesn't need to: increase to 5L or 10L
- VampireListener day/night cycle runs every 40L (2s) — this is fine
- Custom mob spawn scheduler: ensure it's not checking all chunks every run
- Particle-heavy abilities: ensure particle counts are reasonable (not 200+ per tick)

## 6B: Event Listener Optimization
- Any EventHandler with priority HIGH that just checks a boolean and returns:
  add early-return checks at the TOP before any computation
- Inventory click events: return immediately if not the correct inventory
- Entity damage events: cache lookups, avoid repeated getCustomMobManager() chains

## 6C: Collection Optimization
- Any HashMap that grows unbounded per-player: add cleanup in onDisable or scheduled cleanup
- Cooldown maps: add periodic purge of expired entries
- Vampire states: already persisted to disk, good

## 6D: Structure Generation Optimization
- Large structure builders (164KB DimensionStructureBuilders!): ensure they use
  setBlockData instead of setType where possible, batch operations, avoid unnecessary
  getBlock calls, use chunk snapshots if reading large areas

## 6E: Memory Optimization
- Large lists of legendary weapons/armor: ensure they're static final, not re-created per call
- ItemStack creation: cache common items if created frequently (Vampire Blood drops)
- String concatenations in hot loops: use StringBuilder

# ═══════════════════════════════════════════════════════════════════════════════
# PHASE 7: FINAL BUILD, PACKAGE & DEPLOYMENT PREP
# ═══════════════════════════════════════════════════════════════════════════════

## 7A: Build the Plugin
cd C:\Users\jgmel\Documents\projects\JGlimsPlugin\JGlimsPlugin && .\gradlew clean build
- If build fails: READ the error, FIX it, rebuild. Repeat until SUCCESS.
- Report final JAR size

## 7B: Package the Resource Pack
- Ensure merged_pack/ contains: pack.mcmeta, pack.png (if exists), assets/ tree
- Create the ZIP: use PowerShell to 7z or Compress-Archive merged_pack/* into JGlimsResourcePack.zip
- Compute SHA1 of the ZIP
- Place ZIP at project root

## 7C: Generate Deployment Script
Create a file deploy.ps1 at project root that:
1. Copies JGlimsPlugin-2.0.0.jar to server via SCP
2. Copies all .bbmodel files from bbmodels_deploy/ to server's BetterModel/models/
3. Copies JGlimsResourcePack.zip to server's /var/www/resourcepack/
4. SSHes in and updates RESOURCE_PACK_SHA1 in docker-compose.yml
5. Restarts the Docker container: docker compose down && docker compose up -d
6. Waits 30 seconds then runs: docker exec mc-crossplay rcon-cli "bettermodel reload"

Use the SSH key path: C:\Users\jgmel\Documents\projects\server_minecraft\ssh-key-2026-02-25.key

## 7D: Generate the Final Status Report
Create DEPLOYMENT_STATUS.md at project root with:

### Server Readiness: YES/NO
### What Works:
- Complete list of functional systems with status
### What Needs Manual Attention:
- Missing .bbmodel files (list them)
- Missing textures for crafted items (list which ones the user needs to generate)
- Any mob animations that need Blockbench visual tweaking
### How to Make Textures Work In-Game:
Step-by-step instructions for the user:
1. Generate remaining textures with ChatGPT (give the prompts)
2. Place PNGs in merged_pack/assets/minecraft/textures/item/
3. Run deploy.ps1
4. Join server, download resource pack
5. Use /jglims vampire to test items
### How to Make Models Work In-Game:
1. .bbmodel files go to plugins/BetterModel/models/ on the server
2. /bettermodel reload
3. Spawn a mob with /jglims mob spawn <type> and verify model renders
### Performance Expectations:
- Expected TPS on ARM64 free tier
- Recommended player count
- Known performance bottlenecks and mitigations

# ═══════════════════════════════════════════════════════════════════════════════
# EXECUTION RULES (FOLLOW STRICTLY)
# ═══════════════════════════════════════════════════════════════════════════════

1. PLAN FIRST: After Phase 0 audit, create a numbered task list before writing code
2. BATCH READS: Read files once, take notes, never re-read the same file
3. BATCH WRITES: Accumulate changes per file, write once
4. NEVER ASK QUESTIONS: If something is ambiguous, make the best engineering decision
5. FIX FORWARD: If you encounter a bug while working on something else, fix it immediately
6. LOG EVERYTHING: Every file created, modified, or deleted goes in the final report
7. COMPILE OFTEN: Build after each major phase to catch errors early
8. DO NOT SKIP PHASES: Execute every phase in order, even if it seems "good enough"
9. CREDIT EFFICIENCY: Minimize token usage by being precise, not verbose in code comments
10. ABSOLUTE QUALITY: Every line of code must be production-grade. No TODOs, no placeholders, no stubs
11. The previous Claude Code session animated 62 models and rebuilt 6 structures. 
    BUILD ON THAT WORK — do not redo it. Check what exists and extend it.
12. VS Code performance: add to .vscode/settings.json to exclude *.bak, bbmodels/ from search/watcher

# ═══════════════════════════════════════════════════════════════════════════════
# BEGIN EXECUTION. NO QUESTIONS. FULL AUTONOMY. MAKE IT PERFECT.
# ═══════════════════════════════════════════════════════════════════════════════

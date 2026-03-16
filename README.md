JGLIMSPLUGIN — DEFINITIVE PROJECT SUMMARY v10.0
Compiled: 2026-03-13 | Author: JGlims (jg.melo.lima2005@gmail.com) | Plugin Version: 2.0.0 | Target Version: 3.0.0

A. PROJECT METADATA
Repository: https://github.com/JGlims/JGlimsPlugin

Server Environment: Docker container mc-crossplay on Oracle Cloud ARM64 instance. IP 144.22.198.184, Java port 25565, Bedrock port 19132. Paper 1.21.11-127, Java 25 (OpenJDK 25.0.2+10-LTS, Eclipse Adoptium Temurin), GeyserMC 2.9.4-SNAPSHOT + Floodgate 2.2.5-SNAPSHOT, SkinsRestorer 15.11.0, Chunky 1.4.40. Memory: 8GB. Online mode: disabled (crossplay). Game mode: creative. Max players: 15. View distance: 10, Simulation distance: 6.

SSH Access: Key at C:\Users\jgmel\Documents\projects\server_minecraft\ssh-key-2026-02-25.key, user ubuntu@144.22.198.184 (NOT opc — confirmed during this session).

Local Dev Path: C:\Users\jgmel\Documents\projects\JGlimsPlugin\JGlimsPlugin

Resource Pack Working Directory: C:\Users\jgmel\Documents\projects\JGlimsPlugin\resourcepack-work\JGlimsResourcePack

Server Resource Pack Source (original full pack): C:\Users\jgmel\Documents\projects\JGlimsPlugin\JGlimsResourcePack-v2.1-server

Build Output: JGlimsPlugin-2.0.0.jar (711,934 bytes, built successfully 2026-03-13)

Docker Compose File: /home/ubuntu/minecraft-server/docker-compose.yml

Data Volume Mount: /home/ubuntu/minecraft-server/data:/data (bind mount, NOT a Docker volume)

B. CURRENT RESOURCE PACK STATE (CRITICAL — READ THIS CAREFULLY)
What Is Currently Live and Working
The server is running with resource pack v5.0, hosted on GitHub release v5.0. This is the exact same file as the original v4.1.0 pack (SHA-1 a7a0e72c67c052f8710b2957b7193b94dc0c9824, 3,693,680 bytes / 3.52 MB).

Current docker-compose.yml settings:

RESOURCE_PACK: https://github.com/JGlims/JGlimsPlugin/releases/download/v5.0/JGlimsResourcePack-v5.0.zip
RESOURCE_PACK_SHA1: a7a0e72c67c052f8710b2957b7193b94dc0c9824
RESOURCE_PACK_ENFORCE: TRUE
CRITICAL RULE: The resource pack URL and SHA-1 are set in docker-compose.yml as environment variables. The Docker image (itzg/minecraft-server) overwrites server.properties from these env vars on EVERY container start. Editing server.properties directly is useless — you MUST edit docker-compose.yml and run docker compose down && docker compose up -d for changes to persist.

What Works Right Now
All 63 legendary weapon textures (3D models from Fantasy Weapons + Blades of Majestica packs)
All legendary weapon abilities (primary = right-click, alt = crouch + right-click)
Armor set item icons in inventory (13 sets)
Infinity Gauntlet and Thanos Glove textures
Infinity Stone textures
Plugin loads cleanly: JGlimsPlugin 2.0.0, Abyss dimension created, all events loaded
What Does NOT Work
Armor worn textures: Armor shows correct custom icon in inventory, but when equipped on the player character, it shows vanilla netherite texture. Needs assets/minecraft/equipment/*.json files with asset_id matching the equippable component set in LegendaryArmorManager.java, plus worn texture PNGs at assets/minecraft/textures/entity/equipment/humanoid/ and humanoid_leggings/.
Power-up item textures: Heart Crystal, Soul Fragment, Titan's Resolve, Phoenix Feather, KeepInventorer, Vitality Shard, Berserker Mark all show vanilla item textures. The PowerUpManager.java may still use integer CMD instead of string-based CMD, and the item definition JSONs may not have the correct when cases.
Abyss portal: Right-clicking the purpur frame with the Abyssal Key does not open the portal. The AbyssDimensionManager.java portal detection code needs fixing.
Missing animations: Whisperwind, True Excalibur (Awakened), Creation Splitter, Edge of the Astral Plane lack animated textures. Awakened weapons should have animations (the only difference from their base versions).
One Creation Splitter ability bug: One of the abilities has a bug (details to be identified in-game).
Resource Pack Version History
Version	Size	SHA-1	Description
v4.0.0	3.04 MB	63b8e30d...	pack_format 75, initial 63 weapons, item definitions for 1.21.4+
v4.0.2	3.09 MB	626edf29...	Real textures, 3D models
v4.0.3	3.17 MB	6cd61288...	All 15 missing 3D weapon models from v2.1 pack
v4.0.4	3.19 MB	ca60f59f...	Fixed 5 remaining buggy weapons
v4.0.5	3.19 MB	2147a804...	Fixed excalibur + requiem awakened
v4.0.6	3.19 MB	7d03a108...	Added animation mcmeta for excalibur textures
v4.0.7	3.17 MB	c433786c...	Fix Excalibur 3D models: extract 128x128 frame from spritesheet
v4.0.8	3.17 MB	45aeea8a...	Excalibur Awakened: animated fire texture
v4.0.9	3.17 MB	1b9f9e6c...	Excalibur Awakened: majestica model with blue fire + effect-sheet
v4.1.0	3.52 MB	a7a0e72c...	Added armor, infinity, powerup textures + models. Bow models ready. THIS IS THE WORKING PACK
v5.0	3.52 MB	a7a0e72c...	CURRENT — identical copy of v4.1.0, hosted under new version
The 9.5 MB "v4.1.1" file on the v4.1.0 release is BROKEN — it was created by PowerShell's Compress-Archive and has hundreds of extra files that override/break the working definitions. DO NOT USE IT.

How to Update the Resource Pack (for future changes)
Make changes to files in C:\Users\jgmel\Documents\projects\JGlimsPlugin\resourcepack-work\JGlimsResourcePack
DO NOT use PowerShell Compress-Archive — it produces zips with wrong structure. Instead use 7-Zip or the manual zip method that preserves pack.mcmeta at root.
Get the SHA-1: (Get-FileHash -Path $zipPath -Algorithm SHA1).Hash.ToLower()
Upload as a NEW release version (v6.0, v7.0, etc. — never overwrite old versions):
Copygh release create v6.0 $zipPath --repo JGlims/JGlimsPlugin --title "Resource Pack v6.0" --notes "SHA-1: $sha1"
Update docker-compose.yml on the server:
Copyssh -i $sshKey $remoteHost "sudo sed -i 's|v5.0/JGlimsResourcePack-v5.0.zip|v6.0/JGlimsResourcePack-v6.0.zip|' /home/ubuntu/minecraft-server/docker-compose.yml && sudo sed -i 's|OLD_SHA1|NEW_SHA1|' /home/ubuntu/minecraft-server/docker-compose.yml"
Recreate container: ssh -i $sshKey $remoteHost "cd /home/ubuntu/minecraft-server && docker compose down && docker compose up -d"
How Minecraft 1.21.4+ Custom Textures Work
Minecraft 1.21.4 uses item model definitions in assets/minecraft/items/*.json. Each file uses type: "minecraft:select" with property: "minecraft:custom_model_data" to choose which model to render based on the custom_model_data component's strings list. The Java plugin sets the string via CustomModelDataComponent.setStrings(List.of("texture_name")). The "when" value in the JSON must exactly match the string set by the plugin.

For worn armor textures in 1.21.4+, the equippable component must set an asset_id that points to an assets/minecraft/equipment/<name>.json file, which in turn references textures in assets/minecraft/textures/entity/equipment/humanoid/ and humanoid_leggings/.

C. CURRENT CODEBASE (52+ Java files)
File Inventory by Package
Root: JGlimsPlugin.java (34,896 B) — Main class, onEnable(), all listener/manager registration, commands (/guild, /guia, /jglims)

abyss/: AbyssChunkGenerator.java (7,341 B), AbyssDimensionManager.java (12,440 B), AbyssDragonBoss.java (13,844 B)

blessings/: BlessingListener.java (3,452 B), BlessingManager.java (10,296 B)

config/: ConfigManager.java (29,211 B)

crafting/: RecipeManager.java (34,774 B), VanillaRecipeRemover.java (588 B)

enchantments/: AnvilRecipeListener.java (33,648 B), CustomEnchantManager.java (8,853 B), EnchantmentEffectListener.java (69,495 B), EnchantmentType.java (1,942 B), SoulboundListener.java (7,961 B)

events/: EndRiftEvent.java (30,058 B), EventManager.java (10,367 B), NetherStormEvent.java (11,421 B), PiglinUprisingEvent.java (8,956 B), PillagerSiegeEvent.java (19,146 B), PillagerWarPartyEvent.java (19,957 B), VoidCollapseEvent.java (8,961 B)

guilds/: GuildListener.java (1,456 B), GuildManager.java (13,679 B)

legendary/: InfinityGauntletManager.java (23,370 B), InfinityStoneManager.java (8,029 B), LegendaryAbilityContext.java (4,941 B — updated with particle throttle), LegendaryAbilityListener.java (18,032 B), LegendaryAltAbilities.java (73,055 B), LegendaryArmorListener.java (38,729 B), LegendaryArmorManager.java (16,059 B), LegendaryArmorSet.java (16,160 B), LegendaryLootListener.java (22,565 B), LegendaryPrimaryAbilities.java (69,951 B), LegendaryTier.java (3,673 B), LegendaryWeapon.java (15,034 B), LegendaryWeaponManager.java (7,937 B)

menu/: CreativeMenuManager.java (45,503 B), GuideBookManager.java (29,396 B)

mobs/: BiomeMultipliers.java (3,080 B), BloodMoonManager.java (12,329 B), BossEnhancer.java (19,998 B — restored from GitHub, BOM stripped), BossMasteryManager.java (12,294 B), KingMobManager.java (5,972 B), MobDifficultyManager.java (4,744 B), RoamingBossManager.java (57,427 B)

powerups/: PowerUpListener.java (5,607 B), PowerUpManager.java (26,207 B)

quests/: NpcWizardManager.java (15,501 B), QuestManager.java (27,940 B), QuestProgressListener.java (3,231 B)

structures/: StructureBossManager.java (14,554 B), StructureBuilder.java (8,714 B), StructureLootPopulator.java (8,554 B), StructureManager.java (49,819 B), StructureType.java (10,780 B)

utility/: BestBuddiesListener.java (11,609 B), DropRateListener.java, EnchantTransferListener.java, InventorySortListener.java, LootBoosterListener.java, PaleGardenFogTask.java, VillagerTradeListener.java

weapons/: BattleAxeManager.java, BattleBowManager.java, BattleMaceManager.java, BattlePickaxeManager.java, BattleShovelManager.java, BattleSpearManager.java, BattleSwordManager.java, BattleTridentManager.java, SickleManager.java, SpearManager.java, SuperToolManager.java, WeaponAbilityListener.java, WeaponMasteryManager.java

D. LEGENDARY WEAPONS (63 total across 5 tiers)
The LegendaryWeapon.java enum defines all 63 weapons. Each entry has: id, displayName, baseMaterial, baseDamage, customModelData (legacy int), tier (LegendaryTier), textureName (string used for CMD matching), primaryAbilityName, altAbilityName, primaryCooldown, altCooldown. The textureName field is the critical field — it must match the "when" value in the item definition JSON.

Tier Distribution: 20 COMMON, 8 RARE, 7 EPIC, 24 MYTHIC, 4 ABYSSAL

COMMON (20): Amethyst Shuriken, Gravescepter, Lycanbane, Gloomsteel Katana, Viridian Cleaver, Crescent Edge, Gravecleaver, Amethyst Greatblade, Flamberge, Crystal Frostblade, Demonslayer, Vengeance, Oculus, Ancient Greatslab, Neptune's Fang, Tidecaller, Stormfork, Jade Reaper, Vindicator, Spider Fang

RARE (8): Ocean's Rage, Aquatic Sacred Blade, Royal Chakram, Acidic Cleaver, Muramasa, Windreaper, Moonlight, Talonbrand

EPIC (7): Berserker's Greataxe, Black Iron Greatsword, Solstice, Grand Claymore, Calamity Blade, Emerald Greatcleaver, Demon's Blood Blade

MYTHIC (24): True Excalibur, Requiem of the Ninth Abyss, Phoenix's Grace, Soul Collector, Valhakyra, Phantomguard, Zenith, Dragon Sword, Nocturne, Divine Axe Rhitta, Yoru, Tengen's Blade, Edge of the Astral Plane, Fallen God's Spear, Nature Sword, Heavenly Partisan, Soul Devourer, Mjölnir, Thousand Demon Daggers, Star Edge, Rivers of Blood, Dragon Slaying Blade, Stop Sign, Creation Splitter

ABYSSAL (4): Requiem Awakened (dmg 38), Excalibur Awakened (dmg 34), Creation Splitter Awakened (dmg 40), Whisperwind Awakened (dmg 30)

Weapons Missing Models/Textures (9)
neptunes_fang, tidecaller, stormfork (tridents), edge_astral_plane, fallen_gods_spear, star_edge, requiem_awakened, excalibur_awakened, creation_splitter_awakened, whisperwind_awakened

Weapons with Fuzzy Model Name Matches
divine_axe_rhitta → divineaxerhitta, tengens_blade → tengensblade, thousand_demon_daggers → thousanddemondaggers, rivers_of_blood → riversofblood, dragon_slaying_blade → dragonslayingblade, creation_splitter → creationsplitter

E. ARMOR SETS (13 total: 6 craftable + 7 legendary)
Craftable: Reinforced Leather (12 def), Copper Armor (10 def), Chainmail Reinforced (14 def), Amethyst Armor (16 def), Bone Armor (8 def), Sculk Armor (18 def)

Legendary: Shadow Stalker (24 def, RARE), Blood Moon (20 def, EPIC), Nature's Embrace (18 def, EPIC), Frost Warden (22 def, EPIC), Void Walker (20 def, MYTHIC), Dragon Knight (26 def, MYTHIC), Abyssal Plate (30 def, ABYSSAL — +30% damage, Wither immunity)

F. POWER-UP ITEMS (7)
Heart Crystal (+2 HP, max 40 crystals), Soul Fragment (+1% damage, max 100), Titan's Resolve (+10% knockback resistance), Phoenix Feather (auto-revive on death), KeepInventorer (permanent keep-inventory), Vitality Shard (+2 HP, max 20), Berserker Mark (+2% damage, max 10)

G. INFINITY GAUNTLET SYSTEM
Thanos boss (800 HP, 2 phases) drops Thanos Glove. Six Infinity Stone fragments (Power/Space/Reality/Soul/Time/Mind) drop during Blood Moon at 0.1% rate. Fragment + Nether Star in anvil = finished Stone. Thanos Glove + all 6 Stones = Infinity Gauntlet. Right-click snap kills 50% of loaded hostile mobs, 300s cooldown.

H. EVENTS (6 active)
Nether Storm, Piglin Uprising, Void Collapse, Pillager War Party, Pillager Siege, End Rift. All managed by EventManager.java.

I. STRUCTURES (39 types)
25 Overworld, 7 Nether, 5 End, 3 Abyss (counting Abyssal Castle, Void Nexus, Shattered Cathedral). Each has optional mini-boss and tier-appropriate loot.

J. OTHER SYSTEMS
64 custom enchantments, Blessings (C-Bless heal, Ami-Bless damage, La-Bless defense), Guilds, Blood Moon event (15% chance per night), King Mobs (1 per 50 spawns, 10× HP), Mob Difficulty scaling by distance, Boss Mastery Titles, Weapon Mastery (Novice/Fighter/Experienced/Master), NPC Wizard, Quest system (6 quest lines), Creative Menu, Guide Book (PT-BR), 11 battle weapon types, Roaming Bosses (6: The Watcher, Hellfire Drake, Frostbound Colossus, Jungle Predator, End Wraith, Abyssal Leviathan).

K. DEPLOYMENT PROCEDURE
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
# 3. Upload to GitHub as new release (e.g., v6.0)
gh release create v6.0 $zipPath --repo JGlims/JGlimsPlugin --title "Resource Pack v6.0" --notes "SHA-1: $sha1"
# 4. Update docker-compose.yml on server with new URL + SHA-1
# 5. docker compose down && docker compose up -d
CRITICAL RULES FOR FILE WRITING
Always use [System.IO.File]::WriteAllText($path, $content, [System.Text.UTF8Encoding]::new($false)) to avoid BOM
NEVER use PowerShell Compress-Archive for resource packs
ALWAYS edit docker-compose.yml for resource pack URL/SHA-1 changes (NOT server.properties)
ALWAYS use docker compose down && docker compose up -d (not just docker restart)
SSH user is ubuntu, not opc
L. PROBLEMS SOLVED IN THIS SESSION (2026-03-13)
BOM build failure: PowerShell's default UTF-8 BOM corrupted BossEnhancer.java. Fixed by restoring from GitHub and stripping BOM.
Particle throttle for lag reduction: Added spawnThrottled, trackTask, trackSummon, cleanupPlayer methods to LegendaryAbilityContext.java.
Missing Particle import: LegendaryAbilityContext.java was missing the org.bukkit.Particle import after patching. Fixed by rewriting the full file with correct imports.
Resource pack URL pointing to nonexistent file: server.properties pointed to v4.1.0/JGlimsResourcePack-v4.1.0.zip which didn't exist on GitHub. Created v5.0 release with the working pack.
docker-compose.yml overwriting server.properties: Discovered that itzg/minecraft-server Docker image regenerates server.properties from env vars on every start. All resource pack config changes must go in docker-compose.yml.
server.properties AccessDeniedException: docker cp sets file ownership to root, but the container runs as uid 1000. Fixed with sudo chown 1000:1000 on the host bind mount.
sed escaping issues: The \: in Minecraft's properties format was getting eaten by shell layers. Fixed by using sed only on the version/path portion (no special characters).
M. THINGS TO DO NEXT (PRIORITY ORDER)
1. Fix Current Bugs + Power-Up & Armor Worn Textures
Priority: HIGHEST — complete what's broken

A. Creation Splitter ability bug: Identify which ability (primary or alt) is bugged, inspect LegendaryPrimaryAbilities.java and LegendaryAltAbilities.java for the CREATION_SPLITTER case, and fix.

B. Power-up textures: Update PowerUpManager.java to use string-based CustomModelDataComponent (it likely still uses integer CMD). Verify that assets/minecraft/items/amethyst_shard.json (or the correct base material JSON) has when cases matching the strings set by the plugin. Missing PNGs for keep_inventorer.png and berserker_mark.png need to be created (16×16 pixel art).

C. Armor worn textures: In Minecraft 1.21.4+, worn armor uses the equippable component with an asset_id. Create assets/minecraft/equipment/<set_name>.json files for each of the 13 armor sets. Each equipment JSON references textures at assets/minecraft/textures/entity/equipment/humanoid/<set_name>.png and humanoid_leggings/<set_name>.png. Update LegendaryArmorManager.java to set the equippable component with the correct asset_id and slot. Verify all 26 worn texture PNGs (13 sets × 2 layers) exist.

2. Fix the Abyss Portal
Priority: HIGH — core game feature

The AbyssDimensionManager.java portal detection (onPlayerInteract) doesn't activate the portal when right-clicking the purpur frame with the Abyssal Key. Needs investigation: check if the click event fires (add debug logging), verify the portal frame detection algorithm checks adjacent blocks correctly (purpur block recognition, frame shape validation), and ensure the Abyssal Key item is identified correctly via PersistentDataContainer. The portal texture (visual particle effect filling the frame) also needs to be implemented or fixed. The Abyssal Key also needs a unique custom texture (currently uses vanilla Echo Shard appearance).

3. Improve Server Performance
Priority: HIGH — playability

The particle throttle was added to LegendaryAbilityContext.java but more optimization is needed. Investigate TPS during gameplay with /tps. Potential improvements: limit entity counts from abilities (max 6 summoned entities per player), cancel previous BukkitRunnables before starting new ones for the same ability, reduce particle counts per tier, throttle structure generation checks, optimize RoamingBossManager.java (57 KB — may have expensive tick operations), review EnchantmentEffectListener.java (69 KB — likely runs checks every tick).

4. Create World Weapons (New Weapon Category)
Priority: MEDIUM — content expansion

Create a new weapon category called "World Weapons" with the same structure as legendary weapons (same enum pattern, same ability system, same model/texture system). These weapons use the remaining unused textures from Fantasy 3D and Blades of Majestica packs. Damage level sits between COMMON legendaries and MYTHIC legendaries. Same ability type structure (primary + alt). Requires new files: WorldWeapon.java (enum), WorldWeaponManager.java (item creation), WorldWeaponAbilities.java (abilities), WorldWeaponListener.java (event handling). Add item definition JSON cases for each world weapon. Integrate with existing loot tables.

5. Create Magic Items (New Item Category)
Priority: MEDIUM — unique gameplay

A new category of items that work similarly to legendary weapons but are more unique from each other, with custom textures made by JGlims. Requires new dedicated files and folders (magic/ package). Examples:

Wand of Wands: Left-click fires a basic red particle shot (few particles, spammable). Right-click fires a "Vingardium Leviosa" shot (shulker-like homing effect that makes the hit mob float for several seconds). Crouch+right-click fires "Avada Kedavra" — a green beam that instantly kills any non-mini-boss/non-boss mob at 50% HP or below.

New files needed: magic/MagicItem.java (enum), magic/MagicItemManager.java, magic/MagicAbilities.java, magic/MagicItemListener.java.

6. More Structures, Mini-Bosses, and Bosses
Priority: MEDIUM — world content

Add significantly more structures across all dimensions. Add more mini-bosses with unique mechanics. Add world bosses (scheduled massive bosses requiring multiple players). See the existing roadmap Phases 21 (Pillager Warfare), 26 (Ocean Expansion), 27 (World Bosses) for detailed plans already written.

7. More Custom Items and Ideas
Priority: MEDIUM — creativity

Research Minecraft addons, mods, and community ideas for inspiration. Create more interesting custom items with unique mechanics. JGlims creates the textures, then we implement the items. Examples from the roadmap: legendary shields, legendary fishing rods, pets, consumable battle items (War Horn, Grappling Hook, Warp Scroll).

8. Lunar Dimension
Priority: LOW — future expansion

A new dimension with moon-like reduced gravity. Space-themed structures. Some tech-themed textures from Fantasy 3D or Majestica for world weapons found in this area. Portal: Nether portal shape (4×5 frame) built with End Stone blocks. Opened with flint and steel, just like the nether portal. Requires: custom world generator with low gravity simulation (reduced fall speed, higher jump), space-themed structures, unique mobs, loot tables.

9. Aether Dimension
Priority: LOW — future expansion

Adapted from the classic Aether mod. Same portal (water bucket on glowstone frame), same aesthetic (floating sky islands, blue sky, cloud blocks), mobs that can fly, the full Aether experience rebuilt as a server-side plugin. Legendary and world weapons in structure chests, enchanted books, good loot. Requires extensive research on the original Aether mod to faithfully recreate structures, mobs, mechanics, and aesthetics.

N. FULL EXPANSION ROADMAP (from README — preserved)
All the phases from the original README (Phases 20–35) remain planned: End Rift Event, Enhanced Pillager Warfare, Infinity Gauntlet + Thanos, Quest Villagers & NPC Wizard, Abyss Dimension enhancements, Legendary Ranged Weapons (15 bows/crossbows), Ocean Expansion (6 structures + Kraken event), World Bosses (4 Overworld + 3 Nether + 1 End), Dungeon Generator (procedural multi-room dungeons), Legendary Shields & Off-hand Items (16 items), Pet System (8 tameable mythical pets), Seasons & Weather, Fishing Overhaul, Guild Wars & Territories, Hardcore Challenge Modes. Total planned content after all phases: 75+ legendary weapons, 15+ armor sets, 43+ structures, 8+ events, 8 world bosses, 8 pets, procedural dungeon system, full quest system, guild territory/war PvP, 4+ dimensions.

O. MANDATORY DEVELOPMENT RULES
Always use UTF-8 without BOM for Java files: [System.IO.File]::WriteAllText($path, $content, [System.Text.UTF8Encoding]::new($false))
NEVER use Compress-Archive for resource packs — use 7-Zip CLI or manual zip
Resource pack changes go in docker-compose.yml, NOT server.properties
After docker-compose.yml changes: docker compose down && docker compose up -d
SSH user is ubuntu, not opc
Each resource pack update gets a new major version (v6.0, v7.0, v8.0...) — never overwrite
Use Adventure API exclusively (no deprecated ChatColor)
All custom item metadata via PersistentDataContainer with NamespacedKey
String-based CustomModelDataComponent for all item textures
Two abilities per weapon: primary (right-click) and alternate (crouch + right-click)
Particle budgets per tier: COMMON 10, RARE 25, EPIC 50, MYTHIC 100, ABYSSAL 200
All legendary items must be indestructible (Unbreakable: true)
Resource pack pack_format: 75
Item JSON files in assets/minecraft/items/*.json use string-based custom_model_data
File-size limit per source file: ~120 KB; split if exceeding
END OF DEFINITIVE SUMMARY v10.0
(ALL OF THE LEGENDARY WEAPONS HAVE WORKING TEXTURES AND ABILITIES, THATS DONE).






















PLUS SECTION (MORE INFORMATION): 
JGLIMSPLUGIN — PLUS SECTION (Addendum to Project Summary v10.0)
Compiled: 2026-03-13 | Covers all missing detail from the main summary

P1. EXACT CURRENT STATE OF ALL SOURCE FILES (68 Java files + 2 resources)
The repository tree SHA is 9177e26ebe0fffde0b98746b7aca17e9580635be. The plugin.yml declares version 2.0.0 (api-version 1.21), despite the README referencing 1.4.0. The build output is JGlimsPlugin-2.0.0.jar. The full file inventory (with exact sizes from the GitHub API) is:

Root package — com.jglims.plugin: JGlimsPlugin.java (34,896 B) — main class, onEnable() wiring, all command handlers (/jglims, /guild, /guia).

abyss package: AbyssChunkGenerator.java (7,341 B) — custom void-based chunk generation for the Abyss dimension (world_abyss). AbyssDimensionManager.java (12,440 B) — creates and manages world_abyss world with THE_END environment, portal detection (4×5 purpur frame + Abyssal Key interaction), and player teleportation. AbyssDragonBoss.java (13,844 B) — the Abyss Dragon boss fight logic, HP scaling, special attacks (void breath, soul fire rain, summoned minions), and ABYSSAL-tier weapon drops.

blessings package: BlessingListener.java (3,452 B) — listens for blessing crystal use events. BlessingManager.java (10,296 B) — manages C-Bless (heal +1 HP/use, max 10), Ami-Bless (+2% damage/use, max 10), La-Bless (+2% defense/use, max 10); shows stats via /jglims stats.

config package: ConfigManager.java (29,211 B) — reads all config.yml values; provides getters for every tunable parameter. The config.yml (3,851 B, shown above in full) has sections for mob difficulty (distance-based scaling from 350 to 5000 blocks), biome multipliers, boss enhancer (dragon 3.5× HP, wither 1.0×, warden 1.0×, elder guardian 2.0× HP), creeper reduction (50% cancel), pale garden fog, loot booster, blessings, anvil (remove "too expensive" + 50% XP reduction), toggles for 10 features, drop rate booster (35% trident drop), villager trade modifications (50% price reduction, no trade locking), king mob (every 100th mob, 7× HP), axe nerf (0.5 attack speed), weapon mastery (max 1000 kills → +20% bonus), blood moon (15% chance, 12× boss HP), guilds (max 10 members, no friendly fire), dog armor (95% DR), super tools (2% bonus per enchant for netherite), and ore detect radii.

crafting package: RecipeManager.java (34,774 B) — registers all custom recipes: battle tools (sword, axe, bow, mace, shovel, pickaxe, trident, spear), super tools (diamond and netherite), sickles, Infinity Stone combining (colored stone + redstone → finished stone), Infinity Gauntlet (glove + 6 stones), and all craftable armor sets (6 sets × 4 pieces = 24 recipes). VanillaRecipeRemover.java (588 B) — removes conflicting vanilla recipes.

enchantments package: AnvilRecipeListener.java (33,648 B) — custom anvil UI interceptor for enchant combining, Infinity Stone combining, and enchant conflict detection. CustomEnchantManager.java (8,853 B) — defines all 64 custom enchantments with level caps, conflict pairs, and applicable item types. EnchantmentEffectListener.java (69,495 B) — implements all 64 enchantment effects (the largest single file). EnchantmentType.java (1,942 B) — enum of enchantment types/categories. SoulboundListener.java (7,961 B) — prevents Soulbound-enchanted items from dropping on death.

events package: EventManager.java (10,367 B) — central scheduler checking every 60 seconds (1200 ticks) for random events; manages cooldowns (10-minute per-world), broadcasts, boss tagging, weapon drops, and misc loot drops. NetherStormEvent.java (11,421 B) — Ghast swarms, enhanced Blaze spawns, fire rain, Infernal Overlord boss (Ghast, 400 HP). PiglinUprisingEvent.java (8,956 B) — massive Piglin army, Piglin Emperor boss (Piglin Brute, 500 HP). VoidCollapseEvent.java (8,961 B) — void particles pulling players down, Void Leviathan boss (Elder Guardian variant, 500 HP). PillagerWarPartyEvent.java (19,957 B) — overworld Pillager raid with waves. PillagerSiegeEvent.java (19,146 B) — nighttime Pillager siege with Fortress Warlord boss. EndRiftEvent.java (30,058 B) — 15×15 portal, wave spawns, End Rift Dragon (600 HP variant Ender Dragon).

guilds package: GuildListener.java (1,456 B) — guild event hooks. GuildManager.java (13,679 B) — full CRUD, invitations, friendly fire toggle, persistence via file storage.

legendary package (15 files, core of the plugin): LegendaryTier.java (3,673 B) — 5-tier enum: COMMON (12–15 dmg, 10 particles, 1.0× CD multiplier), RARE (14–17 dmg, 25 particles, 0.95×), EPIC (17–20 dmg, 50 particles, 0.9×), MYTHIC (20–30 dmg, 100 particles, 0.85×), ABYSSAL (28–40 dmg, 200 particles, 0.8×). Includes fromId() with backward compat ("LEGENDARY" → EPIC, "UNCOMMON" → COMMON) and validation methods. LegendaryWeapon.java (15,034 B) — enum of 63 weapons: 20 COMMON, 8 RARE, 7 EPIC, 24 MYTHIC, 4 ABYSSAL. Each entry stores: id, displayName, baseMaterial, baseDamage, customModelData (30001–30063), tier, textureName (used for resource pack item JSON lookup), primaryAbilityName, altAbilityName, primaryCooldown, altCooldown. Provides fromId(), byTier(), byMaterial(). LegendaryWeaponManager.java (7,937 B) — creates weapon ItemStacks with string-based CustomModelDataComponent (rule A.14), PersistentDataContainer tags, tier-colored display names, and lore showing abilities/cooldowns. LegendaryAbilityListener.java (18,032 B) — dispatches right-click (primary) and crouch+right-click (alternate) events to the correct ability handler. LegendaryPrimaryAbilities.java (69,951 B) — implementation of all 63 primary abilities. LegendaryAltAbilities.java (73,055 B) — implementation of all 63 alternate abilities. LegendaryAbilityContext.java (4,941 B) — shared state maps (bloodlust stacks, retribution damage, soul counts, phase shift active, etc.) and utility methods (getNearbyEnemies, dealDamage, getTargetEntity, rotateY). LegendaryArmorSet.java (16,160 B) — enum of 13 armor sets (6 craftable, 7 legendary). Each set has 4 pieces with individual CMD values (30101–30254), base material, texture names, passive descriptions, and total defense (12–50). Craftable: Reinforced Leather (12 def), Copper (14), Chainmail Reinforced (15), Amethyst (16), Bone (16), Sculk (18). Legendary: Shadow Stalker (24, RARE), Blood Moon (30, EPIC), Nature's Embrace (28, EPIC), Frost Warden (30, EPIC), Void Walker (36, MYTHIC), Dragon Knight (40, MYTHIC), Abyssal Plate (50, ABYSSAL). LegendaryArmorManager.java (16,059 B) — creates armor ItemStacks. LegendaryArmorListener.java (38,729 B) — implements all set bonuses and piece passives. LegendaryLootListener.java (22,565 B) — drop table logic for bosses, structures, and chests. InfinityStoneManager.java (8,029 B) — 6 stone types (Power=purple, Space=blue, Reality=red, Soul=orange, Time=green, Mind=yellow), fragment creation, finished stone creation, anvil combining. InfinityGauntletManager.java (23,370 B) — Thanos Glove creation, Infinity Gauntlet creation and right-click ability (kills 50% hostile mobs in dimension, 300s cooldown), snap animation.

menu package: CreativeMenuManager.java (45,503 B) — GUI inventory menus for browsing all weapons, armor, power-ups, stones by category/tier. GuideBookManager.java (29,396 B) — generates multi-volume written books explaining the plugin's systems (Portuguese, "guia").

mobs package: BiomeMultipliers.java (3,080 B) — biome-specific scaling lookups. BloodMoonManager.java (12,329 B) — Blood Moon event: red sky, mob buffs, boss spawn every 10th mob, double drops, 0.1% Infinity Stone fragment chance. BossEnhancer.java (19,998 B) — dragon/wither/warden/elder guardian stat scaling, custom abilities per boss. BossMasteryManager.java (12,294 B) — tracks boss kill participation, awards titles (Wither Slayer +5%, Guardian Slayer +7%, Warden Slayer +10%, Dragon Slayer +15%, Abyssal Conqueror +20%, God Slayer +25%/+20% dmg). KingMobManager.java (5,972 B) — every Nth mob spawn (configurable, default 100) becomes a King mob with 7× HP, 2.5× damage, diamond drops. MobDifficultyManager.java (4,744 B) — distance-from-spawn scaling. RoamingBossManager.java (57,427 B) — 6 roaming world bosses: The Watcher (Deep Dark, Warden, 800 HP, EPIC drops, Darkness/Mining Fatigue aura), Hellfire Drake (Nether, Ghast, 600 HP, EPIC, triple fireball salvo), Frostbound Colossus (Snowy biomes, Iron Golem, 700 HP, EPIC, freeze aura + ground pound), Jungle Predator (Jungle, Ravager, 500 HP, RARE/EPIC, stealth pounce + poison fog), End Wraith (The End outer islands, Phantom size 6, 900 HP, MYTHIC, void beam + phase shift + shadow clones at 50% HP), Abyssal Leviathan (Abyss dimension, 1200 HP, ABYSSAL drops). Each has a 2–4% spawn chance per 2-minute check, auto-despawn after 10 minutes, custom particle effects, and special attack patterns.

powerups package: PowerUpManager.java (26,207 B) — 7 power-ups with string-based CustomModelDataComponent: Heart Crystal (RED_DYE, +2 HP, max 40 → +80 HP), Soul Fragment (PURPLE_DYE, +1.0% damage, max 100 → +100%), Titan's Resolve (IRON_NUGGET, +10% KB resist + 2% DR, max 5 → 50% KB + 10% DR), Phoenix Feather (FEATHER, auto-revive, stackable), KeepInventorer (ENDER_EYE, permanent keep-inventory, one-time), Vitality Shard (PRISMARINE_SHARD, +5% DR, max 10 → 50%), Berserker's Mark (BLAZE_POWDER, +3% attack speed, max 10 → 30%). Max total passive DR capped at 75%. PowerUpListener.java (5,607 B) — handles right-click consumption and death events (Phoenix Feather auto-revive, KeepInventory enforcement).

quests package: QuestManager.java (27,940 B) — 6 quest lines (Overworld, Nether, End, Special, Boss, Explorer) with tracking, rewards, and scheduler. QuestProgressListener.java (3,231 B) — monitors kills, exploration, item collection. NpcWizardManager.java (15,501 B) — Archmage NPC villager (purple robes), sells exclusive MYTHIC weapons (rotating stock of 3 for 48 diamonds + 16 Nether Stars), Heart Crystals, Phoenix Feathers, KeepInventorer (64 diamonds + 32 emeralds), and Blessing Crystals.

structures package: StructureType.java (10,780 B) — enum of 40 structure types: 24 Overworld (including Thanos Temple, Pillager Fortress, Pillager Airship, Frost Dungeon, Bandit Hideout, Sunken Ruins, Cursed Graveyard, Sky Altar), 7 Nether, 5 End, 3 Abyss (Abyssal Castle 120×80×120, Void Nexus, Shattered Cathedral), plus the Ender Dragon Death Chest structure. Generation chances range from 0.8% (COMMON) to 0.05% (ABYSSAL) per chunk. StructureManager.java (49,819 B) — chunk-based generation with 300-block minimum spacing, biome filtering, and programmatic block placement. StructureBuilder.java (8,714 B) — block placement API. StructureLootPopulator.java (8,554 B) — chest filling per structure type/tier. StructureBossManager.java (14,554 B) — mini-boss spawning, health bars, and tracking.

utility package: BestBuddiesListener.java (11,609 B) — wolf/dog armor system with 95% DR. DropRateListener.java (3,289 B) — trident 35% drop boost, breeze wind charges. EnchantTransferListener.java (7,135 B) — transfer enchantments between items. InventorySortListener.java (5,282 B) — shift-click empty slot to sort. LootBoosterListener.java (8,991 B) — enhanced chest/mob loot. PaleGardenFogTask.java (1,356 B) — fog effect in Pale Garden biome. VillagerTradeListener.java (4,244 B) — price reduction and trade unlock.

weapons package (12 files): BattleAxeManager.java (8,303 B), BattleBowManager.java (4,871 B), BattleMaceManager.java (4,279 B), BattlePickaxeManager.java (7,168 B), BattleShovelManager.java (8,451 B), BattleSpearManager.java (8,339 B), BattleSwordManager.java (6,821 B), BattleTridentManager.java (4,737 B), SickleManager.java (8,061 B), SpearManager.java (11,128 B), SuperToolManager.java (22,176 B), WeaponAbilityListener.java (81,738 B) — 20 non-legendary weapon abilities, WeaponMasteryManager.java (9,803 B) — kill-based mastery (Novice 0, Fighter 100, Experienced Fighter 500, Master 1000 kills → +1%/+5%/+10%/+15% damage).

Total codebase size: ~1.27 MB of Java source across 68 files.

P2. COMPLETE WEAPON TABLE (63 weapons, exact values from LegendaryWeapon.java)
COMMON (20 weapons, damage 12–15, PCD 3–5s, ACD 10–18s, 10 particles):

#	ID	Display Name	Material	Dmg	CMD	Texture	Primary	Alt	PCD	ACD
1	amethyst_shuriken	Amethyst Shuriken	DIAMOND_SWORD	13	30012	amethyst_shuriken	Shuriken Barrage	Shadow Step	5	10
2	gravescepter	Gravescepter	DIAMOND_SWORD	13	30026	revenants_gravescepter	Grave Rise	Death's Grasp	5	12
3	lycanbane	Lycanbane	DIAMOND_SWORD	14	30027	lycanbane	Silver Strike	Hunter's Sense	5	14
4	gloomsteel_katana	Gloomsteel Katana	DIAMOND_SWORD	13	30028	gloomsteel_katana	Quick Draw	Shadow Stance	3	12
5	viridian_cleaver	Viridian Cleaver	DIAMOND_AXE	15	30029	viridian_greataxe	Verdant Slam	Overgrowth	5	15
6	crescent_edge	Crescent Edge	DIAMOND_AXE	14	30030	crescent_greataxe	Lunar Cleave	Crescent Guard	5	14
7	gravecleaver	Gravecleaver	DIAMOND_SWORD	14	30031	revenants_gravecleaver	Bone Shatter	Undying Rage	5	18
8	amethyst_greatblade	Amethyst Greatblade	DIAMOND_SWORD	13	30032	amethyst_greatblade	Crystal Burst	Gem Resonance	5	18
9	flamberge	Flamberge	DIAMOND_SWORD	14	30033	flamberge	Flame Wave	Ember Shield	5	12
10	crystal_frostblade	Crystal Frostblade	DIAMOND_SWORD	13	30034	crystal_frostblade	Frost Spike	Permafrost	5	15
11	demonslayer	Demonslayer	DIAMOND_SWORD	15	30035	demonslayers_greatsword	Holy Rend	Purifying Aura	5	14
12	vengeance	Vengeance	DIAMOND_SWORD	12	30036	vengeance_blade	Retribution	Grudge Mark	5	10
13	oculus	Oculus	DIAMOND_SWORD	13	30037	oculus	All-Seeing Strike	Third Eye	5	18
14	ancient_greatslab	Ancient Greatslab	DIAMOND_SWORD	15	30038	ancient_greatslab	Seismic Slam	Stone Skin	5	15
15	neptunes_fang	Neptune's Fang	TRIDENT	14	30039	neptunes_fang	Riptide Slash	Maelstrom	5	15
16	tidecaller	Tidecaller	TRIDENT	13	30040	tidecaller	Tidal Spear	Depth Ward	5	14
17	stormfork	Stormfork	TRIDENT	15	30041	stormfork	Lightning Javelin	Thunder Shield	5	18
18	jade_reaper	Jade Reaper	DIAMOND_HOE	14	30042	jadehalberd	Jade Crescent	Emerald Harvest	5	18
19	vindicator	Vindicator	DIAMOND_AXE	13	30043	vindicator	Executioner's Chop	Rally Cry	5	18
20	spider_fang	Spider Fang	DIAMOND_SWORD	12	30044	spider_sword	Web Trap	Wall Crawler	5	14
RARE (8 weapons, damage 14–16, PCD 5–7s, ACD 14–22s, 25 particles):

#	ID	Display Name	Material	Dmg	CMD	Texture	Primary	Alt	PCD	ACD
21	oceans_rage	Ocean's Rage	TRIDENT	16	30001	oceans_rage	Stormbringer	Riptide Surge	5	18
22	aquatic_sacred_blade	Aquatic Sacred Blade	DIAMOND_SWORD	15	30002	aquantic_sacred_blade	Aqua Heal	Depth Pressure	7	18
23	royal_chakram	Royal Chakram	DIAMOND_SWORD	14	30005	royalchakram	Chakram Throw	Spinning Shield	5	14
24	acidic_cleaver	Acidic Cleaver	DIAMOND_AXE	16	30007	treacherous_cleaver	Acid Splash	Corrosive Aura	7	18
25	muramasa	Muramasa	DIAMOND_SWORD	15	30009	muramasa	Crimson Flash	Bloodlust	5	14
26	windreaper	Windreaper	DIAMOND_SWORD	15	30014	windreaper	Gale Slash	Cyclone	5	14
27	moonlight	Moonlight	DIAMOND_SWORD	15	30016	moonlight	Lunar Beam	Eclipse	7	22
28	talonbrand	Talonbrand	DIAMOND_SWORD	15	30022	talonbrand	Talon Strike	Predator's Mark	5	14
EPIC (7 weapons, damage 17–20, PCD 5–8s, ACD 18–28s, 50 particles):

#	ID	Display Name	Material	Dmg	CMD	Texture	Primary	Alt	PCD	ACD
29	berserkers_greataxe	Berserker's Greataxe	DIAMOND_AXE	20	30006	berserkers_greataxe	Berserker Slam	Blood Rage	7	22
30	black_iron_greatsword	Black Iron Greatsword	DIAMOND_SWORD	18	30008	black_iron_greatsword	Dark Slash	Iron Fortress	5	22
31	solstice	Solstice	DIAMOND_SWORD	17	30018	solstice	Solar Flare	Daybreak	7	18
32	grand_claymore	Grand Claymore	DIAMOND_SWORD	19	30019	grand_claymore	Titan Swing	Colossus Stance	7	22
33	calamity_blade	Calamity Blade	DIAMOND_AXE	18	30020	calamity_blade	Cataclysm	Doomsday	8	25
34	emerald_greatcleaver	Emerald Greatcleaver	DIAMOND_AXE	19	30023	emerald_greatcleaver	Emerald Storm	Gem Barrier	7	28
35	demons_blood_blade	Demon's Blood Blade	DIAMOND_SWORD	18	30024	demons_blood_blade	Blood Rite	Demonic Form	5	25
MYTHIC (24 weapons, damage 26–30, PCD 5–7s, ACD 15–28s, 100 particles):

#	ID	Display Name	Material	Dmg	CMD	Texture	Primary	Alt	PCD	ACD
36	true_excalibur	True Excalibur	DIAMOND_SWORD	28	30003	excalibur	Holy Smite	Divine Shield	5	20
37	requiem_ninth_abyss	Requiem of the Ninth Abyss	DIAMOND_SWORD	28	30004	requiem_of_hell	Soul Devour	Abyss Gate	5	28
38	phoenixs_grace	Phoenix's Grace	DIAMOND_AXE	28	30010	gilded_phoenix_greataxe	Phoenix Strike	Rebirth Flame	5	28
39	soul_collector	Soul Collector	DIAMOND_SWORD	27	30011	soul_collector	Soul Harvest	Spirit Army	5	15
40	valhakyra	Valhakyra	DIAMOND_SWORD	26	30013	valhakyra	Valkyrie Dive	Wings of Valor	5	20
41	phantomguard	Phantomguard Greatsword	DIAMOND_SWORD	27	30015	phantomguard_greatsword	Spectral Cleave	Phase Shift	5	18
42	zenith	Zenith	DIAMOND_SWORD	30	30017	zenith	Final Judgment	Ascension	7	28
43	dragon_sword	Dragon Sword	DIAMOND_SWORD	26	30021	dragon_sword	Dragon Breath	Draconic Roar	5	20
44	nocturne	Nocturne	DIAMOND_SWORD	26	30025	nocturne	Shadow Slash	Night Cloak	5	20
45	divine_axe_rhitta	Divine Axe Rhitta	DIAMOND_AXE	30	30045	divine_axe_rhitta	Cruel Sun	Sunshine	5	25
46	yoru	Yoru	DIAMOND_SWORD	28	30046	yoru	World's Strongest Slash	Dark Mirror	6	26
47	tengens_blade	Tengen's Blade	DIAMOND_SWORD	27	30047	tengens_blade	Sound Breathing	Constant Flux	5	20
48	edge_astral_plane	Edge of the Astral Plane	DIAMOND_SWORD	29	30048	edge_astral_plane	Astral Rend	Planar Shift	6	28
49	fallen_gods_spear	Fallen God's Spear	DIAMOND_SWORD	28	30049	fallen_gods_spear	Divine Impale	Heaven's Fall	5	25
50	nature_sword	Nature Sword	DIAMOND_SWORD	26	30050	nature_sword	Gaia's Wrath	Overgrowth Surge	5	20
51	heavenly_partisan	Heavenly Partisan	DIAMOND_SWORD	27	30051	heavenly_partisan	Holy Lance	Celestial Judgment	5	22
52	soul_devourer	Soul Devourer	DIAMOND_SWORD	28	30052	soul_devourer	Soul Rip	Devouring Maw	5	25
53	mjolnir	Mjölnir	MACE	30	30053	mjolnir	Thunderstrike	Bifrost Slam	6	26
54	thousand_demon_daggers	Thousand Demon Daggers	DIAMOND_SWORD	26	30054	thousand_demon_daggers	Demon Barrage	Infernal Dance	5	18
55	star_edge	Star Edge	DIAMOND_SWORD	28	30055	star_edge	Cosmic Slash	Supernova	6	26
56	rivers_of_blood	Rivers of Blood	DIAMOND_SWORD	27	30056	rivers_of_blood	Corpse Piler	Blood Tsunami	5	20
57	dragon_slaying_blade	Dragon Slaying Blade	DIAMOND_SWORD	28	30057	dragon_slaying_blade	Dragon Pierce	Slayer's Fury	5	25
58	stop_sign	Stop Sign	DIAMOND_AXE	26	30058	stop_sign	Full Stop	Road Rage	5	18
59	creation_splitter	Creation Splitter	DIAMOND_SWORD	30	30059	creation_splitter	Reality Cleave	Genesis Break	7	28
ABYSSAL (4 weapons, damage 34–40, PCD 5–8s, ACD 28–40s, 200 particles):

#	ID	Display Name	Material	Dmg	CMD	Texture	Primary	Alt	PCD	ACD
60	requiem_awakened	Requiem of the Ninth Abyss (Awakened)	DIAMOND_SWORD	38	30060	requiem_awakened	Abyssal Devour	Void Collapse	6	35
61	excalibur_awakened	True Excalibur (Awakened)	DIAMOND_SWORD	36	30061	excalibur_awakened	Divine Annihilation	Sacred Realm	5	30
62	creation_splitter_awakened	Creation Splitter (Awakened)	DIAMOND_SWORD	40	30062	creation_splitter_awakened	Reality Shatter	Big Bang	8	40
63	whisperwind_awakened	Whisperwind (Awakened)	DIAMOND_SWORD	34	30063	whisperwind_awakened	Silent Storm	Phantom Cyclone	5	28
P3. COMPLETE ARMOR SET TABLE (13 sets, exact values from LegendaryArmorSet.java)
Craftable Sets (6):

Set	Tier	Mat Base	Def	CMD Range	Set Bonus	Helmet	Chestplate	Leggings	Boots
Reinforced Leather	COMMON	LEATHER	12	30201–30204	Speed I + 10% dodge	Peripheral Vision (mob HP 8 blocks)	Padded Core (+2 HP)	Flexible Joints (no water slow)	Soft Landing (−30% fall)
Copper Armor	COMMON	CHAINMAIL	14	30211–30214	Lightning Rod (no dmg, Speed II 5s)	Static Charge (1 shock to attackers)	Conductive (lightning immune)	Copper Patina (+10% mining)	Grounded (Slowness immune)
Chainmail Reinforced	COMMON	CHAINMAIL	15	30221–30224	Iron Will (+15% KB resist, Mining Fatigue immune)	Chain Mesh (−10% projectile dmg)	Linked Rings (thorns 1)	Chainmail Grip (no KB penalty)	Steel Treads (no soul sand slow)
Amethyst Armor	COMMON	IRON	16	30231–30234	Resonance (auto XP, +20% XP)	Crystal Clarity (Blindness/Darkness immune)	Harmonic Core (Regen I below 30%)	Amethyst Pulse (mobs glow 6 blocks)	Gem Steps (amethyst particles)
Bone Armor	COMMON	IRON	16	30241–30244	Undead Camouflage (undead ignore)	Skull Helm (Wither immune)	Rib Cage (−15% undead dmg)	Bone Marrow (Regen I at night)	Ossified (Slowness immune)
Sculk Armor	RARE	IRON	18	30251–30254	Echo Sense (see thru walls 12 blocks)	Sonic Sight (Night Vision in caves y<50)	Sculk Tendrils (Slowness I 3s to attackers)	Deep Dark (Darkness immune, +10% speed)	Silent Steps (no vibrations)
Legendary Sets (7):

Set	Tier	Mat Base	Def	CMD Range	Set Bonus	Helmet	Chestplate	Leggings	Boots
Shadow Stalker	RARE	DIAMOND	24	30141–30144	Invisible while crouching + 50% sneak dmg	Dark Vision (Night Vision sneaking)	Shadow Cloak (+4 sneak dmg, 15% dodge)	Silent Movement (no sound, Speed I sneak)	Shadow Step (no particles, no fall dmg sneak)
Blood Moon	EPIC	NETHERITE	30	30111–30114	8% lifesteal + 4 HP/kill (max 20)	Blood Sight (glow 40 blocks at night)	Crimson Heart (+4 HP/kill, heal 2)	Blood Rush (Speed II night, Str I <50%)	Silent Predator (no sound, no aggro 5+ night)
Nature's Embrace	EPIC	NETHERITE	28	30131–30134	Regen II in forest, Poison/Wither immune	Photosynthesis (Regen I sunlight, Poison immune)	Living Bark (thorns 4, +4 HP)	Root Grip (KB immune forest, vine climb)	Nature's Path (3x crop growth, flowers)
Frost Warden	EPIC	NETHERITE	30	30151–30154	Freeze 5-block aura, cold immune	Frost Resist (Slowness/Freeze/powder immune)	Blizzard Aura (Slowness II + Mining Fatigue 4 blocks)	Permafrost (Ice Walk 3 blocks, +20% ice speed)	Frost Treads (lava→obsidian, fire immune, water freeze)
Void Walker	MYTHIC	NETHERITE	36	30121–30124	Void Step (crouch+jump TP 12 blocks, 4s CD)	Ender Sight (see invis, Blindness immune)	Void Shield (20% negate damage)	Rift Walk (Slow Falling, phase 1-block walls)	Void Anchor (no fall/void damage)
Dragon Knight	MYTHIC	NETHERITE	40	30101–30104	+35% vs dragon/ender, Dragon Roar (15 dmg, 8 blocks, 30s CD)	Dragon's Eye (Resp III, see entities 20 blocks)	Dragon Heart (+8 HP, permanent Fire Resist)	Dragon Scales (60% KB resist, +15% melee)	Dragon Talons (Feather Fall V, double jump 3s CD)
Abyssal Plate	ABYSSAL	NETHERITE	50	30161–30164	+40% melee, Wither/Poison/Darkness immune, soul-fire thorns 12	Abyssal Vision (Night Vision, all immunity)	Soul Furnace (thorns 12, Wither immune, +10 HP)	Abyssal Stride (Speed II, 80% KB resist, +20% melee)	Obsidian Walker (fire/lava/magma immune, lava walk, no fall)
P4. RESOURCE PACK TEXTURE WORKFLOW (How It Actually Works)
The texture system uses Minecraft 1.21's string-based custom_model_data (not the deprecated integer CMD). The chain is:

The plugin creates an ItemStack (e.g., Material.DIAMOND_SWORD) and sets CustomModelDataComponent with a string list, e.g., cmd.setStrings(List.of("excalibur")).

The resource pack must contain at assets/minecraft/items/diamond_sword.json an item definition that maps the string flag "excalibur" to a specific model path. This JSON file defines multiple model overrides keyed by custom_model_data string values.

The model file (e.g., assets/minecraft/models/item/excalibur.json) references the texture file (e.g., assets/minecraft/textures/item/excalibur.png).

For 3D weapons, the model JSON includes element definitions (cubes, rotation, display transforms) instead of a flat generated item.

For armor, the held/inventory texture uses the same CMD string system. The worn texture (what you see on the player model) requires equipment JSON files at assets/minecraft/equipment/<set_id>.json referencing humanoid layer textures at assets/minecraft/textures/entity/equipment/humanoid/<set_id>.png and humanoid_leggings/<set_id>.png.

Current broken items:

Power-up textures exist in the resource pack at assets/minecraft/textures/item/powerups/ (7 PNG files: heart_crystal, soul_fragment, titan_resolve, phoenix_feather, keep_inventorer, vitality_shard, berserker_mark). The code correctly sets the string CMD. The missing link is the item definition JSON: assets/minecraft/items/red_dye.json, purple_dye.json, iron_nugget.json, feather.json, ender_eye.json, prismarine_shard.json, blaze_powder.json — each needs a custom_model_data override that maps the string (e.g., "heart_crystal") to a model pointing to the power-up texture.

Armor worn textures show vanilla netherite because the resource pack is missing the assets/minecraft/equipment/ directory and the corresponding humanoid PNG files.

P5. COMPLETE STRUCTURE CATALOG (40 structures from StructureType.java)
Overworld (24): Ruined Colosseum (40×25×40, EPIC, Gladiator King 300 HP, Plains/Savanna), Druid's Grove (30×20×30, RARE, Ancient Treant 200 HP, Forest/Dark Forest), Shrek House (15×10×15, EPIC, Shrek 400 HP, Swamp), Mage Tower (12×45×12, EPIC, Arch Mage 250 HP, any biome), Gigantic Castle (80×50×80, MYTHIC, Castle Lord 350 HP, Plains/Mountains), Fortress (50×30×50, EPIC, Warlord 300 HP, Taiga/Stony), Camping Small (10×5×10, COMMON, no boss), Camping Large (20×8×20, RARE, Bandit Leader 150 HP), Ultra Village (100×20×100, RARE, no boss, has NPC Wizard), Witch House Swamp (12×12×12, RARE, Coven Witch 180 HP), Witch House Forest (10×10×10, COMMON, Shadow Witch 150 HP), Allay Sanctuary (25×15×25, RARE, friendly, spawns 5 Allays), Volcano (40×60×40, EPIC, Magma Titan 400 HP, Badlands), Ancient Temple (35×25×35, EPIC, Temple Guardian 250 HP, Jungle/Desert), Abandoned House (12×8×12, COMMON, Restless Spirit 100 HP), House-Tree (15×25×15, COMMON, no boss, Forest), Dungeon Deep (30×15×30, EPIC, Dungeon Keeper 300 HP, underground Y<30), Thanos Temple (50×40×50, MYTHIC, Thanos 800 HP, Badlands), Pillager Fortress (60×35×60, EPIC, Fortress Warlord 400 HP), Pillager Airship (30×25×15, RARE, Sky Captain 250 HP), Frost Dungeon (35×20×35, EPIC, Frost Warden 350 HP, Snowy), Bandit Hideout (25×15×25, RARE, Bandit King 200 HP, Badlands/Desert), Sunken Ruins (30×15×30, EPIC, Drowned Warlord 300 HP, Ocean), Cursed Graveyard (25×12×25, EPIC, Grave Revenant 350 HP, Dark Forest/Swamp), Sky Altar (20×30×20, MYTHIC, Celestial Guardian 500 HP, Mountain peaks).

Nether (7): Crimson Citadel (40×30×40, EPIC, Crimson Warlord 350 HP), Soul Sanctum (30×20×30, RARE, Soul Reaper 250 HP), Basalt Spire (20×50×20, EPIC, Basalt Golem 400 HP), Nether Dungeon (30×15×30, RARE, Blaze Lord 300 HP), Piglin Palace (50×30×50, EPIC, Piglin King 400 HP), Wither Sanctum (40×35×40, EPIC, Wither Priest 450 HP), Blaze Colosseum (45×25×45, EPIC, Infernal Champion 400 HP).

End (5): Void Shrine (25×20×25, MYTHIC, Void Sentinel 300 HP), Ender Monastery (40×25×40, MYTHIC, Ender Monk 250 HP), Dragon's Hoard (20×10×20, MYTHIC, no boss), End Rift Arena (50×20×50, MYTHIC, End Rift Dragon 600 HP), Dragon Death Chest (10×5×10, MYTHIC, no boss).

Abyss (3): Abyssal Castle (120×80×120, ABYSSAL, Abyssal Overlord 1200 HP), Void Nexus (30×40×30, ABYSSAL, Void Arbiter 800 HP), Shattered Cathedral (50×45×50, ABYSSAL, Fallen Archbishop 1000 HP).

P6. ALL COMMANDS AND PERMISSIONS
Commands are defined in plugin.yml and handled in JGlimsPlugin.java:

/jglims — Main admin command. Subcommands: reload (reloads config), stats <player> (blessing stats), enchants (list 64 enchantments), sort (info), mastery (weapon kill mastery), legendary <id|list|tier> (give/list weapons, OP only), armor <set|list> [slot|all] (give/list armor, OP only), powerup <type|stats> [player] (give power-ups, OP only; types: heart, soul, titan, phoenix, keep, vitality, berserker), bosstitles (view boss mastery titles), gauntlet <glove|gauntlet|stone|fragment> [type] (give Infinity items, OP only), menu (creative GUI browser), guia (Portuguese guide books), quests (quest progress), help.

/guild — Guild management: create <name>, invite <player>, join, leave, kick <player>, disband, info, list.

/guia (alias: /guide) — Gives all guide book volumes.

Permission jglims.admin (default: OP) required for admin subcommands.

P7. RESOURCE PACK VERSION HISTORY AND CURRENT STATE
Version	Size (MB)	SHA-1	Status
v4.0.0	3.04	63b8e30d08453c8c18e0cd0472f940a0536818a9	Archive
v4.0.2	3.09	626edf299107f76711835507bda1962740b35474	Archive
v4.0.3	3.17	6cd61288886ea80f091698eab0249b44c25af90a	Archive
v4.0.4	3.19	ca60f59fa3f3dfbeff2d8dfb3339574807b8546b	Archive
v4.0.5	3.19	2147a804f8f0af488c6521ef1c035f4f7feee77d	Archive
v4.0.6	3.19	7d03a10899f13461a2dfb59554dde9ccfcfd9340	Archive
v4.0.7	3.17	c433786cb712d2514c3ad8d89ff2078774742d84	Archive
v4.0.8	3.17	45aeea8ad07ca1512aeccc81ea1d8251bdfe2589	Archive
v4.0.9	3.17	1b9f9e6c7b2d03c114a6ee4f057454ae856a1035	Archive
v4.1.0	3.52	a7a0e72c67c052f8710b2957b7193b94dc0c9824	Last working version
v4.1.1	3.60	649367c93319eb5f9949b0008e3c7d94977d858f	Local only (never confirmed working)
v4.1.2	3.56	ca440d43458f7d7b54c0b25a499ce1b43901a883	Local only
v5.0 (GitHub)	3.52	a7a0e72c67c052f8710b2957b7193b94dc0c9824	Currently deployed (copy of v4.1.0)
v4.1.1 (GitHub, broken)	9.50	dc5cdce0f7a7800c40681ea9e75e9a7528bd6b4d	Broken (wrong internal structure)
Current server state (docker-compose.yml): RESOURCE_PACK=https://github.com/JGlims/JGlimsPlugin/releases/download/v5.0/JGlimsResourcePack-v5.0.zip, RESOURCE_PACK_SHA1=dc5cdce0f7a7800c40681ea9e75e9a7528bd6b4d.

Problem: The SHA-1 in docker-compose.yml (dc5cdce...) is the hash of the broken 9.5 MB pack, but the v5.0 release on GitHub now contains the correct 3.52 MB file (SHA-1 a7a0e72c67c052f8710b2957b7193b94dc0c9824). This SHA-1 mismatch causes Minecraft to reject the download. Fix required: Change RESOURCE_PACK_SHA1 in docker-compose.yml to a7a0e72c67c052f8710b2957b7193b94dc0c9824 and recreate the container.

P8. DEPLOYMENT PIPELINE (COMPLETE REFERENCE)
Build: cd C:\Users\jgmel\Documents\projects\JGlimsPlugin\JGlimsPlugin && .\gradlew clean build → produces build\libs\JGlimsPlugin-2.0.0.jar.

Deploy Plugin JAR:

Copy$sshKey = "C:\Users\jgmel\Documents\projects\server_minecraft\ssh-key-2026-02-25.key"
$remoteHost = "ubuntu@144.22.198.184"
scp -i $sshKey build\libs\JGlimsPlugin-2.0.0.jar "${remoteHost}:/tmp/JGlimsPlugin.jar"
ssh -i $sshKey $remoteHost "docker cp /tmp/JGlimsPlugin.jar mc-crossplay:/data/plugins/JGlimsPlugin.jar && docker restart mc-crossplay"
Deploy Resource Pack (complete procedure):

Build the zip using 7-Zip (never Compress-Archive): 7z a -tzip JGlimsResourcePack-v6.0.zip .\* -xr!.git from inside the resource pack root directory (where pack.mcmeta lives).
Compute SHA-1: (Get-FileHash -Path $zipPath -Algorithm SHA1).Hash.ToLower().
Create GitHub release: gh release create v6.0 $zipPath --repo JGlims/JGlimsPlugin --title "Resource Pack v6.0" --notes "SHA-1: $sha1".
Update docker-compose.yml on the server via SSH: change RESOURCE_PACK URL and RESOURCE_PACK_SHA1 values.
Recreate: cd /home/ubuntu/minecraft-server && docker compose down && docker compose up -d.
Critical rules: Always edit docker-compose.yml, not server.properties (Docker init overwrites it). The SSH user is ubuntu, not opc. The resource pack URL in docker-compose.yml does NOT need escaped colons (the Docker env var handles this). Write all files as UTF-8 without BOM.

P9. KNOWN BUGS AND ISSUES (COMPREHENSIVE)
Resource pack SHA-1 mismatch in docker-compose.yml — Currently set to dc5cdce0f7a7800c40681ea9e75e9a7528bd6b4d (the broken pack) but the actual file is the correct 3.52 MB with SHA-1 a7a0e72c67c052f8710b2957b7193b94dc0c9824. This causes "failed to download" on client connect.

Power-up textures show vanilla — The 7 power-up items use string-based CMD but the resource pack is missing the item definition JSONs for their base materials (red_dye.json, purple_dye.json, etc.) or the JSONs don't include the string-key overrides.

Armor worn textures show vanilla netherite — Missing assets/minecraft/equipment/ directory and assets/minecraft/textures/entity/equipment/humanoid/ PNG files in the resource pack.

Abyss portal does not activate — The 4×5 purpur frame + Abyssal Key interaction has never been confirmed working in production.

Creation Splitter ability bug — An unspecified bug in the "Reality Cleave" or "Genesis Break" ability implementation.

Missing weapon 3D models/textures — At least 9 newer weapons (e.g., divine_axe_rhitta, yoru, tengens_blade, edge_astral_plane, etc.) likely have no corresponding JSON model or PNG texture in the current resource pack, as it was built before these weapons existed.

Awakened weapon animations — The 4 ABYSSAL weapons (requiem_awakened, excalibur_awakened, creation_splitter_awakened, whisperwind_awakened) are designed to have animated textures (spritesheet + mcmeta), but these were partially implemented only for excalibur and requiem; creation_splitter_awakened and whisperwind_awakened have none.

[ERROR] Failed to update server.properties — The Docker mc-image-helper SetPropertiesCommand fails when it can't write to server.properties. This was resolved by fixing file ownership (chown 1000:1000) but may recur if Docker image updates change behavior.

README version mismatch — README says version 1.4.0, plugin.yml says 2.0.0, resource pack is v5.0. These should be unified.

P10. THINGS TO DO NEXT (DETAILED)
Priority 1 — Fix the immediate broken state: Fix the SHA-1 in docker-compose.yml from dc5cdce0f7a7800c40681ea9e75e9a7528bd6b4d to a7a0e72c67c052f8710b2957b7193b94dc0c9824, then docker compose down && docker compose up -d. This will restore the working resource pack download.

Priority 2 — Fix power-up textures: Create item definition JSON files for each power-up's base material. For example, assets/minecraft/items/red_dye.json needs a custom_model_data entry mapping "heart_crystal" to models/item/powerups/heart_crystal which points to textures/item/powerups/heart_crystal.png. Repeat for all 7 power-ups (purple_dye→soul_fragment, iron_nugget→titan_resolve, feather→phoenix_feather, ender_eye→keep_inventorer, prismarine_shard→vitality_shard, blaze_powder→berserker_mark).

Priority 3 — Fix armor worn textures: Create assets/minecraft/equipment/<set_id>.json files for each of the 13 armor sets, and generate the 64×32 humanoid and humanoid_leggings PNG texture sheets. Place them at assets/minecraft/textures/entity/equipment/humanoid/ and humanoid_leggings/.

Priority 4 — Fix Creation Splitter ability bug: Debug LegendaryPrimaryAbilities.java and LegendaryAltAbilities.java for the creation_splitter entry. The "Reality Cleave" primary or "Genesis Break" alternate is causing an error (likely a null entity reference or radius miscalculation).

Priority 5 — Fix Abyss portal: Test and debug AbyssDimensionManager.java portal detection logic. Verify the purpur frame dimensions, Abyssal Key NamespacedKey tag, and world_abyss creation.

Priority 6 — Add missing weapon textures/models: Create 3D model JSONs and PNG textures for the 15 newer MYTHIC weapons (CMD 30045–30059) and 4 ABYSSAL weapons (30060–30063). Each needs: assets/minecraft/models/item/<texture_name>.json (with element definitions or flat generated model) and assets/minecraft/textures/item/<texture_name>.png. Add overrides to assets/minecraft/items/diamond_sword.json, diamond_axe.json, and mace.json.

Priority 7 — Awakened weapon animations: Complete spritesheet + .mcmeta animation files for creation_splitter_awakened and whisperwind_awakened.

Priority 8 — Performance optimization: Implement entity summon caps (max simultaneous summoned entities per ability), particle throttle (reduce count when TPS < 18), task tracking (cancel orphaned BukkitRunnables), and event mob cleanup timers.

Priority 9 — World Weapons category: New enum WorldWeapon.java with the same structure as LegendaryWeapon. Use remaining unused Fantasy 3D and Majestica textures from blades_of_majestica_v2.0.zip and nongko's_Fantasy_Weapons_v1.17A.zip. Damage comparable to COMMON/RARE legendary weapons. New WorldWeaponManager.java, WorldWeaponAbilityListener.java, and item definitions.

Priority 10 — Magic Items: New enum and manager for magic wand items (Wand of Wands, Vingardium Leviosa → levitation spell, Avada Kedavra → instant high damage with long cooldown). Base material: BLAZE_ROD with custom models. New MagicItemManager.java, MagicAbilityListener.java.

Priority 11 — More bosses and structures: Add mini-bosses to boss-less structures (Camping Small, Allay Sanctuary, Ultra Village, House-Tree, Dragon's Hoard). Add new structure types per README roadmap. Increase roaming boss variety.

Priority 12 — Lunar dimension: New dimension with AbyssChunkGenerator-style void generation, moon-surface terrain (end stone + white concrete), reduced gravity (Slow Falling permanent + increased jump), space-themed structures, portal built from end stone (4×5 frame, opened with flint and steel).

Priority 13 — Aether dimension: Classic Aether-inspired: portal from glowstone frame + water bucket, sky islands with grass/trees, flying mobs (Aether variants), structures (sky temples, cloud castles), loot (legendary/world weapons, enchanted books), Aether Dragon boss.

P11. INFINITY GAUNTLET SYSTEM (COMPLETE DETAIL)
Crafting chain: Step 1: Find Thanos Temple structure (MYTHIC, Badlands, 50×40×50), defeat Thanos (Iron Golem, 800 HP, custom attacks) → drops Thanos Glove (100%). Step 2: During Blood Moon, any mob has 0.1% chance to drop a random Colored Stone (6 types: Power=purple, Space=blue, Reality=red, Soul=orange, Time=green, Mind=yellow). Step 3: Combine each Colored Stone + 1 Redstone in Anvil → Infinity Stone. Step 4: Shapeless recipe: Thanos Glove + all 6 Infinity Stones → Infinity Gauntlet.

Ability: Right-click kills 50% of all loaded hostile mobs in the player's dimension. Excluded: all bosses, mini-bosses, event bosses, King mobs, Blood Moon bosses. Cooldown: 300 seconds. Visual: gold + soul fire particle burst, screen shake, dramatic sound. Indestructible.

Commands: /jglims gauntlet glove (gives Thanos Glove), /jglims gauntlet gauntlet (gives completed Infinity Gauntlet), /jglims gauntlet stone <type> (gives finished stone), /jglims gauntlet fragment <type> (gives colored stone fragment).

P12. QUEST SYSTEM (COMPLETE DETAIL)
6 quest lines managed by QuestManager.java (27,940 B) with periodic scheduler. Quest progress tracked in player PersistentDataContainer. Categories: Overworld (kill zombies, find structures, survive Blood Moon), Nether (kill Blazes, clear citadel, defeat Piglin King), End (kill Endermen, defeat End Rift Dragon, find Dragon's Hoard), Special (collect all 6 Infinity Stones, defeat all 5 bosses, earn God Slayer), Boss (damage each boss type), Explorer (visit biomes, find structures). Rewards include Soul Fragments, Heart Crystals, COMMON/RARE/MYTHIC weapons, Phoenix Feathers, Blessing Crystals, exclusive titles, one ABYSSAL weapon choice.

The NPC Wizard (NpcWizardManager.java, 15,501 B) spawns in Mage Towers and Ultra Villages. Sells: 3 rotating exclusive MYTHIC weapons (48 diamonds + 16 Nether Stars), Heart Crystal (16 diamonds), Phoenix Feather (32 diamonds), KeepInventorer (64 diamonds + 32 emeralds), Blessing Crystals (8 diamonds each).

P13. EVENT SYSTEM TIMING AND PROBABILITIES
Events are checked every 60 seconds (1200 ticks). After any event ends, a 10-minute cooldown applies per world before another can start.

Event	Dimension	Trigger Chance	Duration	Final Boss	Boss HP	Drops
Pillager War Party	Overworld	6% per check	5 min	Fortress Warlord	400	EPIC weapons
Pillager Siege	Overworld (night only)	4% per check	5 min	—	—	RARE weapons
Nether Storm	Nether	10% per check	5 min	Infernal Overlord (Ghast)	400	EPIC weapons
Piglin Uprising	Nether	8% per check	5 min	Piglin Emperor (Piglin Brute)	500	EPIC weapons + gold
Void Collapse	The End	5% per check	5 min	Void Leviathan (Elder Guardian)	500	MYTHIC weapons
End Rift	Overworld (triggered by Dragon kill)	10% on Dragon death	15 min	End Rift Dragon	600	1–2 MYTHIC from pool (Tengen's, Soul Devourer, Star Edge, Creation Splitter, Stop Sign)
Blood Moon	Overworld (night)	15% per check (separate scheduler)	Full night	Blood Moon King (Wither Skeleton, 6000 HP)	12× base	5–15 diamonds + RARE weapon + EPIC chance
P14. ROAMING BOSS COMPLETE SPECIFICATIONS
Checked every 2 minutes (2400 ticks). Only one of each type can exist at a time. Auto-despawn after 10 minutes.

Boss	Entity	Dimension/Biome	HP	Spawn %	Drops	Special Attacks
The Watcher	Warden	Deep Dark (y<0)	800	3%	EPIC weapons	Darkness/Mining Fatigue aura (20 blocks), Sculk particles
Hellfire Drake	Ghast	Nether Wastes/Soul Sand	600	4%	EPIC weapons	Triple fireball salvo, flame/lava ambient particles
Frostbound Colossus	Iron Golem	Snowy biomes	700	3%	EPIC weapons	Slowness aura (15 blocks), freeze ticks, ground pound AoE (10 dmg, 10 blocks, 8s CD)
Jungle Predator	Ravager	Jungle biomes	500	4%	RARE/EPIC	Stealth pounce (invis 3s → leap + 12 dmg + poison), poison fog (8 blocks)
End Wraith	Phantom (size 6)	The End (outer islands)	900	2%	MYTHIC weapons	Phase shift (TP behind target), void beam (14 dmg + Levitation), shadow clones at 50% HP (3× Phantom size 3, 100 HP each)
Abyssal Leviathan	—	Abyss dimension	1200	3%	ABYSSAL weapons	(Implementation continues in truncated file)
P15. DOCKER ENVIRONMENT CONFIGURATION
docker-compose.yml location: /home/ubuntu/minecraft-server/docker-compose.yml

Critical environment variables that affect server.properties:

RESOURCE_PACK — URL to the zip (Docker init writes this with escaped colons into server.properties)
RESOURCE_PACK_SHA1 — SHA-1 hash
RESOURCE_PACK_ENFORCE — TRUE
RESOURCE_PACK_PROMPT — "JGlims Server requires a resource pack for custom weapons"
Data mount: Bind mount from /home/ubuntu/minecraft-server/data to /data inside the container (rw, rprivate).

Container name: mc-crossplay. Image: itzg/minecraft-server (Java 25, buildtime 2026-03-13). Container runs as UID 1000:GID 1000.

Key lesson learned: Never edit /data/server.properties inside the container — the Docker init script overwrites it on every start using the environment variables. Always modify docker-compose.yml and recreate the container. 






JGLIMSPLUGIN — DEFINITIVE PROJECT SUMMARY v11.0
Compiled: 2026-03-16 | Author: JGlims (jg.melo.lima2005@gmail.com) | Plugin Version: 2.0.0 | Target Version: 3.0.0 Latest Commit: fc442b48 (2026-03-16 06:01 UTC) | Repository: https://github.com/JGlims/JGlimsPlugin

A. SERVER & INFRASTRUCTURE
The plugin runs inside a Docker container named mc-crossplay on an Oracle Cloud ARM64 instance at IP 144.22.198.184. Java edition connects on port 25565, Bedrock edition on port 19132. The server runs Paper 1.21.11-127 on Java 25 (OpenJDK 25.0.2+10-LTS, Eclipse Adoptium Temurin). Crossplay is handled by GeyserMC 2.9.4-SNAPSHOT paired with Floodgate 2.2.5-SNAPSHOT. Additional server plugins include SkinsRestorer 15.11.0 and Chunky 1.4.40. The JVM has 8 GB of memory allocated. Online mode is disabled to allow crossplay. The default game mode is creative, with a maximum of 15 players, view distance 10, and simulation distance 6.

SSH access uses the key file at C:\Users\jgmel\Documents\projects\server_minecraft\ssh-key-2026-02-25.key connecting as user ubuntu (confirmed — not opc). The Docker Compose file is located at /home/ubuntu/minecraft-server/docker-compose.yml and the server data is a bind mount from /home/ubuntu/minecraft-server/data to /data inside the container (not a Docker volume). The local development path is C:\Users\jgmel\Documents\projects\JGlimsPlugin\JGlimsPlugin and the resource pack working directory is C:\Users\jgmel\Documents\projects\JGlimsPlugin\resourcepack-work\JGlimsResourcePack.

B. RESOURCE PACK
The server currently runs resource pack v5.0 (SHA-1 a7a0e72c67c052f8710b2957b7193b94dc0c9824, 3.52 MB), which is byte-identical to v4.1.0. It is hosted on GitHub Releases and configured via docker-compose.yml environment variables: RESOURCE_PACK (URL), RESOURCE_PACK_SHA1, and RESOURCE_PACK_ENFORCE=TRUE. The Docker image itzg/minecraft-server overwrites server.properties from these env vars on every container start, so editing server.properties directly is useless — all changes must go in docker-compose.yml followed by docker compose down && docker compose up -d.

Minecraft 1.21.4+ uses item model definitions in assets/minecraft/items/*.json with type minecraft:select and property minecraft:custom_model_data. The Java plugin sets the string via CustomModelDataComponent.setStrings(List.of("texture_name")) and the JSON when value must exactly match.

What works right now: all 63 legendary weapon textures and 3D models, all 63 primary and alternate abilities, armor set item icons in inventory (13 sets), armor worn textures (FIXED — now uses equippable component with asset_id pointing to equipment JSONs and humanoid PNGs), Infinity Gauntlet and Thanos Glove textures, Infinity Stone textures, Abyss portal activation (FIXED), and the plugin loads cleanly.

What does NOT work: power-up item textures (7 items still show vanilla — PowerUpManager.java may still use integer CMD instead of string-based CMD, and item definition JSONs may lack the correct when cases; also missing PNGs for keep_inventorer.png and berserker_mark.png), missing animated textures for Whisperwind, True Excalibur Awakened, Creation Splitter, and Edge of the Astral Plane (awakened weapons should have animations as their only visual difference from base versions), and one unidentified Creation Splitter ability bug.

Resource pack version history spans v4.0.0 (3.04 MB, pack_format 75, initial 63 weapons) through v5.0 (current — identical copy of v4.1.0). The 9.5 MB "v4.1.1" file on the v4.1.0 release was created by PowerShell's Compress-Archive and is broken. Updates must use 7-Zip, generate SHA-1, upload as a new GitHub release (v6.0, v7.0, etc. — never overwrite), update docker-compose.yml on the server, then recreate the container.

C. CODEBASE
The repository tree SHA is fe2676dc897462c4a021d0530e49fa7a48255269. The plugin.yml declares version 2.0.0 with api-version 1.21. The build output is JGlimsPlugin-2.0.0.jar. The codebase consists of 70+ Java files across 13 packages totaling approximately 1.3 MB of source code.

C.1 Root Package — com.jglims.plugin
JGlimsPlugin.java (38,319 B) — the main plugin class. Contains onEnable() which registers every manager and listener, all three command handlers (/jglims, /guild, /guia), and subcommand routing. The /jglims command supports subcommands including menu (opens creative GUI), stats (shows player stats), abyss tp (teleports to Abyss), abyss boss (manually triggers dragon fight), abyss key (gives Abyssal Key), give <weapon_id> (gives a legendary weapon), and various admin utilities. The /guild command manages guild CRUD operations. The /guia command opens the Portuguese-language guide book. Lines 286–310 contain the Abyss subcommand handler which delegates to AbyssDimensionManager.teleportPlayer() and AbyssDragonBoss.manualTrigger(). Lines 507–520 wire the AbyssDragonBoss listener registration.

C.2 abyss Package (5 files — the dimension system)
AbyssChunkGenerator.java (6,727 B) — custom void-based chunk generation. Uses multi-octave sine/cosine noise to generate terrain within a 210-block radius of the world origin. Three noise octaves (wavelengths 0.03, 0.07, 0.12) produce an undulating surface centered around Y=50. Surface materials vary by distance from origin: within 110 blocks it places DEEPSLATE_TILES; between 110 and 150 it mixes POLISHED_BLACKSTONE (20%) and DEEPSLATE_TILES; beyond 150 it mixes PURPUR_BLOCK (20%) and END_STONE. Subsurface layers use POLISHED_BLACKSTONE, DEEPSLATE, OBSIDIAN, and CRYING_OBSIDIAN at varying depths. The generator also creates amethyst crystal spires (1.2% chance, height 4–12), obsidian pillars (0.8% chance, height 5–17), end rod lights (1.5%), crying obsidian accents (2.5%), and soul campfires (0.6%) in the outer terrain beyond 120 blocks. At distance 160+, floating islands spawn (12% chance per 28-block grid cell) with randomized radii (7–21 blocks), composed of deepslate, obsidian, end stone, and purpur, topped with soul lanterns and amethyst clusters. Void rivers carve through terrain between distance 130–200 where a sine-based noise function crosses near zero, clearing blocks down to Y=1. All vanilla generation (noise, surface, caves, decorations, mobs, structures) is disabled.

AbyssDimensionManager.java (18,891 B) — creates and manages the world_abyss world. Current committed state: the world is created with World.Environment.NORMAL (was changed from THE_END by a previous fix attempt). The world name is "world_abyss", arena center Z is -120. On initialization, if the world doesn't exist it creates it with the custom chunk generator, then sets game rules: DO_DAYLIGHT_CYCLE false, DO_WEATHER_CYCLE false, DO_MOB_SPAWNING false, KEEP_INVENTORY true, MOB_GRIEFING false, DO_FIRE_TICK false. Time is set to 18000 (midnight). A BukkitRunnable repeats every 6000 ticks to enforce midnight and clear weather. A comment says "No vanilla dragon cleanup needed — NORMAL environment." The ambient spawner runs every 400+ ticks, spawning Void Wanderers (Endermen, 60% chance, 50 HP, purple name) and Abyssal Sentinels (Wither Skeletons, 40% chance, 40 HP, dark red name, stone sword), up to 12 total, avoiding a 50-block exclusion zone around the arena at (0, y, -120). The Abyssal Key is an Echo Shard with CMD 9001 and custom lore. Portal activation listens for RIGHT_CLICK_BLOCK on PURPUR_BLOCK while holding the key, searches for a valid 4×5 frame in both X-axis and Z-axis orientations, fills the interior with purple particles on a repeating task, and teleports the player after 3 seconds to (0.5, safeY, 115.5) facing north (180° yaw). The file also contains findSafeY(), findPortalFrame(), checkFrame(), and activatePortal() methods. On fresh worlds, it calls AbyssCitadelBuilder.build() to construct the cathedral.

AbyssDragonBoss.java (33,876 B) — the boss fight controller. Constants: DRAGON_HP = 1500.0, DAMAGE_REDUCTION = 0.20, ARENA_R = 40, ARENA_CENTER_Z = -120, MAX_MINIONS = 6, RESPAWN_COOLDOWN_MS = 30 minutes. State fields: active boolean, lastDeathTime, attackTick, arenaCenter Location, arenaY int, combatLoop and movementAI BukkitRunnables, a custom Adventure API BossBar (purple, PROGRESS overlay), 8-waypoint movement system, 4-phase tracking. The walk-on trigger (onPlayerMove) checks: player is in world_abyss, the boss isn't already active, respawn cooldown has passed, the block below is solid and one of BEDROCK/OBSIDIAN/CRYING_OBSIDIAN/END_STONE_BRICKS/DEEPSLATE_BRICKS/POLISHED_BLACKSTONE_BRICKS/BLACKSTONE, findArenaY() returns a valid Y, the player is within 6 blocks of arena Y, and the player is within ARENA_R of (0, y, -120). Manual trigger via manualTrigger(Player) bypasses floor checks, falls back to player Y if arena floor isn't found, and teleports the player to arenaCenter + 15 on Z. The startFight() method cleans up previous entities and minions, generates 8 flight waypoints in a circle at varying heights (15–25 above arena), spawns the dragon model at arenaCenter + 25 Y, creates the boss bar, shows a title announcement to all Abyss players, strikes 4 decorative lightning bolts, and starts two loops — combatLoop (every 40 ticks / 2 seconds for attacks) and movementAI (every 10 ticks / 0.5 seconds for waypoint navigation). Phase transitions occur at 75% HP (phase 2), 50% (phase 3), 25% (phase 4), and 10% (enrage). Attacks include: void breath (dragon breath particles + soul fire towards nearest player, 8-15 damage), soul fire rain (fire charges from sky on random arena positions, 6-12 damage), lightning strikes (at player positions, 10 damage), ground pound (AOE knockback + damage when dragon dives low, 12-20 damage within 8 blocks), void pull (drags all players toward arena center for 3 seconds), and minion summons (spawns Wither Skeletons tagged abyss_minion, max 6). Movement AI navigates between the 8 waypoints at 0.6 speed (0.8 in phase 3+, 1.0 in phase 4+). On death: plays ENDER_DRAGON_DEATH sound, explosion particles, drops 1-2 random ABYSSAL tier weapons and 3-5 power-up items, clears boss bar, resets active flag, records lastDeathTime, and spawns an exit portal (end gateway) at arena center. The findArenaY() method scans from Y 200 down to -64 at coordinates (0, y, ARENA_CENTER_Z) looking for BEDROCK, OBSIDIAN, CRYING_OBSIDIAN, END_STONE_BRICKS, DEEPSLATE_BRICKS, POLISHED_BLACKSTONE_BRICKS, or BLACKSTONE. If no floor is found, it logs a warning and returns -1 (manual trigger falls back to player Y - 1). Damage handling intercepts EntityDamageEvent on the dragon's base zombie entity, applies 20% reduction, calls dragonModel.damageFlash(), and triggers phase checks.

AbyssDragonModel.java (15,498 B) — visual representation of the dragon. Uses an invisible Zombie as the base entity with an ItemDisplay (PAPER item with CMD 40000) mounted as a passenger. Scale is 5.0. THE CRITICAL BUG (current committed code): Inside the spawn() method, the zombie is spawned with a lambda that sets MAX_HEALTH and calls setHealth(maxHp) inside the spawn callback, which causes an IllegalArgumentException because Paper validates health against the not-yet-applied max health attribute. This is the root cause of the dragon not spawning. The spawn also sets the zombie to invisible, silent, no AI, no gravity, non-invulnerable, named "Abyssal Void Dragon" (dark purple bold), adult, can't pick up items, doesn't despawn, and tagged abyss_dragon_base. The SCALE attribute is set to 3.0 if available. The ItemDisplay uses FIXED billboard, 1.5 view range, tag abyss_dragon_part, a Transformation with translation offset (-SCALE/2, -0.5, -SCALE/2) and uniform scale, interpolation duration 3, shadow radius 4.0, shadow strength 0.6. The hover animation runs every 2 ticks, oscillating with a sine wave (amplitude 0.4, frequency 0.08), spawning DRAGON_BREATH particles at body, END_ROD at head, and PORTAL particles at wings. Phase 3+ adds SOUL_FIRE_FLAME particles, phase 4+ adds REVERSE_PORTAL. The display entity rotates with a slow sinusoidal yaw. Other methods: setTarget(Location) moves toward target at 0.6 speed, moveToward(Location, double speed) for variable speed movement, breathAttackVisual(Location target) draws a DRAGON_BREATH + SOUL_FIRE_FLAME particle beam, wingGustVisual() creates a 360° CLOUD particle ring + EXPLOSION, phaseTransition(int) and setPhase(int) handle visual phase changes (phase 2: growl + dragon breath, phase 3: wither ambient + soul fire + 1.15× scale increase, phase 4: dragon death sound + reverse portal + explosions), damageFlash() briefly tints the display red, getHealth() / getMaxHealth() / getLocation() / isAlive() delegate to the base zombie, cleanup() cancels animation task and removes both entities.

AbyssCitadelBuilder.java (54,230 B) — the largest abyss file. Builds a massive gothic cathedral centered at the world origin. Block palette defines 25+ material constants. The NB bug (current committed code): NB is still defined as Material.NETHER_BRICKS at line ~46, which should be DEEPSLATE_BRICKS. Other materials: DS = DEEPSLATE_BRICKS (primary wall), DT = DEEPSLATE_TILES (secondary wall), PD = POLISHED_DEEPSLATE (accents), CD = CHISELED_DEEPSLATE (trim), RNB = RED_NETHER_BRICKS (accent), BL = BLACKSTONE (foundation), PBL = POLISHED_BLACKSTONE (floor accent), PBB = POLISHED_BLACKSTONE_BRICKS (floor), OBS = OBSIDIAN, CRY = CRYING_OBSIDIAN (glow accent), AME = AMETHYST_BLOCK, AMC = AMETHYST_CLUSTER, PGP = PURPLE_STAINED_GLASS_PANE (windows), PG = PURPLE_STAINED_GLASS, MG = MAGENTA_STAINED_GLASS_PANE, IB = IRON_BARS, SL = SOUL_LANTERN, SF = SOUL_CAMPFIRE, ER = END_ROD, ES = END_STONE_BRICKS (arena pillars), BK = BEDROCK (arena floor), BA = BARRIER (arena walls), STR = DEEPSLATE_BRICK_STAIRS, PIL = POLISHED_DEEPSLATE (pillars), PUR = PURPUR_BLOCK. Key dimensions: center spire 180 blocks tall, nave length 80 (z=-40 to z=40), nave half-width 30, nave height 50, facade half-width 60, facade height 80, arena radius 40, arena 120 blocks behind cathedral at z=-120. The build executes in 8 phases: (1) elliptical foundation platform (110×140) with blackstone fill and polished blackstone brick surface, plus a circular courtyard at z=65 radius 40 with crying obsidian accent rings; (2) nave body from z=-40 to z=40 with gothic tapering walls (walls narrow as they rise), transept arms extending ±40 on X at z=-10, apse (semicircular rear at z=-40); (3) central mega-spire (180 blocks, octagonal tapering), 2 flanker spires at ±20 X (120 blocks each), and 10+ minor spires at various positions (60–90 blocks); (4) flying buttresses connecting nave walls to outer piers, gothic arched windows with purple glass, and the roof line (buildRoofLine() — pointed roof along the nave with material selection using NB for high progress values, which is where some nether bricks appear and where roof holes exist); (5) interior nave with ribbed vaulting, 8 interior pillars, 4 interior chapels with chests, 4 abyssal weapon guardian chambers (each containing one ABYSSAL tier weapon in a chest, guarded by Wither Skeletons), and stairs descending to the arena; (6) arena at z=-120 with bedrock floor, barrier walls at radius 40, obsidian structural pillars at 8 cardinal+diagonal positions, crying obsidian accents, a rune circle pattern on the floor; (7) environmental decoration including rocky terrain outcrops around the foundation, floating amethyst crystals above the cathedral, glowing crying obsidian court lines, and a lit approach path from the courtyard to the entrance; (8) delayed chest population (60 ticks after build) placing ABYSSAL and MYTHIC tier weapons in chambers, power-ups, and enchanted books, plus guard spawning (Wither Skeletons and Endermen at strategic positions).

The buildFrontFacade() method creates a gothic pointed-arch facade facing south (+Z). It builds the pointed arch shape using the formula where each column's height follows a pointed arch curve. The method constructs a grand entrance arch (10 blocks wide, 15 tall), a rose window (circular pattern with purple glass and chiseled deepslate frame at height ~40), decorative blind arcading, and two corner buttress towers. Material selection uses DS for the main body, NB for high-ratio structural elements (where it shows as nether bricks — this is the "plain front" issue), CD for trim at arch edges, OBS for the entrance frame, and PGP/PG for the rose window glass.

The buildRoofLine() method creates a pointed pitched roof running the length of the nave. It iterates from z=-40 to z=40, calculating a cross-section at each Z. The roof peak is at x=0 and slopes down to the nave walls. Material selection uses DS for the main roof surface but switches to NB when progress > 0.85 and returns NB as the default fallback — this is why nether bricks appear on the roof and likely why there are gaps (the formula may not cover all blocks at the apex and edges).

C.3 blessings Package (2 files)
BlessingListener.java (3,452 B) listens for blessing crystal consumption events. BlessingManager.java (10,296 B) manages three blessing types: C-Bless (+1 HP per use, max 10 uses), Ami-Bless (+2% damage per use, max 10), La-Bless (+2% defense per use, max 10). Stats viewable via /jglims stats. Data persists per-player in the plugin's data folder.

C.4 config Package (1 file)
ConfigManager.java (29,211 B) reads config.yml with sections for: mob difficulty (distance-based scaling from 350 to 5000 blocks, base multipliers 1.0–3.0), biome multipliers (specific biomes get extra scaling), boss enhancer (Ender Dragon 3.5× HP, Wither 1.0×, Warden 1.0×, Elder Guardian 2.0×), creeper explosion reduction (50% cancel chance), pale garden fog, loot booster, blessings, anvil modifications (removes "Too Expensive" cap, 50% XP reduction), feature toggles for 10+ systems, drop rate booster (35% trident drop rate), villager trade modifications (50% price reduction, no trade locking), king mob settings (every 100th mob, 7× HP), axe attack speed nerf (0.5), weapon mastery (max 1000 kills for +20% bonus), blood moon (15% nightly chance, 12× boss HP), guilds (max 10 members, no friendly fire), dog armor (95% damage reduction), super tools (2% bonus per enchant level for netherite), and ore detection radii.

C.5 crafting Package (2 files)
RecipeManager.java (34,774 B) registers all custom shaped and shapeless recipes: 8 battle tool types (sword, axe, bow, mace, shovel, pickaxe, trident, spear), super tools (diamond and netherite tiers), sickles, Infinity Stone combining (6 colored glass + redstone = finished stone for each of the 6 types), Infinity Gauntlet assembly (Thanos Glove + all 6 stones), and all 24 craftable armor piece recipes (6 sets × 4 pieces). VanillaRecipeRemover.java (588 B) removes conflicting vanilla recipes on server start.

C.6 enchantments Package (5 files)
64 custom enchantments defined in CustomEnchantManager.java (8,853 B) with level caps, conflict pairs, and applicable item type restrictions. EnchantmentEffectListener.java (69,495 B) is the largest single file in the project, implementing all 64 enchantment effects triggered on hit, defense, tick, and various game events. AnvilRecipeListener.java (33,648 B) intercepts the anvil GUI for custom enchant combining, Infinity Stone assembly, and enchant conflict detection. EnchantmentType.java (1,942 B) is an enum of enchantment categories. SoulboundListener.java (7,961 B) prevents items with the Soulbound enchantment from dropping on player death.

C.7 events Package (7 files)
6 active world events managed by EventManager.java (10,367 B) which checks every 60 seconds (1200 ticks) with 10-minute per-world cooldowns. NetherStormEvent.java (11,421 B) — Ghast swarms, enhanced Blazes, fire rain, Infernal Overlord boss (Ghast, 400 HP). PiglinUprisingEvent.java (8,956 B) — massive Piglin army, Piglin Emperor boss (Piglin Brute, 500 HP). VoidCollapseEvent.java (8,961 B) — void particles pulling players, Void Leviathan boss (Elder Guardian variant, 500 HP). PillagerWarPartyEvent.java (19,957 B) — overworld Pillager raid with escalating waves. PillagerSiegeEvent.java (19,146 B) — nighttime siege with Fortress Warlord boss. EndRiftEvent.java (30,058 B) — 15×15 portal, wave-based spawns, End Rift Dragon boss (600 HP Ender Dragon variant).

C.8 guilds Package (2 files)
GuildManager.java (13,679 B) — full CRUD for player guilds: create, invite, accept, decline, leave, disband, list, info, promote, demote, set name/tag. Max 10 members per guild. Optional friendly-fire toggle. Data persists via YAML files per guild. GuildListener.java (1,456 B) hooks into damage and chat events for guild integration.

C.9 legendary Package (15 files — the plugin's core)
LegendaryTier.java (3,673 B) — 5-tier enum: COMMON (12–15 damage, 10 particles, 1.0× cooldown multiplier), RARE (14–17 damage, 25 particles, 0.95×), EPIC (17–20 damage, 50 particles, 0.9×), MYTHIC (20–30 damage, 100 particles, 0.85×), ABYSSAL (28–40 damage, 200 particles, 0.8×). Includes backward compatibility mapping ("LEGENDARY" → EPIC, "UNCOMMON" → COMMON).

LegendaryWeapon.java (15,034 B) — enum of all 63 weapons. Each stores: id, displayName, baseMaterial, baseDamage, customModelData (30001–30063), tier, textureName (must match resource pack JSON when value), primaryAbilityName, altAbilityName, primaryCooldown, altCooldown. 20 COMMON: Amethyst Shuriken, Gravescepter, Lycanbane, Gloomsteel Katana, Viridian Cleaver, Crescent Edge, Gravecleaver, Amethyst Greatblade, Flamberge, Crystal Frostblade, Demonslayer, Vengeance, Oculus, Ancient Greatslab, Neptune's Fang, Tidecaller, Stormfork, Jade Reaper, Vindicator, Spider Fang. 8 RARE: Ocean's Rage, Aquatic Sacred Blade, Royal Chakram, Acidic Cleaver, Muramasa, Windreaper, Moonlight, Talonbrand. 7 EPIC: Berserker's Greataxe, Black Iron Greatsword, Solstice, Grand Claymore, Calamity Blade, Emerald Greatcleaver, Demon's Blood Blade. 24 MYTHIC: True Excalibur, Requiem of the Ninth Abyss, Phoenix's Grace, Soul Collector, Valhakyra, Phantomguard, Zenith, Dragon Sword, Nocturne, Divine Axe Rhitta, Yoru, Tengen's Blade, Edge of the Astral Plane, Fallen God's Spear, Nature Sword, Heavenly Partisan, Soul Devourer, Mjölnir, Thousand Demon Daggers, Star Edge, Rivers of Blood, Dragon Slaying Blade, Stop Sign, Creation Splitter. 4 ABYSSAL: Requiem Awakened (38 dmg), Excalibur Awakened (34 dmg), Creation Splitter Awakened (40 dmg), Whisperwind Awakened (30 dmg).

LegendaryWeaponManager.java (7,937 B) — creates weapon ItemStacks with string-based CustomModelDataComponent, PersistentDataContainer tags, tier-colored display names, and lore. LegendaryAbilityListener.java (18,032 B) — dispatches right-click (primary) and crouch+right-click (alt) to ability handlers. LegendaryPrimaryAbilities.java (69,951 B) and LegendaryAltAbilities.java (73,043 B) — implementations of all 126 abilities (63 primary + 63 alternate). LegendaryAbilityContext.java (7,661 B) — shared state maps (bloodlust, retribution, soul counts, phase shift, etc.), utility methods (getNearbyEnemies, dealDamage, getTargetEntity, rotateY), and particle throttle system (spawnThrottled, trackTask, trackSummon, cleanupPlayer).

LegendaryArmorSet.java (16,160 B) — 13 armor sets. 6 craftable: Reinforced Leather (12 def), Copper (14), Chainmail Reinforced (15), Amethyst (16), Bone (16), Sculk (18). 7 legendary: Shadow Stalker (24, RARE), Blood Moon (30, EPIC), Nature's Embrace (28, EPIC), Frost Warden (30, EPIC), Void Walker (36, MYTHIC), Dragon Knight (40, MYTHIC), Abyssal Plate (50, ABYSSAL — +30% damage, Wither immunity). LegendaryArmorManager.java (16,059 B) creates armor ItemStacks with equippable component and asset_id (FIXED). LegendaryArmorListener.java (38,729 B) implements all set bonuses and piece passives. LegendaryLootListener.java (22,565 B) handles boss, structure, and chest drop tables. InfinityStoneManager.java (8,029 B) — 6 stone types (Power/Space/Reality/Soul/Time/Mind), fragment creation, finished stone creation, anvil combining. InfinityGauntletManager.java (23,370 B) — Thanos Glove, Infinity Gauntlet creation, snap ability (kills 50% hostiles in dimension, 300s cooldown), Thanos boss fight (800 HP, 2 phases).

C.10 menu Package (2 files)
CreativeMenuManager.java (45,503 B) — multi-page GUI inventory menus for browsing all weapons, armor, power-ups, and Infinity Stones by category and tier. GuideBookManager.java (29,396 B) — generates multi-volume written books explaining all plugin systems in Portuguese.

C.11 mobs Package (7 files)
BloodMoonManager.java (12,329 B) — 15% nightly chance, red sky visual effect, mob stat buffs, boss spawn every 10th kill, double drops, 0.1% Infinity Stone fragment chance from any kill. BossEnhancer.java (19,995 B) — scaling and custom abilities for vanilla bosses (Dragon 3.5× HP, Wither 1.0×, Warden 1.0×, Elder Guardian 2.0×). BossMasteryManager.java (12,294 B) — tracks participation in boss kills, awards permanent titles: Wither Slayer (+5% damage), Guardian Slayer (+7%), Warden Slayer (+10%), Dragon Slayer (+15%), Abyssal Conqueror (+20%), God Slayer (+25% damage + 20% DR). KingMobManager.java (5,972 B) — every 100th mob spawn becomes a King mob with 7× HP, 2.5× damage, drops diamonds. MobDifficultyManager.java (4,744 B) — distance-from-spawn scaling. BiomeMultipliers.java (3,080 B) — biome-specific scaling lookups. RoamingBossManager.java (57,427 B) — 6 roaming world bosses: The Watcher (Deep Dark biomes, Warden base, 800 HP, EPIC drops, Darkness/Mining Fatigue aura), Hellfire Drake (Nether, Ghast base, 600 HP, EPIC drops, triple fireball salvo), Frostbound Colossus (Snowy biomes, Iron Golem base, 700 HP, EPIC drops, freeze aura + ground pound), Jungle Predator (Jungle, Ravager base, 500 HP, RARE/EPIC drops, stealth pounce + poison fog), End Wraith (End outer islands, Phantom size 6, 900 HP, MYTHIC drops, void beam + phase shift + shadow clones at 50% HP), Abyssal Leviathan (Abyss dimension, 1200 HP, ABYSSAL drops). Each has 2–4% spawn chance per 2-minute check cycle, auto-despawn after 10 minutes, custom particle effects, and multi-stage attack patterns.

C.12 powerups Package (2 files)
PowerUpManager.java (26,207 B) — 7 consumable power-ups: Heart Crystal (RED_DYE, +2 max HP, max 40 uses = +80 HP), Soul Fragment (PURPLE_DYE, +1% damage, max 100 = +100%), Titan's Resolve (IRON_NUGGET, +10% knockback resistance + 2% DR, max 5 = 50% KB resist + 10% DR), Phoenix Feather (FEATHER, auto-revive on death, stackable), KeepInventorer (ENDER_EYE, permanent keep-inventory, one-time), Vitality Shard (PRISMARINE_SHARD, +5% DR, max 10 = 50% DR), Berserker's Mark (BLAZE_POWDER, +3% attack speed, max 10 = 30%). Maximum total passive DR capped at 75%. Uses string-based CustomModelDataComponent. PowerUpListener.java (5,607 B) handles right-click consumption and death event integration (Phoenix Feather auto-revive, KeepInventory enforcement).

C.13 quests Package (3 files)
QuestManager.java (27,940 B) — 6 quest lines (Overworld, Nether, End, Special, Boss, Explorer) with multi-step tracking, reward distribution, and scheduler. QuestProgressListener.java (3,231 B) monitors kills, exploration events, and item collection for quest progress. NpcWizardManager.java (15,501 B) — spawns the Archmage NPC (custom Villager with purple robes) who sells exclusive MYTHIC weapons (rotating stock of 3 for 48 diamonds + 16 Nether Stars), Heart Crystals, Phoenix Feathers, KeepInventorer (64 diamonds + 32 emeralds), and Blessing Crystals.

C.14 structures Package (5 files)
StructureType.java (10,780 B) — enum of 40 structure types: 24 Overworld (including Thanos Temple, Pillager Fortress, Pillager Airship, Frost Dungeon, Bandit Hideout, Sunken Ruins, Cursed Graveyard, Sky Altar, Ancient Library, Abandoned Mine, Dark Tower, Crystal Cave, Ruined Castle, Witch Hut, and others), 7 Nether, 5 End, 3 Abyss (Abyssal Castle 120×80×120, Void Nexus, Shattered Cathedral), and the Ender Dragon Death Chest. Generation chances range from 0.8% (COMMON) to 0.05% (ABYSSAL) per chunk. StructureManager.java (49,819 B) — chunk-based generation with 300-block minimum spacing, biome filtering, and programmatic block placement. StructureBuilder.java (8,714 B) — block placement API. StructureLootPopulator.java (8,554 B) — fills chests per structure type and tier. StructureBossManager.java (14,554 B) — spawns mini-bosses at structures, manages boss health bars and tracking.

C.15 utility Package (7 files)
BestBuddiesListener.java (11,609 B) — wolf/dog armor system giving tamed wolves 95% damage reduction. DropRateListener.java (3,289 B) — 35% trident drop rate boost, breeze wind charge drops. EnchantTransferListener.java (7,135 B) — transfer enchantments between items at anvil. InventorySortListener.java (5,282 B) — shift-click on empty inventory slot to auto-sort. LootBoosterListener.java (8,991 B) — enhanced chest and mob loot generation. PaleGardenFogTask.java (1,356 B) — applies fog effect to players in Pale Garden biome. VillagerTradeListener.java (4,244 B) — 50% price reduction on villager trades and removes trade locking.

C.16 weapons Package (13 files)
11 battle weapon types, each in its own manager: BattleSwordManager.java (6,821 B), BattleAxeManager.java (8,303 B), BattleBowManager.java (4,871 B), BattleMaceManager.java (4,279 B), BattleShovelManager.java (8,451 B), BattlePickaxeManager.java (7,168 B), BattleSpearManager.java (8,339 B), BattleTridentManager.java (4,737 B), SickleManager.java (8,061 B), SpearManager.java (11,128 B), SuperToolManager.java (22,176 B). WeaponAbilityListener.java (81,738 B) implements 20 non-legendary weapon abilities. WeaponMasteryManager.java (9,803 B) — kill-based mastery levels: Novice (0 kills), Fighter (100, +1% damage), Experienced Fighter (500, +5%), Master (1000, +15%).

D. ABYSS DIMENSION — CURRENT BUGS AND ROOT CAUSE ANALYSIS
D.1 Bug: Ender Dragon Health Bar Appears
Root cause: The committed code uses World.Environment.NORMAL but the world level.dat may still contain THE_END from a previous creation. The environment type is baked into level.dat at world creation time and changing the code after the world already exists has no effect. Alternatively, if the world was successfully recreated with NORMAL, the user wants the dark void sky of THE_END anyway (they said the lighting is too bright and want it to feel like the End). The fix is to use THE_END environment and aggressively remove the vanilla Ender Dragon and its boss bar via a repeating task.

D.2 Bug: Abyssal Dragon Does Not Spawn (Chicken Appears Instead)
Root cause confirmed from code: In AbyssDragonModel.java, the spawn() method calls Objects.requireNonNull(zombie.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(maxHp) followed by zombie.setHealth(maxHp) inside the entity spawn lambda callback. On Paper 1.21+, the setHealth() call validates that the value does not exceed MAX_HEALTH, but the attribute modification hasn't been flushed yet at that point in entity initialization. This throws an IllegalArgumentException from Guava's Preconditions.checkArgument. The exception propagates up and leaves the active flag stuck at true in AbyssDragonBoss.java (no try-catch exists in the committed startFight() method), which is why subsequent /jglims abyss boss commands respond "The Abyssal Dragon is already active!" The "chicken" appearance is likely a default entity that Paper substitutes when the spawn callback crashes.

Fix required: Move setAttribute(MAX_HEALTH) and setHealth() outside the spawn lambda, after the entity is fully constructed. Wrap the spawn call in AbyssDragonBoss.startFight() with try-catch to reset active on failure.

D.3 Bug: Nether Bricks on Citadel Front and Roof
Root cause confirmed from code: AbyssCitadelBuilder.java line ~46 defines NB = Material.NETHER_BRICKS. The previous fix-all-v2.ps1 was supposed to change this to DEEPSLATE_BRICKS, but the committed code still has NETHER_BRICKS. The PowerShell regex replacement failed silently due to an em-dash encoding mismatch (Unicode \u2014 in the comment vs the ASCII dash the regex expected). Every method that uses NB — including buildFrontFacade(), buildRoofLine(), and nave walls — places nether bricks.

D.4 Bug: Plain Front Wall
Root cause: The buildFrontFacade() method exists and has extensive gothic architecture code (pointed arch, rose window, entrance, buttress towers), but NB material makes large sections appear as plain brown nether bricks instead of the intended dark deepslate aesthetic. Additionally, the facade design may need architectural expansion (wider, taller, more decorative elements, a dramatic entrance gate).

D.5 Bug: Roof Holes and Bedrock Altar
Root cause: The buildRoofLine() method uses NB as a fallback material and for high-progress values. The coverage formula likely doesn't account for all blocks at the apex and edges, leaving gaps. The bedrock End portal altar is a vanilla artifact from the THE_END environment that auto-generates End spawn structures (the 5-block bedrock platform + end portal frame). Switching to THE_END and removing this structure programmatically, or using the chunk generator to override it, will fix it.

D.6 Bug: active Flag Not Reset on Spawn Failure
Root cause: AbyssDragonBoss.startFight() does not have a try-catch around dragonModel.spawn(). When the spawn throws, active stays true permanently until server restart.

E. COMPLETE FIX PLAN (WHAT THE NEXT SCRIPT MUST DO)
Fix 1 — AbyssDragonModel.java: Move MAX_HEALTH + setHealth() + SCALE attribute out of the spawn lambda. The zombie spawn lambda should only set properties that don't need post-initialization flushing (invisible, silent, no AI, no gravity, name, tags, etc.). After world.spawn() returns the Zombie reference, then set the health attributes.

Fix 2 — AbyssDragonBoss.java: Wrap the dragonModel.spawn(spawnLoc, DRAGON_HP) call in try-catch. On exception: log error, set active = false, call cleanupDragonEntities(world), notify trigger player, and return.

Fix 3 — AbyssDimensionManager.java: Change environment back to World.Environment.THE_END for the dark void sky. Add a repeating BukkitRunnable (every 40 ticks) that removes all vanilla EnderDragon entities, suppresses the vanilla dragon boss bar by iterating world players and hiding any ender dragon boss bars, and keeps time at 18000.

Fix 4 — AbyssCitadelBuilder.java: Change NB = Material.NETHER_BRICKS to NB = Material.DEEPSLATE_BRICKS. This single-line fix propagates to every method that uses the NB constant.

Fix 5 — Delete world_abyss: The world MUST be deleted on the server and the server restarted so it regenerates with the correct THE_END environment and the rebuilt citadel uses deepslate bricks.

F. DEPLOYMENT PROCEDURE
Build:

cd C:\Users\jgmel\Documents\projects\JGlimsPlugin\JGlimsPlugin
.\gradlew clean build
Deploy plugin + delete world:

Copy$sshKey = "C:\Users\jgmel\Documents\projects\server_minecraft\ssh-key-2026-02-25.key"
$remote = "ubuntu@144.22.198.184"
ssh -i $sshKey $remote "cd /home/ubuntu/minecraft-server && docker compose down"
ssh -i $sshKey $remote "sudo rm -rf /home/ubuntu/minecraft-server/data/world_abyss && echo 'world_abyss DELETED'"
ssh -i $sshKey $remote "sudo rm -f /home/ubuntu/minecraft-server/data/plugins/JGlimsPlugin*.jar"
scp -i $sshKey "build\libs\JGlimsPlugin-2.0.0.jar" "${remote}:/tmp/JGlimsPlugin-2.0.0.jar"
ssh -i $sshKey $remote "sudo cp /tmp/JGlimsPlugin-2.0.0.jar /home/ubuntu/minecraft-server/data/plugins/JGlimsPlugin-2.0.0.jar && sudo chown 1000:1000 /home/ubuntu/minecraft-server/data/plugins/JGlimsPlugin-2.0.0.jar"
ssh -i $sshKey $remote "cd /home/ubuntu/minecraft-server && docker compose up -d"
Update resource pack (when needed): Edit files in resourcepack-work\JGlimsResourcePack, zip with 7-Zip, get SHA-1, upload as new GitHub release, update docker-compose.yml env vars, recreate container.

G. REMAINING KNOWN BUGS (post armor/portal fix)
Abyss Dragon does not spawn — setHealth(maxHp) inside spawn lambda crashes (IllegalArgumentException). CRITICAL.
active flag stuck — no try-catch around spawn, subsequent commands say "already active." CRITICAL.
Ender Dragon health bar — world environment mismatch or lack of vanilla dragon cleanup. HIGH.
Nether bricks in citadel — NB = Material.NETHER_BRICKS still in code. HIGH.
Roof holes — buildRoofLine() coverage gaps and NB fallback. MEDIUM.
Bedrock altar on roof — vanilla End spawn structure from THE_END environment. MEDIUM.
Plain front facade — NB material + potential need for richer architecture. MEDIUM.
Dimension too bright — NORMAL environment gives overworld sky instead of void. MEDIUM.
Power-up textures — 7 items show vanilla (may need string CMD + missing PNGs). LOW.
Missing animated textures — 4 awakened weapons. LOW.
Creation Splitter ability bug — unidentified. LOW.
H. THINGS TO DO NEXT (UPDATED PRIORITY ORDER)
Fix Abyss Dragon spawn crash, active flag, environment, and NB material — the 5 fixes described in Section E. HIGHEST.
Improve citadel front facade architecture — after NB→DS fix, expand the facade with more gothic detail, dramatic entrance gate, additional blind arcading, candelabras, banners. HIGH.
Fix roof holes and remove bedrock altar — improve buildRoofLine() coverage, clear vanilla End structures. HIGH.
Fix power-up textures — update PowerUpManager to string CMD, verify item JSONs, create missing PNGs. MEDIUM.
Fix missing animated textures — Whisperwind, Excalibur Awakened, Creation Splitter, Edge of the Astral Plane. MEDIUM.
Fix Creation Splitter ability bug — identify and fix. MEDIUM.
Improve server performance — particle throttle expansion, entity limits, TPS monitoring. MEDIUM.
Create World Weapons — new weapon category between COMMON and MYTHIC legendaries. MEDIUM.
Create Magic Items — Wand of Wands and others. MEDIUM.
More structures, mini-bosses, world bosses — Phases 21, 26, 27. MEDIUM.
Lunar Dimension — moon-like reduced gravity. LOW.
Aether Dimension — classic Aether recreation. LOW.
I. MANDATORY DEVELOPMENT RULES
Always write Java files with [System.IO.File]::WriteAllText($path, $content, [System.Text.UTF8Encoding]::new($false)) to avoid BOM. Never use Compress-Archive for resource packs — use 7-Zip. Resource pack URL/SHA-1 changes go in docker-compose.yml, not server.properties. After docker-compose changes: docker compose down && docker compose up -d. SSH user is ubuntu, not opc. Each resource pack update gets a new major version (never overwrite). Use Adventure API exclusively (no deprecated ChatColor). All custom item metadata via PersistentDataContainer with NamespacedKey. String-based CustomModelDataComponent for all item textures. Two abilities per weapon (primary = right-click, alternate = crouch + right-click). Particle budgets per tier: COMMON 10, RARE 25, EPIC 50, MYTHIC 100, ABYSSAL 200. All legendary items are Unbreakable. Resource pack pack_format: 75. Max source file size ~120 KB; split if exceeding.

END OF DEFINITIVE PROJECT SUMMARY v11.0
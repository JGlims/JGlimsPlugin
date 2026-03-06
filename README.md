JGLIMSPLUGIN — DEFINITIVE PROJECT SUMMARY v9.0
Compiled: 2026-03-06 | Author: JGlims (jg.melo.lima2005@gmail.com) | Plugin Version: 1.4.0 | Target Version: 3.0.0

A. PROJECT METADATA
Repository: https://github.com/JGlims/JGlimsPlugin Latest Commit: c0ba7e0a2691be2c0fb7f59d991020b00594859b (2026-03-06T13:07:35Z, message: "adding the README") Commit URL: https://github.com/JGlims/JGlimsPlugin/commit/c0ba7e0a2691be2c0fb7f59d991020b00594859b Tree SHA: 4e5b53821d0b93dc24830a8d24710f217be1af5e Parent Commit: 866bf96d2aad8674618901380cc89c5c2d27ddc4 API Endpoints: https://api.github.com/repos/JGlims/JGlimsPlugin/commits?per_page=1 | https://api.github.com/repos/JGlims/JGlimsPlugin/git/trees/{sha}?recursive=1 Raw File Base: https://raw.githubusercontent.com/JGlims/JGlimsPlugin/main/

Server Environment: Docker container mc-crossplay on Oracle Cloud. IP 144.22.198.184, Java port 25565, Bedrock port 19132. Paper 1.21.11, Java 21, GeyserMC + Floodgate v2.2.5-SNAPSHOT. Resource pack served via server.properties. SSH Key: C:\Users\jgmel\Documents\projects\server_minecraft\ssh-key-2026-02-25.key Local Dev Path: C:\Users\jgmel\Documents\projects\JGlimsPlugin\JGlimsPlugin Resource Pack Dev Path: C:\Users\jgmel\Documents\projects\resourcepack-work\JGlimsResourcePack Resource Pack Download: https://github.com/JGlims/JGlimsPlugin/releases/download/v1.4.0-rp/JGlimsResourcePack.zip (pack_format 75, 621 entries, SHA-1 217bc25c174d...) Build Output: JGlimsPlugin-1.4.0.jar (349,791 bytes)

B. MANDATORY DEVELOPMENT RULES (A.1–A.18)
A.1 Always verify the latest commit via GitHub API before any edit. A.2 Fetch raw file contents from GitHub before modifying. A.3 Send full file replacements (never partial diffs). Example target: src/main/java/com/jglims/plugin/legendary/LegendaryWeaponManager.java. A.4 Write files with PowerShell [System.IO.File]::WriteAllText($path, $content, [System.Text.Encoding]::UTF8). A.5 Delete old JAR from Docker before copying new one. A.6 Use Adventure API exclusively (no § color codes, no deprecated ChatColor). A.7 Store all tunable numeric/string values in ConfigManager.java and config.yml. A.8 Register all listeners in onEnable() inside JGlimsPlugin.java. A.9 Use PersistentDataContainer with NamespacedKey(plugin, "key") for all custom item metadata. A.10 Legendary abilities: two per weapon — primary (right-click) and alternate (crouch + right-click); the old "hold" input system is deprecated and must be replaced. A.11 Particle budgets per tier: COMMON 10, RARE 25, EPIC 50, MYTHIC 100, ABYSSAL 200 particles per burst. A.12 Keep exactly three trident-type legendaries (Neptune's Fang, Tidecaller, Stormfork). A.13 Resource pack pack.mcmeta must use pack_format: 75. A.14 Item JSON files in assets/minecraft/items/*.json use string-based custom_model_data. A.15 Hide default item attributes with AttributeModifier overrides. A.16 All legendary items must be indestructible (Unbreaking X + Unbreakable: true NBT). A.17 File-size limit per source file: ~120 KB; split if exceeding. A.18 Full build-and-deploy PowerShell script:

Copy# phase7.ps1 (abridged)
cd "C:\Users\jgmel\Documents\projects\JGlimsPlugin\JGlimsPlugin"
.\gradlew clean build
$jar = "build\libs\JGlimsPlugin-1.4.0.jar"
$key = "C:\Users\jgmel\Documents\projects\server_minecraft\ssh-key-2026-02-25.key"
$remote = "opc@144.22.198.184"
scp -i $key $jar "${remote}:/tmp/JGlimsPlugin.jar"
ssh -i $key $remote "docker cp /tmp/JGlimsPlugin.jar mc-crossplay:/data/plugins/JGlimsPlugin.jar"
ssh -i $key $remote "docker exec mc-crossplay rm -f /data/plugins/JGlimsPlugin-*.jar.old"
ssh -i $key $remote "docker restart mc-crossplay"
Start-Sleep -Seconds 30
ssh -i $key $remote "docker exec mc-crossplay rcon-cli tps"
ssh -i $key $remote "docker logs --tail 50 mc-crossplay 2>&1 | Select-String -Pattern 'ERROR|WARN|JGlims'"
C. CURRENT CODE BASE (42 Java files + 2 resources + 1 PowerShell script ≈ 609 KB)
C.1 File Inventory
Package	File	Size	Description
(root)	JGlimsPlugin.java	17,964 B	Main class, onEnable(), all listener/manager registration
blessings	BlessingListener.java	3,452 B	Blessing activation events
blessings	BlessingManager.java	10,296 B	C-Bless (heal), Ami-Bless (dmg), La-Bless (def)
config	ConfigManager.java	29,211 B	All tunable values from config.yml
crafting	RecipeManager.java	28,998 B	Custom recipes (battle tools, super tools, legendaries)
crafting	VanillaRecipeRemover.java	588 B	Removes conflicting vanilla recipes
enchantments	AnvilRecipeListener.java	32,629 B	Custom anvil combining logic
enchantments	CustomEnchantManager.java	8,853 B	64 custom enchantment definitions
enchantments	EnchantmentEffectListener.java	69,495 B	All enchantment effect implementations
enchantments	EnchantmentType.java	1,942 B	Enum of enchantment types
enchantments	SoulboundListener.java	7,961 B	Soulbound enchantment (keep on death)
guilds	GuildListener.java	1,456 B	Guild event hooks
guilds	GuildManager.java	13,679 B	Guild CRUD, invites, friendly fire toggle
legendary	LegendaryAbilityListener.java	109,146 B	88 abilities (NEEDS FULL REWRITE)
legendary	LegendaryLootListener.java	22,502 B	Drop tables for bosses & structures
legendary	LegendaryWeapon.java	10,450 B	Enum of 44 weapons + LegendaryTier inner enum
legendary	LegendaryWeaponManager.java	9,741 B	Weapon creation, identification, damage application
mobs	BiomeMultipliers.java	3,080 B	Biome-specific health/damage scaling
mobs	BloodMoonManager.java	12,103 B	Blood Moon event logic
mobs	BossEnhancer.java	3,345 B	Boss stat scaling (Dragon, Wither, etc.)
mobs	KingMobManager.java	5,972 B	King mob spawning (1 per N spawns)
mobs	MobDifficultyManager.java	4,744 B	Distance-based difficulty scaling
utility	BestBuddiesListener.java	11,609 B	Wolf/dog armor system
utility	DropRateListener.java	3,289 B	Trident drop rate boost, breeze charges
utility	EnchantTransferListener.java	7,135 B	Transfer enchantments between items
utility	InventorySortListener.java	5,282 B	Auto-sort inventory
utility	LootBoosterListener.java	8,991 B	Enhanced chest loot, mob drops
utility	PaleGardenFogTask.java	1,356 B	Fog effect in Pale Garden biome
utility	VillagerTradeListener.java	4,244 B	Villager trade modifications
weapons	BattleAxeManager.java	8,303 B	Battle axe creation/abilities
weapons	BattleBowManager.java	4,871 B	Battle bow
weapons	BattleMaceManager.java	4,279 B	Battle mace
weapons	BattlePickaxeManager.java	7,168 B	Battle pickaxe + ore detect
weapons	BattleShovelManager.java	8,451 B	Battle shovel
weapons	BattleSpearManager.java	8,339 B	Battle spear
weapons	BattleSwordManager.java	6,821 B	Battle sword
weapons	BattleTridentManager.java	4,737 B	Battle trident
weapons	SickleManager.java	8,061 B	Sickle weapon type
weapons	SpearManager.java	11,128 B	Spear weapon type
weapons	SuperToolManager.java	22,176 B	Diamond/netherite super tools with +2%/enchant
weapons	WeaponAbilityListener.java	81,738 B	20 non-legendary weapon abilities
weapons	WeaponMasteryManager.java	9,803 B	Kill-based mastery bonuses
resources	config.yml	3,851 B	All tunable settings
resources	plugin.yml	704 B	Plugin metadata (v1.4.0, api-version 1.21)
(root)	phase7.ps1	8,283 B	Build/deploy script
C.2 Custom Enchantments (64 total, 12 categories)
Swords (8): Lifesteal, Venom, Thunder Strike, Frost Aspect, Execute, Berserk, Whirlwind, Vampiric. Axes (6): Timber, Lumberjack, Splitter, Cleave, Skull Splitter, Devastating. Pickaxes (6): Vein Miner, Blast Mining, Auto Smelt, Ore Magnet, Spelunker, Shatter. Shovels (5): Tunnel Bore, Excavate, Path Maker, Magnetic, Earthmover. Hoes/Sickles (4): Harvest, Replant, Scythe, Growth. Bows (6): Explosive Arrow, Homing, Multishot Plus, Poison Arrow, Frost Arrow, Sniper. Crossbows (5): Rapid Fire, Grapple, Chain Lightning, Volley, Piercing Plus. Tridents (5): Tempest, Lightning Rod, Riptide Plus, Aqua Affinity, Sea's Blessing. Armor (8): Thorns Plus, Fire Walker, Molten Core, Last Stand, Dodge, Regeneration, Absorption, Vitality. Elytra (3): Rocket Boost, Wind Rider, Featherfall Plus. Maces (4): Earthquake, Stun, Graviton, Pulverize. Spears (2): Impale Plus, Javelin. Universal (2): Soulbound, Mending Plus. Conflict pairs defined in CustomEnchantManager.java.

D. TIER SYSTEM (Revised — NO progression gates)
No progression gates. All content is available from the start. Tier determines only stats, particle effects, and drop rarity.

Tier	Color	Base Damage	Primary CD	Alt CD	Particles/Burst	Drop Sources
COMMON	§f White	10–13	5–8s	15–25s	10	Overworld structures, any hostile mob (rare)
RARE	§a Green	12–15	7–10s	20–30s	25	Nether structures, Elder Guardian, Nether bosses
EPIC	§5 Purple	14–17	8–12s	25–45s	50	Warden, Wither, Blood Moon King
MYTHIC	§6 Gold	16–22	10–15s	30–60s	100	Ender Dragon, End City chests, End Rift, NPC Wizard
ABYSSAL	§c Dark Red	22–30	12–18s	45–90s	200	Abyss Dragon, Abyss dimension structures
E. WEAPON TABLES
E.1 Current Weapons in Code (44 weapons from LegendaryWeapon.java)
All weapons currently use the old LegendaryTier.LEGENDARY or LegendaryTier.UNCOMMON inner enum. The new 5-tier system requires replacing this inner enum with a standalone LegendaryTier.java enum containing COMMON, RARE, EPIC, MYTHIC, ABYSSAL.

#	Enum ID	Display Name	Material	Dmg	CMD	Current Tier	Texture	Primary Ability	Alt Ability	PCD	ACD
1	OCEANS_RAGE	Ocean's Rage	DIAMOND_SWORD→TRIDENT	14	30001	LEGENDARY→RARE	stormbringer	Stormbringer	Tidal Crash / Riptide Surge	8	15
2	AQUATIC_SACRED_BLADE	Aquatic Sacred Blade	DIAMOND_SWORD	13	30002	LEGENDARY→RARE	aquantic_sacred_blade	Aqua Heal	Depth Pressure	20	25
3	TRUE_EXCALIBUR	True Excalibur	DIAMOND_SWORD	16→20	30003	LEGENDARY→MYTHIC	excalibur	Holy Smite	Divine Shield	10	45
4	REQUIEM_NINTH_ABYSS	Requiem of the Ninth Abyss	DIAMOND_SWORD	15→20	30004	LEGENDARY→MYTHIC	requiem_of_hell	Soul Devour	Abyss Gate	12	60
5	ROYAL_CHAKRAM	Royal Chakram	DIAMOND_SWORD	12	30005	LEGENDARY→RARE	royalchakram	Chakram Throw	Spinning Shield	6	20
6	BERSERKERS_GREATAXE	Berserker's Greataxe	DIAMOND_AXE	17	30006	LEGENDARY→EPIC	berserkers_greataxe	Berserker Slam	Blood Rage	10	30
7	ACIDIC_CLEAVER	Acidic Cleaver	DIAMOND_AXE	14	30007	LEGENDARY→RARE	treacherous_cleaver	Acid Splash	Corrosive Aura	10	25
8	BLACK_IRON_GREATSWORD	Black Iron Greatsword	DIAMOND_SWORD	15	30008	LEGENDARY→EPIC	black_iron_greatsword	Dark Slash	Iron Fortress	8	30
9	MURAMASA	Muramasa	DIAMOND_SWORD	13	30009	LEGENDARY→RARE	muramasa	Crimson Flash	Bloodlust	6	20
10	PHOENIXS_GRACE	Phoenix's Grace	DIAMOND_AXE	15→20	30010	LEGENDARY→MYTHIC	gilded_phoenix_greataxe	Phoenix Strike	Rebirth Flame	10	120
11	SOUL_COLLECTOR	Soul Collector	DIAMOND_SWORD	14→19	30011	LEGENDARY→MYTHIC	soul_collector	Soul Harvest	Spirit Army	8	30
12	AMETHYST_SHURIKEN	Amethyst Shuriken	DIAMOND_SWORD	11	30012	LEGENDARY→COMMON	amethyst_shuriken	Shuriken Barrage	Shadow Step	7	15
13	VALHAKYRA	Valhakyra	DIAMOND_SWORD	15→18	30013	LEGENDARY→MYTHIC	valhakyra	Valkyrie Dive	Wings of Valor	12	25
14	WINDREAPER	Windreaper	DIAMOND_SWORD	13	30014	LEGENDARY→RARE	windreaper	Gale Slash	Cyclone	8	20
15	PHANTOMGUARD	Phantomguard Greatsword	DIAMOND_SWORD	14→19	30015	LEGENDARY→MYTHIC	phantomguard_greatsword	Spectral Cleave	Phase Shift	10	35
16	MOONLIGHT	Moonlight	DIAMOND_SWORD	13	30016	LEGENDARY→RARE	moonlight	Lunar Beam	Eclipse	10	30
17	ZENITH	Zenith	DIAMOND_SWORD	18→22	30017	LEGENDARY→MYTHIC	zenith	Final Judgment	Ascension	15	60
18	SOLSTICE	Solstice	DIAMOND_SWORD	14	30018	LEGENDARY→EPIC	solstice	Solar Flare	Daybreak	10	25
19	GRAND_CLAYMORE	Grand Claymore	DIAMOND_SWORD	16	30019	LEGENDARY→EPIC	grand_claymore	Titan Swing	Colossus Stance	10	30
20	CALAMITY_BLADE	Calamity Blade	DIAMOND_AXE	15	30020	LEGENDARY→EPIC	calamity_blade	Cataclysm	Doomsday	12	35
21	DRAGON_SWORD	Dragon Sword	DIAMOND_SWORD	14→18	30021	LEGENDARY→MYTHIC	dragon_sword	Dragon Breath	Draconic Roar	10	25
22	TALONBRAND	Talonbrand	DIAMOND_SWORD	13	30022	LEGENDARY→RARE	talonbrand	Talon Strike	Predator's Mark	8	20
23	EMERALD_GREATCLEAVER	Emerald Greatcleaver	DIAMOND_AXE	16	30023	LEGENDARY→EPIC	emerald_greatcleaver	Emerald Storm	Gem Barrier	10	40
24	DEMONS_BLOOD_BLADE	Demon's Blood Blade	DIAMOND_SWORD	15	30024	LEGENDARY→EPIC	demons_blood_blade	Blood Rite	Demonic Form	8	35
25	NOCTURNE	Nocturne	DIAMOND_SWORD	12→18	30025	UNCOMMON→MYTHIC	nocturne	Shadow Slash	Night Cloak	7	20
26	GRAVESCEPTER	Gravescepter	DIAMOND_SWORD	11	30026	UNCOMMON→COMMON	revenants_gravescepter	Grave Rise	Death's Grasp	15	18
27	LYCANBANE	Lycanbane	DIAMOND_SWORD	12	30027	UNCOMMON→COMMON	lycanbane	Silver Strike	Hunter's Sense	8	20
28	GLOOMSTEEL_KATANA	Gloomsteel Katana	DIAMOND_SWORD	11	30028	UNCOMMON→COMMON	gloomsteel_katana	Quick Draw	Shadow Stance	5	18
29	VIRIDIAN_CLEAVER	Viridian Cleaver	DIAMOND_AXE	13	30029	UNCOMMON→COMMON	viridian_greataxe	Verdant Slam	Overgrowth	8	22
30	CRESCENT_EDGE	Crescent Edge	DIAMOND_AXE	12	30030	UNCOMMON→COMMON	crescent_greataxe	Lunar Cleave	Crescent Guard	7	20
31	GRAVECLEAVER	Gravecleaver	DIAMOND_SWORD	12	30031	UNCOMMON→COMMON	revenants_gravecleaver	Bone Shatter	Undying Rage	10	45
32	AMETHYST_GREATBLADE	Amethyst Greatblade	DIAMOND_SWORD	11	30032	UNCOMMON→COMMON	amethyst_greatblade	Crystal Burst	Gem Resonance	8	25
33	FLAMBERGE	Flamberge	DIAMOND_SWORD	12	30033	UNCOMMON→COMMON	flamberge	Flame Wave	Ember Shield	8	18
34	CRYSTAL_FROSTBLADE	Crystal Frostblade	DIAMOND_SWORD	11	30034	UNCOMMON→COMMON	crystal_frostblade	Frost Spike	Permafrost	7	22
35	DEMONSLAYER	Demonslayer	DIAMOND_SWORD	13	30035	UNCOMMON→COMMON	demonslayers_greatsword	Holy Rend	Purifying Aura	8	20
36	VENGEANCE	Vengeance	DIAMOND_SWORD	10	30036	UNCOMMON→COMMON	vengeance_blade	Retribution	Grudge Mark	12	15
37	OCULUS	Oculus	DIAMOND_SWORD	11	30037	UNCOMMON→COMMON	oculus	All-Seeing Strike	Third Eye	8	25
38	ANCIENT_GREATSLAB	Ancient Greatslab	DIAMOND_SWORD	13	30038	UNCOMMON→COMMON	ancient_greatslab	Seismic Slam	Stone Skin	9	22
39	NEPTUNES_FANG	Neptune's Fang	TRIDENT	12	30039	UNCOMMON→COMMON	frostaxe→neptunes_fang	Riptide Slash	Maelstrom	7	22
40	TIDECALLER	Tidecaller	TRIDENT	11	30040	UNCOMMON→COMMON	aquantic_sacred_blade→tidecaller	Tidal Spear	Depth Ward	8	20
41	STORMFORK	Stormfork	TRIDENT	13	30041	UNCOMMON→COMMON	stormbringer→stormfork	Lightning Javelin	Thunder Shield	10	25
42	JADE_REAPER	Jade Reaper	DIAMOND_HOE	12	30042	UNCOMMON→COMMON	jadehalberd	Jade Crescent	Emerald Harvest	7	30
43	VINDICATOR	Vindicator	DIAMOND_AXE	11	30043	UNCOMMON→COMMON	vindicator	Executioner's Chop	Rally Cry	8	25
44	SPIDER_FANG	Spider Fang	DIAMOND_SWORD	10	30044	UNCOMMON→COMMON	spider_sword	Web Trap	Wall Crawler	8	20
E.2 New Weapons To Add (Planned #45–#63, all at least MYTHIC, drop from Ender Dragon / End City / End Rift)
#	Enum ID	Display Name	Material	Dmg	CMD	Tier	Primary Ability	Alt Ability	PCD	ACD	Drop Source
45	DIVINE_AXE_RHITTA	Divine Axe Rhitta	DIAMOND_AXE	22	30045	MYTHIC	Cruel Sun	Sunshine	12	50	Ender Dragon
46	YORU	Yoru	DIAMOND_SWORD	20	30046	MYTHIC	World's Strongest Slash	Dark Mirror	14	55	End City chest
47	TENGENS_BLADE	Tengen's Blade	DIAMOND_SWORD	19	30047	MYTHIC	Sound Breathing	Constant Flux	10	40	End Rift boss
48	EDGE_ASTRAL_PLANE	Edge of the Astral Plane	DIAMOND_SWORD	21	30048	MYTHIC	Astral Rend	Planar Shift	13	60	Ender Dragon
49	FALLEN_GODS_SPEAR	Fallen God's Spear	DIAMOND_SWORD	20	30049	MYTHIC	Divine Impale	Heaven's Fall	11	50	End City chest
50	NATURE_SWORD	Nature Sword	DIAMOND_SWORD	18	30050	MYTHIC	Gaia's Wrath	Overgrowth Surge	10	40	End City chest
51	HEAVENLY_PARTISAN	Heavenly Partisan	DIAMOND_SWORD	19	30051	MYTHIC	Holy Lance	Celestial Judgment	11	45	Ender Dragon
52	SOUL_DEVOURER	Soul Devourer	DIAMOND_SWORD	20	30052	MYTHIC	Soul Rip	Devouring Maw	12	50	End Rift boss
53	MJOLNIR	Mjölnir	DIAMOND_AXE→MACE	22	30053	MYTHIC	Thunderstrike	Bifrost Slam	14	55	Ender Dragon
54	THOUSAND_DEMON_DAGGERS	Thousand Demon Daggers	DIAMOND_SWORD	18	30054	MYTHIC	Demon Barrage	Infernal Dance	8	35	End City chest
55	STAR_EDGE	Star Edge	DIAMOND_SWORD	20	30055	MYTHIC	Cosmic Slash	Supernova	13	55	End Rift boss
56	RIVERS_OF_BLOOD	Rivers of Blood	DIAMOND_SWORD	19	30056	MYTHIC	Corpse Piler	Blood Tsunami	10	40	Ender Dragon
57	DRAGON_SLAYING_BLADE	Dragon Slaying Blade	DIAMOND_SWORD	20	30057	MYTHIC	Dragon Pierce	Slayer's Fury	12	50	End City chest
58	STOP_SIGN	Stop Sign	DIAMOND_AXE	18	30058	MYTHIC	Full Stop	Road Rage	10	35	End Rift (rare)
59	CREATION_SPLITTER	Creation Splitter	DIAMOND_SWORD	22	30059	MYTHIC	Reality Cleave	Genesis Break	15	60	End Rift boss
Note: Phoenix's Grace (#10), True Excalibur (#3), Requiem of the Ninth Abyss (#4), Nocturne (#25), Soul Collector (#11), Phantomguard (#15), Valhakyra (#13), Zenith (#17), and Dragon Sword (#21) have all been upgraded to MYTHIC in the table above and their damage increased to 18–22.

E.3 ABYSSAL Weapons (Planned, Abyss Dimension)
#	Enum ID	Display Name	Dmg	CMD	Abilities	Drop Source
60	REQUIEM_AWAKENED	Requiem of the Ninth Abyss (Awakened)	28	30060	Abyssal Devour / Void Collapse	Abyss Dragon
61	EXCALIBUR_AWAKENED	True Excalibur (Awakened)	26	30061	Divine Annihilation / Sacred Realm	Abyss Dragon
62	CREATION_SPLITTER_AWAKENED	Creation Splitter (Awakened)	30	30062	Reality Shatter / Big Bang	Abyss dimension castle
63	WHISPERWIND_AWAKENED	Whisperwind (Awakened)	24	30063	Silent Storm / Phantom Cyclone	Abyss dimension castle
Abyssal weapon preview images: https://www.genspark.ai/api/files/s/CXakuBB7 and https://www.genspark.ai/api/files/s/pQthnEmC

E.4 Boss Drop Tables (Average 1–3 drops per kill)
Boss	Pool Size	Guaranteed Drops	Weapon Pool
Elder Guardian	1 weapon	1 guaranteed	Ocean's Rage (TRIDENT, RARE)
Wither	4 weapons	1–2 random	Berserker's Greataxe, Black Iron Greatsword, Calamity Blade, Demon's Blood Blade (all EPIC)
Warden	5 weapons	1–2 random	True Excalibur, Requiem, Grand Claymore, Emerald Greatcleaver, Solstice (EPIC–MYTHIC)
Ender Dragon	7 weapons	2–3 random + death chest	Phoenix's Grace, Divine Axe Rhitta, Edge of Astral Plane, Heavenly Partisan, Mjölnir, Rivers of Blood, Nocturne (all MYTHIC). Also drops: Abyssal Key (100%)
End Rift Boss	5 weapons	1–2 random	Tengen's Blade, Soul Devourer, Star Edge, Creation Splitter, Stop Sign (all MYTHIC)
Blood Moon King	3 weapons	1 random	Muramasa, Moonlight, Talonbrand (RARE) + chance for EPIC
Structure Mini-Bosses	Per structure	1 random from structure pool	Varies by structure (see Section F)
Abyss Dragon	4 weapons	2–3 random	All four ABYSSAL-tier awakened weapons
End City chest loot: 15% chance per chest to contain a MYTHIC weapon from pool: Yoru, Fallen God's Spear, Nature Sword, Thousand Demon Daggers, Soul Collector, Phantomguard, Dragon Slaying Blade.

F. CUSTOM STRUCTURES (First Addition — Code-Based, No Command Blocks)
All structures are generated programmatically using a StructureManager system. Each structure spawns a chest with tier-appropriate loot and an optional mini-boss. Structure generation frequency is configurable in config.yml; structures are common but not clustered (minimum 300 blocks between any two custom structures).

F.1 New Java Files Required
File	Package	Description
StructureManager.java	structures	Central registry, chunk-based generation, spacing rules
StructureBuilder.java	structures	Block placement API, schematic-like building
StructureLootPopulator.java	structures	Chest filling per structure type/tier
StructureBossManager.java	structures	Mini-boss spawning and tracking
StructureType.java	structures	Enum of all structure types
F.2 Overworld Structures
Structure	Biome	Blocks	Size	Mini-Boss	Loot Tier	Description
Ruined Colosseum	Plains/Savanna	Stone brick, cracked stone, mossy stone, iron bars	~40×25×40	Gladiator King (Iron Golem, 300 HP, custom texture)	EPIC	Circular arena with spectator stands, broken pillars, central chest
Druid's Grove	Forest/Dark Forest	Oak logs, moss, leaves, glowstone	~30×20×30	Ancient Treant (Zombie, 200 HP, custom texture)	RARE	Circular clearing with giant tree, vine-covered altar
Shrek House	Swamp	Oak wood, dirt, mushrooms, brown wool	~15×10×15	Shrek (Iron Golem, 400 HP, green-tinted, custom texture)	EPIC	Iconic swamp hut with chimney, outhouse nearby, onion garden
Mage Tower	Any	Stone brick, purple glass, bookshelves, End rods	~12×45×12	Arch Mage (Evoker, 250 HP, particle aura)	EPIC	Tall spiraling tower, enchanting room, library, roof observatory
Gigantic Castle	Plains/Mountains	Stone brick, deepslate, banners, iron bars	~80×50×80	Castle Lord (Vindicator, 350 HP, diamond armor)	MYTHIC	Multi-wing castle: throne room, dungeon, armory, towers, courtyard
Fortress	Taiga/Stony	Deepslate, blackstone, chains, lanterns	~50×30×50	Warlord (Piglin Brute, 300 HP, netherite armor)	EPIC	Defensive fortification with walls, watchtowers, barracks, vault
Camping Station (Small)	Any Overworld	Campfire, logs, tents (wool), fences	~10×5×10	None	COMMON	Small camp with fire, tent, and supply chest
Camping Station (Large)	Any Overworld	Multiple campfires, wagons, hay bales	~20×8×20	Bandit Leader (Pillager, 150 HP)	RARE	Larger caravan-style camp with wagon chests
Ultra Village	Plains/Desert/Savanna	Vanilla village blocks + stone walls, iron doors	~100×20×100	None (spawns 4 Iron Golems + enhanced guards)	RARE	Larger walled village, more houses, NPC Wizard villager, vulnerable to enhanced Pillager raids
Witch House (Swamp)	Swamp	Dark oak, purple wool, cauldrons, cobwebs	~12×12×12	Coven Witch (Witch, 180 HP, poison aura)	RARE	Dark cottage with brewing room, cauldron pit
Witch House (Forest)	Dark Forest	Spruce, dead bushes, jack-o-lanterns	~10×10×10	Shadow Witch (Witch, 150 HP, invisibility)	COMMON	Creepy forest cabin
Allay Sanctuary	Meadow/Flower Forest	Amethyst, copper, glow lichen, candles	~25×15×25	None (friendly: spawns 5 Allays)	RARE	Crystal grotto with musical chimes, Allay egg items
Volcano	Badlands/Mountains	Basalt, magma, lava, blackstone, obsidian	~40×60×40	Magma Titan (Magma Cube, 400 HP, massive size)	EPIC	Hollow mountain with lava core, internal chambers, summit crater
Ancient Temple	Jungle/Desert	Sandstone, prismarine, gold blocks, hieroglyphs	~35×25×35	Temple Guardian (Husk, 250 HP, golden armor)	EPIC	Pyramid-style with trapped corridors, puzzle rooms, treasure vault
Abandoned House	Any	Cobblestone, oak planks, cobwebs, broken glass (air)	~12×8×12	Restless Spirit (Phantom, 100 HP)	COMMON	Decayed house with collapsed roof, basement chest
House-Tree	Forest/Birch Forest	Oak/birch logs, planks, leaves, ladders	~15×25×15	None	COMMON	Treehouse built into a giant tree, rope bridges
Dungeon (Deep)	Underground (Y < 30)	Deepslate brick, iron bars, skulk	~30×15×30	Dungeon Keeper (Warden-like Zombie, 300 HP)	EPIC	Multi-room underground dungeon with spawners, treasure room
F.3 Nether Structures
Structure	Biome	Mini-Boss	Loot Tier	Description
Crimson Citadel	Crimson Forest	Crimson Warlord (Hoglin, 350 HP, fire aura)	EPIC	Nether brick fortress with crimson throne room, armory
Soul Sanctum	Soul Sand Valley	Soul Reaper (Wither Skeleton, 250 HP, soul fire attacks)	RARE	Soul-fire temple with chanting particles, soul chest
Basalt Spire	Basalt Deltas	Basalt Golem (Iron Golem variant, 400 HP, magma attacks)	EPIC	Towering obsidian/basalt pillar with internal spiral
Nether Dungeon	Nether Wastes	Blaze Lord (Blaze, 300 HP, multi-fireball)	RARE	Fortress-style dungeon with blaze spawners, chests
Piglin Palace	Crimson Forest	Piglin King (Piglin Brute, 400 HP, gold armor)	EPIC	Opulent gold-decorated structure with vaults
F.4 End Structures
Structure	Location	Mini-Boss	Loot Tier	Description
Void Shrine	End Islands	Void Sentinel (Enderman, 300 HP, teleport frenzy)	MYTHIC	Obsidian + purpur shrine on floating island
Ender Monastery	End Islands	Ender Monk (Shulker variant, 250 HP, levitation beams)	MYTHIC	Purpur + end stone library complex
Dragon's Hoard	Near main island	None (post-Dragon loot)	MYTHIC	Pile of gold blocks + 3 MYTHIC chests
End Rift Arena	Spawns via event	End Rift Dragon (see Section G)	MYTHIC	Portal-ringed arena for End Rift event
Ender Dragon Death Chest Structure	Main End island	None	MYTHIC	Small obsidian-and-purpur pedestal structure that spawns the Ender Dragon death chest (2–3 MYTHIC weapons, Abyssal Key)
G. EVENTS
G.1 Blood Moon (Overworld)
Triggers at nightfall with configurable chance (current: 15%). Red sky, all mobs get 1.5× HP and 1.3× damage. Every 10th mob spawned during Blood Moon is a Blood Moon Boss: a large white Wither Skeleton with blood particle effects (300 HP base × boss multiplier = 6,000 HP, 5× damage). Drops 5–15 diamonds + 1 RARE weapon + chance for EPIC. Double drops on all mobs during event. Secret item: during Blood Moon, any normal mob has a 0.1% chance to drop an Infinity Stone fragment (colored stone, custom texture, secret/undocumented item).

G.2 End Rift (Overworld)
10% chance to trigger when Ender Dragon is killed (no progression gates). A massive purple portal (End portal texture ring, 15×15) opens at a random location within 500 blocks of spawn. The rift spawns waves of Endermen, Shulkers, and End-variant mobs for 10 minutes, culminating in the End Rift Dragon — a variant Ender Dragon with: 600 HP, 2× damage, void breath attack (Wither effect + Blindness), lightning strikes, no end crystals, can summon Shulker turrets. Drops: 1–2 MYTHIC weapons from End Rift pool (Tengen's Blade, Soul Devourer, Star Edge, Creation Splitter, Stop Sign). The rift closes after the boss is killed or 15 minutes elapse.

G.3 Nether Events (NEW)
Nether Storm (Nether): 10% chance per Nether day cycle. Ghast swarms, enhanced Blaze spawns, fire rains from the ceiling. Lasts 5 minutes. Final boss: Infernal Overlord (Ghast, 400 HP, triple fireball). Drops EPIC weapons.

Piglin Uprising (Nether): 8% chance. Massive Piglin army spawns. Final boss: Piglin Emperor (Piglin Brute, 500 HP, golden mace). Drops EPIC weapons + gold.

G.4 End Events (NEW)
Void Collapse (End): 5% chance when player enters End. Void tentacles (custom particles) attack from below, pulling players down. Endermen become aggressive regardless of eye contact. Boss: Void Leviathan (Elder Guardian variant, 500 HP, void beam). Drops MYTHIC weapons.

G.5 Event-Quest Interaction
All events can trigger quest objectives for Quest Villagers (Section K). Example quest triggers: "Survive a Blood Moon," "Defeat the End Rift Dragon," "Collect 3 Infinity Stone fragments during Blood Moons."

H. MASTERY SYSTEM (Reworked)
H.1 Weapon Mastery
Title	Kills Required	Damage Bonus
Novice	0	+1%
Fighter	100	+5%
Experienced Fighter	500	+10%
Master	1,000	+15%
Mastery is tracked per weapon type (sword, axe, bow, etc.) via PersistentDataContainer. Stored in WeaponMasteryManager.java.

H.2 Boss Mastery Titles
Earned by dealing any damage to the boss before it dies. Each title grants passive resistance.

Title	Boss	Resistance Bonus
Wither Slayer	Wither	+5%
Guardian Slayer	Elder Guardian	+7%
Warden Slayer	Warden	+10%
Dragon Slayer	Ender Dragon	+15%
Abyssal Conqueror	Abyss Dragon	+20%
God Slayer	All 5 bosses killed	+25% resistance, +20% damage (REPLACES all other boss titles)
I. PERMANENT POWER-UPS (No Progression Gates)
Item	Effect	Max	Source	Mechanism
Heart Crystal	+1 max heart (2 HP) permanently	10 (20 extra HP)	Structure chests (EPIC+), mini-boss drops (10%), boss drops (25%)	Right-click to consume, stored in PersistentDataContainer
Soul Fragment	+0.5% permanent damage boost	50 (+25% total)	Mini-bosses (15%), structure mobs (2%), Blood Moon King (guaranteed 3)	Consumed on pickup, auto-applied
Blessing Crystals	C-Bless (heal), Ami-Bless (dmg), La-Bless (def)	10 uses each	Existing system, also in NPC Wizard shop and Abyss castle	Stack with Heart Crystals
Titan's Resolve	+10% knockback resistance permanently	1	Warden drop (20%)	One-time consumable
Phoenix Feather	Auto-revive once (consumed on death)	Stackable	Phoenix's Grace alt ability drop (5%), End City chests	Consumed on death
J. LEGENDARY ARMOR SETS
All legendary armor is indestructible (Unbreakable: true). Each piece has a passive enchantment-like effect.

J.1 Armor Sets (Planned)
Set Name	Tier	Total Defense	Set Bonus	Passive Effects	Drop Source
Abyssal Plate	ABYSSAL	28 (full set)	+30% damage, Wither immunity	Helmet: Night Vision; Chest: Thorns III equivalent; Legs: Speed I; Boots: Fire Walker	Abyss Dragon
Dragon Knight	MYTHIC	24	+20% vs dragon-type	Helmet: Respiration III; Chest: +4 HP; Legs: Knockback resistance; Boots: Feather Falling V	Ender Dragon death chest
Void Walker	MYTHIC	22	Teleport (crouch+jump)	Helmet: See invisible mobs; Chest: Ender pearl no damage; Legs: Slow Falling; Boots: No fall damage	End City chest / Void Shrine
Blood Moon	EPIC	20	Lifesteal 5%	Helmet: Glowing enemies at night; Chest: +2 HP per kill; Legs: Speed boost at night; Boots: Silent steps	Blood Moon King
Frost Warden	EPIC	20	Freeze nearby enemies	Helmet: Frost Resistance; Chest: Slowness aura; Legs: Ice Walk; Boots: Creates ice on water, obsidian on lava	Overworld structure (Frost Dungeon)
Nature's Embrace	EPIC	18	Passive regen in forests	Helmet: Poison immunity; Chest: Thorn damage (nature); Legs: Vine climb; Boots: Crop growth boost	Druid's Grove structure
Shadow Stalker	RARE	16	Invisibility while crouching (no armor visible)	Helmet: Dark Vision; Chest: +2 sneak damage; Legs: Silent movement; Boots: No footstep particles	Dungeon structure
J.2 Weak Normal Armors (Craftable early game)
Set	Defense	Material	Recipe
Copper Armor	10 (full)	Copper ingots	Standard armor pattern
Bone Armor	8	Bones + string	Standard pattern
Leather Reinforced	12	Leather + iron nuggets	Standard pattern
AI Image Prompts for Armor Textures:

Copper Armor: "Minecraft-style copper armor set texture sheet, 64x32, oxidized green-brown tones, riveted plates, pixel art, game asset, transparent background"

Bone Armor: "Minecraft-style bone armor set texture sheet, 64x32, white-ivory skeletal plates, rib cage chest piece, skull helmet, pixel art, game asset, transparent background"

Leather Reinforced: "Minecraft-style reinforced leather armor texture sheet, 64x32, brown leather with iron stud details, medieval ranger aesthetic, pixel art, game asset, transparent background"

K. INFINITY GAUNTLET
K.1 Crafting Chain
Step 1 — Obtain Thanos Glove: Defeat the Thanos structure boss. Thanos is a custom Iron Golem with purple/gold texture, 800 HP, ground-slam attack, beam attack. Spawns in the Thanos Temple structure (rare, Badlands/Mountains, deepslate + gold blocks + purple glass). Drops: Thanos Glove (100%).

Step 2 — Obtain Colored Stones: Six custom items with custom textures (16×16 pixel art, colored gem on transparent background). During Blood Moon, any normal mob has a 0.1% chance to drop a random colored stone. They are NOT documented in-game (secret items).

Stone	Color	Texture Name
Power Stone	Purple	infinity_stone_power
Space Stone	Blue	infinity_stone_space
Reality Stone	Red	infinity_stone_reality
Soul Stone	Orange	infinity_stone_soul
Time Stone	Green	infinity_stone_time
Mind Stone	Yellow	infinity_stone_mind
Step 3 — Craft Infinity Stones: Combine each Colored Stone + 1 Redstone in an Anvil → produces the finished Infinity Stone.

Step 4 — Craft Infinity Gauntlet: Combine the Thanos Glove + all 6 Infinity Stones in a custom crafting recipe (shapeless, uses RecipeManager).

K.2 Infinity Gauntlet Behavior
Right-click: kills 50% of all loaded hostile mobs in the user's current dimension. Excluded: bosses (Wither, Ender Dragon, Warden, Elder Guardian, Abyss Dragon), mini-bosses (all structure bosses, King mobs, Blood Moon bosses), mobs in other dimensions. Cooldown: 300 seconds (5 minutes). Visual: snap animation (particle burst: gold + soul fire), screen shake, dramatic sound. The gauntlet is indestructible.

L. QUEST VILLAGERS & NPC WIZARD
L.1 Quest Villager
Custom villager type (Nitwit skin, custom name "§6Questmaster"). Spawns in Ultra Villages and can be summoned via /questvillager. Offers dimension-themed quests:

Quest Category	Examples	Rewards
Overworld	Kill 50 zombies, Find a Ruined Colosseum, Survive a Blood Moon	Soul Fragments, Heart Crystals, COMMON weapons
Nether	Kill 20 Blazes, Clear a Crimson Citadel, Defeat Piglin King	Blessing Crystals, RARE weapons, Nether Star shards
End	Kill 100 Endermen, Defeat End Rift Dragon, Find Dragon's Hoard	MYTHIC weapons, Abyssal Key hint, Phoenix Feather
Special	Collect all 6 Infinity Stones, Defeat all 5 bosses, Earn God Slayer	Exclusive title, unique cosmetics, one ABYSSAL weapon choice
Special item sold: KeepInventorer — a consumable that permanently enables keep-inventory for the player. Cost: 64 diamonds + 32 emeralds.

L.2 NPC Wizard Villager
Custom villager type (purple robes, custom name "§5Archmage"). Spawns in Mage Towers and Ultra Villages. Sells:

Item	Cost	Notes
Exclusive MYTHIC weapons (rotating stock of 3)	48 diamonds + 16 Nether Stars	Weapons not available elsewhere
Heart Crystal	32 diamonds + 8 emeralds	Max 2 per player
Blessing Crystal (any type)	16 diamonds + 4 emeralds	—
Custom Enchanted Books (any custom enchant, level III)	24 diamonds + 12 lapis blocks	—
Soul Fragment ×5	16 diamonds	—
Phoenix Feather	32 diamonds + 1 Nether Star	—
M. ABYSSAL DIMENSION
M.1 Entry
The Ender Dragon has a 100% chance to drop an Abyssal Key in its death chest. The Abyssal Key is used to build an Abyssal Portal: a Nether-portal-shaped frame built from End Stone (4×5 frame). The portal texture matches the End portal (swirling starfield). Right-clicking the portal frame with the Abyssal Key activates it.

M.2 Dimension Description
A massive floating island in a void sky with custom purple-black particle ambiance. Features include: custom trees (chorus-plant-like but taller, with glowing purple leaves), Abyssal Endermen (taller, darker texture, 2× HP, teleport more frequently), Abyssal Wither Skeletons (blue fire, custom texture, 2× HP, wither effect on hit), non-hostile mobs (Abyssal Cows, Abyssal Sheep — custom reskinned, only attack if provoked), scattered ruins with MYTHIC loot.

M.3 Abyssal Castle
A gigantic castle structure (~120×80×120 blocks, deepslate + end stone + crying obsidian + purple glass) at the center of the island. Contains multiple wings: armory (MYTHIC weapon chests), treasury (Heart Crystals, Soul Fragments), library (custom enchanted books), blessing shrine (all blessing types), and a final arena. The arena is a circular chamber (40-block diameter) housing the Abyss Dragon.

M.4 Abyss Dragon
The final boss. Enhanced Ender Dragon with: 2,000 HP (no end crystals to heal from), 4× damage, new mechanics — void breath (applies Wither III + Blindness II for 5s), ground slam (AoE 10 blocks, launches players), summons Abyssal Endermen minions, enrage phase at 25% HP (2× attack speed, permanent Wither aura). Drops: 2–3 ABYSSAL weapons, Abyssal Plate armor pieces, 10 Heart Crystals, 50 Soul Fragments, God Slayer title (if all other bosses previously defeated).

N. BOSS ENHANCEMENTS (Harder Game)
Boss	Current HP Multiplier	New HP Multiplier	Current Dmg Multi	New Dmg Multi	New Mechanics
Ender Dragon	3.5×	5.0× (1,000 HP)	3.0×	4.0×	Summons Enderman waves, lightning breath, enrage at 20%
Wither	1.0×	3.0× (900 HP)	1.0×	2.5×	Wither Storm phase at 50% HP (larger model, more skulls)
Elder Guardian	2.5×	4.0× (320 HP)	1.8×	3.0×	Water prison, summons guardian minions, mining fatigue burst
Warden	1.0× (unchanged)	1.0× (500 HP)	1.0×	1.0×	Stays unchanged per request
O. KING MOBS (Revised)
Spawn rate: 1 per 50 mob spawns (changed from 100). Appearance: large zombie-like model (size 1.5×, resource pack texture with crown and glowing eyes). 10× HP, 3× damage. Drops: 3–9 diamonds, 1 random COMMON weapon, Soul Fragment (15% chance).

P. LEGENDARY LOOT PASSIVE EFFECTS
All legendary items (weapons and armor) can have passive enchantment-like effects that activate just by holding or wearing the item. These are implemented via a PassiveEffectListener.java that checks equipped items each tick (throttled to every 10 ticks for performance).

Passive Effect	Applied To	Description
Frost Walker	Boots (Frost Warden set)	Creates ice on water, obsidian on lava when walking
Fire Aura	Weapons (Phoenix's Grace)	Nearby mobs ignite within 3 blocks while held
Void Shield	Armor (Void Walker chest)	Absorbs first hit every 30 seconds
Soul Drain	Weapons (Soul Devourer, Soul Collector)	+1 HP per kill while held
Night Vision	Helmets (Abyssal Plate, Shadow Stalker)	Permanent Night Vision while worn
Speed Boost	Boots (Dragon Knight)	Speed I while worn
Glowing Enemies	Helmets (Blood Moon)	Hostile mobs glow within 20 blocks
Lifesteal	Full sets (Blood Moon)	Heal 5% of damage dealt
Q. CONFIG.YML (Current Values)
Copymob-difficulty:
  enabled: true
  baseline-health-multiplier: 1.0
  baseline-damage-multiplier: 1.0
  distance: {350: {health: 1.5, damage: 1.3}, 700: {health: 2.0, damage: 1.6}, 1000: {health: 2.5, damage: 1.9}, 2000: {health: 3.0, damage: 2.2}, 3000: {health: 3.5, damage: 2.5}, 5000: {health: 4.0, damage: 3.0}}
  biome:
    pale-garden: 2.0
    deep-dark: 2.5
    swamp: 1.4
    nether-wastes: {health: 1.7, damage: 1.7}
    soul-sand-valley: {health: 1.9, damage: 1.9}
    crimson-forest: {health: 2.0, damage: 2.0}
    warped-forest: {health: 2.0, damage: 2.0}
    basalt-deltas: {health: 2.3, damage: 2.3}
    end: {health: 2.5, damage: 2.0}

boss-enhancer:
  ender-dragon: {health: 5.0, damage: 4.0}   # UPDATED
  wither: {health: 3.0, damage: 2.5}          # UPDATED
  warden: {health: 1.0, damage: 1.0}          # UNCHANGED
  elder-guardian: {health: 4.0, damage: 3.0}   # UPDATED

king-mob:
  enabled: true
  spawns-per-king: 50    # CHANGED from 100
  health-multiplier: 10.0
  damage-multiplier: 3.0

blood-moon:
  enabled: true
  chance: 0.15
  mob-health-multiplier: 1.5
  mob-damage-multiplier: 1.3
  boss-every-nth: 10
  boss-health-multiplier: 20.0
  boss-damage-multiplier: 5.0

weapon-mastery:
  enabled: true
  tiers:
    novice: {kills: 0, bonus: 1.0}
    fighter: {kills: 100, bonus: 5.0}
    experienced: {kills: 500, bonus: 10.0}
    master: {kills: 1000, bonus: 15.0}

# NEW sections to add:
structures:
  enabled: true
  min-spacing: 300
  frequency: 0.08  # per chunk
end-rift:
  enabled: true
  chance: 0.10
  no-gates: true
heart-crystal:
  max: 10
  hp-per-crystal: 2
soul-fragment:
  max: 50
  damage-per-fragment: 0.5
Copy
R. RESOURCE PACK
pack.mcmeta: {"pack": {"pack_format": 75, "description": "JGlims Custom Items & Textures"}}

Current contents: 42 model JSONs in assets/minecraft/models/item/, 327 textures in assets/minecraft/textures/item/, item definitions in assets/minecraft/items/*.json. String-based custom_model_data predicates.

Known texture issues to fix: Ocean's Rage needs ocean_rage.png (currently uses stormbringer). Phoenix's Grace texture name is gilded_phoenix_greataxe but file was misspelled as pheonix_grace.png — needs alignment. Neptune's Fang uses frostaxe placeholder — needs neptunes_fang.png. Tidecaller and Stormfork share textures with other weapons — need unique textures.

New textures needed for v3.0: All 19 new weapons (#45–63), 6 Infinity Stones, Thanos Glove, Infinity Gauntlet, 7 armor sets (4 pieces each = 28 textures), colored stones, Heart Crystal, Soul Fragment, Abyssal Key, King Mob crown overlay, Blood Moon Boss glow overlay, all structure mini-boss textures, Abyssal Enderman, Abyssal Wither Skeleton, Abyssal passive mobs.

Bedrock support: Pending Rainbow conversion for GeyserMC compatibility.

S. ALL IMAGE URLs
#	URL	Description
1	https://www.genspark.ai/api/files/s/KCZKUJPY	Project reference image 1
2	https://www.genspark.ai/api/files/s/ZJ55TviA	Project reference image 2
3	https://www.genspark.ai/api/files/s/RdY1rGh2	Project reference image 3
4	https://www.genspark.ai/api/files/s/ytEdNi95	Reference image 4
5	https://www.genspark.ai/api/files/s/nsFRxnFk	Reference image 5
6	https://www.genspark.ai/api/files/s/cP1E4lFk	Reference image 6
7	https://www.genspark.ai/api/files/s/VxdND6hm	Reference image 7
8	https://www.genspark.ai/api/files/s/iv5VfNlb	Reference image 8
9	https://www.genspark.ai/api/files/s/S3fMCOQQ	Reference image 9
10	https://www.genspark.ai/api/files/s/mMavWwKr	Reference image 10
11	https://www.genspark.ai/api/files/s/YRMQT4iT	Reference image 11
12	https://www.genspark.ai/api/files/s/osi0Zos6	Reference image 12
13	https://www.genspark.ai/api/files/s/skGLHyrK	Reference image 13
14	https://www.genspark.ai/api/files/s/CXakuBB7	Abyssal weapon preview 1
15	https://www.genspark.ai/api/files/s/pQthnEmC	Abyssal weapon preview 2
T. KNOWN BUGS
#	Bug	Status	Priority
1	Ocean's Rage uses DIAMOND_SWORD instead of TRIDENT	Open	High
2	Ocean's Rage texture points to stormbringer instead of ocean_rage	Open	High
3	Phoenix's Grace texture filename mismatch (pheonix_grace vs gilded_phoenix_greataxe)	Open	High
4	Neptune's Fang uses frostaxe placeholder texture	Open	Medium
5	Tidecaller/Stormfork share textures with non-trident weapons	Open	Medium
6	LegendaryAbilityListener.java still uses old hold-timer system; ~24 alternate abilities not implemented	Open	Critical
7	LegendaryTier is an inner enum with only LEGENDARY/UNCOMMON; needs 5-tier standalone enum	Open	Critical
8	Ability damage does not apply to players (PvP)	Open	Low
9	Bedrock resource pack not converted (Rainbow)	Open	Medium
10	Config header uses wrong version string (v1.3.0 instead of v1.4.0)	Open	Low
U. ROADMAP
Phase	Name	Description	Status
8b	Ability Rewrite	Full rewrite of LegendaryAbilityListener.java: replace hold input with crouch+RC, implement all 88+ abilities	In Progress
8c	Tier Visual Overhaul	Implement LegendaryTier.java (5-tier standalone enum), tier-scaled particle effects	Planned
8d	Weapon Expansion	Add weapons #45–63, fix Ocean's Rage/Phoenix's Grace/trident textures, create all new textures	Planned
8e	Passive Effects	Implement PassiveEffectListener.java for held/worn legendary passive effects	Planned
8f	Bedrock RP	Convert resource pack via Rainbow for Bedrock/GeyserMC	Planned
9	Custom Structures	Implement StructureManager, all 20+ Overworld/Nether/End structures, mini-bosses, loot tables	Planned
10	Events Expansion	End Rift event, Nether Storm, Piglin Uprising, Void Collapse, event-quest interaction	Planned
11	Power-Ups & Mastery	Heart Crystals, Soul Fragments, reworked mastery system, boss titles, God Slayer	Planned
12	Legendary Armor	7 armor sets + 3 weak normal sets, passive effects, AI-generated textures	Planned
13	Infinity Gauntlet	Thanos Temple structure, Thanos boss, colored stones, Infinity Stone crafting, Gauntlet item	Planned
14	Quest & NPC System	Quest Villager, NPC Wizard, KeepInventorer item, quest tracking, reward system	Planned
15	Boss Enhancements	Stronger Dragon/Wither/Elder Guardian, new mechanics, death chest structure	Planned
16	Abyss Dimension	Portal, floating island world, Abyssal mobs, Abyssal Castle, Abyss Dragon	Planned
17	Texture & Animation	Custom mob textures via AL revamp packs + Fresh Animations + AI-generated; full workflow	Planned
18	Final Polish	Bug fixes, balance pass, performance optimization, v3.0.0 release	Planned
Immediate Priorities (Next Session):
Step-by-step texture workflow using AL revamp packs / Fresh Animations / AI-generated textures (prompts, file paths, deployment commands).
Begin Phase 8b (LegendaryAbilityListener rewrite).
Create standalone LegendaryTier.java enum.
V. NEXT-CHAT TEXTURE WORKFLOW PREVIEW
The next chat session must provide a complete step-by-step guide covering:

Downloading base packs: AL's Mob Revamp (Modrinth), Fresh Animations (CurseForge/Modrinth) — extract .jar/.zip, locate assets/minecraft/textures/entity/ and assets/minecraft/optifine/cem/ folders.
AI texture generation: Use image AI (Stable Diffusion, DALL-E, Midjourney) with prompts tailored to each mob. Example: "Minecraft pixel art texture, 64x64, zombie king with golden crown and glowing red eyes, dark green skin, royal cape, game asset, PNG transparent".
File paths: Place custom entity textures in assets/minecraft/textures/entity/custom/ and CEM models in assets/minecraft/optifine/cem/ (for OptiFine/CIT) or use the Paper ItemsAdder-free method via resource pack custom model data.
Animation: Fresh Animations' CEM (Custom Entity Models) files define bone structure and keyframes; modify these for custom mobs (King Mobs, Blood Moon Boss, structure bosses, Abyss Dragon).
Deployment: Add textures to JGlimsResourcePack.zip, update pack.mcmeta, upload to GitHub release, update server.properties resource-pack hash.
Full prompts list for every custom mob texture (King Mob, Blood Moon Boss, Shrek, Arch Mage, Gladiator King, Thanos, all Abyssal mobs, Abyss Dragon, etc.).
END OF DEFINITIVE SUMMARY v9.0 — All data, URLs, code references, tables, plans, and metadata preserved for the next session.
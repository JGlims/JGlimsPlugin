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








## N. EXPANSION ROADMAP — FUTURE PHASES (v2.0.0 → v3.0.0)

Everything below is planned content. Status: ❌ = not started.
Current build: 48 Java files, 59 weapons, 6 armor sets, 27 structures, 4 events
(Blood Moon + Nether Storm + Piglin Uprising + Void Collapse), 5 power-ups,
64 enchantments, boss mastery, guilds, weapon mastery, all compiling and deployed.

---

### PHASE 20 — END RIFT OVERWORLD EVENT ❌
The Section G.2 event from the original plan. Distinct from VoidCollapseEvent (which
triggers IN the End). This one opens a portal IN THE OVERWORLD.

Trigger: 10% chance when Ender Dragon dies. A massive purple portal ring (15×15 End
portal frame blocks + purple stained glass) spawns at a random location within 500
blocks of world spawn. Purple beam shoots into the sky. All players get a title:
"THE RIFT HAS OPENED" with coordinates.

Wave system (10 minutes total):
  - Minutes 0–2: 20 Endermen + 5 Shulkers per minute
  - Minutes 2–5: 30 Endermen + 10 Shulkers + 5 Phantoms (End-variant, purple) per minute
  - Minutes 5–8: 40 mixed + Evokers with void particles
  - Minutes 8–10: ALL remaining + End Rift Dragon spawns

End Rift Dragon: Ender Dragon variant, 600 HP, 2× damage, void breath (Wither effect +
Blindness in 30-block cone), lightning strikes every 10s, summons 2 Shulker turrets at
50% HP. Cannot perch on portal — always flying and attacking.

Drops: 1–2 MYTHIC weapons from END_RIFT_POOL (already defined in LegendaryLootListener:
Tengen's Blade, Soul Devourer, Star Edge, Creation Splitter, Stop Sign). Portal closes
after boss dies or 15 minutes elapse. All rift mobs despawn when portal closes.

New file: EndRiftEvent.java in events/ package. Register in EventManager.

---

### PHASE 21 — ENHANCED PILLAGER SYSTEM ❌

**Pillager War Parties** — Roaming groups of 8–15 Pillagers + 2 Ravagers that spawn
naturally in Plains/Forest (5% chance per chunk, min 500 blocks from spawn). Led by a
War Captain (Pillager, 200 HP, enchanted crossbow, banner). Drops RARE weapon on death.

**Enhanced Siege Event** — When a player with Bad Omen enters 100 blocks of an Ultra
Village, trigger custom raid instead of vanilla. 5 waves:
  - Wave 1: 15 Pillagers + 3 Vindicators
  - Wave 2: 20 Pillagers + 5 Vindicators + 2 Ravagers
  - Wave 3: 25 mixed + Evokers + Ravager Knight (Ravager, 300 HP, Vindicator rider)
  - Wave 4: 30 mixed + Illusioner General (Illusioner, 250 HP, mirror images, blindness arrows)
  - Wave 5: ALL remaining + Pillager Warlord (Vindicator, 500 HP, diamond armor, dual axes,
    ground-slam AoE, rally cry buffs all nearby Pillagers +50% damage for 10s)

Warlord drops: 1 EPIC weapon + RARE armor piece (random Shadow Stalker piece) + 5–15 emeralds.
Completing all 5 waves grants "Village Defender" title to all participants.

**New Structures:**

Pillager Fortress — Larger than vanilla outpost. Dark oak + cobblestone + iron bars,
40×25×40. Watchtowers with crossbow Pillagers, armory room with weapon racks (decorative),
prison cells with captured Villagers (free them for reputation), treasure vault behind
iron doors. Boss: Fortress Commander (Pillager, 300 HP, Power V crossbow, Strength II).
EPIC loot tier. Biomes: Plains, Taiga.

Pillager Airship — Balloon-ship floating at Y=150+. Dark oak hull, white wool balloon,
ladder/scaffolding to ground. Navigation room, captain's quarters with map table, cargo
hold with 3 loot chests. Boss: Sky Pirate Captain (Pillager, 250 HP, Elytra, explosive
crossbow that fires TNT arrows). Drops: 1 RARE weapon + 1 Elytra (guaranteed) + 3–8
emerald blocks. Biomes: any Overworld (rare, 0.001 generation chance).

Pillager War Camp — Field encampment, 30×10×30. Tents (wool), campfires, weapon racks,
cages with captured Iron Golems. Boss: War Marshal (Vindicator, 250 HP, netherite axe).
Drops: RARE weapon. Biomes: Savanna, Plains.

New files: PillagerWarManager.java, EnhancedRaidEvent.java.
New structures: PILLAGER_FORTRESS, PILLAGER_AIRSHIP, PILLAGER_WAR_CAMP in StructureType.

---

### PHASE 22 — INFINITY GAUNTLET + THANOS ❌
Full crafting chain from Section K.

**Thanos Temple** (new structure) — Rare, Badlands/Mountains. Deepslate + gold blocks +
purple glass pyramid (45×35×45). External: stepped pyramid with pillars, golden trim.
Internal: trapped corridors (piston crushers, arrow dispensers, lava pits), puzzle room
(lever sequence to open vault), throne room. Boss: Thanos (Iron Golem, 800 HP, purple
particles, custom attacks: ground-slam AoE 10-block radius, beam attack — line of purple
particles + 15 damage, meteor shower — 5 falling anvils with fire on impact). Thanos
enters "enrage" at 25% HP: all attacks double speed, gains Regeneration I. Drops: Thanos
Glove (100%), 5–10 diamonds, 1 MYTHIC weapon, 2 Heart Crystals.

**Infinity Stone Fragments** — During Blood Moon, any hostile mob has 0.1% drop chance
for a random colored stone fragment. Six colors: Purple (Power), Blue (Space), Red
(Reality), Orange (Soul), Green (Time), Yellow (Mind). Custom items: custom_model_data
30201–30206, enchant glow, mysterious lore ("A fragment of cosmic power..."). Secret/
undocumented — no in-game hints exist. Players must discover them.

**Crafting Chain:**
  1. Kill Thanos → Thanos Glove
  2. Collect 6 colored stone fragments (Blood Moon drops)
  3. Each fragment + 1 Nether Star in anvil → finished Infinity Stone (6 total)
  4. Thanos Glove + all 6 Infinity Stones → Infinity Gauntlet (shapeless recipe)

**Infinity Gauntlet Behavior:** Right-click → snap. Kills 50% of all loaded hostile mobs
in current dimension (random selection). Excluded: Wither, Ender Dragon, Warden, Elder
Guardian, Abyss Dragon, all structure mini-bosses, all King mobs, all event bosses.
300s cooldown. VFX: gold + soul fire particle explosion, purple beam skyward, screen-
shake-like darkness pulse (0.5s Darkness effect), dramatic snap sound (anvil land +
totem + thunder). Chat: "PlayerName snapped the Infinity Gauntlet! X mobs were erased."
Gauntlet is indestructible.

New files: InfinityGauntletManager.java, InfinityStoneManager.java (crafting/ or new
infinity/ package). New structure: THANOS_TEMPLE in StructureType.

---

### PHASE 23 — QUEST VILLAGERS & NPC WIZARD ❌

**Questmaster Villager** — Custom Nitwit with gold name "Questmaster". Spawns in Ultra
Villages (1 per village) and can be summoned via /questvillager (OP only). Right-click
opens quest GUI (54-slot chest inventory).

Quest categories with examples:

Overworld Quests:
  - "Slay 50 Zombies" → 3 Soul Fragments
  - "Discover a Ruined Colosseum" → 1 Heart Crystal
  - "Survive a Blood Moon" → 1 COMMON weapon
  - "Defeat the Blood Moon King" → 1 RARE weapon + 5 Soul Fragments
  - "Find all 3 Witch Houses" → 1 Blessing Crystal set (C + Ami + La)
  - "Kill 200 Skeletons" → Titan's Resolve

Nether Quests:
  - "Kill 20 Blazes" → 1 Blessing Crystal
  - "Clear a Crimson Citadel" → 1 RARE weapon
  - "Defeat the Piglin King structure boss" → 3 Blessing Crystals
  - "Survive a Nether Storm event" → 1 EPIC weapon
  - "Mine 64 Ancient Debris" → Phoenix Feather

End Quests:
  - "Kill 100 Endermen" → 1 MYTHIC weapon
  - "Defeat the End Rift Dragon" → Phoenix Feather + 10 Soul Fragments
  - "Find Dragon's Hoard structure" → 2 Heart Crystals
  - "Defeat the Void Sentinel" → 1 MYTHIC weapon

Special Quests (unlocked after completing 10 quests):
  - "Collect all 6 Infinity Stones" → exclusive "[Infinity]" chat title + choice of 1 ABYSSAL weapon
  - "Defeat all 5 bosses (Elder Guardian, Wither, Warden, Dragon, Abyss Dragon)" → "[God Slayer]" title + permanent +25% resistance + +20% damage (replaces all boss titles)
  - "Reach Weapon Master on 10 different weapons" → permanent +5% XP boost
  - "Complete 50 total quests" → "[Questmaster]" title + KeepInventorer
  - "Discover all 27 structure types" → "[Explorer]" title + 5 Heart Crystals

Quest progress saved per player via PersistentDataContainer. Quest tracking: structure
discovery tracked via entering structure bounding boxes. Boss kills tracked via existing
BossMasteryManager hooks. Kill counts tracked via PDC incrementing.

**NPC Wizard (Archmage)** — Custom villager, purple name "Archmage". Spawns in Mage
Towers and Ultra Villages. Right-click opens shop GUI (54-slot chest).

Shop inventory (always available):
  - Heart Crystal: 32 diamonds
  - C-Blessing Crystal: 16 diamonds
  - Ami-Blessing Crystal: 16 diamonds
  - La-Blessing Crystal: 16 diamonds
  - Phoenix Feather: 64 diamonds + 8 Nether Stars
  - KeepInventorer: 64 diamonds + 32 emeralds
  - Soul Fragment ×5: 24 diamonds
  - Totem of Awakening (new item): 48 diamonds — resets all weapon mastery titles, allowing
    player to re-earn them (useful if you want to switch weapon types)

Rotating stock (changes every real-world day, seed-based for consistency):
  - 3 MYTHIC weapons (48 diamonds + 16 Nether Stars each)
  - 2 random custom enchantment books (8–32 diamonds depending on enchant tier)
  - 1 random armor piece from any legendary set (32 diamonds + 8 Nether Stars)

New files: QuestManager.java, QuestVillagerListener.java, WizardShopManager.java in
new npcs/ package.

---

### PHASE 24 — THE ABYSS DIMENSION ❌
The true endgame.

**Abyss Portal** — Nether portal shape (4×5 obsidian frame) built with PURPUR BLOCKS
instead of obsidian. Lit with Abyssal Key (100% Ender Dragon drop, consumed on use).
On ignition, portal fills with custom purple/black particle effect. Walking in teleports
to "world_abyss".

**World Generation** — End-style dimension: void below, floating islands of varying sizes
scattered through the space. NOT a flat world — islands at different Y levels (Y=40 to
Y=180), connected by narrow bridges of crying obsidian or nothing at all (requires
Elytra/ender pearls). Island types:
  - Main Islands: Large (40–80 block diameter). Deepslate + sculk + crying obsidian surface.
    Custom dead trees (dark oak logs, no leaves, soul lanterns). Purple fog particles.
  - Crystal Islands: Medium (20–40 blocks). Amethyst + prismarine. Contain Void Crystal
    ore (custom block, drops Void Crystals — power-up: +1% permanent damage reduction, max 20).
  - Lava Islands: Small (10–20 blocks). Basalt + magma + lava pools. Hostile: Magma Cubes,
    Blazes. Contain Abyssal Shard ore (drops Abyssal Shards, portal crafting material).
  - Void Pockets: Tiny (5–10 blocks). End stone surrounded by void particles. Random MYTHIC
    chest spawns.

Environment: perpetual darkness (time locked midnight), purple/red particle fog,
reversed gravity zones (random areas where player floats upward), void rifts (visible
cracks in the air — walking through deals 5 damage per tick, marked with red particles).
Custom ambient: enderman sounds slowed down, deep bass rumble, occasional void cracking.

**Abyss Structures:**

Abyssal Citadel — THE main structure. Massive floating castle (100×60×100). Deepslate
bricks + crying obsidian + purple stained glass + amethyst accents + soul lanterns.
Wings: Hall of Echoes (gauntlet room — waves of Abyssal Minions: Wither Skeleton variants
with 80 HP, void swords), Void Library (bookshelves + 2 ABYSSAL-tier loot chests + lore
books explaining the Abyss), Armory of the Fallen (weapon displays + 1 guaranteed ABYSSAL
weapon chest), Throne Room (arena for Abyss Dragon fight). The Citadel has no natural
entrance — player must break in or find the hidden path (signs with riddles).

Shattered Monastery — Floating ruins connected by broken bridges. End stone + purpur +
amethyst clusters. Parkour required. Contains meditation rooms with enchanting tables,
library with 4 MYTHIC chests. Boss: Abyssal Monk (Warden variant, 400 HP, sonic boom
attacks + short-range teleportation every 3 hits + summons void echoes that mimic the
monk's attacks). Drops: Whisperwind Awakened (ABYSSAL weapon #63).

Void Forge — Industrial structure on a lava island. Blackstone + polished blackstone +
chains + lava + blast furnaces. Contains the Abyssal Anvil (custom anvil block — used
to upgrade MYTHIC weapons into ABYSSAL awakened forms using Abyssal Essence). Abyssal
Essence is crafted from 4 Void Crystals + 4 Abyssal Shards + 1 Nether Star. Each
awakened upgrade requires: base MYTHIC weapon + 3 Abyssal Essence + 16 Nether Stars.
Only these upgrades exist:
  - Requiem of the Ninth Abyss → Requiem Awakened
  - True Excalibur → Excalibur Awakened
  - Creation Splitter → Creation Splitter Awakened
  - (Whisperwind Awakened drops directly from Abyssal Monk, no upgrade path)

Crystalline Caverns — Underground cave system within a large main island. Amethyst +
prismarine + glow lichen + sculk veins. No boss. Contains Void Crystal ore veins,
Abyssal Shard deposits, rare Abyssal Essence crystal clusters (pre-crafted, 1–2 per
cavern). Also contains 2 MYTHIC chests and 1 ABYSSAL chest (rare).

Abyssal Watchtower — Tall narrow tower (8×50×8) on the edge of main islands. Deepslate
+ soul lanterns + iron bars. Spiral staircase interior. Top floor has a MYTHIC chest +
Spyglass of the Void (custom item: right-click to reveal all structures within 500 blocks
on the current island, 120s cooldown). Guarded by 4 Abyssal Sentries (Wither Skeleton,
150 HP, shields).

**Abyss Dragon** — THE final boss. Spawns when player places the Abyssal Heart (crafted:
4 Dragon Breath + 4 Abyssal Essence + 1 Nether Star) on the Throne Room altar in the
Abyssal Citadel.

Phase 1 (100%–60% HP): 2000 HP base. Standard dragon flight patterns + void breath
(30-block cone, Wither III + Blindness + 10 true damage per second for 3s). Summons
Abyssal Minions (Wither Skeleton, 80 HP) every 30s. Arena covered in sculk.

Phase 2 (60%–30%): Dragon gains speed boost. 4 Abyssal Pillars rise from the floor
(like End Crystals — heal the dragon 1% per second each, but also shoot lightning at
nearest player every 5s). Dragon adds dive-bomb attack (charges at player, 25 damage
+ massive knockback). Pillars must be destroyed (200 HP each, take only melee damage).

Phase 3 (30%–0%): ENRAGE. All attacks doubled in frequency and damage. Arena fills
with void fog (Blindness I every 5s to all players). Gravity reverses every 30 seconds
(players float upward for 5s, then slam down — Slow Falling helps). Dragon spawns 8
Abyssal Minions at once. Dragon regenerates 0.5% HP per second. Must be killed quickly.

Drops (on death):
  - 2–3 ABYSSAL weapons (random from all 4 awakened weapons)
  - 1–2 Abyssal Plate armor pieces (random slots)
  - 10–20 diamonds, 5–10 Abyssal Shards, 3 Abyssal Essence
  - Dragon Soul (new power-up: permanent +10% damage to all bosses, max 1)
  - "[Abyssal Conqueror]" title granted to all participants

**ABYSSAL Weapons (#60–63):**

| # | Enum ID | Display Name | Dmg | CMD | Abilities |
|---|---|---|---|---|---|
| 60 | REQUIEM_AWAKENED | Requiem of the Ninth Abyss (Awakened) | 28 | 30060 | Abyssal Devour / Void Collapse |
| 61 | EXCALIBUR_AWAKENED | True Excalibur (Awakened) | 26 | 30061 | Divine Annihilation / Sacred Realm |
| 62 | CREATION_SPLITTER_AWAKENED | Creation Splitter (Awakened) | 30 | 30062 | Reality Shatter / Big Bang |
| 63 | WHISPERWIND_AWAKENED | Whisperwind (Awakened) | 24 | 30063 | Silent Storm / Phantom Cyclone |

**Abyssal Plate Armor Set** — ABYSSAL tier. Netherite base. Total defense: 28. CMD:
30161–30164. Set bonus: +30% damage, complete Wither immunity. Passives: Night Vision
permanent (helmet), Thorns III equivalent + fire particles (chestplate), Speed I permanent
(leggings), Fire Walker + Frost Walker combined (boots). Drops from Abyss Dragon only.

New files: AbyssDimensionManager.java, AbyssDragonBoss.java, AbyssalAnvilManager.java,
AbyssWorldGenerator.java. New package: abyss/. New structures in StructureType:
ABYSSAL_CITADEL, SHATTERED_MONASTERY, VOID_FORGE, CRYSTALLINE_CAVERNS, ABYSSAL_WATCHTOWER.

---

### PHASE 25 — LEGENDARY BOWS & RANGED WEAPONS ❌
All 59 current legendaries are melee. Fill the ranged gap.

| # | Name | Material | Tier | Dmg | Primary | Alt | PCD | ACD |
|---|---|---|---|---|---|---|---|---|
| 64 | Artemis' Longbow | BOW | MYTHIC | 18 | Moonbeam Arrow (homing, explodes on impact for 8 AoE damage) | Huntress Volley (7 arrows in fan pattern) | 12 | 45 |
| 65 | Hellfire Crossbow | CROSSBOW | EPIC | 16 | Infernal Bolt (leaves fire trail, 6-block AoE explosion) | Demon's Rain (rapid-fire 10 bolts over 3s) | 10 | 35 |
| 66 | Void Caster | BOW | MYTHIC | 20 | Void Arrow (teleports hit target 30 blocks straight up) | Black Hole (8-block pull radius, 5s, damages all pulled mobs) | 14 | 55 |
| 67 | Windrunner | BOW | RARE | 13 | Gale Arrow (knockback 15 blocks + Levitation 3s) | Zephyr Dash (player launches in arrow direction 20 blocks) | 8 | 20 |
| 68 | Frostbite | CROSSBOW | EPIC | 15 | Cryo Bolt (freezes target 5s, ice AoE 4-block radius) | Blizzard Barrage (3 frost bolts, Slowness III 8s) | 10 | 30 |
| 69 | Starcaller | BOW | MYTHIC | 22 | Meteor Arrow (summons falling meteor on impact point, 12-block AoE) | Constellation (5 light beams from sky in 10-block radius) | 15 | 60 |
| 70 | Plague Spreader | CROSSBOW | RARE | 12 | Toxic Bolt (Poison III + Nausea 8s) | Miasma Cloud (poison gas AoE 5-block radius 10s) | 8 | 25 |
| 71 | Cupid's Wrath | BOW | COMMON | 10 | Charm Arrow (forces mobs to fight each other 10s) | Heart Seeker (guaranteed crit, heals shooter 5 HP) | 7 | 18 |

---

### PHASE 26 — OCEAN EXPANSION ❌

**New Structures:**

Sunken City — Massive underwater ruins (60×30×60). Prismarine + sea lanterns + dead
coral + sand. Flooded corridors, air pocket rooms, treasure vaults with 3 EPIC chests.
Boss: Leviathan (Elder Guardian variant, 500 HP, tidal wave attack — pushes all players
back 20 blocks, whirlpool — drags players toward center, summons 10 Drowned with
tridents). Drops: 1 EPIC weapon + Neptune's Crown (new helmet: Water Breathing + Conduit
Power + Dolphin's Grace, EPIC tier, CMD 30171). Biomes: Deep Ocean, Deep Lukewarm Ocean.

Ghost Ship — Ship on ocean surface (30×20×15). Dark oak hull, tattered gray wool sails,
soul lanterns, cobwebs. Interior: captain's cabin with map + log book, cargo hold with
2 RARE chests, brig with skeleton prisoners. Boss: Phantom Captain (Phantom, 300 HP,
summons 6 ghost crew — Strays with chain armor, anchor slam — area stun 3s). Drops:
1 RARE weapon + Spectral Compass (new item: points toward nearest custom structure,
right-click to activate, 300s cooldown, CMD 30172). Biomes: any Ocean.

Deep Sea Trench — Underwater cave at Y=-50 to Y=-30 (40×15×40). Deepslate + prismarine
+ magma blocks + soul sand. Dark, narrow, claustrophobic. Flooded. Boss: Abyssal Angler
(Guardian, 350 HP, lure attack — hook particle beam pulls player 10 blocks toward it,
darkness aura — Blindness 3s on hit, electric shock — chain lightning 3 nearby players).
Drops: 1 EPIC weapon. Biomes: Deep Ocean.

Coral Reef Sanctuary — Beautiful shallow-water (20×10×20). Coral blocks, sea pickles,
tropical fish spawners. NO boss, friendly. Contains: Axolotl spawner (5 Axolotls),
Mermaid's Tear (new power-up: permanent Water Breathing, max 1, CMD 30173), 1 COMMON
chest. Biomes: Warm Ocean, Lukewarm Ocean.

**New Event:**

Kraken Event — 3% chance when a player sails a boat for 60+ continuous seconds in
Deep Ocean. Giant tentacles (armor stands with slime + prismarine blocks) emerge in a
ring around the player's boat. 8 tentacles, each 15 blocks tall. Each tentacle has 100 HP
and must be destroyed. After 4 tentacles die, the Kraken surfaces: Elder Guardian variant,
800 HP. Attacks: tentacle slam (remaining tentacles slam the water, 15 damage AoE),
ink cloud (Blindness 10s to all players in 20 blocks), drag-under (pulls player below
water, Slowness III + suffocation damage if they can't swim out). At 25% HP, Kraken
dives and resurfaces at random location. Drops: 1 MYTHIC weapon + Kraken Ink (throwable
consumable: Blindness AoE 10-block radius 15s, stackable, CMD 30174) + 5–15 prismarine
shards + 3–8 diamonds.

New structures: SUNKEN_CITY, GHOST_SHIP, DEEP_SEA_TRENCH, CORAL_REEF_SANCTUARY.
New event: KrakenEvent.java. New items: Neptune's Crown, Spectral Compass, Mermaid's
Tear, Kraken Ink.

---

### PHASE 27 — WORLD BOSSES (OVERWORLD & NETHER) ❌
Scheduled massive bosses requiring multiple players.

**Overworld World Bosses:**

The Colossus — Spawns every 3 real-time hours (configurable) near a random online player
in Overworld. Announced 5 minutes before: "THE GROUND TREMBLES... The Colossus approaches!"
with coordinates. Giant Iron Golem (simulated 30-block height via stacked armor stands +
falling block entities for the body). 5000 HP. Attacks: ground pound (20-block shockwave,
15 damage, launches players upward), boulder throw (launched falling block, explodes on
impact like TNT), stomp (instakill in 3-block radius, telegraphed by shadow on ground
for 2s). Requires 3+ players to realistically defeat. Drops: 1 MYTHIC weapon per
participant (each player gets their own), Colossus Heart (new power-up: +2 permanent max
hearts, max 1), 10–30 diamonds shared pile, 3–5 Nether Stars.

The Hydra — Spawns every 4 real-time hours in Swamp biomes. Announced: "A terrible
creature stirs in the swamp..." Multi-head dragon (3 custom heads via armor stands
shooting fireballs). 4000 HP. When below 50% HP, grows a 4th head (+1000 HP regenerated,
total 5000 effective). Attacks: triple fire breath (3 streams from 3 heads), poison spit
(AoE Poison II, 8-block radius), tail sweep (180° knockback behind the boss). If you
kill it too slowly, at 10 minutes it grows a 5th head (+1000 more HP). Drops: Hydra Scale
armor set pieces (EPIC tier, set bonus: regenerate 1 HP/s while in water/rain, 4 pieces,
CMD 30181–30184), 1 EPIC weapon per participant, 5–15 diamonds.

The Lich King — Spawns every 5 real-time hours at night in Dark Forest. Announced:
"The dead rise from their graves... The Lich King has awakened!" Evoker model, 3000 HP,
full netherite armor appearance. Attacks: death bolt (Wither V projectile, 15 damage),
raise dead (summons 10 Wither Skeletons + 5 Zombies every 45s), soul drain (AoE: steals
2 HP from every player within 15 blocks, heals self), bone cage (encases target player
in bone blocks for 3s, must be broken out by allies). At 30% HP: mass resurrection
(all dead minions respawn). Drops: Lich's Crown (new helmet, MYTHIC: auto-revive once
per life like Phoenix Feather but recharges every 10 minutes, glowing enemies always,
+50% damage to undead, CMD 30191), 1 MYTHIC weapon per participant, 5–10 Nether Stars.

**Nether World Bosses:**

Infernal Behemoth — Spawns every 3 real-time hours in Nether Wastes. Massive Hoglin
(simulated giant via armor stands). 4000 HP. Attacks: charge (30-block dash, 20 damage +
knockback 15 blocks, destroys blocks in path), lava eruption (creates 5 lava geysers in
random locations, each 3-block radius), fire stomp (all blocks in 10-block radius become
fire for 5s). Drops: 1 EPIC weapon per participant, Behemoth Tusk (new item: crafting
ingredient for Hellfire Crossbow legendary, CMD 30195), 5–10 gold blocks.

The Wither Storm — Spawns every 5 real-time hours in Soul Sand Valley. THREE Withers
fused together (3 Withers spawned simultaneously, linked: when one takes damage all three
share it, total 3000 HP shared pool). Each Wither has different attacks: first shoots
normal skulls, second shoots blue skulls (break blocks), third shoots charged skulls
(explosion + Wither III). When 1 Wither dies (1000 HP threshold), remaining two gain
+50% attack speed. When 2 die, the last enters berserk: 2× damage, rapid-fire skulls.
Drops: 2 EPIC weapons per participant, Nether Star ×3, Wither Rose ×10, Soul Flame
(new power-up: permanent +5% damage to Nether mobs, max 1, CMD 30196).

Magma Wyrm — Spawns every 4 real-time hours in Basalt Deltas. Serpentine dragon made of
magma blocks + chains (armor stand segments, 10-segment body that slithers). 3500 HP.
Attacks: burrow (dives into lava lake, erupts under random player), magma breath (lava
particle cone, 10-block range, sets everything on fire), coil (wraps around player,
constrict damage 5/s for 4s, must be freed by allies hitting the coil segments). Drops:
1 EPIC weapon per participant, Wyrm Scale ×3 (crafting ingredient for Frost Warden boots
upgrade), 3–8 Netherite Scraps.

New files: WorldBossManager.java, WorldBossType.java in mobs/ or new worldboss/ package.
New items: Colossus Heart, Hydra Scale set, Lich's Crown, Behemoth Tusk, Soul Flame.

---


### PHASE 29 — PET SYSTEM ❌

**Tameable Mythical Pets** — Rare drops from bosses. Follow player, have abilities, level
up from owner's combat. Stored in PersistentDataContainer. Persist across sessions.

| Pet | Source | Drop % | Type | Ability | Level Scaling |
|---|---|---|---|---|---|
| Phoenix Hatchling | Phoenix's Grace alt (5%) | 5% | Passive | Heals owner 4 HP every 60s, fire immunity aura 3 blocks | +1 heal/level, +1 block radius at L5 |
| Shadow Wolf | Blood Moon King (10%) | 10% | Melee | Attacks hostiles, 5 base damage | +2 damage/level |
| Crystal Golem | Warden (8%) | 8% | Tank | Draws aggro from nearby mobs, 100 HP | +20 HP/level, taunt radius +1/level |
| Void Sprite | Void Collapse boss (5%) | 5% | Utility | Teleports owner out of danger when HP < 25% | +2% damage reduction/level |
| Infernal Imp | Nether Storm boss (10%) | 10% | Ranged | Throws fireballs at hostiles, 8 damage | +2 damage/level, +1 fireball at L5 |
| Kraken Spawn | Kraken (5%) | 5% | Ranged | Water jet attack (pushes + damages), Water Breathing aura | +3 damage/level |
| Abyssal Wisp | Abyss Dragon (3%) | 3% | Passive | Permanent Night Vision + Glowing on nearby enemies | +5 block radius/level |

Max level: 10. XP: 1 per owner kill, 10 per boss kill. Levels: 0/10/30/60/100/150/210/
280/360/450 XP thresholds. Pet death: 5-minute respawn timer (pet egg appears in
inventory after 5 min). Only 1 active pet at a time.

Command: `/jglims pet <summon|dismiss|stats|rename|list>`.

New files: PetManager.java, PetListener.java, PetType.java in new pets/ package.

---

### PHASE 30 — PROCEDURAL DUNGEONS ❌

**Dungeon Entrance** — New structure type. Stone brick staircase leading underground
(10×10×5 surface footprint). When player descends below Y=50 inside the entrance, the
dungeon generates procedurally ahead of them.

**Dungeon Structure:**
  - 10 floors, each 3–5 rooms
  - Room types: Combat (mob waves, locked doors until cleared), Puzzle (redstone lever
    sequences, parkour over void, pressure plate mazes), Treasure (trapped chest room —
    TNT under floor, arrow dispensers), Boss (floor boss at rooms 4–5), Rest (campfire +
    crafting table + ender chest, safe, no mobs)
  - Rooms are 15×8×15, connected by corridors (3×3×8)
  - Difficulty per floor: Floors 1–3 COMMON, 4–6 RARE, 7–9 EPIC, Floor 10 MYTHIC
  - Mob scaling: Floor 1 zombies/skeletons → Floor 5 Wither Skeletons/Evokers →
    Floor 10 custom Dungeon Lords (300 HP, random modifiers)

**Floor Boss Modifiers** (randomly applied, 1–2 per boss):
  - "Enraged" — +50% damage
  - "Armored" — +50% HP, +50% knockback resistance
  - "Teleporting" — blinks to random location every 5s
  - "Vampiric" — heals 10% of damage dealt
  - "Explosive" — drops TNT on death
  - "Cloning" — splits into 2 half-HP copies at 50%
  - "Shielded" — immune to projectiles
  - "Berserker" — gains +10% damage per hit taken (stacking)

**Floor 10 Boss**: Dungeon Overlord (Warden variant, 500 HP + 2 random modifiers).
Always drops 1 weapon matching floor tier (guaranteed MYTHIC) + Dungeon Key (opens
bonus vault on Floor 10 with 2 extra chests).

**Rules:**
  - Timer: 45 minutes total. Failure = teleport out, dungeon collapses.
  - Guild-instanced: only guild members of the entering player can join. Solo allowed.
  - Max 4 players per dungeon instance.
  - Loot scales with player count (more players = more loot in chests, but mobs also scale).
  - Death in dungeon: respawn at Floor Rest room (lose 1 floor progress if no Rest room
    visited on current floor).
  - Dungeon resets: entrance becomes usable again after 2 real-time hours.

New files: DungeonGenerator.java, DungeonRoom.java, DungeonFloor.java, DungeonInstance.java
in new dungeons/ package. New structure: DUNGEON_ENTRANCE in StructureType.

---

### PHASE 31 — LEGENDARY SHIELDS & OFF-HAND ❌

| # | Name | Material | Tier | Active (Right-click with shield raised) | Passive (while equipped) | CD |
|---|---|---|---|---|---|---|
| S1 | Aegis of Dawn | SHIELD | MYTHIC | 5s invulnerability bubble | +20% block damage reduction | 60s |
| S2 | Void Mirror | SHIELD | EPIC | Reflects next projectile at 2× speed + damage | 15% chance to dodge melee entirely | 30s |
| S3 | Ember Ward | SHIELD | RARE | Fire nova 8-block radius (sets mobs on fire 5s) | Fire immunity while blocking | 25s |
| S4 | Frost Aegis | SHIELD | EPIC | Spawns ice wall (5×3 packed ice blocks in front) | Melee attackers get Slowness I 3s | 35s |
| S5 | Dragon Scale Shield | SHIELD | MYTHIC | Dragon breath cone (15 damage, 8-block range) | +4 max HP while equipped | 45s |
| T1 | Spellbook of Storms | BOOK | MYTHIC | Lightning storm (3 bolts on cursor location) | +10% damage during rain/thunder | 40s |
| T2 | Orb of Shadows | ENDER_EYE | EPIC | 8s full invisibility (breaks on attack) | Permanent Night Vision | 45s |
| T3 | War Banner | SHIELD | RARE | Rally: allies in 15 blocks get +20% damage for 10s | Guild members in 20 blocks get +5% damage passive | 60s |

---

### PHASE 32 — FISHING OVERHAUL ❌

**Legendary Fish** (rare catches, consumed for effects):
  - Golden Koi: Luck III for 10 minutes (0.5% catch chance)
  - Shadow Catfish: Night Vision + Speed I for 5 minutes (1% catch chance)
  - Lava Eel (Nether lava fishing): Fire Resistance + Strength I for 5 minutes (2%)
  - Void Jellyfish (End fishing): Slow Falling + Regeneration II for 3 minutes (1.5%)
  - Ancient Coelacanth: Permanent +0.5% XP gain, max 20 catches = +10% (0.3% catch)
  - Abyssal Leech (Abyss fishing): Wither Resistance + Strength II for 3 minutes (1%)

**Legendary Fishing Rods:**
  - Angler's Pride (RARE, CMD 30201): 2× catch rate, fish never escape. Source: Quest reward.
  - Deep Sea Rod (EPIC, CMD 30202): Can fish in lava. Catches Nether fish + blaze rods +
    magma cream + rarely Netherite Scrap. Source: Sunken City structure chest.
  - Void Line (MYTHIC, CMD 30203): Can fish in End void (cast off island edge). Catches
    End materials + Void Jellyfish + rarely ender pearls + very rarely Shulker Shells.
    Source: Ender Monastery structure chest.
  - Abyssal Hook (ABYSSAL, CMD 30204): Can fish in Abyss void. Catches Abyssal Shards +
    Void Crystals + Abyssal Leech + very rarely Abyssal Essence. Source: Void Forge chest.

**Fishing Tournament Event** — 2% chance per dawn. 10-minute contest. Catch counter in
action bar. Winner (most rare fish) gets: 1 RARE weapon + 32 diamonds. All participants
get: 2× fish catch rate for 1 hour (buff).

---

### PHASE 33 — GUILD WARS & TERRITORIES ❌

**Territory Claims** — `/guild claim` in current chunk. Claims 3×3 chunk area. Max 5
territories per guild (increases with guild level). Claimed land: only guild members can
break/place blocks, PvP toggle per territory, guild banner auto-placed at territory center.
Unclaim: `/guild unclaim`. Territory visible on map (Dynmap integration if available) and
via particle border (subtle gold particles at chunk edges).

**Guild Bank** — Shared storage accessible via guild hall or `/guild bank`. Stores diamonds,
emeralds, Nether Stars. Used for: territory maintenance (1 diamond per territory per
real-time day), war declarations, guild upgrades.

**Guild Wars** — `/guild war declare <guildname>`. Costs 32 diamonds from guild bank.
Target guild has 5 minutes to accept. Once accepted: 30-minute war, PvP enabled between
guilds everywhere regardless of territory settings. Kill tracking scoreboard in sidebar.
Winner (most kills, minimum 5): receives losing guild's territory claims (they can
reclaim) + 50% of losing guild's bank + "[Conqueror]" title for all members for 24 hours.
Tie: both guilds keep everything, no rewards.

**Guild Levels** — XP from member activities: boss kill (50 XP), quest completion (20 XP),
structure discovery (10 XP), war victory (200 XP). Levels: 1/100/300/600/1000/1500/
2100/2800/3600/4500 XP thresholds (max level 10).
Level benefits:
  - L1: 1 territory, 27-slot bank
  - L3: 3 territories, 54-slot bank, +2% damage guild-wide
  - L5: 5 territories, guild hall structure, +5% damage guild-wide
  - L7: 7 territories, +8% damage, +5% HP guild-wide
  - L10: 10 territories, +10% damage, +10% HP, exclusive "[Legendary Guild]" tag

**Guild Hall** — Placeable structure via `/guild hall create` (requires Guild L5, costs
64 diamonds + 32 Nether Stars). Generates a 20×15×20 building with: guild chest (shared
54-slot bank storage), guild anvil (free repairs for guild members), guild enchanting table
(+2 enchant levels for guild members), war room with armor stand displays, guild banner.
Indestructible by non-guild members.

---

### PHASE 34 — CRAFTABLE EARLY-GAME ARMOR + MISC ❌

**Copper Armor Set** — 10 total defense. Craftable with copper ingots (standard pattern).
Special: oxidizes over time — after 3 real-time hours of wear, gains green tint + passive
Conduit Power while in rain. Waxable with honeycomb to prevent oxidation.

**Bone Armor Set** — 8 total defense. Craftable with bones + string (standard pattern).
Special: +30% damage to Undead while wearing full set. Rattling sound when hit (cosmetic).

**Leather Reinforced Set** — 12 total defense. Craftable with leather + iron nuggets.
Special: no armor penalty to movement speed (vanilla heavy armor slows you slightly, this
doesn't). Dyeable like normal leather armor.

**New Consumable Items:**
  - Warp Scroll (crafted: 4 ender pearls + 1 paper + 1 Lapis): teleport to world spawn.
    Single use. Useful in Abyss/End.
  - Battle Horn (crafted: 3 copper + 1 goat horn): +20% damage to all players in 20-block
    radius for 30s. 5-minute cooldown. Announced: "PlayerName blew the Battle Horn!"
  - Pocket Campfire (crafted: 3 sticks + 1 coal + 1 leather): place anywhere, right-click
    to sit and regenerate 2 HP/s. Breaks after 60s. Useful in dungeons.
  - Grappling Hook (crafted: 3 chains + 1 tripwire hook + 1 string): throw like ender
    pearl, pulls player to landing point. 10s cooldown. Doesn't work in void.

---

### PHASE 35 — HARDCORE CHALLENGE MODES ❌

**Challenge Tokens** — Consumable items that activate modifiers for 1 real-time hour:
  - Iron Will (CMD 30301): All mobs +100% HP, all loot drops +50% quantity. Source: NPC Wizard (32 diamonds).
  - Glass Cannon (CMD 30302): Player deals 2× damage, takes 2× damage. Source: Quest reward.
  - Nightmare (CMD 30303): Permanent Darkness, mobs invisible until 5 blocks away, bosses
    +200% HP. Reward: completing any boss under Nightmare gives exclusive black particle
    weapon aura (permanent cosmetic). Source: Blood Moon King (5% drop).
  - Speedrun (CMD 30304): Dungeon timer reduced to 20 minutes. Completing = bonus MYTHIC
    weapon + "[Speedrunner]" title. Source: Complete 5 dungeons normally first.
  - Ironman (CMD 30305): No healing from any source except natural regen (which is halved).
    Last 1 hour. Reward: +5 Soul Fragments. Source: NPC Wizard (16 diamonds).

Only 1 challenge active at a time. Challenge visible as boss bar at top of screen.

---

### IMPLEMENTATION PRIORITY ORDER

1. **Phase 20** (End Rift Event) — Small, uses existing drop pools, high impact
2. **Phase 22** (Infinity Gauntlet + Thanos) — Player excitement, iconic
3. **Phase 21** (Pillager System) — Major overworld gameplay loop
4. **Phase 23** (Quest/NPC Villagers) — Gives structure to all content
5. **Phase 25** (Legendary Bows) — Fills critical ranged weapon gap
6. **Phase 26** (Ocean Expansion) — Huge untapped world area
7. **Phase 27** (World Bosses) — Multiplayer community events
8. **Phase 24** (Abyss Dimension) — Endgame capstone, biggest undertaking
9. **Phase 29** (Pets) — Fun factor, reward for boss hunting
10. **Phase 30** (Procedural Dungeons) — Infinite replayability
11. **Phase 31** (Shields/Off-hand) — Combat depth
13. **Phase 32** (Fishing) — Relaxation + niche progression
14. **Phase 33** (Guild Wars) — PvP endgame
15. **Phase 34** (Early Armor + Misc Items) — Quality of life
16. **Phase 35** (Challenge Modes) — Hardcore replayability

Total new content when all phases complete:
  - 75+ legendary weapons (59 current + 4 ABYSSAL + 8 ranged + 4+ from sets)
  - 10+ armor sets (6 current + Abyssal Plate + Hydra Scale + Copper/Bone/Leather)
  - 40+ structures (27 current + 13 new)
  - 10+ events (4 current + End Rift + Kraken + Enhanced Raid + Fishing Tournament + seasonal)
  - 6 world bosses (3 Overworld + 3 Nether)
  - 7 tameable pets
  - Procedural dungeon system (infinite variations)
  - Full quest system with 25+ quests
  - Guild territory/war PvP system
  - Infinity Gauntlet + Abyss Dimension endgame
  - Seasons affecting the entire world
  - 8 legendary shields/off-hand items
  - 6+ legendary fishing rods/fish
  - 5 challenge modes






## N. EXPANSION ROADMAP — FUTURE PHASES (v2.0.0 → v3.0.0)

Everything below is planned content. Phases are ordered by implementation priority.
Status key: ❌ = Not started | 🔨 = In progress | ✅ = Complete

---

### Phase 20 — End Rift Overworld Event ❌
**Priority: HIGHEST — small scope, reuses existing weapon pools**

A 10% chance triggers when the Ender Dragon dies. A massive purple End-portal-textured ring 
(15×15 blocks) opens at a random location within 500 blocks of world spawn. The rift spawns 
escalating waves of Endermen, Shulkers, and Phantom swarms over 10 minutes, culminating in 
the **End Rift Dragon** — a custom Ender Dragon variant (600 HP, 2× damage, void breath = 
Wither + Blindness, lightning strikes, summons Shulker turrets, no crystals to heal). Drops 
1–2 MYTHIC weapons from the End Rift pool (Tengen's Blade, Soul Devourer, Star Edge, 
Creation Splitter, Stop Sign). Rift closes after boss death or 15-minute timeout.

New files:
- `events/EndRiftEvent.java` (~15 KB) — wave spawning, portal visuals, boss mechanics
- Update `EventManager.java` — dragon-death hook, rift trigger logic

---

### Phase 21 — Enhanced Pillager Warfare System ❌
**Priority: HIGH — makes overworld exploration dangerous and rewarding**

Completely overhauls Pillager encounters with three new systems:

**A. Pillager War Parties** — Roaming squads of 8–15 Pillagers spawn in plains, forests, and 
savanna biomes. Led by a **War Chief** (Vindicator, 250 HP, diamond armor, banner carrier). 
War parties actively hunt players within 100 blocks. Drops: RARE weapons, emeralds, banners.

**B. Pillager Siege Waves** — When a player sleeps in an Ultra Village, there is a 20% chance 
of triggering a multi-wave siege the next night. Five escalating waves:
- Wave 1: 15 Pillagers + 2 Ravagers
- Wave 2: 25 Pillagers + 3 Ravagers + 1 Evoker
- Wave 3: 30 Pillagers + 5 Ravagers + 2 Evokers + Vex swarm
- Wave 4: 40 Pillagers + Iron Golem defectors (hostile) + War Chief
- Wave 5: **Pillager Warlord** boss (Vindicator, 600 HP, netherite armor, ground-slam AoE, 
  rallying cry that buffs all remaining Pillagers)
Surviving all 5 waves rewards: 2 EPIC weapons, Heart Crystal, 64 emeralds, Hero of the 
Village X effect (30 minutes).

**C. Pillager Structures** — Two new structures added to StructureType:
- **Pillager Fortress** (Plains/Savanna, 60×35×60, EPIC loot) — Multi-story fortified base 
  with watchtowers, prison cells holding captive villagers, armory chests, and the War Chief 
  mini-boss (350 HP). Freeing prisoners grants reputation + emerald rewards.
- **Pillager Airship** (Any overworld, 40×25×15, floating at Y 150–200, MYTHIC loot) — A 
  massive flying ship made of dark oak, copper, and wool sails, anchored by chains. Contains 
  a **Sky Captain** mini-boss (Pillager, 400 HP, crossbow with explosive bolts, elytra). 
  Chests contain MYTHIC weapons, elytra, and fireworks.

New files:
- `events/PillagerWarPartyEvent.java` (~12 KB)
- `events/PillagerSiegeEvent.java` (~18 KB)
- `events/PillagerSiegeWave.java` (~5 KB) — wave definition data class
- Update `StructureType.java` — add PILLAGER_FORTRESS, PILLAGER_AIRSHIP
- Update `StructureBuilder.java` — fortress and airship build methods

---

### Phase 22 — Infinity Gauntlet + Thanos ❌
**Priority: HIGH — most exciting secret endgame content**

**Thanos Temple** — New MYTHIC structure (Badlands/Mountains, 50×40×50, deepslate + gold 
blocks + purple stained glass). Interior: trapped corridors, puzzle rooms with redstone 
mechanisms, throne room. Boss: **Thanos** (custom Iron Golem, 800 HP, purple/gold texture, 
ground-slam AoE dealing 15 damage in 5-block radius, energy beam attack = Wither II + 
Slowness III for 5 seconds, phases at 50% HP with speed boost + double damage). Guaranteed 
drop: **Thanos Glove** (custom item, CMD 40001).

**Infinity Stone Fragments** — Six colored stone fragments (secret, undocumented items). 
Each has a 0.1% chance to drop from any normal mob killed during a Blood Moon. They do NOT 
appear in any in-game documentation.

| Stone | Color | CMD | Texture |
|-------|-------|-----|---------|
| Power Stone | Purple | 40010 | `infinity_stone_power` |
| Space Stone | Blue | 40011 | `infinity_stone_space` |
| Reality Stone | Red | 40012 | `infinity_stone_reality` |
| Soul Stone | Orange | 40013 | `infinity_stone_soul` |
| Time Stone | Green | 40014 | `infinity_stone_time` |
| Mind Stone | Yellow | 40015 | `infinity_stone_mind` |

**Crafting chain**: Fragment + Nether Star in anvil → finished Infinity Stone. Then: Thanos 
Glove + all 6 Infinity Stones = **Infinity Gauntlet** (shapeless recipe).

**Gauntlet ability**: Right-click kills 50% of all loaded hostile mobs in current dimension. 
Excludes bosses, mini-bosses, and event mobs. 300-second cooldown. Visual: gold particle 
snap burst, screen-shake, dramatic sound. Indestructible.

New files:
- `legendary/InfinityGauntletManager.java` (~10 KB)
- `legendary/InfinityStoneManager.java` (~8 KB)
- Update `StructureType.java` — add THANOS_TEMPLE
- Update `RecipeManager.java` — gauntlet recipe

---

### Phase 23 — Quest Villagers & NPC Wizard ❌
**Priority: HIGH — gives players direction and goals**

**Quest Villager** — Custom Nitwit with name "§6Questmaster". Spawns in Ultra Villages. 
Opens a chest GUI with quest categories. Quests refresh weekly.

| Category | Examples | Rewards |
|----------|----------|---------|
| Overworld | Kill 50 zombies, Find a Ruined Colosseum, Survive a Blood Moon | Soul Fragments, Heart Crystals, COMMON weapons |
| Nether | Kill 20 Blazes, Clear Crimson Citadel, Defeat Piglin King | Blessing Crystals, RARE weapons, Nether Star shards |
| End | Kill 100 Endermen, Defeat End Rift Dragon, Find Dragon's Hoard | MYTHIC weapons, Phoenix Feather |
| Special | Collect all 6 Infinity Stones, Defeat all 5 bosses, Earn God Slayer | Exclusive title, one ABYSSAL weapon choice |

Special sale item: **KeepInventorer** — consumable that permanently enables keep-inventory 
for the purchasing player. Cost: 64 diamonds + 32 emeralds.

**NPC Wizard** — Custom villager with name "§5Archmage". Spawns in Mage Towers and Ultra 
Villages. Sells rotating stock of 3 exclusive MYTHIC weapons (48 diamonds + 16 Nether Stars 
each), Heart Crystals (32 diamonds), Blessing Crystals (16 diamonds), and enchanted books 
(custom enchantments at max level, 24 diamonds each).

New files:
- `npcs/QuestVillagerManager.java` (~15 KB)
- `npcs/QuestDefinition.java` (~4 KB)
- `npcs/QuestTracker.java` (~8 KB)
- `npcs/WizardVillagerManager.java` (~10 KB)

---

### Phase 24 — Abyss Dimension ❌
**Priority: MEDIUM-HIGH — massive endgame expansion**

A custom dimension that mirrors The End's floating-island aesthetic but with a dark crimson 
and void color palette. No flat terrain — only scattered floating islands of varying sizes 
suspended above the void, connected by narrow obsidian bridges and occasional portal 
gateways between island clusters.

**Portal**: Nether portal shape (4×5 frame minimum) built with **Purpur Blocks** instead of 
obsidian. Activated by right-clicking the frame with a custom **Abyssal Key** item (dropped 
by the Ender Dragon, 100% chance). The portal texture is dark red/purple swirling particles. 
Entering the portal teleports the player to the Abyss world.

**World generation**: Floating islands of end stone, deepslate, crying obsidian, and sculk 
blocks. Islands range from small (10×10) to massive (80×80). The void below kills instantly 
at Y -64. Ambient particles: soul fire, reverse-falling ash, occasional void tendrils. 
Lighting: perpetual twilight, no day/night cycle. Hostile mobs spawn at 3× normal rate with 
2× base HP.

**Abyss Structures** (5 new entries in StructureType):

| Structure | Size | Boss | HP | Loot Tier | Description |
|-----------|------|------|----|-----------|-------------|
| Abyssal Fortress | 60×40×60 | Abyssal Knight | 500 | ABYSSAL | Dark stone fortress on largest island, multi-floor, trapped hallways |
| Void Spire | 15×80×15 | Void Watcher | 350 | MYTHIC | Impossibly tall obsidian tower, spiral staircase, eye-of-ender theme |
| Corrupted Library | 40×20×40 | The Archivist | 300 | MYTHIC | Shelves of enchanted books, puzzle to unlock vault |
| Soul Forge | 30×25×30 | Forge Master | 400 | ABYSSAL | Lava-and-sculk workshop, crafting table for abyssal upgrades |
| Abyss Dragon Lair | 100×60×100 | Abyss Dragon | 1000 | ABYSSAL | Largest island, obsidian nest, multi-phase boss arena |

**Abyss Dragon** — The ultimate boss. 1000 HP, 3 phases:
- Phase 1 (100–60% HP): Standard dragon flight, void breath (Wither III + Blindness), 
  summons Abyssal Endermen (200 HP each, teleport aggressively)
- Phase 2 (60–30% HP): Lands and fights melee, tail swipe AoE, ground slam creates void 
  fissures (instant 10 damage if stepped on), speed increase
- Phase 3 (30–0% HP): Enrages, all attacks deal 2× damage, summons void meteors (falling 
  entities dealing 20 AoE damage), arena crumbles at edges

Drops: 2–3 ABYSSAL weapons (Requiem Awakened, Excalibur Awakened, Creation Splitter 
Awakened, Whisperwind Awakened), Abyssal Plate armor pieces (guaranteed 1, 25% chance for 
second piece), Dragon Heart power-up (+2 max hearts permanently, unique).

New files:
- `abyss/AbyssWorldManager.java` (~20 KB) — world creation, portal mechanics, generation
- `abyss/AbyssPortalListener.java` (~8 KB) — purpur frame detection, key activation
- `abyss/AbyssDragonBoss.java` (~15 KB) — 3-phase boss AI
- `abyss/AbyssMobManager.java` (~6 KB) — custom abyss mobs
- Update `StructureType.java` — 5 abyss structures
- Update `LegendaryArmorSet.java` — add ABYSSAL_PLATE set

---

### Phase 25 — Legendary Ranged Weapons ❌
**Priority: MEDIUM — expands combat variety**

15 legendary bows and crossbows, each with unique primary + alt abilities.

| # | Name | Type | Dmg | Tier | Primary | Alt |
|---|------|------|-----|------|---------|-----|
| 64 | Artemis' Longbow | BOW | 16 | MYTHIC | Moonshot (homing arrow) | Rain of Arrows (15 arrows in AoE) |
| 65 | Windforce | BOW | 14 | EPIC | Gale Arrow (knockback 10) | Tornado Shot (spinning arrow AoE) |
| 66 | Hellfire Crossbow | CROSSBOW | 18 | MYTHIC | Inferno Bolt (fire AoE on impact) | Napalm Barrage (3 fire bolts) |
| 67 | Frostbite | BOW | 13 | RARE | Frost Arrow (slowness III) | Blizzard (AoE freeze 5s) |
| 68 | Soulpiercer | CROSSBOW | 17 | MYTHIC | Soul Bolt (ignores armor) | Spirit Volley (5 homing bolts) |
| 69 | Eclipse Bow | BOW | 15 | EPIC | Shadow Arrow (blindness + damage) | Lunar Eclipse (darkness AoE) |
| 70 | Dragonfire Repeater | CROSSBOW | 19 | MYTHIC | Dragon Bolt (fire breath trail) | Wyrmfire Barrage (rapid 8 bolts) |
| 71 | Titan's Ballista | CROSSBOW | 22 | MYTHIC | Siege Bolt (5-block explosion) | Fortify (shield + slow reload) |
| 72 | Verdant Bow | BOW | 12 | RARE | Vine Arrow (roots target 3s) | Nature's Embrace (heal AoE) |
| 73 | Thunder Cannon | CROSSBOW | 20 | MYTHIC | Lightning Rod (chain lightning) | Thunderclap (AoE stun 2s) |
| 74 | Phantom Bow | BOW | 14 | EPIC | Phase Arrow (passes through walls) | Spectral Volley (invisible arrows) |
| 75 | Coral Crossbow | CROSSBOW | 15 | EPIC | Bubble Shot (levitation 3s) | Depth Charge (water explosion) |
| 76 | Blood Hunter's Bow | BOW | 16 | EPIC | Crimson Arrow (lifesteal 30%) | Blood Rain (AoE bleed) |
| 77 | Celestial Longbow | BOW | 20 | MYTHIC | Star Arrow (meteor on impact) | Constellation (5 guided stars) |
| 78 | Void Crossbow | CROSSBOW | 18 | MYTHIC | Void Bolt (gravity pull) | Black Hole (AoE slow + damage) |

New files:
- `legendary/LegendaryRangedWeapon.java` (~6 KB) — enum
- `legendary/LegendaryRangedManager.java` (~10 KB) — item creation
- `legendary/LegendaryRangedAbilities.java` (~40 KB) — all abilities
- `legendary/LegendaryRangedListener.java` (~15 KB) — event handling

---

### Phase 26 — Ocean Expansion ❌
**Priority: MEDIUM — new dimension-like biome content**

Massively expands ocean gameplay with underwater structures, a world boss, and a new event.

**New Structures** (added to StructureType):

| Structure | Biome | Size | Boss | HP | Loot | Description |
|-----------|-------|------|------|----|------|-------------|
| Sunken City | Deep Ocean | 100×30×100 | Drowned King | 500 | EPIC | Massive underwater ruins with air pockets, coral gardens, treasure vaults |
| Ghost Ship | Ocean (surface) | 50×30×20 | Captain Blacktide | 350 | EPIC | Half-sunk galleon, skeletal crew, cannon traps, captain's quarters |
| Coral Palace | Warm Ocean | 40×25×40 | Coral Guardian | 300 | RARE | Prismarine + coral palace, guardian spawners, throne room |
| Underwater Cave System | Deep Ocean | 60×40×60 | Cave Leviathan | 450 | EPIC | Interconnected underwater caves, glow lichen, drowned nests |
| Shipwreck Graveyard | Ocean | 80×20×80 | None (5+ loot chests) | — | RARE | Cluster of 4–6 shipwrecks piled together, barnacle-covered |
| Mermaid Grotto | Lukewarm Ocean | 25×20×25 | None (friendly NPC) | — | RARE | Hidden cave with a Mermaid NPC that trades ocean loot for RARE weapons |

**Kraken World Event** — 3% chance when player is in a boat on deep ocean for 5+ minutes. 
The **Kraken** (Elder Guardian variant, 800 HP, tentacle attacks = launched Guardians, ink 
cloud = Blindness AoE, whirlpool = pulls all entities toward center, body slam = 20 damage 
AoE). Drops: 2 MYTHIC weapons, Trident of the Deep (new MYTHIC trident, #79, 20 damage, 
abilities: Maelstrom Strike / Abyssal Tide), Heart Crystal, 10–20 prismarine shards.

New files:
- `events/KrakenEvent.java` (~12 KB)
- `structures/OceanStructureBuilder.java` (~15 KB) — underwater building utilities
- Update `StructureType.java` — 6 ocean structures

---

### Phase 27 — World Bosses ❌
**Priority: MEDIUM — adds roaming endgame threats across all dimensions**

World bosses are massive, rare creatures that roam specific biomes. Only one can exist per 
world at a time. They spawn naturally (0.5% per hour check) or can be summoned via a ritual 
at specific structures. All are visible on the map via a boss bar when within 200 blocks.

**Overworld World Bosses:**

| Boss | Biome | HP | Mechanic | Drops |
|------|-------|----|----------|-------|
| The Colossus | Mountains/Stony | 1200 | 15-block-tall Iron Golem, ground pound creates shockwave (15 dmg, 10-block radius), throws boulders (falling blocks), stomp stun (2s). At 30% HP, splits into 3 Mini-Colossi (300 HP each) | 2 MYTHIC weapons, Titan's Resolve, 3 Heart Crystals |
| Ancient Hydra | Swamp/Mangrove | 900 (3 heads × 300 HP) | Three-headed (3 Wither Skeleton riders on a Ravager). Each head must be killed separately. Heads regenerate after 30s if not all killed. Poison breath, tail lash | 2 MYTHIC weapons, 5 Soul Fragments, Hydra Scale (new power-up: +15% poison immunity) |
| The Lich King | Dark Forest/Pale Garden | 800 | Undead necromancer (Evoker base). Summons skeleton armies, casts wither bolts, teleports, creates bone cage traps. At 50% HP, raises a Bone Dragon minion (400 HP) | 2 MYTHIC weapons, Lich's Phylactery (respawn point override, 1 use), 5 Soul Fragments |
| Sandstorm Titan | Desert/Badlands | 1000 | Husk-based giant. Creates sandstorms (reduced visibility + Slowness), summons sand pillars (falling block damage), quicksand traps, earthquake stomp | 2 MYTHIC weapons, Desert Crown (new helmet, sand immunity, EPIC tier), 3 Heart Crystals |

**Nether World Bosses:**

| Boss | Biome | HP | Mechanic | Drops |
|------|-------|----|----------|-------|
| Infernal Titan | Nether Wastes | 1100 | Massive Blaze variant. Fireball barrage (12 fireballs), lava eruption (floor becomes lava in 10-block radius for 10s), flame dash (charges through players), ember shield (reflects projectiles) | 2 MYTHIC weapons, Infernal Core (permanent Fire Resistance I), 4 Soul Fragments |
| The Crimson Mother | Crimson Forest | 900 | Giant Hoglin matriarch. Charge attack, spawns Baby Hoglins (50 HP, swarm), crimson spore cloud (Nausea + Poison), ground tremor (crack pattern damage) | 2 EPIC weapons, Crimson Heart (permanent +2 HP in Nether), Nether Star |
| Wither Storm | Soul Sand Valley | 1500 | Three Withers fused together on a giant skeleton body. Triple wither skull barrage, soul drain beam (steals HP), gravitational pull, spawns Wither Skeletons continuously. Requires destroying 3 cores (one per head, 500 HP each) before main body takes damage | 3 MYTHIC weapons, Wither Heart (new power-up: Wither immunity + 10% damage boost to undead), 2 Nether Stars |

**End World Boss:**

| Boss | Location | HP | Mechanic | Drops |
|------|----------|----|----------|-------|
| Void Leviathan | End Islands (roaming) | 1200 | Elder-Guardian-shaped void creature. Void beam (15 dmg/s channel), teleport ambush, spawns Void Tentacles (destructible, grab + throw players), devour (instant kill below 20% player HP, dodgeable). Arena: destroys blocks it touches | 3 MYTHIC weapons, Void Essence (permanent Slow Falling in End), Abyssal Key |

New files:
- `mobs/WorldBossManager.java` (~20 KB) — spawn logic, boss bars, tracking
- `mobs/WorldBossAI.java` (~25 KB) — individual boss mechanics
- `mobs/WorldBossLoot.java` (~8 KB) — drop tables and announcements

---

### Phase 28 — Dungeon Generator ❌
**Priority: MEDIUM — procedural content for infinite replayability**

Procedurally generated multi-room dungeons that spawn underground (Y 0–30) in all 
dimensions. Each dungeon consists of 5–15 connected rooms selected from a room template pool. 
Rooms are connected by corridors with random traps (arrow dispensers, lava pits, falling 
blocks, poison dart walls). Each dungeon has a guaranteed boss room at the end.

**Dungeon Tiers:**

| Tier | Rooms | Depth | Dimension | Boss Room | Loot |
|------|-------|-------|-----------|-----------|------|
| Minor | 5–7 | Y 20–30 | Overworld | Dungeon Guardian (Zombie, 200 HP) | COMMON–RARE |
| Standard | 8–10 | Y 10–20 | Overworld/Nether | Dungeon Lord (Skeleton, 350 HP) | RARE–EPIC |
| Grand | 11–15 | Y 0–10 | Any | Dungeon Overlord (custom, 500 HP) | EPIC–MYTHIC |
| Abyssal | 12–15 | Y varies | Abyss only | Abyssal Warden (custom, 700 HP) | MYTHIC–ABYSSAL |

**Room Templates** (20+ templates):
Treasure Room (multi-chest), Mob Arena (wave survival to unlock exit), Puzzle Room (lever 
sequence, pressure plate maze), Lava Bridge (timed crossing), Library (enchanted book loot), 
Spawner Room (4 spawners, must destroy all), Flooded Room (underwater section), Collapsed 
Room (parkour over rubble), Throne Room (mini-boss), Forge Room (free weapon upgrade 
station), Prison Room (free captive villagers for loot), Trap Corridor (gauntlet of traps), 
Mushroom Garden (potion ingredient loot), Crystal Cave (ore bonanza + cave spider ambush), 
Boss Antechamber (preparation room before boss), Secret Room (hidden behind destructible 
wall, rare loot), Vault (locked, requires key from another room).

New files:
- `dungeons/DungeonGenerator.java` (~20 KB) — procedural layout algorithm
- `dungeons/DungeonRoom.java` (~5 KB) — room template data class
- `dungeons/DungeonRoomBuilder.java` (~25 KB) — room construction methods
- `dungeons/DungeonTrapManager.java` (~10 KB) — trap mechanics
- `dungeons/DungeonBossManager.java` (~8 KB) — dungeon boss spawning

---

### Phase 29 — Legendary Shields & Off-hand Items ❌
**Priority: MEDIUM — completes the equipment system**

12 legendary shields and 6 legendary off-hand items (spellbooks, orbs, war banner), each 
with passive effects when held in the off-hand slot.

| # | Name | Type | Tier | Passive | Active (right-click while sneaking) |
|---|------|------|------|---------|-------------------------------------|
| 80 | Dragon Scale Shield | SHIELD | MYTHIC | -20% fire damage | Dragon Roar (fear AoE 5s, 60s CD) |
| 81 | Void Barrier | SHIELD | MYTHIC | Absorb 1 hit every 30s | Void Reflect (return 50% damage, 45s CD) |
| 82 | Crystal Ward | SHIELD | EPIC | +10% magic resistance | Crystal Explosion (AoE 8 dmg, 40s CD) |
| 83 | Blood Buckler | SHIELD | EPIC | 3% lifesteal on block | Blood Shield (15 HP shield, 50s CD) |
| 84 | Stormguard | SHIELD | EPIC | Lightning on perfect block | Chain Lightning (3 targets, 30s CD) |
| 85 | Bone Wall | SHIELD | RARE | Thorns II equivalent | Bone Cage (trap target 3s, 45s CD) |
| 86 | Nature's Aegis | SHIELD | RARE | Passive regen I while blocking | Vine Wall (blocking barrier, 40s CD) |
| 87 | Frost Bulwark | SHIELD | EPIC | Slowness I to melee attackers | Ice Wall (3-high ice barrier, 35s CD) |
| 88 | Infernal Rampart | SHIELD | MYTHIC | Fire aura (1 dmg/s to nearby enemies) | Flame Wave (AoE 12 dmg, 50s CD) |
| 89 | Abyssal Aegis | SHIELD | ABYSSAL | -30% all damage while blocking | Absolute Defense (invulnerable 3s, 120s CD) |
| 90 | Tome of Storms | SPELLBOOK | MYTHIC | +15% lightning damage | Summon Lightning Storm (5 bolts, 60s CD) |
| 91 | Orb of Souls | ORB | MYTHIC | +10% Soul Fragment gain | Soul Burst (AoE 15 dmg + heal, 50s CD) |
| 92 | Necronomicon | SPELLBOOK | EPIC | Summon 2 skeleton allies | Raise Dead (5 skeleton army, 90s CD) |
| 93 | Phoenix Orb | ORB | MYTHIC | Auto-revive (180s internal CD) | Phoenix Burst (AoE fire + heal, 60s CD) |
| 94 | War Banner of the Conqueror | BANNER | EPIC | +10% damage for all nearby allies (8 blocks) | Battle Cry (AoE Strength II 10s, 120s CD) |
| 95 | Void Grimoire | SPELLBOOK | ABYSSAL | +20% ability damage | Void Rift (teleport + AoE 20 dmg, 90s CD) |

New files:
- `legendary/LegendaryOffhand.java` (~5 KB) — enum
- `legendary/LegendaryOffhandManager.java` (~10 KB)
- `legendary/LegendaryOffhandListener.java` (~20 KB)

---

### Phase 30 — Pet System ❌
**Priority: MEDIUM-LOW — fun quality-of-life system**

Mythical pets that follow the player, provide passive buffs, and can assist in combat. 
Obtained from boss drops, structure chests, or NPC Wizard. Only one active pet at a time. 
Pets are indestructible but can be dismissed. Commands: `/pet summon`, `/pet dismiss`, 
`/pet list`, `/pet rename`.

| Pet | Source | Passive Buff | Combat Ability | Appearance |
|-----|--------|-------------|----------------|------------|
| Baby Dragon | Ender Dragon (5%) | Fire Resistance | Fireball every 15s | Small dragon (Bee model, fire particles) |
| Shadow Cat | Dungeon Grand tier (10%) | Night Vision + Stealth | Pounce attack every 10s | Black cat with void particles |
| Phoenix Chick | Phoenix's Grace alt (3%) | Slow regen + auto-revive pet | Fire dash every 20s | Small chicken, flame particles |
| Void Wisp | Void Shrine chest (8%) | Slow Falling + Ender pearl no damage | Void bolt every 12s | Floating orb, purple particles |
| Crystal Golem | Allay Sanctuary (15%) | +5% all damage resistance | Ground pound every 20s | Small iron golem, amethyst particles |
| Storm Hawk | Pillager Airship (10%) | Speed I while outdoors | Lightning strike every 15s | Parrot with lightning particles |
| Necro Pup | Lich King drop (8%) | +10% XP gain | Summon skeleton every 30s | Wolf with green eyes, soul particles |
| Magma Sprite | Nether World Boss (5%) | Fire Resistance + lava walk | Fireball burst every 15s | Tiny Blaze, magma particles |

New files:
- `pets/PetManager.java` (~15 KB)
- `pets/PetListener.java` (~12 KB)
- `pets/PetType.java` (~4 KB)
- `pets/PetAI.java` (~10 KB)

---

### Phase 31 — Seasons & Weather System ❌
**Priority: LOW — atmospheric enhancement**

Four seasons cycle every 7 real-world days (configurable). Each season affects mob spawning, 
crop growth, and adds visual effects.

| Season | Duration | Effects | Visual |
|--------|----------|---------|--------|
| Spring | Days 1–7 | +50% crop growth, +20% passive mob spawns, rain 30% more frequent | Flower particles, green tint |
| Summer | Days 8–14 | +25% fire spread, deserts deal 0.5 dmg/s without water nearby, +15% hostile mob damage | Heat shimmer particles, orange tint |
| Autumn | Days 15–21 | +30% leaf decay, +20% drop rates, mushroom growth boost | Falling leaf particles, amber tint |
| Winter | Days 22–28 | Snow in all biomes above Y 90, water freezes in cold biomes, -20% movement speed outdoors without boots, +25% undead spawns | Snowfall particles, blue tint |

New files:
- `utility/SeasonManager.java` (~12 KB)
- `utility/SeasonListener.java` (~8 KB)

---

### Phase 32 — Fishing Overhaul ❌
**Priority: LOW — relaxing side content**

Custom fish, legendary fishing rods, and a fishing tournament event.

**Legendary Fish** (10 types): Each has a custom texture and grants a temporary buff when 
consumed (Golden Koi = Luck III 5min, Void Bass = Night Vision 10min, Lava Eel = Fire 
Resistance 5min, etc.). Rare fish have a 1–5% catch rate.

**Legendary Fishing Rods** (3):
- Rod of the Deep (#96, EPIC, +30% rare fish chance, Luck of the Sea V)
- Abyssal Angler (#97, MYTHIC, auto-catch AFK fishing, double catch rate)
- Poseidon's Line (#98, MYTHIC, can fish up RARE weapons and Heart Crystals)

**Fishing Tournament** — Triggers randomly once per in-game week. 10-minute competition. 
Player who catches the most legendary fish wins: 1 EPIC weapon + 32 diamonds + title 
"Master Angler."

New files:
- `utility/FishingOverhaulManager.java` (~12 KB)
- `utility/FishingOverhaulListener.java` (~8 KB)

---

### Phase 33 — Guild Wars & Territories ❌
**Priority: LOW — multiplayer endgame**

Extends the existing guild system with territory claiming, guild levels, and wars.

**Territory System**: Guilds can claim chunks (max 9 per guild level). Claimed chunks 
prevent non-guild members from building/breaking. Guild banners mark territory.

**Guild Levels**: Earned through collective member activity (kills, quests, bosses). Level 
1–10, each level unlocks: +1 max members, +1 claimable chunk, guild perks (shared XP boost, 
shared loot bonus, guild home teleport).

**Guild Wars**: One guild can declare war on another (costs 32 diamonds). During a war 
(24 real hours), guild members can damage each other in any territory. The guild with more 
kills at the end wins the loser's treasury (stored diamonds/items). War MVP gets a unique 
weapon skin.

**Guild Hall** — New structure type. Guilds that reach level 5 can build a Guild Hall at 
their base. Provides: shared storage (double chest accessible by all members), guild 
crafting table (bonus recipes), guild beacon (permanent buffs in territory).

New files:
- `guilds/GuildTerritoryManager.java` (~12 KB)
- `guilds/GuildWarManager.java` (~10 KB)
- `guilds/GuildLevelManager.java` (~8 KB)
- Update `GuildManager.java` — territory and war hooks

---

### Phase 34 — Hardcore Challenge Modes ❌
**Priority: LOWEST — for experienced players seeking difficulty**

Optional challenge modifiers that increase difficulty in exchange for better loot. Activated 
via a **Challenge Token** (crafted: 4 Nether Stars + 4 diamonds + 1 dragon breath, or 
purchased from NPC Wizard for 64 diamonds).

| Challenge | Modifier | Loot Bonus |
|-----------|----------|------------|
| Iron Man | No natural regen, no totems | +50% weapon drop rate |
| Glass Cannon | Player takes 3× damage, deals 2× damage | +75% weapon drop rate |
| Swarm | All mob spawns tripled | +100% Soul Fragment drops |
| Famine | No food regen above 6 hunger bars | +50% Heart Crystal drops |
| Eclipse | Permanent darkness effect outdoors | +60% all drop rates |
| Corruption | Wither I permanent effect | +100% weapon drop rate |

Challenges last 1 real-world hour. Multiple can be stacked (bonuses multiply). Dying ends 
all active challenges with no bonus.

New files:
- `utility/ChallengeManager.java` (~10 KB)
- `utility/ChallengeListener.java` (~8 KB)

---

### N.1 — ADDITIONAL STRUCTURES (added across relevant phases)

These structures supplement existing phases and are added to `StructureType.java` as each 
phase is implemented:

| Structure | Dimension | Biome | Size | Boss | HP | Tier | Phase |
|-----------|-----------|-------|------|------|----|------|-------|
| Pillager Fortress | Overworld | Plains/Savanna | 60×35×60 | War Chief | 350 | EPIC | 21 |
| Pillager Airship | Overworld | Any (floating Y 150+) | 40×25×15 | Sky Captain | 400 | MYTHIC | 21 |
| Thanos Temple | Overworld | Badlands/Mountains | 50×40×50 | Thanos | 800 | MYTHIC | 22 |
| Sunken City | Overworld | Deep Ocean | 100×30×100 | Drowned King | 500 | EPIC | 26 |
| Ghost Ship | Overworld | Ocean (surface) | 50×30×20 | Captain Blacktide | 350 | EPIC | 26 |
| Coral Palace | Overworld | Warm Ocean | 40×25×40 | Coral Guardian | 300 | RARE | 26 |
| Underwater Caves | Overworld | Deep Ocean | 60×40×60 | Cave Leviathan | 450 | EPIC | 26 |
| Shipwreck Graveyard | Overworld | Ocean | 80×20×80 | None | — | RARE | 26 |
| Mermaid Grotto | Overworld | Lukewarm Ocean | 25×20×25 | None (NPC) | — | RARE | 26 |
| Abyssal Fortress | Abyss | Any (floating island) | 60×40×60 | Abyssal Knight | 500 | ABYSSAL | 24 |
| Void Spire | Abyss | Any (floating island) | 15×80×15 | Void Watcher | 350 | MYTHIC | 24 |
| Corrupted Library | Abyss | Any (floating island) | 40×20×40 | The Archivist | 300 | MYTHIC | 24 |
| Soul Forge | Abyss | Any (floating island) | 30×25×30 | Forge Master | 400 | ABYSSAL | 24 |
| Abyss Dragon Lair | Abyss | Largest island | 100×60×100 | Abyss Dragon | 1000 | ABYSSAL | 24 |
| Frost Dungeon | Overworld | Snowy/Frozen | 35×25×35 | Frost Giant | 400 | EPIC | 27 |
| Volcanic Forge | Nether | Basalt Deltas | 35×30×35 | Forge Demon | 450 | EPIC | 27 |

**Total structures after all phases: 27 (current) + 16 (new) = 43 structures**

---

### N.2 — TOTAL CONTENT SUMMARY (after all phases)

| Category | Current | After Expansion | Notes |
|----------|---------|-----------------|-------|
| Legendary Melee Weapons | 59 | 63 (+ 4 ABYSSAL) | IDs 30001–30063 |
| Legendary Ranged Weapons | 0 | 15 | IDs 30064–30078 |
| Legendary Shields/Offhand | 0 | 16 | IDs 30080–30095 |
| Legendary Fishing Rods | 0 | 3 | IDs 30096–30098 |
| Legendary Armor Sets | 6 | 8 (+ Abyssal Plate, Desert Crown) | |
| Custom Structures | 27 | 43 | All 3 dimensions + Abyss |
| World Events | 4 | 8 (+ End Rift, Kraken, Pillager Siege, Fishing Tournament) | |
| World Bosses | 0 | 8 (4 OW, 3 Nether, 1 End) | |
| Dungeon Types | 0 | 4 tiers (procedural) | |
| Custom Enchantments | 64 | 64 (unchanged) | |
| Boss Mastery Titles | 6 | 8 (+ Abyss Dragon, God Slayer) | |
| Pets | 0 | 8 | |
| Quests | 0 | ~20 unique quests | |
| NPC Types | 0 | 2 (Questmaster, Archmage) | |
| Seasons | 0 | 4 | |
| Challenge Modes | 0 | 6 | |
| Total Java Files | 52 | ~85 estimated | |
| Dimensions | 3 (vanilla) | 4 (+ Abyss) | |

---

### N.3 — IMPLEMENTATION ORDER

| Order | Phase | Est. New Files | Est. LOC | Depends On |
|-------|-------|---------------|----------|------------|
| 1st | Phase 20 (End Rift Event) | 1–2 | ~800 | Nothing |
| 2nd | Phase 22 (Infinity Gauntlet) | 2–3 | ~1,200 | Nothing |
| 3rd | Phase 21 (Pillager Warfare) | 3–4 | ~2,000 | Nothing |
| 4th | Phase 23 (Quest NPCs) | 4 | ~2,500 | Nothing |
| 5th | Phase 25 (Ranged Weapons) | 4 | ~4,000 | Nothing |
| 6th | Phase 26 (Ocean Expansion) | 2–3 | ~2,000 | Nothing |
| 7th | Phase 27 (World Bosses) | 3 | ~3,500 | Nothing |
| 8th | Phase 24 (Abyss Dimension) | 4–5 | ~5,000 | Phase 22 (Abyssal Key) |
| 9th | Phase 28 (Dungeons) | 5 | ~5,000 | Nothing |
| 10th | Phase 29 (Shields/Offhand) | 3 | ~2,500 | Nothing |
| 11th | Phase 30 (Pets) | 4 | ~3,000 | Phase 27 (boss drops) |
| 12th | Phase 31 (Seasons) | 2 | ~1,500 | Nothing |
| 13th | Phase 32 (Fishing) | 2 | ~1,500 | Nothing |
| 14th | Phase 33 (Guild Wars) | 3 | ~2,000 | Existing guild system |
| 15th | Phase 34 (Challenge Modes) | 2 | ~1,500 | Nothing |





Full Repository Audit — JGlimsPlugin (commit a6a016e, 2026-03-08)
CONFIRMED IMPLEMENTED (all registered in onEnable, all have real code)
Legendary Weapons: 63 weapons across 5 tiers — 20 COMMON, 8 RARE, 7 EPIC, 24 MYTHIC (9 original + 15 new #45-59), 4 ABYSSAL (awakened). The LegendaryTier.java standalone enum is done with COMMON through ABYSSAL, particle budgets, cooldown multipliers, and validation methods. LegendaryPrimaryAbilities.java (70 KB) and LegendaryAltAbilities.java (73 KB) hold all 63 weapons' ability implementations.

Structures: 39 structure types in StructureType.java — 25 overworld (Ruined Colosseum, Druid's Grove, Shrek House, Mage Tower, Gigantic Castle, Fortress, Camping Small/Large, Ultra Village, two Witch Houses, Allay Sanctuary, Volcano, Ancient Temple, Abandoned House, House-Tree, Dungeon Deep, Thanos Temple, Pillager Fortress/Airship, Frost Dungeon, Bandit Hideout, Sunken Ruins, Cursed Graveyard, Sky Altar), 7 nether (Crimson Citadel, Soul Sanctum, Basalt Spire, Nether Dungeon, Piglin Palace, Wither Sanctum, Blaze Colosseum), 5 end (Void Shrine, Ender Monastery, Dragon's Hoard, End Rift Arena, Dragon Death Chest), 3 abyss (Abyssal Castle, Void Nexus, Shattered Cathedral). StructureManager.java (50 KB), StructureBuilder.java (9 KB), StructureLootPopulator.java (9 KB), and StructureBossManager.java (15 KB) are all present with full logic.

Mini-Bosses: 30+ structure bosses spawned via StructureBossManager — each StructureType with hasBoss=true has a dedicated spawn method. Specialized bosses include Frost Warden (Stray), Drowned Warlord, Grave Revenant, Wither Priest, Infernal Champion, Abyssal Overlord, Void Arbiter, and Fallen Archbishop. Generic bosses use Iron Golem, Zombie, Evoker, Vindicator, PiglinBrute, Pillager, Witch, Phantom, MagmaCube, Husk, Hoglin, WitherSkeleton, Blaze, and Enderman.

Roaming Bosses: 6 world-roaming bosses in RoamingBossManager.java (57 KB) — The Watcher (Deep Dark, 800 HP), Hellfire Drake (Nether, 600 HP), Frostbound Colossus (Snow, 700 HP), Jungle Predator (Jungle, 500 HP), End Wraith (End, 900 HP, with shadow clones at 50% HP), Abyssal Leviathan (Abyss, 1200 HP). All have custom AI, special attacks, death loot, and despawn timers.

Events: EventManager.java orchestrates 6 events — NetherStormEvent.java (11 KB), PiglinUprisingEvent.java (9 KB), VoidCollapseEvent.java (9 KB), PillagerWarPartyEvent.java (15 KB), PillagerSiegeEvent.java (19 KB), EndRiftEvent.java (30 KB). Periodic scheduler checks dimension/biome/time conditions.

Armor Sets: 13 sets in LegendaryArmorSet.java (16 KB) — 6 craftable (Reinforced Leather, Copper, Chainmail Reinforced, Amethyst, Bone, Sculk) and 7 legendary drops (Shadow Stalker, Blood Moon, Nature's Embrace, Frost Warden, Void Walker, Dragon Knight, Abyssal Plate). Each has 4 pieces with custom_model_data, passives per slot, and set bonuses. LegendaryArmorManager.java (15 KB) creates pieces; LegendaryArmorListener.java (39 KB) applies passive effects.

Quests: QuestManager.java (28 KB) — 6 quest lines (Hunter, Miner, Explorer, Nether Trials, Dragon Slayer, Abyssal Descent), each with 3 stages, NPC villagers, progress tracking via PersistentDataContainer, and rewards (weapons, Heart Crystals, Soul Fragments, Phoenix Feathers). QuestProgressListener.java (3 KB) hooks kill/mine/move events.

Abyss Dimension: AbyssDimensionManager.java (12 KB) — custom world world_abyss using AbyssChunkGenerator.java (7 KB), purpur portal frame activation with Abyssal Key (Echo Shard), teleportation with VFX. AbyssDragonBoss.java (14 KB) is the Abyss Dragon boss.

Infinity Gauntlet: InfinityGauntletManager.java (23 KB) — Thanos boss with 800 HP, 2-phase AI (ground slam, energy beam), guaranteed Thanos Glove drop. Gauntlet snap kills 50% of loaded hostile mobs, 5-min cooldown. InfinityStoneManager.java (8 KB) handles 6 stones (fragments + finished).

Other Systems: Enchantments (64 custom, EnchantmentEffectListener 69 KB), Blessings, Guilds, Blood Moon, King Mobs, Mob Difficulty, Boss Mastery Titles, Power-ups (Heart Crystal, Soul Fragment, Titan's Resolve, Phoenix Feather, KeepInventorer, Vitality Shard, Berserker Mark), NPC Wizard, Creative Menu, Guide Book (PT-BR), 11 battle weapon types, Recipe Manager (35 KB), Pale Garden Fog.

What's Actually Missing / Next Steps
Now that the code audit is done, here's what should be the real priority:

Priority 1 — Verify weapon textures work in-game. The server resource pack (7.84 MB, SHA-1 1b115ef012cdcd34491f4482b4e220303bf2c3db) is deployed and TPS is 20.0. You need to join the server, accept the pack, and confirm that /jglims legendary <id> gives weapons with correct custom textures. Do the custom_model_data values (30001–30063 for weapons, 40001–40002 for gauntlet items, 30101–30264 for armor) actually render the textures from the merged resource pack? Which packs provided the weapon textures — Blades of Majestica (36 items) and nongko's Fantasy Weapons (20 items) cover 56 items, but you have 63 weapons + 4 abyssal + gauntlet items + 52 armor pieces. That's 119+ items needing textures. Are they all mapped?

Priority 2 — Custom mini-boss textures. You mentioned wanting custom textures for mini-bosses. Currently bosses get their visual identity from equipment (vanilla armor/weapons) + name + glow + particles, not custom entity textures. To add unique visuals, you'd equip them with custom_model_data items (like a custom helmet or weapon skin). This requires creating the textures, adding them to the resource pack, and updating StructureBossManager to equip items with those CMD values.

Priority 3 — Features you mentioned wanting to add (from conversation history):

Thanos Gauntlet updates: more epic lighting, revert to glove state after snap, apply Weakness V + Slowness II to user, move Thanos Temple to End Cities
New PvP legendary items: Hollow Purple, Wand of Wands, King's Haki
Aether dimension (mid-game)
Lunar dimension (end-game, post-Dragon)
More structures, mini-bosses, and loot across all biomes
README status section
What should we tackle first? I'd recommend joining the server right now, running /jglims legendary true_excalibur and a few other weapons, and telling me which ones render their textures correctly and which show as plain diamond swords. That'll tell us exactly what texture work is needed before we move forward
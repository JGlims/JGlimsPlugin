JGLIMSPLUGIN — DEFINITIVE PROJECT SUMMARY v2.0
Date compiled: 2026-03-05 Target version: 1.3.0 (current) → 2.0.0 (after all planned phases) Author: JGlims Players: JotaGlims (Java), Gustafare5693 (Bedrock) Repository: https://github.com/JGlims/JGlimsPlugin Latest commit: caf6662 (2026-03-04) API: Paper 1.21.1, Java 21, Gradle 8.x, Kyori Adventure (native, no shading) Server: Docker container mc-crossplay on Oracle Cloud, IP 144.22.198.184, Java port 25565, Bedrock port 19132 (GeyserMC + Floodgate)

SECTION A — INSTRUCTIONS FOR THE NEXT CHAT SESSION
Before doing anything, the next assistant must:

Fetch the latest state of every file it plans to edit from https://github.com/JGlims/JGlimsPlugin. Never assume file contents — always crawler the raw GitHub URL first.
Supply complete file replacements with the exact path (e.g., src/main/java/com/jglims/plugin/weapons/LegendaryWeaponManager.java). Never send partial diffs or snippets.
Include the full build-and-deploy sequence after every code change:
# PowerShell (local)
cd C:\Users\jgmel\Documents\projects\JGlimsPlugin\JGlimsPlugin
.\gradlew.bat clean jar

# Copy to server
scp build\libs\JGlimsPlugin-<VERSION>.jar MinecraftServer:~/

# SSH (server)
docker exec mc-crossplay rm -f /data/plugins/JGlimsPlugin*.jar
docker cp ~/JGlimsPlugin-<VERSION>.jar mc-crossplay:/data/plugins/JGlimsPlugin.jar
docker restart mc-crossplay

# Verify
docker logs --since 2m mc-crossplay 2>&1 | grep -i jglims
docker logs --since 2m mc-crossplay 2>&1 | grep -i "exception\|error\|WARN" | head -20
docker exec mc-crossplay rcon-cli tps
docker stats mc-crossplay --no-stream
Always delete the old JAR before copying the new one (the rm -f step above) to avoid the "ambiguous plugin name" warning.
Use Adventure API everywhere — Component.text(), NamedTextColor, TextDecoration. No ChatColor, no § codes.
All tunable values go through ConfigManager — never hard-code numbers in listeners.
Every new Listener class must be registered in JGlimsPlugin.onEnable() with pm.registerEvents(...).
All item data uses PersistentDataContainer with NamespacedKey under the jglimsplugin namespace.
SECTION B — CURRENT STATE OF THE PROJECT (v1.3.0)
The plugin is fully functional with the following systems operational as of the latest commit:

Architecture: 30 Java source files plus 2 resources (plugin.yml, config.yml), following a manager-listener pattern. JGlimsPlugin.java is the central orchestrator. Data persistence uses PDC for items and player data, and guilds.yml for guild state. 17 event listeners are registered. The plugin loads in approximately 1117 ms, TPS is a solid 20.0, CPU at 2.5% idle, RAM at 10.2 GB / 23.9 GB, with no errors in recent logs.

64 Custom Enchantments spanning 12 categories (Sword ×6, Axe ×6, Pickaxe ×6, Shovel ×3, Hoe/Sickle ×6, Bow ×5, Crossbow ×2, Trident ×6, Armor ×9, Elytra ×4, Mace ×5, Spear ×4, Universal ×2). All stored as integers in PDC. Bidirectional conflict map enforced at anvil-apply time.

10 Weapon Classes (Sword, Axe, Pickaxe, Shovel, Bow, Mace, Trident, Spear, Sickle, plus Super Tool tiers). Each has its own Manager class with PDC tags, display name, lore, attribute modifiers, and durability. Super Tools come in Iron (+1.0 dmg), Diamond (+2.0 dmg), and Netherite (+2.0 dmg base, +2% per custom enchantment) tiers. Enchantments are preserved during tier upgrades via copyAllEnchantments().

20 Weapon Abilities in WeaponAbilityListener.java (81 KB), 2 per weapon class, triggered by sneak + right-click or on-hit, with cooldowns, action-bar messages, particles, and sounds. Ender Dragon takes 30% reduced ability damage. Netherite super-tools get +2% ability damage per custom enchantment.

3 Blessings (C's Bless: +1 heart per use ×10, Ami's Bless: +2% damage per use ×10, La's Bless: +2% defense per use ×10), reapplied on join/respawn.

Mob Difficulty System with distance-based scaling (7 brackets from 350 to 5000+ blocks), biome multipliers (Overworld: Pale Garden 2.0×, Deep Dark 2.5×, Swamp 1.4×; Nether: Wastes 1.7× through Basalt Deltas 2.3×), dimension multiplier (The End: 2.5× health / 2.0× damage), and speed scaling. Boss Enhancer handles Dragon (3.5× health / 3.0× damage), Elder Guardian (2.5× / 1.8×), Wither and Warden (1.0×). Creeper spawns reduced by 50%.

King Mobs: 1 per 100 spawns of each EntityType, gold name, glowing, 10× health, 3× damage, drops 3–9 diamonds.

Blood Moon: 15% chance per night, Darkness effect, red particles, all overworld monsters (except Creepers) get 1.5× health / 1.3× damage, double drops. Every 10th Blood Moon spawns a Blood Moon King boss (diamond armor, netherite sword, 20× health, 5× damage) dropping 5–15 diamonds, 1 netherite ingot, 500 XP.

Guilds: YAML-based, max 10 members, friendly-fire disabled between members, full command set (/guild create/invite/join/leave/kick/disband/info/list).

Utilities: Inventory sort (shift-click empty slot), enchantment transfer (tool → book), loot booster (guaranteed enchanted book in dungeon chests, 5%/15% mob book drops), drop rate booster (trident 35% from Drowned, guardian shards, ghast tears, breeze charges), villager trades (50% price reduction, unlimited), Pale Garden fog (Darkness effect), soulbound (1 per inventory, lostsoul extraction), Best Buddies (wolf with 95% DR, pacifist, Regen II), weapon mastery (10 classes, up to +10% damage at 1000 kills).

Commands: /jglims (version, reload, stats, enchants, sort, mastery, help, battlemode), /guild (create, invite, join, leave, kick, disband, info, list).

SECTION C — KNOWN BUGS (to fix in Phase 5)
Bug 1 — Sickle shows as "Battle Hoe": The display name in SickleManager.java is set to "Battle Hoe" instead of "Sickle." Fix: Change the display name string and lore in SickleManager, and ensure the recipe discovery message also says "Sickle." Additionally, the sickle currently deals only 4 damage — this should be increased to match other battle weapons (7–8 for iron tier).

Bug 2 — Weapon abilities damage only mobs, not players: All 20 weapon abilities in WeaponAbilityListener.java likely use a mob-only damage method or filter out Player entities. Fix: Change the damage call to entity.damage(amount, attackerPlayer) and add a guild-friendly-fire check — if both attacker and target are in the same guild, cancel the PvP damage; otherwise, allow it. The bow ability already damages players, so use its implementation as reference.

Bug 3 — Guild default membership ambiguity: It's unclear whether new players are automatically placed in a default guild or start with no guild. Clarify: Players start with NO guild. The /guild create command is the entry point. The /guild info command should show "You are not in a guild" for unguilded players rather than an error.

Bug 4 — Enchantment glint on custom weapons: Battle, Super, and Legendary weapons should NOT show the enchantment glint. Fix: Add meta.setEnchantmentGlintOverride(false) in all weapon manager createItem() methods.

Bug 5 — Duplicate JAR warning: When deploying, if the old JAR isn't deleted first, Paper warns about an ambiguous plugin. Fix: Always run docker exec mc-crossplay rm -f /data/plugins/JGlimsPlugin*.jar before docker cp.

SECTION D — PHASE ROADMAP
Phase 5: Bug Fixes (Current priority)
Fix all 5 bugs listed in Section C.

Phase 6: Performance Profiling
Monitor TPS under load, ensure memory stays under 6 GB allocated to the JVM, optimize hotspots in EnchantmentEffectListener.java (69 KB) and WeaponAbilityListener.java (81 KB). Consider splitting the two largest files into per-category classes.

Phase 7: Resource Packs & Custom Textures (MAJOR)
Full details in Section F below.

Phase 8: Legendary Weapons (MAJOR)
Full details in Section G below.

Phase 9: Plant Totems & Boss Totems
Full details in Section H below.

Phase 10: Welcome Book (Portuguese Brazil)
Full details in Section I below.

Phase 11: Creative Menu / Catalog
Full details in Section J below.

Phase 12: Mob Difficulty Rebalance
Increase baseline-health-multiplier and baseline-damage-multiplier in config.yml so that early-game mobs are noticeably tougher than vanilla. Re-test with legendary weapons to prevent the game from becoming too easy. If legendary weapons trivialize content, add a further difficulty curve at distances > 1000 blocks.

Phase 13: Events & Minibosses
Add new world events beyond Blood Moon. Examples: Wither Storm (rare Nether event), Ocean Siege (Guardian-led attack near ocean monuments), End Rift (End-themed mobs spawn in Overworld). Each event spawns a miniboss that can drop legendary weapons. The Blood Moon King boss should also have a chance to drop a random legendary weapon.

Phase 14: Survival Mode Switch
Final production deployment — switch from creative testing to survival mode. Lock down admin commands, set proper permissions, remove debug logging.

SECTION E — PROBLEMS SOLVED ACROSS ALL SESSIONS
The project has resolved the following issues over its development history:

RecipeManager super-recipe bug (duplicate NamespacedKeys silently failing), BestBuddiesListener never being registered in onEnable(), King Mob spawn rate too high (set to 1 per 100), inconsistent weapon lore across 10 managers (standardized to Adventure API with gold/gray/green color scheme), weapon ability damage too low for scaled mobs (buffed all 20 abilities), SpearManager using legacy ChatColor (migrated to Components), MobDifficultyManager hard-coding distance multipliers (now reads from ConfigManager), BiomeMultipliers hard-coding biome values (now initialized via initWithConfig()), 12 new enchantments added to bring total from 52 to 64, legacy § color codes in CustomEnchantManager (migrated to Adventure API), and the duplicate JAR deployment warning (resolved by deleting old file first).

Key lessons: Always check PDC key uniqueness. Register every listener. Config-driven values over hard-coded ones. Adventure API is mandatory on Paper (native Kyori support). GitHub base64 blobs strip § characters. Super-tool upgrades must preserve enchantments. Spear detection needs both is_battle_spear and super_spear_tier PDC keys.

SECTION F — TEXTURE & RESOURCE PACK INTEGRATION PLAN (Phase 7)
F.1 — Architecture Overview
The server runs Paper (Java) with GeyserMC + Floodgate for Bedrock crossplay. Resource pack delivery works differently for each platform:

Java players receive a server resource pack via server.properties → resource-pack=<URL>. Since the server is 1.21.1 (not 1.21.4+), custom weapon textures must use CustomModelData (not the newer item_model component). The plugin sets meta.setCustomModelData(integer) on each custom weapon, and the resource pack contains model overrides keyed to those integers. CIT (Custom Item Textures) packs like Fantasy 3D Weapons and Blades of Majestica normally work via OptiFine/CIT Resewn by matching item names — however, for this server the simplest long-term approach is to use CustomModelData integers set by the plugin, not name-based CIT matching. This ensures textures work reliably regardless of player-applied renames and doesn't require OptiFine or CIT Resewn on the client (just the server resource pack). The plugin already controls item creation and can set any CustomModelData value.

Bedrock players cannot use Java resource packs. Geyser supports sending Bedrock resource packs from the packs folder and custom item mappings from the custom_mappings folder. The workflow is: (1) Build the Java resource pack with all custom models, (2) Use Rainbow (GeyserMC's Fabric-based converter mod) to join the server from a Fabric client, map each custom item via /rainbow map or /rainbow mapinventory, then /rainbow finish to generate a Bedrock .mcpack and geyser_mappings.json, (3) Place those in the Geyser packs/ and custom_mappings/ folders respectively, (4) Restart. Rainbow is the best tool for this because it directly creates Geyser-v2-format mappings and handles the CustomModelData → Bedrock custom item conversion.

For simple vanilla texture replacements (armor retextures like Spryzeen's Knight Armor, block/entity texture packs), use Thunder (GeyserMC's Java-to-Bedrock resource pack converter) to convert the Java pack to a Bedrock .mcpack, then place it in Geyser's packs/ folder.

F.2 — Fresh Animations & Entity Packs for Bedrock
Fresh Animations on Java uses OptiFine CEM (Custom Entity Models) or the Fabric-based EMF (Entity Model Features) + ETF (Entity Texture Features) mods. These are client-side Java mods and cannot be enforced server-side.

For the Bedrock player (Gustafare5693), Fresh Animations has a native Bedrock Edition port available on MCPEDL (https://mcpedl.com/fresh-animations-bedrock/). This can be delivered as a .mcpack placed in Geyser's packs/ folder so it auto-downloads for Bedrock clients.

However, Geyser cannot convert Java CEM/EMF entity models to Bedrock. The solution is:

For entity animation packs (Fresh Animations, AL's mob revamps + FA variants, Drodi's Blazes × Fresh Animations), use the Bedrock-native versions of each pack where available. Many of AL's packs have Bedrock versions or the community has ported them. Place each as a .mcpack in Geyser's packs/ folder.

For the Java player (JotaGlims), the server resource pack handles textures only. Fresh Animations and entity model packs are installed client-side (OptiFine or EMF+ETF on Fabric/Quilt). The server cannot enforce these.

Recommended client-side setup for JotaGlims (Java): Install Fabric + EMF + ETF, then add:

Fresh Animations
AL's Zombie Revamped + FA, AL's Skeleton Revamped + FA, AL's Creeper Revamped + FA, AL's Enderman Revamped + FA, AL's Boss Rush + FA, AL's Piglin Revamped + FA, AL's Dungeons Boss Bars, AL's Mob Weapons
Drodi's Blazes × Fresh Animations
Recommended setup for Gustafare5693 (Bedrock): Auto-download from Geyser packs/ folder:

Fresh Animations Bedrock (from MCPEDL)
Any AL revamp packs that have Bedrock ports
The server's custom weapon Bedrock resource pack (generated by Rainbow)
F.3 — Weapon Texture Assignments
Normal weapons (vanilla crafting): Keep vanilla textures. Enchantment Outlines resource pack applies to these only (enchanted items get colored outlines based on enchantment type). The plugin ensures custom weapons have meta.setEnchantmentGlintOverride(false) so they never show the glint/outline.

Battle weapons (tier 1 custom) → Fantasy 3D Weapons CIT textures. Assignments:

Battle Weapon	Fantasy 3D Texture Name	CustomModelData
Battle Sickle	Iron Hay Sickle	10001
Battle Sword	Uchigatana	10002
Battle Axe	Iron Battle Axe	10003
Battle Spear	Iron Halberd	10004
Battle Mace	Iron Mace	10005
Battle Shovel	Iron Sai	10006
Super weapons (tier 2 custom) → Blades of Majestica textures. Assignments:

Super Weapon	Majestica Texture Name	CustomModelData
Super Spear	Heavenly Partisan	20001
Super Sword	Thousand Demon Daggers (Demon's Blood Blade)	20002
Super Axe	Hearthflame (Crimson Cleaver)	20003
Super Sickle	Jade Halberd (Crystal Frostscythe)	20004
Super Mace	Frost Axe (Black Iron Clobberer)	20005
Super Shovel	Sculk Scythe (Azure Scythe)	20006
Legendary weapons (tier 3, non-craftable) → Mix of both packs. See Section G for full assignments.

F.4 — Other Resource Packs
Pack	Purpose	Delivery
Spryzeen's Knight Armor	Medieval armor retextures (all tiers)	Server RP (Java) + Thunder-converted .mcpack (Bedrock)
Enchantment Outlines	Colored glint per enchantment type — normal weapons ONLY	Server RP (Java) + Thunder-converted (Bedrock)
Recolourful Containers GUI + HUD	Custom container/inventory GUI	Server RP (Java) + Thunder-converted (Bedrock)
Recolourful Containers Hardcore Hearts	Custom heart textures	Server RP (Java) + Thunder-converted (Bedrock)
Story Mode Clouds	Stylized clouds	Server RP (Java) + Thunder-converted (Bedrock)
Fresh Food (with blessings)	Custom food textures	Server RP (Java) + Thunder-converted (Bedrock)
Fantasy 3D Weapons CIT	3D weapon models for battle tier	Server RP (Java), models extracted and included
Blades of Majestica	3D weapon models for super/legendary tiers	Server RP (Java), models extracted and included
F.5 — CIT Implementation Strategy (Long-Term Best Solution)
CIT (Custom Item Textures) packs traditionally work by matching item names via OptiFine or CIT Resewn. This approach is fragile because players can rename items in an anvil and break the match. The recommended long-term solution for this project is:

Use CustomModelData integers set by the plugin in each weapon manager's createItem() method. The server resource pack uses model overrides in each item's JSON model file to select the 3D model based on the CustomModelData value. This approach is name-independent, works without OptiFine or CIT Resewn, works with any vanilla client that accepts the server resource pack, and is cleanly convertible to Bedrock via Rainbow.

The plugin already controls item creation through dedicated Manager classes, so adding meta.setCustomModelData(10001) (etc.) to each weapon type is trivial. The 3D model .json and texture .png files from Fantasy 3D Weapons and Blades of Majestica are extracted from their original packs and reorganized into the server resource pack under assets/jglimsplugin/models/item/ and assets/jglimsplugin/textures/item/, with vanilla item model overrides in assets/minecraft/models/item/diamond_sword.json (etc.) pointing to the custom models when the CustomModelData matches.

SECTION G — LEGENDARY WEAPON SYSTEM (Phase 8)
G.1 — Core Concept
Legendary weapons are the pinnacle weapon tier. They are non-craftable, non-enchantable (no custom or vanilla enchantments can be applied), and have built-in effects that replace enchantments. Each has a unique 3D texture, a custom display name with a dark purple/gold color scheme, a lore description, fixed damage, a cooldown, a right-click ability (sneak + right-click, same pattern as other weapons), and a new hold ability (activated by holding right-click for 2+ seconds on Java, or via a custom Bedrock UI button mapped through Geyser forms / GeyserExtras).

PDC tags: is_legendary_weapon (PersistentDataType.BYTE, 1), legendary_id (PersistentDataType.STRING, unique ID like "oceans_rage"), legendary_cooldown (PersistentDataType.LONG, timestamp of last ability use).

Legendary weapons have meta.setEnchantmentGlintOverride(false) and meta.setUnbreakable(true). They display custom lore showing damage, ability description, cooldown timer, and a "LEGENDARY" tag in dark purple bold.

G.2 — Full Legendary Weapon Table
The design uses at least 40% Majestica textures and 40% Fantasy 3D textures, with creative variants for throwing, spear, and hammer types. Excluded textures: Cyber Mantis Blade, Cyber Katana, Cyber Sword (too modern/sci-fi for the medieval fantasy theme).

#	Weapon Name	Texture Source	Texture Name	Base DMG	Right-Click Ability	Hold Ability	Drop Source
1	Ocean's Rage	Majestica	Ocean Rage	14	Tidal Crash — 6-block AoE water blast, 15 dmg, 3s knockback, 8s CD	Riptide Surge — Launch forward in water, trail of damage, 15s CD	Elder Guardian
2	Aquatic Sacred Blade	Majestica	Aquantic Sacred Blade	13	Aqua Heal — Heal 6 hearts + Conduit Power 30s, 20s CD	Depth Pressure — 10-block radius Slowness III + Mining Fatigue to enemies, 25s CD	Elder Guardian
3	True Excalibur	Majestica	Phoenixe's Grace	16	Holy Smite — Lightning + 20 dmg AoE 5 blocks, 10s CD	Divine Shield — 5s invulnerability + Strength II, 45s CD	Warden
4	Requiem of the Ninth Abyss	Majestica	Soul Devourer	15	Soul Devour — Drain 8 hearts from target, heal self, 12s CD	Abyss Gate — Summon wither skeletons (3) for 15s, 60s CD	Warden
5	Royal Chakram	Majestica	Royal Chakram	12	Chakram Throw — Throwable projectile, bounces between 4 targets, 8 dmg each, returns, 6s CD	Spinning Shield — 3s of projectile deflection + 50% melee DR, 20s CD	Warden
6	Berserker's Greataxe	Fantasy 3D	Berserker's Greataxe	17	Berserker Slam — 8-block AoE ground pound, 18 dmg, 10s CD	Blood Rage — +50% damage, +30% speed, -30% defense for 10s, 30s CD	Warden
7	Acidic Cleaver	Fantasy 3D	Treacherous Cleaver	14	Acid Splash — 5-block cone of poison, Poison III 8s, 10s CD	Corrosive Aura — Enemies within 6 blocks lose 1 heart/s for 8s, 25s CD	Warden
8	Black Iron Greatsword	Fantasy 3D	Black Iron Greatsword	15	Dark Slash — 10-block line of void damage, 16 dmg, 8s CD	Iron Fortress — Absorption IV (8 hearts) + Resistance II for 8s, 30s CD	Warden
9	Muramasa	Majestica	Muramasa	13	Crimson Flash — Dash 8 blocks, damage all in path 12 dmg, 6s CD	Bloodlust — Each kill within 15s adds +2 damage (stacks 5×), resets on expire, 20s CD	Wither
10	Phoenix's Grace	Fantasy 3D	Gilded Phoenix Greataxe	15	Phoenix Strike — Fire AoE 6 blocks, 14 dmg + Fire 6s, 10s CD	Rebirth Flame — On death within 60s of activation, revive with 50% HP (single use per activation), 120s CD	Wither
11	Soul Collector	Majestica	Soul Collector	14	Soul Harvest — Kill within 5s stores soul, next attack deals +10 bonus dmg, 8s CD	Spirit Army — Release stored souls as phantom projectiles (up to 5), each deals 6 dmg, 30s CD	Wither
12	Amethyst Shuriken	Majestica	Amethyst Shuriken	11	Shuriken Barrage — Throw 5 shurikens in a fan, 7 dmg each, 7s CD	Shadow Step — Teleport behind target + guaranteed crit (2× damage), 15s CD	Wither
13	Valhakyra	Majestica	Valhakyra	15	Valkyrie Dive — Leap 10 blocks up, slam down for 20 dmg AoE, 12s CD	Wings of Valor — 8s of slow-fall gliding + Strength I, 25s CD	End Cities (chest)
14	Windreaper	Fantasy 3D	Windreaper	13	Gale Slash — 8-block wind cone, 12 dmg + massive knockback, 8s CD	Cyclone — 4s tornado around player, pulls enemies in + 4 dmg/s, 20s CD	End Cities (chest)
15	Phantomguard	Fantasy 3D	Phantomguard Greatsword	14	Spectral Cleave — Pass through blocks, hit all entities in 10-block line, 14 dmg, 10s CD	Phase Shift — 3s of intangibility (immune to damage, can walk through blocks), 35s CD	Ender Dragon (death chest)
16	Moonlight	Fantasy 3D	Moonlight	13	Lunar Beam — 15-block ranged beam, 16 dmg, 10s CD	Eclipse — 6s: all enemies in 15 blocks get Blindness + Weakness II, 30s CD	Ender Dragon (death chest)
17	Zenith	Fantasy 3D	Zenith	18	Final Judgment — 360° AoE 8 blocks, 22 dmg, 15s CD	Ascension — 10s of Flight + all attacks deal +50% damage, 60s CD	Ender Dragon (death chest)
18	Solstice	Fantasy 3D	Solstice	14	Solar Flare — 10-block radius fire explosion, 15 dmg + blindness 3s, 10s CD	Daybreak — Remove all negative effects + Regeneration IV for 6s, 25s CD	Stronghold (chest)
19	Grand Claymore	Fantasy 3D	Grand Claymore	16	Titan Swing — 180° arc, 10-block range, 18 dmg + 6-block knockback, 10s CD	Colossus Stance — 6s: immune to knockback, +3 attack range, +40% damage, 30s CD	Bastion Remnant (chest)
20	Calamity Blade	Majestica	Calamity Blade	15	Cataclysm — 6-block AoE, summons falling blocks + 14 dmg + slowness, 12s CD	Doomsday — 8s: all damage you deal is doubled, but you take 1 heart/s, 35s CD	Nether Fortress (chest)
21	Dragon Sword	Fantasy 3D	Dragon Sword	14	Dragon Breath — 8-block fire cone, 12 dmg + Dragon's Breath cloud 4s, 10s CD	Draconic Roar — 8-block radius: enemies get Fear (forced sprint away) + Weakness II 5s, 25s CD	Ender Dragon (death chest)
22	Talonbrand	Fantasy 3D	Talonbrand	13	Talon Strike — Triple-hit combo in 1s, 8 dmg each (24 total), 8s CD	Predator's Mark — Mark target: they take +30% damage from all sources for 10s, 20s CD	Dungeon (chest)
23	Emerald Greatcleaver	Fantasy 3D	Emerald Greatcleaver	16	Emerald Storm — 6-block AoE gem shards, 14 dmg + Poison II 4s, 10s CD	Gem Barrier — Absorb next 3 hits completely (like totems), lasts 15s, 40s CD	Temple (chest)
24	Demon's Blood Blade	Majestica	Demon's Blood Blade	15	Blood Rite — Sacrifice 3 hearts, deal 25 dmg to target, 8s CD	Demonic Form — 10s: +60% damage, fire trail behind you, but -50% defense, 35s CD	Blood Moon Boss
Texture distribution: 10 Majestica (42%), 14 Fantasy 3D (58%) — both exceed the 40% minimum.

G.3 — Legendary Weapon Drop Locations
Generated Chests (high chance): When a chest generates (via LootBoosterListener or a new LegendaryLootListener), roll a chance based on structure type. If successful, one random legendary weapon is placed in the chest. Chances:

Structure	Chance	Pool
End City	25%	Valhakyra, Windreaper, Phantomguard, Moonlight, Zenith, Dragon Sword
Stronghold	15%	Solstice, True Excalibur
Nether Fortress	12%	Calamity Blade, Muramasa, Phoenix's Grace
Bastion Remnant	12%	Grand Claymore, Berserker's Greataxe
Ocean Monument (hidden chest / Guardian drop)	10%	Ocean's Rage, Aquatic Sacred Blade
Dungeon (Monster Spawner room)	8%	Talonbrand, Acidic Cleaver, Amethyst Shuriken
Temple (Desert/Jungle)	8%	Emerald Greatcleaver, Soul Collector
Boss-Specific Drops (guaranteed-from-pool on kill):

Boss	Drops (1 random from pool)
Elder Guardian	Ocean's Rage, Aquatic Sacred Blade
Warden	True Excalibur, Requiem of the Ninth Abyss, Royal Chakram, Berserker's Greataxe, Acidic Cleaver, Black Iron Greatsword
Wither	Muramasa, Phoenix's Grace, Soul Collector, Amethyst Shuriken
Ender Dragon	ALL legendary weapons (1 random placed in a chest that spawns at the death location)
Blood Moon King Boss	Any random legendary weapon (10% chance per kill)
G.4 — Hold Ability (New Ability Type)
This is a new interaction pattern distinct from the existing sneak + right-click:

Java: The player holds right-click for 2+ seconds while holding a legendary weapon. The plugin detects PlayerInteractEvent (RIGHT_CLICK_AIR or RIGHT_CLICK_BLOCK) and starts a timer in a HashMap<UUID, Long>. On PlayerInteractEvent release or after 2s of holding (checked via a repeating task), the hold ability activates. Action-bar shows a charging indicator: [■■■□□] Charging... → [■■■■■] ACTIVATED!

Bedrock: Bedrock doesn't have the same interact-hold detection. Two approaches: (a) Use GeyserMC's form API to add a "Hold Ability" button via /jglims holdability command, or (b) detect the Bedrock player using Floodgate API (FloodgateApi.getInstance().isFloodgatePlayer(uuid)) and map the hold ability to sneak + sneak + right-click (double-sneak then right-click) as an alternative trigger. Option (b) is simpler and doesn't require UI forms.

G.5 — No Enchantments on Legendary Weapons
The AnvilRecipeListener must check for is_legendary_weapon PDC tag and reject any enchantment application. The EnchantmentEffectListener must also check and skip legendary weapons. Legendary weapons have their effects built into LegendaryAbilityListener.java (new file).

SECTION H — PLANT TOTEMS & BOSS TOTEMS (Phase 9)
H.1 — Plant Totems (Elemental Resistance)
New item type: Plant Totems. These are held in the off-hand and grant passive damage resistance to specific damage types. They are not craftable — found only in generated chests (same structures as legendary weapons, but independent rolls). Each totem provides a stackable resistance buff (up to 30% per damage type) that is independent of armor, blessings, and enchantments — it's a flat damage reduction applied as a separate multiplier in EnchantmentEffectListener or a new TotemListener.

Totem	Resistance Type	Per Totem	Max Stack	Found In
Fern Totem	Fire damage	10%	3 (30%)	Nether Fortress, Bastion
Moss Totem	Poison/Wither damage	10%	3 (30%)	Jungle Temple, Swamp huts
Cactus Totem	Projectile damage	10%	3 (30%)	Desert Temple, Pillager Outpost
Vine Totem	Fall damage	10%	3 (30%)	Jungle Temple, Mineshaft
Lily Totem	Drowning/Freeze damage	10%	3 (30%)	Ocean Monument, Shipwreck
Mushroom Totem	Explosion damage	10%	3 (30%)	Dungeon, Woodland Mansion
Implementation: Custom item with is_plant_totem and totem_type PDC tags. A PlantTotemListener checks for totems in off-hand on EntityDamageEvent, applies the reduction before other calculations. Stacking is per damage type (e.g., 3 Fern Totems = 30% fire reduction, but you can only hold one in off-hand at a time — stacking means the effect increases as you collect more, stored in player PDC as totem_fern_level, etc.). Actually — since only one off-hand slot exists, stacking works differently: each totem consumed (right-click to absorb, like blessings) permanently adds +10% resistance of that type to the player's PDC, up to 3 absorptions (30%). The totem item is consumed on use.

H.2 — Boss Totems (Unique Passive Abilities)
Dropped exclusively by bosses, these grant permanent passive abilities when consumed (right-click). They use ore-based textures via CustomModelData (e.g., a totem with an emerald/diamond/netherite ore texture theme).

Boss Totem	Drop Source	Passive Effect	Texture Theme
Guardian's Blessing	Elder Guardian	+50% swim speed + Respiration III permanent	Prismarine Ore
Wither's Immunity	Wither	Complete Wither effect immunity	Coal/Obsidian Ore
Warden's Silence	Warden	Silent walking (Sculk sensors don't detect you)	Deepslate Ore
Dragon's Gaze	Ender Dragon	Permanent Night Vision + Endermen don't aggro when looked at	End Stone Ore
Implementation: PDC tags boss_totem_guardian, boss_totem_wither, boss_totem_warden, boss_totem_dragon (BYTE, 0 or 1) on the player. Effects reapplied on join/respawn via BossTotemListener. Custom items with is_boss_totem + boss_totem_type PDC tags.

SECTION I — WELCOME BOOK (Phase 10)
Every player receives a Written Book (in Portuguese — Brazil) on first join, stored in their inventory. If lost, they can use /jglims livro to get a replacement. The book is created programmatically using BookMeta with Adventure Components.

Book Contents (Portuguese Brazil):

Página 1: Bem-vindo ao servidor JGlims! Introdução geral, IP, jogadores.
Página 2: Sistema de Armas — Armas normais, Battle, Super, Lendárias. Tabela de dano.
Página 3: Receitas de Crafting — Como criar cada arma battle (referência ao /jglims recipes).
Página 4: Super Ferramentas — Como fazer upgrade de tier (Ferro → Diamante → Netherita).
Página 5: Armas Lendárias — Onde encontrar, lista de bosses e drops, habilidades especiais.
Página 6: Encantamentos Customizados — Lista de todos os 64 encantamentos, como aplicar na bigorna.
Página 7: Encantamentos (continuação) — Conflitos, níveis máximos.
Página 8: Bênçãos — C's Bless, Ami's Bless, La's Bless. Como usar, efeitos.
Página 9: Totems — Plant Totems (resistência elemental) e Boss Totems (habilidades passivas).
Página 10: Guildas — Como criar, convidar, sair. Friendly fire desativado.
Página 11: Eventos — Lua de Sangue, minibosses, drops especiais.
Página 12: Dificuldade dos Mobs — Escala por distância, bioma, dimensão. King Mobs.
Página 13: Utilidades — Ordenar inventário, transferir encantamentos, taxas de drop melhoradas.
Página 14: Comandos — /jglims, /guild, todos os subcomandos.
Página 15: Créditos e links.
SECTION J — CREATIVE MENU / CATALOG (Phase 11)
Add a /jglims catalog command that opens a virtual chest GUI (using Paper's Inventory API) showing all custom items organized by category. Players can browse but not take items (unless in creative mode or op). Categories:

Battle Weapons (page 1): All 10 battle weapon types
Super Weapons (page 2): All super weapon tiers
Legendary Weapons (page 3–4): All 24 legendary weapons
Custom Enchantment Books (page 5–6): All 64 enchantment books at max level
Blessings (page 7): All 3 blessings
Totems (page 8): All plant totems and boss totems
Navigation: Glass panes as borders, arrows for page navigation, category selector on the bottom row.
Implementation: CatalogListener.java handles InventoryClickEvent to prevent item theft (cancel all clicks unless player is OP + creative). Items are display-only copies with a "CATALOG ITEM" lore tag.

SECTION K — MOB DIFFICULTY REBALANCE (Phase 12)
After legendary weapons are implemented, re-test the difficulty curve. If the game becomes too easy:

Increase baseline-health-multiplier to 1.5 and baseline-damage-multiplier to 1.3 in config.yml.
Add a new distance bracket at 7500+ blocks (5.0× health, 4.0× damage).
Add a "Legendary Mob" tier above King Mobs: 1 per 500 spawns, 20× health, 5× damage, guaranteed diamond + chance of plant totem drop.
Bosses scale with number of legendary weapons the nearest player has equipped (checked via PDC scan of inventory): +10% boss HP per legendary weapon found.
SECTION L — REPOSITORY STRUCTURE (after all phases)
JGlimsPlugin/
  .gitignore
  README.md
  build.gradle
  settings.gradle
  gradlew / gradlew.bat
  gradle/wrapper/
  src/main/
    java/com/jglims/plugin/
      JGlimsPlugin.java                    # Main plugin, lifecycle, commands
      config/
        ConfigManager.java                 # All config getters
      enchantments/
        EnchantmentType.java               # Enum: 64 enchantments
        CustomEnchantManager.java          # PDC read/write, conflicts
        AnvilRecipeListener.java           # Anvil crafting (blocks legendary)
        EnchantmentEffectListener.java     # All enchantment effects
        SoulboundListener.java             # Death-save, lostsoul
      blessings/
        BlessingManager.java
        BlessingListener.java
      guilds/
        GuildManager.java
        GuildListener.java
      mobs/
        MobDifficultyManager.java
        BiomeMultipliers.java
        BossEnhancer.java
        KingMobManager.java
        BloodMoonManager.java
      crafting/
        RecipeManager.java
        VanillaRecipeRemover.java
      weapons/
        BattleSwordManager.java
        BattleAxeManager.java
        BattleBowManager.java
        BattleMaceManager.java
        BattleShovelManager.java
        BattlePickaxeManager.java
        BattleTridentManager.java
        BattleSpearManager.java
        SickleManager.java
        SpearManager.java
        SuperToolManager.java
        WeaponAbilityListener.java
        WeaponMasteryManager.java
      legendary/                            # NEW (Phase 8)
        LegendaryWeaponManager.java        # Creates all 24 legendary items
        LegendaryAbilityListener.java      # Right-click + hold abilities
        LegendaryLootListener.java         # Chest loot injection + boss drops
      totems/                               # NEW (Phase 9)
        PlantTotemManager.java
        PlantTotemListener.java
        BossTotemManager.java
        BossTotemListener.java
      catalog/                              # NEW (Phase 11)
        CatalogManager.java
        CatalogListener.java
      book/                                 # NEW (Phase 10)
        WelcomeBookManager.java
      utility/
        BestBuddiesListener.java
        DropRateListener.java
        EnchantTransferListener.java
        InventorySortListener.java
        LootBoosterListener.java
        PaleGardenFogTask.java
        VillagerTradeListener.java
    resources/
      plugin.yml
      config.yml
  resource-pack/                            # NEW (Phase 7) — Java resource pack source
    pack.mcmeta
    assets/
      minecraft/models/item/                # Vanilla model overrides
      jglimsplugin/
        models/item/                        # Custom 3D models
        textures/item/                      # Custom textures
  bedrock-pack/                             # NEW (Phase 7) — Generated by Rainbow
SECTION M — IMAGE URLS FROM PREVIOUS SESSIONS
These images were shared during previous conversations and contain reference material (weapon designs, UI mockups, texture previews, etc.):

https://www.genspark.ai/api/files/s/nhRKbJOw
https://www.genspark.ai/api/files/s/rwop3T2K
https://www.genspark.ai/api/files/s/wm26PNPW
https://www.genspark.ai/api/files/s/Mv2yS8Iu
https://www.genspark.ai/api/files/s/PlrWlWpI
https://www.genspark.ai/api/files/s/XGtWb2G8
https://www.genspark.ai/api/files/s/tZUP7HEg
https://www.genspark.ai/api/files/s/Ch1hMkh3
https://www.genspark.ai/api/files/s/GRfk6sBT
https://www.genspark.ai/api/files/s/TOnhJaKI
https://www.genspark.ai/api/files/s/HYmXvxWz
https://www.genspark.ai/api/files/s/Y9U308nE
https://www.genspark.ai/api/files/s/hCCnZru2
https://www.genspark.ai/api/files/s/4Agrpixv
https://www.genspark.ai/api/files/s/z0Wxs9RY
https://www.genspark.ai/api/files/s/lhLtipOJ
https://www.genspark.ai/api/files/s/pnBbsfiY
SECTION N — KEY REFERENCE LINKS
Resource	URL
JGlimsPlugin Repo	https://github.com/JGlims/JGlimsPlugin
Latest Commit	https://github.com/JGlims/JGlimsPlugin/commit/caf666288523f0c8b5a431717a1470eea0e3d566
Source Tree	https://github.com/JGlims/JGlimsPlugin/tree/main/src/main/java/com/jglims/plugin
Config File	https://github.com/JGlims/JGlimsPlugin/blob/main/src/main/resources/config.yml
Plugin Descriptor	https://github.com/JGlims/JGlimsPlugin/blob/main/src/main/resources/plugin.yml
Paper API Docs	https://jd.papermc.io/paper/1.21.1/
GeyserMC Wiki	https://geysermc.org/wiki/geyser/
Geyser Custom Items (v2)	https://geysermc.org/wiki/geyser/custom-items/
Rainbow (Geyser pack converter)	https://geysermc.org/wiki/other/rainbow/
Thunder (simple RP converter)	https://geysermc.org/wiki/other/thunder/
Hydraulic (mod compat, experimental)	https://geysermc.org/wiki/other/hydraulic/
Fantasy 3D Weapons CIT (Modrinth)	https://modrinth.com/resourcepack/fantasy-3d-weapons-cit
Fantasy 3D Naming Guide	https://nongkos-3d-weapons-guide.webflow.io/
Blades of Majestica (Modrinth)	https://modrinth.com/resourcepack/blades-of-majestica
Blades of Majestica Name List	https://realm-of-majestica.webflow.io/
Spryzeen's Knight Armor (Modrinth)	https://modrinth.com/resourcepack/spryzeens-knight-armor
Fresh Animations Bedrock (MCPEDL)	https://mcpedl.com/fresh-animations-bedrock/
Fresh Animations Java (Modrinth)	https://modrinth.com/resourcepack/fresh-animations
GeyserMC Download	https://geysermc.org/download/
Geyser Example Mappings	https://github.com/eclipseisoffline/geyser-example-mappings/
SECTION O — WORK METHOD SUMMARY
The project follows a strict workflow: Claude fetches the current file state from GitHub before any edit, supplies complete file replacements with exact paths, the user copies, pushes to GitHub, builds with .\gradlew.bat clean jar, transfers to the server via SCP + Docker copy, restarts the container, and verifies via log checks and TPS monitoring. Every new listener must be registered. Every new config value must go through ConfigManager. Every item uses PDC. Every text output uses Adventure API. One logical change per commit. Verify after pushing by re-fetching from GitHub.
DO NOT FORGET ABOUT THE TRIDENT, DO NOT FORGET ABOUT FRESH ANIMATIONS PLAYR EXTENSIONS, follow the rules, Create even more ideas for more legendary weapons based on the majestica and the fantasy 3d weapons, more commom ones, since the ones we already decided are insanely good, but I want more and more commom ones, for the chests of the world, lets fucking go ? always read the github
Alright, let's do this! First, let me fetch the current state of the repository and the weapon texture reference sites to see what textures are available that we haven't used yet. Then I'll design a bunch of new "common" legendary weapons for world chest drops.

Using Tool
|
Read
https://github.com/JGlims/JGlimsPlugin/tree/main/src/main/java/com/jglims/plugin
View
Using Tool
|
Read
https://realm-of-majestica.webflow.io/
View
Using Tool
|
Read
https://nongkos-3d-weapons-guide.webflow.io/
View
Now let me get the Majestica full name list and also check what textures exist in Fantasy 3D that we haven't used yet. Let me also fetch the current plugin source to understand the repo structure.

Using Tool
|
Read
https://github.com/JGlims/JGlimsPlugin/tree/main/src/main/java/com/jglims/plugin/weapons
View
Using Tool
|
Search
modrinth "blades of majestica" full weapon list names
View
Using Tool
|
Search
modrinth "fantasy 3d weapons" full weapon names list nongko
View
Let me get the latest version changelog of Fantasy 3D to see the complete current weapon list, and the Majestica changelog for the same purpose. I also want to check the Fresh Animations Player Extension page you showed me.

Using Tool
|
Read
https://modrinth.com/resourcepack/fantasy-3d-weapons-cit/changelog
View
Using Tool
|
Read
https://modrinth.com/resourcepack/blades-of-majestica/changelog
View
Using Tool
|
Read
https://modrinth.com/resourcepack/fa_player_extension
View
Modrinth's changelog pages are loading as stubs. Let me try fetching the descriptions directly for those packs, and also grab the FA Player Extension page properly.

Using Tool
|
Read
https://modrinth.com/resourcepack/fantasy-3d-weapons-cit
View
Using Tool
|
Read
https://modrinth.com/resourcepack/blades-of-majestica
View
Using Tool
|
Search
"fresh animations player extension" modrinth bedrock compatibility
View
Now let me get the full FA Player Extension page and also search for what textures are available in both packs that haven't been used in the existing 24 legendaries. Let me also check for trident-type legendaries specifically.

Using Tool
|
Read
https://modrinth.com/resourcepack/fa-player-extension
View
Using Tool
|
Search
nongko fantasy 3d weapons complete list "Nocturne" "Vesper" "Oculus" "Piercer" all weapons
View
Using Tool
|
Search
blades of majestica complete weapon names "Heavenly Partisan" "Jade Halberd" "Frost Axe" trident polearm
View
Now I have a solid understanding of both texture packs and the project state. Let me compile the complete lists from the naming guides I already crawled. Let me also verify the FA Player Extension is Java-only (which it is — requires EMF/ETF) and note that for the project doc.

Based on everything I've gathered, here's the comprehensive breakdown.

LEGENDARY WEAPONS EXPANSION — "Common" Tier Legendaries for World Chests
OVERVIEW
You asked for more legendary weapons — specifically more common ones that populate the world's chests, making exploration rewarding at every turn. The existing 24 legendaries are the elite tier (boss drops + rare chest finds). These new ones are the "Uncommon Legendary" tier — still non-craftable, still with unique abilities, but slightly lower base damage (10–13 range instead of 13–18), simpler abilities, and much higher spawn rates in chests. Think of these as the weapons that make a player say "hell yes" when they crack open a random chest, while the original 24 remain the truly jaw-dropping finds.

I'll also address Trident legendaries (which the original 24 completely lacks — no legendary trident!), and the Fresh Animations Player Extension situation.

FRESH ANIMATIONS: PLAYER EXTENSION — STATUS
Fresh Animations: Player Extension (by FreshLX) is Java-only. It requires EMF (Entity Model Features) + ETF (Entity Texture Features) mods on Fabric/Quilt. It animates the player model itself — custom idle, running, sprinting, swimming, climbing, jumping, falling, elytra gliding, first-person hand animations, and more. It's loaded above the base Fresh Animations pack.

For the project:

JotaGlims (Java): Add FA: Player Extension to the client-side mod setup, loaded above Fresh Animations in the resource pack order. This is purely client-side — the server doesn't need to do anything. Update the recommended client setup from Section F.2 to include it.

Gustafare5693 (Bedrock): FA: Player Extension has no Bedrock port and its ToS explicitly prohibits porting to Bedrock Edition. The Bedrock player won't get player animation overhauls. The base Fresh Animations Bedrock port (from MCPEDL) covers mob animations only. This is a limitation we can't work around.

Updated recommended client setup for JotaGlims (Java): Fabric + EMF + ETF + CIT Resewn, then resource packs in order (top to bottom):

Fresh Animations: Player Extension
Fresh Animations
AL's mob revamp packs (+ FA variants)
Drodi's Blazes × Fresh Animations
Server resource pack (auto-applied, contains custom weapon models)
TEXTURE INVENTORY — What's Available and Unused
Fantasy 3D Weapons (by nongko) — Complete list from naming guide:
Already used in existing 24 legendaries: Berserker's Greataxe, Treacherous Cleaver (→ Acidic Cleaver), Black Iron Greatsword, Gilded Phoenix Greataxe (→ Phoenix's Grace), Windreaper, Phantomguard Greatsword, Moonlight, Zenith, Grand Claymore, Solstice, Dragon Sword, Talonbrand, Emerald Greatcleaver

Already used in Battle/Super tiers: Iron Hay Sickle, Uchigatana, Iron Battle Axe, Iron Halberd, Iron Mace, Iron Sai, Heavenly Partisan, Crystal Frostscythe, Black Iron Clobberer, Crimson Cleaver, Azure Scythe

AVAILABLE (unused): Nocturne (New!), Phantomguard Partisan, Viridian Greataxe, Crescent Greataxe, Berserker's Cleaver, Revenant's Gravescepter, Revenant's Gravecleaver, Crystal Frostblade, Lycanbane, Gloomsteel Greataxe, Gloomsteel Katana, Gloomsteel Knife, Vesper, Calamity Blade (already used), Ancient Greatslab, Demonslayer's Greatsword, Sacrificial Cleaver, Amethyst Greatblade, Black Iron Greataxe, Spider Sword, Skeleton Axe, Piercer, Runic Piercer, Demonic Sword, Nature Sword, Vengeance Blade, Vindicator, Treacherous Bludgeon, Treacherous Axe, Wooden Bludgeon, Wooden Tonfa, Iron Greataxe, Iron Polearm, Iron Broadsword, Iron Dagger, Flamberge, Claymore, Oculus, Azure Greatsword, Azure Dagger, Azure Sabre, Azure Greataxe

Blades of Majestica — From realm-of-majestica.webflow.io:
Already used: Ocean Rage, Aquantic Sacred Blade, Phoenixe's Grace (→ True Excalibur texture), Soul Devourer, Royal Chakram, Muramasa, Soul Collector, Amethyst Shuriken, Valhakyra, Calamity Blade, Demon's Blood Blade, Jade Halberd, Frost Axe, Thousand Demon Daggers, Sculk Scythe, Hearthflame

AVAILABLE (from the full 90+ weapon list): Too many to list individually, but the Majestica site shows dozens of additional swords, axes, and polearms I can see from the images. The pack has 90+ weapons total.

NEW "UNCOMMON LEGENDARY" WEAPONS — 20 New Additions
These are designed with the following principles:

Lower base damage (10–13) compared to the elite legendaries (13–18)
Simpler but still fun abilities — shorter cooldowns, less dramatic effects
Higher chest spawn rates — these fill the world with loot excitement
INCLUDES TRIDENT LEGENDARIES — 3 of them, because we cannot forget the trident!
Maintains 40%+ ratio from each texture pack
THE TABLE
#	Weapon Name	Texture Source	Texture Name	Base Item	Base DMG	Right-Click Ability	Hold Ability	Drop Location
25	Nocturne	Fantasy 3D	Nocturne	Diamond Sword	12	Shadow Slash — 6-block line of dark particles, 10 dmg + Blindness 2s to all in path, 7s CD	Night Cloak — 6s of Invisibility + next melee hit deals +8 bonus dmg, 20s CD	Mineshaft, Dungeon (15%)
26	Gravescepter	Fantasy 3D	Revenant's Gravescepter	Diamond Sword	11	Grave Rise — Summon 2 zombie allies for 12s, 6 dmg each hit, 15s CD	Death's Grasp — Root target in place for 3s (no movement), 18s CD	Dungeon, Ancient City (12%)
27	Lycanbane	Fantasy 3D	Lycanbane	Diamond Sword	12	Silver Strike — Single-target 14 dmg + clears all positive effects from target, 8s CD	Hunter's Sense — 10s Glowing on all entities within 20 blocks, 20s CD	Woodland Mansion (15%)
28	Gloomsteel Katana	Fantasy 3D	Gloomsteel Katana	Diamond Sword	11	Quick Draw — Instant dash 5 blocks + 10 dmg to target in front, 5s CD	Shadow Stance — 5s: +25% dodge chance (attacks have 25% chance to deal 0 dmg), 18s CD	Mineshaft, Dungeon (12%)
29	Viridian Cleaver	Fantasy 3D	Viridian Greataxe	Diamond Axe	13	Verdant Slam — 5-block AoE ground pound, 12 dmg + Slowness II 3s, 8s CD	Overgrowth — Plant vines on target (1 heart/s for 6s, rooted 2s), 22s CD	Jungle Temple (18%)
30	Crescent Edge	Fantasy 3D	Crescent Greataxe	Diamond Axe	12	Lunar Cleave — 180° arc 6 blocks, 10 dmg, 7s CD	Crescent Guard — 4s parry: next melee hit taken reflects 100% dmg back, 20s CD	Pillager Outpost (15%)
31	Gravecleaver	Fantasy 3D	Revenant's Gravecleaver	Diamond Axe	12	Bone Shatter — Single target 15 dmg + removes 2 armor points for 10s, 10s CD	Undying Rage — 8s: at 0 HP, survive with 1 HP once (then immune fades), 45s CD	Ancient City (12%)
32	Amethyst Greatblade	Fantasy 3D	Amethyst Greatblade	Diamond Sword	11	Crystal Burst — 4-block AoE amethyst shards, 9 dmg + Levitation 2s, 8s CD	Gem Resonance — 8s: all allies within 10 blocks get Strength I, 25s CD	Amethyst Geode chests*, Trail Ruins (10%)
33	Flamberge	Fantasy 3D	Flamberge	Diamond Sword	12	Flame Wave — 6-block cone of fire, 10 dmg + Fire 4s, 8s CD	Ember Shield — 5s: attackers take 4 fire dmg when hitting you, 18s CD	Nether Fortress (10%), Ruined Portal (8%)
34	Crystal Frostblade	Fantasy 3D	Crystal Frostblade	Diamond Sword	11	Frost Spike — 8-block ranged ice projectile, 10 dmg + Slowness III 3s, 7s CD	Permafrost — 5-block radius: all enemies get Slowness II + Mining Fatigue I for 6s, 22s CD	Igloo (20%), Snow structures (15%)
35	Demonslayer	Fantasy 3D	Demonslayer's Greatsword	Diamond Sword	13	Holy Rend — +50% dmg to Undead/Nether mobs, 14 dmg normal / 21 to targets, 8s CD	Purifying Aura — 6s: all Undead within 8 blocks take 2 dmg/s, 20s CD	Nether Fortress (10%), Desert Temple (10%)
36	Vengeance	Fantasy 3D	Vengeance Blade	Diamond Sword	10	Retribution — Next 8s: store all dmg taken, then release as AoE (stored × 1.5), 12s CD	Grudge Mark — Mark target for 15s: you deal +20% dmg to them specifically, 15s CD	Trial Chambers (12%), Dungeon (8%)
37	Oculus	Fantasy 3D	Oculus	Diamond Sword	11	All-Seeing Strike — Auto-target nearest enemy within 10 blocks, teleport to them + 12 dmg, 8s CD	Third Eye — 10s: see through walls (Glowing on all entities within 30 blocks), 25s CD	Stronghold (10%), End City (8%)
38	Ancient Greatslab	Fantasy 3D	Ancient Greatslab	Diamond Sword	13	Seismic Slam — 6-block AoE, 11 dmg + all entities bounce upward 3 blocks, 9s CD	Stone Skin — 6s: Resistance II + immune to knockback, 22s CD	Desert Temple (12%), Trail Ruins (12%)
39	Neptune's Fang	Majestica	Frost Axe	Trident	12	Riptide Slash — Throw trident in a line, pierces up to 4 entities, 8 dmg each + Slowness II, returns, 7s CD	Maelstrom — 6-block radius water vortex, pulls enemies in + 3 dmg/s for 5s, 22s CD	Ocean Monument (15%), Shipwreck (10%)
40	Tidecaller	Majestica	Aquantic Sacred Blade variant**	Trident	11	Tidal Spear — Charged throw: 12 dmg + 6-block knockback + Conduit Power 10s to self, 8s CD	Depth Ward — 8s: Dolphin's Grace + Respiration, immune to Drowned, 20s CD	Ocean Monument (12%), Buried Treasure (15%)
41	Stormfork	Majestica	Ocean Rage variant**	Trident	13	Lightning Javelin — Throw trident, strikes lightning on impact, 14 dmg + 3-block AoE, 10s CD	Thunder Shield — 6s: any entity that hits you gets struck by lightning (4 dmg), 25s CD	Shipwreck (12%), Ocean Ruins (10%)
42	Jade Reaper	Majestica	Jade Halberd	Diamond Hoe (Sickle base)	12	Jade Crescent — 180° sweep 7 blocks, 10 dmg + Poison I 4s, 7s CD	Emerald Harvest — 10s: every kill drops 1–3 emeralds in addition to normal loot, 30s CD	Jungle Temple (15%), Woodland Mansion (10%)
43	Vindicator	Fantasy 3D	Vindicator	Diamond Axe	11	Executioner's Chop — Single target, deals +1 dmg per missing heart on target (max +10), 8s CD	Rally Cry — 6s: all allies within 8 blocks get Speed I + Strength I, 25s CD	Pillager Outpost (15%), Woodland Mansion (12%)
44	Spider Fang	Fantasy 3D	Spider Sword	Diamond Sword	10	Web Trap — Shoot cobweb projectile, roots target for 3s + Poison II 4s, 8s CD	Wall Crawler — 8s: gain ability to climb walls (levitation when against blocks) + Night Vision, 20s CD	Mineshaft (15%), Trial Chambers (10%)
*For Amethyst Geode chests: requires LegendaryLootListener to also inject into naturally generating chests near amethyst geodes, or simply use Mineshaft chests as the proxy since geodes often generate near them.

**For trident legendaries (39–41): Majestica doesn't have trident-specific textures, but since tridents are sword-shaped when held, we use Majestica sword textures mapped via CustomModelData onto TRIDENT items. The 3D model looks like a mystical polearm/fork when held, which fits the fantasy theme. Alternatively, we can use Fantasy 3D's polearm models (Phantomguard Partisan, Iron Polearm, Heavenly Partisan) re-mapped to trident. The CustomModelData approach means ANY model can be assigned to ANY base item — so a Majestica sword texture on a trident base item works perfectly.

TEXTURE DISTRIBUTION SUMMARY (All 44 Legendaries)
Original 24:

Majestica: 10 (42%)
Fantasy 3D: 14 (58%)
New 20 "Uncommon":

Majestica: 4 (20%) — trident legendaries + Jade Reaper
Fantasy 3D: 16 (80%)
Combined 44 total:

Majestica: 14 (32%)
Fantasy 3D: 30 (68%)
This slightly favors Fantasy 3D since many of the common-tier weapons use simpler Fantasy 3D textures (which fit the "uncommon" aesthetic better — less ornate, more grounded). The Majestica textures tend to look more epic/ornate and suit the elite legendaries. However, if you want to push Majestica higher, we can swap some. I have more Majestica textures available.

LEGENDARY RARITY TIERS (Updated System)
To distinguish between the original 24 and these new 20, I propose a two-tier system within legendaries:

LEGENDARY (the original 24):

Dark purple bold "LEGENDARY" tag in lore
Base damage 13–18
Complex abilities with dramatic visual effects
Drop from bosses (guaranteed from pool) or rare chest finds (8–25%)
CustomModelData range: 30001–30024
UNCOMMON LEGENDARY (the new 20):

Gold bold "UNCOMMON LEGENDARY" tag in lore
Base damage 10–13
Fun but simpler abilities
Found more commonly in world chests (8–20%)
CustomModelData range: 30025–30044
Blood Moon King drops one of THESE (not the elite ones) at 10% chance — makes the elite boss drops feel more special
Ender Dragon death chest: 1 elite legendary (guaranteed) + 1 uncommon legendary (50% chance)
Both tiers share the same PDC system: is_legendary_weapon, legendary_id, legendary_cooldown. A new PDC tag legendary_tier (STRING: "legendary" or "uncommon") distinguishes them.

UPDATED DROP TABLE (Uncommon Legendaries)
Structure	Chance	Pool
Mineshaft	15%	Nocturne, Gloomsteel Katana, Spider Fang
Dungeon (Spawner Room)	12%	Gravescepter, Nocturne, Vengeance
Jungle Temple	18%	Viridian Cleaver, Jade Reaper
Desert Temple	12%	Ancient Greatslab, Demonslayer
Pillager Outpost	15%	Crescent Edge, Vindicator
Woodland Mansion	12%	Lycanbane, Jade Reaper, Vindicator
Ancient City	12%	Gravescepter, Gravecleaver
Nether Fortress	10%	Flamberge, Demonslayer
Ruined Portal	8%	Flamberge
Trail Ruins	12%	Amethyst Greatblade, Ancient Greatslab
Trial Chambers	12%	Vengeance, Spider Fang
Ocean Monument	15%	Neptune's Fang, Tidecaller
Shipwreck	12%	Stormfork, Tidecaller
Buried Treasure	15%	Tidecaller
Ocean Ruins	10%	Stormfork
Igloo	20%	Crystal Frostblade
Stronghold	10%	Oculus
End City	8%	Oculus
Bastion Remnant	10%	Flamberge, Gravecleaver
TRIDENT LEGENDARY DETAILS (The ones you told me not to forget!)
The three trident legendaries deserve special attention since the original 24 had ZERO trident-type weapons:

#39 — Neptune's Fang (DMG: 12) This is the "Riptide replacement" — a trident that's amazing for melee water combat. The Maelstrom hold ability creates a visible water vortex using WATER_SPLASH and BUBBLE particles that sucks nearby enemies toward you. Works in rain too. The trident retains its throwable nature for the right-click ability but always returns (uses custom projectile logic, not vanilla Loyalty — since legendary weapons are unbreakable and unenchantable).

#40 — Tidecaller (DMG: 11) The "support trident" — Conduit Power on throw, Dolphin's Grace on hold. This is the ocean exploration legendary. The "immune to Drowned" hold effect makes EntityDamageByEntityEvent cancel damage when attacker is DROWNED and the player has the buff active. Perfect for ocean monument raids.

#41 — Stormfork (DMG: 13) The "Channeling on steroids" — throws lightning at will (no thunderstorm required). The hold ability punishes melee attackers with lightning. This is the highest-damage trident legendary and the most combat-oriented. Lightning strike uses world.strikeLightning() for visual effect + custom damage to avoid the vanilla lightning's unpredictable damage.

All three trident legendaries work as both melee weapons AND throwable projectiles. The plugin needs to handle ProjectileLaunchEvent for legendary tridents — when thrown, they deal ability damage on hit and return automatically (simulated Loyalty). The hold ability charges while the player holds right-click in melee stance (not throwing stance — detected by checking if the player is NOT in water for Riptide, and using the 2s charge timer).

IMPLEMENTATION NOTES FOR PHASE 8
The LegendaryWeaponManager.java now needs to handle 44 weapons instead of 24. The enum or registry should be structured like:

OCEANS_RAGE("oceans_rage", "Ocean's Rage", Material.DIAMOND_SWORD, 14, LegendaryTier.LEGENDARY, 30001),
// ... original 24 ...
NOCTURNE("nocturne", "Nocturne", Material.DIAMOND_SWORD, 12, LegendaryTier.UNCOMMON, 30025),
// ... new 20 ...
NEPTUNES_FANG("neptunes_fang", "Neptune's Fang", Material.TRIDENT, 12, LegendaryTier.UNCOMMON, 30039),
TIDECALLER("tidecaller", "Tidecaller", Material.TRIDENT, 11, LegendaryTier.UNCOMMON, 30040),
STORMFORK("stormfork", "Stormfork", Material.TRIDENT, 13, LegendaryTier.UNCOMMON, 30041),
The LegendaryAbilityListener.java needs a trident-specific section that handles:

Melee abilities (sneak + right-click with trident in hand, not thrown)
The trident return mechanic for legendary tridents (override vanilla Loyalty behavior)
Hold ability detection for trident (same 2s hold pattern, but must distinguish from throw charge)
The LegendaryLootListener.java gets the expanded structure table with all the new locations.

UPDATED SECTION F.2 — Recommended Client Setup
For JotaGlims (Java): Install Fabric + EMF + ETF + CIT Resewn, then resource packs (load order, top = highest priority):

Fresh Animations: Player Extension ← NEW, adds dynamic player animations
Fresh Animations
AL's Zombie Revamped + FA, AL's Skeleton Revamped + FA, AL's Creeper Revamped + FA, AL's Enderman Revamped + FA, AL's Boss Rush + FA, AL's Piglin Revamped + FA, AL's Dungeons Boss Bars, AL's Mob Weapons
Drodi's Blazes × Fresh Animations
Server resource pack (auto-downloaded, contains custom weapon models, Spryzeen's Knight Armor, Enchantment Outlines, etc.)
For Gustafare5693 (Bedrock): Auto-download from Geyser packs/ folder:

Fresh Animations Bedrock (from MCPEDL) — mob animations only, no player extension available for Bedrock (ToS prohibits porting)
Any AL revamp packs with Bedrock ports
Server's custom weapon Bedrock resource pack (generated by Rainbow)
FULL UPDATED LEGENDARY WEAPON COUNT
Category	Count	Examples
Elite Legendary (Swords)	20	Ocean's Rage, True Excalibur, Zenith, etc.
Elite Legendary (Other)	4	Royal Chakram (throwable), Amethyst Shuriken (throwable), Valhakyra, Berserker's Greataxe
Uncommon Legendary (Swords)	13	Nocturne, Lycanbane, Flamberge, Demonslayer, etc.
Uncommon Legendary (Axes)	4	Viridian Cleaver, Crescent Edge, Gravecleaver, Vindicator
Uncommon Legendary (Tridents)	3	Neptune's Fang, Tidecaller, Stormfork
TOTAL	44	
This gives the world 44 legendary weapons total — 24 elite legendaries that are the endgame chase items, and 20 uncommon legendaries scattered generously throughout every structure type in the game. Players will find uncommon legendaries regularly through exploration (every 5–10 structures roughly), keeping the excitement high, while the elite legendaries remain the prestigious boss-kill / rare-chest rewards. The three trident legendaries ensure ocean content is rewarded, and the FA Player Extension is noted for client-side install on Java.
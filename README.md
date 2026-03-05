JGLIMSPLUGIN — DEFINITIVE PROJECT SUMMARY v3.0
Date compiled: 2026-03-05 Current version: 1.3.0 (plugin.yml) — code contains Phase 8 legendary weapon files (not yet wired into onEnable) Target version: 2.0.0 (after all planned phases) Author: JGlims Players: JotaGlims (Java), Gustafare5693 (Bedrock) Repository: https://github.com/JGlims/JGlimsPlugin Latest commit: a5d0209 — "red lines fix" — 2026-03-05T06:39:23Z Previous key commit: caf6662 (2026-03-04), then ce269aa "Phase 8 start", 214a2d8 "LegendaryAbilityListener.java almost done", 7577b58 "new LegendaryAbilityListener", b779764 "new readme", a5d0209 "red lines fix" API: Paper 1.21.1, Java 21, Gradle 8.x, Kyori Adventure (native in Paper, no shading required) Server: Docker container mc-crossplay on Oracle Cloud, IP 144.22.198.184, Java port 25565, Bedrock port 19132 (GeyserMC + Floodgate)

SECTION A — INSTRUCTIONS FOR THE NEXT CHAT SESSION
Before doing anything, the next assistant must:

Fetch the latest state of every file it plans to edit from https://github.com/JGlims/JGlimsPlugin using https://raw.githubusercontent.com/JGlims/JGlimsPlugin/main/<path>. Never assume file contents — always crawler the raw GitHub URL first.

Supply complete file replacements with the exact path (e.g., src/main/java/com/jglims/plugin/weapons/LegendaryWeaponManager.java). Never send partial diffs or snippets.

Include the full build-and-deploy sequence after every code change:

Copy# PowerShell (local)
cd C:\Users\jgmel\Documents\projects\JGlimsPlugin\JGlimsPlugin
.\gradlew.bat clean jar
Copy# Copy to server
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

Check the latest commit via https://api.github.com/repos/JGlims/JGlimsPlugin/commits?per_page=1 to confirm you're working against the current state.

DO NOT FORGET: Trident legendaries exist (3 weapons: Neptune's Fang, Tidecaller, Stormfork). Fresh Animations: Player Extension is part of the client setup. Always reference the GitHub repo.

SECTION B — CURRENT STATE OF THE PROJECT (v1.3.0 + Phase 8 code on GitHub)
B.1 — Architecture
The repository currently contains 33 Java source files plus 2 resources (plugin.yml, config.yml), following a manager-listener pattern. JGlimsPlugin.java is the central orchestrator. Data persistence uses PDC for items/player data, and guilds.yml for guild state. 17 event listeners are currently registered in onEnable() (the 3 new legendary files exist but are NOT yet registered — see Section B.8). The plugin loads in approximately 1117 ms, TPS is a solid 20.0, CPU at 2.5% idle, RAM at 10.2 GB / 23.9 GB, with no errors in recent logs.

B.2 — 64 Custom Enchantments (12 categories)
Sword ×6, Axe ×6, Pickaxe ×6, Shovel ×3, Hoe/Sickle ×6, Bow ×5, Crossbow ×2, Trident ×6, Armor ×9, Elytra ×4, Mace ×5, Spear ×4, Universal ×2. All stored as integers in PDC. Bidirectional conflict map enforced at anvil-apply time in AnvilRecipeListener.java.

B.3 — 10 Weapon Classes
Sword, Axe, Pickaxe, Shovel, Bow, Mace, Trident, Spear, Sickle, plus Super Tool tiers. Each has its own Manager class (BattleSwordManager.java, BattleAxeManager.java, BattleBowManager.java, BattleMaceManager.java, BattleShovelManager.java, BattlePickaxeManager.java, BattleTridentManager.java, BattleSpearManager.java, SickleManager.java, SpearManager.java, SuperToolManager.java) with PDC tags, display name, lore, attribute modifiers, and durability. Super Tools come in Iron (+1.0 dmg), Diamond (+2.0 dmg), and Netherite (+2.0 dmg base, +2% per custom enchantment) tiers. Enchantments are preserved during tier upgrades via copyAllEnchantments().

B.4 — 20 Weapon Abilities
In WeaponAbilityListener.java (81 KB), 2 per weapon class, triggered by sneak + right-click or on-hit, with cooldowns, action-bar messages, particles, and sounds. Ender Dragon takes 30% reduced ability damage. Netherite super-tools get +2% ability damage per custom enchantment.

B.5 — 3 Blessings
C's Bless: +1 heart per use ×10. Ami's Bless: +2% damage per use ×10. La's Bless: +2% defense per use ×10. Reapplied on join/respawn.

B.6 — Mob Difficulty System
Distance-based scaling (7 brackets: 350→1.5×/1.3×, 700→2.0×/1.6×, 1000→2.5×/1.9×, 2000→3.0×/2.2×, 3000→3.5×/2.5×, 5000→4.0×/3.0×). Biome multipliers: Overworld (Pale Garden 2.0×, Deep Dark 2.5×, Swamp 1.4×), Nether (Wastes 1.7× through Basalt Deltas 2.3×), The End (2.5× health / 2.0× damage). Boss Enhancer: Dragon (3.5×/3.0×), Elder Guardian (2.5×/1.8×), Wither and Warden (vanilla 1.0×). Creeper spawns reduced by 50%.

B.7 — Other Systems
King Mobs: 1 per 100 spawns per EntityType, gold name, glowing, 10× health, 3× damage, drops 3–9 diamonds.

Blood Moon: 15% chance per night, Darkness effect, red particles, overworld monsters (except Creepers) get 1.5× health / 1.3× damage, double drops. Every 10th Blood Moon spawns a Blood Moon King boss (diamond armor, netherite sword, 20× health, 5× damage) dropping 5–15 diamonds, 1 netherite ingot, 500 XP.

Guilds: YAML-based, max 10 members, friendly-fire disabled, commands: /guild create/invite/join/leave/kick/disband/info/list.

Utilities: Inventory sort (shift-click empty slot), enchantment transfer (tool→book), loot booster (guaranteed enchanted book in dungeon chests, 5%/15% mob book drops), drop rate booster (trident 35% from Drowned, guardian shards, ghast tears, breeze charges), villager trades (50% price reduction, unlimited), Pale Garden fog (Darkness effect), soulbound (1 per inventory, lostsoul extraction), Best Buddies (wolf with 95% DR, pacifist, Regen II), weapon mastery (10 classes, up to +10% damage at 1000 kills).

Commands: /jglims (version, reload, stats, enchants, sort, mastery, help, battlemode), /guild (create, invite, join, leave, kick, disband, info, list).

B.8 — Legendary Weapon Files (Phase 8 — ON GITHUB BUT NOT YET WIRED)
Three new files exist in src/main/java/com/jglims/plugin/legendary/:

LegendaryWeapon.java — Enum defining all 44 weapons (24 LEGENDARY + 20 UNCOMMON). Each entry stores: id, displayName, baseMaterial, baseDamage, customModelData (30001–30044), tier (LegendaryTier enum: LEGENDARY or UNCOMMON), textureSource, textureName, rightClickAbilityName, holdAbilityName, rightClickCooldown, holdCooldown. Has fromId(String) static lookup. DMG ranges: LEGENDARY 11–18, UNCOMMON 10–13.

LegendaryWeaponManager.java — Creates legendary weapon ItemStacks. PDC NamespacedKeys: is_legendary_weapon (BYTE), legendary_id (STRING), legendary_tier (STRING), legendary_cooldown (LONG), legendary_damage (used for AttributeModifier key), legendary_speed (used for AttributeModifier key). Methods: isLegendary(), identify(), getTier(), createWeapon(). Sets unbreakable, no glint, CustomModelData, tier-colored bold display name (DARK_PURPLE for LEGENDARY, GOLD for UNCOMMON), detailed lore (tier tag, texture source, damage, speed, right-click ability name + CD, hold ability name + CD, unbreakable/no-enchant notes, trident auto-return hint). Hides attributes, adds ATTACK_DAMAGE = baseDamage−1 and ATTACK_SPEED = materialSpeed−4. Attack speed map: swords 1.6, axes 1.0, trident 1.1, hoes 4.0, mace 0.6, default 1.6.

LegendaryAbilityListener.java — Handles all 88 abilities (44 right-click + 44 hold). Has cooldown system (Map<UUID, Map<String, Long>>), hold-ability charge system (2-second hold with progress bar [■■■■■]), guild-aware enemy detection (getNearbyEnemies() skips same-guild players). Passive buffs tracked in maps: bloodlustStacks (Muramasa), retributionDamageStored (Vengeance), soulCount (Soul Collector), grudgeTargets, predatorMarkTarget, gemBarrierCharges, shadowStanceActive, crescentParryActive, emberShieldActive, thunderShieldActive, undyingRageActive, rebornReady, phaseShiftActive. EntityDamageByEntityEvent handler applies offensive buffs (Bloodlust stacks, Grudge Mark +20%, Predator's Mark +30%) and defensive buffs (Shadow Stance 25% dodge, Crescent parry, Ember Shield fire reflect, Thunder Shield lightning reflect, Gem Barrier hit absorb, Vengeance damage store, Undying Rage survive-at-1HP).

CRITICAL: These 3 files are NOT yet registered in JGlimsPlugin.onEnable(). The next step is to add:

CopyLegendaryWeaponManager legendaryWeaponManager = new LegendaryWeaponManager(this);
pm.registerEvents(new LegendaryAbilityListener(this, configManager, legendaryWeaponManager, guildManager), this);
And add accessor method + field for legendaryWeaponManager. Also need to create and register LegendaryLootListener.java (does not exist yet).

SECTION C — KNOWN BUGS (to fix in Phase 5 — some may already be fixed)
Bug 1 — Sickle shows as "Battle Hoe": SickleManager.java display name is "Battle Hoe" instead of "Sickle." Fix: change display name + lore + recipe discovery message. Sickle damage is only 4, should be 7–8 for iron tier.

Bug 2 — Weapon abilities damage only mobs, not players: All 20 abilities in WeaponAbilityListener.java likely use mob-only damage or filter out Player entities. Fix: use entity.damage(amount, attackerPlayer) and add guild-friendly-fire check. Bow ability already damages players — use as reference.

Bug 3 — Guild default membership ambiguity: Players start with NO guild. /guild info should show "You are not in a guild" instead of erroring.

Bug 4 — Enchantment glint on custom weapons: Battle, Super, and Legendary weapons should NOT show glint. Fix: meta.setEnchantmentGlintOverride(false) in all weapon manager createItem() methods. (Already done in LegendaryWeaponManager; check battle/super managers.)

Bug 5 — Duplicate JAR warning: Always run docker exec mc-crossplay rm -f /data/plugins/JGlimsPlugin*.jar before docker cp.

SECTION D — PHASE ROADMAP
Phase 5: Bug Fixes (needs testing — check if already resolved)
Fix all 5 bugs in Section C.

Phase 6: Performance Profiling
Monitor TPS under load, keep JVM memory under 6 GB, optimize hotspots in EnchantmentEffectListener.java (69 KB) and WeaponAbilityListener.java (81 KB). Consider splitting into per-category classes.

Phase 7: Resource Packs & Custom Textures (MAJOR)
Full details in Section F.

Phase 8: Legendary Weapons (MAJOR — IN PROGRESS)
Full details in Section G. Core files (LegendaryWeapon.java, LegendaryWeaponManager.java, LegendaryAbilityListener.java) are on GitHub. Still needed: register in onEnable(), create LegendaryLootListener.java, in-game testing of all 88 abilities, bump plugin.yml to 1.4.0.

Phase 9: Plant Totems & Boss Totems
Full details in Section H.

Phase 10: Welcome Book (Portuguese Brazil)
Full details in Section I.

Phase 11: Creative Menu / Catalog
Full details in Section J.

Phase 12: Mob Difficulty Rebalance
Increase baseline-health-multiplier to 1.5 and baseline-damage-multiplier to 1.3. Add 7500+ bracket (5.0×/4.0×). Add "Legendary Mob" tier (1/500 spawns, 20×HP, 5×DMG, guaranteed diamond + plant totem chance). Bosses scale with number of legendary weapons in player inventory (+10% HP per).

Phase 13: Events & Minibosses
Wither Storm (rare Nether event), Ocean Siege (Guardian attack near monuments), End Rift (End mobs in Overworld). Each spawns a miniboss that drops legendary weapons. Blood Moon King also has a chance.

Phase 14: Survival Mode Switch
Lock admin commands, set proper permissions, remove debug logging.

SECTION E — PROBLEMS SOLVED ACROSS ALL SESSIONS
RecipeManager super-recipe bug (duplicate NamespacedKeys silently failing). BestBuddiesListener never registered in onEnable(). King Mob spawn rate too high (set to 1/100). Inconsistent weapon lore across 10 managers (standardized to Adventure API gold/gray/green scheme). Weapon ability damage too low for scaled mobs (buffed all 20). SpearManager using legacy ChatColor (migrated). MobDifficultyManager hard-coding multipliers (now ConfigManager). BiomeMultipliers hard-coding values (now initWithConfig()). 12 new enchantments added (52→64). Legacy § codes in CustomEnchantManager (migrated to Adventure). Duplicate JAR warning (resolved by deleting old file). LegendaryAbilityListener red-line compilation errors (fixed in commit a5d0209).

Key lessons: Check PDC key uniqueness. Register every listener. Config-driven values. Adventure API mandatory on Paper. GitHub base64 blobs strip §. Super-tool upgrades must preserve enchantments. Spear detection needs both is_battle_spear and super_spear_tier PDC keys.

SECTION F — TEXTURE & RESOURCE PACK INTEGRATION PLAN (Phase 7)
F.1 — Architecture Overview
Java players get a server resource pack via server.properties → resource-pack=<URL>. Server is 1.21.1 so must use CustomModelData (not 1.21.4+ item_model). Plugin sets meta.setCustomModelData(integer), resource pack uses model overrides. CIT name-matching is NOT used — CustomModelData is name-independent, works without OptiFine/CIT Resewn, and converts cleanly to Bedrock.

Bedrock players get packs via Geyser's packs/ folder. Custom items use Rainbow (Fabric tool) to map CustomModelData → Bedrock custom items + geyser_mappings.json. Vanilla texture packs convert via Thunder.

F.2 — Fresh Animations & Client Setup
For JotaGlims (Java): Install Fabric + EMF + ETF + CIT Resewn, then resource packs (load order, top = highest priority):

Fresh Animations: Player Extension ← Adds dynamic player idle/run/swim/climb/jump/fall/elytra animations (Java only, requires EMF+ETF, ToS prohibits Bedrock porting) — https://modrinth.com/resourcepack/fa-player-extension
Fresh Animations — https://modrinth.com/resourcepack/fresh-animations
AL's mob revamp packs (+ FA variants): Zombie, Skeleton, Creeper, Enderman, Boss Rush, Piglin, Dungeons Boss Bars, Mob Weapons
Drodi's Blazes × Fresh Animations
Server resource pack (auto-downloaded, contains custom weapon models, Spryzeen's Knight Armor, Enchantment Outlines, etc.)
For Gustafare5693 (Bedrock): Auto-download from Geyser packs/ folder:

Fresh Animations Bedrock (from MCPEDL: https://mcpedl.com/fresh-animations-bedrock/) — mob animations only, no Player Extension available for Bedrock
AL revamp packs with Bedrock ports
Server custom weapon Bedrock resource pack (generated by Rainbow)
F.3 — Weapon Texture Assignments
Battle weapons (tier 1) → Fantasy 3D Weapons CIT textures:

Battle Weapon	Fantasy 3D Texture	CustomModelData
Battle Sickle	Iron Hay Sickle	10001
Battle Sword	Uchigatana	10002
Battle Axe	Iron Battle Axe	10003
Battle Spear	Iron Halberd	10004
Battle Mace	Iron Mace	10005
Battle Shovel	Iron Sai	10006
Super weapons (tier 2) → Blades of Majestica textures:

Super Weapon	Majestica Texture	CustomModelData
Super Spear	Heavenly Partisan	20001
Super Sword	Thousand Demon Daggers	20002
Super Axe	Hearthflame (Crimson Cleaver)	20003
Super Sickle	Jade Halberd (Crystal Frostscythe)	20004
Super Mace	Frost Axe (Black Iron Clobberer)	20005
Super Shovel	Sculk Scythe (Azure Scythe)	20006
Legendary weapons (tier 3) → See Section G full table.

F.4 — Other Resource Packs
Pack	Purpose	Delivery
Spryzeen's Knight Armor	Medieval armor retextures	Server RP (Java) + Thunder .mcpack (Bedrock)
Enchantment Outlines	Colored glint per enchantment — normal weapons ONLY	Server RP + Thunder
Recolourful Containers GUI + HUD	Custom container/inventory GUI	Server RP + Thunder
Recolourful Containers Hardcore Hearts	Custom heart textures	Server RP + Thunder
Story Mode Clouds	Stylized clouds	Server RP + Thunder
Fresh Food (with blessings)	Custom food textures	Server RP + Thunder
Fantasy 3D Weapons CIT	3D weapon models for battle tier	Server RP, models extracted
Blades of Majestica	3D models for super/legendary tiers	Server RP, models extracted
F.5 — CIT Implementation Strategy
Use CustomModelData integers, NOT name-based CIT matching. Models stored in assets/jglimsplugin/models/item/ and assets/jglimsplugin/textures/item/, with vanilla model overrides in assets/minecraft/models/item/diamond_sword.json etc. pointing to custom models when CustomModelData matches.

SECTION G — LEGENDARY WEAPON SYSTEM (Phase 8)
G.1 — Core Concept
Non-craftable, non-enchantable (AnvilRecipeListener blocks), built-in effects replace enchantments. Each has: unique 3D texture, tier-colored bold display name, lore, fixed damage, two abilities (right-click + hold), PDC tags. Unbreakable, no glint, CustomModelData.

PDC tags: is_legendary_weapon (BYTE, 1), legendary_id (STRING), legendary_tier (STRING: "legendary" or "uncommon"), legendary_cooldown (LONG, set by listener).

Two tiers: LEGENDARY (dark purple, DMG 11–18, boss drops + rare chests) and UNCOMMON (gold, DMG 10–13, common world chests).

G.2 — Full Legendary Weapon Table (All 44)
LEGENDARY TIER (24) — CustomModelData 30001–30024
#	Weapon Name	Texture Source	Texture Name	Base Mat	DMG	RC Ability (CD)	Hold Ability (CD)	Drop Source
1	Ocean's Rage	Majestica	Ocean Rage	Diamond Sword	14	Tidal Crash — 6b AoE water, 15 dmg, knockback (8s)	Riptide Surge — water launch + trail dmg (15s)	Elder Guardian
2	Aquatic Sacred Blade	Majestica	Aquantic Sacred Blade	Diamond Sword	13	Aqua Heal — +6 hearts + Conduit Power 30s (20s)	Depth Pressure — 10b Slowness III + Mining Fatigue (25s)	Elder Guardian
3	True Excalibur	Majestica	Phoenixe's Grace	Diamond Sword	16	Holy Smite — Lightning + 20 dmg AoE 5b (10s)	Divine Shield — 5s invulnerability + Strength II (45s)	Warden
4	Requiem of the Ninth Abyss	Majestica	Soul Devourer	Diamond Sword	15	Soul Devour — drain 8 hearts, heal self (12s)	Abyss Gate — 3 wither skeletons 15s (60s)	Warden
5	Royal Chakram	Majestica	Royal Chakram	Diamond Sword	12	Chakram Throw — bounces 4 targets, 8 dmg each (6s)	Spinning Shield — 3s projectile deflect + 50% melee DR (20s)	Warden
6	Berserker's Greataxe	Fantasy 3D	Berserker's Greataxe	Diamond Axe	17	Berserker Slam — 8b AoE, 18 dmg (10s)	Blood Rage — +50% dmg, +30% speed, −30% def 10s (30s)	Warden
7	Acidic Cleaver	Fantasy 3D	Treacherous Cleaver	Diamond Axe	14	Acid Splash — 5b cone, Poison III 8s (10s)	Corrosive Aura — 6b 1 heart/s for 8s (25s)	Warden
8	Black Iron Greatsword	Fantasy 3D	Black Iron Greatsword	Diamond Sword	15	Dark Slash — 10b line, 16 dmg (8s)	Iron Fortress — Absorption IV + Resistance II 8s (30s)	Warden
9	Muramasa	Majestica	Muramasa	Diamond Sword	13	Crimson Flash — 8b dash, 12 dmg in path (6s)	Bloodlust — kills add +2 dmg stacks ×5, 15s window (20s)	Wither
10	Phoenix's Grace	Fantasy 3D	Gilded Phoenix Greataxe	Diamond Axe	15	Phoenix Strike — Fire AoE 6b, 14 dmg + fire (10s)	Rebirth Flame — revive at 50% HP within 60s (120s)	Wither
11	Soul Collector	Majestica	Soul Collector	Diamond Sword	14	Soul Harvest — kill stores soul, +10 bonus next hit (8s)	Spirit Army — release up to 5 souls as 6-dmg projectiles (30s)	Wither
12	Amethyst Shuriken	Majestica	Amethyst Shuriken	Diamond Sword	11	Shuriken Barrage — 5 fan projectiles, 7 dmg each (7s)	Shadow Step — teleport behind target + guaranteed crit (15s)	Wither
13	Valhakyra	Majestica	Valhakyra	Diamond Sword	15	Valkyrie Dive — leap 10b up, slam 20 dmg AoE (12s)	Wings of Valor — 8s slow-fall glide + Strength I (25s)	End Cities (chest)
14	Windreaper	Fantasy 3D	Windreaper	Diamond Sword	13	Gale Slash — 8b wind cone, 12 dmg + knockback (8s)	Cyclone — 4s tornado, pulls enemies, 4 dmg/s (20s)	End Cities (chest)
15	Phantomguard	Fantasy 3D	Phantomguard Greatsword	Diamond Sword	14	Spectral Cleave — through blocks, 10b line, 14 dmg (10s)	Phase Shift — 3s intangibility (35s)	Ender Dragon (death chest)
16	Moonlight	Fantasy 3D	Moonlight	Diamond Sword	13	Lunar Beam — 15b ranged beam, 16 dmg (10s)	Eclipse — 6s Blindness + Weakness II to 15b enemies (30s)	Ender Dragon (death chest)
17	Zenith	Fantasy 3D	Zenith	Diamond Sword	18	Final Judgment — 360° AoE 8b, 22 dmg (15s)	Ascension — 10s Flight + 50% dmg bonus (60s)	Ender Dragon (death chest)
18	Solstice	Fantasy 3D	Solstice	Diamond Sword	14	Solar Flare — 10b fire AoE, 15 dmg + blindness 3s (10s)	Daybreak — clear negatives + Regen IV 6s (25s)	Stronghold (chest)
19	Grand Claymore	Fantasy 3D	Grand Claymore	Diamond Sword	16	Titan Swing — 180° arc 10b, 18 dmg + knockback (10s)	Colossus Stance — 6s no-knockback, +3 range, +40% dmg (30s)	Bastion Remnant (chest)
20	Calamity Blade	Majestica	Calamity Blade	Diamond Sword	15	Cataclysm — 6b AoE, falling blocks, 14 dmg + slowness (12s)	Doomsday — 8s double damage, but 1 heart/s cost (35s)	Nether Fortress (chest)
21	Dragon Sword	Fantasy 3D	Dragon Sword	Diamond Sword	14	Dragon Breath — 8b fire cone, 12 dmg + breath cloud (10s)	Draconic Roar — 8b Fear + Weakness II 5s (25s)	Ender Dragon (death chest)
22	Talonbrand	Fantasy 3D	Talonbrand	Diamond Sword	13	Talon Strike — triple-hit combo 3×8=24 dmg (8s)	Predator's Mark — target takes +30% from all 10s (20s)	Dungeon (chest)
23	Emerald Greatcleaver	Fantasy 3D	Emerald Greatcleaver	Diamond Axe	16	Emerald Storm — 6b AoE shards, 14 dmg + Poison II (10s)	Gem Barrier — absorb next 3 hits, 15s window (40s)	Temple (chest)
24	Demon's Blood Blade	Majestica	Demon's Blood Blade	Diamond Sword	15	Blood Rite — sacrifice 3 hearts, 25 dmg (8s)	Demonic Form — 10s +60% dmg, fire trail, −50% def (35s)	Blood Moon Boss
Texture distribution (Legendary 24): Majestica 10 (42%), Fantasy 3D 14 (58%)

UNCOMMON LEGENDARY TIER (20) — CustomModelData 30025–30044
#	Weapon Name	Texture Source	Texture Name	Base Mat	DMG	RC Ability (CD)	Hold Ability (CD)	Drop Locations (% chance)
25	Nocturne	Fantasy 3D	Nocturne	Diamond Sword	12	Shadow Slash — 6b line, 10 dmg + Blindness 2s (7s)	Night Cloak — 6s Invis + next melee +8 bonus (20s)	Mineshaft 15%, Dungeon 12%
26	Gravescepter	Fantasy 3D	Revenant's Gravescepter	Diamond Sword	11	Grave Rise — 2 zombie allies 12s, 6 dmg each (15s)	Death's Grasp — root target 3s (18s)	Dungeon 12%, Ancient City 12%
27	Lycanbane	Fantasy 3D	Lycanbane	Diamond Sword	12	Silver Strike — 14 dmg + clears target buffs (8s)	Hunter's Sense — 10s Glowing on 20b entities (20s)	Woodland Mansion 15%
28	Gloomsteel Katana	Fantasy 3D	Gloomsteel Katana	Diamond Sword	11	Quick Draw — 5b dash + 10 dmg (5s)	Shadow Stance — 5s 25% dodge chance (18s)	Mineshaft 12%, Dungeon 12%
29	Viridian Cleaver	Fantasy 3D	Viridian Greataxe	Diamond Axe	13	Verdant Slam — 5b AoE, 12 dmg + Slowness II 3s (8s)	Overgrowth — 1 heart/s 6s + root 2s (22s)	Jungle Temple 18%
30	Crescent Edge	Fantasy 3D	Crescent Greataxe	Diamond Axe	12	Lunar Cleave — 180° 6b, 10 dmg (7s)	Crescent Guard — 4s parry: 100% dmg reflect (20s)	Pillager Outpost 15%
31	Gravecleaver	Fantasy 3D	Revenant's Gravecleaver	Diamond Axe	12	Bone Shatter — 15 dmg + remove 2 armor 10s (10s)	Undying Rage — 8s survive lethal at 1 HP (45s)	Ancient City 12%
32	Amethyst Greatblade	Fantasy 3D	Amethyst Greatblade	Diamond Sword	11	Crystal Burst — 4b AoE shards, 9 dmg + Levitation 2s (8s)	Gem Resonance — 8s Strength I to allies in 10b (25s)	Trail Ruins 10%
33	Flamberge	Fantasy 3D	Flamberge	Diamond Sword	12	Flame Wave — 6b cone fire, 10 dmg + fire 4s (8s)	Ember Shield — 5s attackers take 4 fire dmg (18s)	Nether Fortress 10%, Ruined Portal 8%
34	Crystal Frostblade	Fantasy 3D	Crystal Frostblade	Diamond Sword	11	Frost Spike — 8b ice projectile, 10 dmg + Slowness III 3s (7s)	Permafrost — 5b AoE Slowness II + Mining Fatigue 6s (22s)	Igloo 20%, Snow structures 15%
35	Demonslayer	Fantasy 3D	Demonslayer's Greatsword	Diamond Sword	13	Holy Rend — +50% to Undead/Nether, 14/21 dmg (8s)	Purifying Aura — 6s Undead in 8b take 2 dmg/s (20s)	Nether Fortress 10%, Desert Temple 10%
36	Vengeance	Fantasy 3D	Vengeance Blade	Diamond Sword	10	Retribution — store dmg taken 8s, release ×1.5 AoE (12s)	Grudge Mark — +20% dmg to marked target 15s (15s)	Trial Chambers 12%, Dungeon 8%
37	Oculus	Fantasy 3D	Oculus	Diamond Sword	11	All-Seeing Strike — teleport to nearest enemy 10b + 12 dmg (8s)	Third Eye — 10s Glowing all entities in 30b (25s)	Stronghold 10%, End City 8%
38	Ancient Greatslab	Fantasy 3D	Ancient Greatslab	Diamond Sword	13	Seismic Slam — 6b AoE, 11 dmg + bounce 3b (9s)	Stone Skin — 6s Resistance II + no-knockback (22s)	Desert Temple 12%, Trail Ruins 12%
39	Neptune's Fang	Majestica	Frost Axe	TRIDENT	12	Riptide Slash — pierces 4 entities, 8 dmg each + Slowness II, returns (7s)	Maelstrom — 6b water vortex, pulls enemies, 3 dmg/s 5s (22s)	Ocean Monument 15%, Shipwreck 10%
40	Tidecaller	Majestica	Aquantic Sacred Blade	TRIDENT	11	Tidal Spear — charged throw, 12 dmg + 6b knockback + Conduit Power 10s (8s)	Depth Ward — 8s Dolphin's Grace + Respiration, immune to Drowned (20s)	Ocean Monument 12%, Buried Treasure 15%
41	Stormfork	Majestica	Ocean Rage	TRIDENT	13	Lightning Javelin — throw, lightning on impact, 14 dmg + 3b AoE (10s)	Thunder Shield — 6s attackers struck by lightning 4 dmg (25s)	Shipwreck 12%, Ocean Ruins 10%
42	Jade Reaper	Majestica	Jade Halberd	Diamond Hoe	12	Jade Crescent — 180° sweep 7b, 10 dmg + Poison I 4s (7s)	Emerald Harvest — 10s kills drop 1–3 emeralds (30s)	Jungle Temple 15%, Woodland Mansion 10%
43	Vindicator	Fantasy 3D	Vindicator	Diamond Axe	11	Executioner's Chop — +1 dmg per target missing heart (max +10) (8s)	Rally Cry — 6s Speed I + Strength I to self + allies 8b (25s)	Pillager Outpost 15%, Woodland Mansion 12%
44	Spider Fang	Fantasy 3D	Spider Sword	Diamond Sword	10	Web Trap — cobweb projectile, root 3s + Poison II 4s (8s)	Wall Crawler — 8s wall-climb + Night Vision (20s)	Mineshaft 15%, Trial Chambers 10%
Texture distribution (Uncommon 20): Majestica 4 (20%), Fantasy 3D 16 (80%) Combined total (all 44): Majestica 14 (32%), Fantasy 3D 30 (68%)

G.3 — Drop Tables
Structure Chest Drops (Elite LEGENDARY):

Structure	Chance	Pool
End City	25%	Valhakyra, Windreaper, Phantomguard, Moonlight, Zenith, Dragon Sword
Stronghold	15%	Solstice, True Excalibur
Nether Fortress	12%	Calamity Blade, Muramasa, Phoenix's Grace
Bastion Remnant	12%	Grand Claymore, Berserker's Greataxe
Ocean Monument	10%	Ocean's Rage, Aquatic Sacred Blade
Dungeon (Spawner room)	8%	Talonbrand, Acidic Cleaver, Amethyst Shuriken
Temple (Desert/Jungle)	8%	Emerald Greatcleaver, Soul Collector
Boss Drops (guaranteed 1 from pool):

Boss	Pool
Elder Guardian	Ocean's Rage, Aquatic Sacred Blade
Warden	True Excalibur, Requiem, Royal Chakram, Berserker's Greataxe, Acidic Cleaver, Black Iron Greatsword
Wither	Muramasa, Phoenix's Grace, Soul Collector, Amethyst Shuriken
Ender Dragon	ALL legendaries (1 random in death chest)
Blood Moon King	Any random legendary (10% per kill)
Structure Chest Drops (UNCOMMON): See drop locations in the Uncommon table above.

G.4 — Hold Ability Mechanic
Java: Hold right-click 2+ seconds → action bar shows [■■□□□] Charging... → [■■■■■] ACTIVATED! with beacon sound. Tracked via holdStartTimes map + repeating BukkitRunnable (1-tick interval, 60-tick max window).

Bedrock: Floodgate API detects Bedrock player → map hold ability to double-sneak + right-click as alternative trigger (simpler than GeyserMC form API).

G.5 — No Enchantments on Legendaries
AnvilRecipeListener checks is_legendary_weapon PDC tag and rejects enchantment application. EnchantmentEffectListener also checks and skips. All legendary effects are in LegendaryAbilityListener.java.

G.6 — Trident Legendary Details (DO NOT FORGET)
Three trident legendaries (#39–#41) are TRIDENT base material. They are throwable and return automatically (custom projectile logic, not vanilla Loyalty). The lore says "Throwable — returns automatically". Plugin needs to handle ProjectileLaunchEvent for legendary tridents: deal ability damage on hit, return via teleportation. Hold ability charges in melee stance (2s timer), distinguished from throw charge.

SECTION H — PLANT TOTEMS & BOSS TOTEMS (Phase 9)
H.1 — Plant Totems (Elemental Resistance)
Right-click to absorb (consumed). Permanently adds +10% resistance per type to player PDC, up to 3 absorptions (30%).

Totem	Resistance	Per Use	Max	Found In
Fern Totem	Fire damage	10%	30%	Nether Fortress, Bastion
Moss Totem	Poison/Wither	10%	30%	Jungle Temple, Swamp Huts
Cactus Totem	Projectile	10%	30%	Desert Temple, Pillager Outpost
Vine Totem	Fall damage	10%	30%	Jungle Temple, Mineshaft
Lily Totem	Drowning/Freeze	10%	30%	Ocean Monument, Shipwreck
Mushroom Totem	Explosion	10%	30%	Dungeon, Woodland Mansion
PDC: is_plant_totem + totem_type. Player PDC: totem_fern_level etc.

H.2 — Boss Totems (Unique Passive Abilities)
Right-click to consume (permanent). Boss drop only.

Boss Totem	Drop Source	Passive Effect	Texture Theme
Guardian's Blessing	Elder Guardian	+50% swim speed + Respiration III permanent	Prismarine Ore
Wither's Immunity	Wither	Complete Wither effect immunity	Coal/Obsidian Ore
Warden's Silence	Warden	Silent walking (Sculk sensors ignore you)	Deepslate Ore
Dragon's Gaze	Ender Dragon	Permanent Night Vision + Endermen don't aggro when looked at	End Stone Ore
Player PDC: boss_totem_guardian, boss_totem_wither, boss_totem_warden, boss_totem_dragon (BYTE). Reapplied on join/respawn via BossTotemListener.

SECTION I — WELCOME BOOK (Phase 10)
Written Book (Portuguese Brazil) given on first join. Replacement: /jglims livro. Created via BookMeta with Adventure Components.

15 pages: Bem-vindo (intro), Sistema de Armas (normal/battle/super/legendary + damage table), Receitas de Crafting, Super Ferramentas (tier upgrades), Armas Lendárias (locations/bosses/drops), Encantamentos Customizados (all 64), Encantamentos continuação (conflicts/max levels), Bênçãos (C's/Ami's/La's Bless), Totems (plant + boss), Guildas, Eventos (Blood Moon/minibosses), Dificuldade dos Mobs (distance/biome/dimension/King Mobs), Utilidades, Comandos (/jglims + /guild), Créditos e links.

SECTION J — CREATIVE MENU / CATALOG (Phase 11)
/jglims catalog → virtual chest GUI. 8 pages: Battle Weapons, Super Weapons, Legendary Weapons (2 pages for 44 items), Custom Enchantment Books (2 pages), Blessings, Totems. Glass pane borders, arrow navigation, category selector bottom row. CatalogListener.java cancels all clicks unless OP + creative. Display-only copies with "CATALOG ITEM" lore tag.

SECTION K — MOB DIFFICULTY REBALANCE (Phase 12)
Post-legendary testing. If too easy: bump baseline multipliers to 1.5×/1.3×, add 7500+ bracket (5.0×/4.0×), add Legendary Mob tier (1/500, 20×HP, 5×DMG, diamond + plant totem drops), bosses scale +10% HP per legendary weapon in player inventory.

SECTION L — REPOSITORY STRUCTURE (current + planned)
JGlimsPlugin/
  .gitignore
  README.md                                        ← Contains project summary v2.0
  build.gradle
  settings.gradle
  gradlew / gradlew.bat
  gradle/wrapper/
  src/main/
    java/com/jglims/plugin/
      JGlimsPlugin.java                            # Main plugin, lifecycle, commands
      config/
        ConfigManager.java                         # All config getters
      enchantments/
        EnchantmentType.java                       # Enum: 64 enchantments
        CustomEnchantManager.java                  # PDC read/write, conflicts
        AnvilRecipeListener.java                   # Anvil crafting (blocks legendary)
        EnchantmentEffectListener.java             # All enchantment effects (69 KB)
        SoulboundListener.java                     # Death-save, lostsoul
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
        WeaponAbilityListener.java                 # 20 abilities (81 KB)
        WeaponMasteryManager.java
      legendary/                                   # ✅ ON GITHUB (Phase 8)
        LegendaryWeapon.java                       # Enum: 44 weapons
        LegendaryWeaponManager.java                # Item creation, PDC, lore
        LegendaryAbilityListener.java              # 88 abilities (RC + hold)
        LegendaryLootListener.java                 # 🔴 NOT YET CREATED
      totems/                                      # 🔴 PLANNED (Phase 9)
        PlantTotemManager.java
        PlantTotemListener.java
        BossTotemManager.java
        BossTotemListener.java
      catalog/                                     # 🔴 PLANNED (Phase 11)
        CatalogManager.java
        CatalogListener.java
      book/                                        # 🔴 PLANNED (Phase 10)
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
      plugin.yml                                   # Currently version 1.3.0
      config.yml                                   # 158 lines of config
  resource-pack/                                   # 🔴 PLANNED (Phase 7)
    pack.mcmeta
    assets/
      minecraft/models/item/
      jglimsplugin/models/item/
      jglimsplugin/textures/item/
  bedrock-pack/                                    # 🔴 PLANNED (Phase 7)
SECTION M — IMAGE URLS FROM PREVIOUS SESSIONS
https://www.genspark.ai/api/files/s/nhRKbJOw https://www.genspark.ai/api/files/s/rwop3T2K https://www.genspark.ai/api/files/s/wm26PNPW https://www.genspark.ai/api/files/s/Mv2yS8Iu https://www.genspark.ai/api/files/s/PlrWlWpI https://www.genspark.ai/api/files/s/XGtWb2G8 https://www.genspark.ai/api/files/s/tZUP7HEg https://www.genspark.ai/api/files/s/Ch1hMkh3 https://www.genspark.ai/api/files/s/GRfk6sBT https://www.genspark.ai/api/files/s/TOnhJaKI https://www.genspark.ai/api/files/s/HYmXvxWz https://www.genspark.ai/api/files/s/Y9U308nE https://www.genspark.ai/api/files/s/hCCnZru2 https://www.genspark.ai/api/files/s/4Agrpixv https://www.genspark.ai/api/files/s/z0Wxs9RY https://www.genspark.ai/api/files/s/lhLtipOJ https://www.genspark.ai/api/files/s/pnBbsfiY https://www.genspark.ai/api/files/s/68IPBgKc https://www.genspark.ai/api/files/s/MkbrO4od https://www.genspark.ai/api/files/s/g5CRNe4s https://www.genspark.ai/api/files/s/cyU0rB17 https://www.genspark.ai/api/files/s/R5rgRubC https://www.genspark.ai/api/files/s/8VzQCelE

SECTION N — KEY REFERENCE LINKS
Resource	URL
JGlimsPlugin Repo	https://github.com/JGlims/JGlimsPlugin
Latest Commit (a5d0209)	https://github.com/JGlims/JGlimsPlugin/commit/a5d02096ccde5682d349dc38dd9b5d03a0326a3e
Commit API (check latest)	https://api.github.com/repos/JGlims/JGlimsPlugin/commits?per_page=1
Source Tree	https://github.com/JGlims/JGlimsPlugin/tree/main/src/main/java/com/jglims/plugin
Weapons Dir	https://github.com/JGlims/JGlimsPlugin/tree/main/src/main/java/com/jglims/plugin/weapons
Legendary Dir	https://github.com/JGlims/JGlimsPlugin/tree/main/src/main/java/com/jglims/plugin/legendary
Config File	https://github.com/JGlims/JGlimsPlugin/blob/main/src/main/resources/config.yml
Plugin Descriptor	https://github.com/JGlims/JGlimsPlugin/blob/main/src/main/resources/plugin.yml
Paper API Docs	https://jd.papermc.io/paper/1.21.1/
GeyserMC Wiki	https://geysermc.org/wiki/geyser/
Geyser Custom Items (v2)	https://geysermc.org/wiki/geyser/custom-items/
Rainbow (Geyser converter)	https://geysermc.org/wiki/other/rainbow/
Thunder (simple RP converter)	https://geysermc.org/wiki/other/thunder/
Hydraulic (experimental)	https://geysermc.org/wiki/other/hydraulic/
Fantasy 3D Weapons CIT	https://modrinth.com/resourcepack/fantasy-3d-weapons-cit
Fantasy 3D Naming Guide	https://nongkos-3d-weapons-guide.webflow.io/
Blades of Majestica	https://modrinth.com/resourcepack/blades-of-majestica
Majestica Name List	https://realm-of-majestica.webflow.io/
Spryzeen's Knight Armor	https://modrinth.com/resourcepack/spryzeens-knight-armor
Fresh Animations Java	https://modrinth.com/resourcepack/fresh-animations
Fresh Animations: Player Extension	https://modrinth.com/resourcepack/fa-player-extension
Fresh Animations Bedrock	https://mcpedl.com/fresh-animations-bedrock/
GeyserMC Download	https://geysermc.org/download/
Geyser Example Mappings	https://github.com/eclipseisoffline/geyser-example-mappings/
SECTION O — KEY CODE ASPECTS & PDC TAG REFERENCE
O.1 — All PDC NamespacedKeys (namespace: jglimsplugin)
Weapon identification: is_battle_sword, is_battle_axe, is_battle_bow, is_battle_mace, is_battle_shovel, is_battle_pickaxe, is_battle_trident, is_battle_spear, is_sickle, super_spear_tier, super_tool_tier, is_legendary_weapon

Legendary weapon data: legendary_id (STRING), legendary_tier (STRING: "legendary"/"uncommon"), legendary_cooldown (LONG), legendary_damage (attr modifier key), legendary_speed (attr modifier key)

Enchantment data: Each of the 64 enchantments stored as INTEGER on the item PDC (e.g., flame_aspect, frost_bite, etc.)

Blessing data (on player): c_bless_uses, ami_bless_uses, la_bless_uses (INTEGER), c_bless_hearts, ami_bless_damage, la_bless_defense (DOUBLE)

Other item tags: is_soulbound (BYTE), lostsoul_enchants (STRING — serialized), weapon_mastery_class (STRING), weapon_mastery_kills (INTEGER)

O.2 — Attack Damage Formula (LegendaryWeaponManager)
AttributeModifier value = baseDamage - 1.0 (because Minecraft adds 1.0 base) Attack speed value = materialSpeed - 4.0 (because Minecraft base is 4.0)

Material speeds: Diamond/Netherite/Iron/Gold/Stone/Wooden Sword = 1.6, Diamond/Netherite Axe = 1.0, Trident = 1.1, Diamond/Netherite Hoe = 4.0, Mace = 0.6

O.3 — Config.yml Structure (158 lines)
Sections: mob-difficulty (enabled, baseline multipliers, distance brackets, biome multipliers), boss-enhancer (4 bosses), creeper-reduction, pale-garden-fog, loot-booster, mob-book-drops, blessings (3 types with max-uses and per-use values), anvil (remove too-expensive, XP reduction), toggles (12 feature toggles), drop-rate-booster, villager-trades, king-mob, axe-nerf, weapon-mastery, blood-moon, guilds, best-buddies, super-tools, ore-detect.

O.4 — JGlimsPlugin.onEnable() Initialization Order
ConfigManager (must be first)
CustomEnchantManager
BlessingManager
All 11 weapon managers (Sickle, Axe, Bow, Mace, Sword, Pickaxe, Trident, Spear, SuperTool, Spear/base, Shovel)
RecipeManager (pass all weapon managers) → registerAllRecipes()
VanillaRecipeRemover.remove()
MobDifficultyManager, KingMobManager, BloodMoonManager
WeaponMasteryManager, GuildManager
Register 17+ event listeners (see full list in JGlimsPlugin.java)
Scheduled tasks (PaleGardenFogTask, BloodMoon scheduler)
STILL NEEDED: Add LegendaryWeaponManager instantiation + LegendaryAbilityListener registration + LegendaryLootListener creation/registration to onEnable().

SECTION P — IMMEDIATE NEXT STEPS (Priority Order)
Wire legendary system into onEnable(): Add LegendaryWeaponManager field + accessor, register LegendaryAbilityListener, update plugin.yml version to 1.4.0.

Create LegendaryLootListener.java: Handle LootGenerateEvent for structure chests (inject legendary weapons based on drop tables), handle boss death events (inject guaranteed drops), handle Blood Moon King drop chance.

Add /jglims legendary command: Give a specific legendary weapon by ID for testing purposes. Also add legendary weapons to the /jglims catalog when Phase 11 is implemented.

Build, deploy, test all 88 abilities in-game. Verify particles, damage values, cooldowns, action-bar messages, trident return behavior, hold-ability charge bar.

Fix remaining Phase 5 bugs (Sickle name, PvP abilities, guild default, glint on battle/super weapons).

Bump version to 1.4.0 after legendary system works.

SECTION Q — WORK METHOD SUMMARY
The project follows a strict workflow: Claude fetches the current file state from GitHub (raw.githubusercontent.com) before any edit, supplies complete file replacements with exact paths, the user copies them, pushes to GitHub, builds with .\gradlew.bat clean jar, transfers to the server via SCP + Docker copy (always delete old JAR first), restarts the container, and verifies via log checks and TPS monitoring. Every new listener must be registered in onEnable(). Every new config value must go through ConfigManager. Every item uses PDC. Every text output uses Adventure API. One logical change per commit. Verify after pushing by re-fetching from GitHub. Always check https://api.github.com/repos/JGlims/JGlimsPlugin/commits?per_page=1 for the actual latest commit before starting work.





JGLIMSPLUGIN — DEFINITIVE PROJECT SUMMARY v5.0
Date compiled: 2026-03-05 Current version: 1.4.0 (plugin.yml confirmed) Target version: 2.0.0 (after all planned phases) Author: JGlims Players: JotaGlims (Java), Gustafare5693 (Bedrock) Repository: https://github.com/JGlims/JGlimsPlugin Latest commit: 288f296d — "." — 2026-03-05T18:03:10Z Previous commits (today): 743a21e6 (08:14:47), 0c2accc3 "new fixes" (07:11:01), 4e217caa "new readme" (06:58:43), a5d02096 "red lines fix" (06:39:23) API: Paper 1.21.1, Java 21, Gradle 8.x, Kyori Adventure (native in Paper — no shading) Server: Docker container mc-crossplay on Oracle Cloud, IP 144.22.198.184, Java port 25565, Bedrock port 19132 (GeyserMC + Floodgate) SSH alias: MinecraftServer (ubuntu user) Local dev path: C:\Users\jgmel\Documents\projects\JGlimsPlugin\JGlimsPlugin\ Resource pack work dir: C:\Users\jgmel\Documents\projects\JGlimsPlugin\resourcepack-work\JGlimsResourcePack\

SECTION A — MANDATORY RULES FOR THE NEXT CHAT SESSION
Before doing anything, the next assistant must:

A.1 — Fetch the latest state of every file it plans to edit from https://raw.githubusercontent.com/JGlims/JGlimsPlugin/main/<path>. Never assume file contents — always crawler the raw GitHub URL first.

A.2 — Supply complete file replacements with the exact path (e.g., src/main/java/com/jglims/plugin/legendary/LegendaryWeaponManager.java). Never send partial diffs or snippets.

A.3 — Write files in PowerShell using [System.IO.File]::WriteAllText(..., (New-Object System.Text.UTF8Encoding $false)) and heredocs @" … "@ for Java code. This avoids BOM issues that break JSON parsing.

A.4 — Include the full build-and-deploy sequence after every code change:

Copy# PowerShell (local)
cd C:\Users\jgmel\Documents\projects\JGlimsPlugin\JGlimsPlugin
.\gradlew.bat clean build
# Copy to server
scp build\libs\JGlimsPlugin-1.4.0.jar MinecraftServer:~/
Copy# SSH (server)
docker exec mc-crossplay rm -f /data/plugins/JGlimsPlugin*.jar
docker cp ~/JGlimsPlugin-1.4.0.jar mc-crossplay:/data/plugins/JGlimsPlugin.jar
docker restart mc-crossplay
# Verify
docker logs --since 2m mc-crossplay 2>&1 | grep -i jglims
docker logs --since 2m mc-crossplay 2>&1 | grep -i "exception\|error\|WARN" | head -20
docker exec mc-crossplay rcon-cli tps
A.5 — Always delete the old JAR before copying the new one (rm -f step above) to avoid the "ambiguous plugin name" warning.

A.6 — Use Adventure API everywhere: Component.text(), NamedTextColor, TextDecoration. No ChatColor, no § codes.

A.7 — All tunable values go through ConfigManager — never hard-code numbers in listeners.

A.8 — Every new Listener class must be registered in JGlimsPlugin.onEnable() with pm.registerEvents(...).

A.9 — All item data uses PersistentDataContainer with NamespacedKey under the jglimsplugin namespace.

A.10 — Legendary weapon abilities must be spectacular: massive particle effects, sounds, high damage numbers, screen shake via velocity, and visual flair. Two abilities per weapon (right-click and hold), both working in PvE and PvP (guild-aware).

A.11 — Provide step-by-step commands. Triple-check enum names, method signatures, and imports.

A.12 — Check the latest commit via https://api.github.com/repos/JGlims/JGlimsPlugin/commits?per_page=1 to confirm you're working against the current state.

A.13 — DO NOT FORGET: Three trident legendaries exist (#39–#41: Neptune's Fang, Tidecaller, Stormfork). They are Material.TRIDENT base. Fresh Animations: Player Extension is part of the client setup. Always reference the GitHub repo.

A.14 — The resource pack must be served from the server (via server.properties → resource-pack=<URL>, resource-pack-sha1=<hash>, require-resource-pack=true), NOT as a client-side local install. This is critical for Bedrock players via Geyser. The user explicitly rejected client-only installation.

SECTION B — CURRENT STATE OF THE PROJECT (v1.4.0)
B.1 — Architecture
The repository contains 36 Java source files + 2 resources (plugin.yml, config.yml), following a manager-listener pattern. JGlimsPlugin.java is the central orchestrator. Data persistence uses PDC for items/player data, and guilds.yml for guild state. 20 event listeners are registered in onEnable() (all 3 legendary listeners are now registered). The plugin loads in approximately 1170 ms, TPS is a solid 20.0, no errors in recent logs.

B.2 — Source File Inventory (36 files)
Root: JGlimsPlugin.java (17,964 bytes — main class, onEnable(), commands, accessor methods)

config/: ConfigManager.java (29,211 bytes — all config getters for every subsystem)

enchantments/: EnchantmentType.java (1,942 bytes — enum with 64 enchantments), CustomEnchantManager.java (8,853 bytes — PDC read/write, bidirectional conflict map), AnvilRecipeListener.java (32,629 bytes — anvil crafting, blocks legendary enchanting), EnchantmentEffectListener.java (69,495 bytes — all enchantment proc effects), SoulboundListener.java (7,961 bytes — death-save, lostsoul extraction)

blessings/: BlessingManager.java (10,296 bytes), BlessingListener.java (3,452 bytes)

guilds/: GuildManager.java (13,679 bytes — YAML persistence, commands), GuildListener.java (1,456 bytes)

mobs/: MobDifficultyManager.java (4,744 bytes — distance + biome scaling), BiomeMultipliers.java (3,080 bytes), BossEnhancer.java (3,345 bytes — Dragon/Elder Guardian/Wither/Warden stat boosts), KingMobManager.java (5,972 bytes — 1/100 golden elite mobs), BloodMoonManager.java (12,103 bytes — nightly event + Blood Moon King boss)

crafting/: RecipeManager.java (28,998 bytes — all custom recipes), VanillaRecipeRemover.java (588 bytes)

weapons/: BattleSwordManager.java (6,821), BattleAxeManager.java (8,303), BattleBowManager.java (4,871), BattleMaceManager.java (4,279), BattleShovelManager.java (8,451), BattlePickaxeManager.java (7,168), BattleTridentManager.java (4,737), BattleSpearManager.java (8,339), SickleManager.java (8,061), SpearManager.java (11,128), SuperToolManager.java (22,176), WeaponAbilityListener.java (81,738 — 20 weapon abilities), WeaponMasteryManager.java (9,803)

legendary/: LegendaryWeapon.java (10,450 — enum, 44 weapons), LegendaryWeaponManager.java (9,724 — item creation, PDC, lore), LegendaryAbilityListener.java (109,146 — 88 abilities), LegendaryLootListener.java (22,502 — boss drops, structure drops, dragon death chest)

utility/: BestBuddiesListener.java (11,609), DropRateListener.java (3,289), EnchantTransferListener.java (7,135), InventorySortListener.java (5,282), LootBoosterListener.java (8,991), PaleGardenFogTask.java (1,356), VillagerTradeListener.java (4,244)

resources/: plugin.yml (704 bytes — v1.4.0), config.yml (3,851 bytes — 158 lines)

B.3 — 64 Custom Enchantments (12 categories)
Sword (6): Vampirism (5), Bleed (3), Venomstrike (3), Lifesteal (3), Chain Lightning (3), Frostbite Blade (3) Axe (6): Berserker (5), Lumberjack (3), Cleave (3), Timber (3), Guillotine (3), Wrath (3) Pickaxe (6): Veinminer (3), Drill (3), Auto Smelt (1), Magnetism (1), Excavator (3), Prospector (3) Shovel (3): Harvester (3), Burial (3), Earthshatter (3) Hoe/Sickle (6): Green Thumb (1), Replenish (1), Harvesting Moon (3), Soul Harvest (3), Reaping Curse (3), Crop Reaper (1) Bow (5): Explosive Arrow (3), Homing (3), Rapidfire (3), Sniper (3), Frostbite Arrow (3) Crossbow (2): Thunderlord (5), Tidal Wave (3) Trident (6): Frostbite (3), Soul Reap (3), Blood Price (3), Reaper's Mark (3), Wither Touch (3), Tsunami (3) Armor (9): Swiftness (3), Vitality (3), Aqua Lungs (3), Night Vision (1), Fortification (3), Deflection (3), Swiftfoot (3), Dodge (3), Leaping (3) Elytra (4): Boost (3), Cushion (1), Glider (1), Stomp (3) Mace (5): Seismic Slam (3), Magnetize (3), Gravity Well (3), Momentum (3), Tremor (3) Spear (4): Impaling Thrust (3), Extended Reach (3), Skewering (3), Phantom Pierce (3) Universal (2): Soulbound (1), Best Buddies (1)

All stored as integers in PDC. Bidirectional conflict map enforced at anvil-apply time. Conflict pairs: Vampirism↔Lifesteal, Berserker↔Blood Price, Bleed↔Venomstrike, Wither Touch↔Chain Lightning, Explosive Arrow↔Homing, Veinminer↔Drill, Momentum↔Swiftfoot, Seismic Slam↔Magnetize, Impaling Thrust↔Extended Reach↔Skewering (triangle), Frostbite Blade↔Venomstrike, Wrath↔Cleave, Prospector↔Auto Smelt, Burial↔Harvester, Soul Harvest↔Green Thumb, Reaping Curse↔Replenish, Frostbite Arrow↔Explosive Arrow, Tsunami↔Frostbite, Tremor↔Gravity Well, Phantom Pierce↔Skewering.

B.4 — 10 Weapon Classes + Super Tools
Each class has a Manager that creates items with PDC tags, display names (Adventure API), lore, attribute modifiers, and durability. Weapon tiers: Battle (tier 1, iron-based), Super (tier 2, diamond/netherite-based, tier upgrades preserve enchantments), Legendary (tier 3, fixed stats, no enchants).

Super Tools come in Iron (+1.0 dmg), Diamond (+2.0 dmg), and Netherite (+2.0 dmg base + 2% per custom enchantment) tiers. Enchantments preserved during tier upgrades via copyAllEnchantments().

B.5 — 20 Weapon Abilities (non-legendary)
In WeaponAbilityListener.java (81 KB), 2 per weapon class, triggered by sneak + right-click or on-hit, with cooldowns, action-bar messages, particles, and sounds. Ender Dragon takes 30% reduced ability damage. Netherite super-tools get +2% ability damage per custom enchantment.

B.6 — 3 Blessings
C's Bless: +1 heart per use ×10. Ami's Bless: +2% damage per use ×10. La's Bless: +2% defense per use ×10. Reapplied on join/respawn.

B.7 — Mob Difficulty System
Distance-based scaling (7 brackets): 350b → 1.5×/1.3×, 700b → 2.0×/1.6×, 1000b → 2.5×/1.9×, 2000b → 3.0×/2.2×, 3000b → 3.5×/2.5×, 5000b → 4.0×/3.0×.

Biome multipliers — Overworld: Pale Garden 2.0×, Deep Dark 2.5×, Swamp 1.4×. Nether: Wastes 1.7×/1.7×, Soul Sand Valley 1.9×/1.9×, Crimson Forest 2.0×/2.0×, Warped Forest 2.0×/2.0×, Basalt Deltas 2.3×/2.3×. The End: 2.5× health / 2.0× damage.

Boss Enhancer: Ender Dragon 3.5×/3.0×, Elder Guardian 2.5×/1.8×, Wither 1.0×/1.0×, Warden 1.0×/1.0×.

Creeper reduction: 50% spawns cancelled.

B.8 — Other Systems
King Mobs: 1 per 100 spawns per EntityType, gold name, glowing, 10× health, 3× damage, drops 3–9 diamonds.

Blood Moon: 15% chance per night, Darkness effect, red particles, overworld monsters (except Creepers) get 1.5× health / 1.3× damage, double drops and double XP. Every 10th Blood Moon spawns a Blood Moon King boss (diamond armor, netherite sword, glow, no-burn, 20× health = 400 HP, 5× damage = 15 dmg, drops 5–15 diamonds + 1 netherite ingot + 500 XP + 10% chance for random LEGENDARY weapon).

Guilds: YAML-based persistence, max 10 members, friendly-fire disabled by default. Commands: /guild create/invite/join/leave/kick/disband/info/list.

Utilities: Inventory sort (shift-click empty slot in container), enchantment transfer (tool → book), loot booster (guaranteed enchanted book in dungeon chests, 5% hostile / 15% boss book drops), drop rate booster (trident 35% from Drowned, guardian shards, ghast tears, breeze charges), villager trades (50% price reduction, unlimited trades), Pale Garden fog (periodic Darkness effect), soulbound (keep 1 item on death, lostsoul extraction), Best Buddies (wolf with diamond armor: 95% DR, pacifist, permanent Regen II), weapon mastery (10 classes, up to +10% damage at 1000 kills), axe nerf (attack speed 0.5).

Ore Detect (super pickaxe ability): Diamond tier scans 8b radius for ores, Netherite 12b. Ancient Debris: Diamond 24b, Netherite 40b. Particles show for 200 ticks (10s).

Commands: /jglims (reload, stats, enchants, sort, mastery, legendary, help), /guild (create, invite, join, leave, kick, disband, info, list). All require jglims.admin (OP) for admin commands.

B.9 — Legendary Weapon System (Phase 8 — FULLY WIRED)
Three files are on GitHub and all registered in onEnable():

LegendaryWeapon.java — Enum: 44 weapons (24 LEGENDARY + 20 UNCOMMON). Each stores: id, displayName, baseMaterial (Material), baseDamage, customModelData (30001–30044), tier (LegendaryTier), textureSource, textureName, rightClickAbilityName, holdAbilityName, rightClickCooldown, holdCooldown. Has fromId(String) static lookup.

LegendaryWeaponManager.java — Creates ItemStacks. PDC keys: is_legendary_weapon (BYTE), legendary_id (STRING), legendary_tier (STRING), legendary_cooldown (LONG), legendary_damage (AttributeModifier key), legendary_speed (AttributeModifier key). Methods: isLegendary(), identify(), getTier(), createWeapon(). Sets unbreakable, no glint, CustomModelData, tier-colored bold name (DARK_PURPLE for LEGENDARY, GOLD for UNCOMMON). Detailed lore: tier tag, texture source, damage, attack speed, both abilities + cooldowns, unbreakable/no-enchant notes, trident auto-return hint. Hides default attributes. Attack speed by material: swords 1.6, axes 1.0, trident 1.1, hoes 4.0, mace 0.6.

LegendaryAbilityListener.java (109 KB) — Handles 88 abilities (44 RC + 44 hold). Cooldown system: Map<UUID, Map<String, Long>>. Hold-ability charge mechanic: 2-second hold with progress bar [■■■■■] → ACTIVATED! with beacon sound. Guild-aware enemy detection: getNearbyEnemies() skips same-guild players, skips Creative/Spectator. Passive buff trackers: bloodlustStacks, retributionDamageStored, soulCount, grudgeTargets, predatorMarkTarget, gemBarrierCharges, shadowStanceActive, crescentParryActive, emberShieldActive, thunderShieldActive, undyingRageActive, rebornReady, phaseShiftActive. Damage event handler applies offensive buffs (Bloodlust +2/stack, Grudge +20%, Predator's Mark +30%) and defensive buffs (Shadow Stance 25% dodge, Crescent parry 100% reflect, Ember Shield 4 fire dmg to attackers, Thunder Shield lightning to attackers, Gem Barrier hit absorb, Undying Rage survive-at-1HP).

LegendaryLootListener.java (22 KB) — Drop system:

Structure chests (LootGenerateEvent): LEGENDARY pool per structure (End City 25%, Stronghold 15%, Nether Fortress 12%, Bastion 12%, Ocean Monument 10%, Dungeon 8%, Temple 8%). UNCOMMON individual rolls per structure (each weapon has 1–2 sources with 8–20% chance).
Boss kills (guaranteed 1): Elder Guardian → Ocean's Rage or Aquatic Sacred Blade; Warden → Excalibur/Requiem/Chakram/Berserker's/Acidic/Black Iron (6 pool); Wither → Muramasa/Phoenix/Soul Collector/Amethyst Shuriken (4 pool).
Ender Dragon death chest: Places a chest at (0, highest, 0) with 6 random unique LEGENDARY weapons from a 14-weapon elite pool. Dramatic effects: Dragon Breath + End Rod particles, challenge toast sound. Server-wide announcement listing all 6 weapons.
Blood Moon King: 10% chance for any random LEGENDARY weapon.
All drops trigger server-wide announcement + Totem of Undying particles + challenge sound.
SECTION C — FULL LEGENDARY WEAPON TABLE (All 44)
LEGENDARY TIER (24) — CustomModelData 30001–30024
#	CMD	Weapon Name	Texture Source	Texture Name	Base Material	DMG	RC Ability (Cooldown)	Hold Ability (Cooldown)	Drop Source
1	30001	Ocean's Rage	Majestica	stormbringer	DIAMOND_SWORD	14	Tidal Crash — 6b AoE water blast, 15 dmg, knockback + Slowness (8s)	Riptide Surge — water launch + trail dmg (15s)	Elder Guardian
2	30002	Aquatic Sacred Blade	Majestica	aquantic_sacred_blade	DIAMOND_SWORD	13	Aqua Heal — +6 hearts + Conduit Power 30s (20s)	Depth Pressure — 10b Slowness III + Mining Fatigue (25s)	Elder Guardian
3	30003	True Excalibur	Majestica	excalibur	DIAMOND_SWORD	16	Holy Smite — Lightning + 20 dmg AoE 5b (10s)	Divine Shield — 5s invulnerability + Strength II (45s)	Warden
4	30004	Requiem of the Ninth Abyss	Majestica	requiem_of_hell	DIAMOND_SWORD	15	Soul Devour — drain 8 hearts, heal self (12s)	Abyss Gate — 3 wither skeletons 15s (60s)	Warden
5	30005	Royal Chakram	Majestica	royalchakram	DIAMOND_SWORD	12	Chakram Throw — bounces 4 targets, 8 dmg each (6s)	Spinning Shield — 3s projectile deflect + 50% melee DR (20s)	Warden
6	30006	Berserker's Greataxe	Fantasy 3D	berserkers_greataxe	DIAMOND_AXE	17	Berserker Slam — 8b AoE, 18 dmg + launch (10s)	Blood Rage — +50% dmg, +30% speed, −30% def 10s (30s)	Warden
7	30007	Acidic Cleaver	Fantasy 3D	treacherous_cleaver	DIAMOND_AXE	14	Acid Splash — 5b cone, 10 dmg + Poison III 8s (10s)	Corrosive Aura — 6b 1 heart/s for 8s (25s)	Warden
8	30008	Black Iron Greatsword	Fantasy 3D	black_iron_greatsword	DIAMOND_SWORD	15	Dark Slash — 10b line, 16 dmg, soul particles (8s)	Iron Fortress — Absorption IV + Resistance II 8s (30s)	Warden
9	30009	Muramasa	Majestica	muramasa	DIAMOND_SWORD	13	Crimson Flash — 8b dash, 12 dmg in path (6s)	Bloodlust — kills add +2 dmg stacks ×5, 15s window (20s)	Wither
10	30010	Phoenix's Grace	Fantasy 3D	gilded_phoenix_greataxe	DIAMOND_AXE	15	Phoenix Strike — Fire AoE 6b, 14 dmg + fire (10s)	Rebirth Flame — revive at 50% HP within 60s (120s)	Wither
11	30011	Soul Collector	Majestica	soul_collector	DIAMOND_SWORD	14	Soul Harvest — kill stores soul (+10 bonus next hit) (8s)	Spirit Army — release up to 5 souls as 6-dmg projectiles (30s)	Wither
12	30012	Amethyst Shuriken	Majestica	amethyst_shuriken	DIAMOND_SWORD	11	Shuriken Barrage — 5 fan projectiles, 7 dmg each (7s)	Shadow Step — teleport behind target + guaranteed crit (15s)	Wither
13	30013	Valhakyra	Majestica	valhakyra	DIAMOND_SWORD	15	Valkyrie Dive — leap 10b up, slam 20 dmg AoE (12s)	Wings of Valor — 8s slow-fall glide + Strength I (25s)	End City chest
14	30014	Windreaper	Fantasy 3D	windreaper	DIAMOND_SWORD	13	Gale Slash — 8b wind cone, 12 dmg + massive knockback (8s)	Cyclone — 4s tornado, pulls enemies, 4 dmg/s (20s)	End City chest
15	30015	Phantomguard	Fantasy 3D	phantomguard_greatsword	DIAMOND_SWORD	14	Spectral Cleave — through blocks, 10b line, 14 dmg (10s)	Phase Shift — 3s intangibility (35s)	Dragon death chest
16	30016	Moonlight	Fantasy 3D	moonlight	DIAMOND_SWORD	13	Lunar Beam — 15b ranged beam, 16 dmg (10s)	Eclipse — 6s Blindness + Weakness II to 15b enemies (30s)	Dragon death chest
17	30017	Zenith	Fantasy 3D	zenith	DIAMOND_SWORD	18	Final Judgment — 360° AoE 8b, 22 dmg (15s)	Ascension — 10s Flight + 50% dmg bonus (60s)	Dragon death chest
18	30018	Solstice	Fantasy 3D	solstice	DIAMOND_SWORD	14	Solar Flare — 10b fire AoE, 15 dmg + blindness 3s (10s)	Daybreak — clear negatives + Regen IV 6s (25s)	Stronghold chest
19	30019	Grand Claymore	Fantasy 3D	grand_claymore	DIAMOND_SWORD	16	Titan Swing — 180° arc 10b, 18 dmg + knockback (10s)	Colossus Stance — 6s no-knockback, +3 range, +40% dmg (30s)	Bastion chest
20	30020	Calamity Blade	Majestica	calamity_blade	DIAMOND_AXE	15	Cataclysm — 6b AoE, falling blocks, 14 dmg + slowness (12s)	Doomsday — 8s double damage, but 1 heart/s cost (35s)	Nether Fortress chest
21	30021	Dragon Sword	Fantasy 3D	dragon_sword	DIAMOND_SWORD	14	Dragon Breath — 8b fire cone, 12 dmg + breath cloud (10s)	Draconic Roar — 8b Fear + Weakness II 5s (25s)	Dragon death chest
22	30022	Talonbrand	Fantasy 3D	talonbrand	DIAMOND_SWORD	13	Talon Strike — triple-hit combo 3×8=24 dmg (8s)	Predator's Mark — target takes +30% from all 10s (20s)	Dungeon chest
23	30023	Emerald Greatcleaver	Fantasy 3D	emerald_greatcleaver	DIAMOND_AXE	16	Emerald Storm — 6b AoE shards, 14 dmg + Poison II (10s)	Gem Barrier — absorb next 3 hits, 15s window (40s)	Temple chest
24	30024	Demon's Blood Blade	Majestica	demons_blood_blade	DIAMOND_SWORD	15	Blood Rite — sacrifice 3 hearts, deal 25 dmg (8s)	Demonic Form — 10s +60% dmg, fire trail, −50% def (35s)	Blood Moon King
UNCOMMON LEGENDARY TIER (20) — CustomModelData 30025–30044
#	CMD	Weapon Name	Texture Source	Texture Name	Base Material	DMG	RC Ability (Cooldown)	Hold Ability (Cooldown)	Drop Locations (% chance)
25	30025	Nocturne	Fantasy 3D	nocturne	DIAMOND_SWORD	12	Shadow Slash — 6b line, 10 dmg + Blindness 2s (7s)	Night Cloak — 6s Invis + next melee +8 bonus (20s)	Mineshaft 15%, Dungeon 12%
26	30026	Gravescepter	Fantasy 3D	revenants_gravescepter	DIAMOND_SWORD	11	Grave Rise — 2 zombie allies 12s, 6 dmg each (15s)	Death's Grasp — root target 3s (18s)	Dungeon 12%, Ancient City 12%
27	30027	Lycanbane	Fantasy 3D	lycanbane	DIAMOND_SWORD	12	Silver Strike — 14 dmg + clears target buffs (8s)	Hunter's Sense — 10s Glowing on 20b entities (20s)	Woodland Mansion 15%
28	30028	Gloomsteel Katana	Fantasy 3D	gloomsteel_katana	DIAMOND_SWORD	11	Quick Draw — 5b dash + 10 dmg (5s)	Shadow Stance — 5s 25% dodge chance (18s)	Mineshaft 12%, Dungeon 12%
29	30029	Viridian Cleaver	Fantasy 3D	viridian_greataxe	DIAMOND_AXE	13	Verdant Slam — 5b AoE, 12 dmg + Slowness II 3s (8s)	Overgrowth — 1 heart/s heal 6s + root 2s (22s)	Jungle Temple 18%
30	30030	Crescent Edge	Fantasy 3D	crescent_greataxe	DIAMOND_AXE	12	Lunar Cleave — 180° 6b, 10 dmg (7s)	Crescent Guard — 4s parry: 100% dmg reflect (20s)	Pillager Outpost 15%
31	30031	Gravecleaver	Fantasy 3D	revenants_gravecleaver	DIAMOND_SWORD	12	Bone Shatter — 15 dmg + remove 2 armor 10s (10s)	Undying Rage — 8s survive lethal at 1 HP (45s)	Ancient City 12%
32	30032	Amethyst Greatblade	Fantasy 3D	amethyst_greatblade	DIAMOND_SWORD	11	Crystal Burst — 4b AoE shards, 9 dmg + Levitation 2s (8s)	Gem Resonance — 8s Strength I to allies in 10b (25s)	Trail Ruins 10%
33	30033	Flamberge	Fantasy 3D	flamberge	DIAMOND_SWORD	12	Flame Wave — 6b cone fire, 10 dmg + fire 4s (8s)	Ember Shield — 5s attackers take 4 fire dmg (18s)	Nether Fortress 10%, Ruined Portal 8%
34	30034	Crystal Frostblade	Fantasy 3D	crystal_frostblade	DIAMOND_SWORD	11	Frost Spike — 8b ice projectile, 10 dmg + Slowness III 3s (7s)	Permafrost — 5b AoE Slowness II + Mining Fatigue 6s (22s)	Igloo 20%, Snow village 15%
35	30035	Demonslayer	Fantasy 3D	demonslayers_greatsword	DIAMOND_SWORD	13	Holy Rend — +50% to Undead/Nether, 14/21 dmg (8s)	Purifying Aura — 6s Undead in 8b take 2 dmg/s (20s)	Nether Fortress 10%, Desert Temple 10%
36	30036	Vengeance	Fantasy 3D	vengeance_blade	DIAMOND_SWORD	10	Retribution — store dmg taken 8s, release ×1.5 AoE (12s)	Grudge Mark — +20% dmg to marked target 15s (15s)	Trial Chambers 12%, Dungeon 8%
37	30037	Oculus	Fantasy 3D	oculus	DIAMOND_SWORD	11	All-Seeing Strike — teleport to nearest enemy 10b + 12 dmg (8s)	Third Eye — 10s Glowing all entities in 30b (25s)	Stronghold 10%, End City 8%
38	30038	Ancient Greatslab	Fantasy 3D	ancient_greatslab	DIAMOND_SWORD	13	Seismic Slam — 6b AoE, 11 dmg + bounce 3b (9s)	Stone Skin — 6s Resistance II + no-knockback (22s)	Desert Temple 12%, Trail Ruins 12%
39	30039	Neptune's Fang	Majestica	frostaxe	TRIDENT	12	Riptide Slash — pierces 4 entities, 8 dmg each + Slowness II (7s)	Maelstrom — 6b water vortex, pulls enemies, 3 dmg/s 5s (22s)	Ocean Monument 15%, Shipwreck 10%
40	30040	Tidecaller	Majestica	aquantic_sacred_blade	TRIDENT	11	Tidal Spear — charged throw, 12 dmg + 6b knockback + Conduit Power 10s (8s)	Depth Ward — 8s Dolphin's Grace + Respiration (20s)	Ocean Monument 12%, Buried Treasure 15%
41	30041	Stormfork	Majestica	stormbringer	TRIDENT	13	Lightning Javelin — throw, lightning on impact, 14 dmg + 3b AoE (10s)	Thunder Shield — 6s attackers struck by lightning (25s)	Shipwreck 12%, Ocean Ruins 10%
42	30042	Jade Reaper	Majestica	jadehalberd	DIAMOND_HOE	12	Jade Crescent — 180° sweep 7b, 10 dmg + Poison I 4s (7s)	Emerald Harvest — 10s kills drop 1–3 emeralds (30s)	Jungle Temple 15%, Woodland Mansion 10%
43	30043	Vindicator	Fantasy 3D	vindicator	DIAMOND_AXE	11	Executioner's Chop — +1 dmg per target missing heart (max +10) (8s)	Rally Cry — 6s Speed I + Strength I to self + allies 8b (25s)	Pillager Outpost 15%, Woodland Mansion 12%
44	30044	Spider Fang	Fantasy 3D	spider_sword	DIAMOND_SWORD	10	Web Trap — cobweb projectile, root 3s + Poison II 4s (8s)	Wall Crawler — 8s wall-climb + Night Vision (20s)	Mineshaft 15%, Trial Chambers 10%
Texture distribution: Majestica 14 (32%), Fantasy 3D 30 (68%). Material distribution: DIAMOND_SWORD 31, DIAMOND_AXE 8, TRIDENT 3, DIAMOND_HOE 1, MACE 0.

SECTION D — RESOURCE PACK & TEXTURE SYSTEM (Phase 7 — CRITICAL OPEN ISSUE)
D.1 — The Problem (as of today)
Custom textures do NOT display in-game. Items show as default diamond swords/axes/tridents. This is the #1 blocking issue. Previous attempts failed because:

pack_format was set to 46 (1.21.4+) instead of 34 (1.21–1.21.1)
JSON files had BOM encoding or syntax errors from PowerShell Set-Content
Model files used 1.21.4+ minecraft:select overrides instead of CustomModelData overrides
The assets/minecraft/items/ folder was created (unused in 1.21.1, confuses the client)
The pack was tested client-side only — must be server-side via server.properties
D.2 — Correct Architecture for 1.21.1
pack.mcmeta must use "pack_format": 34.

CustomModelData overrides: Each base material needs an override file at assets/minecraft/models/item/<material>.json. For example, diamond_sword.json must include the vanilla parent model plus an overrides array mapping each custom_model_data integer to a model path. The model path points to a JSON file under assets/jglimsplugin/models/item/ (or assets/minecraft/models/item/) that references the texture.

Material → Override files needed:

diamond_sword.json — 31 weapons (all DIAMOND_SWORD legendaries)
diamond_axe.json — 8 weapons (all DIAMOND_AXE legendaries)
trident.json — 3 weapons (all TRIDENT legendaries)
diamond_hoe.json — 1 weapon (Jade Reaper)
Texture files: Each weapon needs a .json model file and .png texture file. Currently, 208 model JSONs and 327 PNG textures exist in the resource pack work directory, but many filenames don't match what the enum references.

D.3 — Known Texture Files in Resource Pack
From the PowerShell Get-ChildItem commands run during the conversation:

stormbringer.json — exists (used by Ocean's Rage CMD 30001 and Stormfork CMD 30041)
storms_edge.json — exists (not assigned to any current weapon)
aquantictrident.json — exists (filename mismatch: enum says aquantic_sacred_blade)
No files matching *ocean* — confirmed missing
No files matching *neptune* — confirmed missing
Critical: The texture name in the enum (e.g., stormbringer) must exactly match the filename of the model JSON (e.g., stormbringer.json). All 44 model JSON files must exist, and each must reference a texture PNG that exists.

D.4 — Server-Side Resource Pack Delivery
The resource pack must be served from the server, not installed client-side:

ZIP the pack (no nested folders — pack.mcmeta at the root of the zip):

Copycd C:\Users\jgmel\Documents\projects\JGlimsPlugin\resourcepack-work
Compress-Archive -Path "JGlimsResourcePack\*" -DestinationPath "JGlimsResourcePack.zip" -Force
Host the ZIP at a public URL. Options: upload to a file host, use GitHub Releases, or serve from the Oracle Cloud server.

Generate SHA-1 hash:

Copy(Get-FileHash "JGlimsResourcePack.zip" -Algorithm SHA1).Hash.ToLower()
Configure server.properties:

Copyresource-pack=https://<your-url>/JGlimsResourcePack.zip
resource-pack-sha1=<sha1-hash>
require-resource-pack=true
resource-pack-prompt=JGlims Custom Weapons Pack
Bedrock delivery: Copy the pack into Geyser's packs/ folder as a .mcpack. Use Rainbow (https://geysermc.org/wiki/other/rainbow/) to convert Java models to Bedrock geometry + geyser_mappings.json.

D.5 — Battle & Super Weapon Texture Assignments (still TODO)
Battle Weapons (tier 1) → Fantasy 3D Weapons CIT textures:

Battle Weapon	Fantasy 3D Texture	CustomModelData
Battle Sickle	Iron Hay Sickle	10001
Battle Sword	Uchigatana	10002
Battle Axe	Iron Battle Axe	10003
Battle Spear	Iron Halberd	10004
Battle Mace	Iron Mace	10005
Battle Shovel	Iron Sai	10006
Super Weapons (tier 2) → Blades of Majestica textures:

Super Weapon	Majestica Texture	CustomModelData
Super Spear	Heavenly Partisan	20001
Super Sword	Thousand Demon Daggers	20002
Super Axe	Hearthflame (Crimson Cleaver)	20003
Super Sickle	Jade Halberd (Crystal Frostscythe)	20004
Super Mace	Frost Axe (Black Iron Clobberer)	20005
Super Shovel	Sculk Scythe (Azure Scythe)	20006
NOTE: Battle/Super weapons do NOT yet have CustomModelData set in their Manager classes. This needs to be added in a future phase.

D.6 — Other Resource Packs (client-side or merged)
Pack	Purpose	Delivery
Spryzeen's Knight Armor	Medieval armor retextures	Server RP or Thunder .mcpack
Enchantment Outlines	Colored glint per enchant (normal weapons ONLY)	Server RP + Thunder
Recolourful Containers GUI + HUD	Custom container/inventory GUI	Server RP + Thunder
Recolourful Containers Hardcore Hearts	Custom heart textures	Server RP + Thunder
Story Mode Clouds	Stylized clouds	Server RP + Thunder
Fresh Food (with blessings)	Custom food textures	Server RP + Thunder
Fantasy 3D Weapons CIT	3D weapon models for battle tier	Server RP, models extracted
Blades of Majestica	3D models for super/legendary tiers	Server RP, models extracted
D.7 — Fresh Animations & Client Setup
For JotaGlims (Java) — Install Fabric + EMF + ETF + CIT Resewn. Resource pack load order (top = highest priority):

Fresh Animations: Player Extension (https://modrinth.com/resourcepack/fa-player-extension) — dynamic player idle/run/swim/climb animations (Java only, requires EMF+ETF)
Fresh Animations (https://modrinth.com/resourcepack/fresh-animations)
AL's mob revamp packs (+ FA variants): Zombie, Skeleton, Creeper, Enderman, Boss Rush, Piglin, Dungeons Boss Bars, Mob Weapons
Drodi's Blazes × Fresh Animations
Server resource pack (auto-downloaded)
For Gustafare5693 (Bedrock) — Auto-download from Geyser packs/ folder:

Fresh Animations Bedrock (https://mcpedl.com/fresh-animations-bedrock/) — mob animations only
AL revamp packs Bedrock ports
Server custom weapon Bedrock resource pack (generated by Rainbow)
SECTION E — KNOWN BUGS (Priority Order)
E.1 — Textures not displaying (CRITICAL): See Section D. CustomModelData is set in the plugin but resource pack is not correctly served. Must fix pack_format, ensure model JSONs exist for all 44 weapons, host ZIP server-side.

E.2 — Legendary abilities too weak and need more visual spectacle: All 88 ability methods exist but many need bigger particle counts, louder sounds, screen-shake via velocity knockback, and higher damage values. The user wants them to feel "spectacular and impressive."

E.3 — Sickle shows as "Battle Hoe": SickleManager.java display name is "Battle Hoe" instead of "Sickle." Fix: change display name, lore, recipe discovery message. Sickle damage is only 4, should be 7–8 for iron tier.

E.4 — Weapon abilities damage only mobs, not players: Most of the 20 non-legendary abilities in WeaponAbilityListener.java filter out Player entities. Fix: use entity.damage(amount, attackerPlayer) and add guild-friendly-fire check. The legendary system already handles this correctly — use as reference.

E.5 — Guild default membership: /guild info with no guild should show "You are not in a guild" instead of erroring.

E.6 — Enchantment glint on custom weapons: Battle and Super weapons may still show glint. Fix: meta.setEnchantmentGlintOverride(false) in all weapon manager createItem() methods. (Already done in LegendaryWeaponManager.)

E.7 — Duplicate JAR warning: Always docker exec mc-crossplay rm -f /data/plugins/JGlimsPlugin*.jar before docker cp.

SECTION F — PLANT TOTEMS & BOSS TOTEMS (Phase 9 — PLANNED)
F.1 — Plant Totems (Elemental Resistance)
Right-click to absorb (consumed). Permanently adds +10% resistance per type to player PDC, up to 3 absorptions (30% cap).

Totem	Resistance Type	Per Use	Max	Found In
Fern Totem	Fire damage	10%	30%	Nether Fortress, Bastion
Moss Totem	Poison/Wither	10%	30%	Jungle Temple, Swamp Huts
Cactus Totem	Projectile	10%	30%	Desert Temple, Pillager Outpost
Vine Totem	Fall damage	10%	30%	Jungle Temple, Mineshaft
Lily Totem	Drowning/Freeze	10%	30%	Ocean Monument, Shipwreck
Mushroom Totem	Explosion	10%	30%	Dungeon, Woodland Mansion
PDC: is_plant_totem (BYTE) + totem_type (STRING). Player PDC: totem_fern_level, totem_moss_level, etc. (INTEGER, 0–3).

Implementation: PlantTotemManager.java (creates totem items, paper base with CustomModelData), PlantTotemListener.java (right-click consume, applies PDC resistance, damage reduction in EntityDamageEvent).

F.2 — Boss Totems (Unique Passive Abilities)
Right-click to consume (permanent). Boss drop only. One per player per type.

Boss Totem	Drop Source	Passive Effect	Texture Theme
Guardian's Blessing	Elder Guardian	+50% swim speed + Respiration III permanent	Prismarine Ore
Wither's Immunity	Wither	Complete Wither effect immunity	Coal/Obsidian Ore
Warden's Silence	Warden	Silent walking (Sculk sensors ignore you)	Deepslate Ore
Dragon's Gaze	Ender Dragon	Permanent Night Vision + Endermen don't aggro when looked at	End Stone Ore
PDC: boss_totem_guardian, boss_totem_wither, boss_totem_warden, boss_totem_dragon (BYTE). Reapplied on join/respawn via BossTotemListener.

SECTION G — WELCOME BOOK (Phase 10 — PLANNED)
Written Book in Portuguese Brazil (PT-BR) given on first join. Replacement command: /jglims livro. Created via BookMeta with Adventure Components.

15 pages: Bem-vindo (intro), Sistema de Armas (normal/battle/super/legendary + damage table), Receitas de Crafting, Super Ferramentas (tier upgrades), Armas Lendárias (locations/bosses/drops), Encantamentos Customizados (all 64), Encantamentos continuação (conflicts/max levels), Bênçãos (C's/Ami's/La's Bless), Totems (plant + boss), Guildas, Eventos (Blood Moon/minibosses), Dificuldade dos Mobs (distance/biome/dimension/King Mobs), Utilidades, Comandos (/jglims + /guild), Créditos e links.

Implementation: WelcomeBookManager.java in book/ package. First-join detection via player PDC welcome_book_given (BYTE). /jglims livro gives replacement copy.

SECTION H — CREATIVE MENU / CATALOG (Phase 11 — PLANNED)
/jglims catalog → virtual chest GUI. 8 pages: Battle Weapons, Super Weapons, Legendary Weapons (2 pages for 44 items), Custom Enchantment Books (2 pages), Blessings, Totems. Glass pane borders, arrow navigation items, category selector bottom row.

CatalogListener.java cancels all clicks unless OP + Creative mode. Display-only copies with "CATALOG ITEM" lore tag (PDC is_catalog_item BYTE — these items cannot be used).

Implementation: CatalogManager.java (builds inventory GUIs), CatalogListener.java (handles clicks, page navigation).

SECTION I — MOB DIFFICULTY REBALANCE (Phase 12 — PLANNED)
Post-legendary testing adjustments. If the game feels too easy with legendary weapons:

Bump baseline-health-multiplier to 1.5 and baseline-damage-multiplier to 1.3
Add 7500+ block bracket: 5.0×/4.0×
Add "Legendary Mob" tier: 1/500 spawns, 20× HP, 5× DMG, guaranteed diamond drop + plant totem chance
Bosses scale with player gear: +10% HP per legendary weapon in player inventory
SECTION J — EVENTS & MINIBOSSES (Phase 13 — PLANNED)
Three new world events that trigger randomly:

Wither Storm (rare Nether event): Massive wither-skeleton miniboss with 500 HP, spawns wither skeleton waves. Drops: random LEGENDARY weapon + 10 netherite scraps.

Ocean Siege (Guardian attack near monuments): Elder Guardian becomes empowered, spawns guardian waves. Water turns dark. Drops: guaranteed Ocean's Rage or Aquatic Sacred Blade + trident legendaries.

End Rift (End mobs in Overworld): Portal opens in Overworld, Endermen + Shulkers pour out, Rift Guardian boss (300 HP). Drops: random End City pool legendary + Dragon's Gaze totem chance.

Blood Moon King already exists and drops legendaries (10% chance). Could be expanded with minion waves.

Each event has: server-wide announcement, dramatic visual effects (custom sky color via resource pack, particles, sounds), wave-based mob spawns, and a final boss with guaranteed legendary drops.

SECTION K — SURVIVAL MODE SWITCH (Phase 14 — PLANNED)
Lock admin commands behind proper permissions, disable debug logging, set default difficulty, finalize all config values, remove test commands, ensure graceful error handling everywhere.

SECTION L — CONFIG.YML COMPLETE REFERENCE (158 lines)
Copymob-difficulty:
  enabled: true
  baseline-health-multiplier: 1.0
  baseline-damage-multiplier: 1.0
  distance: { 350: {h:1.5,d:1.3}, 700: {h:2.0,d:1.6}, 1000: {h:2.5,d:1.9}, 2000: {h:3.0,d:2.2}, 3000: {h:3.5,d:2.5}, 5000: {h:4.0,d:3.0} }
  biome: { pale-garden:2.0, deep-dark:2.5, swamp:1.4, nether-wastes:{h:1.7,d:1.7}, soul-sand-valley:{h:1.9,d:1.9}, crimson-forest:{h:2.0,d:2.0}, warped-forest:{h:2.0,d:2.0}, basalt-deltas:{h:2.3,d:2.3}, end:{h:2.5,d:2.0} }
boss-enhancer: { ender-dragon:{h:3.5,d:3.0}, wither:{h:1.0,d:1.0}, warden:{h:1.0,d:1.0}, elder-guardian:{h:2.5,d:1.8} }
creeper-reduction: { enabled:true, cancel-chance:0.5 }
pale-garden-fog: { enabled:true, check-interval:40 }
loot-booster: { enabled:true, guardian-shards 1-3, elder 3-5, ghast tears 1-2, echo-shard-chance:0.40 }
mob-book-drops: { enabled:true, hostile-chance:0.05, boss-custom:0.15, looting bonus:0.02/0.05 }
blessings: { c-bless:{max:10,heal:1}, ami-bless:{max:10,dmg:2%}, la-bless:{max:10,def:2%} }
anvil: { remove-too-expensive:true, xp-cost-reduction:0.5 }
toggles: { inventory-sort:true, enchant-transfer:true, sickle:true, battle-axe/bow/mace/shovel:true, super-tools:true, drop-rate-booster:true, spear:true }
drop-rate-booster: { trident:0.35, breeze-wind-charge 2-5 }
villager-trades: { enabled:true, price-reduction:0.50, disable-trade-locking:true }
king-mob: { enabled:true, spawns-per-king:100, health:10.0, damage:3.0, diamonds 3-9 }
axe-nerf: { enabled:true, attack-speed:0.5 }
weapon-mastery: { enabled:true, max-kills:1000, max-bonus:10.0% }
blood-moon: { enabled:true, check-interval:100, chance:0.15, mob-health:1.5, mob-damage:1.3, boss-every:10, boss-health:20.0, boss-damage:5.0, boss-diamonds 5-15, double-drops:true }
guilds: { enabled:true, max-members:10, friendly-fire:false }
best-buddies: { dog-armor-damage-reduction:0.95 }
super-tools: { iron-bonus:1.0, diamond:2.0, netherite:2.0, per-enchant:2.0% }
ore-detect: { radius diamond:8, netherite:12, ancient-debris diamond:24, netherite:40, duration:200 ticks }
SECTION M — PROBLEMS SOLVED ACROSS ALL SESSIONS (Lessons Learned)
RecipeManager super-recipe bug: Duplicate NamespacedKeys silently failing → use unique key per recipe.
BestBuddiesListener never registered: Always check onEnable() for new listeners.
King Mob spawn rate too high: Set to 1/100 spawns.
Inconsistent weapon lore: Standardized to Adventure API gold/gray/green scheme.
Weapon ability damage too low for scaled mobs: Buffed all 20 abilities.
SpearManager using legacy ChatColor: Migrated to Adventure API.
MobDifficultyManager hard-coding multipliers: Now ConfigManager-driven.
BiomeMultipliers hard-coding values: Now initWithConfig().
12 new enchantments added (52→64) with conflicts registered.
Legacy § codes in CustomEnchantManager: Migrated to Adventure Component.text().
Duplicate JAR warning: Resolved by deleting old file before copy.
LegendaryAbilityListener compilation errors: Fixed in commits a5d0209, 0c2accc3, 743a21e6.
Resource pack pack_format wrong: Must use 34 for 1.21.1, not 46.
JSON files with BOM: PowerShell Set-Content adds BOM; use [System.IO.File]::WriteAllText() with UTF8Encoding($false).
1.21.4+ item_model system used on 1.21.1: Must use CustomModelData overrides instead.
assets/minecraft/items/ folder confusion: Delete it — only used in 1.21.4+.
LegendaryWeapon enum returned String for baseMaterial: Fixed to return Material directly.
Resource pack tested client-side only: Must be server-side for Bedrock support.
SECTION N — REPOSITORY STRUCTURE (Current + Planned)
JGlimsPlugin/
├── .gitignore
├── README.md                                      ← Contains project summary v3.0
├── build.gradle                                   ← Paper 1.21.1, Java 21
├── settings.gradle
├── gradlew / gradlew.bat
├── gradle/wrapper/
└── src/main/
    ├── java/com/jglims/plugin/
    │   ├── JGlimsPlugin.java                      # Main class (17.9 KB)
    │   ├── config/
    │   │   └── ConfigManager.java                 # All config getters (29.2 KB)
    │   ├── enchantments/
    │   │   ├── EnchantmentType.java               # 64 enchantments enum (1.9 KB)
    │   │   ├── CustomEnchantManager.java           # PDC read/write (8.9 KB)
    │   │   ├── AnvilRecipeListener.java            # Anvil crafting (32.6 KB)
    │   │   ├── EnchantmentEffectListener.java      # All effects (69.5 KB)
    │   │   └── SoulboundListener.java              # Death-save (8.0 KB)
    │   ├── blessings/
    │   │   ├── BlessingManager.java                # (10.3 KB)
    │   │   └── BlessingListener.java               # (3.5 KB)
    │   ├── guilds/
    │   │   ├── GuildManager.java                   # YAML persistence (13.7 KB)
    │   │   └── GuildListener.java                  # (1.5 KB)
    │   ├── mobs/
    │   │   ├── MobDifficultyManager.java           # Distance+biome (4.7 KB)
    │   │   ├── BiomeMultipliers.java               # (3.1 KB)
    │   │   ├── BossEnhancer.java                   # (3.3 KB)
    │   │   ├── KingMobManager.java                 # (6.0 KB)
    │   │   └── BloodMoonManager.java               # (12.1 KB)
    │   ├── crafting/
    │   │   ├── RecipeManager.java                  # (29.0 KB)
    │   │   └── VanillaRecipeRemover.java           # (0.6 KB)
    │   ├── weapons/
    │   │   ├── BattleSwordManager.java             # (6.8 KB)
    │   │   ├── BattleAxeManager.java               # (8.3 KB)
    │   │   ├── BattleBowManager.java               # (4.9 KB)
    │   │   ├── BattleMaceManager.java              # (4.3 KB)
    │   │   ├── BattleShovelManager.java            # (8.5 KB)
    │   │   ├── BattlePickaxeManager.java           # (7.2 KB)
    │   │   ├── BattleTridentManager.java           # (4.7 KB)
    │   │   ├── BattleSpearManager.java             # (8.3 KB)
    │   │   ├── SickleManager.java                  # (8.1 KB)
    │   │   ├── SpearManager.java                   # (11.1 KB)
    │   │   ├── SuperToolManager.java               # (22.2 KB)
    │   │   ├── WeaponAbilityListener.java          # 20 abilities (81.7 KB)
    │   │   └── WeaponMasteryManager.java           # (9.8 KB)
    │   ├── legendary/                              # ✅ FULLY WIRED (Phase 8)
    │   │   ├── LegendaryWeapon.java                # 44 weapons enum (10.5 KB)
    │   │   ├── LegendaryWeaponManager.java         # Item creation (9.7 KB)
    │   │   ├── LegendaryAbilityListener.java       # 88 abilities (109.1 KB)
    │   │   └── LegendaryLootListener.java          # Drop system (22.5 KB)
    │   ├── totems/                                 # 🔴 PLANNED (Phase 9)
    │   │   ├── PlantTotemManager.java
    │   │   ├── PlantTotemListener.java
    │   │   ├── BossTotemManager.java
    │   │   └── BossTotemListener.java
    │   ├── catalog/                                # 🔴 PLANNED (Phase 11)
    │   │   ├── CatalogManager.java
    │   │   └── CatalogListener.java
    │   ├── book/                                   # 🔴 PLANNED (Phase 10)
    │   │   └── WelcomeBookManager.java
    │   └── utility/
    │       ├── BestBuddiesListener.java            # (11.6 KB)
    │       ├── DropRateListener.java               # (3.3 KB)
    │       ├── EnchantTransferListener.java        # (7.1 KB)
    │       ├── InventorySortListener.java          # (5.3 KB)
    │       ├── LootBoosterListener.java            # (9.0 KB)
    │       ├── PaleGardenFogTask.java              # (1.4 KB)
    │       └── VillagerTradeListener.java          # (4.2 KB)
    └── resources/
        ├── plugin.yml                              # v1.4.0 (704 bytes)
        └── config.yml                              # (3.9 KB, 158 lines)
Resource pack work directory (separate from plugin source):

C:\Users\jgmel\Documents\projects\JGlimsPlugin\resourcepack-work\
└── JGlimsResourcePack/
    ├── pack.mcmeta                                # pack_format: 34
    ├── assets/
    │   └── minecraft/
    │       └── models/
    │           └── item/
    │               ├── diamond_sword.json          # Overrides for 31 sword CMD
    │               ├── diamond_axe.json            # Overrides for 8 axe CMD
    │               ├── trident.json                # Overrides for 3 trident CMD
    │               ├── diamond_hoe.json            # Override for 1 hoe CMD
    │               ├── stormbringer.json            # ✅ EXISTS
    │               ├── storms_edge.json             # ✅ EXISTS (unassigned)
    │               ├── aquantictrident.json          # ✅ EXISTS (filename mismatch)
    │               └── ... (208 total model JSONs, 327 PNGs)
SECTION O — IMAGE URLS FROM ALL SESSIONS
https://www.genspark.ai/api/files/s/nhRKbJOw, https://www.genspark.ai/api/files/s/rwop3T2K, https://www.genspark.ai/api/files/s/wm26PNPW, https://www.genspark.ai/api/files/s/Mv2yS8Iu, https://www.genspark.ai/api/files/s/PlrWlWpI, https://www.genspark.ai/api/files/s/XGtWb2G8, https://www.genspark.ai/api/files/s/tZUP7HEg, https://www.genspark.ai/api/files/s/Ch1hMkh3, https://www.genspark.ai/api/files/s/GRfk6sBT, https://www.genspark.ai/api/files/s/TOnhJaKI, https://www.genspark.ai/api/files/s/HYmXvxWz, https://www.genspark.ai/api/files/s/Y9U308nE, https://www.genspark.ai/api/files/s/hCCnZru2, https://www.genspark.ai/api/files/s/4Agrpixv, https://www.genspark.ai/api/files/s/z0Wxs9RY, https://www.genspark.ai/api/files/s/lhLtipOJ, https://www.genspark.ai/api/files/s/Qa56tF16, https://www.genspark.ai/api/files/s/LpF08MnP, https://www.genspark.ai/api/files/s/76iabqHL, https://www.genspark.ai/api/files/s/8G2FILbi, https://www.genspark.ai/api/files/s/PEucRdRp, https://www.genspark.ai/api/files/s/2xlCstun, https://www.genspark.ai/api/files/s/wXViEhTo, https://www.genspark.ai/api/files/s/UO34kssN, https://www.genspark.ai/api/files/s/K5Jv5W2t

SECTION P — KEY REFERENCE LINKS
What	URL
GitHub Repository	https://github.com/JGlims/JGlimsPlugin
Latest Commit (288f296d)	https://github.com/JGlims/JGlimsPlugin/commit/288f296d0036d154f0cc164a9125ac27cefe58b2
API Commits Endpoint	https://api.github.com/repos/JGlims/JGlimsPlugin/commits?per_page=1
API Tree Endpoint	https://api.github.com/repos/JGlims/JGlimsPlugin/git/trees/main?recursive=1
Raw File Base	https://raw.githubusercontent.com/JGlims/JGlimsPlugin/main/
Paper API JavaDocs	https://jd.papermc.io/paper/1.21.1/
Pack Format Wiki	https://minecraft.wiki/w/Pack_format
Geyser Docs	https://geysermc.org/wiki/geyser/
Rainbow (Bedrock converter)	https://geysermc.org/wiki/other/rainbow/
Fresh Animations	https://modrinth.com/resourcepack/fresh-animations
FA Player Extension	https://modrinth.com/resourcepack/fa-player-extension
FA Bedrock	https://mcpedl.com/fresh-animations-bedrock/
SECTION Q — PHASE ROADMAP (COMPLETE)
Phase	Name	Status	Priority
5	Bug Fixes (Sickle name, PvP abilities, guild info, glint, duplicate JAR)	Partially done	HIGH
6	Performance Profiling (TPS under load, memory, split large files)	Not started	MEDIUM
7	Resource Packs & Custom Textures (server-side RP, all 44 models, Bedrock pack)	FAILED — MUST RESTART	CRITICAL
8	Legendary Weapons (44 weapons, 88 abilities, loot drops, dragon death chest)	DONE — WIRED & COMPILED	DONE
9	Plant Totems (6 elemental) + Boss Totems (4 unique passives)	Not started	HIGH
10	Welcome Book (PT-BR, 15 pages, first-join)	Not started	MEDIUM
11	Creative Catalog GUI (8 pages, all items/enchants/totems)	Not started	MEDIUM
12	Mob Difficulty Rebalance (post-legendary tuning)	Not started	LOW
13	Events & Minibosses (Wither Storm, Ocean Siege, End Rift)	Not started	HIGH
14	Survival Mode Switch (lock commands, finalize config)	Not started	LOW
SECTION R — WHAT THE NEXT SESSION MUST DO FIRST
The immediate priority is Phase 7 (Resource Pack) done correctly this time:

Audit all 44 texture filenames: Run Get-ChildItem in the resource pack models directory and cross-reference every textureName in the enum against existing .json model files. Log every mismatch.

Fix or create all 44 model JSON files: Each must have correct parent, textures, and display blocks. The filename must match the enum's textureName exactly.

Create override files: diamond_sword.json, diamond_axe.json, trident.json, diamond_hoe.json with proper overrides arrays sorted by ascending custom_model_data value.

Set pack.mcmeta to pack_format: 34.

Delete assets/minecraft/items/ if it exists.

Write all files with no-BOM UTF-8.

ZIP, generate SHA-1, upload, configure server.properties.

Test server-side: Join the server, verify auto-download prompt, check that /jglims legendary oceans_rage shows the 3D Stormbringer model.

Bedrock conversion: Use Rainbow to generate .mcpack + geyser_mappings.json, place in Geyser packs/.

Only after textures work should the next session proceed to Phase 5 bug fixes, then Phase 9 (totems), etc.
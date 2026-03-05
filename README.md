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
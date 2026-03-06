JGLIMSPLUGIN — DEFINITIVE PROJECT SUMMARY v8.0
Date compiled: 2026-03-06 Current version: 1.4.0 (plugin.yml and build.gradle confirmed) Target version: 3.0.0 (after all planned phases including Abyss dimension) Author: JGlims (jg.melo.lima2005@gmail.com) Players: JotaGlims (Java), Gustafare5693 (Bedrock) Repository: https://github.com/JGlims/JGlimsPlugin Latest commit: a159897a80460940c55e5af90e1cdb972adda7aa — "current state" — 2026-03-06T03:01:03Z Previous commits (recent): 8b038aef "." (2026-03-05 18:29), 288f296d "." (18:03), 743a21e6 "." (08:14), 0c2accc3 "new fixes" (07:11), 4e217caa "new readme" (06:58), a5d02096 "red lines fix" (06:39) API: Paper 1.21.11 (io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT), Java 21, Gradle 8.x (wrapper included), Kyori Adventure (native in Paper — no shading) Server: Docker container mc-crossplay on Oracle Cloud, IP 144.22.198.184, Java port 25565, Bedrock port 19132 (GeyserMC + Floodgate v2.2.5-SNAPSHOT + SkinsRestorer) SSH alias: MinecraftServer (user ubuntu, key file C:\Users\jgmel\Documents\projects\server_minecraft\ssh-key-2026-02-25.key) Local dev path: C:\Users\jgmel\Documents\projects\JGlimsPlugin\JGlimsPlugin\ Resource pack work dir: C:\Users\jgmel\Documents\projects\JGlimsPlugin\resourcepack-work\JGlimsResourcePack\ Output JAR: build/libs/JGlimsPlugin-1.4.0.jar (349,791 bytes at last build)

SECTION A — MANDATORY RULES FOR EVERY CHAT SESSION
Before doing anything, the assistant must:

A.1 — Verify the latest commit via https://api.github.com/repos/JGlims/JGlimsPlugin/commits?per_page=1 to confirm you're working against the current state. Never work against stale code.

A.2 — Fetch the latest state of every file it plans to edit from https://raw.githubusercontent.com/JGlims/JGlimsPlugin/main/<path>. Never assume file contents — always crawler the raw GitHub URL first.

A.3 — Supply complete file replacements with the exact path (e.g., src/main/java/com/jglims/plugin/legendary/LegendaryWeaponManager.java). Never send partial diffs or snippets. If a file is too large for one message, split across multiple messages but always send the COMPLETE file.

A.4 — Write files in PowerShell using [System.IO.File]::WriteAllText($path, $content, (New-Object System.Text.UTF8Encoding $false)) and heredocs @" … "@ for Java code. This avoids BOM issues that break JSON parsing.

A.5 — Include the full build-and-deploy sequence after every code change:

Copy# PowerShell (local)
$ErrorActionPreference = "Continue"
$PSNativeCommandUseErrorActionPreference = $false
cd C:\Users\jgmel\Documents\projects\JGlimsPlugin\JGlimsPlugin
cmd /c ".\gradlew.bat clean build --no-daemon 2>&1"
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
A.6 — Always delete the old JAR before copying the new one (rm -f step above) to avoid the "ambiguous plugin name" warning.

A.7 — Use Adventure API everywhere: Component.text(), NamedTextColor, TextDecoration. No ChatColor, no § codes.

A.8 — All tunable values go through ConfigManager — never hard-code numbers in listeners.

A.9 — Every new Listener class must be registered in JGlimsPlugin.onEnable() with pm.registerEvents(...).

A.10 — All item data uses PersistentDataContainer with NamespacedKey under the jglimsplugin namespace.

A.11 — Legendary weapon abilities must be spectacular: massive particle effects, sounds, high damage numbers, screen shake via velocity, and visual flair. Two abilities per weapon (right-click and crouch+right-click), both working in PvE and PvP (guild-aware). Particle counts scale by tier: COMMON 30-50, RARE 50-100, EPIC 100-150, MYTHIC 200+.

A.12 — Provide step-by-step commands. Triple-check enum names, method signatures, and imports.

A.13 — DO NOT FORGET: Three trident legendaries exist (#39–#41: Neptune's Fang, Tidecaller, Stormfork). They are Material.TRIDENT base. Fresh Animations: Player Extension is part of the client setup. Always reference the GitHub repo.

A.14 — The resource pack must be served from the server (via server.properties → resource-pack=<URL>, resource-pack-sha1=<hash>, require-resource-pack=true), NOT as a client-side local install. This is critical for Bedrock players via Geyser.

A.15 — For resource pack changes on 1.21.11: set pack_format to 75, use the assets/minecraft/items/*.json select/custom_model_data system with string-based matching, keep models in assets/minecraft/models/item/, textures in assets/minecraft/textures/item/, ZIP with forward slashes and pack.mcmeta at root. The old overrides system is dead as of 1.21.4.

A.16 — When building in PowerShell, use $ErrorActionPreference = "Continue" and $PSNativeCommandUseErrorActionPreference = $false to prevent Java's deprecated-API warnings from aborting the script. Run Gradle via cmd /c ".\gradlew.bat clean build --no-daemon 2>&1".

A.17 — When running sed through PowerShell → SSH → Docker → bash, minimize quoting layers. Remove outer single quotes around the sed expression and let PowerShell expand variables directly.

A.18 — The LegendaryAbilityListener.java is 109+ KB. The crawler tool may only return partial content. When rewriting this file, the assistant MUST reconstruct ALL 88+ ability methods (one primary + one alternate per weapon) using the weapon table in Section C as the authoritative source. Never leave ability methods incomplete or missing. If the file is too large for one message, split across multiple messages.

SECTION B — CURRENT STATE OF THE PROJECT (v1.4.0)
B.1 — Architecture
The repository contains 42 Java source files + 2 resources (plugin.yml, config.yml) + 1 PowerShell script (phase7.ps1), following a manager-listener pattern. JGlimsPlugin.java is the central orchestrator. Data persistence uses PDC for items/player data, and guilds.yml for guild state. 20+ event listeners are registered in onEnable() (all legendary listeners are now registered). The plugin loads in approximately 1170 ms, TPS is a solid 20.0, no errors in recent logs.

B.2 — Source File Inventory (42 Java files + 2 resources + 1 script)
Root: JGlimsPlugin.java (17,964 bytes — main class, onEnable(), commands, accessor methods)

config/: ConfigManager.java (29,211 bytes — all config getters for every subsystem)

enchantments/: EnchantmentType.java (1,942 bytes — enum with 64 enchantments), CustomEnchantManager.java (8,853 bytes — PDC read/write, bidirectional conflict map), AnvilRecipeListener.java (32,629 bytes — anvil crafting, blocks legendary enchanting), EnchantmentEffectListener.java (69,495 bytes — all enchantment proc effects), SoulboundListener.java (7,961 bytes — death-save, lostsoul extraction)

blessings/: BlessingManager.java (10,296 bytes), BlessingListener.java (3,452 bytes)

guilds/: GuildManager.java (13,679 bytes — YAML persistence, commands), GuildListener.java (1,456 bytes)

mobs/: MobDifficultyManager.java (4,744 bytes — distance + biome scaling), BiomeMultipliers.java (3,080 bytes), BossEnhancer.java (3,345 bytes — Dragon/Elder Guardian/Wither/Warden stat boosts), KingMobManager.java (5,972 bytes — 1/100 golden elite mobs), BloodMoonManager.java (12,103 bytes — nightly event + Blood Moon King boss)

crafting/: RecipeManager.java (28,998 bytes — all custom recipes), VanillaRecipeRemover.java (588 bytes)

weapons/: BattleSwordManager.java (6,821), BattleAxeManager.java (8,303), BattleBowManager.java (4,871), BattleMaceManager.java (4,279), BattleShovelManager.java (8,451), BattlePickaxeManager.java (7,168), BattleTridentManager.java (4,737), BattleSpearManager.java (8,339), SickleManager.java (8,061), SpearManager.java (11,128), SuperToolManager.java (22,176), WeaponAbilityListener.java (81,738 — 20 weapon abilities), WeaponMasteryManager.java (9,803)

legendary/: LegendaryWeapon.java (10,450 — enum, 44 weapons), LegendaryWeaponManager.java (10,606 — item creation, PDC, lore, string-based CustomModelData), LegendaryAbilityListener.java (109,146 — 88 abilities), LegendaryLootListener.java (22,502 — boss drops, structure drops, dragon death chest)

utility/: BestBuddiesListener.java (11,609), DropRateListener.java (3,289), EnchantTransferListener.java (7,135), InventorySortListener.java (5,282), LootBoosterListener.java (8,991), PaleGardenFogTask.java (1,356), VillagerTradeListener.java (4,244)

resources/: plugin.yml (704 bytes — v1.4.0), config.yml (3,851 bytes — 158 lines, header still says "v1.3.0" — cosmetic mismatch only)

root-level scripts: phase7.ps1 (8,283 bytes — resource-pack build automation, committed to repo)

B.3 — 64 Custom Enchantments (12 categories)
Sword (6): Vampirism (5), Bleed (3), Venomstrike (3), Lifesteal (3), Chain Lightning (3), Frostbite Blade (3). Axe (6): Berserker (5), Lumberjack (3), Cleave (3), Timber (3), Guillotine (3), Wrath (3). Pickaxe (6): Veinminer (3), Drill (3), Auto Smelt (1), Magnetism (1), Excavator (3), Prospector (3). Shovel (3): Harvester (3), Burial (3), Earthshatter (3). Hoe/Sickle (6): Green Thumb (1), Replenish (1), Harvesting Moon (3), Soul Harvest (3), Reaping Curse (3), Crop Reaper (1). Bow (5): Explosive Arrow (3), Homing (3), Rapidfire (3), Sniper (3), Frostbite Arrow (3). Crossbow (2): Thunderlord (5), Tidal Wave (3). Trident (6): Frostbite (3), Soul Reap (3), Blood Price (3), Reaper's Mark (3), Wither Touch (3), Tsunami (3). Armor (9): Swiftness (3), Vitality (3), Aqua Lungs (3), Night Vision (1), Fortification (3), Deflection (3), Swiftfoot (3), Dodge (3), Leaping (3). Elytra (4): Boost (3), Cushion (1), Glider (1), Stomp (3). Mace (5): Seismic Slam (3), Magnetize (3), Gravity Well (3), Momentum (3), Tremor (3). Spear (4): Impaling Thrust (3), Extended Reach (3), Skewering (3), Phantom Pierce (3). Universal (2): Soulbound (1), Best Buddies (1).

All stored as integers in PDC. Bidirectional conflict map enforced at anvil-apply time. Conflict pairs: Vampirism↔Lifesteal, Berserker↔Blood Price, Bleed↔Venomstrike, Wither Touch↔Chain Lightning, Explosive Arrow↔Homing, Veinminer↔Drill, Momentum↔Swiftfoot, Seismic Slam↔Magnetize, Impaling Thrust↔Extended Reach↔Skewering (triangle), Frostbite Blade↔Venomstrike, Wrath↔Cleave, Prospector↔Auto Smelt, Burial↔Harvester, Soul Harvest↔Green Thumb, Reaping Curse↔Replenish, Frostbite Arrow↔Explosive Arrow, Tsunami↔Frostbite, Tremor↔Gravity Well, Phantom Pierce↔Skewering.

B.4 — 10 Weapon Classes + Super Tools
Each class has a Manager that creates items with PDC tags, display names (Adventure API), lore, attribute modifiers, and durability. Weapon tiers: Battle (tier 1, iron-based), Super (tier 2, diamond/netherite-based, tier upgrades preserve enchantments), Legendary (tier 3+, fixed stats, no enchants).

Super Tools come in Iron (+1.0 dmg), Diamond (+2.0 dmg), and Netherite (+2.0 dmg base + 2% per custom enchantment) tiers. Enchantments preserved during tier upgrades via copyAllEnchantments().

B.5 — 20 Weapon Abilities (non-legendary)
In WeaponAbilityListener.java (81 KB), 2 per weapon class, triggered by sneak + right-click or on-hit, with cooldowns, action-bar messages, particles, and sounds. Ender Dragon takes 30% reduced ability damage. Netherite super-tools get +2% ability damage per custom enchantment.

B.6 — 3 Blessings
C's Bless: +1 heart per use ×10. Ami's Bless: +2% damage per use ×10. La's Bless: +2% defense per use ×10. Reapplied on join/respawn.

B.7 — Mob Difficulty System
Distance-based scaling (7 brackets): 350b → 1.5×/1.3×, 700b → 2.0×/1.6×, 1000b → 2.5×/1.9×, 2000b → 3.0×/2.2×, 3000b → 3.5×/2.5×, 5000b → 4.0×/3.0×.

Biome multipliers — Overworld: Pale Garden 2.0×, Deep Dark 2.5×, Swamp 1.4×. Nether: Wastes 1.7×/1.7×, Soul Sand Valley 1.9×/1.9×, Crimson Forest 2.0×/2.0×, Warped Forest 2.0×/2.0×, Basalt Deltas 2.3×/2.3×. The End: 2.5× health / 2.0× damage.

Boss Enhancer: Ender Dragon 3.5×/3.0×, Elder Guardian 2.5×/1.8×, Wither 1.0×/1.0×, Warden 1.0×/1.0×.

Creeper reduction: 50% spawns cancelled (KEEP at 50%).

B.8 — Other Systems
King Mobs: 1 per 100 spawns per EntityType, gold name, glowing, 10× health, 3× damage, drops 3–9 diamonds.

Blood Moon: 15% chance per night (will be raised to 35%), Darkness effect, red particles, overworld monsters (except Creepers) get 1.5× health / 1.3× damage, double drops and double XP. Every 10th Blood Moon spawns a Blood Moon King boss (diamond armor, netherite sword, glow, no-burn, 20× health = 400 HP, 5× damage = 15 dmg, drops 5–15 diamonds + 1 netherite ingot + 500 XP + 10% chance for random LEGENDARY weapon).

Guilds: YAML-based persistence, max 10 members, friendly-fire disabled by default. Commands: /guild create/invite/join/leave/kick/disband/info/list.

Utilities: Inventory sort (shift-click empty slot in container), enchantment transfer (tool → book), loot booster (guaranteed enchanted book in dungeon chests, 5% hostile / 15% boss book drops), drop rate booster (trident 35% from Drowned, guardian shards, ghast tears, breeze charges), villager trades (50% price reduction, unlimited trades), Pale Garden fog (periodic Darkness effect), soulbound (keep 1 item on death, lostsoul extraction), Best Buddies (wolf with diamond armor: 95% DR, pacifist, permanent Regen II), weapon mastery (10 classes, up to +10% damage at 1000 kills), axe nerf (attack speed 0.5).

Ore Detect (super pickaxe ability): Diamond tier scans 8b radius for ores, Netherite 12b. Ancient Debris: Diamond 24b, Netherite 40b. Particles show for 200 ticks (10s).

Commands: /jglims (reload, stats, enchants, sort, mastery, legendary, help), /guild (create, invite, join, leave, kick, disband, info, list). All require jglims.admin (OP) for admin commands.

B.9 — Legendary Weapon System (Phase 8 — FULLY WIRED, INPUT REWORK IN PROGRESS)
Four files are on GitHub and all registered in onEnable():

LegendaryWeapon.java — Enum: 44 weapons (24 LEGENDARY + 20 UNCOMMON). Each stores: id, displayName, baseMaterial (Material), baseDamage, customModelData (30001–30044, integer legacy field), tier (LegendaryTier), textureSource, textureName, rightClickAbilityName, holdAbilityName, rightClickCooldown, holdCooldown. Has fromId(String) static lookup.

LegendaryWeaponManager.java — Creates ItemStacks. PDC keys: is_legendary_weapon (BYTE), legendary_id (STRING), legendary_tier (STRING), legendary_cooldown (LONG), legendary_damage (AttributeModifier key), legendary_speed (AttributeModifier key). Methods: isLegendary(), identify(), getTier(), createWeapon(). Sets unbreakable, no glint (setEnchantmentGlintOverride(false)), string-based CustomModelDataComponent with strings[0] = weapon's textureName (1.21.4+ system), tier-colored bold name (DARK_PURPLE for LEGENDARY, GOLD for UNCOMMON). Detailed lore: tier tag, texture source, damage, attack speed, both abilities + cooldowns, unbreakable/no-enchant notes, trident auto-return hint. Hides default attributes via ItemFlag.HIDE_ATTRIBUTES and HIDE_UNBREAKABLE. Attack speed by material: swords 1.6, axes 1.0, trident 1.1, hoes 4.0, mace 0.6.

LegendaryAbilityListener.java (109 KB) — Handles 88 abilities (44 primary + 44 alternate). CRITICAL NOTE: The input rework (Phase 8b) was started in the previous session but NOT completed. The current code on GitHub still uses the OLD hold-to-charge system. The planned rework changes: standing right-click = primary ability, crouch+right-click = alternate ability, no more hold timer. The previous session wrote a partial replacement covering: new onPlayerInteract() dispatcher, all 44 primary ability methods, and about 20 of 44 alternate ability methods. The file must be completed in the next session. The rewrite must: remove holdStartTimes, holdAbilityCharged, HOLD_CHARGE_MS, startHoldChargeDisplay(), rename dispatchers from handleRightClickAbility/handleHoldAbility to handlePrimaryAbility/handleAlternateAbility, rename all hold*() methods to alt*(), cancel event for all legendary weapons (prevents trident throw), add activation feedback sound on alternate abilities.

Passive buff trackers: bloodlustStacks, retributionDamageStored, soulCount, grudgeTargets, predatorMarkTarget, gemBarrierCharges, shadowStanceActive, crescentParryActive, emberShieldActive, thunderShieldActive, undyingRageActive, rebornReady, phaseShiftActive. Damage event handler applies offensive buffs (Bloodlust +2/stack, Grudge +20%, Predator's Mark +30%) and defensive buffs (Shadow Stance 25% dodge, Crescent parry 100% reflect, Ember Shield 4 fire dmg to attackers, Thunder Shield lightning to attackers, Gem Barrier hit absorb, Undying Rage survive-at-1HP).

LegendaryLootListener.java (22 KB) — Drop system: Structure chests (LootGenerateEvent): LEGENDARY pool per structure (End City 25%, Stronghold 15%, Nether Fortress 12%, Bastion 12%, Ocean Monument 10%, Dungeon 8%, Temple 8%). UNCOMMON individual rolls per structure (each weapon has 1–2 sources with 8–20% chance). Boss kills (guaranteed 1): Elder Guardian → Ocean's Rage or Aquatic Sacred Blade; Warden → Excalibur/Requiem/Chakram/Berserker's/Acidic/Black Iron (6 pool); Wither → Muramasa/Phoenix/Soul Collector/Amethyst Shuriken (4 pool). Ender Dragon death chest: Places a chest at (0, highest, 0) with 6 random unique LEGENDARY weapons from a 14-weapon elite pool. Blood Moon King: 10% chance for any random LEGENDARY weapon. All drops trigger server-wide announcement + Totem of Undying particles + challenge sound.

B.10 — Live Client Test Results (Session 8)
Java client (JotaGlims) — CONFIRMED WORKING. Connected to 144.22.198.184:25565 with Minecraft 1.21.11, accepted the resource pack, tested /jglims legendary oceans_rage and other weapons. Custom textures render correctly on Java.

Bedrock client (Gustafare5693) — NOT WORKING YET. The Bedrock conversion via Rainbow has not been performed. This is a critical TODO.

SECTION C — POWER HIERARCHY & LEGENDARY WEAPON TIER SYSTEM (v2.0)
C.1 — Tier Definitions (NEW — replaces old LEGENDARY/UNCOMMON binary)
The old system had two tiers: LEGENDARY (24 weapons) and UNCOMMON (20 weapons). The new system introduces a 4-tier progression hierarchy tied to game progression:

Tier	Name	Color	Base DMG	Primary CD	Alt CD	Particle Budget	Found In
1	COMMON	GOLD	10–13	8–12s	20–30s	30–50	Overworld structures
2	RARE	GREEN	14–16	6–10s	15–25s	50–100	Nether structures
3	EPIC	DARK_PURPLE	17–20	5–8s	12–20s	100–150	Boss drops (Wither/Warden)
4	MYTHIC	DARK_RED	20–25	4–8s	10–15s	200+	End game (Dragon/End Cities)
5	ABYSSAL	DARK_AQUA	25–30	3–6s	8–12s	300+	Abyss dimension (endgame)
The existing 44 weapons will be reassigned to these tiers based on their drop source. New weapons will be added to fill out each tier. The LegendaryTier enum needs to be expanded from {LEGENDARY, UNCOMMON} to {COMMON, RARE, EPIC, MYTHIC, ABYSSAL}.

C.2 — Existing 44 Weapons (Reassigned to New Tiers)
COMMON TIER (Overworld) — 20 weapons (formerly UNCOMMON)

#	CMD	Weapon Name	Texture Source	Texture Name	Base Material	DMG	RC Ability (CD)	Alt Ability (CD)	Drop Locations
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
RARE TIER (Nether) — Formerly some of the old LEGENDARY tier + new additions

#	CMD	Weapon Name	Texture Source	Texture Name	Base Material	DMG	RC Ability (CD)	Alt Ability (CD)	Drop Source
1	30001	Ocean's Rage	Majestica	stormbringer	DIAMOND_SWORD	14	Tidal Crash — 6b AoE water blast, 15 dmg, knockback + Slowness (8s)	Riptide Surge — water launch + trail dmg (15s)	Elder Guardian
2	30002	Aquatic Sacred Blade	Majestica	aquantic_sacred_blade	DIAMOND_SWORD	14	Aqua Heal — +6 hearts + Conduit Power 30s (18s)	Depth Pressure — 10b Slowness III + Mining Fatigue (22s)	Elder Guardian
7	30007	Acidic Cleaver	Fantasy 3D	treacherous_cleaver	DIAMOND_AXE	15	Acid Splash — 5b cone, 12 dmg + Poison III 8s (8s)	Corrosive Aura — 6b 1 heart/s for 8s (22s)	Nether Fortress chest
18	30018	Solstice	Fantasy 3D	solstice	DIAMOND_SWORD	15	Solar Flare — 10b fire AoE, 16 dmg + blindness 3s (8s)	Daybreak — clear negatives + Regen IV 6s (22s)	Stronghold chest
19	30019	Grand Claymore	Fantasy 3D	grand_claymore	DIAMOND_SWORD	16	Titan Swing — 180° arc 10b, 18 dmg + knockback (8s)	Colossus Stance — 6s no-knockback, +3 range, +40% dmg (25s)	Bastion chest
20	30020	Calamity Blade	Majestica	calamity_blade	DIAMOND_AXE	15	Cataclysm — 6b AoE, falling blocks, 15 dmg + slowness (10s)	Doomsday — 8s double damage, but 1 heart/s cost (30s)	Nether Fortress chest
22	30022	Talonbrand	Fantasy 3D	talonbrand	DIAMOND_SWORD	14	Talon Strike — triple-hit combo 3×8=24 dmg (7s)	Predator's Mark — target takes +30% from all 10s (18s)	Dungeon chest
23	30023	Emerald Greatcleaver	Fantasy 3D	emerald_greatcleaver	DIAMOND_AXE	16	Emerald Storm — 6b AoE shards, 15 dmg + Poison II (8s)	Gem Barrier — absorb next 3 hits, 15s window (35s)	Temple chest
24	30024	Demon's Blood Blade	Majestica	demons_blood_blade	DIAMOND_SWORD	16	Blood Rite — sacrifice 3 hearts, deal 28 dmg (7s)	Demonic Form — 10s +60% dmg, fire trail, −50% def (30s)	Blood Moon King
EPIC TIER (Boss Drops — Wither & Warden) — Higher base damage

#	CMD	Weapon Name	Texture Source	Texture Name	Base Material	DMG	RC Ability (CD)	Alt Ability (CD)	Drop Source
3	30003	True Excalibur	Majestica	excalibur	DIAMOND_SWORD	18	Holy Smite — Lightning + 22 dmg AoE 5b (8s)	Divine Shield — 5s invulnerability + Strength II (40s)	Warden
4	30004	Requiem of the Ninth Abyss	Majestica	requiem_of_hell	DIAMOND_SWORD	17	Soul Devour — drain 8 hearts, heal self (10s)	Abyss Gate — 3 wither skeletons 15s (50s)	Warden
5	30005	Royal Chakram	Majestica	royalchakram	DIAMOND_SWORD	17	Chakram Throw — bounces 5 targets, 10 dmg each (6s)	Spinning Shield — 3s projectile deflect + 50% melee DR (18s)	Warden
6	30006	Berserker's Greataxe	Fantasy 3D	berserkers_greataxe	DIAMOND_AXE	19	Berserker Slam — 8b AoE, 20 dmg + launch (8s)	Blood Rage — +50% dmg, +30% speed, −30% def 10s (25s)	Warden
8	30008	Black Iron Greatsword	Fantasy 3D	black_iron_greatsword	DIAMOND_SWORD	17	Dark Slash — 10b line, 18 dmg, soul particles (7s)	Iron Fortress — Absorption IV + Resistance II 8s (25s)	Warden
9	30009	Muramasa	Majestica	muramasa	DIAMOND_SWORD	17	Crimson Flash — 8b dash, 15 dmg in path (5s)	Bloodlust — kills add +2 dmg stacks ×5, 15s window (18s)	Wither
10	30010	Phoenix's Grace	Fantasy 3D	gilded_phoenix_greataxe	DIAMOND_AXE	18	Phoenix Strike — Fire AoE 6b, 16 dmg + fire (8s)	Rebirth Flame — revive at 50% HP within 60s (100s)	Wither
11	30011	Soul Collector	Majestica	soul_collector	DIAMOND_SWORD	17	Soul Harvest — kill stores soul (+12 bonus next hit) (7s)	Spirit Army — release up to 5 souls as 8-dmg projectiles (25s)	Wither
12	30012	Amethyst Shuriken	Majestica	amethyst_shuriken	DIAMOND_SWORD	17	Shuriken Barrage — 5 fan projectiles, 9 dmg each (6s)	Shadow Step — teleport behind target + guaranteed crit (12s)	Wither
MYTHIC TIER (End Game — Dragon & End Cities) — Highest base damage, insane effects

#	CMD	Weapon Name	Texture Source	Texture Name	Base Material	DMG	RC Ability (CD)	Alt Ability (CD)	Drop Source
13	30013	Valhakyra	Majestica	valhakyra	DIAMOND_SWORD	20	Valkyrie Dive — leap 10b up, slam 25 dmg AoE (8s)	Wings of Valor — 8s slow-fall glide + Strength II (20s)	End City chest
14	30014	Windreaper	Fantasy 3D	windreaper	DIAMOND_SWORD	20	Gale Slash — 8b wind cone, 18 dmg + massive knockback (6s)	Cyclone — 4s tornado, pulls enemies, 6 dmg/s (15s)	End City chest
15	30015	Phantomguard	Fantasy 3D	phantomguard_greatsword	DIAMOND_SWORD	21	Spectral Cleave — through blocks, 10b line, 20 dmg (7s)	Phase Shift — 3s intangibility (28s)	Dragon death chest
16	30016	Moonlight	Fantasy 3D	moonlight	DIAMOND_SWORD	20	Lunar Beam — 15b ranged beam, 22 dmg (7s)	Eclipse — 6s Blindness + Weakness II to 15b enemies (25s)	Dragon death chest
17	30017	Zenith	Fantasy 3D	zenith	DIAMOND_SWORD	24	Final Judgment — 360° AoE 8b, 28 dmg (10s)	Ascension — 10s Flight + 50% dmg bonus (45s)	Dragon death chest
21	30021	Dragon Sword	Fantasy 3D	dragon_sword	DIAMOND_SWORD	22	Dragon Breath — 8b fire cone, 18 dmg + breath cloud (7s)	Draconic Roar — 8b Fear + Weakness II 5s (20s)	Dragon death chest
ABYSSAL TIER (The Abyss Dimension — 4 ultimate weapons) — Planned, not implemented These are the 4 ultimate weapons obtainable only from the Abyss dimension. Based on the Majestica textures shown in screenshots:

#	CMD	Weapon Name	Texture Source	Texture Name	Base Material	DMG	RC Ability (CD)	Alt Ability (CD)	Drop Source
45	30045	Requiem of the Ninth Abyss (Awakened)	Majestica	requiem_of_the_ninth_abyss	DIAMOND_SWORD	28	Void Devour — drain all hearts in 8b, heal full (4s)	Abyss Reckoning — 10 void wraiths 20s (45s)	Abyss Dragon
46	30046	True Excalibur (Awakened)	Majestica	true_excalibur	DIAMOND_SWORD	30	Divine Judgment — 5 lightning strikes + 35 dmg AoE 10b (5s)	Immortal Radiance — 10s full invulnerability + Strength III (60s)	Abyss Dragon
47	30047	Creation Splitter	Majestica	creation_splitter	DIAMOND_SWORD	28	Reality Tear — 15b line that splits the ground, 30 dmg (4s)	Dimensional Rift — 6s AoE void zone, 8 dmg/s + pulls (40s)	Abyss Dragon
48	30048	Whisperwind	Majestica	whisperwind	DIAMOND_SWORD	26	Silent Storm — instant 12b AoE, 25 dmg, no sound (3s)	Wind God — 15s flight + all entities in 20b pushed away continuously (50s)	Abyss Dragon
Image references for Abyssal weapons:

Requiem/Excalibur/Creation Splitter preview: https://www.genspark.ai/api/files/s/CXakuBB7
Whisperwind preview: https://www.genspark.ai/api/files/s/pQthnEmC
C.3 — Passive Holding Effects (Gameplay effects only — no visual particles to save FPS)
When a player holds a legendary weapon in their main hand, they passively receive gameplay effects. No visual particles to prevent server/client FPS drops. Effects are applied via LegendaryPassiveTask.java running every 20 ticks, checking main hand, applying/removing effects.

Weapon	Passive Effect
Ocean's Rage	+10% swim speed
Aquatic Sacred Blade	Water Breathing
True Excalibur	Undead in 8b take 1 dmg/s
Requiem of the Ninth Abyss	+10% damage to undead
Berserker's Greataxe	+5% melee damage
Muramasa	+15% attack speed
Phoenix's Grace	Fire Resistance
Soul Collector	+5% XP from kills
Zenith	+10% all damage
Moonlight	Night Vision
Dragon Sword	Fire Resistance + +10% damage in End
Warden weapons (Excalibur, Requiem, etc.)	Silent footsteps (Sculk sensors ignore)
Wither weapons (Muramasa, Phoenix, Soul, Shuriken)	Wither effect immunity
End weapons (Valhakyra, Windreaper, Phantomguard, etc.)	Slow Fall near ledges
Abyssal weapons	All of the above combined at 50% strength
SECTION D — RESOURCE PACK & TEXTURE SYSTEM (Phase 7)
D.1 — Version Discovery and Migration (CRITICAL LESSON)
The server runs Paper 1.21.11, not 1.21.1. The correct pack_format for Minecraft 1.21.11 is 75. From version 1.21.4 onward, the old overrides block in model JSON files is completely ignored by the client. The new system uses item-definition files in assets/minecraft/items/*.json with a minecraft:select type and minecraft:custom_model_data property matching string values (not integers). The plugin uses CustomModelDataComponent.setStrings(List.of(textureName)).

D.2 — Current Architecture (1.21.11 / pack_format 75)
pack.mcmeta: {"pack":{"pack_format":75,"description":"JGlims Legendary Weapons Resource Pack"}}

Item definition files (in assets/minecraft/items/): diamond_sword.json (32 cases), diamond_axe.json (8 cases), trident.json (3 cases), diamond_hoe.json (1 case). Use minecraft:select with "property": "minecraft:custom_model_data" and "index": 0 for string-based matching.

Model files (in assets/minecraft/models/item/): 42 JSON files, one per unique textureName. Each has "parent": "minecraft:item/handheld" and "textures": {"layer0": "minecraft:item/<textureName>"}.

Texture files (in assets/minecraft/textures/item/): 327 total PNGs. All 42 required textures present after Phase 7 fix.

D.3 — Five Texture Mismatches Fixed
Enum textureName	Actual file in pack	Issue	Resolution
aquantic_sacred_blade	aquantic_sacret_blade.png	Typo in original ("sacret")	Copied to correct name
excalibur	excalibur_ani.png	Different filename convention	Copied to correct name
requiem_of_hell	requiem_of_the_ninth_abyss.png	Different naming	Copied to correct name
royalchakram	royal_chakram.png	Underscore difference	Copied to correct name
frostaxe	(no match)	No texture existed	Copied aquantic_trident.png as placeholder
D.4 — Deployed Resource Pack
Current ZIP: JGlimsResourcePack.zip — 621 entries, 3,178,264 bytes, SHA-1 217bc25c174d32a266a6e753a0a0f2202c46f416.

Hosted at: https://github.com/JGlims/JGlimsPlugin/releases/download/v1.4.0-rp/JGlimsResourcePack.zip

server.properties: resource-pack=<URL>, resource-pack-sha1=<hash>, require-resource-pack=true.

Status: Java client CONFIRMED WORKING. Bedrock NOT WORKING (Rainbow conversion not done).

D.5 — Battle & Super Weapon Texture Assignments (NOT YET IMPLEMENTED)
Battle Weapons (tier 1) → Fantasy 3D: Battle Sickle=Iron Hay Sickle (10001), Battle Sword=Uchigatana (10002), Battle Axe=Iron Battle Axe (10003), Battle Spear=Iron Halberd (10004), Battle Mace=Iron Mace (10005), Battle Shovel=Iron Sai (10006).

Super Weapons (tier 2) → Majestica: Super Spear=Heavenly Partisan (20001), Super Sword=Thousand Demon Daggers (20002), Super Axe=Hearthflame/Crimson Cleaver (20003), Super Sickle=Jade Halberd/Crystal Frostscythe (20004), Super Mace=Frost Axe/Black Iron Clobberer (20005), Super Shovel=Sculk Scythe/Azure Scythe (20006).

D.6 — Available Unused Textures for New Weapons
Fantasy 3D (66 total weapons, ~24 unused): Black Iron Greataxe, Gloomsteel Greataxe, Gloomsteel Knife, Phantomguard Partisan, Azure Greatsword, Azure Dagger, Azure Sabre, Azure Greataxe, Azure Scythe, Berserker's Cleaver, Sacrificial Cleaver, Iron Broadsword, Iron Polearm, Claymore, Demonic Sword, Nature Sword, Piercer, Runic Piercer, Vesper, Skeleton Axe, Treacherous Bludgeon, Treacherous Axe, Wooden Bludgeon, Wooden Tonfa, Iron Dagger, Iron Greataxe.

Majestica (90+ total weapons, ~76 unused): Massive pool available. Website: https://realm-of-majestica.webflow.io/ — includes Creation Splitter, Whisperwind, and many others visible in screenshots. [+] items require 1.21.5+.

D.7 — Other Resource Packs (to be merged into server RP)
Pack	Purpose	Delivery
Spryzeen's Knight Armor	Medieval armor retextures	Server RP merge
Enchantment Outlines	Colored glint per enchant (normal weapons ONLY)	Server RP merge
Recolourful Containers GUI + HUD	Custom container/inventory GUI	Server RP merge
Recolourful Containers Hardcore Hearts	Custom heart textures	Server RP merge
Story Mode Clouds	Stylized clouds	Server RP merge
Fresh Food (with blessings)	Custom food textures	Server RP merge
AL's mob revamp packs	Zombie, Skeleton, Creeper, Enderman, Boss Rush, Piglin, Dungeons Boss Bars, Mob Weapons	Server RP merge
Drodi's Blazes × Fresh Animations	Blaze retextures	Server RP merge
Fantasy 3D Weapons CIT	ALL 66 3D weapon models	Server RP merge
Blades of Majestica	ALL 90+ 3D models	Server RP merge
D.8 — Fresh Animations & Client Setup
Java (JotaGlims): Fabric + EMF + ETF + CIT Resewn. Pack order: (1) Fresh Animations: Player Extension, (2) Fresh Animations, (3) AL's mob revamp packs, (4) Drodi's Blazes, (5) Server resource pack.

Bedrock (Gustafare5693): Auto-download from Geyser packs/ folder: Fresh Animations Bedrock, AL revamp Bedrock ports, Server custom weapon Bedrock pack (generated by Rainbow).

D.9 — Bedrock Conversion (NOT YET DONE — CRITICAL)
Run Rainbow (https://geysermc.org/wiki/other/rainbow/) on Java resource pack ZIP → .mcpack with Bedrock geometry + geyser_mappings.json → place in Geyser's packs/ folder on server → verify Bedrock clients see custom textures.

SECTION E — KNOWN BUGS (Priority Order)
E.1 — frostaxe.png is a placeholder (MEDIUM): Neptune's Fang displays wrong visual.

E.2 — Legendary ability input rework INCOMPLETE (CRITICAL): Phase 8b was started but not finished. The LegendaryAbilityListener.java on GitHub still uses the OLD hold-to-charge system. The rewrite was partially done in the previous session (primary abilities complete, ~20 of 44 alternate abilities written). Must be completed.

E.3 — Legendary abilities need visual spectacle (HIGH): All 88 ability methods exist but need dramatically more particles, sounds, screen-shake, and damage. Must scale by tier (COMMON=basic, MYTHIC=insane).

E.4 — Sickle shows as "Battle Hoe" (LOW): Display name wrong, damage only 4 (should be 7–8).

E.5 — Weapon abilities damage only mobs, not players (MEDIUM): Most non-legendary abilities filter out Players. Fix using legendary system as reference.

E.6 — Guild default membership (LOW): /guild info with no guild should show proper message.

E.7 — Enchantment glint on custom weapons (LOW): Battle/Super weapons may still show glint.

E.8 — config.yml version header (COSMETIC): Says "v1.3.0" while plugin is v1.4.0.

E.9 — Bedrock resource pack not working (HIGH): Rainbow conversion not performed.

E.10 — GSON malformed JSON warning in server logs (LOW): Cosmetic, didn't prevent loading.

SECTION F — PLANT TOTEMS & BOSS TOTEMS (Phase 9 — PLANNED)
F.1 — Plant Totems (Elemental Resistance)
Right-click to absorb (consumed). Permanently adds +10% resistance per type to player PDC, up to 3 absorptions (30% cap). Found in structure chests and dropped by Legendary Mobs.

Totem	Resistance Type	Per Use	Max	Found In
Fern Totem	Fire damage	10%	30%	Nether Fortress, Bastion
Moss Totem	Poison/Wither	10%	30%	Jungle Temple, Swamp Huts
Cactus Totem	Projectile	10%	30%	Desert Temple, Pillager Outpost
Vine Totem	Fall damage	10%	30%	Jungle Temple, Mineshaft
Lily Totem	Drowning/Freeze	10%	30%	Ocean Monument, Shipwreck
Mushroom Totem	Explosion	10%	30%	Dungeon, Woodland Mansion
F.2 — Boss Totems (Unique Passive Abilities)
Boss Totem	Drop Source	Passive Effect
Guardian's Blessing	Elder Guardian	+50% swim speed + Respiration III permanent
Wither's Immunity	Wither	Complete Wither effect immunity
Warden's Silence	Warden	Silent walking (Sculk sensors ignore you)
Dragon's Gaze	Ender Dragon	Permanent Night Vision + Endermen don't aggro
SECTION G — NEW CUSTOM ITEMS (PLANNED)
G.1 — Relic Fragments
Dropped by mini-bosses and events. Collect 5 matching fragments to craft a specific legendary weapon at a Relic Forge (custom crafting station — anvil-based recipe). Deterministic path to legendaries. PDC: is_relic_fragment (BYTE) + relic_weapon_id (STRING). Implementation: RelicManager.java, RelicListener.java.

G.2 — Legendary Shards
Break down unwanted legendary weapons into shards by placing legendary + grindstone interaction. 3 shards = 1 random legendary reroll at the Relic Forge. Reroll output is limited to COMMON or RARE tier only (cannot reroll into EPIC/MYTHIC/ABYSSAL). PDC: is_legendary_shard (BYTE), shard_count (INTEGER).

G.3 — Dimensional Anchors
New item dropped by Ender Dragon (guaranteed 1 per kill). 4 Dimensional Anchors + 4 Obsidian + 1 Nether Star = Abyss Portal Frame (custom recipe). Used to build the portal to The Abyss. PDC: is_dimensional_anchor (BYTE).

G.4 — Boss Trophies
Decorative mob head items dropped by mini-bosses and event bosses. Give +2% damage per trophy type when in inventory. Stackable with different trophy types. Types: Blood Moon King Trophy, Blaze Lord Trophy, Pillager Captain Trophy, End Guardian Trophy, Abyss Dragon Trophy. PDC: is_boss_trophy (BYTE), trophy_type (STRING). Implementation: TrophyManager.java, TrophyListener.java.

G.5 — Enchanted Food
Custom food items with powerful temporary buffs. Found in structure chests. Consumed on right-click.

Food	Effect	Duration	Found In
Dragon Steak	+50% damage	30s	End City chests, Dragon death chest
Warden Eye Soup	Night Vision + Sculk immunity	60s	Ancient City chests
Blaze Pepper	Fire Resistance + fire melee damage	45s	Nether Fortress, Bastion
Ender Berry	Teleport randomly within 15b on hit taken (dodge)	30s	End City, Stronghold
Guardian Fin Stew	Water Breathing + Dolphin's Grace + Conduit Power	60s	Ocean Monument, Shipwreck
PDC: is_enchanted_food (BYTE), food_type (STRING). Implementation: EnchantedFoodManager.java, EnchantedFoodListener.java.

G.6 — Artifact Accessories (Offhand Items)
Items placed in the offhand slot providing passive bonuses. One per player active at a time.

Artifact	Passive Effect	Drop Source
Warden's Echo	Darkness immunity + silent walking	Warden
Dragon Scale	+20% End damage + slow fall	Ender Dragon
Nether Heart	Fire immunity + +15% Nether damage	Wither
Ocean Pearl	Water Breathing + Dolphin's Grace	Elder Guardian
Abyss Core	+10% all damage + void immunity	Abyss Dragon
PDC: is_artifact (BYTE), artifact_type (STRING). Implementation: ArtifactManager.java, ArtifactListener.java.

SECTION H — EVENTS & MINI-BOSSES (PLANNED)
H.1 — Blood Moon Enhancement
Increase base chance from 15% to 35% (only one custom event at a time)
Blood Moon King spawns every 5th Blood Moon (not 10th)
Add 3 Blood Moon Lieutenants (mini-bosses with 200 HP each, spawn with King)
"Cursed" mobs during Blood Moon get red particles + extra aggro range
Blood Moon King now drops 15% chance for RARE tier legendary (up from 10%)
H.2 — Pillager Siege (every 3 in-game days)
Pillager patrol spawns near nearest village with players within 100b
3 waves: Wave 1 = 10 pillagers, Wave 2 = 15 pillagers + 3 ravagers, Wave 3 = Pillager Captain boss (300 HP, crossbow rapid-fire, summons reinforcements)
Hero of the Village II on completion
Drops: massive emerald drops (32-64), COMMON legendary 20% chance, plant totem 10% chance
Server-wide announcement on start/completion
H.3 — Nether Rift (random in Nether, chance per Nether night cycle)
Fire portal effect opens at random location near players
Empowered Nether mobs pour out in 3 waves (blazes, wither skeletons, piglins)
Blaze Lord mini-boss (200 HP, fire AoE abilities, lava trail)
Drops: RARE legendary guaranteed, netherite scraps (5-10), blaze powder (32), Relic Fragments
Fire/lava particle effects, dramatic sounds
H.4 — End Storm (after Dragon is killed, periodic chance)
Shulker rain + Enderman horde descend in End islands
End Guardian boss (400 HP, teleportation abilities, beam attack, shulker bullet spam)
Drops: MYTHIC legendary guaranteed, Dragon's Gaze totem 25% chance, Dimensional Anchor
End Rod + Reverse Portal particle storm, dramatic lightning
H.5 — The Abyss Dimension (ENDGAME — Phase 15)
The ultimate endgame content. A custom dimension accessed through a special portal.

Portal Construction: 4 Dimensional Anchors (from Dragon kills) + 4 Obsidian + 1 Nether Star arranged in a custom frame. Right-click with a Dragon's Gaze totem to activate.

Dimension Features:

Void-themed dimension with floating islands of End Stone and Deepslate
Permanent Darkness II effect (countered by Dragon's Gaze totem or Moonlight passive)
Custom ambient sounds (low rumbles, whispers, void hums)
Abyss Mobs:

Void Walker: Enderman variant, 100 HP, teleports rapidly, melee + void damage
Abyss Crawler: Spider variant, 60 HP, wall-climbing, applies Darkness + Slowness
Shadow Sentinel: Wither Skeleton variant, 150 HP, soul fire sword, spawns soul projectiles
Abyss Dragon (Final Boss):

800 HP, void-themed Ender Dragon reskin (custom texture via resource pack)
Phase 1 (800-400 HP): Standard dragon flight + void breath (replaces dragon breath, deals 15 dmg, applies Darkness 5s)
Phase 2 (400-0 HP): Lands, becomes melee boss, summons Shadow Sentinels, void shockwaves
Guaranteed drops: 1 random ABYSSAL weapon, 1 Abyss Core artifact, 2 Dimensional Anchors (for repeat fights), 10 netherite ingots, Boss Trophy
Server-wide announcement with dramatic void-themed particles
ABYSSAL Weapons (4 total — the ultimate items):

Requiem of the Ninth Abyss (Awakened) — DMG 28, textureName: requiem_of_the_ninth_abyss — Void Devour (drain all hearts 8b radius, full heal, 4s CD) + Abyss Reckoning (10 void wraiths 20s, 45s CD)
True Excalibur (Awakened) — DMG 30, textureName: true_excalibur — Divine Judgment (5 lightning + 35 dmg AoE 10b, 5s CD) + Immortal Radiance (10s invulnerability + Strength III, 60s CD)
Creation Splitter — DMG 28, textureName: creation_splitter — Reality Tear (15b line, 30 dmg, 4s CD) + Dimensional Rift (6s void zone AoE, 8 dmg/s + pull, 40s CD)
Whisperwind — DMG 26, textureName: whisperwind — Silent Storm (instant 12b AoE, 25 dmg, no sound, 3s CD) + Wind God (15s flight + 20b push aura, 50s CD)
Implementation: AbyssManager.java (dimension management), AbyssDragonBoss.java, AbyssMobManager.java, AbyssPortalListener.java.

SECTION I — MOB DIFFICULTY REBALANCE (PLANNED)
Updated values (approved by user):

Baseline health multiplier: 1.0 → 1.1 (user chose 1.1, not 1.5)
Baseline damage multiplier: 1.0 → 1.1
New distance bracket: 7500b → 5.0×/4.0×
New distance bracket: 10000b → 6.0×/5.0×
King Mob chance: 1/100 → 1/75
New "Legendary Mob" tier: 1/500 spawns, 20× HP, 5× DMG, guaranteed diamond + plant totem chance + RARE tier legendary weapon drop
Creeper reduction: stays at 50% (user explicitly kept this)
SECTION J — WELCOME BOOK (Phase 10 — PLANNED)
Written Book in Portuguese Brazil (PT-BR) given on first join. 15+ pages covering all systems. /jglims livro for replacement copy. Implementation: WelcomeBookManager.java.

SECTION K — CREATIVE CATALOG GUI (Phase 11 — PLANNED)
/jglims catalog → virtual chest GUI. Pages for all weapon tiers, enchantments, totems, artifacts, food. Display-only (OP+Creative only for item extraction). Implementation: CatalogManager.java, CatalogListener.java.

SECTION L — CONFIG.YML COMPLETE REFERENCE (158 lines — CURRENT, needs expansion)
Copy# JGlimsPlugin v1.3.0 Configuration  # ← will be updated to v3.0.0
mob-difficulty:
  enabled: true
  baseline-health-multiplier: 1.0   # → will change to 1.1
  baseline-damage-multiplier: 1.0   # → will change to 1.1
  distance:
    350: { health: 1.5, damage: 1.3 }
    700: { health: 2.0, damage: 1.6 }
    1000: { health: 2.5, damage: 1.9 }
    2000: { health: 3.0, damage: 2.2 }
    3000: { health: 3.5, damage: 2.5 }
    5000: { health: 4.0, damage: 3.0 }
    # → add 7500: { health: 5.0, damage: 4.0 }
    # → add 10000: { health: 6.0, damage: 5.0 }
  biome:
    pale-garden: 2.0
    deep-dark: 2.5
    swamp: 1.4
    nether-wastes: { health: 1.7, damage: 1.7 }
    soul-sand-valley: { health: 1.9, damage: 1.9 }
    crimson-forest: { health: 2.0, damage: 2.0 }
    warped-forest: { health: 2.0, damage: 2.0 }
    basalt-deltas: { health: 2.3, damage: 2.3 }
    end: { health: 2.5, damage: 2.0 }
boss-enhancer:
  ender-dragon: { health: 3.5, damage: 3.0 }
  wither: { health: 1.0, damage: 1.0 }
  warden: { health: 1.0, damage: 1.0 }
  elder-guardian: { health: 2.5, damage: 1.8 }
creeper-reduction: { enabled: true, cancel-chance: 0.5 }
pale-garden-fog: { enabled: true, check-interval: 40 }
loot-booster: { enabled: true, chest-enchanted-book: true, guardian-shards-min: 1, guardian-shards-max: 3, elder-guardian-shards-min: 3, elder-guardian-shards-max: 5, ghast-tears-min: 1, ghast-tears-max: 2, echo-shard-chance: 0.40 }
mob-book-drops: { enabled: true, hostile-chance: 0.05, boss-custom-chance: 0.15, looting-bonus-regular: 0.02, looting-bonus-boss: 0.05 }
blessings:
  c-bless: { max-uses: 10, heal-per-use: 1 }
  ami-bless: { max-uses: 10, damage-percent-per-use: 2.0 }
  la-bless: { max-uses: 10, defense-percent-per-use: 2.0 }
anvil: { remove-too-expensive: true, xp-cost-reduction: 0.5 }
toggles: { inventory-sort: true, enchant-transfer: true, sickle: true, battle-axe: true, battle-bow: true, battle-mace: true, battle-shovel: true, super-tools: true, drop-rate-booster: true, spear: true }
drop-rate-booster: { trident-drop-chance: 0.35, breeze-wind-charge-min: 2, breeze-wind-charge-max: 5 }
villager-trades: { enabled: true, price-reduction: 0.50, disable-trade-locking: true }
king-mob: { enabled: true, spawns-per-king: 75, health-multiplier: 10.0, damage-multiplier: 3.0, diamond-drop-min: 3, diamond-drop-max: 9 }
axe-nerf: { enabled: true, attack-speed: 0.5 }
weapon-mastery: { enabled: true, max-kills: 1000, max-bonus-percent: 10.0 }
blood-moon: { enabled: true, check-interval: 100, chance: 0.35, mob-health-multiplier: 1.5, mob-damage-multiplier: 1.3, boss-every-nth: 5, boss-health-multiplier: 20.0, boss-damage-multiplier: 5.0, boss-diamond-min: 5, boss-diamond-max: 15, double-drops: true }
guilds: { enabled: true, max-members: 10, friendly-fire: false }
best-buddies: { dog-armor-damage-reduction: 0.95 }
super-tools: { iron-bonus-damage: 1.0, diamond-bonus-damage: 2.0, netherite-bonus-damage: 2.0, netherite-per-enchant-bonus-percent: 2.0 }
ore-detect: { radius-diamond: 8, radius-netherite: 12, ancient-debris-radius-diamond: 24, ancient-debris-radius-netherite: 40, duration-ticks: 200 }
# → New sections to add: legendary-mob, pillager-siege, nether-rift, end-storm, abyss
Copy
SECTION M — PROBLEMS SOLVED ACROSS ALL SESSIONS (Lessons Learned)
RecipeManager super-recipe bug: Duplicate NamespacedKeys silently failing → use unique key per recipe.
BestBuddiesListener never registered: Always check onEnable() for new listeners.
King Mob spawn rate too high: Set to 1/100 spawns (will be 1/75).
Inconsistent weapon lore: Standardized to Adventure API gold/gray/green scheme.
Weapon ability damage too low for scaled mobs: Buffed all 20 abilities.
SpearManager using legacy ChatColor: Migrated to Adventure API.
MobDifficultyManager hard-coding multipliers: Now ConfigManager-driven.
BiomeMultipliers hard-coding values: Now initWithConfig().
12 new enchantments added (52→64) with conflicts registered.
Legacy § codes in CustomEnchantManager: Migrated to Adventure Component.text().
Duplicate JAR warning: Resolved by deleting old file before copy.
LegendaryAbilityListener compilation errors: Fixed in commits a5d0209, 0c2accc3, 743a21e6.
Resource pack pack_format wrong: Must use 75 for 1.21.11 (not 34 for 1.21.1, not 46 for 1.21.4). The server runs 1.21.11, not 1.21.1 as originally assumed.
JSON files with BOM: PowerShell Set-Content adds BOM; use [System.IO.File]::WriteAllText() with UTF8Encoding($false).
1.21.4+ item_model system vs overrides: From 1.21.4 onward, overrides in model JSONs are ignored. Must use assets/minecraft/items/*.json with minecraft:select and string-based custom_model_data.
assets/minecraft/items/ folder: Originally told to delete it (correct for 1.21.1). But since server is 1.21.11, this folder is now required.
LegendaryWeapon enum returned String for baseMaterial: Fixed to return Material directly.
Resource pack tested client-side only: Must be server-side for Bedrock support.
CustomModelData integer vs string mismatch: setCustomModelData(Integer) writes to floats[0], but 1.21.4+ item definitions read from strings[0]. Fixed by using CustomModelDataComponent.setStrings(List.of(textureName)).
PowerShell ErrorActionPreference="Stop": Java's stderr warnings caused build script abort. Fixed with $ErrorActionPreference = "Continue" and $PSNativeCommandUseErrorActionPreference = $false.
SSH hostname resolution: Deploy script used jglims but SSH config has MinecraftServer.
sed quoting through PowerShell → SSH → Docker → bash: Four shell layers broke quoting. Fixed by removing outer single quotes.
Five texture filename mismatches: Always audit filenames against enum before zipping.
LegendaryAbilityListener.java too large for crawler: At 109KB, the crawler tool returns partial content. Must reconstruct from weapon table + patterns seen in partial read.
SECTION N — REPOSITORY STRUCTURE (Current + Planned)
JGlimsPlugin/
├── .gitignore
├── README.md
├── build.gradle                                   # Paper 1.21.11, Java 21
├── settings.gradle
├── gradlew / gradlew.bat
├── gradle/wrapper/
├── phase7.ps1                                     # Resource pack build script
└── src/main/
    ├── java/com/jglims/plugin/
    │   ├── JGlimsPlugin.java                      # Main class (17.9 KB)
    │   ├── config/
    │   │   └── ConfigManager.java                 # (29.2 KB)
    │   ├── enchantments/
    │   │   ├── EnchantmentType.java               # 64 enchantments
    │   │   ├── CustomEnchantManager.java
    │   │   ├── AnvilRecipeListener.java
    │   │   ├── EnchantmentEffectListener.java     # (69.5 KB)
    │   │   └── SoulboundListener.java
    │   ├── blessings/
    │   │   ├── BlessingManager.java
    │   │   └── BlessingListener.java
    │   ├── guilds/
    │   │   ├── GuildManager.java
    │   │   └── GuildListener.java
    │   ├── mobs/
    │   │   ├── MobDifficultyManager.java
    │   │   ├── BiomeMultipliers.java
    │   │   ├── BossEnhancer.java
    │   │   ├── KingMobManager.java
    │   │   └── BloodMoonManager.java
    │   ├── crafting/
    │   │   ├── RecipeManager.java
    │   │   └── VanillaRecipeRemover.java
    │   ├── weapons/
    │   │   ├── Battle*Manager.java (8 files)
    │   │   ├── SickleManager.java
    │   │   ├── SpearManager.java
    │   │   ├── SuperToolManager.java
    │   │   ├── WeaponAbilityListener.java         # (81.7 KB)
    │   │   └── WeaponMasteryManager.java
    │   ├── legendary/                             # ⚠️ INPUT REWORK INCOMPLETE
    │   │   ├── LegendaryWeapon.java               # 44→48+ weapons enum
    │   │   ├── LegendaryWeaponManager.java        # Item creation
    │   │   ├── LegendaryAbilityListener.java      # 88→96+ abilities (NEEDS COMPLETION)
    │   │   ├── LegendaryLootListener.java         # Drop system
    │   │   └── LegendaryPassiveTask.java          # 🔴 PLANNED - holding effects
    │   ├── totems/                                # 🔴 PLANNED (Phase 9)
    │   │   ├── PlantTotemManager.java
    │   │   ├── PlantTotemListener.java
    │   │   ├── BossTotemManager.java
    │   │   └── BossTotemListener.java
    │   ├── items/                                 # 🔴 PLANNED (Phase 11)
    │   │   ├── RelicManager.java                  # Relic Fragments + Legendary Shards
    │   │   ├── RelicListener.java
    │   │   ├── EnchantedFoodManager.java
    │   │   ├── EnchantedFoodListener.java
    │   │   ├── ArtifactManager.java               # Offhand accessories
    │   │   ├── ArtifactListener.java
    │   │   ├── TrophyManager.java
    │   │   └── TrophyListener.java
    │   ├── events/                                # 🔴 PLANNED (Phase 10)
    │   │   ├── PillagerSiegeManager.java
    │   │   ├── NetherRiftManager.java
    │   │   └── EndStormManager.java
    │   ├── abyss/                                 # 🔴 PLANNED (Phase 15)
    │   │   ├── AbyssManager.java
    │   │   ├── AbyssDragonBoss.java
    │   │   ├── AbyssMobManager.java
    │   │   └── AbyssPortalListener.java
    │   ├── catalog/                               # 🔴 PLANNED
    │   │   ├── CatalogManager.java
    │   │   └── CatalogListener.java
    │   ├── book/                                  # 🔴 PLANNED
    │   │   └── WelcomeBookManager.java
    │   └── utility/
    │       ├── BestBuddiesListener.java
    │       ├── DropRateListener.java
    │       ├── EnchantTransferListener.java
    │       ├── InventorySortListener.java
    │       ├── LootBoosterListener.java
    │       ├── PaleGardenFogTask.java
    │       └── VillagerTradeListener.java
    └── resources/
        ├── plugin.yml
        └── config.yml
SECTION O — GITHUB RELEASES & IMAGE URLS
Releases
Release Tag	Title	Date	Size	Status
v1.4.0-rp	Resource Pack v1.4.0	2026-03-06	3,178,264 B	Current production
select-resources-v1.0	Resource Pack v1.0	2026-03-05	3,152,223 B	Legacy
Direct download: https://github.com/JGlims/JGlimsPlugin/releases/download/v1.4.0-rp/JGlimsResourcePack.zip

Image URLs from All Sessions
https://www.genspark.ai/api/files/s/nhRKbJOw, https://www.genspark.ai/api/files/s/rwop3T2K, https://www.genspark.ai/api/files/s/wm26PNPW, https://www.genspark.ai/api/files/s/Mv2yS8Iu, https://www.genspark.ai/api/files/s/PlrWlWpI, https://www.genspark.ai/api/files/s/XGtWb2G8, https://www.genspark.ai/api/files/s/tZUP7HEg, https://www.genspark.ai/api/files/s/Ch1hMkh3, https://www.genspark.ai/api/files/s/GRfk6sBT, https://www.genspark.ai/api/files/s/TOnhJaKI, https://www.genspark.ai/api/files/s/HYmXvxWz, https://www.genspark.ai/api/files/s/Y9U308nE, https://www.genspark.ai/api/files/s/hCCnZru2, https://www.genspark.ai/api/files/s/4Agrpixv, https://www.genspark.ai/api/files/s/z0Wxs9RY, https://www.genspark.ai/api/files/s/lhLtipOJ, https://www.genspark.ai/api/files/s/Qa56tF16, https://www.genspark.ai/api/files/s/LpF08MnP, https://www.genspark.ai/api/files/s/76iabqHL, https://www.genspark.ai/api/files/s/8G2FILbi, https://www.genspark.ai/api/files/s/PEucRdRp, https://www.genspark.ai/api/files/s/2xlCstun, https://www.genspark.ai/api/files/s/wXViEhTo, https://www.genspark.ai/api/files/s/UO34kssN, https://www.genspark.ai/api/files/s/K5Jv5W2t, https://www.genspark.ai/api/files/s/JGwa1ox5, https://www.genspark.ai/api/files/s/XLmuJkJG, https://www.genspark.ai/api/files/s/CXakuBB7 (Abyssal weapons preview: Requiem, Excalibur, Creation Splitter), https://www.genspark.ai/api/files/s/pQthnEmC (Whisperwind preview)

SECTION P — KEY REFERENCE LINKS
Resource	URL
GitHub Repository	https://github.com/JGlims/JGlimsPlugin
Latest Commit	https://github.com/JGlims/JGlimsPlugin/commit/a159897a80460940c55e5af90e1cdb972adda7aa
API Commits Endpoint	https://api.github.com/repos/JGlims/JGlimsPlugin/commits?per_page=1
API Tree Endpoint	https://api.github.com/repos/JGlims/JGlimsPlugin/git/trees/main?recursive=1
Raw File Base	https://raw.githubusercontent.com/JGlims/JGlimsPlugin/main/
Resource Pack Release	https://github.com/JGlims/JGlimsPlugin/releases/tag/v1.4.0-rp
Resource Pack Download	https://github.com/JGlims/JGlimsPlugin/releases/download/v1.4.0-rp/JGlimsResourcePack.zip
Paper API JavaDocs	https://jd.papermc.io/paper/1.21.11/
Paper CustomModelData API	https://jd.papermc.io/paper/1.21.11/io/papermc/paper/datacomponent/item/CustomModelData.html
Pack Format Wiki	https://minecraft.wiki/w/Pack_format
Items Model Definition Wiki	https://minecraft.wiki/w/Items_model_definition
Misode pack.mcmeta Generator	https://misode.github.io/pack-mcmeta/
GeyserMC Wiki	https://geysermc.org/wiki/geyser/
Geyser Custom Items	https://geysermc.org/wiki/geyser/custom-items/
Rainbow (Bedrock converter)	https://geysermc.org/wiki/other/rainbow/
Thunder (RP converter)	https://geysermc.org/wiki/other/thunder/
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
Source Tree	https://github.com/JGlims/JGlimsPlugin/tree/main/src/main/java/com/jglims/plugin
Legendary Dir	https://github.com/JGlims/JGlimsPlugin/tree/main/src/main/java/com/jglims/plugin/legendary
Config File	https://github.com/JGlims/JGlimsPlugin/blob/main/src/main/resources/config.yml
SECTION Q — PHASE ROADMAP (COMPLETE v2.0)
Phase	Name	Status	Priority
5	Bug Fixes (Sickle name, PvP abilities, guild info, glint)	Partially done	MEDIUM
6	Performance Profiling	Not started	LOW
8b	Legendary Ability Input Rework (RC + Crouch+RC)	STARTED BUT INCOMPLETE	CRITICAL
8c	Legendary Ability Visual Overhaul (tier-scaled particle spectacle)	Not started	CRITICAL
8d	Expand Legendary Roster (new weapons from all textures, 5-tier system)	Not started	HIGH
8e	Passive Holding Effects (gameplay buffs, no visual particles)	Not started	HIGH
9	Plant Totems (6) + Boss Totems (4)	Not started	HIGH
10	Events & Mini-Bosses (Blood Moon+, Pillager Siege, Nether Rift, End Storm)	Not started	HIGH
11	New Custom Items (Relics, Shards, Anchors, Trophies, Food, Artifacts)	Not started	HIGH
12	Mob Difficulty Rebalance (baseline 1.1×, new brackets, Legendary Mobs)	Not started	MEDIUM
13	Resource Pack Mega-Merge (all packs into one server RP)	Not started	HIGH
14	Bedrock Conversion (Rainbow + Geyser packs)	Not started	HIGH
15	The Abyss Dimension (endgame, Abyss Dragon, 4 ABYSSAL weapons)	Not started	HIGH
16	Welcome Book (PT-BR, 15+ pages)	Not started	MEDIUM
17	Creative Catalog GUI	Not started	MEDIUM
18	Survival Mode Switch (lock commands, finalize config)	Not started	LOW
SECTION R — WHAT THE NEXT SESSION MUST DO FIRST
IMMEDIATE PRIORITY 1 — Complete Phase 8b (Legendary Ability Input Rework): The LegendaryAbilityListener.java rewrite was started but NOT completed. The file on GitHub still uses the old hold-to-charge system. The next session must:

Fetch the current file from GitHub
Complete the rewrite: remove hold system, implement standing RC = primary, crouching RC = alternate
Ensure ALL 44 primary and ALL 44 alternate ability methods are present
Update LegendaryWeaponManager.java lore from "Hold (2s)" to "Crouch + Right-Click"
Build, deploy, test
IMMEDIATE PRIORITY 2 — Phase 8c (Visual Ability Overhaul): Enhance all 88+ abilities with tier-scaled particle counts, layered sounds, screen shake, and dramatic effects. MYTHIC abilities should be jaw-dropping.

IMMEDIATE PRIORITY 3 — Phase 8d (Expand Legendary Roster + 5-Tier System): Expand LegendaryTier enum to 5 tiers (COMMON, RARE, EPIC, MYTHIC, ABYSSAL). Reassign existing 44 weapons. Add new weapons from unused Fantasy 3D and Majestica textures. Update loot tables.

IMMEDIATE PRIORITY 4 — Bedrock Resource Pack: Run Rainbow conversion, deploy to Geyser packs/.

IMMEDIATE PRIORITY 5 — Phase 8e (Passive Holding Effects): Implement LegendaryPassiveTask.java with gameplay-only buffs (no visual particles).

SUBSEQUENT PRIORITIES: Totems → Events → Custom Items → Difficulty Rebalance → Resource Pack Merge → Abyss Dimension.

Standing Rules: Always verify latest GitHub commit. Always provide complete file replacements. Always include build-and-deploy commands. Always use UTF-8 without BOM. Always test before marking complete. If a file is too large for one message, split across multiple messages but ALWAYS send the complete file.

This summary contains every detail from every conversation session across 8+ sessions: all 44+ weapons with stats/abilities/drops, tier reassignment plan, 4 Abyssal weapons with image references, all 64 enchantments with conflicts, all config values, all file sizes and paths, all bugs, all future plans (including The Abyss dimension), all URLs, all deployment commands, all 24 lessons learned, all texture assignments and mismatch fixes, all server infrastructure details, all SSH configuration, all resource-pack architecture decisions and version-format migration history, the incomplete Phase 8b state, approved expansion plan with user modifications (1.1× baseline, 50% creepers, 35% blood moon, no Warden Awakening event, no Soul Gems, ABYSSAL weapons from Majestica screenshots, gameplay-only passives). Copy this entire document into the next chat session as context.
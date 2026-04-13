# JGlimsPlugin

_Minecraft Paper plugin — custom weapons, armor, enchantments, 72 mobs, 72 structures, 4 custom dimensions, vampire &amp; werewolf systems, a self-hosted resource pack, and BetterModel 3D rendering. Runs on Minecraft **26.1.2** (Paper 26.1.2 alpha)._

This document is the **single-source-of-truth snapshot** of the project as of 2026-04-12. Intended use: paste it into a fresh Claude Code session after `/clear` so the new context knows exactly where the project stands. Also safe to share with Claude Opus via Genspark when planning structure reworks.

---

## 1. Project at a glance

| Thing | Value |
|---|---|
| Plugin name | `JGlimsPlugin` |
| Version | `2.0.0` |
| Target Minecraft | **26.1.2** (released 2026-04-09) |
| Target Paper API | `io.papermc.paper:paper-api:26.1.2.build.5-alpha` |
| Required JVM | **Java 25** (Paper 26.1.x requires it) |
| Resource pack format | `84` |
| BetterModel | `3.0.0-SNAPSHOT-487-paper` |
| Java source files | ~240 |
| Final JAR size | ~1.6 MB |
| Build time | ~15s after a warm cache |
| Build command | `cd JGlimsPlugin && ./gradlew build` |
| Output JAR | `JGlimsPlugin/build/libs/JGlimsPlugin-2.0.0.jar` |

## 2. Repository layout

```
JGlimsPlugin/                      ← repo root
├── README.md                      ← THIS FILE
├── CLAUDE.md                      ← guidance for Claude Code
├── bbmodels/                      ← source .bbmodel files + .bak backups + _animator.py
├── bbmodels_deploy/               ← staged output of _animator.py ready to SCP
└── JGlimsPlugin/                  ← the actual Gradle project
    ├── build.gradle               ← Paper 26.1.2 alpha + Java 25 + BetterModel 2.1.0 compileOnly
    ├── settings.gradle
    ├── build_rp.py                ← Python zip builder for the resource pack
    ├── fix_items.py               ← audit/repair helper for items/*.json files
    ├── gradle/                    ← Gradle wrapper
    ├── gradlew / gradlew.bat
    ├── BetterModel-3.0.0-SNAPSHOT-487-paper.jar   ← reference copy of BetterModel we deploy
    ├── BETTERMODEL_SETUP_GUIDE.txt
    ├── DEPLOYMENT_STATUS.md       ← last-session deployment record
    ├── RESOURCE_PACK_ADDITIONS.md ← reference list of custom items that need art
    ├── instructions_citadel.txt   ← old spec for the Abyss Citadel
    ├── JGlimsResourcePack.zip     ← latest built resource pack (self-hosted on server)
    ├── merged_pack/               ← resource pack SOURCES
    │   ├── pack.mcmeta            ← pack_format: 84
    │   ├── pack.png
    │   ├── assets/minecraft/
    │   │   ├── items/*.json       ← 58 item-model selector JSONs (1.21.4+ format)
    │   │   ├── models/item/*.json ← 379 model JSONs
    │   │   ├── textures/item/*.png← 553 custom textures
    │   │   └── equipment/         ← armor equipment files
    │   ├── bettermodel_legacy/    ← BetterModel overlay dir (format 22-45)
    │   └── bettermodel_modern/    ← BetterModel overlay dir (format 46-99)
    └── src/main/
        ├── java/com/jglims/plugin/  ← all Java source
        └── resources/plugin.yml
```

## 3. Live server infrastructure (Oracle Cloud)

| Thing | Value |
|---|---|
| Provider | Oracle Cloud free-tier ARM64 VM |
| Public IP | `144.22.198.184` |
| SSH user | `ubuntu` |
| SSH key | `C:\Users\jgmel\Documents\projects\server_minecraft\ssh-key-2026-02-25.key` |
| OS on VM | Linux (kernel 6.17.0-1007-oracle, aarch64) |
| Server dir | `/home/ubuntu/minecraft-server/` |
| Runtime | Docker Compose (itzg/minecraft-server:latest) |
| Container name | `mc-crossplay` |
| Java port | `25565/tcp` |
| Bedrock port | `19132/udp` (Geyser disabled — see §13) |
| Resource pack URL | `http://144.22.198.184:8080/resourcepack/JGlimsResourcePack-merged.zip` (nginx self-host) |
| Resource pack path on server | `/var/www/resourcepack/JGlimsResourcePack-merged.zip` |
| Current pack SHA1 | `8f222bc1d5a9f761a9867b0f83a49becd714f508` |
| Plugin dir on server | `/home/ubuntu/minecraft-server/data/plugins/` |
| BetterModel models on server | `/home/ubuntu/minecraft-server/data/plugins/BetterModel/models/` (63 .bbmodel files) |
| docker-compose.yml | `/home/ubuntu/minecraft-server/docker-compose.yml` |
| Current backups of docker-compose | `.bak`, `.bak-v2`, `.bak-v3-2611`, `.bak-v4-fresh` — sequential snapshots |

### Environment variables (docker-compose.yml)

Key settings the current container runs with:

```yaml
TYPE: PAPER
VERSION: 26.1.2
MEMORY: 8G
ONLINE_MODE: false
DIFFICULTY: normal     # was "peaceful" — fixed 2026-04-12, was killing all hostile mobs
MODE: creative
MAX_PLAYERS: 15
VIEW_DISTANCE: 8       # reduced from 10 for FPS
SIMULATION_DISTANCE: 5 # reduced from 6
USE_AIKAR_FLAGS: true
RESOURCE_PACK: http://144.22.198.184:8080/resourcepack/JGlimsResourcePack-merged.zip
RESOURCE_PACK_SHA1: 8f222bc1d5a9f761a9867b0f83a49becd714f508
RESOURCE_PACK_ENFORCE: TRUE
RESOURCE_PACK_PROMPT: '{"text":"JGlims Server requires a resource pack for custom weapons","color":"dark_purple"}'
```

The `PLUGINS:` auto-download env has been **removed** — Geyser and Floodgate crash on Paper 26.1.x because their shaded Cloud command framework can't resolve `ItemStackParser$ModernParser` via reflection. See §13.

### Installed server plugins

```
BetterModel.jar                (3.0.0-SNAPSHOT-487, swapped in from our own SCP)
Chunky-Bukkit-1.4.40.jar       (declares 26.1.2 support)
JGlimsPlugin-2.0.0.jar         (our plugin)
SkinsRestorer.jar              (15.11.1, works unchanged on 26.1.2)
```

**Removed/not installed**: Geyser-Spigot, floodgate-spigot, LevelledMobs (dir remains but no JAR — not loaded).

## 4. How to build, deploy, verify

**Full deploy cycle**:

```bash
# 1. Build plugin
cd JGlimsPlugin && ./gradlew build

# 2. Build resource pack zip (Python, not PowerShell — Compress-Archive silently drops files)
python3 build_rp.py
sha1sum JGlimsResourcePack.zip   # copy the hash

# 3. Upload plugin JAR
scp -i <key> build/libs/JGlimsPlugin-2.0.0.jar ubuntu@144.22.198.184:/home/ubuntu/minecraft-server/data/plugins/

# 4. Upload resource pack + install
scp -i <key> JGlimsResourcePack.zip ubuntu@144.22.198.184:/tmp/rp.zip
ssh -i <key> ubuntu@144.22.198.184 "sudo mv /tmp/rp.zip /var/www/resourcepack/JGlimsResourcePack-merged.zip && sudo chown www-data:www-data /var/www/resourcepack/JGlimsResourcePack-merged.zip"

# 5. Update SHA1 in docker-compose
ssh -i <key> ubuntu@144.22.198.184 "sed -i 's|RESOURCE_PACK_SHA1: .*|RESOURCE_PACK_SHA1: <NEW_SHA1>|' /home/ubuntu/minecraft-server/docker-compose.yml"

# 6. Restart container
ssh -i <key> ubuntu@144.22.198.184 "cd /home/ubuntu/minecraft-server && docker compose restart"

# 7. Verify
ssh -i <key> ubuntu@144.22.198.184 "sleep 45 && docker logs mc-crossplay 2>&1 | grep -E 'JGlimsPlugin|Done \\(|error' | tail -20"
```

**Reset worlds** (completely fresh start):

```bash
ssh -i <key> ubuntu@144.22.198.184 "cd /home/ubuntu/minecraft-server && docker compose down && sudo rm -rf data/world data/world_nether data/world_the_end data/world_abyss data/world_aether data/world_lunar data/world_jurassic && docker compose up -d"
```

## 5. Java package architecture

```
com.jglims.plugin/
├── JGlimsPlugin.java                  ← main class: extends JavaPlugin, implements TabCompleter + Listener
├── abyss/                             ← Abyss dimension + Citadel + Dragon boss
│   ├── AbyssDimensionManager          ← world creation, spawn point, vanilla-End scrubber
│   ├── AbyssChunkGenerator            ← custom terrain generator
│   ├── AbyssCitadelBuilder            ← the massive gothic cathedral builder
│   ├── AbyssDragonBoss                ← fight controller, event-driven arena trigger
│   └── AbyssDragonModel               ← BetterModel integration, hitbox sizing
├── blessings/                         ← C-Bless / Ami-Bless / La-Bless player blessings
├── config/
├── crafting/
│   ├── RecipeManager                  ← all custom recipes (simplified after Battle+Super removal)
│   ├── VanillaRecipeRemover
│   ├── CraftedItemManager             ← 15 crafted items (fang dagger, void scepter, etc.)
│   └── CraftedItemListener            ← functional effects
├── custommobs/
│   ├── CustomMobType                  ← 72 mob enum entries
│   ├── CustomMobEntity                ← base class (now caps HP at 2048 — Paper limit)
│   ├── CustomBossEntity               ← base for bosses with phases + boss bar
│   ├── CustomMobFactory               ← enum→subclass switch (all 72 mapped)
│   ├── CustomMobManager               ← spawn/despawn registry
│   ├── CustomMobSpawnManager          ← natural spawning (Jurassic dinos boosted 4x)
│   ├── CustomMobListener              ← damage/death routing
│   ├── DropItemManager                ← 13 mob drop items (CMDs 41001-41013)
│   ├── mobs/*                         ← 60+ individual mob classes
│   └── MobCategory                    ← PASSIVE/HOSTILE/NEUTRAL/NPC/BOSS/WORLD_BOSS/EVENT_BOSS/RIDEABLE/CONSTRUCTIBLE
├── dimensions/
│   ├── DimensionPortalManager
│   ├── AetherDimensionManager
│   ├── LunarDimensionManager
│   └── JurassicDimensionManager
├── enchantments/
│   ├── CustomEnchantManager           ← 64 custom enchantments
│   ├── EnchantmentType                ← enum of all 64
│   ├── EnchantmentEffectListener      ← applies enchant effects on-hit, on-tick, etc.
│   └── AnvilRecipeListener            ← 62 anvil recipes for applying enchants
├── events/                            ← 6 random world events (Blood Moon, Void Collapse, End Rift, etc.)
├── guilds/                            ← guild system
├── legendary/
│   ├── LegendaryWeapon                ← enum of 63 weapons across 5 tiers
│   ├── LegendaryWeaponManager         ← creates weapon items with CustomModelDataComponent
│   ├── LegendaryArmorSet              ← 13 armor sets
│   ├── LegendaryArmorManager
│   ├── LegendaryAbilityListener       ← legendary weapon primary/alt abilities
│   ├── LegendaryAltAbilities
│   ├── LegendaryLootListener          ← boss drops + vanilla chest injection
│   ├── InfinityStoneManager           ← 6 Infinity Stones
│   └── InfinityGauntletManager        ← Gauntlet + Thanos Glove
├── magic/
│   ├── MagicItemManager               ← Wand of Wands
│   └── MagicItemListener
├── menu/
│   ├── CreativeMenuManager            ← /jglims menu GUI (with Battle+Super categories REMOVED 2026-04-12)
│   └── GuideBookManager               ← Portuguese guide books
├── mobs/                              ← Blood Moon, Roaming Bosses, etc. (legacy but still used)
├── powerups/                          ← 7 permanent power-ups
├── quests/                            ← 11 quest lines, NPC wizard
├── structures/
│   ├── StructureType                  ← 72 structure enum entries
│   ├── StructureManager               ← dispatches via StructureRegistry
│   ├── StructureBuilder               ← primitive library (fillBox, gothicArch, dome, spire, …)
│   ├── StructureBossManager
│   ├── StructureLootPopulator         ← tier-scaled chest loot + FORCE-places chest if overwritten
│   └── builders/                      ← ONE CLASS PER STRUCTURE (all 72 migrated)
│       ├── IStructureBuilder          ← interface
│       ├── StructureRegistry          ← EnumMap<StructureType, IStructureBuilder>
│       └── 72 concrete builder classes
├── utility/                           ← misc listeners (sort, villager trades, loot boost, pale-garden fog)
├── vampire/
│   ├── VampireState                   ← per-player state
│   ├── VampireLevel                   ← FLEDGLING → DRACULA tier enum
│   ├── VampireManager                 ← transform, items, blood/evolver/superblood/ring
│   ├── VampireListener                ← day/night task, item consumption (FIXED 2026-04-12 to block placement)
│   ├── VampireAbilityManager          ← 5-slot ability tier system (900+ LOC)
│   └── VampireAbilityListener         ← ability GUI + activation
├── weapons/                           ← (dramatically reduced 2026-04-12)
│   ├── SickleManager                  ← 5-tier sickle weapons
│   ├── SpearManager                   ← vanilla spear items with PDC tiers
│   └── WeaponMasteryManager           ← weapon mastery XP tracking
└── werewolf/                          ← NEW 2026-04-11
    ├── WerewolfState
    ├── WerewolfManager                ← infection, transformation, shadow wolf, stat buffs
    └── WerewolfListener               ← event wiring + 4Hz shadow-wolf ticker
```

**REMOVED 2026-04-12** (these Java files were deleted):

- `weapons/BattleAxeManager.java`
- `weapons/BattleBowManager.java`
- `weapons/BattleMaceManager.java`
- `weapons/BattlePickaxeManager.java`
- `weapons/BattleShovelManager.java`
- `weapons/BattleSpearManager.java`
- `weapons/BattleSwordManager.java`
- `weapons/BattleTridentManager.java`
- `weapons/SuperToolManager.java`
- `weapons/WeaponAbilityListener.java`

The user considered Battle + Super tools redundant with the 63-weapon legendary system. Enchantments now apply to vanilla and legendary weapons only. Sickle + Spear remain as standalone systems.

## 6. Commands

### `/jglims <subcommand>` (primary admin command, permission `jglims.admin` / OP)

| Subcommand | Description |
|---|---|
| `reload` | Reload config.yml |
| `stats <player>` | Show blessing + power-up stats |
| `enchants` | List all 64 custom enchantments |
| `sort` | Info about shift-click inventory sorting |
| `mastery` | View weapon mastery progress |
| `legendary <id|list|tier>` | Give/list legendary weapons |
| `armor <set|list> [slot]` | Give/list legendary armor |
| `powerup <type|stats> [player]` | Give/view power-ups |
| `bosstitles` | View boss mastery titles |
| `quests` | Show quest progress |
| `menu` | Open creative menu GUI |
| `guia` | Receive Portuguese guide books |
| `gauntlet <glove|gauntlet|stone|fragment>` | Give Infinity items |
| `abyss <key|tp|boss>` | Abyss dimension commands |
| `vampire <player> <set|remove|info|reset|essence>` | Vampire management |
| `werewolf <blood|infect|ability> [player]` | Werewolf management (NEW 2026-04-11) |
| `locate <STRUCTURE_TYPE>` | Force-build a structure 30 blocks in front of you + TP |
| `tp <overworld|nether|end|abyss|aether|lunar|jurassic>` | Teleport to a dimension's spawn |
| `spawn <MOB_TYPE> [amount]` | Spawn a custom mob at crosshair |
| `boss <BOSS_NAME>` | Spawn a boss at crosshair |
| `help` | Show help |

**Tab completion** is wired for every subcommand via `TabCompleter` interface implemented on the plugin class.

### `/guild <subcommand>`

`create <name>`, `invite <player>`, `join`, `leave`, `kick <player>`, `disband`, `info`, `list`.

### `/guia` (alias `/guide`)

Gives the Portuguese guide book volumes.

## 7. Key systems — current state

### 7.1 Resource pack (self-hosted)

- **Built via `build_rp.py` (Python zipfile)** — **do not use PowerShell Compress-Archive**, it silently drops directories (this caused the items/ directory to be missing from earlier builds).
- Served via nginx on port 8080 of the Oracle VM.
- `pack.mcmeta` declares `pack_format: 84`, `supported_formats: [34, 84]`, with two BetterModel overlays.
- **58 `items/*.json`** — Paper 1.21.4+ item-model selector format. Uses `{"type":"minecraft:select","property":"minecraft:custom_model_data","index":0,"cases":[{"when":"..."}],"fallback":{...}}`.
- **Critical learning**: all numeric-when cases **MUST** include `"index": 0` or Paper 26.1.2 fails to parse the file, and everything using that base material falls back to vanilla texture rendering (but may show magenta in edge cases). Fixed 2026-04-12 — 25 files were missing index.
- BOM stripping — 26 files had UTF-8 BOMs that made Python's json.load fail. Stripped 2026-04-12.

### 7.2 Custom model data conventions

| CMD range | System |
|---|---|
| 30000–30099 | Legendary weapons (via `setStrings([id])` on the CustomModelDataComponent) |
| 40001–40005 | Vampire consumables (via deprecated `setCustomModelData(int)` — sets `floats[0]`) |
| 41001–41013 | Mob drop items |
| 42001–42015 | Crafted items |
| 43001–43002 | Werewolf items (wolf form ability, werewolf blood) |
| 50001–50005 | Vampire ability icons |

### 7.3 Custom mobs (72 total)

- Base class `CustomMobEntity` spawns a hitbox entity (Zombie by default), attaches a BetterModel renderer via `kr.toxicity.model.api.BetterModel.model(modelName)`, runs a per-2-tick (10 Hz) update loop.
- **HP cap**: `Math.min(designHp, 2048.0)` — Paper's `Attribute.MAX_HEALTH` hard-caps at 2048. Godzilla (3000) and Ghidorah (2800) fall back to 2048. Design-HP-above-cap bosses should use the damage-reduction system in `CustomBossEntity` for effective HP bloat.
- **Exclusion list** (8 mob types with no model, won't render visually): `LOW_POLY_TROLL, NECROMANCER, DEMON_GUY, PRISMAMORPHA, OBSERVER_GOLEM, WILDFIRE, STONE_GOLEM, GENERAL_PIGLIN`. They still function (spawn, attack, drop loot), just invisible.
- **SHARK removed 2026-04-11** — the bbmodel couldn't be rigged; mob was deleted entirely from the enum, factory, spawn rules, drops, and animator.
- 63 bbmodel files deployed to the server's `BetterModel/models/` directory. Most have animations generated by `bbmodels/_animator.py` (371 animations across 3109 bones).

### 7.4 Abyss Dragon boss — current state

- Hitbox switched from Zombie → **Giant** (12 blocks tall) on 2026-04-12 so players can actually land hits.
- `BASE_SCALE = 2.0` (was 3.0, reduced to fit the 55-block arena).
- Arena radius = 55 blocks.
- Visual model: `abyss_dragon.bbmodel` — **known issue**: disconnected leg segments and black-rectangle UV errors on horns. Cannot be fixed from Java; requires manual Blockbench rigging or a replacement model.
- **Auto-trigger**: `PlayerMoveEvent` in `AbyssDragonBoss` detects arena entry and calls `startFight()`. 30-min cooldown between attempts.
- **Vanilla End dragon block**: `CreatureSpawnEvent` listener cancels any `ENDER_DRAGON` spawn in world_abyss at lowest priority. Post-load scrubber also removes obsidian altar towers, End crystals, exit portal, gateway within radius 30 of world spawn.
- Abyss world spawn set to `(0, 67, 90)` — open ground 30 blocks in front of the citadel facade.

### 7.5 Structures — one file per structure

Completed **2026-04-11**:

- All 72 structures live in `structures/builders/<Name>Builder.java`.
- `StructureRegistry` (EnumMap) dispatches each `StructureType` to its builder.
- `StructureManager.buildStructure()` is a one-line registry lookup.
- The old monolithic `OverworldStructureBuilders.java` (132 KB) and `DimensionStructureBuilders.java` (176 KB) are **deleted**.
- 11 previously BASIC-tier structures were rewritten from scratch to EXCELLENT quality: AncientTemple, SkyAltar, MonsterIsland, DragonsHoard, DragonDeathChest, SkeletonDragonLair, VolcanicForge, TarPit, AetherVillage, CrystalCavern, PitlordLair.
- Every other structure was extracted verbatim with light touches.
- The Abyss Citadel has a new projecting entrance porch (16 blocks deep, gothic arches, flying buttresses to pylon columns, peaked roof).
- The Citadel nave roof was fixed — previously only the edge lines of each slope layer were placed, leaving a lattice with holes. Now a solid triangular cross-section.
- Citadel guard count reduced from 52 to 24 (55% cut).

### 7.6 Vampire system

- Progression: Fledgling → Vampire → Elder Vampire → Vampire Lord → Dracula, driven by Vampire Blood / Vampire Evolver / Super Blood consumption counters.
- 5 ability slots evolve by tier: `vampire_claw` (always), `vampire_bite` (Vampire+), `bat_ability` (Elder+), `blood_nova` (Lord+), `domain_of_night` (Dracula only).
- Abilities equipped from a 3-row GUI opened via Sneak+F (swap-hand while sneaking).
- **Innate passive (added 2026-04-10)**: 50% chance to mind-manipulate a villager into giving the selected trade result for free. Hook: `InventoryClickEvent` on slot 2 of a `MerchantInventory`.
- **Bat Form ability tier 3 (improved 2026-04-11)**: player becomes invisible for 8 seconds, creative-style flight enabled, a shadow Bat entity glued to their location so others see a bat. Plus a 20-damage AoE on-activation.
- **Vampire Blood et al. item fix (2026-04-12)**: the `PlayerInteractEvent` handler now cancels placement/use regardless of vampire status and shows the player an action-bar message if they're not a vampire. Before this fix, Vampire Blood was placeable on the ground as redstone.

### 7.7 Werewolf system (added 2026-04-11)

- **Infection**: consume Werewolf Blood (REDSTONE, CMD 43002). Guaranteed drop from the Blood Moon King boss.
- **Ability**: one hotbar item (BONE, CMD 43001) locked in on infection. Right-click toggles wolf form.
- **In wolf form**: player becomes invisible, armor auto-stored, Night Vision + Speed I + Strength I + 6 extra hearts applied as attribute modifier. A tamed Wolf (red collar, owned by the player, invulnerable, silent) is spawned and teleported to the player every 5 ticks.
- **Day/night gating**: requires night time in worlds with `DO_DAYLIGHT_CYCLE = true`. Usable any time in worlds without a day/night cycle (Abyss, Aether, Lunar). Auto-reverts if the player is in wolf form when daytime arrives.
- Admin subcommand: `/jglims werewolf <blood|infect|ability> [player]`.
- Vampires are blocked from becoming werewolves ("Your vampire blood rejects the werewolf curse").

### 7.8 Custom enchantments (64)

Applied at the anvil with 62 recipes. Event-driven effects in `EnchantmentEffectListener`. Post-Battle+Super removal, enchantments route through vanilla material-name checks and legendary PDC markers only.

### 7.9 Custom dimensions

- `world_abyss` (THE_END env) — Abyss Citadel + Dragon boss + gothic terrain via `AbyssChunkGenerator`.
- `world_aether` (THE_END env) — sky islands + storm peaks.
- `world_lunar` (NORMAL env) — moon base + Invaderlings.
- `world_jurassic` (NORMAL env) — dinosaur valley + Bone Arena + Dinobot Arena.
- Portals via `DimensionPortalManager` — purpur 3×5 frames activated by interaction.

### 7.10 Spawn guards (added 2026-04-12)

Two event handlers on the main plugin class:

- `PlayerJoinEvent`: if the player joins in any world other than `world`, teleport them to overworld spawn on the next tick.
- `PlayerRespawnEvent`: if the respawn location is in a custom dimension, redirect to overworld spawn.

Previously, first-join and death-respawn could land players in the Jurassic dimension because of a race condition between plugin init and Paper's default-world selection.

## 8. Known bugs / open issues (2026-04-12)

| Issue | Severity | Status |
|---|---|---|
| Ender dragon persisting in Abyss (maybe — pending in-game test after CreatureSpawnEvent block) | HIGH | Probably fixed; verify |
| Abyss dragon hitbox too small (maybe — pending test after Zombie→Giant swap) | HIGH | Probably fixed; verify |
| Abyss dragon model visual glitches (disconnected legs, black UV rectangles on horns) | MEDIUM | Needs Blockbench rework or replacement model |
| Some custom mobs render as "floating horns" — BetterModel 3.0 partial model load on specific bbmodel files | MEDIUM | Needs per-bbmodel rigging audit (NotebookLM candidate) |
| Crossplay broken (Geyser + Floodgate incompatible with Paper 26.1.x due to shaded Cloud command framework `ItemStackParser` reflection failure) | HIGH | Waiting for upstream Geyser/Floodgate release |
| ~20 custom item textures never created (vampire ability icons + crafted item art) — currently fall back to vanilla models | LOW | Waiting for user-generated PNGs |
| 4 WEAK-tier bosses with minimal abilities (Ogrin Giant, Wildfire, Shadowsail, Wither Storm) | MEDIUM | Future pass |
| 6 roaming bosses use vanilla entity AI (stat-sticks) | LOW | Future pass |

## 9. Texture status

**Present and working** (after 2026-04-12 pack_format + items/*.json fixes):

- All 63 legendary weapons' PNGs exist
- All 13 armor sets
- All 13 mob drop items
- All 5 vampire consumables
- All 6 Infinity items
- 7 power-ups
- 3 blessings
- Wand of Wands
- Most BetterModel mob textures

**Genuinely missing (user-generated needed)**:

Vampire ability icons (5): `blood_nova.png`, `bat_transform.png`, `domain_of_night.png`, `crimson_mark.png`, `dracula_wrath.png`

Crafted items (15): `blood_chalice.png`, `fang_dagger.png`, `troll_helmet/chestplate/leggings/boots.png` (4), `lunar_blade.png`, `raptor_gauntlet.png`, `shark_tooth_necklace.png`, `tremor_shield.png`, `void_scepter.png`, `soul_lantern_item.png`, `dinosaur_bone_bow.png`, `crimson_elixir.png`, `nightwalker_cloak.png`

These items currently fall back to their vanilla base-material textures via the `"fallback"` field in the items/*.json selector. Functional but not visually distinctive.

## 10. Structure quality — baseline for the NotebookLM rework

All 72 structures in `structures/builders/`:

### Rewritten to high quality (11 — target for comparison)

AncientTempleBuilder, SkyAltarBuilder, MonsterIslandBuilder, DragonsHoardBuilder, DragonDeathChestBuilder, SkeletonDragonLairBuilder, VolcanicForgeBuilder, TarPitBuilder, AetherVillageBuilder, CrystalCavernBuilder, PitlordLairBuilder.

### Already GOOD/EXCELLENT (extracted verbatim, retain quality)

RuinedColosseum, GiganticCastle, UltraVillage, Fortress, ShrekHouse, PillagerFortress, MageTower, DungeonDeep, Forge, JapaneseTemple, CrimsonCitadel, SoulSanctum, BasaltSpire, NetherDungeon, PiglinPalace, WitherSanctum, BlazeColosseum, NetherKingsCastle, DemonFortress, VoidShrine, EnderMonastery, EndRiftArena, GleeokArena, IllidanPrison, AbyssalCastle, VoidNexus, ShatteredCathedral, StormPeakTower, WarriorSkyFortress, BoneArena, DinobotArena.

### Candidates for full NotebookLM rework (simpler existing quality)

CampingSmall, CampingLarge, AbandonedHouse, WitchHouseForest, WitchHouseSwamp, HouseTree, DruidsGrove, AllaySanctuary, Volcano, ThanosTemple, PillagerAirship, FrostDungeon, BanditHideout, SunkenRuins, CursedGraveyard, OgrinHut, FairyGlade, WerewolfDen, NecromancerDungeon, DragonkinTemple, AetherAncientRuins, InvaderlingOutpost, UndergroundHive, AlienCitadel, MeteorCrashSite, ObservationTower, LunarBase, RaptorNest, WateringHole, NestingGround.

## 11. Structure rework plan — NotebookLM workflow

This is the **major upcoming phase**. The user has a subscription to NotebookLM and plans to feed it high-quality Minecraft building tutorial videos. Based on those videos, NotebookLM should generate block-by-block construction guides that Claude can translate directly into `StructureBuilder` Java code.

### Preferred output format from NotebookLM (for Claude to consume)

Format A — preferred: structured layer-by-layer plans. Example:

```
=== STRUCTURE: CRIMSON CATHEDRAL ===
Dimension: Nether
Footprint: 60 × 80
Height: 55
Base tier: LegendaryTier.EPIC
Boss: Crimson Warlord (at altar)

Layer 1 (y = 0, foundation):
  Fill 60×80 rectangle of polished_blackstone_bricks
  Radiating from center, place crying_obsidian at every 8th block

Layer 2-4 (y = 1 to 3, stoop):
  3 steps ascending toward the south face, 1 block inset per step
  Material: polished_blackstone

Walls (y = 4 to 25):
  Perimeter walls of red_nether_bricks, 2 blocks thick
  Vertical pilaster columns every 8 blocks, material: nether_bricks
  Window openings at y = 10 to 14, 2 wide, every other pillar pair

Nave columns (interior):
  8 columns at x/z positions: (-24, -30), (-24, -15), (-24, 0), (-24, 15),
    (+24, -30), (+24, -15), (+24, 0), (+24, 15)
  Height y = 4 to 22
  Materials: chiseled_nether_bricks (base/cap) + nether_brick_pillar (shaft)

Gothic arches (connecting column pairs across the nave):
  Span: 48 blocks
  Rise: 8 blocks above column top (y = 22 → y = 30 at center)
  Material: red_nether_brick_stairs

Rose window (front facade):
  Center at x=0, y=40, z=-40 (south face)
  Radius: 8 blocks
  Construction: parametric — magenta_stained_glass interior, radiating spokes
    of crying_obsidian every 30°, amethyst_block border ring

Flying buttresses (4 per side):
  Origin: column top (y=22)
  Arch outward + down to ground at distance 8 from the wall
  Material: nether_bricks shaft, chiseled_nether_bricks tips

Chests (loot):
  Main altar: (0, 3, 35) — MYTHIC tier
  Side chapels: (-20, 3, 20), (20, 3, 20), (-20, 3, -20), (20, 3, -20) — EPIC tier
  Crypt: (0, -8, 0) — LEGENDARY tier

Boss spawn: (0, 4, 0)
```

Claude translates this directly into a `StructureBuilder` implementation using primitives like `b.fillBox`, `b.gothicArch`, `b.dome`, `b.spire`, `b.flyingButtress`, `b.roseWindow`, `b.spiralStaircase`, `b.battlements`, `b.scatter`, `b.decay`, `b.addVines`, `b.placeChest`, `b.setBossSpawn`.

Format B (secondary): WorldEdit `.schem` files — Claude can write a converter that reads the schematic and emits setBlock calls. More work, but preserves every block exactly.

Format C (fallback): prose descriptions with precise vocabulary. Works but involves creative interpretation.

### StructureBuilder primitives reference (for NotebookLM prompt context)

```
setBlock(x, y, z, material)
setBlockIfAir(x, y, z, material)
setRandomBlock(x, y, z, material...)
setDirectional(x, y, z, material, BlockFace)
setSlab(x, y, z, slabMaterial, topHalf)
setStairs(x, y, z, stairMaterial, facing, upsideDown)
fillBox(x1, y1, z1, x2, y2, z2, material)
fillWalls(x1, y1, z1, x2, y2, z2, material)
hollowBox(x1, y1, z1, x2, y2, z2)
fillFloor(x1, z1, x2, z2, y, material)
pillar(x, y1, y2, z, material)
circle(cx, y, cz, radius, material)
filledCircle(cx, y, cz, radius, material)
filledEllipse(cx, y, cz, radiusX, radiusZ, material)
gothicArch(cx, baseY, cz, width, height, material)
archDoorway(cx, baseY, cz, width, height, material)
dome(cx, baseY, cz, radius, material)
filledDome(cx, baseY, cz, radius, material)
gabledRoof(x1, z1, x2, z2, baseY, material, slabMat)
pyramidRoof(x1, z1, x2, z2, baseY, material)
spire(cx, baseY, cz, baseRadius, height, material)
spiralStaircase(cx, y1, y2, cz, radius, stairMat, pillarMat)
gothicWindow(cx, baseY, cz, height, glassMat, facing)
roseWindow(cx, cy, cz, radius, glassMat, facing)
flyingButtress(x1, y1, z1, x2, y2, z2, material)
battlements(x1, y, z1, x2, z2, material)
table(x, y, z)
chair(x, y, z, stairMat, facing)
chandelier(x, ceilingY, z, chainLength)
banner(x, y, z, bannerMat)
bookshelfWall(x1, y1, z, x2, y2)
scatter(x1, y1, z1, x2, y2, z2, targetMat, replacementMat, chance)
decay(x1, y1, z1, x2, y2, z2, chance)
addVines(x1, y1, z1, x2, y2, z2, chance)
wallLighting(x1, y, z1, x2, z2, spacing, lightSource)
gradientPillar(x, y1, y2, z, bottomMat, topMat)
roundTower(cx, baseY, cz, radius, height, wallMat, roofMat, glassMat)
furnishedRoom(x1, y, z1, x2, z2, height, wallMat, floorMat, ceilMat)
placeChest(x, y, z) → Location
setBossSpawn(x, y, z)
getSurfaceY(x, z)
isTerrainFlat(x1, z1, x2, z2, maxDiff)
isAboveWater(x, z)
```

### Workflow for each structure

1. User feeds tutorial video to NotebookLM
2. NotebookLM outputs Format A block-by-block plan
3. User sends plan to Claude
4. Claude creates/rewrites `structures/builders/<Name>Builder.java`
5. Build → deploy → `/jglims locate <STRUCTURE_NAME>` in-game
6. Iterate on anything that looks off

### Starting with ONE structure (recommended first test)

Before batch-producing 30+ plans, run one through the full workflow end-to-end to validate the Format A → Java translation and catch any ambiguity in the format. Suggested: Ancient Temple or a simpler non-boss structure like Druid's Grove.

## 12. How to continue this project in a fresh Claude session

After `/clear`:

1. Paste this README into Claude.
2. Tell Claude what you're working on (e.g., "I have NotebookLM output for the Crimson Citadel, please rewrite its builder").
3. Claude will read the relevant builder class, apply the plan, build the project, and deploy.

Important context Claude needs:

- SSH key location
- Server IP (144.22.198.184)
- Current SHA1 of the resource pack (if deploying)
- Whether to reset worlds or keep them

## 13. Known incompatibilities — waiting on upstream

### Geyser + Floodgate on Paper 26.1.x

Both plugins crash on `onEnable` with the same stack trace:

```
Caused by: com.google.inject.ProvisionException:
  at CommandModule.configure(CommandModule.java:42)
  at org.incendo.cloud.bukkit.BukkitParsers.register(BukkitParsers.java:75)
  at org.incendo.cloud.bukkit.parser.ItemStackParser.itemStackParser
  at CraftBukkitReflection.firstNonNullOrThrow(CraftBukkitReflection.java:125)
```

Both plugins bundle a shaded copy of the Cloud command framework. Cloud's `LegacyPaperCommandManager.ItemStackParser$ModernParser` uses reflection to locate an ItemStack API method that doesn't exist in Paper 26.1.x (the API was changed upstream). No Floodgate build past b131 (2026-03-21) is available.

**Expected resolution**: Geyser/Floodgate release 26.1.x-compatible builds (typically days to a week after a major Minecraft version). When they do, the restore is 3 files:

```bash
scp -i <key> <new_geyser>.jar ubuntu@144.22.198.184:/home/ubuntu/minecraft-server/data/plugins/Geyser-Spigot.jar
scp -i <key> <new_floodgate>.jar ubuntu@144.22.198.184:/home/ubuntu/minecraft-server/data/plugins/floodgate-spigot.jar
ssh -i <key> ubuntu@144.22.198.184 "cd /home/ubuntu/minecraft-server && docker compose restart"
```

### World format migration

When Paper 26.1.2 ran on an existing 1.21.11 world, it **upgraded `level.dat`** into a format 1.21.11 cannot read. A rollback to 1.21.11 requires wiping the world. For the current server, the worlds are 26.1.2-format and cannot be downgraded.

## 14. Future work — rough roadmap

### Near-term (next 1-3 sessions)

1. **Structure rework with NotebookLM** — batch-convert tutorial-derived plans into builders. Target: 20+ structures reworked to breathtaking quality.
2. **Generate the 20 missing custom texture PNGs** (NotebookLM can describe the art; user generates via AI or manual pixel art).
3. **Replace Abyss Dragon bbmodel** with a better-rigged model (free Modrinth dragon model + re-use our animation definitions).
4. **Strengthen 4 WEAK bosses** — Ogrin Giant, Wildfire, Shadowsail, Wither Storm.
5. **Upgrade 6 roaming bosses** from vanilla AI to CustomBossEntity subclasses.

### Mid-term (gameplay systems)

6. **Dimension invasion events** — rare "your dimension is under attack" with mobs from Abyss pouring into overworld.
7. **Transmutation altar** — convert excess mob drops between types.
8. **Legendary weapon awakening trees** — 3-branch evolution per weapon, unlocks True Form.
9. **Soul Shards currency** — tradable at NPC Wizard for stat upgrades, titles, ability slots.
10. **Party/raid system** separate from guilds — ad-hoc combat groups with shared XP/loot.

### Long-term

11. **Terraform or similar world generator** — currently blocked on Paper 26.1.x compatibility (same ecosystem issue as Geyser).
12. **Seasonal event cycle** — Halloween Blood Moon week, Winter Frostmaw invasions, etc.
13. **Custom music tracks per boss** (resource pack sound events).
14. **Boss raid difficulty tiers** — normal/hard/mythic sliders before summoning world bosses.

## 15. Recent session log (condensed)

- **2026-04-09**: Massive 6-phase expansion — 73 custom mobs, 126 legendary abilities, 73 structure types, 11 quest lines, Ripper Zombie, Realistic Dragon, Infinity Gauntlet rework.
- **2026-04-10**: 62 models animated by `_animator.py`. 6 boss arenas rebuilt to high quality.
- **2026-04-11 (session A)**: Structure rework — 72 structures migrated to one-class-per-structure. 11 rewritten from scratch. 58 item JSON fixes. Werewolf system built from scratch. Vampire innate trade manipulation. 4 new `/jglims` subcommands (locate/tp/spawn/boss). Creative menu expanded by 4 categories.
- **2026-04-11 (session B)**: Server upgraded to Paper 26.1.2 + Java 25. BetterModel 2.1.0 → 3.0.0. Geyser + Floodgate removed (ecosystem compatibility). SHARK mob fully purged.
- **2026-04-11 (session C)**: Pack format 75→84. Items/*.json created for all custom item base materials. Abyss vanilla End scrubber. Abyss dragon scale 3.0→2.0. Jurassic spawn rates 4x boost. Difficulty peaceful→normal on 2026-04-12.
- **2026-04-12 (this session)**:
  - Fixed critical items/*.json bug (25 files missing `"index": 0`, 26 BOMs stripped)
  - Capped boss HP at 2048 (Paper's attribute max) — fixes Godzilla IllegalArgumentException
  - Vampire Blood no longer placeable as redstone
  - Abyss Dragon hitbox Zombie → Giant (hittable now)
  - CreatureSpawnEvent cancels vanilla ender dragon in Abyss
  - Respawn redirect — dying in any custom dim sends player back to overworld
  - First-join guard strengthened — ANY player joining outside overworld is teleported to world spawn
  - Deleted Battle+Super weapons systems (10 files), refactored JGlimsPlugin/RecipeManager/CreativeMenu/EnchantmentEffectListener/WeaponMasteryManager
  - Cleaned ~15 trash files from the repo (old zips, backup scripts, old resource pack dirs, `.bak.bak`, old compiled outputs)

---

_End of README. Safe to `/clear` and paste this back when resuming._

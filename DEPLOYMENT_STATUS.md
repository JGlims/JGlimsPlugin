# JGlimsPlugin — Deployment Status (Session 2)

_Last updated: 2026-04-11 — after live deployment_

## Live Status: ✅ DEPLOYED &amp; RUNNING

| Item | Status |
|---|---|
| JGlimsPlugin-2.0.0.jar | ✅ on server |
| 63 bbmodel files | ✅ in BetterModel/models |
| JGlimsResourcePack-merged.zip | ✅ 40 MB at nginx, SHA1 matches |
| docker-compose RESOURCE_PACK_SHA1 | ✅ updated → `d90d8b286859cab92ee0ba82e85c7fedcf1a40c4` |
| mc-crossplay container | ✅ healthy |
| JGlimsPlugin v2.0.0 | ✅ enabled in 14073 ms |
| 72 structure types loaded | ✅ |
| 64 custom enchantments | ✅ `/jglims enchants` responds in-game |
| Abyssal Citadel auto-populated | ✅ 6 regular chests + 4 abyssal weapon chests + guards |

### Server log excerpt (post-restart)
```
[JGlimsPlugin] Legendary weapon system loaded: 63 weapons across 5 tiers.
[JGlimsPlugin] Structure generation system loaded (72 structure types).
[JGlimsPlugin] Roaming boss scheduler started
[JGlimsPlugin] Quest NPC scheduler started (11 quest lines).
[JGlimsPlugin] JGlimsPlugin v2.0.0 enabled in 14073ms!
[JGlimsPlugin] [Citadel] Populating chests with loot...
[JGlimsPlugin] [Citadel] Found 6 regular chests and 4 abyssal weapon chests.
[JGlimsPlugin] [Citadel] Spawning guards...
```

The plugin is production-ready and responding to commands. Join the server and test.

---

## Session 2 Changes

### 1. Structure rework — COMPLETE
- **72/72 structures** now live in individual builder classes under
  `com.jglims.plugin.structures.builders.*`
- Monolithic `OverworldStructureBuilders.java` (132 KB) and
  `DimensionStructureBuilders.java` (176 KB) have been **deleted**
- `StructureManager.buildStructure()` is a one-line registry dispatch
- 11 were rewritten from scratch to high quality
- 61 were extracted verbatim (their existing quality is preserved)
- New `StructureManager.forceBuild(type, origin)` admin helper

### 2. SHARK mob — removed
- Enum entry, factory case, spawn rule, drop mapping, `SharkMob.java`,
  `shark.bbmodel`, and animator entry all purged

### 3. Vampire innate mind-manipulation ability — added
- `VampireListener.onVampireMerchantClick` — 50% chance when a vampire
  clicks the result slot of a villager/NPC trade to take the item for
  free (event cancelled, item added directly to inventory)
- Feedback: action bar, enderman ambient sound, soul + dust particles

### 4. /jglims command rework
New subcommands, all OP-gated, all tab-complete:

| Command | Effect |
|---|---|
| `/jglims locate <STRUCTURE_TYPE>` | Force-builds the named structure 30 blocks in front of you and teleports you there |
| `/jglims tp <dim>` | Teleports to overworld/nether/end/abyss/aether/lunar/jurassic spawn |
| `/jglims spawn <MOB_TYPE> [amount]` | Spawns a custom mob at your crosshair (up to 20) |
| `/jglims boss <BOSS_NAME>` | Spawns any boss at your crosshair (validates category), special path for `ABYSS_DRAGON` via `manualTrigger` |

Also added `TabCompleter` implementation with suggestions for structures,
mobs, bosses, dimensions, online player names, gauntlet subtypes, etc.

### 5. Creative menu rework
Added 4 new categories with openers, click handlers, and give helpers:

- **Itens Criados** (slot 11): 15 crafted items (Lunar Blade, Fang Dagger,
  Void Scepter, Troll armor set, etc.)
- **Drops de Mobs** (slot 13): 13 mob drop materials (Troll Hide, Lunar
  Fragment, Shark Tooth, Bear Claw, etc.)
- **Itens de Vampiro** (slot 15): 5 vampire consumables (Blood, Essence,
  Evolver, Super Blood, Ring)
- **Itens Magicos** (slot 38): Wand of Wands

40+ items previously inaccessible from the GUI are now one click away.

### 6. Resource pack fallback models
10 `models/item/*.json` files created so items with missing PNG textures
fall back to vanilla base-material models (fang_dagger, lunar_blade,
void_scepter, etc.) instead of rendering as missing.

---

## Verification Findings (from parallel audits)

### Bosses — 28 total
- **6 EPIC**: The Warrior (6 abilities, 4 phases), King Gleeok (7 abilities,
  3-head independent system), Godzilla (7 abilities, 5 phases), Ghidorah
  (8 abilities, 3-head regen), Abyss Dragon (custom arena implementation),
  Invaderling Commander (mech transformation)
- **10 GOOD**: Nether King, Pitlord, Illidan, Werewolf, Frostmaw, Javion
  Dragonkin, Skeleton Dragon, Realistic Dragon, Ripper Zombie, T-Rex
- **4 WEAK** ⚠️: Ogrin Giant (no phase system), Wildfire (minimal phase
  escalation), Shadowsail (no phase escalation), Wither Storm (AI disabled,
  passive only) — candidates for future strengthening
- **6 ROAMING** (stat sticks): The Watcher, Hellfire Drake, Frostbound
  Colossus, Jungle Predator, End Wraith, Abyssal Leviathan — use vanilla
  AI only, no custom moveset (future work)

### Loot system — working
- `StructureLootPopulator` fills structure chests with tier-scaled loot
  (vanilla + legendaries + powerups + dimension-specific like Infinity
  fragments)
- `LegendaryLootListener` hooks `LootGenerateEvent` for 18 vanilla
  structures (End City, Bastion, Stronghold, Ancient City, etc.) with
  tier-appropriate weapon pools and custom enchant books (20% chance)
- Boss death drops for EnderDragon, ElderGuardian, Warden, Wither, Blood
  Moon King with per-boss weapon pools and dragon-death special chest at
  world spawn

---

## Manual Work Still Needed

### 1. Generate 20 custom texture PNGs
Items function with vanilla fallbacks, but for polish the user should
create and drop PNGs into `merged_pack/assets/minecraft/textures/item/`:

**Vampire ability icons (5):** blood_nova, bat_transform, domain_of_night,
crimson_mark, dracula_wrath

**Crafted items (15):** fang_dagger, lunar_blade, tremor_shield,
void_scepter, soul_lantern_item, dinosaur_bone_bow, nightwalker_cloak,
troll_helmet, troll_chestplate, troll_leggings, troll_boots, blood_chalice,
raptor_gauntlet, shark_tooth_necklace, crimson_elixir

After dropping PNGs, re-zip `merged_pack/`, upload as
`JGlimsResourcePack-merged.zip` to `/var/www/resourcepack/`, update
docker-compose SHA1, restart container.

### 2. Consider strengthening the 4 WEAK bosses
- **Ogrin Giant**: add a phase system + beefier abilities
- **Wildfire**: add more distinct phase mechanics
- **Shadowsail**: add phase-gated ability escalation
- **Wither Storm**: re-enable AI (currently stationary) and add active attacks

### 3. Consider upgrading the 6 roaming bosses to full CustomBossEntity
They currently use vanilla mob AI — replace with proper ability loops to
match the quality of the structure bosses.

---

## Server connection (for quick reference)

```bash
SSH:  ssh -i <key> ubuntu@144.22.198.184
Key:  C:/Users/jgmel/Documents/projects/server_minecraft/ssh-key-2026-02-25.key
Container:  mc-crossplay
Plugin dir:  /home/ubuntu/minecraft-server/data/plugins/
bbmodels:    /home/ubuntu/minecraft-server/data/plugins/BetterModel/models/
Resource pack URL:  http://144.22.198.184:8080/resourcepack/JGlimsResourcePack-merged.zip
Resource pack SHA1: d90d8b286859cab92ee0ba82e85c7fedcf1a40c4
docker-compose.yml:  /home/ubuntu/minecraft-server/docker-compose.yml
```

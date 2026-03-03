JGlimsPlugin — Comprehensive Project Summary Document
Version: 1.2.0 → planned 1.3.0 Author: JGlims Repository: https://github.com/JGlims/JGlimsPlugin Server: OCI VM (4 OCPU, 24 GB RAM, Ubuntu), Docker container mc-crossplay, PaperMC 1.21.11 Date: March 3, 2026

1. Project Overview
JGlimsPlugin is a custom Paper/Spigot plugin for Minecraft 1.21.11 that adds an extensive layer of custom mechanics on top of vanilla gameplay. The plugin is designed for a crossplay server (Java + Bedrock via Geyser/Floodgate) and emphasizes custom enchantments, a tiered weapon progression system, mob difficulty scaling, economy-adjacent features (loot boosters, blessings), a guild system, and periodic events (Blood Moon). The design philosophy prioritizes performance: never break farms, only alter health/damage/speed on mobs, use lightweight listeners, target TPS 20, and stay under 6 GB of the 8 GB heap.

The plugin is built with Gradle, targets JDK 21, depends on io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT, and uses Mojang mappings. The compiled JAR is deployed via SCP to the server's ~/minecraft-server/data/plugins/ directory.

2. Server Infrastructure
Hardware: OCI VM with 4 OCPUs, 24 GB system RAM, 47 GB boot disk, running Ubuntu.

Docker container mc-crossplay runs itzg/minecraft-server:latest with the following key environment variables: EULA=TRUE, TYPE=PAPER, VERSION=1.21.11, MEMORY=8G, ONLINE_MODE=false, RCON_PASSWORD=JGlims2026Rcon, and PLUGINS pointing to the latest Geyser-Spigot and Floodgate-Spigot downloads from download.geysermc.org. Ports exposed are 25565/TCP (Java), 19132/UDP (Bedrock), and 25575/TCP (RCON).

Installed plugins (as of current status): JGlimsPlugin v1.2.0, Geyser-Spigot v2.9.4, Floodgate v2.2.5, Chunky v1.4.40. SkinsRestorer 15.10.1 is pending installation.

World data resides in ~/minecraft-server/data/ with directories for world, world_nether, and world_the_end.

Deploy workflow: Build on Windows with .\gradlew.bat clean jar, SCP the output JAR from build\libs\JGlimsPlugin-1.2.0.jar to MinecraftServer:~/, SSH in, copy to ~/minecraft-server/data/plugins/, remove any old JAR (e.g., VampirismPlugin-1.0.0.jar), then docker restart mc-crossplay and verify with docker logs mc-crossplay 2>&1 | grep -i jglims.

3. Current Plugin Status — What Is DONE
The following systems are fully implemented and loaded on the server:

Core scaffold: JGlimsPlugin.java (main class), ConfigManager.java (loads all config with defaults), plugin.yml (declares commands /guild and /jglims), config.yml.

Custom Enchantment System: CustomEnchantManager.java registers NamespacedKeys for all 49 custom enchantments, maintains a bidirectional conflict map (VAMPIRISM↔LIFESTEAL, BERSERKER↔BLOOD_PRICE, BLEED↔VENOMSTRIKE, WITHER_TOUCH↔CHAIN_LIGHTNING, EXPLOSIVE_ARROW↔HOMING, VEINMINER↔DRILL, MOMENTUM↔SWIFTFOOT, SEISMIC_SLAM↔MAGNETIZE), and provides utility methods for getting/setting/removing enchants, counting total enchants, listing enchantments, and checking conflicts. EnchantmentType.java is an enum of all 49 enchantments with getMaxLevel() returning 5 for VAMPIRISM/BERSERKER/THUNDERLORD, 1 for AUTO_SMELT/MAGNETISM/REPLENISH/GREEN_THUMB/NIGHT_VISION/CUSHION/GLIDER/SOULBOUND/BEST_BUDDIES, and 3 for everything else.

Anvil Recipes: AnvilRecipeListener.java registers 47 anvil recipes (one per enchantment minus SOULBOUND and BEST_BUDDIES which have special handling). Each recipe maps an EnchantmentType to a required vanilla enchantment, an ingredient material, a level-matching rule, and a max level. The listener handles PrepareAnvilEvent to support creating custom enchanted books, applying those books to tools, soulbound operations, removing the "Too Expensive" cap, reducing XP costs by 50%, checking conflicts (both custom-custom and custom-vanilla), and adding enchantment glint. It also handles InventoryClickEvent to clear anvil slots after custom operations.

Enchantment Effects: EnchantmentEffectListener.java is a large listener implementing all enchantment behaviors. Damage-event enchantments include Berserker (bonus damage at low health), Blood Price (damage costs health), Reaper's Mark (marks target for bonus damage), Vampirism (heals on hit), Lifesteal (percentage heal), Bleed (damage-over-time), Venomstrike (poison), Frostbite (slowness), Wither Touch (wither effect), Chain Lightning (arcs to nearby mobs), Lumberjack (bonus damage to undead), Cleave (AoE damage), Guillotine (bonus headshot damage), and Soul Reap (strength on kill). Defender enchantments include Dodge (chance to negate damage), Deflection (reflect damage), Fortification (damage reduction), Night Vision, Aqua Lungs, Swiftfoot (speed), Leaping (jump boost), and Vitality (health boost). Projectile enchantments: Explosive Arrow, Sniper (distance damage bonus), Thunderlord (lightning on third hit), Tidal Wave (knockback wave), Homing (arrows track entities). Block-break enchantments: Timber (fell entire tree), Veinminer (mine entire vein), Drill (3×3 mining), Excavator (3×3 digging), Auto-Smelt, Magnetism (items to inventory). Interact enchantments: Gravity Well (pull mobs), Harvester (harvest crop area), Green Thumb (bonemeal area), Replenish (replant crops). Movement/passive: Elytra boost, Cushion (fall damage reduction), Stomp (landing damage), Glider (slow fall), Momentum (speed while sprinting), and passive armor effect updates.

Soulbound: SoulboundListener.java keeps Soulbound-enchanted items on death (removes from drops, re-adds on respawn with 5-tick delay). Supports converting a Soulbound item to a book by renaming to "lostsoul" in an anvil. Enforces a one-per-inventory limit with action-bar warnings.

Mob Difficulty: MobDifficultyManager.java scales mob health and damage based on distance from spawn and biome. Baseline multipliers are 2.0 health and 2.0 damage (note: user wants baseline changed to vanilla, i.e., 1.0). Overworld distance multipliers increase incrementally (e.g., at 350 blocks: 1.5× health, 1.3× damage). Biome multipliers: Pale Garden 2.0, Deep Dark 2.5, Swamp 1.4, Nether biomes 1.7–2.3, End 2.5 health / 2.0 damage.

Boss Enhancer: BossEnhancer.java applies multipliers to bosses. Current values: Ender Dragon 3.5× health / 3.0× damage, Wither 2.5× / 2.2×, Warden 2.0× / 2.5×, Elder Guardian 2.5× / 1.8×. (Note: user wants Warden and Wither reverted to vanilla; Ender Dragon should remain the strongest.)

King Mob: KingMobManager.java tracks mob spawns per type per world. After a configurable number of spawns (default 500), transforms the next mob of that type into a "King" with 10× health, 3× damage, gold glowing name, no despawn. On death drops 3–9 diamonds. Only one King per world at a time. Identifies special mobs for enchanted book drops.

Blood Moon: BloodMoonManager.java checks every 100 ticks during night (time 13000–23000) with a 15% chance to trigger. During Blood Moon: hostile mobs (except creepers and the King) get 1.5× health and 1.3× damage; players receive Darkness effect and red dust particles (reapplied every 60 ticks); double drops and double XP enabled. Every 10th Blood Moon spawns a "Blood Moon King" — a glowing Zombie with diamond armor and a Netherite sword, health = 20 × 20.0 = 400 HP, damage multiplied by 5.0×, all equipment drop chances 0. On death: 5–15 diamonds, 1 Netherite ingot, 500 XP, server announcement. King despawns at dawn. Blood Moon ends at dawn with player notification and Darkness removal.

Pale Garden Fog: PaleGardenFogTask.java runs every 40 ticks, applying atmospheric fog effects to players in the Pale Garden biome.

Vanilla Recipe Remover: VanillaRecipeRemover.java removes specified vanilla recipes to prevent conflicts with custom recipes.

Loot Booster: LootBoosterListener.java adds enchanted books to chest loot, Echo Shards to ancient city loot (40% chance, 1–3 shards), and enchanted book mob drops — boss mobs: 15% + 5% per Looting level for custom enchantment books; hostile mobs: 5% + 2% per Looting level for vanilla enchantment books. Defines pools of valid vanilla and custom enchantments (excluding SOULBOUND and BEST_BUDDIES).

Weapon Managers (existing, needing fixes):

BattleAxeManager.java: Creates battle axes with PDC markers. Damage: Wooden 6, Stone 7, Iron 8, Golden 6, Diamond 9, Netherite 10. Attack speed 0.9 (modifier −3.1). Cancels right-click on strippable blocks. Provides tier-to-ingredient and tier-to-block mappings.
BattleBowManager.java: Creates Battle Bows and Battle Crossbows with PDC markers, custom names, and lore. No attribute modifiers (bows don't have melee stats).
BattleMaceManager.java: Creates Battle Maces with PDC markers, custom names, lore, and attribute modifiers.
SickleManager.java: Converts hoe materials to Sickles. Damage = sword damage + 1 (Wooden 5, Stone 6, Iron 7, Gold 5, Diamond 8, Netherite 9). Attack speed 1.1 (modifier −2.9). Cancels right-click on soil blocks.
SuperToolManager.java: Three-tier system (Iron=1, Diamond=2, Netherite=3). Creates and upgrades super tools with bonus damage (Iron +1, Diamond +2, Netherite +3; reduced for netherite-base items). Trident special case: Diamond +3, Netherite +5. Elytra durability-save: 30%/50%/70%. Handles shields, bows, crossbows with ability lore instead of damage modifiers. Preserves enchantments on upgrade.
Weapon Abilities: WeaponAbilityListener.java handles right-click and left-click abilities for super weapons, weapon mastery tracking, cooldown management, enchant-based damage multipliers, particle effects, status effects, bleed DoT, chain lightning, healing, and per-weapon-class ability implementations (sword, sickle, battle axe, battle mace, pickaxe, shovel, trident, bow, crossbow).

Weapon Mastery: WeaponMasteryManager.java tracks kills per weapon class using PDC, applies a configurable damage bonus (max 10% at 1000 kills), handles join/respawn re-application.

Guilds: GuildManager.java provides full guild CRUD — create, invite, join, leave, kick, disband, info, list. Persists to guilds.yml. Max 10 members, friendly fire disabled by default. GuildListener.java cancels damage between same-guild members with action-bar feedback.

Crafting Recipes: RecipeManager.java registers shaped recipes for battle weapons, sickles, and super tool upgrades.

4. Remaining / Incomplete Modules
The following items are either not yet implemented, partially implemented, or need rework:

BattleShovelManager.java — Not yet created. Specification: battle upgrade converts a shovel to a "Battle Shovel" that loses its normal right-click (path-making) ability. When upgraded to super, gains Efficiency 1 (Iron Super), Efficiency 2 (Diamond Super), Efficiency 3 (Netherite Super). Recipe: shovel in center + 3 matching-material ingots on top row.

SpearManager.java — Not yet created. Spears are a new vanilla weapon in 1.21.11 (crafted with 2 sticks + 1 material). They are NOT trident variants; they are their own item type with tiers (Wooden through Netherite). Stats from wiki: Gold 4 dmg / 0.8 speed, Stone 5 / 0.9, Iron 6 / 1.0, Diamond 7 / 1.0, Netherite presumably 8 / 1.0. Spears skip the "Battle" step entirely and go directly from vanilla → Super Iron → Super Diamond → Super Netherite. The super spear gains a special ability at Diamond tier and a definitive ability at Netherite tier. Spears should have exclusive enchantments (Lunge is vanilla for spear; custom enchantments TBD).

RecipeManager.java — Needs full rewrite. Current issues: recipe pattern conflicts due to identical 3×3 layouts; super-tool upgrade recipes don't properly handle the progression chain; material-matching logic causes enchantment loss on tier upgrades. New design should use PrepareItemCraftEvent for dynamic result generation rather than pre-registering many shaped recipes, and must encode distinct recipes for each weapon path.

SuperToolManager.java — Needs rewrite. Issues: Iron→Diamond upgrades lose enchantments; cross-material super weapons were possible (should not be); lore does not show "base damage + buff" breakdown; Netherite super items don't properly implement the "+2% damage per enchantment" and "no enchantment limit" rules.

Weapon ability system — Super abilities should only activate on Super Diamond and Super Netherite weapons (not Iron). Definitive (Netherite) abilities should be "extremely powerful" with enhanced particle effects. All abilities except the Ender Dragon fight should apply fully. The super pickaxe's ore-detect ability should be very strong (e.g., easily locating Ancient Debris in a large radius).

Blessings system — Partially specified in config (c-bless: 10 uses, 1 heart heal per use; ami-bless: 10 uses, 2% damage per use; la-bless: 10 uses, 2% defense per use). Needs implementation of right-click consumption, PDC persistence for remaining uses, and integration with the main class. A BlessingManager.java and BlessingListener.java are needed.

InventorySorting — Enabled in config but implementation status unclear. Needs a listener or command handler that sorts a player's inventory by material type/category.

EnchantTransfer — Enabled in config. Needs implementation allowing players to transfer enchantments between items (likely via anvil or custom UI).

DropRateBooster — Partially configured (trident drop chance 35%, breeze wind charge 2–5). Needs a listener on EntityDeathEvent that modifies drop tables for specific mobs.

Villager Trade Improvements — Configured (price reduction 50%, trade locking disabled). Needs listeners on villager trade events.

Axe Nerf — Configured (attack speed 0.5). Partially handled in EnchantmentEffectListener. May need a dedicated handler to apply the speed modifier to all axes globally.

5. Identified Problems and Required Fixes
Problem 1 — Super-tool enchantment loss: When upgrading Iron Super → Diamond Super, the upgradeSuperTool method clones and rebuilds the item. If CustomEnchantManager data is not properly transferred during the rebuild (attribute modifiers are removed and re-added), enchantments stored in PDC can be lost. Fix: The upgrade method must explicitly copy all PDC keys (both vanilla enchant storage and custom enchant NamespacedKeys) from the old item to the new item before any modifications.

Problem 2 — Cross-material super weapons: The current createSuperTool(baseTool, tier) method accepts any base tool + any tier, meaning a wooden sword could theoretically become a Netherite Super. Fix: Enforce that the base tool's material must match the super tier being applied: Iron tier requires an iron-material item (or iron+ for sequential upgrade), Diamond requires diamond-material, Netherite requires netherite-material. Exception: tierless weapons (bow, crossbow, mace, trident, spear) follow sequential Iron→Diamond→Netherite regardless of base material.

Problem 3 — Abilities triggering incorrectly: Weapon abilities currently trigger based on both the battle-weapon manager and super-tool manager agreeing. This means abilities only work when both flags are present, which breaks for weapons that skip the battle step (sword, pickaxe, trident, spear). Fix: Abilities should check only the super-tool tier. Diamond Super = standard ability; Netherite Super = definitive ability. Battle status is irrelevant to ability activation.

Problem 4 — Recipe conflicts: Multiple recipes sharing the same 3×3 grid pattern causes the server to reject duplicates or match the wrong recipe. Fix: Use PrepareItemCraftEvent to intercept crafting dynamically — check the center item's type, PDC markers, and surrounding materials to determine the correct output. This eliminates pattern conflicts entirely.

Problem 5 — Mob difficulty baseline: Current baseline is 2.0× health / 2.0× damage, but user wants baseline = vanilla (1.0×) with only distance/biome/event scaling adding difficulty. Fix: Change baselineHealthMultiplier and baselineDamageMultiplier defaults to 1.0 in ConfigManager.

Problem 6 — Boss stats: User wants Warden and Wither at vanilla stats; only Ender Dragon should be enhanced (remains strongest). Fix: Remove or set to 1.0 the multipliers for Warden and Wither in BossEnhancer. Keep Ender Dragon at 3.5× health / 3.0× damage.

Problem 7 — Lore clarity: Super weapons don't show the damage breakdown (base + bonus). Players see a single damage number and can't tell what's from the super upgrade vs. enchantments. Fix: Lore format should be: "⚔ Attack Damage: <base> + <bonus> = <total>" with color-coded components.

Problem 8 — Particle effects: Definitive (Netherite Super) abilities need more dramatic visual effects. Current abilities have basic particles. Fix: Add larger particle bursts, screen effects, and sound effects for definitive abilities. Use EXPLOSION_EMITTER, DRAGON_BREATH, TOTEM_OF_UNDYING, etc.

6. Weapon Progression System — Full Specification
6.1 Progression Chain
The weapon system has two tracks:

Track A — Weapons with Battle step: Axe, Shovel, Hoe (→ Sickle), Bow, Crossbow, Mace.

Path: Vanilla Tool → Battle Tool → Super Iron Battle Tool → Super Diamond Battle Tool → Super Netherite Battle Tool (Definitive).

Track B — Weapons without Battle step: Sword, Pickaxe, Trident, Spear.

Path: Vanilla Tool → Super Iron Tool → Super Diamond Tool → Super Netherite Tool (Definitive).

6.2 Battle Conversion Recipes
Battle Axe, Battle Shovel, Battle Sickle (from Hoe): Tool in center slot, 3× matching material on top row (e.g., Iron Axe center + 3 Iron Ingots on top = Iron Battle Axe).

Battle Bow, Battle Crossbow, Battle Mace: Weapon in center slot, 3× Sticks on top row.

6.3 Super Upgrade Recipes
All super upgrades use: Center item (current weapon) surrounded by 8× upgrade material.

For tiered weapons (those with material tiers — swords, axes, shovels, sickles, pickaxes, spears, hoes):

Iron Sword + 8 Iron Ingots → Super Iron Sword
Diamond Sword + 8 Diamonds → Super Diamond Sword
Netherite Sword + 8 Netherite Ingots → Super Netherite Sword (Definitive)
Sequential upgrade also works: Super Iron Sword + 8 Diamonds → Super Diamond Sword, etc.
For tierless weapons (bow, crossbow, mace, trident):

Weapon + 8 Iron Ingots → Super Iron Weapon
Super Iron Weapon + 8 Diamonds → Super Diamond Weapon
Super Diamond Weapon + 8 Netherite Ingots → Super Netherite Weapon (Definitive)
6.4 Super Tier Bonuses
Iron Super: +1 base damage. No special ability. Lore shows damage breakdown.

Diamond Super: +2 base damage. Gains a right-click (or left-click for some) special ability with cooldown. Ability differs per weapon type.

Netherite Super (Definitive): +2 base damage (over Diamond, so +4 over vanilla when sequentially upgraded, or +2 over Netherite vanilla when directly upgraded). Gains an enhanced "definitive" ability with stronger effects, larger AoE, more particles. Additionally: no enchantment limit, no enchantment conflicts, and +2% damage per enchantment on the weapon.

6.5 Damage Lore Format
Example for a Super Diamond Iron Sword:

§6⚔ Super Diamond Sword
§7Custom Weapon
§f⚔ Attack Damage: §a6.0 §7+ §e2.0 §7= §c8.0
§f⚡ Attack Speed: §b1.6
§d✦ Right-click: Dash Strike
§7Diamond Super Weapon
6.6 Battle Shovel Specifics
Battle Shovel loses normal right-click (path creation) — event cancelled on dirt/grass. When upgraded to Super:

Iron Super Battle Shovel: Efficiency I applied automatically.
Diamond Super Battle Shovel: Efficiency II + ability.
Netherite Super Battle Shovel: Efficiency III + definitive ability.
6.7 Spear Specifics
Spears are a vanilla 1.21.11 weapon (2 sticks + material). Material tiers: Wooden (4 dmg, 0.8 speed), Stone (5, 0.9), Iron (6, 1.0), Diamond (7, 1.0), Netherite (8, 1.0). They have jab and charge attack modes (vanilla mechanic). No battle step. Super upgrade follows sword rules (same-material upgrade: Iron Spear + 8 Iron Ingots → Super Iron Spear). Should receive exclusive enchantments (at minimum Lunge, which is a vanilla spear enchantment, plus potential custom enchantments like IMPALE, REACH, or PIERCING_THRUST — TBD).

7. Custom Enchantments — Full List (49 Enchantments)
Sword Enchantments
VAMPIRISM (max 5) — Heals on hit, scaling with level. Conflicts with LIFESTEAL. BLEED (max 3) — Applies damage-over-time. Conflicts with VENOMSTRIKE. VENOMSTRIKE (max 3) — Applies poison. Conflicts with BLEED. LIFESTEAL (max 3) — Percentage-based heal on hit. Conflicts with VAMPIRISM. CHAIN_LIGHTNING (max 3) — Arcs damage to nearby mobs. Conflicts with WITHER_TOUCH.

Axe Enchantments
BERSERKER (max 5) — Bonus damage at low health. Conflicts with BLOOD_PRICE. LUMBERJACK (max 3) — Bonus damage to wooden mobs/structures. CLEAVE (max 3) — AoE damage on hit. TIMBER (max 3) — Fell entire trees. GUILLOTINE (max 3) — Bonus headshot/critical damage.

Pickaxe Enchantments
VEINMINER (max 3) — Mine entire ore veins. Conflicts with DRILL. DRILL (max 3) — 3×3 mining. Conflicts with VEINMINER. AUTO_SMELT (max 1) — Automatically smelt mined blocks. MAGNETISM (max 1) — Items go directly to inventory. EXCAVATOR (max 3) — 3×3 digging (for pickaxe on stone).

Shovel Enchantments
HARVESTER (max 3) — Harvest crops in area.

Hoe/Sickle Enchantments
GREEN_THUMB (max 1) — Bonemeal area on right-click. REPLENISH (max 1) — Replant crops after harvesting. HARVESTING_MOON (max 3) — Bonus XP from crop harvesting.

Bow Enchantments
EXPLOSIVE_ARROW (max 3) — Arrows explode on impact. Conflicts with HOMING. HOMING (max 3) — Arrows track nearest entity. Conflicts with EXPLOSIVE_ARROW. RAPIDFIRE (max 3) — Increased draw speed. SNIPER (max 3) — Bonus damage at distance.

Crossbow Enchantments
THUNDERLORD (max 5) — Lightning on third consecutive hit. TIDAL_WAVE (max 3) — Knockback wave on hit.

Trident Enchantments
FROSTBITE (max 3) — Applies slowness/freezing. SOUL_REAP (max 3) — Grants Strength on kill. BLOOD_PRICE (max 3) — Bonus damage costs health. Conflicts with BERSERKER. REAPERS_MARK (max 3) — Marks target for bonus damage. WITHER_TOUCH (max 3) — Applies Wither effect. Conflicts with CHAIN_LIGHTNING.

Armor Enchantments
SWIFTNESS (max 3) — Speed boost (boots). VITALITY (max 3) — Health boost (chestplate). AQUA_LUNGS (max 3) — Extended underwater breathing (helmet). NIGHT_VISION (max 1) — Permanent night vision (helmet). FORTIFICATION (max 3) — Damage reduction (chestplate). DEFLECTION (max 3) — Reflect damage to attacker. SWIFTFOOT (max 3) — Speed boost (boots). Conflicts with MOMENTUM. DODGE (max 3) — Chance to negate damage entirely. LEAPING (max 3) — Jump boost (boots).

Elytra Enchantments
BOOST (max 3) — Speed boost while flying. CUSHION (max 1) — Reduced fall damage on landing. GLIDER (max 1) — Slow fall effect. STOMP (max 3) — Deals damage on landing.

Mace Enchantments
SEISMIC_SLAM (max 3) — AoE ground slam. Conflicts with MAGNETIZE. MAGNETIZE (max 3) — Pull nearby mobs. Conflicts with SEISMIC_SLAM. GRAVITY_WELL (max 3) — Pull mobs on right-click. MOMENTUM (max 3) — Speed boost while sprinting. Conflicts with SWIFTFOOT.

Universal Enchantments
SOULBOUND (max 1) — Keep item on death. Special anvil handling; one per inventory. BEST_BUDDIES (max 1) — (Specification TBD — likely pet/companion related).

8. Mob Difficulty System
8.1 Baseline
Should be vanilla (1.0× health, 1.0× damage) — current config has 2.0× which needs correction.

8.2 Distance Scaling (Overworld)
Mobs become progressively harder the farther from spawn. Example thresholds (configurable):

0–349 blocks: no bonus
350 blocks: 1.5× health, 1.3× damage
Further distances increase incrementally
8.3 Biome Multipliers
Applied on top of distance scaling:

Pale Garden: 2.0×
Deep Dark: 2.5×
Swamp: 1.4×
Nether biomes: 1.7–2.3×
End: 2.5× health, 2.0× damage
8.4 Boss Handling
Ender Dragon: 3.5× health, 3.0× damage — must remain the strongest mob in the game. Super weapon definitive abilities do NOT apply full effect against it (treated as normal attack).
Wither: Vanilla stats (1.0× / 1.0×).
Warden: Vanilla stats (1.0× / 1.0×).
Elder Guardian: 2.5× health, 1.8× damage (or revert to vanilla — confirm with user).
8.5 Creeper Spawn Reduction
50% chance to cancel creeper spawns server-wide.

9. Blood Moon Event
Trigger: Every 100 ticks during night (time 13000–23000), 15% chance. Fires once per night per world.

Effects during Blood Moon: All hostile mobs (except Creepers and the King Mob) receive 1.5× health and 1.3× damage. Players receive Darkness effect (200 ticks, reapplied every 60 ticks) and red dust particles. All mob drops are doubled; XP from mob kills is doubled.

Blood Moon King (every 10th Blood Moon): Spawns near a random online player (±20 blocks horizontally). It is a Zombie with diamond armor and a Netherite sword, glowing, named "Blood Moon King" in dark red. Health = 20 HP × 20.0 multiplier = 400 HP. Damage multiplied by 5.0×. Equipment drop chances: 0%. On death: drops 5–15 diamonds, 1 Netherite ingot, 500 XP, server-wide announcement. Despawns at dawn if alive.

Dawn: Blood Moon ends, Darkness removed, players notified.

10. King Mob System
Tracks spawn counts per entity type per world. After 500 spawns of one type, the next spawn becomes a "King" — glowing, gold-named "King [EntityType]", cannot despawn, 10× health, 3× damage. Drops 3–9 diamonds on death. Only one King per world at a time. King mobs are flagged as "special" for enhanced enchanted book drop chances.

11. Blessings System (Needs Implementation)
Three blessing types, consumed via right-click:

C-Bless (Celestial Blessing): 10 uses, heals 1 heart (2 HP) per use. Ami-Bless (Amicable Blessing): 10 uses, grants +2% damage per use (stacking, persistent via PDC). La-Bless (Lambent Blessing): 10 uses, grants +2% defense per use (stacking, persistent via PDC).

Implementation needs: BlessingManager.java (creates blessing items with PDC tracking remaining uses), BlessingListener.java (handles right-click consumption, decrements uses, applies effects, stores accumulated bonuses in player PDC).

12. Additional Systems
12.1 Loot Booster
Enchanted books added to chest loot. Guardian drops 1–3 prismarine shards, Elder Guardian 3–5. Ghast drops 1–2 tears. Echo Shards: 40% chance in ancient city loot, 1–3 shards. Mob enchanted book drops as described in §3.

12.2 Drop Rate Booster
Trident drop chance from Drowned: 35% (vanilla is 8.5% in Java). Breeze drops 2–5 wind charges.

12.3 Villager Trade Improvements
Price reduction: 50%. Trade locking: disabled (villagers don't lock trades when unemployed/angered).

12.4 Axe Nerf
All axes have attack speed set to 0.5 (very slow) to prevent axe dominance over swords.

12.5 Weapon Mastery
Tracks kills per weapon class (sword, axe, bow, etc.) using PDC. Max 1000 kills per class. At max kills, grants +10% damage bonus with that weapon class. Linear scaling: kills/maxKills × maxBonusPercent.

12.6 Inventory Sorting
Enabled in config. Needs implementation — likely triggered by /jglims sort or a keybind, sorting inventory contents by category (blocks, tools, food, etc.).

12.7 Enchant Transfer
Enabled in config. Needs implementation — mechanism to move enchantments between items.

12.8 Pale Garden Fog
Atmospheric effect applied to players in the Pale Garden biome. Check interval: 40 ticks.

13. Commands
/guild (Player-only)
Subcommands: create <name>, invite <player>, join, leave, kick <player>, disband, info, list.

/jglims
Subcommands:

reload — Reloads plugin configuration.
stats <player> — Shows blessing stats for a player.
enchants — Lists all custom enchantments with max levels.
sort — Shows inventory sorting tip/triggers sort.
mastery — Shows weapon mastery stats for the executing player.
14. Configuration Reference (config.yml)
All values loaded with defaults by ConfigManager.java:

mob-difficulty: enabled=true, baseline-health-multiplier=2.0 (→ change to 1.0), baseline-damage-multiplier=2.0 (→ change to 1.0). Distance multipliers and biome multipliers as defined in §8.

creeper-reduction: enabled=true, cancel-chance=0.5.

pale-garden-fog: enabled=true, check-interval=40.

loot-booster: enabled=true, chest-enchanted-book=true, guardian shards 1–3, elder guardian 3–5, ghast tears 1–2, echo-shard-chance=0.40.

mob-book-drops: enabled=true, hostile-chance=0.05, boss-custom-chance=0.15, looting-bonus-regular=0.02, looting-bonus-boss=0.05.

blessings: c-bless (max-uses=10, heal-per-use=1), ami-bless (max-uses=10, damage-percent-per-use=2), la-bless (max-uses=10, defense-percent-per-use=2).

anvil: remove-too-expensive=true, xp-cost-reduction=0.5.

toggles: inventory-sort=true, enchant-transfer=true, sickle=true, battle-axe=true, battle-bow=true, battle-mace=true, super-tools=true, drop-rate-booster=true.

drop-rate-booster: trident-drop-chance=0.35, breeze-wind-charge-min=2, breeze-wind-charge-max=5.

villager-trades: enabled=true, price-reduction=0.50, disable-trade-locking=true.

king-mob: enabled=true, spawns-per-king=500, health-multiplier=10.0, damage-multiplier=3.0, diamond-drop-min=3, diamond-drop-max=9.

axe-nerf: enabled=true, attack-speed=0.5.

weapon-mastery: enabled=true, max-kills=1000, max-bonus-percent=10.0.

blood-moon: enabled=true, check-interval=100, chance=0.15, mob-health-multiplier=1.5, mob-damage-multiplier=1.3, boss-every-nth=10, boss-health-multiplier=20.0, boss-damage-multiplier=5.0, boss-diamond-min=5, boss-diamond-max=15, double-drops=true.

guilds: enabled=true, max-members=10, friendly-fire=false.

15. Class & File Inventory
Package: com.jglims.plugin
JGlimsPlugin.java — Main class, onEnable/onDisable, manager initialization, command registration.
ConfigManager.java — Loads and provides access to all configuration values.
Package: com.jglims.plugin.enchantments
EnchantmentType.java — Enum of all 49 enchantments with max levels.
CustomEnchantManager.java — Registry, conflict map, get/set/remove/list enchantments.
EnchantmentEffectListener.java — All enchantment effect implementations.
AnvilRecipeListener.java — Custom anvil recipes for applying enchantments.
SoulboundListener.java — Keep-on-death, lostsoul conversion, one-per-inventory.
Package: com.jglims.plugin.weapons
BattleAxeManager.java — Battle axe creation and event handling.
BattleBowManager.java — Battle bow and battle crossbow creation.
BattleMaceManager.java — Battle mace creation.
SickleManager.java — Sickle creation from hoes.
BattleShovelManager.java — NEW, needs creation. Battle shovel with blocked right-click.
SpearManager.java — NEW, needs creation. Super spear system.
SuperToolManager.java — Super-tool tier system (needs rewrite).
WeaponAbilityListener.java — Weapon abilities and mastery (needs rewrite for new progression).
WeaponMasteryManager.java — Kill tracking and damage bonus.
Package: com.jglims.plugin.crafting
RecipeManager.java — All crafting recipes (needs rewrite).
VanillaRecipeRemover.java — Removes conflicting vanilla recipes.
Package: com.jglims.plugin.mobs
MobDifficultyManager.java — Distance and biome mob scaling.
BossEnhancer.java — Boss stat multipliers.
KingMobManager.java — King mob spawning and rewards.
BloodMoonManager.java — Blood Moon event system.
Package: com.jglims.plugin.guilds
GuildManager.java — Guild CRUD and persistence.
GuildListener.java — Friendly fire prevention.
Package: com.jglims.plugin.effects
PaleGardenFogTask.java — Atmospheric fog in Pale Garden.
Package: com.jglims.plugin.loot
LootBoosterListener.java — Chest and mob loot enhancements.
Package: com.jglims.plugin.blessings (needs creation)
BlessingManager.java — NEW.
BlessingListener.java — NEW.
Build Files
build.gradle — Java 21, Paper API dependency, Mojang mappings.
src/main/resources/plugin.yml — Plugin metadata, commands, version.
src/main/resources/config.yml — Default configuration.
16. Step-by-Step Implementation Plan
Phase 1 — Config & Mob Fixes (Low risk, immediate)

Update ConfigManager.java: baseline multipliers default to 1.0.
Update BossEnhancer.java: Warden and Wither multipliers to 1.0. Ender Dragon stays at 3.5/3.0.
Add battle-shovel and spear toggles to config.
Phase 2 — New Weapon Managers (Medium risk) 4. Create BattleShovelManager.java with right-click blocking and PDC markers. 5. Create SpearManager.java with super-spear creation, upgrade logic, and PDC markers. 6. Update SickleManager.java if needed for consistency.

Phase 3 — Recipe System Rewrite (High risk, core change) 7. Rewrite RecipeManager.java to use PrepareItemCraftEvent for dynamic recipe matching. 8. Define distinct recipe patterns: battle upgrades (tool center + 3× material on top) and super upgrades (weapon center + 8× material surrounding). 9. Remove old pre-registered shaped recipes that conflict.

Phase 4 — Super Tool Rewrite (High risk, core change) 10. Rewrite SuperToolManager.java: enforce material-matching rules, preserve all enchantments on upgrade, implement "+2% per enchantment" for Netherite, implement "no enchantment limit" for Netherite, update lore format to show "base + bonus = total". 11. Add Efficiency auto-application for Battle Shovels.

Phase 5 — Ability System Rewrite (Medium risk) 12. Rewrite WeaponAbilityListener.java: abilities keyed only on super-tool tier (Diamond = ability, Netherite = definitive ability). Remove battle-weapon checks from ability activation. Add enhanced particles for definitive abilities. Add Ender Dragon exception (definitive abilities → normal attack only). 13. Implement super pickaxe ore-detect ability (large-radius Ancient Debris/ore finder via particle highlights). 14. Implement super spear abilities. 15. Implement super battle shovel abilities.

Phase 6 — Missing Systems (Medium risk) 16. Implement BlessingManager.java and BlessingListener.java. 17. Implement DropRateBoosterListener.java (trident drop chance, breeze wind charges). 18. Implement inventory sorting (command or automatic). 19. Implement enchant transfer mechanism. 20. Implement villager trade listener.

Phase 7 — Integration & Main Class (Low risk) 21. Update JGlimsPlugin.java to instantiate new managers and register new listeners. 22. Update plugin.yml with version bump to 1.3.0. 23. Update build.gradle version to 1.3.0.

Phase 8 — Testing & Deployment 24. Build with .\gradlew.bat clean jar. 25. Deploy to creative test server, verify all systems. 26. Test all battle and super weapon recipes. 27. Test all enchantment effects. 28. Test Blood Moon, King Mob, mob difficulty scaling. 29. Test blessings, guilds, loot drops. 30. Once verified, recreate server in survival mode (delete creative world, re-run docker with GAMEMODE=survival). 31. Install SkinsRestorer 15.10.1. 32. Pre-generate world chunks with Chunky. 33. Final verification and go-live.

17. Docker Commands Reference
Creative Test Server
Copydocker run -d --name mc-crossplay \
  -p 25565:25565 \
  -p 19132:19132/udp \
  -p 25575:25575 \
  -e EULA=TRUE \
  -e TYPE=PAPER \
  -e VERSION=1.21.11 \
  -e MEMORY=8G \
  -e ONLINE_MODE=false \
  -e GAMEMODE=creative \
  -e DIFFICULTY=hard \
  -e RCON_PASSWORD=JGlims2026Rcon \
  -e PLUGINS="https://download.geysermc.org/v2/projects/geyser/versions/latest/builds/latest/downloads/spigot,https://download.geysermc.org/v2/projects/floodgate/versions/latest/builds/latest/downloads/spigot" \
  -v ~/minecraft-server/data:/data \
  itzg/minecraft-server:latest
Switch to Survival
Copydocker rm -f mc-crossplay
# Optionally delete creative world: rm -rf ~/minecraft-server/data/world ~/minecraft-server/data/world_nether ~/minecraft-server/data/world_the_end
docker run -d --name mc-crossplay \
  -p 25565:25565 \
  -p 19132:19132/udp \
  -p 25575:25575 \
  -e EULA=TRUE \
  -e TYPE=PAPER \
  -e VERSION=1.21.11 \
  -e MEMORY=8G \
  -e ONLINE_MODE=false \
  -e GAMEMODE=survival \
  -e DIFFICULTY=hard \
  -e RCON_PASSWORD=JGlims2026Rcon \
  -e PLUGINS="https://download.geysermc.org/v2/projects/geyser/versions/latest/builds/latest/downloads/spigot,https://download.geysermc.org/v2/projects/floodgate/versions/latest/builds/latest/downloads/spigot" \
  -v ~/minecraft-server/data:/data \
  itzg/minecraft-server:latest
18. External Plugin Versions
Plugin	Version	Source
Geyser-Spigot	v2.9.4 (auto-downloaded)	download.geysermc.org
Floodgate	v2.2.5 (auto-downloaded)	download.geysermc.org
Chunky	v1.4.40 (installed)	hangar.papermc.io/pop4959/Chunky
SkinsRestorer	v15.10.1 (pending)	hangar.papermc.io/SRTeam/SkinsRestorer
19. Key Links
GitHub Repository: https://github.com/JGlims/JGlimsPlugin
PaperMC Downloads: https://papermc.io/downloads/paper
Docker Image: https://hub.docker.com/r/itzg/minecraft-server
Docker Minecraft Docs: https://docker-minecraft-server.readthedocs.io/
Geyser Downloads: https://download.geysermc.org/
Chunky (Hangar): https://hangar.papermc.io/pop4959/Chunky/versions/1.4.40
SkinsRestorer (Hangar): https://hangar.papermc.io/SRTeam/SkinsRestorer
Minecraft Spear Wiki: https://minecraft.wiki/w/Spear
20. Open Questions / Decisions Needed
Spear exclusive enchantments: Beyond vanilla Lunge, what custom enchantments should spears receive? Suggestions: IMPALE (bonus damage to armored targets), PIERCING_THRUST (bypasses shields), EXTENDED_REACH (increased attack range).

BEST_BUDDIES enchantment: The specification is unclear. Is this a pet/companion enchantment? What should it do?

Elder Guardian: Keep at 2.5× health / 1.8× damage, or revert to vanilla like Warden/Wither?

Enchant Transfer mechanism: What UI/method? Anvil-based (combine two items to move enchantments)? Command-based? Custom GUI?

Inventory Sorting: Trigger method — command only (/jglims sort), or also on shift-click of a specific block (e.g., chest)?

Super Pickaxe ore-detect: Exact radius for Ancient Debris detection? Suggestion: 16-block radius with particle highlights showing ore locations through walls for 10 seconds.

SkinsRestorer installation: Still pending — run the wget command provided and share logs.

Full rewrite vs. incremental fixes: Given the scope (15+ files), recommendation is phased full rewrite starting with Phase 1–3, delivering complete file contents for each phase before moving to the next.

This document captures the complete state of the JGlimsPlugin project as of March 3, 2026, including all implemented systems, pending work, identified bugs, design specifications, configuration values, class inventories, and the step-by-step plan to reach a fully functional v1.3.0 release. Each subsequent development phase should be checked against this document to ensure nothing is missed

about exclusive spear enchantments and special ability I want you to take your choices and create., about bestbuddies, it is the combination of a totem of undying and a bone in the anvil, it needs to create a dog armor that reduces 95% of the damage the the dog takes, but make him have 0 damage as a condition, you trade dog damage per making him always alive. elder guardians can remain strong. The enchantment transfer base in putting a enchanteditem combined with a normal book in the anvil, it gives you a book with the enchantments you just remove from the weapon, and it costs zero xp. about inventory sorting, I believe it already works, but just in case, is basically an addaptation of the "Mouse Tweaks" mod, a famous mod, for plugins, better use of the mouse, like clicking holding shift in an empty slot of the inventory to sort the inventory, or in an empty spot of the chest to sort the chest or hotbar to sort the hotbar. about ancient debris being easier todetect by the super pickaxe ability, its this, I want this to be the case, be optimal for ancient debris mining ( even the super diamond pickaxe can use its ability to detect ores, and the ancient debris need to have a very very big radius)
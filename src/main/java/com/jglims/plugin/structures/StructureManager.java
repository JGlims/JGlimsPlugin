package com.jglims.plugin.structures;

import com.jglims.plugin.JGlimsPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkPopulateEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Central structure generation manager.
 * Listens for chunk population events and generates structures based on biome, dimension, and spacing rules.
 * Minimum spacing: 300 blocks between any two custom structures (configurable).
 */
public class StructureManager implements Listener {

    private final JGlimsPlugin plugin;
    private final StructureLootPopulator lootPopulator;
    private final StructureBossManager bossManager;
    private final Random random = new Random();

    // Track generated structure locations to enforce spacing
    private final Map<String, List<long[]>> generatedStructures = new HashMap<>();

    // PDC key to mark chunks that already had structure generation attempted
    private final NamespacedKey KEY_CHUNK_PROCESSED;

    // Minimum distance between any two custom structures (in blocks)
    private static final int MIN_SPACING = 300;

    // Don't generate within this distance of world spawn
    private static final int MIN_SPAWN_DISTANCE = 200;

    public StructureManager(JGlimsPlugin plugin) {
        this.plugin = plugin;
        this.lootPopulator = new StructureLootPopulator(plugin);
        this.bossManager = new StructureBossManager(plugin);
        this.KEY_CHUNK_PROCESSED = new NamespacedKey(plugin, "structure_gen_processed");
    }

    @EventHandler
    public void onChunkPopulate(ChunkPopulateEvent event) {
        Chunk chunk = event.getChunk();
        World world = chunk.getWorld();

        // Only process every Nth chunk (performance: check 1 in 4 chunks)
        if ((chunk.getX() + chunk.getZ()) % 4 != 0) return;

        // Check if chunk was already processed via PDC on the world
        String chunkKey = world.getName() + ":" + chunk.getX() + ":" + chunk.getZ();

        // Schedule structure generation 1 tick later to ensure chunk is fully loaded
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            tryGenerateStructure(chunk, world);
        }, 2L);
    }

    private void tryGenerateStructure(Chunk chunk, World world) {
        // Get center of chunk
        int centerX = chunk.getX() * 16 + 8;
        int centerZ = chunk.getZ() * 16 + 8;

        // Don't generate near spawn
        double distFromSpawn = Math.sqrt(centerX * centerX + centerZ * centerZ);
        if (distFromSpawn < MIN_SPAWN_DISTANCE) return;

        // Check spacing against all known structures
        if (!checkSpacing(world.getName(), centerX, centerZ)) return;

        // Get the biome at center
        int surfaceY = world.getHighestBlockYAt(centerX, centerZ);
        Biome biome = world.getBiome(centerX, surfaceY, centerZ);

        // Get eligible structures for this dimension and biome
        StructureType[] candidates = StructureType.byDimension(world.getEnvironment());
        List<StructureType> eligible = new ArrayList<>();

        for (StructureType type : candidates) {
            if (!type.isValidBiome(biome)) continue;

            // Skip event-only structures
            if (type == StructureType.END_RIFT_ARENA || type == StructureType.DRAGON_DEATH_CHEST) continue;

            // Dungeon (Deep) only generates underground
            if (type == StructureType.DUNGEON_DEEP && surfaceY > 30) continue;

            eligible.add(type);
        }

        if (eligible.isEmpty()) return;

        // Roll for each eligible structure type
        for (StructureType type : eligible) {
            if (random.nextDouble() < type.getGenerationChance()) {
                // Found one to generate!
                generateStructure(type, world, centerX, surfaceY, centerZ);
                return; // Only one structure per chunk
            }
        }
    }

    private void generateStructure(StructureType type, World world, int x, int surfaceY, int z) {
        Location origin = new Location(world, x, surfaceY, z);

        // For underground structures, adjust Y
        if (type == StructureType.DUNGEON_DEEP) {
            origin.setY(Math.max(world.getMinHeight() + 10, surfaceY - 30 - random.nextInt(20)));
        }

        // For nether structures, find a valid Y
        if (world.getEnvironment() == World.Environment.NETHER) {
            origin.setY(findNetherFloor(world, x, z));
            if (origin.getY() < 0) return; // No valid floor found
        }

        StructureBuilder builder = new StructureBuilder(plugin, world, origin);

        // Check terrain suitability for surface structures
        if (world.getEnvironment() == World.Environment.NORMAL && type != StructureType.DUNGEON_DEEP) {
            int halfX = type.getSizeX() / 2;
            int halfZ = type.getSizeZ() / 2;
            if (!builder.isTerrainFlat(-halfX, -halfZ, halfX, halfZ, 8)) return;
            if (!builder.isAboveWater(0, 0)) return;
        }

        // Build the structure
        buildStructure(type, builder);

        // Record location for spacing
        recordStructure(world.getName(), x, z);

        // Populate chests with loot
        for (Location chestLoc : builder.getChestLocations()) {
            lootPopulator.populateChest(chestLoc, type);
        }

        // Spawn boss if applicable
        if (type.hasBoss() && builder.getBossSpawnLocation() != null) {
            bossManager.spawnStructureBoss(type, builder.getBossSpawnLocation());
        }

        plugin.getLogger().info("Generated " + type.getDisplayName() + " at " +
            x + ", " + (int) origin.getY() + ", " + z + " in " + world.getName());
    }

    /**
     * Build the actual structure blocks. Each structure type has its own build method.
     * Structures are built using the StructureBuilder API.
     */
    private void buildStructure(StructureType type, StructureBuilder b) {
        switch (type) {
            case CAMPING_SMALL -> buildCampingSmall(b);
            case CAMPING_LARGE -> buildCampingLarge(b);
            case ABANDONED_HOUSE -> buildAbandonedHouse(b);
            case WITCH_HOUSE_FOREST -> buildWitchHouseForest(b);
            case WITCH_HOUSE_SWAMP -> buildWitchHouseSwamp(b);
            case HOUSE_TREE -> buildHouseTree(b);
            case DRUIDS_GROVE -> buildDruidsGrove(b);
            case ALLAY_SANCTUARY -> buildAllaySanctuary(b);
            case MAGE_TOWER -> buildMageTower(b);
            case RUINED_COLOSSEUM -> buildRuinedColosseum(b);
            case SHREK_HOUSE -> buildShrekHouse(b);
            case ANCIENT_TEMPLE -> buildAncientTemple(b);
            case VOLCANO -> buildVolcano(b);
            case FORTRESS -> buildFortress(b);
            case GIGANTIC_CASTLE -> buildGiganticCastle(b);
            case ULTRA_VILLAGE -> buildUltraVillage(b);
            case DUNGEON_DEEP -> buildDungeonDeep(b);
            case CRIMSON_CITADEL -> buildCrimsonCitadel(b);
            case SOUL_SANCTUM -> buildSoulSanctum(b);
            case BASALT_SPIRE -> buildBasaltSpire(b);
            case NETHER_DUNGEON -> buildNetherDungeon(b);
            case PIGLIN_PALACE -> buildPiglinPalace(b);
            case VOID_SHRINE -> buildVoidShrine(b);
            case ENDER_MONASTERY -> buildEnderMonastery(b);
            case DRAGONS_HOARD -> buildDragonsHoard(b);
            default -> buildGenericStructure(b, type);
        }
    }

    // ══════════════════════════════════════════════
    // OVERWORLD STRUCTURES
    // ══════════════════════════════════════════════

    private void buildCampingSmall(StructureBuilder b) {
        // Clear area, place campfire, tent, supply chest
        b.fillFloor(-4, -4, 4, 4, 0, Material.GRASS_BLOCK);
        b.setBlock(0, 1, 0, Material.CAMPFIRE);
        // Tent: wool roof
        b.fillBox(-3, 1, -3, -1, 1, -1, Material.OAK_LOG);
        b.setBlock(-2, 2, -2, Material.WHITE_WOOL);
        b.setBlock(-2, 3, -2, Material.WHITE_WOOL);
        b.setBlock(-3, 2, -2, Material.WHITE_WOOL);
        b.setBlock(-1, 2, -2, Material.WHITE_WOOL);
        // Fences around campfire
        b.setBlock(1, 1, 1, Material.OAK_FENCE);
        b.setBlock(-1, 1, 1, Material.OAK_FENCE);
        b.setBlock(1, 1, -1, Material.OAK_FENCE);
        // Supply chest
        b.placeChest(3, 1, 0);
    }

    private void buildCampingLarge(StructureBuilder b) {
        // Platform
        b.fillFloor(-8, -8, 8, 8, 0, Material.GRASS_BLOCK);
        // Multiple campfires
        b.setBlock(-3, 1, 0, Material.CAMPFIRE);
        b.setBlock(3, 1, 0, Material.CAMPFIRE);
        // Wagon (hay bales + oak)
        b.fillBox(4, 1, -3, 8, 1, -1, Material.OAK_PLANKS);
        b.fillBox(4, 2, -3, 8, 3, -1, Material.HAY_BLOCK);
        b.setBlock(5, 2, -2, Material.OAK_FENCE);
        b.setBlock(7, 2, -2, Material.OAK_FENCE);
        // Tents
        for (int tx = -7; tx <= -5; tx++)
            for (int tz = -3; tz <= -1; tz++)
                b.setBlock(tx, 1, tz, Material.OAK_LOG);
        b.setBlock(-6, 2, -2, Material.RED_WOOL);
        b.setBlock(-6, 3, -2, Material.RED_WOOL);
        // Chests
        b.placeChest(6, 1, 2);
        b.placeChest(-5, 1, 3);
        // Boss spawn
        b.setBossSpawn(0, 1, 0);
    }

    private void buildAbandonedHouse(StructureBuilder b) {
        // Foundation
        b.fillBox(-5, 0, -5, 5, 0, 5, Material.COBBLESTONE);
        // Walls with holes
        b.fillWalls(-5, 1, -5, 5, 4, 5, Material.OAK_PLANKS);
        b.hollowBox(-5, 1, -5, 5, 4, 5);
        // Roof (partial — collapsed)
        b.fillBox(-5, 5, -5, 5, 5, 0, Material.OAK_STAIRS);
        // Decay
        b.decay(-5, 1, -5, 5, 5, 5, 0.15);
        // Cobwebs
        b.scatter(-4, 1, -4, 4, 4, 4, Material.AIR, Material.COBWEB, 0.08);
        // Door opening
        b.setBlock(0, 1, -5, Material.AIR);
        b.setBlock(0, 2, -5, Material.AIR);
        // Chest in basement
        b.fillBox(-2, -1, -2, 2, -1, 2, Material.COBBLESTONE);
        b.placeChest(0, 0, 0);
        b.setBossSpawn(0, 1, 2);
    }

    private void buildWitchHouseForest(StructureBuilder b) {
        // Foundation
        b.fillBox(-4, 0, -4, 4, 0, 4, Material.SPRUCE_PLANKS);
        // Walls
        b.fillWalls(-4, 1, -4, 4, 5, 4, Material.SPRUCE_PLANKS);
        b.hollowBox(-4, 1, -4, 4, 5, 4);
        // Roof
        for (int i = 0; i <= 4; i++) {
            b.fillBox(-4 + i, 6 + i, -4 + i, 4 - i, 6 + i, 4 - i, Material.SPRUCE_PLANKS);
        }
        // Interior
        b.setBlock(-2, 1, -2, Material.CAULDRON);
        b.setBlock(2, 1, 2, Material.BREWING_STAND);
        b.setBlock(0, 1, 3, Material.JACK_O_LANTERN);
        // Dead bushes outside
        b.setBlock(-5, 1, 0, Material.DEAD_BUSH);
        b.setBlock(5, 1, 0, Material.DEAD_BUSH);
        // Door
        b.setBlock(0, 1, -4, Material.AIR);
        b.setBlock(0, 2, -4, Material.AIR);
        b.placeChest(2, 1, -2);
        b.setBossSpawn(0, 1, 0);
    }

    private void buildWitchHouseSwamp(StructureBuilder b) {
        // Stilts
        b.pillar(-4, -3, 0, -4, Material.DARK_OAK_LOG);
        b.pillar(4, -3, 0, -4, Material.DARK_OAK_LOG);
        b.pillar(-4, -3, 0, 4, Material.DARK_OAK_LOG);
        b.pillar(4, -3, 0, 4, Material.DARK_OAK_LOG);
        // Floor
        b.fillBox(-5, 0, -5, 5, 0, 5, Material.DARK_OAK_PLANKS);
        // Walls
        b.fillWalls(-5, 1, -5, 5, 5, 5, Material.DARK_OAK_PLANKS);
        b.hollowBox(-5, 1, -5, 5, 5, 5);
        // Roof
        b.fillBox(-6, 6, -6, 6, 6, 6, Material.PURPLE_WOOL);
        // Interior
        b.setBlock(0, 1, 0, Material.CAULDRON);
        b.setBlock(-3, 1, 3, Material.COBWEB);
        b.setBlock(3, 1, -3, Material.COBWEB);
        b.setBlock(0, 1, -4, Material.AIR);
        b.setBlock(0, 2, -4, Material.AIR);
        b.placeChest(-3, 1, -3);
        b.setBossSpawn(0, 1, 2);
    }

    private void buildHouseTree(StructureBuilder b) {
        // Giant tree trunk
        b.fillBox(-1, 0, -1, 1, 15, 1, Material.OAK_LOG);
        // Canopy
        for (int y = 12; y <= 18; y++) {
            int r = y < 15 ? 5 : (y < 17 ? 3 : 1);
            b.filledCircle(0, y, 0, r, Material.OAK_LEAVES);
        }
        // Platform at Y=8
        b.fillBox(-4, 8, -4, 4, 8, 4, Material.OAK_PLANKS);
        // Walls
        b.fillWalls(-4, 9, -4, 4, 11, 4, Material.OAK_PLANKS);
        b.hollowBox(-4, 9, -4, 4, 11, 4);
        // Ladder
        for (int y = 1; y <= 8; y++) b.setBlock(2, y, -1, Material.LADDER);
        // Window
        b.setBlock(0, 10, -4, Material.GLASS_PANE);
        b.placeChest(-2, 9, -2);
    }

    private void buildDruidsGrove(StructureBuilder b) {
        // Circular clearing - mossy floor
        b.filledCircle(0, 0, 0, 12, Material.MOSS_BLOCK);
        // Giant tree in center
        b.fillBox(-2, 0, -2, 2, 16, 2, Material.OAK_LOG);
        for (int y = 10; y <= 18; y++) b.filledCircle(0, y, 0, 6, Material.OAK_LEAVES);
        // Vine-covered altar
        b.fillBox(-3, 1, -3, 3, 1, 3, Material.MOSSY_STONE_BRICKS);
        b.setBlock(0, 2, 0, Material.GLOWSTONE);
        // Glow lichen details
        b.setBlock(-3, 2, 0, Material.GLOW_LICHEN);
        b.setBlock(3, 2, 0, Material.GLOW_LICHEN);
        b.placeChest(0, 2, -3);
        b.setBossSpawn(0, 1, 5);
    }

    private void buildAllaySanctuary(StructureBuilder b) {
        // Amethyst grotto
        b.filledCircle(0, 0, 0, 10, Material.AMETHYST_BLOCK);
        b.filledCircle(0, 1, 0, 8, Material.AIR);
        b.filledCircle(0, 0, 0, 8, Material.CALCITE);
        // Copper pillars
        b.pillar(-5, 1, 5, 0, Material.COPPER_BLOCK);
        b.pillar(5, 1, 5, 0, Material.COPPER_BLOCK);
        b.pillar(0, 1, 5, -5, Material.COPPER_BLOCK);
        b.pillar(0, 1, 5, 5, Material.COPPER_BLOCK);
        // Candles
        b.setBlock(-3, 1, -3, Material.CANDLE);
        b.setBlock(3, 1, 3, Material.CANDLE);
        b.setBlock(3, 1, -3, Material.CANDLE);
        b.setBlock(-3, 1, 3, Material.CANDLE);
        b.placeChest(0, 1, 0);
    }

    private void buildMageTower(StructureBuilder b) {
        // Base floor
        b.filledCircle(0, 0, 0, 5, Material.STONE_BRICKS);
        // Tower - 8 stories, each 5 blocks tall
        for (int story = 0; story < 8; story++) {
            int baseY = 1 + story * 5;
            int radius = 5 - (story / 3);
            b.filledCircle(0, baseY, 0, radius, Material.STONE_BRICKS); // floor
            for (int y = baseY + 1; y <= baseY + 4; y++) {
                b.circle(0, y, 0, radius, Material.STONE_BRICKS);
            }
            // Window on each floor
            b.setBlock(radius, baseY + 2, 0, Material.PURPLE_STAINED_GLASS);
        }
        // Interior details
        b.setBlock(0, 2, 0, Material.ENCHANTING_TABLE);
        b.setBlock(-2, 2, -2, Material.BOOKSHELF);
        b.setBlock(2, 2, -2, Material.BOOKSHELF);
        // End rods on roof
        b.setBlock(0, 42, 0, Material.END_ROD);
        b.setBlock(1, 42, 0, Material.END_ROD);
        b.setBlock(-1, 42, 0, Material.END_ROD);
        // Ladder shaft
        for (int y = 1; y <= 40; y++) b.setBlock(0, y, -2, Material.LADDER);
        b.placeChest(2, 2, 2);
        b.placeChest(0, 22, 0);
        b.setBossSpawn(0, 37, 0);
    }

    private void buildRuinedColosseum(StructureBuilder b) {
        // Arena floor - circular
        b.filledCircle(0, 0, 0, 18, Material.SAND);
        b.filledCircle(0, 0, 0, 15, Material.SMOOTH_SANDSTONE);
        // Spectator stands (ring)
        for (int i = 0; i < 4; i++) {
            b.circle(0, i + 1, 0, 16 + i, Material.STONE_BRICKS);
        }
        // Pillars around the arena
        for (int angle = 0; angle < 360; angle += 30) {
            int px = (int)(16 * Math.cos(Math.toRadians(angle)));
            int pz = (int)(16 * Math.sin(Math.toRadians(angle)));
            b.pillar(px, 1, 12, pz, Material.STONE_BRICK_WALL);
        }
        // Decay and moss
        b.scatter(-20, 0, -20, 20, 12, 20, Material.STONE_BRICKS, Material.CRACKED_STONE_BRICKS, 0.25);
        b.scatter(-20, 0, -20, 20, 12, 20, Material.STONE_BRICKS, Material.MOSSY_STONE_BRICKS, 0.15);
        b.decay(-20, 5, -20, 20, 12, 20, 0.20);
        // Iron bars (broken cage)
        b.setBlock(-2, 1, 0, Material.IRON_BARS);
        b.setBlock(2, 1, 0, Material.IRON_BARS);
        b.setBlock(-2, 2, 0, Material.IRON_BARS);
        b.setBlock(2, 2, 0, Material.IRON_BARS);
        b.placeChest(0, 1, 0);
        b.setBossSpawn(0, 1, 5);
    }

    private void buildShrekHouse(StructureBuilder b) {
        // Foundation - dirt mound
        b.filledCircle(0, 0, 0, 6, Material.DIRT);
        // House walls
        b.fillWalls(-4, 1, -3, 4, 5, 3, Material.OAK_PLANKS);
        b.hollowBox(-4, 1, -3, 4, 5, 3);
        // Roof - curved
        b.fillBox(-5, 6, -4, 5, 6, 4, Material.OAK_PLANKS);
        b.fillBox(-4, 7, -3, 4, 7, 3, Material.OAK_PLANKS);
        b.fillBox(-3, 8, -2, 3, 8, 2, Material.OAK_PLANKS);
        // Chimney
        b.pillar(3, 6, 10, 2, Material.COBBLESTONE);
        b.setBlock(3, 11, 2, Material.CAMPFIRE);
        // Door
        b.setBlock(0, 1, -3, Material.AIR);
        b.setBlock(0, 2, -3, Material.AIR);
        // Outhouse nearby
        b.fillBox(7, 1, -1, 9, 3, 1, Material.OAK_PLANKS);
        b.setBlock(8, 1, -1, Material.AIR);
        b.setBlock(8, 2, -1, Material.AIR);
        // Onion garden
        b.fillBox(-6, 0, -6, -3, 0, -4, Material.FARMLAND);
        // Interior
        b.setBlock(-2, 1, 0, Material.CRAFTING_TABLE);
        b.setBlock(2, 1, 0, Material.FURNACE);
        // Mushrooms
        b.setBlock(-5, 1, 4, Material.RED_MUSHROOM);
        b.setBlock(-6, 1, 3, Material.BROWN_MUSHROOM);
        b.placeChest(0, 1, 2);
        b.setBossSpawn(0, 1, -5);
    }

    private void buildAncientTemple(StructureBuilder b) {
        // Pyramid base
        for (int layer = 0; layer < 10; layer++) {
            int r = 15 - layer;
            b.fillBox(-r, layer, -r, r, layer, r, Material.SANDSTONE);
        }
        // Hollow interior
        b.fillBox(-10, 1, -10, 10, 7, 10, Material.AIR);
        // Corridors
        b.fillBox(-12, 1, -1, 12, 3, 1, Material.AIR);
        b.fillBox(-1, 1, -12, 1, 3, 12, Material.AIR);
        // Prismarine accents
        b.setBlock(0, 1, 0, Material.PRISMARINE);
        b.setBlock(-5, 1, 0, Material.PRISMARINE_BRICKS);
        b.setBlock(5, 1, 0, Material.PRISMARINE_BRICKS);
        // Gold block details
        b.setBlock(0, 7, 0, Material.GOLD_BLOCK);
        b.setBlock(-8, 1, -8, Material.GOLD_BLOCK);
        b.setBlock(8, 1, 8, Material.GOLD_BLOCK);
        b.placeChest(0, 1, 5);
        b.placeChest(0, 1, -5);
        b.setBossSpawn(0, 1, 0);
    }

    private void buildVolcano(StructureBuilder b) {
        // Cone shape
        for (int y = 0; y < 40; y++) {
            int r = 18 - (y * 18 / 40);
            if (r < 2) r = 2;
            b.filledCircle(0, y, 0, r, Material.BASALT);
            if (r > 4) b.filledCircle(0, y, 0, r - 3, Material.BLACKSTONE);
        }
        // Hollow core (lava)
        for (int y = 1; y < 35; y++) {
            int r = Math.max(1, 6 - (y / 8));
            b.filledCircle(0, y, 0, r, Material.LAVA);
        }
        // Magma blocks at base
        b.scatter(-18, 0, -18, 18, 5, 18, Material.BASALT, Material.MAGMA_BLOCK, 0.15);
        // Summit crater
        b.filledCircle(0, 38, 0, 3, Material.LAVA);
        // Internal chambers
        b.fillBox(-4, 5, -4, 4, 8, 4, Material.AIR);
        b.fillBox(-4, 5, -4, 4, 5, 4, Material.OBSIDIAN);
        b.placeChest(0, 6, 0);
        b.setBossSpawn(0, 6, 3);
    }

    private void buildFortress(StructureBuilder b) {
        // Outer walls
        b.fillWalls(-22, 0, -22, 22, 8, 22, Material.DEEPSLATE_BRICKS);
        b.hollowBox(-22, 1, -22, 22, 8, 22);
        // Corner towers
        for (int[] corner : new int[][]{{-22,-22},{22,-22},{-22,22},{22,22}}) {
            b.fillBox(corner[0]-2, 0, corner[1]-2, corner[0]+2, 12, corner[1]+2, Material.DEEPSLATE_BRICKS);
            b.fillBox(corner[0]-1, 1, corner[1]-1, corner[0]+1, 12, corner[1]+1, Material.AIR);
        }
        // Gate
        b.fillBox(-2, 1, -22, 2, 5, -22, Material.AIR);
        // Barracks interior
        b.fillBox(-10, 1, -10, -5, 4, -5, Material.BLACKSTONE);
        b.fillBox(-9, 1, -9, -6, 3, -6, Material.AIR);
        // Chains and lanterns
        b.setBlock(0, 7, -21, Material.CHAIN);
        b.setBlock(0, 6, -21, Material.LANTERN);
        b.placeChest(-8, 1, -7);
        b.placeChest(10, 1, 10);
        b.setBossSpawn(0, 1, 0);
    }

    private void buildGiganticCastle(StructureBuilder b) {
        // Massive outer walls
        b.fillWalls(-35, 0, -35, 35, 15, 35, Material.STONE_BRICKS);
        b.hollowBox(-35, 1, -35, 35, 15, 35);
        // Courtyard floor
        b.fillFloor(-34, -34, 34, 34, 0, Material.STONE_BRICKS);
        // Main keep (center)
        b.fillWalls(-12, 0, -12, 12, 20, 12, Material.DEEPSLATE_BRICKS);
        b.hollowBox(-12, 1, -12, 12, 20, 12);
        // Throne room
        b.setBlock(0, 1, 8, Material.GOLD_BLOCK);
        b.setBlock(0, 2, 8, Material.GOLD_BLOCK);
        // Towers (4 corners of keep)
        for (int[] c : new int[][]{{-12,-12},{12,-12},{-12,12},{12,12}}) {
            b.fillBox(c[0]-2, 0, c[1]-2, c[0]+2, 25, c[1]+2, Material.STONE_BRICKS);
            b.fillBox(c[0]-1, 1, c[1]-1, c[0]+1, 25, c[1]+1, Material.AIR);
        }
        // Gate
        b.fillBox(-3, 1, -35, 3, 8, -35, Material.AIR);
        // Banners (iron bars as stands)
        b.setBlock(-4, 9, -35, Material.IRON_BARS);
        b.setBlock(4, 9, -35, Material.IRON_BARS);
        // Dungeon
        b.fillBox(-8, -5, -8, 8, -1, 8, Material.DEEPSLATE_BRICKS);
        b.fillBox(-7, -4, -7, 7, -1, 7, Material.AIR);
        b.setBlock(0, -4, 0, Material.IRON_BARS);
        b.placeChest(0, 1, 0);
        b.placeChest(5, -4, 5);
        b.placeChest(-20, 1, 20);
        b.setBossSpawn(0, 1, 5);
    }

    private void buildUltraVillage(StructureBuilder b) {
        // Outer stone walls
        b.fillWalls(-40, 0, -40, 40, 5, 40, Material.STONE_BRICKS);
        b.hollowBox(-40, 1, -40, 40, 5, 40);
        // Gate
        b.fillBox(-2, 1, -40, 2, 4, -40, Material.AIR);
        b.setBlock(-3, 5, -40, Material.STONE_BRICK_WALL);
        b.setBlock(3, 5, -40, Material.STONE_BRICK_WALL);
        // Houses (6 simple houses)
        for (int i = 0; i < 6; i++) {
            int hx = -25 + (i % 3) * 18;
            int hz = (i < 3) ? -20 : 10;
            b.fillBox(hx, 0, hz, hx + 8, 0, hz + 8, Material.COBBLESTONE);
            b.fillWalls(hx, 1, hz, hx + 8, 4, hz + 8, Material.OAK_PLANKS);
            b.hollowBox(hx, 1, hz, hx + 8, 4, hz + 8);
            b.fillBox(hx, 5, hz, hx + 8, 5, hz + 8, Material.OAK_PLANKS);
            b.setBlock(hx + 4, 1, hz, Material.AIR);
            b.setBlock(hx + 4, 2, hz, Material.AIR);
        }
        // Central well
        b.filledCircle(0, 0, 0, 3, Material.COBBLESTONE);
        b.setBlock(0, 0, 0, Material.WATER);
        b.pillar(-2, 1, 3, 0, Material.COBBLESTONE_WALL);
        b.pillar(2, 1, 3, 0, Material.COBBLESTONE_WALL);
        // Chests in some houses
        b.placeChest(-22, 1, -17);
        b.placeChest(14, 1, 13);
        b.placeChest(-5, 1, 13);
    }

    private void buildDungeonDeep(StructureBuilder b) {
        // Multi-room dungeon
        // Main room
        b.fillWalls(-12, 0, -12, 12, 6, 12, Material.DEEPSLATE_BRICKS);
        b.hollowBox(-12, 1, -12, 12, 6, 12);
        // Side rooms
        b.fillBox(-12, 1, -5, -18, 5, 5, Material.AIR);
        b.fillWalls(-18, 0, -5, -12, 5, 5, Material.DEEPSLATE_BRICKS);
        b.fillBox(12, 1, -5, 18, 5, 5, Material.AIR);
        b.fillWalls(12, 0, -5, 18, 5, 5, Material.DEEPSLATE_BRICKS);
        // Sculk patches
        b.scatter(-12, 0, -12, 12, 0, 12, Material.DEEPSLATE_BRICKS, Material.SCULK, 0.1);
        // Iron bars (cells)
        for (int z = -4; z <= 4; z += 2) {
            b.setBlock(-11, 1, z, Material.IRON_BARS);
            b.setBlock(-11, 2, z, Material.IRON_BARS);
        }
        // Spawner placeholder (just mossy cobble)
        b.setBlock(0, 1, 0, Material.MOSSY_COBBLESTONE);
        b.placeChest(-15, 1, 0);
        b.placeChest(15, 1, 0);
        b.setBossSpawn(0, 1, 0);
    }

    // ══════════════════════════════════════════════
    // NETHER STRUCTURES
    // ══════════════════════════════════════════════

    private void buildCrimsonCitadel(StructureBuilder b) {
        // Main structure
        b.fillWalls(-15, 0, -15, 15, 12, 15, Material.NETHER_BRICKS);
        b.hollowBox(-15, 1, -15, 15, 12, 15);
        // Crimson accents
        b.fillFloor(-14, -14, 14, 14, 0, Material.CRIMSON_PLANKS);
        // Throne
        b.fillBox(-2, 1, 10, 2, 3, 12, Material.CRIMSON_PLANKS);
        b.setBlock(0, 2, 11, Material.GOLD_BLOCK);
        // Armory room
        b.fillBox(8, 1, -10, 12, 1, -6, Material.NETHER_BRICK_FENCE);
        // Tower
        b.fillBox(-15, 12, -15, -10, 18, -10, Material.NETHER_BRICKS);
        b.placeChest(0, 1, 0);
        b.placeChest(10, 1, -8);
        b.setBossSpawn(0, 2, 8);
    }

    private void buildSoulSanctum(StructureBuilder b) {
        // Soul fire temple
        b.fillWalls(-10, 0, -10, 10, 8, 10, Material.SOUL_SAND);
        b.hollowBox(-10, 1, -10, 10, 8, 10);
        b.scatter(-10, 0, -10, 10, 8, 10, Material.SOUL_SAND, Material.SOUL_SOIL, 0.3);
        // Soul fire torches
        b.setBlock(-5, 3, -5, Material.SOUL_LANTERN);
        b.setBlock(5, 3, 5, Material.SOUL_LANTERN);
        b.setBlock(-5, 3, 5, Material.SOUL_LANTERN);
        b.setBlock(5, 3, -5, Material.SOUL_LANTERN);
        // Altar
        b.setBlock(0, 1, 0, Material.SOUL_CAMPFIRE);
        b.placeChest(3, 1, 3);
        b.setBossSpawn(0, 1, 4);
    }

    private void buildBasaltSpire(StructureBuilder b) {
        // Towering spire
        for (int y = 0; y < 40; y++) {
            int r = 8 - (y * 6 / 40);
            if (r < 2) r = 2;
            b.filledCircle(0, y, 0, r, Material.BASALT);
        }
        // Internal spiral
        for (int y = 1; y < 35; y++) {
            double angle = y * 15;
            int sx = (int)(3 * Math.cos(Math.toRadians(angle)));
            int sz = (int)(3 * Math.sin(Math.toRadians(angle)));
            b.setBlock(sx, y, sz, Material.AIR);
            b.setBlock(sx, y + 1, sz, Material.AIR);
        }
        // Magma accents
        b.scatter(-8, 0, -8, 8, 40, 8, Material.BASALT, Material.MAGMA_BLOCK, 0.05);
        b.placeChest(0, 20, 0);
        b.setBossSpawn(0, 35, 0);
    }

    private void buildNetherDungeon(StructureBuilder b) {
        b.fillWalls(-12, 0, -8, 12, 6, 8, Material.NETHER_BRICKS);
        b.hollowBox(-12, 1, -8, 12, 6, 8);
        // Blaze spawner room
        b.fillBox(-3, 1, -3, 3, 1, 3, Material.NETHER_BRICKS);
        b.setBlock(0, 2, 0, Material.NETHER_BRICKS); // spawner stand
        // Corridors
        b.fillBox(-12, 1, -1, -8, 3, 1, Material.AIR);
        b.fillBox(8, 1, -1, 12, 3, 1, Material.AIR);
        b.placeChest(-10, 1, 0);
        b.placeChest(10, 1, 0);
        b.setBossSpawn(0, 2, 0);
    }

    private void buildPiglinPalace(StructureBuilder b) {
        // Gold-decorated structure
        b.fillWalls(-20, 0, -20, 20, 10, 20, Material.NETHER_BRICKS);
        b.hollowBox(-20, 1, -20, 20, 10, 20);
        // Gold floor
        b.fillFloor(-19, -19, 19, 19, 0, Material.GOLD_BLOCK);
        // Throne
        b.fillBox(-2, 1, 15, 2, 4, 18, Material.GOLD_BLOCK);
        // Vaults (chests behind gold)
        b.fillBox(12, 1, 12, 18, 4, 18, Material.GOLD_BLOCK);
        b.fillBox(13, 1, 13, 17, 3, 17, Material.AIR);
        // Pillars
        b.pillar(-10, 1, 8, 0, Material.GILDED_BLACKSTONE);
        b.pillar(10, 1, 8, 0, Material.GILDED_BLACKSTONE);
        b.pillar(-10, 1, 8, -10, Material.GILDED_BLACKSTONE);
        b.pillar(10, 1, 8, -10, Material.GILDED_BLACKSTONE);
        b.placeChest(15, 1, 15);
        b.placeChest(-15, 1, -15);
        b.setBossSpawn(0, 1, 12);
    }

    // ══════════════════════════════════════════════
    // END STRUCTURES
    // ══════════════════════════════════════════════

    private void buildVoidShrine(StructureBuilder b) {
        // Obsidian + purpur shrine
        b.filledCircle(0, 0, 0, 10, Material.OBSIDIAN);
        b.filledCircle(0, 1, 0, 8, Material.PURPUR_BLOCK);
        // Pillars
        for (int angle = 0; angle < 360; angle += 60) {
            int px = (int)(8 * Math.cos(Math.toRadians(angle)));
            int pz = (int)(8 * Math.sin(Math.toRadians(angle)));
            b.pillar(px, 1, 8, pz, Material.PURPUR_PILLAR);
            b.setBlock(px, 9, pz, Material.END_ROD);
        }
        // Central altar
        b.setBlock(0, 2, 0, Material.OBSIDIAN);
        b.setBlock(0, 3, 0, Material.ENDER_CHEST);
        b.placeChest(3, 2, 0);
        b.placeChest(-3, 2, 0);
        b.setBossSpawn(0, 2, 5);
    }

    private void buildEnderMonastery(StructureBuilder b) {
        // Purpur + end stone library
        b.fillWalls(-15, 0, -10, 15, 10, 10, Material.PURPUR_BLOCK);
        b.hollowBox(-15, 1, -10, 15, 10, 10);
        b.fillFloor(-14, -9, 14, 9, 0, Material.END_STONE_BRICKS);
        // Bookshelves
        for (int x = -12; x <= 12; x += 4) {
            b.pillar(x, 1, 4, -8, Material.BOOKSHELF);
        }
        // Purpur pillars
        b.pillar(-8, 1, 8, 0, Material.PURPUR_PILLAR);
        b.pillar(8, 1, 8, 0, Material.PURPUR_PILLAR);
        // End rods
        b.setBlock(0, 9, 0, Material.END_ROD);
        b.setBlock(-5, 9, 0, Material.END_ROD);
        b.setBlock(5, 9, 0, Material.END_ROD);
        b.placeChest(0, 1, 0);
        b.placeChest(-10, 1, -5);
        b.setBossSpawn(0, 1, 5);
    }

    private void buildDragonsHoard(StructureBuilder b) {
        // Gold pile
        b.filledCircle(0, 0, 0, 8, Material.GOLD_BLOCK);
        b.filledCircle(0, 1, 0, 6, Material.GOLD_BLOCK);
        b.filledCircle(0, 2, 0, 4, Material.GOLD_BLOCK);
        b.filledCircle(0, 3, 0, 2, Material.GOLD_BLOCK);
        // 3 chests on top
        b.placeChest(-1, 4, 0);
        b.placeChest(0, 4, 0);
        b.placeChest(1, 4, 0);
    }

    private void buildGenericStructure(StructureBuilder b, StructureType type) {
        // Fallback: simple room
        int hx = type.getSizeX() / 4;
        int hz = type.getSizeZ() / 4;
        b.fillWalls(-hx, 0, -hz, hx, 5, hz, Material.STONE_BRICKS);
        b.hollowBox(-hx, 1, -hz, hx, 5, hz);
        b.placeChest(0, 1, 0);
        if (type.hasBoss()) b.setBossSpawn(0, 1, 2);
    }

    // ══════════════════════════════════════════════
    // UTILITY METHODS
    // ══════════════════════════════════════════════

    private boolean checkSpacing(String worldName, int x, int z) {
        List<long[]> locs = generatedStructures.get(worldName);
        if (locs == null) return true;
        for (long[] loc : locs) {
            double dist = Math.sqrt(Math.pow(loc[0] - x, 2) + Math.pow(loc[1] - z, 2));
            if (dist < MIN_SPACING) return false;
        }
        return true;
    }

    private void recordStructure(String worldName, int x, int z) {
        generatedStructures.computeIfAbsent(worldName, k -> new ArrayList<>()).add(new long[]{x, z});
    }

    private int findNetherFloor(World world, int x, int z) {
        for (int y = 32; y < 100; y++) {
            Material below = world.getBlockAt(x, y, z).getType();
            Material at = world.getBlockAt(x, y + 1, z).getType();
            Material above = world.getBlockAt(x, y + 2, z).getType();
            if (!below.isAir() && below != Material.LAVA && at.isAir() && above.isAir()) {
                return y + 1;
            }
        }
        return -1;
    }

    public StructureBossManager getBossManager() { return bossManager; }
    public StructureLootPopulator getLootPopulator() { return lootPopulator; }
}
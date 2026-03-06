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

public class StructureManager implements Listener {

    private final JGlimsPlugin plugin;
    private final StructureLootPopulator lootPopulator;
    private final StructureBossManager bossManager;
    private final Random random = new Random();
    private final Map<String, List<long[]>> generatedStructures = new HashMap<>();
    private final NamespacedKey KEY_CHUNK_PROCESSED;
    private static final int MIN_SPACING = 300;
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
        if ((chunk.getX() + chunk.getZ()) % 4 != 0) return;
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> tryGenerateStructure(chunk, world), 2L);
    }

    private void tryGenerateStructure(Chunk chunk, World world) {
        int centerX = chunk.getX() * 16 + 8;
        int centerZ = chunk.getZ() * 16 + 8;
        double distFromSpawn = Math.sqrt(centerX * centerX + centerZ * centerZ);
        if (distFromSpawn < MIN_SPAWN_DISTANCE) return;
        if (!checkSpacing(world.getName(), centerX, centerZ)) return;

        int surfaceY = world.getHighestBlockYAt(centerX, centerZ);
        Biome biome = world.getBiome(centerX, surfaceY, centerZ);

        // Determine candidate structures
        StructureType[] candidates;
        if (world.getName().equalsIgnoreCase("world_abyss")) {
            candidates = StructureType.abyssStructures();
        } else {
            candidates = StructureType.byDimension(world.getEnvironment());
        }

        List<StructureType> eligible = new ArrayList<>();
        for (StructureType type : candidates) {
            if (!type.isValidBiome(biome)) continue;
            if (type == StructureType.END_RIFT_ARENA || type == StructureType.DRAGON_DEATH_CHEST) continue;
            if (type == StructureType.DUNGEON_DEEP && surfaceY > 30) continue;
            // Skip Abyss structures in normal End
            if (type.isAbyssDimension() && !world.getName().equalsIgnoreCase("world_abyss")) continue;
            // Skip normal End structures in Abyss
            if (!type.isAbyssDimension() && world.getName().equalsIgnoreCase("world_abyss")) continue;
            eligible.add(type);
        }
        if (eligible.isEmpty()) return;
        for (StructureType type : eligible) {
            if (random.nextDouble() < type.getGenerationChance()) {
                generateStructure(type, world, centerX, surfaceY, centerZ);
                return;
            }
        }
    }

    private void generateStructure(StructureType type, World world, int x, int surfaceY, int z) {
        Location origin = new Location(world, x, surfaceY, z);
        if (type == StructureType.DUNGEON_DEEP) {
            origin.setY(Math.max(world.getMinHeight() + 10, surfaceY - 30 - random.nextInt(20)));
        }
        if (world.getEnvironment() == World.Environment.NETHER) {
            origin.setY(findNetherFloor(world, x, z));
            if (origin.getY() < 0) return;
        }
        StructureBuilder builder = new StructureBuilder(plugin, world, origin);
        if (world.getEnvironment() == World.Environment.NORMAL && type != StructureType.DUNGEON_DEEP) {
            int halfX = type.getSizeX() / 2; int halfZ = type.getSizeZ() / 2;
            if (!builder.isTerrainFlat(-halfX, -halfZ, halfX, halfZ, 8)) return;
            if (!builder.isAboveWater(0, 0)) return;
        }
        buildStructure(type, builder);
        recordStructure(world.getName(), x, z);
        for (Location chestLoc : builder.getChestLocations()) { lootPopulator.populateChest(chestLoc, type); }
        if (type.hasBoss() && builder.getBossSpawnLocation() != null) { bossManager.spawnStructureBoss(type, builder.getBossSpawnLocation()); }
        plugin.getLogger().info("Generated " + type.getDisplayName() + " at " + x + ", " + (int) origin.getY() + ", " + z + " in " + world.getName());
    }

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
            case THANOS_TEMPLE -> buildThanosTemple(b);
            case PILLAGER_FORTRESS -> buildPillagerFortress(b);
            case PILLAGER_AIRSHIP -> buildPillagerAirship(b);
            case FROST_DUNGEON -> buildFrostDungeon(b);
            case BANDIT_HIDEOUT -> buildBanditHideout(b);
            case SUNKEN_RUINS -> buildSunkenRuins(b);
            case CURSED_GRAVEYARD -> buildCursedGraveyard(b);
            case SKY_ALTAR -> buildSkyAltar(b);
            case CRIMSON_CITADEL -> buildCrimsonCitadel(b);
            case SOUL_SANCTUM -> buildSoulSanctum(b);
            case BASALT_SPIRE -> buildBasaltSpire(b);
            case NETHER_DUNGEON -> buildNetherDungeon(b);
            case PIGLIN_PALACE -> buildPiglinPalace(b);
            case WITHER_SANCTUM -> buildWitherSanctum(b);
            case BLAZE_COLOSSEUM -> buildBlazeColosseum(b);
            case VOID_SHRINE -> buildVoidShrine(b);
            case ENDER_MONASTERY -> buildEnderMonastery(b);
            case DRAGONS_HOARD -> buildDragonsHoard(b);
            case ABYSSAL_CASTLE -> buildAbyssalCastle(b);
            case VOID_NEXUS -> buildVoidNexus(b);
            case SHATTERED_CATHEDRAL -> buildShatteredCathedral(b);
            default -> buildGenericStructure(b, type);
        }
    }

    // ══════════════════════════════════════
    //  OVERWORLD (original)
    // ══════════════════════════════════════

    private void buildCampingSmall(StructureBuilder b) {
        b.fillFloor(-4, -4, 4, 4, 0, Material.GRASS_BLOCK);
        b.setBlock(0, 1, 0, Material.CAMPFIRE);
        b.fillBox(-3, 1, -3, -1, 1, -1, Material.OAK_LOG);
        b.setBlock(-2, 2, -2, Material.WHITE_WOOL); b.setBlock(-2, 3, -2, Material.WHITE_WOOL);
        b.setBlock(-3, 2, -2, Material.WHITE_WOOL); b.setBlock(-1, 2, -2, Material.WHITE_WOOL);
        b.setBlock(1, 1, 1, Material.OAK_FENCE); b.setBlock(-1, 1, 1, Material.OAK_FENCE); b.setBlock(1, 1, -1, Material.OAK_FENCE);
        b.placeChest(3, 1, 0);
    }

    private void buildCampingLarge(StructureBuilder b) {
        b.fillFloor(-8, -8, 8, 8, 0, Material.GRASS_BLOCK);
        b.setBlock(-3, 1, 0, Material.CAMPFIRE); b.setBlock(3, 1, 0, Material.CAMPFIRE);
        b.fillBox(4, 1, -3, 8, 1, -1, Material.OAK_PLANKS); b.fillBox(4, 2, -3, 8, 3, -1, Material.HAY_BLOCK);
        b.setBlock(5, 2, -2, Material.OAK_FENCE); b.setBlock(7, 2, -2, Material.OAK_FENCE);
        for (int tx = -7; tx <= -5; tx++) for (int tz = -3; tz <= -1; tz++) b.setBlock(tx, 1, tz, Material.OAK_LOG);
        b.setBlock(-6, 2, -2, Material.RED_WOOL); b.setBlock(-6, 3, -2, Material.RED_WOOL);
        b.placeChest(6, 1, 2); b.placeChest(-5, 1, 3); b.setBossSpawn(0, 1, 0);
    }

    private void buildAbandonedHouse(StructureBuilder b) {
        b.fillBox(-5, 0, -5, 5, 0, 5, Material.COBBLESTONE);
        b.fillWalls(-5, 1, -5, 5, 4, 5, Material.OAK_PLANKS); b.hollowBox(-5, 1, -5, 5, 4, 5);
        b.fillBox(-5, 5, -5, 5, 5, 0, Material.OAK_STAIRS);
        b.decay(-5, 1, -5, 5, 5, 5, 0.15); b.scatter(-4, 1, -4, 4, 4, 4, Material.AIR, Material.COBWEB, 0.08);
        b.setBlock(0, 1, -5, Material.AIR); b.setBlock(0, 2, -5, Material.AIR);
        b.fillBox(-2, -1, -2, 2, -1, 2, Material.COBBLESTONE);
        b.placeChest(0, 0, 0); b.setBossSpawn(0, 1, 2);
    }

    private void buildWitchHouseForest(StructureBuilder b) {
        b.fillBox(-4, 0, -4, 4, 0, 4, Material.SPRUCE_PLANKS);
        b.fillWalls(-4, 1, -4, 4, 5, 4, Material.SPRUCE_PLANKS); b.hollowBox(-4, 1, -4, 4, 5, 4);
        for (int i = 0; i <= 4; i++) b.fillBox(-4+i, 6+i, -4+i, 4-i, 6+i, 4-i, Material.SPRUCE_PLANKS);
        b.setBlock(-2, 1, -2, Material.CAULDRON); b.setBlock(2, 1, 2, Material.BREWING_STAND); b.setBlock(0, 1, 3, Material.JACK_O_LANTERN);
        b.setBlock(-5, 1, 0, Material.DEAD_BUSH); b.setBlock(5, 1, 0, Material.DEAD_BUSH);
        b.setBlock(0, 1, -4, Material.AIR); b.setBlock(0, 2, -4, Material.AIR);
        b.placeChest(2, 1, -2); b.setBossSpawn(0, 1, 0);
    }

    private void buildWitchHouseSwamp(StructureBuilder b) {
        b.pillar(-4, -3, 0, -4, Material.DARK_OAK_LOG); b.pillar(4, -3, 0, -4, Material.DARK_OAK_LOG);
        b.pillar(-4, -3, 0, 4, Material.DARK_OAK_LOG); b.pillar(4, -3, 0, 4, Material.DARK_OAK_LOG);
        b.fillBox(-5, 0, -5, 5, 0, 5, Material.DARK_OAK_PLANKS);
        b.fillWalls(-5, 1, -5, 5, 5, 5, Material.DARK_OAK_PLANKS); b.hollowBox(-5, 1, -5, 5, 5, 5);
        b.fillBox(-6, 6, -6, 6, 6, 6, Material.PURPLE_WOOL);
        b.setBlock(0, 1, 0, Material.CAULDRON); b.setBlock(-3, 1, 3, Material.COBWEB); b.setBlock(3, 1, -3, Material.COBWEB);
        b.setBlock(0, 1, -4, Material.AIR); b.setBlock(0, 2, -4, Material.AIR);
        b.placeChest(-3, 1, -3); b.setBossSpawn(0, 1, 2);
    }

    private void buildHouseTree(StructureBuilder b) {
        b.fillBox(-1, 0, -1, 1, 15, 1, Material.OAK_LOG);
        for (int y = 12; y <= 18; y++) { int r = y < 15 ? 5 : (y < 17 ? 3 : 1); b.filledCircle(0, y, 0, r, Material.OAK_LEAVES); }
        b.fillBox(-4, 8, -4, 4, 8, 4, Material.OAK_PLANKS);
        b.fillWalls(-4, 9, -4, 4, 11, 4, Material.OAK_PLANKS); b.hollowBox(-4, 9, -4, 4, 11, 4);
        for (int y = 1; y <= 8; y++) b.setBlock(2, y, -1, Material.LADDER);
        b.setBlock(0, 10, -4, Material.GLASS_PANE); b.placeChest(-2, 9, -2);
    }

    private void buildDruidsGrove(StructureBuilder b) {
        b.filledCircle(0, 0, 0, 12, Material.MOSS_BLOCK);
        b.fillBox(-2, 0, -2, 2, 16, 2, Material.OAK_LOG);
        for (int y = 10; y <= 18; y++) b.filledCircle(0, y, 0, 6, Material.OAK_LEAVES);
        b.fillBox(-3, 1, -3, 3, 1, 3, Material.MOSSY_STONE_BRICKS);
        b.setBlock(0, 2, 0, Material.GLOWSTONE);
        b.setBlock(-3, 2, 0, Material.GLOW_LICHEN); b.setBlock(3, 2, 0, Material.GLOW_LICHEN);
        b.placeChest(0, 2, -3); b.setBossSpawn(0, 1, 5);
    }

    private void buildAllaySanctuary(StructureBuilder b) {
        b.filledCircle(0, 0, 0, 10, Material.AMETHYST_BLOCK);
        b.filledCircle(0, 1, 0, 8, Material.AIR); b.filledCircle(0, 0, 0, 8, Material.CALCITE);
        b.pillar(-5, 1, 5, 0, Material.COPPER_BLOCK); b.pillar(5, 1, 5, 0, Material.COPPER_BLOCK);
        b.pillar(0, 1, 5, -5, Material.COPPER_BLOCK); b.pillar(0, 1, 5, 5, Material.COPPER_BLOCK);
        b.setBlock(-3, 1, -3, Material.CANDLE); b.setBlock(3, 1, 3, Material.CANDLE);
        b.setBlock(3, 1, -3, Material.CANDLE); b.setBlock(-3, 1, 3, Material.CANDLE);
        b.placeChest(0, 1, 0);
    }

    private void buildMageTower(StructureBuilder b) {
        b.filledCircle(0, 0, 0, 5, Material.STONE_BRICKS);
        for (int story = 0; story < 8; story++) {
            int baseY = 1 + story * 5; int radius = 5 - (story / 3);
            b.filledCircle(0, baseY, 0, radius, Material.STONE_BRICKS);
            for (int y = baseY + 1; y <= baseY + 4; y++) b.circle(0, y, 0, radius, Material.STONE_BRICKS);
            b.setBlock(radius, baseY + 2, 0, Material.PURPLE_STAINED_GLASS);
        }
        b.setBlock(0, 2, 0, Material.ENCHANTING_TABLE); b.setBlock(-2, 2, -2, Material.BOOKSHELF); b.setBlock(2, 2, -2, Material.BOOKSHELF);
        b.setBlock(0, 42, 0, Material.END_ROD); b.setBlock(1, 42, 0, Material.END_ROD); b.setBlock(-1, 42, 0, Material.END_ROD);
        for (int y = 1; y <= 40; y++) b.setBlock(0, y, -2, Material.LADDER);
        b.placeChest(2, 2, 2); b.placeChest(0, 22, 0); b.setBossSpawn(0, 37, 0);
    }

    private void buildRuinedColosseum(StructureBuilder b) {
        b.filledCircle(0, 0, 0, 18, Material.SAND); b.filledCircle(0, 0, 0, 15, Material.SMOOTH_SANDSTONE);
        for (int i = 0; i < 4; i++) b.circle(0, i + 1, 0, 16 + i, Material.STONE_BRICKS);
        for (int a = 0; a < 360; a += 30) { int px = (int)(16*Math.cos(Math.toRadians(a))); int pz = (int)(16*Math.sin(Math.toRadians(a))); b.pillar(px, 1, 12, pz, Material.STONE_BRICK_WALL); }
        b.scatter(-20, 0, -20, 20, 12, 20, Material.STONE_BRICKS, Material.CRACKED_STONE_BRICKS, 0.25);
        b.scatter(-20, 0, -20, 20, 12, 20, Material.STONE_BRICKS, Material.MOSSY_STONE_BRICKS, 0.15);
        b.decay(-20, 5, -20, 20, 12, 20, 0.20);
        b.setBlock(-2, 1, 0, Material.IRON_BARS); b.setBlock(2, 1, 0, Material.IRON_BARS);
        b.setBlock(-2, 2, 0, Material.IRON_BARS); b.setBlock(2, 2, 0, Material.IRON_BARS);
        b.placeChest(0, 1, 0); b.setBossSpawn(0, 1, 5);
    }

    private void buildShrekHouse(StructureBuilder b) {
        b.filledCircle(0, 0, 0, 6, Material.DIRT);
        b.fillWalls(-4, 1, -3, 4, 5, 3, Material.OAK_PLANKS); b.hollowBox(-4, 1, -3, 4, 5, 3);
        b.fillBox(-5, 6, -4, 5, 6, 4, Material.OAK_PLANKS); b.fillBox(-4, 7, -3, 4, 7, 3, Material.OAK_PLANKS); b.fillBox(-3, 8, -2, 3, 8, 2, Material.OAK_PLANKS);
        b.pillar(3, 6, 10, 2, Material.COBBLESTONE); b.setBlock(3, 11, 2, Material.CAMPFIRE);
        b.setBlock(0, 1, -3, Material.AIR); b.setBlock(0, 2, -3, Material.AIR);
        b.fillBox(7, 1, -1, 9, 3, 1, Material.OAK_PLANKS); b.setBlock(8, 1, -1, Material.AIR); b.setBlock(8, 2, -1, Material.AIR);
        b.fillBox(-6, 0, -6, -3, 0, -4, Material.FARMLAND);
        b.setBlock(-2, 1, 0, Material.CRAFTING_TABLE); b.setBlock(2, 1, 0, Material.FURNACE);
        b.setBlock(-5, 1, 4, Material.RED_MUSHROOM); b.setBlock(-6, 1, 3, Material.BROWN_MUSHROOM);
        b.placeChest(0, 1, 2); b.setBossSpawn(0, 1, -5);
    }

    private void buildAncientTemple(StructureBuilder b) {
        for (int layer = 0; layer < 10; layer++) { int r = 15 - layer; b.fillBox(-r, layer, -r, r, layer, r, Material.SANDSTONE); }
        b.fillBox(-10, 1, -10, 10, 7, 10, Material.AIR);
        b.fillBox(-12, 1, -1, 12, 3, 1, Material.AIR); b.fillBox(-1, 1, -12, 1, 3, 12, Material.AIR);
        b.setBlock(0, 1, 0, Material.PRISMARINE); b.setBlock(-5, 1, 0, Material.PRISMARINE_BRICKS); b.setBlock(5, 1, 0, Material.PRISMARINE_BRICKS);
        b.setBlock(0, 7, 0, Material.GOLD_BLOCK); b.setBlock(-8, 1, -8, Material.GOLD_BLOCK); b.setBlock(8, 1, 8, Material.GOLD_BLOCK);
        b.placeChest(0, 1, 5); b.placeChest(0, 1, -5); b.setBossSpawn(0, 1, 0);
    }

    private void buildVolcano(StructureBuilder b) {
        for (int y = 0; y < 40; y++) { int r = 18 - (y * 18 / 40); if (r < 2) r = 2; b.filledCircle(0, y, 0, r, Material.BASALT); if (r > 4) b.filledCircle(0, y, 0, r - 3, Material.BLACKSTONE); }
        for (int y = 1; y < 35; y++) { int r = Math.max(1, 6 - (y / 8)); b.filledCircle(0, y, 0, r, Material.LAVA); }
        b.scatter(-18, 0, -18, 18, 5, 18, Material.BASALT, Material.MAGMA_BLOCK, 0.15);
        b.filledCircle(0, 38, 0, 3, Material.LAVA);
        b.fillBox(-4, 5, -4, 4, 8, 4, Material.AIR); b.fillBox(-4, 5, -4, 4, 5, 4, Material.OBSIDIAN);
        b.placeChest(0, 6, 0); b.setBossSpawn(0, 6, 3);
    }

    private void buildFortress(StructureBuilder b) {
        b.fillWalls(-22, 0, -22, 22, 8, 22, Material.DEEPSLATE_BRICKS); b.hollowBox(-22, 1, -22, 22, 8, 22);
        for (int[] c : new int[][]{{-22,-22},{22,-22},{-22,22},{22,22}}) { b.fillBox(c[0]-2, 0, c[1]-2, c[0]+2, 12, c[1]+2, Material.DEEPSLATE_BRICKS); b.fillBox(c[0]-1, 1, c[1]-1, c[0]+1, 12, c[1]+1, Material.AIR); }
        b.fillBox(-2, 1, -22, 2, 5, -22, Material.AIR);
        b.fillBox(-10, 1, -10, -5, 4, -5, Material.BLACKSTONE); b.fillBox(-9, 1, -9, -6, 3, -6, Material.AIR);
        b.setBlock(0, 7, -21, Material.IRON_CHAIN); b.setBlock(0, 6, -21, Material.LANTERN);
        b.placeChest(-8, 1, -7); b.placeChest(10, 1, 10); b.setBossSpawn(0, 1, 0);
    }

    private void buildGiganticCastle(StructureBuilder b) {
        b.fillWalls(-35, 0, -35, 35, 15, 35, Material.STONE_BRICKS); b.hollowBox(-35, 1, -35, 35, 15, 35);
        b.fillFloor(-34, -34, 34, 34, 0, Material.STONE_BRICKS);
        b.fillWalls(-12, 0, -12, 12, 20, 12, Material.DEEPSLATE_BRICKS); b.hollowBox(-12, 1, -12, 12, 20, 12);
        b.setBlock(0, 1, 8, Material.GOLD_BLOCK); b.setBlock(0, 2, 8, Material.GOLD_BLOCK);
        for (int[] c : new int[][]{{-12,-12},{12,-12},{-12,12},{12,12}}) { b.fillBox(c[0]-2, 0, c[1]-2, c[0]+2, 25, c[1]+2, Material.STONE_BRICKS); b.fillBox(c[0]-1, 1, c[1]-1, c[0]+1, 25, c[1]+1, Material.AIR); }
        b.fillBox(-3, 1, -35, 3, 8, -35, Material.AIR);
        b.setBlock(-4, 9, -35, Material.IRON_BARS); b.setBlock(4, 9, -35, Material.IRON_BARS);
        b.fillBox(-8, -5, -8, 8, -1, 8, Material.DEEPSLATE_BRICKS); b.fillBox(-7, -4, -7, 7, -1, 7, Material.AIR); b.setBlock(0, -4, 0, Material.IRON_BARS);
        b.placeChest(0, 1, 0); b.placeChest(5, -4, 5); b.placeChest(-20, 1, 20); b.setBossSpawn(0, 1, 5);
    }

    private void buildUltraVillage(StructureBuilder b) {
        b.fillWalls(-40, 0, -40, 40, 5, 40, Material.STONE_BRICKS); b.hollowBox(-40, 1, -40, 40, 5, 40);
        b.fillBox(-2, 1, -40, 2, 4, -40, Material.AIR);
        b.setBlock(-3, 5, -40, Material.STONE_BRICK_WALL); b.setBlock(3, 5, -40, Material.STONE_BRICK_WALL);
        for (int i = 0; i < 6; i++) {
            int hx = -25 + (i % 3) * 18; int hz = (i < 3) ? -20 : 10;
            b.fillBox(hx, 0, hz, hx+8, 0, hz+8, Material.COBBLESTONE);
            b.fillWalls(hx, 1, hz, hx+8, 4, hz+8, Material.OAK_PLANKS); b.hollowBox(hx, 1, hz, hx+8, 4, hz+8);
            b.fillBox(hx, 5, hz, hx+8, 5, hz+8, Material.OAK_PLANKS);
            b.setBlock(hx+4, 1, hz, Material.AIR); b.setBlock(hx+4, 2, hz, Material.AIR);
        }
        b.filledCircle(0, 0, 0, 3, Material.COBBLESTONE); b.setBlock(0, 0, 0, Material.WATER);
        b.pillar(-2, 1, 3, 0, Material.COBBLESTONE_WALL); b.pillar(2, 1, 3, 0, Material.COBBLESTONE_WALL);
        b.placeChest(-22, 1, -17); b.placeChest(14, 1, 13); b.placeChest(-5, 1, 13);
    }

    private void buildDungeonDeep(StructureBuilder b) {
        b.fillWalls(-12, 0, -12, 12, 6, 12, Material.DEEPSLATE_BRICKS); b.hollowBox(-12, 1, -12, 12, 6, 12);
        b.fillBox(-12, 1, -5, -18, 5, 5, Material.AIR); b.fillWalls(-18, 0, -5, -12, 5, 5, Material.DEEPSLATE_BRICKS);
        b.fillBox(12, 1, -5, 18, 5, 5, Material.AIR); b.fillWalls(12, 0, -5, 18, 5, 5, Material.DEEPSLATE_BRICKS);
        b.scatter(-12, 0, -12, 12, 0, 12, Material.DEEPSLATE_BRICKS, Material.SCULK, 0.1);
        for (int z = -4; z <= 4; z += 2) { b.setBlock(-11, 1, z, Material.IRON_BARS); b.setBlock(-11, 2, z, Material.IRON_BARS); }
        b.setBlock(0, 1, 0, Material.MOSSY_COBBLESTONE);
        b.placeChest(-15, 1, 0); b.placeChest(15, 1, 0); b.setBossSpawn(0, 1, 0);
    }

    // ══════════════════════════════════════
    //  NEW OVERWORLD (Plus Part 2)
    // ══════════════════════════════════════

    private void buildFrostDungeon(StructureBuilder b) {
        b.fillWalls(-15, 0, -15, 15, 8, 15, Material.PACKED_ICE); b.hollowBox(-15, 1, -15, 15, 8, 15);
        b.fillFloor(-14, -14, 14, 14, 0, Material.BLUE_ICE);
        for (int a = 0; a < 360; a += 45) {
            int px = (int)(10*Math.cos(Math.toRadians(a))); int pz = (int)(10*Math.sin(Math.toRadians(a)));
            b.pillar(px, 1, 7, pz, Material.PACKED_ICE); b.setBlock(px, 8, pz, Material.SEA_LANTERN);
        }
        b.fillBox(-5, 1, 8, 5, 5, 14, Material.AIR); b.fillBox(-5, 0, 8, 5, 0, 14, Material.BLUE_ICE);
        b.fillBox(-1, 1, 12, 1, 3, 12, Material.PACKED_ICE); b.setBlock(0, 4, 12, Material.SEA_LANTERN);
        b.fillBox(-15, 1, -2, -20, 4, 2, Material.AIR); b.fillWalls(-20, 0, -5, -15, 5, 5, Material.PACKED_ICE);
        b.fillBox(15, 1, -2, 20, 4, 2, Material.AIR); b.fillWalls(15, 0, -5, 20, 5, 5, Material.PACKED_ICE);
        b.scatter(-14, 0, -14, 14, 0, 14, Material.BLUE_ICE, Material.POWDER_SNOW, 0.08);
        b.placeChest(0, 1, 10); b.placeChest(-18, 1, 0); b.placeChest(18, 1, 0);
        b.setBossSpawn(0, 1, 11);
    }

    private void buildBanditHideout(StructureBuilder b) {
        b.fillWalls(-10, 0, -8, 10, 6, 8, Material.SANDSTONE); b.hollowBox(-10, 1, -8, 10, 6, 8);
        b.fillFloor(-9, -7, 9, 7, 0, Material.SAND);
        b.fillBox(-3, 1, -10, 3, 4, -8, Material.AIR);
        b.fillBox(-8, 1, -5, -6, 3, -3, Material.BARREL);
        b.fillBox(6, 1, 3, 8, 2, 5, Material.HAY_BLOCK);
        b.setBlock(0, 1, 0, Material.CAMPFIRE);
        b.fillBox(-5, 1, 8, 5, 4, 12, Material.AIR); b.fillWalls(-5, 0, 8, 5, 4, 12, Material.CUT_SANDSTONE);
        b.setBlock(0, 1, 8, Material.AIR); b.setBlock(0, 2, 8, Material.AIR);
        b.placeChest(-7, 1, -4); b.placeChest(7, 1, 4); b.placeChest(0, 1, 10);
        b.setBossSpawn(0, 1, 3);
    }

    private void buildSunkenRuins(StructureBuilder b) {
        b.filledCircle(0, 0, 0, 12, Material.PRISMARINE_BRICKS);
        b.filledCircle(0, 1, 0, 10, Material.PRISMARINE); b.filledCircle(0, 2, 0, 8, Material.PRISMARINE);
        for (int a = 0; a < 360; a += 40) { int px = (int)(10*Math.cos(Math.toRadians(a))); int pz = (int)(10*Math.sin(Math.toRadians(a))); b.pillar(px, 1, 4 + b.getRandom().nextInt(5), pz, Material.PRISMARINE_WALL); }
        b.fillBox(-2, 1, -2, 2, 1, 2, Material.DARK_PRISMARINE); b.setBlock(0, 2, 0, Material.SEA_LANTERN);
        b.scatter(-12, 0, -12, 12, 6, 12, Material.PRISMARINE_BRICKS, Material.PRISMARINE, 0.15);
        b.decay(-12, 3, -12, 12, 8, 12, 0.2);
        b.placeChest(0, 1, 5); b.placeChest(-6, 1, -3); b.setBossSpawn(0, 1, 0);
    }

    private void buildCursedGraveyard(StructureBuilder b) {
        b.filledCircle(0, 0, 0, 10, Material.PODZOL);
        b.scatter(-10, 0, -10, 10, 0, 10, Material.PODZOL, Material.SOUL_SOIL, 0.3);
        b.circle(0, 1, 0, 10, Material.IRON_BARS);
        int[][] graves = {{-6,3},{-3,5},{0,7},{3,5},{6,3},{-4,-3},{-1,-6},{2,-4},{5,-2},{-7,0}};
        for (int[] g : graves) { b.setBlock(g[0], 1, g[1], Material.STONE_BRICK_WALL); b.setBlock(g[0], 2, g[1], Material.STONE_BRICK_WALL); }
        b.pillar(-8, 1, 5, -6, Material.DARK_OAK_LOG); b.pillar(7, 1, 4, 5, Material.DARK_OAK_LOG);
        b.setBlock(0, 1, 0, Material.SOUL_CAMPFIRE);
        b.setBlock(-5, 1, 0, Material.SOUL_LANTERN); b.setBlock(5, 1, 0, Material.SOUL_LANTERN);
        b.scatter(-10, 1, -10, 10, 3, 10, Material.AIR, Material.COBWEB, 0.05);
        b.fillWalls(-2, 1, -2, 2, 4, 2, Material.DEEPSLATE_BRICKS); b.hollowBox(-2, 1, -2, 2, 4, 2);
        b.setBlock(0, 1, -2, Material.AIR); b.setBlock(0, 2, -2, Material.AIR);
        b.fillBox(-2, 5, -2, 2, 5, 2, Material.DEEPSLATE_BRICK_SLAB);
        b.placeChest(0, 1, 0); b.placeChest(-5, 0, 4); b.setBossSpawn(0, 1, -4);
    }

    private void buildSkyAltar(StructureBuilder b) {
        b.filledCircle(0, 0, 0, 8, Material.QUARTZ_BLOCK);
        b.filledCircle(0, -1, 0, 6, Material.QUARTZ_BLOCK); b.filledCircle(0, -2, 0, 4, Material.QUARTZ_BLOCK);
        for (int[] p : new int[][]{{-7,0},{7,0},{0,-7},{0,7}}) { b.pillar(p[0], 1, 8, p[1], Material.QUARTZ_PILLAR); b.setBlock(p[0], 9, p[1], Material.END_ROD); }
        b.fillBox(-1, 1, -1, 1, 1, 1, Material.GOLD_BLOCK);
        b.setBlock(0, 2, 0, Material.BEACON); b.setBlock(0, 3, 0, Material.END_ROD);
        b.setBlock(-4, 4, 0, Material.QUARTZ_STAIRS); b.setBlock(-3, 5, 0, Material.QUARTZ_BLOCK);
        b.setBlock(-2, 6, 0, Material.QUARTZ_BLOCK); b.setBlock(-1, 6, 0, Material.QUARTZ_BLOCK);
        b.setBlock(0, 6, 0, Material.QUARTZ_BLOCK); b.setBlock(1, 6, 0, Material.QUARTZ_BLOCK);
        b.setBlock(2, 6, 0, Material.QUARTZ_BLOCK); b.setBlock(3, 5, 0, Material.QUARTZ_BLOCK); b.setBlock(4, 4, 0, Material.QUARTZ_STAIRS);
        b.setBlock(-6, 2, -6, Material.AMETHYST_CLUSTER); b.setBlock(6, 2, 6, Material.AMETHYST_CLUSTER);
        b.setBlock(-6, 2, 6, Material.AMETHYST_CLUSTER); b.setBlock(6, 2, -6, Material.AMETHYST_CLUSTER);
        b.placeChest(3, 1, 3); b.placeChest(-3, 1, -3); b.setBossSpawn(0, 1, 4);
    }

    // ══════════════════════════════════════
    //  NETHER (original)
    // ══════════════════════════════════════

    private void buildCrimsonCitadel(StructureBuilder b) {
        b.fillWalls(-15, 0, -15, 15, 12, 15, Material.NETHER_BRICKS); b.hollowBox(-15, 1, -15, 15, 12, 15);
        b.fillFloor(-14, -14, 14, 14, 0, Material.CRIMSON_PLANKS);
        b.fillBox(-2, 1, 10, 2, 3, 12, Material.CRIMSON_PLANKS); b.setBlock(0, 2, 11, Material.GOLD_BLOCK);
        b.placeChest(0, 1, 0); b.placeChest(10, 1, -8); b.setBossSpawn(0, 2, 8);
    }

    private void buildSoulSanctum(StructureBuilder b) {
        b.fillWalls(-10, 0, -10, 10, 8, 10, Material.SOUL_SAND); b.hollowBox(-10, 1, -10, 10, 8, 10);
        b.scatter(-10, 0, -10, 10, 8, 10, Material.SOUL_SAND, Material.SOUL_SOIL, 0.3);
        b.setBlock(-5, 3, -5, Material.SOUL_LANTERN); b.setBlock(5, 3, 5, Material.SOUL_LANTERN);
        b.setBlock(0, 1, 0, Material.SOUL_CAMPFIRE);
        b.placeChest(3, 1, 3); b.setBossSpawn(0, 1, 4);
    }

    private void buildBasaltSpire(StructureBuilder b) {
        for (int y = 0; y < 40; y++) { int r = Math.max(2, 8-(y*6/40)); b.filledCircle(0, y, 0, r, Material.BASALT); }
        for (int y = 1; y < 35; y++) { double a = y * 15; int sx = (int)(3*Math.cos(Math.toRadians(a))); int sz = (int)(3*Math.sin(Math.toRadians(a))); b.setBlock(sx, y, sz, Material.AIR); b.setBlock(sx, y+1, sz, Material.AIR); }
        b.scatter(-8, 0, -8, 8, 40, 8, Material.BASALT, Material.MAGMA_BLOCK, 0.05);
        b.placeChest(0, 20, 0); b.setBossSpawn(0, 35, 0);
    }

    private void buildNetherDungeon(StructureBuilder b) {
        b.fillWalls(-12, 0, -8, 12, 6, 8, Material.NETHER_BRICKS); b.hollowBox(-12, 1, -8, 12, 6, 8);
        b.fillBox(-3, 1, -3, 3, 1, 3, Material.NETHER_BRICKS);
        b.placeChest(-10, 1, 0); b.placeChest(10, 1, 0); b.setBossSpawn(0, 2, 0);
    }

    private void buildPiglinPalace(StructureBuilder b) {
        b.fillWalls(-20, 0, -20, 20, 10, 20, Material.NETHER_BRICKS); b.hollowBox(-20, 1, -20, 20, 10, 20);
        b.fillFloor(-19, -19, 19, 19, 0, Material.GOLD_BLOCK);
        b.fillBox(-2, 1, 15, 2, 4, 18, Material.GOLD_BLOCK);
        b.fillBox(12, 1, 12, 18, 4, 18, Material.GOLD_BLOCK); b.fillBox(13, 1, 13, 17, 3, 17, Material.AIR);
        b.placeChest(15, 1, 15); b.placeChest(-15, 1, -15); b.setBossSpawn(0, 1, 12);
    }

    // ══════════════════════════════════════
    //  NEW NETHER (Plus Part 2)
    // ══════════════════════════════════════

    private void buildWitherSanctum(StructureBuilder b) {
        b.fillWalls(-18, 0, -18, 18, 15, 18, Material.NETHER_BRICKS); b.hollowBox(-18, 1, -18, 18, 15, 18);
        b.fillFloor(-17, -17, 17, 17, 0, Material.SOUL_SOIL);
        b.fillBox(-3, 1, -3, 3, 1, 3, Material.BLACKSTONE);
        b.setBlock(0, 2, 0, Material.SOUL_CAMPFIRE);
        b.setBlock(-2, 2, -2, Material.SOUL_LANTERN); b.setBlock(2, 2, 2, Material.SOUL_LANTERN);
        b.setBlock(2, 2, -2, Material.SOUL_LANTERN); b.setBlock(-2, 2, 2, Material.SOUL_LANTERN);
        for (int[] c : new int[][]{{-12,-12},{12,-12},{-12,12},{12,12}}) { b.pillar(c[0], 1, 10, c[1], Material.POLISHED_BLACKSTONE_BRICKS); b.setBlock(c[0], 11, c[1], Material.WITHER_SKELETON_SKULL); }
        b.fillBox(-18, 1, -5, -22, 8, 5, Material.AIR); b.fillWalls(-22, 0, -5, -18, 8, 5, Material.NETHER_BRICKS);
        b.fillBox(18, 1, -5, 22, 8, 5, Material.AIR); b.fillWalls(18, 0, -5, 22, 8, 5, Material.NETHER_BRICKS);
        b.scatter(-17, 0, -17, 17, 0, 17, Material.SOUL_SOIL, Material.SOUL_SAND, 0.2);
        b.placeChest(-20, 1, 0); b.placeChest(20, 1, 0); b.placeChest(0, 1, 12);
        b.setBossSpawn(0, 2, 5);
    }

    private void buildBlazeColosseum(StructureBuilder b) {
        b.filledCircle(0, 0, 0, 20, Material.MAGMA_BLOCK); b.filledCircle(0, 0, 0, 16, Material.NETHER_BRICKS);
        for (int i = 0; i < 4; i++) b.circle(0, i + 1, 0, 18 + i, Material.NETHER_BRICKS);
        for (int a = 0; a < 360; a += 60) { int px = (int)(14*Math.cos(Math.toRadians(a))); int pz = (int)(14*Math.sin(Math.toRadians(a))); b.pillar(px, 1, 6, pz, Material.NETHER_BRICK_FENCE); b.setBlock(px, 7, pz, Material.LAVA); }
        b.filledCircle(0, 0, 0, 8, Material.BLACKSTONE);
        b.setBlock(-6, 5, 0, Material.IRON_CHAIN); b.setBlock(6, 5, 0, Material.IRON_CHAIN);
        b.setBlock(0, 5, -6, Material.IRON_CHAIN); b.setBlock(0, 5, 6, Material.IRON_CHAIN);
        b.placeChest(5, 1, 5); b.placeChest(-5, 1, -5); b.setBossSpawn(0, 1, 0);
    }

    // ══════════════════════════════════════
    //  END (original)
    // ══════════════════════════════════════

    private void buildVoidShrine(StructureBuilder b) {
        b.filledCircle(0, 0, 0, 10, Material.OBSIDIAN); b.filledCircle(0, 1, 0, 8, Material.PURPUR_BLOCK);
        for (int a = 0; a < 360; a += 60) { int px = (int)(8*Math.cos(Math.toRadians(a))); int pz = (int)(8*Math.sin(Math.toRadians(a))); b.pillar(px, 1, 8, pz, Material.PURPUR_PILLAR); b.setBlock(px, 9, pz, Material.END_ROD); }
        b.setBlock(0, 2, 0, Material.OBSIDIAN); b.setBlock(0, 3, 0, Material.ENDER_CHEST);
        b.placeChest(3, 2, 0); b.placeChest(-3, 2, 0); b.setBossSpawn(0, 2, 5);
    }

    private void buildEnderMonastery(StructureBuilder b) {
        b.fillWalls(-15, 0, -10, 15, 10, 10, Material.PURPUR_BLOCK); b.hollowBox(-15, 1, -10, 15, 10, 10);
        b.fillFloor(-14, -9, 14, 9, 0, Material.END_STONE_BRICKS);
        for (int x = -12; x <= 12; x += 4) b.pillar(x, 1, 4, -8, Material.BOOKSHELF);
        b.pillar(-8, 1, 8, 0, Material.PURPUR_PILLAR); b.pillar(8, 1, 8, 0, Material.PURPUR_PILLAR);
        b.setBlock(0, 9, 0, Material.END_ROD);
        b.placeChest(5, 1, 5); b.placeChest(-5, 1, -5); b.setBossSpawn(0, 1, 0);
    }

    private void buildDragonsHoard(StructureBuilder b) {
        b.filledCircle(0, 0, 0, 8, Material.GOLD_BLOCK); b.filledCircle(0, 1, 0, 6, Material.GOLD_BLOCK);
        b.filledCircle(0, 2, 0, 4, Material.GOLD_BLOCK); b.filledCircle(0, 3, 0, 2, Material.GOLD_BLOCK);
        b.placeChest(0, 4, 0); b.placeChest(3, 2, 3); b.placeChest(-3, 2, -3);
    }

    // ══════════════════════════════════════
    //  NEW ABYSS (Plus Part 2)
    // ══════════════════════════════════════

    private void buildAbyssalCastle(StructureBuilder b) {
        b.fillWalls(-50, 0, -50, 50, 20, 50, Material.DEEPSLATE_BRICKS); b.hollowBox(-50, 1, -50, 50, 20, 50);
        b.fillFloor(-49, -49, 49, 49, 0, Material.DEEPSLATE_TILES);
        for (int[] c : new int[][]{{-50,-50},{50,-50},{-50,50},{50,50}}) { b.fillBox(c[0]-4, 0, c[1]-4, c[0]+4, 30, c[1]+4, Material.DEEPSLATE_BRICKS); b.fillBox(c[0]-3, 1, c[1]-3, c[0]+3, 30, c[1]+3, Material.AIR); b.setBlock(c[0], 31, c[1], Material.END_ROD); }
        b.fillBox(-4, 1, -50, 4, 10, -50, Material.AIR);
        b.fillWalls(-18, 0, -18, 18, 30, 18, Material.END_STONE_BRICKS); b.hollowBox(-18, 1, -18, 18, 30, 18);
        b.fillFloor(-17, -17, 17, 17, 0, Material.CRYING_OBSIDIAN);
        b.fillBox(-3, 1, 12, 3, 5, 16, Material.CRYING_OBSIDIAN);
        b.setBlock(0, 2, 14, Material.GOLD_BLOCK); b.setBlock(0, 3, 14, Material.GOLD_BLOCK);
        for (int y = 5; y <= 25; y += 5) { b.setBlock(18, y, 0, Material.PURPLE_STAINED_GLASS); b.setBlock(-18, y, 0, Material.PURPLE_STAINED_GLASS); b.setBlock(0, y, 18, Material.PURPLE_STAINED_GLASS); b.setBlock(0, y, -18, Material.PURPLE_STAINED_GLASS); }
        b.fillBox(20, 1, -15, 35, 8, -5, Material.AIR); b.fillWalls(20, 0, -15, 35, 8, -5, Material.DEEPSLATE_BRICKS);
        b.fillBox(-35, 1, 5, -20, 8, 15, Material.AIR); b.fillWalls(-35, 0, 5, -20, 8, 15, Material.DEEPSLATE_BRICKS);
        b.fillBox(20, 1, 5, 35, 8, 15, Material.AIR); b.fillWalls(20, 0, 5, 35, 8, 15, Material.DEEPSLATE_BRICKS);
        for (int x = 22; x <= 33; x += 3) b.pillar(x, 1, 4, 8, Material.BOOKSHELF);
        b.filledCircle(0, -5, 0, 18, Material.DEEPSLATE_BRICKS);
        b.filledCircle(0, -4, 0, 16, Material.AIR); b.filledCircle(0, -3, 0, 16, Material.AIR);
        b.filledCircle(0, -2, 0, 16, Material.AIR); b.filledCircle(0, -1, 0, 16, Material.AIR);
        b.filledCircle(0, -5, 0, 16, Material.OBSIDIAN);
        b.placeChest(0, 1, 0); b.placeChest(25, 1, -10); b.placeChest(-30, 1, 10); b.placeChest(25, 1, 10); b.placeChest(0, -4, 10);
        b.setBossSpawn(0, -4, 0);
    }

    private void buildVoidNexus(StructureBuilder b) {
        b.filledCircle(0, 0, 0, 12, Material.OBSIDIAN); b.filledCircle(0, 1, 0, 10, Material.END_STONE); b.filledCircle(0, 2, 0, 8, Material.END_STONE);
        b.pillar(0, 3, 20, 0, Material.PURPUR_PILLAR); b.setBlock(0, 21, 0, Material.END_ROD); b.setBlock(0, 22, 0, Material.END_ROD);
        for (int a = 0; a < 360; a += 45) { int px = (int)(9*Math.cos(Math.toRadians(a))); int pz = (int)(9*Math.sin(Math.toRadians(a))); b.pillar(px, 1, 12, pz, Material.OBSIDIAN); b.setBlock(px, 13, pz, Material.END_ROD); b.setBlock(px/2, 7, pz/2, Material.CRYING_OBSIDIAN); }
        b.filledCircle(0, -1, 0, 6, Material.OBSIDIAN); b.filledCircle(0, -2, 0, 6, Material.AIR);
        b.filledCircle(0, -3, 0, 6, Material.AIR); b.filledCircle(0, -4, 0, 6, Material.OBSIDIAN);
        b.placeChest(4, 1, 0); b.placeChest(-4, 1, 0); b.placeChest(0, -3, 0);
        b.setBossSpawn(0, 2, 5);
    }

    private void buildShatteredCathedral(StructureBuilder b) {
        b.fillWalls(-8, 0, -20, 8, 18, 20, Material.END_STONE_BRICKS); b.hollowBox(-8, 1, -20, 8, 18, 20);
        b.fillFloor(-7, -19, 7, 19, 0, Material.DEEPSLATE_TILES);
        for (int z = -19; z <= 19; z++) { b.setBlock(-4, 15, z, Material.END_STONE_BRICKS); b.setBlock(4, 15, z, Material.END_STONE_BRICKS); b.setBlock(-2, 17, z, Material.END_STONE_BRICKS); b.setBlock(2, 17, z, Material.END_STONE_BRICKS); b.setBlock(0, 18, z, Material.END_STONE_BRICKS); }
        for (int y = 5; y <= 14; y += 3) { b.setBlock(8, y, -10, Material.PURPLE_STAINED_GLASS); b.setBlock(-8, y, -10, Material.PURPLE_STAINED_GLASS); b.setBlock(8, y, 10, Material.PURPLE_STAINED_GLASS); b.setBlock(-8, y, 10, Material.PURPLE_STAINED_GLASS); }
        b.decay(-8, 10, -20, 8, 18, -10, 0.3); b.decay(-8, 1, 15, 8, 18, 20, 0.25);
        b.fillBox(-2, 1, 16, 2, 1, 18, Material.CRYING_OBSIDIAN);
        b.setBlock(0, 2, 17, Material.BEACON); b.setBlock(0, 3, 17, Material.END_ROD);
        for (int z = -15; z <= 10; z += 3) { b.setBlock(-5, 1, z, Material.PURPUR_STAIRS); b.setBlock(-4, 1, z, Material.PURPUR_STAIRS); b.setBlock(4, 1, z, Material.PURPUR_STAIRS); b.setBlock(5, 1, z, Material.PURPUR_STAIRS); }
        b.decay(-6, 1, -15, 6, 1, 10, 0.15);
        b.fillBox(-3, 0, -24, 3, 25, -20, Material.END_STONE_BRICKS); b.fillBox(-2, 1, -23, 2, 25, -21, Material.AIR);
        b.setBlock(0, 20, -22, Material.BELL); b.decay(-3, 15, -24, 3, 25, -20, 0.35);
        b.scatter(-7, 16, -19, 7, 18, 19, Material.END_STONE_BRICKS, Material.CRYING_OBSIDIAN, 0.08);
        b.placeChest(0, 1, 17); b.placeChest(-6, 1, 0); b.placeChest(6, 1, -12);
        b.setBossSpawn(0, 1, 12);
    }

    // ══════════════════════════════════════
    //  GENERIC FALLBACK + UTILITIES
    // ══════════════════════════════════════


    // ═══════════════════════════════════════════════════════════════
    //  OVERWORLD — Custom builds replacing generic (Part 3B)
    // ═══════════════════════════════════════════════════════════════

    private void buildThanosTemple(StructureBuilder b) {
        // Stepped pyramid base (5 steps, obsidian + purpur)
        for (int step = 0; step < 5; step++) {
            int r = 22 - step * 4;
            int y = step * 4;
            b.fillBox(-r, y, -r, r, y, r, Material.OBSIDIAN);
            b.fillWalls(-r, y + 1, -r, r, y + 3, r, Material.PURPUR_BLOCK);
            b.hollowBox(-r, y + 1, -r, r, y + 3, r);
        }
        // Top platform
        b.fillBox(-6, 20, -6, 6, 20, 6, Material.PURPUR_BLOCK);
        b.fillBox(-5, 21, -5, 5, 21, 5, Material.OBSIDIAN);
        // Throne
        b.fillBox(-1, 21, 2, 1, 21, 4, Material.GOLD_BLOCK);
        b.setBlock(0, 22, 3, Material.GOLD_BLOCK); b.setBlock(0, 23, 3, Material.GOLD_BLOCK);
        b.setBlock(-1, 23, 3, Material.GOLD_BLOCK); b.setBlock(1, 23, 3, Material.GOLD_BLOCK);
        b.setBlock(0, 24, 3, Material.GOLD_BLOCK);
        // Infinity Stone pedestals (6 around the top)
        Material[] stoneMats = {Material.RED_CONCRETE, Material.BLUE_CONCRETE, Material.YELLOW_CONCRETE, Material.ORANGE_CONCRETE, Material.GREEN_CONCRETE, Material.PURPLE_CONCRETE};
        int[][] pedestalPositions = {{-4, 0}, {4, 0}, {0, -4}, {0, 4}, {-3, -3}, {3, 3}};
        for (int i = 0; i < 6; i++) {
            int px = pedestalPositions[i][0]; int pz = pedestalPositions[i][1];
            b.setBlock(px, 21, pz, Material.QUARTZ_PILLAR);
            b.setBlock(px, 22, pz, Material.QUARTZ_PILLAR);
            b.setBlock(px, 23, pz, stoneMats[i]);
        }
        // Entrance
        b.fillBox(-3, 1, -22, 3, 6, -22, Material.AIR);
        b.setBlock(-4, 7, -22, Material.PURPUR_PILLAR); b.setBlock(4, 7, -22, Material.PURPUR_PILLAR);
        // Grand staircase inside
        for (int s = 0; s < 20; s++) {
            int sz = -20 + s; int sy = s;
            b.fillBox(-2, sy, sz, 2, sy, sz, Material.PURPUR_STAIRS);
        }
        // Underground vault
        b.fillBox(-15, -8, -15, 15, -1, 15, Material.OBSIDIAN);
        b.fillBox(-14, -7, -14, 14, -1, 14, Material.AIR);
        b.fillFloor(-14, -14, 14, 14, -8, Material.CRYING_OBSIDIAN);
        for (int[] c : new int[][]{{-10, -10}, {10, -10}, {-10, 10}, {10, 10}}) {
            b.pillar(c[0], -7, -1, c[1], Material.PURPUR_PILLAR);
            b.setBlock(c[0], -1, c[1], Material.END_ROD);
        }
        b.setBlock(0, -7, 0, Material.BEACON);
        // Decay for ruins feel
        b.decay(-22, 8, -22, 22, 20, 22, 0.08);
        b.scatter(-22, 0, -22, 22, 20, 22, Material.PURPUR_BLOCK, Material.CRACKED_STONE_BRICKS, 0.06);
        // Chests and boss
        b.placeChest(0, 21, 0); b.placeChest(-12, -7, 0); b.placeChest(12, -7, 0);
        b.setBossSpawn(0, 21, -2);
    }

    private void buildPillagerFortress(StructureBuilder b) {
        // Outer walls
        b.fillWalls(-28, 0, -28, 28, 10, 28, Material.COBBLESTONE);
        b.hollowBox(-28, 1, -28, 28, 10, 28);
        b.fillFloor(-27, -27, 27, 27, 0, Material.STONE_BRICKS);
        // Battlements on top
        for (int x = -28; x <= 28; x += 2) {
            b.setBlock(x, 11, -28, Material.COBBLESTONE_WALL);
            b.setBlock(x, 11, 28, Material.COBBLESTONE_WALL);
        }
        for (int z = -28; z <= 28; z += 2) {
            b.setBlock(-28, 11, z, Material.COBBLESTONE_WALL);
            b.setBlock(28, 11, z, Material.COBBLESTONE_WALL);
        }
        // Corner watchtowers (4)
        for (int[] c : new int[][]{{-28, -28}, {28, -28}, {-28, 28}, {28, 28}}) {
            b.fillBox(c[0] - 3, 0, c[1] - 3, c[0] + 3, 16, c[1] + 3, Material.STONE_BRICKS);
            b.fillBox(c[0] - 2, 1, c[1] - 2, c[0] + 2, 15, c[1] + 2, Material.AIR);
            b.fillBox(c[0] - 4, 14, c[1] - 4, c[0] + 4, 14, c[1] + 4, Material.DARK_OAK_PLANKS);
            b.setBlock(c[0], 15, c[1], Material.LANTERN);
            for (int y = 1; y <= 14; y++) b.setBlock(c[0] + 1, y, c[1] - 2, Material.LADDER);
        }
        // Main gate
        b.fillBox(-3, 1, -28, 3, 7, -28, Material.AIR);
        b.setBlock(-4, 8, -28, Material.DARK_OAK_LOG); b.setBlock(4, 8, -28, Material.DARK_OAK_LOG);
        b.fillBox(-3, 7, -28, 3, 7, -28, Material.DARK_OAK_PLANKS);
        // Pillager banners along entrance
        for (int x = -2; x <= 2; x += 2) b.setBlock(x, 8, -27, Material.WHITE_BANNER);
        // Central courtyard
        b.filledCircle(0, 0, 0, 8, Material.SMOOTH_STONE);
        b.setBlock(0, 1, 0, Material.CAMPFIRE);
        b.pillar(-6, 1, 4, 0, Material.DARK_OAK_FENCE); b.pillar(6, 1, 4, 0, Material.DARK_OAK_FENCE);
        // Barracks (left side)
        b.fillBox(-25, 0, -15, -15, 0, -5, Material.STONE_BRICKS);
        b.fillWalls(-25, 1, -15, -15, 5, -5, Material.DARK_OAK_PLANKS);
        b.hollowBox(-25, 1, -15, -15, 5, -5);
        b.fillBox(-25, 6, -15, -15, 6, -5, Material.DARK_OAK_PLANKS);
        b.setBlock(-20, 1, -15, Material.AIR); b.setBlock(-20, 2, -15, Material.AIR);
        for (int bx = -24; bx <= -16; bx += 4) { b.setBlock(bx, 1, -12, Material.RED_BED); b.setBlock(bx, 1, -8, Material.RED_BED); }
        // Armory (right side)
        b.fillBox(15, 0, -15, 25, 0, -5, Material.STONE_BRICKS);
        b.fillWalls(15, 1, -15, 25, 5, -5, Material.COBBLESTONE);
        b.hollowBox(15, 1, -15, 25, 5, -5);
        b.fillBox(15, 6, -15, 25, 6, -5, Material.DARK_OAK_PLANKS);
        b.setBlock(20, 1, -15, Material.AIR); b.setBlock(20, 2, -15, Material.AIR);
        b.setBlock(16, 1, -13, Material.ANVIL); b.setBlock(17, 1, -13, Material.GRINDSTONE);
        b.setBlock(24, 1, -6, Material.BARREL); b.setBlock(23, 1, -6, Material.BARREL);
        // Command tent (rear)
        b.fillBox(-8, 0, 10, 8, 0, 22, Material.STONE_BRICKS);
        b.fillWalls(-8, 1, 10, 8, 6, 22, Material.DARK_OAK_PLANKS);
        b.hollowBox(-8, 1, 10, 8, 6, 22);
        b.fillBox(-8, 7, 10, 8, 7, 22, Material.DARK_OAK_PLANKS);
        b.setBlock(0, 1, 10, Material.AIR); b.setBlock(0, 2, 10, Material.AIR);
        b.setBlock(0, 1, 16, Material.CRAFTING_TABLE); b.setBlock(1, 1, 16, Material.CARTOGRAPHY_TABLE);
        // Dungeon below
        b.fillBox(-10, -6, -10, 10, -1, 10, Material.STONE_BRICKS);
        b.fillBox(-9, -5, -9, 9, -1, 9, Material.AIR);
        for (int z = -8; z <= 8; z += 4) {
            b.setBlock(-8, -5, z, Material.IRON_BARS); b.setBlock(-8, -4, z, Material.IRON_BARS);
            b.setBlock(8, -5, z, Material.IRON_BARS); b.setBlock(8, -4, z, Material.IRON_BARS);
        }
        // Chests and boss
        b.placeChest(-22, 1, -10); b.placeChest(22, 1, -10); b.placeChest(0, 1, 18); b.placeChest(0, -5, 0);
        b.setBossSpawn(0, 1, 5);
    }

    private void buildPillagerAirship(StructureBuilder b) {
        // Hull (floating 15 blocks above ground)
        int baseY = 15;
        // Main deck hull — boat shape
        for (int z = -12; z <= 12; z++) {
            int halfWidth = z < -8 ? (z + 12) : (z > 8 ? (12 - z) : 4);
            if (halfWidth < 1) halfWidth = 1;
            b.fillBox(-halfWidth, baseY, z, halfWidth, baseY, z, Material.DARK_OAK_PLANKS);
            // Hull sides (walls, 3 blocks high)
            b.setBlock(-halfWidth, baseY + 1, z, Material.DARK_OAK_PLANKS);
            b.setBlock(halfWidth, baseY + 1, z, Material.DARK_OAK_PLANKS);
            b.setBlock(-halfWidth, baseY + 2, z, Material.DARK_OAK_FENCE);
            b.setBlock(halfWidth, baseY + 2, z, Material.DARK_OAK_FENCE);
        }
        // Fill deck interior
        b.fillFloor(-3, -10, 3, 10, baseY, Material.OAK_PLANKS);
        // Below-deck cargo hold
        b.fillBox(-3, baseY - 3, -6, 3, baseY - 1, 6, Material.DARK_OAK_PLANKS);
        b.fillBox(-2, baseY - 2, -5, 2, baseY - 1, 5, Material.AIR);
        b.setBlock(0, baseY - 2, 0, Material.BARREL);
        b.setBlock(1, baseY - 2, 2, Material.BARREL);
        b.setBlock(-1, baseY - 2, -3, Material.BARREL);
        // Captain's cabin at the stern
        b.fillBox(-3, baseY + 1, 6, 3, baseY + 4, 10, Material.DARK_OAK_PLANKS);
        b.fillBox(-2, baseY + 1, 7, 2, baseY + 3, 9, Material.AIR);
        b.setBlock(0, baseY + 1, 6, Material.AIR); b.setBlock(0, baseY + 2, 6, Material.AIR);
        b.setBlock(0, baseY + 1, 8, Material.CRAFTING_TABLE);
        b.setBlock(1, baseY + 3, 8, Material.LANTERN);
        // Balloon above (big wool envelope)
        int balloonY = baseY + 6;
        for (int y = 0; y < 8; y++) {
            int r = y < 2 ? (2 + y) : (y > 5 ? (9 - y) : 4);
            b.filledCircle(0, balloonY + y, 0, r, Material.GRAY_WOOL);
        }
        // Ropes connecting balloon to hull
        for (int[] rope : new int[][]{{-2, -4}, {2, -4}, {-2, 4}, {2, 4}}) {
            for (int ry = baseY + 3; ry <= balloonY; ry++) {
                b.setBlock(rope[0], ry, rope[1], Material.IRON_BARS);
            }
        }
        // Mast and crow's nest at the bow
        b.pillar(0, baseY + 1, 10, -10, Material.OAK_LOG);
        b.fillBox(-2, baseY + 11, -12, 2, baseY + 11, -8, Material.OAK_PLANKS);
        b.circle(0, baseY + 12, -10, 2, Material.OAK_FENCE);
        // Crossbow mounts on deck
        b.setBlock(-3, baseY + 1, -6, Material.DARK_OAK_FENCE); b.setBlock(-3, baseY + 2, -6, Material.TRIPWIRE_HOOK);
        b.setBlock(3, baseY + 1, 0, Material.DARK_OAK_FENCE); b.setBlock(3, baseY + 2, 0, Material.TRIPWIRE_HOOK);
        // Banners
        b.setBlock(0, baseY + 12, -10, Material.WHITE_BANNER);
        b.setBlock(-4, baseY + 2, 0, Material.WHITE_BANNER);
        b.setBlock(4, baseY + 2, 0, Material.WHITE_BANNER);
        // Chests and boss
        b.placeChest(0, baseY + 1, 9); b.placeChest(0, baseY - 2, 3); b.placeChest(1, baseY + 1, -4);
        b.setBossSpawn(0, baseY + 1, 0);
    }

        private void buildGenericStructure(StructureBuilder b, StructureType type) {
        int hx = type.getSizeX() / 2; int hz = type.getSizeZ() / 2;
        b.fillBox(-hx, 0, -hz, hx, 0, hz, Material.STONE_BRICKS);
        b.fillWalls(-hx, 1, -hz, hx, 4, hz, Material.STONE_BRICKS); b.hollowBox(-hx, 1, -hz, hx, 4, hz);
        b.setBlock(0, 1, -hz, Material.AIR); b.setBlock(0, 2, -hz, Material.AIR);
        b.placeChest(0, 1, 0);
        if (type.hasBoss()) b.setBossSpawn(0, 1, hz/2);
    }

    private boolean checkSpacing(String worldName, int x, int z) {
        List<long[]> structures = generatedStructures.computeIfAbsent(worldName, k -> new ArrayList<>());
        for (long[] pos : structures) { double dx = pos[0] - x; double dz = pos[1] - z; if (dx * dx + dz * dz < MIN_SPACING * MIN_SPACING) return false; }
        return true;
    }

    private void recordStructure(String worldName, int x, int z) {
        generatedStructures.computeIfAbsent(worldName, k -> new ArrayList<>()).add(new long[]{x, z});
    }

    private int findNetherFloor(World world, int x, int z) {
        for (int y = 40; y < 100; y++) { if (!world.getBlockAt(x, y, z).getType().isAir() && world.getBlockAt(x, y+1, z).getType().isAir() && world.getBlockAt(x, y+2, z).getType().isAir()) return y + 1; }
        return -1;
    }

    private int findOceanFloor(World world, int x, int z) {
        for (int y = world.getHighestBlockYAt(x, z); y > world.getMinHeight(); y--) { Material mat = world.getBlockAt(x, y, z).getType(); if (mat != Material.WATER && mat != Material.KELP_PLANT && mat != Material.SEAGRASS && !mat.isAir()) return y; }
        return -1;
    }

    public StructureBossManager getBossManager() { return bossManager; }
    public StructureLootPopulator getLootPopulator() { return lootPopulator; }
}
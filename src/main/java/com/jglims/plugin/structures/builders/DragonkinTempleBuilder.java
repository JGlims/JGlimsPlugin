package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

/**
 * DragonkinTempleBuilder — ornate stone-brick temple with dragon-scale walls, meditation pool,
 * training courtyard, weapon-display wing, inner sanctum with beacon, dragon statue gates,
 * bookshelf library, incense braziers, ceremonial altar, and prismarine tiered roof.
 */
public final class DragonkinTempleBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // ── Stepped foundation platform (3 tiers, mixed materials) ──────
        for (int step = 0; step < 3; step++) {
            int r = 22 - step * 3;
            b.fillBox(-r, step, -r, r, step, r, Material.STONE_BRICKS);
            b.fillWalls(-r, step, -r, r, step, r, Material.POLISHED_DIORITE);
        }
        // Corner markers
        for (int[] c : new int[][]{{-22, -22}, {22, -22}, {-22, 22}, {22, 22}}) {
            b.pillar(c[0], 0, 2, c[1], Material.CHISELED_STONE_BRICKS);
            b.setBlock(c[0], 3, c[1], Material.LANTERN);
        }

        // ── Main temple building ────────────────────────────────────────
        b.fillWalls(-16, 3, -16, 16, 16, 16, Material.STONE_BRICKS);
        b.hollowBox(-16, 4, -16, 16, 16, 16);
        b.fillFloor(-15, -15, 15, 15, 3, Material.DARK_PRISMARINE);
        // Wall mid-band (mossy + cracked variants for weathering)
        b.scatter(-16, 3, -16, 16, 16, 16, Material.STONE_BRICKS, Material.MOSSY_STONE_BRICKS, 0.15);
        b.scatter(-16, 3, -16, 16, 16, 16, Material.STONE_BRICKS, Material.CRACKED_STONE_BRICKS, 0.08);

        // ── Floor diamond inlay ─────────────────────────────────────────
        for (int x = -10; x <= 10; x += 5)
            for (int z = -10; z <= 10; z += 5) {
                b.setBlock(x, 3, z, Material.PRISMARINE_BRICKS);
                b.setBlock(x + 1, 3, z, Material.PRISMARINE);
                b.setBlock(x - 1, 3, z, Material.PRISMARINE);
                b.setBlock(x, 3, z + 1, Material.PRISMARINE);
                b.setBlock(x, 3, z - 1, Material.PRISMARINE);
            }

        // ── Prismarine pillar columns with capitals ─────────────────────
        for (int x = -12; x <= 12; x += 6)
            for (int z = -12; z <= 12; z += 6) {
                if (Math.abs(x) <= 3 && Math.abs(z) <= 3) continue;
                b.pillar(x, 4, 15, z, Material.PRISMARINE_BRICKS);
                b.setBlock(x, 4, z, Material.DARK_PRISMARINE);   // base
                b.setBlock(x, 15, z, Material.DARK_PRISMARINE);  // capital
                b.setBlock(x, 16, z, Material.SEA_LANTERN);      // capital light
            }

        // ── Training courtyard (south exterior) ─────────────────────────
        b.fillBox(-12, 2, -22, 12, 2, -17, Material.STONE_BRICKS);
        b.fillBox(-10, 3, -21, 10, 3, -18, Material.SAND);
        // Training dummies with armor
        for (int x = -8; x <= 8; x += 4) {
            b.pillar(x, 3, 5, -20, Material.OAK_FENCE);
            b.setBlock(x, 6, -20, Material.CARVED_PUMPKIN);
            b.setBlock(x, 4, -20, Material.BROWN_WOOL);  // "chestplate" placeholder
        }
        // Training mat pattern
        for (int x = -8; x <= 8; x += 4)
            for (int z = -20; z >= -21; z--) {
                b.setBlock(x - 1, 3, z, Material.RED_TERRACOTTA);
                b.setBlock(x + 1, 3, z, Material.RED_TERRACOTTA);
            }
        // Courtyard corner braziers
        for (int[] c : new int[][]{{-11, -22}, {11, -22}, {-11, -17}, {11, -17}}) {
            b.pillar(c[0], 3, 5, c[1], Material.POLISHED_BLACKSTONE);
            b.setBlock(c[0], 6, c[1], Material.CAMPFIRE);
        }

        // ── Inner sanctum (raised platform, chiseled pattern) ───────────
        b.fillBox(-5, 3, -5, 5, 5, 5, Material.PRISMARINE_BRICKS);
        b.fillBox(-4, 5, -4, 4, 5, 4, Material.DARK_PRISMARINE);
        // Sanctum ring
        b.circle(0, 5, 0, 4, Material.PRISMARINE_WALL);
        // Beacon with pyramid base
        b.fillBox(-1, 5, -1, 1, 5, 1, Material.DIAMOND_BLOCK);
        b.setBlock(0, 6, 0, Material.BEACON);
        b.setBlock(0, 7, 0, Material.END_ROD);
        // Four incense pedestals around the beacon
        for (int[] inc : new int[][]{{-3, 0}, {3, 0}, {0, -3}, {0, 3}}) {
            b.setBlock(inc[0], 6, inc[1], Material.POLISHED_BLACKSTONE);
            b.setBlock(inc[0], 7, inc[1], Material.CAMPFIRE);
        }

        // ── Meditation pool (east wing) ─────────────────────────────────
        b.fillBox(8, 3, -6, 15, 3, 6, Material.PRISMARINE_BRICKS);
        b.fillBox(9, 3, -5, 14, 3, 5, Material.WATER);
        b.fillFloor(9, -5, 14, 5, 2, Material.PRISMARINE);
        b.setBlock(12, 4, 0, Material.LILY_PAD);
        b.setBlock(10, 4, -3, Material.LILY_PAD);
        b.setBlock(11, 4, 3, Material.LILY_PAD);
        // Meditation cushions on the rim
        for (int z : new int[]{-5, 0, 5}) {
            b.setBlock(15, 4, z, Material.RED_CARPET);
        }
        // Koi pond lanterns under water
        b.setBlock(11, 3, -2, Material.SEA_LANTERN);
        b.setBlock(12, 3, 2, Material.SEA_LANTERN);

        // ── Weapon displays (west wing) ─────────────────────────────────
        b.fillBox(-15, 3, -6, -8, 8, 6, Material.STONE_BRICKS);
        b.fillBox(-14, 4, -5, -9, 7, 5, Material.AIR);
        b.fillFloor(-14, -5, -9, 5, 3, Material.POLISHED_ANDESITE);
        // Display racks (fence + trapdoor pairs)
        for (int z = -4; z <= 4; z += 2) {
            b.setBlock(-14, 4, z, Material.OAK_FENCE);
            b.setBlock(-14, 5, z, Material.OAK_FENCE);
            b.setBlock(-14, 6, z, Material.OAK_TRAPDOOR);
            b.setBlock(-9, 4, z, Material.OAK_FENCE);
            b.setBlock(-9, 5, z, Material.OAK_FENCE);
        }
        b.wallLighting(-14, 6, -5, -9, -5, 3, Material.LANTERN);

        // ── Library wing (north interior corner) ────────────────────────
        b.fillBox(-14, 4, 8, -9, 7, 14, Material.AIR);
        b.fillFloor(-14, 8, -9, 14, 3, Material.POLISHED_DIORITE);
        for (int x = -13; x <= -10; x++) {
            b.setBlock(x, 4, 13, Material.BOOKSHELF);
            b.setBlock(x, 5, 13, Material.BOOKSHELF);
            b.setBlock(x, 6, 13, Material.BOOKSHELF);
        }
        b.setBlock(-11, 4, 10, Material.LECTERN);
        b.setBlock(-11, 4, 11, Material.ENCHANTING_TABLE);
        b.setBlock(-11, 7, 11, Material.LANTERN);

        // ── Altar alcove (north) ────────────────────────────────────────
        b.fillBox(-3, 4, 14, 3, 8, 15, Material.AIR);
        b.fillBox(-2, 4, 15, 2, 4, 15, Material.GILDED_BLACKSTONE);
        b.setBlock(0, 5, 15, Material.CHISELED_QUARTZ_BLOCK);
        b.setBlock(-1, 5, 15, Material.QUARTZ_PILLAR);
        b.setBlock(1, 5, 15, Material.QUARTZ_PILLAR);
        b.setBlock(0, 6, 15, Material.SEA_LANTERN);
        // Altar offerings
        b.setBlock(-2, 5, 15, Material.CANDLE);
        b.setBlock(2, 5, 15, Material.CANDLE);

        // ── Dragon-scale wall carvings (gold + prismarine cascade) ──────
        for (int y = 6; y <= 14; y += 2) {
            for (int[] side : new int[][]{{-16, 0}, {16, 0}}) {
                b.setBlock(side[0], y, side[1], Material.GOLD_BLOCK);
                b.setBlock(side[0], y + 1, side[1] - 1, Material.GOLD_BLOCK);
                b.setBlock(side[0], y + 1, side[1] + 1, Material.GOLD_BLOCK);
                b.setBlock(side[0], y, side[1] - 2, Material.DARK_PRISMARINE);
                b.setBlock(side[0], y, side[1] + 2, Material.DARK_PRISMARINE);
            }
        }

        // ── Dragon guardian statues flanking entrance ───────────────────
        for (int[] g : new int[][]{{-5, -17}, {5, -17}}) {
            b.fillBox(g[0], 3, g[1], g[0], 4, g[1], Material.POLISHED_BLACKSTONE);
            b.setBlock(g[0], 5, g[1], Material.DRAGON_HEAD);
            b.setBlock(g[0] + (g[0] < 0 ? 1 : -1), 3, g[1], Material.GILDED_BLACKSTONE);
        }

        // ── Tiered pagoda-style roof ────────────────────────────────────
        b.pyramidRoof(-16, -16, 16, 16, 16, Material.DARK_PRISMARINE);
        // Second-tier smaller pyramid for layered look
        b.pyramidRoof(-10, -10, 10, 10, 20, Material.PRISMARINE_BRICKS);
        b.setBlock(0, 26, 0, Material.END_ROD);
        b.setBlock(0, 27, 0, Material.SEA_LANTERN);

        // ── Entrance (south) ────────────────────────────────────────────
        b.archDoorway(0, 3, -16, 6, 8, Material.PRISMARINE_BRICKS);
        // Entry steps
        for (int s = 0; s < 3; s++) {
            b.fillBox(-3 - s, 2 - s, -17 - s, 3 + s, 2 - s, -17 - s, Material.STONE_BRICK_STAIRS);
        }

        // ── Lighting ────────────────────────────────────────────────────
        b.chandelier(0, 16, 0, 3);
        b.wallLighting(-15, 8, -16, 15, -16, 5, Material.LANTERN);
        b.wallLighting(-15, 8, 16, 15, 16, 5, Material.LANTERN);
        b.wallLighting(-16, 8, -15, -16, 15, 5, Material.LANTERN);
        b.wallLighting(16, 8, -15, 16, 15, 5, Material.LANTERN);

        // ── Weathering ──────────────────────────────────────────────────
        b.decay(-22, 3, -22, 22, 16, 22, 0.06);
        b.addVines(-22, 3, -22, 22, 16, 22, 0.08);

        // ── Loot ────────────────────────────────────────────────────────
        b.placeChest(0, 6, 3);    // inner sanctum
        b.placeChest(-12, 4, 0);  // weapons wing
        b.placeChest(12, 4, 4);   // pool overlook
        b.placeChest(-11, 4, 12); // library
        b.placeChest(0, 5, 14);   // altar offering
        b.setBossSpawn(0, 6, -3);
    }
}

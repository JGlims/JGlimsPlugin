package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

/**
 * AncientTempleBuilder — massive stepped jungle ziggurat with internal shrine,
 * hypogeum crypt, guardian statues and extensive weathering.
 *
 * <p>Architecture:
 * <ul>
 *   <li>5-tier stepped pyramid on a 34×34 plinth (60 blocks across at base)</li>
 *   <li>Grand staircase carved into the south face</li>
 *   <li>Summit shrine with 8 pillars, altar, rose-window oculus</li>
 *   <li>Carved serpent head guardians at the four tier junctions</li>
 *   <li>Buried crypt: central sarcophagus chamber with 4 side alcoves</li>
 *   <li>Every block is heavily weathered: mossy + cracked + vines + decay</li>
 * </ul>
 */
public final class AncientTempleBuilder implements IStructureBuilder {

    private static final Material STONE   = Material.STONE_BRICKS;
    private static final Material MOSSY   = Material.MOSSY_STONE_BRICKS;
    private static final Material CRACKED = Material.CRACKED_STONE_BRICKS;
    private static final Material CHISEL  = Material.CHISELED_STONE_BRICKS;
    private static final Material GOLD    = Material.GOLD_BLOCK;
    private static final Material DIAMOND = Material.DIAMOND_BLOCK;
    private static final Material TORCH   = Material.TORCH;
    private static final Material SOUL    = Material.SOUL_FIRE;
    private static final Material EMERALD = Material.EMERALD_BLOCK;

    @Override
    public void build(StructureBuilder b) {
        // ─── 1. Plinth foundation — sunk 1 block, 34×34 ────────────
        b.fillBox(-17, -1, -17, 17, 0, 17, Material.SMOOTH_STONE);
        b.fillWalls(-17, 0, -17, 17, 1, 17, CHISEL);

        // ─── 2. Stepped ziggurat — 5 tiers, each tier 2 blocks tall, shrinking by 3 ──
        int[] tierSize = {15, 12, 9, 6, 3};
        int[] tierBase = {1, 3, 5, 7, 9};
        for (int i = 0; i < tierSize.length; i++) {
            int s = tierSize[i];
            int y = tierBase[i];
            // Core of the tier (solid fill)
            b.fillBox(-s, y, -s, s, y + 1, s, STONE);
            // Decorative rim
            b.fillWalls(-s, y, -s, s, y + 1, s, MOSSY);
            // Corner accent blocks
            b.setBlock(-s, y + 1, -s, CHISEL);
            b.setBlock( s, y + 1, -s, CHISEL);
            b.setBlock(-s, y + 1,  s, CHISEL);
            b.setBlock( s, y + 1,  s, CHISEL);
        }

        // ─── 3. Grand staircase down the south face ────────────────
        for (int step = 0; step < 10; step++) {
            int y = step + 1;
            int z = 17 - step;
            // Stair tread (3-wide)
            b.setStairs(-1, y, z, Material.STONE_BRICK_STAIRS, BlockFace.SOUTH, false);
            b.setStairs( 0, y, z, Material.STONE_BRICK_STAIRS, BlockFace.SOUTH, false);
            b.setStairs( 1, y, z, Material.STONE_BRICK_STAIRS, BlockFace.SOUTH, false);
            // Banister walls
            b.setBlock(-2, y, z, CHISEL);
            b.setBlock( 2, y, z, CHISEL);
            if (step % 2 == 0) {
                b.setBlock(-2, y + 1, z, TORCH);
                b.setBlock( 2, y + 1, z, TORCH);
            }
        }

        // ─── 4. Summit shrine — 8 columns + ceiling + oculus ──────
        int topY = 11;
        for (int dx = -2; dx <= 2; dx += 2)
            for (int dz = -2; dz <= 2; dz += 2)
                if (!(dx == 0 && dz == 0))
                    b.pillar(dx, topY, topY + 5, dz, CHISEL);
        // Roof slab with rose-window oculus
        b.fillBox(-3, topY + 6, -3, 3, topY + 6, 3, STONE);
        b.roseWindow(0, topY + 6, 0, 2, Material.LIGHT_BLUE_STAINED_GLASS, BlockFace.UP);
        // Central altar platform
        b.fillBox(-1, topY, -1, 1, topY, 1, GOLD);
        b.setBlock(0, topY + 1, 0, DIAMOND);
        b.setBlock(0, topY + 2, 0, Material.BEACON);
        // Altar braziers
        b.setBlock(-2, topY + 1, -2, SOUL);
        b.setBlock( 2, topY + 1, -2, SOUL);
        b.setBlock(-2, topY + 1,  2, SOUL);
        b.setBlock( 2, topY + 1,  2, SOUL);
        // Loot in shrine corners
        b.placeChest(-2, topY, -2);
        b.placeChest( 2, topY,  2);

        // ─── 5. Serpent guardian heads — chiseled props at the tier corners ─
        serpentHead(b, -15, 2, -15);
        serpentHead(b,  15, 2, -15);
        serpentHead(b, -15, 2,  15);
        serpentHead(b,  15, 2,  15);

        // ─── 6. Hypogeum — buried crypt, accessible via trapdoor at stair base ─
        b.fillBox(-8, -10, -8, 8, -3, 8, Material.AIR);
        // Crypt walls
        b.fillWalls(-8, -10, -8, 8, -3, 8, MOSSY);
        // Crypt floor
        b.fillFloor(-8, -8, 8, 8, -10, Material.POLISHED_ANDESITE);
        // Ceiling
        b.fillFloor(-8, -8, 8, 8, -3, STONE);
        // Central sarcophagus
        b.fillBox(-1, -9, -2, 1, -8, 2, Material.POLISHED_BLACKSTONE);
        b.setBlock(0, -7, -2, CHISEL);
        b.setBlock(0, -7,  2, CHISEL);
        b.setBlock(0, -7,  0, Material.SOUL_LANTERN);
        // 4 alcoves in the walls for side chests
        b.fillBox(-7, -9, 0, -5, -7, 0, Material.AIR);
        b.fillBox( 5, -9, 0,  7, -7, 0, Material.AIR);
        b.fillBox(0, -9, -7, 0, -7, -5, Material.AIR);
        b.fillBox(0, -9,  5, 0, -7,  7, Material.AIR);
        b.placeChest(-6, -9, 0);
        b.placeChest( 6, -9, 0);
        b.placeChest(0, -9, -6);
        b.placeChest(0, -9,  6);
        // Soul lanterns in each alcove
        b.setBlock(-6, -7, 0, Material.SOUL_LANTERN);
        b.setBlock( 6, -7, 0, Material.SOUL_LANTERN);
        b.setBlock(0, -7, -6, Material.SOUL_LANTERN);
        b.setBlock(0, -7,  6, Material.SOUL_LANTERN);
        // Access shaft from surface
        b.fillBox(0, -3, 6, 0, 0, 6, Material.AIR);
        for (int y = -3; y <= -1; y++) b.setBlock(0, y, 5, Material.LADDER);
        // Sarcophagus side bookshelves (study of the entombed)
        b.bookshelfWall(-5, -9, -4, -3, -7);
        b.bookshelfWall( 3, -9, -4,  5, -7);

        // ─── 7. Heavy weathering pass — mossy, cracked, vines, decay ─
        b.scatter(-17, 0, -17, 17, 12, 17, STONE, MOSSY, 0.30);
        b.scatter(-17, 0, -17, 17, 12, 17, STONE, CRACKED, 0.20);
        b.decay(-17, 0, -17, 17, 12, 17, 0.08);
        b.addVines(-17, 0, -17, 17, 12, 17, 0.25);

        // ─── 8. Narrative props ────────────────────────────────────
        b.setBlock(-14, 1, 16, EMERALD);   // offering
        b.setBlock( 14, 1, 16, EMERALD);
        b.setBlock(-10, 1, 15, Material.SKELETON_SKULL);
        b.setBlock( 10, 1, 15, Material.SKELETON_SKULL);
        // Perimeter torches
        for (int t = -16; t <= 16; t += 4) {
            b.setBlock(t, 1, -17, TORCH);
            b.setBlock(t, 1,  17, TORCH);
        }

        b.setBossSpawn(0, topY + 2, 0);
    }

    /** Builds a small carved serpent head guardian. */
    private void serpentHead(StructureBuilder b, int x, int y, int z) {
        b.fillBox(x - 1, y, z - 1, x + 1, y + 1, z + 1, CHISEL);
        b.setBlock(x, y + 2, z, Material.CHISELED_STONE_BRICKS);
        b.setBlock(x - 1, y + 1, z - 1, Material.EMERALD_BLOCK); // glowing eye
        b.setBlock(x + 1, y + 1, z - 1, Material.EMERALD_BLOCK);
        b.setBlock(x, y + 1, z + 2, SOUL); // mouth flame
    }
}

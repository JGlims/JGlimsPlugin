package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

/**
 * OgrinHutBuilder — oversized swamp ogre hut on stilts with cauldron-cookpot, oversized bed,
 * dock with fishing gear, onion + pumpkin garden, bone pile trophy, hanging herb drying rack,
 * stump chair, exterior trash heap, lantern lighting, mushroom garden, tool shed.
 */
public final class OgrinHutBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // ── Swampy ground ───────────────────────────────────────────────
        b.filledCircle(0, 0, 0, 14, Material.MUD);
        b.scatter(-14, 0, -14, 14, 0, 14, Material.MUD, Material.DIRT, 0.20);
        b.scatter(-14, 0, -14, 14, 0, 14, Material.MUD, Material.PODZOL, 0.10);
        b.scatter(-14, 0, -14, 14, 0, 14, Material.MUD, Material.COARSE_DIRT, 0.08);
        // Shallow water patches
        for (int[] w : new int[][]{{-12, -5}, {12, 4}, {-7, 10}, {10, -10}}) {
            b.setBlock(w[0], 0, w[1], Material.WATER);
        }

        // ── Oversized hut platform (raised on stilts) ───────────────────
        b.fillBox(-6, 1, -5, 6, 1, 5, Material.OAK_PLANKS);
        b.scatter(-6, 1, -5, 6, 1, 5, Material.OAK_PLANKS, Material.MUD, 0.2);
        // Stilt posts
        for (int[] s : new int[][]{{-6, -5}, {6, -5}, {-6, 5}, {6, 5}, {0, -5}, {0, 5}}) {
            b.pillar(s[0], 0, 1, s[1], Material.SPRUCE_LOG);
        }

        // ── Walls (spruce logs + mossy patches) ─────────────────────────
        b.fillWalls(-6, 2, -5, 6, 7, 5, Material.SPRUCE_LOG);
        b.hollowBox(-6, 2, -5, 6, 7, 5);
        b.scatter(-6, 2, -5, 6, 7, 5, Material.SPRUCE_LOG, Material.MOSSY_COBBLESTONE, 0.15);
        b.scatter(-6, 2, -5, 6, 7, 5, Material.SPRUCE_LOG, Material.STRIPPED_SPRUCE_LOG, 0.10);

        // ── Oversized door (3 wide, 4 tall, south) ──────────────────────
        b.fillBox(-1, 2, -5, 1, 5, -5, Material.AIR);
        // Door lintel
        b.setBlock(-1, 6, -5, Material.STRIPPED_SPRUCE_LOG);
        b.setBlock(0, 6, -5, Material.STRIPPED_SPRUCE_LOG);
        b.setBlock(1, 6, -5, Material.STRIPPED_SPRUCE_LOG);

        // ── Messy conical roof ──────────────────────────────────────────
        b.pyramidRoof(-7, -6, 7, 6, 8, Material.SPRUCE_PLANKS);
        b.scatter(-7, 8, -6, 7, 12, 6, Material.SPRUCE_PLANKS, Material.MOSS_BLOCK, 0.20);
        b.scatter(-7, 8, -6, 7, 12, 6, Material.SPRUCE_PLANKS, Material.MUD, 0.05);
        // Chimney
        b.pillar(-5, 7, 5, -4, Material.COBBLESTONE);
        b.setBlock(-5, 12, -4, Material.CAMPFIRE);

        // ── Windows (large, with shutters) ──────────────────────────────
        b.setBlock(-6, 4, 0, Material.GLASS_PANE);
        b.setBlock(-6, 5, 0, Material.GLASS_PANE);
        b.setBlock(6, 4, 0, Material.GLASS_PANE);
        b.setBlock(6, 5, 0, Material.GLASS_PANE);
        b.setBlock(0, 4, 5, Material.GLASS_PANE);
        b.setBlock(0, 5, 5, Material.GLASS_PANE);
        // Shutters (trapdoors)
        b.setBlock(-6, 4, -1, Material.SPRUCE_TRAPDOOR);
        b.setBlock(-6, 4, 1, Material.SPRUCE_TRAPDOOR);
        b.setBlock(6, 4, -1, Material.SPRUCE_TRAPDOOR);
        b.setBlock(6, 4, 1, Material.SPRUCE_TRAPDOOR);

        // ── Interior: cauldron cookpot ──────────────────────────────────
        b.setBlock(0, 2, 2, Material.CAULDRON);
        b.setBlock(0, 1, 2, Material.CAMPFIRE);
        b.setBlock(1, 2, 2, Material.BARREL);  // ingredients
        b.setBlock(-1, 2, 2, Material.BARREL);
        // Hanging cauldron chain
        b.setBlock(0, 6, 2, Material.IRON_CHAIN);
        b.setBlock(0, 5, 2, Material.IRON_CHAIN);

        // ── Oversized bed (3×3 wool on planks) ──────────────────────────
        b.fillBox(-5, 2, 2, -2, 2, 4, Material.OAK_PLANKS);
        b.fillBox(-5, 3, 2, -2, 3, 4, Material.GREEN_WOOL);
        b.setBlock(-5, 3, 2, Material.BROWN_WOOL);  // pillow
        b.setBlock(-2, 3, 2, Material.BROWN_WOOL);

        // ── Oversized dining table ──────────────────────────────────────
        b.setBlock(3, 2, 0, Material.OAK_FENCE);
        b.setBlock(3, 3, 0, Material.OAK_PRESSURE_PLATE);
        b.setBlock(4, 2, 0, Material.OAK_FENCE);
        b.setBlock(4, 3, 0, Material.OAK_PRESSURE_PLATE);
        b.setBlock(5, 2, 0, Material.OAK_FENCE);
        b.setBlock(5, 3, 0, Material.OAK_PRESSURE_PLATE);
        // Stump chair
        b.setBlock(3, 2, 1, Material.OAK_LOG);
        // Food (barrel + bone)
        b.setBlock(3, 2, -3, Material.BARREL);
        b.setBlock(4, 2, -3, Material.BONE_BLOCK);
        b.setBlock(5, 2, -3, Material.BARREL);

        // ── Hanging herb drying rack ────────────────────────────────────
        for (int x = -4; x <= 4; x += 2) {
            b.setBlock(x, 6, 0, Material.IRON_CHAIN);
            b.setBlock(x, 5, 0, Material.BROWN_MUSHROOM);
        }

        // ── Bone pile (Ogrin's meals — NE corner) ───────────────────────
        b.fillBox(4, 2, 3, 5, 3, 4, Material.BONE_BLOCK);
        b.setBlock(4, 4, 3, Material.BONE_BLOCK);
        b.setBlock(5, 2, 4, Material.SKELETON_SKULL);

        // ── Onion + pumpkin garden (E exterior) ─────────────────────────
        b.fillBox(8, 1, -5, 12, 1, -1, Material.FARMLAND);
        b.setBlock(9, 2, -4, Material.POTATOES);
        b.setBlock(10, 2, -4, Material.POTATOES);
        b.setBlock(11, 2, -4, Material.POTATOES);
        b.setBlock(9, 2, -2, Material.CARROTS);
        b.setBlock(10, 2, -2, Material.CARROTS);
        b.setBlock(11, 2, -2, Material.CARVED_PUMPKIN);
        // Pumpkin patch
        b.setBlock(12, 2, -5, Material.PUMPKIN);
        b.setBlock(11, 2, -5, Material.PUMPKIN);
        // Scarecrow
        b.pillar(8, 1, 3, -6, Material.OAK_FENCE);
        b.setBlock(8, 4, -6, Material.HAY_BLOCK);
        b.setBlock(8, 5, -6, Material.CARVED_PUMPKIN);

        // ── Fishing dock + dock pilings ─────────────────────────────────
        b.fillBox(-10, 1, 6, -5, 1, 10, Material.OAK_PLANKS);
        b.pillar(-10, -2, 0, 6, Material.OAK_LOG);
        b.pillar(-5, -2, 0, 6, Material.OAK_LOG);
        b.pillar(-10, -2, 0, 10, Material.OAK_LOG);
        b.pillar(-5, -2, 0, 10, Material.OAK_LOG);
        // Fishing rod holders
        b.setBlock(-7, 2, 10, Material.OAK_FENCE);
        b.setBlock(-7, 3, 10, Material.OAK_FENCE);
        b.setBlock(-8, 2, 10, Material.BARREL);
        b.setBlock(-9, 2, 10, Material.OAK_FENCE);
        b.setBlock(-9, 3, 10, Material.OAK_FENCE);
        // Nets (cobwebs)
        b.setBlock(-6, 3, 10, Material.COBWEB);
        b.setBlock(-7, 3, 11, Material.COBWEB);
        // Caught fish (drying)
        b.setBlock(-8, 2, 6, Material.DRIED_KELP_BLOCK);

        // ── Tool shed (W exterior) ──────────────────────────────────────
        b.fillBox(-11, 1, -3, -9, 1, -1, Material.OAK_PLANKS);
        b.fillWalls(-11, 2, -3, -9, 4, -1, Material.OAK_PLANKS);
        b.hollowBox(-11, 2, -3, -9, 4, -1);
        b.setBlock(-10, 2, -1, Material.AIR);  // door
        b.setBlock(-10, 3, -1, Material.AIR);
        b.setBlock(-11, 2, -2, Material.CRAFTING_TABLE);
        b.setBlock(-9, 2, -2, Material.FURNACE);
        b.setBlock(-10, 2, -3, Material.GRINDSTONE);
        b.pyramidRoof(-11, -3, -9, -1, 4, Material.SPRUCE_STAIRS);
        b.setBlock(-10, 4, -2, Material.LANTERN);

        // ── Lanterns (warm mood) ────────────────────────────────────────
        b.setBlock(0, 6, 0, Material.LANTERN);
        b.setBlock(-5, 2, -4, Material.LANTERN);
        b.setBlock(5, 2, -4, Material.LANTERN);
        b.setBlock(0, 2, 4, Material.LANTERN);
        // Hanging lantern chains
        b.setBlock(3, 6, 3, Material.IRON_CHAIN);
        b.setBlock(3, 5, 3, Material.LANTERN);
        b.setBlock(-3, 6, -3, Material.IRON_CHAIN);
        b.setBlock(-3, 5, -3, Material.LANTERN);

        // ── Mushrooms around the hut ────────────────────────────────────
        for (int[] mu : new int[][]{{7, 3}, {-8, -3}, {8, 5}, {-9, 7}, {10, -7}, {-10, 4}}) {
            b.setBlockIfAir(mu[0], 1, mu[1], mu[0] % 2 == 0 ? Material.RED_MUSHROOM : Material.BROWN_MUSHROOM);
        }
        // Larger shroom patches
        b.setBlock(9, 1, 8, Material.RED_MUSHROOM_BLOCK);
        b.setBlock(-10, 1, -7, Material.BROWN_MUSHROOM_BLOCK);

        // ── Exterior trash heap (bones + barrels) ───────────────────────
        b.fillBox(5, 1, 7, 7, 2, 9, Material.BONE_BLOCK);
        b.setBlock(5, 3, 8, Material.SKELETON_SKULL);
        b.setBlock(6, 1, 7, Material.BARREL);
        b.setBlock(7, 1, 9, Material.BARREL);

        // ── Loot ────────────────────────────────────────────────────────
        b.placeChest(4, 2, -2);     // near table
        b.placeChest(-4, 2, -4);    // by bed
        b.placeChest(-8, 2, 7);     // fishing dock
        b.placeChest(-10, 2, -2);   // tool shed
        b.placeChest(6, 1, 8);      // trash-heap stash
        b.setBossSpawn(0, 2, -7);
    }
}

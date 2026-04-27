package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

/**
 * AbandonedHouseBuilder — derelict two-story cottage with collapsed roof, cobwebs, broken
 * windows, rotting furniture, hidden basement laboratory, exterior garden gone to weeds,
 * chimney, attic crawlspace, front porch, vine-covered facade, scattered debris.
 */
public final class AbandonedHouseBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // ── Foundation (cracked cobblestone) ────────────────────────────
        b.fillBox(-6, 0, -6, 6, 0, 6, Material.COBBLESTONE);
        b.scatter(-6, 0, -6, 6, 0, 6, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE, 0.4);
        b.scatter(-6, 0, -6, 6, 0, 6, Material.COBBLESTONE, Material.CRACKED_STONE_BRICKS, 0.15);

        // ── Foundation ring apron ───────────────────────────────────────
        for (int[] c : new int[][]{{-7, -7}, {7, -7}, {-7, 7}, {7, 7}}) {
            b.setBlock(c[0], 0, c[1], Material.COBBLESTONE);
            b.setBlock(c[0], 1, c[1], Material.MOSSY_COBBLESTONE);
        }

        // ── First floor walls ───────────────────────────────────────────
        b.fillWalls(-6, 1, -6, 6, 5, 6, Material.OAK_PLANKS);
        b.hollowBox(-6, 1, -6, 6, 5, 6);
        // Mossy + rotting patches
        b.scatter(-6, 1, -6, 6, 5, 6, Material.OAK_PLANKS, Material.MOSSY_COBBLESTONE, 0.15);
        b.scatter(-6, 1, -6, 6, 5, 6, Material.OAK_PLANKS, Material.SPRUCE_PLANKS, 0.12);
        b.scatter(-6, 1, -6, 6, 5, 6, Material.OAK_PLANKS, Material.STRIPPED_OAK_LOG, 0.05);
        // Corner posts
        for (int[] c : new int[][]{{-6, -6}, {6, -6}, {-6, 6}, {6, 6}}) {
            b.pillar(c[0], 1, 9, c[1], Material.OAK_LOG);
        }

        // ── Front porch (south) ─────────────────────────────────────────
        b.fillBox(-3, 0, -9, 3, 0, -7, Material.COBBLESTONE);
        b.setBlock(-3, 1, -9, Material.OAK_FENCE);
        b.setBlock(3, 1, -9, Material.OAK_FENCE);
        b.setBlock(-3, 2, -9, Material.OAK_FENCE);
        b.setBlock(3, 2, -9, Material.OAK_FENCE);
        b.fillBox(-3, 3, -9, 3, 3, -7, Material.OAK_PLANKS);  // porch roof
        // Porch steps
        b.setStairs(-2, 0, -10, Material.OAK_STAIRS, BlockFace.NORTH, false);
        b.setStairs(-1, 0, -10, Material.OAK_STAIRS, BlockFace.NORTH, false);
        b.setStairs(0, 0, -10, Material.OAK_STAIRS, BlockFace.NORTH, false);
        b.setStairs(1, 0, -10, Material.OAK_STAIRS, BlockFace.NORTH, false);
        b.setStairs(2, 0, -10, Material.OAK_STAIRS, BlockFace.NORTH, false);

        // ── Second floor ────────────────────────────────────────────────
        b.fillBox(-5, 5, -5, 5, 5, 5, Material.OAK_PLANKS);
        b.fillWalls(-5, 6, -5, 5, 9, 5, Material.OAK_PLANKS);
        b.hollowBox(-5, 6, -5, 5, 9, 5);
        b.scatter(-5, 6, -5, 5, 9, 5, Material.OAK_PLANKS, Material.SPRUCE_PLANKS, 0.12);

        // ── Gabled roof ─────────────────────────────────────────────────
        b.gabledRoof(-6, -6, 6, 6, 10, Material.SPRUCE_STAIRS, Material.SPRUCE_SLAB);

        // ── Collapsed roof section (NE corner) ──────────────────────────
        b.decay(2, 10, -6, 6, 13, 0, 0.7);
        b.decay(3, 6, -5, 5, 9, -1, 0.4);
        // Exposed ribs
        b.setBlock(4, 11, -5, Material.STRIPPED_OAK_LOG);
        b.setBlock(5, 12, -4, Material.STRIPPED_OAK_LOG);

        // ── Brick chimney ───────────────────────────────────────────────
        b.pillar(-5, 6, 15, -5, Material.BRICK);
        b.setBlock(-5, 16, -5, Material.CAMPFIRE);

        // ── Broken windows (mixed glass/air) ────────────────────────────
        b.setBlock(-6, 3, 0, Material.AIR);
        b.setBlock(-6, 4, 0, Material.AIR);
        b.setBlock(6, 3, -3, Material.AIR);
        b.setBlock(6, 4, -3, Material.GLASS_PANE);
        b.setBlock(0, 3, -6, Material.GLASS_PANE);
        b.setBlock(0, 4, -6, Material.AIR);
        b.setBlock(0, 7, 6, Material.AIR);
        b.setBlock(0, 8, 6, Material.GLASS_PANE);
        b.setBlock(-5, 7, 0, Material.AIR);
        b.setBlock(-5, 8, 0, Material.GLASS_PANE);
        b.setBlock(5, 7, 0, Material.AIR);
        b.setBlock(5, 8, -3, Material.GLASS_PANE);

        // ── Door (hanging off hinge — open) ─────────────────────────────
        b.setBlock(0, 1, -6, Material.AIR);
        b.setBlock(0, 2, -6, Material.AIR);
        b.setBlock(-1, 1, -7, Material.OAK_TRAPDOOR);  // fallen door piece

        // ── Interior first floor ────────────────────────────────────────
        b.fillFloor(-5, -5, 5, 5, 1, Material.OAK_PLANKS);
        b.scatter(-5, 1, -5, 5, 1, 5, Material.OAK_PLANKS, Material.STRIPPED_OAK_LOG, 0.1);
        // Rotting furniture
        b.setBlock(-4, 1, -4, Material.CRAFTING_TABLE);
        b.setBlock(-3, 1, -4, Material.OAK_FENCE);
        b.setBlock(-3, 2, -4, Material.OAK_PRESSURE_PLATE);
        b.chair(-2, 1, -4, Material.OAK_STAIRS, BlockFace.NORTH);
        b.chair(-1, 1, -4, Material.OAK_STAIRS, BlockFace.NORTH);
        // Fireplace + hearth
        b.setBlock(-5, 1, 4, Material.CAMPFIRE);
        b.setBlock(-5, 2, 4, Material.COBBLESTONE);
        b.setBlock(-5, 3, 4, Material.COBBLESTONE);
        // Kitchen
        b.setBlock(4, 1, -4, Material.FURNACE);
        b.setBlock(4, 1, -3, Material.SMOKER);
        b.setBlock(3, 1, 4, Material.CAULDRON);
        b.setBlock(4, 1, 4, Material.COMPOSTER);
        b.setBlock(4, 1, 3, Material.BARREL);
        // Dining table
        b.setBlock(0, 1, 0, Material.OAK_FENCE);
        b.setBlock(0, 2, 0, Material.OAK_PRESSURE_PLATE);
        // Flower pot (dead)
        b.setBlock(-4, 1, 4, Material.POTTED_DEAD_BUSH);
        b.setBlock(4, 1, -5, Material.POTTED_DEAD_BUSH);

        // ── Cobwebs (dense in corners) ──────────────────────────────────
        b.scatter(-5, 1, -5, 5, 5, 5, Material.AIR, Material.COBWEB, 0.08);
        b.scatter(-4, 6, -4, 4, 9, 4, Material.AIR, Material.COBWEB, 0.08);
        b.setBlock(-5, 4, -5, Material.COBWEB);
        b.setBlock(5, 4, 5, Material.COBWEB);
        b.setBlock(-5, 4, 5, Material.COBWEB);
        b.setBlock(5, 4, -5, Material.COBWEB);

        // ── Ladder to second floor ──────────────────────────────────────
        for (int y = 1; y <= 5; y++) b.setBlock(4, y, 4, Material.LADDER);

        // ── Second floor: broken bed + study ────────────────────────────
        b.setBlock(-3, 6, 3, Material.RED_BED);
        b.setBlock(-3, 6, 4, Material.BARREL);  // nightstand
        b.setBlock(-3, 7, 4, Material.LANTERN);
        b.setBlock(-2, 6, 2, Material.BOOKSHELF);
        b.setBlock(-2, 7, 2, Material.BOOKSHELF);
        b.setBlock(-2, 6, 1, Material.LECTERN);
        b.setBlock(3, 6, -3, Material.ENCHANTING_TABLE);
        b.setBlock(3, 7, -3, Material.LANTERN);
        // Dresser
        b.setBlock(-4, 6, -3, Material.BARREL);
        b.setBlock(-4, 6, -2, Material.BARREL);
        b.setBlock(-4, 7, -3, Material.FLOWER_POT);

        // ── Attic crawlspace (under roof peak) ──────────────────────────
        b.fillBox(-2, 10, -2, 2, 10, 2, Material.OAK_PLANKS);
        b.setBlock(0, 10, 0, Material.AIR);  // hatch
        for (int y = 9; y <= 10; y++) b.setBlock(-1, y, 0, Material.LADDER);
        // Attic contents
        b.setBlock(-2, 11, -2, Material.BARREL);
        b.setBlock(2, 11, 2, Material.BARREL);
        b.setBlock(0, 11, -2, Material.COBWEB);
        b.setBlock(0, 11, 2, Material.COBWEB);

        // ── Hidden basement laboratory ──────────────────────────────────
        b.setBlock(0, 0, 0, Material.OAK_TRAPDOOR);
        b.fillBox(-4, -5, -4, 4, -1, 4, Material.COBBLESTONE);
        b.fillBox(-3, -4, -3, 3, -1, 3, Material.AIR);
        b.fillFloor(-3, -3, 3, 3, -5, Material.MOSSY_COBBLESTONE);
        // Lab equipment
        b.setBlock(-3, -4, -3, Material.BREWING_STAND);
        b.setBlock(-3, -4, -2, Material.CAULDRON);
        b.setBlock(3, -4, 3, Material.ENCHANTING_TABLE);
        b.setBlock(3, -4, 2, Material.BOOKSHELF);
        b.setBlock(3, -4, 1, Material.BOOKSHELF);
        b.setBlock(-3, -4, 3, Material.SMITHING_TABLE);
        b.setBlock(-2, -4, -3, Material.SOUL_LANTERN);
        b.setBlock(2, -4, -3, Material.SOUL_LANTERN);
        // Ritual circle on basement floor
        for (int[] r : new int[][]{{-1, -1}, {1, -1}, {-1, 1}, {1, 1}, {0, -1}, {-1, 0}, {1, 0}, {0, 1}}) {
            b.setBlock(r[0], -4, r[1], Material.SOUL_SOIL);
        }
        b.setBlock(0, -4, 0, Material.LODESTONE);
        b.setBlock(0, -3, 0, Material.SOUL_LANTERN);

        // ── Ladder down to basement ─────────────────────────────────────
        for (int y = -4; y <= -1; y++) b.setBlock(1, y, 0, Material.LADDER);

        // ── Exterior vines + moss ───────────────────────────────────────
        b.addVines(-7, 1, -7, 7, 10, 7, 0.25);
        b.addVines(-7, 6, -7, 7, 13, 7, 0.15);

        // ── Overgrown garden (exterior west) ────────────────────────────
        b.fillBox(-10, 0, -3, -7, 0, 3, Material.COARSE_DIRT);
        for (int x = -10; x <= -7; x++) {
            for (int z = -2; z <= 2; z += 2) {
                b.setBlockIfAir(x, 1, z, Material.TALL_GRASS);
            }
        }
        b.setBlock(-9, 1, 0, Material.DEAD_BUSH);
        b.setBlock(-8, 1, -2, Material.SWEET_BERRY_BUSH);
        b.setBlock(-8, 1, 2, Material.DEAD_BUSH);
        // Rotting scarecrow
        b.setBlock(-9, 1, 2, Material.OAK_FENCE);
        b.setBlock(-9, 2, 2, Material.OAK_FENCE);
        b.setBlock(-9, 3, 2, Material.HAY_BLOCK);
        b.setBlock(-9, 4, 2, Material.CARVED_PUMPKIN);

        // ── Scattered debris around house ───────────────────────────────
        for (int[] dbr : new int[][]{{8, 3}, {-7, -5}, {6, -8}, {-8, 5}, {9, -3}}) {
            b.setBlock(dbr[0], 1, dbr[1], Material.STRIPPED_OAK_LOG);
        }
        // Rusty water barrel
        b.setBlock(8, 1, 6, Material.BARREL);
        b.setBlock(-8, 1, -7, Material.BARREL);

        // ── Loot ────────────────────────────────────────────────────────
        b.placeChest(4, 1, -2);     // main floor hidden
        b.placeChest(0, -4, 0);     // basement ritual
        b.placeChest(-2, 11, 0);    // attic crawlspace
        b.placeChest(-3, 6, 3);     // bedside
        b.placeChest(-8, 1, -7);    // exterior barrel stash
        b.setBossSpawn(0, 1, 2);
    }
}

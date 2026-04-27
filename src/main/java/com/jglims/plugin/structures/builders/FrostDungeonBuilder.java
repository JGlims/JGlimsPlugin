package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

/**
 * FrostDungeonBuilder — ice fortress with packed-ice walls, prismarine accents,
 * ribbed ceiling, throne alcove, prison cells, glowstone chandeliers, and powder-snow traps.
 */
public final class FrostDungeonBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // ── Main hall envelope ─────────────────────────────────────────
        b.fillWalls(-15, 0, -15, 15, 8, 15, Material.PACKED_ICE);
        b.hollowBox(-15, 1, -15, 15, 8, 15);
        b.fillFloor(-14, -14, 14, 14, 0, Material.BLUE_ICE);
        // Interior wall accents (darker prismarine base course)
        b.fillWalls(-15, 0, -15, 15, 1, 15, Material.DARK_PRISMARINE);
        // Crown molding (prismarine bricks at top)
        b.fillWalls(-15, 8, -15, 15, 8, 15, Material.PRISMARINE_BRICKS);
        // Corner reinforcements
        for (int[] c : new int[][]{{-15, -15}, {15, -15}, {-15, 15}, {15, 15}}) {
            b.pillar(c[0], 0, 8, c[1], Material.PRISMARINE_BRICKS);
        }

        // ── Ribbed ceiling (alternating prismarine and ice strips) ─────
        for (int x = -14; x <= 14; x += 3) {
            b.fillBox(x, 8, -14, x, 8, 14, Material.PRISMARINE);
        }

        // ── Floor pattern: diamond inlay ───────────────────────────────
        for (int x = -10; x <= 10; x += 5) {
            for (int z = -10; z <= 10; z += 5) {
                b.setBlock(x, 0, z, Material.DIAMOND_BLOCK);
                b.setBlock(x + 1, 0, z, Material.LIGHT_BLUE_GLAZED_TERRACOTTA);
                b.setBlock(x - 1, 0, z, Material.LIGHT_BLUE_GLAZED_TERRACOTTA);
                b.setBlock(x, 0, z + 1, Material.LIGHT_BLUE_GLAZED_TERRACOTTA);
                b.setBlock(x, 0, z - 1, Material.LIGHT_BLUE_GLAZED_TERRACOTTA);
            }
        }

        // ── Ice pillars with carved-in sea-lanterns ────────────────────
        for (int a = 0; a < 360; a += 45) {
            int px = (int) (10 * Math.cos(Math.toRadians(a)));
            int pz = (int) (10 * Math.sin(Math.toRadians(a)));
            b.pillar(px, 1, 7, pz, Material.PACKED_ICE);
            b.setBlock(px, 4, pz, Material.SEA_LANTERN);
            b.setBlock(px, 8, pz, Material.SEA_LANTERN);
            // Stair-cap base so pillars look anchored
            for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST}) {
                int dx = face.getModX(), dz = face.getModZ();
                b.setStairs(px + dx, 1, pz + dz, Material.PRISMARINE_BRICK_STAIRS, face.getOppositeFace(), false);
            }
        }

        // ── Frozen throne room (north alcove) ──────────────────────────
        b.fillBox(-5, 1, 8, 5, 5, 14, Material.AIR);
        b.fillBox(-5, 0, 8, 5, 0, 14, Material.BLUE_ICE);
        b.fillWalls(-6, 0, 8, 6, 5, 14, Material.PRISMARINE_BRICKS);
        b.hollowBox(-6, 1, 8, 6, 5, 14);
        // Arch into throne room
        b.setBlock(-3, 4, 8, Material.PRISMARINE_WALL);
        b.setBlock(3, 4, 8, Material.PRISMARINE_WALL);
        b.setBlock(-2, 5, 8, Material.PRISMARINE_BRICK_SLAB);
        b.setBlock(2, 5, 8, Material.PRISMARINE_BRICK_SLAB);

        // Throne dais
        b.fillBox(-3, 1, 13, 3, 1, 14, Material.QUARTZ_BLOCK);
        b.fillBox(-2, 2, 13, 2, 2, 14, Material.CHISELED_QUARTZ_BLOCK);
        // Throne seat
        b.setStairs(-1, 2, 13, Material.QUARTZ_STAIRS, BlockFace.NORTH, false);
        b.setStairs(1, 2, 13, Material.QUARTZ_STAIRS, BlockFace.NORTH, false);
        b.setBlock(0, 2, 13, Material.QUARTZ_PILLAR);
        // Throne back
        b.fillBox(-2, 3, 14, 2, 5, 14, Material.QUARTZ_PILLAR);
        b.setBlock(0, 6, 14, Material.SEA_LANTERN);
        b.setBlock(-3, 1, 13, Material.BLUE_ICE);
        b.setBlock(3, 1, 13, Material.BLUE_ICE);

        // Throne-room frozen torches
        for (int[] t : new int[][]{{-4, 9}, {4, 9}, {-4, 12}, {4, 12}}) {
            b.pillar(t[0], 1, 3, t[1], Material.PRISMARINE_BRICKS);
            b.setBlock(t[0], 4, t[1], Material.SOUL_LANTERN);
        }

        // ── Frost crystal formations scattered around the hall ─────────
        for (int[] crystal : new int[][]{{-8, 5}, {8, -7}, {-6, -10}, {10, 3}, {-12, 0}, {12, -12}, {0, -13}}) {
            b.pillar(crystal[0], 1, 3, crystal[1], Material.ICE);
            b.setBlock(crystal[0], 4, crystal[1], Material.BLUE_ICE);
            b.setBlock(crystal[0], 5, crystal[1], Material.LIGHT_BLUE_STAINED_GLASS);
        }

        // ── Glowstone chandeliers ──────────────────────────────────────
        for (int[] ch : new int[][]{{-6, -6}, {6, -6}, {-6, 6}, {6, 6}, {0, 0}}) {
            b.setBlock(ch[0], 7, ch[1], Material.IRON_CHAIN);
            b.setBlock(ch[0], 6, ch[1], Material.GLOWSTONE);
            for (BlockFace f : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST}) {
                b.setBlock(ch[0] + f.getModX(), 6, ch[1] + f.getModZ(), Material.SEA_LANTERN);
            }
        }

        // ── Side corridors to prison cells (east/west) ─────────────────
        b.fillBox(-20, 1, -2, -15, 4, 2, Material.AIR);
        b.fillWalls(-20, 0, -5, -15, 5, 5, Material.PACKED_ICE);
        b.fillFloor(-20, -5, -15, 5, 0, Material.BLUE_ICE);
        b.hollowBox(-20, 1, -5, -15, 5, 5);
        b.fillBox(15, 1, -2, 20, 4, 2, Material.AIR);
        b.fillWalls(15, 0, -5, 20, 5, 5, Material.PACKED_ICE);
        b.fillFloor(15, -5, 20, 5, 0, Material.BLUE_ICE);
        b.hollowBox(15, 1, -5, 20, 5, 5);

        // Prison cells with iron bars + frozen prisoners (armor stands via blocks)
        for (int x : new int[]{-18, 18}) {
            // Front bars
            b.setBlock(x, 1, -4, Material.IRON_BARS);
            b.setBlock(x, 2, -4, Material.IRON_BARS);
            b.setBlock(x, 3, -4, Material.IRON_BARS);
            b.setBlock(x, 1, 4, Material.IRON_BARS);
            b.setBlock(x, 2, 4, Material.IRON_BARS);
            b.setBlock(x, 3, 4, Material.IRON_BARS);
            // Frozen victim (ice pillar with mob head stand-in)
            b.pillar(x, 1, 2, -3, Material.ICE);
            b.setBlock(x, 3, -3, Material.SKELETON_SKULL);
            b.pillar(x, 1, 2, 3, Material.ICE);
            b.setBlock(x, 3, 3, Material.SKELETON_SKULL);
            // Chains on the wall
            b.setBlock(x, 2, -2, Material.IRON_CHAIN);
            b.setBlock(x, 2, 2, Material.IRON_CHAIN);
            // Bone litter
            b.setBlock(x, 1, 0, Material.BONE_BLOCK);
        }

        // ── Powder-snow traps scattered on the main floor ──────────────
        b.scatter(-14, 0, -14, 14, 0, 14, Material.BLUE_ICE, Material.POWDER_SNOW, 0.10);
        b.scatter(-14, 0, -14, 14, 0, 14, Material.BLUE_ICE, Material.SNOW_BLOCK, 0.05);
        b.scatter(-14, 7, -14, 14, 8, 14, Material.PACKED_ICE, Material.ICE, 0.12);

        // ── Icicle stalactites from the ceiling ────────────────────────
        for (int[] ic : new int[][]{{-8, -8}, {-4, 4}, {4, -4}, {8, 8}, {-12, 2}, {12, -2}}) {
            b.setBlock(ic[0], 7, ic[1], Material.ICE);
            b.setBlock(ic[0], 6, ic[1], Material.PACKED_ICE);
        }

        // Entrance portcullis
        b.fillBox(-2, 1, -15, 2, 4, -15, Material.AIR);
        b.fillBox(-2, 5, -15, 2, 5, -15, Material.PRISMARINE_BRICK_STAIRS);
        for (int x = -2; x <= 2; x++) b.setBlock(x, 1, -15, Material.IRON_BARS);

        // Decay / weathering
        b.scatter(-15, 0, -15, 15, 8, 15, Material.PACKED_ICE, Material.CRACKED_STONE_BRICKS, 0.03);
        b.decay(-15, 0, -15, 15, 8, 15, 0.04);

        // Loot
        b.placeChest(0, 1, 10);     // throne chest
        b.placeChest(-18, 1, 0);    // west cell block
        b.placeChest(18, 1, 0);     // east cell block
        b.placeChest(-14, 1, -14);  // corner stash
        b.placeChest(14, 1, 14);    // corner stash

        b.setBossSpawn(0, 1, 11);
    }
}

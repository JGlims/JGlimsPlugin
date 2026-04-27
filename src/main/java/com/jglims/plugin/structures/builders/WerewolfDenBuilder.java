package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;

/**
 * WerewolfDenBuilder — moonlit dark-forest clearing with stone cave, ritual altar,
 * bone piles, claw-marked dead trees, shrine of howling, sleeping den, wolf tracks,
 * hanging fetishes, waning-moon marker, and scattered pelts.
 */
public final class WerewolfDenBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // ── Moonlit clearing ────────────────────────────────────────────
        b.filledCircle(0, 0, 0, 12, Material.PODZOL);
        b.scatter(-12, 0, -12, 12, 0, 12, Material.PODZOL, Material.COARSE_DIRT, 0.25);
        b.scatter(-12, 0, -12, 12, 0, 12, Material.PODZOL, Material.MOSS_BLOCK, 0.10);
        b.scatter(-12, 0, -12, 12, 0, 12, Material.PODZOL, Material.ROOTED_DIRT, 0.05);

        // ── Main cave (into a hill, south) ──────────────────────────────
        b.fillBox(-4, 0, 4, 4, 0, 14, Material.STONE);
        b.fillBox(-5, 1, 4, 5, 6, 14, Material.STONE);
        b.fillBox(-6, 2, 4, 6, 7, 14, Material.STONE);
        b.fillBox(-4, 1, 4, 4, 5, 14, Material.AIR);  // interior
        b.fillBox(-2, 1, 3, 2, 3, 4, Material.AIR);    // entrance opening
        // Cave floor weathering
        b.scatter(-4, 0, 4, 4, 0, 14, Material.STONE, Material.GRAVEL, 0.2);
        b.scatter(-4, 0, 4, 4, 0, 14, Material.STONE, Material.COBBLESTONE, 0.15);
        // Stalactites + stalagmites
        for (int[] sl : new int[][]{{-3, 7}, {3, 9}, {-2, 11}, {2, 6}, {0, 13}}) {
            b.setBlock(sl[0], 4, sl[1], Material.POINTED_DRIPSTONE);
            b.setBlock(sl[0], 1, sl[1], Material.POINTED_DRIPSTONE);
        }

        // ── Cave entrance framing (claw-scratched logs) ─────────────────
        b.setBlock(-3, 1, 3, Material.STRIPPED_OAK_LOG);
        b.setBlock(-3, 2, 3, Material.STRIPPED_OAK_LOG);
        b.setBlock(-3, 3, 3, Material.STRIPPED_OAK_LOG);
        b.setBlock(3, 1, 3, Material.STRIPPED_OAK_LOG);
        b.setBlock(3, 2, 3, Material.STRIPPED_OAK_LOG);
        b.setBlock(3, 3, 3, Material.STRIPPED_OAK_LOG);
        // Lintel
        for (int x = -3; x <= 3; x++) b.setBlock(x, 4, 3, Material.STRIPPED_SPRUCE_LOG);
        // Skull on lintel
        b.setBlock(0, 5, 3, Material.SKELETON_SKULL);
        b.setBlock(-2, 5, 3, Material.WITHER_SKELETON_SKULL);
        b.setBlock(2, 5, 3, Material.WITHER_SKELETON_SKULL);

        // ── Bone-littered cave floor ────────────────────────────────────
        b.scatter(-3, 0, 5, 3, 0, 13, Material.STONE, Material.BONE_BLOCK, 0.25);
        b.setBlock(-1, 1, 8, Material.BONE_BLOCK);
        b.setBlock(1, 1, 6, Material.BONE_BLOCK);
        b.setBlock(0, 1, 10, Material.BONE_BLOCK);
        b.setBlock(2, 1, 9, Material.BONE_BLOCK);
        // Pile of bones at back
        b.fillBox(-2, 1, 12, 2, 2, 13, Material.BONE_BLOCK);
        b.scatter(-2, 1, 12, 2, 2, 13, Material.BONE_BLOCK, Material.AIR, 0.3);
        b.setBlock(0, 3, 12, Material.SKELETON_SKULL);
        b.setBlock(-1, 3, 13, Material.SKELETON_SKULL);

        // ── Claw marks on interior walls ────────────────────────────────
        for (int[] m : new int[][]{{-4, 6}, {-4, 8}, {-4, 10}, {4, 7}, {4, 9}, {4, 11}}) {
            b.setBlock(m[0], 2, m[1], Material.STRIPPED_SPRUCE_LOG);
            b.setBlock(m[0], 3, m[1], Material.STRIPPED_SPRUCE_LOG);
        }

        // ── Wolf pelt carpets (sleeping area) ───────────────────────────
        for (int[] pelt : new int[][]{{-2, 11}, {-1, 11}, {0, 11}, {-1, 10}, {0, 10}, {1, 11}}) {
            Material carpet = (pelt[0] + pelt[1]) % 2 == 0 ? Material.GRAY_CARPET : Material.BROWN_CARPET;
            b.setBlock(pelt[0], 1, pelt[1], carpet);
        }
        b.setBlock(-2, 1, 10, Material.LIGHT_GRAY_CARPET);

        // ── Ritual circle in the clearing ───────────────────────────────
        b.circle(0, 0, 0, 5, Material.SOUL_SOIL);
        b.filledCircle(0, -1, 0, 4, Material.BASALT);
        b.setBlock(0, 0, 0, Material.LODESTONE);
        b.setBlock(0, 1, 0, Material.SOUL_LANTERN);
        for (int a = 0; a < 360; a += 60) {
            int rx = (int) (5 * Math.cos(Math.toRadians(a)));
            int rz = (int) (5 * Math.sin(Math.toRadians(a)));
            b.setBlock(rx, 1, rz, Material.SOUL_CAMPFIRE);
        }
        // Runes on circle perimeter
        for (int a = 0; a < 360; a += 30) {
            int rx = (int) (4 * Math.cos(Math.toRadians(a)));
            int rz = (int) (4 * Math.sin(Math.toRadians(a)));
            b.setBlock(rx, 0, rz, Material.BLACKSTONE);
        }

        // ── Howling shrine (obsidian pillar north of circle) ────────────
        b.pillar(0, 1, 4, -8, Material.OBSIDIAN);
        b.setBlock(0, 5, -8, Material.SKELETON_SKULL);
        b.setBlock(-1, 3, -8, Material.IRON_CHAIN);
        b.setBlock(1, 3, -8, Material.IRON_CHAIN);
        b.setBlock(-1, 1, -8, Material.BLACKSTONE);
        b.setBlock(1, 1, -8, Material.BLACKSTONE);
        b.setBlock(0, 0, -9, Material.SOUL_CAMPFIRE);

        // ── Dead trees around clearing ──────────────────────────────────
        for (int[] t : new int[][]{{-8, 6}, {7, -7}, {-6, 8}, {9, 4}, {-9, -2}, {10, -4}}) {
            b.pillar(t[0], 1, 4 + (t[0] * t[1]) % 3, t[1], Material.SPRUCE_LOG);
        }

        // ── Dense dark-oak tree surround ────────────────────────────────
        int[][] darkTrees = {{-9, 2}, {9, -3}, {-7, -8}, {8, 6}, {-10, -6}, {11, 7}, {-11, 9}, {10, -9}};
        for (int[] dt : darkTrees) {
            b.pillar(dt[0], 1, 10, dt[1], Material.DARK_OAK_LOG);
            for (int y = 7; y <= 11; y++) b.filledCircle(dt[0], y, dt[1], 3, Material.DARK_OAK_LEAVES);
        }
        // Wolfsbane patches (dead-bush + flower)
        for (int[] wb : new int[][]{{-8, 0}, {8, 0}, {0, -10}, {-5, -6}}) {
            b.setBlock(wb[0], 1, wb[1], Material.DEAD_BUSH);
            b.setBlock(wb[0] + 1, 1, wb[1], Material.WITHER_ROSE);
        }

        // ── Hanging fetishes from low branches ──────────────────────────
        for (int[] f : new int[][]{{-6, -6}, {6, 6}, {-6, 6}, {6, -6}}) {
            b.setBlock(f[0], 6, f[1], Material.IRON_CHAIN);
            b.setBlock(f[0], 5, f[1], Material.BONE_BLOCK);
        }

        // ── Eerie lighting ──────────────────────────────────────────────
        b.setBlock(0, 4, 7, Material.SOUL_LANTERN);
        b.setBlock(0, 4, 10, Material.SOUL_LANTERN);
        b.setBlock(0, 4, 13, Material.SOUL_LANTERN);

        // ── Cobwebs in cave corners ─────────────────────────────────────
        b.setBlock(-4, 4, 5, Material.COBWEB);
        b.setBlock(4, 4, 5, Material.COBWEB);
        b.setBlock(-4, 4, 13, Material.COBWEB);
        b.setBlock(4, 4, 13, Material.COBWEB);
        b.setBlock(-2, 3, 10, Material.COBWEB);
        b.setBlock(2, 3, 6, Material.COBWEB);

        // ── Wolf tracks leading into clearing ───────────────────────────
        for (int z = -8; z <= 3; z += 2) {
            b.setBlock(-1, 0, z, Material.DIRT_PATH);
            b.setBlock(1, 0, z + 1, Material.DIRT_PATH);
        }

        // ── Waning moon marker (white-stained glass globe NE) ───────────
        b.pillar(8, 1, 3, 8, Material.DEEPSLATE_BRICKS);
        b.setBlock(8, 4, 8, Material.WHITE_STAINED_GLASS);
        b.setBlock(9, 4, 8, Material.WHITE_STAINED_GLASS);
        b.setBlock(8, 4, 9, Material.WHITE_STAINED_GLASS);

        // ── Loot ────────────────────────────────────────────────────────
        b.placeChest(0, 1, 5);       // near entrance
        b.placeChest(-1, 1, 12);     // deep cave near den
        b.placeChest(0, 0, 0);       // ritual center (subterranean style)
        b.placeChest(0, 1, -9);      // shrine offering
        b.setBossSpawn(0, 1, -3);
    }
}

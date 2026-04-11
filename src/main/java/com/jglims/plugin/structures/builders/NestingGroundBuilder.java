package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;

/**
 * NestingGroundBuilder — bone-walled herd nesting ground with multiple nests, alpha boss pen, and guard towers.
 *
 * <p>Extracted verbatim from the original monolithic builder method.
 */
public final class NestingGroundBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // Ground base
        b.fillBox(-18, 0, -18, 18, 0, 18, Material.COARSE_DIRT);
        b.scatter(-18, 0, -18, 18, 0, 18, Material.COARSE_DIRT, Material.GRASS_BLOCK, 0.3);

        // Protective bone walls (perimeter)
        for (int x = -18; x <= 18; x++) {
            b.setBlock(x, 1, -18, Material.BONE_BLOCK);
            b.setBlock(x, 2, -18, Material.BONE_BLOCK);
            b.setBlock(x, 1, 18, Material.BONE_BLOCK);
            b.setBlock(x, 2, 18, Material.BONE_BLOCK);
        }
        for (int z = -18; z <= 18; z++) {
            b.setBlock(-18, 1, z, Material.BONE_BLOCK);
            b.setBlock(-18, 2, z, Material.BONE_BLOCK);
            b.setBlock(18, 1, z, Material.BONE_BLOCK);
            b.setBlock(18, 2, z, Material.BONE_BLOCK);
        }
        // Fence on top of walls
        for (int x = -18; x <= 18; x++) {
            b.setBlock(x, 3, -18, Material.OAK_FENCE);
            b.setBlock(x, 3, 18, Material.OAK_FENCE);
        }
        for (int z = -18; z <= 18; z++) {
            b.setBlock(-18, 3, z, Material.OAK_FENCE);
            b.setBlock(18, 3, z, Material.OAK_FENCE);
        }

        // Multiple nests (6, scattered)
        int[][] nestPos = {{-10, -10}, {10, -10}, {-10, 10}, {10, 10}, {0, -5}, {0, 8}};
        for (int[] np : nestPos) {
            // Nest ring
            b.filledCircle(np[0], 0, np[1], 3, Material.HAY_BLOCK);
            b.circle(np[0], 1, np[1], 3, Material.DARK_OAK_FENCE);
            // Egg clusters
            b.setBlock(np[0], 1, np[1], Material.TURTLE_EGG);
            b.setBlock(np[0] - 1, 1, np[1], Material.TURTLE_EGG);
            b.setBlock(np[0] + 1, 1, np[1], Material.TURTLE_EGG);
        }

        // Feeding troughs (water-filled cauldrons along paths)
        for (int x = -12; x <= 12; x += 6) {
            b.setBlock(x, 1, 0, Material.CAULDRON);
        }

        // Patrol path (coarse dirt ring inside walls)
        b.circle(0, 0, 0, 15, Material.COARSE_DIRT);

        // Gate entrance (south)
        b.setBlock(0, 1, -18, Material.AIR);
        b.setBlock(0, 2, -18, Material.AIR);
        b.setBlock(1, 1, -18, Material.AIR);
        b.setBlock(1, 2, -18, Material.AIR);

        // Scattered feathers (carpets)
        b.scatter(-16, 0, -16, 16, 0, 16, Material.COARSE_DIRT, Material.WHITE_CARPET, 0.03);

        // ─── Central alpha nest (boss Parasaurolophus spawn — huge hay circle) ───
        b.filledCircle(0, 0, 0, 7, Material.HAY_BLOCK);
        b.filledCircle(0, 1, 0, 5, Material.HAY_BLOCK);
        b.circle(0, 2, 0, 7, Material.DARK_OAK_FENCE);
        b.circle(0, 3, 0, 6, Material.DARK_OAK_FENCE);
        // Giant eggs in alpha nest
        for (int dx = -2; dx <= 2; dx++)
            for (int dz = -2; dz <= 2; dz++)
                if (dx * dx + dz * dz <= 4 && !(dx == 0 && dz == 0))
                    b.setBlock(dx, 2, dz, Material.TURTLE_EGG);
        // Central glowing egg
        b.setBlock(0, 2, 0, Material.SHROOMLIGHT);
        b.setBlock(0, 3, 0, Material.DRAGON_EGG);

        // ─── Guard towers at the 4 corners ───
        for (int[] tower : new int[][]{{-16, -16}, {16, -16}, {-16, 16}, {16, 16}}) {
            b.pillar(tower[0], 1, 6, tower[1], Material.BONE_BLOCK);
            b.fillBox(tower[0] - 1, 7, tower[1] - 1, tower[0] + 1, 8, tower[1] + 1, Material.OAK_PLANKS);
            b.setBlock(tower[0], 8, tower[1], Material.LANTERN);
            b.setBlock(tower[0], 9, tower[1], Material.SKELETON_SKULL);
        }

        // ─── Watering trough (larger, center path) ───
        b.fillBox(-3, 1, 10, 3, 1, 12, Material.SMOOTH_STONE);
        b.fillBox(-2, 1, 11, 2, 1, 11, Material.WATER);

        // ─── Predator carcass as trophy (the herd has killed raptors) ───
        b.fillBox(-14, 1, 14, -12, 1, 12, Material.BONE_BLOCK);
        b.setBlock(-13, 2, 13, Material.SKELETON_SKULL);
        b.setBlock(-13, 1, 13, Material.RED_WOOL);

        // ─── Feeding pile of seeds / fruits near the central nest ───
        b.setBlock(0, 1, -8, Material.HAY_BLOCK);
        b.setBlock(-1, 1, -8, Material.MELON);
        b.setBlock(1, 1, -8, Material.PUMPKIN);
        b.setBlock(0, 2, -8, Material.SWEET_BERRY_BUSH);

        // ─── Ancient carved totem pole (tribal marker) ───
        b.pillar(15, 1, 6, -5, Material.STRIPPED_DARK_OAK_LOG);
        b.setBlock(15, 7, -5, Material.CARVED_PUMPKIN);
        b.setBlock(14, 4, -5, Material.SKELETON_SKULL);
        b.setBlock(16, 4, -5, Material.BONE_BLOCK);
        b.setBlock(15, 5, -5, Material.BONE_BLOCK);

        // ─── Broken fence gaps showing damage from past attacks ───
        b.decay(-18, 1, -18, 18, 3, 18, 0.10);

        b.placeChest(0, 1, -14);
        b.placeChest(-14, 1, 0);
        b.placeChest(0, 4, 0);    // alpha nest chest (boss reward)
        b.placeChest(-16, 8, -16); // guard tower chest
        b.setBossSpawn(0, 2, 0);
    }
}

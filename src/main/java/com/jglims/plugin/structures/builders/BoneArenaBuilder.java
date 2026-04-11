package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

/**
 * BoneArenaBuilder — a colossal T-Rex skeleton whose ribcage forms a cathedral
 * arena nave, with stepped bone bleachers, a massive skull chamber at the north
 * end, a tapering tail to the south, and soul-fire spine braziers.
 *
 * <p>Extracted verbatim from the original {@code DimensionStructureBuilders.buildBoneArena}
 * method to give the structure its own file per the one-class-per-structure
 * architecture.
 */
public final class BoneArenaBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // ─── Bone Arena: a colossal T-Rex skeleton's ribcage forms the arena ───
        // The "spine" runs north-south; ribs arch overhead like a cathedral nave.

        // Arena floor: bleached sandstone with coarse dirt patches and bone fragments
        b.filledCircle(0, 0, 0, 30, Material.SMOOTH_SANDSTONE);
        b.filledCircle(0, 0, 0, 28, Material.SUSPICIOUS_SAND);
        b.scatter(-30, 0, -30, 30, 0, 30, Material.SUSPICIOUS_SAND, Material.COARSE_DIRT, 0.35);
        b.scatter(-30, 0, -30, 30, 0, 30, Material.SUSPICIOUS_SAND, Material.BONE_BLOCK, 0.05);

        // Stepped seating tiers (5 tiers of bone bleachers)
        for (int tier = 0; tier < 5; tier++) {
            int r = 30 - tier * 2;
            int yBase = tier * 2;
            for (int yy = 0; yy < 2; yy++) b.circle(0, yBase + yy, 0, r, Material.BONE_BLOCK);
            // Tier walkway with stairs as seating
            for (int a = 0; a < 360; a += 12) {
                int sx = (int) Math.round((r - 1) * Math.cos(Math.toRadians(a)));
                int sz = (int) Math.round((r - 1) * Math.sin(Math.toRadians(a)));
                org.bukkit.block.BlockFace facing = (Math.abs(sx) > Math.abs(sz))
                        ? (sx > 0 ? org.bukkit.block.BlockFace.WEST : org.bukkit.block.BlockFace.EAST)
                        : (sz > 0 ? org.bukkit.block.BlockFace.NORTH : org.bukkit.block.BlockFace.SOUTH);
                b.setStairs(sx, yBase + 1, sz, Material.SMOOTH_SANDSTONE_STAIRS, facing, false);
            }
        }

        // ─── The spine: massive vertebrae running through the arena's center ───
        // Each vertebra is a 3x3 bone block knot
        for (int z = -26; z <= 26; z += 4) {
            // Vertebra disc
            b.fillBox(-1, 1, z - 1, 1, 2, z + 1, Material.BONE_BLOCK);
            // Spinous process (a fin sticking up)
            b.pillar(0, 3, 6, z, Material.BONE_BLOCK);
            b.setBlock(0, 7, z, Material.BONE_BLOCK);
            // Lateral processes
            b.setBlock(-2, 2, z, Material.BONE_BLOCK);
            b.setBlock(2, 2, z, Material.BONE_BLOCK);
        }

        // ─── Ribs: 14 paired ribs arching overhead from the spine ───
        // Each rib is a parametric arc traced from spine vertebra outward and upward
        for (int v = -6; v <= 6; v++) {
            int spineZ = v * 4;
            for (int side = -1; side <= 1; side += 2) {  // left and right
                // Trace an arc using parametric (cosine for height, sine for outward)
                for (int t = 0; t <= 28; t++) {
                    double angle = Math.toRadians((double) t * 90 / 28);  // 0 to 90 deg
                    int rx = (int) Math.round(side * 26 * Math.sin(angle));
                    int ry = 1 + (int) Math.round(20 * Math.cos(angle) + 4);
                    // The rib starts at the spine top and arches outward and down
                    // Reverse the parameterization so t=0 is at spine top
                    int finalX = (int) Math.round(side * 26 * Math.sin(angle));
                    int finalY = 6 + (int) Math.round(18 * (1 - Math.cos(angle * 0.9)));
                    int actualY = 26 - finalY;
                    if (actualY > 1) {
                        b.setBlock(finalX, actualY, spineZ, Material.BONE_BLOCK);
                        // Thicker mid-section
                        if (Math.abs(finalX) > 5 && Math.abs(finalX) < 22) {
                            b.setBlock(finalX, actualY + 1, spineZ, Material.BONE_BLOCK);
                        }
                    }
                }
            }
        }

        // ─── The skull: massive T-Rex skull at the north end of the arena ───
        // Skull chamber (a 14x12x18 block enclosure shaped like a skull)
        b.fillBox(-7, 1, -42, 7, 14, -28, Material.BONE_BLOCK);
        b.fillBox(-6, 2, -41, 6, 13, -29, Material.AIR);
        // Tapered front (snout)
        for (int z = -42; z <= -38; z++) {
            int taper = (-42 - z) / 2 + 4;
            b.fillBox(-taper, 2, z, taper, 6, z, Material.AIR);
        }
        // Eye sockets (large recessed circles)
        for (int dy = -2; dy <= 2; dy++)
            for (int dz = -2; dz <= 2; dz++)
                if (dy * dy + dz * dz <= 4) {
                    b.setBlock(-7, 10 + dy, -34 + dz, Material.AIR);
                    b.setBlock(7, 10 + dy, -34 + dz, Material.AIR);
                }
        // Glowing eyes deep inside
        b.setBlock(-6, 10, -34, Material.SOUL_LANTERN);
        b.setBlock(6, 10, -34, Material.SOUL_LANTERN);
        // Nostril holes on snout
        b.setBlock(-1, 8, -42, Material.AIR);
        b.setBlock(1, 8, -42, Material.AIR);
        // Massive jaw teeth (lower jaw row)
        for (int x = -6; x <= 6; x += 2) b.setBlock(x, 1, -38, Material.BONE_BLOCK);
        // Upper teeth row
        for (int x = -6; x <= 6; x += 2) b.setBlock(x, 5, -39, Material.BONE_BLOCK);
        // Skull crest / horns
        b.setBlock(-4, 14, -32, Material.BONE_BLOCK);
        b.setBlock(4, 14, -32, Material.BONE_BLOCK);
        b.setBlock(0, 15, -32, Material.BONE_BLOCK);

        // ─── Tail at the south end: vertebrae tapering away ───
        for (int z = 27; z <= 40; z++) {
            int taper = Math.max(0, 2 - (z - 27) / 5);
            b.fillBox(-taper, 1, z, taper, 1 + taper, z, Material.BONE_BLOCK);
            if (z % 3 == 0) b.setBlock(0, 2 + taper, z, Material.BONE_BLOCK);
        }

        // ─── Combat features: 4 destructible bone formations for cover ───
        for (int i = 0; i < 4; i++) {
            int a = i * 90 + 45;
            int px = (int) Math.round(14 * Math.cos(Math.toRadians(a)));
            int pz = (int) Math.round(14 * Math.sin(Math.toRadians(a)));
            // Stalagmite-like bone formation
            b.pillar(px, 1, 4, pz, Material.BONE_BLOCK);
            b.setBlock(px, 5, pz, Material.SKELETON_SKULL);
            b.setBlock(px - 1, 1, pz, Material.BONE_BLOCK);
            b.setBlock(px + 1, 1, pz, Material.BONE_BLOCK);
            b.setBlock(px, 1, pz - 1, Material.BONE_BLOCK);
            b.setBlock(px, 1, pz + 1, Material.BONE_BLOCK);
        }

        // ─── Trophy skulls embedded in the seating walls ───
        for (int a = 0; a < 360; a += 20) {
            int sx = (int) Math.round(29 * Math.cos(Math.toRadians(a)));
            int sz = (int) Math.round(29 * Math.sin(Math.toRadians(a)));
            b.setBlock(sx, 9, sz, Material.SKELETON_SKULL);
            // Lit braziers between skulls
            if (a % 40 == 0) {
                int bx = (int) Math.round(28 * Math.cos(Math.toRadians(a + 10)));
                int bz = (int) Math.round(28 * Math.sin(Math.toRadians(a + 10)));
                b.setBlock(bx, 10, bz, Material.CAMPFIRE);
            }
        }

        // Soul-fire braziers on the spine vertebrae for moody lighting
        for (int z = -24; z <= 24; z += 8) {
            b.setBlock(0, 8, z, Material.SOUL_CAMPFIRE);
        }

        // Loot caches: chests embedded between vertebrae and in the skull
        b.placeChest(0, 2, 0);
        b.placeChest(0, 2, 8);
        b.placeChest(0, 2, -8);
        b.placeChest(0, 5, -34);   // Inside the skull
        b.placeChest(15, 1, 15);
        b.placeChest(-15, 1, 15);

        b.setBossSpawn(0, 1, 0);
    }
}

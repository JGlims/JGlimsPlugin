package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

/**
 * PillagerAirshipBuilder — sky galleon with dark oak hull, gray wool balloon envelope, cannons, and captain's cabin.
 *
 * <p>Extracted verbatim from the original {@code OverworldStructureBuilders.buildPillagerAirship}
 * method to give the structure its own file per the one-class-per-structure
 * architecture.
 */
public final class PillagerAirshipBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        int baseY = 15;

        // Hull (boat shape)
        for (int z = -12; z <= 12; z++) {
            int halfWidth = z < -8 ? (z + 12) : (z > 8 ? (12 - z) : 4);
            if (halfWidth < 1) halfWidth = 1;
            b.fillBox(-halfWidth, baseY, z, halfWidth, baseY, z, Material.DARK_OAK_PLANKS);
            b.setBlock(-halfWidth, baseY + 1, z, Material.DARK_OAK_PLANKS);
            b.setBlock(halfWidth, baseY + 1, z, Material.DARK_OAK_PLANKS);
            b.setBlock(-halfWidth, baseY + 2, z, Material.DARK_OAK_FENCE);
            b.setBlock(halfWidth, baseY + 2, z, Material.DARK_OAK_FENCE);
        }

        // Deck interior
        b.fillFloor(-3, -10, 3, 10, baseY, Material.OAK_PLANKS);

        // Below-deck cargo hold
        b.fillBox(-3, baseY - 3, -6, 3, baseY - 1, 6, Material.DARK_OAK_PLANKS);
        b.fillBox(-2, baseY - 2, -5, 2, baseY - 1, 5, Material.AIR);
        b.setBlock(0, baseY - 2, 0, Material.BARREL);
        b.setBlock(1, baseY - 2, 2, Material.BARREL);
        b.setBlock(-1, baseY - 2, -3, Material.BARREL);
        b.setBlock(0, baseY - 2, 4, Material.TNT);

        // Captain's cabin (stern)
        b.fillBox(-3, baseY + 1, 6, 3, baseY + 4, 10, Material.DARK_OAK_PLANKS);
        b.fillBox(-2, baseY + 1, 7, 2, baseY + 3, 9, Material.AIR);
        b.setBlock(0, baseY + 1, 6, Material.AIR); b.setBlock(0, baseY + 2, 6, Material.AIR);
        b.setBlock(0, baseY + 1, 8, Material.CRAFTING_TABLE);
        b.setBlock(1, baseY + 1, 9, Material.RED_BED);
        b.setBlock(1, baseY + 3, 8, Material.LANTERN);
        b.setBlock(-2, baseY + 2, 10, Material.GLASS_PANE);
        b.setBlock(2, baseY + 2, 10, Material.GLASS_PANE);

        // Balloon (gray wool envelope)
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

        // Mast and crow's nest
        b.pillar(0, baseY + 1, 10, -10, Material.OAK_LOG);
        b.fillBox(-2, baseY + 11, -12, 2, baseY + 11, -8, Material.OAK_PLANKS);
        b.circle(0, baseY + 12, -10, 2, Material.OAK_FENCE);

        // Cannons (dispensers)
        b.setDirectional(-3, baseY + 1, -6, Material.DISPENSER, BlockFace.WEST);
        b.setDirectional(3, baseY + 1, -6, Material.DISPENSER, BlockFace.EAST);
        b.setDirectional(-3, baseY + 1, 0, Material.DISPENSER, BlockFace.WEST);
        b.setDirectional(3, baseY + 1, 0, Material.DISPENSER, BlockFace.EAST);

        // Crossbow mounts on deck
        b.setBlock(-3, baseY + 1, -4, Material.DARK_OAK_FENCE);
        b.setBlock(-3, baseY + 2, -4, Material.TRIPWIRE_HOOK);
        b.setBlock(3, baseY + 1, 2, Material.DARK_OAK_FENCE);
        b.setBlock(3, baseY + 2, 2, Material.TRIPWIRE_HOOK);

        // Banners
        b.setBlock(0, baseY + 12, -10, Material.WHITE_BANNER);
        b.setBlock(-4, baseY + 2, 0, Material.WHITE_BANNER);
        b.setBlock(4, baseY + 2, 0, Material.WHITE_BANNER);

        // Rigging (ladders as rope)
        for (int y = baseY + 2; y <= baseY + 10; y++) {
            b.setBlock(0, y, -10, Material.LADDER);
        }

        b.placeChest(0, baseY + 1, 9);
        b.placeChest(0, baseY - 2, 3);
        b.placeChest(1, baseY + 1, -4);
        b.setBossSpawn(0, baseY + 1, 0);
    }
}

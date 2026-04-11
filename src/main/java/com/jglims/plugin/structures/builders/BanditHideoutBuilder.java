package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;

/**
 * BanditHideoutBuilder — sandstone canyon hideout with wooden platforms, loot piles, and escape tunnel.
 *
 * <p>Extracted verbatim from the original monolithic builder method.
 */
public final class BanditHideoutBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // Canyon walls
        b.fillWalls(-12, 0, -10, 12, 8, 10, Material.SANDSTONE);
        b.hollowBox(-12, 1, -10, 12, 8, 10);
        b.fillFloor(-11, -9, 11, 9, 0, Material.SAND);

        // Cave entrance (north)
        b.fillBox(-3, 1, -12, 3, 5, -10, Material.AIR);

        // Wooden platforms inside canyon walls (multiple levels)
        // Platform 1 (west, low)
        b.fillBox(-11, 3, -5, -8, 3, -2, Material.OAK_PLANKS);
        b.circle(-10, 4, -3, 1, Material.OAK_FENCE);
        b.setBlock(-10, 4, -4, Material.LANTERN);

        // Platform 2 (east, mid)
        b.fillBox(8, 5, 1, 11, 5, 4, Material.OAK_PLANKS);
        b.circle(9, 6, 2, 1, Material.OAK_FENCE);
        b.setBlock(9, 6, 3, Material.LANTERN);

        // Platform 3 (west, high)
        b.fillBox(-11, 6, 3, -8, 6, 6, Material.OAK_PLANKS);
        b.setBlock(-10, 7, 5, Material.OAK_FENCE);

        // Rope bridges (ladders between platforms)
        for (int y = 1; y <= 3; y++) b.setBlock(-10, y, -4, Material.LADDER);
        for (int y = 4; y <= 5; y++) b.setBlock(8, y, 2, Material.LADDER);
        for (int y = 4; y <= 6; y++) b.setBlock(-9, y, 4, Material.LADDER);

        // Rope bridge (string/planks) between platforms
        for (int x = -7; x <= 7; x++) {
            b.setBlock(x, 5, 2, Material.OAK_PLANKS);
            b.setBlock(x, 6, 3, Material.OAK_FENCE);
            b.setBlock(x, 6, 1, Material.OAK_FENCE);
        }

        // Stolen loot piles
        b.fillBox(-8, 1, -7, -6, 2, -5, Material.HAY_BLOCK);
        b.setBlock(-7, 3, -6, Material.GOLD_BLOCK);
        b.fillBox(6, 1, 5, 8, 1, 7, Material.BARREL);

        // Central campfire
        b.setBlock(0, 1, 0, Material.CAMPFIRE);
        b.setBlock(-1, 1, 1, Material.STRIPPED_OAK_LOG);
        b.setBlock(1, 1, -1, Material.STRIPPED_OAK_LOG);

        // Lookout post (on top of east wall)
        b.fillBox(10, 8, -2, 12, 8, 2, Material.OAK_PLANKS);
        b.circle(11, 9, 0, 1, Material.OAK_FENCE);
        b.setBlock(11, 9, 0, Material.LANTERN);
        for (int y = 1; y <= 8; y++) b.setBlock(11, y, -9, Material.LADDER);

        // Escape tunnel (south)
        b.fillBox(-1, 1, 10, 1, 3, 14, Material.AIR);
        b.fillWalls(-2, 0, 10, 2, 3, 14, Material.CUT_SANDSTONE);

        // Weapon rack area
        b.setBlock(-5, 1, 0, Material.GRINDSTONE);
        b.setBlock(-5, 1, 1, Material.SMITHING_TABLE);

        b.placeChest(-7, 1, -5);
        b.placeChest(7, 1, 6);
        b.placeChest(0, 1, 12);
        b.setBossSpawn(0, 1, 3);
    }
}

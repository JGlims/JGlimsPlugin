package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;

/**
 * SunkenRuinsBuilder — prismarine ruins with underwater chambers, shipwreck, and drowned relics.
 *
 * <p>Extracted verbatim from the original monolithic builder method.
 */
public final class SunkenRuinsBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // Base platform
        b.filledCircle(0, 0, 0, 14, Material.PRISMARINE_BRICKS);

        // Rising walls (multiple tiers, partially decayed)
        b.filledCircle(0, 1, 0, 12, Material.PRISMARINE);
        b.circle(0, 2, 0, 12, Material.PRISMARINE);
        b.circle(0, 3, 0, 12, Material.PRISMARINE);
        b.circle(0, 4, 0, 10, Material.PRISMARINE_BRICKS);
        b.circle(0, 5, 0, 10, Material.PRISMARINE_BRICKS);

        // Pillars
        for (int a = 0; a < 360; a += 40) {
            int px = (int) (10 * Math.cos(Math.toRadians(a)));
            int pz = (int) (10 * Math.sin(Math.toRadians(a)));
            int h = 4 + b.getRandom().nextInt(5);
            b.pillar(px, 1, h, pz, Material.PRISMARINE_WALL);
        }

        // Central altar
        b.fillBox(-2, 1, -2, 2, 1, 2, Material.DARK_PRISMARINE);
        b.setBlock(0, 2, 0, Material.SEA_LANTERN);
        b.setBlock(-2, 2, -2, Material.SEA_LANTERN);
        b.setBlock(2, 2, 2, Material.SEA_LANTERN);

        // Coral decorations around perimeter
        Material[] corals = {Material.BRAIN_CORAL_BLOCK, Material.TUBE_CORAL_BLOCK,
                Material.FIRE_CORAL_BLOCK, Material.HORN_CORAL_BLOCK};
        for (int a = 0; a < 360; a += 20) {
            int cx = (int) (13 * Math.cos(Math.toRadians(a)));
            int cz = (int) (13 * Math.sin(Math.toRadians(a)));
            b.setBlockIfAir(cx, 1, cz, corals[a / 20 % 4]);
        }

        // Air pocket rooms (underwater chambers)
        b.fillBox(-6, -3, -6, -2, 0, -2, Material.PRISMARINE_BRICKS);
        b.fillBox(-5, -2, -5, -3, -1, -3, Material.AIR);
        b.fillBox(2, -3, 2, 6, 0, 6, Material.PRISMARINE_BRICKS);
        b.fillBox(3, -2, 3, 5, -1, 5, Material.AIR);

        // Sea lantern lighting throughout
        b.setBlock(-4, -1, -4, Material.SEA_LANTERN);
        b.setBlock(4, -1, 4, Material.SEA_LANTERN);

        // Decay and ruin effects
        b.scatter(-14, 0, -14, 14, 6, 14, Material.PRISMARINE_BRICKS, Material.PRISMARINE, 0.15);
        b.decay(-14, 3, -14, 14, 8, 14, 0.2);

        // Kelp and seagrass ambiance
        b.scatter(-14, 1, -14, 14, 2, 14, Material.AIR, Material.SEAGRASS, 0.05);
        b.scatter(-14, 1, -14, 14, 3, 14, Material.AIR, Material.KELP_PLANT, 0.08);

        // ─── Sunken ship wreckage (southwest) ───
        b.fillBox(-13, 0, -13, -8, 4, -8, Material.DARK_OAK_PLANKS);
        b.fillBox(-12, 1, -12, -9, 3, -9, Material.AIR);
        b.decay(-13, 0, -13, -8, 4, -8, 0.35);
        // Mast
        b.pillar(-10, 4, 8, -10, Material.DARK_OAK_LOG);
        b.setBlock(-10, 9, -10, Material.WHITE_WOOL);  // tattered sail
        b.setBlock(-11, 8, -10, Material.WHITE_WOOL);
        b.setBlock(-9, 8, -10, Material.WHITE_WOOL);
        b.decay(-12, 5, -12, -8, 10, -8, 0.4);

        // ─── Drowned remains circle around the altar ───
        for (int i = 0; i < 8; i++) {
            int a = i * 45;
            int sx = (int) Math.round(6 * Math.cos(Math.toRadians(a)));
            int sz = (int) Math.round(6 * Math.sin(Math.toRadians(a)));
            b.setBlock(sx, 1, sz, Material.BONE_BLOCK);
            if (i % 2 == 0) b.setBlock(sx, 2, sz, Material.SKELETON_SKULL);
        }

        // ─── Trident racks on interior walls (4 display pedestals) ───
        for (int[] rack : new int[][]{{-8, 0}, {8, 0}, {0, -8}, {0, 8}}) {
            b.pillar(rack[0], 1, 3, rack[1], Material.PRISMARINE_BRICKS);
            b.setBlock(rack[0], 4, rack[1], Material.SEA_LANTERN);
        }

        // ─── Connecting tunnel from eastern chamber to altar ───
        b.fillBox(2, -2, 2, 2, -1, -1, Material.AIR);
        b.fillBox(2, -3, 2, 2, -3, -1, Material.PRISMARINE_BRICKS);
        for (int x = 2; x >= -1; x--) b.setBlock(x, -1, 0, Material.SEA_LANTERN);

        // ─── Treasure hoard in the underwater chamber ───
        b.setBlock(-4, -2, -3, Material.GOLD_BLOCK);
        b.setBlock(-3, -2, -4, Material.GOLD_BLOCK);
        b.setBlock(4, -2, 3, Material.DIAMOND_BLOCK);
        b.setBlock(3, -2, 4, Material.EMERALD_BLOCK);

        b.placeChest(0, 1, 5);
        b.placeChest(-4, -2, -4);
        b.placeChest(4, -2, 4);
        b.placeChest(-10, 1, -10);  // shipwreck chest
        b.setBossSpawn(0, 1, 0);
    }
}

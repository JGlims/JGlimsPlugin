package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;
import org.bukkit.Material;

/**
 * LunarBaseBuilder — derelict white-concrete moon base with 4 wings (airlock, hydroponics,
 * crew quarters, control, oxygen), central corridor, observation dome, outside landing pad,
 * antenna farm, fuel depot, and warning lights.
 */
public final class LunarBaseBuilder implements IStructureBuilder {
    @Override
    public void build(StructureBuilder b) {
        // ── Outside landing pad ────────────────────────────────────────
        b.filledCircle(0, 0, 28, 8, Material.GRAY_CONCRETE);
        b.circle(0, 0, 28, 7, Material.YELLOW_CONCRETE);
        b.circle(0, 0, 28, 6, Material.BLACK_CONCRETE);
        b.circle(0, 0, 28, 5, Material.YELLOW_CONCRETE);
        // Landing lights
        for (int a = 0; a < 360; a += 45) {
            int lx = (int) (7 * Math.cos(Math.toRadians(a)));
            int lz = 28 + (int) (7 * Math.sin(Math.toRadians(a)));
            b.setBlock(lx, 1, lz, Material.REDSTONE_LAMP);
        }
        // Landing beacon pillar
        b.pillar(0, 0, 5, 28, Material.IRON_BLOCK);
        b.setBlock(0, 6, 28, Material.BEACON);
        b.setBlock(0, 7, 28, Material.END_ROD);

        // ── Main building shell ─────────────────────────────────────────
        b.fillBox(-18, 0, -18, 18, 8, 18, Material.WHITE_CONCRETE);
        b.fillBox(-17, 1, -17, 17, 7, 17, Material.AIR);
        b.fillFloor(-17, -17, 17, 17, 0, Material.LIGHT_GRAY_CONCRETE);
        // Foundation weathering
        b.scatter(-18, 0, -18, 18, 1, 18, Material.WHITE_CONCRETE, Material.LIGHT_GRAY_CONCRETE, 0.15);
        b.scatter(-18, 0, -18, 18, 8, 18, Material.WHITE_CONCRETE, Material.CRACKED_STONE_BRICKS, 0.05);

        // Exterior reinforced corners
        for (int[] c : new int[][]{{-18, -18}, {18, -18}, {-18, 18}, {18, 18}}) {
            b.pillar(c[0], 0, 9, c[1], Material.DEEPSLATE_BRICKS);
            b.setBlock(c[0], 10, c[1], Material.LIGHTNING_ROD);
        }

        // ── Airlock (south entrance, double iron doors) ─────────────────
        b.fillBox(-3, 0, -18, 3, 6, -14, Material.GRAY_CONCRETE);
        b.fillBox(-2, 1, -17, 2, 5, -15, Material.AIR);
        b.setBlock(-1, 1, -18, Material.IRON_DOOR);
        b.setBlock(1, 1, -18, Material.IRON_DOOR);
        b.setBlock(-1, 1, -14, Material.IRON_DOOR);
        b.setBlock(1, 1, -14, Material.IRON_DOOR);
        // Caution stripes
        for (int x = -2; x <= 2; x++) b.setBlock(x, 0, -16, Material.YELLOW_CONCRETE);
        for (int x = -2; x <= 2; x++) b.setBlock(x, 0, -15, Material.BLACK_CONCRETE);
        // Red emergency light
        b.setBlock(0, 6, -15, Material.REDSTONE_LAMP);

        // ── Central corridor ────────────────────────────────────────────
        b.fillBox(-2, 1, -14, 2, 5, 14, Material.AIR);
        b.fillBox(-2, 0, -14, 2, 0, 14, Material.LIGHT_GRAY_CONCRETE);
        // Corridor floor stripe
        for (int z = -14; z <= 14; z += 2) b.setBlock(0, 0, z, Material.YELLOW_CONCRETE);
        // Corridor ceiling lights
        for (int z = -12; z <= 12; z += 4) b.setBlock(0, 5, z, Material.SEA_LANTERN);

        // ── Hydroponics bay (NE) ────────────────────────────────────────
        b.fillBox(4, 0, -16, 16, 7, -4, Material.WHITE_CONCRETE);
        b.fillBox(5, 1, -15, 15, 6, -5, Material.AIR);
        b.fillFloor(5, -15, 15, -5, 0, Material.LIGHT_GRAY_CONCRETE);
        // Connecting doorway to corridor
        b.setBlock(4, 1, -10, Material.AIR);
        b.setBlock(4, 2, -10, Material.AIR);
        // Farmland plots with crops (wheat, beetroots, dead+live mix)
        for (int x = 6; x <= 14; x += 2) {
            for (int z = -14; z <= -6; z += 2) {
                b.setBlock(x, 0, z, Material.FARMLAND);
                Material crop = switch (((x + z) / 2) % 4) {
                    case 0 -> Material.DEAD_BUSH;
                    case 1 -> Material.WHEAT;
                    case 2 -> Material.BEETROOTS;
                    default -> Material.POTATOES;
                };
                b.setBlock(x, 1, z, crop);
            }
        }
        // Grow lights
        b.wallLighting(5, 5, -15, 15, -15, 3, Material.SEA_LANTERN);
        b.wallLighting(5, 5, -5, 15, -5, 3, Material.SEA_LANTERN);
        // Irrigation channels
        b.fillBox(5, 0, -10, 15, 0, -10, Material.LIGHT_BLUE_STAINED_GLASS);
        // Hydroponic tubes
        for (int x = 6; x <= 14; x += 4) {
            b.pillar(x, 1, 5, -10, Material.LIGHT_BLUE_STAINED_GLASS);
            b.setBlock(x, 6, -10, Material.IRON_BLOCK);
        }

        // ── Crew quarters (NW) ──────────────────────────────────────────
        b.fillBox(-16, 0, -16, -4, 7, -4, Material.WHITE_CONCRETE);
        b.fillBox(-15, 1, -15, -5, 6, -5, Material.AIR);
        b.fillFloor(-15, -15, -5, -5, 0, Material.LIGHT_GRAY_CONCRETE);
        b.setBlock(-4, 1, -10, Material.AIR);
        b.setBlock(-4, 2, -10, Material.AIR);
        // 4 crew bunks with lockers
        for (int z = -14; z <= -6; z += 4) {
            b.setBlock(-14, 1, z, Material.WHITE_BED);
            b.setBlock(-14, 1, z + 1, Material.BARREL);
            b.setBlock(-14, 2, z + 1, Material.BARREL);
            b.setBlock(-6, 1, z, Material.WHITE_BED);
            b.setBlock(-6, 1, z + 1, Material.BARREL);
        }
        // Mess area
        b.table(-10, 1, -10);
        b.setBlock(-9, 1, -10, Material.OAK_STAIRS);
        b.setBlock(-11, 1, -10, Material.OAK_STAIRS);
        b.setBlock(-10, 1, -9, Material.OAK_STAIRS);
        b.setBlock(-10, 1, -11, Material.OAK_STAIRS);
        // Kitchenette
        b.setBlock(-15, 1, -14, Material.FURNACE);
        b.setBlock(-15, 1, -13, Material.SMOKER);
        b.setBlock(-15, 1, -12, Material.BARREL);
        b.chandelier(-10, 7, -10, 1);
        b.setBlock(-10, 6, -10, Material.REDSTONE_LAMP);

        // ── Control room (SE) ───────────────────────────────────────────
        b.fillBox(4, 0, 4, 16, 7, 16, Material.WHITE_CONCRETE);
        b.fillBox(5, 1, 5, 15, 6, 15, Material.AIR);
        b.fillFloor(5, 5, 15, 15, 0, Material.GRAY_CONCRETE);
        b.setBlock(4, 1, 10, Material.AIR);
        b.setBlock(4, 2, 10, Material.AIR);
        // Main control console
        b.fillBox(5, 1, 14, 15, 3, 14, Material.REDSTONE_LAMP);
        b.fillBox(5, 1, 13, 15, 1, 13, Material.SMOOTH_STONE_SLAB);
        for (int x = 6; x <= 14; x += 2) {
            b.setBlock(x, 2, 13, Material.LEVER);
        }
        for (int x = 7; x <= 13; x += 2) {
            b.setBlock(x, 2, 13, Material.STONE_BUTTON);
        }
        // Map projector
        b.setBlock(10, 4, 14, Material.SEA_LANTERN);
        b.setBlock(10, 1, 8, Material.LODESTONE);
        // Command chairs
        b.chair(9, 1, 11, Material.POLISHED_ANDESITE_STAIRS, org.bukkit.block.BlockFace.NORTH);
        b.chair(11, 1, 11, Material.POLISHED_ANDESITE_STAIRS, org.bukkit.block.BlockFace.NORTH);
        // Computer banks
        b.setBlock(14, 1, 6, Material.OBSERVER);
        b.setBlock(14, 1, 8, Material.OBSERVER);
        b.setBlock(14, 1, 10, Material.OBSERVER);
        b.setBlock(14, 2, 6, Material.REDSTONE_LAMP);
        b.setBlock(14, 2, 8, Material.REDSTONE_LAMP);
        b.setBlock(14, 2, 10, Material.REDSTONE_LAMP);

        // ── Oxygen tanks (SW corner — blue glass cylinders) ─────────────
        b.fillBox(-16, 0, 4, -4, 7, 16, Material.WHITE_CONCRETE);
        b.fillBox(-15, 1, 5, -5, 6, 15, Material.AIR);
        b.setBlock(-4, 1, 10, Material.AIR);
        b.setBlock(-4, 2, 10, Material.AIR);
        // Tanks
        for (int x = -14; x <= -6; x += 4) {
            b.pillar(x, 1, 5, 10, Material.LIGHT_BLUE_STAINED_GLASS);
            b.setBlock(x, 1, 10, Material.IRON_BLOCK);
            b.setBlock(x, 6, 10, Material.IRON_BLOCK);
            b.setBlock(x, 5, 10, Material.LIGHTNING_ROD);
        }
        // Pipes
        for (int x = -14; x <= -6; x += 4) {
            b.setBlock(x, 6, 9, Material.IRON_BARS);
            b.setBlock(x, 6, 11, Material.IRON_BARS);
        }
        // Warning light
        b.setBlock(-10, 6, 15, Material.REDSTONE_LAMP);

        // ── Ceiling ─────────────────────────────────────────────────────
        b.fillFloor(-17, -17, 17, 17, 8, Material.WHITE_CONCRETE);
        // Central observation dome
        b.dome(0, 8, 0, 5, Material.LIGHT_BLUE_STAINED_GLASS);
        // Dome access ladder
        for (int y = 8; y <= 12; y++) b.setBlock(0, y, -2, Material.LADDER);

        // ── Exterior antenna farm (roof) ────────────────────────────────
        for (int[] ant : new int[][]{{-14, -14}, {14, -14}, {-14, 14}, {14, 14}}) {
            b.pillar(ant[0], 9, 6, ant[1], Material.IRON_BARS);
            b.setBlock(ant[0], 15, ant[1], Material.LIGHTNING_ROD);
            b.setBlock(ant[0], 14, ant[1], Material.END_ROD);
        }

        // ── Fuel depot (east exterior) ──────────────────────────────────
        for (int[] fuel : new int[][]{{20, -2}, {20, 0}, {20, 2}}) {
            b.pillar(fuel[0], 1, 3, fuel[1], Material.BLACK_CONCRETE);
            b.setBlock(fuel[0], 4, fuel[1], Material.IRON_BLOCK);
            b.setBlock(fuel[0], 5, fuel[1], Material.REDSTONE_LAMP);
        }

        // ── Abandoned details ───────────────────────────────────────────
        b.scatter(-16, 1, -16, 16, 1, 16, Material.AIR, Material.COBWEB, 0.04);
        b.scatter(-15, 0, -15, 15, 0, 15, Material.LIGHT_GRAY_CONCRETE, Material.GRAVEL, 0.03);

        // ── Loot ────────────────────────────────────────────────────────
        b.placeChest(10, 1, 10);    // control room
        b.placeChest(-12, 1, -12);  // crew quarters
        b.placeChest(10, 1, -10);   // hydroponics
        b.placeChest(-10, 1, 10);   // oxygen
        b.placeChest(0, 0, 28);     // landing pad
        b.setBossSpawn(0, 1, 0);
    }
}

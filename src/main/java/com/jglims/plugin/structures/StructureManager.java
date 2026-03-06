
    // ── NEW OVERWORLD (Plus Part 2) ──

    private void buildFrostDungeon(StructureBuilder b) {
        b.fillWalls(-15, 0, -15, 15, 8, 15, Material.PACKED_ICE); b.hollowBox(-15, 1, -15, 15, 8, 15);
        b.fillFloor(-14, -14, 14, 14, 0, Material.BLUE_ICE);
        for (int a = 0; a < 360; a += 45) {
            int px = (int)(10*Math.cos(Math.toRadians(a))); int pz = (int)(10*Math.sin(Math.toRadians(a)));
            b.pillar(px, 1, 7, pz, Material.PACKED_ICE); b.setBlock(px, 8, pz, Material.SEA_LANTERN);
        }
        b.fillBox(-5, 1, 8, 5, 5, 14, Material.AIR); b.fillBox(-5, 0, 8, 5, 0, 14, Material.BLUE_ICE);
        b.fillBox(-1, 1, 12, 1, 3, 12, Material.PACKED_ICE); b.setBlock(0, 4, 12, Material.SEA_LANTERN);
        b.fillBox(-15, 1, -2, -20, 4, 2, Material.AIR); b.fillWalls(-20, 0, -5, -15, 5, 5, Material.PACKED_ICE);
        b.fillBox(15, 1, -2, 20, 4, 2, Material.AIR); b.fillWalls(15, 0, -5, 20, 5, 5, Material.PACKED_ICE);
        b.scatter(-14, 0, -14, 14, 0, 14, Material.BLUE_ICE, Material.POWDER_SNOW, 0.08);
        b.placeChest(0, 1, 10); b.placeChest(-18, 1, 0); b.placeChest(18, 1, 0);
        b.setBossSpawn(0, 1, 11);
    }

    private void buildBanditHideout(StructureBuilder b) {
        b.fillWalls(-10, 0, -8, 10, 6, 8, Material.SANDSTONE); b.hollowBox(-10, 1, -8, 10, 6, 8);
        b.fillFloor(-9, -7, 9, 7, 0, Material.SAND);
        b.fillBox(-3, 1, -10, 3, 4, -8, Material.AIR);
        b.fillBox(-8, 1, -5, -6, 3, -3, Material.BARREL);
        b.fillBox(6, 1, 3, 8, 2, 5, Material.HAY_BLOCK);
        b.setBlock(0, 1, 0, Material.CAMPFIRE);
        b.fillBox(-5, 1, 8, 5, 4, 12, Material.AIR); b.fillWalls(-5, 0, 8, 5, 4, 12, Material.CUT_SANDSTONE);
        b.setBlock(0, 1, 8, Material.AIR); b.setBlock(0, 2, 8, Material.AIR);
        b.placeChest(-7, 1, -4); b.placeChest(7, 1, 4); b.placeChest(0, 1, 10);
        b.setBossSpawn(0, 1, 3);
    }

    private void buildSunkenRuins(StructureBuilder b) {
        b.filledCircle(0, 0, 0, 12, Material.PRISMARINE_BRICKS);
        b.filledCircle(0, 1, 0, 10, Material.PRISMARINE);
        b.filledCircle(0, 2, 0, 8, Material.PRISMARINE);
        for (int a = 0; a < 360; a += 40) {
            int px = (int)(10*Math.cos(Math.toRadians(a))); int pz = (int)(10*Math.sin(Math.toRadians(a)));
            b.pillar(px, 1, 4 + b.getRandom().nextInt(5), pz, Material.PRISMARINE_BRICK_WALL);
        }
        b.fillBox(-2, 1, -2, 2, 1, 2, Material.DARK_PRISMARINE);
        b.setBlock(0, 2, 0, Material.SEA_LANTERN);
        b.scatter(-12, 0, -12, 12, 6, 12, Material.PRISMARINE_BRICKS, Material.PRISMARINE, 0.15);
        b.decay(-12, 3, -12, 12, 8, 12, 0.2);
        b.placeChest(0, 1, 5); b.placeChest(-6, 1, -3); b.setBossSpawn(0, 1, 0);
    }

    private void buildCursedGraveyard(StructureBuilder b) {
        b.filledCircle(0, 0, 0, 10, Material.PODZOL);
        b.scatter(-10, 0, -10, 10, 0, 10, Material.PODZOL, Material.SOUL_SOIL, 0.3);
        b.circle(0, 1, 0, 10, Material.IRON_BARS);
        int[][] graves = {{-6,3},{-3,5},{0,7},{3,5},{6,3},{-4,-3},{-1,-6},{2,-4},{5,-2},{-7,0}};
        for (int[] g : graves) { b.setBlock(g[0], 1, g[1], Material.STONE_BRICK_WALL); b.setBlock(g[0], 2, g[1], Material.STONE_BRICK_WALL); }
        b.pillar(-8, 1, 5, -6, Material.DARK_OAK_LOG); b.pillar(7, 1, 4, 5, Material.DARK_OAK_LOG);
        b.setBlock(0, 1, 0, Material.SOUL_CAMPFIRE);
        b.setBlock(-5, 1, 0, Material.SOUL_LANTERN); b.setBlock(5, 1, 0, Material.SOUL_LANTERN);
        b.scatter(-10, 1, -10, 10, 3, 10, Material.AIR, Material.COBWEB, 0.05);
        b.fillWalls(-2, 1, -2, 2, 4, 2, Material.DEEPSLATE_BRICKS); b.hollowBox(-2, 1, -2, 2, 4, 2);
        b.setBlock(0, 1, -2, Material.AIR); b.setBlock(0, 2, -2, Material.AIR);
        b.fillBox(-2, 5, -2, 2, 5, 2, Material.DEEPSLATE_BRICK_SLAB);
        b.placeChest(0, 1, 0); b.placeChest(-5, 0, 4); b.setBossSpawn(0, 1, -4);
    }

    private void buildSkyAltar(StructureBuilder b) {
        b.filledCircle(0, 0, 0, 8, Material.QUARTZ_BLOCK);
        b.filledCircle(0, -1, 0, 6, Material.QUARTZ_BLOCK);
        b.filledCircle(0, -2, 0, 4, Material.QUARTZ_BLOCK);
        for (int[] p : new int[][]{{-7,0},{7,0},{0,-7},{0,7}}) {
            b.pillar(p[0], 1, 8, p[1], Material.QUARTZ_PILLAR);
            b.setBlock(p[0], 9, p[1], Material.END_ROD);
        }
        b.fillBox(-1, 1, -1, 1, 1, 1, Material.GOLD_BLOCK);
        b.setBlock(0, 2, 0, Material.BEACON); b.setBlock(0, 3, 0, Material.END_ROD);
        // Decorative arch elements
        b.setBlock(-4, 4, 0, Material.QUARTZ_STAIRS); b.setBlock(-3, 5, 0, Material.QUARTZ_BLOCK);
        b.setBlock(-2, 6, 0, Material.QUARTZ_BLOCK); b.setBlock(-1, 6, 0, Material.QUARTZ_BLOCK);
        b.setBlock(0, 6, 0, Material.QUARTZ_BLOCK);
        b.setBlock(1, 6, 0, Material.QUARTZ_BLOCK); b.setBlock(2, 6, 0, Material.QUARTZ_BLOCK);
        b.setBlock(3, 5, 0, Material.QUARTZ_BLOCK); b.setBlock(4, 4, 0, Material.QUARTZ_STAIRS);
        // Floating crystal shards around edges
        b.setBlock(-6, 2, -6, Material.AMETHYST_CLUSTER); b.setBlock(6, 2, 6, Material.AMETHYST_CLUSTER);
        b.setBlock(-6, 2, 6, Material.AMETHYST_CLUSTER); b.setBlock(6, 2, -6, Material.AMETHYST_CLUSTER);
        b.placeChest(3, 1, 3); b.placeChest(-3, 1, -3); b.setBossSpawn(0, 1, 4);
    }

    // ── NETHER (original — compact) ──

    private void buildCrimsonCitadel(StructureBuilder b) {
        b.fillWalls(-15, 0, -15, 15, 12, 15, Material.NETHER_BRICKS); b.hollowBox(-15, 1, -15, 15, 12, 15);
        b.fillFloor(-14, -14, 14, 14, 0, Material.CRIMSON_PLANKS);
        b.fillBox(-2, 1, 10, 2, 3, 12, Material.CRIMSON_PLANKS); b.setBlock(0, 2, 11, Material.GOLD_BLOCK);
        b.placeChest(0, 1, 0); b.placeChest(10, 1, -8); b.setBossSpawn(0, 2, 8);
    }

    private void buildSoulSanctum(StructureBuilder b) {
        b.fillWalls(-10, 0, -10, 10, 8, 10, Material.SOUL_SAND); b.hollowBox(-10, 1, -10, 10, 8, 10);
        b.scatter(-10, 0, -10, 10, 8, 10, Material.SOUL_SAND, Material.SOUL_SOIL, 0.3);
        b.setBlock(-5, 3, -5, Material.SOUL_LANTERN); b.setBlock(5, 3, 5, Material.SOUL_LANTERN);
        b.setBlock(0, 1, 0, Material.SOUL_CAMPFIRE);
        b.placeChest(3, 1, 3); b.setBossSpawn(0, 1, 4);
    }

    private void buildBasaltSpire(StructureBuilder b) {
        for (int y = 0; y < 40; y++) { int r = Math.max(2, 8-(y*6/40)); b.filledCircle(0, y, 0, r, Material.BASALT); }
        for (int y = 1; y < 35; y++) {
            double a = y * 15; int sx = (int)(3*Math.cos(Math.toRadians(a))); int sz = (int)(3*Math.sin(Math.toRadians(a)));
            b.setBlock(sx, y, sz, Material.AIR); b.setBlock(sx, y+1, sz, Material.AIR);
        }
        b.scatter(-8, 0, -8, 8, 40, 8, Material.BASALT, Material.MAGMA_BLOCK, 0.05);
        b.placeChest(0, 20, 0); b.setBossSpawn(0, 35, 0);
    }

    private void buildNetherDungeon(StructureBuilder b) {
        b.fillWalls(-12, 0, -8, 12, 6, 8, Material.NETHER_BRICKS); b.hollowBox(-12, 1, -8, 12, 6, 8);
        b.fillBox(-3, 1, -3, 3, 1, 3, Material.NETHER_BRICKS);
        b.placeChest(-10, 1, 0); b.placeChest(10, 1, 0); b.setBossSpawn(0, 2, 0);
    }

    private void buildPiglinPalace(StructureBuilder b) {
        b.fillWalls(-20, 0, -20, 20, 10, 20, Material.NETHER_BRICKS); b.hollowBox(-20, 1, -20, 20, 10, 20);
        b.fillFloor(-19, -19, 19, 19, 0, Material.GOLD_BLOCK);
        b.fillBox(-2, 1, 15, 2, 4, 18, Material.GOLD_BLOCK);
        b.fillBox(12, 1, 12, 18, 4, 18, Material.GOLD_BLOCK); b.fillBox(13, 1, 13, 17, 3, 17, Material.AIR);
        b.placeChest(15, 1, 15); b.placeChest(-15, 1, -15); b.setBossSpawn(0, 1, 12);
    }

    // ── NEW NETHER (Plus Part 2) ──

    private void buildWitherSanctum(StructureBuilder b) {
        // Dark temple with soul fire and wither skull motifs
        b.fillWalls(-18, 0, -18, 18, 15, 18, Material.NETHER_BRICKS); b.hollowBox(-18, 1, -18, 18, 15, 18);
        b.fillFloor(-17, -17, 17, 17, 0, Material.SOUL_SOIL);
        // Central altar with soul fire
        b.fillBox(-3, 1, -3, 3, 1, 3, Material.BLACKSTONE);
        b.setBlock(0, 2, 0, Material.SOUL_CAMPFIRE);
        b.setBlock(-2, 2, -2, Material.SOUL_LANTERN); b.setBlock(2, 2, 2, Material.SOUL_LANTERN);
        b.setBlock(2, 2, -2, Material.SOUL_LANTERN); b.setBlock(-2, 2, 2, Material.SOUL_LANTERN);
        // Wither skull pillars (4 corners)
        for (int[] c : new int[][]{{-12,-12},{12,-12},{-12,12},{12,12}}) {
            b.pillar(c[0], 1, 10, c[1], Material.POLISHED_BLACKSTONE_BRICKS);
            b.setBlock(c[0], 11, c[1], Material.WITHER_SKELETON_SKULL);
        }
        // Side chambers
        b.fillBox(-18, 1, -5, -22, 8, 5, Material.AIR); b.fillWalls(-22, 0, -5, -18, 8, 5, Material.NETHER_BRICKS);
        b.fillBox(18, 1, -5, 22, 8, 5, Material.AIR); b.fillWalls(18, 0, -5, 22, 8, 5, Material.NETHER_BRICKS);
        // Dark atmosphere
        b.scatter(-17, 0, -17, 17, 0, 17, Material.SOUL_SOIL, Material.SOUL_SAND, 0.2);
        b.placeChest(-20, 1, 0); b.placeChest(20, 1, 0); b.placeChest(0, 1, 12);
        b.setBossSpawn(0, 2, 5);
    }

    private void buildBlazeColosseum(StructureBuilder b) {
        // Circular arena with magma and nether brick stands
        b.filledCircle(0, 0, 0, 20, Material.MAGMA_BLOCK);
        b.filledCircle(0, 0, 0, 16, Material.NETHER_BRICKS);
        // Spectator tiers
        for (int i = 0; i < 4; i++) b.circle(0, i + 1, 0, 18 + i, Material.NETHER_BRICKS);
        // Pillars of fire (lava on top)
        for (int a = 0; a < 360; a += 60) {
            int px = (int)(14*Math.cos(Math.toRadians(a))); int pz = (int)(14*Math.sin(Math.toRadians(a)));
            b.pillar(px, 1, 6, pz, Material.NETHER_BRICK_FENCE);
            b.setBlock(px, 7, pz, Material.LAVA);
        }
        // Central fighting pit
        b.filledCircle(0, 0, 0, 8, Material.BLACKSTONE);
        // Chain decorations
        b.setBlock(-6, 5, 0, Material.IRON_CHAIN); b.setBlock(6, 5, 0, Material.IRON_CHAIN);
        b.setBlock(0, 5, -6, Material.IRON_CHAIN); b.setBlock(0, 5, 6, Material.IRON_CHAIN);
        b.placeChest(5, 1, 5); b.placeChest(-5, 1, -5);
        b.setBossSpawn(0, 1, 0);
    }

    // ── END (original — compact) ──

    private void buildVoidShrine(StructureBuilder b) {
        b.filledCircle(0, 0, 0, 10, Material.OBSIDIAN);
        b.filledCircle(0, 1, 0, 8, Material.PURPUR_BLOCK);
        for (int a = 0; a < 360; a += 60) {
            int px = (int)(8*Math.cos(Math.toRadians(a))); int pz = (int)(8*Math.sin(Math.toRadians(a)));
            b.pillar(px, 1, 8, pz, Material.PURPUR_PILLAR); b.setBlock(px, 9, pz, Material.END_ROD);
        }
        b.setBlock(0, 2, 0, Material.OBSIDIAN); b.setBlock(0, 3, 0, Material.ENDER_CHEST);
        b.placeChest(3, 2, 0); b.placeChest(-3, 2, 0); b.setBossSpawn(0, 2, 5);
    }

    private void buildEnderMonastery(StructureBuilder b) {
        b.fillWalls(-15, 0, -10, 15, 10, 10, Material.PURPUR_BLOCK); b.hollowBox(-15, 1, -10, 15, 10, 10);
        b.fillFloor(-14, -9, 14, 9, 0, Material.END_STONE_BRICKS);
        for (int x = -12; x <= 12; x += 4) b.pillar(x, 1, 4, -8, Material.BOOKSHELF);
        b.pillar(-8, 1, 8, 0, Material.PURPUR_PILLAR); b.pillar(8, 1, 8, 0, Material.PURPUR_PILLAR);
        b.setBlock(0, 9, 0, Material.END_ROD);
        b.placeChest(5, 1, 5); b.placeChest(-5, 1, -5); b.setBossSpawn(0, 1, 0);
    }

    private void buildDragonsHoard(StructureBuilder b) {
        b.filledCircle(0, 0, 0, 8, Material.GOLD_BLOCK);
        b.filledCircle(0, 1, 0, 6, Material.GOLD_BLOCK);
        b.filledCircle(0, 2, 0, 4, Material.GOLD_BLOCK);
        b.filledCircle(0, 3, 0, 2, Material.GOLD_BLOCK);
        b.placeChest(0, 4, 0); b.placeChest(3, 2, 3); b.placeChest(-3, 2, -3);
    }

    // ── NEW ABYSS STRUCTURES (Plus Part 2) ──

    private void buildAbyssalCastle(StructureBuilder b) {
        // Massive castle: deepslate + end stone + crying obsidian + purple glass
        // Outer walls
        b.fillWalls(-50, 0, -50, 50, 20, 50, Material.DEEPSLATE_BRICKS); b.hollowBox(-50, 1, -50, 50, 20, 50);
        b.fillFloor(-49, -49, 49, 49, 0, Material.DEEPSLATE_TILES);
        // Corner towers
        for (int[] c : new int[][]{{-50,-50},{50,-50},{-50,50},{50,50}}) {
            b.fillBox(c[0]-4, 0, c[1]-4, c[0]+4, 30, c[1]+4, Material.DEEPSLATE_BRICKS);
            b.fillBox(c[0]-3, 1, c[1]-3, c[0]+3, 30, c[1]+3, Material.AIR);
            b.setBlock(c[0], 31, c[1], Material.END_ROD);
        }
        // Main gate
        b.fillBox(-4, 1, -50, 4, 10, -50, Material.AIR);
        // Keep (central)
        b.fillWalls(-18, 0, -18, 18, 30, 18, Material.END_STONE_BRICKS); b.hollowBox(-18, 1, -18, 18, 30, 18);
        b.fillFloor(-17, -17, 17, 17, 0, Material.CRYING_OBSIDIAN);
        // Throne room
        b.fillBox(-3, 1, 12, 3, 5, 16, Material.CRYING_OBSIDIAN);
        b.setBlock(0, 2, 14, Material.GOLD_BLOCK); b.setBlock(0, 3, 14, Material.GOLD_BLOCK);
        // Purple glass windows
        for (int y = 5; y <= 25; y += 5) {
            b.setBlock(18, y, 0, Material.PURPLE_STAINED_GLASS); b.setBlock(-18, y, 0, Material.PURPLE_STAINED_GLASS);
            b.setBlock(0, y, 18, Material.PURPLE_STAINED_GLASS); b.setBlock(0, y, -18, Material.PURPLE_STAINED_GLASS);
        }
        // Armory wing
        b.fillBox(20, 1, -15, 35, 8, -5, Material.AIR); b.fillWalls(20, 0, -15, 35, 8, -5, Material.DEEPSLATE_BRICKS);
        // Treasury wing
        b.fillBox(-35, 1, 5, -20, 8, 15, Material.AIR); b.fillWalls(-35, 0, 5, -20, 8, 15, Material.DEEPSLATE_BRICKS);
        // Library wing
        b.fillBox(20, 1, 5, 35, 8, 15, Material.AIR); b.fillWalls(20, 0, 5, 35, 8, 15, Material.DEEPSLATE_BRICKS);
        for (int x = 22; x <= 33; x += 3) b.pillar(x, 1, 4, 8, Material.BOOKSHELF);
        // Arena (boss room) — circular chamber beneath keep
        b.filledCircle(0, -5, 0, 18, Material.DEEPSLATE_BRICKS);
        b.filledCircle(0, -4, 0, 16, Material.AIR); b.filledCircle(0, -3, 0, 16, Material.AIR);
        b.filledCircle(0, -2, 0, 16, Material.AIR); b.filledCircle(0, -1, 0, 16, Material.AIR);
        b.filledCircle(0, -5, 0, 16, Material.OBSIDIAN);
        // Chests scattered through wings
        b.placeChest(0, 1, 0); b.placeChest(25, 1, -10); b.placeChest(-30, 1, 10);
        b.placeChest(25, 1, 10); b.placeChest(0, -4, 10);
        b.setBossSpawn(0, -4, 0);
    }

    private void buildVoidNexus(StructureBuilder b) {
        // Floating obsidian-and-end-stone nexus with energy conduits
        b.filledCircle(0, 0, 0, 12, Material.OBSIDIAN);
        b.filledCircle(0, 1, 0, 10, Material.END_STONE);
        b.filledCircle(0, 2, 0, 8, Material.END_STONE);
        // Central conduit tower
        b.pillar(0, 3, 20, 0, Material.PURPUR_PILLAR);
        b.setBlock(0, 21, 0, Material.END_ROD); b.setBlock(0, 22, 0, Material.END_ROD);
        // Ring of energy pillars
        for (int a = 0; a < 360; a += 45) {
            int px = (int)(9*Math.cos(Math.toRadians(a))); int pz = (int)(9*Math.sin(Math.toRadians(a)));
            b.pillar(px, 1, 12, pz, Material.OBSIDIAN);
            b.setBlock(px, 13, pz, Material.END_ROD);
            // Connecting beams at mid height
            b.setBlock(px/2, 7, pz/2, Material.CRYING_OBSIDIAN);
        }
        // Inner chamber (below main platform)
        b.filledCircle(0, -1, 0, 6, Material.OBSIDIAN);
        b.filledCircle(0, -2, 0, 6, Material.AIR);
        b.filledCircle(0, -3, 0, 6, Material.AIR);
        b.filledCircle(0, -4, 0, 6, Material.OBSIDIAN);
        b.placeChest(4, 1, 0); b.placeChest(-4, 1, 0); b.placeChest(0, -3, 0);
        b.setBossSpawn(0, 2, 5);
    }

    private void buildShatteredCathedral(StructureBuilder b) {
        // Broken cathedral — once grand, now shattered in the Abyss void
        // Nave (main hall)
        b.fillWalls(-8, 0, -20, 8, 18, 20, Material.END_STONE_BRICKS); b.hollowBox(-8, 1, -20, 8, 18, 20);
        b.fillFloor(-7, -19, 7, 19, 0, Material.DEEPSLATE_TILES);
        // Arched ceiling (approximated)
        for (int z = -19; z <= 19; z++) {
            b.setBlock(-4, 15, z, Material.END_STONE_BRICKS); b.setBlock(4, 15, z, Material.END_STONE_BRICKS);
            b.setBlock(-2, 17, z, Material.END_STONE_BRICKS); b.setBlock(2, 17, z, Material.END_STONE_BRICKS);
            b.setBlock(0, 18, z, Material.END_STONE_BRICKS);
        }
        // Stained glass windows (purple)
        for (int y = 5; y <= 14; y += 3) {
            b.setBlock(8, y, -10, Material.PURPLE_STAINED_GLASS); b.setBlock(-8, y, -10, Material.PURPLE_STAINED_GLASS);
            b.setBlock(8, y, 10, Material.PURPLE_STAINED_GLASS); b.setBlock(-8, y, 10, Material.PURPLE_STAINED_GLASS);
        }
        // Broken sections (decay)
        b.decay(-8, 10, -20, 8, 18, -10, 0.3); // Front section more damaged
        b.decay(-8, 1, 15, 8, 18, 20, 0.25);    // Back section also damaged
        // Altar at the far end
        b.fillBox(-2, 1, 16, 2, 1, 18, Material.CRYING_OBSIDIAN);
        b.setBlock(0, 2, 17, Material.BEACON); b.setBlock(0, 3, 17, Material.END_ROD);
        // Pews (damaged)
        for (int z = -15; z <= 10; z += 3) {
            b.setBlock(-5, 1, z, Material.PURPUR_STAIRS); b.setBlock(-4, 1, z, Material.PURPUR_STAIRS);
            b.setBlock(4, 1, z, Material.PURPUR_STAIRS); b.setBlock(5, 1, z, Material.PURPUR_STAIRS);
        }
        b.decay(-6, 1, -15, 6, 1, 10, 0.15); // Some pews destroyed
        // Bell tower (partially collapsed)
        b.fillBox(-3, 0, -24, 3, 25, -20, Material.END_STONE_BRICKS);
        b.fillBox(-2, 1, -23, 2, 25, -21, Material.AIR);
        b.setBlock(0, 20, -22, Material.BELL);
        b.decay(-3, 15, -24, 3, 25, -20, 0.35); // Top is very damaged
        // Crying obsidian tears from ceiling
        b.scatter(-7, 16, -19, 7, 18, 19, Material.END_STONE_BRICKS, Material.CRYING_OBSIDIAN, 0.08);
        b.placeChest(0, 1, 17); b.placeChest(-6, 1, 0); b.placeChest(6, 1, -12);
        b.setBossSpawn(0, 1, 12);
    }

    // ── GENERIC FALLBACK ──

    private void buildGenericStructure(StructureBuilder b, StructureType type) {
        int hx = type.getSizeX() / 2; int hz = type.getSizeZ() / 2;
        b.fillBox(-hx, 0, -hz, hx, 0, hz, Material.STONE_BRICKS);
        b.fillWalls(-hx, 1, -hz, hx, 4, hz, Material.STONE_BRICKS); b.hollowBox(-hx, 1, -hz, hx, 4, hz);
        b.setBlock(0, 1, -hz, Material.AIR); b.setBlock(0, 2, -hz, Material.AIR);
        b.placeChest(0, 1, 0);
        if (type.hasBoss()) b.setBossSpawn(0, 1, hz/2);
    }

    // ── UTILITY METHODS ──

    private boolean checkSpacing(String worldName, int x, int z) {
        List<long[]> structures = generatedStructures.computeIfAbsent(worldName, k -> new ArrayList<>());
        for (long[] pos : structures) {
            double dx = pos[0] - x; double dz = pos[1] - z;
            if (dx * dx + dz * dz < MIN_SPACING * MIN_SPACING) return false;
        }
        return true;
    }

    private void recordStructure(String worldName, int x, int z) {
        generatedStructures.computeIfAbsent(worldName, k -> new ArrayList<>()).add(new long[]{x, z});
    }

    private int findNetherFloor(World world, int x, int z) {
        for (int y = 40; y < 100; y++) {
            if (!world.getBlockAt(x, y, z).getType().isAir() && world.getBlockAt(x, y + 1, z).getType().isAir()
                    && world.getBlockAt(x, y + 2, z).getType().isAir()) return y + 1;
        }
        return -1;
    }

    private int findOceanFloor(World world, int x, int z) {
        for (int y = world.getHighestBlockYAt(x, z); y > world.getMinHeight(); y--) {
            Material mat = world.getBlockAt(x, y, z).getType();
            if (mat != Material.WATER && mat != Material.KELP_PLANT && mat != Material.SEAGRASS && !mat.isAir()) return y;
        }
        return -1;
    }

    public StructureBossManager getBossManager() { return bossManager; }
    public StructureLootPopulator getLootPopulator() { return lootPopulator; }
}
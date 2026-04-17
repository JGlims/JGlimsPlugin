# Schematic Design Pattern Analysis

Analysis of 24 imported schematics for use as JGlimsPlugin structure references.

## Block Palettes by Theme

### Medieval (Tavern, Castle, Mine, Motte & Bailey)
- **Primary walls**: cobblestone, stone_bricks, mossy_stone_bricks
- **Framing**: oak_log, spruce_log (structural beams)
- **Floors/fill**: oak_planks, spruce_planks, dirt
- **Accents**: glass_pane, oak_fence, oak_trapdoor, white_wool
- **Roofing**: cobblestone_stairs, oak_stairs, stone_brick_stairs

### Desert (Pyramid, City, House, Aztec/Maya)
- **Primary walls**: sandstone, chiseled_sandstone, cut_sandstone
- **Accents**: terracotta (orange, yellow, brown), gold_block (sparingly)
- **Fill**: sand, dirt
- **Roofing**: sandstone_stairs, sandstone_slab

### Sacred (Cathedral, Church, Tower of Gods)
- **Primary walls**: stone_bricks, polished_andesite, quartz_block
- **Pillars**: quartz_pillar, stone_brick_stairs (stacked)
- **Glass**: white_stained_glass, stained_glass_pane
- **Accents**: smooth_stone_slab, chiseled_quartz_block
- **Floors**: polished_andesite, smooth_stone

### Asian (Pagoda, Japanese Temple)
- **Primary structure**: oak_planks, dark_oak_planks
- **Eaves/railings**: oak_fence, dark_oak_fence (dense use)
- **Roofing**: oak_stairs (layered, with overhang)
- **Pillars**: oak_log, dark_oak_log
- **Floors**: oak_slab, smooth_stone_slab

### Maritime (Black Pearl, Pirate Ship)
- **Hull**: spruce_planks, dark_oak_planks
- **Sails**: black_wool, white_wool
- **Deck details**: spruce_fence, spruce_stairs
- **Rigging**: coal_block, black_concrete
- **Accents**: gold_block, lantern

## Wall Depth Techniques

1. **Single-layer flat walls**: Small structures (Tavern, Cave House). Simple `setBlock` grid.
2. **Double-layer with insets**: Medium structures. Outer wall of stone_bricks, inner of planks, 1-block deep window insets using glass_pane.
3. **Buttressed walls**: Cathedrals, castles. Stone_brick_stairs placed as decorative supports at regular intervals (every 4-5 blocks).
4. **Gradient walls**: Tower of Gods uses quartz variants — quartz_block base transitioning to chiseled_quartz_block and quartz_pillar.

## Roof Construction Patterns

1. **A-frame**: Stairs placed in opposing directions, narrowing by 1 block per Y level. Most common in medieval buildings.
2. **Tiered/pagoda**: Multiple stacked roof sections, each smaller than the one below. 2-3 block overhang per tier using slabs and stairs.
3. **Flat with parapet**: Desert style. Flat slab roof surrounded by 1-block-high wall (sandstone_slab, sandstone_wall).
4. **Dome approximation**: Cathedral style. Concentric rings of stairs getting progressively smaller. Typically uses stone_brick_stairs.

## Interior Furnishing Patterns

- **Libraries**: bookshelf walls (3 high), crafting_table, lectern
- **Taverns**: oak_stairs as seating, oak_slab tables, barrel, cauldron
- **Temples**: chests at altar positions, enchanting_table, ender_chest
- **Ships**: chest in captain's cabin, furnace, barrel storage below deck
- **Mines**: rail, torch/lantern placement, chest at dead ends

## Decorative Details

- **Window frames**: trapdoor (open) flanking glass_pane
- **Flower boxes**: trapdoor (open) with flower/potted_plant behind
- **Chandeliers**: fence + lantern or fence + chain + lantern
- **Path borders**: stone_slab (half) flanking grass_path/dirt_path
- **Archways**: stairs (upside-down) as arch curves
- **Columns**: log with trapdoor trim at top/bottom
- **Crenellations**: alternating full-block and air on wall tops (stone_brick_wall or stone_bricks with gaps)

## Key Takeaways for Manual Structure Building

1. **Material variety matters**: The best schematics use 25-57 unique block types. Our weakest existing structures use 5-10. Aim for 15+ per structure.
2. **Depth is cheap**: Even adding stairs as trim on flat walls dramatically improves visual quality at minimal block cost.
3. **Consistent palette**: Each structure sticks to 3-4 primary materials (walls, frame, accent, roof). Mixing too many palettes looks incoherent.
4. **Vertical interest**: Add variation every 4-5 blocks vertically — pillar caps, window rows, balconies, eave overhangs.
5. **Ground integration**: Surround structures with 2-3 blocks of grass_block, dirt_path, or coarse_dirt to blend with terrain.

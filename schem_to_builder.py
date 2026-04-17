"""Convert .schem / .schematic files to JGlimsPlugin IStructureBuilder Java classes.

Usage:
  # Single file
  python3 schem_to_builder.py schematics/witch_cottage.schem WitchHouseForest

  # Batch mode — mapping.txt lines like 'witch_cottage.schem -> WitchHouseForest'
  python3 schem_to_builder.py --batch schematics/ mapping.txt

Dependencies: pip install nbtlib
"""
import os, re, sys
from collections import defaultdict

try:
    from nbtlib import load as nbt_load
except ImportError:
    print("ERROR: nbtlib is required. Install with: pip install nbtlib")
    sys.exit(1)

OUT_DIR = os.path.join("src", "main", "java", "com", "jglims", "plugin",
                      "structures", "builders")

SKIP_BLOCKS = {"minecraft:air", "minecraft:cave_air", "minecraft:void_air"}
LIQUID_BLOCKS = {"minecraft:water", "minecraft:lava", "minecraft:flowing_water", "minecraft:flowing_lava"}

# Bukkit Material renames (old Minecraft name → current Paper Material name)
MATERIAL_RENAMES = {
    "GRASS": "SHORT_GRASS", "GRASS_PATH": "DIRT_PATH",
    "SIGN": "OAK_SIGN", "WALL_SIGN": "OAK_WALL_SIGN",
    "WOODEN_DOOR": "OAK_DOOR", "WOODEN_SLAB": "OAK_SLAB",
    "STONE_SLAB": "SMOOTH_STONE_SLAB", "DOUBLE_STONE_SLAB": "SMOOTH_STONE",
    "LONG_GRASS": "SHORT_GRASS", "YELLOW_FLOWER": "DANDELION",
    "RED_ROSE": "POPPY", "CONCRETE": "WHITE_CONCRETE",
    "STAINED_CLAY": "WHITE_TERRACOTTA",
    "WOOL": "WHITE_WOOL",
    "BED_BLOCK": "RED_BED", "BED": "RED_BED",
    "CROPS": "WHEAT", "BURNING_FURNACE": "FURNACE",
    "WOOD": "OAK_PLANKS", "LOG": "OAK_LOG",
    "LEAVES": "OAK_LEAVES",
    "STEP": "SMOOTH_STONE_SLAB", "DOUBLE_STEP": "SMOOTH_STONE",
    "COBBLE_WALL": "COBBLESTONE_WALL",
    "SUGAR_CANE_BLOCK": "SUGAR_CANE",
    "REDSTONE_TORCH_ON": "REDSTONE_TORCH",
    "REDSTONE_TORCH_OFF": "REDSTONE_TORCH",
    "SKULL": "SKELETON_SKULL",
    "PISTON_BASE": "PISTON", "PISTON_STICKY_BASE": "STICKY_PISTON",
    "DIODE_BLOCK_ON": "REPEATER", "DIODE_BLOCK_OFF": "REPEATER",
    "IRON_FENCE": "IRON_BARS",
    "THIN_GLASS": "GLASS_PANE",
    "NETHER_FENCE": "NETHER_BRICK_FENCE",
    "FENCE": "OAK_FENCE", "FENCE_GATE": "OAK_FENCE_GATE",
    "TRAP_DOOR": "OAK_TRAPDOOR",
    "SMOOTH_BRICK": "STONE_BRICKS",
    "ENDER_PORTAL": "END_PORTAL",
    "ENDER_PORTAL_FRAME": "END_PORTAL_FRAME",
    "ENDER_STONE": "END_STONE",
    "WOOD_BUTTON": "OAK_BUTTON",
    "WOOD_PLATE": "OAK_PRESSURE_PLATE",
    "WORKBENCH": "CRAFTING_TABLE",
    "CLAY_BRICK": "BRICKS",
    "PEONY[HALF=UPPER]": "PEONY",
    "PEONY[HALF=LOWER]": "PEONY",
    "ROSE_BUSH[HALF=UPPER]": "ROSE_BUSH",
    "ROSE_BUSH[HALF=LOWER]": "ROSE_BUSH",
    "LILAC[HALF=UPPER]": "LILAC",
    "LILAC[HALF=LOWER]": "LILAC",
    "SUNFLOWER[HALF=UPPER]": "SUNFLOWER",
    "SUNFLOWER[HALF=LOWER]": "SUNFLOWER",
    "TALL_GRASS[HALF=UPPER]": "TALL_GRASS",
    "TALL_GRASS[HALF=LOWER]": "TALL_GRASS",
    "LARGE_FERN[HALF=UPPER]": "LARGE_FERN",
    "LARGE_FERN[HALF=LOWER]": "LARGE_FERN",
}
MAX_METHOD_LINES = 2000

# Block ID → (Material enum, emission kind). Kinds: SOLID, STAIRS, SLAB, DIRECTIONAL, CHEST.
# We default to SOLID with the uppercased short name as Material.
# STAIRS/SLAB/DIRECTIONAL are inferred from the block name suffix.
STAIR_SUFFIX = "_stairs"
SLAB_SUFFIX = "_slab"
DIRECTIONAL_PREFIXES = ("minecraft:",)  # all base
DIRECTIONAL_TYPES = {
    "torch_wall", "wall_torch", "lantern", "chain", "barrel", "lectern",
    "furnace", "blast_furnace", "smoker", "dispenser", "dropper",
    "observer", "piston", "sticky_piston", "grindstone", "loom",
    "campfire", "soul_campfire", "ender_chest",
}

# Bukkit BlockFace vs Minecraft facing names
FACE_MAP = {
    "north": "NORTH", "south": "SOUTH", "east": "EAST", "west": "WEST",
    "up": "UP", "down": "DOWN",
}


# ── SCHEMATIC PARSERS ──────────────────────────────────────────────────────

def _decode_varint_palette(blockdata_bytes, width, height, length, palette):
    """Decode a Sponge-schematic block array. Returns list[(x,y,z,id_str,props)]."""
    out = []
    # Invert palette: index -> blockstate string
    id_to_state = {int(v): str(k) for k, v in palette.items()}
    # Parse varints
    i = 0
    blocks = []
    data = blockdata_bytes if isinstance(blockdata_bytes, (bytes, bytearray, list)) else list(blockdata_bytes)
    # nbtlib might give us a list of ints or a ByteArray
    data = list(data)
    while i < len(data):
        value = 0
        shift = 0
        while True:
            b = data[i] & 0xFF
            i += 1
            value |= (b & 0x7F) << shift
            if (b & 0x80) == 0:
                break
            shift += 7
        blocks.append(value)

    for index, pid in enumerate(blocks):
        y = index // (width * length)
        z = (index % (width * length)) // width
        x = (index % (width * length)) % width
        state = id_to_state.get(pid)
        if state is None:
            continue
        # Parse "minecraft:name[prop=val,prop=val]"
        if "[" in state:
            name, propstr = state.split("[", 1)
            propstr = propstr.rstrip("]")
            props = {}
            for kv in propstr.split(","):
                if "=" in kv:
                    k, v = kv.split("=", 1)
                    props[k.strip()] = v.strip()
        else:
            name = state
            props = {}
        out.append((x, y, z, name.strip(), props))
    return out


def _parse_blockstate(state_str):
    """Parse 'minecraft:name[prop=val,prop=val]' → (name, props_dict)."""
    if "[" in state_str:
        name, propstr = state_str.split("[", 1)
        propstr = propstr.rstrip("]")
        props = {}
        for kv in propstr.split(","):
            if "=" in kv:
                k, v = kv.split("=", 1)
                props[k.strip()] = v.strip()
        return name.strip(), props
    return state_str.strip(), {}


def parse_schem(path):
    """Returns (dims, blocks).
    dims = (width, height, length)
    blocks = list of (x, y, z, id_str, props_dict)
    """
    nbt = nbt_load(path)
    root = nbt
    if "Schematic" in root:
        s = root["Schematic"]
        width = int(s["Width"])
        height = int(s["Height"])
        length = int(s["Length"])
        blocks_container = s.get("Blocks") or {}
        palette = blocks_container.get("Palette") or s.get("Palette") or {}
        bd = blocks_container.get("Data")
        blockdata = bd if bd is not None else (s["BlockData"] if "BlockData" in s else [])
    else:
        s = root
        if "" in s: s = s[""]
        width = int(s["Width"])
        height = int(s["Height"])
        length = int(s["Length"])
        palette = s.get("Palette") or {}
        blockdata = s["BlockData"] if "BlockData" in s else []
    blocks = _decode_varint_palette(blockdata, width, height, length, palette)
    return (width, height, length), blocks


def parse_we_palette_schematic(path):
    """Parse WorldEdit .schematic with Palette + byte-indexed Blocks array."""
    nbt = nbt_load(path)
    s = nbt[""] if "" in nbt else nbt
    width = int(s["Width"]); height = int(s["Height"]); length = int(s["Length"])
    if width == 0 or height == 0 or length == 0:
        return (width, height, length), []
    palette = s.get("Palette") or {}
    block_ids = list(s.get("Blocks", []))
    id_to_state = {int(v): str(k) for k, v in palette.items()}
    blocks = []
    for i, bid in enumerate(block_ids):
        idx = bid & 0xFF
        state = id_to_state.get(idx)
        if state is None or state == "minecraft:air":
            continue
        y = i // (width * length)
        z = (i // width) % length
        x = i % width
        name, props = _parse_blockstate(state)
        blocks.append((x, y, z, name, props))
    return (width, height, length), blocks


_WOOD_TYPES = ["oak", "spruce", "birch", "jungle", "acacia", "dark_oak"]
_COLOR_NAMES = [
    "white", "orange", "magenta", "light_blue", "yellow", "lime", "pink",
    "gray", "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black",
]
_STAIR_FACING = {0: "east", 1: "west", 2: "south", 3: "north"}

# Legacy block ID → modern block name. Data-value variants handled in code below.
_LEGACY_IDS = {
    0: None,  # air
    1: "stone", 2: "grass_block", 3: "dirt", 4: "cobblestone",
    5: "oak_planks", 6: "oak_sapling", 7: "bedrock",
    8: "water", 9: "water", 10: "lava", 11: "lava",
    12: "sand", 13: "gravel", 14: "gold_ore", 15: "iron_ore",
    16: "coal_ore", 17: "oak_log", 18: "oak_leaves",
    19: "sponge", 20: "glass", 21: "lapis_ore", 22: "lapis_block",
    23: "dispenser", 24: "sandstone", 25: "note_block",
    26: "red_bed", 27: "powered_rail", 28: "detector_rail",
    29: "sticky_piston", 30: "cobweb", 31: "short_grass",
    32: "dead_bush", 33: "piston", 34: "piston_head",
    35: "white_wool", 37: "dandelion", 38: "poppy",
    39: "brown_mushroom", 40: "red_mushroom",
    41: "gold_block", 42: "iron_block", 43: "smooth_stone_slab",
    44: "smooth_stone_slab", 45: "bricks", 46: "tnt",
    47: "bookshelf", 48: "mossy_cobblestone", 49: "obsidian",
    50: "wall_torch", 51: "fire", 52: "spawner",
    53: "oak_stairs", 54: "chest", 55: "redstone_wire",
    56: "diamond_ore", 57: "diamond_block", 58: "crafting_table",
    59: "wheat", 60: "farmland", 61: "furnace", 62: "furnace",
    63: "oak_sign", 64: "oak_door", 65: "ladder",
    66: "rail", 67: "cobblestone_stairs",
    68: "oak_wall_sign", 69: "lever",
    70: "stone_pressure_plate", 71: "iron_door",
    72: "oak_pressure_plate", 73: "redstone_ore", 74: "redstone_ore",
    75: "redstone_wall_torch", 76: "redstone_wall_torch",
    77: "stone_button", 78: "snow", 79: "ice",
    80: "snow_block", 81: "cactus", 82: "clay",
    83: "sugar_cane", 84: "jukebox", 85: "oak_fence",
    86: "carved_pumpkin", 87: "netherrack", 88: "soul_sand",
    89: "glowstone", 90: "nether_portal", 91: "jack_o_lantern",
    92: "cake", 93: "repeater", 94: "repeater",
    95: "white_stained_glass", 96: "oak_trapdoor",
    97: "infested_stone", 98: "stone_bricks",
    99: "brown_mushroom_block", 100: "red_mushroom_block",
    101: "iron_bars", 102: "glass_pane", 103: "melon",
    104: "pumpkin_stem", 105: "melon_stem", 106: "vine",
    107: "oak_fence_gate",
    108: "brick_stairs", 109: "stone_brick_stairs",
    110: "mycelium", 111: "lily_pad", 112: "nether_bricks",
    113: "nether_brick_fence", 114: "nether_brick_stairs",
    115: "nether_wart", 116: "enchanting_table",
    117: "brewing_stand", 118: "cauldron",
    119: "end_portal", 120: "end_portal_frame",
    121: "end_stone", 122: "dragon_egg",
    123: "redstone_lamp", 124: "redstone_lamp",
    125: "oak_planks", 126: "oak_slab",
    127: "cocoa", 128: "sandstone_stairs",
    129: "emerald_ore", 130: "ender_chest",
    131: "tripwire_hook", 132: "tripwire",
    133: "emerald_block",
    134: "spruce_stairs", 135: "birch_stairs", 136: "jungle_stairs",
    137: "command_block", 138: "beacon",
    139: "cobblestone_wall", 140: "flower_pot",
    141: "carrots", 142: "potatoes",
    143: "oak_button", 144: "skeleton_skull",
    145: "anvil", 146: "trapped_chest",
    147: "light_weighted_pressure_plate", 148: "heavy_weighted_pressure_plate",
    149: "comparator", 150: "comparator",
    151: "daylight_detector", 152: "redstone_block",
    153: "nether_quartz_ore", 154: "hopper",
    155: "quartz_block", 156: "quartz_stairs",
    157: "activator_rail", 158: "dropper",
    159: "white_terracotta", 160: "white_stained_glass_pane",
    161: "acacia_leaves", 162: "acacia_log",
    163: "acacia_stairs", 164: "dark_oak_stairs",
    165: "slime_block", 166: "barrier",
    167: "iron_trapdoor", 168: "prismarine",
    169: "sea_lantern", 170: "hay_block",
    171: "white_carpet", 172: "terracotta",
    173: "coal_block", 174: "packed_ice",
    175: "sunflower",
    176: "white_banner", 177: "white_wall_banner",
    178: "daylight_detector",
    179: "red_sandstone", 180: "red_sandstone_stairs",
    181: "red_sandstone_slab", 182: "red_sandstone_slab",
    183: "spruce_fence_gate", 184: "birch_fence_gate",
    185: "jungle_fence_gate", 186: "dark_oak_fence_gate",
    187: "acacia_fence_gate",
    188: "spruce_fence", 189: "birch_fence",
    190: "jungle_fence", 191: "dark_oak_fence",
    192: "acacia_fence",
    193: "spruce_door", 194: "birch_door",
    195: "jungle_door", 196: "acacia_door", 197: "dark_oak_door",
    198: "end_rod", 199: "chorus_plant", 200: "chorus_flower",
    201: "purpur_block", 202: "purpur_pillar",
    203: "purpur_stairs", 204: "purpur_slab", 205: "purpur_slab",
    206: "end_stone_bricks",
    208: "grass_path", 210: "repeating_command_block",
    211: "chain_command_block",
    213: "magma_block", 214: "nether_wart_block",
    215: "red_nether_bricks", 216: "bone_block",
    218: "observer", 219: "white_shulker_box",
    235: "white_glazed_terracotta", 236: "orange_glazed_terracotta",
    237: "magenta_glazed_terracotta", 238: "light_blue_glazed_terracotta",
    239: "yellow_glazed_terracotta", 240: "lime_glazed_terracotta",
    241: "pink_glazed_terracotta", 242: "gray_glazed_terracotta",
    243: "light_gray_glazed_terracotta", 244: "cyan_glazed_terracotta",
    245: "purple_glazed_terracotta", 246: "blue_glazed_terracotta",
    247: "brown_glazed_terracotta", 248: "green_glazed_terracotta",
    249: "red_glazed_terracotta", 250: "black_glazed_terracotta",
    251: "white_concrete", 252: "white_concrete_powder",
}


def _legacy_block(bid, dv):
    """Convert legacy block ID + data value to (block_name, props_dict)."""
    # Unsigned: Java bytes are signed, IDs > 127 wrap negative
    if bid < 0:
        bid += 256
    base = _LEGACY_IDS.get(bid)
    if base is None:
        return None, {}

    props = {}
    # Data-variant blocks
    if bid == 1:  # stone variants
        base = ["stone", "granite", "polished_granite", "diorite",
                "polished_diorite", "andesite", "polished_andesite"][dv & 0x7] if (dv & 0x7) < 7 else "stone"
    elif bid == 5:  # planks
        base = _WOOD_TYPES[dv & 0x7] + "_planks" if (dv & 0x7) < 6 else "oak_planks"
    elif bid == 6:  # saplings
        base = _WOOD_TYPES[dv & 0x7] + "_sapling" if (dv & 0x7) < 6 else "oak_sapling"
    elif bid == 17:  # logs
        wt = _WOOD_TYPES[dv & 3] if (dv & 3) < 4 else "oak"
        axis_bits = (dv >> 2) & 3
        axis = {0: "y", 1: "x", 2: "z"}.get(axis_bits, "y")
        base = f"{wt}_log"
        props["axis"] = axis
    elif bid == 18:  # leaves
        wt = _WOOD_TYPES[dv & 3] if (dv & 3) < 4 else "oak"
        base = f"{wt}_leaves"
    elif bid == 24:  # sandstone
        base = ["sandstone", "chiseled_sandstone", "cut_sandstone"][dv & 3] if (dv & 3) < 3 else "sandstone"
    elif bid == 35:  # wool
        base = f"{_COLOR_NAMES[dv & 0xF]}_wool"
    elif bid == 44:  # stone slabs
        slab_types = ["smooth_stone_slab", "sandstone_slab", "petrified_oak_slab",
                      "cobblestone_slab", "brick_slab", "stone_brick_slab",
                      "nether_brick_slab", "quartz_slab"]
        base = slab_types[dv & 7] if (dv & 7) < len(slab_types) else "smooth_stone_slab"
        if dv & 8:
            props["type"] = "top"
    elif bid == 95:  # stained glass
        base = f"{_COLOR_NAMES[dv & 0xF]}_stained_glass"
    elif bid == 98:  # stone bricks
        base = ["stone_bricks", "mossy_stone_bricks", "cracked_stone_bricks",
                "chiseled_stone_bricks"][dv & 3] if (dv & 3) < 4 else "stone_bricks"
    elif bid == 125:  # double wooden slab → planks
        base = _WOOD_TYPES[dv & 7] + "_planks" if (dv & 7) < 6 else "oak_planks"
    elif bid == 126:  # wooden slabs
        base = _WOOD_TYPES[dv & 7] + "_slab" if (dv & 7) < 6 else "oak_slab"
        if dv & 8:
            props["type"] = "top"
    elif bid == 155:  # quartz
        base = ["quartz_block", "chiseled_quartz_block", "quartz_pillar"][dv & 3] if (dv & 3) < 3 else "quartz_block"
    elif bid == 159:  # stained clay / terracotta
        base = f"{_COLOR_NAMES[dv & 0xF]}_terracotta"
    elif bid == 160:  # stained glass pane
        base = f"{_COLOR_NAMES[dv & 0xF]}_stained_glass_pane"
    elif bid == 161:  # leaves2
        base = ["acacia_leaves", "dark_oak_leaves"][dv & 1]
    elif bid == 162:  # log2
        wt = ["acacia", "dark_oak"][dv & 1]
        axis = {0: "y", 1: "x", 2: "z"}.get((dv >> 2) & 3, "y")
        base = f"{wt}_log"
        props["axis"] = axis
    elif bid == 168:  # prismarine
        base = ["prismarine", "prismarine_bricks", "dark_prismarine"][dv & 3] if (dv & 3) < 3 else "prismarine"
    elif bid == 171:  # carpet
        base = f"{_COLOR_NAMES[dv & 0xF]}_carpet"
    elif bid == 175:  # double plants (lower half only)
        base = ["sunflower", "lilac", "tall_grass", "large_fern",
                "rose_bush", "peony"][dv & 7] if (dv & 7) < 6 else "sunflower"
    elif bid == 251:  # concrete
        base = f"{_COLOR_NAMES[dv & 0xF]}_concrete"
    elif bid == 252:  # concrete powder
        base = f"{_COLOR_NAMES[dv & 0xF]}_concrete_powder"
    # Stairs facing
    elif bid in (53, 67, 108, 109, 114, 128, 134, 135, 136, 156, 163, 164, 180, 203):
        facing = _STAIR_FACING.get(dv & 3, "north")
        half = "top" if dv & 4 else "bottom"
        props["facing"] = facing
        props["half"] = half

    return "minecraft:" + base, props


def parse_legacy_schematic(path):
    """Parse MCEdit .schematic format (numeric ids) with full block ID mapping."""
    nbt = nbt_load(path)
    s = nbt[""] if "" in nbt else nbt
    width = int(s["Width"]); height = int(s["Height"]); length = int(s["Length"])
    if width == 0 or height == 0 or length == 0:
        print(f"[ERR] Zero-dimension schematic: {width}x{height}x{length}")
        return (width, height, length), []
    block_ids = list(s.get("Blocks", []))
    data_vals = list(s.get("Data", []))
    if not data_vals:
        data_vals = [0] * len(block_ids)
    blocks = []
    unmapped = set()
    for i, bid in enumerate(block_ids):
        raw_bid = bid if bid >= 0 else bid + 256
        if raw_bid == 0:
            continue
        dv = data_vals[i] if i < len(data_vals) else 0
        dv = dv & 0xFF
        y = i // (width * length)
        z = (i // width) % length
        x = i % width
        name, props = _legacy_block(bid, dv)
        if name is None:
            unmapped.add(raw_bid)
            continue
        blocks.append((x, y, z, name, props))
    if unmapped:
        print(f"  [WARN] Unmapped legacy IDs: {sorted(unmapped)}")
    return (width, height, length), blocks


# ── OPTIMIZATION PASSES ────────────────────────────────────────────────────

def classify(name, props):
    """Returns (emit_kind, material, extra) where emit_kind is
    'CHEST', 'STAIRS', 'SLAB', 'DIRECTIONAL', 'SOLID', 'SKIP'."""
    if name in SKIP_BLOCKS:
        return ("SKIP", None, None)
    if name in LIQUID_BLOCKS:
        return ("SKIP", None, None)  # skipped by default per user spec
    short = name.replace("minecraft:", "")
    mat = MATERIAL_RENAMES.get(short.upper(), short.upper())
    if name == "minecraft:chest":
        return ("CHEST", mat, props.get("facing", "north"))
    if short.endswith(STAIR_SUFFIX):
        facing = props.get("facing", "north")
        upside = props.get("half", "bottom") == "top"
        return ("STAIRS", mat, (facing, upside))
    if short.endswith(SLAB_SUFFIX):
        top = props.get("type", "bottom") == "top"
        return ("SLAB", mat, top)
    # Generic directional block (wall torch, furnace, etc.)
    if "facing" in props and short in DIRECTIONAL_TYPES:
        return ("DIRECTIONAL", mat, props["facing"])
    return ("SOLID", mat, None)


def find_runs_x(solids_by_y):
    """Merge consecutive x-run of same material into fillBox runs. Returns list of
    (mat, y, z, x1, x2) runs. solids_by_y[(y,z)] -> dict x -> mat."""
    runs = []
    for (y, z), xmap in solids_by_y.items():
        xs = sorted(xmap.keys())
        i = 0
        while i < len(xs):
            mat = xmap[xs[i]]
            j = i
            while j + 1 < len(xs) and xs[j+1] == xs[j] + 1 and xmap[xs[j+1]] == mat:
                j += 1
            runs.append((mat, y, z, xs[i], xs[j]))
            i = j + 1
    return runs


def merge_runs_to_boxes(runs):
    """Merge x-runs that are identical across z and y into fillBox calls.
    runs: list of (mat, y, z, x1, x2). Returns list of boxes (mat, x1, y1, z1, x2, y2, z2) and remaining runs."""
    # Index runs by (mat, y, x1, x2) -> list of z
    key_to_zs = defaultdict(list)
    for mat, y, z, x1, x2 in runs:
        key_to_zs[(mat, y, x1, x2)].append(z)

    boxes = []
    leftovers = []
    used = set()
    for (mat, y, x1, x2), zs in key_to_zs.items():
        zs.sort()
        # Find consecutive z ranges
        i = 0
        while i < len(zs):
            j = i
            while j + 1 < len(zs) and zs[j+1] == zs[j] + 1:
                j += 1
            # Now we have a z-range [zs[i], zs[j]] at y level. Can we extend upward?
            # Check if same (x1,x2) for same mat exists at y+1..
            y2 = y
            while True:
                can_extend = all(
                    ((mat, y2+1, x1, x2), z) in {(k, v) for k, vs in key_to_zs.items() for v in vs}
                    for z in range(zs[i], zs[j]+1)
                )
                # For simplicity we don't extend vertically — keeps code cleaner.
                break
            # Emit as a horizontal plane-strip
            if zs[j] - zs[i] >= 1 or x2 - x1 >= 1:
                boxes.append((mat, x1, y, zs[i], x2, y2, zs[j]))
                for z in range(zs[i], zs[j]+1):
                    used.add((mat, y, z, x1, x2))
            else:
                # Single point — keep as run
                leftovers.append((mat, y, zs[i], x1, x2))
            i = j + 1
    # Leftovers
    for r in runs:
        if r not in used and r not in leftovers:
            leftovers.append(r)
    return boxes, leftovers


# ── CODE EMITTER ───────────────────────────────────────────────────────────

def emit_java(name, dims, blocks):
    """Returns a string with the generated Java class."""
    w, h, l = dims
    # Center offset: (0,0,0) at bottom-center of bounding box
    cx = w // 2
    cz = l // 2
    cy = 0  # bottom

    # Bucket by kind
    solids_by_y = defaultdict(dict)  # (y,z) -> x -> mat
    stairs_calls = []
    slab_calls = []
    directional_calls = []
    chest_calls = []
    skipped = 0

    for x, y, z, name_id, props in blocks:
        rx, ry, rz = x - cx, y - cy, z - cz
        kind, mat, extra = classify(name_id, props)
        if kind == "SKIP":
            skipped += 1
            continue
        if kind == "SOLID":
            solids_by_y[(ry, rz)][rx] = mat
        elif kind == "STAIRS":
            facing, upside = extra
            stairs_calls.append((rx, ry, rz, mat, FACE_MAP.get(facing, "NORTH"), upside))
        elif kind == "SLAB":
            slab_calls.append((rx, ry, rz, mat, extra))
        elif kind == "DIRECTIONAL":
            directional_calls.append((rx, ry, rz, mat, FACE_MAP.get(extra, "NORTH")))
        elif kind == "CHEST":
            chest_calls.append((rx, ry, rz))

    # Merge x-runs into fillBox calls
    x_runs = find_runs_x(solids_by_y)
    boxes, leftover_runs = merge_runs_to_boxes(x_runs)

    # Sort for stable output
    boxes.sort()
    leftover_runs.sort()
    stairs_calls.sort()
    slab_calls.sort()
    directional_calls.sort()
    chest_calls.sort()

    # Collect all body lines, then split into methods if needed.
    body = []

    if boxes:
        for mat, x1, y1, z1, x2, y2, z2 in boxes:
            body.append(f"        b.fillBox({x1}, {y1}, {z1}, {x2}, {y2}, {z2}, Material.{mat});")

    if leftover_runs:
        for mat, y, z, x1, x2 in leftover_runs:
            if x1 == x2:
                body.append(f"        b.setBlock({x1}, {y}, {z}, Material.{mat});")
            else:
                body.append(f"        b.fillBox({x1}, {y}, {z}, {x2}, {y}, {z}, Material.{mat});")

    if stairs_calls:
        for x, y, z, mat, face, upside in stairs_calls:
            u = "true" if upside else "false"
            body.append(f"        b.setStairs({x}, {y}, {z}, Material.{mat}, BlockFace.{face}, {u});")

    if slab_calls:
        for x, y, z, mat, top in slab_calls:
            t = "true" if top else "false"
            body.append(f"        b.setSlab({x}, {y}, {z}, Material.{mat}, {t});")

    if directional_calls:
        for x, y, z, mat, face in directional_calls:
            body.append(f"        b.setDirectional({x}, {y}, {z}, Material.{mat}, BlockFace.{face});")

    if chest_calls:
        for x, y, z in chest_calls:
            body.append(f"        b.placeChest({x}, {y}, {z});")

    # Emit class
    lines = []
    ap = lines.append
    ap(f"package com.jglims.plugin.structures.builders;")
    ap("")
    ap(f"import com.jglims.plugin.structures.StructureBuilder;")
    ap(f"import org.bukkit.Material;")
    ap(f"import org.bukkit.block.BlockFace;")
    ap("")
    ap(f"/** Generated by schem_to_builder.py from schematic. Dimensions: {w}x{h}x{l}. */")
    ap(f"public final class {name}Builder implements IStructureBuilder {{")

    if len(body) <= MAX_METHOD_LINES:
        ap(f"    @Override")
        ap(f"    public void build(StructureBuilder b) {{")
        lines.extend(body)
        ap(f"    }}")
    else:
        # Split into sub-methods to avoid 64KB method bytecode limit
        chunks = [body[i:i + MAX_METHOD_LINES] for i in range(0, len(body), MAX_METHOD_LINES)]
        ap(f"    @Override")
        ap(f"    public void build(StructureBuilder b) {{")
        for ci in range(len(chunks)):
            ap(f"        part{ci}(b);")
        ap(f"    }}")
        for ci, chunk in enumerate(chunks):
            ap(f"    private void part{ci}(StructureBuilder b) {{")
            lines.extend(chunk)
            ap(f"    }}")

    ap(f"}}")
    return "\n".join(lines) + "\n"


# ── MAIN ───────────────────────────────────────────────────────────────────

def convert_one(schem_path, class_name):
    # Auto-detect format by NBT content:
    # 1. Sponge v2/v3: has BlockData (varint-encoded) + Palette
    # 2. WorldEdit palette: has Palette + byte Blocks array (no BlockData)
    # 3. MCEdit legacy: has byte Blocks array + numeric IDs (no Palette)
    nbt = nbt_load(schem_path)
    root = nbt[""] if "" in nbt else nbt
    if "Schematic" in root:
        root_check = root["Schematic"]
    else:
        root_check = root
    has_blockdata = "BlockData" in root_check or ("Blocks" in root_check and "BlockData" in (root_check.get("Blocks") or {}))
    has_palette = "Palette" in root_check
    has_legacy_blocks = "Blocks" in root_check and not has_blockdata

    if has_blockdata:
        dims, blocks = parse_schem(schem_path)
    elif has_palette and has_legacy_blocks:
        dims, blocks = parse_we_palette_schematic(schem_path)
    else:
        dims, blocks = parse_legacy_schematic(schem_path)
    code = emit_java(class_name, dims, blocks)
    out_path = os.path.join(OUT_DIR, f"{class_name}Builder.java")
    os.makedirs(OUT_DIR, exist_ok=True)
    with open(out_path, "w", encoding="utf-8") as f:
        f.write(code)
    print(f"[OK] {schem_path} -> {out_path}  (dims={dims[0]}x{dims[1]}x{dims[2]}, blocks={len(blocks)})")
    return True


def run_batch(schem_dir, mapping_file):
    mappings = []
    with open(mapping_file, encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if not line or line.startswith("#"): continue
            # Accept '->' or '→' separators
            for sep in ("->", "→"):
                if sep in line:
                    schem, klass = line.split(sep, 1)
                    mappings.append((schem.strip(), klass.strip()))
                    break
    print(f"Batch: {len(mappings)} files to convert")
    success = 0
    for schem, klass in mappings:
        p = os.path.join(schem_dir, schem)
        if not os.path.exists(p):
            print(f"[SKIP] missing {p}")
            continue
        if convert_one(p, klass):
            success += 1
    print(f"Converted {success}/{len(mappings)}")


def main():
    if len(sys.argv) < 3:
        print(__doc__)
        sys.exit(1)
    if sys.argv[1] == "--batch":
        run_batch(sys.argv[2], sys.argv[3])
    else:
        schem_path = sys.argv[1]
        class_name = sys.argv[2]
        convert_one(schem_path, class_name)


if __name__ == "__main__":
    main()

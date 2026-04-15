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


def parse_schem(path):
    """Returns (dims, blocks, offset).
    dims = (width, height, length)
    blocks = list of (x, y, z, id_str, props_dict)
    offset = (ox, oy, oz) suggested center offset
    """
    nbt = nbt_load(path)
    # Sponge v2 wraps in root, v3 has a "Schematic" compound inside root
    root = nbt
    # Detect v3
    if "Schematic" in root:
        s = root["Schematic"]
        width = int(s["Width"])
        height = int(s["Height"])
        length = int(s["Length"])
        blocks_container = s.get("Blocks") or {}
        palette = blocks_container.get("Palette") or s.get("Palette") or {}
        blockdata = blocks_container.get("Data") or s.get("BlockData") or []
    else:
        s = root
        # Unwrap "" key if present (nbtlib quirk)
        if "" in s: s = s[""]
        width = int(s["Width"])
        height = int(s["Height"])
        length = int(s["Length"])
        palette = s.get("Palette") or {}
        blockdata = s.get("BlockData") or []
    blocks = _decode_varint_palette(blockdata, width, height, length, palette)
    return (width, height, length), blocks


def parse_legacy_schematic(path):
    """Parse MCEdit .schematic format (numeric ids)."""
    nbt = nbt_load(path)
    s = nbt[""] if "" in nbt else nbt
    width = int(s["Width"]); height = int(s["Height"]); length = int(s["Length"])
    # Legacy uses numeric ids + data bytes, mapping to block names requires a table.
    # We don't ship a full legacy table — warn and fall through to minimal support.
    print(f"[WARN] Legacy .schematic format detected — limited support; recommend converting to .schem first")
    block_ids = list(s.get("Blocks", []))
    # Minimum palette — just treat id 0 as air, non-zero as stone
    blocks = []
    for i, bid in enumerate(block_ids):
        if bid == 0: continue
        y = i // (width * length)
        z = (i // width) % length
        x = i % width
        blocks.append((x, y, z, "minecraft:stone", {}))
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
    mat = short.upper()
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
    ap(f"    @Override")
    ap(f"    public void build(StructureBuilder b) {{")

    if boxes:
        ap(f"        // {len(boxes)} filled boxes")
        for mat, x1, y1, z1, x2, y2, z2 in boxes:
            ap(f"        b.fillBox({x1}, {y1}, {z1}, {x2}, {y2}, {z2}, Material.{mat});")

    if leftover_runs:
        ap(f"        // {len(leftover_runs)} individual x-runs")
        for mat, y, z, x1, x2 in leftover_runs:
            if x1 == x2:
                ap(f"        b.setBlock({x1}, {y}, {z}, Material.{mat});")
            else:
                ap(f"        b.fillBox({x1}, {y}, {z}, {x2}, {y}, {z}, Material.{mat});")

    if stairs_calls:
        ap(f"        // {len(stairs_calls)} stairs")
        for x, y, z, mat, face, upside in stairs_calls:
            u = "true" if upside else "false"
            ap(f"        b.setStairs({x}, {y}, {z}, Material.{mat}, BlockFace.{face}, {u});")

    if slab_calls:
        ap(f"        // {len(slab_calls)} slabs")
        for x, y, z, mat, top in slab_calls:
            t = "true" if top else "false"
            ap(f"        b.setSlab({x}, {y}, {z}, Material.{mat}, {t});")

    if directional_calls:
        ap(f"        // {len(directional_calls)} directional blocks")
        for x, y, z, mat, face in directional_calls:
            ap(f"        b.setDirectional({x}, {y}, {z}, Material.{mat}, BlockFace.{face});")

    if chest_calls:
        ap(f"        // {len(chest_calls)} chests")
        for x, y, z in chest_calls:
            ap(f"        b.placeChest({x}, {y}, {z});")

    ap(f"        // Bottom-centered; origin (0,0,0) is at base-center of {w}x{h}x{l} volume.")
    ap(f"        // Skipped air/liquid blocks: {skipped}")
    ap(f"    }}")
    ap(f"}}")
    return "\n".join(lines) + "\n"


# ── MAIN ───────────────────────────────────────────────────────────────────

def convert_one(schem_path, class_name):
    ext = os.path.splitext(schem_path)[1].lower()
    if ext == ".schem":
        dims, blocks = parse_schem(schem_path)
    elif ext == ".schematic":
        dims, blocks = parse_legacy_schematic(schem_path)
    else:
        print(f"[ERR] Unknown extension: {schem_path}")
        return False
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

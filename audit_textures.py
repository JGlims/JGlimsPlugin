"""Audit merged_pack/ for broken texture/model references and orphaned plugin CMDs.

Run: python3 audit_textures.py
Outputs a report — does NOT modify any files.
"""
import json, os, re

PACK = "merged_pack"
ITEMS_DIR = os.path.join(PACK, "assets", "minecraft", "items")
MODELS_DIR = os.path.join(PACK, "assets", "minecraft", "models", "item")
TEXTURES_DIR = os.path.join(PACK, "assets", "minecraft", "textures", "item")
JAVA_DIR = "src/main/java"

# Vanilla items/ filenames must match vanilla item IDs. We don't maintain a full
# list — so we accept anything that fits the pattern. The ones that are clearly
# NOT vanilla items (namespaced as mob/boss names) get flagged in structural issues.
KNOWN_INVALID_ITEM_FILES = {"abyss_dragon.json"}  # flag-and-delete list


def read_json_report(path, issues):
    with open(path, "rb") as f:
        raw = f.read()
    if raw.startswith(b"\xef\xbb\xbf"):
        issues.append(f"{os.path.relpath(path, PACK)} — has UTF-8 BOM")
        raw = raw[3:]
    try:
        return json.loads(raw.decode("utf-8"))
    except Exception as e:
        issues.append(f"{os.path.relpath(path, PACK)} — invalid JSON: {e}")
        return None


def extract_model_refs(node, out):
    """Recursively extract every {'type':'minecraft:model','model':'...'} reference."""
    if isinstance(node, dict):
        if node.get("type") == "minecraft:model" and "model" in node:
            out.append(node["model"])
        for v in node.values():
            extract_model_refs(v, out)
    elif isinstance(node, list):
        for v in node:
            extract_model_refs(v, out)


def iter_cases(items_json):
    sel = items_json.get("model", {})
    if sel.get("type") == "minecraft:select":
        for c in sel.get("cases", []):
            yield c
        fb = sel.get("fallback")
        if fb:
            yield {"when": "<fallback>", "model": fb}
    elif sel.get("type") == "minecraft:model":
        yield {"when": "<direct>", "model": sel}


def model_path_to_file(model_ref):
    # "minecraft:item/foo" -> models/item/foo.json
    if ":" not in model_ref:
        return None
    ns, rest = model_ref.split(":", 1)
    if ns != "minecraft" or not rest.startswith("item/"):
        return None
    name = rest[len("item/"):]
    return os.path.join(MODELS_DIR, name + ".json")


def texture_path_to_file(tex_ref):
    if ":" in tex_ref:
        ns, rest = tex_ref.split(":", 1)
    else:
        ns, rest = "minecraft", tex_ref
    if ns != "minecraft" or not rest.startswith("item/"):
        return None
    name = rest[len("item/"):]
    return os.path.join(TEXTURES_DIR, name + ".png")


def collect_plugin_cmds():
    """Scan Java source for setStrings(...) / setCustomModelData(int) calls.
    Returns list of (file, line, base_item_hint, cmd_value)."""
    results = []
    set_strings_re = re.compile(r'setStrings\s*\(\s*(?:java\.util\.)?(?:List\.of|Collections\.singletonList|Arrays\.asList)?\s*\(?\s*"([^"]+)"')
    set_cmd_int_re = re.compile(r'setCustomModelData\s*\(\s*(\d+)\s*\)')
    material_re = re.compile(r'new\s+ItemStack\s*\(\s*Material\.([A-Z_]+)')
    if not os.path.exists(JAVA_DIR):
        return results
    for root, _, files in os.walk(JAVA_DIR):
        for f in files:
            if not f.endswith(".java"):
                continue
            path = os.path.join(root, f)
            try:
                txt = open(path, encoding="utf-8").read()
            except Exception:
                continue
            for m in set_strings_re.finditer(txt):
                results.append((f, "string", m.group(1)))
            for m in set_cmd_int_re.finditer(txt):
                results.append((f, "int", int(m.group(1))))
    return results


def main():
    broken = []
    orphaned = []
    structural = []
    missing_art = []

    # Step 1: parse every items/*.json
    items_cases = {}  # filename -> list of (when, model_path)
    for fn in sorted(os.listdir(ITEMS_DIR)):
        if not fn.endswith(".json"):
            continue
        path = os.path.join(ITEMS_DIR, fn)
        if fn in KNOWN_INVALID_ITEM_FILES:
            structural.append(f"items/{fn} — not a valid vanilla item ID, delete this file")
        data = read_json_report(path, structural)
        if data is None:
            continue
        sel = data.get("model", {})
        if sel.get("type") == "minecraft:select":
            if "index" not in sel:
                structural.append(f"items/{fn} — missing top-level 'index' field on select")
            for c in sel.get("cases", []):
                if isinstance(c.get("when"), int) and "index" not in sel:
                    pass  # covered by structural check above
        cases_here = []
        for c in iter_cases(data):
            refs = []
            extract_model_refs(c.get("model"), refs)
            for r in refs:
                cases_here.append((c.get("when"), r))
        items_cases[fn] = cases_here

    # Step 2: for every model ref, verify model file and its texture refs.
    # Magenta only happens when a CUSTOM name resolves to nothing in the pack
    # AND vanilla doesn't provide it either. Model/texture names that match a
    # vanilla item (typically same-name self-reference in <fallback> or numeric
    # cases) fall through to vanilla automatically and never produce magenta.
    seen_model_files = set()
    for fn, cases in items_cases.items():
        base_vanilla_name = fn[:-5]  # strip .json
        for when, model_ref in cases:
            mpath = model_path_to_file(model_ref)
            if mpath is None:
                broken.append(f"items/{fn} → case {when!r} → {model_ref} → invalid model path")
                continue
            # Derive the referenced model's short name
            mname = os.path.splitext(os.path.basename(mpath))[0]
            is_self_vanilla = (mname == base_vanilla_name)
            if not os.path.exists(mpath):
                # Self-reference or non-custom-string case: vanilla provides it
                if is_self_vanilla or not isinstance(when, str) or when == "<fallback>":
                    continue
                broken.append(f"items/{fn} → case {when!r} → {model_ref} → MISSING MODEL FILE ({os.path.relpath(mpath, PACK)})")
                continue
            if mpath in seen_model_files:
                continue
            seen_model_files.add(mpath)
            try:
                mdata = json.loads(open(mpath, encoding="utf-8-sig").read())
            except Exception as e:
                broken.append(f"{os.path.relpath(mpath, PACK)} — invalid JSON: {e}")
                continue
            for tkey, tref in (mdata.get("textures") or {}).items():
                if not isinstance(tref, str) or tref.startswith("#"):
                    continue
                tpath = texture_path_to_file(tref)
                if tpath is None:
                    continue
                tname = os.path.splitext(os.path.basename(tpath))[0]
                # Vanilla fallback: texture name matches the item/model base name
                if tname in (base_vanilla_name, mname):
                    continue
                if not os.path.exists(tpath):
                    broken.append(f"items/{fn} → case {when!r} → model {model_ref} → texture '{tref}' → MISSING PNG ({os.path.relpath(tpath, PACK)})")

    # Step 3: orphaned CMDs from Java source
    plugin_cmds = collect_plugin_cmds()
    all_string_cases = set()
    all_int_cases = set()
    for cases in items_cases.values():
        for when, _ in cases:
            if isinstance(when, str) and when not in ("<fallback>", "<direct>"):
                all_string_cases.add(when)
            elif isinstance(when, int):
                all_int_cases.add(when)
    for src, kind, val in plugin_cmds:
        if kind == "string" and val not in all_string_cases:
            orphaned.append(f"{src} → setStrings(\"{val}\") — no matching case in any items/*.json")
        elif kind == "int" and val not in all_int_cases:
            orphaned.append(f"{src} → setCustomModelData({val}) — no matching case in any items/*.json")

    # Print report
    print("=== TEXTURE AUDIT RESULTS ===\n")
    print("BROKEN REFERENCES (will show magenta):")
    for b in broken: print(f"  {b}")
    if not broken: print("  (none)")
    print(f"\nORPHANED CASES (CMD in Java but no items/*.json case): {len(orphaned)}")
    for o in orphaned[:50]: print(f"  {o}")
    if len(orphaned) > 50: print(f"  ... and {len(orphaned) - 50} more")
    print(f"\nSTRUCTURAL ISSUES:")
    for s in structural: print(f"  {s}")
    if not structural: print("  (none)")
    print(f"\nSUMMARY: {len(broken)} broken references, {len(orphaned)} orphaned cases, {len(structural)} structural issues")


if __name__ == "__main__":
    main()

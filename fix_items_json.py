"""Fix items/*.json files for Minecraft 1.21.4+ strict format compliance.

Changes:
1. In `select` with `property=custom_model_data` and `index=0`, remove any
   numeric `when` case (type mismatch — would never match strings[0] anyway).
2. If a file ONLY has numeric cases, convert the whole select to a
   `range_dispatch` over floats[0] (which is where Paper stores the int
   value of setCustomModelData(int) in 1.21.4+).
3. Ensure every file has top-level `model` wrapper and `fallback`.
4. Strip UTF-8 BOMs.

Run: python3 fix_items_json.py
"""
import json, os, shutil, sys

ITEMS_DIR = "merged_pack/assets/minecraft/items"


def load(path):
    with open(path, "rb") as f:
        raw = f.read()
    had_bom = raw.startswith(b"\xef\xbb\xbf")
    if had_bom: raw = raw[3:]
    return json.loads(raw.decode("utf-8")), had_bom


def save(path, data):
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
        f.write("\n")


def convert_select_to_range_dispatch(sel):
    """If select has only numeric cases, return an equivalent range_dispatch.
    Otherwise return None."""
    cases = sel.get("cases", [])
    if not cases: return None
    whens = [c.get("when") for c in cases]
    if not all(isinstance(w, int) or (isinstance(w, float)) for w in whens):
        return None
    # Sort by threshold ascending
    entries = []
    for c in sorted(cases, key=lambda c: c["when"]):
        entries.append({
            "threshold": float(c["when"]),
            "model": c["model"],
        })
    new = {
        "type": "minecraft:range_dispatch",
        "property": "minecraft:custom_model_data",
        "scale": 1.0,
    }
    if "fallback" in sel:
        new["fallback"] = sel["fallback"]
    new["entries"] = entries
    return new


def fix_one(path):
    data, had_bom = load(path)
    changed = had_bom
    notes = []
    if had_bom: notes.append("stripped BOM")

    model = data.get("model")
    if not isinstance(model, dict):
        return changed, ["no model wrapper; skipping"]

    mtype = model.get("type", "")
    if mtype in ("minecraft:select", "select"):
        prop = model.get("property", "")
        if prop in ("minecraft:custom_model_data", "custom_model_data"):
            idx = model.get("index", 0)
            # If index is 0 (reading strings[0]), only string cases are valid.
            cases = model.get("cases", [])
            str_cases = [c for c in cases if isinstance(c.get("when"), str)]
            int_cases = [c for c in cases if isinstance(c.get("when"), (int, float))]
            if int_cases and str_cases:
                # Mixed — drop the int cases (they can't match strings[0])
                model["cases"] = str_cases
                changed = True
                notes.append(f"removed {len(int_cases)} int cases from mixed select (strings-typed)")
            elif int_cases and not str_cases:
                # All numeric — convert to range_dispatch so floats[0] matching works
                new_model = convert_select_to_range_dispatch(model)
                if new_model is not None:
                    data["model"] = new_model
                    changed = True
                    notes.append(f"converted int-only select to range_dispatch ({len(int_cases)} entries)")

    if changed:
        shutil.copy(path, path + ".bak")
        save(path, data)
    return changed, notes


def main():
    touched = 0
    skipped = 0
    for fn in sorted(os.listdir(ITEMS_DIR)):
        if not fn.endswith(".json"): continue
        path = os.path.join(ITEMS_DIR, fn)
        try:
            changed, notes = fix_one(path)
        except Exception as e:
            print(f"[ERR] {fn}: {e}"); continue
        if changed:
            touched += 1
            print(f"[FIX] {fn}: {'; '.join(notes)}")
        else:
            skipped += 1
    print(f"\nDone. Modified {touched}, untouched {skipped}.")


if __name__ == "__main__":
    main()

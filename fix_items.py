"""
Fix items/*.json files by adding "index": 0 where it's missing. Also strips
UTF-8 BOM from files that have it, so Minecraft's strict JSON parser in
alpha Paper 26.1.2 doesn't trip on them.
"""
import os
import json
import codecs

ROOT = "merged_pack/assets/minecraft/items"

fixed_count = 0
bom_stripped = 0

for fname in sorted(os.listdir(ROOT)):
    if not fname.endswith(".json"):
        continue
    path = os.path.join(ROOT, fname)

    # Read raw bytes to detect BOM
    with open(path, "rb") as f:
        raw = f.read()
    had_bom = raw.startswith(codecs.BOM_UTF8)
    if had_bom:
        raw = raw[len(codecs.BOM_UTF8):]
        bom_stripped += 1

    text = raw.decode("utf-8")
    try:
        data = json.loads(text)
    except Exception as e:
        print(f"  SKIP (parse error): {fname}: {e}")
        continue

    model = data.get("model")
    if not isinstance(model, dict):
        continue

    # Only add index if the model is a select over custom_model_data and lacks index
    if (model.get("type") == "minecraft:select"
            and model.get("property") == "minecraft:custom_model_data"
            and "index" not in model):
        # Insert "index": 0 right after "property"
        new_model = {}
        for k, v in model.items():
            new_model[k] = v
            if k == "property":
                new_model["index"] = 0
        data["model"] = new_model
        fixed_count += 1

    # Write back as clean UTF-8 (no BOM)
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)

print(f"Fixed 'index' in {fixed_count} files.")
print(f"Stripped BOM from {bom_stripped} files.")

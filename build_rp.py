import os
import zipfile

ROOT = "merged_pack"
OUT = "JGlimsResourcePack.zip"

if os.path.exists(OUT):
    os.remove(OUT)

count = 0
with zipfile.ZipFile(OUT, "w", zipfile.ZIP_DEFLATED, compresslevel=9) as z:
    for dirpath, dirs, files in os.walk(ROOT):
        for f in files:
            full = os.path.join(dirpath, f)
            rel = os.path.relpath(full, ROOT).replace(os.sep, "/")
            z.write(full, rel)
            count += 1

print(f"files packed: {count}")
print(f"verified in zip: {len(zipfile.ZipFile(OUT).namelist())}")
print(f"size bytes: {os.path.getsize(OUT)}")

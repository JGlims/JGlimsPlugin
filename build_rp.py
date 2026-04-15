"""Build JGlimsResourcePack.zip with fresh BetterModel overlays auto-synced from the server.

Run: python3 build_rp.py
Skip the sync step: python3 build_rp.py --offline
"""
import os, shutil, subprocess, sys, zipfile

ROOT = "merged_pack"
OUT = "JGlimsResourcePack.zip"
SSH_KEY = r"C:\Users\jgmel\Documents\projects\server_minecraft\ssh-key-2026-02-25.key"
SSH_HOST = "ubuntu@144.22.198.184"
REMOTE_BM = "/home/ubuntu/minecraft-server/data/plugins/BetterModel/build.zip"
LOCAL_BM = "bm_build_fresh.zip"


def sync_bettermodel():
    print("[sync] fetching BetterModel build.zip from server...")
    r = subprocess.run(
        ["scp", "-i", SSH_KEY, "-o", "StrictHostKeyChecking=no",
         "-o", "ServerAliveInterval=30", "-o", "ServerAliveCountMax=10", "-C",
         f"{SSH_HOST}:{REMOTE_BM}", LOCAL_BM],
        capture_output=True, text=True, timeout=180)
    if r.returncode != 0 or not os.path.exists(LOCAL_BM):
        print(f"[sync] WARNING: SCP failed ({r.stderr.strip()}); BetterModel overlays may be stale")
        return False
    for d in ("bettermodel_legacy", "bettermodel_modern", os.path.join("assets", "bettermodel")):
        p = os.path.join(ROOT, d)
        if os.path.exists(p): shutil.rmtree(p)
    n = 0
    with zipfile.ZipFile(LOCAL_BM) as z:
        for name in z.namelist():
            if name in ("pack.mcmeta", "pack.png") or name.endswith("/"): continue
            t = os.path.join(ROOT, name.replace("/", os.sep))
            os.makedirs(os.path.dirname(t), exist_ok=True)
            with z.open(name) as s, open(t, "wb") as d: shutil.copyfileobj(s, d)
            n += 1
    print(f"[sync] extracted {n} BetterModel files")
    return True


if "--offline" not in sys.argv:
    sync_bettermodel()
else:
    print("[sync] skipped (--offline); BetterModel overlays may be stale")

if os.path.exists(OUT): os.remove(OUT)
count = 0
with zipfile.ZipFile(OUT, "w", zipfile.ZIP_DEFLATED, compresslevel=9) as z:
    for dp, _, files in os.walk(ROOT):
        for f in files:
            full = os.path.join(dp, f)
            rel = os.path.relpath(full, ROOT).replace(os.sep, "/")
            z.write(full, rel)
            count += 1

print(f"files packed: {count}")
print(f"verified in zip: {len(zipfile.ZipFile(OUT).namelist())}")
print(f"size bytes: {os.path.getsize(OUT)}")

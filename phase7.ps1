# Save this entire block as:
# C:\Users\jgmel\Documents\projects\JGlimsPlugin\phase7.ps1
# Then run:  powershell -ExecutionPolicy Bypass -File "C:\Users\jgmel\Documents\projects\JGlimsPlugin\phase7.ps1"

$ErrorActionPreference = "Continue"
$PSNativeCommandUseErrorActionPreference = $false

$projectRoot = "C:\Users\jgmel\Documents\projects\JGlimsPlugin\JGlimsPlugin"
$rpWork      = "C:\Users\jgmel\Documents\projects\JGlimsPlugin\resourcepack-work\JGlimsResourcePack"
$utf8NoBOM   = New-Object System.Text.UTF8Encoding $false

# ── 2. BUILD ──
Write-Host "`n=== 2/7  Building plugin ===" -ForegroundColor Cyan
Push-Location $projectRoot
cmd /c ".\gradlew.bat clean build --no-daemon 2>&1"
$jarFile = Get-ChildItem "build\libs\JGlimsPlugin*.jar" -ErrorAction SilentlyContinue | Select-Object -First 1
if ($jarFile) {
    Write-Host "  JAR built: $($jarFile.FullName)  ($($jarFile.Length) bytes)" -ForegroundColor Green
} else {
    Write-Host "  BUILD FAILED - no JAR found." -ForegroundColor Red
    Pop-Location
    return
}
Pop-Location

# ── 3. pack.mcmeta ──
Write-Host "`n=== 3/7  Writing pack.mcmeta ===" -ForegroundColor Cyan
$packMcmeta = @'
{
  "pack": {
    "pack_format": 75,
    "description": "JGlims Legendary Weapons Resource Pack"
  }
}
'@
[System.IO.File]::WriteAllText((Join-Path $rpWork "pack.mcmeta"), $packMcmeta, $utf8NoBOM)
Write-Host "  Written pack.mcmeta (format 75)" -ForegroundColor Green

# ── 4. ITEM DEFINITIONS ──
Write-Host "`n=== 4/7  Writing item definition files ===" -ForegroundColor Cyan
$itemsDir = Join-Path $rpWork "assets\minecraft\items"
New-Item -ItemType Directory -Path $itemsDir -Force | Out-Null

$swords = @("stormbringer","aquantic_sacred_blade","excalibur","requiem_of_hell","royalchakram","black_iron_greatsword","muramasa","soul_collector","amethyst_shuriken","valhakyra","windreaper","phantomguard_greatsword","moonlight","zenith","solstice","grand_claymore","dragon_sword","talonbrand","demons_blood_blade","nocturne","revenants_gravescepter","lycanbane","gloomsteel_katana","revenants_gravecleaver","amethyst_greatblade","flamberge","crystal_frostblade","demonslayers_greatsword","vengeance_blade","oculus","ancient_greatslab","spider_sword")
$axes = @("berserkers_greataxe","treacherous_cleaver","gilded_phoenix_greataxe","calamity_blade","emerald_greatcleaver","viridian_greataxe","crescent_greataxe","vindicator")
$tridents = @("frostaxe","aquantic_sacred_blade","stormbringer")
$hoes = @("jadehalberd")

function Build-ItemDef([string]$vanillaModel, [string[]]$names) {
    $c = @()
    foreach ($n in $names) {
        $c += '      { "when": "' + $n + '", "model": { "type": "minecraft:model", "model": "minecraft:item/' + $n + '" } }'
    }
    $j = $c -join ",`n"
    return '{
  "model": {
    "type": "minecraft:select",
    "property": "minecraft:custom_model_data",
    "index": 0,
    "cases": [
' + $j + '
    ],
    "fallback": {
      "type": "minecraft:model",
      "model": "minecraft:item/' + $vanillaModel + '"
    }
  }
}'
}

[System.IO.File]::WriteAllText((Join-Path $itemsDir "diamond_sword.json"), (Build-ItemDef "diamond_sword" $swords), $utf8NoBOM)
Write-Host "  diamond_sword.json ($($swords.Count) cases)" -ForegroundColor Green
[System.IO.File]::WriteAllText((Join-Path $itemsDir "diamond_axe.json"), (Build-ItemDef "diamond_axe" $axes), $utf8NoBOM)
Write-Host "  diamond_axe.json ($($axes.Count) cases)" -ForegroundColor Green
[System.IO.File]::WriteAllText((Join-Path $itemsDir "trident.json"), (Build-ItemDef "trident" $tridents), $utf8NoBOM)
Write-Host "  trident.json ($($tridents.Count) cases)" -ForegroundColor Green
[System.IO.File]::WriteAllText((Join-Path $itemsDir "diamond_hoe.json"), (Build-ItemDef "diamond_hoe" $hoes), $utf8NoBOM)
Write-Host "  diamond_hoe.json ($($hoes.Count) cases)" -ForegroundColor Green

# ── 5. MODEL JSONS ──
Write-Host "`n=== 5/7  Auditing model JSON files ===" -ForegroundColor Cyan
$modelsDir = Join-Path $rpWork "assets\minecraft\models\item"
New-Item -ItemType Directory -Path $modelsDir -Force | Out-Null
$texDir = Join-Path $rpWork "assets\minecraft\textures\item"

$allTex = @("stormbringer","aquantic_sacred_blade","excalibur","requiem_of_hell","royalchakram","berserkers_greataxe","treacherous_cleaver","black_iron_greatsword","muramasa","gilded_phoenix_greataxe","soul_collector","amethyst_shuriken","valhakyra","windreaper","phantomguard_greatsword","moonlight","zenith","solstice","grand_claymore","calamity_blade","dragon_sword","talonbrand","emerald_greatcleaver","demons_blood_blade","nocturne","revenants_gravescepter","lycanbane","gloomsteel_katana","viridian_greataxe","crescent_greataxe","revenants_gravecleaver","amethyst_greatblade","flamberge","crystal_frostblade","demonslayers_greatsword","vengeance_blade","oculus","ancient_greatslab","frostaxe","jadehalberd","vindicator","spider_sword")

$created = 0; $existed = 0
foreach ($tn in $allTex) {
    $mf = Join-Path $modelsDir "$tn.json"
    if (-not (Test-Path $mf)) {
        $mj = '{ "parent": "minecraft:item/handheld", "textures": { "layer0": "minecraft:item/' + $tn + '" } }'
        [System.IO.File]::WriteAllText($mf, $mj, $utf8NoBOM)
        $created++
        $hasPng = Test-Path (Join-Path $texDir "$tn.png")
        if ($hasPng) { Write-Host "  $tn.json - created (texture OK)" -ForegroundColor Green }
        else { Write-Host "  $tn.json - created (TEXTURE MISSING)" -ForegroundColor Yellow }
    } else { $existed++ }
}
Write-Host "  $existed existed, $created created" -ForegroundColor Cyan

# ── 6. TEXTURE AUDIT ──
Write-Host "`n=== 6/7  Texture audit ===" -ForegroundColor Cyan
$miss = @()
foreach ($tn in $allTex) {
    if (-not (Test-Path (Join-Path $texDir "$tn.png"))) { $miss += $tn; Write-Host "  MISSING: $tn.png" -ForegroundColor Red }
}
if ($miss.Count -eq 0) { Write-Host "  All 42 textures present!" -ForegroundColor Green }
else { Write-Host "  $($miss.Count) missing" -ForegroundColor Yellow }

# ── 7. ZIP ──
Write-Host "`n=== 7/7  Creating ZIP ===" -ForegroundColor Cyan
$zipPath = "C:\Users\jgmel\Documents\projects\JGlimsPlugin\resourcepack-work\JGlimsResourcePack.zip"
Remove-Item $zipPath -Force -ErrorAction SilentlyContinue

Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem

$zip = [System.IO.Compression.ZipFile]::Open($zipPath, [System.IO.Compression.ZipArchiveMode]::Create)
$allFiles = Get-ChildItem -Path $rpWork -Recurse -File
$count = 0
foreach ($f in $allFiles) {
    $rel = $f.FullName.Substring($rpWork.Length + 1).Replace('\','/')
    [System.IO.Compression.ZipFileExtensions]::CreateEntryFromFile($zip, $f.FullName, $rel, [System.IO.Compression.CompressionLevel]::Optimal) | Out-Null
    $count++
}
$zip.Dispose()

$vz = [System.IO.Compression.ZipFile]::OpenRead($zipPath)
$bs = $false; $i = 0
Write-Host "`n  First 20 entries:"
foreach ($e in $vz.Entries) { if ($i -ge 20) { break }; Write-Host "    $($e.FullName)"; if ($e.FullName -match '\\') { $bs=$true }; $i++ }
$tot = $vz.Entries.Count
$hasMcmeta = ($vz.Entries | Where-Object { $_.FullName -eq "pack.mcmeta" }).Count -gt 0
$itemsDefs = ($vz.Entries | Where-Object { $_.FullName -like "assets/minecraft/items/*" }).Count
$vz.Dispose()

if (-not $bs) { Write-Host "`n  Forward slashes OK" -ForegroundColor Green } else { Write-Host "`n  BACKSLASH ERROR" -ForegroundColor Red }
if ($hasMcmeta) { Write-Host "  pack.mcmeta at root OK" -ForegroundColor Green } else { Write-Host "  pack.mcmeta MISSING from root" -ForegroundColor Red }
Write-Host "  Item defs in ZIP: $itemsDefs" -ForegroundColor Cyan
Write-Host "  Total entries: $tot"

$h = (Get-FileHash $zipPath -Algorithm SHA1).Hash.ToLower()
$s = (Get-Item $zipPath).Length
Write-Host "`n  ZIP size: $s bytes" -ForegroundColor Cyan
Write-Host "  SHA-1:   $h" -ForegroundColor Cyan

Write-Host "`n============================================" -ForegroundColor White
Write-Host "  DONE" -ForegroundColor Green
Write-Host "  JAR  : $($jarFile.FullName)" -ForegroundColor White
Write-Host "  ZIP  : $zipPath" -ForegroundColor White
Write-Host "  SHA-1: $h" -ForegroundColor White
Write-Host "  Next : paste output here" -ForegroundColor Yellow
Write-Host "============================================" -ForegroundColor White

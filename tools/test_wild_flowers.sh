#!/usr/bin/env bash
# Offline checks for the wild flower system (feature_writeup/flowers.md section 6):
#   1. generator: tools/gen_wild_flowers.py runs with no config problems, every Botania color generates, and the
#      snapshot (flower_features.json) was already up to date
#   2. Rhino: kubejs/server_scripts/worldgen/wild_flowers.js in the pack's Rhino fork; every biome tag must equal
#      the generator's plan, every file must be valid JSON, every modifier must point at a filled tag and a
#      feature that exists, and each Flower Patch rewrite must equal the generator's conversion
#   3. Rhino with CEConfig wildFlowers off: no tags, no files
#   4. Rhino with Botania missing: its palette features and modifier skipped, nothing else changes
# Needs the ATLauncher library folder (Rhino's gson). No game needed.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
T="$(mktemp -d)"; trap 'rm -rf "$T"' EXIT
L="$HOME/.local/share/atlauncher/libraries"
cd "$ROOT"

echo "== 1. generator"
python3 tools/gen_wild_flowers.py | tee "$T/gen.txt" | head -2
! grep -q PROBLEM "$T/gen.txt" || { grep PROBLEM "$T/gen.txt"; echo "generator reports config problems"; exit 1; }
! grep -q '(changed)' "$T/gen.txt" || { echo "flower_features.json was stale (now regenerated: rejoin to use it)"; exit 1; }

echo "== 2. Rhino"
RHINO="$(ls mods/rhino-forge-*.jar | head -1)"; GSON="$L/com/google/code/gson/gson/2.8.9/gson-2.8.9.jar"
mkdir -p "$T/cls"
javac -proc:none -cp "$RHINO:$GSON" -d "$T/cls" tools/wild_flowers_test/RhinoWildFlowers.java
java -cp "$T/cls:$RHINO:$GSON" RhinoWildFlowers "$ROOT" "$T/on" | tee "$T/on.log" | tail -2
! grep -q '^ERROR\|^WARN' "$T/on.log" || { echo "script logged warnings/errors"; exit 1; }
python3 - "$T/on" <<'PY'
import json, sys, glob, os
sys.path.insert(0, 'tools')
import gen_crop_worldgen_table as g
import gen_wild_flowers as f
out = sys.argv[1]
cfg = g.loads_lenient(f.CONFIG_PATH.read_text())
_, traits = g.load_wild_crops()
snap = json.loads(f.SNAPSHOT.read_text())
# 'follows' entries take the biome list the generator wrote into the snapshot (as wild_flowers.js does)
for k, e in cfg.get('flowers', {}).items():
    if e.get('follows'):
        e['_follows'] = snap.get('follows', {}).get(k, [])
tags = json.load(open(os.path.join(out, 'tags.json')))
bad = []

# tags: the generator's plan
want = {}
for i, (pal, key, biomes) in enumerate(f.botania_plan(cfg, traits)):
    want[f'creativeengineering:wild_flowers/botania/{i}'] = biomes
for key in cfg['flowers']:
    for t, biomes in f.entry_plan(cfg, traits, key).items():
        want[f'creativeengineering:wild_flowers/{key}/{t}'] = biomes
for fid, spec in cfg.get('existing', {}).items():
    bs = f.removal_biomes(spec, traits)
    if bs and float(spec.get('scale', 1)) > 0:
        want['creativeengineering:wild_flowers/remove/' + fid.replace(':', '/')] = bs
for t in sorted(set(want) | set(tags)):
    if sorted(want.get(t, [])) != sorted(tags.get(t, [])):
        bad.append(f'tag {t}: ' + str(sorted(set(want.get(t, [])) ^ set(tags.get(t, [])))))

# files
files = {}
for p in glob.glob(os.path.join(out, 'data/**/*.json'), recursive=True):
    rel = os.path.relpath(p, os.path.join(out, 'data')).split(os.sep)
    files[(rel[0], '/'.join(rel[1:]))] = json.load(open(p))
world = f.load_world()
def exists(kind, rid):
    ns, path = rid.split(':', 1)
    return (ns, f'worldgen/{kind}_feature/{path}.json') in files or rid in world[kind]
adds = nones = removes = 0
for (ns, path), d in files.items():
    if path.startswith('forge/biome_modifier/'):
        t = d['type']
        if t == 'forge:none':
            nones += 1
        elif t == 'forge:add_features':
            adds += 1
            if d['biomes'][1:] not in tags:
                bad.append(f'{ns}:{path}: tag {d["biomes"]} is empty')
            if not exists('placed', d['features']):
                bad.append(f'{ns}:{path}: no placed feature {d["features"]}')
        elif t == 'forge:remove_features':
            removes += 1
            if d['biomes'] != '#minecraft:is_overworld' and d['biomes'][1:] not in tags:
                bad.append(f'{ns}:{path}: tag {d["biomes"]} is empty')
        else:
            bad.append(f'{ns}:{path}: unexpected type {t}')
    elif path.startswith('worldgen/placed_feature/') and isinstance(d['feature'], str) and not exists('configured', d['feature']):
        bad.append(f'{ns}:{path}: no configured feature {d["feature"]}')

# weights: the codec wants positive ints
def walk_weights(v, where):
    if isinstance(v, dict):
        if v.get('type') == 'minecraft:weighted_state_provider':
            for e in v['entries']:
                if not (isinstance(e['weight'], int) and e['weight'] > 0):
                    bad.append(f'{where}: weight {e["weight"]}')
        for x in v.values():
            walk_weights(x, where)
    elif isinstance(v, list):
        for x in v:
            walk_weights(x, where)
for k, d in files.items():
    walk_weights(d, ':'.join(k))

# Flower Patch rewrites equal the generator's conversion
fp = cfg['flower_patch']
shares = f.existing_shares(cfg, snap)
patched = 0
for c, data in snap['configured'].items():
    ns, path = c.split(':', 1)
    got = files.get((ns, f'worldgen/configured_feature/{path}.json'))
    st = {'converted': 0, 'skipped': 0}
    exp = f.convert_json(data, fp, shares.get(c, 0), st)
    if not st['converted'] or shares.get(c, 0) <= 0:
        if got is not None:
            bad.append(f'{c}: rewritten but nothing to convert')
        continue
    patched += 1
    if got != exp:
        bad.append(f'{c}: Flower Patch rewrite differs from the generator')
for pid, data in snap['placed'].items():
    ns, path = pid.split(':', 1)
    got = files.get((ns, f'worldgen/placed_feature/{path}.json'))
    spec = cfg.get('existing', {}).get(pid) or {}
    share = float(spec.get('flower_patch', fp.get('existing_share', 0)))
    exp = dict(data)
    changed = False
    if isinstance(data.get('feature'), dict) and share > 0:
        st = {'converted': 0, 'skipped': 0}
        exp['feature'] = f.convert_json(data['feature'], fp, share, st)
        changed |= st['converted'] > 0
    scale = float(spec.get('scale', 1))
    if scale > 0 and abs(scale - 1) > 1e-9:
        exp['placement'] = f.scale_placement(data.get('placement', []), scale)
        changed = True
    if scale <= 0 or not changed:
        if got is not None:
            bad.append(f'{pid}: placed feature rewritten without a change')
    elif got != exp:
        bad.append(f'{pid}: placed feature rewrite differs from the generator')

want_nones = set((cfg.get('botania') or {}).get('disable_modifiers', []))
for e in cfg['flowers'].values():
    want_nones |= set(e.get('disable_modifiers', []))
if nones != len(want_nones):
    bad.append(f'{nones} forge:none files, expected {len(want_nones)}')
if adds != len([t for t in want if '/remove/' not in t]):
    bad.append(f'{adds} add modifiers for {len(want)} tags')
for b in bad:
    print('MISMATCH ' + b)
print(f'{len(want)} tags match the generator; {adds} add, {nones} forge:none, {removes} remove modifiers; '
      f'{patched} configured features with Flower Patch blocks match' if not bad else f'{len(bad)} problems')
sys.exit(1 if bad else 0)
PY

echo "== 3. switch off"
java -cp "$T/cls:$RHINO:$GSON" RhinoWildFlowers "$ROOT" "$T/off" off | tail -1 | grep -q '^0 tags, 0 data files' && echo "off: nothing applied"

echo "== 4. Botania missing"
java -cp "$T/cls:$RHINO:$GSON" RhinoWildFlowers "$ROOT" "$T/miss" missing=botania > "$T/miss.log"
grep -q 'skipped botania' "$T/miss.log" && ! grep -rq 'botania' "$T/miss/data" \
  && [ "$(find "$T/miss/data" -name '*.json' | wc -l)" -lt "$(find "$T/on/data" -name '*.json' | wc -l)" ] \
  && echo "missing Botania: its features and modifier skipped"

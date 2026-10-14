#!/usr/bin/env python3
"""
Shape check for the worldgen JSON in kubejs/data against the 1.19.2 codecs: the keys 1.19.2 requires and later
versions dropped or renamed. A missing key fails the whole world load ("No key name in MapLike..."), and nothing
else offline decodes these files. Run: python3 tools/check_worldgen_json.py (exit 1 on a problem).
"""
import glob
import json
import sys

REQUIRED = {
    'template_pool': ['name', 'fallback', 'elements'],          # 1.20 dropped "name"
    'structure_set': ['structures', 'placement'],
}
JIGSAW = ['biomes', 'spawn_overrides', 'step', 'start_pool', 'size', 'start_height']
bad = 0
n = 0
for f in sorted(glob.glob('kubejs/data/*/worldgen/**/*.json', recursive=True)):
    kind = f.split('/worldgen/')[1].split('/')[0]
    if kind not in REQUIRED and kind != 'structure':
        continue
    n += 1
    try:
        j = json.load(open(f))
    except Exception as e:
        print(f'FAIL {f}: not JSON ({e})'); bad += 1; continue
    need = REQUIRED.get(kind) or (JIGSAW if j.get('type') == 'minecraft:jigsaw' else [])
    missing = [k for k in need if k not in j]
    if kind == 'template_pool':
        for e in j.get('elements', []):
            if 'element' not in e or 'weight' not in e:
                missing.append('elements[].element/weight')
                break
    if missing:
        print(f'FAIL {f}: missing {", ".join(missing)}'); bad += 1
print(f'{n} worldgen file(s) checked, {bad} problem(s)')
sys.exit(1 if bad else 0)

#!/usr/bin/env python3
"""Give Upgrade Aquatic's corals Immersive Weathering's coral growth.

IW grows fans, wall fans, coral and the odd wet sponge into the water around each vanilla coral block, from one
data file per kind (data/immersive_weathering/block_growths/<kind>_coral.json; the five differ only in the kind name).
This copies IW's brain_coral.json once per living Upgrade Aquatic coral kind (every <kind> with a _coral_block,
_coral, _coral_fan and _coral_wall_fan blockstate, dead kinds excluded) into
kubejs/data/immersive_weathering/block_growths/ua_<kind>_coral.json, swapping the block names. Chance, crowding
limit, weights and the 0.8 water match stay IW's.

Dead kinds do not grow, as in IW: the dead_ ones, and elder prismarine, which is prismarine coral's dead form.

Biome: the reef kinds keep IW's warm ocean. Prismarine coral generates in every ocean (UA's prismarine_coral
feature, #minecraft:is_ocean), so it grows there.

UA's own recipes already turn 4 corals or fans into each block, so IW's coral block recipes are not copied.
Owns every ua_*_coral.json in that folder: stale ones are deleted.

  python3 tools/gen_ua_coral_growths.py [--dry-run]

Apply: /reload (IW reads block growths at data load).
"""
import argparse
import glob
import json
import sys
import zipfile
from pathlib import Path

INSTANCE = Path(__file__).resolve().parent.parent
OUT = INSTANCE / 'kubejs/data/immersive_weathering/block_growths'
TEMPLATE = 'data/immersive_weathering/block_growths/brain_coral.json'
UA_STATES = 'assets/upgrade_aquatic/blockstates/'
BIOMES = {'prismarine': '#minecraft:is_ocean'}
DEAD = {'elder_prismarine'}


def jar(pattern):
    jars = sorted(glob.glob(str(INSTANCE / 'mods' / pattern)))
    if len(jars) != 1:
        sys.exit(f'expected one mods/{pattern}, found {len(jars)}')
    return zipfile.ZipFile(jars[0])


def ua_kinds():
    names = {n[len(UA_STATES):-5] for n in jar('upgrade_aquatic-*.jar').namelist()
             if n.startswith(UA_STATES) and n.endswith('.json')}
    return sorted(n[:-len('_coral_block')] for n in names
                  if n.endswith('_coral_block') and not n.startswith('dead_')
                  and n[:-len('_coral_block')] not in DEAD
                  and all(n[:-len('_block')] + s in names for s in ('', '_fan', '_wall_fan')))


def growth(template, kind):
    text = template.replace('minecraft:brain_coral', f'upgrade_aquatic:{kind}_coral')
    body = json.loads(text)
    if kind in BIOMES:
        for p in body['position_predicates']:
            if p['type'] == 'biome_match':
                p['biomes'] = BIOMES[kind]
    return body


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--dry-run', action='store_true')
    args = ap.parse_args()

    template = jar('immersive_weathering-*.jar').read(TEMPLATE).decode()
    kinds = ua_kinds()
    writes = {f'ua_{k}_coral.json': growth(template, k) for k in kinds}
    stale = sorted(p.name for p in OUT.glob('ua_*_coral.json') if p.name not in writes)

    for k in kinds:
        print(f'  {k:18} {BIOMES.get(k, "warm ocean")}')
    for name in stale:
        print(f'  removed {name} (no such kind any more)')
    if args.dry_run:
        print(f'dry run: {len(writes)} growths, nothing written')
        return 0

    OUT.mkdir(parents=True, exist_ok=True)
    for name in stale:
        (OUT / name).unlink()
    for name, body in writes.items():
        (OUT / name).write_text(json.dumps(body, indent=2) + '\n')
    print(f'wrote {len(writes)} growths to {OUT.relative_to(INSTANCE)}')
    return 0


if __name__ == '__main__':
    sys.exit(main())

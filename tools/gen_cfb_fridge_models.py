#!/usr/bin/env python3
"""Reshape Cooking for Blockheads' 2-tall fridge so it reads as a full block (TODO cooking/kitchen #6).

The 13.3.4 fridge body is x 1-15, z 3-15 and its upper half stops at y 13, so it leaves gaps beside counters and
decor on top floats 3 px above it. This reads CfB's own models from the jar in mods/ and writes overrides to
kubejs/assets/cookingforblockheads/models/block/ with:

  - body walls out to x 0 / 16 and back to z 16, the top at y 16 (upper) and full-height walls (lower);
  - seals, shelves and the inside moved out with the walls;
  - doors x 0-16 and full height, at their old depth (z 1-2), so FridgeRenderer's hinge pivot (x 14.5 or 1.5,
    z 1.5) still swings them; handles unchanged.

Faces of stretched elements lose their explicit uv and take position UVs (so the textures don't squash); seals and
handles keep theirs (they pick single texture pixels). tintindex is kept, so dyed fridges still colour. The 1-tall
fridge (block/fridge) is left as it is. Run from anywhere: python3 tools/gen_cfb_fridge_models.py [--check]
"""
import copy
import glob
import json
import os
import sys
import zipfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, 'kubejs', 'assets', 'cookingforblockheads', 'models', 'block')
SRC = 'assets/cookingforblockheads/models/block/'

# element name -> new [from, to]; None keeps it. Elements not listed are kept unchanged.
UPPER = {
    'Static_Right': ([0, 0, 3], [2, 16, 16]),
    'Static_Left': ([14, 0, 3], [16, 16, 16]),
    'Static_Top': ([2, 14, 3], [14, 16, 16]),
    'Static_Back': ([2, 0, 12.5], [14, 14, 16]),
    'Static_SealRight': ([0.5, 0, 2], [1.5, 15.5, 3]),
    'Static_SealLeft': ([14.5, 0, 2], [15.5, 15.5, 3]),
    'Static_SealTop': ([1.5, 14.5, 2], [14.5, 15.5, 3]),
    'Static_TopShelf': ([2, 4.5, 3.5], [14, 5.5, 12.5]),
}
LOWER = {
    'Static_Bottom': ([0, 0, 3], [16, 2, 16]),
    'Static_Right': ([0, 2, 3], [2, 16, 16]),
    'Static_Left': ([14, 2, 3], [16, 16, 16]),
    'Static_Back': ([2, 2, 12.5], [14, 16, 16]),
    'Static_SealRight': ([0.5, 0.5, 2], [1.5, 16, 3]),
    'Static_SealLeft': ([14.5, 0.5, 2], [15.5, 16, 3]),
    'Static_SealBottom': ([1.5, 0.5, 2], [14.5, 1.5, 3]),
    'Static_InsideBottom': ([2, 2, 4], [14, 8.5, 12.5]),
    'Static_BottomShelf': ([2, 8.5, 3.5], [14, 9.5, 12.5]),
    'Static_MiddleShelf': ([2, 14.5, 3.5], [14, 15.5, 12.5]),
}
DOOR = {'Door_Main': ([0, 0, 1], [16, 16, 2])}

MODELS = {
    'fridge_large_upper': UPPER,
    'fridge_large_lower': LOWER,
    'fridge_large_door_upper': DOOR,
    'fridge_large_door_upper_flipped': DOOR,
    'fridge_large_door_lower': DOOR,
    'fridge_large_door_lower_flipped': DOOR,
}


def keeps_uv(name):
    return 'Seal' in name or 'Handle' in name


def reshape(model, moves):
    out = copy.deepcopy(model)
    out['credit'] = ('Cooking for Blockheads model reshaped to a full block by tools/gen_cfb_fridge_models.py '
                     '(CreativeEngineering, TODO cooking/kitchen #6)')
    seen = set()
    for e in out['elements']:
        name = e.get('name')
        if name not in moves:
            continue
        seen.add(name)
        e['from'], e['to'] = moves[name]
        if not keeps_uv(name):
            for face in e['faces'].values():
                face.pop('uv', None)
    missing = set(moves) - seen
    if missing:
        sys.exit(f'element(s) {sorted(missing)} not found; did CfB change its models?')
    return out


def main():
    jars = glob.glob(os.path.join(ROOT, 'mods', 'cookingforblockheads-*.jar'))
    if len(jars) != 1:
        sys.exit(f'expected one cookingforblockheads jar in mods/, found {jars}')
    check = '--check' in sys.argv
    stale = []
    with zipfile.ZipFile(jars[0]) as z:
        for name, moves in MODELS.items():
            text = json.dumps(reshape(json.loads(z.read(SRC + name + '.json')), moves), indent='\t') + '\n'
            path = os.path.join(OUT, name + '.json')
            if check:
                if not os.path.exists(path) or open(path).read() != text:
                    stale.append(name)
                continue
            os.makedirs(OUT, exist_ok=True)
            with open(path, 'w') as f:
                f.write(text)
            print('wrote', os.path.relpath(path, ROOT))
    if check:
        print('stale: ' + ', '.join(stale) if stale else 'fridge models up to date')
        sys.exit(1 if stale else 0)


if __name__ == '__main__':
    main()

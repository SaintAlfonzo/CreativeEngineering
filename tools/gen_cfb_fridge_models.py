#!/usr/bin/env python3
"""Fill Cooking for Blockheads' 2-tall fridge up to a full block (TODO cooking/kitchen #6, #19).

The 13.3.4 fridge's upper half stops at y 13, so decor on top floats 3 px above it. This reads CfB's own
fridge_large_upper model from the jar in mods/ and writes an override to
kubejs/assets/cookingforblockheads/models/block/ that is CfB's model unchanged plus one solid box over the fridge's
outline (x 1-15, z 1-15: the body and the closed door's front) from y 13 to y 16. The box samples the white inside of
CfB's own fridge_side (inside its 1 px dark border) with tintindex 0, so a dyed fridge's box takes the dye too.
CfB bakes this model through Balm's loadDynamicModel, which never stitches textures of its own: a texture only this
model names renders as missing (the old creativeengineering:block/fridge_top_fill did), so the box must reuse a
texture CfB's blockstate models already put on the block atlas.

The earlier version stretched the body walls and doors out to a full block; that reshaping is gone (its five other
overrides are deleted by this script), and the lower half and doors are CfB's own again. The 1-tall fridge
(block/fridge) is left as it is. Run from anywhere: python3 tools/gen_cfb_fridge_models.py [--check]
"""
import copy
import glob
import json
import os
import sys
import zipfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, 'kubejs', 'assets', 'cookingforblockheads', 'models', 'block')
# the old flat-white texture, never stitched (see above); deleted by this script
TEX = os.path.join(ROOT, 'kubejs', 'assets', 'creativeengineering', 'textures', 'block', 'fridge_top_fill.png')
SRC = 'assets/cookingforblockheads/models/block/'

MODEL = 'fridge_large_upper'
# overrides written by the reshaping version, now removed so CfB's own models load
OLD = ['fridge_large_lower', 'fridge_large_door_upper', 'fridge_large_door_upper_flipped',
       'fridge_large_door_lower', 'fridge_large_door_lower_flipped']

FILL_FROM, FILL_TO = [1, 13, 1], [15, 16, 15]
# fridge_side's white inside: the full 14 x 14 for top and bottom, a 14 x 3 strip for the sides
UV_FLAT, UV_SIDE = [1, 1, 15, 15], [1, 1, 15, 4]


def fill_element():
    # down shows over the door's gap when it swings open
    faces = {d: {'uv': UV_FLAT if d in ('up', 'down') else UV_SIDE, 'texture': '#fridge_side', 'tintindex': 0}
             for d in ('north', 'east', 'south', 'west', 'up', 'down')}
    faces['up']['cullface'] = 'up'
    return {'name': 'CE_TopFill', 'from': FILL_FROM, 'to': FILL_TO, 'faces': faces}


def build(model):
    out = copy.deepcopy(model)
    out['credit'] = ('Cooking for Blockheads model with a top fill to a full block by tools/gen_cfb_fridge_models.py '
                     '(CreativeEngineering, TODO cooking/kitchen #6, #19)')
    out['elements'].append(fill_element())
    return out


def main():
    jars = glob.glob(os.path.join(ROOT, 'mods', 'cookingforblockheads-*.jar'))
    if len(jars) != 1:
        sys.exit(f'expected one cookingforblockheads jar in mods/, found {jars}')
    check = '--check' in sys.argv
    with zipfile.ZipFile(jars[0]) as z:
        text = json.dumps(build(json.loads(z.read(SRC + MODEL + '.json'))), indent='\t') + '\n'
    path = os.path.join(OUT, MODEL + '.json')
    if check:
        stale = []
        if not os.path.exists(path) or open(path).read() != text:
            stale.append(MODEL)
        if os.path.exists(TEX):
            stale.append('fridge_top_fill.png (should be deleted)')
        stale += [n + ' (should be deleted)' for n in OLD if os.path.exists(os.path.join(OUT, n + '.json'))]
        print('stale: ' + ', '.join(stale) if stale else 'fridge models up to date')
        sys.exit(1 if stale else 0)
    os.makedirs(OUT, exist_ok=True)
    with open(path, 'w') as f:
        f.write(text)
    print('wrote', os.path.relpath(path, ROOT))
    if os.path.exists(TEX):
        os.remove(TEX)
        print('deleted', os.path.relpath(TEX, ROOT))
    for n in OLD:
        p = os.path.join(OUT, n + '.json')
        if os.path.exists(p):
            os.remove(p)
            print('deleted', os.path.relpath(p, ROOT))


if __name__ == '__main__':
    main()

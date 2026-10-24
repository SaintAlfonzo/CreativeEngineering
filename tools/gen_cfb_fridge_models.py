#!/usr/bin/env python3
"""Fill Cooking for Blockheads' 2-tall fridge up to a full block (TODO cooking/kitchen #6, #19).

The 13.3.4 fridge's upper half stops at y 13, so decor on top floats 3 px above it. This reads CfB's own
fridge_large_upper model from the jar in mods/ and writes an override to
kubejs/assets/cookingforblockheads/models/block/ that is CfB's model unchanged plus one solid box over the fridge's
outline (x 1-15, z 1-15: the body and the closed door's front) from y 13 to y 16. The box uses a flat white texture
(creativeengineering:block/fridge_top_fill, the body's own white) with tintindex 0, so a dyed fridge's box takes the
dye too.

The earlier version stretched the body walls and doors out to a full block; that reshaping is gone (its five other
overrides are deleted by this script), and the lower half and doors are CfB's own again. The 1-tall fridge
(block/fridge) is left as it is. Run from anywhere: python3 tools/gen_cfb_fridge_models.py [--check]
"""
import copy
import glob
import json
import os
import struct
import sys
import zipfile
import zlib

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, 'kubejs', 'assets', 'cookingforblockheads', 'models', 'block')
TEX = os.path.join(ROOT, 'kubejs', 'assets', 'creativeengineering', 'textures', 'block', 'fridge_top_fill.png')
SRC = 'assets/cookingforblockheads/models/block/'

MODEL = 'fridge_large_upper'
# overrides written by the reshaping version, now removed so CfB's own models load
OLD = ['fridge_large_lower', 'fridge_large_door_upper', 'fridge_large_door_upper_flipped',
       'fridge_large_door_lower', 'fridge_large_door_lower_flipped']

FILL_FROM, FILL_TO = [1, 13, 1], [15, 16, 15]
WHITE = (246, 246, 246, 255)


def fill_element():
    # down shows over the door's gap when it swings open
    faces = {d: {'texture': '#ce_top_fill', 'tintindex': 0} for d in ('north', 'east', 'south', 'west', 'up', 'down')}
    faces['up']['cullface'] = 'up'
    return {'name': 'CE_TopFill', 'from': FILL_FROM, 'to': FILL_TO, 'faces': faces}


def build(model):
    out = copy.deepcopy(model)
    out['credit'] = ('Cooking for Blockheads model with a top fill to a full block by tools/gen_cfb_fridge_models.py '
                     '(CreativeEngineering, TODO cooking/kitchen #6, #19)')
    out['textures']['ce_top_fill'] = 'creativeengineering:block/fridge_top_fill'
    out['elements'].append(fill_element())
    return out


def png(rgba):
    row = b'\x00' + bytes(rgba) * 16
    def chunk(kind, data):
        return struct.pack('>I', len(data)) + kind + data + struct.pack('>I', zlib.crc32(kind + data) & 0xffffffff)
    return (b'\x89PNG\r\n\x1a\n' + chunk(b'IHDR', struct.pack('>IIBBBBB', 16, 16, 8, 6, 0, 0, 0))
            + chunk(b'IDAT', zlib.compress(row * 16)) + chunk(b'IEND', b''))


def main():
    jars = glob.glob(os.path.join(ROOT, 'mods', 'cookingforblockheads-*.jar'))
    if len(jars) != 1:
        sys.exit(f'expected one cookingforblockheads jar in mods/, found {jars}')
    check = '--check' in sys.argv
    with zipfile.ZipFile(jars[0]) as z:
        text = json.dumps(build(json.loads(z.read(SRC + MODEL + '.json'))), indent='\t') + '\n'
    path = os.path.join(OUT, MODEL + '.json')
    tex = png(WHITE)
    if check:
        stale = []
        if not os.path.exists(path) or open(path).read() != text:
            stale.append(MODEL)
        if not os.path.exists(TEX) or open(TEX, 'rb').read() != tex:
            stale.append('fridge_top_fill.png')
        stale += [n + ' (should be deleted)' for n in OLD if os.path.exists(os.path.join(OUT, n + '.json'))]
        print('stale: ' + ', '.join(stale) if stale else 'fridge models up to date')
        sys.exit(1 if stale else 0)
    os.makedirs(OUT, exist_ok=True)
    with open(path, 'w') as f:
        f.write(text)
    print('wrote', os.path.relpath(path, ROOT))
    os.makedirs(os.path.dirname(TEX), exist_ok=True)
    with open(TEX, 'wb') as f:
        f.write(tex)
    print('wrote', os.path.relpath(TEX, ROOT))
    for n in OLD:
        p = os.path.join(OUT, n + '.json')
        if os.path.exists(p):
            os.remove(p)
            print('deleted', os.path.relpath(p, ROOT))


if __name__ == '__main__':
    main()

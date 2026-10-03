#!/usr/bin/env python3
"""Missing Display Delight models -> kubejs/assets/displaydelight/models/.

Display Delight 1.2.0 registers 121 block items but ships item models for only 32 of them: the
plated_*/small_plated_* items lost theirs when the blocks were renamed (the jar still has
models/item/hamburger.json, not plated_hamburger.json), and the newer stews, drinks and mod-compat
foods never had one. Those 89 items render as the purple/black missing-model cube in JEI and in the
WTHIT icon, because WTHIT asks the block for its vanilla clone stack (the DD block item).

For each item with no item model this writes models/item/<id>.json parented on the block model of its
default state (facing=north, stacks=1), the same way DD's own item models are written. Two plated
models have no gui transform anywhere in their parent chain, so those item models carry the transform
the other plated models use.

It also rewrites the block models that point a face at the undefined texture '#missing' (pumpkin soup,
cactus soup, pufferfish broth): each such face takes the texture of its element's top face.

Item ids come from /dumpitemsbymod (kubejs/exported/items_by_mod/displaydelight.json).

    python3 tools/gen_display_delight_models.py           write the models
    python3 tools/gen_display_delight_models.py --check   exit 1 if any file is missing or out of date
"""
import argparse
import json
import re
import sys
import zipfile

import lang_index

OUT = lang_index.INSTANCE / 'kubejs/assets/displaydelight/models'
ITEMS = lang_index.INSTANCE / 'kubejs/exported/items_by_mod/displaydelight.json'
JAR_GLOB = 'displaydelight-*.jar'
ASSETS = 'assets/displaydelight/'
# The gui transform most plated block models use (plated_hamburger, plated_cod_roll_1, ...).
PLATED_GUI = {'rotation': [37, 44, 0], 'translation': [0, 4, 0], 'scale': [0.8, 0.8, 0.8]}


def read_jar():
    jars = sorted(lang_index.MODS.glob(JAR_GLOB))
    if not jars:
        sys.exit('no %s in mods/' % JAR_GLOB)
    files = {}
    with zipfile.ZipFile(jars[-1]) as z:
        for name in z.namelist():
            if name.startswith(ASSETS) and name.endswith('.json') and '/lang/' not in name:
                files[name[len(ASSETS):-len('.json')]] = json.loads(z.read(name))
    return jars[-1].name, files


def default_model(blockstate):
    """Model of the facing=north, stacks=1 state (variant keys may leave stacks out)."""
    for key, variant in blockstate.get('variants', {}).items():
        props = dict(p.split('=') for p in key.split(',') if p)
        if props.get('facing', 'north') == 'north' and props.get('stacks', '1') == '1':
            v = variant[0] if isinstance(variant, list) else variant
            return v['model']
    return None


def has_gui(files, model):
    while model and model.startswith('displaydelight:'):
        m = files.get('models/' + model.split(':', 1)[1])
        if m is None:
            return False
        if 'gui' in m.get('display', {}):
            return True
        model = m.get('parent')
    # A vanilla parent (block/block, item/generated) brings its own gui transform.
    return model is not None


def fix_missing_faces(model):
    """Copy of a block model with every '#missing' face retextured from its element's top face."""
    fixed, changed = json.loads(json.dumps(model)), False
    for element in fixed.get('elements', []):
        faces = element.get('faces', {})
        donor = faces.get('up', {}).get('texture')
        if donor in (None, '#missing'):
            donor = next((f['texture'] for f in faces.values() if f.get('texture') not in (None, '#missing')), None)
        for face in faces.values():
            if face.get('texture') == '#missing' and donor:
                face['texture'] = donor
                changed = True
    return fixed if changed else None


def build():
    jar, files = read_jar()
    if not ITEMS.exists():
        sys.exit('%s missing, run /dumpitemsbymod displaydelight in game' % ITEMS.relative_to(lang_index.INSTANCE))
    items = [i.split(':', 1)[1] for i in json.loads(ITEMS.read_text())['items']]
    out, problems = {}, []
    for item in items:
        if 'models/item/' + item in files:
            continue
        blockstate = files.get('blockstates/' + item)
        model = default_model(blockstate) if blockstate else None
        if model is None:
            problems.append('%s: no blockstate default model' % item)
            continue
        if 'models/' + model.split(':', 1)[1] not in files:
            problems.append('%s: block model %s not in the jar' % (item, model))
            continue
        entry = {'parent': model}
        if not has_gui(files, model):
            entry['display'] = {'gui': PLATED_GUI}
        out['item/' + item] = entry
    for path, model in files.items():
        if path.startswith('models/block/') and '"#missing"' in json.dumps(model):
            fixed = fix_missing_faces(model)
            if fixed:
                out[path[len('models/'):]] = fixed
    return jar, out, problems


def render(model):
    return json.dumps(model, indent=1) + '\n'


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--check', action='store_true')
    args = ap.parse_args()
    jar, out, problems = build()
    stale = [p for p, m in out.items() if not (OUT / (p + '.json')).exists()
             or (OUT / (p + '.json')).read_text() != render(m)]
    for p in problems:
        print('WARNING ' + p)
    if args.check:
        if stale:
            print('%d model(s) missing or out of date, run python3 tools/gen_display_delight_models.py' % len(stale))
            return 1
        print('up to date (%d models)' % len(out))
        return 0
    for p in stale:
        f = OUT / (p + '.json')
        f.parent.mkdir(parents=True, exist_ok=True)
        f.write_text(render(out[p]))
    items = sum(1 for p in out if p.startswith('item/'))
    print('%s: %d item models, %d block models with #missing faces fixed; wrote %d file(s) under %s'
          % (jar, items, len(out) - items, len(stale), OUT.relative_to(lang_index.INSTANCE)))
    return 0


if __name__ == '__main__':
    sys.exit(main())

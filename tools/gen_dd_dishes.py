#!/usr/bin/env python3
"""Resources for patcherMod's Display Delight dishes (compat/DisplayDelightDishes) from their list.

The list is development/patcherMod/src/main/resources/creativeengineering/displaydelight_dishes.json. For each entry
this writes, into patcherMod's resources:
  assets/creativeengineering/blockstates/<block>.json     four facings, like Display Delight's own food blocks
  assets/creativeengineering/models/block/<block>.json    bowl: Display Delight's bowl (displaydelight:block/bowl)
                                                          with a food surface cut from the item sprite (crop);
                                                          plate: Display Delight's food plate with the whole sprite
                                                          laid flat on it (cutout)
  data/creativeengineering/loot_tables/blocks/<block>.json  drops the food (survives explosion), as Display Delight's
  data/displaydelight/tags/items/displayable.json         every dish food (optional entries), so Display Delight
                                                          lets it be put down
  assets/creativeengineering/lang/en_us.json              "Placed <food name>" per block (other keys kept)
The item sprite comes from the food's item model (layer0) in its mod jar, or for a KubeJS item its sprite in
kubejs/assets; names from the /dumpfood export, else the entry's "name".
Display Delight's models and textures are referenced, not copied. Output goes into the patcherMod jar, so a change
needs a patcherMod rebuild (not listed in generators.json for that reason).

    python3 tools/gen_dd_dishes.py           write the files
    python3 tools/gen_dd_dishes.py --check   exit 1 if any file is missing or stale
"""
import argparse
import glob
import json
import sys
import zipfile
from collections import OrderedDict
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import display_delight_jar as ddj  # noqa: E402  (the Display Delight jar in mods/)

INSTANCE = Path(__file__).resolve().parent.parent
RES = INSTANCE / 'development/patcherMod/src/main/resources'
LIST = RES / 'creativeengineering/displaydelight_dishes.json'
LANG = RES / 'assets/creativeengineering/lang/en_us.json'
TAG = RES / 'data/displaydelight/tags/items/displayable.json'
FOODS = INSTANCE / 'kubejs/exported/food/foods.json'



def dd_model(name):
    """A model JSON from the Display Delight jar in mods/."""
    with ddj.open_jar() as z:
        return json.loads(z.read(f'assets/displaydelight/models/block/{name}.json'))


def item_sprite(item_id):
    """layer0 of the item's model, read from whichever mod jar ships it."""
    ns, path = item_id.split(':', 1)
    entry = f'assets/{ns}/models/item/{path}.json'
    for jar in sorted(glob.glob(str(INSTANCE / 'mods' / '*.jar'))) + sorted(glob.glob(str(INSTANCE / 'server_mods' / '*.jar'))):
        with zipfile.ZipFile(jar) as z:
            if entry in z.namelist():
                tex = json.loads(z.read(entry)).get('textures', {}).get('layer0')
                if tex:
                    return tex if ':' in tex else 'minecraft:' + tex
    # A KubeJS item has no model in a jar: its generated model's layer0 is <ns>:item/<path>, the sprite in kubejs/assets
    if (INSTANCE / 'kubejs/assets' / ns / 'textures/item' / f'{path}.png').exists():
        return f'{ns}:item/{path}'
    sys.exit(f'no item model with layer0 for {item_id}')


def bowl_model(sprite, crop):
    # Display Delight's bowl: bottom 5..11 at y 0..1, walls up to y 4. The food surface sits inside the walls, its
    # top a crop of the sprite.
    bowl = dd_model('bowl')
    elements = list(bowl['elements'])
    elements.append({'name': 'food', 'from': [5, 1, 5], 'to': [11, 3, 11],
                     'faces': {'up': {'uv': crop, 'texture': '#food'}}})
    # The bowl's own texture keys (1.2.0: bowl, 1.7.0: salad_bowl), then the food
    textures = OrderedDict((k, v) for k, v in bowl['textures'].items() if k != 'particle')
    textures['food'] = sprite
    textures['particle'] = sprite
    return OrderedDict([('parent', 'displaydelight:block/parent_bowl'), ('render_type', 'cutout'),
                        ('textures', textures), ('elements', elements)])


def plate_model(sprite):
    plate = dd_model('food_plate')
    textures = OrderedDict(plate['textures'])
    textures['food'] = sprite
    textures['particle'] = sprite
    elements = list(plate['elements'])
    # The sprite flat on the tray (2 px high), slightly raised so it never z-fights the tray top
    elements.append({'name': 'food', 'from': [2, 2.05, 2], 'to': [14, 2.05, 14],
                     'faces': {'up': {'uv': [0, 0, 16, 16], 'texture': '#food'},
                               'down': {'uv': [0, 16, 16, 0], 'texture': '#food'}}})
    return OrderedDict([('parent', 'displaydelight:block/parent_bowl'), ('render_type', 'cutout'),
                        ('textures', textures), ('elements', elements)])


def blockstate(block):
    model = f'creativeengineering:block/{block}'
    return {'variants': OrderedDict([('facing=north', {'model': model}), ('facing=south', {'model': model, 'y': 180}),
                                     ('facing=west', {'model': model, 'y': 270}), ('facing=east', {'model': model, 'y': 90})])}


def loot(item_id):
    return {'type': 'minecraft:block', 'pools': [{'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': item_id}],
                                                  'conditions': [{'condition': 'minecraft:survives_explosion'}]}]}


def dump(obj):
    return json.dumps(obj, indent=2, ensure_ascii=False) + '\n'


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--check', action='store_true')
    a = ap.parse_args()

    dishes = json.loads(LIST.read_text())['dishes']
    names = {f['id']: f['name'] for f in json.loads(FOODS.read_text())['foods']}
    out = OrderedDict()
    for d in dishes:
        block, item = d['block'], d['item']
        sprite = item_sprite(item)
        model = bowl_model(sprite, d.get('crop', [4, 5, 12, 10])) if d['dish'] == 'bowl' else plate_model(sprite)
        out[RES / f'assets/creativeengineering/blockstates/{block}.json'] = dump(blockstate(block))
        out[RES / f'assets/creativeengineering/models/block/{block}.json'] = dump(model)
        out[RES / f'data/creativeengineering/loot_tables/blocks/{block}.json'] = dump(loot(item))
    out[TAG] = dump({'replace': False, 'values': [{'id': d['item'], 'required': False} for d in dishes]})
    lang = json.loads(LANG.read_text(), object_pairs_hook=OrderedDict)
    for d in dishes:
        lang[f"block.creativeengineering.{d['block']}"] = 'Placed ' + names.get(d['item'], d.get('name', d['item']))
    out[LANG] = json.dumps(lang, indent=2, ensure_ascii=False) + '\n'

    stale = [p for p, text in out.items() if not p.exists() or p.read_text() != text]
    if a.check:
        for p in stale:
            print('stale:', p.relative_to(INSTANCE))
        print(f'{len(dishes)} dishes, {len(stale)} stale files')
        sys.exit(1 if stale else 0)
    for p in stale:
        p.parent.mkdir(parents=True, exist_ok=True)
        p.write_text(out[p])
    print(f'{len(dishes)} dishes: wrote {len(stale)} of {len(out)} files')


if __name__ == '__main__':
    main()

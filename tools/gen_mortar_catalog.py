#!/usr/bin/env python3
"""Catalog of other mods' food grinding recipes for the mortar and pestle (TODO cooking/kitchen #10).

Reads the /dumpfood recipe export (kubejs/exported/food/recipes.json: every loaded recipe with its ingredients and
outputs) and writes kubejs/generation_scripts/food/mortar_catalog.json, which kubejs/server_scripts/cooking/mortar.js
loops through with R.mortar. A recipe is catalogued when:

  - it is Create milling or crushing (IE crusher recipes export without ingredients, so they can't be read);
  - it is another mod's (creativeengineering:* recipes get their mortar craft from R.foodGrinding instead) and not a
    Create scrap-recycling recipe;
  - it takes one item or tag;
  - its main (first) output is a food ingredient: in a forge flour / sugar / salt / grain / crops tag, or in ALLOW;
    and not in EXCLUDE.

The mortar's ratio mirrors R.foodGrinding: E = sum of count x chance of the main output; E >= 1 gives floor(E) from
one input, E < 1 gives 1 from ceil(1 / E) inputs (at most 8), with 0.02 slack. Byproducts are dropped. When milling
and crushing both turn the same input into the same output, the higher E wins. Ids are written as item_dict keys
where one exists (inputKey / outputKey), else raw. Everything looked at and skipped is listed with a reason.

Run: python3 tools/gen_mortar_catalog.py   (after /dumpfood, also /cegen run mortar_catalog)
"""
import json
import math
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RECIPES = os.path.join(ROOT, 'kubejs', 'exported', 'food', 'recipes.json')
TAGS = os.path.join(ROOT, 'kubejs', 'exported', 'tags', 'item_tags_by_item.json')
DICT = os.path.join(ROOT, 'kubejs', 'startup_scripts', '_lib', 'item_dict.js')
OUT = os.path.join(ROOT, 'kubejs', 'generation_scripts', 'food', 'mortar_catalog.json')

TYPES = ('create:milling', 'create:crushing')
FOOD_TAGS = ('forge:flour', 'forge:sugar', 'forge:salt', 'forge:grain', 'forge:crops')
ALLOW = {
    'farmersrespite:coffee_beans': 'coffee beans (untagged)',
    'create_confectionery:cocoa_powder': 'cocoa powder (untagged)',
}
EXCLUDE = {
    'neapolitan:ice_cubes': 'ice is hammered on the cutting board (TODO dishes/ingredients #5), not ground',
    'minecraft:nether_wart': 'nether wart block back to wart is unpacking a decor block, not food grinding',
}


def load_tags():
    by_mod = json.load(open(TAGS))
    flat = {}
    for ns, items in by_mod.items():
        if isinstance(items, dict):
            for k, tags in items.items():
                flat[k if ':' in k else ns + ':' + k] = tags
    return flat


def load_dict_keys():
    keys = {}
    for m in re.finditer(r"^i\.([A-Za-z0-9_]+)\s*=\s*'([^']+)'", open(DICT).read(), re.M):
        keys.setdefault(m.group(2), m.group(1))
    return keys


def ratio(outputs, main):
    e = sum(o.get('n', 1) * o.get('p', 1) for o in outputs if o['id'] == main)
    if e >= 0.98:
        return e, math.floor(e + 0.02), 1
    return e, 1, math.ceil(1 / e - 0.02) if e > 0 else 99


def main():
    recipes = json.load(open(RECIPES))['recipes']
    tags = load_tags()
    keys = load_dict_keys()
    chosen = {}
    skipped = []
    for r in recipes:
        if r['type'] not in TYPES:
            continue
        rid = r['id']

        def skip(why):
            skipped.append({'source': rid, 'reason': why})

        if rid.startswith('creativeengineering:'):
            continue  # own recipes: R.foodGrinding
        if 'scrap' in rid:
            skip('Create scrap recycling')
            continue
        ins = [json.loads(x) for x in r['in']]
        if len(ins) != 1 or not isinstance(ins[0], dict) or not (ins[0].get('item') or ins[0].get('tag')):
            skip('not one item or tag in')
            continue
        main_out = r['out'][0]['id']
        if main_out in EXCLUDE:
            skip(EXCLUDE[main_out])
            continue
        food = main_out in ALLOW or any(t == f or t.startswith(f + '/') for t in tags.get(main_out, []) for f in FOOD_TAGS)
        if not food:
            skip('main output ' + main_out + ' is not a food ingredient')
            continue
        e, count, inputs = ratio(r['out'], main_out)
        if inputs > 8:
            skip('needs %d inputs for one %s' % (inputs, main_out))
            continue
        inp = ins[0].get('item') or '#' + ins[0]['tag']
        entry = {
            'id': re.sub(r'[^a-z0-9_]', '_', rid.split(':', 1)[0] + '_' + rid.split(':', 1)[1].split('/')[-1]),
            'source': rid,
            'input': inp, 'inputKey': keys.get(inp),
            'output': main_out, 'outputKey': keys.get(main_out),
            'count': count, 'inputs': inputs,
            'machineAverage': round(e, 3),
        }
        k = (inp, main_out)
        if k not in chosen or e > chosen[k]['machineAverage']:
            if k in chosen:
                skipped.append({'source': chosen[k]['source'], 'reason': 'same input and output as ' + rid})
            chosen[k] = entry
        else:
            skip('same input and output as ' + chosen[k]['source'])
    out = {
        '_readme': 'Generated by tools/gen_mortar_catalog.py from kubejs/exported/food/recipes.json (/dumpfood). '
                   'Read by kubejs/server_scripts/cooking/mortar.js. Do not edit; change the tool\'s rules.',
        'recipes': sorted(chosen.values(), key=lambda x: x['id']),
        'skipped': sorted(skipped, key=lambda x: x['source']),
    }
    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    with open(OUT, 'w') as f:
        json.dump(out, f, indent=2)
        f.write('\n')
    print('mortar catalog: %d crafts, %d skipped -> %s' % (len(out['recipes']), len(out['skipped']), os.path.relpath(OUT, ROOT)))
    for x in out['recipes']:
        print('  %-45s %d x %s -> %d %s' % (x['source'], x['inputs'], x['input'], x['count'], x['output']))


if __name__ == '__main__':
    sys.exit(main())

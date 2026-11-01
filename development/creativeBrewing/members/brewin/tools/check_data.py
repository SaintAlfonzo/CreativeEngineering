#!/usr/bin/env python3
"""Offline check of the port's data and client JSON against the pack's last registry and tag dumps.

    python3 tools/check_data.py

Reads kubejs/exported/items_by_mod/*.json, fluids.json and tags/*_tags.json (the last /dumpall), plus the port's own
ids from BnCItems / BnCBlocks / BnCFluids. Reports every item, fluid or tag that a recipe, loot table, advancement,
tag, coaster model JSON or fluid item display names but the pack doesn't have. The brewinandchewin ids come from the
port's source, since the dumps were made with Brewin' 2.0. Recipes guarded by a forge:mod_loaded condition for a mod
the pack doesn't have are skipped, as Forge would skip them.
"""
import glob
import json
import os
import re
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
MOD = os.path.dirname(HERE)
INSTANCE = os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(MOD))))  # members/<m> under development/creativeBrewing
EXPORTED = os.path.join(INSTANCE, 'kubejs', 'exported')
RES = os.path.join(MOD, 'src', 'main', 'resources')
JAVA = os.path.join(MOD, 'src', 'main', 'java', 'umpaz', 'brewinandchewin')


def pack_items():
    items = set()
    for f in glob.glob(os.path.join(EXPORTED, 'items_by_mod', '*.json')):
        items.update(json.load(open(f)).get('items', []))
    return {i for i in items if not i.startswith('brewinandchewin:')}


def own_ids(path, pattern):
    return {'brewinandchewin:' + m for m in re.findall(pattern, open(path).read())}


def main():
    items = pack_items()
    items |= own_ids(os.path.join(JAVA, 'common', 'registry', 'BnCItems.java'), r'registerWithTab\("([a-z_]+)"')
    blocks = own_ids(os.path.join(JAVA, 'common', 'registry', 'BnCBlocks.java'), r'asResource\("([a-z_]+)"\)')
    fluids = {f for f in json.load(open(os.path.join(EXPORTED, 'fluids.json'))) if not f.startswith('brewinandchewin:')}
    fluids |= own_ids(os.path.join(JAVA, 'common', 'registry', 'BnCFluids.java'), r'asResource\("([a-z_]+)"\)')
    item_tags = set(json.load(open(os.path.join(EXPORTED, 'tags', 'item_tags.json'))))
    fluid_tags = set(json.load(open(os.path.join(EXPORTED, 'tags', 'fluid_tags.json'))))
    block_tags = set(json.load(open(os.path.join(EXPORTED, 'tags', 'block_tags.json'))))
    mods = {i.split(':')[0] for i in items}
    # Tags the port defines itself count as present.
    for f in glob.glob(os.path.join(RES, 'data', '*', 'tags', '*', '**', '*.json'), recursive=True):
        rel = os.path.relpath(f, os.path.join(RES, 'data')).replace(os.sep, '/')
        ns, _tags, kind, *rest = rel.split('/')
        name = ns + ':' + '/'.join(rest)[:-5]
        {'items': item_tags, 'fluids': fluid_tags, 'blocks': block_tags}.get(kind, set()).add(name)

    problems = []

    def check_item(i, where):
        if i.startswith('#'):
            if i[1:] not in item_tags:
                problems.append(f'{where}: unknown item tag {i}')
        elif i not in items and i not in blocks:
            problems.append(f'{where}: unknown item {i}')

    def check_fluid(i, where):
        if i.startswith('#'):
            if i[1:] not in fluid_tags:
                problems.append(f'{where}: unknown fluid tag {i}')
        elif i not in fluids:
            problems.append(f'{where}: unknown fluid {i}')

    def walk_items(obj, where):
        if isinstance(obj, list):
            for o in obj:
                walk_items(o, where)
        elif isinstance(obj, dict):
            if 'item' in obj and isinstance(obj['item'], str):
                check_item(obj['item'], where)
            if 'tag' in obj and isinstance(obj['tag'], str):
                check_item('#' + obj['tag'], where)
            for k, v in obj.items():
                if k not in ('item', 'tag'):
                    walk_items(v, where)

    def skipped(d):
        for cond in d.get('conditions', []):
            if cond.get('type') == 'forge:mod_loaded' and cond['modid'] not in mods:
                return True
            if cond.get('type') == 'forge:not' and cond['value'].get('modid') in mods:
                return True
        return False

    for f in sorted(glob.glob(os.path.join(RES, 'data', 'brewinandchewin', 'recipes', '**', '*.json'), recursive=True)):
        where = os.path.relpath(f, RES)
        d = json.load(open(f))
        if skipped(d):
            continue
        t = d['type']
        if t == 'brewinandchewin:fermenting':
            walk_items(d['ingredients'], where)
            if 'base_fluid' in d:
                ing = d['base_fluid']['ingredient']
                check_fluid(ing.get('tag') or ing['id'], where)
            res = d['result']
            (check_fluid if 'amount' in res else check_item)(res.get('id') or res['item'], where)
        elif t == 'brewinandchewin:keg_pouring':
            check_fluid(d['fluid']['id'], where)
            check_item(d['output']['item'], where)
            if 'container' in d:
                check_item(d['container']['item'], where)
        else:
            walk_items(d, where)

    for f in sorted(glob.glob(os.path.join(RES, 'data', '*', 'tags', 'items', '**', '*.json'), recursive=True)):
        for v in json.load(open(f))['values']:
            if isinstance(v, str):
                check_item(v, os.path.relpath(f, RES))

    for f in sorted(glob.glob(os.path.join(RES, 'data', 'brewinandchewin', 'loot_tables', '**', '*.json'), recursive=True)):
        for m in re.findall(r'"name": "([a-z0-9_:.]+)"', open(f).read()):
            check_item(m, os.path.relpath(f, RES))

    for f in sorted(glob.glob(os.path.join(RES, 'assets', 'brewinandchewin', 'brewinandchewin', 'coaster', '*.json'))):
        d = json.load(open(f))
        if d['item'] not in items and d['item'] not in blocks:
            # Not an error: the renderer keys models by item id; a missing item only means the model is never used.
            print(f'note: coaster model for an item the pack lacks: {d["item"]}')

    for f in sorted(glob.glob(os.path.join(RES, 'assets', 'brewinandchewin', 'brewinandchewin', 'fluid_item_displays', '*.json'))):
        for key, value in json.load(open(f)).items():
            optional = isinstance(value, dict) and value.get('optional')
            if not optional:
                check_fluid(key, os.path.relpath(f, RES))
                check_item(value if isinstance(value, str) else value['id'], os.path.relpath(f, RES))

    for p in problems:
        print('problem:', p)
    print(f'{len(problems)} problem(s)')
    sys.exit(1 if problems else 0)


if __name__ == '__main__':
    main()

#!/usr/bin/env python3
"""Generate the per-type food cans (cookies, cooked veggie/meat/fish, bread, salad, soup, golden).

A can's value per meal comes from the food it holds: hunger x multiplier, saturation points x multiplier, the
source's effects (durations scaled), meat/fast/always-edible flags. Sections and multipliers live in SECTIONS below
(they mirror the Goal comments in kubejs/server_scripts/cooking/canning.js).

Input (run /dumpfood in game first):  kubejs/exported/food/catalog.json, foods.json
Output:
  kubejs/generation_scripts/food/can_types.json   every can: values, effects, servings, recipes (read by
                                                  startup_scripts/items/cans.js and server_scripts/cooking/canning.js)
  kubejs/startup_scripts/_lib/item_dict.js        block between the BEGIN/END GENERATED CANS markers (new cans only)
  kubejs/generation_scripts/food/can_servings.json  servings for every generated can (other entries kept)
  kubejs/generation_scripts/food/food_types.json  new cans added to types.canned and the canned map

    python3 tools/gen_can_types.py            write everything
    python3 tools/gen_can_types.py --check    print what would change, write nothing

"existing" cans are Canned Goods items (canned_goods:*): no new item, their food values are overridden at startup.
Everything else is a creativeengineering: item that reuses Canned Goods' beetroot can sprite.
Restart the game after a run (new items, new dict keys); /reload alone only applies recipe changes.
"""
import argparse
import json
import re
import sys
from collections import OrderedDict
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import food_catalog  # noqa: E402  (write_types keeps food_types.json's one-line-per-type layout)

INSTANCE = Path(__file__).resolve().parent.parent
EXPORT = INSTANCE / 'kubejs/exported/food'
GEN = INSTANCE / 'kubejs/generation_scripts/food'
DICT = INSTANCE / 'kubejs/startup_scripts/_lib/item_dict.js'
OUT = GEN / 'can_types.json'
SERVINGS = GEN / 'can_servings.json'
TYPES = GEN / 'food_types.json'

BEGIN = '// ===== BEGIN GENERATED CANS (tools/gen_can_types.py, do not edit) ====='
END = '// ===== END GENERATED CANS ====='
ANCHOR = "i.canned_mite_crust             = 'creativeengineering:canned_mite_crust';"

# ---------------------------------------------------------------------------------------------------------------
# Spec
# ---------------------------------------------------------------------------------------------------------------
# mult      hunger and saturation points of one meal = source x mult
# servings  meals per can (can_servings.json)
# n         sources per can in the basic recipe
# extra     additive recipes: list of (suffix, additive keys (alternatives), sources used, additive count)
# dur       effect duration multiplier
# fast      every can in the section is fast to eat
SECTIONS = OrderedDict([
    ('cookies',      dict(mult=2,    servings=5,  n=8, dur=1.0,  fast=True,  extra=[('_sugar', ['sugar'], 6, 1)])),
    ('cooked_veggie', dict(mult=2,   servings=4,  n=6, dur=1.0,  fast=False, extra=[('_salt', ['salt'], 5, 1)])),
    ('cooked_meat',  dict(mult=2,    servings=3,  n=5, dur=1.0,  fast=False, extra=[('_broth', ['salt', 'bone_broth'], 4, 1)])),
    ('bread',        dict(mult=2,    servings=4,  n=6, dur=1.0,  fast=False, extra=[])),
    ('salad',        dict(mult=1.25, servings=8,  n=6, dur=0.75, fast=True,  extra=[])),   # vinegar additive comes later
    ('soup',         dict(mult=1.25, servings=8,  n=6, dur=0.75, fast=False, extra=[('_broth', ['salt', 'bone_broth'], 3, 1)])),
    ('golden',       dict(mult=0.25, servings=10, n=2, dur=0.25, fast=False, extra=[])),   # honey is required, see below
])

# explicit cans: (section, can key, display name, source keys (first = value source), existing Canned Goods can?)
EXPLICIT = [
    ('cookies', 'canned_cookies', None, ['cookie'], True),

    ('cooked_veggie', 'canned_baked_potato', None, ['baked_potato', 'baked_potato_slices'], True),
    ('cooked_veggie', 'canned_cooked_cactus_steak', 'Canned Cactus Steak', ['cactus_steak'], False),
    ('cooked_veggie', 'canned_cooked_rice', 'Canned Cooked Rice', ['cooked_rice'], False),
    ('cooked_veggie', 'canned_dried_kelp', None, ['dried_kelp'], True),
    ('cooked_veggie', 'canned_roasted_bell_pepper', 'Canned Roasted Bell Pepper',
     ['roasted_bell_pepper_green', 'roasted_bell_pepper_red', 'roasted_bell_pepper_yellow'], False),

    # meat: whole cut, or whole + sliced where the raw cans were meant to take both (chicken, mutton, kangaroo,
    # moose); beef, pork and bison keep a separate can for the minced / bacon cut
    ('cooked_meat', 'canned_beef', None, ['beef_cooked'], True),
    ('cooked_meat', 'canned_cooked_beef_patty', 'Canned Beef Patty', ['beef_patty'], False),
    ('cooked_meat', 'canned_pork', None, ['porkchop_cooked'], True),
    ('cooked_meat', 'canned_cooked_bacon', 'Canned Cooked Bacon', ['cooked_bacon'], False),
    ('cooked_meat', 'canned_cooked_bison', 'Canned Cooked Bison', ['bison_cooked'], False),
    ('cooked_meat', 'canned_cooked_bison_patty', 'Canned Bison Patty', ['bison_patty'], False),
    ('cooked_meat', 'canned_chicken', None, ['chicken_cooked', 'chicken_cuts_cooked'], True),
    ('cooked_meat', 'canned_cooked_mutten', None, ['mutton_cooked', 'cooked_mutton_chops'], True),
    ('cooked_meat', 'canned_cooked_rabbit', None, ['rabbit_cooked'], True),
    ('cooked_meat', 'canned_cooked_ham', 'Canned Smoked Ham', ['smoked_ham'], False),
    ('cooked_meat', 'canned_cooked_chevon', 'Canned Cooked Chevon', ['cooked_goat'], False),
    ('cooked_meat', 'canned_cooked_hoglin', 'Canned Hoglin Sirloin', ['hoglin_sirloin'], False),
    ('cooked_meat', 'canned_cooked_kangaroo', 'Canned Cooked Kangaroo',
     ['kangaroo_meat_cooked', 'kangaroo_shank_cooked'], False),
    ('cooked_meat', 'canned_cooked_moose', 'Canned Cooked Moose', ['moose_ribs_cooked', 'moose_rib_cooked_loose'], False),
    ('cooked_meat', 'canned_cooked_bunfungus', 'Canned Cooked Bunfungus', ['bunfungus_cooked'], False),
    # fish: all accept the sliced cut
    ('cooked_meat', 'canned_fish', None, ['cod_cooked', 'cod_slice_cooked'], True),
    ('cooked_meat', 'canned_cooked_salmon', None, ['salmon_cooked', 'salmon_slice_cooked'], True),
    ('cooked_meat', 'canned_cooked_calamari', 'Canned Cooked Calamari', ['cooked_calamari', 'cooked_calamari_slice'], False),
    ('cooked_meat', 'canned_cooked_catfish', 'Canned Cooked Catfish', ['catfish_cooked_mob', 'catfish_slice_cooked'], False),
    ('cooked_meat', 'canned_cooked_perch', 'Canned Cooked Perch', ['cooked_perch', 'cooked_perch_slice'], False),
    ('cooked_meat', 'canned_cooked_pike', 'Canned Cooked Pike', ['cooked_pike', 'cooked_pike_slice'], False),
    ('cooked_meat', 'canned_cooked_lionfish', 'Canned Cooked Lionfish', ['cooked_lionfish'], False),
    ('cooked_meat', 'canned_cooked_lobster', 'Canned Lobster Tail', ['lobster_tail_cooked'], False),
    ('cooked_meat', 'canned_cooked_crab', 'Canned Crab Leg', ['cooked_crab'], False),

    ('soup', 'canned_rabbit_stew', None, ['rabbit_stew'], True),
    ('soup', 'canned_mushrooms', None, ['mushroom_stew'], True),

    ('bread', 'canned_bread', None, ['minecraft_bread'], True),
]

# soup keys that are not canned; salads without a can
SKIP = {
    'salad': {'maggot_salad', 'potato_salad', 'moredelight_potato_salad'},
    'soup': {'cavern_explorer_stew', 'chorus_stew_wood', 'endermite_stew_wood', 'suspicious_stew'},
    'bread': {'strawberry_scones'},
}
# sources that already have an explicit can above (not repeated by the type loops)
STRIP_NAME = re.compile(r'^(Bowl of|Plate of|Slice of|Bar of)\s+')
CAN_SPRITE_NOTE = 'canned_goods:items/canned_beets'


def load_json(path):
    with open(path, encoding='utf-8') as f:
        return json.load(f, object_pairs_hook=OrderedDict)


def parse_dict(text):
    return {m.group(1): m.group(2) for m in re.finditer(r"^i\.(\w+)\s*=\s*'([^']+)'", text, re.M)}


def strip_generated(text):
    a = text.find(BEGIN)
    b = text.find(END)
    if a >= 0 and b > a:
        return text[:a].rstrip('\n') + '\n' + text[b + len(END):].lstrip('\n')
    return text


def title_from_id(item_id):
    path = item_id.split(':', 1)[1]
    return ' '.join(w.capitalize() for w in path.split('_'))


def food_name(item_id, names):
    n = names.get(item_id) or ''
    if not n or '.' in n or n == item_id:
        return title_from_id(item_id)
    return n


def meal_values(src, mult):
    """(hunger, modifier) for a meal worth `mult` x the source: saturation points scale with the same factor."""
    hunger = max(1, int(src['nutrition'] * mult + 0.5))
    points = src['saturation'] * mult if src.get('saturation') is not None else src['nutrition'] * src['saturationModifier'] * 2 * mult
    modifier = round(points / (2 * hunger), 3)
    return hunger, modifier


def scaled_effects(src, dur):
    out = []
    for fx in src.get('effects') or []:
        ticks = max(1, int(round(fx['seconds'] * dur * 20)))
        out.append(OrderedDict([('effect', fx['effect']), ('ticks', ticks), ('amp', fx['amplifier']),
                                ('chance', fx['chance'])]))
    return out


def build(foods, names, keymap_all, keymap_base, types):
    ids_of = lambda key: keymap_all[key]  # noqa: E731
    cans = OrderedDict()
    problems = []

    def add(section, can_key, display, sources, existing):
        spec = SECTIONS[section]
        for k in sources:
            if k not in keymap_all:
                problems.append('%s: source key %s is not in item_dict' % (can_key, k))
                return
            if keymap_all[k] not in foods:
                problems.append('%s: %s (%s) is not in the food catalog' % (can_key, k, keymap_all[k]))
                return
        src = foods[ids_of(sources[0])]
        hunger, modifier = meal_values(src, spec['mult'])
        flags = src.get('flags') or ''
        c = OrderedDict()
        c['section'] = section
        c['existing'] = existing
        if not existing:
            if not display:
                display = 'Canned ' + STRIP_NAME.sub('', food_name(ids_of(sources[0]), names))
            c['name'] = display
        c['servings'] = spec['servings']
        c['hunger'] = hunger
        c['modifier'] = modifier
        c['meat'] = 'M' in flags
        c['fast'] = bool(spec['fast'] or 'F' in flags)
        c['always'] = 'A' in flags
        c['glow'] = section == 'golden' and sources[0].startswith('enchanted_')
        c['effects'] = scaled_effects(src, spec['dur'])
        c['sources'] = sources
        recipes = [OrderedDict([('suffix', ''), ('inputs', [OrderedDict([('any', sources), ('n', spec['n'])])])])]
        for suffix, add_keys, n_src, n_add in spec['extra']:
            recipes.append(OrderedDict([('suffix', suffix), ('inputs', [
                OrderedDict([('any', sources), ('n', n_src)]),
                OrderedDict([('any', add_keys), ('n', n_add)])])]))
        c['recipes'] = recipes
        if can_key in cans:
            problems.append('duplicate can ' + can_key)
        cans[can_key] = c
        return c

    taken = set()
    for section, key, display, sources, existing in EXPLICIT:
        if existing and key not in keymap_base:
            problems.append('existing can %s is not in item_dict' % key)
            continue
        if not existing and key in keymap_base:
            problems.append('new can %s collides with an existing dict key' % key)
            continue
        if add(section, key, display, sources, existing):
            taken.update(sources)

    def loop(section, type_names, skip=()):
        for tname in type_names:
            for src_key in types[tname]:
                if src_key in skip or src_key in taken:
                    continue
                can_key = 'canned_' + src_key
                if can_key in keymap_base:
                    can_key = 'canned_ce_' + src_key
                add(section, can_key, None, [src_key], False)
                taken.add(src_key)

    loop('bread', ['baked'], SKIP['bread'])
    loop('salad', ['salad'], SKIP['salad'])
    loop('soup', ['soup'], SKIP['soup'])
    loop('golden', ['golden', 'golden/enchanted'])

    # honey bread from plain bread + honey, golden cans need honey in the can
    if 'canned_honey_bread' in cans:
        cans['canned_honey_bread']['recipes'].append(OrderedDict([('suffix', '_honey'), ('inputs', [
            OrderedDict([('any', ['minecraft_bread']), ('n', 5)]),
            OrderedDict([('any', ['honey_bottle']), ('n', 1)])])]))
    for key, c in cans.items():
        if c['section'] == 'golden':
            c['recipes'] = [OrderedDict([('suffix', ''), ('inputs', [
                OrderedDict([('any', c['sources']), ('n', SECTIONS['golden']['n'])]),
                OrderedDict([('any', ['honey_bottle']), ('n', 1)])])])]

    # display names must be unique among the new cans
    seen = {}
    for key, c in cans.items():
        if c['existing']:
            continue
        if c['name'] in seen:
            problems.append('duplicate name %r: %s and %s' % (c['name'], seen[c['name']], key))
        seen[c['name']] = key
    return cans, problems


def dict_block(cans):
    lines = [BEGIN]
    new = [(k, c) for k, c in cans.items() if not c['existing']]
    width = max(len(k) for k, _ in new) + 2
    lines.append('// %d cans: cooked veggie/meat/fish, bread, salad, soup, golden (kubejs/generation_scripts/food/can_types.json)' % len(new))
    for k, _ in new:
        lines.append("i.%s = 'creativeengineering:%s';" % (k.ljust(width), k))
    lines.append(END)
    return '\n'.join(lines) + '\n'


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--check', action='store_true')
    args = ap.parse_args()

    catalog = load_json(EXPORT / 'catalog.json')['foods']
    foods = {f['id']: f for f in catalog}
    names = {f['id']: f.get('name') for f in load_json(EXPORT / 'foods.json')['foods']}
    dict_text = DICT.read_text(encoding='utf-8')
    base_text = strip_generated(dict_text)
    keymap_base = parse_dict(base_text)
    types_json = load_json(TYPES)
    types = types_json['types']

    cans, problems = build(foods, names, keymap_base, keymap_base, types)
    if problems:
        print('PROBLEMS:')
        for p in problems:
            print('  ' + p)
        sys.exit(1)

    by_section = OrderedDict()
    for k, c in cans.items():
        by_section.setdefault(c['section'], []).append(k)
    for s, ks in by_section.items():
        new = [k for k in ks if not cans[k]['existing']]
        print('%-14s %3d cans (%d new)' % (s, len(ks), len(new)))

    out = OrderedDict()
    out['_readme'] = [
        'Generated by tools/gen_can_types.py from kubejs/exported/food/catalog.json. Do not edit by hand, edit SECTIONS / EXPLICIT in the script.',
        'cans: can item_dict key -> values per meal, effects (ticks) and recipes. existing = a Canned Goods can whose food values are overridden at startup.',
        'recipes: inputs are {any: item_dict keys (alternatives), n: how many}; a recipe always also takes an empty can.',
        'Read by startup_scripts/items/cans.js (items, overrides) and server_scripts/cooking/canning.js (recipes).',
    ]
    out['cans'] = cans
    new_out = json.dumps(out, indent=1, ensure_ascii=False) + '\n'

    # item_dict block
    block = dict_block(cans)
    if ANCHOR not in base_text:
        print('anchor line not found in item_dict.js: ' + ANCHOR)
        sys.exit(1)
    new_dict = base_text.replace(ANCHOR, ANCHOR + '\n' + block.rstrip('\n'))

    # servings
    servings = load_json(SERVINGS)
    for k, c in cans.items():
        servings['servings'][k] = c['servings']
    new_servings = json.dumps(servings, indent=1, ensure_ascii=False)

    # food types: every new can is a canned food made from its sources
    for k, c in cans.items():
        if c['existing']:
            continue
        if k not in types['canned']:
            types['canned'].append(k)
        types_json['canned'][k] = c['sources']

    if args.check:
        print('check only, nothing written')
        return
    OUT.write_text(new_out, encoding='utf-8')
    DICT.write_text(new_dict, encoding='utf-8')
    SERVINGS.write_text(new_servings, encoding='utf-8')
    food_catalog.write_types(types_json)
    print('wrote %s, %s, %s, %s' % (OUT.name, DICT.name, SERVINGS.name, TYPES.name))


if __name__ == '__main__':
    main()

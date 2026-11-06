#!/usr/bin/env python3
"""Tool and misc dicts (global.tool alias t, global.misc alias m): seed, lint and coverage for
development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/_lib/tool_dict.js and misc_dict.js (creative_delight.md 13.3, framework #4).

Both are masks over the general dict like the food dict: one line per item, `t.<key> = i.<general key>;`, key
`<category>_<name>` with the category from `toolGrammar` / `miscGrammar` in _lib/food_grammar.js (no forms, no states).
  tool  bowls, cups, jars, cookware, knives, stations of the Creative Delight members (t.bowl_bamboo, t.pot_copper)
  misc  everything else of those mods that is not a food: seeds, crates, wild crops, colonies, decor (m.seed_oat)
Coverage: every general-dict item of the namespaces in NAMESPACES that is not in the food dict, not removed and not
merged. The files are kept by hand after the seed; the name is the id path less the category word.

    python3 tools/kitchen_dicts.py            lint, report member items with no key (exit 1 if any)
    python3 tools/kitchen_dicts.py --check    lint only; exit 1 on any problem
    python3 tools/kitchen_dicts.py --seed     write both files from scratch (refuses if one exists; --force to overwrite)
"""
import argparse
import re
import sys
from collections import OrderedDict
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import pack_paths as P  # noqa: E402
import food_dict as fdict  # noqa: E402  (food dict, exclusions)
import food_catalog as fc  # noqa: E402  (item_dict parsing)

INSTANCE = Path(__file__).resolve().parent.parent
LIB = P.STARTUP_LIB
FILES = {'tool': LIB / 'tool_dict.js', 'misc': LIB / 'misc_dict.js'}
ALIAS = {'tool': 't', 'misc': 'm'}

# Creative Delight members and the ports it bundles (creative_delight.md 38.2), plus Farmer's Delight (the base member).
NAMESPACES = ['farmersdelight', 'oceansdelight', 'oceanic_delight', 'ends_delight', 'nethersdelight', 'rusticdelight',
              'delightful', 'endersdelight', 'hearthandharvest', 'roll_delight', 'largemeals', 'barbequesdelight',
              'veggiesdelight', 'vintagedelight', 'fungidelight', 'miners_delight', 'displaydelight']

# Category word -> words in the id path that select it (first match in this order wins; tool before misc).
TOOL_RULES = OrderedDict([
    ('knife', ('knife',)), ('machete', ('machete',)), ('skillet', ('skillet',)), ('kettle', ('kettle',)),
    ('pot', ('pot',)), ('stove', ('stove',)), ('oven', ('oven',)), ('grill', ('grill',)), ('board', ('board',)),
    ('basket', ('basket',)), ('bowl', ('bowl',)), ('cup', ('cup',)), ('jar', ('jar',)), ('bottle', ('bottle',)),
    ('mold', ('mold',)), ('net', ('net',)), ('tray', ('tray',)), ('basin', ('basin',)), ('plate', ('plate',)),
    ('watering_can', ('watering_can',)), ('station', ('evaporator', 'mount', 'skewer_rack', 'drying_rack', 'press')),
])
MISC_RULES = OrderedDict([
    ('crate', ('crate',)), ('bag', ('bag',)), ('seed', ('seeds', 'seed', 'pips')), ('wild', ('wild',)),
    ('colony', ('colony',)), ('spawn_egg', ('spawn_egg',)), ('hat', ('hat',)), ('compost', ('compost',)),
    ('soil', ('soil', 'farmland')), ('crop', ('crop', 'plant', 'vine', 'bush', 'stage')),
    ('sapling', ('sapling',)), ('leaves', ('leaves',)), ('wood', ('log', 'wood', 'planks', 'stairs', 'slab', 'fence',
                                                                 'door', 'sign', 'button', 'pressure')),
    ('block', ('block', 'bale', 'stem', 'cap', 'veil', 'edge', 'bricks', 'tile', 'lamp')),
    ('material', ('canvas', 'rope', 'straw', 'shard', 'fragment', 'tooth', 'shell', 'sight', 'charcoal', 'salt')),
])
FALLBACK = 'item'
# Categories whose word is the item's noun: the word is dropped from the name (iron_knife -> knife_iron). The others
# keep the whole path (salt_lamp_black -> block_salt_lamp_black).
NOUN_CATS = set(TOOL_RULES) - {'station'} | {'crate', 'bag', 'seed', 'wild', 'colony', 'spawn_egg', 'hat', 'compost',
                                             'soil'}
# Foods of ports newer than the last /dumpfood (food_dict.py cannot see them yet): no tool or misc key; they get a food
# key when food_dict.py runs on a fresh dump. Listed by the coverage run.
FOOD_PENDING = {
    'farmersdelight:mushroom_pot_pie', 'farmersdelight:mushroom_pot_pie_block', 'ends_delight:dragon_leg_with_sauce_block',
    'largemeals:raw_mutton_rack', 'largemeals:cooked_mutton_rack', 'largemeals:roasted_mutton_rack',
    'largemeals:roasted_mutton_rack_block', 'largemeals:pufferfish_broth', 'largemeals:potato_soup', 'largemeals:red_soup',
    'largemeals:tomato_egg_soup', 'largemeals:cod_deluxe', 'largemeals:hearty_lunch', 'largemeals:chicken_curry',
    'largemeals:pasta_with_mushroom_sauce', 'largemeals:omurice_block', 'largemeals:omurice',
    'largemeals:rice_pudding', 'largemeals:sweet_berry_custard',
    'roll_delight:rice_roll', 'roll_delight:rice_roll_slice', 'roll_delight:amazing_salmon_roll',
    'roll_delight:amazing_salmon_roll_slice', 'roll_delight:mushroom_roll', 'roll_delight:mushroom_roll_slice',
    'roll_delight:ancient_roll', 'roll_delight:ancient_roll_slice', 'roll_delight:golden_roll',
    'roll_delight:golden_roll_slice', 'roll_delight:nether_roll', 'roll_delight:tobiko_roll',
    'roll_delight:legendary_roll_medley', 'roll_delight:mythical_roll_medley',
    'hearthandharvest:raspberry', 'delightful:matcha_ice_cream_block', 'delightful:salmonberry_ice_cream_block',
    'nethersdelight:cooked_nether_wart',
}

LINE = re.compile(r"^\s*([tm])\.(\w+)\s*=\s*(.+?);?\s*(//.*)?$")


def grammar(name):
    src = (LIB / 'food_grammar.js').read_text()
    m = re.search(r'global\.%sGrammar\s*=\s*\{\s*categories:\s*\[([^\]]*)\]' % name, src)
    return re.findall(r"'(\w+)'", m.group(1)) if m else []


def read(kind):
    p = FILES[kind]
    if not p.exists():
        return []
    out = []
    for n, line in enumerate(p.read_text().splitlines(), 1):
        m = LINE.match(line)
        if m and m.group(1) == ALIAS[kind]:
            out.append((n, m.group(2), m.group(3).strip()))
    return out


def wanted():
    """item id -> general key, for every member item that is not a food, a tag, removed or merged."""
    _, key_val, _ = fc.parse_dict()
    by_id = {}
    for k, v in key_val.items():
        if v.startswith('#') or v.split(':', 1)[0] not in NAMESPACES:
            continue
        by_id.setdefault(v, k)
    food = {rhs[2:] for _, _, rhs in fdict.read_dict() if rhs.startswith('i.')}
    food_ids = {key_val.get(k) for k in food}
    try:
        rows, _, excl = fdict.foods_wanted(key_val)
        food_ids |= set(rows)
    except (OSError, ValueError, KeyError):
        excl = fdict.excluded_ids(key_val)
    return {i: k for i, k in by_id.items() if i not in food_ids and i not in excl and i not in FOOD_PENDING}, key_val


def classify(item_id):
    ns, path = item_id.split(':', 1)
    words = path.split('_')
    for kind, rules in (('tool', TOOL_RULES), ('misc', MISC_RULES)):
        for cat, hits in rules.items():
            for h in hits:
                hw = h.split('_')
                for s in range(len(words) - len(hw) + 1):
                    if words[s:s + len(hw)] == hw:
                        if cat not in NOUN_CATS:
                            return kind, cat, path
                        name = words[:s] + words[s + len(hw):]
                        return kind, cat, '_'.join(name) or ns
    return 'misc', FALLBACK, path


def seed(force):
    for p in FILES.values():
        if p.exists() and not force:
            sys.exit(f'{p.relative_to(INSTANCE)} exists; --force to overwrite')
    items, _ = wanted()
    named = {'tool': {}, 'misc': {}}
    for item_id in sorted(items, key=lambda x: (x.split(':', 1)[0] != 'farmersdelight', x)):
        kind, cat, name = classify(item_id)
        key = f'{cat}_{name}'
        if key in named[kind]:
            key = f'{key}_{item_id.split(":", 1)[0]}'
        n = 2
        base = key
        while key in named[kind]:
            key, n = f'{base}_{n}', n + 1
        named[kind][key] = (cat, items[item_id])
    for kind, entries in named.items():
        cats = TOOL_RULES if kind == 'tool' else list(MISC_RULES) + [FALLBACK]
        a = ALIAS[kind]
        width = max(len(k) for k in entries) + 3
        head = HEADER[kind].replace('{n}', str(len(entries)))
        body = []
        for cat in cats:
            rows = sorted((k, v) for k, (c, v) in entries.items() if c == cat)
            if not rows:
                continue
            body.append(f'\n\t// ---- {cat}')
            body += [f'\t{a}.{k:<{width}}= i.{v};' for k, v in rows]
        FILES[kind].write_text(head + '\n'.join(body) + FOOTER[kind])
        print(f'{FILES[kind].relative_to(INSTANCE)}: {len(entries)} keys')


def lint():
    _, key_val, _ = fc.parse_dict()
    problems = []
    seen = {}
    food = {rhs for _, _, rhs in fdict.read_dict()}
    for kind in FILES:
        cats = grammar(kind)
        if not cats:
            problems.append(f'food_grammar.js: no global.{kind}Grammar')
        for n, key, rhs in read(kind):
            where = f'{FILES[kind].name}:{n} {key}'
            if not rhs.startswith('i.'):
                problems.append(f'{where}: right-hand side must be i.<key> (no literal id)')
                continue
            if rhs[2:] not in key_val:
                problems.append(f'{where}: {rhs} is not in item_dict.js')
            if rhs in food:
                problems.append(f'{where}: {rhs} is already in the food dict')
            if rhs in seen:
                problems.append(f'{where}: {rhs} already named {seen[rhs]}')
            seen[rhs] = key
            cat = max((c for c in cats if key.startswith(c + '_')), key=len, default=None)
            if cat is None or not re.fullmatch(r'[a-z0-9]+(_[a-z0-9]+)*', key[len(cat) + 1:]):
                problems.append(f'{where}: does not fit <category>_<name>')
    return problems, seen


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument('--check', action='store_true')
    ap.add_argument('--seed', action='store_true')
    ap.add_argument('--force', action='store_true')
    a = ap.parse_args()
    if a.seed:
        seed(a.force)
    problems, seen = lint()
    for p in problems:
        print('PROBLEM ' + p)
    print(f'kitchen dicts: {sum(len(read(k)) for k in FILES)} keys, {len(problems)} problems')
    if a.check or problems:
        return 1 if problems else 0
    items, _ = wanted()
    missing = sorted(i for i, k in items.items() if f'i.{k}' not in seen)
    for i in missing:
        print(f'NO KEY {i} (i.{items[i]})')
    print(f'{len(missing)} member items with no key')
    print(f'{len(FOOD_PENDING)} member foods wait for a food key (food_dict.py after /dumpfood): '
          + ', '.join(sorted(FOOD_PENDING)))
    return 1 if missing else 0


HEADER = {
    'tool': """// priority: 979
// Tool dict: global.tool (alias t), a mask over global.item (creative_delight.md 13.3): bowls, cups, jars, cookware,
// knives and stations of the Creative Delight members. The value is always i.<key>, so items stay defined once in
// item_dict.js. Key: <category>_<name>, category from global.toolGrammar (food_grammar.js), e.g. t.pot_copper.
//
// Kept by hand. Seeded 2026-11-02 by tools/kitchen_dicts.py --seed ({n} keys); run tools/kitchen_dicts.py to lint this
// file and misc_dict.js and list member items with no key. Check on load as for the food dict (food_grammar.js):
// a missing general key, a value not in global.item, an item named twice or a key outside the grammar throws.
// global.toolKey is the reverse index (item id -> tool key). Pack scripts alias it t only where t is free.
global.tool = {}
;(function (t, i) {
""",
    'misc': """// priority: 978
// Misc dict: global.misc (alias m), a mask over global.item (creative_delight.md 13.3): every item of the Creative
// Delight members that is neither a food nor a tool (seeds, crates, wild crops, colonies, decor). The value is always
// i.<key>, so items stay defined once in item_dict.js. Key: <category>_<name>, category from global.miscGrammar
// (food_grammar.js), e.g. m.seed_oat; `item_` is the catch-all category.
//
// Kept by hand. Seeded 2026-11-02 by tools/kitchen_dicts.py --seed ({n} keys); run tools/kitchen_dicts.py to lint.
// Check on load as for the food dict; global.miscKey is the reverse index (item id -> misc key).
global.misc = {}
;(function (m, i) {
""",
}
FOOTER = {
    'tool': "\n\n\tglobal.toolKey = global.subdict.check(t, 'tool', global.toolGrammar)\n})(global.tool, global.item)\n",
    'misc': "\n\n\tglobal.miscKey = global.subdict.check(m, 'misc', global.miscGrammar)\n})(global.misc, global.item)\n",
}

if __name__ == '__main__':
    sys.exit(main())

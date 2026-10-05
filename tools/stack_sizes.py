#!/usr/bin/env python3
"""Stack sizes: list every item whose max stack size is not 64, cooking items first, then the rest.

Unstackable gear (stack size 1 and armor, a tool or weapon, or anything with durability) is left out.

Source data:
  kubejs/exported/stacks/stack_sizes.json   /dumpstacks: every item in the registry whose stack is not 64
  kubejs/exported/food/{foods,drinks}.json  /dumpfood: fallback for the cooking part when /dumpstacks
                                            has not been run yet (edible items and drinks only)
  kubejs/exported/food/catalog.json         food type per edible item (tools/food_catalog.py)

    python3 tools/stack_sizes.py    writes feature_writeup/stack_sizes.md
"""
import json
import re
from collections import Counter, OrderedDict, defaultdict
from datetime import datetime
from pathlib import Path

INSTANCE = Path(__file__).resolve().parent.parent
STACKS = INSTANCE / 'kubejs/exported/stacks/stack_sizes.json'
FOOD = INSTANCE / 'kubejs/exported/food'
OUT = INSTANCE / 'feature_writeup/stack_sizes.md'

# Mods whose non-edible items (ingredients, containers, cooking blocks) count as cooking items.
COOKING_MODS = {
    'abnormals_delight', 'alexsdelight', 'appliedcooking', 'berry_good', 'brewinandchewin', 'canned_goods',
    'cookingforblockheads', 'corn_delight', 'create_central_kitchen', 'create_confectionery', 'create_factory',
    'delightful', 'displaydelight', 'endersdelight', 'farmersdelight', 'farmersrespite', 'fruitsdelight',
    'goldenfood', 'moredelight', 'neapolitan', 'nethersdelight', 'quarkdelight', 'refinedcooking',
    'rusticdelight', 'salt', 'solapplepie', 'storagedelight', 'vinery',
}
FOOD_TAG = re.compile(r'^(creativeengineering:food_type/|forge:foods|forge:crops|forge:vegetables|forge:fruits|'
                      r'forge:milk|forge:eggs|forge:dough|forge:flour|forge:cooking_oil|forge:salt|forge:sugar|'
                      r'forge:raw_|forge:cooked_|forge:bread|farmersdelight:|brewinandchewin:)')
FOOD_BLOCK = re.compile(r'(Cake|Feast|Pie|Pizza|Pudding|Sandwich|Roast|Platter|Food)')

# Stack sizes the pack changes on purpose (anything not listed is the mod's own value).
PACK_CHANGES = OrderedDict([
    ('minecraft:mushroom_stew', 'vanilla 1; Farmer\'s Delight `enableStackableSoupItems` (on by default) makes it 16'),
    ('minecraft:beetroot_soup', 'vanilla 1; Farmer\'s Delight `enableStackableSoupItems` (on by default) makes it 16'),
    ('minecraft:rabbit_stew', 'vanilla 1; Farmer\'s Delight `enableStackableSoupItems` (on by default) makes it 16'),
])
# Non-cooking categories, first match wins: (title, test on a row).
def _cls(row):
    return ' '.join(c.rsplit('.', 1)[-1] for c in row.get('classes', []))

CATEGORIES = [
    ('Buckets and fluid containers', lambda r: r['id'].endswith('_bucket') or 'Bucket' in _cls(r)),
    ('Potions, bottles and flasks', lambda r: re.search(r'Potion|Bottle|Flask|Vial', _cls(r)) or re.search(r'potion|bottle|flask|vial', r['id'])),
    ('Books, maps and paper', lambda r: re.search(r'Book|Map|Scroll|Paper|Photograph|Album', _cls(r)) or re.search(r'book|_map$|scroll|photograph', r['id'])),
    ('Vehicles and mounts', lambda r: re.search(r'Boat|Minecart|Cart|Saddle|Vehicle|Raft|Balloon', _cls(r)) or re.search(r'boat|minecart|_cart$|saddle|raft', r['id'])),
    ('Music discs', lambda r: 'RecordItem' in _cls(r) or 'music_disc' in r['id']),
    ('Signs, banners and patterns', lambda r: re.search(r'Sign|Banner', _cls(r)) or re.search(r'_sign$|banner', r['id'])),
    ('Throwables and ammo', lambda r: re.search(r'Snowball|Egg|Pearl|Throwable|Arrow|Bullet|Dart|Projectile', _cls(r)) or re.search(r'snowball|_egg$|pearl|dart', r['id'])),
    ('Bags, backpacks and storage', lambda r: re.search(r'Backpack|Bag|Bundle|Shulker|Storage|Pouch', _cls(r)) or re.search(r'backpack|bag$|bundle|shulker|pouch', r['id'])),
    ('Mob capture, spawn and pets', lambda r: re.search(r'Spawn|Mob|Capture|Cage|Net', _cls(r)) or re.search(r'spawn|cage|capture|net$', r['id'])),
    ('Blocks', lambda r: r.get('blockItem')),
    ('Other items', lambda r: True),
]


def load_json(p):
    with open(p, encoding='utf-8') as f:
        return json.load(f)


def mtime(p):
    return datetime.fromtimestamp(p.stat().st_mtime).strftime('%Y-%m-%d %H:%M')


def food_types():
    p = FOOD / 'catalog.json'
    if not p.exists():
        return {}
    return {f['id']: f.get('type') for f in load_json(p).get('foods', [])}


def is_excluded_gear(r):
    return r['stackSize'] == 1 and r.get('gear')


def cooking_kind(r):
    """Cooking sub-section for a row, or None when it is not a cooking item."""
    if any(c.endswith('.PotionItem') or c.endswith('PotionItem') for c in r.get('classes', [])):
        return None   # potions drink, but they're brewing, not cooking
    if r.get('edible'):
        return 'Food'
    if r.get('useAnim') == 'DRINK':
        return 'Drinks'
    if r.get('blockItem') and any(FOOD_BLOCK.search(c.rsplit('.', 1)[-1]) for c in r.get('blockClasses', [])):
        return 'Placed food (cakes, pies, feasts)'
    if r['mod'] in COOKING_MODS or any(FOOD_TAG.match(t) for t in r.get('tags', [])):
        return 'Other cooking items (ingredients, containers, cooking blocks)'
    return None


COOKING_ORDER = ['Food', 'Drinks', 'Placed food (cakes, pies, feasts)',
                 'Other cooking items (ingredients, containers, cooking blocks)']


def rows_from_stacks():
    data = load_json(STACKS)
    rows = data['items']
    return rows, f'`/dumpstacks` export of {mtime(STACKS)}: {data["listed"]} of {data["total"]} items stack to something other than 64', True


def rows_from_food():
    rows = []
    for f in load_json(FOOD / 'foods.json')['foods']:
        if f['stackSize'] != 64:
            rows.append(dict(f, edible=True, useAnim=f.get('useAnim')))
    for d in load_json(FOOD / 'drinks.json')['drinks']:
        if d['stackSize'] != 64:
            rows.append(dict(d, edible=False, useAnim='DRINK'))
    src = (f'`/dumpfood` export of {mtime(FOOD / "foods.json")} (edible items and drinks only). '
           '**Run `/dumpstacks` in game to fill in the rest** (placed food, other cooking items and every other item)')
    return rows, src, False


def note_for(r):
    notes = []
    if r['id'] in PACK_CHANGES:
        notes.append(PACK_CHANGES[r['id']])
    if r.get('baseStackSize') is not None:
        notes.append(f'item base {r["baseStackSize"]}, stack-aware override')
    if r.get('gear') and r['stackSize'] != 1:
        notes.append(f'gear ({r["gear"]})')
    if r.get('maxDamage') and not r.get('gear'):
        notes.append(f'durability {r["maxDamage"]}')
    return '; '.join(notes)


def esc(s):
    return str(s).replace('|', '\\|')


def table(rows, with_type, types):
    out = []
    head = '| Stack | Item | Name | ' + ('Food type | ' if with_type else '') + 'Leaves | Notes |'
    out.append(head)
    out.append('|---:|---|---|' + ('---|' if with_type else '') + '---|---|')
    for r in sorted(rows, key=lambda r: (r['stackSize'], r['id'])):
        cells = [str(r['stackSize']), f'`{r["id"]}`', esc(r.get('name', r['id']))]
        if with_type:
            cells.append(f'`{types[r["id"]]}`' if types.get(r['id']) else '')
        cells.append(f'`{r["remainder"]}`' if r.get('remainder') else '')
        cells.append(esc(note_for(r)))
        out.append('| ' + ' | '.join(cells) + ' |')
    return out


def size_summary(groups):
    sizes = sorted({r['stackSize'] for rows in groups.values() for r in rows})
    out = ['| Section | ' + ' | '.join(str(s) for s in sizes) + ' | Total |',
           '|---|' + '---:|' * (len(sizes) + 1)]
    for name, rows in groups.items():
        c = Counter(r['stackSize'] for r in rows)
        out.append(f'| {name} | ' + ' | '.join(str(c.get(s, '')) for s in sizes) + f' | {len(rows)} |')
    return out


def main():
    if STACKS.exists():
        rows, source, full = rows_from_stacks()
    else:
        rows, source, full = rows_from_food()
    types = food_types()

    excluded = [r for r in rows if is_excluded_gear(r)]
    rows = [r for r in rows if not is_excluded_gear(r)]

    cooking = OrderedDict((k, []) for k in COOKING_ORDER)
    other = OrderedDict((t, []) for t, _ in CATEGORIES)
    for r in rows:
        k = cooking_kind(r)
        if k:
            cooking[k].append(r)
            continue
        for title, test in CATEGORIES:
            if test(r):
                other[title].append(r)
                break
    cooking = OrderedDict((k, v) for k, v in cooking.items() if v)
    other = OrderedDict((k, v) for k, v in other.items() if v)

    L = ['# Stack sizes (generated)', '']
    L.append(f'Generated by `python3 tools/stack_sizes.py` from the {source}. '
             'Don\'t edit this file: rerun the dump and the tool.')
    L.append('')
    L.append('Every item whose max stack size is not 64, as the game reports it after mod configs and KubeJS '
             'edits. Unstackable gear is left out: stack size 1 and armor (incl. horse armor, elytra), a tool or '
             f'weapon, or anything with durability ({len(excluded)} items). '
             'Cooking items come first, then everything else by category.')
    L.append('')
    L.append('**Columns.** Stack: max stack size. Food type: `#creativeengineering:food_type/...` from the food '
             'catalog ([food_catalog.md](food_catalog.md)). Leaves: the crafting remainder handed back '
             '(bowl, bottle, bucket). Notes: pack changes, and "stack-aware override" when the item reports one '
             'value and its stacks another.')
    L.append('')
    L.append('## What the pack changes')
    L.append('')
    for iid, why in PACK_CHANGES.items():
        L.append(f'- `{iid}`: {why} ([farmersdelight-common.toml](../config/farmersdelight-common.toml), `soupItemList`).')
    L.append('- Most stack sizes are set by [stack_sizes.js](../kubejs/startup_scripts/changes/stack_sizes.js) from '
             '[stack_sizes.json](../kubejs/generation_scripts/stacks/stack_sizes.json), generated by '
             '`tools/gen_stack_sizes.py` (the policy lives in that tool). What it sets to 64 never shows below; '
             'everything else it sets shows with its new size, and wins over the Farmer\'s Delight soup override above.')
    L.append('- Per-item gear overrides (`stackSize` in `global.tuneTools` / `global.tuneArmor`) only touch gear, '
             'which this list leaves out.')
    L.append('')
    L.append('## Summary')
    L.append('')
    L += size_summary(OrderedDict([('Cooking: ' + k, v) for k, v in cooking.items()] + list(other.items())))
    L.append('')

    L.append('## 1. Cooking items')
    L.append('')
    L.append('Edible items, drinks, placed food, and the non-edible items of the cooking mods '
             f'({", ".join(sorted(COOKING_MODS))}) or with a food/ingredient tag.')
    if not full:
        L.append('')
        L.append('Only edible items and drinks so far: the `/dumpfood` export has no placed food or ingredients. '
                 'Run `/dumpstacks` and regenerate.')
    for n, (k, v) in enumerate(cooking.items(), 1):
        L.append('')
        L.append(f'### 1.{n} {k} ({len(v)})')
        L.append('')
        by_mod = Counter(r['mod'] for r in v)
        L.append('By mod: ' + ', '.join(f'{m} {c}' for m, c in sorted(by_mod.items(), key=lambda x: (-x[1], x[0]))) + '.')
        L.append('')
        L += table(v, k in ('Food', 'Drinks'), types)

    L.append('')
    L.append('## 2. All other items')
    L.append('')
    if not full:
        L.append('Not generated yet: run `/dumpstacks` in game (op), then `python3 tools/stack_sizes.py`.')
    else:
        L.append('Grouped by a rough category (item class and id), first match wins; within a category sorted '
                 'by stack size, then id.')
        for n, (k, v) in enumerate(other.items(), 1):
            L.append('')
            L.append(f'### 2.{n} {k} ({len(v)})')
            L.append('')
            by_mod = Counter(r['mod'] for r in v)
            L.append('By mod: ' + ', '.join(f'{m} {c}' for m, c in sorted(by_mod.items(), key=lambda x: (-x[1], x[0]))) + '.')
            L.append('')
            L += table(v, False, types)

    L.append('')
    OUT.write_text('\n'.join(L) + '\n', encoding='utf-8')
    total = sum(len(v) for v in cooking.values()) + sum(len(v) for v in other.values())
    print(f'wrote {OUT.relative_to(INSTANCE)}: {total} items '
          f'({sum(len(v) for v in cooking.values())} cooking), {len(excluded)} unstackable gear left out'
          + ('' if full else ' [cooking only: run /dumpstacks for the rest]'))


if __name__ == '__main__':
    main()

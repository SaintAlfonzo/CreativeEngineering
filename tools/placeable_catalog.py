#!/usr/bin/env python3
"""Placeable food: every food (or food block) that can be put down in the world, and how.

Three ways to place food in this pack:
  * Display Delight (DD): sneak + right click puts a food down as a display block, and right clicking an
    empty Food Plate / Small Food Plate plates it. DD links a food to its block on world load, only when
    a DD block's loot drop is that food AND the food is in the matching displaydelight:* item tag.
  * The food item is itself a block item: cakes, pies, feasts, pizza, Vinery wine bottles, Fruits
    Delight jams. Some of these place only while sneaking (SNEAK_ITEM_CLASSES).
  * Crops and bushes that are edible (carrot, sweet berries...) plant rather than place. They are listed
    in the catalog but get no tag.
  * Brewin' and Chewin' coasters (backport) take up to 4 items, and with [coaster] taggedItemsOnly only the ones
    with a coaster model: the port's own (COASTER_DIR) and the block-modelled foods tools/gen_coaster_models.py
    writes (PACK_COASTER_DIR, run it first). Those get "Placeable on a coaster" (alcohol.md 8.3, 12).

Outputs:
  kubejs/generation_scripts/food/placeable.json   tag -> item_dict keys, read by
                                                  data/creative_delight/kubejs/creativeengineering/placeable_tags.js
  feature_writeup/placeable_catalog.md             the catalog
  item_dict.js                                     keys for any listed item that had none, plus the tag
                                                   keys, in a generated "Placeable food" block

Source data, best first:
  kubejs/exported/food/placeable.json   /dumpplaceable (live DD maps, every food-looking block item)
  kubejs/exported/food/*.json           /dumpfood fallback (DD links rebuilt from the DD jar + live tags)

    python3 tools/placeable_catalog.py           write everything
    python3 tools/placeable_catalog.py --check   change nothing; exit 1 if placeable.json is out of date
"""
import argparse
import glob
import json
import re
import sys
import zipfile
from collections import OrderedDict, defaultdict
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import food_catalog as fc  # noqa: E402  (item_dict parsing + key helpers)
import display_delight_jar as ddj  # noqa: E402  (DD jar in mods/, block -> food links)

INSTANCE = Path(__file__).resolve().parent.parent
EXPORT = INSTANCE / 'kubejs/exported/food'
DUMP = EXPORT / 'placeable.json'
OUT_JSON = INSTANCE / 'kubejs/generation_scripts/food/placeable.json'
OUT_MD = INSTANCE / 'feature_writeup/placeable_catalog.md'
DD_CONFIG = INSTANCE / 'config/displaydelight-common.toml'
ITEMS_BY_MOD = INSTANCE / 'kubejs/exported/items_by_mod'

# tag -> item_dict key. 'placeable/sneak' is nested: the tag script also puts its members in 'placeable'.
TAGS = OrderedDict([
    ('placeable', ('creativeengineering:placeable', 't_placeable')),
    ('placeable/sneak', ('creativeengineering:placeable/sneak', 't_placeable_sneak')),
    ('placeable_on_plate', ('creativeengineering:placeable_on_plate', 't_placeable_on_plate')),
    ('placeable_on_small_plate', ('creativeengineering:placeable_on_small_plate', 't_placeable_on_small_plate')),
    ('placeable_on_coaster', ('creativeengineering:placeable_on_coaster', 't_placeable_on_coaster')),
])

# The Brewin' port's coaster models (one file per item that has its own look on a coaster).
COASTER_DIR = INSTANCE / 'development/creativeBrewing/members/brewin/src/main/resources/assets/brewinandchewin/brewinandchewin/coaster'
# The pack's coaster models (Display Delight's placed models, other mods' wine bottles and jars, borrowed models),
# from tools/gen_coaster_models.py.
PACK_COASTER_DIR = INSTANCE / 'kubejs/assets/creativeengineering/brewinandchewin/coaster'
# Only items with a coaster model go on a coaster (owner 2026-10-25: the produce of every raw, cooked, egg and dairy
# food type had joined the tag and lay flat without a model; those were cut, food_families.md 4.4).

# Block classes that make a non-edible block item a placed food (same words as dump_food.js FOOD_BLOCK).
FOOD_BLOCK = re.compile(r'(Cake|Feast|Pie|Pizza|Pudding|Sandwich|Roast|Platter|Food)')
# Wider net, only used to list non-edible block items for review (dump mode).
FOOD_BLOCK_WIDE = re.compile(r'(Cheese|Wheel|Bottle|Jelly|Jam|Popcorn|Nachos|Ham|Chicken|Medley|Rice|Bread|Loaf|'
                             r'Tart|Cookie|Wine|Drink|Booze)')
PLANT = re.compile(r'(Crop|Bush|Vine|Stem|Sapling|GrowingPlant|Flower|Mushroom|Fungus)')

# Item classes whose place() only runs while sneaking.
SNEAK_ITEM_CLASSES = {
    'dev.xkmc.fruitsdelight.content.item.FDBlockItem': 'Fruits Delight: place() passes unless sneaking',
    'net.satisfy.vinery.core.item.DrinkBlockItem': 'Vinery backport (1.5.5): getPlacementState is null unless crouching',
}
# How each own-block family places, by item class (shown in the catalog).
HOW_BY_ITEM_CLASS = {
    'satisfyu.vinery.item.DrinkBlockItem': 'right click a block (not sneaking); drink in the air',
    'satisfyu.vinery.item.DrinkBlockSmallItem': 'right click a block (not sneaking); drink in the air',
    'satisfyu.vinery.item.DrinkBlockBigItem': 'right click a block (not sneaking); drink in the air',
    'dev.xkmc.fruitsdelight.content.item.FDBlockItem': 'sneak + right click',
    'net.satisfy.vinery.core.item.DrinkBlockItem': 'sneak + right click; drink in the air',
}

# Non-edible block items that are food blocks but the class regex misses (id -> note).
INCLUDE = {
    'corn_delight:popcorn_box': 'feast-style box, serves caramel popcorn',
    'corn_delight:nachos_block': 'feast, serves nachos bowls',
}
# Never tagged (id or 'ns:prefix*' -> reason).
EXCLUDE = {
    'displaydelight:*': "Display Delight's own display-block items (pick block gives the food instead)",
    'fantasyfurniture:decorations/*': 'furniture decoration, not edible and serves nothing',
    'oldguns:*': 'black powder cake: gunpowder, not food',
    'immersive_weathering:icicle': 'ordinary block that happens to be edible',
    'immersive_weathering:moss_clump': 'ordinary block that happens to be edible',
    'vintagedelight:sweet_berry_jam_jar': 'removed (dishes/jams, 2026-10-29): Fruits Delight has the sweet berry jam',
    'vintagedelight:glow_berry_jam_jar': 'removed (dishes/jams, 2026-10-29): Fruits Delight has the glow berry jam',
}

VANILLA_STEWS = {'minecraft:mushroom_stew', 'minecraft:rabbit_stew', 'minecraft:beetroot_soup'}
DD_KIND_LABEL = {'placed': 'placed', 'plate': 'Food Plate', 'small_plate': 'Small Food Plate'}


def excluded(item_id):
    for pat, why in EXCLUDE.items():
        if (pat.endswith('*') and item_id.startswith(pat[:-1])) or item_id == pat:
            return why
    return None


def simple(cls):
    return cls.rsplit('.', 1)[-1]


# ── Display Delight jar ─────────────────────────────────────────────────────

def dd_jar():
    return str(ddj.jar_path())


def dd_from_jar():
    """(item -> {kind: block}) for the registered DD blocks (1.2.0: loot table drop; the 1.7.0 port: block name,
    tools/display_delight_jar.py), first block wins like DD's putIfAbsent, and the jar's own tag lists
    {kind: set(ids)}."""
    links = defaultdict(dict)
    tags = {}
    with zipfile.ZipFile(dd_jar()) as z:
        for block, food in sorted(ddj.links(z).items()):
            kind = 'small_plate' if 'small_plated_' in block else 'plate' if 'plated_' in block else 'placed'
            links[food].setdefault(kind, 'displaydelight:' + block)
        for name in sorted(z.namelist()):
            m = re.match(r'data/displaydelight/tags/items/(\w+)\.json$', name)
            if m:
                vals = json.loads(z.read(name)).get('values', [])
                tags[m.group(1)] = {v if isinstance(v, str) else v['id'] for v in vals}
    return links, tags


def vanilla_disabled():
    try:
        return re.search(r'Disable-Vanila-Food-Placement\s*=\s*true', DD_CONFIG.read_text()) is not None
    except OSError:
        return False


# ── load ─────────────────────────────────────────────────────────────────────

def load():
    """Returns (source, rows by id, dd maps {kind: {item: block}}, other block items list)."""
    jar_links, _ = dd_from_jar()
    if DUMP.exists():
        d = json.loads(DUMP.read_text())
        if not d.get('ddLoaded'):
            print('warning: the dump has no Display Delight links (run /dumpplaceable after a world loads)')
        rows = {r['id']: r for r in d['items']}
        return 'dump', rows, d['displayDelight'], d.get('blockItems', [])

    rows = {}
    for fname, key in (('foods.json', 'foods'), ('drinks.json', 'drinks'), ('food_blocks.json', 'blocks')):
        p = EXPORT / fname
        if not p.exists():
            raise SystemExit(f'no {DUMP.name} and no {p} - run /dumpplaceable (or /dumpfood) in game first')
        for r in json.loads(p.read_text())[key]:
            r = dict(r)
            if key == 'blocks':
                r.update(edible=False, blockItem=True)
            else:
                r.setdefault('edible', key == 'foods')
                r['blockItem'] = bool(r.get('blockClasses'))
            rows.setdefault(r['id'], r)
    # DD links as DD builds them: loot drop + matching live tag (tags come from the dump rows)
    tag_of = {'placed': 'displaydelight:displayable', 'plate': 'displaydelight:plate_displayable',
              'small_plate': 'displaydelight:small_plate_displayable'}
    no_vanilla = vanilla_disabled()
    dd = {k: {} for k in tag_of}
    for item, kinds in jar_links.items():
        r = rows.get(item)
        if not r:
            continue
        for kind, block in kinds.items():
            if tag_of[kind] in r.get('tags', []) and not (kind == 'placed' and no_vanilla and item in VANILLA_STEWS):
                dd[kind][item] = block
    return 'dumpfood', rows, dd, []


def installed_ids():
    ids = set()
    for p in ITEMS_BY_MOD.glob('*.json'):
        try:
            ids.update(json.loads(p.read_text()).get('items', []))
        except (OSError, ValueError):
            pass
    return ids


# ── classify ─────────────────────────────────────────────────────────────────

def classify(rows, dd):
    """id -> entry with kinds (set of tag names), how (list of str), group. Also the plant + excluded lists."""
    out, plants, skipped = {}, [], []
    for item_id, r in sorted(rows.items()):
        entry = {'id': item_id, 'name': r.get('name', item_id), 'mod': item_id.split(':', 1)[0],
                 'kinds': set(), 'how': [], 'dd': {}}
        why = excluded(item_id)
        if why:
            if r.get('blockItem') or any(item_id in dd[k] for k in dd):
                skipped.append((item_id, entry['name'], why))
            continue
        for kind in ('placed', 'plate', 'small_plate'):
            block = dd[kind].get(item_id)
            if block:
                entry['dd'][kind] = block
        if 'placed' in entry['dd']:
            entry['kinds'].update(('placeable', 'placeable/sneak'))
            entry['how'].append('DD: sneak + right click the top of a block')
        if 'plate' in entry['dd']:
            entry['kinds'].add('placeable_on_plate')
            entry['how'].append('DD: right click an empty Food Plate (sneak to put the whole stack)')
        if 'small_plate' in entry['dd']:
            entry['kinds'].add('placeable_on_small_plate')
            entry['how'].append('DD: right click an empty Small Food Plate')

        if r.get('blockItem') and item_id not in dd['placed']:
            chain = r.get('blockClasses') or []
            names = [simple(c) for c in chain]
            item_cls = (r.get('classes') or [''])[0]
            edible = r.get('edible') or r.get('useAnim') == 'DRINK' or 'useTicks' in r
            if any(PLANT.search(n) for n in names[:3]):
                plants.append((item_id, entry['name'], names[0] if names else '?'))
            elif edible or item_id in INCLUDE or any(FOOD_BLOCK.search(n) for n in names):
                entry['kinds'].add('placeable')
                sneak = item_cls in SNEAK_ITEM_CLASSES
                if sneak:
                    entry['kinds'].add('placeable/sneak')
                how = HOW_BY_ITEM_CLASS.get(item_cls) or ('sneak + right click' if sneak else 'right click')
                entry['how'].append(f'own block ({names[0] if names else "?"}): {how}')
                entry['block'] = r.get('block') or item_id
        if entry['kinds']:
            out[item_id] = entry
    return out, plants, skipped


def add_coaster(entries, rows, installed):
    """Adds placeable_on_coaster to the items with a coaster model, the port's and the pack's."""
    modelled, from_dd = set(), set()
    for p in COASTER_DIR.glob('*.json'):
        modelled.add(json.loads(p.read_text())['item'])
    if not modelled:
        print(f'warning: no coaster models in {COASTER_DIR}')
    for p in PACK_COASTER_DIR.glob('*.json'):
        from_dd.add(json.loads(p.read_text())['item'])
    if not from_dd:
        print(f'warning: no pack coaster models in {PACK_COASTER_DIR} (run tools/gen_coaster_models.py)')
    for item_id in sorted(modelled | from_dd):
        present = item_id in rows or item_id in installed or item_id.startswith('minecraft:')
        if not present or excluded(item_id):
            continue
        entry = entries.get(item_id)
        if entry is None:
            r = rows.get(item_id, {})
            entry = {'id': item_id, 'name': r.get('name', item_id), 'mod': item_id.split(':', 1)[0],
                     'kinds': set(), 'how': [], 'dd': {}}
            entries[item_id] = entry
        entry['kinds'].add('placeable_on_coaster')
        entry['how'].append('coaster: right click a coaster' + ('' if item_id in modelled else ' (pack coaster model)'))


def review_candidates(block_items, rows):
    """Non-edible block items whose class matches the wide food net but not the strict one (dump mode)."""
    seen = []
    for b in block_items:
        cls = b.get('blockClass') or ''
        if b['id'] in rows or excluded(b['id']) or b['id'] in INCLUDE:
            continue
        if FOOD_BLOCK_WIDE.search(simple(cls)) and not PLANT.search(simple(cls)):
            seen.append((b['id'], simple(cls)))
    for item_id, r in rows.items():
        names = [simple(c) for c in (r.get('blockClasses') or [])]
        if (r.get('blockItem') and not r.get('edible') and r.get('useAnim') != 'DRINK' and item_id not in INCLUDE
                and not excluded(item_id) and names and not any(FOOD_BLOCK.search(n) for n in names)
                and not PLANT.search(names[0])):
            seen.append((item_id, names[0]))
    return sorted(set(seen))


def dd_gaps(rows, dd, installed):
    """DD block exists for an installed item but the item is untagged, and tagged items no block drops."""
    jar_links, jar_tags = dd_from_jar()
    tag_of = {'placed': 'displaydelight:displayable', 'plate': 'displaydelight:plate_displayable',
              'small_plate': 'displaydelight:small_plate_displayable'}
    untagged, unlinked = [], []
    for item, kinds in sorted(jar_links.items()):
        if item not in installed and item not in rows:
            continue
        for kind, block in sorted(kinds.items()):
            if item not in dd[kind] and not excluded(item):
                tagged = tag_of[kind] in rows.get(item, {}).get('tags', [])
                if not tagged:
                    untagged.append((item, kind, block))
    for kind, tag in tag_of.items():
        for item_id, r in sorted(rows.items()):
            if tag in r.get('tags', []) and item_id not in dd[kind]:
                unlinked.append((item_id, kind))
    return untagged, unlinked


# ── item_dict ────────────────────────────────────────────────────────────────

BLOCK_START = '// ############################## Placeable food (generated by tools/placeable_catalog.py) ##############################'
BLOCK_END = '// ===== end placeable food (generated) ====='


def dict_keys(ids):
    """id -> key, adding keys for ids the dict lacks. Returns (key map, new entries, dict source)."""
    src, key_val, by_id = fc.parse_dict()
    taken = set(key_val)
    keys, new = {}, []
    for item_id in sorted(ids):
        if by_id.get(item_id):
            keys[item_id] = fc.best_key(item_id, by_id[item_id])
        else:
            k = fc.new_key(item_id, taken)
            taken.add(k)
            keys[item_id] = k
            new.append((k, item_id))
    return keys, new, src


def render_dict_block(src, new):
    s, e = src.find(BLOCK_START), src.find(BLOCK_END)
    old = []
    if s >= 0 and e >= 0:
        old = [(m.group(1), m.group(2)) for m in re.finditer(r"^i\.(\w+)\s*=\s*'([^'#][^']*)'", src[s:e], re.M)]
    items = sorted(set(old) | set(new), key=lambda kv: (kv[1], kv[0]))
    lines = [BLOCK_START,
             '// Placeable foods that had no key elsewhere, plus the placeable tag keys',
             '// (data/creative_delight/kubejs/creativeengineering/placeable_tags.js, assets/creative_delight/kubejs/_qol/placeable_tooltip.js). Re-run the tool to add more.',
             '']
    if items:
        w = max(len(k) for k, _ in items)
        lines += [f"i.{k.ljust(w)} = '{v}';" for k, v in items] + ['']
    w = max(len(k) for _, k in TAGS.values())
    lines += [f"i.{k.ljust(w)} = '#{tag}';" for tag, k in TAGS.values()]
    lines.append(BLOCK_END)
    block = '\n'.join(lines)
    if s >= 0 and e >= 0:
        return src[:s] + block + src[e + len(BLOCK_END):]
    anchor = src.find('// ############################## Validation')
    if anchor < 0:
        raise SystemExit('item_dict.js: no Validation header to insert before')
    return src[:anchor] + block + '\n\n' + src[anchor:]


# ── outputs ──────────────────────────────────────────────────────────────────

README = [
    'Generated by tools/placeable_catalog.py - edit the rules in the tool, not this file.',
    'tag -> item_dict keys. data/creative_delight/kubejs/creativeengineering/placeable_tags.js adds them as #creativeengineering:<tag>;',
    'placeable/sneak also lands in placeable. assets/creative_delight/kubejs/_qol/placeable_tooltip.js draws the lines from the tags.',
    'Catalog: feature_writeup/placeable_catalog.md',
]


def build_json(entries, keys):
    data = OrderedDict([('_readme', README)])
    for tag in TAGS:
        data[tag] = sorted((keys[e['id']] for e in entries.values() if tag in e['kinds']),
                           key=lambda k: k)
    return json.dumps(data, indent=1) + '\n'


def md_table(rows, header):
    out = ['| ' + ' | '.join(header) + ' |', '|' + '---|' * len(header)]
    out += ['| ' + ' | '.join(str(c).replace('|', '/') for c in r) + ' |' for r in rows]
    return out


def build_md(source, entries, plants, skipped, review, untagged, unlinked, new_keys):
    by_mod = defaultdict(list)
    for e in entries.values():
        by_mod[e['mod']].append(e)
    count = {t: sum(1 for e in entries.values() if t in e['kinds']) for t in TAGS}
    L = ['# Placeable food catalog', '',
         'Generated by `python3 tools/placeable_catalog.py` - do not edit by hand. Feature writeup: '
         '[placeable_food.md](placeable_food.md).', '',
         f'Source: **{"/dumpplaceable (live)" if source == "dump" else "/dumpfood export + Display Delight jar (fallback)"}**', '',
         '## Summary', '',
         *md_table([[f'`#{TAGS[t][0]}`', f'"{label}"', count[t]] for t, label in (
             ('placeable', 'Placeable'), ('placeable/sneak', '(while sneaking)'),
             ('placeable_on_plate', 'Placeable on plate'), ('placeable_on_small_plate', 'Placeable on small plate'),
             ('placeable_on_coaster', 'Placeable on a coaster'))],
             ['Tag', 'Tooltip line', 'Items']),
         '', f'{len(entries)} items in total across {len(by_mod)} mods.', '',
         '## Catalog by mod', '',
         'P = placeable, S = sneak to place, Pl = on Food Plate, SP = on Small Food Plate, C = on a Brewin\' coaster.', '']
    for mod in sorted(by_mod):
        rows = []
        for e in sorted(by_mod[mod], key=lambda e: e['name'].lower()):
            k = e['kinds']
            rows.append([e['name'], f'`{e["id"]}`',
                         'P' if 'placeable' in k else '', 'S' if 'placeable/sneak' in k else '',
                         'Pl' if 'placeable_on_plate' in k else '', 'SP' if 'placeable_on_small_plate' in k else '',
                         'C' if 'placeable_on_coaster' in k else '', '; '.join(e['how'])])
        L += [f'### {mod} ({len(rows)})', '', *md_table(rows, ['Item', 'Id', 'P', 'S', 'Pl', 'SP', 'C', 'How']), '']

    L += ['## Edible but planted, not placed (no tag)', '',
          'These place a crop or bush, so they get no "Placeable" line.', '',
          *md_table([[n, f'`{i}`', c] for i, n, c in plants], ['Item', 'Id', 'Block class']), '']
    by_why = defaultdict(list)
    for i, n, w in skipped:
        by_why[w].append((i, n))
    ex_rows = []
    for w, its in by_why.items():
        if len(its) > 8:   # a whole namespace: one row
            ex_rows.append([f'{len(its)} items', f'`{its[0][0]}`, ...', w])
        else:
            ex_rows += [[n, f'`{i}`', w] for i, n in its]
    L += ['## Excluded (no tag)', '', *md_table(ex_rows, ['Item', 'Id', 'Why']), '']
    L += ['## Display Delight gaps', '',
          '**DD has a block for an installed food, but the food is not in the matching tag**, so DD never links it. '
          'Adding the item to the `displaydelight:*` tag would make it placeable.', '']
    L += md_table([[f'`{i}`', DD_KIND_LABEL[k], f'`{b}`'] for i, k, b in untagged], ['Item', 'DD kind', 'DD block']) \
        if untagged else ['None.']
    L += ['', '**In a `displaydelight:*` tag but no DD block drops it**, so DD\'s own "Can be placed" line was wrong. '
          'The pack strips DD\'s line everywhere, so these show nothing.', '']
    L += md_table([[f'`{i}`', DD_KIND_LABEL[k]] for i, k in unlinked], ['Item', 'DD kind']) if unlinked else ['None.']
    L += ['', '## Not food placement (no tag)', '',
          '- **Handcrafted plates/bowls/cups** (`handcrafted:*_plate` etc.): right click puts *any* item on them, food or not.',
          '- **Supplementaries item shelf, Quark/Supplementaries pedestals and frames**: generic item display.',
          '- **Supplementaries cake stacking**: a second vanilla cake can go on top of a placed one; the cake is already tagged.',
          '- **Supplementaries placeable sticks / gunpowder / books**: not food.', '']
    if review:
        L += ['## Review: food-looking block items left out', '',
              'Matched the wide block-class net but not the food rules. Add real foods to `INCLUDE` in the tool.', '',
              *md_table([[f'`{i}`', c] for i, c in review], ['Id', 'Block class']), '']
    if new_keys:
        L += ['## New item_dict keys', '', f'{len(new_keys)} keys added in the generated "Placeable food" block.', '']
    return '\n'.join(L) + '\n'


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--check', action='store_true', help='change nothing; exit 1 if placeable.json is stale')
    args = ap.parse_args()

    source, rows, dd, block_items = load()
    entries, plants, skipped = classify(rows, dd)
    installed = installed_ids()
    add_coaster(entries, rows, installed)
    keys, new, src = dict_keys(entries)
    text = build_json(entries, keys)
    untagged, unlinked = dd_gaps(rows, dd, installed)
    review = review_candidates(block_items, rows) if source == 'dump' else []

    if args.check:
        current = OUT_JSON.read_text() if OUT_JSON.exists() else ''
        stale = current != text or bool(new)
        print('placeable.json is ' + ('OUT OF DATE' if stale else 'current') + (f' ({len(new)} items lack keys)' if new else ''))
        sys.exit(1 if stale else 0)

    OUT_JSON.write_text(text)
    fc.DICT.write_text(render_dict_block(src, new))
    OUT_MD.write_text(build_md(source, entries, plants, skipped, review, untagged, unlinked, new))
    counts = ', '.join(f'{t}={sum(1 for e in entries.values() if t in e["kinds"])}' for t in TAGS)
    print(f'[{source}] {len(entries)} placeable items: {counts}')
    print(f'  {len(plants)} planted, {len(skipped)} excluded, {len(untagged)} DD untagged, '
          f'{len(unlinked)} DD tag-only, {len(review)} to review, {len(new)} new item_dict keys')
    print(f'  wrote {OUT_JSON.relative_to(INSTANCE)}, {OUT_MD.relative_to(INSTANCE)}, item_dict.js block')


if __name__ == '__main__':
    main()

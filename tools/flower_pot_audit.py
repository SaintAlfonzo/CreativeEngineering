#!/usr/bin/env python3
"""Flower potting audit: which plant goes in which pot or planter (feature_writeup/flower_pots.md).

    python3 tools/flower_pot_audit.py            writes feature_writeup/flower_pots_matrix.md

Inputs, best first:
  kubejs/exported/blocks/pot_probe.json    scratch probe (tools/flower_pot_test/pot_probe.js, /pottest all): what each
                                           holder really did with each plant; overrides the prediction cell by cell
  kubejs/exported/blocks/flower_pots.json  /dumppots: the vanilla pot's live plant -> potted map and every plant block
                                           item with its class chain
  kubejs/exported/tags/item_tags.json      /dumptags: the holders' tags (flower box, planter box, Vinery box)
Without /dumppots the plant list and the vanilla map are a static guess from the jars' potted_* blockstates, marked as
such in the output. The accept rules per holder were read from each mod's bytecode (flower_pots.md 2).
"""
import glob
import json
import re
import zipfile
from collections import Counter, defaultdict
from pathlib import Path

INSTANCE = Path(__file__).resolve().parent.parent
EXP = INSTANCE / 'kubejs/exported'
OUT_MD = INSTANCE / 'feature_writeup/flower_pots_matrix.md'

VANILLA_POTTED = {
    'dandelion', 'poppy', 'blue_orchid', 'allium', 'azure_bluet', 'red_tulip', 'orange_tulip', 'white_tulip',
    'pink_tulip', 'oxeye_daisy', 'cornflower', 'lily_of_the_valley', 'wither_rose', 'oak_sapling', 'spruce_sapling',
    'birch_sapling', 'jungle_sapling', 'acacia_sapling', 'dark_oak_sapling', 'mangrove_propagule', 'fern',
    'red_mushroom', 'brown_mushroom', 'dead_bush', 'cactus', 'bamboo', 'crimson_fungus', 'warped_fungus',
    'crimson_roots', 'warped_roots', 'azalea', 'flowering_azalea'}
# potted id path -> plant item, where the potted name is not the item name (Quark MorePottedPlants and others)
POTTED_ITEM = {
    'quark:potted_berries': 'minecraft:sweet_berries', 'quark:potted_wheat': 'minecraft:wheat_seeds',
    'quark:potted_beetroot': 'minecraft:beetroot_seeds', 'quark:potted_melon': 'minecraft:melon_seeds',
    'quark:potted_pumpkin': 'minecraft:pumpkin_seeds', 'quark:potted_cocoa_bean': 'minecraft:cocoa_beans',
    'quark:potted_rose': 'minecraft:rose_bush', 'quark:potted_chorus': 'minecraft:chorus_flower',
    'quark:potted_cave_vines': 'minecraft:glow_berries', 'quark:potted_carrot': 'minecraft:carrot',
    'quark:potted_potato': 'minecraft:potato', 'supplementaries:potted_flax': 'supplementaries:flax_seeds',
    'twigs:potted_azalea_flowers': 'twigs:azalea_flowers', 'bushierflowers:potted_grown_lily': 'bushierflowers:grown_lily',
    'neapolitan:potted_mint': 'neapolitan:mint_sprout', 'neapolitan:potted_vanilla_vine': 'neapolitan:vanilla_pods',
    'immersiveengineering:potted_hemp': 'immersiveengineering:seed',
}
# registered FlowerPotBlocks that no addPlant call reaches (flower_pots.md 3): never fillable by a player
NOT_ADDED = {'vinery:potted_apple_tree_sapling', 'vinery:potted_cherry_tree_sapling',
             'farmersrespite:potted_coffee_bush', 'farmersrespite:potted_wild_coffee_bush', 'farmersrespite:potted_tea_bush'}

BEAUTIFY = ['rose_bush', 'lilac', 'blue_orchid', 'vine', 'sunflower', 'peony', 'azure_bluet', 'red_tulip',
            'orange_tulip', 'white_tulip', 'pink_tulip', 'allium', 'dandelion', 'poppy', 'glow_lichen', 'oxeye_daisy',
            'lily_of_the_valley', 'cornflower', 'weeping_vines', 'twisting_vines', 'wither_rose', 'glow_berries',
            'sweet_berries', 'grass', 'fern']
FLORAL_SMALL = ['dandelion', 'poppy', 'blue_orchid', 'allium', 'azure_bluet', 'red_tulip', 'orange_tulip',
                'white_tulip', 'pink_tulip', 'oxeye_daisy', 'cornflower', 'lily_of_the_valley', 'wither_rose']
FLORAL_TALL = ['sunflower', 'lilac', 'rose_bush', 'peony']
# Supplementaries FlowerPotHandler special models: accepted by the flower box outside its tag (Quark loaded)
FLOWER_BOX_SPECIAL = ['cactus', 'flowering_azalea', 'azalea', 'supplementaries:flax_seeds', 'sugar_cane',
                      'beetroot_seeds', 'carrot', 'chorus_flower', 'potato', 'grass', 'cocoa_beans', 'wheat_seeds',
                      'vine', 'large_fern', 'sweet_berries', 'weeping_vines', 'nether_sprouts', 'melon_seeds',
                      'pumpkin_seeds', 'tall_grass', 'sea_pickle', 'nether_wart', 'quark:chorus_weeds', 'quark:root',
                      'quark:chorus_twist']

# column -> (title, probe holder ids that report into it)
HOLDERS = [
    ('pot', 'Flower pot', ['minecraft:flower_pot']),
    ('framed', 'Framed pot', ['framedblocks:framed_flower_pot']),
    ('hanging', 'Hanging pot (Supp.)', ['supplementaries:hanging_flower_pot']),
    ('fbox', 'Flower box (Supp.)', ['supplementaries:flower_box[facing=north,floor=true]']),
    ('pbox', 'Planter box (AF)', ['another_furniture:oak_planter_box[facing=north]']),
    ('beautify', 'Hanging pot (Beautify)', ['beautify:hanging_pot']),
    ('large', 'Large pot (Floral)', ['floralench:large_pot']),
    ('vpot', 'Vinery pot', ['vinery:flower_pot']),
    ('vbox', 'Vinery box', ['vinery:flower_box']),
    ('silt', 'Silt pot (Twigs)', ['twigs:silt_pot[filled=true]']),
    ('planter', 'Planter (Supp.)', ['supplementaries:planter', 'supplementaries:planter_rich']),
]
CATS = ['flower', 'tall flower', 'botania', 'sapling', 'mushroom', 'grass / fern', 'vine', 'crop / seed', 'water',
        'cactus / cane', 'hedge', 'other']


def mc(x):
    return x if ':' in x else 'minecraft:' + x


def load_json(p):
    try:
        return json.loads(p.read_text())
    except (OSError, ValueError):
        return None


def static_potted():
    """potted block -> plant item, from every jar's potted_* blockstates (guess; /dumppots replaces it)."""
    out = {'minecraft:potted_' + n: mc(n) for n in VANILLA_POTTED}
    for j in glob.glob(str(INSTANCE / 'mods/*.jar')):
        try:
            names = zipfile.ZipFile(j).namelist()
        except zipfile.BadZipFile:
            continue
        for n in names:
            m = re.match(r'assets/([^/]+)/blockstates/(potted_[^/]+)\.json$', n)
            if m and m.group(1) != 'minecraft':
                pid = m.group(1) + ':' + m.group(2)
                out[pid] = POTTED_ITEM.get(pid, m.group(1) + ':' + m.group(2)[len('potted_'):])
    for pid, item in POTTED_ITEM.items():
        out.setdefault(pid, item)
    for n in ('wild_cotton', 'wild_bell_peppers', 'wild_pale_bell_peppers', 'wild_dark_bell_peppers', 'wild_coffee'):
        out['rusticdelight:potted_' + n] = 'rusticdelight:' + n
    for pid in NOT_ADDED:
        out.pop(pid, None)
    # Quark names some pots after the vanilla plant without its namespace
    return {pid: (mc(it.split(':', 1)[1]) if pid.startswith('quark:') and it.startswith('quark:')
                  and it.split(':', 1)[1] in ('grass', 'tall_grass', 'large_fern', 'lilac', 'peony', 'sunflower',
                  'vine', 'sea_pickle', 'sugar_cane', 'nether_sprouts', 'nether_wart', 'twisting_vines', 'weeping_vines')
                  else it) for pid, it in out.items()}


def category(item, tags, chain):
    c = ' '.join(chain or [])
    if item.startswith('botania:'):
        return 'botania'
    if 'hedge' in item:
        return 'hedge'
    if re.search(r'Cactus|SugarCane|Bamboo|Chorus', c) or re.search(r'cactus|sugar_cane|bamboo|chorus', item):
        return 'cactus / cane'
    if 'minecraft:tall_flowers' in tags or 'TallFlowerBlock' in c:
        return 'tall flower'
    if 'minecraft:small_flowers' in tags or 'minecraft:flowers' in tags or 'FlowerBlock' in c:
        return 'flower'
    if 'minecraft:saplings' in tags or 'SaplingBlock' in c or 'Propagule' in c:
        return 'sapling'
    if 'forge:mushrooms' in tags or re.search(r'Mushroom|Fungus', c) or re.search(r'mushroom|fungus|shroom', item):
        return 'mushroom'
    if re.search(r'Coral|SeaPickle|Waterlily|Kelp|Seagrass', c) or re.search(r'coral|pickle|lily_pad|kelp|seagrass', item):
        return 'water'
    if re.search(r'Vine|GrowingPlant|Multiface|CaveVines', c) or re.search(r'vine|lichen|glow_berries', item):
        return 'vine'
    if re.search(r'TallGrass|DeadBush|Roots|NetherSprouts|DoublePlant', c) or re.search(r'grass|fern|roots|sprouts|dead_bush', item):
        return 'grass / fern'
    if re.search(r'Crop|Stem|SweetBerry|Cocoa|NetherWart', c) or re.search(r'seeds?$|_crop|carrot|potato|berries|beans|nether_wart|hemp', item):
        return 'crop / seed'
    return 'other'


def main():
    tags = load_json(EXP / 'tags/item_tags.json') or {}
    tag = lambda k: set(tags.get(k, []))
    live = load_json(EXP / 'blocks/flower_pots.json')
    probe = load_json(EXP / 'blocks/pot_probe.json')
    tags_by_item = defaultdict(set)
    for k, v in tags.items():
        for i in v:
            tags_by_item[i].add(k)

    plants = {}   # item -> {chain, plantable, vanilla potted}
    if live:
        for p in live['plants']:
            plants[p['item']] = {'chain': p['chain'], 'plantable': p['plantable'], 'potted': p['vanillaPot']}
        source = 'live `/dumppots`'
    else:
        potted = static_potted()
        by_item = {it: pid for pid, it in potted.items()}
        universe = set(by_item)
        for k in ('minecraft:small_flowers', 'minecraft:tall_flowers', 'minecraft:flowers', 'minecraft:saplings',
                  'forge:mushrooms', 'supplementaries:flower_box_plantable', 'another_furniture:planter_box_placeables'):
            universe |= tag(k)
        universe |= {k2 for k, v in tags.items() if k.startswith('botania:') and 'flowers' in k for k2 in v}
        universe |= {mc(x) for x in BEAUTIFY + FLOWER_BOX_SPECIAL}
        universe -= tag('minecraft:leaves')
        for it in universe:
            plants[it] = {'chain': None, 'plantable': None, 'potted': by_item.get(it)}
        source = 'static guess from the jars and the tag dump (run `/dumppots` for the live list)'

    fbox = tag('supplementaries:flower_box_plantable') | {mc(x) for x in FLOWER_BOX_SPECIAL}
    pbox = tag('another_furniture:planter_box_placeables') - tag('another_furniture:planter_box_banned')
    tall = tag('minecraft:tall_flowers')
    small = tag('minecraft:small_flowers')
    vbox = tag('vinery:small_flower') or small

    def predict(it, p):
        chain = ' '.join(p['chain'] or [])
        is_flowerblock = ('FlowerBlock' in chain and 'TallFlowerBlock' not in chain) if p['chain'] else it in small
        vp = p['potted'] is not None
        return {
            'pot': vp, 'framed': vp, 'hanging': vp,
            'fbox': it in fbox,
            'pbox': it in pbox,
            'beautify': it in {mc(x) for x in BEAUTIFY},
            'large': it in {mc(x) for x in FLORAL_SMALL + FLORAL_TALL},
            'vpot': it in tall,
            'vbox': it in vbox,
            'silt': is_flowerblock,
            'planter': p['plantable'] if p['plantable'] is not None else None,
        }

    probe_rows = {}
    if probe:
        for col, _, ids in HOLDERS:
            for hid in ids:
                rows = probe.get('holders', {}).get(hid, {}).get('rows', {})
                for it, r in rows.items():
                    ok = r.get('n', 0) > 0 or r.get('survive') is True
                    if col == 'planter':
                        ok = r.get('survive') is True
                    probe_rows.setdefault((col, it), []).append((ok, r))

    table, mismatch, errors = [], [], []
    for it in sorted(plants, key=lambda x: (x.split(':')[0] != 'minecraft', x)):
        p = plants[it]
        cat = category(it, tags_by_item.get(it, set()), p['chain'])
        pred = predict(it, p)
        cells = {}
        for col, _, _ in HOLDERS:
            got = probe_rows.get((col, it))
            if got:
                ok = all(g[0] for g in got)
                cells[col] = 'Y' if ok else ('' if not any(g[0] for g in got) else 'part')
                for g in got:
                    if g[1].get('err'):
                        errors.append(f'{col}: `{it}`: {g[1]["err"][:140]}')
                if pred[col] is not None and bool(pred[col]) != any(g[0] for g in got):
                    mismatch.append(f'{col}: `{it}` predicted {"yes" if pred[col] else "no"}, probe {"yes" if not pred[col] else "no"}')
            else:
                cells[col] = '?' if pred[col] is None else ('Y' if pred[col] else '')
        table.append((it, cat, cells))

    counts = defaultdict(Counter)
    totals = Counter()
    for it, cat, cells in table:
        totals[cat] += 1
        for col, v in cells.items():
            if v == 'Y':
                counts[col][cat] += 1

    L = ['# Flower potting matrix (generated)', '',
         'Written by `tools/flower_pot_audit.py`; do not edit by hand. Rules and decisions: [flower_pots.md](flower_pots.md).',
         f'Plant list and vanilla pot map: {source}. Probe results: '
         + ('`kubejs/exported/blocks/pot_probe.json` (cells are what the probe saw)' if probe else
            'none yet (cells are predictions from the bytecode rules; `?` = needs the live dump)') + '.', '',
         'Y = accepted, blank = refused, part = accepted by one variant only (rich vs plain planter).', '',
         '## Summary: accepted plants per holder and kind', '',
         '| Holder | ' + ' | '.join(f'{c} ({totals[c]})' for c in CATS) + ' | All |',
         '|---|' + '---|' * (len(CATS) + 1)]
    for col, title, _ in HOLDERS:
        L.append(f'| {title} | ' + ' | '.join(str(counts[col][c]) for c in CATS) + f' | {sum(counts[col].values())} |')
    L += ['', '## Plants no flower pot takes', '',
          'Plants the vanilla pot (and so the framed and hanging pots) refuses, by kind:', '']
    gaps = defaultdict(list)
    for it, cat, cells in table:
        if cells['pot'] != 'Y':
            gaps[cat].append(it)
    for c in CATS:
        if gaps[c]:
            L.append(f'- **{c}** ({len(gaps[c])}): ' + ', '.join(f'`{x}`' for x in gaps[c]))
    if probe:
        L += ['', '## Probe against prediction', '']
        L += [f'- {m}' for m in mismatch] or ['- none']
        L += ['', '## Probe exceptions', '']
        L += [f'- {e}' for e in errors] or ['- none']
    L += ['', '## Full matrix', '']
    by_mod = defaultdict(list)
    for row in table:
        by_mod[row[0].split(':')[0]].append(row)
    head = '| Plant | Kind | ' + ' | '.join(t for _, t, _ in HOLDERS) + ' |'
    for mod in sorted(by_mod, key=lambda m: (m != 'minecraft', m)):
        L += [f'### {mod} ({len(by_mod[mod])})', '', head, '|---|---|' + '---|' * len(HOLDERS)]
        for it, cat, cells in by_mod[mod]:
            L.append(f'| `{it.split(":", 1)[1]}` | {cat} | ' + ' | '.join(cells[c] for c, _, _ in HOLDERS) + ' |')
        L.append('')
    OUT_MD.write_text('\n'.join(L) + '\n')
    print(f'{len(table)} plants, {len(mismatch)} probe mismatches, {len(errors)} probe exceptions -> {OUT_MD.relative_to(INSTANCE)}')
    for col, title, _ in HOLDERS:
        print(f'  {title}: {sum(counts[col].values())}')


if __name__ == '__main__':
    main()

#!/usr/bin/env python3
"""Wandering Trader catalog: every offer in the two trade pools, and what each category is missing.

Source data (all from /dumpall, or the single commands):
  kubejs/exported/villager_trades.json     /dumptrades: pool_1 (common) and pool_2 (rare) listings, and
                                           the Wandering Winemaker's pool (wandering_winemaker)
  kubejs/exported/tags/item_tags.json      /dumptags
  kubejs/exported/items_by_mod/*.json      /dumpitems
Names come from tools/lang_index.py (offline en_us index).

A category is a group of items the traders already sell some of (saplings, flowers, seeds, ...).
"Sold" counts the Wandering Trader's two pools and the Winemaker's pool together.
Its candidates come from tags plus id patterns; anything a candidate list should not offer is in
EXCLUDE with the reason, so the gap lists stay short and every exclusion is written down.

    python3 tools/wandering_trader_report.py    writes feature_writeup/wandering_trader_catalog.md
"""
import json
import re
from collections import defaultdict
from datetime import datetime
from pathlib import Path

import lang_index

INSTANCE = Path(__file__).resolve().parent.parent
TRADES = INSTANCE / 'kubejs/exported/villager_trades.json'
TAGS = INSTANCE / 'kubejs/exported/tags/item_tags.json'
ITEMS = INSTANCE / 'kubejs/exported/items_by_mod'
OUT = INSTANCE / 'feature_writeup/wandering_trader_catalog.md'

POOLS = {'pool_1': 'Common pool (pool 1)', 'pool_2': 'Rare pool (pool 2)'}

# Shared reasons, so the exclusion table groups cleanly.
MAGIC = 'Botania magic plant; part of its progression, not a decoration'
BUSHIER = 'Bushier Flowers grown_* block; the pack turns these off (memory bushier-flowers-disabled)'
WILD_CROP = 'wild crop plant; worldgen-only block, the seed or crop item is the thing to sell'
NETHER_END = 'Nether or End item; no trader sells these (sequence breaking), see wandering_trader.md'
NOT_PICKED = 'not picked for the trader (2026-10-03 list)'
PIPS = 'Berry Good: the berries cannot be planted; the pips are sold instead'

EXCLUDE = {
    'minecraft:wither_rose': 'only from a wither kill; selling it skips that',
    'nethersdelight:mimicarnation': 'Nether\'s Delight drop with its own effect, not a plain flower',
    'twigs:azalea_flowers': 'decoration made from azalea, not a flower plant',
    'endercrop:ender_seeds': 'crafted from ender pearls; selling it skips that',
    'immersiveengineering:light_bulb': 'not a plant (id pattern match)',
    'beautify:lamp_light_bulb': 'not a plant (id pattern match)',
    'redeco:amethyst_light_bulb': 'not a plant (id pattern match)',
    'redeco:glow_ink_light_bulb': 'not a plant (id pattern match)',
    'redeco:glowstone_light_bulb': 'not a plant (id pattern match)',
    'simplehats:sprout': 'a hat (id pattern match)',
    'neapolitan:roasted_adzuki_beans': 'cooked food, not a planting item',
    'rusticdelight:roasted_coffee_beans': 'cooked food, not a planting item',
    'rusticdelight:golden_coffee_beans': 'crafted from gold, not a planting item',
    'upgrade_aquatic:elder_prismarine_coral_block': 'Upgrade Aquatic boss/structure coral, not reef coral',
    'upgrade_aquatic:prismarine_coral_block': 'Upgrade Aquatic structure coral, not reef coral',
    'minecraft:sweet_berries': PIPS,
    'minecraft:glow_berries': PIPS,
}
for _id in [
    'minecraft:nether_wart', 'minecraft:chorus_flower', 'nethersdelight:propelplant_cane',
    'farmersrespite:coffee_beans', 'architects_palette:twisted_sapling',
    'minecraft:crimson_fungus', 'minecraft:warped_fungus', 'minecraft:crimson_roots', 'minecraft:warped_roots',
    'minecraft:nether_sprouts', 'minecraft:twisting_vines', 'minecraft:weeping_vines',
    'minecraft:crimson_nylium', 'minecraft:warped_nylium', 'minecraft:soul_soil', 'minecraft:glowstone',
    'architects_palette:sunstone', 'architects_palette:moonstone',
    'alexsmobs:cosmic_cod_bucket', 'alexsmobs:stradpole_bucket',
]:
    EXCLUDE[_id] = NETHER_END
for _id in [
    'alexsmobs:comb_jelly_bucket', 'alexsmobs:devils_hole_pupfish_bucket', 'alexsmobs:frilled_shark_bucket',
    'alexsmobs:mimic_octopus_bucket', 'alexsmobs:platypus_bucket',
]:
    EXCLUDE[_id] = NOT_PICKED

# Hand lists for categories no tag covers.
CROP_STARTERS = [
    'minecraft:potato', 'minecraft:carrot', 'minecraft:sweet_berries', 'minecraft:glow_berries',
    'minecraft:cocoa_beans', 'minecraft:nether_wart', 'minecraft:bamboo', 'minecraft:sugar_cane',
    'minecraft:cactus', 'minecraft:chorus_flower',
    'farmersdelight:onion', 'farmersdelight:rice', 'farmersrespite:coffee_beans',
    'fruitsdelight:blueberry', 'fruitsdelight:cranberry',
    'neapolitan:vanilla_pods', 'neapolitan:banana_frond', 'neapolitan:adzuki_beans', 'neapolitan:mint_sprout',
    'neapolitan:magic_beans', 'upgrade_aquatic:mulberry', 'nethersdelight:propelplant_cane',
    'the_dirty_stuff:tobacco_seeds',
]
PLANTS = [
    'minecraft:kelp', 'minecraft:sea_pickle', 'minecraft:seagrass', 'minecraft:lily_pad', 'minecraft:vine',
    'minecraft:glow_lichen', 'minecraft:small_dripleaf', 'minecraft:big_dripleaf', 'minecraft:spore_blossom',
    'minecraft:hanging_roots', 'minecraft:moss_carpet', 'minecraft:fern', 'minecraft:large_fern',
    'minecraft:grass', 'minecraft:tall_grass', 'minecraft:dead_bush', 'minecraft:twisting_vines',
    'minecraft:weeping_vines', 'minecraft:crimson_roots', 'minecraft:warped_roots', 'minecraft:nether_sprouts',
    'upgrade_aquatic:beachgrass', 'upgrade_aquatic:blue_pickerelweed', 'upgrade_aquatic:purple_pickerelweed',
    'upgrade_aquatic:driftwood_log',
    'buzzier_bees:four_leaf_clover', 'immersive_weathering:ivy', 'immersive_weathering:moss_clump',
    'immersive_weathering:frosty_grass', 'immersive_weathering:frosty_fern', 'alexsmobs:acacia_blossom',
]
NATURAL_BLOCKS = [
    'minecraft:sand', 'minecraft:red_sand', 'minecraft:podzol', 'minecraft:rooted_dirt', 'minecraft:moss_block',
    'minecraft:packed_ice', 'minecraft:blue_ice', 'minecraft:pointed_dripstone', 'minecraft:glowstone',
    'minecraft:mycelium', 'minecraft:grass_block', 'minecraft:coarse_dirt', 'minecraft:mud', 'minecraft:clay',
    'minecraft:ice', 'minecraft:snow_block', 'minecraft:dripstone_block', 'minecraft:calcite',
    'minecraft:crimson_nylium', 'minecraft:warped_nylium', 'minecraft:soul_soil',
    'minecraft:sponge',
]
# Mob-bucket mods: id ending _bucket in these namespaces is a mob bucket unless it is a fluid.
MOB_BUCKET_NS = {'minecraft', 'alexsmobs', 'upgrade_aquatic', 'quark'}
NOT_MOB_BUCKETS = {
    'minecraft:lava_bucket', 'minecraft:milk_bucket', 'minecraft:powder_snow_bucket', 'minecraft:water_bucket',
    'quark:slime_in_a_bucket',
}


def load():
    trades = json.loads(TRADES.read_text())
    tags = json.loads(TAGS.read_text())
    items = set()
    for f in ITEMS.glob('*.json'):
        items |= set(json.loads(f.read_text()).get('items', []))
    return trades, tags, items


def name_lookup():
    names, _ = lang_index.build()

    def name(item_id):
        ns, path = item_id.split(':', 1)
        for kind in ('item', 'block'):
            v = names.get(f'{kind}.{ns}.{path}')
            if v:
                return v
        return path.replace('_', ' ').title()
    return name


SOURCES = [
    ('wandering_trader', 'pool_1', 'Wandering Trader: common pool (pool 1)'),
    ('wandering_trader', 'pool_2', 'Wandering Trader: rare pool (pool 2)'),
    ('wandering_winemaker', 'pool_1', 'Wandering Winemaker (Vinery)'),
]


def pool_rows(trades):
    """[row] per distinct listing: copies of one listing (a weight) collapse into one row with weight n.
    A listing that sold different items across the samples (a random-from-tag listing) keeps them all."""
    rows = []
    for src, pool, title in SOURCES:
        groups = {}
        for r in trades.get(src, {}).get(pool, []):
            offers = [o for o in r['offers'] if 'error' not in o and o.get('result')]
            results = sorted({o['result']['item'] for o in offers})
            o = offers[0] if offers else {}
            key = (r['listing'], json.dumps(o.get('costA')), json.dumps(o.get('costB')),
                   json.dumps(o.get('result')) if len(results) == 1 else ','.join(results), o.get('maxUses'))
            if key in groups:
                groups[key]['weight'] += 1
                continue
            groups[key] = {
                'src': src, 'pool': pool, 'listing': r['listing'], 'costA': o.get('costA'), 'costB': o.get('costB'),
                'results': results, 'count': (o.get('result') or {}).get('count', 0), 'maxUses': o.get('maxUses', 0),
                'mod': results[0].split(':')[0] if results else '?', 'weight': 1,
            }
        rows.extend(groups.values())
    return rows


def categories(tags, items):
    def pat(rx):
        return {i for i in items if re.search(rx, i.split(':', 1)[1])}

    def tag(t):
        return set(tags.get(t, []))

    special = tag('botania:special_flowers')
    cats = {}
    cats['Saplings'] = tag('minecraft:saplings') | pat(r'(sapling|propagule)$')
    small = tag('minecraft:small_flowers') - special
    cats['Small flowers'] = {i for i in small if not re.search(r':wild_', i)}
    cats['Tall flowers'] = tag('minecraft:tall_flowers') - {'farmersdelight:wild_rice'}
    cats['Seeds and crop starters'] = (tag('forge:seeds') | tag('c:seeds') | set(CROP_STARTERS)
                                       | pat(r'(_seeds?|_pips)$'))
    cats['Mushrooms and fungi'] = tag('forge:mushrooms') | {'quark:glow_shroom', 'minecraft:crimson_fungus',
                                                            'minecraft:warped_fungus'}
    cats['Coral blocks'] = {i for i in pat(r'coral_block$') if ':dead_' not in i}
    cats['Mob buckets'] = {i for i in pat(r'_bucket$') if i.split(':')[0] in MOB_BUCKET_NS} - NOT_MOB_BUCKETS
    cats['Dyes'] = tag('forge:dyes') | pat(r'_dye$')
    cats['Plants (aquatic, cave, ground cover)'] = set(PLANTS)
    cats['Natural blocks'] = set(NATURAL_BLOCKS)
    return {k: {i for i in v if i in items} for k, v in cats.items()}


def exclusions(items):
    ex = dict(EXCLUDE)
    for i in items:
        if i.startswith('botania:') and re.search(r'(mystical_flower|double_flower|_mushroom|_seeds?)$', i):
            ex[i] = MAGIC
        if i.startswith('bushierflowers:grown_'):
            ex[i] = BUSHIER
        if re.search(r':wild_', i):
            ex[i] = WILD_CROP
    return ex


def md_item(item_id, name):
    return f'{name} (`{item_id}`)'


def price(cost):
    if not cost:
        return ''
    item = cost['item'].split(':')[1].replace('_', ' ')
    return f"{cost['count']} {item}"


def main():
    trades, tags, items = load()
    name = name_lookup()
    rows = pool_rows(trades)
    sold = {res for r in rows if len(r['results']) == 1 for res in r['results']}
    cats = categories(tags, items)
    ex = exclusions(items)

    out = []
    out.append('# Wandering Trader catalog (generated)')
    out.append('')
    out.append(f'Generated by `tools/wandering_trader_report.py` on {datetime.now():%Y-%m-%d %H:%M} from '
               f'`kubejs/exported/villager_trades.json`. Do not edit by hand; rerun after /dumptrades. The '
               f'analysis lives in [wandering_trader.md](wandering_trader.md).')
    out.append('')

    # --- catalog ---
    out.append('## 1. Every offer')
    out.append('')
    out.append('Weight = copies of the listing in the pool; each copy is another chance to be drawn. A row with '
               'several items is a random-from-tag listing (the items seen across the dump\'s sample rolls).')
    out.append('')
    for src, pool, title in SOURCES:
        prow = [r for r in rows if r['src'] == src and r['pool'] == pool]
        if not prow:
            continue
        entries = sum(r['weight'] for r in prow)
        by_mod = defaultdict(int)
        for r in prow:
            by_mod[r['mod']] += r['weight']
        out.append(f'### {title}: {len(prow)} listings, {entries} entries')
        out.append('')
        out.append('By mod (entries): ' + ', '.join(f'{m} {n}' for m, n in sorted(by_mod.items(), key=lambda x: (-x[1], x[0]))))
        out.append('')
        out.append('| Mod | Item | Count | Price | Max uses | Weight |')
        out.append('|---|---|---|---|---|---|')
        for r in prow:
            if not r['results']:
                out.append(f"| ? | listing `{r['listing']}` gave no offer | | | | {r['weight']} |")
                continue
            cost = price(r['costA']) + (f" + {price(r['costB'])}" if r['costB'] else '')
            if len(r['results']) == 1:
                item = md_item(r['results'][0], name(r['results'][0]))
            else:
                item = f"random, {len(r['results'])} seen: " + ', '.join(name(x) for x in r['results'])
            out.append(f"| {r['mod']} | {item} | {r['count']} | {cost} | {r['maxUses']} | {r['weight']} |")
        out.append('')

    # --- gaps ---
    out.append('## 2. Gaps by category')
    out.append('')
    out.append('Candidates come from tags plus id patterns (see the script). "Sold" counts both traders. '
               '"Excluded" items are candidates no trader should sell; the reason is in the script\'s EXCLUDE table.')
    out.append('')
    out.append('| Category | Sold | Missing | Excluded |')
    out.append('|---|---|---|---|')
    summary = []
    for cat, cand in cats.items():
        s = sorted(cand & sold)
        excl = sorted(i for i in cand - sold if i in ex)
        miss = sorted(i for i in cand - sold if i not in ex)
        summary.append((cat, s, miss, excl))
        out.append(f'| {cat} | {len(s)} | {len(miss)} | {len(excl)} |')
    out.append('')

    for cat, s, miss, excl in summary:
        out.append(f'### {cat}')
        out.append('')
        out.append(f'Sold ({len(s)}): ' + (', '.join(name(i) for i in s) if s else 'none') + '.')
        out.append('')
        if miss:
            out.append(f'Missing ({len(miss)}):')
            out.append('')
            by_mod = defaultdict(list)
            for i in miss:
                by_mod[i.split(':')[0]].append(i)
            for mod in sorted(by_mod):
                out.append(f'- **{mod}:** ' + ', '.join(md_item(i, name(i)) for i in by_mod[mod]))
            out.append('')
        else:
            out.append('Missing: none.')
            out.append('')
        if excl:
            reasons = defaultdict(list)
            for i in excl:
                reasons[ex[i]].append(i)
            out.append('Excluded:')
            out.append('')
            for reason, ids in reasons.items():
                shown = ', '.join(f'`{i}`' for i in ids[:6]) + (f' and {len(ids) - 6} more' if len(ids) > 6 else '')
                out.append(f'- {reason}: {shown}')
            out.append('')

    OUT.write_text('\n'.join(out) + '\n')
    print(f'wrote {OUT.relative_to(INSTANCE)}: {len(rows)} distinct listings, '
          + ', '.join(f'{c} {len(m)} missing' for c, _, m, _ in summary))


if __name__ == '__main__':
    main()
